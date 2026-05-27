# Reactive Mall — 用 Reactor 编排电商下单流程

## 架构

```
                    POST /api/orders
 Client ─────────────────────────────▶ order-service (:8080)
                                            │
            ┌───────────────────────────────┤  Reactor 编排链
            │            │                  │          │
    ① 检查库存     ② 创建支付     ③ 轮询支付状态    ④ 扣库存    ⑤ 创建物流
            │            │                  │          │           │
            └────────────┴──────────────────┴──────────┴───────────┘
                             WebClient (非阻塞)
                                    │
                       upstream-services (:8081)
                       ├── /api/inventory/*
                       ├── /api/payments/*
                       ├── /api/shipments/*
                       └── /api/admin/chaos/{mode}
```

## Reactor 模式速查

| 模式 | 代码位置 | 说明 |
|------|---------|------|
| **WebClient** | `client/` 包 | 非阻塞 HTTP 调用，返回 Mono |
| **flatMap 链** | `OrderOrchestrator.placeOrder()` | 5 个异步步骤串行编排 |
| **filter + repeatWhenEmpty** | 步骤 ③ 支付轮询 | 响应式轮询，替代 while+sleep |
| **timeout** | 步骤 ③ | 8 秒总超时保护 |
| **switchIfEmpty** | 步骤 ① | 库存不足时抛异常 |
| **onErrorResume** | 链尾 | 按异常类型分别降级 |
| **doOnNext** | 每个步骤 | 插入副作用（发 SSE 事件） |
| **Sinks.Many + SSE** | `OrderEventPublisher` | 实时推送订单生命周期事件 |

## 运行

**终端 1：启动上游服务**

```bash
cd upstream-services
mvn spring-boot:run
```

**终端 2：启动订单服务**

```bash
cd order-service
mvn spring-boot:run
```

## 测试

### 正常下单

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"P001","quantity":2,"price":99.9,"address":"Shanghai"}' | python3 -m json.tool
```

预期输出：

```json
{
    "orderId": "15ae9476",
    "productId": "P001",
    "quantity": 2,
    "totalPrice": 199.8,
    "status": "COMPLETED",
    "paymentId": "PAY-e6ab4816",
    "shipmentId": "SHIP-d5914af7"
}
```

### SSE 实时事件流

先打开 SSE 监听（用任意 orderId 占位），再在另一个终端下单，观察事件依次到达：

```bash
# 终端 A：监听事件
curl -N http://localhost:8080/api/orders/test123/events

# 终端 B：下单（注意先下单拿到 orderId 后，用正确的 orderId 重新监听）
```

order-service 控制台会打印每个事件：

```
[EVENT] 15ae9476 | INVENTORY_CHECKED | OK
[EVENT] 15ae9476 | PAYMENT_CREATED | PENDING
[EVENT] 15ae9476 | PAYMENT_CONFIRMED | PAID
[EVENT] 15ae9476 | INVENTORY_DEDUCTED | OK
[EVENT] 15ae9476 | SHIPMENT_CREATED | OK
[EVENT] 15ae9476 | ORDER_COMPLETED | COMPLETED
```

### 库存不足（switchIfEmpty → OutOfStockException）

P003 库存为 0：

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"P003","quantity":1,"price":10,"address":"Beijing"}' | python3 -m json.tool
```

预期：`"status": "FAILED_OUT_OF_STOCK"`

### 故障注入（onErrorResume）

```bash
# 切换上游为 FAIL 模式
curl -X POST http://localhost:8081/api/admin/chaos/fail

# 下单 → 库存检查直接 500 → onErrorResume 兜底
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"P001","quantity":1,"price":50,"address":"Hangzhou"}' | python3 -m json.tool

# 预期："status": "FAILED_ERROR"

# 恢复正常
curl -X POST http://localhost:8081/api/admin/chaos/normal
```

### 慢响应（timeout）

```bash
# 切换上游为 SLOW 模式（2 秒延迟）
curl -X POST http://localhost:8081/api/admin/chaos/slow

# 下单 → 库存检查超时 → onErrorResume 兜底
curl -s -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{"productId":"P001","quantity":1,"price":50,"address":"Hangzhou"}' | python3 -m json.tool

# 恢复
curl -X POST http://localhost:8081/api/admin/chaos/normal
```

### 查询订单

```bash
curl -s http://localhost:8080/api/orders/{orderId} | python3 -m json.tool
```

## 预装数据

| 商品 ID | 库存 | 用途 |
|---------|------|------|
| P001 | 100 | 正常下单 |
| P002 | 50 | 正常下单 |
| P003 | 0 | 测试库存不足 |

## 核心源码导读

**从这个文件开始读：** `order-service/.../service/OrderOrchestrator.java`

这个文件是整个示例的核心——一条 Reactor 链串联了 5 个异步步骤，每个步骤都有详细的中文注释，解释了对应的 Reactor 模式和命令式写法的对比。

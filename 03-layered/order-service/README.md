# Order Service — Spring Boot 三层架构示例

演示三个核心概念：分层架构、依赖注入、AOP。

## 项目结构

```
src/main/java/sa/examples/order/
├── OrderApplication.java          # Spring Boot 入口
├── controller/
│   └── OrderController.java       # Presentation Layer（处理 HTTP）
├── service/
│   └── OrderService.java          # Business Logic Layer（业务规则 + @Transactional）
├── repository/
│   └── OrderRepository.java       # Data Access Layer（Spring Data JPA）
├── model/
│   └── Order.java                 # JPA Entity
└── aspect/
    └── LayerLoggingAspect.java    # AOP 切面（跨层日志）
```

## 教学要点

### 1. 分层架构
Controller → Service → Repository 严格单向依赖，每层职责清晰。

### 2. 依赖注入
通过构造器注入组装三层，Spring IoC 容器负责创建和连接对象。

### 3. AOP
- `LayerLoggingAspect`：自定义切面，拦截三层方法调用，打印进入/离开日志
- `@Transactional`：声明式事务，Spring AOP 的内置应用

## 运行

```bash
mvn spring-boot:run
```

## 测试接口

```bash
# 创建订单
curl -X POST localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"items":"Book,Pen","price":42.0}'

# 查询所有订单
curl localhost:8080/orders

# 按 ID 查询
curl localhost:8080/orders/1
```

观察控制台输出，可以看到 AOP 切面打印的跨层调用日志：
```
[Controller] --> OrderController.create(..)
[Service]    --> OrderService.createOrder(..)
[Repository] --> OrderRepository.save(..)
[Repository] <-- OrderRepository.save(..) (5ms)
[Service]    <-- OrderService.createOrder(..) (12ms)
[Controller] <-- OrderController.create(..) (15ms)
```

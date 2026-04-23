# spring-cloud-function-intro

Spring Cloud Function 基础示例，对应讲义 [08-serverless.md](../../../08-serverless.md) 中 "Spring Cloud Function"、"函数类型"、"函数示例" 三页。

## 演示内容

| 函数 | 类型 | 说明 |
|------|------|------|
| `uppercase` | `Function<String,String>` | 最基础的 Function |
| `reverseString` | `Function<String,String>` | 用于组合 |
| `uppercaseFlux` | `Function<Flux,Flux>` | 响应式流处理 |
| `greeter` | `Function<String,String>`（POJO 实现） | 实现 Function 接口的类 |
| `logEvent` | `Consumer<String>` | 只消费，无输出 |
| `timestamp` | `Supplier<String>` | 只生产，无输入 |
| `counter` | `Supplier<Long>` | 有状态 Supplier |
| `uppercase\|reverseString` | 组合函数 | `reverseString(uppercase(x))` |

## 运行

```bash
mvn spring-boot:run
```

## 调用示例

```bash
# Function
curl -d hello http://localhost:8080/uppercase
# -> HELLO

# 组合函数（| 在 URL 中用 , 替代）
curl -d hello http://localhost:8080/uppercase,reverseString
# -> OLLEH

# POJO Function
curl -d world http://localhost:8080/greeter
# -> Hello world, welcome to Spring Cloud Function!

# Supplier（GET，无输入）
curl http://localhost:8080/timestamp
curl http://localhost:8080/counter
curl http://localhost:8080/counter   # 第二次递增

# Consumer（无响应体，仅在控制台打印）
curl -d "user-login" http://localhost:8080/logEvent
```

## 测试

```bash
mvn test
```

单元测试通过 `FunctionCatalog` 直接查找并调用每个函数，覆盖所有演示场景。

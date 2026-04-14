# Spring MVC Greeting 示例

这是一个简单的 Spring MVC 应用，展示了 Controller、Model 和 View 的基本用法。

## 功能

访问 `/greeting?name=YourName`，页面显示问候语。

## 运行

```bash
mvn spring-boot:run
```

访问：http://localhost:8080/greeting

## MVC 架构

- **Controller**：`GreetingController` 处理请求
- **Model**：通过 `Model.addAttribute()` 传递数据
- **View**：Thymeleaf 模板渲染 HTML

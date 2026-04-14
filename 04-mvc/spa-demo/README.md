# SPA Demo

这是一个简单的 SPA (Single Page Application) 示例，展示了前后端分离架构。

## 架构

- **后端**：Spring Boot REST API（返回 JSON）
- **前端**：Vue.js SPA（局部更新，无需刷新页面）

## 运行

```bash
mvn spring-boot:run
```

访问：http://localhost:8080

## 特点

- 后端只提供 API，不渲染 HTML
- 前端通过 AJAX 获取数据，动态更新页面
- 页面无需刷新，用户体验更流畅

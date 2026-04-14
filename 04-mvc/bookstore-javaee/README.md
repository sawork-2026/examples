# Java EE 在线书店示例

这是一个简化的 Java EE 应用示例，展示了传统的 Java EE 架构（EJB + Servlet + JSP）。

## 架构

- **Web 层**：Servlet 处理 HTTP 请求，JSP 渲染页面
- **业务层**：EJB (Enterprise JavaBean) 封装业务逻辑
- **持久层**：JPA (Java Persistence API) 管理数据库

## 项目结构

```
bookstore-javaee/
├── src/main/java/
│   └── com/example/bookstore/
│       ├── entity/
│       │   └── Book.java           # JPA 实体
│       ├── ejb/
│       │   └── BookService.java    # EJB 业务逻辑
│       └── servlet/
│           └── BookServlet.java    # Servlet 控制器
├── src/main/webapp/
│   ├── WEB-INF/
│   │   └── web.xml                 # 部署描述符
│   └── books.jsp                   # JSP 视图
└── pom.xml
```

## 技术栈

- Java EE 7+
- EJB 3.2
- JPA 2.1
- Servlet 3.1
- JSP 2.3

## 运行

需要部署到 Java EE 应用服务器（如 WildFly、GlassFish、TomEE）。

```bash
mvn clean package
# 将生成的 WAR 文件部署到应用服务器
```

# Servlet 在线书店示例

这是一个纯 Servlet 应用示例，展示了传统的 Servlet + JSP 架构（不使用 EJB 和 JPA）。

## 架构

- **Web 层**：Servlet 处理 HTTP 请求，JSP 渲染页面
- **数据层**：内存中的 ArrayList 存储数据（简化实现）

## 项目结构

```
bookstore-servlet/
├── src/main/java/
│   └── com/example/bookstore/
│       ├── model/
│       │   └── Book.java           # 数据模型
│       ├── dao/
│       │   └── BookDAO.java        # 数据访问对象
│       └── servlet/
│           └── BookServlet.java    # Servlet 控制器
├── src/main/webapp/
│   ├── WEB-INF/
│   │   └── web.xml                 # 部署描述符
│   └── books.jsp                   # JSP 视图
└── pom.xml
```

## 技术栈

- Servlet 3.1
- JSP 2.3
- 内存存储（ArrayList）

## 运行

部署到 Servlet 容器（如 Tomcat）：

```bash
mvn clean package
# 将生成的 WAR 文件部署到 Tomcat webapps 目录
```

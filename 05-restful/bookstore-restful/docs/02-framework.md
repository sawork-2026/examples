# 框架架构：请求处理链路

## 请求生命周期

```
浏览器
  └─ TCP 连接 → HttpServer（socket accept + 线程）
       └─ _parse()         原始字节 → Request 对象
            └─ _dispatch()
                 ├─ StaticFileHandler   /static/* 直接读文件
                 └─ Router.match()      URL 正则匹配 → (handler, Request)
                      └─ HandlerAdapter.handle()   参数绑定 → 调用 controller 方法
                           └─ View.render()         → (status, headers, body)
                                └─ HttpServer._send()   HTTP 响应写回 socket
```

## 各组件职责

### HttpServer
- `start()`：socket 监听，每个连接起一个 daemon 线程
- `_parse()`：手动解析 HTTP 报文（`\r\n\r\n` 分割 header/body）
- `_dispatch()`：静态文件优先，否则走路由

### Router（对应 HandlerMapping）
- 启动时 `register_controller()` 扫描 controller 实例的 `@route` 方法
- `/books/<id>` 转成正则 `^/books/([^/]+)$`，param_names 记录捕获组名
- `match()` 返回 `(handler, 注入了 path_params 的 Request)`

### HandlerAdapter
- 用 `inspect.signature` 读取方法参数名
- 按名字从 path_params / query_params / form 里取值
- 自动类型转换（`param.annotation(val)`）

### View 体系

| 类 | 触发方式 | render() 返回 |
|----|---------|---------------|
| `TemplateView` | `view("tmpl", model)` | 200 + Jinja2 渲染的 HTML |
| `RedirectView` | `redirect("/url")` | 302 + Location 头 |
| `JsonView` | `json_view(data)` | 200 + JSON bytes |

`ViewResolver` 封装 Jinja2，`set_resolver()` 在启动时由 HttpServer 调用，
使 `view()` 工厂函数内部能访问到 resolver 实例。

### StaticFileHandler
- 匹配 `/static/*`，映射到 `static/` 目录
- 按扩展名返回正确的 MIME 类型

## IoC 容器的作用

`main.py` 用容器创建所有组件，依赖自动注入：

```
HttpServer
  ├─ Router
  ├─ HandlerAdapter
  ├─ ViewResolver
  └─ StaticFileHandler

BookController
  └─ BookService
       └─ BookRepository
```

容器不参与请求处理，只负责启动时的组装。

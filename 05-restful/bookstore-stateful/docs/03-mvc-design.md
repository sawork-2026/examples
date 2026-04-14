# MVC 三层分离设计

## Model、View、Controller 在哪里

| MVC 角色 | 本项目对应 | 职责 |
|----------|-----------|------|
| Model | `bookstore/models.py` + `repository.py` + `service.py` | 数据结构、存储、业务逻辑 |
| View | `templates/*.html` + `mvc/view.py` + `mvc/view_resolver.py` | 数据渲染成 HTML/JSON |
| Controller | `bookstore/controller.py` + `api_controller.py` | 接收请求、调用 Service、返回 View |

Controller 只做两件事：从 Request 取参数、调 Service、返回 View。不含业务逻辑。

## HTML 视图 vs REST API

同一个 `BookService`，暴露了两种访问方式：

```python
# BookController（对应 Spring @Controller）
@route("/books", method="GET")
def list(self, request):
    books = self.book_service.find_all()
    return view("book/list.html", {"books": books})   # 返回 HTML

# BookApiController（对应 Spring @RestController）
@route("/api/books", method="GET")
def list(self, request):
    books = self.book_service.find_all()
    return json_view([b.to_dict() for b in books])    # 返回 JSON
```

区别只在最后一行：`view()` vs `json_view()`。
Service 层完全不感知上层用什么格式响应，这正是 MVC 分离的价值。

## 表单 DELETE 问题

HTML `<form>` 只支持 GET/POST，无法发 DELETE。
通过隐藏字段 `_method=DELETE` 模拟，Controller 读取后路由到删除逻辑：

```python
@route("/books/<id>", method="POST")
def update(self, request, id: str):
    if request.form().get("_method") == "DELETE":
        self.book_service.delete(id)
        return redirect("/books")
    ...
```

这是 Rails/Laravel 等框架的标准做法，Spring MVC 通过 `HiddenHttpMethodFilter` 实现相同功能。

## 与 Spring MVC 的对应关系

| 本框架 | Spring MVC |
|--------|------------|
| `HttpServer` | Tomcat + `DispatcherServlet` |
| `Router` + `@route` | `RequestMappingHandlerMapping` + `@RequestMapping` |
| `HandlerAdapter` | `RequestMappingHandlerAdapter` |
| `view(tmpl, model)` | `ModelAndView` |
| `json_view(data)` | `@ResponseBody` / `@RestController` |
| `ViewResolver` | `InternalResourceViewResolver` |
| `redirect(url)` | `redirect:` 前缀 / `RedirectView` |
| `StaticFileHandler` | `ResourceHttpRequestHandler` |

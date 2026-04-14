"""Router：URL 路由表（对应 Spring HandlerMapping）"""
import re
import inspect
from mvc.request import Request


class Router:
    """扫描容器中所有已注册的 @route 方法，建立路由表"""

    def __init__(self):
        # list of (method, pattern, param_names, handler_func, controller_instance)
        self._routes: list = []

    def register_controller(self, instance) -> None:
        for _, method in inspect.getmembers(instance, predicate=inspect.ismethod):
            if not getattr(method, '_is_route', False):
                continue
            path = method._route_path
            http_method = method._route_method
            # 把 /books/<id> 转成正则
            param_names = re.findall(r'<(\w+)>', path)
            pattern = re.sub(r'<\w+>', r'([^/]+)', path)
            pattern = f'^{pattern}$'
            self._routes.append((http_method, re.compile(pattern), param_names, method))

    def match(self, request: Request):
        """返回 (handler, request_with_path_params) 或 None
        固定路径优先于参数路径（/books/new 优先于 /books/<id>）
        """
        # 固定路径（无参数）优先匹配
        sorted_routes = sorted(self._routes, key=lambda r: len(r[2]))  # param_names 少的先
        for http_method, pattern, param_names, handler in sorted_routes:
            if request.method() != http_method:
                continue
            m = pattern.match(request.path())
            if m:
                path_params = dict(zip(param_names, m.groups()))
                return handler, request.with_path_params(path_params)
        return None, None

"""@route 装饰器（对应 Spring @RequestMapping）"""

_routes: list = []


def route(path: str, method: str = "GET"):
    def decorator(func):
        func._is_route = True
        func._route_path = path
        func._route_method = method.upper()
        return func
    return decorator

"""HandlerAdapter：调用 controller 方法（对应 Spring HandlerAdapter）"""
import inspect
from mvc.request import Request
from mvc.view import View


class HandlerAdapter:
    def handle(self, handler, request: Request) -> View:
        sig = inspect.signature(handler)
        params = list(sig.parameters.values())
        args = []
        path_params = request.path_params()
        query_params = request.query_params()
        for p in params:
            if p.name == 'request':
                args.append(request)
            elif p.name in path_params:
                val = path_params[p.name]
                if p.annotation not in (inspect.Parameter.empty, str):
                    val = p.annotation(val)
                args.append(val)
            elif p.name in query_params:
                args.append(query_params[p.name])
            elif request.method() == 'POST' and p.name in request.form():
                args.append(request.form()[p.name])
        return handler(*args)

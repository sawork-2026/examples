"""View 抽象及三种实现（对应 Spring ModelAndView / ViewResolver）"""
import json


class View:
    def render(self) -> tuple:
        raise NotImplementedError


class TemplateView(View):
    def __init__(self, template: str, model: dict, resolver):
        self._template = template
        self._model = model
        self._resolver = resolver

    def render(self):
        body = self._resolver.resolve(self._template, self._model)
        return 200, {"Content-Type": "text/html; charset=utf-8"}, body


class RedirectView(View):
    def __init__(self, url: str):
        self._url = url

    def render(self):
        return 302, {"Location": self._url}, b""


class JsonView(View):
    def __init__(self, data, status: int = 200, headers: dict | None = None):
        self._data = data
        self._status = status
        self._headers = headers or {}

    def render(self):
        body = json.dumps(self._data, ensure_ascii=False).encode("utf-8")
        headers = {"Content-Type": "application/json; charset=utf-8"}
        headers.update(self._headers)
        return self._status, headers, body


# 全局 resolver 引用，由 HttpServer 启动时设置
_resolver = None


def set_resolver(resolver):
    global _resolver
    _resolver = resolver


def view(template: str, model: dict) -> View:
    return TemplateView(template, model, _resolver)


def redirect(url: str) -> View:
    return RedirectView(url)


def json_view(data, status: int = 200, headers: dict | None = None) -> View:
    return JsonView(data, status=status, headers=headers)

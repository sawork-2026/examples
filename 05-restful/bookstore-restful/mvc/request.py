"""Request 对象（对应 HttpServletRequest）"""
from urllib.parse import parse_qs


class Request:
    def __init__(self, method: str, path: str, query_string: str,
                 headers: dict, body: bytes, path_params: dict = None):
        self._method = method
        self._path = path
        self._query_string = query_string
        self._headers = headers
        self._body = body
        self._path_params = path_params or {}

    def path(self) -> str:
        return self._path

    def method(self) -> str:
        return self._method

    def query_params(self) -> dict:
        parsed = parse_qs(self._query_string, keep_blank_values=True)
        return {k: v[0] if len(v) == 1 else v for k, v in parsed.items()}

    def path_params(self) -> dict:
        return self._path_params

    def form(self) -> dict:
        from urllib.parse import parse_qs
        parsed = parse_qs(self._body.decode("utf-8", errors="replace"))
        return {k: v[0] if len(v) == 1 else v for k, v in parsed.items()}

    def body(self) -> bytes:
        return self._body

    def headers(self) -> dict:
        return self._headers

    def with_path_params(self, params: dict) -> 'Request':
        return Request(self._method, self._path, self._query_string,
                       self._headers, self._body, params)

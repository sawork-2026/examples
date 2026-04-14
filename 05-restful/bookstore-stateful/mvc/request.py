"""Request 对象（对应 HttpServletRequest）"""
from urllib.parse import parse_qs


class Request:
    def __init__(self, method: str, path: str, query_string: str,
                 headers: dict, body: bytes, path_params: dict = None,
                 cookies: dict = None, session: dict | None = None):
        self._method = method
        self._path = path
        self._query_string = query_string
        self._headers = headers
        self._body = body
        self._path_params = path_params or {}
        self._cookies = cookies or {}
        self._session = session if session is not None else {}

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

    def cookies(self) -> dict:
        return self._cookies

    def session(self) -> dict:
        return self._session

    def with_path_params(self, params: dict) -> 'Request':
        return Request(self._method, self._path, self._query_string,
                       self._headers, self._body, params,
                       self._cookies, self._session)

    def with_session(self, session: dict) -> 'Request':
        return Request(self._method, self._path, self._query_string,
                       self._headers, self._body, self._path_params,
                       self._cookies, session)

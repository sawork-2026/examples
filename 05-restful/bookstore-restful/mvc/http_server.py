"""HttpServer：socket 实现的 HTTP 服务器（对应 Tomcat + DispatcherServlet）"""
import socket
import threading
from mvc.request import Request
from mvc.router import Router
from mvc.handler_adapter import HandlerAdapter
from mvc.view_resolver import ViewResolver
from mvc.static_handler import StaticFileHandler
from mvc import view as view_module
from ioc.decorators import component


@component
class HttpServer:
    def __init__(self, router: Router, handler_adapter: HandlerAdapter,
                 view_resolver: ViewResolver, static_handler: StaticFileHandler):
        self.router = router
        self.handler_adapter = handler_adapter
        self.view_resolver = view_resolver
        self.static_handler = static_handler
        view_module.set_resolver(view_resolver)

    def start(self, host: str = "localhost", port: int = 8080):
        print(f"启动 HTTP 服务器: http://{host}:{port}")
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            s.bind((host, port))
            s.listen()
            while True:
                conn, addr = s.accept()
                threading.Thread(target=self._handle, args=(conn,), daemon=True).start()

    def _handle(self, conn: socket.socket):
        try:
            # 读取完整 headers（直到 \r\n\r\n）
            raw = b""
            while b"\r\n\r\n" not in raw:
                chunk = conn.recv(4096)
                if not chunk:
                    return
                raw += chunk
            header_part, _, rest = raw.partition(b"\r\n\r\n")
            # 按 Content-Length 读取 body
            content_length = 0
            for line in header_part.decode("utf-8", errors="replace").split("\r\n")[1:]:
                if line.lower().startswith("content-length:"):
                    content_length = int(line.split(":", 1)[1].strip())
                    break
            body = rest
            while len(body) < content_length:
                chunk = conn.recv(min(4096, content_length - len(body)))
                if not chunk:
                    break
                body += chunk
            request = self._parse(header_part + b"\r\n\r\n" + body)
            status, headers, resp_body = self._dispatch(request)
            self._send(conn, status, headers, resp_body)
        except Exception as e:
            try:
                self._send(conn, 500, {"Content-Type": "text/plain"}, str(e).encode())
            except Exception:
                pass
        finally:
            conn.close()

    def _parse(self, data: bytes) -> Request:
        header_data, _, body = data.partition(b"\r\n\r\n")
        lines = header_data.decode("utf-8", errors="replace").split("\r\n")
        method, full_path, _ = lines[0].split(" ", 2)
        if "?" in full_path:
            path, query_string = full_path.split("?", 1)
        else:
            path, query_string = full_path, ""
        headers = {}
        for line in lines[1:]:
            if ": " in line:
                k, v = line.split(": ", 1)
                headers[k] = v
        return Request(method, path, query_string, headers, body)

    def _dispatch(self, request: Request) -> tuple:
        # 静态文件
        result = self.static_handler.handle(request.path())
        if result is not None:
            return result
        # 路由分发
        handler, req = self.router.match(request)
        if handler is None:
            return 404, {"Content-Type": "text/html"}, b"<h1>404 Not Found</h1>"
        view = self.handler_adapter.handle(handler, req)
        return view.render()

    def _send(self, conn: socket.socket, status: int, headers: dict, body: bytes):
        status_text = {200: "OK", 201: "Created", 202: "Accepted", 204: "No Content",
                       302: "Found", 400: "Bad Request", 404: "Not Found",
                       409: "Conflict", 500: "Internal Server Error"}
        resp = f"HTTP/1.1 {status} {status_text.get(status, 'OK')}\r\n"
        headers["Content-Length"] = str(len(body))
        headers.setdefault("Connection", "close")
        for k, v in headers.items():
            resp += f"{k}: {v}\r\n"
        resp += "\r\n"
        conn.sendall(resp.encode() + body)

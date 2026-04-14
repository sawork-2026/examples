"""静态文件处理（对应 Servlet 容器内置的静态资源处理）"""
import os

MIME_TYPES = {
    ".html": "text/html",
    ".css": "text/css",
    ".js": "application/javascript",
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".ico": "image/x-icon",
    ".json": "application/json",
}


class StaticFileHandler:
    def __init__(self, static_dir: str = "static"):
        self._dir = static_dir

    def handle(self, path: str):
        """返回 (status, headers, body) 或 None（路径不匹配）"""
        if not path.startswith("/static/"):
            return None
        rel = path[len("/static/"):]
        full = os.path.join(self._dir, rel)
        if not os.path.isfile(full):
            return 404, {"Content-Type": "text/plain"}, b"Not Found"
        ext = os.path.splitext(full)[1].lower()
        mime = MIME_TYPES.get(ext, "application/octet-stream")
        with open(full, "rb") as f:
            body = f.read()
        return 200, {"Content-Type": mime}, body

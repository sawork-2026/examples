"""Order 模型：资源状态保存在服务端。"""
import uuid


class Order:
    def __init__(self, book_ids: list[str], status: str = "CREATED", order_id: str | None = None):
        self.id = order_id or str(uuid.uuid4())[:8]
        self.book_ids = book_ids
        self.status = status


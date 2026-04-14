"""Book 模型"""
import uuid


class Book:
    def __init__(self, title: str, author: str, price: float, stock: int = 0, book_id: str = None):
        self.id = book_id or str(uuid.uuid4())[:8]
        self.title = title
        self.author = author
        self.price = price
        self.stock = stock

    def to_dict(self):
        return {"id": self.id, "title": self.title,
                "author": self.author, "price": self.price, "stock": self.stock}

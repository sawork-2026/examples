"""OrderService：RESTful 订单资源。"""
from ioc.decorators import component
from bookstore.order_models import Order
from bookstore.order_repository import OrderRepository
from bookstore.service import BookService


@component
class OrderService:
    def __init__(self, repo: OrderRepository, book_service: BookService):
        self._repo = repo
        self._book_service = book_service

    def create(self, book_ids: list[str]) -> Order:
        valid_books = []
        for book_id in book_ids:
            book = self._book_service.find_by_id(book_id)
            if not book:
                continue
            if book.stock <= 0:
                raise ValueError(f"book {book_id} is out of stock")
            valid_books.append(book)
        if not valid_books:
            raise ValueError("no valid books to order")
        for book in valid_books:
            book.stock -= 1
            self._book_service.update(book.id, {
                "title": book.title,
                "author": book.author,
                "price": book.price,
                "stock": book.stock,
            })
        order = Order([book.id for book in valid_books])
        return self._repo.save(order)

    def find_all(self) -> list[Order]:
        return self._repo.find_all()

    def find_by_id(self, order_id: str) -> Order | None:
        return self._repo.find_by_id(order_id)

    def mark_paid(self, order_id: str) -> Order | None:
        order = self._repo.find_by_id(order_id)
        if not order or order.status != "CREATED":
            return None
        order.status = "PAID"
        return self._repo.save(order)

    def cancel(self, order_id: str) -> Order | None:
        order = self._repo.find_by_id(order_id)
        if not order or order.status != "CREATED":
            return None
        for book_id in order.book_ids:
            book = self._book_service.find_by_id(book_id)
            if not book:
                continue
            book.stock += 1
            self._book_service.update(book.id, {
                "title": book.title,
                "author": book.author,
                "price": book.price,
                "stock": book.stock,
            })
        order.status = "CANCELLED"
        return self._repo.save(order)

"""BookService：业务逻辑层"""
from bookstore.models import Book
from bookstore.repository import BookRepository
from ioc.decorators import component


@component
class BookService:
    def __init__(self, repo: BookRepository):
        self._repo = repo

    def find_all(self) -> list[Book]:
        return self._repo.find_all()

    def find_by_id(self, book_id: str) -> Book | None:
        return self._repo.find_by_id(book_id)

    def add(self, data: dict) -> Book:
        book = Book(
            title=data["title"],
            author=data["author"],
            price=float(data.get("price", 0)),
            stock=int(data.get("stock", 0)),
        )
        self._repo.save(book)
        return book

    def update(self, book_id: str, data: dict) -> Book | None:
        book = self._repo.find_by_id(book_id)
        if not book:
            return None
        book.title = data.get("title", book.title)
        book.author = data.get("author", book.author)
        book.price = float(data.get("price", book.price))
        book.stock = int(data.get("stock", book.stock))
        self._repo.save(book)
        return book

    def delete(self, book_id: str) -> bool:
        return self._repo.delete(book_id)

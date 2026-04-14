"""BookRepository：SQLite 存储"""
import sqlite3
from bookstore.models import Book
from ioc.decorators import component, post_construct


@component
class BookRepository:
    def __init__(self):
        self._conn: sqlite3.Connection | None = None

    @post_construct
    def init(self):
        self._conn = sqlite3.connect("books.db", check_same_thread=False)
        self._conn.execute(
            "CREATE TABLE IF NOT EXISTS books "
            "(id TEXT PRIMARY KEY, title TEXT, author TEXT, price REAL)"
        )
        self._conn.commit()
        # 插入种子数据
        if self._conn.execute("SELECT COUNT(*) FROM books").fetchone()[0] == 0:
            seeds = [
                ("Python编程：从入门到实践", "Eric Matthes", 89.0),
                ("Clean Code", "Robert C. Martin", 79.0),
                ("设计模式", "GoF", 99.0),
            ]
            for title, author, price in seeds:
                b = Book(title, author, price)
                self._conn.execute(
                    "INSERT INTO books VALUES (?,?,?,?)",
                    (b.id, b.title, b.author, b.price)
                )
            self._conn.commit()

    def find_all(self) -> list[Book]:
        rows = self._conn.execute("SELECT id,title,author,price FROM books").fetchall()
        return [Book(r[1], r[2], r[3], r[0]) for r in rows]

    def find_by_id(self, book_id: str) -> Book | None:
        row = self._conn.execute(
            "SELECT id,title,author,price FROM books WHERE id=?", (book_id,)
        ).fetchone()
        return Book(row[1], row[2], row[3], row[0]) if row else None

    def save(self, book: Book) -> None:
        self._conn.execute(
            "INSERT OR REPLACE INTO books VALUES (?,?,?,?)",
            (book.id, book.title, book.author, book.price)
        )
        self._conn.commit()

    def delete(self, book_id: str) -> bool:
        cursor = self._conn.execute("DELETE FROM books WHERE id=?", (book_id,))
        self._conn.commit()
        return cursor.rowcount > 0

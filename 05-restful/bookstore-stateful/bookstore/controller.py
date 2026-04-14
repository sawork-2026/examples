"""BookController：HTML 页面控制器（对应 Spring @Controller）"""
from bookstore.service import BookService
from mvc.decorators import route
from mvc.view import view, redirect
from ioc.decorators import component


@component(layer="controller")
class BookController:
    def __init__(self, book_service: BookService):
        self.book_service = book_service

    @route("/", method="GET")
    def index(self, request):
        return redirect("/books")

    @route("/books", method="GET")
    def list(self, request):
        books = self.book_service.find_all()
        cart_size = len(request.session().get("cart_ids", []))
        return view("book/list.html", {"books": books, "cart_size": cart_size})

    @route("/books/new", method="GET")
    def new(self, request):
        return view("book/new.html", {})

    @route("/books/<id>", method="GET")
    def show(self, request, id: str):
        book = self.book_service.find_by_id(id)
        if not book:
            return view("error.html", {"message": f"书籍 {id} 不存在"})
        return view("book/show.html", {"book": book, "cart_size": len(request.session().get("cart_ids", []))})

    @route("/books", method="POST")
    def create(self, request):
        self.book_service.add(request.form())
        return redirect("/books")

    @route("/books/<id>/edit", method="GET")
    def edit(self, request, id: str):
        book = self.book_service.find_by_id(id)
        return view("book/edit.html", {"book": book})

    @route("/books/<id>", method="POST")
    def update(self, request, id: str):
        data = request.form()
        if data.get("_method") == "DELETE":
            self.book_service.delete(id)
            return redirect("/books")
        self.book_service.update(id, data)
        return redirect(f"/books/{id}")

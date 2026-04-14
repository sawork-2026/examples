"""BookApiController：REST JSON API（对应 Spring @RestController）"""
from bookstore.service import BookService
from mvc.decorators import route
from mvc.view import json_view
from ioc.decorators import component


@component(layer="controller")
class BookApiController:
    def __init__(self, book_service: BookService):
        self.book_service = book_service

    @route("/api/books", method="GET")
    def list(self, request):
        return json_view([b.to_dict() for b in self.book_service.find_all()])

    @route("/api/books/<id>", method="GET")
    def show(self, request, id: str):
        book = self.book_service.find_by_id(id)
        if not book:
            return json_view({"error": "not found"})
        return json_view(book.to_dict())

    @route("/api/books", method="POST")
    def create(self, request):
        import json
        try:
            data = json.loads(request.body())
        except Exception:
            data = request.form()
        book = self.book_service.add(data)
        return json_view(book.to_dict())

    @route("/api/books/<id>", method="DELETE")
    def delete(self, request, id: str):
        ok = self.book_service.delete(id)
        return json_view({"deleted": ok})

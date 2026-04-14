"""RESTfulBookstoreController：无状态、资源导向的 JSON API。"""
import json

from ioc.decorators import component
from mvc.decorators import route
from mvc.view import json_view
from bookstore.service import BookService
from bookstore.order_service import OrderService


@component(layer="controller")
class RestfulBookstoreController:
    def __init__(self, book_service: BookService, order_service: OrderService):
        self.book_service = book_service
        self.order_service = order_service

    @route("/api", method="GET")
    def api_root(self, request):
        return json_view({
            "message": "bookstore-restful",
            "_links": {
                "books": {"href": "/api/books"},
                "orders": {"href": "/api/orders"},
            },
        })

    @route("/api/books", method="GET")
    def books(self, request):
        books = [self._book_repr(book) for book in self.book_service.find_all()]
        return json_view({
            "count": len(books),
            "_links": {"self": {"href": "/api/books"}},
            "_embedded": {"books": books},
        })

    @route("/api/books/<id>", method="GET")
    def book(self, request, id: str):
        book = self.book_service.find_by_id(id)
        if not book:
            return json_view({"error": "not found"}, status=404)
        return json_view(self._book_repr(book))

    @route("/api/orders", method="GET")
    def orders(self, request):
        orders = [self._order_repr(order) for order in self.order_service.find_all()]
        return json_view({
            "count": len(orders),
            "_links": {"self": {"href": "/api/orders"}},
            "_embedded": {"orders": orders},
        })

    @route("/api/orders", method="POST")
    def create_order(self, request):
        try:
            data = json.loads(request.body() or b"{}")
        except Exception:
            data = request.form()
        book_ids = data.get("bookIds") or []
        if isinstance(book_ids, str):
            book_ids = [book_ids]
        if not book_ids:
            return json_view({"error": "bookIds is required"}, status=400)
        try:
            order = self.order_service.create(book_ids)
        except ValueError as exc:
            return json_view({"error": str(exc)}, status=409)
        return json_view(
            self._order_repr(order),
            status=201,
            headers={"Location": f"/api/orders/{order.id}"},
        )

    @route("/api/orders/<id>", method="GET")
    def order(self, request, id: str):
        order = self.order_service.find_by_id(id)
        if not order:
            return json_view({"error": "not found"}, status=404)
        return json_view(self._order_repr(order))

    @route("/api/orders/<id>/payment", method="POST")
    def pay_order(self, request, id: str):
        order = self.order_service.mark_paid(id)
        if not order:
            return json_view({"error": "order not payable"}, status=409)
        return json_view(self._order_repr(order))

    @route("/api/orders/<id>/cancellation", method="POST")
    def cancel_order(self, request, id: str):
        order = self.order_service.cancel(id)
        if not order:
            return json_view({"error": "order not cancellable"}, status=409)
        return json_view(self._order_repr(order))

    def _book_repr(self, book):
        data = book.to_dict()
        data["_links"] = {
            "self": {"href": f"/api/books/{book.id}"},
            "collection": {"href": "/api/books"},
            "orders": {"href": "/api/orders"},
        }
        return data

    def _order_repr(self, order):
        data = {
            "id": order.id,
            "bookIds": order.book_ids,
            "status": order.status,
            "_links": {
                "self": {"href": f"/api/orders/{order.id}"},
                "collection": {"href": "/api/orders"},
            },
        }
        if order.status == "CREATED":
            data["_links"]["payment"] = {"href": f"/api/orders/{order.id}/payment"}
            data["_links"]["cancel"] = {"href": f"/api/orders/{order.id}/cancellation"}
        return data

"""StatefulBookController：演示服务端 Session 状态管理。"""
from bookstore.service import BookService
from mvc.decorators import route
from mvc.view import redirect, view, json_view
from ioc.decorators import component


@component(layer="controller")
class StatefulBookController:
    def __init__(self, book_service: BookService):
        self.book_service = book_service

    @route("/cart", method="GET")
    def cart(self, request):
        cart_ids = request.session().get("cart_ids", [])
        books = []
        total = 0.0
        for book_id in cart_ids:
            book = self.book_service.find_by_id(book_id)
            if book:
                books.append(book)
                total += book.price
        return view("cart.html", {
            "books": books,
            "total": total,
            "session_id": request.cookies().get("SESSIONID", "new-session"),
        })

    @route("/cart/items", method="POST")
    def add_to_cart(self, request):
        data = request.form()
        book_id = data.get("book_id")
        if not book_id or not self.book_service.find_by_id(book_id):
            return redirect("/books")
        session = request.session()
        cart_ids = session.setdefault("cart_ids", [])
        cart_ids.append(book_id)
        session.setdefault("checkout_step", "cart")
        return redirect("/cart")

    @route("/checkout", method="GET")
    def checkout(self, request):
        session = request.session()
        step = session.get("checkout_step", "cart")
        cart_ids = session.get("cart_ids", [])
        books = [book for book_id in cart_ids if (book := self.book_service.find_by_id(book_id))]
        return view("checkout.html", {
            "step": step,
            "books": books,
            "session_id": request.cookies().get("SESSIONID", "new-session"),
        })

    @route("/checkout/start", method="POST")
    def start_checkout(self, request):
        session = request.session()
        if session.get("cart_ids"):
            session["checkout_step"] = "address"
        return redirect("/checkout")

    @route("/checkout/next", method="POST")
    def next_checkout_step(self, request):
        session = request.session()
        steps = ["cart", "address", "payment", "review", "done"]
        current = session.get("checkout_step", "cart")
        if current in steps and current != "done":
            session["checkout_step"] = steps[steps.index(current) + 1]
        return redirect("/checkout")

    @route("/api/session", method="GET")
    def session_snapshot(self, request):
        return json_view({
            "session_id": request.cookies().get("SESSIONID", "new-session"),
            "server_state": request.session(),
        })

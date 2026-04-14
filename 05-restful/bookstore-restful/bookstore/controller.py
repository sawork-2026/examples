"""SpaController：提供 SPA 外壳页面。"""
from mvc.decorators import route
from mvc.view import view
from ioc.decorators import component


@component(layer="controller")
class BookController:
    @route("/", method="GET")
    def index(self, request):
        return view("spa.html", {})

    @route("/books", method="GET")
    def books(self, request):
        return view("spa.html", {})

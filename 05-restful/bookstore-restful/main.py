"""书店 MVC 应用入口"""
import os, sys

_dir = os.path.dirname(os.path.abspath(__file__))
os.chdir(_dir)
sys.path.insert(0, _dir)

import aspects.http_log  # noqa: F401  注册切面

from ioc.container import Container
from framework.aop import AopWeaver
from mvc.router import Router
from mvc.view_resolver import ViewResolver
from mvc.handler_adapter import HandlerAdapter
from mvc.static_handler import StaticFileHandler
from mvc.http_server import HttpServer
from bookstore.repository import BookRepository
from bookstore.service import BookService
from bookstore.controller import BookController
from bookstore.order_repository import OrderRepository
from bookstore.order_service import OrderService
from bookstore.restful_controller import RestfulBookstoreController

if os.path.exists("books.db"):
    os.remove("books.db")

container = Container()
for cls in [Router, ViewResolver, HandlerAdapter, StaticFileHandler, HttpServer,
            BookRepository, BookService, BookController,
            OrderRepository, OrderService, RestfulBookstoreController]:
    container.register(cls)

AopWeaver(container).weave()

router = container.get(Router)
for cls in [BookController, RestfulBookstoreController]:
    router.register_controller(container.get(cls))

server = container.get(HttpServer)
port = int(sys.argv[1]) if len(sys.argv) > 1 else 8080
server.start(port=port)

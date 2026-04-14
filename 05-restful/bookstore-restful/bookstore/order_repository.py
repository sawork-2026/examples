"""OrderRepository：内存中的订单资源存储。"""
from ioc.decorators import component
from bookstore.order_models import Order


@component
class OrderRepository:
    def __init__(self):
        self._orders: dict[str, Order] = {}

    def find_all(self) -> list[Order]:
        return list(self._orders.values())

    def find_by_id(self, order_id: str) -> Order | None:
        return self._orders.get(order_id)

    def save(self, order: Order) -> Order:
        self._orders[order.id] = order
        return order

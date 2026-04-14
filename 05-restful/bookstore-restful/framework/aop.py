import functools
"""AOP 切面织入器（对应 Spring AOP）"""
import inspect
from ioc.container import Container

# 全局切面注册表（import 时由 @aspect 写入）
_aspects: list = []
# 已织入的类，防止多次 AopWeaver 重复织入
_woven_classes: set[type] = set()


def aspect(layer: str, point: str = 'around'):
    """注册切面函数。point: before / after / around"""
    def decorator(func):
        func._is_aspect = True
        func._aspect_layer = layer
        func._aspect_point = point
        if func not in _aspects:
            _aspects.append(func)
        return func
    return decorator


class AopWeaver:
    """对容器中指定层的所有组件公开方法织入切面。"""

    def __init__(self, container: Container):
        self._container = container

    def weave(self) -> None:
        if not _aspects:
            return
        for cls in list(self._container._classes):
            if cls in _woven_classes:
                continue
            cls_layer = self._container._layer_map.get(cls)
            if cls_layer is None:
                continue
            relevant = [a for a in _aspects if a._aspect_layer == cls_layer]
            if not relevant:
                continue
            _woven_classes.add(cls)
            for attr_name in list(vars(cls)):
                if attr_name.startswith('_'):
                    continue
                method = vars(cls).get(attr_name)
                if not callable(method) or isinstance(method, (classmethod, staticmethod)):
                    continue
                for asp in relevant:
                    method = _weave(method, asp)
                setattr(cls, attr_name, method)


def _weave(method, asp):
    point = asp._aspect_point
    if point == 'around':
        def wrapped(*args, **kwargs):
            return asp(method, *args, **kwargs)
    elif point == 'before':
        def wrapped(*args, **kwargs):
            asp(method, *args, **kwargs)
            return method(*args, **kwargs)
    elif point == 'after':
        def wrapped(*args, **kwargs):
            result = method(*args, **kwargs)
            asp(method, result, *args, **kwargs)
            return result
    else:
        return method
    functools.wraps(method)(wrapped)  # 复制 __name__/__qualname__/__wrapped__ 等
    # 复制自定义属性（@route 的 _is_route/_route_path/_route_method）
    for k, v in vars(method).items():
        setattr(wrapped, k, v)
    wrapped.__wrapped__ = method  # 让 inspect.signature 能透过 wrapper 看到原始签名
    return wrapped

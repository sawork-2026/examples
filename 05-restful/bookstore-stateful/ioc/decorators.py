"""
装饰器定义：@component, @lazy, @inject, @post_construct, @pre_destroy
"""

# 已注册的构件类列表（decorator 注册方式）
_registry: list[type] = []


def component(_cls: type = None, *, name: str = None, layer: str = None):
    """
    标记一个类为 IoC 容器管理的构件（对应 Spring @Component）

    支持三种用法：
      @component
      class Foo: ...

      @component(name='alipay')
      class AlipayProcessor: ...

      @component(layer='data')
      class ProductRepository: ...

      @component(name='alipay', layer='infrastructure')
      class AlipayProcessor: ...
    """
    def decorator(cls: type) -> type:
        cls._is_component = True
        cls._component_name = name
        cls._component_layer = layer
        if cls not in _registry:
            _registry.append(cls)
        return cls

    if _cls is not None:
        return decorator(_cls)
    return decorator


def lazy(cls: type) -> type:
    """标记构件为懒加载（对应 Spring @Lazy）——首次 get() 时才实例化"""
    cls._is_lazy = True
    return cls


def inject(method):
    """标记 setter 方法为需要自动注入（对应 Spring @Autowired setter 注入）"""
    method._is_inject = True
    return method


def post_construct(method):
    """标记初始化回调（对应 Spring @PostConstruct）"""
    method._is_post_construct = True
    return method


def pre_destroy(method):
    """标记销毁回调（对应 Spring @PreDestroy）"""
    method._is_pre_destroy = True
    return method


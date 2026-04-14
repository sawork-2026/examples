"""
IoC/DI 容器核心实现
支持：
  - decorator 注册 (@component) 和配置文件注册 (YAML)
  - 构造器注入 + setter 注入 (@inject)
  - 生命周期回调 (@post_construct / @pre_destroy)
  - 懒加载 (@lazy)
  - 循环依赖检测
  - 工厂函数注册 (register_factory)
  - 具名 Bean 注册与获取 (name 参数)
"""

import importlib
import inspect
from pathlib import Path
from typing import Any, Callable

from ioc.decorators import _registry


class CircularDependencyError(Exception):
    pass


class LazyProxy:
    """对 @lazy 构件的代理，首次访问属性时才触发真实实例的创建"""

    def __init__(self, factory):
        object.__setattr__(self, "_factory", factory)
        object.__setattr__(self, "_instance", None)

    def _resolve(self):
        if object.__getattribute__(self, "_instance") is None:
            instance = object.__getattribute__(self, "_factory")()
            object.__setattr__(self, "_instance", instance)
        return object.__getattribute__(self, "_instance")

    def __getattr__(self, name):
        return getattr(self._resolve(), name)

    def __setattr__(self, name, value):
        setattr(self._resolve(), name, value)


class Container:
    def __init__(self, config_file: str | None = None):
        # 已注册的类：type -> type
        self._classes: dict[type, type] = {}
        # 已创建的单例实例：type -> instance
        self._instances: dict[type, Any] = {}
        self._layer_map: dict[type, str] = {}
        # 工厂函数注册：type -> Callable
        self._factories: dict[type, Callable] = {}
        # 具名 Bean：name -> type
        self._named_classes: dict[str, type] = {}
        # 配置文件中指定的属性值：type -> {key: value}
        self._properties: dict[type, dict] = {}
        # 循环依赖检测栈
        self._creating: list[type] = []

        if config_file:
            self._load_config(config_file)
        else:
            # decorator 注册模式：从全局 _registry 导入
            for cls in _registry:
                self.register(cls)

    # ------------------------------------------------------------------
    # 注册
    # ------------------------------------------------------------------

    def register(self, cls: type, name: str | None = None) -> None:
        """注册一个构件类。name 不为空时同时注册为具名 Bean。"""
        self._classes[cls] = cls
        if getattr(cls, '_component_layer', None):
            self._layer_map[cls] = cls._component_layer
        effective_name = name or getattr(cls, "_component_name", None)
        if effective_name:
            self._named_classes[effective_name] = cls

    def register_factory(self, cls: type, factory: Callable, name: str | None = None) -> None:
        """
        注册工厂函数。容器需要 cls 实例时调用 factory() 创建，
        而非直接实例化 cls。适用于构造逻辑复杂或依赖外部配置的构件。

        示例：
            container.register_factory(
                HttpServer,
                lambda: HttpServer(port=int(os.environ["PORT"])),
                name="http_server",
            )
        """
        self._classes[cls] = cls
        self._factories[cls] = factory
        if name:
            self._named_classes[name] = cls

    def _load_config(self, config_file: str) -> None:
        """从 YAML 配置文件加载构件定义"""
        import yaml

        config_path = Path(config_file)
        if not config_path.is_absolute():
            # 相对路径相对于调用者的工作目录
            config_path = Path.cwd() / config_path

        with open(config_path, encoding="utf-8") as f:
            config = yaml.safe_load(f)

        for entry in config.get("components", []):
            module_name, class_name = entry["class"].rsplit(".", 1)
            module = importlib.import_module(module_name)
            cls = getattr(module, class_name)
            bean_name = entry.get("name")
            self._classes[cls] = cls
            if bean_name:
                self._named_classes[bean_name] = cls
            if "properties" in entry:
                self._properties[cls] = entry["properties"]

    # ------------------------------------------------------------------
    # 获取
    # ------------------------------------------------------------------

    def get(self, cls: type, name: str | None = None) -> Any:
        """
        获取构件实例。

        - get(PaymentProcessor)          → 按类型匹配（issubclass）
        - get(PaymentProcessor, 'alipay') → 按具名精确匹配
        """
        if name is not None:
            if name not in self._named_classes:
                raise KeyError(f"未找到具名 Bean: '{name}'")
            return self._get_or_create(self._named_classes[name])

        return self._get_or_create(cls)

    def _get_or_create(self, cls: type) -> Any:
        # 已有实例直接返回
        existing = self._find_instance(cls)
        if existing is not None:
            return existing

        # 找到注册的具体类
        concrete = self._resolve_class(cls)
        if concrete is None:
            raise KeyError(f"未注册的构件类型: {cls}")

        return self._create(concrete)

    def _find_instance(self, cls: type) -> Any | None:
        """在已创建的实例中找到 cls 的实例（支持接口/父类匹配）"""
        for klass, inst in self._instances.items():
            if issubclass(klass, cls):
                return inst
        return None

    def _resolve_class(self, cls: type) -> type | None:
        """在注册表中找到匹配 cls（或其子类）的具体类"""
        for klass in self._classes:
            if issubclass(klass, cls):
                return klass
        return None

    def _create(self, cls: type) -> Any:
        """创建构件实例，按顺序：构造 → setter 注入 → @post_construct"""
        # 循环依赖检测
        if cls in self._creating:
            chain = " -> ".join(c.__name__ for c in self._creating) + f" -> {cls.__name__}"
            raise CircularDependencyError(f"CircularDependencyError: {chain}")
        self._creating.append(cls)

        try:
            # 1. 工厂函数优先；否则构造器注入
            if cls in self._factories:
                instance = self._factories[cls]()
            else:
                instance = self._constructor_inject(cls)

            # 2. 应用配置文件 properties（简单属性赋值）
            for key, val in self._properties.get(cls, {}).items():
                setattr(instance, key, val)

            # 3. 注册实例（放在 setter 注入之前，避免被循环引用检测误判）
            self._instances[cls] = instance

            # 4. setter 注入
            self._setter_inject(instance)

            # 5. @post_construct
            self._call_post_construct(instance)

        finally:
            self._creating.remove(cls)

        return instance

    def _constructor_inject(self, cls: type) -> Any:
        """读取构造函数类型标注，递归解析并注入依赖"""
        sig = inspect.signature(cls.__init__)
        kwargs: dict[str, Any] = {}
        for name, param in sig.parameters.items():
            if name == "self":
                continue
            annotation = param.annotation
            if annotation is inspect.Parameter.empty:
                continue
            # 跳过内置类型（str/int/float/bool/bytes）和有默认值的参数
            if annotation in (str, int, float, bool, bytes):
                if param.default is not inspect.Parameter.empty:
                    kwargs[name] = param.default
                continue
            kwargs[name] = self._resolve_dependency(annotation)
        return cls(**kwargs)

    def _setter_inject(self, instance: Any):
        """扫描带 @inject 标记的方法，自动调用并注入依赖"""
        for name, method in inspect.getmembers(instance, predicate=inspect.ismethod):
            if not getattr(method, "_is_inject", False):
                continue
            sig = inspect.signature(method)
            kwargs: dict[str, Any] = {}
            for pname, param in sig.parameters.items():
                if param.annotation is inspect.Parameter.empty:
                    continue
                kwargs[pname] = self._resolve_dependency(param.annotation)
            method(**kwargs)

    def _resolve_dependency(self, cls: type) -> Any:
        """解析依赖：如果目标类是 @lazy，返回 LazyProxy；否则直接创建"""
        concrete = self._resolve_class(cls)
        if concrete is not None and getattr(concrete, "_is_lazy", False):
            return LazyProxy(lambda c=concrete: self._get_or_create(c))
        return self._get_or_create(cls)

    # ------------------------------------------------------------------
    # 生命周期
    # ------------------------------------------------------------------

    @staticmethod
    def _call_post_construct(instance: Any):
        for name, method in inspect.getmembers(instance, predicate=inspect.ismethod):
            if getattr(method, "_is_post_construct", False):
                method()

    def close(self):
        """关闭容器，调用所有构件的 @pre_destroy 方法"""
        for instance in self._instances.values():
            for name, method in inspect.getmembers(instance, predicate=inspect.ismethod):
                if getattr(method, "_is_pre_destroy", False):
                    method()

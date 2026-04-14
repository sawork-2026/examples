"""ViewResolver：Jinja2 模板渲染（对应 Spring InternalResourceViewResolver）"""
from jinja2 import Environment, FileSystemLoader


class ViewResolver:
    def __init__(self, template_dir: str = "templates"):
        self.env = Environment(loader=FileSystemLoader(template_dir))

    def resolve(self, template_name: str, model: dict) -> bytes:
        tmpl = self.env.get_template(template_name)
        return tmpl.render(**model).encode("utf-8")

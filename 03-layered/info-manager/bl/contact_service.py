import re
from data.contact import Contact
from data.contact_repository import ContactRepository


class ContactService:
    def __init__(self, repo: ContactRepository):
        self._repo = repo

    def list_all(self) -> list[Contact]:
        return self._repo.find_all()

    def search(self, keyword: str) -> list[Contact]:
        keyword = keyword.lower()
        return [c for c in self._repo.find_all()
                if keyword in c.name.lower()
                or keyword in c.phone
                or keyword in c.email.lower()]

    def add(self, name: str, phone: str, email: str) -> Contact:
        self._validate(name, phone, email)
        return self._repo.save(Contact(name=name, phone=phone, email=email))

    def update(self, contact_id: str, name: str, phone: str, email: str) -> Contact:
        if not self._repo.find_by_id(contact_id):
            raise ValueError("联系人不存在")
        self._validate(name, phone, email)
        contact = Contact(name=name, phone=phone, email=email, id=contact_id)
        return self._repo.save(contact)

    def delete(self, contact_id: str) -> bool:
        return self._repo.delete(contact_id)

    def _validate(self, name: str, phone: str, email: str):
        if not name.strip():
            raise ValueError("姓名不能为空")
        if phone and not re.match(r'^[\d\-+() ]+$', phone):
            raise ValueError("电话格式不正确")
        if email and not re.match(r'^[^@\s]+@[^@\s]+\.[^@\s]+$', email):
            raise ValueError("邮箱格式不正确")

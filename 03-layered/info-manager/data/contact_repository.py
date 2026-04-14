from typing import Optional
from data.contact import Contact


class ContactRepository:
    def __init__(self):
        self._contacts = {}

    def find_all(self) -> list:
        return list(self._contacts.values())

    def find_by_id(self, contact_id: str) -> Optional[Contact]:
        return self._contacts.get(contact_id)

    def save(self, contact: Contact) -> Contact:
        self._contacts[contact.id] = contact
        return contact

    def delete(self, contact_id: str) -> bool:
        if contact_id in self._contacts:
            del self._contacts[contact_id]
            return True
        return False

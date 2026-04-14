import tkinter as tk
from data.contact_repository import ContactRepository
from bl.contact_service import ContactService
from ui.main_view import MainView


def main():
    # 组装三层：data → bl → ui
    repo = ContactRepository()
    service = ContactService(repo)
    root = tk.Tk()
    MainView(root, service)
    root.mainloop()


if __name__ == "__main__":
    main()

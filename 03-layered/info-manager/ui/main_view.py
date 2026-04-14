import tkinter as tk
from tkinter import ttk, messagebox
from bl.contact_service import ContactService


class MainView:
    def __init__(self, root: tk.Tk, service: ContactService):
        self._root = root
        self._service = service
        self._selected_id = None
        self._root.title("联系人管理系统")
        self._root.geometry("700x480")
        self._build_search_bar()
        self._build_table()
        self._build_form()
        self._build_buttons()
        self._refresh_table()

    # --- 搜索栏 ---
    def _build_search_bar(self):
        bar = ttk.Frame(self._root)
        bar.pack(fill=tk.X, padx=10, pady=(10, 0))
        ttk.Label(bar, text="搜索:").pack(side=tk.LEFT)
        self._search_var = tk.StringVar()
        entry = ttk.Entry(bar, textvariable=self._search_var)
        entry.pack(side=tk.LEFT, fill=tk.X, expand=True, padx=5)
        ttk.Button(bar, text="搜索", command=self._on_search).pack(side=tk.LEFT)
        ttk.Button(bar, text="重置", command=self._on_reset).pack(side=tk.LEFT, padx=(5, 0))

    # --- 表格 ---
    def _build_table(self):
        cols = ("id", "name", "phone", "email")
        self._tree = ttk.Treeview(self._root, columns=cols, show="headings", height=10)
        self._tree.heading("id", text="ID")
        self._tree.heading("name", text="姓名")
        self._tree.heading("phone", text="电话")
        self._tree.heading("email", text="邮箱")
        self._tree.column("id", width=80)
        self._tree.column("name", width=120)
        self._tree.column("phone", width=150)
        self._tree.column("email", width=200)
        self._tree.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        self._tree.bind("<<TreeviewSelect>>", self._on_select)

    # --- 表单 ---
    def _build_form(self):
        form = ttk.LabelFrame(self._root, text="联系人信息")
        form.pack(fill=tk.X, padx=10)
        self._name_var = tk.StringVar()
        self._phone_var = tk.StringVar()
        self._email_var = tk.StringVar()
        for i, (label, var) in enumerate([
            ("姓名:", self._name_var),
            ("电话:", self._phone_var),
            ("邮箱:", self._email_var),
        ]):
            ttk.Label(form, text=label).grid(row=0, column=i * 2, padx=5, pady=5)
            ttk.Entry(form, textvariable=var).grid(row=0, column=i * 2 + 1, padx=5, pady=5)

    # --- 按钮 ---
    def _build_buttons(self):
        bar = ttk.Frame(self._root)
        bar.pack(fill=tk.X, padx=10, pady=10)
        ttk.Button(bar, text="添加", command=self._on_add).pack(side=tk.LEFT, padx=5)
        ttk.Button(bar, text="修改", command=self._on_update).pack(side=tk.LEFT, padx=5)
        ttk.Button(bar, text="删除", command=self._on_delete).pack(side=tk.LEFT, padx=5)
        ttk.Button(bar, text="清空表单", command=self._clear_form).pack(side=tk.LEFT, padx=5)

    # --- 事件处理（全部委托给 bl 层）---
    def _refresh_table(self, contacts=None):
        for row in self._tree.get_children():
            self._tree.delete(row)
        for c in (contacts or self._service.list_all()):
            self._tree.insert("", tk.END, values=(c.id, c.name, c.phone, c.email))

    def _on_select(self, _event):
        sel = self._tree.selection()
        if not sel:
            return
        vals = self._tree.item(sel[0], "values")
        self._selected_id = vals[0]
        self._name_var.set(vals[1])
        self._phone_var.set(vals[2])
        self._email_var.set(vals[3])

    def _on_add(self):
        try:
            self._service.add(self._name_var.get(), self._phone_var.get(), self._email_var.get())
            self._refresh_table()
            self._clear_form()
        except ValueError as e:
            messagebox.showwarning("校验失败", str(e))

    def _on_update(self):
        if not self._selected_id:
            messagebox.showinfo("提示", "请先选择一条记录")
            return
        try:
            self._service.update(self._selected_id, self._name_var.get(), self._phone_var.get(), self._email_var.get())
            self._refresh_table()
            self._clear_form()
        except ValueError as e:
            messagebox.showwarning("校验失败", str(e))

    def _on_delete(self):
        if not self._selected_id:
            messagebox.showinfo("提示", "请先选择一条记录")
            return
        self._service.delete(self._selected_id)
        self._refresh_table()
        self._clear_form()

    def _on_search(self):
        keyword = self._search_var.get().strip()
        self._refresh_table(self._service.search(keyword) if keyword else None)

    def _on_reset(self):
        self._search_var.set("")
        self._refresh_table()

    def _clear_form(self):
        self._selected_id = None
        self._name_var.set("")
        self._phone_var.set("")
        self._email_var.set("")

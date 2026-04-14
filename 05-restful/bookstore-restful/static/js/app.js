const booksList = document.getElementById("books-list");
const ordersList = document.getElementById("orders-list");
const cartList = document.getElementById("cart-list");
const booksResponse = document.getElementById("books-response");
const ordersResponse = document.getElementById("orders-response");
const cartState = document.getElementById("cart-state");
const requestLog = document.getElementById("request-log");
const CART_KEY = "bookstore-restful-cart";
let booksCache = [];
let cartIds = loadCart();

function loadCart() {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY) || "[]");
  } catch {
    return [];
  }
}

function persistCart() {
  localStorage.setItem(CART_KEY, JSON.stringify(cartIds));
  cartState.textContent = JSON.stringify({ cartIds }, null, 2);
}

function log(message, payload) {
  const lines = [requestLog.textContent.trim()].filter(Boolean);
  lines.unshift(
    `${new Date().toLocaleTimeString()} ${message}${payload ? `\n${JSON.stringify(payload, null, 2)}` : ""}`
  );
  requestLog.textContent = lines.slice(0, 12).join("\n\n");
}

async function apiFetch(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
    ...options,
  });
  const data = await response.json();
  log(`${options.method || "GET"} ${url} -> ${response.status}`, data);
  return { response, data };
}

function renderBooks(books) {
  booksCache = books;
  booksList.innerHTML = "";
  books.forEach((book) => {
    const item = document.createElement("div");
    item.className = "card";
    const inCart = cartIds.includes(book.id);
    const stock = book.stock ?? 0;
    item.innerHTML = `
      <span>
        <strong>${book.title}</strong><br>
        <small>${book.author} · ¥${book.price}</small><br>
        <small>库存：${stock}</small><br>
        <code>${book._links.self.href}</code>
      </span>
      <div class="actions">
        <button type="button" data-book-id="${book.id}" ${stock <= 0 ? "disabled" : ""}>
          ${inCart ? "已在购物车" : "加入购物车"}
        </button>
      </div>
    `;
    booksList.appendChild(item);
  });
}

function renderCart() {
  cartList.innerHTML = "";
  const items = cartIds
    .map((id) => booksCache.find((book) => book.id === id))
    .filter(Boolean);
  if (!items.length) {
    cartList.innerHTML = `<div class="card"><span>购物车为空。先把几本书加入客户端购物车，再点击“下单”。</span></div>`;
  } else {
    items.forEach((book) => {
      const item = document.createElement("div");
      item.className = "card";
      item.innerHTML = `
        <div>
          <strong>${book.title}</strong><br>
          <small>${book.author} · ¥${book.price}</small>
        </div>
        <div class="actions">
          <button type="button" data-remove-book-id="${book.id}">移除</button>
        </div>
      `;
      cartList.appendChild(item);
    });
  }
  persistCart();
}

function renderOrders(orders) {
  ordersList.innerHTML = "";
  orders.forEach((order) => {
    const item = document.createElement("div");
    item.className = "card";
    const buttons = [];
    if (order._links.payment) {
      buttons.push(`<button type="button" data-action="payment" data-href="${order._links.payment.href}">支付</button>`);
    }
    if (order._links.cancel) {
      buttons.push(`<button type="button" data-action="cancel" data-href="${order._links.cancel.href}">取消</button>`);
    }
    item.innerHTML = `
      <div>
        <strong>订单 ${order.id}</strong><br>
        <small>状态：${order.status}</small><br>
        <small>书籍：${order.bookIds.join(", ") || "(空)"}</small><br>
        <code>${order._links.self.href}</code>
      </div>
      <div class="actions">${buttons.join(" ")}</div>
    `;
    ordersList.appendChild(item);
  });
}

async function loadBooks() {
  const { data } = await apiFetch("/api/books");
  booksResponse.textContent = JSON.stringify(data, null, 2);
  renderBooks(data._embedded.books);
  renderCart();
}

async function loadOrders() {
  const { data } = await apiFetch("/api/orders");
  ordersResponse.textContent = JSON.stringify(data, null, 2);
  renderOrders(data._embedded.orders);
}

async function createOrder() {
  if (!cartIds.length) {
    log("POST /api/orders skipped", { reason: "no books selected" });
    return;
  }
  const { response } = await apiFetch("/api/orders", {
    method: "POST",
    body: JSON.stringify({ bookIds: cartIds }),
  });
  if (response.ok) {
    cartIds = [];
    renderCart();
    await loadBooks();
  }
  await loadOrders();
}

booksList.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-book-id]");
  if (!button) return;
  const { bookId } = button.dataset;
  if (!cartIds.includes(bookId)) {
    cartIds.push(bookId);
    renderBooks(booksCache);
    renderCart();
    log("client cart updated", { cartIds });
  }
});

cartList.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-remove-book-id]");
  if (!button) return;
  cartIds = cartIds.filter((id) => id !== button.dataset.removeBookId);
  renderBooks(booksCache);
  renderCart();
  log("client cart updated", { cartIds });
});

ordersList.addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-href]");
  if (!button) return;
  await apiFetch(button.dataset.href, { method: "POST" });
  await loadBooks();
  await loadOrders();
});

document.getElementById("refresh-books").addEventListener("click", loadBooks);
document.getElementById("refresh-orders").addEventListener("click", loadOrders);
document.getElementById("create-order").addEventListener("click", createOrder);
document.getElementById("clear-cart").addEventListener("click", () => {
  cartIds = [];
  renderBooks(booksCache);
  renderCart();
  log("client cart cleared", { cartIds });
});
document.getElementById("clear-log").addEventListener("click", () => {
  requestLog.textContent = "";
});

Promise.all([loadBooks(), loadOrders()]);

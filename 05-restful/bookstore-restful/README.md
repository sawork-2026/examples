# bookstore-restful

这个示例展示一个更接近 REST 风格的版本：

- 不再使用 Session 保存购物车或结账步骤
- 前端改成一个 SPA，通过浏览器直接调用 JSON 资源接口
- 购物车保存在客户端 `localStorage` 中，用来演示**应用状态由客户端维护**
- 服务端保存的是**资源状态**，例如订单的 `CREATED` / `PAID` / `CANCELLED`，以及书籍库存 `stock`
- 响应中返回 `_links`，让客户端发现下一步可执行操作

快速体验：

1. 进入这个示例目录
2. 安装依赖：`pip install -r requirements.txt`
3. 启动服务：`python3 main.py 8081`
4. 先访问 API 入口，看看系统暴露了哪些资源
5. 再查看书籍资源列表
6. 先在 SPA 中把书加入客户端购物车
7. 点击“下单”，创建订单资源
8. 观察书籍资源里的 `stock` 变化
9. 按返回里的支付链接或取消链接继续交互

示例请求：

```http
POST <订单集合资源>
Content-Type: application/json

{
  "bookIds": ["<book-id-1>", "<book-id-2>"]
}
```

这套示例的重点，是对比 `bookstore-stateful`：

- `bookstore-stateful`：服务器保存应用状态（购物车、结账流程）
- `bookstore-restful`：客户端保存购物车这个应用状态，服务器保存订单和库存这些资源状态

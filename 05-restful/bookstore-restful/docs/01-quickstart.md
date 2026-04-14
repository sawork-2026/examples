# 快速上手

## 安装依赖

```bash
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

## 启动服务器

```bash
.venv/bin/python main.py        # 默认 8080 端口
.venv/bin/python main.py 9000   # 指定端口
```

## 访问地址

| URL | 说明 |
|-----|------|
| http://localhost:8080/ | 重定向到书籍列表 |
| http://localhost:8080/books | 书籍列表（HTML）|
| http://localhost:8080/books/new | 新增书籍表单 |
| http://localhost:8080/books/<id> | 书籍详情 |
| http://localhost:8080/books/<id>/edit | 编辑书籍 |
| http://localhost:8080/api/books | 书籍列表（JSON）|
| http://localhost:8080/api/books/<id> | 单本书籍（JSON）|
| http://localhost:8080/static/css/style.css | 静态文件 |

## curl 示例

```bash
# 获取书籍列表
curl http://localhost:8080/api/books

# 新增书籍
curl -X POST http://localhost:8080/books \
  -d 'title=深入理解JVM&author=周志明&price=109'

# 删除书籍（HTML 表单用 POST + _method=DELETE）
curl -X POST http://localhost:8080/books/<id> \
  -d '_method=DELETE'
```

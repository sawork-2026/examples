# 联系人管理系统 — 分层架构示例

一个使用 Python + Tkinter 实现的跨平台桌面应用，演示经典三层架构（UI / BL / Data）。

## 项目结构

```
info-manager/
├── main.py              # 入口，组装三层
├── ui/                  # Presentation Layer — 界面展示与用户交互
│   └── main_view.py
├── bl/                  # Business Logic Layer — 业务校验与搜索过滤
│   └── contact_service.py
└── data/                # Data Access Layer — 数据模型与内存存储
    ├── contact.py
    └── contact_repository.py
```

## 层间依赖

```
ui → bl → data（严格单向，ui 不直接访问 data）
```

## 运行

```bash
python3 main.py
```

无需安装任何第三方依赖，Python 3.10+ 即可运行。

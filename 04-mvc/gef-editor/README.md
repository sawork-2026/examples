# GEF 简单图形编辑器示例

这是一个使用 Eclipse GEF (Graphical Editing Framework) 实现的简单图形编辑器，展示了 MVC 模式在桌面图形应用中的应用。

## 功能

- 创建矩形节点
- 拖动节点
- 删除节点

## MVC 架构

- **Model**：`Node` 类，表示图形节点的数据（位置、大小）
- **View**：`NodeFigure` 类，Draw2D 绘制的矩形
- **Controller**：`NodeEditPart` 类，连接 Model 和 View，处理用户交互

## 技术栈

- Eclipse GEF 3.x
- Draw2D
- SWT (Standard Widget Toolkit)

## 运行

需要 Eclipse IDE 和 GEF 插件：

```bash
mvn clean package
# 在 Eclipse 中作为 Eclipse Application 运行
```

## 项目结构

```
gef-editor/
├── src/main/java/com/example/gef/
│   ├── model/
│   │   └── Node.java           # Model
│   ├── view/
│   │   └── NodeFigure.java     # View
│   └── controller/
│       └── NodeEditPart.java   # Controller
└── pom.xml
```

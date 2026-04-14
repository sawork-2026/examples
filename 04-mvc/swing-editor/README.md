# 简单图形编辑器示例（MVC 模式）

这是一个使用 Java Swing 实现的简单图形编辑器，展示了 MVC 模式在桌面图形应用中的应用。

## 功能

- 点击画布创建矩形节点
- 拖动节点移动位置
- 右键删除节点

## MVC 架构

- **Model**：`Node` 类，表示图形节点的数据（位置、大小）
- **View**：`NodeView` 类，绘制矩形
- **Controller**：`EditorController` 类，处理鼠标事件，更新 Model

## 运行

```bash
cd examples/04-mvc/swing-editor
javac -d bin src/main/java/com/example/editor/*.java
java -cp bin com.example.editor.EditorApp
```

或使用 Maven：

```bash
mvn clean compile exec:java
```

## 项目结构

```
swing-editor/
├── src/main/java/com/example/editor/
│   ├── Node.java              # Model
│   ├── NodeView.java          # View
│   ├── EditorController.java  # Controller
│   └── EditorApp.java         # 主程序
└── pom.xml
```

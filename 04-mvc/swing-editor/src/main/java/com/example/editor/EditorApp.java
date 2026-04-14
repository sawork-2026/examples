package com.example.editor;

import javax.swing.*;

public class EditorApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("简单图形编辑器 - MVC 示例");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            NodeView view = new NodeView();
            EditorController controller = new EditorController(view);

            view.addMouseListener(controller);
            view.addMouseMotionListener(controller);

            frame.add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            System.out.println("左键点击：创建节点");
            System.out.println("拖动：移动节点");
            System.out.println("Shift+点击两个节点：创建连线");
            System.out.println("右键点击：删除节点");
        });
    }
}

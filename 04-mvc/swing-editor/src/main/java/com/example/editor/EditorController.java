package com.example.editor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EditorController extends MouseAdapter {
    private NodeView view;
    private Node draggedNode = null;
    private Node connectionSource = null;
    private int dragOffsetX, dragOffsetY;

    public EditorController(NodeView view) {
        this.view = view;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Node clickedNode = view.findNodeAt(e.getX(), e.getY());

        if (clickedNode != null) {
            if (e.isShiftDown()) {
                // Shift+点击：创建连线
                if (connectionSource == null) {
                    connectionSource = clickedNode;
                    System.out.println("选中源节点，再 Shift+点击目标节点");
                } else {
                    if (clickedNode != connectionSource) {
                        Connection conn = new Connection(connectionSource, clickedNode);
                        view.addConnection(conn);
                        System.out.println("连线已创建");
                    }
                    connectionSource = null;
                }
            } else {
                draggedNode = clickedNode;
                dragOffsetX = e.getX() - draggedNode.getX();
                dragOffsetY = e.getY() - draggedNode.getY();
            }
        } else if (e.getButton() == MouseEvent.BUTTON1 && !e.isShiftDown()) {
            Node newNode = new Node(e.getX() - 40, e.getY() - 30);
            view.addNode(newNode);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedNode != null) {
            draggedNode.setLocation(e.getX() - dragOffsetX, e.getY() - dragOffsetY);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && draggedNode != null) {
            view.removeNode(draggedNode);
        }
        draggedNode = null;
    }
}

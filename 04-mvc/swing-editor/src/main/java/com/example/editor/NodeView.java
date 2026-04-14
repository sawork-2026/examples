package com.example.editor;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class NodeView extends JPanel implements PropertyChangeListener {
    private List<Node> nodes = new ArrayList<>();
    private List<Connection> connections = new ArrayList<>();

    public NodeView() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);
    }

    public void addNode(Node node) {
        nodes.add(node);
        node.addPropertyChangeListener(this);
        repaint();
    }

    public void removeNode(Node node) {
        nodes.remove(node);
        node.removePropertyChangeListener(this);
        repaint();
    }

    public void addConnection(Connection conn) {
        connections.add(conn);
        conn.addPropertyChangeListener(this);
        repaint();
    }

    public Node findNodeAt(int x, int y) {
        for (int i = nodes.size() - 1; i >= 0; i--) {
            if (nodes.get(i).contains(x, y)) {
                return nodes.get(i);
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 先绘制连线
        g.setColor(Color.BLACK);
        for (Connection conn : connections) {
            Node src = conn.getSource();
            Node tgt = conn.getTarget();
            int x1 = src.getX() + src.getWidth() / 2;
            int y1 = src.getY() + src.getHeight() / 2;
            int x2 = tgt.getX() + tgt.getWidth() / 2;
            int y2 = tgt.getY() + tgt.getHeight() / 2;
            g.drawLine(x1, y1, x2, y2);
        }

        // 再绘制节点
        for (Node node : nodes) {
            g.setColor(Color.BLUE);
            g.fillRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
            g.setColor(Color.BLACK);
            g.drawRect(node.getX(), node.getY(), node.getWidth(), node.getHeight());
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        repaint();
    }
}

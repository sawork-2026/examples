package com.example.gef.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Node {
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_SIZE = "size";

    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private int x, y;
    private int width = 100, height = 50;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(int x, int y) {
        int oldX = this.x, oldY = this.y;
        this.x = x;
        this.y = y;
        listeners.firePropertyChange(PROPERTY_LOCATION,
            new int[]{oldX, oldY}, new int[]{x, y});
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }
}

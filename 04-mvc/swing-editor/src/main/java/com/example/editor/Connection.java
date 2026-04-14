package com.example.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Connection {
    public static final String PROPERTY_ENDPOINTS = "endpoints";

    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private Node source;
    private Node target;

    public Connection(Node source, Node target) {
        this.source = source;
        this.target = target;
    }

    public Node getSource() { return source; }
    public Node getTarget() { return target; }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }
}

package com.example.gef.controller;

import com.example.gef.model.Node;
import com.example.gef.view.NodeFigure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NodeEditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

    @Override
    protected IFigure createFigure() {
        return new NodeFigure();
    }

    @Override
    protected void createEditPolicies() {
        // 可以添加拖动、删除等策略
    }

    @Override
    public void activate() {
        super.activate();
        ((Node) getModel()).addPropertyChangeListener(this);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ((Node) getModel()).removePropertyChangeListener(this);
    }

    @Override
    protected void refreshVisuals() {
        Node node = (Node) getModel();
        NodeFigure figure = (NodeFigure) getFigure();
        Rectangle bounds = new Rectangle(node.getX(), node.getY(),
                                         node.getWidth(), node.getHeight());
        figure.setBounds(bounds);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Node.PROPERTY_LOCATION.equals(evt.getPropertyName())) {
            refreshVisuals();
        }
    }
}

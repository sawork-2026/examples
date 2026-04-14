package com.example.gef.view;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

public class NodeFigure extends Figure {

    @Override
    protected void paintFigure(Graphics graphics) {
        Rectangle bounds = getBounds();
        graphics.setBackgroundColor(org.eclipse.swt.graphics.Color.BLUE);
        graphics.fillRectangle(bounds);
        graphics.setForegroundColor(org.eclipse.swt.graphics.Color.BLACK);
        graphics.drawRectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
    }
}

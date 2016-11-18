package gov.nist.csd.pm.application.workflow.creator;

import java.awt.*;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 8/9/11
* Time: 4:04 PM
* To change this template use File | Settings | File Templates.
*/
class RectangleAnnotation extends ShapeAnnotation {

    public RectangleAnnotation(Shape shape) {
        super(shape);
    }

    @Override
    public void paintAnnotation(Graphics2D g) {
        if (shape != null) {
            Rectangle bounds = shape.getBounds();
            Composite originalComp = g.getComposite();
            Color color = g.getColor();
            Color bg = g.getBackground();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
            g.setColor(Color.BLUE.brighter());
            g.fillRect(
                    (int) bounds.getX(), (int) bounds.getY(),
                    (int) bounds.getWidth(), (int) bounds.getHeight());
            g.setColor(Color.BLUE.darker());
            g.fillRect(
                    (int) bounds.getX(), (int) bounds.getY(),
                    (int) bounds.getWidth(), (int) bounds.getHeight());
            g.setColor(color);
            g.setBackground(bg);
            g.setComposite(originalComp);
        }

    }

}

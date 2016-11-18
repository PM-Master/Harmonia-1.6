package gov.nist.csd.pm.application.workflow.creator;

import java.awt.*;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 8/9/11
* Time: 4:03 PM
* To change this template use File | Settings | File Templates.
*/
public class ShapeAnnotation implements Annotation {
    /**
	 * @uml.property  name="shape"
	 */
    protected final Shape shape;

    public ShapeAnnotation(Shape shape) {
        this.shape = shape;
    }


    @Override
    public void paintAnnotation(Graphics2D g) {
        if (shape != null) {
            Composite originalComp = g.getComposite();
            Color color = g.getColor();
            Color bg = g.getBackground();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, .3f));
            g.setColor(Color.BLUE.brighter());
            g.fill(shape);
            g.setColor(Color.BLUE.darker());
            g.draw(shape);
            g.setColor(color);
            g.setBackground(bg);
            g.setComposite(originalComp);
        }
    }


}

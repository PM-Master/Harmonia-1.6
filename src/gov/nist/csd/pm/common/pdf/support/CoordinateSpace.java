package gov.nist.csd.pm.common.pdf.support;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/26/11 Time: 9:56 AM To change this template use File | Settings | File Templates.
 */
public enum CoordinateSpace {
    /**
	 * @uml.property  name="nORMAL"
	 * @uml.associationEnd  
	 */
    NORMAL, /**
	 * @uml.property  name="sWING"
	 * @uml.associationEnd  
	 */
    SWING, /**
	 * @uml.property  name="pDF"
	 * @uml.associationEnd  
	 */
    PDF;

    public Point2D convertPointTo(Point2D sourcePoint, Rectangle2D coordinateDomain, CoordinateSpace targetSpace){
        if(targetSpace == null || targetSpace.equals(this)){
            return sourcePoint;
        }
        Point2D normalized = NORMAL.convertPointTo(sourcePoint, coordinateDomain, this);
        return convertToTargetSpace(normalized, coordinateDomain, targetSpace);
    };

    private Point2D convertToTargetSpace(Point2D point, Rectangle2D coordinateDomain, CoordinateSpace targetSpace){
        Point2D clone = (Point2D)point.clone();
        switch(targetSpace){
            case NORMAL:
            case SWING:
                break;
            case PDF:
                clone.setLocation(point.getX(), coordinateDomain.getHeight() - point.getY());
            break;
        }
        return clone;
    }

}

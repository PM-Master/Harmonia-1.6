package gov.nist.csd.pm.common.util.swing;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/8/11
 * Time: 5:18 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Points {
    private Points(){}
    public static Point difference(Point first, Point second){
        return new Point(first.x - second.x, first.y - second.y);
    }

    public static Point sum(Point first, Point second){
        return new Point(first.x + second.x, first.y + second.y);
    }
}

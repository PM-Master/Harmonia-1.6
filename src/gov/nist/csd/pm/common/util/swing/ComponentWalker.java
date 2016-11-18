package gov.nist.csd.pm.common.util.swing;

import gov.nist.csd.pm.common.pattern.AbstractWalkable;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/8/11
 * Time: 4:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class ComponentWalker extends AbstractWalkable<Component> {
    public ComponentWalker(Component node) {
        super(node);
    }

    @Override
    public Collection<? extends Component> neighborNodes(Component parentNode) {
        Container container = null;
        if(parentNode instanceof Container) {
            container = (Container)parentNode ;
        }
        return container == null ? Arrays.asList(new Component[0]) : Arrays.asList(container.getComponents());
    }


}

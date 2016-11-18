/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.util;

import java.awt.*;

/**
 *
 * @author Rob
 */
public class DimensionUtils {
    public static Dimension resolveSizeForComponent(Component comp, Dimension maxDimension){
        //So here we have to reslove the appropriate size for a component which may or
        //may not have their size parameter set.
        Dimension resolvedSize;
        //Get size
        //If size is zero, then use preferredSize
        resolvedSize = hasZeroArea(comp.getSize()) ? comp.getPreferredSize() : maximumDimension(comp.getSize(), comp.getPreferredSize());
        //compare what was chosen above against the maxDimension
        //Use the minimum of the two

        return minimumDimension(resolvedSize, maxDimension);
    }

    public static Double getArea(Dimension dim){
        return dim.getHeight() * dim.getWidth();
    }

    public static Boolean hasZeroArea(Dimension dim){
        return (getArea(dim) == 0);
    }

    public static Dimension minimumDimension(Dimension one, Dimension two){
        return new Dimension((int)Math.min(one.getWidth(), two.getWidth()), (int)Math.min(one.getHeight(), two.getHeight()));
    }

    public static Dimension maximumDimension(Dimension one, Dimension two){
        return new Dimension((int)Math.max(one.getWidth(), two.getWidth()), (int)Math.max(one.getHeight(), two.getHeight()));
    }
}

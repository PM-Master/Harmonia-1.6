package gov.nist.csd.pm.common.model;

import java.beans.PropertyChangeListener;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/7/11
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PropertyChangeObserver {
    Set<String> getObservedProperties();
    PropertyChangeListener listenerForProperty(String property);
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.model;

import java.beans.PropertyChangeListener;

/**
 *
 * @author Administrator
 */
public interface PropertyChangeObservable {

    /**
     * Adds a PropertyChangeListener that subscribes to all properties
     * @param listener  the listener for all properties.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);
    /**
     * Adds a PropertyChangeListener that subscribes to the named properties.
     * @param property  the property subscribing to
     * @param listener  the listener for the property.
     */
    public void addPropertyChangeListener(String property, PropertyChangeListener listener);
    /**
     * Removes a PropertyChangeListener from this broadcaster
     * @param listener  the listener for all properties.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
    /**
     * Removes a PropertyChangeListener
     * @param property  the property for the listener
     * @param listener  the listener to be removed.
     */
    public void removePropertyChangeListener(String property, PropertyChangeListener listener);

    /**
     * Registers an observer for all applicable properties of that observer
     * @param observer
     */
    public void addObserver(PropertyChangeObserver observer);

    public void removeObserver(PropertyChangeObserver observer);
}

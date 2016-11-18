package gov.nist.csd.pm.common.util;

import gov.nist.csd.pm.common.model.PropertyChangeObservableSupport;
import gov.nist.csd.pm.common.model.PropertyChangeObserver;

import java.beans.PropertyChangeListener;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/11/11
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */

public abstract class AbstractRef<T> implements Ref<T>{
    /**
	 * @uml.property  name="pcs"
	 * @uml.associationEnd  
	 */
    PropertyChangeObservableSupport pcs = new PropertyChangeObservableSupport(this);
    /**
	 * @uml.property  name="_ref"
	 */
    List<T> _ref = newArrayList();

    public AbstractRef(T ref){
        _ref.add(ref);
    }

    @Override
    public void push(T obj) {
        T old = get();
        _ref.add(obj);
        pcs.firePropertyChange(this.toString(), old, obj);
    }

    @Override
    public T pop() {
        return _ref.isEmpty() ? null : _ref.remove(_ref.size() - 1);
    }

    @Override
    public void set(T obj) {
        T old = pop();
        push(obj);
        pcs.firePropertyChange(this.toString(), old, obj);
    }

    @Override
    public T get() {
        return _ref.isEmpty() ? null : _ref.get(_ref.size() - 1);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRef)) return false;

        AbstractRef that = (AbstractRef) o;

        if (_ref != null ? !_ref.equals(that._ref) : that._ref != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return _ref != null ? _ref.hashCode() : 0;
    }

    /**
     * Adds a PropertyChangeListener that subscribes to all properties
     *
     * @param listener the listener for all properties.
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Adds a PropertyChangeListener that subscribes to the named properties.
     *
     * @param property the property subscribing to
     * @param listener the listener for the property.
     */
    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(property, listener);
    }

    /**
     * Removes a PropertyChangeListener from this broadcaster
     *
     * @param listener the listener for all properties.
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener
     *
     * @param property the property for the listener
     * @param listener the listener to be removed.
     */
    @Override
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(property, listener);
    }

    @Override
    public void addObserver(PropertyChangeObserver observer) {
        pcs.addObserver(observer);
    }

    @Override
    public void removeObserver(PropertyChangeObserver observer) {
        pcs.removeObserver(observer);
    }
}



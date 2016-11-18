/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

/**
 *
 * @author Administrator
 */
public abstract class AbstractModel implements PropertyChangeObservable, VetoableChangeObservable {
    /**
	 * @uml.property  name="propertyChangeSupport"
	 */
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    /**
	 * @uml.property  name="vetoableChangeSupport"
	 */
    private VetoableChangeSupport vetoableChangeSupport = new VetoableChangeSupport(this);


    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener){
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener){
        propertyChangeSupport.addPropertyChangeListener(property, listener);
    }
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener){
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    @Override
    public void removePropertyChangeListener(String property, PropertyChangeListener listener){
        propertyChangeSupport.removePropertyChangeListener(property, listener);
    }

    /**
	 * @return
	 * @uml.property  name="propertyChangeSupport"
	 */
    protected PropertyChangeSupport getPropertyChangeSupport(){
        return propertyChangeSupport;
    }

    /**
	 * @return
	 * @uml.property  name="vetoableChangeSupport"
	 */
    protected VetoableChangeSupport getVetoableChangeSupport(){
        return vetoableChangeSupport;
    }


    /*
     * VetoableChangeBroadcaster implementation
     */
    @Override
    public void addVetoableChangeListener(VetoableChangeListener listener){
        vetoableChangeSupport.addVetoableChangeListener(listener);
    }
    @Override
    public void addVetoableChangeListener(String property, VetoableChangeListener listener){
        vetoableChangeSupport.addVetoableChangeListener(property, listener);
    }
    @Override
    public void removeVetoableChangeListener(VetoableChangeListener listener){
        vetoableChangeSupport.removeVetoableChangeListener(listener);
    }
    @Override
    public void removeVetoableChangeListener(String property, VetoableChangeListener listener){
        vetoableChangeSupport.removeVetoableChangeListener(property, listener);

    }

}

package gov.nist.csd.pm.common.model;

import java.beans.PropertyChangeSupport;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/7/11
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyChangeObservableSupport extends PropertyChangeSupport implements PropertyChangeObservable {


    public PropertyChangeObservableSupport(Object delegator){
        super(delegator);
    }



    @Override
    public void addObserver(PropertyChangeObserver observer) {
        for(String property : observer.getObservedProperties()){
            this.addPropertyChangeListener(property, observer.listenerForProperty(property));
        }
    }

    public void removeObserver(PropertyChangeObserver observer){
        for(String property : observer.getObservedProperties()){
            this.removePropertyChangeListener(observer.listenerForProperty(property));
        }
    }


}

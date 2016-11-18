package gov.nist.csd.pm.common.action;

import javax.swing.*;
import java.beans.PropertyChangeListener;

import static com.google.common.base.Preconditions.checkNotNull;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 6/22/11
* Time: 11:58 AM
 *  This is an abstract implementation of an ActionRef
 */
abstract class AbstractActionWrapper implements ActionRef {
    /**
	 * @uml.property  name="_action"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Action _action;
    /**
	 * @uml.property  name="_target"
	 */
    private Object _target;
    public AbstractActionWrapper(Action action){
        _action = checkNotNull(action);

    }
    @Override
    public Object getTarget(){
    	return _target;
    }
    @Override
    public void setTarget(Object target){
    	_target = target;
    }
    
    @Override
    public <S> S getTarget(Class<S> type){
    	if(_target == null){
    		return null;
    	}
    	if(type.isAssignableFrom(_target.getClass())){
    		return type.cast(getTarget());
    	}
    	else{
    		return null;
    	}
	}
    
    
    
    @Override
    public Action getAction(){
        return _action;
    }
    @Override
    public void setAction(Action action){
        _action = action;
    }

    @Override
    public Object getValue(String s) {
    	
        Object response = _action.getValue(s);
        //Fallback to NAME if ID is unset.
        if(response == null && ID.equals(s)){
        	response = _action.getValue(NAME);
        }
        return response;
    }

    @Override
    public void putValue(String s, Object o) {
        _action.putValue(s, o);
    }

    @Override
    public void setEnabled(boolean b) {
        _action.setEnabled(b);
    }

    @Override
    public boolean isEnabled() {
        return _action.isEnabled();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        _action.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        _action.removePropertyChangeListener(propertyChangeListener);
    }


}

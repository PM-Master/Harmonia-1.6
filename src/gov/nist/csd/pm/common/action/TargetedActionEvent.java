package gov.nist.csd.pm.common.action;

import java.awt.event.ActionEvent;

/**
 * TargetedActionEvent
 *
 * Adds a parameter representing a "target" of an Action.
 */
public class TargetedActionEvent extends ActionEvent {

	public TargetedActionEvent(Object source, int id, String command,
			long when, int modifiers, Object target) {
		super(source, id, command, when, modifiers);
		_target = target;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2734179904100727840L;
	/**
	 * @uml.property  name="_target"
	 */
	private Object _target;
	
	
	public Object getTarget(){
		return _target;
	}
	
	public <T> T getTarget(Class<T> type){
		return type == null ? null : type.cast(_target);
	}

}

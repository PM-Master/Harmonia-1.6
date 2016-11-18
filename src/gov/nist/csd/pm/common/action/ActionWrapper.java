package gov.nist.csd.pm.common.action;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 6/22/11
* Time: 11:59 AM
 * Abstraction for creating an wrapper around an Action.  This behavior is dictated by the ActionRef interface
*/
public class ActionWrapper extends AbstractActionWrapper {

    /**
     * Creates and returns a simple wrapped ActionRef
     * @param action
     * @return
     */
    public static ActionRef createReference(Action action){
        return createReference(action, null);
    }

    /**
     * Creates and returns a simple wrapped ActionRef with a specified target object.
     * Actions generally act on something specific.  This just formalizes that relationship.
     * @param action
     * @param target
     * @return
     */
    public static  ActionRef createReference(Action action, Object target){
    	ActionWrapper wrapper = new ActionWrapper(action);
    	wrapper.setTarget(target);
    	return wrapper;
    }


    public ActionWrapper(Action action){
        super(action);
    }

    /**
     * Makes an action vetoable by wrapping it in a VetoableAction.  The VetoListener will be checked
     * prior to the wrapped Action's invocation
     * @param vetoListener
     */
    @Override
    public void addVeto(VetoListener vetoListener){
        setAction(new VetoableAction(getAction(), vetoListener));
    }

    /**
     * Appends an Action to the beginning of another.  The new action is guaranteed to be run BEFORE
     * the original Action.
     * @param preActionAction
     */
    @Override
    public void addPreEventAction(Action preActionAction){
        setAction(new PreAction(getAction(), preActionAction));
    }

    /**
     * Appends an Action to the end of another.  The new action is quaranteed to be run AFTER
     * the original Action.
     * @param postActionAction
     */
    @Override
    public void addPostEventAction(Action postActionAction){
        setAction(new PostAction(getAction(), postActionAction));
    }
    
    
    private TargetedActionEvent targetAction(ActionEvent ae){
    	return 
		new TargetedActionEvent(
				ae.getSource(), 
				ae.getID(), 
				ae.getActionCommand(),
				ae.getWhen(),
				ae.getModifiers(),
				getTarget());
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        getAction().actionPerformed(targetAction(ae));
    }


    /**
     * Replaces the currently wrapped action with the one specified.  The original action is saved
     * and can be reinstated through the use of removeActionRef().
     *
     * @param action
     */
	@Override
	public void pushAction(Action action) {
		setAction(new PushAction(getAction(), action));
	}

    /**
     * Represents a "pushed" action and provides for saving the original action.
     */
	private static class PushAction extends ActionCombiner{
		
		public PushAction(Action originalAction, Action newAction) {
			super(originalAction, newAction);
		}
		
		public void actionPerformed(ActionEvent ae){
			_combinedAction.actionPerformed(ae);
		}
		
	
	}

    /**
     * removes the Action being wrapped if it is an ActionRef.  The action represented by the ActionRef
     * becomes the new action being wrapped.  In essence an ActionWrapper can actually be a stack of ActionRef's
     * ending in an Action.  This functionality removes the most recent ActionRef from the stack.
     */
	@Override
	public void removeActionRef() {
		Action act = getAction();
		if(act instanceof ActionRef){
			ActionRef actr = (ActionRef)act;
			setAction(actr.getAction());
		}
		
		
	}
}

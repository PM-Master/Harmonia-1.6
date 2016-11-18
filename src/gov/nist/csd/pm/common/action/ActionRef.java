package gov.nist.csd.pm.common.action;

import javax.swing.*;

/**
 * Extends the functionality of Action in a way that permits other parts of an app to  be swap out, add, or insert veto granting functionality at runtime. The "Ref" in the class name refers to "reference" in that an ActionRef is a reference to  an enclosed action which also exposes the functionality of that action by default. The upshot of this arrangement is that you can pass action ref's around like actions.  Insert them into UI elements, but still change their behavior after the fact.
 * @author   Robert McHugh
 */

public interface ActionRef extends Action {
	public static final String CONTEXT = ActionRef.class.getPackage().getName().replace('.', '_');
	public static final String ID =  CONTEXT + "_ID";
	public static final String TARGET = CONTEXT + "_TARGET";
	
	/**
	 * @return   the action that this object references
	 * @uml.property  name="action"
	 * @uml.associationEnd  
	 */
	public Action getAction();
	/**
	 * Replaces the action that this object references with another.  The previous action is not retained.
	 * @param  action
	 * @uml.property  name="action"
	 */
	public void setAction(Action action);
	/**
	 * Sets the target of an action.  A target is the object that an action will presumably  perform operations on, but doesn't have to be.  It's only provided for flexibility's sake.
	 * @param  target
	 * @uml.property  name="target"
	 */
	public void setTarget(Object target);
	/**
	 * Gets the target of an action.
	 * @return
	 * @uml.property  name="target"
	 */
	public Object getTarget();

    /**
     * Returns a target as a specific type
     *
     * A ClassCastException will be thrown if the target's actual type
     * doesnt match the one provided.
     * @param type
     * @param <S>
     * @return
     */
	public <S> S getTarget(Class<S> type);
	
	
	/**
	 * This method replaces the current action with the parameter but retains the previous
	 * action.  That way the original action can be recovered if needed.  While this action is
	 * in place the original action will be ignored.
	 * @param action
	 */
	public void pushAction(Action action);
	/**
	 * Allows for the insertion of a veto into an action.  The veto listener added will be notified
	 * prior to an action being called.  If the veto listener vetoes an action then that action will
	 * not be called.
	 * @param vetoListener
	 */
	public void addVeto(VetoListener vetoListener);
	/**
	 * Inserts an action that will be called prior to the original action.
	 * The original action will be called regardless of the result of the pre action
	 * @param preActionAction
	 */
    public void addPreEventAction(Action preActionAction);
    /**
     * Inserts an action that will be called after the original action.
     * The original action's function remains unchanged.
     * @param postActionAction
     */
    public void addPostEventAction(Action postActionAction);
    /**
     * Action Refs can potentially contain a chain of action refs.  This method
     * will examine the current action to see if it's an action ref, then it will
     * replace it's referenced action with the action referenced by the action ref.
     * 
     * If the action contained by this action ref is not itself an action ref this method
     * does nothing.
     */
    public void removeActionRef();
}
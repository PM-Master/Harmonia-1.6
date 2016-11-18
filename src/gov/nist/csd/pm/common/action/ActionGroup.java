package gov.nist.csd.pm.common.action;

import com.google.common.base.Predicate;
import gov.nist.csd.pm.common.pattern.Transformable;
import gov.nist.csd.pm.common.pattern.Walkable;
import gov.nist.csd.pm.common.util.collect.Insert;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/1/11 Time: 10:49 AM Represents a group of actions as an action. An action group isnt used as an action directly, but the facilities available to the Action class were. UI elements, Menu bars and context sensitive share many of the same actions and the same organization of those actions.  This class centralizes that concept in a UI independent manner.
 */
public interface ActionGroup extends Action {
    String IS_ACTION_GROUP = ActionGroup.class.getCanonicalName() + ".GROUP_NAME";
    String IS_GROUP_DIVIDER = ActionGroup.class.getCanonicalName() + ".GROUP_DIVIDER";
    String IS_ENABLED = ActionGroup.class.getCanonicalName() + ".IS_ENABLED";

    /**
     * Add a divider to a group of actions
     * The interpretation of dividers are dependent on what is utilizing an action group
     * @return
     */
    ActionGroup addDivider();

    /**
     * Creates a subgroup of an action group
     * @param name
     * @param actionsInSubgroup
     * @return
     */
    ActionGroup addSubgroupOf(String name, Action... actionsInSubgroup);

    /**
	 * Sets the name of the action group
	 * @param  name
	 * @uml.property  name="groupName"
	 */
    void setGroupName(String name);

    /**
	 * Gets the name of the group
	 * @return
	 * @uml.property  name="groupName"
	 */
    String getGroupName();

    /**
     * Add an action to this group
     * @param action
     */
    void addAction(Action action);

    void insertActionBefore(Action referenceAction, Action toInsert);

    void insertActionAfter(Action referenceAction, Action toInsert);

    void insertActionBefore(Predicate<Action> actionMatcher, Action toInsert);

    void insertActionAfter(Predicate<Action> actionMatcher, Action toInsert);

    void insertAction(Insert insert, Predicate<Action> actionMatching, Action action);

    void insertActionIntoGroup(Insert insert, Predicate<? super ActionGroup> agPredicate, Action action);

    List<Action> getActions();

    List<ActionGroup> getActionGroups();

    Action findAction(Action action);

    <T extends Action> T find(Predicate<? super T> predicate, Class<T> type);

    <TT> Transformable<Action> actionTransformable();

    Walkable<Action> actionWalkable();

    Walkable<ActionGroup> actionGroupWalkable();

    Transformable<ActionGroup> actionGroupTransformable();

    Map<String, Object> getValues();
}

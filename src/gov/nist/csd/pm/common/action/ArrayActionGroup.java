package gov.nist.csd.pm.common.action;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import gov.nist.csd.pm.common.pattern.AbstractTransformable;
import gov.nist.csd.pm.common.pattern.CollectingVisitor;
import gov.nist.csd.pm.common.pattern.Transformable;
import gov.nist.csd.pm.common.pattern.Walkable;
import gov.nist.csd.pm.common.util.collect.Insert;
import gov.nist.csd.pm.common.util.reflect.Classes;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static gov.nist.csd.pm.common.util.Predicates.hasAny;
import static gov.nist.csd.pm.common.util.lang.Objects.castTo;

/**
 * Created by IntelliJ IDEA.
 * User: Robert McHugh
 * Date: 6/29/11
 * Time: 12:25 PM
 * A group of Action's represented as an array.
 */
public class ArrayActionGroup implements ActionGroup {


    /**
	 * @uml.property  name="_values"
	 * @uml.associationEnd  multiplicity="(0 -1)" ordering="true" elementType="java.lang.Object" qualifier="s:java.lang.String java.lang.Object"
	 */
    private final Map<String, Object> _values = new HashMap();
    /**
	 * @uml.property  name="_actionsInGroup"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javax.swing.Action"
	 */
    private final List<Action> _actionsInGroup;
    /**
	 * @uml.property  name="pcs"
	 */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);






    protected ArrayActionGroup(Action... actionsInGroup){
        putValue(IS_ACTION_GROUP, true);
        putValue(IS_ENABLED, true);
        _actionsInGroup = newArrayList(actionsInGroup);

    }

    public static ArrayActionGroup createActionGroup(Action... actionsInGroup){
        return new ArrayActionGroup(actionsInGroup);
    }

    public static ArrayActionGroup createActionGroup(String name, Action... actionsInGroup){
        ArrayActionGroup ag = createActionGroup(actionsInGroup);
        ag.setGroupName(name);
        return ag;
    }

    public static ArrayActionGroup createActionDivider(){
        ArrayActionGroup ag = createActionGroup();
        ag.putValue(IS_GROUP_DIVIDER, Boolean.TRUE);
        return ag;
    }

    @Override
    public ActionGroup addDivider(){
        ArrayActionGroup ag = createActionDivider();
        addAction(ag);
        return ag;
    }

    @Override
    public ActionGroup addSubgroupOf(String name, Action... actionsInSubgroup){
        ArrayActionGroup subGroup = createActionGroup(name, actionsInSubgroup);
        addAction(subGroup);
        return subGroup;
    }

    @Override
    public void setGroupName(String name){
        putValue(NAME, name);
    }

    @Override
    public String getGroupName(){
        Object groupName = getValue(NAME);
        return groupName == null ? null : groupName.toString();
    }

    @Override
    public void addAction(Action action){
        _actionsInGroup.add(action);
    }



    @Override
    public void insertActionBefore(Action referenceAction, Action toInsert){
        insertActionBefore(Predicates.equalTo(referenceAction), toInsert);
    }

    @Override
    public void insertActionAfter(Action referenceAction, Action toInsert){
        insertActionAfter(Predicates.equalTo(referenceAction), toInsert);
    }

    @Override
    public void insertActionBefore(Predicate<Action> actionMatcher, Action toInsert){
        insertAction(Insert.BEFORE, actionMatcher, toInsert);
    }

    @Override
    public void insertActionAfter(Predicate<Action> actionMatcher, Action toInsert){
        insertAction(Insert.AFTER, actionMatcher, toInsert);
    }

    private static Function<ActionGroup, Collection<Action>> actionsInGroup(){
        return new Function<ActionGroup, Collection<Action>>(){

            @Override
            public Collection<Action> apply(@Nullable ActionGroup actionGroup) {
                return actionGroup.getActions();
            }
        };
    }

    @Override
    public void insertAction(Insert insert, Predicate<Action> actionMatchingPredicate, Action action){

        Predicate<ActionGroup> groupContainingAction = Predicates.compose(hasAny(actionMatchingPredicate), actionsInGroup());
        ActionGroup result = find(groupContainingAction, ArrayActionGroup.class);
        if(result != null){
            Action actionMatching = Iterables.find(result.getActions(), actionMatchingPredicate);
            insert.insert(action,actionMatching, result.getActions());
        }
    }

    @Override
    public void insertActionIntoGroup(Insert insert, Predicate<? super ActionGroup> agPredicate, Action action){
        ActionGroup result = find(agPredicate, ArrayActionGroup.class);
        if(result != null){
            insert.insert(action, null, result.getActions());
        }
    }




    @Override
    public List<Action> getActions(){
        return _actionsInGroup;
    }



    @Override
    public Action findAction(Action action){
        return find(Predicates.equalTo(action), Action.class);
    }


    @Override
    public <T extends Action> T find(Predicate<? super T> predicate, Class<T> type){
        CollectingVisitor<Action> collector = new CollectingVisitor<Action>();
        actionWalkable().traverse(collector,Classes.isObjectSubclassOf(type));
        Iterable<T> collectedActions = filter(transform(collector.get(), castTo(type)), predicate);
        return collectedActions.iterator().hasNext() ? collectedActions.iterator().next() : null;
    }


    @Override
    public List<ActionGroup> getActionGroups(){
        List<ActionGroup> ags = newArrayList();
        for(Action action : getActions()){
            if(action instanceof ActionGroup){
                ags.add((ActionGroup)action);
            }
        }
        return ags;
    }

    @Override
    public <TT> Transformable<Action> actionTransformable(){
        return new AbstractTransformable<Action>(this){

            @Override
            public Collection<? extends Action> neighborNodes(Action parentNode) {
                 if(parentNode instanceof ArrayActionGroup){
                    return ((ActionGroup)parentNode).getActions();
                 }
                 else{
                     return newArrayList();
                 }
            }
        };
    }

    @Override
    public Walkable<Action> actionWalkable(){
        return actionTransformable();
    }

    @Override
    public Walkable<ActionGroup> actionGroupWalkable(){
        return actionGroupTransformable();
    }

    @Override
    public Transformable<ActionGroup> actionGroupTransformable(){
        return new AbstractTransformable<ActionGroup>(this){


              @Override
              public Collection<ActionGroup> neighborNodes(ActionGroup parentNode) {
                  return parentNode.getActionGroups();
              }
          };
    }

    @Override
    public Map<String, Object> getValues() {
        return _values;
    }


    @Override
    public Object getValue(String s) {
        return _values.get(s);
    }

    @Override
    public void putValue(String s, Object o) {
       _values.put(s, o);
    }

    @Override
    public void setEnabled(boolean b) {
       putValue(IS_ENABLED, new Boolean(b));
    }

    @Override
    public boolean isEnabled() {
        Boolean value = (Boolean)getValue(IS_ENABLED);
        return value != null ? value.booleanValue() : false;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.removePropertyChangeListener(propertyChangeListener);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {

    }


}

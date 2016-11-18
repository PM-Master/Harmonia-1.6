package gov.nist.csd.pm.common.action;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Iterables.find;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/1/11
 * Time: 8:34 AM
 * Utilities for dealing with Action objects in various contexts.
 */
public final class Actions {


    private Actions(){}

    /**
     * Constructs a Predicate matching an Action to the given name.
     * The Predicate will return true if the Action's NAME parameter matches
     * the one given, false otherwise.  Actions with a null NAME parameter will
     * throw an exception.
     * @param name - a string representing the name to be matched.
     * @return
     */
     public static Predicate<Action> actionWithName(final String name){
        return new Predicate<Action>(){

            @Override
            public boolean apply(@Nullable Action action) {
                return nullToEmpty((String) action.getValue(Action.NAME)).equals(name);
            }
        };
    }

    /**
     * Constructs a Predicate matching an Action to the given id
     * The Predicate will return true if an Action's ID parameter matches the one given,
     * false otherwise.  Actions with a null ID parameter will throw an exception.
     * @param id
     * @return
     */
    public static Predicate<Action> actionWithId(final String id){
        return new Predicate<Action>() {
            @Override
            public boolean apply(@Nullable Action action) {
                return nullToEmpty((String) action.getValue(ActionRef.ID)).equals(id);
            }
        };
    }

    /**
     * Constructs a Predicate that compares an Action's param value to the
     * predicate given, returning the result of that predicates application.
     * @param param
     * @param predicate
     * @return
     */
    public static Predicate<Action> withParamMatching(final String param, final Predicate<Object> predicate){
        checkNotNull(predicate);
        checkNotNull(param);
        return new Predicate<Action>(){

            @Override
            public boolean apply(@Nullable Action action) {
                return action != null ? predicate.apply(action.getValue(param)): false;
            }
        };
    }

    /**
     * Utility for finding an Action in a collection by id.
     * @param actions
     * @param id
     * @param <T>
     * @return
     */
    public static <T extends Action> T findByID(Collection<T> actions, String id){
        try{
        return find(actions,
                    Actions.withParamMatching(ActionRef.ID,
                                              Predicates.equalTo((Object) id)));

        }catch(NoSuchElementException nsee){
            return null;
        }

    }

    /**
     * Composes multiple actions into a single action by creating a new action that calls the originals in sequence.
     * @param actions
     * @return
     */
    public static Action compose(final Action... actions){
        return new AbstractAction(){

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for(Action act : actions){
                    act.actionPerformed(actionEvent);
                }
            }
        };
    }
}

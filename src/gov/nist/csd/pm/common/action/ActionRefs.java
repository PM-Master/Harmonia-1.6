package gov.nist.csd.pm.common.action;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/7/11
 * Time: 2:35 PM
 * Utilities for dealing with ActionRef objects
 */
public final class ActionRefs {
    private ActionRefs(){}
    public static Predicate<ActionRef> actionRefHasID(final String id){
        return new Predicate<ActionRef>(){

            @Override
            public boolean apply(@Nullable ActionRef tActionRef) {
                String aid = nullToEmpty((String) tActionRef.getValue(ActionRef.ID));
                return aid.equals(id);
            }
        };
    }

    public static boolean actionRefHasID(ActionRef actionRef, String id){
        return actionRefHasID(id).apply(actionRef);
    }
}

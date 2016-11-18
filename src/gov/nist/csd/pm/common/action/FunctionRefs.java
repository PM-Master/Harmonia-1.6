package gov.nist.csd.pm.common.action;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/15/11
 * Time: 2:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class FunctionRefs {
    public static Predicate<FunctionRef> takingInputCompatibleWithType(final Class<?> type){
        return new Predicate<FunctionRef>(){

            @Override
            public boolean apply(@Nullable FunctionRef function) {
                return function != null && function.takes().isAssignableFrom(type);
            }
        };
    }
    public static Predicate<FunctionRef> givingOutputCompatibleWithType(final Class<?>type){
        return new Predicate<FunctionRef>(){

            @Override
            public boolean apply(@Nullable FunctionRef function) {
                return function != null && function.gives().isAssignableFrom(type);
            }
        };
    }
}

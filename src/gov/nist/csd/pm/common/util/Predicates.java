package gov.nist.csd.pm.common.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/30/11
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class Predicates {
    private Predicates(){
    }


    public static <T> Predicate<Collection<T>> hasAny(final Predicate<T> predicate){
        return new Predicate<Collection<T>>(){

            @Override
            public boolean apply(@Nullable Collection<T> ts) {
                return ts == null? false : Iterators.any(ts.iterator(), predicate);
            }
        };
    };

    public static <T> Predicate<Collection<T>> hasAll(final Predicate<T> predicate){
        return new Predicate<Collection<T>>(){

            @Override
            public boolean apply(@Nullable Collection<T> ts) {
                return ts == null ? false : Iterators.all(ts.iterator(), predicate);
            }
        };
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Administrator
 */
public class ReflectionUtils {


    public static <T> Predicate<Collection<T>> everyObjectIn(Collection<T> collection, Function<T, Predicate<T>> predicateFunction){
        return new OverAllElementsPredicate<T>(collection, predicateFunction);
    }

    public static class OverAllElementsPredicate<T> implements Predicate<Collection<T>>{

        private final Collection<T> _collection;
        private final Function<T, Predicate<T>> _predicateFunction;

        public OverAllElementsPredicate(Collection<T> collection, Function<T, Predicate<T>> predicateFunction){
            _collection = checkNotNull(collection);
            _predicateFunction = checkNotNull(predicateFunction);
        }

        @Override
        public boolean apply(Collection<T> t) {
            Iterator<T> iter = _collection.iterator();
            Boolean result = true;
            for(T elt : t){
                T other = iter.next();
                Predicate<T> predicate = _predicateFunction.apply(elt);
                result &= predicate.apply(other);
            }
            return result;
        }

    }


}

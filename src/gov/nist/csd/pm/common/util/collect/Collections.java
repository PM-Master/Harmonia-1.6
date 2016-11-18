/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.util.collect;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import gov.nist.csd.pm.common.util.Tuple2;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static java.util.Arrays.asList;

/**
 *
 * @author Administrator
 */
public class Collections {

    public static <T, F> void padToEqualSize(Collection<T> first, Collection<F> second) {
        if (first.size() < second.size()) {
            List list = Lists.newArrayListWithExpectedSize(second.size() - first.size());
            first.addAll(list);
        }
        if (second.size() < first.size()) {
            List list = Lists.newArrayListWithExpectedSize(first.size() - second.size());
            second.addAll(list);
        }
    }

    public static <T, F> Collection<Tuple2<T, F>> zip(Collection<T> first, Collection<F> second) {
        padToEqualSize(first, second);
        assert first.size() == second.size();
        final Iterator<F> iter = second.iterator();
        return Collections2.transform(first, new Function<T, Tuple2<T, F>>() {

            @Override
            public Tuple2<T, F> apply(T f) {
                return new Tuple2<T, F>(f, iter.next());
            }
        });
    }

    /**
     * Flattens a collection of collections of type T into a single collection of type T
     * @param <T>
     * @param collection
     * @return
     */
    public static <T, C extends Collection<T>> Collection<T> flatten(Collection<C> collection) {
        Function<Collection<C>, Collection<T>> flatFunk = flattenFunction();
        return flatFunk.apply(collection);
    }

    public static <T, C extends Collection<T>> Function<Collection<C>, Collection<T>> flattenFunction() {
        return new Function<Collection<C>, Collection<T>>() {

            @Override
            public Collection<T> apply(Collection<C> collection) {
                Collection<T> flatCollection = Lists.newArrayList();
                for (Collection<T> internal : collection) {
                    flatCollection.addAll(internal);
                }
                return flatCollection;
            }
        };
    }

    /**
     * Multiplies the elements in a collection
     * by the amount specified and collates the
     * results into a result array of size
     * collection.size() * amount
     * @param <T>
     * @param collection
     * @param amount
     * @return
     */
    public static <T> Collection<T> multiplyCollection(Collection<T> collection, int amount){
        Function<Collection<Collection<T>>, Collection<T>> flatten;
        flatten = Collections.flattenFunction();
        Function<Collection<T>, Collection<Collection<T>>> aggregate;
        aggregate = Aggregate.aggregateFunction(5);
        return Functions.compose(flatten, aggregate).apply(collection);
    }

    public static <T> void applyOver(Collection<T> collection, Function<T, ?> function) {
        for (T obj : collection) {
            function.apply(obj);
        }
    }

    public static <T> Function<Collection<?>, Integer> getSize(){
        return new GetCollectionSizeFunction();
    }


    public static <T> Function<Collection<T>, Collection<T>> select(Class<T> klass, final Predicate<? super T> predicate){
        return select(predicate);
    }


    public static <T> Function<Collection<T>, Collection<T>> select(final Predicate<? super T> predicate){
        return new Function<Collection<T>, Collection<T>>(){


            @Override
            public Collection<T> apply(@Nullable Collection<T> ts) {
                return filter(ts, predicate);
            }
        };
    }


    public static <T> Function<T[], Collection<T>> toList(Class<T> klass){
        return toList();
    }

    public static <T> Function<T[],Collection<T>> toList(){
        return new Function<T[], Collection<T>>(){

            @Override
            public Collection<T> apply(@Nullable T[] ts) {
                return asList(ts);
            }
        };
    }

    private static class GetCollectionSizeFunction implements Function<Collection<?>, Integer>{

        @Override
        public Integer apply(Collection<?> t) {
            return t.size();
        }

    }

    public static boolean isNullOrEmpty(Collection<? extends Object> coll){
        return coll == null || coll.isEmpty();
    }
}

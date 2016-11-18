/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.util.collect;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class Aggregate<T> implements Function<Supplier<T>, List<T>> {

    /**
	 * @uml.property  name="count"
	 */
    private int count = 0;

    public Aggregate(int count) {
        this.count = count;
    }


    public static <A> Function<A, Collection<A>> aggregateFunction(final int count){
        return new Function<A, Collection<A>>(){
            @Override
            public List<A> apply(A f) {
                return new Aggregate<A>(count).apply(Suppliers.ofInstance(f));
            }

        };
    }

    public static <A> Supplier<List<A>> aggregateSupplier(final int count, final Supplier<A> supplier) {
        return new Supplier<List<A>>() {
            @Override
            public List<A> get() {
                return new Aggregate<A>(count).apply(supplier);
            }
        };
    }

    @Override
    public List<T> apply(Supplier<T> f) {
        List<T> list = Lists.newArrayList();
        for (int i = 0; i < count; ++i) {
            list.add(f.get());
        }
        return list;
    }
}

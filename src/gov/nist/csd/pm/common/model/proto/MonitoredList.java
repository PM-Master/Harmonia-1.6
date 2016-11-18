package gov.nist.csd.pm.common.model.proto;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/10/11
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class MonitoredList<T> extends ForwardingList<T> {

    public static <T> MonitoredList<T> forType(Class<T> cls){
        return (MonitoredList<T>) new MonitoredList();
    }

    /**
	 * @uml.property  name="_delegateList"
	 */
    private List<T> _delegateList = new ArrayList<T>();
    /**
	 * @uml.property  name="_inserts"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.model.proto.DeferredInsert"
	 */
    private List<DeferredInsert<Integer, T>> _inserts = new ArrayList();
    /**
	 * @uml.property  name="_removes"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.model.proto.DeferredRemove"
	 */
    private List<DeferredRemove<Integer, T>> _removes = new ArrayList();


    ;

    @Override
    protected List<T> delegate() {
        return _delegateList;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean result = true;
        for(Object o : collection){
            result &= remove(o);
        }
        return result;
    }

    @Override
    public boolean add(T element) {
        reconcileInsert(super.size(), element);
        return super.add(element);
    }



    @Override
    public boolean remove(Object object) {
        int index = super.indexOf(object);
        boolean result = super.remove(object);
        if(result){
            reconcileRemove(index, (T)object);
        }
        return result;
    }

    private Predicate<Supplier<T>> containingObject(final T object){
        return new Predicate<Supplier<T>>(){

            @Override
            public boolean apply(@Nullable Supplier<T> input) {
                if(input == null) return false;

                return input.get() != null ? input.get() == object : input.get().equals(object);
            }
        };
    }



    private void reconcileRemove(Integer index, T object){
        _removes.add(new DeferredRemove<Integer, T>(index, object));
    }

    private void reconcileInsert(Integer index, T object){
        _inserts.add(new DeferredInsert<Integer, T>(index, object));
    }

    @Override
    public boolean addAll(Collection<? extends T> ts) {
        boolean result = true;
        for(T o : ts){
            result &= add(o);
        }
        return result;
    }

    @Override
    public T remove(int index) {
        T removed = super.remove(index);
        reconcileRemove(index, removed);
        return super.remove(index);
    }

    @Override
    public T set(int index, T element) {
        return super.set(index, element);
    }
}

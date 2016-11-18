package gov.nist.csd.pm.common.action;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import gov.nist.csd.pm.common.util.AbstractRef;
import gov.nist.csd.pm.common.util.reflect.Classes;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;

import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/11/11 Time: 11:42 AM FunctionalityCenter provides a central point for extending an overriding the functionality of applications that advertise that functionality.
 */



public enum FunctionalityCenter{
    /**
	 * @uml.property  name="iNSTANCE"
	 * @uml.associationEnd  
	 */
    INSTANCE;

    public static interface FunctionalityListener{
        public void functionalityAdded(FunctionRef function);
        public void functionalityRemoved(FunctionRef function);
    }

    /**
	 * @author  Administrator
	 */
    private static class FilteringFunctionalityListener implements FunctionalityListener{
        Predicate<? super FunctionRef> _filter;
        /**
		 * @uml.property  name="_listener"
		 * @uml.associationEnd  
		 */
        FunctionalityListener _listener;

        public FilteringFunctionalityListener(FunctionalityListener listener){
            this(listener, Predicates.alwaysTrue());
        }

        public FilteringFunctionalityListener(FunctionalityListener listener, Predicate<? super FunctionRef> filter){
            _listener = listener;
            _filter = (Predicate<? super FunctionRef>) (filter != null ? filter : Predicates.alwaysTrue());
        }

        @Override
        public void functionalityAdded(FunctionRef function) {
            if(_filter.apply(function)){
                _listener.functionalityAdded(function);
            }
        }

        @Override
        public void functionalityRemoved(FunctionRef function) {
            if(_filter.apply(function)){
                _listener.functionalityRemoved(function);
            }
        }

        @Override
        public boolean equals(Object o) {
            return o != null ? o.equals(_listener) : o == _listener;
        }

        @Override
        public int hashCode() {
            return _listener != null ? _listener.hashCode() : 0;
        }
    }

    /**
	 * @uml.property  name="_listeners"
	 * @uml.associationEnd  
	 */
    private Multimap<Object, FilteringFunctionalityListener>_listeners = ArrayListMultimap.create();

    /**
	 * @uml.property  name="_functionalityMapping"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.action.FunctionRef"
	 */
    private ListMultimap<Object, FunctionRef> _functionalityMapping =  ArrayListMultimap.create();


    public <T,R> void addFunctionality(Function<T,R> function, Class<T> taking, Class<R> giving, Object forContext){
        FunctionRef<T,R> functionref = createFunctionRef(function, taking, giving);
        _functionalityMapping.put(forContext, functionref);
        notifyAddedFunctionInContext(functionref, forContext);
    }

    private boolean listenerContextEmpty(Object context){
        return _listeners.get(context) == null || _listeners.get(context).isEmpty();
    }

    public void addListener(FunctionalityListener listener, Object context){
        addListener(listener, Predicates.alwaysTrue(), context);


    }

    public void addListener(FunctionalityListener listener, Predicate<? super FunctionRef> filter, Object context){
        _listeners.put(context, new FilteringFunctionalityListener(listener, filter));
        if(_functionalityMapping.containsKey(context)){
            for(FunctionRef functionRef : _functionalityMapping.get(context)){
                notifyAddedFunctionInContext(functionRef, context);
            }
        }
    }


    public void removeListener(FunctionalityListener listener, Object context){
        if(_listeners.containsEntry(context, listener)){
            _listeners.remove(context, listener);
        }
    }

    private <T,R> void notifyAddedFunctionInContext(FunctionRef<T, R> ref, Object context){
        if(listenerContextEmpty(context)) return;
        for(FunctionalityListener listener : _listeners.get(context)){
            listener.functionalityAdded(ref);
        }
    }

    private <T,R> void notifyRemovedFunctionInContext(FunctionRef<T,R> ref, Object context){
        if(listenerContextEmpty(context)) return;
        for(FunctionalityListener listener : _listeners.get(context)){
            listener.functionalityRemoved(ref);
        }
    }

    public <T,R> void removeFunctionality(Function<T,R> function, Object forContext){
        if(_functionalityMapping.containsEntry(forContext, function)){
            int index = _functionalityMapping.get(forContext).indexOf(function);
            FunctionRef<T,R> ref = _functionalityMapping.get(forContext).get(index);
            _functionalityMapping.remove(forContext, ref);
            notifyRemovedFunctionInContext(ref, forContext);
        }
    }

    private boolean functionContextEmpty(Object context){
        return _functionalityMapping.get(context) == null || _functionalityMapping.get(context).isEmpty();
    }

    public <T,R> FunctionRef<T,R> getFunctionRef(Class<T> taking, Class<R> giving, Object fromContext){
        try{
            return Iterables.find(getFunctionRefs(fromContext), and(functionRefTakes(taking), functionRefGives(giving)));
        }catch(NoSuchElementException nsee){
            return new NullFunctionRef<T,R>(taking, giving);
        }
    }

    private static class NullFunctionRef<T,R> extends FunctionRefImpl<T,R>{

        public NullFunctionRef(Class<T>taking, Class<R>giving) {

            super(nullFunction(taking, giving), taking, giving);
        }
    }

    private static <T,R> Function<T,R> nullFunction(Class<T> taking, Class<R> giving){
        return new Function<T, R>() {
                @Override
                public R apply(@Nullable T t) {
                    return null;
                }
            };
    }

    public  Collection<FunctionRef> getFunctionRefs(Object context){
        if(functionContextEmpty(context)) return newArrayList();
        return Collections.unmodifiableCollection(_functionalityMapping.get(context));
    }

    private static <T> Function<FunctionRef,Class<T>> getFunctionRefTakes(){
        return new Function<FunctionRef, Class<T>>(){

            @Override
            public Class<T> apply(@Nullable FunctionRef trFunctionRef) {
                return trFunctionRef.takes();
            }
        };
    }



    public static Predicate<FunctionRef> functionRefTakes(Class<?> takes){
        return Predicates.compose(Classes.isSubclassOf(takes), getFunctionRefTakes());
    }

    private static <R> Function<FunctionRef,Class<R>> getFunctionRefGives(){
        return new Function<FunctionRef, Class<R>>(){

            @Override
            public Class<R> apply(@Nullable FunctionRef functionRef) {
                return functionRef.gives();
            }
        };
    }

    public static Predicate<FunctionRef> functionRefGives(Class<?> gives){
        return Predicates.compose(Classes.isSubclassOf(gives), getFunctionRefGives());
    }

    private static class FunctionRefImpl<T,R> extends AbstractRef<Function<T,R>> implements FunctionRef<T,R> {

        public final Class<? extends T> _takes;
        public final Class<? super R> _gives;

        public FunctionRefImpl(Function<T, R> ref, Class<? extends T> takes, Class<? super R> gives) {
            super(ref);
            _takes = takes;
            _gives = gives;
        }

        @Override
        public Class<? extends T> takes() {
            return _takes;
        }

        @Override
        public Class<? super R> gives() {
            return _gives;
        }

        @Override
        public R apply(@Nullable T t) {

            R result = get().apply(t);
            return result;
        }



    }

    public static <T,R> FunctionRef<T,R> createFunctionRef(final Function<T,R>function, final Class<T> taking, final Class<R> giving){
        return new FunctionRefImpl(function, taking, giving);
    }





}

package gov.nist.csd.pm.common.model.proto;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import gov.nist.csd.pm.common.model.BaseObject;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static com.google.common.base.Predicates.*;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.all;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Maps.newHashMap;
import static gov.nist.csd.pm.common.util.collect.Arrays.isNullOrEmpty;
import static gov.nist.csd.pm.common.util.collect.Collections.flatten;
import static gov.nist.csd.pm.common.util.lang.Classes.allInterfacesOf;
import static gov.nist.csd.pm.common.util.reflect.Classes.getDeclaredMethods;
import static gov.nist.csd.pm.common.util.reflect.Classes.isSuperclassOf;
import static gov.nist.csd.pm.common.util.reflect.Methods.*;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 8/9/11
 * Time: 6:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjectProxy implements InvocationHandler{

    /**
	 * @uml.property  name="defaultPropertyHash"
	 * @uml.associationEnd  qualifier="key:java.lang.String gov.nist.csd.pm.common.model.proto.Property"
	 */
    final HashMap<String, Property> defaultPropertyHash = newHashMap();
    /**
	 * @uml.property  name="memoizedResultHash"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Object" qualifier="m:java.lang.reflect.Method java.lang.Object"
	 */
    final HashMap<Method, Object> memoizedResultHash = newHashMap();
    /**
	 * @uml.property  name="proxyOf"
	 */
    final Class<?> proxyOf;
    public static <T extends BaseObject> T getProxyFor(Class<T> klass){
        return getProxyFor(klass, new HashMap<String, Object>());
    }




    public static <T extends BaseObject> T getProxyFor(Class<T> klass, Map<String, Object> defaults){
        if(klass == null){
            throw new RuntimeException("No class given to proxy");
        }
        if(!klass.isInterface()){
            throw new RuntimeException("Only interfaces can have proxies generated for them");
        }

        Object prox = Proxy.newProxyInstance(klass.getClassLoader(), allInterfacesOf(klass), new ObjectProxy(klass, defaults));
        return klass.cast(prox);
    }

    private static Function<String, Method> getDeclaredMethodFrom(final Class<?> klass){
        return new Function<String, Method>(){

            @Override
            public Method apply(@Nullable String input) {
                try {
                    klass.getDeclaredMethod(input);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return null;
            }
        };
    }

    /**
     * Creates a proxy that takes in a mapping of properties to deferred results.
     * In other words you pass in defaults for the return values of each no-arg function in
     * the provided interface.
     * @param klass
     * @param deferredReturnValues
     * @param <T>
     * @return
     */
    public static <T extends BaseObject> T getDeferredProxy(Class<T> klass, Map<String, Object> deferredReturnValues){
        if(klass == null){
            throw new RuntimeException("No class given to proxy");
        }
        if(!klass.isInterface()){
            throw new RuntimeException("Only interfaces can have proxies generated for them");
        }
        ObjectProxy invokeHandler = new ObjectProxy(klass, null);
        Iterable<Method> declaredMethods = transform(deferredReturnValues.keySet(), getDeclaredMethodFrom(klass));
        for(Method m : declaredMethods){
            Object deferredReturnValue = deferredReturnValues.get(m.getName());
            //verify the deferred return value can be passed through the found method prior to invocation.
            if(deferredReturnValue == null ||
                    m.getReturnType().isAssignableFrom(deferredReturnValue.getClass())){
                invokeHandler.memoizedResultHash.put(m, deferredReturnValue);
            }
            else{
                throw new RuntimeException(
                        String.format("The method %s found cannot return the type specified %s",
                            m.getName(), deferredReturnValue.getClass().getName()));
            }
        }
        Object prox = Proxy.newProxyInstance(klass.getClassLoader(), allInterfacesOf(klass), invokeHandler);
        return klass.cast(prox);
    }

    private Collection<String> interfaceMethodNames(){
        return transform(flatten(transform(asList(allInterfacesOf(proxyOf)), getDeclaredMethods())), getMethodName());
    }




    public ObjectProxy(Class<?> proxyOf, Map<String, Object> defaults) {
        if(defaults == null){
            defaults = new HashMap<String, Object>();
        }
        this.proxyOf = proxyOf;
        Iterable<String> applicableKeys  =Iterables.filter(defaults.keySet(), not(in(interfaceMethodNames())));
        for(String key : applicableKeys){
            Property prop = MutableProperty.forType(Object.class, key);
            defaultPropertyHash.put(key, prop);
            
        }
    }



    /**
	 * @uml.property  name="fnCreateMutablePropertyForMethod"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Function<CallingContext, Object> fnCreateMutablePropertyForMethod = new Function<CallingContext, Object>(){

        @Override
        public Object apply(@Nullable CallingContext input) {
            if(input != null){
                Method m = input.method;
                if(!memoizedResultHash.containsKey(m)) {

                    Class<?> type = Object.class;
                    if(m.getAnnotation(TypeOf.class) != null){
                        type = m.getAnnotation(TypeOf.class).value();

                    }
                    Property prop = defaultPropertyHash.containsKey(m.getName())?
                            defaultPropertyHash.get(m.getName()) :
                            MutableProperty.forType(type, m.getName());
                    memoizedResultHash.put(m, prop);

                }

                return memoizedResultHash.get(m);
            }
            return null;
        }
    };

    /**
	 * @uml.property  name="fnCreateMonitoredListForMethod"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private  Function<CallingContext, Object> fnCreateMonitoredListForMethod = new Function<CallingContext, Object>(){

        @Override
        public Object apply(@Nullable CallingContext input) {
            if(input != null){
                Method m = input.method;
                if(!memoizedResultHash.containsKey(input)){
                    Class<?> type = Object.class;
                    if(input.method.getAnnotation(TypeOf.class) != null){
                        type = input.method.getAnnotation(TypeOf.class).value();

                    }
                    memoizedResultHash.put(m, MonitoredList.forType(type));
                }
                return memoizedResultHash.get(m);
            }
            return null;
        }
    };

    /**
	 * @uml.property  name="fnCreateSimpleMapForMethod"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Function<CallingContext, Object> fnCreateSimpleMapForMethod = new Function<CallingContext, Object>(){

        @Override
        public Object apply(@Nullable CallingContext input) {
            if(input != null ){
                Method m = input.method;
                if(!memoizedResultHash.containsKey(m)){
                    memoizedResultHash.put(m, new MonitoredMap());
                }
                return memoizedResultHash.get(m);
            }
            return null;
        }
    };

     /**
	 * @uml.property  name="fnPerformEquality"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Function<CallingContext, Object> fnPerformEquality = new Function<CallingContext, Object>(){

        private boolean compareInterfaces(Object one, Object another){
            List<Class<?>> interfacesOne = asList(one.getClass().getInterfaces());
            List<Class<?>> interfacesTwo = asList(one.getClass().getInterfaces());
            if(all(interfacesOne,/*are*/ in(interfacesTwo))
                    && interfacesOne.size() == interfacesTwo.size()){
                Iterable<Method> noArgumentMethods = getNoArgumentMethods(interfacesOne);
                return all(noArgumentMethods, returnEqualResponsesFrom(one, another));

            }
            return false;

        }

        @Override
        public Object apply(@Nullable CallingContext input) {

            if(isNullOrEmpty(input.parameters))return Boolean.FALSE;
            return ObjectProxy.this.equals(input.parameters[0]);
        }
    };

    private Iterable<Method> getNoArgumentMethods(List<Class<?>> interfacesOne) {
        return filter(flatten(transform(interfacesOne, getDeclaredMethods())), withParameterSignature());
    }

    private Predicate<Method> returnEqualResponsesFrom(final Object one, final Object two) {
        return new Predicate<Method>(){

            @Override
            public boolean apply(@Nullable Method input) {
                Object responseOne = call(one, input);
                Object responseTwo = call(two, input);
                return  (responseOne == null && responseOne == responseTwo) ||
                        (responseOne != null && responseOne.equals(responseTwo));
            }
        };
    }

    /**
	 * @uml.property  name="fnPerformToString"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Function<CallingContext, Object> fnPerformToString = new Function<CallingContext, Object>(){

        @Override
        public Object apply(@Nullable CallingContext context) {
            /*List<Class<?>> interfaces = asList(context.target.getClass().getInterfaces());
            Iterable<Method> noArgMethods =
                    filter(getNoArgumentMethods(interfaces),
                            and(
                                    not(havingName("equals")),
                                    not(havingName("toString"))));
            Objects.ToStringHelper tsh = Objects.toStringHelper(proxyOf);
            for(Method m : noArgMethods){
                tsh = tsh.add(m.getName(), call(context.target, m));
            }*/
            return "Object Proxy of " + proxyOf;
        }
    };

    /**
	 * @uml.property  name="fnPerformHashCode"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Function<CallingContext, Object> fnPerformHashCode = new Function<CallingContext, Object>(){

        @Override
        public Object apply(@Nullable CallingContext input) {
            return ObjectProxy.this.hashCode();
        }
    };

    /**
	 * @uml.property  name="invocationHandlers"
	 * @uml.associationEnd  qualifier="test:com.google.common.base.Predicate com.google.common.base.Function"
	 */
    private Map<Predicate<Method>, Function<CallingContext, Object>> invocationHandlers = new HashMap(){
        {
            put(compose(isSuperclassOf(Property.class),getReturnType()), fnCreateMutablePropertyForMethod);
            put(compose(isSuperclassOf(List.class), getReturnType()), fnCreateMonitoredListForMethod);
            put(compose(isSuperclassOf(Map.class), getReturnType()), fnCreateSimpleMapForMethod);
            put(and(havingName("equals"), withParameterSignature(Object.class)), fnPerformEquality);
            put(and(havingName("toString"), withParameterSignature()), fnPerformToString);
            put(and(havingName("hashCode"), withParameterSignature()), fnPerformHashCode);
        }
    };

    private class CallingContext{
        final Method method;
        final Object target;
        final Object[] parameters;
        private CallingContext(Object o, Method m, Object[] params){
            target = o;
            method = m;
            parameters = params;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CallingContext)) return false;

            CallingContext that = (CallingContext) o;

            if (!method.equals(that.method)) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(parameters, that.parameters)) return false;
            if (!target.equals(that.target)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = method.hashCode();
            result = 31 * result + target.hashCode();
            result = 31 * result + (parameters != null ? Arrays.hashCode(parameters) : 0);
            return result;
        }
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {

        for(Predicate<Method> test : invocationHandlers.keySet()){
            if(test.apply(method)){
                CallingContext cc = new CallingContext(o, method, objects);
                Object result = invocationHandlers.get(test).apply(cc);
                return result;
            }
        }
        return null;
    }
}

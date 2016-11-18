package gov.nist.csd.pm.common.util.reflect;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import gov.nist.csd.pm.common.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.*;
import static com.google.common.collect.Iterables.*;
import static gov.nist.csd.pm.common.util.ReflectionUtils.everyObjectIn;
import static gov.nist.csd.pm.common.util.collect.Collections.getSize;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 1/21/11
 * Time: 6:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class Methods {
    private Methods(){}
    public static Predicate<Method> havingName(String name){
        return new MethodNamedPredicate(name);
    }



    public static Predicate<Method> whoseNameStartsWith(final String name){
        return new Predicate<Method>(){

            @Override
            public boolean apply(Method method) {
                return method.getName().startsWith(name);
            }
        };
    }

    public static <T extends Annotation> Predicate<Method> annotatedWith(final Class<T> annClass){
        return new Predicate<Method>() {
            @Override
            public boolean apply(@Nullable Method method) {
                return method != null ? method.getAnnotation(annClass) != null : false;
            }
        };
    }

    public static Predicate<Method> withSignatureOfIndexedGetter(){
        return new Predicate<Method>(){

            @Override
            public boolean apply(Method method) {
                return  method.getName().startsWith("get")
                        && method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0].equals(Integer.TYPE)
                        && method.getReturnType() != null;
            }
        };
    }

     public static Predicate<Method> withSignatureOfIndexedSetter(){
        return new Predicate<Method>(){

            @Override
            public boolean apply(Method method) {
                return  method.getName().startsWith("set")
                        && method.getParameterTypes().length == 2
                        && method.getParameterTypes()[0].equals(Integer.TYPE)
                        && method.getReturnType() == null;
            }
        };
    }

     public static Predicate<Method> withSignatureOfSimpleGetter(){
        return new Predicate<Method>(){

            @Override
            public boolean apply(Method method) {
                return method.getName().startsWith("get")
                        && method.getParameterTypes().length == 0
                        && method.getReturnType() != null;
            }
        };
    }

     public static Predicate<Method> withSignatureOfSimpleSetter(){
        return new Predicate<Method>(){

            @Override
            public boolean apply(Method method) {
                return method.getName().startsWith("set")
                        && method.getParameterTypes().length == 1
                        && method.getReturnType() == null;
            }
        };
    }

    public static <T> Function<Object, Object> set(String named, T value){
        String fullName = String.format("set%s%s", named.substring(0, 1).toUpperCase(), named.substring(1));
        return call(fullName, value);
    }

    public static Method findMethod(Class klass, String name){
        return find(asList(klass.getMethods()), havingName(name));
    }

    public static Method findMethod(Class klass, String name, Class<?>... signature) throws NoSuchMethodException{
        Iterable<Method> methods = filter(asList(klass.getMethods()), and(havingName(name), withParameterSignature(signature)));
        if(methods.iterator().hasNext()){
            return methods.iterator().next();
        }
        else{
            String message = String.format("Could not find a method (%s) with signature (%s) in class (%s)", name, Arrays.toString(signature), klass.getName());
            throw new NoSuchMethodException(message);
        }
    }

    public static  Function<Object, Object> call(String method, Object param){
        return new CallOneArgFunction(method, param);
    }

    private static class MethodNamedPredicate implements Predicate<Method>{

        private final String _name;

        public MethodNamedPredicate(String name){
            _name = name;
        }


        @Override
        public boolean apply(Method m) {
            return m.getName().equals(_name);
        }

    }

    public static Predicate<Method> withParameterSignature(Class<?>... signature){
        return new MethodAcceptsSignaturePredicate(signature);
    }

    private static class MethodAcceptsSignaturePredicate implements Predicate<Method>{

        private final Class<?>[]_signature;

        public MethodAcceptsSignaturePredicate(Class<?>[] signature){
            _signature= signature;
        }

        @Override
        public boolean apply(Method t) {
            List<Class<?>> params = asList(t.getParameterTypes());
            List<Class<?>> signature = asList(_signature);
            System.out.printf("Comparing %s to %s\n",
                    Joiner.on(", ").join(transform(params, Classes.getNameFromClass())),
                    Joiner.on(", ").join(transform(signature, Classes.getNameFromClass())));
            Predicate<Collection<?>> hasSameSize = compose(equalTo(_signature.length), getSize());
            return and(hasSameSize, everyObjectIn(signature, Classes.subclasses())).apply(params);
        }

    }

    public static Predicate<Method> withGenericReturnType(Type type){
        return new Predicate<Method>(){

            @Override
            public boolean apply(@Nullable Method input) {
                return false;
            }
        } ;
    }

    public static Function<Method, Type[]> getGenericParameterTypes(){
        return new Function<Method, Type[]>(){

            @Override
            public Type[] apply(@Nullable Method input) {
                return input != null ? input.getGenericParameterTypes() : new Type[0];
            }
        };
    }

    public static Function<Method, String> getMethodName(){
        return new Function<Method, String>(){

            @Override
            public String apply(@Nullable Method input) {
                return input != null ? input.getName() : "";
            }
        };
    }

    public static Function<Method, Annotation[]> getAnnotations(){
        return new Function<Method, Annotation[]>(){

            @Override
            public Annotation[] apply(@Nullable Method input) {
                return input != null ? input.getDeclaredAnnotations(): new Annotation[0];
            }
        };
    }

    public static Function<Method, Type> getGenericReturnType(){
        return new Function<Method, Type>(){

            @Override
            public Type apply(@Nullable Method input) {
                return input != null ? input.getGenericReturnType() : null;
            }
        };
    }

    public static Function<Method, Class<?>> getReturnType(){
        return new Function<Method, Class<?>>(){

            @Override
            public Class<?> apply(@Nullable Method input) {
                return input != null ? input.getReturnType() : null;
            }
        };
    }

    public static Object call(Object o, Method m){
        return call(o, m, new Object[0]);
    }

    public static Object call(Object o, Method m, Object... params){
        try {
            return m.invoke(o, params);
        } catch (IllegalAccessException e) {
            Throwables.propagate(e);  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            Throwables.propagate(e);  //To change body of catch statement use File | Settings | File Templates.
        }
        throw new RuntimeException("Unknown error in Methods.call()");
    }

    public static class CallOneArgFunction<T, V> implements Function<T, T> {

        private final String _named;
        private final V _value;

        public CallOneArgFunction(String named, V value){
            _named = checkNotNull(named);
            _value = checkNotNull(value);
        }

        @Override
        public T apply(T f) {
            try {
                Method method = findMethod(f.getClass(), _named, _value.getClass());
                call(f, method, _value);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(ReflectionUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(ReflectionUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(ReflectionUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
            return f;
        }

    }

}

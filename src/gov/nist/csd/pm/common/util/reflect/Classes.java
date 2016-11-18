package gov.nist.csd.pm.common.util.reflect;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Predicates.compose;
import static gov.nist.csd.pm.common.util.Functions2.getObjectClass;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 1/21/11
 * Time: 6:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class Classes {
    private Classes() {
    }

    public static Function<Class<?>, String> getNameFromClass() {
        return new Function<Class<?>, String>() {

            @Override
            public String apply(Class<?> f) {
                return f.getName();
            }
        };
    }

    public static <T> Predicate<Object> isObjectSubclassOf(final Class<T> type){
        return compose(isSubclassOf(type), getObjectClass());
    }

    public static <T> Predicate<Class<?>> isSubclassOf(final Class<T> type) {
        return new Predicate<Class<?>>() {

            @Override
            public boolean apply(Class<?> t) {
                //System.out.printf("%s isSubclassOf(%s)\n", t.getName(), type.getName());
                //String words = "type: %s is assignable from %s: %s\n";
                //System.out.printf("type: %s is instance of %s: %s\n", type.getName(), t.getName(), type.isInstance(t));
                //System.out.printf(words, type.getName(), t.getName(), type.isAssignableFrom(t));
                //System.out.printf(words, t.getName(), type.getName(), t.isAssignableFrom(type));
                return type.isAssignableFrom(t);
            }
        };
    }

    public static <T> Predicate<Class<?>> isSuperclassOf(final Class<T> type) {
        return new Predicate<Class<?>>() {

            @Override
            public boolean apply(Class<?> t) {
                //System.out.printf("%s isSubclassOf(%s)\n", t.getName(), type.getName());
                //String words = "type: %s is assignable from %s: %s\n";
                //System.out.printf("type: %s is instance of %s: %s\n", type.getName(), t.getName(), type.isInstance(t));
                //System.out.printf(words, type.getName(), t.getName(), type.isAssignableFrom(t));
                //System.out.printf(words, t.getName(), type.getName(), t.isAssignableFrom(type));
                return t != null ? t.isAssignableFrom(type) : false;
            }
        };
    }

    public static Function<Object, Class<?>> classFromObject(){
        return _classFromObjectFunction;
    }

    private static final Function<Object, Class<?>> _classFromObjectFunction = new Function<Object, Class<?>>(){

        @Override
        public Class<?> apply(@Nullable Object o) {
            return o.getClass();
        }
    };

    public static Function<Class, List<Field>> getDeclaredFields(){
        return _getDeclaredFieldsFunction;
    }

    private static final Function<Class, List<Field>> _getDeclaredFieldsFunction = new Function<Class, List<Field>>(){

        @Override
        public List<Field> apply(@Nullable Class aClass) {
            return aClass == null ? new ArrayList<Field>()  : asList(aClass.getDeclaredFields());
        }
    };

    public static Function<Class, List<Method>> getDeclaredMethods() {
        return _getDeclaredMethodsFunction;
    }


    private static final Function<Class, List<Method>> _getDeclaredMethodsFunction = new Function<Class, List<Method>>() {

        @Override
        public List<Method> apply(Class aClass) {
            return aClass == null ? new ArrayList<Method>() : asList(aClass.getDeclaredMethods());
        }
    };


    public static Function<Class<?>, Predicate<Class<?>>> subclasses() {
        return new SubclassesFunction();
    }

    private static class SubclassesFunction implements Function<Class<?>, Predicate<Class<?>>> {

        @Override
        public Predicate<Class<?>> apply(Class f) {
            return subclasses(f);
        }
    }



    public static Predicate<Class<?>> subclasses(Class<?> klass) {
        return isSubclassOf(klass);
    }

}

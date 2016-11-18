package gov.nist.csd.pm.common.util.lang;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/20/11
 * Time: 1:08 PM
 * Utilities for working with Class objects
 */
public class Classes {

    public static final int NOT_A_SUPERCLASS = -1;

    /**
     * Implementation of a simple super class comparator.
     *
     * Will return whether or not the first argument is a super class of the second.
     */
    public static final Comparator<Class<?>> SUPERCLASS_COMPARATOR = new Comparator<Class<?>>(){

        List<Class<?>> classQueue = newLinkedList();

        @Override
        public int compare(Class<?> t, Class<?> t1) {
            classQueue.clear();
            classQueue.add(t1);
            int distance = 0;
            while(!classQueue.isEmpty()){
                Class<?>currentClass = classQueue.get(0);
                if(currentClass.equals(t)){
                    //encountered
                    return distance;
                }
                else{
                    if(!currentClass.equals(Object.class)){
                        Class<?> superClass = currentClass.getSuperclass();
                        if(null != superClass){
                            classQueue.add(superClass);
                        }
                        List<Class<?>> interfaces = Arrays.asList(currentClass.getInterfaces());
                        if(null != interfaces){
                            classQueue.addAll(interfaces);
                        }
                    }
                    classQueue.remove(0);
                }
            }
            return NOT_A_SUPERCLASS;
        }

    };

    public static <T extends Enum> List<String> getEnumNamesFor(Class<T> klass){
        return Lists.transform(getEnumsFor(klass), Objects.intoString());
    }

    public static <T extends Enum>  List<T> getEnumsFor(Class<T> klass){
        return asList(klass.getEnumConstants());
    }

    public static Class[] allInterfacesOf(Class<?> klass){
        Class[] subfaces = klass.getInterfaces();
        Class[] interfaces = new Class[subfaces.length + 1];
        interfaces[0] = klass;
        System.arraycopy(subfaces, 0, interfaces, 1, subfaces.length);
        return interfaces;
    }
}

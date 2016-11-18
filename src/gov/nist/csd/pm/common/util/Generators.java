/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Administrator
 */
public abstract class Generators {

    /**
     * This is a utility to turn a map of Class -> Generator into a
     * Function of Class -> Generator
     *
     * This lookup function will also lookup generators that conform to the superclasses and interfaces
     * to see if any of those match a key in the map.  The lookup function will find and return the first successful
     * match to the given parameter.
     *
     * @param map - Map<Class, Generator>
     * @param defaultValue - Generator<Class, Object, Objct> a generator to return if a class, it's superclasses, or any of it's implemented interfaces ARE NOT
     * keys in the map provided as the first parameter.
     * @return
     */
    public static Function<Class<?>, Generator<? extends Class<?>, ? extends Object, ?>> forMap(
            final Map<Class<?>, ? extends Generator<? extends Class<?>, ? extends Object, ?>> map,
            final Generator<? extends Class<?>, ? extends Object, ?> defaultValue) {
        return new Function<Class<?>, Generator<? extends Class<?>, ? extends Object, ?>>() {

            @Override
            public Generator<? extends Class<?>, ? extends Object, ?> apply(Class<?> key) {
                //Logger.getLogger(Generators.class.getName()).log(Level.INFO, "Map keys: {0}", map.keySet().toString());
                //Logger.getLogger(Generators.class.getName()).log(Level.INFO, "Current key: {0}", key);
                List<Class<?>> candidatekeys = Lists.newArrayList();
                //System.out.printf("key(%s) != null (%s) && !map.contains(key) (%s) \n",
//                        key,
//                        Boolean.valueOf(key != null).toString(),
//                        Boolean.valueOf(!map.containsKey(key)).toString());
                while (key != null && !map.containsKey(key)) {
                    //Logger.getLogger(Generators.class.getName()).log(Level.INFO, "Candidate keys: {0}", candidatekeys.toString());
                    Class<?>[] interfaces = key.getInterfaces();
                    //Logger.getLogger(Generators.class.getName()).log(Level.INFO, "Interfaces: {0}", Arrays.deepToString(interfaces));
                    if (interfaces != null && interfaces.length > 0) {
                        candidatekeys.addAll(Lists.asList(key, key.getInterfaces()));
                    }
                    if (key.getSuperclass() != null) {
//                        Logger.getLogger(Generators.class.getName()).log(Level.INFO, "Superclass: {0}", key.getSuperclass().getName());
                        candidatekeys.add(key.getSuperclass());
                    }
                    key = candidatekeys.size() > 0 ? candidatekeys.remove(0) : null;
//                    Logger.getLogger(Generators.class.getName()).log(Level.INFO, "key({0}) != null ({1}) && !map.contains(key) ({2})",
//                        new Object[]{
//                        key.toString(),
//                        Boolean.valueOf(key != null).toString(),
//                        Boolean.valueOf(!map.containsKey(key)).toString()});
//                    Logger.getLogger(Generators.class.getName()).log(Level.INFO, "Candidate keys: {0}", candidatekeys.toString());
                }
                Generator result = map.containsKey(key) ? map.get(key) : defaultValue;
                if (null != result) {
//                    Logger.getLogger(
//                            Generators.class.getName()).log(
//                                Level.INFO,
//                                "key {0} key found? ({1}) result {2}",
//                                new Object[]{key, map.containsKey(key), result});
                }
                return result;
            }
        };
    }
    static Random myRandom = new Random();

    public static String generateRandomName(int n) {
        byte[] bytes = new byte[n];
        myRandom.nextBytes(bytes);
        return Conversions.byteArray2HexString(bytes, 0, n);
    }


}


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.model;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Primitives;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;
import static gov.nist.csd.pm.common.util.reflect.Classes.getNameFromClass;
import static gov.nist.csd.pm.common.util.reflect.Classes.isSubclassOf;
import static java.util.Collections.unmodifiableList;

/**
 *
 * @author Administrator
 */
public class Transformations {

    private static final String NULL_STR = "NULL";
    private static final List<Transformation<Object, Object>> acceptedTransformations = Lists.newArrayList();
    private static final List<Class<? extends Number>> numericTypes =
            unmodifiableList(new ArrayList<Class<? extends Number>>() {

        {
            add(Float.class);
            add(Double.class);
            add(Integer.class);
            add(Long.class);
            add(BigInteger.class);
            add(BigDecimal.class);
        }
    });

    private static abstract class AbstractReversableTransformation<F, T> extends AbstractTransformation<F, T> {

        public AbstractReversableTransformation(Class<F> from, Class<T> to) {
            super(from, to);
        }

        @Override
        public Boolean isReversable() {
            return Boolean.TRUE;
        }
    }

    private static abstract class AbstractTransformation<F, T> implements Transformation<F, T> {

        private final Class<F> fromType;
        private final Class<T> toType;
        private static final String TWO_WAY_TRANSFORM_ERROR_MESSAGE =
                "Two-way transforms are not implemented by default."
                + "\nImplement reverseTransorm in your  "
                + Transformation.class.getSimpleName()
                + "implementation to gain this functionality";

        public AbstractTransformation(Class<F> fromType, Class<T> toType) {
            this.fromType = checkNotNull(fromType);
            this.toType = checkNotNull(toType);
        }

        @Override
        public Class<F> fromType() {
            return fromType;
        }

        @Override
        public Class<T> toType() {
            return toType;
        }

        /**
         * Unless specified all transforms are assumed to be somewhat
         * lossy.  This default was chosen to people wouldn't use transforms
         * thinking they were lossless and find out later that they were losing
         * accuracy in their data.
         * @return
         */
        @Override
        public Boolean isLossless() {
            return LOSSY;
        }

        @Override
        public Boolean isReversable() {
            Boolean reversable = false;
            try {
                isReverseLossless();
                reversable = true;
            } catch (UnsupportedOperationException unsope) {
                reversable = false;
            }
            return reversable;
        }

        /**
         * Specifies whether or not the reverse transform is lossless.
         * @return
         */
        @Override
        public Boolean isReverseLossless() {
            throw new UnsupportedOperationException(TWO_WAY_TRANSFORM_ERROR_MESSAGE);
        }

        /**
         * Transforms the incoming toObject into an equivalent object of the fromType
         *
         * Disabled by default.
         * @param toObject
         * @return
         */
        @Override
        public F reverseTransform(T toObject) {
            throw new UnsupportedOperationException(TWO_WAY_TRANSFORM_ERROR_MESSAGE);
        }
    }

    private static Object nullToNullPlaceholder(Object obj) {
        return obj == null ? NULL_STR : obj;
    }

    private static <T> T nullToNullPlaceholder(T obj, T placeholder) {
        return obj == null ? checkNotNull(placeholder) : obj;
    }

    /**
     * returns null if the object is null or the result of toString()
     * @param toObject
     * @return
     */
    private static <T> String nullOrToString(T toObject) {
        return toObject == null ? null : toObject.toString();
    }

    private static <T extends Number> T backupGenerator(Class<T> klass, String arg) {
        T rval = null;
        Class<T> unwrapped = Primitives.unwrap(klass);
        String valueMethodName = String.format("%sValue", unwrapped.getName());
        try {
            BigDecimal accurateSource = new BigDecimal(arg);
            Logger.getLogger(Transformations.class.getName()).log(Level.INFO, "Trying to find method {0}", valueMethodName);
            Method method = BigDecimal.class.getMethod(valueMethodName, new Class[0]);
            rval = klass.cast(method.invoke(accurateSource, new Object[0]));
        } catch (Exception ex) {
        }
        return rval;
    }

    /**
     * returns the result of invoking the constructor of a specified class
     * taking a single string argument.
     * @param <T>
     * @param klass
     * @param arg
     * @return
     */
    private static <T extends Number> T objectOrNullFrom(Class<T> klass, String arg) {
        T obj = null;
        try {
            Constructor<T> cstr = klass.getConstructor(String.class);
            obj = cstr.newInstance(arg);
        } catch (Exception e) {
            obj = backupGenerator(klass, arg);
        }
        return obj;
    }

    public static <F, T> Function<F, T> transformFunction(Transformation<F, T> transformation) {
        return new TransformFunction(transformation);
    }

    /**
	 * @author  Administrator
	 */
    private static class TransformFunction<F, T> implements Function<F, T> {

        /**
		 * @uml.property  name="_transformation"
		 * @uml.associationEnd  
		 */
        private final Transformation<F, T> _transformation;

        public TransformFunction(Transformation<F, T> transformation) {
            _transformation = transformation;
        }

        @Override
        public T apply(F f) {
            return _transformation.transform(f);
        }
    }

    public static <F, T> Function<T, F> reverseTransformFunction(Transformation<F, T> transformation) {
        return new ReverseTransformFunction<T, F>(transformation);
    }

    /**
	 * @author  Administrator
	 */
    private static class ReverseTransformFunction<T, F> implements Function<T, F> {

        /**
		 * @uml.property  name="_transformation"
		 * @uml.associationEnd  
		 */
        private final Transformation<F, T> _transformation;

        public ReverseTransformFunction(Transformation<F, T> transformation) {
            _transformation = transformation;
        }

        @Override
        public F apply(T f) {
            return _transformation.reverseTransform(f);
        }
    }

    public static <F, T> void createTransformation(final Transformation<F, T> transform) {
        acceptedTransformations.add((Transformation<Object, Object>) transform);
    }

    private static <String, T extends Number> void createStringNumericTransform(Class<String> fromType, Class<T> toType) {
        createTransformation(new AbstractReversableTransformation<String, T>(fromType, toType) {

            @Override
            public T transform(String fromObject) {
                return objectOrNullFrom(this.toType(), (java.lang.String) fromObject);
            }

            @Override
            public String reverseTransform(T toObject) {
                //For some reason the type system is
                //errors if you don't case java.lang.String to String
                //To my knowledge they are the same thing.
                return (String) nullOrToString(toObject);
            }
        });
    }

   

    private static <F extends Number, T extends Number> void createNumberToNumberTransform(Class<F> fromType, Class<T> toType) {
        createTransformation(new AbstractReversableTransformation<F, T>(fromType, toType) {

            private <TT extends Number> TT convertNumberToNumber(Number from, Class<TT> toType) {
                TT rval = null;
                Class<?> transType = null;

                if (any(numericTypes, isSubclassOf(from.getClass()))
                        && any(numericTypes, isSubclassOf(toType))) {
                    if (BigDecimal.class.isInstance(toType)) {
                        transType = Double.TYPE;
                        //from = new Double(from.doubleValue());
                    } else if (BigInteger.class.isInstance(toType)) {
                        transType = Long.TYPE;
                        //from = new Long(from.longValue());
                    } else {
                        transType = Primitives.unwrap(toType);
                    }
                    Constructor<?> c;
                    try {
                        c = toType.getConstructor(transType);
                        String valueMethodName = String.format("%sValue", transType.getName());
                        Method valueMethod = from.getClass().getMethod(valueMethodName, new Class[0]);
                        Object nativeValue = valueMethod.invoke(from, new Object[0]);
                        rval = toType.cast(c.newInstance(nativeValue));
                    } catch (Exception ex) {
                        Logger.getLogger(Transformations.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    Iterable<String> istr = Iterables.transform(numericTypes, getNameFromClass());
                    String typeString = Joiner.on(',').join(istr);
                    String format = String.format("This method can only handle conversion between the following types:\n\t%s", typeString);
                    throw new Error(format);
                }

                return rval;
            }

            @Override
            public T transform(F fromObject) {

                T rval = convertNumberToNumber(fromObject, toType());
                return rval;
            }

            @Override
            public F reverseTransform(T toObject) {
                F rval = convertNumberToNumber(toObject, fromType());
                return rval;
            }
        });
    }

    static {



//        createStringNumericTransform(String.class, Double.class);
//        createStringNumericTransform(String.class, Long.class);
//        createStringNumericTransform(String.class, Integer.class);
//        createStringNumericTransform(String.class, BigInteger.class);
//        createStringNumericTransform(String.class, BigDecimal.class);


        for (int i = 0; i < numericTypes.size(); ++i) {
            createStringNumericTransform(String.class, numericTypes.get(i));
            for (int j = i + 1; j < numericTypes.size(); ++j) {
                createNumberToNumberTransform(numericTypes.get(i), numericTypes.get(j));
            }
        }
    }

    /**
     * Creates a typed, inverted transformation from F,T to T,F
     * @param <T>
     * @param <F>
     * @param transform
     * @return
     */
    public static <T, F> Transformation<F, T> invertTransformation(final Transformation<T, F> transform) {
        return new InvertedTransformation(transform);
    }

    /**
	 * @author  Administrator
	 */
    private static class InvertedTransformation<F, T> implements Transformation<F, T> {

        /**
		 * @uml.property  name="_transformation"
		 * @uml.associationEnd  
		 */
        private final Transformation<T, F> _transformation;

        public InvertedTransformation(Transformation<T, F> transformation) {
            _transformation = transformation;
        }

        @Override
        public Class<F> fromType() {
            return _transformation.toType();
        }

        @Override
        public Class<T> toType() {
            return _transformation.fromType();
        }

        @Override
        public Boolean isLossless() {
            return _transformation.isReverseLossless();
        }

        @Override
        public Boolean isReverseLossless() {
            return _transformation.isLossless();
        }

        @Override
        public Boolean isReversable() {
            return _transformation.isReversable();
        }

        @Override
        public T transform(F fromObject) {
            return _transformation.reverseTransform(fromObject);
        }

        @Override
        public F reverseTransform(T toObject) {
            return _transformation.transform(toObject);
        }
    }

    /**
     * Wraps a predicate so that the wrapped predicate tests an inverted version of the
     * transformer.  @see Transformations.InvertedTransform for details.
     * @param <T>
     * @param <F>
     * @param predicate
     * @return
     */
    public static <T, F> Predicate<Transformation<F, T>> reverseTransform(final Predicate<Transformation<T, F>> predicate) {
        return new Predicate<Transformation<F, T>>() {

            @Override
            public boolean apply(Transformation<F, T> t) {
                return predicate.apply(invertTransformation(t));
            }
        };


    }

    /**
     * Checks a predicate to ensure that it is capable of transforming from a type or a superclass
     * of that type.
     * @param <T>
     * @param <F>
     * @param fromClass
     * @return
     */
    public static <T, F> Predicate<Transformation<T, F>> transformsFrom(Class<?> fromClass) {
        return new TransformsFromPredicate<T, F>(fromClass);
    }

    private static class TransformsFromPredicate<T, F> implements Predicate<Transformation<T, F>> {

        private final Class<?> _fromClass;

        public TransformsFromPredicate(Class<?> fromClass) {
            _fromClass = fromClass;
        }

        @Override
        public boolean apply(Transformation<T, F> t) {
            return _fromClass.isAssignableFrom(t.fromType());
        }
    }

    /**
     * Returns a predicate that ensures a transformer can transform an object into
     * the specified type.
     * @param <T>
     * @param <F>
     * @param klass
     * @return
     */
    public static <T, F> Predicate<Transformation<T, F>> transformsTo(final Class<?> klass) {
        return new TransformsToPredicate(klass);
    }

    private static class TransformsToPredicate<T, F> implements Predicate<Transformation<T, F>> {

        private final Class<?> _toClass;

        public TransformsToPredicate(Class<?> toClass) {
            _toClass = toClass;
        }

        @Override
        public boolean apply(Transformation<T, F> t) {
            return _toClass.isAssignableFrom(t.toType());
        }
    }

    /**
     * returns a transformation object that will accept accept objects of a specified type.
     *
     * @param <T>
     * @param <F>
     * @param transformation - the transformation to be reversed, must be reversible.
     * @param fromType - the incoming type for the transformation.
     * @return if the transformations to type is a super class or super interface then an inverted transformation is returned
     *    otherwise the original transformation is returned.
     */
    public static <T, F> Transformation<Object, Object> reverseTransformationIfNeeded(Transformation<T, F> transformation, Class<?> fromType) {
        Transformation<Object, Object> result = (Transformation<Object, Object>) transformation;
        if (fromType.isAssignableFrom(transformation.toType())) {
            result = (Transformation<Object, Object>) invertTransformation(transformation);
        }
        return result;
    }

    public static <K> K coerceValueTo(Object value, Class<K> klass) {
        checkNotNull(klass);
        K result = null;
        if (value != null) {
            if (klass.isAssignableFrom(value.getClass())) {
                Logger.getLogger(Transformations.class.getName()).log(Level.INFO, "Type {0} is assignable from {1}",
                        new Object[]{value.getClass().getSimpleName(), klass.getName()});
                result = klass.cast(value);
            } else {
                Logger.getLogger(Transformations.class.getName()).log(Level.INFO, "Type {0} is not assignable from {1}",
                        new Object[]{value.getClass().getSimpleName(), klass.getName()});
                /*
                 * This predicate retrieves transforms that will either
                 * 1.  Transform the value of 'value' into a type klass
                 * 2.  Reverse transform the value of 'value' into a type specified by class
                 */
                try {
                    Predicate<Transformation<Object, Object>> predicate = or(
                            and(
                            transformsFrom(value.getClass()),
                            transformsTo(klass)),
                            and(
                            reverseTransform(transformsFrom(value.getClass())),
                            reverseTransform(transformsTo(klass))));
                    Logger.getLogger(Transformations.class.getName()).log(Level.INFO, "created predicate");
                    Iterable<Transformation<Object, Object>> transformations = filter(acceptedTransformations, predicate);
                    Iterator<Transformation<Object, Object>> transformationsIter = transformations.iterator();

                    if (transformationsIter.hasNext()) {
                        Transformation<Object, Object> transformation = transformationsIter.next();
                        Logger.getLogger(Transformations.class.getName()).log(Level.INFO, "Found transformation from {0} to {1}",
                                new Object[]{transformation.fromType().getSimpleName(), transformation.toType().getSimpleName()});
                        transformation = reverseTransformationIfNeeded(transformation, value.getClass());
                        result = klass.cast(transformation.transform(value));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (result == null) {
                String className = klass == null ? "No target class specified" : klass.getSimpleName();
                Logger.getLogger(
                        Transformations.class.getName()).log(Level.WARNING, "Could not coerce value {0} from type {1} to type {2}", new Object[]{value, value.getClass().getName(), className});
            }
        } else {
            Logger.getLogger(Transformations.class.getName()).log(Level.INFO, "Cannot coerce null values, returning null.");
        }
        return result;

    }
}

package gov.nist.csd.pm.common.util.reflect;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Predicates.isNull;
import static com.google.common.base.Predicates.not;
import static gov.nist.csd.pm.common.util.lang.Strings.camelCaseJoin;
import static gov.nist.csd.pm.common.util.reflect.Methods.findMethod;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 1/21/11
 * Time: 6:42 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Fields {
    private Fields(){}

    /**
	 * @author   Administrator
	 */
    public enum PropertyType{
        /**
		 * @uml.property  name="sIMPLE"
		 * @uml.associationEnd  
		 */
        SIMPLE, /**
		 * @uml.property  name="iNDEXED"
		 * @uml.associationEnd  
		 */
        INDEXED;
        public Class[] getterSignature(Class type){
            switch(this){
               case SIMPLE:
                   return new Class[]{};
               case INDEXED:
                   return new Class[]{Integer.TYPE};
            }
            return null;
        }
        public Class[] setterSignature(Class type){
            switch(this){
                case SIMPLE:
                    return new Class[]{type};
                case INDEXED:
                    return new Class[]{Integer.TYPE, type};
            }
            return null;
        }
    }

    public static Function<Field, Class<?>> getDeclaringClass(){
           return new Function<Field, Class<?>>(){

               @Override
               public Class<?> apply(Field field) {
                   return field.getDeclaringClass();
               }
           };
       }


    public static void setFieldWithObject(Field field, Object target, Object value){
        if(field != null){
            try {
                field.set(target, value);
            } catch (IllegalAccessException e) {
                Logger.getLogger(Fields.class.getName()).log(Level.SEVERE, e.getLocalizedMessage());
            }
        }
    }

    public static Predicate<Field> withAnnotationType(final Class<? extends Annotation> annotationType){
        return new Predicate<Field>(){

            @Override
            public boolean apply(@Nullable Field field) {
                if(annotationType != null && field != null){
                    return field.getAnnotation(annotationType) != null;
                }
                return false;
            }
        };
    }

    public static Predicate<Field> withName(final String name){
        return new Predicate<Field>(){

            @Override
            public boolean apply(@Nullable Field field) {

                return name != null ? field.getName().equals(name) : false;
            }
        };
    }

    public static Predicate<Field> withType(final Class<?> aType){
        return new Predicate<Field>(){

            @Override
            public boolean apply(@Nullable Field field) {
                return aType != null ? field.getType().isAssignableFrom(aType) : false;
            }
        };
    }

    public static Function<Field, Object> getObjectFromField(final Object target){
		return new Function<Field, Object>(){

			@Override
			public Object apply(Field field) {
				// TODO Auto-generated method stub
				Object result = null;
				if(field != null){
					try {
                        field.setAccessible(true);
						result = field.get(target);
                        field.setAccessible(false);
					} catch (IllegalArgumentException e) {
						Logger.getLogger(Fields.class.getName()).log(Level.SEVERE, e.getLocalizedMessage());
					} catch (IllegalAccessException e) {
						Logger.getLogger(Fields.class.getName()).log(Level.SEVERE, e.getLocalizedMessage());
					}
				}
				return result;
			}
			
		};
    	
    }


    public static Function<Field,String> getName(){
        return new Function<Field, String>(){

            @Override
            public String apply(Field field) {
                return field.getName();
            }
        };
    }


    public static Function<Field, Class<?>> getType(){
        return new Function<Field, Class<?>>(){

            @Override
            public Class<?> apply(Field field) {
                return field.getType();
            }
        };
    }

    public static Function<Field,String> getGetterMethodName(final PropertyType type){
        return new Function<Field, String>(){

            @Override
            public String apply(Field field) {
                return getterMethodName(field, type);
            }
        };
    }

    public static Function<Field,String> getSetterMethodName(final PropertyType type){
        return new Function<Field, String>(){

            @Override
            public String apply(Field field) {
                return setterMethodName(field, type);
            }
        };
    }

    public static Function<Field,Method> getSetterMethod(final PropertyType type){
        return new Function<Field, Method>(){

            @Override
            public Method apply(Field field) {
                return setterMethod(field, type);
            }
        };
    }

    public static Function<Field,Method> getGetterMethod(final PropertyType type){
        return new Function<Field, Method>(){

            @Override
            public Method apply(Field field) {
                return getterMethod(field, type);
            }
        };
    }

    public static Predicate<Field> hasAcceptableGetter(PropertyType type){
        return Predicates.compose(not(isNull()), getGetterMethod(type));
    }

    public static Predicate<Field> hasAcceptableSetter(PropertyType type){
        return Predicates.compose(not(isNull()), getSetterMethod(type));
    }

    public static Predicate<Field> isASimpleField(){
        return Predicates.and(hasAcceptableGetter(PropertyType.SIMPLE), hasAcceptableSetter(PropertyType.SIMPLE));
    }

    public static Predicate<Field> isAnIndexedField(){
        return Predicates.and(hasAcceptableGetter(PropertyType.INDEXED), hasAcceptableSetter(PropertyType.INDEXED));
    }

    public static Function<Class<?>, Collection<Field>> getFields(){
        return new Function<Class<?>, Collection<Field>>(){

            @Override
            public Collection<Field> apply(Class<?> o) {
                return asList(o.getFields());
            }
        };
    }




    public static <K> Method setterMethod(Field field, PropertyType type){
        try {
            return findMethod(field.getDeclaringClass(), setterMethodName(field, type), type.setterSignature(field.getType()));
        } catch (NoSuchMethodException e) {
        }
        return null;
    }

    public static Method getterMethod(Field field, PropertyType type){
        try {
            return findMethod(field.getDeclaringClass(), getterMethodName(field, type), type.getterSignature(field.getType()));
        } catch (NoSuchMethodException e) {
        }
        return null;
    }

    public static  String setterMethodName(Field field, PropertyType type){
        return camelCaseJoin("set", field.getName());
    }

    public static String getterMethodName(Field field, PropertyType type){
        return camelCaseJoin("get", field.getName());
    }


}

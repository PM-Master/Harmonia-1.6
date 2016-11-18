package gov.nist.csd.pm.common.util.lang;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * Utilities for working with Objects.
 * Primarily adds functionality to work on Objects in conjunction with
 * Google's Guava libs.
 */
public final class Objects {
	private Objects(){
		
	}

    public static Function<Object, String> intoString(){
        return new Function<Object, String>(){

            @Override
            public String apply(@Nullable Object o) {
                return o == null ? null : o.toString();
            }
        };
    }

	public static <T> Function<Object, T> castTo(final Class<T> type){
		return new Function<Object, T>(){

			@Override
			public T apply(Object obj) {
				// TODO Auto-generated method stub
				if(obj != null && type.isAssignableFrom(obj.getClass())){
					return type.cast(obj);
				}
				return null;
			}
			
		};
		
	}



    public static Predicate<Object> equalTo(final Object anObject) {
        return new Predicate<Object>(){

            @Override
            public boolean apply(@Nullable Object o) {
                return anObject == null ? o == null : anObject.equals(o);
            }
        };
    }

    public static boolean areEqualOrNull(Object first, Object second){
        return (first == null && second == null) || (first != null && first.equals(second));
    }
}

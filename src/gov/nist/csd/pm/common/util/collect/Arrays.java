package gov.nist.csd.pm.common.util.collect;

import com.google.common.base.Function;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/22/11
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Arrays {
    public static <T> boolean isNullOrEmpty(T[] array){
        return array == null || array.length == 0;
    }

    public static <T> T getOrElse(T[]array, int index, T orElse){
        if(array != null &&
                (index >= 0 && index < array.length)){
            return array[index];
        }
        return orElse;
    }

    public static <T, U extends T> U[] cast(T[] arr, U[]target){
        checkNotNull(arr); checkNotNull(target);
        checkArgument(target.length >= arr.length, "Insufficient target array size");

        for(int i = 0; i < arr.length; ++i){
            target[i] = (U) arr[i];
        }

        return target;

    }

    public static <T> Function<T[], T> getAtIndex(final int index, final T orElse){
        return new Function<T[], T>() {
            @Override
            public T apply(@Nullable T[] input) {
                return getOrElse(input, index, orElse);
            }
        };
    }
}

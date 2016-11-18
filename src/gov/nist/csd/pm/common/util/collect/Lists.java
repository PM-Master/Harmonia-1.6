package gov.nist.csd.pm.common.util.collect;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/1/11
 * Time: 11:59 AM
 * To change this template use File | Settings | File Templates.
 */
public final class Lists {
    private Lists(){}
    public static <T> T lastElementInList(List<T>list){
        return lastElementInListOr(list,  null);
    }

    public static <T, TT extends T> T lastElementInListOr(List<T>list, TT thisIfNullOrEmpty){
        if(list == null){
            return thisIfNullOrEmpty;
        }
        return list.isEmpty() ? thisIfNullOrEmpty : list.get(list.size()-1);
    }

    public static <T>ImmutableList<T> cons(T val, List<T> list){
        return new ImmutableList.Builder<T>().add(val).addAll(list).build();
    }

    public static <T> ImmutableList<T> append(List<T> list, T... val){
        return append(list, java.util.Arrays.asList(val));
    }

    public static <T> ImmutableList<T> append(List<T> list, Iterable<T> vals){
         return new ImmutableList.Builder<T>().addAll(list).addAll(vals).build();
    }


    public static <T> List<T> interpose(List<T> list, T value){
        List<T> result = new ArrayList<T>(list.size() + list.size() - 1);
        for(T val : list){
            result.add(val);
            result.add(value);
        }
        return result;
    }

    public static <T> List<T> newArrayListInitializedWith(int size, T obj){
        List<T> list = new ArrayList<T>(size);
        for(int i = 0; i < size; ++i){
            list.set(i, obj);
        }
        return list;
    }



}

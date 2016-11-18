package gov.nist.csd.pm.common.util.lang;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 1/23/11
 * Time: 8:16 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Strings {

    private Strings(){}

    public static String camelCaseJoin(String... strings){
        return camelCaseJoin(asList(strings));
    }
    public static String camelCaseJoin(List<String> strings){
        checkNotNull(strings);
        List<String> internalList = newLinkedList(strings);
        String prefix = internalList.remove(0);

        StringBuilder sb = new StringBuilder(prefix);
        for(String string : internalList){
            sb.append(string.substring(0,1).toUpperCase())
              .append(string.substring(1));
        }
        return sb.toString();
    };

    public static String camelCaseChop(String prefix, String target){
        return camelCaseSubstring(target, prefix.length());
    }

    public static String camelCaseSubstring(String target, int start){
        return camelCaseSubstring(target, start, target.length());

    }

    public static String camelCaseSubstring(String target, int start, int until){
        StringBuilder sb = new StringBuilder();
        sb.append(target.substring(start, start + 1).toLowerCase());
        sb.append(target.substring(start + 1, until));
        return sb.toString();
    }

    /**
     * Ensures a string will be namespaced by prepending the given classname
     * Helps with cleanly avoiding key collisions.
     * @param cls
     * @param toNamespace
     * @return
     */
    public static String namespaced(Class<?> cls, String toNamespace){
        return String.format("%s.%s", cls.getCanonicalName(), toNamespace);
    }



    public static Function<String, String> append(final String str){
        return new Function<String, String>(){

            @Override
            public String apply(@Nullable String s) {
                return s + str;
            }
        };
    }

    public static Function<String, String[]> splitOn(final String pattern){
        return new Function<String, String[]>(){

            @Override
            public String[] apply(@Nullable String input) {
                return input != null ? input.split(pattern) : null;
            }
        };
    }

    public static Predicate<String> beingNullOrEmpty(){
        return new Predicate<String>(){

            @Override
            public boolean apply(@Nullable String s) {
                return com.google.common.base.Strings.isNullOrEmpty(s);
            }
        };
    }

    public static Predicate<String> endingWith(final String suffix){
        return new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input != null? input.endsWith(suffix) : false;
            }
        };
    }

      public static Predicate<String> startingWith(final String prefix){
        return new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input != null? input.startsWith(prefix) : false;
            }
        };
    }

    public static String getFileExtensionOfPath(String sIdOrPath) {
        if(sIdOrPath != null && sIdOrPath.contains(".")){
            return sIdOrPath.substring(sIdOrPath.lastIndexOf(".") + 1);
        }
        else{
            return "";
        }
    }
}

package gov.nist.csd.pm.common.application;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.newArrayList;
import static gov.nist.csd.pm.common.util.collect.Arrays.isNullOrEmpty;
import static gov.nist.csd.pm.common.util.collect.Collections.isNullOrEmpty;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/6/11 Time: 3:23 PM To change this template use File | Settings | File Templates.
 */
public final class ArgumentProcessors {
    private ArgumentProcessors() {

    }

    public static int asInt(String arg){
        return asInt(arg, 0);
    }

    public static int asInt(String arg, int alternate){
        if(Strings.isNullOrEmpty(arg)){
            return alternate;
        }
        try{
            return Integer.parseInt(arg);
        }catch(NumberFormatException nfe){
            return alternate;
        }
    }

    public static long   asLong(String arg){
        return asLong(arg, 0);
    }

    public static long asLong(String arg, long alternate){
        if(Strings.isNullOrEmpty(arg)){
            return alternate;
        }
        else{
            try{
                return Long.parseLong(arg);
            }catch(NumberFormatException nfe){
                return alternate;
            }

        }
    }

    public static float asFloat(String arg){
        try{
            return Float.parseFloat(arg);
        }catch(NumberFormatException nfe){
            return 0;
        }    }

    public static double asDouble(String arg){
        try{
            return Double.parseDouble(arg);
        }catch(NumberFormatException nfe){
            return 0;
        }
    }



    public static boolean asBoolean(String arg){
        return !Strings.isNullOrEmpty(arg);
    }

    public static interface ArgumentValue{
        public int toInt();
        public long toLong();
        public float toFloat();
        public double toDouble();
        public boolean toBoolean();
    }

    public static ArgumentProcessor forDirective(String directive){
        return forDirective(directive, 0);
    }

    public static ArgumentProcessor forDirective(String directive, int expectedAdditionalTokens){
        return new DirectiveProcessor(directive, expectedAdditionalTokens);
    }

    public static ArgumentProcessor forArgumentInPosition(int amount){
        return new PositionProcessor(amount);
    }

    private static class PositionProcessor implements ArgumentProcessor{

        private PositionProcessor(int amount){
            this.fromEnd = amount < 0;
            this.amount = fromEnd ? -amount : amount;
        }

        private final int amount;
        private final boolean fromEnd;
        private String value = null;


        @Override
        public boolean matches(String[]args, int position) {
            if(args[position] == null){
                return false;
            }
            else{
                return fromEnd ? (args.length - position) == amount : position == amount;
            }
        }


        @Override
        public void process(String[]args, int position) {
            value = args[position];
            args[position] = null;
        }

        @Override
        public ArgumentValue value() {
            return new StringValue(value);
        }

        @Override
        public boolean processed(){
            return value != null;
        }
    }

    private static class DirectiveProcessor implements ArgumentProcessor{

        private final String directive;
        private final int trailers;
        private List<String> trailingTokens =null;

        private DirectiveProcessor(String directive, int trailers){
            this.directive = directive.toLowerCase();
            this.trailers = trailers;
        }

        @Override
        public boolean matches(String[] arg, int position) {
            return directive.equals(arg[position]);
        }

        @Override
        public void process(String[] args, int position) {
            int remaining = trailers;
            String directive = args[position];
            args[position++] = null;
            trailingTokens = new ArrayList<String>();
            while(remaining-- > 0){
                if(position < args.length){
                    trailingTokens.add(args[position]);
                    args[position++] = null;

                }
                else{
                    throw new RuntimeException("Could not acquire enough args, needed " + remaining + " more.");
                }
            }
        }

        @Override
        public boolean processed() {
            return trailingTokens != null;
        }

        @Override
        public ArgumentValue value() {
            String value = isNullOrEmpty(trailingTokens) ? directive : Joiner.on("").join(trailingTokens);
            return new StringValue(value);
        }
    }

    private static class StringValue implements ArgumentValue{

        private final String value;

        private StringValue(String value){
            this.value = value;
        }

        @Override
        public int toInt() {
            return asInt(value);
        }

        @Override
        public long toLong() {
            return asLong(value);
        }

        @Override
        public float toFloat() {
            return asFloat(value);
        }

        @Override
        public double toDouble() {
            return asDouble(value);
        }

        @Override
        public boolean toBoolean() {
            return asBoolean(value);
        }

        @Override
        public String toString(){
            return value;
        }
    }

    /**
	 * @uml.property  name="nullProcessor"
	 * @uml.associationEnd  
	 */
    public static ArgumentProcessor nullProcessor = new ArgumentProcessor(){

        @Override
        public boolean matches(String[] args, int position) {
            return true;
        }

        @Override
        public void process(String[] args, int position) {

        }

        @Override
        public boolean processed() {
            return false;
        }

        @Override
        public ArgumentValue value() {
            return null;
        }
    };
    public static List<ArgumentProcessor> nullProcessorList = newArrayList(nullProcessor);

    public static void processArguments(String[] inargs, Iterable<ArgumentProcessor> processors){
        String[] args = Arrays.copyOf(inargs, inargs.length);
        if(!isNullOrEmpty(args)){
            Iterable<ArgumentProcessor> allProcessors = concat(processors, nullProcessorList);
            for(int i = 0; i < args.length; ++i){
                Predicate<ArgumentProcessor> matchesAndNotProcessed = and(matchesArgumentPredicate(args, i), not(processedPredicate));

                ArgumentProcessor matchingArg = find(allProcessors, matchesAndNotProcessed);
                matchingArg.process(args, i);
                if(all(processors, processedPredicate)){
                    break;
                }
            }
        }
    }

    private static Predicate<ArgumentProcessor> processedPredicate = new Predicate<ArgumentProcessor>(){

        @Override
        public boolean apply(@Nullable ArgumentProcessor input) {
            return input.processed();
        }
    };

    private static Predicate<ArgumentProcessor> matchesArgumentPredicate(final String[] args, final int pos){
        return new Predicate<ArgumentProcessor>(){
                    @Override
                    public boolean apply(@Nullable ArgumentProcessor input) {
                        return input.matches(args, pos);
                    }
                };
    }


}

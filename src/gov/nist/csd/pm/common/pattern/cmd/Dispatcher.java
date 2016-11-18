package gov.nist.csd.pm.common.pattern.cmd;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/5/11
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Dispatcher<T,R> implements Resolver<T, R>, Dispatch<T, R> {







    /**
	 * @uml.property  name="_delegateResolver"
	 * @uml.associationEnd  
	 */
    private Resolver<T,R> _delegateResolver;
    private Resolver<T,R> getDelegateResolver(){
        return _delegateResolver;
    }




    protected Dispatcher(Resolver<T,R> delegateResolver){

        _delegateResolver = delegateResolver;
    }






    @Override
    public Command<T, R> resolve(T input) {
        return getDelegateResolver().resolve(input);
    }

    public static <T,R> Dispatcher<T,R> create(CommandLookup<T> lookup, Command<T,R> fallbackCommand, Collection<Command<T,R>> commands){
        return new DefaultDispatcher<T, R>(commands, lookup, fallbackCommand);
    }

    public static <T,R> Dispatcher<T,R> create(CommandLookup<T> lookup, Command<T,R>... commands){
        return create(lookup, null ,Arrays.asList(commands));
    }

}

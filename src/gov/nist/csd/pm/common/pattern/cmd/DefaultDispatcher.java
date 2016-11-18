package gov.nist.csd.pm.common.pattern.cmd;


import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/5/11
 * Time: 2:26 PM
 * To change this template use File | Settings | File Templates.
 */
class DefaultDispatcher<T, R> extends Dispatcher<T, R> {
    /**
	 * @uml.property  name="_commands"
	 */
    private final List<Command<T, R>> _commands = new ArrayList<Command<T, R>>();
    /**
	 * @uml.property  name="_defaultCommand"
	 * @uml.associationEnd  
	 */
    private final Command<T, R> _defaultCommand;

    public DefaultDispatcher(Collection<Command<T, R>> commands) {
        this(commands, null, null);
    }

    public DefaultDispatcher(Collection<Command<T, R>> commands, CommandLookup<T> lookup, Command<T, R> defaultCommand) {

        super(new DefaultResolver(commands, lookup));
        _commands.addAll(commands);
        _defaultCommand = defaultCommand;
    }

    @Override
    public R dispatch(T input) {
        return resolve(input).execute(input);
    }

    @Override
    public Command<T, R> resolve(T input) {
        Command<T, R> command = super.resolve(input);
        return command == null ? _defaultCommand : command;
    }


    private static class DefaultLookup<T> implements CommandLookup<T> {


        @Override
        public Object commandKey(Command<T, ?> cmd) {
            return cmd.commandKey();
        }

        @Override
        public Object commandKey(T obj) {
            return obj;
        }
    }


    /**
	 * @author  Administrator
	 */
    private static class DefaultResolver<T, R> implements Resolver<T, R> {
        private final Map<Object, Command<T, R>> _commands;
        /**
		 * @uml.property  name="_lookup"
		 * @uml.associationEnd  
		 */
        private final CommandLookup<T> _lookup;

        public DefaultResolver(Collection<Command<T, R>> commands) {
            this(commands, new DefaultLookup<T>());
        }


        public DefaultResolver(Collection<Command<T, R>> commands, CommandLookup<T> lookup) {
            _commands = new HashMap();
            _lookup = lookup == null ? new DefaultLookup<T>() : lookup;
            for (Command<T, R> cmd : commands) {
                _commands.put(_lookup.commandKey(cmd), cmd);
            }
        }


        @Override
        public Command<T, R> resolve(T input) {
            Object key = _lookup.commandKey(input);

            return _commands.containsKey(key) ? _commands.get(_lookup.commandKey(input)) : null;
        }
    }
}

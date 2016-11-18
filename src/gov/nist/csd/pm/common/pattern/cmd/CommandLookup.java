package gov.nist.csd.pm.common.pattern.cmd;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/5/11
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CommandLookup<T> {
    public Object commandKey(Command<T,?> cmd);
    public Object commandKey(T obj);
}

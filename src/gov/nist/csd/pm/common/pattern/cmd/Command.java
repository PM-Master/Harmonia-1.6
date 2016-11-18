package gov.nist.csd.pm.common.pattern.cmd;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/5/11
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Command<T,R> {

    public Object commandKey();


    /**
     * Implementation of a command
     * @param input
     * @return
     */
    public R execute(T input);
}

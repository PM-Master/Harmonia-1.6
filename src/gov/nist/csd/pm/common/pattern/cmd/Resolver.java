package gov.nist.csd.pm.common.pattern.cmd;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/5/11
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Resolver<T, R> {
    public Command<T,R> resolve(T input);
}

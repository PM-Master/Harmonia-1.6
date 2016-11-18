package gov.nist.csd.pm.common.pattern.cmd;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/5/11
 * Time: 12:57 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Dispatch<T,R> {

    public R dispatch(T input);
}

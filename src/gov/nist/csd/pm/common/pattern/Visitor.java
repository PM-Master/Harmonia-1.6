package gov.nist.csd.pm.common.pattern;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/29/11
 * Time: 12:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Visitor<T> {
    public void visit(T node);
}

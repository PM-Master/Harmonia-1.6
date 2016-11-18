package gov.nist.csd.pm.common.pattern;


import com.google.common.base.Function;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/29/11
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Transformable<F> extends Walkable<F> {
    public <T> Walkable<T> transform(Function<F, T> transformFunction);
}

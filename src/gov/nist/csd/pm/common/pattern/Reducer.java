package gov.nist.csd.pm.common.pattern;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/30/11
 * Time: 9:45 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Reducer<T,R> {
    R reductionStep(T current, Collection<R> neighborResults);
}

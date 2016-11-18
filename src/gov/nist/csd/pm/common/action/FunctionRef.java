package gov.nist.csd.pm.common.action;

import com.google.common.base.Function;
import gov.nist.csd.pm.common.util.Ref;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/11/11
 * Time: 11:38 AM
 * Utilities for swapping out functionality
 */
public interface FunctionRef<T, R> extends Ref<Function<T,R>>, Function<T,R> {
    public Class<? extends T> takes();
    public Class<? super R> gives();
}

package gov.nist.csd.pm.common.model.proto;

import com.google.common.base.Supplier;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/22/11
 * Time: 8:56 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Property<T> extends Supplier<T> {
    String NO_NAME_GIVEN_PLACEHOLDER = "<NO NAME GIVEN>" ;

    public T get();
    public void set(T value);
    public boolean isImmutable();
    public Class<?> type();
    public String name();
}

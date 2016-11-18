package gov.nist.csd.pm.common.model.proto;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/25/11
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class NullProperty<T> implements Property<T> {
    @Override
    public T get() {
        return null;
    }

    @Override
    public void set(T value) {

    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public Class<?> type() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }
}

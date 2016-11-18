package gov.nist.csd.pm.common.model.proto;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/19/11
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImmutableProperty<T> extends MutableProperty<T> {
    public ImmutableProperty(Class<T> type, String name, T defaultValue) {
        super(type, name);
        super.set(defaultValue);
    }


    @Override
    public void set(T value) {
        throw new ImmutablePropertyException(this);
    }

    @Override
    public boolean isImmutable() {
        return true;
    }
}

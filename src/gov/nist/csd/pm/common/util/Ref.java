package gov.nist.csd.pm.common.util;

import com.google.common.base.Supplier;
import gov.nist.csd.pm.common.model.PropertyChangeObservable;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/11/11
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Ref<T> extends Supplier<T>, PropertyChangeObservable {
    public void push(T obj);
    public T pop();
    public void set(T obj);
    public T get();
}

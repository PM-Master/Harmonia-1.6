package gov.nist.csd.pm.common.model.proto;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 8/9/11
 * Time: 6:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class MutableProperty<T> implements Property<T> {

    public static <T> Property<T> forType(Class<T> type, String name){
        return (Property<T>)new MutableProperty(type, name);
    }


    /**
	 * @uml.property  name="value"
	 */
    T value;
    /**
	 * @uml.property  name="type"
	 */
    Class<?> type;
    /**
	 * @uml.property  name="name"
	 */
    String name;
    public MutableProperty(Class<?> type, String name){
        this.type = type;
        this.name = name;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
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

    @Override
    public String toString(){
        return value != null ? value.toString() : "null";
    }
}

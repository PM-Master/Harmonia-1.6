package gov.nist.csd.pm.common.model.proto;

import com.google.common.base.Supplier;

import javax.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/22/11
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class LazyImmutableProperty<T> implements Property<T> {

    /**
	 * @uml.property  name="object"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private final Supplier<T> object;
    /**
	 * @uml.property  name="type"
	 */
    private final Class<?> type;
    /**
	 * @uml.property  name="name"
	 */
    private final String name;


    public LazyImmutableProperty(Supplier<T> object, @Nonnull Class<?> type, @Nonnull String name){
        this.object = object;
        this.type = type;
        this.name = name;
    }


    public LazyImmutableProperty(@Nonnull T object, @Nonnull String name){
        this(new SimpleSupplier(object), object.getClass(), name);
    }

    public LazyImmutableProperty(@Nonnull Class<T> type, @Nonnull String name){
        this(null, type, name);
    }

    @Override
    public T get() {
        return object != null ? object.get() : null;
    }

    @Override
    public void set(T value) {
        throw new ImmutablePropertyException(this);
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public Class<?> type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString(){
        return name();
    }
}

class ImmutablePropertyException extends RuntimeException{
    public ImmutablePropertyException(Property theProperty){
        super(theProperty.toString() + " is an immutable property.  Cannot be changed.");
    }
}

class SimpleSupplier<T> implements Supplier<T>{
    /**
	 * @uml.property  name="object"
	 */
    T object;
    public SimpleSupplier(T object){
        this.object = object;
    }
    @Override
    public T get(){
        return object;
    }
}

package gov.nist.csd.pm.common.model.proto;

import gov.nist.csd.pm.common.application.NullSysCaller;
import gov.nist.csd.pm.common.application.SysCaller;

import static com.google.common.base.Objects.firstNonNull;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/19/11
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */
 public abstract class SysCallerImmutableProperty<T> implements Property<T> {

    /**
	 * @uml.property  name="type"
	 */
    private Class<? extends Object> type;
    /**
	 * @uml.property  name="name"
	 */
    private String name;
    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    private SysCaller sysCaller;

    /**
     * creates a SysCallerImmutableProperty which returns a value based on a value returned from using
     * a sysCaller.  Subclasses are required to implement getWithSysCaller to provide this functionality.
     * @param type
     * @param name
     * @param sysCaller
     */
        public SysCallerImmutableProperty(Class<T> type, String name, SysCaller sysCaller){
            this.type = firstNonNull(type, Object.class);
            this.name = firstNonNull(name, Property.NO_NAME_GIVEN_PLACEHOLDER);
            this.sysCaller = firstNonNull(sysCaller, new NullSysCaller());

        }

        abstract protected T getWithSysCaller(SysCaller sysCaller);

        @Override
        public T get() {

            return getWithSysCaller(sysCaller);
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
    }
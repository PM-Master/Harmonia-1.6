/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.persistence;

import gov.nist.csd.pm.common.application.SysCaller;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Administrator
 */
public class PolicyMachinePersistenceContext implements PersistenceContext<String>  {

    /**
	 * @uml.property  name="_sysCaller"
	 * @uml.associationEnd  
	 */
    private final SysCaller _sysCaller;

    PolicyMachinePersistenceContext(SysCaller sysCaller) {
        _sysCaller = checkNotNull(sysCaller);
    }


    @Override
    public void persistFloat(String key, float value) {
        persistObject(key, new Float(value));

    }

    @Override
    public void persistDouble(String key, double value) {
        persistObject(key, new Double(value));
    }

    @Override
    public void persistLong(String key, long value) {
        persistObject(key, new Long(value));
    }

    @Override
    public void persistInteger(String key, int value) {
        persistObject(key, new Integer(value));
    }

    @Override
    public void persistObject(String key, Object value) {
        key = checkNotNull(key);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float retrieveFloat(String key) {
        return retrieveObjectOfType(key, Float.class).floatValue();
    }

    @Override
    public double retrieveDouble(String key) {
        return retrieveObjectOfType(key, Double.class).doubleValue();
    }

    @Override
    public long retrieveLong(String key) {
        return retrieveObjectOfType(key, Long.class).longValue();
    }

    @Override
    public int retrieveInt(String key) {
        return retrieveObjectOfType(key, Integer.class).intValue();
    }

    @Override
    public Object retrieveObject(String key) {
        checkNotNull(key);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T retrieveObjectOfType(String key, Class<T> type) {
        return type.cast(retrieveObject(key));
    }

}

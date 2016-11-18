/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.persistence;

/**
 *
 * @author Administrator
 * CT - Container Type
 * K - Key Type
 */

public interface PersistenceContext<K> {

    public void persistFloat(K key, float value);
    public void persistDouble(K key, double value);
    public void persistLong (K key, long value);
    public void persistInteger (K key, int value);
    public void persistObject (K key, Object value);
    public float retrieveFloat(K key);
    public double retrieveDouble(K key);
    public long retrieveLong(K key);
    public int retrieveInt(K key);
    public Object retrieveObject(K key);
    public <T> T retrieveObjectOfType(K key, Class<T> type);

}

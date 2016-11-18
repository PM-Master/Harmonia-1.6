/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.persistence;

/**
 *
 * @author Administrator
 */
public interface Persistable<T> {
    /**
     * This is where you save an object to a persistence context
     * @param pc the persistence context to save into
     * @return a reference for retrieving persisted data at a later time.
     */
    public void save(PersistenceContext pc);
    public void load(PersistenceContext pc);
}

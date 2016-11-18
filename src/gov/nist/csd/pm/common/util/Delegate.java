/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.util;

/**
 *
 * @author Administrator
 */
public interface Delegate<T> {
    public void delegate(T delegatingObj);
}

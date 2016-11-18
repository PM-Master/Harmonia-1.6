/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.model;

import java.beans.VetoableChangeListener;

/**
 *
 * @author Administrator
 */
 public interface VetoableChangeObservable {
    public void addVetoableChangeListener(VetoableChangeListener listener);


    public void addVetoableChangeListener(String property, VetoableChangeListener listener);


    public void removeVetoableChangeListener(VetoableChangeListener listner);


    public void removeVetoableChangeListener(String property, VetoableChangeListener listener);
}

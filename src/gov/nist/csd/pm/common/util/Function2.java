/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.util;

/**
 * Implementation of a two parameter function
 * building off of the Google Guava framework.
 *
 * it's noted on the newsgroup that Google doesn't want to
 * go full functional right now, so it's going here.
 *
 * I've also added some converters that will make it easier to use
 * this interface with Guava's other functional utilities
 * @author Administrator
 */
public interface Function2<T1,T2,RT> {

    public abstract RT apply(T1 first, T2 second);


}

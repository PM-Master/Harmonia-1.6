/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.util;

/**
 *
 * This is the definition of a Generator interface.
 *
 * A Generator, as defined within the scope of this library,
 * is an object with a function called "generate."  This function takes in a class
 * and a seed object.  Traditionally it should return an instance of class K with
 * default values in some way derived from the seed.
 *
 * A NOTE ABOUT THE SEED VALUES.  It is important to remember that if two seeds are of
 * equal value i.e. seed1.equals(seed1) then the objects generated from those seeds
 * should also be equal.  Conversely, objects created with two different seeds
 * should NEVER be equal.  This is by design.
 *
 * @author Administrator
 */
public interface Generator<K extends Class<?>, S, R> {
    public R generate(K klass, S seed);
}

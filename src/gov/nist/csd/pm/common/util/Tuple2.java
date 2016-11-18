/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.util;

/**
 *
 * @author Administrator
 */

public class Tuple2<T0, T1> {
    /**
	 * @uml.property  name="_first"
	 */
    private T0 _first;
    /**
	 * @uml.property  name="_second"
	 */
    private T1 _second;
    public Tuple2(T0 first, T1 second){
        _first = first;
        _second = second;
    }

    public T0 getFirst(){
        return _first;
    }
    public T1 getSecond(){
        return _second;
    }
}

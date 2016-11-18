package gov.nist.csd.pm.common.model.proto;

import com.google.common.base.Supplier;

/**
* Created by IntelliJ IDEA.
* User: Administrator
* Date: 9/10/11
* Time: 1:23 PM
* To change this template use File | Settings | File Templates.
*/
abstract class DeferredOperation<K,V> implements Operation<K,V>, Supplier<V>, Indexed<K>{
    /**
	 * @uml.property  name="_key"
	 */
    private final K _key;
    /**
	 * @uml.property  name="_value"
	 */
    private final V _value;
    public DeferredOperation(K key, V value){
        _key = key;
        _value = value;
    }

    @Override
    public V get() {
        return _value;
    }

     @Override
     public K key() {
         return _key;
     }
}

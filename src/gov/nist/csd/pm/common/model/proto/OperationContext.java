package gov.nist.csd.pm.common.model.proto;

/**
* Created by IntelliJ IDEA.
* User: Administrator
* Date: 9/10/11
* Time: 1:21 PM
* To change this template use File | Settings | File Templates.
*/
interface OperationContext<K, V>{
    public OperationResult removeFrom(K key, V val);
    public OperationResult addTo(K key, V val);
}

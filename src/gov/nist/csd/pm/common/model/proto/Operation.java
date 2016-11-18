package gov.nist.csd.pm.common.model.proto;

/**
* Created by IntelliJ IDEA.
* User: Administrator
* Date: 9/10/11
* Time: 1:23 PM
* To change this template use File | Settings | File Templates.
*/
public interface Operation<K,V>{
    public String description();
    public OperationResult perform(OperationContext opCtx);

}

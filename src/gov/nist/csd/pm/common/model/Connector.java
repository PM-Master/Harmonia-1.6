package gov.nist.csd.pm.common.model;


import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/20/11
 * Time: 3:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Connector extends BaseObject {
    public List<OperationSet> operationSets();
    public List<Policy> policies();
    public List<ObjectAttribute> objectAttributes();
    public List<UserAttribute> userAttributes();
    public List<User> users();
}

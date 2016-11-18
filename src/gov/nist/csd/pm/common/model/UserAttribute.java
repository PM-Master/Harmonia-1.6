package gov.nist.csd.pm.common.model;


import gov.nist.csd.pm.common.model.proto.Property;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/20/11
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface UserAttribute extends BaseObject {
    Property<Map<String, String>> properties();
    Property<OperationSet> toOpSet();
    Property<Connector> toConnector();
    Property<Policy> toPolicy();
    Property<UserAttribute> fromAttribute();
    Property<UserAttribute> toAttribute();
    Property<User> fromUser();

}

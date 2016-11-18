package gov.nist.csd.pm.common.model;



import gov.nist.csd.pm.common.model.proto.Property;
import gov.nist.csd.pm.common.model.proto.TypeOf;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/20/11
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Policy extends BaseObject {
    @TypeOf(Connector.class)
    public Property<Connector> connector();
    @TypeOf(Map.class)
    public Property<Map<String, String>> properties();
    @TypeOf(ObjectAttribute.class)
    public List<ObjectAttribute> objectAttributes();
    @TypeOf(UserAttribute.class)
    public List<UserAttribute> userAttributes();
}

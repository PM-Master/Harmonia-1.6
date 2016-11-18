package gov.nist.csd.pm.common.model;


import gov.nist.csd.pm.common.model.proto.ObjectProxy;
import gov.nist.csd.pm.common.model.proto.Property;
import gov.nist.csd.pm.common.model.proto.TypeOf;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/20/11 Time: 3:33 PM To change this template use File | Settings | File Templates.
 */
public interface User extends BaseObject {
    @TypeOf(String.class)
    Property<String> fullName();
    @TypeOf(Map.class)
    Property<Map<String, String>> properties();
    @TypeOf(String.class)
    Property<String> password();
    @TypeOf(Connector.class)
    Property<Connector> connector();
    @TypeOf(UserAttribute.class)
    Property<UserAttribute> toUserAttribute();

    /**
	 * @uml.property  name="aNY"
	 * @uml.associationEnd  
	 */
    public static User ANY = ObjectProxy.getProxyFor(User.class, new HashMap<String, Object>(){{put("name", "");}});

}

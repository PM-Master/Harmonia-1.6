package gov.nist.csd.pm.common.model;


import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.model.proto.Property;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/20/11
 * Time: 3:14 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ObjectAttribute extends BaseObject {

    static String TYPE_CODE = PmNodeType.OBJECT_ATTRIBUTE.typeCode();

    public List<ObjectAttribute> getComponents();
    public Property<String> templateId();
    public Property<String> key();
    public Property<Map<String, String>> properties();
    public Property<OperationSet> fromOpSet();
    public Property<Policy> toPolicy();
    public Property<Connector> toConnector();
    public List<ObjectAttribute> fromAttributes();
    public List<ObjectAttribute> toAttributes();
    public Property<BaseObject> associatedObject();
}

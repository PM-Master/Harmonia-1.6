package gov.nist.csd.pm.common.model;


import gov.nist.csd.pm.common.model.proto.Property;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/20/11
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface VirtualObject extends BaseObject {
    Property<String> path();
    Property<String> host();


}

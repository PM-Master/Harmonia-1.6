package gov.nist.csd.pm.common.model;


import gov.nist.csd.pm.common.model.proto.Property;
import gov.nist.csd.pm.common.model.proto.TypeOf;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/20/11
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BaseObject {

    Property<String> pmClass();
    Property<String> description();
    Property<String> name();
    Property<String> id();
    Property<String> otherInfo();
    @TypeOf(Boolean.class)
    Property<Boolean> includesAscendants();
}

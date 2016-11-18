package gov.nist.csd.pm.application.workflow.model;

import gov.nist.csd.pm.common.model.BaseObject;
import gov.nist.csd.pm.common.model.VirtualObject;
import gov.nist.csd.pm.common.model.proto.Property;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/15/11
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Workflow extends BaseObject {
    public Collection<VirtualObject> children();
    public Collection<VirtualObject> attachments();
    public Collection<VirtualObject> signatureObjects();
    public Collection<VirtualObject> rejectObjects();
    public Property<VirtualObject> form();

}

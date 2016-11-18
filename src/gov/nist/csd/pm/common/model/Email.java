package gov.nist.csd.pm.common.model;


import gov.nist.csd.pm.common.model.proto.Property;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/21/11
 * Time: 2:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Email extends VirtualObject {
    Property<String> internalMailStamp();
    Property<String> externalMailStamp();
    Property<String> emailAttached();
    Property<String> emailSubject();
    Property<String> emailSender();
    Property<List<String>> emailRecipients();

}

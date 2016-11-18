package gov.nist.csd.pm.common.util;

import gov.nist.csd.pm.common.pattern.Emits;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 8/18/11
* Time: 11:47 AM
* To change this template use File | Settings | File Templates.
*/
public interface EVERTransition extends Emits<String> {
    public String emit();
}

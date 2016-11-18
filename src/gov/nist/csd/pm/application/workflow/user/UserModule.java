package gov.nist.csd.pm.application.workflow.user;

import com.google.inject.AbstractModule;
import gov.nist.csd.pm.common.action.PMKit;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/27/11
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PMKit.class).to(UserKit.class);
    }
}

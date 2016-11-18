package gov.nist.csd.pm.application.workflow.creator;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import gov.nist.csd.pm.common.action.PMKit;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/27/11
 * Time: 12:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreatorModule extends AbstractModule {
    @Override
    protected void configure() {
         bind(Action.class).annotatedWith(Names.named(CreatorKit.SUBMIT_WORKFLOW_ACTION_ID)).to(SubmitWorkflowAction.class);
         bind(PMKit.class).to(CreatorKit.class);
    }
}

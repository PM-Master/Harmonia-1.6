package gov.nist.csd.pm.application.workflow.user;

import com.google.common.base.Function;
import com.google.common.io.ByteStreams;
import gov.nist.csd.pm.application.workflow.WorkflowKit;
import gov.nist.csd.pm.application.workflow.model.Workflow;
import gov.nist.csd.pm.application.workflow.model.Workflows;
import gov.nist.csd.pm.common.action.ActionPublisher;
import gov.nist.csd.pm.common.action.ActionRef;
import gov.nist.csd.pm.common.action.FunctionRef;
import gov.nist.csd.pm.common.action.FunctionalityCenter;
import gov.nist.csd.pm.common.application.PMIOObject;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.model.User;
import gov.nist.csd.pm.common.model.VirtualObject;
import gov.nist.csd.pm.common.pdf.PDFFunctionalityContexts;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Predicates.*;
import static com.google.common.collect.Iterables.find;
import static gov.nist.csd.pm.common.action.FunctionRefs.givingOutputCompatibleWithType;
import static gov.nist.csd.pm.common.action.FunctionRefs.takingInputCompatibleWithType;
import static gov.nist.csd.pm.common.model.VirtualObjects.getPath;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.*;


/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/7/11
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserKit extends WorkflowKit{

    public static final String USER_SIGN_ACTION_ID = "SignWorkflowAction";
    public static final String USER_REJECT_ACTION_ID = "RejectWorkflowAction";
    public static final String SIGN_LABEL = "SIGN HERE!";
    public static final String SIGNED_LABEL = "THANK YOU!";
    public static final String REJECT_LABEL = "REJECT!";
    public static final String REJECTED_LABEL = "REJECTED!";



    /**
	 * @uml.property  name="_sysCaller"
	 * @uml.associationEnd  
	 */
    @Inject
    SysCaller _sysCaller;

    /**
	 * @uml.property  name="_currentUser"
	 * @uml.associationEnd  readOnly="true" multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.model.User"
	 */
    @Inject
    Provider<User> _currentUser;

    @Inject
    UserKit(FunctionalityCenter fc){
        fc.addListener(new FunctionalityCenter.FunctionalityListener() {
            @Override
            public void functionalityAdded(FunctionRef function) {
                function.push(new UserSignFieldInjectFunction());
            }

            @Override
            public void functionalityRemoved(FunctionRef function) {

            }
        },
        and(
                takingInputCompatibleWithType(PDSignatureField.class),
                givingOutputCompatibleWithType(Component.class)),
        PDFFunctionalityContexts.PDF_ACROFORM_UI_HOOKING_CONTEXT);
    }


    //Action classes
    private class UserSignFieldInjectFunction implements Function<PDSignatureField, JComponent>{

        @Override
        public JComponent apply(@Nullable PDSignatureField pdSignature) {
            if(pdSignature == null) return jLabel("NULL Signature");
            final COSDictionary dict = pdSignature.getDictionary();
            final String signatureAssignedTo = firstNonNull(dict.getString(PDF_SIGNATURE_ASSIGNMENT_KEY), "");
            JPanel panel = new JPanel();
            return build(panel,
                    withLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS)),
                    addComponent(jLabel(signatureAssignedTo)),
                    addComponent(Box.createHorizontalStrut(5)),
                    addComponent(
                            jButton(
                                    new AbstractAction(){
                                        boolean signed = false;
                                        {
                                            putValue(Action.NAME, SIGN_LABEL);
                                            setEnabled(signatureAssignedTo.equals(_currentUser.get().name()));
                                        }
                                        @Override
                                        public void actionPerformed(ActionEvent actionEvent) {
                                            signed = !signed;
                                            String actionName = signed ? SIGNED_LABEL : SIGN_LABEL;
                                            String referenceKey = signed ? signatureAssignedTo : null;
                                            dict.setString(PDF_SIGNATURE_REFERENCE_KEY, referenceKey);
                                            if(signed){
                                               sign();
                                            }
                                            putValue(Action.NAME, actionName);
                                        }
                                    })),
                    addComponent(Box.createHorizontalStrut(5)),
                    addComponent(
                            jButton(
                                    new AbstractAction() {
                                        boolean rejected = false;

                                        {
                                            putValue(Action.NAME, REJECT_LABEL);
                                            setEnabled(signatureAssignedTo.equals(_currentUser.get().name()));
                                        }

                                        @Override
                                        public void actionPerformed(ActionEvent actionEvent) {
                                            rejected = !rejected;
                                            String actionName = rejected ? REJECTED_LABEL : REJECT_LABEL;
                                            String referenceKey = rejected ? signatureAssignedTo : null;
                                            dict.setString(PDF_REJECTION_REFERENCE_KEY, referenceKey);
                                            if(rejected){
                                                reject();
                                            }
                                            putValue(Action.NAME, actionName);
                                        }
                                    }
                            )));
        }
    }

    public void reject() {
        if(currentWorkflow != null && _currentUser.get() != null){

            //get reject file
            VirtualObject rejectObject = find(currentWorkflow.rejectObjects(), compose(containsPattern(_currentUser.get().name().get()), getPath()));
            //save form
            saveObject(currentWorkflow.name().get());
            //overwrite reject file
            PMIOObject pmio = _sysCaller.openObject4(rejectObject.name().get(), "Read file, Write file");
            try {
                ByteStreams.copy(pmio.getInputStream(), pmio.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
    }

    public void sign() {
        if(currentWorkflow != null && _currentUser.get() != null){
            //get sign file
            VirtualObject signatureObject = find(currentWorkflow.signatureObjects(), compose(containsPattern(_currentUser.get().name().get()), getPath()));

            //save form
            saveObject(currentWorkflow.name().get());
            //overwrite sign file
            PMIOObject pmio  =_sysCaller.openObject4(signatureObject.name().get(), "Read file, Write file");
            try {
                ByteStreams.copy(pmio.getInputStream(), pmio.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    @Override
    public void startup(ActionPublisher host) {
        super.startup(host);
    }

    /**
	 * @uml.property  name="currentWorkflow"
	 * @uml.associationEnd  
	 */
    Workflow currentWorkflow = null;

    @Override
    public void saveObject(String nodeName) {
        if(currentWorkflow != null){
            String formNodeName = currentWorkflow.form().get().name().get();
            super.saveObject(formNodeName);
        }

    }



    @Override
    public void openObject(String nodeName) {
        Workflow flow = Workflows.open(nodeName, sysCaller);
        String formNodeName = flow.form().get().name().get();
        super.openObject(formNodeName);
    }


    @Override
    public void registered(ActionPublisher host) {
        super.registered(host);
    }

    @Override
    public void actionAdded(ActionPublisher host, ActionRef action) {

    }

    @Override
    public void actionRemoved(ActionPublisher host, ActionRef action) {

    }

    @Override
    public void shutdown(ActionPublisher host) {

    }

}

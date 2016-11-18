package gov.nist.csd.pm.application.workflow.creator;

import com.google.common.base.Joiner;
import gov.nist.csd.pm.application.workflow.WorkflowLayout;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.io.DocumentSerializer;
import gov.nist.csd.pm.common.model.User;
import gov.nist.csd.pm.common.util.Conversions;
import gov.nist.csd.pm.common.util.Delegate;
import gov.nist.csd.pm.common.util.swing.DialogUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Strings.isNullOrEmpty;
import static gov.nist.csd.pm.common.io.DocumentSerializers.serializerFor;
import static gov.nist.csd.pm.common.util.lang.Strings.beingNullOrEmpty;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 8/9/11
 * Time: 5:02 PM
 * To change this template use File | Settings | File Templates.
 */
@Singleton
@Named(CreatorKit.SUBMIT_WORKFLOW_ACTION_ID)
public class SubmitWorkflowAction extends AbstractAction {

    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    @Inject
    SysCaller sysCaller;

    public static final String WORKFLOW_ACTION_NAME = "Submit Workflow";


    /**
	 * @uml.property  name="documentProvider"
	 * @uml.associationEnd  readOnly="true" multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.io.DocumentSerializer"
	 */
    @Inject
    Provider<DocumentSerializer> documentProvider;




    /**
	 * @uml.property  name="signers"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.model.User"
	 */
    Provider<List<User>> signers;

    @Inject
    public SubmitWorkflowAction(@SignerList Provider<List<User>> signers) {
        super(WORKFLOW_ACTION_NAME);
        this.signers = signers;
    }

    public void requestDocumentName(Component sender) {

    }

    private static final String FLOW = "FLOW";
    private static final String REJECT = "REJECT";
    private static final String SIGN = "SIGN";
    private static final String FORM = "FORM";

    interface PmNodeLayout {

    }

    private class SubmitActionDelegate implements Delegate<String> {


        private String signNodeNameForUser(String docName, User user){
            return Joiner.on("_").join(docName, FLOW, user.name().get(), SIGN);
        }

        @Override
        public void delegate(final String documentName) {
            documentProvider.get().setDocumentName(Joiner.on("_").join(documentName, FLOW, FORM));
            //Indexes for the string arrays returned through the syscaller interface is inconsistent.
            //We should expose functionality through consistent interfaces and not primitive array types.

            //Nodes of interest
            final PmNode masterAttribute = PmNode.createObjectAttributeNode(Joiner.on("_").join(documentName, FLOW));
            String [] rejectBinStrs = sysCaller.getInfoOfEntityWithPropertyAndType("homeof=Rejected Forms", PmNodeType.CONTAINER.typeCode());
            String [] endNodeStrs = sysCaller.getInfoOfEntityWithPropertyAndType("homeof=Completed Forms", PmNodeType.CONTAINER.typeCode());
            PmNode rejectBin = PmNode.getEntityWithPropTransformFunction.apply(rejectBinStrs);
            PmNode endNodeInfo = PmNode.getEntityWithPropTransformFunction.apply(endNodeStrs);


            //Needs to have reject functionality built into the workflow for signers.
            //Needs reject terminal object attribute.
            String[] startNodeStrs = sysCaller.getInfoOfEntityWithPropertyAndType("homeof=Populated Forms", PmNodeType.CONTAINER.typeCode());
            final PmNode startNodeInfo = PmNode.getEntityWithPropTransformFunction.apply(startNodeStrs);

            WorkflowLayout.Builder workflowBuild = new WorkflowLayout.Builder()
                    .named(masterAttribute.getName())
                    .withParent(startNodeInfo);




            //Build intra-user transitions triggered by signing
            User previous = null;
            List<User> signerList = signers.get();

            for (User user : signerList) {

                PmNode currentHome = getUsersWorkflowHome(user);
                if(previous != null){
                    PmNode previousHome = getUsersWorkflowHome(previous);
                    String previousSignNodeName = signNodeNameForUser(documentName, previous);
                    PmNode signNodeInfo = PmNode.createObjectNode(previousSignNodeName);
                    workflowBuild.withTransition()
                            .from(previousHome)
                            .to(currentHome)
                            .triggeredOn(signNodeInfo)
                            .triggeredBy(previous);
                }
                workflowBuild.withChildFile( serializerFor(Conversions.hexString2ByteArray("DEADBEEF"), signNodeNameForUser(documentName, user), "sign"));

                //Add reject object to workflow
                String rejectNodeName = Joiner.on("_").join(documentName, FLOW, user.name(), REJECT);
                workflowBuild.withChildFile(serializerFor(Conversions.hexString2ByteArray("BADDFEED"),rejectNodeName, "reject"));

                //Add reject transition to object
                workflowBuild.withTransition()
                        .triggeredBy(user)
                        .from(currentHome)
                        .to(rejectBin)
                        .triggeredOn(PmNode.createObjectNode(rejectNodeName));

                previous = user;

            }
            if(signerList.size() > 0){
                //Build initial transition triggered by writing to the prepared forms folder
                PmNode firstUserWorkflowHome = getUsersWorkflowHome(signerList.get(0));
                workflowBuild.withTransition()
                        .from(startNodeInfo)
                        .to(firstUserWorkflowHome)
                        .triggeredOn(masterAttribute);

                //Build final transition triggered by last user signature
                User last = signerList.get(signerList.size() - 1);
                PmNode lastUserWorkflowHome = getUsersWorkflowHome(last);
                PmNode lastUserSignNode = PmNode.createObjectNode(signNodeNameForUser(documentName, last));
                workflowBuild.withTransition()
                        .from(lastUserWorkflowHome)
                        .to(endNodeInfo)
                        .triggeredOn(lastUserSignNode)
                        .triggeredBy(last);


                workflowBuild.withChildFile(documentProvider.get());
            }
            //Create script

            WorkflowLayout layout = workflowBuild.getLayout();


            //Create entity in policy machine.
            layout.create(sysCaller);





            //done?
        }

        private PmNode getUsersWorkflowHome(User previous) {
            String userflowContainer = String.format("witemsof=%s", previous.name().get());
            String[] entityInfo = sysCaller.getInfoOfEntityWithPropertyAndType(userflowContainer, PmNodeType.CONTAINER.typeCode());

            return PmNode.getEntityWithPropTransformFunction.apply(entityInfo);
        }

    }

    //Workflow
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        //Get signatures that apply
        if (documentProvider.get() != null) {
            String docName = documentProvider.get().getDocumentName();

            Delegate<String> actionDelegate = new SubmitActionDelegate();
            if (isNullOrEmpty(docName)) {
                DialogUtils.DialogBuilder db = DialogUtils.buildDialog()
                        .info()
                        .withMessage("Please enter a name for your document")
                        .withTitle("Empty document name!");
                db.presentForInput(not(beingNullOrEmpty()), actionDelegate);
            } else {
                actionDelegate.delegate(docName);
            }


        }

    }
}

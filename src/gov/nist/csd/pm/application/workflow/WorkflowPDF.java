package gov.nist.csd.pm.application.workflow;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.inject.*;
import gov.nist.csd.pm.application.pdfviewer.PDFViewer;
import gov.nist.csd.pm.application.workflow.creator.CreatorModule;
import gov.nist.csd.pm.application.workflow.creator.SignerList;
import gov.nist.csd.pm.application.workflow.user.UserModule;
import gov.nist.csd.pm.common.action.ActionGroup;
import gov.nist.csd.pm.common.action.FunctionalityCenter;
import gov.nist.csd.pm.common.action.PMKit;
import gov.nist.csd.pm.common.application.ArgumentProcessor;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.info.Args;
import gov.nist.csd.pm.common.io.DocumentSerializer;
import gov.nist.csd.pm.common.model.User;
import gov.nist.csd.pm.common.model.proto.ObjectProxy;
import gov.nist.csd.pm.common.pdf.support.PDFields;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static gov.nist.csd.pm.application.workflow.model.Workflows.isWorkflow;
import static gov.nist.csd.pm.common.application.ArgumentProcessors.*;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.displayInJFrame;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.setApplicationLookAndFeel;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/7/11
 * Time: 2:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowPDF {

    /**
	 * @uml.property  name="_sysCaller"
	 * @uml.associationEnd  
	 */
    private final SysCaller _sysCaller;
    /**
	 * @uml.property  name="_viewer"
	 * @uml.associationEnd  
	 */
    private final PDFViewer _viewer;

    /**
	 * @uml.property  name="kit"
	 * @uml.associationEnd  
	 */
    @Inject
    PMKit kit;


    private WorkflowPDF(SysCaller sysCaller){
        this(sysCaller, null);
    }

    private WorkflowPDF(SysCaller sysCaller, String initialObjectName) {
        setApplicationLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        _sysCaller = sysCaller;
        _viewer = new PDFViewer();
        /**
         * Serban - the initializeEnvironment needs to
         * setup the Workflow application in Creator or User/Signer
         * mode based on what initialFileName points to.
         *
         * If it's an object representing a pdf file, then setup as Creator
         * If it's an instantiated Workflow, then setup as User.
         */
        initializeEnvironment(_sysCaller, initialObjectName);
        _viewer.registerSubscriber(kit);
        kit.openObject(initialObjectName);

    }

    /**
     * This is a provider for a list of signers, it's injected into the components that
     * need it by Guice.  At this time it's only used in Creator mode.
     */
    @SignerList
    static class SignerListProvider implements Provider<List<User>>{

        Provider<PDDocument> documentProvider;

        @Inject
        public SignerListProvider(Provider<PDDocument> documentProvider){
            this.documentProvider = documentProvider;
        }

         private final Function<String, User> stringToUser = new Function<String, User>(){

            @Override
            public User apply(@Nullable String s) {
                User user = ObjectProxy.getProxyFor(User.class);
                user.name().set(s);
                return user;
            }
        };
        @Provides @SignerList
        public List<User> getSignerList() {
            Iterable<PDField> specifiedSignatureFields =
                    Iterables.filter(
                            PDFields.fromDocument(documentProvider.get()),  //Document Fields
                            PDFields.withDictionaryKey(WorkflowKit.PDF_SIGNATURE_ASSIGNMENT_KEY)); //having signature assignment key
            Iterable<User> users = Iterables.transform(
                    specifiedSignatureFields,
                    Functions.compose(
                        stringToUser,
                        PDFields.extractDictionaryString(WorkflowKit.PDF_SIGNATURE_ASSIGNMENT_KEY)));
            return newArrayList(users);

        }

        @Override
        public List<User> get() {
            return getSignerList();
        }
    }


    /**
     * The role of this function is to determine, based on initFileName which dependency
     * injection module to supply.  Dependency injection modules are used to decouple
     * parts of this application.  There is a module each which configures the applications
     * dependencies for Creator or User Mode.  They are called CreatorModule and UserModule
     * respectively.
     *
     * Current status:  The control logic is in place but is incomplete.  Specifically,
     * the function isWorkflow() is untested.  It's should test a workflow object to see if it's
     * 1.  An Object Attribute
     * 2.  Contains an object representing a workflow .config file
     * 3.  That this .config file, a java properties file, contains a key/value pair of "type=Workflow"
     * That is not so say that it currently does all of this, just that it should.
     *
     * Another thing, if initialObjectName is an ObjectAttribute the calls to getIdOfEntityWithNameAndType both return
     * null.  I'm not sure why but this is unexpected.
     * @param sysCaller
     * @param initialObjectName
     * @return
     */
    private Module modeModuleForDocument(SysCaller sysCaller, String initialObjectName){
        if(isNullOrEmpty(initialObjectName)){
            return new CreatorModule();
        }
        String objectAttributeId = sysCaller.getIdOfEntityWithNameAndType(initialObjectName, PmNodeType.OBJECT_ATTRIBUTE.typeCode());
        String objectId = sysCaller.getIdOfEntityWithNameAndType(initialObjectName, PmNodeType.OBJECT.typeCode());
        String id = Objects.firstNonNull(objectAttributeId, objectId);
        Module returnMod = new CreatorModule();
        if(id.equals(objectAttributeId) && isWorkflow(sysCaller, id)){
            returnMod = new UserModule();
        }

        return returnMod;
    }

    /**
     * Initializes the application's dependencies.
     * Utilizes the Google Guice library.
     * @param sysCaller
     * @param initialObjectName
     */
    private void initializeEnvironment(final SysCaller sysCaller, String initialObjectName){

        Module modeModule = modeModuleForDocument(sysCaller, initialObjectName);

        Module workflowModule = new AbstractModule(){

            @Override
            protected void configure() {
                bind(SysCaller.class).toInstance(sysCaller);
                bind(ActionGroup.class).toInstance(_viewer.getActionGroup());
                bind(FunctionalityCenter.class).toInstance(FunctionalityCenter.INSTANCE);
                bind(DocumentSerializer.class).toInstance(_viewer);
                bind(User.class).toProvider(new Provider<User>(){
                    @Override
                    public User get() {
                        User user = ObjectProxy.getProxyFor(User.class);
                        user.name().set(_sysCaller.getSessionUser());
                        return user;
                    }
                });
                bind(PDDocument.class).toProvider(new Provider<PDDocument>(){

                    @Override
                    public PDDocument get() {

                        return _viewer.getDocument();
                    }
                });
                bind(new TypeLiteral<List<User>>(){}).annotatedWith(SignerList.class).toProvider(SignerListProvider.class);

            }
        };
        Injector injector = Guice.createInjector(workflowModule).createChildInjector(modeModule);
        injector.injectMembers(this);


    }




    public void showUI(){

        JMenuBar newMenuBar = _viewer.newBuildMenuBar();
        JFrame frame = displayInJFrame(_viewer, "PDFView", newMenuBar);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static final String WORKFLOW_PREFIX = "Workflow PDF";

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){

                ArgumentProcessor simPort = forDirective(Args.SIM_PORT_ARG, 1);
                ArgumentProcessor sessionId = forDirective(Args.SESSION_ARG,1);
                ArgumentProcessor procId = forDirective(Args.PROCESS_ARG, 1);
                ArgumentProcessor debug = forDirective(Args.DEBUG_ARG);
                ArgumentProcessor initialFile = forArgumentInPosition(-1);
                processArguments(args, newArrayList(simPort, sessionId, procId, debug, initialFile));
                SysCaller sysCaller = new SysCallerImpl(simPort.value().toInt(), sessionId.value().toString(), procId.value().toString(), debug.value().toBoolean(), WORKFLOW_PREFIX);

                WorkflowPDF workflowPDF = new WorkflowPDF(sysCaller,initialFile.value().toString());
                workflowPDF.showUI();
            }
        });

        //viewer.setDocument(new File(System.getProperty("user.dir") + "/test-src/resources/n114po.pdf"));
    }




}

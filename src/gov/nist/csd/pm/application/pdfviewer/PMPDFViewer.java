package gov.nist.csd.pm.application.pdfviewer;

import com.google.inject.*;
import gov.nist.csd.pm.common.action.ActionGroup;
import gov.nist.csd.pm.common.action.FunctionalityCenter;
import gov.nist.csd.pm.common.action.PMKit;
import gov.nist.csd.pm.common.application.ArgumentProcessor;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.info.Args;
import gov.nist.csd.pm.common.io.DocumentSerializer;
import gov.nist.csd.pm.common.model.User;
import gov.nist.csd.pm.common.model.proto.ObjectProxy;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.swing.*;

import static com.google.common.collect.Lists.newArrayList;
import static gov.nist.csd.pm.common.application.ArgumentProcessors.*;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.displayInJFrame;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/23/11
 * Time: 6:02 PM
 * This class combines the PDFViewer, which operates on the files system, with a PMKit, which provides
 * data hooks into common policy machine actions.
 */

public class PMPDFViewer {


    /**
	 * @uml.property  name="pmKit"
	 * @uml.associationEnd  
	 */
    final PMKit pmKit = new PMKit();
    /**
	 * @uml.property  name="_pdfViewer"
	 * @uml.associationEnd  
	 */
    @Inject
    PDFViewer _pdfViewer;

    /**
	 * @uml.property  name="_sysCaller"
	 * @uml.associationEnd  
	 */
    @Inject
    SysCaller _sysCaller;

    private static final String PM_PDF_VIEWER_PREFIX = "PM PDF View";

    public static void main(String[] args){
        ArgumentProcessor simPort = forDirective(Args.SIM_PORT_ARG, 1);
        ArgumentProcessor sessionId = forDirective(Args.SESSION_ARG,1);
        ArgumentProcessor procId = forDirective(Args.PROCESS_ARG, 1);
        ArgumentProcessor debug = forDirective(Args.DEBUG_ARG);
        ArgumentProcessor initialObjectName = forArgumentInPosition(-1);
        processArguments(args, newArrayList(simPort, sessionId, procId, debug, initialObjectName));
        SysCaller sysCaller = new SysCallerImpl(simPort.value().toInt(), sessionId.value().toString(), procId.value().toString(), debug.value().toBoolean(), PM_PDF_VIEWER_PREFIX);

        new PMPDFViewer().initialize(sysCaller);
    }

    private void showUI(PDFViewer _viewer){

        JMenuBar newMenuBar = _viewer.newBuildMenuBar();
        JFrame frame = displayInJFrame(_viewer, "PM PDFView", newMenuBar);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initialize(SysCaller sysCaller){
        setupDependencies(sysCaller);
        showUI(_pdfViewer);
    }

    /**
     * Lays out this application's dependencies and injects them into the current class.
     *
     * Serban, The implementation of this is incomplete and untested.  Please use the working
     * implementation of WorkflowPDF as a guide if any of this setup is incorrect.
     */
    private void setupDependencies(final SysCaller sysCaller){
        Module workflowModule = new AbstractModule(){

            @Override
            protected void configure() {
                //Need to figure out how to configure this SysCallerImpl
                bind(PDFViewer.class).toInstance(new PDFViewer());
                bind(SysCaller.class).toInstance(sysCaller);
                bind(ActionGroup.class).toProvider(
                        new Provider<ActionGroup>() {

                            @Override
                            public ActionGroup get() {
                                return _pdfViewer.getActionGroup();
                            }
                        }
                );
                bind(FunctionalityCenter.class).toInstance(FunctionalityCenter.INSTANCE);
                bind(DocumentSerializer.class).toProvider(
                        new Provider<DocumentSerializer>() {

                            @Override
                            public DocumentSerializer get() {
                                return _pdfViewer;
                            }
                        });
                bind(User.class).toProvider(new Provider<User>(){
                    @Inject
                    private SysCaller sc;
                    @Override
                    public User get() {
                        User user = ObjectProxy.getProxyFor(User.class);
                        user.name().set(sc.getSessionUser());
                        return user;
                    }
                });
                bind(PDDocument.class).toProvider(new Provider<PDDocument>(){

                    @Override
                    public PDDocument get() {

                        return _pdfViewer.getDocument();
                    }
                });

            }
        };
        Injector injector = Guice.createInjector(workflowModule);
        injector.injectMembers(this);
    }
}

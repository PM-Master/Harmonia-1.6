package gov.nist.csd.pm.common.action;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import gov.nist.csd.pm.application.pdfviewer.PDFViewerActionLibrary;
import gov.nist.csd.pm.application.workflow.Kit;
import gov.nist.csd.pm.common.application.*;
import gov.nist.csd.pm.common.browser.BrowsingKit;
import gov.nist.csd.pm.common.browser.ObjectBrowserListener;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.info.Permissions;
import gov.nist.csd.pm.common.info.PmClasses;
import gov.nist.csd.pm.common.io.DocumentSerializer;
import gov.nist.csd.pm.common.model.User;
import gov.nist.csd.pm.common.pdf.PDFDocumentActionLibrary;
import gov.nist.csd.pm.common.util.swing.SwingShortcuts;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static gov.nist.csd.pm.common.action.ActionWrapper.createReference;
import static gov.nist.csd.pm.common.util.Functions2.pipe;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.*;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/27/11
 * Time: 4:18 PM
 * Base kit for providing common PolicyMachine functionality in an application.
 */
public class PMKit implements Kit {

    /**
	 * @uml.property  name="currentNode"
	 * @uml.associationEnd  
	 */
    private PmNode currentNode;

    /**
	 * @uml.property  name="applicationActionGroup"
	 * @uml.associationEnd  
	 */
    @Inject
    protected ActionGroup applicationActionGroup;

    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    @Inject
    protected SysCaller sysCaller;

    /**
	 * @uml.property  name="docSerializer"
	 * @uml.associationEnd  
	 */
    @Inject
    protected DocumentSerializer docSerializer;


    /**
	 * @uml.property  name="currentUser"
	 * @uml.associationEnd  
	 */
    @Inject
    protected User currentUser;

    /**
	 * @uml.property  name="notifyWindowSupplier"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Supplier<NotificationLoggingWindow> notifyWindowSupplier = Suppliers.memoize(new Supplier<NotificationLoggingWindow>() {

        @Override
        public NotificationLoggingWindow get() {
            return new NotificationLoggingWindow();
        }
    });





    private static final String TEXT_AREA_NAME = NotificationLoggingWindow.class.getName() + "_TEXT_AREA_NAME";


    private class NotificationLoggingWindow extends JFrame implements NotificationSubscriber {

        private HTMLEditorKit editorKit = new HTMLEditorKit();
        private HTMLDocument notificationDocument = (HTMLDocument)editorKit.createDefaultDocument();
        private Element bodyElement = null;
        public NotificationLoggingWindow() {
            super();
            setupDocument();
            initializeGUI();
            startListening();
        }

        private void startListening(){
            NotificationCenter.INSTANCE.subscribe(this, NotificationType.ALL);
        }

        private void setupDocument(){
            Element html = notificationDocument.getRootElements()[0];
            for(int i = 0; i < html.getElementCount(); ++i){
                Element child = html.getElement(i);
                if(child.getAttributes().getAttribute(StyleConstants.NameAttribute) == HTML.Tag.BODY ){
                    bodyElement = child;
                    break;
                }
            }
        }

        private ActionListener closeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                NotificationLoggingWindow.this.setVisible(false);
            }
        };

        private ActionListener clearAction = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent actionEvent){
                try {
                    notificationDocument.setInnerHTML(bodyElement, "");
                } catch (BadLocationException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        };


        private void initializeGUI() {
            pipe(
                    withLayout(new MigLayout("","[][grow][]", "[][grow][]")),
                    addComponent(jLabel("Messages"), "split 2, span"),
                    addComponent(jSeparator(JSeparator.HORIZONTAL), "growx, wrap"),
                    addBuiltComponent(jTextArea(40, 80), "growx, growy, span, wrap",
                            withName(TEXT_AREA_NAME),
                            withDocument(notificationDocument)),
                    addBuiltComponent(jButton("Clear"), "split2, span, align right",
                            withAction(clearAction)),
                    addBuiltComponent(jButton("Close"), "",
                            withAction(closeAction))
            ).apply(getContentPane());
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        }

        public void appendNotification(Notification note){
            try {
                notificationDocument.insertBeforeEnd(bodyElement, "<p>" + note + "</p>");
            } catch (BadLocationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


        @Override
        public void receivedNotification(Notification n) {
            appendNotification(n);
        }
    }

    @Override
    public void startup(ActionPublisher host) {
        ActionRef saveAction = Actions.findByID(host.publishedActions(), PDFDocumentActionLibrary.SAVE.name());
        ActionRef saveAsAction = Actions.findByID(host.publishedActions(), PDFDocumentActionLibrary.SAVE_AS.name());
        ActionRef openAction = Actions.findByID(host.publishedActions(), PDFDocumentActionLibrary.OPEN.name());
        saveAction.pushAction(new SaveAction());
        saveAsAction.pushAction(BrowsingKit.browseObjectAction(sysCaller, new SaveAsObjectBrowserListener()));
        openAction.pushAction(BrowsingKit.browseObjectAction(sysCaller, new OpenObjectBrowserListener()));
        applicationActionGroup.insertActionAfter(Actions.actionWithId(PDFViewerActionLibrary.ABOUT.name()), loggingAction);

    }

    /**
	 * @uml.property  name="loggingAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private final Action loggingAction = createReference(new AbstractAction("Logs...") {


        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Window win = SwingShortcuts.locateSuitableWindowForActionEvent(actionEvent);
            JFrame frame = notifyWindowSupplier.get();
            frame.setLocationRelativeTo(win);
            frame.pack();
            frame.setVisible(true);
        }
    });

    class SaveAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            NotificationCenter.INSTANCE.postNotification("This action is unimplemented in the policy machine", NotificationType.ERROR);
            if (currentNode == null) {
                BrowsingKit.browseObjectAction(sysCaller, new SaveAsObjectBrowserListener()).actionPerformed(actionEvent);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                docSerializer.saveDocument(baos);
                //Check for file existence
                //If not exists, create
                String handle = sysCaller.openObject3(currentNode.getName(), Permissions.PERMISSION_FILE_READ);

                //Otherwise? open or always open for writing
                //write data
                sysCaller.writeObject3(handle, baos.toByteArray());
                //Profit
            }
        }
    }

    class SaveAsObjectBrowserListener implements ObjectBrowserListener {

        @Override
        public void pmNodeSelected(PmNode node) {
            saveObject(node);
        }
    }

    public void saveObject(String nodeName){
        saveObject(PmNode.createFileNode(nodeName));
    }

    public void saveObject(PmNode node){
          NotificationCenter.INSTANCE.postNotification(
                    String.format(" (%s) selected, with parent (%s)"
                            , node, node.getParent()));
            String handle = sysCaller.createObject3(node.getName(),
                    PmClasses.CLASS_FILE_NAME,
                    docSerializer.getDocumentType(),
                    node.getParent().getName(),
                    Permissions.PERMISSION_FILE_WRITE,
                    null, null, null, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            docSerializer.saveDocument(baos);
            sysCaller.writeObject3(handle, baos.toByteArray());
            currentNode = node;
    }

    class OpenObjectBrowserListener implements ObjectBrowserListener {

        @Override
        public void pmNodeSelected(PmNode node) {
            openObject(node);
        }
    }

    public void openObject(String nodeName){
        openObject(PmNode.createFileNode(nodeName));
    }

    public void openObject(PmNode node){
         NotificationCenter.INSTANCE.postNotification(
                    String.format("Node (name %s, id %s, type %s) selected."
                            , node.getName(), node.getId(), node.getType()));
            PMIOObject pmObjHandle = sysCaller.openObject4(node.getName(), Permissions.PERMISSION_FILE_READ);

            currentNode = node;
            docSerializer.loadDocument(pmObjHandle.getInputStream(), node.getName());
    }

    @Override
    public void registered(ActionPublisher host) {

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

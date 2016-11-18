package gov.nist.csd.pm.application.pdfviewer;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import gov.nist.csd.pm.common.action.*;
import gov.nist.csd.pm.common.application.Notification;
import gov.nist.csd.pm.common.application.NotificationCenter;
import gov.nist.csd.pm.common.application.NotificationSubscriber;
import gov.nist.csd.pm.common.application.NotificationType;
import gov.nist.csd.pm.common.io.DocumentSerializer;
import gov.nist.csd.pm.common.pattern.Reducer;
import gov.nist.csd.pm.common.pdf.PDFFormView;
import gov.nist.csd.pm.common.pdf.PDFFunctionalityContexts;
import gov.nist.csd.pm.common.pdf.PDFViewController;
import gov.nist.csd.pm.common.pdf.support.PageLoadingListener;
import gov.nist.csd.pm.common.util.Percentage;
import gov.nist.csd.pm.common.util.collect.Insert;
import net.miginfocom.swing.MigLayout;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.*;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 2/3/11 Time: 1:56 PM Self-Contained PDFViewer component implemented as a JPanel extension
 */
public class PDFViewer extends JPanel implements PageLoadingListener, ActionPublisher, NotificationSubscriber, DocumentSerializer {



    private static final long serialVersionUID = 1L;
    /**
	 * @uml.property  name="_viewController"
	 * @uml.associationEnd  
	 */
    private PDFViewController _viewController;
    /**
	 * @uml.property  name="_formView"
	 * @uml.associationEnd  
	 */
    private PDFFormView _formView;
    /**
	 * @uml.property  name="name"
	 */
    private String name;
    /**
	 * @uml.property  name="aps"
	 * @uml.associationEnd  
	 */
    ActionPublisherSupport aps = new ActionPublisherSupport(this);


    /**
	 * Converter object for working with percents
	 * @uml.property  name="_numberToPercentConverter"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */

    private Function<Number, Percentage> _numberToPercentConverter = new Function<Number, Percentage>() {

        @Override
        public Percentage apply(Number f) {
            return new Percentage(f.doubleValue());
        }

    };


    private void setTitle(String title) {
        Container cont = this.getTopLevelAncestor();
        if (cont != null) {
            return;
        }
        if (cont instanceof JFrame) {
            ((JFrame) cont).setTitle(title);
        }
    }

    /**
	 * Observer for the PDDocument property change.  Resets the UI when a new PDDcouemnt is loaded
	 * @uml.property  name="_documentFilePropertyChangeListener"
	 */
    private PropertyChangeListener _documentFilePropertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (!pce.getPropertyName().equals(PDFViewController.DOCUMENT_PROPERTY)) {
                return;
            }
            _viewController.getSaveAction().setEnabled(pce.getNewValue() != null);

            _viewController.getSaveAsAction().setEnabled(pce.getNewValue() != null);

            Object newValue = pce.getNewValue();
            if (newValue instanceof PDDocument) {
                PDDocument pddoc = (PDDocument) newValue;
                setTitle(pddoc.getDocumentInformation().getTitle());
            }

        }
    };

    /**
	 * Action for application exit
	 * @uml.property  name="_exitAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Action _exitAction = new AbstractAction("Exit") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
            putValue(Action.SHORT_DESCRIPTION, "Quits the application.");
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            Container topLevelAncestor = PDFViewer.this.getTopLevelAncestor();
            if (topLevelAncestor != null && topLevelAncestor instanceof JFrame) {
                ((JFrame) topLevelAncestor).dispose();
            }
        }
    };
    /**
     * Action for invoking the "about" dialog.
     */
    private static Action _aboutAction = new AbstractAction("About...") {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        {
            putValue(ActionRef.ID, PDFViewerActionLibrary.ABOUT.name());

            //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(Action.SHORT_DESCRIPTION, "Find out more about this application.");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Object source = actionEvent.getSource();
            if (source instanceof JComponent) {
                Container comp = ((JComponent) source).getTopLevelAncestor();
                Window window = comp instanceof Window ? (Window) comp : null;
                new AboutPanel(window).setVisible(true);
            }

        }
    };

    /**
     * Default constructor overrides single argument constrctor to centralize implementation.
     */
    public PDFViewer() {
        this(null);
    }

    public PDFViewer(PDDocument document) {
        if (document == null) {
            try {
                document = new PDDocument();
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
        initializeInterface(document);
    }

    /**
     * initializes the PDFViewer with a PDF form viewer
     * @param document
     */
    private void initializeFormViewer(PDDocument document) {
        _formView = new PDFFormView(null);
        _formView.registerSubscriber(aps);
        _viewController = new PDFViewController(document, _formView);
        _viewController.registerSubscriber(aps);
        _viewController.addPropertyChangeListener(_documentFilePropertyChangeListener);
        _formView.addPageLoadingListener(this);
        NotificationCenter.INSTANCE.subscribe(this, this);
    }


    /**
     * Intializes the user interface.
     * @param document
     */
    private void initializeInterface(PDDocument document) {
        initializeFormViewer(document);
        initialiseScrollView();
        setLayout(new MigLayout("", "[grow]", "[grow][]"));
        List<Percentage> percents = transform(newArrayList(12.5, 25.0, 50.0, 75.0, 100.0, 125.0, 150.0, 200.0, 300.0, 500.0, 1000.0), _numberToPercentConverter);
        build(
                this,
                addComponent(mainScrollView, "span, grow"),
                addComponent(jLabel("Zoom Level"), "newline, label, grow 0, split 6, span"),
                addComponent(
                        build(jComboBox(),
                                withOptions(percents),
                                withDefaultOption(new Percentage(100.0)),
                                withItemListener(_viewController.getZoomLevelItemListener()),
                                withName("zoomSelector")), ""), //Combo box constraints
                addComponent(jButton(_viewController.getPrevPageAction()), ""), //Back button constraints
                addComponent(jButton(_viewController.getNextPageAction()), "grow 0"), //Next button constraints
                addComponent(
                        build(new JProgressBar(), withName("progress")), "grow 0"),
                addComponent(build(jLabel(""), withName("status")), "grow 0"));

    }

    /**
	 * Function Hook for PDFFormView This gives applications that utilize PDF viewer a chance to override the functionality of the default PDFFormView
	 * @uml.property  name="_pdfFormViewHook"
	 * @uml.associationEnd  
	 */
    static private FunctionRef<PDFFormView, JComponent> _pdfFormViewHook;
    /**
	 * @uml.property  name="mainScrollView"
	 * @uml.associationEnd  
	 */
    private JScrollPane mainScrollView;
    static {
        FunctionalityCenter fc = FunctionalityCenter.INSTANCE;
        /**
         * Default pdf form hook does nothing
         */
        fc.addFunctionality(new Function<PDFFormView, JComponent>(){

            @Override
            public JComponent apply(@Nullable PDFFormView pdfFormView) {
                return pdfFormView;
            }
        }, PDFFormView.class, JComponent.class, PDFFunctionalityContexts.PDF_FORM_VIEW_HOOKING_CONTEXT);
        /*
         * Retrieves the pdf form view hook after installation.
         */
        _pdfFormViewHook =
                FunctionalityCenter.INSTANCE.getFunctionRef(
                        PDFFormView.class,
                        JComponent.class,
                        PDFFunctionalityContexts.PDF_FORM_VIEW_HOOKING_CONTEXT);

    }
    private void initialiseScrollView() {
        /**
         * This change listener monitors the pdfFormViewHook.  If a change is made to the
         * hook (e.g. if an extension is pushed), then the hook is reapplied.
         */
        _pdfFormViewHook.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                mainScrollView.setViewportView(_pdfFormViewHook.apply(_formView));
                mainScrollView.invalidate();
            }
        });
        /**
         * Setup of the scroll view and application of settings more user friendly than the default..
         */
        mainScrollView = new JScrollPane(_pdfFormViewHook.apply(_formView));


        mainScrollView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainScrollView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mainScrollView.setPreferredSize(new Dimension(600, 400));
        mainScrollView.getVerticalScrollBar().setUnitIncrement(10);
        mainScrollView.getVerticalScrollBar().setBlockIncrement(10);
        mainScrollView.getHorizontalScrollBar().setUnitIncrement(10);
        mainScrollView.getHorizontalScrollBar().setBlockIncrement(10);
    }


    /**
     * Allows loading a document from a file reference.  documentFile should represent a pdf document.
     * @param documentFile
     */
    public void loadDocument(File documentFile) {
        try {
            loadDocument(new FileInputStream(documentFile), documentFile.getName());
        } catch (FileNotFoundException e) {
            NotificationCenter.INSTANCE.postNotification(this.getTopLevelAncestor(), e.getLocalizedMessage(), NotificationType.ERROR);
        }
    }

    /**
     * Allows loading a docment from an input stream.
     * @param documentInputStream   The open input stream for a pdf document
     * @param name                  The name of that pdf document.
     */
    @Override
    public void loadDocument(InputStream documentInputStream, String name) {
        if (documentInputStream != null) {
            try {
                _viewController.setDocument(PDDocument.load(documentInputStream));
            } catch (IOException e) {
                NotificationCenter.INSTANCE.postNotification( this.getTopLevelAncestor(),e.getLocalizedMessage(), NotificationType.ERROR);
            }
        }
    }

    /**
     *
     * @return the currently loaded PDDocument
     */
    public PDDocument getDocument(){
        return _viewController.getDocument();
    }

    /**
     *
     * @return the document type for this document.  Currently only pdf is supported
     */
    @Override
    public String getDocumentType() {
        return "pdf";
    }

    /**
     *
     * @return the name of the current document.
     */
    @Override
    public String getDocumentName(){
        return name;
    }

    /**
     * Resets the name of the current document.
     * @param name the updated name of the document
     */
    @Override
    public void setDocumentName(String name){
        this.name = name;
    }

    /**
     * Saves a document to the given output stream
     * @param os
     */
    @Override
    public void saveDocument(OutputStream os) {
        PDDocument document = _viewController.getDocument();

        if(os != null && document != null){
            try {
                document.save(os);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (COSVisitorException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }


    public static void main(String[] args) {
        setApplicationLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        PDFViewer viewer = new PDFViewer();
        JMenuBar newMenuBar = viewer.newBuildMenuBar();
        JFrame frame = displayInJFrame(viewer, "PDFView", newMenuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        viewer.loadDocument(new File(System.getProperty("user.dir") + "/test-src/resources/n114po.pdf"));
    }



    /**
	 * @uml.property  name="_actionGroup"
	 * @uml.associationEnd  
	 */
    private ActionGroup _actionGroup;

    public ActionGroup getActionGroup(){
        if(_actionGroup == null){
            _actionGroup = _viewController.getActionGroup();
            /*TODO Consider chaining method calls for inserting actions into actionGroups
            insertAction(_aboutAction).atBeginning().ofGroup().withName("Help");
            insertAction(_aboutAction).before().action().withName("Something");
            insertAction(_aboutAction).before().action().matchingPredicate(Predicate<Action>)*/
            _actionGroup.insertActionIntoGroup(Insert.BEGINNING, Actions.actionWithName("Help"), _aboutAction);
            _actionGroup.insertActionIntoGroup(Insert.END, Actions.actionWithName("File"), _exitAction);
        }
        return _actionGroup;
    }

    public JMenuBar newBuildMenuBar() {


        final ActionGroup rootAction = getActionGroup();
        JComponent comp = rootAction.actionWalkable().reduce(new Reducer<Action, JComponent>() {


            @Override
            public JComponent reductionStep(Action current, Collection<JComponent> neighborResults) {
                JComponent component = null;

                if (firstNonNull((Boolean) current.getValue(ActionGroup.IS_GROUP_DIVIDER), Boolean.FALSE)) {
                    component = new JSeparator();
                } else {
                    if (firstNonNull((Boolean) current.getValue(ActionGroup.IS_ACTION_GROUP), Boolean.FALSE)) {
                        component = rootAction == current ? new JMenuBar() : new JMenu(current);
                        for (JComponent comp : neighborResults) {
                            component.add(comp);
                        }
                    } else {
                        component = new JMenuItem(current);
                    }
                }
                return component;
            }
        });

        return comp instanceof JMenuBar ? (JMenuBar) comp : null;

    }

    /****************************************************
     *  Page loading listener
     ****************************************************/
    @Override
    public void pageLoadStarted(Object sender) {
        JProgressBar progbar = getComponentNamed(this, "progress", JProgressBar.class);
        progbar.setIndeterminate(true);

    }

    @Override
    public void pageLoadingStatusUpdated(Object sender, String status) {
        JLabel statusLabel = getComponentNamed(this, "status", JLabel.class);
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }

    @Override
    public void pageLoadComplete(Object sender) {
        JProgressBar progbar = getComponentNamed(this, "progress", JProgressBar.class);
        progbar.setIndeterminate(false);
        this.revalidate();
        this.repaint();
    }

    /*********************************************************
     *  Action Publisher implementation.
     *********************************************************/
    @Override
    public void registerSubscriber(ActionSubscriber subscriber) {
        aps.registerSubscriber(subscriber);
    }

    @Override
    public void removeSubscriber(ActionSubscriber subscriber) {
        aps.removeSubscriber(subscriber);
    }

    /**
     * ***************************************************************
     * Host Application Implementation
     * ****************************************************************
     */

    @Override
    public List<ActionRef> publishedActions() {
        return aps.publishedActions();
    }


    @Override
    public void receivedNotification(Notification n) {
        n.showDialog(this.getTopLevelAncestor());
    }
}

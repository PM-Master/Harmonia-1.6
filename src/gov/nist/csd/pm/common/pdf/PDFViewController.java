package gov.nist.csd.pm.common.pdf;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import gov.nist.csd.pm.application.pdfviewer.PDFViewer;
import gov.nist.csd.pm.common.action.*;
import gov.nist.csd.pm.common.application.NotificationCenter;
import gov.nist.csd.pm.common.model.PropertyChangeObservable;
import gov.nist.csd.pm.common.model.PropertyChangeObservableSupport;
import gov.nist.csd.pm.common.model.PropertyChangeObserver;
import gov.nist.csd.pm.common.util.Files;
import gov.nist.csd.pm.common.util.Percentage;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.*;
import static com.google.common.collect.Lists.newArrayList;
import static gov.nist.csd.pm.common.action.ActionWrapper.createReference;
import static gov.nist.csd.pm.common.action.ArrayActionGroup.createActionDivider;
import static gov.nist.csd.pm.common.action.ArrayActionGroup.createActionGroup;
import static gov.nist.csd.pm.common.util.Functions2.map;
import static gov.nist.csd.pm.common.util.Functions2.pipe;
import static gov.nist.csd.pm.common.util.collect.Collections.select;
import static gov.nist.csd.pm.common.util.lang.Objects.castTo;
import static gov.nist.csd.pm.common.util.reflect.Classes.classFromObject;
import static gov.nist.csd.pm.common.util.reflect.Classes.getDeclaredFields;
import static gov.nist.csd.pm.common.util.reflect.Fields.getObjectFromField;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.getActiveWindow;
import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 1/24/11 Time: 1:56 PM Controller class for a number of PDFView's.  This allows PDFViews to be synchronized (e.g. when there exists a PDFView and a related PDFOutlineView*) PDFOutlineView not currently implemented.
 */
public class PDFViewController implements PropertyChangeObservable, ActionPublisher {

    /*
     * Available controller properties.
     */
    public static final String DOCUMENT_PROPERTY = "DOCUMENT";
    public static final String PAGE_NUMBER_PROPERTY = "PAGE";
    public static final String PAGE_PROPERTY = "PAGE_NUMBER";
    public static final String ZOOM_PROPERTY = "ZOOM";

    /**
	 * @uml.property  name="aps"
	 * @uml.associationEnd  
	 */
    private final ActionPublisherSupport aps = new ActionPublisherSupport(this);

    
    /**
     * Maps property names to their associated types.
     */
    static private final Map<String, Class<?>> propertyMap = new LinkedHashMap<String, Class<?>>();

    static {
        propertyMap.put(DOCUMENT_PROPERTY, PDDocument.class);
        propertyMap.put(PAGE_NUMBER_PROPERTY, Integer.class);
        propertyMap.put(PAGE_PROPERTY, PDPage.class);
        propertyMap.put(ZOOM_PROPERTY, Double.class);
    }
    /**
	 * Fields
	 * @uml.property  name="_pcs"
	 * @uml.associationEnd  
	 */
    PropertyChangeObservableSupport _pcs = new PropertyChangeObservableSupport(this);
    /**
	 * @uml.property  name="_document"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="org.apache.pdfbox.pdmodel.PDPage"
	 */
    PDDocument _document;
    /**
	 * @uml.property  name="_documentFile"
	 */
    File _documentFile;
    /**
	 * @uml.property  name="_page"
	 * @uml.associationEnd  
	 */
    PDPage _page;
    /**
	 * @uml.property  name="_viewers" multiplicity="(0 -1)"
	 */
    Collection<PDFPageViewer> _viewers;
    /**
	 * @uml.property  name="_pageNumber"
	 */
    int _pageNumber;
    /**
	 * @uml.property  name="_zoomValue"
	 */
    double _zoomValue = 1.0;


    /**
	 * @uml.property  name="intoActions"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public final Function<Object, ActionRef> intoActions = castTo(ActionRef.class);
    
    /**
	 * @uml.property  name="_events" multiplicity="(0 -1)"
	 */
    final Collection<ActionRef> _events = newArrayList();
    

    /**
     * Vararg constructor for PDFViewController
     * @param document - PDF document file to operate over.
     * @param viewers - viewers to control.
     */
    public PDFViewController(PDDocument document, PDFPageViewer... viewers) {
        this(document, asList(viewers));
    }

    private Collection<ActionRef> getPublishableActions(){
    	 Collection<ActionRef> actions = pipe(
                 classFromObject(),
                 getDeclaredFields(),
                 map(getObjectFromField(this)),
                 select(and(not(isNull()), instanceOf(ActionRef.class))),
                 map(intoActions)
         ).apply(this);
         _events.addAll(actions);
        return _events;
    }
    
    /**
     * Similar to the first constructor but accepting a collection of PDFPageViewer's and not
     * a variable argument array.
     * @param document - PDF document to operate over.
     * @param pdfPageViewers - viewers to control.
     */
    public PDFViewController(PDDocument document, Collection<PDFPageViewer> pdfPageViewers) {
    	Collection<ActionRef> publishableActions = getPublishableActions();
        aps.addAllActions(publishableActions);
        initializeActionGroup();
    	if (document == null) {
            try {
                document = new PDDocument();
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
        _viewers = checkNotNull(pdfPageViewers);
        setDocument(document);
    }

    /**
     * Sets the current page and page number of the current document.
     * For internal use only.
     * @param page
     * @param pageNumber
     */
    private void setPage(PDPage page, int pageNumber) {
        PDPage oldPage = _page;
        _page = page;
        _pcs.firePropertyChange(PAGE_PROPERTY, oldPage, _page);

        for (PDFPageViewer viewer : _viewers) {
            viewer.setPage(_page);
        }

        int oldPageNumber = _pageNumber;
        _pageNumber = pageNumber;
        _pcs.firePropertyChange(PAGE_NUMBER_PROPERTY, oldPageNumber, _pageNumber);
    }

    /**
     * Sets the current page of the document.  A document must already be set
     * and this page must exist in the document for any action to be taken.
     * @param page - the page to set.
     */
    public void setPage(PDPage page) {
        if (_document != null) {
            List<PDPage> pages = _document.getDocumentCatalog().getAllPages();
            if (pages.contains(page)) {
                setPage(page, pages.indexOf(page));
            }
        }
    }

    /**
     * Sets the current page of the document to the number indicated.  A document
     * must already be set and the page number must be within the range of 0 and
     * the documents page count - 1, inclusive.
     * @param pageNumber - the number of the page to set current.
     */
    public void setPage(int pageNumber) {
        if (_document != null && (pageNumber >= 0 && pageNumber < _document.getNumberOfPages())) {
            List<PDPage> pages = _document.getDocumentCatalog().getAllPages();
            PDPage page = pages.get(pageNumber);
            setPage(page, pageNumber);
        }
    }

    /**
     * Sets the zoom value as a scaling function applied to the document during rendering.
     * @param zoomValue - a scaling factor to set.  Values between 0 < x <= 1.0 will result
     * in a minification.  1.0 will result in no change.  Numbers greater than 1.0 will result
     * in a maxification.  Values less than zero are invalid and will not change the zoom.
     */
    public void setZoom(double zoomValue) {
        if(zoomValue <= 0.0){
            return;
        }
        double oldValue = _zoomValue;
        _zoomValue = zoomValue;
        for (PDFPageViewer viewer : _viewers) {
            viewer.setZoom(_zoomValue);
        }
        _pcs.firePropertyChange(ZOOM_PROPERTY, oldValue, zoomValue);
    }

    public double getZoom(){
        return _zoomValue;
    }



    /**
     * Sets the value of the current Document.  loadDocument will set the current
     * document automatically using this method.
     * @param document
     */
    public final void setDocument(PDDocument document) {
        PDDocument oldDocument = _document;
        _document = document;
        for (PDFPageViewer viewer : _viewers) {
            viewer.setDocument(_document);
        }
        _pcs.firePropertyChange(DOCUMENT_PROPERTY, oldDocument, _document);
        if(_document == null){
            setPage(null);
        }
        else{
            List<PDPage> pages = _document.getDocumentCatalog().getAllPages();
            PDPage page = !pages.isEmpty() ? pages.get(0) : null;
            setPage(page);
        }
        if(null != oldDocument){
            try {
                oldDocument.close();
            } catch (IOException ex) {
                Logger.getLogger(PDFViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public final void saveDocument(OutputStream os){
        if(os != null && _document != null){
            try {
                _document.save(os);
            } catch (IOException e) {
                NotificationCenter.INSTANCE.postNotification(Exception.class, "Coudn't save document");
            } catch (COSVisitorException e) {
            }
        }
    }

    /**
     *
     * @return the value of the current document.
     */
    public PDDocument getDocument() {
        return _document;
    }

    /**
     * Moves all views to the next page.  Calling this while on the last
     * page will result in no action being taken.
     */
    public void nextPage() {
        setPage(_pageNumber + 1);
    }

    /**
     * Moves all views to the previous page.  Calling this while on the
     * first page will result in no action being taken.
     */
    public void prevPage() {
        setPage(_pageNumber - 1);
    }

    /**
     *
     * @return an Action that will increment the current page.  Good for interfacing
     * with UI buttons and menu items.  Uses nextPage() to do it's work.
     */
    public Action getNextPageAction() {
        return _nextPageAction;
    }
    /**
	 * @uml.property  name="_nextPageAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Action _nextPageAction = createReference(new AbstractAction("Forward") {
         {
            putValue(ActionRef.ID, PDFControllerActionLibrary.NEXT_PAGE.name());
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(Action.SHORT_DESCRIPTION, "View the next page");
        }
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            nextPage();
        }
    });

    /**
     *
     * @return an Action that will decrement the current page.  Good for interfacing
     * with UI buttons and menu items.  Uses prevPage() to do it's work.
     */
    public Action getPrevPageAction() {
        return _prevPageAction;
    }
    /**
	 * @uml.property  name="_prevPageAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Action _prevPageAction = createReference(new AbstractAction("Back") {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(Action.SHORT_DESCRIPTION, "Open a PDF");
        }
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            prevPage();
        }
    });

    /**
     *
     * @return an ItemListener that will alter the zoom level of the controller
     * using the item selected.  The selected items MUST implement the Number
     * interface or NO ACTION will be taken.  Usage of @see Float @see Double
     * @see gov.nist.csd.pm.common.util.Percentage is encouraged when configuring ItemListenable UI objects.
     */
    public ItemListener getZoomLevelItemListener() {
        return _zoomLevelItemListener;
    }

    private static final String ZOOM_WITH_COMBO_COMMAND = "ZOOM_WITH_COMBO";
    /**
	 * @uml.property  name="_zoomLevelItemListener"
	 */
    private ItemListener _zoomLevelItemListener = new ItemListener() {

        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            try {
                Object[] items = itemEvent.getItemSelectable().getSelectedObjects();
                if (items != null && items.length > 0) {
                    Object item = items[0];
                    double zoomValue = item instanceof Number ? ((Number) item).doubleValue() : 1.0;
                    System.out.println("Zoom level is " + zoomValue);
                    _zoomLevelAction.putValue(ZOOM_LEVEL_PROP, new Double(zoomValue));
                    ActionEvent ae = new ActionEvent(itemEvent.getSource(), itemEvent.getID(), ZOOM_WITH_COMBO_COMMAND, System.currentTimeMillis(), 0);
                    _zoomLevelAction.actionPerformed(ae);
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Zoom level was not set or was set improperly.");
            }

        }
    };

    private static final String ZOOM_LEVEL_PROP = PDFViewController.class.getCanonicalName() + "_ZOOM_LEVEL";
    /**
	 * @uml.property  name="_zoomLevelAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Action _zoomLevelAction = createReference(new AbstractAction("Zoom To"){
        {
            putValue(Action.SHORT_DESCRIPTION, "Set's the zoom level");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Z);
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.ALT_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Object zoom = getValue(ZOOM_LEVEL_PROP);
            if(zoom != null && zoom instanceof Number){
                setZoom(((Number)zoom).doubleValue());
            }
        }
    });

    private Action createZoomToAction(final Percentage zoomFactor){

        return createReference(new AbstractAction("Zoom " + zoomFactor){
            {
                putValue(Action.SHORT_DESCRIPTION, "Set's the zoom level to " + zoomFactor);

            }
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                _zoomLevelAction.putValue(ZOOM_LEVEL_PROP, zoomFactor);
                _zoomLevelAction.actionPerformed(actionEvent);
            }
        });
    }

    private Action createZoomByAction(Percentage zoomFactor){
        final Percentage _zoomFactor = zoomFactor == null ? new Percentage(100.0) : zoomFactor;
        return createReference(new AbstractAction("Zoom by " + zoomFactor){
            private final int accelerator = _zoomFactor.isMagnification() ? KeyEvent.VK_MINUS : KeyEvent.VK_PLUS;
            private final String zoomDirection = _zoomFactor.isMagnification() ? "in" : "out";
            {
                putValue(Action.SHORT_DESCRIPTION, "Zoom " + zoomDirection);
                putValue(Action.LONG_DESCRIPTION, "Set's the zoom level to " + _zoomFactor + "of current");
                putValue(Action.MNEMONIC_KEY, accelerator);
                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator, KeyEvent.CTRL_MASK));
            }
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                _zoomLevelAction.putValue(ZOOM_LEVEL_PROP, _zoomFactor.multiply(_zoomValue));
                _zoomLevelAction.actionPerformed(actionEvent);
            }
        });
    }

    /**
     *
     * @return an action which opens a PDF file and presents it in the associated views.
     */
    public Action getOpenAction() {
        return _openAction;
    }
    /**
	 * @uml.property  name="_openAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Action _openAction = createReference(new AbstractAction("Open...") {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
            putValue(ActionRef.ID, PDFDocumentActionLibrary.OPEN.name());
            putValue(Action.SHORT_DESCRIPTION, "Open a PDF");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.dir")));
            jfc.setFileFilter(Files.PDF_FILTER);
            jfc.setMultiSelectionEnabled(false);
            jfc.showOpenDialog(getActiveWindow());
            File selectedFile = jfc.getSelectedFile();
            if(selectedFile != null){
                try {
                    setDocument(PDDocument.load(selectedFile));
                } catch (IOException e) {
                    Logger.getLogger(PDFViewer.class.getName()).log(Level.SEVERE, "Error opening file", e);
                }
            }
        }
    });


    public Action getCloseAction(){
        return _closeAction;
    }

    /**
	 * @uml.property  name="_closeAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Action _closeAction = createReference(new AbstractAction("Close..."){
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
            putValue(Action.SHORT_DESCRIPTION, "Close the current file");
        }
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            setDocument(null);
        }
    });

    public static final String SAVE_AS_ACTION = "save_as_action";
    public static final String SAVE_ACTION = "save_action";

    /**
	 * @return  an Action which saves the currently loaded document.  The currently loaded document must not be null.  The referencing file,  initially set by calling loadDocument, should be set but does not need  to be.  If no documentFile is set then the controller calls the saveAs  Action on the users behalf.
	 * @uml.property  name="sAVE_ACTION"
	 */
    public Action getSaveAction() {
        return _saveAction;
    }
    /**
	 * @uml.property  name="_saveAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Action _saveAction = createReference(new AbstractAction("Save") {
		{
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(ActionRef.ID, PDFDocumentActionLibrary.SAVE.name());
            putValue(Action.SHORT_DESCRIPTION, "Save this PDF");
            setEnabled(false);
        }
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (_documentFile == null) {
                getSaveAsAction().actionPerformed(actionEvent);
            } else {
                try {
                    saveDocument(new FileOutputStream(_documentFile));
                } catch (IOException ex) {
                    Logger.getLogger(PDFViewer.class.getName()).log(Level.SEVERE, "Error saving file", ex);
                }
            }
        }
    });

    /**
	 * @return  an Action for saving a document to another filename.  The current  document MUST be set.  The document file need not be set as it will only  be changed by this action.
	 * @uml.property  name="sAVE_AS_ACTION"
	 */
    public Action getSaveAsAction() {
        return _saveAsAction;
    }
    /**
	 * @uml.property  name="_saveAsAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Action _saveAsAction = createReference(new AbstractAction("Save As...") {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(ActionRef.ID, PDFDocumentActionLibrary.SAVE_AS.name());
            putValue(Action.SHORT_DESCRIPTION, "Save this PDF under another name");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            checkNotNull(_document);

            String homePath = _documentFile == null ? System.getProperty("user.home") : _documentFile.getParent();

            JFileChooser jfc = new JFileChooser(homePath);
            jfc.setFileFilter(Files.PDF_FILTER);

            int result = jfc.showSaveDialog(getActiveWindow());
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = jfc.getSelectedFile();
                if (file != null) {
                    _documentFile = file;
                    getSaveAction().actionPerformed(actionEvent);
                }
            }
        }
    });

    public Action getAddSignatureAction(){
        return _addSignatureAction;
    }

    /**
	 * @uml.property  name="_addSignatureAction"
	 * @uml.associationEnd  
	 */
    private final ActionRef _addSignatureAction = createReference(new AbstractAction(){
        {
            putValue(Action.NAME, "Add Signature");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(ActionRef.ID, PDFDocumentActionLibrary.ADD_SIGNATURE.name());
            putValue(Action.SHORT_DESCRIPTION, "Add a signature to this pdf.");
            setEnabled(false);
        }
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            //Get the selected area
            //add a signature to the form of the present page
            //With the given co-ordinates.
        }
    });
    /*
     *  Action group configuration.  Action Groups are a way to specify relationships between actions
     *  without using GUI elements.  These Action Groups extend Action as a convenience, making it easy to
     *  add them to menus, buttons, and popups.
     */
    /**
	 * @uml.property  name="_actionGroup"
	 * @uml.associationEnd  
	 */
    private final ActionGroup _actionGroup = createActionGroup();

    private void initializeActionGroup(){
        _actionGroup.addSubgroupOf("File",
                _openAction,
                _closeAction,
                createActionDivider(),
                _saveAction,
                _saveAsAction);
        _actionGroup.addSubgroupOf("Annotate",
                _addSignatureAction);
        _actionGroup.addSubgroupOf("Navigate",
                _prevPageAction, _nextPageAction,
                createActionGroup("Zoom",
                        createActionDivider(),
                        createZoomByAction(new Percentage(110)),
                        createZoomByAction(new Percentage(90)),
                        createActionDivider(),
                        createZoomToAction(new Percentage(10)),
                        createZoomToAction(new Percentage(25)),
                        createZoomToAction(new Percentage(50)),
                        createZoomToAction(new Percentage(75)),
                        createZoomToAction(new Percentage(100)),
                        createZoomToAction(new Percentage(200)),
                        createZoomToAction(new Percentage(300)),
                        createZoomToAction(new Percentage(400)),
                        createZoomToAction(new Percentage(500)),
                        createZoomToAction(new Percentage(750)),
                        createZoomToAction(new Percentage(1000)
                    )));
        _actionGroup.addSubgroupOf("Help");
    }

    public ActionGroup getActionGroup(){
        return _actionGroup;
    }


    /**
     * @see gov.nist.csd.pm.common.model.PropertyChangeObservable for details.
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        _pcs.addPropertyChangeListener(listener);
    }

    /**
     * @see gov.nist.csd.pm.common.model.PropertyChangeObservable for details.
     * @param property
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        _pcs.addPropertyChangeListener(property, listener);
    }

    /**
     * @see gov.nist.csd.pm.common.model.PropertyChangeObservable for details.
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        _pcs.removePropertyChangeListener(listener);
    }

    /**
     * @see gov.nist.csd.pm.common.model.PropertyChangeObservable for details.
     * @param property
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        _pcs.removePropertyChangeListener(property, listener);
    }

    @Override
    public void addObserver(PropertyChangeObserver observer) {
        _pcs.addObserver(observer);
    }

    @Override
    public void removeObserver(PropertyChangeObserver observer) {
        _pcs.removeObserver(observer);
    }

    @Override
    public List<ActionRef> publishedActions() {
        return aps.publishedActions();
    }

	@Override
	public void registerSubscriber(ActionSubscriber subscriber) {
		aps.registerSubscriber(subscriber);
		
	}

	@Override
	public void removeSubscriber(ActionSubscriber subscriber) {
		aps.removeSubscriber(subscriber);
	}



}

package gov.nist.csd.pm.common.pdf;


import gov.nist.csd.pm.common.pdf.support.CoordinateSpace;
import gov.nist.csd.pm.common.pdf.support.PDFFormUIBuilder;
import gov.nist.csd.pm.common.pdf.support.PDFields;
import gov.nist.csd.pm.common.pdf.support.PageLoadingListener;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextbox;

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static gov.nist.csd.pm.common.pdf.support.PDDocuments.emptyDocument;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 1/24/11
 * Time: 2:59 PM
 * A view that displays a Swing user interface on top of a PDFPageView.
 */
public class PDFFormView extends PDFPageView implements PageLoadingListener {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5256530062938683498L;

	/**
	 * @uml.property  name="_uiBuilder"
	 * @uml.associationEnd  
	 */
	private PDFFormUIBuilder _uiBuilder;

    /**
	 * @uml.property  name="_document"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="org.apache.pdfbox.pdmodel.PDPage"
	 */
    private PDDocument _document;

    private static PDField NULL_FIELD;

    static {
        try {
            NULL_FIELD = new PDTextbox(new PDAcroForm(new PDDocument()));

        } catch (IOException ex) {
            Logger.getLogger(PDFFormView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public PDFFormView(){
        this(null);
    }



    public PDFFormView(PDDocument document){
        super(null);
        setLayout(null);
        addPageLoadingListener(this);
        setDocument(document);
    }

    public void reinitializeForm(){

        removeAll();
        if(_uiBuilder != null 
                && _document != null 
                && _page != null){
            _uiBuilder.buildFormUIForPage(this, _page, AffineTransform.getScaleInstance(getZoom(), getZoom()));
        }
    }



    /**
     * @see PDFPageViewer for details.
     * @param document
     */
    @Override
    public final void setDocument(PDDocument document){
        _document = document;
        if(_document != null && !emptyDocument(_document)) {
        	if(_uiBuilder != null){
        		_uiBuilder.removeSubscriber(aps);
        	}

            _uiBuilder = new PDFFormUIBuilder(_document);
            _uiBuilder.registerSubscriber(aps);
            PDPage page = (PDPage) _document.getDocumentCatalog().getAllPages().get(0);
            setPage(page);
        }
        else{
            setPage(null);
        }
    }


    public PDField getAcroFormComponentAt(Point2D point, CoordinateSpace sourceSpace){
        Point2D pdfPoint = sourceSpace.convertPointTo(point, this.getBounds(), CoordinateSpace.PDF);
        if(_document == null) return null;
        PDDocumentCatalog cat = _document.getDocumentCatalog();
        if(cat == null) return null;
        PDAcroForm acroForm = cat.getAcroForm();
        if(acroForm == null) return null;
        try {
            List<PDField> fields = acroForm.getFields();
            for(PDField field : fields){
                PDRectangle rect = PDFields.getPDRectangleForField(field);
                if(rect.contains((float)pdfPoint.getX(), (float)pdfPoint.getY())){
                    return field;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    public void forwardMouseEvent(MouseEvent me){
        this.processMouseEvent(me);
    }

    public PDField getAcroFormComponentAt(int x, int y, CoordinateSpace sourceSpace){
        return getAcroFormComponentAt(new Point2D.Double((double)x, (double)y), sourceSpace);
    }



   

    /**
     * @see gov.nist.csd.pm.common.pdf.support.PageLoadingListener for details.
     * @param sender
     */
    @Override
    public void pageLoadStarted(Object sender) {
        removeAll();
    }

    /**
     * @see gov.nist.csd.pm.common.pdf.support.PageLoadingListener for details.
     * @param sender
     * @param status
     */
    @Override
    public void pageLoadingStatusUpdated(Object sender, String status) {
        //Not interested in this message.
    }

    /**
     * @see gov.nist.csd.pm.common.pdf.support.PageLoadingListener for details.
     * @param sender
     */
    @Override
    public void pageLoadComplete(Object sender) {
        reinitializeForm();
    }


}

package gov.nist.csd.pm.common.pdf;


import gov.nist.csd.pm.common.action.ActionPublisher;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 1/24/11 Time: 2:42 PM This interface provides a common facade for controlling with PDF related views. This interface has been intentionally designed uni-directionally, with only setters provided.  The intention of this decision was to reduce potential coupling.
 */
public interface PDFPageViewer {


    /**
     * Sets the document referenced by a page view
     * @param document a PDDocument reference
     */
    public void setDocument(PDDocument document);
    /**
     * Sets the currently viewing page of a page view.
     * @param page the PDPage to view.
     */
    public void setPage(PDPage page);
    /**
     * Sets the current zoom level of the current page of the current document
     * @param zoom - the magnification or minification factor to set. Values
     * between 0 and 1, exclusive, will cause minification.  A value of 1 will cause
     * no change (and is the default).  Values above 1.0 will cause magnification.
     */
    public void setZoom(double zoom);
    
    
    public boolean isActionPublisher();
    
    /**
	 * @uml.property  name="actionPublisher"
	 * @uml.associationEnd  
	 */
    public ActionPublisher getActionPublisher();
}

package gov.nist.csd.pm.common.pdf;

import gov.nist.csd.pm.application.pdfviewer.PDFViewer;
import gov.nist.csd.pm.common.pdf.support.PDFieldSwingAdapters;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/11/11
 * Time: 2:08 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PDFFunctionalityContexts {
    public static final String PDF_ACROFORM_UI_HOOKING_CONTEXT = PDFieldSwingAdapters.class.getCanonicalName().replace(".", "_");
    public static final String PDF_FORM_VIEW_HOOKING_CONTEXT = PDFViewer.class.getCanonicalName().replace(".", "_");
}

package gov.nist.csd.pm.application.workflow.creator;

import gov.nist.csd.pm.common.application.NotificationCenter;
import gov.nist.csd.pm.common.pdf.PDFFormView;
import gov.nist.csd.pm.common.pdf.notifications.SelectionForSignatureConversion;
import gov.nist.csd.pm.common.pdf.support.CoordinateSpace;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextbox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 8/9/11
* Time: 4:04 PM
* To change this template use File | Settings | File Templates.
*/
class OverViewMouseAdapter extends MouseAdapter {

    /**
	 * @uml.property  name="formView"
	 * @uml.associationEnd  
	 */
    private final PDFFormView formView;
    /**
	 * @uml.property  name="overView"
	 * @uml.associationEnd  
	 */
    private final OverView overView;

    public OverViewMouseAdapter(PDFFormView formView, OverView ov) {
        this.formView = formView;
        this.overView = ov;
    }

    public void mouseClicked(MouseEvent me) {

        me = SwingUtilities.convertMouseEvent(me.getComponent(), me, formView);
        Component comp = SwingUtilities.getDeepestComponentAt(formView, me.getX(), me.getY());
         if (overView != null) {
            overView.clearAnnotations();
            if (comp != null && comp != formView) {
                PDField foundField = formView.getAcroFormComponentAt(me.getX(), me.getY(), CoordinateSpace.SWING);
                if (foundField != null && foundField instanceof PDTextbox) {
                    overView.addAnnotation(CreatorKit.createAnnotationFor(comp.getBounds()));
                    NotificationCenter.INSTANCE.postNotification("Signature Found",
                            SelectionForSignatureConversion.TYPE,
                            foundField, formView);
                }
                else{
                    NotificationCenter.INSTANCE.postNotification("No signature found.",
                            SelectionForSignatureConversion.TYPE);
                }
            }
        }
        super.mouseClicked(me);
    }
}

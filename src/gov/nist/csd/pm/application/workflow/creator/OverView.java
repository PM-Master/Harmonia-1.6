package gov.nist.csd.pm.application.workflow.creator;

import gov.nist.csd.pm.common.pdf.support.PageLoadingListener;

import javax.swing.*;
import java.awt.*;

import static com.google.common.collect.Lists.newArrayList;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 8/9/11
* Time: 4:02 PM
* To change this template use File | Settings | File Templates.
*/
class OverView extends JPanel implements PageLoadingListener {
    /**
	 * @uml.property  name="annotations"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.application.workflow.creator.Annotation"
	 */
    java.util.List<Annotation> annotations = newArrayList();

    public OverView() {
        super();
        setLayout(null);
        setOpaque(false);
        setBackground(Color.YELLOW);
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        repaint();
    }

    public void removeAnnotation(Annotation annotation) {
        annotations.remove(annotation);
        repaint();
    }

    public void clearAnnotations() {
        annotations.removeAll(annotations);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        for (Annotation a : annotations) {
            a.paintAnnotation(g2d);
        }
    }

    /**
     * Informs the delegate that the sender has started to load a page.
     *
     * @param sender - the object loading the page.
     */
    @Override
    public void pageLoadStarted(Object sender) {
        clearAnnotations();

    }

    /**
     * Informs the delegate that some status has been updated.
     *
     * @param sender - the object loading the page.
     * @param status - a textual representation of the status of the load.
     */
    @Override
    public void pageLoadingStatusUpdated(Object sender, String status) {

    }

    /**
     * Informs the delegate that the page load has completed.
     *
     * @param sender - the object loading the page.
     */
    @Override
    public void pageLoadComplete(Object sender) {

    }
}

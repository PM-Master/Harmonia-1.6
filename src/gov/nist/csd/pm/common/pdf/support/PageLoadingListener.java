/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.pdf.support;

/**
 * Implementation of a delegate pattern for informing objects of the status
 * of a PDF page load.  Nothing is required on behalf of the implementing
 * class.  It is recommended that the implementing class not mutate the objects
 * passed as parameters to these methods.
 *
 * All methods will be sent over the event dispatch thread.  Update your UI
 * freely, but please don't block.
 * @author Administrator
 */
public interface PageLoadingListener {
    /**
     * Informs the delegate that the sender has started to load a page.
     * @param sender - the object loading the page.
     */
    public void pageLoadStarted(Object sender);
    /**
     * Informs the delegate that some status has been updated.
     * @param sender - the object loading the page.
     * @param status - a textual representation of the status of the load.
     */
    public void pageLoadingStatusUpdated(Object sender, String status);
    /**
     * Informs the delegate that the page load has completed.
     * @param sender - the object loading the page.
     */
    public void pageLoadComplete(Object sender);
}

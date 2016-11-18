/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.pdf.support;

/**
 * Interface for creating PDFPageLoadingListenable objects.
 *
 * Implementations of this interface should add and remove PageLoadingListener's
 * immediately when instructed and call the methods of PageLoadingListener on
 * all registered listeners when appropriate.
 * @author Administrator
 */
public interface PageLoadingListenable {
    /**
     * Adds a PageLoadingListener.  The same listener can be added multiple times.
     * @param listener
     */
    public void addPageLoadingListener(PageLoadingListener listener);
    /**
     * Removes a PageLoadingListener.  All identical instances of PageLoadingListener, i.e. all
     * references to the same instance of an object, must be removed at this time.
     *
     * No further updates may be sent to those listener objects.
     * @param listener
     */
    public void removePageLoadingListener(PageLoadingListener listener);

}

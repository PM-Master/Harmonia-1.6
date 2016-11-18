package gov.nist.csd.pm.common.pdf;

import gov.nist.csd.pm.common.action.ActionPublisher;
import gov.nist.csd.pm.common.action.ActionPublisherSupport;
import gov.nist.csd.pm.common.action.ActionRef;
import gov.nist.csd.pm.common.action.ActionSubscriber;
import gov.nist.csd.pm.common.pdf.support.PageLoadingListenable;
import gov.nist.csd.pm.common.pdf.support.PageLoadingListener;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static javax.swing.SwingUtilities.invokeAndWait;

//'(org.apache.pdfbox.pdmodel.interactive.form PDCheckbox PDTextbox PDPushButton)

/**
 * A class for viewing PDF Pages as an image on a JPanel.
 * User: R McHugh
 * Date: 1/24/11
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class PDFPageView extends JPanel implements PDFPageViewer, PageLoadingListenable, AncestorListener, ActionPublisher {

    protected static final String PAGE_LOADING = "Empty Page";
    protected static final int WIDTH = 512;
    protected static final int HEIGHT = 512;
    protected static final int OFFSET = 2;
    /**
	 * @uml.property  name="_page"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    protected PDPage _page = null;
    /**
	 * @uml.property  name="_zoom"
	 */
    private double _zoom = 1.0f;
    /**
	 * @uml.property  name="_bufferedImageStack"
	 */
    private List<ImageInfo> _bufferedImageStack = newArrayList();
    /**
	 * @uml.property  name="_inFrame"
	 */
    private boolean _inFrame = false;
    /**
	 * @uml.property  name="aps"
	 * @uml.associationEnd  
	 */
    protected ActionPublisherSupport aps = new ActionPublisherSupport(this);

    private class ImageInfo{
        private final Image _img;
        private double _opacity = 1.0;
        public ImageInfo(Image img){
            _img = img;
        }
        public void setOpacity(double opacity){
            _opacity = opacity;
        }
        public double getOpacity(){
            return _opacity;
        }
        public Image getImage(){
            return _img;
        }
        public Composite getComposite(){
            return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) _opacity);
        }
    }

    /**
	 * @uml.property  name="_pageUpdateListeners"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.pdf.support.PageLoadingListener"
	 */
    private List<PageLoadingListener> _pageUpdateListeners = newArrayList();

    public PDFPageView(PDPage currentPage) {
        super();
        setBackground(Color.CYAN);
        addAncestorListener(this);
        setPage(currentPage);
    }

    private static BufferedImage getCompatibleImage(int width, int height) {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
    }

    private void notifyPageListenersStarting() {
        for (PageLoadingListener listener : _pageUpdateListeners) {
            listener.pageLoadStarted(this);
        }
    }

    private void notifyPageListenersComplete() {
        for (PageLoadingListener listener : _pageUpdateListeners) {
            listener.pageLoadComplete(this);
        }
    }

    private void notifyPageListeners(String status) {
        for (PageLoadingListener listener : _pageUpdateListeners) {
            listener.pageLoadingStatusUpdated(this, status);
        }
    }


    protected Image getPageLoadingImage() {
        Image loadingImage = getCompatibleImage(WIDTH, HEIGHT);
        Graphics2D g2 = (Graphics2D) loadingImage.getGraphics();
        Color orig = g2.getColor();
        g2.setColor(Color.DARK_GRAY);
        g2.fillRoundRect(0, 0, 512, 512, 10, 10);
        Font f = Font.decode("Arial-bold-18");
        FontMetrics fm = g2.getFontMetrics(f);
        Rectangle2D bounds = fm.getStringBounds(PAGE_LOADING, g2);
        int xpos = (WIDTH / 2) - (int) bounds.getWidth() / 2;
        int ypos = (HEIGHT / 2) - (int) bounds.getHeight() / 2;
        g2.setColor(Color.WHITE);
        g2.drawString(PAGE_LOADING, xpos + OFFSET, ypos - OFFSET);
        g2.setColor(Color.GRAY);
        g2.drawString(PAGE_LOADING, xpos, ypos);
        g2.setColor(orig);
        return loadingImage;
    }

    /**
     *
     */
    private void reloadBuffersInBackground() {
        if (!_inFrame) {
            return;
        }
        notifyPageListeners("Reloading Buffers");
        BackgroundBufferLoadingWorker worker = new BackgroundBufferLoadingWorker(_page, _zoom);
        worker.execute();
        if (_page != null) {
            try {
                Dimension dim = _page.findMediaBox().createDimension();

            } catch (Exception ex) {
                Logger.getLogger(PDFPageView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.repaint();

    }

    /**
     * @param listener
     * @see gov.nist.csd.pm.common.pdf.support.PageLoadingListenable for details.
     */
    @Override
    public void addPageLoadingListener(PageLoadingListener listener) {
        _pageUpdateListeners.add(listener);
    }

    /**
     * @param listener
     * @see gov.nist.csd.pm.common.pdf.support.PageLoadingListenable for details.
     */
    @Override
    public void removePageLoadingListener(PageLoadingListener listener) {
        _pageUpdateListeners.remove(listener);
    }

    /**
     *
     * @param ae
     * @see javax.swing.event.AncestorListener for details
     */
    @Override
    public void ancestorAdded(AncestorEvent ae) {
        if (ae.getAncestorParent() == null) {
            _inFrame = true;
            reloadBuffersInBackground();
        }
    }

    /**
     *
     * @param ae
     * @see javax.swing.event.AncestorListener for details
     */
    @Override
    public void ancestorRemoved(AncestorEvent ae) {
        if (ae.getAncestorParent() == null) {
            _inFrame = false;
        }
        //System.out.println("Ancestor removed " + ae.getAncestor() + " ancestor parent " + ae.getAncestorParent() + " component " + ae.getComponent() + " this " + this);
    }

    /**
     *
     * @param ae
     * @see javax.swing.event.AncestorListener for details
     */
    @Override
    public void ancestorMoved(AncestorEvent ae) {
        //System.out.println("Ancestor moved " + ae.getAncestor() + " ancestor parent " + ae.getAncestorParent() + " component " + ae.getComponent() + " this " + this);
    }


    private void resetSize(Dimension d){
        setMinimumSize(d);
        setPreferredSize(d);
        setMaximumSize(d);
        revalidate();
        repaint();
    }


    /*
    * Rendering PDF pages to images is an intrinsically heavy operation.
    * To keep the UI responsive, the work of rendering is delegated to this
    * SwingWorker.
    */
    private class BackgroundBufferLoadingWorker extends SwingWorker<Image, String> {

        private final PDPage _pageToBuffer;
        private final double _zoomLevel;

        public BackgroundBufferLoadingWorker(PDPage newPage, double zoom) {
            if(_bufferedImageStack.size() > 1){
                _bufferedImageStack.remove(0);
            }
            notifyPageListenersStarting();
            _pageToBuffer = newPage; //checkNotNull(newPage);
            _zoomLevel = zoom;
        }

        @Override
        protected Image doInBackground() throws Exception {
            publish("Loading...");
            Image renderedImage = null;
            if (_pageToBuffer != null) {
                final Dimension dim = _pageToBuffer.findMediaBox().createDimension();
                int pwidth = (int) (dim.getWidth() * _zoomLevel);
                int pheight = (int) (dim.getHeight() * _zoomLevel);
                System.out.println("Setting size to height " + dim.getHeight() + " width " + dim.getWidth());
                invokeAndWait(new Runnable(){
                    @Override
                    public void run(){
                        resetSize(new Dimension((int) (dim.getWidth() * _zoom), (int) (dim.getHeight() * _zoom)));
                    }
                });
                renderedImage = getCompatibleImage(pwidth, pheight);
                {
                    final Graphics2D g = (Graphics2D) renderedImage.getGraphics();

                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, pwidth, pheight);
                    g.scale(_zoomLevel, _zoomLevel);
                    invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            PageDrawer pageDrawer = null;
                            try {
                                pageDrawer = new PageDrawer();
                                pageDrawer.drawPage(g, _pageToBuffer, dim);
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }
                    });
                }
                publish("Complete!");
            }
            else{
                System.out.println("Setting size to height " + HEIGHT + " width " + WIDTH);
                invokeAndWait(new Runnable(){
                    @Override
                    public void run(){
                        resetSize(new Dimension(WIDTH, HEIGHT));
                    }
                });
                renderedImage = getPageLoadingImage();
            }

            return renderedImage;
        }

        @Override
        protected void process(List<String> statusMessages) {
            if (!statusMessages.isEmpty()) {
                notifyPageListeners(statusMessages.get(0));
            }
        }

        @Override
        protected void done() {
            try {
                ImageInfo ii = new ImageInfo(get());
                _bufferedImageStack.add(ii);
                Image img = ii.getImage();
                PDFPageView.this.setMinimumSize(new Dimension(img.getWidth(null), img.getHeight(null)));
                PDFPageView.this.setPreferredSize(PDFPageView.this.getMinimumSize());
                PDFPageView.this.setBounds(0, 0, img.getWidth(null), img.getHeight(null));
                PDFPageView.this.revalidate();
                PDFPageView.this.repaint();
                if(_pageToBuffer != null){
                   notifyPageListeners("PDF Load Complete!");
                   notifyPageListenersComplete();
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(PDFPageView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(PDFPageView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }


    /**
     * @param g - a Graphics context to draw into.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        for(ImageInfo ii : _bufferedImageStack){
            if (ii.getImage() != null) {
                g2.setComposite(ii.getComposite());
                g.drawImage(ii.getImage(), 0, 0, this);
            }
        }
    }


    /**
     * @return the current zoom level of the view.
     */
    public double getZoom() {
        return _zoom;
    }

    /**
     * @param _zoom
     * @see PDFPageViewer for details.
     */
    @Override
    public void setZoom(double _zoom) {
        this._zoom = _zoom;
        reloadBuffersInBackground();

    }

    /**
     * @param page
     * @see PDFPageViewer for details.
     */
    @Override
    public final void setPage(PDPage page) {
        this._page = page;
        reloadBuffersInBackground();

    }

    @Override
    public void setDocument(PDDocument document) {
        //Don't need the document reference for this viewer;
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

	@Override
	public boolean isActionPublisher() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public ActionPublisher getActionPublisher() {
		// TODO Auto-generated method stub
		return aps;
	}
}

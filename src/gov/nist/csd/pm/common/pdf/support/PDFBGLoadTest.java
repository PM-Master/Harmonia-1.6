/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.pdf.support;

import gov.nist.csd.pm.common.pdf.PDFPageView;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author Administrator
 */
public class PDFBGLoadTest extends JPanel {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test Harness");
        PDFBGLoadTest loadTestPanel = new PDFBGLoadTest(System.getProperty("user.dir") + "/test-src/resources/n114po.pdf");
        frame.getContentPane().add(loadTestPanel);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    /**
	 * @uml.property  name="_file"
	 */
    private File _file;
    /**
	 * @uml.property  name="_bufferedImage"
	 */
    private Image _bufferedImage;
    /**
	 * @uml.property  name="_page"
	 * @uml.associationEnd  readOnly="true"
	 */
    private PDPage _page;
    /**
	 * @uml.property  name="_zoom"
	 */
    private double _zoom = 1.0f;

    public PDFBGLoadTest(String filename) {
        _file = new File(filename);
        if (_file.exists()) {
            System.out.println("file found, loading");
            try {

                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        reloadBuffersInBackground();
                    }
                });
            } catch (Exception ex) {
                Logger.getLogger(PDFBGLoadTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @see javax.swing.JComponent.paintComponent for details.
     * @param g - a Graphics context to draw into.
     */
    @Override
    public void paintComponent(Graphics g) {
        if (_bufferedImage != null) {
            g.drawImage(_bufferedImage, 0, 0, this);
        }
    }

    private void reloadBuffersInBackground() {

        _bufferedImage = null;
        try {
            BackgroundBufferLoadingWorker worker = new BackgroundBufferLoadingWorker(_file, 1, _zoom);
            worker.execute();

        } catch (Exception ex) {
            Logger.getLogger(PDFPageView.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.revalidate();
        this.repaint();

    }

    private static BufferedImage getCompatibleImage(int width, int height) {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
    }

    /*
     * Rendering PDF pages to images is an intrinically heavy operation.
     * To keep the UI responsive, the work of rendering is delegated to this
     * SwingWorker.
     */
    private class BackgroundBufferLoadingWorker extends SwingWorker<BufferedImage, String> {

        private PDPage _pageToBuffer;
        private double _zoomLevel;
        private File _file;
        private int _pageNumber;
        private PDDocument _doc;

        public BackgroundBufferLoadingWorker(PDPage newPage, double zoom) {

            _pageToBuffer = checkNotNull(newPage);
            _zoomLevel = checkNotNull(zoom);
        }

        public BackgroundBufferLoadingWorker(File file, int pageNumber, double zoom) {
            _file = file;
            _pageNumber = pageNumber;
            _zoomLevel = zoom;
        }

        @Override
        protected BufferedImage doInBackground() throws Exception {
            publish("Processing PDF...");

            if (_pageToBuffer == null) {
                //RandomAccessFile raf = new RandomAccessFile(File.createTempFile("file", "another", new File(".")), "rw");
                //System.out.println("RAF Length" + raf.length());
                _doc = PDDocument.load(_file);
                _pageToBuffer = (PDPage) _doc.getDocumentCatalog().getAllPages().get(_pageNumber);
                //stream then file

                /*PDStream stream = _pageToBuffer.getContents();
                for(Field field : stream.getClass().getDeclaredFields()){
                    System.out.println("Field: " + field);
                }
                Field andStream = stream.getClass().getDeclaredField("stream");
                if(andStream != null){
                    andStream.setAccessible(true);
                    COSStream cosStream = (COSStream) andStream.get(stream);
                    Field andFile = cosStream.getClass().getDeclaredField("file");
                    andFile.setAccessible(true);
                    RandomAccessFile file = (RandomAccessFile) andFile.get(cosStream);
                    
                    Field andras = file.getClass().getDeclaredField("ras");
                    andras.setAccessible(true);
                    java.io.RandomAccessFile ras = (java.io.RandomAccessFile)andras.get(file);
                    Field andfd = ras.getClass().getDeclaredField("fd");
                    andfd.setAccessible(true);
                    FileDescriptor fd = (FileDescriptor)andfd.get(ras);
                    Field andfdi = fd.getClass().getDeclaredField("fd");
                    Field andhdi = fd.getClass().getDeclaredField("handle");
                    andfdi.setAccessible(true);
                    andhdi.setAccessible(true);
                    int fdval = andfdi.getInt(fd);
                    long handleval = andhdi.getLong(fd);
                    System.out.println("File desc " + fdval + " handle " + handleval);
                    for(Field field : fd.getClass().getDeclaredFields()){
                        System.out.println("Field 2: " + field);
                    }
                    
                    
                }*/



            }

            Dimension dim = _pageToBuffer.findMediaBox().createDimension();
            int pwidth = (int) (dim.getWidth() * _zoomLevel);
            int pheight = (int) (dim.getHeight() * _zoomLevel);
            BufferedImage renderedImage = getCompatibleImage(pwidth, pheight);
            {
                Graphics2D g = (Graphics2D) renderedImage.getGraphics();

                g.setColor(Color.WHITE);
                g.fillRect(0, 0, pwidth, pheight);
                g.scale(_zoomLevel, _zoomLevel);
                PageDrawer pageDrawer = new PageDrawer();
                pageDrawer.drawPage(g, _pageToBuffer, dim);
            }
            publish("Processing Complete!");
            return renderedImage;
        }

        @Override
        protected void process(List<String> statusMessages) {
            if (!statusMessages.isEmpty()) {
            }
        }

        @Override
        protected void done() {
            try {
                _bufferedImage = get();
                _doc.close();
                PDFBGLoadTest.this.repaint();
            } catch (IOException ex) {
                Logger.getLogger(PDFBGLoadTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(PDFPageView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(PDFPageView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

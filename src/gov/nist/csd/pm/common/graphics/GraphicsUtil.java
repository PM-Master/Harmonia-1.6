package gov.nist.csd.pm.common.graphics;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class implements Image and ImageIcon retrieval.
 * 
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:01 $
 * @since 6.0
 */
public class GraphicsUtil {

    public static Image getImage(String imagePath, Class clazz) {

        return getImage(imagePath, clazz.getClassLoader());
    }

    public static Image getImage(String imagePath, ClassLoader baseClassLoader) {
        //ClassLoader's getResourceAsStream requires a different
        //path format than that of Class.getResourceAsStream()
        if (imagePath.startsWith("/")) {
            imagePath = imagePath.substring(1);
        }
        Image image = null;
        try {
            InputStream in = baseClassLoader.getResourceAsStream(imagePath);
            if (in == null) {
                System.err.println("Warning: Cannot load image: " + imagePath);
            } else {
                BufferedInputStream is = new BufferedInputStream(in);
                image = ImageIO.read(is);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return image;
    }

    /**
     * Get an ImageIcon from a JAR file.
     * @param imagePath
     * @return
     */
    public static ImageIcon getImageIcon(String imagePath) {
        return getImageIcon(imagePath, ClassLoader.getSystemClassLoader());
    }

    public static ImageIcon getImageIcon(String imagePath, Class clazz) {
        return getImageIcon(imagePath, clazz.getClassLoader());
    }

    /**
     * Get an ImageIcon from a JAR file.
     * @param imagePath
     * @return
     */
    public static ImageIcon getImageIcon(String imagePath, ClassLoader baseClassLoader) {
        if (imagePath.startsWith("/")) {
            imagePath = imagePath.substring(1);
        }
        Image image = getImage(imagePath, baseClassLoader);
        return image != null ? new ImageIcon(image) : null;

    }

    public static void centerFrame(Frame frame) {

        //Center frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = frame.getSize();
        screenSize.height = screenSize.height / 2;
        screenSize.width = screenSize.width / 2;
        size.height = size.height / 2;
        size.width = size.width / 2;
        int y = screenSize.height - size.height;
        int x = screenSize.width - size.width;
        frame.setLocation(x, y);

    }

    public static void centerDialog(JDialog dialog) {

        //Center frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = dialog.getSize();
        screenSize.height = screenSize.height / 2;
        screenSize.width = screenSize.width / 2;
        size.height = size.height / 2;
        size.width = size.width / 2;
        int y = screenSize.height - size.height;
        int x = screenSize.width - size.width;
        dialog.setLocation(x, y);

    }

    public static int[] flip_image(int[] pixels, int width, int height){
        int[] result = new int[pixels.length];
        for(int row = 0; row < height; ++row){
            for(int col = 0; col < width; ++col){
                result[(height - 1 - row) * width + col] = pixels[row * width + col];
            }
        }
        return result;
    }

    public static Image imageFromRawData(int[] pixels, int width, int height, boolean flip) {
        if(flip){
            pixels = flip_image(pixels, width, height);
        }
        //ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        ColorModel colorModel = new DirectColorModel(32,  0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000);
        DataBuffer dataBuffer = new DataBufferInt(pixels, width * height);

        SampleModel sampleModel = colorModel.createCompatibleSampleModel(width, height);
        WritableRaster raster = WritableRaster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);
        return image;
    }
}

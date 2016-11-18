package gov.nist.csd.pm.common.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class GradientPanel extends JPanel {

    /**
	 * @uml.property  name="topColor"
	 */
    private Color topColor = Color.CYAN;
    /**
	 * @uml.property  name="bottomColor"
	 */
    private Color bottomColor = Color.BLUE;
    /**
	 * @uml.property  name="backgroundImage"
	 */
    private Image backgroundImage = null;

    public GradientPanel(Color topColor, Color bottomColor) {
        super();
        this.topColor = topColor;
        this.bottomColor = bottomColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isOpaque()) {
            super.paintComponent(g);
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, topColor, 0, h, bottomColor);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
        if(backgroundImage != null){
            Dimension imageDim = new Dimension(backgroundImage.getWidth(null), backgroundImage.getHeight(null));
            Rectangle imageRect = new Rectangle(imageDim);
            Rectangle panelRect = new Rectangle(getSize());

            AffineTransform trans = new AffineTransform();
            trans.translate(5.0, 5.0);
            trans.scale(.5, .5);
            Composite originalComp = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            g2d.drawImage(backgroundImage, trans, null);
            g2d.setComposite(originalComp);
        }
        setOpaque(false);
        super.paintComponent(g);
        setOpaque(true);
    }

    /**
	 * @return
	 * @uml.property  name="backgroundImage"
	 */
    public Image getBackgroundImage() {
        return backgroundImage;
    }

    /**
	 * @param backgroundImage
	 * @uml.property  name="backgroundImage"
	 */
    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }
}

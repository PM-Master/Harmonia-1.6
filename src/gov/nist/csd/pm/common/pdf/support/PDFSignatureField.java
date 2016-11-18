package gov.nist.csd.pm.common.pdf.support;

import javax.swing.*;
import java.awt.*;

class PDFSignatureField extends JPanel {
    private static JButton _signButton;
    private static JLabel _signatureAppearance;
    /**
	 * @uml.property  name="_signed"
	 */
    private boolean _signed = false;
    public PDFSignatureField() {
        super();
        _signButton = new JButton();
        _signatureAppearance = new JLabel();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(_signatureAppearance);
        add(Box.createHorizontalStrut(5));
        add(_signButton);
    }

    private void updateView(){
        if(this.isAncestorOf(_signButton) && _signed){
            remove(_signButton);
        }
        if(!this.isAncestorOf(_signButton) && !_signed){
            add(_signButton);
        }
        if(getParent() != null){
            getParent().repaint();
        }
    }

    public JLabel getAppearanceLabel(){
        return _signatureAppearance;
    }

    public void setAppearance(String text){
        _signatureAppearance.setText(text);

    }

    public void setAppearance(Image image){
        _signatureAppearance.setIcon(new ImageIcon(image));
    }
    public JButton getSignButton() {
        return _signButton;
    }

    public void setSigned(boolean signed){
        _signed = signed;
        updateView();
    }

    public boolean isSigned(){
        return _signed;
    }
}
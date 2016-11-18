package gov.nist.csd.pm.application.pdfviewer;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.build;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.withLayout;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 2/9/11
 * Time: 9:29 AM
 * Placeholder "about" panel.
 */
public class AboutPanel extends JDialog {

    public AboutPanel(Window owner){
        super(owner, JDialog.ModalityType.APPLICATION_MODAL);
        initGUI();
    }

    private void initGUI(){
        build(
                this.getContentPane(),
                withLayout(new MigLayout("", "", ""))
        );

    }
}

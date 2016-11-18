package gov.nist.csd.pm.common.browser;

import gov.nist.csd.pm.common.application.SysCaller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.getActiveWindow;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/11/11
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */
public final class BrowsingKit {
    private BrowsingKit(){

    }
    public static Action browseUserAction(final SysCaller syscall, final UserBrowserListener listener){
        return  new AbstractAction() {
            {
                putValue(Action.NAME, "Browse Users...");
                putValue(Action.LONG_DESCRIPTION, "Browse all users in the Policy Machine System.");
                putValue(Action.SHORT_DESCRIPTION, "Browse all users");
            }
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                UserBrowser ub = new UserBrowser(null, syscall, getValue(Action.NAME).toString());
                ub.addUserBrowserListener(listener);

                setupBrowserDialog(actionEvent, ub);
            }
        };
    }

    private static void setupBrowserDialog(ActionEvent actionEvent, JDialog browserDialog) {
        Object source = actionEvent.getSource();
        Window win = source instanceof Component ? SwingUtilities.getWindowAncestor((Component)source) : null;
        win = win != null ? win : getActiveWindow();
        browserDialog.pack();
        browserDialog.setLocationRelativeTo(win);
        browserDialog.setVisible(true);
    }


    public static Action browseObjectAction(final SysCaller syscall, final ObjectBrowserListener listener){
        return  new AbstractAction() {
            {
                putValue(Action.NAME, "Browse Users...");
                putValue(Action.LONG_DESCRIPTION, "Browse all users in the Policy Machine System.");
                putValue(Action.SHORT_DESCRIPTION, "Browse all users");
            }
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ObjectBrowser ob = new ObjectBrowser(null, syscall, getValue(Action.NAME).toString());
                ob.addObjectBrowserListener(listener);
                setupBrowserDialog(actionEvent, ob);

            }
        };
    }
}

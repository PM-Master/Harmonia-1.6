package gov.nist.csd.pm.common.graphics;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionEvent;

public class MenuToggleButton extends JToggleButton {

    public MenuToggleButton() {
        this("", null);
    }

    public MenuToggleButton(Icon icon) {
        this("", icon);
    }

    public MenuToggleButton(String text) {
        this(text, null);
    }

    public MenuToggleButton(String text, Icon icon) {
        super();
        Action a = new AbstractAction(text) {

            @Override
            public void actionPerformed(ActionEvent ae) {
                MenuToggleButton b = (MenuToggleButton) ae.getSource();
                if (pop != null) {
                    pop.show(b, 0, -(int)pop.getPreferredSize().getHeight());
                }
            }
        };
        a.putValue(Action.SMALL_ICON, icon);
        setAction(a);
        setFocusable(false);
        //Border currentBorder = getBorder();
        //setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    }
    /**
	 * @uml.property  name="pop"
	 * @uml.associationEnd  
	 */
    protected JPopupMenu pop;

    public void setPopupMenu(final JPopupMenu pop) {
        this.pop = pop;
        pop.addPopupMenuListener(new PopupMenuListener() {

            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                setSelected(false);
            }
        });
    }
}

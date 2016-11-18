/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.application.grantor;

import gov.nist.csd.pm.common.util.swing.SwingShortcuts;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Administrator
 */

/*DOUBLE CLICK TAB TO SHOW SOURCE IN SEPARATE WINDOW!
===================================================

JTabbedPane tabbedPane = new JTabbedPane();

LC layC = new LC().fill().wrap();
AC colC = new AC().align("right", 1).fill(2, 4).grow(100, 2, 4).align("right", 3).gap("15", 2);
AC rowC = new AC().align("top", 7).gap("15!", 6).grow(100, 8);

JPanel panel = createTabPanel(new MigLayout(layC, colC, rowC));    // Makes the background gradient

// References to text fields not stored to reduce code clutter.

JScrollPane list2 = new JScrollPane(new JList(new String[] {"Mouse, Mickey"}));
panel.add(list2,                     new CC().spanY().growY().minWidth("150").gapX(null, "10"));

panel.add(new JLabel("Last Name"));
panel.add(new JTextField());
panel.add(new JLabel("First Name"));
panel.add(new JTextField(),          new CC().wrap().alignX("right"));
panel.add(new JLabel("Phone"));
panel.add(new JTextField());
panel.add(new JLabel("Email"));
panel.add(new JTextField());
panel.add(new JLabel("Address 1"));
panel.add(new JTextField(),          new CC().spanX().growX());
panel.add(new JLabel("Address 2"));
panel.add(new JTextField(),          new CC().spanX().growX());
panel.add(new JLabel("City"));
panel.add(new JTextField(),          new CC().wrap());
panel.add(new JLabel("State"));
panel.add(new JTextField());
panel.add(new JLabel("Postal Code"));
panel.add(new JTextField(10),        new CC().spanX(2).growX(0));
panel.add(new JLabel("Country"));
panel.add(new JTextField(),          new CC().wrap());

panel.add(new JButton("New"),        new CC().spanX(5).split(5).tag("other"));
panel.add(new JButton("Delete"),     new CC().tag("other"));
panel.add(new JButton("Edit"),       new CC().tag("other"));
panel.add(new JButton("Save"),       new CC().tag("other"));
panel.add(new JButton("Cancel"),     new CC().tag("cancel"));

tabbedPane.addTab("Layout Showdown (improved)", panel);
 */

public class GrantorMainView extends JPanel {

    /**
	 * @uml.property  name="layout"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    MigLayout layout = new MigLayout("","[fill, grow]","[fill, grow]");
    /**
	 * @uml.property  name="masterTable"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTable masterTable;
    /**
	 * @uml.property  name="detailView"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextArea detailView;

    public GrantorMainView() {
        buildGUI();
    }

    final public void buildGUI() {
        this.setLayout(layout);
        masterTable = new JTable();
        JScrollPane masterTableScroller = SwingShortcuts.embedInJScrollPane(getMasterTable());
        
        //this.add(masterTableScroller, "width 400:700:, height 50:150:, wrap");
        masterTableScroller.setBorder(
                BorderFactory.createTitledBorder("Messages")
                );
        masterTableScroller.setMinimumSize(new Dimension(400, 185));
        masterTableScroller.setPreferredSize(new Dimension(700, 250));
        detailView = new JTextArea();
        JScrollPane detailViewScroller = SwingShortcuts.embedInJScrollPane(getDetailView());
        detailViewScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        detailViewScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        detailViewScroller.setMinimumSize(new Dimension(400, 300));
        detailViewScroller.setPreferredSize(new Dimension(700, 400));
        this.add(SwingShortcuts.embedInJSplitPane(masterTableScroller, detailViewScroller, SwingShortcuts.Split.VERTICAL));
    }

    public static void main(String[] args) {
        System.out.println("going this way");
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                JFrame frame = SwingShortcuts.displayInJFrame(new GrantorMainView(), "Test Window");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

    /**
	 * @return  the masterTable
	 * @uml.property  name="masterTable"
	 */
    public JTable getMasterTable() {
        return masterTable;
    }

    /**
	 * @return  the detailView
	 * @uml.property  name="detailView"
	 */
    public JTextArea getDetailView() {
        return detailView;
    }

    
}

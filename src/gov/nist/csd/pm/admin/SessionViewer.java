/*
 * SessionViewer.java
 *
 * Created on August 12, 2005, 11:29 AM
 * Serban Gavrila
 * KTC Inc.
 */
package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class SessionViewer extends JDialog implements ActionListener, ListSelectionListener {

    /**
	 * @uml.property  name="tool"
	 * @uml.associationEnd  
	 */
    private PmAdmin tool;
    /**
	 * @uml.property  name="sslClient"
	 * @uml.associationEnd  
	 */
    private SSLSocketClient sslClient;
    /**
	 * @uml.property  name="sessJList"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JList sessJList;                   // Session list and
    /**
	 * @uml.property  name="sessListModel"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
    private DefaultListModel sessListModel;   // model.
    /**
	 * @uml.property  name="sessionList"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
    private List<String> sessionList;
    /**
	 * @uml.property  name="sessField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextField sessField;             // Selected session name and id.
    /**
	 * @uml.property  name="userField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextField userField;             // Session user.
    /**
	 * @uml.property  name="hostField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextField hostField;             // Session host.
    /**
	 * @uml.property  name="attrList"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JList attrList;                      // Session's active attributes list and
    /**
	 * @uml.property  name="attrListModel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private DefaultListModel attrListModel;      // model.
    /**
	 * @uml.property  name="capList"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JList capList;                       // Session's capabilities list and
    /**
	 * @uml.property  name="capListModel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private DefaultListModel capListModel;       // model.
    /**
	 * @uml.property  name="closeButton"
	 * @uml.associationEnd  readOnly="true"
	 */
    private JButton closeButton;                 // The close button.
    /**
	 * @uml.property  name="constraints"
	 */
    private GridBagConstraints constraints = new GridBagConstraints();
    /**
	 * @uml.property  name="attrTimer"
	 * @uml.associationEnd  
	 */
    private Timer attrTimer;

    public SessionViewer(PmAdmin tool, SSLSocketClient sslClient) {
        super(tool, false);  // non-modal

        this.tool = tool;
        this.sslClient = sslClient;

        setTitle("Session Viewer");

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                close();
            }
        });

        // Start building the GUI.
        JPanel sessPane = new JPanel();
        sessPane.setLayout(new GridBagLayout());

        JLabel sessListLabel = new JLabel("Current Sessions:");
        sessListModel = new DefaultListModel();
        sessJList = new JList(sessListModel);
        sessJList.addListSelectionListener(this);
        JScrollPane sessListScrollPane = new JScrollPane(sessJList);
        sessListScrollPane.setPreferredSize(new Dimension(360, 160));

        JLabel sessLabel = new JLabel("Session:");
        sessField = new JTextField(30);
        JLabel userLabel = new JLabel("User:");
        userField = new JTextField(30);
        JLabel hostLabel = new JLabel("Host:");
        hostField = new JTextField(30);

        addComp(sessPane, sessListLabel, 0, 0, 1, 1);
        addComp(sessPane, sessListScrollPane, 0, 1, 4, 4);

        constraints.insets = new Insets(0, 10, 0, 0);

        addComp(sessPane, sessLabel, 4, 1, 1, 1);
        addComp(sessPane, userLabel, 4, 2, 1, 1);
        addComp(sessPane, hostLabel, 4, 3, 1, 1);

        constraints.insets = new Insets(0, 0, 0, 0);

        addComp(sessPane, sessField, 5, 1, 3, 1);
        addComp(sessPane, userField, 5, 2, 3, 1);
        addComp(sessPane, hostField, 5, 3, 3, 1);

        sessPane.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));

        JPanel lowerPane = new JPanel();
        lowerPane.setLayout(new GridBagLayout());

        JLabel attrListLabel = new JLabel("Active Attributes:");
        attrListModel = new DefaultListModel();
        attrList = new JList(attrListModel);
        JScrollPane attrListScrollPane = new JScrollPane(attrList);
        attrListScrollPane.setPreferredSize(new Dimension(360, 160));

        JLabel capListLabel = new JLabel("Capabilities:");
        capListModel = new DefaultListModel();
        capList = new JList(capListModel);
        JScrollPane capListScrollPane = new JScrollPane(capList);
        capListScrollPane.setPreferredSize(new Dimension(360, 160));

        addComp(lowerPane, attrListLabel, 0, 0, 1, 1);
        addComp(lowerPane, attrListScrollPane, 0, 1, 4, 4);

        constraints.insets = new Insets(0, 20, 0, 0);

        addComp(lowerPane, capListLabel, 4, 0, 1, 1);
        addComp(lowerPane, capListScrollPane, 4, 1, 4, 4);

        lowerPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(sessPane, BorderLayout.NORTH);
        contentPane.add(lowerPane, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void addComp(Container container, Component component, int x, int y, int w, int h) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        container.add(component, constraints);
    }

    public void prepare() {
        Packet res = null;

        // Some interface cleaning.
        sessField.setText("");
        userField.setText("");
        hostField.setText("");

        attrListModel.clear();

        // Get all sessions. As the last step, the getSessions function of the engine
        // must empty the session event store.
        try {
            Packet cmd = tool.makeCmd("getSessions");
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(tool, "Undetermined error, null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(tool, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(tool, e.getMessage());
            return;
        }
        sessListModel.clear();
        if (sessionList == null) {
            sessionList = new ArrayList<String>();
        } else {
            sessionList.clear();
        }
        for (int i = 0; i < res.size(); i++) {
            String sLine = res.getStringValue(i);
            String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
            sessListModel.addElement(pieces[0]);
            sessionList.add(pieces[1]);
        }
        printVector(sessionList, "after prepare");

        attrTimer = new Timer(3000, this);
        attrTimer.start();
    }

    private int getBinSrcIndex(DefaultListModel model, String target) {
        int high = model.size(), low = -1, probe;
        while (high - low > 1) {
            probe = (high + low) / 2;
            if (target.compareToIgnoreCase((String) model.get(probe)) < 0) {
                high = probe;
            } else {
                low = probe;
            }
        }
        return (low + 1);
    }

    private void close() {
        if (attrTimer != null) {
            attrTimer.stop();
        }
        this.setVisible(false);
    }


    /**
	 * @uml.property  name="refreshGui"
	 */
//    Runnable refreshGui = new Runnable() {
//
//        @Override
//        public void run() {
//            processAttrTimerEvent();
//        }
//    };

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
//        if (src.equals(attrTimer)) {
//            //processAttrTimerEvent();
//            SwingUtilities.invokeLater(refreshGui);
//            return;
//        }

        if (e.getActionCommand().equalsIgnoreCase("add")) {
        } else if (e.getActionCommand().equalsIgnoreCase("refresh")) {
        } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
        } else if (e.getActionCommand().equalsIgnoreCase("close")) {
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        // Get the selected session, which can be null.
        int index = sessJList.getSelectedIndex();
        if (index < 0) {
            return;
        }

        String sSessId = (String) sessionList.get(index);
        String sSessName = (String) sessListModel.get(index);
        printVector(sessionList, "when a session is clicked");
        sessField.setText(sSessName);

        Packet res = null;
        try {
            Packet cmd = tool.makeCmd("getSessionInfo", sSessId);
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(this, exc.getMessage());
            attrTimer.stop();
            return;
        }
        // The information returned by getSessionInfo has the following format:
        // item 0: <sess name>:<sess id>
        // item 1: <user name>:<user id>
        // item 2: <host name>:<host id>
        // items 3 through 3 + active_attr_count - 1: <attr name>:<attr id>
        String sLine = res.getStringValue(1);
        String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
        userField.setText(pieces[0]);

        sLine = res.getStringValue(2);
        pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
        hostField.setText(pieces[0]);

        attrListModel.clear();
        for (int i = 3; i < res.size(); i++) {
            sLine = res.getStringValue(i);
            pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
            attrListModel.addElement(pieces[0]);
        }
    }

    private void printVector(List<String> v, String cap) {
        System.out.println(cap);
        for (int i = 0; i < v.size(); i++) {
            System.out.println("[[[[[[[" + i + ", " + (String) v.get(i) + "]]]]]]]");
        }
    }
}

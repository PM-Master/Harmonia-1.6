package gov.nist.csd.pm.common.browser;

import gov.nist.csd.pm.common.application.SysCaller;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class ObjectBrowser extends JDialog implements ActionListener {

    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_OK = "ok";
    public static final String BROWSER_MODE_OPEN_TITLE = "Open";
    public static final String BROWSER_MODE_SAVE_AS_TITLE = "Save As";
    public static final String PM_PROP_DELIM = "=";
    public static final String PM_FIELD_DELIM = ":";
    public static final String PM_ALT_FIELD_DELIM = "|";
    public static final String PM_ALT_DELIM_PATTERN = "\\|";
    public static final String PM_TERMINATOR = ".";
    public static final String PM_LIST_MEMBER_SEP = ",";
    public static final String PM_GRAPH_UATTR = "ua";
    public static final int PM_OK = 1;
    public static final int PM_CANCEL = 0;
    /**
	 * @uml.property  name="objectTree"
	 * @uml.associationEnd  
	 */
    private PmGraph objectTree;
    /**
	 * @uml.property  name="connectorNode"
	 * @uml.associationEnd  
	 */
    private PmNode connectorNode;
    /**
	 * @uml.property  name="selectedNode"
	 * @uml.associationEnd  
	 */
    private PmNode selectedNode;
    /**
	 * @uml.property  name="objField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextField objField;
    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    private SysCaller sysCaller;
    //private MessageController messenger;
    /**
	 * @uml.property  name="obListeners"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.browser.ObjectBrowserListener"
	 */
    private List<ObjectBrowserListener> obListeners = new ArrayList<ObjectBrowserListener>();
    /**
	 * @uml.property  name="sMode"
	 * @uml.associationEnd  
	 */
    private BrowserMode sMode = BrowserMode.OPEN; // Open or Save As?
    /**
	 * @uml.property  name="userChoice"
	 */
    private int userChoice;
    /**
	 * @uml.property  name="selectedName"
	 */
    String selectedName;
    /**
	 * @uml.property  name="appName"
	 */
    private String appName = "ObjectBrowser";
    //private BrowserConfig browserConfig = BrowserConfig.SHOW_SELECTED_OBJECT;

    /**
	 * @author   Administrator
	 */
    public enum BrowserMode {

        /**
		 * @uml.property  name="sAVE_AS"
		 * @uml.associationEnd  
		 */
        SAVE_AS(BROWSER_MODE_SAVE_AS_TITLE), /**
		 * @uml.property  name="oPEN"
		 * @uml.associationEnd  
		 */
        OPEN(BROWSER_MODE_OPEN_TITLE);

        public String title() {
            return title;
        }
        private String title;

        private BrowserMode(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    /**
	 * @author   Administrator
	 */
    public enum BrowserConfig {

        /**
		 * @uml.property  name="sHOW_SELECTED_OBJECT"
		 * @uml.associationEnd  
		 */
        SHOW_SELECTED_OBJECT, /**
		 * @uml.property  name="hIDE_SELECTED_OBJECT"
		 * @uml.associationEnd  
		 */
        HIDE_SELECTED_OBJECT
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public ObjectBrowser(Window owner, SysCaller sysCaller, String appName) {
        this(owner, sysCaller, appName, BrowserConfig.SHOW_SELECTED_OBJECT);
    }

    public ObjectBrowser(Window owner, SysCaller sysCaller, String appName, BrowserConfig browserConfig) {
        super(owner, ModalityType.APPLICATION_MODAL);  // modal
        this.sysCaller = sysCaller;
        this.appName = appName;
        setTitle("Select object/object container");
        sysCaller.getConnector();
        String[] connectorInfo = new String[]{
                SysCaller.PM_NODE_CONN,
                SysCaller.PM_CONNECTOR_ID,
                SysCaller.PM_CONNECTOR_NAME
        };
        connectorNode = new PmNode(connectorInfo, new PmNodeChildDelegate(sysCaller, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));//PmGraphDirection.USER, PmGraphType.USER));
        objectTree = new PmGraph(connectorNode, true);

        objectTree.addTreeSelectionListener(new PmTreeListener());

        JScrollPane treeScrollPane = new JScrollPane(objectTree);
        treeScrollPane.setPreferredSize(new Dimension(400, 250));
        objField = new JTextField(20);
        JComponent upperPane = treeScrollPane;
        if (browserConfig == BrowserConfig.SHOW_SELECTED_OBJECT) {
            JLabel objLabel = new JLabel("Object name:");
            Container box = Box.createHorizontalBox();
            box.add(objLabel);
            box.add(objField);
            JPanel boxPane = new JPanel();
            boxPane.add(box);
            boxPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            upperPane = new JPanel();
            upperPane.setLayout(new BorderLayout());
            upperPane.add(treeScrollPane, BorderLayout.CENTER);
            upperPane.add(boxPane, BorderLayout.SOUTH);

        }


        JButton okButton = new JButton("OK");
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        JPanel buttonPane = new JPanel();
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(upperPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);
    }

    

    public void addObjectBrowserListener(ObjectBrowserListener obl) {
        obListeners.add(obl);
    }

    public int showOpenDialog() {
        return showDialog(BrowserMode.OPEN);
    }

    public void removeObjectBrowserListener(ObjectBrowserListener obl) {
        obListeners.remove(obl);
    }
    
    public void disableObjField(){
    	objField.setEnabled(false);
    }
    
    public void insertObjName(String input){
    	objField.setText(input);
    }

    public int showDialog(BrowserMode mode) {
        selectedName = null;
        objField.setText(null);
        sMode = mode;
        setTitle(String.format("%s - %s", appName, mode));
        setVisible(true);
        return userChoice;
    }

    public int showSaveAsDialog() {
        return showDialog(BrowserMode.SAVE_AS);
    }

    public String getObjName() {
        return objField.getText();
    }

    public String getContainers() {
        return selectedNode == null ? "" : selectedNode.getType() + PM_ALT_FIELD_DELIM + selectedNode.getName();
    }

    private void doOk() {
        if (sMode.equals(BrowserMode.SAVE_AS)) {
            TreePath selPath = objectTree.getSelectionPath();
            if (selPath == null) {
                JOptionPane.showMessageDialog(this, "Please select a container or more (object attributes)!");
                return;
            }

            PmNode selNode = (PmNode) selPath.getLastPathComponent();
            if (!selNode.getType().equalsIgnoreCase(SysCaller.PM_NODE_OATTR)) {
                JOptionPane.showMessageDialog(this, "Selected node " + selNode.getName() + " is not a valid container (object attribute)!");
                return;
            }


        } else if (sMode.equals(BrowserMode.OPEN)) {
            if (selectedNode == null) {
                JOptionPane.showMessageDialog(this, "Please select an object or enter its name!");
                return;
            }
        }
        for (ObjectBrowserListener obl : obListeners) {
            obl.pmNodeSelected(selectedNode);
        }

        userChoice = PM_OK;
        setVisible(false);
    }

    private void doCancel() {
        userChoice = PM_CANCEL;
        setVisible(false);
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ACTION_OK)) {
            doOk();
        } else if (e.getActionCommand().equals(ACTION_CANCEL)) {
            doCancel();
        }
    }

    /** Capture mouse events on the tree.  If a node is double clicked,
     * we call createNodes() to create two levels of the tree from the
     * double-clicked node.  This will have to be changed to call the policy
     * machine mechanism for getting descendants or ancestors.
     */
    class PmTreeListener implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent tse) {
            TreePath selPath = tse.getPath();
            if (selPath != null) {
                PmNode node = (PmNode) selPath.getLastPathComponent();
                selectedNode = node;
                System.out.println(selectedNode);
                if (sMode.equals(BrowserMode.OPEN))
                {
                	System.out.println("((((()))))SELECT(((()))((())))");
                    objField.setText(selectedNode.getName());
                }
            }
        }
    }
}

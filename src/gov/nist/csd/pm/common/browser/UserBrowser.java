package gov.nist.csd.pm.common.browser;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.util.Log;

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
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:01 $
 * @since 1.5
 */
public class UserBrowser extends JDialog implements ActionListener {
	
	Log log = new Log(Log.Level.INFO, true);

    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_OK = "ok";
    public static final String MSG_SELECT_NODE = "Please select a node!";
    public static final String MSG_SELECT_USER = "%s - Please select a user or user attribute!";

    public static final String PM_PROP_DELIM = "=";
    public static final String PM_FIELD_DELIM = ":";
    public static final String PM_ALT_FIELD_DELIM = "|";
    public static final String PM_ALT_DELIM_PATTERN = "\\|";
    public static final String PM_TERMINATOR = ".";
    public static final String PM_LIST_MEMBER_SEP = ",";
    public static final String PM_GRAPH_UATTR = "ua";
    public static final String TITLE_CANCEL = "Cancel";
    public static final String TITLE_OK = "OK";
    /**
	 * @uml.property  name="userTree"
	 * @uml.associationEnd  
	 */
    private PmGraph userTree;
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
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    private SysCaller sysCaller;
    /**
	 * @uml.property  name="ubListeners"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.browser.UserBrowserListener"
	 */
    private List<UserBrowserListener> ubListeners = new ArrayList<UserBrowserListener>();

    @SuppressWarnings("LeakingThisInConstructor")
    public UserBrowser(Window owner, SysCaller sysCaller, String appName) {
        super(owner, ModalityType.APPLICATION_MODAL);  // modal
        this.sysCaller = sysCaller;

        setTitle(String.format(MSG_SELECT_USER, appName));

        String[] connectorData = sysCaller.getConnector();
    	log.debugStackCall("!!!!!!! getMembersOf - trace 31");	

        connectorNode = new PmNode(connectorData, new PmNodeChildDelegate(sysCaller, PmGraphDirection.UP, PmGraphType.USER_ATTRIBUTES));
        userTree = new PmGraph(connectorNode, false);

        userTree.addTreeSelectionListener(new PmTreeSelectionListener());

        JScrollPane treeScrollPane = new JScrollPane(userTree);
        treeScrollPane.setPreferredSize(new Dimension(400, 250));

        JButton okButton = new JButton(TITLE_OK);
        okButton.addActionListener(this);
        JButton cancelButton = new JButton(TITLE_CANCEL);
        cancelButton.addActionListener(this);
        JPanel buttonPane = new JPanel();
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);

        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(treeScrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);
    }

    public void addUserBrowserListener(UserBrowserListener listener) {
        ubListeners.add(listener);
    }

    public void removeUserBrowserListener(UserBrowserListener listener) {
        ubListeners.remove(listener);
    }

    public void removeAllUserBrowserListeners(){
        ubListeners.removeAll(ubListeners);
    }



    private void doOk() {
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, MSG_SELECT_NODE);
            return;
        }
        for (UserBrowserListener listener : ubListeners) {
            listener.userSelected(selectedNode);
            this.setVisible(false);
        }
        
        this.setVisible(false);
    }

    private void doCancel() {
        this.setVisible(false);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase(ACTION_OK)) {
            doOk();
        } else if (e.getActionCommand().equalsIgnoreCase(ACTION_CANCEL)) {
            doCancel();
        }
    }

    /** Capture mouse events on the tree.  If a node is double clicked,
     * we call createNodes() to create two levels of the tree from the
     * double-clicked node.  This will have to be changed to call the policy
     * machine mechanism for getting descendants or ancestors.
     *
     * REM - This class has been changed to a TreeSelectionListener
     * It leaves the mouse listening to the JTree and relies on the
     * selection events to update the selectedNode value.
     *
     * Updating the expanded nodes is still handled by the tree expansion listener
     * and was redundant in the the mouse listener.
     */
    class PmTreeSelectionListener implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent tse) {
            TreePath path = tse.getPath();
            if(path != null){
                selectedNode = (PmNode) path.getLastPathComponent();
            }
        }
    }
}

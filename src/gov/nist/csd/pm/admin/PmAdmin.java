package gov.nist.csd.pm.admin;


/*
 * Title:        PmAdmin.java
 * Description:  The admin client part of the Policy Machine.
 * Created March 2005 for VDG Inc.
 * Updated August 2005 - October 2006 for KT Consulting Inc.
 * Author Serban I. Gavrila
 */

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;

import com.google.common.base.Throwables;

import gov.nist.csd.pm.application.schema.builder.SchemaBuilder3;
import gov.nist.csd.pm.application.schema.tableeditor.TableEditor;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.browser.*;
import gov.nist.csd.pm.common.graphics.GraphicsUtil;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;
import gov.nist.csd.pm.common.util.Log;
import gov.nist.csd.pm.common.util.swing.DialogUtils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static gov.nist.csd.pm.common.info.PMCommand.*;
import static gov.nist.csd.pm.common.net.LocalHostInfo.getLocalHost;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import static gov.nist.csd.pm.common.util.CommandUtil.makeCmd;
import static gov.nist.csd.pm.common.util.collect.Arrays.isNullOrEmpty;


//import javax.swing.text.*;
/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class PmAdmin extends JFrame implements ActionListener, TreeExpansionListener{
	
	Log log = new Log(Log.Level.INFO, true);


    public static final String PM_BASE_ROLE = "rbac";
    public static final String PM_SUPER_USER = "super";
    public static final String PM_SUPER_ROLE = "SuperAdmins";
    public static final String PM_SGRAPH_CLASS = "Subgraph";
    public static final String PM_FILE_CLASS = "File";
    public static final String PM_DIR_CLASS = "Directory";
    public static final String PM_CLASS_CLASS = "class";
    private static final String HOST = "localhost";
    public static final String PM_FAILURE = "err ";
    public static final String PM_SUCCESS = "ok  ";
    public static final String PM_CMD = "cmd ";
    public static final String PM_EOC = "eoc ";
    public static final String PM_ARG = "arg ";
    public static final String PM_SEP = "sep ";
    public static final String PM_DATA = "data";
    public static final String PM_EOD = "eod ";
    public static final String PM_BYE = "bye ";
    public static final String PM_PROP_DELIM = "=";
    public static final String PM_FIELD_DELIM = ":";
    public static final String PM_ALT_FIELD_DELIM = "|";
    public static final String PM_ALT_DELIM_PATTERN = "\\|";
    public static final String PM_TERMINATOR = ".";
    public static final String PM_LIST_MEMBER_SEP = ",";
    public static final String PM_IMPORT_COMMENT_START = "#";
    public static final String PM_CONNECTOR_ID = "1";
    public static final String PM_CONNECTOR_NAME = "PM";
    // Show descendants == up.
    // Show ancestors == down.
    public static final String PM_DIRECTION_UP = "up";
    public static final String PM_DIRECTION_DOWN = "down";

    public static final String PM_NODE_USER = "u";
    public static final String PM_NODE_USERA = "U";
    public static final String PM_NODE_UATTR = "a";
    public static final String PM_NODE_UATTRA = "A";
    public static final String PM_NODE_POL = "p";
    public static final String PM_NODE_POLA = "P";
    public static final String PM_NODE_OATTR = "b";
    public static final String PM_NODE_OATTRA = "B";
    public static final String PM_NODE_ASSOC = "o";
    public static final String PM_NODE_ASSOCA = "O";
    public static final String PM_NODE_OPSET = "s";
    public static final String PM_NODE_OPSETA = "S";
    public static final String PM_NODE_CONN = "c";
    public static final String PM_NODE_CONNA = "C";
    public static final String PM_NODE_UATTR_MEMBERS = "ma";
    public static final String PM_NODE_OATTR_MEMBERS = "mb";
    public static final String PM_NODE_POL_MEMBERS = "mp";
    public static final String PM_NODE_CONN_MEMBERS = "mc";
    public static final String PM_NODE_OPSET_MEMBERS = "ms";
    public static final String PM_ARC = "r";
 
    private String serverHostName;
 
    private int serverPortNumber;
 
    private String sessionId;
 
    private String sessionName;
 
    private boolean fakeSession = false;
 
    private SSLSocketClient sslClient = null;
 
    private String sCurrentClient = null;

    private PolicyClassEditor pcEditor = null;
  
    private UserAttrEditor uattrEditor = null;
 
    private UserEditor userEditor = null;
 
    private ObjAttrEditor oattrEditor = null;
 
    private ObjClassEditor ocEditor = null;
 
    private HostEditor hostEditor = null;
 
    private ObjEditor objEditor = null;
 
    private OpsetEditor opsetEditor = null;
 
    private CommandEditor cmdEditor = null;
 
//    private SacEditor sacEditor = null;
 
    private SessionViewer sessionViewer = null;
 
    private DenyEditor denyEditor = null;
 
    private RuleEditor ruleEditor = null;
 
    private PermEditor permEditor = null;
 
    private TaskEditor taskEditor = null;
 
//    private SconEditor sconEditor = null;
 
//    private SconaEditor sconaEditor = null;
 
    private EmlAcctEditor emlAcctEditor = null;
 
    private PropertyEditor propertyEditor = null;
    
    // For graph display
    private PmGraph tree;
 
    private PmNodeChildDelegate childDelegate;
 
    private PmNode root;
  
    private MouseListener mouseListener;
 
    private PmNode rightClickedNode;
 
    private PmNode markedNode;
 
    private JLabel viewLabel;
 
    private JLabel clientLabel;
  
    private JPopupMenu pmPopup = null; // popup menu for the global Pm view
 
    boolean debugFlag;
  
    GridBagConstraints constraints = new GridBagConstraints();
    
    // The Admin Tool may be invoked from a simulator session with the session
    // id passed on the command line (by using the -session option), or
    // directly. In the latter case, the Admin Tool should open a session
    // for the user that invoked it, on the target system where the user
    // invoked it.
    @SuppressWarnings({"CallToThreadDumpStack", "LeakingThisInConstructor"})
    public PmAdmin(String title, boolean debug, String host, int port, String session,
            String entity, String type) {
        super(title);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //setIconImage(new ImageIcon("images/nist.gif").getImage());
        /// (steveq) Acquire image from JAR file.
        setIconImage(GraphicsUtil.getImage("/images/nist.gif", getClass()));

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                terminate(0);
            }
        });

        debugFlag = debug;

        if (host == null || host.length() == 0) {
            this.serverHostName = HOST;
        } else {
            this.serverHostName = host;
        }

        if (port < 1024) {
            this.serverPortNumber = PM_DEFAULT_SERVER_PORT;
        } else {
            this.serverPortNumber = port;
        }

        this.sessionId = session;
        System.out.println("PmAdmin opening on port: " + serverPortNumber);


        //printProperties();

        // Prepare keystore password.
        JPasswordField ksPassField = new JPasswordField();
        String ksMessage = "Your keystore password:";
        char[] ksPass = null;

        // Comment out if you want to enter the password on the command line.
        /*
        while (true) {
        int result = JOptionPane.showOptionDialog(null,
        new Object[] {ksMessage, ksPassField},
        "Password", JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (result == JOptionPane.CANCEL_OPTION) System.exit(1);
        ksPass = ksPassField.getPassword();
        if (ksPass != null && ksPass.length > 0) break;
        }*/
        // up to here.
         

        ksPass = "aaaaaa".toCharArray();

        // Set up the SSL client part of the PM and check the connection
        try {
            System.setProperty("javax.net.ssl.keyStorePassword", new String(ksPass));
            sslClient = new SSLSocketClient(serverHostName, serverPortNumber, debugFlag, "ADMIN");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please check whether the PM engine has been started!\n"
                    + "(Unable to create SSL socket for " + serverHostName + ":" + serverPortNumber + ")");
            e.printStackTrace();
            terminate(-1);
        }

        Packet res = (Packet) checkPmConnection();
        if (res.hasError()) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server: "
                    + res.getErrorMessage());
            terminate(-2);
        }

        sCurrentClient = res.getStringValue(0);

        // If the Admin Tool was invoked from a session, compare the session's user
        // to the client name as provided in the SSL certificate. If they match,
        // continue. Otherwise display a message and terminate.
        // If Admin Tool wasn't invoked from a Simulator session, open one
        // for the user sCurrentClient, but first ask for password.
        if (sessionId != null) {
            String sSessUser = getSessionUser(sessionId);
            if (sSessUser == null || !sSessUser.equals(sCurrentClient)) {
                JOptionPane.showMessageDialog(this, "Wrong session id or user");
                terminate(-3);
            }
        } else {
            //doLoginSuper();
            doLogin(sCurrentClient);
            fakeSession = true;
        }
        sessionName = getSessionName(sessionId);
        if (sessionName == null) {
            JOptionPane.showMessageDialog(this, "Wrong session id");
            terminate(-4);
        }

        //==========================================================

        // Build the initial tree, showing only the connector.
        // Attach the listeners to it.//@@@
        String[] connectorData = getConnector();
        
    	log.debugStackCall("!!!!!!! getMembersOf - trace 34");	

        childDelegate = new PmNodeChildDelegate(sslClient, sessionId == null ? "" : sessionId, PmGraphDirection.UP, PmGraphType.USER_ATTRIBUTES);
        root = new PmNode(connectorData, childDelegate);
        tree = new PmGraph(root, true);

        mouseListener = new PmMouseListener();
        tree.addMouseListener(mouseListener);
        tree.addTreeExpansionListener(this);
        ToolTipManager.sharedInstance().registerComponent(tree);

        // Build the GUI.
        JToolBar toolBar = new JToolBar(null);
        toolBar.setFloatable(false);
        addButtons(toolBar);

        JPanel toolBarPanel = new JPanel();
        toolBarPanel.setLayout(new BorderLayout());
        ImageIcon userImageIcon =
                GraphicsUtil.getImageIcon("/images/common/users/admin.gif", getClass());

        JLabel userImageLabel = new JLabel(userImageIcon);
        userImageLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        int maxButtonSize = 30;
        userImageLabel.setPreferredSize(new Dimension(maxButtonSize, maxButtonSize));
        userImageLabel.setMaximumSize(new Dimension(maxButtonSize, maxButtonSize));
        toolBarPanel.add(toolBar, BorderLayout.WEST);
        toolBarPanel.add(userImageLabel, BorderLayout.EAST);

        JScrollPane treeScrollPane = new JScrollPane(tree);
        treeScrollPane.setPreferredSize(new Dimension(400, 500));

        viewLabel = new JLabel("Now viewing: " + getViewName());
        clientLabel = new JLabel("Session: " + sessionName);
        JPanel labelPane = new JPanel();
        labelPane.setLayout(new BorderLayout());
        labelPane.add(viewLabel, BorderLayout.WEST);
        labelPane.add(clientLabel, BorderLayout.EAST);
        labelPane.setBorder(new BevelBorder(BevelBorder.LOWERED));

        JPanel labelAndScrollPane = new JPanel();
        labelAndScrollPane.setLayout(new BorderLayout());
        labelAndScrollPane.add(labelPane, BorderLayout.NORTH);
        labelAndScrollPane.add(treeScrollPane, BorderLayout.CENTER);
        //labelAndScrollPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));


        JPanel contentPane = (JPanel) getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(toolBarPanel, BorderLayout.NORTH);
        contentPane.add(labelAndScrollPane, BorderLayout.CENTER);

        //==========================================================

        // The Menu bar.
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // The File menu.
        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("Import configuration...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

// Commented out by Gopi - 12/16/13
// Commented out unused menu item          
//        menuItem = new JMenuItem("Import configuration (in block)...");
//        menuItem.addActionListener(this);
//        menu.add(menuItem);

        menuItem = new JMenuItem("Export configuration...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

// Commented out by Gopi - 12/16/13
// Commented out unused menu item          
//        menuItem = new JMenuItem("Test export");
//        menuItem.addActionListener(this);
//        menu.add(menuItem);

        menuItem = new JMenuItem("Migrate to PM objects...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Exit", KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, ActionEvent.CTRL_MASK));

        menuItem.addActionListener(this);
        menu.add(menuItem);

        // The View menu.
        menu = new JMenu("View");
        menuBar.add(menu);

        menuItem = new JMenuItem("Change direction");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Users/Attributes");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Objects/Attributes");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Users/Attributes with capabilities");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Objects/Attributes with ACEs");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Sessions...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // The Operations menu.
        menu = new JMenu("Operations");
        menuBar.add(menu);
        
        //NDK 
        // Commenting out, as it is not tested yet, activate in PM 2.1
//        menuItem = new JMenuItem("Audit...");
//        menuItem.addActionListener(this);
//        menu.add(menuItem);
//        menu.addSeparator();
        
        menuItem = new JMenuItem("Policy classes...");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        menu.addSeparator();

        menuItem = new JMenuItem("Users...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Denies...");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        // Gopi - TODO Display a message that not implemented yet
        menuItem = new JMenuItem("DAC Confinement");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Tasks...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        // Gopi - TODO Display a message that not implemented yet
        menuItem = new JMenuItem("Event/Response...");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        menu.addSeparator();

        menuItem = new JMenuItem("Object classes...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Objects...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Operation sets...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Hosts...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Run command...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // The Tools menu.
        menu = new JMenu("Tools");
        menuBar.add(menu);

        menuItem = new JMenuItem("Reset");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        JMenu menu2 = new JMenu("Policy library");
        menu.add(menu2);

        menuItem = new JMenuItem("IBAC...");
        menuItem.addActionListener(this);
        menu2.add(menuItem);

        menuItem = new JMenuItem("DAC...");
        menuItem.addActionListener(this);
        menu2.add(menuItem);

        menuItem = new JMenuItem("MLS...");
        menuItem.addActionListener(this);
        menu2.add(menuItem);

        menuItem = new JMenuItem("Confinement for DAC user...");
        menuItem.addActionListener(this);
        menu2.add(menuItem);

        menuItem = new JMenuItem("DAC user with confinement...");
        menuItem.addActionListener(this);
        menu2.add(menuItem);

        // Create the popup menus.
        pmPopup = new JPopupMenu();

// Commented out by Gopi - 12/16/13
// Commented out unused menu items        
//        menuItem = new JMenuItem("Get connector");
//        menuItem.addActionListener(this);
//        pmPopup.add(menuItem);
//
//        menuItem = new JMenuItem("Get containers of");
//        menuItem.addActionListener(this);
//        pmPopup.add(menuItem);
//
//        menuItem = new JMenuItem("Get members of");
//        menuItem.addActionListener(this);
//        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Request perms on node...");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Request perms");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Is marked node contained in this?");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Show accessible objects");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Find border oa priv relaxed");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Find border oa priv restricted");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Initial oas");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Initial oas with labels");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Subsequent oas");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Successor oas");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Calc priv");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Get permitted ops");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);


        pmPopup.addSeparator();
        
        menuItem = new JMenuItem("Info");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Edit");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Edit email account...");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Set permissions on...");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("View permissions");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Add policy class...");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Add user attribute...");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);
        menuItem = new JMenuItem("Add user...");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Add object attribute...");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Add object...");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Add operation set...");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Mark node");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);
        menuItem = new JMenuItem("Assign marked node");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);
        menuItem = new JMenuItem("Delete assignment");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Delete node");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Delete objects from container");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        menuItem = new JMenuItem("Get member objects");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        menuItem = new JMenuItem("Get pm views");
        menuItem.addActionListener(this);
        pmPopup.add(menuItem);

        pmPopup.addSeparator();

        // Create input tools (user editor, etc.)
        pcEditor = new PolicyClassEditor(this, sslClient);
        pcEditor.pack();
        uattrEditor = new UserAttrEditor(this, sslClient);
        uattrEditor.pack();
        userEditor = new UserEditor(this, sslClient);
        userEditor.pack();
        oattrEditor = new ObjAttrEditor(this, sslClient);
        oattrEditor.pack();
        ocEditor = new ObjClassEditor(this, sslClient);
        ocEditor.pack();
        hostEditor = new HostEditor(this, sslClient);
        hostEditor.pack();
        objEditor = new ObjEditor(this, sslClient, hostEditor);
        objEditor.pack();
        opsetEditor = new OpsetEditor(this, sslClient);
        opsetEditor.pack();
        cmdEditor = new CommandEditor(this, sslClient);
        cmdEditor.pack();
//        sacEditor = new SacEditor(this, sslClient);
//        sacEditor.pack();
        sessionViewer = new SessionViewer(this, sslClient);
        sessionViewer.pack();
        denyEditor = new DenyEditor(this, sslClient);
        denyEditor.pack();
        ruleEditor = new RuleEditor(this, sslClient);
        ruleEditor.pack();
        permEditor = new PermEditor(this, sslClient);
        permEditor.pack();
        taskEditor = new TaskEditor(this, sslClient);
        taskEditor.pack();
//        sconEditor = new SconEditor(this, sslClient);
//        sconEditor.pack();
//        sconaEditor = new SconaEditor(this, sslClient);
//        sconaEditor.pack();
        emlAcctEditor = new EmlAcctEditor(this, sslClient);
        emlAcctEditor.pack();
        propertyEditor = new PropertyEditor(this, sslClient);
        propertyEditor.pack();        

    }

    private void addButtons(JToolBar toolBar) {

        int maxButtonSize = 30;
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(maxButtonSize, maxButtonSize));
        button.setMaximumSize(new Dimension(maxButtonSize, maxButtonSize));
        button.setActionCommand("Change direction");
        button.addActionListener(this);
        button.setToolTipText("Change navigation direction");

        // (steveq) Acquire image from JAR file.
        button.setIcon(GraphicsUtil.getImageIcon("/images/changeDir.gif", getClass()));

        toolBar.add(button);

        button = new JButton();
        button.setPreferredSize(new Dimension(maxButtonSize, maxButtonSize));
        button.setMaximumSize(new Dimension(maxButtonSize, maxButtonSize));
        button.setActionCommand("Users/Attributes with capabilities");
        button.addActionListener(this);
        button.setToolTipText("Display user/attributes");

        // (steveq) Acquire image from JAR file.
        button.setIcon(GraphicsUtil.getImageIcon("/images/user3.gif", getClass()));

        toolBar.add(button);

        button = new JButton();
        button.setPreferredSize(new Dimension(maxButtonSize, maxButtonSize));
        button.setMaximumSize(new Dimension(maxButtonSize, maxButtonSize));
        button.setActionCommand("Objects/Attributes with ACEs");
        button.addActionListener(this);
        button.setToolTipText("Display objects/attributes");

        // (steveq) Acquire image from JAR file.
        button.setIcon(GraphicsUtil.getImageIcon("/images/object3.gif", getClass()));
        toolBar.add(button);
    }

    public void setGraphParams(PmGraphType sGraphType, PmGraphDirection sDir, PmNode selNode) {
        childDelegate.setType(sGraphType);
        childDelegate.setDirection(sDir);
        resetTree(selNode);
    }

    // Rebuild the tree starting with the specified root node,
    // and using the current direction and the current graph type.

    public void resetTree(){
        resetTree((PmNode)tree.getModel().getRoot());
    }

    public void resetTree(PmNode rootNode) {
        if(rootNode == null){
            resetTree();
        }
        else{
            if(rootNode != tree.getModel().getRoot()){
                tree.setModel(new PmGraphModel(PmNode.createFrom(rootNode, childDelegate)));
                tree.invalidate();
            }
            rootNode.invalidate();
        }


    }
    
    private void doLogin(String sUserName) {
        JTextField userField = new JTextField(sUserName);
        JPasswordField passField = new JPasswordField();
        
        String message = "Please enter your password.";
        for (int j = 0; j < 3; j++) {
            /*int res = JOptionPane.showOptionDialog(null,
                    new Object[]{message, userField, passField},
                    "Login", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (res == JOptionPane.CANCEL_OPTION || res == JOptionPane.CLOSED_OPTION) {
                break;
            }*/
        	
            // Get user name and password.
            String sUser = "super";//userField.getText();
            char[] cPass = "super".toCharArray();//passField.getPassword();

            // Try to open the session.
            boolean b = openSession(sUser, cPass);

            // Zero the password.
            for (int i = 0; i < cPass.length; i++) {
                cPass[i] = 0;
            }
            if (b) {
                return;
            }
        }
        // Three unsuccessful tries.
        terminate(-1);
    }

    public void closeSession() {
        if (sessionId != null && fakeSession) {
            deleteSession(sessionId);
            sessionId = null;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private boolean deleteSession(String sId) {
        try {
            Packet cmd = makeCmd("deleteSession", sId);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private boolean openSession(String sSessUser, char[] cPass) {
        if (sSessUser.length() == 0) {
            JOptionPane.showMessageDialog(null, "Incorrect user name or password");
            return false;
        }

        // Find the local host name.
        String sSessHost = getLocalHost();


        // Send the host name, user name, and password to the server.
        // The engine will return the session name and id.
        try {
        	System.out.println(sSessHost);
        	System.out.println(sSessUser);
            Packet cmd = makeCmd("createSession", "My session", sSessHost,
                    sSessUser, new String(cPass));
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(null, res.getErrorMessage());
                return false;
            }
            // The result should contain in the first three items:
            // session name
            // session id
            // user id. 
            sessionId = res.getStringValue(1);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error while opening session: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void treeExpanded(TreeExpansionEvent e) {
        
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent e) {
        // Do nothing.
    }
    static String sessid;
	static String pid;
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        String sCommand = null;
        if (source instanceof JButton) {
            sCommand = ((JButton) source).getActionCommand();
        } else {
            sCommand = ((JMenuItem) source).getText();
        }

        if (sCommand.equals("Import configuration...")) {
            doImportCfg();
        } else if (sCommand.equals("Import configuration (in block)...")) {
            doImportCfgInBlock();
        } else if (sCommand.equals("Export configuration...")) {
            doExportCfg();
        } else if (sCommand.equals("Test export")) {
            doTestExport();
        } else if (sCommand.equals("Migrate to PM objects...")) {
            doMigrateToPm();
        } else if (sCommand.equals("Exit")) {
            terminate(0);
        } else if (sCommand.equals("Change direction")) {
            doChangeDirection();
        } else if (sCommand.equals("Users/Attributes")) {
            doViewUattrGraph();
        } else if (sCommand.equals("Objects/Attributes")) {
            doViewOattrGraph();
        } else if (sCommand.equals("Users/Attributes with capabilities")) {
            doViewCapsGraph();
        } else if (sCommand.equals("Objects/Attributes with ACEs")) {
            doViewAcesGraph();
        } else if (sCommand.equals("Sessions...")) {
            doViewSessions();
        } else if (sCommand.equals("Policy classes...")) {
            doPolicyClasses();
        } else if (sCommand.equals("Users...")) {
            doUsers();
        } else if (sCommand.equals("Denies...")) {
            doDeny();
        } else if (sCommand.equals("Properties...")) {
            doProperties();
        } else if (sCommand.equals("DAC Confinement")) {
            doDacConfinement();
        } else if (sCommand.equals("Tasks...")) {
            doTask();
//        } else if (sCommand.equals("Static constraints...")) {
//            doStatic();
//        } else if (sCommand.equals("Static constraints for attributes...")) {
//            doScona();
        } else if (sCommand.equals("Event/Response...")) {
            doEver();
        } else if (sCommand.equals("Mark node")) {
            doMarkNode();
        } else if (sCommand.equals("Add policy class...")) {
            doAddPolicyClass();
        } else if (sCommand.equals("Add user attribute...")) {
            doAddUserAttribute();
        } else if (sCommand.equals("Add user...")) {
            doAddUser();
        } else if (sCommand.equals("Add object attribute...")) {
            doAddObjAttribute();
        } else if (sCommand.equals("Add object...")) {
            doAddObject();
        } else if (sCommand.equals("Add operation set...")) {
            doAddOpSet();
        } else if (sCommand.equals("Assign marked node")) {
            doAssignTo();
        } else if (sCommand.equals("Delete assignment")) {
            doDeleteAssignment();
        } else if (sCommand.equals("Delete node")) {
            doDeleteNode();
        } else if (sCommand.equals("Delete objects from container")) {
            doDeleteObjects();
        } else if (sCommand.equals("Get member objects")) {
            doGetMemberObjects();
        } else if (sCommand.equals("Get pm views")) {
            doGetPmViews();
        } else if (sCommand.equals("Reset")) {
            doReset();
        } else if (sCommand.equals("Object classes...")) {
            doObjClasses();
        } else if (sCommand.equals("Objects...")) {
            doObjects();
        } else if (sCommand.equals("Operation sets...")) {
            doOpSets();
        } else if (sCommand.equals("Hosts...")) {
            doHosts();
        } else if (sCommand.equals("Run command...")) {
            doCommand();
        } else if (sCommand.equals("Get connector")) {
            doGetConnector();
        } else if (sCommand.equals("Get containers of")) {
            doGetContainersOf();
        } else if (sCommand.equals("Get members of")) {
            doGetMembersOf();
        } else if (sCommand.equals("Info")) {
            doInfo();
        } else if (sCommand.equals("Find border oa priv relaxed")) {
            doFindBorderOaPrivRelaxed();
        } else if (sCommand.equals("Find border oa priv restricted")) {
            doFindBorderOaPrivRestricted();
        } else if (sCommand.equals("Show accessible objects")) {
            doShowAccessibleObjects();
        } else if (sCommand.equals("Initial oas")) {
            doInitialOas();
        } else if (sCommand.equals("Initial oas with labels")) {
            doInitialOasWithLabels();
        } else if (sCommand.equals("Subsequent oas")) {
            doSubsequentOas();
        } else if (sCommand.equals("Successor oas")) {
            doSuccessorOas();
        } else if (sCommand.equals("Calc priv")) {
            doCalcPriv();
        } else if (sCommand.equals("Get permitted ops")) {
            doGetPermittedOps();
        } else if (sCommand.equals("Is marked node contained in this?")) {
            doTestIsContained();
        } else if (sCommand.equals("Edit email account...")) {
            doEditEmail();
        } else if (sCommand.equals("Edit")) {
            doEdit();
        } else if (sCommand.equals("Set permissions on...")) {
            doSetPerms();
        } else if (sCommand.equals("View permissions")) {
            doViewPermGraph();
        } else if (sCommand.equals("DAC...")) {
            doGenDac();
        } else if (sCommand.equals("IBAC...")) {
            doGenIbac();
        } else if (sCommand.equals("Confinement for DAC user...")) {
            doGenConfForDacUser();
        } else if (sCommand.equals("DAC user with confinement...")) {
            doGenDacUserWithConf();
        } else if (sCommand.equals("MLS...")) {
            doGenMls();
        }
        // NDK inserted this
        else if (sCommand.equals("Audit...")){
        	AuditGUI.main(null);
        }
    }

	private void doEditEmail() {
        String sType = rightClickedNode.getType();
        if (!sType.equalsIgnoreCase(PM_NODE_USER)) {
            JOptionPane.showMessageDialog(this, "Selected PM entity must be a user!");
            return;
        }

        if (!emlAcctEditor.prepare(rightClickedNode.getId(), rightClickedNode.getName())) {
            return;
        }
        emlAcctEditor.setVisible(true);
    }

    private void doEdit() {
        String sType = rightClickedNode.getType();
        if (sType.equalsIgnoreCase(PM_NODE_UATTR)) {
            uattrEditor.prepareForEdit(rightClickedNode.getId());
            uattrEditor.setVisible(true);
        } else if (sType.equalsIgnoreCase(PM_NODE_OATTR) || sType.equalsIgnoreCase(PM_NODE_ASSOC)) {
            oattrEditor.prepareForEdit(rightClickedNode.getId());
            oattrEditor.setVisible(true);
        } else if (sType.equalsIgnoreCase(PM_NODE_POL)) {
            pcEditor.prepareForEdit(rightClickedNode.getId());
            pcEditor.setVisible(true);
        }
    }

    

    // Given a file or a folder on the local host and a PM object container,
    // build an object hierarchy from that file or the contents of that folder.
    private void doMigrateToPm() {
        // Ask which file/folder is to be transformed into a PM hierarchy,
        // and the hierarchy root - the PM container that is to contain the h.
        JPanel paramPane = new JPanel();
        paramPane.setLayout(new GridBagLayout());

        JLabel folderLabel = new JLabel("File/folder:");
        JLabel filterLabel = new JLabel("Filter:");
        JLabel baseLabel = new JLabel("Base container:");

        JTextField folderField = new JTextField(20);
        JTextField filterField = new JTextField(20);

        JComboBox baseCombo = new JComboBox();
        baseCombo.setPreferredSize(new Dimension(222, 25));

        constraints.insets = new Insets(5, 0, 0, 0);

        addComp(paramPane, folderLabel, 0, 0, 1, 1);
        addComp(paramPane, filterLabel, 0, 1, 1, 1);
        addComp(paramPane, baseLabel, 0, 2, 1, 1);

        addComp(paramPane, folderField, 1, 0, 2, 1);
        addComp(paramPane, filterField, 1, 1, 2, 1);
        addComp(paramPane, baseCombo, 1, 2, 2, 1);

        List<String> ixVector = new ArrayList<String>();
        Packet result = (Packet) getObjAttrsProper();
        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                String sLine = result.getStringValue(i);
                String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
                int ix = getIndex(ixVector, pieces[0]);
                ixVector.add(ix, pieces[0]);
            }
        }

        for (int i = 0; i < ixVector.size(); i++) {
            baseCombo.addItem(ixVector.get(i));
        }

        String message = "Please select a file/folder and a base container for the object hierarchy:";
        int res = JOptionPane.showOptionDialog(null, new Object[]{message, paramPane},
                "Migrating to PM", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (res == JOptionPane.CANCEL_OPTION || res == JOptionPane.CLOSED_OPTION) {
            return;
        }

        System.out.println("You selected the path " + folderField.getText()
                + " and the container " + baseCombo.getSelectedItem());

        String sPath = folderField.getText();
        if (sPath == null || sPath.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the path!");
            return;
        }
        File f = new File(sPath);
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "No file or folder with the specified path!");
            return;
        }

        String sPmReposit = getHostRepository();
        System.out.println("repository is " + sPmReposit);

        buildHierarchy(f, (String) baseCombo.getSelectedItem(), sPmReposit);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private String getHostRepository() {
        try {
            Packet cmd = makeCmd("getHostRepository");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String getIdOfEntity(String sType, String sName){
    	System.out.println("sType: " + sType + " sNAme: " + sName);
    	try{
    		Packet cmd = makeCmd(GET_ID_OF_ENTITY_WITH_NAME_AND_TYPE, sName, sType);
    		Packet res = sslClient.sendReceive(cmd, null);
    		if(res.hasError()){
    			JOptionPane.showMessageDialog(this, res.getErrorMessage());
    			return null;
    		}
    		System.out.println(res.getStringValue(0));
    		return res.getStringValue(0);
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }

    public Packet deleteTemplate(String sObjId, String sObjName){
    	System.out.println("deleting template with id " + sObjId);
    	try{
    		Packet cmd = makeCmd("deleteTemplate", sObjId, sObjName);
    		Packet res = sslClient.sendReceive(cmd, null);
    		if(res.hasError()){
    			JOptionPane.showMessageDialog(this, res.getErrorMessage());
    			return null;
    		}
    		System.out.println(res.getStringValue(0));
    		return res;
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    
    public Packet getTemplates(){
    	try {
            Packet cmd = makeCmd(GET_TEMPLATES);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
            	JOptionPane.showMessageDialog(this, res.getErrorMessage());
    			return null;            
    		}
            return res;
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(this, "exception in getTemplates");
			return null;
        }
    }
    
    public Packet getTemplateInfo(String sTplId) {
        try {
            Packet cmd = makeCmd(GET_TEMPLATE_INFO, sTplId);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                        JOptionPane.showMessageDialog(this, res.getErrorMessage());
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Packet addTemplate(String sName, List<String> containers, List<String> keys) {
		try {
            Packet cmd = makeCmd(ADD_TEMPLATE, sName);
            StringBuffer sb = new StringBuffer();
            String s;
            for (int i = 0; i < containers.size(); i++) {
                if (i == 0) {
                    sb.append(containers.get(i));
                } else {
                    sb.append(PM_FIELD_DELIM);
                    sb.append(containers.get(i));
                }
            }
            s = sb.toString();
            System.out.println("Containers: " + s);
            if (s.length() > 0) {
                cmd.addItem(ItemType.CMD_ARG, s);
            } else {
                cmd.addItem(ItemType.CMD_ARG, "");
            }

            sb = new StringBuffer();
            for (int i = 0; i < keys.size(); i++) {
                if (i == 0) {
                    sb.append(keys.get(i));
                } else {
                    sb.append(PM_FIELD_DELIM);
                    sb.append(keys.get(i));
                }
            }
            s = sb.toString();
            System.out.println("Keys: " + s);
            if (s.length() > 0) {
                cmd.addItem(ItemType.CMD_ARG, s);
            } else {
                cmd.addItem(ItemType.CMD_ARG, "");
            }
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage() + " res in admin has error");
                return null;
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "exception in add template");
            return null;
        }
    }

    private void buildHierarchy(File file, String sTgtContName, String sPmRepo) {
        if (!file.exists()) {
            System.out.println("File or directory passed as argument does not exist!");
            return;
        }

        // A file:
        if (file.isFile()) {
            // Get a new file name for the copy inserted in the pm repository.
            String sNewFileName = getUniqueFileName(file, sPmRepo);
            System.out.println("Got new file name " + sNewFileName + " for file " + file.getAbsolutePath());

            // Copy the file to the repository under the new name.
            String sNewFilePath = null;
            if (sPmRepo.endsWith("\\")) {
                sNewFilePath = sPmRepo + sNewFileName;
            } else {
                sNewFilePath = sPmRepo + "\\" + sNewFileName;
            }
            System.out.println("    Copy old file " + file.getAbsolutePath() + " to new file " + sNewFilePath);
            String sErr = copyFileContent(file, sNewFilePath);
            if (sErr != null) {
                JOptionPane.showMessageDialog(this, sErr);
                return;
            }

            // Build object corresponding to the copy in the container.
            System.out.println("    Build object for " + sNewFilePath + " within " + sTgtContName);
            String sObjName = createObjForFile(sNewFilePath, sTgtContName);
            if (sObjName == null) {
                JOptionPane.showMessageDialog(this, "Could not create an object for file " + sNewFilePath);
                return;
            }
            return;
        }

        // A directory:
        File[] filelist = file.listFiles();
        for (int i = 0; i < filelist.length; i++) {
            File f = filelist[i];
            if (f.isFile()) {
                // Build object corresponding to f in the container sTargetContainer.
                System.out.println("Building object " + f.getAbsolutePath() + " within " + sTgtContName);

                // Get a new file name for the copy we intend to make to the pm repository.
                String sNewFileName = getUniqueFileName(f, sPmRepo);
                System.out.println("Got new file name " + sNewFileName + " for file " + f.getAbsolutePath());

                // Copy the file to the repository under the new name.
                String sNewFilePath = null;
                if (sPmRepo.endsWith("\\")) {
                    sNewFilePath = sPmRepo + sNewFileName;
                } else {
                    sNewFilePath = sPmRepo + "\\" + sNewFileName;
                }
                System.out.println("    Copy old file " + f.getAbsolutePath() + " to new file " + sNewFilePath);
                String sErr = copyFileContent(f, sNewFilePath);
                if (sErr != null) {
                    JOptionPane.showMessageDialog(this, sErr);
                    return;
                }

                // Build object corresponding to the copy in the container.
                System.out.println("    Build object for " + sNewFilePath + " within " + sTgtContName);
                String sObjName = createObjForFile(sNewFilePath, sTgtContName);
                if (sObjName == null) {
                    JOptionPane.showMessageDialog(this, "Could not create an object for file " + sNewFilePath);
                    return;
                }
            } else if (f.isDirectory()) {
                // Build container corresponding to f in the container sTargetContainer...
                System.out.println("    Build contnr for " + f.getAbsolutePath() + " within " + sTgtContName);
                String sContName = createContForFolder(f.getAbsolutePath(), sTgtContName);
                if (sContName == null) {
                    JOptionPane.showMessageDialog(this, "Could not create a container for folder " + f.getAbsolutePath());
                    return;
                }

                // ...and call buildHierarchy recursively for folder f and the container just built.
                buildHierarchy(f, sContName, sPmRepo);
            }
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private String copyFileContent(File f, String sNewPath) {
        try {
            if (!f.canRead()) {
                return "Cannot read file " + f.getCanonicalPath();
            }
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] buf = new byte[8192];
            int n;
            int howMany = 0;
            FileOutputStream fos = new FileOutputStream(sNewPath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            while ((n = bis.read(buf, 0, buf.length)) > 0) {
                bos.write(buf, 0, n);
                howMany += n;
            }
            // Here n <= 0.
            bis.close();
            bos.close();
            System.out.println("We have read " + howMany + " bytes");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // Create a new PM object container for a given folder in a given container.
    // Returns the name of the new object, or null if unsuccessful.
    @SuppressWarnings("CallToThreadDumpStack")
    private String createContForFolder(String sFolderPath, String sContName) {
        try {
            Packet cmd = makeCmd("createContForFolder", sFolderPath, sContName);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                return null;
            }
            String sLine = res.getStringValue(0);
            String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
            return pieces[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Create a new PM (logical) object for a given file in a given container.
    // Returns the name of the new object, or null if unsuccessful.
    @SuppressWarnings("CallToThreadDumpStack")
    private String createObjForFile(String sFilePath, String sContName) {
        try {
            Packet cmd = makeCmd("createObjForFile", sFilePath, sContName);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                return null;
            }
            String sLine = res.getStringValue(0);
            String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
            return pieces[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getUniqueFileName(File f, String sPmRepo) {
        // Get the file name.
        String sName = f.getName();
        int ix = sName.lastIndexOf(".");
        String sBaseName = null;
        String sFileType = null;
        if (ix >= 0) {
            sBaseName = sName.substring(0, ix);
            sFileType = sName.substring(ix);
        } else {
            sBaseName = sName;
            sFileType = "";
        }

        // Get the file path as part of the PM secure zone.
        String sNewFileName = sBaseName + sFileType;
        int n = 0;
        while (true) {
            String sPath = null;
            if (sPmRepo.endsWith("\\")) {
                sPath = sPmRepo + sNewFileName;
            } else {
                sPath = sPmRepo + "\\" + sNewFileName;
            }

            File newf = new File(sPath);
            if (!newf.exists()) {
                return sNewFileName;
            }
            n++;
            sNewFileName = sBaseName + n + sFileType;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private Object getObjAttrsProper() {
        try {
            Packet cmd = makeCmd("getObjAttrsProper");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage());
            return null;
        }
    }

    /**
	 * @uml.property  name="lastSelectedFile"
	 */
    private File lastSelectedFile = null;
    
    @SuppressWarnings("CallToThreadDumpStack")
    private void doImportCfg() {
        String[] pm = new String[]{"pm"};
        JFileChooser chooser = new JFileChooser(lastSelectedFile);
        chooser.addChoosableFileFilter(new SimpleFileFilter(pm, "Policy Machine (*.pm)"));
        int option = chooser.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (chooser.getSelectedFile() == null) {
            return;
        }
        File f = chooser.getSelectedFile();
        if (f == null) {
            return;
        }
        lastSelectedFile = f;
        if (!f.exists() || !f.isFile() || !f.canRead()) {
            JOptionPane.showMessageDialog(this, "There is something wrong with the input file!");
            return;
        }
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            List<String> delayedLines = new ArrayList<String>();
            String sLine;
            int nLineNo = 0;
            while ((sLine = in.readLine()) != null) {
                nLineNo++;
                sLine = sLine.trim();
                if (sLine.length() <= 0 || sLine.startsWith(PM_IMPORT_COMMENT_START)) {
                    continue;
                }

                Packet cmd = makeCmd("interpretCmd", sLine);
                Packet res = sslClient.sendReceive(cmd, null);
                if (res.hasError()) {
                    delayedLines.add(sLine);
                    continue;
                }
            }
            in.close();

            for (int i = 0; i < delayedLines.size(); i++) {
                sLine = delayedLines.get(i);
                Packet cmd = makeCmd("interpretCmd", sLine);
                Packet res = sslClient.sendReceive(cmd, null);
                if (res.hasError()) {
                    int ret = JOptionPane.showConfirmDialog(this, "Error Message: " + res.getErrorMessage() + "\nWould you like to halt execution now?", "ERROR", JOptionPane.YES_NO_OPTION);
                    if(ret == JOptionPane.YES_OPTION){
                        return;
                    }
                    continue; // try to create as many entities as possible.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception while importing data: "
                    + e.getMessage());
        }
        //updateGraph();
        resetTree(null);
    }

    private void updateGraph(){
        try {
            Packet cmd = makeCmd("updateGraph");
            Packet res = sslClient.sendReceive(cmd, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
 // This method sends all config commands in a block.
    @SuppressWarnings("CallToThreadDumpStack")
    private void doImportCfgInBlock() {
        String[] pm = new String[]{"pm"};
        JFileChooser chooser = new JFileChooser(lastSelectedFile);
        chooser.addChoosableFileFilter(new SimpleFileFilter(pm, "Policy Machine (*.pm)"));
        int option = chooser.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (chooser.getSelectedFile() == null) {
            return;
        }
        File f = chooser.getSelectedFile();
        if (f == null) {
            return;
        }
        lastSelectedFile = f;
        if (!f.exists() || !f.isFile() || !f.canRead()) {
            JOptionPane.showMessageDialog(this, "There is something wrong with the input file!");
            return;
        }
        lastSelectedFile = f;
        try {
            Packet cmd = makeCmd("importConfiguration");

            BufferedReader in = new BufferedReader(new FileReader(f));
            String sLine;
            int nLineNo = 0;
            while ((sLine = in.readLine()) != null) {
                nLineNo++;
                sLine = sLine.trim();
                if (sLine.length() <= 0 || sLine.startsWith(PM_IMPORT_COMMENT_START)) {
                    continue;
                }
                cmd.addItem(ItemType.CMD_ARG, sLine);
            }
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, "ImportConfiguration error: " + res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception while importing data: "
                    + e.getMessage());
        }
        resetTree(null);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doTestExport() {
        try {
            Packet cmd = makeCmd("testExportRecords");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Null result returned by the engine to testExportRecords");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
            for (int i = 0; i < res.size(); i++) {
                System.out.println(res.getStringValue(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception while exporting data: "
                    + e.getMessage());
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doExportCfg() {
        String[] pm = new String[]{"pm"};
        JFileChooser chooser = new JFileChooser(lastSelectedFile);
        chooser.addChoosableFileFilter(new SimpleFileFilter(pm, "Policy Machine (*.pm)"));
        int option = chooser.showSaveDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }
        if (chooser.getSelectedFile() == null) {
            return;
        }
        File f = chooser.getSelectedFile();
        if (f == null) {
            return;
        }
        lastSelectedFile = f;
        String sPath = f.getAbsolutePath();
        if (!sPath.endsWith(".pm")) {
            sPath += ".pm";
        }
        f = new File(sPath);

        if (f.exists() && (!f.isFile() || !f.canWrite())) {
            JOptionPane.showMessageDialog(this, "There is something wrong with the output file!");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(f);
            PrintWriter pw = new PrintWriter(fos);

            Packet cmd = makeCmd("export");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
            for (int i = 0; i < res.size(); i++) {
                pw.println(res.getStringValue(i));
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception while exporting data: "
                    + e.getMessage());
            return;
        }
    }

    // If there is a node selected (highlighted) in the tree, switch to
    // members view with that node as root. Otherwise, switch to members view
    // with the connector as root.
    private void doChangeDirection() {
        TreePath selPath = tree.getSelectionPath();
        childDelegate.setDirection(childDelegate.getDirection().inverse());
        if (selPath == null) {
            resetTree((PmNode) tree.getModel().getRoot());
        } else {
            resetTree((PmNode) selPath.getLastPathComponent());
        }
    }

    // Switch to user attribute view. This is called when the user selects the
    // "User attribute graph" menu item, so there might not be a clicked node.
    private void doViewUattrGraph() {
        childDelegate.setType(PmGraphType.USER_ATTRIBUTES);
        resetTree((PmNode) tree.getModel().getRoot());
        viewLabel.setText("Now viewing: " + getViewName());
    }

    // Switch to object attribute view. This is called when the user selects the
    // "Object attribute graph" menu item, so there might not be a clicked node.
    private void doViewOattrGraph() {
        childDelegate.setType(PmGraphType.OBJECT_ATTRIBUTES);
        resetTree(root);
        viewLabel.setText("Now viewing: " + getViewName());
    }

    private void doViewCapsGraph() {
        System.out.println("-----Caps Graph Timing-----");
        long sum = 0;
        //for(int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            childDelegate.setType(PmGraphType.CAPABILITIES);
            resetTree(root);
            long end = System.currentTimeMillis();
            sum += (end-start);
            System.out.println("TIME: " + (end-start));
        //}
        //System.out.println("Avg Time: " + sum / 10);
        System.out.println("-----Caps Graph Timing-----");
        viewLabel.setText("Now viewing: " + getViewName());
    }

    private void doViewAcesGraph() {
        System.out.println("-----Caps Graph Timing-----");
        long sum = 0;
        //for(int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            childDelegate.setType(PmGraphType.ACCESS_CONTROL_ENTRIES);
            resetTree(root);
            long end = System.currentTimeMillis();
            sum += (end-start);
            System.out.println("TIME: " + (end-start));
        //}
        //System.out.println("Avg Time: " + sum / 10);
        System.out.println("-----Caps Graph Timing-----");
        viewLabel.setText("Now viewing: " + getViewName());
    }

    private void doSetPerms() {
        
        permEditor.prepare(rightClickedNode.getName(), rightClickedNode.getId(), rightClickedNode.getType());
        permEditor.setVisible(true);
    }

    // To date, the names of the DAC policy and attribute container are hard-coded.
    @SuppressWarnings("CallToThreadDumpStack")
    private void doGenDac() {
        // Ask for the DAC policy parameters:
        // policy name, container for user name attributes.
        JPanel paramPane = new JPanel();
        paramPane.setLayout(new GridBagLayout());

        JLabel policyLabel = new JLabel("DAC policy name:");
        JLabel containerLabel = new JLabel("User names container:");

        JTextField policyField = new JTextField(20);
        JTextField containerField = new JTextField(20);

        constraints.insets = new Insets(5, 0, 0, 0);

        addComp(paramPane, policyLabel, 0, 0, 1, 1);
        addComp(paramPane, containerLabel, 0, 1, 1, 1);

        addComp(paramPane, policyField, 1, 0, 2, 1);
        addComp(paramPane, containerField, 1, 1, 2, 1);

        String message = "Please enter the policy parameters:";
        int ret = JOptionPane.showOptionDialog(null,
                new Object[]{message, paramPane},
                "DAC Policy", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        String sPolName = policyField.getText();
        if (sPolName == null || sPolName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a policy name!");
            return;
        }
        String sContName = containerField.getText();
        if (sContName == null || sContName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a container name!");
            return;
        }
        try {
            Packet cmd = makeCmd("genDac", sPolName, sContName);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }

        // We should redraw the graph.
        resetTree(null);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doGenIbac() {
        // Ask for the IBAC policy parameters: the policy name.
        JPanel paramPane = new JPanel();
        paramPane.setLayout(new GridBagLayout());

        JLabel policyLabel = new JLabel("DAC policy name:");
        JTextField policyField = new JTextField(20);

        constraints.insets = new Insets(5, 0, 0, 0);

        addComp(paramPane, policyLabel, 0, 0, 1, 1);
        addComp(paramPane, policyField, 1, 0, 2, 1);

        String message = "Please enter the policy name:";
        int ret = JOptionPane.showOptionDialog(null,
                new Object[]{message, paramPane},
                "DAC Policy", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        String sPolName = policyField.getText();
        if (sPolName == null || sPolName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a policy name!");
            return;
        }

        try {
            Packet cmd = makeCmd("genIbac", sPolName);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception in genIbac!");
            return;
        }

        // We should redraw the graph.
        resetTree(null);
    }

    // Generate a DAC user with confinement.
    // A discretionary policy must exist.
    // The user must not exist.
    // The confinement policy may or may not exist.
    // The confinement attribute must not exist.
    @SuppressWarnings("CallToThreadDumpStack")
    private void doGenDacUserWithConf() {
        // Ask for the parameters:
        // the user name, his full name, the sensitive object container,
        // the confinement policy name, and the confinement attribute.
        JPanel paramPane = new JPanel();
        paramPane.setLayout(new GridBagLayout());

        JLabel userLabel = new JLabel("User logon name:");
        JLabel fullLabel = new JLabel("User full name:");
        JLabel sensLabel = new JLabel("Sensitive container name:");
        JLabel policyLabel = new JLabel("Confinement policy name:");
        JLabel attrLabel = new JLabel("Confinement attribute name:");

        JTextField userField = new JTextField(20);
        JTextField fullField = new JTextField(20);
        JTextField sensField = new JTextField(20);
        JTextField policyField = new JTextField(20);
        JTextField attrField = new JTextField(20);

        constraints.insets = new Insets(5, 0, 0, 0);

        addComp(paramPane, userLabel, 0, 0, 1, 1);
        addComp(paramPane, fullLabel, 0, 1, 1, 1);
        addComp(paramPane, sensLabel, 0, 2, 1, 1);
        addComp(paramPane, policyLabel, 0, 3, 1, 1);
        addComp(paramPane, attrLabel, 0, 4, 1, 1);

        addComp(paramPane, userField, 1, 0, 2, 1);
        addComp(paramPane, fullField, 1, 1, 2, 1);
        addComp(paramPane, sensField, 1, 2, 2, 1);
        addComp(paramPane, policyField, 1, 3, 2, 1);
        addComp(paramPane, attrField, 1, 4, 2, 1);

        String message = "Please enter the policy parameters:";
        int ret = JOptionPane.showOptionDialog(null,
                new Object[]{message, paramPane},
                "Confinement Policy for DAC", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        String sUserName = userField.getText();
        if (sUserName == null || sUserName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a user logon name!");
            return;
        }

        String sUserFullName = fullField.getText();
        if (sUserFullName == null || sUserFullName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the user full name!");
            return;
        }

        String sPolName = policyField.getText();
        if (sPolName == null || sPolName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a policy name!");
            return;
        }

        String sAttrName = attrField.getText();
        if (sAttrName == null || sAttrName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the confinement attribute name!");
            return;
        }

        String sSensName = sensField.getText();
        if (sSensName == null || sSensName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the sensitive container name!");
            return;
        }

        Packet res = null;
        try {
            Packet cmd = makeCmd("genDacUserWithConf", sUserName, sUserFullName,
                    sPolName, sAttrName, sSensName);
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
        // We should redraw the graph.
        resetTree(null);
    }

    // Generate confinement for a specified DAC user.
    // The user must exist (with his/her name attribute) in a discretionary policy.
    // The sensitive object container must exist in the user's home.
    // The confinement policy may or may not exist. The confinement attribute
    // must not exist.
    @SuppressWarnings("CallToThreadDumpStack")
    private void doGenConfForDacUser() {
        // Ask for the DAC confinement policy parameters:
        // the user, the confinement policy name, the confinement attribute,
        // and the object container.

        JPanel paramPane = new JPanel();
        paramPane.setLayout(new GridBagLayout());

        JLabel userLabel = new JLabel("User name:");
        JLabel sensLabel = new JLabel("Sensitive container name:");
        JLabel policyLabel = new JLabel("Confinement policy name:");
        JLabel attrLabel = new JLabel("Confinement attribute name:");

        JTextField userField = new JTextField(20);
        JTextField sensField = new JTextField(20);
        JTextField policyField = new JTextField(20);
        JTextField attrField = new JTextField(20);

        constraints.insets = new Insets(5, 0, 0, 0);

        addComp(paramPane, userLabel, 0, 0, 1, 1);
        addComp(paramPane, sensLabel, 0, 1, 1, 1);
        addComp(paramPane, policyLabel, 0, 2, 1, 1);
        addComp(paramPane, attrLabel, 0, 3, 1, 1);

        addComp(paramPane, userField, 1, 0, 2, 1);
        addComp(paramPane, sensField, 1, 1, 2, 1);
        addComp(paramPane, policyField, 1, 2, 2, 1);
        addComp(paramPane, attrField, 1, 3, 2, 1);

        String message = "Please enter the policy parameters:";
        int ret = JOptionPane.showOptionDialog(null,
                new Object[]{message, paramPane},
                "Confinement Policy for DAC", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        String sUserName = userField.getText();
        if (sUserName == null || sUserName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a user name!");
            return;
        }

        String sSensName = sensField.getText();
        if (sSensName == null || sSensName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the sensitive container name!");
            return;
        }

        String sPolName = policyField.getText();
        if (sPolName == null || sPolName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a policy name!");
            return;
        }

        String sAttrName = attrField.getText();
        if (sAttrName == null || sAttrName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the confinement attribute name!");
            return;
        }

        Packet res = null;
        try {
            Packet cmd = makeCmd("genConfForDacUser", sUserName, sPolName, sAttrName, sSensName);
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
        // We should redraw the graph.
        resetTree(null);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doGenMls() {
        // Ask for the MLS policy parameters:
        // policy name, sec levels separated by commas.
        JPanel paramPane = new JPanel();
        paramPane.setLayout(new GridBagLayout());

        JLabel policyLabel = new JLabel("MLS policy name:");
        JLabel secLabel = new JLabel("Comma-separated security levels (low to high):");

        JTextField policyField = new JTextField(20);
        JTextField secField = new JTextField(20);

        constraints.insets = new Insets(5, 0, 0, 0);

        addComp(paramPane, policyLabel, 0, 0, 1, 1);
        addComp(paramPane, secLabel, 0, 1, 1, 1);

        addComp(paramPane, policyField, 1, 0, 2, 1);
        addComp(paramPane, secField, 1, 1, 2, 1);

        String message = "Please enter the policy parameters:";
        int ret = JOptionPane.showOptionDialog(null,
                new Object[]{message, paramPane},
                "DAC Policy", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        String sPolName = policyField.getText();
        if (sPolName == null || sPolName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a policy name!");
            return;
        }
        String sSecLevels = secField.getText();
        if (sSecLevels == null || sSecLevels.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the security levels separated by commas!");
            return;
        }

        Packet res = null;
        try {
            Packet cmd = makeCmd("genMls", sPolName, sSecLevels);
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }

        // We should redraw the graph.
        resetTree(null);
    }

    // Called when the user selects the pop-up menu "View permissions".
    // Should work only when the selected node is a user attribute, an object
    // attribute, or an operation set.
    private static EnumSet<PmNodeType> nodesWithPermissions = EnumSet.of(PmNodeType.USER_ATTRIBUTE, PmNodeType.OBJECT_ATTRIBUTE, PmNodeType.OBJECT, PmNodeType.OPSET);
    private void doViewPermGraph() {
        System.out.println("Right-clicked node is " + rightClickedNode.getId() + ", "
                + rightClickedNode.getName() + ", " + rightClickedNode.getType());

        if (!nodesWithPermissions.contains(rightClickedNode.getNodeType())) {
            JOptionPane.showMessageDialog(this, "Invalid node type for this request!");
            return;
        }
        childDelegate.setType(PmGraphType.PERMISSIONS);
        resetTree(rightClickedNode);
        viewLabel.setText("Now viewing: " + getViewName());
    }

    private String getViewName() {
        return childDelegate.getType().readableName();
    }

    private void doViewSessions() {
        sessionViewer.prepare();
        sessionViewer.setVisible(true);
    }

    private void doCommand() {
        cmdEditor.prepare();
        cmdEditor.setVisible(true);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doReset() {
        // Prepare engine command.
        try {
            Packet cmd = makeCmd("reset");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
        // We should redraw the graph.
        childDelegate.setType(PmGraphType.USER_ATTRIBUTES);
        resetTree(null);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doGetPmViews() {
        try {
            // Prepare engine command.
            Packet cmd = makeCmd("testGetPmViews", rightClickedNode.getId(), rightClickedNode.getType());
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doGetMemberObjects() {
        try {
            // Prepare engine command.
            Packet cmd = makeCmd("testGetMemberObjects", rightClickedNode.getId(), rightClickedNode.getType());
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doDeleteObjects() {
        try {
            // Prepare engine command.
            Packet cmd = makeCmd("deleteContainerObjects", rightClickedNode.getName(), rightClickedNode.getType());
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Null result from deleteContainerObjects!");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
        // We should redraw the graph.
        if(rightClickedNode != null){
            rightClickedNode.invalidate();
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doDeleteNode() {
        try {
            // Prepare engine command.
            Packet cmd = makeCmd("deleteNode", rightClickedNode.getId(), rightClickedNode.getType(), "no");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
        // We should redraw the graph. If the deleted node was currently selected,
        // we should redraw the tree starting with another node.
        if(rightClickedNode.getParent() != null){
            List<PmNode> children = rightClickedNode.getParent().getChildren();
            children.remove(rightClickedNode);
        }
    }

    private void doPolicyClasses() {
        pcEditor.prepareForAdd();
        pcEditor.setVisible(true);

        // Return here
    }

    private void doDeny() {
        denyEditor.prepare();
        denyEditor.setVisible(true);
    }

    private void doProperties() {
        propertyEditor.prepare();
        propertyEditor.setVisible(true);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doDacConfinement() {
        JOptionPane.showMessageDialog(this, "Not implemented yet");
        return;
        /*try {
            Packet cmd = makeCmd("getEntityWithProp", PM_NODE_POL, "type=discretionary");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, "No policy found!");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }

        // A DAC policy exists. Ask for confinement parameters:
        // user (existing), confinement policy class, confinement attribute,
        // sensitive container.
        JPanel paramPane = new JPanel();
        paramPane.setLayout(new GridBagLayout());

        JLabel userLabel = new JLabel("User name (existing):");
        JLabel confPcLabel = new JLabel("Confinement policy:");
        JLabel confAttrLabel = new JLabel("Confinement attribute (new):");
        JLabel sensContLabel = new JLabel("Sensitive container (new):");

        JTextField userField = new JTextField(20);
        JTextField confPcField = new JTextField(20);
        JTextField confAttrField = new JTextField(20);
        JTextField sensContField = new JTextField(20);

        constraints.insets = new Insets(5, 0, 0, 0);

        addComp(paramPane, userLabel, 0, 0, 1, 1);
        addComp(paramPane, confPcLabel, 0, 1, 1, 1);
        addComp(paramPane, confAttrLabel, 0, 2, 1, 1);
        addComp(paramPane, sensContLabel, 0, 3, 1, 1);

        addComp(paramPane, userField, 1, 0, 2, 1);
        addComp(paramPane, confPcField, 1, 1, 2, 1);
        addComp(paramPane, confAttrField, 1, 2, 2, 1);
        addComp(paramPane, sensContField, 1, 3, 2, 1);

        String message = "Please enter the confinement parameters:";
        int ret = JOptionPane.showOptionDialog(null,
                new Object[]{message, paramPane},
                "Confinement", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (ret != JOptionPane.OK_OPTION) {
            return;
        }

        String sUserName = userField.getText();
        if (sUserName == null || sUserName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a user name!");
            return;
        }
        String sConfPcName = confPcField.getText();
        if (sConfPcName == null || sConfPcName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a confinement policy name!");
            return;
        }
        String sConfAttrName = confAttrField.getText();
        if (sConfAttrName == null || sConfAttrName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a confinement attribute name!");
            return;
        }
        String sSensContName = sensContField.getText();
        if (sSensContName == null || sSensContName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Please enter a name for the sensitive container!");
            return;
        }


        try {
            Packet cmd = makeCmd("doDacConfinement", sUserName, sConfPcName, sConfAttrName, sSensContName);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
        // We should redraw the graph.
        resetTree(null);*/
    }

    private void addComp(Container container, Component component, int x, int y, int w, int h) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        container.add(component, constraints);
    }

    private void doTask() {
        JOptionPane.showMessageDialog(this, "Not implemented yet");
        /*taskEditor.prepare();
        taskEditor.setVisible(true);*/
    }

//    private void doStatic() {
//        sconEditor.prepare();
//        sconEditor.setVisible(true);
//    }

//    // Static Constraints for Attributes.
//    private void doScona() {
//        sconaEditor.prepare();
//        sconaEditor.setVisible(true);
//    }

    private void doEver() {
        ruleEditor.prepare();
        ruleEditor.setVisible(true);
    }

    private void doObjClasses() {
        ocEditor.prepare();
        ocEditor.setVisible(true);

        // Return here
    }

    private void doObjects() {
        objEditor.prepareAndSetBaseNode(null);
        objEditor.setVisible(true);

        // Return here
    }

    private void doOpSets() {
        opsetEditor.prepareAndSetBaseNode(null, null);
        opsetEditor.setVisible(true);

        // Return here
    }

    private void doHosts() {
        hostEditor.prepare();
        hostEditor.setVisible(true);

        // Return here
    }

    // Called when the user selects the "Users..." menu item.
    // Set to null the id, label (name), and type of the optional user attribute
    // this new user can be assigned to.
    private void doUsers() {
        userEditor.prepare(null);
        userEditor.setVisible(true);

        // Return here
    }

    // Called when the user selects the "Add user..." popup menu item after right-click
    // on a node. The clicked node can only be a user attribute or the connector node.
    // In both cases, the user is created and assigned to the clicked node.
    private void doAddUser() {
        if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_UATTR)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)) {
            JOptionPane.showMessageDialog(this, "You may not add a user to the selected node!");
            return;
        }

        userEditor.prepare(rightClickedNode);
        userEditor.setVisible(true);

        // Return here
    }

    private void doMarkNode() {
        System.out.println("doMarkNode: " + rightClickedNode.getId() + ", "
                + rightClickedNode.getName() + ", " + rightClickedNode.getType());
        markedNode = rightClickedNode;
    }

    private void doAddPolicyClass() {
        System.out.println("You right-clicked on node " + rightClickedNode.getId() + ":"
                + rightClickedNode.getName() + ":" + rightClickedNode.getType());

        // If the clicked node is the connector node, a policy class will be
        // added as a new ascendant of the connector.
        // Otherwise, you cannot add a policy class.
        if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)) {
            JOptionPane.showMessageDialog(this, "You cannot add a policy class to the selected node!");
            return;
        }
        pcEditor.prepareForAdd();
        pcEditor.setVisible(true);

        // Return here
    }

    private void doAddUserAttribute() {
        System.out.println("You right-clicked on node " + rightClickedNode.getId() + ":"
                + rightClickedNode.getName() + ":" + rightClickedNode.getType());

        // If the clicked node is:
        // - the connector node, the new attribute will be added as an ascendant of it;
        // - a user, the new attribute will be added as a descendant of it;
        // - a user attribute, the new attribute will be added as an ascendant of it;
        // - a policy, the new attribute will be added as an ascendant of it.
        if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_POL)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_UATTR)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_USER)) {
            JOptionPane.showMessageDialog(this, "You may not add a user attribute to the selected node!");
            return;
        }

        uattrEditor.prepareForAdd(rightClickedNode);
        uattrEditor.setVisible(true);

        // Return here
    }

    // Called when the user right-clicks on a node and selects the "Add Object..."
    // menu item.
    // The new object will have an associated object attribute. This object
    // attribute will be created as an ascendant of the clicked node, which
    // can only be:
    // - the connector node;
    // - a policy class node;
    // - an object attribute node not associated to an object.
    private void doAddObject() {
        if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_POL)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_OATTR)) {
            JOptionPane.showMessageDialog(this, "Cannot add an object to the selected node");
            return;
        }

        objEditor.prepareAndSetBaseNode(rightClickedNode);
        objEditor.setVisible(true);

        // Return here
    }

    // Called when the user right-clicks on a node and selects the "Add object Attribute..."
    // menu item.
    // The new object attribute will be created as an ascendant of the clicked node, which
    // can only be:
    // - the connector node;
    // - a policy class node;
    // - an object attribute node not associated to an object.
    private void doAddObjAttribute() {
        if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_POL)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_OATTR)) {
            JOptionPane.showMessageDialog(this, "Cannot add an object attribute to the selected node");
            return;
        }

        oattrEditor.prepareForAdd(rightClickedNode);
        oattrEditor.setVisible(true);

        // Return here
    }

    // Called when the user right-clicks on a node and selects the
    // "Add operation set..." menu item. The clicked node must be either:
    // - the connector node;  then the new op set will be assigned to the connector
    //   through the "pmToConnector" attribute; or
    // - a user attribute; then the new op set will be assigned to the user
    //   attribute through the "pmFromAttr" attribute; or
    // - an object attribute; then the op set will be assigned to the object
    //   attribute through the "pmToAttr" atrtibute.
    private void doAddOpSet() {
        if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_UATTR)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_OATTR)
                && !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_ASSOC)) {
            JOptionPane.showMessageDialog(this, "You cannot add an operation set to that kind of node!");
            return;
        }

        opsetEditor.prepareAndSetBaseNode(rightClickedNode, null);
        opsetEditor.setVisible(true);

        // Return here
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doAssignTo() {
        if (markedNode == null) {
            JOptionPane.showMessageDialog(this, "Please mark a node for assignment");
            return;
        }

        System.out.println("You asked to assign node " + markedNode.getId() + ":"
                + markedNode.getName() + ":" + markedNode.getType() + " to node "
                + rightClickedNode.getId() + ":" + rightClickedNode.getName() + ":" + rightClickedNode.getType());

        // Permissible assignnments (so far):
        // user ---> user attribute
        // user attribute ---> user attribute, no cycles
        // user attribute ---> policy class

        try {
            // Prepare engine command. The 'null' argument is the process id.
            Packet cmd = makeCmd("assign", null, markedNode.getId(), markedNode.getType(),
                    rightClickedNode.getId(), rightClickedNode.getType(), "no");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
        // We should redraw the graph.
        markedNode.getParent().invalidate();
        rightClickedNode.invalidate();
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doDeleteAssignment() {
        if (markedNode == null) {
            JOptionPane.showMessageDialog(this, "Please mark a node for de-assignment");
            return;
        }

        System.out.println("You asked to delete the assignment of node " + markedNode.getId() + ":"
                + markedNode.getName() + ":" + markedNode.getType() + " to node "
                + rightClickedNode.getId() + ":" + rightClickedNode.getName() + ":" + rightClickedNode.getType());

        try {
            // Prepare engine command. The null argument in makeCmd() is the process id.
            Packet cmd = makeCmd("deleteAssignment", null, markedNode.getId(), markedNode.getType(),
                    rightClickedNode.getId(), rightClickedNode.getType(), "no");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
        // We should redraw the graph.
        rightClickedNode.invalidate();

    }

    private void doInfo() {
        doDisplayInfoOn(rightClickedNode);
    }

    // Called when the user rests the mouse cursor a while on a graph node.
    // The index is the node's index in some tables.
    private void doDisplayInfoOn(PmNode node) {
        System.out.println("Display info on a node of type " + node.getType());
        if (node.getType().equalsIgnoreCase(PM_NODE_OPSET)) {
            doDisplayInfoOnOpset(node.getId(), node.getName());
        } else if (node.getType().equalsIgnoreCase(PM_NODE_OATTR)) {
            doDisplayInfoOnOattr(node.getId());
        } else if (node.getType().equalsIgnoreCase(PM_NODE_ASSOC)) {
            doDisplayInfoOnAssoc(node.getId());
        } else if (node.getType().equalsIgnoreCase(PM_NODE_UATTR)) {
            doDisplayInfoOnUattr(node.getId());
        } else if (node.getType().equalsIgnoreCase(PM_NODE_POL)) {
            doDisplayInfoOnPolicy(node.getId());
        }
    }

    private void doDisplayInfoOnOpset(String sId, String sName) {
        opsetEditor.prepareAndSetBaseNode(null, sId);
        opsetEditor.setVisible(true);
    }

    private void doDisplayInfoOnOattr(String sId) {
    }

    private void doDisplayInfoOnAssoc(String sId) {
    }

    private void doDisplayInfoOnUattr(String sId) {
    }

    private void doDisplayInfoOnPolicy(String sId) {
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void doTestIsContained() {
        if (markedNode == null) {
            JOptionPane.showMessageDialog(this, "Please mark a node first!");
            return;
        }
        System.out.println("Is Contained " + markedNode.getId() + ":" + markedNode.getType()
                + rightClickedNode.getId() + ":" + rightClickedNode.getType());
        Packet res = null;
        try {
            Packet cmd = makeCmd("testIsContained", markedNode.getId(), markedNode.getType(),
                    rightClickedNode.getId(), rightClickedNode.getType());
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }
        String sRes = res.getStringValue(0);
        JOptionPane.showMessageDialog(this, sRes);
    }

    // Request permissions on the virtual object/object attribute clicked
    // by the user.
    // Send the node id and type to the engine; the engine will look for a
    // virtual object mapped to this node and compute the permissions on that
    // virtual object.
    @SuppressWarnings("CallToThreadDumpStack")
    private void doRequestPermsOnObj() {
        String sReqPerms = JOptionPane.showInputDialog(null, "Please enter requested permissions, separated by commas:");
        if (sReqPerms == null) {
            return;
        }
        System.out.println("Request perms " + sReqPerms + " on oattr " + rightClickedNode.getId() + ":" + rightClickedNode.getName() + ":" + rightClickedNode.getType());

        Packet res = null;
        try {
            Packet cmd = makeCmd("requestPerms", sessionId, rightClickedNode.getName(), sReqPerms);
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return;
        }

        DefaultListModel permModel = new DefaultListModel();
        JList permList = new JList(permModel);
        JScrollPane permScrollPane = new JScrollPane(permList);
        permScrollPane.setPreferredSize(new Dimension(300, 150));

        if (res != null) {
            for (int i = 0; i < res.size(); i++) {
                permModel.addElement(res.getStringValue(i));
            }
        }

        String message = "List of granted permissions:";
        int ret = JOptionPane.showOptionDialog(this,
                new Object[]{message, permScrollPane},
                "Permissions", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, null, null);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private String[] getConnector() {
        Packet res = null;
        try {
            Packet cmd = makeCmd("getConnector");
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return null;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
        }

        String sLine = res.getStringValue(0);
        String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
        return new String[]{pieces[0], pieces[1], pieces[2]};
    }

    private void doShowAccessibleObjects() {
    	Vector v = showAccessibleObjects(rightClickedNode.getType(), rightClickedNode.getId(),
    			rightClickedNode.getName());
    	// No need to list v's components, one can look at the packet returned by the engine.
    	
    }
    
    private void doFindBorderOaPrivRelaxed() {
    	Vector v = findBorderOaPrivRelaxed(rightClickedNode.getType(), rightClickedNode.getId(),
    			rightClickedNode.getName());
    	// No need to list v's components, one can look at the packet returned by the engine.
    	
    }

    private void doFindBorderOaPrivRestricted() {
    	Vector v = findBorderOaPrivRestricted(rightClickedNode.getType(), rightClickedNode.getId(),
    			rightClickedNode.getName());
    	// No need to list v's components, one can look at the packet returned by the engine.
    	
    }
    
    private void doInitialOas() {
    	Vector v = initialOas(rightClickedNode.getType(), rightClickedNode.getId(),
    			rightClickedNode.getName());
    	// No need to list v's components, one can look at the packet returned by the engine.
    	
    }
    
    private void doInitialOasWithLabels() {
    	Vector v = initialOasWithLabels(rightClickedNode.getType(), rightClickedNode.getId(),
    			rightClickedNode.getName());
    	// No need to list v's components, one can look at the packet returned by the engine.
    	
    }
    
    private void doSubsequentOas() {
    	Vector v = subsequentOas(markedNode.getType(), markedNode.getId(), markedNode.getName(),
    			rightClickedNode.getType(), rightClickedNode.getId(), rightClickedNode.getName());
    	// No need to list v's components, one can look at the packet returned by the engine.
    	
    }
    
    private void doSuccessorOas() {
    	Vector v = successorOas(markedNode.getType(), markedNode.getId(), markedNode.getName(),
    			rightClickedNode.getType(), rightClickedNode.getId(), rightClickedNode.getName());
    	// No need to list v's components, one can look at the packet returned by the engine.
    	
    }

    private void doCalcPriv() {
    	if (markedNode == null || !markedNode.getType().equalsIgnoreCase(PM_NODE_USER)) {
    		JOptionPane.showMessageDialog(this,  "Marked PM entity must be a user!");
    		
    	}
    	Vector v = calcPriv(markedNode.getType(), markedNode.getId(), markedNode.getName(),
    			rightClickedNode.getType(), rightClickedNode.getId(), rightClickedNode.getName());
    	// No need to list v's components, one can look at the packet returned by the engine.
    	
    }
    private void doGetPermittedOps() {
    	if (markedNode == null || !markedNode.getType().equalsIgnoreCase(PM_NODE_USER)) {
    		JOptionPane.showMessageDialog(this,  "Marked PM entity must be a user!");
    		
    	}
    	Vector v = getPermittedOps(markedNode.getId(), null, rightClickedNode.getName());
    	// No need to list v's components, one can look at the packet returned by the engine.
    	
    }
    
    private void doGetConnector() {
        String[] conNode = getConnector();
        System.out.println("Connector is " + conNode[0] + ", " + conNode[1] + ", " + conNode[2]);
    }

    private void doGetMembersOf() {
    	log.debugStackCall("!!!!!!! getMembersOf - trace 5");	

        List<String[]> v = getMembersOf(rightClickedNode.getType(), rightClickedNode.getId(), rightClickedNode.getName());
        for (int i = 0; i < v.size(); i++) {
            String[] sa = v.get(i);
            System.out.println("Member " + sa[0] + ", " + sa[1] + ", " + sa[2]);
        }
    }

    private void doGetContainersOf() {
        List<String[]> v = getContainersOf(rightClickedNode.getType(), rightClickedNode.getId(), rightClickedNode.getName());
        for (int i = 0; i < v.size(); i++) {
            String[] sa = v.get(i);
            System.out.println("Container " + sa[0] + ", " + sa[1] + ", " + sa[2]);
        }
    }

    private void buildGraph() {

    }

    private Vector showAccessibleObjects(String sType, String sId, String sLabel) {
    	Packet res = null;
    	try {
    		Packet cmd = makeCmd("showAccessibleObjects", sLabel, sId, sType);
    		res = sslClient.sendReceive(cmd, null);
    		if (res == null) {
    			JOptionPane.showMessageDialog(this,  "Undeterminated error; null result returned");
    			return null;
    		}
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
    	}
    }
    
    private Vector findBorderOaPrivRelaxed(String sType, String sId, String sLabel) {
    	Packet res = null;
    	try {
    		Packet cmd = makeCmd("findBorderOaPrivRelaxed", sLabel, sId, sType);
    		res = sslClient.sendReceive(cmd, null);
    		if (res == null) {
    			JOptionPane.showMessageDialog(this,  "Undeterminated error; null result returned");
    			return null;
    		}
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
    	}
    }
    
    private Vector findBorderOaPrivRestricted(String sType, String sId, String sLabel) {
    	Packet res = null;
    	try {
    		Packet cmd = makeCmd("findBorderOaPrivRestricted", sLabel, sId, sType);
    		res = sslClient.sendReceive(cmd, null);
    		if (res == null) {
    			JOptionPane.showMessageDialog(this,  "Undeterminated error; null result returned");
    			return null;
    		}
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
    	}
    }
 
    private Vector initialOas(String sType, String sId, String sLabel) {
    	Packet res = null;
    	try {
    		Packet cmd = makeCmd("initialOas", sLabel, sId, sType);
    		res = sslClient.sendReceive(cmd, null);
    		if (res == null) {
    			JOptionPane.showMessageDialog(this,  "Undeterminated error; null result returned");
    			return null;
    		}
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
    	}
    }

    private Vector initialOasWithLabels(String sType, String sId, String sLabel) {
    	Packet res = null;
    	try {
    		Packet cmd = makeCmd("initialOasWithLabels", sLabel, sId, sType);
    		res = sslClient.sendReceive(cmd, null);
    		if (res == null) {
    			JOptionPane.showMessageDialog(this,  "Undeterminated error; null result returned");
    			return null;
    		}
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
    	}
    }

    private Vector subsequentOas(String sUserType, String sUserId, String sUserName,
    		String sTgtType, String sTgtId, String sTgtLabel) {
    	Packet res = null;
    	try {
    		Packet cmd = makeCmd("subsequentOas", sUserName, sUserId, sUserType,
    				sTgtLabel, sTgtId, sTgtType);
    		res = sslClient.sendReceive(cmd, null);
    		if (res == null) {
    			JOptionPane.showMessageDialog(this,  "Undeterminated error; null result returned");
    			return null;
    		}
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
    	}
    }

    private Vector successorOas(String sUserType, String sUserId, String sUserName,
    		String sTgtType, String sTgtId, String sTgtLabel) {
    	Packet res = null;
    	try {
    		Packet cmd = makeCmd("successorOas", sUserName, sUserId, sUserType,
    				sTgtLabel, sTgtId, sTgtType);
    		res = sslClient.sendReceive(cmd, null);
    		if (res == null) {
    			JOptionPane.showMessageDialog(this,  "Undeterminated error; null result returned");
    			return null;
    		}
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
    	}
    }

    private Vector calcPriv(String sUserType, String sUserId, String sUserName,
    		String sTgtType, String sTgtId, String sTgtLabel) {
    	Packet res = null;
    	try {
    		Packet cmd = makeCmd("calcPriv", sUserName, sUserId, sUserType,
    				sTgtLabel, sTgtId, sTgtType);
    		res = sslClient.sendReceive(cmd, null);
    		if (res == null) {
    			JOptionPane.showMessageDialog(this,  "Undeterminated error; null result returned");
    			return null;
    		}
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
    	}
    }

    private Vector getPermittedOps(String sUserId, String sProcId, String sTgtLabel) {
    	Packet res = null;
    	try {
    		Packet cmd = makeCmd("getPermittedOps", sUserId, sProcId, sTgtLabel);
    		res = sslClient.sendReceive(cmd, null);
    		if (res == null) {
    			JOptionPane.showMessageDialog(this,  "Undeterminated error; null result returned");
    			return null;
    		}
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
            return null;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
    	}
    }


    // Returns a vector of string arrays. Each string array has 3 elements,
    // the type, id, and label/name of a node member of the clicked node.
    // The membership is defined by the assignment relation: we say that
    // x is a member of y if there is an assignment x ---> y, regardless
    // of the types of x, y.
    @SuppressWarnings("CallToThreadDumpStack")
    public List<String[]> getMembersOf(String sType, String sId, String sLabel) {
        Packet res = null;
        try {
        	log.debugStackCall("!!!!!!! getMembersOf - trace 6");	

            Packet cmd = makeCmd("getMembersOf", sLabel, sId, sType, childDelegate.getType().typeCode());
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return null;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
        }

        List<String[]> v = new ArrayList<String[]>();
        if (res != null) {
            for (int i = 0; i < res.size(); i++) {
                String sLine = res.getStringValue(i);
                String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
                v.add(new String[]{pieces[0], pieces[1], pieces[2]});
            }
        }
        return v;
    }


    // Returns a vector of string arrays. Each string array has 3 elements,
    // the type, id, and label/name of a container that contains the clicked node.
    // The containment is defined by the assignment relation: we say that
    // x contains y if there is an assignment y ---> x, regardless
    // of the types of x, y.
    @SuppressWarnings("CallToThreadDumpStack")
    private List<String[]> getContainersOf(String sType, String sId, String sLabel) {
        Packet res = null;
        try {
            Packet cmd = makeCmd("getContainersOf", sLabel, sId, sType, childDelegate.getType().typeCode());
            res = sslClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                return null;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                return null;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
        }

        List<String[]> v = new ArrayList<String[]>();
        if (res != null) {
            for (int i = 0; i < res.size(); i++) {
                String sLine = res.getStringValue(i);
                String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
                v.add(new String[]{pieces[0], pieces[1], pieces[2]});
            }
        }
        return v;
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void terminate(int n) {
        closeSession();
        try {
            sslClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(n);
    }

    private Object checkPmConnection() {
        try {
            Packet cmd = makeCmd("connect");
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null || res.isEmpty()) {
                return failurePacket("Connect: undetermined error");
            }
            return res;
        } catch (Exception e) {
            return failurePacket("Exception in connect: " + e.getMessage());
        }
    }

    private String getSessionUser(String sSessId) {
        try {
            Packet cmd = makeCmd("getSessionUser", sSessId);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null || res.isEmpty()) {
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private String getSessionName(String sSessId) {
        try {
            Packet cmd = makeCmd("getSessionName", sSessId);
            Packet res = sslClient.sendReceive(cmd, null);
            if (res == null | res.isEmpty()) {
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    public Object successPacket() {
        Packet p = new Packet();
        try {
            p.addItem(ItemType.RESPONSE_SUCCESS, "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return p;
    }

    @SuppressWarnings("CallToThreadDumpStack")
    public Object successPacket(String s) {
        Packet p = new Packet();
        try {
            p.addItem(ItemType.RESPONSE_SUCCESS, s);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return p;
    }




    public Packet makeCmd(PMCommand cmd, String... sArgs){
        try {
            return makeCmd(cmd.commandCode(), sArgs);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return failurePacket();
    }


    public Packet makeCmd(String sCode, String... sArgs) throws Exception {
        return CommandUtil.makeCmd(sCode, sessionId == null ? "" : sessionId, sArgs);
    }



    // Find and return where to insert a new string in an alphabetically ordered list.
    public static int getIndex(ListModel model, String target) {
        int high = model.getSize(), low = -1, probe;
        while (high - low > 1) {
            probe = (high + low) / 2;
            if (target.compareToIgnoreCase((String) model.getElementAt(probe)) < 0) {
                high = probe;
            } else {
                low = probe;
            }
        }
        return (low + 1);
    }

    // Find and return where to insert a new string in an alphabetically ordered
    // JComboBox.
    public static int getIndex(JComboBox combo, String target) {
        return getIndex(combo.getModel(), target);
    }

    public static int getIndex(List<String> v, String target) {
        int high = v.size(), low = -1, probe;
        while (high - low > 1) {
            probe = (high + low) / 2;
            if (target.compareToIgnoreCase(v.get(probe)) < 0) {
                high = probe;
            } else {
                low = probe;
            }
        }
        return (low + 1);
    }

    byte byte2HexDigit(byte n) {
        if (n < 10) {
            return (byte) ('0' + n);
        } else {
            return (byte) ('A' + n - 10);
        }
    }


    // Recursively delete a directory.
    private boolean deleteFile(File f) {
        System.out.println("Deleting " + f.getName());
        if (f.isFile()) {
            return f.delete();
        }
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (!deleteFile(files[i])) {
                return false;
            }
        }
        return f.delete();
    }

    // Note: Swing used to say that you could create the GUI on the main thread
    // as long as you didn't modify components that had already been realized.
    // Realized means that the component has been painted onscreen, or is ready
    // to be painted. The methods setVisible(true) and pack() cause a window to be
    // realized, which in turn causes the components it contains to be realized.
    // While this worked for most applications, in certain situations it could
    // cause problems. This is true for the Admin Tool, at least when I run it
    // from a session on a slower (XARAX) computer. In that case, sometimes
    // the Admin Tool would not come up because it would
    // deadlock when updating the picture area if the picture area had not yet
    // been realized. To avoid the possibility of thread problems, I adopted
    // the Swing authors recommendation to use invokeLater to create the GUI
    // on the event-dispatching thread.
    public static void main(String[] args) {
        // Process possible args
        String host = null;
        int port = 0;
        String session = null;
        String entity = null;
        String type = null;
        boolean debugFlag = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-debug")) {
                dbg = true;
            } else if (args[i].equals("-enginehost")) {
                hst = args[++i];
            } else if (args[i].equals("-engineport")) {
                prt = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-session")) {
                ses = args[++i];
            } else if (args[i].equals("-entity")) {
                ent = args[++i];
            } else if (args[i].equals("-type")) {
                typ = args[++i];
            }
        }

        DialogUtils.getAllSystemProperties(PM_APPLICATION_NAME);
        /*
        // Create the GUI
        PmAdmin pmadmin = new PmAdmin("Policy Machine Administration Tool", debugFlag, host, port, session, entity, type);
        pmadmin.pack();
        pmadmin.show();
         */
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createGUI();
            }
        });
    }
    static boolean dbg;
    static String hst;
    static int prt;
    static String ses;
    static String ent;
    static String typ;


    private static void createGUI() {
        long start = System.currentTimeMillis();
        PmAdmin pmadmin = new PmAdmin("Administration Tool", dbg, hst, prt, ses, ent, typ);
        pmadmin.pack();
        GraphicsUtil.centerFrame(pmadmin);
        pmadmin.setVisible(true);
        long end = System.currentTimeMillis();
        System.out.println("Time to load admin tool: " + (end-start));
    }
    private static final String PM_APPLICATION_NAME = "PM Admin Tool";


    /** Capture mouse events on the tree.  If a node is double clicked,
     * we call createNodes() to create two levels of the tree from the 
     * double-clicked node.  This will have to be changed to call the policy
     * machine mechanism for getting descendants or ancestors.
     */
    class PmMouseListener extends MouseAdapter {
        @Override
		public void mouseReleased(MouseEvent e){
            if(e.isPopupTrigger()){
                showPopup(e);
            }
        }

        @Override
		public void mousePressed(MouseEvent e) {
            if(e.isPopupTrigger()){
                showPopup(e);
            }
        }
        public void showPopup(MouseEvent e){
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

            if (selRow != -1) {
                PmNode node = (PmNode) selPath.getLastPathComponent();
                 rightClickedNode = node;
                 pmPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}

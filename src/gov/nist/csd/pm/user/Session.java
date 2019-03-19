/*
 * Session.java
 *
 * Created on May 2, 2005, 10:09 AM
 *
 * Serban I. Gavrila
 * VDG Inc.
 *
 */
package gov.nist.csd.pm.user;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;

import gov.nist.csd.pm.admin.PmAdmin;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.browser.*;
import gov.nist.csd.pm.common.graphics.GraphicsUtil;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;
import gov.nist.csd.pm.common.util.Log;
import gov.nist.csd.pm.common.util.lang.Strings;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static gov.nist.csd.pm.common.net.LocalHostInfo.getLocalHost;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
@SuppressWarnings("CallToThreadDumpStack")

public class Session extends JDialog implements ActionListener, TreeExpansionListener {

	// Use Log.Level.INFO to remove debug statements from output.
	Log log = new Log(Log.Level.INFO, true);

	public static final String PM_ALT_DELIM_PATTERN = "\\|";
	public static final String PM_CONNECTOR_ID = "1";
	public static final String PM_CONNECTOR_NAME = "PM";
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
	public static final String PM_OBJ = "o"; // Gopi - replaced "ob" with "o" as type name changed for object
	public static final String PM_ARC = "r";
	public static final String PM_CLASS_FILE_NAME = "File";
	public static final String PM_CLASS_DIR_NAME = "Directory";
	public static final String PM_CLASS_SGRAPH_NAME = "Subgraph";
	public static final String PM_CLASS_USER_NAME = "User";
	public static final String PM_CLASS_UATTR_NAME = "User attribute";
	public static final String PM_CLASS_OBJ_NAME = "Object";
	public static final String PM_CLASS_OATTR_NAME = "Object attribute";
	public static final String PM_CLASS_CONN_NAME = "Connector";
	public static final String PM_CLASS_POL_NAME = "Policy class";
	public static final String PM_CLASS_OPSET_NAME = "Operation set";
	public static final String PM_CLASS_RECORD_NAME = "Record";
	public static final String PM_OFFICE = "OpenOffice";
	public static final String PM_RTF = "PM RTF Editor";
	public static final String PM_WKF_OLD = "PM WORKFLOWOLD";
	public static final String PM_DUMMY = "Dummy";
	public static final String PM_EMAIL = "PM Email";
	public static final String PM_OBJTYPE_RTF = "rtf";
	public static final String PM_OBJTYPE_EML = "eml";
	public static final String PM_OBJTYPE_WKF = "wkf";
	public static final String PM_OBJTYPE_DOC = "doc";
	public static final String PM_OBJTYPE_PPT = "ppt";
	public static final String PM_OBJTYPE_XLS = "xls";
	public static final String WORD_EDITOR = "MS Word Editor";
	public static final String ADMIN_TOOL = "PM Admin Tool";
	public static final String PM_VOS_PRES_ADMIN = "admin";
	public static final String PM_VOS_PRES_USER = "user";
	public static final String PM_DIRECTION_UP = "up";
	public static final String PM_DIRECTION_DOWN = "down";
	public static final String PM_GRAPH_UATTR = "ua";
	public static final String PM_GRAPH_OATTR = "oa";
	// Steve - Deleted (4/14/16): Commented-out
	//private String glbGraphType = PM_GRAPH_OATTR;
	private PmNodeChildDelegate childDelegate;
	private SessionManager manager;
	private SSLSocketClient sslClient;
	private CommandEditor cmdEditor; 
	private SessConfigEditor configEditor;
	private PcEditor pcEditor;
	private UserEditor userEditor;
	private UattrEditor uattrEditor;
	private OattrEditor oattrEditor;
	private PermEditor permEditor;
	private SSLSocketClient exporterClient;
	private String sKsPath = null;
	private String sTsPath = null;
	private String sSessionName = null;
	private String sessionId = null;
	private String sSessionUser = null;
	private String sSessionUserId = null;
	private String sSessionHost = null;
	private String sVosPresType = PM_VOS_PRES_USER;// The default presentation.
	private PmGraph tree;
	private PmNode root;
	// Steve - Deleted (4/14/16): Commented-out
	//private String direction = PM_DIRECTION_UP;
	private MouseListener mouseListener;
	private List<String> startupVector;
	private PmNode rightClickedNode;
	private PmNode leftSelectedNode;
	private PmNode markedNode;

	private JLabel viewLabel;

	private JLabel clientLabel;

	private JPopupMenu pmUserPopup = null;

	private JPopupMenu pmAdminPopup = null;

	private JPopupMenu pmPopup = null;

	private JMenuItem grantMenuItem;

	private JMenuItem openMenuItem;
	private static final Cursor SESS_DEFAULT_CURSOR = Cursor
			.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR);
	private static final Cursor SESS_WAIT_CURSOR = Cursor
			.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR);
	private static final Cursor SESS_HAND_CURSOR = Cursor
			.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR);

	private HashSet threadSet;

	private Timer refreshTimer;

	private String sLastUpdateTimestamp;

	private ImageIcon userImageIcon = null;

	private List<TreePath> expanedNodes = new ArrayList<TreePath>();
	public static final String PM_SESSION_PROPERTY = "pmSession";


	private String sLastError;

	private Cursor crtDefaultCursor;

	private Cursor crtHandCursor;

	static String sessid;
	static String pid;
	static int simport;
	static String recname;
	static boolean debug;

	@SuppressWarnings("LeakingThisInConstructor")
	public Session(SessionManager manager, SSLSocketClient simClient,
			String sSessName, String sSessId, String sUserName, String sUserId,
			String sHostName) {
		this(manager, simClient, sSessName, sSessId, sUserName, sUserId,
				sHostName, false);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Session(SessionManager manager, SSLSocketClient simClient,
			String sSessName, String sSessId, String sUserName, String sUserId,
			String sHostName, boolean noGUI) {

		super(manager);
        log.debug("TRACE 6 - In Session constructor");

		setTitle(sSessName);
		setLocation(200, 200);

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent we) {
				doClose(true);// ask
			}
		});

		threadSet = new HashSet();

		this.manager = manager;
		this.sslClient = simClient;
		this.sSessionName = sSessName;
		this.sessionId = sSessId;
		this.sSessionUser = sUserName;
		this.sSessionUserId = sUserId;
		this.sSessionHost = sHostName;

		try {
			userImageIcon = GraphicsUtil.getImageIcon("/images/common/users/"
					+ sUserName + ".gif", getClass());
		} catch (Exception e) {
			userImageIcon = GraphicsUtil.getImageIcon(
					"/images/common/users/unknown-person.gif", getClass());
		}

		// Tell the engine to prepare the VOS graph for the user of this
		// session.
		// At this time there is no session yet (we are in the constructor).
		/*Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("computeFastVos", sessionId,
					sVosPresType, sSessionUserId, sessionId);
			res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in computeVos: "
						+ res.getErrorMessage());
				doClose(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in computeVos: " + e.getMessage());
			doClose(false);
		}
		sLastUpdateTimestamp = res.getStringValue(0);*/

		if (noGUI == false) {
			pcEditor = new PcEditor(this, simClient);
			pcEditor.pack();
			userEditor = new UserEditor(this, simClient);
			userEditor.pack();
			uattrEditor = new UattrEditor(this, simClient);
			uattrEditor.pack();
			oattrEditor = new OattrEditor(this, simClient);
			oattrEditor.pack();
			permEditor = new PermEditor(this, simClient);
			permEditor.pack();

			if (manager.getExporterSession() != null) {
				int exporterPort = manager.getExporterPort();
				System.out.println("Trying to create the exporter client socket with port "
						+ exporterPort);
				try {
					exporterClient = new SSLSocketClient("localhost",
							exporterPort, true, "c,Sess");
				} catch (Exception e) {
					JOptionPane
					.showMessageDialog(null,
							"Unable to create SSL socket for Exporter on the local host.");
					e.printStackTrace();
					doClose(false);// don't ask
				}
			}

			crtDefaultCursor = SESS_DEFAULT_CURSOR;
			crtHandCursor = SESS_HAND_CURSOR;
			
			// Select the connector node as root node and ask the engine
			// for the tree data.
			String[] connectorData = getConnector();

			// TODO: SWTICH BACK TO UP_MELL
			childDelegate = new PmNodeChildDelegate(simClient, 
					sessionId == null ? "" : sessionId, 
							// Steve - Modified (4/14/16): was 'UP'
							PmGraphDirection.UP_MELL, 
							// Steve - Modified (4/14/16): was 'USER_ATTRIBUTES'
							PmGraphType.USER_MELL_ATTRIBUTES);
			
	        log.debug("TRACE 7 - In Session constructor - Create PmNode");

			root = new PmNode(connectorData, childDelegate);

	        log.debug("TRACE 9 - In Session constructor - Calling PmGraph constructor");

			tree = new PmGraph(root, false);
			
            log.debug("TRACE 21 - In Session constructor - After tree = new PmGraph()");

			if (connectorData == null) {
				log.error("ConnectorData is null in Session");
			}


			viewLabel = new JLabel("Now viewing: " + sSessionUser + " POS");
			
            log.debug("TRACE 99 - Calling doViewAcesGraph!");

			doViewAcesGraph();  // Only show objects/attributes
			
            log.debug("TRACE 100 - Out of doViewAcesGraph!");


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

			JLabel userImageLabel = new JLabel(userImageIcon);
			userImageLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			int maxButtonSize = 30;
			userImageLabel.setPreferredSize(new Dimension(maxButtonSize,
					maxButtonSize));
			userImageLabel.setMaximumSize(new Dimension(maxButtonSize,
					maxButtonSize));
			toolBarPanel.add(toolBar, BorderLayout.WEST);
			toolBarPanel.add(userImageLabel, BorderLayout.EAST);

			JScrollPane treeScrollPane = new JScrollPane(tree);
			treeScrollPane.setPreferredSize(new Dimension(400, 500));

			clientLabel = new JLabel("Running as: " + sSessionUser);
			JPanel labelPane = new JPanel();
			labelPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
			labelPane.setLayout(new BorderLayout());
			labelPane.add(viewLabel, BorderLayout.WEST);
			labelPane.add(clientLabel, BorderLayout.EAST);

			JPanel labelAndScrollPane = new JPanel();
			labelAndScrollPane.setLayout(new BorderLayout());
			labelAndScrollPane.add(labelPane, BorderLayout.NORTH);
			labelAndScrollPane.add(treeScrollPane, BorderLayout.CENTER);

			JPanel contentPane = (JPanel) getContentPane();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(toolBarPanel, BorderLayout.NORTH);
			contentPane.add(labelAndScrollPane, BorderLayout.CENTER);

			// The Menu bar.
			JMenuBar menuBar = new JMenuBar();
			setJMenuBar(menuBar);

			// The File menu.
			JMenu fileMenu = new JMenu("File");
			menuBar.add(fileMenu);
			// All unused/unimplemented Menu items have been commented out
			JMenuItem menuItem = new JMenuItem("New");

			fileMenu.addSeparator();

			JMenuItem openMenuItem = new JMenuItem("Open");
			openMenuItem.addActionListener(this);
			fileMenu.add(openMenuItem);

			fileMenu.addSeparator();

			grantMenuItem = new JMenuItem("Grant To...");
			grantMenuItem.addActionListener(this);
			fileMenu.add(grantMenuItem);

			fileMenu.addSeparator();

			menuItem = new JMenuItem("Properties");
			menuItem.addActionListener(this);
			fileMenu.add(menuItem);

			fileMenu.addSeparator();

			menuItem = new JMenuItem("Exit", KeyEvent.VK_Q);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
					ActionEvent.CTRL_MASK));
			menuItem.addActionListener(this);
			fileMenu.add(menuItem);

			// The View menu.
			JMenu viewMenu = new JMenu("View");
			menuBar.add(viewMenu);

			viewMenu.addSeparator();

			menuItem = new JMenuItem("Admin view");
			menuItem.addActionListener(this);
			viewMenu.add(menuItem);

			menuItem = new JMenuItem("User view");
			menuItem.addActionListener(this);
			viewMenu.add(menuItem);

			viewMenu.addSeparator();

			menuItem = new JMenuItem("Refresh");
			menuItem.addActionListener(this);
			viewMenu.add(menuItem);

			// The Tools menu.
			JMenu toolsMenu = new JMenu("Tools");
			menuBar.add(toolsMenu);

			menuItem = new JMenuItem("Change password...");
			menuItem.addActionListener(this);
			toolsMenu.add(menuItem);

			toolsMenu.addSeparator();

			toolsMenu.addSeparator();

			JMenu configureMenu = new JMenu("Configure");

			menuItem = new JMenuItem("Key stores...");
			menuItem.addActionListener(this);
			configureMenu.add(menuItem);

			toolsMenu.add(configureMenu);

			// The user popup menu.
			pmUserPopup = new JPopupMenu();

			menuItem = new JMenuItem("Open");
			menuItem.addActionListener(this);
			pmUserPopup.add(menuItem);

			pmUserPopup.addSeparator();

			menuItem = new JMenuItem("Grant To...");
			menuItem.addActionListener(this);
			pmUserPopup.add(menuItem);

			pmUserPopup.addSeparator();

			pmUserPopup.addSeparator();

			menuItem = new JMenuItem("Properties");
			menuItem.addActionListener(this);
			pmUserPopup.add(menuItem);

			// The admin popup menu.
			pmAdminPopup = new JPopupMenu();

			menuItem = new JMenuItem("Add policy class...");
			menuItem.addActionListener(this);
			pmAdminPopup.add(menuItem);

			pmAdminPopup.addSeparator();

			menuItem = new JMenuItem("Add user attribute...");
			menuItem.addActionListener(this);
			pmAdminPopup.add(menuItem);

			menuItem = new JMenuItem("Add user...");
			menuItem.addActionListener(this);
			pmAdminPopup.add(menuItem);

			pmAdminPopup.addSeparator();

			menuItem = new JMenuItem("Add object attribute...");
			menuItem.addActionListener(this);
			pmAdminPopup.add(menuItem);

			pmAdminPopup.addSeparator();

			menuItem = new JMenuItem("Mark node");
			menuItem.addActionListener(this);
			pmAdminPopup.add(menuItem);

			menuItem = new JMenuItem("Assign marked node");
			menuItem.addActionListener(this);
			pmAdminPopup.add(menuItem);

			menuItem = new JMenuItem("Delete assignment");
			menuItem.addActionListener(this);
			pmAdminPopup.add(menuItem);

			pmAdminPopup.addSeparator();

			menuItem = new JMenuItem("Delete node");
			menuItem.addActionListener(this);
			pmAdminPopup.add(menuItem);

			pmAdminPopup.addSeparator();

			menuItem = new JMenuItem("Set permissions on...");
			menuItem.addActionListener(this);
			pmAdminPopup.add(menuItem);

			// Set the user popup as default.
			pmPopup = pmUserPopup;

			cmdEditor = new CommandEditor(manager, this, simClient);
			cmdEditor.pack();
			
            log.debug("TRACE 22 - In Session constructor - GUI create END");

		}
		configEditor = new SessConfigEditor(this);
		configEditor.pack();
		refreshTimer = new Timer(7000, this);
		if (!checkKeyStores()) {
			JOptionPane.showMessageDialog(this,
					"No keystores found or set for " + sUserName + " and host "
							+ sHostName + "!");
			doClose(false); // don't ask
		}
	}

	public void treeExpanded(TreeExpansionEvent e) {
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent e) {
	}

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

	public Packet makeCmd(String sCode, String... sArgs) throws Exception {
		return CommandUtil.makeCmd(sCode, sessionId == null ? "" : sessionId, sArgs);
	}

	public PmGraph getVosGraph() {
		return tree;
	}

	// Try to obtain the keystores paths from the engine,
	// or else from the user.
	private boolean checkKeyStores() {
		if (sKsPath != null && sKsPath.length() > 0 && sTsPath != null
				&& sTsPath.length() > 0) {
			return true;
		}

		// Try to get the paths from the engine.
		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("getKStorePaths", sessionId);
			res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in getKStorePaths: "
						+ res.getErrorMessage());
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Exception in getKStorePaths: "
					+ e.getMessage());
			return false;
		}

		// If the keystores paths are stored in the engine, then the answer
		// contains:
		// item 0: absolute path of the user keystore or the empty string;
		// item 1: absolute path of the user truststore or the empty string.
		if (res.size() >= 2) {
			sKsPath = res.getStringValue(0);
			sTsPath = res.getStringValue(1);
		}

		// Even now, a path may be empty. Give the user a chance to set it.
		if (sKsPath != null && sKsPath.length() > 0 && sTsPath != null
				&& sTsPath.length() > 0) {
			return true;
		}

		// Otherwise ask the user to configure.
		doKeyStores();

		if (sKsPath != null && sKsPath.length() > 0 && sTsPath != null
				&& sTsPath.length() > 0) {
			return true;
		}

		return false;
	}

	public void setKStorePaths(String sKsPath, String sTsPath) {
		this.sKsPath = sKsPath;
		this.sTsPath = sTsPath;

		// Store the paths in the engine for later.
		String sHost = getLocalHost();
		if (sHost == null) {
			JOptionPane.showMessageDialog(this,
					"Failed to obtain the local host name!");
			return;
		}

		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("setKStorePaths", null,
					sSessionUserId, sHost, sKsPath, sTsPath);
			res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in setKStorePaths: "
						+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Exception in setKStorePaths: "
					+ e.getMessage());
			return;
		}
	}

	private void addButtons(JToolBar toolBar) {
		JButton button = new JButton();
		int maxButtonSize = 30;
		button.setPreferredSize(new Dimension(maxButtonSize, maxButtonSize));
		button.setMaximumSize(new Dimension(maxButtonSize, maxButtonSize));
		button.setActionCommand("Change direction");
		button.addActionListener(this);
		button.setToolTipText("Change navigation direction");
		button.setIcon(GraphicsUtil.getImageIcon("/images/changeDir.gif",
				getClass()));
		toolBar.add(button);

		JButton reloadButton = new JButton();
		reloadButton.setPreferredSize(new Dimension(maxButtonSize, maxButtonSize));
		reloadButton.setMaximumSize(new Dimension(maxButtonSize, maxButtonSize));
		reloadButton.setActionCommand("Reload graph");
		reloadButton.addActionListener(this);
		reloadButton.setToolTipText("Reload graph");
		reloadButton.setIcon(GraphicsUtil.getImageIcon("/images/reload.gif",
				getClass()));
		toolBar.add(reloadButton);

	}

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
/*	// Get two levels of descendants or ancestors and hook them up the
	// argument node.
	public void getTwoLevels(PmNode node) {
		log.info("In getTwoLevels");
		if (node == null) {
			log.error("STEVE - mNode is null");
		} else if (node.children == null) {
			log.warn("STEVE - PmNode children is null");
		}

		if (node.children == null || node.children.isEmpty()) {
			log.debug("STEVE - children empty");
			// Get level1 children data
			Vector level1ChildrenData = null;

			PmNodeType nodeType = node.sType;
			String nodeTypeName = nodeType.typeCode();

			if (direction.equalsIgnoreCase(PM_DIRECTION_UP)) {
	        	log.debugStackCall("!!!!!!! getMellMembersOf - trace 1");	
//				level1ChildrenData = getMembersOf(nodeTypeName, node.sId,
//						node.sLabel);
				level1ChildrenData = getMellMembersOf(nodeTypeName, node.sId,
						node.sLabel);

			} else {
	        	log.debugStackCall("!!!!!!! getMellMembersOf - trace 1");	
//				level1ChildrenData = getContainersOf(nodeTypeName, node.sId,
//						node.sLabel);
				level1ChildrenData = getMellContainersOf(nodeTypeName, node.sId,
						node.sLabel);
			}

			if (level1ChildrenData != null) {
				log.debug("STEVE - level1 Children is not null!");
				// Create level1 children nodes
				String[] childData = null;
				for (int i = 0; i < level1ChildrenData.size(); i++) {
					childData = (String[]) level1ChildrenData.get(i);
					PmNode childNode = new PmNode(childData);
					PmNode.linkNodes(node, childNode);
					log.debug("STEVE - Getting level2 children of childNode: "
							+ childNode.sId);

					// Now get level2 children data from this node
					Vector level2ChildrenData;
					PmNodeType childNodeType = childNode.sType;
					String childNodeTypeName = childNodeType.typeCode();

					if (direction.equalsIgnoreCase(PM_DIRECTION_UP)) {
			        	log.debugStackCall("!!!!!!! getMellMembersOf - trace 2");	
//						level2ChildrenData = getMembersOf(childNodeTypeName,
//								childNode.sId, childNode.sLabel);
						level2ChildrenData = getMellMembersOf(childNodeTypeName,
								childNode.sId, childNode.sLabel);
					} else {
						log.debug("STEVE - START calling getContainersOf() for level2 children");
//						level2ChildrenData = getContainersOf(childNodeTypeName,
//								childNode.sId, childNode.sLabel);
						level2ChildrenData = getMellContainersOf(childNodeTypeName,
								childNode.sId, childNode.sLabel);
						
						log.debug("STEVE - END calling getContainersOf() for level2 children");
					}

					if (level2ChildrenData != null) {
						String[] grandChildData = null;
						for (int j = 0; j < level2ChildrenData.size(); j++) {
							grandChildData = (String[]) level2ChildrenData
									.get(j);
							PmNode grandChildNode = new PmNode(grandChildData);
							PmNode.linkNodes(childNode, grandChildNode);
							log.debug("STEVE - grandChildData1: "
									+ grandChildNode.sId);
						}
					} else {
						log.debug("STEVE - level2 Children is null!");
					}
				}
			} else {
				log.debug("STEVE - level1 Children is null!");

			}
		} else if (!node.children.isEmpty()) {
			log.debug("STEVE - children exist");
			PmNode childNode = null;
			for (int i = 0; i < node.children.size(); i++) {
				childNode = (PmNode) node.children.get(i);
				if (childNode.children.isEmpty()) {
					// Now get level2 children data from this node
					Vector level2ChildrenData;
					PmNodeType nodeType = childNode.sType;
					String nodeTypeName = nodeType.typeCode();

					if (direction.equalsIgnoreCase(PM_DIRECTION_UP)) {
			        	log.debugStackCall("!!!!!!! getMellMembersOf - trace 3");	
//						level2ChildrenData = getMembersOf(nodeTypeName,
//								childNode.sId, childNode.sLabel);
						level2ChildrenData = getMellMembersOf(nodeTypeName,
								childNode.sId, childNode.sLabel);
					} else {
//						level2ChildrenData = getContainersOf(nodeTypeName,
//								childNode.sId, childNode.sLabel);
						level2ChildrenData = getMellContainersOf(nodeTypeName,
								childNode.sId, childNode.sLabel);
					}

					if (level2ChildrenData != null) {
						String[] grandChildData = null;
						for (int j = 0; j < level2ChildrenData.size(); j++) {
							grandChildData = (String[]) level2ChildrenData
									.get(j);
							PmNode grandChildNode = new PmNode(grandChildData);
							PmNode.linkNodes(childNode, grandChildNode);
							log.debug("STEVE - grandChildData2: "
									+ grandChildNode.sId);
						}
					} else {
						log.debug("STEVE - level2 Children is null!");
					}
				}
			}
		}

	}*/

/*	private Vector getMembersOf(String sType, String sId, String sLabel) {
		Packet res = null;
		try {
        	log.debugStackCall("!!!!!!! getMembersOf - trace 4");	

			Packet cmd = makeCmd("getMembersOf", sessionId, sLabel, sId, sType,
					glbGraphType);
			res = sslClient.sendReceive(cmd, null);
			
			System.out.println("***** Res size: " + res.size());
			System.out.println("****** Accessible: " + res.getItem(res.size() - 1));
			if (res == null) {
				JOptionPane.showMessageDialog(this,
						"Undetermined error; null result returned");
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

		Vector v = new Vector();
		if (res != null)
			for (int i = 0; i < res.size(); i++) {
				String sLine = res.getStringValue(i);
				String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
				v.add(pieces);
			}
		return v;
	}*/
	
/*	private Vector getMellMembersOf(String sType, String sId, String sLabel) {
		log.info("In getMellMembersOf");

		Packet res = null;
		try {
        	log.debugStackCall("!!!!!!! getMellMembersOf - trace 4");	

			Packet cmd = makeCmd("getMellMembersOf", sessionId, sLabel, sId, sType,
					glbGraphType);
			res = sslClient.sendReceive(cmd, null);
			
			System.out.println("***** Res size: " + res.size());
			System.out.println("****** Accessible: " + res.getItem(res.size() - 1));
			if (res == null) {
				JOptionPane.showMessageDialog(this,
						"Undetermined error; null result returned");
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

		Vector v = new Vector();
		if (res != null)
			for (int i = 0; i < res.size(); i++) {
				String sLine = res.getStringValue(i);
				String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
				v.add(pieces);
			}
		return v;
	}*/

/*	private Vector getContainersOf(String sType, String sId, String sLabel) {
		Packet res = null;
		try {
			Packet cmd = makeCmd("getContainersOf", sessionId, sLabel, sId, sType,
					glbGraphType);
			res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this,
						"Undetermined error; null result returned");
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

		Vector v = new Vector();
		if (res != null)
			for (int i = 0; i < res.size(); i++) {
				String sLine = res.getStringValue(i);
				String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
				v.add(pieces);
			}
		return v;
	}*/
	
/*	private Vector getMellContainersOf(String sType, String sId, String sLabel) {
		log.info("In getMellContainersOf");
		Packet res = null;
		try {
			Packet cmd = makeCmd("getMellContainersOf", sessionId, sLabel, sId, sType,
					glbGraphType);
			res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this,
						"Undetermined error; null result returned");
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

		Vector v = new Vector();
		if (res != null)
			for (int i = 0; i < res.size(); i++) {
				String sLine = res.getStringValue(i);
				String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
				v.add(pieces);
			}
		return v;
	}*/
	
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

	private void processRefreshTimerEvent() {
		conditionalRefresh();
	}

	private void conditionalRefresh() {
		final SwingWorker worker = new SwingWorker() {

			@Override
			public Object construct() {
				Packet res = null;
				try {
					Packet cmd = CommandUtil.makeCmd("isTimeToRefresh",
							sessionId, sLastUpdateTimestamp);
					res = sslClient.sendReceive(cmd, null);
					if (res.hasError()) {
						JOptionPane.showMessageDialog(
								Session.this,
								"Error in isTimeToRefresh: "
										+ res.getErrorMessage());
						return null;
					}
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(Session.this,
							"Exception in isTimeToRefresh: " + e.getMessage());
					return null;
				}

				if (!(res.getStringValue(0)).equals("yes")) {
					return null; // No need to refresh.
				}
				try {
					Packet cmd = CommandUtil.makeCmd("computeFastVos", sessionId,
							sVosPresType, sSessionUserId, sessionId);
					res = sslClient.sendReceive(cmd, null);
					if (res.hasError()) {
						JOptionPane
						.showMessageDialog(
								Session.this,
								"Error in computeVos: "
										+ res.getErrorMessage());
						return null;
					}
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(Session.this,
							"Exception in computeVos: " + e.getMessage());
					return null;
				}
				sLastUpdateTimestamp = res.getStringValue(0);

				resetTree(root);
				return SessionManager.success();
			}

			@Override
			public void finished() {
				if (viewLabel != null) {
					if (sVosPresType.equalsIgnoreCase(PM_VOS_PRES_USER)) {
						viewLabel.setText("Now viewing: user POS");
					} else {
						viewLabel.setText("Now viewing: admin POS");
					}
				}
			}
		};
	}

	@Override
	public String getName() {
		return sSessionName;
	}

	public String getId() {
		return sessionId;
	}

	public String getUser() {
		return sSessionUser;
	}

	public String getUserId() {
		return sSessionUserId;
	}

	public String getHost() {
		return sSessionHost;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();

		if (src.equals(refreshTimer)) {
			processRefreshTimerEvent();
			return;
		}

		String sCommand = null;
		if (src instanceof JButton) {
			sCommand = ((JButton) src).getActionCommand();
		} else {
			sCommand = ((JMenuItem) src).getText();
		}

		if (sCommand.equals("Exit")) {
			doClose(true);
		} else if (sCommand.equalsIgnoreCase("Change direction")) {
			doChangeDirection();
		} else if (sCommand.equalsIgnoreCase("Reload graph")) {
			System.out.println("CURRENT DIRECTION before reload: " + childDelegate.getDirection().name());
			doUserView();
			System.out.println("CURRENT DIRECTION after reload: " + childDelegate.getDirection().name());
			if (childDelegate.getDirection() == PmGraphDirection.DOWN_MELL) {
				doChangeDirection();
				System.out.println("CURRENT DIRECTION after direction change: " + childDelegate.getDirection().name());
			}
		} else if (sCommand.equalsIgnoreCase("Admin view")) {
			doAdminView();
		} else if (sCommand.equalsIgnoreCase("User view")) {
			doUserView();
        } else if (sCommand.equals("Objects/Attributes with ACEs")) {
            doViewAcesGraph();            
		} else if (sCommand.equalsIgnoreCase("Grant to...")) {
			// This event could come from the user right-clicking an object
			// and selecting "Grant to..." popup menu, or from the user
			// selecting the menu "File/Grant to...".
			if (src == grantMenuItem) {
				doGrantOnSelObj();
			} else {
				doGrantOnRcObj();
			}
		} else if (sCommand.equalsIgnoreCase("Send to...")) {
			doEmail();
		} else if (sCommand.equalsIgnoreCase("Export to...")) {
			doExport();
		} else if (sCommand.equalsIgnoreCase("Change password...")) {
			doChangePassword();
		} else if (sCommand.equals("Run command...")) {
			doCommand();
		} else if (sCommand.equals("E-grant")) {
			doLaunchGrantor(null, true);// true doesn't matter.
		} else if (sCommand.equals("Medical Record Editor")) {
			doLaunchMREditor(null);
		} else if (sCommand.equals("Schema Builder")) {
			doLaunchSchemaBuilder();
		} else if (sCommand.equals("Table Editor")) {
			doLaunchTableEditor();
		} else if (sCommand.equals("Accounts Editor")) {
			doLaunchAcctEditor(null);
		} else if (sCommand.equals("Composer")) {
			System.err.println("Composer is not installed!");
			// /doLaunchComposer();
		} else if (sCommand.equals("Composite Viewer")) {
			System.err.println("Composite Viewer is not installed!");
			// /doLaunchCompositeViewer(null);
		} else if (sCommand.equals("RTF Editor")) {
			doLaunchRTFEditor(null);
		} else if (sCommand.equals("Dummy")) {
			doLaunchDummy(null);
		} else if (sCommand.equals("Admin Tool")) {
			doLaunchAdminTool(null, null);
		} else if (sCommand.equalsIgnoreCase("Workflow Old")) {
			doLaunchWorkFlowOld(null);
		} else if (sCommand.equals("Key stores...")) {
			doKeyStores();
			// } else if (sCommand.equals("Add startups")) {
			// doAddStartups();
		} else if (sCommand.equals("Set startups")) {
			doSetStartups();
		} else if (sCommand.equals("Select as startup")) {
			doSelectStartup();
		} else if (sCommand.equals("Test request permissions...")) {
			doTestRequestPerms();
			// } else if (sCommand.equals("Test read file")) {
			// doTestReadFile();
		} else if (sCommand.equals("Open")) {
			if (src == openMenuItem) {
				doOpenSelObj();
			} else {
				doOpenRcObj();
			}
//		} else if (sCommand.equals("Open With TH")) { //Gopi - is it being used?
//			doOpenObjectWithTH();
		} else if (sCommand.equals("Properties")) {
			doProperties();
		} else if (sCommand.equals("Edit")) {
			doEdit();
		} else if (sCommand.equals("Add policy class...")) {
			doAddPolicyClass();
		} else if (sCommand.equals("Add user attribute...")) {
			doAddUattr();
		} else if (sCommand.equals("Add user...")) {
			doAddUser();
		} else if (sCommand.equals("Add object attribute...")) {
			doAddOattr();
		} else if (sCommand.equals("Add object...")) {
			doAddObject();
		} else if (sCommand.equals("Mark node")) {
			doMarkNode();
		} else if (sCommand.equals("Assign marked node")) {
			doAssignNode();
		} else if (sCommand.equals("Delete assignment")) {
			doDeleteAssignment();
		} else if (sCommand.equals("Delete node")) {
			doDeleteNode();
		} else if (sCommand.equals("Set permissions on...")) {
			doSetPerms();
		}
	}

    private void doViewAcesGraph() {
    	if (childDelegate == null)
    		log.error("Child delegate is null in doViewAcesGraph");
    	if (root == null)
    		log.error("root is null in doViewAcesGraph");

    	// Steve - Modified (4/18/16): Was 'ACCESS_CONTROL_ENTRIES'
        childDelegate.setType(PmGraphType.MELL_ACCESS_CONTROL_ENTRIES);
        resetTree(root);
        viewLabel.setText("Now viewing: Objects/Attributes with ACEs");
    }
    
	private void doEmail() {
	}

	// The user has clicked the File/Grant To... menu item. The object
	// is the (left click) selected one.
	private void doGrantOnSelObj() {
		if (leftSelectedNode == null) {
			JOptionPane
			.showMessageDialog(this, "You have to select an object!");
			return;
		}

		System.out
		.println("Granting access rights on: "
				+ leftSelectedNode.getName() + " "
				+ leftSelectedNode.getType());
		/*
		 * if (!leftSelectedNode.getType().equalsIgnoreCase(PM_NODE_ASSOC)) {
		 * JOptionPane.showMessageDialog(this,
		 * "The entity you selected is not an object!"); return; }
		 */
		doLaunchGrantor(leftSelectedNode.getName(), true);
	}

	// The user has right-clicked on an object and selected the Grant To...
	// menu item from the popup menu.
	private void doGrantOnRcObj() {
		System.out
		.println("Granting access rights on: "
				+ rightClickedNode.getName() + " "
				+ rightClickedNode.getType());
		/*
		 * if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_ASSOC)) {
		 * JOptionPane.showMessageDialog(this,
		 * "The entity you selected is not an object!"); return; }
		 */
		doLaunchGrantor(rightClickedNode.getName(), true);
	}

	// Export the selected object to one of the mounted devices.
	// First ask the kernel to provide the list of mounted devices.
	// Let the user select one of the devices and a folder where to save
	// the object's underlying file.
	private void doExport() {
		System.out.println("Exporting the object " + rightClickedNode.getName()
				+ " " + rightClickedNode.getType());
		if (exporterClient == null) {
			JOptionPane.showMessageDialog(this, "No exporter present!");
			return;
		}

		String sObjName = rightClickedNode.getName();

		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("getDevices", sessionId);
			res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in getDevices: "
						+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Exception in getDevices: " + e.getMessage());
			return;
		}

		if (res != null) {
			for (int i = 0; i < res.size(); i++) {
				System.out.println(res.getStringValue(i));
			}
		}

		// Display a list of mounted devices and let the user select one.
		JLabel devLabel = new JLabel("Mounted devices:");
		DefaultListModel devListModel = new DefaultListModel();
		JList devList = new JList(devListModel);
		JScrollPane devListScrollPane = new JScrollPane(devList);
		devListScrollPane.setPreferredSize(new Dimension(200, 200));

		if (res != null) {
			for (int i = 0; i < res.size(); i++) {
				devListModel.addElement(res.getStringValue(i));
			}
		}

		String message = "Please select a device:";
		int ret = JOptionPane.showOptionDialog(null, new Object[] { message,
				devLabel, devListScrollPane }, "Select a device",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null);
		if (ret == JOptionPane.CANCEL_OPTION
				|| ret == JOptionPane.CLOSED_OPTION) {
			return;
		}

		String sSelDev = (String) devList.getSelectedValue();

		// Grant the exporter read permission on the object.
		res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("setPerms", sessionId,
					"Exporter", "", "", "", "", "File read", sObjName,
					PM_NODE_OATTR, "no");
			res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in setPerms: " + res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Exception in setPerms: " + e.getMessage());
			return;
		}

		// Now send a message to the exporter asking it to read the object and
		// create a copy of its underlying file on the selected device.
		res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("exportObject", sessionId,
					sObjName, sSelDev);
			res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in exportObject: "
						+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Exception in exportObject: "
					+ e.getMessage());
			return;
		}
	}

	private void pingExporter() {
		if (exporterClient == null) {
			JOptionPane.showMessageDialog(this, "No exporter present!");
			return;
		}
		try {
			Packet cmd = CommandUtil.makeCmd("ping", sessionId);
			Packet res = exporterClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(
						this,
						"Error while pinging the exporter: "
								+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Exception while pinging the exporter: " + e.getMessage());
			return;
		}
	}

	private void doSetPerms() {
		permEditor.prepare(rightClickedNode.getName(),
				rightClickedNode.getId(), rightClickedNode.getType());
		permEditor.setVisible(true);
	}

	private void doDeleteNode() {
		try {
			Packet cmd = CommandUtil.makeCmd(
					"deleteNode", // commented by Gopi -- sSessionId,
					sessionId, rightClickedNode.getId(),
					rightClickedNode.getType(), "yes");
			Packet res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in deleteNode: "
						+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Exception in deleteNode: " + e.getMessage());
			return;
		}
	}

	private void doEdit() {
		String sType = rightClickedNode.getType();
		if (sType.equalsIgnoreCase(PM_NODE_UATTR)) {
			uattrEditor.prepareForEdit(rightClickedNode.getId());
			uattrEditor.setVisible(true);
		} else if (sType.equalsIgnoreCase(PM_NODE_OATTR)
				|| sType.equalsIgnoreCase(PM_NODE_ASSOC)) {
			oattrEditor.prepareForEdit(rightClickedNode.getId());
			oattrEditor.setVisible(true);
		} else if (sType.equalsIgnoreCase(PM_NODE_POL)) {
			pcEditor.prepareForEdit(rightClickedNode.getId());
			pcEditor.setVisible(true);
		}
	}

	private void doAddOattr() {
		System.out
		.println("You selected \"Add object attribute...\" clicking on:");
		System.out.println("              id =  " + rightClickedNode.getId());
		System.out.println("           label =  " + rightClickedNode.getName());
		System.out.println("            type =  " + rightClickedNode.getType());

		// The clicked node must be the connector, a policy class, or an object
		// attribute.
		if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)
				&& !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_POL)
				&& !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_OATTR)) {
			JOptionPane.showMessageDialog(this,
					"You cannot add an object attribute to the selected node!");
			return;
		}

		oattrEditor.prepareForAdd(rightClickedNode.getId(),
				rightClickedNode.getType());
		oattrEditor.setVisible(true);
	}

	private void doAddObject() {
		System.out.println("You selected \"Add object...\" clicking on:");
		System.out.println("              id =  " + rightClickedNode.getId());
		System.out.println("           label =  " + rightClickedNode.getName());
		System.out.println("            type =  " + rightClickedNode.getType());

		// The clicked node must be the connector, a policy class, or an object
		// attribute.
		if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_OATTR)
				&& !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)
				&& !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_POL)) {
			JOptionPane.showMessageDialog(this,
					"You cannot add an object to the selected node!");
			return;
		}
		JOptionPane.showMessageDialog(this,
				"Not yet implemented. Use an application to create objects!");
	}

	private void doDeleteAssignment() {
		if (markedNode == null) {
			JOptionPane.showMessageDialog(this,
					"Please mark a node for de-assignment!");
			return;
		}
		try {
			Packet cmd = CommandUtil
					.makeCmd("deleteAssignment",
							sessionId,
							null, // Added by Gopi
							markedNode.getId(), markedNode.getType(),
							rightClickedNode.getId(),
							rightClickedNode.getType(), "yes");
			Packet res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in deleteAssignment: " + res.getErrorMessage());
				return;
			}
			markedNode = null;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Exception in deleteAssignment: " + e.getMessage());
			return;
		}
	}

	private void doAssignNode() {
		if (markedNode == null) {
			JOptionPane.showMessageDialog(this,
					"Please mark a node for assignment!");
			return;
		}
		try {
			Packet cmd = CommandUtil
					.makeCmd("assign",
							sessionId,
							null, // added null parameter by Gopi
							markedNode.getId(), markedNode.getType(),
							rightClickedNode.getId(),
							rightClickedNode.getType(), "yes");
			Packet res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in assign: " + res.getErrorMessage());
				return;
			}
			markedNode = null;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Exception in assign: " + e.getMessage());
			return;
		}
	}

	private void doMarkNode() {
		markedNode = new PmNode(rightClickedNode.getType(),
				rightClickedNode.getId(), rightClickedNode.getName());
	}

	private void doAddUattr() {
		System.out
		.println("You selected \"Add user attribute...\" clicking on:");
		System.out.println("              id =  " + rightClickedNode.getId());
		System.out.println("           label =  " + rightClickedNode.getName());
		System.out.println("            type =  " + rightClickedNode.getType());

		// The clicked node must be the connector, a policy class, or a user
		// attribute.
		if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_UATTR)
				&& !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)
				&& !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_POL)) {
			JOptionPane.showMessageDialog(this,
					"You cannot add a user attribute to the selected node!");
			return;
		}

		uattrEditor.prepareForAdd(rightClickedNode.getId(),
				rightClickedNode.getType());
		uattrEditor.setVisible(true);
	}

	private void doAddUser() {
		System.out.println("You selected \"Add user...\" clicking on:");
		System.out.println("              id =  " + rightClickedNode.getId());
		System.out.println("           label =  " + rightClickedNode.getName());
		System.out.println("            type =  " + rightClickedNode.getType());

		// The clicked node must be the connector or a user attribute.
		if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_UATTR)
				&& !rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)) {
			JOptionPane.showMessageDialog(this,
					"You cannot add a user to the selected node!");
			return;
		}

		userEditor
		.prepare(rightClickedNode.getId(), rightClickedNode.getType());
		userEditor.setVisible(true);
	}

	private void doAddPolicyClass() {
		System.out.println("You selected \"Add policy class...\" clicking on:");
		System.out.println("              id =  " + rightClickedNode.getId());
		System.out.println("           label =  " + rightClickedNode.getName());
		System.out.println("            type =  " + rightClickedNode.getType());

		// It's an error if the clicked node is not the connector node.
		if (!rightClickedNode.getType().equalsIgnoreCase(PM_NODE_CONN)) {
			JOptionPane.showMessageDialog(this,
					"You cannot add a policy class to the selected node!");
			return;
		}
		pcEditor.prepareForAdd();
		pcEditor.setVisible(true);
	}

	private void doAddStartups() {
		// Empty or create an empty internal list of startups. An element of the
		// list
		// is <pos id>.
		JOptionPane
		.showMessageDialog(
				this,
				"Right-click desired node, click popup menu \"Select as startup\", repeat. When finished, select \"Tools/Configure/Set startups\"");
		if (startupVector == null) {
			startupVector = new ArrayList<String>();
		} else {
			startupVector.clear();
		}
	}

	// Sends a command to the engine to store the startups for the
	// user of this session to the acative directory. The command arguments are
	// the session id (from which the engine can find the user), and the
	// startups (these are POS ids for this session). The engine must
	// translate these POS ids to original ids before storing them
	// to the AD.
	private void doSetStartups() {

		if (startupVector == null) {
			startupVector = new ArrayList<String>();
		}

		System.out.println("doSetStartups()");
		for (int i = 0; i < startupVector.size(); i++) {
			System.out.println("  POS id=" + startupVector.get(i));
		}

		// Prepare and send the command.
		try {
			Packet cmd = CommandUtil.makeCmd("setStartups", sessionId);
			for (int i = 0; i < startupVector.size(); i++) {
				cmd.addItem(ItemType.CMD_ARG, startupVector.get(i));
			}
			Packet res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in setStartups: "
						+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Exception in setStartups: "
					+ e.getMessage());
			return;
		}
		startupVector.clear();
	}

	private void doSelectStartup() {
		System.out.println("You selected the following node as a startup:");
		System.out.println("              id =  " + rightClickedNode.getId());
		System.out.println("           label =  " + rightClickedNode.getName());
		System.out.println("            type =  " + rightClickedNode.getType());
		if (startupVector == null) {
			startupVector = new ArrayList<String>();
		}
		if (startupVector.contains(rightClickedNode.getId())) {
			return;
		}
		startupVector.add(rightClickedNode.getId());
	}

	private void doKeyStores() {
		configEditor.setKStorePaths(sKsPath, sTsPath);
		configEditor.setVisible(true);
	}

	// Include here any action you want to take before closing the session.
	public void prepareToClose() {
		Iterator hsiter = threadSet.iterator();
		while (hsiter.hasNext()) {
			LauncherThread et = (LauncherThread) hsiter.next();
		}
		try {
			if (exporterClient != null) {
				exporterClient.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (refreshTimer != null) {
			refreshTimer.stop();
		}
		setVisible(false);
	}

	// Close this session.
	private void doClose(boolean ask) {
		/*if (!mayIClose()) {
			JOptionPane
			.showMessageDialog(
					this,
					"You may not close the session. Either internal error or some process still running...");
			return;
		}*/

		if (ask) {
			int option = JOptionPane.showConfirmDialog(this,
					"Do you really want to end this session?", "Close Session",
					JOptionPane.YES_NO_OPTION);
			if (option != JOptionPane.YES_OPTION) {
				return;
			}
		}

		prepareToClose();
		manager.logonMenuItem.setEnabled(true);
		manager.closeSession(sessionId);
	}

	private boolean mayIClose() {
		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("maySessionClose", sessionId);
			res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				return false;
			}
			if (res.hasError()) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in maySessionClose: " + e.getMessage());
			return false;
		}
	}

	private void doCommand() {
		cmdEditor.prepare();
		cmdEditor.setVisible(true);
	}

	private void doProperties() {
		System.out.println("You selected Open on:");
		System.out.println("              id =  " + rightClickedNode.getId());
		System.out.println("           label =  " + rightClickedNode.getName());
		System.out.println("            type =  " + rightClickedNode.getType());

		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("getObjEmailProps", sessionId,
					rightClickedNode.getName());
			res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in getObjEmailProps: " + res.getErrorMessage());
				return;
			}
			markedNode = null;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in getObjEmailProps: " + e.getMessage());
			return;
		}
		if (res.size() <= 0) {
			JOptionPane.showMessageDialog(this,
					"Empty result from getObjEmailProps!");
			return;
		}
		// The result returned by the engine contains:
		// item 0: the sender
		// item 1: recipients
		// item 2: timestamp
		// item 3: subject
		// item 4,...: attached objects
		DefaultListModel propListModel = new DefaultListModel();
		final JList propList = new JList(propListModel);
		JScrollPane propListScrollPane = new JScrollPane(propList);
		propListModel.addElement("From: " + res.getStringValue(0));
		propListModel.addElement("To: " + res.getStringValue(1));
		propListModel.addElement("Sent: " + res.getStringValue(2));
		propListModel.addElement("Subject: " + res.getStringValue(3));
		for (int i = 4; i < res.size(); i++) {
			propListModel.addElement("Attached: " + res.getStringValue(i));
		}

		String message = "Properties of email message "
				+ rightClickedNode.getName();
		JOptionPane.showOptionDialog(this, new Object[] { message,
				propListScrollPane }, null, JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, null, null);
	}

	private void doOpenObjectWithTH() {
		System.out.println("You selected Open on:");
		System.out.println("              id =  " + rightClickedNode.getId());
		System.out.println("           label =  " + rightClickedNode.getName());
		System.out.println("            type =  " + rightClickedNode.getType());

		// Get the properties of the object represented by this VOS node.
		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("getVosIdProperties", sessionId,
					sVosPresType, rightClickedNode.getId());
			res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane
				.showMessageDialog(
						this,
						"Error in getVosIdProperties: "
								+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in getVosIdProperties: " + e.getMessage());
			return;
		}
		if (res.size() <= 0) {
			JOptionPane
			.showMessageDialog(this,
					"Engine returned an empty result while getting VOS node properties");
			return;
		}

		// <name>|<id>|<class>|<inh>|<host or orig or tpl name>|<path or orig
		// id>
		String s = res.getStringValue(0);
		String[] pieces = s.split(PM_ALT_DELIM_PATTERN);
		String sClass = pieces[2];
		String sNameOrHost = null;
		if (pieces.length >= 5) {
			sNameOrHost = pieces[4];
		}
		String sIdOrPath = null;
		if (pieces.length >= 6) {
			sIdOrPath = pieces[5];
		}

		if (!sClass.equalsIgnoreCase(PM_CLASS_FILE_NAME)) {
			JOptionPane.showMessageDialog(this, "Object not of File class!");
			return;
		}
		if (!sIdOrPath.toLowerCase().endsWith(".rtf")) {
			JOptionPane.showMessageDialog(this, "Object content is not RTF!");
			return;
		}
		invokeRtfEditor(rightClickedNode.getName(), true);
	}

	// Invoked when the user right-clicks on a VOS node and selects "Open" from
	// the popup menu.
	private void doOpenRcObj() {
		doOpenObject(rightClickedNode.getId(), rightClickedNode.getName(),
				rightClickedNode.getType());
	}

	// Called when the user selects the object, then selects "Open" from the
	// main menu.
	private void doOpenSelObj() {
		if (leftSelectedNode == null) {
			JOptionPane
			.showMessageDialog(this, "You have to select an object!");
			return;
		}
		doOpenObject(leftSelectedNode.getId(), leftSelectedNode.getName(),
				leftSelectedNode.getType());
	}

	private void doOpenObject(String sSelectedId, String sSelectedLabel,
			String sSelectedType) {
		System.out.println("You selected Open on:");
		System.out.println("              id =  " + sSelectedId);
		System.out.println("           label =  " + sSelectedLabel);
		System.out.println("            type =  " + sSelectedType);

		// Get the properties of the object represented by this VOS node.
		Packet res = null;
		try {
			//Packet cmd = CommandUtil.makeCmd("getVosIdProperties", sessionId,
			//		sVosPresType, sSelectedId);
			Packet cmd = CommandUtil.makeCmd("getPosNodeProperties", sessionId,
					sVosPresType, sSelectedId, sSelectedLabel, sSelectedType);
			res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane
				.showMessageDialog(
						this,
						//"Error in getVosIdProperties: "
						"Error in getPosNodeProperties: "
								+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					//"Exception in getVosIdProperties: " + e.getMessage());
					"Exception in getPosNodeProperties: " + e.getMessage());
			return;
		}
		if (res.size() <= 0) {
			JOptionPane
			.showMessageDialog(this,
					//"Engine returned an empty result while getting VOS node properties");
					"Engine returned an empty result while getting POS node properties");
			return;
		}
		// The result returned by the engine contains a single item:
		// <name>|<id>|<class>|<inh>|<host or orig or tpl name>|<path or orig id>
		// where the name, id, class, host and path are those of the object
		// of class File,
		// and the orig name and id are those of the PM entity represented by
		// the virtual object. The name, id, class, and inh are always present
		// in the result. The others may be omitted.
		// If the class is Record, only the name, id, class, inh ("no"),
		// and <host or orig or tpl name> are present.

		// Invoke the application that corresponds to the file object type.
		String s = res.getStringValue(0);
		String[] pieces = s.split(PM_ALT_DELIM_PATTERN);
		String sClass = pieces[2];
		String sNameOrHost = null;
		if (pieces.length >= 5) {
			sNameOrHost = pieces[4];
		}
		String sIdOrPath = null;
		if (pieces.length >= 6) {
			sIdOrPath = pieces[5];
		}

		if (sClass.equalsIgnoreCase(PM_CLASS_FILE_NAME)) {

			String sName = rightClickedNode.getName();
			sIdOrPath = sIdOrPath.toLowerCase();
			// TODO: invoke the work flow if ext is .wkf.

			// get workflow path from session manager.
			if (sIdOrPath.endsWith(".rtf")) {
				invokeRtfEditor(sSelectedLabel, false);
			} else if (sIdOrPath.endsWith(".pdf")) {
				manager.getApplicationManager().launchClientApp(
						ApplicationManager.PDF_VIEWER_APP_NAME, sessionId,
						sName);
			} else if (sIdOrPath.endsWith(".config")) {
				manager.getApplicationManager().launchClientApp(
						ApplicationManager.WORKFLOW_EDITOR_APP_NAME,
						sessionId, sName);
			} else if (sIdOrPath.endsWith(".doc") || sIdOrPath.endsWith(".ppt")
					|| sIdOrPath.endsWith(".xls")) {
				String sObjType = Strings.getFileExtensionOfPath(sIdOrPath);
				invokeOffice(sObjType, sSelectedLabel);

			} else if (sIdOrPath.endsWith(".eml")) {
				invokeEmailer(sName);

			} else if (sIdOrPath.endsWith(".wkf")) {
				manager.getApplicationManager().launchClientApp(
						ApplicationManager.WORKFLOW_OLD, sessionId,
						sSelectedLabel);
				// invokeWorkFlowOld(sSelectedLabel);
			} else {
				JOptionPane.showMessageDialog(this,
						"Unknown content type for \"" + sIdOrPath.toLowerCase()
						+ "\"!");
				return;
			}

		} else if (sClass.equalsIgnoreCase(PM_CLASS_USER_NAME)
				|| sClass.equalsIgnoreCase(PM_CLASS_UATTR_NAME)
				|| sClass.equalsIgnoreCase(PM_CLASS_OATTR_NAME)
				|| sClass.equalsIgnoreCase(PM_CLASS_OBJ_NAME)
				|| sClass.equalsIgnoreCase(PM_CLASS_POL_NAME)
				|| sClass.equalsIgnoreCase(PM_CLASS_CONN_NAME)
				|| sClass.equalsIgnoreCase(PM_CLASS_OPSET_NAME)) {
			invokeAdminTool(sNameOrHost, sClass);

			// Change this asap! The account template is hard-coded!!!
		} else if (sClass.equalsIgnoreCase(PM_CLASS_RECORD_NAME)) {
			// sNameOrHost should contain the template name.
			System.out.println("*****************************" + sNameOrHost);

			if (sNameOrHost.equals("acctTpl")) {
				invokeAcctEditor(sSelectedLabel);
			} else {
				invokeMREditor(sSelectedLabel);
			}
		}
	}

	private String entityClassToType(String sClass) {
		if (sClass.equalsIgnoreCase(PM_CLASS_USER_NAME)) {
			return PM_NODE_USER;
		} else if (sClass.equalsIgnoreCase(PM_CLASS_UATTR_NAME)) {
			return PM_NODE_UATTR;
		} else if (sClass.equalsIgnoreCase(PM_CLASS_OBJ_NAME)) {
			return PM_OBJ;
		} else if (sClass.equalsIgnoreCase(PM_CLASS_OATTR_NAME)) {
			return PM_NODE_OATTR;
		} else if (sClass.equalsIgnoreCase(PM_CLASS_POL_NAME)) {
			return PM_NODE_POL;
		} else if (sClass.equalsIgnoreCase(PM_CLASS_OPSET_NAME)) {
			return PM_NODE_OPSET;
		} else if (sClass.equalsIgnoreCase(PM_CLASS_CONN_NAME)) {
			return PM_NODE_CONN;
		} else {
			return null;
		}
	}

	// Displays a table of object properties and returns the index
	// of the object selected by the user.
	private int selectObject(ArrayList list) {
		if (list == null || list.isEmpty()) {
			return -1;
		}
		int n = list.size();
		String[][] tableData = new String[n][5];
		for (int i = 0; i < n; i++) {
			String s = ((String) list.get(i)).substring(4);
			String[] pieces = s.split(PM_ALT_DELIM_PATTERN);
			String sClass = pieces[2];
			String sNameOrHost = pieces[4];
			String sIdOrPath = pieces[5];
			String sYesNo = pieces[3];
			tableData[i][0] = sClass;
			tableData[i][4] = sYesNo;
			if (sClass.equalsIgnoreCase(PM_CLASS_FILE_NAME)
					|| sClass.equalsIgnoreCase(PM_CLASS_DIR_NAME)) {
				tableData[i][1] = sNameOrHost;
				tableData[i][2] = sIdOrPath;
				tableData[i][3] = "";
			} else {
				tableData[i][1] = "";
				tableData[i][2] = "";
				tableData[i][3] = sNameOrHost;
			}
		}
		String[] columnNames = { "Class", "Host", "Path", "Name",
		"With ascendants" };
		JTable objTable = new JTable(tableData, columnNames);
		JScrollPane objScrollPane = new JScrollPane(objTable);
		objTable.setPreferredScrollableViewportSize(new Dimension(500, 150));
		String message = "Select the object you want to open:";
		int res = JOptionPane.showOptionDialog(this, new Object[] { message,
				objScrollPane }, "Objects", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (res != JOptionPane.OK_OPTION) {
			return -1;
		}
		return objTable.getSelectedRow();
	}

	private void invokeWordEditor(String sVobjName) {
		JOptionPane.showMessageDialog(this, "Not yet implemented!");
	}

	private void invokeAdminTool(String sEntityName, String sEntityClass) {
		doLaunchAdminTool(sEntityName, entityClassToType(sEntityClass));
	}

	private void invokeCompositeViewer(String sVobjName) {
		System.out
		.println("Method Session.invokeCompositeViewer() is not implemented");
		// /doLaunchCompositeViewer(sVobjName);
	}

	// Called when the user right-clicks on an .EML object to read a
	// saved message.
	private void invokeEmailer(String sVobjName) {
		doLaunchGrantor(sVobjName, false);// to open a msg, not to grant.
	}

	// Invoke the OpenOffice application.
	private void invokeOffice(String sVobjType, String sVobjName) {
		doLaunchOffice(sVobjType, sVobjName);
	}

	// Invoke the MS Office application.
	private void invokeMSOffice(String sVobjType, String sVobjName) {
		// doLaunchMSOffice(sVobjType, sVobjName);
	}

	private void invokeAcctEditor(String sRecName) {
		doLaunchAcctEditor(sRecName);
	}

	private void invokeMREditor(String sRecName) {
		// Before invoking the editor, ask the engine for permissions.
		String sReqPerms = "File read,File write";
		// ...

		doLaunchMREditor(sRecName);
	}

	// The RTFEditor application may be invoked from the current session
	// or from the spawned one.
	private void invokeRtfEditor(String sVobjName, boolean bTH) {
		if (bTH) {
			doLaunchTHEditor(sVobjName);
		} else {
			doLaunchRTFEditor(sVobjName);
		}
	}

	// The Old Work Flow
	private void invokeWorkFlowOld(String sVobjName) {
		doLaunchWorkFlowOld(sVobjName);
	}

	private void invokeDummy(String sVobjName) {
		doLaunchDummy(sVobjName);
	}


	// Get the Office Launcher path, the keystores for SSL, etc., and launch
	// Office component.
	private void doLaunchOffice(String sVobjType, String sVobjName) {
		manager.getApplicationManager().launchClientApp(
				ApplicationManager.OPEN_OFFICE_APP_NAME, sessionId,
				"-objtype", sVobjType, sVobjName);

	}

	// Get the editor path, the keystores for SSL, etc., and launch the editor.
	private void doLaunchTHEditor(String sVobjName) {
		manager.getApplicationManager().launchClientApp(
				ApplicationManager.TH_EDITOR_APP_NAME, sessionId, sVobjName);

	}

	// Get the editor path, the keystores for SSL, etc., and launch the editor.
	private void doLaunchRTFEditor(String sVobjName) {
		manager.getApplicationManager().launchClientApp(
				ApplicationManager.RTF_EDITOR_APP_NAME, sessionId, sVobjName);

	}

	//
	private void doLaunchDummy(String sVobjName) {
		manager.getApplicationManager().launchClientApp(
				ApplicationManager.DUMMY_APP, sessionId, sVobjName);
	}

	// Get the Work Flow to work Old PM_WKF_OLD
	private void doLaunchWorkFlowOld(String sVobjName) {
		manager.getApplicationManager().launchClientApp(
				ApplicationManager.WORKFLOW_OLD, sessionId, sVobjName);
	}

	// The exporter is launched automatically by the session manager, within a
	// session w/o GUI, by calling this method.
	// The exporter's path can be set by using the Configure...
	// menu of the session manager.
	// The keystores for the user "exporter" can be set in the exporter's
	// session.
	/**
	 * @uml.property name="exporterProcess"
	 */
	Process exporterProcess = null;

	public boolean doLaunchExporter() {
		ApplicationManager appMan = manager.getApplicationManager();
		appMan.addApplicationManagerListener(new ApplicationManagerListener() {

			@Override
			public void applicationStarted(String applicationName,
					String processId, NativeProcessWrapper procWrapper) {
				if (applicationName
						.equals(ApplicationManager.EXPORTER_APP_NAME)) {
					exporterProcess = procWrapper.getProcess();
					Runtime.getRuntime().addShutdownHook(
							new Thread(new Runnable() {

								@Override
								public void run() {
									if (exporterProcess != null) {
										exporterProcess.destroy();
									}
								}
							}));
				}
			}

			@Override
			public void applicationTerminated(String processId) {
				// Don't care
			}
		});
		appMan.launchClientApp(ApplicationManager.EXPORTER_APP_NAME,
				sessionId, "-exporter", String.valueOf(PM_DEFAULT_EXPORTER_PORT));
		return true;
	}

	public void destroyExporter() {
		if (exporterProcess != null) {
			exporterProcess.destroy();
		}
	}

	private void doLaunchAcctEditor(String sRecName) {
		manager.getApplicationManager().launchClientApp(
				ApplicationManager.ACCOUNT_EDITOR_APP_NAME, sessionId,
				sRecName);
	}

	private void doLaunchMREditor(String sRecName) {
		manager.getApplicationManager().launchClientApp(
				ApplicationManager.MEDICAL_RECORD_EDITOR_APP_NAME, sessionId,
				sRecName);
	}

	private void doLaunchSchemaBuilder() {
		manager.getApplicationManager().launchClientApp(
				ApplicationManager.SCHEMA_BUILDER_APP_NAME, sessionId);
	}

	private void doLaunchTableEditor() {
		manager.getApplicationManager().launchClientApp(
				ApplicationManager.TABLE_EDITOR_APP_NAME, sessionId);
	}


	// Launch the Grantor tool to open or grant/send a given object.
	// If the object is null, no object was yet selected for grant/send.
	// If bGrant is true, read or read/write access to the object will
	// be granted. Otherwise, the object must be a message (.eml) and
	// will be open.
	// When bGrant is true, the first argument may also be a record.
	// Add the checks later.
	private void doLaunchGrantor(String sVobjName, boolean bGrant) {
		ApplicationManager applicationManager = manager.getApplicationManager();
		List<String> args = newArrayList();
		if (sVobjName != null) {
			args.add(sVobjName);
		}
		if (bGrant) {
			args.add("-grant");
		}
		applicationManager.launchClientApp(ApplicationManager.EGRANT_APP_NAME,
				sessionId, args.toArray(new String[0]));
	}

	private void doLaunchAdminTool(String sEntityName, String sEntityType) {

		String engineHost = manager.getEngineHost();
		int enginePort = manager.getEnginePort();

		List<String> argList = newArrayList("-engine", engineHost,
				"-engineport", Integer.toString(enginePort));

		if (sEntityName != null) {
			argList.addAll(newArrayList("-entity", sEntityName));
		}
		if (sEntityType != null) {
			argList.addAll(newArrayList("-type", sEntityType));
		}

		System.out.println("-=-=-=-=-");
		System.out.println(argList);
		System.out.println("-=-=-=-=-");

		manager.getApplicationManager().launchPeerApp(sSessionName, sessionId,
				argList.toArray(new String[argList.size()]));

	}

	private boolean deleteSession(String sId) {
		try {
			Packet cmd = CommandUtil.makeCmd("deleteSession", null, sId);
			Packet res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in deleteSession: "
						+ res.getErrorMessage());
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Exception in deleteSession: "
					+ e.getMessage());
			return false;
		}
		return true;
	}

	// Ask the kernel to create a process in order to run an
	// application in it. This function returns the process id,
	// which will be passed to the application on the command
	// line like the session id is now.
	public String createProcess() {
		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("createProcess", sessionId);
			res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this,
						"createProcess(in create process) returned null!");
				return null;
			}
			if (res.size() < 1) {
				JOptionPane.showMessageDialog(this,
						"no process id returned in createProcess()!");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in createProcess(): " + res.getErrorMessage());
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in createProcess(): " + e.getMessage());
			return null;
		}
		return res.getStringValue(0);
	}

	private static String setToString(HashSet set) {
		if (set == null || set.isEmpty()) {
			return "";
		}

		Iterator hsiter = set.iterator();
		boolean firstTime = true;
		StringBuilder sb = new StringBuilder();

		while (hsiter.hasNext()) {
			String sId = (String) hsiter.next();
			if (firstTime) {
				sb.append(sId);
				firstTime = false;
			} else {
				sb.append(",").append(sId);
			}
		}
		return sb.toString();
	}

	private HashSet stringToSet(String sArg) {
		HashSet set = new HashSet();
		if (sArg != null) {
			String[] pieces = sArg.split(",");
			for (int i = 0; i < pieces.length; i++) {
				String t = pieces[i].trim();
				if (t.length() > 0) {
					set.add(t);
				}
			}
		}
		return set;
	}

	private void doAdminView() {
		sVosPresType = PM_VOS_PRES_ADMIN;
		pmPopup = pmAdminPopup;
		doRefresh();
	}

	private void doUserView() {
		sVosPresType = PM_VOS_PRES_USER;
		pmPopup = pmUserPopup;
		doRefresh();
	}

	private void doRefresh() {
		crtDefaultCursor = SESS_WAIT_CURSOR;
		crtHandCursor = SESS_WAIT_CURSOR;
		refreshInternal();
	}

	private void refreshInternal() {

		final SwingWorker worker = new SwingWorker() {

			@Override
			public Object construct() {
				/*
				 Packet res = null;
				 
				try {
				
					Packet cmd = CommandUtil.makeCmd("computeFastVos", sessionId,
							sVosPresType, sSessionUserId, sessionId);
					res = sslClient.sendReceive(cmd, null);
					if (res.hasError()) {
						JOptionPane
						.showMessageDialog(
								Session.this,
								"Error in computeVos: "
										+ res.getErrorMessage());
						return null;
					}
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(Session.this,
							"Exception in computeVos: " + e.getMessage());
					return null;
				}
				*/
				if (sVosPresType == PM_VOS_PRES_USER) {
					log.debug("TRACE 30 - Refreshing Root Node");
					root = new PmNode(PM_NODE_CONN, PM_CONNECTOR_ID,
							PM_CONNECTOR_NAME, new PmNodeChildDelegate(
									sslClient, nullToEmpty(sessionId),
									// Steve - Modified (4/16/16): was 'USER'
									PmGraphDirection.USER_MELL, 
									// Steve - Modified (4/16/16): was 'USER'
									PmGraphType.USER_MELL));
				} else {
					root = new PmNode(PM_NODE_CONN, PM_CONNECTOR_ID,
							PM_CONNECTOR_NAME, new PmNodeChildDelegate(
									sslClient, nullToEmpty(sessionId),
									PmGraphDirection.USER, PmGraphType.ADMIN));
				}
				//sLastUpdateTimestamp = res.getStringValue(0);

				resetTree(root);
				return SessionManager.success();
			}

			@Override
			public void finished() {
				if (sVosPresType.equalsIgnoreCase(PM_VOS_PRES_USER))
					viewLabel.setText("Now viewing: user POS");
				else
					viewLabel.setText("Now viewing: admin POS");
				crtDefaultCursor = SESS_DEFAULT_CURSOR;
				crtHandCursor = SESS_HAND_CURSOR;
			}
		};
		worker.start();
	}

	private void doTestRequestPerms() {
		String sReqPerms = JOptionPane.showInputDialog(null,
				"Please enter requested permissions, separated by commas:");
		if (sReqPerms == null) {
			return;
		}
		System.out.println("Request perms {" + sReqPerms + "} for:");
		System.out.println("              id =  " + rightClickedNode.getId());
		System.out.println("           label =  " + rightClickedNode.getName());
		System.out.println("            type =  " + rightClickedNode.getType());

		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("requestPerms", sessionId,
					rightClickedNode.getName(), sReqPerms);
			res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in requestPerms: "
						+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Exception in requestPerms: "
					+ e.getMessage());
			return;
		}

		DefaultListModel permModel = new DefaultListModel();
		JList permList = new JList(permModel);
		JScrollPane permScrollPane = new JScrollPane(permList);
		permScrollPane.setPreferredSize(new Dimension(300, 150));

		for (int i = 0; i < res.size(); i++) {
			permModel.addElement(res.getStringValue(i));
		}

		String message = "List of granted permissions:";
		int ret = JOptionPane.showOptionDialog(this, new Object[] { message,
				permScrollPane }, "Permissions", JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, null, null);
	}

	private void doChangePassword() {
		JPasswordField oldPassField = new JPasswordField();
		JPasswordField newPassField = new JPasswordField();
		JPasswordField conPassField = new JPasswordField();
		String msgOld = "Old password:";
		String msgNew = "New password:";
		String msgCon = "Confirm new password:";
		for (int j = 0; j < 3; j++) {
			int ret = JOptionPane.showOptionDialog(this, new Object[] { msgOld,
					oldPassField, msgNew, newPassField, msgCon, conPassField },
					"Change password", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);
			if (ret != JOptionPane.OK_OPTION) {
				return;
			}

			// Get old, new, and confirmation password.
			char[] cOldPass = oldPassField.getPassword();
			char[] cNewPass = newPassField.getPassword();
			char[] cConPass = conPassField.getPassword();

			try {
				Packet cmd = CommandUtil.makeCmd("changePassword", sessionId,
						sSessionUser, new String(cOldPass),
						new String(cNewPass), new String(cConPass));
				for (int i = 0; i < cOldPass.length; i++) {
					cOldPass[i] = 0;
				}
				for (int i = 0; i < cNewPass.length; i++) {
					cNewPass[i] = 0;
				}
				for (int i = 0; i < cConPass.length; i++) {
					cConPass[i] = 0;
				}

				Packet res = sslClient.sendReceive(cmd, null);
				if (res.hasError()) {
					JOptionPane
					.showMessageDialog(
							this,
							"Error in changePassword: "
									+ res.getErrorMessage());
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						"Exception in changePassword: " + e.getMessage());
				return;
			}

			// Success.
			JOptionPane.showMessageDialog(this,
					"Password successfully changed.");
			return;
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

	private byte[] getBytes(String filename) {
		try {
			File f = new File(filename);
			int bytesLeft = (int) f.length();
			byte[] buffer = new byte[bytesLeft];
			FileInputStream fis = new FileInputStream(f);
			int n;
			int index = 0;
			while ((bytesLeft > 0)
					&& (n = fis.read(buffer, index, bytesLeft)) != -1) {
				index += n;
				bytesLeft -= n;
			}
			fis.close();
			return buffer;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

package gov.nist.csd.pm.application.appeditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SpringLayout;

import java.awt.BorderLayout;

import javax.swing.JSplitPane;
import javax.swing.BoxLayout;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import static gov.nist.csd.pm.common.info.PMCommand.ADD_TEMPLATE;
import static gov.nist.csd.pm.common.info.PMCommand.GET_HOST_APP_PATHS;
import static gov.nist.csd.pm.common.info.PMCommand.GET_ID_OF_ENTITY_WITH_NAME_AND_TYPE;
import static gov.nist.csd.pm.common.info.PMCommand.GET_K_STORE_PATHS;
import static gov.nist.csd.pm.common.info.PMCommand.GET_TEMPLATES;
import static gov.nist.csd.pm.common.info.PMCommand.GET_TEMPLATE_INFO;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import gov.nist.csd.pm.admin.PmAdmin;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.browser.PmGraphDirection;
import gov.nist.csd.pm.common.browser.PmGraphModel;
import gov.nist.csd.pm.common.browser.PmGraphType;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeChildDelegate;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.model.ObjectAttribute;
import gov.nist.csd.pm.common.model.ObjectAttributes;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;
import gov.nist.csd.pm.common.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;

import static gov.nist.csd.pm.common.util.Generators.generateRandomName;



import javax.swing.DefaultComboBoxModel;
import javax.swing.ListModel;

import com.google.common.base.Throwables;

import java.awt.Font;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JSeparator;

import java.awt.GridLayout;

/**
 * @author administrator
 * TODO. List
 * 1. allow user to edit oattrs in the schema
 * 2. allow users to resubmit the schema after esditing it
 *
 */
public class SchemaBuilder3 extends JFrame {

	Log log = new Log(Log.Level.INFO, true);

	public static final String PM_PROP_DELIM = "=";
	public static final String PM_FIELD_DELIM = ":";
	public static final String PM_ALT_FIELD_DELIM = "|";
	public static final String PM_ALT_DELIM_PATTERN = "\\|";
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

	private boolean oattrEdit = false;
	private String selectedOattr;
	public static boolean modify = false;
	private static FileWriter fw;
	private ColumnPermEditor2 colPermEditor;
	private boolean edit;
	private String tempName;
	private String tempId;
	private String sessionId;
	private int nSimulatorPort;
	private final int PM_DEFAULT_SIMULATOR_PORT = 8081;
	private SSLSocketClient engineClient;
	private String userName;
	private SysCaller sysCaller;
	private String sProcessId;
	private JTable table;
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultTableModel model; 
	private DefaultTableModel finalModel = new DefaultTableModel();
	private String sTableName;
	private Vector<String> columnVector;
	private ArrayList<ArrayList<String>> permissions, existingPermissions;
	private final JSplitPane splitPane = new JSplitPane();
	private String schemaName;
	private PmGraph tree;
	private JScrollPane scrollPane;
	private JTextField baseNameField;
	private JTextField oattrNameField;
	private JTextField oattrPropField;
	private JTextField oattrDescriptionField;
	private PmGraphModel treeModel;
	protected PmNode schemaNode, baseNode;
	private DefaultListModel propModel;
	private JButton btnSetPerms;
	private JButton btnCreateOattr;
	private JPanel panel_5;
	private JList list;
	private JLabel lblSchemaName;
	private JTextField schemaNameField;
	private JLabel lblDescription_1;
	private JTextField descrField;
	private JTextField otherField;
	private JLabel lblProperties_1;
	private JLabel lblKeys;
	private JTextField schemaPropField;
	private JTextField schemaKeyField;
	private JButton btnSubmit;
	private JLabel lblKeys_1;
	private JTextField oattrOtherField;
	private JButton btnAddOattr;
	private JButton btnDeleteOattr;
	private JButton btnDone;
	private JButton btnReset;
	private ArrayList<ArrayList<String>> crtSessPerms;
	private SSLSocketClient sslClient;
	private PmNodeChildDelegate childDelegate;
	protected boolean importing;
	private JButton btnCreate;

	/**
	 * @param sessionId
	 * @param nSimPort
	 * @param sProcId
	 * @param bDebug
	 */
	public SchemaBuilder3(String sessionId, int nSimPort, String sProcId, boolean bDebug) {
		setTitle("Schema Builder");
		

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 489);

		/*try {
			fw = new FileWriter("C:\\Users\\Administrator.WIN-DNAR5079LMF\\Desktop\\ouput.txt");
		} catch (IOException e2) {
			e2.printStackTrace();
		}*/

		nSimulatorPort = (nSimPort < 1024) ? PM_DEFAULT_SIMULATOR_PORT
				: nSimPort;
		sProcessId = sProcId;
		sysCaller = new SysCallerImpl(nSimulatorPort, sessionId, sProcId, bDebug, "SB");
		permissions = new ArrayList<ArrayList<String>>();
		existingPermissions = new ArrayList<ArrayList<String>>();
		crtSessPerms = new ArrayList<ArrayList<String>>();
		columnVector = new Vector<String>();
		try {
			this.sslClient = new SSLSocketClient("localhost", nSimulatorPort, bDebug, "SB");
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null,
					"Unable to create SSL socket to talk to the KSim of the local host!");
			e1.printStackTrace();
		}

		try {
			engineClient = new SSLSocketClient("localhost", 8080,
					bDebug, "c,SB-E");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		if (!connectToEngine()) {
			JOptionPane.showMessageDialog(null,
					"Unable to connect to the PM engine.");
			System.exit(-2);
		}

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent we) {
				terminate(0);
			}
		});

		this.colPermEditor = new ColumnPermEditor2(engineClient, this);
		this.colPermEditor.pack();
		this.sessionId = sessionId;

		userName = getCrtUser();
		log("USERNAME: " + userName);
		if(!checkBuilderPerms()){
			JOptionPane.showMessageDialog(this, "You are not allowed to open this application.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		permissions = new ArrayList<ArrayList<String>>();

		childDelegate = new PmNodeChildDelegate(sslClient, sessionId == null ? "" : sessionId, PmGraphDirection.UP, PmGraphType.USER_ATTRIBUTES);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmNewMenuItem = new JMenuItem("New");
		mntmNewMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				resetAll();
			}
		});
		mnFile.add(mntmNewMenuItem);

		separator = new JSeparator();
		mnFile.add(separator);

		mntmNewMenuItem_1 = new JMenuItem("Open");
		mntmNewMenuItem_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				openSchema();
			}
		});
		mnFile.add(mntmNewMenuItem_1);

		separator_1 = new JSeparator();
		mnFile.add(separator_1);

		mntmNewMenuItem_2 = new JMenuItem("Delete");
		mntmNewMenuItem_2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(schemaName == null){
					JOptionPane.showMessageDialog(SchemaBuilder3.this, "Please open a schema to be deleted");
					return;
				}
				String id = getIdOfEntity("b", schemaName);
				deleteTemplate(id, schemaName, true);
			}
		});
		mnFile.add(mntmNewMenuItem_2);

		JMenuItem mntmImport = new JMenuItem("Import");
		mntmImport.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				DDLParser parser = new DDLParser(SchemaBuilder3.this);
				parser.setVisible(true);

			}

		});
		mnFile.add(mntmImport);

		separator_2 = new JSeparator();
		mnFile.add(separator_2);

		mntmNewMenuItem_3 = new JMenuItem("Exit");
		mntmNewMenuItem_3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				terminate(0);
			}
		});
		mnFile.add(mntmNewMenuItem_3);

		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmTutorial = new JMenuItem("Tutorial");
		mntmTutorial.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFrame jf = new JFrame("Schema Builder Tutorial");
				String tutorial = "Start by filling in the fields in the Schema Information panel.\n" +
						"Once you have completed this, click on the \"Create New Schema\" button to create the schema.\n" +
						"You will then see a Tree on the left side of the next panel down. This will be where you can see the\n" +
						"overall structure of the Schema you are creating and how it will look in your session graph.\n" +
						"Now you can create object attributes in you schema by clicking the \"Add Oattr\" at the bottom of the panel, which will\n" +
						"enable the fields on the right side of the panel to edit you new object attribute information.\n" +
						"Once you are done filling out the fields, you can click \"Set Permissions\" which will allow you to set the permissions\n" +
						"on the object attribute you are about to create." +
						"When you are all done filling out the information and setting the permissions than you can click the \"Create Object Attribute\"\n" +
						"button to create the new Object Attribute, and then you will see it added to your Schema's hierarchy on the left.\n" +
						"Once you are done adding all the Object Attributes you want, you can click the \"Submit Schema\" button to finally create a new template and finish\n" +
						"creating your new Schema.";
				jf.getContentPane().add(new JLabel(tutorial));
			}
		});
		mnHelp.add(mntmTutorial);


		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{619, 0, 0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{60, 45, 300, 35, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		JPanel panel = new JPanel();
		panel.setPreferredSize(panel.getPreferredSize());
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
				BorderFactory.createTitledBorder("Schema Information:")));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridheight = 2;
		gbc_panel.gridwidth = 5;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		contentPane.add(panel, gbc_panel);
		panel.setLayout(new BorderLayout(0, 0));

		panel_5 = new JPanel();
		panel_5.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.add(panel_5, BorderLayout.CENTER);
		SpringLayout sl_panel_5 = new SpringLayout();
		panel_5.setLayout(sl_panel_5);

		list = new JList();
		sl_panel_5.putConstraint(SpringLayout.NORTH, list, 0, SpringLayout.NORTH, panel_5);
		sl_panel_5.putConstraint(SpringLayout.WEST, list, 0, SpringLayout.WEST, panel_5);
		panel_5.add(list);

		lblSchemaName = new JLabel("Schema Name:");
		sl_panel_5.putConstraint(SpringLayout.NORTH, lblSchemaName, 0, SpringLayout.NORTH, list);
		sl_panel_5.putConstraint(SpringLayout.WEST, lblSchemaName, 0, SpringLayout.WEST, list);
		panel_5.add(lblSchemaName);

		schemaNameField = new JTextField();
		sl_panel_5.putConstraint(SpringLayout.NORTH, schemaNameField, 0, SpringLayout.NORTH, list);
		sl_panel_5.putConstraint(SpringLayout.WEST, schemaNameField, 6, SpringLayout.EAST, lblSchemaName);
		panel_5.add(schemaNameField);
		schemaNameField.setColumns(16);

		lblDescription_1 = new JLabel("Description:");
		sl_panel_5.putConstraint(SpringLayout.NORTH, lblDescription_1, 0, SpringLayout.NORTH, panel_5);
		sl_panel_5.putConstraint(SpringLayout.WEST, lblDescription_1, 6, SpringLayout.EAST, schemaNameField);
		panel_5.add(lblDescription_1);

		descrField = new JTextField();
		sl_panel_5.putConstraint(SpringLayout.NORTH, descrField, 0, SpringLayout.NORTH, list);
		sl_panel_5.putConstraint(SpringLayout.WEST, descrField, 6, SpringLayout.EAST, lblDescription_1);
		panel_5.add(descrField);
		descrField.setColumns(16);

		JLabel lblNewLabel = new JLabel("Other Info:");
		sl_panel_5.putConstraint(SpringLayout.NORTH, lblNewLabel, 1, SpringLayout.NORTH, list);
		sl_panel_5.putConstraint(SpringLayout.WEST, lblNewLabel, 6, SpringLayout.EAST, descrField);
		panel_5.add(lblNewLabel);

		otherField = new JTextField();
		sl_panel_5.putConstraint(SpringLayout.NORTH, otherField, 0, SpringLayout.NORTH, list);
		sl_panel_5.putConstraint(SpringLayout.WEST, otherField, 6, SpringLayout.EAST, lblNewLabel);
		sl_panel_5.putConstraint(SpringLayout.EAST, otherField, 0, SpringLayout.EAST, panel_5);
		panel_5.add(otherField);
		otherField.setColumns(15);

		btnCreate = new JButton("Create New Schema");
		sl_panel_5.putConstraint(SpringLayout.WEST, btnCreate, 65, SpringLayout.WEST, lblNewLabel);
		sl_panel_5.putConstraint(SpringLayout.EAST, btnCreate, 0, SpringLayout.EAST, otherField);
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(importing){
					importSchema(schemaName);
				}else{
					createSchema();
				}

			}
		});
		panel_5.add(btnCreate);

		lblProperties_1 = new JLabel("Properties:");
		sl_panel_5.putConstraint(SpringLayout.NORTH, btnCreate, -5, SpringLayout.NORTH, lblProperties_1);
		sl_panel_5.putConstraint(SpringLayout.WEST, lblProperties_1, 0, SpringLayout.WEST, list);
		sl_panel_5.putConstraint(SpringLayout.SOUTH, lblProperties_1, -5, SpringLayout.SOUTH, panel_5);
		panel_5.add(lblProperties_1);

		lblKeys = new JLabel("Keys:");
		sl_panel_5.putConstraint(SpringLayout.WEST, lblKeys, 0, SpringLayout.WEST, lblDescription_1);
		sl_panel_5.putConstraint(SpringLayout.SOUTH, lblKeys, -5, SpringLayout.SOUTH, panel_5);
		panel_5.add(lblKeys);

		schemaPropField = new JTextField();
		sl_panel_5.putConstraint(SpringLayout.WEST, schemaPropField, 0, SpringLayout.WEST, schemaNameField);
		sl_panel_5.putConstraint(SpringLayout.SOUTH, schemaPropField, -3, SpringLayout.SOUTH, panel_5);
		sl_panel_5.putConstraint(SpringLayout.EAST, schemaPropField, 0, SpringLayout.EAST, schemaNameField);
		panel_5.add(schemaPropField);
		schemaPropField.setColumns(10);

		schemaKeyField = new JTextField();
		sl_panel_5.putConstraint(SpringLayout.WEST, schemaKeyField, 0, SpringLayout.WEST, descrField);
		sl_panel_5.putConstraint(SpringLayout.SOUTH, schemaKeyField, -3, SpringLayout.SOUTH, panel_5);
		sl_panel_5.putConstraint(SpringLayout.EAST, schemaKeyField, 0, SpringLayout.EAST, descrField);
		panel_5.add(schemaKeyField);
		schemaKeyField.setColumns(16);

		JLabel lblNewLabel_1 = new JLabel("Type desired keys separated by \", \"");
		sl_panel_5.putConstraint(SpringLayout.NORTH, lblNewLabel_1, 6, SpringLayout.SOUTH, descrField);
		sl_panel_5.putConstraint(SpringLayout.WEST, lblNewLabel_1, 0, SpringLayout.WEST, descrField);
		panel_5.add(lblNewLabel_1);
		lblNewLabel_1.setFont(new Font("Dialog", Font.PLAIN, 10));

		JLabel lblNewLabel_2 = new JLabel("Type desired properties separated by \", \"");
		sl_panel_5.putConstraint(SpringLayout.NORTH, lblNewLabel_2, 6, SpringLayout.SOUTH, schemaNameField);
		sl_panel_5.putConstraint(SpringLayout.WEST, lblNewLabel_2, 0, SpringLayout.WEST, schemaNameField);
		lblNewLabel_2.setFont(new Font("Dialog", Font.PLAIN, 10));
		panel_5.add(lblNewLabel_2);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0),
				BorderFactory.createTitledBorder("Create Schema:")));

		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.gridwidth = 5;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		contentPane.add(panel_1, gbc_panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		splitPane.setEnabled(false);


		splitPane.setDividerSize(10);
		splitPane.setResizeWeight(0.5);
		//Dimension d = splitPane.getRightComponent().getPreferredSize();
		//splitPane.getRightComponent().setPreferredSize(d);
		panel_1.add(splitPane, BorderLayout.CENTER);

		JPanel panel_2 = new JPanel();
		panel_2.setPreferredSize(panel_2.getPreferredSize());
		panel_2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
				BorderFactory.createTitledBorder("Edit Oattr:")));

		splitPane.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));


		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_2.add(panel_3, BorderLayout.CENTER);
		SpringLayout sl_panel_3 = new SpringLayout();
		panel_3.setLayout(sl_panel_3);

		JLabel lblBaseName = new JLabel("Base Name:");
		sl_panel_3.putConstraint(SpringLayout.NORTH, lblBaseName, 7, SpringLayout.NORTH, panel_3);
		sl_panel_3.putConstraint(SpringLayout.WEST, lblBaseName, 0, SpringLayout.WEST, panel_3);
		panel_3.add(lblBaseName);

		baseNameField = new JTextField();
		sl_panel_3.putConstraint(SpringLayout.WEST, baseNameField, 6, SpringLayout.EAST, lblBaseName);
		sl_panel_3.putConstraint(SpringLayout.EAST, baseNameField, 0, SpringLayout.EAST, panel_3);
		baseNameField.setEnabled(false);
		sl_panel_3.putConstraint(SpringLayout.NORTH, baseNameField, 5, SpringLayout.NORTH, panel_3);
		panel_3.add(baseNameField);
		baseNameField.setColumns(10);

		JLabel lblOattrName = new JLabel("Oattr Name:");
		sl_panel_3.putConstraint(SpringLayout.NORTH, lblOattrName, 9, SpringLayout.SOUTH, lblBaseName);
		sl_panel_3.putConstraint(SpringLayout.WEST, lblOattrName, 0, SpringLayout.WEST, lblBaseName);
		panel_3.add(lblOattrName);

		oattrNameField = new JTextField();
		oattrNameField.setEnabled(false);
		sl_panel_3.putConstraint(SpringLayout.NORTH, oattrNameField, -2, SpringLayout.NORTH, lblOattrName);
		sl_panel_3.putConstraint(SpringLayout.WEST, oattrNameField, 6, SpringLayout.EAST, lblOattrName);
		sl_panel_3.putConstraint(SpringLayout.EAST, oattrNameField, 0, SpringLayout.EAST, baseNameField);
		panel_3.add(oattrNameField);
		oattrNameField.setColumns(10);

		JLabel lblProperties = new JLabel("Properties:");
		sl_panel_3.putConstraint(SpringLayout.WEST, lblProperties, 0, SpringLayout.WEST, lblBaseName);
		panel_3.add(lblProperties);

		oattrPropField = new JTextField();
		sl_panel_3.putConstraint(SpringLayout.NORTH, lblProperties, 2, SpringLayout.NORTH, oattrPropField);
		sl_panel_3.putConstraint(SpringLayout.NORTH, oattrPropField, 6, SpringLayout.SOUTH, oattrNameField);
		sl_panel_3.putConstraint(SpringLayout.WEST, oattrPropField, 0, SpringLayout.WEST, baseNameField);
		sl_panel_3.putConstraint(SpringLayout.EAST, oattrPropField, 0, SpringLayout.EAST, baseNameField);
		oattrPropField.setEnabled(false);

		panel_3.add(oattrPropField);
		oattrPropField.setColumns(10);

		propModel = new DefaultListModel();

		JLabel lblDescription = new JLabel("Description:");
		sl_panel_3.putConstraint(SpringLayout.EAST, lblDescription, 0, SpringLayout.EAST, lblBaseName);
		panel_3.add(lblDescription);

		oattrDescriptionField = new JTextField();
		sl_panel_3.putConstraint(SpringLayout.NORTH, oattrDescriptionField, -2, SpringLayout.NORTH, lblDescription);
		sl_panel_3.putConstraint(SpringLayout.WEST, oattrDescriptionField, 0, SpringLayout.WEST, baseNameField);
		sl_panel_3.putConstraint(SpringLayout.EAST, oattrDescriptionField, 0, SpringLayout.EAST, baseNameField);
		oattrDescriptionField.setEnabled(false);
		panel_3.add(oattrDescriptionField);
		oattrDescriptionField.setColumns(10);

		btnSetPerms = new JButton("Set Permissions");
		sl_panel_3.putConstraint(SpringLayout.WEST, btnSetPerms, 0, SpringLayout.WEST, lblBaseName);
		sl_panel_3.putConstraint(SpringLayout.EAST, btnSetPerms, 0, SpringLayout.EAST, baseNameField);
		btnSetPerms.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openColPermEditor(oattrNameField.getText());
			}
		});
		btnSetPerms.setEnabled(false);
		panel_3.add(btnSetPerms);

		btnCreateOattr = new JButton("Create Object Attribute");
		sl_panel_3.putConstraint(SpringLayout.SOUTH, btnSetPerms, -6, SpringLayout.NORTH, btnCreateOattr);
		sl_panel_3.putConstraint(SpringLayout.WEST, btnCreateOattr, 0, SpringLayout.WEST, lblBaseName);
		sl_panel_3.putConstraint(SpringLayout.SOUTH, btnCreateOattr, 0, SpringLayout.SOUTH, panel_3);
		sl_panel_3.putConstraint(SpringLayout.EAST, btnCreateOattr, 0, SpringLayout.EAST, baseNameField);
		btnCreateOattr.setEnabled(false);
		btnCreateOattr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createOattr();
			}	
		});
		panel_3.add(btnCreateOattr);

		lblKeys_1 = new JLabel("Other Info:");
		sl_panel_3.putConstraint(SpringLayout.NORTH, lblDescription, 10, SpringLayout.SOUTH, lblKeys_1);
		sl_panel_3.putConstraint(SpringLayout.WEST, lblKeys_1, 0, SpringLayout.WEST, lblBaseName);
		panel_3.add(lblKeys_1);

		oattrOtherField = new JTextField();
		sl_panel_3.putConstraint(SpringLayout.NORTH, lblKeys_1, 2, SpringLayout.NORTH, oattrOtherField);
		sl_panel_3.putConstraint(SpringLayout.NORTH, oattrOtherField, 6, SpringLayout.SOUTH, oattrPropField);
		sl_panel_3.putConstraint(SpringLayout.WEST, oattrOtherField, 0, SpringLayout.WEST, baseNameField);
		sl_panel_3.putConstraint(SpringLayout.EAST, oattrOtherField, 0, SpringLayout.EAST, panel_3);
		oattrOtherField.setEnabled(false);
		panel_3.add(oattrOtherField);
		oattrOtherField.setColumns(10);

		panel_4 = new JPanel();
		splitPane.setLeftComponent(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));

		panel_6 = new JPanel();
		panel_6.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panel_4.add(panel_6, BorderLayout.SOUTH);
		panel_6.setLayout(new GridLayout(0, 2, 0, 0));

		btnAddOattr = new JButton("Add Oattr");
		panel_6.add(btnAddOattr);
		btnAddOattr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//enableAll();
				resetOattrEditor();
			}
		});
		btnAddOattr.setEnabled(false);

		btnDeleteOattr = new JButton("Delete Oattr");
		panel_6.add(btnDeleteOattr);
		btnDeleteOattr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(selectedOattr == null){
					JOptionPane.showMessageDialog(SchemaBuilder3.this, "Please select a Oattr to delete from the graph.");
					return;
				}
				deleteTable(selectedOattr);
			}
		});
		btnDeleteOattr.setEnabled(false);

		scrollPane = new JScrollPane();
		panel_4.add(scrollPane, BorderLayout.CENTER);

		btnDone = new JButton("Done");
		btnDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				terminate(0);
			}
		});
		GridBagConstraints gbc_btnDone = new GridBagConstraints();
		gbc_btnDone.anchor = GridBagConstraints.WEST;
		gbc_btnDone.insets = new Insets(0, 0, 0, 5);
		gbc_btnDone.gridx = 0;
		gbc_btnDone.gridy = 3;
		contentPane.add(btnDone, gbc_btnDone);

		btnSubmit = new JButton("Submit Schema");
		btnSubmit.setEnabled(false);
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				submitSchema();
			}
		});

		btnSetSchemaPermissions = new JButton("Set Schema Permissions");
		btnSetSchemaPermissions.setEnabled(false);
		btnSetSchemaPermissions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openColPermEditor(schemaName);
			}
		});

		btnReset = new JButton("Reset");
		sl_panel_3.putConstraint(SpringLayout.WEST, btnReset, 0, SpringLayout.WEST, lblBaseName);
		sl_panel_3.putConstraint(SpringLayout.SOUTH, btnReset, -6, SpringLayout.NORTH, btnSetPerms);
		sl_panel_3.putConstraint(SpringLayout.EAST, btnReset, 0, SpringLayout.EAST, baseNameField);
		panel_3.add(btnReset);
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetOattrEditor();
			}
		});
		btnReset.setEnabled(false);
		GridBagConstraints gbc_btnSetSchemaPermissions = new GridBagConstraints();
		gbc_btnSetSchemaPermissions.insets = new Insets(0, 0, 0, 5);
		gbc_btnSetSchemaPermissions.gridx = 3;
		gbc_btnSetSchemaPermissions.gridy = 3;
		contentPane.add(btnSetSchemaPermissions, gbc_btnSetSchemaPermissions);
		GridBagConstraints gbc_btnSubmit = new GridBagConstraints();
		gbc_btnSubmit.insets = new Insets(0, 0, 0, 2);
		gbc_btnSubmit.gridx = 4;
		gbc_btnSubmit.gridy = 3;
		contentPane.add(btnSubmit, gbc_btnSubmit);

		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);

	}

	public ArrayList<String> getColumns(){
		return null;
	}

	/**
	 * @return
	 */
	private boolean connectToEngine(){
		try {
			Packet cmd = makeCmd("connect", null);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res.hasError()) {
				log("Connect: " + res.getErrorMessage());
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log("Connect: " + e.getMessage());
			return false;
		}
	}

	private void enableAll(){
		oattrNameField.setEnabled(true);
		baseNameField.setEnabled(true);
		oattrPropField.setEnabled(true);
		oattrOtherField.setEnabled(true);
		oattrDescriptionField.setEnabled(true);
		btnSetPerms.setEnabled(true);
		btnCreateOattr.setEnabled(true);
		btnReset.setEnabled(true);
		//btnSetSchemaPermissions.setEnabled(true);
		oattrEdit = false;
		selectedOattr = "";
	}

	/**
	 * Creates oattr node and adds the oattr info to an ArrayList, and will be created when user clicks "Submit Schema"
	 */
	public void createOattr(){
		ArrayList<String> existingSchemas = new ArrayList<String>();
		String sId = getIdOfEntity("p", "Schema Builder");
    	log.debugStackCall("!!!!!!! getMembersOf - trace 20");	

		List<String[]> conts = getMembersOf("p", sId, "Schema Builder", "ac");
		for(int i = 0; i < conts.size(); i++){
			existingSchemas.add(conts.get(i)[2]);
		}

		String base = baseNameField.getText();
		if(base == null || base.length() == 0){
			JOptionPane.showMessageDialog(this, "Please fill out Base Name Field");
			return;
		}

		String name = oattrNameField.getText();
		if(name == null || name.length() == 0){
			JOptionPane.showMessageDialog(this, "Please fill out Oattr Name Field");
			return;
		}

		String descr = oattrDescriptionField.getText();
		if(descr == null || descr.length() == 0){
			descr = name;
			oattrDescriptionField.setText(name);
		}

		String other = oattrOtherField.getText();
		if(other == null || other.length() == 0){
			other = name;
			oattrOtherField.setText(name);
		}
		if(oattrEdit){
			//TODO get all children and link with new oattr
			int x = JOptionPane.showConfirmDialog(this, "Are you sure you want to overwite the object attribute " + selectedOattr +
					"?\nIf you wish to proceed, press OK." +
					"\nIf you would like to cancel this operation, press CANCEL.", "Overwrite?" +
							"\nYou will also have to resubmit the schema if you proceed.", JOptionPane.OK_CANCEL_OPTION);
			if(x == JOptionPane.CANCEL_OPTION){
				return;
			}
			
			log("selectedOattr: " + selectedOattr);
			deleteTable(selectedOattr);
			log("BEFORE:" + containers);
			for(int i = 0; i < containers.size(); i++){
				if(containers.get(i).get(1).equals(selectedOattr)){
					containers.remove(containers.get(i));
				}
			}
			log("After:" + containers);
			resetTree(schemaNode);
		}
		log("NAME: " + name);
		ArrayList<PmNode> nodes= new ArrayList<PmNode>();

		if(base.equals(schemaName)){
			baseNode = schemaNode;
		}else{
			List<PmNode> children = getAllNodes(schemaNode, nodes);
			log("Base: " + base);
			for(PmNode node : children){
				log(node.getName());
			}
			for(PmNode node : children){
				String nodeName = node.getName();
				log("Node Name: " + nodeName);
				if(nodeName.equals(base)){
					baseNode = node;
				}
			}
		}
		sId = getIdOfEntity("b", base);
		log("OATTR ID: " + sId + " BASENAME: " + base);
		String props = oattrPropField.getText();
		if(props == null || props.equals("")){
			JOptionPane.showMessageDialog(SchemaBuilder3.this, "Fill out properties field");
			return;
		}
		boolean bRes = createBaseOattr(name, sId, "b", descr, other, props);
		if(!bRes){
			JOptionPane.showMessageDialog(SchemaBuilder3.this, "Object attribute could not be created.\n" +
					"Cause of error is most likely an Object attribute with the name " + name + " already exists.\n" +
					"Suggestion: Change Object attribute's name to " + schemaName + "_" + name, "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		containers.add(new ArrayList<String>(Arrays.asList(base, name, sId, "b", descr, other, props)));
		log("after adding new oattr:" + containers);
		PmNode oaNode = PmNode.createObjectAttributeNode(name);//new PmNode(connectorInfo, new PmNodeChildDelegate(sysCaller, PmGraphDirection.USER, PmGraphType.USER));
		PmNode.linkNodes(baseNode, oaNode);
		
		log("child count: " + schemaNode.getChildCount());

		tree.setShowsRootHandles(true);
		treeModel = new PmGraphModel(schemaNode);
		childDelegate.setType(PmGraphType.OBJECT_ATTRIBUTES);
		resetTree(schemaNode);
		scrollPane.setViewportView(tree);
		//resetSchemaView();
		if(oattrEdit){
			nodes.clear();
			ArrayList<PmNode> childList = getAllNodes(selectedNode, nodes);//selectedNode.getChildren();
			for(PmNode n : childList){
				log("CHILD: " + n.getName());
				PmNode.linkNodes(oaNode, childList);
				for(int i = 0; i < containers.size(); i++){
					if(containers.get(i).get(1).equals(n.getName())){
						containers.set(i, new ArrayList<String>(Arrays.asList(new String[]{
								name, containers.get(i).get(1), containers.get(i).get(2),
								containers.get(i).get(3), containers.get(i).get(4), containers.get(i).get(5),
								containers.get(i).get(6)})));
					}
				}
			}
			resetSchemaView();
		}else{
			resetSchemaView();
		}
		diableOattrEditor();
	}

	protected void importSchema(String schemaName){
		log("IMPORTING");
		btnSetSchemaPermissions.setEnabled(true);
		btnSubmit.setEnabled(true);
		btnAddOattr.setEnabled(true);
		//TODO

		log(1);
		if(schemaName == null || schemaName.length() == 0){
			JOptionPane.showMessageDialog(this, "Please fill out Schema Name Field");
			return;
		}
		log(1);
		String descr = descrField.getText();
		if(descr == null || descr.length() == 0){
			descr = schemaName;
			descrField.setText(schemaName);
		}
		log(1);
		String other = otherField.getText();
		if(other == null || other.length() == 0){
			other = schemaName;
			otherField.setText(schemaName);
		}
		log(1);
		String prop = schemaPropField.getText();
		if(prop == null || prop.length() == 0){
			JOptionPane.showMessageDialog(this, "Please include at least one property for this schema.");
			return;
		}
		log(1);
		String key = schemaKeyField.getText();
		if(key == null || key.length() == 0){
			JOptionPane.showMessageDialog(this, "You need to include at least one key.\nSeparate keys by commas.");
			return;
		}
		log(1);
		//Creating schema oattr
		String sId = getIdOfEntity("p", "Schema Builder");
		boolean bRes = createBaseOattr(schemaName, sId, "p", descrField.getText(), otherField.getText(), schemaPropField.getText());
		if(!bRes){
			JOptionPane.showMessageDialog(SchemaBuilder3.this, "Could not create schema " + schemaName + ".\n Cause of error most likely a schema with the name " + 
					schemaName + " already exists", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		log(1);
		for(ArrayList<String> line : containers){
			log(line);
		}
		for(ArrayList<String> line : containers){
			log("IN LOOP");
			sId = getIdOfEntity("b", line.get(0));
			log("  ID: " + sId);
			String name = line.get(1);
			log("  Name: " + name);
			bRes = createBaseOattr(line.get(1), sId, line.get(3), line.get(4), line.get(5), line.get(6));
			if(!bRes){
				JOptionPane.showMessageDialog(SchemaBuilder3.this, "Object attribute could not be created.\n" +
						"Cause of error is most likely an Object attribute with the name " + line.get(1) + " already exists.\n" +
						"Suggestion: Change Object attribute's name to " + schemaName + "_" + line.get(1), "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}
			log("in loop");
		}
		log("end");
	}

	/**
	 * 
	 */
	public void createSchema(){
		schemaName = schemaNameField.getText();
		if(schemaName == null || schemaName.length() == 0){
			JOptionPane.showMessageDialog(this, "Please fill out Schema Name Field");
			return;
		}

		String descr = descrField.getText();
		if(descr == null || descr.length() == 0){
			descr = schemaName;
			descrField.setText(schemaName);
		}

		String other = otherField.getText();
		if(other == null || other.length() == 0){
			other = schemaName;
			otherField.setText(schemaName);
		}

		String prop = schemaPropField.getText();
		if(prop == null || prop.length() == 0){
			JOptionPane.showMessageDialog(this, "Please include at least one property for this schema.");
			return;
		}

		String key = schemaKeyField.getText();
		if(key == null || key.length() == 0){
			JOptionPane.showMessageDialog(this, "You need to include at least one key.\nSeparate keys by commas.");
			return;
		}
		System.out.println(schemaName);
		schemaNode = new PmNode(PmNodeType.OBJECT_ATTRIBUTE, schemaName);

		ArrayList<String> existingSchemas = new ArrayList<String>();
		String sId = getIdOfEntity("p", "Schema Builder");
    	log.debugStackCall("!!!!!!! getMembersOf - trace 21");	

		List<String[]> conts = getMembersOf("p", sId, "Schema Builder", "ac");
		for(int i = 0; i < conts.size(); i++){
			existingSchemas.add(conts.get(i)[2]);
		}

		boolean bRes = createBaseOattr(schemaName, sId, "p", descr, other, prop);
		if(!bRes){
			JOptionPane.showMessageDialog(SchemaBuilder3.this, "Could not create schema " + schemaName + ".\n Cause of error most likely a schema with the name " + 
					schemaName + " already exists", "ERROR", JOptionPane.ERROR_MESSAGE);
			return;
		}
		setSchemaPerms(schemaName, "b", schemaName, userName);
		//schemaNode = new PmNode();
		String id = getIdOfEntity("b", schemaName);
		schemaNode = PmNode.createObjectAttributeNode(schemaName);//new PmNode(connectorInfo, new PmNodeChildDelegate(sysCaller, PmGraphDirection.USER, PmGraphType.USER));
		baseNode = schemaNode;
		tree = new PmGraph(schemaNode, true);
		tree.addMouseListener(new SchemaMouseListener());
		tree.setShowsRootHandles(true);
		//tree = new PmGraph(schemaNode);
		treeModel = new PmGraphModel(schemaNode);

		scrollPane.setViewportView(tree);

		btnAddOattr.setEnabled(true);
		btnDeleteOattr.setEnabled(true);
		btnSetSchemaPermissions.setEnabled(true);
		btnSubmit.setEnabled(true);
	}

	/**
	 * Creates all the oattrs in this schema
	 * adds a template for this schema
	 */
	public void submitSchema(){//TODO resubmitting a schema gives me an eror that there is a duplicate template
		log(modify());
		schemaName = schemaNameField.getText();
		for(int i = 0; i < permissions.size(); i++){
			log(">>: " + permissions.get(i));
		}
		if(edit){
			String tId = getIdOfEntity("b", schemaName);
			ArrayList<String> x = getPerms(tId, "b");
			//crtSessPerms.add(x);
			//addToPerms(tId, sTableName, sTableName, "b");
			for(int i = 0; i < x.size(); i++){
				log(">>>>>" + x.get(i));
			}
			if(!x.contains("Table Modify")){
				JOptionPane.showMessageDialog(this, "You do not have permission to modify this table.", 
						"Permission denied", JOptionPane.ERROR_MESSAGE);
				return;
			}
			deleteTemplate(tempId, tempName, false);
		}

		setPerms();

		//getting keys
		String keys = schemaKeyField.getText();
		String[] pieces = keys.split(", ");
		ArrayList<String> tempKeys = new ArrayList<String>();
		for(int i = 0; i < pieces.length; i++){
			String key = pieces[i];
			tempKeys.add(key);
		}
		ArrayList<String> tempConts = new ArrayList<String>();
		for(int i = 0; i < containers.size(); i++){
			tempConts.add(containers.get(i).get(1));
		}

		log("\r\n\r\nSUBMITTED SCHEMA CONTAINERS: " + tempConts + "\r\n\r\n");
		for(int i = 0; i < permissions.size(); i++){
			log(permissions.get(i));
		}

		Packet res = SchemaBuilder3.this.addTemplate(schemaName, tempConts, tempKeys);
		if(res.hasError() || res == null){
			JOptionPane.showMessageDialog(SchemaBuilder3.this, "Could not create template");
			return;
		}

		JOptionPane.showMessageDialog(SchemaBuilder3.this, "Table was successfully created!", "Success", JOptionPane.INFORMATION_MESSAGE);
		resetAll();

	}

	/**
	 * This method, creates containers in the RBAC policy class
	 * @param sContainerName name of the container being created
	 * @param type type of the container being created
	 */
	public void createContainer(String sContainerName, String name){
		String sNewContainer = "";
		try{
			PmNode newNode, parent;
			ObjectAttribute containerSpec, parentAttribute;

			newNode = PmNode.createObjectAttributeNode(sContainerName + "-" + name);

			parent = new PmNode(PmNodeType.OBJECT_ATTRIBUTE, sContainerName);

			containerSpec = ObjectAttributes.createFromPmNode(newNode);
			parentAttribute = ObjectAttributes.createFromPmNode(parent, sysCaller);

			sNewContainer = sysCaller.addContainer(containerSpec, parentAttribute);
			System.out.println(name + "CONTAINER: " + sNewContainer);

			String nodeName = newNode.getName();
			System.out.println("NODE NAME:" + nodeName);

			String nodeType = newNode.getType();
			System.out.println("TYPE:" + name);

			String id = sysCaller.getIdOfEntityWithNameAndType(nodeName, nodeType);
			System.out.println("ID" + id);

			boolean setProp = sysCaller.addPropToOattr(id, "no", name, sContainerName);
			if(!setProp){
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				System.out.println("set prop didnt work");
				return;
			}
		}catch(Exception e){
			System.out.println("creating treatment container didn't work");
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			e.printStackTrace();
			return;
		}
	}

	/**
	 * @param sBaseName
	 * @param sBaseType
	 * @param sName
	 * @param uattr
	 */
	public void setSchemaPerms(String sBaseName, String sBaseType, String sName, String uattr) {
		try {
			Packet cmd = makeCmd("setSchemaPerms", null, sBaseName, sBaseType, sName, uattr);
			Packet res = engineClient.sendReceive(cmd, null);
			if(res == null){
				JOptionPane.showMessageDialog(this, "Setting Permissions on table returned null");
				return;
			}
			if(res.hasError()){
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param sBaseName
	 * @param sBaseType
	 * @param sName
	 * @param perms
	 * @param uattr
	 */
	public void setBasePermissions(String sBaseName, String sBaseType, String sName, String perms, String uattr) {
		log(modify);
		try {
			Packet cmd = makeCmd("setTablePerms", null, sBaseName, sBaseType, sName, perms, uattr);
			Packet res = engineClient.sendReceive(cmd, null);
			if(res == null){
				//JOptionPane.showMessageDialog(this, "Setting Permissions on table returned null");
				return;
			}
			if(res.hasError()){
				//JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void openColPermEditor(String name) {
		if(name == null || name.length() == 0){
			JOptionPane.showMessageDialog(this, "You must specify the Object attribute name first before setting the permissions");
			return;
		}
		colPermEditor.prepare(name, "b");
		colPermEditor.setColumnName(name);
		colPermEditor.setVisible(true);
	}

	/**
	 * @param base
	 * @param ret
	 * @return
	 */
	private ArrayList<PmNode> getAllNodes(PmNode base, ArrayList<PmNode> ret){;//, List<PmNode> children){
	//ret.add(base);
	log("RET: " + ret);
	List<PmNode> children = base.getChildren();
	Enumeration<PmNode> childEnum= Collections.enumeration(children);
	while(childEnum.hasMoreElements()){
		PmNode n = childEnum.nextElement();
		ret.add(n);
		getAllNodes(n, ret);
	}
	return ret;
	}
	private static ArrayList<ArrayList<String>> containers = new ArrayList<ArrayList<String>>();
	/**
	 * 
	 */
	public void resetTree(){
		resetTree((PmNode)tree.getModel().getRoot());
	}

	/**
	 * @param rootNode
	 */
	public void resetTree(PmNode rootNode) {
		//rootNode.invalidate();
	}

	/**
	 * 
	 */
	private void resetAll() {
		oattrNameField.setText("");
		baseNameField.setText("");
		oattrPropField.setText("");
		oattrDescriptionField.setText("");
		oattrOtherField.setText("");

		oattrNameField.setEnabled(false);
		baseNameField.setEnabled(false);
		oattrPropField.setEnabled(false);
		oattrDescriptionField.setEnabled(false);
		btnSetPerms.setEnabled(false);
		btnReset.setEnabled(false);
		btnCreateOattr.setEnabled(false);
		oattrOtherField.setEnabled(false);
		btnSetSchemaPermissions.setEnabled(false);
		btnSubmit.setEnabled(false);

		schemaNameField.setText("");
		descrField.setText("");
		otherField.setText("");
		schemaPropField.setText("");
		schemaKeyField.setText("");

		btnAddOattr.setEnabled(false);
		btnDeleteOattr.setEnabled(false);

		tree.setVisible(false);
		containers.clear();
		permissions.clear();
	}

	private void diableOattrEditor(){
		oattrNameField.setEnabled(false);
		baseNameField.setEnabled(false);
		oattrPropField.setEnabled(false);
		oattrDescriptionField.setEnabled(false);
		btnSetPerms.setEnabled(false);
		btnReset.setEnabled(false);
		btnCreateOattr.setEnabled(false);
		oattrOtherField.setEnabled(false);
		//btnSetSchemaPermissions.setEnabled(false);
	}

	private void resetOattrEditor(){
		oattrNameField.setEnabled(true);
		baseNameField.setEnabled(true);
		oattrPropField.setEnabled(true);
		oattrDescriptionField.setEnabled(true);
		btnSetPerms.setEnabled(true);
		btnReset.setEnabled(true);
		btnCreateOattr.setEnabled(true);
		oattrOtherField.setEnabled(true);
		oattrNameField.setText("");
		baseNameField.setText("");
		oattrPropField.setText("");
		oattrDescriptionField.setText("");
		oattrOtherField.setText("");
		oattrEdit = false;
	}

	/**
	 * @return
	 */
	private boolean checkBuilderPerms() {
		String label = "Schema Builders";
		String sId = getIdOfEntity("a", label);
    	log.debugStackCall("!!!!!!! getMembersOf - trace 22");	

		List<String[]> members = getMembersOf("a", sId, label, "ua");
		ArrayList<String> names = new ArrayList<String>();
		for(int i = 0; i < members.size(); i++){
			names.add(members.get(i)[2]);
		}
		return names.contains(userName);
	}

	/**
	 * @param b
	 */
	public void setTableModifying(String b){
		try{
			Packet cmd = makeCmd("setTableModifying", b);
			Packet res = engineClient.sendReceive(cmd, null);
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
			//exit = true;
			return;
		}
	}

	/**
	 * @return
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @return
	 */
	public int getnSimulatorPort() {
		return nSimulatorPort;
	}

	/**
	 * @return
	 */
	public String getsProcessId() {
		return sProcessId;
	}

	/**
	 * @param nCode
	 */
	private void terminate(int nCode) {
		sysCaller.exitProcess(sProcessId);
		System.exit(nCode);
	}

	protected void setSchemaName(String input){
		schemaName = input;
	}

	protected void addToContainers(ArrayList<String> input){
		containers.add(input);
	}

	protected void setSchemaField(String input){
		schemaNameField.setText(input);
	}

	protected void resetSchemaView(){
		schemaNode = PmNode.createObjectAttributeNode(schemaName);
		tree = new PmGraph(schemaNode, true);
		tree.addMouseListener(new SchemaMouseListener());
		tree.setShowsRootHandles(true);
		log("containers: " + containers);
		//PmNode baseNode = new PmNode();
		ArrayList<PmNode> existingNodes = new ArrayList<PmNode>();
		existingNodes.add(schemaNode);
		for(ArrayList<String> node : containers){
			for(int i = 0; i < existingNodes.size(); i++){
				log("Existing BaseNode : " + existingNodes.get(i).getName() + " BaseNodeId: " + existingNodes.get(i).getId());
			}
			String nodeName = node.get(1);

			String base = node.get(0);
			if(base.equals(schemaName)){
				baseNode = schemaNode;
			}else{
				for(PmNode pmNode : existingNodes){
					String name = pmNode.getName();
					if(name.equals(base)){
						baseNode = pmNode;
					}
				}
				//baseNode = PmNode.createObjectAttributeNode(base);
				log("Base: " + baseNode.getName());// + " baseID: " + baseNode.getId());
			}
			existingNodes.add(baseNode);
			PmNode oNode = PmNode.createObjectAttributeNode(nodeName);
			PmNode.linkNodes(baseNode, oNode);
			existingNodes.add(oNode);
			log("linked " + nodeName + " to base " + base);
		}
		tree.setShowsRootHandles(true);
		treeModel = new PmGraphModel(schemaNode);
		childDelegate.setType(PmGraphType.OBJECT_ATTRIBUTES);
		resetTree(schemaNode);
		scrollPane.setViewportView(tree);
	}
	/**
	 * 
	 */
	public void openSchema(){
		//TODO when opening a table a resubmitting it, permissions for already existing oattrs are created twice
		userName = getCrtUser();
		log("USERNAME: " + userName);
		//JOptionPane.showMessageDialog(SchemaBuilder3.this, userName);

		/*if(userName == null){
			JOptionPane.showMessageDialog(this, "Cannot open table", 
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}*/
		ArrayList<String> tplList = new ArrayList<String>();
		ArrayList<String> tplNames = new ArrayList<String>();

		JComboBox tableBox = new JComboBox();
		tableBox.setPreferredSize(new Dimension(160, 20));

		String sId = getIdOfEntity("p", "Schema Builder");
    	log.debugStackCall("!!!!!!! getMembersOf - trace 23");	

		List<String[]> tableList = getMembersOf("p", sId, "Schema Builder", "ac");

		DefaultComboBoxModel tableBoxModel = (DefaultComboBoxModel) tableBox.getModel();
		tableBoxModel.removeAllElements();
		for(int i = 0; i < tableList.size(); i++){
			String type = tableList.get(i)[0];
			String id = tableList.get(i)[1];
			String tableName = tableList.get(i)[2];

			ArrayList<String> res = getAttrInfo(id);
			ArrayList<String> props = new ArrayList<String>();
			for(int j = 3; j < res.size(); j++){
				props.add(res.get(j));
			}
			if(!props.contains("Creator=" + userName)){
				tableList.remove(tableName);
			}else{
				if(type.equals("b")){
					tableBoxModel.addElement(tableName);
				}else{
					tableList.remove(tableName);
				}
			}
		}

		int ret = JOptionPane.showOptionDialog(this, new Object[]
				{"Select a table to open.", tableBox}, "Select a Table", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null);
		if(ret != JOptionPane.OK_OPTION){
			return;
		}

		int sel = tableBox.getSelectedIndex();
		if(sel < 0){
			JOptionPane.showMessageDialog(this, "Please select a table to open");
		}

		Packet res = getTemplates();
		if (res == null) {
			JOptionPane.showMessageDialog(this,
					"Null result from getTemplateInfo!");
			resetAll();
			return;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, "Could not get templates");
			resetAll();
			return;
		}

		modify = true;
		permissions.clear();

		for(int i = 0; i < res.size(); i++){
			String line = res.getStringValue(i);
			String[] pieces = line.split(":");

			tplList.add(tplList.size(), pieces[1]);
			tplNames.add(tplNames.size(), pieces[0]);
		}
		log(tplList);

		tempName = (String)tableBox.getSelectedItem();
		if(tempName.equals(schemaNameField.getText())){
			JOptionPane.showMessageDialog(this, "Slected table is already open");
			return;
		}
		for(int i = 0; i < tplNames.size(); i++){
			if(tplNames.get(i).equals(tempName)){
				tempId = tplList.get(i);
			}
		}
		log(tempName + ":" + tempId);

		res = getTemplateInfo(tempId);
		if (res == null) {
			JOptionPane.showMessageDialog(this,
					"Null result from getTemplateInfo!");
			return;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, "Could not open table");
			resetAll();
			return;
		}
		String sLine = res.getStringValue(1);
		log("template line - " + sLine);
		String[] pieces = sLine.split(":");
		ArrayList<String> conts = new ArrayList<String>(Arrays.asList(pieces));

		sLine = res.getStringValue(2);
		sLine = sLine.replaceAll(", ", ":");
		schemaKeyField.setText(sLine);

		log("KEYS: " + sLine);
		sId = getIdOfEntity("b", tempName);
		ArrayList<String> attrInfo = getAttrInfo(sId);


		String s = attrInfo.get(0);
		pieces = s.split(PM_FIELD_DELIM);
		schemaName = pieces[0];
		schemaNameField.setText(schemaName);
		String tId = pieces[1];
		ArrayList<String> schemaPerms = getPerms(tId, "b");
		if(!schemaPerms.contains("Table Modify")){
			JOptionPane.showMessageDialog(this, "You do not have permission to open this table");
			return;
		}
		String prop = "";
		for (int i = 3; i < attrInfo.size()-1; i++) {
			prop += attrInfo.get(i) + ", ";
			log("PROP: " + prop);

		}
		prop += attrInfo.get(attrInfo.size()-1);
		schemaPropField.setText(schemaPropField.getText() + prop);

		btnAddOattr.setEnabled(true);
		btnDeleteOattr.setEnabled(true);
		btnSetSchemaPermissions.setEnabled(true);
		btnSubmit.setEnabled(true);


		schemaNameField.setText(schemaName);
		descrField.setText(attrInfo.get(1));
		otherField.setText(attrInfo.get(2));

		for(int i = 0; i < conts.size(); i++){
			String colId = getIdOfEntity("b", conts.get(i));

			attrInfo = getAttrInfo(colId);
			String line = attrInfo.get(0);
			pieces = line.split(":");
			String col = pieces[0];
			columnVector.add(col);

			addToPerms(colId, schemaName, col, "b");
			for(ArrayList<String> x : permissions){
				log("PERMISSIONS for " + col + " " + x);
			}
			String des = attrInfo.get(1);
			String other = attrInfo.get(2);

			prop = "";
			for(int j = 3; j < attrInfo.size()-1; j++){
				prop += attrInfo.get(j) + ", ";
			}
			prop += attrInfo.get(attrInfo.size()-1);

			String base = getContainers(colId, "b");
			String baseName = getNameOfEntity(base, "b");
			String[] data= {baseName, col, colId, "b", des, other, prop}; 
			containers.add(new ArrayList<String>(Arrays.asList(data)));
			log("data[0]:" + data[0]);
		}
		log("\r\n\r\n\r\n");
		for(ArrayList<String> line : permissions){
			log("PERMISSIONS" + line);
		}
		log("\r\n\r\n\r\n");
		//containers.remove(0);
		resetSchemaView();
		edit = true;
	}

	public ArrayList<String> getAllOattrs(String base){
		ArrayList<String> oattrs = new ArrayList<String>();
		String id = getIdOfEntity("b", base);
    	log.debugStackCall("!!!!!!! getMembersOf - trace 24");	

		List<String[]> members = getMembersOf("b", id, base, "ac");
		Iterator<String[]> it = members.iterator();
		while(it.hasNext()){
			String[] mem = it.next();
			String type = mem[0];
			String name = mem[2];
			if(type.equals("b")){
				oattrs.add(name);
			}else{
				continue;
			}
			if(it.hasNext()) getAllOattrs(it.next()[2]);
		}
		log("OATTRS: " + oattrs);
		return oattrs;
	}

	/**
	 * @param sId
	 * @return
	 */
	private ArrayList<String> getAttrInfo(String sId) {
		try{
			Packet cmd = makeCmd("getAttrInfo", sId, PM_NODE_OATTR, "no");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return null;
			}
			ArrayList<String> ret = new ArrayList<String>();
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage());
			return null;
		}
	}

	/**
	 * @return
	 */
	public String getTempName(){
		return tempName;
	}

	/**
	 * @return
	 */
	public String getTempId(){
		return tempId;
	}

	public static void log(Object input){
		/*try{
			fw.write(input + "\r\n");
			fw.flush();
		}catch(IOException e){
			e.printStackTrace();
		}*/
	}

	/**
	 * @param id
	 * @param name
	 */
	private void deleteTemplate(String id, String name, boolean deleteTable) {
		try{
			log("tplId: " + id);
			log("tplName: " + name);
			Packet r = doDeleteTemplate(id, name);
			if (r.hasError()) {
				JOptionPane.showMessageDialog(this, r.getErrorMessage());
				return;
			}
			//String name = schemaNameField.getText();
			if(deleteTable){
				deleteTable(name);
			}
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, "template didn't delete");
			return;
		}
	}

	/**
	 * @return
	 */
	public String getTableName(){
		return schemaNameField.getText();
	}

	public static boolean modify(){
		return modify;
	}

	/**
	 * 
	 */
	public void setPerms(){
		for(int i = 0; i < permissions.size(); i++){
			log(permissions.get(i));
		}

		for(int i = 0; i < permissions.size(); i++){
			String base = permissions.get(i).get(0);
			String columnName = permissions.get(i).get(1);
			String selectedUattr = permissions.get(i).get(2);
			String perms = permissions.get(i).get(3);

			setBasePermissions(columnName, "b", columnName, perms, selectedUattr);
			//permissions.remove(i);
		}

		/*String base = permissions.get(0).get(0);
		String columnName = permissions.get(0).get(1);
		String selectedUattr = permissions.get(0).get(2);
		String perms = permissions.get(0).get(3);

		setBasePermissions(base, "b", columnName, perms, selectedUattr);
		permissions.remove(0);*/
	}

	/**
	 * @param line
	 */
	public void addToPermissions(ArrayList<String> line){
		permissions.add(line);
	}

	/**
	 * @param tempName
	 * @return
	 */
	private boolean templateExists(String tempName){
		Packet res = getTemplates();
		if (res == null) {
			JOptionPane.showMessageDialog(this,
					"Null return from getTemplates!");
			return false;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return false;
		}
		Vector<String> tempNames = new Vector<String>();
		for(int i = 0; i < res.size(); i++){
			String sLine = res.getStringValue(i);
			//log("Template: " + sLine);
			String[] temp = sLine.split(":");
			String name = temp[0];
			tempNames.add(name);
		}
		//log("tempNames: " + tempNames);
		if(tempNames.contains(tempName)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * @param colName
	 * @return
	 */
	public boolean columnExists(String colName){
		ArrayList<String> oattrs = getAllOattrs(colName);
		for(int i = 0; i < oattrs.size(); i++){
			String[] oa = oattrs.get(i).split(":");
			String oattrName = oa[1];
			if(oattrName.equals(colName)){
				return true;
			}
		}
		return false;
	}

	/**
	 * @param tableName
	 * @param baseName
	 * @return
	 */
	public boolean tableNameExists(String tableName, String baseName){
		String sId = getIdOfEntity("p", baseName);
    	log.debugStackCall("!!!!!!! getMembersOf - trace 25");	

		List<String[]> containers = getMembersOf("p", sId, baseName, "ac");
		Vector<String> contNames = new Vector<String>();
		for(int i = 0; i < containers.size(); i++){
			contNames.add(containers.get(i)[2]);
		}
		//log(contNames);
		//log(tableName + " = tableName");
		if(contNames.contains(tableName)){
			//log("name is duplicate");
			return true;
		}else{
			return false;
		}
	}



	/**
	 * @param name
	 * @param baseId
	 * @param type
	 * @param descr
	 * @param other
	 * @param props
	 * @return
	 */
	public boolean createBaseOattr(String name, String baseId, String type, String descr, String other, String props){
		//setUserName();
		ArrayList<String> existingSchemas = new ArrayList<String>();
		String sId = getIdOfEntity("p", "Schema Builder");
    	log.debugStackCall("!!!!!!! getMembersOf - trace 26");	

		List<String[]> conts = getMembersOf("p", sId, "Schema Builder", "ac");
		for(int i = 0; i < conts.size(); i++){
			existingSchemas.add(conts.get(i)[2]);
		}
		try{
			Packet cmd = makeCmd(PMCommand.ADD_OATTR, null, name, descr, other,
					baseId, type, "no");
			String[] pieces = props.split(", ");
			int n = pieces.length;
			Vector<String> vprops = new Vector<String>();

			if (n > 0) {
				cmd.addItem(ItemType.CMD_ARG, "");
				for (int j = 0; j < n; j++) {
					cmd.addItem(ItemType.CMD_ARG, pieces[j]);
					vprops.add((String)pieces[j]);
				}
				log("PROPS: " + vprops);
			}else{
				cmd.addItem(ItemType.CMD_ARG, "");
			}
			//cmd.addItem(ItemType.CMD_ARG, "Schemas" + "-" + baseName);
			if(!props.contains("Creator=" + userName)){
				cmd.addItem(ItemType.CMD_ARG, "Creator" + "=" + userName);
			}

			Packet res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(SchemaBuilder3.this, "Undetermined error, null result returned");
				return false;
			}
			if (res.hasError()) {
				//JOptionPane.showMessageDialog(SchemaBuilder3.this, res.getErrorMessage() + " - res has error in createOattr");
				if(existingSchemas.contains(sTableName)){
					deleteTable(sTableName);
				}
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(SchemaBuilder3.this, ex.getMessage() + " - exception in createOattr");
			deleteTable(sTableName);
			return false;
		}
		return true;
	}

	/**
	 * @param baseName
	 * @param baseId
	 * @param type
	 * @param descr
	 * @param other
	 * @param prop
	 * @param existingSchemas
	 * @return
	 */
	public boolean createColumnOattr(String baseName, String baseId, String type, String descr, String other, String prop, ArrayList<String> existingSchemas){
		try{
			Packet cmd = makeCmd(PMCommand.ADD_OATTR, null, baseName, descr, other,
					baseId, "b", "no");
			cmd.addItem(ItemType.CMD_ARG, "");
			String[] props = prop.split(", ");
			for(int i = 0; i < props.length; i++){
				cmd.addItem(ItemType.CMD_ARG, props[i]);
			}

			ArrayList<String> arrProps = new ArrayList<String>(Arrays.asList(props));
			log(arrProps);
			if(!arrProps.contains(sTableName + "-" + baseName))
				cmd.addItem(ItemType.CMD_ARG, sTableName + "-" + baseName);
			if(!arrProps.contains("Creator" + "=" + userName))
				cmd.addItem(ItemType.CMD_ARG, "Creator" + "=" + userName);

			Packet res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
				return false;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage() + " - res has error in createOattr");
				if(existingSchemas.contains(sTableName)){
					deleteTable(sTableName);
				}
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, ex.getMessage() + " - exception in createOattr");
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	public ArrayList<String> getOattrs(){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getOattrs");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
				log(ret.get(i));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
		return ret;	
	}

	/**
	 * @return
	 */
	public String getUserName(){
		return userName;
	}

	/**
	 * @return
	 */
	public ArrayList<ArrayList<String>> getPermissions(){
		return permissions;
	}

	/**
	 * @param id
	 * @param type
	 * @return
	 */
	public ArrayList<String> getPerms(String id, String type){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getPermittedOpsOnEntity", sProcessId, id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
				log("ret: " + ret.get(i));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
		return ret;		
	}

	/**
	 * @param id
	 * @param sBaseName
	 * @param name
	 * @param type
	 */
	public void addToPerms(String id, String sBaseName, 
			String name, String type){
		//permissions.clear();
		for(int i = 0; i < permissions.size(); i++){
			log(permissions.get(i));
		}
		//For opsets
		ArrayList<String> ops = getFromOpsets(id);
		processOps(ops, sBaseName, name);

		//For reps
		ArrayList<String> reps = getReps(id, type);
		for(int i = 0; i < reps.size(); i++){
			String repId = reps.get(i);
			String rep = getNameOfEntity(repId, "ob");
			log(rep);
			rep = getAssocOattr(repId);
			ArrayList<String> repOps = getFromOpsets(rep);
			processOps(repOps, sBaseName, name);
		}
	}

	/**
	 * @param ops
	 * @param sBaseName
	 * @param name
	 */
	public void processOps(ArrayList<String> ops, String sBaseName, String name){
		String perms = "";
		String ua = "";
		for(int i = 0; i < ops.size(); i++){
			String op = ops.get(i);
			ArrayList<String> opInfo = getOpsetInfo(op);
			log(">>>: " + op);
			for(int j = 0; j < opInfo.size(); j++){
				log("  >>>: " + opInfo.get(j));
				if(j > 4){
					perms += opInfo.get(j) + ",";

				}
			}
			log(perms);
			ArrayList<String> uattrs = getFromAttrs(op);
			for(int x = 0; x < uattrs.size(); x++){
				ua = uattrs.get(x);
				ua = getNameOfEntity(ua, "a");
				log("      >>>: " + ua);
			}
			ArrayList<String> line = new ArrayList<String>();
			line.add(sBaseName);
			line.add(name);
			line.add(ua);
			line.add(perms.substring(0, perms.length() - 1));
			log(line);
			//permissions.add(line);
			existingPermissions.add(line);
		}
	}


	/**
	 * @param id
	 * @return
	 */
	public String getAssocOattr(String id){
		try{
			Packet cmd = makeCmd("getAssocOattr1", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			return res.getStringValue(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
	}

	/**
	 * @param id
	 * @param type
	 * @return
	 */
	public ArrayList<String> getReps(String id, String type){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getReps", id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
			log(ret);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
		return ret;
	}
	/**
	 * @param id
	 * @param type
	 * @return
	 */
	public String getNameOfEntity(String id, String type){
		try{
			Packet cmd = makeCmd("getNameOfEntityWithIdAndType", id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			return res.getStringValue(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
	}

	/**
	 * @param id
	 * @return
	 */
	/**
	 * @param id
	 * @return
	 */
	public ArrayList<String> getOpsetInfo(String id){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getOpsetInfo", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
			log(ret);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
		return ret;
	}

	/**
	 * @param id
	 * @return
	 */
	public ArrayList<String> getFromOpsets(String id){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getFromOpsets1", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
		return ret;
	}

	/**
	 * @param name
	 */
	public void deleteTable(String name){
		String id = getIdOfEntity("b", name);
    	log.debugStackCall("!!!!!!! getMembersOf - trace 27");	

		List<String[]> mems = getMembersOf("b", id, name, "ac");
		mems.add(new String[]{"b", id, name});
		boolean res1 = doDeleteTable(name, "b", mems, name);
		if(!res1){
			log("deleteing table didnt work");
			JOptionPane.showMessageDialog(this, "Could not delete existing table.\nProgram will exit");
			terminate(0);
		}
	}

	/**
	 * @param name
	 * @param type
	 * @param columns
	 * @param tableName
	 * @return
	 */
	public boolean doDeleteTable(String name, String type, List<String[]> columns, String tableName){
		String sId = getIdOfEntity(type, name);
		if(sId == null){
			log("Table successfully deleted");
			return true;
		}
		if(type.equals("b")){
			boolean res = deleteObjectFromTable(name);
			if(!res){
				return false;
			}
			List<String[]> members = getMembersOf(type, sId, name, "ac");
			if(members.size() == 0){
				deleteNode(sId, type);
				return doDeleteTable(tableName, "b", columns, tableName);
			}else{
				String[] member = members.get(0);
				String memType = member[0];
				String memName = member[2];
				members.remove(0);
				if(memType.equals("b")){
					doDeleteTable(memName, memType, members, tableName);
				}else if(memType.equals("s")){
					deleteOpset(memName);
					doDeleteTable(name, type, columns, tableName);
				}
			}
		}else if(type.equals("s")){
			deleteOpset(name);
			doDeleteTable(name, type, columns, tableName);
		}
		return true;
	}

	/**
	 * @param name
	 */
	public void deleteOpset(String name){
		log("type = s");
		String sId  = getIdOfEntity("s", name);
		log("got id");
		ArrayList<String> op = getFromAttrs(sId);
		log("gotFromAttrs: ");
		log(op);
		for(int j = 0; j < op.size(); j++){
			log("deleting assignemnt");
			boolean res = deleteAssignment(op.get(j), "a", sId, "s");
			if(!res){	
				log("!!><Didn't delete assignemnt");
			}else{
				log("deleted assignment");
				log("s1");
				ArrayList<String> oa = getToAttrs(sId);
				log("s2");
				for(int i = 0; i < oa.size(); i++){
					res = deleteAssignment(sId, "s", oa.get(i), "b");
					if(!res){
						log("could not delete asignment");
					}
				}
				log("s3");
				deleteNode(sId, "s");
				log("s4");
			}
		}
	}

	/**
	 * @param id1
	 * @param type1
	 * @param id2
	 * @param type2
	 * @return
	 */
	public boolean deleteAssignment(String id1, String type1, String id2, String type2){
		try{
			Packet cmd = makeCmd("deleteAssignment", null, id1, type1, id2, type2, "no");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return false;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return false;
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return false;
		}
		return true;
	}

	/**
	 * @param id
	 * @return
	 */
	public ArrayList<String> getToAttrs(String id){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getToAttrs1", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
		return ret;

	}

	private String getContainers(String id, String type){
		try{
			Packet cmd = makeCmd("getContainers", id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			return res.getItemStringValue(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public ArrayList<String> getFromAttrs(String id){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getFromAttrs1", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
		return ret;

	}

	/**
	 * @param sTableName
	 * @return
	 */
	public boolean deleteObjectFromTable(String sTableName){
		try{
			Packet cmd = makeCmd("deleteContainerObjects", sTableName);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return false;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return false;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return false;
		}
		return true;
	}

	/**
	 * @param baseId
	 * @param baseType
	 */
	public void deleteNode(String baseId, String baseType){
		try{
			Packet cmd = makeCmd("deleteNode", baseId, baseType, "no");
			Packet res = engineClient.sendReceive(cmd, null);
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

	/**
	 * @param sType
	 * @param sId
	 * @param sLabel
	 * @param sGraphType
	 * @return
	 */
	/**
	 * @param sType
	 * @param sId
	 * @param sLabel
	 * @param sGraphType
	 * @return
	 */
	public List<String[]> getMembersOf(String sType, String sId, String sLabel, String sGraphType) {
		Packet res = null;
		try {
	    	log.debugStackCall("!!!!!!! getMembersOf - trace 28");	

			Packet cmd = makeCmd("getMembersOf", sLabel, sId, sType, sGraphType);
			res = engineClient.sendReceive(cmd, null);
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

	/**
	 * @param id
	 * @param type
	 * @return
	 */
	public ArrayList<String> getUatts(String id, String type){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getToAttrsUser");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
				log("UA: " + ret.get(i));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
		return ret;	
	}

	/**
	 * @param sId
	 * @return
	 */
	public String getUserFullName(String sId){
		try{
			Packet cmd = makeCmd("getUserFullNamePacket", sId);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			String name = res.getStringValue(0);
			log(name);
			return name;	
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public String getDescr(){
		return descrField.getText();
	}

	public String getOther(){
		return otherField.getText();
	}

	/**
	 * @return
	 */
	public String getCrtUser(){
		String line = getUserId();
		log(line);
		String[] pieces = line.split(":");
		String userId = pieces[1];
		String name = getUserFullName(userId);
		log(name);
		return name;
	}
	/**
	 * @return
	 */
	public String getUserId(){
		try{
			Packet cmd = makeCmd("getSessionName", sessionId);
			//Packet cmd = makeCmd("getUsersAndAttrs", sessionId);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return null;
			}
			//String[] pieces =  res.getStringValue(0).split(":");
			//String userId = pieces[1];
			String sName = res.getStringValue(0);
			sName = sName.substring(0, sName.indexOf("@"));
			//fw.append("user name: " + userName);
			String id = getIdOfEntity("u", sName);
			return sName + ":" + id;
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, "Could not find user id");
			e.printStackTrace();
			return null;
		}	
	}

	/**
	 * @param sType
	 * @param sName
	 * @return
	 */
	public String getIdOfEntity(String sType, String sName){
		//log("sType: " + sType + " sName: " + sName);
		try{
			Packet cmd = makeCmd(GET_ID_OF_ENTITY_WITH_NAME_AND_TYPE, sName, sType);
			Packet res = engineClient.sendReceive(cmd, null);
			if(res.hasError()){
				log(res.getErrorMessage());
				return null;
			}
			//log(res.getStringValue(0));
			return res.getStringValue(0);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param sObjId
	 * @param sObjName
	 * @return
	 */
	public Packet doDeleteTemplate(String sObjId, String sObjName){
		log("deleting template with id " + sObjId);
		try{
			Packet cmd = makeCmd("deleteTemplate", sObjId, sObjName);
			Packet res = engineClient.sendReceive(cmd, null);
			if(res.hasError()){
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return null;
			}
			//log(res.getStringValue(0));
			return res;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return
	 */
	public Packet getTemplates(){
		try {
			Packet cmd = makeCmd(GET_TEMPLATES);
			Packet res = engineClient.sendReceive(cmd, null);
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

	/**
	 * @param sTplId
	 * @return
	 */
	public Packet getTemplateInfo(String sTplId) {
		try {
			Packet cmd = makeCmd(GET_TEMPLATE_INFO, sTplId);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param sName name of the template
	 * @param containers list of containers to add to the template
	 * @param keys keys for the template
	 * @return
	 */
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
			log("Containers: " + s);
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
			log("Keys: " + s);
			if (s.length() > 0) {
				cmd.addItem(ItemType.CMD_ARG, s);
			} else {
				cmd.addItem(ItemType.CMD_ARG, "");
			}
			Packet res = engineClient.sendReceive(cmd, null);
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

	/**
	 * @param cmd
	 * @param sArgs
	 * @return
	 */
	public Packet makeCmd(PMCommand cmd, String... sArgs){
		try {
			return makeCmd(cmd.commandCode(), sArgs);
		} catch (Exception e) {
			Throwables.propagate(e);
		}
		return failurePacket();
	}


	/**
	 * @param sCode
	 * @param sArgs
	 * @return
	 * @throws Exception
	 */
	public Packet makeCmd(String sCode, String... sArgs) throws Exception {
		return CommandUtil.makeCmd(sCode, sessionId == null ? "" : sessionId, sArgs);
	}

	static String sessid;
	static String pid;
	static int simport;
	static boolean debug;
	private JMenuItem mntmNewMenuItem;
	private JMenuItem mntmNewMenuItem_1;
	private JMenuItem mntmNewMenuItem_2;
	private JMenuItem mntmNewMenuItem_3;
	private JSeparator separator;
	private JSeparator separator_1;
	private JSeparator separator_2;
	private JButton btnSetSchemaPermissions;
	private JPanel panel_4;
	private JPanel panel_6;

	public static void createGUI(){
		new SchemaBuilder3(sessid, simport, pid, debug);
	}

	/**
	 * 
	 */
	public void createEditorGUI(){
		//SchemaEditor3 editor = new SchemaEditor3(this);
		//editor.setVisible(true);
	}

	public static void main(String[] args){
		//log("main called in schemabuilder 2");
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-session")) {
				sessid = args[++i];
			} else if (args[i].equals("-process")) {
				pid = args[++i];
			} else if (args[i].equals("-simport")) {
				simport = Integer.valueOf(args[++i]).intValue();
			} else if (args[i].equals("-debug")) {
				debug = true;
			} 
		}
		//log(sessid + " " + pid + " " + simport + " " + debug);
		if (sessid == null) {
			log("This application must run within a Policy Machine session!");
			System.exit(-1);
		}
		if (pid == null) {
			log("This application must run in a Policy Machine process!");
			System.exit(-1);
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				createGUI();
			}
		});
	}

	class ButtonCellRenderer extends JButton implements TableCellRenderer{

		private static final long serialVersionUID = 1L;

		public ButtonCellRenderer(){
			setText("Set Permissions");
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			return this;
		}
	}

	class ButtonCellEditor extends DefaultCellEditor{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JTree button;
		public ButtonCellEditor(JCheckBox check) {
			super(check);
			button = new JTree();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			//button.setText(label);
			return button;
		}

		@Override
		public boolean stopCellEditing() {
			return super.stopCellEditing();
		}

		@Override
		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}

	class AddButtonCellRenderer extends JButton implements TableCellRenderer{

		private static final long serialVersionUID = 1L;

		public AddButtonCellRenderer(){
			setText("+");
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			return this;
		}
	}

	class AddButtonCellEditor extends DefaultCellEditor{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JButton button;
		public AddButtonCellEditor(JCheckBox check) {
			super(check);
			button = new JButton();
			button.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					fireEditingStopped();
					int row, col;
					row = table.getSelectedRow();
					col = 0;
					model.getValueAt(row, col);

				}

			});
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			//button.setText(label);
			return button;
		}

		@Override
		public boolean stopCellEditing() {
			return super.stopCellEditing();
		}

		@Override
		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}
	private PmNode selectedNode;
	class SchemaMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

			String base = "";
			if (selRow != -1) {
				PmNode node = (PmNode) selPath.getLastPathComponent();
				selectedNode = node;
				if(node.getName().equals(schemaName)){
					base = schemaName;
				}else{
					base = node.getParent().getName();
				}

				String name = node.getName();
				selectedOattr = name;
				oattrNameField.setText(name);
				baseNameField.setText(base);
				String id = getIdOfEntity("b", name);
				ArrayList<String> res = getAttrInfo(id);
				String s = res.get(0);
				String[] pieces = s.split(PM_FIELD_DELIM);
				String oattrName = pieces[0];
				oattrNameField.setText(oattrName);

				String descr = res.get(1);
				oattrDescriptionField.setText(descr);

				String other = res.get(2);
				oattrOtherField.setText(other);

				String prop = "";
				for (int i = 3; i < res.size()-1; i++) {
					prop += res.get(i) + ", ";
					log("PROP: " + prop);
					prop = prop.substring(0, prop.length() - 1);
				}
				prop += res.get(res.size()-1);
				oattrPropField.setText(prop);

				//resetOattrEditor();
				oattrNameField.setEnabled(true);
				baseNameField.setEnabled(true);
				oattrPropField.setEnabled(true);
				oattrDescriptionField.setEnabled(true);
				btnSetPerms.setEnabled(true);
				btnReset.setEnabled(true);
				btnCreateOattr.setEnabled(true);
				oattrOtherField.setEnabled(true);
				oattrEdit = true;
			}
		}
	}
}

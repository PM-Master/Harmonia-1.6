package gov.nist.csd.pm.admin;

import java.awt.Component;
import java.awt.EventQueue;
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

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AuditGUI extends JFrame {

	private JPanel contentPane;
	static String information = "";
	static String statement  = "";
	public static ArrayList<String> stmt = new ArrayList<String>();
	static String sessionId = "SESS_ID";
	static String userId = "USER_ID";
	static String userName = "USER_NAME";
	static String hostName = "HOST_NAME";
	static String timeStamp = "TS";
	static String action = "ACTION";
	static String result = "RESULT_SUCCESS";
	static String description = "DESCRIPTION";
	static String objectId = "OBJ_ID";
	static String objectName = "OBJ_NAME";
	static boolean isEmpty = true;
	static boolean si = false;
	static boolean ui = false;
	static boolean un = false;
	static boolean hn = false;
	static boolean ts = false;
	static boolean a = false;
	static boolean r = false;
	static boolean d = false;
	static boolean oi = false;
	static boolean on = false;
	public static ArrayList<String> restOfStmt = new ArrayList<String>();
	public static boolean unf = false;
	static boolean hnf = false;
	static boolean tsf1 = false;
	static boolean tsf2 = false;
	static boolean af = false;
	static boolean rf = false;
	static boolean oif = false;
	static boolean onf = false;
	
	/**
	 * @wbp.nonvisual location=221,281
	 */
	private final Component horizontalStrut = Box.createHorizontalStrut(20);
	private static JTextField userNameField;
	private static JTextField hostNameField;
	private static JTextField timestampField1;
	private static JTextField timestampField2;
	private static JTextField actionField;
	private static JComboBox resultsComboBox; 
	private static JTextField objIdField;
	private static JTextField objNameField;
	static JCheckBox chckbxSessionId;
	static JCheckBox chckbxUserId;
	static JCheckBox chckbxUserName;
	static JCheckBox chckbxHostName;
	static JCheckBox chckbxTimestamp;
	static JCheckBox chckbxAction;
	static JCheckBox chckbxResults;
	static JCheckBox chckbxDescription;
	static JCheckBox chckbxObjectId;
	static JCheckBox chckbxObjectName;
	static JCheckBox chckbxSelectAll;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AuditGUI frame = new AuditGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 **/
	public AuditGUI() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stmt.clear();
				restOfStmt.clear();
				reset();
			}
		});
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 632, 708);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmSubmit = new JMenuItem("Submit");
		mnFile.add(mntmSubmit);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		chckbxSessionId = new JCheckBox("Session ID");
		GridBagConstraints gbc_chckbxSessionId = new GridBagConstraints();
		gbc_chckbxSessionId.anchor = GridBagConstraints.WEST;
		gbc_chckbxSessionId.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSessionId.gridx = 2;
		gbc_chckbxSessionId.gridy = 1;
		contentPane.add(chckbxSessionId, gbc_chckbxSessionId);
		
		
		chckbxSessionId.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxSessionId.isSelected() == true){
					si = true;
					uiFlagsAndAll(si);
				}else{
					si = false;
					uiFlagsAndAll(si);
				}
			}
		});
		
		chckbxUserId = new JCheckBox("User ID");
		GridBagConstraints gbc_chckbxUserId = new GridBagConstraints();
		gbc_chckbxUserId.anchor = GridBagConstraints.WEST;
		gbc_chckbxUserId.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUserId.gridx = 2;
		gbc_chckbxUserId.gridy = 2;
		contentPane.add(chckbxUserId, gbc_chckbxUserId);
		
		chckbxUserId.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxUserId.isSelected() == true){
					ui = true;
					uiFlagsAndAll(ui);
				}else{
					ui = false;
					uiFlagsAndAll(ui);
				}
			}
		});
		
		chckbxUserName = new JCheckBox("User Name");
		GridBagConstraints gbc_chckbxUserName = new GridBagConstraints();
		gbc_chckbxUserName.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUserName.anchor = GridBagConstraints.WEST;
		gbc_chckbxUserName.gridx = 2;
		gbc_chckbxUserName.gridy = 3;
		contentPane.add(chckbxUserName, gbc_chckbxUserName);
		
		chckbxUserName.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxUserName.isSelected() == true){
					un = true;
					unFlagsAndAll(un);
					userNameField.setEnabled(true);
					System.out.println("BEFORE Username Field: "+userNameField.getText());
					if(userNameField.getText().isEmpty()!= true){
						unf = true;
						System.out.println("AFTER UserName Field: "+userNameField.getText());
					}else{
						unf = false;
					}
					
				}else if(chckbxUserName.isSelected() == false){
					un = false;
					userNameField.setEnabled(false);
					unFlagsAndAll(un);
				}
			}
		});
		
		userNameField = new JTextField();
		userNameField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
				warn();
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			public void warn(){
				unf = true;
				System.out.println("IN THE WARN() METHOD "+userNameField.getText());
			}
		});
		userNameField.setEnabled(false);
		GridBagConstraints gbc_userNameField = new GridBagConstraints();
		gbc_userNameField.insets = new Insets(0, 0, 5, 5);
		gbc_userNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_userNameField.gridx = 4;
		gbc_userNameField.gridy = 3;
		contentPane.add(userNameField, gbc_userNameField);
		userNameField.setColumns(10);
		
		chckbxHostName = new JCheckBox("Host Name");
		GridBagConstraints gbc_chckbxHostName = new GridBagConstraints();
		gbc_chckbxHostName.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxHostName.gridx = 2;
		gbc_chckbxHostName.gridy = 4;
		contentPane.add(chckbxHostName, gbc_chckbxHostName);
		
		chckbxHostName.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxHostName.isSelected() == true){
					hn = true;
					hnFlagsAndAll(hn);
					hostNameField.setEnabled(true);
					if(hostNameField.getText().isEmpty() != true){
						hnf = true;
						System.out.println("After host name field: "+hostNameField.getText());
					}else{
						hnf = false;
					}
				}else if(chckbxHostName.isSelected() == false){
					hn = false;
					hostNameField.setEnabled(false);
					hnFlagsAndAll(hn);
				}
			}
		});
		
		hostNameField = new JTextField();
		hostNameField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
				warn();
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			public void warn(){
				hnf = true;
				System.out.println("IN THE WARN() METHOD "+hostNameField.getText());
			}
		});
		hostNameField.setEnabled(false);
		GridBagConstraints gbc_hostNameField = new GridBagConstraints();
		gbc_hostNameField.insets = new Insets(0, 0, 5, 5);
		gbc_hostNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_hostNameField.gridx = 4;
		gbc_hostNameField.gridy = 4;
		contentPane.add(hostNameField, gbc_hostNameField);
		hostNameField.setColumns(10);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.gridwidth = 3;
		gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut.gridx = 0;
		gbc_horizontalStrut.gridy = 5;
		contentPane.add(horizontalStrut, gbc_horizontalStrut);
		
		chckbxTimestamp = new JCheckBox("Timestamp");
		GridBagConstraints gbc_chckbxTimestamp = new GridBagConstraints();
		gbc_chckbxTimestamp.anchor = GridBagConstraints.WEST;
		gbc_chckbxTimestamp.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxTimestamp.gridx = 2;
		gbc_chckbxTimestamp.gridy = 7;
		contentPane.add(chckbxTimestamp, gbc_chckbxTimestamp);
		
		
		chckbxTimestamp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxTimestamp.isSelected() == true){
					ts = true;
					tsFlagsAndAll(ts);
					timestampField1.setEnabled(true);
					if(timestampField1.getText().isEmpty() != true){
						tsf1 = true;
						System.out.println("After timestamp field: "+timestampField1.getText());
					}else{
						tsf1 = false;
					}
				}else{
					ts = false;
					timestampField1.setEnabled(false);
					tsFlagsAndAll(ts);
				}
			}
		});
		
		timestampField1 = new JTextField();
		
		timestampField1.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				timestampField2.setEnabled(false);
				warn();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				timestampField2.setEnabled(true);
				warn();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
				warn();
			}
			
			public void warn(){
				tsf1 = true;
				System.out.println("IN THE WARN() METHOD "+timestampField1.getText());
			}
		});
		
		timestampField1.setEnabled(false);
		GridBagConstraints gbc_timestampField1 = new GridBagConstraints();
		gbc_timestampField1.insets = new Insets(0, 0, 5, 5);
		gbc_timestampField1.fill = GridBagConstraints.HORIZONTAL;
		gbc_timestampField1.gridx = 4;
		gbc_timestampField1.gridy = 7;
		contentPane.add(timestampField1, gbc_timestampField1);
		timestampField1.setColumns(10);
		
		JLabel lblAndl = new JLabel("AND");
		GridBagConstraints gbc_lblAndl = new GridBagConstraints();
		gbc_lblAndl.anchor = GridBagConstraints.EAST;
		gbc_lblAndl.insets = new Insets(0, 0, 5, 5);
		gbc_lblAndl.gridx = 5;
		gbc_lblAndl.gridy = 7;
		contentPane.add(lblAndl, gbc_lblAndl);
		
		timestampField2 = new JTextField();
		timestampField2.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
				warn();
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			public void warn(){
				tsf2 = true;
				System.out.println("IN THE WARN() METHOD "+timestampField2.getText());
			}
		});
		timestampField2.setEnabled(false);
		GridBagConstraints gbc_timestampField2 = new GridBagConstraints();
		gbc_timestampField2.insets = new Insets(0, 0, 5, 0);
		gbc_timestampField2.fill = GridBagConstraints.HORIZONTAL;
		gbc_timestampField2.gridx = 6;
		gbc_timestampField2.gridy = 7;
		contentPane.add(timestampField2, gbc_timestampField2);
		timestampField2.setColumns(10);
		
		chckbxAction = new JCheckBox("Action");
		GridBagConstraints gbc_chckbxAction = new GridBagConstraints();
		gbc_chckbxAction.anchor = GridBagConstraints.WEST;
		gbc_chckbxAction.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxAction.gridx = 2;
		gbc_chckbxAction.gridy = 8;
		contentPane.add(chckbxAction, gbc_chckbxAction);
		
		chckbxAction.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxAction.isSelected() == true){
					a = true;
					aFlagsAndAll(a);
					actionField.setEnabled(true);
					if(actionField.getText().isEmpty() != true){
						af = true;
						System.out.println("After action field: "+actionField.getText());
					}else{
						af = false;
					}
				}else{
					a = false;
					actionField.setEnabled(false);
					aFlagsAndAll(a);
				}
			}
		});
		
		actionField = new JTextField();
		actionField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
				warn();
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			public void warn(){
				af = true;
				System.out.println("IN THE WARN() METHOD "+actionField.getText());
			}
		});
		actionField.setEnabled(false);
		GridBagConstraints gbc_actionField = new GridBagConstraints();
		gbc_actionField.insets = new Insets(0, 0, 5, 5);
		gbc_actionField.fill = GridBagConstraints.HORIZONTAL;
		gbc_actionField.gridx = 4;
		gbc_actionField.gridy = 8;
		contentPane.add(actionField, gbc_actionField);
		actionField.setColumns(10);
		
		chckbxDescription = new JCheckBox("Description");
		GridBagConstraints gbc_chckbxDescription = new GridBagConstraints();
		gbc_chckbxDescription.anchor = GridBagConstraints.WEST;
		gbc_chckbxDescription.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxDescription.gridx = 2;
		gbc_chckbxDescription.gridy = 9;
		contentPane.add(chckbxDescription, gbc_chckbxDescription);
		
		chckbxDescription.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxDescription.isSelected() == true){
					d = true;
					dFlagsAndAll(d);
				}else{
					d = false;
					dFlagsAndAll(d);
				}
			}
		});
		
		chckbxResults = new JCheckBox("Results");
		GridBagConstraints gbc_chckbxResults = new GridBagConstraints();
		gbc_chckbxResults.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxResults.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc_chckbxResults.gridx = 2;
		gbc_chckbxResults.gridy = 10;
		contentPane.add(chckbxResults, gbc_chckbxResults);
		resultsComboBox = new JComboBox();
		resultsComboBox.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				rf = true;
				System.out.println("IN THE actionPerformed() METHOD "+resultsComboBox.getSelectedItem().toString());
			}
		});

		resultsComboBox.setModel(new DefaultComboBoxModel(new String[] {"Select One", "TRUE", "FALSE"}));
		resultsComboBox.setEnabled(false);
		resultsComboBox.setEditable(false);
		GridBagConstraints gbc_resultsComboBox = new GridBagConstraints();
		chckbxResults.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxResults.isSelected() == true){
					r = true;
					rFlagsAndAll(r);
					resultsComboBox.setEnabled(true);
					if((resultsComboBox.getSelectedItem().toString() == "TRUE")||(resultsComboBox.getSelectedItem().toString() == "FALSE")){
						rf = true;
					}else{
						rf = false;
					}
				}else{
					r = false;
					resultsComboBox.setEnabled(false);
					rFlagsAndAll(r);
				}
			}
		});
		gbc_resultsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_resultsComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_resultsComboBox.gridx = 4;
		gbc_resultsComboBox.gridy = 10;
		contentPane.add(resultsComboBox, gbc_resultsComboBox);
		
		chckbxObjectId = new JCheckBox("Object ID");
		chckbxObjectId.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (chckbxObjectId.isSelected() == true){
					oi = true;
					oiFlagsAndAll(oi);
					objIdField.setEnabled(true);
					if(objIdField.getText().isEmpty() != true){
						restOfStmt.add("'OBJ_ID' LIKE "+objIdField.getText());
					}
				}else{
					oi = false;
					objIdField.setEnabled(false);
					oiFlagsAndAll(oi);
				}
			}
		});
		
		GridBagConstraints gbc_chckbxObjectId = new GridBagConstraints();
		gbc_chckbxObjectId.anchor = GridBagConstraints.WEST;
		gbc_chckbxObjectId.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxObjectId.gridx = 2;
		gbc_chckbxObjectId.gridy = 11;
		contentPane.add(chckbxObjectId, gbc_chckbxObjectId);
		objIdField = new JTextField();
		objIdField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
				warn();
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			public void warn(){
				oif = true;
				System.out.println("IN THE WARN() METHOD "+objIdField.getText());
			}
		});
		objIdField.setEnabled(false);
		GridBagConstraints gbc_objIdField = new GridBagConstraints();
		gbc_objIdField.insets = new Insets(0, 0, 5, 5);
		gbc_objIdField.fill = GridBagConstraints.HORIZONTAL;
		gbc_objIdField.gridx = 4;
		gbc_objIdField.gridy = 11;
		contentPane.add(objIdField, gbc_objIdField);
		objIdField.setColumns(10);
		
		
		
		
		chckbxObjectName = new JCheckBox("Object Name");
		chckbxObjectName.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(chckbxObjectName.isSelected() == true){
					on = true;
					onFlagsAndAll(on);
					objNameField.setEnabled(true);
					if(objNameField.getText().isEmpty() != true){
						restOfStmt.add("'OBJ_NAME' LIKE "+objNameField.getText());
					}
				}else{
					on = false;
					onFlagsAndAll(on);
				}
			}
		});
		
		
		GridBagConstraints gbc_chckbxObjectName = new GridBagConstraints();
		gbc_chckbxObjectName.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxObjectName.gridx = 2;
		gbc_chckbxObjectName.gridy = 12;
		contentPane.add(chckbxObjectName, gbc_chckbxObjectName);
		
		objNameField = new JTextField();
		objNameField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				
				warn();
				
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				warn();
			}
			
			public void warn(){
				onf = true;
				System.out.println("IN THE WARN() METHOD "+objNameField.getText());
			}
		});
		objNameField.setEnabled(false);
		GridBagConstraints gbc_objNameField = new GridBagConstraints();
		gbc_objNameField.insets = new Insets(0, 0, 5, 5);
		gbc_objNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_objNameField.gridx = 4;
		gbc_objNameField.gridy = 12;
		contentPane.add(objNameField, gbc_objNameField);
		objNameField.setColumns(10);
		
		chckbxSelectAll = new JCheckBox("Select All");
		GridBagConstraints gbc_chckbxSelectAll = new GridBagConstraints();
		gbc_chckbxSelectAll.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSelectAll.gridx = 4;
		gbc_chckbxSelectAll.gridy = 14;
		contentPane.add(chckbxSelectAll, gbc_chckbxSelectAll);
		
		
		
		//This takes care of the Select All check box
		chckbxSelectAll.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(chckbxSelectAll.isSelected()== true){
					chckbxSessionId.setSelected(true);
					chckbxUserId.setSelected(true);
					chckbxUserName.setSelected(true);
					chckbxHostName.setSelected(true);
					chckbxTimestamp.setSelected(true);
					chckbxAction.setSelected(true);
					chckbxResults.setSelected(true);
					chckbxDescription.setSelected(true);
					chckbxObjectId.setSelected(true);
					chckbxObjectName.setSelected(true);
					si = true;
					ui = true;
					un = true;
					hn = true;
					ts = true;
					a = true;
					r = true;
					d = true;
					on = true;
					oi = true;
					uiFlagsAndAll(ui);
					siFlagsAndAll(si);
					unFlagsAndAll(un);
					hnFlagsAndAll(hn);
					tsFlagsAndAll(ts);
					aFlagsAndAll(a);
					rFlagsAndAll(r);
					dFlagsAndAll(d);
					oiFlagsAndAll(oi);
					onFlagsAndAll(on);
				}else{
					chckbxSessionId.setSelected(false);
					chckbxUserId.setSelected(false);
					chckbxUserName.setSelected(false);
					chckbxHostName.setSelected(false);
					chckbxTimestamp.setSelected(false);
					chckbxAction.setSelected(false);
					chckbxResults.setSelected(false);
					chckbxDescription.setSelected(false);
					chckbxObjectId.setSelected(false);
					chckbxObjectName.setSelected(false);
					si = false;
					ui = false;
					un = false;
					hn = false;
					ts = false;
					a = false;
					r = false;
					d = false;
					on = false;
					oi = false;
					uiFlagsAndAll(ui);
					siFlagsAndAll(si);
					unFlagsAndAll(un);
					hnFlagsAndAll(hn);
					tsFlagsAndAll(ts);
					aFlagsAndAll(a);
					rFlagsAndAll(r);
					dFlagsAndAll(d);
					oiFlagsAndAll(oi);
					onFlagsAndAll(on);
				}
			}
		});
		
		final JButton btnSubmit = new JButton("Submit");
		GridBagConstraints gbc_btnSubmit = new GridBagConstraints();
		gbc_btnSubmit.insets = new Insets(0, 0, 5, 5);
		gbc_btnSubmit.gridx = 4;
		gbc_btnSubmit.gridy = 15;
		contentPane.add(btnSubmit, gbc_btnSubmit);
		
		btnSubmit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				addsTheGoodBits();
				System.out.println(statement);
				AuditResultsHTML.main(null);
			}
		});
	}
	
	public static String checksIfEmpty(String statement){
		System.out.println("In the checksIfEmpty first part:" + statement);

		if(statement.isEmpty()== true ){
			isEmpty = true;
			return statement;
		}else{
			isEmpty = false;
			return statement;
		}
	}
	
	public static boolean siFlagsAndAll(boolean si){
		return si; 
	}
	
	public static boolean uiFlagsAndAll(boolean ui){
		return ui; 
	}

	public static boolean unFlagsAndAll(boolean un){
		return un; 
	}
	
	public static boolean hnFlagsAndAll(boolean hn){
		return hn; 
	}
	
	public static boolean tsFlagsAndAll(boolean ts){
		return ts; 
	}
	
	public static boolean aFlagsAndAll(boolean a){
		return a; 
	}
	
	public static boolean rFlagsAndAll(boolean r){
		return r; 
	}
	
	public static boolean dFlagsAndAll(boolean d){
		return d; 
	}
	
	public static boolean oiFlagsAndAll(boolean oi){
		return oi; 
	}
	
	public static boolean onFlagsAndAll(boolean on){
		return on; 
	}


	public static boolean unfFlagsAndAll(boolean unf){
		return unf; 
	}
	
	public static boolean hnfFlagsAndAll(boolean hnf){
		return hnf; 
	}
	
	public static boolean tsf1FlagsAndAll(boolean tsf1){
		return tsf1; 
	}
	
	public static boolean tsf2FlagsAndAll(boolean tsf2){
		return tsf2; 
	}
	
	public static boolean afFlagsAndAll(boolean af){
		return af; 
	}
	
	public static boolean rfFlagsAndAll(boolean rf){
		return rf; 
	}
	
	public static boolean oifFlagsAndAll(boolean oif){
		return oif; 
	}
	
	public static boolean onfFlagsAndAll(boolean onf){
		return onf; 
	}
	
	public static String addsTheGoodBits(){	
		if(si == true){
			statement = statement + sessionId;
			stmt.add(sessionId);
			System.out.println("In the add the good bits first part:" + statement);
			checksIfEmpty(statement);
			System.out.println("In the add the good bits first part after checksIfEmpty:" + statement);
			System.out.println(stmt.toString());
		}
		
		if(ui == true){
			statement = statement + userId;
			stmt.add(userId);
			checksIfEmpty(statement);
			System.out.println("In the add the good bits second part after checksIfEmpty:" + statement);
			System.out.println(stmt.toString());
		}
		
		if(un == true){
			statement = statement + userName;
			stmt.add(userName);
			
			checksIfEmpty(statement);
			System.out.println("In the add the good bits third part after checksIfEmpty:" + statement);

			System.out.println(stmt.toString());
		}
		
		if(hn == true){
			statement = statement + hostName;
			stmt.add(hostName);
			
			checksIfEmpty(statement);
			System.out.println("In the add the good bits third part after checksIfEmpty:" + statement);

			System.out.println(stmt.toString());
		}
		if(ts == true){
			statement = statement + timeStamp;
			stmt.add(timeStamp);
			
			checksIfEmpty(statement);
			System.out.println("In the add the good bits third part after checksIfEmpty:" + statement);

			System.out.println(stmt.toString());
		}
		if(a == true){
			statement = statement + action;
			stmt.add(action);
			
			checksIfEmpty(statement);
			System.out.println("In the add the good bits third part after checksIfEmpty:" + statement);

			System.out.println(stmt.toString());
		}
		if(r == true){
			statement = statement + result;
			stmt.add(result);
			
			checksIfEmpty(statement);
			System.out.println("In the add the good bits third part after checksIfEmpty:" + statement);

			System.out.println(stmt.toString());
		}
		if(d == true){
			statement = statement + description;
			stmt.add(description);
			
			checksIfEmpty(statement);
			System.out.println("In the add the good bits third part after checksIfEmpty:" + statement);

			System.out.println(stmt.toString());
		}
		if(oi == true){
			statement = statement + objectId;
			stmt.add(objectId);
			
			checksIfEmpty(statement);
			System.out.println("In the add the good bits third part after checksIfEmpty:" + statement);

			System.out.println(stmt.toString());
		}
		if(on == true){
			statement = statement + objectName;
			stmt.add(objectName);
			
			checksIfEmpty(statement);
			System.out.println("In the add the good bits third part after checksIfEmpty:" + statement);

			System.out.println(stmt.toString());
		}
		
		if(unf == true){
			restOfStmt.add("USER_NAME LIKE '"+userNameField.getText()+"'");
		}else{
			String valueToRemove = "USER_NAME LIKE '"+userNameField.getText()+"'";
			for(int i = 0; i<restOfStmt.size(); i++){
				String val = restOfStmt.get(i);
				if(val == valueToRemove){
					restOfStmt.remove(i);
					break;
				}
			}
		}
		
		if(hnf == true){
			restOfStmt.add("HOST_NAME LIKE '"+hostNameField.getText()+"'");
		}else{
			String valueToRemove = "HOST_NAME LIKE '"+hostNameField.getText()+"'";
			for(int i = 0; i<restOfStmt.size(); i++){
				String val = restOfStmt.get(i);
				if(val == valueToRemove){
					restOfStmt.remove(i);
					break;
				}
			}
		}
		
		if((tsf1 == true)&&(timestampField2.getText() == "")){
			restOfStmt.add("TS LIKE '"+timestampField1+"'");
		}else{
			String valueToRemove = "TS LIKE '"+timestampField1+"'";
			for(int i = 0; i<restOfStmt.size(); i++){
				String val = restOfStmt.get(i);
				if(val == valueToRemove){
					restOfStmt.remove(i);
					break;
				}
			}
		}
		
		if((tsf1 == true)&&(tsf2 == true)){
			restOfStmt.add("TS BETWEEN '"+timestampField1+"' AND '"+timestampField2);
		}else{
			String valueToRemove ="TS BETWEEN '"+timestampField1+"' AND '"+timestampField2;
			for(int i = 0; i<restOfStmt.size(); i++){
				String val = restOfStmt.get(i);
				if(val == valueToRemove){
					restOfStmt.remove(i);
					break;
				}
			}
		}
		
		if(af == true){
			restOfStmt.add("ACTION LIKE '"+actionField.getText()+"'");
		}else{
			String valueToRemove = "ACTION LIKE '"+actionField.getText()+"'";
			for(int i = 0; i<restOfStmt.size(); i++){
				String val = restOfStmt.get(i);
				if(val == valueToRemove){
					restOfStmt.remove(i);
					break;
				}
			}
		}
		String amount = "-1";
		if((rf == true)&&(resultsComboBox.getSelectedItem()!= "Select One")){
			if (resultsComboBox.getSelectedItem() == "TRUE"){
				amount = "1";
				restOfStmt.add("RESULT_SUCCESS LIKE '1'");
			}else if (resultsComboBox.getSelectedItem() == "FALSE"){
				amount = "0";
				restOfStmt.add("RESULT_SUCCESS LIKE '0'");
			}
		}else{
			String valueToRemove = "RESULT_SUCCESS LIKE '"+amount+"'";
			for(int i = 0; i<restOfStmt.size(); i++){
				String val = restOfStmt.get(i);
				if(val == valueToRemove){
					restOfStmt.remove(i);
					break;
				}
			}
		}
		
		if(oif == true){
			restOfStmt.add("OBJECT_ID LIKE '"+objIdField.getText()+"'");
		}else{
			String valueToRemove = "OBJECT_ID LIKE '"+objIdField.getText()+"'";
			for(int i = 0; i<restOfStmt.size(); i++){
				String val = restOfStmt.get(i);
				if(val == valueToRemove){
					restOfStmt.remove(i);
					break;
				}
			}
		}
		
		if(onf == true){
			restOfStmt.add("OBJECT_NAME LIKE '"+objNameField.getText()+"'");
		}else{
			String valueToRemove = "OBJECT_NAME LIKE '"+objNameField.getText()+"'";
			for(int i = 0; i<restOfStmt.size(); i++){
				String val = restOfStmt.get(i);
				if(val == valueToRemove){
					restOfStmt.remove(i);
					break;
				}
			}
		}
		
		
		
		System.out.println(statement);
		System.out.println(stmt.size());
		System.out.println(stmt.toString());
		System.out.println(restOfStmt.toString());
		
		//Add the restOfStmt code here
		
		int countVar = restOfStmt.size();
		int addAnd = 0;
		
		if(countVar > 1){
			while(countVar-1 > addAnd ){
				addAnd++;
				restOfStmt.add(addAnd, "AND");
			}
		}
		
		return statement;
	}
	//NDK This method resets all the check boxes and flags
	public static void reset(){
		restOfStmt.clear();
		stmt.clear();
		statement = "";
		chckbxSessionId.setSelected(false);
		chckbxUserId.setSelected(false);
		chckbxUserName.setSelected(false);
		chckbxHostName.setSelected(false);
		chckbxTimestamp.setSelected(false);
		chckbxAction.setSelected(false);
		chckbxResults.setSelected(false);
		chckbxDescription.setSelected(false);
		chckbxObjectId.setSelected(false);
		chckbxObjectName.setSelected(false);
		chckbxSelectAll.setSelected(false);
		si = false;
		ui = false;
		un = false;
		hn = false;
		ts = false;
		a = false;
		r = false;
		d = false;
		on = false;
		oi = false;
		uiFlagsAndAll(ui);
		siFlagsAndAll(si);
		unFlagsAndAll(un);
		hnFlagsAndAll(hn);
		tsFlagsAndAll(ts);
		aFlagsAndAll(a);
		rFlagsAndAll(r);
		dFlagsAndAll(d);
		oiFlagsAndAll(oi);
		onFlagsAndAll(on);
		userNameField.setEnabled(false);
		hostNameField.setEnabled(false);
		timestampField1.setEnabled(false);
		timestampField2.setEnabled(false);
		actionField.setEnabled(false);
		objIdField.setEnabled(false);
		objNameField.setEnabled(false);
		userNameField.setText("");
		hostNameField.setText("");
		timestampField1.setText("");
		timestampField2.setText("");
		actionField.setText("");
		resultsComboBox.setSelectedIndex(0);
		resultsComboBox.setEnabled(false);
		objIdField.setText("");
		objNameField.setText("");
		unf = false;
		hnf = false;
		tsf1 = false;
		tsf2 = false;
		af = false;
		rf = false;
		onf = false;
		oif = false;
		unfFlagsAndAll(unf);
		hnfFlagsAndAll(hnf);
		tsf1FlagsAndAll(tsf1);
		tsf2FlagsAndAll(tsf2);
		afFlagsAndAll(af);
		rfFlagsAndAll(rf);
		oifFlagsAndAll(oif);
		onfFlagsAndAll(onf);
	}
	
}
	



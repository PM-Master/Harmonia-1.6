package gov.nist.csd.pm.application.appeditor;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import static gov.nist.csd.pm.common.util.Generators.generateRandomName;
import static gov.nist.csd.pm.common.info.PMCommand.GET_ID_OF_ENTITY_WITH_NAME_AND_TYPE;
import static gov.nist.csd.pm.common.info.PMCommand.GET_TEMPLATES;
import static gov.nist.csd.pm.common.info.PMCommand.GET_TEMPLATE_INFO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultCellEditor;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.io.ByteArrayOutputStream;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import gov.nist.csd.pm.admin.PmAdmin;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.browser.PmGraphModel;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.DefaultComboBoxModel;
import com.google.common.base.Throwables;

/*
 * TODO make columns sortable
 */
public class TableEditor extends JFrame {

	private JPanel contentPane;
	private JTextField textField_1;

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

	public static boolean modify = false;
	private static FileWriter fw;
	private String tempName;
	private String tempId;
	private String sessionId;
	private int nSimulatorPort;
	private final int PM_DEFAULT_SIMULATOR_PORT = 8081;
	private SSLSocketClient engineClient;
	private SysCaller sysCaller;
	private String sProcessId;
	private JTable table;
	private static final long serialVersionUID = 1L;
	private DefaultTableModel model; 
	private DefaultTableModel finalModel = new DefaultTableModel();
	private Vector<String> columnVector;
	private ArrayList<ArrayList<String>> permissions, existingPermissions;
	private final JSplitPane splitPane = new JSplitPane();
	protected PmNode schemaNode, baseNode;
	private ArrayList<ArrayList<String>> crtSessPerms;
	private SSLSocketClient sslClient;
	private Object userName;
	private String schemaName;
	private PmGraph tree;
	private PmGraphModel treeModel;
	private Vector<String> columns;
	private JScrollPane scrollPane;
	private JTextField textField;
	private Vector<Vector<String>> data = new Vector<Vector<String>>();
	private static ArrayList<ArrayList<String>> containers = new ArrayList<ArrayList<String>>();
	private static Vector<String> objNames = new Vector<String>();

	/**
	 * Create the frame.
	 */
	public TableEditor(String sessionId, int nSimPort, String sProcId, boolean bDebug) {
		setTitle("Table Editor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 750, 480);

		/*try {
			fw = new FileWriter("C:\\Users\\Administrator.WIN-DNAR5079LMF\\Desktop\\ouput.txt");
		} catch (IOException e2) {
			e2.printStackTrace();
		}*/

		this.sessionId = sessionId;
		nSimulatorPort = (nSimPort < 1024) ? PM_DEFAULT_SIMULATOR_PORT
				: nSimPort;
		sProcessId = sProcId;
		sysCaller = new SysCallerImpl(nSimulatorPort, sessionId, sProcId, bDebug, "SB");
		permissions = new ArrayList<ArrayList<String>>();
		existingPermissions = new ArrayList<ArrayList<String>>();
		crtSessPerms = new ArrayList<ArrayList<String>>();
		columnVector = new Vector<String>();
		columns = new Vector<String>();
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

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.4);
		contentPane.add(splitPane, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Schema", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));


		scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.SOUTH);

		//final String sess = sessionId;
		//final int sim = nSimPort;
		//final String sProc = sProcId;
		//final boolean bdeb = bDebug; 

		JButton btnAddEntry = new JButton("Add Entry");
		btnAddEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//addEntry();
				addEntry();
				//EmployeeRecord rec = new EmployeeRecord(sess, sim, sProc, bdeb);
				//rec.setVisible(true);
			}
		});

		JButton btnRefreshTable = new JButton("Refresh Table");
		btnRefreshTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openSchema(false);
			}
		});
		panel_2.add(btnRefreshTable);
		panel_2.add(btnAddEntry);

		JButton btnEditEntry = new JButton("Edit Entry");
		btnEditEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO get the selected row and search through all employee ID records
				//using props.load(bais), if ID prop equals ID in ID column then get the sObjName and EMPREC.dispLayRecord(sObjName)
				editEntry();
			}
		});
		panel_2.add(btnEditEntry);

		JButton btnDeleteEntry = new JButton("Delete Entry");
		panel_2.add(btnDeleteEntry);

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_1.add(scrollPane_1, BorderLayout.CENTER);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lblColumnName = new JLabel("Column Name:");
		panel_3.add(lblColumnName);

		textField = new JTextField();
		panel_3.add(textField);
		textField.setColumns(10);

		JLabel lblValue = new JLabel("Value:");
		panel_3.add(lblValue);

		textField_1 = new JTextField();
		panel_3.add(textField_1);
		textField_1.setColumns(10);

		JButton btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				search(textField.getText(), textField_1.getText());
			}

		});
		panel_3.add(btnSearch);

		if(!openSchema(true)){
			return;
		}
		//Collections.reverse(columns);
		model = new DefaultTableModel(columnVector, 0){
			@Override
			public boolean isCellEditable(int row, int col){
				return false;
			}
		};
		populateTable(data);
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//table.setCellSelectionEnabled(true);
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setReorderingAllowed(false);
		scrollPane_1.setViewportView(table);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		for(int i = 0; i < columnVector.size(); i++){
			table.getColumnModel().getColumn(i).setPreferredWidth(columns.get(i).length()+100);
		}

		JPopupMenu popmenu = new JPopupMenu();
		JMenuItem view = new JMenuItem("Open");
		view.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				int col = table.getSelectedColumn();
				Vector x = (Vector) model.getDataVector().elementAt(row);
				String name = (String) x.get(col);

				Vector<String> cols = new Vector();
				String id = getIdOfEntity("b", name);
				if(id == null){
					JOptionPane.showMessageDialog(TableEditor.this, "Invalid Contianer");
					return;
				}
				List<String[]> members = getMembersOf("b", id, name, "ac");
				for(int i = 0; i < members.size(); i++){
					if(members.get(i)[0].equals("b")){
						cols.add(members.get(i)[2]);
					}
				}
				new TableViewer(cols, data, TableEditor.this);

				//JOptionPane.showMessageDialog(SchemaEditor3.this, line, column, JOptionPane.INFORMATION_MESSAGE);
			}
		});
		popmenu.add(view);
		table.setComponentPopupMenu(popmenu);

		setVisible(true);
	}

	private String getCrtUser(){
		String line = getUserId();
		log(line);
		String[] pieces = line.split(":");
		name = pieces[0];
		String userId = pieces[1];
		String name = getUserFullName(userId);
		log(name);
		return name;
	}
	private String name;
	String retUserLogon(){
		return name;
	}

	/**
	 * @return the userName
	 */
	public Object getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(Object userName) {
		this.userName = userName;
	}

	private String getUserFullName(String sId){
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

	public static void log(Object input){
		/*try{
			fw.write(input + "\r\n");
			fw.flush();
		}catch(IOException e){
			e.printStackTrace();
		}*/
	}

	/**
	 * @param nCode
	 */
	private void terminate(int nCode) {
		sysCaller.exitProcess(sProcessId);
		System.exit(nCode);
	}

	public void addEntry(){
		//TODO gets the columns and puts them into a window with labels and fields for each column
		//EntryEditor editor = new EntryEditor();
		if(tempName.equals("Employee Record")){
			invokeEmployeeRecord();
		}
	}

	private void editEntry(){
		if(tempName.equals("Employee_Record")){
			openEmployeeRecord();
		}
	}

	private void openEmployeeRecord(){
		int row = table.getSelectedRow();
		String sObjName = objNames.get(row);
		String sContId = getIdOfEntity(PM_NODE_OATTR, sObjName);
		EmployeeRecord rec = new EmployeeRecord(sessionId, nSimulatorPort, sProcessId, false);
		rec.displayRecord(sObjName, sContId);
		rec.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		rec.setVisible(true);
	}

	private void invokeEmployeeRecord(){
		EmployeeRecord rec = new EmployeeRecord(sessionId, nSimulatorPort, sProcessId, false);
		rec.setVisible(true);
	}

	public void search(String column, String value){//TODO DOESNT WORK
		if(column.length() == 0 || column == null){
			JOptionPane.showMessageDialog(this, 
					"You must specify a valid column name.");
			return;
		}

		if(value.length() == 0 || value == null){
			JOptionPane.showMessageDialog(this, 
					"You must specify a valid value.");
			return;
		}
		log(value);
		Vector<Vector<String>> data = model.getDataVector();
		int col = columns.indexOf(column);
		Vector<Vector<String>> vals = new Vector<Vector<String>>();
		for(int i = 0; i < data.size(); i++){
			String val = data.elementAt(i).elementAt(col);
			log("VAL: " + val);
			if(val.equals(value)){
				log("match");
				//model.addRow(data.elementAt(i));
				vals.add(data.get(i));
			}
		}
		new TableViewer(columns, vals, this);
	}

	private Properties getSessionRecord(){//FIXME
		Properties ret = new Properties();
		ArrayList<String> x = new ArrayList<String>();
		String sContainerName = sysCaller.getNameOfContainerWithProperty("Employee=" + retUserLogon());// TODO CANT SAY EMPLOYEE
		log("Container: " + sContainerName);
		String sId = getIdOfEntity("b", sContainerName);
		if(sId == null){
			return null;
		}
		List<String[]> members = getMembersOf("b", sId, sContainerName, "ac");

		//for(String[] mem : members){
		for(int i = 0; i < members.size(); i++){
			String[] mem = members.get(i);
			String objName = mem[2];
			List<String[]> recmems = getMembersOf("b", mem[1], objName, "ac");
			for(int j = 0; j < recmems.size(); j++){
				objName = recmems.get(j)[2];
				String col = columnVector.get(j);
				String sHandle = sysCaller.openObject3(objName, "File read");
				byte[] buf = sysCaller.readObject3(sHandle);
				if(buf == null){
					log("buff is nul");
					continue;
				}

				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				Properties props = new Properties();
				for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
					String sName = (String) propEnum.nextElement();
					log("Prop: " + sName + "=" + (String) props.get(sName));
				}

				try {
					props.load(bais);
					//col = col.trim().replaceAll(" ", "_");
					String value = props.getProperty(col);
					log("col" + col);
					ret.put(col, value);
					x.add(value);
				}catch(Exception ex){
					ret.put(col, "");
					log("exception in loading");
					ex.printStackTrace();
				}		
			}
		}
		log("REC: " + x);
		return ret;
	}

	public List<String[]> getMembersOf(String sType, String sId, String sLabel, String sGraphType) {
		Packet res = null;
		try {
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

	private Packet requestTemplate(){
		setUserName(getCrtUser());
		log("USERNAME: " + getUserName());

		ArrayList<String> tplList = new ArrayList<String>();
		ArrayList<String> tplNames = new ArrayList<String>();

		JComboBox tableBox = new JComboBox();
		tableBox.setPreferredSize(new Dimension(160, 20));

		//String sId = getIdOfEntity("p", "Schema Builder");
		//List<String[]> tableList = getMembersOf("p", sId, "Schema Builder", "ac");
		Packet res = sysCaller.getTemplates();
		if (res == null) {
			JOptionPane.showMessageDialog(this,
					"Null result from getTemplateInfo!");
			//resetAll();
			return null;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, "Could not get templates");
			//resetAll();
			return null;
		}
		//DefaultComboBoxModel tableBoxModel = (DefaultComboBoxModel) tableBox.getModel();
		//tableBoxModel.removeAllElements();
		//		for(int i = 0; i < tableList.size(); i++){
		//			String type = tableList.get(i)[0];
		//			String id = tableList.get(i)[1];
		//			String tableName = tableList.get(i)[2];
		//
		//			ArrayList<String> attrInfo = getAttrInfo(id);
		//			ArrayList<String> props = new ArrayList<String>();
		//			for(int j = 3; j < attrInfo.size(); j++){
		//				props.add(attrInfo.get(j));
		//			}
		//			/*if(!props.contains("Creator=" + getUserName())){
		//				tableList.remove(tableName);
		//			}else{*/
		//				if(type.equals("b")){
		//					tableBoxModel.addElement(tableName);
		//				}else{
		//					tableList.remove(tableName);
		//				}
		//			//}
		//		}
		DefaultComboBoxModel tableBoxModel = (DefaultComboBoxModel)tableBox.getModel();
		tableBoxModel.removeAllElements();
		for (int i = 0; i < res.size(); i++) {
			String sLine = res.getStringValue(i);
			String[] pieces = sLine.split(PM_FIELD_DELIM);

			tableBoxModel.addElement(pieces[0]);
		}


		int ret = JOptionPane.showOptionDialog(this, new Object[]
				{"Select a table to open.", tableBox}, "Select a Table", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null);
		if(ret != JOptionPane.OK_OPTION){
			return null;
		}

		int sel = tableBox.getSelectedIndex();
		if(sel < 0){
			JOptionPane.showMessageDialog(this, "Please select a table to open");
		}

		res = getTemplates();
		if (res == null) {
			JOptionPane.showMessageDialog(this,
					"Null result from getTemplateInfo!");
			//resetAll();
			return null;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, "Could not get templates");
			//resetAll();
			return null;
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

		for(int i = 0; i < tplNames.size(); i++){
			if(tplNames.get(i).equals(tempName)){
				tempId = tplList.get(i);
			}
		}
		log(tempName + ":" + tempId);
		/*
		res = getTemplateInfo(tempId);
		if (res == null) {
			JOptionPane.showMessageDialog(this,
					"Null result from getTemplateInfo!");
			return null;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, "Could not open table");
			//resetAll();
			return null;
		}*/
		return res;
	}

	public boolean openSchema(boolean requestTemplate){
		Packet res;
		ArrayList<String> conts = new ArrayList<String>();
		if(requestTemplate){
			res = requestTemplate();
			if(res == null){
				return false;
			}
			if(res.hasError()){
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return false;
			}
			res = getTemplateInfo(tempId);
			String sLine = res.getStringValue(1);
			String[] pieces = sLine.split(":");
			conts = new ArrayList<String>(Arrays.asList(pieces));
			log("CONTS1: " + conts);
		}else{
			res = getTemplateInfo(tempId);
			String sLine = res.getStringValue(1);
			String[] pieces = sLine.split(":");
			conts = new ArrayList<String>(Arrays.asList(pieces));
			log("CONTS2: " + conts);
		}
		String sId = getIdOfEntity("b", tempName);
		ArrayList<String> attrInfo = getAttrInfo(sId);
		log(attrInfo);

		String s = attrInfo.get(0);
		String [] pieces = s.split(PM_FIELD_DELIM);
		schemaName = pieces[0];

		String tId = pieces[1];

		String prop = "";
		for (int i = 3; i < attrInfo.size()-1; i++) {
			prop += attrInfo.get(i) + ", ";
			log("PROP: " + prop);

		}
		prop += attrInfo.get(attrInfo.size()-1);
		for(int i = 0; i < conts.size(); i++){
			String colId = conts.get(i);
			
			log(colId);
			
			attrInfo = getAttrInfo(colId);
			if(attrInfo == null){
				colId = getIdOfEntity("b", colId);
				attrInfo = getAttrInfo(colId);
			}

			String line = attrInfo.get(0);
			pieces = line.split(":");
			String col = pieces[0];
			columnVector.add(col);

			//addToPerms(colId, schemaName, col, "b");
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

		}
		log("\r\n\r\n\r\n");
		for(ArrayList<String> line : permissions){
			log("PERMISSIONS" + line);
		}
		log("\r\n\r\n\r\n");
		//containers.remove(0);
		//edit = true;
		List<String[]> members = getMembersOf("b", tId, schemaName, "ac");
		for(String[] mem : members){
			if(mem[0].equals("b")){
				columns.add(mem[2]);
			}
		}
		log(columnVector);
		for(int i = 0; i < columnVector.size(); i++){
			String col = columnVector.get(i);
			String id = getIdOfEntity("b", col);
			List<String[]> objects = getMembersOf("b", id, col, "ac");
			log("objects: " + objects);
			Vector<String> colObjects = new Vector<String>();
			for(int j = 0; j < objects.size(); j++){
				log(objects.get(j)[2]);
			}
			for(int j = 0; j < objects.size(); j++){
				if(objects.get(j)[0].equals("o")){
					String sObjName = objects.get(j)[2];
					String sHandle = sysCaller.openObject3(sObjName, "File read");
					byte[] buf = sysCaller.readObject3(sHandle);
					if(buf == null){
						//sObjValue = "********";
						colObjects.add("");
						//String emprec = sysCaller.getNameOfContainerWithProperty("employee=" + userName);
						/*Properties p = getSessionRecord();
						if(p == null){
							colObjects.add("");
						}else{
							String value = p.getProperty(col);
							log(col);
							log("GETTING SESSION RECORD - " + value);
							colObjects.add(value);
							if(value == null || value.equals("")){
								colObjects.add("");
							}
						}
						log("buff is nul");*/
						continue;
					}

					ByteArrayInputStream bais = new ByteArrayInputStream(buf);
					Properties props = new Properties();
					for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
						String sName = (String) propEnum.nextElement();
						log("Prop: " + sName + "=" + (String) props.get(sName));
					}

					try {
						props.load(bais);
						//col = col.trim().replaceAll(" ", "_");
						String value = props.getProperty(col);
						log("col" + col);
						colObjects.add(value);
					}catch(Exception ex){
						log("exception in loading");
						ex.printStackTrace();
						System.out.println("properties did not load for " + res);
						//return false;
					}
				}
			}
			log("VVVDDVDV" + colObjects);
			data.add(colObjects);
		}
		return true;
	}
	public void populateTable(Vector<Vector<String>> data){
		log("DATA: " + data);
		Vector<Vector<String>> rows = new Vector<Vector<String>>();	
		int dataSize = data.get(0).size();
		for(int i = 0; i < dataSize; i++){
			Vector<String> row = new Vector<String>();
			for(int j = 0; j < data.size(); j++){
				row.add(data.get(j).get(0));
				data.get(j).remove(0);
			}
			rows.add(row);
			log("ROW: " + row);
			row = null;
		}
		log("ROWS: " + rows);
		for(int i = 0; i < rows.size(); i++){
			log(rows.get(i));
			//Collections.reverse(rows.get(i));
			model.addRow(rows.get(i));
		}
		//model.setRowCount(rows.size());

		resetSchemaView();
	}

	private void resetSchemaView(){
		schemaNode = PmNode.createObjectAttributeNode(schemaName);
		tree = new PmGraph(schemaNode, true);
		//tree.addMouseListener(new SchemaMouseListener());
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
				//log("Base: " + baseNode.getName() + " baseID: " + baseNode.getId());
			}
			existingNodes.add(baseNode);
			PmNode oNode = PmNode.createObjectAttributeNode(nodeName);
			PmNode.linkNodes(baseNode, oNode);
			existingNodes.add(oNode);
			log("linked " + nodeName + " to base " + base);
		}
		tree.setShowsRootHandles(true);
		treeModel = new PmGraphModel(schemaNode);

		scrollPane.setViewportView(tree);
	}

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

	private ArrayList<String> getAttrInfo(String sId) {
		try{
			Packet cmd = makeCmd("getAttrInfo", sId, PM_NODE_OATTR, "no");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				//JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
				return null;
			}
			if (res.hasError()) {
				//JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return null;
			}
			ArrayList<String> ret = new ArrayList<String>();
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
			return ret;
		} catch (Exception e) {
			//e.printStackTrace();
			//JOptionPane.showMessageDialog(this, e.getMessage());
			return null;
		}
	}

	public String getIdOfEntity(String sType, String sName){
		log("sType: " + sType + " sName: " + sName);
		try{
			Packet cmd = makeCmd(GET_ID_OF_ENTITY_WITH_NAME_AND_TYPE, sName, sType);
			Packet res = engineClient.sendReceive(cmd, null);
			if(res.hasError()){
				log(res.getErrorMessage());
				return null;
			}
			log(res.getStringValue(0));
			return res.getStringValue(0);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

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
	public static void createGUI(){
		new TableEditor(sessid, simport, pid, debug);
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

	class EntryEditor extends JFrame{
		public EntryEditor(){
			getContentPane().setLayout(new GridLayout(columns.size()+1, 2));
			setTitle("Entry Editor");
			ArrayList<JLabel> labels = new ArrayList<JLabel>();
			ArrayList<JTextField> fields = new ArrayList<JTextField>();
			for(int i = 0; i < columns.size(); i++){
				JLabel j = new JLabel(columns.get(i));
				labels.add(j);
				getContentPane().add(j);

				JTextField t = new JTextField();
				fields.add(t);
				getContentPane().add(t);
			}

			final ArrayList<JTextField> finalFields = fields;

			JButton reset = new JButton("Reset");
			reset.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					for(int i = 0; i < finalFields.size(); i++){
						finalFields.get(i).setText("");
					}
				}

			});

			JButton submit = new JButton("Submit");
			submit.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					Vector<String> entries = new Vector<String>();
					//TODO might want to loop through the finalFields arraylist instead
					/*for(int j = 0; j < columns.size(); j++){
						String sObjName = generateRandomName(4);
						String sObjClass = "File";
						String sObjType = "rtf";
						String sContainer = "b|" + columns.get(j);
						String sPerms = "File write";

						String sHandle = sysCaller.createObject3(sObjName, sObjClass, sObjType, sContainer, sPerms, 
								null, null, null, null);
						if(sHandle == null){
							JOptionPane.showMessageDialog(TableEditor.this, "Error in creating object " + sObjName 
									+ " in container " + sContainer , "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						//Set properties for the object and then write it
						Properties props = new Properties();
						String value = finalFields.get(j).getText();
						String colName = columns.get(j);
						props.put(colName, value);
						entries.add(value);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						PrintWriter pw = new PrintWriter(baos, true);

						try{
							props.store(baos, null);
							pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}\n");
							pw.print(colName + ": " + value);
							pw.close();
							byte[] buf = baos.toByteArray();
							int len = sysCaller.writeObject3(sHandle,  buf);
							if(len < 0){
								JOptionPane.showMessageDialog(TableEditor.this, sysCaller.getLastError() , "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}

							sysCaller.closeObject(sHandle);
						}catch(Exception e){
							e.printStackTrace();
							return;
						}
					}

					//create record
					String sObjName = generateRandomName(4);
					String sObjClass = "File";
					String sObjType = "rtf";
					String sContainer = "b|Employee Records";
					String sPerms = "File write";

					String sHandle = sysCaller.createObject3(sObjName, sObjClass, sObjType, sContainer, sPerms, 
							null, null, null, null);
					if(sHandle == null){
						JOptionPane.showMessageDialog(TableEditor.this, "Error in creating object " + sObjName 
								+ " in container " + sContainer , "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Properties props = new Properties();
					for(int i = 0; i < columns.size(); i++){
						String colName = columns.get(i);
						String value = entries.get(i);
						props.put(colName, value);
						//entries.add(value);
					}

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(baos, true);

					try{
						props.store(baos, null);
						pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}\n");

						for(int i = 0; i < columns.size(); i++){
							String colName = columns.get(i);
							String value = entries.get(i);
							//props.put(colName, value);
							//entries.add(value);
							pw.print(colName + ": " + value + " \\par ");
						}

						pw.close();
						byte[] buf = baos.toByteArray();
						if(buf == null){
							JOptionPane.showMessageDialog(TableEditor.this, "NO");
							return;
						}
						int len = sysCaller.writeObject3(sHandle,  buf);
						if(len < 0){
							JOptionPane.showMessageDialog(TableEditor.this, sysCaller.getLastError() , "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						sysCaller.closeObject(sHandle);
					}catch(Exception e){
						e.printStackTrace();
						return;
					}
					//}*/					
					HashMap keyMap = new HashMap();
					String sComponents = "";
					for(int i = 0; i < columns.size(); i++){
						String value = finalFields.get(i).getText();
						entries.add(value);
						keyMap.put(columns.get(i), value);
						String colName = columns.get(i);
						String compName = generateRandomName(4);
						String sHandle = sysCaller.createObject3(compName, "File", "rtf",
								"b|" + colName, "File write", null, null, null, null);
						if (sHandle == null) {
							JOptionPane.showMessageDialog(null, sysCaller.getLastError());
							return;
						}
						sComponents += compName + SysCaller.PM_FIELD_DELIM;

						Properties props = new Properties();
						props.put(colName, value);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						PrintWriter pw = new PrintWriter(baos, true);

						try{
							props.store(baos, null);
							pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}\n");
							pw.print(colName + ": " + value);
							pw.close();
							byte[] buf = baos.toByteArray();
							if(buf == null){
								JOptionPane.showMessageDialog(TableEditor.this, "NO");
								return;
							}
							int len = sysCaller.writeObject3(sHandle,  buf);
							if(len < 0){
								JOptionPane.showMessageDialog(TableEditor.this, sysCaller.getLastError() , "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}

							sysCaller.closeObject(sHandle);
						}catch(Exception e){
							e.printStackTrace();
							return;
						}
					}
					//TODO CREATERECORDINENTITYWITHPROP - prop -> employee = user name
					String sContainerName = generateRandomName(4);
					getIdOfEntity("b", "Employee Records");
					Packet res = sysCaller.createRecordInEntityWithProp(sContainerName, 
							"Employee Records", SysCaller.PM_NODE_OATTR, //TODO dont say employee record, need variable so it works with anything
							tempId, sComponents, keyMap);
					log("Template Id:  " + tempId);
					if (res.hasError()) {
						JOptionPane.showMessageDialog(null, res.getErrorMessage());
						return;
					}
					JOptionPane.showMessageDialog(TableEditor.this, "Entry submitted succesfully");
					setVisible(false);
					log("ENTRIES: " + entries);
					model.addRow(entries);
				}
			});

			getContentPane().add(reset);
			getContentPane().add(submit);
			pack();
			setResizable(false);
			setLocationRelativeTo(null);
			setVisible(true);
		}
	}
}


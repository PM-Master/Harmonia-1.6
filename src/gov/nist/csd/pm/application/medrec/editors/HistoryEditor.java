package gov.nist.csd.pm.application.medrec.editors;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.net.Packet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import static gov.nist.csd.pm.common.util.Generators.generateRandomName;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
/**
 * 
 * @author joshua.roberts@nist.gov
 *
 */
public class HistoryEditor extends Editor implements ActionListener{

	private List<String> newResults = new ArrayList<String>();
	public ArrayList<String> found = new ArrayList<String>();

	public SysCaller syscaller;
	public SysCallerImpl syscallerimpl;

	public String sSessionId;
	public String sProcessId;

	private String sKstorePath;
	private String sTstorePath;
	private String sRtfPath;
	private String sWkfPath;
	private String sEmlPath;
	private String sOffPath;

	private JRadioButton butM;
	private JRadioButton butF;
	protected JEditorPane myEditorPane;
	public JTextField mrnField;
	private JTextField ssnField;
	private JTextField firstField;
	private JTextField midField;
	private JTextField lastField;
	private JTextField dobField;
	private JButton btnSubmit, btnSave, btnDone;
	private JTextField doctField;
	private JTextField dateField;
	public DefaultListModel resultListModel;
	private SearchEngine search;
	private JButton btnSearch;
	private String mRecMrn;
	private JButton btnNewButton;

	public HistoryEditor(String theMrn, int nSimPort, String sSessId, String sProcId, boolean bDebug) {
		setTitle("History Editor");

		setBounds(100, 100, 450, 674);
		setSize(450, 480);

		this.sSessionId = sSessId;
		this.sProcessId = sProcId;
		mRecMrn = theMrn;
		syscaller = new SysCallerImpl(nSimPort, sSessId, sProcId, bDebug, "History Editor");

		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);

		JLabel lblDate = new JLabel("Date:");
		getContentPane().add(lblDate);

		dateField = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, dateField, 130, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, dateField, -130, SpringLayout.EAST, getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, lblDate, 3, SpringLayout.NORTH, dateField);
		springLayout.putConstraint(SpringLayout.EAST, lblDate, -6, SpringLayout.WEST, dateField);
		getContentPane().add(dateField);
		dateField.setEditable(false);
		dateField.setColumns(22);

		JLabel lblMrn = new JLabel("HN:");
		springLayout.putConstraint(SpringLayout.NORTH, lblMrn, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, lblMrn, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(lblMrn);

		mrnField = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, mrnField, 7, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, mrnField, 6, SpringLayout.EAST, lblMrn);
		springLayout.putConstraint(SpringLayout.NORTH, dateField, 6, SpringLayout.SOUTH, mrnField);
		getContentPane().add(mrnField);
		mrnField.setEditable(false);
		mrnField.setColumns(10);

		JPanel panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, panel, 3, SpringLayout.SOUTH, dateField);
		springLayout.putConstraint(SpringLayout.WEST, panel, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel, 432, SpringLayout.WEST, getContentPane());
		getContentPane().add(panel);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0),
				BorderFactory.createTitledBorder("Patient Identification:")));
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);

		JLabel lblFirstName = new JLabel("First Name:");
		panel.add(lblFirstName);

		firstField = new JTextField();
		firstField.setEditable(false);
		sl_panel.putConstraint(SpringLayout.NORTH, firstField, 20, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblFirstName, 0, SpringLayout.WEST, firstField);
		sl_panel.putConstraint(SpringLayout.SOUTH, lblFirstName, -6, SpringLayout.NORTH, firstField);
		panel.add(firstField);
		firstField.setColumns(10);

		JLabel lblMi = new JLabel("MI:");
		sl_panel.putConstraint(SpringLayout.NORTH, lblMi, 0, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.WEST, lblMi, 190, SpringLayout.WEST, panel);
		panel.add(lblMi);

		midField = new JTextField();
		midField.setEditable(false);
		sl_panel.putConstraint(SpringLayout.NORTH, midField, 6, SpringLayout.SOUTH, lblMi);
		sl_panel.putConstraint(SpringLayout.WEST, midField, 34, SpringLayout.EAST, firstField);
		panel.add(midField);
		midField.setColumns(10);

		JLabel lblLastName = new JLabel("Last Name:");
		sl_panel.putConstraint(SpringLayout.NORTH, lblLastName, 0, SpringLayout.NORTH, panel);
		panel.add(lblLastName);

		lastField = new JTextField();
		lastField.setEditable(false);
		sl_panel.putConstraint(SpringLayout.NORTH, lastField, 6, SpringLayout.SOUTH, lblLastName);
		sl_panel.putConstraint(SpringLayout.WEST, lastField, 256, SpringLayout.WEST, panel);
		sl_panel.putConstraint(SpringLayout.EAST, midField, -30, SpringLayout.WEST, lastField);
		sl_panel.putConstraint(SpringLayout.WEST, lblLastName, 0, SpringLayout.WEST, lastField);
		panel.add(lastField);
		lastField.setColumns(10);

		JLabel lblSsn = new JLabel("SSN:");
		sl_panel.putConstraint(SpringLayout.NORTH, lblSsn, 6, SpringLayout.SOUTH, firstField);
		sl_panel.putConstraint(SpringLayout.WEST, firstField, 0, SpringLayout.WEST, lblSsn);
		panel.add(lblSsn);

		ssnField = new JTextField();
		ssnField.setEditable(false);
		sl_panel.putConstraint(SpringLayout.WEST, lblSsn, 0, SpringLayout.WEST, ssnField);
		panel.add(ssnField);
		ssnField.setColumns(10);

		JLabel lblSex = new JLabel("Sex:");
		sl_panel.putConstraint(SpringLayout.NORTH, lblSex, 0, SpringLayout.NORTH, lblSsn);
		panel.add(lblSex);

		butM = new JRadioButton("M");
		butM.setEnabled(false);
		sl_panel.putConstraint(SpringLayout.NORTH, butM, 1, SpringLayout.SOUTH, lblSex);
		sl_panel.putConstraint(SpringLayout.WEST, lblSex, 0, SpringLayout.WEST, butM);
		sl_panel.putConstraint(SpringLayout.NORTH, ssnField, 1, SpringLayout.NORTH, butM);
		sl_panel.putConstraint(SpringLayout.EAST, ssnField, -17, SpringLayout.WEST, butM);
		sl_panel.putConstraint(SpringLayout.WEST, butM, 173, SpringLayout.WEST, panel);
		butM.setActionCommand("M");
		panel.add(butM);

		butF = new JRadioButton("F");
		butF.setEnabled(false);
		sl_panel.putConstraint(SpringLayout.NORTH, butF, 21, SpringLayout.SOUTH, midField);
		sl_panel.putConstraint(SpringLayout.WEST, butF, 6, SpringLayout.EAST, butM);
		butF.setActionCommand("F");
		panel.add(butF);

		ButtonGroup bg1 = new ButtonGroup();
		bg1.add(butM);
		bg1.add(butF);

		JLabel lblDob = new JLabel("DOB:");
		sl_panel.putConstraint(SpringLayout.NORTH, lblDob, 6, SpringLayout.SOUTH, lastField);
		sl_panel.putConstraint(SpringLayout.WEST, lblDob, 0, SpringLayout.WEST, lblLastName);
		panel.add(lblDob);

		dobField = new JTextField();
		dobField.setEditable(false);
		sl_panel.putConstraint(SpringLayout.NORTH, dobField, 1, SpringLayout.SOUTH, lblDob);
		sl_panel.putConstraint(SpringLayout.WEST, dobField, 0, SpringLayout.WEST, lblLastName);
		panel.add(dobField);
		dobField.setColumns(10);

		JPanel panel_1 = new JPanel();
		springLayout.putConstraint(SpringLayout.SOUTH, panel, -6, SpringLayout.NORTH, panel_1);
		springLayout.putConstraint(SpringLayout.NORTH, panel_1, 198, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, panel_1, 10, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, panel_1, 0, SpringLayout.EAST, panel);
		getContentPane().add(panel_1);
		panel_1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0),
				BorderFactory.createTitledBorder("History:")));
		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);

		btnSave = new JButton(getSaveDraftAlt());
		btnSave.setActionCommand("Save");
		panel_1.add(btnSave);

		btnSubmit = new JButton(getSubmitToHistory());
		sl_panel_1.putConstraint(SpringLayout.NORTH, btnSave, 0, SpringLayout.NORTH, btnSubmit);
		sl_panel_1.putConstraint(SpringLayout.EAST, btnSave, -7, SpringLayout.WEST, btnSubmit);
		sl_panel_1.putConstraint(SpringLayout.WEST, btnSubmit, 232, SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, btnSubmit, -10, SpringLayout.EAST, panel_1);
		btnSubmit.setEnabled(false);
		sl_panel_1.putConstraint(SpringLayout.NORTH, btnSubmit, 147, SpringLayout.NORTH, panel_1);
		btnSubmit.setActionCommand("Submit");
		btnSubmit.setEnabled(true);
		panel_1.add(btnSubmit);

		myEditorPane = new JEditorPane();
		sl_panel_1.putConstraint(SpringLayout.NORTH, myEditorPane, 0, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, myEditorPane, 10, SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, myEditorPane, 135, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, myEditorPane, 400, SpringLayout.WEST, panel_1);
		panel_1.add(myEditorPane);

		btnNewButton = new JButton("New");
		sl_panel_1.putConstraint(SpringLayout.WEST, btnSave, 6, SpringLayout.EAST, btnNewButton);
		sl_panel_1.putConstraint(SpringLayout.NORTH, btnNewButton, 12, SpringLayout.SOUTH, myEditorPane);
		sl_panel_1.putConstraint(SpringLayout.WEST, btnNewButton, 0, SpringLayout.WEST, myEditorPane);
		panel_1.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dateField.setText("");
				mrnField.setText("");
				myEditorPane.setText("");

				btnSubmit.setEnabled(true);
				btnSave.setEnabled(true);
			}

		});
		JLabel lblDoctor = new JLabel("Doctor:");
		springLayout.putConstraint(SpringLayout.NORTH, lblDoctor, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, lblDoctor, 238, SpringLayout.WEST, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, mrnField, -6, SpringLayout.WEST, lblDoctor);
		getContentPane().add(lblDoctor);

		doctField = new JTextField();
		springLayout.putConstraint(SpringLayout.WEST, doctField, 6, SpringLayout.EAST, lblDoctor);
		springLayout.putConstraint(SpringLayout.EAST, doctField, 0, SpringLayout.EAST, panel);
		doctField.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, doctField, 7, SpringLayout.NORTH, getContentPane());
		getContentPane().add(doctField);
		doctField.setColumns(10);

		btnDone = new JButton("Done");
		springLayout.putConstraint(SpringLayout.SOUTH, panel_1, -6, SpringLayout.NORTH, btnDone);
		springLayout.putConstraint(SpringLayout.SOUTH, btnDone, -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnDone, 0, SpringLayout.EAST, panel);
		btnDone.setActionCommand("Done");
		btnDone.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}

		});
		getContentPane().add(btnDone);

		setResizable(false);

		btnSearch = new JButton("Search");
		springLayout.putConstraint(SpringLayout.WEST, btnSearch, 0, SpringLayout.WEST, lblMrn);
		springLayout.putConstraint(SpringLayout.SOUTH, btnSearch, 0, SpringLayout.SOUTH, btnDone);
		btnSearch.setActionCommand("search");
		btnSearch.addActionListener(this);
		getContentPane().add(btnSearch);

		btnViewAllHistory = new JButton("View All History");
		springLayout.putConstraint(SpringLayout.NORTH, btnViewAllHistory, 6, SpringLayout.SOUTH, panel_1);
		springLayout.putConstraint(SpringLayout.WEST, btnViewAllHistory, 6, SpringLayout.EAST, btnSearch);
		btnViewAllHistory.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		//getContentPane().add(btnViewAllHistory);

		Packet res = syscaller.getKStorePaths();
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}
		sKstorePath = res.getStringValue(0);
		sTstorePath = res.getStringValue(1);
		System.out.println("Kstore path = " + sKstorePath);
		System.out.println("Tstore path = " + sTstorePath);

		sRtfPath = syscaller.getAppPath("Rich Text Editor")[0];
		sWkfPath = syscaller.getAppPath("Workflow Editor")[0];
		sEmlPath = syscaller.getAppPath("e-grant")[0];
		sOffPath = syscaller.getAppPath("Open Office")[0];

		System.out.println("RTF path = " + sRtfPath);
		System.out.println("Wkf path = " + sWkfPath);
		System.out.println("Eml path = " + sEmlPath);
		System.out.println("Off path = " + sOffPath);
		
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		buildReadAllDocument(new ReadAllDocument("History", this)).setVisible(true);
	}
	
	public ReadAllDocument buildReadAllDocument(ReadAllDocument reader){
		//ReadAllDocument reader = new ReadAllDocument("History", HistoryEditor.this);
		reader.setTitle("History");
		System.out.println("History");
		
		String container = mRecMrn + "-History"; 
		
		String id1 = syscaller.getIdOfEntityWithNameAndType(container, "b");
		String[] results;
		List<String> newResults = new ArrayList<String>();
		String sType = "b";
		String sGraphType = "ac";
		List<String[]> members = syscaller.getMembersOf(container, id1, sType, sGraphType);
		System.out.println("before getting history");
		for(int i = 0; i < members.size(); i++){
			results = members.get(i);
			System.out.println("member " + i + ":" + results[0] + ":" + results[1] + ":" + results[2]);
			if(results[0].equals("o") && results[2].length() == 8){
				newResults.add(results[2]);
			}
		}

		for(int j = 0; j < newResults.size(); j++){
			System.out.println("newResults: " + newResults.get(j));
			String sObjName = newResults.get(j);
			String sHandle = syscaller.openObject3(sObjName, "File read");
			if(sHandle == null){
				/*JOptionPane.showMessageDialog(HistoryEditor.this,
						syscaller.getLastError());
				System.out.println("sHandle was null");*/
				continue;
			}
			byte[] buf = syscaller.readObject3(sHandle);
			if(buf == null){
				/*JOptionPane.showMessageDialog(HistoryEditor.this,
						syscaller.getLastError());
				System.out.println("sHandle was null");*/
				continue;
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
			Properties props = new Properties();

			try {
				System.out.println("about to load " + sObjName);
				props.load(bais);
				System.out.println("loaded properties for " + sObjName);
			}catch(Exception ex){
				JOptionPane.showMessageDialog(HistoryEditor.this,
						syscaller.getLastError());
				System.out.println("properties did not load for " + sObjName);
				ex.printStackTrace();
				continue;
			}
			String hn = props.getProperty("hn");
			String date = props.getProperty("date");
			String doctor = props.getProperty("doctor");
			String patient = props.getProperty("name");
			String hist = props.getProperty("History");

			Scanner sc = new Scanner(hist);
			String fHist = "";
			while(sc.hasNextLine()){
				String line = sc.nextLine();
				fHist += line + "\n\t";
			}

			System.out.println("History    " + hist);

			String sHist = "Date:\t" + date
					+ "\nPatient:\t" + patient
					+ "\nDN:\t" + hn
					+ "\nDoctor:\t" + doctor
					+  "\nHistory:\t" + fHist
					+ "\n=============================================================\n";			
			reader.write(sHist);
		}
//		if(newResults.size() == 0){
//			JOptionPane.showMessageDialog(HistoryEditor.this, "There is no history");
//		}else{
			//reader.setVisible(true);
//		}
		return reader;
	}
	
	public void setMrn(String mrn){
		mRecMrn = mrn;
	}
	public void disableButtons(){
		btnNewButton.setEnabled(false);
		btnSave.setEnabled(false);
		btnSubmit.setEnabled(false);
	}
	
	public void clearAll(){
		mrnField.setText("");
		dateField.setText("");
		myEditorPane.setText("");
	}

	public String getDateAndTime() {
		String d = "";

		Date date = new Date();

		d = date.toString();
		dateField.setText(d);

		return d;
	}

	public void setProps(String first, String mi, String last,
			String ssn, String cmd, String dob, String doc){
		firstField.setText(first);
		midField.setText(mi);
		lastField.setText(last);
		ssnField.setText(ssn);
		if(cmd.equals("M")){
			butM.setSelected(true);
		}else{
			butF.setSelected(true);
		}
		dobField.setText(dob);
		doctField.setText(doc);
	}

	public void setStateSubmit(boolean enabled){
		if(enabled){
			btnSubmit.setEnabled(true);
		}else{
			btnSubmit.setEnabled(false);
		}
	}

	public void setStateSave(boolean enabled){
		if(enabled){
			btnSave.setEnabled(true);
		}else{
			btnSave.setEnabled(false);
		}
	}

	public void setPropsWithAllFields(String first, String mi, String last,
			String ssn, String cmd, String dob, String doc, String mrn, String date, String history){
		mrnField.setText(mrn);
		firstField.setText(first);
		midField.setText(mi);
		lastField.setText(last);
		ssnField.setText(ssn);
		if(cmd.equals("M")){
			butM.setSelected(true);
		}else{
			butF.setSelected(true);
		}
		dobField.setText(dob);
		doctField.setText(doc);
		dateField.setText(date);
		myEditorPane.setText(history);
	}
	
	public void search(){
		search = new SearchEngine(syscaller, this);
		search.addItems("History");
		search.addItems("History Drafts");
		search.setTitle("Search Engine - " + firstField.getText() + " " + midField.getText() + " " + lastField.getText());
		search.setMrn(mRecMrn);
		search.setVisible(true);
		System.out.println("SE should be running");
	}

	@Override
	public void actionPerformed(ActionEvent evt){
		String cmd = evt.getActionCommand();

		if(cmd.equalsIgnoreCase("done")) {
			syscaller.exitProcess(sProcessId);
			setVisible(false);
		}else if(cmd.equals("search")){
			search();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	public Action getSaveDraftAlt(){
		return saveDraftAlt;
	}

	private Action saveDraftAlt = new SaveDraftAlt();
	class SaveDraftAlt extends AbstractAction {
		public SaveDraftAlt(){
			super("Save As Draft");
		}
		@Override
		public void actionPerformed(ActionEvent e){
			String doctor = doctField.getText();
			String sssn = ssnField.getText();
			String slast = firstField.getText() + " " + midField.getText() + " " + lastField.getText();
			String sdob = dobField.getText();
			String sex = "";
			if(butM.isSelected()){
				sex = "M";
			}else{
				sex = "F";
			}

			String cont1 = mRecMrn + "-DraftHistory";
			String cont2 = "PatHistoryDrafts";
			
			String sContainers = "b|" + cont1 + ",b|" + cont2;
			
			String sObjClass = "File";
			String sObjType = "rtf";
			String sPerms = "File write";


			if (mrnField.getText().length() == 0) {

				String sHist = myEditorPane.getText();
				if(sHist.length() == 0){
					JOptionPane.showMessageDialog(HistoryEditor.this, "Fill History field");
					return;
				}
				
				if(slast.length() <= 2){
					JOptionPane.showMessageDialog(HistoryEditor.this, "You must have a patient name");
					return;
				}
				

				String smrn = generateRandomName(4) + "Draft";
				String sObjName = smrn.substring(0, 8) + "Draft";

				Scanner sc = new Scanner(sHist);
				String fDiag = "";
				String line = sc.nextLine();
				fDiag += line + "\\par";
				while(sc.hasNextLine()){
					line = sc.nextLine();
					fDiag += "\t\t" + line + "\\par";
				}

				//create object
				String sHandle = syscaller.createObject3(sObjName, sObjClass,
						sObjType, sContainers, sPerms, null, null, null, null);
				if (sHandle == null) {
					JOptionPane.showMessageDialog(HistoryEditor.this,
							syscaller.getLastError());
					System.out.println("sHandle was null");
					clearAll();
					return;
				}
				mrnField.setText(smrn);
				dateField.setText(getDateAndTime());
				String sdate = dateField.getText();

				System.out.println("sHandle is not null");

				//Set up properties
				Properties props = new Properties();

				props.put("date", sdate);
				props.put("hn", sObjName);
				props.put("ssn", sssn);
				props.put("name", slast);
				props.put("History", sHist);
				props.put("doctor", doctor);
				props.put("sex", sex);
				props.put("dob", sdob);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(baos, true);
				
				try {
					props.store(baos, null);
					pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}" 
							+ "\nDRAFT History\\par");
					pw.print("\\par Date:\t" + sdate);
					pw.print("\\par HN:\t" + sObjName);
					pw.print("\\par Name:\t" + slast);
					pw.print("\\par SSN:\t" + sssn);
					pw.print("\\par DOB:\t" + sdob);
					pw.print("\\par Sex:\t" + sex);
					pw.print("\\par Doctor:\t" + doctor);
					pw.print("\\par History:\t" + fDiag);
					pw.close();

					byte[] buf = baos.toByteArray();
					int len = syscaller.writeObject3(sHandle, buf);
					if (len < 0) {
						clearAll();
						JOptionPane.showMessageDialog(HistoryEditor.this,
								syscaller.getLastError());
						return;
					}
					syscaller.closeObject(sHandle);
					JOptionPane.showMessageDialog(HistoryEditor.this,
							"Save successful!");

				} catch (Exception ex) {
					clearAll();
					JOptionPane.showMessageDialog(HistoryEditor.this,
							syscaller.getLastError());
					System.out.println("writing contents failed");
					ex.printStackTrace();
					return;

				}

			}else{
				String sObjName = mrnField.getText().substring(0, 8) + "Draft";
				System.out.println("sObjName: " + sObjName);
				System.out.println("Containers: " + sContainers);
				System.out.println("Object name: " + sObjName);
				String sdiag = myEditorPane.getText();
				if(sdiag.length() == 0){
					JOptionPane.showMessageDialog(HistoryEditor.this, "Fill History field");
					return;
				}
				
				if(slast.length() <= 2){
					JOptionPane.showMessageDialog(HistoryEditor.this, "You must have a patient name");
					return;
				}

				Scanner sc = new Scanner(sdiag);
				String fDiag = "";
				String line = sc.nextLine();
				fDiag += line + "\\par";
				while(sc.hasNextLine()){
					line = sc.nextLine();
					fDiag += "\t\t" + line + "\\par";
				}

				System.out.println(sObjName + sObjClass + sObjType
						+ sContainers + sPerms + null + null + null + null);

				String sHandle = syscaller.openObject3(sObjName, "File read");
				if (sHandle == null) {
					JOptionPane.showMessageDialog(HistoryEditor.this,
							syscaller.getLastError());
					System.out.println("sHandle was null");
					return;
				}
				String sdate = dateField.getText();

				Properties props = new Properties();

				props.put("date", sdate);
				props.put("hn", sObjName);
				props.put("ssn", sssn);
				props.put("name", slast);
				props.put("History", sdiag);
				props.put("doctor", doctor);
				props.put("sex", sex);
				props.put("dob", sdob);


				for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
					String sName = (String) propEnum.nextElement();
					System.out.println(sName + "=" + (String) props.get(sName));
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(baos, true);
				
				try {
					props.store(baos, null);
					pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}"  
							+ "\nDRAFT History\\par");
					pw.print("\\par Date:\t" + sdate);
					pw.print("\\par HN:\t" + sObjName);
					pw.print("\\par Name:\t" + slast);
					pw.print("\\par SSN:\t" + sssn);
					pw.print("\\par DOB:\t" + sdob);
					pw.print("\\par Sex:\t" + sex);
					pw.print("\\par Doctor:\t" + doctor);
					pw.print("\\par History:\t" + fDiag);
					pw.close();
					byte[] buf = baos.toByteArray();
					int len = syscaller.writeObject3(sHandle, buf);
					if (len < 0) {
						JOptionPane.showMessageDialog(HistoryEditor.this,
								syscaller.getLastError());
						clearAll();
						return;
					}
					syscaller.closeObject(sHandle);

					JOptionPane.showMessageDialog(HistoryEditor.this,
							"Save successful!");
				} catch (Exception ex) {
					clearAll();
					JOptionPane.showMessageDialog(HistoryEditor.this,
							syscaller.getLastError());
					ex.printStackTrace();
					return;

				}
			}
		}
	}


	//////////////////////////////////////SUBMIT///////////////////////////////////////////////////
	public Action getSubmitToHistory(){
		return submitHist;
	}

	private Action submitHist = new SubmitToHistory();
	private JButton btnViewAllHistory;

	class SubmitToHistory extends AbstractAction{
		public SubmitToHistory(){
			super("Submit to History");

		}
		@Override
		public void actionPerformed(ActionEvent e){
			String sObjName = mrnField.getText();
			String sssn = ssnField.getText();
			String slast = firstField.getText() + " " + midField.getText() + " " + lastField.getText();
			String doctor = doctField.getText();
			String sdob = dobField.getText();
			String sdate = "";
			String sex = "";

			if(butM.isSelected()){
				sex = "M";
			}else{
				sex = "F";
			}

			Properties props = new Properties();

			//If this History has not been saved to a draft before
			if(sObjName.length() == 0){

				String sdiag = myEditorPane.getText();
				if(sdiag.length() == 0){
					JOptionPane.showMessageDialog(HistoryEditor.this, "Fill History field");
					return;
				}
				
				if(slast.length() <= 2){
					JOptionPane.showMessageDialog(HistoryEditor.this, "You must have a patient name");
					return;
				}

				sObjName = generateRandomName(4);

				Scanner sc = new Scanner(sdiag);
				String fDiag = "";
				String line = sc.nextLine();
				fDiag += line + "\\par";
				while(sc.hasNextLine()){
					line = sc.nextLine();
					fDiag += "\t\t" + line + "\\par";
				}

				String cont1 = mRecMrn + "-History";
				String cont2 = "PatHistory";
				String sContainers = "b|" + cont1 + ",b|" + cont2;

				System.out.println("sObjName: " + sObjName);

				System.out.println("Containers: " + sContainers);
				System.out.println("Object name: " + sObjName);

				String sObjClass = "File";
				String sObjType = "rtf";
				String sPerms = "File read";

				String sHandle = syscaller.createObject3(sObjName, sObjClass, sObjType, 
						sContainers, sPerms, null, null, null, null);

				if(sHandle == null){
					JOptionPane.showMessageDialog(HistoryEditor.this, syscaller.getLastError());
					System.out.println("sHandle was null");
					return;
				}
				mrnField.setText(sObjName);
				dateField.setText(getDateAndTime());
				sdate = dateField.getText();

				props.put("date", sdate);
				props.put("hn", sObjName);
				props.put("ssn", sssn);
				props.put("name", slast);
				props.put("History", sdiag);
				props.put("doctor", doctor);
				props.put("sex", sex);
				props.put("dob", sdob);


				for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
					String sName = (String) propEnum.nextElement();
					System.out.println(sName + "=" + (String) props.get(sName));
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(baos, true);

				try {
					props.store(baos, null);
					pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}" 
							+ "\nSUBMITTED History\\par");
					pw.print("\\par Date:\t" + sdate);
					pw.print("\\par HN:\t" + sObjName);
					pw.print("\\par Name:\t" + slast);
					pw.print("\\par SSN:\t" + sssn);
					pw.print("\\par DOB:\t" + sdob);
					pw.print("\\par Sex:\t" + sex);
					pw.print("\\par Doctor:\t" + doctor);
					pw.print("\\par History:\t" + fDiag);
					pw.close();
					byte[] buf = baos.toByteArray();
					int len = syscaller.writeObject3(sHandle, buf);
					if(len < 0){
						clearAll();
						JOptionPane.showMessageDialog(HistoryEditor.this, syscaller.getLastError());
						return;
					}
					syscaller.closeObject(sHandle);

				}catch(Exception ex){
					clearAll();
					JOptionPane.showMessageDialog(HistoryEditor.this,
							syscaller.getLastError());
					System.out.println("properties did not load for " + sObjName);
					ex.printStackTrace();
					return;
				}

				//if the History has been saved as a draft and already has a History number	
			}else if(sObjName.length() > 0){
				System.out.println("actionPerformed sObjName = " + sObjName);

				sObjName = sObjName.substring(0, 8) + "Draft";
				sdate = getDateAndTime();
				doctor = doctField.getText();
				String sProp1 = "PatHistoryDrafts";
				String sContainer1 = "PatHistory";
				System.out.println(sContainer1 + " " + sProp1);
				String sProp2 = "DraftHistory=" + mRecMrn;
				String newDiag = myEditorPane.getText();
				if(newDiag.length() == 0){
					JOptionPane.showMessageDialog(HistoryEditor.this, "Fill History field");
					return;
				}
				
				if(slast.length() <= 2){
					JOptionPane.showMessageDialog(HistoryEditor.this, "You must have a patient name");
					return;
				}

				Scanner sc = new Scanner(newDiag);
				String fDiag = "";
				String line = sc.nextLine();
				fDiag += line + "\\par";
				while(sc.hasNextLine()){
					line = sc.nextLine();
					fDiag += "\t\t" + line + "\\par";
				}


				boolean cmd = syscaller.deassignObjFromOattr3(sObjName, sProp1);
				System.out.println("deassigning " + sObjName + " from " + sProp1);
				if(!cmd){
					System.out.println("deassignObjFromOattr didn't work 1");
					return;
				}
				cmd = syscaller.deassignObjFromOattr3(sObjName, sProp2);
				System.out.println("deassigning " + sObjName + " from " + sProp2);
				if(!cmd){
					System.out.println("deassignObjFromOattr didn't work 2");
					return;
				}

				String cont1 = mRecMrn + "-History";
				String cont2 = "PatHistory";
				String sContainers = "b|" + cont1 + ",b|" + cont2;

				sObjName = sObjName.substring(0, 8);
				String sHandle = syscaller.createObject3(sObjName, "File", "rtf",
						sContainers, "File read", null, null, null, null);
				System.out.println(sHandle);
				if(sHandle == null){
					JOptionPane.showMessageDialog(HistoryEditor.this, syscaller.getLastError());
					return;
				}

				props.put("date", sdate);
				props.put("hn", sObjName);
				props.put("ssn", sssn);
				props.put("name", slast);
				props.put("History", newDiag);
				props.put("doctor", doctor);
				props.put("sex", sex);
				props.put("dob", sdob);


				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintWriter pw = new PrintWriter(baos, true);

				try {
					props.store(baos, null);		
					pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}"
							+ "\nSUBMITTED History\\par");
					pw.print("\\par Date:\t" + sdate);
					pw.print("\\par HN:\t" + sObjName);
					pw.print("\\par Name:\t" + slast);
					pw.print("\\par SSN:\t" + sssn);
					pw.print("\\par DOB:\t" + sdob);
					pw.print("\\par Sex:\t" + sex);
					pw.print("\\par Doctor:\t" + doctor);
					pw.print("\\par History:\t" + fDiag);
					pw.close();

					byte[] buf = baos.toByteArray();
					int ret = syscaller.writeObject3(sHandle, buf);
					if (ret < 0) {
						clearAll();
						JOptionPane.showMessageDialog(HistoryEditor.this,
								syscaller.getLastError());
						return;
					}
					syscaller.closeObject(sHandle);
				} catch (Exception ex) {
					clearAll();
					JOptionPane.showMessageDialog(HistoryEditor.this,
							"Exception while saving object: " + ex.getMessage());
					ex.printStackTrace();
					return;
				}


			}
			btnSubmit.setEnabled(false);
			JOptionPane.showMessageDialog(HistoryEditor.this, "Submit Sucessful!");
			mrnField.setText(sObjName);
			btnSave.setEnabled(false);

		}
	}
}
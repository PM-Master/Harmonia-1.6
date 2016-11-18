package gov.nist.csd.pm.application.medrec.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.net.Packet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SearchEngine extends JFrame{
	private JTextField textFromMonth;
	private JTextField textToMonth;
	private JTextField textFromDay;
	private JTextField textToDay;
	private JTextField textToYear;
	private JTextField textFromYear;

	private JLabel lblResults;
	private JList resultList;
	public DefaultListModel resultListModel;
	private SysCaller syscaller;

	private JComboBox comboBox;
	private JButton btnReadAll;

	private ArrayList<String> found = new ArrayList<String>();
	private List<String> newResults = new ArrayList<String>();
	private JButton btnSearch;
	private String patientName;
	private String sKstorePath;
	private String sTstorePath;
	private String sRtfPath;
	private String sWkfPath;
	private String sEmlPath;
	private String sOffPath;
	private String selectedMrn;

//	private TreatEditor treatEditor;
//	private DiagnosisEditor diagEditor;
//	private HistoryEditor historyEditor;
	private Editor editor;
	private JButton btnOpen;


	public SearchEngine(SysCaller syscaller, Editor editor){
		setTitle("Search Engine");
		setBounds(100, 100, 450, 674);
		setSize(450, 350);
		SpringLayout springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);

		this.syscaller = syscaller;//new SysCallerImpl(nSimPort, sSessId, sProcId, bDebug, "SearchEngine");
		this.editor = editor;
		
		JLabel lblSelectRecordsTo = new JLabel("Search for:");
		springLayout.putConstraint(SpringLayout.NORTH, lblSelectRecordsTo, 9, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, lblSelectRecordsTo, 10, SpringLayout.WEST, getContentPane());
		getContentPane().add(lblSelectRecordsTo);

		comboBox = new JComboBox();
		springLayout.putConstraint(SpringLayout.NORTH, comboBox, -4, SpringLayout.NORTH, lblSelectRecordsTo);
		springLayout.putConstraint(SpringLayout.WEST, comboBox, 6, SpringLayout.EAST, lblSelectRecordsTo);
		getContentPane().add(comboBox);

		JLabel lblFrom = new JLabel("From:");
		springLayout.putConstraint(SpringLayout.NORTH, lblFrom, 0, SpringLayout.NORTH, lblSelectRecordsTo);
		getContentPane().add(lblFrom);

		textFromMonth = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, lblFrom, -6, SpringLayout.WEST, textFromMonth);
		springLayout.putConstraint(SpringLayout.NORTH, textFromMonth, -3, SpringLayout.NORTH, lblSelectRecordsTo);
		textFromMonth.setText("MM");
		getContentPane().add(textFromMonth);
		textFromMonth.setColumns(2);
		textFromMonth.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent m){
				textFromMonth.setText("");
			}
		});

		textFromDay = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, textFromMonth, -6, SpringLayout.WEST, textFromDay);
		springLayout.putConstraint(SpringLayout.NORTH, textFromDay, -3, SpringLayout.NORTH, lblSelectRecordsTo);
		textFromDay.setText("DD");
		getContentPane().add(textFromDay);
		textFromDay.setColumns(2);
		textFromDay.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent m){
				textFromDay.setText("");
			}
		});

		textFromYear = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, textFromDay, -6, SpringLayout.WEST, textFromYear);
		springLayout.putConstraint(SpringLayout.NORTH, textFromYear, -3, SpringLayout.NORTH, lblSelectRecordsTo);
		springLayout.putConstraint(SpringLayout.EAST, textFromYear, -10, SpringLayout.EAST, getContentPane());
		textFromYear.setText("YYYY");
		getContentPane().add(textFromYear);
		textFromYear.setColumns(3);
		textFromYear.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent m){
				textFromYear.setText("");
			}
		});

		textToYear = new JTextField();
		textToYear.setText("YYYY");
		springLayout.putConstraint(SpringLayout.NORTH, textToYear, 6, SpringLayout.SOUTH, textFromMonth);
		springLayout.putConstraint(SpringLayout.EAST, textToYear, 0, SpringLayout.EAST, textFromYear);
		getContentPane().add(textToYear);
		textToYear.setColumns(3);
		textToYear.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent m){
				textToYear.setText("");
			}
		});

		textToDay = new JTextField();
		textToDay.setText("DD");
		springLayout.putConstraint(SpringLayout.SOUTH, textToDay, 0, SpringLayout.SOUTH, textToYear);
		springLayout.putConstraint(SpringLayout.EAST, textToDay, 0, SpringLayout.EAST, textFromDay);
		getContentPane().add(textToDay);
		textToDay.setColumns(2);
		textToDay.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent m){
				textToDay.setText("");
			}
		});

		textToMonth = new JTextField();
		textToMonth.setText("MM");
		springLayout.putConstraint(SpringLayout.SOUTH, textToMonth, 0, SpringLayout.SOUTH, textToYear);
		springLayout.putConstraint(SpringLayout.EAST, textToMonth, 0, SpringLayout.EAST, textFromMonth);
		getContentPane().add(textToMonth);
		textToMonth.setColumns(2);
		textToMonth.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent m){
				textToMonth.setText("");
			}
		});

		JLabel lblTol = new JLabel("To:");
		springLayout.putConstraint(SpringLayout.NORTH, lblTol, 3, SpringLayout.NORTH, textToYear);
		springLayout.putConstraint(SpringLayout.EAST, lblTol, 0, SpringLayout.EAST, lblFrom);
		getContentPane().add(lblTol);

		btnSearch = new JButton("Search");
		springLayout.putConstraint(SpringLayout.NORTH, btnSearch, 6, SpringLayout.SOUTH, textToYear);
		springLayout.putConstraint(SpringLayout.EAST, btnSearch, 0, SpringLayout.EAST, textFromYear);
		btnSearch.setAction(getTreatmentSearchAction());
		getContentPane().add(btnSearch);

		JButton btnNewSearch = new JButton("New Search");
		springLayout.putConstraint(SpringLayout.WEST, btnNewSearch, 0, SpringLayout.WEST, lblSelectRecordsTo);
		springLayout.putConstraint(SpringLayout.SOUTH, btnNewSearch, -10, SpringLayout.SOUTH, getContentPane());
		btnNewSearch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				resetAll();
			}
		});
		getContentPane().add(btnNewSearch);

		btnOpen = new JButton(getOpenAction());
		springLayout.putConstraint(SpringLayout.NORTH, btnOpen, 0, SpringLayout.NORTH, btnNewSearch);
		springLayout.putConstraint(SpringLayout.WEST, btnOpen, 6, SpringLayout.EAST, btnNewSearch);
		btnOpen.setEnabled(false);
		getContentPane().add(btnOpen);

		btnReadAll = new JButton("Read All Results");
		springLayout.putConstraint(SpringLayout.NORTH, btnReadAll, 0, SpringLayout.NORTH, btnNewSearch);
		springLayout.putConstraint(SpringLayout.WEST, btnReadAll, 6, SpringLayout.EAST, btnOpen);
		getContentPane().add(btnReadAll);
		btnReadAll.setEnabled(false);
		btnReadAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				readAllResults();
			}
		});

		resultListModel = new DefaultListModel();
		resultList = new JList(resultListModel);

		JScrollPane scrollPane = new JScrollPane(resultList);
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, btnSearch);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, lblSelectRecordsTo);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -6, SpringLayout.NORTH, btnNewSearch);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 432, SpringLayout.WEST, getContentPane());

		getContentPane().add(scrollPane);

		lblResults = new JLabel("");
		springLayout.putConstraint(SpringLayout.WEST, lblResults, 0, SpringLayout.WEST, lblSelectRecordsTo);
		springLayout.putConstraint(SpringLayout.SOUTH, lblResults, 0, SpringLayout.SOUTH, btnSearch);
		getContentPane().add(lblResults);

		btnDone = new JButton("Done");
		springLayout.putConstraint(SpringLayout.NORTH, btnDone, 0, SpringLayout.NORTH, btnNewSearch);
		springLayout.putConstraint(SpringLayout.EAST, btnDone, 0, SpringLayout.EAST, scrollPane);
		btnDone.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}

		});
		getContentPane().add(btnDone);

		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);

		Packet res = (Packet) syscaller.getKStorePaths();
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
	}

	public void addItems(Object item){
		comboBox.addItem(item);
	}

	public void resetAll(){
		resultList.removeAll();
		resultListModel.clear();
		textFromMonth.setText("MM");
		textFromDay.setText("DD");
		textFromYear.setText("YYYY");
		textToMonth.setText("MM");
		textToDay.setText("DD");
		textToYear.setText("YYYY");
		lblResults.setText("");
		btnReadAll.setEnabled(false);
	}


	public void readAllResults(){
		System.out.println("readAllResults called");
		String selected = (String)comboBox.getSelectedItem();
		if(selected.equals("Treatments")){
			ReadAllDocument read = new ReadAllDocument(null, null);
			read.setTitle("Treatments");
			for(int i = 0; i < found.size(); i++){
				String sObjName = found.get(i);
				String sHandle = syscaller.openObject3(sObjName, "File read");
				byte[] buf = syscaller.readObject3(sHandle);
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				Properties props = new Properties();

				try {
					System.out.println("about to load " + sObjName);
					props.load(bais);
					System.out.println("loaded properties for " + sObjName);
				}catch(Exception ex){
					System.out.println("properties did not load for " + sObjName);
					JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
					ex.printStackTrace();
					return;
				}
				String tn = props.getProperty("tn");
				String date = props.getProperty("date");
				String doctor = props.getProperty("doctor");
				String patient = props.getProperty("name");
				String treat = props.getProperty("treatment");

				Scanner sc = new Scanner(treat);
				String fTreat = "";
				while(sc.hasNextLine()){
					String line = sc.nextLine();
					fTreat += line + "\n\t";
				}

				System.out.println("TREATMENT    " + treat);

				String treatment = "Date:\t" + date
						+ "\nPatient:\t" + patient
						+ "\nTN:\t" + tn
						+ "\nDoctor:\t" + doctor
						+  "\nTreatment:\t" + fTreat
						+ "\n=============================================================\n";
				read.write(treatment);
			}
			read.setVisible(true);
		}else if(selected.equals("Treatment Drafts")){
			ReadAllDocument read = new ReadAllDocument(null, null);
			read.setTitle("Treatment Drafts");
			for(int i = 0; i < found.size(); i++){
				String sObjName = found.get(i);
				String sHandle = syscaller.openObject3(sObjName, "File read");
				byte[] buf = syscaller.readObject3(sHandle);
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				Properties props = new Properties();
				try {
					System.out.println("about to load " + sObjName);
					props.load(bais);
					System.out.println("loaded properties for " + sObjName);
				}catch(Exception ex){
					System.out.println("properties did not load for " + sObjName);
					JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
					ex.printStackTrace();
					return;
				}
				String tn = props.getProperty("tn");
				String date = props.getProperty("date");
				String doctor = props.getProperty("doctor");
				String patient = props.getProperty("name");
				String treat = props.getProperty("treatment");

				Scanner sc = new Scanner(treat);
				String fTreat = "";
				while(sc.hasNextLine()){
					String line = sc.nextLine();
					fTreat += line + "\n\t";
				}

				System.out.println("DRAFT TREATMENT    " + treat);

				String treatment = "Date:\t" + date
						+ "\nPatient:\t" + patient
						+ "\nTN:\t" + tn
						+ "\nDoctor:\t" + doctor
						+  "\nTreatment:\t" + fTreat
						+ "\n=============================================================\n";
				read.write(treatment);
			}
			read.setVisible(true);
		}else if(selected.equals("Diagnoses")){
			System.out.println("Diagnoses");
			ReadAllDocument read = new ReadAllDocument(null, null);
			read.setTitle("Diagnoses");
			for(int i = 0; i < found.size(); i++){
				String sObjName = found.get(i);
				String sHandle = syscaller.openObject3(sObjName, "File read");
				byte[] buf = syscaller.readObject3(sHandle);
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				Properties props = new Properties();

				try {
					System.out.println("about to load " + sObjName);
					props.load(bais);
					System.out.println("loaded properties for " + sObjName);
				}catch(Exception ex){
					System.out.println("properties did not load for " + sObjName);
					JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
					ex.printStackTrace();
					return;
				}
				String dn = props.getProperty("dn");
				String date = props.getProperty("date");
				String doctor = props.getProperty("doctor");
				String patient = props.getProperty("name");
				String diag = props.getProperty("diagnosis");

				Scanner sc = new Scanner(diag);
				String fDiag = "";
				while(sc.hasNextLine()){
					String line = sc.nextLine();
					fDiag += line + "\n\t";
				}

				System.out.println("DIAGNOSIS    " + fDiag);

				String sDiag = "Date:\t" + date
						+ "\nPatient:\t" + patient
						+ "\nDN:\t" + dn
						+ "\nDoctor:\t" + doctor
						+  "\nDiagnosis:\t" + fDiag
						+ "\n=============================================================\n";
				read.write(sDiag);
			}
			read.setVisible(true);
		}else if(selected.equals("Diagnosis Drafts")){
			ReadAllDocument read = new ReadAllDocument(null, null);
			read.setTitle("Diagnoses Drafts");
			for(int i = 0; i < found.size(); i++){
				String sObjName = found.get(i);
				String sHandle = syscaller.openObject3(sObjName, "File read");
				byte[] buf = syscaller.readObject3(sHandle);
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				Properties props = new Properties();

				try {
					System.out.println("about to load " + sObjName);
					props.load(bais);
					System.out.println("loaded properties for " + sObjName);
				}catch(Exception ex){
					System.out.println("properties did not load for " + sObjName);
					JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
					ex.printStackTrace();
					return;
				}
				String dn = props.getProperty("dn");
				String date = props.getProperty("date");
				String doctor = props.getProperty("doctor");
				String patient = props.getProperty("name");
				String diag = props.getProperty("diagnosis");

				Scanner sc = new Scanner(diag);
				String fDiag = "";
				while(sc.hasNextLine()){
					String line = sc.nextLine();
					fDiag += line + "\n\t";
				}

				System.out.println("DRAFT DIAGNOSIS    " + fDiag);

				String sDiag = "Date:\t" + date
						+ "\nPatient:\t" + patient
						+ "\nDN:\t" + dn
						+ "\nDoctor:\t" + doctor
						+  "\nDiagnosis:\t" + fDiag
						+ "\n=============================================================\n";
				read.write(sDiag);
			}
			read.setVisible(true);
		}else if(selected.equals("History")){
			ReadAllDocument read = new ReadAllDocument(null, null);
			read.setTitle("History");
			for(int i = 0; i < found.size(); i++){
				String sObjName = found.get(i);
				String sHandle = syscaller.openObject3(sObjName, "File read");
				byte[] buf = syscaller.readObject3(sHandle);
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				Properties props = new Properties();

				try {
					System.out.println("about to load " + sObjName);
					props.load(bais);
					System.out.println("loaded properties for " + sObjName);
				}catch(Exception ex){
					System.out.println("properties did not load for " + sObjName);
					JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
					ex.printStackTrace();
					return;
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

				System.out.println("DRAFT DIAGNOSIS    " + fHist);

				String sHist = "Date:\t" + date
						+ "\nPatient:\t" + patient
						+ "\nDN:\t" + hn
						+ "\nDoctor:\t" + doctor
						+  "\nHistory:\t" + fHist
						+ "\n=============================================================\n";
				read.write(sHist);
			}
			read.setVisible(true);
		}else if(selected.equals("History Drafts")){
			ReadAllDocument read = new ReadAllDocument(null, null);
			read.setTitle("History Drafts");
			for(int i = 0; i < found.size(); i++){
				String sObjName = found.get(i);
				String sHandle = syscaller.openObject3(sObjName, "File read");
				byte[] buf = syscaller.readObject3(sHandle);
				ByteArrayInputStream bais = new ByteArrayInputStream(buf);
				Properties props = new Properties();

				try {
					System.out.println("about to load " + sObjName);
					props.load(bais);
					System.out.println("loaded properties for " + sObjName);
				}catch(Exception ex){
					System.out.println("properties did not load for " + sObjName);
					JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
					ex.printStackTrace();
					return;
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

				System.out.println("DRAFT DIAGNOSIS    " + fHist);

				String sDiag = "Date:\t" + date
						+ "\nPatient:\t" + patient
						+ "\nDN:\t" + hn
						+ "\nDoctor:\t" + doctor
						+  "\nDiagnosis:\t" + fHist
						+ "\n=============================================================\n";
				read.write(sDiag);
			}
			read.setVisible(true);
		}
	}
	
	public void setMrn(String mrn){
		selectedMrn = mrn;
	}

	//////////////////////////////////////////////////////SEARCH///////////////////////////////////////////////////////////////////
	public Action getTreatmentSearchAction(){
		return searchTreatAction;
	}

	private Action searchTreatAction = new SearchTreatmentAction();

	class SearchTreatmentAction extends AbstractAction{

		public SearchTreatmentAction(){
			super("Search");
		}

		public ArrayList<String> getResults(String sPropName, boolean draft){
			String cont = syscaller.getNameOfContainerWithProperty(sPropName);
			String id1 = syscaller.getIdOfEntityWithNameAndType(cont, "b");
			String[] results = null;
			ArrayList<String> retList = new ArrayList<String>();
			String sType = "b";
			String sGraphType = "ac";
			List<String[]> members = syscaller.getMembersOf(cont, id1, sType, sGraphType);
			System.out.println("before getting results");
			System.out.println(cont);
			if(draft){
				for(int i = 0; i < members.size(); i++){
					results = (String[])members.get(i);
					System.out.println("member " + i + ":" + results[0] + ":" + results[1] + ":" + results[2]);
					if(results[0].equals("o") && results[2].length() == 13){
						retList.add(results[2]);
					}
				}
				System.out.println("getting results");
			}else{
				for(int i = 0; i < members.size(); i++){
					results = (String[])members.get(i);
					System.out.println("member " + i + ":" + results[0] + ":" + results[1] + ":" + results[2]);
					if(results[0].equals("o") && results[2].length() == 8){
						retList.add(results[2]);
					}
				}
				System.out.println("getting results");
			}
			return retList;
		}

		public void search(){
			System.out.println("search() called");
			patientName = getTitle().substring(16);
			String selected = (String)comboBox.getSelectedItem();
			System.out.println(selected);
			found.clear();
			resultListModel.clear();
			newResults.clear();
			String[] results = null;
			String sel = "";
			if(selected.equals("Treatments")){
				sel = "treatments";
			}else if(selected.equals("Treatment Drafts")){
				sel = "drafts";
			}else if(selected.equals("Diagnoses")){
				sel = "diagnosis";
			}else if(selected.equals("Diagnosis Drafts")){
				sel = "diagdrafts";
			}else if(selected.equals("History")){
				sel = "history";
			}else if(selected.equals("History Drafts")){
				sel = "histdrafts";
			}
			System.out.println(sel);

			if(sel.length() == 0){
				JOptionPane.showMessageDialog(SearchEngine.this, "Please select one");
				return;
			}

			if(sel.equals("treatments")){
				newResults = getResults("Treatments=" + selectedMrn, false);
			}else if(sel.equals("drafts")){
				newResults = getResults("DraftTreatments=" + selectedMrn, true);
			}else if(sel.equals("diagnosis")){
				newResults = getResults("Diagnoses=" + selectedMrn, false);
			}else if(sel.equals("diagdrafts")){
				newResults = getResults("DraftDiagnoses=" + selectedMrn, true);
			}else if(sel.equals("history")){
				newResults = getResults("History=" + selectedMrn, false);
			}else if(sel.equals("histdrafts")){
				newResults = getResults("DraftHistory=" + selectedMrn, true);
			}

			System.out.println("results have been set");
			System.out.println(newResults.size());
			try{
				int counter = 0;
				for(int i = 0; i < newResults.size(); i++){
					String res = newResults.get(i);
					System.out.println(res);
					String sHandle = syscaller.openObject3(res, "File read");
					System.out.println(sHandle);
					byte[] buf = syscaller.readObject3(sHandle);
					if(buf == null){
						JOptionPane.showMessageDialog(SearchEngine.this,
								syscaller.getLastError());
						System.out.println("sHandle was null");
					}
					ByteArrayInputStream bais = new ByteArrayInputStream(buf);
					Properties props = new Properties();
					for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
						String sName = (String) propEnum.nextElement();
						System.out.println(sName + "=" + (String) props.get(sName));
					}

					try {
						System.out.println("about to load " + res);
						props.load(bais);
						System.out.println("loaded properties for " + res);
					}catch(Exception ex){
						ex.printStackTrace();
						System.out.println("properties did not load for " + res);
						return;
					}
					System.out.println("about to get patient");
					String patient = props.getProperty("name");
					System.out.println("got patient " + patient);
					System.out.println(patientName);


					if(patient.equals(patientName)){
						String date = props.getProperty("date");
						int year = Integer.valueOf(date.substring(24));
						int day = Integer.valueOf(date.substring(8, 10));
						String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
						String mon = date.substring(4, 7);
						int month = Integer.valueOf(Arrays.asList(months).indexOf(mon))+1;
						String treatDate =  month + "/" + day + "/" + year;
						System.out.println("Treatment date: " + treatDate);

						int fromMonth = Integer.valueOf(textFromMonth.getText()); 
						int fromDay = Integer.valueOf(textFromDay.getText());
						int fromYear = Integer.valueOf(textFromYear.getText());
						String fromDate =   fromMonth + "/" + fromDay + "/" + fromYear;
						System.out.println("From: " + fromDate);

						int toMonth = Integer.valueOf(textToMonth.getText()); 
						int toDay = Integer.valueOf(textToDay.getText());
						int toYear = Integer.valueOf(textToYear.getText());
						String toDate =  toMonth + "/" + toDay + "/" + toYear;
						System.out.println("To: " + toDate);

						DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
						Date dDate = format.parse(treatDate);
						Date dFrom = format.parse(fromDate);
						Date dTo = format.parse(toDate);

						System.out.println(dFrom + " " + dDate + " " + dTo);

						if((dDate.compareTo(dFrom) >= 0) && (dDate.compareTo(dTo) <= 0)){
							counter++;
							found.add(res);
							resultListModel.addElement(date);
							System.out.println("result list " + i + " = " + date);
						}

					}
				}
				if(counter == 1){
					lblResults.setText("Search found " + counter + " result:");
				}else if(counter > 1){
					lblResults.setText("Search found " + counter + " results:");
				}else if(counter == 0){
					lblResults.setText("Search found " + counter + " results:");
				}
				btnOpen.setEnabled(true);
			}catch(Exception ex){
				JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
				System.out.println("something went wrong in searchAction");
				ex.printStackTrace();
			}
			if(found.size() == 0){
				btnReadAll.setEnabled(false);
				return;
			}else{
				btnReadAll.setEnabled(true);
			}
		}
		public void actionPerformed(ActionEvent e){
			Object cmd = e.getSource();
			System.out.println(cmd);
			if(cmd.equals(btnSearch)){
				search();
			}
		}
	}

	public Action getOpenAction(){
		return openAction;
	}

	private Action openAction = new OpenAction();
	private JButton btnDone;

	class OpenAction extends AbstractAction{

		public OpenAction(){
			super("Open");
		}

		public void select(){
			String sObjName = ""; 
			String selected = (String)comboBox.getSelectedItem();
			int nSelIx = resultList.getSelectedIndex();
			if(nSelIx < 0){
				JOptionPane.showMessageDialog(SearchEngine.this, "Select a result");
				return;
			}

			String selectObj = (String) resultListModel.get(nSelIx);
			System.out.println("SELECTED " + selectObj);
			String sel = "";
			if(selected.equals("Treatments")){
				sel = "treatments";
			}else if(selected.equals("Treatment Drafts")){
				sel = "drafts";
			}else if(selected.equals("Diagnoses")){
				sel = "diagnosis";
			}else if(selected.equals("Diagnosis Drafts")){
				sel = "diagdrafts";
			}else if(selected.equals("History")){
				sel = "history";
			}else if(selected.equals("History Drafts")){
				sel = "histdrafts";
			}

			System.out.println(sel);
			if(sel.equals("treatments") || sel.equals("drafts")){
				Properties props = new Properties();
				for(int i = 0; i < newResults.size(); i++){
					System.out.println(newResults.get(i));
				}

				for(int i = 0; i < newResults.size(); i++){
					String res = newResults.get(i);
					System.out.println(res);
					String sHandle = syscaller.openObject3(res, "File read");
					System.out.println(sHandle);
					byte[] buf = syscaller.readObject3(sHandle);
					ByteArrayInputStream bais = new ByteArrayInputStream(buf);

					try {
						System.out.println("about to load " + res);
						props.load(bais);
						System.out.println("loaded properties for " + res);
					}catch(Exception ex){
						System.out.println("properties did not load for " + sObjName);
						JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
						ex.printStackTrace();
						return;
					}
					String date = props.getProperty("date");
					System.out.println(date);
					if(date.equals(selectObj)){
						sObjName = props.getProperty("tn");
						break;
					}
				}
				System.out.println("OBJNAME: " + sObjName);

				for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
					String sName = (String) propEnum.nextElement();
					System.out.println(sName + "=" + (String) props.get(sName));
				}

				String[] name = props.getProperty("name").split(" ");

				String mrn = (props.getProperty("tn"));
				String date = (props.getProperty("date"));
				String dob = (props.getProperty("dob"));
				String first = (name[0]);
				String mi = (name[1]);
				String last = (name[2]);
				String ssn = (props.getProperty("ssn"));
				String gen = props.getProperty("sex");
				String doc = (props.getProperty("doctor"));
				String treat = (props.getProperty("treatment"));

				editor.setPropsWithAllFields(first, mi, last, ssn, gen, dob, doc, mrn, date, treat);

				if(sel.equals("treatments")){
					editor.setStateSubmit(false);
					editor.setStateSave(false);
				}else{
					System.out.println(sel);
					editor.setStateSubmit(true);
					//treatEditor.setStateSave(false);
				}
				resetAll();
				setVisible(false);
			}else if(sel.equals("diagnosis") || sel.equals("diagdrafts")){
				Properties props = new Properties();
				for(int i = 0; i < newResults.size(); i++){
					System.out.println(newResults.get(i));
				}

				for(int i = 0; i < newResults.size(); i++){
					String res = newResults.get(i);
					System.out.println(res);
					String sHandle = syscaller.openObject3(res, "File read");
					System.out.println(sHandle);
					byte[] buf = syscaller.readObject3(sHandle);
					ByteArrayInputStream bais = new ByteArrayInputStream(buf);

					try {
						System.out.println("about to load " + res);
						props.load(bais);
						System.out.println("loaded properties for " + res);
					}catch(Exception ex){
						System.out.println("properties did not load for " + sObjName);
						JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
						ex.printStackTrace();
						return;
					}
					String date = props.getProperty("date");
					System.out.println(date);
					if(date.equals(selectObj)){
						sObjName = props.getProperty("dn");
						break;
					}
				}
				System.out.println("OBJNAME: " + sObjName);

				for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
					String sName = (String) propEnum.nextElement();
					System.out.println(sName + "=" + (String) props.get(sName));
				}

				String[] name = props.getProperty("name").split(" ");

				String mrn = (props.getProperty("dn"));
				String date = (props.getProperty("date"));
				String dob = (props.getProperty("dob"));
				String first = (name[0]);
				String mi = (name[1]);
				String last = (name[2]);
				String ssn = (props.getProperty("ssn"));
				String gen = props.getProperty("sex");
				String doc = (props.getProperty("doctor"));
				String treat = (props.getProperty("diagnosis"));

				editor.setPropsWithAllFields(first, mi, last, ssn, gen, dob, doc, mrn, date, treat);

				if(sel.equals("diagnosis")){
					editor.setStateSubmit(false);
					editor.setStateSave(false);
				}else{
					System.out.println(sel);
					editor.setStateSubmit(true);
					//diagEditor.setStateSave(false);
				}
				resetAll();
				setVisible(false);
			}else if(sel.equals("history") || sel.equals("histdrafts")){
				Properties props = new Properties();
				for(int i = 0; i < newResults.size(); i++){
					System.out.println(newResults.get(i));
				}

				for(int i = 0; i < newResults.size(); i++){
					String res = newResults.get(i);
					System.out.println(res);
					String sHandle = syscaller.openObject3(res, "File read");
					System.out.println(sHandle);
					byte[] buf = syscaller.readObject3(sHandle);
					ByteArrayInputStream bais = new ByteArrayInputStream(buf);

					try {
						System.out.println("about to load " + res);
						props.load(bais);
						System.out.println("loaded properties for " + res);
					}catch(Exception ex){
						System.out.println("properties did not load for " + sObjName);
						JOptionPane.showMessageDialog(SearchEngine.this, syscaller.getLastError());
						ex.printStackTrace();
						return;
					}
					String date = props.getProperty("date");
					System.out.println(date);
					if(date.equals(selectObj)){
						sObjName = props.getProperty("hn");
						break;
					}
				}
				System.out.println("OBJNAME: " + sObjName);

				for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
					String sName = (String) propEnum.nextElement();
					System.out.println(sName + "=" + (String) props.get(sName));
				}

				String[] name = props.getProperty("name").split(" ");

				String mrn = (props.getProperty("hn"));
				String date = (props.getProperty("date"));
				String dob = (props.getProperty("dob"));
				String first = (name[0]);
				String mi = (name[1]);
				String last = (name[2]);
				String ssn = (props.getProperty("ssn"));
				String gen = props.getProperty("sex");
				String doc = (props.getProperty("doctor"));
				String hist = (props.getProperty("History"));

				editor.setPropsWithAllFields(first, mi, last, ssn, gen, dob, doc, mrn, date, hist);

				if(sel.equals("history")){
					editor.setStateSubmit(false);
					editor.setStateSave(false);
				}else{
					System.out.println(sel);
					editor.setStateSubmit(true);
					//historyEditor.setStateSave(false);
				}
				resetAll();
				setVisible(false);
			}
			editor.setVisible(true);
		}

		public void actionPerformed(ActionEvent e){
			String cmd = e.getActionCommand();
			if(cmd.equalsIgnoreCase("open")){
				select();
			}
		}
	}
}
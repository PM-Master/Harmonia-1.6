/*
 * MedRecord.java
 *
 * Created on October 26, 2007, 1:56 PM
 */
package gov.nist.csd.pm.application.medrec;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.model.ObjectAttribute;
import gov.nist.csd.pm.common.model.ObjectAttributes;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.application.medrec.editors.DiagnosisEditor;
import gov.nist.csd.pm.application.medrec.editors.HistoryEditor;
import gov.nist.csd.pm.application.medrec.editors.TreatEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import java.util.List;

import static gov.nist.csd.pm.common.util.Generators.generateRandomName;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:01 $
 * @since 1.5
 */
public class MREditor extends JFrame implements ActionListener {
	public static final String MREC_PREFIX = "MREC";

	/**
	 * @uml.property  name="constraints"
	 */
	private GridBagConstraints constraints = new GridBagConstraints();
	/**
	 * @uml.property  name="sProcId"
	 */
	private String sProcId;
	/**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
	private SysCaller sysCaller;
	/**
	 * @uml.property  name="tfMrn"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfMrn;
	/**
	 * @uml.property  name="comboDr"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	private JComboBox comboDr;
	/**
	 * @uml.property  name="tfFirst"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfFirst;
	/**
	 * @uml.property  name="tfMi"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfMi;
	/**
	 * @uml.property  name="tfLast"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfLast;
	/**
	 * @uml.property  name="tfSsn"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfSsn;
	/**
	 * @uml.property  name="butM"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JRadioButton butM;
	/**
	 * @uml.property  name="butF"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JRadioButton butF;
	/**
	 * @uml.property  name="tfDob"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfDob;
	/**
	 * @uml.property  name="butSingle"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JRadioButton butSingle;
	/**
	 * @uml.property  name="butMarried"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JRadioButton butMarried;
	/**
	 * @uml.property  name="butDivorced"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JRadioButton butDivorced;
	/**
	 * @uml.property  name="butWidowed"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JRadioButton butWidowed;
	/**
	 * @uml.property  name="tfAddr"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfAddr;
	/**
	 * @uml.property  name="tfWork"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfWork;
	/**
	 * @uml.property  name="tfHome"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfHome;
	/**
	 * @uml.property  name="butAllergies"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butAllergies;
	/**
	 * @uml.property  name="butHistory"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butHistory;
	/**
	 * @uml.property  name="butSymptoms"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butSymptoms;
	/**
	 * @uml.property  name="butDiag"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butDiag;
	/**
	 * @uml.property  name="butTreatment"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butTreatment;
	/**
	 * @uml.property  name="tplEditor"
	 * @uml.associationEnd  
	 */
	private TemplateEditor tplEditor;
	/**
	 * @uml.property  name="mrSelector"
	 * @uml.associationEnd  
	 */
	private MRSelector mrSelector;
	public static final String MR_KEY_SSN = "ssn";
	public static final String MR_KEY_MRN = "mrn";
	public static final String MR_KEY_LASTNAME = "last name";
	public static final String MR_KEY_HOMEPHONE = "home phone";
	/**
	 * @uml.property  name="tplKeys" multiplicity="(0 -1)" dimension="1"
	 */
	private String[] tplKeys = {MR_KEY_SSN, MR_KEY_MRN, MR_KEY_LASTNAME,
			MR_KEY_HOMEPHONE};
	/**
	 * @uml.property  name="tplContainers" multiplicity="(0 -1)" dimension="1"
	 */
	private String[] tplContainers = {"PatId", "PatBio", "PatAllergies",
			"PatHistory", "PatSymptoms", "PatDiag", "PatTreatment"};
	/**
	 * @uml.property  name="sKstorePath"
	 */
	String sKstorePath;
	/**
	 * @uml.property  name="sTstorePath"
	 */
	String sTstorePath;
	/**
	 * @uml.property  name="sRtfPath"
	 */
	String sRtfPath;
	/**
	 * @uml.property  name="sWkfPath"
	 */
	String sWkfPath;
	/**
	 * @uml.property  name="sEmlPath"
	 */
	String sEmlPath;
	/**
	 * @uml.property  name="sOffPath"
	 */
	String sOffPath;
	/**
	 * @uml.property  name="sTemplateName"
	 */
	private String sTemplateName;
	/**
	 * @uml.property  name="sTemplateId"
	 */
	private String sTemplateId;
	/**
	 * @uml.property  name="sCrtRecordName"
	 */
	private String sCrtRecordName;
	/**
	 * @uml.property  name="sIdentName"
	 */
	private String sIdentName;
	/**
	 * @uml.property  name="sBioName"
	 */
	private String sBioName;
	/**
	 * @uml.property  name="sAllergiesName"
	 */
	private String sAllergiesName;
	/**
	 * @uml.property  name="sAllergiesId"
	 */
	private String sAllergiesId;
	/**
	 * @uml.property  name="sHistoryName"
	 */
	private String sHistoryName;
	/**
	 * @uml.property  name="sHistoryId"
	 */
	private String sHistoryId;
	/**
	 * @uml.property  name="sSymptomsName"
	 */
	private String sSymptomsName;
	/**
	 * @uml.property  name="sSymptomsId"
	 */
	private String sSymptomsId;
	/**
	 * @uml.property  name="sDiagName"
	 */
	private String sDiagName;
	/**
	 * @uml.property  name="sDiagId"
	 */
	private String sDiagId;
	/**
	 * @uml.property  name="sTreatmentName"
	 */
	private String sTreatmentName;
	/**
	 * @uml.property  name="sTreatmentId"
	 */
	private String sTreatmentId;

	private TreatEditor treat;

	public static String theMrn;

	@SuppressWarnings("LeakingThisInConstructor")
	public MREditor(int nSimPort, String sSessId, String sProcId, boolean bDebug) {
		super("Medical Record Editor");

		this.sProcId = sProcId;
		//IOC Candidate
		sysCaller = new SysCallerImpl(nSimPort, sSessId, sProcId, bDebug, MREC_PREFIX);
		tplEditor = new TemplateEditor(this, sysCaller, sSessId);
		tplEditor.pack();
		mrSelector = new MRSelector(this, sysCaller, sSessId);
		mrSelector.pack();

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent we) {
				terminate(0);
			}
		});

		// Start building the GUI
		tfMrn = new JTextField(10);
		tfMrn.setEditable(false);
		JPanel paneMrn = new JPanel();
		// paneMrn.add(lblMrn);
		paneMrn.add(tfMrn);
		paneMrn.setPreferredSize(new Dimension(200, 60));
		paneMrn.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 0, 0),
				BorderFactory.createTitledBorder("Medical Record Number")));

		comboDr = new JComboBox();
		comboDr.setPreferredSize(new Dimension(200, 20));
		JPanel paneDr = new JPanel();
		paneDr.add(comboDr);
		paneDr.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 0, 0),
				BorderFactory.createTitledBorder("Assigned to Dr.")));

		JPanel paneHeader = new JPanel(new GridLayout(1, 2));
		paneHeader.add(paneMrn);
		paneHeader.add(paneDr);

		JLabel lblFirst = new JLabel("First name:");
		tfFirst = new JTextField(10);
		JLabel lblMi = new JLabel("MI:    ");
		tfMi = new JTextField(3);
		JLabel lblLast = new JLabel("Last name:");
		tfLast = new JTextField(12);

		JPanel paneName = new JPanel(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(paneName, lblFirst, 0, 0, 1, 1);
		addComp(paneName, tfFirst, 0, 1, 3, 1);

		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(paneName, lblMi, 3, 0, 1, 1);
		addComp(paneName, tfMi, 3, 1, 1, 1);

		addComp(paneName, lblLast, 4, 0, 1, 1);
		addComp(paneName, tfLast, 4, 1, 3, 1);

		//JLabel lblSsn = new JLabel("SSN:         ");
		//tfSsn = new JTextField(10);

		JLabel lblSex = new JLabel("Sex:       ");
		butM = new JRadioButton("M");
		butF = new JRadioButton("F");
		ButtonGroup group = new ButtonGroup();
		group.add(butM);
		group.add(butF);
		JPanel paneSex = new JPanel(new GridLayout(1, 2));
		paneSex.add(butM);
		paneSex.add(butF);

		JLabel lblDob = new JLabel("Date of Birth:");
		tfDob = new JTextField(9);

		JPanel paneSsd = new JPanel(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		//addComp(paneSsd, lblSsn, 0, 0, 1, 1);
		//addComp(paneSsd, tfSsn, 0, 1, 3, 1);

		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(paneSsd, lblSex, 3, 0, 1, 1);
		addComp(paneSsd, paneSex, 3, 1, 1, 1);

		addComp(paneSsd, lblDob, 4, 0, 1, 1);
		addComp(paneSsd, tfDob, 4, 1, 3, 1);

		JPanel paneId = new JPanel(new GridLayout(2, 1));
		paneId.add(paneName);
		paneId.add(paneSsd);
		paneId.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 0, 0),
				BorderFactory.createTitledBorder("Patient Identification")));

		JLabel lblMarital = new JLabel("Marital status:");
		butSingle = new JRadioButton("Single");
		butMarried = new JRadioButton("Married");
		butDivorced = new JRadioButton("Divorced");
		butWidowed = new JRadioButton("Widowed");
		ButtonGroup groupMarital = new ButtonGroup();
		groupMarital.add(butSingle);
		groupMarital.add(butMarried);
		groupMarital.add(butDivorced);
		groupMarital.add(butWidowed);
		JPanel paneMarital = new JPanel(new GridLayout(1, 4));
		paneMarital.add(butSingle);
		paneMarital.add(butMarried);
		paneMarital.add(butDivorced);
		paneMarital.add(butWidowed);

		JLabel lblAddr = new JLabel("Address:      ");
		tfAddr = new JTextField(28);

		JLabel lblTel = new JLabel("Telephone:    ");
		JLabel lblWork = new JLabel("Work:");
		tfWork = new JTextField(9);
		JLabel lblHome = new JLabel("Home:");
		tfHome = new JTextField(9);

		JPanel paneTel = new JPanel(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(paneTel, lblWork, 0, 0, 1, 1);
		addComp(paneTel, tfWork, 1, 0, 1, 1);
		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(paneTel, lblHome, 2, 0, 1, 1);
		addComp(paneTel, tfHome, 3, 0, 1, 1);

		JLabel lblSsn = new JLabel("SSN:");
		tfSsn = new JTextField(10);

		JPanel paneSsn = new JPanel(new FlowLayout());
		constraints.insets = new Insets(10, 0, 0, 0);
		addComp(paneSsn, lblSsn, 0, 0, 1, 1);
		addComp(paneSsn, tfSsn, 0, 0, 1, 1);

		JPanel paneBio = new JPanel(new GridBagLayout());
		/*constraints.insets = new Insets(10, 0, 0, 0);
		addComp(paneBio, lblSsn, 0, 0, 1, 1);
		addComp(paneBio, tfSsn, 0, 0, 1, 1);*/

		addComp(paneBio, paneSsn, 0, 0, 1, 1);

		constraints.insets = new Insets(10, 0, 0, 0);
		addComp(paneBio, lblMarital, 0, 0, 1, 1);
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(paneBio, paneMarital, 0, 1, 3, 1);

		constraints.insets = new Insets(10, 0, 0, 0);
		addComp(paneBio, lblAddr, 0, 2, 1, 1);
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(paneBio, tfAddr, 0, 3, 3, 1);

		constraints.insets = new Insets(10, 0, 0, 0);
		addComp(paneBio, lblTel, 0, 4, 1, 1);
		addComp(paneBio, paneTel, 0, 5, 3, 1);

		paneBio.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 0, 0),
				BorderFactory.createTitledBorder("Biographical Data")));

		butAllergies = new JButton("Allergies");
		butAllergies.addActionListener(this);
		butAllergies.setActionCommand("opencomp");
		butHistory = new JButton("History");
		butHistory.addActionListener(this);
		butHistory.setActionCommand("opencomp");
		butSymptoms = new JButton("Symptoms");
		butSymptoms.addActionListener(this);
		butSymptoms.setActionCommand("opencomp");
		butDiag = new JButton("Diagnosis");
		butDiag.addActionListener(this);
		butDiag.setActionCommand("opencomp");
		butTreatment = new JButton("Treatment");
		butTreatment.addActionListener(this);
		butTreatment.setActionCommand("opencomp");

		JPanel paneButtons = new JPanel(new GridLayout(1, 5));
		paneButtons.add(butAllergies);
		paneButtons.add(butHistory);
		paneButtons.add(butSymptoms);
		paneButtons.add(butDiag);
		paneButtons.add(butTreatment);

		paneButtons.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0), BorderFactory.createTitledBorder("Medical History and Diagnosis")));

		Container vertBox = Box.createVerticalBox();
		vertBox.add(paneHeader);
		vertBox.add(paneId);
		vertBox.add(paneBio);
		vertBox.add(paneButtons);

		JPanel thePane = new JPanel();
		thePane.add(vertBox, BorderLayout.CENTER);

		setContentPane(thePane);

		JMenu fileMenu = createFileMenu();
		JMenu editMenu = createEditMenu();
		JMenu cfgMenu = createCfgMenu();

		JMenuBar mb = new JMenuBar();
		mb.add(fileMenu);
		mb.add(editMenu);
		mb.add(cfgMenu);
		setJMenuBar(mb);

		Packet res = sysCaller.getUsersOf("Doctor");
		if (res == null) {
			JOptionPane.showMessageDialog(this,
					"Null result from getUsersOf(Doctor)!");
			return;
		}

		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}
		DefaultComboBoxModel drBoxModel = (DefaultComboBoxModel) comboDr.getModel();
		drBoxModel.removeAllElements();
		for (int i = 0; i < res.size(); i++) {
			String sLine = res.getStringValue(i);
			String[] pieces = sLine.split(SysCaller.PM_FIELD_DELIM);

			int index = SysCallerImpl.getIndex(comboDr, pieces[0]);
			drBoxModel.insertElementAt(pieces[0], index);
		}

		// Get the keystores' paths.
		res = sysCaller.getKStorePaths();
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}
		sKstorePath = res.getStringValue(0);
		sTstorePath = res.getStringValue(1);
		System.out.println("Kstore path = " + sKstorePath);
		System.out.println("Tstore path = " + sTstorePath);

		sRtfPath = sysCaller.getAppPath("Rich Text Editor")[0];
		sWkfPath = sysCaller.getAppPath("Workflow Editor")[0];
		sEmlPath = sysCaller.getAppPath("e-grant")[0];
		sOffPath = sysCaller.getAppPath("Open Office")[0];

		System.out.println("RTF path = " + sRtfPath);
		System.out.println("Wkf path = " + sWkfPath);
		System.out.println("Eml path = " + sEmlPath);
		System.out.println("Off path = " + sOffPath);
	}

	private JMenu createFileMenu() {
		JMenu menu = new JMenu("Medical Record");

		JMenuItem item = new JMenuItem("Open");
		item.addActionListener(this);
		menu.add(item);

		item = new JMenuItem("New");
		item.addActionListener(this);
		menu.add(item);

		menu.addSeparator();

		item = new JMenuItem("Save");
		item.addActionListener(this);
		menu.add(item);

		menu.addSeparator();

		item = new JMenuItem("Delete");
		item.addActionListener(this);
		menu.add(item);

		menu.addSeparator();

		item = new JMenuItem("Print");
		item.addActionListener(this);
		menu.add(item);

		menu.addSeparator();

		item = new JMenuItem("Exit");
		item.addActionListener(this);
		menu.add(item);

		return menu;
	}

	private JMenu createEditMenu() {
		JMenu menu = new JMenu("Edit");

		JMenuItem item = new JMenuItem("Copy");
		item.addActionListener(this);
		menu.add(item);

		item = new JMenuItem("Paste");
		item.addActionListener(this);
		menu.add(item);

		return menu;
	}

	private JMenu createCfgMenu() {
		JMenu menu = new JMenu("Configure");

		JMenuItem item = new JMenuItem("Create template");
		item.addActionListener(this);
		menu.add(item);

		item = new JMenuItem("Select template");
		item.addActionListener(this);
		menu.add(item);

		return menu;
	}

	private void addComp(Container container, Component component, int x,
			int y, int w, int h) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		container.add(component, constraints);
	}

	private void openComp(ActionEvent e) {
		String sObjName = null;
		String sObjId = null;
		Object src = e.getSource();

		if (src.equals(butAllergies)) {
			sObjName = sAllergiesName;
			sObjId = sAllergiesId;
		} else if (src.equals(butHistory)) {
			//sObjName = sHistoryName;
			//sObjId = sHistoryId;
			sObjName = "history";
		} else if (src.equals(butSymptoms)) {
			sObjName = sSymptomsName;
			sObjId = sSymptomsId;
		} else if (src.equals(butDiag)) {
			//sObjName = sDiagName;
			//sObjId = sDiagId;
			sObjName = "diagnoses";
		} else if (src.equals(butTreatment)) {
			//sObjName = sTreatmentName;
			//sObjId = sTreatmentId;
			sObjName = "treatments";
			System.out.println("treatment button");
		}
		System.out.println("OBJECT NAME: " + sObjName);
		if(sObjName.equals("treatments")){
			System.out.println(tfMrn.getText());
			createNewTreat();
			return;
		}else if(sObjName.equals("diagnoses")){
			createNewDiagnosis();
			return;
		}else if(sObjName.equals("history")){
			System.out.println(sObjName + " = object name 2 history");
			createNewHistory();
			return;
		}

		// Let's use the object name, because the obj id is null for the
		// component objects
		// just created. Note that getObjInfo() accepts also names.
		System.out.println("You're trying to open object " + sObjName
				+ " with id " + sObjId);
		if (sObjName == null) {
			JOptionPane.showMessageDialog(this, "Trying to open a null object!");
			return;
		}
		Packet res = sysCaller.getObjInfo(sObjName);

		// Invoke the application that corresponds to the file object type.
		String s = res.getStringValue(0);
		String[] pieces = s.split(SysCaller.PM_ALT_DELIM_PATTERN);
		String sClass = pieces[2];
		String sNameOrHost = null;
		if (pieces.length >= 5) {
			sNameOrHost = pieces[4];
		}
		String sIdOrPath = null;
		if (pieces.length >= 6) {
			sIdOrPath = pieces[5];
		}
		System.out.println(sObjName + " = object name");
		if (sClass.equalsIgnoreCase(SysCaller.PM_CLASS_FILE_NAME)) {
			if (sIdOrPath.toLowerCase().endsWith(".rtf")) {
				// invokeRtfEditor(sObjName);

				if(sObjName.equals("treatments")){
					System.out.println(tfMrn.getText());
					createNewTreat();
				}else if(sObjName.equals("diagnoses")){
					createNewDiagnosis();
				}else{
					invokeRtfEditor(sObjName);
				}
			} else if(sObjName.equals("history")){
				System.out.println(sObjName + " = object name 2 history");
				createNewHistory();
			}else if (sIdOrPath.toLowerCase().endsWith(".wkf")) {

				invokeWorkflow(sObjName);

			} else if (sIdOrPath.toLowerCase().endsWith(".doc")
					|| sIdOrPath.toLowerCase().endsWith(".ppt")
					|| sIdOrPath.toLowerCase().endsWith(".xls")) {
				String sObjType = sIdOrPath.substring(sIdOrPath.length() - 3);
				invokeOffice(sObjType, sObjName);

				// doesn't happen
			} else if (sIdOrPath.toLowerCase().endsWith(".eml")) {
				invokeEmailer(sObjName);

			}
		}
	}

	public void createNewTreat(){
		TreatEditor treat = new TreatEditor(theMrn, simport, sessid, pid, debug);
		String sex = "";
		if(butM.isSelected()){
			sex = "M";
		}else{
			sex = "F";
		}
		String mrn = theMrn;
		//JOptionPane.showMessageDialog(this, mrn);
		treat.setMrn(mrn);
		treat.setProps(tfFirst.getText(), tfMi.getText(), 
				tfLast.getText(), tfSsn.getText(), sex, tfDob.getText(), 
				(String)comboDr.getSelectedItem());
		//treat.setMrn(tfMrn.getText());
		System.out.println(tfMrn.getText().length());
		if(tfMrn.getText().length() == 0){
			treat.disableButtons();
			return;
		}

		//treat.setVisible(true);
	}

	public void createNewDiagnosis(){
		DiagnosisEditor diag = new DiagnosisEditor(theMrn, simport, sessid, pid, debug);
		String sex = "";
		if(butM.isSelected()){
			sex = "M";
		}else{
			sex = "F";
		}
		String mrn = theMrn;
		//JOptionPane.showMessageDialog(this, mrn);

		diag.setMrn(mrn);;
		diag.setProps(tfFirst.getText(), tfMi.getText(), 
				tfLast.getText(), tfSsn.getText(), sex, tfDob.getText(), 
				(String)comboDr.getSelectedItem());
		//diag.setMrn(tfMrn.getText());
		if(tfMrn.getText().length() == 0){
			diag.disableButtons();
			return;
		}
		//diag.setVisible(true);
	}

	public void createNewHistory(){
		HistoryEditor hist = new HistoryEditor(theMrn, simport, sessid, pid, debug);
		String sex = "";
		if(butM.isSelected()){
			sex = "M";
		}else{
			sex = "F";
		}
		String mrn = theMrn;
		//JOptionPane.showMessageDialog(this, mrn);

		hist.setMrn(mrn);
		hist.setProps(tfFirst.getText(), tfMi.getText(), 
				tfLast.getText(), tfSsn.getText(), sex, tfDob.getText(), 
				(String)comboDr.getSelectedItem());
		//hist.setMrn(tfMrn.getText());
		if(tfMrn.getText().length() == 0){
			hist.disableButtons();
			return;
		}
		//hist.setVisible(true);
	}

	public void setMrnNumber(String mrn){
		theMrn = mrn;
	}

	private void invokeRtfEditor(String sObjName) {
		System.out.println("RTF editor invoked!");
		if (sRtfPath == null || sRtfPath.length() == 0) {
			JOptionPane.showMessageDialog(
					this,
					"The RTF Editor path is not set. Please configure the Session Manager and try again!");
			return;
		}

		if (sKstorePath == null || sKstorePath.length() == 0
				|| sTstorePath == null || sTstorePath.length() == 0) {
			JOptionPane.showMessageDialog(
					this,
					"At least one certificate store path is not set. Please configure the session and try again!");
			return;
		}

		String sProcIdLocal = sysCaller.createProcess();
		if (sProcIdLocal == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}

		StringBuilder sb = new StringBuilder().append("javaw -cp ").append("\"").append(sRtfPath).append("\"").append(" -Djavax.net.ssl.keyStore=").append("\"").append(sKstorePath).append("\"")
				.append(" -Djavax.net.ssl.keyStorePassword=aaaaaa ")
				.append(" -Djavax.net.ssl.trustStore=")
				.append("\"")
				.append(sTstorePath)
				.append("\"")
				.append(" gov.nist.csd.pm.application.rtf.RTFEditor ")
				.append(" -session ")
				.append(sysCaller.getSessionId())
				.append(" -process ")
				.append(sProcIdLocal);
		if (sObjName != null) {
			sb = sb.append(" \"").append(sObjName).append("\"");
		}

		LauncherThread et = new LauncherThread(sb.toString(), "RTF-");
		// threadSet.add(et);

		// lt.setDaemon(true);
		et.start();
	}

	private void invokeWorkflow(String sObjName) {
		System.out.println("Wkf invoked!");
	}

	private void invokeOffice(String sObjType, String sObjName) {
		System.out.println("OpenOffice invoked!");
		if (sOffPath == null || sOffPath.length() == 0) {
			JOptionPane.showMessageDialog(
					this,
					"The Office Launcher's path is not set. Please configure the Session Manager and try again!");
			return;
		}

		if (sKstorePath == null || sKstorePath.length() == 0
				|| sTstorePath == null || sTstorePath.length() == 0) {
			JOptionPane.showMessageDialog(
					this,
					"At least one certificate store path is not set. Please configure the session and try again!");
			return;
		}

		String sProcIdLocal = sysCaller.createProcess();
		if (sProcIdLocal == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}

		StringBuilder sb = new StringBuilder().append("javaw -cp ").append("\"").append(sOffPath).append("\"").append(" -Djavax.net.ssl.keyStore=").append("\"").append(sKstorePath).append("\"")
				.append(" -Djavax.net.ssl.keyStorePassword=aaaaaa ")
				.append(" -Djavax.net.ssl.trustStore=")
				.append("\"")
				.append(sTstorePath)
				.append("\"")
				.append(" gov.nist.csd.pm.application.openoffice.OfficeLauncher")
				.append(" -session ")
				.append(sysCaller.getSessionId())
				.append(" -process ")
				.append(sProcIdLocal);
		if (sObjType != null) {
			sb.append(" -objtype ").append(sObjType);
		}
		if (sObjName != null) {
			sb.append(" \"").append(sObjName).append("\"");
		}

		System.out.println(sb.toString());

		LauncherThread et = new LauncherThread(sb.toString(), "Office-");
		// threadSet.add(et);

		// lt.setDaemon(true);
		et.start();
	}

	private void invokeEmailer(String sObjName) {
		System.out.println("Emailer invoked!");
	}

	// The user tries to open a composite object - a medical record. The user
	// must
	// supply the template and possibly a set of keys that characterize a small
	// set
	// of objects, then he/she must select the object to open from that set.
	private void openMr() {
		mrSelector.prepare();
		mrSelector.setVisible(true);
	}

	private void resetFields() {
		tfMrn.setText(null);
		tfFirst.setText(null);
		tfMi.setText(null);
		tfLast.setText(null);
		tfSsn.setText(null);
		tfDob.setText(null);
	}

	// Called from the MRSelector when the user selects a record container and
	// clicks on the MRSelector's OK button.
	public void displayRecord(String sRecName, String sRecId) {
		setTitle("Medical Record Editor - " + sRecName);
		theMrn = sRecName;
		resetFields();
		// Now get its properties.
		Packet res = sysCaller.getRecordInfo(sRecId);
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}

		// item 0: <name>:<id>
		// item 1: <tpl name>:<tpl id>
		// item 2: <comp count>
		// item 3 to 3 + <comp count> - 1: <comp name>:<comp id>
		// item 3 + <comp count>: <key count>
		// item 3 + <comp count> + 1 to 3 + <comp count> + 1 + <key count> - 1:
		// <key name>=<key value>

		String sLine = res.getStringValue(0);
		String[] pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
		System.out.println("Properties of composite object " + pieces[0]
				+ " of id " + pieces[1]);

		sLine = res.getStringValue(1);
		pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
		System.out.println("  Its template is " + pieces[0] + " of id "
				+ pieces[1]);

		sLine = res.getStringValue(2);
		int nComp = Integer.valueOf(sLine).intValue();
		System.out.println("  It has " + nComp + " components:");

		List<String> compIdList = new ArrayList<String>();
		List<String> compNameList = new ArrayList<String>();
		for (int i = 0; i < nComp; i++) {
			sLine = res.getStringValue(3 + i);
			pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
			compNameList.add(pieces[0]);
			compIdList.add(pieces[1]);
			System.out.println("    " + pieces[0] + " = " + pieces[1]);
		}

		// The keys.
		sLine = res.getStringValue(3 + nComp);
		int nKeys = Integer.valueOf(sLine).intValue();
		System.out.println("  It has " + nKeys + " keys:");

		for (int i = 0; i < nKeys; i++) {
			sLine = res.getStringValue(4 + nComp + i);
			System.out.println("    " + sLine);
		}

		// There should be 7 component objects.
		// First one is a .pid and contains properties name=value.
		openPatIdent(compNameList.get(0));
		openPatBio(compNameList.get(1));

		sCrtRecordName = sRecName;

		sIdentName = compNameList.get(0);
		sBioName = compNameList.get(1);
		sAllergiesName = compNameList.get(2);
		//sHistoryName = (String) compNameList.get(3);
		sSymptomsName = compNameList.get(3);
		//sDiagName = (String) compNameList.get(5);
		//sTreatmentName = (String) compNameList.get(6);


		sAllergiesId = compIdList.get(2);
		//sHistoryId = (String) compIdList.get(3);
		sSymptomsId = compIdList.get(3);
		//sDiagId = (String) compIdList.get(5);
		//sTreatmentId = (String) compIdList.get(6);
	}

	@SuppressWarnings("CallToThreadDumpStack")
	private void openPatIdent(String sObjName) {
		String sHandle = sysCaller.openObject3(sObjName, "File read");
		if (sHandle == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}

		// Reserve space for and read the object content.
		byte[] buf = sysCaller.readObject3(sHandle);
		if (buf == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		Properties props = new Properties();
		try {
			props.load(bais);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception when loading patient's id data");
			return;
		}

		for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
			String sName = (String) propEnum.nextElement();
			System.out.println(sName + "=" + (String) props.get(sName));
		}

		tfMrn.setText(props.getProperty("mrn"));
		comboDr.setSelectedItem(props.getProperty("dr"));
		tfFirst.setText(props.getProperty("first name"));
		tfLast.setText(props.getProperty("last name"));
		tfMi.setText(props.getProperty("mi"));
		if (props.getProperty("sex").equalsIgnoreCase("M")) {
			butM.setSelected(true);
		} else {
			butF.setSelected(true);
		}
		tfDob.setText(props.getProperty("dob"));
		tfSsn.setText(props.getProperty("ssn"));
	}

	@SuppressWarnings("CallToThreadDumpStack")
	private void openPatBio(String sObjName) {
		String sHandle = sysCaller.openObject3(sObjName, "File read");
		if (sHandle == null) {
			JOptionPane.showMessageDialog(this, "Error: " + sysCaller.getLastError());
			return;
		}

		// Reserve space for and read the object content.
		byte[] buf = sysCaller.readObject3(sHandle);
		if (buf == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			tfSsn.setText("");
			return;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		Properties props = new Properties();
		try {
			props.load(bais);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception when loading patient's bio data");
			return;
		}

		for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
			String sName = (String) propEnum.nextElement();
			System.out.println(sName + "=" + (String) props.get(sName));
		}

		String sMarital = props.getProperty("marital status");
		if (sMarital.equalsIgnoreCase("Single")) {
			butSingle.setSelected(true);
		} else if (sMarital.equalsIgnoreCase("Married")) {
			butMarried.setSelected(true);
		} else if (sMarital.equalsIgnoreCase("Divorced")) {
			butDivorced.setSelected(true);
		} else if (sMarital.equalsIgnoreCase("Widowed")) {
			butWidowed.setSelected(true);
		}
		tfAddr.setText(props.getProperty("address"));
		tfWork.setText(props.getProperty("work phone"));
		tfHome.setText(props.getProperty("home phone"));
	}

	private void newMr() {
		sCrtRecordName = null;

		sIdentName = null;
		sBioName = null;
		sAllergiesName = null;
		sHistoryName = null;
		sSymptomsName = null;
		sDiagName = null;
		sTreatmentName = null;

		sAllergiesId = null;
		sHistoryId = null;
		sSymptomsId = null;
		sDiagId = null;
		sTreatmentId = null;

		tfMrn.setText(null);
		tfFirst.setText(null);
		tfMi.setText(null);
		tfLast.setText(null);
		tfSsn.setText(null);
		butM.setSelected(true);
		tfDob.setText(null);
		butSingle.setSelected(true);
		tfAddr.setText(null);
		tfWork.setText(null);
		tfHome.setText(null);
		// generate record number
	}

	// If sCrtCompoName is null, then we have to create a new composite object:
	// Create the PatId object with generated name, write its content of type
	// text
	// extracted from the GUI.
	// Create the PatBio object with generated name, write its content of type
	// text
	// extracted from the GUI.
	// Create the other objects. Click on the allergy, you get "No file", enter
	// some
	// data, save it.
	// Create the composite object with a generated name, pointing to the 7
	// files,
	// and set the keys.
	//
	// If this is an existing object, do the same as before,
	// but use the old names. Set the keys - you'll have to delete the old ones
	// first.
	@SuppressWarnings("CallToThreadDumpStack")
	private void saveMr() {
		// A template needs to be selected.
		if (sTemplateId == null) {
			JOptionPane.showMessageDialog(this,
					"Please select the appropriate template using the Configure menu!");
			return;
		}

		// If this is a new object, generate a record number.
		if (sCrtRecordName == null) {
			tfMrn.setText(generateRandomName(6));
		}
		String sMrn = tfMrn.getText();

		// Check fields, etc.
		String sDoctor = (String) comboDr.getSelectedItem();
		if (sDoctor == null || sDoctor.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please select a doctor!");
			return;
		}

		String sFirst = tfFirst.getText().trim();
		if (sFirst.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter patient's first name!");
			return;
		}

		String sMi = tfMi.getText().trim();

		String sLast = tfLast.getText().trim();
		if (sLast.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter patient's last name!");
			return;
		}

		String sSsn = tfSsn.getText().trim();
		if (sSsn.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter patient's social security number!");
			return;
		}

		if (!butM.isSelected() && !butF.isSelected()) {
			JOptionPane.showMessageDialog(this,
					"Please select the patient's sex!");
			return;
		}
		String sSex = (butM.isSelected()) ? "M" : "F";

		String sDob = tfDob.getText().trim();
		if (sDob.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter patient's date of birth!");
			return;
		}
		if (!butSingle.isSelected() && !butMarried.isSelected()
				&& !butDivorced.isSelected() && !butWidowed.isSelected()) {
			JOptionPane.showMessageDialog(this,
					"Please select the patient's marital status!");
			return;
		}
		String sMarital = (butSingle.isSelected()) ? "Single" : ((butMarried.isSelected()) ? "Married"
				: ((butDivorced.isSelected()) ? "Divorced" : "Widowed"));

		String sAddr = tfAddr.getText().trim();
		if (sAddr.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter patient's address!");
			return;
		}

		String sWork = tfWork.getText().trim();
		String sHome = tfHome.getText().trim();

		// If this is a new composite object:
		if (sCrtRecordName == null) {

			// Create and write the PatId object.
			sIdentName = generateRandomName(4);
			String sHandle = sysCaller.createObject3(sIdentName, "File", "pid",
					"b|PatId", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			Properties props = new Properties();
			props.put("mrn", sMrn);
			props.put("dr", sDoctor);
			props.put("first name", sFirst);
			props.put("mi", sMi);
			props.put("last name", sLast);
			props.put("sex", sSex);
			props.put("ssn", sSsn);
			props.put("dob", sDob);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				props.store(baos, "Patient id data");
				byte[] buf = baos.toByteArray();
				int ret = sysCaller.writeObject3(sHandle, buf);
				if (ret < 0) {
					JOptionPane.showMessageDialog(this,
							sysCaller.getLastError());
					return;
				}
				sysCaller.closeObject(sHandle);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						"Exception while saving object: " + e.getMessage());
				return;
			}

			// Create and write the PatBio object.
			sBioName = generateRandomName(4);
			sHandle = sysCaller.createObject3(sBioName, "File", "bio",
					"b|PatBio", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			props.clear();
			props.put("marital status", sMarital);
			props.put("address", sAddr);
			props.put("work phone", sWork);
			props.put("home phone", sHome);

			baos = new ByteArrayOutputStream();

			try {
				props.store(baos, "Patient bio data");
				byte[] buf = baos.toByteArray();
				int ret = sysCaller.writeObject3(sHandle, buf);
				if (ret < 0) {
					JOptionPane.showMessageDialog(this,
							sysCaller.getLastError());
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						"Exception while saving object: " + e.getMessage());
				return;
			}

			// Create the PatAllergies, PatHistory, PatSymptoms, PatDiag,
			// PatTreatment objects.
			sAllergiesName = generateRandomName(4);
			sHandle = sysCaller.createObject3(sAllergiesName, "File", "rtf",
					"b|PatAllergies", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			sSymptomsName = generateRandomName(4);
			sHandle = sysCaller.createObject3(sSymptomsName, "File", "rtf",
					"b|PatSymptoms", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			/*sHistoryName = generateRandomName(4);
			sHandle = sysCaller.createObject3(sHistoryName, "File", "doc",
					"b|PatHistory", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			sDiagName = generateRandomName(4);
			sHandle = sysCaller.createObject3(sDiagName, "File", "rtf",
					"b|PatDiag", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			sTreatmentName = generateRandomName(4);
			sHandle = sysCaller.createObject3(sTreatmentName, "File", "rtf",
					"b|PatTreatment", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}*/


			// Prepare for the creation of the record container.
			//TODO this is where we ask the user to specify a medical record name
			String sContainerName = JOptionPane.showInputDialog(this, 
					"Specify Medical Record name or hit cancel for a random one.", "Save As", 
					JOptionPane.OK_OPTION);//generateRandomName(4);
			if(sContainerName == null){
				sContainerName = generateRandomName(4);
			}
			setMrnNumber(sContainerName);
			String sComponents = sIdentName + SysCaller.PM_FIELD_DELIM
					+ sBioName + SysCaller.PM_FIELD_DELIM + sAllergiesName
					+ SysCaller.PM_FIELD_DELIM + sSymptomsName;
			//+ SysCaller.PM_FIELD_DELIM + sHistoryName
			//+ SysCaller.PM_FIELD_DELIM + sDiagName
			//+ SysCaller.PM_FIELD_DELIM + sTreatmentName;
			HashMap keyMap = new HashMap();
			keyMap.put(MR_KEY_MRN, sMrn);
			keyMap.put(MR_KEY_SSN, sSsn);
			keyMap.put(MR_KEY_LASTNAME, sLast);
			keyMap.put(MR_KEY_HOMEPHONE, sHome);

			// Create the record container in corresponding doctor's medical
			// records container.
			theMrn = sContainerName;
			Packet res = sysCaller.createRecordInEntityWithProp(
					sContainerName, "patientsof=" + sDoctor,
					SysCaller.PM_NODE_OATTR, sTemplateId, sComponents, keyMap);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////
			try{
				createMrOattrs(sContainerName, "Treatments");
				createMrOattrs(sContainerName, "Diagnoses");
				createMrOattrs(sContainerName, "History");
				createDraftOattrs(sContainerName);
			}catch(Exception e){
				System.out.println("creating new container didn't work");
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				e.printStackTrace();
				return;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////

			// Assign all component objects (the record fields) to the container
			// just created. These fields are already in the right columns.
			boolean bRes = sysCaller.assignObjToContainer(sIdentName,
					sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			bRes = sysCaller.assignObjToContainer(sBioName, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			bRes = sysCaller.assignObjToContainer(sAllergiesName,
					sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			bRes = sysCaller.assignObjToContainer(sSymptomsName, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			/*bRes = sysCaller.assignObjToContainer(sHistoryName, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			bRes = sysCaller.assignObjToContainer(sDiagName, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			bRes = sysCaller.assignObjToContainer(sTreatmentName,
					sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}*/
		} else {
			// Reuse the sIdentName, sBioName, sAllergiesName, sHistoryName,
			// sDiagName,
			// sTreatmentName, which should not be null. Open and write the
			// component
			// objects sIdentName and sBioName.

			// Write the PatId object.
			if (sIdentName == null) {
				JOptionPane.showMessageDialog(this,
						"The patient id object name is null!");
				return;
			}
			String sHandle = sysCaller.openObject3(sIdentName, "File write");
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			Properties props = new Properties();
			props.put("mrn", sMrn);
			props.put("dr", sDoctor);
			props.put("first name", sFirst);
			props.put("mi", sMi);
			props.put("last name", sLast);
			props.put("sex", sSex);
			props.put("ssn", sSsn);
			props.put("dob", sDob);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				props.store(baos, "Patient id data");
				byte[] buf = baos.toByteArray();
				int ret = sysCaller.writeObject3(sHandle, buf);
				if (ret < 0) {
					JOptionPane.showMessageDialog(this,
							sysCaller.getLastError());
					return;
				}
				sysCaller.closeObject(sHandle);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						"Exception while saving object: " + e.getMessage());
				return;
			}

			HashMap keyMap = new HashMap();
			keyMap.put(MR_KEY_MRN, sMrn);
			keyMap.put(MR_KEY_SSN, sSsn);
			keyMap.put(MR_KEY_LASTNAME, sLast);

			if (!sysCaller.setRecordKeys(sCrtRecordName, keyMap)) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			// Write the PatBio object.
			if (sBioName == null) {
				JOptionPane.showMessageDialog(this,
						"The patient bio object name is null!");
				return;
			}
			sHandle = sysCaller.openObject3(sBioName, "File write");
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			props.clear();
			props.put("marital status", sMarital);
			props.put("address", sAddr);
			props.put("work phone", sWork);
			props.put("home phone", sHome);

			baos = new ByteArrayOutputStream();

			try {
				props.store(baos, "Patient bio data");
				byte[] buf = baos.toByteArray();
				int ret = sysCaller.writeObject3(sHandle, buf);
				if (ret < 0) {
					JOptionPane.showMessageDialog(this,
							sysCaller.getLastError());
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this,
						"Exception while saving object: " + e.getMessage());
				return;
			}

			keyMap.clear();
			keyMap.put(MR_KEY_HOMEPHONE, sHome);

			if (!sysCaller.addRecordKeys(sCrtRecordName, keyMap)) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

		}
		JOptionPane.showMessageDialog(this, "Medical Record Saved");
	}


	public void createMrOattrs(String sContainerName, String type){
		String sNewContainer = "";
		try{
			PmNode newNode, parent;
			ObjectAttribute containerSpec, parentAttribute;

			newNode = PmNode.createObjectAttributeNode(sContainerName + "-" + type);

			parent = new PmNode(PmNodeType.OBJECT_ATTRIBUTE, sContainerName);

			containerSpec = ObjectAttributes.createFromPmNode(newNode);
			parentAttribute = ObjectAttributes.createFromPmNode(parent, sysCaller);

			sNewContainer = sysCaller.addContainer(containerSpec, parentAttribute);
			System.out.println(type + "CONTAINER: " + sNewContainer);

			String nodeName = newNode.getName();
			System.out.println("NODE NAME:" + nodeName);

			String nodeType = newNode.getType();
			System.out.println("TYPE:" + type);

			String id = sysCaller.getIdOfEntityWithNameAndType(nodeName, nodeType);
			System.out.println("ID" + id);

			boolean setProp = sysCaller.addPropToOattr(id, "no", type, sContainerName);
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

	public void createDraftOattrs(String sContainerName){
		String draftContainerName = "";
		String sDraftContainer = "";
		try{
			PmNode draftNode = PmNode.createObjectAttributeNode(sContainerName + "-Drafts");
			draftContainerName = draftNode.getName();
			System.out.println("DRAFT CONTAINER NAME: " + draftContainerName);

			PmNode parent = new PmNode(PmNodeType.OBJECT_ATTRIBUTE, sContainerName);

			ObjectAttribute containerSpec = ObjectAttributes.createFromPmNode(draftNode);
			ObjectAttribute parentAttribute = ObjectAttributes.createFromPmNode(parent, sysCaller);

			sDraftContainer = sysCaller.addContainer(containerSpec, parentAttribute);
			System.out.println("DRAFT CONTAINER:" + sDraftContainer);

			String nodeName = draftNode.getName();
			System.out.println("NODE NAME:" + nodeName);

			String type = draftNode.getType();
			System.out.println("TYPE:" + type);

			String id = sysCaller.getIdOfEntityWithNameAndType(nodeName, type);
			System.out.println("ID" + id);

			boolean setProp = sysCaller.addPropToOattr(id, "no", "Drafts", sContainerName);
			if(!setProp){
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				System.out.println("set prop didnt work");
				return;
			}


		}catch(Exception e){
			System.out.println("creating draft container didn't work");
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			e.printStackTrace();
			return;
		}
		String sDraftTreatContainer = "";
		try{
			PmNode draftTreatNode = PmNode.createObjectAttributeNode(sContainerName + "-DraftTreatments");

			PmNode parent = new PmNode(PmNodeType.OBJECT_ATTRIBUTE, draftContainerName);

			ObjectAttribute containerSpec = ObjectAttributes.createFromPmNode(draftTreatNode);
			ObjectAttribute parentAttribute = ObjectAttributes.createFromPmNode(parent, sysCaller);

			sDraftTreatContainer = sysCaller.addContainer(containerSpec, parentAttribute);
			System.out.println("DRAFTTREATMENT CONTAINER:" + sDraftTreatContainer);

			String nodeName = draftTreatNode.getName();
			System.out.println("NODE NAME:" + nodeName);

			String type = draftTreatNode.getType();
			System.out.println("TYPE:" + type);

			String id = sysCaller.getIdOfEntityWithNameAndType(nodeName, type);
			System.out.println("ID" + id);

			boolean setProp = sysCaller.addPropToOattr(id, "no", "DraftTreatments", sContainerName);
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
		String sDraftDiagContainer = "";
		try{
			PmNode draftDiagNode = PmNode.createObjectAttributeNode(sContainerName + "-DraftDiagnoses");

			PmNode parent = new PmNode(PmNodeType.OBJECT_ATTRIBUTE, draftContainerName);

			ObjectAttribute containerSpec = ObjectAttributes.createFromPmNode(draftDiagNode);
			ObjectAttribute parentAttribute = ObjectAttributes.createFromPmNode(parent, sysCaller);

			sDraftDiagContainer = sysCaller.addContainer(containerSpec, parentAttribute);
			System.out.println("DRAFTDIAGNOSES CONTAINER:" + sDraftDiagContainer);

			String nodeName = draftDiagNode.getName();
			System.out.println("NODE NAME:" + nodeName);

			String type = draftDiagNode.getType();
			System.out.println("TYPE:" + type);

			String id = sysCaller.getIdOfEntityWithNameAndType(nodeName, type);
			System.out.println("ID" + id);

			boolean setProp = sysCaller.addPropToOattr(id, "no", "DraftDiagnoses", sContainerName);
			if(!setProp){
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				System.out.println("set prop didnt work");
				return;
			}
		}catch(Exception e){
			System.out.println("creating draftdiagnoses container didn't work");
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			e.printStackTrace();
			return;
		}
		String sDraftHistContainer = "";
		try{
			PmNode draftHistNode = PmNode.createObjectAttributeNode(sContainerName + "-DraftHistory");

			PmNode parent = new PmNode(PmNodeType.OBJECT_ATTRIBUTE, draftContainerName);

			ObjectAttribute containerSpec = ObjectAttributes.createFromPmNode(draftHistNode);
			ObjectAttribute parentAttribute = ObjectAttributes.createFromPmNode(parent, sysCaller);

			sDraftHistContainer = sysCaller.addContainer(containerSpec, parentAttribute);
			System.out.println("DRAFTHISTORY CONTAINER:" + sDraftHistContainer);

			String nodeName = draftHistNode.getName();
			System.out.println("NODE NAME:" + nodeName);

			String type = draftHistNode.getType();
			System.out.println("TYPE:" + type);

			String id = sysCaller.getIdOfEntityWithNameAndType(nodeName, type);
			System.out.println("ID" + id);

			boolean setProp = sysCaller.addPropToOattr(id, "no", "DraftHistory", sContainerName);
			if(!setProp){
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				System.out.println("set prop didnt work");
				return;
			}
		}catch(Exception e){
			System.out.println("creating drafthistory container didn't work");
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			e.printStackTrace();
			return;
		}
	}

	private void terminate(int nCode) {
		sysCaller.exitProcess(sProcId);
		System.exit(nCode);
	}

	// Selects or creates a template for the medical records. Check the
	// template for the required containers: PatId, PatBio, PatAllergies,
	// PatHistory, PatSymptoms, PatDiag, PatTreatment.
	private void selectTemplate() {
		JComboBox tplBox = new JComboBox();
		tplBox.setPreferredSize(new Dimension(160, 20));

		List<String> tplList = new ArrayList<String>();

		Packet res = sysCaller.getTemplates();
		if (res == null) {
			JOptionPane.showMessageDialog(this,
					"Null return from getTemplates!");
			return;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}
		DefaultComboBoxModel tplBoxModel = (DefaultComboBoxModel) tplBox.getModel();
		tplBoxModel.removeAllElements();
		for (int i = 0; i < res.size(); i++) {
			String sLine = res.getStringValue(i);
			String[] pieces = sLine.split(SysCaller.PM_FIELD_DELIM);

			int index = SysCallerImpl.getIndex(tplBox, pieces[0]);
			tplBoxModel.insertElementAt(pieces[0], index);
			tplList.add(index, pieces[1]);
		}
		int ret = JOptionPane.showOptionDialog(null, new Object[]{
				"Please select a template!", tplBox}, "Select Template",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, null, null);
		if (ret != JOptionPane.OK_OPTION) {
			return;
		}

		int nSelIx = tplBox.getSelectedIndex();
		if (nSelIx < 0) {
			JOptionPane.showMessageDialog(this, "Please select a template!");
		}

		// Check the selected template for validity. This is the hard-coded
		// part.
		// There must be 7 containers:
		// PatId, PatBio, PatAllergies, PatHistory, PatSymptoms, PatDiag,
		// PatTreatment.
		res = sysCaller.getTemplateInfo(tplList.get(nSelIx));
		if (res == null) {
			JOptionPane.showMessageDialog(this,
					"Null result from getTemplateInfo!");
			return;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}
		if (res.size() < 3) {
			JOptionPane.showMessageDialog(this, "Bad template!");
			return;
		}
		String sLine = res.getStringValue(1);
		String[] pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
		if (pieces.length != tplContainers.length) {
			JOptionPane.showMessageDialog(this,
					"Invalid template - container count!");
			return;
		}
		for (int i = 0; i < pieces.length; i++) {
			String sContId = pieces[i];
			String sContName = sysCaller.getNameOfEntityWithIdAndType(sContId,
					SysCaller.PM_NODE_OATTR);
			if (!sContName.equalsIgnoreCase(tplContainers[i])) {
				JOptionPane.showMessageDialog(this,
						"Invalid template - container \"" + sContName + "\"!");
				return;
			}
		}

		// There must be 4 keys:
		// ssn, mrn, last name, home phone.
		sLine = res.getStringValue(2);
		if (sLine.length() == 0) {
			JOptionPane.showMessageDialog(this, "Invalid template - no keys!");
			return;
		}

		pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
		if (pieces.length != tplKeys.length) {
			JOptionPane.showMessageDialog(this, "Invalid template - key count!");
			return;
		}

		// Check that each key in tplKeys is in the template.
		for (int i = 0; i < tplKeys.length; i++) {
			String sKey = tplKeys[i];
			boolean found = false;
			for (int j = 0; j < pieces.length; j++) {
				if (sKey.equalsIgnoreCase(pieces[j])) {
					found = true;
					break;
				}
			}
			if (!found) {
				JOptionPane.showMessageDialog(this, "Invalid template - key \""
						+ sKey + "\" not in template!");
				return;
			}
		}

		sTemplateName = (String) tplBox.getItemAt(nSelIx);
		sTemplateId = tplList.get(nSelIx);
		System.out.println("You selected template " + sTemplateName
				+ " and id " + sTemplateId);

	}

	private void createTemplate() {
		tplEditor.prepare();
		tplEditor.setVisible(true);
	}

	private void processRecord(String sRecName) {
		if (sRecName == null) {
			return;
		}

		String sRecId = sysCaller.getIdOfEntityWithNameAndType(sRecName, SysCaller.PM_NODE_OATTR);
		if (sRecId == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}
		displayRecord(sRecName, sRecId);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("open")) {
			openMr();
		} else if (e.getActionCommand().equalsIgnoreCase("opencomp")) {
			openComp(e);
		} else if (e.getActionCommand().equalsIgnoreCase("new")) {
			newMr();
		} else if (e.getActionCommand().equalsIgnoreCase("save")) {
			saveMr();
		} else if (e.getActionCommand().equalsIgnoreCase("exit")) {
			terminate(0);
		} else if (e.getActionCommand().equalsIgnoreCase("create template")) {
			createTemplate();
		} else if (e.getActionCommand().equalsIgnoreCase("select template")) {
			selectTemplate();
		}
	}
	static String sessid;
	static String pid;
	static int simport;
	static String recname;
	static boolean debug;

	// Create the GUI,
	private static void createGUI() {
		MREditor editor = new MREditor(simport, sessid, pid, debug);
		editor.pack();
		editor.setVisible(true);
		editor.processRecord(recname);
	}

	// Arguments on the command line:
	// -debug -session <sessionId> -process <processId> -simport <simulator
	// port> <record name>
	// <simulator port> is the port where the kernel simulator listens for
	// connections,
	// by default 8081.
	// <sessionId> is the id of the session where the grantor is running. It is
	// mandatory.
	// <processId> is the id of the process in which the grantor is running. It
	// is
	// mandatory.
	// <recname> is the name of a record to view.
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-session")) {
				sessid = args[++i];
			} else if (args[i].equals("-process")) {
				pid = args[++i];
			} else if (args[i].equals("-simport")) {
				simport = Integer.valueOf(args[++i]).intValue();
			} else if (args[i].equals("-debug")) {
				debug = true;
			} else {
				recname = args[i];
			}
		}
		if (sessid == null) {
			System.out.println("This application must run within a Policy Machine session!");
			System.exit(-1);
		}
		if (pid == null) {
			System.out.println("This application must run in a Policy Machine process!");
			System.exit(-1);
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				createGUI();
			}
		});
	}

	class LauncherThread extends Thread {

		String sPrefix;
		String cmd;
		Process proc;

		LauncherThread(String cmd, String sPrefix) {
			this.sPrefix = sPrefix;
			this.cmd = cmd;
		}

		public Process getProcess() {
			return proc;
		}

		/*
		 * public void destroy() { proc.destroy(); }
		 */
		@Override
		@SuppressWarnings("CallToThreadDumpStack")
		public void run() {
			Runtime rt = Runtime.getRuntime();
			try {
				proc = rt.exec(cmd);
				StreamGobbler errGobbler = new StreamGobbler(
						proc.getErrorStream(), sPrefix);
				StreamGobbler outGobbler = new StreamGobbler(
						proc.getInputStream(), sPrefix);
				errGobbler.start();
				outGobbler.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class StreamGobbler extends Thread {

		InputStream is;
		String sPrefix;

		StreamGobbler(InputStream is, String sPrefix) {
			this.sPrefix = sPrefix;
			this.is = is;
		}

		@Override
		@SuppressWarnings("CallToThreadDumpStack")
		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(sPrefix + line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.out.println(sPrefix + ioe.getMessage());
			}
		}
	}
}

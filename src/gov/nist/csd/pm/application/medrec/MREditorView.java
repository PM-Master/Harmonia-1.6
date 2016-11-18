package gov.nist.csd.pm.application.medrec;

import gov.nist.csd.pm.common.application.SysCaller;

import javax.swing.*;

public class MREditorView extends JFrame {

	/**
	 * @uml.property  name="sSessId"
	 */
	private String sSessId;
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
	 * @uml.property  name="bDebug"
	 */
	private boolean bDebug;

	/**
	 * @uml.property  name="tfMrn"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JTextField tfMrn;

	/**
	 * @uml.property  name="comboDr"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JComboBox comboDr;

	/**
	 * @uml.property  name="tfFirst"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JTextField tfFirst;
	/**
	 * @uml.property  name="tfMi"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JTextField tfMi;
	/**
	 * @uml.property  name="tfLast"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JTextField tfLast;

	/**
	 * @uml.property  name="tfSsn"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JTextField tfSsn;

	/**
	 * @uml.property  name="butM"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JRadioButton butM;
	/**
	 * @uml.property  name="butF"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JRadioButton butF;

	/**
	 * @uml.property  name="tfDob"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JTextField tfDob;

	/**
	 * @uml.property  name="butSingle"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JRadioButton butSingle;
	/**
	 * @uml.property  name="butMarried"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JRadioButton butMarried;
	/**
	 * @uml.property  name="butDivorced"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JRadioButton butDivorced;
	/**
	 * @uml.property  name="butWidowed"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JRadioButton butWidowed;

	/**
	 * @uml.property  name="tfAddr"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JTextField tfAddr;

	/**
	 * @uml.property  name="tfWork"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JTextField tfWork;
	/**
	 * @uml.property  name="tfHome"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JTextField tfHome;

	/**
	 * @uml.property  name="butAllergies"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JButton butAllergies;
	/**
	 * @uml.property  name="butHistory"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JButton butHistory;
	/**
	 * @uml.property  name="butSymptoms"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JButton butSymptoms;
	/**
	 * @uml.property  name="butDiag"
	 * @uml.associationEnd  readOnly="true"
	 */
	private JButton butDiag;
	/**
	 * @uml.property  name="butTreatment"
	 * @uml.associationEnd  readOnly="true"
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

	public MREditorView(){
	}

}

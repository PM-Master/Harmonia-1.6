package gov.nist.csd.pm.application.appeditor;

import gov.nist.csd.pm.application.medrec.MRSelector;
import gov.nist.csd.pm.common.application.*;
import gov.nist.csd.pm.common.graphics.GraphicsUtil;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static gov.nist.csd.pm.common.util.Generators.generateRandomName;

import javax.swing.border.BevelBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/*
 * ID Info(name DOB ID)
 * Employee PII Info(ssn salary address)
 * Employment Info(job title supervisor years worked)
 * Employee Contact Info(home phone work phone cell phone email office)
 */
public class EmployeeRecord extends JFrame {

	private JPanel contentPane;
	private JTextField tfFirstName;
	private JTextField tfMi;
	private JTextField tfLastName;
	private JTextField tfJobTitle;
	private JTextField tfOU;
	private JTextField tfBuilding;
	private JTextField tfCampus;
	private JTextField tfGroup;
	private JTextField tfWorkPhone;
	private JTextField tfCellPhone;
	private JTextField tfWorkEmail;
	private JTextField tfHomePhone;
	private JTextField tfPersonalEmail;
	private JTextField tfSSN;
	private JTextField tfSalary;
	private JTextField tfAddress;
	private ImageIcon userImageIcon;
	private TableEditor editor;
	private String sessionId;
	private int nSimulatorPort;
	private static FileWriter fw;

	private final int PM_DEFAULT_SIMULATOR_PORT = 8081;
	private String sProcessId;
	private SysCaller sysCaller;
	private Object sCrtRecordName;
	private String sFirstName;
	private String sMi;
	private String sLastName;
	private String sSSN;
	private String sSalary;
	private String sAddress;
	private String sJobTitle;
	private String sOU;
	private String sBuilding;
	private String sOffice;
	private String sCampus;
	private String sGroup;
	private String sWorkPhone;
	private String sCellPhone;
	private String sHomePhone;
	private String sWorkEmail;
	private String sPersonalEmail;
	private String sCrtUser;
	private String sTemplateId;
	private ERSelector erSelector;
	private JLabel lblPersonalEmail;
	private JLabel lblHomePhone;
	private JLabel lblCellPhone;
	private JLabel lblWorkPhone;
	private JLabel lblSSN;
	private JLabel lblLastName;
	private JLabel lblMi;
	private JLabel lblFirstName;
	private JLabel lblCampus;
	private JLabel lblOffice;
	private JLabel lblBuilding;
	private JLabel lblYearsWorked;
	private JLabel lblGroup;
	private JLabel lblOU;
	private JLabel lblJobtitle;
	private JLabel lblAddress;
	private JLabel lblSalary;
	private JLabel picture;
	private JLabel lblWorkEmail;
	private ArrayList<JLabel> labels;
	private ArrayList<JTextField> fields;
	private ArrayList<String> objNames;
	private ArrayList<String> objValues;
	private JTextField tfOffice;

	public EmployeeRecord(String sessionId, int nSimPort, String sProcId,
			boolean bDebug) {
		setResizable(false);
		setTitle("Employee Record Editor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 500, 521);
		objValues = new ArrayList<String>();
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				erSelector.prepare();
				erSelector.setVisible(true);
			}

		});
		mnFile.add(mntmOpen);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		// this.editor = e;
		/*
		 * try { userImageIcon =
		 * GraphicsUtil.getImageIcon("/images/common/users/" + "katie" + ".gif",
		 * getClass()); //+ e.retUserLogon() + ".gif", getClass()); } catch
		 * (Exception ex) { userImageIcon = GraphicsUtil.getImageIcon(
		 * "/images/common/users/unknown-person.gif", getClass()); }
		 */

		/*try {
			fw = new FileWriter(
					"C:\\Users\\Administrator.WIN-DNAR5079LMF\\Desktop\\EMPREC.txt");
		} catch (IOException e2) {
			e2.printStackTrace();
		}*/

		this.sessionId = sessionId;
		nSimulatorPort = (nSimPort < 1024) ? PM_DEFAULT_SIMULATOR_PORT
				: nSimPort;
		sProcessId = sProcId;
		sysCaller = new SysCallerImpl(nSimulatorPort, sessionId, sProcId,
				bDebug, "ER");
		erSelector = new ERSelector(this, sysCaller, sessionId);
		erSelector.pack();
		objNames = new ArrayList<String>();
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
		for (int i = 0; i < res.size(); i++) {
			String sLine = res.getStringValue(i);
			String[] pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
			if (pieces[0].equals("Employee_Record")) {
				sTemplateId = pieces[1];
			}
		}
		JPanel panel = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, panel, 115,SpringLayout.NORTH, contentPane);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
				BorderFactory.createTitledBorder("Identification")));
		sl_contentPane.putConstraint(SpringLayout.NORTH, panel, 0,SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, panel, 0,SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, panel, 320,SpringLayout.WEST, contentPane);
		contentPane.add(panel);

		picture = new JLabel(userImageIcon);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null,
				null, null));
		sl_contentPane.putConstraint(SpringLayout.NORTH, panel_1, 0,
				SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, panel_1, 6,
				SpringLayout.EAST, panel);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, panel_1, 0,
				SpringLayout.SOUTH, panel);
		sl_contentPane.putConstraint(SpringLayout.EAST, panel_1, 0,
				SpringLayout.EAST, contentPane);
		panel_1.add(picture, BorderLayout.CENTER);
		contentPane.add(panel_1);

		JPanel panel_2 = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, panel_2, 11,
				SpringLayout.SOUTH, panel);
		sl_contentPane.putConstraint(SpringLayout.WEST, panel_2, 0,
				SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, panel_2, 0,
				SpringLayout.EAST, contentPane);
		panel_2.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 0, 0, 0),
				BorderFactory.createTitledBorder("Sensitive Information")));
		contentPane.add(panel_2);

		JPanel panel_3 = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.NORTH, panel_3, 195,
				SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, panel_2, -6,
				SpringLayout.NORTH, panel_3);
		sl_contentPane.putConstraint(SpringLayout.WEST, panel_3, 0,
				SpringLayout.WEST, panel);
		sl_contentPane.putConstraint(SpringLayout.EAST, panel_3, 0,
				SpringLayout.EAST, panel_1);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 37, 92, 49, 136, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 20, 0 };
		gbl_panel_2.columnWeights = new double[] { 1.0, 0.0, 1.0,
				0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		lblSalary = new JLabel("Salary:");
		lblSalary.setFont(new Font("Dialog", Font.PLAIN, 10));
		GridBagConstraints gbc_lblSalary = new GridBagConstraints();
		gbc_lblSalary.anchor = GridBagConstraints.WEST;
		gbc_lblSalary.insets = new Insets(0, 0, 5, 5);
		gbc_lblSalary.gridx = 0;
		gbc_lblSalary.gridy = 0;
		panel_2.add(lblSalary, gbc_lblSalary);

		lblAddress = new JLabel("Home Address:");
		lblAddress.setFont(new Font("Dialog", Font.PLAIN, 10));
		GridBagConstraints gbc_lblAddress = new GridBagConstraints();
		gbc_lblAddress.anchor = GridBagConstraints.WEST;
		gbc_lblAddress.insets = new Insets(0, 0, 5, 5);
		gbc_lblAddress.gridx = 2;
		gbc_lblAddress.gridy = 0;
		panel_2.add(lblAddress, gbc_lblAddress);

		tfSalary = new JTextField();
		GridBagConstraints gbc_tfSalary = new GridBagConstraints();
		gbc_tfSalary.gridwidth = 2;
		gbc_tfSalary.insets = new Insets(0, 0, 0, 5);
		gbc_tfSalary.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfSalary.gridx = 0;
		gbc_tfSalary.gridy = 1;
		panel_2.add(tfSalary, gbc_tfSalary);
		tfSalary.setColumns(10);

		tfAddress = new JTextField();
		GridBagConstraints gbc_tfAddress = new GridBagConstraints();
		gbc_tfAddress.gridwidth = 2;
		gbc_tfAddress.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfAddress.gridx = 2;
		gbc_tfAddress.gridy = 1;
		panel_2.add(tfAddress, gbc_tfAddress);
		tfAddress.setColumns(10);
		panel_3.setBorder(new TitledBorder(null, "Employment Information",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(panel_3);

		JPanel panel_4 = new JPanel();
		sl_contentPane.putConstraint(SpringLayout.SOUTH, panel_3, -6,
				SpringLayout.NORTH, panel_4);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 150, 188, 111, 0 };
		gbl_panel_3.rowHeights = new int[] { 14, 20, 14, 20, 0 };
		gbl_panel_3.columnWeights = new double[] { 0.0, 1.0, 0.0,
				Double.MIN_VALUE };
		gbl_panel_3.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);

		lblJobtitle = new JLabel("Job Title:");
		lblJobtitle.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblJobtitle = new GridBagConstraints();
		gbc_lblJobtitle.anchor = GridBagConstraints.NORTH;
		gbc_lblJobtitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblJobtitle.insets = new Insets(0, 0, 5, 5);
		gbc_lblJobtitle.gridx = 0;
		gbc_lblJobtitle.gridy = 0;
		panel_3.add(lblJobtitle, gbc_lblJobtitle);

		lblOU = new JLabel("Organizational Unit:");
		lblOU.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblOU = new GridBagConstraints();
		gbc_lblOU.anchor = GridBagConstraints.NORTH;
		gbc_lblOU.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblOU.insets = new Insets(0, 0, 5, 5);
		gbc_lblOU.gridx = 1;
		gbc_lblOU.gridy = 0;
		panel_3.add(lblOU, gbc_lblOU);

		lblGroup = new JLabel("Group:");
		lblGroup.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblGroup = new GridBagConstraints();
		gbc_lblGroup.anchor = GridBagConstraints.NORTH;
		gbc_lblGroup.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblGroup.insets = new Insets(0, 0, 5, 0);
		gbc_lblGroup.gridx = 2;
		gbc_lblGroup.gridy = 0;
		panel_3.add(lblGroup, gbc_lblGroup);

		tfJobTitle = new JTextField();
		GridBagConstraints gbc_tfJobTitle = new GridBagConstraints();
		gbc_tfJobTitle.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfJobTitle.anchor = GridBagConstraints.NORTH;
		gbc_tfJobTitle.insets = new Insets(0, 0, 5, 5);
		gbc_tfJobTitle.gridx = 0;
		gbc_tfJobTitle.gridy = 1;
		panel_3.add(tfJobTitle, gbc_tfJobTitle);
		tfJobTitle.setColumns(10);

		tfOU = new JTextField();
		GridBagConstraints gbc_tfDivision = new GridBagConstraints();
		gbc_tfDivision.anchor = GridBagConstraints.NORTH;
		gbc_tfDivision.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDivision.insets = new Insets(0, 0, 5, 5);
		gbc_tfDivision.gridx = 1;
		gbc_tfDivision.gridy = 1;
		panel_3.add(tfOU, gbc_tfDivision);
		tfOU.setColumns(10);

		tfGroup = new JTextField();
		GridBagConstraints gbc_tfYearsEmployed = new GridBagConstraints();
		gbc_tfYearsEmployed.insets = new Insets(0, 0, 5, 0);
		gbc_tfYearsEmployed.anchor = GridBagConstraints.NORTH;
		gbc_tfYearsEmployed.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfYearsEmployed.gridx = 2;
		gbc_tfYearsEmployed.gridy = 1;
		panel_3.add(tfGroup, gbc_tfYearsEmployed);
		tfGroup.setColumns(10);

		lblBuilding = new JLabel("Building:");
		lblBuilding.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblBuilding = new GridBagConstraints();
		gbc_lblBuilding.anchor = GridBagConstraints.NORTH;
		gbc_lblBuilding.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblBuilding.insets = new Insets(0, 0, 5, 5);
		gbc_lblBuilding.gridx = 0;
		gbc_lblBuilding.gridy = 2;
		panel_3.add(lblBuilding, gbc_lblBuilding);

		lblOffice = new JLabel("Office:");
		lblOffice.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblOffice = new GridBagConstraints();
		gbc_lblOffice.anchor = GridBagConstraints.NORTH;
		gbc_lblOffice.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblOffice.insets = new Insets(0, 0, 5, 5);
		gbc_lblOffice.gridx = 1;
		gbc_lblOffice.gridy = 2;
		panel_3.add(lblOffice, gbc_lblOffice);

		lblCampus = new JLabel("Campus:");
		lblCampus.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblCampus = new GridBagConstraints();
		gbc_lblCampus.anchor = GridBagConstraints.NORTH;
		gbc_lblCampus.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCampus.insets = new Insets(0, 0, 5, 0);
		gbc_lblCampus.gridx = 2;
		gbc_lblCampus.gridy = 2;
		panel_3.add(lblCampus, gbc_lblCampus);

		tfBuilding = new JTextField();
		GridBagConstraints gbc_tfBuilding = new GridBagConstraints();
		gbc_tfBuilding.anchor = GridBagConstraints.NORTH;
		gbc_tfBuilding.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfBuilding.insets = new Insets(0, 0, 0, 5);
		gbc_tfBuilding.gridx = 0;
		gbc_tfBuilding.gridy = 3;
		panel_3.add(tfBuilding, gbc_tfBuilding);
		tfBuilding.setColumns(10);

		tfOffice = new JTextField();
		GridBagConstraints gbc_tfOffice12 = new GridBagConstraints();
		gbc_tfOffice12.anchor = GridBagConstraints.NORTH;
		gbc_tfOffice12.insets = new Insets(0, 0, 0, 5);
		gbc_tfOffice12.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfOffice12.gridx = 1;
		gbc_tfOffice12.gridy = 3;
		panel_3.add(tfOffice, gbc_tfOffice12);
		tfOffice.setColumns(10);

		tfCampus = new JTextField();
		GridBagConstraints gbc_tfCampus = new GridBagConstraints();
		gbc_tfCampus.anchor = GridBagConstraints.NORTH;
		gbc_tfCampus.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfCampus.gridx = 2;
		gbc_tfCampus.gridy = 3;
		panel_3.add(tfCampus, gbc_tfCampus);
		tfCampus.setColumns(10);
		sl_contentPane.putConstraint(SpringLayout.NORTH, panel_4, 316, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, panel_4, 0, SpringLayout.WEST, panel);
		sl_contentPane.putConstraint(SpringLayout.EAST, panel_4, 0, SpringLayout.EAST, panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_5 = new JPanel();
		panel_1.add(panel_5, BorderLayout.SOUTH);
		panel_5.setLayout(new BorderLayout(0, 0));

		JButton btnChange = new JButton("Change");
		panel_5.add(btnChange, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 119, 26, 141, 0 };
		gbl_panel.rowHeights = new int[] { 14, 20, 14, 20, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		lblFirstName = new JLabel("First Name:");
		lblFirstName.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblFirstName = new GridBagConstraints();
		gbc_lblFirstName.anchor = GridBagConstraints.NORTH;
		gbc_lblFirstName.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblFirstName.insets = new Insets(0, 0, 5, 5);
		gbc_lblFirstName.gridx = 0;
		gbc_lblFirstName.gridy = 0;
		panel.add(lblFirstName, gbc_lblFirstName);

		lblMi = new JLabel("MI:");
		lblMi.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblMi = new GridBagConstraints();
		gbc_lblMi.anchor = GridBagConstraints.NORTH;
		gbc_lblMi.insets = new Insets(0, 0, 5, 5);
		gbc_lblMi.gridx = 1;
		gbc_lblMi.gridy = 0;
		panel.add(lblMi, gbc_lblMi);

		lblLastName = new JLabel("  Last Name:");
		lblLastName.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblLastName = new GridBagConstraints();
		gbc_lblLastName.anchor = GridBagConstraints.NORTH;
		gbc_lblLastName.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblLastName.insets = new Insets(0, 0, 5, 0);
		gbc_lblLastName.gridx = 2;
		gbc_lblLastName.gridy = 0;
		panel.add(lblLastName, gbc_lblLastName);

		tfFirstName = new JTextField();
		GridBagConstraints gbc_tfFirstName = new GridBagConstraints();
		gbc_tfFirstName.anchor = GridBagConstraints.NORTH;
		gbc_tfFirstName.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfFirstName.insets = new Insets(0, 0, 5, 5);
		gbc_tfFirstName.gridx = 0;
		gbc_tfFirstName.gridy = 1;
		panel.add(tfFirstName, gbc_tfFirstName);
		tfFirstName.setColumns(10);

		tfMi = new JTextField();
		GridBagConstraints gbc_tfMi = new GridBagConstraints();
		gbc_tfMi.anchor = GridBagConstraints.NORTH;
		gbc_tfMi.insets = new Insets(0, 0, 5, 5);
		gbc_tfMi.gridx = 1;
		gbc_tfMi.gridy = 1;
		panel.add(tfMi, gbc_tfMi);
		tfMi.setColumns(2);

		tfLastName = new JTextField();
		GridBagConstraints gbc_tfLastName = new GridBagConstraints();
		gbc_tfLastName.anchor = GridBagConstraints.NORTHEAST;
		gbc_tfLastName.insets = new Insets(0, 0, 5, 0);
		gbc_tfLastName.gridx = 2;
		gbc_tfLastName.gridy = 1;
		panel.add(tfLastName, gbc_tfLastName);
		tfLastName.setColumns(12);

		lblSSN = new JLabel("SSN:");
		lblSSN.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblSSN = new GridBagConstraints();
		gbc_lblSSN.anchor = GridBagConstraints.NORTH;
		gbc_lblSSN.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSSN.insets = new Insets(0, 0, 5, 5);
		gbc_lblSSN.gridx = 0;
		gbc_lblSSN.gridy = 2;
		panel.add(lblSSN, gbc_lblSSN);
		panel_4.setBorder(new TitledBorder(null,
				"Employee Contact Information", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		contentPane.add(panel_4);

		JButton btnSubmit = new JButton("Submit");
		btnSubmit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveRecord();
			}
		});
		sl_contentPane.putConstraint(SpringLayout.SOUTH, panel_4, -6,
				SpringLayout.NORTH, btnSubmit);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] { 114, 84, 114, 128, 0 };
		gbl_panel_4.rowHeights = new int[] { 14, 20, 14, 20, 0 };
		gbl_panel_4.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_panel_4.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panel_4.setLayout(gbl_panel_4);

		lblWorkPhone = new JLabel("Work Phone:");
		lblWorkPhone.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblWorkPhone = new GridBagConstraints();
		gbc_lblWorkPhone.anchor = GridBagConstraints.NORTH;
		gbc_lblWorkPhone.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblWorkPhone.insets = new Insets(0, 0, 5, 5);
		gbc_lblWorkPhone.gridx = 0;
		gbc_lblWorkPhone.gridy = 0;
		panel_4.add(lblWorkPhone, gbc_lblWorkPhone);

		lblCellPhone = new JLabel("Cell Phone:");
		lblCellPhone.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblCellPhone = new GridBagConstraints();
		gbc_lblCellPhone.anchor = GridBagConstraints.NORTH;
		gbc_lblCellPhone.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCellPhone.insets = new Insets(0, 0, 5, 5);
		gbc_lblCellPhone.gridx = 2;
		gbc_lblCellPhone.gridy = 0;
		panel_4.add(lblCellPhone, gbc_lblCellPhone);

		lblHomePhone = new JLabel("Home Phone:");
		lblHomePhone.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.NORTH;
		gbc_lblNewLabel_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.gridx = 3;
		gbc_lblNewLabel_2.gridy = 0;
		panel_4.add(lblHomePhone, gbc_lblNewLabel_2);

		tfWorkPhone = new JTextField();
		GridBagConstraints gbc_tfWorkPhone = new GridBagConstraints();
		gbc_tfWorkPhone.gridwidth = 2;
		gbc_tfWorkPhone.anchor = GridBagConstraints.NORTH;
		gbc_tfWorkPhone.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfWorkPhone.insets = new Insets(0, 0, 5, 5);
		gbc_tfWorkPhone.gridx = 0;
		gbc_tfWorkPhone.gridy = 1;
		panel_4.add(tfWorkPhone, gbc_tfWorkPhone);
		tfWorkPhone.setColumns(10);

		tfCellPhone = new JTextField();
		GridBagConstraints gbc_tfCellPhone = new GridBagConstraints();
		gbc_tfCellPhone.anchor = GridBagConstraints.NORTH;
		gbc_tfCellPhone.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfCellPhone.insets = new Insets(0, 0, 5, 5);
		gbc_tfCellPhone.gridx = 2;
		gbc_tfCellPhone.gridy = 1;
		panel_4.add(tfCellPhone, gbc_tfCellPhone);
		tfCellPhone.setColumns(10);

		tfHomePhone = new JTextField();
		GridBagConstraints gbc_tfHomePhone = new GridBagConstraints();
		gbc_tfHomePhone.anchor = GridBagConstraints.NORTH;
		gbc_tfHomePhone.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfHomePhone.insets = new Insets(0, 0, 5, 0);
		gbc_tfHomePhone.gridx = 3;
		gbc_tfHomePhone.gridy = 1;
		panel_4.add(tfHomePhone, gbc_tfHomePhone);
		tfHomePhone.setColumns(10);

		lblWorkEmail = new JLabel("Work Email:");
		lblWorkEmail.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblHomePhone = new GridBagConstraints();
		gbc_lblHomePhone.anchor = GridBagConstraints.NORTH;
		gbc_lblHomePhone.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblHomePhone.insets = new Insets(0, 0, 5, 5);
		gbc_lblHomePhone.gridx = 0;
		gbc_lblHomePhone.gridy = 2;
		panel_4.add(lblWorkEmail, gbc_lblHomePhone);

		lblPersonalEmail = new JLabel("Personal Email:");
		lblPersonalEmail.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblPersonalEmail = new GridBagConstraints();
		gbc_lblPersonalEmail.anchor = GridBagConstraints.NORTH;
		gbc_lblPersonalEmail.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPersonalEmail.insets = new Insets(0, 0, 5, 5);
		gbc_lblPersonalEmail.gridx = 2;
		gbc_lblPersonalEmail.gridy = 2;
		panel_4.add(lblPersonalEmail, gbc_lblPersonalEmail);

		tfWorkEmail = new JTextField();
		GridBagConstraints gbc_tfWorkEmail = new GridBagConstraints();
		gbc_tfWorkEmail.anchor = GridBagConstraints.NORTH;
		gbc_tfWorkEmail.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfWorkEmail.insets = new Insets(0, 0, 0, 5);
		gbc_tfWorkEmail.gridwidth = 2;
		gbc_tfWorkEmail.gridx = 0;
		gbc_tfWorkEmail.gridy = 3;
		panel_4.add(tfWorkEmail, gbc_tfWorkEmail);
		tfWorkEmail.setColumns(10);

		tfPersonalEmail = new JTextField();
		GridBagConstraints gbc_tfPersonalEmail = new GridBagConstraints();
		gbc_tfPersonalEmail.anchor = GridBagConstraints.NORTH;
		gbc_tfPersonalEmail.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfPersonalEmail.gridwidth = 2;
		gbc_tfPersonalEmail.gridx = 2;
		gbc_tfPersonalEmail.gridy = 3;
		panel_4.add(tfPersonalEmail, gbc_tfPersonalEmail);
		tfPersonalEmail.setColumns(10);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnSubmit, 0,
				SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnSubmit, 0,
				SpringLayout.SOUTH, contentPane);
		contentPane.add(btnSubmit);

		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//terminate(0);
				//System.exit(0);
				//TODO Will have to exit process without ending table editor process
				setVisible(false);
			}
		});
		sl_contentPane.putConstraint(SpringLayout.WEST, btnClose, 0,
				SpringLayout.WEST, panel);

		tfSSN = new JTextField();
		GridBagConstraints gbc_tfEmployeeId = new GridBagConstraints();
		gbc_tfEmployeeId.anchor = GridBagConstraints.NORTH;
		gbc_tfEmployeeId.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfEmployeeId.gridwidth = 3;
		gbc_tfEmployeeId.gridx = 0;
		gbc_tfEmployeeId.gridy = 3;
		panel.add(tfSSN, gbc_tfEmployeeId);
		tfSSN.setColumns(10);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnClose, 0,
				SpringLayout.SOUTH, btnSubmit);
		contentPane.add(btnClose);

		JTextField arrFields[] = { tfFirstName,  tfLastName, tfMi,
				tfSSN, tfSalary, tfAddress, tfJobTitle, tfOU, tfGroup, 
				tfBuilding, tfOffice, tfCampus, tfWorkPhone,
				tfWorkEmail, tfHomePhone, tfCellPhone, 
				tfPersonalEmail};

		JLabel arrLabels[] = { lblFirstName, lblLastName, lblMi, lblSSN,
				lblSalary, lblAddress, 
				lblJobtitle, lblOU, lblGroup,
				lblBuilding,  lblOffice, lblCampus,
				lblWorkPhone, lblWorkEmail, lblHomePhone, 
				lblCellPhone, lblPersonalEmail};
		labels = new ArrayList<JLabel>(Arrays.asList(arrLabels));
		fields = new ArrayList<JTextField>(Arrays.asList(arrFields));
	}

	private void terminate(int i) {
		sysCaller.exitProcess(sProcessId);
		System.exit(i);

	}

	public static void log(Object input) {
		/*try {
			fw.write(input + "\r\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	private boolean writeToFile(String key, String val, String sHandle) {
		//key = key.replaceAll(" ", "_");
		log("WTF:" + key + "=" + val);
		Properties props = new Properties();
		props.put(key, val);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos, true);

		try {
			props.store(baos, null);
			pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}");
			pw.print(key + ": " + val);
			pw.close();
			byte[] buf = baos.toByteArray();
			if (buf == null) {
				JOptionPane.showMessageDialog(this, "NO");
				return false;
			}
			int len = sysCaller.writeObject3(sHandle, buf);
			if (len < 0) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError(),
						"Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}

			//sysCaller.closeObject(sHandle);
			log("WTF:: " + props.getProperty(key));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * checks fields to make sure they are all filled out
	 */
	private boolean checkFields() {
		objValues.clear();
		sFirstName = tfFirstName.getText(); objValues.add(sFirstName);
		sMi = tfMi.getText(); objValues.add(sMi);
		sLastName = tfLastName.getText(); objValues.add(sLastName);
		sSSN = tfSSN.getText(); objValues.add(sSSN);
		sSalary = tfSalary.getText(); objValues.add(sSalary);
		sAddress = tfAddress.getText(); objValues.add(sAddress);
		sJobTitle = tfJobTitle.getText(); objValues.add(sJobTitle);
		sOU = tfOU.getText(); objValues.add(sOU);
		sBuilding = tfBuilding.getText(); objValues.add(sBuilding);
		sOffice = tfOffice.getText(); objValues.add(sOffice);
		sCampus = tfCampus.getText(); objValues.add(sCampus);
		sGroup = tfGroup.getText(); objValues.add(sGroup);
		sWorkPhone = tfWorkPhone.getText(); objValues.add(sWorkPhone);
		sCellPhone = tfCellPhone.getText(); objValues.add(sCellPhone);
		sHomePhone = tfHomePhone.getText(); objValues.add(sHomePhone);
		sWorkEmail = tfWorkEmail.getText(); objValues.add(sWorkEmail);
		sPersonalEmail = tfPersonalEmail.getText(); objValues.add(sPersonalEmail);
		for (int i = 0; i < fields.size(); i++) {
			if (fields.get(i).getText().equals("")
					|| fields.get(i).getText() == null) {
				JOptionPane.showMessageDialog(this,
						"Please fill out all fields.");
				return false;
			}
		}
		return true;
	}

	/**
	 * saves record if they dont have permission to write than the object should
	 * not be created
	 * 
	 */
	private void saveRecord() {
		boolean check = checkFields();
		if (!check) {
			return;
			// TODO message to fill all fields.
		}

		if (sCrtRecordName == null) {
			String sFirstNameObj = generateRandomName(4);
			String sHandle = sysCaller.createObject3(sFirstNameObj, "File", "rtf",
					"b|First_Name", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("First_Name", sFirstName, sHandle)) return;
			objNames.add(sFirstNameObj);


			String sLastNameObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sLastNameObj, "File", "rtf",
					"b|Last_Name", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Last_Name", sLastName, sHandle)) return;
			objNames.add(sLastNameObj);

			String sMiObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sMiObj, "File", "rtf", "b|MI",
					"File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("MI", sMi, sHandle)) return;
			objNames.add(sMiObj);

			String sSSNObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sSSNObj, "File", "rtf", "b|SSN",
					"File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("SSN", sSSN, sHandle)) return;
			objNames.add(sSSNObj);

			String sSalaryObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sSalaryObj, "File", "rtf",
					"b|Salary", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Salary", sSalary, sHandle)) return;
			objNames.add(sSalaryObj);

			String sAddressObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sAddressObj, "File", "rtf",
					"b|Home_Address", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Home_Address", sAddress, sHandle)) return;
			objNames.add(sAddressObj);

			String sJobTitleObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sJobTitleObj, "File", "rtf",
					"b|Job_Title", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Job_Title", sJobTitle, sHandle)) return;
			objNames.add(sJobTitleObj);

			String sOUObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sOUObj, "File", "rtf",
					"b|Organizational_Unit", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Organizational_Unit", sOU, sHandle)) return;
			objNames.add(sOUObj);
			
			String sGroupObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sGroupObj, "File", "rtf",
					"b|Group", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Group", sGroup, sHandle)) return;
			objNames.add(sGroupObj);

			String sBuildingObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sBuildingObj, "File", "rtf",
					"b|Building", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Building", sBuilding, sHandle)) return;
			objNames.add(sBuildingObj);

			String sOfficeObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sOfficeObj, "File", "rtf",
					"b|Office", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Office", sOffice, sHandle)) return;
			objNames.add(sOfficeObj);

			String sCampusObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sCampusObj, "File", "rtf",
					"b|Campus", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Campus", sCampus, sHandle)) return;
			objNames.add(sCampusObj);

			
			String sWorkPhoneObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sWorkPhoneObj, "File", "rtf",
					"b|Work_Phone", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Work_Phone", sWorkPhone, sHandle)) return;
			objNames.add(sWorkPhoneObj);
			
			String sWorkEmailObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sWorkEmailObj, "File", "rtf",
					"b|Work_Email", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Work_Email", sWorkEmail, sHandle)) return;
			objNames.add(sWorkEmailObj);


			String sHomePhoneObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sHomePhoneObj, "File", "rtf",
					"b|Home_Phone", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Home_Phone", sHomePhone, sHandle)) return;
			objNames.add(sHomePhoneObj);

			String sCellPhoneObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sCellPhoneObj, "File", "rtf",
					"b|Cell_Phone", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Cell_Phone", sCellPhone, sHandle)) return;
			objNames.add(sCellPhoneObj);

			String sPersonalEmailObj = generateRandomName(4);
			sHandle = sysCaller.createObject3(sPersonalEmailObj, "File", "rtf",
					"b|Personal_Email", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			if(!writeToFile("Personal_Email", sPersonalEmail, sHandle)) return;
			objNames.add(sPersonalEmailObj);
			log("TEMPLATE: " + sTemplateId);
			String sContainerName = sFirstName + " " + sMi + " " + sLastName + " Record";//generateRandomName(4);
			sCrtRecordName = sContainerName;
			this.setTitle(sContainerName);
			String sComponents = sFirstNameObj
					+ SysCaller.PM_FIELD_DELIM + sLastNameObj
					+ SysCaller.PM_FIELD_DELIM + sMiObj
					+ SysCaller.PM_FIELD_DELIM + sSSNObj
					+ SysCaller.PM_FIELD_DELIM + sSalaryObj
					+ SysCaller.PM_FIELD_DELIM + sAddressObj
					+ SysCaller.PM_FIELD_DELIM + sJobTitleObj
					+ SysCaller.PM_FIELD_DELIM + sOUObj
					+ SysCaller.PM_FIELD_DELIM + sGroupObj
					+ SysCaller.PM_FIELD_DELIM + sBuildingObj
					+ SysCaller.PM_FIELD_DELIM + sOfficeObj
					+ SysCaller.PM_FIELD_DELIM + sCampusObj
					+ SysCaller.PM_FIELD_DELIM + sWorkPhoneObj
					+ SysCaller.PM_FIELD_DELIM + sWorkEmailObj
					+ SysCaller.PM_FIELD_DELIM + sHomePhoneObj
					+ SysCaller.PM_FIELD_DELIM + sCellPhoneObj
					+ SysCaller.PM_FIELD_DELIM + sPersonalEmailObj;
			HashMap<String, String> keyMap = new HashMap<String, String>();
			keyMap.put("Last_Name", sLastName);
			Packet res = sysCaller.createRecordInEntityWithProp(sContainerName,
					"Group" + sGroup, "b", sTemplateId, sComponents,
					keyMap);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "could not create record.");
				return;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return;
			}

			boolean bRes = sysCaller.assignObjToContainer(sFirstNameObj,
					sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sLastNameObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			
			bRes = sysCaller.assignObjToContainer(sMiObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sSSNObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sSalaryObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sAddressObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sJobTitleObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sOUObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sGroupObj,sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sBuildingObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sOfficeObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sCampusObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sWorkPhoneObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			
			bRes = sysCaller.assignObjToContainer(sWorkEmailObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sHomePhoneObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sCellPhoneObj, sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			bRes = sysCaller.assignObjToContainer(sPersonalEmailObj,
					sContainerName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			submitted = true;
		} else {
			// Editing Employee Record
			if(!checkFields()){
				return;
			}
			for(int i = 0; i < objNames.size(); i++){
				String sHandle = sysCaller.openObject3(objNames.get(i), "File write");
				if (sHandle == null) {
					JOptionPane.showMessageDialog(this, sysCaller.getLastError());
					return;
				}
				writeToFile(labels.get(i).getText().substring(0, labels.get(i).getText().length()-1),
						objValues.get(i), sHandle);
			}
		}
		JOptionPane.showMessageDialog(this, "Record saved successfully.");
	}

	private void newRecord(){
		//TODO set all fields to null
	}

	private void openRecord() {
		erSelector.prepare();
		erSelector.setVisible(true);
	}

	private boolean openObject(String sObjName, JTextField field, String sLabel) {
		// log("sLABEL: " + sLabel.substring(0, sLabel.length()-1));
		String sHandle = sysCaller.openObject3(sObjName, "File read");
		if (sHandle == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return false;
		}

		// Reserve space for and read the object content.
		byte[] buf = sysCaller.readObject3(sHandle);
		if (buf == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return false;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		Properties props = new Properties();
		try {
			props.load(bais);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception when loading patient's id data");
			return false;
		}

		for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
			String sName = (String) propEnum.nextElement();
			log(sName + "-----" + props.getProperty(sName));
		}
		String propToLookFor = sLabel.substring(0, sLabel.length()-1).trim();
		propToLookFor = propToLookFor.replaceAll(" ", "_");
		log(propToLookFor);
		String prop = props.getProperty(propToLookFor);
		log(propToLookFor + "=" + prop);
		field.setText(prop);
		return true;
	}
	boolean submitted = false;
	protected boolean submitted(){
		return submitted;
	}

	protected void displayRecord(String sContName, String sContId) {
		objNames.clear();
		Packet res = sysCaller.getRecordInfo(sContId);
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}
		sCrtRecordName = sContName;
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
		
		for (int i = 0; i < compNameList.size(); i++) {
			log("compnamelist: " + compNameList);
			String objName = compNameList.get(i);
			String label = labels.get(i).getText();
			openObject(objName, fields.get(i), label);
			objNames.add(objName);
			//objNames.set(i, compNameList.get(i));//FIXME may be wrong
		}

		sCrtRecordName = sContName;
		// TODO display record like in mreditor
	}

	static String sessid;
	static String pid;
	static int simport;
	static boolean debug;

	public static void createGUI() {// TableEditor edit){
		EmployeeRecord r = new EmployeeRecord(sessid, simport, pid, debug);
		r.setVisible(true);

	}

	public static void main(String[] args) {
		// log("main called in schemabuilder 2");
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
		// log(sessid + " " + pid + " " + simport + " " + debug);
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
				createGUI();// edit);
			}
		});
	}
}

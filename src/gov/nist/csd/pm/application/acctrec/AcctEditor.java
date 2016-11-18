/*
 * AcctEditor.java
 *
 */
package gov.nist.csd.pm.application.acctrec;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Vector;

import static gov.nist.csd.pm.common.util.Generators.generateRandomName;

public class AcctEditor extends JFrame implements ActionListener {

	/**
	 * @uml.property  name="constraints"
	 */
	private GridBagConstraints constraints = new GridBagConstraints();

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
	 * @uml.property  name="tfAcctNum"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfAcctNum;
	/**
	 * @uml.property  name="tfAcctName"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfAcctName;
	/**
	 * @uml.property  name="tfAcctSsn"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfAcctSsn;
	/**
	 * @uml.property  name="tfAcctAddr"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField tfAcctAddr;

	/**
	 * @uml.property  name="butReadNum"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butReadNum;
	/**
	 * @uml.property  name="butReadName"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butReadName;
	/**
	 * @uml.property  name="butReadSsn"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butReadSsn;
	/**
	 * @uml.property  name="butReadAddr"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butReadAddr;

	/**
	 * @uml.property  name="butSaveNum"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butSaveNum;
	/**
	 * @uml.property  name="butSaveName"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butSaveName;
	/**
	 * @uml.property  name="butSaveSsn"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butSaveSsn;
	/**
	 * @uml.property  name="butSaveAddr"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton butSaveAddr;

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

	// The account record (an object container)
	/**
	 * @uml.property  name="sCrtRecordName"
	 */
	private String sCrtRecordName; // This is the same as the account number.
	/**
	 * @uml.property  name="sCrtRecordId"
	 */
	private String sCrtRecordId;

	// The acct number object
	/**
	 * @uml.property  name="sAcctNumName"
	 */
	private String sAcctNumName;
	/**
	 * @uml.property  name="sAcctNumId"
	 */
	private String sAcctNumId;

	// The acct name object
	/**
	 * @uml.property  name="sAcctNameName"
	 */
	private String sAcctNameName;
	/**
	 * @uml.property  name="sAcctNameId"
	 */
	private String sAcctNameId;

	// The acct ssn object
	/**
	 * @uml.property  name="sAcctSsnName"
	 */
	private String sAcctSsnName;
	/**
	 * @uml.property  name="sAcctSsnId"
	 */
	private String sAcctSsnId;

	// THe acct address object
	/**
	 * @uml.property  name="sAcctAddrName"
	 */
	private String sAcctAddrName;
	/**
	 * @uml.property  name="sAcctAddrId"
	 */
	private String sAcctAddrId;

	public AcctEditor(int nSimPort, String sSessId, String sProcId,
			boolean bDebug) {
		super("Account Editor");

		this.sSessId = sSessId;
		this.sProcId = sProcId;
		this.bDebug = bDebug;
                //IOC Candidate
		sysCaller = new SysCallerImpl(nSimPort, sSessId, sProcId, bDebug,
				"MREC");

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				terminate(0);
			}
		});

		// Start building the GUI
		JLabel lblAcctNum = new JLabel("Acct number:");
		JLabel lblAcctName = new JLabel("Acct name:");
		JLabel lblAcctSsn = new JLabel("Acct SSN:");
		JLabel lblAcctAddr = new JLabel("Address:");

		tfAcctNum = new JTextField(25);
		tfAcctName = new JTextField(25);
		tfAcctSsn = new JTextField(25);
		tfAcctAddr = new JTextField(25);

		butReadNum = new JButton("Read");
		butReadNum.addActionListener(this);
		butReadNum.setActionCommand("readnum");
		butReadNum.setEnabled(false);

		butReadName = new JButton("Read");
		butReadName.addActionListener(this);
		butReadName.setActionCommand("readname");

		butReadSsn = new JButton("Read");
		butReadSsn.addActionListener(this);
		butReadSsn.setActionCommand("readssn");

		butReadAddr = new JButton("Read");
		butReadAddr.addActionListener(this);
		butReadAddr.setActionCommand("readaddr");

		butSaveNum = new JButton("Save");
		butSaveNum.addActionListener(this);
		butSaveNum.setActionCommand("savenum");
		butSaveNum.setEnabled(false);

		butSaveName = new JButton("Save");
		butSaveName.addActionListener(this);
		butSaveName.setActionCommand("savename");
		butSaveName.setEnabled(false);

		butSaveSsn = new JButton("Save");
		butSaveSsn.addActionListener(this);
		butSaveSsn.setActionCommand("savessn");
		butSaveSsn.setEnabled(false);

		butSaveAddr = new JButton("Save");
		butSaveAddr.addActionListener(this);
		butSaveAddr.setActionCommand("saveaddr");
		butSaveAddr.setEnabled(false);

		JPanel labelPane = new JPanel();
		labelPane.setLayout(new GridLayout(0, 1));
		labelPane.add(lblAcctNum);
		labelPane.add(lblAcctName);
		labelPane.add(lblAcctSsn);
		labelPane.add(lblAcctAddr);

		JPanel acctNumPane = new JPanel();
		// acctNumPane.setLayout(new GridLayout(1, 0));
		acctNumPane.add(tfAcctNum);
		acctNumPane.add(butReadNum);
		acctNumPane.add(butSaveNum);

		JPanel acctNamePane = new JPanel();
		// acctNamePane.setLayout(new GridLayout(1, 0));
		acctNamePane.add(tfAcctName);
		acctNamePane.add(butReadName);
		acctNamePane.add(butSaveName);

		JPanel acctSsnPane = new JPanel();
		// acctSsnPane.setLayout(new GridLayout(1, 0));
		acctSsnPane.add(tfAcctSsn);
		acctSsnPane.add(butReadSsn);
		acctSsnPane.add(butSaveSsn);

		JPanel acctAddrPane = new JPanel();
		// acctAddrPane.setLayout(new GridLayout(1, 0));
		acctAddrPane.add(tfAcctAddr);
		acctAddrPane.add(butReadAddr);
		acctAddrPane.add(butSaveAddr);

		/*
		 * JPanel fieldPane = new JPanel(); fieldPane.setLayout(new
		 * GridLayout(0, 1)); fieldPane.add(tfAcctNum);
		 * fieldPane.add(tfAcctName); fieldPane.add(tfAcctSsn);
		 * fieldPane.add(tfAcctAddr);
		 * 
		 * JPanel readButPane = new JPanel(); readButPane.setLayout(new
		 * GridLayout(0, 1)); readButPane.add(butReadNum);
		 * readButPane.add(butReadName); readButPane.add(butReadSsn);
		 * readButPane.add(butReadAddr);
		 * 
		 * JPanel saveButPane = new JPanel(); saveButPane.setLayout(new
		 * GridLayout(0, 1)); saveButPane.add(butSaveNum);
		 * saveButPane.add(butSaveName); saveButPane.add(butSaveSsn);
		 * saveButPane.add(butSaveAddr);
		 * 
		 * JPanel buttonPane = new JPanel(); buttonPane.add(readButPane);
		 * buttonPane.add(saveButPane);
		 */

		JPanel thePane = new JPanel();
		thePane.setLayout(new GridLayout(0, 1));
		thePane.add(acctNumPane);
		thePane.add(acctNamePane);
		thePane.add(acctSsnPane);
		thePane.add(acctAddrPane);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(labelPane, BorderLayout.WEST);
		contentPane.add(thePane, BorderLayout.CENTER);

		setContentPane(contentPane);

		// Menus
		JMenu fileMenu = createFileMenu();

		JMenuBar mb = new JMenuBar();
		mb.add(fileMenu);
		setJMenuBar(mb);

	}

	private JMenu createFileMenu() {
		JMenu menu = new JMenu("Account Editor");
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

		item = new JMenuItem("Exit");
		item.addActionListener(this);
		menu.add(item);

		return menu;
	}

	private void terminate(int nCode) {
		sysCaller.exitProcess(sProcId);
		System.exit(nCode);
	}

	private void processRecord(String sRecName) {
		if (sRecName == null)
			return;

		String sRecId = sysCaller
				.getIdOfEntityWithNameAndType(sRecName, SysCaller.PM_NODE_OATTR);
		if (sRecId == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}
		displayRecord(sRecName, sRecId);
	}

	// Actually this displays only the account number and sets the names and
	// ids of the other three objects. To display them, the user has to click
	// on the corresponding buttons.
	public void displayRecord(String sRecName, String sRecId) {
		resetFields();
		// Now get record info.
		Packet res = (Packet) sysCaller.getRecordInfo(sRecId);
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
		System.out.println("Properties of record " + pieces[0] + " of id "
				+ pieces[1]);

		sLine = res.getStringValue(1);
		pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
		System.out.println("  Its template is " + pieces[0] + " of id "
				+ pieces[1]);

		sLine = res.getStringValue(2);
		int nComp = Integer.valueOf(sLine).intValue();
		System.out.println("  It has " + nComp + " components:");

		Vector compIdVector = new Vector();
		Vector compNameVector = new Vector();
		for (int i = 0; i < nComp; i++) {
			sLine = res.getStringValue(3 + i);
			pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
			compNameVector.add(pieces[0]);
			compIdVector.add(pieces[1]);
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

		// There should be 4 component objects. Set the global vars
		// to names and ids of these objects.
		sCrtRecordName = sRecName;
		sCrtRecordId = sRecId;

		sAcctNumName = (String) compNameVector.get(0);
		sAcctNameName = (String) compNameVector.get(1);
		sAcctSsnName = (String) compNameVector.get(2);
		sAcctAddrName = (String) compNameVector.get(3);

		sAcctNumId = (String) compIdVector.get(0);
		sAcctNameId = (String) compIdVector.get(1);
		sAcctSsnId = (String) compIdVector.get(2);
		sAcctAddrId = (String) compIdVector.get(3);

		// First one contains the account number.
		readNum();
	}

	private void resetFields() {
		tfAcctNum.setText(null);
		tfAcctName.setText(null);
		tfAcctSsn.setText(null);
		tfAcctAddr.setText(null);
	}

	private void openAcct() {
		JOptionPane.showMessageDialog(this,
				"Please right-click an account and select \"Open\"!");
		return;
	}

	private void readNum() {
		if (sAcctNumName == null || sAcctNumId == null) {
			JOptionPane.showMessageDialog(this,
					"No account number yet; please open an existing account!");
			return;
		}
		String sHandle = sysCaller.openObject3(sAcctNumName, "File read");
		if (sHandle == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}

		// Read the object content.
		byte[] buf = sysCaller.readObject3(sHandle);
		if (buf == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		BufferedReader br = new BufferedReader(new InputStreamReader(bais));
		String sAcctNum = null;
		try {
			sAcctNum = br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception when reading the account number!");
			return;
		}

		tfAcctNum.setText(sAcctNum);
	}

	private void readName() {
		if (sAcctNameName == null || sAcctNameId == null) {
			JOptionPane
					.showMessageDialog(this,
							"No account holder name yet; please open an existing account!");
			return;
		}
		String sHandle = sysCaller.openObject3(sAcctNameName, "File read");
		if (sHandle == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}

		// Read the object content.
		byte[] buf = sysCaller.readObject3(sHandle);
		if (buf == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		BufferedReader br = new BufferedReader(new InputStreamReader(bais));
		String sAcctName = null;
		try {
			sAcctName = br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception when reading the account holder name!");
			return;
		}

		tfAcctName.setText(sAcctName);
	}

	private void readSsn() {
		if (sAcctSsnName == null || sAcctSsnId == null) {
			JOptionPane
					.showMessageDialog(this,
							"No account holder SSN yet; please open an existing account!");
			return;
		}
		String sHandle = sysCaller.openObject3(sAcctSsnName, "File read");
		if (sHandle == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}

		// Read the object content.
		byte[] buf = sysCaller.readObject3(sHandle);
		if (buf == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		BufferedReader br = new BufferedReader(new InputStreamReader(bais));
		String sAcctSsn = null;
		try {
			sAcctSsn = br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception when reading the account holder SSN!");
			return;
		}

		tfAcctSsn.setText(sAcctSsn);
	}

	private void readAddr() {
		if (sAcctAddrName == null || sAcctAddrId == null) {
			JOptionPane
					.showMessageDialog(this,
							"No account holder address yet; please open an existing account!");
			return;
		}
		String sHandle = sysCaller.openObject3(sAcctAddrName, "File read");
		if (sHandle == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}

		// Read the object content.
		byte[] buf = sysCaller.readObject3(sHandle);
		if (buf == null) {
			JOptionPane.showMessageDialog(this, sysCaller.getLastError());
			return;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		BufferedReader br = new BufferedReader(new InputStreamReader(bais));
		String sAcctAddr = null;
		try {
			sAcctAddr = br.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception when reading the account holder address!");
			return;
		}

		tfAcctAddr.setText(sAcctAddr);
	}

	private void saveNum() {
	}

	private void saveName() {
	}

	private void saveSsn() {
	}

	private void saveAddr() {
	}

	private void newAcct() {
		resetFields();
	}

	private void saveAcct() {
		// Check fields.
		String sAcctNum = tfAcctNum.getText();
		if (sAcctNum.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter an account number!");
			return;
		}
		String sAcctName = tfAcctName.getText();
		if (sAcctName.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter the account holder name!");
			return;
		}
		String sAcctSsn = tfAcctSsn.getText();
		if (sAcctSsn.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter the account holder Social Security number!");
			return;
		}
		String sAcctAddr = tfAcctAddr.getText();
		if (sAcctAddr.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter the account holder address!");
			return;
		}
		// If this is a new composite object:
		if (sCrtRecordName == null) {

			// Create and write the AcctNum object.
			sAcctNumName = generateRandomName(4);
			String sHandle = sysCaller.createObject3(sAcctNumName, "File",
					"txt", "b|AcctNum", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(baos);
			pw.print(tfAcctNum.getText());
			pw.close();
			byte[] buf = baos.toByteArray();
			int ret = sysCaller.writeObject3(sHandle, buf);
			if (ret < 0) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			sysCaller.closeObject(sHandle);

			// Create and write the AcctName object.
			sAcctNameName = generateRandomName(4);
			sHandle = sysCaller.createObject3(sAcctNameName, "File", "txt",
					"b|AcctName", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			baos = new ByteArrayOutputStream();
			pw = new PrintWriter(baos);
			pw.print(tfAcctName.getText());
			pw.close();
			buf = baos.toByteArray();
			ret = sysCaller.writeObject3(sHandle, buf);
			if (ret < 0) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			sysCaller.closeObject(sHandle);

			// Create and write the AcctSsn object.
			sAcctSsnName = generateRandomName(4);
			sHandle = sysCaller.createObject3(sAcctSsnName, "File", "txt",
					"b|AcctSsn", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			baos = new ByteArrayOutputStream();
			pw = new PrintWriter(baos);
			pw.print(tfAcctSsn.getText());
			pw.close();
			buf = baos.toByteArray();
			ret = sysCaller.writeObject3(sHandle, buf);
			if (ret < 0) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			sysCaller.closeObject(sHandle);

			// Create and write the AcctAddr object.
			sAcctAddrName = generateRandomName(4);
			sHandle = sysCaller.createObject3(sAcctAddrName, "File", "txt",
					"b|AcctAddr", "File write", null, null, null, null);
			if (sHandle == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			baos = new ByteArrayOutputStream();
			pw = new PrintWriter(baos);
			pw.print(tfAcctAddr.getText());
			pw.close();
			buf = baos.toByteArray();
			ret = sysCaller.writeObject3(sHandle, buf);
			if (ret < 0) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			sysCaller.closeObject(sHandle);

			// Prepare for the creation of the record (a container).
			// The container name is the account number.
			String sRecordName = tfAcctNum.getText();

			// The components' names.
			String sComponents = sAcctNumName + SysCaller.PM_FIELD_DELIM
					+ sAcctNameName + SysCaller.PM_FIELD_DELIM + sAcctSsnName
					+ SysCaller.PM_FIELD_DELIM + sAcctAddrName;

			// The keys: the account number.
			HashMap keyMap = new HashMap();
			keyMap.put("acctnum", tfAcctNum.getText());

			// The template. Look for a template with the name "acctTpl".
			String sTemplateId = sysCaller.getIdOfEntityWithNameAndType("acctTpl",
                    SysCaller.PM_TEMPLATE);
			if (sTemplateId == null) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

			// Create the record in the "Acct Recs" container.
			Packet res = (Packet) sysCaller.createRecord(sRecordName,
					"Acct Recs", SysCaller.PM_NODE_OATTR, sTemplateId,
					sComponents, keyMap);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return;
			}

			// Assign all components (the fields) to the container (record)
			// just created. The fields are already assigned to the correct
			// columns.
			boolean bRes = sysCaller.assignObjToContainer(sAcctNumName,
					sRecordName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			bRes = sysCaller.assignObjToContainer(sAcctNameName, sRecordName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			bRes = sysCaller.assignObjToContainer(sAcctSsnName, sRecordName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}
			bRes = sysCaller.assignObjToContainer(sAcctAddrName, sRecordName);
			if (!bRes) {
				JOptionPane.showMessageDialog(this, sysCaller.getLastError());
				return;
			}

		}

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("open")) {
			openAcct();
		} else if (e.getActionCommand().equalsIgnoreCase("new")) {
			newAcct();
		} else if (e.getActionCommand().equalsIgnoreCase("save")) {
			saveAcct();
		} else if (e.getActionCommand().equalsIgnoreCase("exit")) {
			terminate(0);
		} else if (e.getActionCommand().equalsIgnoreCase("close")) {
			terminate(0);
		} else if (e.getActionCommand().equalsIgnoreCase("readnum")) {
			readNum();
		} else if (e.getActionCommand().equalsIgnoreCase("readname")) {
			readName();
		} else if (e.getActionCommand().equalsIgnoreCase("readssn")) {
			readSsn();
		} else if (e.getActionCommand().equalsIgnoreCase("readaddr")) {
			readAddr();
		} else if (e.getActionCommand().equalsIgnoreCase("savenum")) {
			saveNum();
		} else if (e.getActionCommand().equalsIgnoreCase("savename")) {
			saveName();
		} else if (e.getActionCommand().equalsIgnoreCase("savessn")) {
			saveSsn();
		} else if (e.getActionCommand().equalsIgnoreCase("saveaddr")) {
			saveAddr();
		}
	}

	static String sessid = null;
	static String pid = null;
	static int simport = 0;
	static String recname = null;
	static boolean debug = false;

	// Create the GUI,
	private static void createGUI() {
		AcctEditor editor = new AcctEditor(simport, sessid, pid, debug);
		editor.pack();
		editor.setVisible(true);
		editor.processRecord(recname);
	}

	// Arguments on the command line:
	// -debug -session <sessionId> -simport <simulator port> <record name>
	// <simulator port> is the port where the kernel simulator listens for
	// connections,
	// by default 8081.
	// <sessionId> is the id of the session where the grantor is running. It is
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
			System.out
					.println("This application must run within a Policy Machine session!");
			System.exit(-1);
		}
		if (pid == null) {
			System.out
					.println("This application must run within a Policy Machine process!");
			System.exit(-1);
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
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

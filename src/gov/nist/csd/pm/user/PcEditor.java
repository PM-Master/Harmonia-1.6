/*
 * PcEditor.java
 *
 */

package gov.nist.csd.pm.user;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class PcEditor extends JDialog implements ActionListener {

	/**
	 * @uml.property  name="session"
	 * @uml.associationEnd  
	 */
	private Session session;
	/**
	 * @uml.property  name="simClient"
	 * @uml.associationEnd  
	 */
	private SSLSocketClient simClient;

	/**
	 * @uml.property  name="pcNameField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField pcNameField;
	/**
	 * @uml.property  name="pcDescrField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField pcDescrField;
	/**
	 * @uml.property  name="pcInfoField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField pcInfoField;

	/**
	 * @uml.property  name="propField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField propField;
	/**
	 * @uml.property  name="propListModel"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	private DefaultListModel propListModel;
	/**
	 * @uml.property  name="propList"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	private JList propList;

	/**
	 * @uml.property  name="addButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton addButton;
	/**
	 * @uml.property  name="removeButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton removeButton;

	/**
	 * @uml.property  name="okButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton okButton;
	/**
	 * @uml.property  name="closeButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton closeButton;

	/**
	 * @uml.property  name="editingMode"
	 */
	private boolean editingMode = false;

	/**
	 * @uml.property  name="pcToEditId"
	 */
	private String pcToEditId;

	/**
	 * @uml.property  name="constraints"
	 */
	private GridBagConstraints constraints = new GridBagConstraints();

	public PcEditor(Session session, SSLSocketClient simClient) {
		super(session, true); // modal

		this.session = session;
		this.simClient = simClient;

		setTitle("Add Policy Class");

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				close();
			}
		});

		// Start building the GUI
		JPanel pcPane = new JPanel();
		pcPane.setLayout(new GridBagLayout());

		JLabel pcNameLabel = new JLabel("Name:");
		JLabel pcDescrLabel = new JLabel("Description:");
		JLabel pcInfoLabel = new JLabel("Other Info:");

		pcNameField = new JTextField(20);
		pcDescrField = new JTextField(20);
		pcInfoField = new JTextField(20);

		constraints.insets = new Insets(0, 10, 0, 0);

		addComp(pcPane, pcNameLabel, 0, 0, 1, 1);
		addComp(pcPane, pcDescrLabel, 0, 1, 1, 1);
		addComp(pcPane, pcInfoLabel, 0, 2, 1, 1);

		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(pcPane, pcNameField, 1, 0, 3, 1);
		addComp(pcPane, pcDescrField, 1, 1, 3, 1);
		addComp(pcPane, pcInfoField, 1, 2, 3, 1);

		pcPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// The property list pane
		JPanel propListPane = new JPanel();
		propListPane.setLayout(new GridBagLayout());

		JLabel propLabel = new JLabel("Edit:");
		propField = new JTextField(23);

		JLabel listLabel = new JLabel("Properties:");
		propListModel = new DefaultListModel();
		propList = new JList(propListModel);
		JScrollPane propListScrollPane = new JScrollPane(propList);
		propListScrollPane.setPreferredSize(new Dimension(255, 160));

		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(propListPane, propLabel, 0, 0, 1, 1);
		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(propListPane, propField, 1, 0, 3, 1);

		constraints.insets = new Insets(10, 0, 0, 0);
		addComp(propListPane, listLabel, 0, 1, 1, 1);
		constraints.insets = new Insets(10, 10, 0, 0);
		addComp(propListPane, propListScrollPane, 1, 1, 3, 3);
		propListPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		// The property button pane
		JPanel propButtonPane = new JPanel();

		addButton = new JButton("Add");
		addButton.setActionCommand("add");
		addButton.addActionListener(this);

		removeButton = new JButton("Remove");
		removeButton.setActionCommand("remove");
		removeButton.addActionListener(this);

		propButtonPane.add(addButton);
		propButtonPane.add(removeButton);
		propButtonPane
				.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		// The property pane = prop list pane + prop button pane
		JPanel propPane = new JPanel();
		propPane.setLayout(new BorderLayout());
		propPane.add(propListPane, BorderLayout.CENTER);
		propPane.add(propButtonPane, BorderLayout.SOUTH);
		propPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createTitledBorder("Properties")));

		// The button pane
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridBagLayout());

		okButton = new JButton("OK");
		okButton.setActionCommand("ok");
		okButton.addActionListener(this);

		closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);

		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(buttonPane, okButton, 1, 0, 1, 1);
		addComp(buttonPane, closeButton, 2, 0, 1, 1);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		JPanel thePane = new JPanel();
		thePane.setLayout(new BorderLayout());
		thePane.add(pcPane, BorderLayout.NORTH);
		thePane.add(propPane, BorderLayout.CENTER);
		thePane.add(buttonPane, BorderLayout.SOUTH);

		thePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setContentPane(thePane);
		getRootPane().setDefaultButton(addButton);
	}

	private void addComp(Container container, Component component, int x,
			int y, int w, int h) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		container.add(component, constraints);
	}

	private Packet getPolicyClasses() {
		try {
			Packet cmd = CommandUtil.makeCmd("getPolicyClasses",
					session.getId());
			Packet res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in getPolicyClasses: " + res.getErrorMessage());
				return null;
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in getPolicyClasses: " + e.getMessage());
			return null;
		}
	}

	public void prepareForAdd() {
		setTitle("Add Policy Class");
		okButton.setText("Add policy");
		editingMode = false;
		pcNameField.setText(null);
		pcNameField.requestFocus();
		pcDescrField.setText(null);
		pcInfoField.setText(null);
		propField.setText(null);
		propListModel.clear();

		pcNameField.setEditable(true);
		pcDescrField.setEditable(true);
		pcInfoField.setEditable(true);
	}

	// Prepare the interface for editing the properties of the selected pc.
	// The argument sId is the id of the selected pc.
	public void prepareForEdit(String sId) {
		setTitle("Edit Policy Class");
		okButton.setText("OK");
		editingMode = true;
		pcToEditId = sId;

		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("getPcInfo", session.getId(), sId,
					"yes");
			res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in getPcInfo: "
						+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in getPcInfo: " + e.getMessage());
			return;
		}
		// The result contains:
		// item 0: <name>:<id>
		// item 1: <description>
		// item 2: <other info>
		// item 3: <property>
		// ...
		String s = res.getStringValue(0);
		String[] pieces = s.split(SessionManager.PM_FIELD_DELIM);
		pcNameField.setText(pieces[0]);
		pcDescrField.setText(res.getStringValue(1));
		pcInfoField.setText(res.getStringValue(2));
		pcNameField.setEditable(false);
		pcDescrField.setEditable(false);
		pcInfoField.setEditable(false);

		propField.setText(null);
		propListModel.clear();
		for (int i = 3; i < res.size(); i++) {
			propListModel.addElement(res.getStringValue(i));
		}
	}

	private void close() {
		this.setVisible(false);
	}

	// Add the new policy class.
	private void addPc() {
		if (editingMode) {
			setVisible(false);
			return;
		}

		String sName = pcNameField.getText().trim();
		String sDescr = pcDescrField.getText().trim();
		String sInfo = pcInfoField.getText().trim();

		if (sName.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter a policy class name!");
			return;
		}
		if (sDescr.length() == 0)
			sDescr = sName;
		if (sInfo.length() == 0)
			sInfo = sName;

		// Send the command and let the server set the GUID and test for unique
		// name.
		try {
			Packet cmd = CommandUtil.makeCmd("addPc", session.getId(), null, sName, 
                    sDescr, sInfo); // Added null parameter for processId - Gopi Nov 2013
			int n = propListModel.getSize();
			if (n > 0)
				for (int i = 0; i < n; i++) {
					cmd.addItem(ItemType.CMD_ARG, (String) propListModel.get(i));
				}
			Packet res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in addPc: " + res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in addPc: " + e.getMessage());
			return;
		}

		pcNameField.setText(null);
		pcNameField.requestFocus();
		pcDescrField.setText(null);
		pcInfoField.setText(null);
		propField.setText(null);
		propListModel.clear();
	}

	private void addProp() {
		String sProp = propField.getText().trim();
		if (sProp.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter a property in the \"Edit\" field!");
			return;
		}
		if (editingMode) {
			try {
				Packet cmd = CommandUtil.makeCmd("addProp", session.getId(),
						pcToEditId, Session.PM_NODE_POL, "yes", sProp);
				Packet res = simClient.sendReceive(cmd, null);
				if (res.hasError()) {
					JOptionPane.showMessageDialog(this, "Error in addProp: "
							+ res.getErrorMessage());
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Exception in addProp: "
						+ e.getMessage());
				return;
			}
		}

		propListModel.addElement(sProp);
		propField.setText("");
		propField.requestFocus();
	}

	private void removeProp() {
		String sProp = (String) propList.getSelectedValue();
		if (sProp == null) {
			JOptionPane.showMessageDialog(this,
					"Please select a property from the \"Properties\" list!");
			return;
		}

		if (editingMode) {
			try {
				Packet cmd = CommandUtil.makeCmd("removeProp", session.getId(),
						pcToEditId, Session.PM_NODE_POL, "yes", sProp);
				Packet res = simClient.sendReceive(cmd, null);
				if (res.hasError()) {
					JOptionPane.showMessageDialog(this, "Error in removeProp: "
							+ res.getErrorMessage());
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Exception in removeProp: "
						+ e.getMessage());
				return;
			}
		}
		propListModel.removeElement(sProp);
		propField.setText(sProp);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("ok")) {
			addPc();
		} else if (e.getActionCommand().equalsIgnoreCase("close")) {
			close();
		} else if (e.getActionCommand().equalsIgnoreCase("add")) {
			addProp();
		} else if (e.getActionCommand().equalsIgnoreCase("remove")) {
			removeProp();
		}
	}
}

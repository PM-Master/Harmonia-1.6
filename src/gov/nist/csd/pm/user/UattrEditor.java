/*
 * UserAttrEditor.java
 *
 * Created on April 4, 2005, 4:51 PM
 */

package gov.nist.csd.pm.user;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static gov.nist.csd.pm.common.info.PMCommand.ADD_UATTR;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class UattrEditor extends JDialog implements ActionListener {

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
	 * @uml.property  name="uaNameField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField uaNameField;
	/**
	 * @uml.property  name="uaDescrField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField uaDescrField;
	/**
	 * @uml.property  name="uaInfoField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField uaInfoField;

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
	 * @uml.property  name="constraints"
	 */
	private GridBagConstraints constraints = new GridBagConstraints();

	/**
	 * @uml.property  name="sBaseNodeId"
	 */
	private String sBaseNodeId = null;
	/**
	 * @uml.property  name="sBaseNodeType"
	 */
	private String sBaseNodeType = null;

	/**
	 * @uml.property  name="editingMode"
	 */
	private boolean editingMode = false;
	/**
	 * @uml.property  name="uattrToEditId"
	 */
	private String uattrToEditId;

	public UattrEditor(Session session, SSLSocketClient simClient) {
		super(session, true); // modal

		this.session = session;
		this.simClient = simClient;

		setTitle("Add User Attribute");

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				doClose();
			}
		});

		// Start building the GUI
		JPanel uattrPane = new JPanel();
		uattrPane.setLayout(new GridBagLayout());

		JLabel uaNameLabel = new JLabel("Name:");
		JLabel uaDescrLabel = new JLabel("Description:");
		JLabel uaInfoLabel = new JLabel("Other Info:");

		uaNameField = new JTextField(23);
		uaDescrField = new JTextField(23);
		uaInfoField = new JTextField(23);

		addComp(uattrPane, uaNameLabel, 0, 0, 1, 1);
		addComp(uattrPane, uaDescrLabel, 0, 1, 1, 1);
		addComp(uattrPane, uaInfoLabel, 0, 2, 1, 1);

		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(uattrPane, uaNameField, 1, 0, 3, 1);
		addComp(uattrPane, uaDescrField, 1, 1, 3, 1);
		addComp(uattrPane, uaInfoField, 1, 2, 3, 1);
		uattrPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

		okButton = new JButton("Add attribute");
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
		thePane.add(uattrPane, BorderLayout.NORTH);
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

	private void doClose() {
		this.setVisible(false);
	}

	public void prepareForAdd(String sId, String sType) {
		setTitle("Add User Attribute");
		okButton.setText("Add attribute");
		editingMode = false;
		uaNameField.setText(null);
		uaNameField.requestFocus();
		uaDescrField.setText(null);
		uaInfoField.setText(null);
		propField.setText(null);
		propListModel.clear();

		uaNameField.setEditable(true);
		uaDescrField.setEditable(true);
		uaInfoField.setEditable(true);

		sBaseNodeId = sId;
		sBaseNodeType = sType;
	}

	public void prepareForEdit(String sId) {
		setTitle("Edit User attribute");
		okButton.setText("OK");
		editingMode = true;
		uattrToEditId = sId;

		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("getAttrInfo", session.getId(),
					sId, Session.PM_NODE_UATTR, "yes");
			res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in getAttrInfo: "
						+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Exception in getAttrInfo: "
					+ e.getMessage());
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
		uaNameField.setText(pieces[0]);
		uaDescrField.setText(res.getStringValue(1));
		uaInfoField.setText(res.getStringValue(2));
		uaNameField.setEditable(false);
		uaDescrField.setEditable(false);
		uaInfoField.setEditable(false);

		propField.setText(null);
		propListModel.clear();
		for (int i = 3; i < res.size(); i++) {
			propListModel.addElement(res.getStringValue(i));
		}
	}

	// Invoked when the user clicks the OK button.
	// If editing, close. Otherwise, add the attribute.
	private void addUattr() {
		if (editingMode) {
			setVisible(false);
			return;
		}

		String sName = uaNameField.getText().trim();
		String sDescr = uaDescrField.getText().trim();
		String sInfo = uaInfoField.getText().trim();

		if (sName.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter an attribute name!");
			return;
		}
		if (sDescr.length() == 0)
			sDescr = sName;
		if (sInfo.length() == 0)
			sInfo = sName;

		// Send the command and let the server set the GUID and test for unique
		// name.
		try {
			Packet cmd = CommandUtil.makeCmd(ADD_UATTR, session.getId(), null, // Added null parameter by Gopi for procId
					sName, sDescr, sInfo, sBaseNodeId, sBaseNodeType, "yes");
			int n = propListModel.getSize();
			if (n > 0)
				for (int i = 0; i < n; i++) {
					cmd.addItem(ItemType.CMD_ARG, (String) propListModel.get(i));
				}
			Packet res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in addUattr: " + res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in addUattr: " + e.getMessage());
			return;
		}

		uaNameField.setText(null);
		uaNameField.requestFocus();
		uaDescrField.setText(null);
		uaInfoField.setText(null);
		propField.setText(null);
		propListModel.clear();
	}

	private void doAddProp() {
		String sProp = propField.getText().trim();
		if (sProp.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter a property in the \"Edit\" field!");
			return;
		}
		if (editingMode) {
			try {
				Packet cmd = CommandUtil.makeCmd(PMCommand.ADD_PROP, session.getId(),
						uattrToEditId, Session.PM_NODE_UATTR, "yes", sProp);
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

	private void doRemoveProp() {
		String sProp = (String) propList.getSelectedValue();
		if (sProp == null) {
			JOptionPane.showMessageDialog(this,
					"Please select a property from the \"Properties\" list!");
			return;
		}

		if (editingMode) {
			try {
				Packet cmd = CommandUtil.makeCmd("removeProp", session.getId(),
                        uattrToEditId, Session.PM_NODE_UATTR, "yes", sProp);
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
			addUattr();
		} else if (e.getActionCommand().equalsIgnoreCase("add")) {
			doAddProp();
		} else if (e.getActionCommand().equalsIgnoreCase("remove")) {
			doRemoveProp();
		} else if (e.getActionCommand().equalsIgnoreCase("close")) {
			doClose();
		}
	}
}

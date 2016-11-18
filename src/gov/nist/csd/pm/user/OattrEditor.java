/*
 * ObjAttrEditor.java
 *
 * Created on April 14, 2005, 5:07 PM
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

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class OattrEditor extends JDialog implements ActionListener {

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
	 * @uml.property  name="oaNameField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField oaNameField;
	/**
	 * @uml.property  name="oaDescrField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField oaDescrField;
	/**
	 * @uml.property  name="oaInfoField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField oaInfoField;

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
	 * @uml.property  name="editingMode"
	 */
	private boolean editingMode = false;
	/**
	 * @uml.property  name="oattrToEditId"
	 */
	private String oattrToEditId;

	/**
	 * @uml.property  name="sBaseNodeId"
	 */
	private String sBaseNodeId = null;
	/**
	 * @uml.property  name="sBaseNodeType"
	 */
	private String sBaseNodeType = null;

	public OattrEditor(Session session, SSLSocketClient simClient) {
		super(session, true); // modal

		this.session = session;
		this.simClient = simClient;

		setTitle("Object Attribute");

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				doClose();
			}
		});

		// Start building the GUI
		JPanel oattrPane = new JPanel();
		oattrPane.setLayout(new GridBagLayout());

		JLabel oaNameLabel = new JLabel("Name:");
		JLabel oaDescrLabel = new JLabel("Description:");
		JLabel oaInfoLabel = new JLabel("Other Info:");

		oaNameField = new JTextField(23);
		oaDescrField = new JTextField(23);
		oaInfoField = new JTextField(23);

		addComp(oattrPane, oaNameLabel, 0, 0, 1, 1);
		addComp(oattrPane, oaDescrLabel, 0, 1, 1, 1);
		addComp(oattrPane, oaInfoLabel, 0, 2, 1, 1);

		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(oattrPane, oaNameField, 1, 0, 3, 1);
		addComp(oattrPane, oaDescrField, 1, 1, 3, 1);
		addComp(oattrPane, oaInfoField, 1, 2, 3, 1);
		oattrPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
		thePane.add(oattrPane, BorderLayout.NORTH);
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
		setTitle("Add Object Attribute");
		okButton.setText("Add attribute");

		oaNameField.setText(null);
		oaNameField.requestFocus();
		oaDescrField.setText(null);
		oaInfoField.setText(null);
		propField.setText(null);
		propListModel.clear();

		oaNameField.setEditable(true);
		oaDescrField.setEditable(true);
		oaInfoField.setEditable(true);

		sBaseNodeId = sId;
		sBaseNodeType = sType;
	}

	public void prepareForEdit(String sId) {
		setTitle("Edit Object attribute");
		okButton.setText("OK");
		editingMode = true;
		oattrToEditId = sId;
		Packet res = null;
		try {
			Packet cmd = CommandUtil.makeCmd("getAttrInfo", sId, sId, Session.PM_NODE_OATTR, "yes");
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
		oaNameField.setText(pieces[0]);
		oaDescrField.setText(res.getStringValue(1));
		oaInfoField.setText(res.getStringValue(2));

		oaNameField.setEditable(false);
		oaDescrField.setEditable(false);
		oaInfoField.setEditable(false);

		propField.setText(null);
		propListModel.clear();
		for (int i = 3; i < res.size(); i++) {
			propListModel.addElement(res.getStringValue(i));
		}
	}

	// Invoked when the user clicks the OK button.
	// If editing, close. Otherwise, add the attribute.
	private void doOk() {
		if (editingMode) {
			setVisible(false);
			return;
		}
		String sName = oaNameField.getText().trim();
		String sDescr = oaDescrField.getText().trim();
		String sInfo = oaInfoField.getText().trim();

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
			Packet cmd = CommandUtil.makeCmd(PMCommand.ADD_OATTR, session.getId(), null, // Added null By Gopi
					sName, sDescr, sInfo, sBaseNodeId, sBaseNodeType, "yes");
			int n = propListModel.getSize();
			if (n > 0)
				for (int i = 0; i < n; i++) {
					cmd.addItem(ItemType.CMD_ARG, (String) propListModel.get(i));
				}
			Packet res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in addOattr: " + res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in addOattr: " + e.getMessage());
			return;
		}
		oaNameField.setText(null);
		oaNameField.requestFocus();
		oaDescrField.setText(null);
		oaInfoField.setText(null);
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
				Packet cmd = CommandUtil.makeCmd("addProp", session.getId(),
						oattrToEditId, Session.PM_NODE_OATTR, "yes", sProp);
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
				Packet cmd = CommandUtil.makeCmd("removeProp",session.getId(), oattrToEditId,
						Session.PM_NODE_OATTR, "yes", sProp);
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
			doOk();
		} else if (e.getActionCommand().equalsIgnoreCase("add")) {
			doAddProp();
		} else if (e.getActionCommand().equalsIgnoreCase("remove")) {
			doRemoveProp();
		} else if (e.getActionCommand().equalsIgnoreCase("close")) {
			doClose();
		}
	}
}

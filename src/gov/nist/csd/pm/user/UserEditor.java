/*
 * UserEditor.java
 *
 * Created on April 7, 2005, 8:46 AM
 */

package gov.nist.csd.pm.user;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.SecureRandom;
import java.util.Vector;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class UserEditor extends JDialog implements ActionListener,
		ListSelectionListener {

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
	 * @uml.property  name="userNameField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField userNameField;
	/**
	 * @uml.property  name="userFullField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField userFullField;
	/**
	 * @uml.property  name="userInfoField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JTextField userInfoField;
	/**
	 * @uml.property  name="userPassField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JPasswordField userPassField;

	/**
	 * @uml.property  name="addButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton addButton;
	/**
	 * @uml.property  name="deleteButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton deleteButton;
	/**
	 * @uml.property  name="editButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton editButton;
	/**
	 * @uml.property  name="refreshButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton refreshButton;
	/**
	 * @uml.property  name="usersButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton usersButton;
	/**
	 * @uml.property  name="closeButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JButton closeButton;

	/**
	 * @uml.property  name="userListModel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private DefaultListModel userListModel;
	/**
	 * @uml.property  name="userList"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private JList userList;
	/**
	 * @uml.property  name="userVector"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	private Vector userVector;

	/**
	 * @uml.property  name="constraints"
	 */
	private GridBagConstraints constraints = new GridBagConstraints();

	// The base node is the user attribute or other kind of node the new user
	// will be assigned to (e.g., the connector node).
	/**
	 * @uml.property  name="sBaseNodeId"
	 */
	private String sBaseNodeId = null;
	/**
	 * @uml.property  name="sBaseNodeType"
	 */
	private String sBaseNodeType = null;

	public UserEditor(Session session, SSLSocketClient simClient) {
		super(session, true); // modal

		this.session = session;
		this.simClient = simClient;

		setTitle("Users");

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				close();
			}
		});

		// Start building the GUI
		JPanel userPane = new JPanel();
		userPane.setLayout(new GridBagLayout());

		JLabel userNameLabel = new JLabel("Logon Name:");
		JLabel userDescrLabel = new JLabel("Full Name:");
		JLabel userInfoLabel = new JLabel("Other Info:");
		JLabel userPassLabel = new JLabel("Password:");

		userNameField = new JTextField(20);
		userFullField = new JTextField(20);
		userInfoField = new JTextField(20);
		userPassField = new JPasswordField(20);

		constraints.insets = new Insets(0, 10, 0, 0);

		addComp(userPane, userNameLabel, 0, 0, 1, 1);
		addComp(userPane, userDescrLabel, 0, 1, 1, 1);
		addComp(userPane, userInfoLabel, 0, 2, 1, 1);

		addComp(userPane, userNameField, 1, 0, 3, 1);
		addComp(userPane, userFullField, 1, 1, 3, 1);
		addComp(userPane, userInfoField, 1, 2, 3, 1);

		constraints.insets = new Insets(10, 10, 0, 0);
		addComp(userPane, userPassLabel, 0, 3, 1, 1);
		addComp(userPane, userPassField, 1, 3, 3, 1);

		// The button pane
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridBagLayout());

		addButton = new JButton("Add user");
		addButton.setActionCommand("add");
		addButton.addActionListener(this);

		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(buttonPane, addButton, 1, 0, 1, 1);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		JPanel upperPane = new JPanel();
		upperPane.setLayout(new GridLayout(0, 1));
		upperPane.add(userPane);
		upperPane.add(buttonPane);
		upperPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createTitledBorder("New User")));

		// The list and its buttons
		userListModel = new DefaultListModel();
		userList = new JList(userListModel);
		userList.addListSelectionListener(this);
		JScrollPane userListScrollPane = new JScrollPane(userList);
		userListScrollPane.setPreferredSize(new Dimension(220, 100));

		constraints.insets = new Insets(0, 0, 0, 0);

		deleteButton = new JButton("Delete");
		deleteButton.setActionCommand("delete");
		deleteButton.addActionListener(this);

		editButton = new JButton("Edit");
		editButton.setActionCommand("edit");
		editButton.addActionListener(this);

		refreshButton = new JButton("Refresh");
		refreshButton.setActionCommand("refresh");
		refreshButton.addActionListener(this);

		usersButton = new JButton("Show attributes");
		usersButton.setActionCommand("show attributes");
		usersButton.addActionListener(this);

		Dimension d = new Dimension(80, 26);
		deleteButton.setPreferredSize(d);
		refreshButton.setPreferredSize(d);

		d = new Dimension(150, 26);
		usersButton.setPreferredSize(d);

		JPanel listButtonPane = new JPanel();
		listButtonPane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(listButtonPane, refreshButton, 0, 0, 1, 1);
		addComp(listButtonPane, deleteButton, 1, 0, 1, 1);
		addComp(listButtonPane, usersButton, 0, 1, 2, 1);

		JPanel middlePane = new JPanel();
		middlePane.setLayout(new GridBagLayout());

		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(middlePane, userListScrollPane, 0, 0, 2, 2);
		addComp(middlePane, listButtonPane, 2, 0, 2, 2);

		middlePane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createTitledBorder("Existing Users")));

		// Last buttons
		closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);

		JPanel lowerPane = new JPanel();
		// lowerPane.setLayout(new BorderLayout());
		lowerPane.add(closeButton);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(upperPane, BorderLayout.NORTH);
		contentPane.add(middlePane, BorderLayout.CENTER);
		contentPane.add(lowerPane, BorderLayout.SOUTH);

		setContentPane(contentPane);
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

	private Packet getUsers() {
		try {
			Packet cmd = CommandUtil.makeCmd("getUsers", session.getId());
			Packet res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in getUsers: " + res.getErrorMessage());
				return null;
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in getUsers: " + e.getMessage());
			return null;
		}
	}

	// This method must be called before making visible this frame,
	// to fill the Policy Classes JList and set the base node.
	public void prepare(String sId, String sType) {
		setTitle("Users");

		sBaseNodeId = sId;
		sBaseNodeType = sType;

		userNameField.setText(null);
		userNameField.requestFocus();
		userFullField.setText(null);
		userInfoField.setText(null);
		userPassField.setText(null);

		// Get the users and populate the list.
		Packet res = getUsers();

		userListModel.clear();
		if (userVector == null)
			userVector = new Vector();
		else
			userVector.clear();

		if (res != null)
			for (int i = 0; i < res.size(); i++) {
				String sLine = res.getStringValue(i);
				String[] pieces = sLine.split(SessionManager.PM_FIELD_DELIM);
				int index = SessionManager.getIndex(userListModel, pieces[0]);
				userListModel.add(index, pieces[0]);
				userVector.add(index, pieces[1]);
			}
	}

	private void close() {
		this.setVisible(false);
	}

	// Delete the selected user.
	private void delete() {
		int index = userList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(this, "Please select a user!");
			return;
		}
		String sUserId = (String) userVector.elementAt(index);

		// Send the command "delete <id> <type=user>" and let the server test
		// conditions.
		try {
			Packet cmd = CommandUtil.makeCmd("deleteNode", session.getId(),
                    sUserId, Session.PM_NODE_USER);
			Packet res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in deleteNode: "
						+ res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in deleteNode: " + e.getMessage());
			return;
		}

		prepare(sBaseNodeId, sBaseNodeType);
	}

	// This version hashes the password on the server.
	private void add() {
		String sName = userNameField.getText().trim();
		String sFull = userFullField.getText().trim();
		String sInfo = userInfoField.getText().trim();

		if (sName.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a user name!");
			return;
		}
		if (sFull.length() == 0) {
			JOptionPane.showMessageDialog(this,
					"Please enter the user's full name!");
			return;
		}
		if (sInfo.length() == 0)
			sInfo = sName;

		// Get a random 12-byte salt.
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[12];
		random.nextBytes(salt);

		// Get the password from the UI.
		char[] cPass = userPassField.getPassword();

		// Send the command and let the server set the GUID and test for unique
		// name.
		try {
			Packet cmd = CommandUtil.makeCmd("addUser", session.getId(), null, sName, // Added by Gopi  for ProcId Nov 2013
					sFull, sInfo, new String(cPass), (sBaseNodeId == null) ? ""
							: sBaseNodeId, (sBaseNodeType == null) ? ""
							: sBaseNodeType, "yes");
			Packet res = simClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this,
						"Error in addUser: " + res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Exception in addUser: " + e.getMessage());
			return;
		}

		prepare(sBaseNodeId, sBaseNodeType);
	}

	void printPass(byte[] salt, byte[] pass) {
		System.out.println("Salt has " + salt.length + " bytes");
		for (int i = 0; i < salt.length; i++)
			System.out.println(salt[i]);

		System.out.println("Pass has " + pass.length + " bytes");
		for (int i = 0; i < pass.length; i++)
			System.out.println(pass[i]);
	}

	// Edit the selected user.
	private void editSelectedUser() {
	}

	// Refresh the user list.
	private void refresh() {
		prepare(sBaseNodeId, sBaseNodeType);
	}

	// Select the class, but first unselect it to trigger a valueChanged() call.
	// NOTE that clearSelection() also triggers a valueChanged() with a
	// null selection. Thus, we have to test whether the selection is null
	// in valueChanged().
	private void selectClass(String sClass) {
	}

	byte byte2hexdigit(byte n) {
		if (n < 10)
			return (byte) ('0' + n);
		else
			return (byte) ('A' + n - 10);
	}

	String byteArray2HexString(byte[] inp) {
		byte[] buf = new byte[2 * inp.length];
		int inpix, outix;
		int n;
		byte q, r;

		for (inpix = outix = 0; inpix < inp.length; inpix++) {
			n = inp[inpix] & 0x000000FF;
			q = (byte) (n / 16);
			r = (byte) (n % 16);
			buf[outix++] = byte2hexdigit(q);
			buf[outix++] = byte2hexdigit(r);
		}
		return new String(buf);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("add")) {
			add();
		} else if (e.getActionCommand().equalsIgnoreCase("delete")) {
			delete();
		} else if (e.getActionCommand().equalsIgnoreCase("close")) {
			close();
		} else if (e.getActionCommand().equalsIgnoreCase("edit")) {
			editSelectedUser();
		} else if (e.getActionCommand().equalsIgnoreCase("refresh")) {
			refresh();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;
	}
}

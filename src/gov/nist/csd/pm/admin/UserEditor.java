/*
 * UserEditor.java
 *
 * Created on April 7, 2005, 8:46 AM
 */
package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.browser.PmGraphDirection;
import gov.nist.csd.pm.common.browser.PmGraphType;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class UserEditor extends JDialog implements ActionListener {

	/**
	 * @uml.property  name="tool"
	 * @uml.associationEnd  
	 */
	private PmAdmin tool;
	/**
	 * @uml.property  name="sslClient"
	 * @uml.associationEnd  
	 */
	private SSLSocketClient sslClient;
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
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
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
	private List<String> userVector;
	/**
	 * @uml.property  name="constraints"
	 */
	private GridBagConstraints constraints = new GridBagConstraints();
	// The base node is the user attribute or other kind of node the new user
	// will be assigned to (e.g., the connector node).
	/**
	 * @uml.property  name="baseNode"
	 * @uml.associationEnd  
	 */
	private PmNode baseNode = null;
	private JCheckBox schemBox;
	private static boolean schBuild;

	@SuppressWarnings({"CallToThreadDumpStack", "LeakingThisInConstructor"})
	public UserEditor(PmAdmin tool, SSLSocketClient sslClient) {
		super(tool, false);  // modal

		this.tool = tool;
		this.sslClient = sslClient;

		setTitle("Users");

		addWindowListener(new WindowAdapter() {
			@Override
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

		userPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createTitledBorder("New User")));
		
		JPanel schemaBuilder = new JPanel();
		schemaBuilder.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createTitledBorder("Add as Schema Builder")));
		schemaBuilder.setLayout(new FlowLayout(5, 5, 5));
		schemaBuilder.add(new JLabel("Check for access to Schema Builder:"));
		schemBox = new JCheckBox();

		// The list and its buttons
		userListModel = new DefaultListModel();
		userList = new JList(userListModel);
		JScrollPane userListScrollPane = new JScrollPane(userList);
		userListScrollPane.setPreferredSize(new Dimension(220, 200));
		userListScrollPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createTitledBorder("Existing Users")));

		addButton = new JButton("Add");
		addButton.setActionCommand("add");
		addButton.addActionListener(this);

		deleteButton = new JButton("Delete");
		deleteButton.setActionCommand("delete");
		deleteButton.addActionListener(this);

		editButton = new JButton("Edit");
		editButton.setActionCommand("edit");
		editButton.addActionListener(this);

		usersButton = new JButton("Show");
		usersButton.setActionCommand("show");
		usersButton.addActionListener(this);

		closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);

		JPanel buttonPane = new JPanel();
		buttonPane.add(addButton);
		buttonPane.add(deleteButton);
		buttonPane.add(usersButton);
		buttonPane.add(closeButton);

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(userPane, BorderLayout.NORTH);
		contentPane.add(userListScrollPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.SOUTH);

		setContentPane(contentPane);
		getRootPane().setDefaultButton(addButton);
	}

	private void addComp(Container container, Component component, int x, int y, int w, int h) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		container.add(component, constraints);
	}

	private Object getUsers() throws Exception {
		Packet res = null;
		try {
			Packet cmd = tool.makeCmd("getUsers");
			res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				throw new RuntimeException("Undetermined error");
			}
			if (res.hasError()) {
				throw new RuntimeException(res.getErrorMessage());
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return res;
	}

	// This method must be called before making visible this frame,
	// to fill the Policy Classes JList and set the base node.
	@SuppressWarnings("CallToThreadDumpStack")
	public void prepare(PmNode node) {
		setTitle("Users");

		Packet res = null;

		baseNode = node;


		userNameField.setText(null);
		userNameField.requestFocus();
		userFullField.setText(null);
		userInfoField.setText(null);
		userPassField.setText(null);

		// Get the users and populate the list.
		try {
			res = (Packet) getUsers();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(tool, "Exception in getUsers(), " + e.getMessage());
			return;
		}

		userListModel.clear();
		if (userVector == null) {
			userVector = new ArrayList<String>();
		} else {
			userVector.clear();
		}

		for (int i = 0; i < res.size(); i++) {
			String sLine = res.getStringValue(i);
			String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
			int index = PmAdmin.getIndex(userListModel, pieces[0]);
			userListModel.add(index, pieces[0]);
			userVector.add(index, pieces[1]);
		}
	}

	private void close() {
		this.setVisible(false);
	}

	// Delete the selected user.
	@SuppressWarnings("CallToThreadDumpStack")
	private void delete() {
		int index = userList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(this, "Please select a user!");
			return;
		}
		String sUserId = userVector.get(index);

		// Send the command "delete <id> <type=user>" and let the server test conditions.
		// The last argument means the node is not a VOS node.
		Packet res = null;
		try {
			Packet cmd = tool.makeCmd("deleteNode", sUserId, PmAdmin.PM_NODE_USER, "no");
			res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(tool, "Undetermined error, null result returned");
				return;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(tool, res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(tool, e.getMessage());
			return;
		}

		userListModel.remove(index);
		userVector.remove(index);
		baseNode.invalidate();
	}

	// This version hashes the password on the server.
	@SuppressWarnings("CallToThreadDumpStack")
	private void add() {
		String sName = userNameField.getText().trim();
		String sFull = userFullField.getText().trim();
		String sInfo = userInfoField.getText().trim();
		char[] cPass = userPassField.getPassword();

		if (sName.length() == 0) {
			JOptionPane.showMessageDialog(tool, "Please enter a user name!");
			return;
		}
		if (sFull.length() == 0) {
			JOptionPane.showMessageDialog(tool, "Please enter the user's full name!");
			return;
		}
		if (sInfo.length() == 0) {
			sInfo = sName;
		}

		if(cPass == null || cPass.length == 0){
			JOptionPane.showMessageDialog(tool, "Please specify a password");
			return;
		}

		schBuild = false;
		if(schemBox.isSelected()){
			schBuild = true;
		}
		// Get a random 12-byte salt.
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[12];
		random.nextBytes(salt);

		// Get the password from the UI.

		// Send the command and let the server set the GUID and test for unique name.
		Packet res = null;
		try {
			// The null argument of makeCmd is the process id.
			String baseNodeId = baseNode == null ? "" : baseNode.getId();
			String baseNodeType = baseNode == null ? "" : baseNode.getType();
			Packet cmd = tool.makeCmd("addUser", null, sName, sFull, sInfo,
					new String(cPass), baseNodeId, baseNodeType, "no");
			res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(tool, "Undetermined error, null result returned");
				return;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(tool, res.getErrorMessage());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(tool, e.getMessage());
			return;
		}

		//baseNode.invalidate();
		userList.clearSelection();
		String sLine = res.getStringValue(0);
		String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
		int index = PmAdmin.getIndex(userListModel, pieces[0]);
		userListModel.add(index, pieces[0]);
		userVector.add(index, pieces[1]);
	}
	
	public static boolean isBuilder(){
		return schBuild;
	}

	void printPass(byte[] salt, byte[] pass) {
		System.out.println("Salt has " + salt.length + " bytes");
		for (int i = 0; i < salt.length; i++) {
			System.out.println(salt[i]);
		}

		System.out.println("Pass has " + pass.length + " bytes");
		for (int i = 0; i < pass.length; i++) {
			System.out.println(pass[i]);
		}
	}

	// Edit the selected user.
	private void editSelectedUser() {
	}

	// Display the user attributes graph with the selected user as anchor.
	// The selected line in the pc list has the format sName:sId.
	private void showUserGraph() {
		int index = userList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(this, "Please select a user!");
			return;
		}
		String sUserId = userVector.get(index);
		String sUserName = (String) userListModel.elementAt(index);
		tool.setGraphParams(PmGraphType.USER_ATTRIBUTES, PmGraphDirection.DOWN,
				new PmNode(PmAdmin.PM_NODE_USER, sUserId, sUserName));
		//this.setVisible(false);
	}

	byte byte2hexdigit(byte n) {
		if (n < 10) {
			return (byte) ('0' + n);
		} else {
			return (byte) ('A' + n - 10);
		}
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("add")) {
			add();
		} else if (e.getActionCommand().equalsIgnoreCase("delete")) {
			delete();
		} else if (e.getActionCommand().equalsIgnoreCase("close")) {
			close();
		} else if (e.getActionCommand().equalsIgnoreCase("edit")) {
			editSelectedUser();
		} else if (e.getActionCommand().equalsIgnoreCase("show")) {
			showUserGraph();
		}
	}
}

package gov.nist.csd.pm.application.schema.builder;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gov.nist.csd.pm.admin.PmAdmin;
import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.net.Packet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class DenyEditor extends JPanel implements ActionListener, ListSelectionListener{

	private static final long serialVersionUID = 750254610731264938L;

	public static final String PM_DENY_USER_ID         = "user id";
	public static final String PM_DENY_SESSION         = "session";
	public static final String PM_DENY_PROCESS         = "process";
	public static final String PM_DENY_INTRA_SESSION   = "intra session";
	public static final String PM_DENY_ACROSS_SESSIONS = "across sessions";

	private Utilities util;
	private SysCaller sysCaller;
	private SchemaBuilder3 schema;
	private JTextField nameField;
	private GridBagConstraints constraints = new GridBagConstraints();
	private JRadioButton sessionButton;
	private JRadioButton processButton;
	private JRadioButton userButton;
	private JRadioButton attrButton;
	private JRadioButton interButton;
	private JLabel attrUserLabel;
	private JTextField attrUserField;
	private JCheckBox intersectBox;
	private JLabel attrUser2Label;
	private DefaultListModel attrUserListModel;
	private JList attrUserList;
	private DefaultListModel opListModel;
	private JList opList;
	private DefaultListModel contListModel;
	private JList contList;
	private JCheckBox complementBox;
	private DefaultListModel denyListModel;
	private JList denyList;
	private Vector<String> denyVector;
	private DefaultListModel op2ListModel;
	private JList op2List;
	private DefaultListModel cont2ListModel;
	private JList cont2List;
	private Vector<String> attrUserVector;
	private Vector<String> contVector;
	private Vector<String> cont2Vector;

	public DenyEditor(SchemaBuilder3 sb, Utilities u, SysCaller sys){
		schema = sb;
		util = u;
		sysCaller = sys;

		createGUI();
	}

	private void createGUI(){
		setPreferredSize(new Dimension(850, 570));
		setBorder(
				new CompoundBorder(
						new TitledBorder("Deny Editor"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);

		JLabel nameLabel = new JLabel("Constraint name:");
		nameField = new JTextField(19);
		JPanel namePane = new JPanel(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(namePane, nameLabel, 0, 0, 1, 1);
		addComp(namePane, nameField, 0, 1, 3, 1);
		//namePane.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

		JLabel typeLabel = new JLabel("Constraint type:");

		sessionButton = new JRadioButton("Session id-based");
		sessionButton.setActionCommand("sessionid");
		sessionButton.addActionListener(this);
		sessionButton.setSelected(true);

		processButton = new JRadioButton("Process id-based");
		processButton.setActionCommand("processid");
		processButton.addActionListener(this);
		//processButton.setSelected(true);

		userButton = new JRadioButton("User id-based");
		userButton.setActionCommand("userid");
		userButton.addActionListener(this);
		//userButton.setSelected(true);

		attrButton = new JRadioButton("Attribute-based intrasession");
		attrButton.setActionCommand("attr");
		attrButton.addActionListener(this);

		interButton = new JRadioButton("Attribute-based across sessions");
		interButton.setActionCommand("inter");
		interButton.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		group.add(sessionButton);
		group.add(processButton);
		group.add(userButton);
		group.add(attrButton);
		group.add(interButton);


		JPanel radioButtonPane = new JPanel(new GridLayout(5,1));
		radioButtonPane.add(sessionButton);
		radioButtonPane.add(processButton);
		radioButtonPane.add(userButton);
		radioButtonPane.add(attrButton);
		radioButtonPane.add(interButton);
		radioButtonPane.setBorder(BorderFactory.createLineBorder(Color.gray));

		JPanel typePane = new JPanel();
		typePane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(typePane, typeLabel, 0, 0, 1, 1);
		addComp(typePane, radioButtonPane, 0, 1, 3, 1);

		attrUserLabel = new JLabel("User:                      ");
		attrUserLabel.setEnabled(false);
		attrUserField = new JTextField(22);
		attrUserField.setEditable(false);

		intersectBox = new JCheckBox("Intersection");

		JPanel attrUserPane = new JPanel();
		attrUserPane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(attrUserPane, attrUserLabel, 0, 0, 1, 1);
		addComp(attrUserPane, attrUserField, 0, 1, 3, 1);
		constraints.insets = new Insets(5, 0, 0, 0);
		addComp(attrUserPane, intersectBox, 0, 2, 1, 1);
		attrUserPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

		JPanel upperLeftPane = new JPanel(new BorderLayout());
		upperLeftPane.add(namePane, BorderLayout.NORTH);
		upperLeftPane.add(typePane, BorderLayout.CENTER);
		upperLeftPane.add(attrUserPane, BorderLayout.SOUTH);

		// Upper right pane contains the user list or the user attribute list.
		attrUser2Label = new JLabel("Choose user:");
		attrUserListModel = new DefaultListModel();
		attrUserList = new JList(attrUserListModel);
		attrUserList.addListSelectionListener(this);
		JScrollPane attrUserListScrollPane = new JScrollPane(attrUserList);
		attrUserListScrollPane.setPreferredSize(new Dimension(150,230));

		JPanel upperRightPane = new JPanel();
		upperRightPane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(upperRightPane, attrUser2Label, 0, 0, 1, 1);
		addComp(upperRightPane, attrUserListScrollPane, 0, 1, 3, 1);
		upperRightPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));

		JPanel upperPane = new JPanel();
		upperPane.add(upperLeftPane);
		upperPane.add(upperRightPane);

		JPanel contInfoPanel = new JPanel();
		contInfoPanel.setLayout(new BorderLayout(5, 5));
		contInfoPanel.setBorder(new CompoundBorder(
				new TitledBorder("Constraint Information"),
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)
				));
		contInfoPanel.add(upperPane, BorderLayout.CENTER);

		// Middle pane will contain operations and containers to choose from.
		JLabel opLabel = new JLabel("Operations to add:");
		opListModel = new DefaultListModel();
		opList = new JList(opListModel);
		JScrollPane opListScrollPane = new JScrollPane(opList);
		opListScrollPane.setPreferredSize(new Dimension(175,175));

		JPanel opPane = new JPanel();
		opPane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(opPane, opLabel, 0, 0, 1, 1);
		addComp(opPane, opListScrollPane, 0, 1, 3, 1);
		opPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

		JLabel contLabel = new JLabel("Containers to add:         ");
		contListModel = new DefaultListModel();
		contList = new JList(contListModel);
		JScrollPane contListScrollPane = new JScrollPane(contList);
		contListScrollPane.setPreferredSize(new Dimension(175,140));
		complementBox = new JCheckBox("!Container's complement");

		//	    intersectBox = new JCheckBox("Intersection");

		JPanel contPane = new JPanel();
		contPane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(contPane, contLabel, 0, 0, 1, 1);
		addComp(contPane, contListScrollPane, 0, 1, 3, 1);
		constraints.insets = new Insets(10, 0, 0, 0);
		addComp(contPane, complementBox, 0, 2, 1, 1);
		//	    addComp(contPane, intersectBox, 2, 2, 1, 1);
		contPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));

		// Buttons.
		JButton addButton = new JButton("Add Constraint");
		addButton.addActionListener(this);
		addButton.setActionCommand("add");

		JButton deleteButton = new JButton("Delete Constraint");
		deleteButton.addActionListener(this);
		deleteButton.setActionCommand("delete");

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(this);
		refreshButton.setActionCommand("refresh");

		JPanel buttonPane = new JPanel();
		buttonPane.add(addButton);
		//buttonPane.add(deleteButton);
		//buttonPane.add(refreshButton);
		//buttonPane.add(closeButton);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

		JPanel middlePane = new JPanel();
		middlePane.setLayout(new BorderLayout(5, 5));
		JPanel subMid = new JPanel();
		subMid.add(opPane,  BorderLayout.WEST);
		subMid.add(contPane, BorderLayout.EAST);
		middlePane.add(subMid, BorderLayout.CENTER);
		middlePane.add(buttonPane, BorderLayout.SOUTH);

		JPanel additionPanel = new JPanel(new BorderLayout(5, 5));
		additionPanel.setBorder(new CompoundBorder(
				new TitledBorder("Constraint Additions"),
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)
				));
		additionPanel.add(middlePane, BorderLayout.CENTER);

		// The lower pane will contain existing denies with their operations and containers.
		JLabel conLabel = new JLabel("Deny constraints:");
		denyListModel = new DefaultListModel();
		denyList = new JList(denyListModel);
        denyList.setFixedCellWidth(200);
		denyList.addListSelectionListener(this);
		JScrollPane conListScrollPane = new JScrollPane(denyList);
		//conListScrollPane.setPreferredSize(new Dimension(130,160));

		JPanel conPane = new JPanel();
		conPane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(conPane, conLabel, 0, 0, 1, 1);
		addComp(conPane, conListScrollPane, 0, 1, 3, 1);
		conPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		JLabel op2Label = new JLabel("Operations:");
		op2ListModel = new DefaultListModel();
		op2List = new JList(op2ListModel);
        op2List.setFixedCellWidth(200);
		JScrollPane op2ListScrollPane = new JScrollPane(op2List);
		//op2ListScrollPane.setPreferredSize(new Dimension(155,160));

		JPanel op2Pane = new JPanel();
		op2Pane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
        addComp(op2Pane, op2Label, 0, 0, 1, 1);
		addComp(op2Pane, op2ListScrollPane, 0, 1, 3, 1);
		op2Pane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));


		JLabel cont2Label = new JLabel("Containers:");
		cont2ListModel = new DefaultListModel();
		cont2List = new JList(cont2ListModel);
		cont2List.setFixedCellWidth(200);
        JScrollPane cont2ListScrollPane = new JScrollPane(cont2List);
		//cont2ListScrollPane.setPreferredSize(new Dimension(155,160));

		JPanel cont2Pane = new JPanel();
		cont2Pane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(cont2Pane, cont2Label, 0, 0, 1, 1);
		addComp(cont2Pane, cont2ListScrollPane, 0, 1, 3, 1);
		cont2Pane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		JPanel deletePanel = new JPanel();
		deletePanel.setLayout(new BorderLayout(5, 5));
		deletePanel.add(deleteButton, BorderLayout.EAST);

		JPanel lowerPane = new JPanel();
		//lowerPane.setLayout(new BoxLayout(lowerPane, BoxLayout.X_AXIS));
		lowerPane.setLayout(new BorderLayout(5, 5));
		lowerPane.add(conPane, BorderLayout.WEST);
		lowerPane.add(op2Pane, BorderLayout.CENTER);
		lowerPane.add(cont2Pane, BorderLayout.EAST);
		lowerPane.add(deletePanel, BorderLayout.SOUTH);
		lowerPane.setBorder(new CompoundBorder(
				new TitledBorder("Exisiting Constraints"),
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)
				));

		JPanel entitiesPane = new JPanel(new BorderLayout());
		entitiesPane.add(contInfoPanel, BorderLayout.WEST);
		entitiesPane.add(additionPanel, BorderLayout.CENTER);
		entitiesPane.add(lowerPane, BorderLayout.SOUTH);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout(15, 15));
		JPanel subBottom = new JPanel();
		subBottom.setLayout(new BorderLayout(15, 15));
		subBottom.add(refreshButton, BorderLayout.WEST);
		bottomPanel.add(subBottom, BorderLayout.CENTER);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(entitiesPane, BorderLayout.CENTER);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);

		add(contentPane, BorderLayout.CENTER);

	}

	public void prepare(){
		denyListModel.clear();
		if (denyVector == null) denyVector = new Vector<String>();
		else denyVector.clear();

		Vector<String> res = getDenies();
		if (res != null) for (int i = 0; i < res.size(); i++) {
			String sLine = res.get(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(denyListModel, pieces[0]);
			denyListModel.add(index, pieces[0]);
			denyVector.add(index, pieces[1]);
		}
		op2ListModel.clear();
		cont2ListModel.clear();
		complementBox.setSelected(false);
		intersectBox.setSelected(false);

		attrUserListModel.clear();
		if (attrUserVector == null) attrUserVector = new Vector<String>();
		else attrUserVector.clear();

		userButton.setSelected(true);
		res = getUsers();    
		if (res != null) for (int i = 0; i < res.size(); i++) {
			String sLine = res.get(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(attrUserListModel, pieces[0]);
			attrUserListModel.add(index, pieces[0]);
			attrUserVector.add(index, pieces[1]);
		}


		opListModel.clear();
		res = getAllOps();
		System.out.println("ops" + res);
		if (res != null) for (int i = 0; i < res.size(); i++) {
			String sLine = res.get(i);
			int index = getIndex(opListModel, sLine);
			opListModel.add(index, sLine);
		}

		contListModel.clear();
		if (contVector == null) contVector = new Vector<String>();
		else contVector.clear();
		ArrayList<String> oas = getOattrs();
		if (oas != null) for (int i = 0; i < oas.size(); i++) {
			String sLine = oas.get(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(contListModel, pieces[0]);
			contListModel.add(index, pieces[0]);
			contVector.add(index, sysCaller.getIdOfEntityWithNameAndType(pieces[0], PM_NODE.OATTR.value));
		}
	}

	private Vector<String> getUsers() {
		Packet res = util.genCmd("getUsers");
		Vector<String> ret = new Vector<String>();

		for(int i = 0; i < res.size(); i++){
			ret.add(res.getStringValue(i));
		}

		return ret;
	}

	private Vector<String> getAllOps() {
		Packet res = util.genCmd("getAllOps");
		Vector<String> ret = new Vector<String>();

		for(int i = 0; i < res.size(); i++){
			ret.add(res.getStringValue(i));
		}
		System.out.println("ops" + ret);
		return ret;
	}

	private Vector<String> getDenies() {
		Packet res = util.genCmd("getDenies");
		Vector<String> ret = new Vector<String>();

		for(int i = 0; i < res.size(); i++){
			ret.add(res.getStringValue(i));
		}

		return ret;
	}

	public ArrayList<String> getOattrs() {
		//TODO just get all oattrs
		ArrayList<String> bases = util.getOattrs();
		System.out.println("BASES: " + bases);
		ArrayList<String> ret = new ArrayList<String>();
		for(int i = 0; i < bases.size(); i++){
			ret.add(bases.get(i).split(":")[0]);
		}
		Collections.sort(ret);
		return ret;
	}

	private void addComp(Container container, Component component, int x, int y, int w, int h) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		container.add(component, constraints);
	}

	private void doClose() {
		this.setVisible(false);
	}

	private void doRefresh() {
		attrUserList.clearSelection();
		opList.clearSelection();
		contList.clearSelection();
		denyList.clearSelection();
		op2ListModel.clear();
		cont2ListModel.clear();
		nameField.setText(null);
		attrUserField.setText(null);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		String sLine;
		String[] pieces;

		Object src = e.getSource();
		// The source is the user or attribute list.
		if (src == attrUserList) {
			String sUserOrAttr = (String)attrUserList.getSelectedValue();
			if (sUserOrAttr != null) {
				attrUserField.setText(sUserOrAttr);
			}
			return;
		}

		// The source is the deny list.
		opList.clearSelection();
		contList.clearSelection();

		String sDenyId = null;
		String sDenyName = null;
		int denyIndex = denyList.getSelectedIndex();
		if (denyIndex < 0) return;
		sDenyId = (String)denyVector.get(denyIndex);
		sDenyName = (String)denyListModel.get(denyIndex);

		// Get all information about this deny.
		Packet res = getDenyInfo(sDenyId);
		if (res == null) return;

		// The information returned by getDenyInfo has the following format:
		// item 0: <deny name>:<deny id>
		// item 1: <deny type>:<user or attribute name>:<user or attribute id>:<is intersection>
		// item 2: <operation count, opcount>
		// items 3 through 3 + opcount - 1: <operation>
		// item 3 + opcount: <container count, contcount>
		// item 3 + opcount + 1 through 3 + opcount + 1 + contcount - 1: <container name>:<container id>
		sLine = res.getStringValue(1);
		pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
		String sDenyType = pieces[0];
		String sUserOrAttrName = pieces[1];
		//String sUserOrAttrId = pieces[2];
		String sInters = pieces[3];

		if (sDenyType.equals(PM_DENY_SESSION)) {
			sessionButton.setSelected(true);
			sessButtonWasSelected();
		} else if (sDenyType.equals(PM_DENY_PROCESS)) {
			processButton.setSelected(true);
			procButtonWasSelected();
		} else if (sDenyType.equals(PM_DENY_USER_ID)) {
			userButton.setSelected(true);
			idButtonWasSelected();
		} else if (sDenyType.equals(PM_DENY_INTRA_SESSION)) {
			attrButton.setSelected(true);
			attrButtonWasSelected();
		} else {
			interButton.setSelected(true);
			interButtonWasSelected();
		}

		intersectBox.setSelected(sInters.equalsIgnoreCase("yes"));

		// Set these fields after the correct radio button is selected, because
		// the manual selection of a button clears the fields.
		nameField.setText(sDenyName);
		attrUserField.setText(sUserOrAttrName);

		op2ListModel.clear();
		int opCount = Integer.valueOf(res.getStringValue(2)).intValue();
		for (int i = 3; i < 3 + opCount; i++) {
			sLine = res.getStringValue(i);
			int index = getIndex(op2ListModel, sLine);
			op2ListModel.add(index, sLine);
		}

		cont2ListModel.clear();
		if (cont2Vector == null) cont2Vector = new Vector<String>();
		else cont2Vector.clear();

		int contCount = Integer.valueOf(res.getStringValue(3 + opCount)).intValue();
		for (int i = 4 + opCount; i < 4 + opCount + contCount; i++) {
			sLine = res.getStringValue(i);
			pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(cont2ListModel, pieces[0]);
			cont2ListModel.add(index, pieces[0]);
			cont2Vector.add(index, pieces[1]);
		}
        op2List.setFixedCellWidth(200);
        denyList.setFixedCellWidth(200);
        cont2List.setFixedCellWidth(200);
	}

	// The command parameters are: name, class, user or uattr name,
	// user or uattr id, oattr name, oattr id.
	// The constraint name is mandatory. All others are optional, but:
	//
	// If the constraint does not exist, the class, user or uattr name,
	// and user or uattr id are mandatory.
	// If the constraint exists, the class, user or uattr name, and
	// user or uattr id, if present in the command, must match the ones
	// already registered with the constraint in the engine.
	private void doAdd() {
		String sDenyName = nameField.getText().trim();
		if (sDenyName.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a new constraint name or select a deny constraint!");
			return;
		}

		String sDenyType;
		if (sessionButton.isSelected()) sDenyType = PM_DENY_SESSION;
		else if (processButton.isSelected()) sDenyType = PM_DENY_PROCESS;
		else if (userButton.isSelected()) sDenyType = PM_DENY_USER_ID;
		else if (attrButton.isSelected()) sDenyType = PM_DENY_INTRA_SESSION;
		else sDenyType = PM_DENY_ACROSS_SESSIONS;

		String sUserOrAttrId = null;
		String sUserOrAttrName = null;

		if (sDenyType.equals(PM_DENY_PROCESS)) {
			sUserOrAttrId = attrUserField.getText();
			sUserOrAttrName = attrUserField.getText();
		} else {
			int attrUserIndex = attrUserList.getSelectedIndex();
			if (attrUserIndex >= 0) {
				sUserOrAttrId = (String)attrUserVector.get(attrUserIndex);
				sUserOrAttrName = (String)attrUserListModel.get(attrUserIndex);
			}
		}

		String sOp = (String)opList.getSelectedValue();

		String sOattrId = null;
		String sOattrName = null;
		int oattrIndex = contList.getSelectedIndex();
		if (oattrIndex >= 0) {
			sOattrName = (String)contListModel.get(oattrIndex);
			if (complementBox.isSelected()) sOattrName = "!" + sOattrName;
			sOattrId = (String)contVector.get(oattrIndex);
		}

		//FIXME commented these out to see if they were the reason the lists were changing sizes
		//attrUserList.clearSelection();
		//opList.clearSelection();
		//contList.clearSelection();

		Packet res = util.genCmd("addDeny", sDenyName, sDenyType,
				(sUserOrAttrName == null)? "" : sUserOrAttrName,
						(sUserOrAttrId == null)? "" : sUserOrAttrId,
								(sOp == null)? "" : sOp,
										(sOattrName == null)? "" : sOattrName,
												(sOattrId == null)? "" : sOattrId,
														intersectBox.isSelected()? "yes" : "no");
		if (res == null) {
			JOptionPane.showMessageDialog(this, "Null answer from the engine!");
			return;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}

		// If the add operation is successful, the result contains <name>:<id> of
		// the deny constraint.
		String sNew = res.getStringValue(0);
		String[] pieces = sNew.split(GlobalConstants.PM_FIELD_DELIM);
		if (!denyListModel.contains(pieces[0])) {
			int index = getIndex(denyListModel, pieces[0]);
			denyListModel.add(index, pieces[0]);
			denyList.ensureIndexIsVisible(index);
			denyVector.add(index, pieces[1]);
		}
		selectDeny(pieces[1]);
	}

	private void selectDeny(String sDenyId) {
		int index = denyVector.indexOf(sDenyId);
		if (index < 0) {
			JOptionPane.showMessageDialog(this, "No such deny constraint to display!");
			return;
		}
		denyList.clearSelection();
		denyList.setSelectedIndex(index);
	}

	// Command parameters: constraint name (mandatory), operation name, container name,
	// container id. If the container name and id are both present, they must be
	// consistent.
	private void doDelete() {
		int denyIndex = denyList.getSelectedIndex();
		if (denyIndex < 0) {
			JOptionPane.showMessageDialog(this, "Please select a deny constraint!");
			return;
		}
		String sDenyId = (String)denyVector.get(denyIndex);
		String sDenyName = (String)denyListModel.get(denyIndex);

		String sOp = (String)op2List.getSelectedValue();

		String sOattrId = null;
		String sOattrName = null;
		int contIndex = cont2List.getSelectedIndex();
		if (contIndex >= 0) {
			sOattrName = (String)cont2ListModel.get(contIndex);
			sOattrId = (String)cont2Vector.get(contIndex);
		}

		Packet res = util.genCmd("deleteDeny", sDenyName,
				(sOp == null)? "" : sOp,
						(sOattrName == null)? "" : sOattrName,
								(sOattrId == null)? "" : sOattrId);
		if (res == null) {
			JOptionPane.showMessageDialog(this, "Null answer from the engine!");
			return;
		}
		if (res.hasError()) {
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}

		// If the delete operation is successful, delete from lists.
		if (sOp != null) op2ListModel.removeElement(sOp);
		if (contIndex >= 0) {
			cont2ListModel.removeElementAt(contIndex);
			cont2Vector.removeElementAt(contIndex);
		}
		if (sOp == null && contIndex < 0) {
			denyListModel.removeElementAt(denyIndex);
			denyVector.removeElementAt(denyIndex);
			op2ListModel.clear();
			cont2ListModel.clear();
			if (cont2Vector == null) cont2Vector = new Vector<String>();
			else cont2Vector.clear();
		}
	}

	private void sessButtonWasSelected() {
		attrUserLabel.setText("Session:                    ");
		attrUser2Label.setText("Choose session:           ");

		nameField.setText("");
		attrUserField.setText("");
		attrUserField.setEditable(false);

		attrUserListModel.clear();
		if (attrUserVector == null) attrUserVector = new Vector<String>();
		else attrUserVector.clear();

		Packet res = getSessions();
		if (res != null) for (int i = 0; i < res.size(); i++) {
			String sLine = res.getStringValue(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(attrUserListModel, pieces[0]);
			attrUserListModel.add(index, pieces[0]);
			attrUserVector.add(index, pieces[1]);
		}
	}

	private void procButtonWasSelected() {
		attrUserLabel.setText("Process:                    ");
		attrUser2Label.setText("                           ");

		nameField.setText("");
		attrUserField.setText("");
		attrUserField.setEditable(true);

		attrUserListModel.clear();
		if (attrUserVector == null) attrUserVector = new Vector<String>();
		else attrUserVector.clear();
	}

	private void idButtonWasSelected() {
		attrUserLabel.setText("User:                      ");
		attrUser2Label.setText("Choose user:            ");

		nameField.setText("");
		attrUserField.setText("");
		attrUserField.setEditable(false);

		attrUserListModel.clear();
		if (attrUserVector == null) attrUserVector = new Vector<String>();
		else attrUserVector.clear();

		Vector<String> res = getUsers();
		if (res != null) for (int i = 0; i < res.size(); i++) {
			String sLine = res.get(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(attrUserListModel, pieces[0]);
			attrUserListModel.add(index, pieces[0]);
			attrUserVector.add(index, pieces[1]);
		}
	}

	private void attrButtonWasSelected() {
		attrUserLabel.setText("User attribute:        ");
		attrUser2Label.setText("Choose user attribute:");

		nameField.setText("");
		attrUserField.setText("");
		attrUserField.setEditable(false);

		attrUserListModel.clear();
		if (attrUserVector == null) attrUserVector = new Vector<String>();
		else attrUserVector.clear();

		Vector<String> res = getUserAttributes();
		if (res != null) for (int i = 0; i < res.size(); i++) {
			String sLine = res.get(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(attrUserListModel, pieces[0]);
			attrUserListModel.add(index, pieces[0]);
			attrUserVector.add(index, pieces[1]);
		}
	}

	private Vector<String> getUserAttributes(){
		Packet res = util.getUserAttributes();
		Vector<String> ret = new Vector<String>();

		for(int i = 0; i < res.size(); i++){
			ret.add(res.getStringValue(i));
		}

		return ret;
	}

	private void interButtonWasSelected() {
		attrUserLabel.setText("User attribute:        ");
		attrUser2Label.setText("Choose user attribute:");

		nameField.setText("");
		attrUserField.setText("");
		attrUserField.setEditable(false);

		attrUserListModel.clear();
		if (attrUserVector == null) attrUserVector = new Vector<String>();
		else attrUserVector.clear();

		Vector<String> res = getUserAttributes();
		if (res != null) for (int i = 0; i < res.size(); i++) {
			String sLine = res.get(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(attrUserListModel, pieces[0]);
			attrUserListModel.add(index, pieces[0]);
			attrUserVector.add(index, pieces[1]);
		}
	}

	private Packet getSessions() {
		return util.genCmd("getSessinos");
	}

	private Packet getDenyInfo(String sDenyId) {
		return util.genCmd("getDenyInfo", sDenyId);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("add")) {
			doAdd();
		} else if (e.getActionCommand().equalsIgnoreCase("delete")) {
			doDelete();
		} else if (e.getActionCommand().equalsIgnoreCase("refresh")) {
			doRefresh();
		} else if (e.getActionCommand().equalsIgnoreCase("sessionid")) {
			sessButtonWasSelected();
		} else if (e.getActionCommand().equalsIgnoreCase("processid")) {
			procButtonWasSelected();
		} else if (e.getActionCommand().equalsIgnoreCase("userid")) {
			idButtonWasSelected();
		} else if (e.getActionCommand().equalsIgnoreCase("attr")) {
			attrButtonWasSelected();
		} else if (e.getActionCommand().equalsIgnoreCase("inter")) {
			interButtonWasSelected();
		}
	}

	// Find and return where to insert a new string in an alphabetically ordered list.
	public static int getIndex(ListModel model, String target) {
		int high = model.getSize(), low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (target.compareToIgnoreCase((String) model.getElementAt(probe)) < 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		return (low + 1);
	}

	// Find and return where to insert a new string in an alphabetically ordered
	// JComboBox.
	public static int getIndex(JComboBox combo, String target) {
		return getIndex(combo.getModel(), target);
	}

	public static int getIndex(List<String> v, String target) {
		int high = v.size(), low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (target.compareToIgnoreCase(v.get(probe)) < 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		return (low + 1);
	}

	public static void main(String[] args){
		JFrame j = new JFrame();
		j.add(new DenyEditor(null, null, null));
		j.pack();
		j.setVisible(true);
	}
}
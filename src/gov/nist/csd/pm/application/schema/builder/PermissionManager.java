package gov.nist.csd.pm.application.schema.builder;

import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.net.Packet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class PermissionManager extends JPanel implements ActionListener, ListSelectionListener{

	private Utilities util;
	private SchemaBuilder3 schemabuilder;
	private GridBagConstraints constraints = new GridBagConstraints();
	private JButton resetButton;
	private JTextField opsetField;
	private JTextField descrField;
	private JTextField infoField;
	private JComboBox classCombo;
	private DefaultListModel opListModel;
	private JList opList;
	private JButton addButton;
	private DefaultListModel opsetListModel;
	private JList opsetList;
	private JTextField classField;
	private DefaultListModel op2ListModel;
	private JList op2List;
	private JButton deleteButton;
	private PmNode baseNode;
	private Vector opsetVector;

	public PermissionManager(SchemaBuilder3 sb, Utilities u, SysCaller s){
		util = u;
		schemabuilder = sb;
		createGUI();
	}

	private void createGUI() {
		// Start building the GUI.
		JPanel opsetPane = new JPanel();
		opsetPane.setLayout(new GridBagLayout());

		JLabel newOpsetLabel = new JLabel("New Operation Set:");

		JLabel opsetLabel = new JLabel("Opset Name:");
		JLabel descrLabel = new JLabel("Description:");
		JLabel infoLabel = new JLabel("Other Info:");
		JLabel classLabel = new JLabel("Object Class:");

		opsetField = new JTextField(20);
		descrField = new JTextField(20);
		infoField = new JTextField(20);

		classCombo = new JComboBox();
		classCombo.setPreferredSize(new Dimension(220,25));
		classCombo.addActionListener(this);
		classCombo.setActionCommand("classcombo");

		JLabel opLabel = new JLabel("Operations To Add:");
		opListModel = new DefaultListModel();
		opList = new JList(opListModel);
		JScrollPane opListScrollPane = new JScrollPane(opList);
		opListScrollPane.setPreferredSize(new Dimension(240,100));

		constraints.insets = new Insets(0, 0, 5, 0);
		addComp(opsetPane, newOpsetLabel, 1, 0, 1, 1);

		addComp(opsetPane, opsetLabel, 0, 1, 1, 1);
		addComp(opsetPane, opsetField, 1, 1, 3, 1);

		addComp(opsetPane, descrLabel, 0, 2, 1, 1);
		addComp(opsetPane, descrField, 1, 2, 3, 1);

		addComp(opsetPane, infoLabel, 0, 3, 1, 1);
		addComp(opsetPane, infoField, 1, 3, 3, 1);

		addComp(opsetPane, classLabel, 0, 4, 1, 1);
		addComp(opsetPane, classCombo, 1, 4, 3, 1);

		constraints.insets = new Insets(0, 10, 5, 0);
		addComp(opsetPane, opLabel, 4, 0, 1, 1);
		addComp(opsetPane, opListScrollPane, 4, 1, 4, 4);

		// The button pane
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridBagLayout());

		addButton = new JButton("Add");
		addButton.setActionCommand("add");
		addButton.addActionListener(this);

		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(buttonPane, addButton, 0, 0, 1, 1);

		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

		JPanel upperPane = new JPanel();
		upperPane.setLayout(new BorderLayout());
		upperPane.add(opsetPane, BorderLayout.CENTER);
		upperPane.add(buttonPane, BorderLayout.SOUTH);
		upperPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 0, 0),
				BorderFactory.createTitledBorder("New Operation Set")));

		// The lists pane
		JPanel listPane = new JPanel();
		listPane.setLayout(new GridBagLayout());

		JLabel opsetsLabel = new JLabel("Operation Sets:");
		opsetListModel = new DefaultListModel();
		opsetList = new JList(opsetListModel);
		opsetList.addListSelectionListener(this);
		JScrollPane opsetListScrollPane = new JScrollPane(opsetList);
		opsetListScrollPane.setPreferredSize(new Dimension(240, 200));

		JLabel class2Label = new JLabel("Class:");
		classField = new JTextField(22);

		JLabel op2Label = new JLabel("Operations:");
		op2ListModel = new DefaultListModel();
		op2List = new JList(op2ListModel);
		JScrollPane op2ListScrollPane = new JScrollPane(op2List);
		op2ListScrollPane.setPreferredSize(new Dimension(240,160));

		constraints.insets = new Insets(0, 0, 5, 0);
		addComp(listPane, opsetsLabel, 0, 0, 1, 1);
		addComp(listPane, opsetListScrollPane, 0, 1, 4, 5);

		constraints.insets = new Insets(0, 10, 5, 0);
		addComp(listPane, class2Label, 4, 0, 1, 1);
		addComp(listPane, classField, 4, 1, 4, 1);

		addComp(listPane, op2Label, 4, 2, 1, 1);
		addComp(listPane, op2ListScrollPane, 4, 3, 4, 3);


		// The list button pane
		JPanel listButtonPane = new JPanel();
		listButtonPane.setLayout(new GridBagLayout());

		deleteButton = new JButton("Delete");
		deleteButton.setActionCommand("delete");
		deleteButton.addActionListener(this);

		constraints.insets = new Insets(0, 0, 0, 0);
		addComp(listButtonPane, deleteButton, 0, 0, 1, 1);

		listButtonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));


		JPanel middlePane = new JPanel();
		middlePane.setLayout(new BorderLayout());
		middlePane.add(listPane, BorderLayout.CENTER);
		middlePane.add(listButtonPane, BorderLayout.SOUTH);
		middlePane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createTitledBorder("Existing Operation Sets")));


		resetButton = new JButton("Reset");
		resetButton.setActionCommand("reset");
		resetButton.addActionListener(this);

		JPanel lowerPane = new JPanel();
		lowerPane.add(resetButton); 

		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(upperPane, BorderLayout.NORTH);
		contentPane.add(middlePane, BorderLayout.CENTER);
		contentPane.add(lowerPane, BorderLayout.SOUTH);

		add(contentPane);
	}

	private void addComp(Container container, Component component, int x, int y, int w, int h) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		container.add(component, constraints);
	}

	// Returns <name>:<id>.
	private Object getOpsets() throws Exception {
		Packet res = util.genCmd("getOpsets");
		return res;
	}

	// Returns <name>.
	private Object getObjClasses() throws Exception {
		Packet res = util.genCmd("getObjClasses");
		return res;
	}

	private Object getObjClassOps(String sClass) throws Exception {
		Packet res = util.genCmd("getObjClassOps", sClass);
		return res;
	}

	// This method must be called before making visible this frame.
	// Parameters:
	// sId, sType: if this method is called in order to add an opset to a user
	// attribute or object attribute (called "base node" for the new opset),
	// then sId and sType contain the id and type of the base node. Otherwise
	// (for example if the method is called when you select the "Operation sets..."
	// menu) they are null.
	// sIdToDisplay, sNameToDisplay: the id and name of an opset this method
	// is called on to display. Otherwise null.
	public void prepareAndSetBaseNode(PmNode baseNode, String selectedOpsetId) {
		this.baseNode = baseNode;

		Packet res = null;

		opsetField.setText("");
		opsetField.requestFocus();
		descrField.setText("");
		infoField.setText("");
		classCombo.removeAllItems();

		classField.setText("");

		op2ListModel.clear();

		// Get the object classes.
		try {
			res = (Packet)getObjClasses();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Exception in getObjClasses(), " + e.getMessage());
			return;
		}
		for (int i = 0; i < res.size(); i++) {
			String sClass = res.getStringValue(i);
			int index = getIndex(classCombo, sClass);
			classCombo.insertItemAt(sClass, index);
		}

		// Get the operations for the class that is currently selected in the class combo box.
		String sClass = (String)classCombo.getSelectedItem();
		if (sClass != null) {
			try {
				res = (Packet)getObjClassOps(sClass);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Exception in getObjClassOps(), " + e.getMessage());
				return;
			}
			opListModel.clear();
			if (res != null) for (int i = 0; i < res.size(); i++) {
				String sLine = res.getStringValue(i);
				int index = getIndex(opListModel, sLine);
				opListModel.add(index, sLine);
			}
		}

		// Get the opsets.
		try {
			res = (Packet)getOpsets();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Exception in getOpsets(), " + e.getMessage());
			return;
		}

		opsetListModel.clear();
		if (opsetVector == null) opsetVector = new Vector();
		else opsetVector.clear();

		if (res != null) for (int i = 0; i < res.size(); i++) {
			String sLine = res.getStringValue(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(opsetListModel, pieces[0]);
			opsetListModel.add(index, pieces[0]);
			opsetVector.add(index, pieces[1]);
		}

		// If sIdToDisplay is not null, select the corresponding opset in the list.
		if (selectedOpsetId != null) {
			selectOpset(selectedOpsetId);
		}
	}

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

	private void reset() {
		opsetField.setText("");
		descrField.setText("");
		infoField.setText("");
		classCombo.removeAll();;
		opListModel.clear();
		opList.removeAll();
		opsetListModel.clear();
		opsetList.removeAll();
		classField.setText("");
		op2ListModel.clear();
		op2List.removeAll();
		opsetVector.clear();
	}

	// Delete an op from an op set or an entire op set.
	// What we delete depends on what is selected.
	// If the op set and one of its ops are selected, only the op is deleted from
	// that op set.
	// If only the op set is selected, the op set will be deleted.
	private void delete() {
		int index = opsetList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(this, "Please select an operation set!");
			return;
		}
		String sOpsetId = (String)opsetVector.elementAt(index);
		String sOp = (String)op2List.getSelectedValue();

		// Send the command and let the server test the other conditions.
		Packet res = util.genCmd("deleteOpsetAndOp", sOpsetId,
				(sOp == null || sOp.length() == 0)? "" : sOp);


		if (sOp == null || sOp.length() == 0) {
			opsetListModel.removeElementAt(index);
			opsetList.clearSelection();
			opsetVector.removeElementAt(index);
			op2ListModel.removeAllElements();
		} else {
			op2ListModel.removeElement(sOp);
			opList.clearSelection();
		}
		opsetField.setText(null);
		descrField.setText(null);
		infoField.setText(null);
	}

	// Add 1) an op to an op set, or 2) add an op set, or 3) add an op set and an op.
	// If the op set already exists, an operation must be selected to be added.
	// If the op set does not exist, you may or may not select an operation to be
	// added. The op set will be added together with the selected operation,
	// if any. After adding the op set, its object class cannot be changed, but
	// you can delete the op set.
	private void add() {
		String sOpsetName = opsetField.getText().trim();
		String sDescr = descrField.getText().trim();
		String sInfo = infoField.getText().trim();
		if (sOpsetName.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter an operation set name!");
			opsetField.requestFocus();
			return;
		}
		if (sDescr.length() == 0) sDescr = sOpsetName;
		if (sInfo.length() == 0) sInfo = sOpsetName;

		String sOp = (String)opList.getSelectedValue();
		if (sOp != null) {
			opList.setSelectedIndex(-1);
		}

		opsetField.requestFocus();

		// Send the command and let the server test the other conditions.
		String baseNodeId = baseNode == null ? "" : baseNode.getId();
		String baseNodeType = baseNode == null ? "" : baseNode.getType();
		Packet res = util.genCmd("addOpsetAndOp", sOpsetName, sDescr, sInfo,
				(sOp == null || sOp.length() == 0)? "" : sOp,baseNodeId,baseNodeType);

		// If the add operation is successful, the result contains <name>:<id> of
		// the opset.
		String sLine = res.getStringValue(0);
		String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
		// If the oset is new, add it to the opset list and vector.
		if (!opsetListModel.contains(pieces[0])) {
			int index = getIndex(opsetListModel, pieces[0]);
			opsetListModel.add(index, pieces[0]);
			opsetList.ensureIndexIsVisible(index);
			opsetVector.add(index, pieces[1]);
		}
		// Update the graph image - has effect onnly if the graph type is CAPS.
		selectOpset(pieces[1]);
	}

	// Select the op set, but first unselect it to trigger a valueChanged() call.
	// NOTE that clearSelection() also triggers a valueChanged() with a
	// null selection. Thus, we have to test whether the selection is null
	// in valueChanged().
	// Note that the argument sOpset contains the <name>:<id>.
	private void selectOpset(String sOpsetId) {
		int index = opsetVector.indexOf(sOpsetId);
		if (index < 0) {
			JOptionPane.showMessageDialog(this, "No such opset to display!");
			return;
		}
		opsetList.clearSelection();
		opsetList.setSelectedIndex(index);
		opsetList.ensureIndexIsVisible(index);
	}

	private void selectedClassChanged() {
		Packet res = null;

		// Get the operations for the class that is currently selected in the class combo box.
		String sClass = (String)classCombo.getSelectedItem();
		if (sClass != null) {
			try {
				res = (Packet)getObjClassOps(sClass);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "Exception in getObjClassOps(), " + e.getMessage());
				return;
			}
			opListModel.clear();
			for (int i = 0; i < res.size(); i++) {
				String sLine = res.getStringValue(i);
				int index = getIndex(opListModel, sLine);
				opListModel.add(index, sLine);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("add")) {
			add();
		} else if (e.getActionCommand().equalsIgnoreCase("refresh")) {
			prepareAndSetBaseNode(baseNode, null);
		} else if (e.getActionCommand().equalsIgnoreCase("delete")) {
			delete();
		} else if (e.getActionCommand().equalsIgnoreCase("reset")) {
			reset();
		} else if (e.getActionCommand().equalsIgnoreCase("classcombo")) {
			selectedClassChanged();
		}
	}

	// The op set selected in the op set list has changed (change is forced even
	// when the op set already was there but we added some operation to it).
	// Display all information about it.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		// Get the selected value, which can be null.
		int index = opsetList.getSelectedIndex();
		if (index < 0) return;
		String sOpsetName = (String)opsetListModel.get(index);
		String sOpsetId = (String)opsetVector.get(index);
		opsetField.setText(sOpsetName);

		// Get all information about this opset: id, name, descr, info, class, ops
		// in this order.
		Packet res = util.genCmd("getOpsetInfo", sOpsetId);

		descrField.setText(res.getStringValue(2));
		infoField.setText(res.getStringValue(3));
		String sClass = res.getStringValue(4);
		classCombo.setSelectedItem(sClass);
		classField.setText(sClass);

		op2ListModel.clear();
		for (int i = 5; i < res.size(); i++) {
			op2ListModel.addElement(res.getStringValue(i));
		}
	}
}
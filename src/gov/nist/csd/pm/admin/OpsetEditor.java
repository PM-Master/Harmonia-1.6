/*
 * OpsetEditor.java
 *
 * Created on April 25, 2005, 11:41 AM
 */

package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;



/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class OpsetEditor extends JDialog implements ActionListener, ListSelectionListener {

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
 * @uml.property  name="opsetField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField opsetField;                // The new op set name.
  /**
 * @uml.property  name="descrField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField descrField;                // The new op set descr.
  /**
 * @uml.property  name="infoField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField infoField;                 // The new op set info.

  /**
 * @uml.property  name="classCombo"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JComboBox classCombo;                 // The new op set object class.

  /**
 * @uml.property  name="opListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel opListModel;         // Model and list of operations
  /**
 * @uml.property  name="opList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList opList;                         // to add.
  
  /**
 * @uml.property  name="addButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton addButton;                    // The add opset button.

  
  /**
 * @uml.property  name="opsetListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel opsetListModel;      // Model and list of existing opsets.
  /**
 * @uml.property  name="opsetList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList opsetList;
  /**
 * @uml.property  name="opsetVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector opsetVector;
  
  /**
 * @uml.property  name="classField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField classField;                // The class name of the selected opset.
  
  /**
 * @uml.property  name="op2ListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel op2ListModel;         // Model and list of the selected
  /**
 * @uml.property  name="op2List"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList op2List;                         // opset's operations.
  
  /**
 * @uml.property  name="deleteButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton deleteButton;                 // The delete opset/operation button.

  /**
 * @uml.property  name="closeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton closeButton;                  // The close button.

  /**
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();

  // The base node is either the uattr assigned to this opset,
  // or the oattr this opset is assigned to, or null.
  /**
 * @uml.property  name="baseNode"
 * @uml.associationEnd  
 */
PmNode baseNode;

  public OpsetEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);  // modal

    this.tool = tool;
    this.sslClient = sslClient;

    setTitle("Operation Sets");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

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

    
    closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);

    JPanel lowerPane = new JPanel();
    lowerPane.add(closeButton); 

    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(upperPane, BorderLayout.NORTH);
    contentPane.add(middlePane, BorderLayout.CENTER);
    contentPane.add(lowerPane, BorderLayout.SOUTH);

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

  // Returns <name>:<id>.
  private Object getOpsets() throws Exception {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getOpsets");
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

  // Returns <name>.
  private Object getObjClasses() throws Exception {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getObjClasses");
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

  private Object getObjClassOps(String sClass) throws Exception {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getObjClassOps", sClass);
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
      JOptionPane.showMessageDialog(tool, "Exception in getObjClasses(), " + e.getMessage());
      return;
    }
    for (int i = 0; i < res.size(); i++) {
      String sClass = res.getStringValue(i);
      int index = PmAdmin.getIndex(classCombo, sClass);
      classCombo.insertItemAt(sClass, index);
    }

    // Get the operations for the class that is currently selected in the class combo box.
    String sClass = (String)classCombo.getSelectedItem();
    if (sClass != null) {
      try {
        res = (Packet)getObjClassOps(sClass);
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(tool, "Exception in getObjClassOps(), " + e.getMessage());
        return;
      }
      opListModel.clear();
      if (res != null) for (int i = 0; i < res.size(); i++) {
        String sLine = res.getStringValue(i);
        int index = PmAdmin.getIndex(opListModel, sLine);
        opListModel.add(index, sLine);
      }
    }
    
    // Get the opsets.
    try {
      res = (Packet)getOpsets();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception in getOpsets(), " + e.getMessage());
      return;
    }

    opsetListModel.clear();
    if (opsetVector == null) opsetVector = new Vector();
    else opsetVector.clear();

    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(opsetListModel, pieces[0]);
      opsetListModel.add(index, pieces[0]);
      opsetVector.add(index, pieces[1]);
    }
    
    // If sIdToDisplay is not null, select the corresponding opset in the list.
    if (selectedOpsetId != null) {
      selectOpset(selectedOpsetId);
    }
  }

  private void close() {
    this.setVisible(false);
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
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("deleteOpsetAndOp", sOpsetId,
        (sOp == null || sOp.length() == 0)? "" : sOp);
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
      JOptionPane.showMessageDialog(tool, "Please enter an operation set name!");
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
    Packet res = null;
    try {
      String baseNodeId = baseNode == null ? "" : baseNode.getId();
      String baseNodeType = baseNode == null ? "" : baseNode.getType();
      Packet cmd = tool.makeCmd("addOpsetAndOp", sOpsetName, sDescr, sInfo,
        (sOp == null || sOp.length() == 0)? "" : sOp,baseNodeId,baseNodeType);
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

    // If the add operation is successful, the result contains <name>:<id> of
    // the opset.
    String sLine = res.getStringValue(0);
    String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
    // If the oset is new, add it to the opset list and vector.
    if (!opsetListModel.contains(pieces[0])) {
      int index = PmAdmin.getIndex(opsetListModel, pieces[0]);
      opsetListModel.add(index, pieces[0]);
      opsetList.ensureIndexIsVisible(index);
      opsetVector.add(index, pieces[1]);
    }
    // Update the graph image - has effect onnly if the graph type is CAPS.
    tool.resetTree(null);
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
      JOptionPane.showMessageDialog(tool, "No such opset to display!");
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
        JOptionPane.showMessageDialog(tool, "Exception in getObjClassOps(), " + e.getMessage());
        return;
      }
      opListModel.clear();
      for (int i = 0; i < res.size(); i++) {
        String sLine = res.getStringValue(i);
        int index = PmAdmin.getIndex(opListModel, sLine);
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
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      close();
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
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getOpsetInfo", sOpsetId);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(tool, "Undetermined error");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(tool, res.getErrorMessage());
        return;
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      throw new RuntimeException(exc.getMessage());
    }
    
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
/*
 * DenyEditor.java
 *
 * Created on November 2, 2005, 9:24 AM
 */

package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
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
public class DenyEditor extends JDialog implements ActionListener, ListSelectionListener {
  
  public static final String PM_DENY_USER_ID         = "user id";
  public static final String PM_DENY_SESSION         = "session";
  public static final String PM_DENY_PROCESS         = "process";
  public static final String PM_DENY_INTRA_SESSION   = "intra session";
  public static final String PM_DENY_ACROSS_SESSIONS = "across sessions";

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
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();
  
  /**
 * @uml.property  name="nameField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField nameField;
  /**
 * @uml.property  name="attrUserLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel attrUserLabel;
  /**
 * @uml.property  name="attrUserField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField attrUserField;
  /**
 * @uml.property  name="sessionButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JRadioButton sessionButton;
  /**
 * @uml.property  name="processButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JRadioButton processButton;
  /**
 * @uml.property  name="userButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JRadioButton userButton;
  /**
 * @uml.property  name="attrButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JRadioButton attrButton;
  /**
 * @uml.property  name="interButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JRadioButton interButton;

  /**
 * @uml.property  name="attrUser2Label"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel attrUser2Label;
  /**
 * @uml.property  name="attrUserListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel attrUserListModel;
  /**
 * @uml.property  name="attrUserList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList attrUserList;
  /**
 * @uml.property  name="attrUserVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector attrUserVector;
  
  /**
 * @uml.property  name="opListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel opListModel;
  /**
 * @uml.property  name="opList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList opList;

  /**
 * @uml.property  name="contListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel contListModel;
  /**
 * @uml.property  name="contList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList contList;
  /**
 * @uml.property  name="contVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector contVector;

  /**
 * @uml.property  name="complementBox"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JCheckBox complementBox;
  /**
 * @uml.property  name="intersectBox"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JCheckBox intersectBox;

  /**
 * @uml.property  name="denyListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel denyListModel;
  /**
 * @uml.property  name="denyList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList denyList;
  /**
 * @uml.property  name="denyVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector denyVector;
  
  /**
 * @uml.property  name="op2ListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel op2ListModel;
  /**
 * @uml.property  name="op2List"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList op2List;
  
  /**
 * @uml.property  name="cont2ListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel cont2ListModel;
  /**
 * @uml.property  name="cont2List"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList cont2List;
  /**
 * @uml.property  name="cont2Vector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector cont2Vector;
  
  public DenyEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);

    this.tool = tool;
    this.sslClient = sslClient;
    setTitle("Deny Constraints");
    this.setResizable(false);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        doClose();
      }
    });

    // Start building the GUI
    JLabel nameLabel = new JLabel("Constraint name:");
    nameField = new JTextField(22);
    JPanel namePane = new JPanel(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(namePane, nameLabel, 0, 0, 1, 1);
    addComp(namePane, nameField, 0, 1, 3, 1);
    namePane.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

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
    attrUserListScrollPane.setPreferredSize(new Dimension(240,230));

    JPanel upperRightPane = new JPanel();
    upperRightPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(upperRightPane, attrUser2Label, 0, 0, 1, 1);
    addComp(upperRightPane, attrUserListScrollPane, 0, 1, 3, 1);
    upperRightPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
    
    JPanel upperPane = new JPanel();
    upperPane.add(upperLeftPane);
    upperPane.add(upperRightPane);
    
    // Middle pane will contain operations and containers to choose from.
    JLabel opLabel = new JLabel("Operations to add:");
    opListModel = new DefaultListModel();
    opList = new JList(opListModel);
    JScrollPane opListScrollPane = new JScrollPane(opList);
    opListScrollPane.setPreferredSize(new Dimension(240,175));

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
    contListScrollPane.setPreferredSize(new Dimension(240,140));
    complementBox = new JCheckBox("!Container's complement");
    
//    intersectBox = new JCheckBox("Intersection");
    
    JPanel contPane = new JPanel();
    contPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(contPane, contLabel, 0, 0, 1, 1);
    addComp(contPane, contListScrollPane, 0, 1, 3, 1);
    constraints.insets = new Insets(10, 0, 0, 0);
    addComp(contPane, complementBox, 0, 2, 1, 1);
//    addComp(contPane, intersectBox, 2, 2, 1, 1);
    contPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));

    JPanel middlePane = new JPanel();
    middlePane.add(opPane);
    middlePane.add(contPane);
    
    // The lower pane will contain existing denies with their operations and containers.
    JLabel conLabel = new JLabel("Deny constraints:");
    denyListModel = new DefaultListModel();
    denyList = new JList(denyListModel);
    denyList.addListSelectionListener(this);
    JScrollPane conListScrollPane = new JScrollPane(denyList);
    conListScrollPane.setPreferredSize(new Dimension(155,160));

    JPanel conPane = new JPanel();
    conPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(conPane, conLabel, 0, 0, 1, 1);
    addComp(conPane, conListScrollPane, 0, 1, 3, 1);
    conPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    JLabel op2Label = new JLabel("Operations:");
    op2ListModel = new DefaultListModel();
    op2List = new JList(op2ListModel);
    JScrollPane op2ListScrollPane = new JScrollPane(op2List);
    op2ListScrollPane.setPreferredSize(new Dimension(155,160));

    JPanel op2Pane = new JPanel();
    op2Pane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(op2Pane, op2Label, 0, 0, 1, 1);
    addComp(op2Pane, op2ListScrollPane, 0, 1, 3, 1);
    op2Pane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

    
    JLabel cont2Label = new JLabel("Containers:");
    cont2ListModel = new DefaultListModel();
    cont2List = new JList(cont2ListModel);
    JScrollPane cont2ListScrollPane = new JScrollPane(cont2List);
    cont2ListScrollPane.setPreferredSize(new Dimension(155,160));

    JPanel cont2Pane = new JPanel();
    cont2Pane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(cont2Pane, cont2Label, 0, 0, 1, 1);
    addComp(cont2Pane, cont2ListScrollPane, 0, 1, 3, 1);
    cont2Pane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

    JPanel lowerPane = new JPanel();
    lowerPane.setLayout(new BoxLayout(lowerPane, BoxLayout.X_AXIS));
    lowerPane.add(conPane);
    lowerPane.add(op2Pane);
    lowerPane.add(cont2Pane);
    lowerPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(5, 0, 0, 0),
      BorderFactory.createTitledBorder("Existing constraints")));

    JPanel entitiesPane = new JPanel(new BorderLayout());
    entitiesPane.add(upperPane, BorderLayout.NORTH);
    entitiesPane.add(middlePane, BorderLayout.CENTER);
    entitiesPane.add(lowerPane, BorderLayout.SOUTH);
    
    // Buttons.
    JButton addButton = new JButton("Add");
    addButton.addActionListener(this);
    addButton.setActionCommand("add");

    JButton deleteButton = new JButton("Delete");
    deleteButton.addActionListener(this);
    deleteButton.setActionCommand("delete");

    JButton refreshButton = new JButton("Refresh");
    refreshButton.addActionListener(this);
    refreshButton.setActionCommand("refresh");

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(this);
    closeButton.setActionCommand("close");

    JPanel buttonPane = new JPanel();
    buttonPane.add(addButton);
    buttonPane.add(deleteButton);
    buttonPane.add(refreshButton);
    buttonPane.add(closeButton);
    buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
    
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(entitiesPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    setContentPane(contentPane);
  }
  
  private void addComp(Container container, Component component, int x, int y, int w, int h) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = w;
    constraints.gridheight = h;
    container.add(component, constraints);
  }

  public void prepare() {
    denyListModel.clear();
    if (denyVector == null) denyVector = new Vector();
    else denyVector.clear();
    
    Packet res = (Packet)getDenies();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(denyListModel, pieces[0]);
      denyListModel.add(index, pieces[0]);
      denyVector.add(index, pieces[1]);
    }
    op2ListModel.clear();
    cont2ListModel.clear();
    complementBox.setSelected(false);
    intersectBox.setSelected(false);

    attrUserListModel.clear();
    if (attrUserVector == null) attrUserVector = new Vector();
    else attrUserVector.clear();

    userButton.setSelected(true);
    res = (Packet)getUsers();    
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(attrUserListModel, pieces[0]);
      attrUserListModel.add(index, pieces[0]);
      attrUserVector.add(index, pieces[1]);
    }
    
    
    opListModel.clear();
    res = (Packet)getAllOps();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      int index = PmAdmin.getIndex(opListModel, sLine);
      opListModel.add(index, sLine);
    }

    contListModel.clear();
    if (contVector == null) contVector = new Vector();
    else contVector.clear();
    res = (Packet)getOattrs();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(contListModel, pieces[0]);
      contListModel.add(index, pieces[0]);
      contVector.add(index, pieces[1]);
    }
  }
  
  private Object getOattrs() {
    Packet res;
    try {
      Packet cmd = tool.makeCmd("getOattrs");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        return null;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return null;
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
      return null;
    }
    return res;
  }
  
  private Object getAllOps() {
    Packet res;
    try {
      Packet cmd = tool.makeCmd("getAllOps");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        return null;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return null;
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
      return null;
    }
    return res;
  }

  private Object getDenies() {
    Packet res;

    try {
      Packet cmd = tool.makeCmd("getDenies");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        return null;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return null;
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
      return null;
    }
    return res;
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
    
    attrUserList.clearSelection();
    opList.clearSelection();
    contList.clearSelection();
    
    Packet res;
    try {
      Packet cmd = tool.makeCmd("addDeny", sDenyName, sDenyType,
        (sUserOrAttrName == null)? "" : sUserOrAttrName,
        (sUserOrAttrId == null)? "" : sUserOrAttrId,
        (sOp == null)? "" : sOp,
        (sOattrName == null)? "" : sOattrName,
        (sOattrId == null)? "" : sOattrId,
        intersectBox.isSelected()? "yes" : "no");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null answer from the engine!");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
      return;
    }
    // If the add operation is successful, the result contains <name>:<id> of
    // the deny constraint.
    String sNew = res.getStringValue(0);
    String[] pieces = sNew.split(PmAdmin.PM_FIELD_DELIM);
    if (!denyListModel.contains(pieces[0])) {
      int index = PmAdmin.getIndex(denyListModel, pieces[0]);
      denyListModel.add(index, pieces[0]);
      denyList.ensureIndexIsVisible(index);
      denyVector.add(index, pieces[1]);
    }
    selectDeny(pieces[1]);
  }
  
  private void selectDeny(String sDenyId) {
    int index = denyVector.indexOf(sDenyId);
    if (index < 0) {
      JOptionPane.showMessageDialog(tool, "No such deny constraint to display!");
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
    
    Packet res;
    try {
      Packet cmd = tool.makeCmd("deleteDeny", sDenyName,
        (sOp == null)? "" : sOp,
        (sOattrName == null)? "" : sOattrName,
        (sOattrId == null)? "" : sOattrId);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null answer from the engine!");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
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
      if (cont2Vector == null) cont2Vector = new Vector();
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
    if (attrUserVector == null) attrUserVector = new Vector();
    else attrUserVector.clear();
    
    Packet res = (Packet)getSessions();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(attrUserListModel, pieces[0]);
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
    if (attrUserVector == null) attrUserVector = new Vector();
    else attrUserVector.clear();
  }

  private void idButtonWasSelected() {
    attrUserLabel.setText("User:                      ");
    attrUser2Label.setText("Choose user:            ");

    nameField.setText("");
    attrUserField.setText("");
    attrUserField.setEditable(false);
    
    attrUserListModel.clear();
    if (attrUserVector == null) attrUserVector = new Vector();
    else attrUserVector.clear();
    
    Packet res = (Packet)getUsers();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(attrUserListModel, pieces[0]);
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
    if (attrUserVector == null) attrUserVector = new Vector();
    else attrUserVector.clear();

    Packet res = (Packet)getUserAttributes();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(attrUserListModel, pieces[0]);
      attrUserListModel.add(index, pieces[0]);
      attrUserVector.add(index, pieces[1]);
    }
  }

  private void interButtonWasSelected() {
    attrUserLabel.setText("User attribute:        ");
    attrUser2Label.setText("Choose user attribute:");

    nameField.setText("");
    attrUserField.setText("");
    attrUserField.setEditable(false);

    attrUserListModel.clear();
    if (attrUserVector == null) attrUserVector = new Vector();
    else attrUserVector.clear();

    Packet res = (Packet)getUserAttributes();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(attrUserListModel, pieces[0]);
      attrUserListModel.add(index, pieces[0]);
      attrUserVector.add(index, pieces[1]);
    }
  }

  private Object getSessions() {
    Packet res;
    try {
      Packet cmd = tool.makeCmd("getSessions");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(tool, "Undetermined error");
        return null;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(tool, res.getErrorMessage());
        return null;
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception: " + exc.getMessage());
      return null;
    }
    return res;
  }

  private Object getUsers() {
    Packet res;
    try {
      Packet cmd = tool.makeCmd("getUsers");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(tool, "Undetermined error");
        return null;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(tool, res.getErrorMessage());
        return null;
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception: " + exc.getMessage());
      return null;
    }
    return res;
  }
  
  private Object getUserAttributes() {
    Packet res;
    try {
      Packet cmd = tool.makeCmd("getUserAttributes");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(tool, "Undetermined error");
        return null;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(tool, res.getErrorMessage());
        return null;
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception: " + exc.getMessage());
      return null;
    }
    return res;
  }
  
  // The information returned by getDenyInfo has the following format:
  // item 0: <deny name>:<deny id>
  // item 1: <deny class>:<user or attribute name>:<user or attribute id>:<is intersection>
  // item 2: <operation count, opcount>
  // items 3 through 3 + opcount - 1: <operation>
  // item 3 + opcount: <container count, contcount>
  // item 3 + opcount + 1 through 3 + opcount + 1 + contcount - 1: <container name>:<container id>
  private Object getDenyInfo(String sDenyId) {
    Packet res;
    try {
      Packet cmd = tool.makeCmd("getDenyInfo", sDenyId);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(tool, "Undetermined error");
        return null;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(tool, res.getErrorMessage());
        return null;
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception: " + exc.getMessage());
      return null;
    }
    return res;
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("add")) {
      doAdd();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      doClose();
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
    Packet res = (Packet)getDenyInfo(sDenyId);
    if (res == null) return;
    
    // The information returned by getDenyInfo has the following format:
    // item 0: <deny name>:<deny id>
    // item 1: <deny type>:<user or attribute name>:<user or attribute id>:<is intersection>
    // item 2: <operation count, opcount>
    // items 3 through 3 + opcount - 1: <operation>
    // item 3 + opcount: <container count, contcount>
    // item 3 + opcount + 1 through 3 + opcount + 1 + contcount - 1: <container name>:<container id>
    sLine = res.getStringValue(1);
    pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
    String sDenyType = pieces[0];
    String sUserOrAttrName = pieces[1];
    String sUserOrAttrId = pieces[2];
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
      int index = PmAdmin.getIndex(op2ListModel, sLine);
      op2ListModel.add(index, sLine);
    }
    
    cont2ListModel.clear();
    if (cont2Vector == null) cont2Vector = new Vector();
    else cont2Vector.clear();
    
    int contCount = Integer.valueOf(res.getStringValue(3 + opCount)).intValue();
    for (int i = 4 + opCount; i < 4 + opCount + contCount; i++) {
      sLine = res.getStringValue(i);
      pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(cont2ListModel, pieces[0]);
      cont2ListModel.add(index, pieces[0]);
      cont2Vector.add(index, pieces[1]);
    }
  }
}

/*
 * ObjEditor.java
 *
 * Author: Serban I. Gavrila
 *
 * Created on April 19, 2005, 10:56 AM
 * Updated on October 06, 2005.
 */

package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
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
public class ObjEditor extends JDialog implements ActionListener {

  // Classes of actual objects
  public static final String PM_CLASS_CLASS_NAME   = "class";
  public static final String PM_CLASS_CLASS_ID   = "2";

  public static final String PM_CLASS_FILE_NAME   = "File";
  public static final String PM_CLASS_FILE_ID   = "3";
  
  public static final String PM_CLASS_DIR_NAME    = "Directory";
  public static final String PM_CLASS_DIR_ID   = "4";
  
  public static final String PM_CLASS_SGRAPH_NAME  = "Subgraph";
  public static final String PM_CLASS_SGRAPH_ID   = "5";

  public static final String PM_CLASS_USER_NAME    = "User";
  public static final String PM_CLASS_USER_ID      = "6";

  public static final String PM_CLASS_UATTR_NAME   = "User attribute";
  public static final String PM_CLASS_UATTR_ID     = "7";
  
  public static final String PM_CLASS_OBJ_NAME     = "Object";
  public static final String PM_CLASS_OBJ_ID       = "8";

  public static final String PM_CLASS_OATTR_NAME   = "Object attribute";
  public static final String PM_CLASS_OATTR_ID     = "9";

  public static final String PM_CLASS_CONN_NAME    = "Connector";
  public static final String PM_CLASS_CONN_ID      = "10";
  
  public static final String PM_CLASS_POL_NAME     = "Policy class";
  public static final String PM_CLASS_POL_ID       = "11";
  
  public static final String PM_CLASS_OPSET_NAME   = "Operation set";
  public static final String PM_CLASS_OPSET_ID     = "12";

  /**
 * @uml.property  name="tool"
 * @uml.associationEnd  
 */
private PmAdmin tool;
  /**
 * @uml.property  name="hostEditor"
 * @uml.associationEnd  
 */
private HostEditor hostEditor;
  /**
 * @uml.property  name="sslClient"
 * @uml.associationEnd  
 */
private SSLSocketClient sslClient;

  /**
 * @uml.property  name="addButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton addButton;
  /**
 * @uml.property  name="mapButton"
 * @uml.associationEnd  readOnly="true"
 */
private JButton mapButton;
  /**
 * @uml.property  name="resetButton"
 * @uml.associationEnd  readOnly="true"
 */
private JButton resetButton;
  /**
 * @uml.property  name="permButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton permButton;
  /**
 * @uml.property  name="deleteButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton deleteButton;
  /**
 * @uml.property  name="deleteStrongButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton deleteStrongButton;
  /**
 * @uml.property  name="closeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton closeButton;

  /**
 * @uml.property  name="nameField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField nameField;
  /**
 * @uml.property  name="descrField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField descrField;
  /**
 * @uml.property  name="infoField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField infoField;
  /**
 * @uml.property  name="classLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel classLabel;
  /**
 * @uml.property  name="classCombo"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JComboBox classCombo;
  /**
 * @uml.property  name="hostLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel hostLabel;
  /**
 * @uml.property  name="hostCombo"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JComboBox hostCombo;
  /**
 * @uml.property  name="hostVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector hostVector;
  /**
 * @uml.property  name="hostsAdded"
 */
private boolean hostsAdded;
  
  /**
 * @uml.property  name="pathLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel pathLabel;
  /**
 * @uml.property  name="pathField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField pathField;

  /**
 * @uml.property  name="pmObjLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel pmObjLabel;
  /**
 * @uml.property  name="pmObjListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel pmObjListModel;
  /**
 * @uml.property  name="pmObjList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList pmObjList;
  /**
 * @uml.property  name="pmObjVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector pmObjVector;
  /**
 * @uml.property  name="inhBox"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JCheckBox inhBox;
  

  /**
 * @uml.property  name="objModel"
 * @uml.associationEnd  
 */
private ObjTableModel objModel;
  /**
 * @uml.property  name="objTable"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTable objTable;
  
  // That's the base node, i.e., the node that the object attribute associated
  // with this object will be assigned to. It can be an object attribute
  // but not associated to an object, a policy class, or the connector node.
  /**
 * @uml.property  name="baseNode"
 * @uml.associationEnd  
 */
private PmNode baseNode = null;
  /**
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();

  public ObjEditor(PmAdmin tool, SSLSocketClient sslClient, HostEditor hostEditor) {
    super(tool, true);  // modal

    this.tool = tool;
    this.hostEditor = hostEditor;
    this.sslClient = sslClient;

    setTitle("Objects");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        closeEditor();
      }
    });

    // Start building the GUI
    // The left upper pane contains the object fields.
    JPanel upperLeftPane = new JPanel();
    upperLeftPane.setLayout(new GridBagLayout());

    JLabel nameLabel = new JLabel("Name:");
    nameField = new JTextField(27);
    
    JLabel descrLabel = new JLabel("Description:");
    descrField = new JTextField(27);
    
    JLabel infoLabel = new JLabel("Other Info:");
    infoField = new JTextField(27);

    classLabel = new JLabel("Class:");
    classCombo = new JComboBox();
    classCombo.addActionListener(this);
    classCombo.setActionCommand("class");
    classCombo.setPreferredSize(new Dimension(295,25));

    hostLabel = new JLabel("Host:");
    hostCombo = new JComboBox();
    hostCombo.setPreferredSize(new Dimension(295,25));
    hostCombo.addActionListener(this);
    hostCombo.setActionCommand("Host");

    pathLabel = new JLabel("Path:");
    pathField = new JTextField(27);
    
    constraints.insets = new Insets(0, 0, 5, 0);

    addComp(upperLeftPane, nameLabel, 0, 0, 1, 1);
    addComp(upperLeftPane, nameField, 1, 0, 4, 1);

    addComp(upperLeftPane, descrLabel, 0, 1, 1, 1);
    addComp(upperLeftPane, descrField, 1, 1, 4, 1);

    addComp(upperLeftPane, infoLabel, 0, 2, 1, 1);
    addComp(upperLeftPane, infoField, 1, 2, 4, 1);
    
    addComp(upperLeftPane, classLabel, 0, 3, 1, 1);
    addComp(upperLeftPane, classCombo, 1, 3, 4, 1);

    addComp(upperLeftPane, hostLabel, 0, 4, 1, 1);
    addComp(upperLeftPane, hostCombo, 1, 4, 4, 1);

    addComp(upperLeftPane, pathLabel, 0, 5, 1, 1);
    addComp(upperLeftPane, pathField, 1, 5, 4, 1);

    // The right upper pane contains the PM entities list and a check box.
    JPanel upperRightPane = new JPanel();
    upperRightPane.setLayout(new GridBagLayout());
    
    pmObjLabel = new JLabel("PM Entities:            ");
//    pmObjLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
    pmObjListModel = new DefaultListModel();
    pmObjList = new JList(pmObjListModel);
    JScrollPane pmObjListScrollPane = new JScrollPane(pmObjList);
    pmObjListScrollPane.setPreferredSize(new Dimension(350, 120));

    inhBox = new JCheckBox("Include Ascendants");

    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(upperRightPane, pmObjLabel, 0, 0, 1, 1);
    constraints.insets = new Insets(0, 20, 5, 0);
    addComp(upperRightPane, pmObjListScrollPane, 0, 1, 4, 4);
    addComp(upperRightPane, inhBox, 0, 5, 1, 1);

    JPanel objPane = new JPanel();
    objPane.setLayout(new BorderLayout());
    objPane.add(upperLeftPane, BorderLayout.WEST);
    objPane.add(upperRightPane, BorderLayout.EAST);

    JPanel upperPane = new JPanel();
    upperPane.setLayout(new BorderLayout());
    upperPane.add(objPane, BorderLayout.CENTER);
    upperPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 10, 0),
      BorderFactory.createTitledBorder("New Object")));

    // The lists pane
    JPanel middlePane = new JPanel();
    middlePane.setLayout(new GridBagLayout());

    JLabel objectsLabel = new JLabel("Object:");
    JLabel instancesLabel = new JLabel("Properties:");

    objModel = new ObjTableModel();
    objTable = new JTable(objModel);
    objModel.addTableModelListener(objTable);
    JScrollPane objTableScrollPane = new JScrollPane(objTable);
    objTableScrollPane.setPreferredSize(new Dimension(700,150));

    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(middlePane, objectsLabel, 0, 0, 1, 1);
    addComp(middlePane, objTableScrollPane, 0, 1, 2, 3);

    middlePane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(0, 0, 10, 0),
      BorderFactory.createTitledBorder("Existing Objects")));
    
    addButton = new JButton("Add");
    addButton.setActionCommand("add");
    addButton.addActionListener(this);

    deleteButton = new JButton("Delete");
    deleteButton.setActionCommand("delete");
    deleteButton.addActionListener(this);
    
    deleteStrongButton = new JButton("Delete strong");
    deleteStrongButton.setActionCommand("delete strong");
    deleteStrongButton.addActionListener(this);
    
    permButton = new JButton("Permissions...");
    permButton.setActionCommand("permissions");
    permButton.addActionListener(this);
    
    closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);

    JPanel lowerPane = new JPanel();
    lowerPane.add(addButton);
    lowerPane.add(deleteButton);
    lowerPane.add(deleteStrongButton);
    lowerPane.add(permButton);
    lowerPane.add(closeButton);
    lowerPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

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

  // getObjects() returns the list of all virtual objects, one object
  // per item. An item contains the header "data" and the following information:
  // <obj name>|<obj id>|<class>|<inh>|<host or orig name>|<path or orig id>.
  private Object getObjects() throws Exception {
    Packet res;
    try {
      Packet cmd = tool.makeCmd("getObjects");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        throw new RuntimeException("Undetermined error");
      }
      if (res.hasError()) {
        throw new RuntimeException(res.getErrorMessage());
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
    return res;
  }

  // Called right before making the object editor visible.
  // The parameters sId, sType are the id and type of the
  // PM graph node to which the new object will be assigned,
  // assuming that the editor was invoked from the popup menu
  // "Add object..." for that node.
  // When the editor is invoked from the main menu, these
  // parameters are null, and the engine assumes that the new
  // object attribute (and object) will be assigned directly
  // to the connector.
  public void prepareAndSetBaseNode(PmNode node) {
    baseNode = node;

    nameField.setText("");
    nameField.requestFocus();
    descrField.setText("");
    infoField.setText("");
    pathField.setText("");
  
    // The predefined classes of actual objects:
    
    Packet res;
    try {
      res = (Packet)getObjClasses();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception in getObjClasses(), " + e.getMessage());
      return;
    }
    classCombo.setActionCommand("hohoho");
    classCombo.removeAllItems();
    classCombo.setActionCommand("class");
    Vector vClasses = new Vector();
    for (int i = 0; i < res.size(); i++) {
      String sClass = res.getStringValue(i);
      int index = PmAdmin.getIndex(vClasses, sClass);
      vClasses.add(index, sClass);
    }

    for (int i = 0; i < vClasses.size(); i++) {
      classCombo.addItem(vClasses.get(i));
    }

    // The hosts
    try {
      res = (Packet)hostEditor.getHosts();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception in getHosts(), " + e.getMessage());
      return;
    }
    if (res.size() == 0) {
      JOptionPane.showMessageDialog(this, "You need to define PM hosts first");
      this.setVisible(false);
      return;
    }
    hostCombo.removeAllItems();
    if (hostVector == null) hostVector = new Vector();
    else hostVector.clear();
    
    // Each item contains a host <host name>:<host id>.
    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      hostCombo.addItem(pieces[0]);
      hostVector.add(pieces[1]);
    }
    hostsAdded = true;

    // Get the PM entities and fill their list.
    pmObjListModel.clear();
    if (pmObjVector == null) pmObjVector = new Vector();
    else pmObjVector.clear();
    
    // The objects
    try {
      res = (Packet)getObjects();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception while getting virtual objects: " + e.getMessage());
      return;
    }
    objModel.clear();
    System.out.println("Result size = " + res.size());
    
    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      System.out.println("item = <" + sLine + ">");
      //JOptionPane.showMessageDialog(tool, "item = <" + sLine + ">");
      objModel.addObject(sLine);
    }
  }

  private void closeEditor() {
    hostsAdded = false;
    this.setVisible(false);
  }

  // Add a new object.
  private void addObject() {
    String sName = null;
    String sDescr = null;
    String sInfo = null;
    String sClass = null;
    String sHost = null;
    String sPath = null;
    String sOrigName = null;
    String sOrigId = null;
    String sInh = null;
    
    // We need a few checks.
    sName = nameField.getText().trim();
    if (sName.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter an object name!");
      nameField.requestFocus();
      return;
    }
    sDescr = descrField.getText().trim();
    if (sDescr.length() == 0) sDescr = sName;
    sInfo = infoField.getText().trim();
    if (sInfo.length() == 0) sInfo = sName;

    sClass = (String)classCombo.getSelectedItem();
    if (sClass == null) {
      JOptionPane.showMessageDialog(this, "Please select an object class!");
      nameField.requestFocus();
      return;
    }      
    if (sClass.equals(PM_CLASS_FILE_NAME) || sClass.equals(PM_CLASS_DIR_NAME)) {
      sHost =  (String)hostCombo.getSelectedItem();
      sPath = pathField.getText().trim();
      if (sPath == null || sPath.length() == 0) {
        JOptionPane.showMessageDialog(this, "Please enter the object path!");
        pathField.requestFocus();
        return;
      }
      sOrigName = null;
      sOrigId = null;
      sInh = "no";
    } else if (sClass.equals(PM_CLASS_USER_NAME) ||
               sClass.equals(PM_CLASS_UATTR_NAME) ||
               sClass.equals(PM_CLASS_OBJ_NAME) ||
               sClass.equals(PM_CLASS_OATTR_NAME) ||
               sClass.equals(PM_CLASS_POL_NAME) ||
               sClass.equals(PM_CLASS_CONN_NAME) ||
               sClass.equals(PM_CLASS_OPSET_NAME)) {
      sHost = null;
      sPath = null;
      
      int index = pmObjList.getSelectedIndex();
      if (index < 0) {
        JOptionPane.showMessageDialog(this, "Please select a PM entity from the list!");
        return;
      }
      sOrigName = (String)pmObjListModel.get(index);
      sOrigId = (String)pmObjVector.get(index);
      boolean bInh = inhBox.isSelected();
      sInh = (bInh? "yes" : "no");
    }

    Packet res;
    try {
      // The null argument in makeCmd() is the process id.
      Packet cmd = tool.makeCmd("addObject3", null, sName, sDescr, sInfo,
        (baseNode == null)? "" : baseNode.getType() + PmAdmin.PM_ALT_FIELD_DELIM + baseNode.getName(),
        sClass, "", sHost, sPath, sOrigName, sOrigId, sInh);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result returned trying to add object");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error trying to add object: " + res.getErrorMessage());
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Error trying to add object: " + e.getMessage());
      return;
    }
    
    // Object added sucessfully. The result contains the <name>:<id>
    // of the newly added object. Compose the string expected by the
    // object table model, which has the format:
    // name|id|class|inh|host or orig name|path or orig id.
    String sHostOrName, sPathOrId;
    if (sHost != null) sHostOrName = sHost;
    else if (sOrigName != null) sHostOrName = sOrigName;
    else sHostOrName = "";
    if (sPath != null) sPathOrId = sPath;
    else if (sOrigId != null) sPathOrId = sOrigId;
    else sPathOrId = "";
    String sNew = res.getStringValue(0);
    String[] pieces = sNew.split(PmAdmin.PM_FIELD_DELIM);
    String sObj = pieces[0] + PmAdmin.PM_ALT_FIELD_DELIM + pieces[1] +
      PmAdmin.PM_ALT_FIELD_DELIM + sClass + PmAdmin.PM_ALT_FIELD_DELIM +
      sInh + PmAdmin.PM_ALT_FIELD_DELIM + sHostOrName +
      PmAdmin.PM_ALT_FIELD_DELIM + sPathOrId;
    objModel.addObject(sObj);
    
    nameField.setText("");
    descrField.setText("");
    infoField.setText("");
    
    baseNode.invalidate();
  }
  
  private void resetInterface() {
  }

  private void setPermissions() {
  }

  private void deleteObject() {
    int index = objTable.getSelectedRow();
    if (index < 0) {
      JOptionPane.showMessageDialog(this, "Please select an object in the table!");
      return;
    }
    String sObj = (String)objModel.get(index);
    String[] pieces = sObj.split(PmAdmin.PM_ALT_DELIM_PATTERN);
    System.out.println("Object to delete is:" + sObj + " and has " + pieces.length + " pieces.");

    Packet res;
    try {
      Packet cmd = tool.makeCmd("deleteObject", pieces[1]);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result returned trying to delete object");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error trying to delete object: " + res.getErrorMessage());
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Error trying to delete object: " + e.getMessage());
      return;
    }
    objModel.delObject(index);
    baseNode.invalidate();
  }

  private void deleteObjectStrong() {
    int index = objTable.getSelectedRow();
    if (index < 0) {
      JOptionPane.showMessageDialog(this, "Please select an object in the table!");
      return;
    }
    String sObj = (String)objModel.get(index);
    String[] pieces = sObj.split(PmAdmin.PM_ALT_DELIM_PATTERN);
    System.out.println("Object to delete is:" + sObj + " and has " + pieces.length + " pieces.");

    Packet res;
    try {
      Packet cmd = tool.makeCmd("deleteObjectStrong", pieces[1]);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result returned trying to delete object");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error trying to delete object: " + res.getErrorMessage());
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Error trying to delete object: " + e.getMessage());
      return;
    }
    objModel.delObject(index);
    baseNode.invalidate();
  }

  public void hostWasSelected(ActionEvent event) {
    if (!hostsAdded) return;
    int hostIndex = hostCombo.getSelectedIndex();
    
    String sHostId = (String)hostVector.get(hostIndex);
    String sHost = (String)hostCombo.getItemAt(hostIndex);
    
    // Get the repository path for the selected host and insert it into the
    // path field.
    Packet res = (Packet)hostEditor.getHostInfo(sHostId);
    if (res == null) {
      JOptionPane.showMessageDialog(this, "Null result returned trying to get host info for " + sHost);
      return;
    }
    if (res.hasError()) {
      JOptionPane.showMessageDialog(this, res.getErrorMessage());
      return;
    }
    
    // The information returned by getHostInfo has the following format:
    // item 0: <host name>:<host id>:<is pdc> (e.g., musial:1823A...:true).
    // item 1: <host repo>
    String sRepo = res.getStringValue(1);
    pathField.setText(sRepo);
  }
  
  public void classWasSelected(ActionEvent event) {
    String sClass = (String)classCombo.getSelectedItem();
    
    // The selected class could be null.
    if (sClass == null) {
      JOptionPane.showMessageDialog(this, "Please select an object class!");
      return;
    }
    System.out.println(">>>>>>>>>>>>>>>>>>>You selected class " + sClass);
    
    // Get the PM objects having the selected class.
    Packet res;
    try {
      res = (Packet)getPmEntitiesOfClass(sClass);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception in getPmEntitiesOfClass(), " + e.getMessage());
      return;
    }

    pmObjListModel.clear();
    if (pmObjVector == null) pmObjVector = new Vector();
    else pmObjVector.clear();

    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(pmObjListModel, pieces[0]);
      pmObjListModel.add(index, pieces[0]);
      pmObjVector.add(index, pieces[1]);
    }
    
    // If the object class is file or directory, enable host and path fields,
    // and disable the list of pm objects. Reverse for the other classes.
    inhBox.setSelected(false);
    if (sClass.equals(PM_CLASS_FILE_NAME) || sClass.equals(PM_CLASS_DIR_NAME)) {
      hostLabel.setEnabled(true);
      hostCombo.setEnabled(true);
      
      hostCombo.setSelectedIndex(hostCombo.getSelectedIndex());
      
      pathLabel.setEnabled(true);
      pathField.setEnabled(true);
      pmObjLabel.setEnabled(false);
      pmObjList.setEnabled(false);
      inhBox.setEnabled(false);
    } else if (sClass.equals(PM_CLASS_CLASS_NAME)) {
      hostLabel.setEnabled(false);
      hostCombo.setEnabled(false);
      pathLabel.setEnabled(false);
      pathField.setEnabled(false);
      pmObjLabel.setEnabled(false);
      pmObjList.setEnabled(false);
      inhBox.setEnabled(false);
    } else {
      hostLabel.setEnabled(false);
      hostCombo.setEnabled(false);
      pathLabel.setEnabled(false);
      pathField.setEnabled(false);
      pmObjLabel.setEnabled(true);
      pmObjList.setEnabled(true);
      inhBox.setEnabled(true);
    } 
  }

  private Object getObjClasses() throws Exception {
    Packet res;
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
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
    return res;
  }

  private Object getPmEntitiesOfClass(String sClass) throws Exception {
    Packet res;
    try {
      Packet cmd = tool.makeCmd("getPmEntitiesOfClass", sClass);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        throw new RuntimeException("Undetermined error");
      }
      if (res.hasError()) {
        throw new RuntimeException(res.getErrorMessage());
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
    return res;
  }
 
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("add")) {
      addObject();
    } else if (e.getActionCommand().equalsIgnoreCase("reset")) {
      resetInterface();
    } else if (e.getActionCommand().equalsIgnoreCase("permissions")) {
      setPermissions();
    } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
      deleteObject();
    } else if (e.getActionCommand().equalsIgnoreCase("delete strong")) {
      deleteObjectStrong();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      closeEditor();
    } else if (e.getActionCommand().equalsIgnoreCase("class")) {
      classWasSelected(e);
    } else if (e.getActionCommand().equalsIgnoreCase("Host")) {
      hostWasSelected(e);
    }
  }
}

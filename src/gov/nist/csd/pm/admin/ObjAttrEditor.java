/*
 * ObjAttrEditor.java
 *
 * Created on April 14, 2005, 5:07 PM
 */

package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class ObjAttrEditor extends JDialog implements ActionListener {

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
 * @uml.property  name="editButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton editButton;
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
 * @uml.property  name="baseNode"
 * @uml.associationEnd  
 */
PmNode baseNode = null;
  public ObjAttrEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);  // modal

    this.tool = tool;
    this.sslClient = sslClient;

    setTitle("Add Object Attribute");

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
    propListModel = new DefaultListModel();
    propList = new JList(propListModel);
    JScrollPane propListScrollPane = new JScrollPane(propList);
    propListScrollPane.setPreferredSize(new Dimension(255, 200));
    propListScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    
    // The property button pane
    JPanel propButtonPane = new JPanel();

    addButton = new JButton("Add");
    addButton.setActionCommand("add");
    addButton.addActionListener(this);

    editButton = new JButton("Edit");
    editButton.setActionCommand("edit");
    editButton.addActionListener(this);

    removeButton = new JButton("Remove");
    removeButton.setActionCommand("remove");
    removeButton.addActionListener(this);

    propButtonPane.add(addButton);
    propButtonPane.add(editButton);
    propButtonPane.add(removeButton);
    propButtonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

    // The property pane = prop list pane + prop button pane
    JPanel propPane = new JPanel();
    propPane.setLayout(new BorderLayout());
    propPane.add(propListScrollPane, BorderLayout.CENTER);
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
    getRootPane().setDefaultButton(okButton);
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

  public void prepareForAdd(PmNode node) {
    setTitle("Add Object Attribute");
    okButton.setText("Add attribute");
    editingMode = false;

    oaNameField.setText(null);
    oaNameField.requestFocus();
    oaDescrField.setText(null);
    oaInfoField.setText(null);
    propListModel.clear();

    oaNameField.setEditable(true);
    oaDescrField.setEditable(true);
    oaInfoField.setEditable(true);

    baseNode = node;
  }

  public void prepareForEdit(String sId) {
    setTitle("Edit Object attribute");
    okButton.setText("OK");
    editingMode = true;
    oattrToEditId = sId;
    
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getAttrInfo", sId, PmAdmin.PM_NODE_OATTR, "no");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.getMessage());
      return;
    }
    // The result contains:
    // item 0: <name>:<id>
    // item 1: <description>
    // item 2: <other info>
    // item 3: <property>
    // ...
    String s = res.getStringValue(0);
    String[] pieces = s.split(PmAdmin.PM_FIELD_DELIM);
    oaNameField.setText(pieces[0]);
    oaDescrField.setText(res.getStringValue(1));
    oaInfoField.setText(res.getStringValue(2));

    oaNameField.setEditable(false);
    oaDescrField.setEditable(false);
    oaInfoField.setEditable(false);
    
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
      JOptionPane.showMessageDialog(tool, "Please enter an attribute name!");
      return;
    }
    if (sDescr.length() == 0) sDescr = sName;
    if (sInfo.length() == 0) sInfo = sName;

    // Send the command and let the server set the GUID and test for unique name.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd(PMCommand.ADD_OATTR, null, sName, sDescr, sInfo,
        baseNode.getId(), baseNode.getType(), "no");
      // This attribute has no associated object. If there are no properties,
      // the command is ready. If there are properties, add an empty item for
      // for the pointer to the associated object, then the properties.
      int n = propListModel.getSize();
      if (n > 0) {
        cmd.addItem(ItemType.CMD_ARG, "");
        for (int i = 0; i < n; i++) {
          cmd.addItem(ItemType.CMD_ARG, (String)propListModel.get(i));
        }
      }
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
    baseNode.invalidate();
    oaNameField.setText(null);
    oaNameField.requestFocus();
    oaDescrField.setText(null);
    oaInfoField.setText(null);
    propListModel.clear();
  }

  private void doAddProp() {
    JTextField propField = new JTextField();
    int ret = JOptionPane.showOptionDialog(this,
      new Object[] {"Please enter the property:", propField},
      "Add property", JOptionPane.OK_CANCEL_OPTION,
      JOptionPane.QUESTION_MESSAGE, null, null, null);
    if (ret != JOptionPane.OK_OPTION) return;
    String sProp = propField.getText();
    if (sProp.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter a property!");
      return;
    }
    if (editingMode) {
      Packet res = null;
      try {
        Packet cmd = tool.makeCmd("addProp", oattrToEditId, PmAdmin.PM_NODE_OATTR,
          "no", sProp);
        res = sslClient.sendReceive(cmd, null);
        if (res == null) {
          JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
          return;
        }
        if (res.hasError()) {
          JOptionPane.showMessageDialog(this, res.getErrorMessage());
          return;
        }
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.getMessage());
        return;
      }
    }

    propListModel.addElement(sProp);
  }

  private void doEditProp() {
    String sOldProp = (String)propList.getSelectedValue();
    if (sOldProp == null) {
      JOptionPane.showMessageDialog(this, "Please select a property!");
      return;
    }

    JTextField propField = new JTextField(sOldProp);
    int ret = JOptionPane.showOptionDialog(this,
      new Object[] {"Please edit the property:", propField},
      "Edit property", JOptionPane.OK_CANCEL_OPTION,
      JOptionPane.QUESTION_MESSAGE, null, null, null);
    if (ret != JOptionPane.OK_OPTION) return;
    String sNewProp = propField.getText();
    if (sNewProp.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter a property!");
      return;
    }
    
    if (editingMode) {
      Packet res = null;
      try {
        Packet cmd = tool.makeCmd("replaceProp", oattrToEditId, PmAdmin.PM_NODE_OATTR,
          "no", sOldProp, sNewProp);
        res = sslClient.sendReceive(cmd, null);
        if (res == null) {
          JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
          return;
        }
        if (res.hasError()) {
          JOptionPane.showMessageDialog(this, res.getErrorMessage());
          return;
        }
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.getMessage());
        return;
      }
    }
    propListModel.removeElement(sOldProp);
    propListModel.addElement(sNewProp);
  }
  
  private void doRemoveProp() {
    String sProp = (String)propList.getSelectedValue();
    if (sProp == null) {
      JOptionPane.showMessageDialog(this, "Please select a property from the \"Properties\" list!");
      return;
    }

    if (editingMode) {
      Packet res = null;
      try {
        Packet cmd = tool.makeCmd("removeProp", oattrToEditId, PmAdmin.PM_NODE_OATTR,
          "no", sProp);
        res = sslClient.sendReceive(cmd, null);
        if (res == null) {
          JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
          return;
        }
        if (res.hasError()) {
          JOptionPane.showMessageDialog(this, res.getErrorMessage());
          return;
        }
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.getMessage());
        return;
      }
    }
    
    propListModel.removeElement(sProp);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("ok")) {
      doOk();
    } else if (e.getActionCommand().equalsIgnoreCase("add")) {
      doAddProp();
    } else if (e.getActionCommand().equalsIgnoreCase("edit")) {
      doEditProp();
    } else if (e.getActionCommand().equalsIgnoreCase("remove")) {
      doRemoveProp();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      doClose();
    }
  }
}

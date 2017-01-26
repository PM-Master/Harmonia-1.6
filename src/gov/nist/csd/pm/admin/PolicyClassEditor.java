/*
 * PolicyClassEditor.java
 *
 * Created on April 4, 2005, 4:51 PM
 */

package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
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
public class PolicyClassEditor extends JDialog implements ActionListener {

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
 * @uml.property  name="pcNameField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField pcNameField;
  /**
 * @uml.property  name="pcDescrField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField pcDescrField;
  /**
 * @uml.property  name="pcInfoField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField pcInfoField;
  
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
 * @uml.property  name="editingMode"
 */
private boolean editingMode = false;
  /**
 * @uml.property  name="pcToEditId"
 */
private String pcToEditId;

  /**
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();

	public PolicyClassEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);  // modal

    this.tool = tool;
    this.sslClient = sslClient;

    setTitle("Add Policy Class");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

    // Start building the GUI
    JPanel pcPane = new JPanel();
    pcPane.setLayout(new GridBagLayout());

    JLabel pcNameLabel = new JLabel("Name:");
    JLabel pcDescrLabel = new JLabel("Description:");
    JLabel pcInfoLabel = new JLabel("Other Info:");
    
    pcNameField = new JTextField(20);
    pcDescrField = new JTextField(20);
    pcInfoField = new JTextField(20);

    constraints.insets = new Insets(0, 10, 0, 0);

    addComp(pcPane, pcNameLabel, 0, 0, 1, 1);
    addComp(pcPane, pcDescrLabel, 0, 1, 1, 1);
    addComp(pcPane, pcInfoLabel, 0, 2, 1, 1);
    
    addComp(pcPane, pcNameField, 1, 0, 3, 1);
    addComp(pcPane, pcDescrField, 1, 1, 3, 1);
    addComp(pcPane, pcInfoField, 1, 2, 3, 1);
    
    pcPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
    okButton = new JButton("Add policy");
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
    thePane.add(pcPane, BorderLayout.NORTH);
    thePane.add(propPane, BorderLayout.CENTER);
    thePane.add(buttonPane, BorderLayout.SOUTH);

    thePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setContentPane(thePane);
    getRootPane().setDefaultButton(addButton);
  }

  private void addComp(Container container, Component component, int x, int y, int w, int h) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = w;
    constraints.gridheight = h;
    container.add(component, constraints);
  }

  private Object getPolicyClasses() throws Exception {
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getPolicyClasses");
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

  public void prepareForAdd() {
    setTitle("Add Policy Class");    
    editingMode = false;
   	okButton.setText("Add policy");
    pcNameField.setText(null);
    pcNameField.requestFocus();
    pcDescrField.setText(null);
    pcInfoField.setText(null);
    propListModel.clear();

    pcNameField.setEditable(true);
    pcDescrField.setEditable(true);
    pcInfoField.setEditable(true);
  }

  // Prepare the interface for editing the properties of the selected pc.
  // The argument sId is the id of the selected pc.
  public void prepareForEdit(String sId) {
    setTitle("Edit Policy Class");
    editingMode = true;
   	okButton.setVisible(false);
    pcToEditId = sId;
    
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getPcInfo", sId, "no");
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
    pcNameField.setText(pieces[0]);
    pcDescrField.setText(res.getStringValue(1));
    pcInfoField.setText(res.getStringValue(2));
    pcNameField.setEditable(false);
    pcDescrField.setEditable(false);
    pcInfoField.setEditable(false);
    
    propListModel.clear();
    for (int i = 3; i < res.size(); i++) {
      propListModel.addElement(res.getStringValue(i));
    }
  }

  private void close() {
    this.setVisible(false);
  }

  // Add the new policy class.
  private void addPc() {
    if (editingMode) {
      setVisible(false);
      return;
    }

    String sName = pcNameField.getText().trim();
    String sDescr = pcDescrField.getText().trim();
    String sInfo = pcInfoField.getText().trim();

    if (sName.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter a policy class name!");
      return;
    }
    if (sDescr.length() == 0) sDescr = sName;
    if (sInfo.length() == 0) sInfo = sName;

    // Send the command and let the server set the GUID and test for unique name.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("addPc", null, sName, sDescr, sInfo);

      int n = propListModel.getSize();
      for (int i = 0; i < n; i++) {
        cmd.addItem(ItemType.CMD_ARG, (String)propListModel.get(i));
      }
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
    tool.resetTree(null);
    
    pcNameField.setText(null);
    pcNameField.requestFocus();
    pcDescrField.setText(null);
    pcInfoField.setText(null);
    propListModel.clear();
  }

  private void addProp() {
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
      try {
        Packet cmd = tool.makeCmd("addProp", pcToEditId, PmAdmin.PM_NODE_POL, "no", sProp);
        Packet res = sslClient.sendReceive(cmd, null);
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

  private void editProp() {
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
      try {
        Packet cmd = tool.makeCmd("replaceProp", pcToEditId, PmAdmin.PM_NODE_POL,
          "no", sOldProp, sNewProp);
        Packet res = sslClient.sendReceive(cmd, null);
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

  private void removeProp() {
    String sProp = (String)propList.getSelectedValue();
    if (sProp == null) {
      JOptionPane.showMessageDialog(this, "Please select a property from the \"Properties\" list!");
      return;
    }

    if (editingMode) {
      try {
        Packet cmd = tool.makeCmd("removeProp", pcToEditId, PmAdmin.PM_NODE_POL,
          "no", sProp);
        Packet res = sslClient.sendReceive(cmd, null);
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
      addPc();
    } else if (e.getActionCommand().equalsIgnoreCase("edit")) {
      editProp();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      close();
    } else if (e.getActionCommand().equalsIgnoreCase("add")) {
      addProp();
    } else if (e.getActionCommand().equalsIgnoreCase("remove")) {
      removeProp();
    }
  }
}

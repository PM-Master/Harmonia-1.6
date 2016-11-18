/*
 * UserEditor.java
 *
 * Created on February 11, 2008
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

import static gov.nist.csd.pm.common.net.Packet.failurePacket;


/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class PropertyEditor extends JDialog implements ActionListener, ListSelectionListener {

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
 * @uml.property  name="propNameField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField propNameField;
  /**
 * @uml.property  name="propValueField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField propValueField;
  
  /**
 * @uml.property  name="setButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton setButton;
  /**
 * @uml.property  name="deleteButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton deleteButton;
  /**
 * @uml.property  name="closeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton closeButton;

  /**
 * @uml.property  name="propListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel propListModel;
  /**
 * @uml.property  name="propList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList propList;
  /**
 * @uml.property  name="propVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector propVector;

  /**
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();
  
  public PropertyEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, false);  // modal

    this.tool = tool;
    this.sslClient = sslClient;

    setTitle("Properties");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

    // Start building the GUI
    JPanel propPane = new JPanel();
    propPane.setLayout(new GridBagLayout());

    JLabel propNameLabel = new JLabel("Key (Name):");
    JLabel propValueLabel = new JLabel("Value:");
    
    propNameField = new JTextField(20);
    propValueField = new JTextField(20);

    constraints.insets = new Insets(0, 10, 0, 0);

    addComp(propPane, propNameLabel, 0, 0, 1, 1);
    addComp(propPane, propValueLabel, 0, 1, 1, 1);

    addComp(propPane, propNameField, 1, 0, 3, 1);
    addComp(propPane, propValueField, 1, 1, 3, 1);

    propPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 10, 0),
      BorderFactory.createTitledBorder("New Property")));

    // The list
    propListModel = new DefaultListModel();
    propList = new JList(propListModel);
    propList.addListSelectionListener(this);
    JScrollPane propListScrollPane = new JScrollPane(propList);
    propListScrollPane.setPreferredSize(new Dimension(220,200));
    propListScrollPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 10, 0),
      BorderFactory.createTitledBorder("Existing Properties")));

    setButton = new JButton("Set");
    setButton.setActionCommand("set");
    setButton.addActionListener(this);

    deleteButton = new JButton("Delete");
    deleteButton.setActionCommand("delete");
    deleteButton.addActionListener(this);

    closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);

    JPanel buttonPane = new JPanel();
    buttonPane.add(setButton);
    buttonPane.add(deleteButton);
    buttonPane.add(closeButton);

    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(propPane, BorderLayout.NORTH);
    contentPane.add(propListScrollPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    setContentPane(contentPane);
    getRootPane().setDefaultButton(setButton);
  }

  private void addComp(Container container, Component component, int x, int y, int w, int h) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = w;
    constraints.gridheight = h;
    container.add(component, constraints);
  }

  private Object getProperties() {
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getProperties");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) return failurePacket("Null result from getProperties()!");
      return res;
    } catch (Exception e) {
      return failurePacket("Exception in getProperties(): " + e.getMessage());
    }
  }

  // This method must be called before making visible this dialog,
  // to fill the Policy Classes JList.
  public void prepare() {
    Packet res = null;
    
    propNameField.setText(null);
    propNameField.requestFocus();
    propValueField.setText(null);
    
    // Get the properties and populate the list.
    res = (Packet)getProperties();
    if (res.hasError()) {
      JOptionPane.showMessageDialog(this, res.getErrorMessage());
      return;
    }
    
    propListModel.clear();
    if (propVector == null) propVector = new Vector();
    else propVector.clear();
    
    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_PROP_DELIM);
      int index = PmAdmin.getIndex(propListModel, pieces[0]);
      propListModel.add(index, pieces[0]);
      propVector.add(index, pieces[1]);
    }
  }

  private void close() {
    this.setVisible(false);
  }

  // Delete the selected property.
  private void delete() {
    int index = propList.getSelectedIndex();
    if (index < 0) {
      JOptionPane.showMessageDialog(this, "Please select a property!");
      return;
    }
    String sPropName = (String)propListModel.elementAt(index);

    // Send the command "deleteProperty" and let the server test conditions.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("deleteProperty", sPropName);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from deleteProperty()");
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

    propListModel.remove(index);
    propVector.remove(index);
  }

  private void set() {
    String sName = propNameField.getText().trim();
    String sValue = propValueField.getText().trim();

    if (sName.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter or select a property (name)!");
      return;
    }
    if (sValue.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter the property's value!");
      return;
    }

    // Send the command and let the server set the GUID and test for unique name.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("setProperty", sName, sValue);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from setProperty()!");
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

    propList.clearSelection();
    
    // Was this property already in the list and we just changed its value?
    int index = propListModel.indexOf(sName);
    if (index < 0) {
      // This is a new key.
      index = PmAdmin.getIndex(propListModel, sName);
      propListModel.add(index, sName);
      propVector.add(index, sValue);
    } else {
      // An old entry, new value.
      propVector.set(index, sValue);
    }
  }
  
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;

    int index = propList.getSelectedIndex();
    if (index < 0) return;
    
    String sValue = (String)propVector.get(index);
    String sName = (String)propListModel.get(index);
    propNameField.setText(sName);
    propValueField.setText(sValue);
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("set")) {
      set();
    } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
      delete();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      close();
    }
  }
}

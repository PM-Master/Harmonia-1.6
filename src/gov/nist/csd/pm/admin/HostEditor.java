/*
 * HostEditor.java
 *
 * Author: Serban Gavrila
 * KT Consulting, Inc.
 *
 * Created on April 19, 2005, 11:02 AM
 * Updated August 2005.
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
public class HostEditor extends JDialog implements ActionListener, ListSelectionListener {
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
 * @uml.property  name="nameField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField nameField;
  /**
 * @uml.property  name="repoField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField repoField;
  /**
 * @uml.property  name="reservedField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField reservedField;
  /**
 * @uml.property  name="ipaField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField ipaField;
  /**
 * @uml.property  name="descrField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField descrField;

  /**
 * @uml.property  name="pdcBox"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JCheckBox pdcBox;
  //private JCheckBox localBox;

  /**
 * @uml.property  name="hostListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel hostListModel;
  /**
 * @uml.property  name="hostList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList hostList;
  /**
 * @uml.property  name="hostVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector hostVector; // contains host ids.

  /**
 * @uml.property  name="updateButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton updateButton;
  /**
 * @uml.property  name="removeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton removeButton;
  /**
 * @uml.property  name="addButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton addButton;
  /**
 * @uml.property  name="resetButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton resetButton;
  /**
 * @uml.property  name="closeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton closeButton;

  public HostEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);  // modal

    this.tool = tool;
    this.sslClient = sslClient;

    setTitle("PM Hosts");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

    // Left
    JLabel nameLabel = new JLabel("Host Name: ");
    JLabel repoLabel = new JLabel("Repository: ");
//    JLabel reservedLabel = new JLabel("(Reserved) ");

    nameField = new JTextField(20);
    repoField = new JTextField(20);
    reservedField = new JTextField(20);

    nameLabel.setLabelFor(nameField);
    repoLabel.setLabelFor(repoField);
//    reservedLabel.setLabelFor(reservedField);

    // The left panel containing the labels
    JPanel leftLabelPane = new JPanel();
    leftLabelPane.setLayout(new GridLayout(0, 1));
    leftLabelPane.add(nameLabel);
    leftLabelPane.add(repoLabel);
//    leftLabelPane.add(reservedLabel);

    // The left panel containing the fields
    JPanel leftFieldPane = new JPanel();
    leftFieldPane.setLayout(new GridLayout(0, 1));
    leftFieldPane.add(nameField);
    leftFieldPane.add(repoField);
//    leftFieldPane.add(reservedField);

    JPanel upperLeftPane = new JPanel();
    upperLeftPane.setLayout(new BorderLayout());
    upperLeftPane.add(leftLabelPane, BorderLayout.WEST);
    upperLeftPane.add(leftFieldPane, BorderLayout.CENTER);

    // Right
    /*
    JLabel ipaLabel = new JLabel("IP Address: ");
    JLabel descrLabel = new JLabel("Description: ");
    JLabel pdcLabel = new JLabel("Controller: ");

    ipaField = new JTextField(20);
    descrField = new JTextField(20);
    pdcBox = new JCheckBox();

    ipaLabel.setLabelFor(ipaField);
    descrLabel.setLabelFor(descrField);
    pdcLabel.setLabelFor(pdcBox);

    JPanel rightLabelPane = new JPanel();
    rightLabelPane.setLayout(new GridLayout(0, 1));
    rightLabelPane.add(ipaLabel);
    rightLabelPane.add(descrLabel);
    rightLabelPane.add(pdcLabel);

    JPanel rightFieldPane = new JPanel();
    rightFieldPane.setLayout(new GridLayout(0, 1));
    rightFieldPane.add(ipaField);
    rightFieldPane.add(descrField);
    rightFieldPane.add(pdcBox);

    JPanel upperRightPane = new JPanel();
    upperRightPane.setLayout(new BorderLayout());
    upperRightPane.add(rightLabelPane, BorderLayout.WEST);
    upperRightPane.add(rightFieldPane, BorderLayout.CENTER);
    upperRightPane.setBorder(BorderFactory.createEmptyBorder(0,20,0,0));
*/
    JPanel upperPane = new JPanel();
    upperPane.setLayout(new BorderLayout());
    upperPane.add(upperLeftPane, BorderLayout.WEST);
//    upperPane.add(upperRightPane, BorderLayout.EAST);

    // The host list
    hostListModel = new DefaultListModel();
    hostList = new JList(hostListModel);
    hostList.addListSelectionListener(this);
    JScrollPane hostListScrollPane = new JScrollPane(hostList);

    // Put the scrolling list and the button pane in another pane
    hostListScrollPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(20, 0, 0, 0),
      BorderFactory.createTitledBorder("Host List")));
    
    // The buttons
    addButton = new JButton("Add");
    addButton.setToolTipText("Add or replace the host");
    addButton.addActionListener(this);
    addButton.setActionCommand("Add");

    resetButton = new JButton("Reset");
    resetButton.setToolTipText("Reset all fields");
    resetButton.addActionListener(this);
    resetButton.setActionCommand("Reset");

    updateButton = new JButton("Update");
    updateButton.setToolTipText("Update the selected host");
    updateButton.addActionListener(this);
    updateButton.setActionCommand("Update");

    removeButton = new JButton("Remove");
    removeButton.setToolTipText("Remove the selected host");
    removeButton.addActionListener(this);
    removeButton.setActionCommand("Remove");

    closeButton = new JButton("Close");
    closeButton.setToolTipText("Close this dialog");
    closeButton.addActionListener(this);
    closeButton.setActionCommand("Close");

    //Put the buttons in a panel
    JPanel buttonPane = new JPanel();
    buttonPane.add(addButton);
    buttonPane.add(resetButton);
    buttonPane.add(removeButton);
    buttonPane.add(updateButton);
    buttonPane.add(closeButton);
    buttonPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
    
    JPanel contentPane = new JPanel();
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    contentPane.setLayout(new BorderLayout());
    contentPane.add(upperPane, BorderLayout.NORTH);
    contentPane.add(hostListScrollPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);
    setContentPane(contentPane);
    getRootPane().setDefaultButton(addButton);
  }

  private void remove() {
    // Extract the selected host from the list
    int hostIndex = hostList.getSelectedIndex();
    if (hostIndex < 0) {
      JOptionPane.showMessageDialog(this, "Please select a host!");
      return;
    }
    
    String sHostId = (String)hostVector.get(hostIndex);
    String sHostName = (String)hostListModel.get(hostIndex);

    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("deleteHost", sHostId);
      res = sslClient.sendReceive(cmd, null);

      // The result is either failure or success
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }

      // Remove the selected host from the list.
      hostListModel.removeElementAt(hostIndex);
      hostVector.removeElementAt(hostIndex);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  private void add() {
    // Check all mandatory fields are filled out
    String name = nameField.getText();
    String repo = repoField.getText();
//    String reserved = reservedField.getText();
//    String ipa = ipaField.getText();
//    String descr = descrField.getText();

    if (name == null || name.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter the host name!");
      return;
    }
    if (repo == null || repo.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter the repository path!");
      return;
    }
//    if (descr == null || descr.length() == 0) {
//      descr = name;
//    }

    // The boolean values will translate to 'true' or 'false'.
    Packet res = null;

    try {
//        Packet cmd = tool.makeCmd("addHost", name, repo, reserved,
//                (ipa == null || ipa.length() == 0)? "" : ipa,
//                descr, String.valueOf(pdcBox.isSelected()));
        Packet cmd = tool.makeCmd("addHost", name, repo);
      res = sslClient.sendReceive(cmd, null);

      // The result contains <host name>:<host id> or failure.
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
      String sLine = res.getStringValue(0);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(hostListModel, pieces[0]);
      hostListModel.add(index, pieces[0]);
      hostVector.add(index, pieces[1]);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }

  public void update() {
    // Extract the selected host from the list
    int hostIndex = hostList.getSelectedIndex();
    if (hostIndex < 0) {
      JOptionPane.showMessageDialog(this, "Please select a host!");
      return;
    }
    
    String sHostId = (String)hostVector.get(hostIndex);
    String name = nameField.getText();
    String repo = repoField.getText();
//    String ipa = ipaField.getText();
//    String descr = descrField.getText();

    if (name == null || name.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter the host name!");
      return;
    }
    if (repo == null || repo.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter the repository path!");
      return;
    }
//    if (descr == null || descr.length() == 0) {
//      descr = name;
//    }
//    String isPdc = String.valueOf(pdcBox.isSelected());

    // The boolean values will translate to 'true' or 'false'.
    Packet res = null;

    try {
//        Packet cmd = tool.makeCmd("updateHost", sHostId, name, repo,
//                (ipa == null || ipa.length() == 0)? "Ignored" : ipa,
//                descr, isPdc);
        Packet cmd = tool.makeCmd("updateHost", sHostId, name, repo);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
      hostListModel.setElementAt(name, hostIndex);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, e.getMessage());
    }
  }
  
  public void reset() {
    nameField.setText("");
    repoField.setText("");
    reservedField.setText("");
//    ipaField.setText("");
//    descrField.setText("");
//    pdcBox.setSelected(false);
    
    hostList.clearSelection();
  }

  private void close() {
    this.setVisible(false);
  }

  public Object getHosts() throws Exception {
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getHosts");
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

  // Called just before making it visible
  public void prepare() {
    Packet hosts;

    hostListModel.clear();
    if (hostVector == null) hostVector = new Vector();
    else hostVector.clear();
    
    try {
      hosts = (Packet)getHosts();

      // Add the hosts to the list. List needs to be sorted!!!
      if (hosts != null) for (int i = 0; i < hosts.size(); i++) {
        String sLine = hosts.getStringValue(i);
        String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
        int index = PmAdmin.getIndex(hostListModel, pieces[0]);
        hostListModel.add(index, pieces[0]);
        hostVector.add(index, pieces[1]);
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(tool, e.getMessage());
      e.printStackTrace();
    }
  }

  // Returns an ArrayList with the contents:
  // item 0: <host name>:<host id>:<is pdc>
  // item 1: <repository path>.
  public Object getHostInfo(String sHostId) {
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getHostInfo", sHostId);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Undetermined error");
        return null;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return null;
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception: " + exc.getMessage());
      return null;
    }
    return res;
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("Remove")) {
      remove();
    } else if (e.getActionCommand().equalsIgnoreCase("Add")) {
      add();
    } else if (e.getActionCommand().equalsIgnoreCase("Reset")) {
      reset();
    } else if (e.getActionCommand().equalsIgnoreCase("Update")) {
      update();
    } else if (e.getActionCommand().equalsIgnoreCase("Close")) {
      close();
    }
  }
  
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    
    String sLine;
    String[] pieces;
    
    int hostIndex = hostList.getSelectedIndex();
    if (hostIndex < 0) return;
    
    String sHostId = (String)hostVector.get(hostIndex);
    String sHostName = (String)hostListModel.get(hostIndex);
    
    // Get all information about this host.
    Packet res = (Packet)getHostInfo(sHostId);
    if (res == null) return;
    
    // The information returned by getHostInfo has the following format:
    // item 0: <host name>:<host id>:<is pdc> (e.g., musial:1823A...:true).
    // item 1: <host repo>
    sLine = res.getStringValue(0);
    pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
//    String sPdc = pieces[2];

    String sRepo = res.getStringValue(1);
    
    nameField.setText(sHostName);
    repoField.setText(sRepo);
//    pdcBox.setSelected(sPdc.equalsIgnoreCase("true"));
  }
}

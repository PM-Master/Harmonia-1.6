/*
 * StaticEditor.java
 *
 * Created on May 2, 2006, 3:51 PM
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
public class SconEditor extends JDialog implements ActionListener, ListSelectionListener {
  
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

  // UI components.
  /**
 * @uml.property  name="sconLabel"
 * @uml.associationEnd  readOnly="true"
 */
private JLabel sconLabel;
  /**
 * @uml.property  name="sconField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField sconField;
  /**
 * @uml.property  name="kLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel kLabel;
  /**
 * @uml.property  name="kField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField kField;
  
  /**
 * @uml.property  name="taskListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel taskListLabel;
  /**
 * @uml.property  name="taskListModel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private DefaultListModel taskListModel;
  /**
 * @uml.property  name="taskList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList taskList;
  /**
 * @uml.property  name="taskVector"
 */
private Vector taskVector;
  
  /**
 * @uml.property  name="sconListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel sconListLabel;
  /**
 * @uml.property  name="sconListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel sconListModel;
  /**
 * @uml.property  name="sconList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList sconList;
  /**
 * @uml.property  name="sconVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector sconVector;

  /**
 * @uml.property  name="ctaskListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel ctaskListLabel;
  /**
 * @uml.property  name="ctaskListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel ctaskListModel;
  /**
 * @uml.property  name="ctaskList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList ctaskList;

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
 * @uml.property  name="checkButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton checkButton;
  /**
 * @uml.property  name="checkAllButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton checkAllButton;
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

  public SconEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);

    this.tool = tool;
    this.sslClient = sslClient;
    setTitle("Edit Static Constraints");
    this.setResizable(false);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        doClose();
      }
    });

    // Start building the GUI
    JLabel sconLabel = new JLabel("Function name:");
    sconField = new JTextField(22);
    JPanel sconPane = new JPanel(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(sconPane, sconLabel, 0, 0, 1, 1);
    addComp(sconPane, sconField, 0, 1, 3, 1);
    sconPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
    
    JLabel dummyLabel = new JLabel("         ");
    JPanel dummyPane = new JPanel();
    dummyPane.add(dummyLabel);
//    dummyPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
    
    kLabel = new JLabel("Threshold k:");
    kField = new JTextField(22);
    JPanel kPane = new JPanel();
    kPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(kPane, kLabel, 0, 0, 1, 1);
    addComp(kPane, kField, 0, 1, 3, 1);
    kPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
    
/*
    JPanel upperLeftPane = new JPanel(new BorderLayout());
    upperLeftPane.add(sconPane, BorderLayout.NORTH);
    upperLeftPane.add(kPane, BorderLayout.SOUTH);
    upperLeftPane.add(dummyPane, BorderLayout.CENTER);
*/
    JPanel upperLeftPane = new JPanel(new GridLayout(3,1));
    upperLeftPane.add(sconPane);
    upperLeftPane.add(kPane);
    upperLeftPane.add(dummyPane);

    // Upper right pane contains the task list.
    taskListLabel = new JLabel("Select task:");
    taskListModel = new DefaultListModel();
    taskList = new JList(taskListModel);
    JScrollPane taskListScrollPane = new JScrollPane(taskList);
    taskListScrollPane.setPreferredSize(new Dimension(240,130));

    JPanel upperRightPane = new JPanel();
    upperRightPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(upperRightPane, taskListLabel, 0, 0, 1, 1);
    addComp(upperRightPane, taskListScrollPane, 0, 1, 3, 1);
    upperRightPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
    
    JPanel upperPane = new JPanel();
    upperPane.add(upperLeftPane);
    upperPane.add(upperRightPane);
    
    // The constraint table and the task table.
    sconListLabel = new JLabel("Existing functions:");
    sconListModel = new DefaultListModel();
    sconList = new JList(sconListModel);
    sconList.addListSelectionListener(this);
    JScrollPane sconListScrollPane = new JScrollPane(sconList);
    sconListScrollPane.setPreferredSize(new Dimension(240, 160));

    JPanel sconListPane = new JPanel();
    sconListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(sconListPane, sconListLabel, 0, 0, 1, 1);
    addComp(sconListPane, sconListScrollPane, 0, 1, 3, 1);
    sconListPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The table with a constraint's tasks.
    ctaskListLabel = new JLabel("Tasks:");
    ctaskListModel = new DefaultListModel();
    ctaskList = new JList(ctaskListModel);
    JScrollPane ctaskListScrollPane = new JScrollPane(ctaskList);
    ctaskListScrollPane.setPreferredSize(new Dimension(240, 160));

    JPanel ctaskListPane = new JPanel();
    ctaskListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(ctaskListPane, ctaskListLabel, 0, 0, 1, 1);
    addComp(ctaskListPane, ctaskListScrollPane, 0, 1, 4, 1);
    ctaskListPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The panel with the constraint and task lists.
    JPanel middlePane = new JPanel();
    middlePane.add(sconListPane);
    middlePane.add(ctaskListPane);

    // The buttons.
    addButton = new JButton("Add");
    addButton.addActionListener(this);
    addButton.setActionCommand("add");

    deleteButton = new JButton("Delete");
    deleteButton.addActionListener(this);
    deleteButton.setActionCommand("delete");

    checkButton = new JButton("Check");
    checkButton.addActionListener(this);
    checkButton.setActionCommand("check");

    checkAllButton = new JButton("Check all");
    checkAllButton.addActionListener(this);
    checkAllButton.setActionCommand("check all");

    resetButton = new JButton("Reset");
    resetButton.addActionListener(this);
    resetButton.setActionCommand("reset");

    closeButton = new JButton("Close");
    closeButton.addActionListener(this);
    closeButton.setActionCommand("close");
    
    JPanel buttonPane = new JPanel();
    buttonPane.add(addButton);
    buttonPane.add(deleteButton);
    //buttonPane.add(checkButton);
    //buttonPane.add(checkAllButton);
    buttonPane.add(resetButton);
    buttonPane.add(closeButton);
    buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    // The content pane.
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(upperPane, BorderLayout.NORTH);
    contentPane.add(middlePane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    setContentPane(contentPane);
  }
  
  public void prepare() {
    // Clear lists and vectors.
    taskListModel.clear();
    if (taskVector == null) taskVector = new Vector();
    else taskVector.clear();

    sconListModel.clear();
    if (sconVector == null) sconVector = new Vector();
    else sconVector.clear();
    
    ctaskListModel.clear();

    // Fill the task list.
    Packet res = (Packet)getTasks();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      
      // The returned line contains <task name>:<task id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(taskListModel, pieces[0]);
      taskListModel.add(index, pieces[0]);
      taskVector.add(index, pieces[1]);
    }
    
    // Fill the constraint list.
    res = (Packet)getScons();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      
      // The returned line contains <constraint name>:<constraint id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(sconListModel, pieces[0]);
      sconListModel.add(index, pieces[0]);
      sconVector.add(index, pieces[1]);
    }
  }

  // Get all static constraints.
  private Object getScons() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getScons");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getScons()");
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

  private Object getTasks() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getTasks");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getTasks()");
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

  private void addComp(Container container, Component component, int x, int y, int w, int h) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = w;
    constraints.gridheight = h;
    container.add(component, constraints);
  }

  private void doAdd() {
    String sSconName = sconField.getText().trim();
    if (sSconName.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter or select a function name!");
      sconField.requestFocus();
      return;
    }

    String sK = kField.getText().trim();
    
    String sTask = (String)taskList.getSelectedValue();
    if (sTask != null) {
      taskList.clearSelection();
    }
    sconField.requestFocus();

    // Send the command and let the server test the other conditions.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("addScon", sSconName,
        (sK == null || sK.length() == 0)? "" : sK,
        (sTask == null)? "" : sTask);    
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

    // If the add operation is successful, the result contains <name>:<id> of
    // the constraint.
    String sLine = res.getStringValue(0);
    String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
    // If the constraint is new, add it to the constraint list and vector.
    if (!sconListModel.contains(pieces[0])) {
      int index = PmAdmin.getIndex(sconListModel, pieces[0]);
      sconListModel.add(index, pieces[0]);
      sconList.ensureIndexIsVisible(index);
      sconVector.add(index, pieces[1]);
    }
    sconList.clearSelection();
    ctaskListModel.clear();
    
    sconList.setSelectedValue(pieces[0], true);
  }

  private void doDelete() {
    int index = sconList.getSelectedIndex();
    if (index < 0) {
      JOptionPane.showMessageDialog(this, "Please select a function!");
      return;
    }
    String sSconId = (String)sconVector.elementAt(index);
    String sTask = (String)ctaskList.getSelectedValue(); // could be null.

    // Send the command and let the server test the other conditions.
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("deleteScon", sSconId,
        (sTask == null)? "" : sTask);
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

    if (sTask == null) {
      sconListModel.removeElementAt(index);
      sconList.clearSelection();
      sconVector.removeElementAt(index);
      ctaskListModel.clear();
      sconField.setText(null);
      kField.setText(null);
    } else {
      ctaskListModel.removeElement(sTask);
      ctaskList.clearSelection();
    }
  }

  private void doClose() {
    this.setVisible(false);
  }

  private void doCheck() {
    int index = sconList.getSelectedIndex();
    if (index < 0) {
      JOptionPane.showMessageDialog(this, "Please select the function to check!");
      return;
    }
    String sSconId = (String)sconVector.elementAt(index);

    // Send the command and let the server test the other conditions.
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("checkScon", sSconId);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      } else {
        JOptionPane.showMessageDialog(this, "OK");
        return;
      }        
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.getMessage());
      return;
    }    
  }

  private void doCheckAll() {
  }
  
  private void doReset() {
    sconList.clearSelection();
    ctaskListModel.clear();
    kField.setText(null);
    sconField.setText(null);
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("add")) {
      doAdd();
    } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
      doDelete();
    } else if (e.getActionCommand().equalsIgnoreCase("check")) {
      doCheck();
    } else if (e.getActionCommand().equalsIgnoreCase("check all")) {
      doCheckAll();
    } else if (e.getActionCommand().equalsIgnoreCase("reset")) {
      doReset();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      doClose();
    }
  }

  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    
    int index = sconList.getSelectedIndex();
    if (index < 0) return;
    String sSconName = (String)sconListModel.get(index);
    String sSconId = (String)sconVector.get(index);
    sconField.setText(sSconName);

    // Get all information about this constraint.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getSconInfo", sSconId);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Undetermined error");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(this, exc.getMessage());
      return;
    }
    ctaskListModel.clear();
    // Item 1 contains k as a string.
    String sK = res.getStringValue(1);
    kField.setText(sK);
    
    for (int i = 2; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      // Each item contains: <task name>:<task id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      ctaskListModel.addElement(pieces[0]);
    }
  }
}

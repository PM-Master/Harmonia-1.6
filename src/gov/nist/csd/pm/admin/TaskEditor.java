/*
 * TaskEditor.java
 *
 * Created on May 2, 2006, 12:03 PM
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
public class TaskEditor extends JDialog implements ActionListener, ListSelectionListener {
  
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
 * @uml.property  name="taskLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel taskLabel;
  /**
 * @uml.property  name="taskField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField taskField;
  
  /**
 * @uml.property  name="opListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel opListLabel;
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
  
  // The list of object containers.
  /**
 * @uml.property  name="ocontListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel ocontListLabel;
  /**
 * @uml.property  name="ocontListModel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private DefaultListModel ocontListModel;
  /**
 * @uml.property  name="ocontList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList ocontList;
  /**
 * @uml.property  name="ocontVector"
 */
private Vector ocontVector;

  /**
 * @uml.property  name="pcListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel pcListLabel;
  /**
 * @uml.property  name="pcListModel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private DefaultListModel pcListModel;
  /**
 * @uml.property  name="pcList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList pcList;
  /**
 * @uml.property  name="pcVector"
 */
private Vector pcVector;

  /**
 * @uml.property  name="taskListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel taskListLabel;
  /**
 * @uml.property  name="taskListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel taskListModel;
  /**
 * @uml.property  name="taskList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList taskList;
  /**
 * @uml.property  name="taskVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector taskVector;

  /**
 * @uml.property  name="capListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel capListLabel;
  /**
 * @uml.property  name="capListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel capListModel;
  /**
 * @uml.property  name="capList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList capList;

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
 * @uml.property  name="closeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton closeButton;

  public TaskEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);

    this.tool = tool;
    this.sslClient = sslClient;
    setTitle("Edit Tasks");
    this.setResizable(false);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        doClose();
      }
    });

    // Start building the GUI
    // First the task label and field.
    taskLabel = new JLabel("Task Name:");
    taskField = new JTextField(23);
    
    JPanel taskPane = new JPanel();
    taskPane.add(taskLabel);
    taskPane.add(taskField);
    taskPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    
    // The table with available ops.
    opListLabel = new JLabel("Operations:");
    opListModel = new DefaultListModel();
    opList = new JList(opListModel);
    JScrollPane opListScrollPane = new JScrollPane(opList);
    opListScrollPane.setPreferredSize(new Dimension(200, 160));

    JPanel opListPane = new JPanel();
    opListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(opListPane, opListLabel, 0, 0, 1, 1);
    addComp(opListPane, opListScrollPane, 0, 1, 3, 1);
    opListPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The table with available object containers.
    ocontListLabel = new JLabel("Object containers:");
    ocontListModel = new DefaultListModel();
    ocontList = new JList(ocontListModel);
    JScrollPane ocontListScrollPane = new JScrollPane(ocontList);
    ocontListScrollPane.setPreferredSize(new Dimension(200, 160));

    JPanel ocontListPane = new JPanel();
    ocontListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(ocontListPane, ocontListLabel, 0, 0, 1, 1);
    addComp(ocontListPane, ocontListScrollPane, 0, 1, 3, 1);
    ocontListPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The table with available policies.
    pcListLabel = new JLabel("Policies:");
    pcListModel = new DefaultListModel();
    pcList = new JList(pcListModel);
    JScrollPane pcListScrollPane = new JScrollPane(pcList);
    pcListScrollPane.setPreferredSize(new Dimension(200, 160));

    JPanel pcListPane = new JPanel();
    pcListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(pcListPane, pcListLabel, 0, 0, 1, 1);
    addComp(pcListPane, pcListScrollPane, 0, 1, 3, 1);
    pcListPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The panel with the op, obj, and pc lists.
    JPanel opOcontAndPcPane = new JPanel();
    opOcontAndPcPane.setLayout(new GridLayout(1, 3));
    opOcontAndPcPane.add(opListPane);
    opOcontAndPcPane.add(ocontListPane);
    opOcontAndPcPane.add(pcListPane);

    // The table with existing tasks.
    taskListLabel = new JLabel("Existing tasks:");
    taskListModel = new DefaultListModel();
    taskList = new JList(taskListModel);
    taskList.addListSelectionListener(this);
    JScrollPane taskListScrollPane = new JScrollPane(taskList);
    taskListScrollPane.setPreferredSize(new Dimension(200, 160));

    JPanel taskListPane = new JPanel();
    taskListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(taskListPane, taskListLabel, 0, 0, 1, 1);
    addComp(taskListPane, taskListScrollPane, 0, 1, 3, 1);
    taskListPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The table with a task's capabilities.
    capListLabel = new JLabel("Capabilities:");
    capListModel = new DefaultListModel();
    capList = new JList(capListModel);
    JScrollPane capListScrollPane = new JScrollPane(capList);
    capListScrollPane.setPreferredSize(new Dimension(415, 160));

    JPanel capListPane = new JPanel();
    capListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(capListPane, capListLabel, 0, 0, 1, 1);
    addComp(capListPane, capListScrollPane, 0, 1, 4, 1);
    capListPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The panel with the task and capability lists.
    JPanel taskAndCapsPane = new JPanel();
    taskAndCapsPane.add(taskListPane);
    taskAndCapsPane.add(capListPane);

    // The panel with lists.
    JPanel listPane = new JPanel();
    listPane.setLayout(new GridLayout(2, 1));
    listPane.add(opOcontAndPcPane);
    listPane.add(taskAndCapsPane);

    // The buttons.
    addButton = new JButton("Add");
    addButton.addActionListener(this);
    addButton.setActionCommand("add");

    deleteButton = new JButton("Delete");
    deleteButton.addActionListener(this);
    deleteButton.setActionCommand("delete");

    closeButton = new JButton("Close");
    closeButton.addActionListener(this);
    closeButton.setActionCommand("close");
    
    JPanel buttonPane = new JPanel();
    buttonPane.add(addButton);
    buttonPane.add(deleteButton);
    buttonPane.add(closeButton);
    buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The content pane.
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(taskPane, BorderLayout.NORTH);
    contentPane.add(listPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    setContentPane(contentPane);
  }
  
  public void prepare() {
    // Clear lists and vectors.
    opListModel.clear();
    
    ocontListModel.clear();
    if (ocontVector == null) ocontVector = new Vector();
    else ocontVector.clear();
    
    pcListModel.clear();
    if (pcVector == null) pcVector = new Vector();
    else pcVector.clear();

    taskListModel.clear();
    if (taskVector == null) taskVector = new Vector();
    else taskVector.clear();

    capListModel.clear();
    
    // Fill the operation list.
    Packet res = (Packet)getAllOps();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      int index = PmAdmin.getIndex(opListModel, sLine);
      opListModel.add(index, sLine);
    }

    // Fill the object container list.
    res = (Packet)getOconts();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      
      // The returned line contains <oattr name>:<oattr id>
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(ocontListModel, pieces[0]);
      ocontListModel.add(index, pieces[0]);
      ocontVector.add(index, pieces[1]);
    }
    
    // Fill the pc list.
    res = (Packet)getPolicyClasses();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      
      // The returned line contains <pc name>:<pc id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(pcListModel, pieces[0]);
      pcListModel.add(index, pieces[0]);
      pcVector.add(index, pieces[1]);
    }
    
    // Fill the task list.
    res = (Packet)getTasks();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      
      // The returned line contains <task name>:<task id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(taskListModel, pieces[0]);
      taskListModel.add(index, pieces[0]);
      taskVector.add(index, pieces[1]);
    }
  }

  private Object getPolicyClasses() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getPolicyClasses");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getAllOps()");
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

  private Object getAllOps() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getAllOps");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getAllOps()");
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

  private Object getObjects() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getObjects");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getAllOps()");
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

  // Get all object containers.
  private Object getOconts() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getOconts");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getOconts()");
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
    String sTaskName = taskField.getText().trim();
    if (sTaskName.length() == 0) {
      JOptionPane.showMessageDialog(tool, "Please enter or select a task name!");
      taskField.requestFocus();
      return;
    }

    String sOp = (String)opList.getSelectedValue();
    if (sOp != null) {
      opList.clearSelection();
    }
    String sOcont = (String)ocontList.getSelectedValue();
    if (sOcont != null) {
      ocontList.clearSelection();
    }
    String sPc = (String)pcList.getSelectedValue();
    if (sPc != null) {
      pcList.clearSelection();
    }
    taskField.requestFocus();

    // Send the command and let the server test the other conditions.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("addTask", sTaskName,
        (sOp == null)? "" : sOp,
        (sOp == null)? "" : sOcont,
        (sOp == null)? "" : sPc);
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
    // the task.
    String sLine = res.getStringValue(0);
    String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
    // If the task is new, add it to the task list and vector.
    if (!taskListModel.contains(pieces[0])) {
      int index = PmAdmin.getIndex(taskListModel, pieces[0]);
      taskListModel.add(index, pieces[0]);
      taskList.ensureIndexIsVisible(index);
      taskVector.add(index, pieces[1]);
    }
    taskList.clearSelection();
    capListModel.clear();
  }

  private void doDelete() {
    int index = taskList.getSelectedIndex();
    if (index < 0) {
      JOptionPane.showMessageDialog(this, "Please select a task!");
      return;
    }
    String sTaskId = (String)taskVector.elementAt(index);
    String sCap = (String)capList.getSelectedValue(); // could be null.

    // Send the command and let the server test the other conditions.
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("deleteTask", sTaskId,
        (sCap == null)? "" : sCap);
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

    if (sCap == null) {
      taskListModel.removeElementAt(index);
      taskList.clearSelection();
      taskVector.removeElementAt(index);
      capListModel.clear();
    } else {
      capListModel.removeElement(sCap);
      capList.clearSelection();
    }
    taskField.setText(null);
  }
  
  private void doClose() {
    this.setVisible(false);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("add")) {
      doAdd();
    } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
      doDelete();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      doClose();
    }
  }

  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    
    int index = taskList.getSelectedIndex();
    if (index < 0) return;
    String sTaskName = (String)taskListModel.get(index);
    String sTaskId = (String)taskVector.get(index);
    taskField.setText(sTaskName);

    // Get all information about this task.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getTaskInfo", sTaskId);
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
      JOptionPane.showMessageDialog(tool, exc.getMessage());
      return;
    }
    capListModel.clear();
    for (int i = 1; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      // Contains: <op name>,<obj name>:<obj id>,<pc name>:<pc id>
      String[] pieces = sLine.split(PmAdmin.PM_LIST_MEMBER_SEP);
      String[] objPieces = pieces[1].split(PmAdmin.PM_FIELD_DELIM);
      String[] pcPieces = pieces[2].split(PmAdmin.PM_FIELD_DELIM);
      capListModel.addElement(pieces[0] + PmAdmin.PM_LIST_MEMBER_SEP +
        objPieces[0] + PmAdmin.PM_LIST_MEMBER_SEP + pcPieces[0]);
    }
  }
}

/*
 * ObjClassEditor.java
 *
 * Created on April 18, 2005, 11:41 AM
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


/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class ObjClassEditor extends JDialog implements ActionListener, ListSelectionListener {

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
 * @uml.property  name="classField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField classField;
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
 * @uml.property  name="opField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField opField;

  /**
 * @uml.property  name="addButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton addButton;
  /**
 * @uml.property  name="refreshButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton refreshButton;
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
 * @uml.property  name="classListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel classListModel;
  /**
 * @uml.property  name="classList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList classList;
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
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();

  public ObjClassEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);  // modal

    this.tool = tool;
    this.sslClient = sslClient;

    setTitle("Object Classes");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

    // Start building the GUI.
    JPanel classPane = new JPanel();
    classPane.setLayout(new GridBagLayout());

    JLabel classLabel = new JLabel("Class Name:");
    JLabel descrLabel = new JLabel("Description:");
    JLabel infoLabel = new JLabel("Other Info:");
    JLabel opLabel = new JLabel("Operation:");
    
    classField = new JTextField(20);
    descrField = new JTextField(20);
    infoField = new JTextField(20);
    opField = new JTextField(20);

    constraints.insets = new Insets(0, 10, 0, 0);

    addComp(classPane, classLabel, 0, 0, 1, 1);
    addComp(classPane, descrLabel, 0, 1, 1, 1);
    addComp(classPane, infoLabel, 0, 2, 1, 1);

    addComp(classPane, classField, 1, 0, 3, 1);
    addComp(classPane, descrField, 1, 1, 3, 1);
    addComp(classPane, infoField, 1, 2, 3, 1);

    constraints.insets = new Insets(10, 10, 0, 0);
    addComp(classPane, opLabel, 0, 3, 1, 1);
    addComp(classPane, opField, 1, 3, 3, 1);

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
/*
    upperPane.setLayout(new GridLayout(0, 1));
    upperPane.add(classPane);
    upperPane.add(buttonPane);
 */
    upperPane.setLayout(new BorderLayout());
    upperPane.add(classPane, BorderLayout.CENTER);
    upperPane.add(buttonPane, BorderLayout.SOUTH);
    upperPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 0, 0),
      BorderFactory.createTitledBorder("New Class/Operation")));

    // The lists pane
    JPanel listPane = new JPanel();
    listPane.setLayout(new GridBagLayout());

    JLabel classesLabel = new JLabel("Classes:");
    JLabel opsLabel = new JLabel("Operations:");

    classListModel = new DefaultListModel();
    classList = new JList(classListModel);
    classList.addListSelectionListener(this);
    JScrollPane classListScrollPane = new JScrollPane(classList);
    classListScrollPane.setPreferredSize(new Dimension(220,100));

    opListModel = new DefaultListModel();
    opList = new JList(opListModel);
    JScrollPane opListScrollPane = new JScrollPane(opList);
    opListScrollPane.setPreferredSize(new Dimension(220,100));

    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(listPane, classesLabel, 0, 0, 1, 1);
    constraints.insets = new Insets(0, 10, 0, 0);
    addComp(listPane, opsLabel, 2, 0, 1, 1);

    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(listPane, classListScrollPane, 0, 1, 2, 3);
    constraints.insets = new Insets(0, 10, 0, 0);
    addComp(listPane, opListScrollPane, 2, 1, 3, 3);

    // The list button pane
    JPanel listButtonPane = new JPanel();
    listButtonPane.setLayout(new GridBagLayout());

    deleteButton = new JButton("Delete");
    deleteButton.setActionCommand("delete");
    deleteButton.addActionListener(this);

    refreshButton = new JButton("Refresh");
    refreshButton.setActionCommand("refresh");
    refreshButton.addActionListener(this);

    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(listButtonPane, deleteButton, 0, 0, 1, 1);
    addComp(listButtonPane, refreshButton, 1, 0, 1, 1);

    listButtonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    
    
    JPanel middlePane = new JPanel();
    middlePane.setLayout(new BorderLayout());
    middlePane.add(listPane, BorderLayout.CENTER);
    middlePane.add(listButtonPane, BorderLayout.SOUTH);
    middlePane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 10, 0),
      BorderFactory.createTitledBorder("Existing Classes/Operations")));

    
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

  // This method must be called before making visible this frame,
  // to fill the "Classes" JList.
  public void prepare() {
    Packet res = null;

    classField.setText("");
    classField.requestFocus();
    descrField.setText("");
    infoField.setText("");
    opField.setText("");
    
    // Get the object classes and display them in a list.
    
    try {
      res = (Packet)getObjClasses();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception in getObjClasses(), " + e.getMessage());
      return;
    }

    opListModel.clear();
    classListModel.clear();
    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(classListModel, pieces[0]);
      classListModel.add(index, pieces[0]);
    }
  }

  private void close() {
    this.setVisible(false);
  }

  // What we delete depends on what is selected.
  // If a class and one of its ops are selected, only the op is deleted from
  // that class.
  // If only a class is selected, that class will be deleted.
  // The predefined classes (class, RccRole, NtFile) cannot be deleted or modified.
  private void delete() {
    String sClass = (String)classList.getSelectedValue();
    if (sClass == null || sClass.length() == 0) {
      JOptionPane.showMessageDialog(tool, "Please select a class and optionally an operation to delete");
      return;
    }
    String sOp = (String)opList.getSelectedValue();

    // Send the command and let the server test the other conditions.
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("deleteObjClassAndOp", sClass,
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
      classListModel.removeElement(sClass);
      classList.clearSelection();
      opListModel.removeAllElements();
    } else {
      opListModel.removeElement(sOp);
      opList.clearSelection();
    }
  }

  // The class name cannot be empty.
  // The predefined classes (class, RccRole, NtFile) cannot be modified.
  // If the class already exists, an operation must be specified, and it has
  // to be new for that class.
  // If the class does not exist, the operation is optional.
  private void add() {
    String sClass = classField.getText().trim();
    String sDescr = descrField.getText().trim();
    String sInfo = infoField.getText().trim();
    String sOp = opField.getText().trim();

    if (sClass.length() == 0) {
      JOptionPane.showMessageDialog(tool, "Please enter a class name and optionally an operation name");
      return;
    }
    if (sDescr.length() == 0) sDescr = sClass;
    if (sInfo.length() == 0) sInfo = sClass;

    if (sClass.equalsIgnoreCase(PmAdmin.PM_CLASS_CLASS) ||
        sClass.equalsIgnoreCase(PmAdmin.PM_DIR_CLASS) ||
        sClass.equalsIgnoreCase(PmAdmin.PM_FILE_CLASS)) {
      JOptionPane.showMessageDialog(tool, "Cannot add operations to class \""+ sClass + "\"");
      return;
    }

    // Send the command and let the server test the other conditions.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("addObjClassAndOp", sClass, sDescr, sInfo,
        (sOp.length() == 0)? "" : sOp);

      opField.setText("");

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

    if (!classListModel.contains(sClass)) {
      int index = PmAdmin.getIndex(classListModel, sClass);
      classListModel.add(index, sClass);
    }
    selectClass(sClass);
  }

  // Select the class, but first unselect it to trigger a valueChanged() call.
  // NOTE that clearSelection() also triggers a valueChanged() with a
  // null selection. Thus, we have to test whether the selection is null
  // in valueChanged().
  private void selectClass(String sClass) {
    classList.clearSelection();
    classList.setSelectedValue(sClass, true);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("add")) {
      add();
    } else if (e.getActionCommand().equalsIgnoreCase("refresh")) {
      prepare();
    } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
      delete();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      close();
    }
  }

  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;

    // Get the selected value, which can be null.
    String sClass = (String)classList.getSelectedValue();
    if (sClass == null || sClass.length() == 0) return;

    classField.setText(sClass);

    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getObjClassOps", sClass);
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

    opListModel.clear();
    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      int index = PmAdmin.getIndex(opListModel, sLine);
      opListModel.add(index, sLine);
    }
  }
}

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
public class SconaEditor extends JDialog implements ActionListener, ListSelectionListener {
  
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
 * @uml.property  name="sconaLabel"
 * @uml.associationEnd  readOnly="true"
 */
private JLabel sconaLabel;
  /**
 * @uml.property  name="sconaField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField sconaField;

  /**
 * @uml.property  name="pcLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel pcLabel;
  /**
 * @uml.property  name="pcCombo"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JComboBox pcCombo;

  /**
 * @uml.property  name="uattrListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel uattrListLabel;
  /**
 * @uml.property  name="uattrListModel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private DefaultListModel uattrListModel;
  /**
 * @uml.property  name="uattrList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList uattrList;
  
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

  /**
 * @uml.property  name="oattrListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel oattrListLabel;
  /**
 * @uml.property  name="oattrListModel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private DefaultListModel oattrListModel;
  /**
 * @uml.property  name="oattrList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList oattrList;

  /**
 * @uml.property  name="sconaListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel sconaListLabel;
  /**
 * @uml.property  name="sconaListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel sconaListModel;
  /**
 * @uml.property  name="sconaList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList sconaList;
  /**
 * @uml.property  name="sconaVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector sconaVector;

  /**
 * @uml.property  name="uattrLabel"
 * @uml.associationEnd  readOnly="true"
 */
private JLabel uattrLabel;
  /**
 * @uml.property  name="uattrField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField uattrField;
  
  /**
 * @uml.property  name="oattrLabel"
 * @uml.associationEnd  readOnly="true"
 */
private JLabel oattrLabel;
  /**
 * @uml.property  name="oattrField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField oattrField;
  
  /**
 * @uml.property  name="copListLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel copListLabel;
  /**
 * @uml.property  name="copListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel copListModel;
  /**
 * @uml.property  name="copList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList copList;

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

  public SconaEditor(PmAdmin tool, SSLSocketClient sslClient) {
    //super(tool, true);
    super(tool, false);

    this.tool = tool;
    this.sslClient = sslClient;
    setTitle("Edit Static Constraints for Attributes");
    this.setResizable(false);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        doClose();
      }
    });

    // Start building the GUI
    
    // Uppermost panel will contain the name of the new or selected constraint.
    JLabel sconaLabel = new JLabel("Constraint name:");
    sconaField = new JTextField(18);
    JPanel sconaPane = new JPanel(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(sconaPane, sconaLabel, 0, 0, 1, 1);
    addComp(sconaPane, sconaField, 0, 1, 3, 1);
    sconaPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 0));
    
    pcLabel = new JLabel("Policy:");
    pcCombo = new JComboBox();
    pcCombo.setPreferredSize(new Dimension(200,25));
    JPanel pcPane = new JPanel(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(pcPane, pcLabel, 0, 0, 1, 1);
    addComp(pcPane, pcCombo, 0, 1, 3, 1);
    pcPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 0));

    // A panel with the constraint name and the selected pc.
    JPanel upperPane = new JPanel();
    upperPane.add(sconaPane);
    upperPane.add(pcPane);
        
    // Then the user attribute list, the operation list, and the object attribute list.
    // User attributes list.
    uattrListLabel = new JLabel("Select user attribute:");
    uattrListModel = new DefaultListModel();
    uattrList = new JList(uattrListModel);
    JScrollPane uattrListScrollPane = new JScrollPane(uattrList);
    uattrListScrollPane.setPreferredSize(new Dimension(200,130));

    JPanel uattrListPane = new JPanel();
    uattrListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(uattrListPane, uattrListLabel, 0, 0, 1, 1);
    addComp(uattrListPane, uattrListScrollPane, 0, 1, 3, 1);
    uattrListPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));

    // Operations list
    opListLabel = new JLabel("Select operation:");
    opListModel = new DefaultListModel();
    opList = new JList(opListModel);
    JScrollPane opListScrollPane = new JScrollPane(opList);
    opListScrollPane.setPreferredSize(new Dimension(200,130));

    JPanel opListPane = new JPanel();
    opListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(opListPane, opListLabel, 0, 0, 1, 1);
    addComp(opListPane, opListScrollPane, 0, 1, 3, 1);
    opListPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));

    // Object attributes list.
    oattrListLabel = new JLabel("Select object attribute:");
    oattrListModel = new DefaultListModel();
    oattrList = new JList(oattrListModel);
    JScrollPane oattrListScrollPane = new JScrollPane(oattrList);
    oattrListScrollPane.setPreferredSize(new Dimension(200,130));

    JPanel oattrListPane = new JPanel();
    oattrListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(oattrListPane, oattrListLabel, 0, 0, 1, 1);
    addComp(oattrListPane, oattrListScrollPane, 0, 1, 3, 1);
    oattrListPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
    
    // A panel with those three lists.
    JPanel listPane = new JPanel();
    listPane.add(uattrListPane);
    listPane.add(opListPane);
    listPane.add(oattrListPane);

    // Then the existing constraints list, the user attribute and object attribute
    // of the selected contraint, and the list of operations of the selected
    // constraint.
    // The constraints list.
    sconaListLabel = new JLabel("Existing constraints:");
    sconaListModel = new DefaultListModel();
    sconaList = new JList(sconaListModel);
    sconaList.addListSelectionListener(this);
    JScrollPane sconaListScrollPane = new JScrollPane(sconaList);
    sconaListScrollPane.setPreferredSize(new Dimension(200,130));

    JPanel sconaListPane = new JPanel();
    sconaListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(sconaListPane, sconaListLabel, 0, 0, 1, 1);
    addComp(sconaListPane, sconaListScrollPane, 0, 1, 3, 1);
    sconaListPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));

    // The user attribute and object attribute of the selected constraint.
    JLabel uattrLabel = new JLabel("User attribute:");
    uattrField = new JTextField(18);
    JPanel uattrPane = new JPanel(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(uattrPane, uattrLabel, 0, 0, 1, 1);
    addComp(uattrPane, uattrField, 0, 1, 3, 1);
    uattrPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 0));

    JLabel oattrLabel = new JLabel("Object attribute:");
    oattrField = new JTextField(18);
    JPanel oattrPane = new JPanel(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(oattrPane, oattrLabel, 0, 0, 1, 1);
    addComp(oattrPane, oattrField, 0, 1, 3, 1);
    oattrPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 0));

    JPanel attrPane = new JPanel();
    attrPane.setLayout(new GridLayout(2, 1));
    attrPane.add(uattrPane);
    attrPane.add(oattrPane);
    
    // The constraint's operations list
    copListLabel = new JLabel("Operations:");
    copListModel = new DefaultListModel();
    copList = new JList(copListModel);
    JScrollPane copListScrollPane = new JScrollPane(copList);
    copListScrollPane.setPreferredSize(new Dimension(200,130));

    JPanel copListPane = new JPanel();
    copListPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(copListPane, copListLabel, 0, 0, 1, 1);
    addComp(copListPane, copListScrollPane, 0, 1, 3, 1);
    copListPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));

    // A panel for existing constraints.
    JPanel lowerPane = new JPanel();
    lowerPane.add(sconaListPane);
    lowerPane.add(attrPane);
    lowerPane.add(copListPane);

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

    // A big panel with all elements except the buttons.
    JPanel bigPane = new JPanel();
    bigPane.setLayout(new BorderLayout());
    bigPane.add(upperPane, BorderLayout.NORTH);
    bigPane.add(listPane, BorderLayout.CENTER);
    bigPane.add(lowerPane, BorderLayout.SOUTH);
    
    
    // The content pane.
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(bigPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    setContentPane(contentPane);
  }
  
  public void prepare() {
    // Clear lists, fields, vectors.
    uattrListModel.clear();
    opListModel.clear();
    oattrListModel.clear();
    
    sconaListModel.clear();
    if (sconaVector == null) sconaVector = new Vector();
    else sconaVector.clear();

    copListModel.clear();
    
    sconaField.setText(null);
    pcCombo.removeAllItems();

    uattrField.setText(null);
    oattrField.setText(null);

    // Fill the pc list.
    Packet res = (Packet)getPolicyClasses();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      // The returned line contains <uattr name>:<uattr id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      pcCombo.addItem(pieces[0]);
    }
    
    // Fill the user attribute list.
    res = (Packet)getUserAttributes();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      // The returned line contains <uattr name>:<uattr id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(uattrListModel, pieces[0]);
      uattrListModel.add(index, pieces[0]);
    }    

    // Fill the operation list.
    res = (Packet)getAllOps();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      int index = PmAdmin.getIndex(opListModel, sLine);
      opListModel.add(index, sLine);
    }    

    // Fill the object attribute list.
    res = (Packet)getOattrs();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      // The returned line contains <oattr name>:<oattr id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(oattrListModel, pieces[0]);
      oattrListModel.add(index, pieces[0]);
    }
    
    // Fill the constraint list.
    // Fill the constraint list.
    res = (Packet)getSconas();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      // The returned line contains <constraint name>:<constraint id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(sconaListModel, pieces[0]);
      sconaListModel.add(index, pieces[0]);
      sconaVector.add(index, pieces[1]);
    }
  }

  private Object getPolicyClasses() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getPolicyClasses");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getPolicyClasses()");
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

  private Object getOattrs() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getOattrs");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getOattrs()");
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

  private Object getUserAttributes() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getUserAttributes");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getUserAttributes()");
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
  
  // Get all static constraints.
  private Object getSconas() {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getSconas");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from getSconas()");
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
    String sSconaName = sconaField.getText().trim();
    if (sSconaName.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter or select a constraint name!");
      sconaField.requestFocus();
      return;
    }

    String sPcName = (String)pcCombo.getSelectedItem();
    
    String sUattr = (String)uattrList.getSelectedValue();
    if (sUattr != null) {
      uattrList.clearSelection();
    }
    String sOp = (String)opList.getSelectedValue();
    if (sOp != null) {
      opList.clearSelection();
    }
    String sOattr = (String)oattrList.getSelectedValue();
    if (sOattr != null) {
      oattrList.clearSelection();
    }

    // Send the command and let the server test the other conditions.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("addScona", sSconaName,
        (sPcName == null || sPcName.length() == 0)? "" : sPcName,
        (sUattr == null || sUattr.length() == 0)? "" : sUattr,
        (sOp == null || sOp.length() == 0)? "" : sOp,
        (sOattr == null || sOattr.length() == 0)? "" : sOattr);
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
    if (!sconaListModel.contains(pieces[0])) {
      int index = PmAdmin.getIndex(sconaListModel, pieces[0]);
      sconaListModel.add(index, pieces[0]);
      sconaList.ensureIndexIsVisible(index);
      sconaVector.add(index, pieces[1]);
    }
    sconaList.clearSelection();
    copListModel.clear();
    
    sconaList.setSelectedValue(pieces[0], true);
  }

  private void doDelete() {
    int index = sconaList.getSelectedIndex();
    if (index < 0) {
      JOptionPane.showMessageDialog(this, "Please select a constraint!");
      return;
    }
    String sSconaId = (String)sconaVector.elementAt(index);
    String sOp = (String)copList.getSelectedValue(); // could be null.

    // Send the command and let the server test the other conditions.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("deleteScona", sSconaId,
        (sOp == null)? "" : sOp);
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

    if (sOp == null) {
      sconaListModel.removeElementAt(index);
      sconaList.clearSelection();
      sconaVector.removeElementAt(index);
      copListModel.clear();
      sconaField.setText(null);
      uattrField.setText(null);
      oattrField.setText(null);
    } else {
      copListModel.removeElement(sOp);
      copList.clearSelection();
    }
  }

  private void doClose() {
    this.setVisible(false);
  }

  private void doCheck() {
    int index = sconaList.getSelectedIndex();
    if (index < 0) {
      JOptionPane.showMessageDialog(this, "Please select the constraint to check!");
      return;
    }
    String sSconaId = (String)sconaVector.elementAt(index);

    // Send the command and let the server test the other conditions.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("checkScona", sSconaId);
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
    sconaList.clearSelection();
    copListModel.clear();
    uattrField.setText(null);
    oattrField.setText(null);
    sconaField.setText(null);
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
    
    int index = sconaList.getSelectedIndex();
    if (index < 0) return;
    String sSconaName = (String)sconaListModel.get(index);
    String sSconaId = (String)sconaVector.get(index);
    sconaField.setText(sSconaName);

    // Get all information about this constraint.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getSconaInfo", sSconaId);
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
    copListModel.clear();
    // item 1: <pc name>:<pc id>
    // item 2: <ua name>:<ua id>
    // item 3: <oa name>:<oa id>
    // item 4,...: <op name>
    String sLine = res.getStringValue(1);
    String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
    pcCombo.setSelectedItem(pieces[0]);

    sLine = res.getStringValue(2);
    pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
    uattrField.setText(pieces[0]);
    
    sLine = res.getStringValue(3);
    pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
    oattrField.setText(pieces[0]);
    
    for (int i = 4; i < res.size(); i++) {
      sLine = res.getStringValue(i);
      copListModel.addElement(sLine);
    }
  }
}

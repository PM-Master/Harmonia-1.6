/*
 * SacEditor.java
 *
 * Created on June 6, 2005.
 * Revised on August 30, 2005.
 *
 * Serban I. Gavrila
 * For KT Consulting, Inc.
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
public class SacEditor extends JDialog implements ActionListener, ListSelectionListener {

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
 * @uml.property  name="sacField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField sacField;                 // The new sac name.

  /**
 * @uml.property  name="intraSessButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JRadioButton intraSessButton;
  /**
 * @uml.property  name="acrossSessButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JRadioButton acrossSessButton;

  /**
 * @uml.property  name="asetListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel asetListModel;      // Model and list of attrsets
  /**
 * @uml.property  name="asetList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList asetList;                      // to add.
  /**
 * @uml.property  name="asetVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector asetVector;
  
  /**
 * @uml.property  name="addButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton addButton;                   // The add aset button.
  
  /**
 * @uml.property  name="sacListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel sacListModel;      // Model and list of existing SACs.
  /**
 * @uml.property  name="sacList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList sacList;
  /**
 * @uml.property  name="sacVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector sacVector;
  
  /**
 * @uml.property  name="aset2ListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel aset2ListModel;     // Model and list of the selected
  /**
 * @uml.property  name="aset2List"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList aset2List;                     // sac's asets.
  /**
 * @uml.property  name="aset2Vector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector aset2Vector;
  
  /**
 * @uml.property  name="deleteButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton deleteButton;                // The delete sac/aset button.
  /**
 * @uml.property  name="closeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton closeButton;                 // The close button.

  /**
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();

  public SacEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, false);  // non-modal

    this.tool = tool;
    this.sslClient = sslClient;

    setTitle("Subject Activation Constraints");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

    // Start building the GUI.
    JPanel sacPane = new JPanel();
    sacPane.setLayout(new GridBagLayout());

    JLabel sacLabel = new JLabel("SA Constraint:");
    sacField = new JTextField(20);
    
    intraSessButton = new JRadioButton("Intrasession");
    intraSessButton.setActionCommand(PM_DENY_INTRA_SESSION);
    intraSessButton.addActionListener(this);
    intraSessButton.setSelected(true);

    acrossSessButton = new JRadioButton("Across sessions");
    acrossSessButton.setActionCommand(PM_DENY_ACROSS_SESSIONS);
    acrossSessButton.addActionListener(this);

    ButtonGroup group = new ButtonGroup();
    group.add(intraSessButton);
    group.add(acrossSessButton);

    JPanel radioButtonPane = new JPanel(new GridLayout(2,1));
    radioButtonPane.add(intraSessButton);
    radioButtonPane.add(acrossSessButton);
    radioButtonPane.setBorder(BorderFactory.createLineBorder(Color.gray));


    JLabel asetLabel = new JLabel("Attribute Sets To Add:");
    asetListModel = new DefaultListModel();
    asetList = new JList(asetListModel);
    JScrollPane asetListScrollPane = new JScrollPane(asetList);
    asetListScrollPane.setPreferredSize(new Dimension(240,100));

    constraints.insets = new Insets(0, 0, 5, 0);
    
    addComp(sacPane, sacLabel, 0, 0, 1, 1);
    addComp(sacPane, sacField, 0, 1, 4, 1);
    addComp(sacPane, radioButtonPane, 0, 2, 3, 3);
    
    constraints.insets = new Insets(0, 10, 5, 0);
    addComp(sacPane, asetLabel, 4, 0, 1, 1);
    addComp(sacPane, asetListScrollPane, 4, 1, 4, 4);
    
    JPanel upperPane = new JPanel();
    upperPane.setLayout(new BorderLayout());
    upperPane.add(sacPane, BorderLayout.CENTER);
    //upperPane.add(buttonPane, BorderLayout.SOUTH);
    upperPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 0, 0),
      BorderFactory.createTitledBorder("New SA Constraint")));

    // The lists pane
    JPanel listPane = new JPanel();
    listPane.setLayout(new GridBagLayout());

    JLabel sacsLabel = new JLabel("SA Constraints:");
    sacListModel = new DefaultListModel();
    sacList = new JList(sacListModel);
    sacList.addListSelectionListener(this);
    JScrollPane sacListScrollPane = new JScrollPane(sacList);
    sacListScrollPane.setPreferredSize(new Dimension(240, 160));

    JLabel aset2Label = new JLabel("Attribute Sets:");
    aset2ListModel = new DefaultListModel();
    aset2List = new JList(aset2ListModel);
    JScrollPane aset2ListScrollPane = new JScrollPane(aset2List);
    aset2ListScrollPane.setPreferredSize(new Dimension(240,160));

    constraints.insets = new Insets(0, 0, 5, 0);
    addComp(listPane, sacsLabel, 0, 0, 1, 1);
    addComp(listPane, sacListScrollPane, 0, 1, 4, 4);

    constraints.insets = new Insets(0, 10, 5, 0);
    addComp(listPane, aset2Label, 4, 0, 1, 1);
    addComp(listPane, aset2ListScrollPane, 4, 1, 4, 4);
    
    JPanel middlePane = new JPanel();
    middlePane.setLayout(new BorderLayout());
    middlePane.add(listPane, BorderLayout.CENTER);
    middlePane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 10, 0),
      BorderFactory.createTitledBorder("Existing SA Constraints")));

    addButton = new JButton("Add");
    addButton.setActionCommand("add");
    addButton.addActionListener(this);

    deleteButton = new JButton("Delete");
    deleteButton.setActionCommand("delete");
    deleteButton.addActionListener(this);
    
    closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);

    JPanel lowerPane = new JPanel();
    lowerPane.add(addButton); 
    lowerPane.add(deleteButton); 
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

  // Returns <name>:<id> of all SACs.
  private Object getSacs() throws Exception {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getSacs");
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

  // This method must be called before making visible this frame.
  // Parameters:
  // sId, sType: if this method is called in order to add an opset to a user
  // attribute or object attribute (called "base node" for the new opset),
  // then sId and sType contain the id and type of the base node. Otherwise
  // (for example if the method is called when you select the "Operation sets..."
  // menu) they are null.
  // sIdToDisplay, sNameToDisplay: the id and name of an opset this method
  // is called on to display. Otherwise null.
  public void prepare() {
    Packet res = null;

    sacField.setText("");
    sacField.requestFocus();

    aset2ListModel.clear();
    if (aset2Vector == null) aset2Vector = new Vector();
    else aset2Vector.clear();
    
    // Get all the attribute sets.
    try {
      Packet cmd = tool.makeCmd("getAsets");
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
    
    asetListModel.clear();
    if (asetVector == null) asetVector = new Vector();
    else asetVector.clear();
    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(asetListModel, pieces[0]);
      asetListModel.add(index, pieces[0]);
      asetVector.add(index, pieces[1]);
    }
    
    // Get the SACs.
    sacListModel.clear();
    if (sacVector == null) sacVector = new Vector();
    else sacVector.clear();
    
    try {
      res = (Packet)getSacs();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception in getSacs(), " + e.getMessage());
      return;
    }
    
    // Each item contains <sac name>:<sac id>:<sac type>.
    for (int i = 0; i < res.size(); i++) {
      String sSac = res.getStringValue(i);
      String[] pieces = sSac.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(sacListModel, pieces[0]);
      sacListModel.add(index, pieces[0]);
      sacVector.add(index, pieces[1]);
    }
  }

  private void close() {
    this.setVisible(false);
  }

  // Delete an attr from an attr set or an entire attr set.
  // If the attr set and one of its attrs are selected, only the selected attr
  // is deleted from that attr set.
  // If only the attr set is selected, the attr set will be deleted.
  private void delete() {
    int sacIndex = sacList.getSelectedIndex();
    if (sacIndex < 0) {
      JOptionPane.showMessageDialog(tool, "Please select a SA Constraint and optionally an attribute set to delete!");
      return;
    }
    String sSacId = (String)sacVector.get(sacIndex);
    
    int asetIndex = aset2List.getSelectedIndex();
    String sAsetId = null;
    String sAsetName = null;
    if (asetIndex >= 0) {
      sAsetId = (String)aset2Vector.get(asetIndex);
      sAsetName = (String)aset2ListModel.get(asetIndex);
    }

    // Send the command and let the server test the other conditions.
    try {
      Packet cmd = tool.makeCmd("deleteSacAndAset", sSacId,
        (sAsetId == null)? "" : sAsetId,
        (sAsetName == null)? "" : sAsetName);
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

    if (asetIndex < 0) {
      sacListModel.removeElementAt(sacIndex);
      sacVector.removeElementAt(sacIndex);
      sacList.clearSelection();
      aset2ListModel.clear();
      aset2Vector.clear();
    } else {
      aset2ListModel.removeElementAt(asetIndex);
      aset2Vector.removeElementAt(asetIndex);
      aset2List.clearSelection();
    }
  }

  // Add 1) a new sac, or 2) add a new sac and an attr set, or
  // 3) add an attr set to an existing sac.
  // If the sac already exists, an attribute set must be selected to be added.
  // If the sac does not exist, you may or may not select an attribute set to be
  // added. The sac will be added together with the selected attribute set,
  // if any.
  // Note that the value selected in the attribute set list that identifies the
  // attribute set to be added has the format: <id>:<name>. Send both of them to
  // the engine.
  // When a new sac is added, its type is also set (intra-session or across sessions).
  // For an existing sac, its type is ignored.
  private void addSac() {
    String sSac = sacField.getText().trim();
    if (sSac.length() == 0) {
      JOptionPane.showMessageDialog(tool, "Please enter a SAC name!");
      sacField.requestFocus();
      return;
    }

    String sType = PM_DENY_INTRA_SESSION;
    if (acrossSessButton.isSelected()) sType = PM_DENY_ACROSS_SESSIONS;
    
    int index = asetList.getSelectedIndex();
    String sAsetId = null;
    String sAsetName = null;
    if (index >= 0) {
      sAsetName = (String)asetListModel.get(index);
      sAsetId = (String)asetVector.get(index);
      asetList.setSelectedIndex(-1);
    }

    sacField.requestFocus();

    // Send the command and let the server test the other conditions.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("addSacAndAset", sSac, sType,
        (sAsetId == null)? "" : sAsetId,
        (sAsetName == null)? "" : sAsetName);
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

    // The result contains <name>:<id> of the sac.
    // Adding a new sac triggers a valueChanged event, that could lead
    // to strange things happening. So we clear the selection first.
    sacList.clearSelection();
    String sNew = res.getStringValue(0);
    String[] pieces = sNew.split(PmAdmin.PM_FIELD_DELIM);
    if (!sacListModel.contains(pieces[0])) {
      index = PmAdmin.getIndex(sacListModel, pieces[0]);
      sacListModel.add(index, pieces[0]);
      sacVector.add(index, pieces[1]);
      //opsetList.setSelectedIndex(index);
      sacList.ensureIndexIsVisible(index);
    }
    selectSac(pieces[1]);
  }

  // Select the sac, but first unselect it to trigger a valueChanged() call.
  private void selectSac(String sSacId) {
    int index = sacVector.indexOf(sSacId);
    if (index < 0) {
      JOptionPane.showMessageDialog(tool, "No such SAC to display!");
      return;
    }
    sacList.clearSelection();
    sacList.setSelectedIndex(index);
  }
  
  private void intraButtonWasSelected() {
  }
  
  private void acrossButtonWasSelected() {
  }
  
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("add")) {
      addSac();
    } else if (e.getActionCommand().equalsIgnoreCase("refresh")) {
      prepare();
    } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
      delete();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      close();
    } else if (e.getActionCommand().equalsIgnoreCase(PM_DENY_INTRA_SESSION)) {
      intraButtonWasSelected();
    } else if (e.getActionCommand().equalsIgnoreCase(PM_DENY_ACROSS_SESSIONS)) {
      acrossButtonWasSelected();
    }
  }

  // The SAC selected in the sac list has changed (change is forced even
  // when the sac already was there but we added some attribute set to it).
  // Display all information about it.
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;

    // Get the selected value, which can be null.
    int index = sacList.getSelectedIndex();
    if (index < 0) return;
    String sSacName = (String)sacListModel.get(index);
    String sSacId = (String)sacVector.get(index);
    sacField.setText(sSacName);

    // Get all information about this sac.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getSacInfo", sSacId);
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
      throw new RuntimeException(exc.getMessage());
    }
    
    // Item 0 contains: <sac name>:<sac id>:<sac type>.
    // Items 1,... contain each an attribute set (<aset name>:<aset id>).
    String sLine = res.getStringValue(0);
    String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
    if (pieces[2].equalsIgnoreCase(PM_DENY_INTRA_SESSION)) intraSessButton.setSelected(true);
    else acrossSessButton.setSelected(true);

    aset2ListModel.clear();
    if (aset2Vector == null) aset2Vector = new Vector();
    else aset2Vector.clear();
    for (int i = 1; i < res.size(); i++) {
      sLine = res.getStringValue(i);
      pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      index = PmAdmin.getIndex(aset2ListModel, pieces[0]);
      aset2ListModel.add(index, pieces[0]);
      aset2Vector.add(index, pieces[1]);
    }
  }
}

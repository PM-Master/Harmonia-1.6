package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
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
public class ScriptMgr extends JDialog implements ActionListener {
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
 * @uml.property  name="ruleEditor"
 * @uml.associationEnd  
 */
private RuleEditor ruleEditor;
  
  /**
 * @uml.property  name="enabledField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField enabledField;
  
  /**
 * @uml.property  name="scriptListModel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private DefaultListModel scriptListModel;      // Model and list of scripts.
  /**
 * @uml.property  name="scriptList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList scriptList;
  /**
 * @uml.property  name="scriptVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector scriptVector;
  
  /**
 * @uml.property  name="enableButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton enableButton;
  /**
 * @uml.property  name="disableButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton disableButton;
  /**
 * @uml.property  name="displayButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton displayButton;
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
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();

  public ScriptMgr(PmAdmin tool, SSLSocketClient sslClient, RuleEditor ruleEditor) {
    super(tool, true);  // modal

    this.tool = tool;
    this.sslClient = sslClient;
    this.ruleEditor = ruleEditor;

    setTitle("EVER Scripts");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

    // Start building the GUI.
    JPanel scriptPane = new JPanel();
    scriptPane.setLayout(new GridBagLayout());

    JLabel enabledLabel = new JLabel("Enabled Script:");
    enabledField = new JTextField(20);

    JLabel scriptLabel = new JLabel("EVER Scripts:");
    scriptListModel = new DefaultListModel();
    scriptList = new JList(scriptListModel);
    JScrollPane scriptListScrollPane = new JScrollPane(scriptList);
    scriptListScrollPane.setPreferredSize(new Dimension(240,100));

    constraints.insets = new Insets(0, 0, 5, 0);
    
    addComp(scriptPane, enabledLabel, 0, 0, 1, 1);
    addComp(scriptPane, enabledField, 0, 1, 4, 1);
    
    constraints.insets = new Insets(0, 10, 5, 0);
    addComp(scriptPane, scriptLabel, 4, 0, 1, 1);
    addComp(scriptPane, scriptListScrollPane, 4, 1, 4, 4);

    JPanel upperPane = new JPanel();
    upperPane.setLayout(new BorderLayout());
    upperPane.add(scriptPane, BorderLayout.CENTER);
    upperPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 0, 0),
      BorderFactory.createTitledBorder("Existing Scripts")));

    enableButton = new JButton("Enable");
    enableButton.setActionCommand("enable");
    enableButton.addActionListener(this);

    disableButton = new JButton("Disable");
    disableButton.setActionCommand("disable");
    disableButton.addActionListener(this);

    displayButton = new JButton("Display");
    displayButton.setActionCommand("display");
    displayButton.addActionListener(this);

    deleteButton = new JButton("Delete");
    deleteButton.setActionCommand("delete");
    deleteButton.addActionListener(this);

    closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);

    JPanel lowerPane = new JPanel();
    lowerPane.add(enableButton);
    lowerPane.add(disableButton);
    lowerPane.add(displayButton);
    lowerPane.add(deleteButton);
    lowerPane.add(closeButton);

    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(upperPane, BorderLayout.NORTH);
    contentPane.add(lowerPane, BorderLayout.SOUTH);

    setContentPane(contentPane);
    getRootPane().setDefaultButton(enableButton);
  }

  private void addComp(Container container, Component component, int x, int y, int w, int h) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = w;
    constraints.gridheight = h;
    container.add(component, constraints);
  }

  // Returns <name>:<id> in each item of the returned ArrayList.
  private Object getScripts() throws Exception {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getScripts");
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
  
  // Returns <name>:<id> of the enabled script.
  private Object getEnabledScript() throws Exception {
    Packet res = null;

    try {
      Packet cmd = tool.makeCmd("getEnabledScript");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        throw new RuntimeException("Undetermined error");
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
    return res;
  }

  public void prepare() {
    Packet res = null;

    enabledField.setText("");

    scriptListModel.clear();
    if (scriptVector == null) scriptVector = new Vector();
    else scriptVector.clear();
    
    // Get all scripts
    try {
      res = (Packet)getScripts();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception in getScripts(), " + e.getMessage());
      return;
    }
    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(scriptListModel, pieces[0]);
      scriptListModel.add(index, pieces[0]);
      scriptVector.add(index, pieces[1]);
    }

    try {
      res = (Packet)getEnabledScript();
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(tool, "Exception in getEnabledScript(), " + e.getMessage());
      return;
    }
    if (res.size() >= 1) {
      String sLine = res.getStringValue(0);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      enabledField.setText(pieces[0]);
    }
  }
  
  private void close() {
    this.setVisible(false);
  }
  
  private void displayScript() {
    int scriptIndex = scriptList.getSelectedIndex();
    if (scriptIndex < 0) {
      JOptionPane.showMessageDialog(this, "Please select a script to display!");
      return;
    }
    String sScriptId = (String)scriptVector.get(scriptIndex);
    try {
      Packet cmd = tool.makeCmd("getSourceScript", sScriptId);    
      Packet res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Engine returned a null result!");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
      ruleEditor.doInputFromPacket(res);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception while deleting script: " +
        e.getMessage());
    }
  }
  
  private void deleteScript() {
    int scriptIndex = scriptList.getSelectedIndex();
    if (scriptIndex < 0) {
      JOptionPane.showMessageDialog(this, "Please select a script to delete!");
      return;
    }
    String sScriptId = (String)scriptVector.get(scriptIndex);
        
    try {
      Packet cmd = tool.makeCmd("deleteScript", sScriptId);
      Packet res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Engine returned a null result!");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
      scriptListModel.removeElementAt(scriptIndex);
      scriptVector.removeElementAt(scriptIndex);
      scriptList.clearSelection();
      return;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception while deleting script: " +
        e.getMessage());
      return;
    }
  }

  private void enableScript() {
    int scriptIndex = scriptList.getSelectedIndex();
    if (scriptIndex < 0) {
      JOptionPane.showMessageDialog(this, "Please select a script to enable!");
      return;
    }
    String sScriptId = (String)scriptVector.get(scriptIndex);
        
    try {
      Packet cmd = tool.makeCmd("enableScript", sScriptId);
      Packet res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Engine returned a null result!");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
      enabledField.setText((String)scriptList.getSelectedValue());
      return;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception while enabling script: " +
        e.getMessage());
      return;
    }
  }

  private void disableScript() {
    String sName = enabledField.getText();
    if (sName == null || sName.length() == 0) {
      JOptionPane.showMessageDialog(this, "No script is enabled!");
      return;
    }      
        
    try {
      Packet cmd = tool.makeCmd("disableEnabledScript");
      Packet res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Engine returned a null result!");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
      enabledField.setText(null);
      return;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception while disabling script: " +
        e.getMessage());
      return;
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("enable")) {
      enableScript();
    } else if (e.getActionCommand().equalsIgnoreCase("disable")) {
      disableScript();
    } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
      deleteScript();
    } else if (e.getActionCommand().equalsIgnoreCase("display")) {
      displayScript();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      close();
    }
  }
}
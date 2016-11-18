/*
 * PermEditor.java
 *
 * Created on February 28, 2006
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
public class PermEditor extends JDialog implements ActionListener, ListSelectionListener {
  
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

  // The entity on which the user requested permissions.
  /**
 * @uml.property  name="sEntityName"
 */
private String sEntityName;
  /**
 * @uml.property  name="sEntityId"
 */
private String sEntityId;
  /**
 * @uml.property  name="sEntityType"
 */
private String sEntityType;
  
  // UI components.
  /**
 * @uml.property  name="uattrLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel uattrLabel;
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
 * @uml.property  name="uattrVector"
 */
private Vector uattrVector;
  
  /**
 * @uml.property  name="opsetLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel opsetLabel;
  /**
 * @uml.property  name="opsetListModel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private DefaultListModel opsetListModel;
  /**
 * @uml.property  name="opsetList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList opsetList;

  /**
 * @uml.property  name="oattrLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel oattrLabel;
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
 * @uml.property  name="allPermLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel allPermLabel;
  /**
 * @uml.property  name="allPermListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel allPermListModel;
  /**
 * @uml.property  name="allPermList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList allPermList;
  
  /**
 * @uml.property  name="selPermLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel selPermLabel;
  /**
 * @uml.property  name="selPermListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel selPermListModel;
  /**
 * @uml.property  name="selPermList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList selPermList;
  /**
 * @uml.property  name="selPermVector"
 */
private Vector selPermVector;

  /**
 * @uml.property  name="subgraphBox"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JCheckBox subgraphBox;
  
  /**
 * @uml.property  name="setButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton setButton;
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

  //JList oaList;
  //JList pcList;
  
  public PermEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);

    this.tool = tool;
    this.sslClient = sslClient;
    setTitle("Set Permissions");
    this.setResizable(false);
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        doClose();
      }
    });

    // Start building the GUI
    // First the user attributes list and panel.
    uattrLabel = new JLabel("User attributes:");
    uattrListModel = new DefaultListModel();
    uattrList = new JList(uattrListModel);
    uattrList.addListSelectionListener(this);
    JScrollPane uattrListScrollPane = new JScrollPane(uattrList);
    uattrListScrollPane.setPreferredSize(new Dimension(200, 200));

    JPanel uattrPane = new JPanel();
    uattrPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(uattrPane, uattrLabel, 0, 0, 1, 1);
    addComp(uattrPane, uattrListScrollPane, 0, 1, 3, 1);
    uattrPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The opsets list and panel.
    opsetLabel = new JLabel("Operation sets:");
    opsetListModel = new DefaultListModel();
    opsetList = new JList(opsetListModel);
    opsetList.addListSelectionListener(this);
    JScrollPane opsetListScrollPane = new JScrollPane(opsetList);
    opsetListScrollPane.setPreferredSize(new Dimension(200, 85));

    JPanel opsetPane = new JPanel();
    opsetPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(opsetPane, opsetLabel, 0, 0, 1, 1);
    addComp(opsetPane, opsetListScrollPane, 0, 1, 3, 1);
    opsetPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    // The object attributes list and panel.
    oattrLabel = new JLabel("Object attributes:");
    oattrListModel = new DefaultListModel();
    oattrList = new JList(oattrListModel);
    oattrList.addListSelectionListener(this);
    JScrollPane oattrListScrollPane = new JScrollPane(oattrList);
    oattrListScrollPane.setPreferredSize(new Dimension(200, 85));

    JPanel oattrPane = new JPanel();
    oattrPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(oattrPane, oattrLabel, 0, 0, 1, 1);
    addComp(oattrPane, oattrListScrollPane, 0, 1, 3, 1);
    oattrPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    // The panel with opsets and oattrs.
    JPanel oPane = new JPanel();
    oPane.setLayout(new GridLayout(2, 1));
    oPane.add(opsetPane);
    oPane.add(oattrPane);
    
    // The selected permissions list and panel.
    selPermLabel = new JLabel("Selected permissions:");
    selPermListModel = new DefaultListModel();
    selPermList = new JList(selPermListModel);
    selPermList.addListSelectionListener(this);
    JScrollPane selPermListScrollPane = new JScrollPane(selPermList);
    selPermListScrollPane.setPreferredSize(new Dimension(200, 200));

    JPanel selPermPane = new JPanel();
    selPermPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(selPermPane, selPermLabel, 0, 0, 1, 1);
    addComp(selPermPane, selPermListScrollPane, 0, 1, 3, 1);
    selPermPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    // The all permissions list and panel.
    allPermLabel = new JLabel("All permissions:");
    allPermListModel = new DefaultListModel();
    allPermList = new JList(allPermListModel);
    allPermList.addListSelectionListener(this);
    JScrollPane allPermListScrollPane = new JScrollPane(allPermList);
    allPermListScrollPane.setPreferredSize(new Dimension(200, 200));

    JPanel allPermPane = new JPanel();
    allPermPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(allPermPane, allPermLabel, 0, 0, 1, 1);
    addComp(allPermPane, allPermListScrollPane, 0, 1, 3, 1);
    allPermPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel listPane = new JPanel();
    listPane.setLayout(new GridLayout(1, 4));
    listPane.add(uattrPane);
    listPane.add(oPane);
    listPane.add(selPermPane);
    listPane.add(allPermPane);

    // Buttons.
    subgraphBox = new JCheckBox("All Ascendants?");
    
    setButton = new JButton("Set");
    setButton.addActionListener(this);
    setButton.setActionCommand("set");

    resetButton = new JButton("Reset");
    resetButton.addActionListener(this);
    resetButton.setActionCommand("reset");

    closeButton = new JButton("Close");
    closeButton.addActionListener(this);
    closeButton.setActionCommand("close");
    
    JPanel buttonPane = new JPanel();
    buttonPane.add(subgraphBox);
    buttonPane.add(setButton);
    buttonPane.add(resetButton);
    buttonPane.add(closeButton);
    buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The content pane.
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(listPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    setContentPane(contentPane);
  }
  
  public void prepare(String sName, String sId, String sType) {
    // Save the entity on which we want to set permissions.
    sEntityName = sName;
    sEntityId = sId;
    sEntityType = sType;
    
    // Set the title right.
    String sEntityTypeName = getEntityTypeName(sEntityType);
    setTitle("Set permissions on " + sEntityTypeName + " \"" + sEntityName + "\"");

    // Clear lists and vectors.
    uattrListModel.clear();
    if (uattrVector == null) uattrVector = new Vector();
    else uattrVector.clear();
    selPermListModel.clear();
    allPermListModel.clear();
    
    // Fill the user attribute list.
    Packet packet = getUserAttributes();
    if (packet != null) for (int i = 0; i < packet.size(); i++) {
      String sLine = packet.getStringValue(i);
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(uattrListModel, pieces[0]);
      uattrListModel.add(index, pieces[0]);
      uattrVector.add(index, pieces[1]);
    }
    
    // Fill the All Permissions list and vector.
    packet = getAllOps();
    if (packet != null) for (int i = 0; i < packet.size(); i++) {
      String sLine = packet.getStringValue(i);
      if (allPermListModel.contains(sLine)) continue;
      int index = PmAdmin.getIndex(allPermListModel, sLine);
      allPermListModel.add(index, sLine);
    }
  }

  private Packet getAllOps() {
    try {
      Packet cmd = tool.makeCmd("getAllOps");
      Packet res = sslClient.sendReceive(cmd, null);
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error in getAllOps: " + res.getErrorMessage());
        return null;
      }
      return res;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Exception in getAllOps: " + e.getMessage());
      return null;
    }
  }

  private Packet getUserAttributes() {
    try {
      Packet cmd = tool.makeCmd("getUserAttributes");
      Packet res = sslClient.sendReceive(cmd, null);
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error in getUserAttributes: " + res.getErrorMessage());
        return null;
      }
      return res;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Exception in getUserAttributes: " + e.getMessage());
      return null;
    }
  }
  
  private Packet getOattrs() {
    try {
      Packet cmd = tool.makeCmd("getOattrs");
      Packet res = sslClient.sendReceive(cmd, null);
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error in getOattrs: " + res.getErrorMessage());
        return null;
      }
      return res;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Exception in getOattrs: " + e.getMessage());
      return null;
    }
  }

  private Packet getOpsetsBetween(String sUattrName, String sEntityName,
  String sEntityType) {
    try {
      Packet cmd = tool.makeCmd("getOpsetsBetween", sUattrName, sEntityName,
        sEntityType);
      Packet res = sslClient.sendReceive(cmd, null);
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error in getOpsetsBetween: " + res.getErrorMessage());
        return null;
      }
      return res;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Exception in getOpsetsBetween: " + e.getMessage());
      return null;
    }
  }

  private Packet getOpsetOattrs(String sOpsetName) {
    try {
      Packet cmd = tool.makeCmd("getOpsetOattrs", sOpsetName);
      Packet res = sslClient.sendReceive(cmd, null);
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error in getOpsetOattrs: " + res.getErrorMessage());
        return null;
      }
      return res;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Exception in getOpsetOattrs: " + e.getMessage());
      return null;
    }
  }

  private Packet getOpsetOps(String sOpsetName) {
    try {
      Packet cmd = tool.makeCmd("getOpsetOps", sOpsetName);
      Packet res = sslClient.sendReceive(cmd, null);
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error in getOpsetOps: " + res.getErrorMessage());
        return null;
      }
      return res;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Exception in getOpsetOps: " + e.getMessage());
      return null;
    }
  }

  private Packet getPolicyClasses() {
    try {
      Packet cmd = tool.makeCmd("getPolicyClasses");
      Packet res = sslClient.sendReceive(cmd, null);
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error in getPolicyClasses: " + res.getErrorMessage());
        return null;
      }
      return res;
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(null, "Exception in getPolicyClasses: " + e.getMessage());
      return null;
    }
  }

  private String getEntityTypeName(String sType) {
    if (sType.equalsIgnoreCase(PmAdmin.PM_NODE_USER)) return "user";
    else if (sType.equalsIgnoreCase(PmAdmin.PM_NODE_UATTR)) return "user attribute";
    else if (sType.equalsIgnoreCase(PmAdmin.PM_NODE_OATTR)) return "object attribute";
    else if (sType.equalsIgnoreCase(PmAdmin.PM_NODE_ASSOC)) return "object";
    else if (sType.equalsIgnoreCase(PmAdmin.PM_NODE_POL)) return "policy";
    else if (sType.equalsIgnoreCase(PmAdmin.PM_NODE_CONN)) return "connector";
    else return "unknown";
  }
  
  private void addComp(Container container, Component component, int x, int y, int w, int h) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = w;
    constraints.gridheight = h;
    container.add(component, constraints);
  }

  private void doAdd() {
  }
  
  private void doClose() {
    this.setVisible(false);
  }
  
  private void doReset() {
    uattrList.clearSelection();
    opsetListModel.clear();
    oattrListModel.clear();
    selPermListModel.clear();
  }
  
  private void doSet() {
    String sUattr = (String)uattrList.getSelectedValue();
    if (sUattr == null) {
      JOptionPane.showMessageDialog(tool, "Please select the grantee (a user attribute)!");
      return;
    }
    
    System.out.println("Setting permissions for uattr: " + sUattr);
    System.out.println("on entity " + sEntityName + " of type " + sEntityType);

    String sOpset = (String)opsetList.getSelectedValue();
    System.out.println("Opset being edited: " + sOpset);
    
    String sOattr = (String)oattrList.getSelectedValue();
    System.out.println("Real oattr: " + sOattr);
    
    System.out.println("Grant " + sUattr + " permissions: {");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < selPermListModel.size(); i++) {
      if (i == 0) {
        sb.append((String)selPermListModel.get(i));
        System.out.print((String)selPermListModel.get(i));    
      } else {
        sb.append("," + (String)selPermListModel.get(i));
        System.out.print("," + (String)selPermListModel.get(i));
      }
    }
    System.out.println("}");
    
    JLabel pcLabel = new JLabel("Policy classes:");
    DefaultListModel pcListModel = new DefaultListModel();
    JList pcList = new JList(pcListModel);
    JScrollPane pcListScrollPane = new JScrollPane(pcList);
    pcListScrollPane.setPreferredSize(new Dimension(200, 200));

    JPanel pcPane = new JPanel();
    pcPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(pcPane, pcLabel, 0, 0, 1, 1);
    addComp(pcPane, pcListScrollPane, 0, 1, 3, 1);
    pcPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    Packet packet = getPolicyClasses();
    if (packet != null) for (int i = 0; i < packet.size(); i++) {
      String sLine = packet.getStringValue(i);
      // The returned line contains <pc name>:<pc id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(pcListModel, pieces[0]);
      pcListModel.add(index, pieces[0]);
    }

    JLabel oaLabel = new JLabel("Object attributes:");
    DefaultListModel oaListModel = new DefaultListModel();
    JList oaList = new JList(oaListModel);
    JScrollPane oaListScrollPane = new JScrollPane(oaList);
    oaListScrollPane.setPreferredSize(new Dimension(200, 200));

    JPanel oaPane = new JPanel();
    oaPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(oaPane, oaLabel, 0, 0, 1, 1);
    addComp(oaPane, oaListScrollPane, 0, 1, 3, 1);
    oaPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    packet = getOattrs();
    if (packet != null) for (int i = 0; i < packet.size(); i++) {
      String sLine = packet.getStringValue(i);
      // The returned line contains <oattr name>:<oattr id>.
      String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
      int index = PmAdmin.getIndex(oaListModel, pieces[0]);
      oaListModel.add(index, pieces[0]);
    }

    JPanel contPane = new JPanel();
    contPane.add(pcPane);
    contPane.add(oaPane);
    
    String message = "You may want to suggest a base:";
    int ret = JOptionPane.showOptionDialog(null,
              new Object[] {message, contPane},
              "Suggested base", JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE, null, null, null);
    if (ret != JOptionPane.OK_OPTION) return;
    // Get user name and password.
    String sSelPc = (String)pcList.getSelectedValue();
    String sSelOa = (String)oaList.getSelectedValue();
    if (sSelPc != null && sSelOa != null) {
      JOptionPane.showMessageDialog(this, "You cannot select both a policy and an object attribute as suggested container!");
      return;
    }
    String sBase = null;
    String sBaseType = null;
    
    if (sSelPc != null) {
      sBase = sSelPc;
      sBaseType = PmAdmin.PM_NODE_POL;
    } else if (sSelOa != null) {
      sBase = sSelOa;
      sBaseType = PmAdmin.PM_NODE_OATTR;
    }

    try {
      // Prepare the command setPerms. The makeCmd() method
      // tests each argument and, if null, replaces it with
      // the empty string.
      // The second argument (null) is the process id.
      // It should be a real one!!!
      Packet cmd = tool.makeCmd("setPerms", null, sUattr,
        sOpset, sOattr, sBase, sBaseType,
        sb.toString(), sEntityName, sEntityType,
        subgraphBox.isSelected()? "yes" : "no");
      Packet res = sslClient.sendReceive(cmd, null);
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, "Error in setPerms: " + res.getErrorMessage());
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception in setPerms: " + e.getMessage());
      return;
    }
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("set")) {
      doSet();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      doClose();
    } else if (e.getActionCommand().equalsIgnoreCase("reset")) {
      doReset();
    }

  }

  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    
    Object src = e.getSource();
    if (src == allPermList) {
      String sPerm = (String)allPermList.getSelectedValue();
      if (sPerm != null) {
        if (selPermListModel.contains(sPerm)) {
          JOptionPane.showMessageDialog(this, "Operation is already selected!");
          return;
        }
        int index = PmAdmin.getIndex(selPermListModel, sPerm);
        selPermListModel.add(index, sPerm);
      }
      return;
    } else if (src == selPermList) {
      String sPerm = (String)selPermList.getSelectedValue();
      if (sPerm != null) {
        selPermListModel.removeElement(sPerm);
      }
      return;
    } else if (src == uattrList) {
      String sUattrName = (String)uattrList.getSelectedValue();
      if (sUattrName == null) return;
      
      // Clear the opset and oattr lists.
      opsetListModel.clear();
      oattrListModel.clear();
      
      // List all operation sets between the selected user attribute and the
      // object attribute <sEntityName, sEntityId, sEntityType>.
      Packet opsets = getOpsetsBetween(sUattrName, sEntityName,
        sEntityType);
      if (opsets != null) for (int i = 0; i < opsets.size(); i++) {
        String sLine = opsets.getStringValue(i);
        String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
        int index = PmAdmin.getIndex(opsetListModel, pieces[0]);
        opsetListModel.add(index, pieces[0]);
      }
    } else if (src == opsetList) {
      String sOpsetName = (String)opsetList.getSelectedValue();
      if (sOpsetName == null) return;
      
      // Clear the oattr list and selected permissions list.
      oattrListModel.clear();
      selPermListModel.clear();
      
      // Find the object attributes this opset is assigned to.
      Packet oattrs = getOpsetOattrs(sOpsetName);
      if (oattrs != null) for (int i = 0; i < oattrs.size(); i++) {
        String sLine = oattrs.getStringValue(i);
        String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
        int index = PmAdmin.getIndex(oattrListModel, pieces[0]);
        oattrListModel.add(index, pieces[0]);
      }
      
      // Find the opset's operations.
      Packet ops = getOpsetOps(sOpsetName);
      if (ops != null) for (int i = 0; i < ops.size(); i++) {
        String sLine = ops.getStringValue(i);
        int index = PmAdmin.getIndex(selPermListModel, sLine);
        selPermListModel.add(index, sLine);
      }
    } else if (src == oattrList) {
    }
  }
}

package gov.nist.csd.pm.application.medrec;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:01 $
 * @since 1.5
 */
public class MRSelector extends JDialog implements ActionListener {

  public static final String PM_FAILURE     = "err ";
  public static final String PM_SUCCESS     = "ok  ";
  
  public static final String PM_CMD         = "cmd ";
  public static final String PM_EOC         = "eoc ";
  
  public static final String PM_ARG         = "arg ";
  public static final String PM_SEP         = "sep ";
  
  public static final String PM_DATA        = "data";
  public static final String PM_EOD         = "eod ";
  
  public static final String PM_BYE         = "bye ";
  
  public static final String PM_FIELD_DELIM = ":";
  public static final String PM_TERMINATOR  = ".";
  public static final String PM_ALT_FIELD_DELIM = "|";
  public static final String PM_ALT_DELIM_PATTERN = "\\|";

  /**
 * @uml.property  name="tplBox"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JComboBox tplBox;
  /**
 * @uml.property  name="tplVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector tplVector;
  
  /**
 * @uml.property  name="keyBox"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JComboBox keyBox;
  /**
 * @uml.property  name="keyValueField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField keyValueField;
  /**
 * @uml.property  name="keyMap"
 * @uml.associationEnd  qualifier="sKeyName:java.lang.String java.lang.String"
 */
private HashMap keyMap;

  /**
 * @uml.property  name="objListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel objListModel;
  /**
 * @uml.property  name="objList"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JList objList;
  /**
 * @uml.property  name="objVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector objVector;

  /**
 * @uml.property  name="searchButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton searchButton;
  /**
 * @uml.property  name="okButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton okButton;
  /**
 * @uml.property  name="cancelButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton cancelButton;
  
  /**
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();

  /**
 * @uml.property  name="sSessId"
 */
String sSessId;
  /**
 * @uml.property  name="sysCaller"
 * @uml.associationEnd  
 */
SysCaller sysCaller;
  /**
 * @uml.property  name="mrEditor"
 * @uml.associationEnd  
 */
MREditor mrEditor;

  public MRSelector(MREditor mrEditor, SysCaller sysCaller, String sSessId) {
    super(mrEditor, true);
    setTitle("Medical record selector");

    this.mrEditor = mrEditor;
    this.sSessId = sSessId;
    this.sysCaller = sysCaller;

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

    // Start building the GUI
    JLabel tplLabel = new JLabel("Template:");
    tplBox = new JComboBox();
    //tplBox.setMinimumSize(new Dimension(200, 20));
    tplBox.setPreferredSize(new Dimension(200, 20));    
    tplBox.setActionCommand("tpl");
    tplBox.addActionListener(this);

    JLabel keyLabel = new JLabel("Key:         ");
    keyBox = new JComboBox();
    //keyBox.setMinimumSize(new Dimension(200, 20));
    keyBox.setPreferredSize(new Dimension(200, 20));
    keyBox.setActionCommand("key");
    keyBox.addActionListener(this);
    
    JLabel keyValueLabel = new JLabel("Value:       ");
    keyValueField = new JTextField(18);

  
  
  
  
  
  
  
    JPanel tplPane = new JPanel(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(tplPane, tplBox, 0, 0, 3, 1);
    tplPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 0, 0),
      BorderFactory.createTitledBorder("Template")));
    
    JPanel keyPane = new JPanel(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(keyPane, keyLabel, 0, 0, 1, 1);
    addComp(keyPane, keyBox, 0, 1, 3, 1);
    constraints.insets = new Insets(10, 0, 0, 0);
    addComp(keyPane, keyValueLabel, 0, 2, 1, 1);
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(keyPane, keyValueField, 0, 3, 2, 1);
    keyPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 0, 0),
      BorderFactory.createTitledBorder("Keys")));
    
    JLabel objLabel = new JLabel("Objects:");
    objListModel = new DefaultListModel();
    objList = new JList(objListModel);
    JScrollPane objListScrollPane = new JScrollPane(objList);
    objListScrollPane.setPreferredSize(new Dimension(200, 150));

    JPanel objPane = new JPanel();
    objPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(objPane, objLabel, 0, 0, 1, 1);
    addComp(objPane, objListScrollPane, 0, 1, 3, 1);
    objPane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(10, 0, 0, 0),
        BorderFactory.createTitledBorder("Search results:")));

    
    Container vertBox = Box.createVerticalBox();
    vertBox.add(tplPane);
    vertBox.add(keyPane);
    vertBox.add(objPane);
    
    searchButton = new JButton("Search");
    searchButton.setActionCommand("search");
    searchButton.addActionListener(this);

    okButton = new JButton("OK");
    okButton.setActionCommand("ok");
    okButton.addActionListener(this);
    
    cancelButton = new JButton("Cancel");
    cancelButton.setActionCommand("cancel");
    cancelButton.addActionListener(this);
    
    JPanel buttonPane = new JPanel();
    buttonPane.add(searchButton);
    buttonPane.add(okButton);
    buttonPane.add(cancelButton);
    buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    
    JPanel thePane = new JPanel();
    thePane.setLayout(new BorderLayout());
    thePane.add(vertBox, BorderLayout.CENTER);
    thePane.add(buttonPane, BorderLayout.SOUTH);
    
    setContentPane(thePane);
  }

  private void addComp(Container container, Component component, int x, int y, int w, int h) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = w;
    constraints.gridheight = h;
    container.add(component, constraints);
  }
  
  public void prepare() {
    System.out.println("MRSelector prepare() called");
    if (tplVector == null) tplVector = new Vector();
    else tplVector.clear();

    objListModel.clear();
    if (objVector == null) objVector = new Vector();
    else objVector.removeAllElements();
    
    Packet res = (Packet)sysCaller.getTemplates();
    if (res == null) {
      JOptionPane.showMessageDialog(this, "Null result from getTemplates!");
      return;
    }
    if (res.hasError()) {
      JOptionPane.showMessageDialog(this, res.getErrorMessage());
      return;
    }
    DefaultComboBoxModel tplBoxModel = (DefaultComboBoxModel)tplBox.getModel();
    tplBoxModel.removeAllElements();
    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PM_FIELD_DELIM);
      
      int index = getIndex(tplBox, pieces[0]);
      tplBoxModel.insertElementAt(pieces[0], index);
      tplVector.insertElementAt(pieces[1], index);
    }
   
    if (keyMap == null) keyMap = new HashMap();
    else keyMap.clear();
    DefaultComboBoxModel keyBoxModel = (DefaultComboBoxModel)keyBox.getModel();
    keyBoxModel.removeAllElements();
    keyValueField.setText(null);
  }
  
  // Called when the user clicks the Close button.
  private void close() {
    setVisible(false);
  }

  private void printMap(HashMap map, String sCaption) {
    System.out.println(sCaption);
    Iterator mapiter = map.keySet().iterator();
    while (mapiter.hasNext()) {
      String sKey = (String)mapiter.next();
      String sValue = (String)map.get(sKey);
      System.out.println(sKey + " ---> " + sValue);
    }
  }

  // Called when the user selects a template from the template box.
  private void tpl() {
    System.out.println("tpl() invoked, as if the user selected a template");
    int nSelIx = tplBox.getSelectedIndex();
    if (nSelIx < 0) return;
    
    System.out.println("User selected template " + (String)tplBox.getSelectedItem() +
      ", " + (String)tplVector.get(tplBox.getSelectedIndex()));
    
    // Get the template's components.
    Packet res = (Packet)sysCaller.getTemplateInfo((String)tplVector.get(tplBox.getSelectedIndex()));
    if (res == null) {
      JOptionPane.showMessageDialog(this, "Null result from getTemplateInfo!");
      return;      
    }
    if (res.hasError()) {
      JOptionPane.showMessageDialog(this, res.getErrorMessage());
      return;      
    }
    if (res.size() < 3) {
      JOptionPane.showMessageDialog(this, "Bad template!");
      return;      
    }
        
    // Insert the keys.
    DefaultComboBoxModel keyModel = (DefaultComboBoxModel)keyBox.getModel();
    keyModel.removeAllElements();
    keyMap.clear();
    
    String sLine = res.getStringValue(2);
    if (sLine.length() == 0) return;
    String[] pieces = sLine.split(PM_FIELD_DELIM);
    for (int i = 0; i < pieces.length; i++) {
      keyModel.insertElementAt(pieces[i], i);
      keyMap.put(pieces[i], null);
    }
  }

  // Called when the user selects a key from the key box.
  private void key() {
    int nKeyIx = keyBox.getSelectedIndex();
    System.out.println("Handler for the keyBox activated, selected index = " + nKeyIx);
    if (nKeyIx < 0) {
      return;
    }    
    String sKeyName = (String)keyBox.getItemAt(nKeyIx);

    // Select the key value that corresponds to the selected key.
    String sKeyValue = (String)keyMap.get(sKeyName);
    System.out.println("Key " + sKeyName + " ---> " + sKeyValue);
    keyValueField.setText(sKeyValue);
  }

  // Possible queries:
  // 1. Find all composite objects.
  // 2. Find all composite objects with template x.
  // 3. Find all composite objects with template x and key k=v.
  private void search() {
    int nIx = tplBox.getSelectedIndex();
    String sTplId = null;
    if (nIx >= 0) sTplId = (String)tplVector.get(nIx);
    
    String sKey = null;
    String sKeyName = null;
    String sKeyValue = null;

    nIx = keyBox.getSelectedIndex();
    if (nIx >= 0) {
      sKeyName = (String)keyBox.getSelectedItem();
      sKeyValue = keyValueField.getText().trim();
      if (sKeyValue.length() > 0) sKey = sKeyName + "=" + sKeyValue;
    }
    
    Packet res = (Packet)sysCaller.getRecords(sTplId, sKey);
    if (res == null) {
      JOptionPane.showMessageDialog(this, "Null result from getRecords!");
      return;
    }
    if (res.hasError()) {
      JOptionPane.showMessageDialog(this, res.getErrorMessage());
      return;
    }
    
    objListModel.clear();
    if (objVector == null) objVector = new Vector();
    else objVector.removeAllElements();
    
    for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PM_FIELD_DELIM);
      int index = getIndex(objListModel, pieces[0]);
      objListModel.add(index, pieces[0]);
      objVector.add(index, pieces[1]);
    }
  }
  
  // Called when the user presses the OK button.
  private void select() {
    // Display the selected record's components.
    int nSelIx = objList.getSelectedIndex();
    if (nSelIx < 0) {
      JOptionPane.showMessageDialog(this, "Please select a record from the list!");
      return;
    }
      
    String sContName = (String)objListModel.get(nSelIx);
    String sContId = (String)objVector.get(nSelIx);
    System.out.println("You selected the record " + sContName + " of id " + sContId);
    mrEditor.displayRecord(sContName, sContId);
    mrEditor.setMrnNumber(sContName);
    setVisible(false);
  }
  
  // Find and return where to insert a new string in an alphabetically ordered list.
  public static int getIndex(DefaultListModel model, String target) {
    int high = model.size(), low = -1, probe;
    while (high - low > 1) {
      probe = (high + low) / 2;
      if (target.compareToIgnoreCase((String)model.get(probe)) < 0)
        high = probe;
      else
        low = probe;
    }
    return (low + 1);
  }

  public static int getIndex(Vector v, String target) {
    int high = v.size(), low = -1, probe;
    while (high - low > 1) {
      probe = (high + low) / 2;
      if (target.compareToIgnoreCase((String)v.get(probe)) < 0)
        high = probe;
      else
        low = probe;
    }
    return (low + 1);
  }

  public static int getIndex(JComboBox combo, String target) {
    int high = combo.getItemCount(), low = -1, probe;
    while (high - low > 1) {
      probe = (high + low) / 2;
      if (target.compareToIgnoreCase((String)combo.getItemAt(probe)) < 0)
        high = probe;
      else
        low = probe;
    }
    return (low + 1);
  }
  
  // Events from tplBox, contBox, objBox, keyBox.
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    
    Object src = e.getSource();
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("search")) {
      search();
    } else if (e.getActionCommand().equalsIgnoreCase("ok")) {
      select();
    } else if (e.getActionCommand().equalsIgnoreCase("cancel")) {
      close();
    } else if (e.getActionCommand().equalsIgnoreCase("tpl")) {
      tpl();
    } else if (e.getActionCommand().equalsIgnoreCase("key")) {
      key();
    }
  }
}

package gov.nist.csd.pm.application.medrec;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:01 $
 * @since 1.5
 */
public class TemplateEditor extends JDialog implements ActionListener, ListSelectionListener {

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
 * @uml.property  name="tplNameField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField tplNameField;

  /**
 * @uml.property  name="allContLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel allContLabel;
  /**
 * @uml.property  name="allContListModel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private DefaultListModel allContListModel;
  /**
 * @uml.property  name="allContList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList allContList;
  /**
 * @uml.property  name="allContVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector allContVector;
  
  /**
 * @uml.property  name="selContLabel"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JLabel selContLabel;
  /**
 * @uml.property  name="selContListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel selContListModel;
  /**
 * @uml.property  name="selContList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList selContList;
  /**
 * @uml.property  name="selContVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector selContVector;

  /**
 * @uml.property  name="keyListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel keyListModel;
  /**
 * @uml.property  name="keyList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList keyList;
  /**
 * @uml.property  name="keyVector"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private Vector keyVector;

  /**
 * @uml.property  name="addButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton addButton;
  /**
 * @uml.property  name="editButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton editButton;
  /**
 * @uml.property  name="removeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton removeButton;

  /**
 * @uml.property  name="okButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton okButton;
  /**
 * @uml.property  name="closeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton closeButton;
  
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
  
  public TemplateEditor(MREditor mrEditor, SysCaller sysCaller, String sSessId) {
    super(mrEditor, true);
    
    setTitle("Template Editor");

    this.sSessId = sSessId;
    this.sysCaller = sysCaller;
    this.mrEditor = mrEditor;
    
    // Start building the GUI
    JPanel tplPane = new JPanel();

    JLabel tplNameLabel = new JLabel("Template name:");
    tplNameField = new JTextField(20);

    tplPane.add(tplNameLabel);
    tplPane.add(tplNameField);
    tplPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // The selected containers list and panel.
    selContLabel = new JLabel("Selected:");
    selContListModel = new DefaultListModel();
    selContList = new JList(selContListModel);
    selContList.addListSelectionListener(this);
    JScrollPane selContListScrollPane = new JScrollPane(selContList);
    selContListScrollPane.setPreferredSize(new Dimension(200, 150));

    JPanel selContPane = new JPanel();
    selContPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(selContPane, selContLabel, 0, 0, 1, 1);
    addComp(selContPane, selContListScrollPane, 0, 1, 3, 1);
    selContPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    // The all containers list and panel.
    allContLabel = new JLabel("All:");
    allContListModel = new DefaultListModel();
    allContList = new JList(allContListModel);
    allContList.addListSelectionListener(this);
    JScrollPane allContListScrollPane = new JScrollPane(allContList);
    allContListScrollPane.setPreferredSize(new Dimension(200, 150));

    JPanel allContPane = new JPanel();
    allContPane.setLayout(new GridBagLayout());
    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(allContPane, allContLabel, 0, 0, 1, 1);
    addComp(allContPane, allContListScrollPane, 0, 1, 3, 1);
    allContPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel contPane = new JPanel();
    contPane.setLayout(new GridLayout(1, 2));
    contPane.add(selContPane);
    contPane.add(allContPane);
    contPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 10, 0),
      BorderFactory.createTitledBorder("Containers")));

    // The key list.
    keyListModel = new DefaultListModel();
    keyList = new JList(keyListModel);
    JScrollPane keyListScrollPane = new JScrollPane(keyList);
    keyListScrollPane.setPreferredSize(new Dimension(200, 150));
    //keyListScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    
    // The key button pane
    JPanel keyButtonPane = new JPanel();

    addButton = new JButton("Add");
    addButton.setActionCommand("add");
    addButton.addActionListener(this);

    editButton = new JButton("Edit");
    editButton.setActionCommand("edit");
    editButton.addActionListener(this);

    removeButton = new JButton("Remove");
    removeButton.setActionCommand("remove");
    removeButton.addActionListener(this);

    keyButtonPane.add(addButton);
    keyButtonPane.add(editButton);
    keyButtonPane.add(removeButton);
    keyButtonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

    // The key pane = key list scroll pane + key button pane
    JPanel keyPane = new JPanel();
    keyPane.setLayout(new BorderLayout());
    keyPane.add(keyListScrollPane, BorderLayout.CENTER);
    keyPane.add(keyButtonPane, BorderLayout.SOUTH);
    keyPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 10, 0),
      BorderFactory.createTitledBorder("Keys")));


    // The panel for containers and keys
    JPanel contAndKeyPane = new JPanel();
    contAndKeyPane.setLayout(new GridLayout(2, 1));
    contAndKeyPane.add(contPane);
    contAndKeyPane.add(keyPane);
    
    
    // The button pane
    JPanel buttonPane = new JPanel();
    
    okButton = new JButton("Add template");
    okButton.setActionCommand("ok");
    okButton.addActionListener(this);

    closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);

    buttonPane.add(okButton);
    buttonPane.add(closeButton);
    buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));


    
    
    
    JPanel thePane = new JPanel();
    thePane.setLayout(new BorderLayout());
    thePane.add(tplPane, BorderLayout.NORTH);
    thePane.add(contAndKeyPane, BorderLayout.CENTER);
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
    // Clear lists and vectors.
    selContListModel.clear();
    if (selContVector == null) selContVector = new Vector();
    else selContVector.clear();
    
    allContListModel.clear();
    if (allContVector == null) allContVector = new Vector();
    else allContVector.clear();
    
    keyListModel.clear();
    if (keyVector == null) keyVector = new Vector();
    else keyVector.clear();
    
    // Fill in the "All containers" list.
    Packet res = (Packet)sysCaller.getProperContainers();
    if (res != null) for (int i = 0; i < res.size(); i++) {
      String sLine = res.getStringValue(i);
      String[] pieces = sLine.split(PM_FIELD_DELIM);
      int index = getIndex(allContListModel, pieces[0]);
      allContListModel.add(index, pieces[0]);
      allContVector.add(index, pieces[1]);
    }    
  }

  // Add the new template.
  private void addTemplate() {
    for (int i = 0; i < selContVector.size(); i++) {
      String sId = (String)selContVector.get(i);
      String sName = (String)selContListModel.get(i);
      System.out.println("i = " + i + ", " + sId + ", " + sName);
    }

    String sName = tplNameField.getText().trim();
    if (sName.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter a template name!");
      return;
    }

    // Send the command and let the server set the GUID and test for unique name.
    boolean bRes = sysCaller.addTemplate(sName, selContVector, keyVector);
    setVisible(false);
  }
  
  private void close() {
    setVisible(false);
  }

  private void addKey() {
    JTextField keyField = new JTextField();
    int ret = JOptionPane.showOptionDialog(this,
      new Object[] {"Please enter the key name:", keyField},
      "Add key", JOptionPane.OK_CANCEL_OPTION,
      JOptionPane.QUESTION_MESSAGE, null, null, null);
    if (ret != JOptionPane.OK_OPTION) return;
    String sKey = keyField.getText();
    if (sKey.length() == 0) {
      JOptionPane.showMessageDialog(this, "Please enter a key name!");
      return;
    }
    keyListModel.addElement(sKey);
    keyVector.add(sKey);
  }

  private void removeKey() {
    String sKey = (String)keyList.getSelectedValue();
    if (sKey == null) {
      JOptionPane.showMessageDialog(this, "Please select a key from the \"Keys\" list!");
      return;
    }
    keyListModel.removeElement(sKey);
    keyVector.remove(sKey);
  }

  private void editKey() {
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

  
  
  
  
  
  
  
  
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    
    Object src = e.getSource();
    if (src == allContList) {
      // User selected a container from the "All containers" list.
      int index = allContList.getSelectedIndex();
      // Control gets here also when removing all items.
      if (index < 0) return;
      String sCont = (String)allContList.getSelectedValue();
      String sContId = (String)allContVector.get(index);
      if (sCont != null) {
        if (selContListModel.contains(sCont)) {
          JOptionPane.showMessageDialog(this, "Container already selected!");
          return;
        }
        //int index = getIndex(selContListModel, sCont);
        //selContListModel.add(index, sCont);
        selContListModel.addElement(sCont);
        selContVector.add(sContId);        
      }
      return;
    } else if (src == selContList) {
      int index = selContList.getSelectedIndex();
      String sCont = (String)selContList.getSelectedValue();
      if (sCont != null) {
        selContListModel.removeElement(sCont);
        selContVector.remove(index);
      }
      return;
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("ok")) {
      addTemplate();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      close();
    } else if (e.getActionCommand().equalsIgnoreCase("add")) {
      addKey();
    } else if (e.getActionCommand().equalsIgnoreCase("edit")) {
      editKey();
    } else if (e.getActionCommand().equalsIgnoreCase("remove")) {
      removeKey();
    }
  }
  
  /*
  static String sessid = null;
  static int simport = 0;
   
  // Create the GUI, 
  private static void createGUI() {
    CompositeEditor editor = new CompositeEditor(simport, sessid);
    editor.pack();
    editor.setVisible(true);
  }

  // Arguments on the command line:
  // -session <sessionId> -simport <simulator port>
  // <simulator port> is the port where the kernel simulator listens for connections,
  //    by default 8081.
  // <sessionId> is the id of the session where the grantor is running. It is
  //    mandatory.
  public static void main(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-session")) {
        sessid = args[++i];
      } else if (args[i].equals("-simport")) {
        simport = Integer.valueOf(args[++i]).intValue();
      }
    }
    if (sessid == null) {
      System.out.println("This application must run within a Policy Machine session!");
      System.exit(-1);
    }
    
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            createGUI();
        }
    });
  }
   *
   */
}

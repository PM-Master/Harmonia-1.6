/*
 * EmailEditor.java
 *
 * Created on April 2, 2007
 */

package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
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
public class EmlAcctEditor extends JDialog implements ActionListener {
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

  /**
 * @uml.property  name="pmUserField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField pmUserField;
  /**
 * @uml.property  name="comingFromField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField comingFromField;
  /**
 * @uml.property  name="emailAddressField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField emailAddressField;
  /**
 * @uml.property  name="popServerField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField popServerField;
  /**
 * @uml.property  name="smtpServerField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField smtpServerField;
  /**
 * @uml.property  name="acctNameField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField acctNameField;
  /**
 * @uml.property  name="passwordField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JPasswordField passwordField;
  
  /**
 * @uml.property  name="saveButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton saveButton;
  /**
 * @uml.property  name="closeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton closeButton;
  
  
  public EmlAcctEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, false);

    this.tool = tool;
    this.sslClient = sslClient;
    setTitle("Set/Edit E-mail Account");

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        doClose();
      }
    });

    // Start building the GUI
    JPanel userPane = new JPanel();
    userPane.setLayout(new GridBagLayout());

    JLabel pmUserLabel = new JLabel("PM User:");
    JLabel userNameLabel = new JLabel("'Coming from' Name:");
    JLabel emailAddressLabel = new JLabel("E-mail Address:");
    JLabel popServerLabel = new JLabel("Incoming Server:");
    JLabel smtpServerLabel = new JLabel("Outgoing Server:");
    JLabel acctNameLabel = new JLabel("Account Name:");
    JLabel passwordLabel = new JLabel("Password:");
    
    pmUserField = new JTextField(20);
    comingFromField = new JTextField(20);
    emailAddressField = new JTextField(20);
    popServerField = new JTextField(20);
    smtpServerField = new JTextField(20);
    acctNameField = new JTextField(20);
    passwordField = new JPasswordField(20);

    constraints.insets = new Insets(0, 10, 0, 0);

    addComp(userPane, pmUserLabel, 0, 0, 1, 1);
    addComp(userPane, userNameLabel, 0, 1, 1, 1);
    addComp(userPane, emailAddressLabel, 0, 2, 1, 1);
    addComp(userPane, popServerLabel, 0, 3, 1, 1);
    addComp(userPane, smtpServerLabel, 0, 4, 1, 1);
    addComp(userPane, acctNameLabel, 0, 5, 1, 1);
    addComp(userPane, passwordLabel, 0, 6, 1, 1);

    addComp(userPane, pmUserField, 1, 0, 3, 1);
    addComp(userPane, comingFromField, 1, 1, 3, 1);
    addComp(userPane, emailAddressField, 1, 2, 3, 1);
    addComp(userPane, popServerField, 1, 3, 3, 1);
    addComp(userPane, smtpServerField, 1, 4, 3, 1);
    addComp(userPane, acctNameField, 1, 5, 3, 1);
    addComp(userPane, passwordField, 1, 6, 3, 1);

    userPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(10, 0, 10, 0),
      BorderFactory.createTitledBorder("E-mail Account")));

    saveButton = new JButton("Save");
    saveButton.setActionCommand("save");
    saveButton.addActionListener(this);

    closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);

    JPanel buttonPane = new JPanel();
    buttonPane.add(saveButton);
    buttonPane.add(closeButton);

    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(userPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    setContentPane(contentPane);
    getRootPane().setDefaultButton(saveButton);
  }  

  private void addComp(Container container, Component component, int x, int y, int w, int h) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = w;
    constraints.gridheight = h;
    container.add(component, constraints);
  }

  // Ask the engine for information about the email account of an user.
  // The result contains:
  // item 0: <user name>:<user id>
  // item 1: <'coming from' name>
  // item 2: <email address>
  // item 3: <incoming server>
  // item 4: <outgoing server>
  // item 5: <account name>.
  // If the user has no email account, the result contains only item 0.
  // The result contains an error message in failures, like "No such user".
  public boolean prepare(String sUserId, String sUserName) {
    pmUserField.setText(sUserName);
    pmUserField.setEditable(false);

    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getEmailAcct", sUserName);
      res = sslClient.sendReceive(cmd, null);
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, "Exception in getEmailAcct!");
      return false;
    }
    if (res == null) {
      JOptionPane.showMessageDialog(this, "Null result from getEmailAcct!");
      return false;
    }
    if (res.hasError()) {
      JOptionPane.showMessageDialog(this, res.getErrorMessage());
      return false;
    }
    if (res.size() >= 6) {
      comingFromField.setText(res.getStringValue(1));
      emailAddressField.setText(res.getStringValue(2));
      popServerField.setText(res.getStringValue(3));
      smtpServerField.setText(res.getStringValue(4));
      acctNameField.setText(res.getStringValue(5));
      passwordField.setText(null);
    } else {
      comingFromField.setText(null);
      emailAddressField.setText(null);
      popServerField.setText(null);
      smtpServerField.setText(null);
      acctNameField.setText(null);
      passwordField.setText(null);
    }      
    return true;
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("save")) {
      doSave();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      doClose();
    }
  }

  private void doSave() {
    String sPmUser = pmUserField.getText().trim();
    String sComingFrom = comingFromField.getText().trim();
    String sEmailAddr = emailAddressField.getText().trim();
    String sPopServer = popServerField.getText().trim();
    String sSmtpServer = smtpServerField.getText().trim();
    String sAcctName = acctNameField.getText().trim();

    if (sPmUser.length() == 0 || sComingFrom.length() == 0 ||
        sEmailAddr.length() == 0 || sPopServer.length() == 0 ||
        sSmtpServer.length() == 0) {
      JOptionPane.showMessageDialog(this, "All fields are mandatory!");
      return;
    }

    char[] cPass = passwordField.getPassword();
    if (cPass.length == 0) {
      JOptionPane.showMessageDialog(this, "All fields are mandatory!");
      return;
    }
    String sPassword = new String(cPass);

    // Send the command and let the server set the GUID and test for unique name.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("addEmailAcct", sPmUser, sComingFrom, sEmailAddr,
        sPopServer, sSmtpServer, sAcctName, sPassword);
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(tool, "Null result from addEmailAcct!");
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
  }
  
  private void doClose() {
    this.setVisible(false);
  }  
}

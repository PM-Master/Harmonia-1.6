/*
 * CommandEditor.java
 *
 * Created on April 29, 2005, 11:13 AM
 * Serban I. Gavrila
 * VDG Inc.
 */

package gov.nist.csd.pm.admin;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;



/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class CommandEditor extends JDialog implements ActionListener {

  /**
 * @uml.property  name="tool"
 * @uml.associationEnd  
 */
PmAdmin tool = null;
  /**
 * @uml.property  name="sslClient"
 * @uml.associationEnd  
 */
SSLSocketClient sslClient = null;

  /**
 * @uml.property  name="cmdBox"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JComboBox cmdBox;
  /**
 * @uml.property  name="sendButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton sendButton;
  /**
 * @uml.property  name="closeButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton closeButton;
  /**
 * @uml.property  name="textArea"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextArea textArea;
  
  /**
 * @uml.property  name="history"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private HashSet history;

  /**
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();
  
  /**
 * @uml.property  name="sNewline"
 */
private String sNewline = System.getProperty("line.separator");

  public CommandEditor(PmAdmin tool, SSLSocketClient sslClient) {
    //super(tool, true);  // modal
    super(tool, false);  // modal
    
    this.tool = tool;
    this.sslClient = sslClient;

    setTitle("Send Command");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

    // Start building the GUI.
    JLabel cmdLabel = new JLabel("Command:");
    cmdBox = new JComboBox();
    cmdBox.setPreferredSize(new Dimension(400, 25));
    cmdBox.setEditable(true);
    cmdBox.addActionListener(this);
    cmdBox.setActionCommand("box");

    history = new HashSet();
    
    // Defined Send as the default button of the root pane;
    // no need to define keyboard events for the cmdField.
    //    cmdField.registerKeyboardAction(this,
    //                                    "enter",
    //                                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
    //                                    JComponent.WHEN_FOCUSED);
    JPanel cmdPane = new JPanel();
    cmdPane.setLayout(new GridBagLayout());

    addComp(cmdPane, cmdLabel, 0, 0, 1, 1);
    addComp(cmdPane, cmdBox, 1, 0, 4, 1);

    cmdPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
    
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new GridBagLayout());

    sendButton = new JButton("Send");
    sendButton.setActionCommand("send");
    sendButton.addActionListener(this);

    closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);

    addComp(buttonPane, sendButton, 0, 0, 1, 1);
    constraints.insets = new Insets(0, 10, 0, 0);
    addComp(buttonPane, closeButton, 1, 0, 1, 1);

    buttonPane.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
    
    textArea = new JTextArea(10, 40);
    JScrollPane scrollPane = new JScrollPane(textArea);
    textArea.setEditable(false);
    
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(cmdPane, BorderLayout.NORTH);
    contentPane.add(buttonPane, BorderLayout.CENTER);
    contentPane.add(scrollPane, BorderLayout.SOUTH);

    setContentPane(contentPane);
    getRootPane().setDefaultButton(sendButton);
  }
  
  private void addComp(Container container, Component component, int x, int y, int w, int h) {
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.gridwidth = w;
    constraints.gridheight = h;
    container.add(component, constraints);
  }
  
  private void close() {
    this.setVisible(false);
  }

  public void prepare() {
    textArea.setText("");
  }
  
  // The command syntax is: the command is composed of a command code
  // followed by command fields, separated by PM_FIELD_DELIM, which is
  // the colon ':'.
  // The leading and trailing white space of a field will be trimmed.
  // A field can be omitted, but the separator not.
  private void send() {
    String sLine = ((String)cmdBox.getSelectedItem()).trim();
    if (sLine.length() == 0) {
      //cmdBox.requestFocus();
      return;
    }
    
    Packet cmd = new Packet();
    Packet res = null;
    String sNewLine = System.getProperty("line.separator");

    try {
      String[] fields = sLine.split(PmAdmin.PM_FIELD_DELIM);
      for (int i=0; i < fields.length; i++) {
        if (i == 0) {
          if (fields[0].length() == 0) {
            JOptionPane.showMessageDialog(this, "Command code cannot be empty");
            return;
          }
          cmd.addItem(ItemType.CMD_CODE, fields[0]);
        } else {
          if (fields[i].length() > 0) cmd.addItem(ItemType.CMD_ARG, fields[i]);
        }
      }
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(tool, "Undetermined error, null result returned");
        return;
      }
      for (int i = 0; i < res.size(); i++) {
        textArea.append(sNewLine);
        textArea.append(res.getStringValue(i));
      }
    } catch (Exception e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(tool, e.getMessage());
      return;
    }

    if (!res.hasError() && !history.contains(sLine)) {
      history.add(sLine);
      cmdBox.addItem(sLine);
    }
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("box")) {
      // Nothing, in order to let the user edit the entry.
      //send();
    } else if (e.getActionCommand().equalsIgnoreCase("Send")) {
      send();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      close();
    } else if (e.getActionCommand().equalsIgnoreCase("enter")) {
      send();
    }
  }
}

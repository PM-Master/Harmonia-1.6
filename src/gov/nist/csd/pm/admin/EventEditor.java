/*
 * EventEditor.java
 *
 * Created on December 23, 2005, 12:02 PM
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
public class EventEditor extends JDialog implements ActionListener {
  
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
 * @uml.property  name="eventField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField eventField;

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

  /**
 * @uml.property  name="eventListModel"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private DefaultListModel eventListModel;
  /**
 * @uml.property  name="eventList"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
private JList eventList;

  /**
 * @uml.property  name="constraints"
 */
private GridBagConstraints constraints = new GridBagConstraints();

  public EventEditor(PmAdmin tool, SSLSocketClient sslClient) {
    super(tool, true);  // modal

    this.tool = tool;
    this.sslClient = sslClient;

    setTitle("Event Names");

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        close();
      }
    });

    // Start building the GUI.
    // The new event pane
    JPanel eventPane = new JPanel();
    eventPane.setLayout(new GridBagLayout());

    JLabel eventLabel = new JLabel("Event Name:");
    eventField = new JTextField(20);

    constraints.insets = new Insets(0, 0, 0, 0);
    addComp(eventPane, eventLabel, 0, 0, 1, 1);
    constraints.insets = new Insets(0, 10, 0, 0);
    addComp(eventPane, eventField, 1, 0, 3, 1);
    
    eventPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    // The lists pane
    JPanel listPane = new JPanel();

    eventListModel = new DefaultListModel();
    eventList = new JList(eventListModel);
    JScrollPane eventListScrollPane = new JScrollPane(eventList);
    eventListScrollPane.setPreferredSize(new Dimension(220,100));
    listPane.add(eventListScrollPane);
    listPane.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(20, 0, 0, 0),
      BorderFactory.createTitledBorder("Event Names")));

    // The button pane
    JPanel buttonPane = new JPanel();

    addButton = new JButton("Add");
    addButton.setActionCommand("add");
    addButton.addActionListener(this);

    deleteButton = new JButton("Delete");
    deleteButton.setActionCommand("delete");
    deleteButton.addActionListener(this);

    closeButton = new JButton("Close");
    closeButton.setActionCommand("close");
    closeButton.addActionListener(this);

    buttonPane.add(addButton);
    buttonPane.add(deleteButton);
    buttonPane.add(closeButton);
    buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(eventPane, BorderLayout.NORTH);
    contentPane.add(listPane, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

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
  
  public void prepare() {
    eventField.setText("");
    eventField.requestFocus();
    
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("getEventNames");
      res = sslClient.sendReceive(cmd, null);
      if (res == null) {
        JOptionPane.showMessageDialog(this, "Null result from the engine!");
        return;
      }
      if (res.hasError()) {
        JOptionPane.showMessageDialog(this, res.getErrorMessage());
        return;
      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Exception in getEventNames: " + e.getMessage());
      return;
    }

    eventListModel.clear();
    for (int i = 0; i < res.size(); i++) {
      String sEvent = res.getStringValue(i);
      int index = PmAdmin.getIndex(eventListModel, sEvent);
      eventListModel.add(index, sEvent);
    }
  }


  private void close() {
    this.setVisible(false);
  }

  private void add() {
    String sEvent = eventField.getText().trim();
    if (sEvent.length() == 0) {
      JOptionPane.showMessageDialog(tool, "Please enter an event name!");
      return;
    }

    // Add the event name.
    Packet res = null;
    try {
      Packet cmd = tool.makeCmd("addEventName", sEvent);    
      eventField.setText("");
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

    if (!eventListModel.contains(sEvent)) {
      int index = PmAdmin.getIndex(eventListModel, sEvent);
      eventListModel.add(index, sEvent);
    }
  }

  private void delete() {
    String sEvent = (String)eventList.getSelectedValue();
    if (sEvent == null || sEvent.length() == 0) {
      JOptionPane.showMessageDialog(tool, "Please select an event from the list!");
      return;
    }
    // Send the command and let the server test the other conditions.
    Packet res;
    try {
      Packet cmd = tool.makeCmd("deleteEventName", sEvent);
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

    eventListModel.removeElement(sEvent);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("add")) {
      add();
    } else if (e.getActionCommand().equalsIgnoreCase("delete")) {
      delete();
    } else if (e.getActionCommand().equalsIgnoreCase("close")) {
      close();
    }
  }
}

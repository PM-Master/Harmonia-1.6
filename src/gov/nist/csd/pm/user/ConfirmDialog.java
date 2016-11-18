/*
 * ConfirmDialog
 *
 */

package gov.nist.csd.pm.user;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class ConfirmDialog extends JDialog implements ActionListener {

  /**
 * @uml.property  name="title"
 */
private String title;
  /**
 * @uml.property  name="msg"
 */
private String msg;
  /**
 * @uml.property  name="yesButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton yesButton;
  /**
 * @uml.property  name="noButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton noButton;

  /**
 * @uml.property  name="yes"
 */
private boolean yes = false;
  
  public ConfirmDialog(Session session, String title, String msg) {
    super(session, true);  // modal
    this.title = title;
    this.msg = msg;

    setTitle(title);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        doNo();
      }
    });

    // Start building the GUI.
    JLabel msgLabel = new JLabel(msg);
    JPanel msgPane = new JPanel();
    msgPane.add(msgLabel);
    msgPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    JPanel buttonPane = new JPanel();

    yesButton = new JButton("Yes");
    yesButton.setActionCommand("yes");
    yesButton.addActionListener(this);

    noButton = new JButton("No");
    noButton.setActionCommand("no");
    noButton.addActionListener(this);

    buttonPane.add(yesButton);
    buttonPane.add(noButton);
    buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(msgPane, BorderLayout.NORTH);
    contentPane.add(buttonPane, BorderLayout.SOUTH);
    setContentPane(contentPane);
    getRootPane().setDefaultButton(yesButton);
  }
    
  public boolean getConfirmation() {
    return yes;
  }
  
  private void doYes() {
    yes = true;
    setVisible(false);
  }
  
  private void doNo() {
    yes = false;
    setVisible(false);
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("yes")) {
      doYes();
    } else if (e.getActionCommand().equalsIgnoreCase("no")) {
      doNo();
    }
  }
}

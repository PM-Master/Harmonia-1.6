/*
 * OsConfigEditor.java
 *
 * Created on October 17, 2005, 12:10 PM
 */

package gov.nist.csd.pm.user;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class SessConfigEditor extends JDialog {
  
  /**
 * @uml.property  name="session"
 * @uml.associationEnd  
 */
private Session session = null;

  /**
 * @uml.property  name="ksField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField ksField;
  /**
 * @uml.property  name="ksButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton ksButton;

  /**
 * @uml.property  name="tsField"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JTextField tsField;
  /**
 * @uml.property  name="tsButton"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JButton tsButton;
  
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

  public SessConfigEditor(Session sess) {
    super(sess, true);
    setTitle("Configuration Editor");
    this.session = sess;
    
    this.setLocation(sess.getLocation());

    JLabel promptLabel = new JLabel("Please enter the absolute paths of " +
      session.getUser() + "'s certificate stores:");
    JPanel promptPane = new JPanel();
    promptPane.add(promptLabel);
    promptPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

    
    // A file chooser for jars
    final JFileChooser fc = new JFileChooser();
//    fc.addChoosableFileFilter(new JarFilter());

    // ks
    ksField = new JTextField(25);
    JLabel ksLabel = new JLabel("Key Store");
    ksLabel.setLabelFor(ksField);
    ksButton = new JButton("...");
    ksButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int returnVal = fc.showOpenDialog(SessConfigEditor.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          ksField.setText(file.getPath());
        }
      }
    });

    // ts
    tsField = new JTextField(25);
    JLabel tsLabel = new JLabel("Trust Store");
    tsLabel.setLabelFor(tsField);
    tsButton = new JButton("...");
    tsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int returnVal = fc.showOpenDialog(SessConfigEditor.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File file = fc.getSelectedFile();
          tsField.setText(file.getPath());
        }
      }
    });
    
    JPanel labelPane = new JPanel();
    labelPane.setLayout(new GridLayout(0, 1));
    labelPane.add(ksLabel);
    labelPane.add(tsLabel);

    JPanel fieldPane = new JPanel();
    fieldPane.setLayout(new GridLayout(0, 1));
    fieldPane.add(ksField);
    fieldPane.add(tsField);

    JPanel browseButtonPane = new JPanel();
    browseButtonPane.setLayout(new GridLayout(0, 1));
    browseButtonPane.add(ksButton);
    browseButtonPane.add(tsButton);

    JPanel upperPane = new JPanel();
    upperPane.setLayout(new BorderLayout());
    upperPane.add(promptPane, BorderLayout.NORTH);
    upperPane.add(labelPane, BorderLayout.WEST);
    upperPane.add(fieldPane, BorderLayout.CENTER);
    upperPane.add(browseButtonPane, BorderLayout.EAST);

    okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void  actionPerformed(ActionEvent event) {
        String ksLoc = ksField.getText();
        /*
        if (ksLoc == null || ksLoc.length() == 0) {
          JOptionPane.showMessageDialog(null, "You must enter your Key Store location");
          return;
        }
         */

        String tsLoc = tsField.getText();
        /*
        if (tsLoc == null || tsLoc.length() == 0) {
          JOptionPane.showMessageDialog(null, "You must enter your Trust Store location");
          return;
        }
         */
        session.setKStorePaths(ksLoc, tsLoc);
        setVisible(false);
      }
    });

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void  actionPerformed(ActionEvent event) {
        setVisible(false);
      }
    });

    JPanel lowerPane = new JPanel();
    lowerPane.add(okButton);
    lowerPane.add(cancelButton);
    lowerPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(upperPane, BorderLayout.NORTH);
    contentPane.add(lowerPane, BorderLayout.SOUTH);
    setContentPane(contentPane);


    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        setVisible(false);
      }
    });
  }

  public void setKStorePaths(String sKsPath, String sTsPath) {
    ksField.setText(sKsPath);
    tsField.setText(sTsPath);
  }
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.application.grantor;

import gov.nist.csd.pm.common.util.swing.SwingShortcuts;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 *
 * @author Administrator
 */
public class MessageView extends JPanel {
    //This layout causes the middle column to fill available horizontal
    //space and the 7th row (the message row) fill available vertical space
    /**
	 * @uml.property  name="layout"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private MigLayout layout = new MigLayout("", "[align label][100:300:][]", "[][][][][][]20[100:200:, grow, fill][]");
    /**
	 * @uml.property  name="fromTextField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextField fromTextField;
    /**
	 * @uml.property  name="toTextField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextField toTextField;
    /**
	 * @uml.property  name="toBrowseButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JButton toBrowseButton;
    /**
	 * @uml.property  name="subjectTextField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextField subjectTextField;
    /**
	 * @uml.property  name="objectTextField"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextField objectTextField;
    /**
	 * @uml.property  name="objectBrowseButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JButton objectBrowseButton;
    /**
	 * @uml.property  name="copyObjectCheckBox"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JCheckBox copyObjectCheckBox;
    /**
	 * @uml.property  name="readCheckBox"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JCheckBox readCheckBox;
    /**
	 * @uml.property  name="writeCheckBox"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JCheckBox writeCheckBox;
    /**
	 * @uml.property  name="confineToCheckBox"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JCheckBox confineToCheckBox;
    /**
	 * @uml.property  name="confineToComboBox"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
    private JComboBox confineToComboBox;
    /**
	 * @uml.property  name="messageTextArea"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JTextPane messageTextArea;
    //private JButton pasteButton;
    //private JButton grantSendButton;
    //private JButton closeButton;


    public MessageView(){
        initGUI();
    }

    private final void initGUI(){
        setLayout(layout);
        add(new JLabel("From:"));
        fromTextField = new JTextField();
        add(fromTextField, "growx, wrap");
        add(new JLabel("To:"));
        toTextField = new JTextField();
        add(toTextField, "growx");
        toBrowseButton = new JButton("Browse...");
        add(toBrowseButton, "wrap");
        add(new JLabel("Subject:"));
        subjectTextField = new JTextField();
        add(subjectTextField, "growx,wrap");
        add(new JLabel("Object:"));
        objectTextField = new JTextField();
        add(objectTextField, "growx");
        objectBrowseButton = new JButton("Browse...");
        add(objectBrowseButton, "wrap");
        add(new JLabel("Options:"));
        copyObjectCheckBox = new JCheckBox("Copy Object?");
        add(copyObjectCheckBox, "tag access, split 3, growx 0");
        readCheckBox = new JCheckBox("Read");
        add(readCheckBox, "tag access, growx 0");
        writeCheckBox = new JCheckBox("Write");
        add(writeCheckBox, "tag access,growx 0,push, wrap");
        confineToCheckBox = new JCheckBox("Confine to:");
        add(confineToCheckBox, "skip, split 2, span 1, growx 0");
        confineToComboBox = new JComboBox();
        add(confineToComboBox, "growx,wrap");
        add(new JLabel("Message:"), "growy 0, aligny top");
        messageTextArea = new JTextPane();
        add(new JScrollPane(messageTextArea), "growx,wrap");
        /*
        pasteButton = new JButton("Paste");
        grantSendButton = new JButton("Grant/Send");
        closeButton = new JButton("Close");
        add(pasteButton, "tag other, skip, split 3");
        add(grantSendButton, "tag other");
        add(closeButton, "tag other");
        */
    }

    /**
	 * @return
	 * @uml.property  name="toBrowseButton"
	 */
    public JButton getToBrowseButton(){
        return toBrowseButton;
    }
    /*
    public JButton getCloseButton() {
        return closeButton;
    }
	*/
    /**
	 * @return
	 * @uml.property  name="confineToCheckBox"
	 */
    public JCheckBox getConfineToCheckBox() {
        return confineToCheckBox;
    }

    /**
	 * @return
	 * @uml.property  name="confineToComboBox"
	 */
    public JComboBox getConfineToComboBox() {
        return confineToComboBox;
    }

    /**
	 * @return
	 * @uml.property  name="copyObjectCheckBox"
	 */
    public JCheckBox getCopyObjectCheckBox() {
        return copyObjectCheckBox;
    }

    /**
	 * @return
	 * @uml.property  name="fromTextField"
	 */
    public JTextField getFromTextField() {
        return fromTextField;
    }
    /*
    public JButton getGrantSendButton() {
        return grantSendButton;
    }
     */
    /**
	 * @return
	 * @uml.property  name="messageTextArea"
	 */
    public JTextPane getMessageTextArea() {
        return messageTextArea;
    }

    /**
	 * @return
	 * @uml.property  name="objectBrowseButton"
	 */
    public JButton getObjectBrowseButton() {
        return objectBrowseButton;
    }

    /**
	 * @return
	 * @uml.property  name="objectTextField"
	 */
    public JTextField getObjectTextField() {
        return objectTextField;
    }
    /*
    public JButton getPasteButton() {
        return pasteButton;
    }
	*/
    /**
	 * @return
	 * @uml.property  name="readCheckBox"
	 */
    public JCheckBox getReadCheckBox() {
        return readCheckBox;
    }

    /**
	 * @return
	 * @uml.property  name="subjectTextField"
	 */
    public JTextField getSubjectTextField() {
        return subjectTextField;
    }

    /**
	 * @return
	 * @uml.property  name="toTextField"
	 */
    public JTextField getToTextField() {
        return toTextField;
    }

    /**
	 * @return
	 * @uml.property  name="writeCheckBox"
	 */
    public JCheckBox getWriteCheckBox() {
        return writeCheckBox;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                JFrame frame = SwingShortcuts.displayInJFrame(new MessageView(), "Test Window");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

}

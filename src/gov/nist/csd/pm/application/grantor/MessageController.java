/*
 * Messenger.java
 *
 * Created on July 6, 2007, 11:04 AM
 */
package gov.nist.csd.pm.application.grantor;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;
import com.bric.swing.ColorPicker;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.*;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;
import gov.nist.csd.pm.common.util.DefaultResponseHashMap;
import gov.nist.csd.pm.common.util.swing.ToolBarBuilder;
import say.swing.JFontChooser;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static gov.nist.csd.pm.application.grantor.GrantorIconInfo.*;


/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class MessageController implements ActionListener,
        ObjectBrowserListener, UserBrowserListener {

    public static final String EGRANT_APP_NAME = "e-grant";
    public static final String EXTERNAL_SEND_SUCCESSFUL_MSG;
    public static final String INTERNAL_SEND_SUCCESSFUL_MSG;

    static {
        EXTERNAL_SEND_SUCCESSFUL_MSG = String.format("External %s message successfully sent.", EGRANT_APP_NAME);
        INTERNAL_SEND_SUCCESSFUL_MSG = String.format("Internal %s message sent successfully.", EGRANT_APP_NAME);
    }

    /**
	 * @uml.property  name="grantor"
	 * @uml.associationEnd  
	 */
    private Grantor grantor;
    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    private SysCaller sysCaller;
    /**
	 * @uml.property  name="sUserName"
	 */
    private String sUserName;
    /**
	 * @uml.property  name="sSessId"
	 */
    private String sSessId;
    /**
	 * @uml.property  name="exporterClient"
	 * @uml.associationEnd  
	 */
    private SSLSocketClient exporterClient;
    /**
	 * @uml.property  name="bDebug"
	 */
    private boolean bDebug;
    /**
	 * @uml.property  name="userBrowser"
	 * @uml.associationEnd  
	 */
    private UserBrowser userBrowser;
    /**
	 * @uml.property  name="objectBrowser"
	 * @uml.associationEnd  
	 */
    private ObjectBrowser objectBrowser;
    /**
	 * @uml.property  name="messageView"
	 * @uml.associationEnd  
	 */
    private MessageView messageView;
    /**
	 * @uml.property  name="messageWindow"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JFrame messageWindow;
    /**
	 * @uml.property  name="colorPicker"
	 * @uml.associationEnd  
	 */
    private ColorPicker colorPicker;
    /**
	 * @uml.property  name="fontChooser"
	 * @uml.associationEnd  
	 */
    private JFontChooser fontChooser;
    public static final String ACTION_GRANT_SEND = "grant/send";
    public static final String ACTION_CLOSE = "close";
    public static final String ACTION_PASTE = "paste";
    public static final String ACTION_BROWSE_USERS = "browse_users";
    public static final String ACTION_BROWSE_OBJECTS = "browse_objects";
    public static final String ACTION_ATTACH = "browseobjects";
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_CUT = "cut";
    public static final String ACTION_COPY = "copy";
    public static final String ACTION_EDIT_COLORS = "edit_colors";
    public static final String ACTION_EDIT_FONTS = "edit_fonts";
    public static final String ICON_NULL = "";
    public static final String ACTION_CONFINE_CHECK= "confineCheck";

    public MessageController(Grantor grantor, SysCaller sysCaller,
            String sUserName, String sSessId, int nExPort, boolean bDebug) {

        messageWindow = new JFrame();
        // super(grantor, true);
        messageWindow.setTitle("E-grant/E-mail");
        this.grantor = grantor;
        this.sysCaller = sysCaller;
        this.sUserName = sUserName;
        this.sSessId = sSessId;
        this.bDebug = bDebug;
        messageView = new MessageView();
        messageWindow.getContentPane().setLayout(new BorderLayout());
        messageWindow.getContentPane().add(messageView, BorderLayout.CENTER);
        installToolBar(messageWindow);
        messageWindow.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                close();
            }
        });
        messageWindow.pack();
        int exporterPort = (nExPort < 1024) ? PM_DEFAULT_EXPORTER_PORT
                : nExPort;
        System.out.println("Trying to create the exporter client socket with port "
                + exporterPort);
        try {
            // exporterClient = new SSLSocketClient("localhost", exporterPort,
            // bDebug, "GRANTOR");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to create SSL socket for Exporter on the local host.");
            e.printStackTrace();
            System.exit(-1);
        }

        messageView.getToBrowseButton().addActionListener(this);
       // messageView.getToBrowseButton().setActionCommand("browseusers");
        messageView.getToBrowseButton().setActionCommand(ACTION_BROWSE_USERS);
        
        messageView.getObjectBrowseButton().addActionListener(this);
       // messageView.getObjectBrowseButton().setActionCommand("browseobjects");
        messageView.getObjectBrowseButton().setActionCommand(ACTION_BROWSE_OBJECTS);

        messageView.getConfineToCheckBox().addActionListener(this);
       // messageView.getConfineToCheckBox().setActionCommand("confinebox");
        messageView.getConfineToCheckBox().setActionCommand(ACTION_CONFINE_CHECK);
        fillInConfineBox();

        messageView.getReadCheckBox().setSelected(true);
        messageView.getReadCheckBox().setEnabled(false);
        /*
        messageView.getPasteButton().addActionListener(this);
        messageView.getGrantSendButton().addActionListener(this);
        messageView.getCloseButton().addActionListener(this);
         */
        String sComingFrom = grantor.getComingFrom();
        if (sComingFrom == null) {
            messageView.getFromTextField().setText(sUserName);
        } else {
            messageView.getFromTextField().setText(sComingFrom);
        }
    }

    

    private void installToolBar(RootPaneContainer messageWindow2) {
        ToolBarBuilder tb = new ToolBarBuilder(this).
        		addStrut(5).addButton(ICON_MAIL_SEND, ACTION_GRANT_SEND, "Send mail/grant").
        		addStrut(5).addButton(ICON_DOCUMENT_SAVE, ACTION_SAVE, "Save this message").
        		addStrut(5).addButton(ICON_MAIL_ATTACH, ACTION_ATTACH, "Attach an object").
        		addSpring(20, 50).addButton(ICON_EDIT_CUT, ACTION_CUT, "Cut").
        		addStrut(5).addButton(ICON_EDIT_COPY, ACTION_COPY, "Copy").
        		addStrut(5).addButton(ICON_EDIT_PASTE, ACTION_PASTE, "Paste").
        		addSpring(10, 100).addButton(ICON_GNOME_COLOR_CHOOSER, ACTION_EDIT_COLORS, "Colors").
        		addStrut(5).addButton(ICON_FONT_X_GENERIC, ACTION_EDIT_FONTS, "Fonts").addSpring(5, 50);
        JToolBar toolBar = tb.getToolBar();
        messageWindow2.getContentPane().add(toolBar, BorderLayout.NORTH);

    }

    private void fillInConfineBox() {
        messageView.getConfineToComboBox().addItem("OU messages");
    }

    public void setAttachment(String sObjName) {
        messageView.getObjectTextField().setText(sObjName);
    }

    public void setContent(String sContent) {
        messageView.getMessageTextArea().setText(sContent);
    }

    public void setSubject(String sSubject) {
        messageView.getSubjectTextField().setText(sSubject);
    }

    public void setToField(PmNode node) {
        System.out.println("To field <- " + node.getType() + ", " + node.getId() + ", "
                + node.getName());
        messageView.getToTextField().setText(node.getName());
    }

    public void setAttField(PmNode node) {
        System.out.println("Att field <- " + node.getType() + ", " + node.getId()
                + ", " + node.getName());
        messageView.getObjectTextField().setText(node.getName());
    }

    private void editColors() {
        if (colorPicker == null) {
            colorPicker = new ColorPicker();
            colorPicker.addPropertyChangeListener(ColorPicker.SELECTED_COLOR_PROPERTY,
                    new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            Object newColor = event.getNewValue();
                            Object oldColor = event.getOldValue();
                            System.out.println("New color: " + newColor + " Old color: " + oldColor);
                        }
                    });
            JFrame window = new JFrame();
            window.getContentPane().add(colorPicker);
            window.pack();
        }
        Container cont = colorPicker.getTopLevelAncestor();
        cont.setVisible(!cont.isVisible());

    }

    private void editFonts() {
        if (fontChooser == null) {
            fontChooser = new JFontChooser();
            fontChooser.addPropertyChangeListener(JFontChooser.FONT_PROPERTY,
                    new PropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            Object oldFont = event.getOldValue();
                            Object newFont = event.getNewValue();
                            System.out.println("New font: " + newFont + " Old font: " + oldFont);
                        }
                    });
            fontChooser.showWindow(messageWindow);
        } else {
            Window window = SwingUtilities.getWindowAncestor(fontChooser);
            window.setVisible(!window.isVisible());
        }
    }

    private void paste(ActionEvent e) {
        if (!sysCaller.isPastingAllowed()) {
            System.out.println("Messenger: Paste not allowed!");
            JOptionPane.showMessageDialog(messageWindow,
                    sysCaller.getLastError());
            return;
        }



        Action actions[] = messageView.getMessageTextArea().getActions();
        Action pasteAction = null;
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            String sName = (String) a.getValue(Action.NAME);
            if (sName.equals(DefaultEditorKit.pasteAction)) {
                pasteAction = a;
                break;
            }
        }
        if (pasteAction == null) {
            JOptionPane.showMessageDialog(messageWindow,
                    "No paste action found!");
            return;
        }
        pasteAction.actionPerformed(e);
    }

    private void send() {
        // The receiver name.
        String sReceiver = messageView.getToTextField().getText();
        if (sReceiver == null || sReceiver.length() == 0) {
            JOptionPane.showMessageDialog(messageWindow,
                    "Please complete the 'To:' field!");
            return;
        }

        // Extra-PM email?
        int n = sReceiver.indexOf('@');
        if (n >= 1 && n <= sReceiver.length() - 2) {
            sendExtraPm();
        } else {
            // The receiver must be a user or a user attribute. Ask the engine
            // to
            // check this and get the list of users.
            HashSet recipients = sysCaller.getEmailRecipients(sReceiver);
            if (recipients == null) {
                JOptionPane.showMessageDialog(messageWindow,
                        sysCaller.getLastError());
                return;
            }
            sendIntraPm(recipients);
        }
        grantor.displayOutboxMsgs();
    }

    private static int countLines(JTextArea textArea)
    {
    	AttributedString text = new AttributedString(textArea.getText());
    	FontRenderContext frc = textArea.getFontMetrics(textArea.getFont()).getFontRenderContext();
    	AttributedCharacterIterator charIt = text.getIterator();
    	LineBreakMeasurer lbm = new LineBreakMeasurer(charIt, frc);
    	float formatWidth = (float) textArea.getSize().width;
    	lbm.setPosition(charIt.getBeginIndex());
    	
    	int numLines =0;
    	while (lbm.getPosition()<charIt.getEndIndex())
    	{
    		lbm.nextLayout(formatWidth);
    		numLines++;
    	}	
    	return numLines;
    }
    
    //TODO: Commenting this out because it was not working
    //i will be replacing this with the originial copy and verfy it works
    //later i will alter this to make it work as well....
    private void sendExtraPm() {
        String sReceiver = messageView.getToTextField().getText();//toField.getText();

        // The sender is the current user of this session.
        String sSender = sUserName;
        System.out.println("Sender is " + sSender);
        
        if (sSender == null) {
          JOptionPane.showMessageDialog(messageWindow, "Could not determine the sender!");
          return;
        }
            
        // The subject.
        String sSubject = messageView.getSubjectTextField().getText();//subjField.getText();
        if (sSubject == null || sSubject.length() == 0) sSubject = "None";

        // Get the sender's HOME container name.
        String sHome = sysCaller.getNameOfContainerWithProperty("homeof=" + sSender);
        if (sHome == null) {
          JOptionPane.showMessageDialog(messageWindow, "Could not find the sender's HOME container!");
          return;
        }

        // Get the sender's OUTBOX container name.
        String sOutbox = sysCaller.getNameOfContainerWithProperty("outboxof=" + sSender);
        if (sOutbox == null) {
          JOptionPane.showMessageDialog(messageWindow, "Could not find the sender's OUTBOX container!");
          return;
        }
        
        // The attached object.
        String sAttached = messageView.getObjectTextField().getText();//attField.getText();
        if (sAttached.length() == 0) sAttached = null;

        // Create an object in the sender's HOME that contains the message.
        String sMsgObjName = grantor.generateRandomName();
        String sContainers = "b|" + sHome;
        String sMsgObjClass = "File";
        String sMsgObjType = "eml";
        String sPerms = "File read,File write";
        String sMsgObjHandle = sysCaller.createObject3(sMsgObjName, sMsgObjClass,
          sMsgObjType, sContainers, sPerms, sSender, sReceiver, sSubject, sAttached);
        if (sMsgObjHandle == null) {
          JOptionPane.showMessageDialog(messageView, sysCaller.getLastError());
          return;
        }

        // Write the message as the new object's content.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("From: " + sSender);
        pw.println("To: " + sReceiver);
        pw.println("Subject: " + sSubject);
        pw.println();
        
        String sMsg = messageView.getMessageTextArea().getText();//msgArea.getText();
        //int nl = countLines(messageView.getMessageTextArea());//.get.getLineCount();
        sMsg="hi \n all";
        try {
        	sMsg.replaceAll("\n", " ");
        	pw.print(sMsg);
          /*for (int i = 0; i < nl; i++) {
        	int sLineStart = msgArea.getLineStartOffset(i);
            int sLineEnd = msgArea.getLineEndOffset(i);
            System.out.println("Line " + i + " starts at " + sLineStart +
              " ends at " + sLineEnd);
            // If it's not a line:
            if (sLineStart > sLineEnd - 1) continue;
            // If the line ends with nl, extract the line without the last char.
            // Otherwise, include the last char in line.
            String sLine;
            if (sMsg.charAt(sLineEnd - 1) == '\n')
              sLine = sMsg.substring(sLineStart, sLineEnd - 1);
            else
              sLine = sMsg.substring(sLineStart, sLineEnd);
            pw.println(sLine);
          }*/
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println(e.getMessage());
        }
        pw.close();
        byte[] buf;
        
        try {
          buf = baos.toByteArray();
          int ret = sysCaller.writeObject3(sMsgObjHandle, buf);
          if (ret < 0) {
            JOptionPane.showMessageDialog(messageWindow, sysCaller.getLastError());
            return;
          }
        } catch (Exception e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(messageWindow, "Exception while saving message content: " + e.getMessage());
          return;
        }

        // Grant the "exporter" user (through its Exporter name attribute)
        // the permission "File read" on the new message object.
        Packet res = (Packet)sysCaller.setPerms("Exporter", null, null,
          null, null, "File read", sMsgObjName, SysCaller.PM_NODE_OATTR, "no");
        if (res.hasError()) {
          JOptionPane.showMessageDialog(messageWindow, res.getErrorMessage());
          return;
        }
        
        // If the message has an attached object, grant the exporter the permission
        // "File read" on it.
        if (sAttached != null) {
          res = (Packet)sysCaller.setPerms("Exporter", null, null, null, null,
            "File read", sAttached, SysCaller.PM_NODE_OATTR, "no");
          if (res.hasError()) {
            JOptionPane.showMessageDialog(messageWindow, res.getErrorMessage());
            return;
          }
        }
        
        // Tell the exporter to send the object and the attachment, if any.
        try {
          Packet cmd = CommandUtil.makeCmd("sendMessage", sSessId, sMsgObjName,
            (sAttached == null)? "" : sAttached,
            sSender, sReceiver, sSubject);

          res = exporterClient.sendReceive(cmd, null);
          if (res.hasError()) {
            JOptionPane.showMessageDialog(messageWindow, res.getErrorMessage());
          }
        } catch (Exception e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog(messageWindow, e.getMessage());
        }
        
        res = (Packet)sysCaller.deleteOpsetsBetween("Exporter", sMsgObjName);
        if (res.hasError()) {
          JOptionPane.showMessageDialog(messageWindow, res.getErrorMessage());
        }
        if (sAttached != null) {
          res = (Packet)sysCaller.deleteOpsetsBetween("Exporter", sAttached);
          if (res.hasError()) {
            JOptionPane.showMessageDialog(messageWindow, res.getErrorMessage());
          }
        }
        
        // Assign the msg object to the sender's OUTBOX.
        boolean bRes = sysCaller.assignObjToOutboxOf(sMsgObjName, sSender);
        if (!bRes) {
          JOptionPane.showMessageDialog(messageWindow,
            "Error assigning the message to the sender's OUTBOX: " +
            sysCaller.getLastError());
          return;
        }
        
        // Deassign the msg object from the sender's HOME.
        bRes = sysCaller.deassignObjFromHomeOf(sMsgObjName, sSender);
        if (!bRes) {
          JOptionPane.showMessageDialog(messageWindow,
            "Error deleting the assignment message ---> sender's HOME: " +
            sysCaller.getLastError());
          return;
        }    
      }
  /*  private void sendExtraPm() {
        String sReceiver = messageView.getToTextField().getText();

        // The sender is the current user of this session.
        String sSender = sUserName;
        System.out.println("Sender is " + sSender);

        if (sSender == null) {
            JOptionPane.showMessageDialog(messageWindow,
                    "Could not determine the sender!");
            return;
        }

        // The subject.
        String sSubject = messageView.getSubjectTextField().getText();
        if (sSubject == null || sSubject.length() == 0) {
            sSubject = "None";
        }

        // Get the sender's HOME container name.
        String sHome = sysCaller.getNameOfContainerWithProperty("homeof=" + sSender);
        if (sHome == null) {
            JOptionPane.showMessageDialog(messageWindow,
                    "Could not find the sender's HOME container!");
            return;
        }

        // Get the sender's OUTBOX container name.
        String sOutbox = sysCaller.getNameOfContainerWithProperty("outboxof=" + sSender);
        if (sOutbox == null) {
            JOptionPane.showMessageDialog(messageWindow,
                    "Could not find the sender's OUTBOX container!");
            return;
        }

        // The attached object.
        String sAttached = messageView.getObjectTextField().getText();
        if (sAttached.length() == 0) {
            sAttached = null;
        }

        // Create an object in the sender's HOME that contains the message.
        String sMsgObjName = grantor.generateRandomName();
        String sContainers = "b|" + sHome;
        String sMsgObjClass = "File";
        String sMsgObjType = "eml";
        String sPerms = "File read,File write";
        String sMsgObjHandle = sysCaller.createObject3(sMsgObjName,
                sMsgObjClass, sMsgObjType, sContainers, sPerms, sSender,
                sReceiver, sSubject, sAttached);
        if (sMsgObjHandle == null) {
            JOptionPane.showMessageDialog(messageWindow,
                    sysCaller.getLastError());
            return;
        }

        ByteArrayOutputStream baos = printToByteArray(sReceiver, sSender,
                sSubject);
        byte[] buf;

        try {
            buf = baos.toByteArray();
            int ret = sysCaller.writeObject3(sMsgObjHandle, buf);
            if (ret < 0) {
                JOptionPane.showMessageDialog(messageWindow,
                        sysCaller.getLastError());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    messageWindow,
                    "Exception while saving message content: "
                    + e.getMessage());
            return;
        }

        // Grant the "exporter" user (through its Exporter name attribute)
        // the permission "File read" on the new message object.
        Packet res = (Packet) sysCaller.setPerms("Exporter", null, null, null,
                null, "File read", sMsgObjName, SysCaller.PM_NODE_OATTR, "no");
        if (res.hasError()) {
            JOptionPane.showMessageDialog(messageWindow, res.getErrorMessage());
            return;
        }

        // If the message has an attached object, grant the exporter the
        // permission
        // "File read" on it.
        if (sAttached != null) {
            res = (Packet) sysCaller.setPerms("Exporter", null, null, null, null, "File read",
                    sAttached, SysCaller.PM_NODE_OATTR, "no");
            if (res.hasError()) {
                JOptionPane.showMessageDialog(messageWindow,
                        res.getErrorMessage());
                return;
            }
        }

        // Tell the exporter to send the object and the attachment, if any.
        try {
            Packet cmd = CommandUtil.makeCmd("sendMessage", sSessId, sMsgObjName,
                    (sAttached == null) ? "" : sAttached, sSender, sReceiver,
                    sSubject);
            res = exporterClient.sendReceive(cmd, null);
            if (res.hasError()) {
            	
                JOptionPane.showMessageDialog(messageWindow,
                        res.getErrorMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            //TODO: an error occurs here and an empty message is displayed (e)
            //comment out for now... could be due to network problem (N0 access)
            //JOptionPane.showMessageDialog(messageWindow, e.getMessage());

        }

        res = (Packet) sysCaller.deleteOpsetsBetween("Exporter", sMsgObjName);
        if (res.hasError()) {
            JOptionPane.showMessageDialog(messageWindow, res.getErrorMessage());
        }
        if (sAttached != null) {
            res = (Packet) sysCaller.deleteOpsetsBetween("Exporter", sAttached);
            if (res.hasError()) {
               JOptionPane.showMessageDialog(messageWindow,
                       res.getErrorMessage());
          
            }
        }

        // Assign the msg object to the sender's OUTBOX.
        boolean bRes = sysCaller.assignObjToOutboxOf(sMsgObjName, sSender);
        if (!bRes) {
            JOptionPane.showMessageDialog(messageWindow,
                    "Error assigning the message to the sender's OUTBOX: "
                    + sysCaller.getLastError());
            return;
        }

        // Deassign the msg object from the sender's HOME.
        bRes = sysCaller.deassignObjFromHomeOf(sMsgObjName, sSender);
        if (!bRes) {
            JOptionPane.showMessageDialog(messageWindow,
                    "Error deleting the assignment message ---> sender's HOME: "
                    + sysCaller.getLastError());
            return;
        }
        JOptionPane.showMessageDialog(messageWindow, EXTERNAL_SEND_SUCCESSFUL_MSG,
                Grantor.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }
*/
    // Grant permissions on or send intra-pm message and/or object.
    private void sendIntraPm(HashSet recipients) {
        boolean bRes;
        boolean bCopy = messageView.getCopyObjectCheckBox().isSelected();
        boolean bWrite = messageView.getWriteCheckBox().isSelected();

        String sReceiver = messageView.getToTextField().getText();

        // The sender is the current user of this session.
        String sSender = sUserName;
        if (sSender == null) {
            JOptionPane.showMessageDialog(messageWindow,
                    "Could not determine the sender!");
            return;
        }

        // The subject.
        String sSubject = messageView.getSubjectTextField().getText();
        if (sSubject == null || sSubject.length() == 0) {
            sSubject = "None";
        }

        // Get the sender's HOME container name.
        String sHome = sysCaller.getNameOfContainerWithProperty("homeof=" + sSender);
        if (sHome == null) {
            JOptionPane.showMessageDialog(messageWindow,
                    "Could not find the sender's HOME container!");
            return;
        }

        // Get the sender's OUTBOX container name.
        String sOutbox = sysCaller.getNameOfContainerWithProperty("outboxof=" + sSender);
        if (sOutbox == null) {
            JOptionPane.showMessageDialog(messageWindow,
                    "Could not find the sender's OUTBOX container!");
            return;
        }

        // The attached object.
        String sAttached = messageView.getObjectTextField().getText();
        if (sAttached.length() == 0) {
            sAttached = null;
        }

        // The message.
        String sMsg = messageView.getMessageTextArea().getText();
        if (sMsg.length() == 0) {
            sMsg = null;
        }

        // The confinement container.
        String sConfContainer = null;
        if (messageView.getConfineToCheckBox().isSelected()) {
            sConfContainer = (String) messageView.getConfineToComboBox().getSelectedItem();
            if (sConfContainer.length() == 0) {
                JOptionPane.showMessageDialog(messageWindow,
                        "No confinement container selected!");
                return;
            }
        }

        // If the grantor specified so, copy the attached.
        if (sAttached != null && bCopy) {
            String sAttCopy = sysCaller.copyObject(sAttached);
            if (sAttCopy == null) {
                JOptionPane.showMessageDialog(messageWindow,
                        sysCaller.getLastError());
                return;
            }
            sAttached = sAttCopy;
        }

        // If the grantor wants to also send a message, create the message
        // object
        // in the confined container if one was specified, otherwise in the
        // grantor's HOME.
        String sMsgObjName = null;

        if (sMsg != null) {
            // Create the object for the message.
            sMsgObjName = grantor.generateRandomName();
            String sContainers;
            if (sConfContainer == null) {
                sContainers = "b|" + sHome;
            } else {
                sContainers = "b|" + sConfContainer;
            }
            String sMsgObjClass = "File";
            String sMsgObjType = "eml";
            String sPerms = "File read,File write";
            String sMsgObjHandle = sysCaller.createObject3(sMsgObjName,
                    sMsgObjClass, sMsgObjType, sContainers, sPerms, sSender,
                    sReceiver, sSubject, sAttached);
            if (sMsgObjHandle == null) {
                JOptionPane.showMessageDialog(messageWindow,
                        sysCaller.getLastError());
                return;
            }

            // Write the message to the object just created.
            // The lines in the JTextarea widget are terminated by \n (0x0A).
            // Note that println() on Windows terminates the line with \r\n
            // (0x0D0A).
            ByteArrayOutputStream baos = printToByteArray(sReceiver, sSender,
                    sSubject);
            byte[] buf;

            try {
                buf = baos.toByteArray();
                int ret = sysCaller.writeObject3(sMsgObjHandle, buf);
                if (ret < 0) {
                    JOptionPane.showMessageDialog(messageWindow,
                            sysCaller.getLastError());
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        messageWindow,
                        "Exception while saving message content: "
                        + e.getMessage());
                return;
            }

            messageWindow.setTitle("Composer - " + sMsgObjName);

            // Assign the message object to the sender's OUTBOX.
            bRes = sysCaller.assignObjToOutboxOf(sMsgObjName, sSender);
            if (!bRes) {
                String sErr = sysCaller.getLastError();
                JOptionPane.showMessageDialog(messageWindow,
                        "Error assigning the message to the sender's OUTBOX: "
                        + sErr);
                return;
            }


        }

        // Send the message object and the attachment to each recipient (assign
        // them to the recipient's INBOX). Note that both message and attachment
        // could be missing.
        // In addition, confine the attachment to the specified container, if
        // any.
        Iterator hsiter = recipients.iterator();
        while (hsiter.hasNext()) {
            String sRecipient = (String) hsiter.next();

            // If the msg object exists, assign it to the INBOX:
            if (sMsgObjName != null) {
                bRes = sysCaller.assignObjToInboxOf(sMsgObjName, sRecipient);
                if (!bRes) {
                    String sErr = sysCaller.getLastError();
                    JOptionPane.showMessageDialog(messageWindow,
                            "Error assigning the message to the recipient's INBOX: "
                            + sErr);
                    return;
                }
            }

            // If the attachment exists, confine it if the confinement container
            // exists,...
            if (sAttached == null || sAttached.length() == 0) {
                continue;
            }
            if (sConfContainer != null) {
                bRes = sysCaller.assignObjToContainer(sAttached, sConfContainer);
                if (!bRes) {
                    String sErr = sysCaller.getLastError();
                    if (!sErr.startsWith("Already")) {
                        JOptionPane.showMessageDialog(messageWindow,
                                "Error assigning the attachment to the confinement container: "
                                + sErr);
                        return;
                    }
                }
            }

            // ... then assign it to the INBOX or rwINBOX.
            if (bWrite) {
                bRes = sysCaller.assignObjToWinboxOf(sAttached, sRecipient);
            } else {
                bRes = sysCaller.assignObjToInboxOf(sAttached, sRecipient);
            }
            if (!bRes) {
                String sErr = sysCaller.getLastError();
                if (!sErr.startsWith("Already")) {
                    JOptionPane.showMessageDialog(messageWindow,
                            "Error assigning the attachment to the recipient's INBOX: "
                            + sErr);
                    return;
                }
            }
        }

        // If the msg object exists and no confined container was specified,
        // deassign it from the sender's home.
        if (sMsgObjName != null && sConfContainer == null) {
            bRes = sysCaller.deassignObjFromHomeOf(sMsgObjName, sSender);
            if (!bRes) {
                String sErr = sysCaller.getLastError();
                JOptionPane.showMessageDialog(messageWindow,
                        "Error deleting the assignment message -> sender's HOME: "
                        + sErr);
                return;
            }
        }

        JOptionPane.showMessageDialog(messageWindow,
                INTERNAL_SEND_SUCCESSFUL_MSG,
                Grantor.APP_NAME,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private ByteArrayOutputStream printToByteArray(String sReceiver,
            String sSender, String sSubject) {
        // Write the message as the new object's content.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("From: " + sSender);
        pw.println("To: " + sReceiver);
        pw.println("Subject: " + sSubject);
        pw.println();

        String sMsg = messageView.getMessageTextArea().getText();
        sMsg = sMsg.replaceAll("\n", "");
        pw.println(sMsg);
        pw.close();
        return baos;
    }

    private ByteArrayOutputStream printToByteArrayOld(String sReceiver,
            String sSender, String sSubject, String sMsg) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("From: " + sSender);
        pw.println("To: " + sReceiver);
        pw.println("Subject: " + sSubject);
        pw.println();
        /*  This code doesn't work with the new JEditorPane
         *  all it appears to do is get rid of new line characters
         *
        int nl = messageView.getMessageTextArea().getLineCount();
        try {
        for (int i = 0; i < nl; i++) {
        int sLineStart = messageView.getMessageTextArea()
        .getLineStartOffset(i);
        int sLineEnd = messageView.getMessageTextArea()
        .getLineEndOffset(i);
        System.out.println("Line " + i + " starts at " + sLineStart
        + " ends at " + sLineEnd);
        // If it's not a line:
        if (sLineStart > sLineEnd - 1) {
        continue;
        }
        // If the line ends with nl, extract the line without the
        // last char.
        // Otherwise, include the last char in line.
        String sLine;
        if (sMsg.charAt(sLineEnd - 1) == '\n') {
        sLine = sMsg.substring(sLineStart, sLineEnd - 1);
        } else {
        sLine = sMsg.substring(sLineStart, sLineEnd);
        }
        pw.println(sLine);
        }
        } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(
        messageWindow,
        "Exception while saving message content: "
        + e.getMessage());
        return;
        }
         */
        pw.close();
        return baos;
    }

    private void close() {
        messageWindow.setVisible(false);
    }

    private void browseUsers() {
        if (userBrowser == null) {
            userBrowser = new UserBrowser(messageWindow, sysCaller, EGRANT_APP_NAME);
            userBrowser.addUserBrowserListener(this);
            userBrowser.pack();
        }
        userBrowser.setVisible(true);
    }

    private void browseObjects() {
        if (objectBrowser == null) {
            objectBrowser = new ObjectBrowser(messageWindow, sysCaller, EGRANT_APP_NAME, ObjectBrowser.BrowserConfig.HIDE_SELECTED_OBJECT);
            objectBrowser.addObjectBrowserListener(this);
            objectBrowser.pack();
        }
        objectBrowser.setVisible(true);
    }

    /*
     * Ready made action listeners for default actions of this controller.
     */

    /**
	 * @uml.property  name="defaultAction"
	 */
    final ActionListener defaultAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            String errorMessage = String.format(
                    "The action %s is not implemented.",
                    e.getActionCommand());
            JOptionPane.showMessageDialog(messageView, errorMessage);
        }
    };
    /**
     * implement user action borwser
     */
   /* final ActionListener browsUser = new ActionListener(){
    	@Override
    	public void actionPerformed(ActionEvent ae){
    		
    	}
    };*/
    /**
	 * @uml.property  name="sendAction"
	 */
    final ActionListener sendAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            send();
        }
    };
    /**
	 * @uml.property  name="closeAction"
	 */
    final ActionListener closeAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            close();
        }
    };
    /**
	 * @uml.property  name="pasteAction"
	 */
    final ActionListener pasteAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            paste(ae);
        }
    };
    /**
	 * @uml.property  name="browseUsersAction"
	 */
    final ActionListener browseUsersAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            browseUsers();
        }
    };
    /**
	 * @uml.property  name="browseObjectsAction"
	 */
    final ActionListener browseObjectsAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            browseObjects();
        }
    };
    /**
	 * @uml.property  name="editColorsAction"
	 */
    final ActionListener editColorsAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            editColors();
        }
    };
    /**
	 * @uml.property  name="editFontsAction"
	 */
    final ActionListener editFontsAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            editFonts();
        }
    };
    /**
     * 
     */
    final ActionListener confineCheck = new ActionListener(){
    	@Override
    	public void actionPerformed(ActionEvent ae){
    		
    	}
    };
    /*
     *
     * Action command to actionListener mapping
     */
    /**
	 * @uml.property  name="actionMap"
	 * @uml.associationEnd  qualifier="getActionCommand:java.lang.String java.awt.event.ActionListener"
	 */
    @SuppressWarnings("serial")
    Map<String, ActionListener> actionMap =
            new DefaultResponseHashMap<String, ActionListener>(defaultAction) {

                {
                    put(ACTION_GRANT_SEND, sendAction);
                    put(ACTION_CLOSE, closeAction);
                    put(ACTION_PASTE, pasteAction);
                    put(ACTION_BROWSE_USERS, browseUsersAction);
                    put(ACTION_BROWSE_OBJECTS, browseObjectsAction);
                    put(ACTION_EDIT_COLORS, editColorsAction);
                    put(ACTION_EDIT_FONTS, editFontsAction);
                    put(ACTION_CONFINE_CHECK, confineCheck);
                }
            };

    /*
     *
     * All button and menu actions are routed through this actionPerformed.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        actionMap.get(e.getActionCommand()).actionPerformed(e);
    }

    @Override
    public void pmNodeSelected(PmNode selectedNode) {
        setAttField(selectedNode);
    }

    @Override
    public void userSelected(PmNode userNode) {
        setToField(userNode);
    }

    void displayMessageView() {
        messageWindow.setVisible(true);
    }
}

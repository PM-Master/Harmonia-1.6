package gov.nist.csd.pm.application.grantor;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.DefaultResponseHashMap;
import gov.nist.csd.pm.common.util.Delegate;
import gov.nist.csd.pm.common.util.swing.ToolBarBuilder;

import javax.mail.*;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import static gov.nist.csd.pm.application.grantor.GrantorIconInfo.*;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class Grantor extends JFrame implements ActionListener,
        ListSelectionListener {
    public static final String APP_NAME = "PM E-mail Client";
    public static final String GRANT_APP_PREFIX = "GRANT";

    //Action messages
    private static final String ACTION_NEW_MESSAGE = "new";
    private static final String ACTION_CHECK_MAIL = "check mail";
    private static final String ACTION_COPY_TO_CLIPBOARD = "copy";
    private static final String ACTION_DELETE_MESSAGE = "delete";
    private static final String ACTION_SHOW_OUTBOX = "outbox";
    private static final String ACTION_SHOW_INBOX = "inbox";
    private static final String ACTION_EXIT = "exit";
    private static final String ACTION_PRINT_MESSAGE = "print";
    private static final String ACTION_ATTACH_TO_MESSAGE = "attach";
    private static final String ACTION_SAVE_ATTACHMENT = "save attachment";
    private static final String ACTION_SAVE_MESSAGE = "save";
    private static final String ACTION_REPLY_MESSAGE = "reply";
    private static final String ACTION_FORWARD_MESSAGE = "forward";
    private static final String DEFAULT_ACTION = "__DEFAULT__";
    
    static {
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
    }
    public static final String PM_FAILURE = "err ";
    public static final String PM_SUCCESS = "ok  ";
    public static final String PM_CMD = "cmd ";
    public static final String PM_EOC = "eoc ";
    public static final String PM_ARG = "arg ";
    public static final String PM_SEP = "sep ";
    public static final String PM_DATA = "data";
    public static final String PM_EOD = "eod ";
    public static final String PM_BYE = "bye ";
    public static final String PM_FIELD_DELIM = ":";
    public static final String PM_TERMINATOR = ".";
    public static final String PM_ALT_FIELD_DELIM = "|";
    public static final String PM_ALT_DELIM_PATTERN = "\\|";
    public static final String PM_INBOX = "Inbox";
    public static final String PM_OUTBOX = "Outbox";
    public static final String PM_SERVER = "Server";
    /**
	 * @uml.property  name="sUserName"
	 */
    private String sUserName;
    /**
	 * @uml.property  name="sSessId"
	 */
    private String sSessId;
    /**
	 * @uml.property  name="sProcId"
	 */
    private String sProcId;
    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    private SysCaller sysCaller;
    /**
	 * @uml.property  name="messenger"
	 * @uml.associationEnd  
	 */
    private MessageController messenger;
    /**
	 * @uml.property  name="bDebug"
	 */
    private boolean bDebug;
    //private JTable msgTable;
    //private JTextArea msgArea;
    /**
	 * @uml.property  name="msgTableModel"
	 * @uml.associationEnd  
	 */
    private MsgTableModel msgTableModel;
    /**
	 * @uml.property  name="sComingFrom"
	 */
    private String sComingFrom;
    /**
	 * @uml.property  name="sEmailAddress"
	 */
    private String sEmailAddress;
    /**
	 * @uml.property  name="sEmailServer"
	 */
    private String sEmailServer;
    /**
	 * @uml.property  name="sEmailPass"
	 */
    private String sEmailPass;
    /**
	 * @uml.property  name="msgFolder"
	 * @uml.associationEnd  
	 */
    private Folder msgFolder;
    /**
	 * @uml.property  name="msgs"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    private Message[] msgs;
    /**
	 * @uml.property  name="sNowViewing"
	 */
    private String sNowViewing;
    /**
	 * @uml.property  name="sOpenObjName"
	 */
    private String sOpenObjName;
    /**
	 * @uml.property  name="sOpenObjHandle"
	 */
    private String sOpenObjHandle;
    /**
	 * @uml.property  name="mainView"
	 * @uml.associationEnd  
	 */
    private GrantorMainView mainView = new GrantorMainView();

    public Grantor(int nSimPort, int nExPort, String sSessId, String sProcId,
            boolean bDebug) {
        super(APP_NAME);

        this.sSessId = sSessId;
        this.sProcId = sProcId;
        this.bDebug = bDebug;
        //IOC Candidate
        sysCaller = new SysCallerImpl(nSimPort, sSessId, sProcId, bDebug, GRANT_APP_PREFIX);
        sUserName = sysCaller.getSessionUser();

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                terminate(0);
            }
        });

        msgTableModel = new MsgTableModel();
        mainView.getMasterTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = mainView.getMasterTable().getSelectionModel();
        rowSM.addListSelectionListener(this);
        msgTableModel.addTableModelListener(mainView.getMasterTable());
        mainView.getMasterTable().setModel(msgTableModel);
        mainView.getDetailView().setEditable(false);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(createToolBar(), BorderLayout.NORTH);
        this.getContentPane().add(mainView, BorderLayout.CENTER);

        JMenu msgMenu = createMsgMenu();
        JMenu editMenu = createEditMenu();
        JMenu viewMenu = createViewMenu();

        JMenuBar mb = new JMenuBar();
        mb.add(msgMenu);
        mb.add(editMenu);
        mb.add(viewMenu);
        setJMenuBar(mb);

        displayInboxMsgs();

        Packet userEmailInfo = (Packet) sysCaller.getEmailAcct(sUserName);
        if (userEmailInfo.hasError()) {
            JOptionPane.showMessageDialog(this, userEmailInfo.getErrorMessage());
            return;
        }
        if (userEmailInfo.size() >= 6) {
            if (bDebug) {
                for (int i = 0; i < userEmailInfo.size(); i++) {
                    System.out.println(userEmailInfo.getStringValue(i));
                }
            }
            // Sender information contains:
            // item 0: <user name>:<user id>
            // item 1: <'coming from' name>
            // item 2: <email address>
            // item 3: <incoming server>
            // item 4: <outgoing server>
            // item 5: <account name>.
            sComingFrom = userEmailInfo.getStringValue(1);
            sEmailAddress = userEmailInfo.getStringValue(2);
            sEmailServer = userEmailInfo.getStringValue(3);
        } else {
            JOptionPane.showMessageDialog(this, "User " + sUserName
                    + " has no email account!");
        }

        messenger = new MessageController(this, sysCaller, sUserName, sSessId, nExPort,
                bDebug);
    }

    public String getEmailAddr() {
        return sEmailAddress;
    }

    public String getComingFrom() {
        return sComingFrom;
    }

    private JMenu createEditMenu() {
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic('E');

        JMenuItem item = new JMenuItem("Copy");
        item.addActionListener(this);
        item.setMnemonic('C');
        item.setAccelerator(KeyStroke.getKeyStroke('C', Event.CTRL_MASK, false));
        menu.add(item);

        return menu;
    }

    private JMenu createViewMenu() {
        JMenu menu = new JMenu("View");
        menu.setMnemonic('V');

        JMenuItem item = new JMenuItem("Inbox");
        item.setActionCommand(ACTION_SHOW_INBOX);
        item.addActionListener(this);
        // item.setMnemonic('R');
        // item.setAccelerator(KeyStroke.getKeyStroke('R', Event.CTRL_MASK,
        // false));
        menu.add(item);

        item = new JMenuItem("Outbox");
        item.setActionCommand(ACTION_SHOW_OUTBOX);
        item.addActionListener(this);
        // item.setMnemonic('R');
        // item.setAccelerator(KeyStroke.getKeyStroke('R', Event.CTRL_MASK,
        // false));
        menu.add(item);

        item = new JMenuItem("Check Mail");
        item.setActionCommand(ACTION_CHECK_MAIL);
        item.addActionListener(this);
        // item.setMnemonic('R');
        // item.setAccelerator(KeyStroke.getKeyStroke('R', Event.CTRL_MASK,
        // false));
        menu.add(item);

        return menu;
    }

    private JMenu createMsgMenu() {
        JMenu menu = new JMenu("Message");
        menu.setMnemonic('M');

        JMenuItem item = new JMenuItem("New");
        item.setActionCommand(ACTION_NEW_MESSAGE);
        item.addActionListener(this);
        // item.setMnemonic('N');
        // item.setAccelerator(KeyStroke.getKeyStroke('N', Event.CTRL_MASK,
        // false));
        menu.add(item);

        item = new JMenuItem("Forward");
        item.setActionCommand(ACTION_FORWARD_MESSAGE);
        item.addActionListener(this);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Save");
        item.setActionCommand(ACTION_SAVE_MESSAGE);
        item.addActionListener(this);
        item.setMnemonic('S');
        item.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK, false));
        menu.add(item);

        item = new JMenuItem("Save Attachment");
        item.setActionCommand(ACTION_SAVE_ATTACHMENT);
        item.addActionListener(this);
        item.setMnemonic('A');
        item.setAccelerator(KeyStroke.getKeyStroke('A', Event.CTRL_MASK, false));
        menu.add(item);

        item = new JMenuItem("Create Many Objects");
        item.addActionListener(this);
        menu.add(item);

        item = new JMenuItem("Create Linked Objects");
        item.addActionListener(this);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Delete");
        item.setActionCommand(ACTION_DELETE_MESSAGE);
        item.addActionListener(this);
        item.setMnemonic('D');
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Print");
        item.setActionCommand(ACTION_PRINT_MESSAGE);
        item.addActionListener(this);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Exit");
        item.setActionCommand(ACTION_EXIT);
        item.addActionListener(this);
        item.setMnemonic('x');
        menu.add(item);

        return menu;
    }


    //New method that creates and returns the E-Grant toolbar

    private JToolBar createToolBar() {

        //Tool bar components are specified list of string arrays
        //The first element in an array either specifies the iconpath or the structural component.
        //  If the element is an icon path, then the rest of the array includes an action, tooltip, and optionally, an alt text
        //  else the element is a structural component and the remainder of the array include configuration data.
        //  Currently only one structural component, the strut, requires any configuration data; a single string containing
        //    the width of the strut in integers.
        ToolBarBuilder tbBuilder = new ToolBarBuilder(this);
        tbBuilder
                .addButton(ICON_MAIL_GET, ACTION_CHECK_MAIL, "Checks the server for new mail.")
                .addSpring(20, 100)
                .group()
                    .addButton(ICON_MAIL_INBOX, ACTION_SHOW_INBOX, "Shows the Inbox", new Delegate<AbstractButton>(){
                        @Override
                        public void delegate(AbstractButton button) {
                            ((JToggleButton)button).setSelected(true);
                        }
                    })
                    .addStrut(5)
                    .addButton(ICON_MAIL_OUTBOX, ACTION_SHOW_OUTBOX, "Shows the Outbox")
                .group()
                .addStrut(40)
                .addButton(ICON_MAIL_MESSAGE_NEW, ACTION_NEW_MESSAGE, "Creates an empty message.")
                .addStrut(5)
                .addButton(ICON_MAIL_REPLY, ACTION_REPLY_MESSAGE, "Replies to the currently selected message.")
                .addStrut(5)
                .addButton(ICON_MAIL_FORWARD, ACTION_FORWARD_MESSAGE, "Forwards the currently selected message.")
                .addSpring()
                .addButton(ICON_DOCUMENT_PRINT, ACTION_PRINT_MESSAGE, "Prints the currently selected message.")
                .addDivider()
                .addButton(ICON_EDIT_DELETE, ACTION_DELETE_MESSAGE, "Deletes the currently selected message.")
        ;
        JToolBar toolBar = tbBuilder.getToolBar();
        Border newBorder = new CompoundBorder(new EmptyBorder(5, 0, 0, 0), toolBar.getBorder());
        toolBar.setBorder(newBorder);
        return toolBar;
    }

    

    // Display the list of messages in the user's INBOX.
    private void displayInboxMsgs() {

        if (sOpenObjHandle != null) {
            sysCaller.closeObject(sOpenObjHandle);
        }
        sOpenObjHandle = null;

        Packet msgList = (Packet) sysCaller.getInboxMessages();
        if (msgList.hasError()) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return;
        }
        sNowViewing = PM_INBOX;
        setTitle("E-grant: " + sUserName + " INBOX Messages");
        msgTableModel.clear();
        for (int i = 0; i < msgList.size(); i++) {
            String sLine = msgList.getStringValue(i);
            String[] pieces = sLine.split(PM_ALT_DELIM_PATTERN);
            System.out.println(sLine);
            msgTableModel.addObject(pieces);
        }
        mainView.getDetailView().setText(null);
    }

    // Display the list of messages in the user's OUTBOX.
    public void displayOutboxMsgs() {

        if (sOpenObjHandle != null) {
            sysCaller.closeObject(sOpenObjHandle);
        }
        sOpenObjHandle = null;

        Packet msgList = (Packet) sysCaller.getOutboxMessages();
        if (msgList.hasError()) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return;
        }
        sNowViewing = PM_OUTBOX;
        setTitle("E-grant: " + sUserName + " OUTBOX Messages");
        msgTableModel.clear();
        for (int i = 0; i < msgList.size(); i++) {
            String sLine = msgList.getStringValue(i);
            String[] pieces = sLine.split(PM_ALT_DELIM_PATTERN);
            msgTableModel.addObject(pieces);
        }
        mainView.getDetailView().setText(null);
    }

    // Grant permissions on an object and/or send a message to a group of users.
    // The object name can be null, meaning no object was selected, and in this
    // case
    // just display the messenger UI. Otherwise, display the messenger UI
    // and set the object name as attachment.
    private void composeMsg(String sObjName) {
        if (sObjName != null) {
            messenger.setAttachment(sObjName);
        }
        messenger.displayMessageView();
    }

    private void forwardMsg() {
        if (sNowViewing.equalsIgnoreCase(PM_SERVER)) {
            JOptionPane.showMessageDialog(this,
                    "You cannot forward messages from the server!");
            return;
        }
        if (sNowViewing.equalsIgnoreCase(PM_OUTBOX)) {
            JOptionPane.showMessageDialog(this,
                    "You cannot forward messages from the OUTBOX!");
            return;
        }
        ListSelectionModel lsm = mainView.getMasterTable().getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "You must select a message!");
            return;
        }
        int selindex = lsm.getMaxSelectionIndex();
        String[] msg = (String[]) msgTableModel.get(selindex);
        if (msg == null) {
            JOptionPane.showMessageDialog(this, "You must select a message!");
            return;
        }

        // Open the messenger, like in New, but copy the message content,
        // subject,
        // and attachment if any.
        String sContent = mainView.getDetailView().getText();
        messenger.setContent(sContent);

        messenger.setSubject(msg[5]);
        messenger.setAttachment(msg[1]);

        messenger.displayMessageView();
    }

    private void createLinkedObjects() {
        boolean res = sysCaller.createLinkedObjects();
        if (!res) {
            JOptionPane.showMessageDialog(this,
                    "Could not create the linked objects!");
        }
    }

    private void createManyObjects() {
        final String sInitName = "myObj";
        final int objectCount = 2000;
        final ProgressMonitor progressMonitor = new ProgressMonitor(this, "Creating Many Objects", "Creating Object: 0", 0, objectCount);

        // The home container name.
        final String sHome = sysCaller.getNameOfContainerWithProperty("homeof=" + sUserName);
        if (sHome == null) {
            JOptionPane.showMessageDialog(this, "Could not find " + sUserName
                    + "'s home container!");
            return;
        }

        class BackgroundWorker extends SwingWorker<Boolean, Object> {

            @Override
            protected Boolean doInBackground() throws Exception {
                for (int i = 0; i < objectCount; i++) {
                    if (progressMonitor.isCanceled()) {
                        return false;
                    }
                    final int itemCount = i;
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            progressMonitor.setNote("Created " + objectCount + " objects.");
                            progressMonitor.setProgress(itemCount);
                        }
                    });
                    String sObjName = sInitName + (i + 1);

                    // Create an object for the message in the home container.
                    // String sObjName = Grantor.generateRandomName();
                    String sContainers = "b|" + sHome;
                    String sObjClass = "File";
                    String sObjType = "eml";
                    String sPerms = "File read,File write";

                    String sObjHandle = sysCaller.createObject3(sObjName, sObjClass,
                            sObjType, sContainers, sPerms, null, null, null, null);
                    if (sObjHandle == null) {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                JOptionPane.showMessageDialog(Grantor.this, sysCaller.getLastError());
                            }
                        });
                        return false;
                    }

                    // Assign the message object to the INBOX.
                    boolean bRes = sysCaller.assignObjToInboxOf(sObjName, sUserName);
                    if (!bRes) {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                JOptionPane.showMessageDialog(Grantor.this, sysCaller.getLastError());
                            }
                        });
                        return false;
                    }

                    // Deassign the message object from the user home.
                    bRes = sysCaller.deassignObjFromHomeOf(sObjName, sUserName);
                    if (!bRes) {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                JOptionPane.showMessageDialog(Grantor.this, sysCaller.getLastError());
                            }
                        });

                        return false;
                    }

                }
                return true;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        progressMonitor.close();
                    }
                });
            }
        }

        (new BackgroundWorker()).execute();

    }

    private void saveMsg() {
        if (!sNowViewing.equalsIgnoreCase(PM_SERVER)) {
            JOptionPane.showMessageDialog(this,
                    "You only may save messages from the server!");
            return;
        }

        ListSelectionModel lsm = mainView.getMasterTable().getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "You must select a message!");
            return;
        }
        int selindex = lsm.getMaxSelectionIndex();
        if (selindex >= msgs.length) {
            JOptionPane.showMessageDialog(this, "You must select a message!");
            return;
        }

        Message msg = msgs[selindex];
        System.out.println("You selected message " + msg.toString());

        /*
         * JPanel paramPane = new JPanel(); paramPane.setLayout(new
         * GridBagLayout());
         *
         * JLabel msgLabel = new JLabel("Message object name (optional):");
         * JLabel attLabel = new JLabel("Attachment object name (optional):");
         * JLabel contLabel = new JLabel("Container name (optional):");
         *
         * JTextField msgField = new JTextField(20); JTextField attField = new
         * JTextField(20); JTextField contField = new JTextField(20);
         *
         * constraints.insets = new Insets(5, 0, 0, 0);
         *
         * addComp(paramPane, msgLabel, 0, 0, 1, 1); addComp(paramPane,
         * attLabel, 0, 1, 1, 1); addComp(paramPane, contLabel, 0, 2, 1, 1);
         *
         * addComp(paramPane, msgField, 1, 0, 2, 1); addComp(paramPane,
         * attField, 1, 1, 2, 1); addComp(paramPane, contField, 1, 2, 2, 1);
         *
         * String message =
         * "You may enter names for message, attachment, and container:"; int
         * res = JOptionPane.showOptionDialog(null, new Object[] {message,
         * paramPane}, "Save Message/Attachments", JOptionPane.OK_CANCEL_OPTION,
         * JOptionPane.QUESTION_MESSAGE, null, null, null); if (res ==
         * JOptionPane.CANCEL_OPTION || res == JOptionPane.CLOSED_OPTION)
         * return; String sWarning = "";
         *
         * String sMsgName = msgField.getText(); String sAttName =
         * attField.getText(); String sContName = contField.getText();
         */

        // The message text.
        String sMsgText = getPlainTextMsg(msg, 0);
        byte[] buf = sMsgText.getBytes();

        try {
            // The subject.
            String sSubject = msg.getSubject();

            // The receiver(s).
            // String sReceiver = sUserName;
            Address[] a = msg.getRecipients(Message.RecipientType.TO);
            StringBuffer sb = new StringBuffer();
            if (a != null) {
                for (int j = 0; j < a.length; j++) {
                    sb.append(a[j].toString());
                }
            }
            String sReceiver = sb.toString();

            // The sender (FROM).
            a = msg.getFrom();
            sb = new StringBuffer();
            if (a != null) {
                for (int j = 0; j < a.length; j++) {
                    sb.append(a[j].toString());
                }
            }
            String sSender = sb.toString();

            // Info about the attached file(s).
            String sAttached = getAttOrigName(msg, 0);

            // The home container name.
            String sHome = sysCaller.getNameOfContainerWithProperty("homeof=" + sUserName);
            if (sHome == null) {
                JOptionPane.showMessageDialog(this, "Could not find "
                        + sUserName + "'s home container!");
                return;
            }

            // Create an object for the message in the home container.
            String sObjName = Grantor.generateRandomName();
            String sContainers = "b|" + sHome;
            String sObjClass = "File";
            String sObjType = "eml";
            String sPerms = "File read,File write";

            String sObjHandle = sysCaller.createObject3(sObjName, sObjClass,
                    sObjType, sContainers, sPerms, sSender, sReceiver,
                    sSubject, sAttached);
            if (sObjHandle == null) {
                JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                return;
            }

            int ret = sysCaller.writeObject3(sObjHandle, buf);
            if (ret < 0) {
                JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                return;
            }

            // Assign the message object to the INBOX.
            boolean bRes = sysCaller.assignObjToInboxOf(sObjName, sUserName);
            if (!bRes) {
                JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                return;
            }

            // Deassign the message object from the user home.
            bRes = sysCaller.deassignObjFromHomeOf(sObjName, sUserName);
            if (!bRes) {
                JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
        }
    }

    private void saveAtt() {
        if (!sNowViewing.equalsIgnoreCase(PM_SERVER)) {
            JOptionPane.showMessageDialog(this,
                    "You only may save attachments to messages from the server!");
            return;
        }

        ListSelectionModel lsm = mainView.getMasterTable().getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "You must select a message!");
            return;
        }
        int selindex = lsm.getMaxSelectionIndex();
        if (selindex >= msgs.length) {
            JOptionPane.showMessageDialog(this, "You must select a message!");
            return;
        }

        Message msg = msgs[selindex];
        System.out.println("You selected message " + msg.toString());

        String sFname = getAttOrigName(msg, 0);
        if (sFname == null) {
            JOptionPane.showMessageDialog(this,
                    "The message you selected has no attachment!");
            return;
        }

        String sObjName;
        String sObjType;

        // Get an acceptable file name for the attachment, then split it
        // into object name and object type.
        // "Acceptable": <name>.<ext>, or <name>1.<ext>, <name>2.<ext>,
        // the first one that doesn't exists in the host's repository.
        String sFinalName = sysCaller.findAName(sFname);
        System.out.println("Found name " + sFinalName);
        int ix = sFinalName.lastIndexOf(".");
        if (ix < 0) {
            sObjName = sFinalName;
            sObjType = "";
        } else {
            sObjName = sFinalName.substring(0, ix);
            sObjType = sFinalName.substring(ix + 1);
        }
        System.out.println("Found objname=" + sObjName + " and objtype="
                + sObjType);

        // The INBOX container name.
        String sInbox = sysCaller.getNameOfContainerWithProperty("inboxof=" + sUserName);
        if (sInbox == null) {
            JOptionPane.showMessageDialog(this, "Could not find " + sUserName
                    + "'s INBOX container!");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "The attachment will be saved as object " + sObjName);

        // The home container name.
        String sHome = sysCaller.getNameOfContainerWithProperty("homeof=" + sUserName);
        if (sHome == null) {
            JOptionPane.showMessageDialog(this, "Could not find " + sUserName
                    + "'s home container!");
            return;
        }

        // Create an object in the home that contains the message.
        String sContainers = "b|" + sHome;
        String sObjClass = "File";
        String sPerms = "File read,File write";

        String sObjHandle = sysCaller.createObject3(sObjName, sObjClass,
                sObjType, sContainers, sPerms, null, null, null, null);
        if (sObjHandle == null) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return;
        }

        if (!writeAttToObject(sObjHandle, msg, 0)) {
            JOptionPane.showMessageDialog(this,
                    "Error trying to save the attachment (look at stdin/stderr)");
            return;
        }

        // Assign the attachment object to the INBOX container.
        if (!sysCaller.assignObjToInboxOf(sObjName, sUserName)) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return;
        }

        // Deassign the attachment object from the home container.
        if (!sysCaller.deassignObjFromHomeOf(sObjName, sUserName)) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return;
        }
        JOptionPane.showMessageDialog(this,
                "The attachment has been successfully saved as object "
                + sObjName);
    }

    private void attachToMsg() {
    }

    private void terminate(int exitCode) {
        sysCaller.exitProcess(sProcId);
        System.exit(exitCode);
    }

    private void checkMail() {

        if (sOpenObjHandle != null) {
            sysCaller.closeObject(sOpenObjHandle);
        }
        sOpenObjHandle = null;

        System.out.println("Check mail called by user " + sUserName);
        if (sEmailServer == null) {
            JOptionPane.showMessageDialog(this,
                    "No email account found for user " + sUserName);
            return;
        }

        // Ask for the password if not already entered.
        if (sEmailPass == null) {
            char[] bPass = null;
            JPasswordField passField = new JPasswordField();
            String message = "Please enter password for user " + sUserName;
            while (true) {
                int result = JOptionPane.showOptionDialog(null, new Object[]{
                            message, passField}, "Password",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (result == JOptionPane.CANCEL_OPTION) {
                    return;
                }
                bPass = passField.getPassword();
                if (bPass != null && bPass.length > 0) {
                    break;
                }
            }
            sEmailPass = new String(bPass);
        }

        String sProtocol = "imap";

        System.out.println("Calling getMessages for " + sProtocol + ", "
                + sEmailServer + ", " + sUserName);

        msgs = getMessages(sProtocol, sEmailServer, sUserName, sEmailPass);
        if (msgs == null && sLastErr != null) {
            JOptionPane.showMessageDialog(this, sLastErr);
            return;
        }

        sNowViewing = PM_SERVER;
        setTitle("E-grant: " + sUserName + "'s Messages on SERVER");
        msgTableModel.clear();

        if (msgs == null) {
            return;
        }

        try {
            for (int msgix = 0; msgix < msgs.length; msgix++) {
                String[] msgElems = new String[6];
                // Message name, which in this case (message on the server) is
                // the
                // message index.
                msgElems[0] = String.valueOf(msgix + 1);
                // Attachment.
                msgElems[1] = getAttOrigName(msgs[msgix], 0);
                // From.
                Address[] a = msgs[msgix].getFrom();
                StringBuffer sb = new StringBuffer();
                if (a != null) {
                    for (int j = 0; j < a.length; j++) {
                        sb.append(a[j].toString());
                    }
                }
                msgElems[2] = sb.toString();
                // To.
                a = msgs[msgix].getRecipients(Message.RecipientType.TO);
                sb = new StringBuffer();
                if (a != null) {
                    for (int j = 0; j < a.length; j++) {
                        sb.append(a[j].toString());
                    }
                }
                msgElems[3] = sb.toString();
                // Date.
                msgElems[4] = msgs[msgix].getSentDate().toString();
                // Subject.
                msgElems[5] = msgs[msgix].getSubject();

                msgTableModel.addObject(msgElems);
            }
            mainView.getDetailView().setText(null);
        } catch (Exception exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(this, "Messaging exception!");
            return;
        }
    }
    static String sLastErr = null;

    private Message[] getMessages(String sProtocol, String sServer,
            String sUser, String sPassword) {

        sLastErr = null;

        try {
            Properties props = System.getProperties();
            Session session = Session.getInstance(props, null);
            // session.setDebug(true);
            session.setDebug(false);
            Store store = session.getStore(sProtocol);
            store.connect(sServer, -1, sUser, sPassword);

            msgFolder = store.getDefaultFolder();
            if (msgFolder == null) {
                sLastErr = "No default folder!";
                return null;
            }
            msgFolder = msgFolder.getFolder("INBOX");
            if (msgFolder == null) {
                sLastErr = "Invalid folder INBOX!";
                return null;
            }
            try {
                msgFolder.open(Folder.READ_WRITE);
            } catch (MessagingException exc) {
                msgFolder.open(Folder.READ_ONLY);
            }

            int nTotalMsgs = msgFolder.getMessageCount();

            System.out.println("Number of messages is " + nTotalMsgs);

            if (nTotalMsgs == 0) {
                msgFolder.close(false);
                store.close();
                return null;
            }

            Message[] msgs = msgFolder.getMessages();
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            msgFolder.fetch(msgs, fp);
            return msgs;
        } catch (Exception e) {
            e.printStackTrace();
            sLastErr = "Exception while getting messages: " + e.getMessage()
                    + "!";
            return null;
        }
    }

    private String getPlainTextMsg(Part p, int nLevel) {
        try {
            String s = null;
            String sCtType = p.getContentType();
            try {
                printIndented(
                        "CONTENT-TYPE: "
                        + (new ContentType(sCtType)).toString(), nLevel);
            } catch (ParseException pex) {
                printIndented("BAD CONTENT-TYPE: " + sCtType, nLevel);
                return s;
            }
            String sFileName = p.getFileName();
            if (sFileName != null) {
                printIndented("FILENAME: " + sFileName, nLevel);
            }

            if (p.isMimeType("text/plain")) {
                printIndented("This is plain text", nLevel);
                System.out.println((String) p.getContent());
                return (String) p.getContent();
            } else if (p.isMimeType("multipart/*")) {
                printIndented("This is a Multipart", nLevel);
                Multipart mp = (Multipart) p.getContent();
                nLevel++;
                int nCount = mp.getCount();
                for (int i = 0; i < nCount; i++) {
                    s = getPlainTextMsg(mp.getBodyPart(i), nLevel);
                    if (s != null) {
                        return s;
                    }
                }
                nLevel--;
                return s;
            } else {
                return s;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Exception trying to display message content!");
            return null;
        }
    }

    // Get the original name of the attached file, if any.
    private String getAttOrigName(Part p, int nLevel) {
        try {
            String sFileName = null;
            String sCtType = p.getContentType();
            try {
                printIndented(
                        "CONTENT-TYPE: "
                        + (new ContentType(sCtType)).toString(), nLevel);
            } catch (ParseException pex) {
                printIndented("BAD CONTENT-TYPE: " + sCtType, nLevel);
            }
            sFileName = p.getFileName();
            if (sFileName != null) {
                printIndented("FILENAME: " + sFileName, nLevel);
                return sFileName;
            }

            if (p.isMimeType("text/plain")) {
                printIndented("This is plain text", nLevel);
                System.out.println((String) p.getContent());
            } else if (p.isMimeType("multipart/*")) {
                printIndented("This is a Multipart", nLevel);
                Multipart mp = (Multipart) p.getContent();
                nLevel++;
                int nCount = mp.getCount();
                for (int i = 0; i < nCount; i++) {
                    sFileName = getAttOrigName(mp.getBodyPart(i), nLevel);
                    if (sFileName != null) {
                        return sFileName;
                    }
                }
                nLevel--;
            } else if (p.isMimeType("message/rfc822")) {
                printIndented("This is a Nested Message", nLevel);
                nLevel++;
                sFileName = getAttOrigName((Part) p.getContent(), nLevel);
                if (sFileName != null) {
                    return sFileName;
                }
                nLevel--;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Exception trying to display message content!");
            return null;
        }
    }

    private boolean writeAttToObject(String sObjHandle, Part p, int nLevel) {
        try {
            String sFileName = null;
            String sCtType = p.getContentType();
            try {
                printIndented(
                        "CONTENT-TYPE: "
                        + (new ContentType(sCtType)).toString(), nLevel);
            } catch (ParseException pex) {
                printIndented("BAD CONTENT-TYPE: " + sCtType, nLevel);
            }
            sFileName = p.getFileName();
            if (sFileName != null) {
                printIndented("FILENAME: " + sFileName, nLevel);

                // Save the attached file as a PM object.
                String sDispo = p.getDisposition();
                if (sDispo == null || sDispo.equalsIgnoreCase(Part.ATTACHMENT)) {
                    reallyWriteAttToObject(sObjHandle, p);
                    return true;
                }
                return false;
            }

            if (p.isMimeType("text/plain")) {
                printIndented("This is plain text", nLevel);
                System.out.println((String) p.getContent());
            } else if (p.isMimeType("multipart/*")) {
                printIndented("This is a Multipart", nLevel);
                Multipart mp = (Multipart) p.getContent();
                nLevel++;
                int nCount = mp.getCount();
                for (int i = 0; i < nCount; i++) {
                    boolean b = writeAttToObject(sObjHandle, mp.getBodyPart(i),
                            nLevel);
                    if (b) {
                        return b;
                    }
                }
                nLevel--;
            } else if (p.isMimeType("message/rfc822")) {
                printIndented("This is a Nested Message", nLevel);
                nLevel++;
                boolean b = writeAttToObject(sObjHandle, (Part) p.getContent(),
                        nLevel);
                if (b) {
                    return b;
                }
                nLevel--;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Exception trying to display message content!");
            return false;
        }
    }

    private String reallyWriteAttToObject(String sObjHandle, Part p) {
        try {

            int buflen = p.getSize();
            byte[] buf = new byte[buflen];
            InputStream is = p.getInputStream();

            int c;
            int n;

            int blocklen = 16384;
            int offset = 0;

            for (;;) {
                n = is.read(buf, offset, blocklen);
                if (n == -1) {
                    break;
                }
                offset += n;
            }

            int ret = sysCaller.writeObject3(sObjHandle, buf);
            if (ret < 0) {
                JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                return null;
            }
            return sObjHandle;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void printIndented(String s, int nLevel) {
        for (int i = 0; i < 2 * nLevel; i++) {
            System.out.print(" ");
        }
        System.out.print(nLevel + ">");
        System.out.println(s);
    }

    // Delete a message from the current user's INBOX or OUTBOX or from the
    // server's INBOX.
    private void deleteMsg() {
        // Deleting messages from the SERVER.
        if (sNowViewing.equalsIgnoreCase(PM_SERVER)) {
            ListSelectionModel lsm = mainView.getMasterTable().getSelectionModel();
            if (lsm.isSelectionEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "You must select a message!");
                return;
            }
            int selindex = lsm.getMaxSelectionIndex();
            if (selindex >= msgs.length) {
                JOptionPane.showMessageDialog(this,
                        "You must select a message!");
                return;
            }

            try {
                msgs[selindex].setFlag(Flags.Flag.DELETED, true);
                msgFolder.expunge();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Exception while deleting a message: "
                        + e.getMessage());
            }
            checkMail();
            return;
        }

        // Deleting messages from the INBOX or OUTBOX.
        ListSelectionModel lsm = mainView.getMasterTable().getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "You must select a message!");
            return;
        }
        int selindex = lsm.getMaxSelectionIndex();
        String[] msg = (String[]) msgTableModel.get(selindex);
        if (msg == null) {
            JOptionPane.showMessageDialog(this, "You must select a message!");
            return;
        }

        JOptionPane.showMessageDialog(this, "Trying to delete message "
                + msg[0] + " from " + sUserName + "'s INBOX/OUTBOX!");
        if (sNowViewing.equalsIgnoreCase(PM_INBOX)) {
            boolean bRes = sysCaller.deassignObjFromInboxOf(msg[0], sUserName);
            if (!bRes) {
                String sErr = sysCaller.getLastError();
                JOptionPane.showMessageDialog(this, "Error deleting message: "
                        + sErr);
                return;
            }
            displayInboxMsgs();
        } else {
            boolean bRes = sysCaller.deassignObjFromOutboxOf(msg[0], sUserName);
            if (!bRes) {
                String sErr = sysCaller.getLastError();
                JOptionPane.showMessageDialog(this, "Error deleting message: "
                        + sErr);
                return;
            }
            displayOutboxMsgs();
        }
    }

    private void copy(ActionEvent e) {
        if (!sysCaller.copyToClipboard(sOpenObjHandle, null)) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return;
        }

        Action actions[] = mainView.getDetailView().getActions();
        Action copyAction = null;
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            String sName = (String) a.getValue(Action.NAME);
            if (sName.equals(DefaultEditorKit.copyAction)) {
                copyAction = a;
                break;
            }
        }
        if (copyAction == null) {
            JOptionPane.showMessageDialog(this, "No copy action found!");
            return;
        }
        copyAction.actionPerformed(e);

        /*
         * Clipboard clipboard =
         * Toolkit.getDefaultToolkit().getSystemClipboard(); Transferable
         * clipData = clipboard.getContents(clipboard); if (clipData == null) {
         * System.out.println("Clipboard content is null!"); return; } if
         * (!clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
         * System.out.println("String flavor not supported!"); return; } try {
         * String s =
         * (String)(clipData.getTransferData(DataFlavor.stringFlavor));
         * System.out.println("Clipboard content is:" + s); } catch (Exception
         * exc) { exc.printStackTrace(); return; }
         */
    }

    private int getMsgIndexInTable(String sObjName) {
        int nRows = msgTableModel.nextEmptyRow();
        for (int i = 0; i < nRows; i++) {
            String s = (String) msgTableModel.getValueAt(i, 0);
            if (sObjName.equalsIgnoreCase(s)) {
                return i;
            }
        }
        return -1;
    }

    // Called when a user launches the Grantor in the -open mode on an object.
    private void findAndDisplayLocalMsg(String sObjName) {
        // Find whether the object is in INBOX or OUTBOX.
        if (sysCaller.isObjInInboxOf(sObjName, sUserName)) {
            System.out.println("Object " + sObjName + " is in the inbox of "
                    + sUserName);
            displayInboxMsgs();
        } else if (sysCaller.isObjInOutboxOf(sObjName, sUserName)) {
            System.out.println("Object " + sObjName + " is in the outbox of "
                    + sUserName);
            displayOutboxMsgs();
        } else {
            System.out.println("Object " + sObjName
                    + " is not in the inbox nor the outbox of " + sUserName);
        }
        ListSelectionModel rowSM = mainView.getMasterTable().getSelectionModel();
        int ix = getMsgIndexInTable(sObjName);
        rowSM.setSelectionInterval(ix, ix);
    }

    // Called to display a message from the INBOX or OUTBOX table.
    private void displayLocalMsg(String sObjName) {
        // Open the object.
        if (sOpenObjHandle != null) {
            sysCaller.closeObject(sOpenObjHandle);
        }
        sOpenObjHandle = sysCaller.openObject3(sObjName, "File read");
        if (sOpenObjHandle == null) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return;
        }
        sOpenObjName = sObjName;

        // Read the object content.
        byte[] buf = sysCaller.readObject3(sOpenObjHandle);
        if (buf == null) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return;
        }

        // Insert the object content into the message area.
        ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0, buf.length);
        InputStreamReader isr = new InputStreamReader(bais);
        BufferedReader in = new BufferedReader(isr);

        // On disk the lines are separated by 0D0A (Windows). In memory
        // (in the JTextArea widget) the lines are separated by 0A (\n).
        String line;
        try {
            mainView.getDetailView().setText(null);
            while ((line = in.readLine()) != null) {
                mainView.getDetailView().append(line);
                mainView.getDetailView().append("\n");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Exception while reading the message!");
            return;
        }

        String sTitle = getTitle();
    }

    private void printMsg() {
        try {
            boolean printComplete = mainView.getDetailView().print();
            if (printComplete) {
            } else {
            }
        } catch (PrinterException pe) {
        }
    }

    // Called when the user selects a message in either INBOX,
    // OUTBOX, or SERVER msg table.
    // Use the message index in the array msgs[] if the message is still
    // on the server (after a checkmail). Use the message name as a PM
    // object if the message is in the INBOX or OUTBOX containers.
    private void displayMsg(int msgindex, String[] msg) {
        if (sNowViewing.equalsIgnoreCase(PM_INBOX)
                || sNowViewing.equalsIgnoreCase(PM_OUTBOX)) {
            // The message is not on the server.
            displayLocalMsg(msg[0]);
        } else if (sNowViewing.equalsIgnoreCase(PM_SERVER)) {
            String s = getPlainTextMsg(msgs[msgindex], 0);
            System.out.println("%%%%%%%%%%%getPlainTextMsg returned:");
            System.out.println(s);
            String sAttached = getAttOrigName(msgs[msgindex], 0);
            System.out.println("%%%%%%%%%%%getAttOrigName returned:");
            System.out.println(sAttached);

            mainView.getDetailView().setText(s);

        } else {
            JOptionPane.showMessageDialog(this,
                    "Unrecognized viewing mode of email messages!");
            return;
        }
    }

    
    /**
	 * @uml.property  name="defaultAction"
	 */
    ActionListener defaultAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent ae) {
            JOptionPane.showMessageDialog(null, "This action is not yet implemented");
        }
    };
    /**
	 * @uml.property  name="grantorActionMap"
	 * @uml.associationEnd  qualifier="getActionCommand:java.lang.String java.awt.event.ActionListener"
	 */
    private Map<String, ActionListener> grantorActionMap = new DefaultResponseHashMap<String, ActionListener>(defaultAction) {

        {
            put(ACTION_NEW_MESSAGE, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    composeMsg(null);
                }
            });
            put(ACTION_FORWARD_MESSAGE, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    forwardMsg();
                }
            });
            put(ACTION_SAVE_MESSAGE, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    saveMsg();
                }
            });
            put(ACTION_SAVE_ATTACHMENT, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    saveAtt();
                }
            });
            put("create many objects", new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    createManyObjects();
                }
            });
            put("create linked objects", new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    createLinkedObjects();
                }
            });
            put(ACTION_ATTACH_TO_MESSAGE, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    attachToMsg();
                }
            });
            put(ACTION_PRINT_MESSAGE, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    printMsg();
                }
            });
            put(ACTION_EXIT, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    terminate(0);
                }
            });
            put(ACTION_SHOW_INBOX, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    displayInboxMsgs();
                }
            });
            put(ACTION_SHOW_OUTBOX, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    displayOutboxMsgs();
                }
            });
            put(ACTION_CHECK_MAIL, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    checkMail();
                }
            });
            put(ACTION_DELETE_MESSAGE, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    deleteMsg();
                }
            });
            put(ACTION_COPY_TO_CLIPBOARD, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    copy(ae);
                }
            });


        }
    };

    public void actionPerformed(ActionEvent e) {
        grantorActionMap.get(e.getActionCommand()).actionPerformed(e);
    }

    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (e.getValueIsAdjusting()) {
            return;
        }
        if (lsm.isSelectionEmpty()) {
            return;
        }
        int selindex = lsm.getMaxSelectionIndex();
        // if (selindex >= msgTableModel.nextEmptyRow()) return;

        String[] msg = (String[]) msgTableModel.get(selindex);
        if (msg == null) {
            return;
        }
        System.out.println("You selected message " + msg[0]);

        // displayMsg will use the message index in the array msgs[]
        // if the message is on the server. It will use the message
        // name (the message is a PM object), i.e., msg[0], if the
        // message is in the INBOX or OUTBOX containers.
        displayMsg(selindex, msg);
    }

    // If the object name is null, do nothing.
    // Otherwise, if bGrant is false, open the object (should be of type .eml).
    // Otherwise, prepare everything for granting permissions to the object
    // (open the messenger, set up the attachment to this object).
    private void processObj(String sObjName, boolean bGrant) {
        // No object specified: do nothing.
        if (sObjName == null) {
            return;
        }

        // Object and open specified: display the object (must be a message).
        if (!bGrant) {
            findAndDisplayLocalMsg(sObjName);
            return;
        }

        // Object and grant specified: launch the messenger, etc.
        composeMsg(sObjName);
    }
    // ////////////////////////////////////////////////////////////////////////////
    static Random myRandom = new Random();

    public static String generateRandomName() {
        byte[] bytes = new byte[2];
        myRandom.nextBytes(bytes);
        return byteArray2HexString(bytes);
    }

    static byte byte2HexDigit(byte n) {
        if (n < 10) {
            return (byte) ('0' + n);
        } else {
            return (byte) ('A' + n - 10);
        }
    }

    static String byteArray2HexString(byte[] inp) {
        byte[] buf = new byte[2 * inp.length];
        int inpix, outix;
        int n;
        byte q, r;

        for (inpix = outix = 0; inpix < inp.length; inpix++) {
            n = inp[inpix] & 0x000000FF;
            q = (byte) (n / 16);
            r = (byte) (n % 16);
            buf[outix++] = byte2HexDigit(q);
            buf[outix++] = byte2HexDigit(r);
        }
        return new String(buf);
    }

    private void printSet(HashSet hs, String caption) {
        if (caption != null && caption.length() > 0) {
            System.out.println(caption);
        }
        Iterator hsiter = hs.iterator();

        System.out.print("{");
        boolean firstTime = true;
        while (hsiter.hasNext()) {
            String sId = (String) hsiter.next();
            if (firstTime) {
                System.out.print(sId);
                firstTime = false;
            } else {
                System.out.print(", " + sId);
            }
        }
        System.out.println("}");
    }
    static String sessid;
    static String pid;
    static int simport;
    static int export;
    static String objname;
    static boolean grant;
    static boolean debug;

    // Create the GUI,
    private static void createGUI() {
        Grantor grantor = new Grantor(simport, export, sessid, pid, debug);
        grantor.pack();
        grantor.setVisible(true);
        grantor.processObj(objname, grant);
    }

    // Arguments on the command line:
    // -session <sessionId> -process <processId> -simport <simulator port>
    // -export <exporter port> {-open|-grant} <objname>
    // <simulator port> is the port where the kernel simulator listens for
    // connections,
    // by default 8081.
    // <exporter port> is the port where the exporter listens for connections,
    // by default 8082.
    // <sessionId> is the id of the session where the grantor is running. It is
    // mandatory.
    // <processId> is the id of the process where the grantor is running. It is
    // mandatory.
    // <objname> is the name of an object the grantor will try to open or send.
    // -open indicates that the object is a message that will be open.
    // -grant indicates that the object will be granted access to.
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-session")) {
                sessid = args[++i];
            } else if (args[i].equals("-process")) {
                pid = args[++i];
            } else if (args[i].equals("-simport")) {
                simport = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-export")) {
                export = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-grant")) {
                grant = true;
            } else if (args[i].equals("-open")) {
                grant = false;
            } else if (args[i].equals("-debug")) {
                debug = true;
            } else {
                objname = args[i];
            }
        }
        if (sessid == null) {
            System.out.println("This application must run within a Policy Machine session!");
            System.exit(-1);
        }
        if (pid == null) {
            System.out.println("This application must run within a Policy Machine process!");
            System.exit(-1);
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                createGUI();
            }
        });
    }
}

package gov.nist.csd.pm.user;

/*
 * SessionManager.java
 *
 * Created by Serban I. Gavrila
 * for KT Consulting Inc.
 */
import static gov.nist.csd.pm.common.constants.GlobalConstants.*;

import com.google.common.base.Supplier;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.graphics.GradientPanel;
import gov.nist.csd.pm.common.graphics.GraphicsUtil;
import gov.nist.csd.pm.common.model.PropertyChangeObservable;
import gov.nist.csd.pm.common.model.PropertyChangeObservableSupport;
import gov.nist.csd.pm.common.model.PropertyChangeObserver;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;
import gov.nist.csd.pm.common.util.Log;
import gov.nist.csd.pm.common.util.swing.DialogUtils;
import gov.nist.csd.pm.common.util.swing.SwingShortcuts;
import gov.nist.csd.pm.ksim.KernelSimulator;

import javax.swing.*;

import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import static gov.nist.csd.pm.common.net.LocalHostInfo.getLocalHost;
import static gov.nist.csd.pm.common.util.CommandUtil.makeCmd;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class SessionManager extends JFrame implements ActionListener, PropertyChangeObservable {
	
	Log log = new Log(Log.Level.INFO, true);


    private static final String SESSION_TASK_ID = "SESSION_TASK_ID";
    public static final String PM_FAILURE = "err ";
    public static final String PM_SUCCESS = "ok  ";
    public static final String PM_CMD = "cmd ";
    public static final String PM_EOC = "eoc ";
    public static final String PM_ARG = "arg ";
    public static final String PM_SEP = "sep ";
    public static final String PM_DATA = "data";
    public static final String PM_EOD = "eod ";
    public static final String PM_BYE = "bye ";
    public static final String PM_NODE_USER = "u";
    public static final String PM_NODE_USERA = "U";
    public static final String PM_NODE_UATTR = "a";
    public static final String PM_NODE_UATTRA = "A";
    public static final String PM_NODE_POL = "p";
    public static final String PM_NODE_POLA = "P";
    public static final String PM_NODE_OATTR = "b";
    public static final String PM_NODE_OATTRA = "B";
    public static final String PM_NODE_ASSOC = "o";
    public static final String PM_NODE_ASSOCA = "O";
    public static final String PM_NODE_OPSET = "s";
    public static final String PM_NODE_OPSETA = "S";
    public static final String PM_NODE_CONN = "c";
    public static final String PM_NODE_CONNA = "C";
    public static final String PM_OBJ = "o";
    public static final String PM_FIELD_DELIM = ":";
    public static final String PM_TERMINATOR = ".";
    public static final String PM_ALT_FIELD_DELIM = "|";
    public static final String PM_ALT_DELIM_PATTERN = "\\|";
    /**
	 * @uml.property  name="sessionMap"
	 * @uml.associationEnd  qualifier="sId:java.lang.String gov.nist.csd.pm.user.Session"
	 */
    private HashMap sessionMap = null;
    // The SSL client used to talk to the K-Simulator.
    /**
	 * @uml.property  name="simClient"
	 * @uml.associationEnd  
	 */
    private SSLSocketClient simClient;

    /**
	 * @uml.property  name="sLocalHost"
	 */
    private String sLocalHost;
    // The exporter's session and process.
    /**
	 * @uml.property  name="exporterSession"
	 * @uml.associationEnd  
	 */
    private Session exporterSession;
    /**
	 * @uml.property  name="sExporterName"
	 */
    private String sExporterName = "exporter";
    /**
	 * @uml.property  name="exporterPort"
	 */
    private int exporterPort;
    /**
	 * @uml.property  name="configEditor"
	 * @uml.associationEnd  
	 */
    private OsConfigEditor configEditor;
    /**
	 * @uml.property  name="taskBar"
	 * @uml.associationEnd  
	 */
    private OsTaskBar taskBar;
    /**
	 * @uml.property  name="windowMenu"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JMenu windowMenu;
    /**
	 * @uml.property  name="logonMenuItem"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    public JMenuItem logonMenuItem = new JMenuItem("Login");
    /**
	 * @uml.property  name="debugFlag"
	 */
    boolean debugFlag;
    /**
	 * @uml.property  name="currentSessionDialog"
	 * @uml.associationEnd  
	 */
    private Session currentSessionDialog;
    /**
	 * @uml.property  name="desktopDropTarget"
	 * @uml.associationEnd  
	 */
    private DesktopDropTargetListener desktopDropTarget;
    /**
	 * @uml.property  name="applicationManager"
	 * @uml.associationEnd  
	 */
    private ApplicationManager applicationManager;
    /**
	 * @uml.property  name="sExporterSessName"
	 */
    private String sExporterSessName;
    /**
	 * @uml.property  name="sExporterSessId"
	 * @uml.associationEnd  qualifier="this:gov.nist.csd.pm.user.SessionManager gov.nist.csd.pm.user.Session"
	 */
    private String sExporterSessId;
    /**
	 * @uml.property  name="sExporterId"
	 */
    private String sExporterId;
    /**
	 * @uml.property  name="pcos"
	 * @uml.associationEnd  
	 */
    public final PropertyChangeObservableSupport pcos = new PropertyChangeObservableSupport(this);

    public SessionManager(String title, int simulatorPort, int exporterPort,
                          boolean debugFlag) {
        super(title);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        setIconImage(GraphicsUtil.getImage("/images/nist.gif", getClass()));

        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent we) {
                doTerminate(0);
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){

            @Override
            public void run() {
                cleanup();

            }
        }));
        this.debugFlag = debugFlag;
        this.exporterPort = exporterPort;

        // Get the local host name.
        sLocalHost = getLocalHost();
        if (sLocalHost == null) {
            JOptionPane.showMessageDialog(this,
                    "Failed to obtain the local host name!");
            System.exit(-1);
        }


        /*Thread t = new Thread(){
            public void run(){
                KernelSimulator.main(new String[]{});
            }
        };
        t.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


        // Set the client socket for communication with the Kernel simulator.
        try {
            simClient = new SSLSocketClient("localhost", simulatorPort,
                    debugFlag, "SMgr");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to create the client socket for communication with the K-simulator!");
            e.printStackTrace();
            System.exit(-2);
        }

        // Check the connection to the K-simulator.
        if (!checkKernelConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Unable to connect to the Kernel simulator! Exiting...");
            System.exit(-3);
        }
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent containerEvent) {
                SwingShortcuts.resizeComponentsToDimension(
                        containerEvent.getContainer().getSize(),
                        containerEvent.getContainer().getComponents());
            }

            @Override
            public void componentRemoved(ContainerEvent containerEvent) {
                SwingShortcuts.resizeComponentsToDimension(
                        containerEvent.getContainer().getSize(),
                        containerEvent.getContainer().getComponents());
            }
        });
        layeredPane.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized(ComponentEvent componentEvent) {
                Container cont = (Container)componentEvent.getComponent();
                SwingShortcuts.resizeComponentsToDimension(cont.getSize(), cont.getComponents());
            }
        });
        setContentPane(layeredPane);
        // Build the GUI.
        {
            DesktopPanel desktopPanel = new DesktopPanel();
            layeredPane.add(desktopPanel, JLayeredPane.DEFAULT_LAYER);
            DesktopDropTargetListener dtl = new DesktopDropTargetListener(desktopPanel, new Supplier<PmGraph>(){
                @Override
                public PmGraph get() {
                    return currentSessionDialog.getVosGraph();
                }
            });
            DropTarget dt = new DropTarget(layeredPane, DnDConstants.ACTION_COPY_OR_MOVE,
        dtl, true);
            layeredPane.setDropTarget(dt);



            desktopPanel.setLayout(null);
            desktopPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            pcos.addObserver(dtl);
            desktopDropTarget = dtl;
        }


        GradientPanel backGroundGradient = new GradientPanel(Color.GRAY, Color.WHITE);
        {
            backGroundGradient.setBackgroundImage(GraphicsUtil.getImage(
                    "/images/NIST_logo_blue.png", getClass()));
            backGroundGradient.setLayout(new BorderLayout());
            backGroundGradient.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
            layeredPane.add(backGroundGradient, (Integer) JLayeredPane.DEFAULT_LAYER + 75);
        }

        {


            JLabel titleLabel = new JLabel(GraphicsUtil.getImageIcon("/images/MainGraphicProfile.png", getClass()));
            titleLabel.setFont(new Font("Tahoma", Font.BOLD, 82));
            titleLabel.setForeground(new Color(0, 0, 128));
            titleLabel.setVerticalAlignment(JLabel.CENTER);
            titleLabel.setHorizontalTextPosition(JLabel.CENTER);
            backGroundGradient.add(titleLabel, BorderLayout.CENTER);
        }

        // The Menu bar.
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // The File menu.
        JMenu menu = new JMenu("Manager");
        menuBar.add(menu);

        logonMenuItem = new JMenuItem("Login");
        logonMenuItem.addActionListener(this);
        menu.add(logonMenuItem);

        menu.addSeparator();

        JMenuItem menuItem = new JMenuItem("Configure...");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Exit", KeyEvent.VK_Q);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                ActionEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // The View menu.
        menu = new JMenu("View");
        menuBar.add(menu);

        // The Window menu.
        windowMenu = new JMenu("Window");
        menuBar.add(windowMenu);

        //String titleString = "Policy Machine";
        //JLabel logoLabel = new JLabel(GraphicsUtil.getImageIcon(
        //        "/images/NIST_logo_blue.png", getClass()), JLabel.CENTER);
        //contentPane.add(logoLabel, BorderLayout.NORTH);
        //JLabel titleLabel = new JLabel(titleString, JLabel.CENTER);
        //Unsupported Font titleLabel.setFont(new Font("Gill Sans MT", Font.BOLD, 82));
        //titleLabel.setFont(new Font("Verdana", Font.BOLD, 82));


        sessionMap = new HashMap();
        applicationManager = new ApplicationManager(simClient, getLocalHost());

        configEditor = new OsConfigEditor(this, applicationManager);
        configEditor.pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = configEditor.getSize();
        configEditor.setLocation(
                (int) ((screen.getWidth() - size.getWidth()) / 2),
                (int) ((screen.getHeight() - size.getHeight()) / 2));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        String name = source.getName();
        String sCommand = source.getText();
        System.out.printf("Name of the menuItem %s and the comment %s", name, sCommand);
        if (sCommand.equals("Login")) {
            doLogin();
        } else if (sCommand.equals("Exit")) {
            doTerminate(0);
        } else if (sCommand.equals("Configure...")) {
            doConfigure();
        } else {
            System.out.println(sCommand);
            doBringToFront(sCommand);
            System.out.println("You asked for window " + sCommand);
            // Find the session object that corresponds to the action command.
            Iterator iter = sessionMap.values().iterator();
            while (iter.hasNext()) {
                Session sess = (Session) iter.next();
                if (sess.getName().equals(sCommand)) {
                    sess.toFront();
                }
            }
        }
    }

    // Check the connection to the K-simulator.
    private boolean checkKernelConnection() {
        return true;
        /*try {
            Packet cmd = makeCmd("connect", null);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }*/
    }


    // Terminate the session manager.
    private void doTerminate(int n) {
        // Send each session prepareToClose().
        // Remove the session object.
        // Delete the session from the server.
        cleanup();

        try {
            simClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(n);
    }

    private void cleanup(){
        Iterator iter = sessionMap.values().iterator();
        while (iter.hasNext()) {
            Session sess = (Session) iter.next();
            sess.prepareToClose();
            if (sess == exporterSession) {
                System.out.println("Found the exporter session. trying to destroy the exporter process...");
                sess.destroyExporter();
            }
            CommandUtil.exitSession(sess.getId(), simClient);
            iter.remove();
            sess = null;
        }
    }

    // Bring to front the window for the session with the name sName.
    private void doBringToFront(String sName) {
        // Find the session object that corresponds to the action command.
        Iterator iter = sessionMap.values().iterator();
        while (iter.hasNext()) {
            Session sess = (Session) iter.next();
            if (sess.getName().equals(sName)) {
                sess.toFront();
            }
        }
    }

    private void doConfigure() {
        //set these using the application manager.
        //rework the config editor to accomodate any number of applications.
        configEditor.setVisible(true);
    }


    /**
	 * @return
	 * @uml.property  name="applicationManager"
	 */
    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    // If an user named "exporter" exists, create a session for it.
    public boolean openExporterSession() {
        if (!userExists(sExporterName)) {
            return true;
        }

        Packet res = null;
        try {
            Packet cmd = makeCmd("createSession", null, "My session",
                    sLocalHost, sExporterName, sExporterName);
            res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this,
                        "Error in createSession for user " + sExporterName
                                + ": " + res.getErrorMessage());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Exception in createSession for user " + sExporterName
                            + ": " + e.getMessage());
            return false;
        }

        // The engine answer contains:
        // item 0: <sess name>
        // item 1: <sess id>
        // item 2: <user id>
        sExporterSessName = res.getStringValue(0);
        sExporterSessId = res.getStringValue(1);
        sExporterId = res.getStringValue(2);

        // Build the session object w/o GUI (last argument true means
        // no GUI)
        exporterSession = new Session(this, simClient, sExporterSessName,
                sExporterSessId, sExporterName, sExporterId, sLocalHost, true);
        sessionMap.put(sExporterSessId, exporterSession);


        // Launch the Exporter application within this session.
        return exporterSession.doLaunchExporter();
    }

    /**
	 * @return
	 * @uml.property  name="exporterSession"
	 */
    public Session getExporterSession() {
        return exporterSession;
    }

    private boolean userExists(String sUserName) {
        Packet res = null;
        try {
            Packet cmd = makeCmd("getIdOfEntityWithNameAndType", null, sUserName, PM_NODE_USER);
            res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this,
                        "Error in getIdOfEntityWithNameAndType for user " + sUserName + ": "
                                + res.getErrorMessage());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Exception in getIdOfEntityWithNameAndType for user " + sUserName + ": "
                            + e.getMessage());
            return false;
        }
        return true;
    }


    public void doLogin() {
        log.debug("TRACE 1 - In SessionManager.doLogin()");

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        String message = "Please enter your user name and password.";
        for (int j = 0; j < 3; j++) {
            int res = JOptionPane.showOptionDialog(null, new Object[]{
                    message, userField, passField}, "Login",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, null, null);
            if (res == JOptionPane.CANCEL_OPTION
                    || res == JOptionPane.CLOSED_OPTION) {
                return;
            }

            // Get user name and password.
            String sUser = userField.getText();
            char[] cPass = passField.getPassword();
            
            log.debug("TRACE 2 - In SessionManager.doLogin()");

            // Try to open the session.
            String sessionId = openSession(sUser, cPass);

            // Zero the password.
            for (int i = 0; i < cPass.length; i++) {
                cPass[i] = 0;
            }
            if (sessionId != null) {
                String hostName = getLocalHost();
                logonMenuItem.setEnabled(false);
                taskBar = new OsTaskBar(sessionId, applicationManager, debugFlag);


                NativeProcessWrapper internalProcessWrapper = new NativeProcessWrapper() {

                    @Override
                    public Process getProcess() {
                        return null;
                    }

                    @Override
                    public boolean bringApplicationToFront() {
                        currentSessionDialog.toFront();
                        return true;
                    }

                    @Override
                    public String[] getWindowNames() {
                        return new String[]{currentSessionDialog.getTitle()};
                    }

                    @Override
                    public Icon getApplicationIcon() {
                        Image image = SessionManager.this.getIconImage();
                        image = image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                        return new ImageIcon(image);
                    }
                };

                taskBar.addTask("Session - " + sUser, SESSION_TASK_ID, internalProcessWrapper);
                {
                    JPanel taskBarLayer = new JPanel();
                    taskBarLayer.setLayout(new BorderLayout());
                    taskBarLayer.add(taskBar, BorderLayout.SOUTH);
                    taskBarLayer.setOpaque(false);
                    taskBarLayer.setVisible(true);
                    getContentPane().add(taskBarLayer, JLayeredPane.POPUP_LAYER);
                }
                pack();
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                return;
            }
        }
    }

    /**
     * @param sUserName Username for the user creating a session
     * @param cPass     The user's password
     * @return the new sessions id or null if session creation failed
     */
    // The password is sent in clear to the server.
    private String openSession(String sUserName, char[] cPass) {
        log.debug("TRACE 3 - In SessionManager.openSession()");

        if (sUserName.length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Incorrect user name or password");
            return null;
        }
        // Find the local host name.
        String sHostName = getLocalHost();
        if (sHostName == null) {
            JOptionPane.showMessageDialog(this,
                    "Failed to obatain local host name!");
            return null;
        }

        // Send the session name, host name, user name, and password to the
        // server.
        Packet res = null;
        try {
            log.debug("TRACE 4 - In SessionManager.openSession() - Make command 'createSession'");

            Packet cmd = makeCmd("createSession", null, "My session",
                    sHostName, sUserName, new String(cPass));
            res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, "Error in createSession: "
                        + res.getErrorMessage());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Exception in createSession: "
                    + e.getMessage());
            return null;
        }

        // The engine answer contains:
        // item 0: <sess name>
        // item 1: <sess id>
        // item 2: <user id>
        // Build the new session object.
        String sSessName = res.getStringValue(0);
        String sSessId = res.getStringValue(1);
        String sUserId = res.getStringValue(2);
        Session oldSessionDialog = currentSessionDialog;
        log.debug("TRACE 5 - In SessionManager.openSession() - CREATE SESSION HERE");

        currentSessionDialog = new Session(this, simClient, sSessName, sSessId,
                sUserName, sUserId, sHostName);
        sessionMap.put(sSessId, currentSessionDialog);
        currentSessionDialog.pack();
        GraphicsUtil.centerDialog(currentSessionDialog);
        currentSessionDialog.setVisible(true);

        pcos.firePropertyChange(Session.PM_SESSION_PROPERTY, currentSessionDialog, oldSessionDialog);

        JMenuItem menuItem = new JMenuItem(sSessName);
        menuItem.addActionListener(this);
        windowMenu.add(menuItem);
        log.debug("TRACE 6 - In SessionManager.openSession() END");

        return sSessId;
    }

    public Session getSession(String sId) {
        return (Session) sessionMap.get(sId);
    }

    public void registerSession(Session session) {
        sessionMap.put(session.getId(), session);
        JMenuItem menuItem = new JMenuItem(session.getName());
        menuItem.addActionListener(this);
        windowMenu.add(menuItem);
    }

    // Ask the engine to delete the session from its database.
    // Remove the session from the HashMap.
    // Remove the corresponding item from the Window menu.
    // Zero the ref to the session object.
    // Note that the session is registered (inserted in sessionMap)
    // by the manager only after construction is successful.
    // CloseSession may be called from session's doClose() during
    // construction, so the session might not be registered yet,
    // so we need to test if the sesion is registered.
    public void closeSession(String sId) {
        // Delete the session from the PM server.
        if (!CommandUtil.exitSession(sId, simClient)) {
            return;
        }

        // Delete the session from the manager.
        if (!sessionMap.containsKey(sId)) {
            return;
        }
        Session session = (Session) sessionMap.remove(sId);
        String sName = session.getName();
        int n = windowMenu.getItemCount();
        for (int i = 0; i < n; i++) {
            if (windowMenu.getItem(i).getText().equals(sName)) {
                windowMenu.remove(i);
                break;
            }
        }
        taskBar.setVisible(false);
        taskBar = null;
        session = null;

        doLogin();
    }

    public String getEngineHost() {
        return "musial";
    }

    public int getEnginePort() {
        return PM_DEFAULT_SERVER_PORT;
    }

    /**
	 * @return
	 * @uml.property  name="exporterPort"
	 */
    public int getExporterPort() {
        return exporterPort;
    }


    byte hexDigit2Byte(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        } else if (c >= 'a' && c <= 'f') {
            return (byte) (c - 'a' + 10);
        } else if (c >= 'A' && c <= 'F') {
            return (byte) (c - 'A' + 10);
        } else {
            return (byte) 0;
        }
    }


    public static String getMessage(ArrayList res) {
        return ((String) res.get(0)).substring(4);
    }


    public static Object success() {
        ArrayList res = new ArrayList();
        res.add(PM_SUCCESS);
        return res;
    }


    public static HashSet packetToSet(Packet p) {
        HashSet set = new HashSet();
        if (p == null) {
            return set;
        }
        for (int i = 0; i < p.size(); i++) {
            set.add(p.getStringValue(i));
        }
        return set;
    }


    private static final String PM_APPLICATION_NAME = "PM Operating Environment";

    // Find and return where to insert a new string in an alphabetically ordered
    // list.
    public static int getIndex(DefaultListModel model, String target) {
        int high = model.size(), low = -1, probe;
        while (high - low > 1) {
            probe = (high + low) / 2;
            if (target.compareToIgnoreCase((String) model.get(probe)) < 0) {
                high = probe;
            } else {
                low = probe;
            }
        }
        return (low + 1);
    }

    private static void createAndShowGUI(int simulatorPort, int exporterPort,
                                         boolean debugFlag) {
        final SessionManager manager = new SessionManager("PMOS - Session Manager",
                simulatorPort, exporterPort, debugFlag);

        // manager.setVisible(true);
        manager.pack();
        manager.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // GraphicsUtil.centerFrame(manager);
        manager.setFocusableWindowState(false);

        manager.setVisible(true);
        manager.toBack();
        // if (!manager.checkInstalledApps()) {
        // JOptionPane.showMessageDialog(manager,
        // "No path for the Exporter application found!");
        // manager.doTerminate(0);
        // }

        class DelayLoginWorker extends javax.swing.SwingWorker<Boolean, Object> {

            @Override
            protected Boolean doInBackground() throws Exception {
                Thread.sleep(4000);
                return true;
            }

            @Override
            protected void done() {
                manager.doLogin();
            }
        }

        if (manager.openExporterSession()) {
            (new DelayLoginWorker()).execute();
        }
    }

    private static int nSimPort = 0;
    private static int nExpPort = 0;
    private static boolean bDbgFlag = false;

    public static void main(String[] args) {
        // Process possible arguments.
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-debug")) {
                bDbgFlag = true;
            } else if (args[i].equals("-simport")) {
                nSimPort = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-export")) {
                nExpPort = Integer.valueOf(args[++i]).intValue();
            }
        }

        if (nSimPort < 1024) {
            nSimPort = PM_DEFAULT_SIMULATOR_PORT;
        }
        if (nExpPort < 1024) {
            nExpPort = PM_DEFAULT_EXPORTER_PORT;
        }

        System.out.println("SessionManager started...");
        System.out.println("... with KernelSimulator port " + nSimPort + "...");
        System.out.println("... and Exporter port " + nExpPort + "...");
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //new ProcessHandler().prepareAndShow();
                DialogUtils.getAllSystemProperties(PM_APPLICATION_NAME);
                createAndShowGUI(nSimPort, nExpPort, bDbgFlag);
            }
        });
    }



    @Override
    public void addObserver(PropertyChangeObserver observer) {
        pcos.addObserver(observer);
    }

    @Override
    public void removeObserver(PropertyChangeObserver observer) {
        pcos.removeObserver(observer);
    }
}

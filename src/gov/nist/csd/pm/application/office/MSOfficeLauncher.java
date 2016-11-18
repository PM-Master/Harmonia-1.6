/*
 * MSOfficeLauncher.java
 *
 * Created on February 27, 2009, 10:30 AM
 */
package gov.nist.csd.pm.application.office;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.ObjectBrowser;
import static gov.nist.csd.pm.common.constants.GlobalConstants.*;
import gov.nist.csd.pm.common.net.Packet;

import javax.net.ServerSocketFactory;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

@SuppressWarnings("CallToThreadDumpStack")
public class MSOfficeLauncher extends JFrame {
    public static final String MSOFFICE_LAUNCHER_PREFIX = "OFL";
    public static final String MS_OFFICE_LAUNCHER_APP_TITLE = "MS Office Launcher";

    public static final String PM_OBJTYPE_DOC = "doc";
    public static final String PM_OBJTYPE_PPT = "ppt";
    public static final String PM_OBJTYPE_XLS = "xls";
    /**
	 * @uml.property  name="objectBrowser"
	 * @uml.associationEnd  
	 */
    private ObjectBrowser objectBrowser;
    // Name and handle for the virtual object currently open.
    /**
	 * @uml.property  name="sCrtObjName"
	 */
    private String sCrtObjName;
    /**
	 * @uml.property  name="sCrtObjHandle"
	 */
    private String sCrtObjHandle;
    /**
	 * @uml.property  name="sCrtLocalPath"
	 */
    private String sCrtLocalPath;
    /**
	 * @uml.property  name="sProcessId"
	 */
    private String sProcessId;
    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    private SysCaller sysCaller;
    /**
	 * @uml.property  name="vbClientPort"
	 */
    private int vbClientPort;
    /**
	 * @uml.property  name="ss"
	 */
    private ServerSocket ss;
    /**
	 * @uml.property  name="random"
	 */
    private Random random;

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public MSOfficeLauncher(int simPort, String sSessId, String sProcId,
            String sObjType, boolean bDebug) {
        this.sProcessId = sProcId;

        // An instance of SysCaller to call Kernel's APIs.
        sysCaller = new SysCallerImpl(simPort, sSessId, sProcId, bDebug, MSOFFICE_LAUNCHER_PREFIX);

        setTitle(MS_OFFICE_LAUNCHER_APP_TITLE);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                terminate(0);
            }
        });

        // Get a random port number between 13000 and 14000 for the
        // VB client.
        random = new Random();
        vbClientPort = 13000 + random.nextInt(1000);
        System.out.println("VB Client port is " + vbClientPort);

        // Create the server socket for communication
        // with the automation client.
        try {
            ServerSocketFactory ssf = (ServerSocketFactory) ServerSocketFactory.getDefault();
            ss = ssf.createServerSocket(vbClientPort);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage());
            terminate(-1);
        }

        // Launch the automation client using another thread
        new ServerThread(vbClientPort).start();

    }
    private static String digits = "0123456789abcdef";

    public static String toHex(byte[] data, int length) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i != length; i++) {
            int v = data[i] & 0xff;
            buf.append(digits.charAt(v >> 4));
            buf.append(digits.charAt(v & 0x0f));
        }
        return buf.toString();
    }

    // Return the data byte array as a hex string
    public static String toHex(byte[] data) {
        return toHex(data, data.length);
    }

    /**
	 * @return
	 * @uml.property  name="objectBrowser"
	 */
    private ObjectBrowser getObjectBrowser() {
        // Let the user select the object to be opened.
        if (objectBrowser == null) {
            objectBrowser = new ObjectBrowser(this, sysCaller, "MS Office Launcher");
            objectBrowser.pack();
        }
        return objectBrowser;
    }

    private String newFilename() {
        byte[] nameBytes = new byte[4];
        random.nextBytes(nameBytes);
        return toHex(nameBytes);
    }

    private void service() {
        try {
            // Listen for connection from the automation client
            Socket s = ss.accept();

            // Grab the connected socket's input and output streams
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();

            // INSERT SERVICE LOOP
            byte[] cmd = new byte[256];
            String res = "";
            while (true) {
                int n = in.read(cmd, 0, cmd.length);
                String line = new String(cmd, 0, n);
                System.out.println("Server received " + n + " bytes, <" + line + ">");

                String[] pieces = line.split("\\|");
                String code = pieces[0];

                if (code.equals("start")) {
                    res = startDialog();
                } else if (code.equals("save")) {
                    res = saveDialog();
                } else if (code.equals("open")) {
                    res = openDialog();
                } else if (code.equals("saveas")) {
                    res = saveasDialog();
                } else if (code.equals("rsave")) {
                    res = rsaveDialog();
                } else if (code.equals("rsaveas")) {
                    res = rsaveasDialog();
                } else if (code.equals("stop")) {
                    break;
                } else {
                    System.out.println("Unknown command code " + code);
                    res = "ok";
                }
                System.out.println("Server will return <" + res + ">");
                byte[] response = res.getBytes();
                out.write(response);
                out.flush();
            }
            out.close();
            in.close();
            s.close();
            terminate(0);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Exception: " + e.getMessage());
            terminate(-2);
        }
    }

    // Called when the client sends command "saveas" to save
    // an object's content.
    // Return:
    // "error" - if an error occurs, in which case the application
    //           should terminate.
    // <tmp path> - a temporary location where the object content
    //              will be saved.
    private String saveasDialog() {
        sCrtLocalPath = "E:\\Tmp\\" + newFilename() + ".doc";
        return sCrtLocalPath;
    }

    private String rsaveasDialog() {
        int ret = getObjectBrowser().showSaveAsDialog();
        if (ret != ObjectBrowser.PM_OK) {
            return "ok";
        }

        // Prepare for object creation.
        String sObjName = getObjectBrowser().getObjName();
        String sContainers = getObjectBrowser().getContainers();

        System.out.println("Containers: " + sContainers);
        System.out.println("Object nam: " + sObjName);

        String sObjClass = "File";
        String sObjType = "doc";
        String sPerms = "File write";

        // Create the object.
        String sHandle = sysCaller.createObject3(sObjName, sObjClass, sObjType,
                sContainers, sPerms, null, null, null, null);
        if (sHandle == null) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return "ok";
        }

        // Write the object content from the file sCrtLocalPath.
        try {
            File f = new File(sCrtLocalPath);

            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, "File " + sCrtLocalPath
                        + "does not exist!");
                return "ok";
            }
            if (!f.canRead()) {
                JOptionPane.showMessageDialog(this, "File " + sCrtLocalPath
                        + "not readable!");
                return "ok";
            }
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            int len = bis.available();
            byte[] buf = new byte[len];
            int n = bis.read(buf, 0, len);
            if (len != n) {
                JOptionPane.showMessageDialog(this, "Number of bytes read "
                        + n + " not the file length!");
                return "ok";
            }

            int res = sysCaller.writeObject3(sHandle, buf);
            if (res < 0) {
                JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                return "ok";
            }
            sCrtObjHandle = sHandle;
            sCrtObjName = sObjName;
            return "ok" + "|" + sCrtObjName;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Exception while saving object: " + e.getMessage());
            return "ok";
        }
    }

    // Called when the client sends command "save" following
    // the user's selecting the Save button. The client
    // intercepts the "document before save" event.
    // The server must generate, save, and return a temporary
    // file path where Word should save its current document.
    // Upon receipt of this temporary path, the client should
    // tell Word to save its current document to the temporary
    // location. Right after this temporary saving, the client
    // should send a rsave command to this server, which triggers
    // the actual saving of the document to an object's content.
    //
    // Return:
    // "error" - if an error occurs, in which case the application
    //           should terminate.
    // <tmp path> - a temporary location where the object content
    //              will be saved.
    private String saveDialog() {
        sCrtLocalPath = "E:\\Tmp\\" + newFilename() + ".doc";
        return sCrtLocalPath;
    }

    // To save the content of an object, the client and the server
    // have the following dialog:
    // Client to server: save (the user's interaction with the
    // Word Save menu triggers the dialog).
    // Server responds: <path> (a local path generated by the server).
    // The client temporarily stores the file to <path>.
    // Client to server: rsave[|<path>].
    // Now there are two cases: 1. The user invoked Word without
    // an initial object, he entered some content, and now he wants
    // to save it into a new object. 2. The user invoked Word on
    // an existing object, updated the content, and now he wants
    // to save it into the old object.
    // Server responds: ok[|<objname>].
    // If <objname> is present in the response, the client has to
    // update the file name displayed by Word.
    //
    // Return:
    // ok[|<objname>]
    //
    private String rsaveDialog() {

        if (sCrtObjName == null || sCrtObjHandle == null) {
            int ret = getObjectBrowser().showSaveAsDialog();
            if (ret != ObjectBrowser.PM_OK) {
                return "ok";
            }

            // Prepare for object creation.
            String sObjName = getObjectBrowser().getObjName();
            String sContainers = getObjectBrowser().getContainers();

            System.out.println("Containers: " + sContainers);
            System.out.println("Object nam: " + sObjName);

            String sObjClass = "File";
            String sObjType = "doc";
            String sPerms = "File write";

            // Create the object.
            String sHandle = sysCaller.createObject3(sObjName, sObjClass, sObjType,
                    sContainers, sPerms, null, null, null, null);
            if (sHandle == null) {
                JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                return "ok";
            }

            // Write the object content from the file sCrtLocalPath.
            try {
                File f = new File(sCrtLocalPath);

                if (!f.exists()) {
                    JOptionPane.showMessageDialog(this, "File " + sCrtLocalPath
                            + "does not exist!");
                    return "ok";
                }
                if (!f.canRead()) {
                    JOptionPane.showMessageDialog(this, "File " + sCrtLocalPath
                            + "not readable!");
                    return "ok";
                }
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);
                int len = bis.available();
                byte[] buf = new byte[len];
                int n = bis.read(buf, 0, len);
                if (len != n) {
                    JOptionPane.showMessageDialog(this, "Number of bytes read "
                            + n + " not the file length!");
                    return "ok";
                }

                int res = sysCaller.writeObject3(sHandle, buf);
                if (res < 0) {
                    JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                    return "ok";
                }
                sCrtObjHandle = sHandle;
                sCrtObjName = sObjName;
                return "ok" + "|" + sCrtObjName;

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Exception while saving object: " + e.getMessage());
                return "ok";
            }
        } else {
            try {
                File f = new File(sCrtLocalPath);

                if (!f.exists()) {
                    JOptionPane.showMessageDialog(this, "File " + sCrtLocalPath
                            + "does not exist!");
                    return "ok";
                }
                if (!f.canRead()) {
                    JOptionPane.showMessageDialog(this, "File " + sCrtLocalPath
                            + "not readable!");
                    return "ok";
                }
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);
                int len = bis.available();
                byte[] buf = new byte[len];
                int n = bis.read(buf, 0, len);
                if (len != n) {
                    JOptionPane.showMessageDialog(this, "Number of bytes read "
                            + n + " not the file length!");
                    return "ok";
                }

                int res = sysCaller.writeObject3(sCrtObjHandle, buf);
                if (res < 0) {
                    JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                    return "ok";
                }
                return "ok" + "|" + sCrtObjName;
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Exception while saving object: " + e.getMessage());
                return "ok";
            }
        }
    }

    // Called when the client sends "start" to start the dialog.
    // Return:
    // "new" - to open a new document in Word.
    // <path> - to open the file with this <path>
    // "error" - if an error occurs, in which case the application
    //           should terminate.
    //
    private String startDialog() {
        if (sCrtObjName == null) {
            // Word launched without a filename. Send a response
            // indicating to open a new document.
            return "new";
        } else {
            // Word launched on an object. Try to open the object
            // for reading. If not possible, issue a message and
            // return error. Otherwise, make a local copy of the
            // object's content and open it in word.
            sCrtObjHandle = sysCaller.openObject3(sCrtObjName, "File read,File write");
            if (sCrtObjHandle == null) {
                JOptionPane.showMessageDialog(this, sysCaller.getLastError());
                return "error";
            }
            sCrtLocalPath = "E:\\Tmp\\" + newFilename() + ".doc";
            try {
                FileOutputStream fos = new FileOutputStream(sCrtLocalPath);
                Packet res = sysCaller.readObject3(sCrtObjHandle, fos);
                if (res == null) {
                    JOptionPane.showMessageDialog(this,
                            "Null packet received from readObject()!");
                    return "error";
                } else if (res.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Empty packet received from readObject()!");
                    return "error";
                } else if (res.hasError()) {
                    JOptionPane.showMessageDialog(this,
                            res.getErrorMessage());
                    return "error";
                }
                return sCrtLocalPath + "|" + sCrtObjName;
            } catch (Exception exc) {
                exc.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Exception in readObject: " + exc.getMessage());
                return "error";
            }
        }
    }

    // Called when the client sends "open" to open a new object.
    // Return:
    // <path>[|<objname>] - <objname> is the name of the object
    // selected to be opened. <path> is the path of a local
    // file where the server has stored the object's content.
    // "error" - if an error occurs in the server.
    //
    private String openDialog() {
        int ret = getObjectBrowser().showOpenDialog();
        if (ret != ObjectBrowser.PM_OK) {
            return "error";
        }

        String sObjName = getObjectBrowser().getObjName();
        System.out.println("Object name: " + sObjName);

        String sObjHandle = sysCaller.openObject3(sObjName, "File read,File write");
        if (sObjHandle == null) {
            JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            return "error";
        }
        sCrtLocalPath = "E:\\Tmp\\" + newFilename() + ".doc";
        try {
            FileOutputStream fos = new FileOutputStream(sCrtLocalPath);
            Packet res = sysCaller.readObject3(sObjHandle, fos);
            if (res == null) {
                JOptionPane.showMessageDialog(this,
                        "Null packet received from readObject()!");
                return "error";
            } else if (res.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Empty packet received from readObject()!");
                return "error";
            } else if (res.hasError()) {
                JOptionPane.showMessageDialog(this,
                        res.getErrorMessage());
                return "error";
            }
            sCrtObjName = sObjName;
            sCrtObjHandle = sObjHandle;
            return sCrtLocalPath + "|" + sCrtObjName;
        } catch (Exception exc) {
            exc.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Exception in readObject: " + exc.getMessage());
            return "error";
        }
    }

    private void terminate(int nExitCode) {
        sysCaller.exitProcess(sProcessId);
        System.exit(nExitCode);
    }

    private void setCrtObj(String sObjName) {
        sCrtObjName = sObjName;
    }

    private void openInitialObj(String sObjName) {
        // Open the object.
        if (sObjName == null) {
            JOptionPane.showMessageDialog(null, "No object to open!");
            return;
        }
        sCrtObjHandle = sysCaller.openObject3(sObjName, "File read,File write");
        if (sCrtObjHandle == null) {
            JOptionPane.showMessageDialog(null, sysCaller.getLastError());
            return;
        }

        // Get the object content.
        byte[] buf = sysCaller.readObject3(sCrtObjHandle);
        if (buf == null) {
            JOptionPane.showMessageDialog(null, sysCaller.getLastError());
            String sErr = sysCaller.getLastError();
            if (sErr != null && sErr.indexOf("exist") > 0) {
                buf = new byte[0];
            } else {
                return;
            }
        }

        sCrtObjName = sObjName;

        // Insert code for making local copy of the object's content.

        // Code for launching MS office component.

    }

    // Arguments on the command line:
    // -session <sessionId> -process <processId> -simport <simulator port> -objtype <object type> <virtual object name>
    // The session id, process id, and the object type are mandatory.
    // The object type can be: doc, ppt, xsl.
    public static void main(String[] args) {
        String sessid = null;
        String pid = null;
        int simport = 0;
        String objname = null;
        String objtype = null;
        boolean debug = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-session")) {
                sessid = args[++i];
            } else if (args[i].equals("-process")) {
                pid = args[++i];
            } else if (args[i].equals("-simport")) {
                simport = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-objtype")) {
                objtype = args[++i];
            } else if (args[i].equals("-debug")) {
                debug = true;
            } else {
                objname = args[i];
            }
        }
        if (sessid == null || sessid.length() == 0) {
            System.out.println("The WriterLauncher must run in a PM session!");
            System.exit(-1);
        }
        if (pid == null || pid.length() == 0) {
            System.out.println("The WriterLauncher must run in a PM process!");
            System.exit(-1);
        }
        MSOfficeLauncher launcher = new MSOfficeLauncher(simport, sessid, pid, objtype, debug);
        launcher.setCrtObj(objname);
        launcher.pack();
        launcher.setVisible(true);
        launcher.service();

        //launcher.openInitialObj(objname);

    }
}

@SuppressWarnings("CallToThreadDumpStack")
class ServerThread extends Thread {

    /**
	 * @uml.property  name="vbClientPort"
	 */
    private int vbClientPort;

    public ServerThread(int vbClientPort) {
        this.vbClientPort = vbClientPort;
    }

    @Override
    public void run() {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("WindowsClient.exe " + vbClientPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package gov.nist.csd.pm.exporter;

import gov.nist.csd.pm.common.application.SSLSocketServer;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import static gov.nist.csd.pm.common.constants.GlobalConstants.*;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.Date;
import java.util.Properties;


/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:01 $
 * @since 1.5
 */
public class Exporter implements Runnable {
    public static final String EXPORTER_APP_PREFIX = "EXP";

    public static final String PM_FAILURE     = "err ";
    public static final String PM_SUCCESS     = "ok  ";

    public static final String PM_CMD         = "cmd ";
    public static final String PM_EOC         = "eoc ";

    public static final String PM_ARG         = "arg ";
    public static final String PM_SEP         = "sep ";

    public static final String PM_DATA        = "data";
    public static final String PM_EOD         = "eod ";

    public static final String PM_BYE         = "bye ";

    /**
     * @uml.property  name="sysCaller"
     * @uml.associationEnd
     */
    private SysCaller sysCaller;
    /**
     * @uml.property  name="simPort"
     */
    private int simPort;
    /**
     * @uml.property  name="exporterPort"
     */
    private int exporterPort;
    /**
     * @uml.property  name="bDebug"
     */
    private boolean bDebug;
    /**
     * @uml.property  name="sSessId"
     */
    private String sSessId;
    /**
     * @uml.property  name="sProcId"
     */
    private String sProcId;

    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    public Exporter(int simPort, int exporterPort, String sSessId, String sProcId, boolean bDebug) {
        this.simPort = (simPort < 1024)? PM_DEFAULT_SIMULATOR_PORT : simPort;
        this.exporterPort = (exporterPort < 1024)? PM_DEFAULT_EXPORTER_PORT : exporterPort;
        this.bDebug = bDebug;
        this.sSessId = sSessId;
        this.sProcId = sProcId;

        // An instance of SysCaller to call Kernel's APIs.
        // IOC Candidate
        sysCaller = new SysCallerImpl(simPort, sSessId, sProcId, bDebug, EXPORTER_APP_PREFIX);

        // Launch another thread to run the Exporter server (which receives export
        // requests from email clients run on behalf of other users).
        Thread expThread = new Thread(this);
        expThread.start();
    }

    @Override
    @SuppressWarnings("CallToThreadDumpStack")
    public void run() {
        SSLSocketServer sslSockServer = new SSLSocketServer(exporterPort, bDebug, "s,EXP"){

            @Override
            public Packet executeCommand(String clientName, Packet command, InputStream fromClient, OutputStream toClient) {
                return Exporter.this.executeCommand(clientName, command);
            }
        };

        System.out.println("Exporter's socket server created...");

        try {
            sslSockServer.service();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to create the exporter's server socket.");
            System.exit(1);
        }
    }

    private void terminate(int exitCode) {
        sysCaller.exitProcess(sProcId);
        System.exit(exitCode);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    public synchronized Packet executeCommand(String sClientId, Packet cmd) {

        if (cmd == null || cmd.size() < 1) {
            return failurePacket("Null or incomplete command received!");
        }

        String sCmdCode = cmd.getStringValue(0);

        // Dispatch the command
        try {
            if (sCmdCode.equalsIgnoreCase("connect")) {
                return successPacket(sClientId);

            } else if (sCmdCode.equalsIgnoreCase("ping")) {
                return successPacket(sClientId);

            } else if (sCmdCode.equalsIgnoreCase("sendMessage")) {
                if (cmd.size() <= 6) {
                    return failurePacket("Too few arguments");
                }
                String sessIdLocal = getArgument(cmd, 1);
                String sMsgName = getArgument(cmd, 2);
                String sAttName = getArgument(cmd, 3);
                String sSender = getArgument(cmd, 4);
                String sReceiver = getArgument(cmd, 5);
                String sSubject = getArgument(cmd, 6);
                return sendMessage(sessIdLocal, sMsgName, sAttName,
                        sSender, sReceiver, sSubject);

            } else if (sCmdCode.equalsIgnoreCase("exportObject")) {
                if (cmd.size() <= 3) {
                    return failurePacket("Too few arguments!");
                }
                String sSessIdLocal = getArgument(cmd, 1);
                String sObjName = getArgument(cmd, 2);
                String sDevName = getArgument(cmd, 3);
                return exportObject(sSessIdLocal, sObjName, sDevName);

            } else {
                return failurePacket("Unknown command!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private Packet exportObject(String sSessId, String sObjName, String sDevName) {
        String sHandle = sysCaller.openObject3(sObjName, "File read");
        if (sHandle == null) {
            return failurePacket("Failed to open " + sObjName + ". " + sysCaller.getLastError());
        }

        // Read the attached object.
        byte[] buf = sysCaller.readObject3(sHandle);
        if (buf == null) {
            String sErr = "Failed to read " + sObjName + ": " + sysCaller.getLastError();
            sysCaller.closeObject(sHandle);
            return failurePacket(sErr);
        }
        sysCaller.closeObject(sHandle);

        // Get the name of the file underlying the attached object.
        String sFileName = getFileName(sObjName);

        // Write the file to the device's root folder.
        File f = new File(sDevName + File.separator + sFileName);
        try{
            FileOutputStream fos = new FileOutputStream(f);
            int remaining = buf.length;
            int offset = 0;
            int blocklen = 16384;
            while (remaining > 0) {
                if (blocklen <= remaining) {
                    fos.write(buf, offset, blocklen);
                    offset += blocklen;
                    remaining -= blocklen;
                } else {
                    fos.write(buf, offset, remaining);
                    remaining = 0;
                }
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Couldn't write file " + f.getName() + "!");
        }
        return successPacket(null);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private Packet sendMessage(String sSessId, String sMsgName, String sAttObjName,
                               String sSender, String sTo, String sSubject) {
        if (bDebug)
            System.out.println("SendMessage, sessid = " + sSessId + ", msg = " +
                    sMsgName + ", attach = " + sAttObjName + ", sender = " + sSender +
                    ", to = " + sTo + ", subject = " + sSubject);

        // Get email account information for the sender.
        Packet senderInfo = (Packet)sysCaller.getEmailAcct(sSender);
        if (senderInfo.hasError()) return senderInfo;
        if (bDebug)
            for (int i = 0; i < senderInfo.size(); i++)
                System.out.println(senderInfo.getStringValue(i));
        // Sender information contains:
        // item 0: <user name>:<user id>
        // item 1: <'coming from' name>
        // item 2: <email address>
        // item 3: <incoming server>
        // item 4: <outgoing server>
        // item 5: <account name>.
        String sFrom = senderInfo.getStringValue(2);
        String sServer = senderInfo.getStringValue(4);

        // Open the object.
        String sHandle = sysCaller.openObject3(sMsgName, "File read");
        if (sHandle == null) {
            return failurePacket("Failed to open " + sMsgName + ". " + sysCaller.getLastError());
        }

        // Read the object content.
        byte[] buf = sysCaller.readObject3(sHandle);
        if (buf == null) {
            String sErr = "Failed to read " + sMsgName + ". " + sysCaller.getLastError();
            sysCaller.closeObject(sHandle);
            return failurePacket(sErr);
        }
        sysCaller.closeObject(sHandle);

        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        InputStreamReader isr = new InputStreamReader(bais);
        BufferedReader in = new BufferedReader(isr);

        String line;
        StringBuilder sb = new StringBuilder();
        String newline = System.getProperties().getProperty("line.separator");
        try {
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append(newline);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Reading the msg content: " + e.getMessage());
        }

        System.out.println(sb.toString());

        String sAttFileName = null;
        File f = null;

        // If there is an attached object, we'll make a copy of its underlying file.
        if (sAttObjName != null) {
            // Open the attached object.
            sHandle = sysCaller.openObject3(sAttObjName, "File read");
            if (sHandle == null) {
                return failurePacket("Failed to open " + sAttObjName + ". " + sysCaller.getLastError());
            }

            // Reserve space for and read the object content.
            buf = sysCaller.readObject3(sHandle);
            if (buf == null) {
                String sErr = "Failed to read " + sAttObjName + ". " + sysCaller.getLastError();
                sysCaller.closeObject(sHandle);
                return failurePacket(sErr);
            }
            sysCaller.closeObject(sHandle);

            // Get the name of the file underlying the attached object.
            sAttFileName = getFileName(sAttObjName);

            // Write the attached object's content to a file.
            f = new File(sAttFileName);
            try{
                FileOutputStream fos = new FileOutputStream(f);
                int remaining = buf.length;
                int offset = 0;
                int blocklen = 16384;
                while (remaining > 0) {
                    if (blocklen <= remaining) {
                        fos.write(buf, offset, blocklen);
                        offset += blocklen;
                        remaining -= blocklen;
                    } else {
                        fos.write(buf, offset, remaining);
                        remaining = 0;
                    }
                }
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                return failurePacket("Couldn't write file " + f.getName() + "!");
            }
        }
        Packet res = actualSend(sb.toString(), sAttFileName, sSubject, sFrom, sServer, sTo);
        if (f != null) f.delete();
        return res;
    }

    // Get the name (not the entire path) of the file underlying an object.
    private String getFileName(String sObjName) {
        String sPath = sysCaller.getObjectPath(sObjName);
        if (sPath == null || sPath.length() == 0) return null;
        int index = sPath.lastIndexOf(File.separator);
        if (index + 1 == sPath.length()) return null;
        if (index < 0) return sPath;
        else return sPath.substring(index + 1);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private Packet actualSend(String sMsgTxt, String sAttFile, String sSubject,
                              String sFrom, String sServer, String sTo) {

        System.out.println("##################From: " + sFrom);
        System.out.println("##################Server: " + sServer);
        System.out.println("##################To: " + sTo);



        try {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", sServer);
            Session session = Session.getInstance(props, null);
            session.setDebug(true);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(sFrom));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(sTo, false));
            msg.setSubject(sSubject);

            if (sAttFile == null) {
                msg.setText(sMsgTxt);
            } else {
                MimeBodyPart mbp = new MimeBodyPart();
                mbp.setText(sMsgTxt);
                MimeBodyPart mbp2 = new MimeBodyPart();
                mbp2.attachFile(sAttFile);
                MimeMultipart mp = new MimeMultipart();
                mp.addBodyPart(mbp);
                mp.addBodyPart(mbp2);
                msg.setContent(mp);
            }
            msg.setHeader("X-Mailer", "Exporter");
            msg.setSentDate(new Date());
            Transport.send(msg);
            return successPacket(null);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while actually sending message: " + e.getMessage());
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private Packet failurePacket(String s) {
        try {
            Packet res = new Packet();
            if (s == null || s.length() == 0) res.addItem(ItemType.RESPONSE_ERROR, "Unspecified error");
            else res.addItem(ItemType.RESPONSE_ERROR, s);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private Packet successPacket(String s) {
        try {
            Packet res = new Packet();
            if (s == null || s.length() == 0) res.addItem(ItemType.RESPONSE_SUCCESS, "Success");
            else res.addItem(ItemType.RESPONSE_SUCCESS, s);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getArgument(Packet p, int n) {
        if (p == null) return null;
        if (n < 0 || n >= p.size()) return null;
        String s = p.getStringValue(n);
        if (s == null) return null;
        if (s.length() == 0) return null;
        return s;
    }

    // Arguments on the command line:
    // -session <sessionId> -simPort <simulator port> -export <exporter port>
    // The session id is mandatory.
    public static void main(String[] args) {
        String sessid = null;
        String pid = null;
        int export = 0;
        int simport = 0;
        boolean debug = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-session")) {
                sessid = args[++i];
            } else if (args[i].equals("-process")) {
                pid = args[++i];
            } else if (args[i].equals("-simport")) {
                simport = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-export")) {
                export = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-debug")) {
                debug = true;
            }
        }

        if(export == 0){
            export = PM_DEFAULT_EXPORTER_PORT;
        }

        if(simport == 0){
            simport = PM_DEFAULT_SIMULATOR_PORT;
        }

        System.out.println("THE EXPORTER ****)( : " + simport + "  "+export);
        if (sessid == null || sessid.length() == 0) {
            System.out.println("Exporter must run in a PM session!");
            System.exit(-1);
        }
        if (pid == null || pid.length() == 0) {
            System.out.println("Exporter must run in a PM process!");
            System.exit(-1);
        }
        Exporter exporter = new Exporter(simport, export, sessid, pid, debug);
    }
}

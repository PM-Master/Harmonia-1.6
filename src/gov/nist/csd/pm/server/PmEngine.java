package gov.nist.csd.pm.server;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;
import gov.nist.csd.pm.common.application.SSLSocketServer;
import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.MySQL_Statements;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.Server;
import gov.nist.csd.pm.common.util.swing.DialogUtils;
//import gov.nist.csd.pm.server.dao.ActiveDirectory.ActiveDirectoryDAO;
import gov.nist.csd.pm.server.dao.MySQLDB.CommonSQLDAO;
import gov.nist.csd.pm.server.dao.MySQLDB.ObligationDAO;

import gov.nist.csd.pm.server.graph.DenyManager;
import gov.nist.csd.pm.server.graph.PmGraph;
import gov.nist.csd.pm.server.graph.PmGraphManager;
//import gov.nist.csd.pm.server.packet.ADPacketHandler;
import gov.nist.csd.pm.server.packet.SQLPacketHandler;
import gov.nist.csd.pm.sql.PmDatabase;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.swing.*;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:56 $
 * @since 1.5
 */
@SuppressWarnings("CallToThreadDumpStack")
public class PmEngine {

    /**
     * @uml.property  name="env"
     * @uml.associationEnd  qualifier="constant:java.lang.String java.lang.String"
     */
    private Hashtable<String, String> env = null;
    /**
     * @uml.property  name="ctx"
     * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
     */

    //NDK added this - This flag will let the Audit Function be turned on and off
    //False is on and True is off

    public static Connection connection = null;
    String sEngineHost;
    String adminPass;
    public PmEngine(boolean bDebug, int nPort) throws Exception {
        try{
            ServerConfig.getPmDB();
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Error connecting to database", "ERROR", JOptionPane.ERROR_MESSAGE);
            throw new Exception("Could not connect to database, server not starting: " + e.getMessage());
        }
        ServerConfig.debugFlag = bDebug;
        String message = "Administrator's Password:";
		adminPass = DialogUtils.showQuestionDisplay(message, true);
        DialogUtils.getAllSystemProperties("Server");

        if(ServerConfig.datastore.equals("SQL")) {
            ServerConfig.SQLDAO = new CommonSQLDAO();
            ServerConfig.obligationDAO = new ObligationDAO();
            //ServerConfig.pmDB = new PmDatabase();
        }

        // Find the domain name.
        InetAddress addr = InetAddress.getLocalHost();
        String sDomName = addr.getCanonicalHostName();
        String[] pieces = sDomName.split("\\.");
        sEngineHost = pieces[0];
        System.out.println("Using engine host " + sEngineHost);

        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < pieces.length; i++) {
            if (i == 1) {
                sb.append("DC=").append(pieces[i]);
            } else {
                sb.append(",DC=").append(pieces[i]);
            }
        }
        ServerConfig.sThisDomain = sb.toString();

        ServerConfig.myEngine = this;

        // Set the URL protocol handler for https
        System.setProperty("java.protocol.handler.pkgs",
                "com.sun.net.ssl.internal.www.protocol");
    }

    private void buildGraph() throws Exception {
        ServerConfig.graphMgr = new PmGraphManager(new PmGraph());
        ServerConfig.graphMgr.build();
        //ServerConfig.graphMgr.printGraph();
    }

    private void buildDenies(){
        ServerConfig.denyMgr = new DenyManager();
        ServerConfig.denyMgr.build();
        //ServerConfig.denyMgr.printDenies();
    }

    public void startSQL(int nPort, boolean bDebug) throws Exception {
        buildGraph();
        buildDenies();
        sqlPacketHandler = new SQLPacketHandler();
        try {
            SSLSocketServer sslServer = new SSLSocketServer(nPort,
                    bDebug, "s,E") {

                @Override
                public Packet executeCommand(String clientName, Packet command, InputStream fromClient, OutputStream toClient) {
                    return sqlPacketHandler.executeCommand(clientName, command, fromClient, toClient);
                }
            };
            System.out.println("PM Server running...");
            System.out.println("on port: " + nPort);
            sslServer.service();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public SQLPacketHandler sqlPacketHandler;
    public static void main(String[] args) {
        boolean bDebug = false;
        int nPort = PM_DEFAULT_SERVER_PORT;
        System.out.println(args.length);
        for(int i= 0; i<args.length; i++){
            System.out.println(i + ": " + args[i]);
            if (args[i].equals("-debug")) {
                bDebug = true;
            } else if (args[i].equals("-engineport")) {
                nPort = Integer.valueOf(args[++i]);
            } else if (args[i].equals("-auditflag")) {
                ServerConfig.auditDebug =  args[++i].equalsIgnoreCase("true")?true:false;
            } else if (args[i].equals("-datastore")) {
                ServerConfig.datastore = (args[++i].equals("SQL")) ? "SQL" : "AD";
            }
        }

        try {
            ServerConfig.myEngine = new PmEngine(bDebug, nPort);

            DialogUtils.getAllSystemProperties("Server");
            if(ServerConfig.datastore.equalsIgnoreCase("SQL")){
                System.out.println("SQL");
                ServerConfig.myEngine.startSQL(GlobalConstants.PM_DEFAULT_SERVER_PORT, bDebug);
            }
//            else{
//                System.out.println("Starting AD...");
//                ServerConfig.myEngine.startAD(nPort, bDebug);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}




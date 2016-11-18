package gov.nist.csd.pm.server;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;

import gov.nist.csd.pm.common.application.SSLSocketServer;
import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.Server;
import gov.nist.csd.pm.common.util.swing.DialogUtils;
//import gov.nist.csd.pm.server.dao.ActiveDirectory.ActiveDirectoryDAO;
import gov.nist.csd.pm.server.dao.MySQL.CommonSQLDAO;
import gov.nist.csd.pm.server.dao.MySQL.ObligationDAO;
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
            throw new Exception("Could not connect to database, server not starting");
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

        char[] cAdminPass = "myMachinePasswordHere".toCharArray();

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

    private void buildGraph(){
        ServerConfig.graphMgr = new PmGraphManager(new PmGraph());
        ServerConfig.graphMgr.build();
    }

    private void buildDenies(){
        ServerConfig.denyMgr = new DenyManager();
        ServerConfig.denyMgr.build();
    }

    public void startSQL(int nPort, boolean bDebug){
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

//    private void startAD(int nPort, boolean bDebug){
//        // Connect to the LDAP server
//        try {
//            env = new Hashtable<String, String>();
//            env.put(Context.INITIAL_CONTEXT_FACTORY,
//                    "com.sun.jndi.ldap.LdapCtxFactory");
//            env.put(Context.SECURITY_AUTHENTICATION, "simple");
//            env.put(Context.SECURITY_PRINCIPAL, "CN=Administrator,CN=Users,"
//                    + ServerConfig.sThisDomain);
//            env.put(Context.SECURITY_CREDENTIALS, adminPass);
//            env.put(Context.PROVIDER_URL, "ldap://" + sEngineHost + ":389/");
//            env.put("java.naming.ldap.version", "3");
//            ServerConfig.ctx = new InitialDirContext(env);
//        } catch (AuthenticationException e) {
//            e.printStackTrace();
//            System.out.println("PM Engine: Authentication to AD failed!" + e.getMessage());
//            System.exit(1);
//        } catch (Exception e) {
//            if (ServerConfig.debugFlag) {
//                e.printStackTrace();
//            }
//            System.out.println("PM Engine: Failed to connect to AD" + e.getMessage());
//            System.exit(1);
//        }
//
//        // Set the containers' names.
//        ServerConfig.ADDAO.setContainerNames();
//        DateFormat dfUpdate = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
//        ServerConfig.ADDAO.setLastUpdateTimestamp();
//
//        ServerConfig.ADDAO.extractClassNames();
//        ServerConfig.ADDAO.createInitialObjects();
//
//        ServerConfig.ADDAO.emptyContainer(ServerConfig.ADDAO.sSessionContainerDN, null);
//        ServerConfig.ADDAO.emptyContainer(ServerConfig.ADDAO.sEventContainerDN, null);
//        adPacketHandler = new ADPacketHandler();
//        try {
//            SSLSocketServer sslServer = new SSLSocketServer(nPort,
//                    bDebug, "s,E") {
//
//                @Override
//                public Packet executeCommand(String clientName, Packet command, InputStream fromClient, OutputStream toClient) {
//                    return adPacketHandler.executeCommand(clientName, command, fromClient, toClient);
//                }
//            };
//            System.out.println("PM Server running...");
//            sslServer.service();
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//    }

    //IPolicyMachine policyMachineV2 = (IPolicyMachine)new PmEngine2();
    public SQLPacketHandler sqlPacketHandler;
//    public ADPacketHandler adPacketHandler;
    public static void main(String[] args) {
        boolean bDebug = false;
        int nPort = PM_DEFAULT_SERVER_PORT;
        System.out.println(args.length);
        for (int i = 0; i < args.length; i++) {
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

//		try {
//			ServerConfig.myEngine = new PmEngine(bDebug, nPort);
//
//			DialogUtils.getAllSystemProperties("Server");
//
//			SSLSocketServer sslServer = new SSLSocketServer(nPort,
//					bDebug, "s,E") {
//
//				@Override
//				public Packet executeCommand(String clientName, Packet command, InputStream fromClient, OutputStream toClient) {
//					return packetHandler.executeCommand(clientName, command, fromClient, toClient);
//				}
//			};
//			System.out.println("PM Server running...");
//			sslServer.service();
//			//
//			/*final PmEngine newEngine = new PmEngine(bDebug, nPort, "sql");
//
//			DialogUtils.getAllSystemProperties("Server");
//
//			SSLSocketServer sqlSslServer = new SSLSocketServer(nPort,
//					bDebug, "sqlS,E") {
//
//				@Override
//				public Packet executeCommand(String clientName, Packet command, InputStream fromClient, OutputStream toClient) {
//					return engine.executeCommand(clientName, command, fromClient, toClient);
//				}
//			};
//			System.out.println("PM Server running...");
//			sqlSslServer.service();*/
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
    }
}




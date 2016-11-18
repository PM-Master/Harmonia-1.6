package gov.nist.csd.pm.server;

import gov.nist.csd.pm.common.net.*;
import gov.nist.csd.pm.common.util.Log;
import gov.nist.csd.pm.server.PmEngine;

import java.util.*;
import java.io.*;
import java.lang.*;
import java.net.*;

import javax.net.ssl.*;
import javax.security.cert.*;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:56 $
 * @since 1.5
 */
public class SSLSocketServer {

	private PmEngine engine;
	private int port;
	private boolean debug;
	private String prefix;

	public SSLSocketServer(PmEngine engine, int port, boolean debug, String prefix) {
		this.engine = engine;
		this.port = port;
		this.debug = debug;
		this.prefix = prefix;
	}

	public void service() throws Exception {
		SSLServerSocketFactory ssf = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		SSLServerSocket ss = (SSLServerSocket)ssf.createServerSocket(port);

		ss.setNeedClientAuth(true);

		while (true) {
			Socket s = ss.accept();
			new SSLServerThread(s, engine, debug, prefix).start();
		}
	}
}

class SSLServerThread extends Thread {
	private Socket s;
	private PmEngine engine;
	private boolean debug;
	private String prefix;
	Log log = new Log(Log.Level.INFO, true);

	public SSLServerThread(Socket s, PmEngine engine, boolean debug, String prefix) {
		this.s = s;
		this.engine = engine;
		this.debug = debug;
		this.prefix = prefix;
	}

	public void run() {
		try {
			SSLSession session = ((SSLSocket)s).getSession();

			// Extract the client information from the session
			X509Certificate[] certChain = null;
			certChain = session.getPeerCertificateChain();
			java.security.Principal client = certChain[0].getSubjectDN();
			String dName = client.getName();
			if (dName.indexOf("CN=") != 0) throw new RuntimeException("Incorrect DN");
			int iEnd = dName.indexOf(",");
			if (iEnd < 0) throw new RuntimeException("Incorrect DN");
			String sClientName = dName.substring(3, iEnd);
			//////////////////////////////////////
			// Set the name here for debugging
			//////////////////////////////////////
			//name="serban";

			BufferedInputStream bisFromClient = new BufferedInputStream(s.getInputStream());
			BufferedOutputStream bosToClient = new BufferedOutputStream(s.getOutputStream());

			while (true) {
				Packet cmdPacket = PacketManager.receivePacket(bisFromClient, null);
		        log.debug("TRACE 1 - In SSLSocketServer.run() - Received Packet");

				if (cmdPacket == null) {
					// The connection must have closed, so break
					log.error("PM server received a null packet. Closing connection...");
					break;
				}

				if (cmdPacket.isEmpty()) {
					log.debug("PM server received an empty packet. Return error packet!");
					Packet err = new Packet();
					err.addItem(ItemType.RESPONSE_ERROR, "Server received an empty packet!");
					PacketManager.sendPacket(err, bosToClient);
					continue;
				}

				// For debugging...
				cmdPacket.print(debug, prefix + "<<<");

				// Examine the executeCommand's result.
		        log.debug("TRACE 2 - In SSLSocketServer.run() - Executing command");
		        Packet res = new Packet();
				if(engine.sqlPacketHandler != null){
					res = (Packet)engine.sqlPacketHandler.executeCommand(sClientName, cmdPacket, bisFromClient, bosToClient);
//				}else if(engine.adPacketHandler != null){
//					res = (Packet)engine.adPacketHandler.executeCommand(sClientName, cmdPacket, bisFromClient, bosToClient);
				}

				// For debugging...
				res.print(debug, prefix + ">>>");

				// Some commands would compose and send back the answer in
				// executeCommand or a method called from it. They return
				// a "Do not respond" packet to the run() method in SSLServerThread.
				if (res.doNotRespond()) continue;

				// Send the normal response to the client and loop.
				PacketManager.sendPacket(res, bosToClient);
			}

			bosToClient.close();
			bisFromClient.close();
			if (debug) System.out.println("Server thread shutting down (Server still alive for new connections)");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
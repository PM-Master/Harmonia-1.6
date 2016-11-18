package gov.nist.csd.pm.common.application;

import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketManager;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:56 $
 * @since 1.5
 * RM 2011 Refactored all SSLSocketServer classes into a single abstract implementation
 * Instances of SSLSocketServer provide the command dispatching implementation as this
 * varies based on implementor (e.g. KernelSimulator, PmEngine)
 */
public abstract class SSLSocketServer implements PolicyMachineServer {

	/**
	 * @uml.property  name="port"
	 */
	private int port;
	/**
	 * @uml.property  name="debug"
	 */
	private boolean debug;
	/**
	 * @uml.property  name="prefix"
	 */
	private String prefix;

	public SSLSocketServer(int port, boolean debug, String prefix) {
		this.port = port;
		this.debug = debug;
		this.prefix = prefix;
	}
	@Override
	public void service() throws Exception {
		SSLServerSocketFactory ssf = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
		SSLServerSocket ss = (SSLServerSocket)ssf.createServerSocket(port);

		ss.setNeedClientAuth(true);

		while (true) {
			System.out.println("before accept");
			Socket s = ss.accept();
			System.out.println("after accept");
			new SSLServerThread(s,  debug, prefix).start();
		}
	}
	public abstract Packet executeCommand(String clientName, Packet command, InputStream fromClient, OutputStream toClient);


	class SSLServerThread extends Thread {
		private Socket s;
		private boolean debug;
		private String prefix;

		public SSLServerThread(Socket s, boolean debug, String prefix) {
			this.s = s;
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

					if (cmdPacket == null) {
						// The connection must have closed, so break
						System.out.println("PM server received a null packet. Closing connection...");
						break;
					}

					if (cmdPacket.isEmpty()) {
						System.out.println("PM server received an empty packet. Return error packet!");
						Packet err = new Packet();
						err.addItem(ItemType.RESPONSE_ERROR, "Server received an empty packet!");
						PacketManager.sendPacket(err, bosToClient);
						continue;
					}

					// For debugging...
					cmdPacket.print(debug, prefix + "<<<");
					System.out.println("************DATA SSLCocketServer************\n" +
							"sClientName: " + sClientName + "\n" +
							"cmdPacket  : " + cmdPacket.size() + "\n" + 
							"bisFromClient: " + bisFromClient.toString() + "\n" + 
							"bosToClient: " + bosToClient.toString() + "\n" + 
							"**************END SSLSocketServer 105*************");
					// Examine the executeCommand's result.
					Packet res = executeCommand(sClientName, cmdPacket, bisFromClient, bosToClient);

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
}


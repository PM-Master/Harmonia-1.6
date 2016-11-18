package gov.nist.csd.pm.common.application;

/*
 * Title:        SSLSocketClient
 * Description:
 * Authors: Serban I. Gavrila; S. Quirolgico
 */

import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketManager;
import gov.nist.csd.pm.common.util.Log;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.security.cert.X509Certificate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SSLSocketClient implements PolicyMachineClient {
	Log log = new Log(Log.Level.INFO, true);

  /**
 * @uml.property  name="sServerName"
 */
private String sServerName;
  /**
 * @uml.property  name="bDebug"
 */
private boolean bDebug = false;
  /**
 * @uml.property  name="socket"
 */
private Socket socket;
  /**
 * @uml.property  name="bosToServer"
 */
private BufferedOutputStream bosToServer;
  /**
 * @uml.property  name="bisFromServer"
 */
private BufferedInputStream bisFromServer;
  /**
 * @uml.property  name="sPrefix"
 */
private String sPrefix;
  
  public SSLSocketClient(String host, int port, boolean bDebug, String sPrefix) throws Exception {
    this.bDebug = bDebug;
    this.sPrefix = sPrefix;
    
    SSLSocketFactory sf = (SSLSocketFactory)SSLSocketFactory.getDefault();
    socket = sf.createSocket(host, port);
    SSLSession session = ((SSLSocket)socket).getSession();
    X509Certificate[] certChain = null;
    certChain = session.getPeerCertificateChain();
    java.security.Principal server = certChain[0].getSubjectDN();
    String dName = server.getName();
    if (dName.indexOf("CN=") != 0) throw new RuntimeException("Bad server DN!");
    int iEnd = dName.indexOf(",");
    if (iEnd < 0) throw new RuntimeException("Bad server DN!");
    sServerName = dName.substring(3, iEnd);

    bosToServer = new BufferedOutputStream(socket.getOutputStream());
    bisFromServer = new BufferedInputStream(socket.getInputStream());
  }

  @Override
  public Packet sendReceive(Packet cmd) throws Exception{
      return sendReceive(cmd, null);
  }

  @Override
  public Packet sendReceive(Packet cmd, OutputStream out) throws Exception {
      log.debug("TRACE 21 - In SSLSocketClient.sendReceive() -- SENDING REQUEST PACKET");

	Packet response;
    cmd.print(bDebug, sPrefix + ">>>");
    
    PacketManager.sendPacket(cmd, bosToServer);
    response = PacketManager.receivePacket(bisFromServer, out);
    log.debug("TRACE 22 - In SSLSocketClient.sendReceive() -- RECEIVING RESPONSE PACKET");

    if (response == null) {
      System.out.println();
      System.out.println(sPrefix + "<<<-----Null packet received from the server-----");
    } else {
      response.print(bDebug, sPrefix + "<<<");
    }
    
    return response;
  }
  
  @Override
  public void download(Packet cmd, OutputStream out) throws Exception {
    
    cmd.print(bDebug, sPrefix + ">>>");
    
    PacketManager.sendPacket(cmd, bosToServer);
    PacketManager.forwardBytes(bisFromServer, out);
  }

   @Override
   public void upload(Packet cmd, InputStream in) throws Exception {
    cmd.print(bDebug, sPrefix + ">>>");

    PacketManager.sendPacket(cmd, bosToServer);
    PacketManager.forwardBytes(in, bosToServer);
  }
  
  @Override
  public Packet warnAndSend(Packet warningCmd, byte[] bytes) throws Exception {
    warningCmd.print(bDebug, sPrefix + ">>>");

    PacketManager.sendPacket(warningCmd, bosToServer);
    /*TODO*/
    PacketManager.sendPacket(bytes, bosToServer);
    Packet response = PacketManager.receivePacket(bisFromServer, null);
    System.out.println("BAck from response SSL Socket line: 98");
    if (response == null) {
      System.out.println();
      System.out.println(sPrefix + "<<<-----Null packet received from the server-----");
    } else {
      response.print(bDebug, sPrefix + "<<<");
    }
    
    return response;
  }
  
  @Override
  public Packet warnAndSend(Packet warningCmd, InputStream is, int length) throws Exception {
    warningCmd.print(bDebug, sPrefix + ">>>");

    PacketManager.sendPacket(warningCmd, bosToServer);
    PacketManager.sendPacket(is, length, bosToServer);
    Packet response = PacketManager.receivePacket(bisFromServer, null);
    
    if (response == null) {
      System.out.println();
      System.out.println(sPrefix + "<<<-----Null packet received from the server-----");
    } else {
      response.print(bDebug, sPrefix + "<<<");
    }
    
    return response;
  }

  @Override
  public void close() throws Exception {
    bisFromServer.close();
    bosToServer.close();
    socket.close();
  }
}
/* This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS".  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof including, but
 * not limited to, the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */
package gov.nist.csd.pm.common.net;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class implements a Library to mimic the PM application's SysCaller.
 * 
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 6.0
 */
public class Library {
  /**
 * @uml.property  name="toSrvBufStream"
 */
private BufferedOutputStream toSrvBufStream = null;
  /**
 * @uml.property  name="fromSrvBufStream"
 */
private BufferedInputStream fromSrvBufStream = null;

  /**
 * @uml.property  name="sLocalHost"
 */
private String sLocalHost;
  /**
 * @uml.property  name="nLocalPort"
 */
private int nLocalPort;
  /**
 * @uml.property  name="sWhereFileIs"
 */
private String sWhereFileIs;
  
  public Library(String sLocalHost, int nLocalPort, String sWhereFileIs) {
    System.out.println("Library Constructor called with:");
    System.out.println("  Local host: " + sLocalHost);
    System.out.println("  Local port: " + nLocalPort);
    System.out.println("  Where file is: " + sWhereFileIs);
    
    this.sLocalHost = sLocalHost;
    this.nLocalPort = nLocalPort;
    this.sWhereFileIs = sWhereFileIs;
    
    try {
      // Set up connection to server 1.
      SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("localhost", nLocalPort);

      OutputStream toSrvStream = sslSocket.getOutputStream();
      toSrvBufStream = new BufferedOutputStream(toSrvStream);
      InputStream fromSrvStream = sslSocket.getInputStream();
      fromSrvBufStream = new BufferedInputStream(fromSrvStream);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  // Request and receive the data from the server.
  public Packet getObject(String sPath, OutputStream outputStream) {
    System.out.println("library.getObject called with:");
    System.out.println("  filepath: " + sPath);
    try {
      Packet cmd = new Packet();
      cmd.addItem(ItemType.CMD_CODE, "getFile");
      cmd.addItem(ItemType.CMD_ARG, sPath);
      cmd.addItem(ItemType.CMD_ARG, sWhereFileIs);
      System.out.println("library.getObject ready to send the following command to (local) server:");
      cmd.print(true, "To server");
      PacketManager.sendPacket(cmd, toSrvBufStream);

      Packet res = PacketManager.receivePacket(fromSrvBufStream, outputStream);
      System.out.println("library.getObject received the following response from (local) server:");
      res.print(true, "From server");
      return res;
    } catch (PacketException pe) {
      pe.printStackTrace();
    }
    return null;
  }
  
  // Send data from the application to the server. Note that first we have
  // to send a warning to the server that we are going to send binary data.
  public void sendObject(byte[] bytes) {

    try {
      Packet warning = new Packet();
      warning.addItem(ItemType.CMD_CODE, "putFile");
      warning.addItem(ItemType.CMD_ARG, "result.rtf");
      warning.addItem(ItemType.CMD_ARG, sWhereFileIs);
      // Send warning packet.
      PacketManager.sendPacket(warning, toSrvBufStream);
      // Send data packet.
      PacketManager.sendPacket(bytes, toSrvBufStream);
    } catch (PacketException pe) {
      pe.printStackTrace();
    }
  }
}

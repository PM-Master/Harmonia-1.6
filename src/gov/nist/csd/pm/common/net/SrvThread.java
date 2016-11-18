/*
 * SrvThread.java
 *
 * Created on April 15, 2008, 10:02 AM
 */

package gov.nist.csd.pm.common.net;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class SrvThread extends Thread {

  // The socket returned by the accept() method in the server.
  /**
 * @uml.property  name="srvSocket"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private SSLSocket srvSocket = null;
  /**
 * @uml.property  name="sLocalHost"
 */
private String sLocalHost = null;

  public SrvThread(SSLSocket srvSocket, String sLocalHost) {
    System.out.println("SrvThread constructor called for server " + sLocalHost);
    
    this.srvSocket = srvSocket;
    this.sLocalHost = sLocalHost;
  }

  public void run() {
    try {
      // Set up the communication streams to/from client.
      InputStream fromClientStream = srvSocket.getInputStream();
      BufferedInputStream fromClientBufStream = new BufferedInputStream(fromClientStream);
      OutputStream toClientStream = srvSocket.getOutputStream();
      BufferedOutputStream toClientBufStream = new BufferedOutputStream(toClientStream);

      // Loop {receive command; return response;} until the client closes the connection.
      for (;;) {
        Packet cmd = PacketManager.receivePacket(fromClientBufStream, null);
        if (cmd == null) {
          // The connection must have closed, so break.
          System.out.println("Null packet received, connection will be closed!");
          break;

        } else if (cmd.isEmpty()) {
          // Handle empty packet here!
          System.out.println("Empty packet received!");

        } else {
          System.out.println("Server on " + sLocalHost + " received the following packet:");
          cmd.print(true, "From client :");
          
          // Dispatch the command.
          String sCmdCode = cmd.getItemStringValue(0);
          if (sCmdCode.equals("getFile")) {
            // Absolute path.
            String sPath = cmd.getItemStringValue(1);
            // Host name.
            String sHost = cmd.getItemStringValue(2);

            if (sHost.equals(sLocalHost)) {
              System.out.println("Get content of local file!");
              // Local file.
              // Return the file content to the client.
              File file = new File(sPath);
              
              if (!file.exists()) {
                Packet err = new Packet();
                err.addItem(ItemType.RESPONSE_ERROR, "No such file!");
                PacketManager.sendPacket(err, toClientBufStream);
              } else {
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bfis = new BufferedInputStream(fis);
                PacketManager.sendPacket(bfis, (int)file.length(), toClientBufStream);
              }
            } else {
              System.out.println("Get content of remote file!");
              // Remote file.
              // Establish communication with the remote server.
              SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
              SSLSocket clientSocket = (SSLSocket)sslSocketFactory.createSocket("localhost", PM_DEFAULT_EXPORTER_PORT);
              InputStream fromSrvStream = clientSocket.getInputStream();
              BufferedInputStream fromSrvBufStream = new BufferedInputStream(fromSrvStream);
              OutputStream toSrvStream = clientSocket.getOutputStream();
              BufferedOutputStream toSrvBufStream = new BufferedOutputStream(toSrvStream);

              // Forward the original command to the remote server.
              PacketManager.sendPacket(cmd, toSrvBufStream);

              // Now wait for data from the remote server and just return it
              // to the application. Note that the data might be the file data
              // or it might be a response message.
              PacketManager.forwardBytes(fromSrvBufStream, toClientBufStream);
              
              // Close the communication to the remote server.
            }
          
          } else if (sCmdCode.equals("putFile")) {
            // Absolute path.
            String sPath = cmd.getItemStringValue(1);
            // Host name.
            String sHost = cmd.getItemStringValue(2);

            if (sHost.equals(sLocalHost)) {
              System.out.println("Put content of local file!");
              // Open a stream for the local file and get the bytes from the client
              // transferred to it.
              FileOutputStream fos = new FileOutputStream(sPath);
              cmd = PacketManager.receivePacket(fromClientBufStream, fos);
              fos.close();

            } else {
              System.out.println("Put content of remote file!");
              // Remote file.
              // Establish communication with the remote server.
              SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
              SSLSocket clientSocket = (SSLSocket)sslSocketFactory.createSocket("localhost", PM_DEFAULT_EXPORTER_PORT);
              InputStream fromSrvStream = clientSocket.getInputStream();
              BufferedInputStream fromSrvBufStream = new BufferedInputStream(fromSrvStream);
              OutputStream toSrvStream = clientSocket.getOutputStream();
              BufferedOutputStream toSrvBufStream = new BufferedOutputStream(toSrvStream);

              // Forward the original command (the warning) to the remote server.
              PacketManager.sendPacket(cmd, toSrvBufStream);

              // Forward the file content to the remote server.
              PacketManager.forwardBytes(fromClientBufStream, toSrvBufStream);
              
              // Close the communication to the remote server.
            }
          } else if (sCmdCode.equals("otherCmd")) {
            // Perform the command and return a response to the client.
          }
        } // "good" packet
      } // loop
    } catch (PacketException pe) {
      pe.printStackTrace();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  } // run
}
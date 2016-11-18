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

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;

/**
 * This class implements a server for testing PM mesaging.  
 * 
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 6.0
 */
public class Server {

  public static void main(String[] arg) {
    // The arguments must be host name, port.
    if (arg.length != 2) {
      System.out.println("Usage: server <host name> <server port>");
      System.exit(-1);
    }
    String sHostName = arg[0];
    int nSrvPort = Integer.valueOf(arg[1]).intValue();
    
    try {
      SSLServerSocketFactory sslServerSocketFactory =
        (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
      SSLServerSocket sslServerSocket =
        (SSLServerSocket) sslServerSocketFactory.createServerSocket(nSrvPort);
      sslServerSocket.setNeedClientAuth(true);

      while (true) {
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        SrvThread serverThread = new SrvThread(sslSocket, sHostName);
        serverThread.start();
      }
    } catch (IOException ioException) {
           ioException.printStackTrace();
    }
  }
}
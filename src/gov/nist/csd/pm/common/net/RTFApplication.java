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

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


/**
 * This class implements a client for testing PM messaging.
 * 
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 6.0
 */
public class RTFApplication extends JFrame implements ActionListener {

  /***************************************************************************
  * Constants
  **************************************************************************/

  private static final long serialVersionUID = 0;

  /**
 * Variables
 * @uml.property  name="rtf"
 * @uml.associationEnd  multiplicity="(1 1)"
 */

  private RTFEditorKit rtf = null;
  /**
 * @uml.property  name="editor"
 * @uml.associationEnd  multiplicity="(1 1)"
 */
private JEditorPane editor = null;
  /**
 * @uml.property  name="library"
 * @uml.associationEnd  
 */
private Library library = null;

  /***************************************************************************
  * Constructors
  **************************************************************************/
  public RTFApplication(String sLocalHost, int nLocalPort, String sWhereFileIs) {
    library = new Library(sLocalHost, nLocalPort, sWhereFileIs);
    createGUI();
  }

  /***************************************************************************
  * Methods
  **************************************************************************/

  public void actionPerformed(ActionEvent ae) {
    if (ae.getActionCommand().equals("Get Data")) {
      getData();

    } else if (ae.getActionCommand().equals("Send Data")) {
      sendData();

    } else if (ae.getActionCommand().equals("Exit")) {
      System.exit(0);
    }
  }

  /** Get an RTF document from the proxy or server to load into this document.
  * Here, we send a command to request the data and then open an input
  * stream to receive the data.
  */
  public void getData() {
    try {
      // These streams pipe the data into the RTF reader.
      final PipedOutputStream pipedOutputStream = new PipedOutputStream();
      BufferedOutputStream bos = new BufferedOutputStream(pipedOutputStream);
      final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
      final BufferedInputStream bis = new BufferedInputStream(pipedInputStream);

      // In order to receive data into the piped input stream, we need a
      // separate thread to read from the stream.
      new Thread(new Runnable() {
        public void run() {
          System.out.println("Ready to receive data from piped input stream");

          // Read data from the piped input stream.
          try {
            rtf.read(bis, editor.getDocument(), 0);
          } catch (IOException e) {
            System.out.println("I/O error");
          } catch (BadLocationException e) {
            e.printStackTrace();
          }
        }
      }).start();

      // Now request the file from the server and send the name of the 
      // desired file.
      Packet receivedPacket = library.getObject("source.rtf", bos);
      // Check packet contents.
      if (receivedPacket == null) {
        // The connection must have closed, so break.
        System.out.println("Received null packet.  Closing...");

      } else if (receivedPacket.isEmpty()) {
        // Handle empty packet here!
        System.out.println("Received empty packet!");

      } else { // We received a good packet
        if (receivedPacket.hasError()) {
          System.out.println("Received error msg: " + receivedPacket.getErrorMessage());

        } else if (receivedPacket.successfullyReceivedBinaryStream()) {
          System.out.println("Binary data was successfully received");
        }
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  // Send the RTF document data to the server.  Before the data can be
  // sent, we must convert the output stream of the application to
  // a byte array.  This is required because the PacketManager requires
  // the length of the data to be sent in order to properly create
  // PM packets.
  public void sendData() {

    try {
      System.out.println("Preparing to write from RTF writer");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      rtf.write(baos, editor.getDocument(), 0, PacketManager.MAX_READ_LENGTH);
      byte[] bytes = baos.toByteArray();
      System.out.println("bytes length: " + bytes.length);
      baos.close();
      baos = null;

      library.sendObject(bytes);
    } catch (IOException e) {
      System.out.println("I/O error");
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  public void createGUI() {

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Build Menus
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileMenu.getAccessibleContext().setAccessibleDescription("File Menu");
    menuBar.add(fileMenu);

    JMenuItem recvMenuItem = new JMenuItem("Get Data");
    recvMenuItem.setActionCommand("Get Data");
    recvMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
    recvMenuItem.addActionListener(this);
    recvMenuItem.setToolTipText("Receive RTF data from remote server and display it.");
    fileMenu.add(recvMenuItem);

    JMenuItem sendMenuItem = new JMenuItem("Send Data");
    sendMenuItem.setActionCommand("Send Data");
    sendMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    sendMenuItem.addActionListener(this);
    fileMenu.add(sendMenuItem);
    fileMenu.addSeparator();
    sendMenuItem.setToolTipText("Send this RTF data to remove server.");

    JMenuItem exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.setActionCommand("Exit");
    exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    exitMenuItem.addActionListener(this);
    fileMenu.add(exitMenuItem);

    this.setJMenuBar(menuBar);

    // Create an RTF editor window
    rtf = new RTFEditorKit();
    editor = new JEditorPane();
    editor.setEditorKit(rtf);
    editor.setBackground(Color.white);

    // This text could be big so add a scroll pane
    JScrollPane scroller = new JScrollPane();
    scroller.getViewport().add(editor);

    setIconImage(new ImageIcon("gfx/nist.gif").getImage());

    setTitle("RTF/Stream Tester");

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(scroller, BorderLayout.CENTER);
    setSize(700, 800);
    Point upperLeftPoint = centerComponent(this);
    setLocation(upperLeftPoint);

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    this.setVisible(true);
  }

  // Center a Swing component on the screen.
  // Return: The upper-left Point to place the component.
  private Point centerComponent(Component c) {

    Rectangle rc = new Rectangle();
    rc = c.getBounds(rc);
    Rectangle rs = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

    return new Point((int) ((rs.getWidth() / 2) - (rc.getWidth() / 2)),
      (int) ((rs.getHeight() / 2) - (rc.getHeight() / 2)));
  }

  public static void main(String[] arg) {
    if (arg.length != 3) {
      System.out.println("Usage: app <local server name> <local server port> <server where file is>");
      System.exit(-1);
    }
    new RTFApplication(arg[0], Integer.valueOf(arg[1]).intValue(), arg[2]);
  }
  
}
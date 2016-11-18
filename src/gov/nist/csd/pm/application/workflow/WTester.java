package gov.nist.csd.pm.application.workflow;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:01 $
 * @since 1.5
 */
public class WTester extends JFrame implements ActionListener {

    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    SysCaller sysCaller;
    /**
	 * @uml.property  name="sProcessId"
	 */
    String sProcessId;
    /**
	 * @uml.property  name="lblSign"
	 * @uml.associationEnd  
	 */
    JLabel lblSign;
    /**
	 * @uml.property  name="butSign"
	 * @uml.associationEnd  
	 */
    JButton butSign;
    /**
	 * @uml.property  name="butClose"
	 * @uml.associationEnd  
	 */
    JButton butClose;

    public WTester(int nSimPort, String sSessId, String sProcId, boolean bDebug) {
        super("WTester App v1.0");

        this.sProcessId = sProcId;
        //IOC Candidate SysCaller
        sysCaller = new SysCallerImpl(nSimPort, sSessId, sProcId, bDebug, "WKF");

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                terminate(0);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("Sign")) {
            signForm();
        } else if (e.getActionCommand().equalsIgnoreCase("Close")) {
            terminate(0);
        }
    }

    private void terminate(int exitCode) {
        sysCaller.exitProcess(sProcessId);
        System.exit(exitCode);
    }

    protected void createUI() {
//    setSize(600, 500);
        center();

        lblSign = new JLabel((String) null);
        lblSign.setMinimumSize(new Dimension(200, 50));
        lblSign.setPreferredSize(new Dimension(200, 50));
        lblSign.setMaximumSize(new Dimension(200, 50));

        butSign = new JButton("Sign");
        butSign.addActionListener(this);

        butClose = new JButton("Close");
        butClose.addActionListener(this);

        JPanel buttonPane = new JPanel();
        buttonPane.add(lblSign);
        buttonPane.add(butSign);
        buttonPane.add(butClose);

        Container content = getContentPane();
        content.add(buttonPane, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }

    private void signForm() {
        byte[] fileBytes = sysCaller.getFileContent("alice.signature.file");

        if (fileBytes == null) {
            JOptionPane.showMessageDialog(WTester.this, sysCaller.getLastError());
            return;
        }
        ImageIcon signatureImage = new ImageIcon(fileBytes);
        lblSign.setIcon(signatureImage);
    }

    protected void center() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension us = getSize();
        int x = (screen.width - us.width) / 2;
        int y = (screen.height - us.height) / 2;
        setLocation(x, y);
    }

    protected void openInitialObj() {
        createUI();
    }

    private static void createAndShowGUI(int nSimPort, String sSessId,
            String sProcId, boolean bDebug) {
        WTester myWTester = new WTester(nSimPort, sSessId, sProcId, bDebug);
        myWTester.openInitialObj();
    }
    private static String sesid;
    private static String pid;
    private static int simport;
    private static boolean debug;

    public static void main(String[] args) {
        // Process command line arguments.
        // -session <sessionId> -simport <simulator port>
        // The session id is mandatory.
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-session")) {
                sesid = args[++i];
            } else if (args[i].equals("-process")) {
                pid = args[++i];
            } else if (args[i].equals("-simport")) {
                simport = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-debug")) {
                debug = true;
            }
        }

        if (sesid == null) {
            System.out.println("The Workflow application must run in a Policy Machine session!");
            System.exit(-1);
        }
        if (pid == null) {
            System.out.println("The Workflow application must run in a Policy Machine process!");
            System.exit(-1);
        }

        System.out.println("session=" + sesid + ", simport=" + simport);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                createAndShowGUI(simport, sesid, pid, debug);
            }
        });
    }
}

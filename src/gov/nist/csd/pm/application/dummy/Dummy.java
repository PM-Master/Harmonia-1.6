/**
 * 
 */
package gov.nist.csd.pm.application.dummy;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * @author Administrator
 *
 */
public class Dummy extends JFrame implements ActionListener {
    // Name of the current user.
    /**
	 * @uml.property  name="sCrtUserName"
	 */
    String sCrtUserName = null;
    // Name and handle for the virtual object currently open.
    /**
	 * @uml.property  name="sCrtObjName"
	 */
    String sCrtObjName;
    /**
	 * @uml.property  name="sCrtObjHandle"
	 */
    String sCrtObjHandle;
    // The SysCaller instance.
    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    SysCaller sysCaller;
    /**
	 * @uml.property  name="sProcessId"
	 */
    String sProcessId;
    private static String DUMMY_APP ="Dummy";
    
    public Dummy(int nSimPort, String sSessId, String sProcId, boolean bDebug) {
        super("Dummy App v1.0");
        this.sProcessId = sProcId;
        sysCaller = new SysCallerImpl(nSimPort, sSessId, sProcId, bDebug, "DMY");

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                terminate(0);
            }
        });
    }
    private void terminate(int code){
    	System.exit(code);
    }
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
	private static void createAndShowGUI(int nSimPort, String sSessId,
			String sProcId, String sObjName, boolean bDebug) {
		// TODO Auto-generated method stub
		Dummy dummy = new Dummy(nSimPort, sSessId, sProcId, bDebug);
		dummy.SayHi();
		
		
	}
	private void SayHi(){
		JOptionPane.showConfirmDialog(Dummy.this, "HI");
		
	}
    private static String sesid;
    private static String pid;
    private static String objname;
    private static int simport;
    private static boolean debug;
	/**
	 * @param args
	 */
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
	            } else {
	                objname = args[i];
	            }
	        }

	        if (sesid == null) {
	            System.out.println("The Dummy application must run in a Policy Machine session!");
	            System.exit(-1);
	        }
	        if (pid == null) {
	            System.out.println("The Dummy application must run in a Policy Machine process!");
	            System.exit(-1);
	        }

	        System.out.println("session=" + sesid + ", simport=" + simport + ", objname=" + objname);
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {

	            @Override
	            public void run() {
	                createAndShowGUI(simport, sesid, pid, objname, debug);
	            }


	        });
	    }

}

package gov.nist.csd.pm.user;

import javax.swing.*;

/**
 * This interface defines a wrapper for the java Process object.  It allows more control over various aspects of the project including bringing that process to the foreground and returning system specific information about that process.
 * @author  rmchugh
 */
public interface NativeProcessWrapper {
	/**
	 * @uml.property  name="process"
	 */
	public Process getProcess();
	public boolean bringApplicationToFront();
	public String[] getWindowNames();
	/**
	 * @uml.property  name="applicationIcon"
	 * @uml.associationEnd  
	 */
	public Icon getApplicationIcon();
}

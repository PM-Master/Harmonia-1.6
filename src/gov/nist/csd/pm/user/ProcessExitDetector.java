package gov.nist.csd.pm.user;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects when a process is finished and invokes the associated listeners.
 */
public class ProcessExitDetector extends Thread {

    /**
	 * The process for which we have to detect the end.
	 * @uml.property  name="process"
	 */
    private Process process;
    /**
	 * The associated listeners to be invoked at the end of the process.
	 * @uml.property  name="listeners"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.user.ProcessFinishedListener"
	 */
    private List<ProcessFinishedListener> listeners = new ArrayList<ProcessFinishedListener>();

    /**
     * Starts the detection for the given process
     * @param process the process for which we have to detect when it is finished
     */
    public ProcessExitDetector(Process process) {
        try {
            // test if the process is finished
            process.exitValue();
            throw new IllegalArgumentException("The process is already ended");
        } catch (IllegalThreadStateException exc) {
            this.process = process;
        }
    }

    /**
	 * @return  the process that it is watched by this detector.
	 * @uml.property  name="process"
	 */
    public Process getProcess() {
        return process;
    }

    public void run() {
        try {
            // wait for the process to finish
        	System.out.println("waiting for process to terminate");
            process.waitFor();
            // invokes the listeners
            for (ProcessFinishedListener listener : listeners) {
                listener.processFinished(process);
            }
        } catch (InterruptedException e) {
        }
    }

    /** Adds a process listener.
     * @param listener the listener to be added
     */
    public void addProcessListener(ProcessFinishedListener listener) {
        listeners.add(listener);
    }

    /** Removes a process listener.
     * @param listener the listener to be removed
     */
    public void removeProcessListener(ProcessFinishedListener listener) {
        listeners.remove(listener);
    }
}

package gov.nist.csd.pm.user;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LauncherThread extends Thread {

    /**
	 * @uml.property  name="sPrefix"
	 */
    private String sPrefix;
    /**
	 * @uml.property  name="cmd"
	 */
    private String cmd;
    /**
	 * @uml.property  name="proc"
	 */
    private Process proc;
    /**
	 * @uml.property  name="procId"
	 */
    private String procId;
    /**
	 * @uml.property  name="delegate"
	 * @uml.associationEnd  
	 */
    private LauncherThreadDelegate delegate;
    /**
	 * @uml.property  name="semaphore"
	 */
    private final Object semaphore;

    public LauncherThread(String cmd, String sPrefix, String procId, LauncherThreadDelegate delegate) {
        super();
        this.sPrefix = sPrefix;
        this.cmd = cmd;
        this.procId = procId;
        this.delegate = delegate;
        this.semaphore = new Object();
    }


    public String getProcessId(){
        return procId;
    }

    public Process getProcess() {
        synchronized (this) {
            System.out.println("get : Entering syncro block");
            if(proc == null){
                try {
                    System.out.println("Waiting");
                    this.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(LauncherThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("get : Exiting syncro block");
            return proc;
        }
    }

    /*
     * public void destroy() { proc.destroy(); }
     */

    /*
     * (steveq) I noticed that when this method throws an exception, it does say
     * anything about Session, LauncherThread, or StreamGobbler in the stack
     * trace. I would enhance the catch statements of these classes to make sure
     * that when an exception is thrown, we can identify the class that invoked
     * it.
     */
    @Override
    public void run() {
        //define semaphore for critical section.  
        //We need the process, before other threads can access
        //the value of this process.
        synchronized (this) {
            System.out.println("Put : Entering synchro block");
            Runtime rt = Runtime.getRuntime();
            Properties props = System.getProperties();
    		System.out.println("Properties");
    		for(Object key : props.keySet().toArray()){
    			System.out.printf("Key: %s - Value: %s\n", key.toString(), props.get(key));
    		}
    		System.out.println("\nEnvironment:");
    		for(String key : System.getenv().keySet()){
    			System.out.printf("Key: %s - Value: %s\n", key, System.getenv(key));
    		}
            try {
                proc = rt.exec(cmd);
                StreamGobbler errGobbler = new StreamGobbler(proc.getErrorStream(),
                        sPrefix);
                StreamGobbler outGobbler = new StreamGobbler(proc.getInputStream(),
                        sPrefix);
                errGobbler.start();
                outGobbler.start();
                ProcessExitDetector detector = new ProcessExitDetector(proc);
                detector.addProcessListener(new ProcessFinishedListener() {
                    @Override
                    public void processFinished(Process process) {
                        if (LauncherThread.this.delegate != null) {
                            LauncherThread.this.delegate.launcherThreadTerminated(LauncherThread.this);
                        }
                    }
                });
                detector.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("notifying");
            this.notify();
            System.out.println("put : exiting critical section");
        }
    }
}

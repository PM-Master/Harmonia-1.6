/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.user;

/**
 *
 * @author Administrator
 */
public interface ApplicationManagerListener {

    public void applicationStarted(String applicationName, String processId, NativeProcessWrapper procWrapper);

    public void applicationTerminated(String processId);
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.application;

/**
 *
 * @author Administrator
 */
public interface NotificationSubscriber {
    public void receivedNotification(Notification n);
}

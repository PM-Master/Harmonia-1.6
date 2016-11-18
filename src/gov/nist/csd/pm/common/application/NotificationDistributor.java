package gov.nist.csd.pm.common.application;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/8/11
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public interface NotificationDistributor {
    public void postNotification(String message, Object... userData);
    public void postNotification(Object context, String message, Object... userData);
    public void postNotification(String message, NotificationType type, Object... userData);
    public void postNotification(Object context, NotificationType type, Object... userData);
    public void postNotification(Object context, String message, NotificationType type, Object... userData);
}

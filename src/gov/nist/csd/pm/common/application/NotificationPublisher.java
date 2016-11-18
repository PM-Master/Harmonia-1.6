package gov.nist.csd.pm.common.application;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/8/11
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
public interface NotificationPublisher {
    public void subscribe(NotificationSubscriber sub);
    public void subscribe(NotificationSubscriber sub, NotificationType... notificationTypes);
    public void subscribe(NotificationSubscriber sub, Object context, NotificationType... notificationTypes);
    public void unsubscribe(NotificationSubscriber sub);
}

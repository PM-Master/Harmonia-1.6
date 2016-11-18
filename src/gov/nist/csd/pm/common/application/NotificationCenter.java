package gov.nist.csd.pm.common.application;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/8/11 Time: 11:42 AM To change this template use File | Settings | File Templates.
 */
public enum NotificationCenter implements NotificationPublisher, NotificationDistributor {
    /**
	 * @uml.property  name="iNSTANCE"
	 * @uml.associationEnd  
	 */
    INSTANCE;

    /**
	 * @uml.property  name="subscribers"
	 * @uml.associationEnd  
	 */
    private Multimap<TypeContext, NotificationSubscriber> subscribers = LinkedListMultimap.create();

    @Override
    public void subscribe(NotificationSubscriber sub) {
        subscribe(sub, NotificationType.ALL);
    }

    @Override
    public void subscribe(NotificationSubscriber sub, NotificationType... notificationTypes) {
        subscribe(sub, (Object) null, (NotificationType[])notificationTypes);
    }

    @Override
    public void subscribe(NotificationSubscriber sub, Object context, NotificationType... notificationTypes) {
        for (NotificationType type : notificationTypes) {
            subscribers.put(new TypeContext(type, context), sub);
        }
    }


    @Override
    public void unsubscribe(NotificationSubscriber sub) {
        for (TypeContext tc : subscribers.keys()) {
            if (subscribers.containsEntry(tc, sub)) {
                subscribers.remove(tc, sub);
            }
        }
    }

    @Override
    public void postNotification(String message, Object... userData) {
        postNotification(null, message, NotificationType.STATUS, userData);

    }

    @Override
    public void postNotification(Object context, String message, Object... userData) {
        postNotification(context, message, NotificationType.STATUS, userData);

    }

    @Override
    public void postNotification(Object context, NotificationType type, Object...userData){
        postNotification(context, type.getName(), type, userData);
    }

    @Override
    public void postNotification(String message, NotificationType type, Object... userData) {
        postNotification(null, message, type, userData);
    }

    @Override
    public void postNotification(Object context, String message, NotificationType type, Object... userData) {
        Notification note = type.notification(message, userData);
        for (NotificationSubscriber sub : subscribers.get(new TypeContext(type, context))) {
            sub.receivedNotification(note);
        }
        if(!NotificationType.ALL.equals(type)){
            for(NotificationSubscriber allSub : subscribers.get(new TypeContext(NotificationType.ALL, null))){
                allSub.receivedNotification(note);
            }
        }

    }

    /**
	 * @author  Administrator
	 */
    private class TypeContext {
        /**
		 * @uml.property  name="_type"
		 * @uml.associationEnd  
		 */
        private NotificationType _type;
        private Object _context;

        public TypeContext(NotificationType type, Object context) {
            _type = type;
            _context = context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TypeContext)) return false;

            TypeContext that = (TypeContext) o;

            if (_context != null ? !_context.equals(that._context) : that._context != null) return false;
            if (!_type.equals(that._type)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = _type.hashCode();
            result = 31 * result + (_context != null ? _context.hashCode() : 0);
            return result;
        }
    }

}

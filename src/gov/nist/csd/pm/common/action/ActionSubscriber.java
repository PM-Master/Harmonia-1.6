package gov.nist.csd.pm.common.action;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/22/11
 * Time: 10:57 AM
 * A contract for ActionSubscriber's.  The other side of the ActionPublisher/ActionSubscriber coin.
 */
public interface ActionSubscriber {
    /**
     * Publisher's will call this method once during their lifetime, providing themselves as the parameter
     * @param host
     */
    public void startup(ActionPublisher host);

    /**
     * Publisher's will call this once this subscriber has been successfully registered with the
     * ActionPublisher.
     * @param host
     */
    public void registered(ActionPublisher host);

    /**
     * Called whenever an ActionPublisher adds a published action to its repertoire.  The intent of this
     * is to give subscribers a chance to hook into a published action.
     * @param host
     * @param action
     */
    public void actionAdded(ActionPublisher host, ActionRef action);

    /**
     * Called whenever an ActionPublisher removes a published action from its repertoire.  The intent of this
     * is to give subscribers a chance to "clean up" once an action is removed.
     * @param host
     * @param action
     */
    public void actionRemoved(ActionPublisher host, ActionRef action);

    /**
     * Called once before the end of an ActionPublisher's lifetime.  Publishers should also inform subscribers
     * that Action's provided through this publisher will no longer be available through the actionRemoved mechanism.
     * prior telling subscribers that the publisher is shutting down.
     * @param host
     */
    public void shutdown(ActionPublisher host);
}

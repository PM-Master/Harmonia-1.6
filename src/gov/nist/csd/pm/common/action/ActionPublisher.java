package gov.nist.csd.pm.common.action;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/22/11
 * Time: 11:00 AM
 * Encapsulates the contract for being an Action publisher.  An Action publisher is an advertiser of
 * extensible application functionality @see(ActionRef)
 */

public interface ActionPublisher {
    /**
     * gives a list of all actions published by this ActionPublisher
     * Actions are wrapped as ActionRef's which allows them to be
     * swapped out in-place.
     * @return
     */
    public List<ActionRef> publishedActions();

    /**
     * Notifies this publisher that a new subscriber is interested in receiving
     * published action updates.
     * @param subscriber
     */
    public void registerSubscriber(ActionSubscriber subscriber);

    /**
     * Notifies the publisher that this subscriber is no longer interested in
     * receiving published action updates.  If the provided ActionSubscriber is
     * not currently subscribed to this publisher, then the implementing ActionPublisher
     * should ignore this request.
     * @param subscriber
     */
    public void removeSubscriber(ActionSubscriber subscriber);
}

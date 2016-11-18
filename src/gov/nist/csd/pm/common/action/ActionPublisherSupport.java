package gov.nist.csd.pm.common.action;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * ActionPublisherSupport
 * @author Robert McHugh
 *
 * This is a concrete implementation for an ActionPublisher which offloads the work of
 * implemenation.
 *
 * This class also implements ActionSubscriber, allowing instances of ActionPublisher support to be nested
 * or chained.
 */
public class ActionPublisherSupport implements ActionPublisher, ActionSubscriber {
	
	/**
	 * @uml.property  name="_publishedActions"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.action.ActionRef"
	 */
	private final ArrayList<ActionRef> _publishedActions = newArrayList();
	/**
	 * @uml.property  name="_subscribers"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.common.action.ActionSubscriber"
	 */
	private final ArrayList<ActionSubscriber> _subscribers = newArrayList();
	/**
	 * @uml.property  name="_host"
	 * @uml.associationEnd  
	 */
	private final ActionPublisher _host;
	
	
	public ActionPublisherSupport(ActionPublisher host){
		_host = host == null ? this : host;
	}

	@Override
	public List<ActionRef> publishedActions() {
		System.out.println("ActionPublisherSupport.publishedActions()");
		return ImmutableList.copyOf(_publishedActions);
	}

	@Override
	public void registerSubscriber(ActionSubscriber subscriber) {
		System.out.println("ActionPublisherSupport.registerSubscriber()");
		System.out.println("Registering subscriber of class " + subscriber.getClass().getCanonicalName());
		if(_subscribers.contains(subscriber) || subscriber == this){
			return;
		}
		else{
			_subscribers.add(subscriber);
			for(ActionRef action : _publishedActions){
				subscriber.actionAdded(_host, action);
			}
            subscriber.startup(_host);
		}

	}

	@Override
	public void removeSubscriber(ActionSubscriber subscriber) {
		System.out.println("ActionPublisherSupport.removeSubscriber()");
		System.out.println("Remove subscriber of class " + subscriber.getClass().getCanonicalName());
		if(_subscribers.contains(subscriber)){
			for(ActionRef action : publishedActions()){
				subscriber.actionRemoved(_host, action);
			}
			_subscribers.remove(subscriber);
			
		}
	}
	
	public void addAction(ActionRef action){
		System.out.println("ActionPublisherSupport.addAction() for host " + _host);
		System.out.printf("Action: %s %s\n", action.getValue(ActionRef.ID), action.getValue(ActionRef.TARGET));
		if(_publishedActions.contains(action))
			return;
		_publishedActions.add(action);
		for(ActionSubscriber sub : _subscribers){
			sub.actionAdded(_host, action);
		}
	}
	
	public void removeAction(ActionRef action){
		System.out.println("ActionPublisherSupport.removeAction()");
		System.out.printf("Action: %s %s\n", action.getValue(ActionRef.ID), action.getValue(ActionRef.TARGET));
		if(!_publishedActions.contains(action))
			return;
		_publishedActions.remove(action);
		for(ActionSubscriber sub : _subscribers){
			sub.actionRemoved(_host, action);
		}
	}
	
	public void removeAllActions(){
		System.out.println("ActionPublisherSupport.removeAllActions()");
		for(ActionRef action : publishedActions()){
			removeAction(action);
		}
	}

	@Override
	public void startup(ActionPublisher host) {
		//Ignore, we're only subscribing to chain up to our subscribers
		
	}

	@Override
	public void registered(ActionPublisher host) {
		//Ignore again
		
	}

	@Override
	public void actionAdded(ActionPublisher host, ActionRef action) {
		System.out.println("ActionPublisherSupport.actionAdded()");
		System.out.printf("Action: %s %s\n", action.getValue(ActionRef.ID), action.getValue(ActionRef.TARGET));
		addAction(action);
		
	}

	@Override
	public void actionRemoved(ActionPublisher host, ActionRef action) {
		System.out.println("ActionPublisherSupport.actionRemoved()");
		System.out.printf("Action: %s %s\n", action.getValue(ActionRef.ID), action.getValue(ActionRef.TARGET));
		removeAction(action);
		
	}

	@Override
	public void shutdown(ActionPublisher host) {
		//Ignore
		
	}

    public void addAllActions(Collection<ActionRef> publishableActions) {
        for(ActionRef actionRef : publishableActions){
            addAction(actionRef);
        }
    }
}

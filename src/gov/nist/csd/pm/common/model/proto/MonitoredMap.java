package gov.nist.csd.pm.common.model.proto;

import com.google.common.collect.ForwardingMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/10/11
 * Time: 1:29 PM
 * TODO: add methods for monitoring sets and removes so that we can implement deferred updating.
 * Why am I not using Hibernate?  Didnt have time to find out if hibernate supported LDAP and NoSQL datastores.
 */
public class MonitoredMap<K,V> extends ForwardingMap<K,V> {

    /**
	 * @uml.property  name="_delegate"
	 */
    Map<K,V> _delegate = new HashMap<K,V>();

    @Override
    protected Map<K, V> delegate() {
        return _delegate;
    }
}

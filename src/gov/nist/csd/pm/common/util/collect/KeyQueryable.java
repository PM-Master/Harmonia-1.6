package gov.nist.csd.pm.common.util.collect;

import com.google.common.base.Predicate;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/8/11
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface KeyQueryable<K, V> {
    public Iterable<K> applicableKeysTestingEntries(Predicate<Map.Entry<K,V>> predicate);
    public Iterable<K> applicableKeys(Predicate<K>predicate);
}

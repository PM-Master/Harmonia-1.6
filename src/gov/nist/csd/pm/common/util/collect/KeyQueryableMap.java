package gov.nist.csd.pm.common.util.collect;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/8/11
 * Time: 1:24 PM
 * To change this template use File | Settings | File Templates.
 */
public interface KeyQueryableMap<K,V> extends Map<K,V>, KeyQueryable<K,V> {
}

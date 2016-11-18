package gov.nist.csd.pm.common.util.collect;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/22/11
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Maps {
    private Maps(){}
    public static <K, V> Function<Map.Entry<K,V>, K> keyFromEntry(Class<K> keyType){
        return new Function<Map.Entry<K,V>, K>(){

            @Override
            public K apply(@Nullable Map.Entry<K, V> kvEntry) {
                return kvEntry != null ? kvEntry.getKey() : null;
            }
        };
    }
    public static <K,V> Function<Map.Entry<K,V>, V> valueFromEntry(Class<V> valueType){
        return new Function<Map.Entry<K,V>, V>(){

            @Override
            public V apply(@Nullable Map.Entry<K, V> kvEntry) {
                return kvEntry != null ? kvEntry.getValue() : null;
            }
        };
    };

    public static <K,V> KeyQueryableMap<K,V> keyQueryableMap(){
        return new KeyQueryableHashMap<K, V>();
    }

    private static class KeyQueryableSupport<K,V> implements KeyQueryable<K,V>{
        private final Map<K,V> _keyQueryTarget;
        public KeyQueryableSupport(Map<K,V> keyQueryTarget){
            _keyQueryTarget = keyQueryTarget;
        }

        @Override
        public Iterable<K> applicableKeysTestingEntries(Predicate<Map.Entry<K, V>> entryPredicate) {
            if(_keyQueryTarget != null){
                return Iterables.transform(Iterables.filter(_keyQueryTarget.entrySet(), entryPredicate),
                        new Function<Map.Entry<K, V>, K>() {
                            @Override
                            public K apply(@Nullable Map.Entry<K, V> input) {
                                return input != null ? input.getKey() : null;
                            }
                        });
            }
            return new ArrayList<K>();
        }

        @Override
        public Iterable<K> applicableKeys(Predicate<K> kPredicate) {
            if(_keyQueryTarget != null){
                return Iterables.filter(_keyQueryTarget.keySet(), kPredicate);
            }
            return new ArrayList<K>();
        }
    }

    /**
	 * @author  Administrator
	 */
    private static class KeyQueryableHashMap<K,V> extends ForwardingMap<K,V> implements KeyQueryableMap<K,V>{

        private final Map<K,V> _delegate = new HashMap<K,V>();
        /**
		 * @uml.property  name="_kks"
		 * @uml.associationEnd  
		 */
        private final KeyQueryableSupport<K,V> _kks;

        public KeyQueryableHashMap() {
            super();
            _kks = new KeyQueryableSupport<K,V>(_delegate);

        }

        @Override
        protected Map<K, V> delegate() {
            return _delegate;
        }

        @Override
        public Iterable<K> applicableKeysTestingEntries(Predicate<Entry<K, V>> entryPredicate) {
            return _kks.applicableKeysTestingEntries(entryPredicate);
        }

        @Override
        public Iterable<K> applicableKeys(Predicate<K> kPredicate) {
            return _kks.applicableKeys(kPredicate);
        }
    }
}

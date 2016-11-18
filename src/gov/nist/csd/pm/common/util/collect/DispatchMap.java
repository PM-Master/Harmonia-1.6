package gov.nist.csd.pm.common.util.collect;

import gov.nist.csd.pm.common.pdf.support.PDFieldSwingAdapters;

import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/20/11
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class DispatchMap<K, V> extends LinkedHashMap<K, V> {

    /**
	 * @uml.property  name="defaultValue"
	 */
    private final V defaultValue;
    /**
	 * @uml.property  name="comparator"
	 */
    private final Comparator<? super K> comparator;

    public DispatchMap() {
        this(PDFieldSwingAdapters.EQUALITY_COMPARATOR);
    }

    public DispatchMap(Comparator<? super K> comparator) {
        this(null, comparator);
    }

    public DispatchMap(V defaultValue) {
        this(defaultValue, PDFieldSwingAdapters.EQUALITY_COMPARATOR);
    }

    public DispatchMap(V defaultValue, Comparator<? super K> comparator) {
        this.defaultValue = defaultValue;
        this.comparator = comparator;
    }

    @Override
    public V get(Object key) {
        K typedKey = (K) key;
        int bestResult = Integer.MAX_VALUE;
        java.util.Map.Entry<K, V> foundEntry = null;

        for (java.util.Map.Entry<K, V> candidateEntry : this.entrySet()) {
            int result = comparator.compare(candidateEntry.getKey(), typedKey);
            if (result >= 0 && result < bestResult) {
                foundEntry = candidateEntry;
                bestResult = result;
            }
        }
        V found = foundEntry == null ? defaultValue : foundEntry.getValue();
        return found;
    }



}


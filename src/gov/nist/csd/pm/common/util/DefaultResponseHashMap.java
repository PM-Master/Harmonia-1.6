/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.util;

import java.util.HashMap;

/**
 * @author Administrator
 */
public class DefaultResponseHashMap<K, V> extends HashMap<K, V> {

        private DefaultResponseHashMap() {
            super();
        }
        /**
		 * @uml.property  name="defaultValue"
		 */
        private V defaultValue = null;

        public DefaultResponseHashMap(V defaultValue) {
            this();
            if(defaultValue == null){
                throw new Error("Cannot use a DefaultResponseHashMap without a default value.");
            }
            this.defaultValue = defaultValue;
        }

        @Override
        public V get(Object key) {
            System.out.println("in get");
            return super.containsKey((K) key) ? super.get((K) key) : defaultValue;
        }
    }
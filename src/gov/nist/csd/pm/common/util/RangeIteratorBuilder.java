/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.util;

import com.google.common.base.Preconditions;

import java.util.Iterator;

/**
 *
 * @author Administrator
 */
public class RangeIteratorBuilder {

    /**
	 * @uml.property  name="start"
	 */
    Long start = Long.valueOf(0);
    /**
	 * @uml.property  name="finish"
	 */
    Long finish = Long.MAX_VALUE;
    /**
	 * @uml.property  name="by"
	 */
    Long by = Long.valueOf(1);

    /**
     * Changes the starting value of the iterator to startValue
     * @param startValue
     * @return
     */
    public RangeIteratorBuilder from(Long startValue) {
        Preconditions.checkNotNull(startValue);
        this.start = startValue;
        return this;
    }

    /**
     * Changes the ending value of the resulting iterator to endValue
     * @param endValue
     * @return
     */
    public RangeIteratorBuilder upTo(Long endValue) {
        Preconditions.checkNotNull(endValue);
        this.finish = endValue + 1;
        return this;
    }

    /**
     * Changes the ending value of the resulting iterator to endValue - 1
     * @param endValue
     * @return
     */
    public RangeIteratorBuilder until(Long endValue) {
        Preconditions.checkNotNull(endValue);
        this.finish = endValue;
        return this;
    }

    /**
     * Changes the increment of the resulting iterator
     * @param increment
     * @return
     */
    public RangeIteratorBuilder by(Long increment){
        Preconditions.checkNotNull(increment);
        this.by = increment;
        return this;
    }

    /**
     * Changes the end value of the iterator to be times + startValue
     * @param times
     * @return
     */

    public static RangeIteratorBuilder times(Integer times){
        return times(Long.valueOf(times.longValue()));
    }

    /**
     * Changes the end value of the iterator to be times + startValue
     * @param times
     * @return
     */

    public static RangeIteratorBuilder times(Long times){
        return new RangeIteratorBuilder().until(times);
    }

    /**
     * Returns an iterator for the specified range
     * @return
     */
    public Iterator<Long> iterator() {
        final Long _start = this.start;
        final Long _finish = this.finish;
        final Long _by = this.by;
        return new Iterator<Long>() {

            Long current = _start;

            @Override
            public boolean hasNext() {
                return current != _finish;
            }

            @Override
            public Long next() {
                if(!hasNext()){
                    throw new IllegalStateException("next called on iterator with no iterations remaining.");
                }
                current += _by;
                return current;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}

package gov.nist.csd.pm.common.pattern;

import com.google.common.base.Predicate;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/29/11
 * Time: 12:42 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Walkable<T> {
    public void traverse(Visitor<T> visitor);
    public void traverse(Visitor<T> visitor, Predicate<? super T> visitPredicate);
    public <R> R reduce(Reducer<T, R> reducer);
    public <R> R reduce(Reducer<T, R> reducer, Predicate<? super T> inclusionPredicate);
    public T getNode();
    public Collection<Walkable<T>> neighbors();
}

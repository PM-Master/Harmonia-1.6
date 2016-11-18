package gov.nist.csd.pm.common.pattern;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/29/11
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractWalkable<T> implements Walkable<T> {

    /**
	 * @uml.property  name="_node"
	 */
    private final T _node;
    /**
	 * @uml.property  name="_parent"
	 * @uml.associationEnd  
	 */
    private final Walkable<T> _parent;

    public AbstractWalkable(T node){
        this(node, null);
    }

    public AbstractWalkable(T node, Walkable<T> parent){
        _node = node;
        _parent = parent == null ? this : parent;
    }

    @Override
    public T getNode(){
        return _node;
    }

    @Override
    public void traverse(Visitor<T> tVisitor) {
        traverse(tVisitor, Predicates.alwaysTrue());
    }

    @Override
    public void traverse(Visitor<T> tVisitor, Predicate<? super T> visitPredicate) {
        if(tVisitor != null && visitPredicate.apply(_node)){
            tVisitor.visit(_node);
        }
        for(Walkable<T> walkable : neighbors()){
            walkable.traverse(tVisitor, visitPredicate);
        }
    }

    @Override
    public <R> R reduce(Reducer<T, R> trReducer) {
        return reduce(trReducer, Predicates.alwaysTrue());
    }

    @Override
    public <R> R reduce(Reducer<T, R> trReducer, Predicate<? super T> inclusionPredicate) {
        List<R> results = new ArrayList<R>();
        for(Walkable<T> walkable : neighbors()){
            R result = walkable.reduce(trReducer, inclusionPredicate);
            if(result != null){
                results.add(result);
            }
        }
        return  inclusionPredicate.apply(_node) ? trReducer.reductionStep(_node, results) : null;

    }


    /**
	 * @author  Administrator
	 */
    class ChildWalkable<T> extends AbstractWalkable<T>{
        /**
		 * @uml.property  name="_superWalkable"
		 * @uml.associationEnd  
		 */
        private final AbstractWalkable<T> _superWalkable;
        public ChildWalkable(T node, AbstractWalkable<T> superWalkable) {
            super(node);
            _superWalkable = superWalkable;
        }

        @Override
        public Collection<? extends T> neighborNodes(T parentNode) {
            return _superWalkable.neighborNodes(parentNode);
        }
    }

    @Override
    public Collection<Walkable<T>> neighbors() {
        Collection<Walkable<T>> walkables = newArrayList();
        T node = getNode();
        Collection<? extends T> neighborNodes = neighborNodes(node);
        neighborNodes = neighborNodes == null ? new ArrayList<T>() : neighborNodes;
        for(T sub : neighborNodes){
            walkables.add(new ChildWalkable<T>(sub, this));
        }
        return walkables;
    }

    public abstract Collection<? extends T> neighborNodes(T parentNode);

}

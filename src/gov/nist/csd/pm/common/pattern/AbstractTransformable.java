package gov.nist.csd.pm.common.pattern;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/29/11
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTransformable<F> extends AbstractWalkable<F> implements Transformable<F> {
    public AbstractTransformable(F node) {
        super(node);
    }

    /**
	 * @author  Administrator
	 */
    private class TransformedVisitor<T> implements Visitor<T> {
        /**
		 * @uml.property  name="_origin"
		 * @uml.associationEnd  
		 */
        private final Visitor<F> _origin;
        private final Function<T,F> _transform;
        public TransformedVisitor(Visitor<F> origin, Function<T, F> transform){
            _origin = origin;
            _transform = transform;
        }

        @Override
        public void visit(T node) {
            _origin.visit(_transform.apply(node));
        }
    }


    /**
	 * @author  Administrator
	 */
    private class TransformedReducer<T, R> implements Reducer<T, R>{
        /**
		 * @uml.property  name="_origin"
		 * @uml.associationEnd  
		 */
        private final Reducer<F, R> _origin;
        private final Function<T, F> _transform;

        public TransformedReducer(Reducer<F, R>origin, Function<T, F> transform){
            _origin = origin;
            _transform = transform;
        }
        @Override
        public R reductionStep(T current, Collection<R> neighborResults) {
            return _origin.reductionStep(_transform.apply(current), neighborResults);
        }
    }


    /**
	 * @author  Administrator
	 */
    private class TransformedWalkable<F, T> implements Walkable<T> {
        /**
		 * @uml.property  name="_origin"
		 * @uml.associationEnd  
		 */
        private final Walkable<F> _origin;
        private final Function<F, T> _transform;
        private final Function<Walkable<F>, Walkable<T>> _convertToTransformedWalkable = new Function<Walkable<F>, Walkable<T>>(){

        @Override
        public Walkable<T> apply(@Nullable Walkable<F> walkable) {
            return new TransformedWalkable(walkable, _transform);
        }
    };

        public TransformedWalkable(Walkable<F> origin, Function<F, T> transform) {
            _origin = origin;
            _transform = transform;
        }

        @Override
        public void traverse(Visitor<T> visitor) {
            _origin.traverse(new TransformedVisitor(visitor, _transform));
        }

        @Override
        public void traverse(Visitor<T> visitor, Predicate<? super T> visitPredicate) {
            _origin.traverse(new TransformedVisitor(visitor, _transform), Predicates.compose(visitPredicate, _transform));
        }

        @Override
        public <R> R reduce(Reducer<T, R> trReducer) {
            return (R)_origin.reduce(new TransformedReducer(trReducer, _transform));
        }

        @Override
        public <R> R reduce(Reducer<T, R> trReducer, Predicate<? super T> inclusionPredicate) {
            return (R)_origin.reduce(new TransformedReducer(trReducer, _transform), Predicates.compose(inclusionPredicate, _transform));
        }

        @Override
        public T getNode() {
            return _transform.apply(_origin.getNode());
        }

        @Override
        public Collection<Walkable<T>> neighbors() {
            return Collections2.transform(_origin.neighbors(), _convertToTransformedWalkable);
        }
    }

    ;

    @Override
    public <T> Walkable<T> transform(Function<F, T> transformFunction) {
        return new TransformedWalkable(this, transformFunction);
    }
}

package gov.nist.csd.pm.common.pattern;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/29/11
 * Time: 5:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class WalkableSupport<T> extends AbstractWalkable<T> {
    /**
	 * @uml.property  name="_subNodeFunction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private Function<T, Collection<T>> _subNodeFunction;
    public WalkableSupport(T node, Function<T, Collection<T>> subNodeFunction){
        super(node);
        _subNodeFunction = subNodeFunction;
    }

    public WalkableSupport(T node){
        this(node, null);
    }

    @Override
    public Collection<T> neighborNodes(T parentNode) {
        return _subNodeFunction == null ? new ArrayList<T>() : _subNodeFunction.apply(parentNode);
    }
}

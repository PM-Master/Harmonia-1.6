package gov.nist.csd.pm.common.pattern;

import com.google.common.base.Supplier;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/29/11
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class CollectingVisitor<T> implements Visitor<T>, Supplier<Collection<T>> {
    /**
	 * @uml.property  name="collection"
	 */
    private final List<T> collection = newArrayList();

    @Override
    public void visit(T node) {
        collection.add(node);
    }

    @Override
    public Collection<T> get() {
        return newArrayList(collection);
    }
}

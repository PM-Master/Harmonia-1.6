/*
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS".  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof including, but
 * not limited to, the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */

package gov.nist.csd.pm.common.browser;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingList;

import gov.nist.csd.pm.common.util.Log;

import javax.annotation.Nullable;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Objects.firstNonNull;
import static gov.nist.csd.pm.common.util.collect.Arrays.getOrElse;

/**
 * This class implements a node in a PM graph.
 * @author  steveq@nist.gov
 * @version  $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since  6.0
 */

public class PmNode implements Serializable {
	static Log log = new Log(Log.Level.INFO, true);

	  public boolean isAccessible = true;

    /**
	 * @uml.property  name="sType"
	 * @uml.associationEnd  
	 */
    public PmNodeType sType;
    /**
	 * @uml.property  name="sLabel"
	 */
    public String sLabel;
    /**
	 * @uml.property  name="sId"
	 */
    public String sId;
    /**
	 * @uml.property  name="parent"
	 * @uml.associationEnd  
	 */
    public PmNode parent;
    /**
	 * @uml.property  name="children"
	 * @uml.associationEnd  multiplicity="(0 -1)" inverse="parent:gov.nist.csd.pm.common.browser.PmNode"
	 */
    public List<PmNode> children;
    /**
	 * @uml.property  name="childProvidingDelegate"
	 * @uml.associationEnd  
	 */
    transient
    public ChildProvidingDelegate<PmNode> childProvidingDelegate;
    /**
	 * @uml.property  name="nodeGraphListeners"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="javax.swing.event.TreeModelListener"
	 */
    transient
    public List<TreeModelListener> nodeGraphListeners;

    public static final String NAME_KEY = "node_name";
    public static final String ID_KEY = "node_id";
    public static final String TYPE_KEY = "node_type";




    public static PmNode createObjectNode(String name){
        return new PmNode(PmNodeType.OBJECT, name);
    }

    public static PmNode createContainerNode(String name){
        return new PmNode(PmNodeType.CONTAINER,  name);
    }

    public static PmNode createFileNode(String name){
        return new PmNode(PmNodeType.FILE, name);
    }

    public static PmNode createObjectAttributeNode(String name){
        return new PmNode(PmNodeType.OBJECT_ATTRIBUTE, name);
    }

    public PmNode(PmNodeType type, String name){
        this(type.typeCode(), null, name);
    }

    public PmNode(String[] args) {
        this(args, null);
    }

    public PmNode(String[] args, ChildProvidingDelegate<PmNode> providingDelegate) {
        this(getOrElse(args, 0, ""),
        		getOrElse(args, 1, ""), 
        		getOrElse(args, 2, ""), 
        		providingDelegate);
        
        log.debug("args.length: " + args.length);
        for (int i = 0; i < args.length; i++) {
        	log.debug("%%%% args[" + i + "]: " + args[i]);
        }
        
        if (args.length >= 4) {
        	log.debug(">>>> isAcc param: " + args[3]);
        	if (args[3].equals("true")) {
        		log.debug("@@@@ isAcc is true!");
        		isAccessible = true;
        	} else if (args[3].equals("false")){
        		log.debug("@@@@ isAcc is false!");
        		isAccessible = false;
        	} else {
            	log.warn("???? Invalid accessible parameter value");
        	}
        } else {
        	log.debug("#### No accessible parameter seen");
        }
    }

    public PmNode(String sType, String sId, String sLabel) {
        this(sType, sId, sLabel, null);
    }

    public PmNode(String sType, 
    		String sId, 
    		String sLabel, 
    		ChildProvidingDelegate<PmNode> providingDelegate) {
        log.debug("TRACE 8 - In PmNode constructor");

        setValues(sType, sId, sLabel);
        nodeGraphListeners = new ArrayList();
        if (providingDelegate == null) {
            children = new ArrayList();
        } else {
            childProvidingDelegate = providingDelegate;
        }
    }

    public void addTreeModelListener(TreeModelListener listener) {
        if(listener == null){
            return;
        }
        nodeGraphListeners.add(listener);
    }

    public void removeTreeModelListener(TreeModelListener listener) {
        if(nodeGraphListeners.contains(listener)){
            nodeGraphListeners.remove(listener);
        }
    }

    private void setValues(String sType, String sId, String sLabel) {
        this.sType = PmNodeType.typeForCode(sType);
        this.sId = sId;
        this.sLabel = sLabel;
    }


    public static void linkNodes(PmNode parent, PmNode child) {
        log.debug("TRACE 17 - In PmNode.linkNodes() 2: " + "linking parent '" + 
        		parent.sLabel + "' with child '" + child.sLabel + "'");
        
    	// Steve - Observation (4/14/16): This is HORRIBLE CODING!
        if (child.getParent() != null && child.getParent().getChildren().contains(child)) {
            child.getParent().getChildren().remove(child);
        }
        if (!parent.getChildren().contains(child)) {
            parent.getChildren().add(child);
        }
        if(child.getParent() == null ||
                !child.getParent().equals(parent)){
            child.setParent(parent);
        }
    }

    public static void linkNodes(PmNode parent, Collection<PmNode> children) {
        log.debug("TRACE 16 - In PmNode.linkNodes() 1");
        
    	if (parent != null)
    		log.debug("In linkNodes: parent: " + parent.sLabel);
    	else {
    		log.warn("In linkNodes: parent is null. WHAT DO WE DO HERE?");
    	}
    	if (children != null) {
    		log.debug("In linkNodes: children size: " + children.size());
    	} else {
    		log.warn("In linkNodes: children is null - PmEngine sent null children? WHAT DO WE DO HERE?");
    	}
        for (PmNode child : children) {
            linkNodes(parent, child);
        }
    }

    public static void linkNodes(PmNode parent, PmNode[] children) {
    	log.debug("TRACE PmNode.linkNodes1()");

        linkNodes(parent, Arrays.asList(children));
    }

    public static void linkNodes(Collection<PmNode> parents, Collection<PmNode> children) {
    	log.debug("TRACE PmNode.linkNodes2()");

        for (PmNode parent : parents) {
            linkNodes(parent, children);
        }
    }


    public static void linkNodes(PmNode[] parents, PmNode[] children) {
        linkNodes(Arrays.asList(parents), Arrays.asList(children));
    }


    public static List<PmNode> createAll(List<String[]> rawNodeData, ChildProvidingDelegate<PmNode> childProvidingDelegate) {
        log.debug("TRACE 26 - In PmNode.createAll() -- NODES CREATED HERE!");

        List<PmNode> resultNodes = new ArrayList();
        for (String[] rawNode : rawNodeData) {
            resultNodes.add(new PmNode(rawNode, childProvidingDelegate));
        }
        return resultNodes;
    }

    public static PmNode createFrom(PmNode node, ChildProvidingDelegate<PmNode> childProvidingDelegate) {
        PmNode anode = new PmNode(node.getType(), node.getId(), node.getName(), childProvidingDelegate);
        anode.setParent(node.getParent());
        return anode;
    }

    public PmNode rename(String newName){
        return new PmNode(getType(), getId(), newName, childProvidingDelegate);
    }

    public String toString() {
        return String.format("PmNode: Id - %s, Name - %s, Type - %s", getId(), getName(), getNodeType().name());
    }


    public String getName() {
        return sLabel;
    }


    public String getType() {
        return sType.typeCode();
    }


    public PmNodeType getNodeType(){
        return sType;
    }


    public String getId() {
        return sId;
    }


    /**
	 * @return
	 * @uml.property  name="parent"
	 */
    public PmNode getParent() {
        return parent;
    }

    /**
	 * @param parent
	 * @uml.property  name="parent"
	 */
    public void setParent(PmNode parent) {

        this.parent = parent;
    }

    /**
	 * @return
	 * @uml.property  name="childProvidingDelegate"
	 */
    public ChildProvidingDelegate<PmNode> getChildProvidingDelegate() {
        return childProvidingDelegate;
    }


    public PmNode[] getPath() {
        List<PmNode> nodeInfoPath = new ArrayList<PmNode>();
        nodeInfoPath.add(this);
        while (nodeInfoPath.get(0).getParent() != null) {
            nodeInfoPath.add(0, nodeInfoPath.get(0).getParent());
        }
        return nodeInfoPath.toArray(new PmNode[0]);
    }


    /**
	 * @return
	 * @uml.property  name="children"
	 */
    public List<PmNode> getChildren() {
        log.debug("TRACE 13a - In PmNode.getChildren()");
        if (childProvidingDelegate != null && children == null) {
            log.warn("TRACE 13b - " + sLabel + " has NULL children!");

            children =  childProvidingDelegate.getChildrenOf(this);

            linkNodes(this, children);

        } else {
            log.debug("TRACE 13c - " + sLabel + " has " + children.size() + " children");
        }

        return new ChildMonitoringList();
    }

    public void treeNodesChanged(TreeModelEvent treeModelEvent) {
        if(parent != null){
            parent.treeNodesChanged(treeModelEvent);
        }
        else{
            for (TreeModelListener listener : nodeGraphListeners) {
                listener.treeNodesChanged(treeModelEvent);
            }
        }
    }

    public void treeNodesInserted(TreeModelEvent treeModelEvent) {
        if(parent != null){
            parent.treeNodesInserted(treeModelEvent);
        }
        else{
            for (TreeModelListener listener : nodeGraphListeners) {
                listener.treeNodesInserted(treeModelEvent);
            }
        }

    }

    public void treeNodesRemoved(TreeModelEvent treeModelEvent) {
        if(parent != null){
            parent.treeNodesRemoved(treeModelEvent);
        }
        else{
            for (TreeModelListener listener : nodeGraphListeners) {
                listener.treeNodesRemoved(treeModelEvent);
            }
        }
    }

    public void treeStructureChanged(TreeModelEvent treeModelEvent) {

        for (TreeModelListener listener : nodeGraphListeners) {
            listener.treeStructureChanged(treeModelEvent);
        }
    }

    private void clearChildCache(){
        children = null;
    }

    public void invalidate(){
        clearChildCache();
        treeStructureChanged(new TreeModelEvent(this, this.getPath()));
    }

    class ChildMonitoringList extends ForwardingList<PmNode> {
        @Override
        protected List<PmNode> delegate() {
            return children;
        }

        @Override
        public boolean add(PmNode element) {
            boolean addedSuccessful = super.add(element);
            if (addedSuccessful) {
                int indexOfChild = delegate().size() - 1;
                PmNode.this.treeNodesInserted(
                        new TreeModelEvent(
                                PmNode.this,
                                PmNode.this.getPath(),
                                new int[]{indexOfChild},
                                new Object[]{element}));
            }
            return addedSuccessful;
        }

        @Override
        public void add(int index, PmNode element) {
            throw new UnsupportedOperationException("Adding at an index is not supported");

        }

        @Override
        public boolean addAll(int index, Collection<? extends PmNode> elements) {
            throw new UnsupportedOperationException("Adding all at an index is not supported");
        }

        @Override
        public boolean remove(Object object) {
            if (object instanceof PmNode && super.contains(object)){
                PmNode nodeInfo = (PmNode) object;
                PmNode parent = PmNode.this;
                if (parent != null) {
                    PmNode[] pathToParent = parent.getPath();
                    int indexOfObject = super.indexOf(object);
                    parent.treeNodesRemoved(
                            new TreeModelEvent(
                                    parent,
                                    pathToParent,
                                    new int[]{indexOfObject},
                                    new Object[]{object}));
                }
                return super.remove(nodeInfo);
            }

            return false;


        }

        @Override
        public boolean addAll(Collection<? extends PmNode> pmNodes) {
            for(PmNode node : pmNodes){
                add(node);
            }
            return true;
        }

        @Override
        public PmNode remove(int index) {
            PmNode removed = get(index);
            remove(removed);
            return removed;
        }


    }





    public int getChildCount() {
        log.debug("TRACE 12 - In PmNode.getChildCount()");
        return getChildren().size();
    }

    public PmNode getChildAt(int i) {
        return getChildren().get(i);
    }

    public int getIndexOfChild(PmNode child) {
        return getChildren().indexOf(child);
    }

    public interface ChildProvidingDelegate<T> {
        public List<T> getChildrenOf(T parent);
    }

    /**
	 * @author   Administrator
	 */
    public static enum EntryKey{
        /**
		 * @uml.property  name="nAME"
		 * @uml.associationEnd  
		 */
        NAME(2), /**
		 * @uml.property  name="iD"
		 * @uml.associationEnd  
		 */
        ID(1), /**
		 * @uml.property  name="tYPE"
		 * @uml.associationEnd  
		 */
        TYPE(0);
        EntryKey(int indexInNormalizedArray){
            this.normalIndex = indexInNormalizedArray;
        }
        private int normalIndex;
        public int normalIndex(){
            return normalIndex;
        }
    }

    private static final Integer INVALID_MAPPING = new Integer(-1);

    public abstract static class TransformFunction implements Function<String[], PmNode>{


        public TransformFunction(){
            super();
        }

        private <T> boolean isValidIndex(T[] arr, int index){
            return arr != null && (index >= 0 && index < arr.length);
        }

        private Integer lookup(EntryKey key){
            return firstNonNull(getEntryIndexMapping().get(key), INVALID_MAPPING);
        }

        abstract public Map<EntryKey, Integer> getEntryIndexMapping();

        @Override
        public PmNode apply(@Nullable String[] strings) {
            EnumSet<EntryKey> enums = EnumSet.allOf(EntryKey.class);
            String[] target = new String[]{"","",""};
            for(EntryKey key : enums){
                Integer sourceIndex = lookup(key);
                if(isValidIndex(strings, sourceIndex)){
                    target[key.normalIndex()] = strings[sourceIndex];
                }
            }
            return new PmNode(target);

        }
    }

    /**
	 * @uml.property  name="getEntityWithPropTransformFunction"
	 * @uml.associationEnd  
	 */
    public static final TransformFunction getEntityWithPropTransformFunction = new GetEntityWithPropTransformFunction();

    static class GetEntityWithPropTransformFunction extends TransformFunction{

        private final Map<EntryKey, Integer> entryIndexMap = Collections.unmodifiableMap(
                new HashMap<EntryKey, Integer>(){{
                    put(EntryKey.NAME, 0);
                    put(EntryKey.ID, 1);
                    put(EntryKey.TYPE, 2);
                }});;

        private GetEntityWithPropTransformFunction(){
            super();
        }

        @Override
        public Map<EntryKey, Integer> getEntryIndexMapping() {
            return entryIndexMap;
        }
    }
}

/* This software was developed by employees of the National Institute of
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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import gov.nist.csd.pm.common.util.Log;
import gov.nist.csd.pm.common.util.collect.Arrays;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 6.0
 */
public class PmGraph extends JTree implements TreeModelListener {

	Log log = new Log(Log.Level.INFO, true);

    public static final  DataFlavor pmNodeDataFlavor = new DataFlavor(PmNode[].class, "PM Node Object Array");

    /**
     * @param graphNode
     * @param ignoreIsAccessible Session users are restricted from accessing PM nodes
	 * where pmNode.isAccessible is false. All other users (e.g., Admins) ignore
	 * this parameter.
     */
    public PmGraph(PmNode graphNode, boolean ignoreIsAccessible) {
        super(new PmGraphModel(graphNode));
        log.debug("TRACE 19 - In PmGraph constructor - After super(PmGraphModel)");

        getModel().addTreeModelListener(this);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        PmGraphRenderer renderer = new PmGraphRenderer(ignoreIsAccessible);
        setCellRenderer(renderer);
        setDragEnabled(true);
        setTransferHandler(new PmNodeTransferHandler());
        log.debug("TRACE 20 - In PmGraph constructor END");

    }

    private class PmNodeTransferHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent jComponent) {
            return DnDConstants.ACTION_LINK;
        }

        @Override
        protected Transferable createTransferable(JComponent jComponent) {
            JTree tree = (JTree)jComponent;
            TreePath path = tree.getSelectionPath();
            Object[] pathObjects = path.getPath();
            if(!Arrays.isNullOrEmpty(pathObjects) && pathObjects[0] instanceof PmNode){
                PmNode[] pmNodePath = Arrays.cast(pathObjects, new PmNode[pathObjects.length]);
                return new PmNodeTransferable(pmNodePath);
            }
            return null;
        }

        @Override
        protected void exportDone(JComponent jComponent, Transferable transferable, int i) {
            super.exportDone(jComponent, transferable, i);
        }
    }

    /**
	 * @author  Administrator
	 */
    private static class PmNodeTransferable implements Transferable{

        private final Map<DataFlavor, Object> objectsForFlavor;
        /**
		 * @uml.property  name="node"
		 * @uml.associationEnd  multiplicity="(0 -1)"
		 */
        private PmNode[] node;

        private PmNodeTransferable(PmNode[] nodeToTransfer){
            node = nodeToTransfer;
            objectsForFlavor = new HashMap<DataFlavor, Object>(){{
                put(pmNodeDataFlavor, node);
                put(DataFlavor.stringFlavor, node.toString());
            }};
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return objectsForFlavor.keySet().toArray(new DataFlavor[0]);
        }

        @Override
        public boolean isDataFlavorSupported(final DataFlavor dataFlavor) {
            return Iterables.any(objectsForFlavor.keySet(), new Predicate<DataFlavor>(){
                @Override
                public boolean apply(@Nullable DataFlavor input) {
                    if(input == null){
                        return dataFlavor == null;
                    }
                    else{
                        return input.match(dataFlavor);
                    }
                }
            });
        }

        @Override
        public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
            return objectsForFlavor.get(dataFlavor);
        }


    }



    @Override
    public void treeNodesChanged(TreeModelEvent treeModelEvent) {

    }

    @Override
    public void treeNodesInserted(TreeModelEvent treeModelEvent) {
        if(treeModelEvent == null || treeModelEvent.getTreePath() == null){
            return;
        }
        this.expandPath(treeModelEvent.getTreePath());
        Object lastComponent = treeModelEvent.getTreePath().getLastPathComponent();
        if(lastComponent instanceof PmNode){
            PmNode parentWithNewNodes = (PmNode)lastComponent;
            if(parentWithNewNodes.getChildren().size() == 1){
                //Special case of a new child.  We would like to expand this
                expandPath(treeModelEvent.getTreePath());
            }
        }
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent treeModelEvent) {

    }

    @Override
    public void treeStructureChanged(TreeModelEvent treeModelEvent) {
    
    }
    public void reload(PmGraph tree)
    {
    	((PmGraphModel)tree.getModel()).reload();
    }
}

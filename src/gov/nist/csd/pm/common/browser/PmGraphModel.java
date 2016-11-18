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

import gov.nist.csd.pm.common.util.Log;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 6.0
 */
public class PmGraphModel implements TreeModel {

	Log log = new Log(Log.Level.INFO, true);

    /**
	 * @uml.property  name="rootNode"
	 * @uml.associationEnd  
	 */
    private PmNode rootNode;

    public PmGraphModel(PmNode root) {
        log.debug("TRACE 10 - In PmGraphModel constructor - Called from PmGraph constructor");
        rootNode = root;
    }
    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     */
    public void addTreeModelListener(TreeModelListener l) {
        rootNode.addTreeModelListener(l);
    }

    /**
     * Returns the child of parent at index index in the parent's child array.
     */
    public Object getChild(Object parent, int index) {
        PmNode p = (PmNode) parent;
        return p.getChildAt(index);
    }

    /**
     * Returns the number of children of parent.
     */
    public int getChildCount(Object parent) {
        PmNode p = (PmNode) parent;
        return p.getChildCount();
    }

    /**
     * Returns the index of child in parent.
     */
    public int getIndexOfChild(Object parent, Object child) {
        PmNode p = (PmNode) parent;
        return p.getIndexOfChild((PmNode) child);
    }

    /**
     * Returns true if node is a leaf.
     */
    public boolean isLeaf(Object node) {

        PmNode p = (PmNode) node;
        log.debug("TRACE 11 - In PmGraphModel.isLeaf(): node = " + p.sLabel);
        return p.getChildCount() == 0;
    }

    /**
     * Returns the root of the tree.
     */
    public Object getRoot() {
        return rootNode;
    }

    /**
     * Removes a listener previously added with addTreeModelListener().
     */
    public void removeTreeModelListener(TreeModelListener l) {
        rootNode.removeTreeModelListener(l);
    }

    /**
     * Messaged when the user has altered the value for the item
     * identified by path to newValue.  Not used by this model.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    public void reload()
    {

    	
    }
}

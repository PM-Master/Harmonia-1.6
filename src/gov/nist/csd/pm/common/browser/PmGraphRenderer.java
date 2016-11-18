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

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * This class implements an signature tester.
 *
 * @author steveq@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 6.0
 */
public class PmGraphRenderer extends DefaultTreeCellRenderer {

	private boolean ignoreIsAccessible = false;
	
	/**
	 * @param ignoreIsAccessible Session users are restricted from accessing PM nodes
	 * where pmNode.isAccessible is false. All other users (e.g., Admins) ignore
	 * this parameter.
	 */
	public PmGraphRenderer (boolean ignoreIsAccessible) {
		this.ignoreIsAccessible = ignoreIsAccessible;
	}

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        PmNode pmNode = (PmNode) (value);
        String nodeType = pmNode.getType();
        setText(pmNode.getName());
        switch (PmNodeType.typeForCode(nodeType)) {
            case CONNECTOR:
                setIcon(PmNodeIcons.getIconForNodeType(pmNode.getNodeType()));
                setToolTipText("This is the connector");
                break;
            case USER:
                setIcon(PmNodeIcons.getIconForNodeType(pmNode.getNodeType()));
                setToolTipText("This is a user");
                break;
            case USER_ATTRIBUTE:
                setIcon(PmNodeIcons.getIconForNodeType(pmNode.getNodeType()));
                setToolTipText("This is a user attribute");
                break;
            case POLICY:
                setIcon(PmNodeIcons.getIconForNodeType(pmNode.getNodeType()));
                setToolTipText("This is a policy class");
                break;
            case OBJECT:
            	if (pmNode.isAccessible || ignoreIsAccessible) {
                    setIcon(PmNodeIcons.getIconForNodeType(pmNode.getNodeType()));
                    setToolTipText("This is an object");
            	} else {
                    setIcon(PmNodeIcons.getIconForNodeType(PmNodeType.OBJECT_DISABLED));
                    setToolTipText("This object is not accessible");
            	}
                break;
            case CONTAINER: //Alias for object attribute, the fall-through is intentional
            case OBJECT_ATTRIBUTE:
            	if (pmNode.isAccessible || ignoreIsAccessible) {
                    setIcon(PmNodeIcons.getIconForNodeType(pmNode.getNodeType()));
                    setToolTipText("This is an object attribute");
            	} else {
                    setIcon(PmNodeIcons.getIconForNodeType(PmNodeType.OBJECT_ATTRIBUTE_DISABLED));
                    setToolTipText("This object attribute is not accessible");
            	}               
            	break;
            case OPSET:
            	setIcon(PmNodeIcons.getIconForNodeType(PmNodeType.OPSET));
                setToolTipText("This is an operation set");
            case SYNC_POLICY:
                setIcon(PmNodeIcons.getIconForNodeType(pmNode.getNodeType()));
                setToolTipText("This is a synchronized policy class");
                break;
            case SYNC_OBJECT:
                if (pmNode.isAccessible || ignoreIsAccessible) {
                    setIcon(PmNodeIcons.getIconForNodeType(pmNode.getNodeType()));
                    setToolTipText("This is a synchronized object");
                } /*else {
                    setIcon(PmNodeIcons.getIconForNodeType(PmNodeType.OBJECT_DISABLED));
                    setToolTipText("This object is not accessible");
                }*/
                break;
            case SYNC_OATTR:
                if (pmNode.isAccessible || ignoreIsAccessible) {
                    setIcon(PmNodeIcons.getIconForNodeType(pmNode.getNodeType()));
                    setToolTipText("This is a synchronized object attribute");
                }/* else {
                    setIcon(PmNodeIcons.getIconForNodeType(PmNodeType.OBJECT_ATTRIBUTE_DISABLED));
                    setToolTipText("This object attribute is not accessible");
                }*/
                break;
            default:
                //Do nothing
                break;
        }
        return this;
    }
}

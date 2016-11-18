package gov.nist.csd.pm.user;

import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeIcons;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.util.collect.Arrays;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 9/7/11
 * Time: 2:27 PM
 * To change this template use File | Settings | File Templates.
 */
public final class DesktopShortcuts {
    public static Shortcut createPmGraphShortcut(final String name, final PmGraph graph, final PmNode[] selectionPath){

        final PmNode tailNode =
                Arrays.isNullOrEmpty(selectionPath) ?
                        null :
                        selectionPath[selectionPath.length - 1];
        final PmNodeType type = tailNode == null ? PmNodeType.OBJECT : tailNode.getNodeType();
        return new Shortcut() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Icon getVisualRep() {
                return PmNodeIcons.getIconForNodeType(type);
            }

            @Override
            public Action getAction() {
                return new AbstractAction(){

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        if(graph != null && selectionPath != null){
                            graph.setSelectionPath(new TreePath(selectionPath));
                        }
                    }
                };
            }
        };
    }
}

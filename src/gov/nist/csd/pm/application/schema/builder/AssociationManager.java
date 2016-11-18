package gov.nist.csd.pm.application.schema.builder;

import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.browser.PmGraphDirection;
import gov.nist.csd.pm.common.browser.PmGraphType;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeChildDelegate;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.net.Packet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

public class AssociationManager extends JPanel implements ActionListener{

	private SchemaBuilder3 schemabuilder;
	private Utilities util;
	private SysCaller sysCaller;
	private PmNode selectedNode;
	private JPopupMenu pmPopup;
	public PmNode rightClickedNode;
	private PmGraph tree;
	private JTabbedPane content;
	private PermissionManager permMgr;
	private PmNode markedNode;
	private String schemaName;

	public AssociationManager(Utilities u, SchemaBuilder3 sb, SysCaller sys){
		util = u;
		schemabuilder = sb;
		sysCaller = sys;
		createGUI();
	}

	private void createGUI(){
		JPanel manager = new JPanel();
		manager.setLayout(new BorderLayout(5, 5));
		JPanel treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout(5, 5));
		treePanel.setPreferredSize(new Dimension(200, 300));
		createPopUp();

		schemaName = schemabuilder.getSchemaName();
		PmNode root = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schemabuilder.getSessionId(), PmGraphDirection.UP, PmGraphType.ACCESS_CONTROL_ENTRIES));
		tree = new PmGraph(root, false);
		tree.addMouseListener(new PmMouseListener());
		final JScrollPane treeScroll = new JScrollPane();
		treePanel.add(treeScroll, BorderLayout.CENTER);
		treeScroll.setViewportView(tree);

		JButton refresh = new JButton("Refresh");
		refresh.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				PmNode rRoot = new PmNode(
						PM_NODE.POL.value, 
						sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
						schemaName, 
						new PmNodeChildDelegate(util.sslClient, schemabuilder.getSessionId(), PmGraphDirection.UP, PmGraphType.ACCESS_CONTROL_ENTRIES));
				tree = new PmGraph(rRoot, false);
				tree.addMouseListener(new PmMouseListener());
				//rTree.setShowsRootHandles(true);
				/*tree.addMouseListener(new MouseListener(){

					@Override
					public void mousePressed(MouseEvent e) {
						PmNode selNode = (PmNode)tree.getLastSelectedPathComponent();
						selectedNode = selNode;
						System.out.println("right: " + selectedNode);
					}

					@Override
					public void mouseClicked(MouseEvent e) {}

					@Override
					public void mouseReleased(MouseEvent e) {}

					@Override
					public void mouseEntered(MouseEvent e) {}

					@Override
					public void mouseExited(MouseEvent e) {}

				});*/
				treeScroll.setViewportView(tree);
			}
		});
		treePanel.add(refresh, BorderLayout.SOUTH);

		manager.add(treePanel, BorderLayout.WEST);

		content = new JTabbedPane();
		PermissionEditor permEditor = new PermissionEditor(schemabuilder, util, sysCaller);
		permEditor.prepare();
		content.add("Create Association", permEditor);

		permMgr = new PermissionManager(schemabuilder, util, sysCaller);
		//permMgr.prepareAndSetBaseNode(null, selectedNode.getId());
		content.add("Manage Association", permMgr);

		manager.add(content, BorderLayout.CENTER);

		add(manager);

		setPreferredSize(new Dimension(850, 600));
	}

	private void createPopUp(){
		pmPopup = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Info");
		menuItem.addActionListener(this);
		pmPopup.add(menuItem);

		pmPopup.addSeparator();

		menuItem = new JMenuItem("Mark node");
		menuItem.addActionListener(this);
		pmPopup.add(menuItem);
		menuItem = new JMenuItem("Delete assignment");
		menuItem.addActionListener(this);
		pmPopup.add(menuItem);

		pmPopup.addSeparator();

		menuItem = new JMenuItem("Delete node");
		menuItem.addActionListener(this);
		pmPopup.add(menuItem);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String sCommand = null;
		if (source instanceof JButton) {
			sCommand = ((JButton) source).getActionCommand();
		} else {
			sCommand = ((JMenuItem) source).getText();
		}

		if (sCommand.equals("Mark node")) {
			doMarkNode();
		} else if (sCommand.equals("Delete assignment")) {
			doDeleteAssignment();
		} else if (sCommand.equals("Delete node")) {
			doDeleteNode();
		} else if (sCommand.equals("Info")) {
			doInfo();
		}
	}

	private void doInfo() {
		content.setSelectedIndex(1);
		if(rightClickedNode.getType().equals(PM_NODE.OPSET.value)){
			permMgr.prepareAndSetBaseNode(null, rightClickedNode.getId());
		}
	}

	private void doDeleteNode() {
		Packet res = util.genCmd("deleteNode", rightClickedNode.getId(), rightClickedNode.getType(), "no");
		if(res == null){
			JOptionPane.showMessageDialog(this, "Could not delete node");
			return;
		}
		
		if(res.hasError()){
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}
		// We should redraw the graph. If the deleted node was currently selected,
		// we should redraw the tree starting with another node.
		if(rightClickedNode.getParent() != null){
			List<PmNode> children = rightClickedNode.getParent().getChildren();
			children.remove(rightClickedNode);
		}
	}

	private void doDeleteAssignment() {
		if (markedNode == null) {
			JOptionPane.showMessageDialog(this, "Please mark a node for de-assignment");
			return;
		}

		System.out.println("You asked to delete the assignment of node " + markedNode.getId() + ":"
				+ markedNode.getName() + ":" + markedNode.getType() + " to node "
				+ rightClickedNode.getId() + ":" + rightClickedNode.getName() + ":" + rightClickedNode.getType());

		Packet res = util.genCmd("deleteAssignment", null, markedNode.getId(), markedNode.getType(),
				rightClickedNode.getId(), rightClickedNode.getType(), "no");
		if(res.hasError()){
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}
		// We should redraw the graph.
		rightClickedNode.invalidate();

	}

	private void doMarkNode() {
		markedNode = rightClickedNode;
	}


	class PmMouseListener extends MouseAdapter {
		@Override
		public void mouseReleased(MouseEvent e){
			if(e.isPopupTrigger()){
				showPopup(e);
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if(e.isPopupTrigger()){
				showPopup(e);
			}
		}
		public void showPopup(MouseEvent e){
			int selRow = tree.getRowForLocation(e.getX(), e.getY());
			TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

			if (selRow != -1) {
				PmNode node = (PmNode) selPath.getLastPathComponent();
				rightClickedNode = node;
				pmPopup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
}

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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListModel;

import java.awt.BorderLayout;

public class PermissionEditor extends JPanel implements ActionListener, ListSelectionListener{

	private GridBagConstraints constraints = new GridBagConstraints();
	private String sEntityName;
	private String sEntityType;
	private DefaultListModel uattrListModel;
	private JList uattrList;
	private Vector uattrVector;
	private DefaultListModel opsetListModel;
	private DefaultListModel oattrListModel;
	private JLabel allPermLabel;
	private DefaultListModel allPermListModel;
	private JList allPermList;
	private JLabel selPermLabel;
	private DefaultListModel selPermListModel;
	private JList selPermList;
	private JCheckBox subgraphBox;
	private JButton setButton;
	private JButton resetButton;
	private JLabel uattrLabel;
	private String columnName;
	private String selectedUattr;
	private String selectedPerms;
	private String base;
	private SchemaBuilder3 schemabuilder;
	private Utilities util;
	private JScrollPane lTreeScroll;
	private PmGraph lTree;
	private JScrollPane rTreeScroll;
	private PmGraph rTree;
	private PmNode lRoot;
	protected PmNode lSelectedNode;
	protected PmNode rSelectedNode;
	private SysCaller sysCaller;
	private String schemaName;

	public PermissionEditor(SchemaBuilder3 sb, Utilities u, SysCaller s){
		util = u;
		schemabuilder = sb;
		sysCaller = s;
		createGUI();
	}

	private void createGUI(){
		//setPreferredSize(new Dimension(850, 570));
		setLayout(new BorderLayout(5, 5));		
		JPanel treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout(5, 5));
		JPanel treesPanel = new JPanel();
		treesPanel.setLayout(new GridLayout(1, 2));

		JPanel lTreePanel = new JPanel();
		lTreePanel.setPreferredSize(new Dimension(100, 200));
		lTreePanel.setLayout(new BorderLayout(5, 5));
		lTreePanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Select Base"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		lTreeScroll = new JScrollPane();
		lTreePanel.add(lTreeScroll, BorderLayout.CENTER);
		treesPanel.add(lTreePanel);
		schemaName = schemabuilder.getSchemaName();
		PmNode lRoot = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schemabuilder.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));

		lTree = new PmGraph(lRoot, false);
		//lTreePanel.add(lTree, BorderLayout.CENTER);
		lTree.setShowsRootHandles(true);
		lTree.addMouseListener(new MouseListener(){

			@Override
			public void mousePressed(MouseEvent e) {
				PmNode selNode = (PmNode)lTree.getLastSelectedPathComponent();
				lSelectedNode = selNode;
				System.out.println(lSelectedNode);
			}

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

		});
		lTreeScroll.setViewportView(lTree);

		JPanel rTreePanel = new JPanel();
		rTreePanel.setPreferredSize(new Dimension(100, 200));
		rTreePanel.setLayout(new BorderLayout(5, 5));
		rTreePanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Select Object Attribute"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		rTreeScroll = new JScrollPane();
		rTreePanel.add(rTreeScroll, BorderLayout.CENTER);
		treesPanel.add(rTreePanel);
		PmNode rRoot = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schemabuilder.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));


		//rTreePanel.add(new JScrollPane(rTree), BorderLayout.EAST);
		//treesPanel.add(rTreePanel);
		rTree = new PmGraph(rRoot, false);
		//rTreePanel.add(rTree, BorderLayout.CENTER);
		rTree.setShowsRootHandles(true);
		rTree.addMouseListener(new MouseListener(){

			@Override
			public void mousePressed(MouseEvent e) {
				PmNode selNode = (PmNode)rTree.getLastSelectedPathComponent();
				rSelectedNode = selNode;
				System.out.println(rSelectedNode);
			}

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

		});
		rTreeScroll.setViewportView(rTree);

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshTrees();
			}

		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(refreshButton);

		//assignUattrPanel.add(lTreePanel, BorderLayout.WEST);
		treePanel.add(treesPanel, BorderLayout.NORTH);
		treePanel.add(buttonPanel, BorderLayout.CENTER);

		JTabbedPane contentPane = new JTabbedPane();
		JPanel createAssocPanel = new JPanel();
		createAssocPanel.setLayout(new BorderLayout(5, 5));
		createAssocPanel.add(treePanel, BorderLayout.NORTH);
		createAssocPanel.add(new PermissionPanel(), BorderLayout.CENTER);
		//contentPane.add("Create an Association", createAssocPanel);
		//contentPane.add("Manage Associations", buildAssocManager());
		add(createAssocPanel);
	}

	private void refreshTrees(){
		System.out.println("refreshing");
		PmNode rRoot = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schemabuilder.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
		rTree = new PmGraph(rRoot, false);
		//rTree.setShowsRootHandles(true);
		rTree.addMouseListener(new MouseListener(){

			@Override
			public void mousePressed(MouseEvent e) {
				PmNode selNode = (PmNode)rTree.getLastSelectedPathComponent();
				rSelectedNode = selNode;
				System.out.println("right: " + rSelectedNode);
			}

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

		});
		rTreeScroll.setViewportView(rTree);

		lRoot = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schemabuilder.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
		lTree = new PmGraph(lRoot, false);
		//lTree.setShowsRootHandles(true);
		lTree.addMouseListener(new MouseListener(){

			@Override
			public void mousePressed(MouseEvent e) {
				PmNode selNode = (PmNode)lTree.getLastSelectedPathComponent();
				lSelectedNode = selNode;
				System.out.println("left: " + lSelectedNode);
			}

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

		});
		lTreeScroll.setViewportView(lTree);
	}

	private String getEntityTypeName(String sType) {
		if (sType.equalsIgnoreCase(PM_NODE.USER.value)) return "user";
		else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) return "user attribute";
		else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) return "object attribute";
		else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) return "object";
		else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) return "policy";
		else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) return "connector";
		else return "unknown";
	}

	public void prepare(){//String sName, String sType) {
		// Save the entity on which we want to set permissions.
		//sEntityName = sName;
		//sEntityId = sId;
		//sEntityType = sType;

		// Set the title right.
		//String sEntityTypeName = getEntityTypeName(sEntityType);

		// Clear lists and vectors.
		uattrListModel.clear();
		if (uattrVector == null) uattrVector = new Vector();
		else uattrVector.clear();
		selPermListModel.clear();
		allPermListModel.clear();

		// Fill the user attribute list.
		Packet packet = util.getUserAttributes();
		if (packet != null) for (int i = 0; i < packet.size(); i++) {
			String sLine = packet.getStringValue(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			int index = getIndex(uattrListModel, pieces[0]);
			uattrListModel.add(index, pieces[0]);
			uattrVector.add(index, pieces[1]);
		}

		// Fill the All Permissions list and vector.
		packet = util.getAllOps();
		if (packet != null) for (int i = 0; i < packet.size(); i++) {
			String sLine = packet.getStringValue(i);
			if (allPermListModel.contains(sLine)) continue;
			int index = getIndex(allPermListModel, sLine);
			allPermListModel.add(index, sLine);
		}
	}

	public static int getIndex(JComboBox combo, String target) {
		return getIndex(combo.getModel(), target);
	}

	public static int getIndex(List<String> v, String target) {
		int high = v.size(), low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (target.compareToIgnoreCase(v.get(probe)) < 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		return (low + 1);
	}

	public static int getIndex(ListModel model, String target) {
		int high = model.getSize(), low = -1, probe;
		while (high - low > 1) {
			probe = (high + low) / 2;
			if (target.compareToIgnoreCase((String) model.getElementAt(probe)) < 0) {
				high = probe;
			} else {
				low = probe;
			}
		}
		return (low + 1);
	}

	private void doClose() {
		this.setVisible(false);
		close = true;
	}

	private boolean close;

	public boolean close(){
		return close;
	}

	public void setRow(int r){
	}

	private void doReset() {
		uattrList.clearSelection();
		opsetListModel.clear();
		oattrListModel.clear();
		selPermListModel.clear();
	}

	public void setColumnName(String name){
		columnName = name;
	}

	private void doSet() {
		String sUattr = (String)uattrList.getSelectedValue();
		if(sUattr == null || sUattr.length() == 0){
			JOptionPane.showMessageDialog(this, "You must select a User Attribute from the list.");
			return;
		}
		selectedUattr = sUattr;
		//base = appBuilder.getTableName();
		base = lSelectedNode.getName();
		columnName = rSelectedNode.getName();
		if(base == null || base.length() == 0 || columnName == null || columnName.length() == 0){
			JOptionPane.showMessageDialog(this, "Select a base and an object attribute");
		}
		System.out.println("BASE: " + base);

		System.out.println("Setting permissions for uattr: " + sUattr);
		System.out.println("on entity " + sEntityName + " of type " + sEntityType);

		//String sOattr = (String)oattrList.getSelectedValue();
		//System.out.println("Real oattr: " + sOattr);

		System.out.println("Grant " + sUattr + " permissions: {");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < selPermListModel.size(); i++) {
			if (i == 0) {
				sb.append((String)selPermListModel.get(i));
				System.out.print((String)selPermListModel.get(i));    
			} else {
				sb.append("," + (String)selPermListModel.get(i));
				System.out.print("," + (String)selPermListModel.get(i));
			}
		}
		System.out.println("}");

		selectedPerms = sb.toString();

		if(selectedPerms == null || selectedPerms.length() == 0){
			JOptionPane.showMessageDialog(this, "You must select permissions.");
			return;
		}

		ArrayList<String> line = new ArrayList<String>();
		line.add(base);
		line.add(columnName);
		line.add(selectedUattr);
		line.add(selectedPerms);
		System.out.println("LINE: " + line);
		if(line.size() == 0 || line == null){
			JOptionPane.showMessageDialog(this, "error in doSet ColPermEditor");
			return;
		}
		util.setBasePermissions(columnName, lSelectedNode.getType(), columnName, rSelectedNode.getType(), selectedPerms, selectedUattr, subgraphBox.isSelected() ? "yes" : "no");
		//schemabuilder.addToPermissions(line);
		this.setVisible(false);
		JOptionPane.showMessageDialog(this, "Permissions set for Object Attribute " + columnName, "Success", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("set")) {
			doSet();
		} else if (e.getActionCommand().equalsIgnoreCase("reset")) {
			doReset();
		}

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;

		Object src = e.getSource();
		if (src == allPermList) {
			String sPerm = (String)allPermList.getSelectedValue();
			if (sPerm != null) {
				if (selPermListModel.contains(sPerm)) {
					JOptionPane.showMessageDialog(this, "Operation is already selected!");
					return;
				}
				int index = getIndex(selPermListModel, sPerm);
				selPermListModel.add(index, sPerm);
			}
			return;
		} else if (src == selPermList) {
			String sPerm = (String)selPermList.getSelectedValue();
			if (sPerm != null) {
				selPermListModel.removeElement(sPerm);
			}
			return;
		} else if (src == uattrList) {
			String sUattrName = (String)uattrList.getSelectedValue();
			if (sUattrName == null) return;

			// Clear the opset and oattr lists.
			//opsetListModel.clear();
			//oattrListModel.clear();

			// List all operation sets between the selected user attribute and the
			// object attribute <sEntityName, sEntityId, sEntityType>.
			/*Packet opsets = getOpsetsBetween(sUattrName, sEntityName,
					sEntityType);
			if (opsets != null) for (int i = 0; i < opsets.size(); i++) {
				String sLine = opsets.getStringValue(i);
				String[] pieces = sLine.split(PM_FIELD_DELIM);
				int index = getIndex(opsetListModel, pieces[0]);
				opsetListModel.add(index, pieces[0]);
			}*/
		} 
	}

	class PermissionPanel extends JPanel{
		private JButton refreshButton;

		public PermissionPanel(){
			createGUI();
		}

		private void createGUI(){
			// Start building the GUI
			// First the user attributes list and panel
			//setPreferredSize(new Dimension(550, 570));
			System.out.println("creating permission panel");
			setBorder(
					new CompoundBorder(
							new TitledBorder("Permission Editor"),
							BorderFactory.createBevelBorder(BevelBorder.LOWERED)
							)
					);

			uattrLabel = new JLabel("User attributes:");
			uattrListModel = new DefaultListModel();
			uattrList = new JList(uattrListModel);
			uattrList.addListSelectionListener(PermissionEditor.this);
			JScrollPane uattrListScrollPane = new JScrollPane(uattrList);
			uattrListScrollPane.setPreferredSize(new Dimension(200, 200));

			JPanel uattrPane = new JPanel();
			uattrPane.setLayout(new GridBagLayout());
			constraints.insets = new Insets(0, 0, 0, 0);
			addComp(uattrPane, uattrLabel, 0, 0, 1, 1);
			addComp(uattrPane, uattrListScrollPane, 0, 1, 3, 1);
			uattrPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			// The selected permissions list and panel.
			selPermLabel = new JLabel("Selected permissions:");
			selPermListModel = new DefaultListModel();
			selPermList = new JList(selPermListModel);
			selPermList.addListSelectionListener(PermissionEditor.this);
			JScrollPane selPermListScrollPane = new JScrollPane(selPermList);
			selPermListScrollPane.setPreferredSize(new Dimension(200, 200));

			JPanel selPermPane = new JPanel();
			selPermPane.setLayout(new GridBagLayout());
			constraints.insets = new Insets(0, 0, 0, 0);
			addComp(selPermPane, selPermLabel, 0, 0, 1, 1);
			addComp(selPermPane, selPermListScrollPane, 0, 1, 3, 1);
			selPermPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			// The all permissions list and panel.
			allPermLabel = new JLabel("All permissions:");
			allPermListModel = new DefaultListModel();
			allPermList = new JList(allPermListModel);
			allPermList.addListSelectionListener(PermissionEditor.this);
			JScrollPane allPermListScrollPane = new JScrollPane(allPermList);
			allPermListScrollPane.setPreferredSize(new Dimension(200, 200));

			JPanel allPermPane = new JPanel();
			allPermPane.setLayout(new GridBagLayout());
			constraints.insets = new Insets(0, 0, 0, 0);
			addComp(allPermPane, allPermLabel, 0, 0, 1, 1);
			addComp(allPermPane, allPermListScrollPane, 0, 1, 3, 1);
			allPermPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			JPanel listPane = new JPanel();
			listPane.setLayout(new GridLayout(1, 4));
			listPane.add(uattrPane);
			listPane.add(selPermPane);
			listPane.add(allPermPane);

			// Buttons.
			subgraphBox = new JCheckBox("All Ascendants?");

			setButton = new JButton("Set");
			setButton.addActionListener(PermissionEditor.this);
			setButton.setActionCommand("set");

			resetButton = new JButton("Reset");
			resetButton.addActionListener(PermissionEditor.this);
			resetButton.setActionCommand("reset");
			
			refreshButton = new JButton("Refresh");
			refreshButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					prepare();
				}
				
			});

			JPanel buttonPane = new JPanel();
			buttonPane.add(subgraphBox);
			buttonPane.add(setButton);
			buttonPane.add(resetButton);
			buttonPane.add(refreshButton);
			buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			// The content pane.
			JPanel contentPane = new JPanel();
			contentPane.setLayout(new BorderLayout());
			contentPane.add(listPane, BorderLayout.CENTER);
			contentPane.add(buttonPane, BorderLayout.SOUTH);

			add(contentPane);
		}

		private void addComp(Container container, Component component, int x, int y, int w, int h) {
			constraints.gridx = x;
			constraints.gridy = y;
			constraints.gridwidth = w;
			constraints.gridheight = h;
			container.add(component, constraints);
		}
	}
}

package gov.nist.csd.pm.application.schema.builder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.browser.PmGraphDirection;
import gov.nist.csd.pm.common.browser.PmGraphType;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeChildDelegate;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.JTabbedPane;

public class OattrEditor extends JPanel{

	private Utilities util;

	private SysCaller sysCaller;

	private JPanel basesPanel;

	private DefaultListModel basesListModel;

	private JList basesList;
	
	private PmGraph basesTree;

	private JPanel addOAPanel;

	private JPanel formPanel;

	private JTextField nameField;

	private JTextField propField;

	private JTextField descrField;

	private JTextField otherField;

	private JPanel addOAButtonPanel;

	private JButton setPermissionsButton;

	private JButton createOattrButton;

	private SchemaBuilder3 schema;

	private JButton refreshButton;
	
	private String schemaName;
	
	private PmNode baseNode;
	
	private JTabbedPane contentPane;
	
	private JPanel addOattrPanel;

	private JPanel assignUattrPanel;

	private PmNode lRoot;

	private PmGraph lTree;

	private JScrollPane lTreeScroll;

	private PmNode rRoot;

	private PmGraph rTree;

	private JScrollPane rTreeScroll;

	protected PmNode rSelectedNode;

	protected PmNode lSelectedNode;

	private JScrollPane basesScroll;

	private JButton clearButton;

	public OattrEditor(SchemaBuilder3 sb, Utilities u, SysCaller s){
		util = u;
		sysCaller = s;
		schema = sb;
		createGUI();
	}

	private void createGUI(){
		setLayout(new BorderLayout(5, 5));
		contentPane = new JTabbedPane();
		contentPane.setPreferredSize(new Dimension(550, 400));
		
		addOattrPanel = new JPanel();
		addOattrPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Add Oattr"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);

		addOattrPanel.setLayout(new BorderLayout(5, 5));
		
		schemaName = schema.getSchemaName();

		basesPanel = new JPanel();
		basesPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Bases"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);

		basesListModel = new DefaultListModel();
		basesPanel.setLayout(new BorderLayout(0, 0));

		basesScroll = new JScrollPane();
		basesPanel.add(basesScroll);
		basesList = new JList(basesListModel);
		//basesScroll.setViewportView(basesList);
		PmNode root = new PmNode(
				PM_NODE.POL.value, 
				/*GlobalConstants.PM_CONNECTOR_ID,*/sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				/*GlobalConstants.PM_CONNECTOR_NAME,*/schemaName, 
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
		basesTree = new PmGraph(root, false);
		basesTree.addMouseListener(new MouseListener(){

			@Override
			public void mousePressed(MouseEvent e) {
				PmNode selNode = (PmNode)basesTree.getLastSelectedPathComponent();
				baseNode = selNode; 
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
		basesScroll.setViewportView(basesTree);
		basesScroll.setPreferredSize(new Dimension(200, 150));
		basesList.setListData(retrieveBases().toArray());
		
		addOattrPanel.add(basesPanel, BorderLayout.CENTER);

		addOAPanel = new JPanel();
		addOAPanel.setLayout(new BorderLayout(5, 5));
		addOAPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Add Oattr"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		addOattrPanel.add(addOAPanel, BorderLayout.SOUTH);

		addOAButtonPanel = new JPanel();
		addOAButtonPanel.setLayout(new FlowLayout(5, 5, 5));//new GridLayout(2, 1));

		setPermissionsButton = new JButton("Set Permissions");
		setPermissionsButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				String name = nameField.getText();
				if(name == null || name.length() == 0){
					JOptionPane.showMessageDialog(OattrEditor.this, "You must specify the Object attribute name first before setting the permissions");
					return;
				}
				
				ColumnPermEditor2 colPermEditor = new ColumnPermEditor2(util, schema);
				colPermEditor.pack();
				colPermEditor.prepare(name, PM_NODE.OATTR.value);
				colPermEditor.setColumnName(name);
				colPermEditor.setVisible(true);
			}
			
		});
		//addOAButtonPanel.add(setPermissionsButton);

		createOattrButton = new JButton("Create Oattr");
		createOattrButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				createOattr();
			}
		});
		addOAButtonPanel.add(createOattrButton);
		
		clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				nameField.setText("");
				descrField.setText("");
				propField.setText("");
				otherField.setText("");
			}
		});
		addOAButtonPanel.add(clearButton);
		
		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				basesList.setListData(retrieveBases().toArray());
				PmNode root = new PmNode(
						PM_NODE.POL.value, 
						sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
						schemaName, 
						new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
				basesTree = new PmGraph(root, false);
				basesTree.addMouseListener(new MouseListener(){

					@Override
					public void mousePressed(MouseEvent e) {
						PmNode selNode = (PmNode)basesTree.getLastSelectedPathComponent();
						baseNode = selNode; 
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
				basesScroll.setViewportView(basesTree);
			}

		});
		addOAButtonPanel.add(refreshButton, BorderLayout.EAST);

		addOAPanel.add(addOAButtonPanel, BorderLayout.SOUTH);

		formPanel = new JPanel();
		formPanel.setLayout(new GridLayout(4, 2));
		formPanel.add(new JLabel("Name: "));
		nameField = new JTextField(10);
		formPanel.add(new JLabel("Properties: "));
		formPanel.add(nameField);
		propField = new JTextField();
		formPanel.add(propField);
		formPanel.add(new JLabel("Descr: "));
		formPanel.add(new JLabel("Other: "));
		descrField = new JTextField();
		formPanel.add(descrField);
		otherField = new JTextField();
		formPanel.add(otherField);

		addOAPanel.add(formPanel, BorderLayout.CENTER);
		
		contentPane.add(addOattrPanel, "Oattr Editor");
		
		assignUattrPanel = new JPanel();
		assignUattrPanel.setLayout(new BorderLayout(5, 5));
		
		JPanel treesPanel = new JPanel();
		treesPanel.setLayout(new GridLayout(1, 2));
		
		JPanel lTreePanel = new JPanel();
		lTreePanel.setLayout(new BorderLayout(5, 5));
		lTreePanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Select Node to Assign"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		lTreeScroll = new JScrollPane();
		lTreePanel.add(lTreeScroll, BorderLayout.CENTER);
		treesPanel.add(lTreePanel);
		lRoot = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
		
		lTree = new PmGraph(lRoot, false);;
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
		rTreePanel.setLayout(new BorderLayout(5, 5));
		rTreePanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Select Assignment Destination"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		rTreeScroll = new JScrollPane();
		rTreePanel.add(rTreeScroll, BorderLayout.CENTER);
		treesPanel.add(rTreePanel);
		rRoot = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
		
		//rTreePanel.add(new JScrollPane(rTree), BorderLayout.EAST);
		//treesPanel.add(rTreePanel);
		rTree = new PmGraph(rRoot, false);;
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

		JButton assignButton = new JButton("Assign");
		assignButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				assign();
			}

		});
		
		JButton unassignButton = new JButton("Unassign");
		unassignButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				unassign();
			}

		});
		
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshTrees();
			}

		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(assignButton);
		buttonPanel.add(unassignButton);
		buttonPanel.add(refreshButton);
		
		//assignUattrPanel.add(lTreePanel, BorderLayout.WEST);
		assignUattrPanel.add(treesPanel, BorderLayout.CENTER);
		assignUattrPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		contentPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int sel = contentPane.getSelectedIndex();
				if(sel == 0){
					Dimension d = new Dimension(addOattrPanel.getPreferredSize());
					d.width = d.width + 200;
					d.height = d.height + 50;
					OattrEditor.this.setSize(d);
				}else if(sel == 1){
					Dimension d = new Dimension(assignUattrPanel.getPreferredSize());
					d.width = d.width + 145;
					d.height = d.height + 50;
					OattrEditor.this.setSize(d);
				}
			}
		});

		contentPane.add(assignUattrPanel, "Assign Oattr");
		
		add(contentPane, BorderLayout.CENTER);
	}

	private void unassign() {
		String selId = lSelectedNode.getId();
		String baseId = rSelectedNode.getId();
		
		Packet res = util.genCmd("deleteAssignment", null, selId, lSelectedNode.getType(),
				baseId, rSelectedNode.getType(), "no");
		if(res.hasError()){
			JOptionPane.showMessageDialog(this, res.getErrorMessage());
			return;
		}
	}

	protected void refreshTrees() {
		rRoot = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
		rTree = new PmGraph(rRoot, false);;
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
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
		lTree = new PmGraph(lRoot, false);;
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

	protected void assign() {
		String sel = lSelectedNode.getName();
		String base = rSelectedNode.getName();
		String selId = lSelectedNode.getId();
		String baseId = rSelectedNode.getId();
		
		System.out.println("assigning " + sel + ":" + selId + " to " + base + ":" + baseId);
		
		if(lSelectedNode.getNodeType().equals(PmNodeType.OBJECT_ATTRIBUTE) &&
				rSelectedNode.getNodeType().equals(PmNodeType.OBJECT_ATTRIBUTE)){
			if(!util.assign(selId, PM_NODE.OATTR.value, baseId, PM_NODE.OATTR.value)){
				JOptionPane.showMessageDialog(this, "Could not assign object attribute to object attribute", 
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}else if(lSelectedNode.getNodeType().equals(PmNodeType.OBJECT_ATTRIBUTE) &&
				rSelectedNode.getNodeType().equals(PmNodeType.POLICY)){
			if(!util.assign(selId, PM_NODE.OATTR.value, baseId, PM_NODE.POL.value)){
				JOptionPane.showMessageDialog(this, "Could not assign object attribute to policy", 
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}

	private ArrayList<String> retrieveBases(){
		ArrayList<String> bases = util.getOattrs();
		System.out.println("BASES: " + bases);
		ArrayList<String> ret = new ArrayList<String>();
		for(int i = 0; i < bases.size(); i++){
			ret.add(bases.get(i).split(":")[0]);
		}
		Collections.sort(ret);
		return ret;
	}

	private void createOattr() {
		/*String base = (String) basesList.getSelectedValue();
		if(base == null || base.length() == 0){
			JOptionPane.showMessageDialog(this, "Please select a base");
			return;
		}*/
		if(baseNode == null){
			JOptionPane.showMessageDialog(this, "Please select a base");
			return;
		}
		String base = baseNode.getName();

		String name = nameField.getText();
		if(name == null || name.length() == 0){
			JOptionPane.showMessageDialog(this, "Please fill out Oattr Name Field");
			return;
		}

		String prop = propField.getText();
		if(prop == null || prop.length() == 0){
			JOptionPane.showMessageDialog(this, "Please fill out properties Field");
			return;
		}

		String descr = descrField.getText();
		if(descr == null || descr.length() == 0){
			descr = name;
			descrField.setText(descr);
		}

		String other = otherField.getText();
		if(other == null || other.length() == 0){
			other = name;
			otherField.setText(other);
		}

		boolean addOattr = util.addOattr(name, sysCaller.getIdOfEntityWithNameAndType(base, baseNode.getType()), baseNode.getType(), descr, other, prop);
		if(!addOattr){
			JOptionPane.showMessageDialog(this, "Object Attribute " + name + " could not be created");
			return;
		}
		JOptionPane.showMessageDialog(this, name + " was created!", "Success", JOptionPane.INFORMATION_MESSAGE);
	}
}

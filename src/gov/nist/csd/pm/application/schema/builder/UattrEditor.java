package gov.nist.csd.pm.application.schema.builder;

import static com.google.common.base.Strings.nullToEmpty;

import java.awt.BorderLayout;
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

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

public class UattrEditor extends JPanel{

	private SchemaBuilder3 schema;

	private Utilities util;

	private SysCaller sysCaller;

	private JTextField uaNameField;

	private JTextField uaDescrField;

	private JTextField uaInfoField;

	private DefaultListModel propListModel;

	private JList propList;

	private JButton addButton;

	private JButton editButton;

	private JButton removeButton;

	private JButton okButton;

	private GridBagConstraints constraints = new GridBagConstraints();

	private PmGraph tree;

	private PmNode root;

	private PmNode baseNode;

	private String schemaName;

	private PmNode lRoot;

	private PmNode rRoot;

	private PmGraph lTree;

	private PmGraph rTree;

	private PmNode lSelectedNode;

	private PmNode rSelectedNode;

	private JPanel addUattrPanel;

	private JPanel assignUattrPanel;

	private JTabbedPane contentPane;

	private JScrollPane rTreeScroll;

	private JScrollPane lTreeScroll;

	private JScrollPane treeScroll;

	public UattrEditor(SchemaBuilder3 s, Utilities u, SysCaller sys){
		schema = s;
		util = u;
		sysCaller = sys;

		createGUI();
	}

	private void createGUI(){
		contentPane = new JTabbedPane();

		addUattrPanel = new JPanel();
		addUattrPanel.setLayout(new BorderLayout(5, 5));

		JPanel treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout(5, 5));
		treePanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Uattr Tree"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		schemaName = schema.getSchemaName();
		root = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.USER_ATTRIBUTES));
		tree = new PmGraph(root, false);
		tree.setShowsRootHandles(true);
		tree.addMouseListener(new MouseListener(){

			@Override
			public void mousePressed(MouseEvent e) {
				PmNode selNode = (PmNode)tree.getLastSelectedPathComponent();
				System.out.println(selNode);
				baseNode = selNode;
				System.out.println("BASENODE:: ");
				System.out.println(baseNode.getName());
				System.out.println(baseNode.getId());
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
		treeScroll = new JScrollPane();
		treeScroll.setViewportView(tree);
		treePanel.add(treeScroll, BorderLayout.CENTER);
		JButton refreshTree = new JButton("Refresh");
		refreshTree.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				root = new PmNode(
						PM_NODE.POL.value, 
						sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
						schemaName, 
						new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.USER_ATTRIBUTES));
				tree = new PmGraph(root, false);
				tree.setShowsRootHandles(true);
				tree.addMouseListener(new MouseListener(){

					@Override
					public void mousePressed(MouseEvent e) {
						PmNode selNode = (PmNode)tree.getLastSelectedPathComponent();
						baseNode = selNode;
						System.out.println("BASENODE:: ");
						System.out.println(baseNode.getName());
						System.out.println(baseNode.getId());
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
				treeScroll.setViewportView(tree);
			}
		});
		treePanel.add(refreshTree, BorderLayout.SOUTH);
		addUattrPanel.add(treePanel);

		JPanel addPanel = new JPanel();
		//
		JPanel uaPanel = new JPanel();
		uaPanel.setLayout(new GridBagLayout());

		JLabel uaNameLabel = new JLabel("Name:");
		JLabel uaDescrLabel = new JLabel("Description:");
		JLabel uaInfoLabel = new JLabel("Other Info:");

		uaNameField = new JTextField(23);
		uaDescrField = new JTextField(23);
		uaInfoField = new JTextField(23);


		addComp(uaPanel, uaNameLabel, 0, 0, 1, 1);
		addComp(uaPanel, uaDescrLabel, 0, 1, 1, 1);
		addComp(uaPanel, uaInfoLabel, 0, 2, 1, 1);

		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(uaPanel, uaNameField, 1, 0, 3, 1);
		addComp(uaPanel, uaDescrField, 1, 1, 3, 1);
		addComp(uaPanel, uaInfoField, 1, 2, 3, 1);
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// The property list pane
		propListModel = new DefaultListModel();
		propList = new JList(propListModel);
		JScrollPane propListScrollPane = new JScrollPane(propList);
		propListScrollPane.setPreferredSize(new Dimension(255, 200));
		propListScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		// The property button pane
		JPanel propButtonPane = new JPanel();

		addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				doAddProp();
			}

		});

		editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				doEditProp();
			}

		});

		removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				doRemoveProp();
			}

		});

		propButtonPane.add(addButton);
		propButtonPane.add(editButton);
		propButtonPane.add(removeButton);
		propButtonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

		// The property pane = prop list pane + prop button pane
		JPanel propPane = new JPanel();
		propPane.setLayout(new BorderLayout());
		propPane.add(propListScrollPane, BorderLayout.CENTER);
		propPane.add(propButtonPane, BorderLayout.SOUTH);
		propPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 0, 10, 0),
				BorderFactory.createTitledBorder("Properties")));

		// The button pane
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridBagLayout());

		okButton = new JButton("Add attribute");
		okButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				addUattr();
			}

		});

		constraints.insets = new Insets(0, 10, 0, 0);
		addComp(buttonPane, okButton, 1, 0, 1, 1);
		
		addPanel.setLayout(new BorderLayout());
		addPanel.add(uaPanel, BorderLayout.NORTH);
		addPanel.add(propPane, BorderLayout.CENTER);
		addPanel.add(buttonPane, BorderLayout.SOUTH);
		addPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Uattr Editor"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);

		addUattrPanel.add(addPanel, BorderLayout.EAST);

		contentPane.add(addUattrPanel, "Add Uattr");


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
		lRoot = new PmNode(
				PM_NODE.CONN.value, 
				GlobalConstants.PM_CONNECTOR_ID,//sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				GlobalConstants.PM_CONNECTOR_NAME,//schemaName, 
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.USER_ATTRIBUTES));
		lTreeScroll = new JScrollPane();
		lTreePanel.add(lTreeScroll, BorderLayout.CENTER);
		
		//lTreePanel.add(new JScrollPane(lTree), BorderLayout.WEST);
		treesPanel.add(lTreePanel);
		lTree = new PmGraph(lRoot, false);
		
		//lTreePanel.add(lTree, BorderLayout.CENTER);
		lTreeScroll.setViewportView(lTree);
		lTree.setShowsRootHandles(true);
		lTree.addMouseListener(new MouseListener(){

			@Override
			public void mousePressed(MouseEvent e) {
				PmNode selNode = (PmNode)lTree.getLastSelectedPathComponent();
				lSelectedNode = selNode;
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

		JPanel rTreePanel = new JPanel();
		rTreePanel.setLayout(new BorderLayout(5, 5));
		rTreePanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Select Assignment Destination"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		rRoot =new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.USER_ATTRIBUTES));
		rTreeScroll = new JScrollPane();
		rTreePanel.add(rTreeScroll, BorderLayout.CENTER);
		
		//rTreePanel.add(new JScrollPane(rTree), BorderLayout.EAST);
		treesPanel.add(rTreePanel);
		rTree = new PmGraph(rRoot, false);
		//rTreePanel.add(rTree, BorderLayout.CENTER);
		rTreeScroll.setViewportView(rTree);
		rTree.setShowsRootHandles(true);
		rTree.addMouseListener(new MouseListener(){

			@Override
			public void mousePressed(MouseEvent e) {
				PmNode selNode = (PmNode)rTree.getLastSelectedPathComponent();
				rSelectedNode = selNode;
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
					Dimension d = new Dimension(addUattrPanel.getPreferredSize());
					d.width = d.width + 200;
					d.height = d.height + 50;
					UattrEditor.this.setSize(d);
				}else if(sel == 1){
					Dimension d = new Dimension(assignUattrPanel.getPreferredSize());
					d.width = d.width + 145;
					d.height = d.height + 50;
					UattrEditor.this.setSize(d);
				}
			}
		});

		contentPane.add(assignUattrPanel, "Assign Uattr");
		add(contentPane);
		Dimension d = new Dimension(contentPane.getPreferredSize());
		d.width = d.width + 100;
		d.height = d.height + 25;
		setPreferredSize(d);
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

	private void refreshTrees(){
		System.out.println("refreshing");
		rRoot = new PmNode(
				PM_NODE.POL.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.USER_ATTRIBUTES));
		rTree = new PmGraph(rRoot, false);
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
				PM_NODE.CONN.value, 
				GlobalConstants.PM_CONNECTOR_ID,//sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.POL.value),
				GlobalConstants.PM_CONNECTOR_NAME,//schemaName, 
				new PmNodeChildDelegate(util.sslClient, schema.getSessionId(), PmGraphDirection.UP, PmGraphType.USER_ATTRIBUTES));
		lTree = new PmGraph(lRoot, false);
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

	private void assign(){
		String sel = lSelectedNode.getName();
		String base = rSelectedNode.getName();
		String selId = lSelectedNode.getId();
		String baseId = rSelectedNode.getId();
		
		System.out.println("assigning " + sel + ":" + selId + " to " + base + ":" + baseId);
		
		if(lSelectedNode.getNodeType().equals(PmNodeType.USER)){
			if(!util.assign(selId, PM_NODE.USER.value, baseId, PM_NODE.UATTR.value)){
				JOptionPane.showMessageDialog(this, "Could not assign user to user attribute", 
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}else if(lSelectedNode.getNodeType().equals(PmNodeType.USER_ATTRIBUTE) &&
				rSelectedNode.getNodeType().equals(PmNodeType.USER_ATTRIBUTE)){
			if(!util.assign(selId, PM_NODE.UATTR.value, baseId, PM_NODE.UATTR.value)){
				JOptionPane.showMessageDialog(this, "Could not assign user attribute to user attribute", 
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}else if(lSelectedNode.getNodeType().equals(PmNodeType.USER_ATTRIBUTE) &&
				rSelectedNode.getNodeType().equals(PmNodeType.POLICY)){
			if(!util.assign(selId, PM_NODE.UATTR.value, baseId, PM_NODE.POL.value)){
				JOptionPane.showMessageDialog(this, "Could not assign user attribute to policy", 
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		
		
		//assignUserToUattr in ADDAO if selected node in right is a user
		//assignUattrToUattr
		//assignUattrToPolicy
		
		
		

		/*boolean ret = sysCaller.assignObjToContainer(sel, base);
		if(!ret){
			JOptionPane.showMessageDialog(this, "Could not assign " + sel + " to " + base + ":\n"+ sysCaller.getLastError());
		}*/
		
		JOptionPane.showMessageDialog(this, "Successfully assigned " + sel + " to " + base);
	}

	private void addComp(Container container, Component component, int x, int y, int w, int h) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		container.add(component, constraints);
	}

	private void doAddProp() {
		JTextField propField = new JTextField();
		int ret = JOptionPane.showOptionDialog(this,
				new Object[] {"Please enter the property:", propField},
				"Add property", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (ret != JOptionPane.OK_OPTION) return;
		String sProp = propField.getText();
		if (sProp.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a property!");
			return;
		}
		/*if (editingMode) {
		      try {
		        Packet res = null;
		        Packet cmd = tool.makeCmd("addProp", uattrToEditId, PmAdmin.PM_NODE_UATTR,
		          "no", sProp);
		        res = sslClient.sendReceive(cmd, null);
		        if (res == null) {
		          JOptionPane.showMessageDialog(this, "Undetermined error, null result returned");
		          return;
		        }
		        if (res.hasError()) {
		          JOptionPane.showMessageDialog(this, res.getErrorMessage());
		          return;
		        }
		      } catch (Exception e) {
		        e.printStackTrace();
		        JOptionPane.showMessageDialog(this, e.getMessage());
		        return;
		      }
		    }*/

		propListModel.addElement(sProp);
	}

	private void addUattr() {
		String sName = uaNameField.getText().trim();
		String sDescr = uaDescrField.getText().trim();
		String sInfo = uaInfoField.getText().trim();
	
		if (sName.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter an attribute name!");
			return;
		}
		if (sDescr.length() == 0) sDescr = sName;
		if (sInfo.length() == 0) sInfo = sName;

		int n = propListModel.getSize();
		String[] props = new String[n];
		for (int i = 0; i < n; i++) {
			props[i] = (String)propListModel.get(i);
		}

		if(!util.addUattr(baseNode, sName, sDescr, sInfo, props)){
			JOptionPane.showMessageDialog(this, "Could not create new User Attribute");
			return;
		}

		uaNameField.setText(null);
		uaNameField.requestFocus();
		uaDescrField.setText(null);
		uaInfoField.setText(null);
		propListModel.clear();
	}

	private void doEditProp() {
		String sOldProp = (String)propList.getSelectedValue();
		if (sOldProp == null) {
			JOptionPane.showMessageDialog(this, "Please select a property!");
			return;
		}

		JTextField propField = new JTextField(sOldProp);
		int ret = JOptionPane.showOptionDialog(this,
				new Object[] {"Please edit the property:", propField},
				"Edit property", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, null, null);
		if (ret != JOptionPane.OK_OPTION) return;
		String sNewProp = propField.getText();
		if (sNewProp.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter a property!");
			return;
		}

		propListModel.removeElement(sOldProp);
		propListModel.addElement(sNewProp);
	}

	private void doRemoveProp() {
		String sProp = (String)propList.getSelectedValue();
		if (sProp == null) {
			JOptionPane.showMessageDialog(this, "Please select a property from the \"Properties\" list!");
			return;
		}
		propListModel.removeElement(sProp);
	}

}

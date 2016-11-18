package gov.nist.csd.pm.application.schema.builder;

import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.browser.PmGraphModel;
import gov.nist.csd.pm.common.browser.PmGraphType;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeType;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *TODO MUST BE IN PMADMIN
 * tabs for pc, oa, ua, u
 * buttons to add
 * MAYBE DRAG AND DROP FEATURE?!?!?!?!
 * for each tab were going to list all attrs 
 * - have user select one
 *      - these are just going to be a list of strings - get the string name and the active tab to get the proper node
 *  - drag it to the graph on the right and drop in container he wants to add it to
 * @author Administrator
 *
 */
public class PMBuilder extends JFrame{

	//PC OA UA U
	private JList[] attrLists = new JList[4];
	private DefaultListModel[] attrListModels = new DefaultListModel[4];
	private final String[] ATTR_NAMES = {"PC", "OA", "UA", "U"};
	private JTabbedPane attrsPanel;
	private String activePanel;
	private JButton create;
	private PmGraph tree;
	private JLabel nameLabel;
	private JLabel idLabel;
	private JLabel typeLabel;
	private JTextField schemaNameField;
	private JTextField descrField;
	private JTextField otherField;
	private JTextField propField;
	private JTextField keyField;

	public PMBuilder(){
		createGUI();
	}

	private void createGUI(){
		setTitle("PMBuilder");
		setLayout(new BorderLayout(5, 5));
		add(buildLeftPanel(), BorderLayout.WEST);
		add(buildRightPanel(),  BorderLayout.CENTER);
		add(buildTopPanel(), BorderLayout.NORTH);
		pack();
		setVisible(true);
	}
	
	private JPanel buildTopPanel(){
		JPanel topPanel = new JPanel();
		topPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Schema Info"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		topPanel.add(new JLabel("Schema Name:"));
		schemaNameField = new JTextField(10);
		topPanel.add(schemaNameField);
		
		topPanel.add(new JLabel("Description:"));
		descrField = new JTextField(10);
		topPanel.add(descrField);
		
		topPanel.add(new JLabel("Other Info:"));
		otherField = new JTextField(10);
		topPanel.add(otherField);
		
		//TODO drag OAs to keys field
		
		topPanel.add(new JLabel("Properties:"));
		propField = new JTextField(10);
		topPanel.add(propField);
		
		topPanel.add(new JLabel("Keys:"));
		keyField = new JTextField(10);
		topPanel.add(keyField);
		
		JButton createSchemaButton = new JButton("Create Schema");
		topPanel.add(createSchemaButton);
		
		return topPanel;
	}
	
	private JPanel buildRightPanel(){
		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("PM Hierarchy"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		rightPanel.setLayout(new BorderLayout(5, 5));
		rightPanel.setPreferredSize(new Dimension(500, 400));
		
		JTabbedPane trees = new JTabbedPane();
		JPanel oaTree = new JPanel();
		oaTree.setLayout(new BorderLayout(5, 5));
		tree = new PmGraph(new PmNode(PmNodeType.CONNECTOR, "root"), false);
		tree.setTransferHandler(new TransferHandler(){
			public boolean canImport(TransferSupport support) {
                // for the demo, we'll only support drops (not clipboard paste)
                if (!support.isDrop()) {
                    return false;
                }

                // we only import Strings
                if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return false;
                }
                //System.out.println("we can import");
                return true;
            }

            public boolean importData(TransferSupport support) {
                // if we can't handle the import, say so
                if (!canImport(support)) {
                    return false;
                }
                System.out.println("b");
                // fetch the drop location
                JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
                System.out.println("a");
                TreePath row = dl.getPath();
                System.out.println(row);
                // fetch the data and bail if this fails
                String data;
                try {
                    data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
                
                System.out.println(data);
                
                PmNode n = (PmNode) row.getLastPathComponent();
                
                PmGraphModel model = (PmGraphModel) tree.getModel();
                PmNode.linkNodes(n, PmNode.createObjectAttributeNode(data));
                //TODO create oa/ua etc...
                //model.insertNodeInto(new DefaultMutableTreeNode(data), n, 0);

                
              
                
               
                return true;
            }
		});
		oaTree.add(new JScrollPane(tree), BorderLayout.CENTER);
		trees.add(oaTree, "Object Attributes");
		
		JPanel uaTree = new JPanel();
		uaTree.setLayout(new BorderLayout(5, 5));
		uaTree.add(new JScrollPane(new JTree()), BorderLayout.CENTER);
		trees.add(uaTree, "Users/User Attributes");
		
		rightPanel.add(trees);
		return rightPanel;
	}
	
	private JPanel buildLeftPanel(){
		JPanel leftPanel = new JPanel();
		//leftPanel.setLayout(new GridLayout(2, 1));
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		//leftPanel.add(buildUpperLeftPanel());
		//leftPanel.add(buildLowerLeftPanel());
		splitPane.setLeftComponent(buildUpperLeftPanel());
		splitPane.setRightComponent(buildLowerLeftPanel());
		leftPanel.add(splitPane);
		//add(leftPanel);
		return leftPanel;
	}
	
	private JPanel buildLowerLeftPanel(){
		JPanel lowerLeftPanel = new JPanel();
		lowerLeftPanel.setLayout(new BorderLayout(5, 5));
		lowerLeftPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Selected Node Information"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		JPanel compPanel = new JPanel();
		compPanel.setLayout(new GridLayout(4, 1));
		nameLabel = new JLabel("Name: ");
		idLabel = new JLabel("ID: ");
		typeLabel = new JLabel("Type: ");
		compPanel.add(nameLabel);
		compPanel.add(idLabel);
		compPanel.add(typeLabel);
		
		lowerLeftPanel.add(compPanel);
		
		JButton setPermsButton = new JButton("Set Permissions");
		setPermsButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				//TODO set permissions for selected node
			}
		});
		lowerLeftPanel.add(setPermsButton, BorderLayout.SOUTH);
		return lowerLeftPanel;
		
	}
	
	private JPanel buildUpperLeftPanel(){
		JPanel upperLeftPanel = new JPanel();
		upperLeftPanel.setLayout(new BorderLayout(5, 5));
		upperLeftPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Attributes"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		
		attrsPanel = new JTabbedPane();
		for(int i = 0; i < attrLists.length; i++){
			DefaultListModel model = new DefaultListModel();
			attrLists[i] = new JList(model);
			attrLists[i].setDragEnabled(true);
			attrListModels[i] = model;

			JPanel listPanel = new JPanel();
			listPanel.setLayout(new BorderLayout(5, 5));
			listPanel.add(attrLists[i]);
			loadList(i);
			
			attrsPanel.add(new JScrollPane(listPanel), ATTR_NAMES[i]);
		}
		attrsPanel.setPreferredSize(new Dimension(250, 250));
		upperLeftPanel.add(attrsPanel, BorderLayout.CENTER);
		
		attrsPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int sel = attrsPanel.getSelectedIndex();
				activePanel = ATTR_NAMES[sel];
				create.setText("Create New " + activePanel);
				System.out.println(activePanel);
			}
		});
	
		create = new JButton("Create New");
		create.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {	
				//TODO create new attr
			}
		});
		
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {	
				//TODO refresh current list
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));
		buttonPanel.add(create);
		buttonPanel.add(refresh);
		
		upperLeftPanel.add(buttonPanel, BorderLayout.SOUTH);
		return upperLeftPanel;
	}

	private void loadList(int i){
		attrLists[i].setListData(new String []{"aaaaaa", "Baaaaaa", "CBaaaaaa", "DBaaaaaa", "EBaaaaaa", 
				"fBaaaaaa", "gBaaaaaa", "hBaaaaaa", "aBaaaaaa", "BBaaaaaa", "CBaaaaaa", "DBaaaaaa", "EBaaaaaa", "f", "gBaaaaaa", "hBaaaaaa"});
	}
	
	public static void main(String[] args){
		new PMBuilder();
	}
}

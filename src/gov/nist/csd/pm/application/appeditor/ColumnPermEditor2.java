package gov.nist.csd.pm.application.appeditor;

import gov.nist.csd.pm.common.application.SSLSocketClient;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ListModel;

import java.awt.BorderLayout;

public class ColumnPermEditor2 extends JDialog implements ActionListener, ListSelectionListener{

	private SSLSocketClient sslClient;
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
	private JButton closeButton;
	private JLabel uattrLabel;
	private String columnName;
	private String selectedUattr;
	private String selectedPerms;
	private String base;
	public static final String PM_PROP_DELIM = "=";
    public static final String PM_FIELD_DELIM = ":";
    public static final String PM_ALT_FIELD_DELIM = "|";
    public static final String PM_ALT_DELIM_PATTERN = "\\|";
    public static final String PM_NODE_USER = "u";
    public static final String PM_NODE_USERA = "U";
    public static final String PM_NODE_UATTR = "a";
    public static final String PM_NODE_UATTRA = "A";
    public static final String PM_NODE_POL = "p";
    public static final String PM_NODE_POLA = "P";
    public static final String PM_NODE_OATTR = "b";
    public static final String PM_NODE_OATTRA = "B";
    public static final String PM_NODE_ASSOC = "o";
    public static final String PM_NODE_ASSOCA = "O";
    public static final String PM_NODE_OPSET = "s";
    public static final String PM_NODE_OPSETA = "S";
    public static final String PM_NODE_CONN = "c";
    public static final String PM_NODE_CONNA = "C";
	private SchemaBuilder3 app3;
	private int row;
	
	public ColumnPermEditor2(SSLSocketClient sslClient, SchemaBuilder3 app) {
		//super(tool, true);

		this.sslClient = sslClient;
		this.app3 = app;
		setTitle("Set Permissions");
		this.setResizable(false);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				doClose();
			}
		});

		// Start building the GUI
		// First the user attributes list and panel.
		uattrLabel = new JLabel("User attributes:");
		uattrListModel = new DefaultListModel();
		uattrList = new JList(uattrListModel);
		uattrList.addListSelectionListener(this);
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
		selPermList.addListSelectionListener(this);
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
		allPermList.addListSelectionListener(this);
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
		setButton.addActionListener(this);
		setButton.setActionCommand("set");

		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		resetButton.setActionCommand("reset");

		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButton.setActionCommand("close");

		JPanel buttonPane = new JPanel();
		buttonPane.add(subgraphBox);
		buttonPane.add(setButton);
		buttonPane.add(resetButton);
		buttonPane.add(closeButton);
		buttonPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// The content pane.
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(listPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.SOUTH);

		setContentPane(contentPane);
	}

	public void prepare(String sName, String sType) {
		// Save the entity on which we want to set permissions.
		sEntityName = sName;
		//sEntityId = sId;
		sEntityType = sType;

		// Set the title right.
		String sEntityTypeName = getEntityTypeName(sEntityType);
		setTitle("Set permissions on " + sEntityTypeName + " \"" + sEntityName + "\"");

		// Clear lists and vectors.
		uattrListModel.clear();
		if (uattrVector == null) uattrVector = new Vector();
		else uattrVector.clear();
		selPermListModel.clear();
		allPermListModel.clear();

		// Fill the user attribute list.
		Packet packet = getUserAttributes();
		if (packet != null) for (int i = 0; i < packet.size(); i++) {
			String sLine = packet.getStringValue(i);
			String[] pieces = sLine.split(PM_FIELD_DELIM);
			int index = getIndex(uattrListModel, pieces[0]);
			uattrListModel.add(index, pieces[0]);
			uattrVector.add(index, pieces[1]);
		}

		// Fill the All Permissions list and vector.
		packet = getAllOps();
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

	private Packet getAllOps() {
		try {
			//Packet cmd = appBuilder.makeCmd("getAllOps");
			Packet cmd = app3.makeCmd("getAllOps");
			Packet res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in getAllOps: " + res.getErrorMessage());
				return null;
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Exception in getAllOps: " + e.getMessage());
			return null;
		}
	}

	private Packet getUserAttributes() {
		try {
			//Packet cmd = appBuilder.makeCmd("getUserAttributes");
			Packet cmd = app3.makeCmd("getUserAttributes");
			Packet res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, "Error in getUserAttributes: " + res.getErrorMessage());
				return null;
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Exception in getUserAttributes: " + e.getMessage());
			return null;
		}
	}

	private String getEntityTypeName(String sType) {
		if (sType.equalsIgnoreCase(PM_NODE_USER)) return "user";
		else if (sType.equalsIgnoreCase(PM_NODE_UATTR)) return "user attribute";
		else if (sType.equalsIgnoreCase(PM_NODE_OATTR)) return "object attribute";
		else if (sType.equalsIgnoreCase(PM_NODE_ASSOC)) return "object";
		else if (sType.equalsIgnoreCase(PM_NODE_POL)) return "policy";
		else if (sType.equalsIgnoreCase(PM_NODE_CONN)) return "connector";
		else return "unknown";
	}

	private void addComp(Container container, Component component, int x, int y, int w, int h) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = w;
		constraints.gridheight = h;
		container.add(component, constraints);
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
		row = r;
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
		base = app3.getTableName();
		
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
			JOptionPane.showMessageDialog(app3, "error in doSet ColPermEditor");
			return;
		}
		//appBuilder.addToPermissions(line);
		app3.addToPermissions(line);
		this.setVisible(false);
		JOptionPane.showMessageDialog(app3, "Permissions set for Object Attribute " + columnName, "Success", JOptionPane.INFORMATION_MESSAGE);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("set")) {
			doSet();
		} else if (e.getActionCommand().equalsIgnoreCase("close")) {
			doClose();
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
}

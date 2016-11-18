package gov.nist.csd.pm.application.schema.builder;

import java.awt.Dimension;




import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SysCaller;


/**
 * TODO allow user to assign columns to containers
 * imrpove search function
 * make sure a schema is opened in SB before it opens
 * @author Administrator
 *
 */

public class SchemaAugmentor extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3841781878784329872L;

	/**
	 * Utility methods
	 */
	public Utilities util;

	/**
	 * SysCaller instance
	 */
	public SysCaller sysCaller;

	public OattrEditor oaEditor;

	public DenyEditor denyEditor;

	public RecordAssigner recAssign;
	
	public UattrEditor uaEditor;

	private SchemaBuilder3 schema;

	private JTabbedPane tabPane;
	
	private PermissionEditor permEditor;
	
	private AssociationManager assocMgr;

	public SchemaAugmentor(SchemaBuilder3 sb, Utilities u, SysCaller sys){
		util = u;
		sysCaller = sys;
		schema = sb;
		if(schema.getSchemaName() == null || schema.getSchemaName().length() == 0){
			JOptionPane.showMessageDialog(this, "Please select a schema.");
			return;
		}

		oaEditor = new OattrEditor(schema, util, sysCaller);
		uaEditor = new UattrEditor(schema, util, sysCaller);
		recAssign = new RecordAssigner(schema, util, sysCaller);
		denyEditor = new DenyEditor(schema, util, sysCaller);
		denyEditor.prepare();
		permEditor = new PermissionEditor(schema, util, sysCaller);
		permEditor.prepare();
		assocMgr = new AssociationManager(util, schema, sysCaller);
		
		createGUI();
	}

	private void createGUI(){
		setTitle("Schema Augmentor");

		Dimension d = new Dimension(oaEditor.getPreferredSize());
		d.width = d.width + 200;
		d.height = d.height + 50;
		setPreferredSize(d);

		tabPane = new JTabbedPane();
		tabPane.setTabPlacement(JTabbedPane.LEFT);
		tabPane.add(oaEditor, "Oattr Editor");
		tabPane.add(uaEditor, "Uattr Editor");
		tabPane.add(recAssign, "Assign Records");
		tabPane.add(assocMgr, "Associations");
		tabPane.add(new JScrollPane(denyEditor), "Deny Editor");		

		tabPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int sel = tabPane.getSelectedIndex();
				if(sel == 0){
					Dimension d = new Dimension(oaEditor.getPreferredSize());
					d.width = d.width + 200;
					d.height = d.height + 50;
					SchemaAugmentor.this.setSize(d);
				}else if(sel == 1){
					Dimension d = new Dimension(uaEditor.getPreferredSize());
					d.width = d.width + 50;
					d.height = d.height + 50;
					SchemaAugmentor.this.setSize(d);
				}else if(sel == 2){
					Dimension d = new Dimension(recAssign.getPreferredSize());
					d.width = d.width + 50;
					d.height = d.height + 50;
					SchemaAugmentor.this.setSize(d);
				}else if(sel == 3){
					Dimension d = new Dimension(assocMgr.getPreferredSize());
					d.width = d.width + 150;
					d.height = d.height + 50;
					SchemaAugmentor.this.setSize(d);
				}else if(sel == 4){
					Dimension d = new Dimension(denyEditor.getPreferredSize());
					d.width = d.width + 145;
					d.height = d.height + 75;
					SchemaAugmentor.this.setSize(d);
				}
			}
		});

		add(tabPane);

		//this.setResizable(false);
		this.pack();
		this.setVisible(true);
	}

	public static void main(String[] args){
		//new SchemaAugmentor(null, null, null);
	}
}

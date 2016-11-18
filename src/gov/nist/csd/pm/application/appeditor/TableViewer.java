package gov.nist.csd.pm.application.appeditor;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.GridLayout;

import static gov.nist.csd.pm.common.util.Generators.generateRandomName;
import static gov.nist.csd.pm.common.info.PMCommand.GET_ID_OF_ENTITY_WITH_NAME_AND_TYPE;
import static gov.nist.csd.pm.common.info.PMCommand.GET_TEMPLATES;
import static gov.nist.csd.pm.common.info.PMCommand.GET_TEMPLATE_INFO;
import javax.swing.DefaultCellEditor;
import javax.swing.JSplitPane;
import java.awt.FlowLayout;
import javax.swing.border.TitledBorder;
import java.io.ByteArrayOutputStream;


import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import gov.nist.csd.pm.admin.PmAdmin;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.browser.PmGraphModel;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.DefaultComboBoxModel;
import com.google.common.base.Throwables;

public class TableViewer extends JFrame {

	private JPanel contentPane;
	private Vector<String> columns;
	private JTextField textField;
	private JTextField textField_1;
	private JTable table;
	
	private TableEditor editor;
	private DefaultTableModel model;

	/**
	 * Launch the application.
	 */
	/*public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TableViewer frame = new TableViewer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

	/**
	 * Create the frame.
	 */
	public TableViewer(Vector<String> cols, Vector<Vector<String>> data, TableEditor edit) {
		editor = edit;
		columns = cols;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 650, 450);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPane.add(panel, BorderLayout.NORTH);
		
		JLabel lblColumnName = new JLabel("Column Name:");
		panel.add(lblColumnName);
		
		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(10);
		
		JLabel lblValue = new JLabel("Value:");
		panel.add(lblValue);
		
		textField_1 = new JTextField();
		panel.add(textField_1);
		textField_1.setColumns(10);
		
		JButton btnSearch = new JButton("Search");
		panel.add(btnSearch);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(5, 0, 5, 0));
		contentPane.add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{632, 0};
		gbl_panel_1.rowHeights = new int[]{23, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JButton btnDone = new JButton("Done");
		GridBagConstraints gbc_btnDone = new GridBagConstraints();
		gbc_btnDone.fill = GridBagConstraints.VERTICAL;
		gbc_btnDone.anchor = GridBagConstraints.EAST;
		gbc_btnDone.gridx = 0;
		gbc_btnDone.gridy = 0;
		panel_1.add(btnDone, gbc_btnDone);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_2.add(panel_3, BorderLayout.SOUTH);
		
		JButton btnAddEntry = new JButton("Add Entry");
		btnAddEntry.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				addEntry();
			}
			
		});
		panel_3.add(btnAddEntry);
		
		JButton btnEditEntry = new JButton("Edit Entry");
		panel_3.add(btnEditEntry);
		
		JButton btnDeleteEntry = new JButton("Delete Entry");
		panel_3.add(btnDeleteEntry);
		
		JScrollPane scrollPane = new JScrollPane();
		panel_2.add(scrollPane, BorderLayout.CENTER);
		
		model = new DefaultTableModel(columns, 0);
		model.setDataVector(data, columns);
		table = new JTable(model);
		
		table.setCellSelectionEnabled(true);
		//table.setEnabled(false);
		//table.setRowSelectionAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);
		scrollPane.setViewportView(table);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		
		JPopupMenu popmenu = new JPopupMenu();
		JMenuItem view = new JMenuItem("Open");
		view.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				int col = table.getSelectedColumn();
				Vector x = (Vector) model.getDataVector().elementAt(row);
				String line = (String) x.get(col);
				
				//TODO TableViewer viewer = new TableViewer(columns, editor);
				
				//openObject(line);
				//String column = model.getColumnName(col);

				//JOptionPane.showMessageDialog(SchemaEditor3.this, line, column, JOptionPane.INFORMATION_MESSAGE);
			}
		});
		popmenu.add(view);
		table.setComponentPopupMenu(popmenu);
		
		setVisible(true);
	}
	
	public void addEntry(){
		//TODO gets the columns and puts them into a window with labels and fields for each column
		JFrame entry = new JFrame();
		entry.getContentPane().setLayout(new GridLayout(2, columns.size()));
		ArrayList<JLabel> labels = new ArrayList<JLabel>();
		ArrayList<JTextField> fields = new ArrayList<JTextField>();
		for(int i = 0; i < columns.size(); i++){
			JLabel j = new JLabel(columns.get(i));
			labels.add(j);
			entry.getContentPane().add(j);
			
			JTextField t = new JTextField();
			fields.add(t);
			entry.getContentPane().add(t);
		}
		
		final ArrayList<JTextField> finalFields = fields;
		
		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener(){
	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(int i = 0; i < finalFields.size(); i++){
					finalFields.get(i).setText("");
				}
			}
			
		});
		
		/*JButton submit = new JButton("Submit");
		reset.addActionListener(new ActionListener(){
	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//TODO SUBMIT
				ArrayList<String> entries = new ArrayList<String>();
				for(int j = 0; j < columns.size(); j++){
					String sObjName = generateRandomName(4);
					String sObjClass = "File";
					String sObjType = "rtf";
					String sContainer = "b|" + columns.get(j);
					String sPerms = "File write";

					String sHandle = sysCaller.createObject3(sObjName, sObjClass, sObjType, sContainer, sPerms, 
							null, null, null, null);
					if(sHandle == null){
						JOptionPane.showMessageDialog(TableEditor.this, "Error in creating object " + sObjName 
								+ " in container " + sContainer , "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					//Set properties for the object and then write it
					Properties props = new Properties();
					String value = finalFields.get(j).getText();
					String colName = columns.get(j);
					props.put(colName, value);
					entries.add(value);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(baos, true);

					try{
						props.store(baos, null);
						pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}\n");
						pw.print(colName + ": " + value);
						pw.close();
						byte[] buf = baos.toByteArray();
						int len = sysCaller.writeObject3(sHandle,  buf);
						if(len < 0){
							JOptionPane.showMessageDialog(TableEditor.this, sysCaller.getLastError() , "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						sysCaller.closeObject(sHandle);
					}catch(Exception e){
						e.printStackTrace();
						return;
					}
				}
				String sObjName = generateRandomName(4);
				String sObjClass = "File";
				String sObjType = "rtf";
				String sContainer = "b|Employee Records";
				String sPerms = "File write";
				
				//for(int i = 0; i < columns.size(); i++){
					
					String sHandle = sysCaller.createObject3(sObjName, sObjClass, sObjType, sContainer, sPerms, 
							null, null, null, null);
					if(sHandle == null){
						JOptionPane.showMessageDialog(TableEditor.this, "Error in creating object " + sObjName 
								+ " in container " + sContainer , "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					Properties props = new Properties();
					for(int i = 0; i < columns.size(); i++){
						String colName = columns.get(i);
						String value = entries.get(i);
						props.put(colName, value);
						entries.add(value);
					}
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(baos, true);

					try{
						props.store(baos, null);
						pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}\n");
						
						for(int i = 0; i < columns.size(); i++){
							String colName = columns.get(i);
							String value = entries.get(i);
							//props.put(colName, value);
							//entries.add(value);
							pw.print(colName + ": " + value);
						}
						
						pw.close();
						byte[] buf = baos.toByteArray();
						int len = sysCaller.writeObject3(sHandle,  buf);
						if(len < 0){
							JOptionPane.showMessageDialog(TableEditor.this, sysCaller.getLastError() , "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						sysCaller.closeObject(sHandle);
					}catch(Exception e){
						e.printStackTrace();
						return;
					}
				//}
				JOptionPane.showMessageDialog(TableEditor.this, "Entry submitted succesfully");
				//TODO method to refresh table data to include new entry
			}
		});*/
		
		
	}
	
	

}

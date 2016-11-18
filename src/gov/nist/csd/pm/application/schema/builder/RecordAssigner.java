package gov.nist.csd.pm.application.schema.builder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.net.Packet;

public class RecordAssigner extends JPanel{

	private Utilities util;

	private SysCaller sysCaller;

	private SchemaBuilder3 schema;

	private JPanel basesPanel;

	private DefaultListModel basesListModel;

	private JList basesList;

	private JPanel recordsPanel;

	private DefaultListModel recordsListModel;

	private JList recordsList;

	private JButton assignButton;

	private JButton refreshButton;

	public RecordAssigner(SchemaBuilder3 sb, Utilities u, SysCaller s){
		schema = sb;
		util = u;
		sysCaller = s;

		createGUI();
	}

	private void createGUI(){
		setPreferredSize(new Dimension(500, 300));
		setBorder(
				new CompoundBorder(
						new TitledBorder("Assign Records in Schema"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);

		setLayout(new BorderLayout(5, 5));

		basesPanel = new JPanel();
		basesPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Bases"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);

		basesListModel = new DefaultListModel();
		basesPanel.setLayout(new BorderLayout(0, 0));
		basesList = new JList(basesListModel);
		basesList.setListData(retrieveBases().toArray());
		
		JScrollPane basesScroll = new JScrollPane();
		basesScroll.setPreferredSize(new Dimension(175, 125));
		basesScroll.setViewportView(basesList);

		basesPanel.add(basesScroll);
		add(basesPanel, BorderLayout.WEST);

		recordsPanel = new JPanel();
		recordsPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Records"),
						BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
		recordsListModel = new DefaultListModel();
		recordsPanel.setLayout(new BorderLayout(0, 0));
		recordsList = new JList(recordsListModel);

		//recordsList.setListData(getRecords(schema.getTempId()));
		recordsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane recordsScroll = new JScrollPane();
		recordsScroll.setViewportView(recordsList);

		recordsPanel.add(recordsScroll);

		add(recordsPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout(5, 5));

		assignButton = new JButton("Assign");
		assignButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				assign();
			}

		});
		buttonPanel.add(assignButton, BorderLayout.WEST);
		
		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				basesList.setListData(retrieveBases().toArray());
				//recordsList.setListData(getRecords(schema.getTempId()));
			}

		});
		buttonPanel.add(refreshButton, BorderLayout.EAST);

		add(buttonPanel, BorderLayout.SOUTH);
	}

	public void assign(){
		Object[] bases = basesList.getSelectedValues();
		Object[] recs = recordsList.getSelectedValues();

		String sBase = "";
		for(int i = 0; i < bases.length; i++){
			sBase += "- " + bases[i] + "\n";
		}

		String sRecs = "";
		for(int i = 0; i < recs.length; i++){
			sRecs += "- " + recs[i] + "\n";
		}

		int answer = JOptionPane.showConfirmDialog(this, "Base(s) Selected:\n"
				+ sBase + "\n" + "Record(s) Selected:\n" 
				+ sRecs + "\n Are you sure you want to assign these records?");
		
		if(answer != JOptionPane.YES_OPTION) return;
		
		for(int i = 0; i < bases.length; i++){
			String base = (String)bases[i];
			for(int j = 0; j < recs.length; j++){
				String rec = (String)recs[i];
				
				boolean ret = sysCaller.assignObjToContainer(rec, base);
				if(!ret){
					JOptionPane.showMessageDialog(this, "Could not assign " + rec + " to " + base + sysCaller.getLastError());
				}
			}
		}
		//JOptionPane.showMessageDialog(this, "Assignment was successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private ArrayList<String> retrieveBases(){
		//TODO just get all oattrs
		ArrayList<String> bases = util.getOattrs();
		System.out.println("BASES: " + bases);
		ArrayList<String> ret = new ArrayList<String>();
		for(int i = 0; i < bases.size(); i++){
			ret.add(bases.get(i).split(":")[0]);
		}
		Collections.sort(ret);
		return ret;
	}
	
	public String[] getRecords(String sTempId){
		//Add function to search for records to assign
		Packet recs = sysCaller.getRecords(sTempId, null);
		String[] retRecs = new String[recs.size()];
		for(int i = 0; i < recs.size(); i++){
			retRecs[i] = recs.getStringValue(i);
		}
		String[] ret = new String[retRecs.length];
		for(int i = 0; i < retRecs.length; i++){
			ret[i] = retRecs[i].split(":")[0];
		}
		Arrays.sort(ret);
		return ret;
	}

	public static void main(String[] args){
		JFrame j = new JFrame();
		j.getContentPane().add(new RecordAssigner(null, null, null));
		j.pack();
		j.setVisible(true);
	}
}

package gov.nist.csd.pm.application.appeditor;

import java.io.*;
import java.util.*;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class DDLParser extends JFrame{

	private String myFileName;
	private FileReader fr;
	private BufferedReader br;
	private Scanner sc;
	ArrayList<String> contents;

	private String[] keywords = {"CREATE", "TABLE"};
	private JTextField textField;
	private JTextArea textArea;

	private String schemaName;

	private SchemaBuilder3 schema;

	public DDLParser(SchemaBuilder3 sb){

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);

		JLabel lblSchemaToImport = new JLabel("Schema to Import:");
		panel.add(lblSchemaToImport);

		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(30);
		schema = sb;
		JButton btnImport = new JButton("Import");
		btnImport.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				myFileName = textField.getText();
				//myFileName = myFileName.replaceAll("\\", "\\\\");
				try {
					sc = new Scanner(new File(myFileName));
					String[] pieces = myFileName.split("\\\\");
					String x = pieces[pieces.length-1];
					pieces = x.split("\\.");
					schema.setSchemaName(pieces[0]);
					schemaName = pieces[0];
					schema.setSchemaField(schemaName);
					schema.importing = true;
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				contents = readFile();
				parseEverything();
			}
		});
		panel.add(btnImport);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea(20, 50);
		scrollPane.setViewportView(textArea);

		pack();
	}
	/*public DDLParser(String fileName){
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);

		JLabel lblSchemaToImport = new JLabel("Schema to Import:");
		panel.add(lblSchemaToImport);

		textField = new JTextField("C:\\Users\\Administrator.WIN-DNAR5079LMF\\Desktop\\DDL.txt");
		panel.add(textField);
		textField.setColumns(30);
		myFileName = fileName;//textField.getText();

		JButton btnImport = new JButton("Import");
		btnImport.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					sc = new Scanner(new File(myFileName));
					String[] pieces = myFileName.split("\\\\");
					String x = pieces[pieces.length-1];
					pieces = x.split("\\.");
					//schema.setSchemaName(pieces[0]);
					//schemaName = pieces[0];
					//schema.setSchemaField(schemaName);
					//schema.importing = true;
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				contents = readFile();
				parseEverything();
			}
		});
		panel.add(btnImport);

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea(20, 50);
		scrollPane.setViewportView(textArea);

		pack();
	}*/

	private void log(Object input){
		textArea.append(input + "\n");
	}

	private ArrayList<String> readFile(){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			while(sc.hasNext()){
				ret.add(sc.next());
			}
			return ret;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private boolean inKeywords(String a){
		for(int i = 0; i < keywords.length; i++){
			if(keywords[i].equals(a)){
				return true;
			}
		}
		return false;
	}

	private int getNumBlocks(){
		int counter = 0;
		for(int i = 0; i < contents.size(); i++){
			if(contents.get(i).equals("CREATE")){
				counter++;
			}
		}
		return counter;
	}
	private String baseName = "";
	public ArrayList<String> getBlock(){
		ArrayList<String> ret = new ArrayList<String>();
		String a1 = contents.get(0);
		if(a1.equals("CREATE")){
			contents.remove(0);
			if(contents.get(0).equals("TABLE")){
				contents.remove(0);
				String name = contents.get(0);//.replaceAll("`", "");
				for(int i = 0; i < name.length(); i++){
					if(!Character.isLetter(name.charAt(i))
							&& name.charAt(i) != '_' 
							&& name.charAt(i) != '.'
							&& !Character.isDigit(name.charAt(i))){
						name = name.replaceAll(name.charAt(i)+"", "");
					}
				}
				contents.remove(0);
				log("Creating table " + name + "...");
				baseName = name;
				schema.addToContainers(new ArrayList<String>(Arrays.asList(new String[]{schemaName, name, "", "b", name, name, name})));

				int start = contents.indexOf("(");
				int end = contents.indexOf(");");
				String x = "";
				for(int i = start+1; i < end-1; i++){
					//ret.add(contents.get(i));
					x += contents.get(i) + " ";
				}
				x += contents.get(end-1);
				for(int i = end; i >= start; i--){
					contents.remove(i);
				}
				String[] pieces = x.split(",");
				ret = new ArrayList<String>(Arrays.asList(pieces));
				//log(ret);
			}
		}
		return ret;    
	}

	public void parseEverything(){
		int y = getNumBlocks();
		for(int i = 0; i < y; i++){
			parseBlock();
		}
		schema.resetSchemaView();
		log("DONE - your schema should be visible in Schema Builder");
	}

	public void parseBlock(){
		ArrayList<String> block = getBlock();
		for(int i = 0; i < block.size(); i++){
			/*if(block.get(i).charAt(0) == '`'){
				String name = block.get(i).replaceAll("`", "");
				log("Creating column " + name + "...");
				schema.addToContainers(new ArrayList<String>(Arrays.asList(new String[]{baseName, name, "", "b", name, name, name})));
			}*/
			Scanner sc = new Scanner(block.get(i));
			//while(sc.hasNext()){
			String name = sc.next();
			for(int j = 0; j < name.length(); j++){
				if(!Character.isLetter(name.charAt(j)) 
						&& name.charAt(j) != '_' 
						&& name.charAt(j) != '.'
						&& !Character.isDigit(name.charAt(j))){
					name = name.replaceAll(name.charAt(j)+"", "");
				}
			}
			log("Creating column " + name + "...");
			schema.addToContainers(new ArrayList<String>(Arrays.asList(new String[]{baseName, name, "", "b", name, name, name})));
			//}
			//the remaining elements in the scanner are column options
		}
	}

	/*
	CREATE TABLE `person` (
	  `person_id` int(11) NOT NULL AUTO_INCREMENT,
	  `gender` varchar(50) DEFAULT '',
	  `birthdate` date DEFAULT NULL,
	  `birthdate_estimated` tinyint(1) NOT NULL DEFAULT '0',
	  `dead` tinyint(1) NOT NULL DEFAULT '0',
	  `death_date` datetime DEFAULT NULL,
	  `cause_of_death` int(11) DEFAULT NULL,
	  `creator` int(11) DEFAULT NULL,
	  `date_created` datetime NOT NULL,
	  `changed_by` int(11) DEFAULT NULL,
	  `date_changed` datetime DEFAULT NULL,
	  `voided` tinyint(1) NOT NULL DEFAULT '0',
	  `voided_by` int(11) DEFAULT NULL,
	  `date_voided` datetime DEFAULT NULL,
	  `void_reason` varchar(255) DEFAULT NULL,
	  `uuid` char(38) DEFAULT NULL,
	  `deathdate_estimated` tinyint(1) NOT NULL DEFAULT '0'
	);
	 */ 
	public static void main(String[] args){
		//DDLParser parser = new DDLParser("C:\\Users\\Administrator.WIN-DNAR5079LMF\\Desktop\\DDL3.txt");
		//parser.setVisible(true);
	}
}
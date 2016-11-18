package gov.nist.csd.pm.application.schema.importing;

import gov.nist.csd.pm.application.schema.builder.SchemaBuilder3;

import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class DDLParser extends JFrame implements PropertyChangeListener{

	/**
	 * Name of the file
	 */
	private String myFileName;

	/**
	 * Scanner to read from file
	 */
	private Scanner sc;

	/**
	 * List containg all of the contents fo the file
	 */
	private ArrayList<String> contents;

	/**
	 * message to be output to the screen 
	 */
	private String message;

	/**
	 * Name of the schema
	 */
	private String schemaName;

	/**
	 * TextField to put the path once a file is selected
	 */
	private JTextField pathField;

	/**
	 * TextArea where log messages will be sent to
	 */
	private JTextArea outputWindow;

	/**
	 * The SchemaBuilder3 instance which will handle creathing the OAs
	 */
	private SchemaBuilder3 schema;

	/**
	 * Progress bar to show progress of processing file
	 */
	private JProgressBar progressBar;

	/**
	 * Keywords to look for in the file
	 */
	private String[] keywords = {"CREATE", "TABLE"};

	/**
	 * Int holding the amount of blocks in the file
	 */
	private int numberOfBlocks;
	
	private static FileWriter fw;


	public DDLParser(SchemaBuilder3 sb){
		try {
			fw = new FileWriter("C:\\Users\\Administrator.WIN-DNAR5079LMF\\Desktop\\DDLParser.txt");
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		schema = sb;
		createGUI();
	}
	public DDLParser(){
		createGUI();
	}
	
	public static void log(Object input){
		try{
			fw.write(input + "\r\n");
			fw.flush();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * creates the GUI
	 */
	public void createGUI(){
		setTitle("DDL Parser");
		JPanel upperPanel = new JPanel();
		getContentPane().add(upperPanel, BorderLayout.NORTH);

		JLabel lblSchemaToImport = new JLabel("Schema to Import:");
		upperPanel.add(lblSchemaToImport);

		pathField = new JTextField("C:\\Users\\Administrator.WIN-DNAR5079LMF\\Desktop\\DDL4.txt");
		upperPanel.add(pathField);
		pathField.setColumns(30);
		myFileName = pathField.getText();

		JButton browser = new JButton("Browse");
		browser.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showOpenDialog(null);
				if(returnVal == JFileChooser.CANCEL_OPTION)return;
				pathField.setText(fileChooser.getSelectedFile().getPath());
				myFileName = pathField.getText();
			}
		});
		upperPanel.add(browser);

		JButton btnImport = new JButton("Import");
		btnImport.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					sc = new Scanner(new File(myFileName));
					String[] pieces = myFileName.split("\\\\");
					String x = pieces[pieces.length-1];
					pieces = x.split("\\.");
					schema.setSchemaName(pieces[0]);
					schemaName = pieces[0];
					schema.setSchemaField(schemaName);
					schema.importing();
				}catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				contents = readFile();
				delegateToTask();
			}
		});
		upperPanel.add(btnImport);

		progressBar = new JProgressBar(0, 100);
		progressBar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 10, 10, 10), 
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
		Dimension d = progressBar.getPreferredSize();
		d.width = 500;
		progressBar.setPreferredSize(d);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		JPanel outputPanel = new JPanel();
		getContentPane().add(outputPanel, BorderLayout.CENTER);
		outputPanel.setLayout(new BorderLayout(0, 0));

		outputWindow = new JTextArea(25, 40);

		outputPanel.add(progressBar, BorderLayout.NORTH);
		outputPanel.add(new JScrollPane(outputWindow), BorderLayout.CENTER);

		pack();
		setVisible(true);
	}

	private void delegateToTask(){
		DDLTask ddl = new DDLTask();
		ddl.addPropertyChangeListener(this);
		ddl.execute();
	}
	
	private ArrayList<String> readFile(){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			while(sc.hasNext()){
				ret.add(sc.next());
			}
			sc.close();
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
		numberOfBlocks = counter;
		return counter;
	}

	private int getNumberOfBlocks(){
		return numberOfBlocks;
	}
	private String baseName = "";

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar.setValue(progress);
		}
	}

	public static void main(String[] args){
		new DDLParser();//"C:\\Users\\Administrator.WIN-DNAR5079LMF\\Desktop\\DDL3.txt");
	}

	class DDLTask extends SwingWorker<String, String>{

		@Override
		public String doInBackground() {
			setProgress(0);

			progressBar.setMaximum(100);

			parseEverything();

			return null;
		}

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
					
					baseName = name;
					schema.addToContainers(new ArrayList<String>(Arrays.asList(new String[]{schemaName, name, "", "b", name, name, name})));
					message = "Creating TABLE \"" + name + "\"\n";
					log(message);
					publish(message);
					
					int start = contents.indexOf("(");
					int end = contents.indexOf(")  ");
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

		public void parseBlock(){
			ArrayList<String> block = getBlock();
			log("BLOCK SIZE: " + block.size());
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
				message = "\tCreating COLUMN \"" + name + "\"\n";
				publish(message);
				log(message);
				schema.addToContainers(new ArrayList<String>(Arrays.asList(new String[]{baseName, name, "", "b", name, name, name})));
			}
			setProgress(getProgress()+100/getNumberOfBlocks());
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {}
		}

		public void parseEverything(){
			log(">>" + contents);
			int y = getNumBlocks();
			for(int i = 0; i < y; i++){
				parseBlock();
			}
			//schema.resetSchemaView();
		}

		@Override
		public void process(List<String> input){
			for(int i = 0; i < input.size(); i++){
				outputWindow.append(input.get(i));
			}
		}

		@Override
		public void done() {
			outputWindow.append("The Schema should be visible in SchemaBuilder now!\n");
		}
	}
}
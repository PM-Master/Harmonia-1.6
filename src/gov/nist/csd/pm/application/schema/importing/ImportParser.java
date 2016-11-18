package gov.nist.csd.pm.application.schema.importing;

import gov.nist.csd.pm.application.schema.builder.SchemaBuilder3;
import gov.nist.csd.pm.common.constants.PM_NODE;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImportParser extends JFrame{

	/**
	 * Name of the file
	 */
	private String myFileName;

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
	public final String CREATE_KEYWORD = "CREATE";
	public final String TABLE_KEYWORD = "TABLE";

	/**
	 * Int holding the amount of blocks in the file
	 */
	private int numberOfBlocks;

	/**
	 * List to hold table names and column names
	 */
	private List<Table> tables = new ArrayList<Table>();
    private ImporterTask it;
    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JTextField passField;
    private JTextField dbNameField;

    private boolean connected;

    public static void main(String[] args){
        ImportParser parser = new ImportParser(null);
        parser.setLocationRelativeTo(null);
        parser.setVisible(true);
    }

	public ImportParser(SchemaBuilder3 sb){
		schema = sb;
		createGUI();
	}
	/**
	 * Creates the GUI of the Parser
	 */
	void createGUI(){
		setTitle("Import Parser");

        JTabbedPane pane = new JTabbedPane();
        pane.setTabPlacement(JTabbedPane.TOP);
        getContentPane().add(pane, BorderLayout.NORTH);

		JPanel fileImport = new JPanel();
		pane.add(fileImport, "Import File");

		JLabel lblSchemaToImport = new JLabel("Schema to Import:");
        fileImport.add(lblSchemaToImport);

		pathField = new JTextField();
        fileImport.add(pathField);
		pathField.setColumns(30);
		myFileName = pathField.getText();

		JButton browser = new JButton("Browse");
		browser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.CANCEL_OPTION) return;
                pathField.setText(fileChooser.getSelectedFile().getPath());
                myFileName = pathField.getText();
            }
        });
        fileImport.add(browser);

		JButton btnImport = new JButton("Import");
		btnImport.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
                outputWindow.setText("");
                System.out.println(System.getProperty("user.dir"));
                pathField.setText(".\\doc\\MYSQLSchema\\emp_rec.sql");
				myFileName = pathField.getText();
				String[] pieces = myFileName.split("\\\\");
				String x = pieces[pieces.length-1];
				pieces = x.split("\\.");
				schemaName = pieces[0];

                schema.setSchemaName(schemaName);
                if(!schema.createPCSchema(PM_NODE.POL.value, PM_NODE.OATTR.value)){
                    return;
                }
                schema.setSchemaField(schemaName);
				schema.importing();

				progressBar.setString("Reading File \"" + schemaName + "\"...");

				String fileType = pieces[1];
				Parser parser = null;
				if(fileType.equals("rtf")){
					parser = new RTFParser();
				}else if(fileType.equals("txt")){
					parser = new TXTParser();
				}else if(fileType.equals("sql")){
					parser = new SQLParser();
				}else{
                    return;
                }

				contents = parser.readLines(myFileName);
                if(contents == null){
                    JOptionPane.showMessageDialog(ImportParser.this, "Error reading file", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                tables.clear();
                tables = parser.parse(contents);

				//numberOfBlocks = contents.length();
				progressBar.setString(null);
				delegateToTask();
			}
		});
        fileImport.add(btnImport);

        JPanel connectPane = new JPanel();
        pane.add(connectPane, "Connect to Database");

        //host, port, user, password
        connectPane.add(new JLabel("Host:"));
        hostField = new JTextField(10);
        connectPane.add(hostField);
        hostField.setText("localhost");

        connectPane.add(new JLabel("Port:"));
        portField = new JTextField(5);
        connectPane.add(portField);
        portField.setText("3306");

        connectPane.add(new JLabel("User:"));
        userField = new JTextField(10);
        connectPane.add(userField);
        userField.setText("root");

        connectPane.add(new JLabel("Password:"));
        passField = new JTextField(10);
        connectPane.add(passField);
        passField.setText("root");

        connectPane.add(new JLabel("Database:"));
        dbNameField = new JTextField(10);
        connectPane.add(dbNameField);
        dbNameField.setText("testdb");

        JButton connectBtn = new JButton("Connect");
        connectBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                outputWindow.setText("");
                tables.clear();
                String host = hostField.getText();
                String port = portField.getText();
                String user = userField.getText();
                String pass = passField.getText();
                String dbName = dbNameField.getText();

                schemaName = dbName;
                schema.setSchemaName(schemaName);
                if(!schema.createPCSchema(PM_NODE.POL.value, PM_NODE.OATTR.value)){
                    return;
                }
                schema.setSchemaField(schemaName);
                schema.connecting();

                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    //System.out.println("Connecting to database...");
                    //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/","root", "root");
                    Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName, user, pass);
                    if (conn != null) {
                        //System.out.println("connected to db");
                        conn.setAutoCommit(true); // No need to manually commit

                    } else {
                        JOptionPane.showMessageDialog(null, "Could not establish database connection...");
                        return;
                    }

                    long s = System.currentTimeMillis();
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate("use " + dbName);

                    PreparedStatement ps = conn.prepareStatement("show tables");
                    ResultSet rs = ps.executeQuery();
                    if (rs!=null) {
                        while(rs.next()){
                            String tableName = rs.getString(1);
                            System.out.println("tableName: " + tableName);
                            PreparedStatement ps1 = conn.prepareStatement("describe " + tableName);
                            ResultSet rs1 = ps1.executeQuery();
                            List<String> cols = new ArrayList<String>();
                            while(rs1.next()){
                                String columnName = rs1.getString(1);
                                System.out.println("\tcolumn: " + columnName);
                                cols.add(columnName);
                            }

                            stmt = conn.createStatement();
                            rs1 = stmt.executeQuery("select * from " + tableName);
                            ResultSetMetaData meta = rs1.getMetaData();
                            int numCols = meta.getColumnCount();
                            List<List<Object>> data = new ArrayList<List<Object>>();
                            if (rs1!=null) {
                                while(rs1.next()){
                                    ArrayList<Object> row = new ArrayList<Object>();
                                    for(int i = 0; i < numCols; i++){
                                        if(rs1.getObject(i+1) != null){
                                            //row.add(rs.getObject(i+1));
                                            row.add("*");
                                        }
                                    }
                                    data.add(row);
                                }
                            }

                            Table t = new Table(tableName, cols.toArray(new String[cols.size()]), data);
                            tables.add(t);
                        }
                    }
                    long end = System.currentTimeMillis();
                    System.out.println("TIME: " + (double)(end-s)/1000);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
                connected = true;
                progressBar.setString(null);
                delegateToTask();
            }
        });
        connectPane.add(connectBtn);



		progressBar = new JProgressBar(0, 100);
		progressBar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 10, 10, 10),
				BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
		Dimension d = progressBar.getPreferredSize();
		d.width = 500;
		progressBar.setPreferredSize(d);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		progressBar.setString("Waiting...");

		JPanel outputPanel = new JPanel();
		getContentPane().add(outputPanel, BorderLayout.CENTER);
		outputPanel.setLayout(new BorderLayout(0, 0));

		outputWindow = new JTextArea(25, 40);

		outputPanel.add(progressBar, BorderLayout.NORTH);
		outputPanel.add(new JScrollPane(outputWindow), BorderLayout.CENTER);

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
                if(it != null){
                    it.cancel(true);
                    schema.doDelete(schemaName, PM_NODE.POL.value);
                }
				setVisible(false);
			}
		});
		outputPanel.add(close, BorderLayout.SOUTH);

		pack();
	}

	protected void delegateToTask() {
		it = new ImporterTask();
		it.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress" == evt.getPropertyName()) {
                    int progress = (Integer) evt.getNewValue();
                    progressBar.setValue(progress);
                }
            }
        });
		it.execute();
	}

	class ImporterTask extends SwingWorker<String, String>{

        public boolean error = false;

		@Override
		public String doInBackground() {
			parseEverything();

			return null;
		}

		public Table getTable(int index){
			return tables.get(index);
		}

		public void parseBlock(int index){
            error = false;
			Table table = getTable(index);
			System.out.println("BLOCK: " + table);

			//create table oattr
			String tableName = table.getTable().trim();
			System.out.println("SCHEMA NAME: " + schemaName);

			if(!schema.getUtilities().createOattr(schema.getSchemaType(PM_NODE.OATTR.value), tableName,
                    schemaName, schema.getSchemaType(PM_NODE.OATTR.value))){
                message = "Could not create the table Object attribute";
                publish(message);
                it.cancel(true);
                error = true;
                return;
            }

			//output message to log
			message = "Importing TABLE \"" + tableName + "\"\n";
			publish(message);

			//create columns
			String[] columns = table.getColumns();

			for(int i = 0; i < columns.length; i++){
                String c = columns[i];
				message = "\tImporting COLUMN \"" + c + "\"\n";
				publish(message);

				c = c.trim();

				if(!schema.getUtilities().createOattr(schema.getSchemaType(PM_NODE.OATTR.value),
                        tableName + "_" + c, tableName, schema.getSchemaType(PM_NODE.OATTR.value))){
                    message = "Could not create the column Object attribute";
                    publish(message);
                    it.cancel(true);
                    error = true;
                    return;
                }

                List<List<Object>> data = table.getData();
                if(data != null){
                    message = "\t\tImporting DATA for column  \"" + c + "\"\n";
                    publish(message);
                    List<Object> col = table.getColumn(i);
                    for(int j = 0; j < col.size(); j++) {
                        if(col.get(j).equals("*")) {
                            String sHandle = schema.getSysCaller().createObject3(UUID.randomUUID().toString().split("-")[0], "File", "doc", "b|" + tableName + "_" + c, "File write",
                                    null, null, null, null);
                            if (sHandle == null) {
                                message = "Could not create a representation for data";
                                publish(message);
                                it.cancel(true);
                                error = true;
                                return;
                            }
                        }
                    }
                }
			}
		}

		public void parseEverything(){
			int y = tables.size();
            progressBar.setMaximum(y);
			for(int i = 0; i < y; i++){
                parseBlock(i);
                setProgress(i);
                System.out.println(getProgress());
			}
			//schema.callImportTask();
		}

		@Override
		public void process(List<String> input){
			for(int i = 0; i < input.size(); i++){
				outputWindow.append(input.get(i));
			}
		}

		@Override
		public void done() {
			if(!error){
                progressBar.setValue(progressBar.getMaximum());
                progressBar.setString("DONE");
                outputWindow.append("\n DONE! The Schema should be visible in SchemaBuilder now!\n");
                schema.setCreated(true);
                schema.enableBuilder();
                schema.resetSchemaView(PM_NODE.POL.value);
                schema.disableCreateButton();
            }else{
                outputWindow.append("\n Error occured during import.\n");
            }
            it = null;
        }
	}
}
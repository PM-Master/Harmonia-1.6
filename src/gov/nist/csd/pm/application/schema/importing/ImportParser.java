package gov.nist.csd.pm.application.schema.importing;

import gov.nist.csd.pm.application.schema.builder.SchemaBuilder3;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.*;
import java.util.*;
import java.util.List;

import static gov.nist.csd.pm.common.constants.GlobalConstants.PM_TEMPLATE;

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
     * The SchemaBuilder instance which will handle creathing the OAs
     */
    private SchemaBuilder3 schema;

    /**
     * Progress bar to show progress of processing file
     */
    private JProgressBar tableProgressBar;
    private JProgressBar rowProgressbar;

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
    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JTextField passField;
    private JTextField dbNameField;

    private boolean connected;
    private JButton btnImport;
    private SwingWorker<String, String> worker;

    public static final String PATH_DELIM = "/";
    public static final String NAME_DELIM = "_";

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

        JPanel connectPane = new JPanel();
        pane.add(connectPane, "Connect to Database");

        JPanel fileImport = new JPanel();
        //pane.add(fileImport, "Import File");

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
                btnImport.setEnabled(true);
            }
        });
        fileImport.add(browser);

        btnImport = new JButton("Import");
        btnImport.setEnabled(false);
        btnImport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                outputWindow.setText("");
                System.out.println(System.getProperty("user.dir"));
                myFileName = pathField.getText();
                String[] pieces = myFileName.split("\\\\");
                String x = pieces[pieces.length - 1];
                pieces = x.split("\\.");
                schemaName = pieces[0];

                schema.setSchemaName(schemaName);
                if (!schema.createPCSchema(PM_NODE.POL.value, PM_NODE.OATTR.value, "storage=database")) {
                    return;
                }
                schema.setSchemaField(schemaName);
                schema.importing();

                tableProgressBar.setString("Reading File \"" + schemaName + "\"...");

                String fileType = pieces[1];
                Parser parser;
                if (fileType.equals("rtf")) {
                    parser = new RTFParser();
                } else if (fileType.equals("txt")) {
                    parser = new TXTParser();
                } else if (fileType.equals("sql")) {
                    parser = new SQLParser();
                } else {
                    return;
                }

                contents = parser.readLines(myFileName);
                if (contents == null) {
                    JOptionPane.showMessageDialog(ImportParser.this, "Error reading file", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                tables.clear();
                tables = parser.parse(contents);
                schema.setTables(tables);

                //numberOfBlocks = contents.length();
                tableProgressBar.setString(null);
                delegateToTask();
            }
        });
        fileImport.add(btnImport);



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
        dbNameField.setText("emp_rec");

        JButton connectBtn = new JButton("Connect");
        connectBtn.addActionListener(e -> {
            outputWindow.setText("");
            tables.clear();
            String host = hostField.getText();
            String port = portField.getText();
            String user = userField.getText();
            String pass = passField.getText();
            String dbName = dbNameField.getText();

            tableProgressBar.setString("Processing database " + dbName);
            //tableProgressBar.setIndeterminate(true);

            schemaName = dbName;
            schema.setSchemaName(schemaName);
            if (!schema.createPCSchema(PM_NODE.POL.value, PM_NODE.OATTR.value, "storage=database")) {
                return;
            }
            schema.setSchemaField(schemaName);
            schema.connecting();

            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName, user, pass);
                if (conn != null) {
                    conn.setAutoCommit(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Could not establish database connection...");
                    return;
                }

                long s = System.currentTimeMillis();
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("use " + dbName);


                PreparedStatement ps = conn.prepareStatement("show full tables where Table_Type = 'BASE TABLE'");
                ResultSet rs = ps.executeQuery();
                if (rs != null) {
                    while (rs.next()) {
                        String tableName = rs.getString(1);
                        System.out.println("tableName: " + tableName);
                        PreparedStatement ps2 = conn.prepareStatement("SELECT k.COLUMN_NAME\n" +
                                "FROM information_schema.table_constraints t\n" +
                                "LEFT JOIN information_schema.key_column_usage k\n" +
                                "USING(constraint_name,table_schema,table_name)\n" +
                                "WHERE t.constraint_type='PRIMARY KEY'\n" +
                                "    AND t.table_schema=DATABASE()\n" +
                                "    AND t.table_name='" + tableName + "';");
                        ResultSet rs2 = ps2.executeQuery();
                        List<String> keys = new ArrayList<>();
                        if (rs2 == null) {
                            JOptionPane.showMessageDialog(null, "Error", "Table does not have a Primary Key.\nThis tool requires the table to have a Primary Key.", JOptionPane.ERROR_MESSAGE);
                            return;
                        } else {
                            while (rs2.next()) {
                                keys.add(rs2.getString(1));
                            }
                        }
                        PreparedStatement ps1 = conn.prepareStatement("describe " + tableName);
                        ResultSet rs1 = ps1.executeQuery();
                        List<String> cols = new ArrayList<String>();
                        String sCols = "";
                        while (rs1.next()) {
                            String columnName = rs1.getString(1);
                            if (!keys.contains(columnName)) {
                                System.out.println("\tcolumn: " + columnName);
                                cols.add(columnName);
                                sCols += columnName + ", ";
                            }
                        }
                        cols.addAll(keys);
                        for (String k : keys) {
                            sCols += k + ", ";
                        }
                        sCols = sCols.substring(0, sCols.length() - 2);

                        stmt = conn.createStatement();
                        System.out.println("select " + sCols + " from " + schemaName + "." + tableName);
                        rs1 = stmt.executeQuery("select " + sCols + " from " + schemaName + "." + tableName);
                        ResultSetMetaData meta = rs1.getMetaData();
                        int numCols = meta.getColumnCount();
                        List<Row> data = new ArrayList<>();
                        while (rs1.next()) {
                            String rowName = "";
                            Vector<Object> rowData = new Vector<>();
                            for (int i = 1; i <= numCols; i++) {
                                String value = String.valueOf(rs1.getObject(i));
                                if (value != null) {
                                    rowData.add("*");
                                }
                                if(keys.contains(meta.getColumnName(i))){
                                    if(rowName.isEmpty()){
                                        rowName += value;
                                    }else{
                                        rowName += NAME_DELIM + value;
                                    }
                                }

                            }
                            Row row = new Row(rowName, rowData);
                            data.add(row);
                        }

                        Table t = new Table(tableName, keys, cols, data);
                        tables.add(t);
                    }
                }
                long end = System.currentTimeMillis();
                System.out.println("TIME: " + (double) (end - s) / 1000);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            schema.setTables(tables);
            schema.schemaPropField.setSelectedItem("Database");
            connected = true;
            tableProgressBar.setString(null);
            delegateToTask();
        });
        connectPane.add(connectBtn);


        tableProgressBar = new JProgressBar(0, 100);
        tableProgressBar.setIndeterminate(false);
        tableProgressBar.setStringPainted(true);
        tableProgressBar.setString("Waiting...");

        rowProgressbar = new JProgressBar(0, 100);
        rowProgressbar.setIndeterminate(false);
        rowProgressbar.setStringPainted(true);

        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.add(tableProgressBar, BorderLayout.NORTH);
        progressPanel.add(rowProgressbar, BorderLayout.SOUTH);
        progressPanel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));

        JPanel outputPanel = new JPanel();
        getContentPane().add(outputPanel, BorderLayout.CENTER);
        outputPanel.setLayout(new BorderLayout(0, 0));

        outputWindow = new JTextArea(25, 40);

        outputPanel.add(progressPanel, BorderLayout.NORTH);
        outputPanel.add(new JScrollPane(outputWindow), BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.addActionListener(arg0 -> {
            /*if(it != null){
                it.cancel(true);
                schema.doDelete(schemaName, PM_NODE.POL.value);
            }*/
            worker.firePropertyChange("cancel", false, true);

            setVisible(false);
        });
        outputPanel.add(close, BorderLayout.SOUTH);

        pack();
    }

    private String progressBar;
    public boolean error = false;
    protected void delegateToTask() {
        worker = new SwingWorker<String, String>(){
            private boolean hasData;

            @Override
            public String doInBackground() {
                parseEverything();

                return null;
            }

            public Table getTable(int index){
                return tables.get(index);
            }

            private void error(String errorMessage){
                message = "\n" + errorMessage;
                publish(message);
                error = true;
                cancel(true);
            }

            public boolean parseBlock(int index){
                error = false;
                Table table = getTable(index);
                System.out.println("BLOCK: " + table);

                //create table oattr
                String tableName = schemaName + PATH_DELIM + table.getTable().trim();
                System.out.println("SCHEMA NAME: " + schemaName);

                if(!schema.getUtilities().createOattr(schema.getSchemaType(PM_NODE.OATTR.value), tableName,
                        schemaName, schema.getSchemaType(PM_NODE.OATTR.value))){
                    error("ERROR: Could not create the table Object attribute");
                    return false;
                }

                //output message to log
                message = "Importing TABLE \"" + tableName + "\"\n";
                publish(message);

                //create acolumn container
                String columnContName = tableName + "(columns)";
                if(!schema.getUtilities().createOattr(schema.getSchemaType(PM_NODE.OATTR.value), columnContName,
                        tableName, schema.getSchemaType(PM_NODE.OATTR.value))){
                    error("ERROR: Could not create the columns container");
                    return false;
                }

                //create Columns
                List<String> columns = table.getColumns();
                List<String> conts = new ArrayList<String>();
                for(int i = 0; i < columns.size(); i++) {
                    String c = tableName + PATH_DELIM +  columns.get(i).trim() ;
                    conts.add(c);
                    message = "\tCreating COLUMN \"" + c + "\"\n";
                    publish(message);

                    if (!schema.getUtilities().createOattr(schema.getSchemaType(PM_NODE.OATTR.value),
                            c, columnContName, schema.getSchemaType(PM_NODE.OATTR.value))) {
                        error("ERROR: Could not create the column Object attribute");
                        return false;
                    }
                }

                //create template for this table
                Packet res = schema.getUtilities().addTemplate(tableName,
                        conts, table.getKeys());
                if(res.hasError()){
                    error("ERROR: could not add a template. " + res.getErrorMessage());
                    return false;
                }
                String tplId = schema.getSysCaller().getIdOfEntityWithNameAndType(tableName, PM_TEMPLATE);
                System.out.println("template: " + tableName + ":" + conts);

                String columnRowName = tableName + "(rows)";
                if(!schema.getUtilities().createOattr(schema.getSchemaType(PM_NODE.OATTR.value), columnRowName,
                        tableName, schema.getSchemaType(PM_NODE.OATTR.value))){
                    error("ERROR: Could not create the rows container");
                    return false;
                }

                //create row containers
                List<Row> rows = table.getData();
                int counter = 0;
                for(Row r : rows){
                    long startTime = System.nanoTime();
                    hasData = true;
                    String rowName = tableName + PATH_DELIM +  r.getRowId();
                    List<Object> data = r.getData();
                    message = "\tROW:\n\t\tName: " + rowName + "\n";
                    publish(message);

                    //create row container
                    if(!schema.getUtilities().createOattr(schema.getSchemaType(PM_NODE.OATTR.value), rowName,
                            columnRowName, schema.getSchemaType(PM_NODE.OATTR.value))){
                        error("ERROR: Could not create the row Object attribute");
                        return false;
                    }
                    //create objects
                    List<String> objNames = new ArrayList<String>();
                    for(Object o : data){
                        String s = (String) o;
                        if(s.equals("*")) {
                            String objName = UUID.randomUUID().toString().split("-")[0];
                            message = "\t\tData: " + objName + "\n";
                            publish(message);
                            String sHandle = schema.getSysCaller().createObject3(objName, "Object", "o", "b|" + rowName, "File write",
                                    null, null, null, null);
                            if (sHandle == null) {
                                error("ERROR: Could not create a representation for data");
                                return false;
                            }
                            objNames.add(objName);
                        }
                    }
                    r.setObjects(objNames);

                    HashMap<String, String> keyMap = new HashMap<String, String>();
                    //keyMap.put(table.getKeys(), rowName);

                    String comps = "";
                    for(int i = 1; i < objNames.size(); i++){
                        comps += ":" + objNames.get(i);
                    }
                    comps = objNames.get(0) + comps;

                    String recId = schema.getSysCaller().getIdOfEntityWithNameAndType(rowName, PM_NODE.OATTR.value);
                    Packet record = schema.getUtilities().genCmd("addTemplateToRecord", recId, tplId);
                /*if(!schema.getSysCaller().addRecordKeys(rowName, keyMap)){
                    error("ERROR: could not add keys to record. " + schema.getSysCaller().getLastError());
                    return;
                }*/

                    record = schema.getUtilities().genCmd("addCompsToRecord", recId, comps);
                    if(record.hasError()){
                        error("ERROR: could not add components to record. " + record.getErrorMessage());
                        return false;
                    }
                    System.out.println("Time to create one row: " + (System.nanoTime() - startTime)/1000000000.0f);
                    counter++;
                    int p = (int)(((double)counter/rows.size())*100);
                    progressBar = "row";
                    setProgress(p);
                }

                //assign record objects to columns
                for(int i = 0; i < conts.size(); i++){
                    String c = conts.get(i).trim();
                    List<Row> data = table.getData();
                    if(data != null){
                        List<String> col = table.getColumnObjects(i);
                        for(String s : col) {
                            schema.getSysCaller().assignObjToContainer(s, c);
                            message = "\t\tAssigning OBJECT \"" + s + "\" to COLUMN \"" + c + "\"\n";
                            publish(message);
                        }
                    }
                }
                return true;
            }

            public void parseEverything(){
                int y = tables.size();
                for(int i = 0; i < y; i++){
                    if(!parseBlock(i)){
                        break;
                    }
                    int p = (int)(((double)i/y)*100);
                    progressBar = "table";
                    setProgress(p);
                    progressBar = "row";
                    setProgress(0);
                }
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
                    tableProgressBar.setIndeterminate(false);
                    tableProgressBar.setValue(tableProgressBar.getMaximum());
                    tableProgressBar.setStringPainted(true);
                    tableProgressBar.setString("DONE");
                    JOptionPane.showMessageDialog(null, "The schema \"" + schemaName + "\" has succesfully been created.\nYou may now use the Schema Augmentor (In Schema Builder go to Edit > Schema Augmentor)\nto manage your schema.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    schema.setCreated(true);
                    schema.enableBuilder();
                    schema.resetSchemaView(PM_NODE.POL.value);
                    schema.submitted();
                    if(hasData){
                        schema.disableSchemaButtons();
                    }
                    setVisible(false);
                }else{
                    outputWindow.append("\nError occured during import.\n");
                    schema.deleteTable(schemaName, PM_NODE.POL.value);
                }
            }
        };
        worker.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("progress")) {
                int progress = (Integer) evt.getNewValue();
                if (progressBar.equals("table")) {
                    tableProgressBar.setValue(progress);
                } else {
                    rowProgressbar.setValue(progress);
                }
            }else if(evt.getPropertyName().equals("cancel")){
                error = true;
                worker.cancel(true);
                schema.doDelete(schemaName, PM_NODE.POL.value);
            }
        });
        worker.execute();
    }

}
package gov.nist.csd.pm.application.schema.tableeditor;

import gov.nist.csd.pm.application.schema.importing.Row;
import gov.nist.csd.pm.application.schema.importing.Table;
import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.*;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static gov.nist.csd.pm.common.constants.GlobalConstants.PM_FIELD_DELIM;

/**
 * allow user to view multiple tables
 * @author Administrator
 *
 * TODO sorting rows
 * add function to clear search and to return to full table
 *
 */
public class TableEditor extends JFrame {

    private static String[] sqlExamples = new String[]{
            "select name,ssn,phone,salary from employees",
            "select name,ssn, phone,salary from employees where ssn like '%'",
            "select name, phone from employees where ssn like '%'"
    };
    private static final int DEFAULT_NUM_ROWS = 10;
    private JButton execQuery;
    private JComboBox sqlList;
    private int numRows = DEFAULT_NUM_ROWS;
    private JSplitPane splitPane;
    private JPanel panel;
    private JPanel panel_4;
    private JPanel contentPane;
    private JTextField searchValueField;
    public static boolean modify = false;
    private String tempName;
    private String tempId;
    private int nSimulatorPort;
    private static SSLSocketClient engineClient;
    private SysCaller sysCaller;
    private String sProcessId;
    private JTable table;
    private static final long serialVersionUID = 1L;
    private TableModel model;
    protected PmNode schemaNode, baseNode;
    private PmGraph tree;
    private JScrollPane scrollPane;
    private JTextField searchColField;
    private Vector<Vector<String>> data = new Vector<Vector<String>>();
    private ArrayList<Container> containerList;
    private Utilities util;
    private String sSessionId;
    private JScrollPane scrollPane_2;
    private TableRowSorter<TableModel> sorter;
    private List<String> tempKeys = new ArrayList<String>();
    private Template activeTemplate;
    private List<Template> templates;
    private String pcName;

    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JTextField passField;
    private JTextField dbNameField;

    private JTextField sqlField;
    private boolean db;

    private String tableName;
    private String key;
    private ArrayList<String> reqColumns;

    public TableEditor(String sessionId, int nSimPort, String sProcId, boolean bDebug) {
        setTitle("Table Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 950, 700);

        nSimulatorPort = (nSimPort < 1024) ? GlobalConstants.PM_DEFAULT_SIMULATOR_PORT
                : nSimPort;
        sProcessId = sProcId;
        sSessionId = sessionId;
        sysCaller = new SysCallerImpl(nSimulatorPort, sessionId, sProcId, bDebug, "TE");
        containerList = new ArrayList<Container>();
        new ArrayList<ArrayList<String>>();
        new ArrayList<ArrayList<String>>();
        templates = new ArrayList<Template>();
        util = new Utilities(sessionId, sProcessId, nSimulatorPort, bDebug);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                dispose();
            }
        });

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmOpen = new JMenuItem("Open");
        mntmOpen.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                //openSchema(true);
                reset();
            }

        });
        mnFile.add(mntmOpen);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(5, 5));
        setContentPane(contentPane);

        splitPane = new JSplitPane();

        panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Schema", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        splitPane.setLeftComponent(panel);
        //getContentPane().add(panel, BorderLayout.WEST);
        panel.setLayout(new BorderLayout(5, 5));


        scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);


        JPanel leftCompButtonPanel = new JPanel();
        leftCompButtonPanel.setLayout(new BorderLayout(5, 5));

        JButton btnResetSchemaView = new JButton("Refresh");
        btnResetSchemaView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetSchemaView();
            }
        });
        leftCompButtonPanel.add(btnResetSchemaView, BorderLayout.WEST);


        JButton btnOpenRecords = new JButton("Open Records");
        btnOpenRecords.addActionListener(e -> {
            //TODO add check for multiple templates and then ask which one they would like to see
            List<String> recs = new ArrayList<>();
            String inClause = " in (";
            String tempName = "";
            String tempId = "";
            List<String> columns = new ArrayList<>();
            List<PmNode> children = lastSelNode.getChildren();
            for(PmNode n : children){
                String id = n.getId();
                String name = n.getName();
                if(util.isRecord(id)){
                    if(columns.isEmpty()) {
                        Packet p = sysCaller.getRecordInfo(id);
                        String line = p.getStringValue(1);
                        String[] pieces = line.split(PM_FIELD_DELIM);
                        tempName = pieces[0];
                        tableName = tempName;
                        tempId = pieces[1];

                        p = sysCaller.getTemplateInfo(tempId);

                        String conts = p.getStringValue(1);
                        pieces = conts.split(PM_FIELD_DELIM);
                        for (String s : pieces) {
                            columns.add(sysCaller.getNameOfEntityWithIdAndType(s, PM_NODE.OATTR.value));
                        }

                        String keys = p.getStringValue(2);
                        pieces = keys.split(PM_FIELD_DELIM);
                        for (String s : pieces) {
                            columns.remove(s);
                            columns.add(0,s);
                        }

                        System.out.println(columns.toString());
                    }
                    //columns = columns.substring(0, columns.length()-1);
                    inClause += "'" + name + "',";
                }
            }
            String queryColumns = columns.toString().replaceAll("\\[", "").replace("]", "");
            inClause = inClause.substring(0, inClause.length()-1);
            inClause += ");";

            String sql = "select " + queryColumns + " from " + tempName + " where id " + inClause;
            sqlField.setText(sql);
            select(sql);
            rows = recs;
            numRows = DEFAULT_NUM_ROWS;
            //updateTable(tempName);
        });
        leftCompButtonPanel.add(btnOpenRecords, BorderLayout.EAST);
        panel.add(leftCompButtonPanel, BorderLayout.SOUTH);

        if(!openSchema(true)){
            return;
        }

        model = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int col){
                return false;
            }
        };
        resetSchemaView();
        sorter = new TableRowSorter<TableModel>(model);

        JMenuItem view = new JMenuItem("Open");
        view.addActionListener(e -> editField());

        JPanel panel_5 = new JPanel();
        contentPane.add(panel_5, BorderLayout.CENTER);
        panel_5.setLayout(new BorderLayout(5, 5));

        JPanel panel_1 = new JPanel();
        panel_5.add(panel_1);
        panel_1.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        panel_1.setLayout(new BorderLayout(5, 5));

        JPanel sqlPanel = new JPanel();
        sqlPanel.setBorder(new TitledBorder(null, "Query", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        sqlPanel.add(new JLabel("Query:"));
        sqlField = new JTextField(40);
        sqlField.setText("select name, phone,ssn, salary from employees limit 50");
        sqlList = new JComboBox(sqlExamples);
        sqlList.setEditable(true);
        sqlPanel.add(sqlList);

        execQuery = new JButton("Execute");
        execQuery.addActionListener(evt -> {
            String sql = (String) sqlList.getSelectedItem();//sqlField.getText();
            try {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sql);

                if (statement instanceof Select) {
                    select(sql);
                } else if (statement instanceof Insert) {
                } else if (statement instanceof Update) {
                    Update update = (Update) statement;
                    update(update);
                } else if (statement instanceof Delete) {
                }
            } catch (JSQLParserException e) {
                e.printStackTrace();
            }
        });
        sqlPanel.add(execQuery);

        JPanel panel_2 = new JPanel();
        //panel_1.add(panel_2, BorderLayout.SOUTH);

        JButton btnAddEntry = new JButton("Add Record");
        btnAddEntry.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (activeTemplate != null) {
                    panel_4.setVisible(true);
                    addEntry();
                } else {
                    JOptionPane.showMessageDialog(TableEditor.this, "A table must be open in order to add a record");
                }
            }
        });

        JButton btnRefreshTable = new JButton("Reset Table");
        btnRefreshTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                loadModelData();
            }
        });
        //panel_2.add(btnRefreshTable);
        //panel_2.add(btnAddEntry);
        panel_2.add(new JLabel("Fetch the next "));

        JTextField limitField = new JTextField(5);
        limitField.setText("10");
        panel_2.add(limitField);

        panel_2.add(new JLabel(" rows"));

        JButton goButton = new JButton("GO");
        goButton.addActionListener(e -> {
            try {
                SwingWorker<String, String> worker = new SwingWorker<String, String>() {
                    @Override
                    protected String doInBackground() throws Exception {
                        numRows += Integer.valueOf(limitField.getText());
                        updateTable(tableName);

                        return null;
                    }
                };
                worker.execute();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            //updateTable();
        });
        panel_2.add(goButton);

        JScrollPane scrollPane_1 = new JScrollPane();
        panel_1.add(scrollPane_1, BorderLayout.CENTER);

        JPanel panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(null, "Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_1.add(sqlPanel, BorderLayout.NORTH);
        panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));


        JLabel lblValue = new JLabel("Query:");
        panel_3.add(lblValue);

        searchValueField = new JTextField();
        panel_3.add(searchValueField);
        searchValueField.setColumns(10);

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                search(searchColField.getText(), searchValueField.getText());
            }

        });
        panel_3.add(btnSearch);

        JButton btnResetSearch = new JButton("Reset Search");
        btnResetSearch.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                search("", "");
                searchValueField.setText("");
                searchColField.setText("");
            }

        });
        panel_3.add(btnResetSearch);


        table = new JTable(model);
        table.setRowSorter(sorter);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(true);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {
                if(!db) {
                    panel_4.setVisible(true);
                    String recName = (String) model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
                    List<String> comps = getCompsForRecord(recName);
                    Collections.sort(comps);
                    System.out.println("Opening records with comps: " + comps);
                    RecordEditor editor = new RecordEditor(TableEditor.this, sysCaller,
                            recName, null, comps, util);
                    scrollPane_2.setViewportView(editor);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });
        scrollPane_1.setViewportView(table);

        panel_4 = new JPanel();
        panel_4.setVisible(false);
        panel_5.add(panel_4, BorderLayout.SOUTH);
        panel_4.setBorder(
                new CompoundBorder(
                        new TitledBorder("Detail View"),
                        BorderFactory.createBevelBorder(BevelBorder.LOWERED)
                )
        );
        panel_4.setPreferredSize(new Dimension(200, 240));
        panel_4.setLayout(new BorderLayout(5, 5));

        scrollPane_2 = new JScrollPane();
        panel_4.add(scrollPane_2, BorderLayout.CENTER);
        JButton btnCollapse = new JButton("Collapse Detail View");
        btnCollapse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel_4.setVisible(false);
            }
        });
        panel_4.add(btnCollapse, BorderLayout.SOUTH);

        splitPane.setRightComponent(panel_5);
        splitPane.setDividerLocation(-1);

        contentPane.add(splitPane, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateTable(String tableName){
        try {

            long start = System.nanoTime();
            List<String> allColumnNames = getTableColumns(tableName);//if null get all the columns of the table
            Hashtable<String, List<String>> results = new Hashtable<>();
            Hashtable<List<String>, List<String>> subsets = new Hashtable<>();

            for (String rowName : rows) {
                String rowId = sysCaller.getIdOfEntityWithNameAndType(rowName, PM_NODE.OATTR.value);

                Packet pack = util.genCmd("getMellMembersOf", rowName, rowId, "b", "ac");
                if (pack.isEmpty()) {
                    continue;
                }

                List<String> pos = new ArrayList<>();
                for (int i = 0; i < pack.size(); i++) {
                    pos.add(pack.getStringValue(i).split(PM_FIELD_DELIM)[1]);
                }

                List<String> okColumns = new ArrayList<String>();

                Vector<Object> row = new Vector<>();
                row.add(rowName);

                for (int i = 0; i < reqColumns.size(); i++) {
                    String column = reqColumns.get(i);
                    String columnId = sysCaller.getIdOfEntityWithNameAndType(column, PM_NODE.OATTR.value);
                    String inters = util.genCmd("getIntersection", columnId, rowId).getStringValue(0);

                    String sql = (String) sqlList.getSelectedItem();//sqlField.getText();
                    String where = "";
                    try {
                        where = sql.substring(sql.indexOf(" where "));
                    } catch (Exception e) {
                    }

                    Collections.sort(pos);
                    if (Collections.binarySearch(pos, inters) >= 0) {
                        //value = (String) t.getRow(rowName).getCell(i - 1);
                        okColumns.add(columnId);
                    } else {
                        //check if inaccessible column is in where clause and columns
                        if (where.contains(reqColumns.get(i))) {
                            row.clear();
                            break;
                        }
                    }

                    try {
                        //check if inaccessible column is in where clause and not in columns
                        String[] wherePieces = where.split(" |=");
                        boolean cantRead = false;
                        for (String p : wherePieces) {
                            if (allColumnNames.contains(p.trim())) {
                                column = p;
                                columnId = sysCaller.getIdOfEntityWithNameAndType(column, PM_NODE.OATTR.value);
                                inters = util.genCmd("getIntersection", columnId, rowId).getStringValue(0);
                                if (Collections.binarySearch(pos, inters) < 0) {
                                    row.clear();
                                    cantRead = true;
                                    break;
                                }
                            }
                        }
                        if (cantRead) {
                            break;
                        }
                    } catch (Exception e) {
                    }
                    //row.add(value);
                }
                if (okColumns.size()>1) {
                    //data.add(row);
                    System.out.println("Adding ok columns: " + okColumns);
                    results.put(rowName, okColumns);
                }
            }
            for (String rName : results.keySet()) {
                List<String> columns = results.get(rName);
                List<String> rows = subsets.get(columns);
                if (rows != null && !rows.contains(rName)) {
                    rows.add(rName);
                }else{
                    rows = new ArrayList<>();
                    rows.add(rName);
                }
                subsets.put(columns, rows);
            }

            Vector<String> columnHeaders = new Vector<String>();
            for(List<String> sub : subsets.keySet()){
                for(String s : sub){
                    String cName = sysCaller.getNameOfEntityWithIdAndType(s, PM_NODE.OATTR.value);
                    if(!columnHeaders.contains(cName) && reqColumns.contains(cName)){
                        columnHeaders.add(cName);
                    }
                }
            }
            ((DefaultTableModel) model).setColumnIdentifiers(columnHeaders);
            setTableColumnSize();

            List<List<String>> subsetKeys = new ArrayList<>();
            Iterator<List<String>> iter = subsets.keySet().iterator();
            while(iter.hasNext()){
                subsetKeys.add(iter.next());
            }
            for(int i = 0; i < subsetKeys.size(); i++){
                List<String> sub = subsetKeys.get(i);
                List<String> subColumns = new ArrayList<>(columnHeaders);
                List<String> subNames = new ArrayList<String>();
                for(String s : sub){
                    subNames.add(sysCaller.getNameOfEntityWithIdAndType(s, PM_NODE.OATTR.value));
                }
                List<String> subRows = subsets.get(sub);

                for(int j = 0; j < subColumns.size(); j++){
                    if(!subNames.contains(subColumns.get(j))){
                        subColumns.set(j, "null");
                    }
                }

                subsetKeys.remove(i);
                i--;
                subsets.remove(sub);
                subsets.put(subColumns, subRows);
            }

            String union = "";
            for (List<String> s : subsets.keySet()) {
                String subsetCols = "";
                for (String col : s) {
                    subsetCols += col + ", ";
                }
                subsetCols = subsetCols.substring(0, subsetCols.length() - 2);
                List<String> rows = subsets.get(s);
                String in = "( ";
                for (String id : rows) {
                    in += "'" + id + "', ";
                }
                in = in.substring(0, in.length() - 2) + " ) ";

                String newSQL = "(SELECT " + subsetCols + " FROM " + tableName + " WHERE " + key + " IN " + in;
                union += newSQL + ") UNION ";
            }

            union = union.substring(0, union.length()-7);

            Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            ResultSet rs = stmt.executeQuery(union);
            ResultSetMetaData meta = rs.getMetaData();
            int numCols = meta.getColumnCount();
            //textArea.append("\n");
            while (rs.next()) {
                Row r = new Row();
                String rId = rs.getString(1);
                r.setRowId(rId);

                Vector<Object> rowData = new Vector<>();
                for (int i = 0; i < numCols; i++) {
                    String value = rs.getString(i + 1);
                    rowData.add(value);
                }
                r.setData(rowData);
                t.addRow(r);
                if(rowData.size() > 1){
                    addRow(rowData);
                }
            }

            Long end1 = System.nanoTime();
            System.out.println("TOTAL TIME: " + ((end1 - start) / 1000000000.0f));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void updateTableJosh(String tableName) {
        long s = System.nanoTime();
        List<String> allColumnNames = getTableColumns(tableName);//if null get all the columns of the table

        int rowCount = 0;
        int curRowCount = table.getRowCount();
        int end = numRows;

        Hashtable<String, List<String>> results = new Hashtable<>();
        Hashtable<List<String>, List<String>> subsets = new Hashtable<>();

        for (int x = curRowCount; x < end; x++) {
            String rowName = rows.get(x);
            String rowId = sysCaller.getIdOfEntityWithNameAndType(rowName, PM_NODE.OATTR.value);

            Packet pack = util.genCmd("getMellMembersOf", rowName, rowId, "b", "ac");
            if(pack.isEmpty()){
                continue;
            }

            List<String> pos = new ArrayList<>();
            for (int i = 0; i < pack.size(); i++) {
                pos.add(pack.getStringValue(i).split(PM_FIELD_DELIM)[1]);
            }

            Vector<Object> row = new Vector<>();
            row.add(rowName);
            for (int i = 1; i < columns.size(); i++) {
                String column = columns.get(i);
                String columnId = sysCaller.getIdOfEntityWithNameAndType(column, PM_NODE.OATTR.value);
                String inters = util.genCmd("getIntersection", columnId, rowId).getStringValue(0);

                String sql = (String) sqlList.getSelectedItem();//sqlField.getText();
                String where = "";
                try {
                    where = sql.substring(sql.indexOf(" where "));
                } catch (Exception e) {
                }

                String value = "";
                Collections.sort(pos);
                if (Collections.binarySearch(pos, inters) >= 0) {
                    value = (String) t.getRow(rowName).getCell(i - 1);
                } else {
                    //check if inaccessible column is in where clause and columns
                    if (where.contains(columns.get(i))) {
                        row.clear();
                        break;
                    }
                }

                try {
                    //check if inaccessible column is in where clause and not in columns
                    String[] wherePieces = where.split(" |=");
                    boolean cantRead = false;
                    for (String p : wherePieces) {
                        if (allColumnNames.contains(p.trim())) {
                            column = p;
                            columnId = sysCaller.getIdOfEntityWithNameAndType(column, PM_NODE.OATTR.value);
                            inters = util.genCmd("getIntersection", columnId, rowId).getStringValue(0);
                            if (Collections.binarySearch(pos, inters) < 0) {
                                row.clear();
                                cantRead = true;
                                break;
                            }
                        }
                    }
                    if (cantRead) {
                        break;
                    }
                }catch(Exception e){}
                row.add(value);
            }
            if (row.size()>1) {
                //data.add(row);
                System.out.println("Adding row: " + row);
                if (rowCount < numRows) {
                    addRow(row);
                    rowCount++;
                }
            }else{
                end++;
            }
            if(x==rows.size()-1){
                break;
            }
        }
        Long e = System.nanoTime();
        System.out.println("TOTAL TIME: " + ((e - s) / 1000000000.0f));
    }

    private void addRow(Vector<Object> row){
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                ((DefaultTableModel) model).addRow(row);
                return null;
            }
        };
        worker.execute();
    }


    private Table t;
    private List<String> rows = new ArrayList<>();
    private List<String> columns = new ArrayList<>();

    private List<String> getTableColumns(String tableName){
        List<String> columns = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement("show full tables where Table_Type = 'BASE TABLE'");
            ResultSet rs = ps.executeQuery();
            String key = "";
            if (rs != null) {
                while (rs.next()) {
                    //textArea.append("tableName: " + tableName);
                    PreparedStatement ps2 = conn.prepareStatement("SELECT column_name from information_schema.columns\n" +
                            "where table_schema=DATABASE()\n" +
                            "and table_name = '" + tableName + "'");
                    ResultSet rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        columns.add(rs2.getString(1));
                    }
                }
            }
        }catch(Exception e){
        }
        return columns;
    }

    /**
     * Need to be able to write to the columns and read anything in the where class
     * @param update
     */
    private void update(Update update){
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    columns.clear();
                    data.clear();
                    numRows = DEFAULT_NUM_ROWS;
                    ((DefaultTableModel) model).setRowCount(0);

                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate("use " + pcName);

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
                        return null;
                    } else {
                        while (rs2.next()) {
                            keys.add(rs2.getString(1));
                        }
                    }

                    String selectCols = "";
                    for(int i = 0; i < keys.size(); i++){
                        if(i == 0){
                            selectCols += keys.get(i);
                        }else{
                            selectCols += ", " + keys.get(i);
                        }
                    }

                    for(Column c : update.getColumns()){
                        selectCols += ", " + c.getColumnName();
                    }

                    String select = "select " + selectCols + " from " + tableName + " " + update.getWhere().toString();
                    System.out.println(select);


                    /*Statement sqlStmt = conn.createStatement();
                    ResultSet rs = sqlStmt.executeQuery(select);
                    ResultSetMetaData meta = rs.getMetaData();
                    int numCols = meta.getColumnCount();
                    List<String> rowNames = new ArrayList<>();
                    while (rs.next()) {
                        String name = (String) rs.getObject(1);
                        for (int i = 2; i <= numCols; i++) {
                            if (rs.getObject(i) != null) {
                                name += rs.getObject(i);
                            }
                        }
                        rowNames.add(name);
                    }*/

                    Table t = new Table();
                    t.setTable(tableName);
                    //t.setKeys(key);
                    //t.setColumns(finalCols.split(","));


                    stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    stmt.setFetchSize(Integer.MIN_VALUE);
                    ResultSet rs = stmt.executeQuery(select);
                    ResultSetMetaData meta = rs.getMetaData();
                    int numCols = meta.getColumnCount();
                    List<String> rowIds = new ArrayList<>();
                    while (rs.next()) {
                        Row row = new Row();
                        String rowId = rs.getString(1);
                        rowIds.add(rowId);
                        row.setRowId(rowId);

                        Vector<Object> rowData = new Vector<>();
                        for (int i = 1; i < numCols; i++) {
                            String value = rs.getString(i + 1);
                            rowData.add(value);
                        }
                        row.setData(rowData);
                        t.addRow(row);
                    }
                    for(int i = 1; i <= numCols; i++){
                        columns.add(meta.getColumnName(i));
                    }
                    rows = rowIds;
                    TableEditor.this.t = t;


                    //now we have the rows that are going to be updated







                    updateTable(tableName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        worker.execute();
    }

    private void select(String sql){
        SwingWorker<String, String> worker = new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    columns.clear();
                    data.clear();
                    numRows = DEFAULT_NUM_ROWS;
                    ((DefaultTableModel) model).setRowCount(0);

                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate("use " + pcName);

                    PreparedStatement ps = conn.prepareStatement("show full tables where Table_Type = 'BASE TABLE'");
                    ResultSet rs = ps.executeQuery();
                    key = "";
                    if (rs != null) {
                        while (rs.next()) {
                            String tableName = rs.getString(1);
                            //textArea.append("tableName: " + tableName);
                            PreparedStatement ps2 = conn.prepareStatement("SELECT k.COLUMN_NAME\n" +
                                    "FROM information_schema.table_constraints t\n" +
                                    "LEFT JOIN information_schema.key_column_usage k\n" +
                                    "USING(constraint_name,table_schema,table_name)\n" +
                                    "WHERE t.constraint_type='PRIMARY KEY'\n" +
                                    "    AND t.table_schema=DATABASE()\n" +
                                    "    AND t.table_name='" + tableName + "'\n" +
                                    "    AND k.COLUMN_NAME like '%id%'");
                            ResultSet rs2 = ps2.executeQuery();
                            if (rs2 == null) {
                                JOptionPane.showMessageDialog(null, "Error", "Table does not have a Primary Key.\nThis tool requires the table to have a Primary Key.", JOptionPane.ERROR_MESSAGE);
                                return null;
                            } else {
                                while (rs2.next()) {
                                    key = rs2.getString(1);
                                }
                            }
                        }
                    }

                    String userId = util.getUserId().split(PM_FIELD_DELIM)[1];
                    String[] pieces = sql.split("from |FROM |select |SELECT |where |WHERE |limit |LIMIT ");
                    String cols = pieces[1];

                    tableName = pieces[2].trim();

                    String[] colPieces = cols.split("\\, |\\,");
                    String finalCols = "";
                    for (String s : colPieces) {
                        if (s.equals(key)) {
                            finalCols = key + "," + finalCols;
                            continue;
                        }
                        finalCols += s + ",";
                    }

                    if (finalCols.contains(key)) {
                        finalCols = finalCols.substring(0, finalCols.lastIndexOf(','));
                    } else {
                        finalCols = key + "," + finalCols.substring(0, finalCols.lastIndexOf(','));
                    }

                    reqColumns = new ArrayList<>(Arrays.asList(cols.split("\\, |\\,")));
                    for(int i = 0; i < reqColumns.size(); i++){
                        reqColumns.set(i, reqColumns.get(i).trim());
                    }

                    String finalSql = "select " + finalCols + " from " + sql.substring(sql.indexOf(tableName));

                    Table t = new Table();
                    t.setTable(tableName);
                    //t.setKeys(key);
                    //t.setColumns(finalCols.split(","));


                    stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    stmt.setFetchSize(Integer.MIN_VALUE);
                    rs = stmt.executeQuery(finalSql);
                    ResultSetMetaData meta = rs.getMetaData();
                    int numCols = meta.getColumnCount();
                    List<String> rowIds = new ArrayList<>();
                    while (rs.next()) {
                        Row row = new Row();
                        String rowId = rs.getString(1);
                        rowIds.add(rowId);
                        row.setRowId(rowId);

                        Vector<Object> rowData = new Vector<>();
                        for (int i = 1; i < numCols; i++) {
                            String value = rs.getString(i + 1);
                            rowData.add(value);
                        }
                        row.setData(rowData);
                        t.addRow(row);
                    }
                    for(int i = 1; i <= numCols; i++){
                        columns.add(meta.getColumnName(i));
                    }
                    rows = rowIds;
                    TableEditor.this.t = t;
                    updateTable(tableName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        worker.execute();
    }

    private void editField(){
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if(col == 0){
            JOptionPane.showMessageDialog(this, "Sorry you can't edit this field.");
            return;
        }
        String recName = ((Vector<String>) ((DefaultTableModel) model).getDataVector().elementAt(row)).elementAt(0);
        Packet recInfo = sysCaller.getRecordInfo(sysCaller.getIdOfEntityWithNameAndType(recName, PM_NODE.OATTR.value));

        String sLine = recInfo.getStringValue(2);
        int nComp = Integer.valueOf(sLine);

        List<String> compNameList = new ArrayList<String>();
        for (int j = 0; j < nComp; j++) {
            sLine = recInfo.getStringValue(3 + j);
            String[] pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
            compNameList.add(pieces[0]);
        }

        String sObjName = compNameList.get(col-1);

        String sValue = ((Vector<String>) ((DefaultTableModel) model).getDataVector().elementAt(row)).elementAt(col);
        String sHandle = sysCaller.openObject3(sObjName, "File write");
        if (sHandle == null) {
            JOptionPane.showMessageDialog(TableEditor.this, sysCaller.getLastError());
            return;
        }

        TableColumn column = table.getColumnModel().getColumn(col);
        String colName = (String) column.getHeaderValue();
        //log("colName: " + colName);

        String newValue = JOptionPane.showInputDialog(colName, sValue);
        //log("newValue: " + newValue);
        if(newValue == null){
            return;
        }

        Properties props = new Properties();
        props.put(colName, newValue);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos, true);

        try {
            props.store(baos, null);
            pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}");
            pw.print(colName + ": " + newValue);
            pw.close();
            byte[] buf = baos.toByteArray();
            if (buf == null) {
                JOptionPane.showMessageDialog(TableEditor.this, "NO");
                return;
            }
            int len = sysCaller.writeObject3(sHandle, buf);
            if (len < 0) {
                JOptionPane.showMessageDialog(TableEditor.this, sysCaller.getLastError(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sysCaller.closeObject(sHandle);
            //log("WTF:: " + props.getProperty(colName));
            model.setValueAt(newValue, row, col);
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    private String name;
    private String pcId;
    String retUserLogon(){
        return name;
    }

    private String getUserFullName(String sId){
        try{
            Packet cmd = util.makeCmd("getUserFullNamePacket", sId);
            Packet res = engineClient.sendReceive(cmd, null);
            if (res == null) {
                JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
                //exit = true;
                return null;
            }
            if (res.hasError()) {
                JOptionPane.showMessageDialog(this, res.getErrorMessage());
                //exit = true;
                return null;
            }
            String name = res.getStringValue(0);
            log(name);
            return name;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void log(Object input){
        System.out.println(input);
    }

    /**
     * @param nCode
     */
    private void terminate(int nCode) {
        sysCaller.exitProcess(sProcessId);
        dispose();//System.exit(nCode);
    }

    public void addEntry(){
        RecordEditor editor = new RecordEditor(this, util, sysCaller);
        scrollPane_2.setViewportView(editor);
        //editor.setVisible(true);
        //editor.submit();

        //TODO update table to add the new record which will be empty
    }

    public void search(String column, String value){//FIXME
        System.out.println(column + "------" + value);
        if (value.length() == 0) {
            sorter.setRowFilter(null);
        } else if(column.length() == 0){
            sorter.setRowFilter(
                    RowFilter.regexFilter(value));
        }else{
            try {
                sorter.setRowFilter(
                        RowFilter.regexFilter(value, table.getColumnModel().getColumnIndex(column)+1));
            } catch (PatternSyntaxException pse) {
                System.err.println("Bad regex pattern");
            }
        }
    }

    private boolean requestPolicyClass(){
        ArrayList<String> tplList = new ArrayList<String>();
        ArrayList<String> tplNames = new ArrayList<String>();

        JComboBox tableBox = new JComboBox();
        tableBox.setPreferredSize(new Dimension(160, 20));

        ArrayList<String> pcs = util.getPolicyClasses();
        if (pcs == null) {
            JOptionPane.showMessageDialog(this,
                    "Could not get schemas");
            return false;
        }

        DefaultComboBoxModel tableBoxModel = (DefaultComboBoxModel)tableBox.getModel();
        tableBoxModel.removeAllElements();
        for (int i = 0; i < pcs.size(); i++) {
            tableBoxModel.addElement(pcs.get(i).split(GlobalConstants.PM_FIELD_DELIM)[0]);
        }


        int ret = JOptionPane.showOptionDialog(this, new Object[]
                        {"Select a table to open.", tableBox}, "Select a Table",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, null, null);
        if(ret != JOptionPane.OK_OPTION){
            return false;
        }

        int sel = tableBox.getSelectedIndex();
        if(sel < 0){
            JOptionPane.showMessageDialog(this, "Please select a table to open");
        }

        pcName = ((String) tableBoxModel.getElementAt(sel));
        pcId = sysCaller.getIdOfEntityWithNameAndType(pcName, PM_NODE.POL.value);

        Packet storageProp = util.genCmd("getNodePropertyValue", pcId, "storage");
        String prop = storageProp.getStringValue(0);
        if(prop.equalsIgnoreCase("database")){
            //prompt for database connection
            int result = JOptionPane.showConfirmDialog(null, getDbConnection(), "Connect to " + pcName, JOptionPane.OK_CANCEL_OPTION);
            if(result == JOptionPane.CANCEL_OPTION){
                return false;
            }else{
                String host = hostField.getText();
                String port = portField.getText();
                String user = userField.getText();
                String pass = passField.getText();
                String dbName = pcName;//dbNameField.getText();

                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + dbName, user, pass);
                    if (conn != null) {
                        conn.setAutoCommit(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "Could not establish database connection...");
                        return false;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            //splitPane.setLeftComponent(null);
        }

        Packet temps = util.getTemplates();
        for(int i = 0 ; i < temps.size(); i++){
            String temp = temps.getStringValue(i);
            String pieces[] = temp.split(GlobalConstants.PM_FIELD_DELIM);
            String tempId = pieces[1];
            String tempName = pieces[0];
            Packet tempInfo = util.getTemplateInfo(pieces[1]);
            List<String> conts = new ArrayList<String>();
            List<String> keys = new ArrayList<String>();
            String contIds = tempInfo.getStringValue(1);
            pieces = contIds.split(GlobalConstants.PM_FIELD_DELIM);
            for(String s : pieces){
                conts.add(sysCaller.getNameOfEntityWithIdAndType(s, PM_NODE.OATTR.value));
            }
            String tempKeys = tempInfo.getStringValue(2);
            pieces = tempKeys.split(GlobalConstants.PM_FIELD_DELIM);
            for(String s : pieces){
                keys.add(sysCaller.getIdOfEntityWithNameAndType(s, PM_NODE.OATTR.value));
            }

            Template t = new Template(tempName, tempId, conts, keys);
            templates.add(t);
        }
        return true;
    }

    private Connection conn;
    private JPanel getDbConnection(){
        db = true;

        JPanel connectPane = new JPanel();

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


        return connectPane;
    }

    public Template getActiveTemplate(){
        return activeTemplate;
    }

    private void reset(){
        setVisible(false);
        new TableEditor(sSessionId, nSimulatorPort, sProcessId, false);
    }

    public boolean openSchema(boolean requestTemplate){
        activeTemplate = null;
        containerList.clear();
        data.clear();
        records.clear();

        if(requestTemplate){
            if(!requestPolicyClass()){
                return false;
            }
        }
        return true;
    }

    private void refreshRecord(String recName){

    }

    private ArrayList<TERecord> records = new ArrayList<TERecord>();

    private void loadModelData(){
        ArrayList<String> reqRecs = new ArrayList<String>();
        Packet recs = sysCaller.getRecords(tempId, "");
        for(int i = 0; i < recs.size(); i++){
            try{
                String line = recs.getItemStringValue(i);
                log("REC: " + line);
                String[] pieces = line.split(":");
                String recName = pieces[0];
                reqRecs.add(recName);
            }catch(Exception e){

            }
        }
        System.out.println(reqRecs);
        openTableRecords(null);
    }

    private void setTableColumnSize(){
        //table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));
        for(int i = 0; i < table.getColumnModel().getColumnCount(); i++)
        {
            table.getColumnModel().getColumn(i).setPreferredWidth(((String) table.getColumnModel().getColumn(i).getHeaderValue()).length() + 200);
        }
    }

    private JProgressBar progressBar;
    private JDialog jd;
    private SwingWorker<String, String> recordWorker;
    private boolean firstTime = true;
    private HashMap<String, Vector<String>> dataCache = new HashMap<String, Vector<String>>();
    private void getRequestedRecords(final int start, final int end, final List<String> req){
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        jd = new JDialog();
        jd.setTitle("Loading Table...");
        Dimension d = progressBar.getPreferredSize();
        d.width = 500;
        progressBar.setPreferredSize(d);
        jd.add(progressBar);
        jd.pack();
        jd.setLocationRelativeTo(this);
        jd.setVisible(true);

        recordWorker = new SwingWorker<String, String>() {
            private List<String> columns;
            @Override
            protected String doInBackground() throws Exception {
                data.clear();
                records.clear();
                System.out.println("requested records: " + req);
                Packet recs = sysCaller.getRecords(activeTemplate.getTplId(), "");
                progressBar.setMaximum(recs.size());
                for(int i = 0; i < recs.size(); i++){
                    Long s = System.currentTimeMillis();
                    try {
                        String line = recs.getItemStringValue(i);
                        log("REC: " + line);
                        String[] pieces = line.split(":");
                        String recId = pieces[1];
                        String recName = pieces[0];
                        System.out.println("Record " + recName + "not changed, skipping");

                        if (!req.contains(recName)) {
                            System.out.println("req does not contain " + recName);
                            continue;
                        }
                        Packet recInfo = sysCaller.getRecordInfo(recId);


                        String sLine = recInfo.getStringValue(2);
                        int nComp = Integer.valueOf(sLine);
                        log("  It has " + nComp + " components:");

                        for (int j = 0; j < recInfo.size(); j++) {
                            sLine = recInfo.getStringValue(3 + j);
                            System.out.println((3 + j) + ":" + sLine);
                        }

                        List<String> compNameList = new ArrayList<String>();
                        for (int j = 0; j < nComp; j++) {
                            sLine = recInfo.getStringValue(3 + j);
                            System.out.println("sLine in getRecords():: ");
                            System.out.println(sLine);
                            pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
                            compNameList.add(pieces[0]);
                        }

                        // The keys.
                        sLine = recInfo.getStringValue(3 + nComp);
                        int nKeys = Integer.valueOf(sLine);
                        log("  It has " + nKeys + " keys:");

                        List<String[]> keys = new ArrayList<String[]>();
                        for (int j = 0; j < nKeys; j++) {
                            sLine = recInfo.getStringValue(4 + nComp + j);
                            log("    " + sLine);
                            pieces = sLine.split("=");
                            keys.add(new String[]{pieces[0], pieces[1]});
                        }

                        Vector<String> recordData = new Vector<String>();
                        //if(recordChanged(recName, recId) || firstTime) {
                        recordData.add(recName);

                        System.out.println("compNameList: " + compNameList);

                        String preview = "";
                        System.out.println(compNameList.size());
                        columns = activeTemplate.getConts();
                        Collections.sort(columns);
                        Collections.sort(compNameList);
                        for (int j = 0; j < compNameList.size(); j++) {
                            System.out.println(compNameList.get(j));
                            List<String[]> members = sysCaller.getMembersOf(compNameList.get(j),
                                    sysCaller.getIdOfEntityWithNameAndType(compNameList.get(j), PM_NODE.OATTR.value), PM_NODE.OATTR.value, SysCaller.PM_VOS_PRES_USER);
                            List<String> comps = new ArrayList<String>();
                            for (String[] mem : members) {
                                System.out.println(mem[0] + ":" + mem[1] + ":" + mem[2]);
                                comps.add(mem[2]);
                            }
                            System.out.println("*<>*" + columns);
                            System.out.println(compNameList);
                            System.out.println(comps);
                            System.out.println("Members size: " + members.size());

                            //if file has not been modified get from memory
                            String value = openObject(members.get(0)[2], columns.get(j));
                            System.out.println("Value: " + value);
                            preview = value;
                            if (members.size() == 1) {
                                // only one child
                                recordData.add(preview);
                                System.out.println("1 : " + preview);
                            } else if (members.size() > 1) {
                                preview = value + "...";
                                recordData.add(preview);
                                System.out.println("2: " + preview);
                            }
                        }
                            /*dataCache.put(recName, recordData);
                        }else{
                            recordData = dataCache.get(recName);
                        }*/
                        System.out.println("before row data");
                        /*Vector<String> rowData = new Vector<String>();
                        for(int j = start; j <= end; j++){
                            System.out.println("j: " + recordData.get(j));
                            rowData.add(recordData.get(j));
                        }
                        */
                        System.out.println("rowData: " + recordData);
                        data.add(recordData);
                        records.add(new TERecord(recName, recId, tempName, tempId, compNameList, keys));
                    } catch (PacketException e) {
                        e.printStackTrace();
                    }
                    Long e = System.currentTimeMillis();
                    System.out.println("Time for one record: " + (double)(e-s)/1000);
                    setProgress(i+1);
                }
                firstTime = false;
                return null;
            }

            @Override
            protected void done(){
                progressBar.setValue(progressBar.getMaximum());
                jd.setVisible(false);
                recordWorker = null;
                setTableModel(columns);
            }
        };
        recordWorker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress" == evt.getPropertyName()) {
                    int progress = (Integer) evt.getNewValue();
                    progressBar.setValue(progress);
                    if (recordWorker.getState().equals(SwingWorker.StateValue.DONE)) ;
                }
            }
        });
        recordWorker.execute();
    }

    //true = record was changed we have to update the table
    //false = record was not changed, skip it
    private boolean recordChanged(final String recName, final String recId){
        final boolean[] changed = {false};
        Thread t = new Thread(){
            @Override
            public void run(){
                List<String[]> members = sysCaller.getMembersOf(recName, recId, PM_NODE.OATTR.value, PmGraphType.OBJECT_ATTRIBUTES.typeCode());
                List<String> objNames = new ArrayList<String>();
                for(String[] mem : members){
                    objNames.add(mem[2]);
                }
                /*WatchKey key = null;
                try {
                    key = watcher.take();
                    while(key != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();

                            @SuppressWarnings("unchecked")
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path fileName = ev.context();

                            System.out.println(kind.name() + ": " + fileName);

                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                for (String s : objNames) {
                                    if (fileName.toString().contains(s)) {
                                        System.out.println("file " + s + " in record " + recName + " was changed");
                                        changed[0] = true;
                                        return;
                                    }
                                }
                            }
                            key.reset();
                            key = watcher.take();
                        }
                    }
                } catch (InterruptedException ex) {
                    changed[0] = true;
                    return;
                }*/
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return true;
        }
        return changed[0];
    }

    private String openObject(String sObjName, String cont){
        System.out.println(sObjName + "->" + cont);
        String sHandle = sysCaller.openObject3(sObjName, "File read");
        if (sHandle == null) {
            //JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            System.out.println(sysCaller.getLastError());
            System.out.println("TE.OO could not open Object");
            return "";
        }

        // Reserve space for and read the object content.
        byte[] buf = sysCaller.readObject3(sHandle);
        if (buf == null) {
            System.out.println(sysCaller.getLastError());
            //JOptionPane.showMessageDialog(this, sysCaller.getLastError());
            System.out.println("TE.OO could not read Object");
            return "";
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        Properties props = new Properties();
        try {
            props.load(bais);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Exception when loading patient's id data");
            return "";
        }

        for (Enumeration propEnum = props.propertyNames(); propEnum.hasMoreElements();) {
            String sName = (String) propEnum.nextElement();
            System.out.println(sName + "=" + (String) props.get(sName));
        }
        return props.getProperty(cont);
    }

    public void populateTable(Vector<Vector<String>> data){
        log("DATA: " + data);
        for(int i = 0; i < data.size(); i++){
            log("  ROW: " + data.get(i));
            if(!isEmpty(data.get(i))){
                ((DefaultTableModel) model).addRow(data.get(i));
            }
        }
    }

    private boolean isEmpty(Vector<String> row){
        for(int i = 0; i < row.size(); i++){
            if(!row.get(i).equals("")){
                return false;
            }
        }
        return true;
    }

    private void resetSchemaView(){
        PmNodeChildDelegate childDelegate = new PmNodeChildDelegate(util.sslClient, sSessionId,
                PmGraphDirection.UP_MELL,
                PmGraphType.USER_MELL_ATTRIBUTES);
        schemaNode = new PmNode(
                PM_NODE.OATTR.value,
                sysCaller.getIdOfEntityWithNameAndType(pcName, PM_NODE.OATTR.value),
                pcName,
                childDelegate);
        tree = new PmGraph(schemaNode, false);
        final PmNode actualSchemaNode = new PmNode(
                PM_NODE.OATTR.value,
                pcId,
                pcName,
                new PmNodeChildDelegate(util.sslClient, sSessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
        PmNode root = PmNode.createObjectAttributeNode("root");
        PmNode.linkNodes(root, new PmNode[]{schemaNode});//, otherNode});
        tree = new PmGraph(root, false);
        tree.setToolTipText("Right-click to open a record, container, or table");
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.addMouseListener(new SchemaMouseListener());

        scrollPane.setViewportView(tree);
    }

    private List<String> getCompsForRecord(String recName) {
        List<String> compNameList = new ArrayList<String>();
        try{
            String recId = sysCaller.getIdOfEntityWithNameAndType(recName, PM_NODE.OATTR.value);
            Packet recInfo = sysCaller.getRecordInfo(recId);

            String sLine = recInfo.getStringValue(2);
            int nComp = Integer.valueOf(sLine).intValue();
            log("  It has " + nComp + " components:");

            for (int j = 0; j < nComp; j++) {
                sLine = recInfo.getStringValue(3 + j);
                System.out.println("sLine in getRecords()");
                System.out.println(sLine);
                String[] pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
                compNameList.add(pieces[0]);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return compNameList;
    }

    private ArrayList<String> getChildRecords(PmNode oaNode){
        ArrayList<String> retList = new ArrayList<String>();
        List<PmNode> nodes = getAllNodes(oaNode);
        System.out.println(nodes);
        for(PmNode n : nodes){
            String name = n.getName();
            System.out.println("Name: " + name + " Type: " + n.getNodeType());
            //System.out.println(model.isLeaf(n));
            //if(model.isLeaf(n) && n.getNodeType().equals(PmNodeType.OBJECT_ATTRIBUTE)){
            ArrayList<PmNode> children = getAllNodes(n);
            for(int i = 0; i < children.size(); i++){
                System.out.print(children.get(i).getName() + ",");
            }
            System.out.println();
            if(children == null || children.isEmpty()){
                System.out.println("adding " + name);
                retList.add(name);
            }
        }
        return retList;
    }

    private ArrayList<PmNode> getAllNodes(PmNode base){
        ArrayList<PmNode> ret = new ArrayList<PmNode>();
        List<PmNode> children = base.getChildren();
        Enumeration<PmNode> childEnum= Collections.enumeration(children);
        while(childEnum.hasMoreElements()){
            PmNode n = childEnum.nextElement();
            if(util.isRecord(n.getId()))ret.add(n);
            ret.addAll(getAllNodes(n));
        }
        return ret;
    }

    private void setTableModel(List<String> columns){
        ((DefaultTableModel) model).setRowCount(0);
        if(!columns.contains("Record Name"))columns.add(0, "Record Name");
        System.out.println("columns: " + columns);
        Collections.sort(columns);
        Vector<String> vColumns = new Vector<String>();
        for(String c : columns){
            vColumns.add(c);
        }
        ((DefaultTableModel) model).setColumnIdentifiers(vColumns);
        setTableColumnSize();
        populateTable(data);
    }

    private Vector<String> getSchemaLeafs(PmNode selNode){
        System.out.println(selNode.getName());
        List<String> columns = activeTemplate.getConts();
        Vector<String> leafs = new Vector<String>();
        if(columns.contains(selNode.getName()))
            leafs.add(selNode.getName());

        List<PmNode> children = selNode.getChildren();
        System.out.println("children : " + children);
        Iterator<PmNode> it = children.iterator();
        while(it.hasNext()){
            PmNode child = it.next();
            String name = child.getName();
            System.out.println("name: " + name);
            if(columns.contains(child.getName())){
                leafs.add(name);
            }else if(!tree.getModel().isLeaf(child)){
                leafs.addAll(getSchemaLeafs(child));
            }
            if(it.hasNext()){
                leafs.addAll(getSchemaLeafs(it.next()));
            }
        }
        return leafs;
    }

    private void openTableRecords(PmNode selNode){
        Packet records = sysCaller.getRecords(activeTemplate.getTplId(), null);
        List<String> reqRecs = new ArrayList<String>();
        for(int i = 0; i < records.size(); i++){
            String rec = records.getStringValue(i);
            String[] pieces = rec.split(GlobalConstants.PM_FIELD_DELIM);
            reqRecs.add(pieces[0]);
        }
        Vector<String> columns = new Vector<String>();
        List<String> tempConts = activeTemplate.getConts();
        for(String s : tempConts){
            columns.add(s);
        }
        getRequestedRecords(0, columns.size(), reqRecs);
        //TODO this method should be in the done method, but take getRequestedRecords from it and call it above ^
        //setTableModel(0, columns.size(), reqRecs, columns);
    }

    private PmNode lastSelNode;
    public PmNode getLasSelNode(){
        return lastSelNode;
    }
    public void refreshModel(PmNode selNode){
        schemaNode = new PmNode(
                PM_NODE.POL.value,
                pcId,
                pcName,
                new PmNodeChildDelegate(util.sslClient, sSessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
        final PmNode actualSchemaNode = new PmNode(
                PM_NODE.OATTR.value,
                pcId,
                pcName,
                new PmNodeChildDelegate(util.sslClient, sSessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));

        tree.expandPath(new TreePath(selNode.getPath()));

        //String selNodeClass = util.getNodeClass(selNode.getId());
        //System.out.println("selected node class: " + selNodeClass);

        List<String> reqRecs = new ArrayList<String>();

        System.out.print("Path:");
        PmNode[] selPath = selNode.getPath();
        ArrayList<String> path = new ArrayList<String>();
        for(int i = 0; i < selPath.length; i++){
            System.out.print(selPath[i].getName() + "->");
            path.add(selPath[i].getId());
        }
        System.out.println("end path");

        System.out.println((selNode.getChildCount() > 0) + " " +  (selNode != schemaNode)  + " " +   /*(selNode != otherNode)  +*/ " " +   (!path.contains(actualSchemaNode.getId())));
        System.out.println(util.isRecord(selNode.getId()) + " " + selNode.getId());
        if(util.isRecord(selNode.getId())){
            openRecordEditor(selNode);
            return;
        }

        //find if user selected a template
        boolean found = false;
        for(Template t : templates){
            if(t.getTplName().equals(selNode.getName())){
                activeTemplate = t;
                found = true;
            }
        }
        if(!found)activeTemplate = null;

        System.out.println(activeTemplate);

        if(activeTemplate != null){
            //user selected a table with a template
            openTableRecords(selNode);
        }else{
            //user did not select a template
            if(selNode.getType().equals(PM_NODE.OATTR.value)){
                for(int j = 0; j < templates.size(); j++){
                    System.out.println("path: " + path);
                    System.out.println("temp id: " + templates.get(j).getTplId());
                    if(path.contains(templates.get(j).getTplId())){
                        System.out.println("selected node is in schema");
                        Vector<String> leafs = getSchemaLeafs(selNode);
                        System.out.println("LEAFS: " + leafs);
                        if(leafs.isEmpty()){
                            for(int i = 0; i < table.getColumnModel().getColumnCount(); i++){
                                table.getColumnModel().getColumn(i).setMinWidth(0);
                                table.getColumnModel().getColumn(i).setMaxWidth(((String) table.getColumnModel().getColumn(i).getHeaderValue()).length()+200);
                                table.getColumnModel().getColumn(i).setPreferredWidth(((String) table.getColumnModel().getColumn(i).getHeaderValue()).length()+200);
                            }
                            for(int i = 0; i < table.getColumnModel().getColumnCount(); i++){
                                String header = (String)table.getColumnModel().getColumn(i).getHeaderValue();
                                if(!header.equals(selNode.getName())){
                                    table.getColumnModel().getColumn(i).setMinWidth(0);
                                    table.getColumnModel().getColumn(i).setMaxWidth(0);
                                    table.getColumnModel().getColumn(i).setWidth(0);
                                }
                            }
                        }else{
                            for(int i = 0; i < table.getColumnModel().getColumnCount(); i++){
                                table.getColumnModel().getColumn(i).setMinWidth(0);
                                table.getColumnModel().getColumn(i).setMaxWidth(((String) table.getColumnModel().getColumn(i).getHeaderValue()).length()+200);
                                table.getColumnModel().getColumn(i).setPreferredWidth(((String) table.getColumnModel().getColumn(i).getHeaderValue()).length()+200);
                            }
                            for(int i = 0; i < table.getColumnModel().getColumnCount(); i++){
                                String header = (String)table.getColumnModel().getColumn(i).getHeaderValue();
                                if(!leafs.contains(header)){
                                    table.getColumnModel().getColumn(i).setMinWidth(0);
                                    table.getColumnModel().getColumn(i).setMaxWidth(0);//((String) table.getColumnModel().getColumn(i).getHeaderValue()).length()+200);
                                    table.getColumnModel().getColumn(i).setWidth(0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void openRecordEditor(PmNode selNode) {
        panel_4.setVisible(true);
        String recName = selNode.getName();
        List<String> comps = getCompsForRecord(recName);
        Collections.sort(comps);
        System.out.println("Opening records with comps: " + comps);
        RecordEditor editor = new RecordEditor(TableEditor.this, sysCaller, recName, null, comps, util);
        scrollPane_2.setViewportView(editor);
    }

    private TERecord getSelectedRecord(String name){
        System.out.println("searching for record " + name);
        for(TERecord rec : records){
            if(rec.getName().equals(name)){
                return rec;
            }
        }
        return null;
    }

    static String sessid;
    static String pid;
    static int simport;
    static boolean debug;
    public static void createGUI(){
        new TableEditor(sessid, simport, pid, debug);
    }


    public static void main(String[] args){
        //log("main called in schema 2");
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-session")) {
                sessid = args[++i];
            } else if (args[i].equals("-process")) {
                pid = args[++i];
            } else if (args[i].equals("-simport")) {
                simport = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-debug")) {
                debug = true;
            }
        }
        //log(sessid + " " + pid + " " + simport + " " + debug);
        if (sessid == null) {
            log("This application must run within a Policy Machine session!");
            System.exit(-1);
        }
        if (pid == null) {
            log("This application must run in a Policy Machine process!");
            System.exit(-1);
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                createGUI();
            }
        });
    }

    class Container {
        //baseName, col, colId, "b", des, other, prop
        String sBaseName;
        String sColName;
        String sColId;
        String sType;
        String sDescr;
        String sOther;
        String sProp;

        public String getsOther() {
            return sOther;
        }
        public void setsOther(String sOther) {
            this.sOther = sOther;
        }

        public void setsBaseName(String sBaseName) {
            this.sBaseName = sBaseName;
        }
        public void setsColName(String sColName) {
            this.sColName = sColName;
        }
        public void setsColId(String sColId) {
            this.sColId = sColId;
        }
        public void setsType(String sType) {
            this.sType = sType;
        }
        public void setsDescr(String sDescr) {
            this.sDescr = sDescr;
        }
        public void setsProp(String sProp) {
            this.sProp = sProp;
        }
        public String getsBaseName() {
            return sBaseName;
        }
        public String getsColName() {
            return sColName;
        }
        public String getsColId() {
            return sColId;
        }
        public String getsType() {
            return sType;
        }
        public String getsDescr() {
            return sDescr;
        }
        public String getsProp() {
            return sProp;
        }

        public String toString(){
            return "{" + getsBaseName() + ":" + getsColName() + ":" + getsColId() + ":"
                    + getsType() + ":" + getsDescr() + ":" + getsProp() + "}";
        }
    }

    class TERecord{
        String name;
        String id;
        String tempName;
        String tempId;
        List<String> comps;
        List<String[]> keys;

        public TERecord(String name, String id, String tempName, String tempId,
                        List<String> compNameList, List<String[]> keys2) {
            super();
            this.name = name;
            this.id = id;
            this.tempName = tempName;
            this.tempId = tempId;
            this.comps = compNameList;
            this.keys = keys2;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getTempName() {
            return tempName;
        }
        public void setTempName(String tempName) {
            this.tempName = tempName;
        }
        public String getTempId() {
            return tempId;
        }
        public void setTempId(String tempId) {
            this.tempId = tempId;
        }
        public List<String> getComps() {
            return comps;
        }
        public void setComps(ArrayList<String> comps) {
            this.comps = comps;
        }
        public List<String[]> getKeys() {
            return keys;
        }
        public void setKeys(ArrayList<String[]> keys) {
            this.keys = keys;
        }
        public String toString(){
            String ks = "{ ";
            for(String[] k : keys){
                ks += "[" + k[0] + ", " + k[1] + "], ";
            }
            ks += " }";
            return "**********START RECORD***********\n"
                    + "Name: " + getName()
                    + "\nID: " + getId()
                    + "\nTemplate Name: " + getTempName()
                    + "\nTemplate ID: " + getTempId()
                    + "\nComponents: " + getComps()
                    + "\nKeys: " + ks
                    + "\n***************END RECORD**************\n";
        }
    }

    class SchemaMouseListener implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            if(SwingUtilities.isRightMouseButton(e)){
                return;
            }
            System.out.println("IN SCHEMA MOUSE LISTENER");

            int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row);

            PmNode selNode = (PmNode)tree.getLastSelectedPathComponent();
            System.out.println("SelectedNode: " + selNode.getName());
            //refreshModel(selNode);
            lastSelNode = selNode;
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

    }
}


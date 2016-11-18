package gov.nist.csd.pm.application.schema.tableeditor;

import java.awt.BorderLayout;

import static gov.nist.csd.pm.common.util.Generators.generateRandomName;

import javax.swing.*;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import java.awt.FlowLayout;

import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.regex.PatternSyntaxException;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.PmGraph;
import gov.nist.csd.pm.common.browser.PmGraphDirection;
import gov.nist.csd.pm.common.browser.PmGraphType;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeChildDelegate;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketException;
import gov.nist.csd.pm.user.*;
//import java.nio.file.*;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;

/**
 * allow user to view multiple tables
 * @author Administrator
 *
 * TODO sorting rows
 * add function to clear search and to return to full table
 *
 */
public class TableEditor extends JFrame {

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
    //private WatchService watcher;

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
        /*try {
            watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get("C:/PMWorkArea");
            dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);


        } catch (IOException e) {
            e.printStackTrace();
        }*/

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
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JSplitPane splitPane = new JSplitPane();

        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Schema", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        splitPane.setLeftComponent(panel);
        //getContentPane().add(panel, BorderLayout.WEST);
        panel.setLayout(new BorderLayout(0, 0));


        scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton btnResetSchemaView = new JButton("Reset Schema View");
        btnResetSchemaView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetSchemaView();
            }
        });
        panel.add(btnResetSchemaView, BorderLayout.SOUTH);

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

        //JPopupMenu popmenu = new JPopupMenu();
		/*JMenuItem view = new JMenuItem("Edit Field");
		view.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				editField();
			}
		});
		popmenu.add(view);*/

        JMenuItem view = new JMenuItem("Open");
        view.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                editField();
            }
        });
        //popmenu.add(view);

        JPanel panel_5 = new JPanel();
        contentPane.add(panel_5, BorderLayout.CENTER);
        panel_5.setLayout(new BorderLayout(0, 0));

        JPanel panel_1 = new JPanel();
        panel_5.add(panel_1);
        panel_1.setBorder(
                new CompoundBorder(
                        new TitledBorder("Master View"),
                        BorderFactory.createBevelBorder(BevelBorder.LOWERED)
                )
        );
        panel_1.setLayout(new BorderLayout(0, 0));

        JPanel panel_2 = new JPanel();
        panel_1.add(panel_2, BorderLayout.SOUTH);

        JButton btnAddEntry = new JButton("Add Record");
        btnAddEntry.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(activeTemplate != null){
                    panel_4.setVisible(true);
                    addEntry();
                }else{
                    JOptionPane.showMessageDialog(TableEditor.this, "A table must be open in order to add a record");
                    return;
                }
            }
        });

        JButton btnRefreshTable = new JButton("Reset Table");
        btnRefreshTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                loadModelData();
            }
        });
        panel_2.add(btnRefreshTable);
        panel_2.add(btnAddEntry);

        JButton btnEditEntry = new JButton("Edit Record");
        btnEditEntry.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //editEntry();
            }
        });
        //panel_2.add(btnEditEntry);

        JButton btnDeleteEntry = new JButton("Delete Record");
        btnDeleteEntry.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {

            }

        });
        //panel_2.add(btnDeleteEntry);

        JScrollPane scrollPane_1 = new JScrollPane();
        panel_1.add(scrollPane_1, BorderLayout.CENTER);

        JPanel panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(null, "Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_1.add(panel_3, BorderLayout.NORTH);
        panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JLabel lblColumnName = new JLabel("Column Name:");
        panel_3.add(lblColumnName);

        searchColField = new JTextField();
        panel_3.add(searchColField);
        searchColField.setColumns(10);

        JLabel lblValue = new JLabel("Value:");
        panel_3.add(lblValue);

        searchValueField = new JTextField();
        panel_3.add(searchValueField);
        searchValueField.setColumns(10);

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(new ActionListener(){

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
        //table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                panel_4.setVisible(true);
                String recName = (String) model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
                List<String> comps = getCompsForRecord(recName);
                Collections.sort(comps);
                System.out.println("Opening records with comps: " + comps);
                RecordEditor editor = new RecordEditor(TableEditor.this, sysCaller,
                        recName, null, comps, util);
                scrollPane_2.setViewportView(editor);
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

        });
        scrollPane_1.setViewportView(table);
        //table.setComponentPopupMenu(popmenu);

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
        panel_4.setLayout(new BorderLayout(0, 0));

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

        //loadModelData();

        setLocationRelativeTo(null);
        setVisible(true);
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
        int nComp = Integer.valueOf(sLine).intValue();

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
                keys.add(s);
            }

            Template t = new Template(tempName, tempId, conts, keys);
            templates.add(t);
        }
        return true;
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
        schemaNode = new PmNode(
                PM_NODE.POL.value,
                pcId,
                pcName,
                new PmNodeChildDelegate(util.sslClient, sSessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
        tree = new PmGraph(schemaNode, false);
        tree.setToolTipText("Right-click to open a record, container, or table");
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.addMouseListener(new SchemaMouseListener());
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
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));
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
		/*schemaNode = new PmNode(
				PM_NODE.OATTR.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName, PM_NODE.OATTR.value), 
				schemaName, 
				new PmNodeChildDelegate(util.sslClient, sSessionId, PmGraphDirection.USER, PmGraphType.USER));
		 */schemaNode = new PmNode(
                PM_NODE.POL.value,
                pcId,
                pcName,
                new PmNodeChildDelegate(util.sslClient, sSessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
        final PmNode actualSchemaNode = new PmNode(
                PM_NODE.OATTR.value,
                pcId,
                pcName,
                new PmNodeChildDelegate(util.sslClient, sSessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
		 /*otherNode = new PmNode(
				PM_NODE.OATTR.value, 
				sysCaller.getIdOfEntityWithNameAndType(schemaName + " Containers", PM_NODE.OATTR.value), 
				schemaName + " Containers", 
				new PmNodeChildDelegate(util.sslClient, sSessionId, PmGraphDirection.USER, PmGraphType.USER));*/
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
        //log("main called in schemabuilder 2");
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
            if(!SwingUtilities.isRightMouseButton(e)){
                return;
            }
            System.out.println("IN SCHEMA MOUSE LISTENER");

            int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row);

            PmNode selNode = (PmNode)tree.getLastSelectedPathComponent();
            System.out.println("SelectedNode: " + selNode.getName());
            if (selNode != null)
            {
                Long s = System.currentTimeMillis();
                refreshModel(selNode);
                Long e1 = System.currentTimeMillis();
                System.out.println((double)(e1-s)/1000);
                lastSelNode = selNode;
            }
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


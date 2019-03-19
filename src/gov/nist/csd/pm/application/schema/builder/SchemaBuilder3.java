package gov.nist.csd.pm.application.schema.builder;

import gov.nist.csd.pm.application.schema.importing.ImportParser;
import gov.nist.csd.pm.application.schema.importing.Table;
import gov.nist.csd.pm.application.schema.tableeditor.Template;
import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.*;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.net.Packet;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;

/**
 * @author administrator
 * TODO multiple keys for multiple tables
 * TODO multiple table design - their product - order example
 * TODO EACH TABLE IS A RECORD WITH ITS OWN KEYS and own templategf
 * TODO schema name + admin uattr
 */
public class SchemaBuilder3 extends JFrame {

    private String schemaProp;
    public boolean modify = false;
    private boolean submitted = false;
    private boolean oattrEdit = false;
    private String selectedOattr;
    private ColumnPermEditor2 colPermEditor;
    private boolean edit;
    private String sessionId;
    private int nSimulatorPort;
    private String userName;
    public SysCaller sysCaller;
    private String sProcessId;
    private JPanel contentPane;
    private JSplitPane splitPane = new JSplitPane();
    private PmGraph tree;
    private JScrollPane scrollPane;
    private JTextField baseNameField;
    private JTextField oattrNameField;
    private JTextField oattrPropField;
    private JTextField oattrDescriptionField;
    private JButton btnSetPerms;
    private JButton btnCreateOattr;
    private JPanel panel_5;
    private JList list;
    private JLabel lblSchemaName;
    private JTextField schemaNameField;
    private JLabel lblDescription_1;
    private JTextField descrField;
    private JTextField otherField;
    private JLabel lblProperties_1;
    public JComboBox schemaPropField;
    private JButton btnSubmit;
    private JLabel lblKeys_1;
    private JTextField oattrOtherField;
    private JButton btnAddOattr;
    private JButton btnDeleteTable;
    private JButton btnDone;
    private JButton btnReset;
    private boolean importing;
    private boolean connecting;
    private JButton btnCreate;
    private boolean created = false;
    private HashMap<String, String> keyMap;
    protected PmNode schemaNode, baseNode;
    private List<Template> templates;

    public Utilities util;

    private static final long serialVersionUID = 1L;

    private JButton btnRSV;
    private boolean opened = false;

    public SchemaBuilder3(String sessId, int nSimPort, String sProcId, boolean bDebug) {
        setTitle("Schema Builder");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 800, 489);

        sessionId = sessId;
        System.out.println("SESSION ID IN SB: " + sessionId);
        nSimulatorPort = (nSimPort < 1024) ? PM_DEFAULT_SIMULATOR_PORT
                : nSimPort;
        sProcessId = sProcId;
        System.out.println("SB: simulator port: " + nSimulatorPort);
        sysCaller = new SysCallerImpl(nSimulatorPort, sessionId, sProcId, bDebug, "SB");
        util = new Utilities(sessionId, sProcessId, nSimulatorPort, bDebug);
        tempKeys = new ArrayList<String>();
        keyMap = new HashMap<String, String>();

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                //SchemaBuilder.this.checkIfCreatedAndDelete();
                //dispose();//terminate(0);
                System.out.println("exiting Schema Builder");
                if (!submitted && created) {
                    doDelete(pcName, getSchemaType(PM_NODE.POL.value));
                }
                dispose();
            }
        });

        this.colPermEditor = new ColumnPermEditor2(util, this);
        this.colPermEditor.pack();

        userName = util.getCrtUser();
        log("USERNAME: " + userName);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        mntmNewMenuItem = new JMenuItem("New");
        mntmNewMenuItem.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                SchemaBuilder3.this.checkIfCreatedAndDelete();
                resetAll();
            }
        });
        mnFile.add(mntmNewMenuItem);

        separator = new JSeparator();
        mnFile.add(separator);

        mntmNewMenuItem_1 = new JMenuItem("Open");
        mntmNewMenuItem_1.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                openSchema();
            }
        });
        mnFile.add(mntmNewMenuItem_1);

        separator_1 = new JSeparator();
        mnFile.add(separator_1);

        mntmNewMenuItem_2 = new JMenuItem("Delete");
        mntmNewMenuItem_2.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                /*if(pcName == null){
                    JOptionPane.showMessageDialog(null, "Please open a schema to be deleted");
                    return;
                }
                deleteTable(pcName, getSchemaType(PM_NODE.POL.value));
                resetAll();*/
                JOptionPane.showMessageDialog(SchemaBuilder3.this, "Not yet implemented");
            }
        });
        mnFile.add(mntmNewMenuItem_2);

        separator_2 = new JSeparator();
        mnFile.add(separator_2);

        JMenuItem mntmImport = new JMenuItem("Import");
        mntmImport.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                ImportParser parser = new ImportParser(SchemaBuilder3.this);
                parser.setLocationRelativeTo(SchemaBuilder3.this);
                parser.setVisible(true);
            }
        });
        mnFile.add(mntmImport);

        separator_2 = new JSeparator();
        mnFile.add(separator_2);

        mntmNewMenuItem_3 = new JMenuItem("Exit");
        mntmNewMenuItem_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SchemaBuilder3.this.checkIfCreatedAndDelete();
                dispose();
            }
        });
        mnFile.add(mntmNewMenuItem_3);

        JMenu mnEdit = new JMenu("Edit");
        menuBar.add(mnEdit);

        JMenuItem mntmAug = new JMenuItem("Schema Augmentor");
        mntmAug.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                new SchemaAugmentor(SchemaBuilder3.this, util, sysCaller);
            }

        });

        mnEdit.add(mntmAug);

        JMenuItem mntmBuilder = new JMenuItem("PMBuilder");
        mntmBuilder.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                new PMBuilder();
            }

        });

        //mnEdit.add(mntmBuilder);



        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        JMenuItem mntmTutorial = new JMenuItem("Tutorial");
        mntmTutorial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                //JFrame jf = new JFrame("Schema Builder Tutorial");
                String tutorial = "1. Start by filling in the fields in the Schema Information panel.\n" +
                        "2. Once you have completed this, click on the \"Create New Schema\" button to create the schema.\n" +
                        "3. You will then see a Tree on the left side of the next panel down. This will be where you can see the\n" +
                        "\toverall structure of the Schema you are creating and how it will look in your session graph.\n" +
                        "4. Now you can create object attributes in you schema by clicking the \"Add Oattr\" at the bottom of the panel, which will\n" +
                        "\tenable the fields on the right side of the panel to edit your new object attribute information.\n" +
                        "5. Once you are done filling out the fields, you can click \"Set Permissions\" which will allow you to set the permissions\n" +
                        "\ton the object attribute you are about to create. (optional)\n" +
                        "6. When you are all done filling out the information and setting the permissions than you can click the \"Create Object Attribute\"\n" +
                        "7. button to create the new Object Attribute, and then you will see it added to your Schema's hierarchy on the left.\n" +
                        "8. Once you are done adding all the Object Attributes you want, make sure you clicl on \"Manage Keys\" to manage the keys for the schema.\n" +
                        "9. Once that is done you can click the \"Submit Schema\" button to finally create a new template and finish\n" +
                        "\tcreating your new Schema.";
                //jf.getContentPane().add(new JLabel(tutorial));
                //jf.pack();
                //jf.setVisible(true);
                JOptionPane.showMessageDialog(null, tutorial);
            }
        });
        mnHelp.add(mntmTutorial);

        JMenuItem testItem = new JMenuItem("Test");
        testItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SchemaBuilder3Test.setParams(sessid);
                System.out.println(SchemaBuilder3Test.class);
                class TestTask extends SwingWorker<String, String>{

                    @Override
                    protected String doInBackground() throws Exception {
                        System.out.println("testin");
                        SchemaBuilder3Test.setParams(sessid);
                        System.out.println("testing");

                        SchemaBuilder3Test.setSchemaBuilder(SchemaBuilder3.this);
                        System.out.println("testing");

                        String message = "";
                        System.out.println("testing");

                        Result result = JUnitCore.runClasses(SchemaBuilder3Test.class);
                        for (Failure failure : result.getFailures()){
                            message += failure.toString() + "\n";
                            message += "=================================";
                        }
                        System.out.println("testing");

                        message += "\nTESTS COMPLETED";
                        message += "\nTotal tests run: " + result.getRunCount();
                        message += "\nTotal tests failed: " + result.getFailureCount();

                        JOptionPane.showMessageDialog(SchemaBuilder3.this, message, "Test Results", JOptionPane.INFORMATION_MESSAGE);
                        return null;
                    }
                }
                TestTask t = new TestTask();
                t.execute();
            }
        });
        mnHelp.add(testItem);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{619, 0, 0, 0, 0, 0};
        gbl_contentPane.rowHeights = new int[]{60, 45, 300, 35, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);

        JPanel panel = new JPanel();
        panel.setPreferredSize(panel.getPreferredSize());
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
                BorderFactory.createTitledBorder("Schema Information:")));
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.gridheight = 2;
        gbc_panel.gridwidth = 5;
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.insets = new Insets(0, 0, 5, 0);
        gbc_panel.gridx = 0;
        gbc_panel.gridy = 0;
        contentPane.add(panel, gbc_panel);
        panel.setLayout(new BorderLayout(0, 0));

        panel_5 = new JPanel();
        panel_5.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(panel_5, BorderLayout.CENTER);
        //SpringLayout sl_panel_5 = new SpringLayout();
        //panel_5.setLayout(sl_panel_5);

        list = new JList();
        //sl_panel_5.putConstraint(SpringLayout.NORTH, list, 0, SpringLayout.NORTH, panel_5);
        //sl_panel_5.putConstraint(SpringLayout.WEST, list, 0, SpringLayout.WEST, panel_5);
        //panel_5.add(list);

        lblSchemaName = new JLabel("Name:");
        //sl_panel_5.putConstraint(SpringLayout.NORTH, lblSchemaName, 0, SpringLayout.NORTH, list);
        //sl_panel_5.putConstraint(SpringLayout.WEST, lblSchemaName, 0, SpringLayout.WEST, list);
        panel_5.add(lblSchemaName);

        schemaNameField = new JTextField();
        //sl_panel_5.putConstraint(SpringLayout.NORTH, schemaNameField, 0, SpringLayout.NORTH, list);
        //sl_panel_5.putConstraint(SpringLayout.WEST, schemaNameField, 6, SpringLayout.EAST, lblSchemaName);
        panel_5.add(schemaNameField);
        schemaNameField.setColumns(10);

        lblDescription_1 = new JLabel("Description:");
        //sl_panel_5.putConstraint(SpringLayout.NORTH, lblDescription_1, 0, SpringLayout.NORTH, panel_5);
        //sl_panel_5.putConstraint(SpringLayout.WEST, lblDescription_1, 6, SpringLayout.EAST, schemaNameField);
        panel_5.add(lblDescription_1);

        descrField = new JTextField();
        //sl_panel_5.putConstraint(SpringLayout.NORTH, descrField, 0, SpringLayout.NORTH, list);
        //sl_panel_5.putConstraint(SpringLayout.WEST, descrField, 6, SpringLayout.EAST, lblDescription_1);
        panel_5.add(descrField);
        descrField.setColumns(10);

        JLabel lblNewLabel = new JLabel("Other Info:");
        //sl_panel_5.putConstraint(SpringLayout.NORTH, lblNewLabel, 1, SpringLayout.NORTH, list);
        panel_5.add(lblNewLabel);

        otherField = new JTextField();
        //sl_panel_5.putConstraint(SpringLayout.WEST, otherField, 650, SpringLayout.WEST, panel_5);
        //sl_panel_5.putConstraint(SpringLayout.EAST, otherField, 0, SpringLayout.EAST, panel_5);
        //sl_panel_5.putConstraint(SpringLayout.EAST, lblNewLabel, -6, SpringLayout.WEST, otherField);
        //sl_panel_5.putConstraint(SpringLayout.NORTH, otherField, 0, SpringLayout.NORTH, schemaNameField);
        panel_5.add(otherField);
        otherField.setColumns(10);

        btnCreate = new JButton("Create Schema");
        //sl_panel_5.putConstraint(SpringLayout.WEST, btnCreate, 568, SpringLayout.WEST, panel_5);
        //sl_panel_5.putConstraint(SpringLayout.SOUTH, btnCreate, 0, SpringLayout.SOUTH, panel_5);
        //sl_panel_5.putConstraint(SpringLayout.EAST, btnCreate, 0, SpringLayout.EAST, panel_5);
        btnCreate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createSchema();
            }
        });
        panel_5.add(btnCreate);

        lblProperties_1 = new JLabel("Properties:");
        //sl_panel_5.putConstraint(SpringLayout.EAST, descrField, -6, SpringLayout.WEST, lblProperties_1);
        //sl_panel_5.putConstraint(SpringLayout.NORTH, lblProperties_1, 0, SpringLayout.NORTH, list);
        //panel_5.add(lblProperties_1);

		/*lblKeys = new JLabel("Keys:");
		sl_panel_5.putConstraint(SpringLayout.WEST, lblKeys, 0, SpringLayout.WEST, lblDescription_1);
		sl_panel_5.putConstraint(SpringLayout.SOUTH, lblKeys, -5, SpringLayout.SOUTH, panel_5);
		panel_5.add(lblKeys);*/

        schemaPropField = new JComboBox(new String[]{"File System", "Database"});
        //sl_panel_5.putConstraint(SpringLayout.EAST, lblProperties_1, -6, SpringLayout.WEST, schemaPropField);
        //sl_panel_5.putConstraint(SpringLayout.WEST, schemaPropField, 465, SpringLayout.WEST, panel_5);
        //sl_panel_5.putConstraint(SpringLayout.EAST, schemaPropField, -6, SpringLayout.WEST, lblNewLabel);
        //sl_panel_5.putConstraint(SpringLayout.NORTH, schemaPropField, 0, SpringLayout.NORTH, schemaNameField);
        //panel_5.add(schemaPropField);
        schemaPropField.addActionListener(e -> {
            schemaProp = (String) schemaPropField.getSelectedItem();
        });

        JPanel panel_1 = new JPanel();
        panel_1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0),
                BorderFactory.createTitledBorder("Create Schema:")));

        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.insets = new Insets(0, 0, 5, 0);
        gbc_panel_1.gridwidth = 5;
        gbc_panel_1.fill = GridBagConstraints.BOTH;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 2;
        contentPane.add(panel_1, gbc_panel_1);
        panel_1.setLayout(new BorderLayout(0, 0));
        splitPane.setEnabled(false);


        splitPane.setDividerSize(10);
        splitPane.setResizeWeight(0.5);

        panel_4 = new JPanel();
        panel_1.add(panel_4);//splitPane.setLeftComponent(panel_4);
        panel_4.setLayout(new BorderLayout(0, 0));

        panel_6 = new JPanel();
        panel_6.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        panel_4.add(panel_6, BorderLayout.SOUTH);

        panel_6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        btnCreateTable = new JButton("Create Table");
        btnCreateTable.setEnabled(false);
        btnCreateTable.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                SBTableEditor editor = new SBTableEditor(SchemaBuilder3.this);
                editor.setVisible(true);
            }

        });
        panel_6.add(btnCreateTable);

        btnDeleteTable = new JButton("Delete Table");
        panel_6.add(btnDeleteTable);
        btnDeleteTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(selectedNode == null){
                    JOptionPane.showMessageDialog(null, "Please select a Oattr to delete from the graph.");
                    return;
                }
                if(selectedNode == schemaNode){
                    JOptionPane.showMessageDialog(null, "You can't delete this node.");
                    return;
                }
                deleteTable(selectedNode.getName(), selectedNode.getType());
            }
        });
        btnDeleteTable.setEnabled(false);

        btnRSV = new JButton("Refresh");
        panel_6.add(btnRSV);
        btnRSV.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetSchemaView(schemaNode.getType());
            }
        });
        btnRSV.setEnabled(false);

        scrollPane = new JScrollPane();
        panel_4.add(scrollPane, BorderLayout.CENTER);

        btnDone = new JButton("Done");
        btnDone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("exiting Schema Builder");
                if(!submitted && created){
                    //deleteTable(schemaName, PM_NODE.POL.value);
                    doDelete(pcName, getSchemaType(PM_NODE.POL.value));
                }
                dispose();//terminate(0);
            }
        });
        GridBagConstraints gbc_btnDone = new GridBagConstraints();
        gbc_btnDone.anchor = GridBagConstraints.WEST;
        gbc_btnDone.insets = new Insets(0, 0, 0, 5);
        gbc_btnDone.gridx = 0;
        gbc_btnDone.gridy = 3;
        contentPane.add(btnDone, gbc_btnDone);

        btnSubmit = new JButton("Submit Schema");
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                submitSchema();
            }
        });

        btnManageKeys = new JButton("Manage Keys");
        btnManageKeys.setEnabled(false);
        btnManageKeys.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                //openColPermEditor(schemaName);
                promptKeys();
            }
        });

        GridBagConstraints gbc_btnManageKeys = new GridBagConstraints();
        gbc_btnManageKeys.insets = new Insets(0, 0, 0, 5);
        gbc_btnManageKeys.gridx = 3;
        gbc_btnManageKeys.gridy = 3;
        contentPane.add(btnManageKeys, gbc_btnManageKeys);
        GridBagConstraints gbc_btnSubmit = new GridBagConstraints();
        gbc_btnSubmit.insets = new Insets(0, 0, 0, 2);
        gbc_btnSubmit.gridx = 4;
        gbc_btnSubmit.gridy = 3;
        contentPane.add(btnSubmit, gbc_btnSubmit);

        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public String getSchemaType(String type){/*
        if(type.equals(PM_NODE.POL.value)) {
            return connecting ? PM_NODE.S_POL.value : PM_NODE.POL.value;
        }else if(type.equals(PM_NODE.OATTR.value)){
            return connecting ? PM_NODE.S_OATTR.value : PM_NODE.OATTR.value;
        }else{*/
            return type;
        //}
    }

    private void enableAll(){
        oattrNameField.setEnabled(true);
        baseNameField.setEnabled(true);
        oattrPropField.setEnabled(true);
        oattrOtherField.setEnabled(true);
        oattrDescriptionField.setEnabled(true);
        btnSetPerms.setEnabled(true);
        btnCreateOattr.setEnabled(true);
        btnReset.setEnabled(true);
        //btnManageKeys.setEnabled(true);
        oattrEdit = false;
        selectedOattr = "";
    }

    public void create(ArrayList<String> data){
        System.out.println("PcName: " + pcName);
        System.out.println("data: " + data);
        String tableName = data.get(0);
        boolean bRes = util.createOattr(getSchemaType(PM_NODE.OATTR.value), tableName, pcName, getSchemaType(PM_NODE.OATTR.value));
        if(!bRes){
            JOptionPane.showMessageDialog(null, "Table could not be created",
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        data.remove(0);
        for(String column : data){
            bRes = util.createOattr(getSchemaType(PM_NODE.OATTR.value), column,
                    tableName, getSchemaType(PM_NODE.OATTR.value));
            if(!bRes){
                JOptionPane.showMessageDialog(null, "Column \""+ column +"\" could not be created",
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        resetSchemaView(schemaNode.getType());
    }

    public void importing(){
        importing = true;
    }

    public void connecting(){
        connecting = true;
    }

    private ImportSchemaTask task;

    private ArrayList<String> tempKeys;

    public void callImportTask(){
        task = new ImportSchemaTask();
        task.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress" == evt.getPropertyName()) {
                    int progress = (Integer) evt.getNewValue();
                    task.progressBar.setValue(progress);

                }
            }
        });
        task.execute();
    }

    public void createSchema(){
        pcName = schemaNameField.getText();
        if(pcName == null || pcName.length() == 0){
            JOptionPane.showMessageDialog(null, "Please fill out Schema Name Field");
            return;
        }

        String descr = descrField.getText();
        if(descr == null || descr.length() == 0){
            descr = pcName;
            descrField.setText(pcName);
        }

        String other = otherField.getText();
        if(other == null || other.length() == 0){
            other = pcName;
            otherField.setText(pcName);
        }

        String prop = (String) schemaPropField.getSelectedItem();
        if(prop == null || prop.length() == 0){
            JOptionPane.showMessageDialog(null, "Please select a property.");
            return;
        }

        btnCreate.setEnabled(false);

		/*String key = schemaKeyField.getText();
		if(key == null || key.length() == 0){
			JOptionPane.showMessageDialog(null, "You need to include at least one key.\nSeparate keys by commas.");
			return;
		}*/
        System.out.println(pcName);
        schemaNode = new PmNode(PmNodeType.POLICY, pcName);

        boolean bRes = util.createBaseOattr(getSchemaType(PM_NODE.POL.value), getSchemaType(PM_NODE.OATTR.value), pcName, descr, other, "storage=file");
        if(!bRes){
            JOptionPane.showMessageDialog(null, "Could not create schema " + pcName, "ERROR", JOptionPane.ERROR_MESSAGE);
            resetAll();
            return;
        }

        resetSchemaView(schemaNode.getType());

        //btnAddOattr.setEnabled(true);
        //resetOattrEditor();
        enableBuilder();
        created = true;
    }

    public void enableBuilder(){
        if(!importing) {
            btnDeleteTable.setEnabled(true);
            btnRSV.setEnabled(true);
            btnCreateTable.setEnabled(true);
        }
        btnManageKeys.setEnabled(true);
        btnSubmit.setEnabled(true);
    }

    /**
     * adds a template for this schema
     */
    public void submitSchema(){
        int ret = JOptionPane.showConfirmDialog(this, "Once you submit the schema you won't be able to change it.\nDo you still want to continue?");
        if(ret != JOptionPane.OK_OPTION)return;

        if(!setKeys && !connecting){
            JOptionPane.showMessageDialog(this, "You must set keys for the tables to create a template");
            return;
        }

        //List<String> tables = getSchemaTables();
        System.out.println("tables: " + tables);

        if(keyMap.size() != tables.size() && !connecting){
            JOptionPane.showMessageDialog(this, "You must set keys for all of the tables to create a template");
            return;
        }
        pcName = schemaNameField.getText();
        System.out.println(connecting);
        if(!connecting) {
            Collections.reverse(tables);
            for (Table table : tables) {
                List<PmNode> contNodes = getAllNodes(new PmNode(
                        getSchemaType(PM_NODE.OATTR.value),
                        sysCaller.getIdOfEntityWithNameAndType(table.getTable(), getSchemaType(PM_NODE.OATTR.value)),
                        table.getTable(),
                        new PmNodeChildDelegate(util.sslClient, sessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES)));
                List<String> conts = new ArrayList<String>();
                for (PmNode n : contNodes) {
                    conts.add(n.getName());
                }
                String sKeys = keyMap.get(table);
                String pieces[] = sKeys.split(GlobalConstants.PM_FIELD_DELIM);
                List<String> keys = new ArrayList<String>();
                for (String s : pieces) {
                    keys.add(s);
                }
                System.out.println("Adding template " + table + " with conts " + conts + " with keys " + keys);
                Packet res = util.addTemplate(table.getTable(), conts, keys);
                if (res == null || res.hasError()) {
                    JOptionPane.showMessageDialog(null, "Could not create template");
                    //checkIfCreatedAndDelete();
                    //resetAll();
                    return;
                }
            }
        }

        submitted = true;
        opened = true;
        created = false;
        disableSchemaButtons();
    }

    public void submitted(){
        submitted = true;
    }

    public void disableSchemaButtons(){
        btnSubmit.setEnabled(false);
        btnManageKeys.setEnabled(false);
        btnCreateTable.setEnabled(false);
        btnDeleteTable.setEnabled(false);
        btnCreate.setEnabled(false);
    }

    private boolean setKeys = false;
    private String selectedTable;

    private void promptKeys() {
        setKeys = true;
        final JFrame jf = new JFrame();
        jf.setTitle("Key Manager");
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));

        System.out.println("TABLES>>" + tables);
        //select a table panel
        if(tables == null || tables.size() < 1){
            JOptionPane.showMessageDialog(this, "You need to create tables first");
            return;
        }

        JPanel selectTablePanel = new JPanel();
        selectTablePanel.setLayout(new BorderLayout(5, 5));
        selectTablePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
                BorderFactory.createTitledBorder("Select a table:")));
        final DefaultComboBoxModel tableModel = new DefaultComboBoxModel();
        for(Table t : tables){
            tableModel.addElement(t.getTable());
        }
        final JComboBox tableList = new JComboBox(tableModel);

        final JComboBox keyBox = new JComboBox();
        final DefaultComboBoxModel model = (DefaultComboBoxModel) keyBox.getModel();
        final DefaultListModel keyModel = new DefaultListModel();
        tableList.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                keyModel.removeAllElements();
                model.removeAllElements();
                selectedTable = (String)tableList.getSelectedItem();
                /*PmNode parent = new PmNode(
                        getSchemaType(PM_NODE.OATTR.value),
                        sysCaller.getIdOfEntityWithNameAndType(selectedTable, getSchemaType(PM_NODE.OATTR.value)),
                        selectedTable,
                        new PmNodeChildDelegate(util.sslClient, sessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
                List<PmNode> tempConts = getAllNodes(parent);
                System.out.println(tempConts);
                for(int i = 0; i < tempConts.size(); i++){
                    model.addElement(tempConts.get(i).getName());
                }*/
                Table table = null;
                for(Table t : tables){
                    if(t.getTable().equalsIgnoreCase(selectedTable)){
                        table = t;
                    }
                }

                if(table == null)return;

                List<String> columns = table.getColumns();
                for(String c : columns){
                    model.addElement(c);
                }

                String value = keyMap.get(selectedTable);
                if(value != null){
                    String[] pieces = value.split(GlobalConstants.PM_FIELD_DELIM);
                    for(String s : pieces){
                        keyModel.addElement(s);
                    }
                }
            }
        });
        selectTablePanel.add(new JScrollPane(tableList), BorderLayout.CENTER);
        contentPane.add(selectTablePanel, BorderLayout.NORTH);

        tableList.setSelectedIndex(0);
        /*selectedTable = (String)tableList.getSelectedItem();
        PmNode parent = new PmNode(
                getSchemaType(PM_NODE.OATTR.value),
                sysCaller.getIdOfEntityWithNameAndType(selectedTable, getSchemaType(PM_NODE.OATTR.value)),
                selectedTable,
                new PmNodeChildDelegate(util.sslClient, sessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
        List<PmNode> tempNodes = getAllNodes(parent);
        System.out.println(tempNodes);
        for(int i = 0; i < tempNodes.size(); i++){
            model.addElement(tempNodes.get(i).getName());
        }*/

        //key list
        final JList keyList = new JList(keyModel);
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.add(keyList, BorderLayout.CENTER);
        centerPanel.add(new JScrollPane(keyList), BorderLayout.CENTER);

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BorderLayout());
        fieldPanel.add(keyBox, BorderLayout.SOUTH);
        centerPanel.add(fieldPanel, BorderLayout.SOUTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        //add and remove buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout(5, 5));

        JButton add = new JButton("Add");
        add.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                String key = (String) keyBox.getSelectedItem();
                if(key == null || key.length() == 0){
                    return;
                }
                keyModel.addElement(key);
                String value = "";
                for(int i = 0; i < keyModel.size()-1; i++){
                    value += keyModel.getElementAt(i) + GlobalConstants.PM_FIELD_DELIM;
                }
                value += keyModel.getElementAt(keyModel.size()-1);
                keyMap.put(selectedTable, value);
            }

        });
        buttonPanel.add(add, BorderLayout.WEST);

        JButton remove = new JButton("Remove");
        remove.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                keyModel.remove(keyList.getSelectedIndex());
                String value = "";
                for(int i = 0; i < keyModel.size()-1; i++){
                    value += keyModel.getElementAt(i) + GlobalConstants.PM_FIELD_DELIM;
                }
                value += keyModel.getElementAt(keyModel.size()-1);
                keyMap.put(selectedTable, value);
            }

        });
        buttonPanel.add(remove, BorderLayout.EAST);

        //done button
        tempKeys = new ArrayList<String>();
        JButton done = new JButton("Close");
        done.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
				/*List<String> tables = new ArrayList<String>();
				for(int i = 0; i < tableModel.getSize(); i++){
					tables.add((String)tableModel.getElementAt(i));
				}
				for(String t : tables){

				}
				/*tempKeys.clear();
				for(int i = 0; i < keyModel.size(); i++){
					if(!tempKeys.contains((String) keyModel.get(i))){
						tempKeys.add((String) keyModel.get(i));
					}
				}
				System.out.println(tempKeys);
				if(tempKeys.size() > 0){
					btnSubmit.setEnabled(true);
				}*/

                jf.setVisible(false);
                System.out.println(keyMap);
            }
        });
        buttonPanel.add(done, BorderLayout.SOUTH);
        contentPane.add(buttonPanel,  BorderLayout.SOUTH);

        jf.setContentPane(contentPane);
        jf.setLocationRelativeTo(null);
        jf.pack();
        jf.setVisible(true);

        //return tempKeys;
    }

    private List<String> getSchemaTables() {
        ArrayList<PmNode> retList = new ArrayList<PmNode>();
        new ArrayList<PmNode>();

        PmNode oaNode = new PmNode(
                getSchemaType(PM_NODE.OATTR.value),
                sysCaller.getIdOfEntityWithNameAndType(pcName, getSchemaType(PM_NODE.OATTR.value)),
                pcName,
                new PmNodeChildDelegate(util.sslClient, sessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
        PmGraph oaGraph = new PmGraph(oaNode, false);
        TreeModel tm = oaGraph.getModel();

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
                retList.add(n);
            }
        }

        List<PmNode> tables = new ArrayList<PmNode>();
        for(int i = 0; i < retList.size(); i++){
            PmNode child = retList.get(i);
            PmNode parent = child.getParent();
            if(!tables.contains(parent))tables.add(parent);
        }

        List<String> tableNames = new ArrayList<String>();
        for(PmNode node : tables){
            tableNames.add(node.getName());
        }
        return tableNames;
    }

    private List<Table> tables;
    public void setTables(List<Table> tables){
        this.tables = tables;
    }

    public List<Table> getTables(){
        return tables;
    }

    /**
     *
     */
    private void openColPermEditor(String name) {
        if(name == null || name.length() == 0){
            JOptionPane.showMessageDialog(null, "You must specify the Object attribute name first before setting the permissions");
            return;
        }
        colPermEditor.prepare(name, getSchemaType(PM_NODE.OATTR.value));
        colPermEditor.setColumnName(name);
        colPermEditor.setVisible(true);
    }

    /**
     * @param base
     * @return
     */
    private ArrayList<PmNode> getAllNodes(PmNode base){//, List<PmNode> children){
        //ret.add(base);
        //log("RET: " + ret);
        ArrayList<PmNode> ret = new ArrayList<PmNode>();
        List<PmNode> children = base.getChildren();
        Enumeration<PmNode> childEnum= Collections.enumeration(children);
        while(childEnum.hasMoreElements()){
            PmNode n = childEnum.nextElement();
            if(n.getNodeType().equals(PmNodeType.OBJECT_ATTRIBUTE)
                    || n.getNodeType().equals(PmNodeType.SYNC_OATTR))ret.add(n);
            ret.addAll(getAllNodes(n));
        }
        return ret;
    }
    private ArrayList<ArrayList<String>> containers = new ArrayList<ArrayList<String>>();
    private String pcName;
    private String pcId;
    /**
     *
     */
    public void resetTree(){
        resetTree((PmNode)tree.getModel().getRoot());
    }

    /**
     * @param rootNode
     */
    public void resetTree(PmNode rootNode) {
        //rootNode.invalidate();
    }

    /**
     *
     */
    public void resetAll() {
        btnManageKeys.setEnabled(false);
        btnSubmit.setEnabled(false);
        btnCreate.setEnabled(true);

        schemaNameField.setText("");
        descrField.setText("");
        otherField.setText("");
        schemaPropField.setSelectedIndex(0);

        btnRSV.setEnabled(false);

        if(tree != null)tree.setVisible(false);
        containers.clear();

        btnCreateTable.setEnabled(false);
        btnDeleteTable.setEnabled(false);

        pcName = null;
        tree = null;

        submitted = false;
        opened = false;
        created = false;
        importing = false;

        keyMap.clear();
        tempKeys.clear();
    }

    public void setCreated(boolean input){
        created = input;
    }

    public void checkIfCreatedAndDelete(){
        if(created){
            deleteTable(pcName, getSchemaType(PM_NODE.POL.value));
            //deletePC(pcName);
        }
    }


    /**
     * @return
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @return
     */
    public int getnSimulatorPort() {
        return nSimulatorPort;
    }

    /**
     * @return
     */
    public String getsProcessId() {
        return sProcessId;
    }

    /**
     * @param nCode
     */
    public void terminate(int nCode) {
        sysCaller.exitProcess(sProcessId);
        System.exit(nCode);
    }

    public void setSchemaName(String input){
        pcName = input;
    }

    public void addToContainers(ArrayList<String> input){
        containers.add(input);
    }

    public void setSchemaField(String input){
        schemaNameField.setText(input);
    }

    public void resetSchemaView(String baseType){
        schemaNode = new PmNode(
                baseType,
                sysCaller.getIdOfEntityWithNameAndType(pcName, baseType),
                pcName,
                new PmNodeChildDelegate(util.sslClient, sessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
        tree = new PmGraph(schemaNode, false);
        tree.addMouseListener(new SchemaMouseListener());
        tree.setExpandsSelectedPaths(true);
        resetTree(schemaNode);
        scrollPane.setViewportView(tree);
    }

    private void requestPolicyClass(){
        ArrayList<String> tplList = new ArrayList<String>();
        ArrayList<String> tplNames = new ArrayList<String>();

        JComboBox tableBox = new JComboBox();
        tableBox.setPreferredSize(new Dimension(160, 20));

        ArrayList<String> pcs = util.getPolicyClasses();
        if (pcs == null) {
            JOptionPane.showMessageDialog(null,
                    "Could not get schemas");
            return;
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
            return;
        }

        int sel = tableBox.getSelectedIndex();
        if(sel < 0){
            JOptionPane.showMessageDialog(null, "Please select a table to open");
        }

        pcName = ((String) tableBoxModel.getElementAt(sel));
        pcId = sysCaller.getIdOfEntityWithNameAndType(pcName,getSchemaType(PM_NODE.POL.value));

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
                conts.add(s);
            }
            String tempKeys = tempInfo.getStringValue(2);
            pieces = tempKeys.split(GlobalConstants.PM_FIELD_DELIM);
            for(String s : pieces){
                keys.add(s);
            }
        }
    }

    /**
     * Opens a schema and fills all fields in SchemaBuilder
     */
    public void openSchema(){
        checkIfCreatedAndDelete();
        requestPolicyClass();
        btnManageKeys.setEnabled(false);
        btnSubmit.setEnabled(false);
        btnCreate.setEnabled(false);

        ArrayList<String> attrInfo = util.getAttrInfo(pcId, getSchemaType(PM_NODE.POL.value));

        schemaNameField.setText(pcName);

        //schemaPropField.setText(null);
        String prop = "";
        for (int i = 3; i < attrInfo.size()-1; i++) {
            prop += attrInfo.get(i) + ", ";
            log("PROP: " + prop);

        }
        /*prop += attrInfo.get(attrInfo.size()-1);
        //prop = schemaPropField.getText() + prop;
        if(prop != null && !prop.equals("null")) {
            schemaPropField.setText(prop);
        }*/

        descrField.setText(attrInfo.get(1));
        otherField.setText(attrInfo.get(2));

        schemaNode = new PmNode(
                getSchemaType(PM_NODE.POL.value),
                pcId,
                pcName,
                new PmNodeChildDelegate(util.sslClient, sessionId, PmGraphDirection.UP, PmGraphType.OBJECT_ATTRIBUTES));
        tree = new PmGraph(schemaNode, false);
        //tree.setToolTipText("Right-click to open a record, container, or table");
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.addMouseListener(new SchemaMouseListener());

        scrollPane.setViewportView(tree);
        opened = true;
    }

    public static void log(Object input){
        System.out.println(input);
    }

    /**
     * @param id
     * @param name
     */
    private void deleteTemplate(String id, String name) {
        try{
            log("tplId: " + id);
            log("tplName: " + name);
            Packet r = util.doDeleteTemplate(id, name);
            if (r.hasError()) {
                JOptionPane.showMessageDialog(null, r.getErrorMessage());
                return;
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "template didn't delete");
            return;
        }
    }

    /**
     * @return
     */
    public String getSchemaName(){
        return pcName;
    }

    public boolean modify(){
        return modify;
    }

    /**
     * @param tempName
     * @return
     */
    private boolean templateExists(String tempName){
        Packet res = sysCaller.getTemplates();
        if (res == null) {
            JOptionPane.showMessageDialog(null,
                    "Null return from getTemplates!");
            return false;
        }
        if (res.hasError()) {
            JOptionPane.showMessageDialog(null, res.getErrorMessage());
            return false;
        }
        Vector<String> tempNames = new Vector<String>();
        for(int i = 0; i < res.size(); i++){
            String sLine = res.getStringValue(i);
            //log("Template: " + sLine);
            String[] temp = sLine.split(":");
            String name = temp[0];
            tempNames.add(name);
        }
        //log("tempNames: " + tempNames);
        if(tempNames.contains(tempName)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * @param colName
     * @return
     */
    public boolean columnExists(String colName){
        ArrayList<String> oattrs = util.getAllOattrs(colName);
        for(int i = 0; i < oattrs.size(); i++){
            String[] oa = oattrs.get(i).split(":");
            String oattrName = oa[1];
            if(oattrName.equals(colName)){
                return true;
            }
        }
        return false;
    }

    /**
     * @return
     */
    public String getUserName(){
        return userName;
    }

    /**
     * @return
     */


    /**
     * @param id
     * @param sBaseName
     * @param name
     * @param type
     */
    public void addToPerms(String id, String sBaseName,
                           String name, String type){
        //For opsets
        ArrayList<String> ops = util.getFromOpsets(id);
        processOps(ops, sBaseName, name);

        //For reps
        ArrayList<String> reps = util.getReps(id, type);
        for(int i = 0; i < reps.size(); i++){
            String repId = reps.get(i);
            String rep = sysCaller.getNameOfEntityWithIdAndType(repId, "ob");
            log(rep);
            rep = util.getAssocOattr(repId);
            ArrayList<String> repOps = util.getFromOpsets(rep);
            processOps(repOps, sBaseName, name);
        }
    }

    /**
     * @param ops
     * @param sBaseName
     * @param name
     */
    public void processOps(ArrayList<String> ops, String sBaseName, String name){
        String perms = "";
        String ua = "";
        for(int i = 0; i < ops.size(); i++){
            String op = ops.get(i);
            ArrayList<String> opInfo = util.getOpsetInfo(op);
            log(">>>: " + op);
            for(int j = 0; j < opInfo.size(); j++){
                log("  >>>: " + opInfo.get(j));
                if(j > 4){
                    perms += opInfo.get(j) + ",";

                }
            }
            log(perms);
            ArrayList<String> uattrs = util.getFromAttrs(op);
            for(int x = 0; x < uattrs.size(); x++){
                ua = uattrs.get(x);
                ua = sysCaller.getNameOfEntityWithIdAndType(ua, "a");
                log("      >>>: " + ua);
            }
            ArrayList<String> line = new ArrayList<String>();
            line.add(sBaseName);
            line.add(name);
            line.add(ua);
            line.add(perms.substring(0, perms.length() - 1));
            log(line);
        }
    }

    /**
     * @param name
     */
    public void deleteTable(String name, String type){
        if(opened) {
            if (type.equals(getSchemaType(PM_NODE.POL.value))) {
                List<String> tables = getSchemaTables();
                for (String t : tables) {
                    if (templateExists(t)) {
                        Packet temps = util.getTemplates();
                        for (int i = 0; i < temps.size(); i++) {
                            String temp = temps.getStringValue(i);
                            String pieces[] = temp.split(GlobalConstants.PM_FIELD_DELIM);
                            String tempId = pieces[1];
                            String tempName = pieces[0];
                            if (tempName.equals(t)) {
                                deleteTemplate(tempId, tempName);
                            }
                        }
                    }
                }
            }
        }

        boolean res1 = doDelete(name, type);
        if(!res1){
            log("deleteing table didnt work");
            JOptionPane.showMessageDialog(null, "Could not delete existing table.");
        }

        if(opened) {
            if (templateExists(name)) {
                Packet temps = util.getTemplates();
                for (int i = 0; i < temps.size(); i++) {
                    String temp = temps.getStringValue(i);
                    String pieces[] = temp.split(GlobalConstants.PM_FIELD_DELIM);
                    String tempId = pieces[1];
                    String tempName = pieces[0];
                    if (tempName.equals(name)) {
                        deleteTemplate(tempId, name);
                    }
                }
            }
        }
        resetSchemaView(schemaNode.getType());
    }

    public void deletePC(String name){
        String id = sysCaller.getIdOfEntityWithNameAndType(name, getSchemaType(PM_NODE.POL.value));
        ArrayList<String> uattrs = util.getFromUserAttrs(id,getSchemaType(PM_NODE.POL.value));
        for(int i = 0; i < uattrs.size(); i++){
            util.deleteAssignment(uattrs.get(i), PM_NODE.UATTR.value, id, getSchemaType(PM_NODE.POL.value));
        }
        util.deleteNode(id, getSchemaType(PM_NODE.POL.value));
    }

    public boolean doDelete(String name, String type){
        System.out.println(name + ":" + type);
        if(type.equals(PM_NODE.OPSET.value)){
            System.out.println("deleting opset" + name);
            deleteOpset(name);
            return true;
        }else if(type.equals(getSchemaType(PM_NODE.OATTR.value)) || type.equals(getSchemaType(PM_NODE.POL.value))){
            System.out.println("type = oa || pc");
            String id = sysCaller.getIdOfEntityWithNameAndType(name, type);

            /*List<String[]> members = sysCaller.getMembersOf(name, id, type, "ua");
            if(members == null || members.size() == 0){
                //we have an oa with no members
                System.out.println("deleting " + name);
                util.deleteNode(id, type);
            }else{
                System.out.println("deleting objects from table");
                util.deleteObjectFromTable(name, type);
                members = sysCaller.getMembersOf(name, id, type, "ac");
                for(int i = 0; i < members.size(); i++){
                    System.out.println("deleting " + members.get(i)[2]);
                    doDelete(members.get(i)[2], members.get(i)[0]);
                }
                util.deleteNode(id, type);
            }*/

            List<String[]> members = sysCaller.getMembersOf(name, id, type, "ac");
            if(members == null || members.size() == 0){
                //we have an oa with no members
                System.out.println("deleting " + name);
                util.deleteNode(id, type);
            }else{
                System.out.println("deleting objects from table");
                util.deleteObjectFromTable(name, type);
                members = sysCaller.getMembersOf(name, id, type, "ac");
                for(int i = 0; i < members.size(); i++){
                    System.out.println("deleting " + members.get(i)[2]);
                    doDelete(members.get(i)[2], members.get(i)[0]);
                }
                util.deleteNode(id, type);
            }

        }

        if(type.equals(getSchemaType(PM_NODE.POL.value))){
            System.out.println("deleting pc");
            deletePC(name);
        }
        return true;
    }
    /**
     * @param name
     */
    public void deleteOpset(String name){
        log("type = s");
        String sId  = sysCaller.getIdOfEntityWithNameAndType(name, "s");
        log("got id");
        ArrayList<String> op = util.getFromAttrs(sId);
        log("gotFromAttrs: ");
        log(op);
        for(int j = 0; j < op.size(); j++){
            log("deleting assignemnt");
            boolean res = util.deleteAssignment(op.get(j), "a", sId, "s");
            if(!res){
                log("!!><Didn't delete assignemnt");
            }else{
                log("deleted assignment");
                ArrayList<String> oa = util.getToAttrs(sId);
                for(int i = 0; i < oa.size(); i++){
                    res = util.deleteAssignment(sId, "s", oa.get(i), getSchemaType(PM_NODE.OATTR.value));
                    if(!res){
                        log("could not delete asignment");
                    }
                }
                util.deleteNode(sId, "s");
            }
        }
    }

    public String getDescr(){
        return descrField.getText();
    }

    public String getOther(){
        return otherField.getText();
    }

    public SysCaller getSysCaller(){
        return sysCaller;
    }

    public Utilities getUtilities(){
        return util;
    }

    public boolean createPCSchema(String polType, String oaType, String storage){
        System.out.println("called createPCSChema");
        System.out.println(pcName);
        boolean bRes = util.createBaseOattr(polType, oaType, pcName, pcName, pcName, storage);
        if(!bRes){
            JOptionPane.showMessageDialog(null, "Could not create schema " + pcName, "ERROR", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        System.out.println(sysCaller.getIdOfEntityWithNameAndType(pcName, getSchemaType(PM_NODE.POL.value)));
        return true;
    }

    static String sessid;
    static String pid;
    static int simport;
    static boolean debug;
    private JMenuItem mntmNewMenuItem;
    private JMenuItem mntmNewMenuItem_1;
    private JMenuItem mntmNewMenuItem_2;
    private JMenuItem mntmNewMenuItem_3;
    private JSeparator separator;
    private JSeparator separator_1;
    private JSeparator separator_2;
    private JButton btnManageKeys;
    private JPanel panel_4;
    private JPanel panel_6;

    public static void createGUI(){
        new SchemaBuilder3(sessid, simport, pid, debug);
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
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                createGUI();
            }
        });
    }

    private PmNode selectedNode;
    private JButton btnCreateTable;
    class SchemaMouseListener extends MouseAdapter {
        private JPopupMenu pmPopup;
        private TreePath selPath;

        public SchemaMouseListener(){
            if(SchemaBuilder3.this.modify()){
                return;
            }
            createPopUp();
        }

        @Override
        public void mouseReleased(MouseEvent e){
            if(e.isPopupTrigger()){
                showPopup(e);
            }else{
                selectedNode = (PmNode) selPath.getLastPathComponent();
            }
        }

        private void createPopUp(){
            return;
            /*pmPopup = new JPopupMenu();

            JMenuItem menuItem = new JMenuItem("Edit");
            menuItem.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    PmNode node = (PmNode) selPath.getLastPathComponent();
                    if(node.equals(schemaNode) || node.getName().equals(pcName)){
                        JOptionPane.showMessageDialog(null, "You cannot edit this attribute");
                        return;
                    }else if(!node.getType().equals(getSchemaType(PM_NODE.OATTR.value))){
                        JOptionPane.showMessageDialog(null, "Selected node is not an object attribute");
                        return;
                    }else{
                        int ret = JOptionPane.showConfirmDialog(SchemaBuilder.this, "You are about to overwrite " + node.getName(), "Alert", JOptionPane.OK_CANCEL_OPTION);
                        if(ret != JOptionPane.OK_OPTION){
                            return;
                        }
                    }

                    selectedNode = node;
                    System.out.println(selectedNode);
                    System.out.println("in mouselistener");

                    String base = "";
                    if(node.getName().equals(pcName)){
                        base = pcName;
                    }else{
                        base = node.getParent().getName();
                    }

                    String name = node.getName();
                    selectedOattr = name;
                    oattrNameField.setText(name);
                    baseNameField.setText(base);
                    String id = sysCaller.getIdOfEntityWithNameAndType(name, getSchemaType(PM_NODE.OATTR.value));
                    ArrayList<String> res = util.getAttrInfo(id, getSchemaType(PM_NODE.OATTR.value));
                    String s = res.get(0);
                    String[] pieces = s.split(GlobalConstants.PM_FIELD_DELIM);
                    String oattrName = pieces[0];
                    oattrNameField.setText(oattrName);

                    String descr = res.get(1);
                    oattrDescriptionField.setText(descr);

                    String other = res.get(2);
                    oattrOtherField.setText(other);

                    String prop = "";
                    for (int i = 3; i < res.size()-1; i++) {
                        prop += res.get(i) + ", ";
                        log("PROP: " + prop);
                        prop = prop.substring(0, prop.length() - 1);
                    }
                    prop += res.get(res.size()-1);
                    oattrPropField.setText(prop);

                    oattrNameField.setEnabled(true);
                    baseNameField.setEnabled(true);
                    oattrPropField.setEnabled(true);
                    oattrDescriptionField.setEnabled(true);
                    btnSetPerms.setEnabled(true);
                    btnReset.setEnabled(true);
                    btnCreateOattr.setEnabled(true);
                    oattrOtherField.setEnabled(true);
                    oattrEdit = true;
                }
            });
            pmPopup.add(menuItem);*/
        }

        private void showPopup(MouseEvent e) {
            pmPopup.show(e.getComponent(), e.getX(), e.getY());
        }
        @Override
        public void mousePressed(MouseEvent e) {
            if(SchemaBuilder3.this.modify()){
                return;
            }
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            selPath = tree.getPathForLocation(e.getX(), e.getY());
            PmNode node = (PmNode) selPath.getLastPathComponent();
            System.out.println(node);
            if (selRow != -1) {
                if(e.isPopupTrigger()){
                    showPopup(e);
                }else{
                    if(node.getType().equals(getSchemaType(PM_NODE.OATTR.value))){
                        System.out.println("SETTING BASE NOW");
                        baseNameField.setText(node.getName());
                    }else{
                        JOptionPane.showMessageDialog(null, "Selected node is not an object attribute");
                        return;
                    }
                    oattrNameField.setEnabled(true);
                    baseNameField.setEnabled(true);
                    oattrPropField.setEnabled(true);
                    oattrDescriptionField.setEnabled(true);
                    btnSetPerms.setEnabled(true);
                    btnReset.setEnabled(true);
                    btnCreateOattr.setEnabled(true);
                    oattrOtherField.setEnabled(true);
                }
            }
        }
    }

    class ImportSchemaTask extends SwingWorker<String, String>{

        private JProgressBar progressBar;
        private JTextArea outputWindow;
        private JFrame jf;


        @Override
        protected String doInBackground() throws Exception {
            jf = new JFrame();

            jf.setTitle("Importing Schema Progress Monitor");

            JPanel upperPanel = new JPanel();
            jf.getContentPane().add(upperPanel, BorderLayout.NORTH);

            progressBar = new JProgressBar(0, 100);
            progressBar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(10, 10, 10, 10),
                    BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
            Dimension d = progressBar.getPreferredSize();
            d.width = 500;
            progressBar.setPreferredSize(d);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Processing...");

            JPanel outputPanel = new JPanel();
            jf.getContentPane().add(outputPanel, BorderLayout.CENTER);
            outputPanel.setLayout(new BorderLayout(0, 0));

            outputWindow = new JTextArea(25, 40);
            outputWindow.setEditable(false);

            outputPanel.add(progressBar, BorderLayout.NORTH);
            outputPanel.add(new JScrollPane(outputWindow), BorderLayout.CENTER);

            jf.pack();
            jf.toFront();
            repaint();
            jf.setLocationRelativeTo(SchemaBuilder3.this);
            jf.setVisible(true);

            pcName = schemaNameField.getText();
            importSchema(pcName);
            return null;
        }

        protected void importSchema(String schemaName){
            log("IMPORTING");
            btnManageKeys.setEnabled(true);
            btnSubmit.setEnabled(true);
            btnAddOattr.setEnabled(true);

            if(schemaName == null || schemaName.length() == 0){
                JOptionPane.showMessageDialog(jf, "Please fill out Schema Name Field");
                return;
            }

            String descr = descrField.getText();
            if(descr == null || descr.length() == 0){
                descr = schemaName;
                descrField.setText(schemaName);
            }

            String other = otherField.getText();
            if(other == null || other.length() == 0){
                other = schemaName;
                otherField.setText(schemaName);
            }

            //schemaPropField.getText();


            //Creating schema oattr
            progressBar.setString("Creating Object Attributes...");

            for(ArrayList<String> line : containers){
                String message = "Creating Object Attribute: " + line.get(1) + "\n";
                publish(message);
                String sId = sysCaller.getIdOfEntityWithNameAndType(line.get(0), getSchemaType(PM_NODE.OATTR.value));
                //String name = line.get(1);
                System.out.println("\t" + line);
                boolean bRes = util.createOattr(getSchemaType(PM_NODE.OATTR.value), line.get(1), line.get(0), line.get(3));
                if(!bRes){
                    JOptionPane.showMessageDialog(null, "Could not create \"" + line.get(1) + "\"");
                }
            }
            jf.setVisible(false);
        }

        @Override
        public void process(List<String> input){
            for(int i = 0; i < input.size(); i++){
                outputWindow.append(input.get(i));
            }
        }

        @Override
        public void done() {
            progressBar.setString("DONE");
            JOptionPane.showMessageDialog(null, "Schema has been created.");
            jf.setVisible(false);
        }
    }
}

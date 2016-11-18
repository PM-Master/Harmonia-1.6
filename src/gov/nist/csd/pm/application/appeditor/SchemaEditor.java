package gov.nist.csd.pm.application.appeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.WindowConstants;

import java.awt.GridLayout;
import java.awt.GridBagConstraints;

public class SchemaEditor extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private JTable finalTable;
	private DefaultTableModel model, finalModel;
	private DefaultListModel keyListModel;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTable table;
	private ArrayList<JTextField> fields;

	@SuppressWarnings("serial")
	public SchemaEditor(int simport, String sessid, String pid, boolean debug) {
		setTitle("Table Editor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 630, 630);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{619, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{60, 225, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		List<String> cols = new ArrayList<String>();
		String[] colNames = {"Column Name", "Permissions", "PK", "NN", "UQ"};
		cols = Arrays.asList(colNames);
		Vector<String> columnNames = new Vector<String>(cols);
		int numRows = 1;
		model = new DefaultTableModel(columnNames, numRows);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
				BorderFactory.createTitledBorder("Table Information:")));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		contentPane.add(panel, gbc_panel);

		JLabel lblTableName = new JLabel("Name:");
		panel.add(lblTableName);

		textField = new JTextField(20);
		panel.add(textField);
		textField.setColumns(12);

		JLabel lblDescription = new JLabel("Description:");
		panel.add(lblDescription);

		textField_1 = new JTextField();
		panel.add(textField_1);
		textField_1.setColumns(12);

		JLabel lblOther = new JLabel("Other:");
		panel.add(lblOther);

		textField_2 = new JTextField();
		panel.add(textField_2);
		textField_2.setColumns(12);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
				BorderFactory.createTitledBorder("Form Editor:")));

		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.gridwidth = 3;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		contentPane.add(panel_1, gbc_panel_1);
		SpringLayout sl_panel_1 = new SpringLayout();
		panel_1.setLayout(sl_panel_1);

		JPanel panel_3 = new JPanel();
		sl_panel_1.putConstraint(SpringLayout.NORTH, panel_3, 0, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, panel_3, 0, SpringLayout.WEST, panel_1);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, panel_3, 197, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, panel_3, 105, SpringLayout.WEST, panel_1);
		panel_3.setLayout(new GridLayout(0, 1, 0, 0));
		panel_1.add(panel_3);

		JPanel panel_4 = new JPanel();
		sl_panel_1.putConstraint(SpringLayout.NORTH, panel_4, 0, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.WEST, panel_4, 12, SpringLayout.EAST, panel_3);
		sl_panel_1.putConstraint(SpringLayout.SOUTH, panel_4, 197, SpringLayout.NORTH, panel_1);
		sl_panel_1.putConstraint(SpringLayout.EAST, panel_4, -6, SpringLayout.EAST, panel_1);
		panel_4.setLayout(new GridLayout(0, 1, 0, 0));
		panel_1.add(panel_4);
		//panel_3.setLayout(new GridLayout(1, 0, 0, 0));
		int x = 6;

		ArrayList<JLabel> labels = new ArrayList<JLabel>();
		fields = new ArrayList<JTextField>();
		ArrayList<String> names = new ArrayList<String>(Arrays.asList("F_NAME", "L_NAME", "ID", "EMAIL", "JOB", "SSN"));// THIS WILL BE WHERE WE GET NAMES OF THE TEMPLATES AND STORE THEM HERE
		for(int i = 0; i < x; i++){
			labels.add(new JLabel(names.get(i) + ": "));
			panel_3.add(labels.get(i));

			fields.add(new JTextField());
			panel_4.add(fields.get(i));
		}

		JButton btnReset = new JButton("Reset");
		GridBagConstraints gbc_btnReset = new GridBagConstraints();
		gbc_btnReset.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnReset.insets = new Insets(0, 0, 5, 5);
		gbc_btnReset.gridx = 1;
		gbc_btnReset.gridy = 2;
		contentPane.add(btnReset, gbc_btnReset);

		JButton btnApply = new JButton("Add To Table");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnApply.insets = new Insets(0, 0, 5, 0);
		gbc_btnApply.gridx = 2;
		gbc_btnApply.gridy = 2;
		btnApply.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String fName = fields.get(0).getText();
				String lName = fields.get(1).getText();
				String id = fields.get(2).getText();
				String email = fields.get(3).getText();
				String job = fields.get(4).getText();
				String ssn = fields.get(5).getText();
				
				Vector<String> vals = new Vector<String>();
				vals.add(fName);
				vals.add(lName);
				vals.add(id);
				vals.add(email);
				vals.add(job);
				vals.add(ssn);
				
				
				model.insertRow(model.getRowCount(), vals);
				clearAllFields();
			}
		});
		contentPane.add(btnApply, gbc_btnApply);



		final JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridheight = 2;
		gbc_panel_2.gridwidth = 3;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 3;
		panel_2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0),
				BorderFactory.createTitledBorder("Table:")));
		contentPane.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_2.add(scrollPane, gbc_scrollPane);
		Vector<String> y = new Vector<String>(names);
		model = new DefaultTableModel(y, 0);
		table = new JTable(model);
		scrollPane.setViewportView(table);


		keyListModel = new DefaultListModel();

		DefaultListModel listModel = new DefaultListModel();


		JButton btnCreateTable = new JButton("Create Table");
		GridBagConstraints gbc_btnCreateTable = new GridBagConstraints();
		gbc_btnCreateTable.gridx = 2;
		gbc_btnCreateTable.gridy = 5;
		contentPane.add(btnCreateTable, gbc_btnCreateTable);

		//scrollPane_1.setViewportView(finalTable);

		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

	}
	
	public void clearAllFields(){
		for(int i = 0; i < fields.size(); i++){
			fields.get(i).setText("");
		}
	}
	
	static String sessid;
	static String pid;
	static int simport;
	static String recname;
	static boolean debug;

	public static void createGUI(){
		SchemaEditor emp = new SchemaEditor(simport, sessid, pid, debug);
		emp.setVisible(true);
	}

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-session")) {
				sessid = args[++i];
			} else if (args[i].equals("-process")) {
				pid = args[++i];
			} else if (args[i].equals("-simport")) {
				simport = Integer.valueOf(args[++i]).intValue();
			} else if (args[i].equals("-debug")) {
				debug = true;
			} else {
				recname = args[i];
			}
		}
		if (sessid == null) {
			System.out.println("This application must run within a Policy Machine session!");
			System.exit(-1);
		}
		if (pid == null) {
			System.out.println("This application must run in a Policy Machine process!");
			System.exit(-1);
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				createGUI();
			}
		});
	}
}
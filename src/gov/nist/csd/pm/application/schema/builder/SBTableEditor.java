package gov.nist.csd.pm.application.schema.builder;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SBTableEditor extends JFrame {

	private JPanel contentPane;
	private JTextField nameField;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private int gridX, gridY;
	private SchemaBuilder3 builder;

	private ArrayList<JTextField> columnFields = new ArrayList<JTextField>();
	private ArrayList<JButton> cancelButtons = new ArrayList<JButton>();

	/**
	 * Create the frame.
	 */
	public SBTableEditor(SchemaBuilder3 sb) {
		builder = sb;

		setTitle("Create Table");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//setBounds(100, 100, 575, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel tablePanel = new JPanel();
		tablePanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(tablePanel, BorderLayout.NORTH);

		JLabel lblTableName = new JLabel("Table Name");
		tablePanel.add(lblTableName);

		nameField = new JTextField();
		tablePanel.add(nameField);
		nameField.setColumns(20);

		JPanel columnPanel = new JPanel();
		columnPanel.setBorder(new TitledBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null), "Columns", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		columnPanel.setPreferredSize(new Dimension(columnPanel.getWidth(), 200));
		contentPane.add(columnPanel, BorderLayout.CENTER);
		columnPanel.setLayout(new BorderLayout(0, 0));

		final JPanel addColumnPanel = new JPanel();
		columnPanel.add(new JScrollPane(addColumnPanel), BorderLayout.CENTER);
		GridBagLayout gbl_addColumnPanel = new GridBagLayout();
		gbl_addColumnPanel.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_addColumnPanel.rowHeights = new int[]{0, 0, 0};
		gbl_addColumnPanel.columnWeights = new double[]{1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_addColumnPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		addColumnPanel.setLayout(gbl_addColumnPanel);

		JLabel lblColumnName = new JLabel("Column Name");
		GridBagConstraints gbc_lblColumnName = new GridBagConstraints();
		gbc_lblColumnName.insets = new Insets(0, 0, 5, 5);
		gbc_lblColumnName.gridx = 0;
		gbc_lblColumnName.gridy = 0;
		addColumnPanel.add(lblColumnName, gbc_lblColumnName);
		
		JButton btnX = new JButton("X");
		btnX.setVisible(false);
		GridBagConstraints gbc_btnX = new GridBagConstraints();
		gbc_btnX.gridx = 3;
		gbc_btnX.gridy = 0;
		addColumnPanel.add(btnX, gbc_btnX);

		JPanel columnButtonPanel = new JPanel();
		columnPanel.add(columnButtonPanel, BorderLayout.SOUTH);

		JButton btnAddColumn = new JButton("Add Column");
		btnAddColumn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				gridY++;
				System.out.println(gridY);
				JTextField nameFld = new JTextField();
				GridBagConstraints gbc_textField_5 = new GridBagConstraints();
				gridX = 0;
				gbc_textField_5.insets = new Insets(0, 0, 0, 5);
				gbc_textField_5.fill = GridBagConstraints.HORIZONTAL;
				gbc_textField_5.gridx = gridX;
				gbc_textField_5.gridy = gridY;
				addColumnPanel.add(nameFld, gbc_textField_5);
				nameFld.setColumns(10);
				
				
				columnFields.add(nameFld);
				final JButton cancelButton = new JButton("X");
				GridBagConstraints gbc_btnX = new GridBagConstraints();
				gridX = 3;
				gbc_btnX.gridx = gridX;
				gbc_btnX.gridy = gridY;
				addColumnPanel.add(cancelButton, gbc_btnX);
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						int i = cancelButtons.indexOf(cancelButton);
						JTextField field = columnFields.get(i);
						addColumnPanel.remove(field);
						columnFields.remove(i);
						cancelButtons.remove(cancelButton);
						addColumnPanel.remove(cancelButton);
						addColumnPanel.revalidate();
						addColumnPanel.repaint();
						if(columnFields.size()==0)gridY=0;
					}
				});
				cancelButtons.add(cancelButton);
				addColumnPanel.revalidate();
				addColumnPanel.repaint();
			}
		});
		columnButtonPanel.add(btnAddColumn);

		JPanel buttonPanel = new JPanel();
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = nameField.getText();
                System.out.println("SBTableEditor name: " + name);
				if(name != null && name.length() != 0){
					ArrayList<String> data = new ArrayList<String>();
					data.add(name);
                    System.out.println("data in SBTE: " + data);
                    System.out.println("comlumnFields length = " + columnFields.size());
					for (JTextField field : columnFields) {
						data.add(field.getText());
					}
                    System.out.println("builder.create() called");
                    builder.create(data);
				}
                setVisible(false);
			}
		});
		buttonPanel.add(btnCreate);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPanel.add(btnClose);

		setLocationRelativeTo(builder);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
	}
}

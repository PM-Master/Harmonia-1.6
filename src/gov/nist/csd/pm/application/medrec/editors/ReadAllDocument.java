package gov.nist.csd.pm.application.medrec.editors;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;


public class ReadAllDocument extends JFrame {

	private JPanel contentPane;
	private JTextArea textArea;
	private String appName;
	private Editor editor;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		//		EventQueue.invokeLater(new Runnable() {
		//			public void run() {
		//				try {
		//					ReadAllDocument frame = new ReadAllDocument();
		//					//frame.setVisible(true);
		//				} catch (Exception e) {
		//					e.printStackTrace();
		//				}
		//			}
		//		});
	}

	/**
	 * Create the frame.
	 */
	public ReadAllDocument(String appName, Editor editor) {
		this.appName = appName;
		this.editor = editor;
		
		setBounds(100, 100, 450, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JButton createNew = new JButton("Create New " + appName + " Entry");
		createNew.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				ReadAllDocument.this.editor.setVisible(true);
			}

		});
		
		JButton search = new JButton("Search");
		search.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				ReadAllDocument.this.editor.search();
			}

		});
		
		JButton refresh = new JButton("Refresh");
		refresh.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				ReadAllDocument.this.clearArea();
				ReadAllDocument.this.editor.buildReadAllDocument(ReadAllDocument.this);
			}

		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(createNew);
		buttonPanel.add(search);
		buttonPanel.add(refresh);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		setLocationRelativeTo(null);

	}
	
	public void clearArea(){
		textArea.setText("");
	}

	public void write(String input) {
		textArea.append(input);
	}
}

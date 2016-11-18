package gov.nist.csd.pm.application.schema.tableeditor;

import static gov.nist.csd.pm.common.util.Generators.generateRandomName;

import gov.nist.csd.pm.application.schema.tableeditor.Template;
import gov.nist.csd.pm.application.schema.utilities.Utilities;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.browser.ObjectBrowser;
import gov.nist.csd.pm.common.browser.PmGraphType;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.net.Packet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.border.*;

public class RecordEditor extends JPanel{

	private List<String> columns;
	private ArrayList<ArrayList<JTextArea>> fields;
	private SysCaller sysCaller;
	private Template activeTemplate;
	private String sCrtRec;
	private Utilities util;
	private List<String> comps;
	private TableEditor tableEditor;
	private List<String> keyCols;

	public RecordEditor(TableEditor editor, Utilities u, SysCaller sys){
		tableEditor = editor;
		sysCaller = sys;
		activeTemplate = editor.getActiveTemplate();
		util = u;
		columns = getColumns();
		keyCols = activeTemplate.getKeys();
		createGUI();
	}

	public RecordEditor(TableEditor editor, SysCaller sys, String aCrtRec,
						Vector<String> data, List<String> comps2, Utilities u){
		tableEditor = editor;
		sysCaller = sys;
		activeTemplate = editor.getActiveTemplate();
		util = u;
		sCrtRec = aCrtRec;
		getObjectNames(sCrtRec);
		columns = getColumns();
		keyCols = activeTemplate.getKeys();
		comps = comps2;
		createGUI();
	}

	/**
	 * Get the template containers
	 * @return
	 */
	private List<String> getColumns(){
		List<String> cols = activeTemplate.getConts();
		cols.remove("Record Name");
		return cols;
	}

	private ArrayList<JButton> addButtons;
	protected void createGUI(){
		setSize(500, 500);
		setLayout(new BorderLayout(5, 5));

		addButtons = new ArrayList<JButton>();
		fields = new ArrayList<ArrayList<JTextArea>>();

		GridLayout grid = new GridLayout(1, columns.size());
		grid.setHgap(5);

		JPanel panel = new JPanel();
		panel.setLayout(grid);

		System.out.println(columns);
		System.out.println(comps);
		if(comps != null){
			for(int i = 0; i < comps.size(); i++){
				final JPanel panel1 = new JPanel();
				panel1.setLayout(new BorderLayout(5, 5));
				final JPanel fieldPanel = new JPanel();
				List<String[]> members = sysCaller.getMembersOf(
						comps.get(i),
						sysCaller.getIdOfEntityWithNameAndType(comps.get(i), PM_NODE.OATTR.value),
						PM_NODE.OATTR.value,
						SysCaller.PM_VOS_PRES_USER);
				System.out.println(members);
				for(String[] mem : members){
					String type = mem[0];
					String name = mem[2];
					System.out.println(type + " : " + name);
				}
				BoxLayout box = new BoxLayout(fieldPanel, BoxLayout.X_AXIS);
				fieldPanel.setLayout(box);
				ArrayList<JTextArea> memberFields = new ArrayList<JTextArea>();
				System.out.println(members.size());
				for(int j = 0; j < members.size(); j++){
					JTextArea f = new JTextArea(2, 10){
						@Override
						public void cut(){
						}
						@Override
						public void copy() {
						}
					};

					String objName = members.get(j)[2];
					String value = openObject(objName, columns.get(i));
					System.out.println("Value in RecordEditor: " + value);
					f.append(value);
					fieldPanel.add(new JScrollPane(f));
					fieldPanel.add(Box.createRigidArea(new Dimension(2, 0)));
					memberFields.add(f);
				}
				fields.add(memberFields);

				JPanel addBtnPanel = new JPanel();
				final JButton btnAddField = new JButton("Add");
				addBtnPanel.add(btnAddField);
				addButtons.add(btnAddField);
				btnAddField.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						JTextArea a = new JTextArea(3, 10);
						fieldPanel.add(new JScrollPane(a));
						fieldPanel.repaint();
						fieldPanel.revalidate();
						for(int i = 0; i < addButtons.size(); i++){
							if(addButtons.get(i) == btnAddField){
								System.out.println("found right panel");
								fields.get(i).add(a);
							}
						}
					}
				});
				panel1.add(new JScrollPane(fieldPanel), BorderLayout.CENTER);
				panel1.add(addBtnPanel, BorderLayout.SOUTH);
				//fields.add(memberFields);
				//panel1.addAll(new ArrayList<JTextField>(Arrays.asList(memberFields)));
				panel1.setBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(new TitledBorder(columns.get(i))),
								BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
				panel.add(panel1);
			}
		}else{
			//opening new record so just one area per panel
			for(int i = 0; i < columns.size(); i++){
				final JPanel panel1 = new JPanel();
				final JPanel fieldPanel = new JPanel();
				fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));//new GridLayout(members.size(), 1));
				panel1.setLayout(new BorderLayout(5, 5));//new BoxLayout(panel1, BoxLayout.Y_AXIS));

				JTextArea f = new JTextArea(3, 10){
					@Override
					public void cut(){
					}
					@Override
					public void copy() {
					}
				};
				fieldPanel.add(new JScrollPane(f));
				fields.add(new ArrayList<JTextArea>(Arrays.asList(new JTextArea[]{f})));

				JPanel addBtnPanel = new JPanel();
				final JButton btnAddField = new JButton("Add");
				addBtnPanel.add(btnAddField);
				addButtons.add(btnAddField);
				btnAddField.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						JTextArea a = new JTextArea(3, 10){
							@Override
							public void cut(){
							}
							@Override
							public void copy() {
							}
						};
						//a.setAlignmentX(Component.CENTER_ALIGNMENT);
						fieldPanel.add(new JScrollPane(a));
						fieldPanel.repaint();
						fieldPanel.revalidate();
						for(int i = 0; i < addButtons.size(); i++){
							if(addButtons.get(i) == btnAddField){
								System.out.println("found right panel");
								fields.get(i).add(a);
							}
						}
					}
				});
				panel1.add(new JScrollPane(fieldPanel), BorderLayout.CENTER);
				panel1.add(addBtnPanel, BorderLayout.SOUTH);
				panel1.setBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createTitledBorder(new TitledBorder(columns.get(i))),
								BorderFactory.createBevelBorder(BevelBorder.LOWERED)
						)
				);
				panel.add(panel1);
			}
		}
		add(panel, BorderLayout.CENTER);

		JPanel btnPanel = new JPanel();
		JButton submit = new JButton("Submit");
		submit.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				submit();
			}

		});
		btnPanel.add(submit);

		JButton cancel = new JButton("Close");
		cancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}

		});
		//btnPanel.add(cancel); TODO I commented this out because closing it in the TableEditor caused it to glitch out

		add(btnPanel, BorderLayout.SOUTH);
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
		String value = (String)props.get(cont);
		System.out.println(value);
		return value;
	}

	private ArrayList<String> getObjectNames(String recName) {
		Packet recInfo = sysCaller.getRecordInfo(sysCaller.getIdOfEntityWithNameAndType(recName, SysCaller.PM_NODE_OATTR));

		String sLine = recInfo.getStringValue(2);
		int nComp = Integer.valueOf(sLine).intValue();

		ArrayList<String> compNameList = new ArrayList<String>();
		for (int j = 0; j < nComp; j++) {
			sLine = recInfo.getStringValue(3 + j);
			String[] pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
			compNameList.add(pieces[0]);
		}
		return compNameList;
	}

	/**
	 * for creating new records
	 */
	public void submit(){
		Long s1 = System.currentTimeMillis();
		if(sCrtRec == null){
			//Construct record name using keys
			String tplName = activeTemplate.getTplName();
			String tplId = activeTemplate.getTplId();
			String sRecordName = UUID.randomUUID().toString();
			/*for(int i = 0; i < columns.size(); i++){
				if(keyCols.contains(columns.get(i))){
					ArrayList<JTextArea> areaArr = fields.get(i);
					//for each field
					for(JTextArea j : areaArr){
						String value = j.getText();
						sRecordName += value + "-";
					}
				}
			}
			sRecordName += tplName;*/
			System.out.println("sRecordName: " + sRecordName);
			ArrayList<String> objNames = new ArrayList<String>();
			//creating the components of the record
			for(int i = 0; i < columns.size(); i++){
				String oattrName = sRecordName + "_" + columns.get(i);
				if(!util.addOattr(oattrName, oattrName, oattrName,
						sysCaller.getIdOfEntityWithNameAndType(columns.get(i), PM_NODE.OATTR.value), "name=" + oattrName)){
					JOptionPane.showMessageDialog(this, "Could not create record component " + oattrName);
					return;
				}

				objNames.add(oattrName);

				//create the objects and assign it to the oa
				ArrayList<JTextArea> areaArr = fields.get(i);

				//for each field
				for(JTextArea j : areaArr){
					String sObjName = generateRandomName(4);
					String sContainer = "b|" + columns.get(i);
					String sHandle = sysCaller.createObject3(sObjName, "File", "doc", sContainer, "File write",
							null, null, null, null);
					if(sHandle == null){
						System.out.println(sysCaller.getLastError());
						JOptionPane.showMessageDialog(this, "Error in creating object " + sObjName
								+ " in container " + sContainer , "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}

					if(!sysCaller.assignObjToContainer(sObjName, oattrName)){
						JOptionPane.showMessageDialog(this, "Could not assign obj to component",
								"ERROR", JOptionPane.ERROR_MESSAGE);
					}

					//Set properties for the object and then write it
					Properties props = new Properties();
					String value = j.getText();

					String colName = columns.get(i);
					props.put(colName, value);

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(baos, true);

					try {
						props.store(baos, null);
						pw.print(value);
						pw.close();
						byte[] buf = baos.toByteArray();
						if (buf == null) {
							JOptionPane.showMessageDialog(this, "Buffer is null, could not complete action.");
						}
						int len = sysCaller.writeObject3(sHandle, buf);
						if (len < 0) {
							JOptionPane.showMessageDialog(this, sysCaller.getLastError(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}

						sysCaller.closeObject(sHandle);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			String sComponents = "";
			for(int i = 0; i < objNames.size()-1; i++){
				sComponents += objNames.get(i) + SysCaller.PM_FIELD_DELIM;
			}
			sComponents += objNames.get(objNames.size()-1);

			HashMap<String, String> keyMap = new HashMap<String, String>();
			//String sLine = res.getStringValue(2);
			//pieces = sLine.split(SysCaller.PM_FIELD_DELIM);
			for(int j = 0; j < keyCols.size(); j++){
				for(int i = 0; i < columns.size(); i++){
					if(columns.get(i).equals(keyCols.get(j))){
						//we have a key
						System.out.println(columns.get(i) + "=" + fields.get(i).get(0).getText());
						keyMap.put(columns.get(i), fields.get(i).get(0).getText());
					}
				}
			}

			System.out.println(tplName);
			System.out.println(tplId);
			System.out.println(sComponents);

			ObjectBrowser objectBrowser = new ObjectBrowser(tableEditor, sysCaller, "Table Editor");
			objectBrowser.disableObjField();
			objectBrowser.insertObjName(sRecordName);
			objectBrowser.pack();

			Long e = System.currentTimeMillis();
			System.out.println("TIME: " + (double) (e - s1) / 1000);
			int ret = objectBrowser.showSaveAsDialog();
			if (ret != ObjectBrowser.PM_OK) return;

			// Prepare for object creation.
			//String sInputRecName = objectBrowser.getObjName();
			String sInputRecConts = objectBrowser.getContainers();
			System.out.println(sInputRecConts);

			String[] pieces = sInputRecConts.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
			for(String s : pieces){
				System.out.println(s);
			}
			String contType = pieces[0];
			String contName = pieces[1];

			System.out.println(contType);
			System.out.println(contName);

			if(contType == null || contName == null){
				JOptionPane.showMessageDialog(this, "Invalid container selected");
				submit();
			}
			System.out.println("submitting records with keys: " + keyMap);
			Packet res = sysCaller.createRecord(sRecordName, contName,
					contType, tplId, sComponents, keyMap);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "could not create record.");
				return;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return;
			}

			for(Entry<String, String> entry : keyMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				sysCaller.addPropToOattr(sysCaller.getIdOfEntityWithNameAndType(sRecordName, PM_NODE.OATTR.value), "no", key, value);
			}

			System.out.println(objNames);
			for(int i = 0; i < objNames.size(); i++){
				boolean bRes = sysCaller.assignObjToContainer(objNames.get(i),
						sRecordName);
				if (!bRes) {
					JOptionPane.showMessageDialog(this, sysCaller.getLastError());
					return;
				}
			}
		}else{
            columns.remove("Record Name");
            Collections.sort(columns);
            Collections.sort(comps);
			for(int i = 0; i < comps.size(); i++){
				String colName = columns.get(i);
				String sOattrName = comps.get(i);
				System.out.println(colName + "<>" + sOattrName);
				List<String[]> members = sysCaller.getMembersOf(sOattrName,
						sysCaller.getIdOfEntityWithNameAndType(sOattrName, PM_NODE.OATTR.value),
						PM_NODE.OATTR.value,
						SysCaller.PM_VOS_PRES_USER);
				for(int j = 0; j < members.size(); j++){
					String sObjName = members.get(j)[2];
					String sHandle = sysCaller.openObject3(sObjName, "File write");
					if (sHandle == null) {
						JOptionPane.showMessageDialog(this, sysCaller.getLastError());
						return;
					}

					String sValue = fields.get(i).get(j).getText();
					Properties props = new Properties();
					props.put(colName, sValue);

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(baos, true);

					try {
						props.store(baos, null);
						//pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}");
						pw.print(sValue);
						pw.close();
						byte[] buf = baos.toByteArray();
						if (buf == null) {
							//JOptionPane.showMessageDialog(this, "NO");
							return;
						}
						int len = sysCaller.writeObject3(sHandle, buf);
						if (len < 0) {
							JOptionPane.showMessageDialog(this, sysCaller.getLastError() + "\n\n" + columns.get(i),
									"Error", JOptionPane.ERROR_MESSAGE);
						}

						sysCaller.closeObject(sHandle);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				//TODO creating the new objects -- does not work
				ArrayList<JTextArea> areaArr = fields.get(i);
				int start = members.size();
				for(int j = start; j < areaArr.size(); j++){
					String sObjName = generateRandomName(4);
					String sContainer = "b|" + sOattrName;
					String sHandle = sysCaller.createObject3(sObjName, "File", "doc", sContainer, "File write",
							null, null, null, null);
					if(sHandle == null){
						JOptionPane.showMessageDialog(this, "Error in creating object " + sObjName
								+ " in container " + sContainer , "Error", JOptionPane.ERROR_MESSAGE);
						//return;
					}
					System.out.println(sObjName);
					System.out.println(sContainer);
					//objNames.add(sObjName);
					//Set properties for the object and then write it
					Properties props = new Properties();
					String value = areaArr.get(j).getText();
					System.out.println(value);

					colName = columns.get(i);
					props.put(colName, value);

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintWriter pw = new PrintWriter(baos, true);

					try {
						props.store(baos, null);
						//pw.print("{\\rtf1\\ansi{\\fonttbl\\f0\\fnil Monospaced;}");
						pw.print(value);
						pw.close();
						byte[] buf = baos.toByteArray();
						if (buf == null) {
							JOptionPane.showMessageDialog(this, "Buffer is null, could not complete action.");
						}
						int len = sysCaller.writeObject3(sHandle, buf);
						if (len < 0) {
							JOptionPane.showMessageDialog(this, sysCaller.getLastError(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}

						sysCaller.closeObject(sHandle);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		JOptionPane.showMessageDialog(this, "Record has been submitted");
		tableEditor.refreshModel(tableEditor.getLasSelNode());
	}

	public static void main(String[] args){

	}
}

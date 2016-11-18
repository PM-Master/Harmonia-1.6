/*
 * OsConfigEditor.java
 *
 * Created on October 17, 2005, 12:10 PM
 */
package gov.nist.csd.pm.user;


import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import gov.nist.csd.pm.common.util.swing.SwingShortcuts;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class OsConfigEditor extends JDialog {
    public static final String TEXTFIELD_NAME_EXT = "_TextField";

    /**
	 * @uml.property  name="_applicationManager"
	 * @uml.associationEnd  
	 */
    private final ApplicationManager _applicationManager;
    
    /**
	 * @uml.property  name="okButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JButton okButton;
    /**
	 * @uml.property  name="cancelButton"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JButton cancelButton;
    //RM - removed all instances of confirmBox and it's usages.
    //I couldn't find it being used anywhere except
    //to set itself whenever this window shows itself.
    //final JCheckBox confirmBox;

    public static class FileChoosingAction extends AbstractAction {

        private static final JFileChooser jfc = new JFileChooser();

        static {
            jfc.setFileFilter(new JarFilter());
            jfc.setMultiSelectionEnabled(true);
        }
        private final Component _topLevelAncestor;
        private final JTextComponent _fileChosenComp;

        public FileChoosingAction(JTextComponent textComp) {
            _fileChosenComp = checkNotNull(textComp);
            _topLevelAncestor = _fileChosenComp.getTopLevelAncestor();
        }

        private static Function<File, String> getPathFunction(){
            return new Function<File,String>(){
                public String apply(File file){
                    return file.getPath();
                }
            };

        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int returnVal = jfc.showOpenDialog(_topLevelAncestor);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = jfc.getSelectedFiles();
                List<String> filePaths = Lists.transform(Arrays.asList(selectedFiles), getPathFunction());
                String filePathString =  Joiner.on("; ").join(filePaths);
                _fileChosenComp.setText(filePathString);
            }
        }
    }

    private static java.util.List<String> _applicationNames;
    /**
	 * @uml.property  name="_originalPathValues"
	 * @uml.associationEnd  qualifier="appName:java.lang.String java.lang.String"
	 */
    private java.util.Map<String, String> _originalPathValues = newHashMap();


    public OsConfigEditor(Window root, ApplicationManager mgr) {
        super(root, JDialog.ModalityType.APPLICATION_MODAL);
        setTitle("Configuration Editor");
        _applicationManager = checkNotNull(mgr);
        _applicationNames = _applicationManager.getInstalledApplications();
        System.out.println("List is installed applications " + Joiner.on(", ").join(_applicationNames));
        JLabel promptLabel = new JLabel("Please enter the absolute paths of the following applications:");
        
        final Container contentPane = this.getContentPane();
        contentPane.setLayout(new MigLayout("","[align label][grow, fill, 100:200:][]",""));

        contentPane.add(promptLabel, "span");

        for(String appName : _applicationNames){
            String appPath = _applicationManager.getApplicationPathString(appName);
            _originalPathValues.put(appName, appPath);
            JTextField pathInputTextField = new JTextField(25);
            pathInputTextField.setText(appPath);
            pathInputTextField.setName(appName + TEXTFIELD_NAME_EXT);
            JLabel appNameLabel = new JLabel(appName);
            contentPane.add(appNameLabel, "label, newline");
            contentPane.add(pathInputTextField, "");
            appNameLabel.setLabelFor(pathInputTextField);
            JButton appChooserButton = new JButton("...");
            contentPane.add(appChooserButton, "");
            appChooserButton.addActionListener(new FileChoosingAction(pathInputTextField));
        }

        

        /*
        JLabel prompt2Label = new JLabel("Please check the box for user request confirmation:");
        contentPane.add(prompt2Label, "newline, span");
        confirmBox = new JCheckBox("Ask user to confirm?", true);
        contentPane.add(confirmBox, "newline, span");
        */



        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            private final String PATH_SET_ERROR_MSG_FORMAT = "There was a problem updating this path: %s\n" +
                    "Would you like to continue updating application paths?";

            private String getErrorMessage(Exception e){
                return String.format(PATH_SET_ERROR_MSG_FORMAT, e.getMessage());
            }

            public void actionPerformed(ActionEvent event) {
                //TODO - RM Update the configuration in the application manager based on what we get here.
                List<JTextField> textFields = SwingShortcuts.getComponentsWithBaseClass(contentPane, JTextField.class);
                for(JTextField textField : textFields){
                    String name = textField.getName();
                    if(name.endsWith(TEXTFIELD_NAME_EXT)){
                        String appName = name.substring(0, name.indexOf(TEXTFIELD_NAME_EXT));
                        String appPath = textField.getText();
                        if(!_originalPathValues.get(appName).equals(appPath)) {
                            try {
                                _applicationManager.setApplicationPath(appName, appPath);
                            } catch (ApplicationUpdateException e) {
                                int result = JOptionPane.showConfirmDialog(OsConfigEditor.this, getErrorMessage(e), "Path Set Error", JOptionPane.YES_NO_OPTION);
                                if(JOptionPane.NO_OPTION == result){
                                    break;
                                }
                            }
                        }
                    }
                }
                OsConfigEditor.this.setVisible(false);
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });

        contentPane.add(okButton, "split 2, span, center, newline");
        contentPane.add(cancelButton);

        

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        
    }

    
    /*
    public void setUserConfirm(boolean bConfirm) {
        confirmBox.setSelected(bConfirm);
    }
    */
}

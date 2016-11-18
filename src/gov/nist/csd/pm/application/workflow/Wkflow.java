package gov.nist.csd.pm.application.workflow;

import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.UserBrowser;
import gov.nist.csd.pm.common.browser.UserBrowserListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:01 $
 * @since 1.5
 */
public class Wkflow extends JFrame implements ActionListener {
    public static final String BROWSE_ACTION_CMD = "Browse";
    public static final String CANCEL_ACTION_CMD = "Cancel";
    public static final String COPY_ACTION_CMD = "Copy";
    public static final String SIGN_ACTION_CMD = "Sign";
    public static final String SUBMIT_ACTION_CMD = "Submit";

    public static final String WORKFLOW_APP_NAME = "Workflow App";
    public static final String WFLOW_IN_POLICY_MACHINE_PROCESS_MESSAGE = String.format("The %s must run in a Policy Machine process!", WORKFLOW_APP_NAME);
    public static final String WFLOW_IN_POLICY_MACHINE_SESSION_MESSAGE = String.format("The %s must run in a Policy Machine session!", WORKFLOW_APP_NAME);
    public static final String WORKFLOW_APP_TITLE = String.format("%s v1.0", WORKFLOW_APP_NAME);
    
    //Workflow menu items
    public static final String MENU_ITEM_SEND_FORWARD = "send forward";
    public static final String MENU_ITEM_SEND_BACK = "send back";
    public static final String WORKFLOW_PREFIX = "WKF";

    // Name of the current user.
    /**
	 * @uml.property  name="sCrtUserName"
	 */
    String sCrtUserName = null;
    // Name and handle for the virtual object currently open.
    /**
	 * @uml.property  name="sCrtObjName"
	 */
    String sCrtObjName;
    /**
	 * @uml.property  name="sCrtObjHandle"
	 */
    String sCrtObjHandle;
    // The SysCaller instance.
    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    SysCaller sysCaller;
    /**
	 * @uml.property  name="sProcessId"
	 */
    String sProcessId;
    /**
	 * @uml.property  name="lblType"
	 * @uml.associationEnd  
	 */
    JLabel lblType;
    /**
	 * @uml.property  name="tfNumber"
	 * @uml.associationEnd  
	 */
    JTextField tfNumber;
    /**
	 * @uml.property  name="tfRequester"
	 * @uml.associationEnd  
	 */
    JTextField tfRequester;
    /**
	 * @uml.property  name="tfItem"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    JTextField[] tfItem;
    /**
	 * @uml.property  name="tfQty"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    JTextField[] tfQty;
    /**
	 * @uml.property  name="tfUnitPrice"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    JTextField[] tfUnitPrice;
    /**
	 * @uml.property  name="tfPrice"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    JTextField[] tfPrice;
    /**
	 * @uml.property  name="tfOfficial"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    JTextField[] tfOfficial;
    /**
	 * @uml.property  name="browseOfficial"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    JButton[] browseOfficial;
    /**
	 * @uml.property  name="browsePanel"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    JPanel[] browsePanel;
    /**
	 * @uml.property  name="tfSignature"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    JLabel[] tfSignature;
    /**
	 * @uml.property  name="sSignature" multiplicity="(0 -1)" dimension="1"
	 */
    String[] sSignature;
    /**
	 * @uml.property  name="butSign"
	 * @uml.associationEnd  multiplicity="(0 -1)"
	 */
    JButton[] butSign;

    public Wkflow(int nSimPort, String sSessId, String sProcId, boolean bDebug) {
        super(WORKFLOW_APP_TITLE);
        this.sProcessId = sProcId;
        //IOC Candidate Syscaller
        sysCaller = new SysCallerImpl(nSimPort, sSessId, sProcId, bDebug, WORKFLOW_PREFIX);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                terminate(0);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase(SUBMIT_ACTION_CMD)) {
            submitForm();
        } else if (e.getActionCommand().equalsIgnoreCase(CANCEL_ACTION_CMD)) {
            cancelForm();
        } else if (e.getActionCommand().equalsIgnoreCase(SIGN_ACTION_CMD)) {
            signForm(e);
        } else if (e.getActionCommand().equalsIgnoreCase(COPY_ACTION_CMD)) {
            copyForm();
        } else if (e.getActionCommand().equalsIgnoreCase(BROWSE_ACTION_CMD)) {
            browse(e);
        }
    }

    private void terminate(int exitCode) {
        sysCaller.exitProcess(sProcessId);
        System.exit(exitCode);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    //Were creating the UI and populating the UI with file data at the same time
    //This is bad practice.
    //TODO Decouple view creation with view population.
    protected void createUI(byte[] buf) {
//    setSize(600, 500);
        center();

        // Get the current user.
        sCrtUserName = sysCaller.getSessionUser();
        if (sCrtUserName == null) {
            JOptionPane.showMessageDialog(Wkflow.this, "Cannot determine the session user: "
                    + sysCaller.getLastError());
            return;
        }
        //JOptionPane.showMessageDialog(Wkflow.this, "You are user " + sCrtUserName);
        //Creating a data parser should not be in the view creation or population parts.
        //A data parser should be passed into the data population part.
        ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0, buf.length);
        InputStreamReader isr = new InputStreamReader(bais);
        BufferedReader br = new BufferedReader(isr);
        String sLine;
        try {
            // First line should contain the type of form (purchase order, leave request, etc.)
            //Lots of repetition: read line, check line, process line.
            //Only the process part is unique

            //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
            sLine = br.readLine();
            if (!sLine.startsWith("Type:")) {
                JOptionPane.showMessageDialog(Wkflow.this, "Form type missing in .wkf file!");
                return;
            }
            String sRest = sLine.substring(sLine.indexOf("Type:") + "Type:".length());
            //END EXTRACT
            
            lblType = new JLabel(sRest);

            // Next line should contain the form number.
            //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
            sLine = br.readLine();
            if (!sLine.startsWith("Number:")) {
                JOptionPane.showMessageDialog(Wkflow.this, "Form number missing in .wkf file!");
                return;
            }
            sRest = sLine.substring(sLine.indexOf("Number:") + "Number:".length());
            //END EXTRACT
            //VIEW CREATION
            JLabel lblNumber = new JLabel("Number:");
            tfNumber = new JTextField(10);
            //END VIEW CREATION

            //DATA POPULATION
            if (sRest != null && sRest.length() > 0) {
                tfNumber.setText(sRest);
                tfNumber.setEditable(false);
            }
            //END DATA POPULATION

            //VIEW CREATION
            JPanel pane1 = new JPanel();
            pane1.setLayout(new GridLayout(1, 2));
            pane1.add(lblNumber);
            pane1.add(tfNumber);
            pane1.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            //END VIEW CREATION

            // Next line should contain the requester.
            //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
            sLine = br.readLine();
            if (!sLine.startsWith("Requester:")) {
                JOptionPane.showMessageDialog(Wkflow.this, "Requester missing in .wkf file!");
                return;
            }
            sRest = sLine.substring(sLine.indexOf("Requester:") + "Requester:".length());
            //END EXTRACT
            //VIEW CREATION
            JLabel lblRequester = new JLabel("Requester:");
            tfRequester = new JTextField(10);
            //END VIEW CREATION

            //DATA POPULATION
            if (sRest != null && sRest.length() > 0) {
                tfRequester.setText(sRest);
                tfRequester.setEditable(false);
            }
            //DATA POPULATION

            //VIEW CREATION
            JPanel pane2 = new JPanel();
            pane2.setLayout(new GridLayout(1, 2));
            pane2.add(lblRequester);
            pane2.add(tfRequester);
            pane2.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));


            // Items.
            JLabel lblItem = new JLabel("Item");
            JLabel lblQty = new JLabel("Quantity");
            JLabel lblUnitPrice = new JLabel("Unit price");
            JLabel lblPrice = new JLabel("Price");

            JPanel itHdrPane = new JPanel();
            itHdrPane.setLayout(new GridLayout(1, 4));
            itHdrPane.add(lblItem);
            itHdrPane.add(lblQty);
            itHdrPane.add(lblUnitPrice);
            itHdrPane.add(lblPrice);

            tfItem = new JTextField[5];
            tfQty = new JTextField[5];
            tfUnitPrice = new JTextField[5];
            tfPrice = new JTextField[5];
            //END VIEW CREATION
            boolean bItemsPresent = false;
            //COMMENT - View is populated statically, allowing no room for additional data/signators
            //COMMENT - Replace with dynamically allocated views, updated during data population
            //MAKE DYNAMIC
            for (int i = 0; i < 5; i++) {
                // Item field
                sLine = br.readLine();
                tfItem[i] = new JTextField(10);
                //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
                if (!sLine.startsWith("Item:")) {
                    JOptionPane.showMessageDialog(Wkflow.this, "Item missing in .wkf file!");
                    return;
                }
                sRest = sLine.substring(sLine.indexOf("Item:") + "Item:".length());
                //END EXTRACT
                //DATA POPULATION
                if (sRest != null && sRest.length() > 0) {
                    tfItem[i].setText(sRest);
                    bItemsPresent = true;
                }
                //COMMENT - Why is this not in the above logic block?
                //COMMENT - figure out a way to consolidate this logic without changing it's function.
                if (bItemsPresent) {
                    tfItem[i].setEditable(false);
                }
                //END DATA POPULATION
                // Qty field
                //DATA POPULATION
                sLine = br.readLine();
                //VIEW CREATION
                tfQty[i] = new JTextField(10);
                //END VIEW CREATION

                //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
                if (!sLine.startsWith("Quantity:")) {
                    JOptionPane.showMessageDialog(Wkflow.this, "Quantity missing in .wkf file!");
                    return;
                }
                sRest = sLine.substring(sLine.indexOf("Quantity:") + "Quantity:".length());
                //END EXTRACT METHOD

                //DATA POPULATION
                if (sRest != null && sRest.length() > 0) {
                    tfQty[i].setText(sRest);
                }
                if (bItemsPresent) {
                    tfQty[i].setEditable(false);
                }
                //END DATA POPULATION
                // Unit price field
                //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
                sLine = br.readLine();
                tfUnitPrice[i] = new JTextField(10);
                if (!sLine.startsWith("Unit price:")) {
                    JOptionPane.showMessageDialog(Wkflow.this, "Unit price missing in .wkf file!");
                    return;
                }
                sRest = sLine.substring(sLine.indexOf("Unit price:") + "Unit price:".length());
                //END EXTRACT METHOD
                //DATA POPULATION
                if (sRest != null && sRest.length() > 0) {
                    tfUnitPrice[i].setText(sRest);
                }
                if (bItemsPresent) {
                    tfUnitPrice[i].setEditable(false);
                }


                // Total Price field
                String sQty = tfQty[i].getText();
                String sUnitPrice = tfUnitPrice[i].getText();
                String sPrice = null;
                if (sQty.length() > 0 && sUnitPrice.length() > 0) {
                    int qty = Integer.valueOf(sQty).intValue();
                    double unitPrice = Double.valueOf(sUnitPrice).doubleValue();
                    sPrice = String.valueOf(unitPrice * qty);
                }
                //END DATA POPULATION

                //VIEW CREATION
                tfPrice[i] = new JTextField(14);
                tfPrice[i].setText(sPrice);
                tfPrice[i].setEditable(false);
                //END VIEW CREATION
            }
            //END MAKE DYNAMIC

            //COMMENT - static allocation of a number of line item panels
            //COMMENT - this should be dynamic.
            //COMMENT - do not store view objects, this can leak.  retrieve named objects from the container itself.
            //MAKE DYNAMIC
            JPanel[] itPane = new JPanel[5];
            for (int i = 0; i < 5; i++) {
                itPane[i] = new JPanel();
                itPane[i].setLayout(new GridLayout(1, 4));
                itPane[i].add(tfItem[i]);
                itPane[i].add(tfQty[i]);
                itPane[i].add(tfUnitPrice[i]);
                itPane[i].add(tfPrice[i]);
            }
            //END MAKE DYNAMIC

            // Signatures.
            //VIEW CREATION
            JLabel lblOfficial = new JLabel("Official signatures:");

            JPanel signHdrPane = new JPanel();
            signHdrPane.setLayout(new GridLayout(1, 1));
            signHdrPane.add(lblOfficial);
            signHdrPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            //END VIEW CREATION

            //REDUNDANT
            tfOfficial = new JTextField[3];
            browseOfficial = new JButton[3];
            browsePanel = new JPanel[3];
            tfSignature = new JLabel[3];
            sSignature = new String[3];
            butSign = new JButton[3];
            //END REDUNDANT

            HashSet hs = null;
            boolean bOfficialsPresent = false;
            //MAKE DYNAMIC
            for (int i = 0; i < 3; i++) {
                // Official field
                //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
                sLine = br.readLine();
                if (!sLine.startsWith("Official:")) {
                    JOptionPane.showMessageDialog(Wkflow.this, "Official/Role missing in .wkf file!");
                    return;
                }
                sRest = sLine.substring(sLine.indexOf("Official:") + "Official:".length());
                //END EXTRACT METHOD
                if (sRest != null && sRest.length() > 0) {
                    bOfficialsPresent = true;
                }
                //VIEW CREATION
                tfOfficial[i] = new JTextField(10);
                browseOfficial[i] = new JButton("Browse...");
                browseOfficial[i].setActionCommand("browse");
                browseOfficial[i].addActionListener(this);
                browsePanel[i] = new JPanel();
                browsePanel[i].setLayout(new BorderLayout());
                browsePanel[i].add(tfOfficial[i], BorderLayout.CENTER);
                browsePanel[i].add(browseOfficial[i], BorderLayout.EAST);
                browsePanel[i].setPreferredSize(new Dimension(200, 30));
                browsePanel[i].setMaximumSize(new Dimension(200, 30));
                //END VIEW CREATION

                //VIEW POPULATION
                if (bOfficialsPresent) {
                    tfOfficial[i].setText(sRest);
                    tfOfficial[i].setEditable(false);
                    browseOfficial[i].setEnabled(false);
                } else {
                    tfOfficial[i].setEditable(true);
                    browseOfficial[i].setEnabled(true);
                }
                //END VIEW POPULATION
                // Signatures and Sign buttons.

                //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
                sLine = br.readLine();
                if (!sLine.startsWith("Signature:")) {
                    JOptionPane.showMessageDialog(Wkflow.this, "Signature missing in .wkf file!");
                    return;
                }
                //END EXTRACT METHOD
                // The sign buttons.
                //VIEW CREATION
                butSign[i] = new JButton("Sign");
                butSign[i].addActionListener(this);
                butSign[i].setMinimumSize(new Dimension(60, 30));
                butSign[i].setPreferredSize(new Dimension(60, 30));
                butSign[i].setMaximumSize(new Dimension(60, 30));

                // The signature labels.
                tfSignature[i] = new JLabel((String) null);
                tfSignature[i].setMinimumSize(new Dimension(200, 50));
                tfSignature[i].setPreferredSize(new Dimension(200, 50));
                tfSignature[i].setMaximumSize(new Dimension(200, 50));

                // Shadow signatures.
                sSignature[i] = null;
                //END VIEW CREATION

                //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
                sRest = sLine.substring(sLine.indexOf("Signature:") + "Signature:".length());
                if (sRest != null && sRest.length() > 0) {
                    //COMMENT - MOVE FILE READING CODE INTO A UTILITY FOR READING SIGNATURE FILES
                   
                    //NOTE - THE "SIGNATURE" FILE IS AN ICON, AN IMAGE OF A SIGNATURE.
                    //JOptionPane.showMessageDialog(Wkflow.this, "Extracting sign file of " + sRest + " to draw for i = " + i);
                    //EXTRACT METHOD / PROCESS - boilerplate, refactor out.
                    //COMMENT - HAVE THE METHOD GUARANTEE RETURNING A PLACEHOLDER ICON IF THE SIGNATURE ICON IS NOT FOUND
                    //COMMENT - MAKE SURE THAT THIS DOESN'T COUNT AS AN ACTUAL SIGNATURE.
                    byte[] fileBytes = sysCaller.getFileContent(sRest + ".signature.file");
                    if (fileBytes == null) {
                        JOptionPane.showMessageDialog(Wkflow.this, sysCaller.getLastError());
                        return;
                    }
                    ImageIcon signatureImage = new ImageIcon(fileBytes);
                    //END EXTRACT METHOD
                    //VIEW POPULATION
                    tfSignature[i].setIcon(signatureImage);
                    //END VIEW POPULATION
                    //DATA POPULATION
                    sSignature[i] = sRest;
                    //END DATA POPULATION
                } else {
                    //DATA POPULATION
                    sSignature[i] = "";
                    //END DATA POPULATION
                }
                //END EXTRACT METHOD
            }
            //END MAKE DYNAMIC

            //MAKE DYNAMIC
            Container[] signBox = new Container[3];
            for (int i = 0; i < 3; i++) {
                signBox[i] = Box.createHorizontalBox();
                signBox[i].add(browsePanel[i]);
                signBox[i].add(butSign[i]);
                signBox[i].add(tfSignature[i]);
            }
            //END MAKE DYNAMIC

            //VIEW CREATION
            Container box = Box.createVerticalBox();
            //COMMENT - TYPE LABEL
            box.add(lblType);
            //COMMENT - NUMBER PART
            box.add(pane1);
            //COMMENT - REQUESTER PART
            box.add(pane2);
            //COMMENT - ITEM HEADER
            box.add(itHdrPane);
            //COMMENT - ITEMS
            //MAKE DYNAMIC
            box.add(itPane[0]);
            box.add(itPane[1]);
            box.add(itPane[2]);
            box.add(itPane[3]);
            box.add(itPane[4]);
            //END MAKE DYNAMIC
            //COMMENT - SIGNATURES HEADER
            box.add(signHdrPane);
            //COMMENT - SIGNATURES
            //MAKE DYNAMIC
            box.add(signBox[0]);
            box.add(signBox[1]);
            box.add(signBox[2]);
            //END MAKE DYNAMIC
            //COMMENT - ACTION BUTTONS
            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(this);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(this);

            JButton copyButton = new JButton("Copy");
            copyButton.addActionListener(this);

            JPanel buttonPane = new JPanel();
            buttonPane.add(submitButton);
            buttonPane.add(cancelButton);
//      buttonPane.add(copyButton);
            buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

            //COMMENT - FINAL CONTAINER CREATION, WHEW!
            Container content = getContentPane();
            content.add(box, BorderLayout.CENTER);
            content.add(buttonPane, BorderLayout.SOUTH);
            //COMMENT - MAKE IT SMALL AS POSSIBLE
            pack();
            //COMMENT - SHOW ME!
            setVisible(true);
            //END VIEW CREATION
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSignerField(int fieldNo, PmNode node) {
        tfOfficial[fieldNo].setText(node.getName());
    }

    @SuppressWarnings("CallToThreadDumpStack")
    public static byte[] getBytesFromFile(File file) {
        try {
            InputStream is = new FileInputStream(file);

            // Get the size of the file
            long length = file.length();

            // You cannot create an array using a long type.
            // It needs to be an int type.
            // Before converting to an int type, check
            // to ensure that file is not larger than Integer.MAX_VALUE.
            if (length > Integer.MAX_VALUE) {
                // File is too large
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                return null;
            }

            // Close the input stream and return bytes
            is.close();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void browse(ActionEvent e) {
        Object o = e.getSource();
        int i = -1;
        for (i = 0; i < 3; i++) {
            if (o.equals(browseOfficial[i])) {
                break;
            }
        }
        if (i >= 3) {
            System.out.println("The event source is not a button...");
            return;
        }
        System.out.println("The browse source is button " + i);
        
        UserBrowser userBrowser = new UserBrowser(this, sysCaller, WORKFLOW_APP_NAME);

        userBrowser.pack();
        
        final int selection = i;
        userBrowser.addUserBrowserListener(new UserBrowserListener() {

            @Override
            public void userSelected(PmNode userNode) {
                setSignerField(selection, userNode);
                //remove ourselves from the listener list.
            }
        });
        userBrowser.setVisible(true);
    }

    private void signForm(ActionEvent e) {
        Object o = e.getSource();
        int i = -1;
        for (i = 0; i < 3; i++) {
            if (o.equals(butSign[i])) {
                break;
            }
        }
        if (i >= 3) {
            System.out.println("The event source is not a button...");
            return;
        }
        System.out.println("The source is button " + i);

        byte[] fileBytes = sysCaller.getFileContent(sCrtUserName + ".signature.file");
        if (fileBytes == null) {
            JOptionPane.showMessageDialog(Wkflow.this, sysCaller.getLastError());
            return;
        }
        ImageIcon signatureImage = new ImageIcon(fileBytes);
        tfSignature[i].setIcon(signatureImage);
        sSignature[i] = sCrtUserName;
    }

    private void copyForm() {
        String sCopyName = sysCaller.copyObject("prop1");
        if (sCopyName == null) {
            JOptionPane.showMessageDialog(Wkflow.this, sysCaller.getLastError());
            return;
        }
    }

    // Convert the form to a text file and write it back.
    @SuppressWarnings("CallToThreadDumpStack")
    private void submitForm() {
        // Maximum 4 rules, one for the creator's action,
        // three for the max. 3 officials.
        String[] ruleLabels = new String[4]; // first for the creator's action

        String sNumber = tfNumber.getText();
        if (sNumber == null || sNumber.trim().length() == 0) {
            JOptionPane.showMessageDialog(Wkflow.this, "Please complete the order number!");
            return;
        }

        // Get the object name.
        // If the form is submitted by the secretary, (i.e., tfNumber is editable),
        // then the tfNumber is the object name. Otherwise, sCrtObjName is the
        // name of the object.
        String sObjName = sCrtObjName;
        if (tfNumber.isEditable()) {
            sObjName = tfNumber.getText();
        }

        // Prepare the random generator (for labels).
        Random random = new Random();
        byte[] bytes = new byte[4];

        // If this form is submitted by the Secretary, generate the EVER rules
        // before writing back the form.
        // The secretary is the one submitting the form iff tfNumber
        // (or tfRequester) is editable.
        if (tfNumber.isEditable()) {
            // At least the first combo box should contain the name of an official,
            // i.e., something different from "Select...".
            String sFirstSigner = tfOfficial[0].getText();

            if (sFirstSigner.length() == 0) {
                JOptionPane.showMessageDialog(Wkflow.this, "Please select at least an official signer!");
                return;
            }

            ArrayList script = new ArrayList();
            script.add("script demo");
            script.add("");

            // Generate a random rule label and save it.
            random.nextBytes(bytes);
            ruleLabels[0] = byteArray2HexString(bytes);

            script.add(ruleLabels[0] + ": when any user performs \"Object write\"");
//      script.add("  on any object of attribute \"Populated Forms\"");
            script.add("  on object \"" + sObjName + "\" of attribute \"Populated Forms\"");
            script.add("  do");
            script.add("    assign object attribute oattr_of_default_obj() to");
            script.add("      object attribute \"" + sFirstSigner + " witems\"");
            script.add("    delete assignment of object attribute oattr_of_default_obj()");
            script.add("      to object attribute \"Populated Forms\"");

            int i = 1;
            while (i < 3) {
                String sSignerI = tfOfficial[i].getText();
                String sSignerPrev = tfOfficial[i - 1].getText();

                if (sSignerI.length() == 0) {
                    break;
                }
                script.add("");

                // Generate a random rule label and save it.
                random.nextBytes(bytes);
                ruleLabels[i] = byteArray2HexString(bytes);

                script.add(ruleLabels[i] + ": when any user performs \"Object write\"");
//        script.add("when user \"" + tfOfficial[i-1].getText() + "\" performs \"Object write\"");
//        script.add("  on any object of attribute \"" + tfOfficial[i-1].getText() + " witems\"");
                script.add("  on object \"" + sObjName + "\" of attribute \""
                        + sSignerPrev + " witems\"");
                script.add("  do");
                script.add("    assign object attribute oattr_of_default_obj() to");
                script.add("      object attribute \"" + sSignerI + " witems\"");
                script.add("    delete assignment of object attribute oattr_of_default_obj()");
                script.add("      to object attribute \"" + sSignerPrev + " witems\"");
                script.add("    deny user user_default() operations \"File write\" on oattr_of_default_obj()");

                i++;
            }

            // For the last official, generate a rule that moves the object to the
            // Approved Orders container and deletes all rules.
            script.add("");
            random.nextBytes(bytes);
            ruleLabels[i] = byteArray2HexString(bytes);
            String sSignerPrev = tfOfficial[i - 1].getText();

            script.add(ruleLabels[i] + ": when any user performs \"Object write\"");
            script.add("  on object \"" + sObjName + "\" of attribute \""
                    + sSignerPrev + " witems\"");
            script.add("  do");
            script.add("    assign object attribute oattr_of_default_obj() to");
            script.add("      object attribute \"Approved Orders\"");
            script.add("    delete assignment of object attribute oattr_of_default_obj()");
            script.add("      to object attribute \"" + sSignerPrev + " witems\"");
            StringBuilder sb = new StringBuilder();
            sb.append("    delete rules ");
            for (int j = 0; j <= i; j++) {
                if (j == 0) {
                    sb.append(ruleLabels[j]);
                } else {
                    sb.append(", ").append(ruleLabels[j]);
                }
            }
            script.add(sb.toString());

            // Compile and submit the script.
            boolean bScriptCompiled = sysCaller.addScript(script);
            System.out.println("Result from compile script: " + bScriptCompiled);
            Wkflow.this.terminate(0);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        // Extract the type.
        pw.println("Type:" + lblType.getText());

        // Extract the number.
        pw.println("Number:" + tfNumber.getText());

        // Extract the requester:
        pw.println("Requester:" + tfRequester.getText());

        // Extract the items:
        for (int i = 0; i < 5; i++) {
            pw.println("Item:" + tfItem[i].getText());
            pw.println("Quantity:" + tfQty[i].getText());
            pw.println("Unit price:" + tfUnitPrice[i].getText());
        }

        // Extract the signatures (from combo boxes or from text fields).
        for (int i = 0; i < 3; i++) {
            pw.println("Official:" + tfOfficial[i].getText());
            pw.println("Signature:" + sSignature[i]);
        }

        pw.close();

        // If this form is submitted by the secretary, create a new object
        // in Populated Forms as in "Save As". Otherwise, simply save the object.
        if (tfNumber.isEditable()) {
            // Prepare for object creation.
            sObjName = tfNumber.getText();
            String sContainers = "b|Populated Forms";
            String sObjClass = "File";
            String sObjType = "wkf";
            String sPerms = "File write";

            // Create the object.
            sCrtObjHandle = sysCaller.createObject3(sObjName, sObjClass, sObjType,
                    sContainers, sPerms, null, null, null, null);
            if (sCrtObjHandle == null) {
                JOptionPane.showMessageDialog(Wkflow.this, sysCaller.getLastError());
                return;
            }

            sCrtObjName = sObjName;
        }

        try {
            byte[] buf = baos.toByteArray();
            int res = sysCaller.writeObject3(sCrtObjHandle, buf);
            if (res < 0) {
                JOptionPane.showMessageDialog(Wkflow.this, sysCaller.getLastError());
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(Wkflow.this, "Exception while saving object: " + e.getMessage());
            return;
        }


        // Terminate.
        terminate(0);
    }

    private void cancelForm() {
        terminate(0);
    }

    protected void center() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension us = getSize();
        int x = (screen.width - us.width) / 2;
        int y = (screen.height - us.height) / 2;
        setLocation(x, y);
    }

    protected void openInitialObj(String sObjName) {
        // Open the object.
        sCrtObjHandle = sysCaller.openObject3(sObjName, "File read,File write");
        if (sCrtObjHandle == null) {
            JOptionPane.showMessageDialog(Wkflow.this, sysCaller.getLastError());
            return;
        }
        sCrtObjName = sObjName;

        // Read the object content.
        byte[] buf = sysCaller.readObject3(sCrtObjHandle);
        if (buf == null) {
            JOptionPane.showMessageDialog(Wkflow.this, sysCaller.getLastError());
            return;
        }

        createUI(buf);
    }

    byte byte2HexDigit(byte n) {
        if (n < 10) {
            return (byte) ('0' + n);
        } else {
            return (byte) ('A' + n - 10);
        }
    }

    String byteArray2HexString(byte[] inp) {
        byte[] buf = new byte[2 * inp.length];
        int inpix, outix;
        int n;
        byte q, r;

        for (inpix = outix = 0; inpix < inp.length; inpix++) {
            n = inp[inpix] & 0x000000FF;
            q = (byte) (n / 16);
            r = (byte) (n % 16);
            buf[outix++] = byte2HexDigit(q);
            buf[outix++] = byte2HexDigit(r);
        }
        return new String(buf);
    }

    private static void createAndShowGUI(int nSimPort, String sSessId, String sProcId, String sObjName, boolean bDebug) {
        //Create and set up the window.
        Wkflow myWkflow = new Wkflow(nSimPort, sSessId, sProcId, bDebug);

        if (sObjName == null) {
            return;
        }
        myWkflow.openInitialObj(sObjName);
    }
    private static String sesid;
    private static String pid;
    private static String objname;
    private static int simport;
    private static boolean debug;

    public static void main(String[] args) {
        // Process command line arguments.
        // -session <sessionId> -simport <simulator port>
        // The session id is mandatory.
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-session")) {
                sesid = args[++i];
            } else if (args[i].equals("-process")) {
                pid = args[++i];
            } else if (args[i].equals("-simport")) {
                simport = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-debug")) {
                debug = true;
            } else {
                objname = args[i];
            }
        }

        if (sesid == null) {
            System.out.println(WFLOW_IN_POLICY_MACHINE_SESSION_MESSAGE);
            System.exit(-1);
        }
        if (pid == null) {
            System.out.println(WFLOW_IN_POLICY_MACHINE_PROCESS_MESSAGE);
            System.exit(-1);
        }

        System.out.println("session=" + sesid + ", simport=" + simport + ", objname=" + objname);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                createAndShowGUI(simport, sesid, pid, objname, debug);
            }
        });
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.application.workflow;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.*;

/**
 * WARNING THIS CLASS IS DEPRECATED IN FAVOR OF PDF BASED WORKFLOW.  IT REPRESENTS
 * EARLY RESEARCH INTO WORKFLOW AND AS SUCH IS IMCOMPLETE.
 * @author Administrator
 */
public class WorkflowView extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 813444061082077126L;

	/**
	 * @author   Administrator
	 */
	public static enum WorkflowViewMode {

        /**
		 * @uml.property  name="cREATE_MODE"
		 * @uml.associationEnd  
		 */
        CREATE_MODE, /**
		 * @uml.property  name="uSE_MODE"
		 * @uml.associationEnd  
		 */
        USE_MODE;
    }
    private static final String REQUESTER_TEXT = "Requester";
    private static final String NUMBER_TEXT = "Number";
    private static final String BROWSE_TEXT = "Browse...";
    private static final String SIGN_TEXT = "Sign";
    private static final String BROWSE_ACTION_CMD = "browse";
    
    /**
	 * @uml.property  name="tfNumber"
	 * @uml.associationEnd  
	 */
    private JTextField tfNumber;
    /**
	 * @uml.property  name="tfRequester"
	 * @uml.associationEnd  readOnly="true"
	 */
    private JTextField tfRequester;
    

    /*************************************
     * Generators and Functions
     *************************************/

    //A generator for line item panels.
   
    

   

     


    /***********************************************
     * Constructors
     ***********************************************/

   

   


//    private ActionListener browseInto(UserAdapter userDisplayingComponent) {
//        return new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent ae) {
//                UserBrowser ub = new UserBrowser((Window) WorkflowView.this.getTopLevelAncestor(),
//                        getSystemDelegate().getSystemCaller(), "Select User");
//            }
//        };
//    }

    

    private void initGUI() {
        MigLayout layout = new MigLayout("", "[align label][fill, 100:300:]", "[][][][]");
        setLayout(layout);

        tfNumber = build(jTextField(10), makeNotEditable());
        JLabel lblNumber = jLabel(NUMBER_TEXT);
        add(lblNumber, "newline");
        add(tfNumber, "");

        JLabel lblRequester = jLabel(REQUESTER_TEXT);


        add(lblRequester, "newline");
        add(tfRequester, "");
        setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));


        add(jLabel("Content"), "newline");
        add(jSeparator(SwingConstants.HORIZONTAL), "growx");

        
        
        

    }

    
    

    public JTextField getNumberTextField() {
        return tfNumber;
    }

    public JTextField getRequesterTextField() {
        return tfRequester;
    }



    
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(WorkflowView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InstantiationException ex) {
                        Logger.getLogger(WorkflowView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(WorkflowView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedLookAndFeelException ex) {
                        Logger.getLogger(WorkflowView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                 
                   
               

            }
        });
    }
}

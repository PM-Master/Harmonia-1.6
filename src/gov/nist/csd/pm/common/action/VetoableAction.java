package gov.nist.csd.pm.common.action;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 6/22/11
* Time: 12:00 PM
 * An ActionRef that can be vetoed, giving an opportunity to interrupt Actions on a state or condition.
*/
class VetoableAction extends ActionWrapper {
    /**
	 * @uml.property  name="_vetoListener"
	 * @uml.associationEnd  
	 */
    private final VetoListener _vetoListener;
    public VetoableAction(Action action, VetoListener vetoListener){
        super(action);
        if(vetoListener == null){
            vetoListener = new VetoListener(){

                @Override
                public boolean shouldVetoAction(ActionEvent ae) {
                    return false;
                }
            };
        }
        _vetoListener = vetoListener;
    }



    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if(!_vetoListener.shouldVetoAction(actionEvent)){
            getAction().actionPerformed(actionEvent);
        }
    }
}

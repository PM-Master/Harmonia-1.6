package gov.nist.csd.pm.common.action;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 6/22/11
* Time: 11:59 AM
* To change this template use File | Settings | File Templates.
*/
abstract class ActionCombiner extends ActionWrapper {
    /**
	 * @uml.property  name="_combinedAction"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    Action _combinedAction;
    public ActionCombiner(Action action, Action eventAction){
        super(action);
        if(eventAction == null){
            eventAction = new AbstractAction(){

                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                }
            };
        }
        _combinedAction = eventAction;
    }
}

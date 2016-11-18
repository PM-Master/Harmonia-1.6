package gov.nist.csd.pm.common.action;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 6/22/11
* Time: 11:59 AM
 * A composition of an action coming before another action.
 */
class PreAction extends ActionCombiner {

    public PreAction(Action action, Action actionAction) {
        super(action, actionAction);
    }
    @Override
    public void actionPerformed(ActionEvent actionEvent){
        _combinedAction.actionPerformed(actionEvent);
        getAction().actionPerformed(actionEvent);
    }
}

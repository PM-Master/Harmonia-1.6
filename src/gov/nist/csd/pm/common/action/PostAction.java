package gov.nist.csd.pm.common.action;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
* Created by IntelliJ IDEA.
* User: R McHugh
* Date: 6/22/11
* Time: 12:00 PM
 * Allows for the composition of an action after another action.
 */
class PostAction extends ActionCombiner {

    public PostAction(Action action, Action actionAction) {
        super(action, actionAction);
    }
    @Override
    public void actionPerformed(ActionEvent actionEvent){
        getAction().actionPerformed(actionEvent);
        _combinedAction.actionPerformed(actionEvent);
    }
}

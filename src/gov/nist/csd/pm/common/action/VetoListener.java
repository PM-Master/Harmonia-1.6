package gov.nist.csd.pm.common.action;

import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/22/11
 * Time: 11:06 AM
 * Contract of a VetoListener.
 */
public interface VetoListener {
    /**
     * A VetoableAction will call this method on a VetoListener prior to performing an
     * action.  Listener's that wish to prevent that action from being performed should return
     * true, otherwise return false.  The ActionEvent parameter is provided as a means to
     * disambiguate veto requests made to the listener.
     * @param ae
     * @return
     */
    public boolean shouldVetoAction(ActionEvent ae);
}

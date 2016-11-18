package gov.nist.csd.pm.common.util.swing;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 6/20/11
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class DocumentListenerAdapterForAction implements DocumentListener {

    /**
	 * @uml.property  name="_actionOnUpdate"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    Action _actionOnUpdate = NULL_ACTION;

    public DocumentListenerAdapterForAction() {
        this(NULL_ACTION);
    }

    public DocumentListenerAdapterForAction(Action actionOnUpdate) {
        setActionOnUpdate(actionOnUpdate);
    }


    public void setActionOnUpdate(Action actionOnUpdate) {
        _actionOnUpdate = checkNotNull(actionOnUpdate);
    }

    private void performUpdateAction(DocumentEvent documentEvent) {
        _actionOnUpdate.actionPerformed(new ActionEvent(documentEvent, 0, DOCUMENT_UPDATE_ACTION_NAME));
    }

    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        performUpdateAction(documentEvent);
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        performUpdateAction(documentEvent);
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        performUpdateAction(documentEvent);

    }


    public static Action NULL_ACTION = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {

        }
    };
    private static String DOCUMENT_UPDATE_ACTION_NAME = "_nist_csd_pm_document_update_action";
}

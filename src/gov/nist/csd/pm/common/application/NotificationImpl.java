/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.application;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Administrator
 */
public class NotificationImpl implements Notification {

    /**
	 * @uml.property  name="type"
	 * @uml.associationEnd  
	 */
    private final NotificationType type;
    /**
	 * @uml.property  name="message"
	 */
    private final String message;
    /**
	 * @uml.property  name="userData"
	 */
    private final Object[] userData;

    public NotificationImpl(NotificationType type, String message, Object... userData) {
        this.type = type;
        this.message = message;
        this.userData = userData;
    }

    /**
	 * @return
	 * @uml.property  name="type"
	 */
    @Override
    public NotificationType getType() {
        return type;
    }

    /**
	 * @return
	 * @uml.property  name="message"
	 */
    @Override
    public String getMessage() {
        return message;
    }

    /**
	 * @return
	 * @uml.property  name="userData"
	 */
    @Override
    public Object[] getUserData(){
        return userData;
    }

    @Override
    public void showDialog(Container container) {
        JOptionPane.showMessageDialog(container, getMessage(), getType().getName(), getType().dialogType());
    }
}

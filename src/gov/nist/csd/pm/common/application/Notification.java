/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.application;

import java.awt.*;

/**
 * @author  Administrator
 */
public interface Notification {

    /**
	 * @uml.property  name="type"
	 * @uml.associationEnd  
	 */
    public NotificationType getType();

    /**
	 * @uml.property  name="message"
	 */
    public String getMessage();

    public void showDialog(Container container);

    public Object[] getUserData();

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.application;

import javax.swing.*;

/**
 * @author   Administrator
 */
public interface NotificationType {

    /**
	 * @author  Administrator
	 */
    static abstract class AbstractNotificationType implements NotificationType{

        /**
		 * @uml.property  name="name"
		 */
        private final String name;

        public AbstractNotificationType(String name){
            this.name = name;
        }


        /**
		 * @return
		 * @uml.property  name="name"
		 */
        @Override
        public String getName(){
            return name;
        }

        @Override
        public Notification notification(String message, Object... userData) {
            return new NotificationImpl(this, message, userData);
        }
    }

    /**
	 * @uml.property  name="eRROR"
	 * @uml.associationEnd  
	 */
    static final NotificationType ERROR = new AbstractNotificationType("Error") {


        @Override
        public int dialogType() {
            return JOptionPane.ERROR_MESSAGE ;
        }
    };

    /**
	 * @uml.property  name="hEADER"
	 * @uml.associationEnd  
	 */
    static final NotificationType HEADER = new AbstractNotificationType("Header"){

        @Override
        public int dialogType() {
            return JOptionPane.INFORMATION_MESSAGE;
        }
    };

    /**
	 * @uml.property  name="sTATUS"
	 * @uml.associationEnd  
	 */
    static final NotificationType STATUS = new AbstractNotificationType("Status"){

        @Override
        public int dialogType() {
            return JOptionPane.INFORMATION_MESSAGE;
        }
    };

    /**
	 * @uml.property  name="aLL"
	 * @uml.associationEnd  
	 */
    static final NotificationType ALL = new AbstractNotificationType("ALL") {
        @Override
        public int dialogType() {
            return JOptionPane.INFORMATION_MESSAGE;
        }
    };

     public int dialogType();
     public Notification notification(String message, Object... userData);
     /**
	 * @uml.property  name="name"
	 */
    public String getName();
    






}

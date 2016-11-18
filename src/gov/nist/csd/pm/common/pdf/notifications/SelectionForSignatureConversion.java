package gov.nist.csd.pm.common.pdf.notifications;

import gov.nist.csd.pm.common.application.NotificationType;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/26/11 Time: 1:03 PM To change this template use File | Settings | File Templates.
 */
public interface SelectionForSignatureConversion  {


    /**
	 * @uml.property  name="tYPE"
	 * @uml.associationEnd  
	 */
    public static NotificationType TYPE = new ValidSelectionForSignatureConversionType("Valid Signature Conversion Target Notification");

    public static class ValidSelectionForSignatureConversionType extends NotificationType.AbstractNotificationType {

        public ValidSelectionForSignatureConversionType(String name) {
            super(name);
        }

        @Override
        public int dialogType() {
            return JOptionPane.INFORMATION_MESSAGE;
        }
    }
}

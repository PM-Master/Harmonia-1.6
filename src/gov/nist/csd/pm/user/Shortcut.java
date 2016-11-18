package gov.nist.csd.pm.user;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA. User: Administrator Date: 9/7/11 Time: 2:27 PM To change this template use File | Settings | File Templates.
 */
public interface Shortcut {
    /**
	 * @uml.property  name="name"
	 */
    public String getName();
    /**
	 * @uml.property  name="visualRep"
	 * @uml.associationEnd  
	 */
    public Icon getVisualRep();
    /**
	 * @uml.property  name="action"
	 * @uml.associationEnd  
	 */
    public Action getAction();
}

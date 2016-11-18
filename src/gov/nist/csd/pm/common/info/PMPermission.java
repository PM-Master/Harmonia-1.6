package gov.nist.csd.pm.common.info;


/**
 * Created by IntelliJ IDEA. User: Administrator Date: 9/28/11 Time: 10:02 PM To change this template use File | Settings | File Templates.
 */
public enum PMPermission {
    /**
	 * @uml.property  name="fILE_READ"
	 * @uml.associationEnd  
	 */
    FILE_READ, /**
	 * @uml.property  name="fILE_WRITE"
	 * @uml.associationEnd  
	 */
    FILE_WRITE, /**
	 * @uml.property  name="oBJECT_CREATE"
	 * @uml.associationEnd  
	 */
    OBJECT_CREATE, /**
	 * @uml.property  name="oBJECT_DELETE"
	 * @uml.associationEnd  
	 */
    OBJECT_DELETE, /**
	 * @uml.property  name="oBJECT_ATTRIBUTE_ASSIGN"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTE_ASSIGN, /**
	 * @uml.property  name="oBJECT_ATTRIBUTE_CREATE"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTE_CREATE, /**
	 * @uml.property  name="oBJECT_ATTRIBUTE_DELETE"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTE_DELETE;
}

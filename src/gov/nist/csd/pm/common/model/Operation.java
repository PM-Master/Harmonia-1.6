package gov.nist.csd.pm.common.model;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/20/11 Time: 3:33 PM To change this template use File | Settings | File Templates.
 */
public enum Operation {
    /**
	 * @uml.property  name="fILE_READ"
	 * @uml.associationEnd  
	 */
    FILE_READ, /**
	 * @uml.property  name="fILE_WRITE"
	 * @uml.associationEnd  
	 */
    FILE_WRITE,

    /**
	 * @uml.property  name="oBJECT_ATTRIBUTE_ASSIGN_TO"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTE_ASSIGN_TO, /**
	 * @uml.property  name="oBJECT_ATTRIBUTE_ASSIGN"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTE_ASSIGN, /**
	 * @uml.property  name="oBJECT_ATTRIBUTE_CREATE_OBJECT_ATTRIBUTE"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTE_CREATE_OBJECT_ATTRIBUTE,
    /**
	 * @uml.property  name="oBJECT_ATTRIBUTE_CREATE_OBJECT"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTE_CREATE_OBJECT, /**
	 * @uml.property  name="oBJECT_ATTRIBUTE_CREATE_OPERATION_SET"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTE_CREATE_OPERATION_SET,

    /**
	 * @uml.property  name="uSER_ASSIGN"
	 * @uml.associationEnd  
	 */
    USER_ASSIGN,

    /**
	 * @uml.property  name="uSER_ATTRIBUTE_ASSIGN_TO_OPERATION_SET"
	 * @uml.associationEnd  
	 */
    USER_ATTRIBUTE_ASSIGN_TO_OPERATION_SET,

    /**
	 * @uml.property  name="oPERATION_SET_ASSIGN"
	 * @uml.associationEnd  
	 */
    OPERATION_SET_ASSIGN, /**
	 * @uml.property  name="oPERATION_SET_ASSIGN_TO"
	 * @uml.associationEnd  
	 */
    OPERATION_SET_ASSIGN_TO,
    /**
	 * @uml.property  name="eNTITY_REPRESENT"
	 * @uml.associationEnd  
	 */
    ENTITY_REPRESENT;
}

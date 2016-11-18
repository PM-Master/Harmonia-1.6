package gov.nist.csd.pm.common.browser;

import gov.nist.csd.pm.common.util.lang.Strings;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/22/11 Time: 10:21 AM To change this template use File | Settings | File Templates.
 */
public enum PmNodeType implements PmNodeTypes {
    SYNC_POLICY(PM_NODE_SYNC_POLICY),
    SYNC_OATTR(PM_NODE_SYNC_OATTR),
    SYNC_OBJECT(PM_NODE_SYNC_OBJECT),

    /**
	 * @uml.property  name="uSER"
	 * @uml.associationEnd  
	 */
    USER(PM_NODE_USER),
    /**
	 * @uml.property  name="uSER_ATTRIBUTE"
	 * @uml.associationEnd  
	 */
    USER_ATTRIBUTE(PM_NODE_UATTR),
    /**
	 * @uml.property  name="pOLICY"
	 * @uml.associationEnd  
	 */
    POLICY(PM_NODE_POLICY),
    /**
	 * @uml.property  name="oBJECT_ATTRIBUTE"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTE(PM_NODE_OATTR),
    OBJECT_ATTRIBUTE_DISABLED(PM_NODE_OATTR_DISABLED),

    /**
	 * @uml.property  name="cONTAINER"
	 * @uml.associationEnd  
	 */
    CONTAINER(PM_NODE_OATTR),
    /**
	 * @uml.property  name="oBJECT"
	 * @uml.associationEnd  
	 */
    OBJECT(PM_NODE_OBJECT),
    
    OBJECT_DISABLED(PM_NODE_OBJECT_DISABLED),
    
    /**
	 * @uml.property  name="oPSET"
	 * @uml.associationEnd  
	 */
    OPSET(PM_NODE_OPSET),
    /**
	 * @uml.property  name="cONNECTOR"
	 * @uml.associationEnd  
	 */
    CONNECTOR(PM_NODE_CONN),
    /**
	 * @uml.property  name="fILE"
	 * @uml.associationEnd  
	 */
    FILE(PM_NODE_OBJECT),
    /**
	 * @uml.property  name="nODE"
	 * @uml.associationEnd  
	 */
    NODE(PM_NODE),//Representative of any type of node
    /**
	 * @uml.property  name="bACKLINK"
	 * @uml.associationEnd  
	 */
    BACKLINK(PM_NODE_BACKLINK),
    /**
	 * @uml.property  name="rESOURCE"
	 * @uml.associationEnd  
	 */
    RESOURCE(PM_NODE_RESOURCE),
    /**
	 * @uml.property  name="iNVALID"
	 * @uml.associationEnd  
	 */
    INVALID(PM_NODE_INVALID);


    PmNodeType(String typeCode){
        this.typeCode = typeCode;
    }
    /**
	 * @uml.property  name="typeCode"
	 */
    private String typeCode;
    public String typeCode(){
        return typeCode;
    }

    public String typeString(){
        return Strings.camelCaseJoin(name().split("_"));
    }

    public boolean isOfEqualType(PmNodeType other){
        if(other == null){
            return false;
        }
        else{
            return this.typeCode().equals(other.typeCode());
        }
    }

    public static PmNodeType typeForCode(String code){
        code = code.toLowerCase();
        for(PmNodeType type : PmNodeType.values()){
            if(type.typeCode().equals(code)){
                return type;
            }
        }
        return INVALID;
    }

}

interface PmNodeTypes{
    public static final String PM_NODE = "no";
    public static final String PM_NODE_USER = "u";
    public static final String PM_NODE_UATTR = "a";
    public static final String PM_NODE_POLICY = "p";
    public static final String PM_NODE_OATTR = "b";
    public static final String PM_NODE_OATTR_DISABLED = "";
    public static final String PM_NODE_OBJECT = "o";
    public static final String PM_NODE_OBJECT_DISABLED = "";

    public static final String PM_NODE_OPSET = "s";
    public static final String PM_NODE_CONN = "c";
    public static final String PM_NODE_INVALID  ="";
    public static final String PM_NODE_BACKLINK = "";
    public static final String PM_NODE_RESOURCE = "";
    public static final String PM_NODE_SYNC_POLICY = "sp";
    public static final String PM_NODE_SYNC_OATTR = "sb";
    public static final String PM_NODE_SYNC_OBJECT = "so";

}

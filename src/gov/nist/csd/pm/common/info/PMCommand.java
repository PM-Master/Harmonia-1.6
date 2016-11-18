/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.info;

import com.google.common.collect.Lists;

import gov.nist.csd.pm.common.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author  Administrator
 */

public enum PMCommand {
    /**
	 * @uml.property  name="aDD_HOST_APP"
	 * @uml.associationEnd  
	 */
    ADD_HOST_APP,
    /**
	 * @uml.property  name="aDD_OATTR"
	 * @uml.associationEnd  
	 */
    ADD_OATTR,
    /**
	 * @uml.property  name="aDD_OPEN_OBJ"
	 * @uml.associationEnd  
	 */
    ADD_OPEN_OBJ,
    /**
	 * @uml.property  name="aDD_PROP"
	 * @uml.associationEnd  
	 */
    ADD_PROP,
    /**
	 * @uml.property  name="aDD_RECORD_KEYS"
	 * @uml.associationEnd  
	 */
    ADD_RECORD_KEYS,
    /**
	 * @uml.property  name="aDD_SCRIPT"
	 * @uml.associationEnd  
	 */
    ADD_SCRIPT,
    /**
	 * @uml.property  name="aDD_TEMPLATE"
	 * @uml.associationEnd  
	 */
    ADD_TEMPLATE,
    /**
	 * @uml.property  name="aDD_UATTR"
	 * @uml.associationEnd  
	 */
    ADD_UATTR,
    /**
	 * @uml.property  name="aSSIGN"
	 * @uml.associationEnd  
	 */
    ASSIGN,
    /**
	 * @uml.property  name="aSSIGN_OBJ_TO_OATTR"
	 * @uml.associationEnd  
	 */
    ASSIGN_OBJ_TO_OATTR,
    /**
	 * @uml.property  name="aSSIGN_OBJ_TO_OATTR_WITH_PROP"
	 * @uml.associationEnd  
	 */
    ASSIGN_OBJ_TO_OATTR_WITH_PROP,
    /**
	 * @uml.property  name="bUILD_CLIPBOARD"
	 * @uml.associationEnd  
	 */
    BUILD_CLIPBOARD,
    /**
	 * @uml.property  name="cHANGE_PASSWORD"
	 * @uml.associationEnd  
	 */
    CHANGE_PASSWORD,
    /**
	 * @uml.property  name="aSSIGN_OBJ_TO_CONTAINER"
	 * @uml.associationEnd  
	 */
    ASSIGN_OBJ_TO_CONTAINER,
    /**
	 * @uml.property  name="aSSIGN_OBJ_TO_INBOX_OF"
	 * @uml.associationEnd  
	 */
    ASSIGN_OBJ_TO_INBOX_OF,
    /**
	 * @uml.property  name="aSSIGN_OBJ_TO_OUTBOX_OF"
	 * @uml.associationEnd  
	 */
    ASSIGN_OBJ_TO_OUTBOX_OF,
    /**
	 * @uml.property  name="aSSIGN_OBJ_TO_WINBOX_OF"
	 * @uml.associationEnd  
	 */
    ASSIGN_OBJ_TO_WINBOX_OF,
    /**
	 * @uml.property  name="bYE"
	 * @uml.associationEnd  
	 */
    BYE,
    /**
	 * @uml.property  name="cLOSE_OBJECT"
	 * @uml.associationEnd  
	 */
    CLOSE_OBJECT,
    /**
	 * @uml.property  name="cOMPILE_SCRIPT_AND_ADD_TO_ENABLED"
	 * @uml.associationEnd  
	 */
    COMPILE_SCRIPT_AND_ADD_TO_ENABLED,
    /**
	 * @uml.property  name="cOMPILE_SCRIPT_AND_ENABLE"
	 * @uml.associationEnd  
	 */
    COMPILE_SCRIPT_AND_ENABLE,
    /**
	 * @uml.property  name="cOMPUTE_VOS"
	 * @uml.associationEnd  
	 */
    COMPUTE_FAST_VOS,
    /**
	 * @uml.property  name="cOMPUTE_FAST_VOS"
	 * @uml.associationEnd  
	 */
    COMPUTE_VOS,
    /**
	 * @uml.property  name="cONNECT"
	 * @uml.associationEnd  
	 */
    CONNECT,
    /**
	 * @uml.property  name="cOPY_OBJECT"
	 * @uml.associationEnd  
	 */
    COPY_OBJECT,
    /**
	 * @uml.property  name="cOPY_TO_CLIPBOARD"
	 * @uml.associationEnd  
	 */
    COPY_TO_CLIPBOARD,
    /**
	 * @uml.property  name="cREATE_LINKED_OBJECTS"
	 * @uml.associationEnd  
	 */
    CREATE_LINKED_OBJECTS,
    /**
	 * @uml.property  name="cREATE_OBJECT_3"
	 * @uml.associationEnd  
	 */
    CREATE_OBJECT_3,
    /**
	 * @uml.property  name="cREATE_PROCESS"
	 * @uml.associationEnd  
	 */
    CREATE_PROCESS,
    /**
	 * @uml.property  name="cREATE_RECORD"
	 * @uml.associationEnd  
	 */
    CREATE_RECORD,
    /**
	 * @uml.property  name="cREATE_RECORD_IN_ENTITY_WITH_PROP"
	 * @uml.associationEnd  
	 */
    CREATE_RECORD_IN_ENTITY_WITH_PROP,
    /**
	 * @uml.property  name="cREATE_SESSION"
	 * @uml.associationEnd  
	 */
    CREATE_SESSION,
    /**
	 * @uml.property  name="dEASSIGN_OBJ_FROM_HOME_OF"
	 * @uml.associationEnd  
	 */
    DEASSIGN_OBJ_FROM_HOME_OF,
    /**
	 * @uml.property  name="dEASSIGN_OBJ_FROM_INBOX_OF"
	 * @uml.associationEnd  
	 */
    DEASSIGN_OBJ_FROM_INBOX_OF,
    /**
	 * @uml.property  name="dEASSIGN_OBJ_FROM_OATTR_WITH_PROP"
	 * @uml.associationEnd  
	 */
    DEASSIGN_OBJ_FROM_OATTR_WITH_PROP,
    /**
	 * @uml.property  name="dEASSIGN_OBJ_FROM_OUTBOX_OF"
	 * @uml.associationEnd  
	 */
    DEASSIGN_OBJ_FROM_OUTBOX_OF,
    /**
	 * @uml.property  name="dELETE_ASSIGNMENT"
	 * @uml.associationEnd  
	 */
    DELETE_ASSIGNMENT,
    /**
	 * @uml.property  name="dELETE_CONTAINER_OBJECTS"
	 * @uml.associationEnd  
	 */
    DELETE_CONTAINER_OBJECTS,
    /**
	 * @uml.property  name="dELETE_NODE"
	 * @uml.associationEnd  
	 */
    DELETE_NODE,
    /**
	 * @uml.property  name="dELETE_OPEN_OBJ"
	 * @uml.associationEnd  
	 */
    DELETE_OPEN_OBJ,
    /**
	 * @uml.property  name="dELETE_OPSETS_BETWEEN"
	 * @uml.associationEnd  
	 */
    DELETE_OPSETS_BETWEEN,
    /**
	 * @uml.property  name="dELETE_SESSION"
	 * @uml.associationEnd  
	 */
    DELETE_SESSION,
    /**
	 * @uml.property  name="dO_CREATE_PROCESS"
	 * @uml.associationEnd  
	 */
    DO_CREATE_PROCESS,
    /**
	 * @uml.property  name="eXIT_PROCESS"
	 * @uml.associationEnd  
	 */
    EXIT_PROCESS,
    /**
	 * @uml.property  name="fIND_A_NAME"
	 * @uml.associationEnd  
	 */
    FIND_A_NAME,
    /**
	 * @uml.property  name="gET_ALL_OPS"
	 * @uml.associationEnd  
	 */
    GET_ALL_OPS,
    /**
	 * @uml.property  name="gET_APP_PATH"
	 * @uml.associationEnd  
	 */
    GET_APP_PATH,
    /**
	 * @uml.property  name="gET_CONNECTOR"
	 * @uml.associationEnd  
	 */
    GET_CONNECTOR,
    /**
	 * @uml.property  name="gET_CONTAINER_WITH_PROP"
	 * @uml.associationEnd  
	 */
    GET_CONTAINER_WITH_PROP,
    /**
	 * @uml.property  name="gET_CONTAINERS_OF"
	 * @uml.associationEnd  
	 */
    GET_CONTAINERS_OF,
    /**
	 * @uml.property  name="gET_MELL_CONTAINERS_OF"
	 * @uml.associationEnd  
	 */
    GET_MELL_CONTAINERS_OF,
    /**
	 * @uml.property  name="gET_DASC_OBJECTS"
	 * @uml.associationEnd  
	 */
    GET_DASC_OBJECTS,
    /**
	 * @uml.property  name="gET_DEVICES"
	 * @uml.associationEnd  
	 */
    GET_DEVICES,
    /**
	 * @uml.property  name="gET_EMAIL_ACCT"
	 * @uml.associationEnd  
	 */
    GET_EMAIL_ACCT,
    /**
	 * @uml.property  name="gET_EMAIL_RECIPIENTS"
	 * @uml.associationEnd  
	 */
    GET_EMAIL_RECIPIENTS,
    /**
	 * @uml.property  name="gET_ENTITY_ID"
	 * @uml.associationEnd  
	 */
    GET_ENTITY_ID("getIdOfEntityWithNameAndType"),
    /**
	 * @uml.property  name="gET_ENTITY_NAME"
	 * @uml.associationEnd  
	 */
    GET_ENTITY_NAME("getNameOfEntityWithIdAndType"),
    /**
	 * @uml.property  name="gET_ENTITY_WITH_PROP"
	 * @uml.associationEnd  
	 */
    GET_ENTITY_WITH_PROP,
    /**
	 * @uml.property  name="gET_FILE_CONTENT"
	 * @uml.associationEnd  
	 */
    GET_FILE_CONTENT,
    /**
	 * @uml.property  name="gET_HOST_APP_PATHS"
	 * @uml.associationEnd  
	 */
    GET_HOST_APP_PATHS,
    /**
	 * @uml.property  name="gET_HOST_REPOSITORY"
	 * @uml.associationEnd  
	 */
    GET_HOST_REPOSITORY,
    /**
	 * @uml.property  name="gET_INBOX_MESSAGES"
	 * @uml.associationEnd  
	 */
    GET_INBOX_MESSAGES,
    /**
	 * @uml.property  name="gET_INSTALLED_APP_NAMES"
	 * @uml.associationEnd  
	 */
    GET_INSTALLED_APP_NAMES,
    /**
	 * @uml.property  name="gET_K_STORE_PATHS"
	 * @uml.associationEnd  
	 */
    GET_K_STORE_PATHS,
    /**
	 * @uml.property  name="gET_LAST_ERROR"
	 * @uml.associationEnd  
	 */
    GET_LAST_ERROR,
    /**
	 * @uml.property  name="gET_MEMBERS_OF"
	 * @uml.associationEnd  
	 */
    GET_MEMBERS_OF,
    /**
	 * @uml.property  name="gET_MELL_MEMBERS_OF"
	 * @uml.associationEnd  
	 */
    GET_MELL_MEMBERS_OF,
    /**
	 * @uml.property  name="gET_NAME_OF_ENTITY_WITH_ID_AND_TYPE"
	 * @uml.associationEnd  
	 */
    GET_NAME_OF_ENTITY_WITH_ID_AND_TYPE,
    /**
	 * @uml.property  name="gET_ID_OF_ENTITY_WITH_NAME_AND_TYPE"
	 * @uml.associationEnd  
	 */
    GET_ID_OF_ENTITY_WITH_NAME_AND_TYPE,
    /**
	 * @uml.property  name="gET_OATTRS"
	 * @uml.associationEnd  
	 */
    GET_OATTRS,
    /**
	 * @uml.property  name="gET_OBJ_ATTRS_PROPER"
	 * @uml.associationEnd  
	 */
    GET_OBJ_ATTRS_PROPER,
    /**
	 * @uml.property  name="gET_OBJ_EMAIL_PROPS"
	 * @uml.associationEnd  
	 */
    GET_OBJ_EMAIL_PROPS,
    /**
	 * @uml.property  name="gET_OBJ_INFO"
	 * @uml.associationEnd  
	 */
    GET_OBJ_INFO,
    /**
	 * @uml.property  name="gET_OBJECT_IN_INBOX_OF"
	 * @uml.associationEnd  
	 */
    GET_OBJECT_IN_INBOX_OF,
    /**
	 * @uml.property  name="gET_OBJ_IN_OUTBOX_OF"
	 * @uml.associationEnd  
	 */
    GET_OBJ_IN_OUTBOX_OF,
    /**
	 * @uml.property  name="gET_OBJ_PROPERTIES"
	 * @uml.associationEnd  
	 */
    GET_OBJ_PROPERTIES,
    /**
	 * @uml.property  name="gET_OBJ_NAME_PATH"
	 * @uml.associationEnd  
	 */
    GET_OBJ_NAME_PATH,
    /**
	 * @uml.property  name="gET_OPSETS_BETWEEN"
	 * @uml.associationEnd  
	 */
    GET_OPSETS_BETWEEN,
    /**
	 * @uml.property  name="gET_OPSET_OATTRS"
	 * @uml.associationEnd  
	 */
    GET_OPSET_OATTRS,
    /**
	 * @uml.property  name="gET_OPSET_OPS"
	 * @uml.associationEnd  
	 */
    GET_OPSET_OPS,
    /**
	 * @uml.property  name="gET_OUTBOX_MESSAGES"
	 * @uml.associationEnd  
	 */
    GET_OUTBOX_MESSAGES,
    /**
	 * @uml.property  name="gET_PERMITTED_OPS"
	 * @uml.associationEnd  
	 */
    GET_PERMITTED_OPS,
    /**
	 * @uml.property  name="gET_POLICY_CLASSES"
	 * @uml.associationEnd  
	 */
    GET_POLICY_CLASSES,
    /**
	 * @uml.property  name="gET_POS_CONTAINERS_OF"
	 * @uml.associationEnd  
	 */
    /**
	 * @uml.property  name="gET_POS_MEMBERS_OF"
	 * @uml.associationEnd  
	 */
    /**
	 * @uml.property  name="gET_PROPER_CONTAINERS"
	 * @uml.associationEnd  
	 */
    GET_PROPER_CONTAINERS,
    /**
	 * @uml.property  name="gET_RECORD_INFO"
	 * @uml.associationEnd  
	 */
    GET_RECORD_INFO,
    /**
	 * @uml.property  name="gET_RECORDS"
	 * @uml.associationEnd  
	 */
    GET_RECORDS,
    /**
	 * @uml.property  name="gET_SEL_VOS_GRAPH"
	 * @uml.associationEnd  
	 */
    GET_SEL_VOS_GRAPH,
    /**
	 * @uml.property  name="gET_SEL_VOS_GRAPH_2"
	 * @uml.associationEnd  
	 */
    GET_SEL_VOS_GRAPH_2,
    /**
	 * @uml.property  name="gET_SESSION_ID"
	 * @uml.associationEnd  
	 */
    GET_SESSION_ID,
    /**
	 * @uml.property  name="gET_SESSION_USER"
	 * @uml.associationEnd  
	 */
    GET_SESSION_USER,
    /**
	 * @uml.property  name="gET_SIMPLE_VOS_GRAPH"
	 * @uml.associationEnd  
	 */
    GET_SIMPLE_VOS_GRAPH,
    /**
	 * @uml.property  name="gET_TEMPLATES"
	 * @uml.associationEnd  
	 */
    GET_TEMPLATES,
    /**
	 * @uml.property  name="gET_TEMPLATE_INFO"
	 * @uml.associationEnd  
	 */
    GET_TEMPLATE_INFO,
    /**
	 * @uml.property  name="gET_USERS"
	 * @uml.associationEnd  
	 */
    GET_USERS,
    /**
	 * @uml.property  name="gET_USERS_AND_ATTRS"
	 * @uml.associationEnd  
	 */
    GET_USERS_AND_ATTRS,
    /**
	 * @uml.property  name="gET_USERS_OF"
	 * @uml.associationEnd  
	 */
    GET_USERS_OF,
    /**
	 * @uml.property  name="gET_USER_ATTRIBUTES"
	 * @uml.associationEnd  
	 */
    GET_USER_ATTRIBUTES,
    /**
	 * @uml.property  name="gET_VOS_GRAPH"
	 * @uml.associationEnd  
	 */
    GET_VOS_GRAPH,
    /**
	 * @uml.property  name="gET_VOS_ID_PROPERTIES"
	 * @uml.associationEnd  
	 */
    GET_VOS_ID_PROPERTIES,
    /**
	 * @uml.property  name="gET_POS_NODE_PROPERTIES"
	 * @uml.associationEnd  
	 */
    GET_POS_NODE_PROPERTIES,
    /**
	 * @uml.property  name="gET_VOS_PROPERTIES"
	 * @uml.associationEnd  
	 */
    GET_VOS_PROPERTIES,
    /**
	 * @uml.property  name="iS_IN_POS"
	 * @uml.associationEnd  
	 */
    IS_IN_POS,
    /**
	 * @uml.property  name="iS_OBJ_IN_OATTR_WITH_PROP"
	 * @uml.associationEnd  
	 */
    IS_OBJ_IN_OATTR_WITH_PROP,

    /**
	 * @uml.property  name="iS_PASTING_ALLOWED"
	 * @uml.associationEnd  
	 */
    IS_PASTING_ALLOWED,
    /**
	 * @uml.property  name="iS_TIME_TO_REFRESH"
	 * @uml.associationEnd  
	 */
    IS_TIME_TO_REFRESH,
    /**
	 * @uml.property  name="mAKE_CMD"
	 * @uml.associationEnd  
	 */
    MAKE_CMD,
    /**
	 * @uml.property  name="mAY_SESSION_CLOSE"
	 * @uml.associationEnd  
	 */
    MAY_SESSION_CLOSE,
    /**
	 * @uml.property  name="nEW_OPEN_OBJECT"
	 * @uml.associationEnd  
	 */
    NEW_OPEN_OBJECT,
    /**
	 * @uml.property  name="nEW_READ_FILE"
	 * @uml.associationEnd  
	 */
    NEW_READ_FILE,
    /**
	 * @uml.property  name="nEW_READ_OBJECT"
	 * @uml.associationEnd  
	 */
    NEW_READ_OBJECT,
    /**
	 * @uml.property  name="nEW_WRITE_FILE"
	 * @uml.associationEnd  
	 */
    NEW_WRITE_FILE,
    /**
	 * @uml.property  name="nEW_WRITE_OBJECT"
	 * @uml.associationEnd  
	 */
    NEW_WRITE_OBJECT,
    /**
	 * @uml.property  name="oPEN_OBJECT"
	 * @uml.associationEnd  
	 */
    OPEN_OBJECT,
    /**
	 * @uml.property  name="pASS_CMD_TO_ENGINE"
	 * @uml.associationEnd  
	 */
    PASS_CMD_TO_ENGINE,
    /**
	 * @uml.property  name="pROCESS_EVENT"
	 * @uml.associationEnd  
	 */
    PROCESS_EVENT,
    /**
	 * @uml.property  name="rEAD_NEW_FILE"
	 * @uml.associationEnd  
	 */
    READ_NEW_FILE,
    /**
	 * @uml.property  name="oPEN_OBJECT_3"
	 * @uml.associationEnd  
	 */
    OPEN_OBJECT_3,
    //API command for openobject for reuses openObject3
    /**
	 * @uml.property  name="oPEN_OBJECT_4"
	 * @uml.associationEnd  
	 */
    OPEN_OBJECT_4("openObject3"),
    /**
	 * @uml.property  name="rEAD_OBJECT_3"
	 * @uml.associationEnd  
	 */
    READ_OBJECT_3,
    /**
	 * @uml.property  name="rEQUEST_PERMS"
	 * @uml.associationEnd  
	 */
    REQUEST_PERMS,
    /**
	 * @uml.property  name="sEND_OBJECT"
	 * @uml.associationEnd  
	 */
    SEND_OBJECT,
    /**
	 * @uml.property  name="sEND_SIMPLE_MSG"
	 * @uml.associationEnd  
	 */
    SEND_SIMPLE_MSG,
    /**
	 * @uml.property  name="sET_K_STORE_PATHS"
	 * @uml.associationEnd  
	 */
    SET_K_STORE_PATHS,
     /**
	 * @uml.property  name="sET_HOST_APP_PATHS"
	 * @uml.associationEnd  
	 */
    SET_HOST_APP_PATHS,
     /**
	 * @uml.property  name="sET_PERMS"
	 * @uml.associationEnd  
	 */
    SET_PERMS,
    /**
	 * @uml.property  name="sET_RECORD_KEYS"
	 * @uml.associationEnd  
	 */
    SET_RECORD_KEYS,
    /**
	 * @uml.property  name="sET_STARTUPS"
	 * @uml.associationEnd  
	 */
    SET_STARTUPS,
    /**
	 * @uml.property  name="sPAWN_SESSION"
	 * @uml.associationEnd  
	 */
    SPAWN_SESSION,
    /**
	 * @uml.property  name="wOULD_OPEN_PREVENT_SAVE"
	 * @uml.associationEnd  
	 */
    WOULD_OPEN_PREVENT_SAVE,
    /**
	 * @uml.property  name="wRITE_OBJECT_3"
	 * @uml.associationEnd  
	 */
    WRITE_OBJECT_3;

    /**
	 * @uml.property  name="cmdCode"
	 */
    private String cmdCode = null;
    
    private static Log log = new Log(Log.Level.INFO, true);

    PMCommand(String commandCode){
        cmdCode = commandCode;
    }
    PMCommand(){
        this(null);
    }

    public String commandCode() {
        if(cmdCode == null){
            cmdCode = toCommandCode(name());
           	if (cmdCode.equals("getMembersOf")) {
            	log.debugStackCall("!!!!!!! COMMAND CODE1 IS: " + cmdCode);	
        	}
        }

        
        return cmdCode;
    }

    public static String toCommandCode(String name) {
    	
    	// STEVE - Added (3/24/15)
    	if (name.equals("GET_MEMBERS_OF")) {
        	log.debugStackCall("!!!!!!! COMMAND CODE2 IS: " + name);	
    	}

        List<MatchResult> results = new ArrayList();
        Matcher underscores = Pattern.compile("_").matcher(name);
        while (underscores.find()) {
            results.add(underscores.toMatchResult());
        }
        StringBuilder sb = new StringBuilder(name.toLowerCase());

        for (MatchResult result : Lists.reverse(results)) {
            sb.replace(result.end(), result.end() + 1, sb.substring(result.end(), result.end() + 1).toUpperCase());
            sb.delete(result.start(), result.end());
        }
    	System.out.println("COMMAND CODE OUT: " + sb.toString());

        return sb.toString();
    }
}



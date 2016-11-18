/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.application;

import gov.nist.csd.pm.common.model.ObjectAttribute;
import gov.nist.csd.pm.common.net.Packet;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author  Administrator
 */
public interface SysCaller {
    public static final String PM_ALT_DELIM_PATTERN = "\\|";
    public static final String PM_ALT_FIELD_DELIM = "|";
    public static final String PM_PROPERTY_DELIM = "=";
    public static final String PM_ARG = "arg ";
    public static final String PM_BYE = "bye ";
    public static final String PM_CLASS_FILE_NAME = "File";
    public static final String PM_CMD = "cmd ";
    public static final String PM_CONNECTOR_ID = "1";
    public static final int PM_OATTR_NAME_INDEX = 0;
    public static final int PM_OATTR_ID_INDEX = 1;
    public static final String PM_CONNECTOR_NAME = "PM";
    public static final String PM_DATA = "data";
    public static final String PM_EOC = "eoc ";
    public static final String PM_EOD = "eod ";
    public static final String PM_EVENT_CREATE_OBJECT = "Create object";
    public static final String PM_FAILURE = "err ";
    public static final String PM_FIELD_DELIM = ":";
    public static final String PM_IMPORT_COMMENT_START = "#";
    public static final String PM_INFO_USER_SELECTS_CONTAINERS = "User selects containers";
    public static final String PM_LIST_MEMBER_SEP = ",";
    public static final String PM_NODE_ASSOC = "o";
    public static final String PM_NODE_ASSOCA = "O";
    public static final String PM_NODE_AUATTR = "aa";
    public static final String PM_NODE_CONN = "c";
    public static final String PM_NODE_CONNA = "C";
    public static final String PM_NODE_OATTR = "b";
    public static final String PM_TEMPLATE = "tpl";
    public static final String PM_VOS_PRES_ADMIN = "admin";
    public static final String PM_VOS_PRES_USER = "user";

    //Configuration Information
    /**
	 * @uml.property  name="socketClient"
	 * @uml.associationEnd  
	 */
    SSLSocketClient getSocketClient();

    //Communication Information


    boolean addRecordKeys(String sRecName, HashMap keyMap);

    boolean addScript(List<String> script);

    boolean addTemplate(String sName, List<String> containers, List<String> keys);

    boolean assignObjToContainer(String sObjName, String sContainer);

    boolean assignObjToInboxOf(String sObjName, String sUserName);

    boolean assignObjToOutboxOf(String sObjName, String sUserName);

    boolean assignObjToWinboxOf(String sObjName, String sUserName);

    Packet bye();

    int closeObject(String sHandle);

    String copyObject(String sObjName);

    boolean copyToClipboard(String sHandle, String sSelText);

    boolean createLinkedObjects();

    String createObject3(String sObjName, String sObjClass, String sObjType, String sContainers, String sPerms, String sSender, String sRecip, String sSubject, String sAttached);

    String createProcess();

    /**
     * Creates a container inside the specified parent attribute.
     *
     *
     * @param containerSpec - an object attribute with a name specified.
     * @param parentAttribute - an object attribute with an id or name specified.  This object attribute must already
     * exist in the policy machine.
     * @return
     */
    String addContainer(ObjectAttribute containerSpec, ObjectAttribute parentAttribute);

    Packet createRecord(String sRecName, String sBase, String sBaseType, String sTemplateId, String sCompos, HashMap keyMap);

    Packet createRecordInEntityWithProp(String sRecName, String sBaseProp, String sBaseType, String sTemplateId, String sCompos, HashMap keyMap);

    public Map<String, String> getPropertiesFor(String id);

    public boolean setPropertyFor(String objId, String key, String value);

    boolean deassignObjFromHomeOf(String sObjName, String sUserName);

    boolean deassignObjFromInboxOf(String sObjName, String sUserName);


    boolean deassignObjFromOutboxOf(String sObjName, String sUserName);

    boolean deassignObjFromOattr3(String sObjName, String sProp);
    
    boolean addPropToOattr(String id, String isVos, String key, String value);
    
    public boolean deleteObject(String sObjId, String type);
    
    Packet deleteOpsetsBetween(String sUattrName, String sOattrName);
    

    /**
     * This should not be called directly by an application rather you should use the  in the application manager for closing the application.
     * @param sPid the process id to be terminated
     * @return  if the process was successfully terminated
     */
    boolean exitProcess(String sPid);

    String findAName(String sFileName);

    /**
     * TODO:This method has a duplicate in OsTaskBar
     * @param appName
     * The name of the application whose path you need
     * @return At index zero the applications path, at index 1 the applications
     * prefix, at index 2 the applications main class
     */
    String[] getAppPath(String appName);

    String[] getConnector();

    String getNameOfContainerWithProperty(String sProp);

    Packet getEmailAcct(String sUser);

    HashSet getEmailRecipients(String sReceiver);

    String getIdOfEntityWithNameAndType(String sName, String sType);

    String getNameOfEntityWithIdAndType(String sId, String sType);

    String getNameOfEntityWithPropertyAndType(String sEntProp, String sEntType);
    String getIdOfEntityWithPropertyAndType(String sEntProp, String sEntType);
    String[] getInfoOfEntityWithPropertyAndType(String sEntProp, String sEntType);

    /**
     * Returns the pm type of the given entity
     * @param eId
     * @return
     */
    String getEntityType(String eId);

    byte[] getFileContent(String sFileProp);

    /**
	 * @uml.property  name="hostAppPaths"
	 * @uml.associationEnd  
	 */
    Packet getHostAppPaths();

    /**
	 * @uml.property  name="hostRepository"
	 */
    String getHostRepository();

    /**
	 * @uml.property  name="inboxMessages"
	 * @uml.associationEnd  
	 */
    Packet getInboxMessages();

    /**
	 * @uml.property  name="kStorePaths"
	 * @uml.associationEnd  
	 */
    Packet getKStorePaths();

    /**
	 * @uml.property  name="lastError"
	 */
    String getLastError();

    List<String[]> getMembersOf(String sName, String sId, String sType, String sGraphType);

    List<String[]> getMellMembersOf(String sName, String sId, String sType, String sGraphType);

    /*
    ObjectInfo known indexes, INCOMPLETE!
     */
    public static final int IDX_GET_OBJ_INFO_NAME= 0;
    public static final int IDX_GET_OBJ_INFO_ID = 1;
    public static final int IDX_GET_OBJ_INFO_CLASS = 2;
    public static final int IDX_GET_OBJ_INFO_INCLUDES = 3;
    public static final int IDX_GET_OBJ_INFO_HOST = 4;
    public static final int IDX_GET_OBJ_INFO_PATH = 5;
    Packet getObjInfo(String sObjId);

    String getObjectPath(String sObjName);


    /**
	 * @uml.property  name="outboxMessages"
	 * @uml.associationEnd  
	 */
    Packet getOutboxMessages();
    
    /**
	 * @uml.property  name="properContainers"
	 * @uml.associationEnd  
	 */
    Packet getProperContainers();

    Packet getRecordInfo(String sContId);

    Packet getRecords(String sTplId, String sKey);


    /**
	 * @uml.property  name="sessionId"
	 */
    String getSessionId();

    /**
	 * @uml.property  name="sessionUser"
	 */
    String getSessionUser();

    Packet getTemplateInfo(String sTplId);

    /**
	 * @uml.property  name="templates"
	 * @uml.associationEnd  
	 */
    Packet getTemplates();


    Packet getUsersOf(String sUattr);



    boolean isObjInInboxOf(String sObjName, String sUserName);

    boolean isObjInOutboxOf(String sObjName, String sUserName);

    boolean isPastingAllowed();



    String openObject3(String sObjName, String sPerms);

    PMIOObject openObject4(String sObjName, String sPerms);

    byte[] readObject3(String sHandle);

    Packet readObject3(String sHandle, OutputStream out);

    int writeObject3(String sHandle, byte[] buf);


    Packet setPerms(String sUattr, String sOpset, String sOattr, String sBase, String sBaseType, String sPerms, String sEntityName, String sEntityType, String sYesNo);

    boolean setRecordKeys(String sRecName, HashMap keyMap);

    /**
     * Embue a pm entity with a set of permissions.
     * @param id the id of the entity we wish to set permissions on.
     * @param permissions the permissions we'd like to set (e.g. "File read", "File write")
     * @return true if the permissions were successfully set, false otherwise.
     */
    boolean setPermissions(String id, String... permissions);
}

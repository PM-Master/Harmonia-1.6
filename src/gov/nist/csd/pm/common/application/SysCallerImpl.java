package gov.nist.csd.pm.common.application;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.model.ObjectAttribute;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static gov.nist.csd.pm.common.info.PMCommand.*;
import static gov.nist.csd.pm.common.net.LocalHostInfo.getLocalHost;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import static gov.nist.csd.pm.common.util.CommandUtil.makeCmd;
import static gov.nist.csd.pm.common.util.collect.Arrays.getOrElse;
import static gov.nist.csd.pm.common.util.collect.Arrays.isNullOrEmpty;

@SuppressWarnings("CallToThreadDumpStack")
public class SysCallerImpl implements SysCaller  {

    /**
	 * @uml.property  name="sSessionId"
	 */
    String sSessionId;
    /**
	 * @uml.property  name="sProcessId"
	 */
    String sProcessId;
    /**
	 * @uml.property  name="nSimulatorPort"
	 */
    int nSimulatorPort;
    /**
	 * @uml.property  name="simClient"
	 * @uml.associationEnd  
	 */
    SSLSocketClient simClient;
    /**
	 * @uml.property  name="sLastError"
	 */
    String sLastError;
    /**
	 * @uml.property  name="openObjects"
	 * @uml.associationEnd  inverse="this$0:gov.nist.csd.pm.common.application.SysCallerImpl$OpenObject" qualifier="sHandle:java.lang.String gov.nist.csd.pm.common.application.SysCallerImpl$OpenObject"
	 */
    HashMap openObjects;

    public SysCallerImpl(int nSimPort, String sSessId, String sProcId,
            boolean bDebug, String sPrefix) {
        this.sSessionId = sSessId;
        this.sProcessId = sProcId;

        nSimulatorPort = (nSimPort < 1024) ? PM_DEFAULT_SIMULATOR_PORT
                : nSimPort;
        try {
            simClient = new SSLSocketClient("localhost", nSimulatorPort,
                    bDebug, sPrefix);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to create SSL socket to talk to the KSim of the local host!");
            e.printStackTrace();
            System.exit(-1);
        }
        openObjects = new HashMap();
    }

    public List<String[]> getMembersOf(String sName, String sId, String sType,
            String sGraphType) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_MEMBERS_OF, sSessionId, sName, sId, sType,
                    sGraphType);
            Packet res = simClient.sendReceive(cmd, null);
            if (res == null) {
                sLastError = "Null result from getMembersOf()!";
                return null;
            }
            
            //System.out.println("$$$$$$$$ RES2: " + res.size());
            //System.out.println("$$$$$$$$ RES2 isAcc: " + res.getItem(res.size() - 1));
            
            //System.exit(0);
            
            if (res.hasError()) {
                sLastError = res.getErrorMessage();
                return null;
            }
            List<String[]> v = new ArrayList<String[]>();
            for (int i = 0; i < res.size(); i++) {
                String sLine = res.getStringValue(i);
                String[] pieces = sLine.split(PM_FIELD_DELIM);
                v.add(new String[]{pieces[0], pieces[1], pieces[2]});
            }
            return v;
        } catch (Exception e) {
            sLastError = e.getMessage();
            e.printStackTrace();
            return null;
        }
    }
    
    public List<String[]> getMellMembersOf(String sName, String sId, String sType,
            String sGraphType) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_MELL_MEMBERS_OF, sSessionId, sName, sId, sType,
                    sGraphType);
            Packet res = simClient.sendReceive(cmd, null);
            if (res == null) {
                sLastError = "Null result from getMellMembersOf()!";
                return null;
            }
            
            //System.out.println("$$$$$$$$ RES2: " + res.size());
            //System.out.println("$$$$$$$$ RES2 isAcc: " + res.getItem(res.size() - 1));
            
            //System.exit(0);
            
            if (res.hasError()) {
                sLastError = res.getErrorMessage();
                return null;
            }
            List<String[]> v = new ArrayList<String[]>();
            for (int i = 0; i < res.size(); i++) {
                String sLine = res.getStringValue(i);
                String[] pieces = sLine.split(PM_FIELD_DELIM);
                v.add(new String[]{pieces[0], pieces[1], pieces[2]});
            }
            return v;
        } catch (Exception e) {
            sLastError = e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    public String[] getConnector() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_CONNECTOR, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res == null) {
                sLastError = "Null result from getConnector()!";
                return null;
            }
            if (res.hasError()) {
                sLastError = res.getErrorMessage();
                return null;
            }
            String sLine = res.getStringValue(0);
            String[] pieces = sLine.split(PM_FIELD_DELIM);
            return new String[]{pieces[0], pieces[1], pieces[2]};
        } catch (Exception e) {
            sLastError = e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    public Packet bye() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(BYE, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in bye: " + e.getMessage();
            return failurePacket("Exception in bye: " + e.getMessage());
        }
    }

    public String getLastError() {
        return sLastError;
    }

    // Creates a new process in the current session and returns its pid.
    public String createProcess() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(CREATE_PROCESS, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res == null) {
                sLastError = "createProcess() returned null!";
                return null;
            }
            if (res.size() < 1) {
                sLastError = "no process id returned in createProcess()!";
                return null;
            }
            if (res.hasError()) {
                sLastError = "Error in createProcess(): "
                        + res.getErrorMessage();
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in createProcess(): " + e.getMessage();
            return null;
        }
    }

    @Override
    public String addContainer(ObjectAttribute containerSpec, ObjectAttribute parentAttribute) {
        checkNotNull(containerSpec);
        checkNotNull(parentAttribute);
        checkArgument(!Strings.isNullOrEmpty(parentAttribute.id().get()), "Parent attribute id cannot be null or empty");

        String name = containerSpec.name().get();
        String parentId = parentAttribute.id().get();
        String parentType = PmNodeType.OBJECT_ATTRIBUTE.typeCode();
        String description = "Description for " + name;
        String information = "Information for " + name;
        Packet cmd = null;
        try {

            /*
            Corresponds to
            private Packet addOattr(String sSessId, String sProcId, String sName,
            String sDescr, String sInfo, String sBaseId, String sBaseType,
            String sBaseIsVos, String sAssocObjId, String[] sProps) {
             */
            cmd = CommandUtil.makeCmd(ADD_OATTR, sSessionId, sProcessId, name, description, information, parentId, parentType, PmNodeType.NODE.typeCode());
            Packet response =getSocketClient().sendReceive(cmd, null);
            if (response.hasError()) {
               throw new RuntimeException(response.getErrorMessage());
            }
            String result = response.getItemStringValue(0);
            String[] parts = result.split(PM_FIELD_DELIM);
            containerSpec.id().set(parts[PM_OATTR_ID_INDEX]);
            return parts[PM_OATTR_ID_INDEX];
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        System.out.println("Calling command " + cmd);
        return "";
    }

    /**
     * This should not be called directly by an application rather you should use the  in the application manager for closing the application.
     * @param sPid the process id to be terminated
     * @return  if the process was successfully terminated
     */
    public boolean exitProcess(String sPid) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(EXIT_PROCESS, sSessionId, sPid);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in exitProcess: " + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in exitProcess: " + e.getMessage();
            return false;
        }
    }

    public Packet getUsersOf(String sUattr) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_USERS_OF, sSessionId, sUattr);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getUsersOf: " + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getUsersOf: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }


    public String getSessionId() {
        return sSessionId;
    }

    /**
     * TODO:This method has a duplicate in OsTaskBar
     * @param appName
     *            The name of the application whose path you need
     * @return At index zero the applications path, at index 1 the applications
     *         prefix, at index 2 the applications main class
     */
    public String[] getAppPath(String appName) {
        Packet resp = null;
        String[] returnValue = new String[3];
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String hostName = addr.getHostName();
            Packet cmd = makeCmd(GET_HOST_APP_PATHS, sSessionId, hostName,
                    appName);
            resp = simClient.sendReceive(cmd, null);
            if (resp == null || resp.hasError()) {
                return null;
            }
            returnValue[0] = resp.getItemStringValue(0);
            returnValue[1] = resp.getItemStringValue(1);
            returnValue[2] = resp.getItemStringValue(2);
            return returnValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Packet getKStorePaths() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_K_STORE_PATHS, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getKStorePaths: "
                        + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getKStorePaths: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }


  public Packet getHostAppPaths() {
    sLastError = null;
    try {
      Packet cmd = makeCmd(GET_HOST_APP_PATHS, sSessionId, getLocalHost());
      Packet res = simClient.sendReceive(cmd, null);
      if (res.hasError()) {
        sLastError = "Error in getHostAppPaths: " + res.getErrorMessage();
      }
      return res;
    } catch (Exception e) {
      e.printStackTrace();
      sLastError = "Exception in getHostAppPaths: " + e.getMessage();
      return failurePacket(sLastError);
    }
  }

    public Packet getObjInfo(String sObjId) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_OBJ_INFO, sSessionId, sObjId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getObjInfo: " + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getObjInfo: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }

    public String getNameOfEntityWithIdAndType(String sId, String sType) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_NAME_OF_ENTITY_WITH_ID_AND_TYPE, sSessionId, sId, sType);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getNameOfEntityWithIdAndType: " + res.getErrorMessage();
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getNameOfEntityWithIdAndType: " + e.getMessage();
            return null;
        }
    }

    public String getIdOfEntityWithNameAndType(String sName, String sType) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_ID_OF_ENTITY_WITH_NAME_AND_TYPE, sSessionId, sName, sType);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getIdOfEntityWithNameAndType: " + res.getErrorMessage();
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getIdOfEntityWithNameAndType: " + e.getMessage();
            return null;
        }
    }

    // Get the list of messages from the INBOX container of the current user.
    // Returns a Packet, each item containing the following info
    // about a message: <label>|<date>|<from>|<subject>.
    public Packet getInboxMessages() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_INBOX_MESSAGES, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getInboxMessages: "
                        + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getInboxMessages: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }

    public Packet getOutboxMessages() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_OUTBOX_MESSAGES, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getOutboxMessages: "
                        + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getOutboxMessages: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }

    public boolean isObjInInboxOf(String sObjName, String sUserName) {
        sLastError = null;
        Packet res = null;
        try {
            Packet cmd = makeCmd(IS_OBJ_IN_OATTR_WITH_PROP, sSessionId, sObjName,
                    "inboxof=" + sUserName);
            res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in isObjInInboxOf: "
                        + res.getErrorMessage();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in isObjInInboxOf: " + e.getMessage();
            return false;
        }
        if (res.getStringValue(0).equals("yes")) {
            return true;
        }
        return false;
    }

    public boolean isObjInOutboxOf(String sObjName, String sUserName) {
        sLastError = null;
        Packet res = null;
        try {
            Packet cmd = makeCmd(IS_OBJ_IN_OATTR_WITH_PROP, sSessionId, sObjName,
                    "outboxof=" + sUserName);
            res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in isObjInOutboxOf: "
                        + res.getErrorMessage();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in isObjInOutboxOf: " + e.getMessage();
            return false;
        }
        if (res.getStringValue(0).equals("yes")) {
            return true;
        }
        return false;
    }

    public boolean deassignObjFromInboxOf(String sObjName, String sUserName) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(DEASSIGN_OBJ_FROM_OATTR_WITH_PROP, sSessionId,
                    sProcessId, sObjName, "inboxof=" + sUserName);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in deassignObjFromInboxOf: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in deassignObjFromInboxOf: "
                    + e.getMessage();
            return false;
        }
    }

    public boolean deassignObjFromOutboxOf(String sObjName, String sUserName) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(DEASSIGN_OBJ_FROM_OATTR_WITH_PROP, sSessionId,
                    sProcessId, sObjName, "outboxof=" + sUserName);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in deassignObjFromOutboxOf: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in deassignObjFromOutboxOf: "
                    + e.getMessage();
            return false;
        }
    }

    public boolean deassignObjFromHomeOf(String sObjName, String sUserName) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(DEASSIGN_OBJ_FROM_OATTR_WITH_PROP, sSessionId,
                    sProcessId, sObjName, "homeof=" + sUserName);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in deassignObjFromHomeOf: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in deassignObjFromHomeOf: "
                    + e.getMessage();
            return false;
        }
    }
    /**
     *  public boolean setPropertyFor(String id, String key, String value) {


        String entityTypeCode = getEntityType(id);
        PmNodeType entityType  = PmNodeType.typeForCode(entityTypeCode);
        //Remapping entity type from file to object
        //in order to add property.
        if(!validEntityTypes.contains(entityType)){
            System.out.println("warning, attempting to set property for entity of invalid type " + entityType);
        }
        String isVos = "yes";
        String propPair = String.format("%s=%s", key, value);
        System.out.printf("SysCaller: setPropertyFor(id: %s,key: %s,value: %s) with type %s\n", id, key, value, entityTypeCode);
        try {
            Packet command = makeCmd(PMCommand.ADD_PROP, getSessionId(), id, entityTypeCode, isVos, propPair);
            Packet response  = simClient.sendReceive(command, null);
            return !response.hasError();
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return false;
    }
     */
    
    public boolean addPropToOattr(String id, String isVos, String key, String value){
    	 sLastError = null;
    	 String propPair = String.format("%s=%s", key, value);
    	 String entityTypeCode = PmNodeType.OBJECT_ATTRIBUTE.typeCode();//getEntityType(id);
    	 //PmNodeType entityType = PmNodeType.typeForCode(entityTypeCode);
         System.out.printf("SysCaller: setPropertyFor(id: %s,key: %s,value: %s) with type %s\n", id, key, value, entityTypeCode);
         try {
             Packet cmd = makeCmd(ADD_PROP, getSessionId(), id, entityTypeCode, isVos, propPair);
             Packet res = simClient.sendReceive(cmd, null);
             if (res.hasError()) {
                 sLastError = "Error in addPropToOattr: "
                         + res.getErrorMessage();
                 return false;
             }
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             sLastError = "Exception in addPropToOattr: "
                     + e.getMessage();
             return false;
         }
    }
    
    public boolean deassignObjFromOattr3(String sObjName, String sProp) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(DEASSIGN_OBJ_FROM_OATTR_WITH_PROP, sSessionId,
                    sProcessId, sObjName, sProp);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in deassignObjFromHomeOf: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in deassignObjFromHomeOf: "
                    + e.getMessage();
            return false;
        }
    }
    
    /*
     * doesn't work
     */
    public boolean deleteObject(String sObjId, String type){
    	sLastError = null;
    	type = PmNodeType.OBJECT.typeCode();
    	System.out.println("Deleting object with id: " + sObjId + " of type " + type + "\nsSessionId = " + getSessionId());
    	try{
    		Packet cmd = makeCmd(DELETE_NODE, getSessionId(), sObjId, type, "yes");
    		Packet res = simClient.sendReceive(cmd, null);
    		if (res.hasError()) {
                sLastError = "Error in deleteObject: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in deleteObject: "
                    + e.getMessage();
            return false;
        }
    }
    
    public boolean assignObjToWinboxOf(String sObjName, String sUserName) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(ASSIGN_OBJ_TO_OATTR_WITH_PROP, sSessionId,
                    sProcessId, sObjName, "winboxof=" + sUserName);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in assignObjToWinboxOf: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in assignObjToWinboxOf: " + e.getMessage();
            return false;
        }
    }

    public boolean assignObjToInboxOf(String sObjName, String sUserName) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(ASSIGN_OBJ_TO_OATTR_WITH_PROP, sSessionId,
                    sProcessId, sObjName, "inboxof=" + sUserName);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in assignObjToInboxOf: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in assignObjToInboxOf: " + e.getMessage();
            return false;
        }
    }

    public boolean assignObjToOutboxOf(String sObjName, String sUserName) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(ASSIGN_OBJ_TO_OATTR_WITH_PROP, sSessionId,
                    sProcessId, sObjName, "outboxof=" + sUserName);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in assignObjToOutboxOf: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in assignObjToOutboxOf: " + e.getMessage();
            return false;
        }
    }

    public boolean assignObjToContainer(String sObjName, String sContainer) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(ASSIGN_OBJ_TO_OATTR, sSessionId, sProcessId,
                    sObjName, sContainer);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in assignObjToContainer: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in assignObjToContainer: " + e.getMessage();
            return false;
        }
    }



    public Packet deleteOpsetsBetween(String sUattrName, String sOattrName) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(DELETE_OPSETS_BETWEEN, sSessionId, sProcessId,
                    sUattrName, sOattrName);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in deleteOpsetsBetween: "
                        + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in deleteOpsetsBetween: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }

    // getEmailAcct returns:
    // item 0: <user name>:<user id>
    // item 1: <'coming from' name>
    // item 2: <email address>
    // item 3: <incoming server>
    // item 4: <outgoing server>
    // item 5: <account name>.
    public Packet getEmailAcct(String sUser) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_EMAIL_ACCT, sSessionId, sUser);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getEmailAcct: " + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getEmailAcct: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }

    public boolean addTemplate(String sName, List<String> containers, List<String> keys) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(ADD_TEMPLATE, sSessionId, sName);
            StringBuffer sb = new StringBuffer();
            String s;
            for (int i = 0; i < containers.size(); i++) {
                if (i == 0) {
                    sb.append(containers.get(i));
                } else {
                    sb.append(PM_FIELD_DELIM);
                    sb.append(containers.get(i));
                }
            }
            s = sb.toString();
            if (s.length() > 0) {
                cmd.addItem(ItemType.CMD_ARG, s);
            } else {
                cmd.addItem(ItemType.CMD_ARG, "");
            }

            sb = new StringBuffer();
            for (int i = 0; i < keys.size(); i++) {
                if (i == 0) {
                    sb.append((String) keys.get(i));
                } else {
                    sb.append(PM_FIELD_DELIM);
                    sb.append((String) keys.get(i));
                }
            }
            s = sb.toString();
            if (s.length() > 0) {
                cmd.addItem(ItemType.CMD_ARG, s);
            } else {
                cmd.addItem(ItemType.CMD_ARG, "");
            }

            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in addTemplate: " + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in addTemplate: " + e.getMessage();
            return false;
        }
    }

    public Packet getTemplateInfo(String sTplId) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_TEMPLATE_INFO, sSessionId, sTplId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getTemplateInfo: "
                        + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getTemplateInfo: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }

    public Packet getTemplates() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_TEMPLATES, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getTemplates: " + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getTemplates: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }

    public Packet getRecordInfo(String sContId) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_RECORD_INFO, sSessionId, sContId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getRecordInfo: " + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getRecordInfo: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }

    public Packet getRecords(String sTplId, String sKey) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_RECORDS, sSessionId,
                    (sTplId == null) ? "" : sTplId, (sKey == null) ? "" : sKey);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getRecords: " + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getRecords: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }


    public Packet getProperContainers() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_OBJ_ATTRS_PROPER, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getProperContainers: "
                        + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getProperContainers: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }



    // This function checks whether the argument is a user name or a
    // user attribute name. It returns a set containing the user or the
    // users of that attribute, or null if the argument is not a user
    // or a user attribute.
    public HashSet getEmailRecipients(String sReceiver) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_EMAIL_RECIPIENTS, sSessionId, sReceiver);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getEmailRecipients: "
                        + res.getErrorMessage();
                return null;
            }
            HashSet hs = new HashSet();
            for (int i = 0; i < res.size(); i++) {
                hs.add(res.getStringValue(i));
            }
            return hs;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getEmailRecipients: " + e.getMessage();
            return null;
        }
    }

    public Packet setPerms(String sUattr, String sOpset, String sOattr,
            String sBase, String sBaseType, String sPerms, String sEntityName,
            String sEntityType, String sYesNo) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(SET_PERMS, sSessionId, sProcessId, sUattr,
                    (sOpset == null) ? "" : sOpset, (sOattr == null) ? ""
                    : sOattr, (sBase == null) ? "" : sBase,
                    (sBaseType == null) ? "" : sBaseType, sPerms, sEntityName,
                    sEntityType, sYesNo);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in setPerms: " + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in setPerms: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }



    // Convenience API:
    public String getNameOfContainerWithProperty(String sProp) {
        String sEntity = getNameOfEntityWithPropertyAndType(sProp, "b");
        // Other processing if needed...
        return sEntity;
    }

    public String[] getInfoOfEntityWithPropertyAndType(String sEntProp, String sEntType){
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_ENTITY_WITH_PROP, sSessionId, sEntType,
                    sEntProp);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getEntityWithProp: "
                        + res.getErrorMessage();
                return null;
            }
            if (res.size() <= 0) {
                sLastError = "No entity with the requested type and property!";
                return null;
            }
            String sLine = res.getStringValue(0);
            String[] pieces = sLine.split(":");
            //re-add type to the end of this info block
            String[] morePieces = Arrays.copyOf(pieces, pieces.length + 1);
            morePieces[pieces.length] = sEntType;
            return morePieces;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getEntityWithProp: " + e.getMessage();
            return null;
        }
    }

    @Override
    public String getEntityType(String eId) {
        System.out.printf("SysCaller: getEntityType(eId: %s)\n", eId);
        /**
         * Fails here
         */
        Packet response = getObjInfo(eId);
        String item = response.getStringValue(0);
        if(response.hasError()){
            throw new RuntimeException(response.getErrorMessage());
        }
        if(Strings.isNullOrEmpty(item)){
            throw new RuntimeException("Invalid or empty result returned in getEntityType");
        }
        String[] returnedInfo = item.split(SysCaller.PM_ALT_DELIM_PATTERN);
        return getOrElse(returnedInfo, IDX_GET_OBJ_INFO_CLASS, PmNodeType.INVALID.typeCode());
    }

    public String getIdOfEntityWithPropertyAndType(String sEntProp, String sEntType){
        String[] entityInfo = getInfoOfEntityWithPropertyAndType(sEntProp, sEntType);
        return isNullOrEmpty(entityInfo) ? null : entityInfo[1];
    }

    public String getNameOfEntityWithPropertyAndType(String sEntProp, String sEntType) {
        String[] entityInfo = getInfoOfEntityWithPropertyAndType(sEntProp, sEntType);
        return isNullOrEmpty(entityInfo) ? null : entityInfo[0];
    }

    // Get the current session user name.
    public String getSessionUser() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_SESSION_USER, sSessionId, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getSessionUser: "
                        + res.getErrorMessage();
                return null;
            }
            if (res.size() <= 0) {
                sLastError = "Empty response from gegtSessionUser!";
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getSessionUser: " + e.getMessage();
            return null;
        }
    }

    // Ask the engine for the VOS graph visible by the user (the user sees
    // all objects and object attributes on which he has some permissions).
    public Object getVosGraph(String sAnchorId, String sAnchorLabel,
            String sAnchorType, int level) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_SIMPLE_VOS_GRAPH, null, sSessionId,
                    sAnchorId, sAnchorLabel, sAnchorType, String.valueOf(level));
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getSimpleVosGraph: "
                        + res.getErrorMessage();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getSimpleVosGraph: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }



    // For objects of class File, Directory, get the object's underlying file
    // (its full path).
    public String getObjectPath(String sObjName) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_OBJ_NAME_PATH, sSessionId, sObjName);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getObjNamePath: "
                        + res.getErrorMessage();
                return null;
            }
            if (res.size() <= 0) {
                sLastError = "Empty response from getObjNamePath!";
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getSessionUser: " + e.getMessage();
            return null;
        }
    }

    public byte[] getFileContent(String sFileProp) {
        sLastError = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Packet cmd = makeCmd(GET_FILE_CONTENT, sSessionId, sFileProp);

            Packet res = simClient.sendReceive(cmd, baos);

            if (res.hasError()) {
                sLastError = res.getErrorMessage();
                return null;
            }

            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getFileContent: " + e.getMessage();
            return null;
        }
    }



    // Open an object for a set of operations.
    // If successful, return an open object handle,
    // otherwise return null.
    public String openObject3(String sObjName, String sPerms) {
        return openObject(OPEN_OBJECT_3,sObjName, sProcessId, sObjName, sPerms);
    }

    public String openObject(PMCommand cmdCode,String sObjName, String... cmdArgs){
        sLastError = null;
        try {
            Packet cmd = makeCmd(cmdCode, sSessionId, cmdArgs);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = String.format("Error in %s: %s", cmdCode,res.getErrorMessage());
                return null;
            }
            if (res.size() <= 0) {
                sLastError = String.format("Empty response from %s!", cmdCode);
                return null;
            }
            String sHandle = res.getStringValue(0);
            // Record the open object in openObjects.
            OpenObject oo = new OpenObject(sObjName);
            openObjects.put(sHandle, oo);

            // Return the object handle.
            return sHandle;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in openObject3: " + e.getMessage();
            return null;
        }
    }

    public PMIOObject openObject4(String sObjName, String sPerms){

        return new PMIOObjectImpl(this, sObjName, sPerms);
    }

    public byte[] readObject3(String sHandle) {
        sLastError = null;

        OpenObject oo = (OpenObject)openObjects.get(sHandle);
        if (oo == null) {
          sLastError = "Invalid handle in readObject3()!";
          return null;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
          Packet cmd = makeCmd(READ_OBJECT_3, sSessionId, sProcessId, sHandle);
          Packet res = simClient.sendReceive(cmd, baos);
          
          if (res.hasError()) {
            sLastError = res.getErrorMessage();
            return null;
          }
          
          return baos.toByteArray();
          
        } catch (Exception e) {
          e.printStackTrace();
          sLastError = "Exception in readObject3(): " + e.getMessage();
          return null;
        }
      }

      public Packet readObject3(String sHandle, OutputStream out) {
        sLastError = null;

        OpenObject oo = (OpenObject)openObjects.get(sHandle);
        if (oo == null) {
          sLastError = "Invalid handle in readObject3()!";
          return failurePacket("Invalid handle in readObject3()!");
        }

        try {
          Packet cmd = makeCmd(READ_OBJECT_3, sSessionId, sProcessId, sHandle);
          Packet res = simClient.sendReceive(cmd, out);
          return res;      
        } catch (Exception e) {
          e.printStackTrace();
          sLastError = "Exception in readObject3: " + e.getMessage();
          return failurePacket("Exception in readObject3()!");
        }
      }

    // Returns 0 (success) or a negative integer (failure).
    public int closeObject(String sHandle) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(CLOSE_OBJECT, sSessionId, sHandle);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in closeObject: " + res.getErrorMessage();
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in closeObject: " + e.getMessage();
            return -2;
        }
        OpenObject oo = (OpenObject) openObjects.get(sHandle);
        if (oo == null) {
            sLastError = "Invalid handle in closeObject call";
            return -3;
        }
        openObjects.remove(sHandle);
        return 0;
    }

    // Find a file/object name based on a provided file name.
    public String findAName(String sFileName) {
        String sReposit = getHostRepository();
        if (sReposit == null) {
            sLastError = "Could not find the host repository!";
            return null;
        }
        if (!sReposit.endsWith(File.separator)) {
            sReposit += File.separator;
        }

        String sObjName = null;
        String sOrigName = null;
        String sObjType = null;// extension
        String sPath = null;

        int ix = sFileName.lastIndexOf('.');
        if (ix < 0) {
            sOrigName = sFileName;
            sObjType = "";
        } else {
            sOrigName = sFileName.substring(0, ix);
            sObjType = sFileName.substring(ix + 1);
        }
        // Try to create a new file with the path <reposit>\<orig name>.<ext>,
        // <reposit>\<orig name>1.<ext>, <reposit>\<orig name>2.<ext>, etc.
        sObjName = sOrigName;
        if (sObjType == null || sObjType.length() == 0) {
            sPath = sReposit + sObjName;
        } else {
            sPath = sReposit + sObjName + "." + sObjType;
        }
        File f = new File(sPath);
        int n = 1;
        while (f.exists()) {
            sObjName = sOrigName + n;
            if (sObjType == null || sObjType.length() == 0) {
                sPath = sReposit + sObjName;
            } else {
                sPath = sReposit + sObjName + "." + sObjType;
            }
            n++;
            f = new File(sPath);
        }
        if (sObjType == null || sObjType.length() == 0) {
            return sObjName;
        } else {
            return sObjName + "." + sObjType;
        }
    }


    public int writeObject3(String sHandle, byte[] buf){
	    sLastError = null;
	    OpenObject oo = (OpenObject) openObjects.get(sHandle);
	    if (oo == null) {
	        sLastError = "Invalid handle in writeObject3()!";
	        return -1;
	    }
	    try {
	        Packet warningCmd = makeCmd(WRITE_OBJECT_3, sSessionId, sProcessId, sHandle);
	        Packet res = simClient.warnAndSend(warningCmd, buf);
	        if (res.hasError()) {
	            sLastError = res.getErrorMessage();
	            return -1;
	        }
	        return 0;
	    } catch (Exception e) {
	        e.printStackTrace();
	        sLastError = e.getMessage();
	        return -1;
	    }
    }

    /**
     * Serban, setPermissions is needed when creating a workflow's object attribute container.
     * @param oattrId the id of the object attr to set permissions on.
     * @param permissions an array of permissions to set (e.g. "File read", "File write")
     */
    @Override
    public boolean setPermissions(String oattrId, String... permissions) {
        throw new UnsupportedOperationException("setPermissions is unimplemented.");
    }

    // Make a copy of the object (same attributes, same content, maybe
    // another supporting host and path). Return the name of the copy
    // or null if the command failed.
    public String copyObject(String sObjName) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(COPY_OBJECT, sSessionId, sProcessId, sObjName);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in copyObject: " + res.getErrorMessage();
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in copyObject: " + e.getMessage();
            return null;
        }
    }

    public String getHostRepository() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(GET_HOST_REPOSITORY, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in getHostRepository: "
                        + res.getErrorMessage();
                return null;
            }
            return res.getStringValue(0);
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in getHostRepository: " + e.getMessage();
            return null;
        }
    }

    public boolean setRecordKeys(String sRecName, HashMap keyMap) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(SET_RECORD_KEYS, sSessionId, sRecName);
            if (keyMap != null) {
                Iterator mapiter = keyMap.keySet().iterator();
                while (mapiter.hasNext()) {
                    String sKeyName = (String) mapiter.next();
                    String sKeyValue = (String) keyMap.get(sKeyName);
                    cmd.addItem(ItemType.CMD_ARG, sKeyName + "=" + sKeyValue);
                }
            }
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in setRecordKeys: " + res.getErrorMessage();
                return false;
            }
            if (res.size() <= 0) {
                sLastError = "Empty response from setRecordKeys!";
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in setRecordKeys: " + e.getMessage();
            return false;
        }
    }

    @Override
    public SSLSocketClient getSocketClient() {
        return simClient;
    }

    public boolean addRecordKeys(String sRecName, HashMap keyMap) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(ADD_RECORD_KEYS, sSessionId, sRecName);
            if (keyMap != null) {
                Iterator mapiter = keyMap.keySet().iterator();
                while (mapiter.hasNext()) {
                    String sKeyName = (String) mapiter.next();
                    String sKeyValue = (String) keyMap.get(sKeyName);
                    cmd.addItem(ItemType.CMD_ARG, sKeyName + "=" + sKeyValue);
                }
            }
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in addRecordKeys: " + res.getErrorMessage();
                return false;
            }
            if (res.size() <= 0) {
                sLastError = "Empty response from addRecordKeys!";
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in addRecordKeys: " + e.getMessage();
            return false;
        }
    }

    // Create the container/oattr for a DB record.
    // Params:
    // sRecName: name of the record container.
    // sBase: name or id of the base node for this record container.
    // sBaseType: type of the base node for this record container.
    // sTemplateId: the id of the template of this record container.
    // sCompos: the ids of the fields of this record separated by ":".
    // Note that these are the ids of the associated oattrs,
    // not of the objects.
    // keyMap: a hashtable with the keys (name=value) for this record.
    // The keys start at item 14 in the command built in this method.
    public Packet createRecord(String sRecName, String sBase, String sBaseType,
            String sTemplateId, String sCompos, HashMap keyMap) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(CREATE_RECORD, sSessionId, sProcessId,
                    sRecName, sBase, sBaseType, sTemplateId, sCompos);
            if (keyMap != null) {
                Iterator mapiter = keyMap.keySet().iterator();
                while (mapiter.hasNext()) {
                    String sKeyName = (String) mapiter.next();
                    String sKeyValue = (String) keyMap.get(sKeyName);
                    cmd.addItem(ItemType.CMD_ARG, sKeyName + "=" + sKeyValue);
                }
            }
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in createRecord: " + res.getErrorMessage();
                return res;
            }
            if (res.size() <= 0) {
                sLastError = "Empty response from createRecord!";
                return failurePacket(sLastError);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in createRecord: " + e.getMessage();
            return failurePacket(sLastError);
        }
    }

    // Same as above, but the base is identified by property and type.
    public Packet createRecordInEntityWithProp(String sRecName,
            String sBaseProp, String sBaseType, String sTemplateId,
            String sCompos, HashMap keyMap) {
        sLastError = null;
        try {
            Packet cmd = makeCmd(CREATE_RECORD_IN_ENTITY_WITH_PROP, sSessionId,
                    sProcessId, sRecName, sBaseProp, sBaseType, sTemplateId,
                    sCompos);

            if (keyMap != null) {
                Iterator mapiter = keyMap.keySet().iterator();
                while (mapiter.hasNext()) {
                    String sKeyName = (String) mapiter.next();
                    String sKeyValue = (String) keyMap.get(sKeyName);
                    cmd.addItem(ItemType.CMD_ARG, sKeyName + "=" + sKeyValue);
                }
            }
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in createRecordInEntityWithProp: "
                        + res.getErrorMessage();
                return res;
            }
            if (res.size() <= 0) {
                sLastError = "Empty response from createRecordInEntityWithProp!";
                return failurePacket(sLastError);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in createRecordInEntityWithProp: "
                    + e.getMessage();
            return failurePacket(sLastError);
        }
    }

   @Override
   public Map<String, String> getPropertiesFor(String objId){
       Map<String, String> map = new HashMap<String, String>();
       try{
           Packet cmd = makeCmd(GET_OBJ_PROPERTIES, sSessionId, objId);
           if(!cmd.hasError() && cmd.getItems().size() > 0){
               for(String property : cmd.getStringValue(0).split(PM_FIELD_DELIM)){
                   String[] keyValuePair = property.split(PM_PROPERTY_DELIM, 2);
                   if(isNullOrEmpty(keyValuePair) && keyValuePair.length > 2){
                       map.put(keyValuePair[0], keyValuePair[1]);
                   }
               }
           }
       }catch(Exception e){
           e.printStackTrace();
           sLastError = "Exception in getPropertiesForEntity: " +
                   e.getMessage();
           throw new RuntimeException(e);
       }
       return map;
   }


    /**
     * Sets a property value for the PM entity represented by the id parameter
     * Creates a property of the form "key=value", separated into two params to simplify
     * the implementation of the API.
     * @param id
     * @param key
     * @param value
     * @return
     */
    private static Set<PmNodeType> validEntityTypes = EnumSet.of(PmNodeType.POLICY, PmNodeType.USER_ATTRIBUTE, PmNodeType.OBJECT_ATTRIBUTE);
    @Override
    public boolean setPropertyFor(String id, String key, String value) {


        String entityTypeCode = getEntityType(id);
        PmNodeType entityType  = PmNodeType.typeForCode(entityTypeCode);
        //Remapping entity type from file to object
        //in order to add property.
        if(!validEntityTypes.contains(entityType)){
            System.out.println("warning, attempting to set property for entity of invalid type " + entityType);
        }
        String isVos = "yes";
        String propPair = String.format("%s=%s", key, value);
        System.out.printf("SysCaller: setPropertyFor(id: %s,key: %s,value: %s) with type %s\n", id, key, value, entityTypeCode);
        try {
            Packet command = makeCmd(PMCommand.ADD_PROP, getSessionId(), id, entityTypeCode, isVos, propPair);
            Packet response  = simClient.sendReceive(command, null);
            return !response.hasError();
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return false;
    }

    // Ask the kernel to create the specified object and return a handle to it,
    // so we can write the contents.
    public String createObject3(String sObjName, String sObjClass,
            String sObjType, String sContainers, String sPerms, String sSender,
            String sRecip, String sSubject, String sAttached) {
        System.out.println("Syscaller: createObject3(" + sObjName + ", "
                + sObjClass + ", " + sObjType + ", \"" + sContainers + "\")");

        sLastError = null;
        try {
            Packet cmd = makeCmd(CREATE_OBJECT_3, sSessionId, sProcessId,
                    sObjName, sObjClass, sObjType, sContainers, sPerms,
                    sSender, sRecip, sSubject, sAttached);

            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = res.getErrorMessage();
                return null;
            }
            if (res.size() <= 0) {
                sLastError = "Empty response from createObject3!";
                return null;
            }
            String sHandle = res.getStringValue(0);
            OpenObject oo = new OpenObject(sObjName);
            openObjects.put(sHandle, oo);
            return sHandle;

        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in createObject3: " + e.getMessage();
            return null;
        }
    }

    // Returns true if and only if the paste action is allowed.
    // The text to paste is in the system clipboard, ignore the
    // answer from the kernel.
    public boolean isPastingAllowed() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(IS_PASTING_ALLOWED, sSessionId, sProcessId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in isPastingAllowed: "
                        + res.getErrorMessage();
                return false;
            }
            if (res.size() <= 0) {
                sLastError = "Empty response from isPastingAllowed!";
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in isPastingAllowed: " + e.getMessage();
            return false;
        }
    }

    // Copy some part of an object? to the clipboard.
    // If the handle is null, the copying is done from something which
    // is not yet an object, so the clipboard object should be created
    // without attributes. Otherwise, the handle identifies an opened
    // object and the engine will create the clipboard object with the
    // attributes of that object.
    public boolean copyToClipboard(String sHandle, String sSelText) {
        sLastError = null;
        if (sHandle != null) {
            OpenObject oo = (OpenObject) openObjects.get(sHandle);
            if (oo == null) {
                sLastError = "Invalid handle in copyToClipboard call";
                return false;
            }
            String sObjName = oo.getName();
            System.out.println("<<<Copy from object " + sObjName
                    + " to clipboard: " + sSelText);
        } else {
            System.out.println("<<<Copy from \"not an object yet\" to clipboard: "
                    + sSelText);
        }

        try {
            Packet cmd = makeCmd(COPY_TO_CLIPBOARD, sSessionId,
                    (sHandle == null) ? "" : sHandle, (sSelText == null) ? ""
                    : sSelText);
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in copyToClipboard: "
                        + res.getErrorMessage();
                return false;
            }
            if (res.size() <= 0) {
                sLastError = "Empty response from copyToClipboard!";
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in copyToClipboard: " + e.getMessage();
            return false;
        }
    }


    // The ArrayList script contains only the script's text lines.
    // Build a command out of it.
    public boolean addScript(List<String> script) {
        // The script should start at item 3 (0 = code, 1 = sessid,
        // 2 = filler, 3 = first line).
        sLastError = null;
        try {
            Packet cmd = makeCmd(COMPILE_SCRIPT_AND_ADD_TO_ENABLED, sSessionId,
                    "filler");
            for (int i = 0; i < script.size(); i++) {
                cmd.addItem(ItemType.CMD_ARG,  script.get(i));
            }
            Packet res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                sLastError = "Error in compileScriptAndAddToEnabled: "
                        + res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            sLastError = "Exception in compileScriptAndAddToEnabled: "
                    + e.getMessage();
            return false;
        }
    }






    public static int getIndex(JComboBox combo, String target) {
        int high = combo.getItemCount(), low = -1, probe;
        while (high - low > 1) {
            probe = (high + low) / 2;
            if (target.compareToIgnoreCase((String) combo.getItemAt(probe)) < 0) {
                high = probe;
            } else {
                low = probe;
            }
        }
        return (low + 1);
    }




    public static HashSet stringToSet(String sArg) {
        HashSet set = new HashSet();
        if (sArg != null) {
            String[] pieces = sArg.split(",");
            for (int i = 0; i < pieces.length; i++) {
                String t = pieces[i].trim();
                if (t.length() > 0) {
                    set.add(t);
                }
            }
        }
        return set;
    }
   

    public boolean createLinkedObjects() {
        sLastError = null;
        try {
            Packet cmd = makeCmd(CREATE_LINKED_OBJECTS, sSessionId);
            Packet res = simClient.sendReceive(cmd, null);
            if (res == null) {
                sLastError = "Null result from createLinkedObjects()!";
                return false;
            }
            if (res.hasError()) {
                sLastError = res.getErrorMessage();
                return false;
            }
            return true;
        } catch (Exception e) {
            sLastError = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    // ////////////////OpenObject Class///////////////////////////////
    /**
	 * @author  Administrator
	 */
    class OpenObject {

        HashSet setPerms; // The permissions returned by the engine.
        // String sId; // The virtual object id.
        String sName; // The virtual object name.
        /**
		 * @uml.property  name="pContent"
		 * @uml.associationEnd  
		 */
        Packet pContent; // The content of the object, filled in at first read.
        int iItemPos; // Next item to be read from alContent.
        int iBytePos; // Next byte to be read from the item.

        public OpenObject(String sName) {
            this.sName = sName;
            pContent = null;
            iItemPos = 0;
            iBytePos = 0;
            setPerms = null;
        }

        // public String getId() {
        // return sId;
        // }
        public String getName() {
            return sName;
        }

        public HashSet getPerms() {
            return setPerms;
        }

        public Packet getContent() {
            return pContent;
        }

        public int getBytePos() {
            return iBytePos;
        }

        public int getItemPos() {
            return iItemPos;
        }

        // public void setId(String sId) {
        // this.sId = sId;
        // }
        public void setName(String sName) {
            this.sName = sName;
        }

        public void setPerms(String sPerms) {
            setPerms = stringToSet(sPerms);
        }

        public void setContent(Packet p) {
            pContent = p;
        }

        public void setBytePos(int pos) {
            iBytePos = pos;
        }

        public void setItemPos(int pos) {
            iItemPos = pos;
        }
    }
}

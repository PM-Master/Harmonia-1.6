package gov.nist.csd.pm.common.application;

import gov.nist.csd.pm.common.model.ObjectAttribute;
import gov.nist.csd.pm.common.net.Packet;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static gov.nist.csd.pm.common.info.PMCommand.DEASSIGN_OBJ_FROM_OATTR_WITH_PROP;
import static gov.nist.csd.pm.common.net.Packet.nullPacket;
import static gov.nist.csd.pm.common.util.CommandUtil.makeCmd;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/20/11
 * Time: 2:52 PM
 * No-op implementation of SysCaller
 */
public class NullSysCaller implements SysCaller {


    @Override
    public SSLSocketClient getSocketClient() {
        return null;
    }

    @Override
    public boolean addRecordKeys(String sRecName, HashMap keyMap) {
        return false;
    }

    @Override
    public boolean addScript(List<String> script) {
        return false;
    }

    @Override
    public boolean addTemplate(String sName, List<String> containers, List<String> keys) {
        return false;
    }

    @Override
    public boolean assignObjToContainer(String sObjName, String sContainer) {
        return false;
    }

    @Override
    public boolean assignObjToInboxOf(String sObjName, String sUserName) {
        return false;
    }

    @Override
    public boolean assignObjToOutboxOf(String sObjName, String sUserName) {
        return false;
    }

    @Override
    public boolean assignObjToWinboxOf(String sObjName, String sUserName) {
        return false;
    }

    @Override
    public Packet bye() {
        return nullPacket();
    }

    @Override
    public int closeObject(String sHandle) {
        return 0;
    }

    @Override
    public String copyObject(String sObjName) {
        return "";
    }

    @Override
    public boolean copyToClipboard(String sHandle, String sSelText) {
        return false;
    }

    @Override
    public boolean createLinkedObjects() {
        return false;
    }

    @Override
    public String createObject3(String sObjName, String sObjClass, String sObjType, String sContainers, String sPerms, String sSender, String sRecip, String sSubject, String sAttached) {
        return "";
    }

    @Override
    public String createProcess() {
        return "";
    }

    @Override
    public String addContainer(ObjectAttribute containerSpec, ObjectAttribute parentAttribute) {
        return "";
    }

    @Override
    public Packet createRecord(String sRecName, String sBase, String sBaseType, String sTemplateId, String sCompos, HashMap keyMap) {
        return nullPacket();
    }

    @Override
    public Packet createRecordInEntityWithProp(String sRecName, String sBaseProp, String sBaseType, String sTemplateId, String sCompos, HashMap keyMap) {
        return nullPacket();
    }

    @Override
    public Map<String, String> getPropertiesFor(String obj) {
        return new HashMap<String, String>();
    }

    @Override
    public boolean setPropertyFor(String objId, String key, String value) {
        return false;
    }

    @Override
    public boolean deassignObjFromHomeOf(String sObjName, String sProp) {
        return false;
    }
    
    @Override
    public boolean deassignObjFromOattr3(String sObjName, String sUserName) {
        return false;
    }

    @Override
    public boolean deassignObjFromInboxOf(String sObjName, String sUserName) {
        return false;
    }

    @Override
    public boolean deassignObjFromOutboxOf(String sObjName, String sUserName) {
        return false;
    }

    @Override
    public Packet deleteOpsetsBetween(String sUattrName, String sOattrName) {
        return nullPacket();
    }

    /**
     * This should not be called directly by an application rather you should use the  in the application manager for closing the application.
     *
     * @param sPid the process id to be terminated
     * @return if the process was successfully terminated
     */
    @Override
    public boolean exitProcess(String sPid) {
        return false;
    }

    @Override
    public String findAName(String sFileName) {
        return "";
    }

    /**
     * TODO:This method has a duplicate in OsTaskBar
     *
     * @param appName The name of the application whose path you need
     * @return At index zero the applications path, at index 1 the applications
     *         prefix, at index 2 the applications main class
     */
    @Override
    public String[] getAppPath(String appName) {
        return new String[0];
    }

    @Override
    public String[] getConnector() {
        return new String[0];
    }

    @Override
    public String getNameOfContainerWithProperty(String sProp) {
        return "";
    }

    @Override
    public Packet getEmailAcct(String sUser) {
        return nullPacket();
    }

    @Override
    public HashSet getEmailRecipients(String sReceiver) {
        return new HashSet();
    }

    @Override
    public String getIdOfEntityWithNameAndType(String sName, String sType) {
        return "";
    }

    @Override
    public String getNameOfEntityWithIdAndType(String sId, String sType) {
        return "";
    }



    @Override
    public String[] getInfoOfEntityWithPropertyAndType(String sEntProp, String sEntType) {
        return new String[0];
    }

    @Override
    public String getEntityType(String eId) {
        return "";
    }

    @Override
    public String getIdOfEntityWithPropertyAndType(String sEntProp, String sEntType) {
        return "";
    }
    @Override
    public String getNameOfEntityWithPropertyAndType(String sEntProp, String sEntType) {
        return "";
    }

    @Override
    public byte[] getFileContent(String sFileProp) {
        return new byte[0];
    }

    @Override
    public Packet getHostAppPaths() {
        return nullPacket();
    }

    @Override
    public String getHostRepository() {
        return "";
    }

    @Override
    public Packet getInboxMessages() {
        return nullPacket();
    }

    @Override
    public Packet getKStorePaths() {
        return nullPacket();
    }

    @Override
    public String getLastError() {
        return "";
    }

    @Override
    public List<String[]> getMembersOf(String sName, String sId, String sType, String sGraphType) {
        return newArrayList();
    }
    
    @Override
    public List<String[]> getMellMembersOf(String sName, String sId, String sType, String sGraphType) {
        return newArrayList();
    }

    @Override
    public Packet getObjInfo(String sObjId) {
        return nullPacket();
    }

    @Override
    public String getObjectPath(String sObjName) {
        return "";
    }

    @Override
    public Packet getOutboxMessages() {
        return nullPacket();
    }

//    @Override
//    public List<String[]> getPosMembersOf(String sLabel, String sId, String sType, String sPresType) {
//        return newArrayList();
//    }

    @Override
    public Packet getProperContainers() {
        return nullPacket();
    }

    @Override
    public Packet getRecordInfo(String sContId) {
        return nullPacket();
    }

    @Override
    public Packet getRecords(String sTplId, String sKey) {
        return nullPacket();
    }

    @Override
    public String getSessionId() {
        return "";
    }

    @Override
    public String getSessionUser() {
        return "";
    }

    @Override
    public Packet getTemplateInfo(String sTplId) {
        return nullPacket();
    }

    @Override
    public Packet getTemplates() {
        return nullPacket();
    }

    @Override
    public Packet getUsersOf(String sUattr) {
        return nullPacket();
    }

    @Override
    public boolean isObjInInboxOf(String sObjName, String sUserName) {
        return false;
    }

    @Override
    public boolean isObjInOutboxOf(String sObjName, String sUserName) {
        return false;
    }

    @Override
    public boolean isPastingAllowed() {
        return false;
    }


    @Override
    public String openObject3(String sObjName, String sPerms) {
        return "";
    }

    @Override
    public PMIOObject openObject4(String sObjName, String sPerms) {
        return new PMIOObjectImpl(this, sObjName, sPerms);
    }

    @Override
    public byte[] readObject3(String sHandle) {
        return new byte[0];
    }

    @Override
    public Packet readObject3(String sHandle, OutputStream out) {
        return nullPacket();
    }

    @Override
    public Packet setPerms(String sUattr, String sOpset, String sOattr, String sBase, String sBaseType, String sPerms, String sEntityName, String sEntityType, String sYesNo) {
        return nullPacket();
    }

    @Override
    public boolean setRecordKeys(String sRecName, HashMap keyMap) {
        return false;
    }

    @Override
    public int writeObject3(String sHandle, byte[] buf) {
        return 0;
    }


    @Override
    public boolean setPermissions(String oattrId, String... permissions) {
        return false;
    }

	@Override
	public boolean addPropToOattr(String id, String isVos, String key, String value){
		return false;
	}
	
	public boolean deleteObject(String sObjId, String type){
		return false;
	}

}

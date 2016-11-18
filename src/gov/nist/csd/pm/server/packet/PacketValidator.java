package gov.nist.csd.pm.server.packet;

import gov.nist.csd.pm.common.net.Packet;

import java.util.HashMap;

public class PacketValidator {
	
	protected static HashMap<String, Integer> commandsSize = new HashMap<String, Integer>();
	static {
		commandsSize.put("connect", new Integer(1));
		commandsSize.put("reset", new Integer(2));
		commandsSize.put("isRecordPacket", new Integer(2));
		commandsSize.put("getDescOattrsInPcPacket", new Integer(3));
		commandsSize.put("getDescendantOattrs", new Integer(3));
		commandsSize.put("createSchemaPC", new Integer(5));
		commandsSize.put("addSchemaOattr", new Integer(6));
		commandsSize.put("setTableModifying", new Integer(2));
		commandsSize.put("getParentNodes_SteveQ", new Integer(6));
		commandsSize.put("getFromAttrs1", new Integer(3));
		commandsSize.put("getFromUserAttrsPacket", new Integer(3));
		commandsSize.put("getContainers", new Integer(4));
		commandsSize.put("getToAttrsUser", new Integer(3));
		commandsSize.put("getPermittedOpsOnEntity", new Integer(5));
		commandsSize.put("getAssocOattr1", new Integer(3));
		commandsSize.put("getReps", new Integer(4));
		commandsSize.put("getFromOpsets1", new Integer(3));
		commandsSize.put("getPermittedOps1", new Integer(5));
		commandsSize.put("getArrayPerms", new Integer(3));
		commandsSize.put("getToAttrs1", new Integer(3));
		commandsSize.put("setSchemaPerms", new Integer(7));
		commandsSize.put("setTablePerms", new Integer(9));
		commandsSize.put("deleteTemplate", new Integer(4));
		commandsSize.put("testSynchro", new Integer(1));
		commandsSize.put("testDoubleLink", new Integer(1));
		commandsSize.put("getGraph", new Integer(7));
		commandsSize.put("getPolicyClasses", new Integer(1));
		commandsSize.put("addPc", new Integer(6));
		commandsSize.put("getPcInfo", new Integer(4));
		commandsSize.put("addUattr", new Integer(10));
		commandsSize.put("addOattr", new Integer(11));
		commandsSize.put("assign", new Integer(7));
		commandsSize.put("deleteAssignment", new Integer(8));
		commandsSize.put("getUserAttributes", new Integer(1));
		commandsSize.put("getUsers", new Integer(1));
		commandsSize.put("getAsets", new Integer(1));
		commandsSize.put("addAsetAndAttr", new Integer(5));
		commandsSize.put("deleteAsetAndAttr", new Integer(5));
		commandsSize.put("getAsetInfo", new Integer(3));
		commandsSize.put("addSacAndAset", new Integer(6));
		commandsSize.put("deleteSacAndAset", new Integer(5));
		commandsSize.put("getSacInfo", new Integer(3));
		commandsSize.put("addUser", new Integer(10));
		commandsSize.put("deleteNode", new Integer(5));
		commandsSize.put("addObjClassAndOp", new Integer(4));
		commandsSize.put("deleteObjClassAndOp", new Integer(4));
		commandsSize.put("getObjClasses", new Integer(1));
		commandsSize.put("getObjects", new Integer(1));
		commandsSize.put("getPmEntitiesOfClass", new Integer(3));
		commandsSize.put("addObject3", new Integer(18));
		commandsSize.put("deleteObject", new Integer(3));
		commandsSize.put("deleteObjectStrong", new Integer(3));
		commandsSize.put("deleteContainerObjects", new Integer(4));
		commandsSize.put("getAssocObj", new Integer(3));
		commandsSize.put("getObjNamePath", new Integer(3));
		commandsSize.put("getObjClassOps", new Integer(3));
		commandsSize.put("getHosts", new Integer(1));
		commandsSize.put("addHost", new Integer(4));
		commandsSize.put("updateHost", new Integer(5));
		commandsSize.put("deleteHost", new Integer(3));
		commandsSize.put("getHostInfo", new Integer(3));
		commandsSize.put("getOpsets", new Integer(1));
		commandsSize.put("addOpsetAndOp", new Integer(8));
		commandsSize.put("getOpsetOps", new Integer(3));
		commandsSize.put("getOpsetClassName", new Integer(3));
		commandsSize.put("getOpsetInfo", new Integer(3));
		commandsSize.put("deleteOpsetAndOp", new Integer(4));
		commandsSize.put("createSession", new Integer(6));
		commandsSize.put("spawnSession", new Integer(2));
		commandsSize.put("changePassword", new Integer(6));
		commandsSize.put("deleteSession", new Integer(3));
		commandsSize.put("refMediation", new Integer(3));
		commandsSize.put("getPermittedOps", new Integer(5));
		commandsSize.put("getObjEmailProps", new Integer(3));
		commandsSize.put("getVosIdProperties", new Integer(4));
		commandsSize.put("getPosNodeProperties", new Integer(6));
		commandsSize.put("computeVos", new Integer(5));
		commandsSize.put("computeFastVos", new Integer(5));
		commandsSize.put("getVosGraph", new Integer(10));
		commandsSize.put("getSimpleVosGraph", new Integer(7));
		commandsSize.put("getSelVosGraph2", new Integer(13));
		commandsSize.put("getSelVosGraph", new Integer(13));
		commandsSize.put("activateAttributes", new Integer(4));
		commandsSize.put("getActiveAttributes", new Integer(3));
		commandsSize.put("getSessions", new Integer(1));
		commandsSize.put("getSessionEvents", new Integer(1));
		commandsSize.put("getSessionInfo", new Integer(3));
		commandsSize.put("getSessionUser", new Integer(3));
		commandsSize.put("getSessionName", new Integer(3));
		commandsSize.put("updateProtObjs", new Integer(4));
		commandsSize.put("sessionHasCap", new Integer(5));
		commandsSize.put("testIsContained", new Integer(6));
		commandsSize.put("requestPermsOnPmEntity", new Integer(6));
		commandsSize.put("requestPerms", new Integer(4));
		commandsSize.put("wouldOpenPreventSave", new Integer(4));
		commandsSize.put("getPerms", new Integer(4));
		commandsSize.put("getUserDescendants", new Integer(3));
		commandsSize.put("getMaximalSubsets", new Integer(3));
		commandsSize.put("export", new Integer(2));
		commandsSize.put("interpretCmd", new Integer(3));
		commandsSize.put("importConfiguration", new Integer(2));
		commandsSize.put("getHostAppPaths", new Integer(4));
		commandsSize.put("getInstalledAppNames", new Integer(3));
		commandsSize.put("getHostRepository", new Integer(2));
		commandsSize.put("addHostApp", new Integer(7));
		commandsSize.put("setHostAppPaths", new Integer(14));
		commandsSize.put("getKStorePaths", new Integer(2));
		commandsSize.put("setKStorePaths", new Integer(6));
		commandsSize.put("isTimeToRefresh", new Integer(3));
		commandsSize.put("getDenies", new Integer(2));
		commandsSize.put("getDenyInfo", new Integer(3));
		commandsSize.put("getAllOps", new Integer(2));
		commandsSize.put("getOattrs", new Integer(2));
		commandsSize.put("getOconts", new Integer(2));
		commandsSize.put("getObjAttrsProper", new Integer(2));
		commandsSize.put("addDeny", new Integer(10));
		commandsSize.put("deleteDeny", new Integer(6));
		commandsSize.put("getIdOfEntityWithNameAndType", new Integer(4));
		commandsSize.put("getNameOfEntityWithIdAndType", new Integer(4));
		commandsSize.put("getScripts", new Integer(1));
		commandsSize.put("enableScript", new Integer(3));
		commandsSize.put("disableEnabledScript", new Integer(2));
		commandsSize.put("getEnabledScript", new Integer(1));
		commandsSize.put("compileScript", new Integer(3));
		commandsSize.put("compileScriptAndEnable", new Integer(2));
		commandsSize.put("compileScriptAndAddToEnabled", new Integer(2));
		commandsSize.put("addScript", new Integer(4));
		commandsSize.put("addScriptToEnabled", new Integer(3));
		commandsSize.put("deleteScriptsWithNameExcept", new Integer(4));
		commandsSize.put("deleteScript", new Integer(3));
		commandsSize.put("getSourceScript", new Integer(3));
		commandsSize.put("deleteScriptRule", new Integer(4));
		commandsSize.put("getAttrInfo", new Integer(5));
		commandsSize.put("addProp", new Integer(6));
		commandsSize.put("replaceProp", new Integer(7));
		commandsSize.put("removeProp", new Integer(6));
		commandsSize.put("createObject3", new Integer(12));
		commandsSize.put("processEvent", new Integer(7));
		commandsSize.put("getUserInfo", new Integer(3));
		commandsSize.put("getOpsetsBetween", new Integer(5));
		commandsSize.put("deleteOpsetsBetween", new Integer(5));
		commandsSize.put("getOpsetOattrs", new Integer(3));
		commandsSize.put("buildClipboard", new Integer(3));
		commandsSize.put("isolateOattr", new Integer(4));
		commandsSize.put("setPerms", new Integer(12));
		commandsSize.put("testGUWC", new Integer(5));
		commandsSize.put("addTask", new Integer(6));
		commandsSize.put("getTasks", new Integer(2));
		commandsSize.put("getTaskInfo", new Integer(3));
		commandsSize.put("deleteTask", new Integer(4));
		commandsSize.put("addScon", new Integer(5));
		commandsSize.put("getScons", new Integer(2));
		commandsSize.put("getSconInfo", new Integer(3));
		commandsSize.put("deleteScon", new Integer(4));
		commandsSize.put("checkScon", new Integer(3));
		commandsSize.put("addScona", new Integer(7));
		commandsSize.put("getSconas", new Integer(2));
		commandsSize.put("getSconaInfo", new Integer(3));
		commandsSize.put("deleteScona", new Integer(4));
		commandsSize.put("checkScona", new Integer(3));
		commandsSize.put("getEntityWithProp", new Integer(4));
		commandsSize.put("doDacConfinement", new Integer(6));
		commandsSize.put("testGetMemberObjects", new Integer(4));
		commandsSize.put("testGetPmViews", new Integer(4));
		commandsSize.put("isAssigned", new Integer(6));
		commandsSize.put("getIdOfEntityWithNameAndType", new Integer(4));
		commandsSize.put("getNameOfEntityWithIdAndType", new Integer(4));
		commandsSize.put("testGetDeniedPerms", new Integer(4));
		commandsSize.put("genDac", new Integer(4));
		commandsSize.put("genMls", new Integer(4));
		commandsSize.put("genConfForDacUser", new Integer(6));
		commandsSize.put("genDacUserWithConf", new Integer(7));
		commandsSize.put("genIbac", new Integer(3));
		commandsSize.put("addEmailAcct", new Integer(9));
		commandsSize.put("getEmailAcct", new Integer(3));
		commandsSize.put("sendSimpleMsg", new Integer(4));
		commandsSize.put("copyObject", new Integer(4));
		commandsSize.put("sendObject", new Integer(4));
		commandsSize.put("getEmailRecipients", new Integer(3));
		commandsSize.put("getUsersAndAttrs", new Integer(2));
		commandsSize.put("addOpenObj", new Integer(3));
		commandsSize.put("deleteOpenObj", new Integer(3));
		commandsSize.put("assignObjToOattrWithProp", new Integer(5));
		commandsSize.put("assignObjToOattr", new Integer(5));
		commandsSize.put("deassignObjFromOattrWithProp", new Integer(5));
		commandsSize.put("isObjInOattrWithProp", new Integer(4));
		commandsSize.put("getInboxMessages", new Integer(2));
		commandsSize.put("getOutboxMessages", new Integer(2));
		commandsSize.put("getFileContent", new Integer(3));
		commandsSize.put("createObjForFile", new Integer(4));
		commandsSize.put("createContForFolder", new Integer(4));
		commandsSize.put("getDascUattrs", new Integer(4));
		commandsSize.put("getDascUsers", new Integer(4));
		commandsSize.put("getDascOattrs", new Integer(4));
		commandsSize.put("getDascObjects", new Integer(4));
		commandsSize.put("getDascOpsets", new Integer(4));
		commandsSize.put("decreaseAscs", new Integer(2));
		commandsSize.put("increaseAscs", new Integer(2));
		
		commandsSize.put("showAccessibleObjects", new Integer(5));
		commandsSize.put("findBorderOaPrivRelaxed", new Integer(5));
		commandsSize.put("findBorderOaPrivRestricted", new Integer(5));
		commandsSize.put("initialOas", new Integer(5));
		commandsSize.put("initialOasWithLabels", new Integer(5));
		commandsSize.put("subsequentOas", new Integer(8));
		commandsSize.put("successorOas", new Integer(8));
		commandsSize.put("calcPriv", new Integer(8));
		
		commandsSize.put("getContainersOf", new Integer(6));
		commandsSize.put("getMellContainersOf", new Integer(6));
		commandsSize.put("getMembersOf", new Integer(6));
		commandsSize.put("getMellMembersOf", new Integer(6));
		commandsSize.put("getConnector", new Integer(1));
		commandsSize.put("getPosMembersOf", new Integer(6));
		commandsSize.put("getPosContainersOf", new Integer(6));
		commandsSize.put("setStartups", new Integer(2));
		commandsSize.put("addTemplate", new Integer(5));
		commandsSize.put("getTemplates", new Integer(2));
		commandsSize.put("getTemplateInfo", new Integer(3));
		commandsSize.put("createRecord", new Integer(9));
		commandsSize.put("createRecordInEntityWithProp", new Integer(9));
		commandsSize.put("getRecords", new Integer(4));
		commandsSize.put("getRecordInfo", new Integer(3));
		commandsSize.put("getObjInfo", new Integer(3));
		commandsSize.put("getObjProperties", new Integer(3));
		commandsSize.put("isInPos", new Integer(3));
		commandsSize.put("testExportRecords", new Integer(2));
		commandsSize.put("getUsersOf", new Integer(3));
		commandsSize.put("setRecordKeys", new Integer(4));
		commandsSize.put("addRecordKeys", new Integer(4));
		commandsSize.put("getProperties", new Integer(2));
		commandsSize.put("deleteProperty", new Integer(3));
		commandsSize.put("setProperty", new Integer(4));
		commandsSize.put("getPropertyValue", new Integer(3));
		commandsSize.put("getProperty", new Integer(3));
		commandsSize.put("createLinkedObjects", new Integer(1));
		commandsSize.put("audit", new Integer(5));
	}
	
	
	public static boolean isValid (Packet packet){
		String commandName=null;
		if(packet.size()>0){
			commandName = packet.getStringValue(0);
			if(commandsSize.containsKey(commandName)){
				if(packet.size()>=commandsSize.get(commandName).intValue()){
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isSuccessPacket(Packet p) {
		return !(p == null || p.size() == 0) && !p.hasError();
	}
	
	public static boolean isFailurePacket(Packet p) {
		return !(p == null || p.size() == 0) && p.hasError();
	}


}

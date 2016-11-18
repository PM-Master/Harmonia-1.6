package gov.nist.csd.pm.server.packet;

import static gov.nist.csd.pm.common.info.PMCommand.ADD_HOST_APP;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;

public class SQLPacketHandler {

	Log log = new Log(Log.Level.INFO, true);

	/// Definitely not void, it should be the base 
	/// of the command and parameters, all of which 
	/// should be the vocabulary objects the 
	/// PM Engine talks and operates on
	public static void ParseCommandPacket(Packet cmdPacket){
		String sCmdCode = cmdPacket.getStringValue(0);
		/// TODO: make it conditional on debugging flag
		System.out.println(sCmdCode);
		System.out.println("PM Engine Dispatch " + sCmdCode);
	}

	// Pulls all from the Packet, but is here to avoid referring to Packet from PMEngine
	public static  Packet getSuccessPacket() {
		return Packet.getSuccessPacket();
	}
	// Pulls all from the Packet, but is here to avoid referring to Packet from PMEngine
	public static  Packet getSuccessPacket(String s) {
		return Packet.getSuccessPacket(s);
	}

	public static Packet getFailurePacket(){
		return Packet.failurePacket("");
	}

	public static Packet getFailurePacket(String message){
		return Packet.failurePacket(message);
	}

	/**
	 * Creates base entity from reading packet
	 * */
//	protected static BaseEntity createEntity(Packet cmdPacket){
//		String sCmdCode = cmdPacket.getStringValue(0);
//		return null;
//	}

//	public static Packet TranslateToPacket(ResBase response) {
//		Packet p = new Packet();
//		try {
//			List<NameValuePair> list = response.getResponse();
//			for (int i=0 ; i<= list.size(); i++) {
//				p.addItem(ItemType.RESPONSE_TEXT, list.get(i).getName() + GlobalConstants.PM_FIELD_DELIM + list.get(i).getValue());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//		return p;
//	}

	public static Packet setToPacket(HashSet<String> set) {
		if (set == null) {
			return null;
		}
		Packet p = new Packet();
		try {
			Iterator<String> iter = set.iterator();
			while (iter.hasNext()) {
				p.addItem(ItemType.RESPONSE_TEXT, iter.next());
			}
			return p;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	// Get the items string contents from a packet starting at given index as an
	// array of strings.
	private String[] getItemsFromPacket(Packet p, int index) {
		if (p == null || p.size() <= index) {
			return null;
		}
		int n = p.size() - index;
		String[] res = new String[n];
		for (int i = 0; i < n; i++) {
			res[i] = p.getStringValue(index + i);
		}
		return res;
	}

	public synchronized Packet executeCommand(String sClientId,
											  Packet cmdPacket, InputStream bisFromClient,
											  OutputStream bosToClient) {


		// Get the command code.

		String sCmdCode = cmdPacket.getStringValue(0);

		log.debug("TRACE 3 - In PacketHandler.executeCommand(): " + sCmdCode);

		/*if (!PacketValidator.isValid(cmdPacket)) {
			return failurePacket("Too few arguments!");
		}*/

		System.out.println(sCmdCode);
		System.out.println("PM Engine Dispatch " + sCmdCode);

		// Dispatch the command
		if(sCmdCode.equalsIgnoreCase("setTableModifying")){
			ServerConfig.SQLDAO.setTableModifying(cmdPacket.getStringValue(2));
		}else if (sCmdCode.equalsIgnoreCase("buildClipboard")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sOattrName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.buildClipboard(sSessId, sOattrName);
		} else if(sCmdCode.equalsIgnoreCase("updateGraph")){
			return ServerConfig.SQLDAO.updateAllNodes();
		} else if(sCmdCode.equalsIgnoreCase("getFromAttrs1")){
			String sSessId = cmdPacket.getStringValue(1);
			String sOpsetId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getFromAttrs1(sSessId, sOpsetId);
		} else if(sCmdCode.equalsIgnoreCase("getFromUserAttrsPacket")){
			String sBaseId = cmdPacket.getStringValue(2);
			String sBaseType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getFromUserAttrsPacket(sBaseId, sBaseType);
		} else if(sCmdCode.equalsIgnoreCase("isRecordPacket")){
			String sId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.isRecordPacket(sId);
		}else if(sCmdCode.equalsIgnoreCase("createSchemaPC")){
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
            String policyType = cmdPacket.getStringValue(3);
            String oattrType = cmdPacket.getStringValue(4);
			String sPolicyClass = cmdPacket.getStringValue(5);
			String sUattr = cmdPacket.getStringValue(6);
			return ServerConfig.SQLDAO.createSchemaPC(sSessId, sProcId, policyType,
					oattrType, sPolicyClass, sUattr);
		}else if(sCmdCode.equalsIgnoreCase("addSchemaOattr")){
			String sSessId = cmdPacket.getStringValue(1);
			String oattrType = cmdPacket.getStringValue(3);
			String name = cmdPacket.getStringValue(4);
			String baseName = cmdPacket.getStringValue(5);
			String baseType = cmdPacket.getStringValue(6);
			return ServerConfig.SQLDAO.addSchemaOattr(sSessId, oattrType, name, baseName, baseType);
		}else if(sCmdCode.equalsIgnoreCase("getContainers")){
			String id = cmdPacket.getStringValue(2);
			String type = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getContainers(id, type);
		}else if(sCmdCode.equalsIgnoreCase("getToAttrsUser")){
			String sId = cmdPacket.getStringValue(1);
			String sType = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getToAttrsUser(sId, sType);
		}else if(sCmdCode.equalsIgnoreCase("getPermittedOpsOnEntity")){
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sId = cmdPacket.getStringValue(3);
			String sType = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.getPermittedOpsOnEntity(sSessId, sProcId, sId, sType);
		}else if(sCmdCode.equalsIgnoreCase("getAssocOattr1")){
			String sId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getAssocOattr1(sId);
		}else if(sCmdCode.equalsIgnoreCase("getReps")){
			String sId = cmdPacket.getStringValue(2);
			String sType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getReps(sId, sType);
		}else if(sCmdCode.equalsIgnoreCase("getFromOpsets1")){
			String sSessId = cmdPacket.getStringValue(1);
			String sOpsetId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getFromOpsets1(sSessId, sOpsetId);
		}else if(sCmdCode.equalsIgnoreCase("getPermittedOps1")){
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sBaseId = cmdPacket.getStringValue(3);
			String sBaseType = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.getPermittedOps1(sSessId, sProcId, sBaseId, sBaseType);
		}else if(sCmdCode.equalsIgnoreCase("getArrayPerms")){
			String sSessId = cmdPacket.getStringValue(1);
			String sOpsetId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getFromAttrs1(sSessId, sOpsetId);
		}else if(sCmdCode.equalsIgnoreCase("getToAttrs1")){
			String sSessId = cmdPacket.getStringValue(1);
			String sOpsetId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getToAttrs1(sSessId, sOpsetId);
		}else if(sCmdCode.equalsIgnoreCase("setSchemaPerms")){
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sBaseName = cmdPacket.getStringValue(3);
			String sBaseType = cmdPacket.getStringValue(4);
			String sAttrName = cmdPacket.getStringValue(5);
			String uattr = cmdPacket.getStringValue(6);
			return ServerConfig.SQLDAO.setSchemaPerms(sSessId, sProcId, sBaseName, sBaseType, sAttrName, uattr);
		}else if(sCmdCode.equalsIgnoreCase("setTablePerms")){
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sBaseName = cmdPacket.getStringValue(3);
			String sBaseType = cmdPacket.getStringValue(4);
			String sAttrName = cmdPacket.getStringValue(5);
			String sAttrType = cmdPacket.getStringValue(6);
			String sPerms = cmdPacket.getStringValue(7);
			String uattr = cmdPacket.getStringValue(8);
			String inh = cmdPacket.getStringValue(9);
			return ServerConfig.SQLDAO.setTablePerms(sSessId, sProcId, sBaseName,
					sBaseType, sAttrName, sAttrType, sPerms, uattr, inh.equalsIgnoreCase("yes"));
		}else if(sCmdCode.equalsIgnoreCase("deleteTemplate")){
			String sSessId = cmdPacket.getStringValue(1);
			String sTplId = cmdPacket.getStringValue(2);
			String sTplName = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.deleteTemplate(sSessId, sTplId, sTplName);
		}else if (sCmdCode.equalsIgnoreCase("testSynchro")) {
			return ServerConfig.SQLDAO.testSynchro(sClientId);

		}
		// CONNECT comand
		if (sCmdCode.equalsIgnoreCase("connect")) {
			return getSuccessPacket();
		}
		// RESET command
		else if (sCmdCode.equalsIgnoreCase("reset")) {
			System.out.println("*********************************** $$$$$$$$$$$$$$$$$$$ Reset is called from Admin");
			String crtSessionId = cmdPacket.getStringValue(1);
			ServerConfig.SQLDAO.reset(crtSessionId);
			return getSuccessPacket();

		} else if (sCmdCode.equalsIgnoreCase("getPolicyClasses")) {
			return ServerConfig.SQLDAO.getPolicyClasses(sClientId);

		} else if (sCmdCode.equalsIgnoreCase("addPc")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sName = cmdPacket.getStringValue(3);
			String sDescr = cmdPacket.getStringValue(4);
			String sInfo = cmdPacket.getStringValue(5);
			// The properties start at item 5:
			String[] sProps = getItemsFromPacket(cmdPacket, 6);
			return ServerConfig.SQLDAO.addPc(sSessId, sProcId, sName, sDescr, sInfo, sProps);

		} else if (sCmdCode.equalsIgnoreCase("getPcInfo")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sPcId = cmdPacket.getStringValue(2);
			String sIsVos = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getPcInfo(sSessId, sPcId, sIsVos);

		} else if (sCmdCode.equalsIgnoreCase("addUattr")) {
			String crtSessionId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sName = cmdPacket.getStringValue(3);
			String sDescr = cmdPacket.getStringValue(4);
			String sInfo = cmdPacket.getStringValue(5);
			String sBaseId = cmdPacket.getStringValue(6);
			String sBaseType = cmdPacket.getStringValue(7);
			String sBaseIsVos = cmdPacket.getStringValue(8);
			// The properties start at item 9:
			String[] sProps = getItemsFromPacket(cmdPacket, 9);
			return ServerConfig.SQLDAO.addUattr(sClientId, crtSessionId, sProcId, sName,
					sDescr, sInfo, sBaseId, sBaseType, sBaseIsVos, sProps);

		} else if (sCmdCode.equalsIgnoreCase("addOattr")) {
			String crtSessionId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sName = cmdPacket.getStringValue(3);
			String sDescr = cmdPacket.getStringValue(4);
			String sInfo = cmdPacket.getStringValue(5);
			String sBaseId = cmdPacket.getStringValue(6);
			String sBaseType = cmdPacket.getStringValue(7);
			String sBaseIsVos = cmdPacket.getStringValue(8);
			String sAssocObjId = cmdPacket.getStringValue(9); // Could be
			// "".
			// When invoked from a client, there is no associated object id
			// (should be "").
			// The properties start at item 10.
			String[] sProps = getItemsFromPacket(cmdPacket, 10);
			if (sProps == null) {
				System.out.println("sProps is null");
			} else {
				for (int i = 0; i < sProps.length; i++) {
					System.out.println(i + " " + sProps[i]);
				}
			}
			return ServerConfig.SQLDAO.addOattr(crtSessionId, sProcId, sName, sDescr, sInfo,
					sBaseId, sBaseType, sBaseIsVos, sAssocObjId, sProps);

		} else if (sCmdCode.equalsIgnoreCase("assign")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sId1 = cmdPacket.getStringValue(3);
			String sType1 = cmdPacket.getStringValue(4);
			String sId2 = cmdPacket.getStringValue(5);
			String sType2 = cmdPacket.getStringValue(6);
			return ServerConfig.SQLDAO.assign(sSessId, sProcId, sId1, sType1, sId2, sType2);
			//,sIsAdminVos);

		} else if (sCmdCode.equalsIgnoreCase("deleteAssignment")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sId1 = cmdPacket.getStringValue(3);
			String sType1 = cmdPacket.getStringValue(4);
			String sId2 = cmdPacket.getStringValue(5);
			String sType2 = cmdPacket.getStringValue(6);
			String sIsAdminVos = cmdPacket.getStringValue(7);
			return ServerConfig.SQLDAO.deleteAssignment(sSessId, sProcId, sId1, sType1, sId2,
					sType2, sIsAdminVos);

		} else if (sCmdCode.equalsIgnoreCase("getUserAttributes")) {
			return ServerConfig.SQLDAO.getUserAttributes(sClientId);

		} else if (sCmdCode.equalsIgnoreCase("getUsers")) {
			return ServerConfig.SQLDAO.getUsers(sClientId);

		} else if (sCmdCode.equalsIgnoreCase("getAsets")) {
			return ServerConfig.SQLDAO.getAsets(sClientId);

		} else if (sCmdCode.equalsIgnoreCase("addUser")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sName = cmdPacket.getStringValue(3);
			String sFull = cmdPacket.getStringValue(4);
			String sInfo = cmdPacket.getStringValue(5);
			String sHash = cmdPacket.getStringValue(6);
			String sBaseId = cmdPacket.getStringValue(7);
			String sBaseType = cmdPacket.getStringValue(8);
			String sBaseIsVos = cmdPacket.getStringValue(9);
			return ServerConfig.SQLDAO.addUser(sSessId, sProcId, sName, sFull, sInfo, sHash,
					sBaseId, sBaseType, sBaseIsVos);

		} else if (sCmdCode.equalsIgnoreCase("deleteNode")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sId = cmdPacket.getStringValue(2);
			String sType = cmdPacket.getStringValue(3);
			String sIsVos = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.deleteNode(sSessId, sId, sType, sIsVos);

		} else if (sCmdCode.equalsIgnoreCase("addObjClassAndOp")) {
			String sClass = cmdPacket.getStringValue(2);
			String sDescr = cmdPacket.getStringValue(3);
			String sInfo = cmdPacket.getStringValue(4);
			String sOp = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.addObjClassAndOp(sClientId, sClass, sDescr, sInfo, sOp);

		} else if (sCmdCode.equalsIgnoreCase("deleteObjClassAndOp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sClass = cmdPacket.getStringValue(2);
			String sOp = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.deleteObjClassAndOp(sSessId, sClass, sOp);

		} else if (sCmdCode.equalsIgnoreCase("getObjClasses")) {
			return ServerConfig.SQLDAO.getObjClasses(sClientId);

		} else if (sCmdCode.equalsIgnoreCase("getObjects")) {
			return ServerConfig.SQLDAO.getObjects(sClientId);

		} else if (sCmdCode.equalsIgnoreCase("getPmEntitiesOfClass")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sClass = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getPmEntitiesOfClass(sSessId, sClass);

		} else if (sCmdCode.equalsIgnoreCase("addObject3")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sName = cmdPacket.getStringValue(3);
			String sDescr = cmdPacket.getStringValue(4);
			String sInfo = cmdPacket.getStringValue(5);
			String sContainers = cmdPacket.getStringValue(6);
			String sClass = cmdPacket.getStringValue(7);
			String sType = cmdPacket.getStringValue(8);
			String sHost = cmdPacket.getStringValue(9);
			String sPath = cmdPacket.getStringValue(10);
			String sOrigName = cmdPacket.getStringValue(11);
			String sOrigId = cmdPacket.getStringValue(12);
			String sInh = cmdPacket.getStringValue(13);
			String sSender = cmdPacket.getStringValue(14);
			String sRecip = cmdPacket.getStringValue(15);
			String sSubject = cmdPacket.getStringValue(16);
			String sAttached = cmdPacket.getStringValue(17);
			return ServerConfig.SQLDAO.addObject3(sSessId, sProcId, sName, sDescr, sInfo,
					sContainers, sClass, sType, sHost, sPath, sOrigName,
					sOrigId, sInh, sSender, sRecip, sSubject, sAttached,
					null, null, null);

		} else if (sCmdCode.equalsIgnoreCase("deleteObject")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sObjId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.deleteObject(sSessId, sObjId);

		} else if (sCmdCode.equalsIgnoreCase("deleteObjectStrong")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sObjId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.deleteObjectStrong(sSessId, sObjId);

		} else if (sCmdCode.equalsIgnoreCase("deleteContainerObjects")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sContainerName = cmdPacket.getStringValue(2);
			String sContainerType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.deleteContainerObjects(sSessId, sContainerName, sContainerType);

		} else if (sCmdCode.equalsIgnoreCase("getAssocObj")) {
			String sOaId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getAssocObj(sClientId, sOaId);

		} else if (sCmdCode.equalsIgnoreCase("getObjNamePath")) {
			String sObjName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getObjNamePath(sObjName);

		} else if (sCmdCode.equalsIgnoreCase("getObjClassOps")) {
			String sClass = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getObjClassOps(sClientId, sClass);

		} else if (sCmdCode.equalsIgnoreCase("getHosts")) {
			return ServerConfig.SQLDAO.getHosts(sClientId);

		} else if (sCmdCode.equalsIgnoreCase("addHost")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sName = cmdPacket.getStringValue(2);
			String sRepo = cmdPacket.getStringValue(3);
//			String sReserved = cmdPacket.getStringValue(4);
//			String sIpa = cmdPacket.getStringValue(5);
//			String sDescr = cmdPacket.getStringValue(6);
//			String sPdc = cmdPacket.getStringValue(7);
//			return ServerConfig.SQLDAO.addHost(sSessId, sName, sRepo, sReserved, sIpa, sDescr,
//					sPdc);
			return ServerConfig.SQLDAO.addHost(sSessId, sName, sRepo);

		} else if (sCmdCode.equalsIgnoreCase("updateHost")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sHostId = cmdPacket.getStringValue(2);
			String sHostName = cmdPacket.getStringValue(3);
			String sRepo = cmdPacket.getStringValue(4);
//			String sIpa = cmdPacket.getStringValue(5);
//			String sDescr = cmdPacket.getStringValue(6);
//			String sPdc = cmdPacket.getStringValue(7);
//			return ServerConfig.SQLDAO.updateHost(sSessId, sHostId, sHostName, sRepo, sIpa,
//					sDescr, sPdc);
			return ServerConfig.SQLDAO.updateHost(sSessId, sHostId, sHostName, sRepo);

		} else if (sCmdCode.equalsIgnoreCase("deleteHost")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sHostId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.deleteHost(sSessId, sHostId);

		} else if (sCmdCode.equalsIgnoreCase("getHostInfo")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sHostId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getHostInfo(sSessId, sHostId);

		} else if (sCmdCode.equalsIgnoreCase("getOpsets")) {
			return ServerConfig.SQLDAO.getOpsets(sClientId);

		} else if (sCmdCode.equalsIgnoreCase("addOpsetAndOp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sOpset = cmdPacket.getStringValue(2);
			String sDescr = cmdPacket.getStringValue(3);
			String sInfo = cmdPacket.getStringValue(4);
			String sOp = cmdPacket.getStringValue(5);
			String sBaseId = cmdPacket.getStringValue(6);
			String sBaseType = cmdPacket.getStringValue(7);
			return ServerConfig.SQLDAO.addOpsetAndOp(sSessId, sOpset, sDescr, sInfo, sOp,
					sBaseId, sBaseType);

		} else if (sCmdCode.equalsIgnoreCase("getOpsetOps")) {
			String sOpset = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getOpsetOps(sClientId, sOpset);

		} else if (sCmdCode.equalsIgnoreCase("getOpsetInfo")) {
			String sOpsetId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getOpsetInfo(sClientId, sOpsetId);

		} else if (sCmdCode.equalsIgnoreCase("deleteOpsetAndOp")) {
			String sOpsetId = cmdPacket.getStringValue(2);
			String sOp = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.deleteOpsetAndOp(sClientId, sOpsetId, sOp);

		} else if (sCmdCode.equalsIgnoreCase("createSession")) {
			String sName = cmdPacket.getStringValue(2);
			String sHost = cmdPacket.getStringValue(3);
			String sUser = cmdPacket.getStringValue(4);
			String sHash = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.createSession(sClientId, sName, sHost, sUser, sHash);

		}else if (sCmdCode.equalsIgnoreCase("changePassword")) {
			String sUser = cmdPacket.getStringValue(2);
			String sOldPass = cmdPacket.getStringValue(3);
			String sNewPass = cmdPacket.getStringValue(4);
			String sConPass = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.changePassword(sClientId, sUser, sOldPass, sNewPass,
					sConPass);

		} else if (sCmdCode.equalsIgnoreCase("deleteSession")) {
			String sId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.deleteSession(sClientId, sId);

		} else if (sCmdCode.equalsIgnoreCase("getPermittedOps")) {
			String sCrtSessId = cmdPacket.getStringValue(1);
			String sUserId = cmdPacket.getStringValue(2);
			String sProcId = cmdPacket.getStringValue(3);
			String sObjName = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.newGetPermittedOps(sCrtSessId, sUserId, sProcId, sObjName);

		} else if (sCmdCode.equalsIgnoreCase("getObjEmailProps")) {
			String sCrtSessId = cmdPacket.getStringValue(1);
			String sObjName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getObjEmailProps(sCrtSessId, sObjName);

		} else if (sCmdCode.equalsIgnoreCase("getPosNodeProperties")) {
			String sCrtSessId = cmdPacket.getStringValue(1);
			String sPresType = cmdPacket.getStringValue(2);
			String sNodeId = cmdPacket.getStringValue(3);
			String sNodeLabel = cmdPacket.getStringValue(4);
			String sNodeType = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.getPosNodeProperties(sCrtSessId, sPresType, sNodeId,
					sNodeLabel, sNodeType);

			} else if (sCmdCode.equalsIgnoreCase("getSessions")) {
			return ServerConfig.SQLDAO.getSessions(sClientId);

		}else if (sCmdCode.equalsIgnoreCase("getSessionInfo")) {
			String sSessId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getSessionInfo(sClientId, sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getSessionUser")) {
			String sSessId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getSessionUser(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getSessionName")) {
			String sSessId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getSessionName(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("testIsContained")) {
			String sId1 = cmdPacket.getStringValue(2);
			String sClass1 = cmdPacket.getStringValue(3);
			String sId2 = cmdPacket.getStringValue(4);
			String sClass2 = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.testIsContained(sId1, sClass1, sId2, sClass2);

			// Permissions on a PM entity seen as an object.
			// The entity is identified by its id and type.
		} else if (sCmdCode.equalsIgnoreCase("getUserDescendants")) {
			String sUserId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getUserDescendants(sClientId, sUserId);
		} else if (sCmdCode.equalsIgnoreCase("export")) {
			String sCrtSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.export(sClientId, sCrtSessId);

		} else if (sCmdCode.equalsIgnoreCase("interpretCmd")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sCmd = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);

		} else if (sCmdCode.equalsIgnoreCase("importConfiguration")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.importConfiguration(sSessId, cmdPacket);

		} else if (sCmdCode.equalsIgnoreCase("getHostAppPaths")) {
			String sCrtSessId = cmdPacket.getStringValue(1);
			String sHost = cmdPacket.getStringValue(2);
			String appname = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getHostAppPaths(sClientId, sCrtSessId, sHost, appname);

		} else if (sCmdCode.equalsIgnoreCase("getInstalledAppNames")) {
			String sHost = cmdPacket.getStringValue(2);
			System.out.println("Calling getInstalledAppNames for " + sHost);
			return ServerConfig.SQLDAO.getInstalledApps(sHost);
		} else if (sCmdCode.equalsIgnoreCase("getHostRepository")) {
			String sCrtSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getHostRepository(sCrtSessId);

		} else if (sCmdCode.equalsIgnoreCase(ADD_HOST_APP.commandCode())) {
			String sSessId = cmdPacket.getStringValue(1);
			String sHost = cmdPacket.getStringValue(2);
			String appName = cmdPacket.getStringValue(3);
			String appPath = cmdPacket.getStringValue(4);
			String mainClassName = cmdPacket.getStringValue(5);
			String appPrefix = cmdPacket.getStringValue(6);
			return ServerConfig.SQLDAO.addHostApp(sSessId, sHost, appName, appPath, mainClassName, appPrefix);

		} else if (sCmdCode.equalsIgnoreCase("setHostAppPaths")) {
			String sCrtSessId = cmdPacket.getStringValue(1);
			String sHost = cmdPacket.getStringValue(2);
			String sAtoolPath = cmdPacket.getStringValue(3);
			String sRtfedPath = cmdPacket.getStringValue(4);
			String sWkfPath = cmdPacket.getStringValue(5);
			String sEmlPath = cmdPacket.getStringValue(6);
			String sExpPath = cmdPacket.getStringValue(7);
			String sLauncherPath = cmdPacket.getStringValue(8);
			String sMsofficePath = cmdPacket.getStringValue(9);
			String sMedrecPath = cmdPacket.getStringValue(10);
			String sAcctrecPath = cmdPacket.getStringValue(11);
			String soldWkfPath = cmdPacket.getStringValue(12);
			String sSchemaPath = cmdPacket.getStringValue(13);

			return ServerConfig.SQLDAO.setHostAppPaths(sCrtSessId, sHost, sAtoolPath,
					sRtfedPath, sWkfPath, sEmlPath, sExpPath,
					sLauncherPath, sMsofficePath, sMedrecPath, sAcctrecPath, soldWkfPath, sSchemaPath);

		} else if (sCmdCode.equalsIgnoreCase("getKStorePaths")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getKStorePaths(sClientId, sSessId);

		} else if (sCmdCode.equalsIgnoreCase("setKStorePaths")) {
			String sCrtSessId = cmdPacket.getStringValue(1);
			String sUserId = cmdPacket.getStringValue(2);
			String sHost = cmdPacket.getStringValue(3);
			String sKsPath = cmdPacket.getStringValue(4);
			String sTsPath = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.setKStorePaths(sCrtSessId, sUserId, sHost, sKsPath,
					sTsPath);
		} else if (sCmdCode.equalsIgnoreCase("getDenies")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getDenies(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getDenyInfo")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sDenyId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getDenyInfo(sSessId, sDenyId);

		} else if (sCmdCode.equalsIgnoreCase("getAllOps")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getAllOps(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getOattrs")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getOattrs(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getOconts")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getOattrs(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getObjAttrsProper")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getObjAttrsProper(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("addDeny")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sDenyName = cmdPacket.getStringValue(2);
			String sDenyType = cmdPacket.getStringValue(3);
			String sUserOrAttrName = cmdPacket.getStringValue(4);
			String sUserOrAttrId = cmdPacket.getStringValue(5);
			String sOp = cmdPacket.getStringValue(6);
			String sOattrName = cmdPacket.getStringValue(7);
			String sOattrId = cmdPacket.getStringValue(8);
			String sIsInters = cmdPacket.getStringValue(9);
			return ServerConfig.SQLDAO.addDeny(sSessId, sDenyName, sDenyType, sUserOrAttrName,
					sUserOrAttrId, sOp, sOattrName, sOattrId, sIsInters);

		} else if (sCmdCode.equalsIgnoreCase("deleteDeny")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sDenyName = cmdPacket.getStringValue(2);
			String sOp = cmdPacket.getStringValue(3);
			String sOattrName = cmdPacket.getStringValue(4);
			String sOattrId = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.deleteDeny(sSessId, sDenyName, sOp, sOattrName, sOattrId);

		} else if (sCmdCode.equalsIgnoreCase("getIdOfEntityWithNameAndType")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sEntityName = cmdPacket.getStringValue(2);
			String sEntityType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getEntityId(sSessId, sEntityName, sEntityType);
		} else if (sCmdCode.equalsIgnoreCase("getNameOfEntityWithIdAndType")) {
			if (cmdPacket.size() < 4) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sEntityId = cmdPacket.getStringValue(2);
			String sEntityType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getEntityName(sSessId, sEntityId, sEntityType);
			// Obligation commands - need to be activated when Obligation feature is turned on
		} else if (sCmdCode.equalsIgnoreCase("getScripts")) {
				return ServerConfig.obligationDAO.getScripts();

		} else if (sCmdCode.equalsIgnoreCase("enableScript")) {
			if (cmdPacket.size() < 3) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sScriptId = cmdPacket.getStringValue(2);
				return ServerConfig.obligationDAO.enableScript(sSessId, sScriptId);

		} else if (sCmdCode.equalsIgnoreCase("disableEnabledScript")) {
			if (cmdPacket.size() < 2) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.obligationDAO.disableEnabledScript(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getEnabledScript")) {
				return ServerConfig.obligationDAO.getEnabledScript();

		} else if (sCmdCode.equalsIgnoreCase("compileScript")) {
			if (cmdPacket.size() < 3) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sDeleteOthers = cmdPacket.getStringValue(2);
				return ServerConfig.obligationDAO.compileScript(sSessId, sDeleteOthers, cmdPacket);

		} else if (sCmdCode.equalsIgnoreCase("compileScriptAndEnable")) {
			if (cmdPacket.size() < 2) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.obligationDAO.compileScriptAndEnable(sSessId, cmdPacket);

		} else if (sCmdCode.equalsIgnoreCase("compileScriptAndAddToEnabled")) {
			if (cmdPacket.size() < 2) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.obligationDAO.compileScriptAndAddToEnabled(sSessId, cmdPacket);

		} else if (sCmdCode.equalsIgnoreCase("addScript")) {
			if (cmdPacket.size() < 4) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sScriptId1 = cmdPacket.getStringValue(2);
			String sScriptId2 = cmdPacket.getStringValue(3);
				return ServerConfig.obligationDAO.addScript(sSessId, sScriptId1, sScriptId2);

		} else if (sCmdCode.equalsIgnoreCase("addScriptToEnabled")) {
			if (cmdPacket.size() < 3) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sScriptId = cmdPacket.getStringValue(2);
				return ServerConfig.obligationDAO.addScriptToEnabled(sSessId, sScriptId);

		} else if (sCmdCode.equalsIgnoreCase("deleteScriptsWithNameExcept")) {
			if (cmdPacket.size() < 4) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sScriptName = cmdPacket.getStringValue(2);
			String sScriptId = cmdPacket.getStringValue(3);
				return ServerConfig.obligationDAO.deleteScriptsWithNameExcept(sSessId, sScriptName,
						sScriptId);

		} else if (sCmdCode.equalsIgnoreCase("deleteScript")) {
			if (cmdPacket.size() < 3) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sScriptId = cmdPacket.getStringValue(2);
				return ServerConfig.obligationDAO.deleteScript(sSessId, sScriptId);

		} else if (sCmdCode.equalsIgnoreCase("getSourceScript")) {
			if (cmdPacket.size() < 3) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sScriptId = cmdPacket.getStringValue(2);
				return ServerConfig.obligationDAO.getSourceScript(sSessId, sScriptId);

		} else if (sCmdCode.equalsIgnoreCase("deleteScriptRule")) {
			if (cmdPacket.size() < 4) {
				return failurePacket("Too few arguments!");
			}
			String sScriptId = cmdPacket.getStringValue(2);
			String sLabel = cmdPacket.getStringValue(3);
				return ServerConfig.obligationDAO.deleteScriptRule(sScriptId, sLabel);

		} else if (sCmdCode.equalsIgnoreCase("processEvent")) {
			if (cmdPacket.size() < 7) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sEventName = cmdPacket.getStringValue(3);
			String sObjName = cmdPacket.getStringValue(4);
			String sObjId = cmdPacket.getStringValue(5);
			String sObjClass = cmdPacket.getStringValue(6);
			String sObjType = cmdPacket.getStringValue(7);
			String sCtx1 = cmdPacket.getStringValue(8);
			String sCtx2 = cmdPacket.getStringValue(9);
				return ServerConfig.obligationDAO.processEvent(sSessId, sProcId, sEventName, sObjName,
						sObjId, sObjClass, sObjType, sCtx1, sCtx2);

		} else if (sCmdCode.equalsIgnoreCase("genDac")) {
			if (cmdPacket.size() < 4) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sPolName = cmdPacket.getStringValue(2);
			String sContName = cmdPacket.getStringValue(3);
				return ServerConfig.obligationDAO.genDac(sSessId, sPolName, sContName);

		} else if (sCmdCode.equalsIgnoreCase("genMls")) {
			if (cmdPacket.size() < 4) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sPolName = cmdPacket.getStringValue(2);
			String sLevels = cmdPacket.getStringValue(3);
				return ServerConfig.obligationDAO.genMls(sSessId, sPolName, sLevels);

		} else if (sCmdCode.equalsIgnoreCase("genIbac")) {
			if (cmdPacket.size() < 3) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sPolName = cmdPacket.getStringValue(2);
				return ServerConfig.obligationDAO.genIbac(sSessId, sPolName);

		} else if (sCmdCode.equalsIgnoreCase("sendObject")) {
			if (cmdPacket.size() < 4) {
				return failurePacket("Too few arguments!");
			}
			String sSessId = cmdPacket.getStringValue(1);
			String sObjName = cmdPacket.getStringValue(2);
			String sReceiver = cmdPacket.getStringValue(3);
			return ServerConfig.obligationDAO.sendObject(sSessId, sObjName, sReceiver);
		} else if (sCmdCode.equalsIgnoreCase("getAttrInfo")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sAttrId = cmdPacket.getStringValue(2);
			String sAttrType = cmdPacket.getStringValue(3);
			String sIsVos = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.getAttrInfo(sSessId, sAttrId, sAttrType, sIsVos);

		} else if (sCmdCode.equalsIgnoreCase("addProp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sId = cmdPacket.getStringValue(2);
			String sType = cmdPacket.getStringValue(3);
			String sIsVos = cmdPacket.getStringValue(4);
			String sProp = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.addProp(sSessId, sId, sType, sIsVos, sProp);

		} else if (sCmdCode.equalsIgnoreCase("replaceProp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sId = cmdPacket.getStringValue(2);
			String sType = cmdPacket.getStringValue(3);
			String sIsVos = cmdPacket.getStringValue(4);
			String sOldProp = cmdPacket.getStringValue(5);
			String sNewProp = cmdPacket.getStringValue(6);
			return ServerConfig.SQLDAO.replaceProp(sSessId, sId, sType, sIsVos, sOldProp,
					sNewProp);

		} else if (sCmdCode.equalsIgnoreCase("removeProp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sId = cmdPacket.getStringValue(2);
			String sType = cmdPacket.getStringValue(3);
			String sIsVos = cmdPacket.getStringValue(4);
			String sProp = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.removeProp(sSessId, sId, sType, sIsVos, sProp);

		} else if (sCmdCode.equalsIgnoreCase("createObject3")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sObjName = cmdPacket.getStringValue(3);
			String sObjClass = cmdPacket.getStringValue(4);
			String sObjType = cmdPacket.getStringValue(5);
			String sContainers = cmdPacket.getStringValue(6);
			String sPerms = cmdPacket.getStringValue(7);
			String sSender = cmdPacket.getStringValue(8);
			String sReceiver = cmdPacket.getStringValue(9);
			String sSubject = cmdPacket.getStringValue(10);
			String sAttached = cmdPacket.getStringValue(11);
			return ServerConfig.SQLDAO.createObject3(sSessId, sProcId, sObjName, sObjClass,
					sObjType, sContainers, sPerms, sSender, sReceiver,
					sSubject, sAttached);

		} else if (sCmdCode.equalsIgnoreCase("getUserInfo")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sUserId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getUserInfo(sSessId, sUserId);

		} else if (sCmdCode.equalsIgnoreCase("getOpsetsBetween")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sUattrName = cmdPacket.getStringValue(2);
			String sEntityName = cmdPacket.getStringValue(3);
			String sEntityType = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.getOpsetsBetween(sSessId, sUattrName, sEntityName,
					sEntityType);

		} else if (sCmdCode.equalsIgnoreCase("deleteOpsetsBetween")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sUattrName = cmdPacket.getStringValue(3);
			String sOattrName = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.deleteOpsetsBetween(sSessId, sProcId, sUattrName,
					sOattrName);

		} else if (sCmdCode.equalsIgnoreCase("getOpsetOattrs")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sOpsetName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getOpsetOattrs(sSessId, sOpsetName);

		} else if (sCmdCode.equalsIgnoreCase("isolateOattr")) {
			String sAttrName = cmdPacket.getStringValue(2);
			String sAttrType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.isolateOattr(sAttrName, sAttrType);

		} else if (sCmdCode.equalsIgnoreCase("setPerms")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sUattrName = cmdPacket.getStringValue(3);
			String sOpset = cmdPacket.getStringValue(4);
			String sSuggOattr = cmdPacket.getStringValue(5);
			String sSuggBase = cmdPacket.getStringValue(6);
			String sSuggBaseType = cmdPacket.getStringValue(7);
			String sPerms = cmdPacket.getStringValue(8);
			String sEntName = cmdPacket.getStringValue(9);
			String sEntType = cmdPacket.getStringValue(10);
			String sInclAscs = cmdPacket.getStringValue(11);
			return ServerConfig.SQLDAO.setPerms(sSessId, sProcId, sUattrName, sOpset,
					sSuggOattr, sSuggBase, sSuggBaseType, sPerms, sEntName,
					sEntType, sInclAscs);

		} else if (sCmdCode.equalsIgnoreCase("getEntityWithProp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sEntType = cmdPacket.getStringValue(2);
			String sProp = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getEntityWithProp(sSessId, sEntType, sProp);

		} else if (sCmdCode.equalsIgnoreCase("doDacConfinement")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sUser = cmdPacket.getStringValue(2);
			String sPc = cmdPacket.getStringValue(3);
			String sAttr = cmdPacket.getStringValue(4);
			String sCont = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.doDacConfinement(sSessId, sUser, sPc, sAttr, sCont);

		} else if (sCmdCode.equalsIgnoreCase("testGetMemberObjects")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sContId = cmdPacket.getStringValue(2);
			String sContType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.testGetMemberObjects(sSessId, sContId, sContType);

		} else if (sCmdCode.equalsIgnoreCase("isAssigned")) {
			String sId1 = cmdPacket.getStringValue(2);
			String sType1 = cmdPacket.getStringValue(3);
			String sId2 = cmdPacket.getStringValue(4);
			String sType2 = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.isAssigned(sId1, sType1, sId2, sType2);

		} else if (sCmdCode.equalsIgnoreCase("getIdOfEntityWithNameAndType")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sName = cmdPacket.getStringValue(2);
			String sType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getEntityId(sSessId, sName, sType);

		} else if (sCmdCode.equalsIgnoreCase("getNameOfEntityWithIdAndType")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sId = cmdPacket.getStringValue(2);
			String sType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getEntityName(sSessId, sId, sType);

		} else if (sCmdCode.equalsIgnoreCase("testGetDeniedPerms")) {
			String sSessId = cmdPacket.getStringValue(2);
			String sObjName = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.testGetDeniedPerms(sSessId, sObjName);

		} else if (sCmdCode.equalsIgnoreCase("genConfForDacUser")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sUser = cmdPacket.getStringValue(2);
			String sPc = cmdPacket.getStringValue(3);
			String sAttr = cmdPacket.getStringValue(4);
			String sCont = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.genConfForDacUser(sSessId, sUser, sPc, sAttr, sCont);

		} else if (sCmdCode.equalsIgnoreCase("genDacUserWithConf")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sUser = cmdPacket.getStringValue(2);
			String sFullName = cmdPacket.getStringValue(3);
			String sPc = cmdPacket.getStringValue(4);
			String sAttr = cmdPacket.getStringValue(5);
			String sCont = cmdPacket.getStringValue(6);
			return ServerConfig.SQLDAO.genDacUserWithConf(sSessId, sUser, sFullName, sPc,
					sAttr, sCont);

		} else if (sCmdCode.equalsIgnoreCase("addEmailAcct")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sPmUser = cmdPacket.getStringValue(2);
			String sFullName = cmdPacket.getStringValue(3);
			String sEmailAddr = cmdPacket.getStringValue(4);
			String sPopServer = cmdPacket.getStringValue(5);
			String sSmtpServer = cmdPacket.getStringValue(6);
			String sAcctName = cmdPacket.getStringValue(7);
			String sPassword = cmdPacket.getStringValue(8);
			return ServerConfig.SQLDAO.addEmailAcct(sSessId, sPmUser, sFullName, sEmailAddr,
					sPopServer, sSmtpServer, sAcctName, sPassword);

		} else if (sCmdCode.equalsIgnoreCase("getEmailAcct")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sPmUser = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getEmailAcct(sSessId, sPmUser);

		} else if (sCmdCode.equalsIgnoreCase("sendSimpleMsg")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sMsgName = cmdPacket.getStringValue(2);
			String sReceiver = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.sendSimpleMsg(sSessId, sMsgName, sReceiver);

		} else if (sCmdCode.equalsIgnoreCase("copyObject")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sObjName = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.copyObject(sSessId, sProcId, sObjName);

		} else if (sCmdCode.equalsIgnoreCase("getEmailRecipients")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sReceiver = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getEmailRecipients(sSessId, sReceiver);

		} else if (sCmdCode.equalsIgnoreCase("getUsersAndAttrs")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getUsersAndAttrs(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("addOpenObj")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sObjName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.addOpenObj(sSessId, sObjName);

		} else if (sCmdCode.equalsIgnoreCase("deleteOpenObj")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sObjName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.deleteOpenObj(sSessId, sObjName);

		} else if (sCmdCode.equalsIgnoreCase("assignObjToOattrWithProp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sObjName = cmdPacket.getStringValue(3);
			String sProp = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.assignObjToOattrWithProp(sSessId, sProcId, sObjName,
					sProp);

		} else if (sCmdCode.equalsIgnoreCase("assignObjToOattr")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sObjName = cmdPacket.getStringValue(3);
			String sOattrName = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.assignObjToOattr(sSessId, sProcId, sObjName, sOattrName);

		} else if (sCmdCode.equalsIgnoreCase("deassignObjFromOattrWithProp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sObjName = cmdPacket.getStringValue(3);
			String sProp = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.deassignObjFromOattrWithProp(sSessId, sProcId, sObjName,
					sProp);

		} else if (sCmdCode.equalsIgnoreCase("isObjInOattrWithProp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sObjName = cmdPacket.getStringValue(2);
			String sProp = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.isObjInOattrWithProp(sSessId, sObjName, sProp);

		} else if (sCmdCode.equalsIgnoreCase("getInboxMessages")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getInboxMessages(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getOutboxMessages")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getOutboxMessages(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getFileContent")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sFileProp = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getFileContent(sSessId, sFileProp, bisFromClient,
					bosToClient);

		} else if (sCmdCode.equalsIgnoreCase("createObjForFile")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sFilePath = cmdPacket.getStringValue(2);
			String sContName = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.createObjForFile(sSessId, sFilePath, sContName);

		} else if (sCmdCode.equalsIgnoreCase("createContForFolder")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sFolderPath = cmdPacket.getStringValue(2);
			String sBaseContName = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.createContForFolder(sSessId, sFolderPath, sBaseContName);

		} else if (sCmdCode.equalsIgnoreCase("getDascUattrs")) {
			String sBaseName = cmdPacket.getStringValue(2);
			String sBaseType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getDascUattrs(sBaseName, sBaseType);

		} else if (sCmdCode.equalsIgnoreCase("getDascUsers")) {
			String sBaseName = cmdPacket.getStringValue(2);
			String sBaseType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getDascUsers(sBaseName, sBaseType);

		} else if (sCmdCode.equalsIgnoreCase("getDascOattrs")) {
			String sBaseName = cmdPacket.getStringValue(2);
			String sBaseType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getDascOattrs(sBaseName, sBaseType);

		} else if (sCmdCode.equalsIgnoreCase("getDascObjects")) {
			String sBaseName = cmdPacket.getStringValue(2);
			String sBaseType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getDascObjects(sBaseName, sBaseType);

		} else if (sCmdCode.equalsIgnoreCase("getDascOpsets")) {
			String sBaseName = cmdPacket.getStringValue(2);
			String sBaseType = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getDascOpsets(sBaseName, sBaseType);

		} else if (sCmdCode.equalsIgnoreCase("getContainersOf")) {
			log.debug("TRACE 4 - In PacketHandler.getContainersOf() - Got getContainersOf");

			String sSessId = cmdPacket.getStringValue(1);
			String sBaseName = cmdPacket.getStringValue(2);
			String sBaseId = cmdPacket.getStringValue(3);
			String sBaseType = cmdPacket.getStringValue(4);
			String sGraphType = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.getContainersOf(sSessId, sBaseName, sBaseId, sBaseType,
					sGraphType);

		} else if (sCmdCode.equalsIgnoreCase("getMellContainersOf")) {
			log.debug("TRACE 4 - In PacketHandler.getMellContainersOf()");

			String sSessId = cmdPacket.getStringValue(1);
			String sBaseName = cmdPacket.getStringValue(2);
			String sBaseId = cmdPacket.getStringValue(3);
			String sBaseType = cmdPacket.getStringValue(4);
			String sGraphType = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.getMellContainersOf(sSessId, sBaseName, sBaseId, sBaseType,
					sGraphType);

		} else if (sCmdCode.equalsIgnoreCase("getMembersOf")) {
			log.debug("TRACE 4 - In PacketHandler.executeCommand() - Got getMembersOf");

			String sSessId = cmdPacket.getStringValue(1);
			String sBaseName = cmdPacket.getStringValue(2);
			String sBaseId = cmdPacket.getStringValue(3);
			String sBaseType = cmdPacket.getStringValue(4);
			String sGraphType = cmdPacket.getStringValue(5);

			return ServerConfig.SQLDAO.getMembersOf(sSessId, sBaseName, sBaseId, sBaseType, sGraphType);

		} else if (sCmdCode.equalsIgnoreCase("getMellMembersOf")) {
			log.debug("TRACE 4 - In PacketHandler.executeCommand() - Got getMellMembersOf");

			String sSessId = cmdPacket.getStringValue(1);
			String sBaseName = cmdPacket.getStringValue(2);
			String sBaseId = cmdPacket.getStringValue(3);
			String sBaseType = cmdPacket.getStringValue(4);
			String sGraphType = cmdPacket.getStringValue(5);
			return ServerConfig.SQLDAO.getMellMembersOf(sSessId, sBaseName, sBaseId, sBaseType, sGraphType);

		} else if (sCmdCode.equalsIgnoreCase("getConnector")) {
			return ServerConfig.SQLDAO.getConnector();

		} else if (sCmdCode.equalsIgnoreCase("showAccessibleObjects")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sName = cmdPacket.getStringValue(2);
			String sId = cmdPacket.getStringValue(3);
			String sType = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.showAccessibleObjects(sSessId, sName, sId, sType);
		
		} else if (sCmdCode.equalsIgnoreCase("findBorderOaPrivRelaxed")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sName = cmdPacket.getStringValue(2);
			String sId = cmdPacket.getStringValue(3);
			String sType = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.findBorderOaPrivRelaxed(sSessId, sName, sId, sType);
		
		} else if (sCmdCode.equalsIgnoreCase("findBorderOaPrivRestricted")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sName = cmdPacket.getStringValue(2);
			String sId = cmdPacket.getStringValue(3);
			String sType = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.findBorderOaPrivRestricted(sSessId, sName, sId, sType);
			
		} else if (sCmdCode.equalsIgnoreCase("initialOas")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sName = cmdPacket.getStringValue(2);
			String sId = cmdPacket.getStringValue(3);
			String sType = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.initialOas(sSessId, sName, sId, sType);
			
		} else if (sCmdCode.equalsIgnoreCase("initialOasWithLabels")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sName = cmdPacket.getStringValue(2);
			String sId = cmdPacket.getStringValue(3);
			String sType = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.initialOasWithLabels(sSessId, sName, sId, sType);
			
		} else if (sCmdCode.equalsIgnoreCase("subsequentOas")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sUserName = cmdPacket.getStringValue(2);
			String sUserId = cmdPacket.getStringValue(3);
			String sUserType = cmdPacket.getStringValue(4);
			String sTgtName = cmdPacket.getStringValue(5);
			String sTgtId = cmdPacket.getStringValue(6);
			String sTgtType = cmdPacket.getStringValue(7);
			return ServerConfig.SQLDAO.subsequentOas(sSessId, sUserName, sUserId, sUserType,
					sTgtName, sTgtId, sTgtType);
			
		} else if (sCmdCode.equalsIgnoreCase("successorOas")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sUserName = cmdPacket.getStringValue(2);
			String sUserId = cmdPacket.getStringValue(3);
			String sUserType = cmdPacket.getStringValue(4);
			String sTgtName = cmdPacket.getStringValue(5);
			String sTgtId = cmdPacket.getStringValue(6);
			String sTgtType = cmdPacket.getStringValue(7);
			return ServerConfig.SQLDAO.successorOas(sSessId, sUserName, sUserId, sUserType,
					sTgtName, sTgtId, sTgtType);
			
		} else if (sCmdCode.equalsIgnoreCase("calcPriv")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sUserName = cmdPacket.getStringValue(2);
			String sUserId = cmdPacket.getStringValue(3);
			String sUserType = cmdPacket.getStringValue(4);
			String sTgtName = cmdPacket.getStringValue(5);
			String sTgtId = cmdPacket.getStringValue(6);
			String sTgtType = cmdPacket.getStringValue(7);
			return ServerConfig.SQLDAO.calcPriv(sSessId, sUserName, sUserId, sUserType,
					sTgtName, sTgtId, sTgtType);
			
		} else if (sCmdCode.equalsIgnoreCase("addTemplate")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sTplName = cmdPacket.getStringValue(2);
			String sContainers = cmdPacket.getStringValue(3);
			String sKeys = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.addTemplate(sSessId, sTplName, sContainers, sKeys);

		} else if (sCmdCode.equalsIgnoreCase("getTemplates")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getTemplates(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("getTemplateInfo")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sTplId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getTemplateInfo(sSessId, sTplId);

		} else if (sCmdCode.equalsIgnoreCase("createRecord")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sRecordName = cmdPacket.getStringValue(3);
			String sBase = cmdPacket.getStringValue(4);
			String sBaseType = cmdPacket.getStringValue(5);
			String sTplId = cmdPacket.getStringValue(6);
			String sComponents = cmdPacket.getStringValue(7);
			String[] sKeys = getItemsFromPacket(cmdPacket, 8);
			return ServerConfig.SQLDAO.createRecord(sSessId, sProcId, sRecordName, sBase,
					sBaseType, sTplId, sComponents, sKeys);

		} else if (sCmdCode.equalsIgnoreCase("createRecordInEntityWithProp")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sProcId = cmdPacket.getStringValue(2);
			String sRecordName = cmdPacket.getStringValue(3);
			String sProp = cmdPacket.getStringValue(4);
			String sBaseType = cmdPacket.getStringValue(5);
			String sTplId = cmdPacket.getStringValue(6);
			String sComponents = cmdPacket.getStringValue(7);
			String[] sKeys = getItemsFromPacket(cmdPacket, 8);
			return ServerConfig.SQLDAO.createRecordInEntityWithProp(sSessId, sProcId,
					sRecordName, sProp, sBaseType, sTplId, sComponents,
					sKeys);

		} else if (sCmdCode.equalsIgnoreCase("getRecords")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sTplId = cmdPacket.getStringValue(2);
			String sKey = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.getRecords(sSessId, sTplId, sKey);

		} else if (sCmdCode.equalsIgnoreCase("getRecordInfo")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sOattrId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getRecordInfo(sSessId, sOattrId);

		} else if (sCmdCode.equalsIgnoreCase("getObjInfo")) {
			String sObjId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getObjInfo(sObjId);

		} else if (sCmdCode.equalsIgnoreCase("getObjProperties")){
			String sObjId = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getObjProperties(sObjId);
		} else if (sCmdCode.equalsIgnoreCase("testExportRecords")) {
			return ServerConfig.SQLDAO.testExportRecords();

		} else if (sCmdCode.equalsIgnoreCase("getUsersOf")) {
			String sUattrName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getUsersOf(sUattrName);

		} else if (sCmdCode.equalsIgnoreCase("setRecordKeys")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sRecName = cmdPacket.getStringValue(2);
			String[] sKeys = getItemsFromPacket(cmdPacket, 3);
			return ServerConfig.SQLDAO.setRecordKeys(sSessId, sRecName, sKeys);

		} else if (sCmdCode.equalsIgnoreCase("addRecordKeys")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sRecName = cmdPacket.getStringValue(2);
			String[] sKeys = getItemsFromPacket(cmdPacket, 3);
			return ServerConfig.SQLDAO.addRecordKeys(sSessId, sRecName, sKeys);

		} else if (sCmdCode.equalsIgnoreCase("getProperties")) {
			String sSessId = cmdPacket.getStringValue(1);
			return ServerConfig.SQLDAO.getProperties(sSessId);

		} else if (sCmdCode.equalsIgnoreCase("deleteProperty")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sPropName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.deleteProperty(sSessId, sPropName);

		} else if (sCmdCode.equalsIgnoreCase("setProperty")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sPropName = cmdPacket.getStringValue(2);
			String sPropValue = cmdPacket.getStringValue(3);
			return ServerConfig.SQLDAO.setProperty(sSessId, sPropName, sPropValue);

		} else if (sCmdCode.equalsIgnoreCase("getPropertyValue")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sPropName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getPropertyValue(sSessId, sPropName);

		} else if (sCmdCode.equalsIgnoreCase("getProperty")) {
			String sSessId = cmdPacket.getStringValue(1);
			String sPropName = cmdPacket.getStringValue(2);
			return ServerConfig.SQLDAO.getProperty(sSessId, sPropName);

		}else if (sCmdCode.equalsIgnoreCase("audit")) {
			//NDK Added this
			String sSessId = cmdPacket.getStringValue(1);
			String sEvent = cmdPacket.getStringValue(2);
			String sObjId = cmdPacket.getStringValue(3);
			String sResult = cmdPacket.getStringValue(4);
			return ServerConfig.SQLDAO.audit(sSessId, sEvent, sObjId, sResult);
		}else {
			System.out.println(sCmdCode);
			try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return failurePacket("Unknown command 2");
		}
	}

}

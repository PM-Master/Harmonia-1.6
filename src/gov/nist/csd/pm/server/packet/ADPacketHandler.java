package gov.nist.csd.pm.server.packet;

import static gov.nist.csd.pm.common.info.PMCommand.ADD_HOST_APP;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.Log;
import gov.nist.csd.pm.server.dao.ActiveDirectory.ActiveDirectoryDAO;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ADPacketHandler {

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

		if (!PacketValidator.isValid(cmdPacket)) {
			return failurePacket("Too few arguments!");
		}
		int paramSize = PacketValidator.commandsSize.get(sCmdCode).intValue();
		String[] reqParams = getItemsFromPacket(cmdPacket, paramSize);

		System.out.println(sCmdCode);
		System.out.println("PM Engine Dispatch " + sCmdCode);

		// Dispatch the command
		try {//if(createColumns)
			if(sCmdCode.equalsIgnoreCase("setTableModifying")){
				ServerConfig.ADDAO.setTableModifying(cmdPacket.getStringValue(2));
			}else if (sCmdCode.equalsIgnoreCase("buildClipboard")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sOattrName = cmdPacket.getItemStringValue(2);
				return ServerConfig.ADDAO.buildClipboard(sSessId, sOattrName);
			}else if(sCmdCode.equalsIgnoreCase("getFromAttrs1")){
				String sSessId = cmdPacket.getStringValue(1);
				String sOpsetId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getFromAttrs1(sSessId, sOpsetId);
			} else if(sCmdCode.equalsIgnoreCase("getFromUserAttrsPacket")){
				String sBaseId = cmdPacket.getStringValue(2);
				String sBaseType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getFromUserAttrsPacket(sBaseId, sBaseType);
			} else if(sCmdCode.equalsIgnoreCase("isRecordPacket")){
				String sId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.isRecordPacket(sId);
			}else if(sCmdCode.equalsIgnoreCase("createSchemaPC")){
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String policyType = cmdPacket.getStringValue(3);
				String oattrType = cmdPacket.getStringValue(4);
				String sPolicyClass = cmdPacket.getStringValue(5);
				String sUattr = cmdPacket.getStringValue(6);
				System.out.println("in packethandler: " + sSessId + ":" + policyType + ":" + oattrType + ":" + sPolicyClass + ":" + sUattr);
				return ServerConfig.ADDAO.createSchemaPC(sSessId, sProcId, policyType,
						oattrType, sPolicyClass, sUattr);
			}else if(sCmdCode.equalsIgnoreCase("addSchemaOattr")){
				String sSessId = cmdPacket.getStringValue(1);
				String oattrType = cmdPacket.getStringValue(3);
				String name = cmdPacket.getStringValue(4);
				String baseName = cmdPacket.getStringValue(5);
				String baseType = cmdPacket.getStringValue(6);
				return ServerConfig.ADDAO.addSchemaOattr(sSessId, oattrType, name, baseName, baseType);
			}else if(sCmdCode.equalsIgnoreCase("getContainers")){
				String id = cmdPacket.getStringValue(2);
				String type = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getContainers(id, type);
			}else if(sCmdCode.equalsIgnoreCase("getToAttrsUser")){
				String sId = cmdPacket.getStringValue(1);
				String sType = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getFromAttrs1(sId, sType);
			}else if(sCmdCode.equalsIgnoreCase("getPermittedOpsOnEntity")){
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sId = cmdPacket.getStringValue(3);
				String sType = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.getPermittedOpsOnEntity(sSessId, sProcId, sId, sType);
			}else if(sCmdCode.equalsIgnoreCase("getAssocOattr1")){
				String sSessId = cmdPacket.getStringValue(1);
				String sId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getAssocOattr1(sId);
			}else if(sCmdCode.equalsIgnoreCase("getReps")){
				String sSessId = cmdPacket.getStringValue(1);
				String sId = cmdPacket.getStringValue(2);
				String sType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getReps(sId, sType);
			}else if(sCmdCode.equalsIgnoreCase("getFromOpsets1")){
				String sSessId = cmdPacket.getStringValue(1);
				String sOpsetId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getFromOpsets1(sSessId, sOpsetId);
			}else if(sCmdCode.equalsIgnoreCase("getPermittedOps1")){
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sBaseId = cmdPacket.getStringValue(3);
				String sBaseType = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.getPermittedOps1(sSessId, sProcId, sBaseId, sBaseType);
			}else if(sCmdCode.equalsIgnoreCase("getArrayPerms")){
				String sSessId = cmdPacket.getStringValue(1);
				String sOpsetId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getFromAttrs1(sSessId, sOpsetId);
			}else if(sCmdCode.equalsIgnoreCase("getToAttrs1")){
				String sSessId = cmdPacket.getStringValue(1);
				String sOpsetId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getToAttrs1(sSessId, sOpsetId);
			}else if(sCmdCode.equalsIgnoreCase("setSchemaPerms")){
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sBaseName = cmdPacket.getStringValue(3);
				String sBaseType = cmdPacket.getStringValue(4);
				String sAttrName = cmdPacket.getStringValue(5);
				String uattr = cmdPacket.getStringValue(6);
				return ServerConfig.ADDAO.setSchemaPerms(sSessId, sProcId, sBaseName, sBaseType, sAttrName, uattr);
			}else if(sCmdCode.equalsIgnoreCase("setTablePerms")){
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sBaseName = cmdPacket.getStringValue(3);
				String sBaseType = cmdPacket.getStringValue(4);
				String sAttrName = cmdPacket.getStringValue(5);
				String sAttrType = cmdPacket.getStringValue(6);
				String sPerms = cmdPacket.getStringValue(7);
				String uattr = cmdPacket.getStringValue(8);
				return ServerConfig.ADDAO.setTablePerms(sSessId, sProcId, sBaseName, sBaseType, sAttrName, sAttrType, sPerms, uattr);
			}else if(sCmdCode.equalsIgnoreCase("deleteTemplate")){
				String sSessId = cmdPacket.getStringValue(1);
				String sTplId = cmdPacket.getStringValue(2);
				String sTplName = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.deleteTemplate(sSessId, sTplId, sTplName);
			}else if (sCmdCode.equalsIgnoreCase("testSynchro")) {
				return ServerConfig.ADDAO.testSynchro(sClientId);

			} 
			// CONNECT comand 
			if (sCmdCode.equalsIgnoreCase("connect")) {
				return getSuccessPacket();
			}
			// RESET command 
			else if (sCmdCode.equalsIgnoreCase("reset")) {
				System.out.println("*********************************** $$$$$$$$$$$$$$$$$$$ Reset is called from Admin");
				String crtSessionId = cmdPacket.getStringValue(1);
				ServerConfig.ADDAO.reset(crtSessionId);
				return getSuccessPacket();

			} else if (sCmdCode.equalsIgnoreCase("getPolicyClasses")) {
				return ServerConfig.ADDAO.getPolicyClasses(sClientId);

			} else if (sCmdCode.equalsIgnoreCase("addPc")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sName = cmdPacket.getStringValue(3);
				String sDescr = cmdPacket.getStringValue(4);
				String sInfo = cmdPacket.getStringValue(5);
				// The properties start at item 5:
				String[] sProps = getItemsFromPacket(cmdPacket, 6);
				return ServerConfig.ADDAO.addPc(sSessId, sProcId, sName, sDescr, sInfo, sProps);

			} else if (sCmdCode.equalsIgnoreCase("getPcInfo")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPcId = cmdPacket.getStringValue(2);
				String sIsVos = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getPcInfo(sSessId, sPcId, sIsVos);

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
				return ServerConfig.ADDAO.addUattr(sClientId, crtSessionId, sProcId, sName,
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
				return ServerConfig.ADDAO.addOattr(crtSessionId, sProcId, sName, sDescr, sInfo,
						sBaseId, sBaseType, sBaseIsVos, sAssocObjId, sProps);

			} else if (sCmdCode.equalsIgnoreCase("assign")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sId1 = cmdPacket.getStringValue(3);
				String sType1 = cmdPacket.getStringValue(4);
				String sId2 = cmdPacket.getStringValue(5);
				String sType2 = cmdPacket.getStringValue(6);
				String sIsAdminVos = cmdPacket.getStringValue(7);
				return ServerConfig.ADDAO.assign(sSessId, sProcId, sId1, sType1, sId2, sType2);
				//,sIsAdminVos);

			} else if (sCmdCode.equalsIgnoreCase("deleteAssignment")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sId1 = cmdPacket.getStringValue(3);
				String sType1 = cmdPacket.getStringValue(4);
				String sId2 = cmdPacket.getStringValue(5);
				String sType2 = cmdPacket.getStringValue(6);
				String sIsAdminVos = cmdPacket.getStringValue(7);
				return ServerConfig.ADDAO.deleteAssignment(sSessId, sProcId, sId1, sType1, sId2,
						sType2, sIsAdminVos);

			} else if (sCmdCode.equalsIgnoreCase("getUserAttributes")) {
				return ServerConfig.ADDAO.getUserAttributes(sClientId);

			} else if (sCmdCode.equalsIgnoreCase("getUsers")) {
				return ServerConfig.ADDAO.getUsers(sClientId);

			} else if (sCmdCode.equalsIgnoreCase("getAsets")) {
				return ServerConfig.ADDAO.getAsets(sClientId);

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
				return ServerConfig.ADDAO.addUser(sSessId, sProcId, sName, sFull, sInfo, sHash,
						sBaseId, sBaseType, sBaseIsVos);

			} else if (sCmdCode.equalsIgnoreCase("deleteNode")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sId = cmdPacket.getStringValue(2);
				String sType = cmdPacket.getStringValue(3);
				String sIsVos = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.deleteNode(sSessId, sId, sType, sIsVos);

			} else if (sCmdCode.equalsIgnoreCase("addObjClassAndOp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sClass = cmdPacket.getStringValue(2);
				String sDescr = cmdPacket.getStringValue(3);
				String sInfo = cmdPacket.getStringValue(4);
				String sOp = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.addObjClassAndOp(sClientId, sClass, sDescr, sInfo, sOp);

			} else if (sCmdCode.equalsIgnoreCase("deleteObjClassAndOp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sClass = cmdPacket.getStringValue(2);
				String sOp = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.deleteObjClassAndOp(sSessId, sClass, sOp);

			} else if (sCmdCode.equalsIgnoreCase("getObjClasses")) {
				return ServerConfig.ADDAO.getObjClasses(sClientId);

			} else if (sCmdCode.equalsIgnoreCase("getObjects")) {
				return ServerConfig.ADDAO.getObjects(sClientId);

			} else if (sCmdCode.equalsIgnoreCase("getPmEntitiesOfClass")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sClass = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getPmEntitiesOfClass(sSessId, sClass);

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
				return ServerConfig.ADDAO.addObject3(sSessId, sProcId, sName, sDescr, sInfo,
						sContainers, sClass, sType, sHost, sPath, sOrigName,
						sOrigId, sInh, sSender, sRecip, sSubject, sAttached,
						null, null, null);

			} else if (sCmdCode.equalsIgnoreCase("deleteObject")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sObjId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.deleteObject(sSessId, sObjId);

			} else if (sCmdCode.equalsIgnoreCase("deleteObjectStrong")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sObjId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.deleteObjectStrong(sSessId, sObjId);

			} else if (sCmdCode.equalsIgnoreCase("deleteContainerObjects")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sContainerName = cmdPacket.getStringValue(2);
				String sContainerType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.deleteContainerObjects(sSessId, sContainerName, sContainerType);

			} else if (sCmdCode.equalsIgnoreCase("getAssocObj")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sOaId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getAssocObj(sClientId, sOaId);

			} else if (sCmdCode.equalsIgnoreCase("getObjNamePath")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sObjName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getObjNamePath(sObjName);

			} else if (sCmdCode.equalsIgnoreCase("getObjClassOps")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sClass = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getObjClassOps(sClientId, sClass);

			} else if (sCmdCode.equalsIgnoreCase("getHosts")) {
				return ServerConfig.ADDAO.getHosts(sClientId);

			} else if (sCmdCode.equalsIgnoreCase("addHost")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sName = cmdPacket.getStringValue(2);
				String sRepo = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.addHost(sSessId, sName, sRepo, null,null,null,null);

			} else if (sCmdCode.equalsIgnoreCase("updateHost")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sHostId = cmdPacket.getStringValue(2);
				String sHostName = cmdPacket.getStringValue(3);
				String sRepo = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.updateHost(sSessId, sHostId, sHostName, sRepo, null,
						null, null);

			} else if (sCmdCode.equalsIgnoreCase("deleteHost")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sHostId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.deleteHost(sSessId, sHostId);

			} else if (sCmdCode.equalsIgnoreCase("getHostInfo")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sHostId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getHostInfo(sSessId, sHostId);

			} else if (sCmdCode.equalsIgnoreCase("getOpsets")) {
				return ServerConfig.ADDAO.getOpsets(sClientId);

			} else if (sCmdCode.equalsIgnoreCase("addOpsetAndOp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sOpset = cmdPacket.getStringValue(2);
				String sDescr = cmdPacket.getStringValue(3);
				String sInfo = cmdPacket.getStringValue(4);
				String sOp = cmdPacket.getStringValue(5);
				String sBaseId = cmdPacket.getStringValue(6);
				String sBaseType = cmdPacket.getStringValue(7);
				return ServerConfig.ADDAO.addOpsetAndOp(sSessId, sOpset, sDescr, sInfo, sOp,
						sBaseId, sBaseType);

			} else if (sCmdCode.equalsIgnoreCase("getOpsetOps")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sOpset = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getOpsetOps(sClientId, sOpset);

			} else if (sCmdCode.equalsIgnoreCase("getOpsetInfo")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sOpsetId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getOpsetInfo(sClientId, sOpsetId);

			} else if (sCmdCode.equalsIgnoreCase("deleteOpsetAndOp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sOpsetId = cmdPacket.getStringValue(2);
				String sOp = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.deleteOpsetAndOp(sClientId, sOpsetId, sOp);

			} else if (sCmdCode.equalsIgnoreCase("createSession")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sName = cmdPacket.getStringValue(2);
				String sHost = cmdPacket.getStringValue(3);
				String sUser = cmdPacket.getStringValue(4);
				String sHash = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.createSession(sClientId, sName, sHost, sUser, sHash);

			} else if (sCmdCode.equalsIgnoreCase("spawnSession")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.spawnSession(sClientId, sCrtSessId);

			} else if (sCmdCode.equalsIgnoreCase("changePassword")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sUser = cmdPacket.getStringValue(2);
				String sOldPass = cmdPacket.getStringValue(3);
				String sNewPass = cmdPacket.getStringValue(4);
				String sConPass = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.changePassword(sClientId, sUser, sOldPass, sNewPass,
						sConPass);

			} else if (sCmdCode.equalsIgnoreCase("deleteSession")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.deleteSession(sClientId, sId);

			} else if (sCmdCode.equalsIgnoreCase("getPermittedOps")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sCrtProcId = cmdPacket.getStringValue(2);
				String sObjName = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getPermittedOps(sCrtSessId, sCrtProcId, sObjName);

			} else if (sCmdCode.equalsIgnoreCase("getObjEmailProps")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sObjName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getObjEmailProps(sCrtSessId, sObjName);

			} else if (sCmdCode.equalsIgnoreCase("getVosIdProperties")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sPresType = cmdPacket.getStringValue(2);
				String sVosId = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getVosIdProperties(sCrtSessId, sPresType, sVosId);

			} else if (sCmdCode.equalsIgnoreCase("getPosNodeProperties")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sPresType = cmdPacket.getStringValue(2);
				String sNodeId = cmdPacket.getStringValue(3);
				String sNodeLabel = cmdPacket.getStringValue(4);
				String sNodeType = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.getPosNodeProperties(sCrtSessId, sPresType, sNodeId,
						sNodeLabel, sNodeType);

			} else if (sCmdCode.equalsIgnoreCase("computeVos")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sPresType = cmdPacket.getStringValue(2);
				String sUserId = cmdPacket.getStringValue(3);
				String sSessId = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.computeVos(sClientId, sPresType, sUserId, sSessId);

			} else if (sCmdCode.equalsIgnoreCase("computeFastVos")) {
				System.out.println("computeFastVos cmd size = " + cmdPacket.size());
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sPresType = cmdPacket.getStringValue(2);
				String sUserId = cmdPacket.getStringValue(3);
				String sSessId = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.computeFastVos(sClientId, sPresType, sUserId, sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getSessions")) {
				return ServerConfig.ADDAO.getSessions(sClientId);

			} else if (sCmdCode.equalsIgnoreCase("getSessionEvents")) {
				return ServerConfig.ADDAO.getSessionEvents(sClientId);

			} else if (sCmdCode.equalsIgnoreCase("getSessionInfo")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sSessId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getSessionInfo(sClientId, sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getSessionUser")) {
				if (cmdPacket.size() < 3) {
					return failurePacket("Too few arguments!");
				}
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sSessId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getSessionUser(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getSessionName")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sSessId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getSessionName(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("testIsContained")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sId1 = cmdPacket.getStringValue(2);
				String sClass1 = cmdPacket.getStringValue(3);
				String sId2 = cmdPacket.getStringValue(4);
				String sClass2 = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.testIsContained(sId1, sClass1, sId2, sClass2);

				// Permissions on a PM entity seen as an object.
				// The entity is identified by its id and type.
			} else if (sCmdCode.equalsIgnoreCase("getUserDescendants")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sUserId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getUserDescendants(sClientId, sUserId);

			} else if (sCmdCode.equalsIgnoreCase("getMaximalSubsets")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sSetId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getMaximalSubsets(sClientId, sSetId);
			} else if (sCmdCode.equalsIgnoreCase("export")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.export(sClientId, sCrtSessId);

			} else if (sCmdCode.equalsIgnoreCase("interpretCmd")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sCmd = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.interpretCmd(sSessId, sCmd);

			} else if (sCmdCode.equalsIgnoreCase("importConfiguration")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.importConfiguration(sSessId, cmdPacket);

			} else if (sCmdCode.equalsIgnoreCase("getHostAppPaths")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sHost = cmdPacket.getStringValue(2);
				String appname = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getHostAppPaths(sClientId, sCrtSessId, sHost, appname);

			} else if (sCmdCode.equalsIgnoreCase("getInstalledAppNames")) {
				String sSessionId = cmdPacket.getStringValue(1);
				String sHost = cmdPacket.getStringValue(2);
				System.out.println("Calling getInstalledAppNames for " + sHost);
				return ServerConfig.ADDAO.getInstalledApps(sHost);
			} else if (sCmdCode.equalsIgnoreCase("getHostRepository")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getHostRepository(sCrtSessId);

			} else if (sCmdCode.equalsIgnoreCase(ADD_HOST_APP.commandCode())) {
				String sSessId = cmdPacket.getStringValue(1);
				String sHost = cmdPacket.getStringValue(2);
				String appName = cmdPacket.getStringValue(3);
				String appPath = cmdPacket.getStringValue(4);
				String mainClassName = cmdPacket.getStringValue(5);
				String appPrefix = cmdPacket.getStringValue(6);
				return ServerConfig.ADDAO.addHostApp(sSessId, sHost, appName, appPath, mainClassName, appPrefix);

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

				return ServerConfig.ADDAO.setHostAppPaths(sCrtSessId, sHost, sAtoolPath,
						sRtfedPath, sWkfPath, sEmlPath, sExpPath,
						sLauncherPath, sMsofficePath, sMedrecPath, sAcctrecPath, soldWkfPath, sSchemaPath);

			} else if (sCmdCode.equalsIgnoreCase("getKStorePaths")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getKStorePaths(sClientId, sSessId);

			} else if (sCmdCode.equalsIgnoreCase("setKStorePaths")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sUserId = cmdPacket.getStringValue(2);
				String sHost = cmdPacket.getStringValue(3);
				String sKsPath = cmdPacket.getStringValue(4);
				String sTsPath = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.setKStorePaths(sCrtSessId, sUserId, sHost, sKsPath,
						sTsPath);

				//			} else if (sCmdCode.equalsIgnoreCase("isTimeToRefresh")) {
				//				if (cmdPacket.size() < 3) {
				//					return failurePacket("Too few arguments!");
				//				}
				//				String sSessId = cmdPacket.getStringValue(1);
				//				String sClientTimestamp = cmdPacket.getStringValue(2);
				//				return ServerConfig.ADDAO.isTimeToRefresh(sClientId, sSessId, sClientTimestamp);

			} else if (sCmdCode.equalsIgnoreCase("getDenies")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getDenies(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getDenyInfo")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sDenyId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getDenyInfo(sSessId, sDenyId);

			} else if (sCmdCode.equalsIgnoreCase("getAllOps")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getAllOps(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getOattrs")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getOattrs(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getOconts")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getOattrs(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getObjAttrsProper")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getObjAttrsProper(sSessId);

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
				return ServerConfig.ADDAO.addDeny(sSessId, sDenyName, sDenyType, sUserOrAttrName,
						sUserOrAttrId, sOp, sOattrName, sOattrId, sIsInters);

			} else if (sCmdCode.equalsIgnoreCase("deleteDeny")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sDenyName = cmdPacket.getStringValue(2);
				String sOp = cmdPacket.getStringValue(3);
				String sOattrName = cmdPacket.getStringValue(4);
				String sOattrId = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.deleteDeny(sSessId, sDenyName, sOp, sOattrName, sOattrId);

			} else if (sCmdCode.equalsIgnoreCase("getIdOfEntityWithNameAndType")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sEntityName = cmdPacket.getStringValue(2);
				String sEntityType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getEntityId(sSessId, sEntityName, sEntityType);

			} else if (sCmdCode.equalsIgnoreCase("getNameOfEntityWithIdAndType")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sEntityId = cmdPacket.getStringValue(2);
				String sEntityType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getEntityName(sSessId, sEntityId, sEntityType);

			} else if (sCmdCode.equalsIgnoreCase("getScripts")) {
				return ServerConfig.ADDAO.getScripts();

			} else if (sCmdCode.equalsIgnoreCase("enableScript")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sScriptId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.enableScript(sSessId, sScriptId);

			} else if (sCmdCode.equalsIgnoreCase("disableEnabledScript")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.disableEnabledScript(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getEnabledScript")) {
				return ServerConfig.ADDAO.getEnabledScript();

			} else if (sCmdCode.equalsIgnoreCase("compileScript")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sDeleteOthers = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.compileScript(sSessId, sDeleteOthers, cmdPacket);

			} else if (sCmdCode.equalsIgnoreCase("compileScriptAndEnable")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.compileScriptAndEnable(sSessId, cmdPacket);

			} else if (sCmdCode.equalsIgnoreCase("compileScriptAndAddToEnabled")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.compileScriptAndAddToEnabled(sSessId, cmdPacket);

			} else if (sCmdCode.equalsIgnoreCase("addScript")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sScriptId1 = cmdPacket.getStringValue(2);
				String sScriptId2 = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.addScript(sSessId, sScriptId1, sScriptId2);

			} else if (sCmdCode.equalsIgnoreCase("addScriptToEnabled")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sScriptId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.addScriptToEnabled(sSessId, sScriptId);

			} else if (sCmdCode.equalsIgnoreCase("deleteScriptsWithNameExcept")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sScriptName = cmdPacket.getStringValue(2);
				String sScriptId = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.deleteScriptsWithNameExcept(sSessId, sScriptName,
						sScriptId);

			} else if (sCmdCode.equalsIgnoreCase("deleteScript")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sScriptId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.deleteScript(sSessId, sScriptId);

			} else if (sCmdCode.equalsIgnoreCase("getSourceScript")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sScriptId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getSourceScript(sSessId, sScriptId);

			} else if (sCmdCode.equalsIgnoreCase("deleteScriptRule")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sScriptId = cmdPacket.getStringValue(2);
				String sLabel = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.deleteScriptRule(sScriptId, sLabel);

			} else if (sCmdCode.equalsIgnoreCase("getAttrInfo")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sAttrId = cmdPacket.getStringValue(2);
				String sAttrType = cmdPacket.getStringValue(3);
				String sIsVos = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.getAttrInfo(sSessId, sAttrId, sAttrType, sIsVos);

			} else if (sCmdCode.equalsIgnoreCase("addProp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sId = cmdPacket.getStringValue(2);
				String sType = cmdPacket.getStringValue(3);
				String sIsVos = cmdPacket.getStringValue(4);
				String sProp = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.addProp(sSessId, sId, sType, sIsVos, sProp);

			} else if (sCmdCode.equalsIgnoreCase("replaceProp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sId = cmdPacket.getStringValue(2);
				String sType = cmdPacket.getStringValue(3);
				String sIsVos = cmdPacket.getStringValue(4);
				String sOldProp = cmdPacket.getStringValue(5);
				String sNewProp = cmdPacket.getStringValue(6);
				return ServerConfig.ADDAO.replaceProp(sSessId, sId, sType, sIsVos, sOldProp,
						sNewProp);

			} else if (sCmdCode.equalsIgnoreCase("removeProp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sId = cmdPacket.getStringValue(2);
				String sType = cmdPacket.getStringValue(3);
				String sIsVos = cmdPacket.getStringValue(4);
				String sProp = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.removeProp(sSessId, sId, sType, sIsVos, sProp);

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
				return ServerConfig.ADDAO.createObject3(sSessId, sProcId, sObjName, sObjClass,
						sObjType, sContainers, sPerms, sSender, sReceiver,
						sSubject, sAttached);

			} else if (sCmdCode.equalsIgnoreCase("processEvent")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sEventName = cmdPacket.getStringValue(3);
				String sObjName = cmdPacket.getStringValue(4);
				String sObjId = cmdPacket.getStringValue(5);
				String sObjClass = cmdPacket.getStringValue(6);
				String sObjType = cmdPacket.getStringValue(7);
				String sCtx1 = cmdPacket.getStringValue(8);
				String sCtx2 = cmdPacket.getStringValue(9);
				return ServerConfig.ADDAO.processEvent(sSessId, sProcId, sEventName, sObjName,
						sObjId, sObjClass, sObjType, sCtx1, sCtx2);

			} else if (sCmdCode.equalsIgnoreCase("getUserInfo")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sUserId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getUserInfo(sSessId, sUserId);

			} else if (sCmdCode.equalsIgnoreCase("getOpsetsBetween")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sUattrName = cmdPacket.getStringValue(2);
				String sEntityName = cmdPacket.getStringValue(3);
				String sEntityType = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.getOpsetsBetween(sSessId, sUattrName, sEntityName,
						sEntityType);

			} else if (sCmdCode.equalsIgnoreCase("deleteOpsetsBetween")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sUattrName = cmdPacket.getStringValue(3);
				String sOattrName = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.deleteOpsetsBetween(sSessId, sProcId, sUattrName,
						sOattrName);

			} else if (sCmdCode.equalsIgnoreCase("getOpsetOattrs")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sOpsetName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getOpsetOattrs(sSessId, sOpsetName);

			} else if (sCmdCode.equalsIgnoreCase("isolateOattr")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sAttrName = cmdPacket.getStringValue(2);
				String sAttrType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.isolateOattr(sAttrName, sAttrType);

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
				return ServerConfig.ADDAO.setPerms(sSessId, sProcId, sUattrName, sOpset,
						sSuggOattr, sSuggBase, sSuggBaseType, sPerms, sEntName,
						sEntType, sInclAscs);

			} else if (sCmdCode.equalsIgnoreCase("getEntityWithProp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sEntType = cmdPacket.getStringValue(2);
				String sProp = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getEntityWithProp(sSessId, sEntType, sProp);

			} else if (sCmdCode.equalsIgnoreCase("doDacConfinement")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sUser = cmdPacket.getStringValue(2);
				String sPc = cmdPacket.getStringValue(3);
				String sAttr = cmdPacket.getStringValue(4);
				String sCont = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.doDacConfinement(sSessId, sUser, sPc, sAttr, sCont);

			} else if (sCmdCode.equalsIgnoreCase("testGetMemberObjects")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sContId = cmdPacket.getStringValue(2);
				String sContType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.testGetMemberObjects(sSessId, sContId, sContType);

			} else if (sCmdCode.equalsIgnoreCase("testGetPmViews")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sUserId = cmdPacket.getStringValue(2);
				String sType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.testGetPmViews(sSessId, sUserId, sType);

			} else if (sCmdCode.equalsIgnoreCase("isAssigned")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sId1 = cmdPacket.getStringValue(2);
				String sType1 = cmdPacket.getStringValue(3);
				String sId2 = cmdPacket.getStringValue(4);
				String sType2 = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.isAssigned(sId1, sType1, sId2, sType2);

			} else if (sCmdCode.equalsIgnoreCase("getIdOfEntityWithNameAndType")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sName = cmdPacket.getStringValue(2);
				String sType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getEntityId(sSessId, sName, sType);

			} else if (sCmdCode.equalsIgnoreCase("getNameOfEntityWithIdAndType")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sId = cmdPacket.getStringValue(2);
				String sType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getEntityName(sSessId, sId, sType);

			} else if (sCmdCode.equalsIgnoreCase("testGetDeniedPerms")) {
				String sCrtSessId = cmdPacket.getStringValue(1);
				String sSessId = cmdPacket.getStringValue(2);
				String sObjName = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.testGetDeniedPerms(sSessId, sObjName);

			} else if (sCmdCode.equalsIgnoreCase("genDac")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPolName = cmdPacket.getStringValue(2);
				String sContName = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.genDac(sSessId, sPolName, sContName);

			} else if (sCmdCode.equalsIgnoreCase("genMls")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPolName = cmdPacket.getStringValue(2);
				String sLevels = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.genMls(sSessId, sPolName, sLevels);

			} else if (sCmdCode.equalsIgnoreCase("genConfForDacUser")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sUser = cmdPacket.getStringValue(2);
				String sPc = cmdPacket.getStringValue(3);
				String sAttr = cmdPacket.getStringValue(4);
				String sCont = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.genConfForDacUser(sSessId, sUser, sPc, sAttr, sCont);

			} else if (sCmdCode.equalsIgnoreCase("genDacUserWithConf")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sUser = cmdPacket.getStringValue(2);
				String sFullName = cmdPacket.getStringValue(3);
				String sPc = cmdPacket.getStringValue(4);
				String sAttr = cmdPacket.getStringValue(5);
				String sCont = cmdPacket.getStringValue(6);
				return ServerConfig.ADDAO.genDacUserWithConf(sSessId, sUser, sFullName, sPc,
						sAttr, sCont);

			} else if (sCmdCode.equalsIgnoreCase("genIbac")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPolName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.genIbac(sSessId, sPolName);

			} else if (sCmdCode.equalsIgnoreCase("addEmailAcct")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPmUser = cmdPacket.getStringValue(2);
				String sFullName = cmdPacket.getStringValue(3);
				String sEmailAddr = cmdPacket.getStringValue(4);
				String sPopServer = cmdPacket.getStringValue(5);
				String sSmtpServer = cmdPacket.getStringValue(6);
				String sAcctName = cmdPacket.getStringValue(7);
				String sPassword = cmdPacket.getStringValue(8);
				return ServerConfig.ADDAO.addEmailAcct(sSessId, sPmUser, sFullName, sEmailAddr,
						sPopServer, sSmtpServer, sAcctName, sPassword);

			} else if (sCmdCode.equalsIgnoreCase("getEmailAcct")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPmUser = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getEmailAcct(sSessId, sPmUser);

			} else if (sCmdCode.equalsIgnoreCase("sendSimpleMsg")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sMsgName = cmdPacket.getStringValue(2);
				String sReceiver = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.sendSimpleMsg(sSessId, sMsgName, sReceiver);

			} else if (sCmdCode.equalsIgnoreCase("copyObject")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sObjName = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.copyObject(sSessId, sProcId, sObjName);

			} else if (sCmdCode.equalsIgnoreCase("sendObject")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sObjName = cmdPacket.getStringValue(2);
				String sReceiver = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.sendObject(sSessId, sObjName, sReceiver);

			} else if (sCmdCode.equalsIgnoreCase("getEmailRecipients")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sReceiver = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getEmailRecipients(sSessId, sReceiver);

			} else if (sCmdCode.equalsIgnoreCase("getUsersAndAttrs")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getUsersAndAttrs(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("addOpenObj")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sObjName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.addOpenObj(sSessId, sObjName);

			} else if (sCmdCode.equalsIgnoreCase("deleteOpenObj")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sObjName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.deleteOpenObj(sSessId, sObjName);

			} else if (sCmdCode.equalsIgnoreCase("assignObjToOattrWithProp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sObjName = cmdPacket.getStringValue(3);
				String sProp = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.assignObjToOattrWithProp(sSessId, sProcId, sObjName,
						sProp);

			} else if (sCmdCode.equalsIgnoreCase("assignObjToOattr")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sObjName = cmdPacket.getStringValue(3);
				String sOattrName = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.assignObjToOattr(sSessId, sProcId, sObjName, sOattrName);

			} else if (sCmdCode.equalsIgnoreCase("deassignObjFromOattrWithProp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sObjName = cmdPacket.getStringValue(3);
				String sProp = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.deassignObjFromOattrWithProp(sSessId, sProcId, sObjName,
						sProp);

			} else if (sCmdCode.equalsIgnoreCase("isObjInOattrWithProp")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sObjName = cmdPacket.getStringValue(2);
				String sProp = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.isObjInOattrWithProp(sSessId, sObjName, sProp);

			} else if (sCmdCode.equalsIgnoreCase("getInboxMessages")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getInboxMessages(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getOutboxMessages")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getOutboxMessages(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getFileContent")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sFileProp = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getFileContent(sSessId, sFileProp, bisFromClient,
						bosToClient);

			} else if (sCmdCode.equalsIgnoreCase("createObjForFile")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sFilePath = cmdPacket.getStringValue(2);
				String sContName = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.createObjForFile(sSessId, sFilePath, sContName);

			} else if (sCmdCode.equalsIgnoreCase("createContForFolder")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sFolderPath = cmdPacket.getStringValue(2);
				String sBaseContName = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.createContForFolder(sSessId, sFolderPath, sBaseContName);

			} else if (sCmdCode.equalsIgnoreCase("getDascUattrs")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sBaseName = cmdPacket.getStringValue(2);
				String sBaseType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getDascUattrs(sBaseName, sBaseType);

			} else if (sCmdCode.equalsIgnoreCase("getDascUsers")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sBaseName = cmdPacket.getStringValue(2);
				String sBaseType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getDascUsers(sBaseName, sBaseType);

			} else if (sCmdCode.equalsIgnoreCase("getDascOattrs")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sBaseName = cmdPacket.getStringValue(2);
				String sBaseType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getDascOattrs(sBaseName, sBaseType);

			} else if (sCmdCode.equalsIgnoreCase("getDascObjects")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sBaseName = cmdPacket.getStringValue(2);
				String sBaseType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getDascObjects(sBaseName, sBaseType);

			} else if (sCmdCode.equalsIgnoreCase("getDascOpsets")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sBaseName = cmdPacket.getStringValue(2);
				String sBaseType = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getDascOpsets(sBaseName, sBaseType);

			} else if (sCmdCode.equalsIgnoreCase("getContainersOf")) {
				log.debug("TRACE 4 - In PacketHandler.getContainersOf() - Got getContainersOf");

				for (int i = 0; i < cmdPacket.size(); i++) {
					System.out.println("**** CMDPACKET FOR getContainersOf (" + i + "): " 
							+ cmdPacket.getItemStringValue(i));
				}
				String sSessId = cmdPacket.getStringValue(1);
				String sBaseName = cmdPacket.getStringValue(2);
				String sBaseId = cmdPacket.getStringValue(3);
				String sBaseType = cmdPacket.getStringValue(4);
				String sGraphType = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.getContainersOf(sSessId, sBaseName, sBaseId, sBaseType,
						sGraphType);

			} else if (sCmdCode.equalsIgnoreCase("getMellContainersOf")) {
				log.debug("TRACE 4 - In PacketHandler.getMellContainersOf()");
				for (int i = 0; i < cmdPacket.size(); i++) {
					System.out.println("**** CMDPACKET FOR getMellContainersOf (" + i + "): " 
							+ cmdPacket.getItemStringValue(i));
				}
				String sSessId = cmdPacket.getStringValue(1);
				String sBaseName = cmdPacket.getStringValue(2);
				String sBaseId = cmdPacket.getStringValue(3);
				String sBaseType = cmdPacket.getStringValue(4);
				String sGraphType = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.getMellContainersOf(sSessId, sBaseName, sBaseId, sBaseType,
						sGraphType);

			} else if (sCmdCode.equalsIgnoreCase("getMembersOf")) {
				log.debug("TRACE 4 - In PacketHandler.executeCommand() - Got getMembersOf");

				for (int i = 0; i < cmdPacket.size(); i++) {
					System.out.println("**** CMDPACKET FOR getMembersOf (" + i + "): " 
							+ cmdPacket.getItemStringValue(i));
				}
				String sSessId = cmdPacket.getStringValue(1);
				String sBaseName = cmdPacket.getStringValue(2);
				String sBaseId = cmdPacket.getStringValue(3);
				String sBaseType = cmdPacket.getStringValue(4);
				String sGraphType = cmdPacket.getStringValue(5);

				return ServerConfig.ADDAO.getMembersOf(sSessId, sBaseName, sBaseId, sBaseType, sGraphType);

			} else if (sCmdCode.equalsIgnoreCase("getMellMembersOf")) {
				log.debug("TRACE 4 - In PacketHandler.executeCommand() - Got getMellMembersOf");
				for (int i = 0; i < cmdPacket.size(); i++) {
					System.out.println("**** CMDPACKET FOR getMellMembersOf (" + i + "): " 
							+ cmdPacket.getItemStringValue(i));
				}
				String sSessId = cmdPacket.getStringValue(1);
				String sBaseName = cmdPacket.getStringValue(2);
				String sBaseId = cmdPacket.getStringValue(3);
				String sBaseType = cmdPacket.getStringValue(4);
				String sGraphType = cmdPacket.getStringValue(5);
				return ServerConfig.ADDAO.getMellMembersOf(sSessId, sBaseName, sBaseId, sBaseType, sGraphType);

			} else if (sCmdCode.equalsIgnoreCase("getConnector")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getConnector();

			} else if (sCmdCode.equalsIgnoreCase("addTemplate")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sTplName = cmdPacket.getStringValue(2);
				String sContainers = cmdPacket.getStringValue(3);
				String sKeys = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.addTemplate(sSessId, sTplName, sContainers, sKeys);

			} else if (sCmdCode.equalsIgnoreCase("getTemplates")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getTemplates(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("getTemplateInfo")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sTplId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getTemplateInfo(sSessId, sTplId);

			} else if (sCmdCode.equalsIgnoreCase("createRecord")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sProcId = cmdPacket.getStringValue(2);
				String sRecordName = cmdPacket.getStringValue(3);
				String sBase = cmdPacket.getStringValue(4);
				String sBaseType = cmdPacket.getStringValue(5);
				String sTplId = cmdPacket.getStringValue(6);
				String sComponents = cmdPacket.getStringValue(7);
				String[] sKeys = getItemsFromPacket(cmdPacket, 8);
				return ServerConfig.ADDAO.createRecord(sSessId, sProcId, sRecordName, sBase,
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
				return ServerConfig.ADDAO.createRecordInEntityWithProp(sSessId, sProcId,
						sRecordName, sProp, sBaseType, sTplId, sComponents,
						sKeys);

			} else if (sCmdCode.equalsIgnoreCase("getRecords")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sTplId = cmdPacket.getStringValue(2);
				String sKey = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.getRecords(sSessId, sTplId, sKey);

			} else if (sCmdCode.equalsIgnoreCase("getRecordInfo")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sOattrId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getRecordInfo(sSessId, sOattrId);

			} else if (sCmdCode.equalsIgnoreCase("getObjInfo")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sObjId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getObjInfo(sObjId);

			} else if (sCmdCode.equalsIgnoreCase("getObjProperties")){
				String sSessId = cmdPacket.getStringValue(1);
				String sObjId = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getObjProperties(sObjId);
				//			} else if (sCmdCode.equalsIgnoreCase("isInPos")) {
				//				if (cmdPacket.size() < 3) {
				//					return failurePacket("Too few arguments!");
				//				}
				//				String sSessId = cmdPacket.getStringValue(1);
				//				String sObjId = cmdPacket.getStringValue(2);
				//				return ServerConfig.ADDAO.isInPos(sSessId, sObjId);

			} else if (sCmdCode.equalsIgnoreCase("testExportRecords")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.testExportRecords();

			} else if (sCmdCode.equalsIgnoreCase("getUsersOf")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sUattrName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getUsersOf(sUattrName);

			} else if (sCmdCode.equalsIgnoreCase("setRecordKeys")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sRecName = cmdPacket.getStringValue(2);
				String[] sKeys = getItemsFromPacket(cmdPacket, 3);
				return ServerConfig.ADDAO.setRecordKeys(sSessId, sRecName, sKeys);

			} else if (sCmdCode.equalsIgnoreCase("addRecordKeys")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sRecName = cmdPacket.getStringValue(2);
				String[] sKeys = getItemsFromPacket(cmdPacket, 3);
				return ServerConfig.ADDAO.addRecordKeys(sSessId, sRecName, sKeys);

			} else if (sCmdCode.equalsIgnoreCase("getProperties")) {
				String sSessId = cmdPacket.getStringValue(1);
				return ServerConfig.ADDAO.getProperties(sSessId);

			} else if (sCmdCode.equalsIgnoreCase("deleteProperty")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPropName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.deleteProperty(sSessId, sPropName);

			} else if (sCmdCode.equalsIgnoreCase("setProperty")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPropName = cmdPacket.getStringValue(2);
				String sPropValue = cmdPacket.getStringValue(3);
				return ServerConfig.ADDAO.setProperty(sSessId, sPropName, sPropValue);

			} else if (sCmdCode.equalsIgnoreCase("getPropertyValue")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPropName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getPropertyValue(sSessId, sPropName);

			} else if (sCmdCode.equalsIgnoreCase("getProperty")) {
				String sSessId = cmdPacket.getStringValue(1);
				String sPropName = cmdPacket.getStringValue(2);
				return ServerConfig.ADDAO.getProperty(sSessId, sPropName);

			}else if (sCmdCode.equalsIgnoreCase("audit")) {
				//NDK Added this
				String sSessId = cmdPacket.getStringValue(1);
				String sEvent = cmdPacket.getStringValue(2);
				String sObjId = cmdPacket.getStringValue(3);   
				String sResult = cmdPacket.getStringValue(4);
				return ServerConfig.ADDAO.audit(sSessId, sEvent, sObjId, sResult);
			}else {
				System.out.println(sCmdCode);
				System.in.read();
				return failurePacket("Unknown command 2");
			}  
		} catch (Exception e) {
			return failurePacket(e.getMessage());
		}
	}

}

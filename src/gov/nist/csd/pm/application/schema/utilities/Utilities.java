package gov.nist.csd.pm.application.schema.utilities;

import com.google.common.base.Throwables;
import gov.nist.csd.pm.admin.PmAdmin;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.PmNode;
import gov.nist.csd.pm.common.browser.PmNodeType;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.model.ObjectAttribute;
import gov.nist.csd.pm.common.model.ObjectAttributes;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;

import javax.swing.*;
import java.util.*;

import static gov.nist.csd.pm.common.constants.GlobalConstants.PM_DEFAULT_SERVER_PORT;
import static gov.nist.csd.pm.common.constants.GlobalConstants.PM_DEFAULT_SIMULATOR_PORT;
import static gov.nist.csd.pm.common.info.PMCommand.*;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;

/**
 * This is a Utilities class to be used with Schema Builder and like classes
 *
 * @author Josh Roberts
 *
 */
public class Utilities {
	public SSLSocketClient sslClient;
	private SysCaller sysCaller;

	private int nSimulatorPort;
	private String sSessionId;
	private String sProcessId;
	private SSLSocketClient engineClient;

	public Utilities(String sessId, String sProc, int nSimPort, boolean bDebug){
		sSessionId = sessId;
		sProcessId = sProc;
		nSimulatorPort = (nSimPort < 1024) ? PM_DEFAULT_SIMULATOR_PORT  : nSimPort;
		try {
			engineClient = new SSLSocketClient("localhost", PM_DEFAULT_SERVER_PORT,
					bDebug, "c,UTIL");
		} catch (Exception e) {
			System.out.println("error creating engine client");
			e.printStackTrace();
			System.exit(-1);
		}

		/*try {
			sslClient = new SSLSocketClient("localhost", nSimulatorPort, bDebug, "SB");
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null,
					"Unable to create SSL socket to talk to the KSim of the local host!");
			e1.printStackTrace();
		}*/

		if (!connectToEngine()) {
			JOptionPane.showMessageDialog(null,
					"Unable to connect to the PM engine.");
			System.exit(-2);
		}
		sysCaller = new SysCallerImpl(nSimulatorPort, sSessionId, sProcessId, bDebug, "U");
        sslClient = sysCaller.getSocketClient();
    }

    public SysCaller getSysCaller(){
        return sysCaller;
    }

	private boolean connectToEngine(){
		try {
			Packet cmd = makeCmd("connect", new String[]{});
			Packet res = engineClient.sendReceive(cmd, null);
			if (res.hasError()) {
				log("Connect: " + res.getErrorMessage());
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log("Connect: " + e.getMessage());
			return false;
		}
	}

	public boolean addUattr(PmNode baseNode, String sName, String sDescr,
			String sInfo, String[] props){
		Packet res = null;
		try {
			// The second argument, which is null, is the process id.
			String baseNodeId = baseNode == null ? "" : baseNode.getId();
			String baseNodeType = baseNode == null ? "" : baseNode.getType();
			Packet cmd = makeCmd("addUattr", null, sName, sDescr, sInfo,
					baseNodeId, baseNodeType, "no");
			int n = props.length;
			for (int i = 0; i < n; i++) {
				cmd.addItem(ItemType.CMD_ARG, props[i]);
			}
			res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				System.out.println("res == null");
				return false;
			}
			if (res.hasError()) {
				System.out.println(res.getErrorMessage());
				return false;
			}
			System.out.println("added user attribute");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isRecord(String sId){
		Packet res = null;
		try {
			Packet cmd = makeCmd("isRecordPacket", sId);
			res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				System.out.println("res == null");
				return false;
			}
			if (res.hasError()) {
				System.out.println(res.getErrorMessage());
				return false;
			}
			if(res.getStringValue(0).equalsIgnoreCase("yes"))return true;
			else return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean assignUattrToPolicy(String sId1, String sId2){
		Packet res = null;
		try {
			Packet cmd = makeCmd("assignUattrToPolicy", null, sId1, sId2);
			res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				System.out.println("res == null");
				return false;
			}
			if (res.hasError()) {
				System.out.println(res.getErrorMessage());
				return false;
			}
			System.out.println("added user to user attribute");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean assignUattrToUattr(String sId1, String sId2){
		Packet res = null;
		try {
			Packet cmd = makeCmd("assignUattrToUattr", null, sId1, sId2);
			res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				System.out.println("res == null");
				return false;
			}
			if (res.hasError()) {
				System.out.println(res.getErrorMessage());
				return false;
			}
			System.out.println("added user to user attribute");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean assignUserToUattr(String sId1, String sId2){
		Packet res = null;
		try {
			Packet cmd = makeCmd("assignUserToUattr", null, sId1, sId2);
			res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				System.out.println("res == null");
				return false;
			}
			if (res.hasError()) {
				System.out.println(res.getErrorMessage());
				return false;
			}
			System.out.println("added user to user attribute");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean assign(String sId1, String sType1, String sId2, String sType2){
		Packet res = null;
		try {
			Packet cmd = makeCmd("assign", null, sId1, sType1, sId2, sType2, "no");
			res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				System.out.println("res == null");
				return false;
			}
			if (res.hasError()) {
				System.out.println(res.getErrorMessage());
				return false;
			}
			System.out.println("assignment successful");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Vector getMembersOf(String sType, String sId, String sLabel) {
		Packet res = null;
		try {
			Packet cmd = makeCmd("getMembersOf", sSessionId, sLabel, sId, sType,
					"oa");
			// res = sslClient.sendReceive(cmd, null);
			res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		Vector v = new Vector();
		if (res != null)
			for (int i = 0; i < res.size(); i++) {
				String sLine = res.getStringValue(i);
				String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
				v.add(pieces);
			}
		return v;
	}


	public List<String[]> getMembersOf(String sType, String sId, String sLabel, String sGraphType) {
		Packet res = null;
		try {
			Packet cmd = makeCmd("getMembersOf", sLabel, sId, sType, sGraphType);
			res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(null, "Undetermined error; null result returned");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(null, res.getErrorMessage());
				return null;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
			return null;
		}

		List<String[]> v = new ArrayList<String[]>();
		if (res != null) {
			for (int i = 0; i < res.size(); i++) {
				String sLine = res.getStringValue(i);
				String[] pieces = sLine.split(PmAdmin.PM_FIELD_DELIM);
				v.add(new String[]{pieces[0], pieces[1], pieces[2]});
			}
		}
		return v;
	}

	public String getNameOfEntity(String id, String type){
		try{
			Packet cmd = makeCmd("getNameOfEntityWithIdAndType", id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(null, "Undetermined error; null result returned");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(null, res.getErrorMessage());
				return null;
			}
			return res.getStringValue(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public Packet getTemplateInfo(String sTplId) {
		try {
			Packet cmd = makeCmd(GET_TEMPLATE_INFO, sTplId);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(null, res.getErrorMessage());
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Packet getTemplates(){
		try {
			Packet cmd = makeCmd(GET_TEMPLATES);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(null, res.getErrorMessage());
				return null;
			}
			return res;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "exception in getTemplates");
			return null;
		}
	}

	public String getUserId(){
		try{
			Packet cmd = makeCmd("getSessionName", sSessionId);
			//Packet cmd = makeCmd("getUsersAndAttrs", sessionId);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(null, "Undetermined error; null result returned");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(null, res.getErrorMessage());
				return null;
			}
			//String[] pieces =  res.getStringValue(0).split(":");
			//String userId = pieces[1];
			String sName = res.getStringValue(0);
			sName = sName.substring(0, sName.indexOf("@"));
			//fw.append("user name: " + userName);
			String id = sysCaller.getIdOfEntityWithNameAndType(sName, PM_NODE.USER.value);
			return sName + ":" + id;
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "Could not find user id");
			e.printStackTrace();
			return null;
		}
	}

	/*public ArrayList<String> getUsers() {
		Packet res;
		try {
			Packet cmd = makeCmd("getUsers");
			res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
		ArrayList<String> ret = new ArrayList<String>();
		for(int i = 0; i < res.size(); i++){
			ret.add(res.getStringValue(i));
		}
		return ret;
	}*/

	public ArrayList<String> getPerms(String id, String type){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getPermittedOpsOnEntity", sProcessId, id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(null, "Undetermined error; null result returned");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(null, res.getErrorMessage());
				return null;
			}
			log("RESSIZE: " + res.size());
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
				log("ret: " + ret.get(i));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
			return null;
		}
		return ret;
	}

	public void log(Object input){
		System.out.println(input);
	}

	public Packet genCmd(String sCmdCode, String... sArgs){
		Packet res;
		try {
			Packet cmd = makeCmd(sCmdCode, sArgs);
			res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
		return res;
	}

	/**
	 * @param cmd
	 * @param sArgs
	 * @return
	 */
	public Packet makeCmd(PMCommand cmd, String... sArgs){
		try {
			return makeCmd(cmd.commandCode(), sArgs);
		} catch (Exception e) {
			Throwables.propagate(e);
		}
		return failurePacket();
	}

	/**
	 * @param sCode
	 * @param sArgs
	 * @return
	 * @throws Exception
	 */
	public Packet makeCmd(String sCode, String... sArgs) throws Exception {
		return CommandUtil.makeCmd(sCode, sSessionId == null ? "" : sSessionId, sArgs);
	}



	/**
	 * @return
	 */
	public String getCrtUser(){
		String line = getUserId();
		log(line);
		String[] pieces = line.split(":");
		//String userId = pieces[1];
		//String name = getUserFullName(userId);
		String name = pieces[0];
		log(name);
		return name;
	}

	///////////////////Methods in Schema Builder////////////////////////////////////////////////////////////////////////////////////////////////
	//TODO
	/*public ArrayList<String> getAttrInfo(String sId) {
		try{
			Packet cmd = makeCmd("getAttrInfo", sId, PM_NODE.OATTR.value, "no");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			ArrayList<String> ret = new ArrayList<String>();
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
			return ret;
		} catch (Exception e) {
			return null;
		}
	}

	public String getContainers(String id, String type){
		try{
			Packet cmd = makeCmd("getContainers", id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(null, "Undetermined error; null result returned");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(null, res.getErrorMessage());
				return null;
			}
			return res.getItemStringValue(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
			return null;
		}
	}*/

	public ArrayList<String> getAllOattrs(String base){
		ArrayList<String> oattrs = new ArrayList<String>();
		String id = sysCaller.getIdOfEntityWithNameAndType(base, PM_NODE.OATTR.value);
		List<String[]> members = getMembersOf(PM_NODE.OATTR.value, id, base, GlobalConstants.PM_GRAPH_OATTR);//TODO might be PM_GRAPH_ACES
		Iterator<String[]> it = members.iterator();
		while(it.hasNext()){
			String[] mem = it.next();
			String type = mem[0];
			String name = mem[2];
			if(type.equals(PM_NODE.OATTR.value)){
				oattrs.add(name);
			}else{
				continue;
			}
			if(it.hasNext()) getAllOattrs(it.next()[2]);
		}
		log("OATTRS: " + oattrs);
		return oattrs;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getAssocOattr(String id){
		try{
			Packet cmd = makeCmd("getAssocOattr1", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			return res.getStringValue(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param sId
	 * @return
	 */
	public ArrayList<String> getAttrInfo(String sId, String type) {
		try{
			Packet cmd = makeCmd("getAttrInfo", sId, type, "no");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			ArrayList<String> ret = new ArrayList<String>();
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getContainers(String id, String type){
		try{
			Packet cmd = makeCmd("getContainers", id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			return res.getItemStringValue(0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param sName name of the template
	 * @param containers list of containers to add to the template
	 * @param keys keys for the template
	 * @return
	 */
	public Packet addTemplate(String sName, List<String> containers, List<String> keys) {
		try {
			Packet cmd = makeCmd(ADD_TEMPLATE, sName);
			StringBuffer sb = new StringBuffer();
			String s;
			for (int i = 0; i < containers.size(); i++) {
				if (i == 0) {
					sb.append(containers.get(i));
				} else {
					sb.append(GlobalConstants.PM_FIELD_DELIM);
					sb.append(containers.get(i));
				}
			}
			s = sb.toString();
			log("Containers: " + s);
			if (s.length() > 0) {
				cmd.addItem(ItemType.CMD_ARG, s);
			} else {
				cmd.addItem(ItemType.CMD_ARG, "");
			}

			sb = new StringBuffer();
			for (int i = 0; i < keys.size(); i++) {
				if (i == 0) {
					sb.append(keys.get(i));
				} else {
					sb.append(GlobalConstants.PM_FIELD_DELIM);
					sb.append(keys.get(i));
				}
			}
			s = sb.toString();
			log("Keys: " + s);
			if (s.length() > 0) {
				cmd.addItem(ItemType.CMD_ARG, s);
			} else {
				cmd.addItem(ItemType.CMD_ARG, "");
			}
			Packet res = engineClient.sendReceive(cmd, null);
			if (res.hasError()) {
				return null;
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public ArrayList<String> getFromAttrs(String id){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getFromAttrs1", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;

	}

	/**
	 * @param id
	 * @return
	 *//*
	public ArrayList<String> getFromAttrs(String id){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getFromAttrs1", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}*/

	/**
	 * @param id
	 * @return
	 */
	public ArrayList<String> getFromOpsets(String id){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getFromOpsets1", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}

	/**
	 * @param sTplId
	 * @return
	 *//*
	public Packet getTemplateInfo(String sTplId) {
		try {
			Packet cmd = makeCmd(GET_TEMPLATE_INFO, sTplId);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}*/

	/**
	 * @param id
	 * @return
	 */
	public ArrayList<String> getToAttrs(String id){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getToAttrs1", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;

	}

	/**
	 * @param id1
	 * @param type1
	 * @param id2
	 * @param type2
	 * @return
	 */
	public boolean deleteAssignment(String id1, String type1, String id2, String type2){
		try{
			Packet cmd = makeCmd("deleteAssignment", null, id1, type1, id2, type2, "no");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return false;
			}
			if (res.hasError()) {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param sTableName
	 * @return
	 */
	public boolean deleteObjectFromTable(String sTableName, String type){
		try{
			Packet cmd = makeCmd("deleteContainerObjects", sTableName, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return false;
			}
			if (res.hasError()) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param baseId
	 * @param baseType
	 */
	public void deleteNode(String baseId, String baseType){
		try{
			Packet cmd = makeCmd("deleteNode", baseId, baseType, "no");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return;
			}
			if (res.hasError()) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * @param id
	 * @param type
	 * @return
	 */
	public ArrayList<String> getUatts(String id, String type){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getToAttrsUser");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
				log("UA: " + ret.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}

	public ArrayList<String> getFromUserAttrs(String id, String type){
		ArrayList<String> uattrs = new ArrayList<String>();
		Packet res = genCmd("getFromUserAttrsPacket", sSessionId, id, type);
		try{
			for(int i = 0; i < res.size(); i++){
				uattrs.add(res.getStringValue(i));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(uattrs);
		return uattrs;
	}

	/**
	 * @return
	 *//*
	public String getUserId(){
		try{
			Packet cmd = util.makeCmd("getSessionName", sessionId);
			//Packet cmd = util.makeCmd("getUsersAndAttrs", sessionId);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return null;
			}
			//String[] pieces =  res.getStringValue(0).split(":");
			//String userId = pieces[1];
			String sName = res.getStringValue(0);
			sName = sName.substring(0, sName.indexOf("@"));
			//fw.append("user name: " + userName);
			String id = sysCaller.getIdOfEntityWithNameAndType(sName, PM_NODE.USER.value);
			return sName + ":" + id;
		}catch(Exception e){
			JOptionPane.showMessageDialog(this, "Could not find user id");
			e.printStackTrace();
			return null;
		}
	}*/

	/**
	 * @param tplId
	 * @param tplName
	 * @return
	 */
	public Packet doDeleteTemplate(String tplId, String tplName){
		log("deleting template with id " + tplId);
		try{
			Packet cmd = makeCmd("deleteTemplate", sSessionId, tplId, tplName);
			Packet res = engineClient.sendReceive(cmd, null);
			if(res.hasError()){
				return null;
			}
			//log(res.getStringValue(0));
			return res;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return
	 *//*
	public Packet getTemplates(){
		try {
			Packet cmd = util.makeCmd(GET_TEMPLATES);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				return null;
			}
			return res;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "exception in getTemplates");
			return null;
		}
	}*/

	public boolean createBaseOattr(String polType, String oaType, String name, String descr, String other, String props){
		//setUserName();
		Packet res = null;
		try {
			Packet cmd = makeCmd("createSchemaPC", null, polType, oaType, name, name + " Admins");
			log("SCHEMA NAME:" + name);
			cmd.addItem(ItemType.CMD_ARG, "");
			String[] pieces = props.split(", ");
			for(int i = 0; i < pieces.length; i++){
				cmd.addItem(ItemType.CMD_ARG, pieces[i]);
			}
			res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				System.out.println("res is null in createbaseoattr");
				return false;
			}
			if (res.hasError()) {
				System.out.println("res has error in createbaseoattr: " + res.getErrorMessage());
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean createOattr(String oattrType, String name, String baseName, String type){
		try{
			Packet cmd = makeCmd("addSchemaOattr", null, oattrType, name, baseName, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				System.out.println("RES IS NULL!!!!!!!!!!!!!!!!");
				return false;
			}
			if (res.hasError()) {
				System.out.println(res.getErrorMessage());
				System.out.println("RES HAS ERROR!!!!!!!!!!!!!!!!");
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}

	public boolean addOattr(String name, String baseId, String baseType, String descr, String other, String prop){
		try{
			Packet cmd = makeCmd(PMCommand.ADD_OATTR, null, name, descr, other,
					baseId, baseType, "no");
			cmd.addItem(ItemType.CMD_ARG, "");
			String[] props = prop.split(", ");
            for (String prop1 : props) {
                cmd.addItem(ItemType.CMD_ARG, prop1);
            }
			Packet res = sslClient.sendReceive(cmd, null);
			if (res == null) {
				return false;
			}
			if (res.hasError()) {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	public ArrayList<String> getOattrs(){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getOattrs");
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
				log(ret.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}

	/**
	 * @return
	 *//*
	private boolean connectToEngine(){
		try {
			Packet cmd = makeCmd("connect", null);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res.hasError()) {
				log("Connect: " + res.getErrorMessage());
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log("Connect: " + e.getMessage());
			return false;
		}
	}*/

	/**
	 * @param sBaseName
	 * @param sBaseType
	 * @param sName
	 * @param uattr
	 */
	public void setSchemaPerms(String sBaseName, String sBaseType, String sName, String uattr) {
		try {
			Packet cmd = makeCmd("setSchemaPerms", null, sBaseName, sBaseType, sName, uattr);
			Packet res = engineClient.sendReceive(cmd, null);
			if(res == null){
				return;
			}
			if(res.hasError()){
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param sBaseName
	 * @param sBaseType
	 * @param sName
	 * @param perms
	 * @param uattr
	 */
	public void setBasePermissions(String sBaseName, String sBaseType, String sName,
								   String sType, String perms, String uattr, String inh) {
		try {
			Packet cmd = makeCmd("setTablePerms", null, sBaseName, sBaseType,
					sName, sType, perms, uattr, inh);
			Packet res = engineClient.sendReceive(cmd, null);
			if(res == null){
				return;
			}
			if(res.hasError()){
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param id
	 * @param type
	 * @return
	 *//*
	public ArrayList<String> getPerms(String id, String type){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = util.makeCmd("getPermittedOpsOnEntity", sProcessId, id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(this, "Undetermined error; null result returned");
				//exit = true;
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(this, res.getErrorMessage());
				//exit = true;
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
				log("ret: " + ret.get(i));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
			e.printStackTrace();
			//exit = true;
			return null;
		}
		return ret;
	}*/

	/**
	 * @param id
	 * @param type
	 * @return
	 */
	public ArrayList<String> getReps(String id, String type){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getReps", id, type);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
			log(ret);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}

	/**
	 * @param id
	 * @return
	 */
	public ArrayList<String> getOpsetInfo(String id){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getOpsetInfo", id);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getStringValue(i));
			}
			log(ret);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return ret;
	}


	///////////////Methods in ColPermEditor/////////////////////////////////////////////////////////////////
	public Packet getAllOps() {
		try {
			//Packet cmd = appBuilder.makeCmd("getAllOps");
			Packet cmd = makeCmd("getAllOps");
			Packet res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				return null;
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Exception in getAllOps: " + e.getMessage());
			return null;
		}
	}

	public Packet getUserAttributes() {
		try {
			Packet cmd = makeCmd("getUserAttributes");
			Packet res = sslClient.sendReceive(cmd, null);
			if (res.hasError()) {
				return null;
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Exception in getUserAttributes: " + e.getMessage());
			return null;
		}
	}

	///////////////////////////////////////////////
	public ArrayList<String> getDecsendants(String sOattrId, String sPcId){
		ArrayList<String> desc = new ArrayList<String>();
		try{
			Packet cmd = makeCmd("getDescendantOattrs", null, sOattrId, sPcId);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				System.out.println("IN GETDESCENDANTS is null");
				return null;
			}
			if (res.hasError()) {
				System.out.println("IN GETDESCENDANTS has error");
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				desc.add(res.getItemStringValue(i));
			}
			return desc;
		}catch(Exception e){
			return null;
		}
	}

	public boolean addOattr(String name, String descr, String other, String baseId, String prop){
		System.out.println(name + "," + baseId);
		try{
			Packet cmd = makeCmd("addOattr", null, name, descr, other,
					baseId, PM_NODE.OATTR.value, "no");
			cmd.addItem(ItemType.CMD_ARG, "");
			String[] props = prop.split(", ");
			for(int i = 0; i < props.length; i++){
				cmd.addItem(ItemType.CMD_ARG, props[i]);
			}

			ArrayList<String> arrProps = new ArrayList<String>(Arrays.asList(props));
			log(arrProps);
			String baseName = sysCaller.getNameOfEntityWithIdAndType(baseId, PM_NODE.OATTR.value);
			if(!arrProps.contains("name=" + name))
				cmd.addItem(ItemType.CMD_ARG, "name=" + name);

			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return false;
			}
			if (res.hasError()) {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public ArrayList<String> getPolicyClasses(){
		return sendCmdReceiveList("getPolicyClasses", null, "");
	}

	public ArrayList<String> sendCmdReceiveList(String sCmd, String ... sArgs){
		ArrayList<String> ret = new ArrayList<String>();
		try{
			Packet cmd = makeCmd(sCmd, sArgs);
			Packet res = engineClient.sendReceive(cmd, null);
			if (res == null) {
				return null;
			}
			if (res.hasError()) {
				System.out.println(res.getErrorMessage());
				return null;
			}
			for(int i = 0; i < res.size(); i++){
				ret.add(res.getItemStringValue(i));
			}
			return ret;
		}catch(Exception e){
			return null;
		}
	}
}

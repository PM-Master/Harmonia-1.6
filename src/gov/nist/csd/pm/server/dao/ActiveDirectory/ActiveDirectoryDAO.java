package gov.nist.csd.pm.server.dao.ActiveDirectory;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;
import static gov.nist.csd.pm.common.net.Packet.dnrPacket;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import gov.nist.csd.pm.admin.UserEditor;
import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketException;
import gov.nist.csd.pm.common.net.PacketManager;
import gov.nist.csd.pm.common.util.Log;
import gov.nist.csd.pm.common.util.RandomGUID;
import gov.nist.csd.pm.common.util.UtilMethods;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.server.audit.Audit;
import gov.nist.csd.pm.server.packet.ADPacketHandler;
import gov.nist.csd.pm.server.parser.ActionSpec;
import gov.nist.csd.pm.server.parser.CondSpec;
import gov.nist.csd.pm.server.parser.ContSpec;
import gov.nist.csd.pm.server.parser.ObjSpec;
import gov.nist.csd.pm.server.parser.OpSpec;
import gov.nist.csd.pm.server.parser.OpndSpec;
import gov.nist.csd.pm.server.parser.PatternSpec;
import gov.nist.csd.pm.server.parser.PcSpec;
import gov.nist.csd.pm.server.parser.RuleParser;
import gov.nist.csd.pm.server.parser.RuleScanner;
import gov.nist.csd.pm.server.parser.RuleSpec;
import gov.nist.csd.pm.server.parser.UserSpec;
import sun.security.x509.AttributeNameEnumeration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.naming.CommunicationException;
import javax.naming.InvalidNameException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
//import gov.nist.csd.pm.server.ActOpnd;
//import gov.nist.csd.pm.server.AdminVosQueueElement;
//import gov.nist.csd.pm.server.EventContext;
/**
 * This class is supposed to be used by the PM Engine
 * for interfacing the Active Directory 
 * (Similar one will be done for the )
 * 
 * 
 * @author DC&Gopi
 */


public class ActiveDirectoryDAO{
	Log log = new Log(Log.Level.INFO, true);

	private static  String sAttrSetContainerDN;
	private static  String sConnectorContainerDN;
	private static  String sDenyContainerDN;
	public static  String sEventContainerDN;
	private static  String sHostContainerDN;
	private static  String sVosNodeContainerDN;
	private static  String sAdminVosNodeContainerDN;
	private static  String sObjAttrContainerDN;
	private static  String sObjClassContainerDN;
	private static  String sOpsetContainerDN;
	private static  String sOsConfigContainerDN;
	private static  String sPolicyContainerDN;
	private static  String sPropertyContainerDN;
	private static  String sSacContainerDN;
	public static  String sSessionContainerDN;
	private static  String sStartupContainerDN;
	private static  String sUserAttrContainerDN;
	private static  String sUserContainerDN;
	private static  String sUserConfigContainerDN;
	private static  String sNameContainerDN;
	private static  String sVirtualObjContainerDN;
	private static  String sRuleContainerDN;
	private static  String sTaskContainerDN;
	private static  String sSconContainerDN;
	private static  String sSconaContainerDN;
	private static  String sEmailAcctContainerDN;
	private static  String sTemplateContainerDN;
	private String sAttrSetClass;
	private String sConnectorClass;
	private String sEventClass;
	private String sConditionClass;
	private String sDenyClass;
	private String sHostClass;
	private String sVosNodeClass;
	private String sAdminVosNodeClass;
	private String sObjAttrClass;
	private String sObjClassClass;
	private String sOpsetClass;
	private String sOsConfigClass;
	private String sPolicyClass;
	private String sPropertyClass;
	private String sSacClass;
	private String sSessionClass;
	private String sStartupClass;
	private String sUserClass;
	private String sUserAttrClass;
	private String sUserConfigClass;
	private String sVirtualObjClass;
	private String sScriptClass;
	private String sScriptSourceClass;
	private String sSourceLineClass;
	private String sRuleClass;
	private String sEventPatternClass;
	private String sActionClass;
	private String sOperandClass; 
	private String sTaskClass;
	private String sSconClass;
	private String sSconaClass;
	private String sEmailAcctClass;
	private String sTemplateClass;
	private DateFormat dfUpdate = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
	private String sLastUpdateTimestamp;
	private int nMaxDspAscs = GlobalConstants.PM_MAX_DISPLAYED_ASCS;
	private HashMap<String, Set<String>> htSelVosNodes = new HashMap<String, Set<String>>();

	// A hashtable to store the accessible objects and object attributes of the users.
	// The key is the user id. The value is a HashSet of object attribute IDs that are
	// accessible to the key user. The HashSet is computed by computeFastVos().
	private Hashtable htAcc = new Hashtable();

	// A global where processEvent stores the enabled script id.
	/**
	 * @uml.property  name="sEnabledScriptId"
	 */
	private String sEnabledScriptId;

	public void setContainerNames() {
		sAttrSetContainerDN = "CN=PmAttributeSetContainer," + ServerConfig.sThisDomain;
		sConnectorContainerDN = "CN=PmConnectorContainer," + ServerConfig.sThisDomain;
		sDenyContainerDN = "CN=PmDenyContainer," + ServerConfig.sThisDomain;
		sHostContainerDN = "CN=PmHostContainer," + ServerConfig.sThisDomain;
		sObjAttrContainerDN = "CN=PmObjectAttributeContainer," + ServerConfig.sThisDomain;
		sObjClassContainerDN = "CN=PmObjectClassContainer," + ServerConfig.sThisDomain;
		sOpsetContainerDN = "CN=PmOperationSetContainer," + ServerConfig.sThisDomain;
		sOsConfigContainerDN = "CN=PmOsConfigContainer," + ServerConfig.sThisDomain;
		sPolicyContainerDN = "CN=PmPolicyContainer," + ServerConfig.sThisDomain;
		sPropertyContainerDN = "CN=PmPropertyContainer," + ServerConfig.sThisDomain;
		sSacContainerDN = "CN=PmSacContainer," + ServerConfig.sThisDomain;
		sSessionContainerDN = "CN=PmSessionContainer," + ServerConfig.sThisDomain;
		sStartupContainerDN = "CN=PmStartupContainer," + ServerConfig.sThisDomain;
		sUserAttrContainerDN = "CN=PmUserAttributeContainer," + ServerConfig.sThisDomain;
		sUserContainerDN = "CN=PmUserContainer," + ServerConfig.sThisDomain;
		sUserConfigContainerDN = "CN=PmUserConfigContainer," + ServerConfig.sThisDomain;
		sNameContainerDN = "CN=PmNameContainer," + ServerConfig.sThisDomain;
		sEventContainerDN = "CN=PmEventContainer," + ServerConfig.sThisDomain;
		sVosNodeContainerDN = "CN=PmVosNodeContainer," + ServerConfig.sThisDomain;
		sAdminVosNodeContainerDN = "CN=PmAdminVosNodeContainer," + ServerConfig.sThisDomain;
		sVirtualObjContainerDN = "CN=PmVirtualObjContainer," + ServerConfig.sThisDomain;
		sRuleContainerDN = "CN=PmRuleContainer," + ServerConfig.sThisDomain;
		sTaskContainerDN = "CN=PmTaskContainer," + ServerConfig.sThisDomain;
		sSconContainerDN = "CN=PmSconContainer," + ServerConfig.sThisDomain;
		sSconaContainerDN = "CN=PmSconaContainer," + ServerConfig.sThisDomain;
		sEmailAcctContainerDN = "CN=PmEmailAcctContainer," + ServerConfig.sThisDomain;
		sTemplateContainerDN = "CN=PmTemplateContainer," + ServerConfig.sThisDomain;
	}


	// Returns the list of direct containers of a user, user attribute, or
	// object attribute.
	public static String getContainerList(String sId, String sType) {
		System.out.println(sId + ":" + sType);
		String sContainer = null;
		if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
			sContainer = sUserContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sContainer = sObjAttrContainerDN;
		} else {
			return null;
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sContainer);
			Attribute attr = attrs.get("pmToAttr");
			if (attr == null) {
				return null;
			}
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				if (first) {
					sb.append((String) enumer.next());
					first = false;
				} else {
					sb.append("," + (String) enumer.next());
				}
			}
			return sb.toString();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}


	// First empty almost all containers. Exceptions:
	// The HostContainer, the NameContainer.
	// Also, do not delete the current session in which the Admin Tool is
	// executing.

	public void reset(String sSessId) throws Exception {
		System.out.println("in reset");
		try {
			emptyContainer(sUserContainerDN, null);
			emptyContainer(sEmailAcctContainerDN, null);
			emptyContainer(sUserAttrContainerDN, null);
			emptyContainer(sUserConfigContainerDN, null);
			emptyContainer(sPolicyContainerDN, null);
			emptyContainer(sObjAttrContainerDN, null);
			emptyContainer(sOpsetContainerDN, null);
			emptyContainer(sOsConfigContainerDN, null);
			emptyContainer(sVirtualObjContainerDN, null);
			emptyContainer(sVosNodeContainerDN, null);
			emptyContainer(sAdminVosNodeContainerDN, null);
			emptyContainer(sConnectorContainerDN, null);
			emptyContainer(sSessionContainerDN, sSessId);
			emptyContainer(sObjClassContainerDN, null);
			emptyContainer(sEventContainerDN, null);
			emptyContainer(sAttrSetContainerDN, null);
			emptyContainer(sSacContainerDN, null);
			emptyContainer(sDenyContainerDN, null);
			emptyContainer(sRuleContainerDN, null);
			emptyContainer(sTaskContainerDN, null);
			emptyContainer(sSconContainerDN, null);
			emptyContainer(sSconaContainerDN, null);
			emptyContainer(sTemplateContainerDN, null);
			emptyContainer(sPropertyContainerDN, null);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			throw new Exception("Exception when emptying the database: ");
		}
		createInitialObjects();
	}


	public void emptyContainer(String sContainerDn, String sExceptedId) {
		NamingEnumeration<?> members;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			members = ServerConfig.ctx.search(sContainerDn, "(objectClass=*)", constraints);
			while (members != null && members.hasMore()) {
				SearchResult sr = (SearchResult) members.next();
				String sName = sr.getName();
				if (sExceptedId != null) {
					Attribute attr = sr.getAttributes().get("pmId");
					if (attr != null) {
						String sId = (String) attr.get();
						if (sExceptedId.equals(sId)) {
							continue;
						}
					}
				}
				ServerConfig.ctx.destroySubcontext(sName + "," + sContainerDn);
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
	}


	public void createInitialObjects() {
		System.out.println("createInitialObject");
		String sObjName = null; // Used to locate where an exception occurred.
		Attributes attrs;
		Packet res;

		createObjClass(GlobalConstants.PM_CLASS_CLASS_NAME, GlobalConstants.PM_CLASS_CLASS_ID, GlobalConstants.sClassOps,
				"Class of all object classes");
		createObjClass(GlobalConstants.PM_CLASS_ANY_NAME, GlobalConstants.PM_CLASS_ANY_ID, GlobalConstants.sAnyOps,
				"Class any class");
		createObjClass(GlobalConstants.PM_CLASS_FILE_NAME, GlobalConstants.PM_CLASS_FILE_ID, GlobalConstants.sFileOps,
				"Class of files");
		createObjClass(GlobalConstants.PM_CLASS_DIR_NAME, GlobalConstants.PM_CLASS_DIR_ID, GlobalConstants.sDirOps,
				"Class of directories");
		createObjClass(GlobalConstants.PM_CLASS_USER_NAME, GlobalConstants.PM_CLASS_USER_ID, GlobalConstants.sUserOps,
				"Class of PM users");
		createObjClass(GlobalConstants.PM_CLASS_UATTR_NAME, GlobalConstants.PM_CLASS_UATTR_ID, GlobalConstants.sUattrOps,
				"Class of PM user attributes");
		createObjClass(GlobalConstants.PM_CLASS_OBJ_NAME, GlobalConstants.PM_CLASS_OBJ_ID, GlobalConstants.sObjOps,
				"Class of PM objects");
		createObjClass(GlobalConstants.PM_CLASS_OATTR_NAME, GlobalConstants.PM_CLASS_OATTR_ID, GlobalConstants.sOattrOps,
				"Class of PM object attributes");
		createObjClass(GlobalConstants.PM_CLASS_POL_NAME, GlobalConstants.PM_CLASS_POL_ID, GlobalConstants.sPolOps,
				"Class of PM policy classes");
		createObjClass(GlobalConstants.PM_CLASS_OPSET_NAME, GlobalConstants.PM_CLASS_OPSET_ID, GlobalConstants.sOpsetOps,
				"Class of PM operation sets");
		createObjClass(GlobalConstants.PM_CLASS_CONN_NAME, GlobalConstants.PM_CLASS_CONN_ID, GlobalConstants.sConnOps,
				"Class of the PM connector node");
		try {
			// If the connector node does not exist, create it.
			sObjName = GlobalConstants.PM_CONNECTOR_NAME;
			if (!entityExists(GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value)) {
				attrs = new BasicAttributes(true); // true means ignoreCase
				attrs.put("objectClass", sConnectorClass);
				attrs.put("pmId", GlobalConstants.PM_CONNECTOR_ID);
				attrs.put("pmName", GlobalConstants.PM_CONNECTOR_NAME);
				ServerConfig.ctx.bind("CN=" + GlobalConstants.PM_CONNECTOR_ID + "," + sConnectorContainerDN,
						null, attrs);
			}

			// If the "admin" policy class does not exist, create it.
			sObjName = GlobalConstants.PM_ADMIN_NAME;
			if (!entityExists(GlobalConstants.PM_ADMIN_ID,PM_NODE.POL.value)) {
				res =  addPcInternal(GlobalConstants.PM_ADMIN_NAME, GlobalConstants.PM_ADMIN_ID,
						"The admin policy class", "No other info", null);
				if (res.hasError()) {
					System.out.println("Failed to create the \"admin\" policy class!");
					System.exit(1);
				}
			}

			// If the "superAdmin" user attribute does not exist, create it.
			sObjName = GlobalConstants.PM_SUPER_ADMIN_NAME;
			if (!entityExists(GlobalConstants.PM_SUPER_ADMIN_ID,PM_NODE.UATTR.value)) {
				attrs = new BasicAttributes(true); // true means ignoreCase
				attrs.put("objectClass", sUserAttrClass);
				attrs.put("pmId", GlobalConstants.PM_SUPER_ADMIN_ID);
				attrs.put("pmName", GlobalConstants.PM_SUPER_ADMIN_NAME);
				attrs.put("pmDescription", "The superAdmin user attribute.");
				attrs.put("pmOtherInfo", "No other info.");
				ServerConfig.ctx.bind(
						"CN=" + GlobalConstants.PM_SUPER_ADMIN_ID + "," + sUserAttrContainerDN,
						null, attrs);
				res =  addDoubleLink(GlobalConstants.PM_SUPER_ADMIN_ID,PM_NODE.UATTR.value,
						GlobalConstants.PM_ADMIN_ID,PM_NODE.POL.value);
				if (res.hasError()) {
					System.out.println("Failed to link \"superAdmin\" to policy class \"admin\"");
					System.exit(1);
				}
			}

			// If the "super" user does not exist, create it.
			sObjName = GlobalConstants.PM_SUPER_NAME;
			if (!entityExists(GlobalConstants.PM_SUPER_ID,PM_NODE.USER.value)) {
				res =  addUserInternal(GlobalConstants.PM_SUPER_NAME, GlobalConstants.PM_SUPER_ID,
						GlobalConstants.PM_SUPER_NAME, "Super administrator user",
						GlobalConstants.PM_SUPER_NAME, GlobalConstants.PM_SUPER_ADMIN_ID,PM_NODE.UATTR.value);
				if (res.hasError()) {
					System.out.println("Failed to create \"super\"");
					System.exit(1);
				}
			}

			// Create a virtual object (and its associated oattr) to represent
			// all entities.
			sObjName = GlobalConstants.PM_EVERYTHING_NAME;
			if (!entityExists(GlobalConstants.PM_EVERYTHING_ID, GlobalConstants.PM_OBJ)) {
				res =  addObjectInternal(GlobalConstants.PM_EVERYTHING_NAME,
						GlobalConstants.PM_EVERYTHING_ID, GlobalConstants.PM_EVERYTHING_ASSOC_ID,
						"Object mapped to all entities.", "No info.",
						GlobalConstants.PM_ADMIN_ID,PM_NODE.POL.value, GlobalConstants.PM_CLASS_CONN_NAME, null,
						null, null, GlobalConstants.PM_CONNECTOR_NAME, GlobalConstants.PM_CONNECTOR_ID, true,
						null, null, null, null, null, null, null);
				if (res.hasError()) {
					System.out.println("Failed to create the \"everything\" object");
					System.exit(1);
				}
			}

			// Create an operation set "all ops".
			sObjName = GlobalConstants.PM_ALL_OPS_NAME;
			if (!entityExists(GlobalConstants.PM_ALL_OPS_ID,PM_NODE.OPSET.value)) {
				res =  addOpsetAndOpInternal(GlobalConstants.PM_ALL_OPS_NAME,
						GlobalConstants.PM_ALL_OPS_ID, "Op set containing all operations",
						"No info.", GlobalConstants.PM_ANY_ANY, GlobalConstants.PM_SUPER_ADMIN_ID,
						PM_NODE.UATTR.value, GlobalConstants.PM_EVERYTHING_ASSOC_ID,PM_NODE.OATTR.value);
				if (res.hasError()) {
					System.out.println("Failed to create and assign all operations set");
					System.exit(1);
				}
			}

			// If a host for the computer where the engine is running does not
			// exist,
			// create one.
			InetAddress ia = InetAddress.getLocalHost();
			String sCanHostName = ia.getCanonicalHostName();
			System.out.println("Engine host canonical name is " + sCanHostName);
			String[] pieces = sCanHostName.split("\\.");

			if (!entityNameExists(pieces[0], GlobalConstants.PM_HOST)) {

				JTextField workField = new JTextField();
				String message = "PM Work Area:";
				String sWorkArea;
				while (true) {
					int result = JOptionPane.showOptionDialog(null,
							new Object[]{message, workField}, "Work area",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, null, null);
					if (result == JOptionPane.CANCEL_OPTION) {
						System.exit(1);
					}
					sWorkArea = workField.getText().trim();
					if (sWorkArea.length() > 0) {
						break;
					}
				}
				res =  addHost(GlobalConstants.PM_SUPER_ID, pieces[0], sWorkArea,
						"Ignored.", "Ignored.", "Engine host", "true");
				if (res.hasError()) {
					System.out.println("Failed to create the engine's host!");
					System.exit(1);
				}
			}
			System.out.println("passed all in createInitialobjects");

		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			System.out.println("AD connection error");
			System.exit(1);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			System.out.println("Unable to create " + sObjName);
			System.exit(1);
		}
		System.out.println("end cIO");
	}


	public boolean entityExists(String sId, String sType) {
		NamingEnumeration<?> objects;
		String sContainer;

		if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sContainer = sConnectorContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sContainer = sPolicyContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
			sContainer = sUserContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sContainer = sOpsetContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
			sContainer = sVirtualObjContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_HOST)) {
			sContainer = sHostContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ_CLASS)) {
			sContainer = sObjClassContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SESSION)) {
			sContainer = sSessionContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_EMAIL_ACCT)) {
			sContainer = sEmailAcctContainerDN;
		} else {
			return false;
		}

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(null);
			objects = ServerConfig.ctx.search(sContainer, "(pmId=" + sId + ")", constraints);
			if (objects == null || !objects.hasMore()) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	// *****************************************************************************************************
	// DATA STRUCTURE METHODS
	// *****************************************************************************************************

	/*private void addADGraphNode(String baseId, String id, String name, String type){
		HashSet<String> members = adGraphMgr.getAllMembers(id.toString(), null);
		HashSet<String> containers = adGraphMgr.getAllContainers(id.toString(), null);
		HashSet<String> operations = null;
		if(type.equals(PM_NODE.OPSET.value)){
			try {
				operations = new HashSet<String>(getOpsetOperations(id.toString()));
			} catch (Exception e) {}
		}
		ADGraphNode node = new ADGraphNode(name, type, id, members, containers, operations);
		adGraphMgr.addNode(baseId, node);
	}

	private void deleteADGraphNode(String id){
		adGraphMgr.deleteNode(id);
	}

	private void updateADGraphNode(String id, String name, String type){
		HashSet<String> members = adGraphMgr.getAllMembers(id.toString(), null);
		HashSet<String> containers = adGraphMgr.getAllContainers(id.toString(), null);
		HashSet<String> operations = null;
		if(type.equals(PM_NODE.OPSET.value)){
			try {
				operations = new HashSet<String>(getOpsetOperations(id.toString()));
			} catch (Exception e) {}
		}
		ADGraphNode node = new ADGraphNode(name, type, id.toString(), members, containers, operations);
		adGraphMgr.updateNode(node);
	}

	private void assignADGraphNode(String startId, String endId){
		//add endId to the members of startId and startId to the containers of endId
		ADGraphNode start = adGraphMgr.getGraph().getNode(startId);
		start.addMember(endId);
		ADGraphNode end = adGraphMgr.getGraph().getNode(endId);
		end.addContainer(startId);
	}

	private void deleteAssignmentADGraphNode(String startId, String endId){
		ADGraphNode start = adGraphMgr.getGraph().getNode(startId);
		start.deleteMember(endId);
		ADGraphNode end = adGraphMgr.getGraph().getNode(endId);
		end.deleteContainer(startId);
	}*/


	// *****************************************************************************************************
	// END DATA STRUCTURE METHODS
	// *****************************************************************************************************


	// sId may be null, in which case the engine generates an id. Sometimes,
	// we want the pc to have a certain id (e.g., when the initial objects
	// are created.

	public Packet addPcInternal(String sName, String sId, String sDescr,
			String sInfo, String[] sProps) {
		// Test if duplicate name.
		if (entityNameExists(sName, PM_NODE.POL.value)) {
			return failurePacket("Policy with duplicate name");
		}

		// Prepare the attributes of the new policy class object.
		Attributes attrs = new BasicAttributes(true);
		if (sId == null) {
			RandomGUID myGUID = new RandomGUID();
			sId = myGUID.toStringNoDashes();
		}
		attrs.put("objectClass", sPolicyClass);
		attrs.put("pmId", sId);
		attrs.put("pmName", sName);
		attrs.put("pmDescription", sDescr);
		attrs.put("pmOtherInfo", sInfo);
		attrs.put("pmToConnector", GlobalConstants.PM_CONNECTOR_ID);

		// Prepare the path and create.
		String sDn = "CN=" + sId + "," + sPolicyContainerDN;
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (InvalidNameException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Invalid object name (id)" + sId);
		} catch (NameAlreadyBoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Duplicate id " );
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Probably invalid id " + sId);
		}

		// Prepare to add back link to the new policy in the connector node.
		// NOTE THAT THE ATTRIBUTE SHOULD BE NAMED pmFromPolicy,
		// BUT I WRONGLY DEFINED THAT ATTRIBUTE AS BEING SINGLE VALUED,
		// SO I HAD TO DEFINE A NEW ONE.
		String sConnectorDn = "CN=" + GlobalConstants.PM_CONNECTOR_ID + ","
				+ sConnectorContainerDN;
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmFromPolicyClass", sId));
		// Add the back link.
		try {
			ServerConfig.ctx.modifyAttributes(sConnectorDn, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Unable to set link to new policy in the connector node");
		}

		// Add the pc's properties, if any.
		if (sProps == null) {
			return ADPacketHandler.getSuccessPacket();
		}
		int n = sProps.length;
		if (n <= 0) {
			return ADPacketHandler.getSuccessPacket();
		}
		mods = new ModificationItem[n];
		for (int i = 0; i < n; i++) {
			System.out.println("Prop " + sProps[i]);
			mods[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmProperty", sProps[i]));
		}
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sId + "," + sPolicyContainerDN, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Unable to set the new user attribute's properties!");
		}
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Unable to create the result package!");
		}

		//addADGraphNode(GlobalConstants.PM_CONNECTOR_ID, sId, sName, PM_NODE.POL.value);

		return res;
	}

	// Create a predefined object class if it does not already exist.

	public void createObjClass(String sClassName, String sClassId,
			String[] sClassOps, String sDescr) {
		Attributes attrs;
		ModificationItem[] mods;

		if (entityExists(sClassId, GlobalConstants.PM_OBJ_CLASS)) {
			return;
		}
		attrs = new BasicAttributes(true); // true means ignoreCase
		attrs.put("objectClass", sObjClassClass);
		attrs.put("pmId", sClassId);
		attrs.put("pmName", sClassName);
		attrs.put("pmDescription", sDescr);
		attrs.put("pmOtherInfo", "No other info.");
		try {
			ServerConfig.ctx.bind("CN=" + sClassId + "," + sObjClassContainerDN, null, attrs);
			// Add the predefined operations.
			mods = new ModificationItem[sClassOps.length];
			for (int i = 0; i < sClassOps.length; i++) {
				mods[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOp", sClassOps[i]));
			}
			ServerConfig.ctx.modifyAttributes("CN=" + sClassId + "," + sObjClassContainerDN,
					mods);
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			System.out.println("AD connection error");
			System.exit(1);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			System.out.println("Unable to create predefined class "
					+ sClassName);
			System.exit(1);
		}
	}


	public void extractClassNames() {
		try {
			sAttrSetClass = getPmClassName("AttributeSetClassName");
			sConditionClass = getPmClassName("ConditionClassName");
			sConnectorClass = getPmClassName("ConnectorClassName");
			sDenyClass = getPmClassName("DenyClassName");
			sHostClass = getPmClassName("HostClassName");
			sObjAttrClass = getPmClassName("ObjectAttributeClassName");
			sObjClassClass = getPmClassName("ObjectClassClassName");
			sOpsetClass = getPmClassName("OperationSetClassName");
			sOsConfigClass = getPmClassName("OsConfigClassName");
			sPolicyClass = getPmClassName("PolicyClassName");
			sPropertyClass = getPmClassName("PropertyClassName");
			sSacClass = getPmClassName("SacClassName");
			sSessionClass = getPmClassName("SessionClassName");
			sStartupClass = getPmClassName("StartupClassName");
			sUserClass = getPmClassName("UserClassName");
			sUserAttrClass = getPmClassName("UserAttributeClassName");
			sUserConfigClass = getPmClassName("UserConfigClassName");
			sEventClass = getPmClassName("EventClassName");
			sVosNodeClass = getPmClassName("VosNodeClassName");
			sAdminVosNodeClass = getPmClassName("AdminVosNodeClassName");
			sVirtualObjClass = getPmClassName("VirtualObjectClassName");
			sScriptClass = getPmClassName("ScriptClassName");
			sScriptSourceClass = getPmClassName("ScriptSourceClassName");
			sSourceLineClass = getPmClassName("SourceLineClassName");
			sRuleClass = getPmClassName("RuleClassName");
			sEventPatternClass = getPmClassName("EventPatternClassName");
			sActionClass = getPmClassName("ActionClassName");
			sOperandClass = getPmClassName("OperandClassName");
			sTaskClass = getPmClassName("TaskClassName");
			sSconClass = getPmClassName("StaticConstraintClassName");
			sSconaClass = getPmClassName("SconaClassName");
			sEmailAcctClass = getPmClassName("EmailAcctClassName");
			sTemplateClass = getPmClassName("TemplateClassName");
		} catch (Exception e) {
			System.out.println("Unable to extract the class names");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public Packet addDoubleLink(String sId1, String sType1, String sId2,
			String sType2) {
		String sCont1, sCont2;
		String sAttr1, sAttr2;
		String sDn;
		ModificationItem[] mods = new ModificationItem[1];

		if (sType1.equalsIgnoreCase(PM_NODE.USER.value)) {
			sCont1 = sUserContainerDN;
			sAttr2 = "pmFromUser";
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont1 = sUserAttrContainerDN;
			if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)
					|| sType2.equalsIgnoreCase(PM_NODE.OPSET.value)) {
				sAttr2 = "pmFromAttr";
			} else {
				sAttr2 = "pmFromUserAttr";
			}
		} else if (sType1.equalsIgnoreCase(PM_NODE.POL.value)) {
			sCont1 = sPolicyContainerDN;
			sAttr2 = "pmFromPolicyClass";
		} else if (sType1.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sCont1 = sOpsetContainerDN;
			sAttr2 = "pmFromOpSet";
		} else if (sType1.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sCont1 = sObjAttrContainerDN;
			if (sType2.equalsIgnoreCase(PM_NODE.OATTR.value)) {
				sAttr2 = "pmFromAttr";
			} else {
				sAttr2 = "pmFromObjAttr";
			}
		} else {
			return failurePacket("Incorrect first node type");
		}

		if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont2 = sUserAttrContainerDN;
			sAttr1 = "pmToAttr";
		} else if (sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
			sCont2 = sPolicyContainerDN;
			sAttr1 = "pmToPolicy";
		} else if (sType2.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sCont2 = sOpsetContainerDN;
			sAttr1 = "pmToOpSet";
		} else if (sType2.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sCont2 = sObjAttrContainerDN;
			sAttr1 = "pmToAttr";
		} else if (sType2.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sCont2 = sConnectorContainerDN;
			sAttr1 = "pmToConnector";
		} else {
			return failurePacket("Incorrect second node type");
		}

		sDn = "CN=" + sId1 + "," + sCont1;
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute(sAttr1, sId2));
		try {
			ServerConfig.ctx.modifyAttributes(sDn, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Couldn't add direct link between nodes");
		}
		sDn = "CN=" + sId2 + "," + sCont2;
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute(sAttr2, sId1));
		try {
			ServerConfig.ctx.modifyAttributes(sDn, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Couldn't add back link between nodes");
		}

        //assignADGraphNode(sId1, sId2);

		return ADPacketHandler.getSuccessPacket();
	}

	// Internally used, no permission checks. In general, the user id is null
	// because this is a new user. Only for initial users, like super,
	// is the id known.

	public Packet addUserInternal(String sName, String sId, String sFull,
			String sInfo, String sPass, String sBaseId, String sBaseType) {
		System.out.println("AddUserInternal sName = " + sName);
		System.out.println("AddUserInternal sId = " + sId);
		System.out.println("AddUserInternal sFull = " + sFull);
		System.out.println("AddUserInternal sInfo = " + sInfo);
		System.out.println("AddUserInternal sPass = " + sPass);
		System.out.println("AddUserInternal sBaseId = " + sBaseId);
		System.out.println("AddUserInternal sBaseType = " + sBaseType);

		// Test if duplicate name.
		if (entityNameExists(sName,PM_NODE.USER.value)) {
			return failurePacket("Duplicate name!");
		}

		// A null password means an empty one.
		if (sPass == null) {
			sPass = "";
		}

		// Get a random 12-byte salt.
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[12];
		random.nextBytes(salt);

		// Get a message digest instance and hash the salt and the password.
		byte[] digest;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(salt);
			md.update(sPass.getBytes());
			sPass = null;
			digest = md.digest();
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Error while hashing the password!");
		}

		// Convert the hash to a string of hex digits.
		String sHash = UtilMethods.byteArray2HexString(salt) + UtilMethods.byteArray2HexString(digest);

		// In general, the user id is null when called from the Admin Tool.
		// When called for the initial objects, the id is predefined.
		if (sId == null) {
			RandomGUID myGUID = new RandomGUID();
			sId = myGUID.toStringNoDashes();
		}

		// Prepare the attributes of the new user object.
		Attributes attrs = new BasicAttributes(true);
		attrs.put("objectClass", sUserClass);
		attrs.put("pmId", sId);
		attrs.put("pmName", sName);
		attrs.put("pmFullName", sFull);
		attrs.put("pmOtherInfo", sInfo);
		attrs.put("pmPassword", sHash);

		System.out.print("Attempting to create the user object...");
		// Prepare the path and create the new user object.
		String sDn = "CN=" + sId + "," + sUserContainerDN;
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (InvalidNameException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Invalid user id " + sId);
		} catch (NameAlreadyBoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Duplicate id " + sId);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Probably invalid id " + sId);
		}
		System.out.println("OK");

		// A null base node is interpreted as the connector node.
		if (sBaseId == null) {
			sBaseId = GlobalConstants.PM_CONNECTOR_ID;
			sBaseType =PM_NODE.CONN.value;
		}
		Packet res =  addDoubleLink(sId,PM_NODE.USER.value, sBaseId,
				sBaseType);
		if (res.hasError()) {
			return res;
		}

		System.out.println("OK2");

		res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("OK4");

		//addADGraphNode(sBaseId, sId, sName, PM_NODE.USER.value);

		return res;
	}

	// Note that the parameter sComponents may contain either ids or names of
	// object attributes associated with objects, so we need to bring them
	// to the canonic form.

	public Packet addObjectInternal(String sName, String sId, String sAssocId,
			String sDescr, String sInfo, String sBaseId, String sBaseType,
			String sClass, String sType, String sHost, String sPath,
			String sOrigName, String sOrigId, boolean bInh, String sSender,
			String sReceiver, String sSubject, String sAttached,
			String sTemplateId, String sComponents, String[] sKeys) {
		System.out.print("addObjectInternal(");
		System.out.print("objname=" + sName);
		System.out.print(", objid=" + sId);
		System.out.print(", associd=" + sAssocId);
		System.out.print(", descr=" + sDescr);
		System.out.print(", info=" + sInfo);
		System.out.print(", baseid=" + sBaseId);
		System.out.print(", basetype=" + sBaseType);
		System.out.print(", class=" + sClass);
		System.out.print(", type=" + sType);
		System.out.print(", host=" + sHost);
		System.out.print(", path=" + sPath);
		System.out.print(", origname=" + sOrigName);
		System.out.print(", origid=" + sOrigId);
		System.out.println(", inh=" + bInh);
		System.out.println(", sender=" + sSender);
		System.out.println(", receiver=" + sReceiver);
		System.out.println(", subject=" + sSubject);
		System.out.println(", attached=" + sAttached);
		System.out.println(", template=" + sTemplateId);
		System.out.println(", components=" + sComponents);
		if (sKeys != null) {
			for (int i = 0; i < sKeys.length; i++) {
				System.out.println(", key=" + sKeys[i]);
			}
		}

		String sDn;

		// If the id of the new object is null, generate it.
		if (sId == null) {
			RandomGUID myGUID = new RandomGUID();
			sId = myGUID.toStringNoDashes();
		}

		// Create the associated object attribute. sAssocId could be null,
		// meaning an id will be generated for it, or not null, meaning
		// that the specified id will be used.
		Packet res =  addOattrInternal(sName, sAssocId,
				"Attribute associated to object " + sName, "No other info",
				sBaseId, sBaseType, sId, null);
		if (res.hasError()) {
			return res;
		}

		// We've been successful in adding the associated object attribute.
		// The result from addObjAttribute contains <name>:<id> of the
		// new associated attribute. Extract the id from it and create
		// the object.
		String sLine = res.getStringValue(0);
		String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);

		Attributes attrs = new BasicAttributes(true);
		attrs.put("objectClass", sVirtualObjClass);
		attrs.put("pmId", sId);
		attrs.put("pmName", sName);
		attrs.put("pmDescription", sDescr);
		attrs.put("pmOtherInfo", sInfo);
		attrs.put("pmAssocAttr", pieces[1]);
		attrs.put("pmObjClass", sClass);
		if (sHost != null) {
			attrs.put("pmHost", sHost);
		}
		if (sPath != null) {
			attrs.put("pmPath", sPath);
		}
		if (sOrigName != null) {
			attrs.put("pmOriginalName", sOrigName);
		}
		if (sOrigId != null) {
			attrs.put("pmOriginalId", sOrigId);
		}
		attrs.put("pmIncludesAscendants", (bInh ? "TRUE" : "FALSE"));
		if (sSender != null && sSender.length() > 0) {
			attrs.put("pmEmlSender", sSender);
		}
		if (sReceiver != null && sReceiver.length() > 0) {
			attrs.put("pmEmlRecip", sReceiver);
		}
		if (sSubject != null && sSubject.length() > 0) {
			attrs.put("pmEmlSubject", sSubject);
		}
		if (sAttached != null && sAttached.length() > 0) {
			attrs.put("pmEmlAttached", sAttached);
		}
		if (sSender != null && sSender.length() > 0) {
			String sTimestamp = dfUpdate.format(new Date());
			attrs.put("pmTimestamp", sTimestamp);
		}
		if (sTemplateId != null) {
			attrs.put("pmTemplateId", sTemplateId);
		}
		if (sComponents != null) {
			String sCanonicCompos = getCanonicList(sComponents);
			attrs.put("pmComponents", sCanonicCompos);
		}

		// Prepare the path and create.
		sDn = "CN=" + sId + "," + sVirtualObjContainerDN;
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (InvalidNameException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Invalid object id " + sId);
		} catch (NameAlreadyBoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Duplicate object id " + sId);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Probably invalid id " + sId);
		}

		// If this object represents a PM entity or a set of PM entities,
		// propagate it to all set members.
		// HashSet visited = new HashSet();
		// propagateRep(sClass, sOrigId, sInh, sId, visited); mods = new
		// ModificationItem[n];

		if (sKeys != null && sKeys.length > 0) {
			int n = sKeys.length;
			ModificationItem[] mods = new ModificationItem[n];
			for (int i = 0; i < n; i++) {
				mods[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmKey", sKeys[i]));
			}
			try {
				ServerConfig.ctx.modifyAttributes(
						"CN=" + sId + "," + sVirtualObjContainerDN, mods);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Unable to set the new object's keys!");
			}
		}

		// removeRep(sClass, sOrigId, sInh, sId, visited);

		setLastUpdateTimestamp();

		res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet!");
		}

		//addADGraphNode(sBaseId, sId, sName, PM_NODE.ASSOC.value);

		return res;
	}

	// Create a new opset with predefined id: use new name and id.
	// Create a new opset and let the engine set its id: use a new name and null
	// id.
	// Add an op to an old opset: use the old name (the id may be null).

	public Packet addOpsetAndOpInternal(String sOpsetName, String sOpsetId,
			String sDescr, String sInfo, String sOp, String sAscId,
			String sAscType, String sDescId, String sDescType) {
		System.out.println("addOpsetAndOpInternal(name=" + sOpsetName + ", id="
				+ sOpsetId + ", descr=" + sDescr + ", info=" + sInfo + ", op="
				+ sOp + ", ascid=" + sAscId + ", asctype=" + sAscType
				+ ", descid=" + sDescId + ", desctype=" + sDescType + ")");

		// Test if the opset exists.
		boolean newOpset = false;
		String sId = getEntityId(sOpsetName,PM_NODE.OPSET.value);
		if (sId == null) {
			// THIS IS A NEW OPSET.
			newOpset = true;

			// Create the opset and assign it.
			Attributes attrs = new BasicAttributes(true);
			if (sOpsetId == null) {
				RandomGUID myGUID = new RandomGUID();
				sOpsetId = myGUID.toStringNoDashes();
			}
			attrs.put("objectClass", sOpsetClass);
			attrs.put("pmId", sOpsetId);
			attrs.put("pmName", sOpsetName);
			attrs.put("pmDescription", sDescr);
			attrs.put("pmOtherInfo", sInfo);
			attrs.put("pmObjClass", "Ignored");

			// Create the opset object.
			try {
				ServerConfig.ctx.bind("CN=" + sOpsetId + "," + sOpsetContainerDN, null,
						attrs);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Unable to create opset \"" + sOpsetName
						+ "\"");
			}

			// Set the links.
			Packet res;
			if (sDescId == null) {
				sDescId = GlobalConstants.PM_CONNECTOR_ID;
				sDescType =PM_NODE.CONN.value;
			}
			if (sAscId != null) {
				res =  addDoubleLink(sAscId, sAscType, sOpsetId,
						PM_NODE.OPSET.value);
				if (res.hasError()) {
					return res;
				}
			}
			res =  addDoubleLink(sOpsetId,PM_NODE.OPSET.value, sDescId,
					sDescType);
			if (res.hasError()) {
				return res;
			}
		} else {
			sOpsetId = sId;
		}

		// Prepare the result for the successful case.
		Packet okRes = new Packet();
		try {
			okRes.addItem(ItemType.RESPONSE_TEXT, sOpsetName + GlobalConstants.PM_FIELD_DELIM
					+ sOpsetId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in addOpsetAndOpInternal: "
					+ e.getMessage());
		}

		// The operation couldn't be null if the opset already existed.
		if (sOp == null || sOp.length() == 0) {
			if (newOpset) {
				return okRes;
			} else {
				return failurePacket("Operation cannot be null!");
			}
		}

		// Operation must be a valid (existing) operation.
		if (!isOperation(sOp)) {
			return failurePacket(sOp + " is not an operation!");
		}
		// Operation cannot be duplicate.
		if (opsetContainsOp(sOpsetId, sOp)) {
			return failurePacket("Operation already in the operation set!");
		}

		// Add the operation to the opset.
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmOp", sOp));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sOpsetId + "," + sOpsetContainerDN,
					mods);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Unable to add operation \"" + sOp + "\": "
					+ e.getMessage());
		}
		return okRes;

	}


	public boolean entityNameExists(String sName, String sType) {
		NamingEnumeration<?> objects;
		String sContainer;

		if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sContainer = sConnectorContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sContainer = sPolicyContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
			sContainer = sUserContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
			sContainer = sVirtualObjContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sContainer = sOpsetContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_HOST)) {
			sContainer = sHostContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ_CLASS)) {
			sContainer = sObjClassContainerDN;
		} else {
			return false;
		}

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(null);
			objects = ServerConfig.ctx.search(sContainer, "(pmName=" + sName + ")",
					constraints);
			if (objects == null || !objects.hasMore()) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	public Packet addHost(String sSessId, String sHost, String sRepo,
			String sReserved, String sIpa, String sDescr, String sPdc) {
		// Check permissions...

		// Test if duplicate name.
		if (entityNameExists(sHost, GlobalConstants.PM_HOST)) {
			return failurePacket("Duplicate host name!");
		}
		// Test whether the attributes are valid.
		if (sHost == null || sHost.length() == 0) {
			return failurePacket("Null host name!");
		}
		if (!UtilMethods.hostNameIsValid(sHost)) {
			return failurePacket("Invalid host name!");
		}
		if (sRepo == null || sRepo.length() == 0) {
			return failurePacket("Null repository path!");
		}
		if (sDescr == null || sDescr.length() == 0) {
			return failurePacket("Null description!");
		}
		if (!sPdc.equalsIgnoreCase("true") && !sPdc.equalsIgnoreCase("false")) {
			return failurePacket("Non-boolean isPDController!");
		}

		// Prepare the attributes of the new host object.
		Attributes attrs = new BasicAttributes(true);
		RandomGUID myGUID = new RandomGUID();
		String sId = myGUID.toStringNoDashes();
		// Mandatory
		attrs.put("objectClass", sHostClass);
		attrs.put("pmId", sId);
		attrs.put("pmName", sHost);
		attrs.put("pmPath", sRepo);
		//attrs.put("pmCgiName", "Ignored.");
		attrs.put("pmDescription", sDescr);
		attrs.put("pmOtherInfo", "None.");
		//attrs.put("pmVirtualDir", "Ignored.");
		if (sIpa != null && sIpa.length() > 0) {
			attrs.put("pmIpAddress", sIpa);
		}
		attrs.put("pmIsDomController", sPdc.toUpperCase());

		// Create the record
		try {
			ServerConfig.ctx.bind("CN=" + sId + "," + sHostContainerDN, null, attrs);
		} catch (InvalidNameException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Invalid name " + sHost);
		} catch (NameAlreadyBoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Duplicate name " + sHost);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Probably invalid name " + sHost);
			// E.g.: "a/b" raises a NamingException with DIR_ERROR in JNDI
		}

		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sHost + GlobalConstants.PM_FIELD_DELIM + sId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when adding host: "
					+ e.getMessage());
		}
		return res;
	}

	// The PmNameContainer contains a map of all PM object classes, for example,
	// UserClassName ---> pmClassUser. The reason is that from time to time
	// the PM classes might get updated, and most often that requires a new
	// class with a changed name to be created. All one has to do is to change
	// the mapping in PmNameContainer, for example, UserClassName --->
	// pmClassUser1.
	// The actual name is kept in the attribute pmName.

	public String getPmClassName(String sClassId) throws Exception {
		Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sClassId + ","
				+ sNameContainerDN);
		Attribute attr = attrs.get("pmName");
		String sClassName = (String) attr.get();
		return sClassName;
	}

	// Given the name of an entity, return the id, if the entity exists,
	// otherwise null. Special care for entity name containing special
	// character *.

	public String getEntityId(String sName, String sType) {
		String sId = null;
		String sCont = null;
		System.out.println("NAME " + sName + " TYPE " + sType);

		if (sName == null) {
			return null;
		}
		if (sName.equals("*")) {
			return "*";
		}

		if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
			sCont = sUserContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
			sCont = sVirtualObjContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sCont = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sCont = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sCont = sPolicyContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sCont = sOpsetContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_HOST)) {
			sCont = sHostContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ_CLASS)) {
			sCont = sObjClassContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sCont = sConnectorContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_ASET)) {
			sCont = sAttrSetContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SAC)) {
			sCont = sSacContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_DENY)) {
			sCont = sDenyContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SCRIPT)) {
			sCont = sRuleContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_TASK)) {
			sCont = sTaskContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SCON)) {
			sCont = sSconContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SCONA)) {
			sCont = sSconaContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_TEMPLATE)) {
			sCont = sTemplateContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SESSION)) {
			sCont = sSessionContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_PROCESS)) {
			return sName;
		} else {
			return null;
		}
		System.out.println("cont: " + sCont);

		NamingEnumeration<?> entities;
		String sPreparedName = sName.replaceAll("\\*", "\\\\*");
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			entities = ServerConfig.ctx.search(sCont, "(pmName=" + sPreparedName + ")",
					constraints);
			if (entities == null || !entities.hasMore()) {
				System.out.println("entities = null");
				return null;
			}

			SearchResult sr = (SearchResult) entities.next();
			Attributes attrs = sr.getAttributes();
			//Attribute attr = attrs.get("pmId");
			sId = (String) (attrs.get("pmId").get());
			System.out.println("sId: " + sId);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		System.out.println("sId: " + sId);
		return sId;
	}


	public Packet assignUserToUattr(String sId1, String sId2) {
		// Conditions: sId1 not already an ascendant of sId2.
		if (userIsAscendant(sId1, sId2)) {
			return failurePacket("Marked user already has this attribute");
		}

		// Add the double link between the user sId1 and the attribute sId2.
		//		String sDn;
		//		ModificationItem[] mods = new ModificationItem[1];

		Packet res =  addDoubleLink(sId1,PM_NODE.USER.value, sId2,
				PM_NODE.UATTR.value);
		if (res.hasError()) {
			return res;
		}

		// If sId1 is assigned to the connector node, delete that assignment.
		if (userIsAssignedToConnector(sId1)) {
			res =  deleteDoubleLink(sId1,PM_NODE.USER.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("User successfully assigned to this attribute");
	}
	// In general, the argument sId is null, but sometimes we may want to create
	// an object attribute with a predefined id.

	public Packet addOattrInternal(String sName, String sId, String sDescr,
			String sInfo, String sBaseId, String sBaseType, String sAssocObjId,
			String[] sProps) {
		// Test if duplicate name.
		if (attrNameExists(sName,PM_NODE.OATTR.value)) {
			return failurePacket("Attribute with duplicate name");
		}

		String sDn;

		if (sId == null) {
			RandomGUID myGUID = new RandomGUID();
			sId = myGUID.toStringNoDashes();
		}

		// Prepare the attributes of the new attribute object.
		Attributes attrs = new BasicAttributes(true);
		attrs.put("objectClass", sObjAttrClass);
		attrs.put("pmId", sId);
		attrs.put("pmName", sName);
		attrs.put("pmDescription", sDescr);
		attrs.put("pmOtherInfo", sInfo);
		if (sAssocObjId != null) {
			attrs.put("pmAssocObj", sAssocObjId);
		}

		// Set the back link towards the base node in the new attribute.
		// At the same time, prepare the base node's DN
		// and the attribute to be added to the base node.
		String sBaseDn;
		ModificationItem[] mods = new ModificationItem[1];
		if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			attrs.put("pmToAttr", sBaseId);
			sBaseDn = "CN=" + sBaseId + "," + sObjAttrContainerDN;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmFromAttr", sId));
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			attrs.put("pmToPolicy", sBaseId);
			sBaseDn = "CN=" + sBaseId + "," + sPolicyContainerDN;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmFromObjAttr", sId));
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			attrs.put("pmToConnector", sBaseId);
			sBaseDn = "CN=" + sBaseId + "," + sConnectorContainerDN;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmFromObjAttr", sId));
		} else {
			return failurePacket("Base node's type incompatible with object attribute");
		}

		// Set the base node's link to the new user attribute.
		try {
			ServerConfig.ctx.modifyAttributes(sBaseDn, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Unable to set link to new user attribute in its base node");
		}

		// Prepare the path and create.
		sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (InvalidNameException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Invalid object name (id)" + sId);
		} catch (NameAlreadyBoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Duplicate id " + sId);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Probably invalid id " + sId);
		}

		// Add the attribute's properties, if any.
		if (sProps != null) {
			int n = sProps.length;
			if (n > 0) {
				mods = new ModificationItem[n];
				for (int i = 0; i < n; i++) {
					System.out.println("Property " + sProps[i]);
					mods[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
							new BasicAttribute("pmProperty", sProps[i]));
				}
				try {
					ServerConfig.ctx.modifyAttributes("CN=" + sId + ","
							+ sObjAttrContainerDN, mods);
				} catch (Exception e) {
					if (ServerConfig.debugFlag) {
						e.printStackTrace();
					}
					return failurePacket("Unable to set the new object attribute's properties!");
				}
			}
		}

		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet!");
		}

		//addADGraphNode(sBaseId, sId, sName, PM_NODE.OATTR.value);

		return res;
	}


	public String getCanonicList(String sCompos) {
		if (sCompos == null) {
			return null;
		}
		String[] pieces = sCompos.split(GlobalConstants.PM_FIELD_DELIM);
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (int i = 0; i < pieces.length; i++) {
			String sId = null;
			String sName = getEntityName(pieces[i],PM_NODE.OATTR.value);
			if (sName == null) {
				sId = getEntityId(pieces[i],PM_NODE.OATTR.value);
				if (sId == null) {
					return null;
				}
			} else {
				sId = pieces[i];
			}
			if (first) {
				first = false;
				sb.append(sId);
			} else {
				sb.append(GlobalConstants.PM_FIELD_DELIM + sId);
			}
		}
		return sb.toString();
	}

	public void setLastUpdateTimestamp() {
		sLastUpdateTimestamp = dfUpdate.format(new Date());
	}


	public boolean isOperation(String sOpName) {
		NamingEnumeration<?> cls;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmName", "pmOp"});
			cls = ServerConfig.ctx.search(sObjClassContainerDN, "(objectClass=*)",
					constraints);
			while (cls != null && cls.hasMore()) {
				SearchResult sr = (SearchResult) cls.next();
				/*String sClassName = (String) sr.getAttributes().get("pmName")
	                .get();*/

				// if (!sOpName.startsWith(sClassName)) continue;

				Attribute attr = sr.getAttributes().get("pmOp");
				if (attr == null) {
					continue;
				}
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String s = (String) enumer.next();
					if (sOpName.equalsIgnoreCase(s)) {
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}
	// Test whether a given opset contains a given operation.

	public boolean opsetContainsOp(String sOpsetId, String sOp) {
		Attributes attrs;
		Attribute attr;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sOpsetId + ","
					+ sOpsetContainerDN);
			attr = attrs.get("pmOp");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				if (sOp.equalsIgnoreCase((String) attrEnum.next())) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	// Test whether a given attr set has a given attribute.

	public boolean asetHasAttr(String sAsetId, String sAttrId) {
		Attributes attrs;
		Attribute attr;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sAsetId + ","
					+ sAttrSetContainerDN);
			attr = attrs.get("pmAttr");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				if (sAttrId.equalsIgnoreCase((String) attrEnum.next())) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}


	public boolean denyHasOp(String sDenyId, String sOp) {
		Attributes attrs;
		Attribute attr;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sDenyId + "," + sDenyContainerDN);
			attr = attrs.get("pmOp");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				if (sOp.equalsIgnoreCase((String) attrEnum.next())) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}


	public boolean denyHasOattr(String sDenyId, String sOattrId) {
		Attributes attrs;
		Attribute attr;

		if (sOattrId.startsWith("!")) {
			sOattrId = sOattrId.substring(1);
		}
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sDenyId + "," + sDenyContainerDN);
			attr = attrs.get("pmAttr");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sCrtId = (String) attrEnum.next();
				if (sCrtId.startsWith("!")) {
					sCrtId = sCrtId.substring(1);
				}
				if (sOattrId.equalsIgnoreCase(sCrtId)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}


	// Test whether a given class has a given operation.

	public boolean objClassHasOp(String sClassId, String sOp) {
		Attributes attrs;
		Attribute attr;
		//String sCont;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sClassId + ","
					+ sObjClassContainerDN);
			attr = attrs.get("pmOp");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				if (sOp.equalsIgnoreCase((String) attrEnum.next())) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	// Test whether a user is assigned to a (user) attribute.

	public boolean userIsAssigned(String sId1, String sId2) {
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId1 + "," + sUserContainerDN);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return false;
			}
			System.out.println(attr.getAll());
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sId = (String) attrEnum.next();
				System.out.println(sId2 + "??" + sId);
				if (sId2.equals(sId)) {
					System.out.println("returning true");
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("returning false1");
			return false;
		}
		System.out.println("returning false2");
		return false;
	}

	// Test whether an operation set is assigned to the connector.

	public boolean opsetIsAssignedToConnector(String sId) {
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sOpsetContainerDN);
			attr = attrs.get("pmToConnector");
			if (attr == null) {
				return false;
			}
			String sConn = (String) attr.get();
			if (sConn == null) {
				return false;
			}
			if (sConn.equalsIgnoreCase(GlobalConstants.PM_CONNECTOR_ID)) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	// Test whether a user is assigned (directly) to the connector.

	public boolean userIsAssignedToConnector(String sId) {
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sUserContainerDN);
			attr = attrs.get("pmToConnector");
			if (attr == null) {
				return false;
			}
			String sConn = (String) attr.get();
			if (sConn == null) {
				return false;
			}
			if (sConn.equalsIgnoreCase(GlobalConstants.PM_CONNECTOR_ID)) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	// Test whether a user has no descendant.

	public boolean userHasNoDescendant(String sId) {
		Attributes attrs;
		Attribute attr;
		String s;

		try {
			// Check the connector.
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sUserContainerDN);
			attr = attrs.get("pmToConnector");
			if (attr != null) {
				s = (String) attr.get();
				if (s != null && s.equalsIgnoreCase(GlobalConstants.PM_CONNECTOR_ID)) {
					return false;
				}
			}

			// Check the user attributes.
			attr = attrs.get("pmToAttr");
			if (attr != null && attr.size() > 0) {
				return false;
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}


	public boolean attrHasAscendants(String sId, String sType) {
		Attributes attrs;
		String sDn;

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId + "," + sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else {
			return false;
		}

		try {
			attrs = ServerConfig.ctx.getAttributes(sDn);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}

		Attribute attr = attrs.get("pmFromAttr");
		if (attr != null && attr.size() > 0) {
			return true;
		}

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			attr = attrs.get("pmFromUser");
			if (attr != null && attr.size() > 0) {
				return true;
			}
		}

		return false;
	}

	// Test whether an attribute (user or object) has descendants of type
	// attribute,
	// policy class, or connector.

	public boolean attrHasDescendants(String sId, String sType) {
		Attributes attrs;
		Attribute attr;
		String sDn;
		String s;

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId + "," + sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else {
			return false;
		}

		try {
			// Check the connector.
			attrs = ServerConfig.ctx.getAttributes(sDn);
			attr = attrs.get("pmToConnector");
			if (attr != null) {
				s = (String) attr.get();
				if (s != null && s.equalsIgnoreCase(GlobalConstants.PM_CONNECTOR_ID)) {
					return true;
				}
			}

			// Check attributes.
			attr = attrs.get("pmToAttr");
			if (attr != null && attr.size() > 0) {
				return true;
			}

			// Check policy classes.
			attr = attrs.get("pmToPolicy");
			if (attr != null && attr.size() > 0) {
				return true;
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return false;
	}


	public boolean attrHasOpsets(String sId, String sType) {
		Attributes attrs;
		String sDn;
		String sAttr;

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId + "," + sUserAttrContainerDN;
			sAttr = "pmToOpSet";
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
			sAttr = "pmFromOpSet";
		} else {
			return false;
		}

		try {
			attrs = ServerConfig.ctx.getAttributes(sDn);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}

		Attribute attr = attrs.get(sAttr);
		if (attr == null || attr.size() <= 0) {
			return false;
		}
		return true;
	}


	public Packet isAssigned(String sId1, String sType1, String sId2,
			String sType2) {
		boolean r = false;
		System.out.println(sId1 + " " + sType1 + " " + sId2 + " " + sType2);

		if (sType1.equalsIgnoreCase(PM_NODE.USER.value)) {
			if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				System.out.println("checking user is assigned to ua");
				r = userIsAssigned(sId1, sId2);
			} else if (sType2.equalsIgnoreCase(PM_NODE.CONN.value)) {
				r = userIsAssignedToConnector(sId1);
			} else {
				r = false;
			}
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				r = attrIsAssignedToAttr(sId1, sId2, sType1);
			} else if (sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
				r = attrIsAssignedToPolicy(sId1, sType1, sId2);
			} else if (sType2.equalsIgnoreCase(PM_NODE.CONN.value)) {
				r = attrIsAssignedToConnector(sId1, sType1);
			} else if (sType2.equalsIgnoreCase(PM_NODE.OPSET.value)) {
				r = attrIsAssignedToOpset(sId1, sId2);
			} else {
				r = false;
			}
		} else {
			r = false;
		}

		Packet res = new Packet();
		try {
			if (r) {
				res.addItem(ItemType.RESPONSE_TEXT, "yes");
			} else {
				res.addItem(ItemType.RESPONSE_TEXT, "no");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return res;
	}

	// Test whether a user or object attribute is assigned (directly) to the
	// connector.

	public boolean attrIsAssignedToConnector(String sId, String sType) {
		Attributes attrs;
		Attribute attr;
		String sCont;

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sCont = sObjAttrContainerDN;
		} else {
			return false;
		}

		try {

			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sCont);
			attr = attrs.get("pmToConnector");
			if (attr == null) {
				return false;
			}
			String sConn = (String) attr.get();
			if (sConn == null) {
				return false;
			}
			if (sConn.equalsIgnoreCase(GlobalConstants.PM_CONNECTOR_ID)) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	// Determines assignment between attributes (user or object).

	public boolean attrIsAssignedToAttr(String sId1, String sId2, String sType) {
		Attributes attrs;
		Attribute attr;
		String sCont;

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sCont = sObjAttrContainerDN;
		} else {
			return false;
		}

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId1 + "," + sCont);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				if (sId2.equals(attrEnum.next())) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}


	public boolean attrIsAssignedToPolicy(String sId1, String sType1,
			String sId2) {
		Attributes attrs;
		Attribute attr;
		String sDn;

		if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId1 + "," + sUserAttrContainerDN;
		} else if (sType1.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType1.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sDn = "CN=" + sId1 + "," + sObjAttrContainerDN;
		} else {
			return false;
		}

		try {
			attrs = ServerConfig.ctx.getAttributes(sDn);
			attr = attrs.get("pmToPolicy");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				if (sId2.equals(attrEnum.next())) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	// Determines whether a user attribute is assigned to an operation set.

	public boolean attrIsAssignedToOpset(String sId1, String sId2) {
		Attributes attrs;
		Attribute attr;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId1 + "," + sUserAttrContainerDN);
			attr = attrs.get("pmToOpSet");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				if (sId2.equals(attrEnum.next())) {
					return true;
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return false;
	}

	// Determines whether an operation set sId1 is assigned to an
	// object attribute sId2.

	public boolean opsetIsAssignedToAttr(String sId1, String sId2) {
		Attributes attrs;
		Attribute attr;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId2 + "," + sObjAttrContainerDN);
			attr = attrs.get("pmFromOpSet");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				if (sId1.equals(attrEnum.next())) {
					return true;
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return false;
	}

	// Test whether a user attribute or an object attribute is an ascendant of a
	// policy class.

	public boolean attrIsAscendantToPolicy(String sId1, String sType1,
			String sId2) {
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId2 + "," + sPolicyContainerDN);

			if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				attr = attrs.get("pmFromUserAttr");
			} else if (sType1.equalsIgnoreCase(PM_NODE.OATTR.value)
					|| sType1.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
				attr = attrs.get("pmFromObjAttr");
			} else {
				return false;
			}

			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sId = (String) attrEnum.next();
				if (attrIsAscendant(sId1, sId, sType1)) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	// Tests whether the user sId1 "inherits" the user attribute sId2.

	public boolean userIsAscendant(String sId1, String sId2) {
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId1 + "," + sUserContainerDN);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sId = (String) attrEnum.next();
				if (attrIsAscendant(sId, sId2,PM_NODE.UATTR.value)) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	// A user may be an ascendant of a policy only through some user attributes.

	public boolean userIsAscendantToPolicy(String sId1, String sId2) {
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId1 + "," + sUserContainerDN);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sId = (String) attrEnum.next();
				if (attrIsAscendantToPolicy(sId,PM_NODE.UATTR.value, sId2)) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	// Determine whether an attribute is ascendant of another attribute
	// (of the same type - user or object).

	public boolean attrIsAscendant(String sId1, String sId2, String sType) {
		Attributes attrs;
		Attribute attr;
		String sCont;

		if (sId1.equals(sId2)) {
			return true;
		}

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sCont = sObjAttrContainerDN;
		} else {
			return false;
		}

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId1 + "," + sCont);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				if (attrIsAscendant((String) attrEnum.next(), sId2, sType)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	// Get all operations of an object class. The object class is identified by
	// its name!!!

	public Packet getObjClassOps(String sClientId, String sClass) {
		Packet res = new Packet();
		NamingEnumeration<?> cls;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmOp"});
			String sPreparedClass = sClass.replaceAll("\\*", "\\\\*");
			cls = ServerConfig.ctx.search(sObjClassContainerDN, "(pmName=" + sPreparedClass
					+ ")", constraints);
			// cls should have exactly one entry.
			if (cls == null || !cls.hasMore()) {
				return failurePacket("No object class " + sClass);
			}
			SearchResult sr = (SearchResult) cls.next();
			Attributes attrs = sr.getAttributes();
			Attribute attr = attrs.get("pmOp");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					res.addItem(ItemType.RESPONSE_TEXT, (String) enumer.next());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket(e.getMessage());
		}
		return res;
	}
	// Get PM objects of a given class (users, user attributes, etc.
	// The result is a list of name:id.

	public Packet getPmEntitiesOfClass(String sClientId, String sClass)
			throws Exception {
		Packet res = new Packet();
		NamingEnumeration<?> objs;
		String sContainer;

		if (sClass.equals(GlobalConstants.PM_CLASS_USER_NAME)) {
			sContainer = sUserContainerDN;
		} else if (sClass.equals(GlobalConstants.PM_CLASS_UATTR_NAME)) {
			sContainer = sUserAttrContainerDN;
		} else if (sClass.equals(GlobalConstants.PM_CLASS_OBJ_NAME)) {
			sContainer = sVirtualObjContainerDN;
		} else if (sClass.equals(GlobalConstants.PM_CLASS_OATTR_NAME)) {
			sContainer = sObjAttrContainerDN;
		} else if (sClass.equals(GlobalConstants.PM_CLASS_CONN_NAME)) {
			sContainer = sConnectorContainerDN;
		} else if (sClass.equals(GlobalConstants.PM_CLASS_POL_NAME)) {
			sContainer = sPolicyContainerDN;
		} else if (sClass.equals(GlobalConstants.PM_CLASS_OPSET_NAME)) {
			sContainer = sOpsetContainerDN;
		} else {
			return res;
		}

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			objs = ServerConfig.ctx.search(sContainer, "(objectClass=*)", constraints);
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}

		while (objs != null && objs.hasMore()) {
			SearchResult sr = (SearchResult) objs.next();
			String sName = (String) sr.getAttributes().get("pmName").get();
			String sId = (String) sr.getAttributes().get("pmId").get();
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		}
		return res;
	}

	// Return all objects, one per item. Each item contains:
	// For File/Dir: name|id|class|"no"|host|path.
	// For PM entities: name|id|class|inh|original name|original id.
	// For composites: name|id|"Composite"|"no"|template id|components|keys.
	// The components have the format: comp1:...:compn, where each compi is
	// the id of the oattr associated with a component object of the composite.
	// The keys have the format: key1=value1:...:keyn=valuen.

	public Packet getObjects(String sClientId) throws Exception {
		Packet res = new Packet();
		NamingEnumeration<?> objs;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName",
					"pmObjClass", "pmIncludesAscendants", "pmHost", "pmPath",
					"pmOriginalId", "pmOriginalName", "pmTemplateId",
					"pmComponents", "pmKey"});
			objs = ServerConfig.ctx.search(sVirtualObjContainerDN, "(objectClass=*)",
					constraints);

			while (objs != null && objs.hasMore()) {
				SearchResult sr = (SearchResult) objs.next();
				Attributes virattrs = sr.getAttributes();
				String sName = (String) virattrs.get("pmName").get();
				String sId = (String) virattrs.get("pmId").get();
				String sClass = (String) virattrs.get("pmObjClass").get();
				String sIncludes = (String) virattrs.get("pmIncludesAscendants").get();
				sIncludes = (sIncludes.equals("TRUE") ? "yes" : "no");
				if (sClass.equals(GlobalConstants.PM_CLASS_FILE_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_DIR_NAME)) {
					String sHost = (String) virattrs.get("pmHost").get();
					String sPath = (String) virattrs.get("pmPath").get();
					res.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sId + GlobalConstants.PM_ALT_FIELD_DELIM
							+ sClass + GlobalConstants.PM_ALT_FIELD_DELIM + sIncludes
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sHost + GlobalConstants.PM_ALT_FIELD_DELIM
							+ sPath);
				} else if (sClass.equals(GlobalConstants.PM_CLASS_USER_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_UATTR_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_OBJ_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_OATTR_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_POL_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_CONN_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_OPSET_NAME)) {
					String sOrigName = (String) virattrs.get("pmOriginalName").get();
					String sOrigId = (String) virattrs.get("pmOriginalId").get();
					res.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sId + GlobalConstants.PM_ALT_FIELD_DELIM
							+ sClass + GlobalConstants.PM_ALT_FIELD_DELIM + sIncludes
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sOrigName
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sOrigId);
				} else if (sClass.equals(GlobalConstants.PM_CLASS_CLIPBOARD_NAME)) {
					String sHost = (String) virattrs.get("pmHost").get();
					res.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sId + GlobalConstants.PM_ALT_FIELD_DELIM
							+ sClass + GlobalConstants.PM_ALT_FIELD_DELIM + sIncludes
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sHost);
				} else {
					res.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sId + GlobalConstants.PM_ALT_FIELD_DELIM
							+ sClass + GlobalConstants.PM_ALT_FIELD_DELIM + sIncludes);
				}
			}
			return res;
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
	}


	public Packet getOpsetOattrs(String sSessId, String sOpsetName) {
		String sOpsetId = getEntityId(sOpsetName,PM_NODE.OPSET.value);
		if (sOpsetId == null) {
			return failurePacket("No operation set " + sOpsetName);
		}
		Packet result = new Packet();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOpsetId + ","
					+ sOpsetContainerDN);
			Attribute attr = attrs.get("pmToAttr");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOattrId = (String) enumer.next();
					String sOattrName = getEntityName(sOattrId,PM_NODE.OATTR.value);
					result.addItem(ItemType.RESPONSE_TEXT, sOattrName
							+ GlobalConstants.PM_FIELD_DELIM + sOattrId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getOpsetOattrs: "
					+ e.getMessage());
		}
		return result;
	}

	// entity type representative objects
	// ---------------------------------------------------------------------------
	// o all objects that represent the associated object attribute.
	// b all objects that represent the object attribute.
	// etc.

	public Packet getOpsetsBetween(String sSessId, String sUattrName,
			String sEntName, String sEntType) {
		// A few checks.
		String sUattrId = getEntityId(sUattrName,PM_NODE.UATTR.value);
		if (sUattrId == null) {
			return failurePacket("No user attribute " + sUattrName);
		}

		// If sEntType is "o", getIdOfEntityWithNameAndType will return the id of the associated
		// object attribute.
		String sEntId = getEntityId(sEntName, sEntType);
		if (sEntId == null) {
			return failurePacket("No entity " + sEntName + " of type "
					+ sEntType);
		}

		// Find all objects that represent the entity, even when the entity is
		// an
		// object or object attribute.
		HashSet<String> repSet = getObjectsRepresentingEntity(sEntId, sEntType);
		printSet(repSet, GlobalConstants.PM_OBJ,
				"Set of virtual objects representing the entity "
						+ getEntityName(sEntId, sEntType));

		// Create an empty result.
		Packet result = new Packet();

		// If sEntType is b or o, find the opsets between them.
		// Find the opsets between sUattrName and each element in repSet.
		if (sEntType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sEntType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			getOpsetsBetween(sUattrId, sEntId, result);
		}

		// Now for every virtual object that directly represents the entity,
		// find the
		// associated object attribute, then get the opsets between the user
		// attribute
		// and the object attribute.
		Iterator<String> hsiter = repSet.iterator();
		while (hsiter.hasNext()) {
			String sObjId = hsiter.next();
			String sAssocId = getAssocOattr(sObjId);
			getOpsetsBetween(sUattrId, sAssocId, result);
		}
		return result;
	}

	// Delete all opsets directly assigned to an user attribute and an object
	// attribute

	public Packet deleteOpsetsBetween(String sSessId, String sProcId,
			String sUattrName, String sOattrName) {
		Packet opsetids = new Packet();
		String sUattrId = getEntityId(sUattrName,PM_NODE.UATTR.value);
		String sOattrId = getEntityId(sOattrName,PM_NODE.OATTR.value);
		getOpsetsBetween(sUattrId, sOattrId, opsetids);
		for (int i = 0; i < opsetids.size(); i++) {
			String s = opsetids.getStringValue(i);
			String[] pieces = s.split(GlobalConstants.PM_FIELD_DELIM);
			String sOpsetId = pieces[1];
			deleteDoubleLink(sUattrId,PM_NODE.UATTR.value, sOpsetId,PM_NODE.OPSET.value);
			deleteDoubleLink(sOpsetId,PM_NODE.OPSET.value, sOattrId,PM_NODE.OATTR.value);
			try {
				ServerConfig.ctx.destroySubcontext("CN=" + sOpsetId + ","
						+ sOpsetContainerDN);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Error while deleting opset " + sOpsetId
						+ ": " + e.getMessage());
			}
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Get all opsets between a given uattr and a given oattr.
	// Add their <name>:<id> to the array list specified as the third parameter.

	public void getOpsetsBetween(String sUattrId, String sOattrId, Packet res) {
		try {
			Attributes uattrAttrs = ServerConfig.ctx.getAttributes("CN=" + sUattrId + ","
					+ sUserAttrContainerDN);
			Attribute uattrAttr = uattrAttrs.get("pmToOpset");
			// From all these opsets, retain those assigned to the oattr.
			if (uattrAttr != null) {
				for (NamingEnumeration<?> enumer = uattrAttr.getAll(); enumer.hasMore(); ) {
					String sOpsetId = (String) enumer.next();
					Attributes opsetAttrs = ServerConfig.ctx.getAttributes("CN=" + sOpsetId
							+ "," + sOpsetContainerDN);
					Attribute opsetAttr = opsetAttrs.get("pmToAttr");
					if (opsetAttr != null) {
						for (NamingEnumeration<?> enum2 = opsetAttr.getAll(); enum2.hasMore(); ) {
							String sToAttrId = (String) enum2.next();
							if (sOattrId.equalsIgnoreCase(sToAttrId)) {
								res.addItem(ItemType.RESPONSE_TEXT,
										(String) opsetAttrs.get("pmName").get()
										+ GlobalConstants.PM_FIELD_DELIM + sOpsetId);
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public Packet deleteDoubleLink(String sId1, String sType1, String sId2,
			String sType2) {

		System.out.println(sId1 + "," + sType1 + ":" + sId2 + "," + sType2);

		String sCont1, sCont2;
		String sAttr1, sAttr2;
		String sDn;

		ModificationItem[] mods = new ModificationItem[1];

		if (sType1.equalsIgnoreCase(PM_NODE.USER.value)) {
			sCont1 = sUserContainerDN;
			sAttr2 = "pmFromUser";
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont1 = sUserAttrContainerDN;
			if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)
					|| sType2.equalsIgnoreCase(PM_NODE.OPSET.value)) {
				sAttr2 = "pmFromAttr";
			} else {
				sAttr2 = "pmFromUserAttr";
			}
		} else if (sType1.equalsIgnoreCase(PM_NODE.POL.value)) {
			sCont1 = sPolicyContainerDN;
			sAttr2 = "pmFromPolicyClass";
		} else if (sType1.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sCont1 = sOpsetContainerDN;
			sAttr2 = "pmFromOpSet";
		} else if (sType1.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sCont1 = sObjAttrContainerDN;
			if (sType2.equalsIgnoreCase(PM_NODE.OATTR.value)) {
				sAttr2 = "pmFromAttr";
			} else {
				sAttr2 = "pmFromObjAttr";
			}
		} else {
			return failurePacket("Incorrect first node type");
		}

		if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont2 = sUserAttrContainerDN;
			sAttr1 = "pmToAttr";
		} else if (sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
			sCont2 = sPolicyContainerDN;
			sAttr1 = "pmToPolicy";
		} else if (sType2.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sCont2 = sOpsetContainerDN;
			sAttr1 = "pmToOpSet";
		} else if (sType2.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sCont2 = sObjAttrContainerDN;
			sAttr1 = "pmToAttr";
		} else if (sType2.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sCont2 = sConnectorContainerDN;
			sAttr1 = "pmToConnector";
		} else {
			return failurePacket("Incorrect second node type");
		}

		sDn = "CN=" + sId1 + "," + sCont1;
		mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
				new BasicAttribute(sAttr1, sId2));
		try {
			System.out.println("Delete " + sAttr1 + "=" + sId2 + " in " + sDn);
			ServerConfig.ctx.modifyAttributes(sDn, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Couldn't delete direct link between nodes");
		}
		sDn = "CN=" + sId2 + "," + sCont2;
		mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
				new BasicAttribute(sAttr2, sId1));
		try {
			System.out.println("Delete " + sAttr2 + "=" + sId1 + " in " + sDn);
			ServerConfig.ctx.modifyAttributes(sDn, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Couldn't delete back link between nodes");
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public boolean attrNameExists(String sName, String sType) {
		NamingEnumeration<?> objects;
		String sContainer;

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sContainer = sObjAttrContainerDN;
		} else {
			return false;
		}

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(null);
			objects = ServerConfig.ctx.search(sContainer, "(pmName=" + sName + ")",
					constraints);
			if (objects == null || !objects.hasMore()) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String getEntityName(String sId, String sType) {
		Attributes attrs;
		//Attribute attr;
		String sName;
		String sDn;
		if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
			sDn = "CN=" + sId + "," + sUserContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId + "," + sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_HOST)) {
			sDn = "CN=" + sId + "," + sHostContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sDn = "CN=" + sId + "," + sOpsetContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ_CLASS)) {
			sDn = "CN=" + sId + "," + sObjClassContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
			sDn = "CN=" + sId + "," + sVirtualObjContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sDn = "CN=" + sId + "," + sPolicyContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sDn = "CN=" + sId + "," + sConnectorContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_ASET)) {
			sDn = "CN=" + sId + "," + sAttrSetContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SAC)) {
			sDn = "CN=" + sId + "," + sSacContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_DENY)) {
			sDn = "CN=" + sId + "," + sDenyContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_TASK)) {
			sDn = "CN=" + sId + "," + sTaskContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SCON)) {
			sDn = "CN=" + sId + "," + sSconContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SCONA)) {
			sDn = "CN=" + sId + "," + sSconaContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SCRIPT)) {
			sDn = "CN=" + sId + "," + sRuleContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_TEMPLATE)) {
			sDn = "CN=" + sId + "," + sTemplateContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_SESSION)) {
			sDn = "CN=" + sId + "," + sSessionContainerDN;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_PROCESS)) {
			return sId;
		} else {
			return null;
		}
		try {
			attrs = ServerConfig.ctx.getAttributes(sDn);
			if (attrs == null) {
				return null;
			}
			sName = (String) attrs.get("pmName").get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return sName;
	}

	// Return the set of all OBJECTS (NOT object attributes) that DIRECTLY
	// represent
	// the specified entity (regardless of whether they represent only the
	// entity
	// or its entire subgraph).

	public HashSet<String> getObjectsRepresentingEntity(String sEntId,
			String sEntType) {
		HashSet<String> prSet = new HashSet<String>();
		String sObjId;
		// Search PmVirtualObjContainer for objects x such that:
		// x represents a PM entity and
		// x.origid = entity.id.
		NamingEnumeration<?> objects;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId",
			"pmOriginalId"});
			objects = ServerConfig.ctx.search(sVirtualObjContainerDN, "(objectClass=*)",
					constraints);
			while (objects != null && objects.hasMore()) {
				SearchResult sr = (SearchResult) objects.next();
				sObjId = (String) sr.getAttributes().get("pmId").get();
				Attribute attr = sr.getAttributes().get("pmOriginalId");
				if (attr == null) {
					continue;
				}
				String sOrigId = (String) attr.get();
				if (sOrigId.equalsIgnoreCase(sEntId)) {
					prSet.add(sObjId);
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return prSet;
	}


	public void printSet(Set<String> hs, String sType, String caption) {
		if (caption != null && caption.length() > 0) {
			System.out.println(caption);
		}
		Iterator<String> hsiter = hs.iterator();

		System.out.print("{");
		boolean firstTime = true;
		while (hsiter.hasNext()) {
			String sId = hsiter.next();
			if (sType.equalsIgnoreCase(GlobalConstants.PM_PERM)) {
				if (firstTime) {
					System.out.print(sId);
					firstTime = false;
				} else {
					System.out.print(", " + sId);
				}
			} else {
				String sName = getEntityName(sId, sType);
				if (firstTime) {
					System.out.print(sName);
					firstTime = false;
				} else {
					System.out.print(", " + sName);
				}
			}
		}
		System.out.println("}");
	}



	// Get the object attribute associated to an object given by its id.

	public String getAssocOattr(String sObjId) {
		Attributes attrs;
		Attribute attr;
		if (sObjId == null) {
			return null;
		}
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			attr = attrs.get("pmAssocAttr");
			if (attr == null || attr.size() <= 0) {
				return null;
			}
			return (String) attr.get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return null;
	}


	public Packet getAssocOattr1(String sObjId){
		Packet res = new Packet();
		String assoc = getAssocOattr(sObjId);
		try {
			res.addItem(ItemType.RESPONSE_TEXT, assoc);
		} catch (PacketException e) {
			e.printStackTrace();
		}
		return res;
	}

	// An object attribute is ignored when computing an user's VOS iff:
	// the  to an object, AND (the object
	// either represents a PM entity, OR is a clipboard object).

	public boolean ignoreOattrInVos(String sOattrId) {
		Attributes attrs;
		Attribute attr;

		System.out.println("oooooooo ignoreOattrInVos for "
				+ getEntityName(sOattrId,PM_NODE.OATTR.value));

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sOattrId + ","
					+ sObjAttrContainerDN);
			attr = attrs.get("pmAssocObj");
			if (attr == null || attr.size() <= 0) {
				System.out.println("ignoreOattrInVos: pmAssocObj is null or empty");
				return false;
			}
			String sObjId = (String) attr.get();
			attrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			attr = attrs.get("pmObjClass");
			if (attr == null || attr.size() <= 0) {
				System.out.println("ignoreOattrInVos: pmObjClass is null or empty");
				return false;
			}
			String sClass = (String) attr.get();
			System.out.println("ignoreOattrInVos: sClass is " + sClass);
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_CLASS_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_UATTR_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_OBJ_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_OATTR_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_CONN_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_POL_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_OPSET_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_CLIPBOARD_NAME)) {
				System.out.println("ignoreOattrInVos: returned true.");
				return true;
			}
			return false;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}

	// Get the object associated to an object attribute or null if no such
	// object exists.

	public String getAssocObj(String sOattrId) {
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sOattrId + ","
					+ sObjAttrContainerDN);
			attr = attrs.get("pmAssocObj");
			// Should not happen, function should be called only for associated
			// attributes.
			if (attr == null || attr.size() <= 0) {
				return null;
			}
			return (String) attr.get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// Get the object associated to an object attribute given by its id.

	public Packet getAssocObj(String sClientId, String sOattrId) {
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sOattrId + ","
					+ sObjAttrContainerDN);
			attr = attrs.get("pmAssocObj");
			// Should not happen, function should be called only for associated
			// attributes.
			if (attr == null || attr.size() <= 0) {
				return null;
			}
			Packet res = new Packet();
			res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());
			return res;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Couldn't get the object: " + e.getMessage());
		}
	}


	public boolean hasAssocObj(String sId) {
		Attributes attrs;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sObjAttrContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		Attribute attr = attrs.get("pmAssocObj");
		if (attr == null || attr.size() <= 0) {
			return false;
		}
		return true;
	}


	public boolean opsetIsIsolated(String sId) {
		Attributes attrs;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sOpsetContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		Attribute attr = attrs.get("pmFromAttr");
		if (attr != null && attr.size() > 0) {
			return false;
		}
		attr = attrs.get("pmToAttr");
		if (attr == null || attr.size() <= 0) {
			return true;
		}
		return false;
	}






	public Packet getObjNamePath(String sObjName) {
		String sObjId = getEntityId(sObjName, GlobalConstants.PM_OBJ);
		if (sObjId == null) {
			return failurePacket("No object " + sObjName + "!");
		}
		String sPath = getObjPath(sObjId);
		if (sPath == null) {
			return failurePacket("No path for object " + sObjName + "!");
		}
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sPath);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet!");
		}
		return res;
	}


	public String getObjPath(String sId) {
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sVirtualObjContainerDN);
			attr = attrs.get("pmPath");
			if (attr == null) {
				return null;
			}
			return (String) attr.get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Get all info about an opset: id, name, descr, info, class name, ops.

	public Packet getOpsetInfo(String sClientId, String sId) {
		Packet res = new Packet();
		Attributes attrs;
		Attribute attr;
		//String s;

		try {
			// 0: the id.
			res.addItem(ItemType.RESPONSE_TEXT, sId);

			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sOpsetContainerDN);

			// 1: the name.
			attr = attrs.get("pmName");
			res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());

			// 2: the description.
			attr = attrs.get("pmDescription");
			res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());

			// 3: the other info.
			attr = attrs.get("pmOtherInfo");
			res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());

			// 4: the class name is ignored.
			attr = attrs.get("pmObjClass");
			res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());

			// 5...: the operations.
			attr = attrs.get("pmOp");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					res.addItem(ItemType.RESPONSE_TEXT, (String) enumer.next());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Unable to retrieve complete information: "
					+ e.getMessage());
		}
		return res;
	}

	// Get all operations of an operation set. The operation set is identified
	// by
	// its name!!!

	public Packet getOpsetOps(String sClientId, String sOpset) {
		Packet res = new Packet();
		NamingEnumeration<?> ops;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmOp"});
			ops = ServerConfig.ctx.search(sOpsetContainerDN, "(pmName=" + sOpset + ")",
					constraints);
			// ops should have exactly one entry.
			if (ops == null || !ops.hasMore()) {
				return failurePacket("No operation set " + sOpset);
			}
			SearchResult sr = (SearchResult) ops.next();
			Attributes attrs = sr.getAttributes();
			Attribute attr = attrs.get("pmOp");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					res.addItem(ItemType.RESPONSE_TEXT, (String) enumer.next());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket(e.getMessage());
		}
		return res;
	}


	public Packet getEntityId(String sSessId, String sName, String sType) {
		System.out.println("getEntityId(sessid, sname, stype) called");
		String sId = getEntityId(sName, sType);
		System.out.println("sId: " + sId);
		if (sId == null) {
			return failurePacket("No such entity or type!");
		}
		Packet result = new Packet();
		try {
			result.addItem(ItemType.RESPONSE_TEXT, sId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return result;
	}


	public Packet getEntityName(String sSessId, String sId, String sType) {
		String sName = getEntityName(sId, sType);
		if (sName == null) {
			return failurePacket("No such entity or type!");
		}
		Packet result = new Packet();
		try {
			result.addItem(ItemType.RESPONSE_TEXT, sName);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return result;
	}


	// This method deletes all objects directly assigned to a container.
	// If the container itself is the oattr associated to an object, that
	// object is deleted.
	// Deleting an object deletes also the associated oattr, as well as all
	// assignments from opsets to that object (actually to the associated
	// oattr), and all assignments from that object (actually from the
	// associated oattr) to any other container. Note that in this last
	// case a container can be an oattr, a pc, or the connector.

	public Packet deleteContainerObjects(String sSessId, String sContName, String type) {
		try {
			System.out.println("DeleteContainerObjects for " + sContName);
			// Check the container exists.
			if (type == null) {
				type = PM_NODE.OATTR.value;
			}
			String sContId = getEntityId(sContName, type);
			if (sContId == null) {
				return failurePacket("No such container: " + sContName);
			}

			// If the container is associated to an object, delete it (the
			// container,
			// the associated object, and the assignments).
			if (hasAssocObj(sContId)) {
				return deleteObjectInternal(sContId);
			}

			// The container is a true container. Get the collection of directly
			// contained oattrs.
			String adContName = "";
			if(type.equals(PM_NODE.OATTR.value)){
				adContName = sObjAttrContainerDN;
			}else if(type.equals(PM_NODE.POL.value)){
				adContName = sPolicyContainerDN;
			}
			Attributes contAttrs = ServerConfig.ctx.getAttributes("CN=" + sContId + ","
					+ adContName);
			String sAttr = "";
			if(type.equals(PM_NODE.OATTR.value)){
				sAttr = "pmFromAttr";
			}else if(type.equals(PM_NODE.POL.value)){
				sAttr = "pmFromObjAttr";
			}
			Attribute attr = contAttrs.get(sAttr);
			if (attr == null) {
				return ADPacketHandler.getSuccessPacket();
			}

			// For each oattr contained in the given container:
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOattrId = (String) enumer.next();

				// If it's not an object, continue.
				if (!hasAssocObj(sOattrId)) {
					continue;
				}
				System.out.println("Found object "
						+ getEntityName(sOattrId,PM_NODE.OATTR.value));

				// We found an oattr which is an object. Delete it!
				Packet res =  deleteObjectInternal(sOattrId);
				if (res.hasError()) {
					return res;
				}
			}
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in deleteContainerObjects(): "
					+ e.getMessage());
		}
	}

	// This mthod deletes an object completely. The argument is the id of the
	// oattr associated to the object.
	// We need to find all opsets assigned to it and delete these assignments.
	// If after this step an opset remains unassigned, it is also deleted.
	// Then we find all direct containers of this object and delete the
	// double assignment between the object and each container.
	// Note that container here means any object attribute, or policy,
	// or the connector, to which the object is assigned.
	// Finally, delete the object and the associated oattr.

	public Packet deleteObjectInternal(String sOattrId) {
		String sObjId = getAssocObj(sOattrId);
		if (sObjId == null) {
			return failurePacket("No object associated to attribute "
					+ sOattrId);
		}

		System.out.println("DeletObjectInternal called for "
				+ getEntityName(sOattrId,PM_NODE.OATTR.value));
		try {
			// Get the opsets assigned to the oattr.
			Attribute attr = getFromOpsets(sOattrId,PM_NODE.OATTR.value);

			// For each opset:
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpsetId = (String) enumer.next();
					System.out.println("Found opset "
							+ getEntityName(sOpsetId,PM_NODE.OPSET.value));

					// How many object attributes is sOpsetId assigned to?
					// If only one, then it's just the sOattrId, and we can
					// delete
					// the opset together with its assignments from user
					// attributes.
					// If more than one, delete just the assignment sOpsetId ->
					// sOattrId.
					Attribute attr2 = getToAttrs(sOpsetId,PM_NODE.OPSET.value);
					if (attr2.size() == 1) {
						// First delete the opset's assignments from user
						// attributes.
						Attribute attr3 = getFromAttrs(sOpsetId,PM_NODE.OPSET.value);
						if (attr3 != null) {
							for (NamingEnumeration<?> enum3 = attr3.getAll(); enum3.hasMore(); ) {
								String sUattrId = (String) enum3.next();
								System.out.println("Deleting dbl link "
										+ getEntityName(sUattrId,
												PM_NODE.UATTR.value)
										+ "--->"
										+ getEntityName(sOpsetId,
												PM_NODE.OPSET.value));

								// Delete the assignment sUattrId <--->
								// sOpsetId.
								deleteDoubleLink(sUattrId,PM_NODE.UATTR.value,
										sOpsetId,PM_NODE.OPSET.value);
							}
						}
						// Delete the double link sOpsetId <--> sOattrId
						// preventively:
						deleteDoubleLink(sOpsetId,PM_NODE.OPSET.value, sOattrId,
								PM_NODE.OATTR.value);
						// Now delete the opset.
						ServerConfig.ctx.destroySubcontext("CN=" + sOpsetId + ","
								+ sOpsetContainerDN);
					} else {
						// The opset is assigned to other object attributes
						// beside sOattrId.
						// Just delete the assignment sOpsetId <---> sOattrId.
						deleteDoubleLink(sOpsetId,PM_NODE.OPSET.value, sOattrId,
								PM_NODE.OATTR.value);
					}
				}
			}

			// Find all containers of type OATTR of sOattrId.
			attr = getToAttrs(sOattrId,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sContId = (String) enumer.next();
					System.out.println("Found container "
							+ getEntityName(sContId,PM_NODE.OATTR.value));

					// Delete double link sOattrId <---> sContId.
					deleteDoubleLink(sOattrId,PM_NODE.OATTR.value, sContId,
							PM_NODE.OATTR.value);
				}
			}

			// Find all containers of type POL of sOattrId.
			attr = getToPolicies(sOattrId,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sPolId = (String) enumer.next();
					System.out.println("Found policy "
							+ getEntityName(sPolId,PM_NODE.POL.value));

					// Delete double link sOattrId <---> sPolId.
					deleteDoubleLink(sOattrId,PM_NODE.OATTR.value, sPolId,
							PM_NODE.POL.value);
				}
			}

			// Find all containers of type CONNECTOR of sOattrId (at most one).
			attr = getToConnector(sOattrId,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sConnId = (String) enumer.next();
					System.out.println("Found connector "
							+ getEntityName(sConnId,PM_NODE.CONN.value));

					// Delete double link sOattrId <---> sPConnId.
					deleteDoubleLink(sOattrId,PM_NODE.OATTR.value, sConnId,
							PM_NODE.CONN.value);
				}
			}

			// Delete the associated object and the oattr.
			ServerConfig.ctx.destroySubcontext("CN=" + sObjId + "," + sVirtualObjContainerDN);
			ServerConfig.ctx.destroySubcontext("CN=" + sOattrId + "," + sObjAttrContainerDN);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in deleteObjectInternal(): "
					+ e.getMessage());
		}

		return ADPacketHandler.getSuccessPacket();
	}



	public Packet deleteObjectStrong(String sSessId, String sObjId) throws Exception {
		String sAssocId = getAssocOattr(sObjId);
		if (sAssocId == null) {
			return failurePacket("Inconsistency: no object or no associated attribute");
		}

		String sContainers = getContainerList(sAssocId,PM_NODE.OATTR.value);

		try {
			// Get the opsets assigned to the assoc oattr.
			Attribute attr = getFromOpsets(sAssocId,PM_NODE.OATTR.value);
			// For each opset:
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpsetId = (String) enumer.next();

					// How many object attributes is this opset assigned to?
					// If one, then it's the o's assoc oattr, and the opset must
					// be deleted
					// together with its assignments from user attributes. If
					// more than
					// one, delete just the assignment opset -> o's assoc oattr.
					Attribute attr2 = getToAttrs(sOpsetId,PM_NODE.OPSET.value);
					if (attr2.size() == 1) {
						// First delete the opset's assignments from user
						// attributes.
						Attribute attr3 = getFromAttrs(sOpsetId,PM_NODE.OPSET.value);
						if (attr3 != null) {
							for (NamingEnumeration<?> enum3 = attr3.getAll(); enum3.hasMore(); ) {
								String sUattrId = (String) enum3.next();
								// Delete the assignment from uattr to opset.
								deleteDoubleLink(sUattrId,PM_NODE.UATTR.value,
										sOpsetId,PM_NODE.OPSET.value);
							}
						}
						// Now delete the opset.
						ServerConfig.ctx.destroySubcontext("CN=" + sOpsetId + ","
								+ sOpsetContainerDN);
					} else {
						// The opset is assigned to other object attributes in
						// addition to
						// o's assoc. Just delete the assignment from the opset
						// to o's assoc.
						deleteDoubleLink(sOpsetId,PM_NODE.OPSET.value, sAssocId,
								PM_NODE.OATTR.value);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Save the object name for events.
		String sObjName = getEntityName(sAssocId,PM_NODE.OATTR.value);

		// Delete the associated object attribute.
		Packet res =  deleteOattr(sSessId, sAssocId, false);
		if (res.hasError()) {
			return res;
		}

		// Delete the object.
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sObjId + "," + sVirtualObjContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete object " + sObjId + ": "
					+ e.getMessage());
		}

		res =  processEvent(sSessId, null, "Object delete", sObjName,
				sObjId, null, null, sContainers, sAssocId);
		return ADPacketHandler.getSuccessPacket();
	}

	// Delete an object.
	// Conditions: no opset is assigned to the object
	// (i.e., to the associated object attribute).

	public Packet deleteObject(String sSessId, String sObjId) throws Exception {
		// Find the associated attribute.
		String sAssocId = getAssocOattr(sObjId);
		if (sAssocId == null) {
			return failurePacket("Inconsistency: no object or no associated attribute");
		}

		String sContainers = getContainerList(sAssocId,PM_NODE.OATTR.value);

		// See whether the attribute has any opsets assigned to it.
		if (attrHasOpsets(sAssocId,PM_NODE.OATTR.value)) {
			return failurePacket("Associated attribute is assigned to operation sets");
		}

		// Save the object name for events.
		String sObjName = getEntityName(sAssocId,PM_NODE.OATTR.value);

		// Delete the associated object attribute.
		Packet res =  deleteOattr(sSessId, sAssocId, false);
		if (res.hasError()) {
			return res;
		}

		// Delete the object.
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sObjId + "," + sVirtualObjContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete object " + sObjId + ": "
					+ e.getMessage());
		}

		res =  processEvent(sSessId, null, "Object delete", sObjName,
				sObjId, null, null, sContainers, sAssocId);
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet deleteTemplate(String sSessId, String sTplId, String sTplName) throws Exception{
		System.out.println("CN=" + sTplId + "," + sTemplateContainerDN);
		try{
			ServerConfig.ctx.destroySubcontext("CN=" + sTplId + "," + sTemplateContainerDN);
		}catch(Exception e){
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete object " + sTplId + ": "
					+ e.getMessage());
		}
		Packet res =  processEvent(sSessId, null, "Template delete", sTplName,
				sTplId, null, null, null, null);
		if(res == null){
			System.out.println("res is null");
			return null;
		}
		if(res.hasError()){
			System.out.println(res.getErrorMessage());
			return null;
		}

		return ADPacketHandler.getSuccessPacket();
	}

	// sObjName is the name of the original object. The function creates a new
	// object with the name "copy<n>Of<sObjName>", where n is the null string,
	// or
	// 1, 2, etc.
	// The copy has the same class, type, containers as the original object.
	// The copy will have the content located on the session's host.
	//
	// Note that this function does not copy the object's content! This must be
	// done separately, probably in the K simulator.

	public Packet copyObject(String sSessId, String sProcId, String sObjName) {
		// Let's find the properties of the original object.
		String sObjId = getEntityId(sObjName, GlobalConstants.PM_OBJ);
		if (sObjId == null) {
			return failurePacket("No object of name " + sObjName);
		}
		Packet result =  getObjInfo(sObjId);
		if (result.hasError()) {
			return result;
		}

		String s = result.getStringValue(0);
		String[] pieces = s.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sClass = pieces[2];
		String sNameOrHost = null;
		if (pieces.length >= 5) {
			sNameOrHost = pieces[4];
		}
		String sIdOrPath = null;
		if (pieces.length >= 6) {
			sIdOrPath = pieces[5];
		}

		// Isolate the object type (rtf, eml, etc.) from sIdOrPath.
		// First get the underlying file name (the last element in the path).
		int nLastSep = sIdOrPath.lastIndexOf(File.separator);
		String sLookAt;
		String sCopyType;
		if (nLastSep < 0) {
			// The path has no file separators in it, look for type in the
			// entire
			// path.
			sLookAt = sIdOrPath;
		} else {
			sLookAt = sIdOrPath.substring(nLastSep + 1);
		}

		if (sLookAt == null || sLookAt.length() == 0) {
			sCopyType = "";
		} else {
			int nLastDot = sLookAt.lastIndexOf('.');
			if (nLastDot < 0) {
				sCopyType = "";
			} else {
				sCopyType = sLookAt.substring(nLastDot + 1);
			}
		}

		System.out.println("Class = " + sClass);
		System.out.println("Name or host = " + sNameOrHost);
		System.out.println("Id or path = " + sIdOrPath);

		String sCopyName = getCopyName(sObjName, GlobalConstants.PM_OBJ);
		String sCopyDescr = "Copy of object " + sObjName;
		String sCopyInfo = "None";
		String sCopyClass = pieces[2];
		String sCopyHost = getSessionHostName(sSessId);
		// String sCopyHost = "pmclient";
		String sPhysLoc = getHostRepositoryInternal(sCopyHost);
		String sCopyPath;
		if (sPhysLoc.endsWith(File.separator)) {
			sCopyPath = sPhysLoc + sCopyName + "." + sCopyType;
		} else {
			sCopyPath = sPhysLoc + File.separator + sCopyName + "." + sCopyType;
		}

		// Find the id of the attribute associated to the original object.
		String sOattrId = getEntityId(sObjName,PM_NODE.OATTR.value);
		if (sOattrId == null) {
			return failurePacket("The object " + sObjName
					+ " has no associated attribute!");
		}

		// Find the containers of the original object (note that PM or a policy
		// class can be an object container, not only other object attributes).
		String sContainers = getOattrContainers(sOattrId);
		System.out.println("Containers of object " + sObjName + " are: "
				+ sContainers);

		String sCopyId = null;
		String sAssocId = null;

		// For each container:
		pieces = sContainers.split(GlobalConstants.PM_LIST_MEMBER_SEP);
		for (int i = 0; i < pieces.length; i++) {
			String[] sTypeLabel = pieces[i].split(GlobalConstants.PM_ALT_DELIM_PATTERN);
			System.out.println("Container no. " + i + ": " + sTypeLabel[0]
					+ "|" + sTypeLabel[1]);

			// Get container's id,
			String sContId = getEntityId(sTypeLabel[1], sTypeLabel[0]);
			if (sContId == null) {
				return failurePacket("No container " + sTypeLabel[1]
						+ " of type " + sTypeLabel[0]);
			}

			// create or insert the new object within the container.
			if (i == 0) {
				// For the first container, create the object within. Let the
				// engine
				// generate the object's and the assoc. object attribute's ids.
				result =  addObjectInternal(sCopyName, null, null,
						sCopyDescr, sCopyInfo, sContId, sTypeLabel[0],
						sCopyClass, sCopyType, sCopyHost, sCopyPath, null,
						null, false, null, null, null, null, null, null, null);
				if (result.hasError()) {
					return result;
				}
				String sLine = result.getStringValue(0);
				String[] splinters = sLine.split(GlobalConstants.PM_FIELD_DELIM);
				sCopyId = splinters[1];
				sAssocId = getAssocOattr(sCopyId);
			} else {
				// For the other containers, assign the oattr associated with
				// the
				// new object to each container, if not already there.
				result =  assignInternal(sAssocId,PM_NODE.OATTR.value,
						sContId, sTypeLabel[0]);
				if (result.hasError()) {
					return result;
				}
			}
		}
		result = new Packet();
		try {
			result.addItem(ItemType.RESPONSE_TEXT, sCopyName
					+ GlobalConstants.PM_ALT_FIELD_DELIM + sCopyId + GlobalConstants.PM_ALT_FIELD_DELIM
					+ sCopyClass + GlobalConstants.PM_ALT_FIELD_DELIM + "no"
					+ GlobalConstants.PM_ALT_FIELD_DELIM + sCopyHost + GlobalConstants.PM_ALT_FIELD_DELIM
					+ sCopyPath);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return result;
	}


	public String getCopyName(String sEntName, String sEntType) {
		String sCandidate = "copyOf" + sEntName;
		int i = 0;
		while (entityNameExists(sCandidate, sEntType)) {
			i++;
			sCandidate = "copy" + String.valueOf(i) + "Of" + sEntName;
		}
		return sCandidate;
	}

	//NDK added this 

	public Packet audit(String sSessId, String sEvent, String sObjId,
			String sResult) throws Exception {
		String sUserId = getSessionUserId(sSessId);
		String sUser = getEntityName(sUserId,PM_NODE.USER.value);
		String sHost = getSessionHostName(sSessId);
		String sAction = sEvent;
		boolean result = true;
		if(sResult == "false"){
			result = false;
		}else{
			result = true;
		}
		String sDesc = "";
		String sObjName = getEntityName(sObjId, GlobalConstants.PM_OBJ);
		System.out.println("Audit method in the PMEngine************!!!!!!!!!!!");
		Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction, result, sDesc, sObjId, sObjName);
		return ADPacketHandler.getSuccessPacket();
	}
	//My section stops here

	/**
	 * Get only the first accessible ascendants. Modified from getDascObjects().
	 */

	public void getFirstAccessibleAscendants(String sBaseName,
			String sBaseId, String sBaseType, String sUserId, 
			String policyClassId, Packet result) throws PmServerException {

		String container = null;
		String context = null;
		if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			container = sPolicyContainerDN;
			context = "pmFromObjAttr";
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			container = sObjAttrContainerDN;
			context = "pmFromAttr";
		} else {
			throw new PmServerException("Invalid type " + sBaseType + " in getDascObjects");
		}
		try {
			// Get all ascending object attributes in the specified container
			Attributes allAscendingObjectAttributes = 
					ServerConfig.ctx.getAttributes("CN=" + sBaseId + "," + container);
			if (allAscendingObjectAttributes == null) {
				System.out.println("attrs for " + sBaseName + " is null");
				return;
			}
			// Get all the parent object attributes within the specified context.
			Attribute contextObjectAttributes = allAscendingObjectAttributes.get(context);
			if (contextObjectAttributes != null) {
				for (NamingEnumeration enumer = contextObjectAttributes.getAll(); enumer.hasMore();) {
					String objAttrID = (String) enumer.next();
					String objName = getEntityName(objAttrID,PM_NODE.OATTR.value);
					boolean objectAttributeIsAccessible = isAccessibleBy(objAttrID, objName, sUserId);
					if (objectAttributeIsAccessible){
						String oatype;
						if (hasAssocObj(objAttrID)) {
							oatype =PM_NODE.ASSOC.value;
						} else {
							oatype =PM_NODE.OATTR.value;
						}
						result.addItem(ItemType.RESPONSE_TEXT, oatype + GlobalConstants.PM_FIELD_DELIM +
								objAttrID + GlobalConstants.PM_FIELD_DELIM + objName); 
					} else {
						getFirstAccessibleAscendants(objName,
								objAttrID,PM_NODE.OATTR.value, sUserId, 
								policyClassId, result);
					}
				}
			}
		} catch (Exception e) {
			throw new PmServerException("Exception in getFirstAccessibleAscendants", e);
		}
	}


	public boolean isAccessibleBy(String objAttrID, String objName, String userID){
		int numberOfAscendantPolicies = 0;
		// First get all policy classes
		Vector policyClassIDs = getPolicyClasses();
		// Check if each policy class is a descendant of the current object attr.
		for (int i = 1; i < policyClassIDs.size(); i++) {
			String pcID = (String) policyClassIDs.get(i);
			if (attrIsAscendantToPolicy(objAttrID,PM_NODE.OATTR.value, pcID)) {
				numberOfAscendantPolicies++;
				boolean b = foundFor(objAttrID, objName, userID, pcID);
				if (b == false) {
					return false;
				}
			} 
		}
		if (numberOfAscendantPolicies == 0) 
			return false;
		else
			return true;
	}


	public boolean foundFor(String objAttrID, String objName, String userID, String pcID) {
		// Get all user attributes such that u->+ua and ua->+pc.
		Vector userAttributesToPC = getUserAttributesToPC(userID, pcID);
		// Find an oa' such that u->ua->oa' and oa->oa' and oa'->pc.
		for (int j = 0; j < userAttributesToPC.size(); j++) {
			String userAttrToPC = (String) userAttributesToPC.get(j);
			Vector operations = getToOpsets(userAttrToPC);
			for (int k = 0; k < operations.size(); k++) {
				String operation = (String)operations.elementAt(k);
				Vector objectAttributesFromOperation = getToAttrs(operation);
				for (int l = 0; l < objectAttributesFromOperation.size(); l++){
					String objectAttributeFromOperation = (String) objectAttributesFromOperation.get(l);
					if (attrIsAscendant(objAttrID, objectAttributeFromOperation,PM_NODE.OATTR.value) &&
							attrIsAscendantToPolicy(objectAttributeFromOperation,PM_NODE.OATTR.value, pcID)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Get all user attributes such that u->+ua and ua->+pc.
	 */

	public Vector getUserAttributesToPC(String userID, String pcID) {
		Vector result = new Vector();
		HashSet userAttributes = getUserDescendantsInternal(userID); 
		Iterator uaIterator = userAttributes.iterator();
		while (uaIterator.hasNext()) {
			String sUaId = (String)uaIterator.next();
			if (attrIsAscendantToPolicy(sUaId,PM_NODE.UATTR.value, pcID)) {
				result.add(sUaId);
			}
		}
		return result;
	}



	public Packet createLinkedObjects() {
		Attributes attrs = null;
		String sUattrContainerDN = "CN=PmUattrContainer," + ServerConfig.sThisDomain;

		try {
			attrs = new BasicAttributes(true);
			attrs.put("objectClass", "pmClassUattr");
			ServerConfig.ctx.bind("CN=uattr0," + sUattrContainerDN, null, attrs);

			for (int i = 1; i <= 5000; i++) {
				String sDn = "CN=uattr" + i + "," + sUattrContainerDN;
				attrs = new BasicAttributes(true);
				attrs.put("objectClass", "pmClassUattr");
				ServerConfig.ctx.bind(sDn, null, attrs);

				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmAssignedTo", sDn));
				ServerConfig.ctx.modifyAttributes("CN=uattr0," + sUattrContainerDN, mods);
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket(e.getMessage());
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet getPropertyValue(String sSessId, String sPropName) {
		Attributes attrs = null;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sPropName + ","
					+ sPropertyContainerDN);
		} catch (NameNotFoundException nnfe) {
			return failurePacket("No such property " + sPropName);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getProperty() (reading)!");
		}
		Attribute attr = attrs.get("pmValue");
		if (attr == null) {
			return failurePacket("Property " + sPropName + " has no value!");
		}
		String sPropValue = null;
		try {
			sPropValue = (String) attr.get();
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getProperty() (getting the value)!");
		}
		Packet result = new Packet();
		try {
			result.addItem(ItemType.RESPONSE_TEXT, sPropValue);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building result packet!");
		}
		return result;
	}


	public Packet getProperty(String sSessId, String sPropName) {
		Attributes attrs = null;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sPropName + ","
					+ sPropertyContainerDN);
		} catch (NameNotFoundException nnfe) {
			return failurePacket("No such property " + sPropName);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getProperty() (reading)!");
		}
		Attribute attr = attrs.get("pmValue");
		if (attr == null) {
			return failurePacket("Property " + sPropName + " has no value!");
		}
		String sPropValue = null;
		try {
			sPropValue = (String) attr.get();
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getProperty() (getting the value)!");
		}
		Packet result = new Packet();
		try {
			result.addItem(ItemType.RESPONSE_TEXT, sPropName + GlobalConstants.PM_PROP_DELIM
					+ sPropValue);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building result packet!");
		}
	}


	public Packet setProperty(String sSessId, String sPropName,
			String sPropValue) {
		// Is there a property with this name?
		boolean bNew = false;
		try {
			ServerConfig.ctx.getAttributes("CN=" + sPropName + "," + sPropertyContainerDN);
		} catch (NameNotFoundException nnfe) {
			bNew = true;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in setProperty() (reading)!");
		}

		if (bNew) {
			// A new property.
			Attributes attrs = new BasicAttributes(true);
			attrs.put("objectClass", sPropertyClass);
			attrs.put("pmValue", sPropValue);
			String sPropDn = "CN=" + sPropName + "," + sPropertyContainerDN;
			try {
				ServerConfig.ctx.bind(sPropDn, null, attrs);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Exception in setProperty() (new)!");
			}
		} else {
			// Property exists, just replace its value.
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmValue", sPropValue));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + sPropName + ","
						+ sPropertyContainerDN, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Exception in setProperty() (replace)!");
			}
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet deleteProperty(String sSessId, String sPropName) {
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sPropName + ","
					+ sPropertyContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete property with name "
					+ sPropName);
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet getProperties(String sSessId) {
		Packet result = new Packet();
		NamingEnumeration<SearchResult> props;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"cn", "pmValue"});
			props = ServerConfig.ctx.search(sPropertyContainerDN, "(objectClass=*)",
					constraints);
			while (props != null && props.hasMore()) {
				SearchResult sr = props.next();
				String sName = (String) sr.getAttributes().get("cn").get();
				String sValue = (String) sr.getAttributes().get("pmValue").get();
				result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_PROP_DELIM
						+ sValue);
			}
			return result;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in getProperties(): "
					+ e.getMessage());
		}
	}

	// Returns sRecId of a record that contains the given object or object
	// attribute as a field. There should be at most one such record.
	// Returns null if there is no such object or object attribute,
	// or if there is no record that contains it.

	public String getRecordOf(String sId, String sType) {
		String sOattrId = null;
		if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
			sOattrId = getAssocOattr(sId);
		} else {
			sOattrId = sId;
		}
		if (sOattrId == null) {
			return null;
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOattrId + ","
					+ sObjAttrContainerDN);
			Attribute attr = attrs.get("pmToAttr");
			if (attr == null) {
				return null;
			}

			// For each descendant, check whether it is a record:
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sRecId = (String) enumer.next();
				if (isRecord(sRecId)) {
					return sRecId;
				}
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Check whether the given object attribute is directly assigned to a
	// record,
	// i.e., a container with a template.

	public boolean isInARecord(String sOattrId) {
		try {
			// Get all direct descendant object attributes.
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOattrId + ","
					+ sObjAttrContainerDN);
			Attribute attr = attrs.get("pmToAttr");
			if (attr == null) {
				return false;
			}

			// For each descendant, check whether it is a record:
			NamingEnumeration<?> enumer = attr.getAll();
			while (enumer.hasMore()) {
				String sContId = (String) enumer.next();
				if (isRecord(sContId)) {
					enumer.close();
					return true;
				}
			}
			enumer.close();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Check whether the given record is indeed a record, and whether the given
	// oa
	// is directly assigned to the given record.

	public boolean isInRecord(String sOattrId, String sRecordId) {
		if (!isRecord(sRecordId)) {
			return false;
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOattrId + ","
					+ sObjAttrContainerDN);
			Attribute attr = attrs.get("pmToAttr");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sContId = (String) enumer.next();
				if (sContId.equalsIgnoreCase(sRecordId)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Packet isRecordPacket(String sId){
		Packet res = new Packet();
		
		boolean isR = isRecord(sId);
		try {
			res.addItem(ItemType.RESPONSE_TEXT, (isR) ? "yes" : "no");
		} catch (PacketException e) {
			e.printStackTrace();
		}

		return res;
	}
	
	public boolean isRecord(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sObjAttrContainerDN);
			if (attrs == null) {
				return false;
			}
			Attribute attr = attrs.get("pmTemplateId");
			if (attr == null) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	public Packet isInPos(String sSessId, String sObjId) {
		String sPosObjId = getVosNodeId(sObjId, sSessId);
		if (sPosObjId == null) {
			return failurePacket();
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Get all info about the composite object associated with the specified
	// object attribute.
	// Item 0: <name>:<id>
	// Item 1: <template name>:<template id>
	// Item 2: <comp count>
	// Items 3 to 3 + <comp count> - 1: <comp name>:<comp id>
	// Item 3 + <comp count>: <key count>
	// Items 3 + <comp count> + 1 to 3 + <comp count> + 1 + <key count> - 1:
	// <key name>=<key value>
	// The <comp id> is the id of the object attribute associated with a
	// component object.

	public Packet getRecordInfo(String sSessId, String sContId) {
		Packet result = new Packet();
		Attributes attrs;
		Attribute attr;

		try {
			// Get container's attributes of interest.
			attrs = ServerConfig.ctx.getAttributes("CN=" + sContId + ","
					+ sObjAttrContainerDN);

			String sContName = (String) attrs.get("pmName").get();
			attr = attrs.get("pmTemplateId");
			if (attr == null) {
				return failurePacket("Container " + sContId
						+ " is not a record!");
			}

			String sTplId = (String) attr.get();
			String sTplName = getEntityName(sTplId, GlobalConstants.PM_TEMPLATE);
			if (sTplName == null) {
				return failurePacket("Inconsistency: no template with such id: "
						+ sTplId);
			}

			result.addItem(ItemType.RESPONSE_TEXT, sContName + GlobalConstants.PM_FIELD_DELIM
					+ sContId);
			result.addItem(ItemType.RESPONSE_TEXT, sTplName + GlobalConstants.PM_FIELD_DELIM
					+ sTplId);

			// The component objects: how many, then name:id.
			attr = attrs.get("pmComponents");
			if (attr == null) {
				result.addItem(ItemType.RESPONSE_TEXT, "0");
			} else {
				String sObjCompos = (String) attr.get();
				String[] pieces = sObjCompos.split(GlobalConstants.PM_FIELD_DELIM);
				result.addItem(ItemType.RESPONSE_TEXT,
						String.valueOf(pieces.length));
				for (int i = 0; i < pieces.length; i++) {
					String sName = getEntityName(pieces[i],PM_NODE.OATTR.value);
					if (sName == null) {
						return failurePacket("Inconsistency: no component object (attribute) with id "
								+ pieces[i] + " exists!");
					}
					result.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_FIELD_DELIM + pieces[i]);
				}
			}

			// Now the keys: how many, then name=value.
			attr = attrs.get("pmKey");
			if (attr == null) {
				result.addItem(ItemType.RESPONSE_TEXT, "0");
			} else {
				result.addItem(ItemType.RESPONSE_TEXT,
						String.valueOf(attr.size()));
			}
			if (attr != null) {
				for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
					String sKey = (String) attrEnum.next();
					result.addItem(ItemType.RESPONSE_TEXT, sKey);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getRecordInfo(): "
					+ e.getMessage());
		}
	}

	// Returns the <name>:<id> of the oattrs that are records (containers)
	// satisfying some criteria. A record is a container distinguished by
	// the fact that it has an associated template.
	// If the given template is null, return all record containers.
	// Else if the given key is null, return all records that have the given
	// template.
	// Else it returns all records that have the given template and key.

	public Packet getRecords(String sSessId, String sTplId, String sKey) {
		System.out.println("template id = " + sTplId);
		System.out.println("key = " + sKey);

		NamingEnumeration<?> attrs;
		Packet result = new Packet();

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName",
			"pmTemplateId"});
			String sFilter;
			if (sTplId == null) {
				sFilter = "(objectClass=*)";
			} else if (sKey == null) {
				sFilter = "(&(objectClass=*)(pmTemplateId=" + sTplId + "))";
			} else {
				sFilter = "(&(objectClass=*)(pmTemplateId=" + sTplId
						+ ")(pmKey=" + sKey + "))";
			}
			attrs = ServerConfig.ctx.search(sObjAttrContainerDN, sFilter, constraints);
			while (attrs != null && attrs.hasMore()) {
				SearchResult sr = (SearchResult) attrs.next();
				Attribute attr = sr.getAttributes().get("pmTemplateId");
				if (attr == null) {
					continue;
				}
				String sId = (String) sr.getAttributes().get("pmId").get();
				String sName = (String) sr.getAttributes().get("pmName").get();
				result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
						+ sId);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getCompositeObjects()");
		}
	}

	// Create a record container (formerly a composite object).
	// sRecName is the name of the new record/container.
	// sBase is the name or id - both should work - of the base node where this
	// record is to be created.
	// sBaseType is the type of the base node where this
	// record is to be created.
	// sTplId is the id of the record's template.
	// sComponents contains the ids OR NAMES of the object attributes associated
	// with the component objects, in the format
	// <id or name 1>:<id or name 2>:...:<id or name n>.
	// sKeys contains the keys of the record, one key per item,
	// in the format <key name>=<key value>.

	public Packet createRecord(String sSessId, String sProcId,
			String sRecName, String sBase, String sBaseType, String sTplId,
			String sComponents, String[] sKeys) {

		String sBaseId = null, sBaseName = null;
		sBaseName = getEntityName(sBase, sBaseType);
		if (sBaseName != null) {
			sBaseId = sBase;
		} else {
			sBaseId = getEntityId(sBase, sBaseType);
			if (sBaseId != null) {
				sBaseName = sBase;
			} else {
				return failurePacket("No node of type " + sBaseType
						+ " with name or id " + sBase);
			}
		}

		Packet result = null;
		try {
			result =  addOattr(sSessId, sProcId, sRecName, sRecName,
					sRecName, sBaseId, sBaseType, "no", null, null);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception during record creation: "
					+ e.getMessage());
		}

		// If successful, the result contains the name and id of the new record.
		if (result.hasError()) {
			return result;
		}
		String sLine = result.getStringValue(0);
		String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);

		// Add the template id, components, and keys.
		int n = 0;
		if (sTplId != null) {
			n++;
		}
		if (sComponents != null) {
			n++;
		}
		if (sKeys != null) {
			n += sKeys.length;
		}
		if (n == 0) {
			return result;
		}

		ModificationItem[] mods = new ModificationItem[n];
		int i = 0;
		if (sTplId != null) {
			mods[i++] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmTemplateId", sTplId));
		}
		if (sComponents != null) {
			String sCanonicCompos = getCanonicList(sComponents);
			mods[i++] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmComponents", sCanonicCompos));
		}
		if (sKeys != null) {
			for (int j = 0; j < sKeys.length; j++) {
				mods[i + j] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmKey", sKeys[j]));
			}
		}
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + pieces[1] + "," + sObjAttrContainerDN,
					mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Unable to set the new record's properties (template, components, or keys)!");
		}
		return result;
	}


	public Packet createRecordInEntityWithProp(String sSessId, String sProcId,
			String sRecName, String sProp, String sBaseType, String sTplId,
			String sComponents, String[] sKeys) {
		String sBaseId = getEntityWithPropInternal(sBaseType, sProp);
		if (sBaseId == null) {
			return failurePacket("No entity of type " + sBaseType
					+ " with property " + sProp);
		}
		return createRecord(sSessId, sProcId, sRecName, sBaseId, sBaseType,
				sTplId, sComponents, sKeys);
	}

	// A record is an object container associated with:
	// - a template that tells the number, order, and names of the columns.
	// - pointers to the fields, which are the objects within the container
	// and must be contained in the appropriate columns.
	// - keys that allow relatively fast retrieval of the record.
	// This method sets the keys of a record.

	public Packet setRecordKeys(String sSessId, String sRecName, String[] sKeys) {

		// The record is an object container. Must exist and must be a record:
		String sId = getEntityId(sRecName,PM_NODE.OATTR.value);
		if (sId == null) {
			return failurePacket("No such record " + sRecName);
		}
		if (!isRecord(sId)) {
			return failurePacket(sRecName + " is not a record!");
		}

		if (sKeys == null || sKeys.length == 0) {
			return ADPacketHandler.getSuccessPacket();
		}

		int n = sKeys.length;
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
				new BasicAttribute("pmKey"));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sId + "," + sObjAttrContainerDN, mods);
		} catch (Exception e) {
			System.out.println("Probably no pmKey attribute found; exception follows!");
			e.printStackTrace();
		}

		mods = new ModificationItem[n];
		for (int i = 0; i < n; i++) {
			mods[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmKey", sKeys[i]));
		}
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sId + "," + sObjAttrContainerDN, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Unable to set the record's keys!");
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet addRecordKeys(String sSessId, String sRecName, String[] sKeys) {

		// The record is an object container. Must exist and must be a record:
		String sId = getEntityId(sRecName,PM_NODE.OATTR.value);
		if (sId == null) {
			return failurePacket("No such record " + sRecName);
		}
		if (!isRecord(sId)) {
			return failurePacket(sRecName + " is not a record!");
		}

		if (sKeys == null || sKeys.length == 0) {
			return ADPacketHandler.getSuccessPacket();
		}

		int n = sKeys.length;
		ModificationItem[] mods = new ModificationItem[n];
		for (int i = 0; i < n; i++) {
			mods[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmKey", sKeys[i]));
		}
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sId + "," + sObjAttrContainerDN, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Unable to add the key t " + sRecName + "!");
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Each item returned is a string <tpl name>:<tpl id>.

	public Packet getTemplates(String sClientId) {
		Packet result = new Packet();
		NamingEnumeration<?> tpls;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			tpls = ServerConfig.ctx.search(sTemplateContainerDN, "(objectClass=*)",
					constraints);

			while (tpls != null && tpls.hasMore()) {
				SearchResult sr = (SearchResult) tpls.next();
				String sName = (String) sr.getAttributes().get("pmName").get();
				String sId = (String) sr.getAttributes().get("pmId").get();
				result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
						+ sId);
			}
			return result;
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
	}


	public Packet addRecordKey(String sSessId, String sRecId, String sKey) {
		if (sKey == null || sKey.length() <= 0) {
			return ADPacketHandler.getSuccessPacket();
		}
		ModificationItem[] adds = new ModificationItem[1];

		try {
			adds[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmKey", sKey));
			ServerConfig.ctx.modifyAttributes("CN=" + sRecId + "," + sObjAttrContainerDN,
					adds);
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception when adding key to record "
					+ sRecId);
		}
	}


	public Packet addTemplateKey(String sSessId, String sTplId, String sKey) {
		if (sKey == null || sKey.length() <= 0) {
			return ADPacketHandler.getSuccessPacket();
		}
		ModificationItem[] adds = new ModificationItem[1];

		try {
			adds[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmKey", sKey));
			ServerConfig.ctx.modifyAttributes("CN=" + sTplId + "," + sTemplateContainerDN,
					adds);
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception when adding key to the template");
		}
	}

	// Add a record's template. sTplName is the template name, sContainers
	// contains the container ids separated by ":", and sKeys contains the
	// keys separated by ":".

	public Packet addTemplate(String sSessId, String sTplName,
			String sContainers, String sKeys) {
		String sId = getEntityId(sTplName, GlobalConstants.PM_TEMPLATE);
		if (sId != null) {
			return failurePacket("Duplicate template name!");
		}

		RandomGUID myGUID = new RandomGUID();
		sId = myGUID.toStringNoDashes();

		Attributes attrs = new BasicAttributes(true);
		attrs.put("objectClass", sTemplateClass);
		attrs.put("pmId", sId);
		attrs.put("pmName", sTplName);

		if (sContainers == null || sContainers.length() == 0) {
			return failurePacket("The containers argument cannot be empty or null!");
		}
		attrs.put("pmComponents", sContainers);

		// Prepare the path and create.
		String sDn = "CN=" + sId + "," + sTemplateContainerDN;
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when creating the template object!");
		}

		if (sKeys == null || sKeys.length() == 0) {
			return ADPacketHandler.getSuccessPacket();
		}

		String[] pieces = sKeys.split(GlobalConstants.PM_FIELD_DELIM);
		ModificationItem[] adds = new ModificationItem[pieces.length];

		try {
			for (int i = 0; i < pieces.length; i++) {
				adds[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmKey", pieces[i]));
			}
			ServerConfig.ctx.modifyAttributes("CN=" + sId + "," + sTemplateContainerDN, adds);
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception when adding keys to the template");
		}
	}


	public Packet isObjInOattrWithProp(String sSessId, String sObjName,
			String sProp) {
		String sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
		if (sOattrId == null) {
			return failurePacket("No object attribute with property \"" + sProp
					+ "\"!");
		}
		String sOaId = getEntityId(sObjName,PM_NODE.OATTR.value);
		if (sOaId == null) {
			return failurePacket("No object attribute or no object of name \""
					+ sObjName + "\"!");
		}

		Packet res = new Packet();
		try {
			if (attrIsAscendant(sOaId, sOattrId,PM_NODE.OATTR.value)) {
				res.addItem(ItemType.RESPONSE_TEXT, "yes");
			} else {
				res.addItem(ItemType.RESPONSE_TEXT, "no");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet in isObjInattrWithProp()!");
		}
		return res;
	}


	// Find the containers of an entity. We'll call the entity "base node".
	// "Container" can be any node of the PM graph to which the base node is
	// assigned.
	// If the base node is the connector, there are no containers.
	// If the base node is a policy class, the only container is the connector.
	// If the base node is a user attribute, its containers can be other
	// user attributes, a policy class, the connector, or an operation set.
	// If the base node is a user, its containers can be user attributes or the
	// connector.
	// If the base node is an object attribute or associate to an object,
	// its containers can be other object attributes, a policy, or the
	// connector.
	// If the base node is an operation set, its containers can be an object
	// attribute ot the connector.
	// Parameters:
	// sBaseName, sBaseId, sBaseType: the label/name, id, and type of the base
	// node. One
	// but not both of the name and id may be null. The type must be non-null.
	// sGraphType: the type of graph we display.

	public Packet getContainersOf(String sSessId, String sBaseName, String sBaseId,
			String sBaseType, String sGraphType) {
        log.debug("TRACE 5 - In ActiveDirectoryDAO.getContainersOf()");

		if (sBaseType == null) {
			return failurePacket("Null base node type in getContainersOf()");
		}
		if (sBaseName == null) {
			if (sBaseId == null) {
				return failurePacket("Null base node name and id in getContainersOf()");
			}
			sBaseName = getEntityName(sBaseId, sBaseType);
			if (sBaseName == null) {
				return failurePacket("No base node of type " + sBaseType
						+ " and id " + sBaseId);
			}
		} else if (sBaseId == null) {
			sBaseId = getEntityId(sBaseName, sBaseType);
			if (sBaseId == null) {
				return failurePacket("No base node of type " + sBaseType
						+ " and name " + sBaseName);
			}
		} else {
			String sBaseName2 = getEntityName(sBaseId, sBaseType);
			if (sBaseName2 == null) {
				return failurePacket("No base node of type " + sBaseType
						+ " and id " + sBaseId);
			}
			if (!sBaseName2.equalsIgnoreCase(sBaseName)) {
				return failurePacket("Inconsistency between base node name "
						+ sBaseName + " and id " + sBaseId);
			}
		}
		
		String sUserId = getSessionUserId(sSessId);
		System.out.println("getContainersOf, user is " + getEntityName(sUserId, PM_NODE.USER.value));
		System.out.println("Base type: " + sBaseType);

		Packet result = new Packet();

		try {
			// POLICY.
			if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sPolicyContainerDN);

				// Add the connector.
				Attribute attr = attrs.get("pmToConnector");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.CONN.value));
					}
				}

				// USER ATTRIBUTE.
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sUserAttrContainerDN);

				// Add the user attributes.
				Attribute attr = attrs.get("pmToAttr");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.UATTR.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.UATTR.value));
					}
				}

				// Add the policies.
				attr = attrs.get("pmToPolicy");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.POL.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.POL.value));
					}
				}

				// Add the connector.
				attr = attrs.get("pmToConnector");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.CONN.value));
					}
				}

				// Add the operation sets if graph type is correct.
				if (sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_CAPS)) {
					attr = attrs.get("pmToOpSet");
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							String sId = (String) attrEnum.next();
							result.addItem(
									ItemType.RESPONSE_TEXT,
									PM_NODE.OPSET.value + GlobalConstants.PM_FIELD_DELIM + sId
									+ GlobalConstants.PM_FIELD_DELIM
									+ getEntityName(sId,PM_NODE.OPSET.value));
						}
					}
				}

				// USER.
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.USER.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sUserContainerDN);

				// Add the user attributes.
				Attribute attr = attrs.get("pmToAttr");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.UATTR.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.UATTR.value));
					}
				}

				// Add the connector.
				attr = attrs.get("pmToConnector");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.CONN.value));
					}
				}

				// OBJECT ATTRIBUTE OR ASSOCIATE.
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)
					|| sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sObjAttrContainerDN);

				// Add the object attributes.
				Attribute attr = attrs.get("pmToAttr");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();

						boolean isAcc = oattrIsAccessible(sUserId, sId);
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.OATTR.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.OATTR.value)
								+ GlobalConstants.PM_FIELD_DELIM + isAcc);
					}
				}

				// Add the connector.
				attr = attrs.get("pmToConnector");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.CONN.value)
								+ GlobalConstants.PM_FIELD_DELIM + true);
					}
				}

				// Add the policy classes.
				attr = attrs.get("pmToPolicy");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.POL.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.POL.value)
								+ GlobalConstants.PM_FIELD_DELIM + true);
					}
				}

				// OPERATION SET.
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sOpsetContainerDN);

				// Add the object attributes.
				Attribute attr = attrs.get("pmToAttr");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						boolean isAcc = oattrIsAccessible(sUserId, sId);
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.OATTR.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.OATTR.value)
								+ GlobalConstants.PM_FIELD_DELIM + isAcc);
					}
				}

				// Add the connector.
				attr = attrs.get("pmToConnector");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.CONN.value)
								+ GlobalConstants.PM_FIELD_DELIM + true);
					}
				}

			}
			// Print packet
			log.debug("PACKET TO RETURN");
			result.print(true, "GET_CONTAINERS_OF");
	        log.debug("TRACE 6 - In ActiveDirectoryDAO.getContainersOf() END");

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getContainersOf()!");
		}
	}
	
	//***************************************MELL*****************************************  
	  boolean bMellPrint = false;
	  
	  
	  private Object subsequentOas(String sSessId, String sUserName, String sUserId,
			  String sUserType, String sTgtName, String sTgtId, String sTgtType) throws Exception {
		if (bMellPrint) {
			System.out.println("SubsequentOas called with arguments:");
			System.out.println("  session id: " + sSessId);
			System.out.println("  User name:" + sUserName);
			System.out.println("  User id: " + sUserId);
			System.out.println("  User type:" + sUserType);
			System.out.println("  Target name:" + sTgtName);
			System.out.println("  Target id: " + sTgtId);
			System.out.println("  Target type:" + sTgtType);
		}

	    if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value))
	    	return failurePacket("SubsequentOas called with first argument NOT a user!");
		String sUId = getEntityId(sUserName, PM_NODE.USER.value);
	    if (sUId == null) return failurePacket("No such user!");
	    if (sUserId == null)
	    	sUserId = sUId;
	    else if (!sUserId.equalsIgnoreCase(sUId))
	    	return failurePacket("Inconsistent user id!");
	    
		if (sTgtType.equalsIgnoreCase(PM_NODE.ASSOC.value)) sTgtType = PM_NODE.OATTR.value;
		if (!sTgtType.equalsIgnoreCase(PM_NODE.OATTR.value))
			return failurePacket("Selected entity is not an object attribute!");
		String sOaId = getEntityId(sTgtName, sTgtType);
	    if (sOaId == null) return failurePacket("No such object attribute!");
	    if (!sOaId.equalsIgnoreCase(sTgtId)) return failurePacket("Inconsistent object attribute id!");
	    
	    HashSet hsOas = subsequentOasInternal(sUserName, sUserId, sTgtName, sTgtId);
	    try {
	    	Packet res = new Packet();
		    if (hsOas != null) {
		    	for (Iterator hsiter = hsOas.iterator(); hsiter.hasNext(); ) {
		    		String sId = (String)hsiter.next();
		    		String sName = getEntityName(sId, PM_NODE.OATTR.value);
			    	res.addItem(ItemType.RESPONSE_TEXT, sId + GlobalConstants.PM_FIELD_DELIM + sName);
		    	}
		    	return res;
	    	}
	    	return failurePacket();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return failurePacket();
	    }
	  }

	  private Object successorOas(String sSessId, String sUserName, String sUserId,
			  String sUserType, String sTgtName, String sTgtId, String sTgtType) throws Exception {
		if (bMellPrint) {
			System.out.println("SuccessorOas called with arguments:");
			System.out.println("  session id: " + sSessId);
			System.out.println("  User name:" + sUserName);
			System.out.println("  User id: " + sUserId);
			System.out.println("  User type:" + sUserType);
			System.out.println("  Target name:" + sTgtName);
			System.out.println("  Target id: " + sTgtId);
			System.out.println("  Target type:" + sTgtType);
		}

	    if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value))
	    	return failurePacket("SuccessorOas called with first argument NOT a user!");
		String sUId = getEntityId(sUserName, PM_NODE.USER.value);
	    if (sUId == null) return failurePacket("No such user!");
	    if (sUserId == null)
	    	sUserId = sUId;
	    else if (!sUserId.equalsIgnoreCase(sUId))
	    	return failurePacket("Inconsistent user id!");
	    
		if (sTgtType.equalsIgnoreCase(PM_NODE.ASSOC.value)) sTgtType = PM_NODE.OATTR.value;
		if (!sTgtType.equalsIgnoreCase(PM_NODE.OATTR.value))
			return failurePacket("Selected entity is not an object attribute!");
		String sOaId = getEntityId(sTgtName, sTgtType);
	    if (sOaId == null) return failurePacket("No such object attribute!");
	    if (!sOaId.equalsIgnoreCase(sTgtId)) return failurePacket("Inconsistent object attribute id!");
	    
	    HashSet hsOas = successorOasInternal(sUserName, sUserId, sTgtName, sTgtId);
	    try {
	    	Packet res = new Packet();
		    if (hsOas != null) {
		    	for (Iterator hsiter = hsOas.iterator(); hsiter.hasNext(); ) {
		    		String sId = (String)hsiter.next();
		    		String sName = getEntityName(sId, PM_NODE.OATTR.value);
			    	res.addItem(ItemType.RESPONSE_TEXT, sId + GlobalConstants.PM_FIELD_DELIM + sName);
		    	}
		    	return res;
	    	}
	    	return failurePacket();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return failurePacket();
	    }
	  }

	  private HashSet successorOasInternal(String sUserName, String sUserId, String sOaName, String sOaId) throws Exception {
		if (bMellPrint) {
			System.out.println("SuccessorOasInternal called with arguments:");
			System.out.println("  user name: " + sUserName);
			System.out.println("  user id:" + sUserId);
			System.out.println("  oa name: " + sOaName);
			System.out.println("  oa id:" + sOaId);
		}
		long lStart = System.nanoTime();
		
		// Find the set of successor nodes of oa (i.e., {x | oa -> x}.
		HashSet hsSuccs = findSuccNodes(sOaId, PM_NODE.OATTR.value);
		printSet(hsSuccs, PM_NODE.OATTR.value, "Successors of " + getEntityName(sOaId, PM_NODE.OATTR.value));
		
		// Prepare the set of nodes "available for display"
		HashSet hsAvDisp = new HashSet();
		
		// Prepare the Hashtable of labeled object attributes nodes
		Hashtable htLabeled = findBorderOaPrivRestrictiveInternal(sUserName, sUserId);
		
		// For each successor node x
	   	Iterator hsiter = hsSuccs.iterator();
	   	while (hsiter.hasNext()) {
	   		String sSuccId = (String)hsiter.next();
	   		
	   		// Find "required" PCs for pred. x
	   		HashSet hsReqdPcs = findPcSet(sSuccId, PM_NODE.OATTR.value);
	   		printSet(hsReqdPcs, PM_NODE.POL.value, "Required PCs for " + getEntityName(sSuccId, PM_NODE.OATTR.value));

	   		// BFS from predecessor x to find "labeled" nodes.
	   		HashSet hsLabeled = getLabeledNodesFrom(sSuccId, htLabeled);
	   		printSet(hsLabeled, PM_NODE.OATTR.value, "Labeled nodes for succ "
	   				+ getEntityName(sSuccId, PM_NODE.OATTR.value));

	   		// From the labels of the labeled nodes for predecessor x
	   		// build a new hashtable {op -> pcset} with the mappings consolidated.
	   		Hashtable htNew = new Hashtable();
	   		// For (each y in the labeled node set)
	    	Iterator hsLabeledIter = hsLabeled.iterator();
	 	    while (hsLabeledIter.hasNext()) {
	 	    	String sYId = (String)hsLabeledIter.next();
	 	    	String sYName = getEntityName(sYId, PM_NODE.OATTR.value);
	 	    	// Extract y's label from the htLabeled.
	 	    	Hashtable htYLabel = (Hashtable)htLabeled.get(sYId);
	 	    	// For (each operation op in y's label
	 	        for (Enumeration ops = htYLabel.keys(); ops.hasMoreElements(); ) {
	 	        	String sOp = (String)ops.nextElement();
	 	        	// Here is the pcset corresponding to this op in y's label.
	        		HashSet hsPcsOfOpInYLabel = (HashSet)htYLabel.get(sOp);
	 	        	// If op is already a key in the new table,
	 	        	// do the union of the pcset corresponding to this op in
	 	        	// y's label and the pcset of the op already in the new table.
	 	        	if (htNew.containsKey(sOp)) {
	 	        		HashSet hsPcsOfOpInNewHt = (HashSet)htNew.get(sOp);
	 	        		hsPcsOfOpInNewHt.addAll(new HashSet(hsPcsOfOpInYLabel));
	 	        	} else {
	 	        		// If op is not a key in the new ht, insert the mapping
	 	        		// op -> pcset of this op in y's label into the new ht.
	 	        		htNew.put(sOp,  new HashSet(hsPcsOfOpInYLabel));
	 	        	}
	 	        }
	 	        System.out.println("For labeled node y = " + sYName);
	 	        System.out.println("  the new hashtable is");
	 	        for (Enumeration ops = htNew.keys(); ops.hasMoreElements(); ) {
	 	        	String sOp = (String)ops.nextElement();
	 	        	System.out.print(sOp + " -> ");
	 	        	HashSet hsPcs = (HashSet)htNew.get(sOp);
	 	        	Iterator hsPcsIter = hsPcs.iterator();
	 	    	    while (hsPcsIter.hasNext()) {
	 	    	    	String sPcId = (String)hsPcsIter.next();
	 	    	    	System.out.print(getEntityName(sPcId, PM_NODE.POL.value) + ", ");
	 	    	    }
	 	    	    System.out.println();
	 	        }
	 	    }
	 	    // Decide if the predecessor x is available for display.
	 	    // If in the new ht = {op -> pcset} one can find a mapping op -> pcset
	 	    // such that pcset contains the required PCs of x, then x is available.
	        for (Enumeration ops = htNew.keys(); ops.hasMoreElements(); ) {
	        	String sOp = (String)ops.nextElement();
	        	HashSet hsPcs = (HashSet)htNew.get(sOp);
	        	if (hsPcs.containsAll(hsReqdPcs)) {
	       			hsAvDisp.add(sSuccId);
	       			break;
	        	}
	        }
		}
	   	
	   	printSet(hsAvDisp, PM_NODE.OATTR.value, "The set of oa available for display");
		return hsAvDisp;
	  }

	  private HashSet subsequentOasInternal(String sUserName, String sUserId, String sOaName, String sOaId) throws Exception {
		if (bMellPrint) {
			System.out.println("SubsequentOasInternal called with arguments:");
			System.out.println("  user name: " + sUserName);
			System.out.println("  user id:" + sUserId);
			System.out.println("  oa name: " + sOaName);
			System.out.println("  oa id:" + sOaId);
		}
		long lStart = System.nanoTime();
		
		// Find the set of predecessor nodes of oa (i.e., {x | x -> oa}.
		HashSet hsPreds = findPredNodes(sOaId, PM_NODE.OATTR.value);
		printSet(hsPreds, PM_NODE.OATTR.value, "Predecessors of " + getEntityName(sOaId, PM_NODE.OATTR.value));
		
		// Find 'covered' PCs by calling findPcSet(oa)
		HashSet hsCoveredPcs = findPcSet(sOaId, PM_NODE.OATTR.value);
		printSet(hsCoveredPcs, PM_NODE.POL.value, "Covered PCs for " + getEntityName(sOaId, PM_NODE.OATTR.value));
		
		// Prepare the set of nodes "available for display"
		HashSet hsAvDisp = new HashSet();
		
		// Prepare the Hashtable of labeled object attributes nodes
		Hashtable htLabeled = null;
		
		// For each predecessor node x
	   	Iterator hsiter = hsPreds.iterator();
	   	while (hsiter.hasNext()) {
	   		String sPredId = (String)hsiter.next();
	   		
	   		// Find "required" PCs for pred. x
	   		HashSet hsReqdPcs = findPcSet(sPredId, PM_NODE.OATTR.value);
	   		printSet(hsReqdPcs, PM_NODE.POL.value, "Required PCs for " + getEntityName(sPredId, PM_NODE.OATTR.value));

	   		// If required PC set is a subset of covered PCs, then x is available for display.
	   		// if (hsReqdPcs.equals(hsCoveredPcs)) hsAvDisp.add(sPredId);
	   		if (hsCoveredPcs.containsAll(hsReqdPcs)) {
	   			hsAvDisp.add(sPredId);
	   			System.out.println("req is a subset of covered, make "
	   					+ getEntityName(sPredId, PM_NODE.OATTR.value)
	   					+ " available for display");
	   			continue;
	   		}
	   		
	   		// If we got here, it means that predecessor node x is not (yet) available.
	   		System.out.println("Pred " + getEntityName(sPredId, PM_NODE.OATTR.value)
	   				+ " is not available for display");
	   		// This is the first not available pred if and only if
	   		// 			htLabeled == null.
	   		// If this is the case, compute the labeled oas for user u.
	   		if (htLabeled == null) {
	   			htLabeled = findBorderOaPrivRestrictiveInternal(sUserName, sUserId);
	   			System.out.println("First pred not available, computed labeled oas");
	   		}   		
			// BFS from predecessor x to find "labeled" nodes.
	   		HashSet hsLabeled = getLabeledNodesFrom(sPredId, htLabeled);
	   		printSet(hsLabeled, PM_NODE.OATTR.value, "Labeled nodes for pred "
	   				+ getEntityName(sPredId, PM_NODE.OATTR.value));
	   		
	   		// From the labels of the labeled nodes for predecessor x
	   		// build a new hashtable {op -> pcset} with the mappings consolidated.
	   		Hashtable htNew = new Hashtable();
	   		// For (each y in the labeled node set)
	    	Iterator hsLabeledIter = hsLabeled.iterator();
	 	    while (hsLabeledIter.hasNext()) {
	 	    	String sYId = (String)hsLabeledIter.next();
	 	    	String sYName = getEntityName(sYId, PM_NODE.OATTR.value);
	 	    	// Extract y's label from the htLabeled.
	 	    	Hashtable htYLabel = (Hashtable)htLabeled.get(sYId);
	 	    	// For (each operation op in y's label
	 	        for (Enumeration ops = htYLabel.keys(); ops.hasMoreElements(); ) {
	 	        	String sOp = (String)ops.nextElement();
	 	        	// Here is the pcset corresponding to this op in y's label.
	        		HashSet hsPcsOfOpInYLabel = (HashSet)htYLabel.get(sOp);
	 	        	// If op is already a key in the new table,
	 	        	// do the union of the pcset corresponding to this op in
	 	        	// y's label and the pcset of the op already in the new table.
	 	        	if (htNew.containsKey(sOp)) {
	 	        		HashSet hsPcsOfOpInNewHt = (HashSet)htNew.get(sOp);
	 	        		hsPcsOfOpInNewHt.addAll(new HashSet(hsPcsOfOpInYLabel));
	 	        	} else {
	 	        		// If op is not a key in the new ht, insert the mapping
	 	        		// op -> pcset of this op in y's label into the new ht.
	 	        		htNew.put(sOp,  new HashSet(hsPcsOfOpInYLabel));
	 	        	}
	 	        }
	 	        System.out.println("For labeled node y = " + sYName);
	 	        System.out.println("  the new hashtable is");
	 	        for (Enumeration ops = htNew.keys(); ops.hasMoreElements(); ) {
	 	        	String sOp = (String)ops.nextElement();
	 	        	System.out.print(sOp + " -> ");
	 	        	HashSet hsPcs = (HashSet)htNew.get(sOp);
	 	        	Iterator hsPcsIter = hsPcs.iterator();
	 	    	    while (hsPcsIter.hasNext()) {
	 	    	    	String sPcId = (String)hsPcsIter.next();
	 	    	    	System.out.print(getEntityName(sPcId, PM_NODE.POL.value) + ", ");
	 	    	    }
	 	    	    System.out.println();
	 	        }
	 	    }
	 	    // Decide if the predecessor x is available for display.
	 	    // If in the new ht = {op -> pcset} one can find a mapping op -> pcset
	 	    // such that pcset contains the required PCs of x, then x is available.
	        for (Enumeration ops = htNew.keys(); ops.hasMoreElements(); ) {
	        	String sOp = (String)ops.nextElement();
	        	HashSet hsPcs = (HashSet)htNew.get(sOp);
	        	if (hsPcs.containsAll(hsReqdPcs)) {
	       			hsAvDisp.add(sPredId);
	       			break;
	        	}
	        }
		}
	   	
	   	printSet(hsAvDisp, PM_NODE.OATTR.value, "The set of oa available for display");
		return hsAvDisp;
	  }
	  
	  // sOaId is the id of an object attribute oa.
	  // htLabeled is the hashtable of border object attributes
	  // returned by findBorderOaPriv(u).
	  // This method finds all x such that oa ->* x and x is in htLabeled.keys.
	  HashSet getLabeledNodesFrom(String sOaId, Hashtable htLabeled) {
		  ArrayList queue = new ArrayList();
		  HashSet visited = new HashSet();
		  String sCrtId;
		  Attributes attrs;
		  Attribute attr;
		  HashSet hsResult = new HashSet();
		  
		  try {
			  // Insert the oa into the queue.
			  queue.add(sOaId);
			  // While the queue has elements, extract an element from it and
			  // if not already visited, visit it.
			  while (!queue.isEmpty()) {
				  sCrtId = (String)queue.remove(0);
				  if (visited.contains(sCrtId)) continue;
				  // Visit the crt element.
				  if (htLabeled.containsKey(sCrtId)) hsResult.add(sCrtId);
				  // Mark the crt element as visited.
				  visited.add(sCrtId);
				  // Insert the direct descendants of the crt element into the queue.
				  attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtId + "," + sObjAttrContainerDN);
				  attr = attrs.get("pmToAttr");
				  if (attr != null) for (NamingEnumeration enumer = attr.getAll();
						  enumer.hasMore(); ) {
					  String sAttrId = (String)enumer.next();
					  queue.add(sAttrId);
				  }
			  }
		  } catch (Exception e) {
			  e.printStackTrace();
			  return null;
		  }
		  return hsResult; 
	  }
	  
	  // Given an oa node, find the set {x | x -> oa}.
	  HashSet findPredNodes(String sAttrId, String sAttrType) {
		  Attributes attrs;
		  Attribute attr;
		  String sContName;
		  HashSet hsPreds = new HashSet();
		  
		  if (sAttrType.equalsIgnoreCase(PM_NODE.UATTR.value)) sContName = sUserAttrContainerDN;
		  else sContName = sObjAttrContainerDN;
		  try {
			  attrs = ServerConfig.ctx.getAttributes("CN=" + sAttrId + "," + sContName);
			  attr = attrs.get("pmFromAttr");
			  if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
				  hsPreds.add((String)enumer.next());
			  }
		  } catch (Exception e) {
			  e.printStackTrace();
			  return null;
		  }
		  return hsPreds;
	  }
	   
	  // Given an oa node, find the set {x | oa -> x}.
	  // Should work also for users and user attributes.
	  HashSet findSuccNodes(String sAttrId, String sAttrType) {
		  Attributes attrs;
		  Attribute attr;
		  String sContName;
		  HashSet hsSuccs = new HashSet();
		  
		  if (sAttrType.equalsIgnoreCase(PM_NODE.UATTR.value)) sContName = sUserAttrContainerDN;
		  else if (sAttrType.equalsIgnoreCase(PM_NODE.USER.value)) sContName = sUserContainerDN;
		  else sContName = sObjAttrContainerDN;
		  try {
			  attrs = ServerConfig.ctx.getAttributes("CN=" + sAttrId + "," + sContName);
			  attr = attrs.get("pmToAttr");
			  if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
				  hsSuccs.add((String)enumer.next());
			  }
		  } catch (Exception e) {
			  e.printStackTrace();
			  return null;
		  }
		  return hsSuccs;
	  }

	  // InitialOas() returns the initial set of oa nodes to display when a user
	  // logs on and wants to explore his/her objects.
	  private Object initialOas(String sSessId, String sUserName, String sUserId, String sUserType) throws Exception {
		if (bMellPrint) {
			System.out.println("InitialOas called with arguments:");
			System.out.println("  session id: " + sSessId);
			System.out.println("  User name:" + sUserName);
			System.out.println("  User id: " + sUserId);
			System.out.println("  User type:" + sUserType);
		}
			
		if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value)) return failurePacket("InitialOas only applicable to users!");
	    String sId = getEntityId(sUserName, sUserType);
	    if (sId == null) return failurePacket("No such user!");
	    if (sUserId == null) sUserId = sId;
	    else if (!sId.equalsIgnoreCase(sUserId)) return failurePacket("Inconsistent user id!");

	    HashSet hsOas = initialOasInternal(sUserName, sUserId);
	    if (hsOas == null) return failurePacket();
	    Packet res = new Packet();
	    try {
	    	Iterator hsiter = hsOas.iterator();
	 	    while (hsiter.hasNext()) {
	 	    	String sOaId = (String)hsiter.next();
	 	    	String sOaName = getEntityName(sOaId, PM_NODE.OATTR.value);
		    	res.addItem(ItemType.RESPONSE_TEXT, sOaId + GlobalConstants.PM_FIELD_DELIM + sOaName);
		    }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return failurePacket();
	    }
	    return res;
	  }

	  HashSet initialOasInternal(String sUserName, String sUserId) throws Exception {
		// Prepare the hashset to return.
		HashSet hsOa = new HashSet();
		
		// Call find_border_oa_priv(u). The result is a Hashtable
		// htoa = {oa -> {op -> pcset}}:
		Hashtable htOa = findBorderOaPrivRestrictiveInternal(sUserName, sUserId);
		
		// For each returned oa (key in htOa)
	    for (Enumeration oas = htOa.keys(); oas.hasMoreElements(); ) {
	    	String sOaId = (String)oas.nextElement();
	    	// Compute oa's required PCs by calling find_pc_set(sOaId).
	    	HashSet hsReqPcs = findPcSet(sOaId, PM_NODE.OATTR.value);
	    	// Extract oa's label.
	    	Hashtable htOaLabel = (Hashtable)htOa.get(sOaId);
	    	
	    	// Walk through the op -> pcset of the oa's label.
	    	// For each operation/access right
	        for (Enumeration ops = htOaLabel.keys(); ops.hasMoreElements(); ) {
	        	String sOp = (String)ops.nextElement();
	        	// Extract the pcset corresponding to this operation/access right.
	        	HashSet hsActualPcs = (HashSet)htOaLabel.get(sOp);
	        	// if the set of required PCs is a subset of the actual pcset,
	        	// then user u has some privileges on the current oa node.
	        	if (hsActualPcs.containsAll(hsReqPcs)) {
	        		hsOa.add(sOaId);
	        		break;
	        	}
	        }
	    }
		return hsOa;
	  }
	  
	  
	  // ShowAccessibleObjects() computes the objects that are accessible to a given user,
	  // together with the operations permitted on each object.
	  private Object showAccessibleObjects(String sSessId, String sUserName,
			  String sUserId, String sUserType) throws Exception {
		if (bMellPrint) {
			System.out.println("ShowAccessibleObjects called with arguments:");
			System.out.println("  session id: " + sSessId);
			System.out.println("  User name:" + sUserName);
			System.out.println("  User id: " + sUserId);
			System.out.println("  User type:" + sUserType);
		}
		
		if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value)) return failurePacket("ShowAccessibleObjects only applicable to users!");
	    String sId = getEntityId(sUserName, sUserType);
	    if (sId == null) return failurePacket("No such user!");
	    if (sUserId == null) sUserId = sId;
	    else if (!sId.equalsIgnoreCase(sUserId)) return failurePacket("Inconsistent user id!");

	    Hashtable htResult = showAccessibleObjectsInternal(sUserName, sUserId);

	    if (htResult == null) return failurePacket();
	    Packet res = new Packet();
	    try {
		    for (Enumeration objs = htResult.keys(); objs.hasMoreElements(); ) {
		    	String sOId = (String)objs.nextElement();
		    	String sOName = getEntityName(sOId, PM_NODE.OATTR.value);
		    	StringBuilder sb = new StringBuilder();
		    	sb.append(sOId);
		    	sb.append(GlobalConstants.PM_FIELD_DELIM);
		    	sb.append(sOName);
		    	sb.append(GlobalConstants.PM_FIELD_DELIM);
		    	
		    	HashSet hsOps = (HashSet)htResult.get(sOId);
		    	Iterator hsiter = hsOps.iterator();
		    	boolean bFirst = true;
		 	    while (hsiter.hasNext()) {
		 	    	String sOp = (String)hsiter.next();
		 	    	if (bFirst) {
		 	    		sb.append(sOp);
		 	    		bFirst = false;
		 	    	} else {
		 	    		sb.append(GlobalConstants.PM_LIST_MEMBER_SEP + sOp);
		 	    	}
		 	    }
		    	
		    	res.addItem(ItemType.RESPONSE_TEXT, sb.toString());
		    }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return failurePacket();
	    }
	    return res;
	  }
	  
	  private Hashtable showAccessibleObjectsInternal(String sUserName, String sUserId) throws Exception {
		// The argument is a user u.
		if (bMellPrint) {
			System.out.println("showAccessibleObjectsInternal called with arguments:");
			System.out.println("  user name: " + sUserName);
			System.out.println("  user id:" + sUserId);	
		}

		long lStart = System.nanoTime();
		// Prepare the Hashtable to return, where the key is an object and
		// the corresponding value is a HashSet of all privileges of the user
		// on the key object.
		Hashtable htResult = new Hashtable();
		
		// Call find_border_oa_priv(u). The result is a Hashtable
		// htoa = {oa -> {op -> pcset}}:
		Hashtable htOa = findBorderOaPrivRestrictiveInternal(sUserName, sUserId);
		
		// Intermediary Hashtable for objects and labels: htObjects = {o -> {op -> pcset}}.
		// The label is a set of mappings {op -> pcset} extracted from all oa
		// with o ->* oa.
		Hashtable htObjects = new Hashtable();
		
		try {
			// For each oa in htOa.keys
			for (Enumeration oas = htOa.keys(); oas.hasMoreElements(); ) {
				String sOaId = (String)oas.nextElement();
			
				// Extract oa's label:
				Hashtable htOaLabel = (Hashtable)htOa.get(sOaId);
			
				// Starting with oa, do a BFS backwards to find all objects o
				// such that o ->* oa.
				// Prepare a queue.
				ArrayList queue = new ArrayList();
				HashSet visited = new HashSet();
				String sCrtOaId;
			
				// Prepare a temporary set for objects o with o ->* oa:
				HashSet hsO = new HashSet();
			
				Attributes attrs;
				Attribute attr;

				// Insert oa into the queue (it may be an object itself).
				queue.add(sOaId);
				if (bMellPrint) {
					System.out.println("For oa " + getEntityName(sOaId, PM_NODE.OATTR.value) +
						" we found the objects ");
				}
				
				// While the queue has elements, extract an element from the queue
				// and visit it.
				while (!queue.isEmpty()) {
					sCrtOaId = (String)queue.remove(0);
					if (visited.contains(sCrtOaId)) continue;
					// VISIT the element.
					if (hasAssocObj(sCrtOaId)) {
						hsO.add(sCrtOaId);
						if (bMellPrint) System.out.println("    " + getEntityName(sCrtOaId, PM_NODE.OATTR.value));
					}
					visited.add(sCrtOaId);
					attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtOaId + "," + sObjAttrContainerDN);
					attr = attrs.get("pmFromAttr");
					if (attr != null)
						for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
							String sAttrId = (String)enumer.next();
							queue.add(sAttrId);
							
						}
				}
				
				// Label each discovered object o (i.e., such that o ->* oa)
				// with the (access right, pcs) from the oa node, as calculated
				// by the findBorderOaPriv() method. The labeling will be done
				// in the Hashtable htObjects. Thus,
				// For each o in hsO
				Iterator hsiter = hsO.iterator();
				while (hsiter.hasNext()) {
					String sOId = (String)hsiter.next();
					
					// If o is already in htObjects.keys
					if (htObjects.containsKey(sOId)) {
						// Extract o's label from htObjects
						Hashtable htOLabel = (Hashtable)htObjects.get(sOId);
						// For each operation op in htOaLabel.keys
			            for (Enumeration lbl = htOaLabel.keys(); lbl.hasMoreElements();) {
			            	String sOp = (String)lbl.nextElement();
			            	// Get corresponding pcset in htOaLabel
			            	HashSet hsOaPcset = (HashSet)htOaLabel.get(sOp);
			            	// If the o's label already contains op -> some pcset
			            	if (htOLabel.containsKey(sOp)) {
			            		// Add the oa's pcset to the pcset in o's label
			            		HashSet hsOPcset = (HashSet)htOLabel.get(sOp);
			            		hsOPcset.addAll(hsOaPcset);
			            	} else {
			            		// op is a new operation for o. Add op -> oa pcset for op
			            		// to o's label
			            		HashSet hsNewPcs = new HashSet(hsOaPcset);
			            		htOLabel.put(sOp,  hsNewPcs);
			            	}
			            }
					} else {
						// o is a new object, is not in htObjects.keys.
						// Create a new label for o.
						Hashtable htOLabel = new Hashtable();
						
						// For each operation op in htOaLabel.keys
			            for (Enumeration lbl = htOaLabel.keys(); lbl.hasMoreElements();) {
			            	String sOp = (String)lbl.nextElement();
			            	
			            	// Insert op -> oa pcset for op into o's label.
			            	HashSet hsOaPcset = (HashSet)htOaLabel.get(sOp);
			            	HashSet hsNewPcs = new HashSet(hsOaPcset);
			            	htOLabel.put(sOp,  hsNewPcs);
			            }
			            // Insert o and its new label into htObjects
			            htObjects.put(sOId, htOLabel);
					}
				}
				
			}// Next oa.
		
			if (bMellPrint) {
				// Print htObjects.
				System.out.println("HTOBJECTS");
			    for (Enumeration keys = htObjects.keys(); keys.hasMoreElements() ;) {
			        String sKey = (String)keys.nextElement();
			        String sName = getEntityName(sKey, PM_NODE.OATTR.value);
			        //System.out.println("Object " + sName);
			        System.out.println(sName + " ---> ");
			        
			        Hashtable htLabel = (Hashtable)htObjects.get(sKey);
			        for (Enumeration ops = htLabel.keys(); ops.hasMoreElements(); ) {
			        	String sOp = (String)ops.nextElement();
			        	System.out.print("    " + sOp + " ---> ");
			        
			        	HashSet hsPcs = (HashSet)htLabel.get(sOp);
			        	//System.out.println("    Policies");
			        	Iterator hsiter = hsPcs.iterator();
			     	    while (hsiter.hasNext()) {
			     	    	String sPcId = (String)hsiter.next();
			     	    	System.out.print(getEntityName(sPcId, PM_NODE.POL.value) + ", ");
			     	    }
			     	    System.out.println();
			        }
			    }
			}
		    
		    // For each discovered object do BFS to find a set of required PCs.
		    // In other words, for each o in htObjects, find pcset = {pc | o ->+ pc}.
		    for (Enumeration keys = htObjects.keys(); keys.hasMoreElements() ;) {
		        String sOId = (String)keys.nextElement();
		        HashSet hsReqPcs = findPcSet(sOId, PM_NODE.OATTR.value);
		        // Prepare the set of access rights for this object.
		        HashSet hsORights = new HashSet();
		        // Get the object label and traverse the label's keys (access rights).
		        Hashtable htOLabel = (Hashtable)htObjects.get(sOId);
		        // For each operation/access right
		        for (Enumeration ops = htOLabel.keys(); ops.hasMoreElements(); ) {
		        	String sOp = (String)ops.nextElement();
		        	// Extract the pcset corresponding to this operation/access right.
		        	HashSet hsActualPcs = (HashSet)htOLabel.get(sOp);
		        	// if the set of required PCs is a subset of the actual pcset,
		        	// then add the operation to the set of access rights for this object.
		        	if (hsActualPcs.containsAll(hsReqPcs)) {
		        		hsORights.add(sOp);
		        	}
		        }
		        // If the set of access rights for this object is not empty, add
		        // o -> operations to the result.
		        if (!hsORights.isEmpty()) htResult.put(sOId, hsORights);
		    }
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		// Print htResult.
		if (bMellPrint) {
		    System.out.println("***FINAL RESULT***");
			for (Enumeration objs = htResult.keys(); objs.hasMoreElements(); ) {
		    	String sOId = (String)objs.nextElement();
		    	System.out.print("  " + getEntityName(sOId, PM_NODE.OATTR.value) + " ---> ");
		    	// Extract the value corresponding to this object.
		    	HashSet hsOps = (HashSet)htResult.get(sOId);
		    	Iterator hsiter = hsOps.iterator();
		 	    while (hsiter.hasNext()) {
		 	    	String sOp = (String)hsiter.next();
		 	    	System.out.print(sOp + ", ");
		 	    }
		 	    System.out.println();
		    }
		}
		long lDuration = System.nanoTime() - lStart;
		System.out.print("ShowAccessibleObjectsInternal duration (nanoseconds) ");
		System.out.println(lDuration);
		return htResult;
	  }
	  
	  // Mell: Compute the operations permitted to a user on an object (attribute).
	  private Object calcPriv(String sSessId, String sUserName, String sUserId,
			  String sUserType, String sTgtName, String sTgtId, String sTgtType) throws Exception {
		if (bMellPrint) {
			System.out.println("CalcPriv called with arguments:");
			System.out.println("  session id: " + sSessId);
			System.out.println("  User name:" + sUserName);
			System.out.println("  User id: " + sUserId);
			System.out.println("  User type:" + sUserType);
			System.out.println("  Target name:" + sTgtName);
			System.out.println("  Target id: " + sTgtId);
			System.out.println("  Target type:" + sTgtType);
		}

	    if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value))
	    	return failurePacket("CalcPriv called with first argument NOT a user!");
		String sUId = getEntityId(sUserName, PM_NODE.USER.value);
	    if (sUId == null) return failurePacket("No such user!");
	    if (sUserId == null)
	    	sUserId = sUId;
	    else if (!sUserId.equalsIgnoreCase(sUId))
	    	return failurePacket("Inconsistent user id!");
	    
		if (sTgtType.equalsIgnoreCase(PM_NODE.ASSOC.value)) sTgtType = PM_NODE.OATTR.value;
		if (!sTgtType.equalsIgnoreCase(PM_NODE.OATTR.value))
			return failurePacket("Selected entity is not an object attribute!");
		String sOaId = getEntityId(sTgtName, sTgtType);
	    if (sOaId == null) return failurePacket("No such object attribute!");
	    if (!sOaId.equalsIgnoreCase(sTgtId)) return failurePacket("Inconsistent object attribute id!");
	    
	    HashSet hsOps = calcPrivInternal(sUserName, sUserId, sTgtName, sTgtId);
	    Packet res = new Packet();
	    if (hsOps != null) {
	    	for (Iterator hsiter = hsOps.iterator(); hsiter.hasNext(); ) {
	    		String sOp = (String)hsiter.next();
	    		try {
	    			res.addItem(ItemType.RESPONSE_TEXT, (String)sOp);
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			return failurePacket();
	    		}
	    	}
	    	return res;
	    }
	    return failurePacket();
	  }
	  
	  // CalcPrivInternal calculates a user's permitted operations on an
	  // object attribute/object. No checks.
	  private HashSet calcPrivInternal(String sUserName, String sUserId, String sOaName, String sOaId) {
		if (bMellPrint) {
			System.out.println("CalcPrivInternal called with arguments:");
			System.out.println("  user name: " + sUserName);
			System.out.println("  user id:" + sUserId);
			System.out.println("  oa name: " + sOaName);
			System.out.println("  oa id:" + sOaId);
		}
		long lStart = System.nanoTime();
		
		// Prepare the table with u's permitted ops on the object attribute oa.
		HashSet hsOps = new HashSet();
		  
		// Get the border nodes (i.e., the object attributes reachable from u).
		Hashtable htReachableOas = findBorderOaPrivRestrictiveInternal(sUserName, sUserId);
		// The border nodes are the keys of htReachableOas.
		  
		// Do a BFS from argument oa to find border nodes reachable from oa,
		// i.e., compute:
		// reachable = { x | x is oattr and oa ->* x and x is in htReachableOas}.
		HashSet reachable = new HashSet();

		// Prepare oa label, initially empty.
		Hashtable htOaLabel = new Hashtable();

		// Prepare the queue.
		ArrayList queue = new ArrayList();
		HashSet visited = new HashSet();
		String sCrtId;
		  
		Attributes attrs;
		Attribute attr;
		try {
			// Insert oa into the queue.
			queue.add(sOaId);
			  
			// while the queue has elements, extract an element from the queue
			// and visit it.
			while (!queue.isEmpty()) {
				// extract
				sCrtId = (String)queue.remove(0);
				if (bMellPrint) System.out.println("Attribute " + getEntityName(sCrtId, PM_NODE.OATTR.value)
						  + " has been removed from the queue");
				if (!visited.contains(sCrtId)) {
					if (bMellPrint) System.out.println("Attribute " + getEntityName(sCrtId, PM_NODE.OATTR.value)
							  + " is being visited");
					// Visit sCrtId.
					// If the current oattr sCrtId is in htReachableOas,
					// include it into reachable.
					if (htReachableOas.containsKey(sCrtId)) reachable.add(sCrtId);
					  
					// Set the crt element as having been visited.
					visited.add(sCrtId);
					// Find and insert its descendants into the queue.
					attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtId + "," + sObjAttrContainerDN);
					attr = attrs.get("pmToAttr");
					if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
						String sAttrId = (String)enumer.next();
						queue.add(sAttrId);
						if (bMellPrint) System.out.println("Child attribute " + getEntityName(sAttrId, PM_NODE.OATTR.value)
								  + " has been inserted into the queue");
					}
				}
			}
			// Let's print reachable.
			if (bMellPrint) printSet(reachable, PM_NODE.OATTR.value, "OAs reachable from "
			    		+ sOaName + " and from " + sUserName);
			
			// Label oa with the labels of all object attributes x in reachable.
			// Prepare the oa label.

			// For each x in reachable
			Iterator iter = reachable.iterator();
			while (iter.hasNext()) {
				String sXId = (String)iter.next();
				if (bMellPrint) System.out.println("Current reachable x is: " + getEntityName(sXId, PM_NODE.OATTR.value));
				// Extract x's label from htReachableOas:
				Hashtable htXLabel = (Hashtable)htReachableOas.get(sXId);
				// Walk through the x's label's associations op ---> pcset.
				for (Enumeration ops = htXLabel.keys(); ops.hasMoreElements(); ) {
					String sOp = (String)ops.nextElement();// x's op
					HashSet hsXPcset = (HashSet)htXLabel.get(sOp);// x's pcset
					if (htOaLabel.containsKey(sOp)) {
						// If x's op is already in oa's label,...
						HashSet hsOaPcset = (HashSet)htOaLabel.get(sOp);
						// Just add x's pcset to oa's pcset for that op.
						hsOaPcset.addAll(hsXPcset);
					} else {
						// If x's op is not in oa's label yet,
						// make a copy of x's pcset and...
						HashSet hsOaPcset = new HashSet(hsXPcset);
						// Insert the association x's op ---> copy of x's pcset
						// into oa's label.
						htOaLabel.put(sOp, hsOaPcset);
					}
				}
			}
			
			// BFS from oa to find oa's reachable pcs.
			HashSet hsOaReachablePcs = findPcSet(sOaId, PM_NODE.OATTR.value);
			if (bMellPrint) printSet(hsOaReachablePcs, PM_NODE.POL.value, "Policy Classes reachable from "
					+ getEntityName(sOaId, PM_NODE.OATTR.value));
			
			// For each op ---> pcset in oa's label
			for (Enumeration ops = htOaLabel.keys(); ops.hasMoreElements(); ) {
				String sOp = (String)ops.nextElement();// oa's op
				HashSet hsOaPcset = (HashSet)htOaLabel.get(sOp);// oa's pcset
				
				// If the pcset in the oa's label includes the pcs reachable
				// from oa, then the operation in the oa's label is permitted on oa.
				if (hsOaPcset.containsAll(hsOaReachablePcs)) hsOps.add(sOp);
			}
			if (bMellPrint) printSet(hsOps, GlobalConstants.PM_PERM, "Operations permitted on "
					+ getEntityName(sOaId, PM_NODE.OATTR.value)
					+ " to user " + sUserName);
			
		} catch (Exception e) {
		  e.printStackTrace();
		  return null;
		}
		long lDuration = System.nanoTime() - lStart;
		System.out.print("CalcPrivInternal duration (nanoseconds) ");
		System.out.println(lDuration);

		return hsOps;
	  }
		  
	  // Peter Mell: This function has a user u as argument. It returns all oa nodes that
	  // are successors of ua nodes (for ua nodes reachable from u). It labels each
	  // returned node with the reachable PC nodes paired with the access rights
	  // conferred by the ua -> oa edges.
	  //
	  // Step 1. BFS from u to find the set of ua nodes that have ua -> oa edges
	  // (do not traverse any ua -> oa edges).
	  //
	  // Step 2. For each discovered ua node, traverse all ua -> oa edges. For each
	  // edge, label the oa node with the access rights conferred by the edge. An oa
	  // node may end up with multiple access rights - consolidate duplicates.
	  //
	  // Step 3. For each reached oa node, x, execute find_pc_set(x) to find the set
	  // of reachable PC nodes. Pair each access right with each reached PC node to
	  // create access right, PC node pairings.
	  //
	  // Step 4. Return the set of reachable oa nodes with their respective
	  // access right/pc node pairings.
	  //
	  // In the implementation, the base is the user, and the session and graph type
	  // are not used.
	  private Object findBorderOaPrivRelaxed(String sSessId, String sUserName, String sUserId,
			  String sUserType) throws Exception {
		if (bMellPrint) {
			System.out.println("FindBorderOaPrivRelaxed called with arguments:");
			System.out.println("  session id: " + sSessId);
			System.out.println("  User name:" + sUserName);
			System.out.println("  User id: " + sUserId);
			System.out.println("  User type:" + sUserType);
		}

		if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value)) return failurePacket("FindBorderOaPrivRelaxed only applicable to users!");
	    String sId = getEntityId(sUserName, sUserType);
	    if (sId == null) return failurePacket("No such user!");
	    if (sUserId == null) sUserId = sId;
	    else if (!sId.equalsIgnoreCase(sUserId)) return failurePacket("Inconsistent user id!");

	    Hashtable htReachableOas = findBorderOaPrivRelaxedInternal(sUserName,
	    		sUserId, sUserType);
	    return ADPacketHandler.getSuccessPacket();
	  }
	  
	  // FindBorderOaPrivRestrictive uses the restrictive definition of a privilege.
	  //
	  private Object findBorderOaPrivRestrictive(String sSessId, String sUserName,
			  String sUserId, String sUserType) throws Exception {
		if (bMellPrint) {
			System.out.println("FindBorderOaPrivRestrictive called with arguments:");
			System.out.println("  session id: " + sSessId);
			System.out.println("  User name:" + sUserName);
			System.out.println("  User id: " + sUserId);
			System.out.println("  User type:" + sUserType);
		}

		if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value)) return failurePacket("FindBorderOaPrivRelaxed only applicable to users!");
	    String sId = getEntityId(sUserName, sUserType);
	    if (sId == null) return failurePacket("No such user!");
	    if (sUserId == null) sUserId = sId;
	    else if (!sId.equalsIgnoreCase(sUserId)) return failurePacket("Inconsistent user id!");

	    Hashtable htReachableOas = findBorderOaPrivRestrictiveInternal(sUserName, sUserId);
	    return ADPacketHandler.getSuccessPacket();
	  }
	  
	  private Hashtable findBorderOaPrivRelaxedInternal(String sUserName, String sUserId, String sUserType) {
		    if (bMellPrint) System.out.println("findBorderOaPrivRelaxedInternal");
		    // Uses a hashtable htReachableOas of reachable oas (see find_border_oa_priv(u))
		    // An oa is a key in this hashtable. The value is another hashtable that
		    // represents a label of the oa. A label is a set of pairs {(op -> pcset), with
		    // the op being the key and pcset being the value.
		    // {oa -> {op -> pcset}}.
		    Hashtable htReachableOas = new Hashtable();

		    // BFS from u (the base node). Prepare a queue.
		    ArrayList queue = new ArrayList();
		    HashSet visited = new HashSet();
		    String sCrtId;
		    
		    Attributes attrs;
		    Attribute attr;
		    try {
		        // Insert u's directly assigned attributes into the queue.
		    	attrs = ServerConfig.ctx.getAttributes("CN=" + sUserId + "," + sUserContainerDN);
		    	attr = attrs.get("pmToAttr");
		    	if (attr == null) return null;
		    	
		    	for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
		    		String sAttrId = (String)enumer.next();
		    		queue.add(sAttrId);
		    		if (bMellPrint) System.out.println("Attribute " + getEntityName(sAttrId, PM_NODE.UATTR.value)
		    				+ " has been inserted into the queue");
		    	}

		    	// While the queue has elements, extract an element from the queue
		    	// and visit it.
		    	while (!queue.isEmpty()) {
		    		// Extract an ua from queue.
		    		sCrtId = (String)queue.remove(0);

		    		if (!visited.contains(sCrtId)) {
		    			if (bMellPrint) System.out.println("User attribute "+ getEntityName(sCrtId, PM_NODE.UATTR.value)
		        				+ " is being visited");
		    			
		    			// If the ua has ua -> oa edges
		    			if (attrHasOpsets(sCrtId, PM_NODE.UATTR.value)) {
		    				if (bMellPrint) System.out.println("User attribute " + getEntityName(sCrtId, PM_NODE.UATTR.value)
		    						+ " has opsets!");
		    			    // From each discovered ua traverse the edges ua -> oa.
		    			    // Find the opsets of this user attribute.
		    			    Vector opsets = getToOpsets(sCrtId);
		    			    // For each opset ops of ua:
		        			for (int ops = 0; ops < opsets.size(); ops++) {
		    			    	String sOpsetId = (String)opsets.elementAt(ops);
		    			    	if (bMellPrint) System.out.println("  Found opset " + sOpsetId
		    			    			+ " = " + getEntityName(sOpsetId, PM_NODE.OPSET.value));
		    			    	// Find the object attributes of this opset.
		    			    	Vector oattrs = getToAttrs(sOpsetId);
		    			    	// For each object attribute of this opset
		    			    	for (int oa = 0; oa < oattrs.size(); oa++) {
		    			    		String sOaId = (String)oattrs.elementAt(oa);
		    			    		if (bMellPrint) System.out.println("    Found oattr " + sOaId
		    			    				+ " = " + getEntityName(sOaId, PM_NODE.OATTR.value));
		    			    		// Compute the PC nodes reachable from oa.
		    			    		HashSet hsPcs = findPcSet(sOaId, PM_NODE.OATTR.value);
		    			    		// If oa is in htReachableOas
		    			    		if (htReachableOas.containsKey(sOaId)) {
		    			    			// oa has a label op1 -> hsPcs, op2 -> hsPcs,...
		    			    			// Extract its label
		    			    			Hashtable htOaLabel = (Hashtable)htReachableOas.get(sOaId);
		    			    			if (bMellPrint) System.out.println("This oa is already reachable");
		    			    			// Get the operations from the opset.
		    			    			Vector opers = getOpsetOperations(sOpsetId);
		    			    			// For each operation in the opset
		    			    			for (int op = 0; op < opers.size(); op++) {
		    			    				String sOp = (String)opers.elementAt(op);
		    			    				if (bMellPrint) System.out.println("      Found operation " + sOp);
		    			    				// If the oa's label already contains the op
		    			    				if (htOaLabel.containsKey(sOp)) {
		    			    					if (bMellPrint) System.out.println("        Already in; don't do anything!");
		    			    				} else {// The op is not in the label. Create new op -> pcs pair in label.
		    			    					if (bMellPrint) System.out.println("        New op; create new op -> pcs pair in label");
		    			    					HashSet hsNewPcs = new HashSet(hsPcs);
		    			    					htOaLabel.put(sOp, hsNewPcs);
		    			    				}
		    			    			}
		    			    		} else {// oa is not in htReachableOas
		    			    			if (bMellPrint) System.out.println("This oa is not yet reachable");
		    			    			// Prepare a new label
		    			    			Hashtable htOaLabel = new Hashtable();
		    			    			// Get the operations from the opset.
		    			    			Vector opers = getOpsetOperations(sOpsetId);
		    			    			// For each operation in the opset add op -> pcs to the label.
		    			    			for (int op = 0; op < opers.size(); op++) {
		    			    				String sOp = (String)opers.elementAt(op);
		    			    				if (bMellPrint) {
		    			    					System.out.println("      Operation " + sOp);
			    			    				System.out.println("        Create op -> pcs");
		    			    				}
		    			    				HashSet hsNewPcs = new HashSet(hsPcs);
		    			    				htOaLabel.put(sOp,  hsNewPcs);
		    			    			}
		    			    			// Add oa -> {op -> pcs}
		    			    			htReachableOas.put(sOaId, htOaLabel);
		    			    		}
		    			    	}
		    			    		
		    			    }
		    			    
		    			}
		    			
		    			visited.add(sCrtId);
		    			attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtId + "," + sUserAttrContainerDN);
		    			
		    			attr = attrs.get("pmToAttr");
		    			if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
		    				String sAttrId =  (String)enumer.next();
		    				queue.add(sAttrId);
		    				if (bMellPrint) System.out.println("Child attribute "+ getEntityName(sAttrId, PM_NODE.UATTR.value)
		    						+ "has been inserted into the queue");
		    			}
		    		}    		
		    	}
		    	
		    	// Print htReachableOas.
		    	if (bMellPrint) {
			    	System.out.println("TABLE OF REACHABLE OAS");
			        for (Enumeration keys = htReachableOas.keys(); keys.hasMoreElements() ;) {
			            String sKey = (String)keys.nextElement();
			            String sName = getEntityName(sKey, PM_NODE.OATTR.value);
			            //System.out.println("Object attribute " + sName);
			            System.out.println(sName + " ---> ");
			            
			            Hashtable htLabel = (Hashtable)htReachableOas.get(sKey);
			            for (Enumeration ops = htLabel.keys(); ops.hasMoreElements(); ) {
			            	String sOp = (String)ops.nextElement();
			            	System.out.print("    " + sOp + " ---> ");
			            
			            	HashSet hsPcs = (HashSet)htLabel.get(sOp);
			            	//System.out.println("    Policies");
			            	Iterator hsiter = hsPcs.iterator();
			         	    while (hsiter.hasNext()) {
			         	    	String sPcId = (String)hsiter.next();
			         	    	System.out.print(getEntityName(sPcId, PM_NODE.POL.value) + ", ");
			         	    }
			         	    System.out.println();
			            }
			        }
		    	}

		    } catch (Exception e) {
		        e.printStackTrace();
		        return null;
		    } 

		    return htReachableOas;
		  }

	  private Hashtable findBorderOaPrivRestrictiveInternal(String sUserName, String sUserId) {
		    if (bMellPrint) System.out.println("findBorderOaPrivRestrictiveInternal");
		    // Uses a hashtable htReachableOas of reachable oas (see find_border_oa_priv(u))
		    // An oa is a key in this hashtable. The value is another hashtable that
		    // represents a label of the oa. A label is a set of pairs {(op -> pcset)}, with
		    // the op being the key and pcset being the value.
		    // {oa -> {op -> pcset}}.
		    Hashtable htReachableOas = new Hashtable();

		    // BFS from u (the base node). Prepare a queue.
		    ArrayList queue = new ArrayList();
		    HashSet visited = new HashSet();
		    String sCrtId;
		    
		    Attributes attrs;
		    Attribute attr;
		    try {
		        // Insert u's directly assigned attributes into the queue.
		    	attrs = ServerConfig.ctx.getAttributes("CN=" + sUserId + "," + sUserContainerDN);
		    	attr = attrs.get("pmToAttr");
		    	if (attr == null) return null;
		    	
		    	for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
		    		String sAttrId = (String)enumer.next();
		    		queue.add(sAttrId);
		    		if (bMellPrint) System.out.println("Attribute " + getEntityName(sAttrId, PM_NODE.UATTR.value)
		    				+ " has been inserted into the queue");
		    	}

		    	// While the queue has elements, extract an element from the queue
		    	// and visit it.
		    	while (!queue.isEmpty()) {
		    		// Extract an ua from queue.
		    		sCrtId = (String)queue.remove(0);

		    		if (!visited.contains(sCrtId)) {
		    			if (bMellPrint) System.out.println("User attribute "+ getEntityName(sCrtId, PM_NODE.UATTR.value)
		        				+ " is being visited");
		    			
		    			// If the ua has ua -> oa edges
		    			if (attrHasOpsets(sCrtId, PM_NODE.UATTR.value)) {
		    				if (bMellPrint) System.out.println("User attribute " + getEntityName(sCrtId, PM_NODE.UATTR.value)
		    						+ " has opsets!");
		    				
		    				// Find the set of PCs reachable from ua.
		    				HashSet hsUaPcs = findPcSet(sCrtId, PM_NODE.UATTR.value);
		    				if (bMellPrint) printSet(hsUaPcs, PM_NODE.POL.value, "Policies reachable from " +
		    				    getEntityName(sCrtId, PM_NODE.UATTR.value));
		    				    
		    			    // From each discovered ua traverse the edges ua -> oa.
		    			    // Find the opsets of this user attribute.
		    			    Vector opsets = getToOpsets(sCrtId);
		    			    // For each opset ops of ua:
		        			for (int ops = 0; ops < opsets.size(); ops++) {
		    			    	String sOpsetId = (String)opsets.elementAt(ops);
		    			    	if (bMellPrint) System.out.println("  Found opset " + sOpsetId
		    			    			+ " = " + getEntityName(sOpsetId, PM_NODE.OPSET.value));
		    			    	// Find the object attributes of this opset.
		    			    	Vector oattrs = getToAttrs(sOpsetId);
		    			    	// For each object attribute of this opset
		    			    	for (int oa = 0; oa < oattrs.size(); oa++) {
		    			    		String sOaId = (String)oattrs.elementAt(oa);
		    			    		if (bMellPrint) System.out.println("    Found oattr " + sOaId
		    			    				+ " = " + getEntityName(sOaId, PM_NODE.OATTR.value));

		    			    		// If oa is in htReachableOas
		    			    		if (htReachableOas.containsKey(sOaId)) {
		    			    			// oa has a label op1 -> hsPcs1, op2 -> hsPcs2,...
		    			    			// Extract its label
		    			    			Hashtable htOaLabel = (Hashtable)htReachableOas.get(sOaId);
		    			    			if (bMellPrint) System.out.println("This oa is already reachable");
		    			    			// Get the operations from the opset.
		    			    			Vector opers = getOpsetOperations(sOpsetId);
		    			    			// For each operation in the opset
		    			    			for (int op = 0; op < opers.size(); op++) {
		    			    				String sOp = (String)opers.elementAt(op);
		    			    				if (bMellPrint) System.out.println("      Found operation " + sOp);
		    			    				// If the oa's label already contains the op
		    			    				if (htOaLabel.containsKey(sOp)) {
		    			    					// The label contains op -> some pcset.
		    			    					if (bMellPrint) System.out.println("        Op already in label. Union old pcs with the ua's pcs!");
		    			    					// Add the ua's pcset to it.
		    			    					HashSet hsPcs = (HashSet)htOaLabel.get(sOp);
		    			    					hsPcs.addAll(hsUaPcs);
		    			    				} else {// The op is not in the label. Create new op -> ua's pcs pair in label.
		    			    					if (bMellPrint) System.out.println("        New op; create new op -> ua's pcs pair in label");
		    			    					HashSet hsNewPcs = new HashSet(hsUaPcs);
		    			    					htOaLabel.put(sOp, hsNewPcs);
		    			    				}
		    			    			}
		    			    		} else {// oa is not in htReachableOas
		    			    			if (bMellPrint) System.out.println("This oa is not yet reachable");
		    			    			// Prepare a new label
		    			    			Hashtable htOaLabel = new Hashtable();
		    			    			// Get the operations from the opset.
		    			    			Vector opers = getOpsetOperations(sOpsetId);
		    			    			// For each operation in the opset add op -> pcs to the label.
		    			    			for (int op = 0; op < opers.size(); op++) {
		    			    				String sOp = (String)opers.elementAt(op);
		    			    				if (bMellPrint) System.out.println("      Operation " + sOp);
		    			    				if (bMellPrint) System.out.println("        Create op -> pcs");
		    			    				HashSet hsNewPcs = new HashSet(hsUaPcs);
		    			    				htOaLabel.put(sOp,  hsNewPcs);
		    			    			}
		    			    			// Add oa -> {op -> pcs}
		    			    			htReachableOas.put(sOaId, htOaLabel);
		    			    		}
		    			    		
		    			    	}
		    	
		    			    }
		    			    
		    			}
		    			
		    			visited.add(sCrtId);
		    			attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtId + "," + sUserAttrContainerDN);
		    			
		    			attr = attrs.get("pmToAttr");
		    			if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
		    				String sAttrId =  (String)enumer.next();
		    				queue.add(sAttrId);
		    				if (bMellPrint) System.out.println("Child attribute "+ getEntityName(sAttrId, PM_NODE.UATTR.value)
		    						+ " has been inserted into the queue");
		    			}
		    		}    		
		    	}

		    	// Print htReachableOas.
		    	if (bMellPrint) {
		    		System.out.println("TABLE OF REACHABLE OAS BEFORE");
			        for (Enumeration keys = htReachableOas.keys(); keys.hasMoreElements() ;) {
			            String sKey = (String)keys.nextElement();
			            String sName = getEntityName(sKey, PM_NODE.OATTR.value);
			            //System.out.println("Object attribute " + sName);
			            System.out.println(sName + " ---> ");
			            
			            Hashtable htLabel = (Hashtable)htReachableOas.get(sKey);
			            for (Enumeration ops = htLabel.keys(); ops.hasMoreElements(); ) {
			            	String sOp = (String)ops.nextElement();
			            	System.out.print("    " + sOp + " ---> ");
			            
			            	HashSet hsPcs = (HashSet)htLabel.get(sOp);
			            	//System.out.println("    Policies");
			            	Iterator hsiter = hsPcs.iterator();
			         	    while (hsiter.hasNext()) {
			         	    	String sPcId = (String)hsiter.next();
			         	    	System.out.print(getEntityName(sPcId, PM_NODE.POL.value) + ", ");
			         	    }
			         	    System.out.println();
			            }
			        }
		    	}
		    	
		    	// For each reachable oa in htReachableOas.keys
		        for (Enumeration keys = htReachableOas.keys(); keys.hasMoreElements() ;) {
		            String sOaId = (String)keys.nextElement();
		            // Compute {pc | oa ->+ pc}
		            HashSet hsOaPcs = findPcSet(sOaId, PM_NODE.OATTR.value);
		            // Extract oa's label.
		            Hashtable htOaLabel = (Hashtable)htReachableOas.get(sOaId);
		            // The label contains op1 -> pcs1, op2 -> pcs2,...
		            // For each operation in the label
		            for (Enumeration lbl = htOaLabel.keys(); lbl.hasMoreElements();) {
		            	String sOp = (String)lbl.nextElement();
		            	// Intersect the pcset corresponding to this operation,
		            	// which comes from the uas, with the oa's pcset.
		            	HashSet oaPcs = (HashSet)htOaLabel.get(sOp);
		            	oaPcs.retainAll(hsOaPcs);
		            	if (oaPcs.isEmpty()) htOaLabel.remove(sOp);
		            }
		        }
		    	
		    	// Print htReachableOas.
		        if (bMellPrint) {
		        	System.out.println("TABLE OF REACHABLE OAS AFTER");
			        for (Enumeration keys = htReachableOas.keys(); keys.hasMoreElements() ;) {
			            String sKey = (String)keys.nextElement();
			            String sName = getEntityName(sKey, PM_NODE.OATTR.value);
			            //System.out.println("Object attribute " + sName);
			            System.out.println(sName + " ---> ");
			            
			            Hashtable htLabel = (Hashtable)htReachableOas.get(sKey);
			            for (Enumeration ops = htLabel.keys(); ops.hasMoreElements(); ) {
			            	String sOp = (String)ops.nextElement();
			            	System.out.print("    " + sOp + " ---> ");
			            
			            	HashSet hsPcs = (HashSet)htLabel.get(sOp);
			            	//System.out.println("    Policies");
			            	Iterator hsiter = hsPcs.iterator();
			         	    while (hsiter.hasNext()) {
			         	    	String sPcId = (String)hsiter.next();
			         	    	System.out.print(getEntityName(sPcId, PM_NODE.POL.value) + ", ");
			         	    }
			         	    System.out.println();
			            }
			        }
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		        return null;
		    } 

		    return htReachableOas;
		  }

	  

	  // The first argument is a user attribute or an object attribute.
	  // The second attribute is the type of first argument.
	  // The function returns the set of pc nodes reachable from the start node.
	  // Method: Initialize the set to be returned with the empty set.
	  // Do a BFS from the start node (do not traverse the ua -> oa edges).
	  // When you visit a node, add all pcs it points to to the set to be returned,
	  // if not already there.
	  private HashSet findPcSet(String sStartNodeId, String sAttrType) throws Exception {
		  // The set of pcs reachable from the start node.
		  HashSet reachable = new HashSet();
	      
		  // Initialize the queue and visited.
	      ArrayList queue = new ArrayList();
	      HashSet visited = new HashSet();
	      
	      // The current element.
	      String sCrtId = null;
	      
	      // The attribute container
	      String sAttrContainer = null;
	      if (sAttrType.equalsIgnoreCase(PM_NODE.UATTR.value))
	    	  sAttrContainer = sUserAttrContainerDN;
	      else
	    	  sAttrContainer = sObjAttrContainerDN;
	      // Insert the start node into the queue.
	      queue.add(sStartNodeId);
	      if (bMellPrint) System.out.println("User or object attribute "
	    		  + getEntityName(sStartNodeId, sAttrType) +
	    		  " has been inserted into the queue");
	      
	      try {
	    	  // while queue is not empty
	    	  while (!queue.isEmpty()) {
	    		  // Extract current element from the queue.
	    		  sCrtId = (String)queue.remove(0);
	    		  // If not visited
	    		  if (!visited.contains(sCrtId)) {
	    			  // Mark it as visited
	    			  visited.add(sCrtId);
	    			  // Extract its direct descendant attributes and insert them into the queue
	    			  Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtId + "," + sAttrContainer);
	    			  Attribute attr = attrs.get("pmToAttr");
	    			  if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
	    				  String sAttrId = (String)enumer.next();
	    				  queue.add(sAttrId);
	    			  }
	    			  // Extract current node's directly assigned policy classes
	    			  // and add them to the ua's label if not already there.
	    			  attr = attrs.get("pmToPolicy");
	    			  if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
	    				  String sPcId = (String)enumer.next();
	    				  reachable.add(sPcId);
	    			  }
	    		  }
	    	  }
	      } catch (Exception e) {
	    	  e.printStackTrace();
	      }
	      return reachable;
	  }
	    


	  public Packet getMellMembersOf(String sSessId, String sBaseName, String sBaseId, String sBaseType,
	  String sGraphType) throws Exception {
	        log.debug("TRACE 5 - In ActiveDirectoryDAO.getMellMembersOf()");

	    if (sBaseType == null) return failurePacket("Null (unknown) base node type in getMellMembersOf()");
	    if (sBaseName == null) {
	      if (sBaseId == null) return failurePacket("Name and id of the base node are both null in getMellMembersOf()");
	      sBaseName = getEntityName(sBaseId, sBaseType);
	      if (sBaseName == null) return failurePacket("No base node of type " + sBaseType + " and id " + sBaseId);
	    } else if (sBaseId == null) {
	      sBaseId = getEntityId(sBaseName, sBaseType);
	      if (sBaseId == null) return failurePacket("No base node of type " + sBaseType + " and name " + sBaseName);
	    } else {
	      String sBaseName2 = getEntityName(sBaseId, sBaseType);
	      if (sBaseName2 == null) return failurePacket("No base node of type " + sBaseType + " and id " + sBaseId);
	      if (!sBaseName2.equalsIgnoreCase(sBaseName))
	        return failurePacket("Inconsistency between base node name " + sBaseName + " and id " + sBaseId);
	    }
	    
	    String sUserId = getSessionUserId(sSessId);
	    String sUserName = getEntityName(sUserId, PM_NODE.USER.value);
	    System.out.println("getMellMembersOf, user: " + sUserName);
	    System.out.println("Base type: " + sBaseType);
	    System.out.println("Base name: " + sBaseName);
	    // Steve - Deleted (4/18/16): Why is this defined when Packet res is
	    // defined below?
	    //Packet result = new Packet();
	    
	    try {
	    	// For the CONNECTOR.
	    	// The members of the connector are the initial nodes as detected by Mell algo
	    	// implemented in initialOasInternal().
	    	if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
		        log.debug("TRACE 6a - ActiveDirectoryDAO.getMellMembersOf() baseType == CONN");
	    		HashSet hsOas = initialOasInternal(sUserName, sUserId);
	    		Packet res = new Packet();
	    		Iterator hsiter = hsOas.iterator();
	    		while (hsiter.hasNext()) {
	    			String sOaId = (String)hsiter.next();
	    			String sOaName = getEntityName(sOaId, PM_NODE.OATTR.value);
	    			String sOaType = PM_NODE.OATTR.value;
	    			if (hasAssocObj(sOaId)) sOaType = PM_NODE.ASSOC.value;
	    			res.addItem(ItemType.RESPONSE_TEXT, sOaType + GlobalConstants.PM_FIELD_DELIM +
	    					sOaId + GlobalConstants.PM_FIELD_DELIM + sOaName + GlobalConstants.PM_FIELD_DELIM + "true");    			
	    		}
				// Print packet
				log.debug("RETURNING PACKET:");
				res.print(true, "GET_MELL_MEMBERS_OF");
	    		return res;

	    	} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
		        log.debug("TRACE 6b - ActiveDirectoryDAO.getMellMembersOf() baseType == OATTR");
	        	// OBJECT ATTRIBUTE
	    		// The members are the oa nodes (they include objects) as computed
	    		// by Mell's algorithm implemented in subsequentOas().
	    		HashSet hsOas = subsequentOasInternal(sUserName, sUserId, sBaseName, sBaseId);
	    		Packet res = new Packet();
	    		if (hsOas == null) return failurePacket("Null result returned by subsequentOasInternal!");
	    		for (Iterator hsiter = hsOas.iterator(); hsiter.hasNext(); ) {
	    			String sOaId = (String)hsiter.next();
	    			String sOaName = getEntityName(sOaId, PM_NODE.OATTR.value);
	    			String sOaType = PM_NODE.OATTR.value;
	    			if (hasAssocObj(sOaId)) sOaType = PM_NODE.ASSOC.value;
	    			res.addItem(ItemType.RESPONSE_TEXT, sOaType + GlobalConstants.PM_FIELD_DELIM +
	    					sOaId + GlobalConstants.PM_FIELD_DELIM + sOaName + GlobalConstants.PM_FIELD_DELIM + "true");
	    		}
				// Print packet
				log.debug("RETURNING PACKET:");
				// Steve - Modified (4/18/16): Was 'GET_MEMBERS_OF'
				res.print(true, "GET_MELL_MEMBERS_OF");
	    		return res;
	    	} else if (sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
		        log.debug("TRACE 6c - ActiveDirectoryDAO.getMellMembersOf() baseType == ASSOC");
	    		// OBJECT ATTRIBUTE ASSOCIATED TO AN OBJECT
	    		Packet res = new Packet();
				// Print packet
				log.debug("RETURNING PACKET:");
				res.print(true, "GET_MELL_MEMBERS_OF");
	    		return res;
	    	} else {
		        log.debug("TRACE 6d - ActiveDirectoryDAO.getMellMembersOf() wrong baseType");
	    		return failurePacket("Wrong base type in getMellMembersOf()!");
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	return failurePacket();
	    }
	  }

	  // Get all operations of an opset in a vector.
	  private Vector getOpsetOperations(String sOpsetId) {
	    Vector res = new Vector();
	    Attributes attrs;
	    Attribute attr;
	    String s;
	    
	    try {
	      attrs = ServerConfig.ctx.getAttributes("CN=" + sOpsetId + "," + sOpsetContainerDN);
	      attr = attrs.get("pmOp");
	      if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
	        res.add((String)enumer.next());
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	      return null;
	    }
	    return res;
	  }
	
	  // Find the containers of an object attribute which are accessible to a given user.
	  // We'll call the object attribute "base node". The base node may also be the
	  // object attribute associated with an object, or the connector node itself.
	  // It is assumed that when the base node is an object attribute, it is
	  // accessible to the current user (the one that clicked on the base node
	  // to display its containers). In this case, the containers are returned
	  // by the method
	  //		HashSet successorOas(User u, ObjectAttribute oa).
	  // If the base node is the connector node, the containers do not exist.
	  // Parameters:
	  // sBaseName, sBaseId, sBaseType: the label/name, id, and type of the base node. One
	  //   but not both of the name and id may be null. The type must be non-null.
	  // sGraphType: the type of graph we display.
	  public Packet getMellContainersOf(String sSessId, String sBaseName, String sBaseId, String sBaseType,
	  String sGraphType) {
	        log.debug("TRACE 5 - In ActiveDirectoryDAO.getMellContainersOf()");

	    if (sBaseType == null) return failurePacket("Null base node type in getMellContainersOf()");
	    if (sBaseName == null) {
	      if (sBaseId == null) return failurePacket("Null base node name and id in getMellContainersOf()");
	      sBaseName = getEntityName(sBaseId, sBaseType);
	      if (sBaseName == null) return failurePacket("No base node of type " + sBaseType + " and id " + sBaseId);
	    } else if (sBaseId == null) {
	      sBaseId = getEntityId(sBaseName, sBaseType);
	      if (sBaseId == null) return failurePacket("No base node of type " + sBaseType + " and name " + sBaseName);
	    } else {
	      String sBaseName2 = getEntityName(sBaseId, sBaseType);
	      if (sBaseName2 == null)
	        return failurePacket("No base node of type " + sBaseType + " and id " + sBaseId);
	      if (!sBaseName2.equalsIgnoreCase(sBaseName))
	        return failurePacket("Inconsistency between base node name " + sBaseName + " and id " + sBaseId);
	    }
	    String sUserId = getSessionUserId(sSessId);
	    String sUserName = getEntityName(sUserId, PM_NODE.USER.value);
	    System.out.println("getMellContainersOf, user is " + getEntityName(sUserId, PM_NODE.USER.value));
	    System.out.println("Base type: " + sBaseType);

	    Packet result = new Packet();
	    
	    try {
	      // OBJECT ATTRIBUTE OR ASSOCIATE.
	      if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value) ||
	          sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
	    	  HashSet hsOas = successorOasInternal(sUserName, sUserId, sBaseName, sBaseId);
	    	  if (hsOas.isEmpty()) {
	    		  result.addItem(ItemType.RESPONSE_TEXT, PM_NODE.CONN.value + GlobalConstants.PM_FIELD_DELIM +
	    				  GlobalConstants.PM_CONNECTOR_ID + GlobalConstants.PM_FIELD_DELIM + GlobalConstants.PM_CONNECTOR_NAME +
	    				  GlobalConstants.PM_FIELD_DELIM + "true");
	    	  } else {
	    		  Iterator hsiter = hsOas.iterator();
	    		  while (hsiter.hasNext()) {
	    			  String sOaId = (String)hsiter.next();
	    			  String sOaName = getEntityName(sOaId, PM_NODE.OATTR.value);
	    			  String sOaType = PM_NODE.OATTR.value;
	    			  if (hasAssocObj(sOaId)) sOaType = PM_NODE.ASSOC.value;
	    			  result.addItem(ItemType.RESPONSE_TEXT, sOaType + GlobalConstants.PM_FIELD_DELIM +
	    					  sOaId + GlobalConstants.PM_FIELD_DELIM + sOaName + GlobalConstants.PM_FIELD_DELIM + "true");
	    		  }
	    	  }
	      }
			// Print packet
			log.debug("PACKET TO RETURN");
			result.print(true, "GET_MELL_CONTAINERS_OF");
	        log.debug("TRACE 6 - In ActiveDirectoryDAO.getMellContainersOf() END");

	      return result;
	    } catch (Exception e) {
	      e.printStackTrace();
	      return failurePacket("Exception in getMellContainersOf()!");
	    }
	  }
	  
	// Find the members of a container. "Container" can be any node of the PM
	// graph, and we'll also call it "base" node. The membership is defined
	// by the direct
	// assignment relation of another entity to the base node.
	// According to this def, an operation set can be considered a container
	// for the user attributes that are assigned to it.
	// The container is given as a "base" node of the PM graph.
	// If the base node is the connector, the nodes assigned to it can be users,
	// user attributes, policies, object attributes, object attributes
	// associated
	// to objects, and operation sets.
	// If the base node is a policy class, the nodes assigned to it can be user
	// attributes, object attributes, object attributes associated to objects.
	// If the base node is a user attribute, the nodes assigned to it can be
	// users,
	// user attributes.
	// If the base node is a user, there are no nodes assigned to it.
	// If the base node is an object attribute, the nodes assigned to it can be
	// object attributes, object attributes associated to objects, operation
	// sets.
	// If the base node is an object attribute associated to an object, the
	// nodes
	// assigned to it are operation sets.
	// If the base node is an operation set, the nodes assigned to it can be
	// user attributes.
	// Parameters:
	// sBaseName, sBaseId, sBaseType: the label/name, id, and type of the base
	// node. One
	// but not both of the name and id may be null. The type must be non-null.
	// sGraphType: the type of graph we display.

	public Packet getMembersOf(String sSessId, String sBaseName, String sBaseId,
			String sBaseType, String sGraphType) {

        log.debug("TRACE 5 - In ActiveDirectoryDAO.getMembersOf()");

		System.out.println("********************* getMembersOf() called with parameters " + 
				" sSessId = " + sSessId +
				" sBaseName = " + sBaseName +
				" sBaseId   = " + sBaseId +
				" sBaseType   = " + sBaseType +
				" sGraphType   = " + sGraphType);

		if (sBaseType == null) {
			return failurePacket("Null (unknown) base node type in getMembersOf()");
		}
		if (sBaseName == null) {
			if (sBaseId == null) {
				return failurePacket("Name and id of the base node are both null in getMembersOf()");
			}
			sBaseName = getEntityName(sBaseId, sBaseType);
			if (sBaseName == null) {
				return failurePacket("No base node of type " + sBaseType
						+ " and id " + sBaseId);
			}
		} else if (sBaseId == null) {
			sBaseId = getEntityId(sBaseName, sBaseType);
			if (sBaseId == null) {
				return failurePacket("No base node of type " + sBaseType
						+ " and name " + sBaseName);
			}
		} else {
			String sBaseName2 = getEntityName(sBaseId, sBaseType);
			if (sBaseName2 == null) {
				return failurePacket("No base node of type " + sBaseType
						+ " and id " + sBaseId);
			}
			if (!sBaseName2.equalsIgnoreCase(sBaseName)) {
				return failurePacket("Inconsistency between base node name "
						+ sBaseName + " and id " + sBaseId);
			}
		}

		String sUserId = getSessionUserId(sSessId);
		System.out.println("getMembersOf, user is " + getEntityName(sUserId,PM_NODE.USER.value));

		Packet result = new Packet();

		try {
			// For the CONNECTOR.
			if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sConnectorContainerDN);

				// Add the users and user attributes if correct graph type.
				if (sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_CAPS)
						|| sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_UATTR)) {
					Attribute attr = attrs.get("pmFromUser");
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							String sId = (String) attrEnum.next();
							result.addItem(ItemType.RESPONSE_TEXT, PM_NODE.USER.value
									+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
									+ getEntityName(sId,PM_NODE.USER.value));
						}
					}

					attr = attrs.get("pmFromUserAttr");
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							String sId = (String) attrEnum.next();
							result.addItem(
									ItemType.RESPONSE_TEXT,
									PM_NODE.UATTR.value + GlobalConstants.PM_FIELD_DELIM + sId
									+ GlobalConstants.PM_FIELD_DELIM
									+ getEntityName(sId,PM_NODE.UATTR.value));
						}
					}
				}

				// Always add the policies.
				Attribute attr = attrs.get("pmFromPolicyClass");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.POL.value
								+ GlobalConstants.PM_FIELD_DELIM 
								+ sId 
								+ GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.POL.value) 
								+ GlobalConstants.PM_FIELD_DELIM +
								"true");
					}
				}

				// Add the object attributes and associates if correct graph
				// type.
				if (sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_ACES)
						|| sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_OATTR)) {
					attr = attrs.get("pmFromObjAttr");
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							String sId = (String) attrEnum.next();
							
							// Is thiss object attribute accessible to the session user?
							boolean isAcc = oattrIsAccessible(sUserId, sId);
							
							if (hasAssocObj(sId)) {
								result.addItem(
										ItemType.RESPONSE_TEXT,
										PM_NODE.ASSOC.value
										+ GlobalConstants.PM_FIELD_DELIM
										+ sId
										+ GlobalConstants.PM_FIELD_DELIM
										+ getEntityName(sId, PM_NODE.OATTR.value) 
										+ GlobalConstants.PM_FIELD_DELIM
										+ isAcc);
							} else {
								result.addItem(
										ItemType.RESPONSE_TEXT,
										PM_NODE.OATTR.value
										+ GlobalConstants.PM_FIELD_DELIM
										+ sId
										+ GlobalConstants.PM_FIELD_DELIM
										+ getEntityName(sId, PM_NODE.OATTR.value)
										+ GlobalConstants.PM_FIELD_DELIM
										+ isAcc);
							}
						}
					}
				}

				// Always add the operation sets.
				attr = attrs.get("pmFromOpSet");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.OPSET.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.OPSET.value));
					}
				}

				// POLICY
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sPolicyContainerDN);

				// Add the user attributes if correct graph type.
				if (sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_CAPS)
						|| sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_UATTR)) {
					Attribute attr = attrs.get("pmFromUserAttr");
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							String sId = (String) attrEnum.next();
							result.addItem(
									ItemType.RESPONSE_TEXT,
									PM_NODE.UATTR.value + GlobalConstants.PM_FIELD_DELIM + sId
									+ GlobalConstants.PM_FIELD_DELIM
									+ getEntityName(sId,PM_NODE.UATTR.value));
						}
					}
				}

				// Add the object attributes and associates if correct graph
				// type.
				if (sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_ACES)
						|| sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_OATTR)) {
					Attribute attr = attrs.get("pmFromObjAttr");
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							String sId = (String) attrEnum.next();
							
							// Is this object attribute accessible to the session user?
							boolean isAcc = oattrIsAccessible(sUserId, sId);
							
							if (hasAssocObj(sId)) {
								result.addItem(
										ItemType.RESPONSE_TEXT,
										PM_NODE.ASSOC.value
										+ GlobalConstants.PM_FIELD_DELIM
										+ sId
										+ GlobalConstants.PM_FIELD_DELIM
										+ getEntityName(sId,PM_NODE.OATTR.value)
										+ GlobalConstants.PM_FIELD_DELIM
										+ isAcc);
							} else {
								result.addItem(
										ItemType.RESPONSE_TEXT,
										PM_NODE.OATTR.value
										+ GlobalConstants.PM_FIELD_DELIM
										+ sId
										+ GlobalConstants.PM_FIELD_DELIM
										+ getEntityName(sId,PM_NODE.OATTR.value)
										+ GlobalConstants.PM_FIELD_DELIM
										+ isAcc);
							}
						}
					}
				}

				// USER ATTRIBUTE
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sUserAttrContainerDN);

				// Add the user attributes.
				Attribute attr = attrs.get("pmFromAttr");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.UATTR.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.UATTR.value));
					}
				}

				// Add the users.
				attr = attrs.get("pmFromUser");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.USER.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.USER.value));
					}
				}

				// OBJECT ATTRIBUTE
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sObjAttrContainerDN);

				// Add the object attributes and associates.
				Attribute attr = attrs.get("pmFromAttr");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();

						boolean isAcc = oattrIsAccessible(sUserId, sId);

						if (hasAssocObj(sId))
							result.addItem(
									ItemType.RESPONSE_TEXT,
									PM_NODE.ASSOC.value + GlobalConstants.PM_FIELD_DELIM + sId
									+ GlobalConstants.PM_FIELD_DELIM
									+ getEntityName(sId,PM_NODE.OATTR.value)
									+ GlobalConstants.PM_FIELD_DELIM + isAcc);
						else
							result.addItem(
									ItemType.RESPONSE_TEXT,
									PM_NODE.OATTR.value + GlobalConstants.PM_FIELD_DELIM + sId
									+ GlobalConstants.PM_FIELD_DELIM
									+ getEntityName(sId,PM_NODE.OATTR.value)
									+ GlobalConstants.PM_FIELD_DELIM + isAcc);
					}
				}

				// Add the operation sets if correct graph type.
				if (sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_ACES)) {
					attr = attrs.get("pmFromOpSet");
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							String sId = (String) attrEnum.next();
							result.addItem(
									ItemType.RESPONSE_TEXT,
									PM_NODE.OPSET.value + GlobalConstants.PM_FIELD_DELIM + sId
									+ GlobalConstants.PM_FIELD_DELIM
									+ getEntityName(sId,PM_NODE.OPSET.value));
						}
					}
				}

				// OBJECT ATTRIBUTE ASSOCIATED TO AN OBJECT
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sObjAttrContainerDN);

				// Add the operation sets if correct graph type.
				if (sGraphType.equalsIgnoreCase(GlobalConstants.PM_GRAPH_ACES)) {
					Attribute attr = attrs.get("pmFromOpSet");
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							String sId = (String) attrEnum.next();
							result.addItem(
									ItemType.RESPONSE_TEXT,
									PM_NODE.OPSET.value + GlobalConstants.PM_FIELD_DELIM + sId
									+ GlobalConstants.PM_FIELD_DELIM
									+ getEntityName(sId,PM_NODE.OPSET.value));
						}
					}
				}

				// OPERATION SET.
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sOpsetContainerDN);

				// Add the user attributes.
				Attribute attr = attrs.get("pmFromAttr");
				if (attr != null) {
					for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
						String sId = (String) attrEnum.next();
						result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.UATTR.value
								+ GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.UATTR.value));
					}
				}
			}
			
			// Print packet
			log.debug("PACKET TO RETURN");
			result.print(true, "GET_MEMBERS_OF");
	        log.debug("TRACE 6 - In ActiveDirectoryDAO.getMembersOf() END");

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getMembersOf()");
		}
	}

	public boolean oattrIsAccessible(String sUserId, String sOaId) {
		if (!htAcc.containsKey(sUserId)) return false;
		HashSet hs = (HashSet)htAcc.get(sUserId);
		if (hs.contains(sOaId)) return true;
		return false;
	}

	// Create a PM object container for a given folder within a given container.

	public Packet createContForFolder(String sSessId, String sFolderPath,
			String sBaseContName) {
		System.out.println("Entering createContForFolder");
		System.out.println("    sFolderPath = " + sFolderPath);
		System.out.println("    sContName = " + sBaseContName);

		// First get the folder's name.
		File f = new File(sFolderPath);
		String sFolderName = f.getName();
		System.out.println("    sFolderName = " + sFolderName);

		// Now get a name for the container. We can use the same
		// getUniqueObjName.
		String sContName = getUniqueObjName(sFolderName);
		System.out.println("    sContName = " + sContName);

		// We need the id of the base container.
		String sBaseContId = getEntityId(sBaseContName,PM_NODE.OATTR.value);
		if (sBaseContId == null) {
			return failurePacket("No container " + sBaseContName);
		}

		// Now create the new container.
		Packet result =  addOattrInternal(sContName, null, sContName,
				sContName, sBaseContId,PM_NODE.OATTR.value, null, null);

		return result;
	}

	// Create a PM (logical) object for a given file within a given container.

	public Packet createObjForFile(String sSessId, String sFilePath,
			String sContName) {
		System.out.println("Entering createObjForFile");
		System.out.println("    sFilePath = " + sFilePath);
		System.out.println("    sContName = " + sContName);

		// First get the file's name.
		File f = new File(sFilePath);
		String sFileName = f.getName();
		System.out.println("    sFileName = " + sFileName);

		// Now get a name for the object.
		String sObjName = getUniqueObjName(sFileName);
		System.out.println("    sObjName = " + sObjName);

		// Get the file host.
		String sHostName = getSessionHostName(sSessId);

		String sContId = getEntityId(sContName,PM_NODE.OATTR.value);
		if (sContId == null) {
			return failurePacket("No container " + sContName);
		}

		// Now create the object.
		Packet result =  addObjectInternal(sObjName, null, null,
				sObjName, sObjName, sContId,PM_NODE.OATTR.value, GlobalConstants.PM_CLASS_FILE_NAME,
				null, sHostName, sFilePath, null, null, false, null, null,
				null, null, null, null, null);

		return result;
	}

	// Given a proper file name (not the entire path), this function returns
	// a suitable object name.
	// Strating with the file name (excluding the file type), check
	// to see whether an object attribute with that name exists.
	// If yes, try that name with a '1' appended, then a '2', etc.,
	// until the object attribute with that name does not exist.

	public String getUniqueObjName(String sFileName) {
		System.out.println("Entering getUniqueObjName with argument "
				+ sFileName);
		int ix = sFileName.lastIndexOf(".");
		String sBaseName = null;
		String sFileType = null;
		if (ix >= 0) {
			sBaseName = sFileName.substring(0, ix);
			sFileType = sFileName.substring(ix);
		} else {
			sBaseName = sFileName;
			sFileType = "";
		}

		System.out.println("    Found ix = " + ix + ", sBaseName = "
				+ sBaseName + ", sFileType = " + sFileType);

		String sObjName = sBaseName;
		int n = 0;
		System.out.println("    Entering the loop of getUniqueObjName");
		while (true) {
			System.out.println("    Trying the name " + sObjName);
			if (getEntityId(sObjName,PM_NODE.OATTR.value) == null) {
				return sObjName;
			}
			n++;
			sObjName = sObjName + n;
		}
	}

	// sProp is a property name. The corresponding property value
	// is the file absolute path on the engine host.

	public String getFilePath(String sSessId, String sProp) {
		Packet res =  getPropertyValue(sSessId, sProp);
		if (res.hasError()) {
			return null;
		}
		return res.getStringValue(0);
	}


	public Packet getOutboxMessages(String sSessId) {
		return getMailboxMessages(sSessId, "outboxof=");
	}


	public Packet getInboxMessages(String sSessId) {
		return getMailboxMessages(sSessId, "inboxof=");
	}


	public Packet getMailboxMessages(String sSessId, String sPropPrefix) {
		String sUserId = getSessionUserId(sSessId);
		if (sUserId == null) {
			return failurePacket("Couldn't find the session or the user id!");
		}
		String sUserName = getEntityName(sUserId,PM_NODE.USER.value);
		if (sUserName == null) {
			return failurePacket("Couldn't find the session user!");
		}
		String sProp = sPropPrefix + sUserName;
		String sMailboxId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
		if (sMailboxId == null) {
			return failurePacket("No object attribute with property \"" + sProp
					+ "\"!");
		}

		// We found the session's user's mailbox. Get all objects that are
		// messages,
		// i.e., have pmEmlRecip.
		Packet result = new Packet();
		try {
			Attributes mailboxAttrs = ServerConfig.ctx.getAttributes("CN=" + sMailboxId
					+ "," + sObjAttrContainerDN);
			Attribute mailboxAttr = mailboxAttrs.get("pmFromAttr");
			if (mailboxAttr != null) {
				for (NamingEnumeration<?> attrEnum = mailboxAttr.getAll(); attrEnum.hasMore(); ) {
					String sOattrId = (String) attrEnum.next();
					String sObjId = getAssocObj(sOattrId);

					// Note that the INBOX could contain oattrs, for example
					// wINBOX,
					// which do not have associated objects. Skip them.
					if (sObjId == null) {
						continue;
					}

					Attributes objAttrs = ServerConfig.ctx.getAttributes("CN=" + sObjId
							+ "," + sVirtualObjContainerDN);
					Attribute attr = objAttrs.get("pmEmlRecip");
					if (attr == null) {
						continue;
					}
					String sRecip = (String) attr.get();
					// For the messages in the inbox, the recipient is the user.
					// For the messages in the outbox, the recipient may be
					// another user.
					String sSender = (String) objAttrs.get("pmEmlSender").get();
					String sDate = (String) objAttrs.get("pmTimestamp").get();
					String sSubject = (String) objAttrs.get("pmEmlSubject").get();
					String sLabel = getEntityName(sObjId, GlobalConstants.PM_OBJ);
					attr = objAttrs.get("pmEmlAttached");
					if (attr == null) {
						result.addItem(ItemType.RESPONSE_TEXT, sLabel
								+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_ALT_FIELD_DELIM
								+ sSender + GlobalConstants.PM_ALT_FIELD_DELIM + sRecip
								+ GlobalConstants.PM_ALT_FIELD_DELIM + sDate
								+ GlobalConstants.PM_ALT_FIELD_DELIM + sSubject);
					} else {
						result.addItem(ItemType.RESPONSE_TEXT, sLabel
								+ GlobalConstants.PM_ALT_FIELD_DELIM + attr.get()
								+ GlobalConstants.PM_ALT_FIELD_DELIM + sSender
								+ GlobalConstants.PM_ALT_FIELD_DELIM + sRecip
								+ GlobalConstants.PM_ALT_FIELD_DELIM + sDate
								+ GlobalConstants.PM_ALT_FIELD_DELIM + sSubject);
					}
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getMailboxMessages: "
					+ e.getMessage());
		}
	}


	public Packet deassignObjFromOattrWithProp(String sSessId, String sProcId,
			String sObjName, String sProp) {
		String sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
		if (sOattrId == null) {
			return failurePacket("No object attribute with property \"" + sProp
					+ "\"!");
		}
		String sOaId = getEntityId(sObjName,PM_NODE.OATTR.value);
		if (sOaId == null) {
			return failurePacket("No object attribute or no object of name \""
					+ sObjName + "\"!");
		}
		return deleteAssignment(sSessId, sProcId, sOaId,PM_NODE.OATTR.value,
				sOattrId,PM_NODE.OATTR.value, "no");
	}


	public Packet assignObjToOattrWithProp(String sSessId, String sProcId,
			String sObjName, String sProp) {
		String sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
		if (sOattrId == null) {
			return failurePacket("No object attribute with property \"" + sProp
					+ "\"!");
		}
		String sOaId = getEntityId(sObjName,PM_NODE.OATTR.value);
		if (sOaId == null) {
			return failurePacket("No object attribute or no object of name \""
					+ sObjName + "\"!");
		}
		return assign(sSessId, sProcId, sOaId,PM_NODE.OATTR.value, sOattrId,
				PM_NODE.OATTR.value); //, "no");
	}


	public Packet assignObjToOattr(String sSessId, String sProcId,
			String sObjName, String sOattrName) {
		String sOattrId = getEntityId(sOattrName,PM_NODE.OATTR.value);
		if (sOattrId == null) {
			return failurePacket("No object attribute with the name \""
					+ sOattrName + "\"!");
		}
		String sOaId = getEntityId(sObjName,PM_NODE.OATTR.value);
		if (sOaId == null) {
			return failurePacket("No object attribute or no object of name \""
					+ sObjName + "\"!");
		}
		return assign(sSessId, sProcId, sOaId,PM_NODE.OATTR.value, sOattrId,
				PM_NODE.OATTR.value); //, "no");
	}

	// An object can be opened multiple times in a session before being closed.
	// pmOpenObj stores the name of the object and a count:
	// <obj name>|<link count>.

	public Packet addOpenObj(String sSessId, String sObjName) {
		try {
			Attributes ooattrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
					+ sSessionContainerDN);
			Attribute ooattr = ooattrs.get("pmOpenObj");
			if (ooattr == null) {
				// There are no open objects in this session. Add the open
				// object with a count of 1.
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpenObj", sObjName
								+ GlobalConstants.PM_ALT_FIELD_DELIM + 1));
				ServerConfig.ctx.modifyAttributes("CN=" + sSessId + ","
						+ sSessionContainerDN, mods);
				return ADPacketHandler.getSuccessPacket();
			} else {
				// There are some open objects. Find the specified one.
				for (NamingEnumeration<?> enumer = ooattr.getAll(); enumer.hasMore(); ) {
					String sOO = (String) enumer.next();
					String[] pieces = sOO.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
					if (sObjName.equalsIgnoreCase(pieces[0])) {
						// Object found. Increase its count.
						int count = Integer.valueOf(pieces[1]).intValue() + 1;
						String sNewOO = sObjName + GlobalConstants.PM_ALT_FIELD_DELIM + count;
						ModificationItem[] mods = new ModificationItem[1];
						mods[0] = new ModificationItem(
								DirContext.REMOVE_ATTRIBUTE,
								new BasicAttribute("pmOpenObj", sOO));
						ServerConfig.ctx.modifyAttributes("CN=" + sSessId + ","
								+ sSessionContainerDN, mods);
						mods[0] = new ModificationItem(
								DirContext.ADD_ATTRIBUTE, new BasicAttribute(
										"pmOpenObj", sNewOO));
						ServerConfig.ctx.modifyAttributes("CN=" + sSessId + ","
								+ sSessionContainerDN, mods);
						return ADPacketHandler.getSuccessPacket();
					}
				}
				// Object not found. Add it with a count of 1.
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpenObj", sObjName
								+ GlobalConstants.PM_ALT_FIELD_DELIM + 1));
				ServerConfig.ctx.modifyAttributes("CN=" + sSessId + ","
						+ sSessionContainerDN, mods);
				return ADPacketHandler.getSuccessPacket();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while adding open object: "
					+ e.getMessage());
		}
	}


	public Packet deleteOpenObj(String sSessId, String sObjName) {
		try {
			Attributes ooattrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
					+ sSessionContainerDN);
			Attribute ooattr = ooattrs.get("pmOpenObj");
			if (ooattr == null) {
				// There are no open objects in this session. Signal error.
				return failurePacket("There are no open objects in this session!");
			}

			// There are some open objects. Find the specified one.
			for (NamingEnumeration<?> enumer = ooattr.getAll(); enumer.hasMore(); ) {
				String sOO = (String) enumer.next();
				String[] pieces = sOO.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				if (sObjName.equalsIgnoreCase(pieces[0])) {
					// Object found. Decrease its count.
					int count = Integer.valueOf(pieces[1]).intValue() - 1;
					if (count <= 0) {
						// Delete object completely.
						ModificationItem[] mods = new ModificationItem[1];
						mods[0] = new ModificationItem(
								DirContext.REMOVE_ATTRIBUTE,
								new BasicAttribute("pmOpenObj", sOO));
						ServerConfig.ctx.modifyAttributes("CN=" + sSessId + ","
								+ sSessionContainerDN, mods);
						return ADPacketHandler.getSuccessPacket();
					}
					// Count still positive. Replace the attribute.
					String sNewOO = sObjName + GlobalConstants.PM_ALT_FIELD_DELIM + count;
					ModificationItem[] mods = new ModificationItem[1];
					mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
							new BasicAttribute("pmOpenObj", sOO));
					ServerConfig.ctx.modifyAttributes("CN=" + sSessId + ","
							+ sSessionContainerDN, mods);
					mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
							new BasicAttribute("pmOpenObj", sNewOO));
					ServerConfig.ctx.modifyAttributes("CN=" + sSessId + ","
							+ sSessionContainerDN, mods);
					return ADPacketHandler.getSuccessPacket();
				}
			}
			// Object not found. Signal error.
			return failurePacket("There is no such object open in this session!");
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while deleting open object "
					+ sObjName);
		}
	}

	// Check the conditions:
	// sReceiver must be a user with an inbox container or
	// a user attribute.
	// If sReceiver is a user with an inbox container, the method returns that
	// user.
	// If sReceiver is a user attribute, the method returns all users members
	// of that attribute, which have an inbox container.

	public Packet getEmailRecipients(String sSessId, String sReceiver) {
		String sUserId = null;
		String sUattrId = null;
		String sInboxId = null;
		Packet result = new Packet();

		// Is this a user?
		sUserId = getEntityId(sReceiver,PM_NODE.USER.value);
		if (sUserId != null) {
			// Does the user have an inbox container?
			sInboxId = getEntityWithPropInternal(PM_NODE.OATTR.value, "inboxof="
					+ sReceiver);
			if (sInboxId == null) {
				return failurePacket("User " + sReceiver + " has no INBOX!");
			}
			try {
				result.addItem(ItemType.RESPONSE_TEXT, sReceiver);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Exception when building the result packet");
			}
			return result;
		}

		// Not a user. Is this a user attribute?
		sUattrId = getEntityId(sReceiver,PM_NODE.UATTR.value);
		if (sUattrId == null) {
			return failurePacket(sReceiver
					+ " is not a user or user attribute!");
		}

		// A user attribute. Get all its users and keep only those with an
		// INBOX.
		HashSet<String> users = new HashSet<String>();
		getMemberUsers(sUattrId, users);
		Iterator<String> iter = users.iterator();
		try {
			while (iter.hasNext()) {
				sUserId = iter.next();
				String sUserName = getEntityName(sUserId,PM_NODE.USER.value);
				// Should not happen:
				if (sUserName == null) {
					continue;
				}
				sInboxId = getEntityWithPropInternal(PM_NODE.OATTR.value, "inboxof="
						+ sUserName);
				if (sInboxId != null) {
					result.addItem(ItemType.RESPONSE_TEXT, sUserName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return result;
	}

	/*
	 * // If the object contains the message, then the object should already be
	 * // in the sender's OUTBOX. If the object is an attachment, it should not
	 * // be in the sender's OUTBOX. This function simply inserts the object //
	 * in the receiver's INBOX, if not already there. public Packet
	 * sendObject(String sSessId, String sObjName, String sReceiver) { // Find
	 * the id of the associated oattr. String sOattrId = getIdOfEntityWithNameAndType(sObjName,
	 *PM_NODE.OATTR.value); if (sOattrId == null) return
	 * failurePacket("No object or associated attribute " + sObjName +
	 * " exists!");
	 *
	 * // Find the receiver's INBOX container. String sInboxId =
	 * getEntityWithPropInternal(PM_NODE.OATTR.value, "inboxof=" + sReceiver); if
	 * (sInboxId == null) return
	 * failurePacket("Receiver has no INBOX container!");
	 *
	 * // Insert the object in the receiver's INBOX, if not already there. if
	 * (!attrIsAssignedToAttr(sOattrId, sInboxId,PM_NODE.OATTR.value)) { Packet res =
	 * addDoubleLink(sOattrId,PM_NODE.OATTR.value, sInboxId,PM_NODE.OATTR.value);
	 * if (res.hasError()) return res; } return ADPacketHandler.getSuccessPacket(); }
	 */
	// Generate an event.

	public Packet sendObject(String sSessId, String sObjName, String sReceiver) throws Exception {
		String sObjId = getEntityId(sObjName, GlobalConstants.PM_OBJ);
		return  processEvent(sSessId, null, GlobalConstants.PM_EVENT_OBJECT_SEND,
				sObjName, sObjId, null, null, sReceiver, null);
	}

	// The message is already in an object with the name sMsgName, probably
	// located
	// in the OUTBOX container of the sender. Just include the message in the
	// receiver's INBOX container.

	public Packet sendSimpleMsg(String sSessId, String sMsgName,
			String sReceiver) {
		// Find the msg id (as an object attribute).
		String sMsgId = getEntityId(sMsgName,PM_NODE.OATTR.value);
		if (sMsgId == null) {
			return failurePacket("No message " + sMsgName + " exists!");
		}

		// Find the sender and the sender's OUTBOX container.
		String sSenderId = getSessionUserId(sSessId);
		if (sSenderId == null) {
			return failurePacket("Couldn't find sender!");
		}
		String sSender = getEntityName(sSenderId,PM_NODE.USER.value);
		if (sSender == null) {
			return failurePacket("Couldn't find sender!");
		}

		String sOutboxId = getEntityWithPropInternal(PM_NODE.OATTR.value, "outboxof="
				+ sSender);
		if (sOutboxId == null) {
			return failurePacket("Sender has no OUTBOX container!");
		}

		// Find the receiver's INBOX container.
		String sInboxId = getEntityWithPropInternal(PM_NODE.OATTR.value, "inboxof="
				+ sReceiver);
		if (sInboxId == null) {
			return failurePacket("Receiver has no INBOX container!");
		}

		Packet res =  addDoubleLink(sMsgId,PM_NODE.OATTR.value, sInboxId,
				PM_NODE.OATTR.value);
		if (res.hasError()) {
			return res;
		}
		res =  deleteDoubleLink(sMsgId,PM_NODE.OATTR.value, sOutboxId,
				PM_NODE.OATTR.value);
		if (res.hasError()) {
			return res;
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Returns:
	// item 0: <user name>:<user id>
	// item 1: <'coming from' name>
	// item 2: <email address>
	// item 3: <incoming server>
	// item 4: <outgoing server>
	// item 5: <account name>.
	// If the user has no email account, the result contains only item 0.

	public Packet getEmailAcct(String sSessId, String sPmUser) {
		String sPmUserId = getEntityId(sPmUser,PM_NODE.USER.value);
		if (sPmUserId == null) {
			return failurePacket("No such PM user!");
		}
		if (!entityExists(sPmUserId, GlobalConstants.PM_EMAIL_ACCT)) {
			Packet result = new Packet();
			try {
				result.addItem(ItemType.RESPONSE_TEXT, sPmUser + GlobalConstants.PM_FIELD_DELIM
						+ sPmUserId);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Exception when building the result packet");
			}
			return result;
		}

		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sPmUserId + ","
					+ sEmailAcctContainerDN);
			Packet result = new Packet();
			result.addItem(ItemType.RESPONSE_TEXT, sPmUser + GlobalConstants.PM_FIELD_DELIM
					+ sPmUserId);
			result.addItem(ItemType.RESPONSE_TEXT,
					(String) attrs.get("pmUserName").get());
			result.addItem(ItemType.RESPONSE_TEXT,
					(String) attrs.get("pmEmailAddr").get());
			result.addItem(ItemType.RESPONSE_TEXT,
					(String) attrs.get("pmPopServer").get());
			result.addItem(ItemType.RESPONSE_TEXT,
					(String) attrs.get("pmSmtpServer").get());
			result.addItem(ItemType.RESPONSE_TEXT,
					(String) attrs.get("pmAcctName").get());
			return result;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while retrieveing the email account attributes!");
		}
	}


	public Packet addEmailAcct(String sSessId, String sPmUser,
			String sFullName, String sEmailAddr, String sPopServer,
			String sSmtpServer, String sAcctName, String sPassword) {
		// Checks...
		return addEmailAcctInternal(sPmUser, sFullName, sEmailAddr, sPopServer,
				sSmtpServer, sAcctName, sPassword);
	}


	public Packet addEmailAcctInternal(String sPmUser, String sFullName,
			String sEmailAddr, String sPopServer, String sSmtpServer,
			String sAcctName, String sPassword) {
		// Test if account already exists.
		String sPmUserId = getEntityId(sPmUser,PM_NODE.USER.value);
		if (sPmUserId == null) {
			return failurePacket("No such PM user!");
		}
		if (entityExists(sPmUserId, GlobalConstants.PM_EMAIL_ACCT)) {
			System.out.println("Email account already exists. Modifying...");
			ModificationItem[] mods = new ModificationItem[6];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmUserName", sFullName));
			mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmEmailAddr", sEmailAddr));
			mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmPopServer", sPopServer));
			mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmSmtpServer", sSmtpServer));
			mods[4] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmAcctName", sAcctName));
			mods[5] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmPassword", sPassword));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + sPmUserId + ","
						+ sEmailAcctContainerDN, mods);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket(e.getMessage());
			}
		} else {
			System.out.println("Email account new. Setting it up...");
			// Prepare the attributes of the new user object.
			Attributes attrs = new BasicAttributes(true);
			attrs.put("objectClass", sEmailAcctClass);
			attrs.put("pmId", sPmUserId);
			attrs.put("pmUserName", sFullName);
			attrs.put("pmEmailAddr", sEmailAddr);
			attrs.put("pmPopServer", sPopServer);
			attrs.put("pmSmtpServer", sSmtpServer);
			attrs.put("pmAcctName", sAcctName);
			attrs.put("pmPassword", sPassword);
			// Prepare the path and create the new user object.
			String sDn = "CN=" + sPmUserId + "," + sEmailAcctContainerDN;
			try {
				ServerConfig.ctx.bind(sDn, null, attrs);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Failed to create the e-mail account for user "
						+ sPmUser);
			}
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet genIbac(String sSessId, String sPolName) {
		// Check permissions...

		// If a policy with the property "type=identity" already exists, return.
		if (getEntityWithPropInternal(PM_NODE.POL.value, "type=identity") != null) {
			return failurePacket("An identity-based policy already exists!");
		}

		if (getEntityId(sPolName,PM_NODE.POL.value) != null) {
			return failurePacket("A policy with the same name already exists!");
		}

		String sCmd;
		Packet res;

		// Create the policy class.
		sCmd = "add|p|" + sPolName + "|c|PM";
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|prop|type=identity|p|" + sPolName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Generate the EVER script for "User create".
		Packet script = new Packet();
		// The function that processes the script expects it to start at item 3.
		try {
			script.addItem(ItemType.RESPONSE_TEXT, "Filler");
			script.addItem(ItemType.RESPONSE_TEXT, "Filler");
			script.addItem(ItemType.RESPONSE_TEXT, "Filler");
			script.addItem(ItemType.RESPONSE_TEXT, "script ibac");
			script.addItem(ItemType.RESPONSE_TEXT, "");
			script.addItem(ItemType.RESPONSE_TEXT, "when any user performs \""
					+ GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			script.addItem(ItemType.RESPONSE_TEXT, "do");
			script.addItem(ItemType.RESPONSE_TEXT,
					"  create user attribute uattr_name_of_user(user_new())");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    with property prop_name_of_user(user_new())");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    in policy pol_with_prop(\"type=identity\")");
			script.addItem(ItemType.RESPONSE_TEXT,
					"  assign user user_new() to user attribute uattr_name_of_new_user()");
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the script packet!");
		}

		// Compile and submit the script.
		// boolean bScriptCompiled = sysCaller.addScript(script);
		// System.out.println("Result from compile script: " + bScriptCompiled);

		res =  compileScriptAndAddToEnabled(sSessId, script);
		if (res == null) {
			return failurePacket("Null result from addScript call");
		}
		if (res.hasError()) {
			return res;
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// We create a DAC user with specified name and a sensitive container in
	// his/her home, a confinement policy if it does not exist, a
	// confinement attribute for the sensitive container.
	// The DAC policy must exist. The user (as well as his home) must not exist.
	// The confinement policy class may or may not exist; if it doesn't
	// exist, it will be created. The confinement attribute must not exist;
	// it will be created.

	public Packet genDacUserWithConf(String sSessId, String sUser,
			String sFullName, String sConfPol, String sConfAttr,
			String sSensCont) throws Exception {

		String sCmd;
		Packet res;

		// Do permission checks...

		// Return failure if a DAC policy does not exist.
		String sDacPolId = getEntityWithPropInternal(PM_NODE.POL.value,
				"type=discretionary");
		if (sDacPolId == null) {
			return failurePacket("No DAC policy exists!");
		}

		// The user must not exist.
		String sUserId = getEntityId(sUser,PM_NODE.USER.value);
		if (sUserId != null) {
			return failurePacket("User \"" + sUser + "\" already exists!");
		}

		// The user name attribute must not exist.
		String sUserNameAttrId = getEntityId(sFullName,PM_NODE.UATTR.value);
		if (sUserNameAttrId != null) {
			return failurePacket("A user attribute \"" + sFullName
					+ "\" already exists!");
		}

		// Return failure if the confinement policy exists but does not have
		// the property "type=confinement".
		String sConfPolId = getEntityId(sConfPol,PM_NODE.POL.value);
		if (sConfPolId != null) {
			HashSet<String> props = getProps(sConfPolId,PM_NODE.POL.value);
			if (!props.contains("type=confinement")) {
				return failurePacket("Policy \"" + sConfPol
						+ "\" exists, but is not a confinement policy!");
			}
		}

		// Create the DAC user and process the event.
		Packet result =  addUserInternal(sUser, null, sFullName,
				sFullName, null, GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
		if (result.hasError()) {
			return result;
		}
		String sLine = result.getStringValue(0);
		String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
		result =  processEvent(sSessId, null, GlobalConstants.PM_EVENT_USER_CREATE,
				sUser, pieces[1], GlobalConstants.PM_CLASS_USER_NAME, null, null, null);
		if (result.hasError()) {
			return result;
		}

		// Create the sensitive container in the user's home.
		String sHomeId = getEntityWithPropInternal(PM_NODE.OATTR.value, "homeof="
				+ sUser);
		if (sHomeId == null) {
			return failurePacket("User \"" + sUser
					+ "\" has no home container!");
		}
		String sHome = getEntityName(sHomeId,PM_NODE.OATTR.value);
		sCmd = "add|b|" + sSensCont + "|b|" + sHome;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		return genConfForDacUser(sSessId, sUser, sConfPol, sConfAttr, sSensCont);
	}

	// We create the confinement attribute for a specified sensitive container
	// of a
	// specified user. The policy may or may not exist; if it doesn't
	// exist, it will be created. The user, home, and his/her container must
	// exist.
	// The confinement attribute must not exist; it will be created.

	public Packet genConfForDacUser(String sSessId, String sUser,
			String sConfPol, String sConfAttr, String sSensCont) {
		String sCmd;
		Packet res;

		// Return failure if a DAC policy does not exist.
		String sDacPolId = getEntityWithPropInternal(PM_NODE.POL.value,
				"type=discretionary");
		if (sDacPolId == null) {
			return failurePacket("No DAC policy exists!");
		}

		// Return failure if the confinement policy exists but does not have the
		// property
		// "type=confinement".
		String sConfPolId = getEntityId(sConfPol,PM_NODE.POL.value);
		if (sConfPolId != null) {
			HashSet<String> props = getProps(sConfPolId,PM_NODE.POL.value);
			if (!props.contains("type=confinement")) {
				return failurePacket("Policy \"" + sConfPol
						+ "\" exists, but is not a confinement policy!");
			}
		}

		// The user must exist.
		String sUserId = getEntityId(sUser,PM_NODE.USER.value);
		if (sUserId == null) {
			return failurePacket("User \"" + sUser + "\" must already exist!");
		}

		// The user name attribute must exist.
		String sUserNameAttrId = getEntityWithPropInternal(PM_NODE.UATTR.value,
				"nameof=" + sUser);
		if (sUserNameAttrId == null) {
			return failurePacket("User \"" + sUser
					+ "\" does not have a name attribute!");
		}

		// The user's home must exist.
		String sHomeId = getEntityWithPropInternal(PM_NODE.OATTR.value, "homeof="
				+ sUser);
		if (sHomeId == null) {
			return failurePacket("User \"" + sUser
					+ "\" has no home container!");
		}

		// The sensitive container must exist somewhere in the user's home.
		String sSensContId = getEntityId(sSensCont,PM_NODE.OATTR.value);
		if (sSensContId == null) {
			return failurePacket("Container \"" + sSensCont
					+ "\" does not exist!");
		}
		if (!attrIsAscendant(sSensContId, sHomeId,PM_NODE.OATTR.value)) {
			return failurePacket("Container \"" + sSensCont
					+ "\" is not contained in user \"" + sUser + "\" home!");
		}

		// The confine attribute must not exist.
		String sConfAttrId = getEntityId(sConfAttr,PM_NODE.UATTR.value);
		if (sConfAttrId != null) {
			return failurePacket("The confinement attribute \"" + sConfAttr
					+ "\" already exists!");
		}

		// Create the confinement policy if necessary.
		if (sConfPolId == null) {
			sCmd = "add|p|" + sConfPol + "|c|PM";
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sCmd = "add|prop|type=confinement|p|" + sConfPol;
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sConfPolId = getEntityId(sConfPol,PM_NODE.POL.value);
		}



		// Create the new confine attribute and assign the user to it.
		sCmd = "add|a|" + sConfAttr + "|p|" + sConfPol;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "asg|u|" + sUser + "|a|" + sConfAttr;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Assign the sensitive container to the confinement policy.
		sCmd = "asg|o|" + sSensCont + "|p|" + sConfPol;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Create an object that represents the confinement attribute.
		// Insert this object in the DAC policy class - the same policy
		// class where the user name attribute is located.
		String sDacPol = getEntityName(sDacPolId,PM_NODE.POL.value);
		sCmd = "add|ob|" + sConfAttr + " rep|User attribute|no|" + sConfAttr
				+ "|ignored|p|" + sDacPol;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Create an object that represents the sensitive container - in the
		// confinement policy.
		sCmd = "add|ob|" + sSensCont + " rep|Object attribute|no|" + sSensCont
				+ "|ignored|p|" + sConfPol;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Set permissions:
		// <confine attribute> -> read/write -> <sensitive container>.
		String sName = UtilMethods.generateRandomName();
		sCmd = "add|s|" + sName + "|oc|ignored|a|" + sConfAttr;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "asg|s|" + sName + "|b|" + sSensCont;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|File read|s|" + sName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|File write|s|" + sName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Set permissions:
		// <confine attribute> -> create objects/assign object attributes ->
		// <sensitive container>.
		sName = UtilMethods.generateRandomName();
		sCmd = "add|s|" + sName + "|oc|ignored|a|" + sConfAttr;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "asg|s|" + sName + "|b|" + sSensCont + " rep";
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|Object attribute assign to|s|" + sName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|Object attribute create object|s|" + sName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Set permissions:
		// <user name attribute> -> assign user attrs -> <confine attribute>.
		sName = UtilMethods.generateRandomName();
		String sUserNameAttr = getEntityName(sUserNameAttrId,PM_NODE.UATTR.value);
		sCmd = "add|s|" + sName + "|oc|ignored|a|" + sUserNameAttr;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "asg|s|" + sName + "|b|" + sConfAttr + " rep";
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|User attribute assign to|s|" + sName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Deny conf attribute File write on the complement of the sensitive
		// container.
		sName = UtilMethods.generateRandomName();
		sCmd = "add|deny|" + sName + "|" + GlobalConstants.PM_DENY_ACROSS_SESSIONS + "|a|"
				+ sConfAttr + "|no";
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|File write|deny|" + sName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|cb|" + sSensCont + "|deny|" + sName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Deny conf attribute Create object on the complement of the sensitive
		// container.
		sName = UtilMethods.generateRandomName();
		sCmd = "add|deny|" + sName + "|" + GlobalConstants.PM_DENY_ACROSS_SESSIONS + "|a|"
				+ sConfAttr + "|no";
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|Object attribute create object|deny|" + sName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|cb|" + sSensCont + "|deny|" + sName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		return ADPacketHandler.getSuccessPacket();
	}

	/*public Packet setSchemaPerms(String sSessId, String sProcId, String sBaseName,
					String sBaseType, String sAttrName, String sUattr){
				String sCmd;
				Packet res;

				sCmd = "add|ob|" + sAttrName + " rep|Object attribute|yes|" + sAttrName + "|ignored|" + sBaseType + "|" + sBaseName;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}

				String id = UtilMethods.generateRandomName();
				sCmd = "add|s|" + id + "|oc|ignored|a|" + sUattr;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					System.out.println(res.getErrorMessage());
					return res;
				}

				sCmd = "asg|s|" + id + "|b|" + sAttrName + " rep";
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}

				sCmd = "add|op|" + GlobalConstants.PM_TABLE_CREATE + "|s|" + id;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}

				sCmd = "add|op|" + GlobalConstants.PM_TABLE_MODIFY + "|s|" + id;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}

				sCmd = "add|op|*|s|" + id;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}

				id = UtilMethods.generateRandomName();
				sCmd = "add|s|" + id + "|oc|ignored|a|" + sUattr;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					System.out.println(res.getErrorMessage());
					return res;
				}

				sCmd = "asg|s|" + id + "|b|" + sAttrName;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}

				sCmd = "add|op|" + GlobalConstants.PM_FILE_READ + "|s|" + id;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}

				sCmd = "add|op|" + GlobalConstants.PM_FILE_WRITE + "|s|" + id;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}
				return ADPacketHandler.getSuccessPacket();
			}


			public Packet setTablePerms(String sSessId, String sProcId, String sBaseName, 
					String sBaseType, String sAttrName, String perms, String uattr){//TODO
				String sCmd;
				Packet res;

				if (allIoOpers == null) {
					allIoOpers = new HashSet<String>();
					for (int i = 0; i < GlobalConstants.sDirOps.length; i++) {
						allIoOpers.add(GlobalConstants.sDirOps[i]);
					}
					for (int i = 0; i < GlobalConstants.sFileOps.length; i++) {
						allIoOpers.add(GlobalConstants.sFileOps[i]);
					}
				}
				System.out.println("0");
				ArrayList<String> io = new ArrayList<String>();
				ArrayList<String> admin = new ArrayList<String>();
				String[] pieces = perms.split(",");
				System.out.println("1");
				System.out.println(allIoOpers);
				for(int i = 0; i < pieces.length; i++){
					String op = pieces[i];
					if(allIoOpers.contains(op)){
						io.add(op);
						System.out.println("op addded to io");
					}else if(!allIoOpers.contains(op)){
						admin.add(op);
						System.out.println("op addded to admin");
					}
				}

				String repId = getEntityId(sAttrName + " rep", "b");
				System.out.println("repId: " + repId);
				if(repId != null){
					String repName = getEntityName(repId, "b");
					System.out.println("repName: " + repName);
					//a rep object already exists for this attribute
					//add new opset to the rep object
					if(admin.size() > 0){
						System.out.println("3");
						String id = UtilMethods.generateRandomName();
						sCmd = "add|s|" + id + "|oc|ignored|a|" + uattr;
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							System.out.println(res.getErrorMessage());
							return res;
						}
						System.out.println("4");
						sCmd = "asg|s|" + id + "|b|" + repName;
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							return res;
						}
						System.out.println("5");
						for(int i = 0; i < admin.size(); i++){
							sCmd = "add|op|" + admin.get(i) + "|s|" + id;
							System.out.println(sCmd);
							res = interpretCmd(sSessId, sCmd);
							if(res.hasError()){
								return res;
							}
						}
						System.out.println("6");
					}
					if(io.size() > 0){
						System.out.println("7");
						String id = UtilMethods.generateRandomName();
						sCmd = "add|s|" + id + "|oc|ignored|a|" + uattr;
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							System.out.println(res.getErrorMessage());
							return res;
						}
						System.out.println("8");
						sCmd = "asg|s|" + id + "|b|" + sAttrName;
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							return res;
						}

						for(int i = 0; i < io.size(); i++){
							sCmd = "add|op|" + io.get(i) + "|s|" + id;
							System.out.println(sCmd);
							res = interpretCmd(sSessId, sCmd);
							if(res.hasError()){
								return res;
							}
						}
						System.out.println("9");
					}

				}else{
					System.out.println("2");
					//create new rep object for the attr
					sCmd = "add|ob|" + sAttrName + " rep|Object attribute|yes|" + sAttrName + "|ignored|" + sBaseType + "|" + sBaseName;
					System.out.println(sCmd);
					res = interpretCmd(sSessId, sCmd);
					if(res.hasError()){
						return res;

					}
					if(admin.size() > 0){
						System.out.println("3");
						String id = UtilMethods.generateRandomName();
						sCmd = "add|s|" + id + "|oc|ignored|a|" + uattr;
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							System.out.println(res.getErrorMessage());
							return res;
						}
						System.out.println("4");
						sCmd = "asg|s|" + id + "|b|" + sAttrName + " rep";
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							return res;
						}
						System.out.println("5");
						for(int i = 0; i < admin.size(); i++){
							sCmd = "add|op|" + admin.get(i) + "|s|" + id;
							System.out.println(sCmd);
							res = interpretCmd(sSessId, sCmd);
							if(res.hasError()){
								return res;
							}
						}
						System.out.println("6");
					}
					if(io.size() > 0){
						System.out.println("7");
						String id = UtilMethods.generateRandomName();
						sCmd = "add|s|" + id + "|oc|ignored|a|" + uattr;
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							System.out.println(res.getErrorMessage());
							return res;
						}
						System.out.println("8");
						sCmd = "asg|s|" + id + "|b|" + sAttrName;
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							return res;
						}

						for(int i = 0; i < io.size(); i++){
							sCmd = "add|op|" + io.get(i) + "|s|" + id;
							System.out.println(sCmd);
							res = interpretCmd(sSessId, sCmd);
							if(res.hasError()){
								return res;
							}
						}
						System.out.println("9");
					}

				}

				return ADPacketHandler.getSuccessPacket();
			}*/

	public Packet createSchemaPC(String sSessId, String sProcId, String policyType,
								 String oattrType, String sPolicyName, String sUattr){
        System.out.println(sSessId + ":" + policyType + ":" + oattrType + ":" + sPolicyName + ":" + sUattr);

        String sCmd;
		Packet res = new Packet();
		try {
			sCmd = "add|" + policyType + "|" + sPolicyName + "|c|PM";
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			if(!entityNameExists(sUattr, PM_NODE.UATTR.value)){
				sCmd = "add|a|" + sUattr + "|" + policyType + "|" + sPolicyName;
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}
			}else{
				sCmd = "asg|a|" + sUattr + "|" + policyType + "|" + sPolicyName;
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}
			}

			String user = getSessionUser(sSessId).getStringValue(0);
			String isAsgn = isAssigned(getEntityId(user, PM_NODE.USER.value), PM_NODE.USER.value, getEntityId(sUattr, PM_NODE.UATTR.value), PM_NODE.UATTR.value).getStringValue(0);
			System.out.println(isAsgn);
			if(isAsgn.equalsIgnoreCase("NO")){
				sCmd = "asg|u|" + user + "|a|" + sUattr;
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}
			}

			sCmd = "add|" + oattrType + "|" + sPolicyName + "|" + policyType + "|" + sPolicyName;
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			String sEntType = policyType;
			String sEntId = getEntityId(sPolicyName, sEntType);

			HashSet<String> opers = new HashSet<String>();
			opers.add("*");

			String sRepOaId = getOrCreateOattrRepresentingEntity(sEntId, sEntType,
					null, sEntId, sEntType, true);

			/*res =  createOpsetBetween(sSessId, sProcId, opers,
						getEntityId(sUattr, "ua"), sRepOaId);
				if (res.hasError()) {
					return res;
				}*/

			/*sCmd = "add|ob|" + sPolicyName + " rep|Object Attribute|yes|" + sPolicyName + "|ignored|b|" + sPolicyName;
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}*/

			String id = UtilMethods.generateRandomName();
			sCmd = "add|s|" + id + "|oc|ignored|a|" + sUattr;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				System.out.println(res.getErrorMessage());
				return res;
			}

			sCmd = "asg|s|" + id + "|o|" + getEntityName(sRepOaId, "o");
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			sCmd = "add|op|" + PM_OATTR_CREATE_OATTR + "|s|" + id;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			sCmd = "add|op|" + PM_POL_CREATE_OATTR + "|s|" + id;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			/*sCmd = "add|op|*|s|" + id;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}*/

			id = UtilMethods.generateRandomName();
			sCmd = "add|s|" + id + "|oc|ignored|a|" + sUattr;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			sCmd = "asg|s|" + id + "|" + oattrType + "|" + sPolicyName;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			sCmd = "add|op|" + PM_FILE_READ + "|s|" + id;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			sCmd = "add|op|" + PM_FILE_WRITE + "|s|" + id;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket(e.getMessage());
		}

		return res;
	}

	public Packet addSchemaOattr(String sSessId, String oattrType, String name, String baseName, String baseType){
		System.out.println(sSessId + ":" + oattrType + ":" + name + ":" + baseName + ":" + baseType);
		String sCmd;
		Packet res;

		sCmd = "add|" + oattrType + "|" + name + "|" + baseType + "|" + baseName;
		res = interpretCmd(sSessId, sCmd);
		if(res.hasError()){
			return res;
		}
		return res;
	}


	public Packet setSchemaPerms(String sSessId, String sProcId, String sBaseName,
			String sBaseType, String sAttrName, String sUattr){
		String sCmd;
		Packet res;

		/*sCmd = "add|ob|" + sAttrName + " rep|Policy Class|yes|" + sAttrName + "|ignored|" + sBaseType + "|" + sBaseName;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}*/

		String id = UtilMethods.generateRandomName();
		sCmd = "add|s|" + id + "|oc|ignored|a|" + sUattr;
		System.out.println(sCmd);
		res = interpretCmd(sSessId, sCmd);
		if(res.hasError()){
			System.out.println(res.getErrorMessage());
			return res;
		}

		sCmd = "asg|s|" + id + "|b|" + sAttrName + " rep";
		System.out.println(sCmd);
		res = interpretCmd(sSessId, sCmd);
		if(res.hasError()){
			return res;
		}

		sCmd = "add|op|" + GlobalConstants.PM_OATTR_CREATE_OATTR + "|s|" + id;
		System.out.println(sCmd);
		res = interpretCmd(sSessId, sCmd);
		if(res.hasError()){
			return res;
		}

		/*sCmd = "add|op|*|s|" + id;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}*/

		id = UtilMethods.generateRandomName();
		sCmd = "add|s|" + id + "|oc|ignored|a|" + sUattr;
		System.out.println(sCmd);
		res = interpretCmd(sSessId, sCmd);
		if(res.hasError()){
			System.out.println(res.getErrorMessage());
			return res;
		}

		sCmd = "asg|s|" + id + "|b|" + sAttrName;
		System.out.println(sCmd);
		res = interpretCmd(sSessId, sCmd);
		if(res.hasError()){
			return res;
		}

		sCmd = "add|op|" + GlobalConstants.PM_FILE_READ + "|s|" + id;
		System.out.println(sCmd);
		res = interpretCmd(sSessId, sCmd);
		if(res.hasError()){
			return res;
		}

		sCmd = "add|op|" + GlobalConstants.PM_FILE_WRITE + "|s|" + id;
		System.out.println(sCmd);
		res = interpretCmd(sSessId, sCmd);
		if(res.hasError()){
			return res;
		}
		return ADPacketHandler.getSuccessPacket();//();
	}



	public Packet setTablePerms(String sSessId, String sProcId, String sBaseName, 
			String sBaseType, String sAttrName, String sAttrType, String perms, String uattr){//TODO
		String sCmd;
		Packet res;

		if (allIoOpers == null) {
			allIoOpers = new HashSet<String>();
			for (int i = 0; i < GlobalConstants.sDirOps.length; i++) {
				allIoOpers.add(GlobalConstants.sDirOps[i]);
			}
			for (int i = 0; i < GlobalConstants.sFileOps.length; i++) {
				allIoOpers.add(GlobalConstants.sFileOps[i]);
			}
		}
		System.out.println("0");
		ArrayList<String> io = new ArrayList<String>();
		ArrayList<String> admin = new ArrayList<String>();
		String[] pieces = perms.split(",");
		System.out.println("1");
		System.out.println(allIoOpers);
		for(int i = 0; i < pieces.length; i++){
			String op = pieces[i];
			if(allIoOpers.contains(op)){
				io.add(op);
				System.out.println("op addded to io");
			}else if(!allIoOpers.contains(op)){
				admin.add(op);
				System.out.println("op addded to admin");
			}
		}

		String sEntType = sAttrType;
		String sEntId = getEntityId(sAttrName, sEntType);
		//String repId = getEntityId(sAttrName + " rep", "b");

		//a rep object already exists for this attribute
		//add new opset to the rep object
		if(admin.size() > 0){
			String sRepOaId = getOrCreateOattrRepresentingEntity(sEntId, sEntType,
					null, sEntId, sEntType, true);

			String id = UtilMethods.generateRandomName();
			sCmd = "add|s|" + id + "|oc|ignored|a|" + uattr;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				System.out.println(res.getErrorMessage());
				return res;
			}

			sCmd = "asg|s|" + id + "|b|" + getEntityName(sRepOaId, PM_NODE.OATTR.value);
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			for(int i = 0; i < admin.size(); i++){
				sCmd = "add|op|" + admin.get(i) + "|s|" + id;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}
			}
		}
		if(io.size() > 0){
			String id = UtilMethods.generateRandomName();
			sCmd = "add|s|" + id + "|oc|ignored|a|" + uattr;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				System.out.println(res.getErrorMessage());
				return res;
			}
			sCmd = "asg|s|" + id + "|b|" + sAttrName;
			System.out.println(sCmd);
			res = interpretCmd(sSessId, sCmd);
			if(res.hasError()){
				return res;
			}

			for(int i = 0; i < io.size(); i++){
				sCmd = "add|op|" + io.get(i) + "|s|" + id;
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;
				}
			}
		}
		/*
					String id = generateRandomName();
					sCmd = "add|s|" + id + "|oc|ignored|a|" + uattr;
					System.out.println(sCmd);
					res = interpretCmd(sSessId, sCmd);
					if(res.hasError()){
						System.out.println(res.getErrorMessage());
						return res;
					}

					sCmd = "asg|s|" + id + "|b|" + repName;
					System.out.println(sCmd);
					res = interpretCmd(sSessId, sCmd);
					if(res.hasError()){
						return res;
					}

					String[] pieces = perms.split(",");
					for(int i = 0; i < pieces.length; i++){
						sCmd = "add|op|" + pieces[i] + "|s|" + id;
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							return res;
						}
					}

					/*String id1 = getEntityId(uattr, "a");
					String type1 = "a";
					String id2 = getEntityId("Schema Viewers", "a");
					String type2 = "a";

					if(!isContained(id1, type1, id2, type2)){
						sCmd = "asg|a|" + uattr + "|a|Schema Viewers";
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							return res;
						}
					}*/
		/*
					id = generateRandomName();
					sCmd = "add|s|" + id + "|oc|ignored|a|Schema Viewers";
					System.out.println(sCmd);
					res = interpretCmd(sSessId, sCmd);
					if(res.hasError()){
						System.out.println(res.getErrorMessage());
						return res;
					}

					sCmd = "asg|s|" + id + "|b|" + sAttrName;
					System.out.println(sCmd);
					res = interpretCmd(sSessId, sCmd);
					if(res.hasError()){
						return res;
					}

					sCmd = "add|op|File read|s|" + id;
					System.out.println(sCmd);
					res = interpretCmd(sSessId, sCmd);
					if(res.hasError()){
						return res;
					}


					/*String id1 = getEntityId(uattr, "a");
					String type1 = "a";
					String id2 = getEntityId("Schema Viewers", "a");
					String type2 = "a";

					if(!isContained(id1, type1, id2, type2)){
						sCmd = "asg|a|" + uattr + "|a|Schema Viewers";
						System.out.println(sCmd);
						res = interpretCmd(sSessId, sCmd);
						if(res.hasError()){
							return res;
						}
					}*/
		//}

		/*sCmd = "asg|s|" + id + "|b|" + sAttrName + " rep";
				System.out.println(sCmd);
				res = interpretCmd(sSessId, sCmd);
				if(res.hasError()){
					return res;*/


		return ADPacketHandler.getSuccessPacket();//();
	}


	// sFileProp is the name of a property. This method first finds the value
	// of that property, which is used as the absolute path of the file.

	public Packet getFileContent(String sSessId, String sFileProp,
			InputStream bisFromClient, OutputStream bosToClient) {
		// First get the file location on the server.
		String sPath = getFilePath(sSessId, sFileProp);
		if (sPath == null) {
			return failurePacket("No file with property: " + sFileProp + "!");
		}

		File f = new File(sPath);
		if (!f.exists() || !f.isFile() || !f.canRead()) {
			return failurePacket("Something wrong with file " + sPath + "!");
		}
		try {
			FileInputStream fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			PacketManager.sendPacket(bis, (int) f.length(), bosToClient);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when creating streams for the requested file!");
		}
		return dnrPacket();
	}




	public Packet genMls(String sSessId, String sPolName, String sLevels) {
		// Check permissions...

		// If a policy with the property "type=mls" already exists, return.
		if (getEntityWithPropInternal(PM_NODE.POL.value, "type=mls") != null) {
			return failurePacket("A multi-level security policy already exists!");
		}

		// Generate commands for creating the policy class.
		String sCmd;
		Packet res;

		sCmd = "add|p|" + sPolName + "|c|PM";
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|prop|type=mls|p|" + sPolName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|prop|levels=" + sLevels + "|p|" + sPolName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Get the levels in an array.
		String[] levels = sLevels.split(GlobalConstants.PM_LIST_MEMBER_SEP);
		int n = levels.length;
		if (n < 2) {
			return failurePacket("Too few security levels!");
		}

		// Generate the user attributes.
		for (int i = 1; i <= n; i++) {
			if (i == 1) {
				sCmd = "add|a|" + levels[i - 1] + "|p|" + sPolName;
			} else {
				sCmd = "add|a|" + levels[i - 1] + "|a|" + levels[i - 2];
			}
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
		}

		// Generate the object attribute x1...xn.
		StringBuffer sb = new StringBuffer();
		for (int i = 1; i <= n; i++) {
			sb.append(levels[i - 1]);
		}
		String sAll = sb.toString();
		sCmd = "add|b|" + sAll + "|p|" + sPolName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Prepare the array sRight used to store the object attributes
		// x1...xn, x2...xn, x3...xn, ..., xn, and the array sLeft used to store
		// the object attributes x1...xn, x1...xn-1, ..., x1.
		String[] sRight = new String[n];
		sRight[0] = sAll;
		String[] sLeft = new String[n];
		sLeft[0] = sAll;
		// Generate the other names in the arrays.
		for (int i = 2; i <= n; i++) {
			sb = new StringBuffer();
			for (int j = i; j <= n; j++) {
				sb.append(levels[j - 1]);
			}
			sRight[i - 1] = sb.toString();
			System.out.println("Generated right name " + sRight[i - 1]);
		}

		for (int i = 2; i <= n; i++) {
			sb = new StringBuffer();
			for (int j = 1; j <= n - i + 1; j++) {
				sb.append(levels[j - 1]);
			}
			sLeft[i - 1] = sb.toString();
			System.out.println("Generated left name " + sLeft[i - 1]);
		}

		// Generate the object attributes.
		for (int i = 2; i <= n; i++) {
			sCmd = "add|b|" + sRight[i - 1] + "|b|" + sRight[i - 2];
			System.out.println("Trying to generate container " + sRight[i - 1]
					+ " in " + sRight[i - 2]);
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
		}

		for (int i = 2; i <= n; i++) {
			sCmd = "add|b|" + sLeft[i - 1] + "|b|" + sLeft[i - 2];
			System.out.println("Trying to generate container " + sLeft[i - 1]
					+ " in " + sLeft[i - 2]);
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
		}

		// Generate the object attributes x2,...,xn-1 and assign them as
		// follows (the indexes start with 1; in the code they start with 0):
		// x2 -> leftn-1, x2 -> right2
		// x3 -> leftn-2, x3 -> right3
		// ...
		// xn-1 -> left2, xn-1 -> rightn-1
		for (int i = 2; i <= n - 1; i++) {
			sCmd = "add|b|" + levels[i - 1] + "|b|" + sLeft[n - i];
			System.out.println("Trying to generate container " + levels[i - 1]
					+ " in " + sLeft[n - i]);
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}

			sCmd = "asg|b|" + levels[i - 1] + "|b|" + sRight[i - 1];
			System.out.println("Trying to assign container " + levels[i - 1]
					+ " in " + sRight[i - 1]);
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
		}

		// Generate and assign the operation sets:
		// xi -> {read} -> x1...xi (i.e., left[n-i+1]),
		// xi -> {write} -> xi...xn (i.e., right[i]).
		for (int i = 1; i <= n; i++) {
			String sOpsName = UtilMethods.generateRandomName();
			sCmd = "add|s|" + sOpsName + "|oc|ignored|a|" + levels[i - 1];
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sCmd = "asg|s|" + sOpsName + "|b|" + sLeft[n - i];
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sCmd = "add|op|File read|s|" + sOpsName;
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}

			sOpsName = UtilMethods.generateRandomName();
			sCmd = "add|s|" + sOpsName + "|oc|ignored|a|" + levels[i - 1];
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sCmd = "asg|s|" + sOpsName + "|b|" + sRight[i - 1];
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sCmd = "add|op|File write|s|" + sOpsName;
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
		}

		// Generate the objects that represent containers x1,...,xn:
		for (int i = 1; i <= n; i++) {
			sCmd = "add|ob|" + levels[i - 1] + " rep|Object attribute|no|"
					+ levels[i - 1] + "|ignored|p|" + sPolName;
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}

			String sOpsName = UtilMethods.generateRandomName();
			sCmd = "add|s|" + sOpsName + "|oc|ignored|a|" + levels[i - 1];
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sCmd = "asg|s|" + sOpsName + "|b|" + levels[i - 1] + " rep";
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sCmd = "add|op|Object attribute assign to|s|" + sOpsName;
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sCmd = "add|op|Object attribute create object|s|" + sOpsName;
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
		}

		// Add the subject attribute constraints.
		// first the attribute sets.
		sCmd = "add|sac|mls|intra session";
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		for (int i = 1; i <= n; i++) {
			sCmd = "add|as|" + levels[i - 1];
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}

			sCmd = "add|a|" + levels[i - 1] + "|as|" + levels[i - 1];
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}

			sCmd = "add|as|" + levels[i - 1] + "|sa|mls";
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
		}

		// Generate the EVER script for MLS.
		Packet script = new Packet();
		// The function that processes the script expects it to start at item 3.
		try {
			script.addItem(ItemType.RESPONSE_TEXT, "Filler");
			script.addItem(ItemType.RESPONSE_TEXT, "Filler");
			script.addItem(ItemType.RESPONSE_TEXT, "Filler");
			script.addItem(ItemType.RESPONSE_TEXT, "script mls");
			script.addItem(ItemType.RESPONSE_TEXT, "");
			script.addItem(ItemType.RESPONSE_TEXT, "when any user performs \""
					+ GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			script.addItem(ItemType.RESPONSE_TEXT, "do");
			script.addItem(ItemType.RESPONSE_TEXT,
					"  assign user user_new() to");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    user attribute uattr_lowest_level()");

			script.addItem(ItemType.RESPONSE_TEXT, "when any user performs \""
					+ GlobalConstants.PM_EVENT_OBJECT_CREATE + "\"");
			script.addItem(ItemType.RESPONSE_TEXT, "do");
			script.addItem(ItemType.RESPONSE_TEXT,
					"  assign object object_new() to");
			script.addItem(
					ItemType.RESPONSE_TEXT,
					"    object attribute oattr_with_name_of_active_attr(pol_with_prop(\"type=mls\"))");
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the script packet!");
		}

		// Compile and submit the script.
		res =  compileScriptAndAddToEnabled(sSessId, script);
		if (res == null) {
			return failurePacket("Null result from addScript call");
		}
		return res;
	}

	// Configure a DAC policy. Parameters:
	// sPolName: the DAC policy name.
	// sContName: the name of a user attribute that will contain all users'
	// name attributes.

	public Packet genDac(String sSessId, String sPolName, String sContName) {
		// Check permissions...

		// If a policy with the property "type=discretionary" already exists,
		// return.
		if (getEntityWithPropInternal(PM_NODE.POL.value, "type=discretionary") != null) {
			return failurePacket("A discretionary policy already exists!");
		}

		String sCmd;
		Packet res;

		// Create the policy class.
		sCmd = "add|p|" + sPolName + "|c|PM";
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|prop|type=discretionary|p|" + sPolName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Create the attribute container and its representative object.
		sCmd = "add|a|" + sContName + "|p|" + sPolName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|prop|usersof=discretionary|a|" + sContName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|ob|" + sContName + " rep|User attribute|yes|" + sContName
				+ "|ignored|p|" + sPolName;
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Generate the EVER script for "User create".
		Packet script = new Packet();
		// The function that processes the script expects it to start at item 3.
		try {
			script.addItem(ItemType.RESPONSE_TEXT, "Filler");
			script.addItem(ItemType.RESPONSE_TEXT, "Filler");
			script.addItem(ItemType.RESPONSE_TEXT, "Filler");
			script.addItem(ItemType.RESPONSE_TEXT, "script dac");
			script.addItem(ItemType.RESPONSE_TEXT, "");
			script.addItem(ItemType.RESPONSE_TEXT, "when any user performs \""
					+ GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			script.addItem(ItemType.RESPONSE_TEXT, "do");
			script.addItem(ItemType.RESPONSE_TEXT,
					"  create user attribute uattr_name_of_user(user_new())");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    with property prop_name_of_user(user_new())");
			script.addItem(ItemType.RESPONSE_TEXT, "    in policy pol_discr()");

			script.addItem(ItemType.RESPONSE_TEXT,
					"  assign user user_new() to user attribute uattr_name_of_new_user()");

			script.addItem(ItemType.RESPONSE_TEXT,
					"  assign user attribute uattr_name_of_new_user() to");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    user attribute uattr_discr_users()");

			script.addItem(ItemType.RESPONSE_TEXT,
					"  create object attribute oattr_home_of_new_user()");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    with property prop_home_of_new_user()");
			script.addItem(ItemType.RESPONSE_TEXT, "    in policy pol_discr()");

			script.addItem(ItemType.RESPONSE_TEXT,
					"  create object obj_rep_of_home_of_new_user()");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    representing object attribute oattr_home_of_new_user() and ascendants");
			script.addItem(ItemType.RESPONSE_TEXT, "    in policy pol_discr()");

			script.addItem(ItemType.RESPONSE_TEXT,
					"  grant uattr_name_of_new_user() operations");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    \"File read\", \"File write\"");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    on object attribute oattr_home_of_new_user()");

			script.addItem(ItemType.RESPONSE_TEXT,
					"  grant uattr_name_of_new_user() operations");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    \"Object attribute create object\",");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    \"Object attribute create object attribute\",");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    \"Object attribute create operation set\",");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    \"Object attribute assign\",");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    \"Object attribute assign to\",");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    \"Operation set assign to\",");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    \"Operation set assign\",");
			script.addItem(ItemType.RESPONSE_TEXT, "    \"Entity represent\"");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    on object attribute oattr_rep_of_home_of_new_user()");

			script.addItem(ItemType.RESPONSE_TEXT,
					"  grant uattr_name_of_new_user() operations");
			script.addItem(ItemType.RESPONSE_TEXT, "    \"User assign\",");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    \"User attribute assign to operation set\"");
			script.addItem(ItemType.RESPONSE_TEXT,
					"    on object attribute oattr_rep_of_discr_users()");
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}

		// Compile and submit the script.
		// boolean bScriptCompiled = sysCaller.addScript(script);
		// System.out.println("Result from compile script: " + bScriptCompiled);

		res =  compileScriptAndAddToEnabled(sSessId, script);
		if (res == null) {
			return failurePacket("Null result from addScript call");
		}
		if (res.hasError()) {
			return res;
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet doDacConfinement(String sSessId, String sUser, String sPc,
			String sAttr, String sContainer) {
		// Check permissions...
		// Not yet.

		// Generate commands for creating needed entities and relations,
		// and call interpretCmd(sSessId, sSessId, command) repeatedly
		// to process them.
		String sCmd;
		Packet res;

		// If the confinement policy class does not exist, create it.
		if (getEntityId(sPc,PM_NODE.POL.value) == null) {
			sCmd = "add|p|" + sPc + "|c|PM";
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
			sCmd = "add|prop|type=confinement|p|" + sPc;
			res =  interpretCmd(sSessId, sCmd);
			if (res.hasError()) {
				return res;
			}
		}
		ArrayList<String> commands = new ArrayList<String>();//TODO
		// Create the new confine attribute and assign the user to it.
		// Create an object that represents the confine attribute in
		sCmd = "add|a|" + sAttr + "|p|" + sPc;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|ob|" + sAttr + " rep|User attribute|no|" + sAttr
				+ "|ignored|p|" + sPc;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "asg|u|" + sUser + "|a|" + sAttr;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Create the sensitive container and its representative object.
		sCmd = "add|b|" + sContainer + "|p|" + sPc;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|ob|" + sContainer + " rep|Object attribute|no|"
				+ sContainer + "|ignored|p|" + sPc;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Create and assign an opset that allows users with the confine
		// attribute
		// to assign other users to the confine attribute.
		String sName = UtilMethods.generateRandomName();
		sCmd = "add|s|" + sName + "|oc|ignored|a|" + sAttr;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "asg|s|" + sName + "|b|" + sAttr + " rep";
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|User attribute assign to|s|" + sName;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Create and assign an opset that allows users with the confine
		// attribute
		// to create objects in the sensitive container and to assign object
		// attributes (including objects) to the sensitive container.
		sName = UtilMethods.generateRandomName();
		sCmd = "add|s|" + sName + "|oc|ignored|a|" + sAttr;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "asg|s|" + sName + "|b|" + sContainer + " rep";
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|Object attribute assign to|s|" + sName;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|Object attribute create object|s|" + sName;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Create and assign an opset that allows users with the confine
		// attribute
		// to read/write objects of the sensitive container.
		sName = UtilMethods.generateRandomName();
		sCmd = "add|s|" + sName + "|oc|ignored|a|" + sAttr;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "asg|s|" + sName + "|b|" + sContainer;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|File read|s|" + sName;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|File write|s|" + sName;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Create a deny, attribute-based, across sessions, denying File write
		// on the complement of the sensitive container.
		sName = UtilMethods.generateRandomName();
		sCmd = "add|deny|" + sName + "|" + GlobalConstants.PM_DENY_ACROSS_SESSIONS + "|a|"
				+ sAttr + "|no";
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|File write|deny|" + sName;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|cb|" + sContainer + "|deny|" + sName;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}

		// Create a deny, attribute-based, across sessions, denying Create
		// object
		// on the complement of the sensitive container.
		sName = UtilMethods.generateRandomName();
		sCmd = "add|deny|" + sName + "|" + GlobalConstants.PM_DENY_ACROSS_SESSIONS + "|a|"
				+ sAttr + "|no";
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|op|Object attribute create object|deny|" + sName;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		sCmd = "add|cb|" + sContainer + "|deny|" + sName;
		commands.add(sCmd);
		res =  interpretCmd(sSessId, sCmd);
		if (res.hasError()) {
			return res;
		}
		try{
			// TODO: Move hardcoded value to config file. For SteveQ, 
			// replace WIN-DNAR5079LMF with win08-SQ.
			FileWriter fw = new FileWriter("C:\\Users\\Administrator.win08-SQ\\Desktop\\output.txt");
			for(int i = 0; i < commands.size(); i++){
				System.out.println(commands.get(i));
				fw.append(commands.get(i) + "\r\n");
				fw.flush();
			}
			fw.close();
		}catch(IOException e){
			e.printStackTrace();
		}

		return ADPacketHandler.getSuccessPacket();
	}



	public boolean containersIntersect(String sContId1, String sContId2) {
		HashSet<String> members = new HashSet<String>();
		getMemberObjects(sContId1, members);
		Iterator<String> membersIter = members.iterator();
		while (membersIter.hasNext()) {
			String sAssocId = membersIter.next();
			if (attrIsAscendant(sAssocId, sContId2,PM_NODE.OATTR.value)) {
				return true;
			}
		}
		return false;
	}



	// The function setPerms() tries to create or update a set of operations and
	// establish a double assignment ua -> opset -> oa.
	//
	// Algorithm for setting permissions
	// Input:
	// Ua: the uattr we grant permissions.
	// TheOpset: the operation set we want to update.
	// SuggOa: the suggested object attribute to assign the opset to. May be
	// null.
	// SuggBase: the suggested container (object attribute or policy) to contain
	// the object attribute to assign the opset to. May be null.
	// Ent: the entity on which we grant permissions.
	// Opers: the operations for the permissions we grant.
	// IncludesAscs: true when the admin permissions need to be set on a
	// sub-graph
	// Including the entities ascendants.
	// Return value:
	// true (success) or false (failure).
	// Pseudo-code:
	// Boolean SetPerms(Ua, TheOpset, SuggOa, SuggBase, Ent, Opers, IncludeAscs)
	// {
	// If (Ua == null || Ua does not exist) return false;
	// If (TheOpset != null && TheOpset does not exist) return false;
	// If (Ent == null || Ent does not exist) return false;
	// If (SuggBase != null && SuggBase does not exist) return false;
	// If (SuggBase != null && SuggBase.type != oattr && SuggBase.type !=
	// policy)
	// return false;
	// If (SuggOa != null && SuggOa does not exist) return false;
	// // If we have an opset to upate, then update it with the operations.
	// // We ignore all other params, although we should check some conditions
	// If (TheOpset != null) {
	// TheOpset.operations = Opers;
	// Return true;
	// }
	// // We need to separate the admin operations from the i/o operations.
	// Set AdminOpers = Opers - FilePerms 4 DirPerms;
	// Set IoOpers = Opers - adminOpers;
	// // First, the case when the entity is an object that represents a PM
	// // sub-graph.
	// If (Ent.type ==PM_NODE.ASSOC.value && Ent is a rep object) {
	// // Ignore the i/o operations and create an opset with the admin
	// operations
	// // between Ua and Ent.
	// If (AdminOpers g ) {
	// Create opset TheOpset with TheOpset.operations = AdminOpers;
	// Assign Ua t TheOpset t Ent;
	// }
	// Return true;
	// }
	//
	// // Now the entity can be a File object, an object attribute, a policy,
	// // a user, a user attribute, the connector, or an operation set.
	//
	// // The i/o operations are allowed only on an object attribute or File
	// object.
	// If (Ent.type ==PM_NODE.ASSOC.value || Ent.type ==PM_NODE.OATTR.value) {
	// If (IoOpers g ) {
	// Create opset TheOpset with TheOpset.operations = IoOpers;
	// Assign Ua t TheOpset t Ent;
	// }
	// }
	//
	// // Let's process the admin operations.
	// If (AdminOpers == ?) return true;
	//
	// // First get an existing object or create one that represents the entity.
	// Oattr RepOa = getOrCreateOaRepresentingEntity(Ent, SuggOa, SuggBase,
	// IncludesAscs);
	// If (RepOa == null) return false;
	// CreateOpseBetween(AdminOpers, Ua, RepOa);
	// Return true;
	// }
	//
	// Oattr getOrCreateOaRepresentingEntity(Ent, SuggOa, SuggBase,
	// IncludesAscs) {
	// // If the suggested oattr is null
	// If (SuggOa == null) {
	// // If suggested base is null, find an oattr that represents the subgraph
	// // specified by the entity and IncludesAscs.
	// // Otherwise, create an oattr within the suggested base that represents
	// // the subgraph specified by the entity and IncludesAscs.
	// If (SuggBase == null) {
	// Return getOaRepresentingEntity(Ent, IncludesAscs);
	// } else {
	// Return createOaRepresentingEntity(Ent, SuggBase, IncludesAscs);
	// }
	// }
	// The suggested oattr is not null. Use it if it represents
	// the entity and ignore the suggested base. Otherwise, return null.
	// If (oaRepresentsEntity(SuggOa, Ent, IncludesAscs)) return SuggOa;
	// Return null;
	// }

	public Packet setPerms(String sSessId, String sProcId, String sUattrName,
			String sOpsetName, String sSuggOattr, String sSuggBase,
			String sSuggBaseType, String sOpers, String sEntName,
			String sEntType, String sIncludesAscs) {
		System.out.println("Uattr: " + sUattrName);
		System.out.println("Opset: " + sOpsetName);
		System.out.println("Suggested oattr: " + sSuggOattr);
		System.out.println("Suggested base: " + sSuggBase);
		System.out.println("Perms: " + sOpers);
		System.out.println("Entity: " + sEntName + "(" + sEntType + ")");
		System.out.println("Ascendants: " + sIncludesAscs);

		boolean isSubgraph = sIncludesAscs.equalsIgnoreCase("yes");

		String sUattrId = getEntityId(sUattrName,PM_NODE.UATTR.value);
		if (sUattrId == null) {
			return failurePacket("No user attribute " + sUattrName);
		}

		String sEntId = getEntityId(sEntName, sEntType);
		if (sEntId == null) {
			return failurePacket("No entity " + sEntName + " of type "
					+ sEntType);
		}

		String sSuggBaseId = null;
		if (sSuggBase != null) {
			sSuggBaseId = getEntityId(sSuggBase, sSuggBaseType);
			if (sSuggBaseId == null) {
				return failurePacket("No such suggested base " + sSuggBase);
			}
		}

		String sSuggOattrId = null;
		if (sSuggOattr != null) {
			sSuggOattrId = getEntityId(sSuggOattr,PM_NODE.OATTR.value);
			if (sSuggOattrId == null) {
				return failurePacket("No such suggested object attribute "
						+ sSuggOattr);
			}
		}

		/*
		 * HashSet hs = getEntityDirectRepOattrs(sEntId, sEntType,
		 * sIncludesAscs.equalsIgnoreCase("yes")); printSet(hs, GlobalConstants.PM_PERM,
		 * "Direct representatives of entity " + sEntName);
		 */

		// If the opset name is specified, simply set its permissions equal to
		// sPerms.
		if (sOpsetName != null) {
			System.out.println("Opset name specified, set its operations to "
					+ sOpers);
			String sOpsetId = getEntityId(sOpsetName,PM_NODE.OPSET.value);
			if (sOpsetId == null) {
				return failurePacket("No operation set " + sOpsetName);
			}

			// Set the opset permissions as specified by sPerms.
			setOpsetOps(sOpsetId, sOpers);
			return ADPacketHandler.getSuccessPacket();
		}

		// No opset name specified. We treat I/O and admin operations
		// differently,
		// so get the set of all I/O operations if you didn't already do so.
		if (allIoOpers == null) {
			allIoOpers = new HashSet<String>();
			for (int i = 0; i < GlobalConstants.sDirOps.length; i++) {
				allIoOpers.add(GlobalConstants.sDirOps[i]);
			}
			for (int i = 0; i < GlobalConstants.sFileOps.length; i++) {
				allIoOpers.add(GlobalConstants.sFileOps[i]);
			}
		}

		//TODO Split the set of operations into I/O and administrative operations.
		HashSet<String> adminOpers = UtilMethods.stringToSet(sOpers);
		adminOpers.removeAll(allIoOpers);
		printSet(adminOpers, GlobalConstants.PM_PERM,
				"Trying to set administrative operations on " + sEntName
				+ " for " + sUattrName);

		HashSet<String> ioOpers = UtilMethods.stringToSet(sOpers);
		ioOpers.retainAll(allIoOpers);
		printSet(ioOpers, GlobalConstants.PM_PERM, "Trying to set I/O operations on "
				+ sEntName + " for " + sUattrName);

		Packet res;
		if (sEntType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			if (oattrRepresentsAnEntity(sEntId)) {
				// The entity is an object attribute associated to an object
				// that represents some entity or subgraph.
				// We ignore the I/O operations and set only the admin
				// operations
				// in a new opset.
				if (adminOpers.isEmpty()) {
					return ADPacketHandler.getSuccessPacket();
				}
				res =  createOpsetBetween(sSessId, sProcId, adminOpers,
						sUattrId, sEntId);
				if (res.hasError()) {
					return res;
				}
				return ADPacketHandler.getSuccessPacket();
			}
		}

		// Now the entity is an object attribute associated to a File/Dir
		// object,
		// or an object attribute not associated, or the connector node, or a
		// policy
		// class, or a user attribute, or a user. The I/O operations are allowed
		// only on an object attribute.
		if (sEntType.equalsIgnoreCase(PM_NODE.ASSOC.value)
				|| sEntType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			if (!ioOpers.isEmpty()) {
				res =  createOpsetBetween(sSessId, sProcId, ioOpers,
						sUattrId, sEntId);
				if (res.hasError()) {
					return res;
				}
			}
		}

		// Now process the administrative operations.
		if (adminOpers.isEmpty()) {
			return ADPacketHandler.getSuccessPacket();
		}

		// First get an existing or create a representative object attribute
		// for the entity, according to isSubgraph.
		String sRepOaId = getOrCreateOattrRepresentingEntity(sEntId, sEntType,
				sSuggOattrId, sSuggBaseId, sSuggBaseType, isSubgraph);

		if (sRepOaId == null) {
			return failurePacket("Could not find or create a suitable representative object for entity "
					+ sEntName);
		}

		res =  createOpsetBetween(sSessId, sProcId, adminOpers,
				sUattrId, sRepOaId);
		if (res.hasError()) {
			return res;
		}
		return ADPacketHandler.getSuccessPacket();
	}

	/**
	 * @uml.property  name="allIoOpers"
	 */
	HashSet<String> allIoOpers = null;

	// This function finds or creates an object attribute associated to an
	// object that represents a given entity/subgraph.
	//
	// if (suggested oattr and base are both null) {
	// if (an object attribute oa that represents the entity or its subgraph
	// exists) return oa;
	// else return null.
	// } else if (suggested oattr is not null and the suggested base is null) {
	// if (suggested oattr represents the entity or its subgraph) return
	// suggested oattr;
	// else return null.
	// } else if (suggested oattr is null and the suggested base is not null) {
	// create an object representing the entity or its subgraph within the
	// suggested base;
	// return the associated oattr;
	// } else {
	// return null;
	// }

	public String getOrCreateOattrRepresentingEntity(String sEntId,
			String sEntType, String sSuggOattrId, String sSuggBaseId,
			String sSuggBaseType, boolean isSubgraph) {
		String sRepOaId;

		if (sSuggOattrId == null) {
			if (sSuggBaseId == null) {
				System.out.println("Suggested oattr = null, suggested base = null");
				// Try to find an oattr that represents the entity or subgraph.
				sRepOaId = getOattrRepresentingEntity(sEntId, sEntType,
						isSubgraph);
				System.out.println("Got oattr "
						+ getEntityName(sRepOaId,PM_NODE.OATTR.value)
						+ " representing entity "
						+ getEntityName(sEntId, sEntType));
				return sRepOaId;
			} else {
				// We have a suggested base. Create an oattr that represents the
				// entity
				// according to isSubgraph in the suggested base.

				sRepOaId = createOattrRepresentingEntity(sSuggBaseId,
						sSuggBaseType, sEntId, sEntType, isSubgraph);
				return sRepOaId;
			}
		} else {
			// We have a suggested object attribute. We'll use it only if it
			// represents
			// our entity. We don't care about the suggested base.
			// When invoked from the admin tool, this case doesn't take place,
			// because the opset is also selected, so the opset will get
			// updated.
			if (oattrRepresentsThisEntity(sSuggOattrId, sEntId, sEntType,
					isSubgraph)) {
				System.out.println("Oattr " + sSuggOattrId
						+ " represents the entity or subgraph!");
				return sSuggOattrId;
			} else {
				System.out.println("Oattr " + sSuggOattrId
						+ " does not represent the entity or subgraph!");
			}
		}
		return null;
	}

	// Return first oattr (if any) you find associated to an object that
	// represents
	// the entity according to isSubgraph.

	public String getOattrRepresentingEntity(String sEntId, String sEntType,
			boolean isSubgraph) {
		// Search PmVirtualObjContainer for objects x such that:
		// x represents a PM entity and
		// x.origid = entity.id and
		// x.includesAscs = isSubgraph
		String sObjId;
		NamingEnumeration<?> objects;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId",
					"pmOriginalId", "pmIncludesAscendants"});
			objects = ServerConfig.ctx.search(sVirtualObjContainerDN, "(objectClass=*)",
					constraints);
			while (objects != null && objects.hasMore()) {
				SearchResult sr = (SearchResult) objects.next();
				Attributes attrs = sr.getAttributes();
				sObjId = (String) attrs.get("pmId").get();
				Attribute attr = attrs.get("pmOriginalId");
				if (attr == null) {
					continue;
				}
				String sOrigId = (String) attr.get();
				if (!sOrigId.equalsIgnoreCase(sEntId)) {
					continue;
				}
				attr = attrs.get("pmIncludesAscendants");
				if (((String) attr.get()).equalsIgnoreCase("TRUE") == isSubgraph) {
					String sOattrId = getAssocOattr(sObjId);
					if (sOattrId == null) {
						continue;
					}
					return sOattrId;
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// Returns true iff the oattr is associated to an object that represents
	// the PM entity or subgraph, according to the param isSubgraph.

	public boolean oattrRepresentsThisEntity(String sOattrId, String sEntId,
			String sEntType, boolean isSubgraph) {
		// Get the assoc object, if any.
		String sObjId = getAssocObj(sOattrId);
		if (sObjId == null) {
			return false;
		}
		// Test whether pmOriginalId == sEntId.
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			Attribute attr = attrs.get("pmOriginalId");
			if (attr == null) {
				return false;
			}
			String sOrigId = (String) attr.get();
			if (!sOrigId.equalsIgnoreCase(sEntId)) {
				return false;
			}
			// Even when the object represents the entity, the
			// pmIncludesAscendants
			// must have the same value as isSubgraph.
			String sInclAscs = (String) attrs.get("pmIncludesAscendants").get();
			if (isSubgraph) {
				return sInclAscs.equalsIgnoreCase("true");
			} else {
				return sInclAscs.equalsIgnoreCase("false");
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}

	// Returns true if the argument is an object that represents a PM entity
	// as needed by AdminVos (i.e., one in {pm} U PC U UA U U U OA).

	public boolean objRepresentsAGraphEntity(String sObjId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			Attribute attr = attrs.get("pmObjClass");
			if (attr == null || attr.size() <= 0) {
				System.out.println("objReprAnEntity: pmObjClass is null or empty");
				return false;
			}
			String sClass = (String) attr.get();
			System.out.println("objReprAnEntity: sClass is " + sClass);
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_UATTR_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_OATTR_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_CONN_NAME)) {
				return true;
			}
			if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_POL_NAME)) {
				return true;
			}
			return false;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}

	// Returns true iff the oattr is associated to an object that represents
	// a PM entity.

	public boolean oattrRepresentsAnEntity(String sOattrId) {
		// Get the assoc object, if any.
		String sObjId = getAssocObj(sOattrId);
		if (sObjId == null) {
			return false;
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			// pmOriginalName must be set for representative objects.
			Attribute attr = attrs.get("pmOriginalName");
			return (attr != null);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}

	// Create an opset with the operations specified in opSet and insert it
	// between
	// the user attribute and the object attribute. Return the id of the new
	// opset
	// or null in case of error.

	public Packet createOpsetBetween(String sSessId, String sProcId,
			HashSet<String> opSet, String sUattrId, String sOattrId) {
		Packet res = null;

		// Check some permissions.
		if (!requestAddOpsetPerms(sSessId, sProcId, sOattrId,PM_NODE.OATTR.value)) {
			return failurePacket(reqPermsMsg);
		}

		// Generate a name for the operation set.
		Random random = new Random();
		byte[] bytes = new byte[4];
		random.nextBytes(bytes);
		String sOpsetName = UtilMethods.byteArray2HexString(bytes);

		// Create an empty opset object between the uattr and oattr.
		Attributes attrs = new BasicAttributes(true);
		RandomGUID myGUID = new RandomGUID();
		String sOpsetId = myGUID.toStringNoDashes();
		attrs.put("objectClass", sOpsetClass);
		attrs.put("pmId", sOpsetId);
		attrs.put("pmName", sOpsetName);
		attrs.put("pmDescription", "Auto opset.");
		attrs.put("pmOtherInfo", "No info.");
		attrs.put("pmObjClass", "Ignored");
		try {
			ServerConfig.ctx.bind("CN=" + sOpsetId + "," + sOpsetContainerDN, null, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Could not create the opset object: "
					+ e.getMessage());
		}
		res =  addDoubleLink(sOpsetId,PM_NODE.OPSET.value, sOattrId,
				PM_NODE.OATTR.value);
		if (res.hasError()) {
			return res;
		}

		// Insert here the permission check.
		if (!requestAssignPerms(sSessId, sProcId, sUattrId,PM_NODE.UATTR.value,
				sOpsetId,PM_NODE.OPSET.value)) {
			return failurePacket(reqPermsMsg);
		}

		res =  addDoubleLink(sUattrId,PM_NODE.UATTR.value, sOpsetId,
				PM_NODE.OPSET.value);
		if (res.hasError()) {
			return res;
		}

		// Add the operations.
		if (!opSet.isEmpty()) {
			int n = opSet.size();
			ModificationItem[] adds = new ModificationItem[n];
			Iterator<String> iter = opSet.iterator();
			int i = 0;
			while (iter.hasNext()) {
				adds[i++] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOp", iter.next()));
			}
			try {
				ServerConfig.ctx.modifyAttributes(
						"CN=" + sOpsetId + "," + sOpsetContainerDN, adds);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Could not add operations to the opset: "
						+ e.getMessage());
			}
		}
		res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sOpsetName + GlobalConstants.PM_FIELD_DELIM
					+ sOpsetId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return res;
	}

	// Create a representative object for the specified entity. Assign the
	// associated object attribute to the base node, which can be a policy class
	// or an object attribute.
	// It returns the id of the associated object attribute or null.

	public String createOattrRepresentingEntity(String sBaseId,
			String sBaseType, String sEntId, String sEntType, boolean bInh) {
		// Find a name for the representative object.
		String sEntName = getEntityName(sEntId, sEntType);
		if (sEntName == null) {
			System.out.println("CreateOattrReprresentingEntity: no such entity "
					+ sEntId);
			return null;
		}
		String sPrefix = sEntName + " rep";
		String sObjName = sPrefix;
		int i = 1;
		while (true) {
			String sId = getEntityId(sObjName,PM_NODE.OATTR.value);
			if (sId == null) {
				break;
			}
			sObjName = sPrefix + " " + i;
			i++;
		}
		String sObjClass = typeToClass(sEntType);
		Packet res =  addObjectInternal(sObjName, null, null,
				"Representative", "No info.", sBaseId, sBaseType, sObjClass,
				null, null, null, sEntName, sEntId, bInh, null, null, null,
				null, null, null, null);
		if (res.hasError()) {
			System.out.println("CreateOattrReprresentingEntity: could not create rep object. "
					+ res.getErrorMessage());
			return null;
		}
		String sLine = res.getStringValue(0);
		String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
		String sAssocId = getAssocOattr(pieces[1]);
		return sAssocId;
	}

	// Set the opset's operations as specified by the list sOps, where the ops
	// are separated by commas.

	public void setOpsetOps(String sOpsetId, String sOps) {
		try {
			// Get the opset's operations and delete them.
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOpsetId + ","
					+ sOpsetContainerDN);
			Attribute attr = attrs.get("pmOp");
			if (attr != null) {
				int n = attr.size();
				ModificationItem[] mods = new ModificationItem[n];
				int i = 0;
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					mods[i++] = new ModificationItem(
							DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(
									"pmOp", enumer.next()));
				}
				ServerConfig.ctx.modifyAttributes(
						"CN=" + sOpsetId + "," + sOpsetContainerDN, mods);
			}

			// Add the new operations.
			if (sOps != null) {
				String[] pieces = sOps.split(",");
				int n = pieces.length;
				ModificationItem[] adds = new ModificationItem[n];
				for (int i = 0; i < n; i++) {
					adds[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
							new BasicAttribute("pmOp", pieces[i]));
				}
				ServerConfig.ctx.modifyAttributes(
						"CN=" + sOpsetId + "," + sOpsetContainerDN, adds);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// Get a string with the type and names of the containers of an object
	// attribute identified by its id. The containers include other object
	// attributes, policy classes, and the connector node.
	// An example of the result: "b|Med Records,p|DAC,c|PM".

	public String getOattrContainers(String sOattrId) {
		StringBuffer sb = new StringBuffer();
		try {
			boolean first = true;
			String sId;
			String sName;

			// Get the policy classes.
			Attribute attr = getToPolicies(sOattrId,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sId = (String) enumer.next();
					sName = getEntityName(sId,PM_NODE.POL.value);
					if (first) {
						sb.append(PM_NODE.POL.value + "|" + sName);
						first = false;
					} else {
						sb.append("," +PM_NODE.POL.value + "|" + sName);
					}
				}
			}

			// Get other object attributes.
			attr = getToAttrs(sOattrId,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sId = (String) enumer.next();
					sName = getEntityName(sId,PM_NODE.OATTR.value);
					if (first) {
						sb.append(PM_NODE.OATTR.value + "|" + sName);
						first = false;
					} else {
						sb.append("," +PM_NODE.OATTR.value + "|" + sName);
					}
				}
			}

			// Get the connector.
			attr = getToConnector(sOattrId,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sId = (String) enumer.next();
					sName = getEntityName(sId,PM_NODE.CONN.value);
					if (first) {
						sb.append(PM_NODE.CONN.value + "|" + sName);
						first = false;
					} else {
						sb.append("," +PM_NODE.CONN.value + "|" + sName);
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
		return sb.toString();
	}

	// Copy assignments (to policies, to other attributes, or from opsets)
	// from object attribute 1 to object attribute 2.

	public Packet copyOattrAssignments(String sOattrId1, String sOattrId2) {
		Packet res;
		try {
			// Copy links to policy classes.
			Attribute attr = getToPolicies(sOattrId1,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();

					System.out.println("Copy assignment "
							+ getEntityName(sOattrId2,PM_NODE.OATTR.value) + " --> "
							+ getEntityName(sId,PM_NODE.POL.value));
					res =  addDoubleLink(sOattrId2,PM_NODE.OATTR.value, sId,
							PM_NODE.POL.value);
					if (res.hasError()) {
						return res;
					}
				}
			}

			// Copy links to other object attributes.
			attr = getToAttrs(sOattrId1,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					System.out.println("Copy assignment "
							+ getEntityName(sOattrId2,PM_NODE.OATTR.value) + " --> "
							+ getEntityName(sId,PM_NODE.OATTR.value));
					res =  addDoubleLink(sOattrId2,PM_NODE.OATTR.value, sId,
							PM_NODE.OATTR.value);
					if (res.hasError()) {
						return res;
					}
				}
			}

			// Copy links from opsets.
			attr = getFromOpsets(sOattrId1,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					System.out.println("Copy assignment "
							+ getEntityName(sId,PM_NODE.OPSET.value) + " --> "
							+ getEntityName(sOattrId2,PM_NODE.OATTR.value));
					res =  addDoubleLink(sId,PM_NODE.OPSET.value, sOattrId2,
							PM_NODE.OATTR.value);
					if (res.hasError()) {
						return res;
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in copyAssignments: "
					+ e.getMessage());
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Cut all links to/from this attribute. To be called as a command.

	public Packet isolateOattr(String sName, String sType) {
		String sAttrId = getEntityId(sName, sType);
		if (sAttrId == null) {
			return failurePacket("No attribute " + sName + " of type " + sType);
		}
		return isolateOattr(sAttrId);
	}


	public Packet isolateOattr(String sAttrId) {
		Packet res;
		try {
			// Cut link to connector node, if it exists.
			Attribute attr = getToConnector(sAttrId,PM_NODE.OATTR.value);
			if (attr != null) {
				res =  deleteDoubleLink(sAttrId,PM_NODE.OATTR.value,
						GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			}
		} catch (Exception e) {
		}

		try {
			// Cut links to policy classes.
			Attribute attr = getToPolicies(sAttrId,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					res =  deleteDoubleLink(sAttrId,PM_NODE.OATTR.value,
							sId,PM_NODE.POL.value);
					if (res.hasError()) {
						return res;
					}
				}
			}

			// Cut links to other object attributes.
			attr = getToAttrs(sAttrId,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					res =  deleteDoubleLink(sAttrId,PM_NODE.OATTR.value,
							sId,PM_NODE.OATTR.value);
					if (res.hasError()) {
						return res;
					}
				}
			}

			// Cut links from opsets.
			attr = getFromOpsets(sAttrId,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					res =  deleteDoubleLink(sId,PM_NODE.OPSET.value,
							sAttrId,PM_NODE.OATTR.value);
					if (res.hasError()) {
						return res;
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in isolateOattr: " + e.getMessage());
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Event Obj id Obj class ctx1 ctx2
	// ---------------------------------------------------------------------------
	// Object create obj id File Containers Permissions
	// Object read ? File - -
	// Object write - File Containers -
	// Object delete obj id - Containers assoc id
	// User create user id User - -
	// Object send obj id File Recipient -
	// Session delete sess id Session - -
	//
	// / For "Object delete", ctx1 contains the containers the deleted object is
	// directly assigned to. ctx2 contains the id of the associated object
	// attribute.
	// For Object write/read, first get the obj id, ctx1 and ctx2.

	public Packet processEvent(String sSessId, String sProcId,
			String sEventName, String sObjName, String sObjId,
			String sObjClass, String sObjType, String ctx1, String ctx2)throws Exception {

		System.out.println("processEvent with initial arguments");
		System.out.println("    sessId          = " + sSessId);
		System.out.println("    procId          = " + sProcId);
		System.out.println("    eventName       = " + sEventName);
		System.out.println("    objName         = " + sObjName);
		System.out.println("    objId           = " + sObjId);
		System.out.println("    objClass        = " + sObjClass);
		System.out.println("    objType         = " + sObjType);
		System.out.println("    ctx1            = " + ctx1);
		System.out.println("    ctx2            = " + ctx2);

		//NDK Added stuff here

		String sHost = getSessionHostName(sSessId);
		String sUserId = getSessionUserId(sSessId);
		String sUser = getEntityName(sUserId,PM_NODE.USER.value);
		String sAction = sEventName;
		//Addition Finished - NDK

		if (sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_OBJECT_WRITE)
				|| sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_OBJECT_READ)) {
			sObjId = getEntityId(sObjName, GlobalConstants.PM_OBJ);
		} else if (sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			sObjId = getEntityId(sObjName,PM_NODE.USER.value);
		}

		try {
			if (sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_OBJECT_WRITE)
					|| sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_OBJECT_READ)) {
				String sAssocId = getAssocOattr(sObjId);
				// Get the first level containers where this object is in this
				// moment.
				Attribute attr = getToAttrs(sAssocId,PM_NODE.OATTR.value);
				StringBuffer sb = new StringBuffer();
				boolean firstTime = true;
				if (attr != null) {
					for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
						String sId = (String) enumer.next();
						if (firstTime) {
							firstTime = false;
						} else {
							sb.append(GlobalConstants.PM_LIST_MEMBER_SEP);
						}
						sb.append(PM_NODE.OATTR.value + GlobalConstants.PM_ALT_FIELD_DELIM
								+ getEntityName(sId,PM_NODE.OATTR.value));
					}
				}
				if (sb.length() > 0) {
					ctx1 = sb.toString();
				}
				ctx2 = sAssocId;
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			//NDK - Added
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction, false, "This is the description", sObjName, sObjId);
			}
			//end = NDK    
			return failurePacket("Exception processing event arguments");
		}

		System.out.println("processEvent with processed arguments");
		System.out.println("    sessId          = " + sSessId);
		System.out.println("    procId          = " + sProcId);
		System.out.println("    eventName       = " + sEventName);
		System.out.println("    objName         = " + sObjName);
		System.out.println("    objId           = " + sObjId);
		System.out.println("    objClass        = " + sObjClass);
		System.out.println("    objType         = " + sObjType);
		System.out.println("    ctx1            = " + ctx1);
		System.out.println("    ctx2            = " + ctx2);

		// Get the enabled script. Set the global var sEnabledScriptId
		// because applyActionDeleteRule will us it.
		// The call chain is:
		// processEvent->applyRule->applyAction->applyActionDeleteRule.
		sEnabledScriptId = getEnabledScriptId();
		if (sEnabledScriptId == null) {
			//NDK Added stuff here   	
			String sName = sUser + "@" + sHost + "-" + nextSessionNumber++;   
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction, true, "This is the description", sObjName, sObjId);
			}
			//end of addition- NDK		
			return ADPacketHandler.getSuccessPacket();
		}

		Packet res = null;
		try {
			Attributes scriptAttrs = ServerConfig.ctx.getAttributes("CN=" + sEnabledScriptId
					+ "," + sRuleContainerDN);
			Attribute attr = scriptAttrs.get("pmFirst");

			EventContext eventctx = new EventContext(sSessId, sProcId,
					sEventName, sObjName, sObjClass, sObjType, sObjId, ctx1,
					ctx2);
			while (attr != null) {
				String sRuleId = (String) attr.get();
				res =  matchEvent(eventctx, sRuleId);
				if (res.hasError()) {
					System.out.println(res.getErrorMessage());
				} else {
					System.out.println("...Applying rule " + sRuleId + "!");
					res =  applyRule(eventctx, sRuleId);
					if (res.hasError()) {
						System.out.println("Error in rule " + sRuleId + ": "
								+ res.getErrorMessage());
					}
				}
				Attributes ruleAttrs = ServerConfig.ctx.getAttributes("CN=" + sRuleId + ","
						+ sRuleContainerDN);
				attr = ruleAttrs.get("pmNext");
			}
		} catch (NameNotFoundException e) {
			// If the rule was deleted by an action???
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception looping through rules during event match: "
					+ e.getMessage());
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet applyRule(EventContext eventctx, String sRuleId) {
		try {
			Attributes ruleAttrs = ServerConfig.ctx.getAttributes("CN=" + sRuleId + ","
					+ sRuleContainerDN);
			Attribute attr = ruleAttrs.get("pmFirst");
			while (attr != null) {
				String sActionId = (String) attr.get();
				System.out.println("......Applying action " + sActionId + "!");
				Packet res =  applyAction(eventctx, sActionId);

				// Even when applying the action results in a failure, we may
				// want
				// to continue.
				if (res.hasError()) {
					System.out.println("ERROR: " + res.getErrorMessage());
				}
				// if (res.hasError()) return result;

				// If this action deleted the rule, extracting the action
				// attributes
				// will result in an exception we can ignore.
				Attributes actAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
						+ sRuleContainerDN);
				attr = actAttrs.get("pmNext");
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			// return failurePacket("Exception while getting rule actions: " +
			// e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while getting rule actions: "
					+ e.getMessage());
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet applyAction(EventContext eventctx, String sActionId) {
		try {
			Attributes actAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
					+ sRuleContainerDN);
			Attribute attr = actAttrs.get("pmCondition");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sCondId = (String) enumer.next();
					Packet res =  checkCondition(eventctx, sCondId);
					if (res.hasError()) {
						return res;
					}
				}
			}

			String sAct = (String) actAttrs.get("pmType").get();
			if (sAct.equals("assign")) {
				return applyActionAssign(eventctx, sActionId);
			} else if (sAct.equals("assign like")) {
				return applyActionAssignLike(eventctx, sActionId);
			} else if (sAct.equals("grant")) {
				return applyActionGrant(eventctx, sActionId);
			} else if (sAct.equals("create")) {
				return applyActionCreate(eventctx, sActionId);
			} else if (sAct.equals("deny")) {
				return applyActionDeny(eventctx, sActionId);
			} else if (sAct.equals("delete assignment")) {
				return applyActionDeassign(eventctx, sActionId);
			} else if (sAct.equals("delete deny")) {
				return applyActionDeleteDeny(eventctx, sActionId);
			} else if (sAct.equals("delete rule")) {
				return applyActionDeleteRule(eventctx, sActionId);
			} else {
				return failurePacket("Unknown action type " + sAct);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while dispatching event action");
		}
	}

	// When checking conditions, retain all operands, even those that have
	// null name and/or id - this means that the operand looked for does not
	// exist.

	public Packet checkCondition(EventContext eventctx, String sCondId) {
		try {
			Attributes condAttrs = ServerConfig.ctx.getAttributes("CN=" + sCondId + ","
					+ sRuleContainerDN);
			Attribute attr = condAttrs.get("pmOpnd1");
			if (attr == null) {
				return failurePacket("No operand in condition " + sCondId);
			}
			HashSet<ActOpnd> hsOpnd1 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOpndId = (String) enumer.next();
				// Get the runtime action operand and insert it into the HashSet
				// of
				// first operands. Most often, the run-time operand is the same
				// as the
				// compile-time operand. A function operand at run-time is
				// different.
				ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd1.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd1, "Set of first operands in condition");
			if (hsOpnd1.isEmpty()) {
				return failurePacket("No first operands in condition!?");
			}

			// To date, the only check implemented is whether the operand exists
			// (or
			// does not exist if the condition is negated).
			Iterator<ActOpnd> iter1 = hsOpnd1.iterator();
			attr = condAttrs.get("pmIsNegated");
			boolean isNegated = ((String) attr.get()).equalsIgnoreCase("TRUE");
			while (iter1.hasNext()) {
				if (!isNegated && !condOpndExists(iter1.next())) {
					return failurePacket("Operand does not exist");
				} else if (isNegated && condOpndExists(iter1.next())) {
					return failurePacket("Operand exists");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while dispatching event action");
		}
		return ADPacketHandler.getSuccessPacket();
	}



	public boolean condOpndExists(ActOpnd opnd) {
		String sName = opnd.getName();
		String sId = opnd.getId();
		String sType = opnd.getType();
		System.out.println("(((((((((((((Operand " + sName + ", " + sId + ", "
				+ sType);
		if (sName == null) {
			return false;
		}
		if (sType == null) {
			return false;
		}
		if (getEntityId(sName, sType) == null) {
			return false;
		}
		return true;
	}


	public Packet applyActionDeleteRule(EventContext eventctx, String sActionId) {
		try {
			Attributes actAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
					+ sRuleContainerDN);
			Attribute attr = actAttrs.get("pmOpnd1");

			String sLastError = null;
			ActOpnd[] actOpnds = null;
			String sOpndId = null;

			attr = actAttrs.get("pmOpnd1");
			if (attr == null) {
				return failurePacket("No labels in \"Delete rule(s)\" action "
						+ sActionId);
			}
			HashSet<ActOpnd> hsOpnd1 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				sOpndId = (String) enumer.next();
				actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 1: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd1.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd1, "Set of first operands in \"Delete rule(s)\"");
			if (hsOpnd1.isEmpty()) {
				return failurePacket("No first operands in \"Delete rule(s)\". Last error was: "
						+ sLastError);
			}

			// Delete the rules with the labels specified in hsOpnd1.
			Iterator<ActOpnd> iter1 = hsOpnd1.iterator();

			while (iter1.hasNext()) {
				ActOpnd opnd1 = iter1.next();
				Packet res =  deleteScriptRule(sEnabledScriptId,
						opnd1.getName());
				if (res.hasError()) {
					sLastError = res.getErrorMessage();
				}
			}
			if (sLastError != null) {
				return failurePacket(sLastError);
			}

			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while dispatching event action");
		}
	}


	public Packet applyActionDeassign(EventContext eventctx, String sActionId) {
		try {
			Attributes actAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
					+ sRuleContainerDN);
			Attribute attr = actAttrs.get("pmOpnd1");
			if (attr == null) {
				return failurePacket("No operand 1 in \"Delete assignment\" action");
			}
			HashSet<ActOpnd> hsOpnd1 = new HashSet<ActOpnd>();
			String sLastError = null;
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOpndId = (String) enumer.next();
				// Get the runtime action operand and insert it into the HashSet
				// of
				// first operands. Most often, the run-time operand is the same
				// as the
				// compile-time operand. A function operand at run-time is
				// different.
				ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error in evalOpnd was: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd1.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd1,
					"Set of first operands in \"Delete assignment\"");
			if (hsOpnd1.isEmpty()) {
				System.out.println("No first operands in deassign. Last error was: "
						+ sLastError);
				return failurePacket("No first operands in deassign. Last error was: "
						+ sLastError);
			}

			attr = actAttrs.get("pmOpnd2");
			if (attr == null) {
				return failurePacket("No operand 2 in \"Delete assignment\" action");
			}
			HashSet<ActOpnd> hsOpnd2 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOpndId = (String) enumer.next();
				ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error in evalOpnd was: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd2.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd2,
					"Set of second operands in \"Delete assignment\"");
			if (hsOpnd2.isEmpty()) {
				System.out.println("No second operands in \"Delete assignment\". Last error was: "
						+ sLastError);
				return failurePacket("No second operands in \"Delete assignment\". Last error was: "
						+ sLastError);
			}

			// Deassign each operand1 from each operand2. Even if there is an
			// error,
			// try to perform as much as possible and then report.
			Iterator<ActOpnd> iter1 = hsOpnd1.iterator();
			Iterator<ActOpnd> iter2 = hsOpnd2.iterator();

			while (iter1.hasNext()) {
				ActOpnd opnd1 = iter1.next();
				while (iter2.hasNext()) {
					ActOpnd opnd2 = iter2.next();
					Packet result =  deleteAssignmentInternal(
							opnd1.getId(), opnd1.getType(), opnd2.getId(),
							opnd2.getType());
					if (result.hasError()) {
						sLastError = result.getErrorMessage();
					}
				}
			}
			if (sLastError != null) {
				return failurePacket(sLastError);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while dispatching event action");
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet applyActionDeleteDeny(EventContext eventctx, String sActionId) {
		try {
			Attributes actAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
					+ sRuleContainerDN);
			Attribute attr = actAttrs.get("pmIsIntrasession");
			if (attr == null) {
				return failurePacket("No intrasession attribute in \"Delete deny\" action "
						+ sActionId);
			}
			boolean bIntrasession = ((String) attr.get()).equals("TRUE");
			attr = actAttrs.get("pmIsIntersection");
			if (attr == null) {
				return failurePacket("No intersection attribute in \"Delete deny\" action "
						+ sActionId);
			}
			boolean bIntersection = ((String) attr.get()).equals("TRUE");

			attr = actAttrs.get("pmOpnd1");

			String sLastError = null;
			ActOpnd actOpnd1 = null;
			ActOpnd[] actOpnds = null;
			String sOpndId = null;

			// "Delete deny" could have an empty set of first operands,
			// meaning any user.
			if (attr == null) {
				actOpnd1 = new ActOpnd("*",PM_NODE.USER.value, "*", false, false,
						null);
			} else {
				// There should be only one first operand - a user or a user
				// attribute.
				sOpndId = (String) attr.get();
				// Get the runtime action operand
				actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 1: "
							+ sLastError);
					return failurePacket("No first operands in \"Delete deny\". Last error was: "
							+ sLastError);
				}
				actOpnd1 = actOpnds[0];
			}
			printOpnd(actOpnd1, "First operand in \"Delete deny\"");

			attr = actAttrs.get("pmOpnd2");
			if (attr == null) {
				return failurePacket("No operand 2 in \"Delete deny\" action "
						+ sActionId);
			}
			HashSet<ActOpnd> hsOpnd2 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				sOpndId = (String) enumer.next();
				actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 2: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd2.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd2, "Set of second operands in \"Delete deny\"");
			if (hsOpnd2.isEmpty()) {
				return failurePacket("No second operands in \"Deny\". Last error was: "
						+ sLastError);
			}

			attr = actAttrs.get("pmOpnd3");
			if (attr == null) {
				return failurePacket("No operand 3 in \"Delete deny\" action "
						+ sActionId);
			}
			HashSet<ActOpnd> hsOpnd3 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				sOpndId = (String) enumer.next();
				actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 3: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd3.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd3, "Set of third operands in \"Delete deny\"");
			if (hsOpnd3.isEmpty()) {
				return failurePacket("No third operands in \"Delete deny\". Last error was: "
						+ sLastError);
			}

			// Generate a name for the deny constraint.
			Random random = new Random();
			byte[] bytes = new byte[4];
			random.nextBytes(bytes);
			String sDenyName = UtilMethods.byteArray2HexString(bytes);
			System.out.println("============deny name is " + sDenyName);
			String sType = actOpnd1.getType();
			String sDenyType = null;
			if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
				sDenyType = GlobalConstants.PM_DENY_USER_ID;
			} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				sDenyType = GlobalConstants.PM_DENY_ACROSS_SESSIONS;
				if (bIntrasession) {
					sDenyType = GlobalConstants.PM_DENY_INTRA_SESSION;
				}
			} else {
				return failurePacket("Incorrect type for first \"Delete deny\" operand");
			}

			String sExistingDeny = getSimilarDeny(sDenyType,
					actOpnd1.getName(), actOpnd1.getId(), bIntersection,
					hsOpnd2, hsOpnd3);
			if (sExistingDeny == null) {
				return failurePacket("No such deny exists");
			}
			return deleteDenyInternal(sExistingDeny, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while dispatching event action");
		}
	}


	public Packet applyActionAssign(EventContext eventctx, String sActionId) {
		try {
			Attributes actAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
					+ sRuleContainerDN);
			Attribute attr = actAttrs.get("pmOpnd1");
			if (attr == null) {
				return failurePacket("No operand 1 in \"Assign\" action");
			}
			HashSet<ActOpnd> hsOpnd1 = new HashSet<ActOpnd>();
			String sLastError = null;
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOpndId = (String) enumer.next();
				// Get the runtime action operand and insert it into the HashSet
				// of
				// first operands. Most often, the run-time operand is the same
				// as the
				// compile-time operand. A function operand at run-time is
				// different.
				ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error in evalOpnd was: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd1.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd1, "Set of first operands in \"Assign\"");
			if (hsOpnd1.isEmpty()) {
				System.out.println("No first operands in assign. Last error was: "
						+ sLastError);
				return failurePacket("No first operands in assign. Last error was: "
						+ sLastError);
			}

			attr = actAttrs.get("pmOpnd2");
			if (attr == null) {
				return failurePacket("No operand 2 in \"Assign\" action");
			}
			HashSet<ActOpnd> hsOpnd2 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOpndId = (String) enumer.next();
				ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error in evalOpnd was: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd2.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd2, "Set of second operands in \"Assign\"");
			if (hsOpnd2.isEmpty()) {
				System.out.println("No second operands in \"Assign\". Last error was: "
						+ sLastError);
				return failurePacket("No second operands in \"Assign\". Last error was: "
						+ sLastError);
			}

			// Assign each operand1 to each operand2. Even if there is an error,
			// try to perform as much as possible and then report.
			Iterator<ActOpnd> iter1 = hsOpnd1.iterator();
			Iterator<ActOpnd> iter2 = hsOpnd2.iterator();

			while (iter1.hasNext()) {
				ActOpnd opnd1 = iter1.next();
				while (iter2.hasNext()) {
					ActOpnd opnd2 = iter2.next();
					Packet res =  assignInternal(opnd1.getId(),
							opnd1.getType(), opnd2.getId(), opnd2.getType());
					if (res.hasError()) {
						sLastError = res.getErrorMessage();
					}
				}
			}
			if (sLastError != null) {
				return failurePacket(sLastError);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while dispatching event action");
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public void printOpndSet(HashSet<ActOpnd> opndSet, String caption) {
		Iterator<ActOpnd> iter = opndSet.iterator();

		System.out.println(caption);
		while (iter.hasNext()) {
			System.out.print("  Operand (");
			ActOpnd actOpnd = iter.next();
			System.out.print("name=" + actOpnd.getName());
			System.out.print(", type=" + actOpnd.getType());
			System.out.print(", id=" + actOpnd.getId());
			System.out.print(", err=" + actOpnd.getError());
			System.out.println(")");
		}
	}


	public void printOpnd(ActOpnd actOpnd, String caption) {
		System.out.println(caption);
		System.out.print("  Operand (");
		System.out.print("name=" + actOpnd.getName());
		System.out.print(", type=" + actOpnd.getType());
		System.out.print(", id=" + actOpnd.getId());
		System.out.print(", err=" + actOpnd.getError());
		System.out.println(")");
	}

	// Evaluate an action operand (an object of AD class pmClassOperand),
	// pointed to by sOpndId. It has the following attributes:
	// pmId, pmType, pmIsFunction, pmIsSubgraph, pmIsComplement,
	// pmOriginalName, pmOriginalId, pmArgs.
	// If the operand is function, pmArgs is a list of pointers to its arguments
	// (which are operands); pmOriginalName and pmOriginalId are the function's
	// name and id.
	// If it's not a function, the operand could be a PM entity, and then
	// the pmOriginalName and pmOriginalId are the name and id of that entity.
	// If it's not a PM entity, the operand could be a word, and then the
	// pmOriginalName is that word, and pmOriginalId should be ignored.
	// For example, the operand could be the name of an operation,
	// like "File write", or a property like "homeof=gigi".
	// The return value is an object of Java class ActOpnd, which contains
	// the name, type, and id of a PM entity, whether it represents a subgraph,
	// or if it's to be interpreted as the complement of a container.
	// It also contains an error message, which, if null, indicates successful
	// evaluation.
	// NOTE THAT THE RESULT OF AN EVALUATION IS ALWAYS AN ARRAY OF ActOpnd
	// objects.
	// From an operand which is argument of a function, we retain only the
	// first array component.

	public ActOpnd[] evalOpnd(EventContext eventctx, String sOpndId) {
		System.out.println("EvalOpnd " + sOpndId);
		ActOpnd[] res = new ActOpnd[1];
		try {
			Attributes opndAttrs = ServerConfig.ctx.getAttributes("CN=" + sOpndId + ","
					+ sRuleContainerDN);
			String sIsFun = (String) opndAttrs.get("pmIsFunction").get();
			boolean isFun = sIsFun.equals("TRUE");
			String sType = (String) opndAttrs.get("pmType").get();
			Attribute attr = opndAttrs.get("pmOriginalId");
			if (attr == null && !sType.equalsIgnoreCase(GlobalConstants.PM_OP)
					&& !sType.equalsIgnoreCase(GlobalConstants.PM_LABEL)) {
				res[0] = new ActOpnd(null, null, null, false, false,
						"Missing id of operand " + sOpndId);
				return res;
			}
			String sOrigId = null;
			if (attr != null) {
				sOrigId = (String) attr.get();
			}

			attr = opndAttrs.get("pmOriginalName");
			if (attr == null) {
				res[0] = new ActOpnd(null, null, null, false, false,
						"Missing name of operand " + sOpndId);
				return res;
			}
			String sOrigName = (String) attr.get();

			attr = opndAttrs.get("pmIsSubgraph");
			if (attr == null) {
				res[0] = new ActOpnd(null, null, null, false, false,
						"Missing subgraph specification in operand " + sOpndId);
				return res;
			}
			boolean isSubgraph = ((String) attr.get()).equals("TRUE");

			attr = opndAttrs.get("pmIsComplement");
			if (attr == null) {
				res[0] = new ActOpnd(null, null, null, false, false,
						"Missing complement specification in operand "
								+ sOpndId);
				return res;
			}
			boolean isComplement = ((String) attr.get()).equals("TRUE");

			// If the operand is not a function, return a record containing its
			// name,
			// type, id, isSubgraph, and a null error string.
			if (!isFun) {
				res[0] = new ActOpnd(sOrigName, sType, sOrigId, isSubgraph,
						isComplement, null);
				return res;
			}

			// If the operand is a function, first evaluate its arguments, put
			// them
			// into a vector to preserve the order, then evaluate the function.
			// Be careful to put in the vector only the first component of the
			// array resulted from the evaluation of an argument.
			Vector<ActOpnd> funArgs = new Vector<ActOpnd>();
			attr = opndAttrs.get("pmArgs");

			// Function without parameters.
			if (attr == null) {
				res = evalFun(eventctx, sOrigName, sOrigId, sType, funArgs);
				for (int i = 0; i < res.length; i++) {
					res[i].setSubgraph(isSubgraph);
					res[i].setComplement(isComplement);
				}
				return res;
			}

			// Function with parameters. Its parameters' ids are
			// separated by "|".
			String sArgIds = (String) attr.get();
			if (sArgIds == null || sArgIds.length() == 0) {
				res[0] = new ActOpnd(null, null, null, false, false,
						"Null or empty function arguments in operand "
								+ sOpndId);
				return res;
			}
			String[] pieces = sArgIds.split(GlobalConstants.PM_ALT_DELIM_PATTERN);

			// Prepare the vector containing the arguments evaluated.
			// Be careful to insert into the vector only the first component of
			// the
			// array resulted from the evaluation of an argument.
			for (int i = 0; i < pieces.length; i++) {
				String sArgId = pieces[i];
				res = evalOpnd(eventctx, sArgId);
				if (res[0].getError() != null) {
					return res;
				}
				funArgs.addElement(res[0]);// !!!!!!!!!!!!!!!!!!!!!!!!??????????
			}

			res = evalFun(eventctx, sOrigName, sOrigId, sType, funArgs);
			for (int i = 0; i < res.length; i++) {
				res[i].setSubgraph(isSubgraph);
				res[i].setComplement(isComplement);
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			res[0] = new ActOpnd(null, null, null, false, false,
					"Exception during evaluation of operand " + sOpndId);
			return res;
		}
	}

	// Evaluate a function. funArgs is a vector containing the runtime function
	// arguments.

	public ActOpnd[] evalFun(EventContext eventctx, String sFunName,
			String sFunId, String sFunType, Vector<ActOpnd> funArgs) {
		System.out.println("Evaluating function " + sFunName);
		for (int i = 0; i < funArgs.size(); i++) {
			System.out.println("Argument id = " + funArgs.get(i).getId());
		}

		// Dispatch the function evaluation to the correct method.
		if (sFunName.equalsIgnoreCase("object_new")) {
			return evalFun_object_new(eventctx, sFunType, funArgs);// 1116
		} else if (sFunName.equalsIgnoreCase("oattr_of_user_choice")) {
			return evalFun_oattr_of_user_choice(eventctx, sFunType, funArgs);// 1119
		} else if (sFunName.equalsIgnoreCase("oattr_of_default_user")) {
			return evalFun_oattr_of_default_user(eventctx, sFunType, funArgs);// 1120
		} else if (sFunName.equalsIgnoreCase("oattr_home_of_default_user")) {
			return evalFun_oattr_home_of_default_user(eventctx, sFunType,
					funArgs);// 1121
		} else if (sFunName.equalsIgnoreCase("user_default")) {
			return evalFun_user_default(eventctx, sFunType, funArgs);// 1122
		} else if (sFunName.equalsIgnoreCase("prop_home_of_new_user")) {
			return evalFun_prop_home_of_new_user(eventctx, sFunType, funArgs);// 1123
		} else if (sFunName.equalsIgnoreCase("uattr_name_of_new_user")) {
			return evalFun_uattr_name_of_new_user(eventctx, sFunType, funArgs);// 1124
		} else if (sFunName.equalsIgnoreCase("prop_name_of_new_user")) {
			return evalFun_prop_name_of_new_user(eventctx, sFunType, funArgs);// 1125
		} else if (sFunName.equalsIgnoreCase("user_new")) {
			return evalFun_user_new(eventctx, sFunType, funArgs);// 1126
		} else if (sFunName.equalsIgnoreCase("uattr_name_of_user")) {
			return evalFun_uattr_name_of_user(eventctx, sFunType, funArgs);// 1127
		} else if (sFunName.equalsIgnoreCase("prop_name_of_user")) {
			return evalFun_prop_name_of_user(eventctx, sFunType, funArgs);// 1128
		} else if (sFunName.equalsIgnoreCase("pol_discr")) {
			return evalFun_pol_discr(eventctx, sFunType, funArgs);// 1129
		} else if (sFunName.equalsIgnoreCase("pol_id")) {
			return evalFun_pol_id(eventctx, sFunType, funArgs);// 1130
		} else if (sFunName.equalsIgnoreCase("pol_with_prop")) {
			return evalFun_pol_with_prop(eventctx, sFunType, funArgs);// 1131
		} else if (sFunName.equalsIgnoreCase("oattr_home_of_new_user")) {
			return evalFun_oattr_home_of_new_user(eventctx, sFunType, funArgs);// 1132
		} else if (sFunName.equalsIgnoreCase("oattr_home_of_user")) {
			return evalFun_oattr_home_of_user(eventctx, sFunType, funArgs);// 1133
		} else if (sFunName.equalsIgnoreCase("obj_rep_of_home_of_new_user")) {
			return evalFun_obj_rep_of_home_of_new_user(eventctx, sFunType,
					funArgs);// 1134
		} else if (sFunName.equalsIgnoreCase("obj_rep_of_home_of_user")) {
			return evalFun_obj_rep_of_home_of_user(eventctx, sFunType, funArgs);// 1135
		} else if (sFunName.equalsIgnoreCase("oattr_rep_of_home_of_new_user")) {
			return evalFun_oattr_rep_of_home_of_new_user(eventctx, sFunType,
					funArgs);// 1136
		} else if (sFunName.equalsIgnoreCase("oattr_rep_of_home_of_user")) {
			return evalFun_oattr_rep_of_home_of_user(eventctx, sFunType,
					funArgs);// 1137
		} else if (sFunName.equalsIgnoreCase("oattr_rep_of_discr_users")) {
			return evalFun_oattr_rep_of_discr_users(eventctx, sFunType, funArgs);// 1138
		} else if (sFunName.equalsIgnoreCase("uattr_discr_users")) {
			return evalFun_uattr_discr_users(eventctx, sFunType, funArgs);// 1139
		} else if (sFunName.equalsIgnoreCase("oattr_of_default_obj")) {
			return evalFun_oattr_of_default_obj(eventctx, sFunType, funArgs);// 1140
		} else if (sFunName.equalsIgnoreCase("uattr_lowest_level")) {
			return evalFun_uattr_lowest_level(eventctx, sFunType, funArgs);// 1141
		} else if (sFunName.equalsIgnoreCase("oattr_direct_asc_of_and_containing")) {
			return evalFun_oattr_direct_asc_of_and_containing(eventctx,
					sFunType, funArgs);// 1142
		} else if (sFunName.equalsIgnoreCase("uattr_direct_ascs_of_uattr")) {
			return evalFun_uattr_direct_ascs_of_uattr(eventctx, sFunType,
					funArgs);// 1143
		} else if (sFunName.equalsIgnoreCase("uattr_direct_ascs_of_uattr_except")) {
			return evalFun_uattr_direct_ascs_of_uattr_except(eventctx,
					sFunType, funArgs);// 1144
		} else if (sFunName.equalsIgnoreCase("uattr_active_in_default_session_and_in_uattr")) {
			return evalFun_uattr_active_in_default_session_and_in_uattr(
					eventctx, sFunType, funArgs);// 1145
		} else if (sFunName.equalsIgnoreCase("prop_discr_users")) {
			return evalFun_prop_discr_users(eventctx, sFunType, funArgs);// 1146
		} else if (sFunName.equalsIgnoreCase("obj_rep_of_discr_users")) {
			return evalFun_obj_rep_of_discr_users(eventctx, sFunType, funArgs);// 1147
		} else if (sFunName.equalsIgnoreCase("oattr_witems_of_new_user")) {
			return evalFun_oattr_witems_of_new_user(eventctx, sFunType, funArgs);// 1148
		} else if (sFunName.equalsIgnoreCase("oattr_inbox_of_new_user")) {
			return evalFun_oattr_inbox_of_new_user(eventctx, sFunType, funArgs);// 1148
		} else if (sFunName.equalsIgnoreCase("oattr_winbox_of_new_user")) {
			return evalFun_oattr_winbox_of_new_user(eventctx, sFunType, funArgs);// 1148
		} else if (sFunName.equalsIgnoreCase("oattr_inbox_of_user")) {
			return evalFun_oattr_inbox_of_user(eventctx, sFunType, funArgs);// 1149
		} else if (sFunName.equalsIgnoreCase("oattr_winbox_of_user")) {
			return evalFun_oattr_winbox_of_user(eventctx, sFunType, funArgs);// 1149
		} else if (sFunName.equalsIgnoreCase("oattr_outbox_of_new_user")) {
			return evalFun_oattr_outbox_of_new_user(eventctx, sFunType, funArgs);// 1150
		} else if (sFunName.equalsIgnoreCase("oattr_outbox_of_user")) {
			return evalFun_oattr_outbox_of_user(eventctx, sFunType, funArgs);// 1151
		} else if (sFunName.equalsIgnoreCase("prop_witems_of_new_user")) {
			return evalFun_prop_witems_of_new_user(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("prop_inbox_of_new_user")) {
			return evalFun_prop_inbox_of_new_user(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("prop_inbox_of_user")) {
			return evalFun_prop_inbox_of_user(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("prop_outbox_of_new_user")) {
			return evalFun_prop_outbox_of_new_user(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("prop_outbox_of_user")) {
			return evalFun_prop_outbox_of_user(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("obj_rep_of_inbox_of_new_user")) {
			return evalFun_obj_rep_of_inbox_of_new_user(eventctx, sFunType,
					funArgs);
		} else if (sFunName.equalsIgnoreCase("obj_rep_of_inbox_of_user")) {
			return evalFun_obj_rep_of_inbox_of_user(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_rep_of_inbox_of_new_user")) {
			return evalFun_oattr_rep_of_inbox_of_new_user(eventctx, sFunType,
					funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_rep_of_inbox_of_user")) {
			return evalFun_oattr_rep_of_inbox_of_user(eventctx, sFunType,
					funArgs);
		} else if (sFunName.equalsIgnoreCase("obj_rep_of_outbox_of_new_user")) {
			return evalFun_obj_rep_of_outbox_of_new_user(eventctx, sFunType,
					funArgs);
		} else if (sFunName.equalsIgnoreCase("obj_rep_of_outbox_of_user")) {
			return evalFun_obj_rep_of_outbox_of_user(eventctx, sFunType,
					funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_rep_of_outbox_of_new_user")) {
			return evalFun_oattr_rep_of_outbox_of_new_user(eventctx, sFunType,
					funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_rep_of_outbox_of_user")) {
			return evalFun_oattr_rep_of_outbox_of_user(eventctx, sFunType,
					funArgs);
		} else if (sFunName.equalsIgnoreCase("user_recipient")) {
			return evalFun_user_recipient(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_witems")) {
			return evalFun_oattr_witems(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_inboxes")) {
			return evalFun_oattr_inboxes(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_outboxes")) {
			return evalFun_oattr_outboxes(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("session_default")) {
			return evalFun_session_default(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("rule_composed_of")) {
			return evalFun_rule_composed_of(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("id_or_name_as_string")) {
			return evalFun_id_or_name_as_string(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("name_of_rep_of_oattr")) {
			return evalFun_name_of_rep_of_oattr(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_rep_of_oattr")) {
			return evalFun_oattr_rep_of_oattr(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("obj_rep_of_oattr")) {
			return evalFun_obj_rep_of_oattr(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_record_of_default_obj")) {
			return evalFun_oattr_record_of_default_obj(eventctx, sFunType,
					funArgs);
		} else if (sFunName.equalsIgnoreCase("oattr_record_of_oattr")) {
			return evalFun_oattr_record_of_oattr(eventctx, sFunType, funArgs);
		} else if (sFunName.equalsIgnoreCase("process_default")) {
			return evalFun_process_default(eventctx, sFunType, funArgs);
		} else {
			ActOpnd[] err = new ActOpnd[1];
			err[0] = new ActOpnd(null, null, null, false, false, "Function "
					+ sFunName + " not implemented!");
			return err;
		}
	}

	// Returns the object attribute associated to an object that represents
	// another object attribute.

	public ActOpnd[] evalFun_oattr_rep_of_oattr(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_rep_of_oattr");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_rep_of_oattr is not an object attribute");
			return res;
		}
		String sOattrName = arg.getName();
		String sRepName = sOattrName + " rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		if (sRepId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_rep_of_oattr has no representative");
			return res;
		}
		res[0] = new ActOpnd(sRepName,PM_NODE.OATTR.value, sRepId, false, false,
				null);
		return res;
	}

	// Returns the object attribute associated to an object that represents
	// another object attribute.

	public ActOpnd[] evalFun_obj_rep_of_oattr(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_rep_of_oattr");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_rep_of_oattr is not an object attribute");
			return res;
		}
		String sOattrName = arg.getName();
		String sRepName = sOattrName + " rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		// No check
		res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false, false, null);
		return res;
	}

	// Returns the name of the object attribute associated to the object
	// that represents a specified object attribute.
	// Parameters: an object attribute.

	public ActOpnd[] evalFun_name_of_rep_of_oattr(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function name_of_rep_of_oattr");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function name_of_rep_of_oattr is not an object attribute");
			return res;
		}
		String sOattrName = arg.getName();
		String sRepName = sOattrName + " rep";
		String sRepId = getEntityId(sRepName, GlobalConstants.PM_OBJ);
		// I didn't check existence because it ccould just being created.
		res[0] = new ActOpnd(sRepName, GlobalConstants.PM_UNKNOWN, sRepId, false, false, null);
		return res;
	}

	// If the argument is of any of the types a, u, p, c, b, ob, ses,
	// then return its id as string (i.e., return it in the name field and
	// with type k.
	// If the argument is of type k or rule, then return its name in the
	// name field and with type k.

	public ActOpnd[] evalFun_id_or_name_as_string(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Wrong argument count in id_or_name_as_string()");
			return res;
		}

		ActOpnd arg0 = funArgs.get(0);
		String sT = arg0.getType();

		RandomGUID myGUID = new RandomGUID();
		String sId = myGUID.toStringNoDashes();

		if (sT.equals(GlobalConstants.PM_UNKNOWN) || sT.equals(GlobalConstants.PM_RULE)) {
			res[0] = new ActOpnd(arg0.getName(), GlobalConstants.PM_UNKNOWN, sId, false, false,
					null);
		} else {
			res[0] = new ActOpnd(arg0.getId(), GlobalConstants.PM_UNKNOWN, sId, false, false,
					null);
		}
		return res;
	}

	// This function has a variable number of arguments, but at least one.
	// This first argument (argument number 0) is the string representation
	// of an EVER rule, that could contain up to 9 macros #1,...,#n (n <= 9),
	// which will be replaced by the values of its subsequent arguments:
	// #1 by argument number 1, #2 by argument number 2, etc. If naa is the
	// number of actual arguments including the first (argument number 0),
	// then naa must be > n. After substitution, the first argument must be
	// compiled and added to the currently enabled script.

	public ActOpnd[] evalFun_rule_composed_of(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty()) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No arguments in rule_composed_of()");
			return res;
		}

		int nActualArgs = funArgs.size();
		ActOpnd arg0 = funArgs.get(0);
		System.out.println("rule_composed_of's arg 0 type is " + arg0.getType());
		System.out.println("rule_composed_of's arg 0 id is " + arg0.getId());
		System.out.println("rule_composed_of's arg 0 name is " + arg0.getName());

		String sOrigString = arg0.getName();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < sOrigString.length(); ) {
			char c = sOrigString.charAt(i++);
			if (c == '#') {
				if (i >= sOrigString.length()) {
					res[0] = new ActOpnd(null, null, null, false, false,
							"Incorrect argument reference in rule_composed_of()");
					return res;
				}
				c = sOrigString.charAt(i++);
				if (c < '1' || c > '9') {
					res[0] = new ActOpnd(null, null, null, false, false,
							"Argument reference not in range 1..9 in rule_composed_of()");
					return res;
				}
				int iArg = c - '0';
				if (iArg >= nActualArgs) {
					res[0] = new ActOpnd(null, null, null, false, false,
							"Argument reference out of the arguments range in rule_composed_of()");
					return res;
				}

				String sRepl = funArgs.get(iArg).getName();
				sb.append(sRepl);
			} else {
				sb.append(c);
			}
		}

		RandomGUID myGUID = new RandomGUID();
		String sId = myGUID.toStringNoDashes();
		res[0] = new ActOpnd(sb.toString(), GlobalConstants.PM_RULE, sId, false, false, null);
		System.out.println("result type is " + res[0].getType());
		System.out.println("result id is " + res[0].getId());
		System.out.println("result name is " + res[0].getName());
		return res;
	}

	// The object_new() function returns an ActOpnd containing the object
	// attribute associated with the newly created object, if the event
	// who triggered its evaluation was "Object create". Otherwise, it
	// returns an error ActOpnd.
	// Parameters: none. It uses the event context to extract info.

	public ActOpnd[] evalFun_object_new(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		// The event context should contain the virtual object name and id,
		// and the function type is GlobalConstants.PM_OBJ. This function needs to return the
		// object attribute associated to the virtual object.
		if (!eventctx.getEventName().equalsIgnoreCase(GlobalConstants.PM_EVENT_OBJECT_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_OBJECT_CREATE + "\"!");
			return res;
		}
		String sObjId = eventctx.getObjId();
		if (sObjId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Null object id in the context");
			return res;
		}
		String sId = getAssocOattr(sObjId);
		if (sId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute associated to object "
							+ eventctx.getObjId());
			return res;
		}
		res[0] = new ActOpnd(eventctx.getObjName(),PM_NODE.OATTR.value, sId, false,
				false, null);
		return res;
	}

	// Returns the user attribute uaa, which is active in the default session
	// and
	// uaa ->+ uaarg, where uaarg is the first argument (a user attribute).

	public ActOpnd[] evalFun_uattr_active_in_default_session_and_in_uattr(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(
					null,
					null,
					null,
					false,
					false,
					"Incorrect number of arguments for function uattr_active_in_default_session_and_in_uattr");
			return res;
		}

		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
			res[0] = new ActOpnd(
					null,
					null,
					null,
					false,
					false,
					"First argument of uattr_active_in_default_session_and_in_uattr is not a user attribute");
			return res;
		}
		String sUattrId1 = arg.getId();

		HashSet<String> actives = getSessionActiveAttrSet(eventctx.getSessId());
		if (actives.isEmpty()) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user attributes active in the event context");
			return res;
		}
		Iterator<String> iter = actives.iterator();
		while (iter.hasNext()) {
			String sUattrId = iter.next();
			if (attrIsAscendant(sUattrId, sUattrId1,PM_NODE.UATTR.value)) {
				String sUattrName = getEntityName(sUattrId,PM_NODE.UATTR.value);
				res[0] = new ActOpnd(sUattrName,PM_NODE.UATTR.value, sUattrId,
						false, false, null);
				return res;
			}
		}
		res[0] = new ActOpnd(null, null, null, false, false,
				"No active attribute contained in "
						+ getEntityName(sUattrId1,PM_NODE.UATTR.value));
		return res;
	}

	// The user_recipient() function returns an ActOpnd
	// containing the user to be recipient of an email message.
	// It is extracted from ctx1.
	// Parameters: None.

	public ActOpnd[] evalFun_user_recipient(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];
		String sUserName = eventctx.getctx1();
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user of name " + sUserName);
			return res;
		}

		res[0] = new ActOpnd(sUserName,PM_NODE.USER.value, sUserId, false, false,
				null);
		return res;
	}

	// The oattr_of_user_choice() function returns an ActOpnd
	// containing an object attribute selected by the user app, which is
	// contained in a given policy class.
	// Parameters: the policy class.

	public ActOpnd[] evalFun_oattr_of_user_choice(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_of_user_choice");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.POL.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_of_user_choice is not a policy class");
			return res;
		}
		HashSet<String> selConts = UtilMethods.stringToSet(eventctx.getctx1());
		if (selConts.isEmpty()) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user-selected (oattr) containers!");
			return res;
		}
		Iterator<String> iter = selConts.iterator();
		while (iter.hasNext()) {
			String sOattrName = iter.next();
			String sOattrId = getEntityId(sOattrName,PM_NODE.OATTR.value);
			if (sOattrId == null) {
				res[0] = new ActOpnd(null, null, null, false, false,
						"No object attribute of name " + sOattrName);
				return res;
			}
			if (attrIsAscendantToPolicy(sOattrId,PM_NODE.OATTR.value, arg.getId())) {
				res[0] = new ActOpnd(sOattrName,PM_NODE.OATTR.value, sOattrId,
						false, false, null);
				return res;
			}
		}
		res[0] = new ActOpnd(null, null, null, false, false,
				"No user-selected container in policy class " + arg.getName());
		return res;
	}

	// Returns the object attribute with the property:
	// owner=<user>
	// where <user> is the user of the process that triggered the event.
	// Parameters: None.

	public ActOpnd[] evalFun_oattr_of_default_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sSessId = eventctx.getSessId();
		String sUserId = getSessionUserId(sSessId);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user for session " + sSessId);
			return res;
		}
		String sUser = getEntityName(sUserId,PM_NODE.USER.value);
		if (sUser == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user with id " + sUserId);
			return res;
		}

		// Let's find the oattr with the property:
		String sProp = "owner=" + sUser;
		String sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
		System.out.println("=============oattr with property " + sProp
				+ " found " + sOattrId);

		if (sOattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with property " + sProp);
			return res;
		}
		String sOattr = getEntityName(sOattrId,PM_NODE.OATTR.value);
		if (sOattr == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with id " + sOattrId);
			return res;
		}
		res[0] = new ActOpnd(sOattr,PM_NODE.OATTR.value, sOattrId, false, false,
				null);
		return res;
	}

	// Returns all direct ascendants of first argument excepting the second
	// argument. Both arguments are user attributes.

	public ActOpnd[] evalFun_uattr_direct_ascs_of_uattr_except(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] err = new ActOpnd[1];
		if (funArgs.isEmpty() || funArgs.size() != 2) {
			err[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function uattr_direct_ascs_of_uattr_except");
			return err;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
			err[0] = new ActOpnd(
					null,
					null,
					null,
					false,
					false,
					"Argument 1 of function uattr_direct_ascs_of_uattr_except is not a user attribute");
			return err;
		}
		String sUattrId1 = arg.getId();
		arg = funArgs.get(1);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
			err[0] = new ActOpnd(
					null,
					null,
					null,
					false,
					false,
					"Argument 2 of function uattr_direct_ascs_of_uattr_except is not a user attribute");
			return err;
		}
		String sUattrId2 = arg.getId();

		// Get all direct ascendants of the first argument.
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sUattrId1 + ","
					+ sUserAttrContainerDN);
			Attribute attr = attrs.get("pmFromAttr");
			if (attr == null) {
				err[0] = new ActOpnd(null, null, null, false, false,
						"No direct ascendants found");
				return err;
			}
			// Let's see if the second argument is one of these ascendants.
			boolean isOne = false;
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sId = (String) attrEnum.next();
				if (sId.equalsIgnoreCase(sUattrId2)) {
					isOne = true;
					break;
				}
			}
			int n = attr.size();
			if (isOne) {
				n--;
			}
			if (n == 0) {
				err[0] = new ActOpnd(null, null, null, false, false,
						"No ascendants as requested found");
				return err;
			}
			ActOpnd[] result = new ActOpnd[n];
			int i = 0;
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sId = (String) attrEnum.next();
				if (sId.equalsIgnoreCase(sUattrId2)) {
					continue;
				}
				result[i++] = new ActOpnd(getEntityName(sId,PM_NODE.UATTR.value),
						PM_NODE.UATTR.value, sId, false, false, null);
			}
			return result;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			err[0] = new ActOpnd(null, null, null, false, false, "Exception: "
					+ e.getMessage());
			return err;
		}
	}


	public ActOpnd[] evalFun_uattr_direct_ascs_of_uattr(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] err = new ActOpnd[1];
		if (funArgs.isEmpty() || funArgs.size() != 1) {
			err[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function uattr_direct_ascs_of_uattr");
			return err;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
			err[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function uattr_direct_ascs_of_uattr is not a user attribute");
			return err;
		}
		String sUattrId = arg.getId();
		// Get all direct ascendants of the first argument.
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sUattrId + ","
					+ sUserAttrContainerDN);
			Attribute attr = attrs.get("pmFromAttr");
			if (attr == null) {
				err[0] = new ActOpnd(null, null, null, false, false,
						"No direct ascendants found");
				return err;
			}
			int n = attr.size();
			ActOpnd[] result = new ActOpnd[n];
			int i = 0;
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sId = (String) attrEnum.next();
				result[i++] = new ActOpnd(getEntityName(sId,PM_NODE.UATTR.value),
						PM_NODE.UATTR.value, sId, false, false, null);
			}
			return result;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			err[0] = new ActOpnd(null, null, null, false, false, "Exception: "
					+ e.getMessage());
			return err;
		}
	}

	// Returns an object attribute oa with the property:
	// oa -> oattr1 and oattr2 ->* oa, where oattr1 and oattr2 are its two
	// parameters. It assumes that oa is unique.
	// Parameters: two object attributes oattr1 and oattr2 such that
	// there is a unique oa such that oattr2 ->* oa -> oattr1.

	public ActOpnd[] evalFun_oattr_direct_asc_of_and_containing(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 2) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_direct_asc_of_and_containing");
			return res;
		}
		ActOpnd arg1 = funArgs.get(0);
		if (!arg1.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
			res[0] = new ActOpnd(
					null,
					null,
					null,
					false,
					false,
					"First argument of function oattr_direct_asc_of_and_containing is not an object attribute");
			return res;
		}
		String sOattrId1 = arg1.getId();
		System.out.println("First operand is " + arg1.getName());

		ActOpnd arg2 = funArgs.get(1);
		if (!arg2.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
			res[0] = new ActOpnd(
					null,
					null,
					null,
					false,
					false,
					"Second argument of function oattr_direct_asc_containing is not an object attribute");
			return res;
		}
		String sOattrId2 = arg2.getId();
		System.out.println("Second operand is " + arg2.getName());

		// Get all direct ascendants of the first argument.
		System.out.println("Walking thru all ascendants of the first argument");
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOattrId1 + ","
					+ sObjAttrContainerDN);
			Attribute attr = attrs.get("pmFromAttr");
			if (attr != null) {
				for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
					String sId = (String) attrEnum.next();
					System.out.println("For direct ascendant " + sId);
					if (attrIsAscendant(sOattrId2, sId,PM_NODE.OATTR.value)) {
						res[0] = new ActOpnd(getEntityName(sId,PM_NODE.OATTR.value),
								PM_NODE.OATTR.value, sId, false, false, null);
						return res;
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			res[0] = new ActOpnd(null, null, null, false, false, "Exception: "
					+ e.getMessage());
			return res;
		}
		res[0] = new ActOpnd(null, null, null, false, false,
				"No container satisfying the requirement was found");
		return res;
	}

	// Returns the object attribute associated with the event's object,
	// if the event is Object create, Object write, etc., but not User create,
	// etc.
	//
	// Note that when the event is "Object delete", the id of the associated
	// object attribute cannot be obtained using getAssocId(), because the
	// object no longer exists. Instead, the assoc id is passed to
	// processEvent() and all subsequent functions in ctx2. All containers
	// the deleted object is assigned to are passed in ctx1.
	// Parameters: None.

	public ActOpnd[] evalFun_oattr_of_default_obj(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.startsWith("Object ")) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"The event is not \"Object ...\"!");
			return res;
		}
		String sObjName = eventctx.getObjName();
		String sObjId = eventctx.getObjId();

		// If the event is "Object delete", the assoc id is in ctx2.
		if (sEventName.equalsIgnoreCase("Object delete")) {
			res[0] = new ActOpnd(sObjName,PM_NODE.OATTR.value, eventctx.getctx2(),
					false, false, null);
			return res;
		}
		String sActObjId = getEntityId(sObjName, GlobalConstants.PM_OBJ);
		if (sActObjId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No such object \"" + sObjName + "\"!");
			return res;
		}
		if (!sActObjId.equalsIgnoreCase(sObjId)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Context's object id is not the actual id!");
			return res;
		}
		String sAssocId = getAssocOattr(sObjId);
		if (sAssocId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Context's object has no associated object attribute!");
			return res;
		}
		res[0] = new ActOpnd(sObjName,PM_NODE.OATTR.value, sAssocId, false, false,
				null);
		return res;
	}

	// Returns the record (an object container) the default object is a member
	// of.
	// The event must be Object create, Object write, etc., but not User create,
	// etc.
	//
	// Note that when the event is "Object delete", the id of the associated
	// object attribute cannot be obtained using getAssocId(), because the
	// object no longer exists. Instead, the assoc id is passed to
	// processEvent() and all subsequent functions in ctx2. All containers
	// the deleted object is assigned to are passed in ctx1.
	// Parameters: None.

	public ActOpnd[] evalFun_oattr_record_of_default_obj(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.startsWith("Object ")) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"The event is not \"Object ...\"!");
			return res;
		}
		String sObjName = eventctx.getObjName();

		String sAssocId = null;

		// If the event is "Object delete", the assoc id is in ctx2.
		if (sEventName.equalsIgnoreCase("Object delete")) {
			sAssocId = eventctx.getctx2();
		} else {
			sAssocId = getEntityId(sObjName,PM_NODE.OATTR.value);
		}
		if (sAssocId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Could not find an object attribute associated to object \""
							+ sObjName + "\"!");
			return res;
		}
		String sRecId = getRecordOf(sAssocId,PM_NODE.OATTR.value);
		if (sRecId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "Object "
					+ sObjName + " is not a field of a record!");
			return res;
		}

		String sRecName = getEntityName(sRecId,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sRecName,PM_NODE.OATTR.value, sRecId, false, false,
				null);
		return res;
	}


	public ActOpnd[] evalFun_oattr_record_of_oattr(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];
		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_record_of_oattr");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_record_of_oattr is not an object attribute");
			return res;
		}
		String sOattrId = arg.getId();
		if (sOattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_record_of_oattr has null id!");
			return res;
		}

		String sRecId = getRecordOf(sOattrId,PM_NODE.OATTR.value);
		if (sRecId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No record containing the argument " + sOattrId);
			return res;
		}

		String sRecName = getEntityName(sRecId,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sRecName,PM_NODE.OATTR.value, sRecId, false, false,
				null);
		return res;
	}

	// This function returns the property "homeof=<user>", where <user>
	// is the name of the user just created. The event
	// must be "Create user", the context object must be a user, and the
	// event object class must be "User".
	// Parameters: None.

	public ActOpnd[] evalFun_prop_home_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		res[0] = new ActOpnd("homeof=" + sUserName, GlobalConstants.PM_UNKNOWN, null, false,
				false, null);
		return res;
	}


	public ActOpnd[] evalFun_prop_witems_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		res[0] = new ActOpnd("witemsof=" + sUserName, GlobalConstants.PM_UNKNOWN, null, false,
				false, null);
		return res;
	}


	public ActOpnd[] evalFun_prop_inbox_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		res[0] = new ActOpnd("inboxof=" + sUserName, GlobalConstants.PM_UNKNOWN, null, false,
				false, null);
		return res;
	}


	public ActOpnd[] evalFun_prop_inbox_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function prop_inbox_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function prop_inbox_of_user is not a user");
			return res;
		}
		String sProp = "inboxof=" + arg.getName();
		res[0] = new ActOpnd(sProp, GlobalConstants.PM_UNKNOWN, null, false, false, null);
		return res;
	}


	public ActOpnd[] evalFun_prop_outbox_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		res[0] = new ActOpnd("outboxof=" + sUserName, GlobalConstants.PM_UNKNOWN, null, false,
				false, null);
		return res;
	}


	public ActOpnd[] evalFun_prop_outbox_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function prop_outbox_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function prop_outbox_of_user is not a user");
			return res;
		}
		String sProp = "outboxof=" + arg.getName();
		res[0] = new ActOpnd(sProp, GlobalConstants.PM_UNKNOWN, null, false, false, null);
		return res;
	}

	// This function returns the property "nameof=<user>", where <user>
	// is the name of the user just created. The event
	// must be "Create user", the context object must be a user, and the
	// event object class must be "User".
	// Parameters: None.

	public ActOpnd[] evalFun_prop_name_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		res[0] = new ActOpnd("nameof=" + sUserName, GlobalConstants.PM_UNKNOWN, null, false,
				false, null);
		return res;
	}

	// Return the new user when the event is "User create".
	// Parameters: None.

	public ActOpnd[] evalFun_user_new(EventContext eventctx, String sFunType,
			Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		res[0] = new ActOpnd(sUserName,PM_NODE.USER.value, sUserId, false, false,
				null);
		return res;
	}

	// Returns the object attribute with the property homeof=<user>,
	// where <user> is the user of the current session.
	// Parameters: None.

	public ActOpnd[] evalFun_oattr_home_of_default_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sSessId = eventctx.getSessId();
		String sUserId = getSessionUserId(sSessId);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user for session " + sSessId);
			return res;
		}
		String sUser = getEntityName(sUserId,PM_NODE.USER.value);
		if (sUser == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user with id " + sUserId);
			return res;
		}
		// Let's find the oattr with the property:
		String sProp = "homeof=" + sUser;
		String sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
		System.out.println("=============oattr with property " + sProp
				+ " found " + sOattrId);

		if (sOattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with property " + sProp);
			return res;
		}
		String sOattr = getEntityName(sOattrId,PM_NODE.OATTR.value);
		if (sOattr == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with id " + sOattrId);
			return res;
		}
		res[0] = new ActOpnd(sOattr,PM_NODE.OATTR.value, sOattrId, false, false,
				null);
		return res;
	}


	public ActOpnd[] evalFun_oattr_outboxes(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		// Let's find the oattr with the property:
		String sProp = "containerof=outboxes";
		String sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
		if (sOattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with property " + sProp);
			return res;
		}
		String sOattr = getEntityName(sOattrId,PM_NODE.OATTR.value);
		if (sOattr == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with id " + sOattrId);
			return res;
		}
		res[0] = new ActOpnd(sOattr,PM_NODE.OATTR.value, sOattrId, false, false,
				null);
		return res;
	}


	public ActOpnd[] evalFun_oattr_inboxes(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		// Let's find the oattr with the property:
		String sProp = "containerof=inboxes";
		String sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
		if (sOattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with property " + sProp);
			return res;
		}
		String sOattr = getEntityName(sOattrId,PM_NODE.OATTR.value);
		if (sOattr == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with id " + sOattrId);
			return res;
		}
		res[0] = new ActOpnd(sOattr,PM_NODE.OATTR.value, sOattrId, false, false,
				null);
		return res;
	}


	public ActOpnd[] evalFun_oattr_witems(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		// Let's find the oattr with the property:
		String sProp = "containerof=witems";
		String sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
		if (sOattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with property " + sProp);
			return res;
		}
		String sOattr = getEntityName(sOattrId,PM_NODE.OATTR.value);
		if (sOattr == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute with id " + sOattrId);
			return res;
		}
		res[0] = new ActOpnd(sOattr,PM_NODE.OATTR.value, sOattrId, false, false,
				null);
		return res;
	}


	public ActOpnd[] evalFun_oattr_inbox_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_inbox_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_inbox_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sInboxName = sUserName + " INBOX";
		String sInboxId = getEntityId(sInboxName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sInboxName,PM_NODE.OATTR.value, sInboxId, false, false,
				null);
		return res;
	}


	public ActOpnd[] evalFun_oattr_winbox_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_winbox_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_winbox_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sInboxName = sUserName + " wINBOX";
		String sInboxId = getEntityId(sInboxName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sInboxName,PM_NODE.OATTR.value, sInboxId, false, false,
				null);
		return res;
	}


	public ActOpnd[] evalFun_oattr_outbox_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sOutboxName = sUserName + " OUTBOX";
		String sOutboxId = getEntityId(sOutboxName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sOutboxName,PM_NODE.OATTR.value, sOutboxId, false,
				false, null);
		return res;
	}


	public ActOpnd[] evalFun_oattr_outbox_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_outbox_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_outbox_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sOutboxName = sUserName + " OUTBOX";
		String sOutboxId = getEntityId(sOutboxName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sOutboxName,PM_NODE.OATTR.value, sOutboxId, false,
				false, null);
		return res;
	}


	public ActOpnd[] evalFun_oattr_witems_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sInboxName = sUserName + " witems";
		String sInboxId = getEntityId(sInboxName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sInboxName,PM_NODE.OATTR.value, sInboxId, false, false,
				null);
		return res;
	}

	// Returns the object attribute which will be the INBOX container of the
	// new user. Its name is "<user> INBOX",
	// where <user> is the new user. The event name must be "User create",
	// the object name must be the new user name, and the object class
	// must be "User".

	public ActOpnd[] evalFun_oattr_inbox_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sInboxName = sUserName + " INBOX";
		String sInboxId = getEntityId(sInboxName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sInboxName,PM_NODE.OATTR.value, sInboxId, false, false,
				null);
		return res;
	}

	// Returns the object attribute which will be the wINBOX container of the
	// new user. Its name is "<user> wINBOX",
	// where <user> is the new user. The event name must be "User create",
	// the object name must be the new user name, and the object class
	// must be "User".

	public ActOpnd[] evalFun_oattr_winbox_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sInboxName = sUserName + " wINBOX";
		String sInboxId = getEntityId(sInboxName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sInboxName,PM_NODE.OATTR.value, sInboxId, false, false,
				null);
		return res;
	}

	// Returns the object attribute which will be the home container of the
	// new user. Its name is "<user> home",
	// where <user> is the new user. The event name must be "User create",
	// the object name must be the new user name, and the object class
	// must be "User".
	// Parameters: None.

	public ActOpnd[] evalFun_oattr_home_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sHomeName = sUserName + " home";
		String sHomeId = getEntityId(sHomeName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sHomeName,PM_NODE.OATTR.value, sHomeId, false, false,
				null);
		return res;
	}

	// Returns the object attribute which is the home container of the
	// user passed as argument. Its name is "<user> home".
	// Parameters: A user.

	public ActOpnd[] evalFun_oattr_home_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_home_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_home_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sHomeName = sUserName + " home";
		String sHomeId = getEntityId(sHomeName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sHomeName,PM_NODE.OATTR.value, sHomeId, false, false,
				null);
		return res;
	}

	// Returns the object that represents the outbox container of the
	// new user. Its name is "<user> outbox rep",
	// where <user> is the new user. The event name must be "User create",
	// the object name must be the new user name, and the object class
	// must be "User".
	// Parameters: None.

	public ActOpnd[] evalFun_obj_rep_of_outbox_of_new_user(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sRepName = sUserName + " outbox rep";
		String sRepId = getEntityId(sRepName, GlobalConstants.PM_OBJ);
		res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false, false, null);
		return res;
	}

	// Returns the object that represents the inbox container of the
	// new user. Its name is "<user> inbox rep",
	// where <user> is the new user. The event name must be "User create",
	// the object name must be the new user name, and the object class
	// must be "User".
	// Parameters: None.

	public ActOpnd[] evalFun_obj_rep_of_inbox_of_new_user(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sRepName = sUserName + " inbox rep";
		String sRepId = getEntityId(sRepName, GlobalConstants.PM_OBJ);
		res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false, false, null);
		return res;
	}

	// Returns the object that represents the home container of the
	// new user. Its name is "<user> home rep",
	// where <user> is the new user. The event name must be "User create",
	// the object name must be the new user name, and the object class
	// must be "User".
	// Parameters: None.

	public ActOpnd[] evalFun_obj_rep_of_home_of_new_user(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sRepName = sUserName + " home rep";
		String sRepId = getEntityId(sRepName, GlobalConstants.PM_OBJ);
		res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false, false, null);
		return res;
	}

	// Returns the object which represents the user attribute container
	// of the discretionary users, i.e., the user attribute with the
	// property "usersof=dicretionary". The name of this object is
	// <name of user attr container> + "rep".
	// Parameters: None.

	public ActOpnd[] evalFun_obj_rep_of_discr_users(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		// Let's find the uattr with the property:
		String sProp = "usersof=discretionary";
		String sUattrId = getEntityWithPropInternal(PM_NODE.UATTR.value, sProp);
		System.out.println("=============uattr with property " + sProp
				+ " found " + sUattrId);

		if (sUattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user attribute with property " + sProp);
			return res;
		}
		String sUattrName = getEntityName(sUattrId,PM_NODE.UATTR.value);
		if (sUattrName == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user attribute with id " + sUattrId);
			return res;
		}
		String sRepName = sUattrName + " rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		// We don't test sRepId, because it may not exist when we call this
		// function just to get the object created.
		res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false, false, null);
		return res;
	}

	// Returns the object which represents the outbox container of the
	// user passed as argument. Its name is "<user> outbox rep".
	// Parameters: a user.

	public ActOpnd[] evalFun_obj_rep_of_outbox_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function obj_rep_of_outbox_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function obj_rep_of_outbox_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sRepName = sUserName + " outbox rep";
		String sRepId = getEntityId(sRepName, GlobalConstants.PM_OBJ);
		res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false, false, null);
		return res;
	}

	// Returns the object which represents the inbox container of the
	// user passed as argument. Its name is "<user> inbox rep".
	// Parameters: a user.

	public ActOpnd[] evalFun_obj_rep_of_inbox_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function obj_rep_of_inbox_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function obj_rep_of_inbox_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sRepName = sUserName + " inbox rep";
		String sRepId = getEntityId(sRepName, GlobalConstants.PM_OBJ);
		res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false, false, null);
		return res;
	}

	// Returns the object which represents the home container of the
	// user passed as argument. Its name is "<user> home rep".
	// Parameters: a user.

	public ActOpnd[] evalFun_obj_rep_of_home_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function obj_rep_of_home_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function obj_rep_of_home_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sRepName = sUserName + " home rep";
		String sRepId = getEntityId(sRepName, GlobalConstants.PM_OBJ);
		res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false, false, null);
		return res;
	}

	// Like evalFun_obj_rep_of_outbox_of_new_user, but returns an object
	// attribute.
	// Parameters: None.

	public ActOpnd[] evalFun_oattr_rep_of_outbox_of_new_user(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sRepName = sUserName + " outbox rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sRepName,PM_NODE.OATTR.value, sRepId, false, false,
				null);
		return res;
	}

	// Like evalFun_obj_rep_of_inbox_of_new_user, but returns an object
	// attribute.
	// Parameters: None.

	public ActOpnd[] evalFun_oattr_rep_of_inbox_of_new_user(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sRepName = sUserName + " inbox rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sRepName,PM_NODE.OATTR.value, sRepId, false, false,
				null);
		return res;
	}

	// Like evalFun_obj_rep_of_home_of_new_user, but returns an object
	// attribute.
	// Parameters: None.

	public ActOpnd[] evalFun_oattr_rep_of_home_of_new_user(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "No user \""
					+ sUserName + "\"");
			return res;
		}
		String sRepName = sUserName + " home rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sRepName,PM_NODE.OATTR.value, sRepId, false, false,
				null);
		return res;
	}

	// Like evalFun_obj_rep_of_outbox_of_user, but returns an object attribute.
	// Parameters: a user.

	public ActOpnd[] evalFun_oattr_rep_of_outbox_of_user(
			EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_rep_of_outbox_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_rep_of_outbox_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sRepName = sUserName + " outbox rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sRepName,PM_NODE.OATTR.value, sRepId, false, false,
				null);
		return res;
	}

	// Like evalFun_obj_rep_of_inbox_of_user, but returns an object attribute.
	// Parameters: a user.

	public ActOpnd[] evalFun_oattr_rep_of_inbox_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_rep_of_inbox_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_rep_of_inbox_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sRepName = sUserName + " inbox rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sRepName,PM_NODE.OATTR.value, sRepId, false, false,
				null);
		return res;
	}

	// Like evalFun_obj_rep_of_home_of_user, but returns an object attribute.
	// Parameters: a user.

	public ActOpnd[] evalFun_oattr_rep_of_home_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function oattr_rep_of_home_of_user");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function oattr_rep_of_home_of_user is not a user");
			return res;
		}
		String sUserName = arg.getName();
		String sRepName = sUserName + " home rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		res[0] = new ActOpnd(sRepName,PM_NODE.OATTR.value, sRepId, false, false,
				null);
		return res;
	}

	// Returns the object that represents all discretionary users and
	// attributes.
	// First find the uattr with the property
	// "usersof=discretionary", then add " rep" to its name, and look for an
	// object attribute with this name associated to an object.
	// Parameters: None.

	public ActOpnd[] evalFun_oattr_rep_of_discr_users(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		// Let's find the uattr with the property:
		String sProp = "usersof=discretionary";
		String sUattrId = getEntityWithPropInternal(PM_NODE.UATTR.value, sProp);
		System.out.println("=============uattr with property " + sProp
				+ " found " + sUattrId);

		if (sUattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user attribute with property " + sProp);
			return res;
		}
		String sUattrName = getEntityName(sUattrId,PM_NODE.UATTR.value);
		if (sUattrName == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user attribute with id " + sUattrId);
			return res;
		}
		String sRepName = sUattrName + " rep";
		String sRepId = getEntityId(sRepName,PM_NODE.OATTR.value);
		if (sRepId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No object attribute " + sRepName);
			return res;
		}
		res[0] = new ActOpnd(sRepName,PM_NODE.OATTR.value, sRepId, false, false,
				null);
		return res;
	}

	// Returns the user attribute "DAC users".
	// Parameters: None.

	public ActOpnd[] evalFun_uattr_discr_users(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		// Let's find the uattr with the property:
		String sProp = "usersof=discretionary";
		String sUattrId = getEntityWithPropInternal(PM_NODE.UATTR.value, sProp);
		System.out.println("=============uattr with property " + sProp
				+ " found " + sUattrId);
		if (sUattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user attribute with property " + sProp);
			return res;
		}
		String sUattrName = getEntityName(sUattrId,PM_NODE.UATTR.value);
		if (sUattrName == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user attribute with id " + sUattrId);
			return res;
		}
		res[0] = new ActOpnd(sUattrName,PM_NODE.UATTR.value, sUattrId, false, false,
				null);
		return res;
	}

	// Returns the default process, i.e., the one that triggered the event.
	// Parameters: None.

	public ActOpnd[] evalFun_process_default(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sProcId = eventctx.getProcId();
		if (sProcId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No process in the event context");
			return res;
		}
		// The process name (first arg in ActOpnd) is the same as the process
		// id.
		res[0] = new ActOpnd(sProcId, GlobalConstants.PM_PROCESS, sProcId, false, false, null);
		return res;
	}

	// Returns the default session, i.e., the one that triggered the event.
	// Parameters: None.

	public ActOpnd[] evalFun_session_default(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sSessId = eventctx.getSessId();
		if (sSessId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No session in the event context");
			return res;
		}
		String sSessName = getEntityName(sSessId, GlobalConstants.PM_SESSION);
		if (sSessName == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No session with id " + sSessId);
			return res;
		}
		res[0] = new ActOpnd(sSessName, GlobalConstants.PM_SESSION, sSessId, false, false, null);
		return res;
	}

	// Returns the default user, i.e., the session user.
	// Parameters: None.

	public ActOpnd[] evalFun_user_default(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sSessId = eventctx.getSessId();
		String sUserId = getSessionUserId(sSessId);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user for session " + sSessId);
			return res;
		}
		String sUserName = getEntityName(sUserId,PM_NODE.USER.value);
		if (sUserName == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user with id " + sUserId);
			return res;
		}
		res[0] = new ActOpnd(sUserName,PM_NODE.USER.value, sUserId, false, false,
				null);
		return res;
	}

	// Returns the name attribute of the new user. Its name is "<user> name",
	// where <user> is the new user. The event name must be "User create",
	// the object name must be the new user name, and the object class
	// must be "User".
	// Parameters: None.

	public ActOpnd[] evalFun_uattr_name_of_new_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sEventName = eventctx.getEventName();
		if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
			return res;
		}
		String sUserName = eventctx.getObjName();
		String sClass = eventctx.getObjClass();
		if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Object class is not \"User\"");
			return res;
		}
		String sUserId = getEntityId(sUserName,PM_NODE.USER.value);
		if (sUserId == null) {
			res[0] = new ActOpnd(null, null, null, false, false, "User \""
					+ sUserName + "\" not yet created");
			return res;
		}
		String sAttrName = getUserFullName(sUserId);
		String sAttrId = getEntityId(sAttrName,PM_NODE.UATTR.value);
		// It's not an error to get a null id for the user name attribute.
		// It might not be created yet. But if it is already created, get the
		// id.
		res[0] = new ActOpnd(sAttrName,PM_NODE.UATTR.value, sAttrId, false, false,
				null);

		return res;
	}

	// Returns the name attribute of the user passed as argument,
	// i.e., the user attribute that has the full name of the user as name.
	// Parameters: a user.

	public ActOpnd[] evalFun_uattr_name_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function uattr_user_name");
			return res;
		}
		ActOpnd arg = funArgs.get(0);

		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"The argument of function uattr_user_name is not a user");
			return res;
		}
		String sAttrName = getUserFullName(arg.getId());
		String sAttrId = getEntityId(sAttrName,PM_NODE.UATTR.value);
		// We may get a null id for the attribute. This is not an error,
		// it might not be created yet. But if it's there, we need the id.
		res[0] = new ActOpnd(sAttrName,PM_NODE.UATTR.value, sAttrId, false, false,
				null);
		return res;
	}

	// Returns the property "nameof=<user>", where <user> is a user passed as
	// argument.
	// Parameters: a user.

	public ActOpnd[] evalFun_prop_name_of_user(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function prop_user_name");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Argument of function uattr_user_name is not a user");
			return res;
		}
		String sProp = "nameof=" + arg.getName();
		res[0] = new ActOpnd(sProp, GlobalConstants.PM_UNKNOWN, null, false, false, null);
		return res;
	}

	// Returns the property "usersof=discretionary".
	// Parameters: None.

	public ActOpnd[] evalFun_prop_discr_users(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];
		res[0] = new ActOpnd("usersof=discretionary", GlobalConstants.PM_UNKNOWN, null, false,
				false, null);
		return res;
	}

	// Returns a policy with the property "type=discretionary", if one exists.
	// Parameters: None.

	public ActOpnd[] evalFun_pol_discr(EventContext eventctx, String sFunType,
			Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sProp = "type=discretionary";
		String sPolId = getEntityWithPropInternal(PM_NODE.POL.value, sProp);
		System.out.println("=============policy with property " + sProp
				+ " found " + sPolId);
		if (sPolId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No policy with property " + sProp);
			return res;
		}
		String sPol = getEntityName(sPolId,PM_NODE.POL.value);
		if (sPol == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No policy with id " + sPolId);
			return res;
		}
		res[0] = new ActOpnd(sPol,PM_NODE.POL.value, sPolId, false, false, null);
		return res;
	}

	// Returns the user attribute representing the lowest clearance level of the
	// mls policy, if one exists.
	// Parameters: None.

	public ActOpnd[] evalFun_uattr_lowest_level(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sProp = "type=mls";
		String sPolId = getEntityWithPropInternal(PM_NODE.POL.value, sProp);
		System.out.println("=============policy with property " + sProp
				+ " found " + sPolId);
		if (sPolId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No policy with property " + sProp);
			return res;
		}
		String sPolName = getEntityName(sPolId,PM_NODE.POL.value);
		if (sPolName == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No policy with id " + sPolId);
			return res;
		}
		String sPrefix = "levels=";
		String sLevelsProp = getPropertyWithPrefix(sPrefix, sPolId,PM_NODE.POL.value);
		if (sLevelsProp == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No levels specified in policy " + sPolName);
			return res;
		}
		String sLevels = sLevelsProp.substring(sPrefix.length());
		String[] pieces = sLevels.split(GlobalConstants.PM_LIST_MEMBER_SEP);
		if (pieces.length < 1 || pieces[0] == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No lowest level specified in policy " + sPolName);
			return res;
		}
		String sUattrId = getEntityId(pieces[0],PM_NODE.UATTR.value);
		if (sUattrId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No user attribute at lowest level " + pieces[0]);
			return res;
		}
		res[0] = new ActOpnd(pieces[0],PM_NODE.UATTR.value, sUattrId, false, false,
				null);
		return res;
	}

	// Returns a policy with the property "type=identity", if one exists.
	// Parameters: None.

	public ActOpnd[] evalFun_pol_id(EventContext eventctx, String sFunType,
			Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		String sProp = "type=identity";
		String sPolId = getEntityWithPropInternal(PM_NODE.POL.value, sProp);
		System.out.println("=============policy with property " + sProp
				+ " found " + sPolId);
		if (sPolId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No policy with property " + sProp);
			return res;
		}
		String sPol = getEntityName(sPolId,PM_NODE.POL.value);
		if (sPol == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No policy with id " + sPolId);
			return res;
		}
		res[0] = new ActOpnd(sPol,PM_NODE.POL.value, sPolId, false, false, null);
		return res;
	}

	// Returns a policy with the property passed as the sole argument.
	// Parameters: A property.

	public ActOpnd[] evalFun_pol_with_prop(EventContext eventctx,
			String sFunType, Vector<ActOpnd> funArgs) {
		ActOpnd[] res = new ActOpnd[1];

		if (funArgs.isEmpty() || funArgs.size() != 1) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"Incorrect number of arguments for function pol_with_prop");
			return res;
		}
		ActOpnd arg = funArgs.get(0);
		String sProp = arg.getName();
		String sPolId = getEntityWithPropInternal(PM_NODE.POL.value, sProp);
		System.out.println("=============policy with property " + sProp
				+ " found " + sPolId);
		if (sPolId == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No policy with property " + sProp);
			return res;
		}
		String sPol = getEntityName(sPolId,PM_NODE.POL.value);
		if (sPol == null) {
			res[0] = new ActOpnd(null, null, null, false, false,
					"No policy with id " + sPolId);
			return res;
		}
		res[0] = new ActOpnd(sPol,PM_NODE.POL.value, sPolId, false, false, null);
		return res;
	}


	public Packet applyActionAssignLike(EventContext eventctx, String sActionId) {
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet applyActionGrant(EventContext eventctx, String sActionId) {
		try {
			Attributes actAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
					+ sRuleContainerDN);
			Attribute attr = actAttrs.get("pmOpnd1");
			if (attr == null) {
				return failurePacket("No operand 1 in \"Grant\" action "
						+ sActionId);
			}
			HashSet<ActOpnd> hsOpnd1 = new HashSet<ActOpnd>();
			String sLastError = null;
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOpndId = (String) enumer.next();
				// Get the runtime action operand and insert it into the HashSet
				// of
				// first operands. Most of the times, the run-time operand is
				// the same as the
				// compile-time operand. A function operand at run-time is
				// different.
				ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 1: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd1.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd1, "Set of first operands in \"Grant\"");
			if (hsOpnd1.isEmpty()) {
				return failurePacket("No first operands in grant. Last error was: "
						+ sLastError);
			}

			attr = actAttrs.get("pmOpnd2");
			if (attr == null) {
				return failurePacket("No operand 2 in \"Grant\" action "
						+ sActionId);
			}
			HashSet<ActOpnd> hsOpnd2 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOpndId = (String) enumer.next();
				ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 2: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd2.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd2, "Set of second operands in \"Grant\"");
			if (hsOpnd2.isEmpty()) {
				return failurePacket("No second operands in \"Grant\". Last error was: "
						+ sLastError);
			}

			attr = actAttrs.get("pmOpnd3");
			if (attr == null) {
				return failurePacket("No operand 3 in \"Grant\" action "
						+ sActionId);
			}
			HashSet<ActOpnd> hsOpnd3 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOpndId = (String) enumer.next();
				ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 3: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd3.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd3, "Set of third operands in \"Grant\"");
			if (hsOpnd3.isEmpty()) {
				return failurePacket("No third operands in \"Grant\". Last error was: "
						+ sLastError);
			}

			// Generate a name for the operation set.
			Random random = new Random();
			byte[] bytes = new byte[4];
			random.nextBytes(bytes);
			String sOpset = UtilMethods.byteArray2HexString(bytes);
			System.out.println("============opset name is " + sOpset);
			// Create an empty opset and assign it to connector.
			Packet res =  addOpsetAndOpInternal(sOpset, null, sOpset,
					sOpset, null, null, null, null, null);
			if (res.hasError()) {
				return res;
			}
			// Get the opset name and id from the result.
			String sLine = res.getStringValue(0);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			String sOpsetId = pieces[1];
			// Add all the ops in operands 2 to the new opset.
			Iterator<ActOpnd> iter2 = hsOpnd2.iterator();
			while (iter2.hasNext()) {
				ActOpnd opnd2 = iter2.next();
				res =  addOpsetAndOpInternal(sOpset, null, null, null,
						opnd2.getName(), null, null, null, null);
				if (res.hasError()) {
					return res;
				}
			}

			// Assign each operand1 to the opset.
			Iterator<ActOpnd> iter1 = hsOpnd1.iterator();
			while (iter1.hasNext()) {
				ActOpnd opnd1 = iter1.next();
				res =  assignInternal(opnd1.getId(), opnd1.getType(),
						sOpsetId,PM_NODE.OPSET.value);
				if (res.hasError()) {
					return res;
				}
			}

			// Assign the opset to each operand3.
			Iterator<ActOpnd> iter3 = hsOpnd3.iterator();
			while (iter3.hasNext()) {
				ActOpnd opnd3 = iter3.next();
				res =  assignInternal(sOpsetId,PM_NODE.OPSET.value,
						opnd3.getId(), opnd3.getType());
				if (res.hasError()) {
					return res;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while dispatching event action");
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet applyActionDeny(EventContext eventctx, String sActionId) {
		try {
			Attributes actAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
					+ sRuleContainerDN);
			Attribute attr = actAttrs.get("pmIsIntrasession");
			if (attr == null) {
				return failurePacket("No intrasession attribute in \"Deny\" action "
						+ sActionId);
			}
			boolean bIntrasession = ((String) attr.get()).equals("TRUE");
			attr = actAttrs.get("pmIsIntersection");
			if (attr == null) {
				return failurePacket("No intersection attribute in \"Deny\" action "
						+ sActionId);
			}
			boolean bIntersection = ((String) attr.get()).equals("TRUE");

			attr = actAttrs.get("pmOpnd1");
			if (attr == null) {
				return failurePacket("No operand 1 in \"Deny\" action "
						+ sActionId);
			}
			String sLastError = null;

			// There should be only one first operand - a session, a user or a
			// user attribute.
			String sOpndId = (String) attr.get();
			// Get the runtime action operand
			ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
			sLastError = actOpnds[0].getError();
			if (sLastError != null) {
				System.out.println("Last error when evaluating operand 1: "
						+ sLastError);
				return failurePacket("No first operands in \"Deny\". Last error was: "
						+ sLastError);
			}
			ActOpnd actOpnd1 = actOpnds[0];
			printOpnd(actOpnd1, "First operand in \"Deny\"");

			attr = actAttrs.get("pmOpnd2");
			if (attr == null) {
				return failurePacket("No operand 2 in \"Deny\" action "
						+ sActionId);
			}
			HashSet<ActOpnd> hsOpnd2 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				sOpndId = (String) enumer.next();
				actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 2: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd2.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd2, "Set of second operands in \"Deny\"");
			if (hsOpnd2.isEmpty()) {
				return failurePacket("No second operands in \"Deny\". Last error was: "
						+ sLastError);
			}

			attr = actAttrs.get("pmOpnd3");
			if (attr == null) {
				return failurePacket("No operand 3 in \"Deny\" action "
						+ sActionId);
			}
			HashSet<ActOpnd> hsOpnd3 = new HashSet<ActOpnd>();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				sOpndId = (String) enumer.next();
				actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 3: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd3.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd3, "Set of third operands in \"Deny\"");
			if (hsOpnd3.isEmpty()) {
				return failurePacket("No third operands in \"Deny\". Last error was: "
						+ sLastError);
			}

			// Generate a name for the deny constraint.
			Random random = new Random();
			byte[] bytes = new byte[4];
			random.nextBytes(bytes);
			String sDenyName = UtilMethods.byteArray2HexString(bytes);
			System.out.println("============deny name is " + sDenyName);
			String sType = actOpnd1.getType();
			String sDenyType = null;
			if (sType.equalsIgnoreCase(GlobalConstants.PM_SESSION)) {
				sDenyType = GlobalConstants.PM_DENY_SESSION;
			} else if (sType.equalsIgnoreCase(GlobalConstants.PM_PROCESS)) {
				sDenyType = GlobalConstants.PM_DENY_PROCESS;
			} else if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
				sDenyType = GlobalConstants.PM_DENY_USER_ID;
			} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				sDenyType = GlobalConstants.PM_DENY_ACROSS_SESSIONS;
				if (bIntrasession) {
					sDenyType = GlobalConstants.PM_DENY_INTRA_SESSION;
				}
			} else {
				return failurePacket("Incorrect type for first \"Deny\" operand");
			}

			String sSimDeny = getSimilarDeny(sDenyType, actOpnd1.getName(),
					actOpnd1.getId(), bIntersection, hsOpnd2, hsOpnd3);
			if (sSimDeny != null) {
				System.out.println("A similar deny constraint exists.");
				return ADPacketHandler.getSuccessPacket();
			}

			// Create a deny constraint of the correct class/type.
			System.out.println("Creating the deny relation");
			Packet res =  addDenyInternal(sDenyName, sDenyType,
					actOpnd1.getName(), actOpnd1.getId(), null, null, null,
					bIntersection);
			if (res.hasError()) {
				return res;
			}

			// Get the deny name and id from the result.
			String sLine = res.getStringValue(0);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			sDenyName = pieces[0];

			// Add all the ops in operands 2 to the new deny constraint.
			System.out.println("Adding the operations to the deny relation");
			Iterator<ActOpnd> iter2 = hsOpnd2.iterator();
			while (iter2.hasNext()) {
				ActOpnd opnd2 = iter2.next();
				res =  addDenyInternal(sDenyName, null, null, null,
						opnd2.getName(), null, null, bIntersection);
				if (res.hasError()) {
					return res;
				}
			}

			// Add all the containers (object attributes) to the new deny
			// constraint.
			System.out.println("Adding the containers to the deny relation");
			Iterator<ActOpnd> iter3 = hsOpnd3.iterator();
			while (iter3.hasNext()) {
				ActOpnd opnd3 = iter3.next();
				String sContName;
				if (opnd3.isComplement()) {
					sContName = "!" + opnd3.getName();
				} else {
					sContName = opnd3.getName();
				}
				res =  addDenyInternal(sDenyName, null, null, null,
						null, sContName, opnd3.getId(), bIntersection);
				if (res.hasError()) {
					return res;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while dispatching event action");
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Apply a "create" action. First evaluate the operands, because the
	// run-time value of an operand could be different from the compile-time
	// value (when the operand is a function).
	// The first operand specifies the entity to be created (usually name and
	// type).
	// The second operand specifies the PM entity represented by the entity to
	// be
	// created if that entity is an object, or is empty.
	// The third operand specifies the properties of the entity to be created,
	// or is empty.
	// The fourth operand specifies the container where the entity to be created
	// should reside.

	public Packet applyActionCreate(EventContext eventctx, String sActionId) {
		try {
			Attributes actAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
					+ sRuleContainerDN);
			Attribute attr = actAttrs.get("pmOpnd1");
			if (attr == null) {
				return failurePacket("No operand 1 in \"Create\" action");
			}
			HashSet<ActOpnd> hsOpnd1 = new HashSet<ActOpnd>();
			String sLastError = null;
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOpndId = (String) enumer.next();
				System.out.println("Evaluating first operand " + sOpndId);
				// Get the runtime action operand and insert it into the HashSet
				// of
				// first operands. Most of the times, the run-time operand is
				// the same as the
				// compile-time operand. A function operand at run-time is
				// different.
				ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
				sLastError = actOpnds[0].getError();
				if (sLastError != null) {
					System.out.println("Last error when evaluating operand 1: "
							+ sLastError);
					continue;
				}
				for (int i = 0; i < actOpnds.length; i++) {
					hsOpnd1.add(actOpnds[i]);
				}
			}
			printOpndSet(hsOpnd1, "Set of first operands in \"Create\"");
			if (hsOpnd1.isEmpty()) {
				return failurePacket("No first operands in \"Create\". Last error was: "
						+ sLastError);
			}

			attr = actAttrs.get("pmOpnd2");
			HashSet<ActOpnd> hsOpnd2 = new HashSet<ActOpnd>();
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpndId = (String) enumer.next();
					System.out.println("Evaluating second operand " + sOpndId);
					ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
					sLastError = actOpnds[0].getError();
					if (sLastError != null) {
						System.out.println("Last error when evaluating operand 2: "
								+ sLastError);
						continue;
					}
					for (int i = 0; i < actOpnds.length; i++) {
						hsOpnd2.add(actOpnds[i]);
					}
				}
			}
			printOpndSet(hsOpnd2, "Set of second operands in \"Create\"");

			attr = actAttrs.get("pmOpnd3");
			HashSet<ActOpnd> hsOpnd3 = new HashSet<ActOpnd>();
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpndId = (String) enumer.next();
					System.out.println("Evaluating third operand " + sOpndId);
					ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
					sLastError = actOpnds[0].getError();
					if (sLastError != null) {
						System.out.println("Last error when evaluating operand 3: "
								+ sLastError);
						continue;
					}
					for (int i = 0; i < actOpnds.length; i++) {
						hsOpnd3.add(actOpnds[i]);
					}
				}
			}
			printOpndSet(hsOpnd3, "Set of third operands in \"Create\"");

			attr = actAttrs.get("pmOpnd4");
			HashSet<ActOpnd> hsOpnd4 = new HashSet<ActOpnd>();
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpndId = (String) enumer.next();
					System.out.println("Evaluating fourth operand " + sOpndId);
					ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
					sLastError = actOpnds[0].getError();
					if (sLastError != null) {
						System.out.println("Last error when evaluating operand 4: "
								+ sLastError);
						continue;
					}
					for (int i = 0; i < actOpnds.length; i++) {
						hsOpnd4.add(actOpnds[i]);
					}
				}
			}
			printOpndSet(hsOpnd4, "Set of fourth operands in \"Create\"");

			// Create entity specified by operand 1...
			ActOpnd opnd1 = null;
			Iterator<ActOpnd> iter = hsOpnd1.iterator();
			while (iter.hasNext()) {
				opnd1 = iter.next();
			}
			// opnd1 cannot be null.
			String sEntType = opnd1.getType();
			String sEntName = opnd1.getName();

			// ...within the container specified in operand 4...
			ActOpnd opnd4 = null;
			iter = hsOpnd4.iterator();
			while (iter.hasNext()) {
				opnd4 = iter.next();
			}
			String sContType = null;
			String sContId = null;
			if (opnd4 != null) {
				sContType = opnd4.getType();
				sContId = opnd4.getId();
			}

			// ...with the properties specified in operands 3...
			ActOpnd opnd3 = null;
			int n = hsOpnd3.size();
			String[] sProps = null;
			if (n > 0) {
				sProps = new String[n];
				iter = hsOpnd3.iterator();
				int i = 0;
				while (iter.hasNext()) {
					opnd3 = iter.next();
					sProps[i++] = opnd3.getName();
				}
			}

			// ...and (if the new entity is an object) representing the entity
			// specified by operand 2 (ignored if the entity to be created
			// is NOT an object).
			ActOpnd opnd2 = null;
			iter = hsOpnd2.iterator();
			while (iter.hasNext()) {
				opnd2 = iter.next();
			}

			String sRepEntName = null;
			String sRepEntId = null;
			String sRepEntType = null;
			boolean bInh = false;
			if (opnd2 != null) {
				sRepEntName = opnd2.getName();
				sRepEntId = opnd2.getId();
				sRepEntType = opnd2.getType();
				bInh = opnd2.isSubgraph();
			}

			System.out.println(".........Represented entity name "
					+ sRepEntName);
			System.out.println(".........Represented entity id " + sRepEntId);
			System.out.println(".........Represented entity type "
					+ sRepEntType);
			System.out.println(".........Represented entity subgraph " + bInh);

			Packet result = null;

			if (!sEntType.equalsIgnoreCase(GlobalConstants.PM_RULE)
					&& (sContType == null || sContId == null)) {
				return failurePacket("Container unspecified in operand 4!");
			}

			if (sEntType.equalsIgnoreCase(GlobalConstants.PM_RULE)) {
				result =  ADPacketHandler.getSuccessPacket("Now compile rule: " + sEntName);
				System.out.println(result.getStringValue(0));

				// Compile the rule and insert it into the enabled script.
				Packet script = new Packet();
				// The function that processes the script expects it to start at
				// item 3.
				try {
					script.addItem(ItemType.RESPONSE_TEXT, "Filler");
					script.addItem(ItemType.RESPONSE_TEXT, "Filler");
					script.addItem(ItemType.RESPONSE_TEXT, "Filler");
					script.addItem(ItemType.RESPONSE_TEXT, "script genrule");
					script.addItem(ItemType.RESPONSE_TEXT, sEntName);
				} catch (Exception e) {
					e.printStackTrace();
					return failurePacket("Exception when building the result packet");
				}
				Packet res =  compileScriptAndAddToEnabled(null, script);
				if (res == null) {
					return failurePacket("compileScript... returned null!");
				}
				return res; // failure or success, possibly name:id of the
				// compiled script.

			} else if (sEntType.equalsIgnoreCase(PM_NODE.POL.value)) {
				result =  addPcInternal(sEntName, null, sEntName,
						sEntName, sProps);
			} else if (sEntType.equalsIgnoreCase(PM_NODE.USER.value)) {
				result =  addUserInternal(sEntName, null, sEntName,
						sEntName, sEntName, sContId, sContType);
			} else if (sEntType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				result =  addUattrInternal(sEntName, sEntName,
						sEntName, sContId, sContType, sProps);
			} else if (sEntType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
				result =  addOattrInternal(sEntName, null, sEntName,
						sEntName, sContId, sContType, null, sProps);
			} else if (sEntType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
				// Only an object representing a PM entity/subgraph can be
				// created
				// as a response to an event.
				if (opnd2 == null) {
					return failurePacket("The object to create as a response must represent a PM entity!");
				}
				// Find the object class based on the represented entity type.
				String sObjClass = typeToClass(sRepEntType);
				result =  addObjectInternal(sEntName, null, null,
						sEntName, sEntName, sContId, sContType, sObjClass,
						null, null, null, sRepEntName, sRepEntId, bInh, null,
						null, null, null, null, null, null);
			} else {
				return failurePacket("Creation of this type of entity not implemented");
			}
			if (result.hasError()) {
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while dispatching event action");
		}

		return ADPacketHandler.getSuccessPacket();
	}


	public Packet matchEvent(EventContext eventctx, String sRuleId) {
		System.out.println("Matching event against rule " + sRuleId + "...");
		// Get rule's event pattern.
		try {
			Attributes ruleAttrs = ServerConfig.ctx.getAttributes("CN=" + sRuleId + ","
					+ sRuleContainerDN);
			Attribute attr = ruleAttrs.get("pmEventPattern");
			if (attr == null) {
				return failurePacket("No event pattern in rule " + sRuleId);
			}
			String sPatternId = (String) attr.get();

			Packet res =  eventUserMatchesPattern(eventctx.getSessId(),
					eventctx.getProcId(), sPatternId);
			if (res.hasError()) {
				return res;
			}

			res =  eventNameMatchesPattern(eventctx.getEventName(),
					sPatternId);
			if (res.hasError()) {
				return res;
			}

			res =  eventObjMatchesPattern(eventctx, sPatternId);
			return res;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while matching event to pattern in rule "
					+ sRuleId + ": " + e.getMessage());
		}
	}

	// Matching the event's object spec against the pattern's object spec and
	// container specs.
	//
	// The event's object spec should contain: the object name, the object id,
	// the obj class name, the list of first level containers of the object
	// in the format t1|name1,t2|name2,..., where ti is the container
	// type, and namei is the container name, and finally the id of the
	// object's associated attribute.
	//
	// The pattern's object spec may be missing, or is '*', or is 'b'|associd.
	// The pattern's container spec may be missing, or is a list
	// t1|id1,t2|id2,..., where ti is 'b' or 'oc', and idi is the id of an
	// object attribute or object class.
	//
	// The matching algorithm.
	// If the pattern's object spec is not missing and is not '*',
	// check whether the event's object is the one specified in the
	// pattern's object spec. If not, return false.
	// If the pattern's container spec is missing, return true.
	// If the event's object is contained in one of the pattern's object
	// attributes, or the event's object class is the same as one of the
	// pattern's classes, return true.
	// Otherwise return false.

	public Packet eventObjMatchesPattern(EventContext eventctx,
			String sPatternId) {
		// The event name, object name, id, class, containers, assoc oattr.
		String sEventName = eventctx.getEventName();
		String sEventObjName = eventctx.getObjName();
		String sEventObjId = eventctx.getObjId();
		String sEventObjClass = eventctx.getObjClass();
		String sEventContainers = eventctx.getctx1();
		String sEventAssocId = eventctx.getctx2();

		if (sEventObjClass != null
				&& sEventObjClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_FILE_NAME)
				&& sEventObjId != null) {
			sEventAssocId = getAssocOattr(sEventObjId);
		}
		String sEventObjClassId = null;
		if (sEventObjClass != null) {
			sEventObjClassId = getEntityId(sEventObjClass, GlobalConstants.PM_OBJ_CLASS);
		}

		System.out.println("===========Event's name is " + sEventName);
		System.out.println("===========Event's object name is " + sEventObjName);
		System.out.println("===========Event's object id is " + sEventObjId);
		System.out.println("===========Event's object class " + sEventObjClass);
		System.out.println("===========Event's object class id "
				+ sEventObjClassId);
		System.out.println("===========Event's object containers are "
				+ sEventContainers);
		System.out.println("===========Event's object assoc id is "
				+ sEventAssocId);
		System.out.println("===========");

		try {
			Attributes patternAttrs = ServerConfig.ctx.getAttributes("CN=" + sPatternId
					+ "," + sRuleContainerDN);
			Attribute attr = patternAttrs.get("pmObjSpec");

			String sPatObjSpec = null;
			String sPatContSpec = null;

			if (attr != null) {
				sPatObjSpec = (String) attr.get();
			}
			System.out.println("===========Pattern's object spec is "
					+ sPatObjSpec);

			attr = patternAttrs.get("pmContSpec");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sPatContSpec = (String) enumer.next();
					System.out.println("===========Pattern's container spec is "
							+ sPatContSpec);
				}
			}

			// Let's match the event's object against the pattern's objspec.

			// If the pattern's objspec is not null and is not '*', check
			// the objname/objid of the event against the pattern's.
			if (sPatObjSpec != null && !sPatObjSpec.equals("*")) {
				// The pattern's objspec has the format type|name, where the
				// type should be 'b'.
				String[] pieces = sPatObjSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				// Pieces[0] is the type (oc or b). Pieces[1] is the id.
				String sPatObjName = pieces[1];

				if (!sEventObjName.equalsIgnoreCase(sPatObjName)) {
					return failurePacket("Event's object does not match pattern object spec!");
				}
			}

			// Now let's match the event's object against the pattern's
			// container specs.
			// If the pattern has no container specs, the event matches the
			// pattern.
			if (sPatContSpec == null || sPatContSpec.equals("*")) {
				return ADPacketHandler.getSuccessPacket();
			}

			// VERY IMPORTANT NOTE: Probably we need to check that the event's
			// object is still included in the original containers as specified
			// in the event. This would prevent applying rules in cascade if the
			// object location is changed by the first (and subsequent) rule.
			if (sEventContainers != null) {
				String[] pieces = sEventContainers.split(GlobalConstants.PM_LIST_MEMBER_SEP);
				for (int i = 0; i < pieces.length; i++) {
					String[] sTypeAndName = pieces[i].split(GlobalConstants.PM_ALT_DELIM_PATTERN);
					System.out.println("Container no. " + i + ": "
							+ sTypeAndName[0] + "|" + sTypeAndName[1]);
					String sEventContId = getEntityId(sTypeAndName[1],
							sTypeAndName[0]);
					if (!attrIsAscendant(sEventAssocId, sEventContId,
							PM_NODE.OATTR.value)) {
						return failurePacket("Event's object is no longer in the original containers!");
					}
				}
			}

			// To match, the event's object must be included in at least one of
			// the pattern's containers.
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				sPatContSpec = (String) enumer.next();
				System.out.println("For pattern's container spec "
						+ sPatContSpec);

				String[] pieces = sPatContSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				// Pieces[0] is the type (oc or b or rec). Pieces[1] is the id
				// or *
				// (in the case "any record").
				if (pieces[0].equalsIgnoreCase(GlobalConstants.PM_OBJ_CLASS)) {
					if (sEventObjClassId != null
							&& sEventObjClassId.equalsIgnoreCase(pieces[1])) {
						System.out.println("Event obj class matches pattern cont spec");
						return ADPacketHandler.getSuccessPacket();
					}
				} else if (pieces[0].equalsIgnoreCase(PM_NODE.OATTR.value)) {
					if (sEventAssocId != null
							&& attrIsAscendant(sEventAssocId, pieces[1],
									PM_NODE.OATTR.value)) {
						System.out.println("Event obj is contained in pattern cont spec");
						return ADPacketHandler.getSuccessPacket();
					}
				} else if (pieces[0].equalsIgnoreCase(GlobalConstants.PM_RECORD)) {
					String sRecordId = pieces[1];
					if (sEventAssocId != null) {
						if (sRecordId.equals("*") && isInARecord(sEventAssocId)) {
							return ADPacketHandler.getSuccessPacket();
						} else if (!sRecordId.equals("*")
								&& isInRecord(sEventAssocId, sRecordId)) {
							return ADPacketHandler.getSuccessPacket();
						}
					}
				}
				System.out.println("Event obj does not match this pattern cont spec. Continuing...");
			}
			return failurePacket("Event's object does not match pattern container specs!");
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception during event object matching!");
		}
	}


	public Packet eventNameMatchesPattern(String sEventOp, String sPatternId) {
		// System.out.println("<<<eventNameMatchesPattern " + sPatternId);
		// Get the op specs from the pattern.
		try {
			Attributes patternAttrs = ServerConfig.ctx.getAttributes("CN=" + sPatternId
					+ "," + sRuleContainerDN);
			Attribute attr = patternAttrs.get("pmOpSpec");
			if (attr == null) {
				return ADPacketHandler.getSuccessPacket();
			}
			String sOpSpec = (String) attr.get();
			if (sOpSpec.equals("*")) {
				return ADPacketHandler.getSuccessPacket();
			}

			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				sOpSpec = (String) enumer.next();
				if (sEventOp.equalsIgnoreCase(sOpSpec)) {
					return ADPacketHandler.getSuccessPacket();
				}
			}
			return failurePacket("Event operation doesn't match specs!");
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception during event operation matching!");
		}
	}


	public Packet eventUserMatchesPattern(String sSessId, String sProcId,
			String sPatternId) {
		// System.out.println("<<<userMatchesPattern " + sPatternId);
		// Get the user specs from the pattern.
		try {
			Attributes patternAttrs = ServerConfig.ctx.getAttributes("CN=" + sPatternId
					+ "," + sRuleContainerDN);
			Attribute attr = patternAttrs.get("pmUserSpec");

			// If user spec is missing from the pattern, or is empty, the user
			// can be
			// anything. We still need to check the pc specs.
			if (attr == null) {
				// System.out.println("No user specs in pattern");
				if (userMatchesPatternPc(sSessId, sPatternId)) {
					return ADPacketHandler.getSuccessPacket();
				} else {
					return failurePacket("Event user does not match the pattern!");
				}
			}

			// If user spec contains '*', the user can be anything. We still
			// need
			// to check the pc specs.
			String sUserSpec = (String) attr.get();
			if (sUserSpec.equals("*")) {
				// System.out.println("The pattern specifies any user (*)");
				if (userMatchesPatternPc(sSessId, sPatternId)) {
					return ADPacketHandler.getSuccessPacket();
				} else {
					return failurePacket("Event user does not match the pattern!");
				}
			}

			// At least a user, a user attribute, a session, or a process
			// is specified in the pattern.
			// Check whether the event's user/session/process matches any of
			// those specified.
			String sSessUserId = getSessionUserId(sSessId);
			// System.out.println("Event user is " + sSessUserId);

			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				sUserSpec = (String) enumer.next();
				System.out.println("Found user spec " + sUserSpec);

				String[] pieces = sUserSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				if (pieces[0].equalsIgnoreCase(PM_NODE.USER.value)) {
					// The spec is a user.
					if (!sSessUserId.equalsIgnoreCase(pieces[1])) {
						continue;
					}
					if (userMatchesPatternPc(sSessId, sPatternId)) {
						return ADPacketHandler.getSuccessPacket();
					}
				} else if (pieces[0].equalsIgnoreCase(PM_NODE.UATTR.value)) {
					// The spec is a user attribute, not necessarily active for
					// the user.
					// We check only that the user has that attribute.
					if (!userIsAscendant(sSessUserId, pieces[1])) {
						continue;
					}
					if (userAttrMatchesPatternPc(sSessId, pieces[1], sPatternId)) {
						return ADPacketHandler.getSuccessPacket();
					}
				} else if (pieces[0].equalsIgnoreCase(GlobalConstants.PM_SESSION)) {
					// The spec is a session.
					// We check only that the current session is the same as the
					// one in spec.
					if (!sSessId.equalsIgnoreCase(pieces[1])) {
						continue;
					}
					if (userAttrMatchesPatternPc(sSessId, pieces[1], sPatternId)) {
						return ADPacketHandler.getSuccessPacket();
					}
				} else if (pieces[0].equalsIgnoreCase(GlobalConstants.PM_PROCESS)) {
					// The spec is a process.
					// We check only that the current process is the same as the
					// one in spec.
					if (!sProcId.equalsIgnoreCase(pieces[1])) {
						continue;
					}
					if (userAttrMatchesPatternPc(sSessId, pieces[1], sPatternId)) {
						return ADPacketHandler.getSuccessPacket();
					}
				} else {
					// The spec is an active user attribute. Check whether the
					// user
					// is active in that attribute in the current session.
					if (userIsActiveInAttr(sSessId, pieces[1])) {
						System.out.println("Session user found active in attribute "
								+ getEntityName(pieces[1],
										PM_NODE.UATTR.value));
						return ADPacketHandler.getSuccessPacket();
					}
					System.out.println("Session user NOT active in attribute "
							+ getEntityName(pieces[1],PM_NODE.UATTR.value));
				}
			}
			return failurePacket("Event user does not match pattern!");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while matching event user to pattern "
					+ sPatternId + ": " + e.getMessage());
		}
	}


	public boolean userMatchesPatternPc(String sSessId, String sPatternId) {
		// System.out.println("<<<userMatchesPatternPc " + sPatternId);
		try {
			Attributes patternAttrs = ServerConfig.ctx.getAttributes("CN=" + sPatternId
					+ "," + sRuleContainerDN);
			Attribute attr = patternAttrs.get("pmIsAny");
			boolean isAny = ((String) attr.get()).equals("TRUE");
			attr = patternAttrs.get("pmIsActive");
			boolean isActive = ((String) attr.get()).equals("TRUE");
			attr = patternAttrs.get("pmPolicySpec");
			if (isActive) {
				// System.out.println("User must be active");
				if (isAny) {
					// If no pc specs:
					// System.out.println("Pc is any of...");
					if (attr == null) {
						return userIsActive(sSessId);
					}

					// If a pc spec containing *:
					String s = (String) attr.get();
					// System.out.println("The case: get first pc spec and see if it's *: "
					// + s);
					if (s.equals("*")) {
						return userIsActive(sSessId);
					}

					// Some pc specs:
					return userIsActiveInAnyPolicy(sSessId, attr);
				} else {
					// It's each policy.
					return userIsActiveInEachPolicy(sSessId, attr);
				}
			} else {
				// System.out.println("User doesn't have to be active");
				// It's sufficient to be in, not necessarily active.
				if (isAny) {
					// If no pc specs:
					// System.out.println("Pc is any of...");
					if (attr == null) {
						return true;
					}

					// If a pc spec containing *:
					String s = (String) attr.get();
					// System.out.println("The case: get first pc spec and see if it's *: "
					// + s);
					if (s.equals("*")) {
						return true;
					}

					// Some pc specs:
					return userIsInAnyPolicy(sSessId, attr);
				} else {
					// It's each policy.
					return userIsInEachPolicy(sSessId, attr);
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}


	public boolean userIsActive(String sSessId) {
		HashSet<String> hs = getSessionActiveAttrSet(sSessId);
		return (!hs.isEmpty());
	}

	// Whether the session's user is active in the specified attribute.

	public boolean userIsActiveInAttr(String sSessId, String sUattrId) {
		HashSet<String> hs = getSessionActiveAttrSet(sSessId);
		Iterator<String> iter = hs.iterator();
		while (iter.hasNext()) {
			String sCrtActiveId = iter.next();
			if (sCrtActiveId.equalsIgnoreCase(sUattrId)) {
				return true;
			}
		}
		return false;
	}

	// The Attribute attr contains all values of pmPcSpec.

	public boolean userIsActiveInAnyPolicy(String sSessId, Attribute attr) {
		if (attr == null) {
			return true;
		}

		HashSet<String> hs = getSessionActiveAttrSet(sSessId);

		try {
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sPcSpec = (String) enumer.next();
				String[] pieces = sPcSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				if (userIsActiveInPolicy(hs, pieces[1])) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// The Attribute attr contains all values of pmPcSpec.

	public boolean userIsActiveInEachPolicy(String sSessId, Attribute attr) {
		if (attr == null) {
			return true;
		}

		HashSet<String> hs = getSessionActiveAttrSet(sSessId);
		try {
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sPcSpec = (String) enumer.next();
				String[] pieces = sPcSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				if (!userIsActiveInPolicy(hs, pieces[1])) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// At least one of the session's active attributes must be in the
	// specified policy.

	public boolean userIsActiveInPolicy(HashSet<String> activeAttrs,
			String sPolId) {
		Iterator<String> iter = activeAttrs.iterator();
		while (iter.hasNext()) {
			String sAttrId = iter.next();
			if (attrIsAscendantToPolicy(sAttrId,PM_NODE.UATTR.value, sPolId)) {
				return true;
			}
		}
		return false;
	}


	public boolean userIsInAnyPolicy(String sSessId, Attribute attr) {
		if (attr == null) {
			return true;
		}
		try {
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sPcSpec = (String) enumer.next();
				String[] pieces = sPcSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				if (userIsInPolicy(sSessId, pieces[1])) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	public boolean userIsInEachPolicy(String sSessId, Attribute attr) {
		if (attr == null) {
			return true;
		}
		try {
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sPcSpec = (String) enumer.next();
				String[] pieces = sPcSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				if (!userIsInPolicy(sSessId, pieces[1])) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	public boolean userIsInPolicy(String sSessId, String sPolId) {
		String sUserId = getSessionUserId(sSessId);
		return userIsAscendantToPolicy(sUserId, sPolId);
	}


	public boolean userAttrMatchesPatternPc(String sSessId, String sAttrId,
			String sPatternId) {
		System.out.println("[userAttrMatchesPatternPc " + sAttrId
				+ ", pattern = " + sPatternId);
		try {
			Attributes patternAttrs = ServerConfig.ctx.getAttributes("CN=" + sPatternId
					+ "," + sRuleContainerDN);
			Attribute attr = patternAttrs.get("pmIsAny");
			boolean isAny = ((String) attr.get()).equals("TRUE");
			attr = patternAttrs.get("pmIsActive");
			boolean isActive = ((String) attr.get()).equals("TRUE");
			attr = patternAttrs.get("pmPcSpec");
			if (isActive) {
				HashSet<String> hs = getSessionActiveAttrSet(sSessId);
				if (!hs.contains(sAttrId)) {
					return false;
				}
			}

			if (isAny) {
				// If no pc specs:
				if (attr == null) {
					return true;
				}

				// If a pc spec containing *:
				String s = (String) attr.get();
				System.out.println("The case: get first pc spec and see if it's *: "
						+ s);
				if (s.equals("*")) {
					return true;
				}

				// Some pc specs:
				return userAttrIsInAnyPolicy(sAttrId, attr);
			} else {
				// It's each policy.
				return userAttrIsInEachPolicy(sAttrId, attr);
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}


	public boolean userAttrIsInAnyPolicy(String sAttrId, Attribute attr) {
		if (attr == null) {
			return true;
		}
		try {
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sPcSpec = (String) enumer.next();
				String[] pieces = sPcSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				if (attrIsAscendantToPolicy(sAttrId,PM_NODE.UATTR.value, pieces[1])) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	public boolean userAttrIsInEachPolicy(String sAttrId, Attribute attr) {
		if (attr == null) {
			return true;
		}
		try {
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sPcSpec = (String) enumer.next();
				String[] pieces = sPcSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				if (!attrIsAscendantToPolicy(sAttrId,PM_NODE.UATTR.value, pieces[1])) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Add a property to a PM entity (user attr, obj attr, or policy).

	public Packet addProp(String sSessId, String sId, String sType,
			String sIsVos, String sProp) {
		if (sIsVos.equalsIgnoreCase("yes")) {
			sId = getAdminVosNodeOrigId(sId);
		}

		// Test permissions.

		return addPropInternal(sId, sType, sProp);
	}


	public Packet addPropInternal(String sId, String sType, String sProp) {
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmProperty", sProp));
		String sDn;
		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId + "," + sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sDn = "CN=" + sId + "," + sPolicyContainerDN;
		} else {
			return failurePacket("Invalid entity type!");
		}

		try {
			ServerConfig.ctx.modifyAttributes(sDn, mods);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Unable to add property " + sProp
					+ " to the entity!");
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet replaceProp(String sSessId, String sId, String sType,
			String sIsVos, String sOldProp, String sNewProp) {
		if (sIsVos.equalsIgnoreCase("yes")) {
			sId = getAdminVosNodeOrigId(sId);
		}
		ModificationItem[] mods = new ModificationItem[2];
		mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
				new BasicAttribute("pmProperty", sOldProp));
		mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmProperty", sNewProp));
		String sDn;
		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId + "," + sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sDn = "CN=" + sId + "," + sPolicyContainerDN;
		} else {
			return failurePacket("Invalid entity type!");
		}

		try {
			ServerConfig.ctx.modifyAttributes(sDn, mods);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Unable to replace property " + sOldProp
					+ " by " + sNewProp + "!");
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet removeProp(String sSessId, String sId, String sType,
			String sIsVos, String sProp) {
		if (sIsVos.equalsIgnoreCase("yes")) {
			sId = getAdminVosNodeOrigId(sId);
		}
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
				new BasicAttribute("pmProperty", sProp));
		String sDn;
		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId + "," + sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sDn = "CN=" + sId + "," + sPolicyContainerDN;
		} else {
			return failurePacket("Invalid entity type!");
		}

		try {
			ServerConfig.ctx.modifyAttributes(sDn, mods);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Unable to remove property " + sProp
					+ " from the entity!");
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Get the properties set of a PM entity.

	public HashSet<String> getProps(String sId, String sType) {
		HashSet<String> hs = new HashSet<String>();
		String sDn = null;
		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId + "," + sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sDn = "CN=" + sId + "," + sPolicyContainerDN;
		} else {
			return hs;
		}

		try {
			Attribute attr = ServerConfig.ctx.getAttributes(sDn).get("pmProperty");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					hs.add((String) enumer.next());
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return hs;
	}

	// Return the property of the entity specified by id and type that starts
	// with the specified prefix, if one exists. Otherwise return null.

	public String getPropertyWithPrefix(String sPrefix, String sId,
			String sType) {
		String sDn = null;
		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sId + "," + sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sDn = "CN=" + sId + "," + sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sDn = "CN=" + sId + "," + sPolicyContainerDN;
		} else {
			return null;
		}

		try {
			Attribute attr = ServerConfig.ctx.getAttributes(sDn).get("pmProperty");
			if (attr == null) {
				return null;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sProp = (String) enumer.next();
				if (sProp.startsWith(sPrefix)) {
					return sProp;
				}
			}
			return null;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// Returns:
	// item 0: <name>:<id>
	// item 1: <description>
	// item 2: <other info>
	// item 3 and following: <property>

	public Packet getPcInfo(String sSessId, String sPcId, String sIsVos) {
		Packet res = new Packet();

		if (sIsVos.equalsIgnoreCase("yes")) {
			sPcId = getAdminVosNodeOrigId(sPcId);
		}

		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sPcId + ","
					+ sPolicyContainerDN);
			String sPcName = (String) attrs.get("pmName").get();
			String sDescr = (String) attrs.get("pmDescription").get();
			String sInfo = (String) attrs.get("pmOtherInfo").get();
			res.addItem(ItemType.RESPONSE_TEXT, sPcName + GlobalConstants.PM_FIELD_DELIM
					+ sPcId);
			res.addItem(ItemType.RESPONSE_TEXT, sDescr);
			res.addItem(ItemType.RESPONSE_TEXT, sInfo);

			Attribute attr = attrs.get("pmProperty");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					res.addItem(ItemType.RESPONSE_TEXT, (String) enumer.next());
				}
			}
			return res;
		} catch (NameNotFoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("No such policy class!");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception during getPcInfo: "
					+ e.getMessage());
		}
	}

	// Returns:
	// item 0: <name>:<id>
	// item 1: <description>
	// item 2: <other info>
	// item 3 and following: <property>

	public Packet getAttrInfo(String sSessId, String sAttrId,
			String sAttrType, String sIsVos) {
		if (sIsVos.equalsIgnoreCase("yes")) {
			sAttrId = getAdminVosNodeOrigId(sAttrId);
		}

		Packet result = new Packet();

		String sDn;
		if (sAttrType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sDn = "CN=" + sAttrId + "," + sUserAttrContainerDN;
		} else if (sAttrType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sDn = "CN=" + sAttrId + "," + sObjAttrContainerDN;
		} else if (sAttrType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sDn = "CN=" + sAttrId + "," + sPolicyContainerDN;
		} else {
			return failurePacket("Invalid attribute type!");
		}

		try {
			Attributes attrs = ServerConfig.ctx.getAttributes(sDn);
			String sAttrName = (String) attrs.get("pmName").get();
			String sDescr = (String) attrs.get("pmDescription").get();
			String sInfo = (String) attrs.get("pmOtherInfo").get();
			result.addItem(ItemType.RESPONSE_TEXT, sAttrName + GlobalConstants.PM_FIELD_DELIM
					+ sAttrId);
			result.addItem(ItemType.RESPONSE_TEXT, sDescr);
			result.addItem(ItemType.RESPONSE_TEXT, sInfo);

			Attribute attr = attrs.get("pmProperty");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					result.addItem(ItemType.RESPONSE_TEXT,
							(String) enumer.next());
				}
			}
			return result;
		} catch (NameNotFoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("No such attribute!");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception during getAttrInfo: "
					+ e.getMessage());
		}
	}

	// Returns:
	// item 0: <name>:<id>
	// item 1: <description>
	// item 2: <other info>
	// item 3: <full name>
	// item 4 and following: <property>

	public Packet getUserInfo(String sSessId, String sUserId) {
		Packet result = new Packet();

		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sUserId + ","
					+ sUserContainerDN);
			String sUserName = (String) attrs.get("pmName").get();
			String sFullName = (String) attrs.get("pmFullName").get();
			String sInfo = (String) attrs.get("pmOtherInfo").get();
			result.addItem(ItemType.RESPONSE_TEXT, sUserName + GlobalConstants.PM_FIELD_DELIM
					+ sUserId);
			result.addItem(ItemType.RESPONSE_TEXT, sFullName);
			result.addItem(ItemType.RESPONSE_TEXT, sInfo);

			Attribute attr = attrs.get("pmProperty");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					result.addItem(ItemType.RESPONSE_TEXT,
							(String) enumer.next());
				}
			}
			return result;
		} catch (NameNotFoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("No such user!");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception during getUserInfo: "
					+ e.getMessage());
		}
	}


	public String getUserFullName(String sUserId) {
		System.out.println("USER ID: " + sUserId);
		try {
			System.out.println("USER ID: " + sUserId);
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sUserId + ","
					+ sUserContainerDN);
			System.out.println("Full name: " + (String) attrs.get("pmFullName").get());
			return (String) attrs.get("pmFullName").get();
		} catch (Exception e) {
			System.out.println("caught " + sUserId);
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}


	public Packet getScripts() throws Exception {
		Packet result = new Packet();
		NamingEnumeration<?> scripts;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			scripts = ServerConfig.ctx.search(sRuleContainerDN, "(objectClass="
					+ sScriptClass + ")", constraints);
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}

		while (scripts != null && scripts.hasMore()) {
			SearchResult sr = (SearchResult) scripts.next();
			String sName = (String) sr.getAttributes().get("pmName").get();
			String sId = (String) sr.getAttributes().get("pmId").get();
			result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		}
		return result;
	}

	// Enable the script specified by its id. Returns the enabled script name
	// and id,
	// or failure.

	public Packet enableScript(String sSessId, String sScriptId) {
		// Make sure the script exists.
		String sName = getEntityName(sScriptId, GlobalConstants.PM_SCRIPT);
		if (sName == null) {
			return failurePacket("Script does not exist!");
		}

		// Enable it.
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("pmIsEnabled", "TRUE"));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sScriptId + "," + sRuleContainerDN,
					mods);
			Packet res = new Packet();
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
					+ sScriptId);
			return res;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket(e.getMessage());
		}
	}


	public Packet disableEnabledScript(String sSessId) {
		String sScriptId = getEnabledScriptId();
		if (sScriptId == null) {
			return failurePacket("No script is enabled!");
		}

		// Disable it.
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("pmIsEnabled", "FALSE"));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sScriptId + "," + sRuleContainerDN,
					mods);
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket(e.getMessage());
		}
	}


	public Packet getEnabledScript() {
		Packet res = new Packet();

		String sId = getEnabledScriptId();
		if (sId == null) {
			return res;
		}

		String sName = getEntityName(sId, GlobalConstants.PM_SCRIPT);
		if (sName == null) {
			return failurePacket("Inconsistency: Enabled script does not exist!");
		}
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return res;
	}


	public String getEnabledScriptId() {
		NamingEnumeration<?> scripts;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			scripts = ServerConfig.ctx.search(sRuleContainerDN, "(&(objectClass="
					+ sScriptClass + ")(pmIsEnabled=TRUE))", constraints);
			if (scripts == null || !scripts.hasMore()) {
				return null;
			}
			SearchResult sr = (SearchResult) scripts.next();
			String sId = (String) sr.getAttributes().get("pmId").get();
			return sId;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}


	public Packet deleteScriptRule(String sScriptId, String sLabel) {
		if (sScriptId == null) {
			return failurePacket("The script id is null!");
		}
		String sName = getEntityName(sScriptId, GlobalConstants.PM_SCRIPT);
		if (sName == null) {
			return failurePacket("No such script!");
		}
		if (sLabel == null || sLabel.length() == 0) {
			return failurePacket("Null or empty rule label!");
		}

		try {
			// The script attributes.
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sScriptId + ","
					+ sRuleContainerDN);

			// Get the rule count, the first rule, and the last rule of the
			// script.
			int count = (Integer.valueOf((String) attrs.get("pmCount").get())).intValue();

			String sFirstRuleId = null;
			Attribute attr = attrs.get("pmFirst");
			if (attr != null) {
				sFirstRuleId = (String) attr.get();
			}

			String sLastRuleId = null;
			attr = attrs.get("pmLast");
			if (attr != null) {
				sLastRuleId = (String) attr.get();
			}

			// Walk sequentially through the script, looking for a rule with the
			// specified label.
			String sCrtRuleId = sFirstRuleId;
			Attributes ruleAttrs = null;
			while (sCrtRuleId != null) {
				// Get the crt rule label.
				ruleAttrs = ServerConfig.ctx.getAttributes("CN=" + sCrtRuleId + ","
						+ sRuleContainerDN);
				String sCrtRuleLabel = null;
				attr = ruleAttrs.get("pmLabel");
				if (attr != null) {
					sCrtRuleLabel = (String) attr.get();
				}

				if (sCrtRuleLabel.equalsIgnoreCase(sLabel)) {
					break;
				}

				attr = ruleAttrs.get("pmNext");
				if (attr == null) {
					sCrtRuleId = null;
				} else {
					sCrtRuleId = (String) attr.get();
				}
			}

			// If sCrtRuleId is null, we didn't find a rule with that label.
			if (sCrtRuleId == null) {
				return failurePacket("No rule with label \"" + sLabel + "\"!");
			}

			// We found a rule. Its attributes are ruleAttrs.
			// The rule may be the first, the last, or in the middle of the
			// script.
			if (sCrtRuleId.equalsIgnoreCase(sFirstRuleId)) {
				// The target rule is the first in script.
				// Find the next rule relative to the current rule.
				String sNextRuleId = null;
				attr = ruleAttrs.get("pmNext");
				if (attr != null) {
					sNextRuleId = (String) attr.get();
				}
				if (sNextRuleId == null) {
					// If this is the only rule in script then set the script
					// empty.
					ModificationItem[] mods = new ModificationItem[2];
					mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
							new BasicAttribute("pmFirst", sFirstRuleId));
					mods[1] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
							new BasicAttribute("pmLast", sLastRuleId));
					ServerConfig.ctx.modifyAttributes("CN=" + sScriptId + ","
							+ sRuleContainerDN, mods);
				} else {
					// Otherwise, set script.first to point to crt.next rule ...
					ModificationItem[] mods = new ModificationItem[1];
					mods[0] = new ModificationItem(
							DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
									"pmFirst", sNextRuleId));
					ServerConfig.ctx.modifyAttributes("CN=" + sScriptId + ","
							+ sRuleContainerDN, mods);

					// .. and set next rule's pmPrev pointer to null.
					mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
							new BasicAttribute("pmPrev", sCrtRuleId));
					ServerConfig.ctx.modifyAttributes("CN=" + sNextRuleId + ","
							+ sRuleContainerDN, mods);
				}
			} else if (sCrtRuleId.equalsIgnoreCase(sLastRuleId)) {
				// The target rule is the last in script.
				// Find the previous rule relative to the current rule.
				String sPrevRuleId = null;
				attr = ruleAttrs.get("pmPrev");
				if (attr != null) {
					sPrevRuleId = (String) attr.get();
				}
				if (sPrevRuleId == null) {
					// If the target rule is the only rule in script then set
					// the script empty.
					ModificationItem[] mods = new ModificationItem[2];
					mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
							new BasicAttribute("pmFirst", sFirstRuleId));
					mods[1] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
							new BasicAttribute("pmLast", sLastRuleId));
					ServerConfig.ctx.modifyAttributes("CN=" + sScriptId + ","
							+ sRuleContainerDN, mods);
				} else {
					// Otherwise, set script.last to point to crt.prev rule ...
					ModificationItem[] mods = new ModificationItem[1];
					mods[0] = new ModificationItem(
							DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(
									"pmLast", sPrevRuleId));
					ServerConfig.ctx.modifyAttributes("CN=" + sScriptId + ","
							+ sRuleContainerDN, mods);

					// .. and set prevous rule's pmNext pointer to null.
					mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
							new BasicAttribute("pmNext", sCrtRuleId));
					ServerConfig.ctx.modifyAttributes("CN=" + sPrevRuleId + ","
							+ sRuleContainerDN, mods);
				}
			} else {
				// The target rule is in the middle of the script. First get the
				// previous and next rules, then set their pmNext and pmPrev
				// pointers
				// correctly.
				String sNextRuleId = (String) ruleAttrs.get("pmNext").get();
				String sPrevRuleId = (String) ruleAttrs.get("pmPrev").get();

				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("pmNext", sNextRuleId));
				ServerConfig.ctx.modifyAttributes("CN=" + sPrevRuleId + ","
						+ sRuleContainerDN, mods);

				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("pmPrev", sPrevRuleId));
				ServerConfig.ctx.modifyAttributes("CN=" + sNextRuleId + ","
						+ sRuleContainerDN, mods);
			}

			// Now decrease the rank for all rules following the target rule.
			attr = ruleAttrs.get("pmNext");
			while (attr != null) {
				String sNextRuleId = (String) attr.get();
				// Increase the rank of each source rule.
				ruleAttrs = ServerConfig.ctx.getAttributes("CN=" + sNextRuleId + ","
						+ sRuleContainerDN);
				int rank = (Integer.valueOf((String) ruleAttrs.get("pmRank").get())).intValue() - 1;
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("pmRank", String.valueOf(rank)));
				ServerConfig.ctx.modifyAttributes("CN=" + sNextRuleId + ","
						+ sRuleContainerDN, mods);

				attr = ruleAttrs.get("pmNext");
			}

			// Update the rule count in the destination script.
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmCount", String.valueOf(count - 1)));
			ServerConfig.ctx.modifyAttributes("CN=" + sScriptId + "," + sRuleContainerDN,
					mods);

			// Delete the rule record.
			String sRes = deleteRule(sCrtRuleId);
			if (sRes != null) {
				return failurePacket(sRes);
			}
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception when deleting a rule from a script: "
					+ e.getMessage());
		}
	}

	// Compile and add to the enabled script, if one exists.
	// Otherwise, enable the new script. Returns the name and id of the
	// enabled script.

	public Packet compileScriptAndAddToEnabled(String sSessId, Packet cmdPacket) {
		// Create a temporary file.
		File ftemp = null;
		RuleParser ruleParser;
		String sResult;
		
		// The rules start at item 3 (0 = cmd code, 1 = sess id, 2 = filler).
		try {
			ftemp = File.createTempFile("evr", ".evr", null);
			FileOutputStream fos = new FileOutputStream(ftemp);
			PrintWriter pw = new PrintWriter(fos);
			for (int i = 3; i < cmdPacket.size(); i++) {
				String sLine = cmdPacket.getStringValue(i);
				if (sLine == null) {
					continue;
				}
				pw.println(sLine);
			}
			pw.close();
		

		// Create a new parser object and parse the script. Note that if parsing
		// is
		// successful, the script code is written in the active directory. An
		// object
		// of class pmClassScript with the script's id and name is created.
		// Other
		// scripts stored in the AD might have the same name.
		ruleParser = new RuleParser(ftemp);
		sResult = ruleParser.parse();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while writing the rules to a temporary file: "
					+ e.getMessage());
		}
		if (sResult != null) {
			return failurePacket(sResult);
		}

		// Compilation is OK.
		// If no script is enabled, delete the scripts with the same
		// name as the new script and enable the new script.
		Packet res;

		String sEnScriptId = getEnabledScriptId();
		String sEnScriptName = null;

		if (sEnScriptId != null) {
			sEnScriptName = getEntityName(sEnScriptId, GlobalConstants.PM_SCRIPT);
			if (sEnScriptName == null) {
				sEnScriptId = null;
			}
		}

		if (sEnScriptId == null) {
			res =  deleteScriptsWithNameExcept(sSessId,
					ruleParser.getScriptName(), ruleParser.getScriptId());
			if (res.hasError()) {
				return res;
			}
			sEnScriptId = ruleParser.getScriptId();

			res =  enableScript(sSessId, sEnScriptId);
			if (res.hasError()) {
				return res;
			}

			saveScriptSource(ruleParser.getScriptId(),
					ruleParser.getScriptName(), cmdPacket);

			return res;
		}

		// An enabled script exists. Add the new script to it.
		res =  addScript(sSessId, ruleParser.getScriptId(), sEnScriptId);
		if (res.hasError()) {
			return res;
		}

		// Now add the source for the compiled script to the end of the old
		// source.
		addScriptSource(sEnScriptId, sEnScriptName, cmdPacket);

		res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT,
					getEntityName(sEnScriptId, GlobalConstants.PM_SCRIPT) + GlobalConstants.PM_FIELD_DELIM
					+ sEnScriptId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return res;
	}

	// Compile and enable the script (deleting all other scripts with the same
	// name).
	// The script starts at item 3 in the packet.

	public Packet compileScriptAndEnable(String sSessId, Packet cmdPacket) {
		// Create a temporary file.
		File ftemp = null;
		RuleParser ruleParser;
		String sResult;
		
		// The rules start at item 3 (0 = cmd code, 1 = sess id, 2 = filler).
		try {
			ftemp = File.createTempFile("evr", ".evr", null);
			FileOutputStream fos = new FileOutputStream(ftemp);
			PrintWriter pw = new PrintWriter(fos);
			for (int i = 3; i < cmdPacket.size(); i++) {
				String sLine = cmdPacket.getStringValue(i);
				if (sLine == null) {
					continue;
				}
				pw.println(sLine);
			}
			pw.close();
		

		// Create a new parser object and parse the script. Note that if parsing
		// is
		// successful, the script code is written in the active directory. An
		// object
		// of class pmClassScript with the script's id and name is created.
		// Other
		// scripts stored in the AD might have the same name.
		ruleParser = new RuleParser(ftemp);
		sResult = ruleParser.parse();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while writing the rules to a temporary file: "
					+ e.getMessage());
		}
		// Delete the other scripts with the same name.
		if (sResult == null) {
			Packet res =  deleteScriptsWithNameExcept(sSessId,
					ruleParser.getScriptName(), ruleParser.getScriptId());
			if (res.hasError()) {
				return res;
			}

			// Enable the new script.
			String sEnScriptId = ruleParser.getScriptId();
			res =  enableScript(sSessId, sEnScriptId);
			if (res.hasError()) {
				return res;
			}
			saveScriptSource(ruleParser.getScriptId(),
					ruleParser.getScriptName(), cmdPacket);
			return res;
		}
		return failurePacket(sResult);
	}

	// Let scr be the script contained in the command.
	//
	// compile scr;
	// if (compilation fails) return failurePacket("Compilation failed!");
	//
	// if (! synonymExists(scr)) {
	// save scr;
	// return scr's name and id;
	// }
	//
	// s = synonym(scr);
	//
	// if (sDelOthers != "yes") {
	// delete scr;
	// return failurePacket("Synonym script exists!");
	// }
	//
	// if (s is enabled) {
	// delete scr;
	// return
	// failurePacket("another script with the same name exists and is enabled");
	// }
	//
	// delete s;
	// save scr;
	// return scr's name and id;

	public Packet compileScript(String sSessId, String sDeleteOthers,
			Packet cmd) {
		// Create a temporary file.
		File ftemp = null;
		RuleParser ruleParser;
		String sResult;
		
		// The rules start at item 3 (0 = cmd code, 1 = sess id, 2 =
		// sDeleteOthers).
		// Some items could be empty, the String used to extract them would be
		// null,
		// skip them.
		try {
			ftemp = File.createTempFile("evr", ".evr", null);
			FileOutputStream fos = new FileOutputStream(ftemp);
			PrintWriter pw = new PrintWriter(fos);

			// Copy the lines to the temp file.
			for (int i = 3; i < cmd.size(); i++) {
				String sLine = cmd.getStringValue(i);
				if (sLine == null) {
					continue;
				}
				pw.println(sLine);
			}
			pw.close();
		

		// Create a new parser object and parse the script. Note that if parsing
		// is
		// successful, the script code is written in the active directory. An
		// object
		// of class pmClassScript with the script's id and name is created.
		// Other
		// scripts stored in the AD might have the same name.
		ruleParser = new RuleParser(ftemp);
		sResult = ruleParser.parse();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while writing the rules to a temporary file: "
					+ e.getMessage());
		}
		// If the compilation failed, return failure.
		if (sResult != null) {
			System.out.println("Error during compilation:" + sResult);
			return failurePacket(sResult);
		} else {
			System.out.println("Successful compilation of script "
					+ ruleParser.getScriptName());
		}

		// Compilation was successful.
		// If there is no other script with the same name, return the new
		// script's name and id.
		Packet res = new Packet();
		if (!synonymScriptsExist(ruleParser.getScriptName())) {
			System.out.println("No other scripts with the same name exist!");
			try {
				res.addItem(ItemType.RESPONSE_TEXT, ruleParser.getScriptName()
						+ GlobalConstants.PM_FIELD_DELIM + ruleParser.getScriptId());
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Exception when building the result packet");
			}

			System.out.println("Trying to save script source!");
			saveScriptSource(ruleParser.getScriptId(),
					ruleParser.getScriptName(), cmd);
			System.out.println("Saved script source!");

			return res;
		}

		// There is a script with the same name.
		// If sDeleteOthers is not "yes", delete the new script and return
		// failure.
		if (!sDeleteOthers.equalsIgnoreCase("yes")) {
			System.out.println("I am at 13145 pmEngin");
			sResult = deleteScriptInternal(ruleParser.getScriptId());
			if (sResult == null) {
				return failurePacket("A script with the same name exists. The new script was deleted!");
			} else {
				return failurePacket("A script with the same name exists. Error while deleting the new script!");
			}
		}

		// There is a script with the same name, and sDeleteOthers is "yes".
		// If the synonym script is enabled, we delete the new script.
		String sEnScriptId = getEnabledScriptId();
		if (sEnScriptId != null) {
			String sEnName = getEntityName(sEnScriptId, GlobalConstants.PM_SCRIPT);
			if (sEnName == null) {
				return failurePacket("Inconsistency: the enabled script "
						+ sEnScriptId + " does not exist!");
			}
			if (sEnName.equalsIgnoreCase(ruleParser.getScriptName())) {
				// Delete the script just compiled and return failure.
				System.out.println("13164 in PmEngin");
				sResult = deleteScriptInternal(ruleParser.getScriptId());
				if (sResult == null) {
					return failurePacket("A script with the same name exists and is enabled. The new script was deleted!");
				}
				return failurePacket("A script with the same name exists and is enabled. Error while deleting the new script!");
			}
		}

		// The synonyn is not enabled. Delete it.
		res =  deleteScriptsWithNameExcept(sSessId,
				ruleParser.getScriptName(), ruleParser.getScriptId());
		if (res.hasError()) {
			return failurePacket("A script with the same name exists. Error while deleting it!");
		}
		res = new Packet();

		try {
			res.addItem(ItemType.RESPONSE_TEXT, ruleParser.getScriptName()
					+ GlobalConstants.PM_FIELD_DELIM + ruleParser.getScriptId());
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}

		saveScriptSource(ruleParser.getScriptId(), ruleParser.getScriptName(),
				cmd);

		return res;
	}

	// Add a source script to the end of the source of a compiled script.
	// Parameters:
	// sScriptId and sScriptName: the id and name of the compiled script.
	// cmd: a Packet that contains the source script starting at item 3.

	public Packet addScriptSource(String sScriptId, String sScriptName,
			Packet cmd) {
		Attributes scriptAttrs;
		Attribute attr;
		String sSrcScriptId;

		try {
			// First get the source script of the compiled script.
			scriptAttrs = ServerConfig.ctx.getAttributes("CN=" + sScriptId + ","
					+ sRuleContainerDN);
			attr = scriptAttrs.get("pmSourceId");
			if (attr == null) {
				return failurePacket("No source for the script " + sScriptName);
			}
			sSrcScriptId = (String) attr.get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while extracting the attributes of script "
					+ sScriptName);
		}

		// For each line of the source to be added, create a source line object
		// and link it to the source script.
		for (int i = 3, n = 0; i < cmd.size(); i++) {
			String sLine = cmd.getStringValue(i);
			addLineToSourceScript(++n, sLine, sSrcScriptId);
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Save a source script in the AD and set it as the source script of a
	// compiled
	// script.
	// Parameters:
	// sScriptId and sScriptName: the id and name of the compiled script.
	// cmd: a Packet that contains the source script starting at item 3.

	public Packet saveScriptSource(String sScriptId, String sScriptName,
			Packet cmd) {
		// Generate the source script id and prepare the attributes.
		RandomGUID myGUID = new RandomGUID();
		String sSourceScriptId = myGUID.toStringNoDashes();
		Attributes attrs = new BasicAttributes(true);
		attrs.put("objectClass", sScriptSourceClass);
		attrs.put("pmId", sSourceScriptId);
		attrs.put("pmName", sScriptName);
		// The source script must point to the compiled script with its
		// pmScriptId.
		attrs.put("pmScriptId", sScriptId);

		// Prepare the path and create the new object.
		String sDn = "CN=" + sSourceScriptId + "," + sRuleContainerDN;
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not create source script object "
					+ sScriptName);
		}

		// The compiled script must point to the source script with its
		// pmSourceId.
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("pmSourceId", sSourceScriptId));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sScriptId + "," + sRuleContainerDN,
					mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not link the compiled script to the source script!");
		}

		// For each line create a source line object and link it in a list.
		for (int i = 3, n = 0; i < cmd.size(); i++) {
			String sLine = cmd.getStringValue(i);
			addLineToSourceScript(++n, sLine, sSourceScriptId);
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet addLineToSourceScript(int n, String sLine,
			String sSourceScriptId) {
		try {
			System.out.println("Adding line " + n + ":<" + sLine + ">");

			// Extract previous line id from pmLast of the source script object.
			// Get script's attributes.
			Attributes scriptAttrs = ServerConfig.ctx.getAttributes("CN=" + sSourceScriptId
					+ "," + sRuleContainerDN);
			String sPrevLineId = null;
			Attribute attr = scriptAttrs.get("pmLast");
			if (attr != null) {
				sPrevLineId = (String) attr.get();
			}

			// Create a new source line object.
			RandomGUID myGUID = new RandomGUID();
			String sCrtLineId = myGUID.toStringNoDashes();
			Attributes basicAttrs = new BasicAttributes(true);
			basicAttrs.put("objectClass", sSourceLineClass);
			basicAttrs.put("pmId", sCrtLineId);
			if (sLine != null && sLine.length() > 0) {
				basicAttrs.put("pmText", sLine);
			}
			if (sPrevLineId != null) {
				basicAttrs.put("pmPrev", sPrevLineId);
			}
			ServerConfig.ctx.bind("CN=" + sCrtLineId + "," + sRuleContainerDN, null,
					basicAttrs);

			// Insert the new object in the linked list.
			ModificationItem[] mods = new ModificationItem[1];
			if (sPrevLineId == null) {
				// This is the first line, the script must point to it with
				// pmFirst.
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmFirst", sCrtLineId));
				ServerConfig.ctx.modifyAttributes("CN=" + sSourceScriptId + ","
						+ sRuleContainerDN, mods);
			} else {
				// This is not the first line, the previous line must point to
				// it with
				// pmNext.
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmNext", sCrtLineId));
				ServerConfig.ctx.modifyAttributes("CN=" + sPrevLineId + ","
						+ sRuleContainerDN, mods);
			}

			// pmLast of the script must point to this new line.
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmLast", sCrtLineId));
			ServerConfig.ctx.modifyAttributes("CN=" + sSourceScriptId + ","
					+ sRuleContainerDN, mods);

			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Error while saving source line " + n);
		}
	}


	public boolean synonymScriptsExist(String sScriptName) {
		int howmany = 0;
		NamingEnumeration<?> scripts;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			scripts = ServerConfig.ctx.search(sRuleContainerDN, "(&(objectClass="
					+ sScriptClass + ")(pmName=" + sScriptName + "))",
					constraints);
			while (scripts != null && scripts.hasMore()) {
				SearchResult sr = (SearchResult) scripts.next();
				String sName = (String) sr.getAttributes().get("pmName").get();
				if (sName.equals(sScriptName)) {
					howmany++;
				}
				if (howmany >= 2) {
					return true;
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return false;
	}

	// Add the rules of the source script to the end of the destination script.
	// Both scripts must exist. Take into account the case when the destination
	// script exists but is empty (pmFirst and pmLast are not set).
	// The source script is destroyed after the operation.
	// Return success or failure.

	public Packet addScript(String sSessId, String sSrcScriptId,
			String sDstScriptId) {
		// If the source script does not exist, return failure.
		if (sSrcScriptId == null) {
			return failurePacket("Source script id is null!");
		}
		String sName = getEntityName(sSrcScriptId, GlobalConstants.PM_SCRIPT);
		if (sName == null) {
			return failurePacket("Source script does not exist!");
		}

		// If the destination script does not exist, return failure.
		if (sDstScriptId == null) {
			return failurePacket("Destination script id is null!");
		}
		sName = getEntityName(sDstScriptId, GlobalConstants.PM_SCRIPT);
		if (sName == null) {
			return failurePacket("Destination script does not exist!");
		}

		try {
			// The target and destination script attributes.
			Attributes dstAttrs = ServerConfig.ctx.getAttributes("CN=" + sDstScriptId + ","
					+ sRuleContainerDN);
			Attributes srcAttrs = ServerConfig.ctx.getAttributes("CN=" + sSrcScriptId + ","
					+ sRuleContainerDN);

			// Get the rule count, the first rule, and the last rule of the
			// source script.
			int srcCount = (Integer.valueOf((String) srcAttrs.get("pmCount").get())).intValue();
			String sSrcFirstRuleId = null;
			Attribute attr = srcAttrs.get("pmFirst");
			if (attr != null) {
				sSrcFirstRuleId = (String) attr.get();
			}
			String sSrcLastRuleId = null;
			attr = srcAttrs.get("pmLast");
			if (attr != null) {
				sSrcLastRuleId = (String) attr.get();
			}

			// If the source script is empty, return success.
			if (sSrcFirstRuleId == null) {
				// Delete the source script record.
				ServerConfig.ctx.destroySubcontext("CN=" + sSrcScriptId + ","
						+ sRuleContainerDN);
				return ADPacketHandler.getSuccessPacket();
			}

			// Get the rule count and the last rule of the destination script.
			int dstCount = (Integer.valueOf((String) dstAttrs.get("pmCount").get())).intValue();
			String sDstLastRuleId = null;
			attr = dstAttrs.get("pmLast");
			if (attr != null) {
				sDstLastRuleId = (String) attr.get();
			}

			// If the last rule of the destination does not exist
			// (sDstLastRuleId is
			// null), that means the destination script is empty. To add the
			// source
			// we need to copy the source first rule, last rule, and count to
			// the
			// destination.
			if (sDstLastRuleId == null) {
				ModificationItem[] mods = new ModificationItem[3];
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmFirst", sSrcFirstRuleId));
				mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmLast", sSrcLastRuleId));
				mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("pmCount", String.valueOf(srcCount)));
				ServerConfig.ctx.modifyAttributes("CN=" + sDstScriptId + ","
						+ sRuleContainerDN, mods);
				// Delete the source script record.
				ServerConfig.ctx.destroySubcontext("CN=" + sSrcScriptId + ","
						+ sRuleContainerDN);
				return ADPacketHandler.getSuccessPacket();
			}

			// Set pmNext in the last destination rule to the first source rule.
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmNext", sSrcFirstRuleId));
			ServerConfig.ctx.modifyAttributes("CN=" + sDstLastRuleId + ","
					+ sRuleContainerDN, mods);

			// Set the pmPrev in the first source rule to the last destination
			// rule.
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmPrev", sDstLastRuleId));
			ServerConfig.ctx.modifyAttributes("CN=" + sSrcFirstRuleId + ","
					+ sRuleContainerDN, mods);

			// Loop over source rules.
			String sLastSrcRuleId = null;// last processed source rule (not the
			// last source rule).
			String sCrtSrcRuleId = sSrcFirstRuleId;
			while (sCrtSrcRuleId != null) {
				// Increase the rank of each source rule.
				Attributes ruleAttrs = ServerConfig.ctx.getAttributes("CN=" + sCrtSrcRuleId
						+ "," + sRuleContainerDN);
				int rank = (Integer.valueOf((String) ruleAttrs.get("pmRank").get())).intValue() + dstCount;
				mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("pmRank", String.valueOf(rank)));
				ServerConfig.ctx.modifyAttributes("CN=" + sCrtSrcRuleId + ","
						+ sRuleContainerDN, mods);

				// Save the rule just processed, so we get the last source rule
				// after the loop.
				sLastSrcRuleId = sCrtSrcRuleId;

				// Next source rule.
				attr = ruleAttrs.get("pmNext");
				if (attr == null) {
					sCrtSrcRuleId = null;
				} else {
					sCrtSrcRuleId = (String) attr.get();
				}
			}

			// Update the rule count in the destination script.
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmCount", String.valueOf(dstCount
							+ srcCount)));
			ServerConfig.ctx.modifyAttributes("CN=" + sDstScriptId + "," + sRuleContainerDN,
					mods);

			// Update the last rule id in the destination script.
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmLast", sLastSrcRuleId));
			ServerConfig.ctx.modifyAttributes("CN=" + sDstScriptId + "," + sRuleContainerDN,
					mods);

			// Delete the source script record.
			ServerConfig.ctx.destroySubcontext("CN=" + sSrcScriptId + "," + sRuleContainerDN);

			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception when adding scripts: "
					+ e.getMessage());
		}
	}

	// Add the rules of script sScriptId to the end of the enabled script.
	// If no enabled script exists, the script sScriptId is enabled.
	// Return success or failure.

	public Packet addScriptToEnabled(String sSessId, String sScriptId) {
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet getSourceScript(String sSessId, String sScriptId) {
		String sName = getEntityName(sScriptId, GlobalConstants.PM_SCRIPT);
		if (sName == null) {
			return failurePacket("Script does not exist!");
		}
		Packet script = new Packet();

		try {
			Attributes scriptAttrs = ServerConfig.ctx.getAttributes("CN=" + sScriptId + ","
					+ sRuleContainerDN);
			Attribute attr = scriptAttrs.get("pmSourceId");
			if (attr == null) {
				return failurePacket("No source for script " + sName
						+ " with id " + sScriptId);
			}
			String sSourceScriptId = (String) attr.get();

			Attributes sourceAttrs = ServerConfig.ctx.getAttributes("CN=" + sSourceScriptId
					+ "," + sRuleContainerDN);
			attr = sourceAttrs.get("pmFirst");
			while (attr != null) {
				String sSourceLineId = (String) attr.get();
				sourceAttrs = ServerConfig.ctx.getAttributes("CN=" + sSourceLineId + ","
						+ sRuleContainerDN);
				String sLine = "";
				attr = sourceAttrs.get("pmText");
				if (attr != null) {
					sLine = (String) attr.get();
				}
				script.addItem(ItemType.RESPONSE_TEXT, sLine);
				attr = sourceAttrs.get("pmNext");
			}
			return script;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Error while extracting source script!");
		}
	}

	// Delete the script specified by its id. The enabled script cannot be
	// deleted.

	public Packet deleteScript(String sSessId, String sScriptId) {
		// If the script to be deleted is the enabled script, return failure.
		String sEnScriptId = getEnabledScriptId();
		if (sEnScriptId != null && sEnScriptId.equalsIgnoreCase(sScriptId)) {
			return failurePacket("The enabled script cannot be deleted!");
		}
		System.out.println("i am at 13582 pmEngin");
		String res = deleteScriptInternal(sScriptId);
		if (res == null) {
			return ADPacketHandler.getSuccessPacket();
		} else {
			return failurePacket(res);
		}
	}


	public String deleteScriptInternal(String sScriptId) {
		System.out.println(">>>>>>>>>>>>Delete Script " + sScriptId);
		try {
			Attributes scriptAttrs = ServerConfig.ctx.getAttributes("CN=" + sScriptId + ","
					+ sRuleContainerDN);

			Attribute attr = scriptAttrs.get("pmSourceId");
			if (attr != null) {
				String sSourceScriptId = (String) attr.get();
				deleteSourceScript(sSourceScriptId);
			}

			attr = scriptAttrs.get("pmFirst");
			while (attr != null) {
				String sRuleId = (String) attr.get();
				Attributes ruleAttrs = ServerConfig.ctx.getAttributes("CN=" + sRuleId + ","
						+ sRuleContainerDN);
				attr = ruleAttrs.get("pmNext");
				String result = deleteRule(sRuleId);
				if (result != null) {
					return result;
				}
			}

			System.out.println("^^^^^^^Deleting script " + sScriptId);
			ServerConfig.ctx.destroySubcontext("CN=" + sScriptId + "," + sRuleContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception while deleting the EVER script " + sScriptId;
		}
		return null;
	}


	public String deleteSourceScript(String sSourceScriptId) {
		try {
			Attributes scriptAttrs = ServerConfig.ctx.getAttributes("CN=" + sSourceScriptId
					+ "," + sRuleContainerDN);
			Attribute attr = scriptAttrs.get("pmFirst");
			while (attr != null) {
				String sSourceLineId = (String) attr.get();
				Attributes lineAttrs = ServerConfig.ctx.getAttributes("CN=" + sSourceLineId
						+ "," + sRuleContainerDN);
				attr = lineAttrs.get("pmNext");
				ServerConfig.ctx.destroySubcontext("CN=" + sSourceLineId + ","
						+ sRuleContainerDN);
			}
			ServerConfig.ctx.destroySubcontext("CN=" + sSourceScriptId + ","
					+ sRuleContainerDN);
			return null;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception while deleting the source script "
			+ sSourceScriptId;
		}
	}


	public String deleteRule(String sRuleId) {
		System.out.println(">>>>>>>>>>>>Delete Rule " + sRuleId);
		String result = null;
		try {
			Attributes ruleAttrs = ServerConfig.ctx.getAttributes("CN=" + sRuleId + ","
					+ sRuleContainerDN);
			Attribute attr = ruleAttrs.get("pmEventPattern");
			if (attr != null) {
				String sPatternId = (String) attr.get();
				result = deletePattern(sPatternId);
				if (result != null) {
					return result;
				}
			}

			attr = ruleAttrs.get("pmAction");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sActionId = (String) enumer.next();
					result = deleteAction(sActionId);
					if (result != null) {
						return result;
					}
				}
			}
			System.out.println("^^^^^^^Deleting rule " + sRuleId);
			ServerConfig.ctx.destroySubcontext("CN=" + sRuleId + "," + sRuleContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception while deleting the rule " + sRuleId;
		}
		return null;
	}


	public String deletePattern(String sPatternId) {
		System.out.println(">>>>>>>>>>>>Delete Pattern " + sPatternId);
		System.out.println("^^^^^^^Deleting pattern " + sPatternId);
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sPatternId + "," + sRuleContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception while deleting the pattern " + sPatternId;
		}
		return null;
	}


	public String deleteAction(String sActionId) {
		System.out.println(">>>>>>>>>>>>Delete Action " + sActionId);
		String result = null;
		try {
			Attributes actionAttrs = ServerConfig.ctx.getAttributes("CN=" + sActionId + ","
					+ sRuleContainerDN);
			Attribute attr = actionAttrs.get("pmCondition");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sCondId = (String) enumer.next();
					result = deleteCondition(sCondId);
					if (result != null) {
						return result;
					}
				}
			}

			attr = actionAttrs.get("pmOpnd1");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpndId = (String) enumer.next();
					result = deleteOperand(sOpndId);
					if (result != null) {
						return result;
					}
				}
			}

			attr = actionAttrs.get("pmOpnd2");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpndId = (String) enumer.next();
					result = deleteOperand(sOpndId);
					if (result != null) {
						return result;
					}
				}
			}

			attr = actionAttrs.get("pmOpnd3");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpndId = (String) enumer.next();
					result = deleteOperand(sOpndId);
					if (result != null) {
						return result;
					}
				}
			}

			attr = actionAttrs.get("pmOpnd4");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpndId = (String) enumer.next();
					result = deleteOperand(sOpndId);
					if (result != null) {
						return result;
					}
				}
			}

			System.out.println("^^^^^^^Deleting action " + sActionId);
			ServerConfig.ctx.destroySubcontext("CN=" + sActionId + "," + sRuleContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception while deleting the action " + sActionId;
		}
		return null;
	}


	public String deleteCondition(String sCondId) {
		System.out.println(">>>>>>>>>>>>Delete Condition " + sCondId);
		String result = null;
		try {
			Attributes actionAttrs = ServerConfig.ctx.getAttributes("CN=" + sCondId + ","
					+ sRuleContainerDN);
			Attribute attr = actionAttrs.get("pmOpnd1");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sOpndId = (String) enumer.next();
					result = deleteOperand(sOpndId);
					if (result != null) {
						return result;
					}
				}
			}
			ServerConfig.ctx.destroySubcontext("CN=" + sCondId + "," + sRuleContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception while deleting the condition " + sCondId;
		}
		return null;
	}


	public String deleteOperand(String sOpndId) {
		System.out.println(">>>>>>>>>>>>Delete Operand " + sOpndId);
		String result = null;
		try {
			Attributes opndAttrs = ServerConfig.ctx.getAttributes("CN=" + sOpndId + ","
					+ sRuleContainerDN);
			Attribute attr = opndAttrs.get("pmArgs");
			if (attr != null) {
				String sArgs = (String) attr.get();
				String[] pieces = sArgs.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				for (int i = 0; i < pieces.length; i++) {
					result = deleteOperand(pieces[i]);
					if (result != null) {
						return result;
					}
				}
			}
			System.out.println("^^^^^^^Deleting operand " + sOpndId);
			ServerConfig.ctx.destroySubcontext("CN=" + sOpndId + "," + sRuleContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception while deleting the operand " + sOpndId;
		}
		return null;
	}

	// Delete all scripts of the specified name, except the one with the
	// specified id.

	public Packet deleteScriptsWithNameExcept(String sSessId,
			String sScriptName, String sScriptId) {
		// Find all script records with the specified name.
		NamingEnumeration<?> scripts;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			scripts = ServerConfig.ctx.search(sRuleContainerDN, "(&(objectClass="
					+ sScriptClass + ")(pmName=" + sScriptName + "))",
					constraints);
			while (scripts != null && scripts.hasMore()) {
				SearchResult sr = (SearchResult) scripts.next();
				Attribute attr = sr.getAttributes().get("pmId");
				if (attr != null) {
					String sId = (String) attr.get();
					if (!sId.equalsIgnoreCase(sScriptId)) {
						System.out.println("I am at 13846 in pmEngin");
						String sRes = deleteScriptInternal(sId);
						if (sRes != null) {
							return failurePacket(sRes);
						}
						continue;
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// This is called from the parser at the start of a new compilation.
	// It creates a script record with the specified name and id.

	public String createScriptRecord(String scriptId, String scriptName) {
		Attributes attrs = new BasicAttributes(true);
        System.out.println("scriptId = " + scriptId);
		attrs.put("objectClass", sScriptClass);
		attrs.put("pmId", scriptId);
		attrs.put("pmName", scriptName);
		attrs.put("pmIsEnabled", "FALSE");
		attrs.put("pmCount", String.valueOf(0));
		String sScriptDn = "CN=" + scriptId + "," + sRuleContainerDN;
		try {
			ServerConfig.ctx.bind(sScriptDn, null, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to create AD Script object!";
		}
		return null;
	}

	// Called from the parser for one rule if parsing is OK for that rule.
	// Generate the code for the rule.

	public String generateRuleCode(RuleSpec ruleSpec, String scriptId,
			String prevRuleId) {
		String result = checkRuleSemantics(ruleSpec);
		if (result != null) {
			return result;
		}

		result = writeRuleCode(ruleSpec);
		if (result != null) {
			return result;
		}

		return addRuleToScript(ruleSpec, scriptId, prevRuleId);
	}

	// If this is the first rule (prevRuleId = null), add the rule to the script
	// record as the first rule.
	// Otherwise, set the double link between previous rule and this rule.
	// Increase the rule count in the script record.

	public String addRuleToScript(RuleSpec ruleSpec, String scriptId,
			String prevRuleId) {
        System.out.println("script id: " + scriptId);
		String crtRuleId = ruleSpec.getId();
		ModificationItem[] mods = new ModificationItem[1];
		if (prevRuleId == null) {
			// This is the first rule.
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmFirst", crtRuleId));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + scriptId + "," + sRuleContainerDN,
						mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add rule id as the script's first rule!";
			}
		} else {
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmNext", crtRuleId));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + prevRuleId + ","
						+ sRuleContainerDN, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to set the link previous rule ---> this rule!";
			}
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmPrev", prevRuleId));
			try {
				ServerConfig.ctx.modifyAttributes(
						"CN=" + crtRuleId + "," + sRuleContainerDN, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to set the link this rule ---> previous rule!";
			}
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + scriptId + ","
					+ sRuleContainerDN);
			int count = (Integer.valueOf((String) attrs.get("pmCount").get())).intValue() + 1;
			mods = new ModificationItem[2];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmCount", String.valueOf(count)));
			mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmLast", crtRuleId));
			ServerConfig.ctx.modifyAttributes("CN=" + scriptId + "," + sRuleContainerDN,
					mods);
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to update the rule count or the last rule id in the script record!";
		}
		return null;
	}


	public String writeRuleCode(RuleSpec ruleSpec) {
		String result = null;

		// Get the pattern spec and the action specs.
		Vector<ActionSpec> actSpecs = ruleSpec.getActions();

		result = writeRuleRecord(ruleSpec);
		if (result != null) {
			return result;
		}

		result = writePatternRecord(ruleSpec.getPattern());
		if (result != null) {
			return result;
		}

		// Write the actions to AD.
		traceTitle("RESPONSE ACTIONS");
		if (actSpecs.isEmpty()) {
			return null;
		}

		String sPrevActId = null;

		for (int nActRank = 0; nActRank < actSpecs.size(); nActRank++) {
			ActionSpec actSpec = actSpecs.get(nActRank);

			// First create the AD record, links, etc.
			result = createActionRecord(actSpec, sPrevActId, nActRank);

			// Write the condition record.
			result = writeActionCondRecord(actSpec);
			if (result != null) {
				return result;
			}

			// Then fill in the record.
			String type = actSpec.getType();
			if (type.startsWith(RuleScanner.PM_VALUE_ASSIGN)) {
				result = writeAssignActionRecord(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_GRANT)) {
				result = writeGrantActionRecord(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_CREATE)) {
				result = writeCreateActionRecord(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_DENY)) {
				result = writeDenyActionRecord(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
					+ RuleScanner.PM_VALUE_ASSIGNMENT)) {
				result = writeDeassignActionRecord(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
					+ RuleScanner.PM_VALUE_DENY)) {
				result = writeDeleteDenyActionRecord(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
					+ RuleScanner.PM_VALUE_RULE)) {
				result = writeDeleteRuleActionRecord(actSpec);
				if (result != null) {
					return result;
				}
			}
			sPrevActId = actSpec.getId();
		}

		return null;
	}


	public String writePatternRecord(PatternSpec patSpec) {
		ModificationItem[] mods = new ModificationItem[1];
		Attributes attrs = new BasicAttributes(true);

		String sPatternId = patSpec.getId();
		attrs.put("objectClass", sEventPatternClass);
		attrs.put("pmId", sPatternId);
		attrs.put("pmIsAny", (patSpec.isAny() ? "TRUE" : "FALSE"));
		attrs.put("pmIsActive", (patSpec.isActive() ? "TRUE" : "FALSE"));
		String sPatternDn = "CN=" + sPatternId + "," + sRuleContainerDN;
		try {
			ServerConfig.ctx.bind(sPatternDn, null, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to create AD Event Pattern object!";
		}
		traceTitle("EVENT PATTERN id = " + patSpec.getId());
		traceLine("  isAny = " + patSpec.isAny());
		traceLine("  isActive = " + patSpec.isActive());

		// Write the user specs:
		traceTitle("Event users");
		Set<UserSpec> UserSet = patSpec.getUserSpecs();
		if (UserSet.isEmpty()) {
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmUserSpec", "*"));
			try {
				ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add a user specification to the event pattern object!";
			}
			traceLine("  userSpec(\"*\", \"" + PM_NODE.USER.value + "\")");
		} else {
			Iterator<UserSpec> hsiter;
			hsiter = UserSet.iterator();
			while (hsiter.hasNext()) {
				UserSpec spec = hsiter.next();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmUserSpec", spec.getType()
								+ GlobalConstants.PM_ALT_FIELD_DELIM + spec.getId()));
				try {
					ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add a user specification to the event pattern object!";
				}
				traceLine("  userSpec(name = \"" + spec.getName() + "\", "
						+ "id = \"" + spec.getId() + "\", " + "type = \""
						+ spec.getType() + "\")");
			}
		}
		// Write the policy specs.
		traceTitle("Event policy classes");
		Set<PcSpec> pcSet = patSpec.getPcSpecs();
		if (pcSet.isEmpty()) {
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmPolicySpec", "*"));
			try {
				ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add a policy specification to the event pattern object!";
			}
			traceLine("  policySpec(\"*\", \"" + PM_NODE.POL.value + "\")");
		} else {
			Iterator<PcSpec> hsiter = pcSet.iterator();
			while (hsiter.hasNext()) {
				PcSpec spec = hsiter.next();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmPolicySpec", spec.getType()
								+ GlobalConstants.PM_ALT_FIELD_DELIM + spec.getId()));
				try {
					ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add a policy specification to the event pattern object!";
				}
				traceLine("  pcSpec(name = \"" + spec.getName() + "\", "
						+ "id = \"" + spec.getId() + "\", " + "type = \""
						+ spec.getType() + "\")");
			}
		}

		// Write the op specs.
		traceTitle("Event operations");
		Set<OpSpec> opSet = patSpec.getOpSpecs();
		if (opSet.isEmpty()) {
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmOpSpec", "*"));
			try {
				ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add an operation specification to the event pattern object!";
			}
			traceLine("  opSpec(\"*\", \"" + GlobalConstants.PM_OP + "\")");
		} else {
			Iterator<OpSpec> hsiter = opSet.iterator();
			while (hsiter.hasNext()) {
				OpSpec spec = hsiter.next();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpSpec", spec.getName()));
				try {
					ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operation specification to the event pattern object!";
				}
				traceLine("  opSpec(\"" + spec.getName() + "\", \""
						+ spec.getType() + "\")");
			}
		}

		// Write the obj specs. There is only one. If multiple objects are
		// desired,
		// use their associated object attributes as containers.
		traceTitle("Event objects");
		Set<ObjSpec> hs = patSpec.getObjSpecs();
		if (hs.isEmpty()) {
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmObjSpec", "*"));
			try {
				ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add an object/attribute/class specification to the event pattern object!";
			}
			traceLine("  objSpec(\"*\", \"" + GlobalConstants.PM_OBJ + "\")");
		} else {
			Iterator<ObjSpec> hsiter = hs.iterator();
			while (hsiter.hasNext()) {
				ObjSpec spec = hsiter.next();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmObjSpec", spec.getType()
								+ GlobalConstants.PM_ALT_FIELD_DELIM + spec.getName()));
				try {
					ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an object specification to the event pattern object!";
				}
				traceLine("  objSpec(name = \"" + spec.getName() + "\", "
						+ "id = \"" + spec.getId() + "\", " + "type = \""
						+ spec.getType() + "\")");
			}
		}

		// Write the container specs.
		traceTitle("Event object containers");
		Set<ContSpec> contSet = patSpec.getContSpecs();
		if (contSet.isEmpty()) {
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmContSpec", "*"));
			try {
				ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add an object container (attribute or class) specification to the event pattern object!";
			}
			traceLine("  contSpec(\"*\", \"" + GlobalConstants.PM_OBJ + "\")");
		} else {
			Iterator<ContSpec> hsiter = contSet.iterator();
			while (hsiter.hasNext()) {
				ContSpec spec = hsiter.next();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmContSpec", spec.getType()
								+ GlobalConstants.PM_ALT_FIELD_DELIM + spec.getId()));
				try {
					ServerConfig.ctx.modifyAttributes(sPatternDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an object container (attribute or class) specification to the event pattern object!";
				}
				traceLine("  contSpec(name = \"" + spec.getName() + "\", "
						+ "id = \"" + spec.getId() + "\", " + "type = \""
						+ spec.getType() + "\")");
			}
		}

		return null;
	}


	public String writeRuleRecord(RuleSpec ruleSpec) {
		String sLabel = ruleSpec.getLabel();
		PatternSpec patSpec = ruleSpec.getPattern();
		List<ActionSpec> actSpecs = ruleSpec.getActions();
		ModificationItem[] mods = new ModificationItem[1];

		Attributes attrs = new BasicAttributes(true);
		String sRuleId = ruleSpec.getId();
		attrs.put("objectClass", sRuleClass);
		attrs.put("pmId", sRuleId);
		if (sLabel != null) {
			attrs.put("pmLabel", sLabel);
		}
		attrs.put("pmRank", String.valueOf(ruleSpec.getRank()));
		attrs.put("pmEventPattern", patSpec.getId());

		// Set the number of actions in the rule.
		int nActs = actSpecs.size();
		attrs.put("pmCount", String.valueOf(nActs));
		// Set the id of the first action in the rule.
		if (nActs > 0) {
			attrs.put("pmFirst", ( actSpecs.get(0)).getId());
			attrs.put("pmLast", ( actSpecs.get(nActs - 1)).getId());
		}

		String sRuleDn = "CN=" + sRuleId + "," + sRuleContainerDN;
		try {
			ServerConfig.ctx.bind(sRuleDn, null, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to create AD Rule object!";
		}
		traceTitle("RULE id = " + ruleSpec.getId());
		traceLine("  event id = " + patSpec.getId());

		for (int vindex = 0; vindex < actSpecs.size(); vindex++) {
			ActionSpec actSpec =  actSpecs.get(vindex);
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmAction", actSpec.getId()));
			try {
				ServerConfig.ctx.modifyAttributes(sRuleDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add action id to the rule object!";
			}
			traceLine("  action id = " + actSpec.getId());
		}
		return null;

	}



	public String checkRuleSemantics(RuleSpec ruleSpec) {
		PatternSpec patSpec = ruleSpec.getPattern();
		List<ActionSpec> actSpecs = ruleSpec.getActions();
		String result = null;

		// Check user specs in the event pattern. Note that the user spec
		// could in fact be a session spec or a process spec.
		Set<UserSpec> hs = patSpec.getUserSpecs();
		Iterator<UserSpec> hsiter;
		hsiter = hs.iterator();
		while (hsiter.hasNext()) {
			UserSpec spec = hsiter.next();
			String sType = spec.getType();
			if (sType.equalsIgnoreCase(PM_NODE.AUATTR.value)) {
				sType =PM_NODE.UATTR.value;
			}
			String sName = spec.getName();
			String sId = spec.getId();
			if (sId == null) {
				if (sName == null) {
					return "The user spec has both name and id null!";
				}
				sId = getEntityId(sName, sType);
				if (sId == null) {
					return "No entity " + sName + " of type " + sType;
				}
				spec.setId(sId);
			} else {
				if (sName == null) {
					sName = getEntityName(sId, sType);
					if (sName == null) {
						return "No entity with id " + sId + " of type " + sType;
					}
				}
				spec.setName(sName);
			}
		}

		// Check policy class specs in the event pattern:
		Set<PcSpec> pcSpec = patSpec.getPcSpecs();
		Iterator<PcSpec> pcIterator = pcSpec.iterator();
		while (pcIterator.hasNext()) {
			PcSpec spec = pcIterator.next();
			String sType = spec.getType();
			String sName = spec.getName();
			String sId = getEntityId(sName, sType);
			if (sId == null) {
				return ("No PM policy class \"" + sName + "\"!");
			}
			spec.setId(sId);
		}

		// Check event/op specs in the event pattern:
		Set<OpSpec> opSet = patSpec.getOpSpecs();
		Iterator<OpSpec> opIterator = opSet.iterator();
		while (opIterator.hasNext()) {
			OpSpec spec = opIterator.next();
			String sName = spec.getName();
			System.out.println("Op name = " + sName);
			if (!isEvent(sName)) {
				return ("No PM event \"" + sName + "\"!");
			}
		}

		// Check object specs in the event pattern:
		Set<ObjSpec> objSet = patSpec.getObjSpecs();
		Iterator<ObjSpec> objIterator = objSet.iterator();
		while (opIterator.hasNext()) {
			ObjSpec spec = objIterator.next();
			String sName = spec.getName();
			String sId = null;

			// sType can be: GlobalConstants.PM_OBJ. Change the type to object attribute
			// and check that the object really exists.
			spec.setType(PM_NODE.OATTR.value);
			sId = getEntityId(sName,PM_NODE.OATTR.value);

			// When the target object is the object just created and written,
			// the object name is unknown at compile time, so we don't signal
			// an error now. We just let the id be null.

			// if (sId == null || !hasAssocObj(sId)) return "No object \"" +
			// sName + "\"!";

			spec.setId(sId);
		}

		// Check container specs in the event pattern:
		Set<ContSpec> contSpecs = patSpec.getContSpecs();
		Iterator<ContSpec> contIterator = contSpecs.iterator();
		while (contIterator.hasNext()) {
			ContSpec spec = contIterator.next();
			System.out.println("Container before: \n" + spec);
			String sName = spec.getName();
			String sType = spec.getType();
			String sId = null;

			// sType can be: GlobalConstants.PM_OBJ,PM_NODE.OATTR.value, GlobalConstants.PM_RECORD, or GlobalConstants.PM_OBJ_CLASS.
			// Instead of
			// objects use the associated object attributes.
			// For records (containers with a small addition - a template plus
			// other
			// few things), the name can be * (for "any record").
			if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
				sType =PM_NODE.OATTR.value;
				spec.setType(PM_NODE.OATTR.value);
			} else if (sType.equalsIgnoreCase(GlobalConstants.PM_RECORD)) {
				sType =PM_NODE.OATTR.value;
			}
			// getIdOfEntityWithNameAndType(*, t) should return * (any).
			sId = getEntityId(sName, sType);
			if (sId == null) {
				return "No object or object attribute or class \"" + sName
						+ "\"!";
			}
			spec.setId(sId);
			System.out.println("Container After: \n" + spec);
		}

		// Check actions in the rule.
		for (int vindex = 0; vindex < actSpecs.size(); vindex++) {
			ActionSpec actSpec = actSpecs.get(vindex);

			// Check the action's condition.
			result = checkCondSemantics(actSpec);
			if (result != null) {
				return result;
			}

			String type = actSpec.getType();
			if (type.startsWith(RuleScanner.PM_VALUE_ASSIGN)) {
				result = checkAssignSemantics(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_GRANT)) {
				result = checkGrantSemantics(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_CREATE)) {
				result = checkCreateSemantics(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_DENY)) {
				result = checkDenySemantics(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
					+ RuleScanner.PM_VALUE_ASSIGNMENT)) {
				result = checkDeassignSemantics(actSpec);
				if (result != null) {
					return result;
				}
			} else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
					+ RuleScanner.PM_VALUE_DENY)) {
				result = checkDeleteDenySemantics(actSpec);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}


	public String checkCondSemantics(ActionSpec actSpec) {
		CondSpec condSpec = actSpec.getCondSpec();
		System.out.println("Checking condition's semantics");
		if (condSpec == null) {
			return null;
		}
		String sType = condSpec.getType();
		System.out.println("Condition of type " + sType);
		if (!sType.equalsIgnoreCase(RuleScanner.PM_VALUE_EXISTS)) {
			return "Condition type is not \"" + RuleScanner.PM_VALUE_EXISTS
					+ "\"!";
		}
		Set<List<OpndSpec>> opnds = condSpec.getOpnds1();
		String result = null;
		Iterator<List<OpndSpec>> hsiter = opnds.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, false);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}


	public String checkDeassignSemantics(ActionSpec actSpec) {
		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		String result = null;

		Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}


	public String checkAssignSemantics(ActionSpec actSpec) {
		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		String result = null;

		Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		Iterator<List<OpndSpec>> opnds2Iterator = opnds2.iterator();
		while (opnds2Iterator.hasNext()) {
			List<OpndSpec> opndVec = opnds2Iterator.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	// Check the semantics of the operand contained in a vector.
	// The operand is a PM entity name or function call and is stored as a tree
	// in a vector. First item (0) is the root of the tree. Each item points
	// to its children and to its parent. A simple name does not have children.
	// A function call has its arguments as children. Each item is of class
	// OpndSpec. A side effect of the semantic checks is setting the ids of
	// existing PM entities.
	// The arguments are: the vector containing the operand, the index
	// of the component to be first checked, and a boolean that indicates
	// whether to check the existence of a PM entity or not.

	public String checkOpndSemantics(List<OpndSpec> opndVec, int index,
			boolean checkExist) {
		String result = null;
		OpndSpec os = opndVec.get(index);

		// Operand is a PM entity (process, session, user, uattr, object, oattr,
		// policy, base, opset, class).
		if (!os.isFunction()) {
			String sName = os.getOrigName();
			String sType = os.getType();
			String sOrigId = null;
			if (!checkExist) {
				RandomGUID myGUID = new RandomGUID();
				os.setOrigId(myGUID.toStringNoDashes());
				return null;
			}
			if (sType.equalsIgnoreCase(GlobalConstants.PM_SESSION)) {
				// Look for the operand as a session.
				sOrigId = getEntityId(sName, GlobalConstants.PM_SESSION);
				if (sOrigId == null) {
					return "No PM entity \"" + sName + "\" of type \"" + sType
							+ "\"";
				}
			} else if (sType.equalsIgnoreCase(GlobalConstants.PM_PROCESS)) {
				// Look for the operand as a process.
				// The process name and id are identical.
				sOrigId = sName;
			} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
				// Look for the operand as an object attribute but associated to
				// an object.
				sOrigId = getEntityId(sName,PM_NODE.OATTR.value);
				if (sOrigId == null) {
					return "No PM entity \"" + sName + "\" of type \"" + sType
							+ "\"";
				}
				if (!hasAssocObj(sOrigId)) {
					return "No PM entity \"" + sName + "\" of type \"" + sType
							+ "\"";
				}
			} else if (sType.equalsIgnoreCase(GlobalConstants.PM_UNKNOWN)) {
				RandomGUID myGUID = new RandomGUID();
				sOrigId = myGUID.toStringNoDashes();
			} else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
				sOrigId = getEntityId(GlobalConstants.PM_CONNECTOR_NAME, sType);
				if (sOrigId == null) {
					return "No PM entity \"" + GlobalConstants.PM_CONNECTOR_NAME
							+ "\" of type \"" + sType + "\"";
				}
			} else {
				sOrigId = getEntityId(sName, sType);
				if (sOrigId == null) {
					return "No PM entity \"" + sName + "\" of type \"" + sType
							+ "\"";
				}
			}
			os.setOrigId(sOrigId);
			return null;
		}

		// Operand is a function.
		String sFunName = os.getOrigName();
		// Find out whether the function exists.
		if (!evrFunctionExists(sFunName)) {
			return "No EVER function \"" + sFunName + "\"";
		}
		os.setOrigId("F");

		// Check the number of arguments against the number of parameters. Note
		// that
		// the number of arguments may be larger than the number of parameters.
		List<Integer> children = os.getChildren();
		result = checkNumberOfArgs(sFunName, children.size());
		if (result != null) {
			return result;
		}

		// For each function parameter, check the corresponding argument type
		// against the
		// parameter type. Some arguments may not have corresponding parameters.
		// We don't
		// check those arguments.
		for (int j = 0; j < children.size(); j++) {
			int child = children.get(j).intValue();
			result = checkOpndSemantics(opndVec, child, checkExist);
			if (result != null) {
				return result;
			}

			// Check whether the argument type matches the function parameter
			// type.
			result = checkFunctionArg(sFunName, j,
					opndVec.get(child));
			if (result != null) {
				return result;
			}
		}

		// Check the function type.
		String sFunType = os.getType();
		result = checkFunctionType(sFunName, sFunType);
		if (result != null) {
			return result;
		}

		// Anything else???

		return result;
	}

	// First argument is the function name.
	// Second argument is the number of actual arguments.
	// One can find the number of formal parameters using the function name.
	// Originally, we wanted the number of formal parameters to be the same
	// as the number of actual arguments, but later we thought about allowing
	// functions with an unknown number of arguments, so we allow
	// the number of actual arguments to be larger than the number
	// of formal parameters, and we check the types only for the common ones.
	// Returns null, or err message: number of arguments mismatch.

	public String checkNumberOfArgs(String sFunName, int nArgCount) {
		int nParamCount = 0;

		try {
			String sTypes = getEvrFunctionParamTypes(sFunName);
			if (sTypes == null) {
				nParamCount = 0;
			} else {
				String[] pieces = sTypes.split(",");
				nParamCount = pieces.length;
			}
			// if (nArgCount != nParamCount) return
			// "Argument number mismatch for function " + sFunName;
			if (nArgCount < nParamCount) {
				return "Too few arguments for function " + sFunName;
			}
			return null;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception when matching the number of arguments for function "
			+ sFunName;
		}
	}

	// Tries to match the nth argument and parameter types. The argument type
	// probably is GlobalConstants.PM_UNKNOWN.
	// If the parameter type is GlobalConstants.PM_UNKNOWN, do not check further.
	// Otherwise, check whether an entity of the param type with the name of
	// the argument exists.
	// Returns null if OK, or err message if argument type mismatch.

	public String checkFunctionArg(String sFunName, int argIndex, OpndSpec arg) {
		try {
			String sTypes = getEvrFunctionParamTypes(sFunName);
			// If the function has no parameters, we still allow arguments.
			if (sTypes == null) {
				return null;
			}

			// Get the parameter count.
			String[] pieces = sTypes.split(",");
			int nParamCount = pieces.length;

			// We don't check arguments that do not have corresponding
			// parameters.
			if (argIndex >= nParamCount) {
				return null;
			}

			// Get the param type.
			String sParamType = pieces[argIndex];

			// If the param type is unknown, ok.
			if (sParamType.equalsIgnoreCase(GlobalConstants.PM_UNKNOWN)) {
				return null;
			}

			// The param type is known. First get the argument name.
			String sArgName = arg.getOrigName();

			// If the argument is a function call, check this function type
			// against
			// the parameter type.
			if (arg.isFunction()) {
				if (!evrFunctionExists(sArgName)) {
					return "No EVER function \"" + sArgName + "\"";
				}
				return checkFunctionType(sArgName, sParamType);
			}

			// Otherwise, check that an entity of the parameter type with the
			// name
			// of the argument exists.
			String sOrigId = getEntityId(sArgName, sParamType);
			if (sOrigId == null) {
				return "No entity " + sArgName + " of type " + sParamType
						+ " (argument " + argIndex + " of function " + sFunName
						+ ")";
			}
			arg.setOrigId(sOrigId);
			arg.setType(sParamType);
			return null;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception when matching the number of arguments of function "
			+ sFunName;
		}
	}

	// Tries to match the declared and required function type.
	// The required type may be unknown (no check), or specified explicitly
	// in the EVER script, or is the type of another function's parameter,
	// like in f1(f2(...)), where sFunId identifies f2, and sActType
	// is the type of the first argument of f1.
	// Returns null, or err message: function type mismatch.

	public String checkFunctionType(String sFunName, String sReqType) {
		if (sReqType.equalsIgnoreCase(GlobalConstants.PM_UNKNOWN)) {
			return null;
		}
		try {
			String sDclType = getEvrFunctionType(sFunName);
			if (sDclType == null) {
				return "No function or no type for function " + sFunName;
			}

			if (sDclType.equalsIgnoreCase(sReqType)) {
				return null;
			}
			if (sDclType.equalsIgnoreCase(PM_NODE.OATTR.value)
					&& sReqType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
				return null;
			}
			return "Type mismatch for function " + sFunName;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return "Exception when matching the type of function " + sFunName;
		}
	}


	public String getEvrFunctionType(String sName) {
		for (int i = 0; i < GlobalConstants.evrFunctions.length; i++) {
			if (sName.equalsIgnoreCase(GlobalConstants.evrFunctions[i].getName())) {
				return GlobalConstants.evrFunctions[i].getType();
			}
		}
		return null;
	}


	public String getEvrFunctionParamTypes(String sName) {
		for (int i = 0; i < GlobalConstants.evrFunctions.length; i++) {
			if (sName.equalsIgnoreCase(GlobalConstants.evrFunctions[i].getName())) {
				return GlobalConstants.evrFunctions[i].getParamTypes();
			}
		}
		return null;
	}


	public boolean evrFunctionExists(String sName) {
		for (int i = 0; i < GlobalConstants.evrFunctions.length; i++) {
			if (sName.equalsIgnoreCase(GlobalConstants.evrFunctions[i].getName())) {
				return true;
			}
		}
		return false;
	}


	public String checkGrantSemantics(ActionSpec actSpec) {
		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
		String result = null;

		// Opnds1 is a HashSet; each item is a vector of OpndsSpecs
		// that describes a user attribute.
		Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			Iterator<OpndSpec> hsiter2 = opndVec.iterator();
			while (hsiter2.hasNext()) {
				OpndSpec os = hsiter2.next();
				if (!isOperation(os.getOrigName())) {
					return "\"" + os.getOrigName() + "\" is not an operation";
				}
			}
		}

		// Opnds3 is a HashSet; each item is a vector of OpndsSpecs
		// that describes an object (attribute).
		hsiter = opnds3.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	// Opnds1 = what to create, opnds2 = representing what,
	// opnds3 = what properties, opnds4 = where to create.

	public String checkCreateSemantics(ActionSpec actSpec) {
		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
		Set<List<OpndSpec>> opnds4 = actSpec.getOpnds4();
		String result = null;

		// Opnds1 is a HashSet; each item (there should be exactly one item)
		// is a vector of OpndsSpec that describes the entity to be created.
		Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, false);
				if (result != null) {
					return result;
				}
			}
		}

		// Opnds2 is a HashSet; each item (there should be exactly one item)
		// is a vector of OpndsSpec that describes the entity represented
		// by the entity to be created. In Addition, the entity to be created
		// must
		// be an object.
		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		// Opnds3 is a HashSet; each item (there may be more than one or none)
		// is a vector of OpndsSpec that defines a property of the entity to be
		// created.
		hsiter = opnds3.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, false);
				if (result != null) {
					return result;
				}
			}
		}

		// Opnds4 is a HashSet; each item (there should be exactly one item)
		// is a vector of OpndsSpec that defines the container where the new
		// entity is to be created.
		hsiter = opnds4.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}


	public String checkDenySemantics(ActionSpec actSpec) {
		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
		String result = null;

		// Opnds1 is a HashSet; each item is a vector of OpndsSpecs
		// that describes a user, user attribute, session, or process.
		Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		// Opnds2 is a HashSet; each item is a OpndsSpec containing
		// exactly one granted operation.
		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndSpecList = hsiter.next();

			Iterator<OpndSpec> hister2 = opndSpecList.iterator();
			while (hister2.hasNext()) {
				OpndSpec os = hister2.next();

				if (!isOperation(os.getOrigName())) {
					return "\"" + os.getOrigName() + "\" is not an operation";
				}
			}

		}

		// Opnds3 is a HashSet; each item is a vector of OpndsSpecs
		// that describes an object attribute.
		hsiter = opnds3.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}


	public String checkDeleteDenySemantics(ActionSpec actSpec) {
		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
		String result = null;

		// Opnds1 is a HashSet; each item is a vector of OpndsSpecs
		// that describes a user or user attribute.
		Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}

		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndSpecList = hsiter.next();
			Iterator<OpndSpec> hister2 = opndSpecList.iterator();
			while (hister2.hasNext()) {
				OpndSpec os = hister2.next();
				if (!isOperation(os.getOrigName())) {
					return "\"" + os.getOrigName() + "\" is not an operation";
				}
			}
		}

		// Opnds3 is a HashSet; each item is a vector of OpndsSpecs
		// that describes an object attribute.
		hsiter = opnds3.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			if (!opndVec.isEmpty()) {
				result = checkOpndSemantics(opndVec, 0, true);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}


	public String createActionRecord(ActionSpec actSpec, String sPrevActId,
			int nActRank) {
		traceTitle("Action " + actSpec.getType() + ", id = " + actSpec.getId());
		Attributes attrs = new BasicAttributes(true);
		String sCrtActId = actSpec.getId();
		attrs.put("objectClass", sActionClass);
		attrs.put("pmId", sCrtActId);
		attrs.put("pmType", actSpec.getType());
		attrs.put("pmIsIntrasession", (actSpec.isIntrasession() ? "TRUE"
				: "FALSE"));
		attrs.put("pmIsIntersection", (actSpec.isIntersection() ? "TRUE"
				: "FALSE"));
		attrs.put("pmRank", String.valueOf(nActRank));

		// If there is a previous action record, set the back link to it.
		if (sPrevActId != null) {
			attrs.put("pmPrev", sPrevActId);
		}
		try {
			ServerConfig.ctx.bind("CN=" + sCrtActId + "," + sRuleContainerDN, null, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to create AD Action object!";
		}

		// If there is a previous action record, set the forward link to crt
		// record in it.
		if (sPrevActId != null) {
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmNext", sCrtActId));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + sPrevActId + ","
						+ sRuleContainerDN, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to set the forward link in an action object!";
			}
		}
		return null;
	}


	public String writeActionCondRecord(ActionSpec actSpec) {
		ModificationItem[] mods = new ModificationItem[1];
		Iterator<List<OpndSpec>> hsiter;
		List<OpndSpec> opndVec;

		CondSpec condSpec = actSpec.getCondSpec();
		if (condSpec == null) {
			return null;
		}

		String sCondId = condSpec.getId();
		traceTitle("Condition " + condSpec.getType() + ", id = " + sCondId);

		// Insert the link to the condition in the action record.
		String sActionDn = "CN=" + actSpec.getId() + "," + sRuleContainerDN;
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmCondition", sCondId));
		try {
			ServerConfig.ctx.modifyAttributes(sActionDn, mods);
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to add a condition to the action object!";
		}

		// Build the condition AD object.
		Attributes attrs = new BasicAttributes(true);
		String sCondDn = "CN=" + sCondId + "," + sRuleContainerDN;
		attrs.put("objectClass", sConditionClass);
		attrs.put("pmId", sCondId);
		attrs.put("pmType", condSpec.getType());
		attrs.put("pmIsNegated", condSpec.isNegated() ? "TRUE" : "FALSE");

		try {
			ServerConfig.ctx.bind(sCondDn, null, attrs);
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to create AD Condition object!";
		}

		// Insert the links to operands into the condition object.
		Set<List<OpndSpec>> opnds1 = condSpec.getOpnds1();
		hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd1", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sCondDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 1 id to the condition object!";
				}
				traceLine("  opnd1 id = " + sOpndId);
			}
		}

		// Write the operand AD objects.
		traceTitle("  First Operands:");
		String result = writeOpndRecords(opnds1);
		if (result != null) {
			return result;
		}

		return null;
	}


	public String writeDeleteRuleActionRecord(ActionSpec actSpec) {
		ModificationItem[] mods = new ModificationItem[1];
		Iterator<List<OpndSpec>> hsiter;

		String sActionDn = "CN=" + actSpec.getId() + "," + sRuleContainerDN;

		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();

		hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			String sOpndId = hsiter.next().get(0).getId();
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmOpnd1", sOpndId));
			try {
				ServerConfig.ctx.modifyAttributes(sActionDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add an operand 1 id to the action object!";
			}
			traceLine("  opnd1 id = " + sOpndId);
		}

		traceTitle("  First Operands:");
		String result = writeOpndRecords(opnds1);
		if (result != null) {
			return result;
		}
		return null;
	}


	public String writeDeleteDenyActionRecord(ActionSpec actSpec) {
		ModificationItem[] mods = new ModificationItem[1];
		Iterator<List<OpndSpec>> hsiter;
		List<OpndSpec> opndVec;

		String sActionDn = "CN=" + actSpec.getId() + "," + sRuleContainerDN;

		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();

		hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd1", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 1 id to the action object!";
				}
				traceLine("  opnd1 id = " + sOpndId);
			}
		}

		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			String sOpndId = hsiter.next().get(0).getId();
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmOpnd2", sOpndId));
			try {
				ServerConfig.ctx.modifyAttributes(sActionDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add an operand 2 id to the action object!";
			}
			traceLine("  opnd2 id = " + sOpndId);
		}

		hsiter = opnds3.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd3", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 3 id to the action object!";
				}
				traceLine("  opnd3 id = " + sOpndId);
			}
		}

		traceTitle("  First Operands:");
		String result = writeOpndRecords(opnds1);
		if (result != null) {
			return result;
		}

		traceTitle("  Second Operands:");
		result = writeOpndRecords(opnds2);
		if (result != null) {
			return result;
		}

		traceTitle("  Third Operands:");
		result = writeOpndRecords(opnds3);
		if (result != null) {
			return result;
		}
		return null;
	}


	public String writeDeassignActionRecord(ActionSpec actSpec) {
		ModificationItem[] mods = new ModificationItem[1];
		Iterator<List<OpndSpec>> hsiter;
		List<OpndSpec> opndVec;

		String sActionDn = "CN=" + actSpec.getId() + "," + sRuleContainerDN;

		// Write the operands to the active directory.
		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();

		hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd1", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 1 id to the action object!";
				}
				traceLine("  opnd1 id = " + sOpndId);
			}
		}

		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd2", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 2 id to the action object!";
				}
				traceLine("  opnd2 id = " + sOpndId);
			}
		}

		traceTitle("  First Operands:");
		String result = writeOpndRecords(opnds1);
		if (result != null) {
			return result;
		}

		traceTitle("  Second Operands:");
		result = writeOpndRecords(opnds2);
		if (result != null) {
			return result;
		}
		return null;
	}


	public String writeAssignActionRecord(ActionSpec actSpec) {
		ModificationItem[] mods = new ModificationItem[1];
		Iterator<List<OpndSpec>> hsiter;
		List<OpndSpec> opndVec;

		String sActionDn = "CN=" + actSpec.getId() + "," + sRuleContainerDN;

		// Write the operands to the active directory.
		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();

		hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd1", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 1 id to the action object!";
				}
				traceLine("  opnd1 id = " + sOpndId);
			}
		}

		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd2", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 2 id to the action object!";
				}
				traceLine("  opnd2 id = " + sOpndId);
			}
		}

		traceTitle("  First Operands:");
		String result = writeOpndRecords(opnds1);
		if (result != null) {
			return result;
		}

		traceTitle("  Second Operands:");
		result = writeOpndRecords(opnds2);
		if (result != null) {
			return result;
		}
		return null;
	}


	public String writeCreateActionRecord(ActionSpec actSpec) {
		ModificationItem[] mods = new ModificationItem[1];
		Iterator<List<OpndSpec>> hsiter;
		List<OpndSpec> opndVec;

		String sActionDn = "CN=" + actSpec.getId() + "," + sRuleContainerDN;

		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
		Set<List<OpndSpec>> opnds4 = actSpec.getOpnds4();

		// There should be only one opnd1, containing the entity to be created.
		hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd1", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 1 id to the action object!";
				}
				traceLine("  opnd1 id = " + sOpndId);
			}
		}

		// There should be at most one opnd2, containing the entity represented
		// by the entity to be created.
		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd2", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 2 id to the action object!";
				}
				traceLine("  opnd2 id = " + sOpndId);
			}
		}

		// Opnd3 contain the properties of the entity to be created.
		hsiter = opnds3.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd3", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 3 id to the action object!";
				}
				traceLine("  opnd3 id = " + sOpndId);
			}
		}

		// There should be only one opnd4, containing the container where the
		// entity in opnd1 is to be created.
		hsiter = opnds4.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd4", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 4 id to the action object!";
				}
				traceLine("  opnd4 id = " + sOpndId);
			}
		}

		traceTitle("  First Operands:");
		String result = writeOpndRecords(opnds1);
		if (result != null) {
			return result;
		}

		traceTitle("  Second Operands:");
		result = writeOpndRecords(opnds2);
		if (result != null) {
			return result;
		}

		traceTitle("  Third Operands:");
		result = writeOpndRecords(opnds3);
		if (result != null) {
			return result;
		}

		traceTitle("  Fourth Operands:");
		result = writeOpndRecords(opnds4);
		if (result != null) {
			return result;
		}

		return null;
	}


	public String writeGrantActionRecord(ActionSpec actSpec) {
		ModificationItem[] mods = new ModificationItem[1];
		Iterator<List<OpndSpec>> hsiter;
		List<OpndSpec> opndVec;

		String sActionDn = "CN=" + actSpec.getId() + "," + sRuleContainerDN;

		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();

		hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd1", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 1 id to the action object!";
				}
				traceLine("  opnd1 id = " + sOpndId);
			}
		}

		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			String sOpndId = hsiter.next().get(0).getId();
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmOpnd2", sOpndId));
			try {
				ServerConfig.ctx.modifyAttributes(sActionDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add an operand 2 id to the action object!";
			}
			traceLine("  opnd2 id = " + sOpndId);
		}

		hsiter = opnds3.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd3", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 3 id to the action object!";
				}
				traceLine("  opnd3 id = " + sOpndId);
			}
		}

		traceTitle("  First Operands:");
		String result = writeOpndRecords(opnds1);
		if (result != null) {
			return result;
		}

		traceTitle("  Second Operands:");
		result = writeOpndRecords(opnds2);
		if (result != null) {
			return result;
		}

		traceTitle("  Third Operands:");
		result = writeOpndRecords(opnds3);
		if (result != null) {
			return result;
		}
		return null;
	}


	public String writeDenyActionRecord(ActionSpec actSpec) {
		ModificationItem[] mods = new ModificationItem[1];
		Iterator<List<OpndSpec>> hsiter;
		List<OpndSpec> opndVec;

		String sActionDn = "CN=" + actSpec.getId() + "," + sRuleContainerDN;

		Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
		Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
		Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();

		hsiter = opnds1.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd1", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 1 id to the action object!";
				}
				traceLine("  opnd1 id = " + sOpndId);
			}
		}

		hsiter = opnds2.iterator();
		while (hsiter.hasNext()) {
			String sOpndId = hsiter.next().get(0).getId();
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmOpnd2", sOpndId));
			try {
				ServerConfig.ctx.modifyAttributes(sActionDn, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return "Unable to add an operand 2 id to the action object!";
			}
			traceLine("  opnd2 id = " + sOpndId);
		}

		hsiter = opnds3.iterator();
		while (hsiter.hasNext()) {
			opndVec = hsiter.next();
			if (opndVec.size() > 0) {
				String sOpndId = opndVec.get(0).getId();
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOpnd3", sOpndId));
				try {
					ServerConfig.ctx.modifyAttributes(sActionDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to add an operand 3 id to the action object!";
				}
				traceLine("  opnd3 id = " + sOpndId);
			}
		}

		traceTitle("  First Operands:");
		String result = writeOpndRecords(opnds1);
		if (result != null) {
			return result;
		}

		traceTitle("  Second Operands:");
		result = writeOpndRecords(opnds2);
		if (result != null) {
			return result;
		}

		traceTitle("  Third Operands:");
		result = writeOpndRecords(opnds3);
		if (result != null) {
			return result;
		}
		return null;
	}

	// The argument is a HashSet. Each item is an OpndSpec containing
	// exactly one simple operand (e.g., an operation for a grant action).

	public String writeSimpleOpndRecords(Set<List<OpndSpec>> opndsSet) {
		return writeOpndRecords(opndsSet, Boolean.TRUE);
	}


	public String writeOpndRecords(Set<List<OpndSpec>> opnds) {
		return writeOpndRecords(opnds, Boolean.FALSE);
	}


	public String writeOpndRecords(Set<List<OpndSpec>> opnds, Boolean simple) {
		Iterator<List<OpndSpec>> hsiter = opnds.iterator();
		while (hsiter.hasNext()) {
			List<OpndSpec> opndVec = hsiter.next();
			traceTitle("    Operand with id = "
					+ opndVec.get(0).getId());
			for (int i = 0; i < opndVec.size(); i++) {
				OpndSpec os = opndVec.get(i);
				String sOpndId = os.getId();
				StringBuilder sb = new StringBuilder();
				List<Integer> children = os.getChildren();
				assert (!simple || (simple && children.isEmpty()));
				for (int j = 0; j < children.size(); j++) {
					int child = children.get(j).intValue();
					if (j > 0) {
						sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
					}
					sb.append(opndVec.get(child).getId());
				}
				String sArgs = sb.toString();
				traceTitle("      Item " + i);
				traceLine("      id       = " + sOpndId);
				traceLine("      type     = " + os.getType());
				traceLine("      origName = " + os.getOrigName());
				traceLine("      function = " + os.isFunction());
				traceLine("      subgraph = " + os.isSubgraph());
				traceLine("      compl    = " + os.isComplement());
				traceLine("      parent   = " + os.getParent());
				if (!simple) {
					traceLine("      origId   = " + os.getOrigId());
					traceLine("      args     = " + sArgs);
				}

				Attributes attrs = new BasicAttributes(true);
				attrs.put("objectClass", sOperandClass);
				attrs.put("pmId", sOpndId);
				attrs.put("pmType", os.getType());
				attrs.put("pmIsFunction", (os.isFunction() ? "TRUE" : "FALSE"));
				attrs.put("pmIsSubgraph", (os.isSubgraph() ? "TRUE" : "FALSE"));
				attrs.put("pmIsComplement", (os.isComplement() ? "TRUE"
						: "FALSE"));
				attrs.put("pmOriginalName", os.getOrigName());
				if (!simple) {
					attrs.put("pmOriginalId", os.getOrigId());
					if (sArgs != null && sArgs.length() > 0) {
						attrs.put("pmArgs", sArgs);
					}
				}
				String sOpndDn = "CN=" + sOpndId + "," + sRuleContainerDN;
				try {
					ServerConfig.ctx.bind(sOpndDn, null, attrs);
				} catch (Exception e) {
					e.printStackTrace();
					return "Unable to create AD Operand " + os.getOrigName();
				}
			}
		}
		return null;
	}


	public void traceTitle(String title) {
		System.out.println();
		System.out.println(title);
	}


	public void traceLine(String line) {
		System.out.println("  " + line);
	}

	// The command parameters are: constraint name, operation, oattr name, oattr
	// id.
	// Note that the oattr name and id are prefixed with '!' if they designate
	// the complement of a container.
	// The constraint must exist. All other params may be null, but:
	//
	// If the operation is not null, it must be registered for the constraint.
	// If the oattr name or id is non-null, it must be registered for the
	// constraint and they must be consistent.

	public Packet deleteDeny(String sSessId, String sDenyName, String sOp,
			String sOattrName, String sOattrId) {
		// Check permissions...

		String sDenyId = getEntityId(sDenyName, GlobalConstants.PM_DENY);
		if (sDenyId == null) {
			return failurePacket("No such deny: " + sDenyName);
		}

		return deleteDenyInternal(sDenyId, sOp, sOattrName, sOattrId);
	}


	public Packet deleteDenyInternal(String sDenyId, String sOp,
			String sOattrName, String sOattrId) {
		ModificationItem[] mods = new ModificationItem[1];

		// First some checks.
		if (sOp != null && !denyHasOp(sDenyId, sOp)) {
			return failurePacket("Inconsistency: the deny constraint has no such operation!");
		}

		String sId = null;
		boolean isComplement = false;
		if (sOattrName != null) {
			if (sOattrName.startsWith("!")) {
				isComplement = true;
				sOattrName = sOattrName.substring(1);
			}

			sId = getEntityId(sOattrName,PM_NODE.OATTR.value);
			if (sId == null) {
				return failurePacket("Inconsistency: no such object attribute!");
			}
			if (sOattrId != null) {
				if (sOattrId.startsWith("!")) {
					sOattrId = sOattrId.substring(1);
				}
				if (!sOattrId.equals(sId)) {
					return failurePacket("Inconsistency: the object attribute name is not consistent with its id!");
				}
			}

			if (isComplement) {
				sOattrId = "!" + sId;
			} else {
				sOattrId = sId;
			}
		}

		// If neither op nor container, delete the deny constraint.
		if (sOp == null && sOattrId == null) {
			try {
				ServerConfig.ctx.destroySubcontext("CN=" + sDenyId + "," + sDenyContainerDN);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Error while deleting the deny constraint "
						+ sDenyId + ": " + e.getMessage());
			}
			return ADPacketHandler.getSuccessPacket();
		}
		if (sOp != null) {
			// Delete the operation from the deny constraint.
			mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute("pmOp", sOp));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + sDenyId + "," + sDenyContainerDN,
						mods);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Error while deleting operation " + sOp
						+ ": " + e.getMessage());
			}
		}

		if (sOattrId != null) {
			// Delete the container from the deny constraint.
			mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute("pmAttr", sOattrId));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + sDenyId + "," + sDenyContainerDN,
						mods);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Error while deleting container: "
						+ e.getMessage());
			}
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Returns the id of a similar deny.

	public String getSimilarDeny(String sDenyType, String sOrigName,
			String sOrigId, boolean bInters, HashSet<ActOpnd> hsOpnd2,
			HashSet<ActOpnd> hsOpnd3) {

		// The op set of an existing deny.
		HashSet<String> hsCrtOps = new HashSet<String>();

		// The op set of the deny we want to add.
		HashSet<String> hsOps = new HashSet<String>();
		Iterator<ActOpnd> iter = hsOpnd2.iterator();
		while (iter.hasNext()) {
			String sOp = iter.next().getName();
			hsOps.add(sOp);
		}

		// The container set of an existing deny.
		HashSet<String> hsCrtConts = new HashSet<String>();

		// The container set of the deny we want to add.
		HashSet<String> hsConts = new HashSet<String>();
		iter = hsOpnd3.iterator();
		while (iter.hasNext()) {
			ActOpnd opnd = iter.next();
			String sContId;
			if (opnd.isComplement()) {
				sContId = "!" + opnd.getId();
			} else {
				sContId = opnd.getId();
			}
			hsConts.add(sContId);
		}
		try {
			// Walk thru all denies.
			NamingEnumeration<?> denies;
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName",
					"pmType", "pmOriginalId", "pmIsIntersection", "pmOp",
			"pmAttr"});
			denies = ServerConfig.ctx.search(sDenyContainerDN, "(objectClass=*)",
					constraints);
			while (denies != null && denies.hasMore()) {
				SearchResult sr = (SearchResult) denies.next();

				// Get the id of the current deny constraint.
				String sCrtDenyId = (String) sr.getAttributes().get("pmId").get();

				// Check the types.
				String sCrtDenyType = (String) sr.getAttributes().get("pmType").get();
				System.out.println("Compare " + sDenyType + " to "
						+ sCrtDenyType);
				if (!sCrtDenyType.equalsIgnoreCase(sDenyType)) {
					continue;
				}
				// Check if both are intersections or both unions.
				boolean bCrtInters = ((String) sr.getAttributes().get("pmIsIntersection").get()).equalsIgnoreCase("TRUE");
				System.out.println("Compare " + bInters + " to " + bCrtInters);
				if (bCrtInters != bInters) {
					continue;
				}
				// Check the user or attribute id.
				String sCrtId = (String) sr.getAttributes().get("pmOriginalId").get();
				System.out.println("Compare " + sOrigId + " to " + sCrtId);
				if (!sOrigId.equalsIgnoreCase(sCrtId)) {
					continue;
				}
				// Check the operations.
				hsCrtOps.clear();
				Attribute attr = sr.getAttributes().get("pmOp");
				if (attr != null) {
					for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
						hsCrtOps.add((String) enumer.next());
					}
				}
				printSet(hsOps, GlobalConstants.PM_PERM, "Compare operations");
				printSet(hsCrtOps, GlobalConstants.PM_PERM, "with crt. operations");
				if (!hsOps.containsAll(hsCrtOps)) {
					continue;
				}
				if (!hsCrtOps.containsAll(hsOps)) {
					continue;
				}

				// Check the containers.
				hsCrtConts.clear();
				attr = sr.getAttributes().get("pmAttr");
				if (attr != null) {
					for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
						hsCrtConts.add((String) enumer.next());
					}
				}
				printSet(hsConts, GlobalConstants.PM_PERM, "Compare containers");
				printSet(hsCrtConts, GlobalConstants.PM_PERM, "with crt. containers");
				if (!hsConts.containsAll(hsCrtConts)) {
					continue;
				}
				if (!hsCrtConts.containsAll(hsConts)) {
					continue;
				}
				return sCrtDenyId;
			}
			return null;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// The command parameters are: constraint name, type, user or uattr name,
	// user or uattr id, an operation, an oattr name, oattr id, and a boolean
	// denoting whether the denied operations apply to the intersection or to
	// the union of the object attributes.
	//
	// The deny type can be: GlobalConstants.PM_DENY_USER_ID, GlobalConstants.PM_DENY_INTRA_SESSION, or
	// GlobalConstants.PM_DENY_ACROSS_SESSIONS.
	// The constraint name must be non-null. All others may be null, with
	// the following restrictions:
	//
	// If the constraint does not exist, this API will try to create it. In this
	// case, the type and the user or uattr name cannot be null. The user or
	// uattr id, if non-null, must match the user or uattr name.
	// If the constraint already exists, this API will try to add an operation
	// or/and an object container (attribute), whichever is non-null.
	// In this case, the type, the user or uattr name, and the user or uattr id,
	// if non-null, must match the ones registered with the constraint.
	//
	// A container name (sOattrName) may start with an "!" to denote the
	// complement of that container.

	public Packet addDeny(String sSessId, String sDenyName, String sDenyType,
			String sDenyToName, String sDenyToId, String sOp,
			String sOattrName, String sOattrId, String sIsInters) {
		// Check permissions.
		// ...

		return addDenyInternal(sDenyName, sDenyType, sDenyToName, sDenyToId,
				sOp, sOattrName, sOattrId, sIsInters.equalsIgnoreCase("yes"));
	}


	public Packet addDenyInternal(String sDenyName, String sDenyType,
			String sDenyToName, String sDenyToId, String sOp,
			String sOattrName, String sOattrId, boolean bInters) {
		System.out.println("addDenyInternal(");
		System.out.println("                deny name = " + sDenyName);
		System.out.println("                deny type = " + sDenyType);
		System.out.println("                denyto name = " + sDenyToName);
		System.out.println("                denyto id = " + sDenyToId);
		System.out.println("                deny op = " + sOp);
		System.out.println("                deny oa name = " + sOattrName);
		System.out.println("                deny oa id = " + sOattrId);
		System.out.println("                deny inters = " + bInters);
		System.out.println("                )");

		ModificationItem[] mods = new ModificationItem[1];
		Packet result;

		// First check whether the deny exists.
		String sDenyId = getEntityId(sDenyName, GlobalConstants.PM_DENY);
		if (sDenyId != null) {
			// A deny constraint with that name already exists.
			// Do a series of checks.
			result =  getDenySimpleInfo(sDenyId);
			// The information returned by getDenySimpleInfo has the following
			// format:
			// item 0: <deny name>:<deny id>
			// item 1: <deny type>:<denyto name>:<denyto id>:<is intersection>
			if (result.hasError()) {
				return result;
			}
			String sLine = result.getStringValue(1);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);

			if (sDenyType != null && !sDenyType.equals(pieces[0])) {
				return failurePacket("In deny, the type does not match its registered type!");
			}

			if (sDenyToName != null && !sDenyToName.equals(pieces[1])) {
				return failurePacket("In deny, the session/process/user/attribute name does not match the registered name!");
			}

			if (sDenyToId != null && !sDenyToId.equals(pieces[2])) {
				return failurePacket("In deny, the session/process/user/attribute id does not match the registered id!");
			}

		} else { // New constraint.
			// A series of checks.
			if (sDenyType == null
					|| (!sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_USER_ID)
							&& !sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_USER_SET)
							&& !sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_SESSION)
							&& !sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_PROCESS)
							&& !sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_INTRA_SESSION) && !sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_ACROSS_SESSIONS))) {
				return failurePacket("Null or invalid deny type!");
			}
			if (sDenyToName == null) {
				return failurePacket("Please select a session, process, user or user attribute name!");
			}

			String sId;
			if (sDenyType.equals(GlobalConstants.PM_DENY_SESSION)) {
				sId = getEntityId(sDenyToName, GlobalConstants.PM_SESSION);
			} else if (sDenyType.equals(GlobalConstants.PM_DENY_PROCESS)) {
				sId = sDenyToName;
			} else if (sDenyType.equals(GlobalConstants.PM_DENY_USER_ID)) {
				sId = getEntityId(sDenyToName,PM_NODE.USER.value);
			} else {
				sId = getEntityId(sDenyToName,PM_NODE.UATTR.value);
			}

			if (sDenyToId != null && !sDenyToId.equals(sId)) {
				return failurePacket("In deny, the session/process/user/user attribute id does not match the registered id!");
			}

			// Anyway, set the session, user or attr id:
			sDenyToId = sId;
		}

		// Now add the constraint if new.
		if (sDenyId == null) {
			Attributes attrs = new BasicAttributes(true);
			RandomGUID myGUID = new RandomGUID();
			sDenyId = myGUID.toStringNoDashes();
			attrs.put("objectClass", sDenyClass);
			attrs.put("pmId", sDenyId);
			attrs.put("pmName", sDenyName);
			attrs.put("pmType", sDenyType);
			attrs.put("pmOriginalId", sDenyToId);
			attrs.put("pmIsIntersection", (bInters ? "TRUE" : "FALSE"));

			String sDenyDn = "CN=" + sDenyId + "," + sDenyContainerDN;
			try {
				ServerConfig.ctx.bind(sDenyDn, null, attrs);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Unable to create Deny constraint \""
						+ sDenyName + "\"");
			}
			// If the operation is present, add it to the deny constraint.
			if (sOp != null && sOp.length() > 0) {
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOp", sOp));
				try {
					ServerConfig.ctx.modifyAttributes(sDenyDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return failurePacket("Unable to add operation \"" + sOp
							+ "\": " + e.getMessage());
				}
			}
			// If the container is present, add it to the deny constraint.
			if (sOattrId != null && sOattrId.length() > 0) {
				if (sOattrName.startsWith("!")) {
					System.out.println("Complementary container!");
					sOattrId = "!" + sOattrId;
				}
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmAttr", sOattrId));
				try {
					ServerConfig.ctx.modifyAttributes(sDenyDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return failurePacket("Unable to add container \""
							+ sOattrName + "\": " + e.getMessage());
				}
			}
		} else {
			// Deny exists, try to add the operation and/or container, which
			// cannot be
			// duplicate or both null.
			if ((sOp == null || sOp.length() == 0)
					&& (sOattrId == null || sOattrId.length() == 0)) {
				return failurePacket("Please select an operation and/or a container!");
			}

			if (sOp != null) {
				if (denyHasOp(sDenyId, sOp)) {
					return failurePacket("Duplicate operation " + sOp
							+ " in deny constraint!");
				}
				// Add the operation.
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOp", sOp));
				try {
					ServerConfig.ctx.modifyAttributes("CN=" + sDenyId + ","
							+ sDenyContainerDN, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return failurePacket("Unable to add operation \"" + sOp
							+ "\"; " + e.getMessage());
				}
			}
			if (sOattrId != null) {
				if (denyHasOattr(sDenyId, sOattrId)) {
					return failurePacket("Duplicate container " + sOattrName
							+ " in deny constraint!");
				}
				if (sOattrName.startsWith("!")) {
					sOattrId = "!" + sOattrId;
				}
				// Add the container.
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmAttr", sOattrId));
				try {
					ServerConfig.ctx.modifyAttributes("CN=" + sDenyId + ","
							+ sDenyContainerDN, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return failurePacket("Unable to add container \""
							+ sOattrName + "\"; " + e.getMessage());
				}
			}
		}
		result = new Packet();
		try {
			result.addItem(ItemType.RESPONSE_TEXT, sDenyName + GlobalConstants.PM_FIELD_DELIM
					+ sDenyId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return result;
	}

	// The information returned by getDenySimpleInfo has the following format:
	// item 0: <deny name>:<deny id>
	// item 1: <deny type>:<user or attribute name>:<user or attribute id>:<is
	// intersection>

	public Packet getDenySimpleInfo(String sDenyId) {
		Packet result = new Packet();
		Attributes attrs;
		String sId, sName;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sDenyId + "," + sDenyContainerDN);

			// First insert the constraint name and id.
			sName = (String) attrs.get("pmName").get();
			result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
					+ sDenyId);

			// Now the deny type and the session/process/user/attr name and id:
			String sType = (String) attrs.get("pmType").get();
			sId = (String) attrs.get("pmOriginalId").get();
			if (sType.equals(GlobalConstants.PM_DENY_USER_ID)) {
				sName = getEntityName(sId,PM_NODE.USER.value);
			} else if (sType.equals(GlobalConstants.PM_DENY_SESSION)) {
				sName = getEntityName(sId, GlobalConstants.PM_SESSION);
			} else if (sType.equals(GlobalConstants.PM_DENY_PROCESS)) {
				sName = sId;
			} else {
				sName = getEntityName(sId,PM_NODE.UATTR.value);
			}
			String sInters = (((String) attrs.get("pmIsIntersection").get()).equalsIgnoreCase("TRUE") ? "yes" : "no");
			result.addItem(ItemType.RESPONSE_TEXT, sType + GlobalConstants.PM_FIELD_DELIM
					+ sName + GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM + sInters);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in getDenySimpleInfo: "
					+ e.getMessage());
		}
		return result;
	}

	// The information returned by getTemplateInfo has the following format:
	// item 0: <tpl name>:<tpl id>
	// item 1: <cont 1 id>:...:<cont n id>
	// item 2: <key1>:...:<keyn>

	public Packet getTemplateInfo(String sSessId, String sTplId) {
		Packet result = new Packet();
		Attributes attrs;
		String sName;
		Attribute attr;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sTplId + ","
					+ sTemplateContainerDN);

			// First insert the template name and id.
			sName = (String) attrs.get("pmName").get();
			result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
					+ sTplId);

			// Now the container ids.
			attr = attrs.get("pmComponents");
			result.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());

			// Now the keys (their names).
			attr = attrs.get("pmKey");
			if (attr == null) {
				result.addItem(ItemType.RESPONSE_TEXT, "");
			} else {
				boolean first = true;
				StringBuffer sb = new StringBuffer();
				for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
					String sKey = (String) attrEnum.next();
					if (first) {
						sb.append(sKey);
						first = false;
					} else {
						sb.append(GlobalConstants.PM_FIELD_DELIM + sKey);
					}
				}
				result.addItem(ItemType.RESPONSE_TEXT, sb.toString());
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getTemplateInfo(): "
					+ e.getMessage());
		}
	}

	// The information returned by getDenyInfo has the following format:
	// item 0: <deny name>:<deny id>
	// item 1: <deny type>:<user or attribute name>:<user or attribute id>:<is
	// intersection>
	// item 2: <opcount>
	// items 3 through 3 + opcount - 1: <operation>
	// item 3 + opcount: <contcount>
	// item 3 + opcount + 1 through 3 + opcount + 1 + contcount - 1: <container
	// name>:<container id>
	// Note that the container name is prefixed with "!" for complements.

	public Packet getDenyInfo(String sSessId, String sDenyId) {
		Packet result = new Packet();
		Attributes attrs;
		String sId, sName;
		Attribute attr;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sDenyId + "," + sDenyContainerDN);

			// First insert the constraint name and id.
			sName = (String) attrs.get("pmName").get();
			result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
					+ sDenyId);

			// Now the deny type and the session, user, or attribute name and
			// id.
			String sType = (String) attrs.get("pmType").get();
			sId = (String) attrs.get("pmOriginalId").get();
			if (sType.equals(GlobalConstants.PM_DENY_SESSION)) {
				sName = getEntityName(sId, GlobalConstants.PM_SESSION);
			} else if (sType.equals(GlobalConstants.PM_DENY_PROCESS)) {
				sName = sId;
			} else if (sType.equals(GlobalConstants.PM_DENY_USER_ID)) {
				sName = getEntityName(sId,PM_NODE.USER.value);
			} else {
				sName = getEntityName(sId,PM_NODE.UATTR.value);
			}

			if (sName == null) {
				return failurePacket("No entity of type " + sType + " and id "
						+ sId);
			}

			String sInters = (((String) attrs.get("pmIsIntersection").get()).equalsIgnoreCase("TRUE") ? "yes" : "no");
			result.addItem(ItemType.RESPONSE_TEXT, sType + GlobalConstants.PM_FIELD_DELIM
					+ sName + GlobalConstants.PM_FIELD_DELIM + sId + GlobalConstants.PM_FIELD_DELIM + sInters);

			// Now the operations, one per item, prefixed by their number.
			attr = attrs.get("pmOp");
			if (attr == null) {
				result.addItem(ItemType.RESPONSE_TEXT, String.valueOf(0));
			} else {
				result.addItem(ItemType.RESPONSE_TEXT,
						String.valueOf(attr.size()));
				for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
					result.addItem(ItemType.RESPONSE_TEXT,
							(String) attrEnum.next());
				}
			}

			// Now the containers, one per item, prefixed by their number.
			attr = attrs.get("pmAttr");
			if (attr == null) {
				result.addItem(ItemType.RESPONSE_TEXT, String.valueOf(0));
			} else {
				result.addItem(ItemType.RESPONSE_TEXT,
						String.valueOf(attr.size()));
				for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
					sId = (String) attrEnum.next();
					String sCleanId;
					if (sId.startsWith("!")) {
						sCleanId = sId.substring(1);
					} else {
						sCleanId = sId;
					}
					sName = getEntityName(sCleanId,PM_NODE.OATTR.value);
					if (sName == null) {
						return failurePacket("No object attribute with id = "
								+ sCleanId);
					}

					if (sId.startsWith("!")) {
						sName = "!" + sName;
					}
					result.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_FIELD_DELIM + sId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getDenyInfo(): "
					+ e.getMessage());
		}
		return result;
	}


	public Packet getDenies(String sSessId) {
		Packet result = new Packet();
		NamingEnumeration<?> denies;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			denies = ServerConfig.ctx.search(sDenyContainerDN, "(objectClass=*)",
					constraints);
			while (denies != null && denies.hasMore()) {
				SearchResult sr = (SearchResult) denies.next();
				String sDenyName = (String) sr.getAttributes().get("pmName").get();
				String sDenyId = (String) sr.getAttributes().get("pmId").get();
				result.addItem(ItemType.RESPONSE_TEXT, sDenyName
						+ GlobalConstants.PM_FIELD_DELIM + sDenyId);
			}
			return result;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in getDenies(): " + e.getMessage());
		}
	}


	public Packet testGetDeniedPerms(String sCrtSessId, String sObjName) {
		String sObjId = getEntityId(sObjName, GlobalConstants.PM_OBJ);
		if (sObjId == null) {
			return failurePacket("No such object " + sObjName + "!");
		}
		String sOattrId = getAssocOattr(sObjId);
		if (sOattrId == null) {
			return failurePacket("Inconsistency: no associated object attribute!");
		}
		HashSet<String> deniedOps;
		try {
			deniedOps = getDeniedPerms(sCrtSessId, null, sOattrId,
					PM_NODE.OATTR.value);
		} catch (Exception e) {
			e.printStackTrace();
			deniedOps = new HashSet<String>();
		}
		printSet(deniedOps, GlobalConstants.PM_PERM, "Denied permissions are:");
		return ADPacketHandler.setToPacket(deniedOps);
	}

	// Compute and return the permissions denied to the user/process/session.
	// Parameters:
	// sCrtSessId: the id of the current session. The user can be retrieved.
	// sCrtProcId: the id of the current process. Not used if null.
	// sEntId, sEntType: the id and type of the entity on which we compute
	// the denied permissions.
	//

	public HashSet<String> getDeniedPerms(String sCrtSessId,
			String sCrtProcId, String sEntId, String sEntType) throws Exception {
		System.out.println("%%%%% getDeniedPerms(" + sEntId + ", " + sEntType
				+ ") i.e., for " + getEntityName(sEntId, sEntType));

		HashSet<String> deniedOps = new HashSet<String>();

		// For entities other than object attributes, return an empty set of
		// denied ops.
		if (!sEntType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			return deniedOps;
		}

		// Get the user of the current session.
		String sUserId = getSessionUserId(sCrtSessId);

		// The user's active attributes across all his sessions.
		HashSet<String> activesAcrossSessions = null;
		// The user's active attributes in the current session.
		HashSet<String> activesInCrtSession = null;

		// Walk thru all denies.
		NamingEnumeration<?> denies;
		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		constraints.setReturningAttributes(new String[]{"pmId", "pmName",
				"pmType", "pmOriginalId", "pmIsIntersection", "pmOp",
		"pmAttr"});
		denies = ServerConfig.ctx.search(sDenyContainerDN, "(objectClass=*)", constraints);

		while (denies != null && denies.hasMore()) {
			SearchResult sr = (SearchResult) denies.next();
			String sDenyType = (String) sr.getAttributes().get("pmType").get();
			String sDenyId = (String) sr.getAttributes().get("pmId").get();
			boolean bIntersection = ((String) sr.getAttributes().get("pmIsIntersection").get()).equalsIgnoreCase("TRUE");

			System.out.println("Found deny constraint " + sDenyId + " of type "
					+ sDenyType + " intersection=" + bIntersection);

			// Regardless of the type of the deny constraint, find out whether
			// the entity (an object attribute) is in the constraint's container
			// set.
			Attribute attr = sr.getAttributes().get("pmAttr");
			if (!entityIsInDenyList(sEntId, sEntType, attr, bIntersection)) {
				System.out.println("Entity " + getEntityName(sEntId, sEntType)
				+ " is NOT in " + sDenyId);
				continue;
			}
			System.out.println("Object " + getEntityName(sEntId, sEntType)
			+ " is in " + sDenyId);

			// If the deny constraint is of type session (PM_DENY_SESSION),
			// check whether the session id in the constraint matches this
			// session id
			// (the session trying to access a resource) and
			// skip the deny if not.
			// Else if the deny constraint is of type user id (PM_DENY_USER_ID),
			// check that the userid in the constraint matches sUserId, and
			// skip the deny if not.
			// Else if the deny constraint is of type attribute intrasession
			// (PM_DENY_INTRA_SESSION), check that the attribute in the
			// constraint
			// is active for sUserId in the current session.
			// Else (i.e., the deny constraint is of type
			// GlobalConstants.PM_DENY_ACROSS_SESSIONS),
			// check that the attribute in the constraint is active in any
			// session of the user.
			String sDenyToId = (String) sr.getAttributes().get("pmOriginalId").get();
			if (sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_SESSION)) {
				if (!sCrtSessId.equals(sDenyToId)) {
					System.out.println("Session "
							+ getEntityName(sCrtSessId, GlobalConstants.PM_SESSION)
							+ " is NOT in " + sDenyId);
					continue;
				} else {
					System.out.println("Session "
							+ getEntityName(sCrtSessId, GlobalConstants.PM_SESSION) + " is in "
							+ sDenyId);
				}
			} else if (sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_PROCESS)) {
				if (sCrtProcId == null || !sCrtProcId.equals(sDenyToId)) {
					System.out.println("Process " + sCrtProcId + " is NOT in "
							+ sDenyId);
					continue;
				} else {
					System.out.println("Process " + sCrtProcId + " is in "
							+ sDenyId);
				}
			} else if (sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_USER_ID)) {
				if (!sUserId.equals(sDenyToId)) {
					System.out.println("User "
							+ getEntityName(sUserId,PM_NODE.USER.value)
							+ " is NOT in " + sDenyId);
					continue;
				} else {
					System.out.println("User "
							+ getEntityName(sUserId,PM_NODE.USER.value) + " is in "
							+ sDenyId);
				}
				}
			// Regardless of the type of the deny constraint, add the
			// constraint's
			// operations to the set deniedOps.
			attr = sr.getAttributes().get("pmOp");
			if (attr == null) {
				continue;
			}
			try {
				for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
					String sOp = (String) attrEnum.next();
					if (!deniedOps.contains(sOp)) {
						deniedOps.add(sOp);
					}
				}
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
			}
		}
		printSet(deniedOps, GlobalConstants.PM_PERM, "Operations denied for user "
				+ getEntityName(sUserId,PM_NODE.USER.value) + " and entity "
				+ getEntityName(sEntId, sEntType));
		return deniedOps;
	}

	// sEntId and sEntType identify an object attribute.
	// Parameter attr contains the "pmAttr" attributes of a deny constraint
	// and represents a set of object containers (i.e., object attributes).
	// The function checks whether the object attribute is contained in the
	// union or the intersection of the containers, depending on the value
	// of the bInters (false or true). Note that some containers may have
	// their id prefixed with the "!" symbol, which means to check whether
	// the object attribute is NOT in that container.

	public boolean entityIsInDenyList(String sEntId, String sEntType,
			Attribute attr, boolean bInters) {
		if (attr == null) {
			System.out.println("No object attributes in the constraint!");
			return false;
		}
		System.out.println("....ObjIsInDenyList");
		try {
			if (bInters) {
				// Do the intersection
				System.out.println("......Do intersection");
				for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
					String sId = (String) attrEnum.next();
					if (sId.startsWith("!")) {
						sId = sId.substring(1);
						System.out.println("........Processing container C("
								+ getEntityName(sId,PM_NODE.OATTR.value) + ")");
						if (attrIsAscendant(sEntId, sId,PM_NODE.OATTR.value)) {
							System.out.println("..........Obj is ascendant of container, return false");
							return false;
						} else {
							System.out.println("..........Obj is not ascendant of container");
						}
					} else {
						System.out.println("........Processing container "
								+ getEntityName(sId,PM_NODE.OATTR.value));
						if (!attrIsAscendant(sEntId, sId,PM_NODE.OATTR.value)) {
							System.out.println("..........Obj is not ascendant of container, return false");
							return false;
						} else {
							System.out.println("..........Obj is ascendant of container");
						}
					}
				}
				return true;
			} else {
				// Do the union.
				for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
					String sId = (String) attrEnum.next();
					if (sId.startsWith("!")) {
						sId = sId.substring(1);
						System.out.println("........Processing container C("
								+ getEntityName(sId,PM_NODE.OATTR.value) + ")");
						if (!attrIsAscendant(sEntId, sId,PM_NODE.OATTR.value)) {
							System.out.println("..........Obj is not ascendant of container, return true");
							return true;
						} else {
							System.out.println("..........Obj is ascendant of container");
						}
					} else {
						System.out.println("........Processing container "
								+ getEntityName(sId,PM_NODE.OATTR.value));
						if (attrIsAscendant(sEntId, sId,PM_NODE.OATTR.value)) {
							System.out.println("..........Obj is ascendant of container, return true");
							return true;
						} else {
							System.out.println("..........Obj is not ascendant of container");
						}
					}
				}
				return false;
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}



	public Packet getHostRepository(String sSessId) {
		String sHost = getSessionHostName(sSessId);
		if (sHost == null) {
			return failurePacket("No such session or host!");
		}
		String sReposit = getHostRepositoryInternal(sHost);
		if (sReposit == null) {
			return failurePacket("No repository for host " + sHost + "!");
		}
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sReposit);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return res;
	}

	// Get host's repository path. This is the physical home of newly created
	// objects.

	public String getHostRepositoryInternal(String sHostName) {
		NamingEnumeration<?> entities;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmPath"});
			entities = ServerConfig.ctx.search(sHostContainerDN, "(pmName=" + sHostName
					+ ")", constraints);
			if (entities == null || !entities.hasMore()) {
				return null;
			}
			SearchResult sr = (SearchResult) entities.next();
			Attributes attrs = sr.getAttributes();
			return (String) (attrs.get("pmPath").get());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return a list of all of the pm applications installed in the system
	 */

	public Packet getInstalledApps(String sHost) {
		NamingEnumeration<SearchResult> users;
		Packet resp = new Packet();
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			users = ServerConfig.ctx.search(sOsConfigContainerDN, "(&(pmId=*)(pmHost=" + sHost + "))", constraints);
			System.out.println("Searching for apps");
			if (users == null || !users.hasMore()) {
				return resp;
			}
			while (users.hasMore()) {
				SearchResult result = users.next();
				Attribute attr = result.getAttributes().get("pmId");
				if (attr == null) {
					return resp;
				}
				if (attr.get().toString().equalsIgnoreCase("Exporter") || attr.get().toString().equalsIgnoreCase("Admin")) {
					continue;
				}
				System.out.println("Adding installed item " + attr.get());
				resp.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return resp;

	}

	// Get the paths of applications installed on a host. The result should
	// contain the admin tool path in item 0 and the rtf editor path in item 1
	// if they are installed, otherwise the empty strings.

	public Packet getHostAppPaths(String sClientId, String sSessId,
			String sHost, String appName) {
		System.out.println("getHostAppPaths called in PmEngine");
		NamingEnumeration<?> configs;
		Packet res = new Packet();

		// Look for a OS Config record for the given host.
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId",
					"pmAppPath", "pmAppPrefix", "pmAppMainClass"});
			configs = ServerConfig.ctx.search(sOsConfigContainerDN, "(&(pmid=" + appName
					+ ")(pmHost=" + sHost + "))", constraints);
			if (configs == null || !configs.hasMore()) {
				return res;
			}

			SearchResult sr = (SearchResult) configs.next();
			Attribute attr = sr.getAttributes().get("pmAppPath");
			if (attr == null) {
				res.addItem(ItemType.RESPONSE_TEXT, "");
			} else {
				res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());
			}
			System.out.println("pmAppPath: " + (String)attr.get());

			attr = sr.getAttributes().get("pmAppPrefix");
			if (attr == null) {
				res.addItem(ItemType.RESPONSE_TEXT, "");
			} else {
				res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());
			}
			System.out.println("pmAppPrefix: " + (String)attr.get());

			attr = sr.getAttributes().get("pmAppMainClass");
			if (attr == null) {
				res.addItem(ItemType.RESPONSE_TEXT, "");
			} else {
				res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());
			}
			System.out.println("pmAppMainClass: " + (String)attr.get());

			return res;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				System.out.println("debugflag");
				e.printStackTrace();
			}
			return failurePacket("Exception while looking for the OS configuration on "
					+ sHost);
		}
	}

	// Get the app paths for all hosts, one host per item.
	// An item should contain:
	// <admin tool path>|
	// <rtf editor path>|
	// <wkf app path>|
	// <egrant app path>|
	// <exporter path>|
	// <openoffice path>|
	// <msoffice path>|
	// <med rec editor path>|
	// <acct rec editor path>|
	// <host>.
	// It's essential to have the <host> NOT EMPTY and LAST.

	public Packet getAppPaths(String sClientId, String sSessId) {
		Packet result = new Packet();
		NamingEnumeration<?> paths;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmHost",
					"pmAtoolPath", "pmRtfedPath", "pmWkfPath", "pmEmlPath",
					"pmExpPath", "pmLauncherPath", "pmMsofficePath",
					"pmMedrecPath", "pmAcctrecPath", "pmWorkflowOld", "pmSchemaPath"});
			paths = ServerConfig.ctx.search(sOsConfigContainerDN, "(objectClass=*)",
					constraints);

			while (paths != null && paths.hasMore()) {
				SearchResult sr = (SearchResult) paths.next();
				StringBuffer sb = new StringBuffer();

				Attribute attr = sr.getAttributes().get("pmAtoolPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmRtfedPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmWkfPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmEmlPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmExpPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmLauncherPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmMsofficePath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmMedrecPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmAcctrecPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmWorkflowOld");
				if(attr !=null){
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmSchemaPath");
				if(attr !=null){
					sb.append((String) attr.get());
				}

				sb.append((String) sr.getAttributes().get("pmHost").get());
				result.addItem(ItemType.RESPONSE_TEXT, sb.toString());
				System.out.println(sb.toString());
			}
			return result;
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
	}


	public Packet addHostApp(String sSessId,
			String sHost, String appName, String appPath,
			String mainClassName, String appPrefix) {
		//Check for existing application with the same id and host combination.
		//If exists, remove the old entry.
		//Add a new entry to the directory.
		System.out.println("ADDHOSTAPP:\nsSessId: " + sSessId + "\nsHost: " + sHost + "\nappName: " + appName + "\nappPath" + appPath + "\nMainClassName: " + mainClassName);
		NamingEnumeration<SearchResult> matchingApps;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"cn", "pmId"});
			String searchString = String.format("(&(pmHost=%s)(pmId=%s))", sHost, appName);
			matchingApps = ServerConfig.ctx.search(sOsConfigContainerDN, searchString, constraints);
			while (matchingApps != null && matchingApps.hasMore()) {
				SearchResult sr = matchingApps.next();
				String sId = sr.getAttributes().get("cn").get().toString();
				String deleteMe = String.format("CN=%s,%s", sId, sOsConfigContainerDN);
				ServerConfig.ctx.destroySubcontext(deleteMe);
			}

			Attributes attrs = new BasicAttributes(true);
			RandomGUID myGUID = new RandomGUID();
			String sId = myGUID.toStringNoDashes();

			attrs.put("objectClass", sOsConfigClass);
			if (appName == null) {
				return failurePacket("Exception in installHostApp: no appname specified.");
			}
			attrs.put("pmId", appName);
			attrs.put("pmHost", sHost);
			if (appPath != null && !appPath.trim().isEmpty()) {
				attrs.put("pmAppPath", appPath);
			}
			if (mainClassName != null && !mainClassName.trim().isEmpty()) {
				attrs.put("pmAppMainClass", mainClassName);
			}
			if (appPrefix != null && !appPrefix.trim().isEmpty()) {
				attrs.put("pmAppPrefix", appPrefix);
			}
			String basePath = sOsConfigContainerDN;
			String objectAddress = String.format("CN=%s,%s", sId, basePath);
			ServerConfig.ctx.bind(objectAddress, null, attrs);
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in engine's installHostApp for "
					+ sHost);
		}

	}

	// Set the paths of applications installed on a host.

	public Packet setHostAppPaths(String sSessId, String sHost,
			String sAtoolPath, String sRtfedPath, String sWkfPath,
			String sEmlPath, String sExpPath, String sLauncherPath,
			String sMsofficePath, String sMedrecPath, String sAcctrecPath 
			,String soldWkfPath, String sSchemaPath) {
		// First delete all records for host.
		NamingEnumeration<?> configs;

		// Look for a OS Config record for the given host.
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			configs = ServerConfig.ctx.search(sOsConfigContainerDN,
					"(pmHost=" + sHost + ")", constraints);
			// Delete all records for the given host.
			while (configs != null && configs.hasMore()) {
				SearchResult sr = (SearchResult) configs.next();
				String sId = (String) sr.getAttributes().get("pmId").get();
				ServerConfig.ctx.destroySubcontext("CN=" + sId + "," + sOsConfigContainerDN);
			}

			// Add a new record for the given host.
			Attributes attrs = new BasicAttributes(true);
			RandomGUID myGUID = new RandomGUID();
			String sId = myGUID.toStringNoDashes();
			attrs.put("objectClass", sOsConfigClass);
			attrs.put("pmId", sId);
			attrs.put("pmHost", sHost);
			if (sAtoolPath != null && sAtoolPath.length() > 0) {
				attrs.put("pmAtoolPath", sAtoolPath);
			}
			if (sRtfedPath != null && sRtfedPath.length() > 0) {
				attrs.put("pmRtfedPath", sRtfedPath);
			}
			if (sWkfPath != null && sWkfPath.length() > 0) {
				attrs.put("pmWkfPath", sWkfPath);
			}
			if (soldWkfPath != null && soldWkfPath.length() > 0) {
				attrs.put("pmWorkflowOld", soldWkfPath);
			}
			if (sEmlPath != null && sEmlPath.length() > 0) {
				attrs.put("pmEmlPath", sEmlPath);
			}
			if (sExpPath != null && sExpPath.length() > 0) {
				attrs.put("pmExpPath", sExpPath);
			}
			if (sLauncherPath != null && sLauncherPath.length() > 0) {
				attrs.put("pmLauncherPath", sLauncherPath);
			}
			if (sMsofficePath != null && sMsofficePath.length() > 0) {
				attrs.put("pmMsofficePath", sMsofficePath);
			}
			if (sMedrecPath != null && sMedrecPath.length() > 0) {
				attrs.put("pmMedrecPath", sMedrecPath);
			}
			if (sAcctrecPath != null && sAcctrecPath.length() > 0) {
				attrs.put("pmAcctrecPath", sAcctrecPath);
			}
			if (sSchemaPath != null && sSchemaPath.length() > 0) {
				attrs.put("pmSchemaPath", sSchemaPath);
			}
			ServerConfig.ctx.bind("CN=" + sId + "," + sOsConfigContainerDN, null, attrs);
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in engine's setAppPaths for "
					+ sHost);
		}
	}

	// Get the paths of the key and trust stores for a user on a host.
	// They are returned in items 0 and 1 of the result.

	public Packet getKStorePaths(String sClientId, String sSessId) {
		NamingEnumeration<?> configs;
		Packet res = new Packet();
		String sUserId = getSessionUserId(sSessId);
		String sHost = getSessionHostName(sSessId);

		// Look for a User Config record for the given host.
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId",
					"pmKsPath", "pmTsPath"});
			configs = ServerConfig.ctx.search(sUserConfigContainerDN, "(&(pmHost=" + sHost
					+ ")(pmUserId=" + sUserId + "))", constraints);
			if (configs == null || !configs.hasMore()) {
				return res;
			}
			SearchResult sr = (SearchResult) configs.next();
			Attribute attr = sr.getAttributes().get("pmKsPath");
			if (attr == null) {
				res.addItem(ItemType.RESPONSE_TEXT, "");
			} else {
				res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());
			}
			attr = sr.getAttributes().get("pmTsPath");
			if (attr == null) {
				res.addItem(ItemType.RESPONSE_TEXT, "");
			} else {
				res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());
			}
			return res;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while looking for the session configuration on "
					+ sHost);
		}
	}


	public Packet getAllKStorePaths(String sClientId, String sSessId) {
		NamingEnumeration<?> configs;
		Packet result = new Packet();

		// Look for a User Config record for the given host.
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmHost",
					"pmUserId", "pmKsPath", "pmTsPath"});
			configs = ServerConfig.ctx.search(sUserConfigContainerDN, "(objectClass=*)",
					constraints);
			while (configs != null && configs.hasMore()) {
				SearchResult sr = (SearchResult) configs.next();
				StringBuffer sb = new StringBuffer();
				Attribute attr = sr.getAttributes().get("pmKsPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				attr = sr.getAttributes().get("pmTsPath");
				if (attr != null) {
					sb.append((String) attr.get());
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				sb.append((String) sr.getAttributes().get("pmHost").get());
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
				String sUserId = (String) sr.getAttributes().get("pmUserId").get();
				String sUserName = getEntityName(sUserId,PM_NODE.USER.value);
				if (sUserName == null) {
					continue;
				}
				sb.append(sUserName);
				result.addItem(ItemType.RESPONSE_TEXT, sb.toString());
				System.out.println(sb.toString());
			}
			return result;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while looking for the keystore paths");
		}
	}

	// Set the paths of the key and trust stores for a user on a host.

	public Packet setKStorePaths(String sSessId, String sUserId, String sHost,
			String sKsPath, String sTsPath) {
		// First delete all records for user and host.
		NamingEnumeration<?> configs;

		// Find and delete the user config records for the given host and user.
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId",
					"pmAtoolPath", "pmRtfedPath"});
			configs = ServerConfig.ctx.search(sUserConfigContainerDN, "(&(pmHost=" + sHost
					+ ")(pmUserId=" + sUserId + "))", constraints);
			while (configs != null && configs.hasMore()) {
				SearchResult sr = (SearchResult) configs.next();
				String sId = (String) sr.getAttributes().get("pmId").get();
				ServerConfig.ctx.destroySubcontext("CN=" + sId + ","
						+ sUserConfigContainerDN);
			}

			// Add a new record for the given host and user.
			Attributes attrs = new BasicAttributes(true);
			RandomGUID myGUID = new RandomGUID();
			String sId = myGUID.toStringNoDashes();
			attrs.put("objectClass", sUserConfigClass);
			attrs.put("pmId", sId);
			attrs.put("pmUserId", sUserId);
			attrs.put("pmHost", sHost);
			if (sKsPath != null && sKsPath.length() > 0) {
				attrs.put("pmKsPath", sKsPath);
			}
			if (sTsPath != null && sTsPath.length() > 0) {
				attrs.put("pmTsPath", sTsPath);
			}
			ServerConfig.ctx.bind("CN=" + sId + "," + sUserConfigContainerDN, null, attrs);
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in engine's setAppPaths for "
					+ sHost);
		}
	}

	// The items in cmdPacket contain:
	// item 0: cmd code "importConfiguration".
	// item 1: <session id>
	// item 2,3...: the script.

	public Packet importConfiguration(String sSessId, Packet cmdPacket) {
		Packet res;
		Vector<String> delayedLines = new Vector<String>();

		for (int i = 2; i < cmdPacket.size(); i++) {
			String sLine = cmdPacket.getStringValue(i);
			if (sLine.length() <= 0
					|| sLine.startsWith(GlobalConstants.PM_IMPORT_COMMENT_START)) {
				continue;
			}

			res =  interpretCmd(sSessId, sLine);
			if (res == null) {
				return failurePacket("Null result returned from command "
						+ sLine);
			}
			if (res.hasError()) {
				delayedLines.addElement(sLine);
				continue;
			}
		}
		for (int i = 0; i < delayedLines.size(); i++) {
			String sLine = delayedLines.get(i);
			res =  interpretCmd(sSessId, sLine);
			if (res == null) {
				return failurePacket("Null result returned from command "
						+ sLine);
			}
			if (res.hasError()) {
				return res;
			}
		}
		return ADPacketHandler.getSuccessPacket();
	}

	public Packet interpretCmd(String sSessId, String sCmd) {
		//				try{
		//
		//					//fw.append(sCmd + "\r\n");
		//					//fw.flush();
		//					//fw.close();
		//				}catch(IOException e){
		//					e.printStackTrace();
		//				}
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sOpCode = pieces[0];
		String sPrimType = pieces[1];

		// Dispatch command according to its code and primary operand type.
		if (sOpCode.equals("add")) {
			if (sPrimType.equals(GlobalConstants.PM_OBJ_CLASS)) {
				return cmdAddObjClass(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_OP)) {
				return cmdAddOp(sSessId, sCmd);
			} else if (sPrimType.equals(PM_NODE.OPSET.value)) {
				System.out.println("adding an opset");
				return cmdAddOpset(sSessId, sCmd);
			} else if (sPrimType.equals(PM_NODE.POL.value)) {
				return cmdAddPc(sSessId, sCmd);
			} else if (sPrimType.equals(PM_NODE.UATTR.value)) {
				return cmdAddUattr(sSessId, sCmd);
			} else if (sPrimType.equals(PM_NODE.USER.value)) {
				return cmdAddUser(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_EMAIL_ACCT)) {
				return cmdAddEmail(sSessId, sCmd);
			} else if (sPrimType.equals(PM_NODE.OATTR.value)) {
				return cmdAddOattr(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_COMPL_OATTR)) {
				return cmdAddComplOattr(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_OBJ)) {
				return cmdAddObj(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_APP_PATH)) {
				return cmdAddApplication(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_KS_PATH)) {
				return cmdAddKStores(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_PROP)) {
				return cmdAddProp(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_DENY)) {
				return cmdAddDeny(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_TEMPLATE)) {
				return cmdAddTpl(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_KEY)) {
				return cmdAddKey(sSessId, sCmd);
			} else if (sPrimType.equals(GlobalConstants.PM_COMPONENTS)) {
				return cmdAddComps(sSessId, sCmd);
			}
		} else if (sOpCode.equals("asg")) {
			return assign(sSessId, sCmd);
		}
		return failurePacket("Invalid command code and/or primary operand type: "
				+ sCmd);
	}

	// Process:
	// add|comps|<components>|b|<record name>
	// <components> is a list of object names separated by ":".
	// <record name> is the name of a record (formerly a composite object).
	// All - the record and the components - must already exist.

	public Packet cmdAddComps(String sSessId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sComps = pieces[2];
		if (sComps == null || sComps.length() <= 0) {
			return ADPacketHandler.getSuccessPacket();
		}
		String sRecId = getEntityId(pieces[4],PM_NODE.OATTR.value);
		if (sRecId == null) {
			return failurePacket("No such record " + pieces[4]);
		}

		boolean first = true;
		StringBuilder sb = new StringBuilder();
		String[] splinters = sComps.split(GlobalConstants.PM_FIELD_DELIM);
		for (String splinter : splinters) {
			String sOattrId = getEntityId(splinter,PM_NODE.OATTR.value);
			if (sOattrId == null) {
				return failurePacket("No such object (attribute) "
						+ splinter);
			}
			if (first) {
				first = false;
				sb.append(sOattrId);
			} else {
				sb.append(GlobalConstants.PM_FIELD_DELIM).append(sOattrId);
			}
		}

		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmComponents", sb.toString()));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sRecId + "," + sObjAttrContainerDN,
					mods);
			return ADPacketHandler.getSuccessPacket();
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Couldn't add the components to object "
					+ pieces[4]);
		}
	}

	// Process:
	// add|tpl|<tpl name>|conts|<containers>
	// add|tpl|<tpl name>|b|<record name>
	//
	// <containers> is a list of container names separated by ":".
	// First command creates a template.
	// The second command adds an existing template to an existing object.

	public Packet cmdAddTpl(String sSessId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sTplName = pieces[2];
		String sType = pieces[3];
		if (sType.equals(GlobalConstants.PM_CONTAINERS)) {
			String containers = pieces[4];
			String[] splinters = containers.split(GlobalConstants.PM_FIELD_DELIM);
			boolean first = true;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < splinters.length; i++) {
				if (first) {
					first = false;
					sb.append(getEntityId(splinters[i],PM_NODE.OATTR.value));
				} else {
					sb.append(GlobalConstants.PM_FIELD_DELIM
							+ getEntityId(splinters[i],PM_NODE.OATTR.value));
				}
			}
			return addTemplate(sSessId, sTplName, sb.toString(), null);
		} else if (sType.equals(PM_NODE.OATTR.value)) {
			String sRecId = getEntityId(pieces[4],PM_NODE.OATTR.value);
			if (sRecId == null) {
				return failurePacket("No such object attribute (record) "
						+ pieces[4]);
			}
			String sTplId = getEntityId(sTplName, GlobalConstants.PM_TEMPLATE);
			if (sTplId == null) {
				return failurePacket("No such template " + sTplName);
			}

			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmTemplateId", sTplId));
			try {
				ServerConfig.ctx.modifyAttributes(
						"CN=" + sRecId + "," + sObjAttrContainerDN, mods);
				return ADPacketHandler.getSuccessPacket();
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Couldn't add the template " + sTplName
						+ " to object attribute (record) " + pieces[4]);
			}
		} else {
			return failurePacket("Wrong type in cmdAddTpl(): " + sType);
		}
	}

	// add|key|<key>|tpl|<tpl name>
	// add|key|<key>|b|<record name>

	public Packet cmdAddKey(String sSessId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sKey = pieces[2];
		String sType = pieces[3];
		if (sType.equals(GlobalConstants.PM_TEMPLATE)) {
			String sTplId = getEntityId(pieces[4], GlobalConstants.PM_TEMPLATE);
			if (sTplId == null) {
				return failurePacket("No template " + pieces[4]);
			}
			return addTemplateKey(sSessId, sTplId, sKey);
		} else if (sType.equals(PM_NODE.OATTR.value)) {
			String sRecId = getEntityId(pieces[4],PM_NODE.OATTR.value);
			if (sRecId == null) {
				return failurePacket("No object attribute (record) "
						+ pieces[4]);
			}
			return addRecordKey(sSessId, sRecId, sKey);
		} else {
			return failurePacket("Trying to add a key to entity of unknown type "
					+ sType);
		}
	}

	// Note that adding a session or process deny via a script makes no sense.
	// Process add|deny|<deny name>|<deny type>|<user or attr>|<user or attr
	// name>|<is intersection>
	// where:
	// <deny type> ::=
	// GlobalConstants.PM_DENY_USER_ID|PM_DENY_INTRA_SESSION|PM_DENY_ACROSS_SESSIONS
	// <user or attr> ::= u|a
	// <is intersection> ::= yes|no

	public Packet cmdAddDeny(String sSessId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sDenyName = pieces[2];
		String sDenyType = pieces[3];
		String sNameType = pieces[4];
		String sName = pieces[5];
		String sIsInters = pieces[6];
		String sId = getEntityId(sName, sNameType);
		if (sId == null) {
			return failurePacket("No entity " + sName + " of type " + sNameType);
		}
		return addDenyInternal(sDenyName, sDenyType, sName, null, null, null,
				null, sIsInters.equalsIgnoreCase("yes"));
	}

	// Process add|prop|<property>|{a|b|p|v}|<attribute or policy or value>

	public Packet cmdAddProp(String sSessId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sProp = pieces[2];
		String sType = pieces[3];
		String sName = pieces[4];
		if (sType.equals(GlobalConstants.PM_VALUE)) {
			return setProperty(null, sProp, sName);
		}
		String sId = getEntityId(sName, sType);
		if (sId == null) {
			return failurePacket("No entity " + sName + " of type " + sType);
		}
		return addPropInternal(sId, sType, sProp);
	}

	// Process add|ks|<key store path>|<trust store path>|h|<host>|u|<user>

	public Packet cmdAddKStores(String sSessId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sKsPath = pieces[2];
		String sTsPath = pieces[3];
		String sHost = pieces[5];
		String sUser = pieces[7];
		String sUserId = getEntityId(sUser,PM_NODE.USER.value);
		return setKStorePaths(sSessId, sUserId, sHost, sKsPath, sTsPath);
	}


	public Packet cmdAddApplication(String sSessId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String appName = pieces[2];
		String appPath = pieces[3];
		String appMainClass = pieces[4];
		String appPrefix = pieces[5];
		String sHost = pieces[6];
		return addHostApp(sSessId, sHost, appName, appPath, appMainClass, appPrefix);
	}


	// Process add|ob|<object>|<class>|<inh>|<host or orig name>|<path or
	// ignored>|p|<policy class>
	// add|ob|<object>|<class>|<inh>|<host or orig name>|<path or
	// ignored>|c|<connector>
	// add|ob|<object>|<class>|<inh>|<host or orig name>|<path or
	// ignored>|b|<object attribute>
	// <inh> is "yes" or "no".
	// <class> is File, Directory, User, User attribute, Object, Object
	// attribute,
	// Policy class, Connector, Operation set, or a custom class name.

	public Packet cmdAddObj(String sClientId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sObj = pieces[2];
		String sClass = pieces[3];
		String sInh = pieces[4];
		String sHost = pieces[5];
		String sPath = pieces[6];
		String sToType = pieces[7];
		String sToName = pieces[8];
		String sToId = getEntityId(sToName, sToType);
		String sOrigId = null;
		String sOrigName = null;




		if (sToId == null) {
			return failurePacket("No entity " + sToName + " of type " + sToType);
		}
		if (sClass.equals(GlobalConstants.PM_CLASS_USER_NAME)
				|| sClass.equals(GlobalConstants.PM_CLASS_UATTR_NAME)
				|| sClass.equals(GlobalConstants.PM_CLASS_OBJ_NAME)
				|| sClass.equals(GlobalConstants.PM_CLASS_OATTR_NAME)
				|| sClass.equals(GlobalConstants.PM_CLASS_POL_NAME)
				|| sClass.equals(GlobalConstants.PM_CLASS_CONN_NAME)
				|| sClass.equals(GlobalConstants.PM_CLASS_OPSET_NAME)) {
			sOrigName = sHost;
			sOrigId = getEntityId(sOrigName, classToType(sClass));
			if (sOrigId == null) {
				return failurePacket("No entity " + sHost + " of class "
						+ sClass);
			}
		}
		System.out.println("===========cmdAddObj===========");
		System.out.println(sObj);
		System.out.println(sClass);
		System.out.println(sInh );
		System.out.println(sHost );
		System.out.println(sPath );
		System.out.println(sToType);
		System.out.println(sToName);
		System.out.println(sToId);
		System.out.println(sOrigId);
		System.out.println(sOrigName);
		try {
			return addObjectInternal(sObj, null, null, sObj, sObj, sToId,
					sToType, sClass, null, sHost, sPath, sOrigName, sOrigId,
					(sInh.equalsIgnoreCase("yes")), null, null, null, null,
					null, null, null);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while adding object: "
					+ e.getMessage());
		}
	}

	// Process add|b|<object attribute>|c|<connector>
	// add|b|<object attribute>|p|<policy class>
	// add|b|<object attribute>|b|<object attribute>
	// add|b|<object attribute>|deny|<deny constraint>

	public Packet cmdAddOattr(String sClientId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sOattr = pieces[2];
		String sToType = pieces[3];
		String sToName = pieces[4];
		String sToId = getEntityId(sToName, sToType);
		if (sToId == null) {
			return failurePacket("No entity " + sToName + " of type " + sToType);
		}

		// Note that in the case of adding the object attribute to a deny, the
		// object attribute must already exist!
		if (sToType.equalsIgnoreCase(GlobalConstants.PM_DENY)) {
			String sOattrId = getEntityId(sOattr,PM_NODE.OATTR.value);
			if (sOattrId == null) {
				return failurePacket("No object attribute " + sOattr);
			}
			return addDenyInternal(sToName, null, null, null, null, sOattr,
					sOattrId, false);
		} else {
			return addOattrInternal(sOattr, null, sOattr, sOattr, sToId,
					sToType, null, null);
		}
	}

	// Process add|cb|<object attribute>|deny|<deny constraint>,
	// i.e., the complement of an object attribute.

	public Packet cmdAddComplOattr(String sClientId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sToType = pieces[3];
		String sToName = pieces[4];
		if (!sToType.equalsIgnoreCase(GlobalConstants.PM_DENY)) {
			return failurePacket("Entity " + sToName
					+ " is not a deny constraint!");
		}
		String sOattrId = getEntityId(pieces[2],PM_NODE.OATTR.value);
		if (sOattrId == null) {
			return failurePacket("No object attribute " + pieces[2]);
		}
		String sOattr = "!" + pieces[2];
		return addDenyInternal(sToName, null, null, null, null, sOattr,
				sOattrId, false);
	}

	// Process add|eml|<coming from>|<email addr>|<pop server>|<smtp server>|
	// <acct name>|<password>|u|<user name>

	public Packet cmdAddEmail(String sClientId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sToType = pieces[8];// must be a user
		String sToName = pieces[9];
		String sToId = getEntityId(sToName, sToType);
		if (sToId == null) {
			return failurePacket("No entity " + sToName + " of type " + sToType);
		}
		return addEmailAcctInternal(sToName, pieces[2], pieces[3], pieces[4],
				pieces[5], pieces[6], pieces[7]);
	}

	// Process add|u|<user>|fn|<full name>|c|<connector>
	// add|u|<user>|fn|<full name>|a|<user attribute>

	public Packet cmdAddUser(String sClientId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sUser = pieces[2];
		String sFull = pieces[4];
		String sToType = pieces[5];
		String sToName = pieces[6];
		String sToId = getEntityId(sToName, sToType);
		if (sToId == null) {
			return failurePacket("No entity " + sToName + " of type " + sToType);
		}
		return addUserInternal(sUser, null, sFull, sUser, sUser, sToId, sToType);
	}

	// Process add|a|<user attribute>|c|<connector>
	// add|a|<user attribute>|p|<policy class>
	// add|a|<user attribute>|a|<user attribute>
	// add|a|<user attribute>|u|<user>


	public Packet cmdAddUattr(String sSessId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sUattr = pieces[2];
		String sToType = pieces[3];
		String sToName = pieces[4];
		String sToId = getEntityId(sToName, sToType);
		if (sToId == null) {
			return failurePacket("No entity " + sToName + " of type " + sToType);
		}

		return addUattrInternal(sUattr, sUattr, sUattr, sToId, sToType, null);
	}

	// Process add|p|<policy class>|c|<connector>

	public Packet cmdAddPc(String sClientId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sPol = pieces[2];
		return addPcInternal(sPol, null, sPol, sPol, null);
	}

	// Process add|s|<operation set>|oc|<object class>|a|<user attribute>,
	// add|s|<operation set>|oc|<object class>|b|<object attribute>, and
	// add|s|<operation set>|oc|<object class>|c|<connector>.

	@SuppressWarnings("unused")
	public Packet cmdAddOpset(String sClientId, String sCmd) {
		System.out.println("cmdAddOpset called");
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		System.out.println("pieces: " + Arrays.toString(pieces));
		String sOpset = pieces[2];
		String sClass = pieces[4];// ignored
		String sToType = pieces[5];
		String sToName = pieces[6];
		System.out.println("sOpset: " + sOpset);
		System.out.println("sClass: " + sClass);
		System.out.println("sToType: " + sToType);
		System.out.println("sToName: " + sToName);
		String sToId = getEntityId(sToName, sToType);
		if (sToId == null) {
			return failurePacket("No entity " + sToName + " of type " + sToType);
		}
		if (sToType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			System.out.println("addOpsetaAndOpInternal called for uattr");
			return addOpsetAndOpInternal(sOpset, null, sOpset, sOpset, null,
					sToId, sToType, null, null);
		} else if (sToType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sToType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			System.out.println("addOpsetaAndOpInternal called for oattr or conn");
			return addOpsetAndOpInternal(sOpset, null, sOpset, sOpset, null,
					null, null, sToId, sToType);
		} else {
			return failurePacket("Incorrect base type in command " + sCmd);
		}
	}

	// Process add|oc|<object class>

	public Packet cmdAddObjClass(String sClientId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sClass = pieces[2];
		if (sClass == null || sClass.length() == 0) {
			return failurePacket("Invalid or null object class name");
		}
		return addObjClassAndOp(sClientId, sClass, sClass, sClass, null);
	}

	// Process add|op|<operation>|oc|<object class>
	// add|op|<operation>|s|<operation set>
	// add|op|<operation>|deny|<deny constraint>

	public Packet cmdAddOp(String sClientId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sOp = pieces[2];
		String sToType = pieces[3];
		String sToName = pieces[4];
		if (sToType.equals(GlobalConstants.PM_OBJ_CLASS)) {
			return addObjClassAndOp(sClientId, sToName, sToName, sToName, sOp);
		} else if (sToType.equals(PM_NODE.OPSET.value)) {
			return addOpsetAndOpInternal(sToName, null, null, null, sOp, null,
					null, null, null);
		} else if (sToType.equals(GlobalConstants.PM_DENY)) {
			return addDenyInternal(sToName, null, null, null, sOp, null, null,
					false);
		}
		return failurePacket("Not yet implemented");
	}

	// Process assignments:
	// asg|u|<user>|a|<user attribute>
	//
	// asg|a|<user attribute>|a|<user attribute>
	// asg|a|<user attribute>|p|<policy class>
	// asg|a|<user attribute>|s|<operation set>
	//
	// asg|b|<object attribute>|b|<object attribute>
	// asg|b|<object attribute>|p|<policy class>
	//
	// asg|s|<operation set>|b|<object attribute>

	public Packet assign(String sClientId, String sCmd) {
		String[] pieces = sCmd.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
		String sType = pieces[1];
		String sName = pieces[2];
		String sToType = pieces[3];
		String sToName = pieces[4];
		String sId = getEntityId(sName, sType);
		if (sId == null) {
			return failurePacket("No entity " + sName + " of type " + sType);
		}
		String sToId = getEntityId(sToName, sToType);
		if (sToId == null) {
			return failurePacket("No entity " + sToName + " of type " + sToType);
		}
		return assignInternal(sId, sType, sToId, sToType);
	}

	/**
	 * @uml.property  name="savedRecords"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
	HashSet<String> savedRecords = null;


	public Packet export(String sClientId, String sSessId) {
		if (savedRecords == null) {
			savedRecords = new HashSet<String>();
		}
		savedRecords.clear();

		HashSet<String> visitedSet = new HashSet<String>();
		ArrayList<QueueElement> queue = new ArrayList<QueueElement>();
		QueueElement qe, crtQe;
		String sId = null;
		Packet result = new Packet();
		Packet ar, ar2;

		// Export the object classes.
		try {
			ar =  getObjClasses(sClientId);
			if (ar == null) {
				return failurePacket("Null result to getObjClasses");
			}
			for (int i = 0; i < ar.size(); i++) {
				String sClass = ar.getStringValue(i);

				// Exclude the predefined classes.
				if (sClass.equals(GlobalConstants.PM_CLASS_FILE_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_DIR_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_USER_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_UATTR_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_OBJ_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_OATTR_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_POL_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_CONN_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_OPSET_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_CLASS_NAME)
						|| sClass.equals(GlobalConstants.PM_CLASS_ANY_NAME)) {
					continue;
				}

				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_OBJ_CLASS
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sClass);
				ar2 =  getObjClassOps(sClientId, sClass);
				for (int j = 0; j < ar2.size() - 1; j++) {
					String sOp = ar2.getStringValue(j);
					result.addItem(ItemType.RESPONSE_TEXT, "add"
							+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_OP + GlobalConstants.PM_ALT_FIELD_DELIM
							+ sOp + GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_OBJ_CLASS
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sClass);
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in getObjClasses or getObjClassOps: "
					+ e.getMessage());
		}

		// Export the main PM graph.

		// Start by inserting the connector in the queue.
		crtQe = new QueueElement(PM_NODE.CONN.value, GlobalConstants.PM_CONNECTOR_ID, 0);
		queue.add(crtQe);

		try {
			while (!queue.isEmpty()) {
				// Extract the next node from the queue.
				crtQe = queue.remove(0);

				if (ServerConfig.debugFlag) {
					System.out.println("Out of queue: "
							+ getEntityName(crtQe.getId(), crtQe.getType())
							+ " type " + crtQe.getType());
				}
				// Examine its type.
				if (crtQe.getType().equalsIgnoreCase(PM_NODE.CONN.value)) {
					// The current node is a CONNECTOR NODE.

					// Process the policy classes assigned to the connector
					// node.
					Attribute attr = getFromPolicies(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							String sName = getEntityName(sId,PM_NODE.POL.value);

							// Don't list the "admin" policy class, but insert
							// it into the queue.
							if (!visitedSet.contains(sId)) {
								qe = new QueueElement(PM_NODE.POL.value, sId, 0);
								queue.add(qe);
								if (sName.equals(GlobalConstants.PM_ADMIN_NAME)) {
									continue;
								}
								exportPc(result, sId);
								visitedSet.add(sId);
							} else {
								result.addItem(ItemType.RESPONSE_TEXT, "asg"
										+ GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.POL.value
										+ GlobalConstants.PM_ALT_FIELD_DELIM + sName
										+ GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.CONN.value
										+ GlobalConstants.PM_ALT_FIELD_DELIM
										+ GlobalConstants.PM_CONNECTOR_NAME);
							}
						}
					}

					// Process the user attributes assigned to the connector
					// node.
					attr = getFromUserAttrs(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							if (!visitedSet.contains(sId)) {
								qe = new QueueElement(PM_NODE.UATTR.value, sId, 0);
								queue.add(qe);
								exportUattr(result, sId,PM_NODE.CONN.value,
										GlobalConstants.PM_CONNECTOR_NAME);
								visitedSet.add(sId);
							} else {
								result.addItem(
										ItemType.RESPONSE_TEXT,
										"asg"
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.UATTR.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ getEntityName(sId,
														PM_NODE.UATTR.value)
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.CONN.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ GlobalConstants.PM_CONNECTOR_NAME);
							}
						}
					}

					// Process the users assigned to the connector node.
					attr = getFromUsers(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							if (!visitedSet.contains(sId)) {
								qe = new QueueElement(PM_NODE.USER.value, sId, 0);
								queue.add(qe);
								exportUser(result, sId,PM_NODE.CONN.value,
										GlobalConstants.PM_CONNECTOR_NAME);
								visitedSet.add(sId);
							} else {
								result.addItem(
										ItemType.RESPONSE_TEXT,
										"asg"
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.USER.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ getEntityName(sId,
														PM_NODE.USER.value)
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.CONN.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ GlobalConstants.PM_CONNECTOR_NAME);
							}
						}
					}

					// Process the object attributes assigned to the connector
					// node.
					attr = getFromObjAttrs(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							if (!visitedSet.contains(sId)) {
								qe = new QueueElement(PM_NODE.OATTR.value, sId, 0);
								queue.add(qe);
								exportOattr(result, sId,PM_NODE.CONN.value,
										GlobalConstants.PM_CONNECTOR_NAME);
								visitedSet.add(sId);
							} else {
								result.addItem(
										ItemType.RESPONSE_TEXT,
										"asg"
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.OATTR.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ getEntityName(sId,
														PM_NODE.OATTR.value)
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.CONN.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ GlobalConstants.PM_CONNECTOR_NAME);
							}
						}
					}

					// Process the operation sets assigned to the connector
					// node.
					attr = getFromOpsets(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							String sName = getEntityName(sId,PM_NODE.OPSET.value);

							if (!visitedSet.contains(sId)) {
								qe = new QueueElement(PM_NODE.OPSET.value, sId, 0);
								queue.add(qe);

								// Get its class.
								String sClass = "Ignored";
								result.addItem(ItemType.RESPONSE_TEXT, "add"
										+ GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.OPSET.value
										+ GlobalConstants.PM_ALT_FIELD_DELIM + sName
										+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_OBJ_CLASS
										+ GlobalConstants.PM_ALT_FIELD_DELIM + sClass
										+ GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.CONN.value
										+ GlobalConstants.PM_ALT_FIELD_DELIM
										+ GlobalConstants.PM_CONNECTOR_NAME);
								visitedSet.add(sId);

								// Get its operations.
								ar =  getOpsetOps(sClientId, sName);
								if (ar == null) {
									return failurePacket("Null result from getOpsetOps");
								}
								for (int i = 0; i < ar.size(); i++) {
									String sOp = ar.getStringValue(i);
									result.addItem(ItemType.RESPONSE_TEXT,
											"add" + GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_OP
											+ GlobalConstants.PM_ALT_FIELD_DELIM + sOp
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OPSET.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName);
								}
							} else {
								result.addItem(ItemType.RESPONSE_TEXT, "asg"
										+ GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.OPSET.value
										+ GlobalConstants.PM_ALT_FIELD_DELIM + sName
										+ GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.CONN.value
										+ GlobalConstants.PM_ALT_FIELD_DELIM
										+ GlobalConstants.PM_CONNECTOR_NAME);
							}
						}
					}
				} else if (crtQe.getType().equalsIgnoreCase(PM_NODE.POL.value)) {

					// The current node is a POLICY CLASS node.
					String sCrtName = getEntityName(crtQe.getId(),
							crtQe.getType());

					// Process the user attributes assigned to the policy class.
					Attribute attr = getFromUserAttrs(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							String sName = getEntityName(sId,PM_NODE.UATTR.value);

							// The "superAdmin" attribute has special treatment:
							// Do not generate "add superAdmin" to any entity.
							// Generate "asg superAdmin" to any pc such that
							// superAdmin-->pc
							// and pc != admin.
							// Insert superAdmin into the queue.
							if (sName.equals(GlobalConstants.PM_SUPER_ADMIN_NAME)) {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.UATTR.value, sId, 0);
									queue.add(qe);
									visitedSet.add(sId);
								}
								if (!sCrtName.equals(GlobalConstants.PM_ADMIN_NAME)) {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.UATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.POL.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
								}
							} else {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.UATTR.value, sId, 0);
									queue.add(qe);
									exportUattr(result, sId,PM_NODE.POL.value,
											sCrtName);
									visitedSet.add(sId);
								} else {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.UATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.POL.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
								}
							}
						}
					}

					// Process the object attributes assigned to the policy
					// class.
					attr = getFromObjAttrs(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							String sName = getEntityName(sId,PM_NODE.OATTR.value);

							// The "everything" object (attribute) has a special
							// treatment.
							// Do not generate "add everything" to any entity.
							// Generate "asg everything" to the right entities
							// except the "admin" pc.
							// Insert "everything" into the queue.
							if (sName.equals(GlobalConstants.PM_EVERYTHING_NAME)) {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.OATTR.value, sId, 0);
									queue.add(qe);
									visitedSet.add(sId);
								}
								if (!sCrtName.equals(GlobalConstants.PM_ADMIN_NAME)) {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.POL.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
								}
							} else {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.OATTR.value, sId, 0);
									queue.add(qe);
									exportOattr(result, sId,PM_NODE.POL.value,
											sCrtName);
									visitedSet.add(sId);
								} else {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.POL.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
								}
							}
						}
					}

				} else if (crtQe.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {

					// The current node is a USER ATTRIBUTE node.
					String sCrtName = getEntityName(crtQe.getId(),
							crtQe.getType());

					// Process the users assigned to this user attribute.
					Attribute attr = getFromUsers(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							String sName = getEntityName(sId,PM_NODE.USER.value);

							// The "super" user has a special treatment.
							// Do not generate "add super" to any entity.
							// Generate "asg super" to the correct entities
							// except the
							// "superAdmin" user attribute.
							// Insert "super" into the queue.
							if (sName.equals(GlobalConstants.PM_SUPER_NAME)) {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.USER.value, sId, 0);
									queue.add(qe);
									visitedSet.add(sId);
								}
								if (!sCrtName.equals(GlobalConstants.PM_SUPER_ADMIN_NAME)) {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.USER.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.UATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
								}
							} else {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.USER.value, sId, 0);
									queue.add(qe);
									exportUser(result, sId,PM_NODE.UATTR.value,
											sCrtName);
									visitedSet.add(sId);
								} else {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.USER.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.UATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
								}
							}
						}
					}

					// Process the user attributes assigned to this user
					// attribute.
					attr = getFromAttrs(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							if (!visitedSet.contains(sId)) {
								qe = new QueueElement(PM_NODE.UATTR.value, sId,
										crtQe.getLevel() + 1);
								queue.add(qe);
								exportUattr(result, sId,PM_NODE.UATTR.value,
										sCrtName);
								visitedSet.add(sId);
							} else {
								result.addItem(
										ItemType.RESPONSE_TEXT,
										"asg"
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.UATTR.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ getEntityName(sId,
														PM_NODE.UATTR.value)
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.UATTR.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM + sCrtName);
							}
						}
					}

					// Process the operation sets assigned to this user
					// attribute.
					attr = getToOpsets(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							String sName = getEntityName(sId,PM_NODE.OPSET.value);

							// The "all ops" operation set has a special
							// treatment.
							// Do not generate "add allops" to any entity.
							// Generate "asg entity to allops" for the right
							// entities except the
							// "superAdmin" user attribute.
							// Insert "allops" into the queue.
							if (sName.equals(GlobalConstants.PM_ALL_OPS_NAME)) {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.OPSET.value, sId, 0);
									queue.add(qe);
									visitedSet.add(sId);
								}
								if (!sCrtName.equals(GlobalConstants.PM_SUPER_ADMIN_NAME)) {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.UATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OPSET.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName);
								}
							} else {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.OPSET.value, sId, 0);
									queue.add(qe);
									// Get its class.
									String sClass = "Ignored";
									String sOpsetName = getEntityName(sId,
											PM_NODE.OPSET.value);
									result.addItem(ItemType.RESPONSE_TEXT,
											"add" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OPSET.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sOpsetName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ GlobalConstants.PM_OBJ_CLASS
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sClass
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.UATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
									visitedSet.add(sId);

									// Get its operations.
									ar =  getOpsetOps(sClientId, sName);
									if (ar == null) {
										return failurePacket("Null result from getOpsetOps");
									}
									for (int i = 0; i < ar.size(); i++) {
										String sOp = ar.getStringValue(i);
										result.addItem(ItemType.RESPONSE_TEXT,
												"add" + GlobalConstants.PM_ALT_FIELD_DELIM
												+ GlobalConstants.PM_OP
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ sOp
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.OPSET.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ sName);
									}
								} else {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.UATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OPSET.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName);
								}
							}
						}
					}

				} else if (crtQe.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {

					// The current node is an OBJECT ATTRIBUTE node.
					String sCrtName = getEntityName(crtQe.getId(),
							crtQe.getType());

					// Process the object attributes assigned to this object
					// attribute.
					Attribute attr = getFromAttrs(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							if (!visitedSet.contains(sId)) {
								qe = new QueueElement(PM_NODE.OATTR.value, sId, 0);
								queue.add(qe);
								exportOattr(result, sId,PM_NODE.OATTR.value,
										sCrtName);
								visitedSet.add(sId);
							} else {
								result.addItem(
										ItemType.RESPONSE_TEXT,
										"asg"
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.OATTR.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ getEntityName(sId,
														PM_NODE.OATTR.value)
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.OATTR.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM + sCrtName);
							}
						}
					}

					// Process the operation sets assigned to this object
					// attribute.
					attr = getFromOpsets(crtQe);
					if (attr != null) {
						for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
							sId = (String) attrEnum.next();
							String sName = getEntityName(sId,PM_NODE.OPSET.value);

							// The "all ops" operation set has a special
							// treatment.
							// Do not generate "add allops" to any entity.
							// Generate "asg allops" to the right entities
							// except the
							// "everything" user attribute.
							// Insert "allops" into the queue.
							if (sName.equals(GlobalConstants.PM_ALL_OPS_NAME)) {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.OPSET.value, sId, 0);
									queue.add(qe);
									visitedSet.add(sId);
								}
								if (!sCrtName.equals(GlobalConstants.PM_EVERYTHING_NAME)) {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OPSET.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
								}
							} else {
								if (!visitedSet.contains(sId)) {
									qe = new QueueElement(PM_NODE.OPSET.value, sId, 0);
									queue.add(qe);
									// Get its class.
									String sClass = "Ignored";
									result.addItem(ItemType.RESPONSE_TEXT,
											"add" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OPSET.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ GlobalConstants.PM_OBJ_CLASS
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sClass
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
									visitedSet.add(sId);

									// Get its operations.
									ar =  getOpsetOps(sClientId, sName);
									if (ar == null) {
										return failurePacket("Null result from getOpsetOps");
									}
									for (int i = 0; i < ar.size(); i++) {
										String sOp = ar.getStringValue(i);
										result.addItem(ItemType.RESPONSE_TEXT,
												"add" + GlobalConstants.PM_ALT_FIELD_DELIM
												+ GlobalConstants.PM_OP
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ sOp
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+PM_NODE.OPSET.value
												+ GlobalConstants.PM_ALT_FIELD_DELIM
												+ sName);
									}
								} else {
									result.addItem(ItemType.RESPONSE_TEXT,
											"asg" + GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OPSET.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sName
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+PM_NODE.OATTR.value
											+ GlobalConstants.PM_ALT_FIELD_DELIM
											+ sCrtName);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in export: " + e.getMessage());
		}

		// Export the applications.
		ar =  getAppPaths(sClientId, sSessId);
		if (ar == null) {
			return failurePacket("Null result from getAppPaths");
		}
		if (ar.hasError()) {
			return ar;
		}
		for (int i = 0; i < ar.size(); i++) {
			String sLine = ar.getStringValue(i);

			try {
				// An item of the result contains:
				// <admin tool path>|<editor path>|<wkf path>|<email path>|
				// <exporter path>|<openoffice path>|<msoffice path>|
				// <mr editor>|<acct editor>|<host>.
				// At least the host is not empty.
				String[] pieces = sLine.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				System.out.println("got " + pieces.length + " pieces");

				// pieces[0]=admin tool path,
				// pieces[1]=editor path,
				// pieces[2]=wkf path,
				// pieces[3]=email path,
				// pieces[4]=exporter path,
				// pieces[5]=openoffice path,
				// pieces[6]=msoffice path,
				// pieces[7]=mr editor path,
				// pieces[8]=acct editor path,
				// pieces[9]=host.
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_APP_PATH + GlobalConstants.PM_ALT_FIELD_DELIM
						+ pieces[0] + GlobalConstants.PM_ALT_FIELD_DELIM + pieces[1]
								+ GlobalConstants.PM_ALT_FIELD_DELIM + pieces[2] + GlobalConstants.PM_ALT_FIELD_DELIM
								+ pieces[3] + GlobalConstants.PM_ALT_FIELD_DELIM + pieces[4]
										+ GlobalConstants.PM_ALT_FIELD_DELIM + pieces[5] + GlobalConstants.PM_ALT_FIELD_DELIM
										+ pieces[6] + GlobalConstants.PM_ALT_FIELD_DELIM + pieces[7]
												+ GlobalConstants.PM_ALT_FIELD_DELIM + pieces[8] + GlobalConstants.PM_ALT_FIELD_DELIM
												+ pieces[9] + GlobalConstants.PM_ALT_FIELD_DELIM 
												+ GlobalConstants.PM_HOST + GlobalConstants.PM_ALT_FIELD_DELIM + pieces[10]);
			} catch (Exception ee) {
				ee.printStackTrace();
				return failurePacket("Exception while exporting application paths: "
						+ ee.getMessage());
			}
		}

		// Export the keystore paths.
		ar =  getAllKStorePaths(sClientId, sSessId);
		if (ar == null) {
			return failurePacket("Null result from getAllKStorePaths");
		}
		if (ar.hasError()) {
			return ar;
		}
		for (int i = 0; i < ar.size(); i++) {
			String sLine = ar.getStringValue(i);

			try {
				// An item of the result contains: <key store path>|<trust store
				// path>|<host>|<user>.
				// At least the host and the user names are not empty.
				String[] pieces = sLine.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				System.out.println("got " + pieces.length + " pieces");

				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_KS_PATH + GlobalConstants.PM_ALT_FIELD_DELIM
						+ pieces[0] + GlobalConstants.PM_ALT_FIELD_DELIM + pieces[1]
								+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_HOST + GlobalConstants.PM_ALT_FIELD_DELIM
								+ pieces[2] + GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.USER.value
								+ GlobalConstants.PM_ALT_FIELD_DELIM + pieces[3]);
			} catch (Exception ee) {
				ee.printStackTrace();
				return failurePacket("Exception while exporting key store paths: "
						+ ee.getMessage());
			}
		}

		// Export denies.
		ar =  getDenies(sSessId);
		if (ar == null) {
			return failurePacket("Null result from getDenies");
		}
		if (ar.hasError()) {
			return ar;
		}
		for (int i = 0; i < ar.size(); i++) {
			String sLine = ar.getStringValue(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			// pieces[0] is the deny name, pieces[1] is the deny id.
			String sDenyName = pieces[0];
			String sDenyId = pieces[1];
			ar2 =  getDenyInfo(sSessId, sDenyId);

			// The deny may have been for a session that does not exist anymore,
			// skip it.
			if (ar2.hasError()) {
				continue;
			}

			// The information returned by getDenyInfo has the following format:
			// item 0: <deny name>:<deny id>
			// item 1: <deny type>:<user or attribute name>:<user or attribute
			// id>:<is intersection>
			// item 2: <operation count, opcount>
			// items 3 through 3 + opcount - 1: <operation>
			// item 3 + opcount: <container count, contcount>
			// item 3 + opcount + 1 through 3 + opcount + 1 + contcount - 1:
			// <container name>:<container id>
			sLine = ar2.getStringValue(1);
			System.out.println("Deny type, name, id, is intersection: " + sLine);
			pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			String sDenyType = pieces[0];
			String sUserOrAttrName = pieces[1];
			String sIsInters = pieces[3];
			String sNameType = (sDenyType.equalsIgnoreCase(GlobalConstants.PM_DENY_USER_ID) ?PM_NODE.USER.value
					:PM_NODE.UATTR.value);

			// Write cmd to create deny with name, type, user or attribute name,
			// and whether is an intersection or union of containers.
			try {
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_DENY + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sDenyName + GlobalConstants.PM_ALT_FIELD_DELIM + sDenyType
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sNameType + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sUserOrAttrName + GlobalConstants.PM_ALT_FIELD_DELIM + sIsInters);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Exception when building result packet");
			}

			// Write cmd to add denied operations.
			int opCount = Integer.valueOf(ar2.getStringValue(2)).intValue();
			for (int j = 3; j < 3 + opCount; j++) {
				String sOp = ar2.getStringValue(j);
				try {
					result.addItem(ItemType.RESPONSE_TEXT, "add"
							+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_OP + GlobalConstants.PM_ALT_FIELD_DELIM
							+ sOp + GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_DENY
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sDenyName);
				} catch (Exception e) {
					e.printStackTrace();
					return failurePacket("Exception when building result packet");
				}
			}
			// Write cmd to add denied containers.
			int contCount = Integer.valueOf(ar2.getStringValue(3 + opCount)).intValue();
			for (int j = 4 + opCount; j < 4 + opCount + contCount; j++) {
				sLine = ar2.getStringValue(j);
				System.out.println("cont name, cont id: " + sLine);
				pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
				// pieces[0] is the container name, possibly prefixed with the
				// symbol '!'
				// indicating complement.
				String sCont;
				String sContType;
				if (pieces[0].startsWith("!")) {
					sCont = pieces[0].substring(1);
					sContType = GlobalConstants.PM_COMPL_OATTR;
				} else {
					sCont = pieces[0];
					sContType =PM_NODE.OATTR.value;
				}
				try {
					result.addItem(ItemType.RESPONSE_TEXT, "add"
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sContType
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sCont + GlobalConstants.PM_ALT_FIELD_DELIM
							+ GlobalConstants.PM_DENY + GlobalConstants.PM_ALT_FIELD_DELIM + sDenyName);
				} catch (Exception e) {
					e.printStackTrace();
					return failurePacket("Exception when building result packet");
				}
			}
		}


		// Export the record templates.
		exportTemplates(result);

		// The records (some object containers) were exported as all other
		// objects attributes,
		// but incompletely. Now we export their templates, containers and keys.
		// The record ids
		// were saved in savedRecords (the ids were those of the associated
		// oattrs).
		exportRecordProperties(result);

		// Export the properties (from the Property Container).
		exportProperties(result);

		return result;
	}


	public void exportProperties(Packet result) {
		Packet props =  getProperties(null);
		if (props == null || props.size() <= 0) {
			return;
		}
		if (props.hasError()) {
			return;
		}

		try {
			for (int i = 0; i < props.size(); i++) {
				String sLine = props.getStringValue(i);
				String[] pieces = sLine.split(GlobalConstants.PM_PROP_DELIM);
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_PROP + GlobalConstants.PM_ALT_FIELD_DELIM
						+ pieces[0] + GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_VALUE
						+ GlobalConstants.PM_ALT_FIELD_DELIM + pieces[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public Packet testExportRecords() {
		if (savedRecords == null) {
			savedRecords = new HashSet<String>();
		} else {
			savedRecords.clear();
		}

		Packet ar =  getRecords(null, null, null);
		for (int i = 0; i < ar.size() - 1; i++) {
			String sLine = ar.getStringValue(i);
			String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
			savedRecords.add(pieces[1]);
		}

		Packet result = new Packet();
		exportTemplates(result);
		exportRecordProperties(result);

		return result;
	}


	public void exportRecordProperties(Packet result) {
		Iterator<String> setiter = savedRecords.iterator();
		try {
			while (setiter.hasNext()) {
				String sCompoId = setiter.next();
				System.out.println("Exporting record with id " + sCompoId);
				Packet info =  getRecordInfo(null, sCompoId);
				if (info.hasError()) {
					System.out.println("Error in getRecordInfo called from exportRecordProperties:");
					System.out.println(info.getErrorMessage());
					continue;
				}

				// Item 0: <name>:<id>
				// Item 1: <template name>:<template id>
				// Item 2: <comp count>
				// Items 3 to 3 + <comp count> - 1: <comp name>:<comp id>
				// Item 3 + <comp count>: <key count>
				// Items 3 + <comp count> + 1 to 3 + <comp count> + 1 + <key
				// count> - 1: <key name>=<key value>
				// The <comp id> is the id of the object attribute associated
				// with a
				// component object.
				String sLine = info.getStringValue(0);
				String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
				String sRecName = pieces[0];

				// Get the object's template and generate the add template
				// command.
				sLine = info.getStringValue(1);
				pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_TEMPLATE + GlobalConstants.PM_ALT_FIELD_DELIM
						+ pieces[0] + GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.OATTR.value
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sRecName);

				// Get the object's components and generate the add components
				// command.
				sLine = info.getStringValue(2);
				int nComp = Integer.valueOf(sLine).intValue();
				StringBuffer sb = new StringBuffer();
				boolean first = true;
				for (int i = 0; i < nComp; i++) {
					sLine = info.getStringValue(3 + i);
					pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
					if (first) {
						first = false;
						sb.append(pieces[0]);
					} else {
						sb.append(GlobalConstants.PM_FIELD_DELIM + pieces[0]);
					}
				}
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_COMPONENTS
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sb.toString()
						+ GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.OATTR.value
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sRecName);

				// Get the object's keys and generate the add key commands.
				sLine = info.getStringValue(3 + nComp);
				int nKeys = Integer.valueOf(sLine).intValue();
				for (int i = 0; i < nKeys; i++) {
					sLine = info.getStringValue(4 + nComp + i);
					result.addItem(ItemType.RESPONSE_TEXT, "add"
							+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_KEY + GlobalConstants.PM_ALT_FIELD_DELIM
							+ sLine + GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.OATTR.value
							+ GlobalConstants.PM_ALT_FIELD_DELIM + sRecName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void exportTemplates(Packet result) {
		Packet templates =  getTemplates(null);
		if (templates == null || templates.size() <= 0) {
			return;
		}
		if (templates.hasError()) {
			return;
		}

		try {
			for (int i = 0; i < templates.size(); i++) {
				String sLine = templates.getStringValue(i);
				String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
				Packet tplInfo =  getTemplateInfo(null, pieces[1]);

				tplInfo.print(true, "Template info for " + pieces[0]);

				// item 0: <tpl name>:<tpl id>
				// item 1: <cont 1 id>:...:<cont n id>
				// item 2: <key1>:...:<keyn>
				String sContainers = tplInfo.getStringValue(1);
				String[] splinters = sContainers.split(GlobalConstants.PM_FIELD_DELIM);
				boolean first = true;
				StringBuffer sb = new StringBuffer();
				for (int j = 0; j < splinters.length; j++) {
					String sContName = getEntityName(splinters[j],
							PM_NODE.OATTR.value);
					if (first) {
						first = false;
						sb.append(sContName);
					} else {
						sb.append(GlobalConstants.PM_FIELD_DELIM + sContName);
					}
				}
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_TEMPLATE + GlobalConstants.PM_ALT_FIELD_DELIM
						+ pieces[0] + GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_CONTAINERS
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sb.toString());
				if (tplInfo.size() >= 3) {
					String sKeys = tplInfo.getStringValue(2);
					splinters = sKeys.split(GlobalConstants.PM_FIELD_DELIM);
					for (int j = 0; j < splinters.length; j++) {
						result.addItem(ItemType.RESPONSE_TEXT, "add"
								+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_KEY
								+ GlobalConstants.PM_ALT_FIELD_DELIM + splinters[j]
										+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_TEMPLATE
										+ GlobalConstants.PM_ALT_FIELD_DELIM + pieces[0]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void exportOattr(Packet result, String sId, String sBaseType,
			String sBaseName) {
		String sName = getEntityName(sId,PM_NODE.OATTR.value);

		String sVobjId = getAssocObj(sId);
		try {
			if (sVobjId == null) {
				// This is an object attribute not associated with an object.
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.OATTR.value
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sName + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sBaseType + GlobalConstants.PM_ALT_FIELD_DELIM + sBaseName);
			} else {
				// This is an object attribute associated with an object.
				// Export the object.
				Packet ar =  getObjInfo(sVobjId);
				String sLine = ar.getStringValue(0);

				// sLine may contain:
				// name|id|class|inh|host|path, or
				// name|id|class|inh|orig name|orig id, or
				// name|id|class|inh.
				String[] pieces = sLine.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				String sClass = pieces[2];

				// If the object's class is Clipboard, don't export it.
				if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_CLIPBOARD_NAME)) {
					return;
				}

				StringBuffer sb = new StringBuffer();
				sb.append("add" + GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_OBJ
						+ GlobalConstants.PM_ALT_FIELD_DELIM + pieces[0] + GlobalConstants.PM_ALT_FIELD_DELIM
						+ pieces[2] + GlobalConstants.PM_ALT_FIELD_DELIM + pieces[3]);
				if (pieces.length <= 4) {
					sb.append(GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_ALT_FIELD_DELIM);
				} else {
					sb.append(GlobalConstants.PM_ALT_FIELD_DELIM + pieces[4]
							+ GlobalConstants.PM_ALT_FIELD_DELIM + pieces[5]);
				}
				sb.append(GlobalConstants.PM_ALT_FIELD_DELIM + sBaseType + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sBaseName);
				result.addItem(ItemType.RESPONSE_TEXT, sb.toString());
			}

			// If the object attribute is a record (container), save it to add
			// template,
			// components, and keys later.
			if (isRecord(sId)) {
				savedRecords.add(sId);
			}

			// Export its properties.
			HashSet<String> props = getProps(sId,PM_NODE.OATTR.value);
			Iterator<String> iter = props.iterator();
			while (iter.hasNext()) {
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_PROP + GlobalConstants.PM_ALT_FIELD_DELIM
						+ iter.next() + GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.OATTR.value
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void exportPc(Packet result, String sId) {
		String sName = getEntityName(sId,PM_NODE.POL.value);

		try {
			result.addItem(ItemType.RESPONSE_TEXT, "add" + GlobalConstants.PM_ALT_FIELD_DELIM
					+PM_NODE.POL.value + GlobalConstants.PM_ALT_FIELD_DELIM + sName
					+ GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.CONN.value + GlobalConstants.PM_ALT_FIELD_DELIM
					+ GlobalConstants.PM_CONNECTOR_NAME);

			// Export its properties.
			HashSet<String> props = getProps(sId,PM_NODE.POL.value);
			Iterator<String> iter = props.iterator();
			while (iter.hasNext()) {
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_PROP + GlobalConstants.PM_ALT_FIELD_DELIM
						+ iter.next() + GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.POL.value
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void exportUser(Packet result, String sId, String sBaseType,
			String sBaseName) {
		String sName = getEntityName(sId,PM_NODE.USER.value);
		String sFull = getUserFullName(sId);
		try {
			result.addItem(ItemType.RESPONSE_TEXT, "add" + GlobalConstants.PM_ALT_FIELD_DELIM
					+PM_NODE.USER.value + GlobalConstants.PM_ALT_FIELD_DELIM + sName
					+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_FULL_NAME + GlobalConstants.PM_ALT_FIELD_DELIM
					+ sFull + GlobalConstants.PM_ALT_FIELD_DELIM + sBaseType
					+ GlobalConstants.PM_ALT_FIELD_DELIM + sBaseName);

			// Export its email account info.
			Packet emlInfo =  getEmailAcct(null, sName);
			if (emlInfo == null || emlInfo.size() < 6 || emlInfo.hasError()) {
				return;
			}
			String sComingFrom = emlInfo.getStringValue(1);
			String sEmailAddr = emlInfo.getStringValue(2);
			String sPopServer = emlInfo.getStringValue(3);
			String sSmtpServer = emlInfo.getStringValue(4);
			String sAcctName = emlInfo.getStringValue(5);
			String sPassword = sName;

			result.addItem(ItemType.RESPONSE_TEXT, "add" + GlobalConstants.PM_ALT_FIELD_DELIM
					+ GlobalConstants.PM_EMAIL_ACCT + GlobalConstants.PM_ALT_FIELD_DELIM + sComingFrom
					+ GlobalConstants.PM_ALT_FIELD_DELIM + sEmailAddr + GlobalConstants.PM_ALT_FIELD_DELIM
					+ sPopServer + GlobalConstants.PM_ALT_FIELD_DELIM + sSmtpServer
					+ GlobalConstants.PM_ALT_FIELD_DELIM + sAcctName + GlobalConstants.PM_ALT_FIELD_DELIM
					+ sPassword + GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.USER.value
					+ GlobalConstants.PM_ALT_FIELD_DELIM + sName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void exportUattr(Packet result, String sId, String sBaseType,
			String sBaseName) {
		String sName = getEntityName(sId,PM_NODE.UATTR.value);
		try {

			result.addItem(ItemType.RESPONSE_TEXT, "add" + GlobalConstants.PM_ALT_FIELD_DELIM
					+PM_NODE.UATTR.value + GlobalConstants.PM_ALT_FIELD_DELIM + sName
					+ GlobalConstants.PM_ALT_FIELD_DELIM + sBaseType + GlobalConstants.PM_ALT_FIELD_DELIM
					+ sBaseName);

			// Export its properties.
			HashSet<String> props = getProps(sId,PM_NODE.UATTR.value);
			Iterator<String> iter = props.iterator();
			while (iter.hasNext()) {
				result.addItem(ItemType.RESPONSE_TEXT, "add"
						+ GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_PROP + GlobalConstants.PM_ALT_FIELD_DELIM
						+ iter.next() + GlobalConstants.PM_ALT_FIELD_DELIM +PM_NODE.UATTR.value
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public Packet testSynchro(String sClientId) throws Exception {
		System.out.println("Starting work in testSynchro");
		for (long i = 0; i < 5000000000l; i++) ;
		return ADPacketHandler.getSuccessPacket("TestSynchro terminated");
	}


	public void printSetOfSets(HashSet<HashSet<String>> hs, String caption) {
		if (caption != null && caption.length() > 0) {
			System.out.println(caption);
		}
		Iterator<HashSet<String>> hsiter = hs.iterator();

		System.out.println("{");
		while (hsiter.hasNext()) {
			HashSet<String> set = hsiter.next();
			System.out.print(" ");
			printSet(set,PM_NODE.UATTR.value, null);
		}
		System.out.println("}");
	}


	public void printVector(Vector<String> v, String sType, String caption) {
		System.out.println("-------" + caption + "-------");

		System.out.println("{");
		for (int i = 0; i < v.size(); i++) {
			String sId = v.elementAt(i);
			String sName = getEntityName(sId, sType);
			System.out.println("    " + sName + ":" + sId + ",");
		}
		System.out.println("}");
	}

	// Whether the string sY can be added to the HashSet set without
	// conflict with the SAC constraint sac
	// (sac is only one of the SAC constraints!).

	public boolean canAddTo(String sY, HashSet<String> set,
			HashSet<HashSet<String>> sac) {
		Iterator<String> setiter = set.iterator();
		while (setiter.hasNext()) {
			String sX = setiter.next();
			if (elementsConflict(sY, sX, sac)) {
				return false;
			}
		}
		return true;
	}

	// Whether x and y are each in a different set of sac.

	public boolean elementsConflict(String sX, String sY,
			HashSet<HashSet<String>> sac) {
		int ix = 0;
		int iy = 0;
		int i = 0;
		Iterator<HashSet<String>> saciter = sac.iterator();
		while (saciter.hasNext()) {
			i++;
			HashSet<?> saci = saciter.next();
			if (saci.contains(sX)) {
				ix = i;
			}
			if (saci.contains(sY)) {
				iy = i;
			}
		}
		return (ix > 0 && iy > 0 && ix != iy);
	}


	public void addSetToResult(HashSet<String> set,
			HashSet<HashSet<String>> result) {
		HashSet<String> member = null;
		Iterator<HashSet<String>> resiter = result.iterator();
		boolean replace = false;

		while (resiter.hasNext()) {
			member = resiter.next();
			if (member.containsAll(set)) {
				return;
			}
			if (set.containsAll(member)) {
				replace = true;
				break;
			}
		}
		if (replace) {
			result.remove(member);
		}
		result.add(set);
	}

	public Packet getMaximalSubsets(String sClientId, String sAsetId) {
		HashSet<String> hsAttrs = asetToSet(sAsetId);
		HashSet<HashSet<String>> res = getMaximalSubsetsInternal(hsAttrs);

		Packet result = new Packet();

		// Each element of res is a subset we were looking for.
		Iterator<HashSet<String>> resiter = res.iterator();
		while (resiter.hasNext()) {
			HashSet<?> subset = resiter.next();
			Iterator<?> subiter = subset.iterator();
			boolean first = true;
			String sLine = "";
			while (subiter.hasNext()) {
				String sId = (String) subiter.next();
				String sName = getEntityName(sId,PM_NODE.UATTR.value);
				if (first) {
					first = false;
					sLine = sName;
				} else {
					sLine = sLine + "," + sName;
				}
			}
			try {
				// result.addItem(ItemType.RESPONSE_TEXT, setToString(subset));
				result.addItem(ItemType.RESPONSE_TEXT, sLine);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Exception when building the result packet");
			}
		}
		return result;
	}

	// Returns the maximal (with respect to SAC) activable attribute subsets of
	// a set.
	// The argument is the id of a user attribute set.

	public HashSet<HashSet<String>> getMaximalSubsetsInternal(
			HashSet<String> hsAttrs) {
		HashSet<HashSet<String>> temp, result;

		// TODO Empty method, need to be revisited
		return null;
	}

	// An attribute set to HashSet.

	public HashSet<String> asetToSet(String sAsetId) {
		Attributes attrs;
		Attribute attr;
		HashSet<String> hs = new HashSet<String>();
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sAsetId + ","
					+ sAttrSetContainerDN);
			attr = attrs.get("pmAttr");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sAttrId = (String) enumer.next();
					hs.add(sAttrId);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hs;
	}


	public String selectBest(HashMap<String, Set<String>> selectable,
			HashSet<String> remaining, HashSet<String> sm) {
		// Walk through selectable's entries whose keys ARE ALSO IN sm.
		// Select that key (i.e., ua) which contributes the maximum number of
		// perms.
		int maxContrib = 0;
		String sSelectedUa = null;
		System.out.println("*    *Begin selectBest");
		printSet(remaining, GlobalConstants.PM_PERM, "*    *with remaining set:");
		Iterator<String> mapiter = selectable.keySet().iterator();
		while (mapiter.hasNext()) {
			String sUa = mapiter.next();
			if (!sm.contains(sUa)) {
				continue;
			}
			HashSet<String> set = new HashSet<String>(selectable.get(sUa));
			printSet(set, GlobalConstants.PM_PERM, "*    *selectBest: attribute "
					+ getEntityName(sUa,PM_NODE.UATTR.value) + " has contrib:");
			if (set.contains(GlobalConstants.PM_ANY_ANY)) {
				set = new HashSet<String>(remaining);
			} else if (remaining.contains(GlobalConstants.PM_ANY_ANY)) {
			} else {
				set.retainAll(remaining);
			}
			if (set.size() > maxContrib) {
				maxContrib = set.size();
				sSelectedUa = sUa;
			}
		}
		System.out.println("*    *End selectBest");
		return sSelectedUa;
	}

	//
	// GetPermittedOps computes the operations that are permitted
	// to a session/process on a given object. In doing that, it
	// considers all attributes of the process/session user as being
	// active.
	// Let p be a process for user u in a session sess.
	//
	// Process p in session sess is permitted operation op on object o
	// iff (u, op, o) is a permission and (op, o) is not denied
	// for user u or session sess or process p.
	//
	// For the definition of permissions (u, op, o), see the docs.
	//
	// Parameters:
	// sCrtSessId: the id of the session.
	// sCrtProcId: the id of the process.
	// sObjName: the name of the object.
	//
	// Return:
	// An error packet or a packet that contains the following items:
	// Item 0: <name>|<id>|<class>|<inh>|<host or orig name>|<path or orig id>
	// Item 1: <operation permitted>
	// Item 2: <operation permitted>
	// ...
	//

	public Packet getPermittedOps(String sCrtSessId, String sCrtProcId,
			String sObjName) {
		String sObjId = getEntityId(sObjName, GlobalConstants.PM_OBJ);
		if (sObjId == null) {
			return failurePacket("No object of name " + sObjName);
		}

		Packet res =  getObjInfo(sObjId);
		if (res.hasError()) {
			return res;
		}

		// Find the id of the associated object attribute, then the permissions.
		String sOattrId = getEntityId(sObjName,PM_NODE.OATTR.value);
		if (sOattrId == null) {
			return failurePacket("The object " + sObjName
					+ " has no associated attribute!");
		}
		HashSet<String> grantedPerms = getPermittedOpsInternal(sCrtSessId,
				sOattrId);
		if (grantedPerms == null) {
			return failurePacket("The engine returned a null set of granted permissions!");
		}

		HashSet<String> deniedPerms;
		try {
			deniedPerms = getDeniedPerms(sCrtSessId, sCrtProcId, sOattrId,
					PM_NODE.OATTR.value);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception while computing denied permissions: "
					+ e.getMessage());
		}
		grantedPerms.removeAll(deniedPerms);

		// Add the permissions to the resulting packet.
		try {
			Iterator<String> iter = grantedPerms.iterator();
			while (iter.hasNext()) {
				res.addItem(ItemType.RESPONSE_TEXT, iter.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}

		return res;
	}

	// Return some object properties, like if it's an email message, sent by
	// whom,
	// when, subject.

	public Packet getObjEmailProps(String sCrtSessId, String sObjName) {
		// sObjName must be the name of an object.
		String sObjId = getEntityId(sObjName, GlobalConstants.PM_OBJ);
		if (sObjId == null) {
			return failurePacket("Selected entity, " + sObjName
					+ ", is not an object!");
		}

		try {
			Attributes objattrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			String sClass = (String) objattrs.get("pmObjClass").get();
			if (!sClass.equals(GlobalConstants.PM_CLASS_FILE_NAME)) {
				return failurePacket("Selected object is not an email message!");
			}
			String sPath = (String) objattrs.get("pmPath").get();
			if (!sPath.endsWith(".eml")) {
				return failurePacket("Selected object is not an email message!");
			}
			Attribute attr = objattrs.get("pmEmlSender");
			if (attr == null) {
				return failurePacket("No email properties set!");
			}

			Packet res = new Packet();
			res.addItem(ItemType.RESPONSE_TEXT, (String) attr.get());
			res.addItem(ItemType.RESPONSE_TEXT,
					(String) objattrs.get("pmEmlRecip").get());
			res.addItem(ItemType.RESPONSE_TEXT,
					(String) objattrs.get("pmTimestamp").get());
			res.addItem(ItemType.RESPONSE_TEXT,
					(String) objattrs.get("pmEmlSubject").get());
			attr = objattrs.get("pmEmlAttached");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					res.addItem(ItemType.RESPONSE_TEXT, (String) enumer.next());
				}
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception while extracting properties; "
					+ e.getMessage());
		}
	}

	public Packet getPosNodeProperties(String sCrtSessId, String sPosPresType,
			String sNodeId, String sNodeLabel, String sNodeType) {
		System.out.println("getPosNodeProperties called for:");
		System.out.println("  " + sPosPresType);
		System.out.println("  " + sNodeId);
		System.out.println("  " + sNodeLabel);
		System.out.println("  " + sNodeType);
		
		// The selected node must be a record or an object. In more details,
		// it must be an object attribute that has an associated object (PM_NODE_ASSOC),
		// or a template id.
		// If it's associated to an object, get the object.
		if (sNodeType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			String sObjId = getAssocObj(sNodeId);
			if (sObjId == null) {
				return failurePacket("Inconsistency: the selected node has no associated object!");
			}
			// Extract the properties from the object.
			return getObjInfo(sObjId);
			
		// If it's a record (check first if oa, then look for template).
		} else if (sNodeType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			try {
				Attributes attrs;
				attrs = ServerConfig.ctx.getAttributes("CN=" + sNodeId + "," +
						sObjAttrContainerDN);
				Attribute attr = attrs.get("pmTemplateId");
				if (attr == null) return failurePacket("The selected node is not an object or a record!");
				String sTemplateId = (String)attr.get();
				String sTemplateName = getEntityName(sTemplateId, GlobalConstants.PM_TEMPLATE);
				if (sTemplateName == null)
					return failurePacket("Inconsistency: no template with id = " + sTemplateId);
				String sName = getEntityName(sNodeId, PM_NODE.OATTR.value);
				Packet res = new Packet();
				res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_ALT_FIELD_DELIM +
						sNodeId + GlobalConstants.PM_ALT_FIELD_DELIM +
						GlobalConstants.PM_CLASS_RECORD_NAME + GlobalConstants.PM_ALT_FIELD_DELIM +
						"no" + GlobalConstants.PM_ALT_FIELD_DELIM + sTemplateName);
				return res;
			} catch (Exception e) {
				if (ServerConfig.debugFlag) e.printStackTrace();
				return failurePacket("Error while getting POS node properties: " + e.getMessage());
			}
		} else {
			return failurePacket("The selected node is not an object nor a record!");
		}
	}
	
	// For return value, see the refMediation() function.
	public Packet getVosIdProperties(String sCrtSessId, String sVosPresType,
			String sVosId) {
		//Josh added
		String sObjId = "";
		NamingEnumeration<?> objects;
		try {
			String cont = "";
			if (sVosPresType.equalsIgnoreCase(GlobalConstants.PM_VOS_PRES_ADMIN)) {
				cont = sAdminVosNodeContainerDN;
			} else {
				cont = sVosNodeContainerDN;
			}
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			objects = ServerConfig.ctx.search(cont, "(pmOriginalId=" + sVosId + ")",
					constraints);
			System.out.println(objects);
			if(objects == null){
				System.out.println("could not find pmId from " + sVosId);
				return failurePacket("could not find pmId from " + sVosId);
			}

			SearchResult sr = (SearchResult) objects.next();
			sObjId = (String) sr.getAttributes().get("pmId").get();
			sVosId = sObjId;
			System.out.println("sVosId: " + sVosId);

		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		//end Josh added

		Attributes attrs;
		try {
			if (sVosPresType.equalsIgnoreCase(GlobalConstants.PM_VOS_PRES_ADMIN)) {
				attrs = ServerConfig.ctx.getAttributes("CN=" + sVosId + ","
						+ sAdminVosNodeContainerDN);
			} else {
				attrs = ServerConfig.ctx.getAttributes("CN=" + sVosId + ","
						+ sVosNodeContainerDN);
			}
			/*String sSessId = (String) attrs.get("pmSessId").get();
								System.out.println("sSessid in getVosIdProperties: " + sSessId);
								if (!sSessId.equalsIgnoreCase(sCrtSessId)) {
									return failurePacket("Inconsistency: The node you selected does not appear in the current session!");
								}*/
			String sOrigId = (String) attrs.get("pmOriginalId").get();
			System.out.println("sOrigId in getVosIdProperties: " + sOrigId);
			String sType = null;
			if (sVosPresType.equalsIgnoreCase(GlobalConstants.PM_VOS_PRES_ADMIN)) {
				sType = (String) attrs.get("pmType").get();
			} else {
				String s = (String) attrs.get("pmIsObj").get();
				sType = s.equalsIgnoreCase("TRUE") ?PM_NODE.ASSOC.value
						:PM_NODE.OATTR.value;
			}
			System.out.println("sType in getVosIdProperties: " + sType);
			if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
				// Let's find the virtual object.
				/*String*/ sObjId = getAssocObj(sOrigId);
				//System.out.println("ASSOC sObjId in getVosIdProperties: " + sSessId);
				if (sObjId == null) {
					return failurePacket("Inconsistency: The node you selected has no associated object!");
				}
				// Extract the properties from the virtual object.
				return getObjInfo(sObjId);
			} else {
				attrs = ServerConfig.ctx.getAttributes("CN=" + sOrigId + ","
						+ sObjAttrContainerDN);
				Attribute attr = attrs.get("pmTemplateId");
				if (attr == null) {
					return failurePacket("The selected node is not an object or a record!");
				}
				String sTemplateId = (String) attr.get();
				String sTemplateName = getEntityName(sTemplateId, GlobalConstants.PM_TEMPLATE);
				if (sTemplateName == null) {
					return failurePacket("Inconsistency: no template with id "
							+ sTemplateId);
				}
				String sName = getEntityName(sOrigId,PM_NODE.OATTR.value);
				Packet res = new Packet();
				res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sOrigId + GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_CLASS_RECORD_NAME
						+ GlobalConstants.PM_ALT_FIELD_DELIM + "no" + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sTemplateName);
				return res;
			}

		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Error while getting VOS node properties: "
					+ e.getMessage());
		}
	}


	// Find the object id from an object id, or an oattr id, or an object name.
	String getObjId(String sObj) {
		//String sObjId = null;

		// First try as if sObj were an oattr id. Return the assoc object.
		String sObjName = getEntityName(sObj,PM_NODE.OATTR.value);
		if (sObjName != null) {
			return getAssocObj(sObj);
		}

		// Argument is not an oattr id. Try as if it were an object id.
		sObjName = getEntityName(sObj, GlobalConstants.PM_OBJ);
		if (sObjName != null) {
			return sObj;
		}

		// The argument is not an object id. Try as if it were an object name.
		return getEntityId(sObj, GlobalConstants.PM_OBJ);
	}


	public Packet getObjProperties(String sObjOrObjAttribute) {
		System.out.println("Calling getObjProperties(" + sObjOrObjAttribute + ")");
		String sObjId = getObjId(sObjOrObjAttribute);
		System.out.println("Actual sObjId is " + sObjId);
		Attributes virattrs;
		Packet result = new Packet();
		try {
			virattrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			String sName = (String) virattrs.get("pmName").get();
			String sOattrId = (String) virattrs.get("pmAssocAttr").get();
			String sClass = (String) virattrs.get("pmObjClass").get();
			String sIncludes = (String) virattrs.get("pmIncludesAscendants").get();
			sIncludes = (sIncludes.equals("TRUE") ? "yes" : "no");
			if (sClass.equals(GlobalConstants.PM_CLASS_UATTR_NAME) ||
					sClass.equals(GlobalConstants.PM_CLASS_OATTR_NAME) ||
					sClass.equals(GlobalConstants.PM_CLASS_POL_NAME)) {
				HashSet props = getProps(sObjId, sClass);
				if (props != null) {
					StringBuilder propsBuilder = new StringBuilder();
					for (Object obj : props) {
						propsBuilder.append(obj);
						propsBuilder.append(GlobalConstants.PM_ALT_FIELD_DELIM);
					}
					String propsString = propsBuilder.toString();
					result.addItem(ItemType.RESPONSE_TEXT, propsString);
				}
			}

		} catch (NamingException ne) {
			ne.printStackTrace(System.out);
		} catch (PacketException pe) {
			pe.printStackTrace(System.out);
		}
		return result;

	}

	// This method should work when sObj is the name or id of an object or of
	// the
	// oattr associated with an object.
	// Returns:
	// For File/Dir: <name>|<oattr id>|<class>|<inh>|<host>|<path>
	// For PM entity: <name>|<oattr id>|<class>|<inh>|<orig name>|<orig id>
	// For Composite: <name>|<oattr id>|<class>|<inh>.

	public Packet getObjInfo(String sObj) {
		System.out.println("Calling getObjInfo(" + sObj + ")" + " and the obj String: " + sObj + "\n");
		String sObjId = getObjId(sObj);
		System.out.println("Actual sObjId is " + sObjId);

		Attributes virattrs;
		Packet result = new Packet();
		try {
			virattrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			String sName = (String) virattrs.get("pmName").get();
			String sOattrId = (String) virattrs.get("pmAssocAttr").get();
			String sClass = (String) virattrs.get("pmObjClass").get();
			String sIncludes = (String) virattrs.get("pmIncludesAscendants").get();
			sIncludes = (sIncludes.equals("TRUE") ? "yes" : "no");
			if (sClass.equals(GlobalConstants.PM_CLASS_FILE_NAME)
					|| sClass.equals(GlobalConstants.PM_CLASS_DIR_NAME)) {
				String sHost = (String) virattrs.get("pmHost").get();
				String sPath = (String) virattrs.get("pmPath").get();
				result.addItem(ItemType.RESPONSE_TEXT, sName
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sOattrId + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sClass + GlobalConstants.PM_ALT_FIELD_DELIM + sIncludes
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sHost + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sPath);
			} else if (sClass.equals(GlobalConstants.PM_CLASS_USER_NAME)
					|| sClass.equals(GlobalConstants.PM_CLASS_UATTR_NAME)
					|| sClass.equals(GlobalConstants.PM_CLASS_OBJ_NAME)
					|| sClass.equals(GlobalConstants.PM_CLASS_OATTR_NAME)
					|| sClass.equals(GlobalConstants.PM_CLASS_POL_NAME)
					|| sClass.equals(GlobalConstants.PM_CLASS_CONN_NAME)
					|| sClass.equals(GlobalConstants.PM_CLASS_OPSET_NAME)) {

				String sOrigName = (String) virattrs.get("pmOriginalName").get();
				String sOrigId = (String) virattrs.get("pmOriginalId").get();
				result.addItem(ItemType.RESPONSE_TEXT, sName
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sOattrId + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sClass + GlobalConstants.PM_ALT_FIELD_DELIM + sIncludes
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sOrigName + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sOrigId);

			} else {
				result.addItem(ItemType.RESPONSE_TEXT, sName
						+ GlobalConstants.PM_ALT_FIELD_DELIM + sOattrId + GlobalConstants.PM_ALT_FIELD_DELIM
						+ sClass + GlobalConstants.PM_ALT_FIELD_DELIM + sIncludes);
			}
			return result;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in getObjInfo(): " + e.getMessage());
		}
	}


	public Packet getUserDescendants(String sClientId, String sUserId) {
		return ADPacketHandler.setToPacket(getUserDescendantsInternal(sUserId));
	}


	// Get the current permissions on an object attribute. It is assumed that
	// the
	// object attribute is associated to a virtual object, but in reality it
	// could
	// be any oattr.
	// This function is called from getPermittedOps(). It works as
	// if all the user's attributes were active.

	public HashSet<String> getPermittedOpsInternal(String sSessId,
			String sTgtOaId) /* throws Exception */ {
		//NamingEnumeration<?> objs;

		Vector<String> activeAttrs;
		Vector<String> policyClasses;
		Vector<String> opsets;
		Vector<String> oattrs;
		//HashSet<?> objects;

		String sPcId;
		String sAaId;
		String sOpsId;
		String sOaId;

		// Get the session's active attributes and the policy classes (as
		// Vectors).
		String sUserId = getSessionUserId(sSessId);
		System.out.println("User: " + sUserId);
		activeAttrs = getUserDescendantsInternalVector(sUserId);
		policyClasses = getPolicyClasses();

		// Create an empty HashMap.
		HashMap<String, HashSet<String>> hm = new HashMap<String, HashSet<String>>();

		// For each policy class pc
		for (int pc = 0; pc < policyClasses.size(); pc++) {
			sPcId = policyClasses.elementAt(pc);
			System.out.println("For pc = " + getEntityName(sPcId,PM_NODE.POL.value));
			// If (o is not in policy class pc) continue.
			if (!attrIsAscendantToPolicy(sTgtOaId,PM_NODE.OATTR.value, sPcId)) {
				System.out.println("  Oattr "
						+ getEntityName(sTgtOaId,PM_NODE.OATTR.value)
						+ " is not in " + getEntityName(sPcId,PM_NODE.POL.value));
				continue;
			}
			System.out.println("  Oattr "
					+ getEntityName(sTgtOaId,PM_NODE.OATTR.value) + " is in "
					+ getEntityName(sPcId,PM_NODE.POL.value));

			// The object is in this policy class. Note that at this time
			// hm has no entry for pc. Add an entry [pc, empty] to hm.
			System.out.println("  Create entry hm["
					+ getEntityName(sPcId,PM_NODE.POL.value) + "]");
			addHmEntry(sPcId, null, hm);

			// For each active attribute
			for (int aa = 0; aa < activeAttrs.size(); aa++) {
				sAaId = activeAttrs.elementAt(aa);
				System.out.println("  For each active attr "
						+ getEntityName(sAaId,PM_NODE.UATTR.value));
				// If !(aa ->+ pc) continue.
				if (!attrIsAscendantToPolicy(sAaId,PM_NODE.UATTR.value, sPcId)) {
					System.out.println("    "
							+ getEntityName(sAaId,PM_NODE.UATTR.value)
							+ " is not in pc = "
							+ getEntityName(sPcId,PM_NODE.POL.value));
					continue;
				}
				System.out.println("    " + getEntityName(sAaId,PM_NODE.UATTR.value)
				+ " is in pc = " + getEntityName(sPcId,PM_NODE.POL.value));

				// For each opset such that aa -> opset
				opsets = getToOpsets(sAaId);
				for (int ops = 0; ops < opsets.size(); ops++) {
					sOpsId = opsets.elementAt(ops);

					// For each oa such that opset -> oa
					oattrs = getToAttrs(sOpsId);
					for (int oa = 0; oa < oattrs.size(); oa++) {
						sOaId = oattrs.elementAt(oa);
						// If !(oa ->+ pc) continue.
						if (!attrIsAscendantToPolicy(sOaId,PM_NODE.OATTR.value,
								sPcId)) {
							continue;
						}
						// If !(o ->* oa) continue
						if (!attrIsAscendant(sTgtOaId, sOaId,PM_NODE.OATTR.value)) {
							continue;
						}
						// Add [pc, ops] to the hashmap.
						System.out.println("    Added "
								+ getEntityName(sOpsId,PM_NODE.OPSET.value)
								+ " to entry pc = "
								+ getEntityName(sPcId,PM_NODE.POL.value));
						addHmEntry(sPcId, sOpsId, hm);
					}
				}
			}
		}
		System.out.println(hm);
		// Intersect the permissions in each entry of the HashMap.
		Iterator<HashSet<String>> iter = hm.values().iterator();
		boolean firstTime = true;
		HashSet<String> resultSet = new HashSet<String>();
		HashSet<String> crtSet = null;
		while (iter.hasNext()) {
			if (firstTime) {
				firstTime = false;
				resultSet = iter.next();
			} else {
				crtSet = iter.next();
				if (crtSet.contains(GlobalConstants.PM_ANY_ANY)) {
				} else if (resultSet.contains(GlobalConstants.PM_ANY_ANY)) {
					resultSet = crtSet;
				} else {
					resultSet.retainAll(crtSet);
				}
			}
		}
		System.out.println("resultset: " + resultSet.toString());
		return resultSet;
	}

	// A null sOpsId means an empty set of operations.

	public void addHmEntry(String sPcId, String sOpsId,
			HashMap<String, HashSet<String>> hm) {
		HashSet<String> set;

		if (hm.containsKey(sPcId)) {
			set = hm.get(sPcId);
			addOpsetToSet(sOpsId, set);
		} else {
			set = new HashSet<String>();
			addOpsetToSet(sOpsId, set);
			hm.put(sPcId, set);
		}
	}

	// The command getPermittedOpsOnEntity is just for debugging.
	// It may be called from the admin tool.
	/*public Packet getPermittedOpsOnEntity(String sSessId, String sTargetId,
		    String sTargetType) {
		    return setToPacket(getPermittedOpsOnEntityInternal(sSessId, null,
		    sTargetId, sTargetType));
		    }*/
	// The command getPermittedOpsOnEntityInternal returns the set
	// of admin operations permitted to all processes
	// in a given session on a given target. The target is a PM entity
	// (like an object attribute, a policy class, etc.), which is
	// identified by its id and type. To find the admin operations
	// permitted to a process on this target, first we must find
	// the objects that represent the target. Then we have to find
	// the admin operations that are permitted on each representative
	// in each policy class, etc.
	// An object x represents the target t if:
	// - x.pmOriginalId = t.id and x.inh = no; or:
	// - x.pmOriginalId != null and x.inh = yes and t is in x.ascendants.

	public HashSet<String> getPermittedOpsOnEntityInternal(String sCrtSessId,//TODO
			String sProcId, String sTgtId, String sTgtType) {
		System.out.println("sTgtId:\t" + sTgtId);
		System.out.println("sTgtType:\t" + sTgtType);
		HashSet<String> prSet = new HashSet<String>();
		String sObjId;

		if (sTgtType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sTgtType =PM_NODE.OATTR.value;
		}

		System.out.println("***********getPermittedOpsOnEntityInternal "
				+ getEntityName(sTgtId, sTgtType));

		// Search PmVirtualObjContainer for objects x as in the above comment:
		NamingEnumeration<?> objects;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId",
					"pmOriginalId", "pmObjClass", "pmIncludesAscendants"});
			objects = ServerConfig.ctx.search(sVirtualObjContainerDN, "(objectClass=*)",
					constraints);
			while (objects != null && objects.hasMore()) {
				SearchResult sr = (SearchResult) objects.next();
				sObjId = (String) sr.getAttributes().get("pmId").get();
				Attribute attr = sr.getAttributes().get("pmOriginalId");
				if (attr == null) {
					continue;
				}
				String sSomeEntityId = (String) attr.get();
				String sInh = (String) sr.getAttributes().get("pmIncludesAscendants").get();
				String sSomeEntityClass = (String) sr.getAttributes().get("pmObjClass").get();
				if (sSomeEntityId.equalsIgnoreCase(sTgtId)) {
					prSet.add(sObjId);
					System.out.println("Added virtual obj id " + sObjId);
				} else if (sInh.equalsIgnoreCase("true")
						&& isContained(sTgtId, sTgtType, sSomeEntityId,
								classToType(sSomeEntityClass))) {
					prSet.add(sObjId);
					System.out.println("Added virtual obj id " + sObjId);
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}

		printSet(prSet, GlobalConstants.PM_OBJ,
				"Set of virtual objects representing the entity "
						+ getEntityName(sTgtId, sTgtType));

		// If no virtual objects are mapped to the target entity,
		// return empty list of permissions.
		if (prSet.isEmpty()) {
			return new HashSet<String>();
		}

		// The computed operations permitted on the given entity.
		HashSet<String> computedOps = new HashSet<String>();
		System.out.println("computedOps" + computedOps);
		// Find the denied operations on the target entity.
		HashSet<String> deniedOps;
		try {
			deniedOps = getDeniedPerms(sCrtSessId, sProcId, sTgtId, sTgtType);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			System.out.println("caught in getPermittedOpsOnEntityInternal");
			return computedOps;
		}

		// Prepare for iteration through the objects that represent
		// the given entity (which were collected in prSet.
		Iterator<String> setiter = prSet.iterator();
		//String sUserId = getSessionUserId(sCrtSessId);

		// For each representative object
		while (setiter.hasNext()) {
			sObjId = setiter.next();
			System.out.println("---For the rep object " + sObjId + ","
					+ getEntityName(sObjId, GlobalConstants.PM_OBJ));

			// Get the oattr associated with the representative object.
			String sAssocOaId = getAssocOattr(sObjId);
			System.out.println("sAssocOaId: " + sAssocOaId);
			if (sAssocOaId == null) {
				continue;
			}

			// Get the permitted ops on the associated oattr:
			System.out.println("current sessid: " + sCrtSessId);
			computedOps.addAll(getPermittedOpsInternal(sCrtSessId, sAssocOaId));
			System.out.println("computedOps" + computedOps);
		}

		// Remove the denied operations:
		computedOps.removeAll(deniedOps);
		return computedOps;
	}


	public Packet getPermittedOpsOnEntity(String sCrtSessId, String sProcId, 
			String sTgtId, String sTgtType){
		return ADPacketHandler.setToPacket(getPermittedOpsOnEntityInternal(sCrtSessId, sTgtId,
				sTgtId, sTgtType));
	}

	// Translates PM entity classes to types, e.g., User --> u, User attribute
	// --> a.

	public String classToType(String sClass) {
		if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
			return PM_NODE.USER.value;
		} else if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_UATTR_NAME)) {
			return PM_NODE.UATTR.value;
		} else if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_OBJ_NAME)) {
			return GlobalConstants.PM_OBJ;
		} else if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_OATTR_NAME)) {
			return PM_NODE.OATTR.value;
		} else if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_POL_NAME)) {
			return PM_NODE.POL.value;
		} else if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_OPSET_NAME)) {
			return PM_NODE.OPSET.value;
		} else if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_CONN_NAME)) {
			return PM_NODE.CONN.value;
		} else if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_FILE_NAME)) {
			return GlobalConstants.PM_OBJ;
		} else if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_DIR_NAME)) {
			return GlobalConstants.PM_OBJ;
		} else {
			return null;
		}
	}

	// Translates types to PM entity classes, e.g., u --> User.

	public String typeToClass(String sType) {
		if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
			return GlobalConstants.PM_CLASS_USER_NAME;
		} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			return GlobalConstants.PM_CLASS_UATTR_NAME;
		} else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
			return GlobalConstants.PM_CLASS_OBJ_NAME;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			return GlobalConstants.PM_CLASS_OATTR_NAME;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			return GlobalConstants.PM_CLASS_OATTR_NAME;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			return GlobalConstants.PM_CLASS_POL_NAME;
		} else if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			return GlobalConstants.PM_CLASS_OPSET_NAME;
		} else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			return GlobalConstants.PM_CLASS_CONN_NAME;
		} else {
			return null;
		}
	}


	public Packet testIsContained(String sId1, String sClass1, String sId2,
			String sClass2) {
		Packet result = new Packet();
		System.out.println("Test isContained " + sId1 + ":" + sClass1 + ", "
				+ sId2 + ":" + sClass2);
		try {
			result.addItem(ItemType.RESPONSE_TEXT,
					(isContained(sId1, sClass1, sId2, sClass2) ? "true"
							: "false"));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
	}

	// The types are abbrevs used in the engine (u, a, ob, b, p, s, c).

	public boolean isContained(String sId1, String sType1, String sId2,
			String sType2) {
		System.out.println(sId1 + ":" + sType1 + "->" + sId2 + ":" + sType2);
		if (sType1.equalsIgnoreCase(PM_NODE.USER.value)) {
			return (sType2.equalsIgnoreCase(PM_NODE.UATTR.value) && userIsAscendant(
					sId1, sId2))
					|| (sType2.equalsIgnoreCase(PM_NODE.POL.value) && userIsAscendantToPolicy(
							sId1, sId2))
					|| (sType2.equalsIgnoreCase(PM_NODE.CONN.value));
		}

		if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			return (sType2.equalsIgnoreCase(PM_NODE.UATTR.value) && attrIsAscendant(
					sId1, sId2,PM_NODE.UATTR.value))
					|| (sType2.equalsIgnoreCase(PM_NODE.POL.value) && attrIsAscendantToPolicy(
							sId1,PM_NODE.UATTR.value, sId2))
					|| (sType2.equalsIgnoreCase(PM_NODE.CONN.value));
		}

		if (sType1.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType1.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			return (sType2.equalsIgnoreCase(PM_NODE.OATTR.value) && attrIsAscendant(
					sId1, sId2,PM_NODE.OATTR.value))
					|| (sType2.equalsIgnoreCase(PM_NODE.POL.value) && attrIsAscendantToPolicy(
							sId1,PM_NODE.OATTR.value, sId2))
					|| (sType2.equalsIgnoreCase(PM_NODE.CONN.value));
		}

		if (sType1.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
			// We have a virtual object. To assess containment, look at its
			// associated object attribute.
			String sAssoc1 = getAssocOattr(sId1);
			if (sAssoc1 == null) {
				return false;
			}
			return (sType2.equalsIgnoreCase(PM_NODE.OATTR.value) && attrIsAscendant(
					sAssoc1, sId2,PM_NODE.OATTR.value))
					|| (sType2.equalsIgnoreCase(PM_NODE.POL.value) && attrIsAscendantToPolicy(
							sAssoc1,PM_NODE.OATTR.value, sId2))
					|| (sType2.equalsIgnoreCase(PM_NODE.CONN.value));
		}

		if (sType1.equalsIgnoreCase(PM_NODE.POL.value)) {
			return (sType2.equalsIgnoreCase(PM_NODE.CONN.value));
		}

		if (sType1.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			return (opsetIsAscendant(sId1, sId2, sType2));
		}

		return false;
	}


	public boolean opsetIsAscendant(String sId1, String sId2, String sType2) {
		Attributes attrs;
		Attribute attr;

		if (sType2.equalsIgnoreCase(PM_NODE.CONN.value)) {
			return true;
		}

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId1 + "," + sOpsetContainerDN);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return false;
			}
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sId = (String) attrEnum.next();
				if (sType2.equalsIgnoreCase(PM_NODE.OATTR.value)) {
					if (attrIsAscendant(sId, sId2, sType2)) {
						return true;
					}
				} else if (sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
					if (attrIsAscendantToPolicy(sId,PM_NODE.OATTR.value, sId2)) {
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}




	// Test whether (op, o) is in the caps for an active attribute in a policy
	// class.
	// Note that instead of the object o we use the associated object attribute.

	public boolean attrHasCap(String sOp, String sAssocOaId, String sPcId,
			String sAaId) {
		Vector<String> opsets;
		Vector<String> oattrs;
		String sOpsId;
		String sOaId;

		// For each opset such that aa -> opset
		opsets = getToOpsets(sAaId);
		for (int ops = 0; ops < opsets.size(); ops++) {
			sOpsId = opsets.elementAt(ops);

			// If the operation is not in the opset, continue with next opset.
			if (!opsetContainsOp(sOpsId, sOp)) {
				continue;
			}
			// For each oa such that opset->oa
			oattrs = getToAttrs(sOpsId);
			for (int oa = 0; oa < oattrs.size(); oa++) {
				sOaId = oattrs.elementAt(oa);
				// If (o ->* oa && oa ->+ pc) return true
				if (attrIsAscendant(sAssocOaId, sOaId,PM_NODE.OATTR.value)
						&& attrIsAscendantToPolicy(sOaId,PM_NODE.OATTR.value, sPcId)) {
					return true;
				}
			}
		}
		return false;
	}



	public void printHtWithPcKeys(Map<String, Map<String, Set<String>>> ht) {
		System.out.println("---------- Hashtable ----------");
		for (Iterator<String> keys = ht.keySet().iterator(); keys.hasNext(); ) {
			String sKey = keys.next();
			String sPc = getEntityName(sKey,PM_NODE.POL.value);
			System.out.println("For policy class " + sPc);

			Map<String, Set<String>> map = ht.get(sKey);
			Set<String> keyset = map.keySet();
			Iterator<String> iter = keyset.iterator();
			while (iter.hasNext()) {
				String sMapkey = iter.next();
				String sUa = getEntityName(sMapkey,PM_NODE.UATTR.value);
				System.out.print("    Attr: " + sUa);
				System.out.println(" -> "
						+ UtilMethods.setToString(map.get(sMapkey)));
			}
		}
	}

	// Add (to) an entry in a Hashtable, whose value is a HashMap, whose value
	// is a HashSet built out of HashSets.
	// sKey is a Hashtable key, and sSecKey is a HashMap key.
	// sKey cannot be null.
	// If sSecKey is null (possible only when sKey is new), add an empty
	// HashMap.

	public void addHtEntry(String sKey, String sSecKey, Set<String> setToAdd,
			Map<String, Map<String, Set<String>>> ht) {
		Map<String, Set<String>> map;
		Set<String> set;

		if (ht.containsKey(sKey)) {
			map = ht.get(sKey);
			if (sSecKey == null) {
				return;
			}
			if (map.containsKey(sSecKey)) {
				set = map.get(sSecKey);
				set.addAll(setToAdd);
			} else {
				// Build a new entry in the HashMap.
				set = new HashSet<String>();
				set.addAll(setToAdd);
				map.put(sSecKey, set);
			}
		} else {
			// Add a new entry with key sKey, but first build the value.
			map = new HashMap<String, Set<String>>();
			if (sSecKey == null) {
				ht.put(sKey, map);
				return;
			}
			set = new HashSet<String>();
			set.addAll(setToAdd);
			map.put(sSecKey, set);
			ht.put(sKey, map);
		}
	}

	// Add (to) an entry in a Hashtable, whose value is a HashMap, whose value
	// is a HashSet built out of opsets.
	// sKey is a Hashtable key, and sSecKey is a HashMap key.
	// sKey cannot be null.
	// If sSecKey is null (possible only when sKey is new), add an empty
	// HashMap.
	// In addOpsetToSet(), a null sOpsId is interpreted as the empty set of
	// operations.

	public void addHtEntry(String sKey, String sSecKey, String sOpsId,
			Map<String, Map<String, Set<String>>> ht) {
		Map<String, Set<String>> map;
		Set<String> set;

		if (ht.containsKey(sKey)) {
			map = ht.get(sKey);
			if (sSecKey == null) {
				return;
			}
			if (map.containsKey(sSecKey)) {
				set = map.get(sSecKey);
				addOpsetToSet(sOpsId, set);
			} else {
				// Build a new entry in the HashMap.
				set = new HashSet<String>();
				addOpsetToSet(sOpsId, set);
				map.put(sSecKey, set);
			}
		} else {
			// Add a new entry with key sKey, but first build the value.
			map = new HashMap<String, Set<String>>();
			if (sSecKey == null) {
				ht.put(sKey, map);
				return;
			}
			set = new HashSet<String>();
			addOpsetToSet(sOpsId, set);
			map.put(sSecKey, set);
			ht.put(sKey, map);
		}
	}

	// A null sOpsetId means an empty set of operations.

	public void addOpsetToSet(String sOpsetId, Set<String> set) {
		Attributes attrs;
		Attribute attr;
		if (sOpsetId == null) {
			return;
		}
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sOpsetId + ","
					+ sOpsetContainerDN);
			attr = attrs.get("pmOp");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					set.add((String) enumer.next());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Get session events since the last update and delete them from
	// the Events container.

	public Packet getSessionEvents(String sClientId) throws Exception {
		Packet result = new Packet();
		NamingEnumeration<?> events;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId",
					"pmEvent", "pmOriginalId"});
			events = ServerConfig.ctx.search(sEventContainerDN, "(pmObjClass="
					+ sSessionClass + ")", constraints);
			while (events != null && events.hasMore()) {
				SearchResult sr = (SearchResult) events.next();
				String sEvent = (String) sr.getAttributes().get("pmEvent").get();
				String sSessId = (String) sr.getAttributes().get("pmOriginalId").get();
				String sEventId = (String) sr.getAttributes().get("pmId").get();

				ServerConfig.ctx.destroySubcontext("CN=" + sEventId + ","
						+ sEventContainerDN);

				result.addItem(ItemType.RESPONSE_TEXT, sEvent + GlobalConstants.PM_FIELD_DELIM
						+ sSessId);
			}
			return result;
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
	}


	public HashSet<String> getUserSessions(String sUserId) {
		NamingEnumeration<?> sessions;
		HashSet<String> sessSet = new HashSet<String>();
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			sessions = ServerConfig.ctx.search(sSessionContainerDN, "(pmUserId=" + sUserId
					+ ")", constraints);
			while (sessions != null && sessions.hasMore()) {
				SearchResult sr = (SearchResult) sessions.next();
				String sSessId = (String) sr.getAttributes().get("pmId").get();
				sessSet.add(sSessId);
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return sessSet;
	}

	// Get a list of all sessions. Each entry in the list contains:
	// name:id.
	// As the last step, this function deletes the session create and delte
	// events
	// from the event container.

	public Packet getSessions(String sClientId) throws Exception {
		Packet result = new Packet();
		NamingEnumeration<?> ses;

		// Delete all session events.
		emptyContainer(sEventContainerDN, null);

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			ses = ServerConfig.ctx.search(sSessionContainerDN, "(objectClass=*)",
					constraints);
			while (ses != null && ses.hasMore()) {
				SearchResult sr = (SearchResult) ses.next();
				String sName = (String) sr.getAttributes().get("pmName").get();
				String sId = (String) sr.getAttributes().get("pmId").get();
				result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
						+ sId);
			}
			return result;
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
	}

	// The information returned by getSessionInfo has the following format:
	// item 0: <sess name>:<sess id>
	// item 1: <user name>:<user id>
	// item 2: <host name>:<host id>
	// items 3 through 3 + active_attr_count - 1: <attr name>:<attr id>

	public Packet getSessionInfo(String sClientId, String sSessId) {
		Packet result = new Packet();

		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
					+ sSessionContainerDN);
			String sSessName = (String) attrs.get("pmName").get();
			String sUserId = (String) attrs.get("pmUserId").get();
			String sHostName = (String) attrs.get("pmHost").get();
			result.addItem(ItemType.RESPONSE_TEXT, sSessName + GlobalConstants.PM_FIELD_DELIM
					+ sSessId);
			result.addItem(ItemType.RESPONSE_TEXT,
					getEntityName(sUserId,PM_NODE.USER.value) + GlobalConstants.PM_FIELD_DELIM
					+ sUserId);
			result.addItem(ItemType.RESPONSE_TEXT, sHostName + GlobalConstants.PM_FIELD_DELIM
					+ getEntityId(sHostName, GlobalConstants.PM_HOST));

			Attribute attr = attrs.get("pmAttr");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sAttrId = (String) enumer.next();
					String sAttrName = getEntityName(sAttrId,PM_NODE.UATTR.value);
					if (sAttrName == null) {
						return failurePacket("No such attribute " + sAttrId);
					}
					result.addItem(ItemType.RESPONSE_TEXT, sAttrName
							+ GlobalConstants.PM_FIELD_DELIM + sAttrId);
				}
			}
			return result;
		} catch (NameNotFoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("No such session");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception during getSessionInfo: "
					+ e.getMessage());
		}
	}


	public HashSet<String> getPolicyAttributesAssigned(String sPolId,
			String sOattrId) {
		try {
			HashSet<String> hs = new HashSet<String>();
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOattrId + ","
					+ sObjAttrContainerDN);
			Attribute attr = attrs.get("pmToAttr");
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					if (attrIsAscendantToPolicy(sId,PM_NODE.OATTR.value, sPolId)) {
						hs.add(sId);
					}
				}
			}
			return hs;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}


	public String getSessionHostName(String sSessId) {
		String sHost;

		System.out.println("Looking for session CN=" + sSessId + ","
				+ sSessionContainerDN);

		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
					+ sSessionContainerDN);
			Attribute attr = attrs.get("pmHost");
			sHost = (String) attr.get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
		return sHost;
	}


	public Packet getSessionUser(String sSessId) {
		String sUserId = getSessionUserId(sSessId);
		if (sUserId == null) {
			return failurePacket("Couldn't find session or its user id");
		}
		String sUser = getEntityName(sUserId,PM_NODE.USER.value);
		if (sUser == null) {
			return failurePacket("Couldn't find session user");
		}
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sUser);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
	}


	public Packet getSessionName(String sSessId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
					+ sSessionContainerDN);
			Attribute attr = attrs.get("pmName");
			String sName = (String) attr.get();
			Packet result = new Packet();
			result.addItem(ItemType.RESPONSE_TEXT, sName);
			return result;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in getSessionName: "
					+ e.getMessage());
		}
	}


	public String getSessionUserId(String sSessId) {
		String sUserId;

		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
					+ sSessionContainerDN);
			Attribute attr = attrs.get("pmUserId");
			sUserId = (String) attr.get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
		return sUserId;
	}


	public Vector<String> getSessionActiveAttrs(String sSessId) {
		Vector<String> v = new Vector<String>();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
					+ sSessionContainerDN);
			Attribute attr = attrs.get("pmAttr");
			if (attr == null) {
				return v;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				v.add((String) enumer.next());
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return v;
	}

	// Returns the set of already open objects for the given session.
	// Note that they are returned as object names!

	public HashSet<String> getSessionOpenObjs(String sSessId) {
		HashSet<String> hs = new HashSet<String>();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
					+ sSessionContainerDN);
			Attribute attr = attrs.get("pmOpenObj");
			if (attr == null) {
				return hs;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				String sOO = (String) enumer.next();
				String[] pieces = sOO.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
				hs.add(pieces[0]);
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return hs;
	}


	public HashSet<String> getSessionActiveAttrSet(String sSessId) {
		HashSet<String> hs = new HashSet<String>();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
					+ sSessionContainerDN);
			Attribute attr = attrs.get("pmAttr");
			if (attr == null) {
				return hs;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				hs.add((String) enumer.next());
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return hs;
	}


	public String getUserPass(String sId) {
		Attributes attrs;
		//Attribute attr;
		String sPass;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sUserContainerDN);
			if (attrs == null) {
				return null;
			}
			sPass = (String) attrs.get("pmPassword").get();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return sPass;
	}

	/**
	 * @uml.property  name="nextSessionNumber"
	 */
	int nextSessionNumber = 1;

	// The password is not encrypted in any way by PmSimul, but the
	// communication
	// client-server is secure.

	public Packet spawnSession(String sClientId, String sCrtSessId)
			throws Exception {
		String sUserId;
		String sUserName;
		String sHost;
		String sSessName;
		//String sSessid;
		Attributes attrs;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtSessId + ","
					+ sSessionContainerDN);
			sUserId = (String) attrs.get("pmUserId").get();
			sHost = (String) attrs.get("pmHost").get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in spawnSession: " + e.getMessage());
		}

		sUserName = getEntityName(sUserId,PM_NODE.USER.value);
		sSessName = sUserName + "@" + sHost + "-" + nextSessionNumber++;

		attrs = new BasicAttributes(true);
		RandomGUID myGUID = new RandomGUID();
		String sSessId = myGUID.toStringNoDashes();
		attrs.put("objectClass", sSessionClass);
		attrs.put("pmId", sSessId);
		attrs.put("pmName", sSessName);
		attrs.put("pmHost", sHost);
		attrs.put("pmUserId", sUserId);
		String sTimestamp = dfUpdate.format(new Date());
		attrs.put("pmTimestamp", sTimestamp);

		// Prepare the path and create the new session object.
		String sDn = "CN=" + sSessId + "," + sSessionContainerDN;
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not spawn the session object");
		}

		// Create an event in PmEventContainer with:
		// pmObjClass = the class of the new session object, i.e.,
		// "pmClassSession";
		// pmOriginalId = the id of the new session object;
		// pmEvent = "create";
		// pmTimestamp = timestamp of the new session.
		// cn = "create" + "|" + the id of the new session object.
		myGUID = new RandomGUID();
		String sEventId = myGUID.toStringNoDashes();
		attrs = new BasicAttributes(true);
		attrs.put("objectClass", sEventClass); // Class of this object, which is
		// an event.
		attrs.put("pmObjClass", sSessionClass); // Class of the event's object,
		// which is a session.
		attrs.put("pmId", sEventId); // The id of the event.
		attrs.put("pmOriginalId", sSessId); // The id of the event's object,
		// which is the session id.
		attrs.put("pmEvent", GlobalConstants.PM_EVENT_CREATE); // The event - create.
		attrs.put("pmTimestamp", sTimestamp); // The event's timestamp.

		// Prepare the path for the new event object.
		sDn = "CN=" + sEventId + "," + sEventContainerDN;

		// If same event for the same object already exists, delete it.
		if (!deleteSimilarEvents(GlobalConstants.PM_EVENT_CREATE, sSessId)) {
			return failurePacket("Failed to delete similar events");
		}
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not register the event \"Create session\"");
		}

		// Return the name and id of the new session.
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sSessName);
			res.addItem(ItemType.RESPONSE_TEXT, sSessId);
			res.addItem(ItemType.RESPONSE_TEXT, sUserId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return res;
	}

	// The password is not encrypted.

	public Packet createSession(String sClientId, String sName, String sHost,
			String sUser, String sPass) throws Exception {
		// An empty password arrives here as null.
		//NDK Added
		String sAction = "create session";
		boolean sResult = true;
		String sDesc = "This creates a new session";
		//end of addition - NDK
		if (sPass == null) {
			sPass = "";
		}

		// Check that the host is registered with PM, but use the host name
		// in the session, as we do in the objects.
		String sHostId = getEntityId(sHost, GlobalConstants.PM_HOST);
		if (sHostId == null) {
			//NDK Added
			sResult = false;
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo("", sHost, sUser, null, sAction, sResult, sDesc, null, null);
			}
			//addition end - NDK
			return failurePacket("No such host name!");
		}

		String sUserId = getEntityId(sUser,PM_NODE.USER.value);
		if (sUserId == null) {
			//NDK addition
			sResult = false;
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
			}
			//end of addition - NDK
			return failurePacket("Incorrect user name or password!");
		}

		// Get the hash from database, then the salt (first 24 hex digits = 12
		// bytes.
		String sStoredHash = getUserPass(sUserId);
		if (sStoredHash == null) {
			//NDK addition
			sResult = false;
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
			}
			//end of addition - NDK
			return failurePacket("Internal error - user has no password hash!");
		}
		String sSalt = sStoredHash.substring(0, 24);

		// Convert the salt from 24 hex digits to 12 bytes.
		byte[] salt = UtilMethods.hexString2ByteArray(sSalt);

		// Get a message digest instance and hash the salt and the password.
		byte[] digest;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(salt);
			md.update(sPass.getBytes());
			sPass = null;
			digest = md.digest();
		} catch (Exception e) {
			e.printStackTrace();
			//NDK Addition
			sResult = false;
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
			}
			//end of addition - NDK
			return failurePacket("Internal error while hashing the password");
		}
		String sComputedHash = sSalt + UtilMethods.byteArray2HexString(digest);
		if (!sComputedHash.equalsIgnoreCase(sStoredHash)) {
			//NDK addition
			sResult = false;
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
			}
			//end of addition - NDK
			return failurePacket("Incorrect user name or password!");
		}

		// Get a name for session.
		sName = sUser + "@" + sHost + "-" + nextSessionNumber++;

		Attributes attrs = new BasicAttributes(true);
		RandomGUID myGUID = new RandomGUID();
		String sSessId = myGUID.toStringNoDashes();
		attrs.put("objectClass", sSessionClass);
		attrs.put("pmId", sSessId);
		attrs.put("pmName", sName);
		attrs.put("pmHost", sHost);
		attrs.put("pmUserId", sUserId);
		String sTimestamp = dfUpdate.format(new Date());
		attrs.put("pmTimestamp", sTimestamp);

		// Prepare the path and create the new session object.
		String sDn = "CN=" + sSessId + "," + sSessionContainerDN;
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			//NDK addition
			sResult = false;
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
			}
			//end of addition - NDK
			return failurePacket("Could not create the session object");
		}

		// Create an event in PmEventContainer with:
		// pmObjClass = the class of the new session object, i.e.,
		// "pmClassSession";
		// pmOriginalId = the id of the new session object;
		// pmEvent = "create";
		// pmTimestamp = timestamp of the new session.
		// cn = "create" + "|" + the id of the new session object.
		myGUID = new RandomGUID();
		String sEventId = myGUID.toStringNoDashes();
		attrs = new BasicAttributes(true);
		attrs.put("objectClass", sEventClass); // Class of this object, which is
		// an event.
		attrs.put("pmObjClass", sSessionClass); // Class of the event's object,
		// which is a session.
		attrs.put("pmId", sEventId); // The id of the event.
		attrs.put("pmOriginalId", sSessId); // The id of the event's object,
		// which is the session id.
		attrs.put("pmEvent", GlobalConstants.PM_EVENT_CREATE); // The event - create.
		attrs.put("pmTimestamp", sTimestamp); // The event's timestamp.

		// Prepare the path for the new event object.
		sDn = "CN=" + sEventId + "," + sEventContainerDN;

		// If same event for the same object already exists, delete it.
		if (!deleteSimilarEvents(GlobalConstants.PM_EVENT_CREATE, sSessId)) {
			//NDk addition
			sResult = false;
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
			}
			//end of addition - NDK
			return failurePacket("Failed to delete similar events");
		}
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			//NDK
			sResult = false;
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
			}
			//end- NDK
			return failurePacket("Could not register the event \"Create session\"");
		}

		// Return the name and id of the new session to PmSimul.
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sName);
			res.addItem(ItemType.RESPONSE_TEXT, sSessId);
			res.addItem(ItemType.RESPONSE_TEXT, sUserId);
		} catch (Exception e) {
			//NDK
			sResult = false;
			if(ServerConfig.auditDebug){
				Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
			}
			//end  - NDK
			return failurePacket("Exception when building the result packet");
		}
		//added by NDK this calls my method that sets the audit data

		sResult = true;
		if(ServerConfig.auditDebug){
			Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
		}
		//end of addition - NDK
		return res;
	}

	// Delete all events with pmEvent=sEvent on the the object pmOriginalId =
	// sObjId.

	public boolean deleteSimilarEvents(String sEvent, String sObjId) {
		NamingEnumeration<?> events;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			events = ServerConfig.ctx.search(sEventContainerDN, "(&(pmEvent=" + sEvent
					+ ")(pmOriginalId=" + sObjId + "))", constraints);
			while (events != null && events.hasMore()) {
				SearchResult sr = (SearchResult) events.next();
				String sEventId = (String) sr.getAttributes().get("pmId").get();
				ServerConfig.ctx.destroySubcontext("CN=" + sEventId + ","
						+ sEventContainerDN);
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}


	public Packet changePassword(String sClientId, String sUser,
			String sOldPass, String sNewPass, String sConPass) throws Exception {
		// Empty passwords arrive here as nulls.
		if (sOldPass == null) {
			sOldPass = "";
		}
		if (sNewPass == null) {
			sNewPass = "";
		}
		if (sConPass == null) {
			sConPass = "";
		}

		if (!sNewPass.equals(sConPass)) {
			return failurePacket("The new passwords you typed do not match!");
		}
		sConPass = null;

		String sUserId = getEntityId(sUser,PM_NODE.USER.value);
		if (sUserId == null) {
			return failurePacket("Incorrect user name or password!");
		}

		// Get the hash from database, then the salt (first 24 hex digits = 12
		// bytes.
		String sStoredHash = getUserPass(sUserId);
		if (sStoredHash == null) {
			return failurePacket("Internal error - user has no password hash");
		}
		String sSalt = sStoredHash.substring(0, 24);

		// Convert the salt from 24 hex digits to 12 bytes.
		byte[] salt = UtilMethods.hexString2ByteArray(sSalt);

		// Get a message digest instance and hash the salt and the password.
		byte[] digest;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(salt);
			md.update(sOldPass.getBytes());
			sOldPass = null;
			digest = md.digest();
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Internal error while hashing the password!");
		}
		String sComputedHash = sSalt + UtilMethods.byteArray2HexString(digest);
		if (!sComputedHash.equalsIgnoreCase(sStoredHash)) {
			return failurePacket("Incorrect user name or password!");
		}

		// Get a random 12-byte salt.
		SecureRandom random = new SecureRandom();
		salt = new byte[12];
		random.nextBytes(salt);

		// Digest the salt and the new password.
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(salt);
			md.update(sNewPass.getBytes());
			sNewPass = null; // but they're not collected probably
			digest = md.digest();
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Error while hashing the password");
		}

		// Convert the hash to a string of hex digits.
		sComputedHash = UtilMethods.byteArray2HexString(salt) + UtilMethods.byteArray2HexString(digest);

		// Store the new hash as attribute "pmPassword" of the user.
		try {
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
					new BasicAttribute("pmPassword", sComputedHash));
			ServerConfig.ctx.modifyAttributes("CN=" + sUserId + "," + sUserContainerDN, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Failed to change password: " + e.getMessage());
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public boolean deleteSessionVosNodes(String sSessId) {
		NamingEnumeration<?> nodes;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"cn"});
			nodes = ServerConfig.ctx.search(sVosNodeContainerDN, "(pmSessId=" + sSessId
					+ ")", constraints);
			while (nodes != null && nodes.hasMore()) {
				SearchResult sr = (SearchResult) nodes.next();
				String sCn = (String) sr.getAttributes().get("cn").get();
				ServerConfig.ctx.destroySubcontext("CN=" + sCn + "," + sVosNodeContainerDN);
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}


	public Packet deleteSession(String sClientId, String sSessId) {
		// First delete the session's VOS nodes.
		if (!deleteSessionVosNodes(sSessId)) {
			return failurePacket("Could not delete the session's VOS nodes");
		}
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sSessId + "," + sSessionContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete session " + sSessId);
		}

		// Create an event in PmEventContainer with:
		// pmObjClass = the class of the session object, i.e., "pmClassSession";
		// pmId = the id of the session object;
		// pmEvent = "delete";
		// pmTimestamp = current timestamp.
		// cn = "delete" + "|" + the id of the session object.
		Attributes attrs = new BasicAttributes(true);
		RandomGUID myGUID = new RandomGUID();
		String sEventId = myGUID.toStringNoDashes();
		attrs.put("objectClass", sEventClass); // Class of this object, which is
		// an event.
		attrs.put("pmObjClass", sSessionClass); // Class of the event's object,
		// which is a session.
		attrs.put("pmId", sEventId); // The id of the event.
		attrs.put("pmOriginalId", sSessId); // The id of the event's object,
		// which is the session id.
		attrs.put("pmEvent", GlobalConstants.PM_EVENT_DELETE); // The event - delete.
		String sTimestamp = dfUpdate.format(new Date());
		attrs.put("pmTimestamp", sTimestamp); // The event's timestamp.

		// Prepare the path and create the new event object. If an event with
		// the
		// same name exists, delete it first.
		String sDn = "CN=" + sEventId + "," + sEventContainerDN;
		if (!deleteSimilarEvents(GlobalConstants.PM_EVENT_DELETE, sSessId)) {
			return failurePacket("Failed to delete similar events");
		}
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not create the event \"Delete session\"");
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Determine the admin view of the virtual object system for a given
	// session/user.

	public Packet computeAdminVos(String sClientId, String sUserId,
			String sSessId) {
		Vector<String> repOasInPc;

		String sUserName = getEntityName(sUserId,PM_NODE.USER.value);
		if (sUserName == null) {
			return failurePacket("No user with such id!");
		}

		System.out.println("Determining new VOS for user " + sUserName);

		// Get all policy classes:
		Vector<String> policyClasses = getPolicyClasses();

		// Get all user's attributes (ua such that u ->+ ua):
		HashSet<String> authAttrs = getUserDescendantsInternal(sUserId);

		// Init a HashMap whose entries will contain the AccRepOas(u, pc)
		// with pc as key.
		HashMap<String, Vector<String>> hm = new HashMap<String, Vector<String>>();

		// For each policy class pc:
		for (int pc = 0; pc < policyClasses.size(); pc++) {
			String sPcId = policyClasses.elementAt(pc);
			String sPcName = getEntityName(sPcId,PM_NODE.POL.value);

			// Init the vector for RepOasInPc(u, pc) and create the hm entry
			// for the current pc:
			repOasInPc = new Vector<String>();
			hm.put(sPcId, repOasInPc);

			// For each user attribute ua such that ua ->+ pc:
			Iterator<String> uaiter = authAttrs.iterator();
			while (uaiter.hasNext()) {
				String sUaId = uaiter.next();
				if (!attrIsAscendantToPolicy(sUaId,PM_NODE.UATTR.value, sPcId)) {
					continue;
				}

				// For each ops such that ua->ops:
				Vector<String> opsets = getToOpsets(sUaId);
				for (int ops = 0; ops < opsets.size(); ops++) {
					String sOpsId = opsets.elementAt(ops);

					// For each oa such that ops -> oa
					Vector<String> oattrs = getToAttrs(sOpsId);
					for (int oa = 0; oa < oattrs.size(); oa++) {
						String sOaId = oattrs.elementAt(oa);

						// If oa does not have an associate object, skip it.
						String sObjId = getAssocObj(sOaId);
						if (sObjId == null) {
							continue;
						}

						// If not (oa ->+ pc) skip this oa.
						if (!attrIsAscendantToPolicy(sOaId,PM_NODE.OATTR.value,
								sPcId)) {
							continue;
						}

						// If the object sObjId does not represent a PM entity,
						// skip this oa.
						if (!objRepresentsAGraphEntity(sObjId)) {
							continue;
						}

						// Add oa to repOasInPc, if not already there.
						if (!repOasInPc.contains(sOaId)) {
							repOasInPc.add(sOaId);
						}
					}
				}
			}
			// Now we have the RepOasInPc(u, pc) set. Let's print its elements:
			printVector(repOasInPc,PM_NODE.OATTR.value,
					"Accessible object attributes in policy " + sPcName);
		}

		// Let's compute AccRepOas(u).
		HashSet<String> accRepOas = new HashSet<String>();

		// For each policy class pc:
		for (int pc = 0; pc < policyClasses.size(); pc++) {
			String sPcId = policyClasses.elementAt(pc);
			//String sPcName = getNameOfEntityWithIdAndType(sPcId,PM_NODE.POL.value);

			// Get the set RepOasInPc for the current pc.
			repOasInPc = hm.get(sPcId);

			// For each oa in RepOasInPc(u, pc)
			for (int i = 0; i < repOasInPc.size(); i++) {
				String sOaId = repOasInPc.elementAt(i);

				// If oa is accessible to user u, add oa to accRepOas.
				if (repOattrIsAccessible(sOaId, sPcId, hm)) {
					if (!accRepOas.contains(sOaId)) {
						accRepOas.add(sOaId);
					}
				}
			}
		}

		printSet(accRepOas,PM_NODE.OATTR.value, "Accessible representative oattrs");

		// Let's compute the set PrincRepOas(u).
		HashSet<String> princRepOas = new HashSet<String>();

		Iterator<String> accIter = accRepOas.iterator();
		loopOas:
			while (accIter.hasNext()) {
				String sOaId = accIter.next();
				Iterator<String> princIter = princRepOas.iterator();
				while (princIter.hasNext()) {
					String sPrincId = princIter.next();
					if (viewIsIncludedInView(sOaId, sPrincId)) {
						continue loopOas;
					}
				}
				princIter = princRepOas.iterator();
				while (princIter.hasNext()) {
					String sPrincId = princIter.next();
					if (viewIsIncludedInView(sPrincId, sOaId)) {
						princIter.remove();
					}
				}
				princRepOas.add(sOaId);
			}

		printSet(princRepOas,PM_NODE.OATTR.value, "Principal representative oattrs");

		// Build the VOS graph.
		buildAdminVosGraph(princRepOas, sUserId, sUserName, sSessId);
		Packet result = new Packet();
		try {
			result.addItem(ItemType.RESPONSE_TEXT, dfUpdate.format(new Date()));
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
	}


	public void buildAdminVosGraph(HashSet<String> princRepOas,
			String sUserId, String sUserName, String sSessId) {
		try {
			// First delete all records for the session.
			deleteAdminVosRecords(sSessId);

			// Build the connector node.
			Attributes vosAttrs = new BasicAttributes(true);
			vosAttrs.put("objectClass", sAdminVosNodeClass);
			RandomGUID myGUID = new RandomGUID();
			String sConnNewId = myGUID.toStringNoDashes();
			vosAttrs.put("pmId", sConnNewId);
			vosAttrs.put("pmOriginalId", GlobalConstants.PM_CONNECTOR_ID);
			vosAttrs.put("pmName", GlobalConstants.PM_CONNECTOR_NAME);
			vosAttrs.put("pmType",PM_NODE.CONN.value);
			vosAttrs.put("pmSessId", sSessId);
			vosAttrs.put("pmUserId", sUserId);
			vosAttrs.put("pmUserName", sUserName);
			ServerConfig.ctx.bind("CN=" + sConnNewId + "," + sAdminVosNodeContainerDN, null,
					vosAttrs);

			// Prepare for walk through views defined by the principals.
			HashSet<String> visitedSet = new HashSet<String>();
			ArrayList<AdminVosQueueElement> queue = new ArrayList<AdminVosQueueElement>();
			//			AdminVosQueueElement qe;
			AdminVosQueueElement crtQe;

			// For each principal in princRepOas
			Iterator<String> princIter = princRepOas.iterator();
			while (princIter.hasNext()) {
				String sPrincOaId = princIter.next();
				System.out.println("Generating the subgraph for principal \""
						+ getEntityName(sPrincOaId,PM_NODE.OATTR.value) + "\"");

				// Get information from the principal.
				// TO DO: Get this information in the previous steps and store
				// it in
				// the hash set princRepOas.
				String sObjId = getAssocObj(sPrincOaId);
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
						+ sVirtualObjContainerDN);
				Attribute attr = attrs.get("pmObjClass");
				String sPrincType = classToType((String) attr.get());
				attr = attrs.get("pmOriginalId");
				String sPrincId = (String) attr.get();
				String sPrincName = getEntityName(sPrincId, sPrincType);
				attr = attrs.get("pmIncludesAscendants");
				boolean bPrincAscs = ((String) attr.get()).equalsIgnoreCase("TRUE");
				if (sPrincType.equalsIgnoreCase(PM_NODE.OATTR.value)
						&& getAssocObj(sPrincId) != null) {
					sPrincType =PM_NODE.ASSOC.value;
				}
				String sEntNewId = null;

				System.out.println("    Principal's type = " + sPrincType
						+ ", id = " + sPrincId + ", name = "
						+ getEntityName(sPrincId, sPrincType) + ", subgraph = "
						+ bPrincAscs);

				// Build the principal node.
				// If the principal is the connector, don't build the principal
				// (the connector is already built), but set sEntNewId to
				// sConnNewId
				// in order to be initialized.
				// Anyway, build the principal's ascendants.
				sEntNewId = sConnNewId;
				if (!sPrincId.equalsIgnoreCase(GlobalConstants.PM_CONNECTOR_ID)) {
					vosAttrs = new BasicAttributes(true);
					vosAttrs.put("objectClass", sAdminVosNodeClass);
					myGUID = new RandomGUID();
					sEntNewId = myGUID.toStringNoDashes();
					vosAttrs.put("pmId", sEntNewId);
					vosAttrs.put("pmOriginalId", sPrincId);
					vosAttrs.put("pmName", sPrincName);
					vosAttrs.put("pmType", sPrincType);
					vosAttrs.put("pmSessId", sSessId);
					vosAttrs.put("pmUserId", sUserId);
					vosAttrs.put("pmUserName", sUserName);
					ServerConfig.ctx.bind(
							"CN=" + sEntNewId + "," + sAdminVosNodeContainerDN,
							null, vosAttrs);

					// Link it to the connnector node.
					if (!addAdminVosDoubleLink(sEntNewId, sConnNewId)) {
						System.out.println("Error while linking principal "
								+ sPrincName + " to connector!");
						return;
					}
				}

				// If the principal defines a view limited to the pricipal node
				// itself,
				// continue with next principal. Otherwise, insert the principal
				// into
				// the queue.
				if (!bPrincAscs) {
					continue;
				}
				crtQe = new AdminVosQueueElement(sPrincType, sPrincId,
						sEntNewId);
				queue.add(crtQe);

				// Loop while queue is not empty.
				while (!queue.isEmpty()) {
					// Extract first queue element:
					crtQe = queue.remove(0);
					String sCrtType = crtQe.getType();
					String sCrtId = crtQe.getId();
					String sCrtDescId = crtQe.getDesc();
					String sCrtName = getEntityName(sCrtId, sCrtType);
					if (ServerConfig.debugFlag) {
						System.out.println("Out of queue: " + sCrtName + "("
								+ sCrtId + ", " + sCrtType + "), "
								+ sCrtDescId);
					}
					// Get the direct ascendants of crtQe. They depend on crtQe
					// type.
					if (sCrtType.equalsIgnoreCase(PM_NODE.USER.value)) {
						// do nothing, there are no ascendants.
					} else if (sCrtType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
						// Direct ascendants could be user attributes...
						attr = getFromAttrs(sCrtId,PM_NODE.UATTR.value);
						doAdminVosDirAscs(attr,PM_NODE.UATTR.value, sCrtDescId,
								sSessId, queue, visitedSet);

						// ... or users.
						attr = getFromUsers(sCrtId,PM_NODE.UATTR.value);
						doAdminVosDirAscs(attr,PM_NODE.USER.value, sCrtDescId,
								sSessId, queue, visitedSet);
					} else if (sCrtType.equalsIgnoreCase(PM_NODE.POL.value)) {
						// Direct ascendants could be user attributes...
						attr = getFromUserAttrs(sCrtId,PM_NODE.POL.value);
						doAdminVosDirAscs(attr,PM_NODE.UATTR.value, sCrtDescId,
								sSessId, queue, visitedSet);
						// ... or object attributes.
						attr = getFromObjAttrs(sCrtId,PM_NODE.POL.value);
						doAdminVosDirAscs(attr,PM_NODE.OATTR.value, sCrtDescId,
								sSessId, queue, visitedSet);
					} else if (sCrtType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
						// Direct ascendants could be object attributes.
						attr = getFromAttrs(sCrtId,PM_NODE.OATTR.value);
						doAdminVosDirAscs(attr,PM_NODE.OATTR.value, sCrtDescId,
								sSessId, queue, visitedSet);
					} else if (sCrtType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
						// do nothing, an associated oattr does not have
						// ascendants.
					} else if (sCrtType.equalsIgnoreCase(PM_NODE.CONN.value)) {
						attr = getFromPolicies(GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
						doAdminVosDirAscs(attr,PM_NODE.POL.value, sCrtDescId,
								sSessId, queue, visitedSet);
					}

				}// while queue not empty
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Parameters:
	// attr: An Attribute containing the ids of the direct ascendants of a node.
	// sType: the type of those ascendants (PM_NODE.USER.value, etc.)
	// sVosBaseId: the VOS id of the base node.
	// sSessId: the session id.
	// queue and visited: we know them.
	// Note that the type for object attributes comes always asPM_NODE.OATTR.value,
	// event when the object attribute is associated to an object. In such a
	// case,
	// we need to put it correctly in the VOS asPM_NODE.ASSOC.value.

	public void doAdminVosDirAscs(Attribute attr, String sType,
			String sVosBaseId, String sSessId,
			ArrayList<AdminVosQueueElement> queue, HashSet<String> visited) {
		try {
			if (attr != null) {
				for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
					String sTrueType = sType;
					String sId = (String) attrEnum.next();
					String sName = getEntityName(sId, sType);
					String sNewId = null;

					// If the current ascendant is an object attribute
					// associated to an
					// object, change its type toPM_NODE.ASSOC.value ("o").
					if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
							&& getAssocObj(sId) != null) {
						sTrueType =PM_NODE.ASSOC.value;
					}

					// Do not display objects that represent PM entities!
					if (sTrueType.equals(PM_NODE.ASSOC.value)
							&& oattrRepresentsAnEntity(sId)) {
						continue;
					}

					if (!visited.contains(sId)) {
						visited.add(sId);
						Attributes vosAttrs = new BasicAttributes(true);
						vosAttrs.put("objectClass", sAdminVosNodeClass);
						RandomGUID myGUID = new RandomGUID();
						sNewId = myGUID.toStringNoDashes();
						vosAttrs.put("pmId", sNewId);
						vosAttrs.put("pmOriginalId", sId);
						vosAttrs.put("pmName", sName);
						vosAttrs.put("pmType", sTrueType);
						vosAttrs.put("pmSessId", sSessId);
						ServerConfig.ctx.bind("CN=" + sNewId + ","
								+ sAdminVosNodeContainerDN, null, vosAttrs);

						// Insert the new VOS node into the queue.
						AdminVosQueueElement qe = new AdminVosQueueElement(
								sTrueType, sId, sNewId);
						queue.add(qe);
					}

					// Create double link new VOS node -> base VOS node. When
					// the new VOS node
					// was already visited, we don;t know its new id, so we must
					// search for it
					// based on the original id and the session id.
					if (sNewId == null) {
						sNewId = getAdminVosNodeId(sId, sSessId);
					}
					if (!addAdminVosDoubleLink(sNewId, sVosBaseId)) {
						System.out.println("Error when linking VOS node "
								+ sName + " to its VOS base!");
						return;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void addAdminVosNode(Packet graph, String sType, String sId)
			throws Exception {
		String sLabel = getAdminVosNodeName(sId);
		StringBuffer sb = new StringBuffer();
		sb.append(sType);
		sb.append(GlobalConstants.PM_FIELD_DELIM);
		sb.append(sId);
		sb.append(GlobalConstants.PM_FIELD_DELIM);
		sb.append(sLabel);
		graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());
	}

	// For a given admin VOS node, return its direct ascendants.
	// sId is the new id of the VOS node.

	public Attribute getAdminVosNodeAscs(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sAdminVosNodeContainerDN);
			Attribute attr = attrs.get("pmAscNode");
			return attr;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// For a given admin VOS node, return its direct descendants.
	// sId is the new id of the VOS node.

	public Attribute getAdminVosNodeDescs(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sAdminVosNodeContainerDN);
			Attribute attr = attrs.get("pmDescNode");
			return attr;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// For a given admin VOS node, return its name.
	// sId is the new id of the VOS node.

	public String getAdminVosNodeName(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sAdminVosNodeContainerDN);
			return (String) attrs.get("pmName").get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// For a given admin VOS node, return its original id.
	// Parameter sId is the new id of the VOS node.

	public String getAdminVosNodeOrigId(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sAdminVosNodeContainerDN);
			return (String) attrs.get("pmOriginalId").get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// For a given VOS node, return its type.
	// sId is the new id of the VOS node.

	public String getAdminVosNodeType(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sAdminVosNodeContainerDN);
			return (String) attrs.get("pmType").get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// Given an admin VOS node's original id and the session id,
	// return the node's new id.

	public String getAdminVosNodeId(String sOrigId, String sSessId) {
		NamingEnumeration<?> nodes;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			nodes = ServerConfig.ctx.search(sAdminVosNodeContainerDN, "(&(pmOriginalId="
					+ sOrigId + ")(pmSessId=" + sSessId + "))", constraints);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
		try {
			if (nodes == null || !nodes.hasMore()) {
				return null;
			}
			SearchResult sr = (SearchResult) nodes.next();
			return (String) sr.getAttributes().get("pmId").get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}


	public boolean addAdminVosDoubleLink(String sId1, String sId2) {
		ModificationItem[] mods = new ModificationItem[1];

		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmDescNode", sId2));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sId1 + "," + sAdminVosNodeContainerDN,
					mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmAscNode", sId1));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sId2 + "," + sAdminVosNodeContainerDN,
					mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	// sOa1 and sOa2 are object attributes associated to objects that
	// represent PM graph entities.

	public boolean viewIsIncludedInView(String sOa1, String sOa2) {
		try {
			// First find the entities.
			// Get type, id, and isSubgraph for first entity.
			String sObjId = getAssocObj(sOa1);
			if (sObjId == null) {
				System.out.println("viewIsIncludedInView: arg 1 is not associated to an object");
				return false;
			}
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			Attribute attr = attrs.get("pmObjClass");
			if (attr == null || attr.size() <= 0) {
				System.out.println("viewIsIncludedInView: pmObjClass for arg 1 is null or empty");
				return false;
			}
			String sEntType1 = classToType((String) attr.get());
			attr = attrs.get("pmOriginalId");
			if (attr == null || attr.size() <= 0) {
				System.out.println("viewIsIncludedInView: pmOriginalId for arg 1 is null or empty");
				return false;
			}
			String sEntId1 = (String) attr.get();
			attr = attrs.get("pmIncludesAscendants");
			if (attr == null || attr.size() <= 0) {
				System.out.println("viewIsIncludedInView: pmIncludesAscendants for arg 1 is null or empty");
				return false;
			}
			boolean bEntAscs1 = ((String) attr.get()).equalsIgnoreCase("TRUE");

			// Get type, id, and isSubgraph for second entity.
			sObjId = getAssocObj(sOa2);
			if (sObjId == null) {
				System.out.println("viewIsIncludedInView: arg 2 is not associated to an object");
				return false;
			}
			attrs = ServerConfig.ctx.getAttributes("CN=" + sObjId + ","
					+ sVirtualObjContainerDN);
			attr = attrs.get("pmObjClass");
			if (attr == null || attr.size() <= 0) {
				System.out.println("viewIsIncludedInView: pmObjClass for arg 2 is null or empty");
				return false;
			}
			String sEntType2 = classToType((String) attr.get());
			attr = attrs.get("pmOriginalId");
			if (attr == null || attr.size() <= 0) {
				System.out.println("viewIsIncludedInView: pmOriginalId for arg 2 is null or empty");
				return false;
			}
			String sEntId2 = (String) attr.get();
			attr = attrs.get("pmIncludesAscendants");
			if (attr == null || attr.size() <= 0) {
				System.out.println("viewIsIncludedInView: pmIncludesAscendants for arg 2 is null or empty");
				return false;
			}
			boolean bEntAscs2 = ((String) attr.get()).equalsIgnoreCase("TRUE");

			if (bEntAscs2
					&& isContained(sEntId1, sEntType1, sEntId2, sEntType2)) {
				System.out.println("viewIsIncludedInView("
						+ getEntityName(sOa1,PM_NODE.OATTR.value) + ", "
						+ getEntityName(sOa2,PM_NODE.OATTR.value) + ") = true");
				return true;
			}
			if (sEntId1.equalsIgnoreCase(sEntId2) && !bEntAscs1 && !bEntAscs2) {
				System.out.println("viewIsIncludedInView("
						+ getEntityName(sOa1,PM_NODE.OATTR.value) + ", "
						+ getEntityName(sOa2,PM_NODE.OATTR.value) + ") = true");
				return true;
			}
			System.out.println("viewIsIncludedInView("
					+ getEntityName(sOa1,PM_NODE.OATTR.value) + ", "
					+ getEntityName(sOa2,PM_NODE.OATTR.value) + ") = false");
			return false;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
	}

	// The oa here is associated to an object that represents a graph entity.
	// It is sort of special, because it has opsets assigned to itself,
	// not to descendant oattrs.

	public boolean repOattrIsAccessible(String sOaId, String sPcId,
			HashMap<String, Vector<String>> hm) {
		// Try to find pc' != pc such that oa ->+ pc'. Use the hm keys to get
		// pc'.
		Set<String> keyset = hm.keySet();
		Iterator<String> iter = keyset.iterator();
		while (iter.hasNext()) {
			String sPcIdPrim = iter.next();
			if (sPcIdPrim.equals(sPcId)) {
				continue;
			}
			if (!attrIsAscendantToPolicy(sOaId,PM_NODE.OATTR.value, sPcIdPrim)) {
				continue;
			}
			// The vector MinAOA(u,pc'):
			Vector<String> v = hm.get(sPcIdPrim);
			// Looking for a oam' such that oa ->* oam'.
			// If we don't find one, then oa is not visible.
			boolean foundOaPrim = false;
			for (int i = 0; i < v.size(); i++) {
				String sOaIdPrim = v.elementAt(i);
				if (attrIsAscendant(sOaId, sOaIdPrim,PM_NODE.OATTR.value)) {
					foundOaPrim = true;
					break;
				}
			}
			if (!foundOaPrim) {
				return false;
			}
		}
		return true;
	}

	public Packet computeFastAdminVos(String sClientId, String sUserId, String sSessId) {
		return ADPacketHandler.getSuccessPacket();
	}
	
	public Packet computeFastVos(String sClientId, String sPresType, String sUserId,
			String sSessId) {
		if (sPresType.equalsIgnoreCase(GlobalConstants.PM_VOS_PRES_ADMIN)) {
			return computeFastAdminVos(sSessId, sUserId, sSessId);
		}

		// The base and secondary tables. For both, the key is the id of an
		// object attribute, and the value is a SimpleOattr object.
		Hashtable htBase = new Hashtable();
		Hashtable htSecond = new Hashtable();
		HashSet hsAccessible = new HashSet();
			    
		// Find user's name:
		String sUserName = getEntityName(sUserId, PM_NODE.USER.value);
		if (sUserName == null) return failurePacket("No user with such id");
		System.out.println("ComputeFastVos: User name is " + sUserName);
			    
		ArrayList queue = new ArrayList();
		HashSet visited = new HashSet();
		String sCrtId;
			    
		// Insert u's directly assigned attributes into the queue.
		Attributes attrs;
		Attribute attr;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sUserId + "," + sUserContainerDN);
			attr = attrs.get("pmToAttr");
			if (attr == null) return ADPacketHandler.getSuccessPacket();
			    
			for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
				String sAttrId = (String)enumer.next();
			    queue.add(sAttrId);
			    System.out.println("Attribute " + getEntityName(sAttrId, PM_NODE.UATTR.value) +
			    		" has been inserted into the queue");
			}
			  
			while (!queue.isEmpty()) {
				sCrtId = (String)queue.remove(0);
				System.out.println("Attribute " + getEntityName(sCrtId, PM_NODE.UATTR.value) +
			        	" has been removed from the queue");
				
			    if (!visited.contains(sCrtId)) {
			    	// VISIT sCrtId
			    	System.out.println("VISITING " + getEntityName(sCrtId, PM_NODE.UATTR.value));
			    	
			    	// For each opset ops such that sCrtId -> ops
			    	Vector opsets = getToOpsets(sCrtId);
			    	for (int ops = 0; ops < opsets.size(); ops++) {
			    		String sOpsId = (String)opsets.elementAt(ops);
			    	    System.out.println("Attr " + getEntityName(sCrtId, PM_NODE.UATTR.value) +
			    	    		" has attached opset " + getEntityName(sOpsId, PM_NODE.OPSET.value));

			    	    // Prepare the set of operations of this opset.
			    	    Attributes attrs12 = ServerConfig.ctx.getAttributes("CN=" + sOpsId + "," + sOpsetContainerDN);
			    	    Attribute attr12 = attrs12.get("pmOp");

			    	    // For each oa such that ops -> oa
			            Vector oattrs = getToAttrs(sOpsId);
			            for (int oa = 0; oa < oattrs.size(); oa++) {
			            	String sOaId = (String)oattrs.elementAt(oa);
			            	System.out.println("  Opset is assigned to oattr " +
			            		  getEntityName(sOaId, PM_NODE.OATTR.value));
			              
							// The base list is a Hashtable. An entry represents an oa
							// accessible to user u through u ->+ ua -> ops -> oa.
							// The key of an entry is the oa id. The value of an entry
							// is a SimpleOattr object (see the SimpleOattr class).
							  
							// The object attribute oa may or may not be in the base list.
							// If it is, find the entry for it. Otherwise, create a new
							// entry for it.
							// In both cases, add the operations in the opset (they are
							// in attr12) to the SimpleOattr.
			            	SimpleOattr so = null;
			            	if (htBase.containsKey(sOaId)) {
			            		so = (SimpleOattr)htBase.get(sOaId);
			            		System.out.println("Found key " +
			            			  getEntityName(sOaId, PM_NODE.OATTR.value) + " in the base");
			            	} else {
			            		so = new SimpleOattr(sOaId);
			            		htBase.put(sOaId, so);
			            		System.out.println("NOT found key, created " +
			            			  getEntityName(sOaId, PM_NODE.OATTR.value) + " in the base");
			            	}
			            	// Add the opset's operations to the object attribute
			            	if (attr12 != null) for (NamingEnumeration attrEnum = attr12.getAll(); attrEnum.hasMore(); ) {
			            		String sOp = (String)attrEnum.next();
			            		so.addOp(sOp);
			            	}
			            }
			        }
			    	  
			    	visited.add(sCrtId);
			        attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtId + "," + sUserAttrContainerDN);
			        attr = attrs.get("pmToAttr");
			        if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
			        	String sAttrId = (String)enumer.next();
			        	System.out.println("For child " + getEntityName(sAttrId, PM_NODE.UATTR.value));
			        	queue.add(sAttrId);
			        	System.out.println("Child Attribute " + getEntityName(sAttrId, PM_NODE.UATTR.value) +
			        			" has been inserted into the queue");
			        }
			    }
			}

			// For each oa from the base list, collect its pcs and store
			// them in its label in the list.
			System.out.println();
			System.out.println();
			System.out.println("COLLECT PCS");
			// For each oa in the base table:
			for (Enumeration keys = htBase.keys(); keys.hasMoreElements(); ) {
				String sOaId = (String)keys.nextElement();
				
			    // In addition, add the oa to the list of accessible oas.
			    hsAccessible.add(sOaId);
	  
			    System.out.println("For Object attribute " + getEntityName(sOaId, PM_NODE.OATTR.value));
			    SimpleOattr so = (SimpleOattr)htBase.get(sOaId);

			    // Initialize the queue and visited.
			    queue = new ArrayList();
			    visited = new HashSet();
			    // Insert oa into the queue.
			    queue.add(sOaId);
			    System.out.println(" Object attribute " + getEntityName(sOaId, PM_NODE.OATTR.value) +
			    		" has been inserted into the queue");

			    // while queue is not empty
			    while (!queue.isEmpty()) {
			    	// Extract current element from the queue.
			    	sCrtId = (String)queue.remove(0);
			    	// If not yet visited
			    	if (!visited.contains(sCrtId)) {
			    		// Mark it as visited
			    		visited.add(sCrtId);
			    		// Extract its direct descendant attributes and insert them into the queue
			    		attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtId + "," + sObjAttrContainerDN);
			    		attr = attrs.get("pmToAttr");
			    		if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
			    			String sAttrId = (String)enumer.next();
			    			queue.add(sAttrId);
			    		}
			    		// Extract current node's directly assigned policy classes
			    		// and add them to the oa's label if not already there.
			    		attr = attrs.get("pmToPolicy");
			    		if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
			    			String sPcId = (String)enumer.next();
			    			so.addPc(sPcId);
			    		}
			    	}
			    }

			}
			      
			// List the base table.
			System.out.println("THE BASE LIST");
			for (Enumeration keys = htBase.keys(); keys.hasMoreElements() ;) {
				String sKey = (String)keys.nextElement();
				String sName = getEntityName(sKey, PM_NODE.OATTR.value);
				System.out.println("Object attribute " + sName);
				SimpleOattr so = (SimpleOattr)htBase.get(sKey);
				System.out.println("  Operations");
				ArrayList al = so.getOps();
				for (int i = 0; i < al.size(); i++) {
					System.out.println("    " + (String)al.get(i));
				}
				System.out.println("  Policy classes");
				al = so.getPcs();
				for (int i = 0; i < al.size(); i++) {
					System.out.println("    " + getEntityName((String)al.get(i), PM_NODE.POL.value));
				}
			}
			      
			// Propagate capabilities in policy classes to ascendant oattrs.
			System.out.println();
			System.out.println();
			System.out.println("PROPAGATE OPs AND PCs");
			// For each oa in the base table:
			for (Enumeration keys = htBase.keys(); keys.hasMoreElements(); ) {
				String sOaId = (String)keys.nextElement();
				System.out.println("For Object attribute " + getEntityName(sOaId, PM_NODE.OATTR.value));
				SimpleOattr so = (SimpleOattr)htBase.get(sOaId);
				
				// Initialize the queue and visited.
				queue = new ArrayList();
				visited = new HashSet();
				// Insert oa's direct ascendants into the queue.
				attrs = ServerConfig.ctx.getAttributes("CN=" + sOaId + "," + sObjAttrContainerDN);
				attr = attrs.get("pmFromAttr");
				if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
					String sAttrId = (String)enumer.next();
					queue.add(sAttrId);
				}
			          
				// while queue is not empty
				while (!queue.isEmpty()) {
					// Extract current element from the queue.
					sCrtId = (String)queue.remove(0);
					// If not visited
					if (!visited.contains(sCrtId)) {
						// Mark it as visited
						visited.add(sCrtId);
						// Extract its direct ascendant attributes and insert them into the queue
						attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtId + "," + sObjAttrContainerDN);
						attr = attrs.get("pmFromAttr");
						if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
							String sAttrId = (String)enumer.next();
							queue.add(sAttrId);
						}
						// Add the current node to the secondary table if not already.
						// Note that the current node may be in the secondary list
						// even if it was not visited (may have been visited and inserted
						// during the propagation phase for another oa).
						SimpleOattr soCrt = null;
						if (htSecond.containsKey(sCrtId)) {
							soCrt = (SimpleOattr)htSecond.get(sCrtId);
							System.out.println("Found key " +
									getEntityName(sCrtId, PM_NODE.OATTR.value) + " in the secondary");
						} else {
							soCrt = new SimpleOattr(sCrtId);
							htSecond.put(sCrtId, soCrt);
							System.out.println("NOT found key, created " +
									getEntityName(sCrtId, PM_NODE.OATTR.value) + " in the secondary");
						}
						// Add the operations and pcs of the oa for which we run this
						// to the label of the current node extracted from queue.
						soCrt.addOps(so.getOps());
						soCrt.addPcs(so.getPcs());
					}
				}
			}
			// List the secondary table.
			System.out.println("THE SECOND LIST");
			for (Enumeration keys = htSecond.keys(); keys.hasMoreElements() ;) {
				String sKey = (String)keys.nextElement();
				String sName = getEntityName(sKey, PM_NODE.OATTR.value);
				System.out.println("Object attribute " + sName);
				SimpleOattr so = (SimpleOattr)htSecond.get(sKey);
				System.out.println("  Operations");
				ArrayList al = so.getOps();
				for (int i = 0; i < al.size(); i++) {
					System.out.println("    " + (String)al.get(i));
				}
				System.out.println("  Policy classes");
				al = so.getPcs();
				for (int i = 0; i < al.size(); i++) {
					System.out.println("    " + getEntityName((String)al.get(i), PM_NODE.POL.value));
				}
			}
			      
			      
			// For each oa from the secondary list, find out if there is any
			// policy class which is NOT on its label. If you find one, then
			// that oa is not accessible.
			System.out.println();
			System.out.println();
			System.out.println("ACCESSIBLE OAs");
			boolean isAccessible;
			// For each oa in the second table:
			for (Enumeration keys = htSecond.keys(); keys.hasMoreElements(); ) {
				String sOaId = (String)keys.nextElement();
				//System.out.println("For Object attribute " + getEntityName(sOaId, PM_NODE_OATTR));
				SimpleOattr so = (SimpleOattr)htSecond.get(sOaId);
			    	  
				// Initialize the queue and visited.
				queue = new ArrayList();
				visited = new HashSet();
				isAccessible = true;
				// Insert oa into the queue.
				queue.add(sOaId);
				//System.out.println(" Object attribute " + getEntityName(sOaId, PM_NODE_OATTR) +
				//	  " has been inserted into the queue");
			          
				// while queue is not empty
				while (!queue.isEmpty() && isAccessible) {
					// Extract current element from the queue.
					sCrtId = (String)queue.remove(0);
					// If not visited
					if (!visited.contains(sCrtId)) {
						// Mark it as visited
						visited.add(sCrtId);
						// Extract its direct descendant attributes and insert them into the queue
						attrs = ServerConfig.ctx.getAttributes("CN=" + sCrtId + "," + sObjAttrContainerDN);
						attr = attrs.get("pmToAttr");
						if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
							String sAttrId = (String)enumer.next();
							queue.add(sAttrId);
						}
						// Extract current node's directly assigned policy classes
						// and see if any of them is NOT in the oa's label.
						attr = attrs.get("pmToPolicy");
						if (attr != null) for (NamingEnumeration enumer = attr.getAll(); enumer.hasMore(); ) {
							String sPcId = (String)enumer.next();
							if (!so.containsPc(sPcId)) {
								isAccessible = false;
								break;
							}
						}
					}
				}
				// The boolean isAccessible indicates the accessibility of sOaId.
				if (isAccessible) {
					hsAccessible.add(sOaId);
				}
			}
			      
			Iterator iterAcc = hsAccessible.iterator();
			while (iterAcc.hasNext()) {
				String sOaId = (String)iterAcc.next();
				System.out.println(getEntityName(sOaId, PM_NODE.OATTR.value));
			}
			//JOptionPane.showMessageDialog(null, "END of Accessible OAs for " + sUserName);
			      
			if (htAcc.containsKey(sUserId)) htAcc.remove(sUserId);
			htAcc.put(sUserId, hsAccessible);
			      
			/*
			buildFastVosSet(hsAccessible, sSessId, sUserName, sUserId);

			System.out.println("        TESTING getFastVosMembersOf()");
			System.out.println("     getFastVosMembersOf(Connector)");
			Packet res = (Packet)getFastVosMembersOf(sSessId, GlobalConstants.PM_CONNECTOR_NAME, GlobalConstants.PM_CONNECTOR_ID,
			    		  PM_NODE.CONN.value, GlobalConstants.PM_VOS_PRES_USER);
			      res.print(true, "TEST");

			      JTextField workField = new JTextField();
			      String message = "VOS id";
			      String sString;
			      while (true) {
			        int result = JOptionPane.showOptionDialog(null,
			                          new Object[] {message, workField},
			                          "VOS ID", JOptionPane.OK_CANCEL_OPTION,
			                          JOptionPane.QUESTION_MESSAGE, null, null, null);
			        if (result == JOptionPane.CANCEL_OPTION) break;
			        sString = workField.getText().trim();
			        if (sString.length() == 0) break;
			        
			        String[] pieces = sString.split(PM_FIELD_DELIM);
			        System.out.println("     getFastVosMembersOf(ses=" + sSessId + ", name=" +
			        		pieces[2] + ", id=" + pieces[1] + ", type=" + pieces[0]);
			        res = (Packet)getFastVosMembersOf(sSessId, pieces[2], pieces[1],
			      		  pieces[0], PM_VOS_PRES_USER);
			        res.print(true, "VOS");
			      }
				*/
			      
			      
			      
			      
			      
			      
			      
			      
			      
			      
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception during graph visitation" + e.getMessage());
		}

		return ADPacketHandler.getSuccessPacket();
	}
		  
	private String newId() {
		RandomGUID myGUID = new RandomGUID();
		return myGUID.toStringNoDashes();
	}

/*
			  private void buildFastVosSet(HashSet hs, String sSessId, String sUserName, String sUserId)
			  throws Exception {
				  Attributes attrs;
				  
				  // First delete previous records for the session:
			      deleteFastVosRecords(sSessId);
			      
				  // Build connector node.
				  attrs = new BasicAttributes(true);
				  attrs.put("objectClass", sVosNodeClass);
				  String sConnectorVosId = newId(); // Might be needed farther
				  attrs.put("pmId", sConnectorVosId);
				  attrs.put("pmOriginalId", GlobalConstants.PM_CONNECTOR_ID);
				  attrs.put("pmOriginalName", GlobalConstants.PM_CONNECTOR_NAME);
				  attrs.put("pmUserId", sUserId);
				  attrs.put("pmUserName", sUserName);
				  attrs.put("pmObjClass", sConnectorClass);
				  attrs.put("pmIsObj", "FALSE");
				  attrs.put("pmSessId", sSessId);
				  ctx.bind("CN=" + sConnectorVosId + "," + sFastVosNodeContainerDN, null, attrs);
				  System.out.println("***Added accessible root " + GlobalConstants.PM_CONNECTOR_NAME);    

			      // Build nodes for policy classes.
				  Vector v = getPolicyClasses();
				  for (int i = 0; i < v.size(); i++) {
					  String sPcId = (String)v.get(i);
					  String sPcName = getEntityName(sPcId, PM_NODE.POL.value);
					  attrs = new BasicAttributes(true);
					  attrs.put("objectClass", sVosNodeClass);
					  String sPcVosId = newId();
					  attrs.put("pmId", sPcVosId);
					  attrs.put("pmOriginalId", sPcId);
					  attrs.put("pmOriginalName", sPcName);
					  attrs.put("pmUserId", sUserId);
					  attrs.put("pmUserName", sUserName);
					  attrs.put("pmObjClass", sPolicyClass);
					  attrs.put("pmIsObj", "FALSE");
					  attrs.put("pmSessId", sSessId);
					  ServerConfig.ctx.bind("CN=" + sPcVosId + "," + sFastVosNodeContainerDN, null, attrs);
					  System.out.println("***Added accessible policy " + sPcName);    
				  }

			      Iterator iterAcc = hs.iterator();
			      while (iterAcc.hasNext()) {
			    	  String sOaId = (String)iterAcc.next();
					  String sOaName = getEntityName(sOaId, PM_NODE.OATTR.value);
			    	  attrs = new BasicAttributes(true);
			    	  attrs.put("objectClass", sVosNodeClass);
			          String sOaVosId = newId();
			          attrs.put("pmId", sOaVosId);
			          attrs.put("pmOriginalId", sOaId);
			          attrs.put("pmOriginalName", sOaName);
			          attrs.put("pmUserId", sUserId);
			          attrs.put("pmUserName", sUserName);
			          attrs.put("pmObjClass", sObjAttrClass);
			          if (hasAssocObj(sOaId)) attrs.put("pmIsObj", "TRUE");
			          else attrs.put("pmIsObj", "FALSE");
			          attrs.put("pmSessId", sSessId);
			          ctx.bind("CN=" + sOaVosId + "," + sFastVosNodeContainerDN, null, attrs);
			          System.out.println("***Added accessible oattr " + sOaName);    
			      }
			  }
*/

	
	// sPresType is the presentation type: administrative or end-user.
	public Packet computeVos(String sClientId, String sPresType,
			String sUserId, String sSessId) {
		if (sPresType.equalsIgnoreCase(GlobalConstants.PM_VOS_PRES_ADMIN)) {
			return computeAdminVos(sSessId, sUserId, sSessId);
		}
		String sUserName;
		Vector<String> policyClasses;
		Vector<String> opsets;
		Vector<String> oattrs;
		Vector<String> aoattrs; // accessible (to user) object attributes.
		String sPcId;
		String sPcName;
		String sOpsId;
		String sOaId;

		// Find user's name:
		sUserName = getEntityName(sUserId,PM_NODE.USER.value);
		if (sUserName == null) {
			return failurePacket("No user with such id");
		}

		// Get all policy classes:
		policyClasses = getPolicyClasses();

		// Get user's attributes (ua such that u ->+ ua):
		HashSet<String> authattrs = getUserDescendantsInternal(sUserId);
		System.out.println("authattrs: " + authattrs);
		// Init a HashMap whose entries will contain the MinAOA for various pcs.
		// The key of an entry is the pcId.
		// The value of an entry is a vector containing MinAOA(u,pc).
		HashMap<String, Vector<String>> hm = new HashMap<String, Vector<String>>();

		// For each policy class pc:
		for (int pc = 0; pc < policyClasses.size(); pc++) {
			sPcId = policyClasses.elementAt(pc);
			sPcName = getEntityName(sPcId,PM_NODE.POL.value);

			// Init new vector for MinAOA(u,pc) and create entry for crt. pc:
			Vector<String> minaoattrs = new Vector<String>();
			hm.put(sPcId, minaoattrs);

			// New vector for AOA(u,pc)
			aoattrs = new Vector<String>();

			// For each user attribute ua such that ua ->+ pc:
			Iterator<String> uaiter = authattrs.iterator();
			while (uaiter.hasNext()) {
				String sUaId = uaiter.next();
				System.out.println("sUaId " + sUaId);
				if (!attrIsAscendantToPolicy(sUaId,PM_NODE.UATTR.value, sPcId)) {
					System.out.println("sUaId is ascenden of pollicy " + sUaId);
					continue;
				}

				// For each ops such that ua->ops:
				opsets = getToOpsets(sUaId);
				System.out.println("opsets: " + opsets);
				System.out.println("opsets size = " + opsets.size());
				for (int ops = 0; ops < opsets.size(); ops++) {
					System.out.println("ops: " + ops);
					sOpsId = opsets.elementAt(ops);
					System.out.println("sOpsId: " + sOpsId);
					// For each oa such that ops -> oa
					oattrs = getToAttrs(sOpsId);
					System.out.println("oattrs: " + oattrs);
					System.out.println("oattrs size = " + oattrs.size());
					//}
					for (int oa = 0; oa < oattrs.size(); oa++) {
						System.out.println("oa: " + oa);
						sOaId = oattrs.elementAt(oa);
						System.out.println("sOaId: " + sOaId);
						// Added (March 2006) to ignore the objects that
						// represents PM entities.
						if (ignoreOattrInVos(sOaId)) {
							System.out.println("ignored " + sOaId);
							continue;
						}

						// Retain only oa such that oa ->+ pc:
						if (!attrIsAscendantToPolicy(sOaId,PM_NODE.OATTR.value,
								sPcId)) {
							System.out.println("attrr is not ascendent");
							continue;
						}
						if (!aoattrs.contains(sOaId)) {
							aoattrs.add(sOaId);
						}
					}
				}
			}
			// Now we have the AOA(u,pc) set. Let's print its elements:
			printVector(aoattrs,PM_NODE.OATTR.value,
					"Accessible object attributes in policy " + sPcName);

			// For each oam in AOA(u,pc):
			boolean isMinimal;
			for (int oam = 0; oam < aoattrs.size(); oam++) {
				String sOaIdM = aoattrs.elementAt(oam);
				isMinimal = true;
				// For each oa in AOA(u,pc), oa != oam:
				for (int oa = 0; oa < aoattrs.size(); oa++) {
					if (oa == oam) {
						continue;
					}
					sOaId = aoattrs.elementAt(oa);
					if (attrIsAscendant(sOaIdM, sOaId,PM_NODE.OATTR.value)) {
						isMinimal = false;
						break;
					}
				}
				if (isMinimal) {
					minaoattrs.add(sOaIdM);
				}
			}
		}

		// Now the hm entry with key = pc contains MinAOA(u,pc). Let's print
		// them.
		Set<String> keyset = hm.keySet();
		Iterator<String> iter = keyset.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			String sName = getEntityName(key,PM_NODE.POL.value);
			Vector<String> v = hm.get(key);
			printVector(v,PM_NODE.OATTR.value, "MinAOA(" + sName + ")");

			// It's incorrect to ignore empty policy classes at this stage!!!
			// if (v.isEmpty()) iter.remove();
		}

		// For testing only.
		// For each key=pc of hm
		// For each element minimal in pc
		// For each object attribute ascendant
		// Print whether it is visible.
		iter = keyset.iterator();
		HashSet<String> attrSet = new HashSet<String>();

		while (iter.hasNext()) {
			sPcId = iter.next();
			sPcName = getEntityName(sPcId,PM_NODE.POL.value);
			System.out.println("//For pc " + sPcName + ":");
			Vector<String> v = hm.get(sPcId);
			for (int i = 0; i < v.size(); i++) {
				String sOamId = v.elementAt(i);
				String sOamName = getEntityName(sOamId,PM_NODE.OATTR.value);
				attrSet.clear();
				getAscAttrs(sOamId,PM_NODE.OATTR.value, attrSet);
				System.out.println("////For attr " + sOamName
						+ " and its ascendants:");
				Iterator<String> oamiter = attrSet.iterator();
				while (oamiter.hasNext()) {
					sOaId = oamiter.next();
					String sOaName = getEntityName(sOaId,PM_NODE.OATTR.value);
					if (attrIsVisible(sOaId, sPcId, hm)) {
						System.out.println("   ascendant " + sOaName
								+ " is visible.");
					} else {
						System.out.println("   ascendant " + sOaName
								+ " is NOT visible.");
					}
				}
			}
		}

		// Build the VOS graph.
		buildVosGraph(hm, sUserId, sUserName, sSessId);
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, dfUpdate.format(new Date()));
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return res;
	}


	public void deleteAdminVosRecords(String sSessId) throws Exception {
		NamingEnumeration<?> members;
		String sDn;

		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		constraints.setReturningAttributes(null);
		members = ServerConfig.ctx.search(sAdminVosNodeContainerDN, "(pmSessId=" + sSessId
				+ ")", constraints);
		while (members != null && members.hasMore()) {
			sDn = ((SearchResult) members.next()).getName() + ","
					+ sAdminVosNodeContainerDN;

			ServerConfig.ctx.destroySubcontext(sDn);
		}
	}


	public void deleteVosRecords(String sSessId) throws Exception {
		NamingEnumeration<?> members;
		String sDn;

		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		constraints.setReturningAttributes(null);
		members = ServerConfig.ctx.search(sVosNodeContainerDN, "(pmSessId=" + sSessId + ")",
				constraints);
		while (members != null && members.hasMore()) {
			sDn = ((SearchResult) members.next()).getName() + ","
					+ sVosNodeContainerDN;
			ServerConfig.ctx.destroySubcontext(sDn);
		}
	}


	public void buildVosGraph(HashMap<String, Vector<String>> minAoa,
			String sUserId, String sUserName, String sSessId) {
		Attributes attrs;
		String sConnNewId;
		String sPcNewId;
		String sOamNewId;
		String sOaNewId;

		// The id of the node used as direct descendant of a visible node.
		// It is not necessarily the true descendant of the visible node.
		// The visible portions of a branch may have invisible gaps between
		// them.

		try {
			// First delete all records for the session.
			deleteVosRecords(sSessId);

			// Build connector node.
			attrs = new BasicAttributes(true);
			attrs.put("objectClass", sVosNodeClass);
			RandomGUID myGUID = new RandomGUID();
			sConnNewId = myGUID.toStringNoDashes();
			attrs.put("pmId", sConnNewId);
			// attrs.put("pmId", GlobalConstants.PM_CONNECTOR_ID);
			attrs.put("pmOriginalId", GlobalConstants.PM_CONNECTOR_ID);
			attrs.put("pmOriginalName", GlobalConstants.PM_CONNECTOR_NAME);
			attrs.put("pmUserId", sUserId);
			attrs.put("pmUserName", sUserName);
			attrs.put("pmObjClass", sConnectorClass);
			attrs.put("pmIsObj", "FALSE");
			attrs.put("pmSessId", sSessId);
			ServerConfig.ctx.bind("CN=" + sConnNewId + "," + sVosNodeContainerDN, null,
					attrs);
			// System.out.println("***Built connector node " + GlobalConstants.PM_CONNECTOR_NAME
			// + "(" + sConnNewId + ")");

			// Build nodes for policy classes.
			HashSet<String> visitedSet = new HashSet<String>();
			ArrayList<VosQueueElement> queue = new ArrayList<VosQueueElement>();
			VosQueueElement qe, crtQe;

			Set<String> keyset = minAoa.keySet();
			Iterator<String> iter = keyset.iterator();
			// For each policy class pc:
			while (iter.hasNext()) {
				String sPcId = iter.next();
				String sPcName = getEntityName(sPcId,PM_NODE.POL.value);

				// Build the policy class node and the relation pc->pm:
				attrs = new BasicAttributes(true);
				attrs.put("objectClass", sVosNodeClass);
				myGUID = new RandomGUID();
				sPcNewId = myGUID.toStringNoDashes();
				attrs.put("pmId", sPcNewId);
				attrs.put("pmOriginalId", sPcId);
				attrs.put("pmOriginalName", sPcName);
				attrs.put("pmUserId", sUserId);
				attrs.put("pmUserName", sUserName);
				attrs.put("pmObjClass", sPolicyClass);
				attrs.put("pmIsObj", "FALSE");
				attrs.put("pmSessId", sSessId);
				ServerConfig.ctx.bind("CN=" + sPcNewId + "," + sVosNodeContainerDN, null,
						attrs);
				// System.out.println("***Built policy node " + sPcName + "(" +
				// sPcNewId + ")");

				if (!addVosDoubleLink(sPcNewId, sConnNewId)) {
					System.out.println("Error when setting links policy class - connector");
					return;
				}

				// Get the set MinAOA(u,pc):
				Vector<String> v = minAoa.get(sPcId);
				// For each oam in MinAOA(u,pc):
				for (int i = 0; i < v.size(); i++) {
					String sOamId = v.elementAt(i);
					String sOamName = getEntityName(sOamId,PM_NODE.OATTR.value);

					// If oam is visible:
					if (attrIsVisible(sOamId, sPcId, minAoa)) {
						// Note that even a minimal node may have been already
						// visited.
						if (!visitedSet.contains(sOamId)) {
							// Put oam in the queue together with itself as
							// descendant of its
							// direct ascendants, because it is visible:
							// Mark it as visited:
							visitedSet.add(sOamId);

							// Create node for oam:
							attrs = new BasicAttributes(true);
							attrs.put("objectClass", sVosNodeClass);
							myGUID = new RandomGUID();
							sOamNewId = myGUID.toStringNoDashes();
							attrs.put("pmId", sOamNewId);
							attrs.put("pmOriginalId", sOamId);
							attrs.put("pmOriginalName", sOamName);
							attrs.put("pmUserId", sUserId);
							attrs.put("pmUserName", sUserName);
							attrs.put("pmObjClass", sObjAttrClass);
							if (hasAssocObj(sOamId)) {
								attrs.put("pmIsObj", "TRUE");
							} else {
								attrs.put("pmIsObj", "FALSE");
							}
							attrs.put("pmSessId", sSessId);
							ServerConfig.ctx.bind("CN=" + sOamNewId + ","
									+ sVosNodeContainerDN, null, attrs);
							// System.out.println("***Built min attribute node "
							// + sOamName + "(" + sOamNewId + ")");

							// Add a record for oam in the queue:
							crtQe = new VosQueueElement(sOamId, sOamNewId);
							queue.add(crtQe);
						}
						// Create double link oam->pc (between new ids). I know
						// pc's new id,
						// but not oam's new id (unless it was just created
						// before).
						// So, search for it:
						sOamNewId = getVosNodeId(sOamId, sSessId);
						if (!addVosDoubleLink(sOamNewId, sPcNewId)) {
							System.out.println("Error when setting links object attribute - policy class");
							return;
						}
					} else { // oam is NOT visible:
						if (!visitedSet.contains(sOamId)) {
							// Put oam in the queue together with pc as
							// descendant of its
							// direct ascendants, because it is NOT visible:
							crtQe = new VosQueueElement(sOamId, sPcNewId);
							queue.add(crtQe);
							// Mark it as visited:
							visitedSet.add(sOamId);
						}
					}

					// While queue is not empty
					while (!queue.isEmpty()) {
						// Extract first element {crtoa, crtdesc} from the
						// queue:
						crtQe = queue.remove(0);
						String sCrtOaId = crtQe.getId();
						String sCrtDescId = crtQe.getDesc();
						String sCrtOaName = getEntityName(sCrtOaId,
								PM_NODE.OATTR.value);

						if (ServerConfig.debugFlag) {
							System.out.println("Out of queue: " + sCrtOaName
									+ "(" + sCrtOaId + "), " + sCrtDescId);
						}
						// For each direct ascendant oa of crtoa:
						Attribute attr = getFromAttrs(sCrtOaId,PM_NODE.OATTR.value);
						if (attr != null) {
							for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
								String sOaId = (String) attrEnum.next();
								String sOaName = getEntityName(sOaId,
										PM_NODE.OATTR.value);
								// System.out.println("***Processing direct ascendant: "
								// + sOaName + "(" + sOaId + ")");
								// If oa is visible and NOT to be ignored in
								// VOS:
								if (attrIsVisible(sOaId, sPcId, minAoa)
										&& !ignoreOattrInVos(sOaId)) {
									if (!visitedSet.contains(sOaId)) {
										visitedSet.add(sOaId);
										// Create node oa:
										attrs = new BasicAttributes(true);
										attrs.put("objectClass", sVosNodeClass);
										myGUID = new RandomGUID();
										sOaNewId = myGUID.toStringNoDashes();
										attrs.put("pmId", sOaNewId);
										attrs.put("pmOriginalId", sOaId);
										attrs.put("pmOriginalName", sOaName);
										attrs.put("pmUserId", sUserId);
										attrs.put("pmUserName", sUserName);
										attrs.put("pmObjClass", sObjAttrClass);
										if (hasAssocObj(sOaId)) {
											attrs.put("pmIsObj", "TRUE");
										} else {
											attrs.put("pmIsObj", "FALSE");
										}
										attrs.put("pmSessId", sSessId);
										ServerConfig.ctx.bind("CN=" + sOaNewId + ","
												+ sVosNodeContainerDN, null,
												attrs);
										// System.out.println("***Built attribute node "
										// + sOaName + "(" + sOaNewId + ")");

										// Because oa is visible, it will be
										// displayed, and its direct
										// ascendants will be pointing down to
										// it:
										qe = new VosQueueElement(sOaId,
												sOaNewId);
										queue.add(qe);
									}

									// Create double link oa->crtdesc (between
									// new ids). Crtdesc
									// is already a new id (it was obtained from
									// the queue).
									// But I don't know oa's new id (unless it
									// was just created before).
									// So, search for it:
									sOaNewId = getVosNodeId(sOaId, sSessId);
									if (!addVosDoubleLink(sOaNewId, sCrtDescId)) {
										System.out.println("Error when setting links object attribute - object attribute");
										return;
									}
								} else {// oa is not visible.
									if (!visitedSet.contains(sOaId)) {
										// Because oa is not visible, its
										// ascendants
										// will be linked down to the crt. desc:
										qe = new VosQueueElement(sOaId,
												sCrtDescId);
										queue.add(qe);
										visitedSet.add(sOaId);
									}
								}
							}
						}
					}// while queue not empty
				}// for each oam
			}// for each pc
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			System.out.println("Error while building VOS graph");
			return;
		}
	}

	// Returns true = success.

	public boolean addVosDoubleLink(String sId1, String sId2) {
		ModificationItem[] mods = new ModificationItem[1];

		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmDescNode", sId2));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sId1 + "," + sVosNodeContainerDN, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
				new BasicAttribute("pmAscNode", sId1));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sId2 + "," + sVosNodeContainerDN, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}

	// Returns whether object attribute oa is visible by the user u for which
	// {MinAOA(u,pc)| pc in PC} has been computed.
	// Attribute oa is not arbitrary. There must be a policy class pc and an
	// object attribute oam in MinAOA(u,pc) such that oa ->* oam.
	// Attribute oa is visible by user u iff there is no pc' != pc such
	// that ((oa ->+ pc') and (there is no oam' in MinAOA(u,pc') with
	// oa->*oam')).
	// sOaId is the id of oa.
	// sPcId is the id of pc.
	// Each entry in hm has a pc as key and MinAOA(u,pc) (as a vector) as value.

	public boolean attrIsVisible(String sOaId, String sPcId,
			HashMap<String, Vector<String>> hm) {
		// Try to find pc' != pc such that oa ->+ pc'. Use the hm keys to get
		// pc'.
		Set<String> keyset = hm.keySet();
		Iterator<String> iter = keyset.iterator();
		while (iter.hasNext()) {
			String sPcIdPrim = iter.next();
			if (sPcIdPrim.equals(sPcId)) {
				continue;
			}
			if (!attrIsAscendantToPolicy(sOaId,PM_NODE.OATTR.value, sPcIdPrim)) {
				continue;
			}
			// The vector MinAOA(u,pc'):
			Vector<String> v = hm.get(sPcIdPrim);
			// Looking for a oam' such that oa ->* oam'.
			// If we don't find one, then oa is not visible.
			boolean foundOaPrim = false;
			for (int i = 0; i < v.size(); i++) {
				String sOaIdPrim = v.elementAt(i);
				if (attrIsAscendant(sOaId, sOaIdPrim,PM_NODE.OATTR.value)) {
					foundOaPrim = true;
					break;
				}
			}
			if (!foundOaPrim) {
				return false;
			}
		}
		return true;
	}


	public Packet deleteHost(String sSessId, String sHostId) {
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sHostId + "," + sHostContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete host with id " + sHostId);
		}
		return ADPacketHandler.getSuccessPacket();
	}


	public Packet updateHost(String sSessId, String sHostId, String sHost,
			String sRepo, String sIpa, String sDescr, String sPdc) {
		// Check permissions...

		// Test that the host id exists.
		String s = getEntityName(sHostId, GlobalConstants.PM_HOST);
		if (s == null) {
			return failurePacket("No host with id " + sHostId);
		}

		// Convert the boolean to upper case.
		sPdc = sPdc.toUpperCase();

		// Prepare the attributes to be updated.
		ModificationItem[] mods = new ModificationItem[5];

		System.out.println("pmName=" + sHost);
		System.out.println("pmPath=" + sRepo);
		System.out.println("pmIsDomController=" + sPdc);
		System.out.println("pmDescription=" + sDescr);
		System.out.println("pmIpAddress=" + sIpa);

		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("pmName", sHost));
		mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("pmPath", sRepo));
		mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("pmIsDomController", sPdc));
		mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("pmDescription", sDescr));
		mods[4] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("pmIpAddress", sIpa));
		try {
			ServerConfig.ctx.modifyAttributes("CN=" + sHostId + "," + sHostContainerDN, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Couldn't update host with id " + sHostId);
		}
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sHost + GlobalConstants.PM_FIELD_DELIM
					+ sHostId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when updating the host: "
					+ e.getMessage());
		}
		return res;
	}


	// Get all hosts. Each item has the format: <host name>:<host id>.

	public Packet getHosts(String sClientId) throws Exception {
		Packet res = new Packet();
		NamingEnumeration<?> hosts;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			hosts = ServerConfig.ctx.search(sHostContainerDN, "(objectClass=*)", constraints);
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}

		while (hosts != null && hosts.hasMore()) {
			SearchResult sr = (SearchResult) hosts.next();
			String sName = (String) sr.getAttributes().get("pmName").get();
			String sId = (String) sr.getAttributes().get("pmId").get();
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		}
		return res;
	}


	public Packet getHostInfo(String sSessId, String sHostId) {
		Packet res = new Packet();
		Attributes attrs;
		String sIsPdc, sName, sRepoPath;

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sHostId + "," + sHostContainerDN);

			// First insert the name, id, and pdc.
			sName = (String) attrs.get("pmName").get();
			sIsPdc = (String) attrs.get("pmIsDomController").get();
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
					+ sHostId + GlobalConstants.PM_FIELD_DELIM + sIsPdc);

			// Now the repository path.
			sRepoPath = (String) attrs.get("pmPath").get();
			res.addItem(ItemType.RESPONSE_TEXT, sRepoPath);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getHostInfo(): "
					+ e.getMessage());
		}
		return res;
	}




	// Cannot delete object attributes associated to objects -
	// you have to delete the object.

	public Packet deleteNode(String sSessId, String sId, String sType,
			String sIsVos) {
		System.out.println("============================= sType = " + sType + " ========================================");
		if (sIsVos.equalsIgnoreCase("yes")) {
			sId = getAdminVosNodeOrigId(sId);
		}

        Packet p = new Packet();
		if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
            p = deleteUser(sSessId, sId);
		} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
            p = deleteUattr(sSessId, sId);
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
            p = deletePolicyClass(sSessId, sId);
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            p = deleteOattr(sSessId, sId, true); // true means verify no
		} // opsets assigned to it.
		else if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
            p = deleteOpset(sSessId, sId);
		} else {
            p = failurePacket("This node cannot be deleted");
		}

        //deleteADGraphNode(sId);
        
        return  p;
	}

	// Delete an object attribute. It can be deleted only if it's not associated
	// to an object, and does not have ascendants.

	public Packet deleteOattr(String sSessId, String sIdToDelete,
			boolean verifyOpsetsAndAssoc) {
		if (!entityExists(sIdToDelete,PM_NODE.OATTR.value)) {
			return failurePacket("No object attribute with id " + sIdToDelete);
		}
		if (attrHasAscendants(sIdToDelete,PM_NODE.OATTR.value)) {
			return failurePacket("Object attribute has ascendants");
		}
		if (verifyOpsetsAndAssoc && hasAssocObj(sIdToDelete)) {
			return failurePacket("Object attribute is associated to an object");
		}
		if (verifyOpsetsAndAssoc && attrHasOpsets(sIdToDelete,PM_NODE.OATTR.value)) {
			return failurePacket("An operation set is assigned to this object attribute");
		}

		// Delete the assignment this node ---> descendant attributes.
		Packet res;
		Attribute attr;
		String sId;

		try {
			attr = getToAttrs(sIdToDelete,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sId = (String) enumer.next();

					// Now delete the assignment sIdToDelete (oattr) ---> sId
					// (oattr).
					res =  deleteDoubleLink(sIdToDelete,PM_NODE.OATTR.value,
							sId,PM_NODE.OATTR.value);
					if (res.hasError()) {
						return res;
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket(e.getMessage());
		}

		// Get the policy classes this object attribute is assigned to and
		// delete the assignment.
		try {
			attr = getToPolicies(sIdToDelete,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sId = (String) enumer.next();

					// Now delete the assignment sIdToDelete (oattr) ---> sId
					// (policy class).
					res =  deleteDoubleLink(sIdToDelete,PM_NODE.OATTR.value,
							sId,PM_NODE.POL.value);
					if (res.hasError()) {
						return res;
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket(e.getMessage());
		}

		// If this attribute is assigned to the connector node, delete the
		// assignment.
		if (attrIsAssignedToConnector(sIdToDelete,PM_NODE.OATTR.value)) {
			// Now delete the assignment sIdToDelete (oattr) ---> Connector
			// node.
			res =  deleteDoubleLink(sIdToDelete,PM_NODE.OATTR.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}

		// Destroy the object attribute PM object.
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sIdToDelete + ","
					+ sObjAttrContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete object attribute "
					+ sIdToDelete);
		}

        //deleteADGraphNode(sIdToDelete);

		return ADPacketHandler.getSuccessPacket();
	}

	// Called for the "deleteNode" command, triggered when the user right-clicks
	// on the opset and selects the "delete node" popup menu.

	public Packet deleteOpset(String sClientId, String sIdToDelete) {
		return deleteOpsetAndOp(sClientId, sIdToDelete, null);
	}

	// Delete a PM user. Just delete all its assignments
	// to user attributes, delete its assignment to the connector node
	// if it's assigned, and delete the user object.

	public Packet deleteUser(String sSessId, String sIdToDelete) {
		// Get user attributes (directly) assigned to the user we want to
		// delete.
		Packet res;
		try {
			Attribute attr = getToAttrs(sIdToDelete,PM_NODE.USER.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();

					// Now delete the assignment sIdToDelete (user) ---> sId
					// (uattr).
					res =  deleteDoubleLink(sIdToDelete,PM_NODE.USER.value,
							sId,PM_NODE.UATTR.value);
					if (res.hasError()) {
						return res;
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket(e.getMessage());
		}

		// If this user is assigned to the connector node, delete the
		// assignment.
		if (userIsAssignedToConnector(sIdToDelete)) {
			res =  deleteDoubleLink(sIdToDelete,PM_NODE.USER.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}

		// Now delete the user object.
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sIdToDelete + "," + sUserContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete user " + sIdToDelete);
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Delete a user attribute. We can delete it only if it's isolated, i.e., no
	// node is
	// assigned to it (it has no ascendants).

	public Packet deleteUattr(String sClientId, String sIdToDelete) {
		if (!entityExists(sIdToDelete,PM_NODE.UATTR.value)) {
			return failurePacket("No user attribute with id " + sIdToDelete);
		}
		if (attrHasAscendants(sIdToDelete,PM_NODE.UATTR.value)) {
			return failurePacket("User attribute has ascendants");
		}
		if (attrHasOpsets(sIdToDelete,PM_NODE.UATTR.value)) {
			return failurePacket("User attribute is assigned to operation sets");
		}

		// Get user attributes this user attribute is assigned to and delete the
		// assignment.
		Packet res;
		Attribute attr;
		String sId;

		try {
			attr = getToAttrs(sIdToDelete,PM_NODE.UATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sId = (String) enumer.next();

					// Now delete the assignment sIdToDelete (uattr) ---> sId
					// (uattr).
					res =  deleteDoubleLink(sIdToDelete,PM_NODE.UATTR.value,
							sId,PM_NODE.UATTR.value);
					if (res.hasError()) {
						return res;
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket(e.getMessage());
		}

		// Get policy classes this user attribute is assigned to and delete the
		// assignment.
		try {
			attr = getToPolicies(sIdToDelete,PM_NODE.UATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sId = (String) enumer.next();

					// Now delete the assignment sIdToDelete (uattr) ---> sId
					// (policy class).
					res =  deleteDoubleLink(sIdToDelete,PM_NODE.UATTR.value,
							sId,PM_NODE.POL.value);
					if (res.hasError()) {
						return res;
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket(e.getMessage());
		}

		// If this attribute is assigned to the connector node, delete the back
		// link
		// in the connector (there is no need to fully delete the assignment
		// because
		// the attribute will be destroyed).
		if (attrIsAssignedToConnector(sIdToDelete,PM_NODE.UATTR.value)) {
			// Now delete the assignment sIdToDelete (uattr) ---> Connector
			// node.
			res =  deleteDoubleLink(sIdToDelete,PM_NODE.UATTR.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}

		// Destroy the user attribute object.
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sIdToDelete + ","
					+ sUserAttrContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete user attribute "
					+ sIdToDelete);
		}

		return ADPacketHandler.getSuccessPacket();
	}

	// Delete the assignment between two nodes of the graph at the user request.
	// The assignments that can be deleted are:
	// user ---> user attribute
	// user attribute ---> user attribute
	// user attribute ---> policy class
	// user attribute ---> operation set
	// object attribute ---> object attribute
	// object attribute ---> policy class
	// operation set ---> object attribute.
	// The assignments to the connector node cannot be deleted (or created)
	// explicitly by the administrator.
	// NOTE. The methods for deleting the assignment between specific types of
	// nodes,
	// like deleteAssgnUserUattr below do more than just delete the double link
	// between the nodes (for example, deleteAssgnUserUattr may also link the
	// user
	// to the connector node). If you just need to delete the double link, use
	// the deleteDoubleLink() method.

	public Packet deleteAssignment(String sSessId, String sProcId,
			String sId1, String sType1, String sId2, String sType2,
			String sIsAdminVos) {

		if (sIsAdminVos.equalsIgnoreCase("yes")) {
			sId1 = getAdminVosNodeOrigId(sId1);
			sId2 = getAdminVosNodeOrigId(sId2);
		}

		// Check permissions.
		// ...

		// Call the internal function.
		return deleteAssignmentInternal(sId1, sType1, sId2, sType2);
	}


	public Packet deleteAssignmentInternal(String sId1, String sType1,
			String sId2, String sType2) {
		if (sType1.equalsIgnoreCase(PM_NODE.USER.value)
				&& sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			return deleteAssgnUserUattr(sId1, sId2);
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)
				&& sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			return deleteAssgnUattrUattr(sId1, sId2);
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)
				&& sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
			return deleteAssgnUattrPolicy(sId1, sId2);
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)
				&& sType2.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			return deleteAssgnUattrOpset(sId1, sId2);
		} else if ((sType1.equalsIgnoreCase(PM_NODE.OATTR.value) || sType1.equalsIgnoreCase(PM_NODE.ASSOC.value))
				&& sType2.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			return deleteAssgnOattrOattr(sId1, sId2);
		} else if ((sType1.equalsIgnoreCase(PM_NODE.OATTR.value) || sType1.equalsIgnoreCase(PM_NODE.ASSOC.value))
				&& sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
			return deleteAssgnOattrPolicy(sId1, sId2);
		} else if (sType1.equalsIgnoreCase(PM_NODE.OPSET.value)
				&& (sType2.equalsIgnoreCase(PM_NODE.OATTR.value) || sType2.equalsIgnoreCase(PM_NODE.ASSOC.value))) {
			return deleteAssgnOpsetOattr(sId1, sId2);
		}
		return failurePacket("The assignment cannot be deleted or does not exist");
	}




	public Packet testDoubleLink() {
		String sCont2;
		String sAttr2;
		String sDn;
		ModificationItem[] mods = new ModificationItem[1];

		String sId2 = getEntityId("gavrila INBOX",PM_NODE.OATTR.value);
		sAttr2 = "pmFromAttr";
		sCont2 = sObjAttrContainerDN;

		for (int i = 0; i < 1000; i++) {
			String sId1 = "id" + (i + 1);

			System.out.println("Adding value " + sId1);

			sDn = "CN=" + sId2 + "," + sCont2;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute(sAttr2, sId1));
			try {
				ServerConfig.ctx.modifyAttributes(sDn, mods);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Couldn't add back link between nodes");
			}
		}
		return ADPacketHandler.getSuccessPacket();
	}



	public Packet deleteAssgnUserUattr(String sId1, String sId2) {
		// Conditions: sId1 is assigned to sId2.
		if (!userIsAssigned(sId1, sId2)) {
			return failurePacket("Marked user is not assigned to this attribute");
		}

		// Delete the double link between the user sId1 and the attribute sId2.
		Packet res =  deleteDoubleLink(sId1,PM_NODE.USER.value, sId2,
				PM_NODE.UATTR.value);
		if (res.hasError()) {
			return res;
		}

		// If the user is not assigned to any other node, assign it to the
		// connector.
		if (userHasNoDescendant(sId1)) {
			res =  addDoubleLink(sId1,PM_NODE.USER.value, GlobalConstants.PM_CONNECTOR_ID,
					PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("User assignment to attribute successfully deleted");
	}


	public Packet deleteAssgnUattrUattr(String sId1, String sId2) {
		// Conditions: sId1 is assigned to sId2.
		if (!attrIsAssignedToAttr(sId1, sId2,PM_NODE.UATTR.value)) {
			return failurePacket("The marked user attribute is not assigned to this attribute");
		}

		// Delete the double link between the user attributes sId1, sId2.
		Packet res =  deleteDoubleLink(sId1,PM_NODE.UATTR.value, sId2,
				PM_NODE.UATTR.value);
		if (res.hasError()) {
			return res;
		}

		// If the user attribute is not assigned to any other node, assign it to
		// the connector.
		if (!attrHasDescendants(sId1,PM_NODE.UATTR.value)) {
			res =  addDoubleLink(sId1,PM_NODE.UATTR.value, GlobalConstants.PM_CONNECTOR_ID,
					PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Attribute assignment successfully deleted");
	}


	public Packet deleteAssgnUattrPolicy(String sId1, String sId2) {
		// Conditions: sId1 is assigned to sId2.
		if (!attrIsAssignedToPolicy(sId1,PM_NODE.UATTR.value, sId2)) {
			return failurePacket("Marked user attribute is not assigned to this policy class");
		}

		// Delete the double link between the user attribute sId1 and policy
		// sId2.
		Packet res =  deleteDoubleLink(sId1,PM_NODE.UATTR.value, sId2,
				PM_NODE.POL.value);
		if (res.hasError()) {
			return res;
		}

		// If the user attribute is not assigned to any other node, assign it to
		// the connector.
		if (!attrHasDescendants(sId1,PM_NODE.UATTR.value)) {
			res =  addDoubleLink(sId1,PM_NODE.UATTR.value, GlobalConstants.PM_CONNECTOR_ID,
					PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Attribute assignment to policy successfully deleted");
	}


	public Packet deleteAssgnUattrOpset(String sId1, String sId2) {
		// Conditions: sId1 is assigned to sId2.
		if (!attrIsAssignedToOpset(sId1, sId2)) {
			return failurePacket("Marked user attribute is not assigned to this operation set");
		}

		// Delete the double link between the user attribute sId1 and opset
		// sId2.
		Packet res =  deleteDoubleLink(sId1,PM_NODE.UATTR.value, sId2,
				PM_NODE.OPSET.value);
		if (res.hasError()) {
			return res;
		}

		// If the opset is not assigned to anything else (to another user
		// attribute,
		// or object attribute or to the connector), assign it to the connector
		// node.
		if (opsetIsIsolated(sId2) && !opsetIsAssignedToConnector(sId2)) {
			res =  addDoubleLink(sId2,PM_NODE.OPSET.value, GlobalConstants.PM_CONNECTOR_ID,
					PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Assignment successfully deleted");
	}


	public Packet deleteAssgnOattrOattr(String sId1, String sId2) {
		// Conditions: sId1 is assigned to sId2.
		if (!attrIsAssignedToAttr(sId1, sId2,PM_NODE.OATTR.value)) {
			return failurePacket("Marked object attribute is not assigned to this attribute");
		}

		// Delete the double link between the object attributes sId1, sId2.
		Packet res =  deleteDoubleLink(sId1,PM_NODE.OATTR.value, sId2,
				PM_NODE.OATTR.value);
		if (res.hasError()) {
			return res;
		}

		// If the object attribute is not assigned to any other node, assign it
		// to the connector.
		if (!attrHasDescendants(sId1,PM_NODE.OATTR.value)) {
			res =  addDoubleLink(sId1,PM_NODE.OATTR.value, GlobalConstants.PM_CONNECTOR_ID,
					PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Attribute assignment successfully deleted");
	}


	public Packet deleteAssgnOattrPolicy(String sId1, String sId2) {
		// Conditions: sId1 is assigned to sId2.
		if (!attrIsAssignedToPolicy(sId1,PM_NODE.OATTR.value, sId2)) {
			return failurePacket("Marked object attribute is not assigned to this policy class");
		}

		// Delete the double link between the object attribute sId1 and policy
		// sId2.
		Packet res =  deleteDoubleLink(sId1,PM_NODE.OATTR.value, sId2,
				PM_NODE.POL.value);
		if (res.hasError()) {
			return res;
		}

		// If the object attribute is not assigned to any other node, assign it
		// to the connector.
		if (!attrHasDescendants(sId1,PM_NODE.OATTR.value)) {
			res =  addDoubleLink(sId1,PM_NODE.OATTR.value, GlobalConstants.PM_CONNECTOR_ID,
					PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Attribute assignment to policy successfully deleted");
	}


	public Packet deleteAssgnOpsetOattr(String sId1, String sId2) {
		// Conditions: sId1 is assigned to sId2.
		if (!opsetIsAssignedToAttr(sId1, sId2)) {
			return failurePacket("Marked operation set is not assigned to this object attribute");
		}

		// Delete the double link between the user attribute sId1 and opset
		// sId2.
		Packet res =  deleteDoubleLink(sId1,PM_NODE.OPSET.value, sId2,
				PM_NODE.OATTR.value);
		if (res.hasError()) {
			return res;
		}

		// If the opset is not assigned to anything else (to a user attribute,
		// or object attribute, or to the connector), assign it to the connector
		// node.
		if (opsetIsIsolated(sId1) && !opsetIsAssignedToConnector(sId1)) {
			res =  addDoubleLink(sId1,PM_NODE.OPSET.value, GlobalConstants.PM_CONNECTOR_ID,
					PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Assignment successfully deleted");
	}


	public Packet assign(String sSessId, String sProcId, String sId1,
			String sType1, String sId2, String sType2) { //, String sIsAdminVos) {

//		if (sIsAdminVos.equalsIgnoreCase("yes")) {
//			sId1 = getAdminVosNodeOrigId(sId1);
//			sId2 = getAdminVosNodeOrigId(sId2);
//		}

		// Check permissions.
		if (!requestAssignPerms(sSessId, sProcId, sId1, sType1, sId2, sType2)) {
			return failurePacket(reqPermsMsg);
		}

		return assignInternal(sId1, sType1, sId2, sType2);
	}


	public Packet assignInternal(String sId1, String sType1, String sId2,
			String sType2) {
		// The permissible types are:
		// user ---> user attribute
		// user attribute ---> user attribute
		// user attribute ---> policy class
		// user attribute ---> operation set
		// object attribute ---> object attribute (but not associated to object)
		// object attribute ---> policy class
		// operation set ---> object attribute.
		if (sType1.equalsIgnoreCase(PM_NODE.USER.value)
				&& sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			return assignUserToUattr(sId1, sId2);
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)
				&& sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			return assignUattrToUattr(sId1, sId2);
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)
				&& sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
			return assignUattrToPolicy(sId1, sId2);
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)
				&& sType2.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			return assignUattrToOpset(sId1, sId2);
		} else if ((sType1.equalsIgnoreCase(PM_NODE.OATTR.value) || sType1.equalsIgnoreCase(PM_NODE.ASSOC.value))
				&& sType2.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			return assignOattrToOattr(sId1, sId2);
		} else if ((sType1.equalsIgnoreCase(PM_NODE.OATTR.value) || sType1.equalsIgnoreCase(PM_NODE.ASSOC.value))
				&& sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
			return assignOattrToPolicy(sId1, sId2);
		} else if (sType1.equalsIgnoreCase(PM_NODE.OPSET.value)
				&& (sType2.equalsIgnoreCase(PM_NODE.OATTR.value) || sType2.equalsIgnoreCase(PM_NODE.ASSOC.value))) {
			return assignOpsetToOattr(sId1, sId2);
		} else {
			return failurePacket("Incompatible types for assignment");
		}
	}





	public Packet assignUattrToUattr(String sId1, String sId2) {
		// Conditions: sId1 not already an ascendant of sId2, sId2 not an
		// ascendant of sId1.
		if (attrIsAscendant(sId1, sId2,PM_NODE.UATTR.value)) {
			return failurePacket("Marked attribute already contains this attribute");
		}
		if (attrIsAscendant(sId2, sId1,PM_NODE.UATTR.value)) {
			return failurePacket("Assignment would create a cycle");
		}

		// Add the double link between the attribute sId1 and the attribute
		// sId2.
		Packet res =  addDoubleLink(sId1,PM_NODE.UATTR.value, sId2,
				PM_NODE.UATTR.value);
		if (res.hasError()) {
			return res;
		}

		// If sId1 is assigned to the connector node, delete that assignment.
		if (attrIsAssignedToConnector(sId1,PM_NODE.UATTR.value)) {
			res =  deleteDoubleLink(sId1,PM_NODE.UATTR.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}

		// If sId1--->policy class pc, and sId2--->*pc, delete sId1--->pc.
		try {
			Attribute attr = getToPolicies(sId1,PM_NODE.UATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					if (attrIsAscendantToPolicy(sId2,PM_NODE.UATTR.value, sId)) {
						res =  deleteDoubleLink(sId1,PM_NODE.UATTR.value,
								sId,PM_NODE.POL.value);
						if (res.hasError()) {
							return res;
						}
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Error when deleting assignment to a policy class");
		}

		// For all uattr sId such that sId1--->sId and sId != sId2 and
		// sId2--->*sId,
		// delete sId1--->sId.
		try {
			Attribute attr = getToAttrs(sId1,PM_NODE.UATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					if (!sId.equalsIgnoreCase(sId2)
							&& attrIsAscendant(sId2, sId,PM_NODE.UATTR.value)) {
						res =  deleteDoubleLink(sId1,PM_NODE.UATTR.value,
								sId,PM_NODE.UATTR.value);
						if (res.hasError()) {
							return res;
						}
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Error when deleting assignment to an attribute");
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket();
	}



	public Packet assignUattrToPolicy(String sId1, String sId2) {
		// Conditions: sId1 not already an ascendant of sId2.
		if (attrIsAscendantToPolicy(sId1,PM_NODE.UATTR.value, sId2)) {
			return failurePacket("Marked attribute already pertains to this policy");
		}

		// Add the double link between the attribute sId1 and the policy sId2.
		Packet res =  addDoubleLink(sId1,PM_NODE.UATTR.value, sId2,
				PM_NODE.POL.value);
		if (res.hasError()) {
			return res;
		}

		// If sId1 is assigned to the connector node, delete that assignment.
		if (attrIsAssignedToConnector(sId1,PM_NODE.UATTR.value)) {
			res =  deleteDoubleLink(sId1,PM_NODE.UATTR.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Marked attribute successfully assigned to this policy");
	}


	public Packet assignUattrToOpset(String sId1, String sId2) {
		// Conditions: sId1 not assigned to sId2.
		if (attrIsAssignedToOpset(sId1, sId2)) {
			return failurePacket("Marked attribute already assigned to this operation set");
		}

		// Add the double link between the attribute sId1 and the operation set
		// sId2.
		Packet res =  addDoubleLink(sId1,PM_NODE.UATTR.value, sId2,
				PM_NODE.OPSET.value);
		if (res.hasError()) {
			return res;
		}

		// If the opset is assigned to the connector, delete this assignment.
		if (opsetIsAssignedToConnector(sId2)) {
			res =  deleteDoubleLink(sId2,PM_NODE.OPSET.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Marked attribute successfully assigned to this operation set");
	}



	public Packet assignOattrToOattr(String sId1, String sId2) {
		// Conditions: sId2 not associated to an object,
		// sId1 not already an ascendant of sId2,
		// sId2 not an ascendant of sId1.
		if (hasAssocObj(sId2)) {
			return failurePacket("The selected attribute is asociated to an object");
		}
		if (attrIsAscendant(sId1, sId2,PM_NODE.OATTR.value)) {
			return failurePacket("Already contained");
		}
		if (attrIsAscendant(sId2, sId1,PM_NODE.OATTR.value)) {
			return failurePacket("Assignment would create a cycle");
		}

		// Add the double link between the attribute sId1 and the attribute
		// sId2.
		Packet res =  addDoubleLink(sId1,PM_NODE.OATTR.value, sId2,
				PM_NODE.OATTR.value);
		if (res.hasError()) {
			return res;
		}

		// If sId1 is assigned to the connector node, delete that assignment.
		if (attrIsAssignedToConnector(sId1,PM_NODE.OATTR.value)) {
			res =  deleteDoubleLink(sId1,PM_NODE.OATTR.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}

		String sId;

		// If sId1--->policy class pc, and sId2--->*pc, delete sId1--->pc.
		try {
			Attribute attr = getToPolicies(sId1,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sId = (String) enumer.next();
					if (attrIsAscendantToPolicy(sId2,PM_NODE.OATTR.value, sId)) {
						res =  deleteDoubleLink(sId1,PM_NODE.OATTR.value,
								sId,PM_NODE.POL.value);
						if (res.hasError()) {
							return res;
						}
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Error when deleting assignment to policy class");
		}

		// For all oattr sId such that sId1--->sId and sId != sId2 and
		// sId2--->*sId,
		// delete sId1--->sId.
		try {
			Attribute attr = getToAttrs(sId1,PM_NODE.OATTR.value);
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					sId = (String) enumer.next();
					if (!sId.equalsIgnoreCase(sId2)
							&& attrIsAscendant(sId2, sId,PM_NODE.OATTR.value)) {
						res =  deleteDoubleLink(sId1,PM_NODE.OATTR.value,
								sId,PM_NODE.OATTR.value);
						if (res.hasError()) {
							return res;
						}
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Error when deleting assignment to policy class");
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket();
	}



	public Packet assignOattrToPolicy(String sId1, String sId2) {
		// Conditions: sId1 not already an ascendant of sId2.
		if (attrIsAscendantToPolicy(sId1,PM_NODE.OATTR.value, sId2)) {
			return failurePacket("Marked attribute already pertains to this policy");
		}

		// Add the double link between the object attribute sId1 and the policy
		// sId2.
		Packet res =  addDoubleLink(sId1,PM_NODE.OATTR.value, sId2,
				PM_NODE.POL.value);
		if (res.hasError()) {
			return res;
		}

		// If sId1 is assigned to the connector node, delete that assignment.
		if (attrIsAssignedToConnector(sId1,PM_NODE.OATTR.value)) {
			res =  deleteDoubleLink(sId1,PM_NODE.OATTR.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}
		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Marked attribute successfully assigned to this policy");
	}


	public Packet assignOpsetToOattr(String sId1, String sId2) {
		// Conditions: sId1 not assigned to sId2.
		if (opsetIsAssignedToAttr(sId1, sId2)) {
			return failurePacket("Marked operation set is already assigned to this object attribute");
		}

		// Add the double link between the operation set sId1 and the object
		// attribute sId2.
		Packet res =  addDoubleLink(sId1,PM_NODE.OPSET.value, sId2,
				PM_NODE.OATTR.value);
		if (res.hasError()) {
			return res;
		}

		// If the opset is assigned to the connector, delete this assignment.
		if (opsetIsAssignedToConnector(sId1)) {
			res =  deleteDoubleLink(sId1,PM_NODE.OPSET.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
		}

		setLastUpdateTimestamp();
		return ADPacketHandler.getSuccessPacket("Marked operation set successfully assigned to this object attribute");
	}

	// Get the startups for the user of the session passed as argument.
	// If the session does not have predefined startups, return null.
	// Also if we encounter an error we return null.

	public Attribute getPosStartups(String sSessId) {
		String sUserId = getSessionUserId(sSessId);
		Attributes attrs = null;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sUserId + ","
					+ sStartupContainerDN);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		Attribute attr = attrs.get("pmStartup");
		if (attr == null) {
			return null;
		}
		if (attr.size() == 0) {
			return null;
		}
		try {
			Attribute newAttr = new BasicAttribute("pmUnknown");
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sOrigId = (String) attrEnum.next();
				System.out.println("In getPosStartup found origid=" + sOrigId);
				String sPosId = getVosNodeId(sOrigId, sSessId);
				newAttr.add(sPosId);
			}
			return newAttr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// For a given VOS node, return its direct ascendants.
	// sId is the real (new) id of the VOS node.

	public Attribute getVosNodeAscs(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sVosNodeContainerDN);
			Attribute attr = attrs.get("pmAscNode");
			return attr;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// For a given VOS node, return its direct descendants.
	// sId is the real (new) id of the VOS node.

	public Attribute getVosNodeDescs(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sVosNodeContainerDN);
			Attribute attr = attrs.get("pmDescNode");
			return attr;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// For a given VOS node, return its name.
	// The parameter sId is the real (new) id of the VOS node.

	public String getVosNodeName(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sVosNodeContainerDN);
			return (String) attrs.get("pmOriginalName").get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// For a given VOS node, return its type.
	// sId is the new id of the VOS node.

	public String getVosNodeType(String sId) {
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + ","
					+ sVosNodeContainerDN);
			String sClass = (String) attrs.get("pmObjClass").get();
			if (sClass.equals(sConnectorClass)) {
				return PM_NODE.CONN.value;
			} else if (sClass.equals(sPolicyClass)) {
				return PM_NODE.POL.value;
			} else if (sClass.equals(sObjAttrClass)) {
				String sAssoc = (String) attrs.get("pmIsObj").get();
				if (sAssoc.equalsIgnoreCase("true")) {
					return PM_NODE.ASSOC.value;
				} else {
					return PM_NODE.OATTR.value;
				}
			}
			return null;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}

	// Given a VOS node's original id and the session id, returns the new id
	// of that VOS node.

	public String getVosNodeId(String sOrigId, String sSessId) {
		NamingEnumeration<?> nodes;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			nodes = ServerConfig.ctx.search(sVosNodeContainerDN, "(&(pmOriginalId="
					+ sOrigId + ")(pmSessId=" + sSessId + "))", constraints);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
		try {
			if (nodes == null || !nodes.hasMore()) {
				return null;
			}
			SearchResult sr = (SearchResult) nodes.next();
			return (String) sr.getAttributes().get("pmId").get();
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return null;
		}
	}


	// Add a node representing the members of a container. sNodeType and sNodeId
	// are the type (user attribute, object attribute, policy, or connector)
	// and id of the container.

	public void addMembersOfNode(Packet graph, String sNodeType, String sNodeId) {
		// Build the new node.
		String sLabel = "Members of " + getEntityName(sNodeId, sNodeType);
		RandomGUID myGUID = new RandomGUID();
		String sId = myGUID.toStringNoDashes();
		String sType =PM_NODE.M_PREFIX.value + sNodeType;
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(sType);
			sb.append(GlobalConstants.PM_FIELD_DELIM);
			sb.append(sId);
			sb.append(GlobalConstants.PM_FIELD_DELIM);
			sb.append(sLabel);
			graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());

			// Assign it to the container.
			sb = new StringBuffer();
			sb.append(GlobalConstants.PM_ARC);
			sb.append(GlobalConstants.PM_FIELD_DELIM);
			sb.append(sId);
			sb.append(GlobalConstants.PM_FIELD_DELIM);
			sb.append(sNodeId);
			graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Add a node to the text representation of the graph.
	// If the node type isPM_NODE.OATTR.value orPM_NODE.OATTRA.value, i.e., an object
	// attribute,
	// check whether it's associated to an object. If yes, change its type to
	//PM_NODE.ASSOC.value orPM_NODE.ASSOCA.value respectively.

	public void addNode(Packet graph, String sType, String sId)
			throws Exception {
		// First get the node label, which is the node name (AD attribute
		// "pmName").
		String sLabel = getEntityName(sId, sType);
		StringBuffer sb = new StringBuffer();
		sb.append(sType);
		sb.append(GlobalConstants.PM_FIELD_DELIM);
		sb.append(sId);
		sb.append(GlobalConstants.PM_FIELD_DELIM);
		sb.append(sLabel);
		graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());
	}


	public void addVosNode(Packet graph, String sType, String sId)
			throws Exception {
		String sLabel = getVosNodeName(sId);
		StringBuffer sb = new StringBuffer();
		sb.append(sType);
		sb.append(GlobalConstants.PM_FIELD_DELIM);
		sb.append(sId);
		sb.append(GlobalConstants.PM_FIELD_DELIM);
		sb.append(sLabel);
		graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());
	}


	public void addRel(Packet graph, String sId1, String sId2)
			throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append(GlobalConstants.PM_ARC);
		sb.append(GlobalConstants.PM_FIELD_DELIM);
		sb.append(sId1);
		sb.append(GlobalConstants.PM_FIELD_DELIM);
		sb.append(sId2);
		graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());
	}


	// The base node can be:
	// - null; then add the user as an ascendant of the connector node;
	// - a user attribute; add the user as an ascendant of the user attribute;
	// - the connector node; add the user as an ascendant of the connector node.
	// Parameter sBaseIsVos (values "yes" or "no"), tells whether the base node
	// id is a VOS node id.

	public Packet addUser(String sSessId, String sProcId, String sName,
			String sFull, String sInfo, String sPass, String sBaseId,
			String sBaseType, String sBaseIsVos) throws Exception {

		System.out.println("AddUser sName = " + sName);
		System.out.println("AddUser sFull = " + sFull);
		System.out.println("AddUser sInfo = " + sInfo);
		System.out.println("AddUser sPass = " + sPass);
		System.out.println("AddUser sBaseId = " + sBaseId);
		System.out.println("AddUser sBaseType = " + sBaseType);
		System.out.println("AddUser sBaseIsVos = " + sBaseIsVos);

		if (sBaseIsVos.equalsIgnoreCase("yes")) {
			sBaseId = getAdminVosNodeOrigId(sBaseId);
		} else if (sBaseId == null) {
			sBaseId = GlobalConstants.PM_CONNECTOR_ID;
			sBaseType =PM_NODE.CONN.value;
		}

		// Do permission checks.
		if (!requestAddUserPerms(sSessId, sProcId, sBaseId, sBaseType)) {
			return failurePacket(reqPermsMsg);
		}

		// Call addUserInternal() with a null sId to force generation of the
		// user id:
		Packet res =  addUserInternal(sName, null, sFull, sInfo, sPass,
				sBaseId, sBaseType);
		if (res.hasError()) {
			return res;
		}

		setLastUpdateTimestamp();

		// addUserInternal returns the new user's name and id in the first item.
		String sLine = res.getStringValue(0);
		String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);

		System.out.println("Before event");
		Packet eventRes =  processEvent(sSessId, null,
				GlobalConstants.PM_EVENT_USER_CREATE, sName, pieces[1], GlobalConstants.PM_CLASS_USER_NAME,
				null, null, null);
		System.out.println("After event");

		if (eventRes.hasError()) {
			return eventRes;
		}

		return res;
	}




	// Get all the attribute sets. Each item of the rsult contains name:id
	// of an attribute set.

	public Packet getAsets(String sClientId) {
		Packet res = new Packet();
		NamingEnumeration<?> asets;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			asets = ServerConfig.ctx.search(sAttrSetContainerDN, "(objectClass=*)",
					constraints);

			while (asets != null && asets.hasMore()) {
				SearchResult sr = (SearchResult) asets.next();
				String sName = (String) sr.getAttributes().get("pmName").get();
				String sId = (String) sr.getAttributes().get("pmId").get();
				res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
						+ sId);
			}
			return res;
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
	}



	// Each item of the return contains <uattr name>:<uattr id>.

	public Packet getUserAttributes(String sClientId) throws Exception {
		Packet res = new Packet();
		NamingEnumeration<?> uattrs;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			uattrs = ServerConfig.ctx.search(sUserAttrContainerDN, "(objectClass=*)",
					constraints);
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}

		while (uattrs != null && uattrs.hasMore()) {
			SearchResult sr = (SearchResult) uattrs.next();
			String sName = (String) sr.getAttributes().get("pmName").get();
			String sId = (String) sr.getAttributes().get("pmId").get();
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		}
		return res;
	}


	public Packet getUsersAndAttrs(String sSessId) {
		NamingEnumeration<?> users;

		try {
			// First the users...
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmName"});
			users = ServerConfig.ctx.search(sUserContainerDN, "(objectClass=*)", constraints);

			Packet res = new Packet();
			while (users != null && users.hasMore()) {
				SearchResult sr = (SearchResult) users.next();
				res.addItem(ItemType.RESPONSE_TEXT, (String) sr.getAttributes().get("pmName").get());
			}

			// ...now the attributes.
			// constraints = new SearchControls();
			// constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			// constraints.setReturningAttributes(new String[] {"pmName"});
			users = ServerConfig.ctx.search(sUserAttrContainerDN, "(objectClass=*)",
					constraints);

			while (users != null && users.hasMore()) {
				SearchResult sr = (SearchResult) users.next();
				res.addItem(ItemType.RESPONSE_TEXT, (String) sr.getAttributes().get("pmName").get());
			}
			return res;
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
	}

	// Each item is a string <user name>:<user id>

	public Packet getUsers(String sClientId) throws Exception {
		Packet res = new Packet();
		NamingEnumeration<?> users;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			users = ServerConfig.ctx.search(sUserContainerDN, "(objectClass=*)", constraints);
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}

		while (users != null && users.hasMore()) {
			SearchResult sr = (SearchResult) users.next();
			String sName = (String) sr.getAttributes().get("pmName").get();
			String sId = (String) sr.getAttributes().get("pmId").get();
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		}
		return res;
	}

	// Retuns the policy classes as a vector. Does not take any argument.

	public Vector<String> getPolicyClasses() {
		Vector<String> v = new Vector<String>();
		NamingEnumeration<?> policies;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			policies = ServerConfig.ctx.search(sPolicyContainerDN, "(objectClass=*)",
					constraints);
			if (policies == null) {
				return v;
			}
			while (policies.hasMore()) {
				SearchResult sr = (SearchResult) policies.next();
				v.add((String) sr.getAttributes().get("pmId").get());
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return v;
	}

	// Each item contains <policy name>:<policy id>

	public Packet getPolicyClasses(String sClientId) throws Exception {
		Packet result = new Packet();
		NamingEnumeration<?> policies;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			policies = ServerConfig.ctx.search(sPolicyContainerDN, "(objectClass=*)",
					constraints);
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception in getPolicyClasses: "
					+ e.getMessage());
		}

		if (policies != null) {
			while (policies.hasMore()) {
				SearchResult sr = (SearchResult) policies.next();
				String sName = (String) sr.getAttributes().get("pmName").get();
				String sId = (String) sr.getAttributes().get("pmId").get();
				result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
			}
		}
		return result;
	}

	// Add a policy class with the given name, description, info, and
	// properties.
	// First test permissions, then call the internal function.

	public Packet addPc(String sSessId, String sProcId, String sName,
			String sDescr, String sInfo, String[] sProps) {

		// Test permissions.
		if (!requestAddPcPerms(sSessId, sProcId)) {
			return failurePacket(reqPermsMsg);
		}
		return addPcInternal(sName, null, sDescr, sInfo, sProps);
	}



	public Packet deletePolicyClass(String sClientId, String sIdToDelete) {
		if (!entityExists(sIdToDelete,PM_NODE.POL.value)) {
			return failurePacket("No such policy id " + sIdToDelete);
		}
		if (!policyHasNoAscendant(sIdToDelete)) {
			return failurePacket("Policy class has ascendants");
		}

		Packet res;

		/*res =  deleteDoubleLink(sIdToDelete,PM_NODE.POL.value,
				GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
		if (res.hasError()) {
			return res;
		}*/

		// Destroy the policy class object.
		try {
			ServerConfig.ctx.destroySubcontext("CN=" + sIdToDelete + ","
					+ sPolicyContainerDN);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Could not delete policy class " + sIdToDelete);
		}
		deleteConnToPcLink(sIdToDelete);

		return ADPacketHandler.getSuccessPacket();
	}
	
	private boolean deleteConnToPcLink(String sId){
		String sDn = "CN=" + GlobalConstants.PM_CONNECTOR_ID + "," + sConnectorContainerDN;
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
				new BasicAttribute("pmFromPolicyClass", sId));
		try {
			System.out.println("Delete " + "pmFromPolicyClass" + "=" + sId + " in " + sDn);
			ServerConfig.ctx.modifyAttributes(sDn, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}


	public boolean policyHasNoAscendant(String sId) {
		Attributes attrs;
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sPolicyContainerDN);
		} catch (NameNotFoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return false;
		}
		Attribute attr = attrs.get("pmFromUserAttr");
		if (attr != null && attr.size() > 0) {
			return false;
		}
		attr = attrs.get("pmFromObjAttr");
		if (attr != null && attr.size() > 0) {
			return false;
		}
		return true;
	}

	// Called from a host's kernel to create a new object with specified name,
	// class, type, in the specified containers. Uses the process id to
	// check for the permission to create an object.
	//
	// The sContainers parameter contains a list of type|label separated by
	// commas.
	// The sPerms paramter contains the permissions requested by the process
	// (actually session) creating the object on the object (usually
	// "File write").
	//
	// This method calls addObject3(), which does a permission check (among
	// other things it calls requestPerms() to activate the user's attributes
	// that allow his process to create the object in the specified containers).
	//
	// After the object creation, the engine generates and processes an event
	// "Object create".

	public Packet createObject3(String sSessId, String sProcId,
			String sObjName, String sObjClass, String sObjType,
			String sContainers, String sPerms, String sSender,
			String sReceiver, String sSubject, String sAttached) throws Exception {
		if (!sObjClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_FILE_NAME)) {
			return failurePacket("Creation of non-File objects not yet implemented!");
		}

		// The host of the new object will be the session host.
		String sHost = getSessionHostName(sSessId);
		String sPhysCont = getHostRepositoryInternal(sHost);
		String sPath = null;
		if (sPhysCont.endsWith(File.separator)) {
			sPath = sPhysCont + sObjName + "." + sObjType;
		} else {
			sPath = sPhysCont + File.separator + sObjName + "." + sObjType;
		}
		Packet result = null;

		try {
			result =  addObject3(sSessId, sProcId, sObjName, sObjName,
					sObjName, sContainers, sObjClass, sObjType, sHost, sPath, null,
					null, "no", sSender, sReceiver, sSubject, sAttached, null,
					null, null);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception during object creation: "
					+ e.getMessage());
		}

		// If successful, the result contains the name and id of the new object.
		if (result.hasError()) {
			return result;
		}
		String sLine = result.getStringValue(0);
		String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);

		result =  processEvent(sSessId, null, GlobalConstants.PM_EVENT_OBJECT_CREATE,
				sObjName, pieces[1], sObjClass, sObjType, sContainers, sPerms);

		// If failure, delete the new object???
		// ...
		return result;
	}

	// Create a new object. Called from createObject3() or directly from the
	// Admin Tool.
	//
	// The engine creates an object attribute associated with the object.
	// This attribute will be assigned to the connector node, unless the
	// sContainers
	// is non-null. sContainers specifies the types and ids of object
	// attributes,
	// policies, or
	// connector node, which will be used
	// as immediate descendants of the object.

	public Packet addObject3(String sSessId, String sProcId, String sName,
			String sDescr, String sInfo, String sContainers, String sClass,
			String sType, String sHost, String sPath, String sOrigName,
			String sOrigId, String sInh, String sSender, String sReceiver,
			String sSubject, String sAttached, String sTplId,
			String sComponents, String[] sKeys) throws Exception {

		if (sContainers == null || sContainers.length() == 0) {
			sContainers =PM_NODE.CONN.value + GlobalConstants.PM_ALT_FIELD_DELIM + GlobalConstants.PM_CONNECTOR_NAME;
		}

		System.out.print("addObject3(");
		System.out.print("sess=" + sSessId);
		System.out.print(", proc=" + sProcId);
		System.out.print(", name=" + sName);
		System.out.print(", descr=" + sDescr);
		System.out.print(", info=" + sInfo);
		System.out.print(", class=" + sClass);
		System.out.print(", type=" + sType);
		System.out.print(", containers=" + sContainers);
		System.out.print(", host=" + sHost);
		System.out.print(", path=" + sPath);
		System.out.print(", origname=" + sOrigName);
		System.out.print(", origid=" + sOrigId);
		System.out.println(", inh=" + sInh);
		System.out.println(", sender=" + sSender);
		System.out.println(", receiver=" + sReceiver);
		System.out.println(", subject=" + sSubject);
		System.out.println(", attached=" + sAttached);
		System.out.println(", template=" + sTplId);
		System.out.println(", components=" + sComponents);
		if (sKeys != null) {
			for (int i = 0; i < sKeys.length; i++) {
				System.out.println(", key=" + sKeys[i]);
			}
		}
		System.out.println(")");

		String[] pieces = sContainers.split(GlobalConstants.PM_LIST_MEMBER_SEP);
		Packet res;
		String sAssocId = null;
		String sObjId = null;

		// For each container:
		for (int i = 0; i < pieces.length; i++) {
			String[] sTypeLabel = pieces[i].split(GlobalConstants.PM_ALT_DELIM_PATTERN);
			System.out.println("Container no. " + i + ": " + sTypeLabel[0]
					+ "|" + sTypeLabel[1]);

			// Get container's id,
			String sContId = getEntityId(sTypeLabel[1], sTypeLabel[0]);
			if (sContId == null) {
				return failurePacket("No container " + sTypeLabel[1]
						+ " of type " + sTypeLabel[0]);
			}

			// check permission to create a new object within this container,
			if (!requestAddObjectPerms(sSessId, sProcId, sContId, sTypeLabel[0])) {
				return failurePacket(reqPermsMsg);
			}
			// check permission to represent a PM entity by the new object.
			if (!requestRepresentEntityPerms(sSessId, sProcId, sClass,
					sOrigName, sOrigId)) {
				return failurePacket(reqPermsMsg);
			}

			// create or insert the new object within the container.
			if (i == 0) {
				// For the first container, create the object within. Let the
				// engine
				// generate the object and obj attr ids.
				res =  addObjectInternal(sName, null, null, sDescr,
						sInfo, sContId, sTypeLabel[0], sClass, sType, sHost,
						sPath, sOrigName, sOrigId, sInh.equals("yes"), sSender,
						sReceiver, sSubject, sAttached, sTplId, sComponents,
						sKeys);
				if (res.hasError()) {
					return res;
				}
				String sLine = res.getStringValue(0);
				String[] splinters = sLine.split(GlobalConstants.PM_FIELD_DELIM);
				sObjId = splinters[1];
				sAssocId = getAssocOattr(sObjId);
			} else {
				// For the other containers, assign the oattr associated with
				// the
				// new object to them, if not already there.
				//
				res =  assignInternal(sAssocId,PM_NODE.OATTR.value, sContId,
						sTypeLabel[0]);
				if (res.hasError()) {
					return res;
				}
			}
		}
		res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sObjId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet!");
		}
		return res;
	}







	public Packet getReps(String id, String type){
		Packet res = new Packet();
		HashSet<String> reps = getObjectsRepresentingEntity(id, type);
		Iterator hsiter = reps.iterator();
		while (hsiter.hasNext()) {
			String rep = (String)hsiter.next();
			try {
				res.addItem(ItemType.RESPONSE_TEXT, rep);
			} catch (PacketException e) {
				e.printStackTrace();
				return null;
			}
		}
		return res;
	}


	// Get all operation sets. Each item of the return contains
	// <operation set name>:<operation set id>.

	public Packet getOpsets(String sClientId) throws Exception {
		Packet res = new Packet();
		NamingEnumeration<?> osets;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			osets = ServerConfig.ctx.search(sOpsetContainerDN, "(objectClass=*)",
					constraints);
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}

		while (osets != null && osets.hasMore()) {
			SearchResult sr = (SearchResult) osets.next();
			String sName = (String) sr.getAttributes().get("pmName").get();
			String sId = (String) sr.getAttributes().get("pmId").get();
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		}
		return res;
	}

	// Add a new op set and/or a new operation.
	// If the op set does not exist, add a new one and the operation (if the op
	// is present in the command).
	// If the op set already exists, add a new operation (which must be
	// present in the command).
	// The last two arguments are the id and type of a graph node where the
	// new opset must be assigned (it can be the connector, a user attribute, or
	// an object attribute - including oattr associated to objects).
	// The base id and type can be null; then we will add the opset to the
	// connector node.
	// Note that the base node is used only when the opset is
	// created (and not when we add only an operation).

	public Packet addOpsetAndOp(String sSessId, String sOpset, String sDescr,
			String sInfo, String sOp, String sBaseId, String sBaseType) {
		System.out.println("addOpsetAndOp(name=" + sOpset + ", descr=" + sDescr
				+ ", info=" + sInfo + ", op=" + sOp + ", baseid=" + sBaseId
				+ ", basetype=" + sBaseType + ")");

		if (sBaseId == null) {
			sBaseId = GlobalConstants.PM_CONNECTOR_ID;
			sBaseType =PM_NODE.CONN.value;
		}

		if (sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sBaseType =PM_NODE.OATTR.value;
		}

		// Check permissions.
		// If the opset does not exist, check permission to create it.
		// Otherwise, check permission to add an operation to the opset.
		String sId = getEntityId(sOpset,PM_NODE.OPSET.value);
		if (sId == null) {
			if (!requestAddOpsetPerms(sSessId, null, sBaseId, sBaseType)) {
				return failurePacket(reqPermsMsg);
			}
		}

		// Test the base node type.
		if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)
				|| sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			return addOpsetAndOpInternal(sOpset, null, sDescr, sInfo, sOp,
					null, null, sBaseId, sBaseType);
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			return addOpsetAndOpInternal(sOpset, null, sDescr, sInfo, sOp,
					sBaseId, sBaseType, null, null);
		} else {
			return failurePacket("Cannot add/assign an operation set to this type of node");
		}
	}


	// Returns a Packet that contains the <name>:<id> of the entity
	// in its first item.

	public Packet getEntityWithProp(String sSessId, String sEntType,
			String sProp) {
		String sId = getEntityWithPropInternal(sEntType, sProp);
		if (sId == null) {
			return failurePacket("No such entity found!");
		}
		String sName = getEntityName(sId, sEntType);
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return res;
	}

	// Returns the id of the entity of the specified type with the specified
	// property, or null if no such entity exists.

	public String getEntityWithPropInternal(String sType, String sProp) {
		NamingEnumeration<?> attrs;
		String sContainer;
		String sClass;
		if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sContainer = sObjAttrContainerDN;
			sClass = sObjAttrClass;
		} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
			sClass = sUserAttrClass;
		} else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sContainer = sPolicyContainerDN;
			sClass = sPolicyClass;
		} else {
			return null;
		}

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId"});
			attrs = ServerConfig.ctx.search(sContainer, "(&(objectClass=" + sClass
					+ ")(pmProperty=" + sProp + "))", constraints);
			while (attrs != null && attrs.hasMore()) {
				SearchResult sr = (SearchResult) attrs.next();
				String sId = (String) sr.getAttributes().get("pmId").get();
				return sId;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// Return all object attributes proper (excluding those associated to
	// objects).

	public Packet getObjAttrsProper(String sClientId) throws Exception {
		Packet result = new Packet();
		NamingEnumeration<?> oattrs;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmName", "pmId",
			"pmAssocObj"});
			oattrs = ServerConfig.ctx.search(sObjAttrContainerDN, "(objectClass=*)",
					constraints);
			while (oattrs != null && oattrs.hasMore()) {
				SearchResult sr = (SearchResult) oattrs.next();
				Attribute assocattr = sr.getAttributes().get("pmAssocObj");
				if (assocattr != null) {
					continue;
				}
				String sName = (String) sr.getAttributes().get("pmName").get();
				String sId = (String) sr.getAttributes().get("pmId").get();
				result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
						+ sId);
			}
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
		return result;
	}

	// Return all object attributes (including those associated to objects).

	public Packet getOattrs(String sClientId) throws Exception {
		Packet result = new Packet();
		NamingEnumeration<?> oattrs;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmName", "pmId"});
			oattrs = ServerConfig.ctx.search(sObjAttrContainerDN, "(objectClass=*)",
					constraints);
			while (oattrs != null && oattrs.hasMore()) {
				SearchResult sr = (SearchResult) oattrs.next();
				String sName = (String) sr.getAttributes().get("pmName").get();
				String sId = (String) sr.getAttributes().get("pmId").get();
				result.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM
						+ sId);
			}
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
		return result;
	}


	public boolean isEvent(String sName) {
		for (int i = 0; i < GlobalConstants.sEventNames.length; i++) {
			if (GlobalConstants.sEventNames[i].equalsIgnoreCase(sName)) {
				return true;
			}
		}
		return false;
	}


	public Packet getAllOps(String sClientId) throws Exception {
		Packet result = new Packet();
		NamingEnumeration<?> cls;
		NamingEnumeration<?> ops;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmOp"});
			cls = ServerConfig.ctx.search(sObjClassContainerDN, "(objectClass=*)",
					constraints);
			while (cls != null && cls.hasMore()) {
				SearchResult sr = (SearchResult) cls.next();
				Attribute attr = sr.getAttributes().get("pmOp");
				if (attr != null) {
					for (ops = attr.getAll(); ops.hasMore(); ) {
						result.addItem(ItemType.RESPONSE_TEXT,
								(String) ops.next());
					}
				}
			}
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}
		return result;
	}

	// Get all object classes. Each item of the return value contains
	// an object class name.

	public Packet getObjClasses(String sClientId) throws Exception {
		Packet res = new Packet();
		NamingEnumeration<?> cls;

		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
			constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
			cls = ServerConfig.ctx.search(sObjClassContainerDN, "(objectClass=*)",
					constraints);
		} catch (CommunicationException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("AD connection error");
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Exception: " + e.getMessage());
		}

		while (cls != null && cls.hasMore()) {
			SearchResult sr = (SearchResult) cls.next();
			String sName = (String) sr.getAttributes().get("pmName").get();
			//String sId = (String) sr.getAttributes().get("pmId").get();
			// result.add(GlobalConstants.PM_DATA + sName + GlobalConstants.PM_FIELD_DELIM + sId);
			res.addItem(ItemType.RESPONSE_TEXT, sName);
		}
		return res;
	}


	public Packet deleteObjClassAndOp(String sClientId, String sClass,
			String sOp) {
		// The class name cannot be null or empty.
		// The class cannot be a predefined class.
		// The class must exist.
		// If the operation is present, delete the operation from the class.
		// Otherwise delete the class.
		if (sClass == null || sClass.length() == 0) {
			return failurePacket("The class name cannot be null or empty");
		}

		if (sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_CLASS_NAME)
				|| sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_FILE_NAME)
				|| sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_DIR_NAME)) {
			return failurePacket("Cannot delete or modify a predefined class");
		}

		String sId = getEntityId(sClass, GlobalConstants.PM_OBJ_CLASS);
		if (sId == null) {
			return failurePacket("Unknown class " + sClass);
		}

		if (sOp == null || sOp.length() == 0) {
			// No operation was selected, delete the class.
			try {
				ServerConfig.ctx.destroySubcontext("CN=" + sId + "," + sObjClassContainerDN);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Error while deleting class " + sClass
						+ ": " + e.getMessage());
			}
		} else {
			// Delete the operation.
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute("pmOp", sOp));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + sId + "," + sObjClassContainerDN,
						mods);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Error while deleting operation " + sOp
						+ ": " + e.getMessage());
			}
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// The op set id cannot be null or empty.
	// The op set must exist.
	// If the operation is present, delete the operation from the opset.
	// Otherwise delete the opset.

	public Packet deleteOpsetAndOp(String sClientId, String sId, String sOp) {
		if (sId == null || sId.length() == 0) {
			return failurePacket("The operation set id cannot be null or empty");
		}

		String sName = getEntityName(sId,PM_NODE.OPSET.value);
		if (sName == null) {
			return failurePacket("Unknown operation set " + sId);
		}
		if (sOp == null || sOp.length() == 0) {
			// No operation was selected, try to delete the operation set.
			if (!opsetIsIsolated(sId)) {
				return failurePacket("Operation set has assignments");
			}
			// The opset is only assigned to the connector node, and can be
			// deleted.
			Packet res =  deleteDoubleLink(sId,PM_NODE.OPSET.value,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			if (res.hasError()) {
				return res;
			}
			try {
				ServerConfig.ctx.destroySubcontext("CN=" + sId + "," + sOpsetContainerDN);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Error while deleting op set " + sName
						+ ": " + e.getMessage());
			}
            //deleteADGraphNode(sId);
		} else {

			// An operation of the opset is also selected. Delete the operation
			// only.
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
					new BasicAttribute("pmOp", sOp));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + sId + "," + sOpsetContainerDN,
						mods);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Error while deleting operation " + sOp
						+ ": " + e.getMessage());
			}

            //updateADGraphNode(sId, sName, PM_NODE.OPSET.value);
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Add a new object class and/or a new operation.
	// If the object class does not exist, add a new one and the operation,
	// if present in the command.
	// If the object class already exists, add a new operation (which must be
	// present in the command.

	public Packet addObjClassAndOp(String sClientId, String sClass,
			String sDescr, String sInfo, String sOp) {
		// Test if the class exists.
		String sClassId = getEntityId(sClass, GlobalConstants.PM_OBJ_CLASS);
		System.out.println("Class id is " + sClassId);
		if (sClassId == null) {
			// Class does not exist. Add the class and the optional operation.
			Attributes attrs = new BasicAttributes(true);
			RandomGUID myGUID = new RandomGUID();
			sClassId = myGUID.toStringNoDashes();
			attrs.put("objectClass", sObjClassClass);
			attrs.put("pmId", sClassId);
			attrs.put("pmName", sClass);
			attrs.put("pmDescription", sDescr);
			attrs.put("pmOtherInfo", sInfo);

			String sClassDn = "CN=" + sClassId + "," + sObjClassContainerDN;

			try {
				ServerConfig.ctx.bind(sClassDn, null, attrs);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Unable to create class \"" + sClass
						+ "\"");
			}

			if (sOp == null) {
				System.out.println("the op is null");
			} else {
				System.out.println("the op is NOT null and has length "
						+ sOp.length());
			}

			if (sOp != null && sOp.length() > 0) {
				ModificationItem[] mods = new ModificationItem[1];
				mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmOp", sOp));
				try {
					ServerConfig.ctx.modifyAttributes(sClassDn, mods);
				} catch (Exception e) {
					e.printStackTrace();
					return failurePacket("Unable to add operation \"" + sOp
							+ "\": " + e.getMessage());
				}
			}
		} else {
			// Class exist, try to add the operation, which cannot be null or
			// duplicate.
			if (sOp == null || sOp.length() == 0) {
				return failurePacket("The operation name cannot be null or empty for an existing class");
			}

			if (objClassHasOp(sClassId, sOp)) {
				return failurePacket("Duplicate operation in the same object class");
			}

			// Add the operation
			ModificationItem[] mods = new ModificationItem[1];
			//String sClassDn = "CN=" + sClassId + "," + sObjClassContainerDN;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmOp", sOp));
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + sClassId + ","
						+ sObjClassContainerDN, mods);
			} catch (Exception e) {
				e.printStackTrace();
				return failurePacket("Unable to add operation \"" + sOp
						+ "\"; " + e.getMessage());
			}
		}
		return ADPacketHandler.getSuccessPacket();
	}

	// Add an object attribute.
	// The base node can be the connector node, an object attribute, or a
	// policy.
	// Return: failure or <name>:<id> of the new object attribute.

	public Packet addOattr(String sSessId, String sProcId, String sName,//TODO
			String sDescr, String sInfo, String sBaseId, String sBaseType,
			String sBaseIsVos, String sAssocObjId, String[] sProps) {
		if (sBaseIsVos.equalsIgnoreCase("yes")) {
			sBaseId = getAdminVosNodeOrigId(sBaseId);
		}

		if (!sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)
				&& !sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)
				&& !sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			return failurePacket("You cannot create an object attribute in this type of node");
		}

		// Check permissions.
		System.out.println("addOattr()");
		System.out.println("sBaseId: " + sBaseId);
		System.out.println("sBaseType: " + sBaseType);
		if (!requestAddOattrPerms(sSessId, sProcId, sBaseId, sBaseType)) {
			return failurePacket(reqPermsMsg);
		}

		Packet res =  addOattrInternal(sName, null, sDescr, sInfo,
				sBaseId, sBaseType, sAssocObjId, sProps);
		if (!res.hasError()) {
			setLastUpdateTimestamp();
		}
		return res;
	}

	   // Build an object for the clipboard of the session's host.
    // If the clipboard object exists, first delete it.
    //
    // The object attribute passed as argument actually is associated to the
    // object from which we copy a part to the clipboard. It can be null, which
    // means we copy from something that's not (yet) an object.
    //
    // The idea is to create a clipboard object for the session's host, which
    // has exactly the same attributes as the source object (with some
    // exceptions, like when the source object is labeled low (MLS) and the
    // user's level is high; in this case, the clipboard object should be high,
    // because it is created by a high session).
    //
    // If the object attribute passed as argument is null, just create the
    // clipboard object with the connector node as base.
    // If the object attribute passed as argument is not null, copy its
    // assignments to the new clipboard object (include containers, policies,
    // and opsets).
    // Finally, assign the clipboard to the MLS container corresponding to
    // the session's level. This also means that any possible assignment of the
    // clipboard object to another level (assignment that could have been
    // inherited from the object attribute passed as argument) must be deleted.
    // The clipboard object has the name "<host> clipboard".
    // Note. The final step
    // (assignment to MLS container) could be made in an event/response rule
    // for a "create clipboard" event.
    public Packet buildClipboard(String sSessId, String sOattrName) {
        System.out.println("Build clipboard for oattr " + sOattrName);

        // Get the session's host and the clipboard object name.
        String sHost = getSessionHostName(sSessId);
        if (sHost == null) {
            return failurePacket("Couldn't get the session's host name!");
        }
        String sClipName = sHost + " clipboard";

        // If the clipboard object already exists, delete it.
        String sClipObjId = getEntityId(sClipName, GlobalConstants.PM_OBJ);
        if (sClipObjId != null) {
        	try {
        		deleteObjectStrong(sSessId, sClipObjId);
        	} catch (Exception e) {
        		 return failurePacket("Couldn't delete all existing clipboard objects!");
        	}
        }

        // Build the clipboard object on the connector.
        Packet res =  addObjectInternal(sClipName, null, null,
                "Clipboard object for host " + sHost, "None.", GlobalConstants.PM_CONNECTOR_ID,
                PM_NODE.CONN.value, GlobalConstants.PM_CLASS_CLIPBOARD_NAME, null, sHost, null, null,
                null, false, null, null, null, null, null, null, null);
        if (res.hasError()) {
            return res;
        }

        // Get the id of the associated object attribute.
        String sClipId = getEntityId(sClipName, PM_NODE.OATTR.value);

        // If the source object is specified (its associated oattr - the second
        // argument of this method), we need to copy its assignments to the
        // clipboard object.
        if (sOattrName != null) {
            String sOattrId = getEntityId(sOattrName, PM_NODE.OATTR.value);
            if (sOattrId == null) {
                return failurePacket("No source object (attribute) for the clipboard!");
            }
            String sObjId = getAssocObj(sOattrId);
            if (sObjId == null) {
                return failurePacket("No source object for the clipboard!");
            }

            // Delete the link clipboard <-> connector.
            res =  deleteDoubleLink(sClipId, PM_NODE.OATTR.value,
                    GlobalConstants.PM_CONNECTOR_ID, PM_NODE.CONN.value);
            if (res.hasError()) {
                return res;
            }

            // Now copy the assignments of sOattrId to the clipboard object.
            copyOattrAssignments(sOattrId, sClipId);
        }

        // Now label the clipboard with the session's level, if the clipboard
        // is in MLS.
        labelClipboard(sSessId, sClipId);

        return ADPacketHandler.getSuccessPacket();
    }


    // Labeling the clipboard. In the case of an mls policy, the session
    // could have more than one active attributes in that policy. We must
    // select the "highest" attribute among the actives, and we define
    // "higher" as "being contained in": a > b iff a ->+ b.
    private Packet labelClipboard(String sSessId, String sClipId) {
        System.out.println("Labeling the Clipboard object.");

        // Find whether there is a MLS policy.
        String sMlsPolId = getEntityWithPropInternal(PM_NODE.POL.value, "type=mls");
        System.out.println("Found policy mls " + sMlsPolId);

        // Find the MLS user attributes that are active in the current session.
        HashSet<String> hs = getPolicyActiveAttributes(sSessId, sMlsPolId);
        // If no such attributes, there is nothing to do.
        if (hs.isEmpty()) {
        	return ADPacketHandler.getSuccessPacket();
        }

        // Find the highest active attribute in this policy.
        Iterator<String> hsiter = hs.iterator();
        String sHighestId = null;
        String sCrtId;
        while (hsiter.hasNext()) {
            sCrtId = hsiter.next();
            // If this is the first in loop, remember it and continue.
            if (sHighestId == null) {
                sHighestId = sCrtId;
                continue;
            }
            // There is a highest until now, compare crt to highest.
            if (attrIsAscendant(sCrtId, sHighestId, PM_NODE.UATTR.value)) {
                sHighestId = sCrtId;
            }
        }
        System.out.println("We found the highest active attribute to be "
                + getEntityName(sHighestId, PM_NODE.UATTR.value));

        // Get the corresponding object attribute, if one exists.
        String sPrefix = "correspondsto=";
        String sProp = getPropertyWithPrefix(sPrefix, sHighestId, PM_NODE.UATTR.value);
        if (sProp == null) {
        	return ADPacketHandler.getSuccessPacket();
        }
        String sCorrOattr = sProp.substring(sProp.indexOf(sPrefix)
                + sPrefix.length());
        String sCorrOattrId = getEntityId(sCorrOattr, PM_NODE.OATTR.value);
        if (sCorrOattrId == null) {
            return failurePacket("No object attribute " + sCorrOattr);
        }
        System.out.println("We found the corresponding oattr to be "
                + sCorrOattr);

        // Need to delete all other clipboard assignments to object attributes
        // of that policy.
        hs = getPolicyAttributesAssigned(sMlsPolId, sClipId);
        if (!hs.isEmpty()) {
            hsiter = hs.iterator();
            while (hsiter.hasNext()) {
                sCrtId = hsiter.next();
                // Delete sClipId ---> sCrtId.
                System.out.println("Trying to delete clipboard assignment to "
                        + getEntityName(sCrtId, PM_NODE.OATTR.value));
                Packet res =  deleteDoubleLink(sClipId, PM_NODE.OATTR.value,
                        sCrtId, PM_NODE.OATTR.value);
                if (res.hasError()) {
                    return res;
                }
            }
        }

        // Add sClipId ---> sCorrOattrId.
        System.out.println("Trying to add clipboard assignment to "
                + sCorrOattr);
        Packet res =  addDoubleLink(sClipId, PM_NODE.OATTR.value,
                sCorrOattrId, PM_NODE.OATTR.value);
        if (res.hasError()) {
            return res;
        }
        return ADPacketHandler.getSuccessPacket();
    }

    // Get the ids of the user attributes that are active in the specified
    // user session and specified policy class.
    private HashSet<String> getPolicyActiveAttributes(String sSessId,
                                                      String sPolId) {
        System.out.println("sess = " + sSessId + ", pol = " + sPolId);
        Attributes attrs;
        Attribute attr;
        String sId;// sName;
        HashSet<String> hs = new HashSet<String>();

        try {
            attrs = ServerConfig.ctx.getAttributes("CN=" + sSessId + ","
                    + sSessionContainerDN);
            attr = attrs.get("pmAttr");
            if (attr != null) {
                for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
                    sId = (String) enumer.next();
                    if (attrIsAscendantToPolicy(sId, PM_NODE.UATTR.value, sPolId)) {
                        hs.add(sId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hs;
    }
	/**
	 * @uml.property  name="reqPermsMsg"
	 */
	String reqPermsMsg;

	boolean requestAddPcPerms(String sSessId, String sProcId) {
		HashSet<String> resPerms;
		String sReqPerms;

		if (!entityExists(sSessId, GlobalConstants.PM_SESSION)) {
			reqPermsMsg = "You're not in a Policy Machine session!";
			return false;
		}

		sReqPerms = GlobalConstants.PM_CONN_CREATE_POL;
		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId,
				GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
		printSet(resPerms, GlobalConstants.PM_PERM, "Permissions on connector");
		if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
			reqPermsMsg = "You're not authorized to create a policy class (assigned to connector)!";
			return false;
		}
		return true;
	}

	boolean requestAssignPerms(String sSessId, String sProcId, String sId1,
			String sType1, String sId2, String sType2) {
		HashSet<String> resPerms;
		String sReqPerms1;
		String sReqPerms2;

		if (!entityExists(sSessId, GlobalConstants.PM_SESSION)) {
			reqPermsMsg = "You're not in a Policy Machine session!";
			return false;
		}

		if (sType1.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType1.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sReqPerms1 = GlobalConstants.PM_OATTR_ASSIGN;
			if (sType2.equalsIgnoreCase(PM_NODE.OATTR.value)) {
				sReqPerms2 = GlobalConstants.PM_OATTR_ASSIGN_TO;
			} else if (sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
				sReqPerms2 = GlobalConstants.PM_POL_ASSIGN_TO;
			} else {
				reqPermsMsg = "Incorrect types for assignment: " + sType1
						+ "--->" + sType2;
				return false;
			}
		} else if (sType1.equalsIgnoreCase(PM_NODE.USER.value)) {
			sReqPerms1 = GlobalConstants.PM_USER_ASSIGN;
			if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				sReqPerms2 = GlobalConstants.PM_UATTR_ASSIGN_TO;
			} else {
				reqPermsMsg = "Incorrect types for assignment: " + sType1
						+ "--->" + sType2;
				return false;
			}
		} else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sReqPerms1 = GlobalConstants.PM_UATTR_ASSIGN;
			if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				sReqPerms2 = GlobalConstants.PM_UATTR_ASSIGN_TO;
			} else if (sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
				sReqPerms2 = GlobalConstants.PM_POL_ASSIGN_TO;
			} else if (sType2.equalsIgnoreCase(PM_NODE.OPSET.value)) {
				sReqPerms1 = GlobalConstants.PM_UATTR_ASSIGN_TO_OPSET;
				sReqPerms2 = GlobalConstants.PM_OPSET_ASSIGN_TO;
			} else {
				reqPermsMsg = "Incorrect types for assignment: " + sType1
						+ "--->" + sType2;
				return false;
			}
		} else if (sType1.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sReqPerms1 = GlobalConstants.PM_OPSET_ASSIGN;
			if (sType2.equalsIgnoreCase(PM_NODE.OATTR.value)
					|| sType2.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
				sReqPerms2 = GlobalConstants.PM_OPSET_ASSIGN_TO;
			} else {
				reqPermsMsg = "Incorrect types for assignment: " + sType1
						+ "--->" + sType2;
				return false;
			}
		} else {
			reqPermsMsg = "Incorrect types for assignment: " + sType1 + "--->"
					+ sType2;
			return false;
		}

		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sId1,
				sType1);
		System.out.println("resPerms: " + resPerms);
		printSet(resPerms, GlobalConstants.PM_PERM, "Permissions on entity of type " + sType1);
		if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms1)) {
			reqPermsMsg = "You're not authorized to assign entity "
					+ getEntityName(sId1, sType1);
			return false;
		}
		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sId2,
				sType2);
		printSet(resPerms, GlobalConstants.PM_PERM, "Permissions on entity of type " + sType2);
		if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms2)) {
			reqPermsMsg = "You're not authorized to assign to entity "
					+ getEntityName(sId2, sType2);
			return false;
		}
		return true;
	}

	boolean requestAddOpsetPerms(String sSessId, String sProcId,
			String sBaseId, String sBaseType) {
		HashSet<String> resPerms;
		String sReqPerms;

		if (!entityExists(sSessId, GlobalConstants.PM_SESSION)) {
			reqPermsMsg = "You're not in a Policy Machine session!";
			return false;
		}

		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
				sBaseType);
		printSet(resPerms, GlobalConstants.PM_PERM,
				"Permissions on entity " + getEntityName(sBaseId, sBaseType));

		if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sReqPerms = GlobalConstants.PM_OATTR_CREATE_OPSET;
			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create an operation set assigned to "
						+ getEntityName(sBaseId, sBaseType);
				return false;
			}
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sReqPerms = GlobalConstants.PM_UATTR_CREATE_OPSET;
			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create an operation set assigned to "
						+ getEntityName(sBaseId, sBaseType);
				return false;
			}
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sReqPerms = GlobalConstants.PM_CONN_CREATE_OPSET;
			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create an operation set assigned to "
						+ getEntityName(sBaseId, sBaseType);
				return false;
			}
		} else {
			reqPermsMsg = "Invalid type (" + sBaseType + ") of the base node "
					+ getEntityName(sBaseId, sBaseType);
			return false;
		}
		return true;
	}

	boolean requestRepresentEntityPerms(String sSessId, String sProcId,
			String sClass, String sName, String sId) {
		HashSet<String> resPerms;
		String sReqPerms;

		if (!entityExists(sSessId, GlobalConstants.PM_SESSION)) {
			reqPermsMsg = "You're not in a Policy Machine session!";
			return false;
		}

		if (!sClass.equals(GlobalConstants.PM_CLASS_USER_NAME)
				&& !sClass.equals(GlobalConstants.PM_CLASS_UATTR_NAME)
				&& !sClass.equals(GlobalConstants.PM_CLASS_OBJ_NAME)
				&& !sClass.equals(GlobalConstants.PM_CLASS_OATTR_NAME)
				&& !sClass.equals(GlobalConstants.PM_CLASS_POL_NAME)
				&& !sClass.equals(GlobalConstants.PM_CLASS_CONN_NAME)
				&& !sClass.equals(GlobalConstants.PM_CLASS_OPSET_NAME)) {
			return true;
		}

		sReqPerms = GlobalConstants.PM_ENTITY_REPRESENT;
		String sType = classToType(sClass);
		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sId, sType);
		printSet(resPerms, GlobalConstants.PM_PERM,
				"Permissions on " + getEntityName(sId, sType));
		if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
			reqPermsMsg = "You're not authorized to represent entity "
					+ getEntityName(sId, sType) + " of type " + sType;
			return false;
		}
		return true;
	}

	boolean requestAddObjectPerms(String sSessId, String sProcId,
			String sBaseId, String sBaseType) {
		HashSet<String> resPerms;
		String sReqPerms;

		if (!entityExists(sSessId, GlobalConstants.PM_SESSION)) {
			reqPermsMsg = "You're not in a Policy Machine session!";
			return false;
		}

		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
				sBaseType);
		printSet(resPerms, GlobalConstants.PM_PERM,
				"Obtained Permissions on " + getEntityName(sBaseId, sBaseType));

		if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sReqPerms = GlobalConstants.PM_OATTR_CREATE_OBJ;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create an object in "
						+ getEntityName(sBaseId, sBaseType);
				return false;
			}
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sReqPerms = GlobalConstants.PM_POL_CREATE_OBJ;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create an object in "
						+ getEntityName(sBaseId, sBaseType);
				return false;
			}
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sReqPerms = GlobalConstants.PM_CONN_CREATE_OBJ;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create an object in "
						+ getEntityName(sBaseId, sBaseType);
				return false;
			}
		}
		return true;
	}

	boolean requestAddUserPerms(String sSessId, String sProcId, String sBaseId,
			String sBaseType) {
		HashSet<String> resPerms;
		String sReqPerms;

		if (!entityExists(sSessId, GlobalConstants.PM_SESSION)) {
			reqPermsMsg = "You're not in a Policy Machine session!";
			return false;
		}

		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
				sBaseType);
		printSet(resPerms, GlobalConstants.PM_PERM,
				"Permissions on " + getEntityName(sBaseId, sBaseType));

		if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sReqPerms = GlobalConstants.PM_UATTR_CREATE_USER;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create a user in this user attribute!";
				return false;
			}
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sReqPerms = GlobalConstants.PM_CONN_CREATE_USER;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create a user in the connector!";
				return false;
			}
		}
		return true;
	}

	boolean requestAddUattrPerms(String sSessId, String sProcId,
			String sBaseId, String sBaseType) {
		HashSet<String> resPerms;
		String sReqPerms;

		// if (sClientId.equals("super") || sClientId.equals("serban")) return
		// true;

		if (!entityExists(sSessId, GlobalConstants.PM_SESSION)) {
			reqPermsMsg = "You're not in a Policy Machine session!";
			return false;
		}

		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
				sBaseType);
		printSet(resPerms, GlobalConstants.PM_PERM,
				"Permissions on entity " + getEntityName(sBaseId, sBaseType));

		if (sBaseType.equalsIgnoreCase(PM_NODE.USER.value)) {
			sReqPerms = GlobalConstants.PM_USER_CREATE_UATTR;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create a user attribute for this user!";
				return false;
			}
			sReqPerms = GlobalConstants.PM_CONN_CREATE_UATTR;
			resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId,
					GlobalConstants.PM_CONNECTOR_ID,PM_NODE.CONN.value);
			printSet(resPerms, GlobalConstants.PM_PERM, "Permissions on connector");

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create a user attribute assigned to connector!";
				return false;
			}
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sReqPerms = GlobalConstants.PM_UATTR_CREATE_UATTR;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create a user attribute contained in this user attribute!";
				return false;
			}
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sReqPerms = GlobalConstants.PM_POL_CREATE_UATTR;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create a user attribute in this policy class!";
				return false;
			}
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sReqPerms = GlobalConstants.PM_CONN_CREATE_UATTR;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create a user attribute on the connector!";
				return false;
			}
		}
		return true;
	}


	public Packet getPermittedOps1(String sSessId, String sProcId, 
			String sBaseId, String sBaseType){
		Packet res = new Packet();
		HashSet<String> resPerms;

		if (!entityExists(sSessId, GlobalConstants.PM_SESSION)) {
			reqPermsMsg = "You're not in a Policy Machine session!";
			System.out.println(reqPermsMsg);
			return null;
		}

		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
				sBaseType);
		try{
			Iterator<String> hsiter = resPerms.iterator();
			while(hsiter.hasNext()){
				res.addItem(ItemType.RESPONSE_TEXT, (String)hsiter.next());
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}

		return res;
	}

	public static boolean modifyingTable;

	public boolean getTableModifying(){
		return modifyingTable;
	}
	/**
	 * Emulates work with table
	 * @author JOSH
	 * */

	public void setTableModifying(String b){
		System.out.println(b);
		if(b.equalsIgnoreCase("true")){
			modifyingTable = true;
		}else{
			modifyingTable = false;
		}
		System.out.println(modifyingTable);
	}

	boolean requestAddOattrPerms(String sSessId, String sProcId,//TODO
			String sBaseId, String sBaseType) {
		HashSet<String> resPerms;
		String sReqPerms;

		System.out.println("requestAddOattrPerms()");
		System.out.println("sBaseId: " + sBaseId);
		System.out.println("sBaseType: " + sBaseType);

		// if (sClientId.equals("super") || sClientId.equals("serban")) return
		// true;

		if (!entityExists(sSessId, GlobalConstants.PM_SESSION)) {
			reqPermsMsg = "You're not in a Policy Machine session!";
			System.out.println(reqPermsMsg);
			return false;
		}

		resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
				sBaseType);
		System.out.println("resPerms: " + resPerms);
		printSet(resPerms, GlobalConstants.PM_PERM,
				"Permissions on entity " + getEntityName(sBaseId, sBaseType));

		if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sReqPerms = GlobalConstants.PM_OATTR_CREATE_OATTR;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create an object attribute in this object attribute!";
				System.out.println(reqPermsMsg);
				return false;
			}

		} else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sReqPerms = GlobalConstants.PM_POL_CREATE_OATTR;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create an object attribute in this policy class!";
				System.out.println(reqPermsMsg);
				return false;
			}
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sReqPerms = GlobalConstants.PM_CONN_CREATE_OATTR;

			if (!resPerms.contains(GlobalConstants.PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
				reqPermsMsg = "You're not authorized to create an object attribute on the connector!";
				System.out.println(reqPermsMsg);
				return false;
			}
		}
		return true;
	}

	// If the clicked - also called base - node is:
	// - the connector node, the new attribute will be added as an ascendant
	// of the connector;
	// - a user, the new attribute will be added as a descendant of it and
	// ascendant of the connector node;
	// - a user attribute, the new attribute will be added as an ascendant of
	// it;
	// - a policy, the new attribute will be added as an ascendant of it.
	// Permissions.
	// sSessId needs the following permissions:
	// If the base node is a user:
	// "User create user attribute" on the base node
	// "Connector create user attribute" on the connector.
	// If the base node is a user attribute:
	// "User attribute create user attribute" on the base node.
	// If the base node is a policy class:
	// "Policy class create user attribute" on the base node.
	// If the base node is the connector:
	// "Connector create user attribute" on the base node.

	public Packet addUattr(String sClientId, String sSessId, String sProcId,
			String sName, String sDescr, String sInfo, String sBaseId,
			String sBaseType, String sBaseIsVos, String[] sProps) {

		if (sBaseIsVos.equalsIgnoreCase("yes")) {
			sBaseId = getAdminVosNodeOrigId(sBaseId);
		}

		// Test permissions.
		if (!requestAddUattrPerms(sSessId, sProcId, sBaseId, sBaseType)) {
			return failurePacket(reqPermsMsg);
		}

		Packet res =  addUattrInternal(sName, sDescr, sInfo, sBaseId,
				sBaseType, sProps);
		if (!res.hasError()) {
			setLastUpdateTimestamp();
		}
		return res;
	}


	public Packet addUattrInternal(String sName, String sDescr, String sInfo,
			String sBaseId, String sBaseType, String[] sProps) {
		// Test if duplicate name.
		if (attrNameExists(sName,PM_NODE.UATTR.value)) {
			return failurePacket("Attribute with duplicate name");
		}

		String sDn;

		// Prepare the attributes of the new attribute object.
		Attributes attrs = new BasicAttributes(true);
		RandomGUID myGUID = new RandomGUID();
		String sId = myGUID.toStringNoDashes();
		attrs.put("objectClass", sUserAttrClass);
		attrs.put("pmId", sId);
		attrs.put("pmName", sName);
		attrs.put("pmDescription", sDescr);
		attrs.put("pmOtherInfo", sInfo);

		// Set the back link towards the base node in the new attribute.
		// At the same time, prepare the base node's DN
		// and the attribute to be added to the base node.
		String sBaseDn;
		ModificationItem[] mods = new ModificationItem[1];
		if (sBaseType.equalsIgnoreCase(PM_NODE.USER.value)) {
			attrs.put("pmFromUser", sBaseId);
			attrs.put("pmToConnector", GlobalConstants.PM_CONNECTOR_ID);
			sBaseDn = "CN=" + sBaseId + "," + sUserContainerDN;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmToAttr", sId));
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			attrs.put("pmToAttr", sBaseId);
			sBaseDn = "CN=" + sBaseId + "," + sUserAttrContainerDN;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmFromAttr", sId));
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			attrs.put("pmToPolicy", sBaseId);
			sBaseDn = "CN=" + sBaseId + "," + sPolicyContainerDN;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmFromUserAttr", sId));
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			attrs.put("pmToConnector", sBaseId);
			sBaseDn = "CN=" + sBaseId + "," + sConnectorContainerDN;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmFromUserAttr", sId));
		} else {
			return failurePacket("Base node type incompatible with user attribute");
		}

		// Set the clicked (base) node's link to the new user attribute.
		try {
			ServerConfig.ctx.modifyAttributes(sBaseDn, mods);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Unable to set link to new user attribute in the base node");
		}

		// If the base node is a user, the new user attribute must also be an
		// ascendant
		// of the connector node.
		if (sBaseType.equalsIgnoreCase(PM_NODE.USER.value)) {
			sDn = "CN=" + GlobalConstants.PM_CONNECTOR_ID + "," + sConnectorContainerDN;
			mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
					new BasicAttribute("pmFromUserAttr", sId));
			try {
				ServerConfig.ctx.modifyAttributes(sDn, mods);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Unable to set link to new user attribute in the connector node");
			}
		}

		// Prepare the path and create.
		sDn = "CN=" + sId + "," + sUserAttrContainerDN;
		try {
			ServerConfig.ctx.bind(sDn, null, attrs);
		} catch (InvalidNameException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Invalid object name (id)" + sId);
		} catch (NameAlreadyBoundException e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Duplicate id " + sId);
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
			return failurePacket("Probably invalid id " + sId);
		}

		// Add the attribute's properties, if any.
		if (sProps != null) {
			int n = sProps.length;
			if (n <= 0) {
				return ADPacketHandler.getSuccessPacket();
			}
			mods = new ModificationItem[n];
			for (int i = 0; i < n; i++) {
				System.out.println("Prop " + sProps[i]);
				mods[i] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
						new BasicAttribute("pmProperty", sProps[i]));
			}
			try {
				ServerConfig.ctx.modifyAttributes("CN=" + sId + "," + sUserAttrContainerDN,
						mods);
			} catch (Exception e) {
				if (ServerConfig.debugFlag) {
					e.printStackTrace();
				}
				return failurePacket("Unable to set the new user attribute's properties!");
			}
		}

		// Return the attribute name and id.
		Packet res = new Packet();
		try {
			res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sId);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the resulting packet!");
		}

		//addADGraphNode(sBaseId, sId, sName, PM_NODE.UATTR.value);

		return res;
	}


	// For a given user attribute or the connector node, get the users (ids)
	// assigned to it.

	public Attribute getFromUsers(QueueElement qe) throws Exception {
		Attributes attrs;
		//Attribute attr;
		String sType = qe.getType();
		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
					+ sUserAttrContainerDN);
		} else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
					+ sConnectorContainerDN);
		} else {
			return null;
		}
		return attrs.get("pmFromUser");
	}

	// For a given user attribute or the connector node, get the users (ids)
	// assigned to it.

	public Attribute getFromUsers(String sBaseId, String sBaseType) {
		Attributes attrs;
		//Attribute attr;
		try {
			if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
				attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sUserAttrContainerDN);
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
				attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
						+ sConnectorContainerDN);
			} else {
				return null;
			}
			return attrs.get("pmFromUser");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// For a given user attribute or object attribute get the direct ascendant
	// attributes.
	// For a given operation set, get the user attributes assigned to it.

	public Attribute getFromAttrs(QueueElement qe) throws Exception {
		String sContainer = null;
		if (qe.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
		} else if (qe.getType().equalsIgnoreCase(PM_NODE.OATTR.value)
				|| qe.getType().equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (qe.getType().equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sContainer = sOpsetContainerDN;
		} else {
			return null;
		}
		Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
				+ sContainer);
		Attribute attr = attrs.get("pmFromAttr");
		return attr;
	}

	// For a given user attribute or object attribute get the direct ascendant
	// attributes.
	// For a given operation set, get the user attributes assigned to it.

	public Attribute getFromAttrs(String sId, String sType) throws Exception {
		String sContainer = null;
		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sContainer = sOpsetContainerDN;
		} else {
			return null;
		}
		Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sContainer);
		Attribute attr = attrs.get("pmFromAttr");
		return attr;
	}

	// For a given user, user attribute, or object attribute, get its direct
	// descendant attributes.
	// For a given operation set, get the object attributes this opset is
	// assigned to.

	public Attribute getToAttrs(QueueElement qe) throws Exception {
		String sContainer = null;
		if (qe.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
			sContainer = sUserContainerDN;
		} else if (qe.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
		} else if (qe.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (qe.getType().equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (qe.getType().equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sContainer = sOpsetContainerDN;
		} else {
			return null;
		}
		Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
				+ sContainer);
		Attribute attr = attrs.get("pmToAttr");
		return attr;
	}


	public Vector<String> getUserDescendantsInternalVector(String sUserId) {
		Attributes attrs;
		Attribute attr;

		Vector<String> vector = new Vector<String>();

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sUserId + "," + sUserContainerDN);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return vector;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				getDescAttrsVector((String) enumer.next(), vector);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vector;
	}

	// sAttrId is the id of a user attribute.

	public void getDescAttrsVector(String sAttrId, Vector<String> vector) {
		Attributes attrs;
		Attribute attr;

		// If the argument attribute is in the set, all its descendants already
		// are there.
		if (vector.contains(sAttrId)) {
			return;
		}
		// Add sAttrId to the vector, together with all its descendants.
		vector.add(sAttrId);
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sAttrId + ","
					+ sUserAttrContainerDN);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				getDescAttrsVector((String) enumer.next(), vector);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public HashSet<String> getUserDescendantsInternal(String sUserId) {
		Attributes attrs;
		Attribute attr;

		HashSet<String> set = new HashSet<String>();

		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sUserId + "," + sUserContainerDN);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return set;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				getDescAttrs((String) enumer.next(), set);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return set;
	}

	// sAttrId is the id of a user attribute.

	public void getDescAttrs(String sAttrId, HashSet<String> set) {
		Attributes attrs;
		Attribute attr;

		// If the argument attribute is in the set, all its descendants already
		// are there.
		if (set.contains(sAttrId)) {
			return;
		}
		// Add sAttrId to the set, together with all its descendants.
		set.add(sAttrId);
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sAttrId + ","
					+ sUserAttrContainerDN);
			attr = attrs.get("pmToAttr");
			if (attr == null) {
				return;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				getDescAttrs((String) enumer.next(), set);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void getAscAttrs(String sAttrId, String sType, HashSet<String> set) {
		Attributes attrs;
		Attribute attr;
		String sCont;

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sCont = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sCont = sObjAttrContainerDN;
		} else {
			return;
		}

		// If the argument attribute is in the set, all its ascendants already
		// are there.
		if (set.contains(sAttrId)) {
			return;
		}
		// Add sAttrId to the set, together with all its ascendants.
		set.add(sAttrId);
		try {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sAttrId + "," + sCont);
			attr = attrs.get("pmFromAttr");
			if (attr == null) {
				return;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				getAscAttrs((String) enumer.next(), sType, set);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// For a given user, user attribute, object attribute, or opset,
	// get its direct descendant attributes.

	public Attribute getToAttrs(String sId, String sType) throws Exception {
		String sContainer = null;
		if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
			sContainer = sUserContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sContainer = sOpsetContainerDN;
		} else {
			return null;
		}
		Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sContainer);
		Attribute attr = attrs.get("pmToAttr");
		return attr;
	}


	public Packet getContainers(String id, String type){
		Packet res = new Packet();
		String container = getContainerList(id, type);
		System.out.println("CONTAINER: " + container);
		try {
			res.addItem(ItemType.RESPONSE_TEXT, container == null ? "" : container);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}


	public Packet testGetPmViews(String sSessId, String sUserId, String sType) {
		if (!sType.equalsIgnoreCase(PM_NODE.USER.value)) {
			return failurePacket("Argument is not a user!");
		}

		Packet result =  computeAdminVos(sSessId, sUserId, sSessId);
		return result;
	}


	public Packet testGetMemberObjects(String sSessId, String sContainerId,
			String sType) {
		if (!sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				&& !sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			return failurePacket("Wrong type of container!");
		}

		Packet result = new Packet();
		HashSet<String> assocOattrs = new HashSet<String>();
		getMemberObjects(sContainerId, assocOattrs);

		try {
			Iterator<String> iter = assocOattrs.iterator();
			while (iter.hasNext()) {
				String sId = iter.next();
				result.addItem(ItemType.RESPONSE_TEXT,
						getEntityName(sId,PM_NODE.OATTR.value) + GlobalConstants.PM_FIELD_DELIM
						+ sId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet");
		}
		return result;
	}

	// Get all object attributes associated with objects of a container.

	public void getMemberObjects(String sContId, HashSet<String> assocOattrs) {
		Vector<String> queue = new Vector<String>();
		HashSet<String> visited = new HashSet<String>();
		try {
			// Insert the container into the queue.
			queue.add(sContId);

			// While the queue contains something...
			while (!queue.isEmpty()) {
				// ... remove an element from queue,
				String sOaId = queue.remove(0);
				// ... and continue if it's already visited.
				if (visited.contains(sOaId)) {
					continue;
				}

				// Mark this element as visited.
				visited.add(sOaId);

				// If it's associated to an object, add it to the result.
				Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOaId + ","
						+ sObjAttrContainerDN);
				Attribute oaAttr = attrs.get("pmFromAttr");
				Attribute assocAttr = attrs.get("pmAssocObj");
				if (assocAttr != null) {
					assocOattrs.add(sOaId);
				} else {
					if (oaAttr != null) {
						for (NamingEnumeration<?> enumer = oaAttr.getAll(); enumer.hasMore(); ) {
							queue.add((String) enumer.next());
						}
					}
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
	}


	public Packet getUsersOf(String sUattr) {
		String sUattrId = getEntityId(sUattr,PM_NODE.UATTR.value);
		if (sUattrId == null) {
			return failurePacket("No user attribute " + sUattr);
		}
		HashSet<String> users = new HashSet<String>();
		getMemberUsers(sUattrId, users);
		Iterator<String> iter = users.iterator();
		Packet result = new Packet();
		try {
			while (iter.hasNext()) {
				String sUserId = iter.next();
				String sUserName = getEntityName(sUserId,PM_NODE.USER.value);
				result.addItem(ItemType.RESPONSE_TEXT, sUserName
						+ GlobalConstants.PM_FIELD_DELIM + sUserId);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building result packet!");
		}
		return result;
	}

	// A non-recursive version for obtaining the users that are members of a
	// user attribute.
	// They are added to the HashSet users. Should be called like this:
	// HashSet users = new HashSet(); getMemberUsers(sUaId, users);

	public void getMemberUsers(String sUattrId, HashSet<String> users) {
		Vector<String> queue = new Vector<String>();
		HashSet<String> visited = new HashSet<String>();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sUattrId + ","
					+ sUserAttrContainerDN);
			// Get all users assigned to this user attribute and insert them
			// into the "users" set.
			Attribute userAttr = attrs.get("pmFromUser");
			if (userAttr != null) {
				for (NamingEnumeration<?> enumer = userAttr.getAll(); enumer.hasMore(); ) {
					users.add((String) enumer.next());
				}
			}
			// Get all user attributes assigned to this user attribute and
			// insert them
			// into the queue.
			Attribute uaAttr = attrs.get("pmFromAttr");
			if (uaAttr != null) {
				for (NamingEnumeration<?> enumer = uaAttr.getAll(); enumer.hasMore(); ) {
					queue.add((String) enumer.next());
				}
			}
			// Mark this user attribute as visited.
			visited.add(sUattrId);

			// While the queue contains something, remove and visit one element
			// from it.
			while (!queue.isEmpty()) {
				String sUaId = queue.remove(0);
				if (!visited.contains(sUaId)) {
					attrs = ServerConfig.ctx.getAttributes("CN=" + sUaId + ","
							+ sUserAttrContainerDN);
					userAttr = attrs.get("pmFromUser");
					if (userAttr != null) {
						for (NamingEnumeration<?> enumer = userAttr.getAll(); enumer.hasMore(); ) {
							users.add((String) enumer.next());
						}
					}
					uaAttr = attrs.get("pmFromAttr");
					if (uaAttr != null) {
						for (NamingEnumeration<?> enumer = uaAttr.getAll(); enumer.hasMore(); ) {
							queue.add((String) enumer.next());
						}
					}
					visited.add(sUaId);
				}
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
	}

	// Get the descendants of an object attribute in a given policy class.

	public HashSet<String> getDescOattrsInPc(String sOattrId, String sPcId) {
		Vector<String> queue = new Vector<String>();
		HashSet<String> visited = new HashSet<String>();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOattrId + ","
					+ sObjAttrContainerDN);
			Attribute attr = attrs.get("pmToAttr");
			if (attr == null) {
				return visited;
			}

			// The assoc attribute has children attributes. Check whether the
			// assoc. is in pc.
			if (!attrIsAscendantToPolicy(sOattrId,PM_NODE.OATTR.value, sPcId)) {
				return visited;
			}

			// Insert its children into the queue.
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				queue.add((String) enumer.next());
			}

			// Mark the assoc attribute as visited.
			visited.add(sOattrId);

			// Loop on the queue elements.
			while (!queue.isEmpty()) {
				// Extract the first from queue.
				String sOaId = queue.remove(0);

				// If this element was already visited, skip it.
				if (visited.contains(sOaId)) {
					continue;
				}

				// If this element is not in pc, skip it.
				if (!attrIsAscendantToPolicy(sOaId,PM_NODE.OATTR.value, sPcId)) {
					continue;
				}

				// Insert its children into the queue.
				attrs = ServerConfig.ctx.getAttributes("CN=" + sOaId + ","
						+ sObjAttrContainerDN);
				attr = attrs.get("pmToAttr");
				if (attr != null) {
					for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
						queue.add((String) enumer.next());
					}
				}
				// Mark this element as visited.
				visited.add(sOaId);
			}
		} catch (Exception e) {
			if (ServerConfig.debugFlag) {
				e.printStackTrace();
			}
		}
		return visited;
	}

	// Get the vector of object attributes an opset is assigned to.

	public Vector<String> getToAttrs(String sOpsetId) {
		Vector<String> v = new Vector<String>();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOpsetId + ","
					+ sOpsetContainerDN);
			Attribute attr = attrs.get("pmToAttr");
			if (attr == null) {
				return v;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				v.add((String) enumer.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("v: " + v);
		return v;
	}


	public Packet getToAttrsUser(String id, String type) {
		try {
			Attribute attr = getToAttrs(id, type);
			Packet res = new Packet();
			NamingEnumeration<?> user = attr.getAll();
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				res.addItem(ItemType.RESPONSE_TEXT, (String) enumer.next());
			}
			return res;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}


	public Packet getToAttrs1(String sSessId, String sOpsetId) {
		Packet res = new Packet();
		Vector v = getToAttrs(sOpsetId);
		for(int i = 0; i < v.size(); i++){
			try {
				res.addItem(ItemType.RESPONSE_TEXT, (String)v.get(i));
			} catch (PacketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return res;
	}

	// Get the vector of user attributes assigned to an opset.

	public Vector<String> getFromAttrs(String sOpsetId) {
		Packet res = new Packet();
		Vector<String> v = new Vector<String>();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sOpsetId + ","
					+ sOpsetContainerDN);
			Attribute attr = attrs.get("pmFromAttr");
			if (attr == null) {
				return v;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				v.add((String) enumer.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return v;
	}


	public Packet getFromAttrs1(String sSessId, String sOpsetId) {
		Packet res = new Packet();
		Vector v = getFromAttrs(sOpsetId);
		for(int i = 0; i < v.size(); i++){
			try {
				res.addItem(ItemType.RESPONSE_TEXT, (String)v.get(i));
			} catch (PacketException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	// For a given user attribute, get the operation sets this attribute is
	// assigned to.

	public Attribute getToOpsets(QueueElement qe) throws Exception {
		Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
				+ sUserAttrContainerDN);
		Attribute attr = attrs.get("pmToOpset");
		return attr;
	}


	public Packet getFromOpsets1(String sSessId, String sOpsetId) {
		Packet res = new Packet();
		Vector v = getFromOpsets(sOpsetId);
		for(int i = 0; i < v.size(); i++){
			try {
				res.addItem(ItemType.RESPONSE_TEXT, (String)v.get(i));
			} catch (PacketException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	// For an object attribute, get the operation sets assigned to this oattr.

	public Vector<String> getFromOpsets(String sAttrId) {
		Vector<String> v = new Vector<String>();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sAttrId + ","
					+ sObjAttrContainerDN);
			Attribute attr = attrs.get("pmFromOpset");
			if (attr == null) {
				return v;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				v.add((String) enumer.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	// For a user attribute, get the operation sets this attribute is assigned
	// to, as a vector.

	public Vector<String> getToOpsets(String sAttrId) {
		Vector<String> v = new Vector<String>();
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sAttrId + ","
					+ sUserAttrContainerDN);
			Attribute attr = attrs.get("pmToOpset");
			if (attr == null) {
				return v;
			}
			for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
				v.add((String) enumer.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return v;
	}

	// For a given user attribute or object attribute, get the policy classes
	// this attribute is assigned to (directly).

	public Attribute getToPolicies(QueueElement qe) throws Exception {
		String sContainer = null;
		if (qe.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sContainer = sUserAttrContainerDN;
		} else if (qe.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sContainer = sObjAttrContainerDN;
		} else if (qe.getType().equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sContainer = sObjAttrContainerDN;
		} else {
			return null;
		}
		Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
				+ sContainer);
		Attribute attr = attrs.get("pmToPolicy");
		return attr;
	}

	// For a given user or object attribute, get the policy classes the
	// attribute is assigned to.
	// Argument sType should bePM_NODE.UATTR.value orPM_NODE.OATTR.value orPM_NODE.ASSOC.value.

	public Attribute getToPolicies(String sId, String sType) throws Exception {
		Attributes attrs = null;

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sUserAttrContainerDN);
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sObjAttrContainerDN);
		} else {
			return null;
		}

		Attribute attr = attrs.get("pmToPolicy");
		return attr;
	}


	public Attribute getToConnector(String sId, String sType) throws Exception {
		Attributes attrs = null;

		if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sUserAttrContainerDN);
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sId + "," + sObjAttrContainerDN);
		} else {
			return null;
		}

		Attribute attr = attrs.get("pmToConnector");
		return attr;
	}

	// For the connector node, get the policy classes assigned to it (i.e.,
	// all).
	// NOTE THAT THE ATTRIBUTE SHOULD BE CALLED pmFromPolicy, BUT I WRONGLY
	// DEFINED IT AS BEING SINGLE VALUED, SO I HAD TO DEFINE A NEW ONE,
	// MULTI-VALUED.

	public Attribute getFromPolicies(QueueElement qe) throws Exception {
		Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
				+ sConnectorContainerDN);
		Attribute attr = attrs.get("pmFromPolicyClass");
		return attr;
	}


	public Attribute getFromPolicies(String sBaseId, String sBaseType)
			throws Exception {
		Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
				+ sConnectorContainerDN);
		Attribute attr = attrs.get("pmFromPolicyClass");
		return attr;
	}

	// For the connector node or an object attribute, get the operation sets
	// assigned to it.

	public Attribute getFromOpsets(QueueElement qe) throws Exception {
		Attributes attrs;
		String sDn;
		String sType = qe.getType();

		if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sDn = "CN=" + qe.getId() + "," + sConnectorContainerDN;
		} else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sDn = "CN=" + qe.getId() + "," + sObjAttrContainerDN;
		} else {
			return null;
		}
		attrs = ServerConfig.ctx.getAttributes(sDn);
		return attrs.get("pmFromOpSet");
	}


	public Attribute getFromOpsets(String sBaseId, String sBaseType)
			throws Exception {
		Attributes attrs;
		String sDn;

		if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sDn = "CN=" + sBaseId + "," + sConnectorContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)
				|| sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
			sDn = "CN=" + sBaseId + "," + sObjAttrContainerDN;
		} else {
			return null;
		}
		attrs = ServerConfig.ctx.getAttributes(sDn);
		return attrs.get("pmFromOpSet");
	}

	// For a policy class or connector node, get the user attributes
	// (directly) assigned to it. Params:
	// qe: a QueueElement that contains the id and type of the base node.

	public Attribute getFromUserAttrs(QueueElement qe) throws Exception {
		Attributes attrs;
		//Attribute attr;
		String sType = qe.getType();
		if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
					+ sPolicyContainerDN);
		} else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
					+ sConnectorContainerDN);
		} else {
			return null;
		}
		return attrs.get("pmFromUserAttr");
	}

	// For a policy class or connector node, get the user attributes
	// (directly) assigned to it.

	public Attribute getFromUserAttrs(String sBaseId, String sBaseType)
			throws Exception {
		Attributes attrs;
		//Attribute attr;
		if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
					+ sPolicyContainerDN);
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
					+ sConnectorContainerDN);
		} else {
			return null;
		}
		return attrs.get("pmFromUserAttr");
	}
	
	public Packet getFromUserAttrsPacket(String sBaseId, String sBaseType){
		Packet res = new Packet();
		try{
		Attribute attr = getFromUserAttrs(sBaseId, sBaseType);
		if (attr != null) {
			for (NamingEnumeration<?> attrEnum = attr.getAll(); attrEnum.hasMore(); ) {
				String sId = (String) attrEnum.next();
				res.addItem(ItemType.RESPONSE_TEXT, sId);
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		return res;
	}

	// Get the user attributes that are direct ascendants of a base node. The
	// base node
	// can be a user attribute, the connector, a policy class, or an operation
	// set.

	public Packet getDascUattrs(String sBaseName, String sBaseType) {
		String sBaseId = getEntityId(sBaseName, sBaseType);
		if (sBaseId == null) {
			return failurePacket("No PM entity \"" + sBaseName + "\" of type "
					+ sBaseType);
		}
		String sCont = null;
		if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sCont = sConnectorContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sCont = sPolicyContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont = sUserAttrContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
			sCont = sOpsetContainerDN;
		} else {
			return failurePacket("Invalid type " + sBaseType
					+ " in getDascUattrs");
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + "," + sCont);
			Attribute attr = null;
			if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)
					|| sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
				attr = attrs.get("pmFromUserAttr");
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)
					|| sBaseType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
				attr = attrs.get("pmFromAttr");
			}

			Packet result = new Packet();
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					String sName = getEntityName(sId,PM_NODE.UATTR.value);
					result.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_FIELD_DELIM + sId);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getDascUattrs");
		}
	}

	// Get the users that are direct ascendants of a base node. The base node
	// can be a user attribute or the connector.

	public Packet getDascUsers(String sBaseName, String sBaseType) {
		String sBaseId = getEntityId(sBaseName, sBaseType);
		if (sBaseId == null) {
			return failurePacket("No PM entity \"" + sBaseName + "\" of type "
					+ sBaseType);
		}
		String sCont = null;
		if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sCont = sConnectorContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
			sCont = sUserAttrContainerDN;
		} else {
			return failurePacket("Invalid type " + sBaseType
					+ " in getDascUsers");
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + "," + sCont);
			Attribute attr = attrs.get("pmFromUser");

			Packet result = new Packet();
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					String sName = getEntityName(sId,PM_NODE.USER.value);
					result.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_FIELD_DELIM + sId);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getDascUsers");
		}
	}

	// Get the object attributes that are direct ascendants of a base node AND
	// NOT OBJECTS.
	// The base node can be an object attribute, the connector, or a policy
	// class.

	public Packet getDascOattrs(String sBaseName, String sBaseType) {
		String sBaseId = getEntityId(sBaseName, sBaseType);
		if (sBaseId == null) {
			return failurePacket("No PM entity \"" + sBaseName + "\" of type "
					+ sBaseType);
		}
		String sCont = null;
		if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sCont = sConnectorContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sCont = sPolicyContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sCont = sObjAttrContainerDN;
		} else {
			return failurePacket("Invalid type " + sBaseType
					+ " in getDascOattrs");
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + "," + sCont);
			Attribute attr = null;
			if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)
					|| sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
				attr = attrs.get("pmFromObjAttr");
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
				attr = attrs.get("pmFromAttr");
			}

			Packet result = new Packet();
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					if (hasAssocObj(sId)) {
						continue;
					}
					String sName = getEntityName(sId,PM_NODE.OATTR.value);
					result.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_FIELD_DELIM + sId);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getDascOattrs");
		}
	}

	// Get the object attributes that are direct ascendants of a base node AND
	// ALSO OBJECTS.
	// The base node can be an object attribute, the connector, or a policy
	// class.

	public Packet getDascObjects(String sBaseName, String sBaseType) {
		String sBaseId = getEntityId(sBaseName, sBaseType);
		if (sBaseId == null) {
			return failurePacket("No PM entity \"" + sBaseName + "\" of type "
					+ sBaseType);
		}
		String sCont = null;
		if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sCont = sConnectorContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			sCont = sPolicyContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sCont = sObjAttrContainerDN;
		} else {
			return failurePacket("Invalid type " + sBaseType
					+ " in getDascObjects");
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + "," + sCont);
			Attribute attr = null;
			if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)
					|| sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
				attr = attrs.get("pmFromObjAttr");
			} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
				attr = attrs.get("pmFromAttr");
			}

			Packet result = new Packet();
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					if (!hasAssocObj(sId)) {
						continue;
					}
					String sName = getEntityName(sId,PM_NODE.OATTR.value);
					result.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_FIELD_DELIM + sId);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getDascObjects");
		}
	}

	// Get the operation sets that are direct ascendants of a base node. The
	// base node
	// can be an object attribute or the connector.

	public Packet getDascOpsets(String sBaseName, String sBaseType) {
		String sBaseId = getEntityId(sBaseName, sBaseType);
		if (sBaseId == null) {
			return failurePacket("No PM entity \"" + sBaseName + "\" of type "
					+ sBaseType);
		}
		String sCont;
		if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			sCont = sConnectorContainerDN;
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
			sCont = sObjAttrContainerDN;
		} else {
			return failurePacket("Invalid base node type " + sBaseType
					+ " in getDascOpsets");
		}
		try {
			Attributes attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + "," + sCont);
			Attribute attr = attrs.get("pmFromOpSet");

			Packet result = new Packet();
			if (attr != null) {
				for (NamingEnumeration<?> enumer = attr.getAll(); enumer.hasMore(); ) {
					String sId = (String) enumer.next();
					String sName = getEntityName(sId,PM_NODE.OPSET.value);
					result.addItem(ItemType.RESPONSE_TEXT, sName
							+ GlobalConstants.PM_FIELD_DELIM + sId);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception in getDascOpsets");
		}
	}

	// For a policy class or the connector node, get the object attributes
	// (directly) assigned to it. Params:
	// qe: a QueueElement that contains the id and type of the policy class
	// or the connector node.

	public Attribute getFromObjAttrs(QueueElement qe) throws Exception {
		Attributes attrs;
		//Attribute attr;
		String sType = qe.getType();
		if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
					+ sPolicyContainerDN);
		} else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + qe.getId() + ","
					+ sConnectorContainerDN);
		} else {
			return null;
		}
		return attrs.get("pmFromObjAttr");
	}


	public Attribute getFromObjAttrs(String sBaseId, String sBaseType)
			throws Exception {
		Attributes attrs;
		if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
					+ sPolicyContainerDN);
		} else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
			attrs = ServerConfig.ctx.getAttributes("CN=" + sBaseId + ","
					+ sConnectorContainerDN);
		} else {
			return null;
		}
		return attrs.get("pmFromObjAttr");
	}


	/**
	 * Get policy nodes of the PM connector whether accessible or not.
	 */
	public Packet getPolicyClassNodes_SteveQ() throws PmServerException {
		try {
			Packet resultPacket = new Packet();
			Vector policyClasses = getPolicyClasses();
			for (int i = 0; i < policyClasses.size(); i++) {
				String policyId = (String) policyClasses.get(i);
				String policyName = getEntityName(policyId,PM_NODE.POL.value);
				resultPacket.addItem(ItemType.RESPONSE_TEXT,PM_NODE.POL.value
						+ GlobalConstants.PM_FIELD_DELIM + policyId + GlobalConstants.PM_FIELD_DELIM
						+ policyName);
			}
			return resultPacket;
		} catch (PacketException pe) {
			throw new PmServerException(pe.toString());
		}
	}

	/**
	 * Get the accessible parent nodes of the user-selected base node to display
	 * in the GUI.*/
	public Packet getParentNodes_SteveQ(String sSessId, 
			String sBaseName, String sBaseId, String sBaseType, String sGraphType, 
			String sUserId, String policyClassId) {
		try {
			Packet result = new Packet();
			getFirstAccessibleAscendants(sBaseName, sBaseId, sBaseType, sUserId,
					policyClassId, result);
			return result;
		} catch (PmServerException pse) {
			return failurePacket(pse.toString());
		}
	}  


	public Packet getConnector() {
		Packet result = new Packet();
		try {
			result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
					+ GlobalConstants.PM_FIELD_DELIM + GlobalConstants.PM_CONNECTOR_ID + GlobalConstants.PM_FIELD_DELIM
					+ GlobalConstants.PM_CONNECTOR_NAME);
		} catch (Exception e) {
			e.printStackTrace();
			return failurePacket("Exception when building the result packet in getConnector()");
		}
		return result;
	}

	class SimpleOattr {
		private String sId;
		private ArrayList ops;
		private ArrayList pcs;
		
		public SimpleOattr(String sId, ArrayList ops, ArrayList pcs) {
			this.sId = sId;
			this.ops = ops;
			this.pcs = pcs;
		}
		
		public SimpleOattr(String sOaId) {
			this.sId = sOaId;
			this.ops = new ArrayList();
			this.pcs = new ArrayList();
		}
		
		public String getId() {
			return sId;
		}
		
		public ArrayList getOps() {
			return this.ops;
		}
		
		public void setOps(ArrayList ops) {
			this.ops = ops;
		}
		
		public void addOp(String sOp) {
			if (sOp == null || sOp.length() == 0) return;
			if (this.ops == null) {
				this.ops = new ArrayList();
				this.ops.add(sOp);
				return;
			}
			for (int j = 0; j < this.ops.size(); j++) {
				String sOpComp = (String)this.ops.get(j);
				if (sOp.equalsIgnoreCase(sOpComp)) {
					return;
				}
			}
			this.ops.add(sOp);
		}
		
		public void addOps(ArrayList opsToAdd) {
			if (opsToAdd == null || opsToAdd.size() == 0) return;
			if (this.ops == null) {
				this.ops = opsToAdd;
				return;
			}
			for (int i = 0; i < opsToAdd.size(); i++) {
				String sOp = (String)opsToAdd.get(i);
				boolean found = false;
				for (int j = 0; j < this.ops.size(); j++) {
					String sOpComp = (String)this.ops.get(j);
					if (sOp.equalsIgnoreCase(sOpComp)) {
						found = true;
						break;
					}
				}
				if (!found) this.ops.add(sOp);
			}
		}

		public ArrayList getPcs() {
			return this.pcs;
		}
		
		public void setPcs(ArrayList pcs) {
			this.pcs = pcs;
		}
		
		public void addPcs(ArrayList pcsToAdd) {
			if (pcsToAdd == null || pcsToAdd.size() == 0) return;
			if (this.pcs == null) {
				this.pcs = pcsToAdd;
				return;
			}
			for (int i = 0; i < pcsToAdd.size(); i++) {
				String sPc = (String) pcsToAdd.get(i);
				boolean found = false;
				for (int j = 0; j < this.pcs.size(); j++) {
					String sPcComp = (String) this.pcs.get(j);
					if (sPc.equalsIgnoreCase(sPcComp)) {
						found = true;
						break;
					}
				}
				if (!found) this.pcs.add(sPc);
			}
		}

		public void addPc(String sPc) {
			if (sPc == null || sPc.length() == 0) return;
			if (this.pcs == null) {
				this.pcs = new ArrayList();
				this.pcs.add(sPc);
				return;
			}
			for (int j = 0; j < this.pcs.size(); j++) {
				String sPcComp = (String)this.pcs.get(j);
				if (sPc.equalsIgnoreCase(sPcComp)) {
					return;
				}
			}
			this.pcs.add(sPc);
		}
		
		public boolean containsPc(String sPc) {
			if (sPc == null || sPc.length() == 0) return false;
			if (this.pcs == null) return false;
			for (int j = 0; j < this.pcs.size(); j++) {
				String sPcComp = (String)this.pcs.get(j);
				if (sPc.equalsIgnoreCase(sPcComp)) return true;
			}
			return false;
		}
	}
	
	class QueueElement {

		/**
		 * @uml.property  name="sType"
		 */
		private String sType;
		/**
		 * @uml.property  name="sId"
		 */
		private String sId;
		/**
		 * @uml.property  name="nLevel"
		 */
		private int nLevel;

		public QueueElement(String sType, String sId, int nLevel) {
			this.sType = sType;
			this.sId = sId;
			this.nLevel = nLevel;
		}

		public String getType() {
			return sType;
		}

		public String getId() {
			return sId;
		}

		public int getLevel() {
			return nLevel;
		}
	}

	class AdminVosQueueElement {
		// The nodes inserted in the queue are: policy classes, user attributes,
		// users, object attributes, object attributes associated to objects,
		// operation sets.
		// Node type. Its value may bePM_NODE.POL.value,PM_NODE.UATTR.value,PM_NODE.USER.value,
		//PM_NODE.OATTR.value,PM_NODE.ASSOC.value,PM_NODE.OPSET.value.

		/**
		 * @uml.property  name="sType"
		 */
		private String sType;
		// The original id of the node:
		/**
		 * @uml.property  name="sId"
		 */
		private String sId;
		// The new id (which is also the CN in the VOS container). Link the direct
		// ascendants of sId to this id:
		/**
		 * @uml.property  name="sDescId"
		 */
		private String sDescId;

		public AdminVosQueueElement(String sType, String sId, String sDescId) {
			this.sType = sType;
			this.sId = sId;
			this.sDescId = sDescId;
		}

		public String getType() {
			return sType;
		}

		public String getId() {
			return sId;
		}

		public String getDesc() {
			return sDescId;
		}
	}

	class VosQueueElement {
		// The nodes inserted in the queue are always object attributes.
		// There is no need for type, nor for level.
		// This is the original id of the node inserted in queue:

		/**
		 * @uml.property  name="sOaId"
		 */
		private String sOaId;
		// This is the new id (which is also the CN in the VOS container) of the
		// descendant to use to link the direct ascendants of sOaId to.
		/**
		 * @uml.property  name="sDescId"
		 */
		private String sDescId;

		public VosQueueElement(String sOaId, String sDescId) {
			this.sOaId = sOaId;
			this.sDescId = sDescId;
		}

		public String getId() {
			return sOaId;
		}

		public String getDesc() {
			return sDescId;
		}
	}

	// Class Action's Operand
	class ActOpnd {

		/**
		 * @uml.property  name="sName"
		 */
		private String sName;
		/**
		 * @uml.property  name="sType"
		 */
		private String sType;
		/**
		 * @uml.property  name="sId"
		 */
		private String sId;
		/**
		 * @uml.property  name="bSubgraph"
		 */
		private boolean bSubgraph;
		/**
		 * @uml.property  name="bComplement"
		 */
		private boolean bComplement;
		/**
		 * @uml.property  name="sError"
		 */
		private String sError;

		public ActOpnd(String sName, String sType, String sId, boolean bSubgraph,
				boolean bComplement, String sError) {
			this.sName = sName;
			this.sType = sType;
			this.sId = sId;
			this.bSubgraph = bSubgraph;
			this.bComplement = bComplement;
			this.sError = sError;
		}

		public String getName() {
			return sName;
		}

		public String getId() {
			return sId;
		}

		public String getType() {
			return sType;
		}

		public boolean isSubgraph() {
			return bSubgraph;
		}

		public boolean isComplement() {
			return bComplement;
		}

		public String getError() {
			return sError;
		}

		public void setName(String sName) {
			this.sName = sName;
		}

		public void setId(String sId) {
			this.sId = sId;
		}

		public void setType(String sType) {
			this.sType = sType;
		}

		public void setSubgraph(boolean bSubgraph) {
			this.bSubgraph = bSubgraph;
		}

		public void setComplement(boolean bComplement) {
			this.bComplement = bComplement;
		}

		public void setError(String sError) {
			this.sError = sError;
		}
	}

	class EventContext {

		/**
		 * @uml.property  name="sSessId"
		 */
		private String sSessId;
		/**
		 * @uml.property  name="sProcId"
		 */
		private String sProcId;
		/**
		 * @uml.property  name="sEventName"
		 */
		private String sEventName;
		/**
		 * @uml.property  name="sObjName"
		 */
		private String sObjName;
		/**
		 * @uml.property  name="sObjClass"
		 */
		private String sObjClass;
		/**
		 * @uml.property  name="sObjType"
		 */
		private String sObjType;
		/**
		 * @uml.property  name="sObjId"
		 */
		private String sObjId;
		/**
		 * @uml.property  name="ctx1"
		 */
		private String ctx1;
		/**
		 * @uml.property  name="sctx2"
		 */
		private String sctx2;

		public EventContext(String sSessId, String sProcId, String sEventName,
				String sObjName, String sObjClass, String sObjType, String sObjId,
				String ctx1, String sctx2) {
			this.sSessId = sSessId;
			this.sProcId = sProcId;
			this.sEventName = sEventName;
			this.sObjName = sObjName;
			this.sObjClass = sObjClass;
			this.sObjType = sObjType;
			this.sObjId = sObjId;
			this.ctx1 = ctx1;
			this.sctx2 = sctx2;
		}

		public String getSessId() {
			return sSessId;
		}

		public String getProcId() {
			return sProcId;
		}

		public String getEventName() {
			return sEventName;
		}

		public String getObjName() {
			return sObjName;
		}

		public String getObjId() {
			return sObjId;
		}

		public String getObjClass() {
			return sObjClass;
		}

		public String getObjType() {
			return sObjType;
		}

		public String getctx1() {
			return ctx1;
		}

		public String getctx2() {
			return sctx2;
		}
	}

	public class PmServerException extends Exception {
		public PmServerException(String msg) {
			super(msg);
		}
		public PmServerException(String msg, Throwable t){
			super(msg, t);
		}
	}
}
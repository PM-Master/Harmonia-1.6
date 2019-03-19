package gov.nist.csd.pm.server.dao.MySQLDB;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.*;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketException;
import gov.nist.csd.pm.common.net.PacketManager;
import gov.nist.csd.pm.common.util.RandomGUID;
import gov.nist.csd.pm.common.util.UtilMethods;
import gov.nist.csd.pm.server.audit.Audit;
import gov.nist.csd.pm.server.graph.*;
import gov.nist.csd.pm.server.packet.SQLPacketHandler;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.*;

import static gov.nist.csd.pm.common.config.ServerConfig.denyMgr;
import static gov.nist.csd.pm.common.config.ServerConfig.graphMgr;
import static gov.nist.csd.pm.common.constants.GlobalConstants.*;
import static gov.nist.csd.pm.common.constants.MySQL_Functions.*;
import static gov.nist.csd.pm.common.constants.MySQL_Statements.*;
import static gov.nist.csd.pm.common.constants.MySQL_StoredProcedures.*;
import static gov.nist.csd.pm.common.net.Packet.dnrPacket;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import static gov.nist.csd.pm.common.net.Packet.getSuccessPacket;


public class CommonSQLDAO{

    private DateFormat dfUpdate = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
    private String sLastUpdateTimestamp;

    // Steve - Added (3/6/15) - A hashtable to store the accessible objects and object attributes of the users.
    // The key is the user id. The value is a HashSet of object attribute IDs that are
    // accessible to the key user. The HashSet is computed by computeFastVos().
    private Hashtable htAcc = new Hashtable();

    public final int DEPTH = 1;
    public final int NO_DEPTH = -1;

    public final String NO = "no";
    public final String YES = "yes";

    public String errorMessage = "";

    public CommonSQLDAO(){

    }

    // *****************************************************************************************************
    // DATA STRUCTURE METHODS
    // *****************************************************************************************************
    public PmGraphManager getGraphMgr(){
        return graphMgr;
    }
    private boolean creating;
    private void addPmGraphNode(String baseId, String id, String name, String type){
        creating = true;
        HashSet<String> members = getFromAttrsSet(id, null, 1);
        for(String s : members){
            updatePmGraphNode(s);
        }
        HashSet<String> containers = getToAttrsSet(id, null, 1);
        for(String s : containers){
            updatePmGraphNode(s);
        }
        HashSet<String> operations = null;
        if(type.equals(PM_NODE.OPSET.value)){
            try {
                operations = new HashSet<String>(getOpsetOperations(id));
            } catch (Exception e) {}
        }
        PmGraphNode node = new PmGraphNode(name, type, id, members, containers, operations);
        graphMgr.addNode(baseId, node);
        if(baseId != null) {
            updatePmGraphNode(baseId);
        }
        creating = false;
    }

    private void deletePmGraphNode(String id){
        graphMgr.deleteNode(id);
    }

    private void updatePmGraphNode(String id){
        creating = true;
        HashSet<String> members = getFromAttrsSet(id, null, 1);
        HashSet<String> containers = getToAttrsSet(id, null, 1);
        HashSet<String> operations = null;
        PmGraphNode node = graphMgr.getGraph().getNode(id);
        if(node.getType().equals(PM_NODE.OPSET.value)){
            try {
                operations = new HashSet<String>(getOpsetOperations(id));
            } catch (Exception e) {}
        }
        PmGraphNode updatedNode = new PmGraphNode(node.getName(), node.getType(), id, members, containers, operations);
        graphMgr.updateNode(updatedNode);
        creating = false;
    }

    private void assignPmGraphNode(String startId, String endId){
        //add endId to the members of startId and startId to the containers of endId
        PmGraphNode start = graphMgr.getGraph().getNode(startId);
        start.addMember(endId);
        PmGraphNode end = graphMgr.getGraph().getNode(endId);
        end.addContainer(startId);
    }

    private void deleteAssignmentPmGraphNode(String startId, String endId){
        PmGraphNode start = graphMgr.getGraph().getNode(startId);
        start.deleteMember(endId);
        PmGraphNode end = graphMgr.getGraph().getNode(endId);
        end.deleteContainer(startId);
    }

    public Packet updateAllNodes(){
        List<PmGraphNode> nodes = graphMgr.getAllNodes();
        for(PmGraphNode n : nodes){
            updatePmGraphNode(n.getId());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    class AssignmentNode{
        String id;
        String name;
        Integer depth;
        public AssignmentNode(String aId, String aName, Integer aDepth){
            id = aId;
            name = aName;
            depth = aDepth;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Integer getDepth() {
            return depth;
        }
        public void setDepth(Integer depth) {
            this.depth = depth;
        }
    }

    public List<AssignmentNode> getSubPmGraph(String start){
        List<AssignmentNode> subGraph = new ArrayList<AssignmentNode>();
        try {
            ArrayList<ArrayList<Object>> result = select(GET_ASSIGNMENT_SUBGRAPH, Integer.valueOf(start));
            for(ArrayList<Object> row : result){
                AssignmentNode aN = new AssignmentNode(String.valueOf(row.get(0)), (String)row.get(1), (Integer)row.get(2));
                subGraph.add(aN);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return subGraph;
    }

    // *****************************************************************************************************
    // END DATA STRUCTURE METHODS
    // *****************************************************************************************************

    // *****************************************************************************************************
    // COMMON METHODS
    // *****************************************************************************************************
    /*public List<Integer> getAllAccessibleNodes(Integer uaId){
        long start = System.nanoTime();
        try {

            if(getEntityName(uaId.toString(), PM_NODE.USER.value).equals("super")){
                return extractIntegers(select(GET_ALL_NODES, null));
            }else if(graphMgr.getGraph().getNode(uaId).getMembers().contains(1)){
                return extractIntegers(select(GET_ALL_NODES, null));
            }
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, uaId);
            return extractIntegers(select(GET_ALL_ACC_NODES, params));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally{
            long end = System.nanoTime();
            System.out.println("getAllAccessibleNodes: " + (end - start));
        }
    }

    class Session {
        private Integer id;
        private PmGraph graph;

        public Session(Integer i, PmGraph g){
            id = i;
            graph = g;
        }

        public PmGraph getGraph(){
            return graph;
        }
    }*/

    public String getErrorMessage(){
        return errorMessage;
    }

    public Integer createNode(String sName, String typeName, String description, Integer baseId) throws Exception{
        if(entityNameExists(sName, typeName)){
            errorMessage = "entity name " + sName + " already exists";
            throw new Exception(errorMessage);
        }
        Integer id = (Integer)executeFunction(CREATE_NODE_FUN, ParamType.INT, sName, typeName, description, baseId);

        addPmGraphNode(baseId.toString(), id.toString(), sName, typeName);
        return id;
    }

    public void deleteNode(Integer id) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, id);
        delete(DELETE_NODE, params);

        deletePmGraphNode(id.toString());
    }

    //is there an assignment start->end
    public boolean isAssigned(Integer end, Integer start) throws Exception {
        ArrayList<ArrayList<Object>> returned = select(IS_ASSIGNED, start, end);
        return (Long) returned.get(0).get(0) != 0;
    }

    public boolean hasAscendants(String sId) throws Exception {
        if (sId == null) return false;
        ArrayList<ArrayList<Object>> returned = select(HAS_ASCENDENTS, Integer.valueOf(sId));
        return (Long)returned.get(0).get(0) > 0;
    }

    public boolean hasDescendants(String sId) throws Exception {
        if (sId == null) return false;
        ArrayList<ArrayList<Object>> returned = select(HAS_DESCENDENTS, Integer.valueOf(sId));
        return (Long) returned.get(0).get(0)>0;

    }

    public void insertProperties(String[] sProps, String sType, Integer nodeId){
        if (sProps == null || sProps.length <= 0)
            return;
        for (int i = 0; i < sProps.length; i++) {
            System.out.println("i = " + i);
            System.out.println("Prop = " + sProps[i]);
            if (sProps[i] != null) {
                addPropInternal(nodeId, sType, sProps[i]);
            }
        }
    }

    public void insertOrModifyProperty(String sPropName, String sPropValue, Integer nodeId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sPropName);
        params.addParam(ParamType.STRING, sPropValue);
        params.addParam(ParamType.INT, nodeId);
        executeStoredProcedure(SET_PROPERTY, params);
    }

    public void deleteProperty(String sPropName) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sPropName);
        executeStoredProcedure(DELETE_PROPERTY_SP, params);
    }

    public void assign(Integer start, Integer end) throws Exception {
        if (isAscendant(end.toString(), start.toString())) {
            throw new Exception("end node: " + end + " already contained in start node: " + start);
        }

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, start);
        params.addParam(ParamType.INT, end);
        executeStoredProcedure(CREATE_ASSIGNMENT, params);
        int assignmentId = extractIntegers(select(GET_MAX_ASSIGNMENT_ID_BTW, start, end)).get(0);
        ArrayList<Integer> pathId = extractIntegers(select(GET_ASSIGNMENT_PATH_ID, assignmentId));
        Integer assignmentPathId = null;
        if(pathId != null && !pathId.isEmpty()){
            assignmentPathId = pathId.get(0);
        }

        List<Integer> children = getFromAttrs(end.toString(), null, NO_DEPTH);
        for(Integer i : children){
            if(i.equals(end))continue;
            if(isAssigned(i, start))continue;
            if(!(getNodeTypeValue(i).equals(PM_NODE.OATTR.value) || getNodeTypeValue(i).equals(PM_NODE.ASSOC.value)))continue;

            //create assignment
            executeStoredProcedure(CREATE_ASSIGNMENT, start, i);

            //select max assignment_id from assignment where start = start and end = i;
            assignmentId = extractIntegers(select(GET_MAX_ASSIGNMENT_ID_BTW, start, i)).get(0);

            //update path_id to the path_id from the original assignment start->end
            update(UPDATE_ASSIGNMENT_PATH_ID, assignmentPathId, assignmentId);

            int depth = getDepthFrom(end, i)+1;
            update(UPDATE_ASSIGNMENT_DEPTH, depth, assignmentId);
        }

        assignPmGraphNode(start.toString(), end.toString());
    }

    private int getDepthFrom(int start, int end){
        try {
            return extractIntegers(select(GET_DEPTH_FROM, start, end, end)).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean isNum(String strNum) {
        boolean ret = true;
        try {
            Double.parseDouble(strNum);
        }catch (NumberFormatException e) {
            ret = false;
        }
        return ret;
    }
    // *****************************************************************************************************
    // COMMON METHODS END
    // *****************************************************************************************************

    // *****************************************************************************************************
    // COMMAND METHODS START
    // *****************************************************************************************************

    public Packet getFromUserAttrsPacket(String sBaseId, String sBaseType){
        Packet res = new Packet();
        try{
            ArrayList<Integer> attrs = getFromUserAttrs(sBaseId, sBaseType);
            if (attrs != null) {
                for (Integer i : attrs) {
                    String sId = String.valueOf(i);
                    res.addItem(ItemType.RESPONSE_TEXT, sId);
                }
            }else{
                return failurePacket("Could not get user attributes assigned to attribute: " + sBaseId);
            }
        }catch(Exception e){
            e.printStackTrace();
            return failurePacket("Could not get user attributes assigned to attribute: " + sBaseId);
        }
        return res;
    }

    public Packet isRecordPacket(String sId){
        Packet res = new Packet();
        if (sId==null) {
            return failurePacket("null id");
        }
        try {
            boolean isR = isRecord(sId);
            res.addItem(ItemType.RESPONSE_TEXT, (isR) ? YES : NO);
        } catch (Exception e) {
            e.printStackTrace();
            return SQLPacketHandler.getFailurePacket(e.getMessage());
        }

        return res;
    }

    public Packet createSchemaPC(String sSessId, String sProcId, String policyType,
                                 String oattrType, String sPolicyName, String sUattr, String[] props){
        String sCmd;
        Packet res;
        try {
            sCmd = "add|" + policyType + "|" + sPolicyName + "|c|PM";
            res = interpretCmd(sSessId, sCmd);
            if(res.hasError()){
                return res;
            }

            for(String s : props){
                sCmd = "add|prop|" + s + "|p|" + sPolicyName;
                res = interpretCmd(sSessId, sCmd);
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
            if(isAsgn.equalsIgnoreCase(NO)){
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

            sCmd = "add|op|" + PM_ANY_ANY + "|s|" + id;
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
        String sCmd;
        Packet res;

        sCmd = "add|" + oattrType + "|" + name + "|" + baseType + "|" + baseName;
        res = interpretCmd(sSessId, sCmd);
        if(res.hasError()){
            return res;
        }
        return res;
    }

    public Packet getContainers(String id, String type){
        if(id == null || id.equals("")){
            return failurePacket("null container list for id: " + id);
        }
        Packet res = new Packet();
        try {
            String containers = getContainerList(id, type);
            res.addItem(ItemType.RESPONSE_TEXT, containers == null ? "" : containers);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    public Packet getToAttrsUser(String id, String type) {
        try {
            ArrayList<Integer> attr = getToAttrs(id, PM_NODE.UATTR.value, DEPTH);
            Packet res = new Packet();
            for (int i = 0; i < attr.size(); i++) {
                res.addItem(ItemType.RESPONSE_TEXT, String.valueOf(attr.get(i)));
            }
            return res;
        } catch (Exception e) {
            return failurePacket("Could not get attrs user is assigned to");
        }
    }

    public Packet getPermittedOpsOnEntity(String sCrtSessId, String sProcId,
                                          String sTgtId, String sTgtType){
        try {
            return SQLPacketHandler.setToPacket(getPermittedOpsOnEntityInternal(sCrtSessId, sTgtId,sTgtId, sTgtType));
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet getAssocOattr1(String sObjId){
        Packet res = new Packet();
        try {
            String assoc = getAssocOattr(sObjId);
            res.addItem(ItemType.RESPONSE_TEXT, assoc);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    /**
     * get all reps
     * @param id ID to get reps of
     * @param type type of the entity
     * @return a Packet containing all of the reps of an entity
     */
    public Packet getReps(String id, String type){
        if(id == null || id.equals("")){
            return failurePacket("getReps received a null id");
        }
        Packet res = new Packet();
        try {
            HashSet<String> reps = getObjectsRepresentingEntity(id, type);
            Iterator hsiter = reps.iterator();
            while (hsiter.hasNext()) {
                String rep = (String) hsiter.next();
                res.addItem(ItemType.RESPONSE_TEXT, rep);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    public Packet getFromOpsets1(String sSessId, String sOattrId) {
        Packet res = new Packet();
        try {
            ArrayList<Integer> v = getFromOpsets(sOattrId);
            for (int i = 0; i < v.size(); i++) {
                res.addItem(ItemType.RESPONSE_TEXT, v.get(i) + "");

            }
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }
        return res;
    }

    public Packet getPermittedOps1(String sSessId, String sProcId,
                                   String sBaseId, String sBaseType){
        Packet res = new Packet();
        HashSet<String> resPerms;

        try{
            if (!sessionExists(sSessId)) {
                reqPermsMsg = "You're not in a Policy Machine session!";
                return failurePacket(reqPermsMsg);
            }
            resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
                    sBaseType);
            Iterator<String> hsiter = resPerms.iterator();
            while(hsiter.hasNext()){
                res.addItem(ItemType.RESPONSE_TEXT, (String)hsiter.next());
            }
        }catch(Exception e){
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return res;
    }

    public Packet getFromAttrs1(String sSessId, String sOpsetId) {
        Packet res = new Packet();
        try {
            ArrayList<Integer> v = getFromAttrs(sOpsetId);
            for (int i = 0; i < v.size(); i++) {
                res.addItem(ItemType.RESPONSE_TEXT, v.get(i) + "");
            }
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }
        return res;
    }

    public Packet getToAttrs1(String sSessId, String sOpsetId) {
        Packet res = new Packet();
        try {
            Vector v = getToAttrsOpset(sOpsetId);
            for (int i = 0; i < v.size(); i++) {
                res.addItem(ItemType.RESPONSE_TEXT, String.valueOf(v.get(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
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

        sCmd = "add|op|" + PM_OATTR_CREATE_OATTR + "|s|" + id;
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
        return SQLPacketHandler.getSuccessPacket();//();
    }

    public Packet setTablePerms(String sSessId, String sProcId, String sBaseName,
                                String sBaseType, String sAttrName, String sAttrType,
                                String perms, String uattr, boolean bInh){
        String sCmd;
        Packet res;
        try {
            if (allIoOpers == null) {
                allIoOpers = new HashSet<String>();
                for (int i = 0; i < sDirOps.length; i++) {
                    allIoOpers.add(sDirOps[i]);
                }
                for (int i = 0; i < sFileOps.length; i++) {
                    allIoOpers.add(sFileOps[i]);
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
                        null, sEntId, sEntType, bInh);

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
        } catch (Exception e) {
            e.printStackTrace();
            failurePacket(e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();//();
    }

    public Packet deleteTemplate(String sSessId, String sTplId, String sTplName){
        if(sTplId == null || sTplId.equals("")){
            return failurePacket("deleteTemplate recevied a null template id");
        }

        try {
            if(!entityExists(sTplId, PM_TEMPLATE)){
                return failurePacket("Template " + sTplName + " does not exist");
            }

            delete(DELETE_TEMPLATE, sTplId);

            Packet res =  ServerConfig.obligationDAO.processEvent(sSessId, null, "Template delete", sTplName,sTplId, null, null, null, null);
            if(res == null){
                System.out.println("processEvent returned null");
                return failurePacket(res.getErrorMessage());
            }

            if(res.hasError()){
                return res;
            }
        } catch (Exception e) {
            return failurePacket("Could not delete object " + sTplId + ": " + e.getMessage());
        }

        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet testSynchro(String sClientId) {
        System.out.println("Starting work in testSynchro");
        for (long i = 0; i < 5000000000l; i++) ;
        return SQLPacketHandler.getSuccessPacket("TestSynchro terminated");
    }

    /**
     * Each item contains <policy name>:<policy id>
     * @param sClientId
     * @return a Packet containing all policy classes
     * @throws Exception
     */
    public Packet getPolicyClasses(String sClientId) {
        Packet res = new Packet();

        //SQL statement should return name and id
        try {
            ArrayList<ArrayList<Object>> pcs =  select(GET_POLICY_CLASSES);
            for(ArrayList<Object> pc : pcs){
                res.addItem(ItemType.RESPONSE_TEXT, pc.get(0) + PM_FIELD_DELIM + pc.get(1));
            }
        } catch (Exception e){
            return failurePacket(e.getMessage());
        }
        return res;
    }

    public Packet addPc(String sSessId, String sProcId, String sName,
                        String sDescr, String sInfo, String[] sProps) {

        // Test permissions.
        try {
            if (!requestAddPcPerms(sSessId, sProcId)) {
                return failurePacket(reqPermsMsg);
            }
            Integer pcId = addPcInternal(sName, null, sDescr, sInfo, sProps);
            if(pcId == null){
                return fail();
            }
            Packet res = new Packet();
            res.addItem(ItemType.RESPONSE_TEXT, sName + PM_FIELD_DELIM + pcId);
            return res;
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }
    }

    /**
     * Returns:
     * item 0: <name>:<id>
     * item 1: <description>
     * item 2: <other info>
     * item 3 and following: <property>
     *
     * @param sSessId
     * @param sPcId
     * @param sIsVos
     * @return a packet containin the information for the pc
     */
    public Packet getPcInfo(String sSessId, String sPcId, String sIsVos) {
        return getAttrInfo(sSessId, sPcId, PM_NODE.POL.value, sIsVos);
    }

    /**
     * If the clicked - also called base - node is:
     * - the connector node, the new attribute will be added as an ascendant
     * of the connector;
     * - a user, the new attribute will be added as a descendant of it and
     * ascendant of the connector node;
     * - a user attribute, the new attribute will be added as an ascendant of
     * it;
     * - a policy, the new attribute will be added as an ascendant of it.
     * Permissions.
     * sSessId needs the following permissions:
     * If the base node is a user:
     * "User create user attribute" on the base node
     * "Connector create user attribute" on the connector.
     * If the base node is a user attribute:
     * "User attribute create user attribute" on the base node.
     * If the base node is a policy class:
     * "Policy class create user attribute" on the base node.
     * If the base node is the connector:
     * "Connector create user attribute" on the base node.
     *
     * @param sClientId
     * @param sSessId
     * @param sProcId
     * @param sName the name of the uattr
     * @param sDescr description of the uattr
     * @param sInfo other info regarding uattr
     * @param sBaseId the ID of the base node
     * @param sBaseType type of the base node
     * @param sBaseIsVos
     * @param sProps properties for the base node
     * @return
     */

    public Packet addUattr(String sClientId, String sSessId, String sProcId,
                           String sName, String sDescr, String sInfo, String sBaseId,
                           String sBaseType, String sBaseIsVos, String[] sProps) {

        // TODO Test permissions.
        try {
            System.out.println("Calling requestAddUattrPerms");
            if (!requestAddUattrPerms(sSessId, sProcId, sBaseId, sBaseType)) {
                return failurePacket(reqPermsMsg);
            }
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }

        System.out.println("Calling addUattrInternal");
        Packet res =  new Packet();
        Integer uattrId = addUattrInternal(sName, sDescr, sInfo, sBaseId,
                sBaseType, sProps);
        try {
            res.addItem(ItemType.RESPONSE_TEXT, sName
                    + PM_FIELD_DELIM + uattrId);
        } catch (PacketException e) {
            return fail();
        }
        if (!res.hasError()) {
            setLastUpdateTimestamp();
        }
        return res;
    }

    // Add an object attribute.
    // The base node can be the connector node, an object attribute, or a
    // policy.
    // Return: failure or <name>:<id> of the new object attribute.

    public Packet addOattr(String sSessId, String sProcId, String sName,
                           String sDescr, String sInfo, String sBaseId, String sBaseType,
                           String sBaseIsVos, String sAssocObjId, String[] sProps) {

        if (!sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)
                && !sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)
                && !sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
            return failurePacket("You cannot create an object attribute in this type of node");
        }

        // Check permissions.
        System.out.println("addOattr()");
        System.out.println("sBaseId: " + sBaseId);
        System.out.println("sBaseType: " + sBaseType);
        try {
            if (!requestAddOattrPerms(sSessId, sProcId, sBaseId, sBaseType)) {
                return failurePacket(reqPermsMsg);
            }
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }

        Integer oattrId =  addOattrInternal(sName, null, sDescr, sInfo,
                sBaseId, sBaseType, sAssocObjId, sProps);
        if(oattrId == null){
            return fail();
        }
        Packet result = new Packet();
        try {
            result.addItem(ItemType.RESPONSE_TEXT, sName + PM_FIELD_DELIM + oattrId);
        } catch (PacketException e) {
            return failurePacket("failed creating reuslt packet");
        }
        return result;
    }

    public Packet assign(String sSessId, String sProcId, String sId1, String sType1, String sId2,
                         String sType2) {
        // The permissible types are:
        // user ---> user attribute
        // user attribute ---> user attribute
        // user attribute ---> policy class
        // user attribute ---> operation set
        // object attribute ---> object attribute (but not associated to object)
        // object attribute ---> policy class
        // operation set ---> object attribute.
        Packet res = new Packet();
        if (sSessId==null || sId1==null || sId2==null)  {
            return failurePacket("One or more parameters are null");
        }
        try{

            if (!requestAssignPerms(sSessId, sProcId, sId1, sType1, sId2,
                    sType2) && !isSuper(sSessId)) {
                return failurePacket(reqPermsMsg);
            }
            if(assignInternal(sId1, sType1, sId2, sType2)){
                return Packet.getSuccessPacket();
            }
        }catch(Exception e){
            e.printStackTrace();
            res = SQLPacketHandler.getFailurePacket("Could not assign " + sId1 + " and " + sId2 + ". " + e.getMessage());
        }
        return res;
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
                                   String end, String sType1, String start, String sType2,
                                   String sIsAdminVos) {

        // TODO Check permissions.
        // ...

        // Call the internal function.
        if(deleteAssignment(Integer.valueOf(end), Integer.valueOf(start))){
            return Packet.getSuccessPacket();
        }else{
            return fail();
        }
    }

    /**
     * Get user attributes (<uattr name> : <uattr id>)
     * @param sClientId
     * @return a Packet containing all user attributes in name:id pairs
     * @throws Exception
     */
    public Packet getUserAttributes(String sClientId) {
        Packet res = new Packet();

        //SQL statement should return name and id
        try {
            ArrayList<ArrayList<Object>> uattrs = select(GET_USER_ATTRS);
            for(ArrayList<Object> ua : uattrs){
                res.addItem(ItemType.RESPONSE_TEXT, ua.get(0) + PM_FIELD_DELIM + ua.get(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return res;
    }

    /**
     * Get all users. Each item is a string <user name>:<user id>
     * @param sClientId
     * @return a Packet containing all users in name:id pairs
     */
    public Packet getUsers(String sClientId) {
        Packet res = new Packet();

        //SQL statement should return name and id
        try {
            ArrayList<ArrayList<Object>> users = select(GET_USERS);
            for(ArrayList<Object> u : users){
                res.addItem(ItemType.RESPONSE_TEXT, u.get(0) + PM_FIELD_DELIM + u.get(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    /**
     * Get all the attribute sets. Each item of the rsult contains name:id of an attribute set.
     *
     * @param sClientId
     * @return
     */
    public Packet getAsets(String sClientId) {// TODO
        Packet res = new Packet();

        try {
            ArrayList<ArrayList<Object>> asets = select(GET_USER_ATTRS);
            for(ArrayList<Object> a : asets){
                res.addItem(ItemType.RESPONSE_TEXT, a.get(0) + PM_FIELD_DELIM + a.get(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    /**
     * The base node can be:
     * - null; then add the user as an ascendant of the connector node;
     * - a user attribute; add the user as an ascendant of the user attribute;
     * - the connector node; add the user as an ascendant of the connector node.
     * Parameter sBaseIsVos (values YES or NO), tells whether the base node
     * id is a VOS node id.
     *
     * @param sSessId
     * @param sProcId
     * @param sName
     * @param sFull
     * @param sInfo
     * @param sPass
     * @param sBaseId
     * @param sBaseType
     * @param sBaseIsVos
     * @return
     * @throws Exception
     */

    public Packet addUser(String sSessId, String sProcId, String sName,
                          String sFull, String sInfo, String sPass, String sBaseId,
                          String sBaseType, String sBaseIsVos) {

        System.out.println("AddUser sName = " + sName);
        System.out.println("AddUser sFull = " + sFull);
        System.out.println("AddUser sInfo = " + sInfo);
        System.out.println("AddUser sPass = " + sPass);
        System.out.println("AddUser sBaseId = " + sBaseId);
        System.out.println("AddUser sBaseType = " + sBaseType);
        System.out.println("AddUser sBaseIsVos = " + sBaseIsVos);

        if (sBaseId == null) {
            sBaseId = PM_CONNECTOR_ID;
            sBaseType =PM_NODE.CONN.value;
        }
        Packet res = new Packet();
        Packet eventRes = null;

        try {
            // Do permission checks.
            if (!requestAddUserPerms(sSessId, sProcId, sBaseId, sBaseType)) {
                return failurePacket(reqPermsMsg);
            }

            // Call addUserInternal() with a null sId to force generation of the
            // user id:
            Integer userId = addUserInternal(sName, null, sFull, sInfo, sPass,
                    sBaseId, sBaseType);
            if (userId == null) {
                return fail();
            }

            setLastUpdateTimestamp();

            res.addItem(ItemType.RESPONSE_TEXT, sName + PM_FIELD_DELIM + userId);

            // addUserInternal returns the new user's name and id in the first item.
            String sLine = res.getStringValue(0);
            String[] pieces = sLine.split(PM_FIELD_DELIM);

            System.out.println("Before event");
            eventRes = ServerConfig.obligationDAO.processEvent(sSessId, null,
                    PM_EVENT_USER_CREATE, sName, pieces[1], PM_CLASS_USER_NAME,
                    null, null, null);
            System.out.println("After event");
            if (eventRes.hasError()) {
                return eventRes;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }


        return res;
    }

    /**
     * Cannot delete object attributes associated to objects -
     * you have to delete the object.
     *
     * WITH SQL, WE MAY ONLY NEED THIS METHOD
     *
     * @param sSessId
     * @param sId ID of the node to be deleted
     * @param sType type of the node to be deleted
     * @param sIsVos TODO
     * @return a success or failure packet
     */
    public Packet deleteNode(String sSessId, String sId, String sType,
                             String sIsVos) {
        System.out.println("============================= sType = " + sType + " ========================================");

        Packet res = new Packet();
        try {
            deleteNode(Integer.valueOf(sId));
            res = Packet.getSuccessPacket();
        } catch (Exception e) {
            res = failurePacket(e.getMessage());
        }
        return res;
    }

    // Add a new object class and/or a new operation.
    // If the object class does not exist, add a new one and the operation,
    // if present in the command.
    // If the object class already exists, add a new operation (which must be
    // present in the command.

    public Packet addObjClassAndOp(String sClientId, String sClass,
                                   String sDescr, String sInfo, String sOp) {
        // Test if the class exists.
        try {
            MySQL_Parameters params = new MySQL_Parameters();

            String sClassId = getEntityId(sClass, PM_OBJ_CLASS);
            System.out.println("Class id is " + sClassId);
            if (sClassId == null) {
                // Class does not exist. Add the class and the optional
                // operation.
                params.addParam(ParamType.STRING, sClass);
                params.addParam(ParamType.STRING, sDescr);

                insert(ADD_OBJ_CLASS, params);

                if (sOp == null) {
                    System.out.println("the op is null");
                } else {
                    System.out.println("the op is NOT null and has length "
                            + sOp.length());
                }

                if (sOp != null && sOp.length() > 0) {
                    sClassId = getEntityId(sClass, PM_OBJ_CLASS);

                    params.clearParams();
                    params.addParam(ParamType.STRING, sOp);
                    params.addParam(ParamType.INT, sClassId);

                    executeStoredProcedure(CREATE_OPERATION, params);
                }
            } else {
                // Class exist, try to add the operation, which cannot be null
                // or
                // duplicate.
                if (sOp == null || sOp.length() == 0) {
                    return failurePacket("The operation name cannot be null or empty for an existing class");
                }

                if (objClassHasOp(sClassId, sOp)) {
                    return failurePacket("Duplicate operation in the same object class");
                }
                params.clearParams();
                params.addParam(ParamType.STRING, sOp);
                params.addParam(ParamType.INT, sClassId);

                executeStoredProcedure(CREATE_OPERATION, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Unable to add operation \"" + sOp
                    + "\"; " + e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
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

        if (sClass.equalsIgnoreCase(PM_CLASS_CLASS_NAME)
                || sClass.equalsIgnoreCase(PM_CLASS_FILE_NAME)
                || sClass.equalsIgnoreCase(PM_CLASS_DIR_NAME)) {
            return failurePacket("Cannot delete or modify a predefined class");
        }
        try {
            String sId = getEntityId(sClass, PM_OBJ_CLASS);
            if (sId == null) {
                return failurePacket("Unknown class " + sClass);
            }

            MySQL_Parameters params = new MySQL_Parameters();
            if (sOp == null || sOp.length() == 0) {
                // No operation was selected, delete the class.
                params.addParam(ParamType.STRING, sClass);

                delete(DELETE_OBJ_CLASS, params);
            } else {
                // Delete the operation.
                params.addParam(ParamType.STRING, sOp);

                delete(DELETE_OP, params);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    /**
     * Get all object classes. Each item of the return value contains an object class name.
     * @param sClientId
     * @return a Packet continaing the object classes
     * @throws Exception
     */
    public Packet getObjClasses(String sClientId) {
        Packet res = new Packet();
        try{
            ArrayList<ArrayList<Object>> pcs =  select(GET_OBJ_CLASSES);
            for(ArrayList<Object> pc : pcs){
                res.addItem(ItemType.RESPONSE_TEXT, (String)pc.get(0));
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Return all objects, one per item. Each item contains:
    // For File/Dir: name|id|class|NO|host|path.
    // For PM entities: name|id|class|inh|original name|original id.
    // For composites: name|id|"Composite"|NO|template id|components|keys.
    // The components have the format: comp1:...:compn, where each compi is
    // the id of the oattr associated with a component object of the composite.
    // The keys have the format: key1=value1:...:keyn=valuen.

    public Packet getObjects(String sClientId) {
        Packet res = new Packet();
        try {
            ArrayList<ArrayList<Object>> objects = select(GET_ALL_OBJECTS);
            if (objects == null || objects.size() == 0) {
                System.out.println("NO OBJECTS!");
                return failurePacket("did not find any objects");
            }
            for (ArrayList<Object> object : objects) {
                String sName = object.get(0)!=null? (String) object.get(0):null;
                String sId = object.get(1)!=null? ((Integer) object.get(1)).toString():null;
                String sClass = object.get(2)!=null? (String) object.get(2):null;
                Integer include_ascendents = (Integer)object.get(3);
                String sIncludes = (include_ascendents==0?"NO":"YES");
                String sOrigName = object.get(4)!=null? (String) object.get(4):"";
                String sOrigId = object.get(5)!=null? ((Integer) object.get(5)).toString():"";
                String sHost = object.get(6)!=null? (String) object.get(6):"";
                String sPath = object.get(7)!=null? (String) object.get(7):"";
                if (sClass.equals(PM_CLASS_FILE_NAME)
                        || sClass.equals(PM_CLASS_DIR_NAME)) {
                    res.addItem(ItemType.RESPONSE_TEXT, sName
                            + PM_ALT_FIELD_DELIM + sId
                            + PM_ALT_FIELD_DELIM + sClass
                            + PM_ALT_FIELD_DELIM + sIncludes
                            + PM_ALT_FIELD_DELIM + sHost
                            + PM_ALT_FIELD_DELIM + sPath);
                } else if (sClass.equals(PM_CLASS_USER_NAME)
                        || sClass.equals(PM_CLASS_UATTR_NAME)
                        || sClass.equals(PM_CLASS_OBJ_NAME)
                        || sClass.equals(PM_CLASS_OATTR_NAME)
                        || sClass.equals(PM_CLASS_POL_NAME)
                        || sClass.equals(PM_CLASS_CONN_NAME)
                        || sClass.equals(PM_CLASS_OPSET_NAME)) {
                    res.addItem(ItemType.RESPONSE_TEXT, sName
                            + PM_ALT_FIELD_DELIM + sId
                            + PM_ALT_FIELD_DELIM + sClass
                            + PM_ALT_FIELD_DELIM + sIncludes
                            + PM_ALT_FIELD_DELIM + sOrigName+sHost
                            + PM_ALT_FIELD_DELIM + sOrigId+sPath);
                } else if (sClass
                        .equals(PM_CLASS_CLIPBOARD_NAME)) {
                    res.addItem(ItemType.RESPONSE_TEXT, sName
                            + PM_ALT_FIELD_DELIM + sId
                            + PM_ALT_FIELD_DELIM + sClass
                            + PM_ALT_FIELD_DELIM + sIncludes
                            + PM_ALT_FIELD_DELIM + sHost);
                } else {
                    res.addItem(ItemType.RESPONSE_TEXT, sName
                            + PM_ALT_FIELD_DELIM + sId
                            + PM_ALT_FIELD_DELIM + sClass
                            + PM_ALT_FIELD_DELIM + sIncludes);
                }
            }
            return res;
        } catch (Exception e) {
            return failurePacket("Exception: " + e.getMessage());
        }
    }

    // Get PM objects of a given class (users, user attributes, etc.
    // The result is a list of name:id.
    public Packet getPmEntitiesOfClass(String sClientId, String sClass){
        Packet result = new Packet();
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sClass);
        try {
            ArrayList<ArrayList<Object>> operations = select(GET_PM_ENTITIES_OF_CLASS, params);
            for(ArrayList<Object> op : operations){
                result.addItem(ItemType.RESPONSE_TEXT, (String)op.get(0) + PM_FIELD_DELIM + (Integer)op.get(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getPmEntitiesOfClass(): " + e.getMessage());
        }
        return result;
    }

    /**
     * Create a new object. Called from createObject3() or directly from the
     * Admin Tool.
     *
     * The engine creates an object attribute associated with the object.
     * This attribute will be assigned to the connector node, unless the
     * sContainers
     * is non-null. sContainers specifies the types and ids of object
     * attributes,
     * policies, or
     * connector node, which will be used
     * as immediate descendants of the object.
     *
     * @param sSessId
     * @param sProcId
     * @param sName
     * @param sDescr
     * @param sInfo
     * @param sContainers
     * @param sClass
     * @param sType
     * @param sHost
     * @param sPath
     * @param sOrigName
     * @param sOrigId
     * @param sInh
     * @param sSender
     * @param sReceiver
     * @param sSubject
     * @param sAttached
     * @param sTplId
     * @param sComponents
     * @param sKeys
     * @return
     * @throws Exception
     */

    public Packet addObject3(String sSessId, String sProcId, String sName,
                             String sDescr, String sInfo, String sContainers, String sClass,
                             String sType, String sHost, String sPath, String sOrigName,
                             String sOrigId, String sInh, String sSender, String sReceiver,
                             String sSubject, String sAttached, String sTplId,
                             String sComponents, String[] sKeys) {

        if (sContainers == null || sContainers.length() == 0) {
            sContainers =PM_NODE.CONN.value + PM_ALT_FIELD_DELIM + PM_CONNECTOR_NAME;
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

        String[] pieces = sContainers.split(PM_LIST_MEMBER_SEP);
        Packet res;
        Integer objId = 0;

        try {
            // For each container:
            for (int i = 0; i < pieces.length; i++) {
                String[] sTypeLabel = pieces[i].split(PM_ALT_DELIM_PATTERN);
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
                    objId  =  addObjectInternal(sName, null, null, sDescr,
                            sInfo, sContId, sTypeLabel[0], sClass, sType, sHost,
                            sPath, sOrigName, sOrigId, sInh.equals(YES), sSender,
                            sReceiver, sSubject, sAttached, sTplId, sComponents,
                            sKeys);

                    if(objId == null){
                        return fail();
                    }
                } else {
                    // For the other containers, assign the oattr associated with
                    // the
                    // new object to them, if not already there.
                    //
                    if(!assignInternal(objId.toString(),PM_NODE.OATTR.value, sContId,
                            sTypeLabel[0])){
                        return fail();
                    }
                }
            }
            res = new Packet();

            res.addItem(ItemType.RESPONSE_TEXT, sName + PM_FIELD_DELIM + objId);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet!");
        }
        return res;
    }

    // Delete an object.
    // Conditions: no opset is assigned to the object
    // (i.e., to the associated object attribute).

    public Packet deleteObject(String sSessId, String sObjId) {
        // Find the associated attribute.
        // Gopi following method checks if the passed id is a true object.
        // Later on, need to change the name of the method accordingly.
        try {
            String sAssocId = getAssocOattr(sObjId);
            if (sAssocId == null) {
                return failurePacket("This is not an object");
            }
            // See whether the attribute has any opsets assigned to it.
            if (oattrHasOpsets(Integer.valueOf(sAssocId))) {
                return failurePacket("Associated attribute is assigned to operation sets");
            }

            // Delete the associated object attribute (which is an object itself).
            if(!deleteOattr(sSessId, sAssocId, false)) {
                return fail();
            }
            // Get following information to process event
            String sContainers = getContainerList(sAssocId,PM_NODE.OATTR.value);
            String sObjName = getEntityName(sAssocId, PM_NODE.OATTR.value);
            Packet res =  ServerConfig.obligationDAO.processEvent(sSessId, null, "Object delete", sObjName,
                    sObjId, null, null, sContainers, sAssocId);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet deleteObjectStrong(String sSessId, String sObjId){
        return deleteObjectStrongInternal(sSessId, sObjId) ? Packet.getSuccessPacket() : fail();
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
            String sContId = getEntityId(sContName, type);
            if (sContId == null) {
                return failurePacket("No such container: " + sContName);
            }

            // If the container is associated to an object, delete it (the
            // container,
            // the associated object, and the assignments).
            if (hasAssocObj(sContId)) {
                return deleteObjectInternal(sContId) ? Packet.getSuccessPacket() : fail();
            }

            // The container is a true container. Get the collection of directly
            // contained oattrs.

            ArrayList<Integer> objIds = getFromAttrs(sContId, PM_NODE.ASSOC.value, 1);

            if (objIds == null) {
                return SQLPacketHandler.getSuccessPacket();
            }
            if (objIds != null) {
                // For each oattr contained in the given container:
                for (Integer i : objIds) {
                    String sOattrId = String.valueOf(i);
                    // If it's not an object, continue.
                    if (!hasAssocObj(sOattrId)) {
                        continue;
                    }
                    System.out.println("Found object "
                            + getEntityName(sOattrId, PM_NODE.OATTR.value));

                    // We found an oattr which is an object. Delete it!
                    if(!deleteObjectInternal(sOattrId)){
                        return fail();
                    }
                }
            }
            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in deleteContainerObjects(): "
                    + e.getMessage());
        }
    }

    /**
     *  Get the object associated to an object attribute given by its id.
     */
    public Packet getAssocObj(String sClientId, String sOattrId) {
        String assocObjId;
        Packet res = new Packet();
        try {
            assocObjId = getAssocObj(sOattrId);
            res.addItem(ItemType.RESPONSE_TEXT, (String) assocObjId);
            return res;
        } catch (Exception e) {
            return failurePacket("Couldn't get the object: " + e.getMessage());
        }
    }

    public Packet getObjNamePath(String sObjName) {
        Packet res = new Packet();
        try {
            String sObjId = getEntityId(sObjName, PM_NODE.ASSOC.value);
            if (sObjId == null) {
                return failurePacket("No object " + sObjName + "!");
            }
            String sPath = getObjPath(sObjId);
            if (sPath == null) {
                return failurePacket("No path for object " + sObjName + "!");
            }
            res.addItem(ItemType.RESPONSE_TEXT, sPath);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    // Get all operations of an object class. The object class is identified by
    // its name!!!

    public Packet getObjClassOps(String sClientId, String sClass) {
        if(sClass == null || sClass.equals("")){
            return failurePacket("getObjClassOps received a null class name");
        }
        Packet result = new Packet();
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sClass);
        try {
            ArrayList<ArrayList<Object>> operations = select(GET_OP_NAMES, params);
            for(ArrayList<Object> op : operations){
                result.addItem(ItemType.RESPONSE_TEXT, (String)op.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getObjClassOps(): " + e.getMessage());
        }
        return result;
    }

    /**
     * Get all hosts. Each item has the format: <host name>:<host id>.
     *
     * @param sClientId
     * @return a Packet containing the <host name>:<host id> of all hosts
     * @throws Exception
     */
    public Packet getHosts(String sClientId) {
        Packet res = new Packet();

        try{
            ArrayList<ArrayList<Object>> hosts = select(GET_HOSTS);
            for(ArrayList<Object> host : hosts){
                res.addItem(ItemType.RESPONSE_TEXT, host.get(1) + PM_FIELD_DELIM + host.get(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    //    public Packet addHost(String sSessId, String sHost, String sRepo,
//            String sReserved, String sIpa, String sDescr, String sPdc) {
    public Packet addHost(String sSessId, String sHost, String sRepo) {
        ArrayList<ArrayList<Object>> returned = null;
        Integer newHostId = null;
        Packet res = new Packet();
        MySQL_Parameters params = new MySQL_Parameters();
        if (sSessId == null) {
            return  failurePacket("Session ID is null");
        }
        try {
            // Test whether the attributes are valid.
            if (sHost == null || sHost.length() == 0) {
                return failurePacket("Null host name!");
            }
            // check if duplicate name.
            if (hostNameExists(sHost)) {
                return failurePacket("Duplicate host name!");
            }
            if (!UtilMethods.hostNameIsValid(sHost)) {
                return failurePacket("Invalid host name!");
            }
            if (sRepo == null || sRepo.length() == 0) {
                return failurePacket("Null repository path!");
            }
			/*if (sPdc == null || sPdc.equalsIgnoreCase("false")) {
				sPdc = "0";
			} else {
				if (sPdc.equalsIgnoreCase("true")) {
					sPdc = "1";
				}
    		}*/
            String userId = getSessionUserIdInternal(sSessId);
            if (userId == null) {
                return failurePacket("No user for the session");
            }
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, sRepo);

            returned = executeStoredProcedure(CREATE_HOST, params);
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            newHostId = getHostId(sHost);
            res.addItem(ItemType.RESPONSE_TEXT, sHost + PM_FIELD_DELIM + newHostId);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when adding host: " + e.getMessage());
        }
        return res;
    }

    private boolean hostNameExists(String name){
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, name);
        try {
            return (Long) select(GET_HOST_ID, params).get(0).get(0) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * update a host
     * @param sSessId
     * @param sHostId the ID of the host to update
     * @param sHost the name of the Host
     * @param sRepo the workarea_path
     * @return a Packet containing the name and ID of the host
     */
//    public Packet updateHost(String sSessId, String sHostId, String sHost,
//            String sRepo, String sIpa, String sDescr, String sPdc) {
    public Packet updateHost(String sSessId, String sHostId, String sHost,
                             String sRepo) {
        // Check permissions...

        // Test that the host id exists.
        try{
            String s = getEntityName(sHostId, PM_HOST);
            if (s == null) {
                return failurePacket("No host with id " + sHostId);
            }

            Packet res = new Packet();

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, sRepo);
            params.addParam(ParamType.INT, sHostId);

            update(UPDATE_HOST, params);

            res.addItem(ItemType.RESPONSE_TEXT, sHost + PM_FIELD_DELIM + sHostId);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when updating the host: " + e.getMessage());
        }
    }

    /**
     * delete the host with the given ID
     * @param sSessId
     * @param sHostId the ID of the host to delete
     * @return a success or failure Packet
     */
    public Packet deleteHost(String sSessId, String sHostId) {
        MySQL_Parameters params = new MySQL_Parameters();
        if (sHostId==null || sHostId.isEmpty()) {
            return failurePacket("Null Host Id");
        }
        params.addParam(ParamType.INT, sHostId);
        try {
            delete(DELETE_HOST, params);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Could not delete host with id " + sHostId);
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public void deleteHostApp(String appName, String sHost) {
        MySQL_Parameters params = new MySQL_Parameters();
        try {
            params.addParam(ParamType.STRING, appName);
            params.addParam(ParamType.INT, getHostId(sHost));
            delete(DELETE_APP, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the host info:
     * item 1: name
     * item 2: is domain controller
     * item 3: workarea_path
     * @param sSessId current session ID
     * @param sHostId the ID of the host
     * @return a Packet containg the host info
     */
    public Packet getHostInfo(String sSessId, String sHostId) {
        Packet res = new Packet();

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sHostId);
        try {
            ArrayList<ArrayList<Object>> results = select(GET_HOST_INFO, params);
            ArrayList<Object> host = results.get(0);
            String hostName = (String) host.get(0);
            String path = (String) host.get(1);

            res.addItem(ItemType.RESPONSE_TEXT, hostName);
            res.addItem(ItemType.RESPONSE_TEXT, path);
            //res.addItem(ItemType.RESPONSE_TEXT, ""); TODO might need this because client is expecting 3 args in result
        } catch (Exception e) {
            e.printStackTrace();
            res = failurePacket("Exception in getHostInfo(): "
                    + e.getMessage());
        }
        return res;
    }

    /**
     * Get all operation sets. Each item of the return contains
     * <operation set name>:<operation set id>.
     * @param sClientId
     * @return a Packet containing all opsets
     * @throws Exception
     */
    public Packet getOpsets(String sClientId) {
        Packet res = new Packet();
        try{
            ArrayList<ArrayList<Object>> pcs =  select(GET_OPSETS);
            for(ArrayList<Object> pc : pcs){
                res.addItem(ItemType.RESPONSE_TEXT, pc.get(0) + PM_FIELD_DELIM + pc.get(1));
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    /**
     * Add a new op set and/or a new operation.
     * If the op set does not exist, add a new one and the operation (if the op
     * is present in the command).
     * If the op set already exists, add a new operation (which must be
     * present in the command).
     * The last two arguments are the id and type of a graph node where the
     * new opset must be assigned (it can be the connector, a user attribute, or
     * an object attribute - including oattr associated to objects).
     * The base id and type can be null; then we will add the opset to the
     * connector node.
     * Note that the base node is used only when the opset is
     * created (and not when we add only an operation).
     *
     * @param sSessId current session ID
     * @param sOpset name of the opset to be added
     * @param sDescr the description of the
     * @param sInfo the information about the opset
     * @param sOp the operation to add to the opset
     * @param sBaseId the ID of the node which the opset will be assigned to
     * @param sBaseType the type of the node which the opset will be assigned to
     * @return
     */
    public Packet addOpsetAndOp(String sSessId, String sOpset, String sDescr,
                                String sInfo, String sOp, String sBaseId, String sBaseType) {
        System.out.println("addOpsetAndOp(name=" + sOpset + ", descr=" + sDescr
                + ", info=" + sInfo + ", op=" + sOp + ", baseid=" + sBaseId
                + ", basetype=" + sBaseType + ")");

        if (sBaseId == null) {
            sBaseId = PM_CONNECTOR_ID;
            sBaseType =PM_NODE.CONN.value;
        }

        if (sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            sBaseType =PM_NODE.OATTR.value;
        }
        try {
            // Check permissions.
            // If the opset does not exist, check permission to create it.
            // Otherwise, check permission to add an operation to the opset.
            String sId = getEntityId(sOpset, PM_NODE.OPSET.value);
            if (sId == null) {

                if (!requestAddOpsetPerms(sSessId, null, sBaseId, sBaseType)) {
                    return failurePacket(reqPermsMsg);
                }

            }
            System.out.println("Permitted to add OpSet");

            // Test the base node type.
            if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)
                    || sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
                Integer opsetId = addOpsetAndOpInternal(sOpset, null, sDescr, sInfo, sOp,
                        null, null, sBaseId, sBaseType);
                if(opsetId == null){
                    return fail();
                }

                try{
                    insert(ADD_NODE_OPSET, Integer.valueOf(sBaseId), opsetId);
                }catch(Exception e){
                }

                Packet res = new Packet();
                res.addItem(ItemType.RESPONSE_TEXT, sOpset
                        + PM_FIELD_DELIM + opsetId);
                return res;
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                Integer opsetId = addOpsetAndOpInternal(sOpset, null, sDescr, sInfo, sOp,
                        sBaseId, sBaseType, null, null);
                if(opsetId == null){
                    return fail();
                }
                Packet res = new Packet();
                res.addItem(ItemType.RESPONSE_TEXT, sOpset
                        + PM_FIELD_DELIM + opsetId);
                return res;
            } else {
                return failurePacket("Cannot add/assign an operation set to this type of node");
            }
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }
    }

    // Get all operations of an operation set. The operation set is identified
    // by
    // its name!!!
    public Packet getOpsetOps(String sClientId, String sOpset) {
        Packet res = new Packet();
        try {
            String sOpsetId = getEntityId(sOpset, PM_NODE.OPSET.value);
            if (sOpsetId == null || sOpsetId.length() == 0) {
                return failurePacket("Specified OpSet does not exist");
            }

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sOpsetId);

            ArrayList<String> ops = extractStrings(select(GET_OPSET_OPS, params));
            for (int i = 0; i < ops.size(); i++) {
                res.addItem(ItemType.RESPONSE_TEXT, ops.get(i));
            }
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }

        return res;
    }

    /**
     * Get all info about an opset: name, id, descr, ops
     * @param sClientId
     * @param sId the ID of the opset
     * @return a Packet containing name, id, descr, info, class name, ops
     */
    public Packet getOpsetInfo(String sClientId, String sId) {
        Packet res = new Packet();

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId);

        //name, description, ops
        try{
            ArrayList<String> opsetInfo = getNodeInfo(sId);
            res.addItem(ItemType.RESPONSE_TEXT, opsetInfo.get(0));
            res.addItem(ItemType.RESPONSE_TEXT, opsetInfo.get(1));
            res.addItem(ItemType.RESPONSE_TEXT, opsetInfo.get(2));
            res.addItem(ItemType.RESPONSE_TEXT, "");
            res.addItem(ItemType.RESPONSE_TEXT, PM_CLASS_OPSET_NAME);//TODO not sure what class is for operation sets

            ArrayList<String> ops = extractStrings(select(GET_OPSET_OPS, params));
            for(int i = 0; i < ops.size(); i++){
                res.addItem(ItemType.RESPONSE_TEXT, ops.get(i));
            }
        }catch(Exception e){
            return failurePacket(e.getMessage());
        }
        return res;
    }

    // The op set id cannot be null or empty.
    // The op set must exist.
    // If the operation is present, delete the operation from the opset.
    // Otherwise delete the opset.

    public Packet deleteOpsetAndOp(String sClientId, String sId, String sOp) {
        if (sId == null || sId.length() == 0) {
            return failurePacket("The operation set id cannot be null or empty");
        }
        try {
            String sName = getEntityName(sId, PM_NODE.OPSET.value);
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
                if(!deleteAssignment(Integer.valueOf(sId), Integer.valueOf(PM_CONNECTOR_ID))){
                    return fail();
                }
                deleteNode(Integer.valueOf(sId));
            } else {

                // An operation of the opset is also selected. Delete the
                // operation
                // only.
                MySQL_Parameters params = new MySQL_Parameters();
                params.addParam(ParamType.INT, sId);
                params.addParam(ParamType.STRING, getOperationId(sOp));

                delete(DELETE_OPSET_OP, params);

                updatePmGraphNode(sId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    /**
     * @uml.property  name="nextSessionNumber"
     */

    // Gopi - need to be static? accessed by obligationDAO as well.
    int nextSessionNumber = 1;
    public Packet createSession(String sClientId, String sName, String sHost,
                                String sUser, String sPass) {
        // An empty password arrives here as null.
        int i = 0;
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
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sHost);
        ArrayList<ArrayList<Object>> returned = null;
        try {
            returned = select(GET_HOST_ID, params);
            if(returned.size()== 0)
                return failurePacket("Failed getting host id");
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        Integer hostId = (Integer)returned.get(0).get(0);
        if (hostId == null) {
            //NDK Added
            sResult = false;
            if(ServerConfig.auditDebug){
                try {
                    Audit.setAuditInfo("", sHost, sUser, null, sAction, sResult, sDesc, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //addition end - NDK
            return failurePacket("No such host name!");
        }

        String sUserId = null;
        String sStoredHash = null;
        try {
            sUserId = getEntityId(sUser, PM_NODE.USER.value);
            sStoredHash = getUserPass(sUserId);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        if (sUserId == null) {
            //NDK addition
            sResult = false;
            if(ServerConfig.auditDebug){
                try {
                    Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //end of addition - NDK
            return failurePacket("Incorrect user name or password!");
        }

        // Get the hash from database, then the salt (first 24 hex digits = 12
        // bytes.
        if (sStoredHash == null) {
            //NDK addition
            sResult = false;
            if(ServerConfig.auditDebug){
                try {
                    Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //end of addition - NDK
            return failurePacket("Internal error - user has no password hash!");
        }
        // Gopi - Change the algorithm for encryption
        //		String sSalt = sStoredHash.substring(0, 24);
        //
        //		// Convert the salt from 24 hex digits to 12 bytes.
        //		byte[] salt = UtilMethods.hexString2ByteArray(sSalt);
        //
        //		// Get a message digest instance and hash the salt and the password.
        //		byte[] digest;
        //		try {
        //			MessageDigest md = MessageDigest.getInstance("MD5");
        //			md.update(salt);
        //			md.update(sPass.getBytes());
        //			sPass = null;
        //			digest = md.digest();
        //		} catch (Exception e) {
        //			e.printStackTrace();
        //			//NDK Addition
        //			sResult = false;
        //			if(ServerConfig.auditDebug){
        //				Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
        //			}
        //			//end of addition - NDK
        //			return failurePacket("Internal error while hashing the password");
        //		}
        //		String sComputedHash = sSalt + UtilMethods.byteArray2HexString(digest);
        if (!sStoredHash.equalsIgnoreCase(sStoredHash)) {
            //NDK addition
            sResult = false;
            if(ServerConfig.auditDebug){
                try {
                    Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //end of addition - NDK
            return failurePacket("Incorrect user name or password!");
        }
        try {

            if (!checkPasswordHash(sStoredHash, sPass)) {
                return failurePacket("Incorrect Password");
            }

            // Get a name for session.
            sName = sUser + "@" + sHost + "-" + nextSessionNumber++;

            params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sUserId);
            params.addParam(ParamType.STRING, sName);
            params.addParam(ParamType.INT, hostId);

            // Prepare the path and create the new session object.
            insert(CREATE_SESSION, params);
        } catch (Exception e) {
            e.printStackTrace();
            //NDK addition
            sResult = false;
            if(ServerConfig.auditDebug){
                try {
                    Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            //end of addition - NDK
            return failurePacket("Could not create the session object");
        }

        // Return the name and id of the new session to PmSimul.
        Packet res = new Packet();
        int sess_id = 0;
        try {
            sess_id = Integer.valueOf(getEntityId(sName, PM_SESSION));
            res.addItem(ItemType.RESPONSE_TEXT, sName);
            res.addItem(ItemType.RESPONSE_TEXT, String.valueOf(sess_id));
            res.addItem(ItemType.RESPONSE_TEXT, sUserId);
        } catch (Exception e) {
            //NDK
            sResult = false;
            if(ServerConfig.auditDebug){
                try {
                    Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            //end  - NDK
            return failurePacket("Exception when building the result packet");
        }
        //added by NDK this calls my method that sets the audit data

        sResult = true;
        if(ServerConfig.auditDebug){
            try {
                Audit.setAuditInfo("", sHost, sUser, sUserId, sAction, sResult, sDesc, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //end of addition - NDK

        /*List<Integer> allNodes = getAllAccessibleNodes(Integer.valueOf(sUserId));
        allNodes.add(1);
        ArrayList<Integer> pols = getPolicies();
        for(Integer p : pols){
            allNodes.add(p);
        }
        PmGraphManager mgr = new PmGraphManager(new PmGraph());

        for(Integer id : allNodes){
            PmGraphNode node = graphMgr.getGraph().getNode(id);
            mgr.getGraph().addNode(id, node);
        }

        Session s = new Session(sess_id, mgr.getGraph());
        sessions.put(sess_id, s);*/


        return res;
    }
    //private Hashtable<Integer, Session> sessions = new Hashtable<Integer, Session>();

    public Packet changePassword(String sClientId, String sUser,
                                 String sOldPass, String sNewPass, String sConPass) {
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

        String sUserId = null;
        String sStoredHash = null;
        try {
            sUserId = getEntityId(sUser, PM_NODE.USER.value);
            sStoredHash = getUserPass(sUserId);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        if (sUserId == null) {
            return failurePacket("Incorrect user name or password!");
        }

        // Get the hash from database, then the salt (first 24 hex digits = 12
        // bytes.
        if (sStoredHash == null) {
            return failurePacket("Internal error - user has no password hash");
        }
//		String sSalt = sStoredHash.substring(0, 24);
//
//		// Convert the salt from 24 hex digits to 12 bytes.
//		byte[] salt = UtilMethods.hexString2ByteArray(sSalt);
//
//		// Get a message digest instance and hash the salt and the password.
//		byte[] digest;
//		try {
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			md.update(salt);
//			md.update(sOldPass.getBytes());
//			sOldPass = null;
//			digest = md.digest();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return failurePacket("Internal error while hashing the password!");
//		}
//		String sComputedHash = sSalt + UtilMethods.byteArray2HexString(digest);
//		if (!sComputedHash.equalsIgnoreCase(sStoredHash)) {
//			return failurePacket("Incorrect user name or password!");
//		}
//
//		// Get a random 12-byte salt.
//		SecureRandom random = new SecureRandom();
//		salt = new byte[12];
//		random.nextBytes(salt);
//
//		// Digest the salt and the new password.
//		try {
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			md.update(salt);
//			md.update(sNewPass.getBytes());
//			sNewPass = null; // but they're not collected probably
//			digest = md.digest();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return failurePacket("Error while hashing the password");
//		}
//
//		// Convert the hash to a string of hex digits.
//		sComputedHash = UtilMethods.byteArray2HexString(salt) + UtilMethods.byteArray2HexString(digest);

        try {
            if(!checkPasswordHash(sStoredHash, sOldPass)){
                return failurePacket("Incorrect user name or password!");
            }
            String sComputedHash = generatePasswordHash(sNewPass);

            // Store the new hash as attribute "pmPassword" of the user.

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sComputedHash);
            params.addParam(ParamType.STRING, sUser);
            update(CHANGE_PASSWORD, params);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Failed to change password: " + e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet deleteSession(String sClientId, String sSessId) {
        if(sSessId == null || sSessId.equals("")){
            return failurePacket("deleteSession received a null session id");
        }
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sSessId);
        try {
            delete(DELETE_SESSION, params);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Could not delete session " + sSessId);
        }

        return SQLPacketHandler.getSuccessPacket();
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
    public Packet newGetPermittedOps(String sCrtSessId, String sUserId, String sCrtProcId, String sObjName){
        String sObjId = null;
        try {
            sObjId = getEntityId(sObjName, PM_NODE.ASSOC.value);
        } catch (Exception e) {
            return failurePacket("Can't ifnd id for entity " + sObjName);
        }

        Packet res = getObjInfo(sObjId);
        if (res.hasError()) {
            errorMessage = res.getErrorMessage();
            return null;
        }

        Iterator iter = newGetPermittedOpsInternal(sCrtSessId, sUserId, sCrtProcId, sObjName).iterator();
        while (iter.hasNext()) {
            try {
                res.addItem(ItemType.RESPONSE_TEXT, (String)iter.next());
            } catch (PacketException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public HashSet<String> newGetPermittedOpsInternal(String sCrtSessId, String sUserId, String sCrtProcId, String sObjName) {
        System.out.println("newGetPermittedOps called with arguments:");
        System.out.println("    session id: " + sCrtSessId);
        System.out.println("    process id: " + sCrtProcId);

        try {
            if (sUserId == null || sUserId.length() == 0) sUserId = getSessionUserIdInternal(sCrtSessId);
            String sUserName = graphMgr.getGraph().getNode(sUserId).getName();
            System.out.println("    user id: " + sUserId);
            System.out.println("    user name: " + sUserName);

            HashSet<String> deniedPerms;
            HashSet<String> grantedPerms;

            //Packet res = new Packet();

            String sObjId = getEntityId(sObjName, PM_NODE.ASSOC.value);
            System.out.println("    object id: " + sObjId);
            System.out.println("    object name: " + sObjName);
            if (sObjId == null) {
                errorMessage = "getPermittedOps: No object of name " + sObjName;
                return null;
            }
            // Get the information about the object (item 0 of the return packet).
            /*res = getObjInfo(sObjId);
            if (res.hasError()) {
                errorMessage = res.getErrorMessage();
                return null;
            }*/
            grantedPerms = calcPrivInternal(sUserId, sObjId);
            if (grantedPerms == null) {
                errorMessage = "CalcPrivInternal returned a null value!";
                return null;
            }
            inMemPrintSet(grantedPerms, sUserName + " granted permissions on " + sObjName, false);

            deniedPerms = getDeniedPerms(sCrtSessId, sUserId, sCrtProcId, sObjId,
                    PM_NODE.OATTR.value);
            inMemPrintSet(deniedPerms, "Denied permissions on " + sObjName, false);
            grantedPerms.removeAll(deniedPerms);

            /*// Add the permissions to the resulting packet.
            Iterator iter = grantedPerms.iterator();
            while (iter.hasNext()) {
                res.addItem(ItemType.RESPONSE_TEXT, (String)iter.next());
            }*/
            return grantedPerms;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Exception while computing permissions: " + e.getMessage();
            return null;
        }
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
        //System.out.println("GPO> GETPERMITTEDOPS-----------------{");
        //System.out.println("GPO> sObjName: " + sObjName);
        //System.out.println("GPO> sCrtSessId in getPermittedOps = " + sCrtSessId);
        HashSet<String> deniedPerms;
        HashSet<String> grantedPerms;
        Packet res = new Packet();
        try {
            String sObjId = getEntityId(sObjName, PM_NODE.ASSOC.value);//PM_OBJ);
            if (sObjId == null) {
                return failurePacket("No object of name " + sObjName);
            }
            //System.out.println("GPO> sObjId: " + sObjId);
            res = getObjInfo(sObjId);
            if (res.hasError()) {
                return res;
            }

            // Find the id of the associated object attribute, then the
            // permissions.
            String sOattrId = getEntityId(sObjName, PM_NODE.OATTR.value);
            if (sOattrId == null) {
                return failurePacket("The object " + sObjName
                        + " has no associated attribute!");
            }
            //System.out.println("GPO> sOattrId: " + sOattrId);
            grantedPerms = getPermittedOpsInternal(sCrtSessId, sOattrId);
            if (grantedPerms == null) {
                return failurePacket("The engine returned a null set of granted permissions!");
            }

            printSet(grantedPerms, PM_OBJ,
                    "Set of grantedPerms "
                            + getEntityName(sObjId, PM_NODE.ASSOC.value));
            deniedPerms = getDeniedPerms(sCrtSessId, null, sCrtProcId, sOattrId,
                    PM_NODE.OATTR.value);
            grantedPerms.removeAll(deniedPerms);

            // Add the permissions to the resulting packet.
            Iterator<String> iter = grantedPerms.iterator();
            while (iter.hasNext()) {
                res.addItem(ItemType.RESPONSE_TEXT, iter.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while computing permissions: " + e.getMessage());
        }


        return res;
    }

    // Return some object properties, like if it's an email message, sent by
    // whom,
    // when, subject.

    public Packet getObjEmailProps(String sCrtSessId, String sObjName) {
        // sObjName must be the name of an object.
        try {
            String sObjId = getEntityId(sObjName, PM_OBJ);
            if (sObjId == null) {
                return failurePacket("Selected entity, " + sObjName
                        + ", is not an object!");
            }

            Packet p = getObjInfo(sObjName);
            String sClass = p.getStringValue(2);

            if (!sClass.equals(PM_CLASS_FILE_NAME)) {
                return failurePacket("Selected object is not an email message!");
            }
            String sPath = p.getStringValue(p.size()-1);
            if(sPath == null){
                return failurePacket("No path for object.");
            }else if (!sPath.endsWith(".eml")) {
                return failurePacket("Selected object is not an email message!");
            }

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sObjId);

            ArrayList<Object> emailProps = select(GET_EMAIL_DETAIL, params).get(0);
            String sSender = (String)emailProps.get(0);
            if (sSender == null) {
                return failurePacket("No email properties set!");
            }

            Packet res = new Packet();
            res.addItem(ItemType.RESPONSE_TEXT, (String) emailProps.get(0));
            res.addItem(ItemType.RESPONSE_TEXT,
                    (String) emailProps.get(1));
            res.addItem(ItemType.RESPONSE_TEXT,
                    (String) emailProps.get(2));
            res.addItem(ItemType.RESPONSE_TEXT,
                    (String) emailProps.get(3));

            ArrayList<Integer> attachments = extractIntegers(select(GET_ATTACHMENTS, params));
            if (attachments != null && attachments.size() > 0) {
                for (Integer i : attachments) {
                    res.addItem(ItemType.RESPONSE_TEXT, getEntityName(String.valueOf(i), PM_NODE.OATTR.value));
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
        try {
            // The selected node must be a record or an object. In more details,
            // it must be an object attribute that has an associated object
            // (PM_NODE_ASSOC),
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
                if(!isRecord(sNodeId))
                    return failurePacket("The selected node is not an object or a record!");

                String sTemplateId = getRecordTemplate(sNodeId);
                String sTemplateName = getEntityName(sTemplateId,
                        PM_TEMPLATE);
                if (sTemplateName == null)
                    return failurePacket("Inconsistency: no template with id = "
                            + sTemplateId);
                String sName = getEntityName(sNodeId, PM_NODE.OATTR.value);
                Packet res = new Packet();
                res.addItem(ItemType.RESPONSE_TEXT, sName
                        + PM_ALT_FIELD_DELIM + sNodeId
                        + PM_ALT_FIELD_DELIM
                        + PM_CLASS_RECORD_NAME
                        + PM_ALT_FIELD_DELIM + NO
                        + PM_ALT_FIELD_DELIM + sTemplateName);
                return res;

            } else {
                return failurePacket("The selected node is not an object nor a record!");
            }
        } catch (Exception e) {
            if (ServerConfig.debugFlag)
                e.printStackTrace();
            return failurePacket("Error while getting POS node properties: "
                    + e.getMessage());
        }
    }

    // Get a list of all sessions. Each entry in the list contains:
    // name:id.
    // As the last step, this function deletes the session create and delte
    // events
    // from the event container.

    public Packet getSessions(String sClientId) {
        Packet result = new Packet();

        // Delete all session events.
        //emptyContainer(sEventContainerDN, null);

        try {
            ArrayList<ArrayList<Object>> sessions = select(GET_SESSIONS);
            for(ArrayList<Object> sess : sessions){
                result.addItem(ItemType.RESPONSE_TEXT, sess.get(0) + PM_FIELD_DELIM +  sess.get(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return result;
    }

    /**The information returned by getSessionInfo has the following format:
     * item 0: <sess name>:<sess id>
     * item 1: <user name>:<user id>
     * item 2: <host name>:<host id>
     * items 3 through 3 + active_attr_count - 1: <attr name>:<attr id>
     *
     * @param sClientId
     * @param sSessId
     * @return
     */
    public Packet getSessionInfo(String sClientId, String sSessId) {
        Packet result = new Packet();
        if (sSessId==null || sSessId.isEmpty()) {
            return failurePacket("Null session Id.");
        }
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sSessId);
            ArrayList<Object> returned = select(GET_SESSION_INFO, params).get(0);
            String sessionName = (String)returned.get(0);
            Integer sessionId = (Integer)returned.get(1);
            Integer userId = (Integer)returned.get(2);
            String userName = (String)returned.get(3);
            Integer hostId = (Integer)returned.get(4);
            String hostname = (String)returned.get(5);
            result.addItem(ItemType.RESPONSE_TEXT, sessionName + PM_FIELD_DELIM + sessionId);
            result.addItem(ItemType.RESPONSE_TEXT, userName + PM_FIELD_DELIM + userId);
            result.addItem(ItemType.RESPONSE_TEXT, hostname + PM_FIELD_DELIM + hostId);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception during getSessionInfo: " + e.getMessage());
        }
    }

    public Packet getSessionUser(String sSessId) {
        if(sSessId == null || sSessId.equals("")){
            return failurePacket("getSessionUser received a null session id");
        }
        Packet res = new Packet();
        try {
            String sUserId = getSessionUserIdInternal(sSessId);
            if (sUserId == null) {
                return failurePacket("Couldn't find session or its user id");
            }
            String sUser = getEntityName(sUserId,PM_NODE.USER.value);
            if (sUser == null) {
                return failurePacket("Couldn't find session user");
            }
            res.addItem(ItemType.RESPONSE_TEXT, sUser);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet");
        }
    }

    public Packet getSessionName(String sSessId) {
        if (sSessId==null) {
            return failurePacket("null Session Id");
        }
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sSessId);

        try {
            ArrayList<String> strs = extractStrings(select(GET_SESSION_NAME, params));
            String sName = (strs == null || strs.isEmpty()) ? null : strs.get(0);
            if(sName == null){
                return failurePacket("session name was null");
            }
            if (sName==null || sName.isEmpty()) {
                return failurePacket("session does not exist.");
            }
            Packet result = new Packet();

            result.addItem(ItemType.RESPONSE_TEXT, sName);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getSessionName: "
                    + e.getMessage());
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

    public Packet getUserDescendants(String sClientId, String sUserId) {
        try {
            return SQLPacketHandler.setToPacket(getUserDescendantsInternal(sUserId));
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

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
                if (sClass.equals(PM_CLASS_FILE_NAME)
                        || sClass.equals(PM_CLASS_DIR_NAME)
                        || sClass.equals(PM_CLASS_USER_NAME)
                        || sClass.equals(PM_CLASS_UATTR_NAME)
                        || sClass.equals(PM_CLASS_OBJ_NAME)
                        || sClass.equals(PM_CLASS_OATTR_NAME)
                        || sClass.equals(PM_CLASS_POL_NAME)
                        || sClass.equals(PM_CLASS_CONN_NAME)
                        || sClass.equals(PM_CLASS_OPSET_NAME)
                        || sClass.equals(PM_CLASS_CLASS_NAME)
                        || sClass.equals(PM_CLASS_ANY_NAME)) {
                    continue;
                }

                result.addItem(ItemType.RESPONSE_TEXT, "add"
                        + PM_ALT_FIELD_DELIM + PM_OBJ_CLASS
                        + PM_ALT_FIELD_DELIM + sClass);
                ar2 =  getObjClassOps(sClientId, sClass);
                for (int j = 0; j < ar2.size() - 1; j++) {
                    String sOp = ar2.getStringValue(j);
                    result.addItem(ItemType.RESPONSE_TEXT, "add"
                            + PM_ALT_FIELD_DELIM + PM_OP + PM_ALT_FIELD_DELIM
                            + sOp + PM_ALT_FIELD_DELIM + PM_OBJ_CLASS
                            + PM_ALT_FIELD_DELIM + sClass);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getObjClasses or getObjClassOps: "
                    + e.getMessage());
        }

        // Export the main PM graph.

        // Start by inserting the connector in the queue.
        crtQe = new QueueElement(PM_NODE.CONN.value, PM_CONNECTOR_ID, 0);
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
                    ArrayList<Integer> attrs = getFromPolicies(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
                            String sName = getEntityName(sId,PM_NODE.POL.value);

                            // Don't list the "admin" policy class, but insert
                            // it into the queue.
                            if (!visitedSet.contains(sId)) {
                                qe = new QueueElement(PM_NODE.POL.value, sId, 0);
                                queue.add(qe);
                                if (sName.equals(PM_ADMIN_NAME)) {
                                    continue;
                                }
                                exportPc(result, sId);
                                visitedSet.add(sId);
                            } else {
                                result.addItem(ItemType.RESPONSE_TEXT, "asg"
                                        + PM_ALT_FIELD_DELIM +PM_NODE.POL.value
                                        + PM_ALT_FIELD_DELIM + sName
                                        + PM_ALT_FIELD_DELIM +PM_NODE.CONN.value
                                        + PM_ALT_FIELD_DELIM
                                        + PM_CONNECTOR_NAME);
                            }
                        }
                    }

                    // Process the user attributes assigned to the connector
                    // node.
                    attrs = getFromUserAttrs(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
                            if (!visitedSet.contains(sId)) {
                                qe = new QueueElement(PM_NODE.UATTR.value, sId, 0);
                                queue.add(qe);
                                exportUattr(result, sId,PM_NODE.CONN.value,
                                        PM_CONNECTOR_NAME);
                                visitedSet.add(sId);
                            } else {
                                result.addItem(
                                        ItemType.RESPONSE_TEXT,
                                        "asg"
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.UATTR.value
                                                + PM_ALT_FIELD_DELIM
                                                + getEntityName(sId,
                                                PM_NODE.UATTR.value)
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.CONN.value
                                                + PM_ALT_FIELD_DELIM
                                                + PM_CONNECTOR_NAME);
                            }
                        }
                    }

                    // Process the users assigned to the connector node.
                    attrs = getFromUsers(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
                            if (!visitedSet.contains(sId)) {
                                qe = new QueueElement(PM_NODE.USER.value, sId, 0);
                                queue.add(qe);
                                exportUser(result, sId,PM_NODE.CONN.value,
                                        PM_CONNECTOR_NAME);
                                visitedSet.add(sId);
                            } else {
                                result.addItem(
                                        ItemType.RESPONSE_TEXT,
                                        "asg"
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.USER.value
                                                + PM_ALT_FIELD_DELIM
                                                + getEntityName(sId,
                                                PM_NODE.USER.value)
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.CONN.value
                                                + PM_ALT_FIELD_DELIM
                                                + PM_CONNECTOR_NAME);
                            }
                        }
                    }

                    // Process the object attributes assigned to the connector
                    // node.
                    attrs = getFromObjAttrs(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
                            if (!visitedSet.contains(sId)) {
                                qe = new QueueElement(PM_NODE.OATTR.value, sId, 0);
                                queue.add(qe);
                                exportOattr(result, sId,PM_NODE.CONN.value,
                                        PM_CONNECTOR_NAME);
                                visitedSet.add(sId);
                            } else {
                                result.addItem(
                                        ItemType.RESPONSE_TEXT,
                                        "asg"
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.OATTR.value
                                                + PM_ALT_FIELD_DELIM
                                                + getEntityName(sId,
                                                PM_NODE.OATTR.value)
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.CONN.value
                                                + PM_ALT_FIELD_DELIM
                                                + PM_CONNECTOR_NAME);
                            }
                        }
                    }

                    // Process the operation sets assigned to the connector
                    // node.
                    attrs = getFromOpsets(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
                            String sName = getEntityName(sId,PM_NODE.OPSET.value);

                            if (!visitedSet.contains(sId)) {
                                qe = new QueueElement(PM_NODE.OPSET.value, sId, 0);
                                queue.add(qe);

                                // Get its class.
                                String sClass = "Ignored";
                                result.addItem(ItemType.RESPONSE_TEXT, "add"
                                        + PM_ALT_FIELD_DELIM +PM_NODE.OPSET.value
                                        + PM_ALT_FIELD_DELIM + sName
                                        + PM_ALT_FIELD_DELIM + PM_OBJ_CLASS
                                        + PM_ALT_FIELD_DELIM + sClass
                                        + PM_ALT_FIELD_DELIM +PM_NODE.CONN.value
                                        + PM_ALT_FIELD_DELIM
                                        + PM_CONNECTOR_NAME);
                                visitedSet.add(sId);

                                // Get its operations.
                                ar =  getOpsetOps(sClientId, sName);
                                if (ar == null) {
                                    return failurePacket("Null result from getOpsetOps");
                                }
                                for (int i = 0; i < ar.size(); i++) {
                                    String sOp = ar.getStringValue(i);
                                    result.addItem(ItemType.RESPONSE_TEXT,
                                            "add" + PM_ALT_FIELD_DELIM + PM_OP
                                                    + PM_ALT_FIELD_DELIM + sOp
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OPSET.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName);
                                }
                            } else {
                                result.addItem(ItemType.RESPONSE_TEXT, "asg"
                                        + PM_ALT_FIELD_DELIM +PM_NODE.OPSET.value
                                        + PM_ALT_FIELD_DELIM + sName
                                        + PM_ALT_FIELD_DELIM +PM_NODE.CONN.value
                                        + PM_ALT_FIELD_DELIM
                                        + PM_CONNECTOR_NAME);
                            }
                        }
                    }
                } else if (crtQe.getType().equalsIgnoreCase(PM_NODE.POL.value)) {

                    // The current node is a POLICY CLASS node.
                    String sCrtName = getEntityName(crtQe.getId(),
                            crtQe.getType());

                    // Process the user attributes assigned to the policy class.
                    ArrayList<Integer> attrs = getFromUserAttrs(crtQe);
                    if (attrs != null) {
                        for(Integer id : attrs){
                            sId = id.toString();
                            String sName = getEntityName(sId,PM_NODE.UATTR.value);

                            // The "superAdmin" attribute has special treatment:
                            // Do not generate "add superAdmin" to any entity.
                            // Generate "asg superAdmin" to any pc such that
                            // superAdmin-->pc
                            // and pc != admin.
                            // Insert superAdmin into the queue.
                            if (sName.equals(PM_SUPER_ADMIN_NAME)) {
                                if (!visitedSet.contains(sId)) {
                                    qe = new QueueElement(PM_NODE.UATTR.value, sId, 0);
                                    queue.add(qe);
                                    visitedSet.add(sId);
                                }
                                if (!sCrtName.equals(PM_ADMIN_NAME)) {
                                    result.addItem(ItemType.RESPONSE_TEXT,
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.UATTR.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.POL.value
                                                    + PM_ALT_FIELD_DELIM
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
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.UATTR.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.POL.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sCrtName);
                                }
                            }
                        }
                    }

                    // Process the object attributes assigned to the policy
                    // class.
                    attrs = getFromObjAttrs(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
                            String sName = getEntityName(sId,PM_NODE.OATTR.value);

                            // The "everything" object (attribute) has a special
                            // treatment.
                            // Do not generate "add everything" to any entity.
                            // Generate "asg everything" to the right entities
                            // except the "admin" pc.
                            // Insert "everything" into the queue.
                            if (sName.equals(PM_EVERYTHING_NAME)) {
                                if (!visitedSet.contains(sId)) {
                                    qe = new QueueElement(PM_NODE.OATTR.value, sId, 0);
                                    queue.add(qe);
                                    visitedSet.add(sId);
                                }
                                if (!sCrtName.equals(PM_ADMIN_NAME)) {
                                    result.addItem(ItemType.RESPONSE_TEXT,
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OATTR.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.POL.value
                                                    + PM_ALT_FIELD_DELIM
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
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OATTR.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.POL.value
                                                    + PM_ALT_FIELD_DELIM
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
                    ArrayList<Integer> attrs = getFromUsers(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
                            String sName = getEntityName(sId,PM_NODE.USER.value);

                            // The "super" user has a special treatment.
                            // Do not generate "add super" to any entity.
                            // Generate "asg super" to the correct entities
                            // except the
                            // "superAdmin" user attribute.
                            // Insert "super" into the queue.
                            if (sName.equals(PM_SUPER_NAME)) {
                                if (!visitedSet.contains(sId)) {
                                    qe = new QueueElement(PM_NODE.USER.value, sId, 0);
                                    queue.add(qe);
                                    visitedSet.add(sId);
                                }
                                if (!sCrtName.equals(PM_SUPER_ADMIN_NAME)) {
                                    result.addItem(ItemType.RESPONSE_TEXT,
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.USER.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.UATTR.value
                                                    + PM_ALT_FIELD_DELIM
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
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.USER.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.UATTR.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sCrtName);
                                }
                            }
                        }
                    }

                    // Process the user attributes assigned to this user
                    // attribute.
                    attrs = getFromAttrs(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
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
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.UATTR.value
                                                + PM_ALT_FIELD_DELIM
                                                + getEntityName(sId,
                                                PM_NODE.UATTR.value)
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.UATTR.value
                                                + PM_ALT_FIELD_DELIM + sCrtName);
                            }
                        }
                    }

                    // Process the operation sets assigned to this user
                    // attribute.
                    Vector<Integer> vAttrs = getToOpsets(crtQe);
                    if (vAttrs != null) {
                        for (Integer id : vAttrs) {
                            sId = id.toString();
                            String sName = getEntityName(sId,PM_NODE.OPSET.value);

                            // The "all ops" operation set has a special
                            // treatment.
                            // Do not generate "add allops" to any entity.
                            // Generate "asg entity to allops" for the right
                            // entities except the
                            // "superAdmin" user attribute.
                            // Insert "allops" into the queue.
                            if (sName.equals(PM_ALL_OPS_NAME)) {
                                if (!visitedSet.contains(sId)) {
                                    qe = new QueueElement(PM_NODE.OPSET.value, sId, 0);
                                    queue.add(qe);
                                    visitedSet.add(sId);
                                }
                                if (!sCrtName.equals(PM_SUPER_ADMIN_NAME)) {
                                    result.addItem(ItemType.RESPONSE_TEXT,
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.UATTR.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sCrtName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OPSET.value
                                                    + PM_ALT_FIELD_DELIM
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
                                            "add" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OPSET.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sOpsetName
                                                    + PM_ALT_FIELD_DELIM
                                                    + PM_OBJ_CLASS
                                                    + PM_ALT_FIELD_DELIM
                                                    + sClass
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.UATTR.value
                                                    + PM_ALT_FIELD_DELIM
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
                                                "add" + PM_ALT_FIELD_DELIM
                                                        + PM_OP
                                                        + PM_ALT_FIELD_DELIM
                                                        + sOp
                                                        + PM_ALT_FIELD_DELIM
                                                        +PM_NODE.OPSET.value
                                                        + PM_ALT_FIELD_DELIM
                                                        + sName);
                                    }
                                } else {
                                    result.addItem(ItemType.RESPONSE_TEXT,
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.UATTR.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sCrtName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OPSET.value
                                                    + PM_ALT_FIELD_DELIM
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
                    ArrayList<Integer> attrs = getFromAttrs(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
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
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.OATTR.value
                                                + PM_ALT_FIELD_DELIM
                                                + getEntityName(sId,
                                                PM_NODE.OATTR.value)
                                                + PM_ALT_FIELD_DELIM
                                                +PM_NODE.OATTR.value
                                                + PM_ALT_FIELD_DELIM + sCrtName);
                            }
                        }
                    }

                    // Process the operation sets assigned to this object
                    // attribute.
                    attrs = getFromOpsets(crtQe);
                    if (attrs != null) {
                        for (Integer id : attrs) {
                            sId = id.toString();
                            String sName = getEntityName(sId,PM_NODE.OPSET.value);

                            // The "all ops" operation set has a special
                            // treatment.
                            // Do not generate "add allops" to any entity.
                            // Generate "asg allops" to the right entities
                            // except the
                            // "everything" user attribute.
                            // Insert "allops" into the queue.
                            if (sName.equals(PM_ALL_OPS_NAME)) {
                                if (!visitedSet.contains(sId)) {
                                    qe = new QueueElement(PM_NODE.OPSET.value, sId, 0);
                                    queue.add(qe);
                                    visitedSet.add(sId);
                                }
                                if (!sCrtName.equals(PM_EVERYTHING_NAME)) {
                                    result.addItem(ItemType.RESPONSE_TEXT,
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OPSET.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OATTR.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sCrtName);
                                }
                            } else {
                                if (!visitedSet.contains(sId)) {
                                    qe = new QueueElement(PM_NODE.OPSET.value, sId, 0);
                                    queue.add(qe);
                                    // Get its class.
                                    String sClass = "Ignored";
                                    result.addItem(ItemType.RESPONSE_TEXT,
                                            "add" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OPSET.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName
                                                    + PM_ALT_FIELD_DELIM
                                                    + PM_OBJ_CLASS
                                                    + PM_ALT_FIELD_DELIM
                                                    + sClass
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OATTR.value
                                                    + PM_ALT_FIELD_DELIM
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
                                                "add" + PM_ALT_FIELD_DELIM
                                                        + PM_OP
                                                        + PM_ALT_FIELD_DELIM
                                                        + sOp
                                                        + PM_ALT_FIELD_DELIM
                                                        +PM_NODE.OPSET.value
                                                        + PM_ALT_FIELD_DELIM
                                                        + sName);
                                    }
                                } else {
                                    result.addItem(ItemType.RESPONSE_TEXT,
                                            "asg" + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OPSET.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sName
                                                    + PM_ALT_FIELD_DELIM
                                                    +PM_NODE.OATTR.value
                                                    + PM_ALT_FIELD_DELIM
                                                    + sCrtName);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in export: " + e.getMessage());
        }

        // Export the applications.
        List<String> appPaths =  getAppPaths(sClientId, sSessId);
        if (appPaths == null) {
            return fail();
        }

        for (int i = 0; i < appPaths.size(); i++) {
            String sLine = appPaths.get(i);

            try {
                // An item of the result contains:
                // <admin tool path>|<editor path>|<wkf path>|<email path>|
                // <exporter path>|<openoffice path>|<msoffice path>|
                // <mr editor>|<acct editor>|<host>.
                // At least the host is not empty.
                String[] pieces = sLine.split(PM_ALT_DELIM_PATTERN);
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
                        + PM_ALT_FIELD_DELIM + PM_APP_PATH + PM_ALT_FIELD_DELIM
                        + pieces[0] + PM_ALT_FIELD_DELIM + pieces[1]
                        + PM_ALT_FIELD_DELIM + pieces[2] + PM_ALT_FIELD_DELIM
                        + pieces[3] + PM_ALT_FIELD_DELIM + pieces[4]
                        + PM_ALT_FIELD_DELIM + pieces[5] + PM_ALT_FIELD_DELIM
                        + pieces[6] + PM_ALT_FIELD_DELIM + pieces[7]
                        + PM_ALT_FIELD_DELIM + pieces[8] + PM_ALT_FIELD_DELIM
                        + pieces[9] + PM_ALT_FIELD_DELIM
                        + PM_HOST + PM_ALT_FIELD_DELIM + pieces[10]);
            } catch (Exception ee) {
                ee.printStackTrace();
                /*return failurePacket("Exception while exporting application paths: "
                        + ee.getMessage());*/
            }
        }

        // Export the keystore paths.
        List<String> ksPaths =  getAllKStorePaths(sClientId, sSessId);
        if (ksPaths == null) {
            return fail();
        }
        for (int i = 0; i < ksPaths.size(); i++) {
            String sLine = ksPaths.get(i);

            try {
                // An item of the result contains: <key store path>|<trust store
                // path>|<host>|<user>.
                // At least the host and the user names are not empty.
                String[] pieces = sLine.split(PM_ALT_DELIM_PATTERN);
                System.out.println("got " + pieces.length + " pieces");

                result.addItem(ItemType.RESPONSE_TEXT, "add"
                        + PM_ALT_FIELD_DELIM + PM_KS_PATH + PM_ALT_FIELD_DELIM
                        + pieces[0] + PM_ALT_FIELD_DELIM + pieces[1]
                        + PM_ALT_FIELD_DELIM + PM_HOST + PM_ALT_FIELD_DELIM
                        + pieces[2] + PM_ALT_FIELD_DELIM +PM_NODE.USER.value
                        + PM_ALT_FIELD_DELIM + pieces[3]);
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
            String[] pieces = sLine.split(PM_FIELD_DELIM);
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
            pieces = sLine.split(PM_FIELD_DELIM);
            String sDenyType = pieces[0];
            String sUserOrAttrName = pieces[1];
            String sIsInters = pieces[3];
            String sNameType = (sDenyType.equalsIgnoreCase(PM_DENY_USER_ID) ?PM_NODE.USER.value
                    :PM_NODE.UATTR.value);

            // Write cmd to create deny with name, type, user or attribute name,
            // and whether is an intersection or union of containers.
            try {
                result.addItem(ItemType.RESPONSE_TEXT, "add"
                        + PM_ALT_FIELD_DELIM + PM_DENY + PM_ALT_FIELD_DELIM
                        + sDenyName + PM_ALT_FIELD_DELIM + sDenyType
                        + PM_ALT_FIELD_DELIM + sNameType + PM_ALT_FIELD_DELIM
                        + sUserOrAttrName + PM_ALT_FIELD_DELIM + sIsInters);
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
                            + PM_ALT_FIELD_DELIM + PM_OP + PM_ALT_FIELD_DELIM
                            + sOp + PM_ALT_FIELD_DELIM + PM_DENY
                            + PM_ALT_FIELD_DELIM + sDenyName);
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
                pieces = sLine.split(PM_FIELD_DELIM);
                // pieces[0] is the container name, possibly prefixed with the
                // symbol '!'
                // indicating complement.
                String sCont;
                String sContType;
                if (pieces[0].startsWith("!")) {
                    sCont = pieces[0].substring(1);
                    sContType = PM_COMPL_OATTR;
                } else {
                    sCont = pieces[0];
                    sContType =PM_NODE.OATTR.value;
                }
                try {
                    result.addItem(ItemType.RESPONSE_TEXT, "add"
                            + PM_ALT_FIELD_DELIM + sContType
                            + PM_ALT_FIELD_DELIM + sCont + PM_ALT_FIELD_DELIM
                            + PM_DENY + PM_ALT_FIELD_DELIM + sDenyName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return failurePacket("Exception when building result packet");
                }
            }
        }


        // Export the record templates.

        // The records (some object containers) were exported as all other
        // objects attributes,
        // but incompletely. Now we export their templates, containers and keys.
        // The record ids
        // were saved in savedRecords (the ids were those of the associated
        // oattrs).

        // Export the properties (from the Property Container).
        try {
            exportTemplates(result);
            exportRecordProperties(result);
            exportProperties(result);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return result;
    }

    public Packet interpretCmd(String sSessId, String sCmd) {
        System.out.println(sCmd);
        //				try{
        //
        //					//fw.append(sCmd + "\r\n");
        //					//fw.flush();
        //					//fw.close();
        //				}catch(IOException e){
        //					e.printStackTrace();
        //				}
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sOpCode = pieces[0];
        String sPrimType = pieces[1];
        // Dispatch command according to its code and primary operand type.
        if (sOpCode.equals("add")) {
            if (sPrimType.equals(PM_OBJ_CLASS)) {
                return cmdAddObjClass(sSessId, sCmd);
            } else if (sPrimType.equals(PM_OP)) {
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
            } else if (sPrimType.equals(PM_EMAIL_ACCT)) {
                return cmdAddEmail(sSessId, sCmd);
            } else if (sPrimType.equals(PM_NODE.OATTR.value)) {
                return cmdAddOattr(sSessId, sCmd);
            } else if (sPrimType.equals(PM_COMPL_OATTR)) {
                return cmdAddComplOattr(sSessId, sCmd);
            } else if (sPrimType.equals(PM_OBJ)) {
                return cmdAddObj(sSessId, sCmd);
            } else if (sPrimType.equals(PM_APP_PATH)) {
                return cmdAddApplication(sSessId, sCmd);
            } else if (sPrimType.equals(PM_KS_PATH)) {
                return cmdAddKStores(sSessId, sCmd);
            } else if (sPrimType.equals(PM_PROP)) {
                return cmdAddProp(sSessId, sCmd);
            } else if (sPrimType.equals(PM_DENY)) {
                return cmdAddDeny(sSessId, sCmd);
            } else if (sPrimType.equals(PM_TEMPLATE)) {
                return cmdAddTpl(sSessId, sCmd);
            } else if (sPrimType.equals(PM_KEY)) {
                return cmdAddKey(sSessId, sCmd);
            } else if (sPrimType.equals(PM_COMPONENTS)) {
                return cmdAddComps(sSessId, sCmd);
            }
        } else if (sOpCode.equals("asg")) {
            return assign(sSessId, sCmd);
        }
        return failurePacket("Invalid command code and/or primary operand type: "
                + sCmd);
    }

    // The items in cmdPacket contain:
    // item 0: cmd code "importConfiguration".
    // item 1: <session id>
    // item 2,3...: the script.
    public Packet importConfiguration(String sSessId, Packet cmdPacket) {
        Packet res;
        Vector<String> delayedLines = new Vector<String>();
        List<String> lines = new ArrayList<String>();
        for (int i = 2; i < cmdPacket.size(); i++) {
            String sLine = cmdPacket.getStringValue(i);
            lines.add(sLine);
            if (sLine.length() <= 0
                    || sLine.startsWith(PM_IMPORT_COMMENT_START)) {
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
        System.out.println("DELAYED LINES");
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

        List<String> infectedOpsets = new ArrayList<String>();
        List<List<String>> opsetLines = new ArrayList<List<String>>();
        for(String l : lines) {
            String[] aL = l.split("\\|");
            if (l.contains("add|s") && l.contains("|a|")) {
                String setName = aL[2];
                infectedOpsets.add(setName);
            }

            if(infectedOpsets.contains(aL[2])){
                int index = infectedOpsets.indexOf(aL[2]);
                opsetLines.get(index).add(l);
            }
        }

        for(List<String> i : opsetLines){
            System.out.println(i);
        }

        return SQLPacketHandler.getSuccessPacket();
    }

    // Get the paths of applications installed on a host. The result should
    // contain the admin tool path in item 0 and the rtf editor path in item 1
    // if they are installed, otherwise the empty strings.

    public Packet getHostAppPaths(String sClientId, String sSessId,
                                  String sHost, String appName) {
        System.out.println("getHostAppPaths called in PmEngine");
        Packet res = new Packet();

        // Look for a OS Config record for the given host.
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, appName);
            params.addParam(ParamType.STRING, sHost);

            ArrayList<Object> appInfo = select(GET_HOST_APP_PATH, params).get(0);
            String appMainClass = (String) appInfo.get(0);
            String appPath = (String) appInfo.get(1);
            String appPrefix = (String)appInfo.get(2);

            res.addItem(ItemType.RESPONSE_TEXT, appPath);
            res.addItem(ItemType.RESPONSE_TEXT, appPrefix);
            res.addItem(ItemType.RESPONSE_TEXT, appMainClass);

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while looking for the OS configuration on "
                    + sHost);
        }
    }

    /**
     * @param sHost
     * @return a list of all of the pm applications installed in the system
     */
    public Packet getInstalledApps(String sHost) {
        try{
            if(getHostId(sHost)==null){
                return failurePacket("No such host name");
            }
            Packet resp = new Packet();
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);

            ArrayList<ArrayList<Object>> select = select(GET_INSTALLED_APPS, params);

            for(ArrayList<Object> apps : select){
                try {
                    resp.addItem(ItemType.RESPONSE_TEXT, (String)apps.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                    return failurePacket(e.getMessage());
                }
            }
            return resp;
        }catch(Exception e){
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet getHostRepository(String sSessId) {
        Packet res = new Packet();
        try {
            String sHost = getSessionHostName(sSessId);
            if (sHost == null) {
                return failurePacket("No such session or host!");
            }
            String sReposit = getHostRepositoryInternal(sHost);
            if (sReposit == null) {
                return failurePacket("No repository for host " + sHost + "!");
            }
            res.addItem(ItemType.RESPONSE_TEXT, sReposit);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet");
        }
        return res;
    }

    public Packet addHostApp(String sSessId,
                             String sHost, String appName, String appPath,
                             String mainClassName, String appPrefix) {
        MySQL_Parameters params = new MySQL_Parameters();
        try {
            //params.addParam(ParamType.INT, appId);
            params.addParam(ParamType.INT, getHostId(sHost));
            params.addParam(ParamType.STRING, appName);
            params.addParam(ParamType.STRING, mainClassName);
            params.addParam(ParamType.STRING, appPath);
            params.addParam(ParamType.STRING, appPrefix);

            insert(ADD_HOST_APP, params);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return SQLPacketHandler.getSuccessPacket();
    }

    // Set the paths of applications installed on a host.
    public Packet setHostAppPaths(String sSessId, String sHost,
                                  String sAtoolPath, String sRtfedPath, String sWkfPath,
                                  String sEmlPath, String sExpPath, String sLauncherPath,
                                  String sMsofficePath, String sMedrecPath, String sAcctrecPath
            ,String soldWkfPath, String sSchemaPath) {
        MySQL_Parameters params = new MySQL_Parameters();
        try{
            // Admin Tool
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Admin");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.admin.PmAdmin");
            params.addParam(ParamType.STRING, sAtoolPath);
            params.addParam(ParamType.STRING, "Admin >>");
            executeStoredProcedure(SET_APP_PATH, params);
            System.out.println("Admin Tool Entry is successful");

            // Rich Text Editor
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Rich Text Editor");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.rtf.RTFEditor");
            params.addParam(ParamType.STRING, sRtfedPath);
            params.addParam(ParamType.STRING, "Rich Text Editor >>");
            executeStoredProcedure(SET_APP_PATH, params);

            // Workflow Editor
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Workflow Editor");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.oldworkflow.Wkflow");
            params.addParam(ParamType.STRING, soldWkfPath);
            params.addParam(ParamType.STRING, "Workflow Editor >>");
            executeStoredProcedure(SET_APP_PATH, params);

            // e-grant
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "e-grant");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.grantor.Grantor");
            params.addParam(ParamType.STRING, sEmlPath);
            params.addParam(ParamType.STRING, "e-grant >>");
            executeStoredProcedure(SET_APP_PATH, params);

            // Exporter
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Exporter");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.exporter.Exporter");
            params.addParam(ParamType.STRING, sExpPath);
            params.addParam(ParamType.STRING, "Exporter >>");
            executeStoredProcedure(SET_APP_PATH, params);

            // Open Office
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Open Office");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.openoffice.OfficeLauncher");
            params.addParam(ParamType.STRING, sLauncherPath);
            params.addParam(ParamType.STRING, "Open Office >>");
            executeStoredProcedure(SET_APP_PATH, params);

            // Microsoft Office Launcher
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Microsoft Office Launcher");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.office.MSOfficeLauncher");
            params.addParam(ParamType.STRING, sMsofficePath);
            params.addParam(ParamType.STRING, "Microsoft Office Launcher >>");
            executeStoredProcedure(SET_APP_PATH, params);

            // Med-Rec
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Med-Rec");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.medrec.MREditor");
            params.addParam(ParamType.STRING, sMedrecPath);
            params.addParam(ParamType.STRING, "Med-Rec >>");
            executeStoredProcedure(SET_APP_PATH, params);

            // Acct-Rec
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Acct-Rec");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.acctrec.AcctEditor");
            params.addParam(ParamType.STRING, sAcctrecPath);
            params.addParam(ParamType.STRING, "Acct-Rec >>");
            executeStoredProcedure(SET_APP_PATH, params);

            // Schema Builder
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Schema Builder");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.schema.builder.SchemaBuilder");
            params.addParam(ParamType.STRING, sSchemaPath);
            params.addParam(ParamType.STRING, "Schema Builder>>");
            executeStoredProcedure(SET_APP_PATH, params);

            // Employee Record
            String empRecPath = "C:\\PM\\dist\\pm-app-emprec-1.5.jar;C:\\PM\\dist\\pm-commons-1.5.jar;C:\\PM\\lib\\activation-1.1.jar;C:\\PM\\lib\\aopalliance-1.0.jar;C:\\PM\\lib\\asm-3.1.jar;C:\\PM\\lib\\bcmail-jdk15-1.44.jar;C:\\PM\\lib\\bcprov-jdk15-1.44.jar;C:\\PM\\lib\\cglib-2.2.1-v20090111.jar;C:\\PM\\lib\\colorchooser-1.0.jar;C:\\PM\\lib\\commons-logging-1.1.1.jar;C:\\PM\\lib\\fontbox-1.6.0.jar;C:\\PM\\lib\\guava-r09.jar;C:\\PM\\lib\\guice-3.0.jar;C:\\PM\\lib\\icu4j-3.8.jar;C:\\PM\\lib\\jarjar-1.0.jar;C:\\PM\\lib\\javax.inject-1.jar;C:\\PM\\lib\\javax.mail-1.4.4.jar;C:\\PM\\lib\\jempbox-1.6.0.jar;C:\\PM\\lib\\jfontchooser-1.0.5-pm.jar;C:\\PM\\lib\\jna-3.2.7-pm-platform.jar;C:\\PM\\lib\\jna-3.2.7-pm.jar;C:\\PM\\lib\\jsr305-1.3.7.jar;C:\\PM\\lib\\miglayout-3.7.3.1-swing.jar;C:\\PM\\lib\\pdfbox-1.6.0.jar;C:\\PM\\lib\\wrapper-3.2.3.jar;C:\\PM\\lib\\wrapper.jar";
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Employee Record");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.appeditor.EmployeeRecord");
            params.addParam(ParamType.STRING, empRecPath);
            params.addParam(ParamType.STRING, "Employee Record>>");
            executeStoredProcedure(SET_APP_PATH, params);

            // Table Editor
            String tableEditorPath = "C:\\PM\\dist\\pm-app-tableeditor-1.6.jar;C:\\PM\\dist\\pm-commons-1.5.jar;C:\\PM\\lib\\activation-1.1.jar;C:\\PM\\lib\\aopalliance-1.0.jar;C:\\PM\\lib\\asm-3.1.jar;C:\\PM\\lib\\bcmail-jdk15-1.44.jar;C:\\PM\\lib\\bcprov-jdk15-1.44.jar;C:\\PM\\lib\\cglib-2.2.1-v20090111.jar;C:\\PM\\lib\\colorchooser-1.0.jar;C:\\PM\\lib\\commons-logging-1.1.1.jar;C:\\PM\\lib\\fontbox-1.6.0.jar;C:\\PM\\lib\\guava-r09.jar;C:\\PM\\lib\\guice-3.0.jar;C:\\PM\\lib\\icu4j-3.8.jar;C:\\PM\\lib\\jarjar-1.0.jar;C:\\PM\\lib\\javax.inject-1.jar;C:\\PM\\lib\\javax.mail-1.4.4.jar;C:\\PM\\lib\\jempbox-1.6.0.jar;C:\\PM\\lib\\jfontchooser-1.0.5-pm.jar;C:\\PM\\lib\\jna-3.2.7-pm-platform.jar;C:\\PM\\lib\\jna-3.2.7-pm.jar;C:\\PM\\lib\\jsr305-1.3.7.jar;C:\\PM\\lib\\miglayout-3.7.3.1-swing.jar;C:\\PM\\lib\\pdfbox-1.6.0.jar;C:\\PM\\lib\\wrapper-3.2.3.jar;C:\\PM\\lib\\wrapper.jar";
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sHost);
            params.addParam(ParamType.STRING, "Table Editor");
            params.addParam(ParamType.STRING, "gov.nist.csd.pm.application.schema.tableeditor.TableEditor");
            params.addParam(ParamType.STRING, tableEditorPath);
            params.addParam(ParamType.STRING, "TE>>");
            executeStoredProcedure(SET_APP_PATH, params);

            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in engine's setAppPaths for "+ sHost);
        }
    }

    /**
     * Get the paths of the key and trust stores for a user on a host.
     * They are returned in items 0 and 1 of the result.
     * @param sClientId
     * @param sSessId
     * @return
     */

    public Packet getKStorePaths(String sClientId, String sSessId) {
        Packet res = new Packet();
        String sUserId = "";
        String sHost = "";
        if (sSessId==null) {
            return failurePacket("Null session ID");
        }
        MySQL_Parameters params = new MySQL_Parameters();
        try {
            sHost = getSessionHostId(sSessId);

            sUserId = getSessionUserIdInternal(sSessId);
            params.addParam(ParamType.INT, sHost);
            params.addParam(ParamType.INT, sUserId);

            ArrayList<Object> results = select(GET_KSTORE_PATHS, params).get(0);

            String kSPath = (String) results.get(0);
            String tSPath = (String) results.get(1);
            res.addItem(ItemType.RESPONSE_TEXT, kSPath);
            res.addItem(ItemType.RESPONSE_TEXT, tSPath);
            return res;
        }catch(Exception e){
            e.printStackTrace();
            return failurePacket("Exception while looking for the session configuration on "
                    + sHost);
        }
    }

    // Set the paths of the key and trust stores for a user on a host.

    public Packet setKStorePaths(String sSessId, String sUserId, String sHost,
                                 String sKsPath, String sTsPath) {
        MySQL_Parameters params = new MySQL_Parameters();
        try {
            params.addParam(ParamType.STRING, sKsPath);
            params.addParam(ParamType.STRING, sTsPath);
            params.addParam(ParamType.STRING, getHostId(sHost));
            params.addParam(ParamType.INT, sUserId);

            // update(SET_KSTORE_PATHS, params);
            insert(SET_KSTORE_PATHS, params);
        } catch (Exception e) {
            try{
                update(UPDATE_KSTORE_PATHS, params);
            } catch (Exception e1) {
                return failurePacket("Exception in engine's setKStorePaths for "
                        + sHost + ": " + e.getMessage());
            }
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet getDenies(String sSessId) {
        Packet result = new Packet();
        try {
            ArrayList<ArrayList<Object>> denies = select(GET_DENIES);
            for(ArrayList<Object> d : denies){
                result.addItem(ItemType.RESPONSE_TEXT, (String)d.get(0) + PM_FIELD_DELIM + d.get(1));
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getDenies(): " + e.getMessage());
        }
    }

    /**
     * The information returned by getDenyInfo has the following format:
     * item 0: <deny name>:<deny id>
     * item 1: <deny type>:<user or attribute name>:<user or attribute id>:<is intersection>
     * item 2: <opcount>
     * items 3 through 3 + opcount - 1: <operation>
     * item 3 + opcount: <contcount>
     * item 3 + opcount + 1 through 3 + opcount + 1 + contcount - 1: <container name>:<container id>
     * Note that the container name is prefixed with "!" for complements.
     *
     * @param sSessId
     * @param sDenyId the ID of the deny to retrieve the info for
     * @return
     */
    public Packet getDenyInfo(String sSessId, String sDenyId) {
        Packet result = new Packet();

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sDenyId);

        try {

            ArrayList<String> simpleInfo = getDenySimpleInfo(sDenyId);
            if(simpleInfo == null){
                return fail();
            }
            result.addItem(ItemType.RESPONSE_TEXT, simpleInfo.get(0));
            result.addItem(ItemType.RESPONSE_TEXT, simpleInfo.get(1));

            ArrayList<String> ops = extractStrings(select(GET_DENY_OPS, params));
            if(ops != null) {
                if(ops.size() > 0) {
                    result.addItem(ItemType.RESPONSE_TEXT, String.valueOf(ops.size()));

                    for (String op : ops) {
                        result.addItem(ItemType.RESPONSE_TEXT, op);
                    }
                }
            }

            ArrayList<ArrayList<Object>> oattrs = select(GET_DENY_OATTRS, params);
            if(oattrs != null & oattrs.size() > 0) {
                result.addItem(ItemType.RESPONSE_TEXT, String.valueOf(oattrs.size()));

                for (ArrayList<Object> oa : oattrs) {
                    result.addItem(ItemType.RESPONSE_TEXT, (((Integer) oa.get(2) == 1) ? ("!" + oa.get(0)) : oa.get(0)) + PM_FIELD_DELIM + oa.get(1));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getDenyInfo(): "
                    + e.getMessage());
        }
        return result;
    }

    /**
     * Get all operations in PM
     * @param sClientId
     * @return a Packet containing the names of all the operations
     * @throws Exception
     */
    public Packet getAllOps(String sClientId){
        Packet res = new Packet();
        try{
            ArrayList<ArrayList<Object>> pcs = select(GET_ALL_OPS);
            for(ArrayList<Object> pc : pcs){
                res.addItem(ItemType.RESPONSE_TEXT, (String)pc.get(0));
            }
            return res;
        } catch (Exception e){
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    /**
     * Return all object attributes (including those associated to objects).
     * @param sClientId
     * @return a Packet containg the <name>:<id> pair of every object attribute
     * @throws Exception TODO should this throw an exception?
     */
    public Packet getOattrs(String sClientId) {
        Packet result = new Packet();

        try{
            ArrayList<ArrayList<Object>> oattrs = select(GET_OATTRS);
            for(ArrayList<Object> oa : oattrs){
                result.addItem(ItemType.RESPONSE_TEXT, oa.get(0) + PM_FIELD_DELIM + oa.get(1));
            }
        } catch (Exception e){
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return result;
    }

    /**
     * Return all object attributes proper (excluding those associated to objects).
     * @param sClientId
     * @return
     * @throws Exception
     */
    public Packet getObjAttrsProper(String sClientId) {
        Packet result = new Packet();

        try{
            ArrayList<ArrayList<Object>> oattrs = select(GET_OATTRS_PROPER);
            for(ArrayList<Object> oa : oattrs){
                result.addItem(ItemType.RESPONSE_TEXT, oa.get(0) + PM_FIELD_DELIM + oa.get(1));
            }
        }  catch (Exception e){
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return result;
    }

    // The command parameters are: constraint name, type, user or uattr name,
    // user or uattr id, an operation, an oattr name, oattr id, and a boolean
    // denoting whether the denied operations apply to the intersection or to
    // the union of the object attributes.
    //
    // The deny type can be: PM_DENY_USER_ID, PM_DENY_INTRA_SESSION, or
    // PM_DENY_ACROSS_SESSIONS.
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
        try {
            Integer denyId = addDenyInternal(sDenyName, sDenyType, sDenyToName, sDenyToId,
                    sOp, sOattrName, sOattrId, sIsInters.equalsIgnoreCase(YES));
            if(denyId == null){
                return fail();
            }
            Packet result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, sDenyName
                    + PM_FIELD_DELIM + denyId);
            return result;
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }
    }

    public Packet addDenyAdmin(String sSessId, Deny deny){
        try {
            Integer denyId = addDenyInternalAdmin(deny);
            if(denyId == null){
                return fail();
            }
            Packet result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, deny.getName()
                    + PM_FIELD_DELIM + denyId);
            return result;
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }
    }
    public Integer addDenyInternalAdmin(Deny deny) throws Exception {
        System.out.println(deny);

        boolean newDeny = false;

        ArrayList<String> result;
        // First check whether the deny exists.
        String sDenyName = deny.getName();
        String sDenyType = deny.getDenyType();
        int procId = deny.getProcessId();
        int userId = deny.getUserId();
        int sessId = deny.getSessionId();
        boolean bInters = deny.isIntersection();

        String sDenyId = getEntityId(sDenyName, PM_DENY);
        if (sDenyId != null) {
            // A deny constraint with that name already exists.
            // Do a series of checks.
            result = getDenySimpleInfo(sDenyId);
            // The information returned by getDenySimpleInfo has the following
            // format:
            // item 0: <deny name>:<deny id>
            // item 1: <deny type>:<denyto name>:<denyto id>:<is intersection>
            if (result == null) {
                return null;
            }
            String sLine = result.get(1);
            String[] pieces = sLine.split(PM_FIELD_DELIM);

            if (sDenyType != null && !sDenyType.equals(pieces[0])) {
                errorMessage = "In deny, the type does not match its registered type!";
                return null;
            }

            if(sDenyType.equals(PM_DENY_PROCESS)){
                if(procId < 0){
                    errorMessage = "The process id cannot be null for a process deny";
                    return null;
                }
            }else{
                if(userId < 0){
                    errorMessage = "The user id cannot be null for a user id deny";
                    return null;
                }
            }
        } else { // New constraint.
            newDeny = true;
            // A series of checks.
            if (sDenyType == null
                    || (!sDenyType.equalsIgnoreCase(PM_DENY_USER_ID)
                    && !sDenyType.equalsIgnoreCase(PM_DENY_USER_SET)
                    && !sDenyType.equalsIgnoreCase(PM_DENY_SESSION)
                    && !sDenyType.equalsIgnoreCase(PM_DENY_PROCESS)
                    && !sDenyType.equalsIgnoreCase(PM_DENY_INTRA_SESSION) && !sDenyType.equalsIgnoreCase(PM_DENY_ACROSS_SESSIONS))) {
                errorMessage = "Null or invalid deny type!";
                return null;
            }
            if(sDenyType.equals(PM_DENY_PROCESS)){
                if(procId < 0){
                    errorMessage = "The process id cannot be null for a process deny";
                    return null;
                }
            }else{
                if(userId < 0){
                    errorMessage = "The user id cannot be null for a user id deny";
                    return null;
                }
            }

            //create the new deny
            if(sDenyType.equals(PM_DENY_PROCESS)){
                insert(ADD_PROCESS_DENY, sDenyName, sDenyType, procId, bInters ? 1 : 0);
            }else{
                insert(ADD_DENY, sDenyName, sDenyType, userId, bInters ? 1 : 0);
            }

            sDenyId = getEntityId(sDenyName, PM_DENY);
            if (sDenyId == null) {
                errorMessage = "error creating deny constraint";
                return null;
            }

            //ADD deny in denies graph
            denyMgr.addDeny(deny);
            denyMgr.printDenies();
        }

        if(!newDeny){
            delete(DELETE_DENY_OPS, sDenyId);
            delete(DELETE_DENY_OBJECTS, sDenyId);
        }

        HashSet<String> ops = deny.getOps();
        if (ops != null) {
            for(String op : ops) {
                if (!denyHasOp(sDenyId, op)) {
                    // Add the operation.

                    MySQL_Parameters params = new MySQL_Parameters();
                    params.addParam(ParamType.INT, Integer.valueOf(sDenyId));
                    params.addParam(ParamType.STRING, op);
                    insert(ADD_DENY_OP, params);
                }
            }
        }

        HashSet<DenyObject> objects = deny.getObjects();
        if (objects != null) {
            for(DenyObject o : objects) {
                String sOattrId = o.getId();
                if (!denyHasOattr(sDenyId, o.getId())) {
                    MySQL_Parameters params = new MySQL_Parameters();
                    params.addParam(ParamType.INT, Integer.valueOf(sDenyId));
                    params.addParam(ParamType.INT, Integer.valueOf(sOattrId));
                    params.addParam(ParamType.INT, o.isCompliment() ? 1 : 0);
                    insert(ADD_DENY_OBJ_ID, params);
                }
            }
        }

        if(newDeny){
            deny.setId(sDenyId);
            denyMgr.addDeny(deny);
        }else{
            deny.setId(sDenyId);
            denyMgr.updateDeny(deny);
        }
        return Integer.valueOf(sDenyId);
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

        String sDenyId;
        try {
            sDenyId = getEntityId(sDenyName, PM_DENY);

            if (sDenyId == null) {
                return failurePacket("No such deny: " + sDenyName);
            }
            if(deleteDenyInternal(sDenyId, sOp, sOattrName, sOattrId)){
                return Packet.getSuccessPacket();
            }else{
                return fail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet getEntityId(String sSessId, String sName, String sType) {
        System.out.println("getEntityId(sessid, sname, stype) called");
        Packet result = new Packet();
        try {
            String sId = getEntityId(sName, sType);
            System.out.println("sId: " + sId);
            if (sId == null) {
                return failurePacket("Could not find entity with name: " + sName + " and type: " + sType);
            }
            result.addItem(ItemType.RESPONSE_TEXT, sId);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return result;
    }

    // Returns:
    // item 0: <name>:<id>
    // item 1: <description>
    // item 2: <other info>
    // item 3 and following: <property>

    public Packet getAttrInfo(String sSessId, String sAttrId,
                              String sAttrType, String sIsVos) {

        Packet result = new Packet();

        if (!(sAttrType.equalsIgnoreCase(PM_NODE.UATTR.value) ||
                (sAttrType.equalsIgnoreCase(PM_NODE.OATTR.value)) ||
                (sAttrType.equalsIgnoreCase(PM_NODE.POL.value)))) {
            return failurePacket("Invalid attribute type!");
        }

        try{
            ArrayList<String> pc = getNodeInfo(sAttrId);
            String name = pc.get(0);
            String id = pc.get(1);
            String descr = pc.get(2);


            result.addItem(ItemType.RESPONSE_TEXT, name + PM_FIELD_DELIM + id);
            result.addItem(ItemType.RESPONSE_TEXT, descr);
            result.addItem(ItemType.RESPONSE_TEXT, "");

            HashSet<String> props = getProps(sAttrId, PM_NODE.POL.value);
            Iterator<String> propIter = props.iterator();
            while(propIter.hasNext()){
                String prop = propIter.next();
                result.addItem(ItemType.RESPONSE_TEXT, prop.substring(1, prop.length()-1));
            }
        }catch(Exception e){
            return failurePacket(e.getMessage());
        }
        return result;
    }

    // Add a property to a PM entity (user attr, obj attr, or policy).

    public Packet addProp(String sSessId, String sId, String sType,
                          String sIsVos, String sProp) {
        // TODO Test permissions.
        if (sId==null || sId.isEmpty()) {
            return failurePacket("Object Id is null");
        }
        if(addPropInternal(Integer.valueOf(sId), sType, sProp)){
            return Packet.getSuccessPacket();
        }else{
            return fail();
        }
    }

    public Packet replaceProp(String sSessId, String sId, String sType,
                              String sIsVos, String sOldProp, String sNewProp) {
        if(sId == null || sId.equals("")){
            return failurePacket("replaceProp receieved a null id");
        }

        if (!(sType.equalsIgnoreCase(PM_NODE.UATTR.value) ||
                (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) ||
                (sType.equalsIgnoreCase(PM_NODE.POL.value)))) {
            return failurePacket("Invalid entity type!");
        }

        if(sOldProp == null || sOldProp.equals("")){
            return failurePacket("old property cannot be empty or null");
        }else if(sNewProp == null || sNewProp.equals("")){
            return failurePacket("new property cannot be empty or null");
        }

        if(!sNewProp.contains(PM_PROP_DELIM)){
            return failurePacket("property is not formatted correctly: property=value");
        }

        String[] pieces = sNewProp.split(PM_PROP_DELIM);

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, pieces[0]);
        params.addParam(ParamType.STRING, pieces[1]);
        params.addParam(ParamType.INT, sId);
        params.addParam(ParamType.STRING, sOldProp);

        try {
            update(REPLACE_PROP, params);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Unable to replace property " + sOldProp
                    + " by " + sNewProp + "!");
        }

        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet removeProp(String sSessId, String sId, String sType,
                             String sIsVos, String sProp) {
        if(sId == null || sId.equals("")){
            return failurePacket("removeProp returned a null id");
        }

        if (!(sType.equalsIgnoreCase(PM_NODE.UATTR.value) ||
                (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) ||
                (sType.equalsIgnoreCase(PM_NODE.POL.value)))) {
            return failurePacket("Invalid entity type!");
        }

        if(sProp == null || sProp.equals("")){
            return failurePacket("old property cannot be empty or null");
        }

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId);
        params.addParam(ParamType.STRING, sProp);

        try {
            delete(DELETE_PROPERTY, params);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Unable to remove property " + sProp
                    + " from the entity!");
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    /**
     * Called from a host's kernel to create a new object with specified name,
     * class, type, in the specified containers. Uses the process id to
     * check for the permission to create an object.
     *
     * The sContainers parameter contains a list of type|label separated by
     * commas.
     * The sPerms paramter contains the permissions requested by the process
     * (actually session) creating the object on the object (usually
     * "File write").
     *
     * This method calls addObject3(), which does a permission check (among
     * other things it calls requestPerms() to activate the user's attributes
     * that allow his process to create the object in the specified containers).
     *
     * After the object creation, the engine generates and processes an event
     * "Object create".
     *
     * @param sSessId
     * @param sProcId
     * @param sObjName the name of the object to be created
     * @param sObjClass the class fo the object
     * @param sObjType the object type
     * @param sContainers containers to assign the object to
     * @param sPerms permissions for the object
     * @param sSender
     * @param sReceiver
     * @param sSubject
     * @param sAttached
     * @return a Packet that results from processing the event of adding an object
     * @throws Exception
     */
    public Packet createObject3(String sSessId, String sProcId,
                                String sObjName, String sObjClass, String sObjType,
                                String sContainers, String sPerms, String sSender,
                                String sReceiver, String sSubject, String sAttached) {
        /*if (!sObjClass.equalsIgnoreCase(PM_CLASS_FILE_NAME)) {
            return failurePacket("Creation of non-File objects not yet implemented!");
        }*/

        // The host of the new object will be the session host.
        Packet result = null;
        try {
            String sHost = getSessionHostName(sSessId);
            String sPhysCont = getHostRepositoryInternal(sHost);
            String sPath = null;
            if (sPhysCont.endsWith(File.separator)) {
                sPath = sPhysCont + sObjName + "." + sObjType;
            } else {
                sPath = sPhysCont + File.separator + sObjName + "." + sObjType;
            }
            result = addObject3(sSessId, sProcId, sObjName, sObjName, sObjName,
                    sContainers, sObjClass, sObjType, sHost, sPath, null, null,
                    NO, sSender, sReceiver, sSubject, sAttached, null, null,
                    null);
            if (result.hasError()) {
                return result;
            }

            String sLine = result.getStringValue(0);
            String[] pieces = sLine.split(PM_FIELD_DELIM);

            Packet procEvent = ServerConfig.obligationDAO.processEvent(sSessId, null,
                    PM_EVENT_OBJECT_CREATE, sObjName,
                    pieces[1], sObjClass, sObjType, sContainers, sPerms);
            if(procEvent.hasError()){
                deleteNode(Integer.valueOf(pieces[1]));
                return procEvent;
            }else{
                return result;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception during object creation: "
                    + e.getMessage());
        }
    }

    /**
     * Returns:
     * item 0: <name>:<id>
     * item 1: <description>
     * item 2: <other info>
     * item 3: <full name>
     * item 4 and following: <property>
     *
     * with current user_detail table there is no column for description, other info, and properties
     *
     * @param sSessId
     * @param sUserId
     * @return
     */
    public Packet getUserInfo(String sSessId, String sUserId) {
        Packet res = new Packet();

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sUserId);

        try{
            ArrayList<ArrayList<Object>> result = select(GET_USER_INFO, params);
            ArrayList<Object> userInfo = result.get(0);
            String name = (String) userInfo.get(0);
            String id = String.valueOf(userInfo.get(1));
            String descr = "";
            String other = "";
            String fullName = (String) userInfo.get(2);

            res.addItem(ItemType.RESPONSE_TEXT, name + PM_FIELD_DELIM + id);
            res.addItem(ItemType.RESPONSE_TEXT, descr);
            res.addItem(ItemType.RESPONSE_TEXT, other);
            res.addItem(ItemType.RESPONSE_TEXT, fullName);

            HashSet<String> props = getProps(sUserId, PM_NODE.USER.value);
            Iterator<String> propIter = props.iterator();
            while(propIter.hasNext()){
                String prop = propIter.next();
                res.addItem(ItemType.RESPONSE_TEXT, prop);
            }

        }catch(Exception e){
            return failurePacket("failed getting info for user: " + e.getMessage());
        }
        return res;
    }

    // entity type representative objects
    // ---------------------------------------------------------------------------
    // o all objects that represent the associated object attribute.
    // b all objects that represent the object attribute.
    // etc.

    public Packet getOpsetsBetween(String sSessId, String sUattrName,
                                   String sEntName, String sEntType) {
        // A few checks.
        String sUattrId = null;
        String sEntId = null;
        Packet result = new Packet();
        try {
            sUattrId = getEntityId(sUattrName, PM_NODE.UATTR.value);
            if (sUattrId == null) {
                return failurePacket("No user attribute " + sUattrName);
            }

            // If sEntType is "o", getIdOfEntityWithNameAndType will return the
            // id of the associated
            // object attribute.
            sEntId = getEntityId(sEntName, sEntType);
            if (sEntId == null) {
                return failurePacket("No entity " + sEntName + " of type "
                        + sEntType);
            }

            // Find all objects that represent the entity, even when the entity is
            // an
            // object or object attribute.
            HashSet<String> repSet = getObjectsRepresentingEntity(sEntId, sEntType);
            printSet(repSet, PM_OBJ,
                    "Set of virtual objects representing the entity "
                            + getEntityName(sEntId, sEntType));

            // Create an empty result.
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
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return result;
    }

    // Delete all opsets directly assigned to an user attribute and an object
    // attribute

    public Packet deleteOpsetsBetween(String sSessId, String sProcId,
                                      String sUattrName, String sOattrName) {
        Packet opsetids = new Packet();
        try {
            String sUattrId = getEntityId(sUattrName,PM_NODE.UATTR.value);
            String sOattrId = getEntityId(sOattrName,PM_NODE.OATTR.value);
            getOpsetsBetween(sUattrId, sOattrId, opsetids);
            for (int i = 0; i < opsetids.size(); i++) {
                String s = opsetids.getStringValue(i);
                String[] pieces = s.split(PM_FIELD_DELIM);
                String sOpsetId = pieces[1];
                deleteAssignment(Integer.valueOf(sUattrId),Integer.valueOf(sOpsetId));
                deleteAssignment(Integer.valueOf(sOpsetId),Integer.valueOf(sOattrId));
                deleteNode(sSessId, sOpsetId, null, NO);
            }
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    /**
     * get the oattrs that are assigned to an opset
     * @param sSessId
     * @param sOpsetName
     * @return
     */
    public Packet getOpsetOattrs(String sSessId, String sOpsetName) {
        MySQL_Parameters params = new MySQL_Parameters();
        Packet res = new Packet();
        try {
            String sOpsetId = getEntityId(sOpsetName, PM_NODE.OPSET.value);
            if(sOpsetId == null || sOpsetId.equals("")){
                return failurePacket("removeProp returned a null id");
            }
            params.addParam(ParamType.INT, sOpsetId);

            ArrayList<ArrayList<Object>> results = select(GET_OPSET_OATTRS,
                    params);
            for (ArrayList<Object> oa : results) {
                String name = (String) oa.get(0);
                String id = String.valueOf(oa.get(1));
                res.addItem(ItemType.RESPONSE_TEXT, name
                        + PM_FIELD_DELIM + id);
            }
        } catch (Exception e) {
            return failurePacket("Exception getting opset oattrs: " + e.getMessage());
        }

        return res;
    }

    // Cut all links to/from this attribute. To be called as a command.

    public Packet isolateOattr(String sName, String sType) {

        String sAttrId;
        try {
            sAttrId = getEntityId(sName, sType);
            if (sAttrId == null) {
                return failurePacket("No attribute " + sName + " of type "
                        + sType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return isolateOattr(sAttrId);
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

        boolean isSubgraph = sIncludesAscs.equalsIgnoreCase(YES);

        String sUattrId;
        try {
            sUattrId = getEntityId(sUattrName, PM_NODE.UATTR.value);
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
                sSuggOattrId = getEntityId(sSuggOattr, PM_NODE.OATTR.value);
                if (sSuggOattrId == null) {
                    return failurePacket("No such suggested object attribute "
                            + sSuggOattr);
                }
            }

			/*
			 * HashSet hs = getEntityDirectRepOattrs(sEntId, sEntType,
			 * sIncludesAscs.equalsIgnoreCase(YES)); printSet(hs,
			 * PM_PERM, "Direct representatives of entity " +
			 * sEntName);
			 */

            // If the opset name is specified, simply set its permissions equal
            // to
            // sPerms.
            if (sOpsetName != null) {
                System.out
                        .println("Opset name specified, set its operations to "
                                + sOpers);
                String sOpsetId = getEntityId(sOpsetName, PM_NODE.OPSET.value);
                if (sOpsetId == null) {
                    return failurePacket("No operation set " + sOpsetName);
                }

                // Set the opset permissions as specified by sPerms.
                setOpsetOps(sOpsetId, sOpers);
                return SQLPacketHandler.getSuccessPacket();
            }

            // No opset name specified. We treat I/O and admin operations
            // differently,
            // so get the set of all I/O operations if you didn't already do so.
            if (allIoOpers == null) {
                allIoOpers = new HashSet<String>();
                for (int i = 0; i < sDirOps.length; i++) {
                    allIoOpers.add(sDirOps[i]);
                }
                for (int i = 0; i < sFileOps.length; i++) {
                    allIoOpers.add(sFileOps[i]);
                }
            }

            // TODO Split the set of operations into I/O and administrative
            // operations.
            HashSet<String> adminOpers = UtilMethods.stringToSet(sOpers);
            adminOpers.removeAll(allIoOpers);
            printSet(adminOpers, PM_PERM,
                    "Trying to set administrative operations on " + sEntName
                            + " for " + sUattrName);

            HashSet<String> ioOpers = UtilMethods.stringToSet(sOpers);
            ioOpers.retainAll(allIoOpers);
            printSet(ioOpers, PM_PERM,
                    "Trying to set I/O operations on " + sEntName + " for "
                            + sUattrName);

            Packet res;
            if (sEntType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
                if (oattrRepresentsAnEntity(sEntId)) {
                    // The entity is an object attribute associated to an object
                    // that represents some entity or subgraph.
                    // We ignore the I/O operations and set only the admin
                    // operations
                    // in a new opset.
                    if (adminOpers.isEmpty()) {
                        return SQLPacketHandler.getSuccessPacket();
                    }
                    res = createOpsetBetween(sSessId, sProcId, adminOpers,
                            sUattrId, sEntId);
                    if (res.hasError()) {
                        return res;
                    }
                    //addPmGraphNode(sEntId, res.getStringValue(0).split(PM_FIELD_DELIM)[1],
                    //sOpsetName, PM_NODE.OPSET.value);
                    return SQLPacketHandler.getSuccessPacket();
                }
            }

            // Now the entity is an object attribute associated to a File/Dir
            // object,
            // or an object attribute not associated, or the connector node, or
            // a
            // policy
            // class, or a user attribute, or a user. The I/O operations are
            // allowed
            // only on an object attribute.
            if (sEntType.equalsIgnoreCase(PM_NODE.ASSOC.value)
                    || sEntType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
                if (!ioOpers.isEmpty()) {
                    res = createOpsetBetween(sSessId, sProcId, ioOpers,
                            sUattrId, sEntId);
                    if (res.hasError()) {
                        return res;
                    }
                }
            }

            // Now process the administrative operations.
            if (adminOpers.isEmpty()) {
                return SQLPacketHandler.getSuccessPacket();
            }

            // First get an existing or create a representative object attribute
            // for the entity, according to isSubgraph.
            String sRepOaId = getOrCreateOattrRepresentingEntity(sEntId,
                    sEntType, sSuggOattrId, sSuggBaseId, sSuggBaseType,
                    isSubgraph);

            if (sRepOaId == null) {
                return failurePacket("Could not find or create a suitable representative object for entity "
                        + sEntName);
            }

            res = createOpsetBetween(sSessId, sProcId, adminOpers, sUattrId,
                    sRepOaId);
            if (res.hasError()) {
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return SQLPacketHandler.getSuccessPacket();
    }

    /**
     * Returns a Packet that contains the <name>:<id> of the entity in
     * its first item, that has the specified prop
     *
     * @param sSessId current session ID
     * @param sEntType the type of the entity to retrieve
     * @param sProp the property used to find an entity
     * @return a Packet conating the <name>:<id> fo the entity with the specified type and property
     */
    public Packet getEntityWithProp(String sSessId, String sEntType,
                                    String sProp) {
        Packet res = new Packet();
        try {
            String sId = getEntityWithPropInternal(sEntType, sProp);
            if (sId == null) {
                return failurePacket("No such entity found!");
            }
            String sName = getEntityName(sId, sEntType);
            res.addItem(ItemType.RESPONSE_TEXT, sName
                    + PM_FIELD_DELIM + sId);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
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
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
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
        sCmd = "add|deny|" + sName + "|" + PM_DENY_ACROSS_SESSIONS + "|a|"
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
        sCmd = "add|deny|" + sName + "|" + PM_DENY_ACROSS_SESSIONS + "|a|"
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
            FileWriter fw = new FileWriter("C:\\Users\\Administrator\\Desktop\\output.txt");
            for(int i = 0; i < commands.size(); i++){
                System.out.println(commands.get(i));
                fw.append(commands.get(i) + "\r\n");
                fw.flush();
            }
            fw.close();
        }catch(IOException e){
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return SQLPacketHandler.getSuccessPacket();
    }

    // Gopi - should we delete test methods?
    public Packet testGetMemberObjects(String sSessId, String sContainerId,
                                       String sType) {
        if (!sType.equalsIgnoreCase(PM_NODE.OATTR.value)
                && !sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            return failurePacket("Wrong type of container!");
        }

        Packet result = new Packet();
        HashSet<String> assocOattrs = new HashSet<String>();
        try {
            getMemberObjects(sContainerId, assocOattrs);
            Iterator<String> iter = assocOattrs.iterator();
            while (iter.hasNext()) {
                String sId = iter.next();
                result.addItem(ItemType.RESPONSE_TEXT,
                        getEntityName(sId,PM_NODE.OATTR.value) + PM_FIELD_DELIM
                                + sId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet");
        }
        return result;
    }

    /**
     * Check if a given attribute is assigned to another given attribute
     */
    public Packet isAssigned(String end, String sType1, String start,
                             String sType2) {
        Boolean isAssigned = false;
        Packet res = new Packet();
        if (end==null || end.isEmpty() || start==null || start.isEmpty()) {
            return failurePacket("One or more attributes are null.");
        }

        try {
            isAssigned = isAssigned(Integer.valueOf(end), Integer.valueOf(start));
            if (isAssigned ) {
                res.addItem(ItemType.RESPONSE_TEXT, YES);
            } else {
                res.addItem(ItemType.RESPONSE_TEXT, NO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    public Packet getEntityName(String sSessId, String sId, String sType) {
        Packet result = new Packet();
        try {
            String sName = getEntityName(sId, sType);
            if (sName == null) {
                return failurePacket("No such entity or type!");
            }
            result.addItem(ItemType.RESPONSE_TEXT, sName);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet");
        }
        return result;
    }

    public Packet testGetDeniedPerms(String sCrtSessId, String sObjName) {
        HashSet<String> deniedOps;
        try {
            String sObjId = getEntityId(sObjName, PM_OBJ);
            if (sObjId == null) {
                return failurePacket("No such object " + sObjName + "!");
            }
            String sOattrId = getAssocOattr(sObjId);
            if (sOattrId == null) {
                return failurePacket("Inconsistency: no associated object attribute!");
            }
            deniedOps = getDeniedPerms(sCrtSessId, null, null, sOattrId,
                    PM_NODE.OATTR.value);
            printSet(deniedOps, PM_PERM,
                    "Denied permissions are:");
        } catch (Exception e) {
            e.printStackTrace();
            deniedOps = new HashSet<String>();
        }

        return SQLPacketHandler.setToPacket(deniedOps);
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
        String sUserId = null;
        Packet res;
        String sHomeId;
        String sUserNameAttrId;
        String sConfAttrId = null;
        // Return failure if a DAC policy does not exist.
        try {
            String sDacPolId = getEntityWithPropInternal(PM_NODE.POL.value,
                    "type=discretionary");
            if (sDacPolId == null) {
                return failurePacket("No DAC policy exists!");
            }

            // Return failure if the confinement policy exists but does not have
            // the
            // property
            // "type=confinement".
            String sConfPolId = null;
            sConfPolId = getEntityId(sConfPol, PM_NODE.POL.value);
            if (sConfPolId != null) {
                HashSet<String> props = getProps(sConfPolId, PM_NODE.POL.value);
                if (!props.contains("type=confinement")) {
                    return failurePacket("Policy \"" + sConfPol
                            + "\" exists, but is not a confinement policy!");
                }
            }

            // The user must exist.
            sUserId = getEntityId(sUser, PM_NODE.USER.value);

            if (sUserId == null) {
                return failurePacket("User \"" + sUser
                        + "\" must already exist!");
            }

            // The user name attribute must exist.
            sUserNameAttrId = getEntityWithPropInternal(PM_NODE.UATTR.value,
                    "nameof=" + sUser);
            if (sUserNameAttrId == null) {
                return failurePacket("User \"" + sUser
                        + "\" does not have a name attribute!");
            }

            // The user's home must exist.
            sHomeId = getEntityWithPropInternal(PM_NODE.OATTR.value, "homeof="
                    + sUser);
            if (sHomeId == null) {
                return failurePacket("User \"" + sUser
                        + "\" has no home container!");
            }
            // The sensitive container must exist somewhere in the user's home.
            String sSensContId = getEntityId(sSensCont, PM_NODE.OATTR.value);
            if (sSensContId == null) {
                return failurePacket("Container \"" + sSensCont
                        + "\" does not exist!");
            }
            if (!attrIsAscendant(sSensContId, sHomeId)) {
                return failurePacket("Container \"" + sSensCont
                        + "\" is not contained in user \"" + sUser + "\" home!");
            }

            // The confine attribute must not exist.
            sConfAttrId = getEntityId(sConfAttr, PM_NODE.UATTR.value);
            if (sConfAttrId != null) {
                return failurePacket("The confinement attribute \"" + sConfAttr
                        + "\" already exists!");
            }

            // Create the confinement policy if necessary.
            if (sConfPolId == null) {
                sCmd = "add|p|" + sConfPol + "|c|PM";
                res = interpretCmd(sSessId, sCmd);
                if (res.hasError()) {
                    return res;
                }
                sCmd = "add|prop|type=confinement|p|" + sConfPol;
                res = interpretCmd(sSessId, sCmd);
                if (res.hasError()) {
                    return res;
                }
                sConfPolId = getEntityId(sConfPol, PM_NODE.POL.value);
            }

            // Create the new confine attribute and assign the user to it.
            sCmd = "add|a|" + sConfAttr + "|p|" + sConfPol;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "asg|u|" + sUser + "|a|" + sConfAttr;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            // Assign the sensitive container to the confinement policy.
            sCmd = "asg|o|" + sSensCont + "|p|" + sConfPol;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            // Create an object that represents the confinement attribute.
            // Insert this object in the DAC policy class - the same policy
            // class where the user name attribute is located.
            String sDacPol = getEntityName(sDacPolId, PM_NODE.POL.value);
            sCmd = "add|ob|" + sConfAttr + " rep|User attribute|no|"
                    + sConfAttr + "|ignored|p|" + sDacPol;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            // Create an object that represents the sensitive container - in the
            // confinement policy.
            sCmd = "add|ob|" + sSensCont + " rep|Object attribute|no|"
                    + sSensCont + "|ignored|p|" + sConfPol;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            // Set permissions:
            // <confine attribute> -> read/write -> <sensitive container>.
            String sName = UtilMethods.generateRandomName();
            sCmd = "add|s|" + sName + "|oc|ignored|a|" + sConfAttr;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "asg|s|" + sName + "|b|" + sSensCont;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|File read|s|" + sName;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|File write|s|" + sName;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            // Set permissions:
            // <confine attribute> -> create objects/assign object attributes ->
            // <sensitive container>.
            sName = UtilMethods.generateRandomName();
            sCmd = "add|s|" + sName + "|oc|ignored|a|" + sConfAttr;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "asg|s|" + sName + "|b|" + sSensCont + " rep";
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|Object attribute assign to|s|" + sName;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|Object attribute create object|s|" + sName;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            // Set permissions:
            // <user name attribute> -> assign user attrs -> <confine
            // attribute>.
            sName = UtilMethods.generateRandomName();
            String sUserNameAttr = getEntityName(sUserNameAttrId,
                    PM_NODE.UATTR.value);
            sCmd = "add|s|" + sName + "|oc|ignored|a|" + sUserNameAttr;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "asg|s|" + sName + "|b|" + sConfAttr + " rep";
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|User attribute assign to|s|" + sName;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            // Deny conf attribute File write on the complement of the sensitive
            // container.
            sName = UtilMethods.generateRandomName();
            sCmd = "add|deny|" + sName + "|"
                    + PM_DENY_ACROSS_SESSIONS + "|a|"
                    + sConfAttr + "|no";
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|File write|deny|" + sName;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|cb|" + sSensCont + "|deny|" + sName;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            // Deny conf attribute Create object on the complement of the
            // sensitive
            // container.
            sName = UtilMethods.generateRandomName();
            sCmd = "add|deny|" + sName + "|"
                    + PM_DENY_ACROSS_SESSIONS + "|a|"
                    + sConfAttr + "|no";
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|Object attribute create object|deny|" + sName;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|cb|" + sSensCont + "|deny|" + sName;
            res = interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
            failurePacket(e.getMessage());
        }

        return SQLPacketHandler.getSuccessPacket();
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
                                     String sSensCont) {

        String sCmd;

        // Do permission checks...

        // Return failure if a DAC policy does not exist.
        try {
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
            Integer userId = addUserInternal(sUser, null, sFullName,
                    sFullName, null, PM_CONNECTOR_ID, PM_NODE.CONN.value);
            if (userId == null) {
                return fail();
            }
            Packet result =  ServerConfig.obligationDAO.processEvent(sSessId, null, PM_EVENT_USER_CREATE,
                    sUser, userId.toString(), PM_CLASS_USER_NAME, null, null, null);
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
            Packet res =  interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }

        return genConfForDacUser(sSessId, sUser, sConfPol, sConfAttr, sSensCont);
    }

    public Packet addEmailAcct(String sSessId, String sPmUser,
                               String sFullName, String sEmailAddr, String sPopServer,
                               String sSmtpServer, String sAcctName, String sPassword) {
        if(sEmailAddr == null || sEmailAddr.equals("")){
            return failurePacket("addEmailAcct received a null email address");
        }
        // Checks...
        if(addEmailAcctInternal(sPmUser, sFullName, sEmailAddr, sPopServer,
                sSmtpServer, sAcctName, sPassword)){
            return Packet.getSuccessPacket();
        }else{
            return fail();
        }
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
        Packet result = new Packet();
        String sPmUserId;
        try {
            sPmUserId = getEntityId(sPmUser,PM_NODE.USER.value);
            if (sPmUserId == null || sPmUserId.isEmpty()) {
                return failurePacket("No such PM user!");
            }
            if (!entityExists(sPmUser, PM_NODE.USER.value)) {
                result.addItem(ItemType.RESPONSE_TEXT, sPmUser + PM_FIELD_DELIM
                        + sPmUserId);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sPmUserId);
            ArrayList<Object> emailAcct = select(GET_EMAIL_ACCT, params).get(0);

            result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, sPmUser + PM_FIELD_DELIM
                    + sPmUserId);
            result.addItem(ItemType.RESPONSE_TEXT,
                    (String) emailAcct.get(0));
            result.addItem(ItemType.RESPONSE_TEXT,
                    emailAcct.get(1)==null?"":(String) emailAcct.get(1));
            result.addItem(ItemType.RESPONSE_TEXT,
                    emailAcct.get(1)==null?"":(String) emailAcct.get(2));
            result.addItem(ItemType.RESPONSE_TEXT,
                    emailAcct.get(1)==null?"":(String) emailAcct.get(3));
            result.addItem(ItemType.RESPONSE_TEXT,
                    emailAcct.get(1)==null?"":(String) emailAcct.get(4));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while retrieveing the email account attributes!");
        }
    }

    // The message is already in an object with the name sMsgName, probably
    // located
    // in the OUTBOX container of the sender. Just include the message in the
    // receiver's INBOX container.

    public Packet sendSimpleMsg(String sSessId, String sMsgName,
                                String sReceiver) {
        // Find the msg id (as an object attribute).
        String sMsgId;
        Packet res;
        try {
            sMsgId = getEntityId(sMsgName, PM_NODE.OATTR.value);
            if (sMsgId == null) {
                return failurePacket("No message " + sMsgName + " exists!");
            }

            // Find the sender and the sender's OUTBOX container.
            String sSenderId = getSessionUserIdInternal(sSessId);
            if (sSenderId == null) {
                return failurePacket("Couldn't find sender!");
            }
            String sSender = getEntityName(sSenderId, PM_NODE.USER.value);
            if (sSender == null) {
                return failurePacket("Couldn't find sender!");
            }

            String sOutboxId = getEntityWithPropInternal(PM_NODE.OATTR.value,
                    "outboxof=" + sSender);
            if (sOutboxId == null) {
                return failurePacket("Sender has no OUTBOX container!");
            }

            // Find the receiver's INBOX container.
            String sInboxId = getEntityWithPropInternal(PM_NODE.OATTR.value,
                    "inboxof=" + sReceiver);
            if (sInboxId == null) {
                return failurePacket("Receiver has no INBOX container!");
            }

            if(!addDoubleLink(sInboxId, sMsgId)){
                return fail();
            }

            if(!deleteAssignment(Integer.valueOf(sMsgId), Integer.valueOf(sOutboxId))){
                return fail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
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
        String sObjId = null;
        try {
            sObjId = getEntityId(sObjName, PM_OBJ);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        if (sObjId == null) {
            return failurePacket("No object of name " + sObjName);
        }
        Packet result =  getObjInfo(sObjId);
        if (result.hasError()) {
            return result;
        }

        String s = result.getStringValue(0);
        String[] pieces = s.split(PM_ALT_DELIM_PATTERN);
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

        String sCopyName = null;
        String sPhysLoc = null;
        String sCopyDescr = null;
        String sCopyInfo = null;
        String sCopyClass = null;
        String sCopyHost = null;
        try {
            sCopyName = getCopyName(sObjName, PM_OBJ);
            sCopyDescr = "Copy of object " + sObjName;
            sCopyInfo = "None";
            sCopyClass = pieces[2];
            sCopyHost = getSessionHostName(sSessId);
            // String sCopyHost = "pmclient";
            sPhysLoc = getHostRepositoryInternal(sCopyHost);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        String sCopyPath;
        if (sPhysLoc.endsWith(File.separator)) {
            sCopyPath = sPhysLoc + sCopyName + "." + sCopyType;
        } else {
            sCopyPath = sPhysLoc + File.separator + sCopyName + "." + sCopyType;
        }

        // Find the id of the attribute associated to the original object.
        String sOattrId;
        try {
            sOattrId = getEntityId(sObjName,PM_NODE.OATTR.value);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        if (sOattrId == null) {
            return failurePacket("The object " + sObjName
                    + " has no associated attribute!");
        }

        // Find the containers of the original object (note that PM or a policy
        // class can be an object container, not only other object attributes).
        String sContainers;
        try {
            sContainers = getOattrContainers(sOattrId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        System.out.println("Containers of object " + sObjName + " are: "
                + sContainers);

        String sCopyId = null;
        String sAssocId = null;

        // For each container:
        pieces = sContainers.split(PM_LIST_MEMBER_SEP);
        for (int i = 0; i < pieces.length; i++) {
            String[] sTypeLabel = pieces[i].split(PM_ALT_DELIM_PATTERN);
            System.out.println("Container no. " + i + ": " + sTypeLabel[0]
                    + "|" + sTypeLabel[1]);

            // Get container's id,
            String sContId;
            try {
                sContId = getEntityId(sTypeLabel[1], sTypeLabel[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return failurePacket(e.getMessage());
            }
            if (sContId == null) {
                return failurePacket("No container " + sTypeLabel[1]
                        + " of type " + sTypeLabel[0]);
            }

            // create or insert the new object within the container.
            if (i == 0) {
                // For the first container, create the object within. Let the
                // engine
                // generate the object's and the assoc. object attribute's ids.
                Integer objId = addObjectInternal(sCopyName, null, null,
                        sCopyDescr, sCopyInfo, sContId, sTypeLabel[0],
                        sCopyClass, sCopyType, sCopyHost, sCopyPath, null,
                        null, false, null, null, null, null, null, null, null);
                if (objId == null) {
                    return fail();
                }
                String sLine = result.getStringValue(0);
                String[] splinters = sLine.split(PM_FIELD_DELIM);
                sCopyId = splinters[1];
                // objectId is same as associated object Id from PM2.0
                //sAssocId = getAssocOattr(sCopyId);
                sAssocId = sCopyId;
            } else {
                // For the other containers, assign the oattr associated with
                // the
                // new object to each container, if not already there.
                try {
                    if(isAssigned(Integer.valueOf(sAssocId), Integer.valueOf(sContId))){
                        continue;
                    }

                    if(!assignInternal(sAssocId,PM_NODE.OATTR.value,
                            sContId, sTypeLabel[0])){
                        return failurePacket("could not copy object - failed at assign internal");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return failurePacket(e.getMessage());
                }
            }
        }
        result = new Packet();
        try {
            result.addItem(ItemType.RESPONSE_TEXT, sCopyName
                    + PM_ALT_FIELD_DELIM + sCopyId + PM_ALT_FIELD_DELIM
                    + sCopyClass + PM_ALT_FIELD_DELIM + NO
                    + PM_ALT_FIELD_DELIM + sCopyHost + PM_ALT_FIELD_DELIM
                    + sCopyPath);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet");
        }
        return result;
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
        HashSet<Integer> users = new HashSet<Integer>();
        // Is this a user?
        try {
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

            getMemberUsers(sUattrId, users);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        Iterator<Integer> iter = users.iterator();
        try {
            while (iter.hasNext()) {
                sUserId = String.valueOf(iter.next());
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

    /**
     * Get all users and user attrubtes
     * @param sSessId
     * @return a packet containing all users and user attributes in name:id pair
     */
    public Packet getUsersAndAttrs(String sSessId) {
        Packet res = new Packet();

        //SQL statement should return name and id
        try {
            ArrayList<ArrayList<Object>> users = select(GET_USERS);
            for(ArrayList<Object> u : users){
                res.addItem(ItemType.RESPONSE_TEXT, u.get(0) + PM_FIELD_DELIM + u.get(1));
            }

            //sql statement should return name, id
            ArrayList<ArrayList<Object>> uattrs =  select(GET_USER_ATTRS);
            for(ArrayList<Object> ua : uattrs){
                res.addItem(ItemType.RESPONSE_TEXT, ua.get(0) + PM_FIELD_DELIM + ua.get(1));
            }

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception: " + e.getMessage());
        }
    }

    public ArrayList<Integer> getPolicies(){
        try {
            return extractIntegers(select(GET_ALL_POLICY_IDS));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Integer> getUattrs(){
        try {
            return extractIntegers(select(GET_ALL_UATTR_IDS));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Integer> getUsers(){
        try {
            return extractIntegers(select(GET_ALL_USER_IDS));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Integer> getOattrs(){
        try {
            return extractIntegers(select(GET_ALL_OATTR_IDS));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Integer> getObjs(){
        try {
            return extractIntegers(select(GET_ALL_OBJ_IDS));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<Integer> getOpsets(){
        try{
            return extractIntegers(select(GET_ALL_OPSET_IDS));
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // An object can be opened multiple times in a session before being closed.
    // pmOpenObj stores the name of the object and a count:
    // <obj name>|<link count>.

    public Packet addOpenObj(String sSessId, String sObjName) {
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            String objId = getEntityId(sObjName, PM_NODE.ASSOC.value);
            if (objId==null) {
                return failurePacket("Object name is null or does not exists");
            }
            params.addParam(ParamType.INT, sSessId);
            params.addParam(ParamType.STRING, objId);
            params.addParam(ParamType.INT, 1);
            insert(ADD_OPEN_OBJ, params);
            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while adding open object: "
                    + e.getMessage());
        }
    }

    public Packet deleteOpenObj(String sSessId, String sObjName) {
        if (sSessId == null || sSessId.isEmpty() || sObjName==null || sObjName.isEmpty()) {
            return failurePacket("Session ID or object Name null");
        }
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sSessId);
            params.addParam(ParamType.STRING, getEntityId(sObjName, PM_NODE.ASSOC.value));
            ArrayList<Integer> ints = extractIntegers(select(GET_OPEN_OBJ_COUNT, params));
            Integer count = (ints == null || ints.isEmpty()) ? null : ints.get(0);
            if(count!= null && count > 0){
                params.clearParams();
                params.addParam(ParamType.INT, count--);
                params.addParam(ParamType.INT, sSessId);
                params.addParam(ParamType.STRING, getEntityId(sObjName, PM_NODE.ASSOC.value));
                update(UPDATE_OPEN_OBJ_COUNT, params);
                return SQLPacketHandler.getSuccessPacket();
            }else{
                params.clearParams();
                params.addParam(ParamType.INT, sSessId);
                params.addParam(ParamType.STRING, getEntityId(sObjName, PM_NODE.ASSOC.value));
                delete(DELETE_OPEN_OBJ, params);
                return SQLPacketHandler.getSuccessPacket();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while adding open object: "
                    + e.getMessage());
        }
    }

    public Packet assignObjToOattrWithProp(String sSessId, String sProcId,
                                           String sObjName, String sProp) {
        String sOaId;
        String sOattrId;
        if (sProp==null) {
            return failurePacket("Null Properties");
        }
        try {
            sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
            if (sOattrId == null) {
                return failurePacket("No object attribute with property \"" + sProp
                        + "\"!");
            }
            sOaId = getEntityId(sObjName,PM_NODE.OATTR.value);
            if (sOaId == null) {
                return failurePacket("No object attribute or no object of name \""
                        + sObjName + "\"!");
            }
            if(assignInternal(sOaId,PM_NODE.OATTR.value, sOattrId,
                    PM_NODE.OATTR.value)){
                return Packet.getSuccessPacket();
            }else{
                return failurePacket("could not assign obj to oattr");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet assignObjToOattr(String sSessId, String sProcId,
                                   String sObjName, String sOattrName) {
        String sOattrId;
        String sOaId;
        try {
            sOattrId = getEntityId(sOattrName, PM_NODE.OATTR.value);
            if (sOattrId == null) {
                return failurePacket("No object attribute with the name \""
                        + sOattrName + "\"!");
            }
            sOaId = getEntityId(sObjName,PM_NODE.OATTR.value);
            if (sOaId == null) {
                return failurePacket("No object attribute or no object of name \""
                        + sObjName + "\"!");
            }
            if(assignInternal(sOaId,PM_NODE.OATTR.value, sOattrId,
                    PM_NODE.OATTR.value)){
                return Packet.getSuccessPacket();
            }else{
                return failurePacket("could not complete assignment for obj to oattr");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet deassignObjFromOattrWithProp(String sSessId, String sProcId,
                                               String sObjName, String sProp) {
        String sOaId;
        String sOattrId;
        if (sProp==null || sProp.isEmpty()) {
            return failurePacket("Null properties");
        }
        try {
            sOattrId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
            if (sOattrId == null) {
                return failurePacket("No object attribute with property \"" + sProp
                        + "\"!");
            }
            sOaId = getEntityId(sObjName,PM_NODE.OATTR.value);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        if (sOaId == null) {
            return failurePacket("No object attribute or no object of name \""
                    + sObjName + "\"!");
        }
        return deleteAssignment(sSessId, sProcId, sOaId,PM_NODE.OATTR.value,
                sOattrId,PM_NODE.OATTR.value, NO);
    }

    public Packet isObjInOattrWithProp(String sSessId, String sObjName,
                                       String sProp) {
        Packet res = new Packet();
        if (sProp==null) {
            return failurePacket("Null Properties");
        }
        try {
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

            if (attrIsAscendant(sOaId, sOattrId)) {
                res.addItem(ItemType.RESPONSE_TEXT, YES);
            } else {
                res.addItem(ItemType.RESPONSE_TEXT, NO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet in isObjInattrWithProp()!" + e.getMessage());
        }
        return res;
    }

    public Packet getInboxMessages(String sSessId) {
        return getMailboxMessages(sSessId, "inboxof=");
    }

    public Packet getOutboxMessages(String sSessId) {
        return getMailboxMessages(sSessId, "outboxof=");
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

    // Create a PM (logical) object for a given file within a given container.

    public Packet createObjForFile(String sSessId, String sFilePath,
                                   String sContName) {
        System.out.println("Entering createObjForFile");
        System.out.println("    sFilePath = " + sFilePath);
        System.out.println("    sContName = " + sContName);

        // First get the file's name.
        File f = new File(sFilePath);
        String sFileName = f.getName();
        String sContId;
        String sHostName;
        System.out.println("    sFileName = " + sFileName);

        // Now get a name for the object.
        String sObjName;
        try {
            sObjName = getUniqueObjName(sFileName);
            System.out.println("    sObjName = " + sObjName);

            // Get the file host.
            sHostName = getSessionHostName(sSessId);

            sContId = getEntityId(sContName,PM_NODE.OATTR.value);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        if (sContId == null) {
            return failurePacket("No container " + sContName);
        }

        // Now create the object.
        Integer objId =  addObjectInternal(sObjName, null, null,
                sObjName, sObjName, sContId, PM_NODE.OATTR.value, PM_CLASS_FILE_NAME,
                null, sHostName, sFilePath, null, null, false, null, null,
                null, null, null, null, null);
        if(objId == null){
            return fail();
        }

        Packet result = new Packet();
        try {
            result.addItem(ItemType.RESPONSE_TEXT, sObjName + PM_FIELD_DELIM + objId);
        } catch (PacketException e) {
            return failurePacket("error building result packet");
        }
        return result;
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
        String sBaseContId;
        String sContName;
        try {
            sContName = getUniqueObjName(sFolderName);
            System.out.println("    sContName = " + sContName);

            // We need the id of the base container.
            sBaseContId = getEntityId(sBaseContName,PM_NODE.OATTR.value);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        if (sBaseContId == null) {
            return failurePacket("No container " + sBaseContName);
        }

        // Now create the new container.
        Integer oattrId =  addOattrInternal(sContName, null, sContName,
                sContName, sBaseContId, PM_NODE.OATTR.value, null, null);
        if(oattrId == null){
            return fail();
        }
        Packet result = new Packet();
        try {
            result.addItem(ItemType.RESPONSE_TEXT, sContName + PM_FIELD_DELIM + oattrId);
        } catch (PacketException e) {
            return failurePacket("failed creating reuslt packet");
        }
        return result;
    }

    /**
     * Get the user attributes that are direct ascendants of a base node. The base node
     * can be a user attribute, the connector, a policy class, or an operation set.
     * @param sBaseName name of the base node
     * @param sBaseType type of the base node
     * @return a Packet containing the <name>:<id> of direct ascendants of the given base node
     */
    public Packet getDascUattrs(String sBaseName, String sBaseType) {
        try{
            String sBaseId = getEntityId(sBaseName, sBaseType);
            if (sBaseId == null) {
                return failurePacket("No PM entity \"" + sBaseName + "\" of type "
                        + sBaseType);
            }

            Packet result = new Packet();

            ArrayList<Integer> uattrs = getFromAttrs(sBaseId, PM_NODE.UATTR.value, DEPTH);

            for(Integer i : uattrs){
                result.addItem(ItemType.RESPONSE_TEXT,
                        getEntityName(String.valueOf(i), PM_NODE.UATTR.value)
                                + PM_FIELD_DELIM
                                + i);

            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    /**
     * Get the users that are direct ascendants of a base node. The base node
     * can be a user attribute or the connector.
     * @param sBaseName base node name
     * @param sBaseType base node type
     * @return a Packet containing the <name>:<id> of the direct ascendants of the base node
     */
    public Packet getDascUsers(String sBaseName, String sBaseType) {
        try {
            String sBaseId = getEntityId(sBaseName, sBaseType);
            if (sBaseId == null) {
                return failurePacket("No PM entity \"" + sBaseName + "\" of type "
                        + sBaseType);
            }
            Packet result = new Packet();

            ArrayList<Integer> uattrs = getFromAttrs(sBaseId, PM_NODE.USER.value, DEPTH);

            for(Integer i : uattrs){
                try {
                    result.addItem(ItemType.RESPONSE_TEXT,
                            getEntityName(String.valueOf(i), PM_NODE.USER.value)
                                    + PM_FIELD_DELIM
                                    + i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    /**
     * Get the object attributes that are direct ascendants of a base node AND NOT OBJECTS.
     * The base node can be an object attribute, the connector, or a policy class.
     * @param sBaseName name of the base node
     * @param sBaseType type of the base node
     * @return a Packet containg the <name>:<id> of the direct ascendants of a node that are object attributes
     */
    public Packet getDascOattrs(String sBaseName, String sBaseType) {
        try{
            String sBaseId = getEntityId(sBaseName, sBaseType);
            if (sBaseId == null) {
                return failurePacket("No PM entity \"" + sBaseName + "\" of type "
                        + sBaseType);
            }
            Packet result = new Packet();

            ArrayList<Integer> uattrs = getFromAttrs(sBaseId, PM_NODE.OATTR.value, DEPTH);

            for(Integer i : uattrs){

                result.addItem(ItemType.RESPONSE_TEXT,
                        getEntityName(String.valueOf(i), PM_NODE.OATTR.value)
                                + PM_FIELD_DELIM
                                + i);
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    /**
     * Get the object attributes that are direct ascendants of a base node AND ALSO OBJECTS.
     * The base node can be an object attribute, the connector, or a policy class.
     * @param sBaseName name of the base node
     * @param sBaseType type of the base node
     * @return a Packet containg the <name>:<id> of the direct ascendants of a node that are objects
     */
    public Packet getDascObjects(String sBaseName, String sBaseType) {
        try {
            String sBaseId = getEntityId(sBaseName, sBaseType);
            if (sBaseId == null) {
                return failurePacket("No PM entity \"" + sBaseName
                        + "\" of type " + sBaseType);
            }
            Packet result = new Packet();
            HashSet hsPreds = findPredNodes(sBaseId);
            Iterator<Integer> it = hsPreds.iterator();
            Integer id;
            String name;
            while(it.hasNext()){
                id = it.next();
                name = getEntityName(id.toString(), PM_NODE.ASSOC.value);
                result.addItem(ItemType.RESPONSE_TEXT,name+ PM_FIELD_DELIM + id);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    /**
     * Get the operation sets that are direct ascendants of a base node.
     * The base node can be an object attribute, the connector.
     * @param sBaseName name of the base node
     * @param sBaseType type of the base node
     * @return a Packet containg the <name>:<id> of the direct ascendants of a node that are opsets
     */
    public Packet getDascOpsets(String sBaseName, String sBaseType) {//TODO
        try {
            String sBaseId = getEntityId(sBaseName, sBaseType);
            if (sBaseId == null) {
                return failurePacket("No PM entity \"" + sBaseName
                        + "\" of type " + sBaseType);
            }

            Packet result = new Packet();

            ArrayList<Integer> opsets = getFromAttrs(sBaseId, null, 1);

            for (Integer i : opsets) {
                result.addItem(ItemType.RESPONSE_TEXT,
                        getEntityName(String.valueOf(i), PM_NODE.OPSET.value)
                                + PM_FIELD_DELIM + i);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
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
    // Gopi Still being called from a right click menu - need to revisit
    public Packet getContainersOf(String sSessId, String sBaseName, String sBaseId,
                                  String sBaseType, String sGraphType) {
        if (sBaseType == null) {
            return failurePacket("Null base node type in getContainersOf()");
        }
        String sUserId = null;
        try {
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

            sUserId = getSessionUserIdInternal(sSessId);
            System.out.println("getContainersOf, user is " + getEntityName(sUserId, PM_NODE.USER.value));
            System.out.println("Base type: " + sBaseType);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        Packet result = new Packet();

        try {
            getToAttrs(sBaseId, null, DEPTH);

        } catch (Exception e1) {
            e1.printStackTrace();
        }

        try {
            // POLICY.
            if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
                ArrayList<Integer> toAttrs = getToAttrs(sBaseId, PM_NODE.CONN.value, DEPTH);

                // Add the connector.
                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.CONN.value));
                    }
                }

                // USER ATTRIBUTE.
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                ArrayList<Integer> toAttrs = getToAttrs(sBaseId, PM_NODE.UATTR.value, DEPTH);

                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.UATTR.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.UATTR.value));
                    }
                }

                // Add the policies.
                toAttrs = getToAttrs(sBaseId, PM_NODE.POL.value, DEPTH);

                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.POL.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.POL.value));
                    }
                }

                // Add the connector.
                toAttrs = getToAttrs(sBaseId, PM_NODE.CONN.value, DEPTH);

                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.CONN.value));
                    }
                }

                // Add the operation sets if graph type is correct.
                if (sGraphType.equalsIgnoreCase(PM_GRAPH_CAPS)) {
                    toAttrs = getToAttrs(sBaseId, PM_NODE.OPSET.value, DEPTH);

                    if (toAttrs != null) {
                        for (Integer attr : toAttrs) {
                            String sId = String.valueOf(attr);
                            result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.OPSET.value
                                    + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                    + getEntityName(sId,PM_NODE.OPSET.value));
                        }
                    }
                }

                // USER.
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.USER.value)) {
                // Add the user attributes.
                ArrayList<Integer> toAttrs = getToAttrs(sBaseId, PM_NODE.UATTR.value, DEPTH);

                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.UATTR.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.UATTR.value));
                    }
                }

                // Add the connector.
                toAttrs = getToAttrs(sBaseId, PM_NODE.CONN.value, DEPTH);

                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.CONN.value));
                    }
                }

                // OBJECT ATTRIBUTE OR ASSOCIATE.
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)
                    || sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
                // Add the object attributes.
                ArrayList<Integer> toAttrs = getToAttrs(sBaseId, PM_NODE.OATTR.value, DEPTH);
                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        boolean isAcc = oattrIsAccessibleInternal(sUserId, sId);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.OATTR.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.OATTR.value)
                                + PM_FIELD_DELIM + isAcc);
                    }
                }

                // Add the connector.
                toAttrs = getToAttrs(sBaseId, PM_NODE.CONN.value, DEPTH);
                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.CONN.value)
                                + PM_FIELD_DELIM + true);
                    }
                }

                // Add the policy classes.
                toAttrs = getToAttrs(sBaseId, PM_NODE.POL.value, DEPTH);
                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.POL.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.POL.value)
                                + PM_FIELD_DELIM + true);
                    }
                }

                // OPERATION SET.
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
                // Add the object attributes.
                ArrayList<Integer> toAttrs = getToAttrs(sBaseId, PM_NODE.OATTR.value, DEPTH);
                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        boolean isAcc = oattrIsAccessibleInternal(sUserId, sId);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.OATTR.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.OATTR.value)
                                + PM_FIELD_DELIM + isAcc);
                    }
                }

                // Add the connector.
                toAttrs = getToAttrs(sBaseId, PM_NODE.CONN.value, DEPTH);
                if (toAttrs != null) {
                    for (Integer attr : toAttrs) {
                        String sId = String.valueOf(attr);
                        result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
                                + PM_FIELD_DELIM + sId + PM_FIELD_DELIM
                                + getEntityName(sId,PM_NODE.CONN.value)
                                + PM_FIELD_DELIM + true);
                    }
                }

            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getContainersOf()!");
        }
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
                                      String sPerm) {
        try {
            if (sBaseType == null)
                return failurePacket("Null (unknown) base node type in getMellContainersOf()");
            if (sBaseId == null)
                return failurePacket("Null (unknown) base node id in getMellContainersOf()");
            PmGraphNode node = graphMgr.getGraph().getNode(sBaseId);
            if (node == null)
                return failurePacket("No graph node for base with id " + sBaseId + " in getMellContainersOf()");
            String sBName = node.getName();
            if (sBaseName == null) sBaseName = sBName;
            else if (!sBaseName.equalsIgnoreCase(sBName))
                return failurePacket("Inconsistent base node in getMellContainersOf()");

            String sUserId = getSessionUserIdInternal(sSessId);
            node = graphMgr.getGraph().getNode(sUserId);
            if (node == null) return failurePacket("No graph node for user witth id " + sUserId + " in getMellContainersOf()");

            String sUserName = node.getName();
            System.out.println("getMellContainersOf, User: " + sUserName);
            System.out.println("                     Base type: " + sBaseType);
            System.out.println("                     Base name: " + sBaseName);

            Packet result = new Packet();

            // OBJECT ATTRIBUTE OR ASSOCIATE.
            if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value) ||
                    sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
                HashSet hsOas = successorOasInternal(sUserName, sUserId, sBaseName, sBaseId, sPerm);
                if (hsOas.isEmpty()) {
                    result.addItem(ItemType.RESPONSE_TEXT, PM_NODE.CONN.value
                            + PM_FIELD_DELIM
                            + PM_CONNECTOR_ID
                            + PM_FIELD_DELIM
                            + PM_CONNECTOR_NAME
                            + PM_FIELD_DELIM + "true");
                } else {
                    Iterator hsiter = hsOas.iterator();
                    while (hsiter.hasNext()) {
                        String sOaId = (String) hsiter.next();
                        String sOaName = graphMgr.getGraph().getNode(sOaId).getName();
                        String sOaType = graphMgr.getGraph().getNode(sOaId).getType();
                        result.addItem(ItemType.RESPONSE_TEXT, sOaType
                                + PM_FIELD_DELIM + sOaId
                                + PM_FIELD_DELIM + sOaName
                                + PM_FIELD_DELIM + "true");
                    }
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getMellContainersOf()!");
        }
    }
    /************************ END MELL mothods ***********************************************/

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

    // Steve - Deleted (3/6/15)
    //public Packet getMembersOf(String sBaseName, String sBaseId,
    //		String sBaseType, String sGraphType) {

    // Steve - Added (3/6/15)
    // Gopi - This is method is still being called - it should not be
    public Packet getMembersOf(String sSessId, String sBaseName, String sBaseId,
                               String sBaseType, String sGraphType) {
        Long s = System.nanoTime();
        System.out.println("********************* getMembersOf() called with parameters " +
                " sSessId = " + sSessId +
                " sBaseName = " + sBaseName +
                " sBaseId   = " + sBaseId +
                " sBaseType   = " + sBaseType +
                " sGraphType   = " + sGraphType);

        if (sBaseType == null) {
            return failurePacket("Null (unknown) base node type in getMembersOf()");
        }
        String sUserId = null;
        try {
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

            // Steve - Added (3/6/15)
            sUserId = getSessionUserIdInternal(sSessId);
            System.out.println("getMembersOf, user is " + getEntityName(sUserId,PM_NODE.USER.value));
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        Packet result = new Packet();
        List<Member> retMembers = new ArrayList<Member>();
        try {
            // For the CONNECTOR.
            if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {

                // Add the users and user attributes if correct graph type.
                if (sGraphType.equalsIgnoreCase(PM_GRAPH_CAPS)
                        || sGraphType.equalsIgnoreCase(PM_GRAPH_UATTR)) {
                    //u and ua
                    ArrayList<ArrayList<Object>> members = select(GET_MEMBERS_U_UA, sBaseId);
                    for(ArrayList<Object> row : members){
                        String type = (String) row.get(0);
                        Integer id = (Integer) row.get(1);
                        String  name = (String) row.get(2);
                        retMembers.add(new Member(type, name, id.toString()));
                    }
                }

                //p
                ArrayList<ArrayList<Object>> members = select(GET_MEMBERS_POL, sBaseId);
                for(ArrayList<Object> row : members){
                    String type = (String) row.get(0);
                    Integer id = (Integer) row.get(1);
                    String  name = (String) row.get(2);
                    retMembers.add(new Member(type, id.toString(), name));
                }

                // Add the object attributes and associates if correct graph
                // type.
                if (sGraphType.equalsIgnoreCase(PM_GRAPH_ACES)
                        || sGraphType.equalsIgnoreCase(PM_GRAPH_OATTR)) {
                    ArrayList<Integer> oaIds = getFromObjAttrs(sBaseId, sBaseType);

                    if (oaIds != null) {
                        for (Integer oaId : oaIds) {
                            String sId = String.valueOf(oaId);

                            // Is thiss object attribute accessible to the session user?
                            boolean isAcc = true;//oattrIsAccessibleInternal(sUserId, sId);

                            if (hasAssocObj(sId)) {
                                retMembers.add(new Member(PM_NODE.ASSOC.value, sId, getEntityName(sId, PM_NODE.ASSOC.value)));
                            } else {
                                retMembers.add(new Member(PM_NODE.OATTR.value, sId, getEntityName(sId, PM_NODE.OATTR.value)));
                            }
                        }
                    }

                    ArrayList<Integer> objects = getFromAttrs(sBaseId, PM_NODE.ASSOC.value, DEPTH);
                    if (objects != null) {
                        for (Integer objId : objects) {
                            String sId = String.valueOf(objId);

                            // Steve - Added (3/6/15)
                            boolean isAcc = true;//oattrIsAccessibleInternal(sUserId, sId);

                            retMembers.add(new Member(PM_NODE.ASSOC.value, sId, getEntityName(sId, PM_NODE.ASSOC.value)));
                            // Steve END - Added(3/6/15)

                        }
                    }
                }

                ArrayList<Integer> opsets = getFromOpsets(sBaseId, sBaseType);
                // Always add the operation sets.
                if (opsets != null) {
                    for (Integer opsetId : opsets) {
                        retMembers.add(new Member(PM_NODE.OPSET.value, opsetId.toString(), getEntityName(String.valueOf(opsetId),PM_NODE.OPSET.value)));
                    }
                }

                // POLICY
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
                // Add the user attributes if correct graph type.
                if (sGraphType.equalsIgnoreCase(PM_GRAPH_CAPS)
                        || sGraphType.equalsIgnoreCase(PM_GRAPH_UATTR)) {
                    ArrayList<Integer> uattrs = getFromUserAttrs(sBaseId, sBaseType);
                    if (uattrs != null) {
                        for (Integer uaId : uattrs) {
                            retMembers.add(

                                    new Member(PM_NODE.UATTR.value, uaId.toString(), getEntityName(String.valueOf(uaId),PM_NODE.UATTR.value)));
                        }
                    }
                }

                // Add the object attributes and associates if correct graph
                // type.
                if (sGraphType.equalsIgnoreCase(PM_GRAPH_ACES)
                        || sGraphType.equalsIgnoreCase(PM_GRAPH_OATTR)) {
                    ArrayList<Integer> oaIds = getFromObjAttrs(sBaseId, sBaseType);

                    if (oaIds != null) {
                        for (Integer oaId : oaIds) {
                            String sId = String.valueOf(oaId);

                            // Is thiss object attribute accessible to the session user?
                            boolean isAcc = true;//oattrIsAccessibleInternal(sUserId, sId);

                            if (hasAssocObj(sId)) {
                                retMembers.add(new Member(PM_NODE.ASSOC.value, sId, getEntityName(sId, PM_NODE.OATTR.value)));
                            } else {
                                retMembers.add(new Member(PM_NODE.OATTR.value, sId, getEntityName(sId, PM_NODE.OATTR.value)));
                            }
                        }
                    }

                    ArrayList<Integer> objects = getFromAttrs(sBaseId, PM_NODE.ASSOC.value, DEPTH);
                    if (objects != null) {
                        for (Integer objId : objects) {
                            String sId = String.valueOf(objId);

                            // Steve - Added (3/6/15)
                            boolean isAcc = true;//oattrIsAccessibleInternal(sUserId, sId);

                            retMembers.add(new Member(PM_NODE.ASSOC.value, sId, getEntityName(sId,PM_NODE.ASSOC.value)));
                            // Steve END - Added(3/6/15)

                        }
                    }
                }

                // USER ATTRIBUTE
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                ArrayList<Integer> uattrs = getFromUserAttrs(sBaseId, sBaseType);
                if (uattrs != null) {
                    for (Integer uaId : uattrs) {
                        retMembers.add(

                                new Member(PM_NODE.UATTR.value, uaId.toString(), getEntityName(String.valueOf(uaId), PM_NODE.UATTR.value)));
                    }
                }

                ArrayList<Integer> users = getFromUsers(sBaseId, sBaseType);
                if (users != null) {
                    for (Integer userId : users) {
                        retMembers.add(
                                new Member(PM_NODE.USER.value, userId.toString(), getEntityName(String.valueOf(userId), PM_NODE.USER.value)));
                    }
                }

                // OBJECT ATTRIBUTE  JOSH END
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {

                // Add the object attributes and associates.
                ArrayList<Integer> oattrs = getFromAttrs(sBaseId, PM_NODE.OATTR.value, DEPTH);
                if (oattrs != null) {
                    for (Integer oaId : oattrs) {
                        String sId = String.valueOf(oaId);

                        // Steve - Added (3/6/15)
                        boolean isAcc = true;//oattrIsAccessibleInternal(sUserId, sId);

                        if (hasAssocObj(sId))
                            retMembers.add(new Member(PM_NODE.ASSOC.value, sId, getEntityName(sId, PM_NODE.OATTR.value)));
                        else
                            retMembers.add(new Member(PM_NODE.OATTR.value, sId, getEntityName(sId, PM_NODE.OATTR.value)));
                        // Steve END - Added(3/6/15)

                    }
                }

                ArrayList<Integer> objects = getFromAttrs(sBaseId, PM_NODE.ASSOC.value, DEPTH);
                if (objects != null) {
                    for (Integer objId : objects) {
                        String sId = String.valueOf(objId);

                        // Steve - Added (3/6/15)
                        boolean isAcc = true;//oattrIsAccessibleInternal(sUserId, sId);

                        retMembers.add(new Member(PM_NODE.ASSOC.value, sId, getEntityName(sId,PM_NODE.ASSOC.value)));
                        // Steve END - Added(3/6/15)

                    }
                }

                // Add the operation sets if correct graph type.
                if (sGraphType.equalsIgnoreCase(PM_GRAPH_ACES)) {
                    ArrayList<Integer> opsets = getFromOpsets(sBaseId, sBaseType);
                    if (opsets != null) {
                        for (Integer opsetId : opsets) {
                            String sId = String.valueOf(opsetId);
                            retMembers.add(

                                    new Member(PM_NODE.OPSET.value, sId, getEntityName(sId,PM_NODE.OPSET.value)));
                        }
                    }
                }

                // OBJECT ATTRIBUTE ASSOCIATED TO AN OBJECT
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
                // Add the operation sets if correct graph type.
                if (sGraphType.equalsIgnoreCase(PM_GRAPH_ACES)) {
                    ArrayList<Integer> opsets = getFromAttrs(sBaseId, PM_NODE.OPSET.value, DEPTH);
                    if (opsets != null) {
                        for (Integer opsetId : opsets) {
                            String sId = String.valueOf(opsetId);
                            retMembers.add(

                                    new Member(PM_NODE.OPSET.value, sId, getEntityName(sId,PM_NODE.OPSET.value)));
                        }
                    }
                }

                // OPERATION SET.
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
                // Add the user attributes.
                ArrayList<Integer> uattrs = getFromUserAttrs(sBaseId, sBaseType);
                if (uattrs != null) {
                    for (Integer uaId : uattrs) {
                        String sId = String.valueOf(uaId);
                        retMembers.add(new Member(PM_NODE.UATTR.value, sId, getEntityName(sId,PM_NODE.UATTR.value)));
                    }
                }
            }
            Collections.sort(retMembers);
            System.out.println("--------getMembersOf Results for " + sBaseName + "--------");
            for(Member m : retMembers){
                result.addItem(ItemType.RESPONSE_TEXT, m.toString());
                System.out.println(m);
            }
            System.out.println("--------getMembersOf Results for " + sBaseName + "--------");
            Long e = System.nanoTime();
            System.out.println("getMembersOf time = " + (double)((e-s)/1000000000.0f));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getMembersOf()");
        }
    }

    class Member implements Comparable<Member>{

        private String name;
        private String type;
        private String id;

        private Member(String t, String i, String n){
            name = n;
            type = t;
            id = i;
        }

        @Override
        public String toString(){
            return type + PM_FIELD_DELIM + id + PM_FIELD_DELIM + name;
        }

        @Override
        public int compareTo(Member m) {
            if(!this.type.equals(m.type)){
                return this.type.compareTo(m.type);
            }

            return this.name.toLowerCase().compareTo(m.name.toLowerCase());
        }
    }

    public Packet getMellMembersOf(String sSessId, String sBaseName, String sBaseId, String sBaseType,
                                   String sGraphType) {
        try {
            if (sBaseType == null) return failurePacket("Null (unknown) base node type in getMellMembersOf()");
            if (sBaseId == null) return failurePacket("Null (unknown) base node id in getMellMembersOf()");
            PmGraphNode node = graphMgr.getGraph().getNode(sBaseId);
            if (node == null) return failurePacket("No graph node for base with id " + sBaseId + " in getMellMembersOf()");
            String sBName = node.getName();
            if (sBaseName == null) sBaseName = sBName;
            else if (!sBaseName.equalsIgnoreCase(sBName)) return failurePacket("Inconsistent base node in getMellMembersOf()");

            String sUserId = getSessionUserIdInternal(sSessId);
            node = graphMgr.getGraph().getNode(sUserId);
            if (node == null) return failurePacket("No graph node for user with id " + sUserId + " in getMellMembersOf()");
            String sUserName = node.getName();
            System.out.println("getMellMembersOf, User: " + sUserName);
            System.out.println("                  Base type: " + sBaseType);
            System.out.println("                  Base name: " + sBaseName);

            // For the CONNECTOR.
            // The members of the connector are the initial nodes as detected by Mell algo
            // implemented in initialOasInternal().
            if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
                HashSet hsOas = initialOasInternal(sUserName, sUserId);
                Iterator hsiter = hsOas.iterator();
                List<Member> members = new ArrayList<Member>();
                while (hsiter.hasNext()) {
                    String sOaId = (String)hsiter.next();
                    String sOaName = graphMgr.getGraph().getNode(sOaId).getName();
                    String sOaType = graphMgr.getGraph().getNode(sOaId).getType();
                    if (hasAssocObj(sOaId)) sOaType = PM_NODE.ASSOC.value;

                    if(getDeniedPerms(sSessId, sUserId, null, sOaId, sOaType).contains("File read")){
                        continue;
                    }
                    members.add(new Member(sOaType, sOaId, sOaName));
                }
                Packet res = new Packet();

                Collections.sort(members);
                System.out.println("getMellMembersOf for " + sBaseName + "{");
                for(Member m : members){
                    res.addItem(ItemType.RESPONSE_TEXT, m.toString());
                    System.out.println("\t" + m);
                }
                System.out.println("}");
                return res;

            } else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
                // OBJECT ATTRIBUTE
                // The members are the oa nodes (they include objects) as computed
                // by Mell's algorithm implemented in subsequentOas().
                HashSet hsOas = subsequentOasInternal(sUserName, sUserId, sBaseName, sBaseId);
                if (hsOas == null) return failurePacket("Null result returned by subsequentOasInternal!");
                List<Member> members = new ArrayList<Member>();
                for (Object hsOa : hsOas) {
                    String sOaId = hsOa.toString();
                    String sOaName = graphMgr.getGraph().getNode(sOaId).getName();
                    String sOaType = graphMgr.getGraph().getNode(sOaId).getType();
                    if (getDeniedPerms(sSessId, sUserId, null, sOaId, sOaType).contains("File read")) {
                        continue;
                    }
                    members.add(new Member(sOaType, sOaId, sOaName));
                }
                Packet res = new Packet();

                Collections.sort(members);
                System.out.println("getMellMembersOf for " + sBaseName + "{");
                for(Member m : members){
                    res.addItem(ItemType.RESPONSE_TEXT, m.toString());
                    System.out.println("\t" + m);
                }
                System.out.println("}");
                return res;
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
                // OBJECT
                Packet res = new Packet();
                return res;
            } else {
                return failurePacket("Wrong base type in getMellMembersOf()!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet getMellMembersOf(String sUserId, String sBaseId, String sPerm) {
        try {
            PmGraphNode node = graphMgr.getNode(sBaseId);
            //if (sBaseType == null) return failurePacket("Null (unknown) base node type in getMellMembersOf()");
            if (sBaseId == null) return failurePacket("Null (unknown) base node id in getMellMembersOf()");
            //PmGraphNode node = graphMgr.getGraph().getNode(sBaseId);
            if (node == null) return failurePacket("No graph node for base with id " + sBaseId + " in getMellMembersOf()");
            String sBaseName = node.getName();
            String sBaseType = node.getType();
            //if (sBaseName == null) sBaseName = sBName;
            //else if (!sBaseName.equalsIgnoreCase(sBName)) return failurePacket("Inconsistent base node in getMellMembersOf()");

            node = graphMgr.getGraph().getNode(sUserId);
            if (node == null) return failurePacket("No graph node for user with id " + sUserId + " in getMellMembersOf()");
            String sUserName = node.getName();
            System.out.println("getMellMembersOf, User: " + sUserName);
            System.out.println("                  Base type: " + sBaseType);
            System.out.println("                  Base name: " + sBaseName);

            // For the CONNECTOR.
            // The members of the connector are the initial nodes as detected by Mell algo
            // implemented in initialOasInternal().
            if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
                HashSet hsOas = initialOasInternal(sUserName, sUserId);
                Iterator hsiter = hsOas.iterator();
                List<Member> members = new ArrayList<Member>();
                while (hsiter.hasNext()) {
                    String sOaId = (String)hsiter.next();
                    String sOaName = graphMgr.getGraph().getNode(sOaId).getName();
                    String sOaType = graphMgr.getGraph().getNode(sOaId).getType();
                    if (hasAssocObj(sOaId)) sOaType = PM_NODE.ASSOC.value;

                    if(getDeniedPerms(null, sUserId, null, sOaId, sOaType).contains(sPerm)){
                        continue;
                    }
                    members.add(new Member(sOaType, sOaId, sOaName));
                }
                Packet res = new Packet();

                Collections.sort(members);
                System.out.println("getMellMembersOf for " + sBaseName + "{");
                for(Member m : members){
                    res.addItem(ItemType.RESPONSE_TEXT, m.toString());
                    System.out.println("\t" + m);
                }
                System.out.println("}");
                return res;

            } else if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
                // OBJECT ATTRIBUTE
                // The members are the oa nodes (they include objects) as computed
                // by Mell's algorithm implemented in subsequentOas().
                HashSet hsOas = subsequentOasInternal(sUserName, sUserId, sBaseName, sBaseId);
                if (hsOas == null) return failurePacket("Null result returned by subsequentOasInternal!");
                List<Member> members = new ArrayList<Member>();
                for (Object hsOa : hsOas) {
                    String sOaId = hsOa.toString();
                    String sOaName = graphMgr.getGraph().getNode(sOaId).getName();
                    String sOaType = graphMgr.getGraph().getNode(sOaId).getType();
                    if (getDeniedPerms(null, sUserId, null, sOaId, sOaType).contains(sPerm)) {
                        continue;
                    }
                    members.add(new Member(sOaType, sOaId, sOaName));
                }
                Packet res = new Packet();

                Collections.sort(members);
                System.out.println("getMellMembersOf for " + sBaseName + "{");
                for(Member m : members){
                    res.addItem(ItemType.RESPONSE_TEXT, m.toString());
                    System.out.println("\t" + m);
                }
                System.out.println("}");
                return res;
            } else if (sBaseType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
                // OBJECT
                Packet res = new Packet();
                return res;
            } else {
                return failurePacket("Wrong base type in getMellMembersOf()!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet getConnector() {
        Packet result = new Packet();
        try {
            result.addItem(ItemType.RESPONSE_TEXT,PM_NODE.CONN.value
                    + PM_FIELD_DELIM + PM_CONNECTOR_ID + PM_FIELD_DELIM
                    + PM_CONNECTOR_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet in getConnector()");
        }
        return result;
    }

    /**
     * Add a record's template. sTplName is the template name, sContainers
     * contains the container ids separated by ":", and sKeys contains the
     * keys separated by ":".
     *
     * @param sSessId
     * @param sTplName the name of the template
     * @param sContainers the components for the template
     * @param sKeys the keys for the template (ids)
     * @return a success or failure packet
     */
    public Packet addTemplate(String sSessId, String sTplName,
                              String sContainers, String sKeys) {
        try{
            String sId = getEntityId(sTplName, PM_TEMPLATE);
            if (sId != null) {
                return failurePacket("Duplicate template name!");
            }

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sTplName);

            insert(INSERT_TEMPLATE, params);

            //add conts
            sId = getEntityId(sTplName, PM_TEMPLATE);

            String[] conts = sContainers.split(PM_FIELD_DELIM);
            int order=1;
            for(String cont: conts){
                String contId =  getEntityId(cont, PM_NODE.OATTR.value);
                if(contId == null)contId = cont;
                params.clearParams();
                params.addParam(ParamType.INT, sId);
                params.addParam(ParamType.INT, contId);
                params.addParam(ParamType.INT, order++);
                insert(ADD_TEMPLATE_CONT, params);
            }

            //add keys
            if(sKeys != null) {
                String[] tempKeys = sKeys.split(PM_FIELD_DELIM);
                for (String s : tempKeys) {
                    addTemplateKey(null, sId, s);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            return failurePacket("Could not create template " + sTplName + ". " + e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    // Each item returned is a string <tpl name>:<tpl id>.
    public Packet getTemplates(String sClientId) {
        Packet result = new Packet();

        try{
            ArrayList<ArrayList<Object>> templates = select(GET_TEMPLATES);
            for(ArrayList<Object> temp : templates){
                String name = (String) temp.get(0);
                Integer id = (Integer) temp.get(1);
                result.addItem(ItemType.RESPONSE_TEXT, name + PM_FIELD_DELIM + id);
            }
        }catch(Exception e){
            return failurePacket(e.getMessage());
        }
        return result;
    }

    // The information returned by getTemplateInfo has the following format:
    // item 0: <tpl name>:<tpl id>
    // item 1: <cont 1 id>:...:<cont n id>
    // item 2: <key1>:...:<keyn>

    public Packet getTemplateInfo(String sSessId, String sTplId) {
        Packet result = new Packet();

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sTplId);

        try {
            ArrayList<Object> tempInfo = select(GET_TEMPLATE_INFO, params).get(0);
            Integer tempId = (Integer)tempInfo.get(0);
            String tempName = (String)tempInfo.get(1);
            result.addItem(ItemType.RESPONSE_TEXT, tempName + PM_FIELD_DELIM + tempId);

            ArrayList<Integer> conts = extractIntegers(select(GET_TEMPLATE_CONTS, params));
            String comps = "";
            for(int i = 0; i < conts.size()-1; i++){
                comps += conts.get(i) + PM_FIELD_DELIM;
            }
            comps += conts.get(conts.size()-1);
            result.addItem(ItemType.RESPONSE_TEXT, comps);

            ArrayList<String> keys = extractStrings(select(GET_TEMPLATE_KEYS, params));
            String sKeys = "";
            for(int i = 0; i < keys.size()-1; i++){
                sKeys += keys.get(i) + PM_FIELD_DELIM;
            }
            sKeys += keys.get(keys.size() - 1);

            result.addItem(ItemType.RESPONSE_TEXT, sKeys);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return result;
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
        try {
            try {
                sBaseName = getEntityName(sBase, sBaseType);
            }catch(Exception e){
                sBaseId = getEntityId(sBase, sBaseType);
                if (sBaseId == null) {
                    return failurePacket("No node of type " + sBaseType
                            + " with name or id " + sBase);
                }
            }
            if (sBaseName != null) {
                sBaseId = sBase;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        Packet result = null;
        try {
            result =  addOattr(sSessId, sProcId, sRecName, sRecName,
                    sRecName, sBaseId, sBaseType, NO, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception during record creation: "
                    + e.getMessage());
        }

        // If successful, the result contains the name and id of the new record.
        if (result.hasError()) {
            return result;
        }
        String sLine = result.getStringValue(0);
        String[] pieces = sLine.split(PM_FIELD_DELIM);

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

        try {
            String newRecordName = pieces[0];
            String newRecordId = pieces[1];
            String keys = null;
            if (sKeys != null) {
                for (int j = 0; j < sKeys.length; ++j) {
                    keys = j == 0 ? sKeys[j] : keys + ":" + sKeys[j];
                }
            }
            assert sTplId != null;
            update(UPDATE_RECORD_TPL, Integer.valueOf(sTplId), Integer.valueOf(newRecordId));
            addCompsToOattrInternal(newRecordId, sComponents);
            addRecordKeys(null, newRecordName, sKeys);
        }
        catch (Exception e) {
            e.printStackTrace();
            return Packet.failurePacket("Exception while calling stored procedure : " + e.getMessage());
        }
        return result;
    }

    public Packet createRecordInEntityWithProp(String sSessId, String sProcId,
                                               String sRecName, String sProp, String sBaseType, String sTplId,
                                               String sComponents, String[] sKeys) {
        try {
            String sBaseId = getEntityWithPropInternal(sBaseType, sProp);
            if (sBaseId == null) {
                return failurePacket("No entity of type " + sBaseType
                        + " with property " + sProp);
            }
            return createRecord(sSessId, sProcId, sRecName, sBaseId, sBaseType,
                    sTplId, sComponents, sKeys);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
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

        Packet result = new Packet();

        try {
            MySQL_Parameters params = new MySQL_Parameters();
            ArrayList<ArrayList<Object>> recs = null;
            if (sTplId == null) {
                recs = select(GET_RECORDS);
            } else if (sKey == null) {
                params.addParam(ParamType.INT, sTplId);
                recs = select(GET_TPL_RECORDS, params);
            } else {
                params.addParam(ParamType.INT, sTplId);
                params.addParam(ParamType.STRING, sKey);
                recs = select(GET_TPL_RECORDS_WITH_KEY, params);
            }

            for(ArrayList<Object> rec : recs){
                String sName = (String) rec.get(0);
                Integer sId = (Integer) rec.get(1);
                result.addItem(ItemType.RESPONSE_TEXT, sName + PM_FIELD_DELIM + sId);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getCompositeObjects()");
        }
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

        try {
            ArrayList<Object> recInfo = select(GET_REC_INFO, sContId).get(0);
            String name = (String) recInfo.get(0);
            Integer id = (Integer)recInfo.get(1);
            String tempName = (String) recInfo.get(2);
            Integer tempId = (Integer) recInfo.get(3);

            if (tempId == null) {
                return failurePacket("Container " + sContId
                        + " is not a record!");
            }

            if (tempName == null) {
                return failurePacket("Inconsistency: no template with such id: "
                        + tempId);
            }

            result.addItem(ItemType.RESPONSE_TEXT, name + PM_FIELD_DELIM + id);
            result.addItem(ItemType.RESPONSE_TEXT, tempName + PM_FIELD_DELIM + tempId);

            List<Integer> components = new ArrayList<Integer>();
            ArrayList<ArrayList<Object>> recComps = select(GET_REC_COMPS, sContId);
            for(ArrayList<Object> comp : recComps){
                Integer compId = (Integer) comp.get(0);
                components.add(compId);
            }

            // The component objects: how many, then name:id.
            result.addItem(ItemType.RESPONSE_TEXT,
                    String.valueOf(components.size()));
            for (int i = 0; i < components.size(); i++) {
                String sName = getEntityName(String.valueOf(components.get(i)),PM_NODE.OATTR.value);
                if (sName == null) {
                    return failurePacket("Inconsistency: no component object (attribute) with id "
                            + components.get(i) + " exists!");
                }
                result.addItem(ItemType.RESPONSE_TEXT, sName
                        + PM_FIELD_DELIM + components.get(i));
            }

            ArrayList<String> keys = extractStrings(select(GET_RECORD_KEY, sContId));
            // Now the keys: how many, then name=value.
            if (keys == null) {
                result.addItem(ItemType.RESPONSE_TEXT, "0");
            } else {
                result.addItem(ItemType.RESPONSE_TEXT,
                        String.valueOf(keys.size()));
            }
            if (keys != null) {
                for (String key : keys) {
                    result.addItem(ItemType.RESPONSE_TEXT, key);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getRecordInfo(): "
                    + e.getMessage());
        }
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
        Packet result = new Packet();
        String sOrigId = null;
        String sOrigName = null;

        try {
            String sObjId = getObjId(sObj);
            System.out.println("Actual sObjId is " + sObjId);

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sObjId);
            ArrayList<ArrayList<Object>> info = select(GET_OBJECT_INFO, params);
            if (info==null) return null;
            ArrayList<Object> objInfo = info.get(0);
            String sName = getEntityName(String.valueOf((Integer) objInfo.get(0)), PM_NODE.ASSOC.value);
            Integer sOattrId = objInfo.get(1)==null?null:(Integer) objInfo.get(1);
            String sClass = objInfo.get(3)==null?null:(String) objInfo.get(3);
            Integer include_ascedents = objInfo.get(2)==null?null:(Integer) objInfo.get(2);
            String sIncludes = (include_ascedents==null || include_ascedents==0 ? "NO" : "YES");

            if (sClass.equals(PM_CLASS_FILE_NAME)
                    || sClass.equals(PM_CLASS_DIR_NAME)) {
                ArrayList<ArrayList<Object>> hostAndPath= select(GET_HOST_AND_PATH, params);
                objInfo = hostAndPath==null?null:hostAndPath.get(0);
                String sHost = objInfo.get(0)==null?null:(String) objInfo.get(0);
                String sPath = objInfo.get(1)==null?null:(String) objInfo.get(1);
                result.addItem(ItemType.RESPONSE_TEXT, sName
                        + PM_ALT_FIELD_DELIM + sOattrId + PM_ALT_FIELD_DELIM
                        + sClass + PM_ALT_FIELD_DELIM + sIncludes
                        + PM_ALT_FIELD_DELIM + sHost + PM_ALT_FIELD_DELIM
                        + sPath);
            } else if (sClass.equals(PM_CLASS_USER_NAME)
                    || sClass.equals(PM_CLASS_UATTR_NAME)
                    || sClass.equals(PM_CLASS_OBJ_NAME)
                    || sClass.equals(PM_CLASS_OATTR_NAME)
                    || sClass.equals(PM_CLASS_POL_NAME)
                    || sClass.equals(PM_CLASS_CONN_NAME)
                    || sClass.equals(PM_CLASS_OPSET_NAME)) {
                if (sOattrId!=null) {
                    sOrigId = sOattrId.toString();
                    sOrigName = getEntityName(sOrigId, PM_NODE.OATTR.value);
                }
                result.addItem(ItemType.RESPONSE_TEXT, sName
                        + PM_ALT_FIELD_DELIM + sOattrId + PM_ALT_FIELD_DELIM
                        + sClass + PM_ALT_FIELD_DELIM + sIncludes
                        + PM_ALT_FIELD_DELIM + sOrigName + PM_ALT_FIELD_DELIM
                        + sOrigId);

            } else {
                result.addItem(ItemType.RESPONSE_TEXT, sName
                        + PM_ALT_FIELD_DELIM + sOattrId + PM_ALT_FIELD_DELIM
                        + sClass + PM_ALT_FIELD_DELIM + sIncludes);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getObjInfo(): " + e.getMessage());
        }
    }

    public Packet getNodePropertyValue(String nodeId, String propName){
        Packet p = new Packet();
        try {
            String value = extractStrings(select(GET_NODE_PROP_VALUE, nodeId, propName)).get(0);
            p.addItem(ItemType.RESPONSE_TEXT, value);
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet getObjProperties(String sObjOrObjAttribute) {
        System.out.println("Calling getObjProperties(" + sObjOrObjAttribute + ")");
        Packet result = new Packet();
        try {
            String sObjId = getObjId(sObjOrObjAttribute);
            System.out.println("Object ID is "+sObjId);
            HashSet props = getProps(sObjId, PM_NODE.OATTR.value);
            if (props != null) {
                StringBuilder propsBuilder = new StringBuilder();
                for (Object obj : props) {
                    propsBuilder.append(obj);
                    propsBuilder.append(PM_ALT_FIELD_DELIM);
                }
                String propsString = propsBuilder.toString();
                result.addItem(ItemType.RESPONSE_TEXT, propsString);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return failurePacket(e.getMessage());
        }
        return result;
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
            String[] pieces = sLine.split(PM_FIELD_DELIM);
            savedRecords.add(pieces[1]);
        }

        Packet result = new Packet();
        try {
            exportTemplates(result);
            exportRecordProperties(result);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return result;
    }

    /**
     * Get the users that are assigned to a uattr
     * @param sUattr the ID of the base uattr
     * @return a Packet containing all of the users that are assigned to a given uattr
     */
    public Packet getUsersOf(String sUattr) {
        Packet result = new Packet();
        try {
            String sUattrId = getEntityId(sUattr, PM_NODE.UATTR.value);
            if (sUattrId == null) {
                return failurePacket("No user attribute " + sUattr);
            }
            HashSet<Integer> users = new HashSet<Integer>();
            getMemberUsers(sUattrId, users);
            Iterator<Integer> iter = users.iterator();
            while (iter.hasNext()) {
                String sUserId = String.valueOf(iter.next());
                String sUserName = getEntityName(sUserId, PM_NODE.USER.value);
                result.addItem(
                        ItemType.RESPONSE_TEXT,
                        sUserName + PM_FIELD_DELIM
                                + String.valueOf(sUserId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building result packet!");
        }
        return result;
    }


    // A record is an object container associated with:
    // - a template that tells the number, order, and names of the columns.
    // - pointers to the fields, which are the objects within the container
    // and must be contained in the appropriate columns.
    // - keys that allow relatively fast retrieval of the record.
    // This method sets the keys of a record.

    public Packet setRecordKeys(String sSessId, String sRecName, String[] sKeys) {

        // The record is an object container. Must exist and must be a record:
        String sId;
        try {
            sId = getEntityId(sRecName, PM_NODE.OATTR.value);
            if (sId == null) {
                return failurePacket("No such record " + sRecName);
            }
            if (!isRecord(sId)) {
                return failurePacket(sRecName + " is not a record!");
            }

            if (sKeys == null || sKeys.length == 0) {
                return SQLPacketHandler.getSuccessPacket();
            }

            String keys = null;
            if (sKeys != null) {
                for (int j = 0; j < sKeys.length; ++j) {
                    keys = j == 0 ? sKeys[j] : keys + ":" + sKeys[j];
                }
            }
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, Integer.valueOf(sId));
            params.addParam(ParamType.STRING, keys);
            System.out.println("Calling procedure create Record keys");
            executeStoredProcedure(CREATE_RECORD_KEYS, params);
            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public List<String> getIntersectionInternal(String columnId, String rowId) throws Exception {
        List<Integer> inters =  extractIntegers(select(GET_INTERSECTION, columnId, rowId));
        List<String> ret = new ArrayList<String>();
        for(Integer i : inters){
            ret.add(i.toString());
        }
        return ret;
    }

    public Packet getIntersection(String columnId, String rowId){
        try {
            Packet p = new Packet();
            List<String> inters = getIntersectionInternal(columnId, rowId);
            for (String s : inters) {
                p.addItem(ItemType.RESPONSE_TEXT, s);
            }
            return p;
        }catch (Exception e){
            e.getMessage();
            return failurePacket(e.getMessage());
        }
    }

    public Packet addRecordKeys(String sSessId, String sRecName, String[] sKeys) {

        // The record is an object container. Must exist and must be a record:
        String sId;
        try {
            sId = getEntityId(sRecName,PM_NODE.OATTR.value);
            if (sId == null) {
                return failurePacket("No such record " + sRecName);
            }
            if (!isRecord(sId)) {
                return failurePacket(sRecName + " is not a record!");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            return failurePacket(e1.getMessage());
        }

        if (sKeys == null || sKeys.length == 0) {
            return SQLPacketHandler.getSuccessPacket();
        }

        int n = sKeys.length;
        for (int i = 0; i < n; i++) {
            Packet p = addRecordKey(sSessId, sRecName, sKeys[i]);
            if(p.hasError()){
                return failurePacket("Unable to add the key t " + sRecName + "!");
            }
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet getProperties(String sSessId) {
        Packet result = new Packet();
        String sName;
        String sValue;
        try {
            ArrayList<ArrayList<Object>> properties = select(GET_ALL_PROPERTIES);
            if (properties== null || properties.size() == 0) {
                return null;
            }
            for(ArrayList<Object> property : properties){
                sName = (String) property.get(0);
                sValue = (String) property.get(0);
                result.addItem(ItemType.RESPONSE_TEXT, sName + PM_PROP_DELIM + sValue);
            }
            return result;
        } catch (Exception e) {
            return failurePacket("Exception in getProperties(): " + e.getMessage());
        }
    }

    public Packet deleteProperty(String sSessId, String sPropName) {
        try {
            deleteProperty(sPropName);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Could not delete property with name " + sPropName + "." + e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet setProperty(String sSessId, String sPropName,
                              String sPropValue) {
        try{
            insertOrModifyProperty(sPropName, sPropValue, null);
        }catch(Exception e){
            return SQLPacketHandler.getFailurePacket("Exception in setProperty()" + e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet getPropertyValue(String sSessId, String sPropName) {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sPropName);
        ArrayList<ArrayList<Object>> returned = null;
        try {
            returned = select(GET_PROPERTY_VALUE, params);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in getProperty() (reading)!" + e.getMessage());
        }
        String propValue = (String)returned.get(0).get(0);

        if (propValue == null || propValue.length() == 0) {
            return failurePacket("Property " + sPropName + " has no value!");
        }

        Packet result = new Packet();
        try {
            result.addItem(ItemType.RESPONSE_TEXT, propValue);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return result;
    }

    public Packet getProperty(String sSessId, String sPropName) {
        return getPropertyValue(sSessId, sPropName);
    }


    //NDK added this

    public Packet audit(String sSessId, String sEvent, String sObjId,
                        String sResult) {
        try {
            String sUserId = getSessionUserIdInternal(sSessId);
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
            String sObjName = getEntityName(sObjId, PM_OBJ);
            System.out.println("Audit method in the PMEngine************!!!!!!!!!!!");
            Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction, result, sDesc, sObjId, sObjName);
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }
    //My section stops here



    // *****************************************************************************************************
    // COMMAND METHODS END
    // *****************************************************************************************************

    // Returns the list of direct containers - id, type, name
    public List<PmGraphNode> getFirstLeveContainerList(Integer sId) throws Exception{
        if (sId == null || sId.equals("")) {
            return null;
        }
        Integer containerId;
        String ContainerType, containerName;
        List<PmGraphNode> containers = new ArrayList<PmGraphNode>();
        ArrayList<ArrayList<Object>> result = null;
        MySQL_Parameters params = new MySQL_Parameters();

        params.addParam(ParamType.INT, sId == null ? null : Integer.valueOf(sId));
        result = select(GET_1ST_LEVEL_ASSIGNMENT_INFO, params);
        if (result != null) {

            for (int i=0;i<result.size();i++) {
                containerId = (Integer) result.get(i).get(0);
                ContainerType = (String) result.get(i).get(1);
                containerName = (String) result.get(i).get(2);
                containers.add(new PmGraphNode(ContainerType, containerName, containerId.toString(), null, null, null));
            }
        }
        return containers;
    }

    // Returns the list of direct containers of a user, user attribute, or
    // object attribute.
    public String getContainerList(String sId, String sType) throws Exception{
        if(sId == null || sId.equals("")) {
            return null;
        }
        if (!sType.equalsIgnoreCase(PM_NODE.USER.value) &&
                !sType.equalsIgnoreCase(PM_NODE.UATTR.value) &&
                !sType.equalsIgnoreCase(PM_NODE.OATTR.value) &&
                !sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            return null;
        }

        ArrayList<ArrayList<Object>> containers = null;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId == null ? null : Integer.valueOf(sId));
        containers = select(GET_ASSIGNMENTS, params);
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        if (containers == null || containers.size() == 0) {
            return null;
        }
        for(ArrayList<Object> container : containers){
            if (first) {
                sb.append(String.valueOf(container.get(0)));
                first = false;
            } else {
                sb.append("," + String.valueOf(container.get(0)));
            }
        }
        return sb.toString();
    }



    // First empty almost all containers. Exceptions:
    // The HostContainer, the NameContainer.
    // Also, do not delete the current session in which the Admin Tool is
    // executing.

    public void reset(String sSessId) {
        System.out.println("reseting data...");

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sSessId);
        try {
            executeStoredProcedure(RESET_DATA, params);

            graphMgr.clearGraph();
            denyMgr.clearDenies();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }


    public boolean entityExists(String nameOrId, String sType) throws Exception {
        String id = getEntityId(nameOrId, sType);
        if (id == null) {
            String name = getEntityName(nameOrId, sType);
            return (name != null);
        } else {
            return true;
        }
    }

    public boolean entityExists(Integer id) throws Exception{
        MySQL_Parameters params = null;
        String name;
        params = new MySQL_Parameters();
        params.setOutParamType(ParamType.STRING);
        params.addParam(ParamType.INT, id);
        name = (String)executeFunction(GET_NAME_OF_ENTITY_WITH_ID, params);
        if (name == null || name.length() == 0) {
            return false;
        }
        return true;
    }

    // sId may be null, in which case the engine generates an id. Sometimes,
    // we want the pc to have a certain id (e.g., when the initial objects
    // are created.

    public Integer addPcInternal(String sName, String sId, String sDescr,
                                 String sInfo, String[] sProps) {
        Integer newNodeId=null;
        try {
            // Test if duplicate name.
            if (entityNameExists(sName,PM_NODE.POL.value)) {
                errorMessage = "Policy with duplicate name";
                return null;
            }

            // Create a policy Node
            newNodeId = createNode(sName, PM_NODE.POL.value, sDescr, Integer.valueOf(PM_CONNECTOR_ID));


            if(sProps != null && sProps.length > 0) {
                for(int i = 0; i < sProps.length; i++) {
                    if (!sProps[i].contains(PM_PROP_DELIM)) {
                        errorMessage = "property \"" + sProps[i] + "\" is not formatted correctly: property=value";
                        return null;
                    }
                }
            }else {
                // Add the pc's properties, if any.
                if (newNodeId != null) insertProperties(sProps, PM_NODE.POL.value, newNodeId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return null;
        }
        return newNodeId;
    }

    // SQL only does assignment, it does not need double link
    // Keeping still same name as AD
    public boolean addDoubleLink(String start, String end) {
        try {
            assign(Integer.valueOf(start), Integer.valueOf(end));
        } catch (Exception e) {
            errorMessage = e.getMessage();
            return false;
        }
        return true;
    }

    public String generatePasswordHash(String password) throws InvalidKeySpecException, NoSuchAlgorithmException{
        int iterations = 100;
        char[] chars = password.toCharArray();
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations /*+ PM_FIELD_DELIM*/ + toHex(salt) + /*PM_FIELD_DELIM +*/ toHex(hash);
    }

    public boolean checkPasswordHash(String stored, String toCheck) throws NoSuchAlgorithmException, InvalidKeySpecException{
        String part0 = stored.substring(0, 3);
        String part1 = stored.substring(3, 35);
        String part2 = stored.substring(35);
        //String[] parts = stored.split(PM_FIELD_DELIM);
        int iterations = Integer.parseInt(part0);
        byte[] salt = fromHex(part1);
        byte[] hash = fromHex(part2);

        PBEKeySpec spec = new PBEKeySpec(toCheck.toCharArray(), salt, iterations, hash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        String x = toHex(testHash);

        int diff = hash.length ^ testHash.length;
        for(int i = 0; i < hash.length && i < testHash.length; i++)
        {
            diff |= hash[i] ^ testHash[i];
            if(hash[i] != testHash[i]){
                int cx = 0;
            }
        }
        return diff == 0;
    }

    private static byte[] fromHex(String hex) throws NoSuchAlgorithmException
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    private static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }

    // Internally used, no permission checks. In general, the user id is null
    // because this is a new user. Only for initial users, like super,
    // is the id known.

    public Integer addUserInternal(String sName, String sId, String sFull,
                                   String sInfo, String sPass, String sBaseId, String sBaseType) {
        // A null password means an empty one.
        if(sName == null || sName.equals("")){
            return null;
        }
        if (sPass == null) {
            sPass = "";
        }
//		// Get a random 12-byte salt.
//		SecureRandom random = new SecureRandom();
//		byte[] salt = new byte[12];
//		random.nextBytes(salt);
//
//		// Get a message digest instance and hash the salt and the password.
//		byte[] digest;
//		try {
//			// Test if duplicate name.
////			if (entityNameExists(sName,PM_NODE.USER.value)) {
////				return failurePacket("Duplicate name!");
////			}
//			MessageDigest md = MessageDigest.getInstance("MD5");
//			md.update(salt);
//			md.update(sPass.getBytes());
//			sPass = null;
//			digest = md.digest();
//
//			// Convert the hash to a string of hex digits.
//			String sHash = UtilMethods.byteArray2HexString(salt) + UtilMethods.byteArray2HexString(digest);
        String sHash = null;
        Integer newNodeId = null;
        try{
            sHash = generatePasswordHash(sPass);

            // In general, the user id is null when called from the Admin Tool.
            // When called for the initial objects, the id is predefined.
            newNodeId = createNode(sName, PM_NODE.USER.value, sInfo, Integer.valueOf(sBaseId));

            // Create new user object.
            MySQL_Parameters params = null;
            params = new MySQL_Parameters();
            params.setOutParamType(ParamType.INT);
            params.addParam(ParamType.INT, newNodeId);
            params.addParam(ParamType.STRING, sName);
            params.addParam(ParamType.STRING, sFull);
            params.addParam(ParamType.STRING, sHash);
            params.addParam(ParamType.STRING, null);
            params.addParam(ParamType.INT, null);
            params.addParam(ParamType.INT, null);
            newNodeId = (Integer)executeFunction(CREATE_USER_FUN, params);

        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return null;
        }

        return newNodeId;
    }

    // Note that the parameter sComponents may contain either ids or names of
    // object attributes associated with objects, so we need to bring them
    // to the canonic form.
    public Integer addObjectInternal(String sName, String sId, String sAssocId,
                                     String sDescr, String sInfo, String sBaseId, String sBaseType,
                                     String sClass, String sType, String sHost, String sPath,
                                     String sOrigName, String sOrigId, boolean bInh, String sSender,
                                     String sReceiver, String sSubject, String sAttached,
                                     String sTemplateId, String sComponents, String[] sKeys) {

        Integer newNodeId = null;
        MySQL_Parameters params = new MySQL_Parameters();
        ArrayList<ArrayList<Object>> returned = null;
        Integer hostId = null;
        Integer classId = null;
        try {
            // Create Object node
            newNodeId = createNode(sName, PM_NODE.ASSOC.value, sDescr, Integer.valueOf(sBaseId));

            // We've been successful in adding the Object Node
            // get host_id and Class_id from sClass
            if (sHost != null) {
                if (!(sClass.equals(PM_CLASS_USER_NAME)
                        || sClass.equals(PM_CLASS_UATTR_NAME)
                        || sClass.equals(PM_CLASS_OBJ_NAME)
                        || sClass.equals(PM_CLASS_OATTR_NAME)
                        || sClass.equals(PM_CLASS_POL_NAME)
                        || sClass.equals(PM_CLASS_CONN_NAME)
                        || sClass.equals(PM_CLASS_OPSET_NAME))) {
                    params.addParam(ParamType.STRING, sHost);
                    ArrayList<Integer> ints = extractIntegers(select(GET_HOST_ID, params));
                    hostId = (ints == null || ints.isEmpty()) ? null : ints.get(0);
                }
            } else hostId = null;
            if (sClass != null) {
                params = new MySQL_Parameters();
                params.addParam(ParamType.STRING, sClass);
                returned = select(GET_CLASS_ID, params);
                System.out.println("returned.size() is " + returned.size());
                if (returned.size() > 0) {
                    classId = (Integer)returned.get(0).get(0);
                } else classId = null;
            } else classId = null;

            // Add Object Detail
            params = new MySQL_Parameters();
            params.addParam(ParamType.INT, newNodeId);
            params.addParam(ParamType.INT, (sOrigId==null) ? null:Integer.valueOf(sOrigId));
            params.addParam(ParamType.INT, classId);
            params.addParam(ParamType.INT, hostId);
            params.addParam(ParamType.STRING, sPath);
            params.addParam(ParamType.INT, (bInh) ? 1 : 0);
            params.addParam(ParamType.INT, sTemplateId);

            returned = executeStoredProcedure(CREATE_OBJECT_DETAIL, params);
            // Insert into Template table
            params = new MySQL_Parameters();
            if (sTemplateId != null) {
                params.addParam(ParamType.INT, sTemplateId==null?null:Integer.valueOf(sTemplateId));
                String sCanonicCompos = getCanonicList(sComponents);
                params.addParam(ParamType.INT, newNodeId);
                update(UPDATE_RECORD_TPL, params);
                addCompsToOattrInternal(newNodeId.toString(), sCanonicCompos);
            }
            // Add oa keys, if any.
            if (sKeys != null){
                if(sKeys.length > 0) {
                    //return SQLPacketHandler.getSuccessPacket();
                    int n = sKeys.length;
                    for (int i = 0; i < n; i++) {
                        params = new MySQL_Parameters();
                        params.addParam(ParamType.INT, Integer.valueOf(sId));
                        params.addParam(ParamType.STRING, sKeys[i]);
                        insert(INSERT_RECORD_KEY, params);
                    }
                }
            }
            setLastUpdateTimestamp();

            if(sSender != null && sReceiver != null){
                //object is an email
                params.clearParams();
                params.addParam(ParamType.INT, newNodeId);
                params.addParam(ParamType.STRING, sSender);
                params.addParam(ParamType.STRING, sReceiver);
                params.addParam(ParamType.STRING, sSubject);
                insert(ADD_EMAIL_DETAIL, params);

                if(sAttached != null) {
                    params.clearParams();
                    params.addParam(ParamType.INT, newNodeId);
                    params.addParam(ParamType.INT, getEntityId(sAttached, PM_NODE.OATTR.value));
                    insert(ADD_EMAIL_ATTACHMENT, params);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return null;
        }
        return newNodeId;
    }

    private boolean associationExists(String id, String oaId){
        if(id == null || id.length() < 1){
            return false;
        }
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, id);
        params.addParam(ParamType.INT, oaId);
        try {
            return (Long)select(ASSOCIATION_EXISTS, params).get(0).get(0) == 1;
        } catch (Exception e) {
            errorMessage = "error checking if association exists";
            return false;
        }
    }

    // Create a new opset with predefined id: use new name and id.
    // Create a new opset and let the engine set its id: use a new name and null
    // id.
    // Add an op to an old opset: use the old name (the id may be null).
    //sOp can be multiple ops separated by "&"
    public Integer addOpsetAndOpInternal(String sOpsetName, String sOpsetId,
                                         String sDescr, String sInfo, String sOp, String sAscId,
                                         String sAscType, String sDescId, String sDescType) {
        // Check if the opset exists.
        boolean newOpset = false;
        String sId;
        Integer opSetId;
        try {
            sId = getEntityId(sOpsetName, PM_NODE.OPSET.value);
            if (sId == null) newOpset = true;
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sDescId==null?null:Integer.valueOf(sDescId));
            params.addParam(ParamType.INT, sAscId==null?null:Integer.valueOf(sAscId));
            params.addParam(ParamType.STRING, sOpsetName);
            params.addParam(ParamType.STRING, sOp);
            System.out.println("Callinng stored proc create_Assocation");
            if(!associationExists(sId, sDescId)  && (newOpset || sDescId != PM_CONNECTOR_ID)){
                executeStoredProcedure(CREATE_ASSOCIATION, params);//create an association unless one already exists
            }
            opSetId = Integer.valueOf(getEntityId(sOpsetName, PM_NODE.OPSET.value));

            if (!newOpset && sOp!=null ) {
                System.out.println("Existing opset, trying to update ops if passed");

                // Add the operation(s) to the opset.
                String[] ops = sOp.split("&");
                for (String op : ops) {
                    if (!isOperation(op)) {
                        errorMessage = "invalid operation: " + op;
                        System.out.println(errorMessage);
                        continue;
                    }
                    // Operation cannot be duplicate.
                    if (opsetContainsOp(opSetId, sOp)) {
                        errorMessage = sOp + " Operation already in the operation set!";
                        System.out.println(errorMessage);
                        continue;
                    }
                    addOperationToOperationSet(opSetId, getOperationId(op));
                }
            }else{
                //new opset add to graph
                addPmGraphNode(sDescId, opSetId.toString(), sOpsetName, PM_NODE.OPSET.value);
                return opSetId;
            }
            updatePmGraphNode(opSetId.toString());
            return opSetId;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "error adding op to opset or a new opset";
            return null;
        }
    }

    public Integer getOperationId(String operationName) throws Exception {
        Integer operationId = null;
        ArrayList<ArrayList<Object>> returned = null;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, operationName);
        returned = select(GET_OP_ID, params);
        operationId = (returned.size() > 0)?(Integer)returned.get(0).get(0):null;
        return operationId;
    }

    public boolean entityNameExists(String sName, String sType) throws Exception {
        return getEntityId(sName, sType) != null;
    }


    // Given the name of an entity, return the id, if the entity exists,
    // otherwise null. Special care for entity name containing special
    // character *.

    public String getEntityId(String sName, String sType) throws Exception {
        String sId = null;

        if (sName == null) {
            return sId;
        }

        if (sName.equals("*")) {
            return "*";
        }

        MySQL_Parameters params = new MySQL_Parameters();
        if (sType==null || (sType.equalsIgnoreCase(PM_NODE.USER.value)
                || sType.equalsIgnoreCase(PM_NODE.UATTR.value)
                || sType.equalsIgnoreCase(PM_NODE.OATTR.value)
                || sType.equalsIgnoreCase(PM_NODE.ASSOC.value)
                || sType.equalsIgnoreCase(PM_NODE.OPSET.value)
                || sType.equalsIgnoreCase(PM_NODE.POL.value)
                || sType.equalsIgnoreCase(PM_NODE.CONN.value)
                || sType.equalsIgnoreCase(PM_OBJ))) {

            if(sType != null && sType.equals(PM_OBJ)){
                sType = PM_NODE.ASSOC.value;
            }
            params.setOutParamType(ParamType.INT);
            params.addParam(ParamType.STRING, sName);
            params.addParam(ParamType.STRING, sType);
            sId = String.valueOf(executeFunction(
                    GET_ID_OF_ENTITY_WITH_NAME_FUN, params));

            if(sId.equals("null") && sType!= null && sType.equals(PM_NODE.OATTR.value)){
                return getEntityId(sName, PM_NODE.ASSOC.value);
            }else{
                return sId.equals("null") ? null : sId;
            }

            // TODO the rest of these
        } else if (sType.equalsIgnoreCase(PM_OBJ_CLASS)) {
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sName);
            ArrayList<ArrayList<Object>> results = select(GET_CLASS_ID, params);
            return (results.size() > 0) ? String.valueOf(results.get(0).get(0))
                    : null;
        } else if (sType.equalsIgnoreCase(PM_DENY)) { //
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sName);
            ArrayList<ArrayList<Object>> results = select(GET_DENY_ID, params);
            return (results.size() > 0) ? String.valueOf(results.get(0).get(0))
                    : null;
        } else if (sType.equalsIgnoreCase(PM_SCRIPT)) { // TODO
            // sDn = "CN=" + sId + "," + sRuleContainerDN;
        } else if (sType.equalsIgnoreCase(PM_TEMPLATE)) {
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sName);
            ArrayList<ArrayList<Object>> results = select(GET_TEMPLATE_ID,
                    params);
            return (results.size() > 0) ? String.valueOf(results.get(0).get(0))
                    : null;
        } else if (sType.equalsIgnoreCase(PM_SESSION)) {
            //return sId; // return the sessionId - Gopi, please confirm
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sName);
            ArrayList<ArrayList<Object>> results = select(GET_SESSION_ID,
                    params);
            return (results.size() > 0) ?
                    String.valueOf(results.get(0).get(0))
                    : null;
        } else if (sType.equalsIgnoreCase(PM_HOST)){
            params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sName);
            ArrayList<ArrayList<Object>> results = select(GET_HOST_ID, params);
            return results.get(0).get(0).toString();
        }
        return sId;
    }

    // In general, the argument sId is null, but sometimes we may want to create
    // an object attribute with a predefined id.

    public Integer addOattrInternal(String sName, String sId, String sDescr,
                                    String sInfo, String sBaseId, String sBaseType, String sAssocObjId,
                                    String[] sProps) {
        Integer newNodeId = null;
        try {
            newNodeId = createNode(sName, PM_NODE.OATTR.value, sDescr, Integer.valueOf(sBaseId));

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, newNodeId);
            params.addParam(ParamType.INT, null);
            params.addParam(ParamType.INT, PM_CLASS_OATTR_ID);
            params.addParam(ParamType.INT, null);
            params.addParam(ParamType.STRING, null);
            params.addParam(ParamType.STRING, null);
            params.addParam(ParamType.INT, null);
            executeStoredProcedure(CREATE_OBJECT_DETAIL, params);

            // Add oa properties, if any.
            try{
                if(sProps != null && (sProps.length > 0)) {
                    insertProperties(sProps, PM_NODE.OATTR.value, newNodeId);
                }
            }catch(Exception e){
                errorMessage = "Could not add properties to oattr";
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return null;
        }
        return newNodeId;
    }


    public String getCanonicList(String sCompos) throws Exception {
        if (sCompos == null) {
            return null;
        }
        String[] pieces = sCompos.split(PM_FIELD_DELIM);
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
                sb.append(PM_FIELD_DELIM + sId);
            }
        }
        return sb.toString();
    }

    public void setLastUpdateTimestamp() {
        sLastUpdateTimestamp = dfUpdate.format(new Date());
    }

    public boolean isOperation(String sOpName) throws Exception{
        Integer operationId = null;
        ArrayList<ArrayList<Object>> returned = null;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sOpName);
        returned = select(GET_OP_ID, params);
        operationId = (returned.size() > 0)?(Integer)returned.get(0).get(0):null;

        if (operationId==null || operationId <= 0) {
            return false;
        } else return true;
    }

    // Test whether a given opset contains a given operation.

    public boolean opsetContainsOp(Integer opsetId, String sOp) throws Exception {
        Integer operationId = null;
        ArrayList<ArrayList<Object>> returned = null;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, opsetId);
        params.addParam(ParamType.INT, getOperationId(sOp));
        returned = select(OPSET_CONTAINS_OP, params);
        if (returned==null) return false;
        if(returned.size() < 1){
            return false;
        }else{
            operationId = (Integer)returned.get(0).get(0);
        }

        if (operationId==null || operationId <=0 ) return false;
        else return true;
    }

    public void addOperationToOperationSet(Integer opsetId, Integer opId) throws Exception{
        if (!opsetContainsOp(opsetId, opId.toString())) {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, opsetId);
            params.addParam(ParamType.INT, opId);
            insert(ADD_OPERATION_TO_OPSET, params);
        }
    }

    public boolean denyHasOp(String sDenyId, String sOp) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, Integer.valueOf(sDenyId));
        params.addParam(ParamType.INT, getOperationId(sOp));
        ArrayList<ArrayList<Object>> returned = select(DENY_HAS_OP, params);
        if (returned==null) return false;
        Long count = (Long)returned.get(0).get(0);

        if (count==null || count <=0 ) return false;
        else return true;
    }


    public boolean denyHasOattr(String sDenyId, String sOattrId) throws Exception {
        if (sOattrId.startsWith("!")) {
            sOattrId = sOattrId.substring(1);
        }
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sDenyId);
        params.addParam(ParamType.INT, Integer.valueOf(sOattrId));
        ArrayList<ArrayList<Object>> returned = select(DENY_HAS_OATTR, params);
        return ((Long) returned.get(0).get(0) == 1);
    }


    // Test whether a given class has a given operation.

    public boolean objClassHasOp(String sClassId, String sOp) throws Exception {
        Long count;
        ArrayList<ArrayList<Object>> returned = null;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sOp);
        params.addParam(ParamType.INT, Integer.valueOf(sClassId));
        returned = select(OBJ_CLASS_HAS_OP, params);
        count = (Long)returned.get(0).get(0);
        return count>=1?true:false;
    }

    // Check whether a user is assigned to a (user) attribute.
    public boolean userIsAssigned(String sId1, String sId2) throws Exception {
        return isAssigned(Integer.valueOf(sId1),Integer.valueOf(sId2));
    }

    // Test whether an operation set is assigned to the connector.

    public boolean opsetIsAssignedToConnector(String sId) throws Exception{
        return isAssigned(Integer.valueOf(sId),Integer.valueOf(PM_CONNECTOR_ID));
    }

    // Test whether a user is assigned (directly) to the connector.

    public boolean userIsAssignedToConnector(String sId) throws Exception{
        return isAssigned(Integer.valueOf(sId),Integer.valueOf(PM_CONNECTOR_ID));
    }

    // Test whether a user has no descendant.

    public boolean userHasNoDescendant(String sId) throws Exception{
        boolean ret = false;
        ret = !hasDescendants(sId);
        return ret;
    }


    public boolean attrHasAscendants(String sId, String sType) throws Exception {
        boolean ret = false;
        ret = hasAscendants(sId);
        return ret;

    }

    // Test whether an attribute (user or object) has descendants of type
    // attribute,
    // policy class, or connector.

    public boolean attrHasDescendants(String sId, String sType) throws Exception {
        boolean ret = false;
        ret = hasDescendants(sId);
        return ret;
    }

    public boolean uattrHasOpsets(Integer id) throws Exception {
        ArrayList<ArrayList<Object>> returned = null;
        Long hasOpset;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, id);
        params.addParam(ParamType.STRING, PM_NODE.OPSET.value);
        returned = select(UATTR_HAS_OPSET, params);
        hasOpset = (Long)returned.get(0).get(0);
        if (hasOpset > 0) return true;
        else return false;
    }

    public boolean inMemUattrHasOpsets(String sUaId) {
        HashSet hsContainers = graphMgr.getGraph().getNode(sUaId).getContainers();
        Iterator hsiter = hsContainers.iterator();
        while (hsiter.hasNext()) {
            String sId = (String)hsiter.next();
            String sType = graphMgr.getGraph().getNode(sId).getType();
            String sName = graphMgr.getGraph().getNode(sId).getName();
            System.out.println("InMemUattrHasOpsets checking if " + sName + "/" + sType + " is an opset");
            if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) return true;
        }
        return false;
    }

    public boolean inMemOattrHasOpsets(String sOaId) {
        HashSet hsMembers = graphMgr.getGraph().getNode(sOaId).getMembers();
        Iterator hsiter = hsMembers.iterator();
        while (hsiter.hasNext()) {
            String sId = (String)hsiter.next();
            String sType = graphMgr.getGraph().getNode(sId).getType();
            if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) return true;
        }
        return false;
    }


    public boolean oattrHasOpsets(Integer id) throws Exception {
        ArrayList<ArrayList<Object>> returned = null;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, id);
        params.addParam(ParamType.STRING, PM_NODE.OPSET.value);
        returned = select(OATTR_HAS_OPSET, params);
        Long opsets = (Long)returned.get(0).get(0);
        if (opsets > 0) return true;
        else return false;
    }

    // Test whether a user or object attribute is assigned (directly) to the
    // connector.

    public boolean attrIsAssignedToConnector(String sId) throws Exception {
        return isAssigned(Integer.valueOf(sId), Integer.valueOf(PM_CONNECTOR_ID));
    }

    // Determines assignment between attributes (user or object).

    public boolean attrIsAssignedToAttr(String sId1, String sId2) throws Exception  {
        return isAssigned(Integer.valueOf(sId1),Integer.valueOf(sId2));
    }

    public boolean attrIsAssignedToPolicy(String sId1, String sId2) throws Exception {
        return isAssigned(Integer.valueOf(sId1),Integer.valueOf(sId2));
    }

    // Determines whether a user attribute is assigned to an operation set.

    public boolean attrIsAssignedToOpset(String sId1, String sId2) throws Exception   {
        return isAssigned(Integer.valueOf(sId1),Integer.valueOf(sId2));
    }

    // Determines whether an operation set sId1 is assigned to an
    // object attribute sId2.

    public boolean opsetIsAssignedToAttr(String sId1, String sId2) throws Exception   {
        return isAssigned(Integer.valueOf(sId1),Integer.valueOf(sId2));
    }

    // Test whether a user attribute or an object attribute is an ascendant of a
    // policy class.

    public boolean attrIsAscendantToPolicy(String sId1, String sId2) throws Exception   {
        return isAssigned(Integer.valueOf(sId1),Integer.valueOf(sId2));
    }

    // Tests whether the user sId1 "inherits" the user attribute sId2.

    public boolean userIsAscendant(Integer id1, Integer id2) throws Exception   {
        return isAssigned(id1, id2);
    }

    // A user may be an ascendant of a policy only through some user attributes.
    public boolean userIsAscendantToPolicy(String sId1, String sId2) throws Exception   {
        return isAssigned(Integer.valueOf(sId1),Integer.valueOf(sId2));
    }

    // Determine whether an attribute is ascendant of another attribute
    // (of the same type - user or object).

    public boolean attrIsAscendant(String sId1, String sId2) throws Exception   {

        if (sId1.equals(sId2)) {
            return true;
        }
        return isAssigned(Integer.valueOf(sId1), Integer.valueOf(sId2));
    }

    private boolean isAscendant(String end, String start) throws Exception {
        return end.equals(start) || isAssigned(Integer.valueOf(end), Integer.valueOf(start));
    }

    private boolean memIsAscendant(String end, String start){
        PmGraphNode node = ServerConfig.graphMgr.getGraph().getNode(start);
        if(node == null){
            return false;
        }
        return end.equals(start) || node.getMembers().contains(end);
    }

    /**
     * Get all opsets between a given uattr and a given oattr.
     * Add their <name>:<id> to the array list specified as the third parameter.
     *
     * @param sUattrId the ID of the user attribute
     * @param sOattrId the ID of the object attribute
     * @param res the packet to store the results
     */
    public void getOpsetsBetween(String sUattrId, String sOattrId, Packet res)
            throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sUattrId);
        params.addParam(ParamType.INT, sOattrId);

        ArrayList<ArrayList<Object>> opsetsBtw = select(GET_OPSETS_BETWEEN,
                params);

        for (ArrayList<Object> opset : opsetsBtw) {
            String name = String.valueOf(opset.get(0));
            String id = String.valueOf(opset.get(1));

            res.addItem(ItemType.RESPONSE_TEXT, name
                    + PM_FIELD_DELIM + id);

        }
    }

    public boolean attrNameExists(Integer baseId, String sName, String sType) throws Exception {
        return entityNameExists(sName, sType);
    }

    public String getEntityName(String sId, String sType) throws Exception {
        if(sId == null){
            return null;
        }
        //if (!isNum(sId)) return sId; // It is a name aleady
        /*if(graphMgr.isBuilt()){
            PmGraphNode n = graphMgr.getGraph().getNode(sId);
            return n == null ? null : n.getName();
        }*/

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId);

        if (sType.equalsIgnoreCase(PM_NODE.USER.value)
                || sType.equalsIgnoreCase(PM_NODE.UATTR.value)
                || sType.equalsIgnoreCase(PM_NODE.OATTR.value)
                || sType.equalsIgnoreCase(PM_NODE.ASSOC.value)
                || sType.equalsIgnoreCase(PM_NODE.OPSET.value)
                || sType.equalsIgnoreCase(PM_NODE.POL.value)
                || sType.equalsIgnoreCase(PM_NODE.CONN.value)
                || sType.equalsIgnoreCase(PM_OBJ)) {

            /*if(graphMgr.isBuilt()){
                return graphMgr.getGraph().getNode(sId).getName();
            }*/
            params.setOutParamType(ParamType.STRING);
            return (String)executeFunction(GET_NAME_OF_ENTITY_WITH_ID, params);

            //TODO the rest of these
        } else if (sType.equalsIgnoreCase(PM_OBJ_CLASS)) {
            ArrayList<ArrayList<Object>> results = select(GET_OBJ_CLASS_NAME, params);
            return (String) results.get(0).get(0);
        } else if (sType.equalsIgnoreCase(PM_DENY)) {
            ArrayList<ArrayList<Object>> results = select(GET_DENY_NAME, params);
            return (String) results.get(0).get(0);
        } else if (sType.equalsIgnoreCase(PM_SCRIPT)) {
            ArrayList<ArrayList<Object>> results = select(GET_SCRIPT_NAME, params);
            return (String) results.get(0).get(0);
        } else if (sType.equalsIgnoreCase(PM_TEMPLATE)) {
            ArrayList<ArrayList<Object>> results = select(GET_TEMPLATE_NAME, params);
            return (String) results.get(0).get(0);
        } else if (sType.equalsIgnoreCase(PM_SESSION)) {
            ArrayList<ArrayList<Object>> results = select(GET_SESSION_NAME, params);
            return (String) results.get(0).get(0);
        } else if (sType.equalsIgnoreCase(PM_PROCESS)) {
            return sId;
        }else if (sType.equalsIgnoreCase(PM_HOST)){
            ArrayList<ArrayList<Object>> results = select(GET_HOST_NAME, params);
            return (String) results.get(0).get(0);
        } else {
            return null;
        }
    }

    // Return the set of all OBJECTS (NOT object attributes) that DIRECTLY
    // represent
    // the specified entity (regardless of whether they represent only the
    // entity
    // or its entire subgraph).

    public HashSet<String> getObjectsRepresentingEntity(String sEntId,
                                                        String sEntType) throws Exception{
        HashSet<String> prSet = new HashSet<String>();
        Integer objectId;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sEntId);
        ArrayList<ArrayList<Object>> results = select(GET_OBJ_IDS_OF_ORIG_ID, params);
        if (results != null) {
            for (int i=0;i<results.size();i++) {
                objectId = (Integer) results.get(i).get(0);
                prSet.add(objectId.toString());
            }
        }
        return prSet;
    }

    public void printSet(Set<String> hs, String sType, String caption) {
        String sName=null;
        if (caption != null && caption.length() > 0) {
            System.out.println(caption);
        }
        Iterator<String> hsiter = hs.iterator();

        System.out.print("{");
        boolean firstTime = true;
        while (hsiter.hasNext()) {
            String sId = String.valueOf(hsiter.next());
            if (sType.equalsIgnoreCase(PM_PERM)) {
                if (firstTime) {
                    System.out.print(sId);
                    firstTime = false;
                } else {
                    System.out.print(", " + sId);
                }
            } else {
                try {
                    sName = getEntityName(sId, sType);
                } catch(NumberFormatException e) {
                    sName = sId;
                } catch (Exception e) {
                    sName = sId;
                }
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
    public String getNodeType(Integer objId) throws Exception{
        if (objId==null) return null;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, objId);
        ArrayList<ArrayList<Object>> results = select(GET_NODE_TYPE_NAME, params);
        return results==null || results.isEmpty()?null:(String) results.get(0).get(0);
    }

    public String getNodeTypeValue(Integer objId) throws Exception{
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, objId);
        ArrayList<ArrayList<Object>> results = select(GET_NODE_TYPE, params);
        return (String) results.get(0).get(0);
    }
    // Get the object attribute associated to an object given by its id.

    public String getAssocOattr(String sObjId) throws Exception{
        // new implementation has only one id for object and associated object attribute
        // just check if the passed id is of true object
        String type = getNodeType(Integer.valueOf(sObjId));
        if (type==null) return null;
        if (type.equalsIgnoreCase(PM_CLASS_OBJ_NAME)) {
            return sObjId;
        } else return null;
    }


    // Get the object associated to an object attribute or null if no such
    // object exists.

    public String getAssocObj(String sOattrId) throws Exception {
        return getAssocOattr(sOattrId);
    }

    public boolean hasAssocObj(String sId) throws Exception{
        String assocObjId = getAssocObj(sId);
        if (assocObjId== null || assocObjId.length() == 0) {
            return false;
        } else return true;
    }

    public Boolean isNodeUnAssigned(Integer nodeId) throws Exception {
        Boolean isUnAssigned = false;
        MySQL_Parameters params = new MySQL_Parameters();
        params.setOutParamType(ParamType.BOOLEAN);
        params.addParam(ParamType.INT, nodeId);
        params.addParam(ParamType.INT, nodeId);
        ArrayList<ArrayList<Object>> results = select(IS_NODE_ISOLATED, params);
        if (results == null || results.size() == 0) {
            System.out.println("isNodeAssigned returned null");
            return true;
        }
        Long ret= (Long) results.get(0).get(0);
        if (ret==1) isUnAssigned = true;
        else isUnAssigned = false;
        return isUnAssigned;
    }
    public boolean opsetIsIsolated(String sId) throws Exception {
        return isNodeUnAssigned(Integer.valueOf(sId));
    }

    /**
     * get the path for an object
     * @param sId the id of the object
     * @return the path
     * @throws Exception
     */
    public String getObjPath(String sId) throws Exception {
        //TODO is sId the object_node_id or orginial_node_id
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId);
        ArrayList<ArrayList<Object>> results = select(GET_OBJ_PATH, params);
        return (String) results.get(0).get(0);
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

    public boolean deleteObjectInternal(String sOattrId) {
        try {
            String sObjId = getAssocObj(sOattrId);
            return deleteObjectStrongInternal(null,sObjId);
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Could not delete object " + sOattrId + "-"+ e.getMessage();
            return false;
        }
    }

    // need to revisit the for loop
    public boolean deleteObjectStrongInternal(String sSessId, String sObjId) {
        String sAssocId;
        try {
            sAssocId = getAssocOattr(sObjId);
            if (sAssocId == null) {
                errorMessage = "Inconsistency: no object or no associated attribute";
                return false;
            }

            // Get the opsets assigned to the assoc oattr.
            ArrayList<Integer> attr = getFromOpsets(sAssocId,
                    PM_NODE.OATTR.value);
            // For each opset:
            if (attr != null) {
                for (Integer i : attr) {
                    String sOpsetId = String.valueOf(i);

                    // How many object attributes is this opset assigned to?
                    // If one, then it's the o's assoc oattr, and the opset must
                    // be deleted
                    // together with its assignments from user attributes. If
                    // more than
                    // one, delete just the assignment opset -> o's assoc oattr.
                    ArrayList<Integer> attr2 = getToAttrs(sOpsetId,
                            PM_NODE.OPSET.value, DEPTH);
                    if (attr2.size() == 1) {
                        // First delete the opset's assignments from user
                        // attributes.
                        ArrayList<Integer> attr3 = getFromAttrs(sOpsetId,
                                PM_NODE.OPSET.value, DEPTH);
                        if (attr3 != null) {
                            for (Integer i3 : attr3) {
                                String sUattrId = String.valueOf(i);
                                // Delete the assignment from uattr to opset.
                                deleteAssignment(Integer.valueOf(sUattrId), Integer.valueOf(sOpsetId));
                            }
                        }
                        // Now delete the opset.
                        MySQL_Parameters params = new MySQL_Parameters();
                        params.addParam(ParamType.INT, Integer.valueOf(sOpsetId));
                        try {
                            delete(DELETE_NODE, params);
                        } catch (Exception e) {
                            errorMessage = "Could not delete opset " + sOpsetId + ": " + e.getMessage();
                            return false;
                        }
                    } else {
                        // The opset is assigned to other object attributes in
                        // addition to
                        // o's assoc. Just delete the assignment from the opset
                        // to o's assoc.
                        deleteAssignment(Integer.valueOf(sOpsetId), Integer.valueOf(sAssocId));
                    }
                }
            }

            // Delete the associated object attribute.
            if(!deleteOattr(sSessId, sAssocId, false)){
                return false;
            }
            String sContainers = getContainerList(sAssocId, PM_NODE.OATTR.value);
            String sObjName = getEntityName(sAssocId, PM_NODE.OATTR.value);

            Packet res = ServerConfig.obligationDAO.processEvent(sSessId, null, "Object delete", sObjName,
                    sObjId, null, null, sContainers, sAssocId);
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Could not delete object " + sObjId + ": "
                    + e.getMessage();
            return false;
        }
        return true;
    }

    public String getCopyName(String sEntName, String sEntType) throws Exception{
        String sCandidate = "copyOf" + sEntName;
        int i = 0;
        while (entityNameExists(sCandidate, sEntType)) {
            i++;
            sCandidate = "copy" + String.valueOf(i) + "Of" + sEntName;
        }
        return sCandidate;
    }



    // Returns sRecId of a record that contains the given object or object
    // attribute as a field. There should be at most one such record.
    // Returns null if there is no such object or object attribute,
    // or if there is no record that contains it.

    public String getRecordOf(String sId, String sType) throws Exception{
        ArrayList<ArrayList<Object>> results = select(GET_RECORD, Integer.valueOf(sId));
        if (results== null || results.size() == 0) {
            return null;
        }
        Integer recId =  (Integer) results.get(0).get(0);

        return recId.toString();
    }

    // Check whether the given object attribute is directly assigned to a
    // record,
    // i.e., a container with a template.

    public boolean isInARecord(String sOattrId) throws Exception {
        if (getRecordOf(sOattrId, null) == null) {
            return false;
        } else return true;
    }

    // Check whether the given record is indeed a record, and whether the given
    // oa
    // is directly assigned to the given record.

    public boolean isInRecord(String sOattrId, String sRecordId) throws Exception {
        if (!isRecord(sRecordId)) {
            return false;
        }
        return isAssigned(Integer.valueOf(sOattrId), Integer.valueOf(sRecordId));

    }

    public boolean isRecord(String sId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, Integer.valueOf(sId));
        ArrayList<ArrayList<Object>> results = select(IS_RECORD, params);
        return (Long) results.get(0).get(0) > 0;
    }

    public String getRecordTemplate(String sId) throws Exception{
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId);
        return String.valueOf(select(GET_REC_TPL, params).get(0).get(0));
    }

    public Packet addRecordKey(String sSessId, String sRecId, String sKey) {
        if (sKey == null || sKey.length() <= 0) {
            return SQLPacketHandler.getSuccessPacket();
        }

        try {
            String id = getEntityId(sRecId, PM_NODE.OATTR.value);
            if(id != null){
                sRecId = id;
            }

            String key = sKey.split(PM_PROP_DELIM)[0];
            String val = sKey.split(PM_PROP_DELIM)[1];

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sRecId);
            params.addParam(ParamType.STRING, key);
            params.addParam(ParamType.STRING, val);
            insert(INSERT_RECORD_KEY, params);
        }catch(Exception e){
            e.printStackTrace();
            return failurePacket("Exception when adding key to record "
                    + sRecId);
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet addTemplateToRecord(String sRecId, String sTplId){
        try{
            if(addTemplateToRecordInternal(sRecId, sTplId)){
                return getSuccessPacket();
            }else{
                return failurePacket("error adding template to record");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    private boolean addTemplateToRecordInternal(String sRecId, String sTplId) throws Exception{
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sTplId);
        params.addParam(ParamType.INT, sRecId);
        update(UPDATE_RECORD_TPL, params);
        return true;
    }

    public Packet addTemplateKey(String sSessId, String sTplId, String sKey) {
        if (sKey == null || sKey.length() <= 0) {
            return SQLPacketHandler.getSuccessPacket();
        }
        try{
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sTplId);
            //String id = getEntityId(sKey, PM_NODE.OATTR.value);
            //String keyParam = (id==null)?sKey:id;
            params.addParam(ParamType.STRING, sKey);

            insert(INSERT_TPL_KEY, params);
        }catch(Exception e){
            e.printStackTrace();
            return failurePacket("could not add key: " + sKey + " to template with id: " + sTplId);
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    //***************************************MELL*****************************************
    boolean bMellPrint = false;

    public Packet successorOas(String sSessId, String sUserName, String sUserId, String sUserType,
                               String sTgtName, String sTgtId, String sTgtType, String sPerm) {
        if (bMellPrint) {
            System.out.println("SuccessorOas called with arguments:");
            System.out.println("  Session id: " + sSessId);
            System.out.println("  User name: " + sUserName);
            System.out.println("  User id: " + sUserId);
            System.out.println("  User type: " + sUserType);
            System.out.println("  Target name:" + sTgtName);
            System.out.println("  Target id: " + sTgtId);
            System.out.println("  Target type:" + sTgtType);
        }

        if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value))
            return failurePacket("successorOas: the marked entity must be a user!");
        if (sUserId == null)
            return failurePacket("successorOas: user id cannot be null!");
        PmGraphNode node = graphMgr.getGraph().getNode(sUserId);
        if (node == null)
            return failurePacket("successorOas: no user with id " + sUserId);
        String sUName = node.getName();
        if (sUserName == null) sUserName = sUName;
        else if (!sUserName.equalsIgnoreCase(sUName))
            return failurePacket("successorOas: inconsistent user name!");

        if (!sTgtType.equalsIgnoreCase(PM_NODE.OATTR.value) &&
                !sTgtType.equalsIgnoreCase(PM_NODE.ASSOC.value))
            return failurePacket("successorOas: target is not an object attribute!");
        if (sTgtId == null)
            return failurePacket("successorOas: target id cannot be null!");
        node = graphMgr.getGraph().getNode(sTgtId);
        if (node == null)
            return failurePacket("successorOas: no target with id " + sTgtId);
        String sTName = node.getName();
        if (sTgtName == null) sTgtName = sTName;
        else if (!sTgtName.equalsIgnoreCase(sTName))
            return failurePacket("successorOas: inconsistent target name!");


        try {
            HashSet hsOas = successorOasInternal(sUserName, sUserId, sTgtName, sTgtId, sPerm);


            Packet res = new Packet();
            if (hsOas != null) for (Iterator hsiter = hsOas.iterator(); hsiter.hasNext(); ) {
                String sId = (String)hsiter.next();
                String sName = graphMgr.getGraph().getNode(sId).getName();
                res.addItem(ItemType.RESPONSE_TEXT, sId + PM_FIELD_DELIM + sName);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("successorOas: exception!");
        }
    }

    private HashSet successorOasInternal(String sUserName, String sUserId, String sOaName, String sOaId, String sPerm) {
        if (bMellPrint) {
            System.out.println("SuccessorOasInternal called with arguments:");
            System.out.println("  user name: " + sUserName);
            System.out.println("  user id:" + sUserId);
            System.out.println("  oa name: " + sOaName);
            System.out.println("  oa id:" + sOaId);
        }
        // Find the set of successor nodes of oa (i.e., {x | oa -> x}.
        String sOaType = graphMgr.getGraph().getNode(sOaId).getType();
        HashSet hsSuccs = inMemFindSuccNodes(sOaId, sOaType);
        printSet(hsSuccs, PM_NODE.OATTR.value, "Successors of " + sOaName);

        // Prepare the set of nodes "available for display"
        HashSet hsAvDisp = new HashSet();

        // Prepare the Hashtable of labeled object attributes nodes
        Hashtable htLabeled = findBorderOaPrivRestrictedInternal(sUserName, sUserId);

        // For each successor node x
        Iterator hsiter = hsSuccs.iterator();
        while (hsiter.hasNext()) {
            String sSuccId = (String)hsiter.next();
            String sSuccName = graphMgr.getGraph().getNode(sSuccId).getName();

            // Find "required" PCs for successor x
            HashSet hsReqdPcs = inMemFindPcSet(sSuccId, PM_NODE.OATTR.value);
            inMemPrintSet(hsReqdPcs, "Required PCs for succ " + sSuccName, true);

            // BFS from predecessor x to find "labeled" nodes.
            HashSet hsLabeled = inMemGetLabeledNodesFrom(sSuccId, htLabeled);
            inMemPrintSet(hsLabeled, "Labeled nodes for succ " + sSuccName, true);

            // From the labels of the labeled nodes for successor x
            // build a new hashtable {op -> pcset} with the mappings consolidated.
            Hashtable htNew = new Hashtable();
            // For (each y in the labeled node set)
            Iterator itLabeled = hsLabeled.iterator();
            while (itLabeled.hasNext()) {
                String sYId = (String)itLabeled.next();
                String sYName = graphMgr.getGraph().getNode(sYId).getName();
                // Extract y's label from the htLabeled.
                Hashtable htYLabel = (Hashtable)htLabeled.get(sYId);
                // For (each operation op in y's label
                for (Enumeration ops = htYLabel.keys(); ops.hasMoreElements(); ) {
                    String sOp = (String)ops.nextElement();
                    if(!sOp.equals(sPerm)){
                        continue;
                    }
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
                    Iterator itPcs = hsPcs.iterator();
                    while (itPcs.hasNext()) {
                        String sPcId = (String)itPcs.next();
                        String sPcName = graphMgr.getGraph().getNode(sPcId).getName();
                        System.out.print(sPcName + ", ");
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

        inMemPrintSet(hsAvDisp, "The set of oa available for display", true);
        return hsAvDisp;
    }

    public Packet subsequentOas(String sSessId, String sUserName, String sUserId,
                                String sUserType, String sTgtName, String sTgtId, String sTgtType, String sPerm) {
        if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value))
            return failurePacket("subsequentOas: the marked node must be a user!");
        if (sUserId == null)
            return failurePacket("subsequentOas: user id cannot be null!");
        PmGraphNode node = graphMgr.getGraph().getNode(sUserId);
        if (node == null)
            return failurePacket("subsequentOas: no user with id " + sUserId);
        String sUName = node.getName();
        if (sUserName == null) sUserName = sUName;
        else if (!sUserName.equalsIgnoreCase(sUName))
            return failurePacket("subsequentOas: Inconsistent user name!");

        if (!sTgtType.equalsIgnoreCase(PM_NODE.OATTR.value) &&
                !sTgtType.equalsIgnoreCase(PM_NODE.ASSOC.value) )
            return failurePacket("SubsequentOas: target must be an object or object attribute!");
        if (sTgtId == null) return failurePacket("SubsequentOas: target id cannot be null!");
        node = graphMgr.getGraph().getNode(sTgtId);
        if (node == null)
            return failurePacket("subsequentOas: no target with id " + sTgtId);
        String sTName = node.getName();
        if (sTgtName == null) sTgtName = sTName;
        else if (!sTgtName.equalsIgnoreCase(sTName))
            return failurePacket("subsequentOas: Inconsistent target name!");
        try {
            HashSet hsOas = subsequentOasInternal(sUserName, sUserId, sTgtName, sTgtId);
            Packet res = new Packet();
            if (hsOas != null) for (Iterator hsIter = hsOas.iterator(); hsIter.hasNext(); ) {
                String sId = (String)hsIter.next();
                String sName = graphMgr.getGraph().getNode(sId).getName();
                res.addItem(ItemType.RESPONSE_TEXT, sId + PM_FIELD_DELIM + sName);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket();
        }
    }

    private HashSet subsequentOasInternal(String sUserName, String sUserId,
                                          String sOaName, String sOaId) {
        if (bMellPrint) {
            System.out.println("SubsequentOasInternal called with arguments:");
            System.out.println("  user name: " + sUserName);
            System.out.println("  user id:" + sUserId);
            System.out.println("  oa name: " + sOaName);
            System.out.println("  oa id:" + sOaId);
        }
        long lStart = System.nanoTime();

        // Get the predecessor nodes of oa (i.e., {x | x -> oa}. These are
        // the members of oa seen as a container. Exclude the opsets fromm it.
        HashSet hsPreds = graphMgr.getGraph().getNode(sOaId).getMembers();
        inMemPrintSet(hsPreds, "Predecessors of " + sOaName, true);

        // Find 'covered' PCs by calling findPcSet(oa)
        HashSet hsCoveredPcs = inMemFindPcSet(sOaId, PM_NODE.OATTR.value);
        inMemPrintSet(hsCoveredPcs, "Covered PCs for " + sOaName, true);

        // Prepare the set of nodes "available for display"
        HashSet hsAvDisp = new HashSet();

        // Prepare the Hashtable of labeled object attributes nodes
        Hashtable htLabeled = null;

        // For each predecessor node x
        Iterator itPreds = hsPreds.iterator();
        while (itPreds.hasNext()) {
            String sPredId = (String)itPreds.next();
            String sPredType = graphMgr.getGraph().getNode(sPredId).getType();
            String sPredName = graphMgr.getGraph().getNode(sPredId).getName();

            // Exclude the opsets from predecessors.
            if (sPredType.equalsIgnoreCase(PM_NODE.OPSET.value)) continue;

            // Find "required" PCs for pred. x
            HashSet hsReqdPcs = inMemFindPcSet(sPredId, PM_NODE.OATTR.value);
            inMemPrintSet(hsReqdPcs, "Required PCs for " + sPredName, true);

            // If required PC set is a subset of covered PCs, then x is available for display.
            // if (hsReqdPcs.equals(hsCoveredPcs)) hsAvDisp.add(sPredId);
            /*if (hsCoveredPcs.containsAll(hsReqdPcs)) {
                hsAvDisp.add(sPredId);
                System.out.println("reqdPcs is a subset of coveredPcs, make "
                        + sPredName + " available for display");
                continue;
            }*/

            // If we got here, it means that predecessor node x is not (yet) available.
            System.out.println("Pred " + sPredName + " is not available for display");
            // This is the first not available pred if and only if
            // 			htLabeled == null.
            // If this is the case, compute the labeled oas for user u.
            if (htLabeled == null) {
                htLabeled = findBorderOaPrivRestrictedInternal(sUserName, sUserId);
                System.out.println(sPredName + "is the first pred not available, computed labeled oas");
            }
            // BFS from predecessor x to find "labeled" nodes.
            HashSet hsLabeled = inMemGetLabeledNodesFrom(sPredId, htLabeled);
            inMemPrintSet(hsLabeled, "Labeled nodes for pred " + sPredName, true);

            // From the labels of the labeled nodes for predecessor x
            // build a new hashtable {op -> pcset} with the mappings consolidated.
            Hashtable htNew = new Hashtable();
            // For (each y in the labeled node set)
            Iterator itLabeled = hsLabeled.iterator();
            while (itLabeled.hasNext()) {
                String sYId = (String)itLabeled.next();
                String sYName = graphMgr.getGraph().getNode(sYId).getName();
                // Extract y's label from the htLabeled.
                Hashtable htYLabel = (Hashtable)htLabeled.get(sYId);
                // For (each operation op in y's label
                for (Enumeration ops = htYLabel.keys(); ops.hasMoreElements(); ) {
                    String sOp = (String)ops.nextElement();
                    /*if(!sOp.equals(sPerm)){
                        continue;
                    }*/
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
                for (Enumeration ops = htNew.keys(); ops.hasMoreElements(); ) {
                    String sOp = (String)ops.nextElement();
                    HashSet hsPcs = (HashSet)htNew.get(sOp);
                    Iterator itPcs = hsPcs.iterator();
                    while (itPcs.hasNext()) {
                        String sPcId = (String)itPcs.next();
                        //System.out.print(graphMgr.getGraph().getNode(sPcId).getName() + ", ");
                    }
                    //System.out.println();
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

        inMemPrintSet(hsAvDisp, "The set of oa available for display", true);
        return hsAvDisp;
    }

    // sOaId is the id of an object attribute oa.
    // htLabeled is the hashtable of border object attributes
    // returned by findBorderOaPriv(u).
    // This method finds all x such that oa ->* x and x is in htLabeled.keys.
    // In MySQL version, take care to select only x that are object attributes.
    // In the AD version this was taken care auttomatically through the
    // AD attribute pmToAttr.
    public HashSet getLabeledNodesFrom(String sOaId, Hashtable htLabeled) throws Exception {
        ArrayList queue = new ArrayList();
        HashSet visited = new HashSet();
        String sCrtId;
        HashSet hsResult = new HashSet();

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
            ArrayList<Integer> attrs = getFromAttrs(sCrtId, PM_NODE.OATTR.value, DEPTH);
            if (attrs != null && attrs.size() > 0) for (Integer id : attrs) {
                String sAttrId = id.toString();
                queue.add(sAttrId);
            }
        }
        return hsResult;
    }

    // sOaId is the id of an object attribute oa.
    // htLabeled is the hashtable of border object attributes
    // returned by findBorderOaPriv(u).
    // This method finds all x such that oa ->* x and x is in htLabeled.keys.
    // In MySQL version, take care to select only x that are object attributes.
    // In the AD version this was taken care auttomatically through the
    // AD attribute pmToAttr.
    public HashSet inMemGetLabeledNodesFrom(String sOaId, Hashtable htLabeled) {
        ArrayList queue = new ArrayList();
        HashSet visited = new HashSet();
        String sCrtId;
        HashSet hsResult = new HashSet();

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
            HashSet descs = new HashSet(graphMgr.getGraph().getNode(sCrtId).getContainers());
            // From these descendants insert only the object attributes into the queue.
            Iterator itDescs = descs.iterator();
            while (itDescs.hasNext()) {
                String sDescId = (String)itDescs.next();
                String sDescType = graphMgr.getGraph().getNode(sDescId).getType();
                if (sDescType.equalsIgnoreCase(PM_NODE.OATTR.value)) queue.add(sDescId);
            }
        }
        return hsResult;
    }

    // Given an oa node, find the set {x | x -> oa}.
    public HashSet findPredNodes(String sAttrId)
            throws Exception {
        HashSet hsPreds = new HashSet();
        System.out.println("In findPredNodes ");
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, Integer.valueOf(sAttrId));
        ArrayList<ArrayList<Object>> members = select(GET_MEMBERS, params);
        if (members.size() <= 0) {
            System.out.println("No values returned");
        } else {
            for (ArrayList<Object> member : members) {
                hsPreds.add((Integer) member.get(0));
            }
        }
        return hsPreds;
    }

    // The argument may be a user attribute or an object attribute.
    // The method returns the set { x | x is attribute and x -> attr }.
    // If the attribute type is object attribute, it should return the set of members
    // of attr. If the attribute type is a user attribute, it should return the
    // set of members that are not users.
    HashSet inMemFindPredNodes(String sBaseId, String sType) {
        PmGraphNode node = graphMgr.getGraph().getNode(sBaseId);
        if (node == null) {
            System.out.println("inMemFindPredNodes: no node with id " + sBaseId);
            return null;
        }
        HashSet preds = new HashSet(node.getMembers());
        if (sType.equalsIgnoreCase(PM_NODE.OATTR.value) ||
                sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            return preds;
        }
        else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
            Iterator predsIter = preds.iterator();
            while (predsIter.hasNext()) {
                String sPredId = (String)predsIter.next();
                String sPredType = graphMgr.getGraph().getNode(sPredId).getType();
                if (sPredType.equalsIgnoreCase(PM_NODE.USER.value))
                    predsIter.remove();
            }
            return preds;
        } else {
            System.out.println("inMemFindPredNodes: type " + sType + " not a, b, or o!");
            return null;
        }
    }

    // The argument can be a user, a user attribute, an object, or an object attribute.
    // If it's a user u, return { ua | u -> ua } (all containers that are UATTR).
    // If it's a user attribute ua, return { ua' | ua -> ua'} (all containers that are UATTR).
    // If it's an object attribute oa, return { oa' | oa -> oa'} (all containers that are
    // OATTR).
    // If it's an object o, return { oa' | o -> oa'} (all containers that are OATTR).

    HashSet inMemFindSuccNodes(String sBaseId, String sType) {
        PmGraphNode node = graphMgr.getGraph().getNode(sBaseId);
        if (node == null) {
            System.out.println("inMemFindSuccNodes: no node with id " + sBaseId);
            return null;
        }
        String sAllowed = null;
        if (sType.equalsIgnoreCase(PM_NODE.USER.value) ||
                sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
            sAllowed = PM_NODE.UATTR.value;
        } else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value) ||
                sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            sAllowed = PM_NODE.OATTR.value;
        } else {
            System.out.println("inMemFindSuccNodes: type " + sType + " not u, a, b or o!");
            return null;
        }

        HashSet succ = new HashSet(node.getContainers());
        Iterator succIter = succ.iterator();
        while (succIter.hasNext()) {
            String sSuccId = (String)succIter.next();
            String sSuccType = graphMgr.getGraph().getNode(sSuccId).getType();
            if (!sSuccType.equalsIgnoreCase(sAllowed))
                succIter.remove();
        }
        return succ;
    }




    // Given an oa node, find the set {x | oa -> x}.
    // Should work also for users and user attributes.
    public HashSet findSuccNodes(String sAttrId) throws Exception {
        HashSet hsPreds = new HashSet();
        System.out.println("In findSuccNodes ");
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, Integer.valueOf(sAttrId));
        ArrayList<ArrayList<Object>> members = select(GET_ASSIGNMENTS, params);
        if (members.size() <= 0) {
            System.out.println("No values returned");
        } else {
            for (ArrayList<Object> member : members) {
                hsPreds.add((Integer) member.get(0));
            }
        }
        return hsPreds;
    }

    // InitialOas() returns the initial set of oa nodes to display when a user
    // logs on and wants to explore his/her objects.

    public Packet initialOas(String sSessId, String sUserName, String sUserId, String sUserType, String sPerm) {
        if (bMellPrint) {
            System.out.println("InitialOas called with arguments:");
            System.out.println("  session id: " + sSessId);
            System.out.println("  User name:" + sUserName);
            System.out.println("  User id: " + sUserId);
            System.out.println("  User type:" + sUserType);
        }

        if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value)) return failurePacket("InitialOas only applicable to users!");
        PmGraphNode node = graphMgr.getGraph().getNode(sUserId);
        if (node == null) return failurePacket("No user with id " + sUserId + " in InitialOas()!");
        String sUName = node.getName();
        if (sUserName == null) sUserName = sUName;
        else if (!sUserName.equalsIgnoreCase(sUName)) return failurePacket("Inconsistent user name in iinitialOas()!");

        HashSet hsOas = initialOasInternal(sUserName, sUserId);
        if (hsOas == null) return failurePacket("Null set of oas returned by initialOasInetrnal()!");
        Packet res = new Packet();
        try {
            Iterator hsiter = hsOas.iterator();
            while (hsiter.hasNext()) {
                String sOaId = (String)hsiter.next();
                String sOaName = graphMgr.getGraph().getNode(sOaId).getName();
                res.addItem(ItemType.RESPONSE_TEXT, sOaId + PM_FIELD_DELIM + sOaName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket();
        }
        return res;
    }

    HashSet initialOasInternal(String sUserName, String sUserId) {
        // Prepare the hashset to return.
        HashSet hsOa = new HashSet();

        // Call find_border_oa_priv(u). The result is a Hashtable
        // htoa = {oa -> {op -> pcset}}:
        Hashtable htOa = findBorderOaPrivRestrictedInternal(sUserName, sUserId);

        // For each returned oa (key in htOa)
        for (Enumeration oas = htOa.keys(); oas.hasMoreElements(); ) {
            String sOaId = (String)oas.nextElement();
            // Compute oa's required PCs by calling find_pc_set(sOaId).
            HashSet hsReqPcs = inMemFindPcSet(sOaId, PM_NODE.OATTR.value);
            // Extract oa's label.
            Hashtable htOaLabel = (Hashtable)htOa.get(sOaId);

            // Walk through the op -> pcset of the oa's label.
            // For each operation/access right
            for (Enumeration ops = htOaLabel.keys(); ops.hasMoreElements(); ) {
                String sOp = (String)ops.nextElement();
                /*if(!sOp.equals(perm)){
                    continue;
                }*/
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

    HashSet initialOasWithLabelsInternal(String sUserName, String sUserId) {
        // Prepare the hashset to return.
        HashSet hsOa = new HashSet();

        // Call find_border_oa_priv(u). The result is a Hashtable
        // htoa = {oa -> {op -> pcset}}:
        Hashtable htOa = findBorderOaPrivRestrictedInternal(sUserName, sUserId);

        // For each returned oa (key in htOa)
        for (Enumeration oas = htOa.keys(); oas.hasMoreElements(); ) {
            String sOaId = (String)oas.nextElement();
            // Compute oa's required PCs by calling find_pc_set(sOaId).
            HashSet hsReqPcs = inMemFindPcSet(sOaId, PM_NODE.OATTR.value);
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

    public Packet initialOasWithLabels(String sSessId, String sUserName, String sUserId, String sUserType) {
        if (bMellPrint) {
            System.out.println("InitialOasWithLabels called with arguments:");
            System.out.println("  session id: " + sSessId);
            System.out.println("  User name:" + sUserName);
            System.out.println("  User id: " + sUserId);
            System.out.println("  User type:" + sUserType);
        }

        if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value)) return failurePacket("InitialOas only applicable to users!");
        PmGraphNode node = graphMgr.getGraph().getNode(sUserId);
        if (node == null) return failurePacket("No user with id " + sUserId + " in InitialOas()!");
        String sUName = node.getName();
        if (sUserName == null) sUserName = sUName;
        else if (!sUserName.equalsIgnoreCase(sUName)) return failurePacket("Inconsistent user name in iinitialOas()!");

        HashSet hsOas = initialOasWithLabelsInternal(sUserName, sUserId);
        if (hsOas == null) return failurePacket("Null set of oas returned by initialOasInetrnal()!");
        Packet res = new Packet();
        try {
            Iterator hsiter = hsOas.iterator();
            while (hsiter.hasNext()) {
                String sOaId = (String)hsiter.next();
                String sOaName = graphMgr.getGraph().getNode(sOaId).getName();
                res.addItem(ItemType.RESPONSE_TEXT, sOaId + PM_FIELD_DELIM + sOaName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket();
        }
        return res;
    }

    public Packet showAccessibleObjects(String sSessId, String sUserName, String sUserId, String sUserType) {
        System.out.println("ShowAccessibleObjects called with arguments:");
        System.out.println("  session id: " + sSessId);
        System.out.println("  User name:" + sUserName);
        System.out.println("  User id: " + sUserId);
        System.out.println("  User type:" + sUserType);

        try {
            if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value))
                return SQLPacketHandler.getFailurePacket("ShowAccessibleObjects: only applicable to users!");
            String sUName = graphMgr.getGraph().getNode(sUserId).getName();
            if (!sUName.equalsIgnoreCase(sUserName))
                return SQLPacketHandler.getFailurePacket("ShowAccessibleObjects: inconsistent user name!");

            System.out.println("Members are:");
            HashSet hsMembers = graphMgr.getGraph().getNode(sUserId).getMembers();
            Iterator hsiter = hsMembers.iterator();
            while (hsiter.hasNext()) {
                String sId = (String)hsiter.next();

                String sType = graphMgr.getGraph().getNode(sId).getType();
                String sName = graphMgr.getGraph().getNode(sId).getName();

                System.out.println("   " + sId + " " + sName + " " + sType);
            }
            System.out.println("Containers are:");
            HashSet hsContainers = graphMgr.getGraph().getNode(sUserId).getContainers();
            hsiter = hsContainers.iterator();
            while (hsiter.hasNext()) {
                String sId = (String)hsiter.next();

                String sType = graphMgr.getGraph().getNode(sId).getType();
                String sName = graphMgr.getGraph().getNode(sId).getName();

                System.out.println("   " + sId + " " + sName + " " + sType);
            }

            Hashtable<String, HashSet> htResult = showAccessibleObjectsInternal(sUserName, sUserId);

            if (htResult == null) return failurePacket();
            Packet res = new Packet();
            for (Enumeration objs = htResult.keys(); objs.hasMoreElements(); ) {
                String sOId = (String)objs.nextElement();
                String sOName = graphMgr.getGraph().getNode(sOId).getName();
                StringBuilder sb = new StringBuilder();
                sb.append(sOId);
                sb.append(PM_FIELD_DELIM);
                sb.append(sOName);
                sb.append(PM_FIELD_DELIM);
                // The elements of the following set are operations (type String).
                HashSet hsOps = (HashSet)htResult.get(sOId);
                Iterator hsit = hsOps.iterator();
                boolean bFirst = true;
                while (hsit.hasNext()) {
                    String sOp = (String)hsit.next();
                    if (bFirst) {
                        sb.append(sOp);
                        bFirst = false;
                    } else {
                        sb.append(PM_LIST_MEMBER_SEP + sOp);
                    }
                }
                res.addItem(ItemType.RESPONSE_TEXT, sb.toString());
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return SQLPacketHandler.getFailurePacket("Exception in ShowAccessibleObjects!");
        }
    }

    // Called from showAccessibleObjects().
    // No need to check the arguments.
    Hashtable showAccessibleObjectsInternal(String sUserName, String sUserId) throws Exception {

        System.out.println("showAccessibleObjectsInternal called with arguments:");
        System.out.println("  user name: " + sUserName);
        System.out.println("  user id: " + sUserId);

        inMemPrintSet(inMemGetContainers("110"), "Containers of ua-2", true);


        // Start timer.
        long lStart = System.nanoTime();

        // Prepare the Hashtable to return, where the key is an object id and
        // the corresponding value is a HashSet of all privileges of the user
        // on the key object.
        Hashtable htResult = new Hashtable();

        // Call find_border_oa_priv(u). Note that are two versions of this
        // function, one relaxed that does not require the user attribute to be
        // in the same policy class as the object attribute, the other restrictive.
        // The result is a Hashtable
        //				 htoa = {oa -> {op -> pcset}}.
        Hashtable htOa = findBorderOaPrivRestrictedInternal(sUserName, sUserId);
        //Hashtable htOa = findBorderOaPrivRelaxedInternal(sUserName, sUserId);

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

                // Insert oa into the queue (it may be an object itself).
                queue.add(sOaId);

                if (bMellPrint) {
                    System.out.println("For oa " + graphMgr.getGraph().getNode(sOaId).getName() +
                            " we found the objects ");
                }

                // While the queue has elements, extract an element from the queue
                // and visit it.
                while (!queue.isEmpty()) {
                    sCrtOaId = (String)queue.remove(0);
                    if (visited.contains(sCrtOaId)) continue;
                    // VISIT the element.
                    String sCrtOaType = graphMgr.getGraph().getNode(sCrtOaId).getType();
                    String sCrtOaName = graphMgr.getGraph().getNode(sCrtOaId).getName();
                    if (sCrtOaType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
                        hsO.add(sCrtOaId);
                        if (bMellPrint) System.out.println("    " + sCrtOaName);
                    }
                    visited.add(sCrtOaId);

                    HashSet hsAscs = graphMgr.getGraph().getNode(sCrtOaId).getMembers();
                    Iterator hsAscsIter = hsAscs.iterator();
                    while (hsAscsIter.hasNext()) {
                        String sAttrId = (String)hsAscsIter.next();
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
                    String sName = graphMgr.getGraph().getNode(sKey).getName();
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
                            String sPcName = graphMgr.getGraph().getNode(sPcId).getName();

                            System.out.print(sPcName + ", ");
                        }
                        System.out.println();
                    }
                }
            }

            // For each discovered object do BFS to find a set of required PCs.
            // In other words, for each o in htObjects, find pcset = {pc | o ->+ pc}.
            for (Enumeration keys = htObjects.keys(); keys.hasMoreElements() ;) {
                String sOId = (String)keys.nextElement();
                HashSet hsReqPcs = inMemFindPcSet(sOId, PM_NODE.OATTR.value);
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
                String sOName = graphMgr.getGraph().getNode(sOId).getName();

                System.out.print("  " + sOName + " ---> ");
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

    // Compute the operations permitted to a user on an object/object attribute.
    public Packet calcPriv(String sSessId, String sUserName, String sUserId,
                           String sUserType, String sTgtName, String sTgtId, String sTgtType) {
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
            return failurePacket("calcPriv: the marked node must be a user!");
        if (sUserId == null)
            return failurePacket("calcPriv: user id cannot be null!");
        PmGraphNode node = graphMgr.getGraph().getNode(sUserId);
        if (node == null)
            return failurePacket("calcPriv: no user with user id " + sUserId);
        String sUName = node.getName();
        if (sUserName == null) sUserName = sUName;
        else if (!sUserName.equalsIgnoreCase(sUName))
            return failurePacket("calcPriv: Inconsistent user name!");

        if (!sTgtType.equalsIgnoreCase(PM_NODE.OATTR.value) &&
                !sTgtType.equalsIgnoreCase(PM_NODE.ASSOC.value) )
            return failurePacket("calcPriv: target must be an object or object attribute!");
        if (sTgtId == null) return failurePacket("calcPriv: target id cannot be null!");
        node = graphMgr.getGraph().getNode(sTgtId);
        if (node == null)
            return failurePacket("calcPriv: no target with id " + sTgtId);
        String sTName = node.getName();
        if (sTgtName == null) sTgtName = sTName;
        else if (!sTgtName.equalsIgnoreCase(sTName))
            return failurePacket("calcPriv: Inconsistent target name!");


        HashSet hsOps = calcPrivInternal(sUserId, sTgtId);
        Packet res = new Packet();
        if (hsOps == null) return failurePacket("CalcPrivInternal returned a null value!");
        for (Iterator itOps = hsOps.iterator(); itOps.hasNext(); ) {
            String sOp = (String)itOps.next();
            try {
                res.addItem(ItemType.RESPONSE_TEXT, (String)sOp);
            } catch (Exception e) {
                e.printStackTrace();
                return failurePacket();
            }
        }
        return res;
    }

    // CalcPrivInternal calculates a user's permitted operations on an
    // object attribute/object. No checks.
    private HashSet<String> calcPrivInternal(String sUserId, String sOaId) {
        if (bMellPrint) {
            System.out.println("CalcPrivInternal called with arguments:");
            System.out.println("  user id:" + sUserId);
            System.out.println("  oa id:" + sOaId);
        }
        long lStart = System.nanoTime();

        // Prepare the table with u's permitted ops on the object attribute oa.
        HashSet<String> hsOps = new HashSet<String>();

        // Get the border nodes (i.e., the object attributes reachable from u).
        String sUserName = graphMgr.getGraph().getNode(sUserId).getName();
        String sOaName = graphMgr.getGraph().getNode(sOaId).getName();
        Hashtable htReachableOas = findBorderOaPrivRestrictedInternal(sUserName, sUserId);

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
        String sCrtName;

        // Insert oa into the queue.
        queue.add(sOaId);

        // while the queue has elements, extract an element from the queue
        // and visit it.
        while (!queue.isEmpty()) {
            sCrtId = (String)queue.remove(0);
            sCrtName = graphMgr.getGraph().getNode(sCrtId).getName();
            if (bMellPrint) System.out.println("Attribute " + sCrtName + " has been removed from the queue");
            if (!visited.contains(sCrtId)) {
                if (bMellPrint) System.out.println("Attribute " + sCrtName + " is being visited");

                // If the current oattr sCrtId is in htReachableOas,
                // include it into reachable.
                if (htReachableOas.containsKey(sCrtId)) reachable.add(sCrtId);
                // Set the crt element as having been visited.
                visited.add(sCrtId);
                // Find and insert its descendants (i.e., all oattr x such that crt ---> x)
                // into the queue.
                HashSet hsContainers = graphMgr.getGraph().getNode(sCrtId).getContainers();
                Iterator itContainers = hsContainers.iterator();
                while (itContainers.hasNext()) {
                    String sContId = (String)itContainers.next();
                    String sContType = graphMgr.getGraph().getNode(sContId).getType();
                    String sContName = graphMgr.getGraph().getNode(sContId).getName();
                    if (sContType.equalsIgnoreCase(PM_NODE.OATTR.value)) queue.add(sContId);
                    if (bMellPrint) System.out.println("Descendant " + sContName + " has been inserted into the queue");
                }
            }
        }
        // Let's print reachable.
        if (bMellPrint) inMemPrintSet(reachable, "OAs reachable from "
                + sOaName + " and from " + sUserName, true);

        // Label oa with the labels of all object attributes x in reachable.

        // For each x in reachable
        Iterator iter = reachable.iterator();
        while (iter.hasNext()) {
            String sXId = (String)iter.next();
            String sXName = graphMgr.getGraph().getNode(sXId).getName();
            if (bMellPrint) System.out.println("Current reachable x is: " + sXName);
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
                    // ...insert the association x's op ---> copy of x's pcset
                    // into oa's label.
                    htOaLabel.put(sOp, hsOaPcset);
                }
            }
        }

        // BFS from oa to find oa's reachable pcs.
        HashSet hsOaReachablePcs = inMemFindPcSet(sOaId, PM_NODE.OATTR.value);
        if (bMellPrint) inMemPrintSet(hsOaReachablePcs,
                "Policy Classes reachable from " + sOaName, true);

        // For each op ---> pcset in oa's label
        for (Enumeration ops = htOaLabel.keys(); ops.hasMoreElements(); ) {
            String sOp = (String)ops.nextElement();// oa's op
            HashSet hsOaPcset = (HashSet)htOaLabel.get(sOp);// oa's pcset
            // If the pcset in the oa's label includes the pcs reachable
            // from oa, then the operation in the oa's label is permitted on oa.
            if (hsOaPcset.containsAll(hsOaReachablePcs)) hsOps.add(sOp);
        }
        if (bMellPrint) inMemPrintSet(hsOps, "Operations permitted on "
                + sOaName + " to user " + sUserName, false);

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

    public Packet findBorderOaPrivRelaxed(String sSessId, String sUserName, String sUserId,
                                          String sUserType) {
        if (bMellPrint) {
            System.out.println("FindBorderOaPrivRelaxed called with arguments:");
            System.out.println("  session id: " + sSessId);
            System.out.println("  User name:" + sUserName);
            System.out.println("  User id: " + sUserId);
            System.out.println("  User type:" + sUserType);
        }

        if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value)) return failurePacket("FindBorderOaPrivRelaxed: only applicable to users!");
        String sUName = graphMgr.getGraph().getNode(sUserId).getName();
        if (!sUName.equalsIgnoreCase(sUserName)) return failurePacket("FindBorderOaPrivRelaxed: inconsistent user name!");

        Hashtable htReachableOas = findBorderOaPrivRelaxedInternal(sUserName, sUserId);
        return SQLPacketHandler.getSuccessPacket();
    }

    // InMemory version of findBorderOaPrivRestricted.
    public Packet findBorderOaPrivRestricted(String sSessId, String sUserName,
                                             String sUserId, String sUserType) {
        if (bMellPrint) {
            System.out.println("findBorderOaPrivRestricted called with arguments:");
            System.out.println("  session id: " + sSessId);
            System.out.println("  User name:" + sUserName);
            System.out.println("  User id: " + sUserId);
            System.out.println("  User type:" + sUserType);
        }

        if (!sUserType.equalsIgnoreCase(PM_NODE.USER.value)) return failurePacket("findBorderOaPrivRestricted: only applicable to users!");
        String sUName = graphMgr.getGraph().getNode(sUserId).getName();
        if (!sUName.equalsIgnoreCase(sUserName)) return failurePacket("findBorderOaPrivRestricted: inconsistent user name!");

        Hashtable htReachableOas = findBorderOaPrivRestrictedInternal(sUserName, sUserId);
        return SQLPacketHandler.getSuccessPacket();
    }


    // Serban - InMemory version
    public Hashtable findBorderOaPrivRelaxedInternal(String sUserName, String sUserId) {
        if (bMellPrint) {
            System.out.println("FindBorderOaPrivRelaxedInternal called with arguments:");
            System.out.println("  User name:" + sUserName);
            System.out.println("  User id: " + sUserId);
        }

        // Uses a hashtable htReachableOas of reachable oas (see find_border_oa_priv(u))
        // An oa is a key in this hashtable. The value is another hashtable that
        // represents a label of the oa. A label is a set of pairs {op -> pcset}, with
        // the op being the key and pcset being the value.
        // 			htReachableOas = {oa -> {op -> pcset}}.


        Hashtable htReachableOas = new Hashtable();

        // BFS from u (the base node). Prepare a queue.
        ArrayList queue = new ArrayList();
        HashSet visited = new HashSet();
        String sCrtId;

        try {
            // Insert u's directly assigned attributes into the queue.
            HashSet hsContainers = graphMgr.getGraph().getNode(sUserId).getContainers();
            Iterator itContainers = hsContainers.iterator();
            while (itContainers.hasNext()) {
                String sAttrId = (String)itContainers.next();

                queue.add(sAttrId);
                if (bMellPrint) System.out.println("Attribute "
                        + graphMgr.getGraph().getNode(sAttrId).getName()
                        + " has been inserted into the queue");
            }

            // While the queue has elements, extract an element from the queue
            // and visit it.
            while (!queue.isEmpty()) {
                // Extract an ua from queue.
                sCrtId = (String)queue.remove(0);

                if (!visited.contains(sCrtId)) {
                    if (bMellPrint) System.out.println("User attribute " +
                            graphMgr.getGraph().getNode(sCrtId).getName() +
                            " is being visited");

                    // If the ua has ua -> oa edges
                    if (inMemUattrHasOpsets(sCrtId)) {
                        if (bMellPrint) System.out.println("User attribute " +
                                graphMgr.getGraph().getNode(sCrtId).getName() +
                                " has opsets!");
                        // From each discovered ua traverse the edges ua -> oa.
                        // Find the opsets of this user attribute. Note that for
                        // the user attribute the opsets are containers, but
                        // not all containers of the user attribute are opsets.
                        HashSet hsOpsets = graphMgr.getGraph().getNode(sCrtId).getContainers();
                        // Go through the containers and only for each opset
                        Iterator hsOpsetsIter = hsOpsets.iterator();
                        while (hsOpsetsIter.hasNext()) {
                            String sOpsetId = (String)hsOpsetsIter.next();
                            if (graphMgr.getGraph().getNode(sOpsetId).getType().equalsIgnoreCase(PM_NODE.OPSET.value)) {
                                // Find the object attributes of this opset
                                HashSet hsOas = graphMgr.getGraph().getNode(sOpsetId).getContainers();
                                // For each object attribute oa of this opset
                                Iterator hsOasIter = hsOas.iterator();
                                while (hsOasIter.hasNext()) {
                                    String sOaId = (String)hsOasIter.next();
                                    // Compute the pc nodes reachable from oa
                                    HashSet hsPcs = inMemFindPcSet(sOaId, PM_NODE.OATTR.value);
                                    // If oa is in htReachableOas
                                    if (htReachableOas.containsKey(sOaId)) {
                                        // oa has a label op1 -> hsPcs, op2 -> hsPcs,...
                                        // Extract its label
                                        Hashtable htOaLabel = (Hashtable)htReachableOas.get(sOaId);
                                        // Get the operations from opset
                                        HashSet hsOpers = graphMgr.getGraph().getNode(sOpsetId).getOperations();
                                        // For each operation
                                        Iterator hsOpersIter = hsOpers.iterator();
                                        while (hsOpersIter.hasNext()) {
                                            String sOp = (String)hsOpersIter.next();
                                            // If the oa's label already contains the op
                                            if (htOaLabel.containsKey(sOp)) {
                                                // Don't do anything
                                            } else { // the op is not in the label. Create new op -> pcs pair in the label
                                                HashSet hsNewPcs = new HashSet(hsPcs);
                                                htOaLabel.put(sOp,  hsNewPcs);
                                            }
                                        }
                                    } else { // oa is not in htReachableOas
                                        // Prepare a new label
                                        Hashtable htOaLabel = new Hashtable();
                                        // Get the operations from the opset.
                                        HashSet hsOpers = graphMgr.getGraph().getNode(sOpsetId).getOperations();
                                        // For each operation
                                        Iterator hsOpersIter = hsOpers.iterator();
                                        while (hsOpersIter.hasNext()) {
                                            String sOp = (String)hsOpersIter.next();
                                            // add op -> pcs to the label
                                            HashSet hsNewPcs = new HashSet(hsPcs);
                                            htOaLabel.put(sOp, hsNewPcs);
                                        }
                                        // Add oa -> {op -> pcs}
                                        htReachableOas.put(sOaId,  htOaLabel);
                                    }
                                }
                            }
                        }
                    }
                    visited.add(sCrtId);
                    // Insert all user attributes that are descendants of the crt
                    // user attribute into the queue. Note that not all descendants
                    // of the user attribute are user attribute, you have to check
                    // the type.
                    HashSet hsDescs = graphMgr.getGraph().getNode(sCrtId).getContainers();
                    Iterator descsIter = hsDescs.iterator();
                    while (descsIter.hasNext()) {
                        String sAttrId = (String)descsIter.next();
                        String sAttrType = graphMgr.getGraph().getNode(sAttrId).getType();
                        if (sAttrType.equalsIgnoreCase(PM_NODE.UATTR.value)){
                            queue.add(sAttrId);
                        }
                    }
                }
            }


            // Print htReachableOas.
            if (bMellPrint) {
                System.out.println("TABLE OF REACHABLE OAS");
                for (Enumeration keys = htReachableOas.keys(); keys.hasMoreElements() ;) {
                    String sKey = (String)keys.nextElement();
                    String sName = graphMgr.getGraph().getNode(sKey).getName();
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
                            System.out.print(graphMgr.getGraph().getNode(sPcId).getName() + ", ");
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

    HashSet inMemGetContainers(String sId) {
        return graphMgr.getGraph().getNode(sId).getContainers();
    }

    // For graph elements get the names from ids. For operations, there is no need.
    void inMemPrintSet(HashSet hs, String sCaption, Boolean bGetNames) {
        Iterator iter = hs.iterator();
        System.out.println("Start of " + sCaption);
        while (iter.hasNext()) {
            String sElId = (String)iter.next();
            if (bGetNames) {
                String sElName = graphMgr.getGraph().getNode(sElId).getName();
                String sElType = graphMgr.getGraph().getNode(sElId).getType();

                System.out.println(sElId + "/" + sElName + "/" + sElType);
            } else {
                System.out.println(sElId);
            }
        }
        System.out.println("End of " + sCaption);
    }

    // Serban - This is the InMemory version.
    private Hashtable findBorderOaPrivRestrictedInternal(String sUserName, String sUserId) {
        if (bMellPrint) {
            System.out.println("findBorderOaPrivRestrictedInternal called with arguments:");
            System.out.println("  User name:" + sUserName);
            System.out.println("  User id: " + sUserId);
        }
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

        // Get u's directly assigned attributes and put them into the queue.
        HashSet hsAttrs = graphMgr.getGraph().getNode(sUserId).getContainers();
        Iterator hsiter = hsAttrs.iterator();
        while (hsiter.hasNext()) {
            String sAttrId = (String)hsiter.next();
            String sAttrType = graphMgr.getGraph().getNode(sAttrId).getType();
            String sAttrName = graphMgr.getGraph().getNode(sAttrId).getName();
            queue.add(sAttrId);
            if (bMellPrint) System.out.println("Attribute " + sAttrName
                    + " has been inserted into the queue");
        }

        // While the queue has elements, extract an element from the queue
        // and visit it.
        while (!queue.isEmpty()) {
            // Extract an ua from queue.
            sCrtId = (String)queue.remove(0);
            String sCrtName = graphMgr.getGraph().getNode(sCrtId).getName();

            if (!visited.contains(sCrtId)) {
                if (bMellPrint) {
                    System.out.println("User attribute "+ sCrtName + " is being visited");
                }
                // If the ua has ua -> oa edges
                if (inMemUattrHasOpsets(sCrtId)) {
                    if (bMellPrint) {
                        System.out.println("User attribute " + " has opsets!");
                    }

                    // Find the set of PCs reachable from ua.
                    HashSet<String> hsUaPcs = inMemFindPcSet(sCrtId, PM_NODE.UATTR.value);
                    if (bMellPrint) printSet(hsUaPcs, PM_NODE.POL.value, "Policies reachable from " + sCrtName);

                    // From each discovered ua traverse the edges ua -> oa.

                    // Find the opsets of this user attribute. Note that the set of containers for this
                    // node (user attribute) may contain not only opsets.
                    HashSet opsets = graphMgr.getGraph().getNode(sCrtId).getContainers();

                    // Go through the containers and only for opsets do the following.
                    // For each opset ops of ua:
                    Iterator opsetsIter = opsets.iterator();
                    while (opsetsIter.hasNext()) {
                        String sOpsetId = (String)opsetsIter.next();
                        // If this is an opset
                        if (graphMgr.getGraph().getNode(sOpsetId).getType().equalsIgnoreCase(PM_NODE.OPSET.value)) {
                            // Find the object attributes of this opset.
                            HashSet oattrs = graphMgr.getGraph().getNode(sOpsetId).getContainers();
                            // For each object attribute oa of this opset
                            Iterator oasIter = oattrs.iterator();
                            while (oasIter.hasNext()) {
                                String sOaId = (String)oasIter.next();
                                // If oa is in htReachableOas
                                if (htReachableOas.containsKey(sOaId)) {
                                    // Then oa has a label op1 -> hsPcs1, op2 -> hsPcs2,...
                                    // Extract its label:
                                    Hashtable htOaLabel = (Hashtable)htReachableOas.get(sOaId);

                                    // Get the operations from the opset:
                                    HashSet opers = graphMgr.getGraph().getNode(sOpsetId).getOperations();
                                    // For each operation in the opset
                                    Iterator opersIter = opers.iterator();
                                    while (opersIter.hasNext()) {
                                        String sOp = (String)opersIter.next();
                                        // If the oa's label already contains the operation sOp
                                        if (htOaLabel.containsKey(sOp)) {
                                            // The label contains op -> some pcset.
                                            // Do the union of the old pc with ua's pcset
                                            HashSet hsPcs = (HashSet)htOaLabel.get(sOp);
                                            hsPcs.addAll(hsUaPcs);
                                        } else {
                                            // The op is not in the oa's label.
                                            // Create new op -> ua's pcs mappiing in the label.
                                            HashSet hsNewPcs = new HashSet(hsUaPcs);
                                            htOaLabel.put(sOp, hsNewPcs);
                                        }
                                    }
                                } else {
                                    // oa is not in htReachableOas.
                                    // Prepare a new label
                                    Hashtable htOaLabel = new Hashtable();

                                    // Get the operations from the opset:
                                    HashSet opers = graphMgr.getGraph().getNode(sOpsetId).getOperations();
                                    // For each operation in the opset
                                    Iterator opersIter = opers.iterator();
                                    while (opersIter.hasNext()) {
                                        String sOp = (String)opersIter.next();
                                        // Add op -> pcs to the label.
                                        HashSet hsNewPcs = new HashSet(hsUaPcs);
                                        htOaLabel.put(sOp,  hsNewPcs);
                                    }

                                    // Add oa -> {op -> pcs}
                                    htReachableOas.put(sOaId,  htOaLabel);
                                }
                            }
                        }
                    }
                }
                visited.add(sCrtId);

                HashSet hsDescs = graphMgr.getGraph().getNode(sCrtId).getContainers();
                Iterator descsIter = hsDescs.iterator();
                while (descsIter.hasNext()) {
                    String sAttrId = (String)descsIter.next();
                    String sAttrType = graphMgr.getGraph().getNode(sAttrId).getType();
                    String sAttrName = graphMgr.getGraph().getNode(sAttrId).getName();
                    if (sAttrType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                        queue.add(sAttrId);
                    }
                }
            }
        }

        // Print htReachableOas.
        if (bMellPrint) {
            System.out.println("TABLE OF REACHABLE OAS BEFORE");
            for (Enumeration keys = htReachableOas.keys(); keys.hasMoreElements() ;) {
                String sKey = (String)keys.nextElement();
                String sName = graphMgr.getGraph().getNode(sKey).getName();
                //System.out.println("Object attribute " + sName);
                System.out.println(sName + " ---> ");

                Hashtable htLabel = (Hashtable)htReachableOas.get(sKey);
                for (Enumeration ops = htLabel.keys(); ops.hasMoreElements(); ) {
                    String sOp = (String)ops.nextElement();
                    System.out.print("    " + sOp + " ---> ");

                    HashSet hsPcs = (HashSet)htLabel.get(sOp);
                    //System.out.println("    Policies");
                    Iterator hsPcsIter = hsPcs.iterator();
                    while (hsPcsIter.hasNext()) {
                        String sPcId = (String)hsPcsIter.next();
                        String sPcName = graphMgr.getGraph().getNode(sPcId).getName();
                        System.out.print(sPcName + ", ");
                    }
                    System.out.println();
                }
            }
        }

        // For each reachable oa in htReachableOas.keys
        for (Enumeration keys = htReachableOas.keys(); keys.hasMoreElements() ;) {
            String sOaId = (String)keys.nextElement();
            // Compute {pc | oa ->+ pc}
            HashSet hsOaPcs = inMemFindPcSet(sOaId, PM_NODE.OATTR.value);
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
                String sName = graphMgr.getGraph().getNode(sKey).getName();
                //System.out.println("Object attribute " + sName);
                System.out.println(sName + " ---> ");

                Hashtable htLabel = (Hashtable)htReachableOas.get(sKey);
                for (Enumeration ops = htLabel.keys(); ops.hasMoreElements(); ) {
                    String sOp = (String)ops.nextElement();
                    System.out.print("    " + sOp + " ---> ");

                    HashSet hsPcs = (HashSet)htLabel.get(sOp);
                    //System.out.println("    Policies");
                    Iterator hsPcsIter = hsPcs.iterator();
                    while (hsPcsIter.hasNext()) {
                        String sPcId = (String)hsPcsIter.next();
                        String sPcName = graphMgr.getGraph().getNode(sPcId).getName();
                        System.out.print(sPcName + ", ");
                    }
                    System.out.println();
                }
            }
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
    public HashSet findPcSet(String sStartNodeId, String sAttrType) throws Exception {
        // The set of pcs reachable from the start node.
        HashSet reachable = new HashSet();

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, Integer.valueOf(sStartNodeId));

        ArrayList<Integer> pcs = extractIntegers(select(GET_REACHABLE_PC_NODES, params));
        for (Integer pc : pcs){
            reachable.add(String.valueOf(pc));
        }

        return reachable;
    }

    // The first argument is a user attribute or an object attribute.
    // The second argument is the type of the first argument.
    // Returns the set of pc nodes reachable from the start node.
    public HashSet inMemFindPcSet(String sStartId, String sAttrType) {
        HashSet reachable = new HashSet();

        // Init the queue, visited
        ArrayList queue = new ArrayList();
        HashSet visited = new HashSet();

        // The current element
        String sCrtId = null;

        // Insert the start node into the queue
        queue.add(sStartId);

        // While queue is not empty
        while (!queue.isEmpty()) {
            // Extract current element from queue
            sCrtId = (String)queue.remove(0);
            // If not visited
            if (!visited.contains(sCrtId)) {
                // Mark it as visited
                visited.add(sCrtId);
                // Extract its direct descendants. If a descendant is an attribute,
                // insert it into the queue. If it is a pc, add it to reachable,
                // if not already there
                HashSet hsContainers = graphMgr.getGraph().getNode(sCrtId).getContainers();
                Iterator hsiter = hsContainers.iterator();
                while (hsiter.hasNext()) {
                    String sId = (String)hsiter.next();
                    String sType = graphMgr.getGraph().getNode(sId).getType();
                    if (sType.equalsIgnoreCase(sAttrType)) {
                        queue.add(sId);
                    } else if (sType.equals(PM_NODE.POL.value)) {
                        reachable.add(sId);
                    }
                }
            }
        }
        return reachable;
    }

    // Get all operations of an opset in a vector.
    public Vector<String> getOpsetOperations(String sOpsetId) throws Exception {
        Vector<String> res = new Vector<String>();
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sOpsetId==null?null:Integer.valueOf(sOpsetId));

        ArrayList<ArrayList<Object>> operations = select(GET_OPSET_OPS, params);
        for(ArrayList<Object> operation : operations){
            res.add((String) operation.get(0));
        }
        return res;
    }

    public boolean oattrIsAccessibleInternal(String sUserId, String sOaId) throws Exception {
        //return true;
        MySQL_Parameters params = new MySQL_Parameters();
        params.setOutParamType(ParamType.STRING);
        params.addParam(ParamType.INT, sUserId==null?null:Integer.valueOf(sUserId));
        params.addParam(ParamType.INT, sOaId==null?null:Integer.valueOf(sOaId));
        Object returned = executeFunction(IS_ACCESSIBLE, params);
        if(returned == null){
            return false;
        }
        Integer isAccessible = Integer.valueOf((String)returned);
        return isAccessible != 0;
    }

    public Packet oattrIsAccessible(String sUserId, String sOaId){
        Packet p = new Packet();
        try {
            p.addItem(ItemType.RESPONSE_TEXT, String.valueOf(oattrIsAccessibleInternal(sUserId, sOaId)));
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }


    // Given a proper file name (not the entire path), this function returns
    // a suitable object name.
    // Strating with the file name (excluding the file type), check
    // to see whether an object attribute with that name exists.
    // If yes, try that name with a '1' appended, then a '2', etc.,
    // until the object attribute with that name does not exist.

    public String getUniqueObjName(String sFileName) throws Exception{
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

    public Packet getMailboxMessages(String sSessId, String sPropPrefix) {
        String sUserId;
        String sUserName;
        try {
            sUserId = getSessionUserIdInternal(sSessId);
            if (sUserId == null) {
                return failurePacket("Couldn't find the session or the user id!");
            }
            sUserName = getEntityName(sUserId, PM_NODE.USER.value);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        if (sUserName == null) {
            return failurePacket("Couldn't find the session user!");
        }
        String sProp = sPropPrefix + sUserName;
        try {
            String sMailboxId = getEntityWithPropInternal(PM_NODE.OATTR.value, sProp);
            if (sMailboxId == null) {
                return failurePacket("No object attribute with property \"" + sProp
                        + "\"!");
            }

            // We found the session's user's mailbox. Get all objects that are
            // messages,
            // i.e., have pmEmlRecip.
            Packet result = new Packet();
            ArrayList<Integer> mail = getFromAttrs(sMailboxId, PM_NODE.ASSOC.value, DEPTH);
            System.out.println(mail);
            if (mail != null) {
                for (Integer oa : mail) {
                    String sOattrId = String.valueOf(oa);
                    String sObjId = sOattrId;//getAssocObj(sOattrId);

                    // Note that the INBOX could contain oattrs, for example
                    // wINBOX,
                    // which do not have associated objects. Skip them.
                    if (sObjId == null) {
                        continue;
                    }

                    MySQL_Parameters params = new MySQL_Parameters();
                    params.addParam(ParamType.INT, sObjId);

                    ArrayList<ArrayList<Object>> details = select(GET_EMAIL_DETAIL, params);
                    if (details != null && details.size() > 0) {
                        ArrayList<Object> emailProps = details.get(0);

                        String sRecip = (String) emailProps.get(1);
                        // For the messages in the inbox, the recipient is the user.
                        // For the messages in the outbox, the recipient may be
                        // another user.
                        String sSender = (String) emailProps.get(0);
                        Timestamp sDate = (Timestamp) emailProps.get(2);
                        String sSubject = (String) emailProps.get(3);
                        String sLabel = getEntityName(sObjId, PM_OBJ);

                        ArrayList<ArrayList<Object>> atts = select(GET_ATTACHMENTS, params);
                        if (atts != null && atts.size() > 0) {
                            ArrayList<Integer> attachments = extractIntegers(atts);
                            //if (attachments != null && attachments.size() > 0) {
                            //for (Integer i : attachments) {
                            result.addItem(ItemType.RESPONSE_TEXT, sLabel
                                    + PM_ALT_FIELD_DELIM + getEntityName(String.valueOf(attachments.get(0)), PM_NODE.OATTR.value)
                                    + PM_ALT_FIELD_DELIM + sSender
                                    + PM_ALT_FIELD_DELIM + sRecip
                                    + PM_ALT_FIELD_DELIM + sDate
                                    + PM_ALT_FIELD_DELIM + sSubject);
                            //}
                            //}
                        } else {
                            result.addItem(ItemType.RESPONSE_TEXT, sLabel
                                    + PM_ALT_FIELD_DELIM + PM_ALT_FIELD_DELIM
                                    + sSender + PM_ALT_FIELD_DELIM + sRecip
                                    + PM_ALT_FIELD_DELIM + sDate
                                    + PM_ALT_FIELD_DELIM + sSubject);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("reached end of getMessagesOf so returning failure");
        }
    }


    public boolean userDetailExists(int userId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, userId);
        ArrayList<ArrayList<Object>> returned = select(USER_DETAIL_EXISTS, params);
        Boolean userExists = false;
        if (returned!= null && returned.size() > 0) {
            userExists = (Long)returned.get(0).get(0) == 1;
        }
        return userExists;
    }

    public boolean addEmailAcctInternal(String sPmUser, String sFullName,
                                        String sEmailAddr, String sPopServer, String sSmtpServer,
                                        String sAcctName, String sPassword) {
        // Test if account already exists.
        MySQL_Parameters params = new MySQL_Parameters();
        //params.setOutParamType(ParamType.INT);
        //params.addParam(ParamType.STRING, sPmUser);
        //params.addParam(ParamType.STRING, PM_NODE.USER.value);
        Integer userId = null;
        try{
            userId = Integer.valueOf(getEntityId(sPmUser, PM_NODE.USER.value));//(Integer)executeFunction(GET_ID_OF_ENTITY_WITH_NAME_FUN, params);
            if (userId == null) {
                errorMessage = "No such PM user!";
                return false;
            }
            if (userDetailExists(userId)) {
                System.out.println("Email account already exists. Modifying...");
                params.clearParams();
                params.addParam(ParamType.STRING, sPmUser);
                params.addParam(ParamType.STRING, sEmailAddr);
                params.addParam(ParamType.STRING, sPopServer);
                params.addParam(ParamType.STRING, sSmtpServer);
                params.addParam(ParamType.STRING, sAcctName);
                //params.addParam(ParamType.STRING, sPassword);
                params.addParam(ParamType.INT, userId);
                update(UPDATE_EMAIL_ACCT, params);
            } else {
                System.out.println("Email account new. Setting it up...");
                params.clearParams();
                params.addParam(ParamType.INT, userId);
                params.addParam(ParamType.STRING, sPmUser);
                params.addParam(ParamType.STRING, sEmailAddr);
                params.addParam(ParamType.STRING, sPopServer);
                params.addParam(ParamType.STRING, sSmtpServer);
                params.addParam(ParamType.STRING, sAcctName);
                //params.addParam(ParamType.STRING, sPassword);
                insert(ADD_EMAIL_ACCT, params);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return false;
        }
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
                                                     String sSuggBaseType, boolean isSubgraph) throws Exception{
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
                                             boolean isSubgraph) throws Exception {
        // Search PmVirtualObjContainer for objects x such that:
        // x represents a PM entity and
        // x.origid = entity.id and
        // x.includesAscs = isSubgraph
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, Integer.valueOf(sEntId));
        params.addParam(ParamType.STRING, isSubgraph==true?1:0);
        ArrayList<ArrayList<Object>> returned = select(GET_OATTR_REPRESENTING_ENTITY, params);
        if (returned!= null && returned.size() > 0) {
            Integer oAttr =  (Integer)returned.get(0).get(0);
            return oAttr.toString();
        } else return null;
    }

    // Returns true iff the oattr is associated to an object that represents
    // the PM entity or subgraph, according to the param isSubgraph.

    public boolean oattrRepresentsThisEntity(String sOattrId, String sEntId,
                                             String sEntType, boolean isSubgraph) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, Integer.valueOf(sOattrId));
        params.addParam(ParamType.INT, Integer.valueOf(sEntId));
        params.addParam(ParamType.STRING, isSubgraph?1:0);
        ArrayList<ArrayList<Object>> returned = select(OATTR_REPRESENTS_THIS_ENTITY, params);
        if (returned!= null && returned.size()>0) {
            return true;
        } else return false;
    }
    // Returns true iff the oattr is associated to an object that represents
    // a PM entity.

    public boolean oattrRepresentsAnEntity(String sOattrId) throws Exception {
        // Get the assoc object, if any.
        Integer origObjId = getOriginalObjId(Integer.valueOf(sOattrId));
        return origObjId==null?false:true;
    }

    public Integer getOriginalObjId(Integer objId) throws Exception {

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, objId);
        ArrayList<ArrayList<Object>> results = select(GET_ORIG_OBJ_ID, params);
        if(results == null || results.isEmpty()){
            return null;
        }
        return (Integer) results.get(0).get(0);
    }

    // Create an opset with the operations specified in opSet and insert it
    // between
    // the user attribute and the object attribute. Return the id of the new
    // opset
    // or null in case of error.

    public Packet createOpsetBetween(String sSessId, String sProcId,
                                     HashSet<String> opSet, String sUattrId, String sOattrId) {
        Packet res = null;
        String sOpsetName = null;
        Integer opsetId = null;
        String ops = "";
        res = new Packet();
        try {
            // Check some permissions.
            if (!requestAddOpsetPerms(sSessId, sProcId, sOattrId, PM_NODE.OATTR.value)) {
                return failurePacket(reqPermsMsg);
            }
            if (!requestAssignPerms(sSessId, sProcId, sUattrId,PM_NODE.UATTR.value, String.valueOf(opsetId), PM_NODE.OPSET.value)) {
                return failurePacket(reqPermsMsg);
            }

            // Generate a name for the operation set.
            Random random = new Random();
            byte[] bytes = new byte[4];
            random.nextBytes(bytes);
            sOpsetName = UtilMethods.byteArray2HexString(bytes);

            Iterator<String> hsiter = opSet.iterator();
            while (hsiter.hasNext()) {
                ops += hsiter.next()+",";
            }
            ops = ops.substring(0, ops.length()-1);

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sOattrId);
            params.addParam(ParamType.INT, sUattrId);
            params.addParam(ParamType.STRING, sOpsetName);
            params.addParam(ParamType.STRING, ops);
            executeStoredProcedure(CREATE_ASSOCIATION, params);
            opsetId = Integer.valueOf(getEntityId(sOpsetName, PM_NODE.OPSET.value));

            // Prepare the result for the successful case.
            res.addItem(ItemType.RESPONSE_TEXT, sOpsetName
                    + PM_FIELD_DELIM + opsetId);
            addPmGraphNode(sOattrId, opsetId.toString(), sOpsetName, PM_NODE.OPSET.value);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    // Create a representative object for the specified entity. Assign the
    // associated object attribute to the base node, which can be a policy class
    // or an object attribute.
    // It returns the id of the associated object attribute or null.

    public String createOattrRepresentingEntity(String sBaseId,
                                                String sBaseType, String sEntId, String sEntType, boolean bInh) throws Exception {
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
        Integer objId =  addObjectInternal(sObjName, null, null,
                "Representative", "No info.", sBaseId, sBaseType, sObjClass,
                null, null, null, sEntName, sEntId, bInh, null, null, null,
                null, null, null, null);
        if (objId == null) {
            System.out.println("CreateOattrReprresentingEntity: could not create rep object. "
                    + errorMessage);
            return null;
        }
        return objId.toString();
    }

    // Set the opset's operations as specified by the list sOps, where the ops
    // are separated by commas.

    public void setOpsetOps(String sOpsetId, String sOps) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sOpsetId);

        delete(DELETE_OPSET_OPS, params);

        // Add the new operations.
        if (sOps != null) {
            String[] pieces = sOps.split(",");
            int n = pieces.length;
            for (int i = 0; i < n; i++) {
                params.addParam(ParamType.STRING, pieces[i]);
                insert(ADD_OPSET_OP, params);
            }
        }
    }

    public Map<String, String> getContainersWithType(int id) throws Exception{
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, id);
        Map<String, String> containers = new HashMap<String, String>();
        ArrayList<ArrayList<Object>> ary = select(GET_TO_ATTRS_WITH_TYPE, params);
        for(ArrayList<Object> line : ary){
            containers.put((String)line.get(0), (String)line.get(1));
        }
        return containers;
    }

    // Get a string with the type and names of the containers of an object
    // attribute identified by its id. The containers include other object
    // attributes, policy classes, and the connector node.
    // An example of the result: "b|Med Records,p|DAC,c|PM".

    public String getOattrContainers(String sOattrId) throws NumberFormatException, Exception {
        StringBuffer sb = new StringBuffer();
        boolean first = true;

        Map<String, String> attr = getContainersWithType(Integer.valueOf(sOattrId));
        for (Map.Entry<String, String> entry : attr.entrySet()) {
            if (first) {
                sb.append(entry.getValue() + "|" + entry.getKey());
                first = false;
            } else {
                sb.append("," +entry.getValue() + "|" + entry.getKey());
            }
        }
        return sb.toString();
    }

    // Copy assignments (to policies, to other attributes, or from opsets)
    // from object attribute 1 to object attribute 2.

    public Packet copyOattrAssignments(String sOattrId1, String sOattrId2) {
        Packet res;
        try {
            // Copy links to policy classes.
            ArrayList<Integer> attr = getToPolicies(sOattrId1,PM_NODE.OATTR.value);
            if (attr != null) {
                for (Integer i : attr) {
                    String sId = String.valueOf(i);

                    System.out.println("Copy assignment "
                            + getEntityName(sOattrId2,PM_NODE.OATTR.value) + " --> "
                            + getEntityName(sId,PM_NODE.POL.value));

                    if(!addDoubleLink(sId, sOattrId2)){
                        return fail();
                    }
                }
            }

            // Copy links to other object attributes.
            attr = getToAttrs(sOattrId1,PM_NODE.OATTR.value, DEPTH);
            if (attr != null) {
                for (Integer i : attr) {
                    String sId = String.valueOf(i);
                    System.out.println("Copy assignment "
                            + getEntityName(sOattrId2,PM_NODE.OATTR.value) + " --> "
                            + getEntityName(sId,PM_NODE.OATTR.value));
                    if(!addDoubleLink(sId, sOattrId2)){
                        return fail();
                    }
                }
            }

            // Copy links from opsets.
            attr = getFromOpsets(sOattrId1, PM_NODE.OATTR.value);
            if (attr != null) {
                for (Integer i : attr) {
                    String sId = String.valueOf(i);
                    System.out.println("Copy assignment "
                            + getEntityName(sId,PM_NODE.OPSET.value) + " --> "
                            + getEntityName(sOattrId2,PM_NODE.OATTR.value));
                    if(!addDoubleLink(sId, sOattrId2)){
                        return fail();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception in copyAssignments: "
                    + e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }


    public Packet isolateOattr(String sAttrId) {
        Packet res;
        try {
            // Cut link to connector node, if it exists.
            ArrayList<Integer> attr = getToConnector(sAttrId,PM_NODE.OATTR.value);
            if (attr != null) {
                if(!deleteAssignment(Integer.valueOf(sAttrId), Integer.valueOf(PM_CONNECTOR_ID))){
                    return fail();
                }
            }
        } catch (Exception e) {
        }

        try {
            // Cut links to policy classes.
            ArrayList<Integer> attr = getToPolicies(sAttrId,PM_NODE.OATTR.value);
            if (attr != null) {
                for (Integer i : attr) {
                    String sId = String.valueOf(i);
                    if(!deleteAssignment(Integer.valueOf(sAttrId), Integer.valueOf(sId))){
                        return fail();
                    }
                }
            }

            // Cut links to other object attributes.
            attr = getToAttrs(sAttrId,PM_NODE.OATTR.value, DEPTH);
            if (attr != null) {
                for (Integer i : attr) {
                    String sId = String.valueOf(i);
                    if(!deleteAssignment(Integer.valueOf(sAttrId),Integer.valueOf(sId))){
                        return fail();
                    }
                }
            }

            // Cut links from opsets.
            attr = getFromOpsets(sAttrId,PM_NODE.OATTR.value);
            if (attr != null) {
                for (Integer i : attr) {
                    String sId = String.valueOf(i);
                    if(!deleteAssignment(Integer.valueOf(sId),Integer.valueOf(sAttrId))){
                        return fail();
                    }
                }
            }
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return failurePacket("Exception in isolateOattr: " + e.getMessage());
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    // At least one of the session's active attributes must be in the
    // specified policy.

    public boolean userIsActiveInPolicy(HashSet<String> activeAttrs,
                                        String sPolId) throws Exception {
        Iterator<String> iter = activeAttrs.iterator();
        while (iter.hasNext()) {
            String sAttrId = iter.next();
            if (attrIsAscendantToPolicy(sAttrId,sPolId)) {
                return true;
            }
        }
        return false;
    }

    public boolean addPropInternal(Integer sId, String sType, String sProp) {
        if (!(sType.equalsIgnoreCase(PM_NODE.UATTR.value) ||
                (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) ||
                (sType.equalsIgnoreCase(PM_NODE.POL.value)))) {
            errorMessage = "Invalid entity type!";
            return false;
        }

        if(sProp == null || sProp.equals("")){
            errorMessage = "property cannot be empty or null";
            return false;
        }

        if(!sProp.contains(PM_PROP_DELIM)){
            errorMessage = "property is not formatted correctly: property=value";
            return false;
        }

        String pieces[] = sProp.split(PM_PROP_DELIM);

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, pieces[0]);
        params.addParam(ParamType.STRING, pieces[1]);
        params.addParam(ParamType.INT, sId);

        try {
            insert(INSERT_PROPERTY, params);
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Unable to add property " + sProp
                    + " to the entity!";
            return false;
        }
        return true;
    }

    // Get the properties set of a PM entity.

    public HashSet<String> getProps(String sId, String sType) throws Exception{
        HashSet<String> hs = new HashSet<String>();

        if (!(sType.equalsIgnoreCase(PM_NODE.UATTR.value)
                || (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) || (sType
                .equalsIgnoreCase(PM_NODE.POL.value)))) {
            return hs;
        }

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId);

        ArrayList<ArrayList<Object>> result = select(GET_NODE_PROPERTIES,
                params);
        for (ArrayList<Object> prop : result) {
            hs.add(String.valueOf(prop));
        }

        return hs;
    }

    // Return the property of the entity specified by id and type that starts
    // with the specified prefix, if one exists. Otherwise return null.

    public String getPropertyWithPrefix(String sPrefix, String sId,String sType) throws Exception {
        HashSet<String> props;
        props = getProps(sId, sType);
        Iterator<String> propIter = props.iterator();
        while (propIter.hasNext()) {
            String prop = propIter.next();
            if (prop.indexOf(sPrefix) == 0) {
                return prop;
            }
        }
        return null;
    }

    private ArrayList<String> getNodeInfo(String sId) throws Exception{
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId);

        ArrayList<ArrayList<Object>> result = select(GET_NODE_INFO, params);
        ArrayList<Object> pc = result.get(0);
        String name = (String) pc.get(0);
        String id = String.valueOf(pc.get(1));
        String descr = (String) pc.get(2);
        ArrayList<String> ret = new ArrayList<String>();
        ret.add(name);
        ret.add(id);
        ret.add(descr);
        return ret;
    }

    public String getUserFullName(String sUserId) throws Exception {
        String userFullName = "";

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sUserId);
        ArrayList<ArrayList<Object>> result = select(GET_USER_FULL_NAME, params);
        ArrayList<String> name = extractStrings(result);
        userFullName = name.get(0);
        return userFullName;
    }

    public boolean deleteDenyInternal(String sDenyId, String sOp,
                                      String sOattrName, String sOattrId){
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, Integer.valueOf(sDenyId));
            delete(DELETE_DENY, params);

            denyMgr.deleteDeny(denyMgr.getDeny(sDenyId));
            return true;
        }catch(Exception e){
            errorMessage = "could not delete deny: " + e.getMessage();
            return false;
        }
    }

    public Integer addDenyInternal(String sDenyName, String sDenyType,
                                   String sDenyToName, String sDenyToId, String sOp,
                                   String sOattrName, String sOattrId, boolean bInters) throws Exception {
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

        boolean complement = false;
        if ((sOattrName != null && sOattrName.startsWith("!"))) {
            System.out.println("Complementary container!");
            sOattrName = sOattrName.substring(1);
            complement = true;
        }
        if ((sOattrId != null && sOattrId.startsWith("!"))) {
            System.out.println("Complementary container!");
            sOattrId = sOattrId.substring(1);
            complement = true;
        }

        ArrayList<String> result;
        // First check whether the deny exists.
        String sDenyId;
        sDenyId = getEntityId(sDenyName, PM_DENY);
        if (sDenyId != null) {
            // A deny constraint with that name already exists.
            // Do a series of checks.
            result = getDenySimpleInfo(sDenyId);
            // The information returned by getDenySimpleInfo has the following
            // format:
            // item 0: <deny name>:<deny id>
            // item 1: <deny type>:<denyto name>:<denyto id>:<is intersection>
            if (result == null) {
                return null;
            }
            String sLine = result.get(1);
            String[] pieces = sLine.split(PM_FIELD_DELIM);

            if (sDenyType != null && !sDenyType.equals(pieces[0])) {
                errorMessage = "In deny, the type does not match its registered type!";
                return null;
            }

            if (sDenyToName != null && !sDenyToName.equals(pieces[1])) {
                errorMessage = "In deny, the session/process/user/attribute name does not match the registered name!";
                return null;
            }

            if (sDenyToId != null && !sDenyToId.equals(pieces[2])) {
                errorMessage = "In deny, the session/process/user/attribute id does not match the registered id!";
                return null;
            }

        } else { // New constraint.
            // A series of checks.
            if (sDenyType == null
                    || (!sDenyType.equalsIgnoreCase(PM_DENY_USER_ID)
                    && !sDenyType.equalsIgnoreCase(PM_DENY_USER_SET)
                    && !sDenyType.equalsIgnoreCase(PM_DENY_SESSION)
                    && !sDenyType.equalsIgnoreCase(PM_DENY_PROCESS)
                    && !sDenyType.equalsIgnoreCase(PM_DENY_INTRA_SESSION) && !sDenyType.equalsIgnoreCase(PM_DENY_ACROSS_SESSIONS))) {
                errorMessage = "Null or invalid deny type!";
                return null;
            }
            if (sDenyToName == null) {
                errorMessage = "Please select a session, process, user or user attribute name!";
                return null;
            }

            String sId;
            if (sDenyType.equals(PM_DENY_SESSION)) {
                sId = getEntityId(sDenyToName, PM_SESSION);
            } else if (sDenyType.equals(PM_DENY_PROCESS)) {
                sId = sDenyToName;
            } else if (sDenyType.equals(PM_DENY_USER_ID)) {
                sId = getEntityId(sDenyToName, PM_NODE.USER.value);
            } else {
                sId = getEntityId(sDenyToName, PM_NODE.UATTR.value);
            }

            if (sDenyToId != null && !sDenyToId.equals(sId)) {
                errorMessage = "In deny, the session/process/user/user attribute id does not match the registered id!";
                return null;
            }

            // Anyway, set the session, user or attr id:
            sDenyToId = sId;

            Integer processId = -1;
            Integer userId = -1;
            //create the new deny
            if(sDenyType.equals(PM_DENY_PROCESS)){
                processId = Integer.valueOf(sDenyToId);
                insert(ADD_PROCESS_DENY, sDenyName, sDenyType, processId, bInters ? 1 : 0);
            }else{
                userId = Integer.valueOf(sDenyToId);
                insert(ADD_DENY, sDenyName, sDenyType, userId, bInters ? 1 : 0);
            }

            sDenyId = getEntityId(sDenyName, PM_DENY);
            if (sDenyId == null) {
                errorMessage = "error creating deny constraint";
                return null;
            }

            HashSet<DenyObject> denyObjects = new HashSet<DenyObject>();
            if (sOattrId != null) {
                denyObjects.add(new DenyObject(sOattrId, complement));
            } else if (sOattrName != null) {
                denyObjects.add(new DenyObject(getEntityId(sOattrName, PM_NODE.OATTR.value), complement));
            }

            HashSet<String> ops = new HashSet<String>();
            if (sOp != null && sOp.length() > 0) {
                ops.add(sOp);
            }

            Deny deny = new Deny(sDenyId, sDenyName, sDenyType, processId, userId, bInters, denyObjects, ops);
            denyMgr.addDeny(deny);
            denyMgr.printDenies();
        }

        Integer denyId = Integer.valueOf(sDenyId);
        //update an existing deny -- get deny by id call appropriate Deny methods
        Deny deny = denyMgr.getDeny(sDenyId);

        if (sOp != null) {
            if (!denyHasOp(sDenyId, sOp)) {
                // Add the operation.

                MySQL_Parameters params = new MySQL_Parameters();
                params.addParam(ParamType.INT, Integer.valueOf(sDenyId));
                params.addParam(ParamType.STRING, sOp);
                insert(ADD_DENY_OP, params);

                deny.addOp(sOp);
            }
        }
        if (sOattrId != null) {
            if (!denyHasOattr(sDenyId, sOattrId)) {

                if (sOattrName != null && sOattrName.startsWith("!")) {
                    sOattrId = "!" + sOattrId;
                }
                MySQL_Parameters params = new MySQL_Parameters();
                params.addParam(ParamType.INT, Integer.valueOf(sDenyId));
                params.addParam(ParamType.INT, Integer.valueOf(sOattrId));
                params.addParam(ParamType.INT, complement ? 1 : 0);

                insert(ADD_DENY_OBJ_ID, params);

                deny.addObject(sOattrId, complement);
            }
        }
        denyMgr.updateDeny(deny);
        //}
        denyMgr.printDenies();

        return Integer.valueOf(sDenyId);
    }

    private void addOpToDeny(String sDenyId, String sOp) {
    }

    private boolean addOattrToDeny(String sDenyId, String sOattrId, boolean complement) {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sDenyId);
        params.addParam(ParamType.INT, sOattrId);
        params.addParam(ParamType.INT, complement ? 1 : 0);
        try {
            update(ADD_OATTR_TO_DENY, params);
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return false;
        }
        return true;
    }

    // The information returned by getDenySimpleInfo has the following format:
    // item 0: <deny name>:<deny id>
    // item 1: <deny type>:<user or attribute name>:<user or attribute id>:<is
    // intersection>

    public ArrayList<String> getDenySimpleInfo(String sDenyId) {
        ArrayList<String> info = new ArrayList<String>();

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sDenyId);

        try {
            ArrayList<Object> denyInfo = select(GET_DENY_SIMPLE_INFO, params).get(0);
            String denyName = (String)denyInfo.get(0);
            String denyType = (String)denyInfo.get(1);
            String denyUaName = (String)denyInfo.get(2);
            Integer denyUaId = (Integer)denyInfo.get(3);
            String processId = String.valueOf((Integer)denyInfo.get(4));
            String denyIsIn = (Integer)denyInfo.get(5) == 1 ? YES : NO;

            // First insert the constraint name and id.
            info.add(denyName + PM_FIELD_DELIM
                    + sDenyId);

            String denyToName = denyUaName;
            String denyToId = String.valueOf(denyUaId);
            if(denyToName == null && denyToId.equals("null")){
                denyToName = processId;
                denyToId = processId;
            }

            info.add(denyType + PM_FIELD_DELIM + denyToName + PM_FIELD_DELIM + denyToId + PM_FIELD_DELIM + denyIsIn);
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Exception in getDenySimpleInfo: "
                    + e.getMessage();
            return null;
        }
        return info;
    }

    private boolean objectInDeny(String objId, Deny d){
        //if INTERSEECTION
        //if obj is ascendant of any objects in d, check if that object in d is NOT compliment
        //OR
        //if obj is NOT ascendant of any objects in d, check if those object are compliment
        //ELSE
        //same

        //(is_ascendant_of(obj_id,D.object_attribute_id) AND NOT object_complement)
        // OR (!is_ascendant_of(obj_id,D.object_attribute_id) AND object_complement)
        if (d.isIntersection()) {
            HashSet<DenyObject> objects = d.getObjects();
            return getObjInDenyCount(objId, objects) == objects.size();
        }else{
            HashSet<DenyObject> objects = d.getObjects();
            return getObjInDenyCount(objId, objects) > 0;
        }
    }

    private int getObjInDenyCount(String objId, HashSet<DenyObject> objects){
        try {
            int c = 0;
            for (DenyObject o : objects) {
                if ((memIsAscendant(objId, o.getId()) && !o.isCompliment())
                        || (!memIsAscendant(objId, o.getId()) && o.isCompliment())) {
                    c++;
                }
            }
            return c;
        }catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    // Compute and return the permissions denied to the user/process/session.
    // Parameters:
    // sCrtSessId: the id of the current session. The user can be retrieved.
    // sCrtProcId: the id of the current process. Not used if null.
    // sEntId, sEntType: the id and type of the entity on which we compute
    // the denied permissions.
    public HashSet<String> getDeniedPerms(String sCrtSessId, String sUserId,
                                          String sCrtProcId, String sEntId, String sEntType) throws Exception {
        /*System.out.println("GetDeniedPerms called with arguments:");
        System.out.println("	sCrtSessId: " + sCrtSessId);
        System.out.println("	sUserId: " + sUserId);
        System.out.println("	sCrtProcId: " + sCrtProcId);
        System.out.println("	sEntId: " + sEntId);
        System.out.println("	sEntType: " + sEntType);
        System.out.println("	sEntName: " + getEntityName(sEntId, sEntType));*/

        HashSet<String> deniedOps = new HashSet<String>();

        // For entities other than object attributes, return an empty set of
        // denied ops.
        //		if (!sEntType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
        //			return deniedOps;
        //		}
        // Get the user of the current session.
        /*if ((sUserId == null || sUserId.isEmpty()) *//*&& (sCrtSessId == null || sCrtSessId.isEmpty())*//*)
            return deniedOps;*/

        if (sUserId == null || sUserId.length() == 0) sUserId = getSessionUserIdInternal(sCrtSessId);

        long s = System.nanoTime();
        List<Deny> denies = ServerConfig.denyMgr.getDenies();
        for(Deny d : denies) {
            boolean inter = d.isIntersection();
            HashSet<DenyObject> objects = d.getObjects();
            HashSet<String> ops = d.getOps();
            sCrtProcId = (sCrtProcId == null ? String.valueOf(d.getProcessId()) : sCrtProcId);
            if (memIsAscendant(sUserId, String.valueOf(d.getUserId()))
                    || (!sCrtProcId.equals("-1") && sCrtProcId.equals(String.valueOf(d.getProcessId())))){
                if(objectInDeny(sEntId, d)){
                    deniedOps.addAll(d.getOps());
                }
            }
        }
        long e = System.nanoTime();
        System.out.println("***\nSingle object MEMORY: " + (double)((e-s)/1000000000.0f));
        //(is the user id an ascendant of the id in the deny
        //OR
        //is the process id in the deny)
        //AND
        //is the object in the deny



        /*s = System.nanoTime();
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sCrtProcId);
        params.addParam(ParamType.INT, sUserId);
        params.addParam(ParamType.INT, sEntId);

        ArrayList<ArrayList<Object>> returned = executeStoredProcedure(GET_DENIED_OPS, params);
        if (returned == null || returned.size() == 0) {
            return deniedOps;
        }
        for(ArrayList<Object> op : returned){
            deniedOps.add(op.get(0).toString());
        }
        e = System.nanoTime();
        System.out.println("Single object DISK: " + ((e-s)) + "\n***");*/

        printSet(deniedOps, PM_PERM, "Operations denied for user "
                + getEntityName(sUserId, PM_NODE.USER.value) + " and entity "
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
                                      ArrayList<String> objAttrIds, boolean bInters) throws Exception {
        if (objAttrIds == null) {
            System.out.println("No object attributes in the constraint!");
            return false;
        }
        System.out.println("....ObjIsInDenyList");
        if (bInters) {
            // Do the intersection
            System.out.println("......Do intersection");
            for (String sId : objAttrIds ) {

                if (sId.startsWith("!")) {
                    sId = sId.substring(1);
                    System.out.println("........Processing container C("
                            + getEntityName(sId,PM_NODE.OATTR.value) + ")");
                    if (attrIsAscendant(sEntId, sId)) {
                        System.out.println("..........Obj is ascendant of container, return false");
                        return false;
                    } else {
                        System.out.println("..........Obj is not ascendant of container");
                    }
                } else {
                    System.out.println("........Processing container "
                            + getEntityName(sId,PM_NODE.OATTR.value));
                    if (!attrIsAscendant(sEntId, sId)) {
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
            for (String sId : objAttrIds) {
                if (sId.startsWith("!")) {
                    sId = sId.substring(1);
                    System.out.println("........Processing container C("
                            + getEntityName(sId,PM_NODE.OATTR.value) + ")");
                    if (!attrIsAscendant(sEntId, sId)) {
                        System.out.println("..........Obj is not ascendant of container, return true");
                        return true;
                    } else {
                        System.out.println("..........Obj is ascendant of container");
                    }
                } else {
                    System.out.println("........Processing container "
                            + getEntityName(sId,PM_NODE.OATTR.value));
                    if (attrIsAscendant(sEntId, sId)) {
                        System.out.println("..........Obj is ascendant of container, return true");
                        return true;
                    } else {
                        System.out.println("..........Obj is not ascendant of container");
                    }
                }
            }
            return false;
        }
    }

    /**
     * Get host's repository path. This is the physical home of newly created objects.
     * @param sHostName the name of the host
     * @return a String containing the host repository
     * @throws Exception
     */
    public String getHostRepositoryInternal(String sHostName) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sHostName);
        ArrayList<String> strs = extractStrings(select(GET_HOST_REPO, params));
        String repo = (strs == null || strs.isEmpty()) ? null : strs.get(0);
        return repo;
    }

    public List<String> getMembers(String u, String i, String perm){
        List<String> pos = new ArrayList<String>();
        pos.add(i);
        Packet p = getMellMembersOf(u, i, perm);
        for(int j = 0; j < p.size(); j++){
            String[] mem = p.getStringValue(j).split(":");
            String type = mem[0];
            String id = mem[1];
            String name = mem[2];
            pos.addAll(getMembers(u, id, perm));
        }
        return pos;
    }

    public static void main(String[] args) {
        System.out.println("1/2/3".split("/")[1]);

        /*int num = 500;
        String cmd = "";

        for(int i = 0; i < num; i++){
            cmd = "add|p|PC_" + i + "|c|PM";
            System.out.println(cmd);
        }

        for(int i = 0; i < num; i++){
            cmd = "add|b|OA_" + i + "|p|PC_" + i;
            System.out.println(cmd);
            *//*cmd = "add|b|OA_" + (i + 1) + "|p|PC_" + i;
            System.out.println(cmd);*//*
        }

        for(int i = 0; i < num; i++){
            cmd = "add|a|UA_" + i + "|p|PC_" + i;
            System.out.println(cmd);
            *//*cmd = "add|a|UA_" + (i + 1) + "|p|PC_" + i;
            System.out.println(cmd);*//*
        }

        cmd = "add|u|U|fn|U|a|UA_0";
        System.out.println(cmd);
        for(int i = 1; i < num; i++){
            cmd = "asg|u|U|a|UA_" + i;
            System.out.println(cmd);
        }

        for(int i = 0; i < num; i++){
            cmd = "add|s|S_" + i + "|oc|Ignored|b|OA_" + i;
            System.out.println(cmd);
            cmd = "asg|a|UA_" + i + "|s|S_" + i;
            System.out.println(cmd);
            cmd = "add|op|File read|s|S_" + i;
            System.out.println(cmd);
        }*/
    }

    public String getHostAppPaths(String hostName) throws Exception {
        Packet result = new Packet();
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, hostName);
        ArrayList<ArrayList<Object>> appInfo = select(GET_HOST_APP_PATHS, params);
        String appPath = null;
        boolean first = true;
        StringBuffer sb = new StringBuffer();
        for(ArrayList<Object> app : appInfo){
            appPath = (String) app.get(1);
            if (first) {
                first = false;
            } else {
                sb.append(PM_ALT_FIELD_DELIM);
            }
            sb.append(appPath);
        }
        return sb.toString();
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

    public List<String> getAppPaths(String sClientId, String sSessId){
        try {
            List<String> result = new ArrayList<String>();
            ArrayList<ArrayList<Object>> hosts = select(GET_HOSTS);
            String hostName = null;
            String appPaths = null;
            StringBuffer sb = new StringBuffer();
            for (ArrayList<Object> host : hosts) {
                hostName = (String) host.get(1);
                appPaths = (String) getHostAppPaths(hostName);
                sb.append(appPaths);
                sb.append(PM_ALT_FIELD_DELIM);
                sb.append(hostName);
                result.add(sb.toString());
                System.out.println(sb.toString());
            }
            return result;
        }catch(Exception e){
            errorMessage = e.getMessage();
            return null;
        }
    }

    private Integer getHostId(String sName) throws Exception{
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sName);
        ArrayList<Integer> ints = extractIntegers(select(GET_HOST_ID, params));
        Integer hostId = (ints == null || ints.isEmpty()) ? null : ints.get(0);
        return hostId;
    }

    /**
     * get all of the keystore paths
     * each item is kStorePath:tStorePath:hostId:username
     * @param sClientId
     * @param sSessId
     * @return a packet containing the info of all the keystore paths
     */
    public ArrayList<String> getAllKStorePaths(String sClientId, String sSessId) {
        ArrayList<String> result = new ArrayList<String>();
        MySQL_Parameters params = null;
        try {
            ArrayList<ArrayList<Object>> results = select(GET_ALL_KSTORE_PATHS, params);
            for(ArrayList<Object> kStore : results){
                Integer hostId = (Integer) kStore.get(0);
                Integer userId = (Integer) kStore.get(1);
                String kSPath = (String) kStore.get(2);
                String tSPath = (String) kStore.get(3);

                result.add(kSPath + PM_ALT_DELIM_PATTERN +
                        tSPath + PM_ALT_DELIM_PATTERN +
                        hostId + PM_ALT_DELIM_PATTERN +
                        getEntityName(String.valueOf(userId), PM_NODE.USER.value));
            }
            return result;
        }catch(Exception e){
            e.printStackTrace();
            errorMessage = "Exception while looking for the keystore paths: " + e.getMessage();
            return null;
        }
    }

    // TODO: Move hardcoded value to config file. For SteveQ,
    // replace WIN-DNAR5079LMF with win08-SQ.
    //public FileWriter fw = new FileWriter("C:\\Users\\Administrator.win08-SQ\\Desktop\\output.txt");


    // Process:
    // add|comps|<components>|b|<record name>
    // <components> is a list of object names separated by ":".
    // <record name> is the name of a record (formerly a composite object).
    // All - the record and the components - must already exist.

    public Packet cmdAddComps(String sSessId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sComps = pieces[2];
        if (sComps == null || sComps.length() <= 0) {
            return SQLPacketHandler.getSuccessPacket();
        }
        try {
            String sRecId = getEntityId(pieces[4], PM_NODE.OATTR.value);
            if (sRecId == null) {
                return failurePacket("No such record " + pieces[4]);
            }
            // Call Stored Proc
            addCompsToOattrInternal(sRecId, sComps);
            System.out.println("Record Comps created Successfully.");
            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Error adding comps to record: " + e.getMessage());
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
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sTplName = pieces[2];
        String sType = pieces[3];
        try {
            if (sType.equals(PM_CONTAINERS)) {
                String containers = pieces[4];
                String[] splinters = containers
                        .split(PM_FIELD_DELIM);
                boolean first = true;
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < splinters.length; i++) {
                    if (first) {
                        first = false;
                        sb.append(getEntityId(splinters[i], PM_NODE.OATTR.value));
                    } else {
                        sb.append(PM_FIELD_DELIM
                                + getEntityId(splinters[i], PM_NODE.OATTR.value));
                    }
                }
                return addTemplate(sSessId, sTplName, sb.toString(), null);
            } else if (sType.equals(PM_NODE.OATTR.value)) {
                String sRecId = getEntityId(pieces[4], PM_NODE.OATTR.value);
                if (sRecId == null) {
                    return failurePacket("No such object attribute (record) "
                            + pieces[4]);
                }
                String sTplId = getEntityId(sTplName,
                        PM_TEMPLATE);
                if (sTplId == null) {
                    return failurePacket("No such template " + sTplName);
                }
                addTemplateToRecordInternal(sRecId, sTplId);
                return SQLPacketHandler.getSuccessPacket();
            } else {
                return failurePacket("Wrong type in cmdAddTpl(): " + sType);
            }
        } catch (Exception e) {
            return failurePacket("Exception in cmdAddTpl(): " + e.getMessage());
        }
    }

    // add|key|<key>|tpl|<tpl name>
    // add|key|<key>|b|<record name>

    public Packet cmdAddKey(String sSessId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sKey = pieces[2];
        String sType = pieces[3];
        try {
            if (sType.equals(PM_TEMPLATE)) {
                String sTplId = getEntityId(pieces[4], PM_TEMPLATE);
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
        }catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Note that adding a session or process deny via a script makes no sense.
    // Process add|deny|<deny name>|<deny type>|<user or attr>|<user or attr
    // name>|<is intersection>
    // where:
    // <deny type> ::=
    // PM_DENY_USER_ID|PM_DENY_INTRA_SESSION|PM_DENY_ACROSS_SESSIONS
    // <user or attr> ::= u|a
    // <is intersection> ::= yes|no

    public Packet cmdAddDeny(String sSessId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sDenyName = pieces[2];
        String sDenyType = pieces[3];
        String sNameType = pieces[4];
        String sName = pieces[5];
        String sIsInters = pieces[6];
        String sId;
        try {
            sId = getEntityId(sName, sNameType);
            if (sId == null) {
                return failurePacket("No entity " + sName + " of type " + sNameType);
            }
            Integer denyId = addDenyInternal(sDenyName, sDenyType, sName, null, null, null,
                    null, sIsInters.equalsIgnoreCase(YES));
            if(denyId == null){
                return fail();
            }
            Packet result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, sDenyName
                    + PM_FIELD_DELIM + denyId);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Process add|prop|<property>|{a|b|p|v}|<attribute or policy or value>

    public Packet cmdAddProp(String sSessId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sProp = pieces[2];
        String sType = pieces[3];
        String sName = pieces[4];
        try {
            if (sType.equals(PM_VALUE)) {
                return setProperty(null, sProp, sName);
            }
            String sId = getEntityId(sName, sType);
            if (sId == null) {
                return failurePacket("No entity " + sName + " of type " + sType);
            }
            if(addPropInternal(Integer.valueOf(sId), sType, sProp)){
                return Packet.getSuccessPacket();
            }else{
                return fail();
            }
        } catch (Exception e ) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Process add|ks|<key store path>|<trust store path>|h|<host>|u|<user>

    public Packet cmdAddKStores(String sSessId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sKsPath = pieces[2];
        String sTsPath = pieces[3];
        String sHost = pieces[5];
        String sUser = pieces[7];
        try {
            String sUserId = getEntityId(sUser,PM_NODE.USER.value);
            return setKStorePaths(sSessId, sUserId, sHost, sKsPath, sTsPath);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }


    public Packet cmdAddApplication(String sSessId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
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
    // <inh> is YES or NO.
    // <class> is File, Directory, User, User attribute, Object, Object
    // attribute,
    // Policy class, Connector, Operation set, or a custom class name.

    public Packet cmdAddObj(String sClientId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sObj = pieces[2];
        String sClass = pieces[3];
        String sInh = pieces[4];
        String sHost = pieces[5];
        String sPath = pieces[6];
        String sToType = pieces[7];
        String sToName = pieces[8];
        String sToId = null;
        String sOrigId = null;
        String sOrigName = null;

        try {
            sToId = getEntityId(sToName, sToType);
            if (sToId == null) {
                return failurePacket("No entity " + sToName + " of type "
                        + sToType);
            }
            if (sClass.equals(PM_CLASS_USER_NAME)
                    || sClass.equals(PM_CLASS_UATTR_NAME)
                    || sClass.equals(PM_CLASS_OBJ_NAME)
                    || sClass.equals(PM_CLASS_OATTR_NAME)
                    || sClass.equals(PM_CLASS_POL_NAME)
                    || sClass.equals(PM_CLASS_CONN_NAME)
                    || sClass.equals(PM_CLASS_OPSET_NAME)) {
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
            System.out.println(sInh);
            System.out.println(sHost);
            System.out.println(sPath);
            System.out.println(sToType);
            System.out.println(sToName);
            System.out.println(sToId);
            System.out.println(sOrigId);
            System.out.println(sOrigName);

            Integer objId = addObjectInternal(sObj, null, null, sObj, sObj, sToId,
                    sToType, sClass, null, sHost, sPath, sOrigName, sOrigId,
                    (sInh.equalsIgnoreCase(YES)), null, null, null, null,
                    null, null, null);
            if(objId == null){
                return fail();
            }
            Packet result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, sObj + PM_FIELD_DELIM + objId);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while adding object: "
                    + e.getMessage());
        }
    }

    // Process add|b|<object attribute>|c|<connector>
    // add|b|<object attribute>|p|<policy class>
    // add|b|<object attribute>|b|<object attribute>
    // add|b|<object attribute>|deny|<deny constraint>

    public Packet cmdAddOattr(String sClientId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String oattrType = pieces[1];
        String sOattr = pieces[2];
        String sToType = pieces[3];
        String sToName = pieces[4];
        try {
            String sToId = getEntityId(sToName, sToType);
            if (sToId == null) {
                return failurePacket("No entity " + sToName + " of type " + sToType);
            }

            // Note that in the case of adding the object attribute to a deny, the
            // object attribute must already exist!
            if (sToType.equalsIgnoreCase(PM_DENY)) {
                String sOattrId = getEntityId(sOattr,oattrType);
                if (sOattrId == null) {
                    return failurePacket("No object attribute " + sOattr);
                }
                Integer denyId = addDenyInternal(sToName, null, null, null, null, sOattr,
                        sOattrId, false);
                if(denyId == null){
                    return fail();
                }
                Packet result = new Packet();
                result.addItem(ItemType.RESPONSE_TEXT, sToName
                        + PM_FIELD_DELIM + denyId);
                return result;
            } else {
                Integer oattrId =  addOattrInternal(sOattr, null, sOattr, sOattr, sToId,
                        sToType, null, null);
                if(oattrId == null){
                    return fail();
                }
                Packet result = new Packet();
                try {
                    result.addItem(ItemType.RESPONSE_TEXT, sOattr + PM_FIELD_DELIM + oattrId);
                } catch (PacketException e) {
                    return failurePacket("failed creating reuslt packet");
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Process add|cb|<object attribute>|deny|<deny constraint>,
    // i.e., the complement of an object attribute.

    public Packet cmdAddComplOattr(String sClientId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sToType = pieces[3];
        String sToName = pieces[4];
        if (!sToType.equalsIgnoreCase(PM_DENY)) {
            return failurePacket("Entity " + sToName
                    + " is not a deny constraint!");
        }
        try {
            String sOattrId = getEntityId(pieces[2],PM_NODE.OATTR.value);
            if (sOattrId == null) {
                return failurePacket("No object attribute " + pieces[2]);
            }
            String sOattr = "!" + pieces[2];
            Integer denyId = addDenyInternal(sToName, null, null, null, null, sOattr,
                    sOattrId, false);
            if(denyId == null){
                return fail();
            }
            Packet result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, sToName
                    + PM_FIELD_DELIM + denyId);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Process add|eml|<coming from>|<email addr>|<pop server>|<smtp server>|
    // <acct name>|<password>|u|<user name>

    public Packet cmdAddEmail(String sClientId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sToType = pieces[8];// must be a user
        String sToName = pieces[9];
        try {
            String sToId = getEntityId(sToName, sToType);
            if (sToId == null) {
                return failurePacket("No entity " + sToName + " of type "
                        + sToType);
            }
            if(addEmailAcctInternal(sToName, pieces[2], pieces[3],
                    pieces[4], pieces[5], pieces[6], pieces[7])){
                return Packet.getSuccessPacket();
            }else{
                return fail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Process add|u|<user>|fn|<full name>|c|<connector>
    // add|u|<user>|fn|<full name>|a|<user attribute>

    public Packet cmdAddUser(String sClientId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sUser = pieces[2];
        String sFull = pieces[4];
        String sToType = pieces[5];
        String sToName = pieces[6];
        try {
            String sToId = getEntityId(sToName, sToType);
            if (sToId == null) {
                return failurePacket("No entity " + sToName + " of type " + sToType);
            }
            Integer userId = addUserInternal(sUser, null, sFull, sUser, sUser, sToId, sToType);
            if(userId == null){
                return fail();
            }
            Packet result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, sUser + PM_FIELD_DELIM + userId);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Process add|a|<user attribute>|c|<connector>
    // add|a|<user attribute>|p|<policy class>
    // add|a|<user attribute>|a|<user attribute>
    // add|a|<user attribute>|u|<user>


    public Packet cmdAddUattr(String sSessId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sUattr = pieces[2];
        String sToType = pieces[3];
        String sToName = pieces[4];
        try {
            String sToId = getEntityId(sToName, sToType);
            if (sToId == null) {
                return failurePacket("No entity " + sToName + " of type "
                        + sToType);
            }
            Integer uattrId = addUattrInternal(sUattr, sUattr, sUattr, sToId, sToType, null);
            if(uattrId == null){
                return fail();
            }
            Packet res = new Packet();
            res.addItem(ItemType.RESPONSE_TEXT, sUattr + PM_FIELD_DELIM + uattrId);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Process add|p|<policy class>|c|<connector>

    public Packet cmdAddPc(String sClientId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sPol = pieces[2];
        Packet res = new Packet();
        Integer pcId =  addPcInternal(sPol, null, sPol, sPol, null);
        if(pcId == null){
            return failurePacket(errorMessage);
        }
        try {
            res.addItem(ItemType.RESPONSE_TEXT, sPol + PM_FIELD_DELIM + pcId);
        } catch (PacketException e) {
            return failurePacket(e.getMessage());
        }
        return res;
    }

    // Process
    // add|s|<operation set>|oc|<object class>|a|<user attribute>|,
    // add|s|<operation set>|oc|<object class>|b|<object attribute>|, and
    // add|s|<operation set>|oc|<object class>|c|<connector>|.
    public Packet cmdAddOpset(String sClientId, String sCmd) {
        System.out.println("cmdAddOpset called");
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        System.out.println("pieces: " + Arrays.toString(pieces));
        String sOpset = pieces[2];
        String sClass = pieces[4];
        String sToType = pieces[5];
        String sToName = pieces[6];
        System.out.println("sOpset: " + sOpset);
        System.out.println("sClass: " + sClass);
        System.out.println("sToType: " + sToType);
        System.out.println("sToName: " + sToName);
        try {
            String sToId = getEntityId(sToName, sToType);
            if (sToId == null) {
                return failurePacket("No entity " + sToName + " of type "
                        + sToType);
            }
            if (sToType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                System.out.println("addOpsetaAndOpInternal called for uattr");
                /*return addOpsetAndOpInternal(sOpset, null, sOpset, sOpset,
                        pieces[8], sToId, sToType, null, null);*/
                Integer opsetId = addOpsetAndOpInternal(sOpset, null, sOpset, sOpset,
                        null, sToId, sToType, null, null);
                if(opsetId == null){
                    return fail();
                }
                Packet res = new Packet();
                res.addItem(ItemType.RESPONSE_TEXT, sOpset
                        + PM_FIELD_DELIM + opsetId);
                return res;
            } else if (sToType.equalsIgnoreCase(PM_NODE.OATTR.value)
                    || sToType.equalsIgnoreCase(PM_NODE.CONN.value)) {
                System.out.println("addOpsetaAndOpInternal called for oattr or conn");
                /*return addOpsetAndOpInternal(sOpset, null, sOpset, sOpset,
                        pieces[8], null, null, sToId, sToType);*/
                Integer opsetId = addOpsetAndOpInternal(sOpset, null, sOpset, sOpset,
                        null, null, null, sToId, sToType);
                if(opsetId == null){
                    return fail();
                }
                Packet res = new Packet();
                res.addItem(ItemType.RESPONSE_TEXT, sOpset
                        + PM_FIELD_DELIM + opsetId);
                return res;
            } else {
                return failurePacket("Incorrect base type in command " + sCmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Process add|oc|<object class>

    public Packet cmdAddObjClass(String sClientId, String sCmd) {
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
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
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sOp = pieces[2];
        String sToType = pieces[3];
        String sToName = pieces[4];
        if (sToType.equals(PM_OBJ_CLASS)) {
            return addObjClassAndOp(sClientId, sToName, sToName, sToName, sOp);
        } else if (sToType.equals(PM_NODE.OPSET.value)) {
            Integer opsetId = addOpsetAndOpInternal(sToName, null, null, null, sOp, null,
                    null, null, null);
            if(opsetId == null){
                return fail();
            }
            Packet res = new Packet();
            try {
                res.addItem(ItemType.RESPONSE_TEXT, sToName
                        + PM_FIELD_DELIM + opsetId);
            } catch (PacketException e) {
                return fail();
            }
            return res;
        } else if (sToType.equals(PM_DENY)) {
            try {
                Integer denyId = addDenyInternal(sToName, null, null, null, sOp, null, null, false);
                if(denyId == null){
                    return fail();
                }
                Packet result = new Packet();
                result.addItem(ItemType.RESPONSE_TEXT, sToName
                        + PM_FIELD_DELIM + denyId);
                return result;
            } catch(Exception e) {
                return SQLPacketHandler.getFailurePacket(e.getMessage());
            }
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
        String[] pieces = sCmd.split(PM_ALT_DELIM_PATTERN);
        String sType = pieces[1];
        String sName = pieces[2];
        String sToType = pieces[3];
        String sToName = pieces[4];
        try {
            String sId = getEntityId(sName, sType);
            if (sId == null) {
                return failurePacket("No entity " + sName + " of type " + sType);
            }
            String sToId = getEntityId(sToName, sToType);
            if (sToId == null) {
                return failurePacket("No entity " + sToName + " of type "
                        + sToType);
            }
            if(assignInternal(sId, sType, sToId, sToType)){
                return Packet.getSuccessPacket();
            }else{
                return failurePacket("could not complete assignment");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

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
        try{
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
                    //deleteObjectStrong(sSessId, sClipObjId);
                    deleteNode(Integer.valueOf(sClipObjId));
                    graphMgr.printGraph();
                } catch (Exception e) {
                    return failurePacket("Couldn't delete all existing clipboard objects!");
                }
            }

            // Build the clipboard object on the connector.
            Integer objId = addObjectInternal(sClipName, null, null,
                    "Clipboard object for host " + sHost, "None.", GlobalConstants.PM_CONNECTOR_ID,
                    PM_NODE.CONN.value, GlobalConstants.PM_CLASS_CLIPBOARD_NAME, null, sHost, null, null,
                    null, false, null, null, null, null, null, null, null);
            if (objId == null) {
                return fail();
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
                if(!deleteAssignment(Integer.valueOf(sClipId), Integer.valueOf(GlobalConstants.PM_CONNECTOR_ID))){
                    return fail();
                }

                // Now copy the assignments of sOattrId to the clipboard object.
                copyOattrAssignments(sOattrId, sClipId);
            }

            // Now label the clipboard with the session's level, if the clipboard
            // is in MLS.
            labelClipboard(sSessId, sClipId);
        }catch(Exception e){
            return failurePacket("error building cliboard. exception thrown");
        }

        return Packet.getSuccessPacket();
    }


    // Labeling the clipboard. In the case of an mls policy, the session
    // could have more than one active attributes in that policy. We must
    // select the "highest" attribute among the actives, and we define
    // "higher" as "being contained in": a > b iff a ->+ b.
    private Packet labelClipboard(String sSessId, String sClipId) {
        /*try {
            System.out.println("Labeling the Clipboard object.");

            // Find whether there is a MLS policy.
            String sMlsPolId = getEntityWithPropInternal(PM_NODE.POL.value, "type=mls");
            System.out.println("Found policy mls " + sMlsPolId);

            // Find the MLS user attributes that are active in the current session.
            HashSet<String> hs = getPolicyActiveAttributes(sSessId, sMlsPolId);
            // If no such attributes, there is nothing to do.
            if (hs.isEmpty()) {
                return Packet.getSuccessPacket();
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
                if (attrIsAscendant(sCrtId, sHighestId)) {
                    sHighestId = sCrtId;
                }
            }
            System.out.println("We found the highest active attribute to be "
                    + getEntityName(sHighestId, PM_NODE.UATTR.value));

            // Get the corresponding object attribute, if one exists.
            String sPrefix = "correspondsto=";
            String sProp = getPropertyWithPrefix(sPrefix, sHighestId, PM_NODE.UATTR.value);
            if (sProp == null) {
                return Packet.getSuccessPacket();
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
                    if(deleteAssignment(Integer.valueOf(sClipId), Integer.valueOf(sCrtId))){
                        return fail();
                    }
                }
            }

            // Add sClipId ---> sCorrOattrId.
            System.out.println("Trying to add clipboard assignment to "
                    + sCorrOattr);
            if(!addDoubleLink(sClipId, PM_NODE.OATTR.value,
                    sCorrOattrId, PM_NODE.OATTR.value)) {
                fail();
            }
        }catch(Exception e){
            return failurePacket();
        }*/
        return Packet.getSuccessPacket();
    }


    /**
     * @uml.property  name="savedRecords"
     * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
     */
    HashSet<String> savedRecords = null;


    public void exportProperties(Packet result) throws Exception {
        Packet props =  getProperties(null);
        if (props == null || props.size() <= 0) {
            return;
        }
        if (props.hasError()) {
            return;
        }

        for (int i = 0; i < props.size(); i++) {
            String sLine = props.getStringValue(i);
            String[] pieces = sLine.split(PM_PROP_DELIM);
            result.addItem(ItemType.RESPONSE_TEXT, "add"
                    + PM_ALT_FIELD_DELIM
                    + PM_PROP
                    + PM_ALT_FIELD_DELIM + pieces[0]
                    + PM_ALT_FIELD_DELIM
                    + PM_VALUE
                    + PM_ALT_FIELD_DELIM + pieces[1]);
        }
    }

    public void exportRecordProperties(Packet result) throws Exception {
        Iterator<String> setiter = savedRecords.iterator();
        while (setiter.hasNext()) {
            String sCompoId = setiter.next();
            System.out.println("Exporting record with id " + sCompoId);
            Packet info =  getRecordInfo(null, sCompoId);
            if (info.hasError()) {
                System.out
                        .println("Error in getRecordInfo called from exportRecordProperties:");
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
            String[] pieces = sLine.split(PM_FIELD_DELIM);
            String sRecName = pieces[0];

            // Get the object's template and generate the add template
            // command.
            sLine = info.getStringValue(1);
            pieces = sLine.split(PM_FIELD_DELIM);
            result.addItem(ItemType.RESPONSE_TEXT, "add"
                    + PM_ALT_FIELD_DELIM
                    + PM_TEMPLATE
                    + PM_ALT_FIELD_DELIM + pieces[0]
                    + PM_ALT_FIELD_DELIM + PM_NODE.OATTR.value
                    + PM_ALT_FIELD_DELIM + sRecName);

            // Get the object's components and generate the add components
            // command.
            sLine = info.getStringValue(2);
            int nComp = Integer.valueOf(sLine).intValue();
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (int i = 0; i < nComp; i++) {
                sLine = info.getStringValue(3 + i);
                pieces = sLine.split(PM_FIELD_DELIM);
                if (first) {
                    first = false;
                    sb.append(pieces[0]);
                } else {
                    sb.append(PM_FIELD_DELIM + pieces[0]);
                }
            }
            result.addItem(ItemType.RESPONSE_TEXT, "add"
                    + PM_ALT_FIELD_DELIM
                    + PM_COMPONENTS
                    + PM_ALT_FIELD_DELIM + sb.toString()
                    + PM_ALT_FIELD_DELIM +PM_NODE.OATTR.value
                    + PM_ALT_FIELD_DELIM + sRecName);

            // Get the object's keys and generate the add key commands.
            sLine = info.getStringValue(3 + nComp);
            int nKeys = Integer.valueOf(sLine).intValue();
            for (int i = 0; i < nKeys; i++) {
                sLine = info.getStringValue(4 + nComp + i);
                result.addItem(ItemType.RESPONSE_TEXT, "add"
                        + PM_ALT_FIELD_DELIM
                        + PM_KEY
                        + PM_ALT_FIELD_DELIM + sLine
                        + PM_ALT_FIELD_DELIM
                        + PM_NODE.OATTR.value
                        + PM_ALT_FIELD_DELIM + sRecName);
            }
        }
    }


    public void exportTemplates(Packet result) throws Exception {
        Packet templates =  getTemplates(null);
        if (templates == null || templates.size() <= 0) {
            return;
        }
        if (templates.hasError()) {
            return;
        }

        for (int i = 0; i < templates.size(); i++) {
            String sLine = templates.getStringValue(i);
            String[] pieces = sLine.split(PM_FIELD_DELIM);
            Packet tplInfo =  getTemplateInfo(null, pieces[1]);

            tplInfo.print(true, "Template info for " + pieces[0]);

            // item 0: <tpl name>:<tpl id>
            // item 1: <cont 1 id>:...:<cont n id>
            // item 2: <key1>:...:<keyn>
            String sContainers = tplInfo.getStringValue(1);
            String[] splinters = sContainers
                    .split(PM_FIELD_DELIM);
            boolean first = true;
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < splinters.length; j++) {
                String sContName = getEntityName(splinters[j],
                        PM_NODE.OATTR.value);
                if (first) {
                    first = false;
                    sb.append(sContName);
                } else {
                    sb.append(PM_FIELD_DELIM + sContName);
                }
            }
            result.addItem(ItemType.RESPONSE_TEXT, "add"
                    + PM_ALT_FIELD_DELIM
                    + PM_TEMPLATE
                    + PM_ALT_FIELD_DELIM + pieces[0]
                    + PM_ALT_FIELD_DELIM
                    + PM_CONTAINERS
                    + PM_ALT_FIELD_DELIM + sb.toString());
            if (tplInfo.size() >= 3) {
                String sKeys = tplInfo.getStringValue(2);
                splinters = sKeys.split(PM_FIELD_DELIM);
                for (int j = 0; j < splinters.length; j++) {
                    result.addItem(ItemType.RESPONSE_TEXT, "add"
                            + PM_ALT_FIELD_DELIM
                            + PM_KEY
                            + PM_ALT_FIELD_DELIM + splinters[j]
                            + PM_ALT_FIELD_DELIM
                            + PM_TEMPLATE
                            + PM_ALT_FIELD_DELIM + pieces[0]);
                }
            }
        }
    }


    public void exportOattr(Packet result, String sId, String sBaseType,
                            String sBaseName) throws Exception {
        String sName = getEntityName(sId, PM_NODE.OATTR.value);

        String sVobjId = getAssocObj(sId);
        if (sVobjId == null) {
            // This is an object attribute not associated with an object.
            result.addItem(ItemType.RESPONSE_TEXT, "add"
                    + PM_ALT_FIELD_DELIM + PM_NODE.OATTR.value
                    + PM_ALT_FIELD_DELIM + sName
                    + PM_ALT_FIELD_DELIM + sBaseType
                    + PM_ALT_FIELD_DELIM + sBaseName);
        } else {
            // This is an object attribute associated with an object.
            // Export the object.
            Packet ar = getObjInfo(sVobjId);
            String sLine = ar.getStringValue(0);

            // sLine may contain:
            // name|id|class|inh|host|path, or
            // name|id|class|inh|orig name|orig id, or
            // name|id|class|inh.
            String[] pieces = sLine.split(PM_ALT_DELIM_PATTERN);
            String sClass = pieces[2];

            // If the object's class is Clipboard, don't export it.
            if (sClass
                    .equalsIgnoreCase(PM_CLASS_CLIPBOARD_NAME)) {
                return;
            }

            StringBuffer sb = new StringBuffer();
            sb.append("add" + PM_ALT_FIELD_DELIM
                    + PM_OBJ
                    + PM_ALT_FIELD_DELIM + pieces[0]
                    + PM_ALT_FIELD_DELIM + pieces[2]
                    + PM_ALT_FIELD_DELIM + pieces[3]);
            if (pieces.length <= 4) {
                sb.append(PM_ALT_FIELD_DELIM
                        + PM_ALT_FIELD_DELIM);
            } else {
                sb.append(PM_ALT_FIELD_DELIM + pieces[4]
                        + PM_ALT_FIELD_DELIM + pieces[5]);
            }
            sb.append(PM_ALT_FIELD_DELIM + sBaseType
                    + PM_ALT_FIELD_DELIM + sBaseName);
            result.addItem(ItemType.RESPONSE_TEXT, sb.toString());
        }

        // If the object attribute is a record (container), save it to add
        // template,
        // components, and keys later.
        if (isRecord(sId)) {
            savedRecords.add(sId);
        }

        // Export its properties.
        HashSet<String> props = getProps(sId, PM_NODE.OATTR.value);
        Iterator<String> iter = props.iterator();
        while (iter.hasNext()) {
            result.addItem(ItemType.RESPONSE_TEXT, "add"
                    + PM_ALT_FIELD_DELIM
                    + PM_PROP
                    + PM_ALT_FIELD_DELIM + iter.next()
                    + PM_ALT_FIELD_DELIM + PM_NODE.OATTR.value
                    + PM_ALT_FIELD_DELIM + sName);
        }
    }


    public void exportPc(Packet result, String sId) throws Exception{
        String sName = getEntityName(sId,PM_NODE.POL.value);

        try {
            result.addItem(ItemType.RESPONSE_TEXT, "add" + PM_ALT_FIELD_DELIM
                    +PM_NODE.POL.value + PM_ALT_FIELD_DELIM + sName
                    + PM_ALT_FIELD_DELIM +PM_NODE.CONN.value + PM_ALT_FIELD_DELIM
                    + PM_CONNECTOR_NAME);

            // Export its properties.
            HashSet<String> props = getProps(sId,PM_NODE.POL.value);
            Iterator<String> iter = props.iterator();
            while (iter.hasNext()) {
                result.addItem(ItemType.RESPONSE_TEXT, "add"
                        + PM_ALT_FIELD_DELIM + PM_PROP + PM_ALT_FIELD_DELIM
                        + iter.next() + PM_ALT_FIELD_DELIM +PM_NODE.POL.value
                        + PM_ALT_FIELD_DELIM + sName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportUser(Packet result, String sId, String sBaseType,
                           String sBaseName) throws Exception{
        String sName = getEntityName(sId,PM_NODE.USER.value);
        String sFull = getUserFullName(sId);
        result.addItem(ItemType.RESPONSE_TEXT, "add"
                + PM_ALT_FIELD_DELIM + PM_NODE.USER.value
                + PM_ALT_FIELD_DELIM + sName
                + PM_ALT_FIELD_DELIM
                + PM_FULL_NAME
                + PM_ALT_FIELD_DELIM + sFull
                + PM_ALT_FIELD_DELIM + sBaseType
                + PM_ALT_FIELD_DELIM + sBaseName);

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

        result.addItem(ItemType.RESPONSE_TEXT, "add"
                + PM_ALT_FIELD_DELIM
                + PM_EMAIL_ACCT
                + PM_ALT_FIELD_DELIM + sComingFrom
                + PM_ALT_FIELD_DELIM + sEmailAddr
                + PM_ALT_FIELD_DELIM + sPopServer
                + PM_ALT_FIELD_DELIM + sSmtpServer
                + PM_ALT_FIELD_DELIM + sAcctName
                + PM_ALT_FIELD_DELIM + sPassword
                + PM_ALT_FIELD_DELIM + PM_NODE.USER.value
                + PM_ALT_FIELD_DELIM + sName);
    }


    public void exportUattr(Packet result, String sId, String sBaseType,
                            String sBaseName) throws Exception {
        String sName = getEntityName(sId,PM_NODE.UATTR.value);
        result.addItem(ItemType.RESPONSE_TEXT, "add"
                + PM_ALT_FIELD_DELIM + PM_NODE.UATTR.value
                + PM_ALT_FIELD_DELIM + sName
                + PM_ALT_FIELD_DELIM + sBaseType
                + PM_ALT_FIELD_DELIM + sBaseName);

        // Export its properties.
        HashSet<String> props = getProps(sId,PM_NODE.UATTR.value);
        Iterator<String> iter = props.iterator();
        while (iter.hasNext()) {
            result.addItem(ItemType.RESPONSE_TEXT, "add"
                    + PM_ALT_FIELD_DELIM
                    + PM_PROP
                    + PM_ALT_FIELD_DELIM + iter.next()
                    + PM_ALT_FIELD_DELIM + PM_NODE.UATTR.value
                    + PM_ALT_FIELD_DELIM + sName);
        }
    }

    public void printSetOfSets(HashSet<HashSet<String>> hs, String caption) throws Exception{
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


    public void printVector(Vector<String> v, String sType, String caption) throws Exception {
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


    // Returns the maximal (with respect to SAC) activable attribute subsets of
    // a set.
    // The argument is the id of a user attribute set.

    public HashSet<HashSet<String>> getMaximalSubsetsInternal(
            HashSet<String> hsAttrs) {
        // TODO Empty method, need to be revisited
        return null;
    }

    public String selectBest(HashMap<String, Set<String>> selectable,
                             HashSet<String> remaining, HashSet<String> sm) throws Exception {
        // Walk through selectable's entries whose keys ARE ALSO IN sm.
        // Select that key (i.e., ua) which contributes the maximum number of
        // perms.
        int maxContrib = 0;
        String sSelectedUa = null;
        System.out.println("*    *Begin selectBest");
        printSet(remaining, PM_PERM, "*    *with remaining set:");
        Iterator<String> mapiter = selectable.keySet().iterator();
        while (mapiter.hasNext()) {
            String sUa = mapiter.next();
            if (!sm.contains(sUa)) {
                continue;
            }
            HashSet<String> set = new HashSet<String>(selectable.get(sUa));
            printSet(set, PM_PERM, "*    *selectBest: attribute "
                    + getEntityName(sUa,PM_NODE.UATTR.value) + " has contrib:");
            if (set.contains(PM_ANY_ANY)) {
                set = new HashSet<String>(remaining);
            } else if (remaining.contains(PM_ANY_ANY)) {
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

    public void addCompsToOattrInternal(String sId, String comps) throws Exception {
        String[] pieces = comps.split(PM_FIELD_DELIM);
        int order = 1;
        for(String p : pieces){
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sId);
            params.addParam(ParamType.INT, getEntityId(p, PM_NODE.OATTR.value));
            params.addParam(ParamType.INT, order++);
            insert(ADD_COMP_TO_OATTR, params);
        }
    }

    public Packet addCompsToRecord(String sId, String comps){
        try {
            addCompsToOattrInternal(sId, comps);
            return getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    // Find the object id from an object id, or an oattr id, or an object name.
    String getObjId(String sObj) throws Exception {
        String sObjName = "";
        if (!isNum(sObj)) {
            // The argument is not an object id. Try as if it were an object name.
            return getEntityId(sObj, PM_NODE.OATTR.value);
        } else {
            sObjName = getEntityName(sObj, PM_NODE.OATTR.value);
            System.out.println("sObjName is " + sObjName);
            return sObj;
        }
    }



    // Get the current permissions on an object attribute. It is assumed that
    // the
    // object attribute is associated to a virtual object, but in reality it
    // could
    // be any oattr.
    // This function is called from getPermittedOps(). It works as
    // if all the user's attributes were active.

    public HashSet<String> getPermittedOpsInternal(String sSessId,
                                                   String sTgtOaId) throws Exception {
        //NamingEnumeration<?> objs;

        Vector<String> activeAttrs;
        Vector<Integer> policyClasses;
        Vector<Integer> opsets;
        Vector<Integer> oattrs;
        //HashSet<?> objects;

        String sPcId;
        String sAaId;
        String sOpsId;
        String sOaId;

        // Get the session's active attributes and the policy classes (as
        // Vectors).
        System.out.println("GPOInternal> sSessId: " + sSessId);
        String sUserId = getSessionUserIdInternal(sSessId);
        System.out.println("GPOInternal> User: " + sUserId);
        activeAttrs = getUserDescendantsInternalVector(sUserId);
        policyClasses = getPolicyClasses();

        // Create an empty HashMap.
        HashMap<String, HashSet<String>> hm = new HashMap<String, HashSet<String>>();
        // For each policy class pc
        for (int pc = 0; pc < policyClasses.size(); pc++) {
            sPcId = String.valueOf(policyClasses.elementAt(pc));
            System.out.println("GPOInternal> For pc = " + getEntityName(sPcId,PM_NODE.POL.value));
            // If (o is not in policy class pc) continue.
            if (!attrIsAscendantToPolicy(sTgtOaId, sPcId)) {
                System.out.println("GPOInternal>   Oattr "
                        + getEntityName(sTgtOaId,PM_NODE.OATTR.value)
                        + " is not in " + getEntityName(sPcId,PM_NODE.POL.value));
                continue;
            }
            System.out.println("GPOInternal>   Oattr "
                    + getEntityName(sTgtOaId,PM_NODE.OATTR.value) + " is in "
                    + getEntityName(sPcId,PM_NODE.POL.value));

            // The object is in this policy class. Note that at this time
            // hm has no entry for pc. Add an entry [pc, empty] to hm.
            System.out.println("GPOInternal>   Create entry hm["
                    + getEntityName(sPcId,PM_NODE.POL.value) + "]");
            addHmEntry(sPcId, null, hm);

            // For each active attribute
            for (int aa = 0; aa < activeAttrs.size(); aa++) {
                sAaId = activeAttrs.elementAt(aa);
                System.out.println("GPOInternal>   For each active attr "
                        + getEntityName(sAaId,PM_NODE.UATTR.value));
                // If !(aa ->+ pc) continue.
                if (!attrIsAscendantToPolicy(sAaId, sPcId)) {
                    System.out.println("GPOInternal>     "
                            + getEntityName(sAaId,PM_NODE.UATTR.value)
                            + " is not in pc = "
                            + getEntityName(sPcId,PM_NODE.POL.value));
                    continue;
                }
                System.out.println("GPOInternal>     " + getEntityName(sAaId,PM_NODE.UATTR.value)
                        + " is in pc = " + getEntityName(sPcId,PM_NODE.POL.value));

                // For each opset such that aa -> opset
                opsets = getToOpsets(sAaId);
                System.out.println("GPOInternal> PC: " + getEntityName(sPcId,PM_NODE.POL.value) + " has opsets: " + opsets);
                for (int ops = 0; ops < opsets.size(); ops++) {
                    sOpsId = String.valueOf(opsets.elementAt(ops));

                    // For each oa such that opset -> oa
                    oattrs = getToAttrsOpset(sOpsId);
                    System.out.println("GPOInternal> Opset: " + opsets.get(ops) + " has oattrs: " + oattrs);
                    for (int oa = 0; oa < oattrs.size(); oa++) {
                        sOaId = String.valueOf(oattrs.elementAt(oa));
                        boolean reps = false;
                        if(oattrRepresentsAnEntity(sOaId)){
                            sOaId = getOriginalObjId(Integer.valueOf(sOaId)).toString();
                            reps = true;
                        }
                        // If !(oa ->+ pc) continue.
                        if (!attrIsAscendantToPolicy(sOaId, sPcId)) {
                            //if(!isAscendant(sPcId,sOaId)){
                            System.out.println("GPOInternal> oattr " + oattrs.get(oa) + " is not an ascendant to PC " + sPcId);
                            continue;
                        }
                        // If !(o ->* oa) continue
                        //if (!attrIsAscendant(sTgtOaId, sOaId)) {
                        //if(!getToAttrs(sTgtOaId, PM_NODE.OATTR.value, NO_DEPTH).contains(oattrs.get(oa))){
                        if(!isAscendant(sOaId, sTgtOaId)){
                            if(getNodeTypeValue(Integer.valueOf(sTgtOaId)).equals(PM_NODE.ASSOC.value)){
                                List<Integer> toAttrs = getToAttrs(sTgtOaId, PM_NODE.OATTR.value, NO_DEPTH);
                                if(!toAttrs.contains(Integer.valueOf(sOaId))){
                                    //System.out.println("GPOInternal> oattr " + getEntityName(sTgtOaId,PM_NODE.OATTR.value)
                                    //+ " is not an ascendant to OA " + getEntityName(oattrs.get(oa).toString(), PM_NODE.OATTR.value));
                                    continue;
                                }
                            }
                        }
                        // Add [pc, ops] to the hashmap.
                        System.out.println("GPOInternal>     Added "
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
                if (crtSet.contains(PM_ANY_ANY)) {
                } else if (resultSet.contains(PM_ANY_ANY)) {
                    resultSet = crtSet;
                } else {
                    resultSet.retainAll(crtSet);
                }
            }
        }
        System.out.println("GPOInternal> resultset: " + resultSet.toString());
        return resultSet;
    }

    // A null sOpsId means an empty set of operations.

    public void addHmEntry(String sPcId, String sOpsId,
                           HashMap<String, HashSet<String>> hm) throws Exception {
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
    public Packet getPermittedOpsOnEntity(String sSessId, String sTargetId,
                                          String sTargetType) {
        try {
            return SQLPacketHandler.setToPacket(getPermittedOpsOnEntityInternal(
                    sSessId, null, sTargetId, sTargetType));
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }
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

    public HashSet<String> getPermittedOpsOnEntityInternal(String sCrtSessId,
                                                           String sProcId, String sTgtId, String sTgtType) throws Exception {System.out.println("sTgtId:\t" + sTgtId);
        System.out.println("sTgtType:\t" + sTgtType);
        System.out.println("gPOOEI sessid: " + sCrtSessId);
        HashSet<String> prSet = new HashSet<String>();
        String sObjId;
        HashSet<String> computedOps = new HashSet<String>();
        System.out.println("Calling isSuper");

        if(isSuper(sCrtSessId)){
            System.out.println("is super");
            computedOps.add(PM_ANY_ANY);
            return computedOps;
        }

        if (sTgtType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            sTgtType =PM_NODE.OATTR.value;
        }

        System.out.println("***********getPermittedOpsOnEntityInternal "
                + getEntityName(sTgtId, sTgtType));


        // An object x represents the target t if:
        // - x.pmOriginalId = t.id and x.inh = no; or:
        // - x.pmOriginalId != null and x.inh = yes and t is in x.ascendants.

        //get all objects that represent target
        System.out.println("executing select GET_OBJ_IDS_OF_ORIG_ID");

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sTgtId);
        ArrayList<Integer> objIds = extractIntegers(select(GET_OBJ_IDS_OF_ORIG_ID, params));
        for(Integer id : objIds){
            params.clearParams();
            params.addParam(ParamType.INT, id);
            ArrayList<Object> objInfo = select(GET_OBJECT_INFO, params).get(0);
            sObjId = String.valueOf(objInfo.get(0));
            String sSomeEntityId = String.valueOf(objInfo.get(1));
            String sInh = String.valueOf(objInfo.get(2)).equalsIgnoreCase("1")?"true":"false";
            String sSomeEntityClass = String.valueOf(objInfo.get(3));

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
        //TODO JOSH ADDED
        ArrayList<Integer> conts = getToAttrs(sTgtId, null, NO_DEPTH);
        for (Integer cont : conts) {
            String contId = cont.toString();
            String repId = getOattrRepresentingEntity(contId, getNodeTypeValue(Integer.valueOf(contId)), true);
            if (repId != null) prSet.add(repId);
        }

        printSet(prSet, PM_OBJ,
                "Set of virtual objects representing the entity "
                        + getEntityName(sTgtId, sTgtType));

        // If no virtual objects are mapped to the target entity,
        // return empty list of permissions.
        if (prSet.isEmpty()) {
            return new HashSet<String>();
        }

        // The computed operations permitted on the given entity.
        System.out.println("computedOps" + computedOps);
        // Find the denied operations on the target entity.
        HashSet<String> deniedOps;
        try {
            deniedOps = getDeniedPerms(sCrtSessId, null, sProcId, sTgtId, sTgtType);
        } catch (Exception e) {
            e.printStackTrace();
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
                    + getEntityName(sObjId, PM_OBJ));

            // Get the oattr associated with the representative object.
            String sAssocOaId = getOriginalObjId(Integer.valueOf(sObjId)).toString();//getAssocOattr(sObjId);
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
        //computedOps.add(PM_ANY_ANY);
        if(isSuper(sCrtSessId)){
            computedOps.clear();
            computedOps.add(PM_ANY_ANY);
            return computedOps;
        }else{
            return computedOps;
        }
    }

    private boolean isSuper(String sessId) throws Exception{
        if(sessId == null || sessId.equals(""))return false;

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sessId);
        return ((Long) select(IS_SUPER, params).get(0).get(0) == 1);
    }

// Request permissions on a PM entity. The entity is given by its id and
    // type.
    // Calls requestPermsOnPmEntityInternal() on the PM entity, which is
    // supposed
    // to be called from within the engine, to test whether the subject
    // is allowed to perform administrative operations on that entity.
    //
    // No longer used. PM works as if all a user's attributes were
    // active. Instead, PM calls getPermittedOpsOnEntity or
    // getPermittedOpsOnEntityInternal.
    //

    // Translates PM entity classes to types, e.g., User --> u, User attribute
    // --> a.

    public String classToType(String sClass) {
        if (sClass.equalsIgnoreCase(PM_CLASS_USER_NAME)) {
            return PM_NODE.USER.value;
        } else if (sClass.equalsIgnoreCase(PM_CLASS_UATTR_NAME)) {
            return PM_NODE.UATTR.value;
        } else if (sClass.equalsIgnoreCase(PM_CLASS_OBJ_NAME)) {
            return PM_OBJ;
        } else if (sClass.equalsIgnoreCase(PM_CLASS_OATTR_NAME)) {
            return PM_NODE.OATTR.value;
        } else if (sClass.equalsIgnoreCase(PM_CLASS_POL_NAME)) {
            return PM_NODE.POL.value;
        } else if (sClass.equalsIgnoreCase(PM_CLASS_OPSET_NAME)) {
            return PM_NODE.OPSET.value;
        } else if (sClass.equalsIgnoreCase(PM_CLASS_CONN_NAME)) {
            return PM_NODE.CONN.value;
        } else if (sClass.equalsIgnoreCase(PM_CLASS_FILE_NAME)) {
            return PM_OBJ;
        } else if (sClass.equalsIgnoreCase(PM_CLASS_DIR_NAME)) {
            return PM_OBJ;
        } else {
            return null;
        }
    }

    // Translates types to PM entity classes, e.g., u --> User.

    public String typeToClass(String sType) {
        if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
            return PM_CLASS_USER_NAME;
        } else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
            return PM_CLASS_UATTR_NAME;
        } else if (sType.equalsIgnoreCase(PM_OBJ)) {
            return PM_CLASS_OBJ_NAME;
        } else if (sType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
            return PM_CLASS_OATTR_NAME;
        } else if (sType.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            return PM_CLASS_OATTR_NAME;
        } else if (sType.equalsIgnoreCase(PM_NODE.POL.value)) {
            return PM_CLASS_POL_NAME;
        } else if (sType.equalsIgnoreCase(PM_NODE.OPSET.value)) {
            return PM_CLASS_OPSET_NAME;
        } else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
            return PM_CLASS_CONN_NAME;
        } else {
            return null;
        }
    }


    // The types are abbrevs used in the engine (u, a, ob, b, p, s, c).

    public boolean isContained(String sId1, String sType1, String sId2,
                               String sType2) throws Exception {
        System.out.println(sId1 + ":" + sType1 + "->" + sId2 + ":" + sType2);
        if (sType1.equalsIgnoreCase(PM_NODE.USER.value)) {
            return (sType2.equalsIgnoreCase(PM_NODE.UATTR.value) && userIsAscendant(
                    Integer.valueOf(sId1), Integer.valueOf(sId2)))
                    || (sType2.equalsIgnoreCase(PM_NODE.POL.value) && userIsAscendantToPolicy(
                    sId1, sId2))
                    || (sType2.equalsIgnoreCase(PM_NODE.CONN.value));
        }

        if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)) {
            return (sType2.equalsIgnoreCase(PM_NODE.UATTR.value) && attrIsAscendant(
                    sId1, sId2))
                    || (sType2.equalsIgnoreCase(PM_NODE.POL.value) && attrIsAscendantToPolicy(
                    sId1, sId2))
                    || (sType2.equalsIgnoreCase(PM_NODE.CONN.value));
        }

        if (sType1.equalsIgnoreCase(PM_NODE.OATTR.value)
                || sType1.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            return (sType2.equalsIgnoreCase(PM_NODE.OATTR.value) && attrIsAscendant(
                    sId1, sId2))
                    || (sType2.equalsIgnoreCase(PM_NODE.POL.value) && attrIsAscendantToPolicy(
                    sId1, sId2))
                    || (sType2.equalsIgnoreCase(PM_NODE.CONN.value));
        }

        if (sType1.equalsIgnoreCase(PM_OBJ)) {
            // We have a virtual object. To assess containment, look at its
            // associated object attribute.
            String sAssoc1 = getAssocOattr(sId1);
            if (sAssoc1 == null) {
                return false;
            }
            return (sType2.equalsIgnoreCase(PM_NODE.OATTR.value) && attrIsAscendant(
                    sAssoc1, sId2))
                    || (sType2.equalsIgnoreCase(PM_NODE.POL.value) && attrIsAscendantToPolicy(
                    sAssoc1,sId2))
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

    /**
     * checks if the given opset is an ascendant of a given attribute
     * opset is assigned to given attribute with depth >= 1
     * @param sId1 the ID of the opset
     * @param sId2 the ID of the attribute
     * @param sType2 the type of the other attribute
     * @return true if it is an ascendant or false otherwise
     * @throws Exception
     */
    public boolean opsetIsAscendant(String sId1, String sId2, String sType2) throws Exception {
        if (sType2.equalsIgnoreCase(PM_NODE.CONN.value)) {
            return true;
        }

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId2);
        params.addParam(ParamType.INT, sId1);
        ArrayList<ArrayList<Object>> returned = null;
        returned = select(IS_ASSIGNED, params);
        return (Long) returned.get(0).get(0) != 0;
    }




    // Test whether (op, o) is in the caps for an active attribute in a policy
    // class.
    // Note that instead of the object o we use the associated object attribute.

    public boolean attrHasCap(String sOp, String sAssocOaId, String sPcId,
                              String sAaId) throws Exception  {
        Vector<Integer> opsets;
        Vector<Integer> oattrs;
        String sOpsId;
        String sOaId;

        // For each opset such that aa -> opset
        opsets = getToOpsets(sAaId);
        for (int ops = 0; ops < opsets.size(); ops++) {
            sOpsId = String.valueOf(opsets.elementAt(ops));

            // If the operation is not in the opset, continue with next opset.
            if (!opsetContainsOp(Integer.valueOf(sOpsId), sOp)) {
                continue;
            }
            // For each oa such that opset->oa
            oattrs = getToAttrsOpset(sOpsId);
            for (int oa = 0; oa < oattrs.size(); oa++) {
                sOaId = String.valueOf(oattrs.elementAt(oa));
                // If (o ->* oa && oa ->+ pc) return true
                if (attrIsAscendant(sAssocOaId, sOaId)
                        && attrIsAscendantToPolicy(sOaId, sPcId)) {
                    return true;
                }
            }
        }
        return false;
    }



    public void printHtWithPcKeys(Map<String, Map<String, Set<String>>> ht) throws Exception {
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
                           Map<String, Map<String, Set<String>>> ht) throws Exception {
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

    /**
     * Add the operations of an opset to another set of operations
     * A null sOpsetId means an empty set of operations.
     * @param sOpsetId the operation set to retrieve operations
     * @param set the destination for the operatins
     * @throws Exception
     */
    public void addOpsetToSet(String sOpsetId, Set<String> set)
            throws Exception {
        if(sOpsetId == null){
            return;
        }
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sOpsetId);
        ArrayList<String> ops = extractStrings(select(GET_OPSET_OPS, params));
        for(int i = 0; i < ops.size(); i++){
            set.add(ops.get(i));
        }
    }

    /**
     * Get all user sessions
     * @param sUserId user ID to get sessions of
     * @return
     * @throws Exception
     */
    public HashSet<String> getUserSessions(String sUserId) throws Exception {
        HashSet<String> sessSet = new HashSet<String>();
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sUserId);
        ArrayList<ArrayList<Object>> sessions = select(GET_USER_SESSIONS,
                params);
        for(ArrayList<Object> s : sessions){
            sessSet.add((String)s.get(0));
        }
        return sessSet;
    }



    public HashSet<String> getPolicyAttributesAssigned(String sPolId,
                                                       String sOattrId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        HashSet<String> hs = new HashSet<String>();
        String attribute = null;
        params.addParam(ParamType.INT, Integer.valueOf(sOattrId));
        ArrayList<ArrayList<Object>> attrs =  select(GET_ASSIGNMENTS, params);
        for(ArrayList<Object> attr : attrs){
            attribute = (String)attr.get(0);
        }
        if (attrIsAscendantToPolicy(attribute, sPolId)) {
            hs.add(attribute);
        }
        return hs;
    }

    /**
     * retrieve the host name associated with the session id
     * @param sSessId the ID of the session
     * @return the name of the host
     * @throws Exception
     */
    public String getSessionHostName(String sSessId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sSessId);
        ArrayList<ArrayList<Object>> result = select(GET_SESSION_HOST_NAME, params);
        ArrayList<String> strings = extractStrings(result==null?null:result);
        String sHost = strings==null?null:strings.get(0);
        return sHost;
    }

    /**
     * retrieve the host id associated with the session id
     * @param sSessId the ID of the session
     * @return the name of the host
     * @throws Exception
     */
    public String getSessionHostId(String sSessId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sSessId);
        ArrayList<ArrayList<Object>> result = select(GET_SESSION_HOST_ID, params);
        ArrayList<Integer> strings = result==null?null:extractIntegers(result);
        String sHost = strings==null||strings.isEmpty()?null:String.valueOf(strings.get(0));
        return sHost;
    }



    /**
     * Get the user ID associated with the session ID
     * @param sSessId session ID
     * @return the ID of the user ID
     */
    public String getSessionUserIdInternal(String sSessId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sSessId);
        System.out.println(sSessId);
        ArrayList<Integer> ints = extractIntegers(select(GET_SESS_USER_ID, params));
        String userId = (ints == null || ints.isEmpty()) ? null : String.valueOf(ints.get(0));
        return userId;
    }

    public Packet getSessionUserId(String sSessId){
        try {
            String userId = getSessionUserIdInternal(sSessId);
            Packet p = new Packet();
            p.addItem(ItemType.RESPONSE_TEXT, userId);
            return p;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }


    // Returns the set of already open objects for the given session.
    // Note that they are returned as object names!

    public HashSet<String> getSessionOpenObjs(String sSessId) throws Exception {
        HashSet<String> hs = new HashSet<String>();

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sSessId);

        ArrayList<Integer> objIds = extractIntegers(select(
                GET_SESSION_OPEN_OBJECTS, params));
        if (objIds == null) {
            return hs;
        }
        for (Integer id : objIds) {
            String sOO = id.toString();
            String[] pieces = sOO.split(PM_ALT_DELIM_PATTERN);
            hs.add(pieces[0]);
        }
        return hs;
    }


    public String getUserPass(String sId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sId);
        ArrayList<String> strs = extractStrings(select(GET_USER_PASS, params));
        return (strs == null || strs.isEmpty()) ? null : strs.get(0);
    }

    // Delete an object attribute. It can be deleted only if it's not associated
    // to an object, and does not have ascendants.

    public boolean deleteOattr(String sSessId, String sIdToDelete,
                               boolean verifyOpsetsAndAssoc) {
        try {
            if (!entityExists(sIdToDelete, PM_NODE.OATTR.value)) {
                errorMessage = "No object attribute with id " + sIdToDelete;
                return false;
            }
            if (attrHasAscendants(sIdToDelete,PM_NODE.OATTR.value)) {
                errorMessage = "Object attribute has ascendants";
                return false;
            }
            if (verifyOpsetsAndAssoc && hasAssocObj(sIdToDelete)) {
                errorMessage = "Object attribute is associated to an object";
                return false;
            }
            if (verifyOpsetsAndAssoc && attrHasOpsets(sIdToDelete,PM_NODE.OATTR.value)) {
                errorMessage = "An operation set is assigned to this object attribute";
                return false;
            }
        } catch (Exception e) {
            errorMessage = "Exception in deleting Object Attribute " + e.getMessage();
            return false;
        }
        // Delete the assignment this node ---> descendant attributes.
        Packet res;
        ArrayList<Integer> attr;
        String sId;

        try {
            attr = getToAttrs(sIdToDelete,PM_NODE.OATTR.value,DEPTH);
            if (attr != null) {
                for (Integer i : attr) {
                    sId = String.valueOf(i);
                    if (sId==null || sId.isEmpty()) continue;
                    // Now delete the assignment sIdToDelete (oattr) ---> sId
                    // (oattr).
                    if(!deleteAssignment(Integer.valueOf(sIdToDelete), Integer.valueOf(sId))){
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return false;
        }

        // Get the policy classes this object attribute is assigned to and
        // delete the assignment.
        try {
            attr = getToPolicies(sIdToDelete,PM_NODE.OATTR.value);
            if (attr != null) {
                for (Integer i : attr) {
                    sId = String.valueOf(i);

                    // Now delete the assignment sIdToDelete (oattr) ---> sId
                    // (policy class).
                    if(!deleteAssignment(Integer.valueOf(sIdToDelete), Integer.valueOf(sId))){
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return false;
        }
        try {
            // If this attribute is assigned to the connector node, delete the
            // assignment.
            if (attrIsAssignedToConnector(sIdToDelete)) {
                // Now delete the assignment sIdToDelete (oattr) ---> Connector
                // node.
                if(!deleteAssignment(Integer.valueOf(sIdToDelete), Integer.valueOf(PM_CONNECTOR_ID))){
                    return false;
                }
            }

            deleteNode(Integer.valueOf(sIdToDelete));

        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "Could not delete object attribute "
                    + sIdToDelete;
            return false;
        }

        return true;
    }

    public boolean attrHasOpsets(String sId, String sType) throws Exception {
        return uattrHasOpsets(Integer.valueOf(sId)) || oattrHasOpsets(Integer.valueOf(sId));
    }

    /**
     * Called for the "deleteNode" command, triggered when the user right-clicks
     * on the opset and selects the "delete node" popup menu.
     *
     * @param sClientId
     * @param sIdToDelete the ID of the opset to delete
     * @return the result of calling deleteOpsetAndOP()
     */
    public Packet deleteOpset(String sClientId, String sIdToDelete) {
        return deleteOpsetAndOp(sClientId, sIdToDelete, null);
    }

    /**
     * Delete a PM user. Just delete all its assignments
     * to user attributes, delete its assignment to the connector node
     * if it's assigned, and delete the user object.
     *
     * @param sSessId the current session ID
     * @param sIdToDelete the ID of the user to delete
     * @return a success or failure packet
     */
    public Packet deleteUser(String sSessId, String sIdToDelete) {
        Packet res = new Packet();
        try {
            this.deleteNode(null, sIdToDelete, PM_NODE.USER.value, NO);
            res = SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            res = failurePacket("Could not delete user " + sIdToDelete);
        }
        return res;
    }

    /**
     * Delete a user attribute. We can delete it only if it's isolated, i.e., no
     * node is
     * assigned to it (it has no ascendants).
     *
     * @param sClientId
     * @param sIdToDelete the ID of the uattr to delete
     * @return Success packet or failure packet
     */
    public Packet deleteUattr(String sClientId, String sIdToDelete) {
        Packet res = new Packet();
        try {
            getEntityName(sIdToDelete, PM_NODE.UATTR.value);
            if (!entityExists(sIdToDelete, PM_NODE.UATTR.value)) {
                return failurePacket("No user attribute with id " + sIdToDelete);
            }
            if (attrHasAscendants(sIdToDelete, PM_NODE.UATTR.value)) {
                return failurePacket("User attribute has ascendants");
            }
            if (uattrHasOpsets(Integer.valueOf(sIdToDelete))) {
                return failurePacket("User attribute is assigned to operation sets");
            }

            // Destroy the user attribute object.
            deleteNode(null, sIdToDelete, PM_NODE.UATTR.value, NO);
            res = SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            res = failurePacket("Could not delete user attribute "
                    + sIdToDelete);
        }

        return res;
    }

    /**
     * delete assignment between two nodes
     * @param end ID of the first node
     * param sType1 type of the first node
     * @param start ID of the second node
     * param sType2 type of the second node
     * @return a Packet: either success or failure
     */
    public boolean deleteAssignment(Integer end, Integer start) {
        try{
            executeStoredProcedure(DELETE_ASSIGNMENT, start, end);
            deleteAssignmentPmGraphNode(start.toString(), end.toString());
            return true;
        }catch(Exception e){
            e.printStackTrace();
            errorMessage = "Could not delete assignment between end:" + end + " and start:" + start + ". " + e.getMessage();
            return false;
        }
    }

    public boolean assignInternal(String end, String sType1, String start,
                                  String sType2){
        //START     END
        //ua    ->  u
        //ua    ->  ua
        //pc    ->  ua
        //oa    ->  oa
        //pc    ->  oa

        //associations
        //s     ->  ua
        //oa    ->  s

        try{
            String startType = getNodeType(Integer.valueOf(end));
            String endType = getNodeType(Integer.valueOf(start));
            if(startType.equals(PM_NODE.OPSET.value) || endType.equals(PM_NODE.OPSET.value)){
                //need to add to association because there will
                //always already be one since an opset needs to be created before assigned

                //if s -> ua

            }
            assign(Integer.valueOf(start), Integer.valueOf(end));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // Add a node representing the members of a container. sNodeType and sNodeId
    // are the type (user attribute, object attribute, policy, or connector)
    // and id of the container.

    public void addMembersOfNode(Packet graph, String sNodeType, String sNodeId)
            throws Exception {
        // Build the new node.
        String sLabel = "Members of " + getEntityName(sNodeId, sNodeType);
        RandomGUID myGUID = new RandomGUID();
        String sId = myGUID.toStringNoDashes();
        String sType =PM_NODE.M_PREFIX.value + sNodeType;
        StringBuffer sb = new StringBuffer();
        sb.append(sType);
        sb.append(PM_FIELD_DELIM);
        sb.append(sId);
        sb.append(PM_FIELD_DELIM);
        sb.append(sLabel);
        graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());

        // Assign it to the container.
        sb = new StringBuffer();
        sb.append(PM_ARC);
        sb.append(PM_FIELD_DELIM);
        sb.append(sId);
        sb.append(PM_FIELD_DELIM);
        sb.append(sNodeId);
        graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());
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
        sb.append(PM_FIELD_DELIM);
        sb.append(sId);
        sb.append(PM_FIELD_DELIM);
        sb.append(sLabel);
        graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());
    }

    public void addRel(Packet graph, String sId1, String sId2)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(PM_ARC);
        sb.append(PM_FIELD_DELIM);
        sb.append(sId1);
        sb.append(PM_FIELD_DELIM);
        sb.append(sId2);
        graph.addItem(ItemType.RESPONSE_TEXT, sb.toString());
    }

    /**
     * Retuns the policy classes as a vector. Does not take any argument.
     * get all policy classes. <id>
     * @return a vector of all policy classes
     * @throws Exception
     */
    public Vector<Integer> getPolicyClasses() throws Exception {
        try{
            //sql statement should return name, id
            ArrayList<ArrayList<Object>> pcs =  select(GET_POLICY_CLASSES);
            Vector<Integer> retPcs = new Vector<Integer>();
            for(ArrayList<Object> pc : pcs){
                retPcs.add((Integer) pc.get(1));
            }
            return retPcs;
        }catch(SQLException e){
            return null;
        }
    }

    // Add a policy class with the given name, description, info, and
    // properties.
    // First test permissions, then call the internal function.

    public Packet fail(){
        return failurePacket(errorMessage);
    }

    public Packet deletePolicyClass(String sClientId, String sIdToDelete) {

		/*res =  deleteDoubleLink(sIdToDelete,PM_NODE.POL.value,
				PM_CONNECTOR_ID,PM_NODE.CONN.value);
		if (res.hasError()) {
			return res;
		}*/

        // Destroy the policy class object.
        try {
            if (!entityExists(sIdToDelete, PM_NODE.POL.value)) {
                return failurePacket("No such policy id " + sIdToDelete);
            }
            if (!policyHasNoAscendant(sIdToDelete)) {
                return failurePacket("Policy class has ascendants");
            }
            this.deleteNode(null, sIdToDelete, PM_NODE.POL.value, NO);
            //deleteConnToPcLink(sIdToDelete);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Could not delete policy class " + sIdToDelete);
        }


        return SQLPacketHandler.getSuccessPacket();
    }

    /**
     * determine if a policy has ascendants or not
     * @param sId the ID of the policy class
     * @return true if policy has ascendants, false otherwise
     * @throws Exception
     */
    public boolean policyHasNoAscendant(String sId) throws Exception {
        return !hasAscendants(sId);

    }

    /**
     * Returns the id of the entity of the specified type with the specified
     * property, or null if no such entity exists.
     *
     * @param sType the type of the entity to search for
     * @param sProp the prop used to find the entity
     * @return a String containing the ID of the entity with the given property
     */

    public String getEntityWithPropInternal(String sType, String sProp) throws Exception {
        if (sProp==null || sProp.isEmpty()) {
            return null;
        }
        String[] pieces = sProp.split(PM_PROP_DELIM);
        MySQL_Parameters params = new MySQL_Parameters();
        if (pieces.length < 2) {
            return null;
        }
        String prop = pieces[0];
        String value = pieces[1];
        if (prop == null || prop.isEmpty() || value==null || value.isEmpty()) {
            return null;
        }
        params.addParam(ParamType.STRING, prop);
        params.addParam(ParamType.STRING, value);
        ArrayList<Integer> ints = extractIntegers(select(GET_ENTITY_WITH_PROP, params));
        String id = (ints == null || ints.isEmpty()) ? null : String.valueOf(ints.get(0));
        return id==null ? null : String.valueOf(id);
    }

    public boolean isEvent(String sName) {
        for (int i = 0; i < sEventNames.length; i++) {
            if (sEventNames[i].equalsIgnoreCase(sName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @uml.property  name="reqPermsMsg"
     */
    String reqPermsMsg;

    public boolean requestAddPcPerms(String sSessId, String sProcId) throws Exception {
        HashSet<String> resPerms;
        String sReqPerms;
        if (!sessionExists(sSessId)) {
            reqPermsMsg = "You're not in a Policy Machine session!";
            return false;
        }

        sReqPerms = PM_CONN_CREATE_POL;
        resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId,
                PM_CONNECTOR_ID,PM_NODE.CONN.value);
        printSet(resPerms, PM_PERM, "Permissions on connector");
        if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
            reqPermsMsg = "You're not authorized to create a policy class (assigned to connector)!";
            return false;
        }
        return true;
    }

    public boolean requestAssignPerms(String sSessId, String sProcId, String sId1,
                                      String sType1, String sId2, String sType2) throws Exception {
        HashSet<String> resPerms;
        String sReqPerms1;
        String sReqPerms2;

        if (!sessionExists(sSessId)) {
            reqPermsMsg = "You're not in a Policy Machine session!";
            return false;
        }

        if (sType1.equalsIgnoreCase(PM_NODE.OATTR.value)
                || sType1.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            sReqPerms1 = PM_OATTR_ASSIGN;
            if (sType2.equalsIgnoreCase(PM_NODE.OATTR.value)) {
                sReqPerms2 = PM_OATTR_ASSIGN_TO;
            } else if (sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
                sReqPerms2 = PM_POL_ASSIGN_TO;
            } else {
                reqPermsMsg = "Incorrect types for assignment: " + sType1
                        + "--->" + sType2;
                return false;
            }
        } else if (sType1.equalsIgnoreCase(PM_NODE.USER.value)) {
            sReqPerms1 = PM_USER_ASSIGN;
            if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                sReqPerms2 = PM_UATTR_ASSIGN_TO;
            } else {
                reqPermsMsg = "Incorrect types for assignment: " + sType1
                        + "--->" + sType2;
                return false;
            }
        } else if (sType1.equalsIgnoreCase(PM_NODE.UATTR.value)) {
            sReqPerms1 = PM_UATTR_ASSIGN;
            if (sType2.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                sReqPerms2 = PM_UATTR_ASSIGN_TO;
            } else if (sType2.equalsIgnoreCase(PM_NODE.POL.value)) {
                sReqPerms2 = PM_POL_ASSIGN_TO;
            } else if (sType2.equalsIgnoreCase(PM_NODE.OPSET.value)) {
                sReqPerms1 = PM_UATTR_ASSIGN_TO_OPSET;
                sReqPerms2 = PM_OPSET_ASSIGN_TO;
            } else {
                reqPermsMsg = "Incorrect types for assignment: " + sType1
                        + "--->" + sType2;
                return false;
            }
        } else if (sType1.equalsIgnoreCase(PM_NODE.OPSET.value)) {
            sReqPerms1 = PM_OPSET_ASSIGN;
            if (sType2.equalsIgnoreCase(PM_NODE.OATTR.value)
                    || sType2.equalsIgnoreCase(PM_NODE.ASSOC.value)) {
                sReqPerms2 = PM_OPSET_ASSIGN_TO;
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
        printSet(resPerms, PM_PERM, "Permissions on entity of type " + sType1);
        if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms1)) {
            reqPermsMsg = "You're not authorized to assign entity "
                    + getEntityName(sId1, sType1);
            return false;
        }
        resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sId2,
                sType2);
        printSet(resPerms, PM_PERM, "Permissions on entity of type " + sType2);
        if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms2)) {
            reqPermsMsg = "You're not authorized to assign to entity "
                    + getEntityName(sId2, sType2);
            return false;
        }
        return true;
    }

    boolean requestAddOpsetPerms(String sSessId, String sProcId,
                                 String sBaseId, String sBaseType) throws Exception   {
        HashSet<String> resPerms;
        String sReqPerms;

        if (!sessionExists(sSessId)) {
            reqPermsMsg = "You're not in a Policy Machine session!";
            return false;
        }

        resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
                sBaseType);
        printSet(resPerms, PM_PERM,
                "Permissions on entity " + getEntityName(sBaseId, sBaseType));

        if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
            sReqPerms = PM_OATTR_CREATE_OPSET;
            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create an operation set assigned to "
                        + getEntityName(sBaseId, sBaseType);
                return false;
            }
        } else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
            sReqPerms = PM_UATTR_CREATE_OPSET;
            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create an operation set assigned to "
                        + getEntityName(sBaseId, sBaseType);
                return false;
            }
        } else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
            sReqPerms = PM_CONN_CREATE_OPSET;
            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
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
                                        String sClass, String sName, String sId) throws Exception  {
        HashSet<String> resPerms;
        String sReqPerms;

        if (!sessionExists(sSessId)) {
            reqPermsMsg = "You're not in a Policy Machine session!";
            return false;
        }

        if(sId == null){
            return true;
        }

        if (!sClass.equals(PM_CLASS_USER_NAME)
                && !sClass.equals(PM_CLASS_UATTR_NAME)
                && !sClass.equals(PM_CLASS_OBJ_NAME)
                && !sClass.equals(PM_CLASS_OATTR_NAME)
                && !sClass.equals(PM_CLASS_POL_NAME)
                && !sClass.equals(PM_CLASS_CONN_NAME)
                && !sClass.equals(PM_CLASS_OPSET_NAME)) {
            return true;
        }

        sReqPerms = PM_ENTITY_REPRESENT;
        String sType = classToType(sClass);
        resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sId, sType);
        printSet(resPerms, PM_PERM,
                "Permissions on " + getEntityName(sId, sType));
        if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
            reqPermsMsg = "You're not authorized to represent entity "
                    + getEntityName(sId, sType) + " of type " + sType;
            return false;
        }
        return true;
    }

    boolean requestAddObjectPerms(String sSessId, String sProcId,
                                  String sBaseId, String sBaseType) throws Exception  {
        HashSet<String> resPerms;
        String sReqPerms;

        if (!sessionExists(sSessId)) {
            reqPermsMsg = "You're not in a Policy Machine session!";
            return false;
        }

        resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
                sBaseType);
        printSet(resPerms, PM_PERM,
                "Obtained Permissions on " + getEntityName(sBaseId, sBaseType));

        if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
            sReqPerms = PM_OATTR_CREATE_OBJ;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create an object in "
                        + getEntityName(sBaseId, sBaseType);
                return false;
            }
        } else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
            sReqPerms = PM_POL_CREATE_OBJ;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create an object in "
                        + getEntityName(sBaseId, sBaseType);
                return false;
            }
        } else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
            sReqPerms = PM_CONN_CREATE_OBJ;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create an object in "
                        + getEntityName(sBaseId, sBaseType);
                return false;
            }
        }
        return true;
    }

    boolean requestAddUserPerms(String sSessId, String sProcId, String sBaseId,
                                String sBaseType) throws Exception  {
        HashSet<String> resPerms;
        String sReqPerms;

        if (!sessionExists(sSessId)) {
            reqPermsMsg = "You're not in a Policy Machine session!";
            return false;
        }

        resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
                sBaseType);
        printSet(resPerms, PM_PERM,
                "Permissions on " + getEntityName(sBaseId, sBaseType));

        if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
            sReqPerms = PM_UATTR_CREATE_USER;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create a user in this user attribute!";
                return false;
            }
        } else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
            sReqPerms = PM_CONN_CREATE_USER;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create a user in the connector!";
                return false;
            }
        }
        return true;
    }

    boolean requestAddUattrPerms(String sSessId, String sProcId,
                                 String sBaseId, String sBaseType) throws Exception  {
        HashSet<String> resPerms;
        String sReqPerms;

        // if (sClientId.equals("super") || sClientId.equals("serban")) return
        // true;
        System.out.println("Calling sessionExists");

        if (!sessionExists(sSessId)) {
            reqPermsMsg = "You're not in a Policy Machine session!";
            return false;
        }

        System.out.println("Calling getPermittedOpsOnEntityInternal");
        resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
                sBaseType);
        printSet(resPerms, PM_PERM,
                "Permissions on entity " + getEntityName(sBaseId, sBaseType));

        if (sBaseType.equalsIgnoreCase(PM_NODE.USER.value)) {
            sReqPerms = PM_USER_CREATE_UATTR;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create a user attribute for this user!";
                return false;
            }
            sReqPerms = PM_CONN_CREATE_UATTR;
            resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId,
                    PM_CONNECTOR_ID,PM_NODE.CONN.value);
            printSet(resPerms, PM_PERM, "Permissions on connector");

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create a user attribute assigned to connector!";
                return false;
            }
        } else if (sBaseType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
            sReqPerms = PM_UATTR_CREATE_UATTR;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create a user attribute contained in this user attribute!";
                return false;
            }
        } else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
            sReqPerms = PM_POL_CREATE_UATTR;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create a user attribute in this policy class!";
                return false;
            }
        } else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
            sReqPerms = PM_CONN_CREATE_UATTR;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create a user attribute on the connector!";
                return false;
            }
        }
        return true;
    }


    public static boolean modifyingTable;

    public boolean getTableModifying(){
        return modifyingTable;
    }
    /**
     * Emulates work with table
     * @author JOSH
     */
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
                                 String sBaseId, String sBaseType) throws Exception  {
        HashSet<String> resPerms;
        String sReqPerms;

        System.out.println("requestAddOattrPerms()");
        System.out.println("sBaseId: " + sBaseId);
        System.out.println("sBaseType: " + sBaseType);

        // if (sClientId.equals("super") || sClientId.equals("serban")) return
        // true;

        if (!sessionExists(sSessId)) {
            reqPermsMsg = "You're not in a Policy Machine session!";
            System.out.println(reqPermsMsg);
            return false;
        }

        resPerms = getPermittedOpsOnEntityInternal(sSessId, sProcId, sBaseId,
                sBaseType);
        System.out.println("resPerms: " + resPerms);
        printSet(resPerms, PM_PERM,
                "Permissions on entity " + getEntityName(sBaseId, sBaseType));

        if (sBaseType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
            sReqPerms = PM_OATTR_CREATE_OATTR;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create an object attribute in this object attribute!";
                System.out.println(reqPermsMsg);
                return false;
            }

        } else if (sBaseType.equalsIgnoreCase(PM_NODE.POL.value)) {
            sReqPerms = PM_POL_CREATE_OATTR;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create an object attribute in this policy class!";
                System.out.println(reqPermsMsg);
                return false;
            }
        } else if (sBaseType.equalsIgnoreCase(PM_NODE.CONN.value)) {
            sReqPerms = PM_CONN_CREATE_OATTR;

            if (!resPerms.contains(PM_ANY_ANY) && !resPerms.contains(sReqPerms)) {
                reqPermsMsg = "You're not authorized to create an object attribute on the connector!";
                System.out.println(reqPermsMsg);
                return false;
            }
        }
        return true;
    }

    public Integer addUattrInternal(String sName, String sDescr, String sInfo,
                                    String sBaseId, String sBaseType, String[] sProps) {
        // Test if duplicate name.
        try {
            if(sName == null || sName.equals("")){
                errorMessage = "addUattrInternal received a null attribute name";
                return null;
            }

            if (entityNameExists(sName, PM_NODE.UATTR.value)) {
                errorMessage = "Attribute with duplicate name!";
                return null;
            }

            // Create a user Node
            Integer newNodeId = createNode(sName, PM_NODE.UATTR.value, sDescr,
                    sBaseId == null ? null : Integer.valueOf(sBaseId));

            // Add the attribute's properties, if any.
            if(sProps != null && sProps.length > 0){
                insertProperties(sProps, PM_NODE.UATTR.value, newNodeId);
            }

            // Return the attribute name and id.
            return newNodeId;
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = e.getMessage();
            return null;
        }
    }

    // For a given user attribute or the connector node, get the users (ids)
    // assigned to it.

    public ArrayList<Integer> getFromUsers(QueueElement qe) throws Exception {
        return getFromUsers(qe.getId(), qe.getType());
    }

    /**
     * For a given user attribute or the connector node, get the users (ids) assigned to it.
     *
     * @param sBaseId the ID of the base node
     * @param sBaseType the type of the base node
     * @return the users assigned to it
     * @throws Exception
     */
    public ArrayList<Integer> getFromUsers(String sBaseId, String sBaseType) throws Exception {
        return getFromAttrs(sBaseId, PM_NODE.USER.value, DEPTH);
    }

    // For a given user attribute or object attribute get the direct ascendant
    // attributes.
    // For a given operation set, get the user attributes assigned to it.
    public ArrayList<Integer> getFromAttrs(QueueElement qe) throws Exception {
        String sContainer = null;
        if (qe.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
            return getFromAttrs(qe.getId(), PM_NODE.UATTR.value, NO_DEPTH);
        } else if (qe.getType().equalsIgnoreCase(PM_NODE.OATTR.value)
                || qe.getType().equalsIgnoreCase(PM_NODE.ASSOC.value)) {
            return getFromAttrs(qe.getId(), PM_NODE.OATTR.value, NO_DEPTH);
        } else if (qe.getType().equalsIgnoreCase(PM_NODE.OPSET.value)) {
            return getFromAttrs(qe.getId(), PM_NODE.UATTR.value, NO_DEPTH);
        } else {
            return null;
        }
    }

    /**
     * For a given user, user attribute, or object attribute, get its direct
     * descendant attributes.
     * For a given operation set, get the object attributes this opset is
     * assigned to.
     *
     * @param qe
     * @return an ArrayList of ArrayList of Objects.  Each element in inner ArrayList wil be a name:id pair
     * @throws Exception
     */
    public ArrayList<ArrayList<Object>>getToAttrs(QueueElement qe) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, qe.getId());
        return select(GET_TO_ATTRS, params);
    }

    /**
     * Get the attributes a user is assigned to
     * @param sUserId the intended user's ID
     * @return a vector of IDs for the attributes the user is assigned to
     * @throws Exception
     */
    public Vector<String> getUserDescendantsInternalVector(String sUserId) throws Exception {
        Vector<String> uattrs = new Vector<String>();
        HashSet<String> attrs = getUserDescendantsInternal(sUserId);
        Iterator<String> iter = attrs.iterator();
        while(iter.hasNext()){
            uattrs.add(iter.next());
        }

        return uattrs;
    }

    /**
     * Get the attributes that a given attr is assigned to, and store in a vector
     * @param sAttrId the ID of an user attribute.
     * @param vector the vector to store descendants in
     * @throws Exception
     */
    public void getDescAttrsVector(String sAttrId, Vector<String> vector) throws Exception {
        // If the argument attribute is in the set, all its descendants already
        // are there.
        if (vector.contains(sAttrId)) {
            return;
        }
        // Add sAttrId to the set, together with all its descendants.
        vector.add(sAttrId);

        ArrayList<Integer> attrs = getToAttrs(sAttrId, PM_NODE.UATTR.value, DEPTH);
        if (attrs == null) {
            return;
        }
        for(int i = 0; i < attrs.size(); i++){
            getDescAttrsVector(String.valueOf(attrs.get(i)), vector);
        }
    }

    /**
     * Get the attributes that a given user is assigned to.
     * @param sUserId the ID of a user attribute.
     * @return a HashSet containing the IDs of descendants of the given user
     * @throws Exception
     */
    public HashSet<String> getUserDescendantsInternal(String sUserId) throws Exception {
        HashSet<String> set = new HashSet<String>();

        ArrayList<Integer> attrs = getToAttrs(sUserId, PM_NODE.UATTR.value, DEPTH);
        if (attrs == null) {
            return set;
        }
        for(int i = 0; i < attrs.size(); i++){
            getDescAttrs(String.valueOf(attrs.get(i)), set);
        }
        return set;
    }

    /**
     * Get the user attributes a given user attribute is assigned to.
     * @param sAttrId the ID of the user attribute we are finding descendants for
     * @param set a HashSet to add the descendants to
     * @throws Exception
     */
    public void getDescAttrs(String sAttrId, HashSet<String> set) throws Exception {
        // If the argument attribute is in the set, all its descendants already
        // are there.
        if (set.contains(sAttrId)) {
            return;
        }
        // Add sAttrId to the set, together with all its descendants.
        set.add(sAttrId);

        ArrayList<Integer> children = getToAttrs(sAttrId, PM_NODE.UATTR.value, DEPTH);
        if(children == null) {
            return;
        }

        for (int i = 0; i < children.size(); i++) {
            getDescAttrs(String.valueOf(children.get(i)), set);
        }
    }

    /*
	/**
	 * For a given user, user attribute, object attribute, or opset, get its direct descendant attributes.
	 * @param sId ID of an attribute to get the descendants of
	 * @param sType the type of the given attribute
	 * @return an ArrayList of the IDs of the descendants
	 * @throws Exception
	 *
	public ArrayList<Integer> getToAttrs(String sId, String sType) throws Exception {
		ArrayList<Integer> attrs = new ArrayList<Integer>();
		MySQL_Parameters params = new MySQL_Parameters();
		params.addParam(ParamType.STRING, sType);
		params.addParam(ParamType.INT, sId);

		try{
			ArrayList<ArrayList<Object>> select = select(MySQL_Statements.GET_TO_ATTRS, params);
			for(ArrayList<Object> attr : select){
				attrs.add((Integer) attr.get(0));
			}
		}catch(SQLException sqle){
			return null;
		}
		return attrs;
	}*/


    /**
     * Get all object attributes associated with objects of a container.
     * @param sContId the ID of the container
     * @param assocOattrs a HashSet to store all of the associated oattrs
     * @throws Exception
     */
    public void getMemberObjects(String sContId, HashSet<String> assocOattrs) throws Exception {
        new Vector<String>();
        new HashSet<String>();

        //just getting nodes of type 'o' since there are no longer oattrs associated with objects
        ArrayList<Integer> objects = getFromAttrs(sContId, PM_NODE.ASSOC.value, NO_DEPTH);

        for(Integer i : objects){
            assocOattrs.add(i.toString());
        }
    }

    /**
     * A non-recursive version for obtaining the users that are members of a
     * user attribute.
     * They are added to the HashSet users. Should be called like this:
     * HashSet users = new HashSet(); getMemberUsers(sUaId, users);
     *
     * @param sUattrId the uattr ID
     * @param users HashSet to hold all of the member users IDs
     * @throws Exception
     */
    public void getMemberUsers(String sUattrId, HashSet<Integer> users) throws Exception {
        ArrayList<Integer> memUsers = getFromAttrs(sUattrId, PM_NODE.USER.value, NO_DEPTH);
        for(Integer user : memUsers){
            users.add(user);
        }
    }

    /**
     * Get the descendants of an object attribute in a given policy class.
     * @param sOattrId the ID of the Oattr to get descendants of
     * @param sPcId the ID of the policy class
     * @return a HashSet containing the IDs of the descendants
     * @throws Exception
     */
    public HashSet<String> getDescOattrsInPc(String sOattrId, String sPcId)
            throws Exception {
        Vector<String> queue = new Vector<String>();
        HashSet<String> visited = new HashSet<String>();
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sOattrId);
        ArrayList<ArrayList<Object>> children = select(GET_TO_ATTRS, params);

        if (children == null) {
            return visited;
        }

        ArrayList<Integer> ids = extractIntegers(children);

        // The assoc attribute has children attributes. Check whether the
        // assoc. is in pc.
        if (!attrIsAscendantToPolicy(sOattrId,sPcId)) {
            return visited;
        }

        // Insert its children into the queue.
        for (int i = 0; i < ids.size(); i++) {
            queue.add(String.valueOf(ids.get(i)));
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
            if (!attrIsAscendantToPolicy(sOaId, sPcId)) {
                continue;
            }

            // Insert its children into the queue.
            params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sOaId);
            params.addParam(ParamType.STRING, PM_NODE.OATTR.value);
            children = select(GET_TO_ATTRS_T_D, params);
            ids = extractIntegers(children);

            if (children != null) {
                for (int i = 0; i < ids.size(); i++) {
                    queue.add(String.valueOf(ids.get(i)));
                }
            }

            // Mark this element as visited.
            visited.add(sOaId);
        }
        return visited;
    }

    /**
     * Method to extract the IDs for sql statements that return a collection of IDs
     * @param input the result of calling a select
     * @return and ArrayList containing the IDs as Integers
     */
    public ArrayList<String> extractStrings(ArrayList<ArrayList<Object>> input) {
        ArrayList<String> ret = new ArrayList<String>();
        if (input==null || input.isEmpty()) {
            return null;
        }
        for(ArrayList<Object> line : input){
            ret.add((String) line.get(0));
        }
        return ret;
    }

    /**
     * Method to extract the IDs for sql statements that return a collection of IDs
     * @param input the result of calling a select
     * @return and ArrayList containing the IDs as Integers
     */
    public ArrayList<Integer> extractIntegers(ArrayList<ArrayList<Object>> input) {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        if (input==null) return null;
        for(ArrayList<Object> line : input){
            ret.add((Integer) line.get(0));
        }
        return ret;
    }


    /**
     * Get the vector of object attributes an opset is assigned to.
     * @param sOpsetId the ID of the opset we want to get the assigned object attributes of
     * @return a vector of the assigned object atributes
     * @throws Exception
     */
    public Vector<Integer> getToAttrsOpset(String sOpsetId) throws Exception {
        ArrayList<Integer> select = getToAttrs(sOpsetId, null/*PM_NODE.OATTR.value*/, DEPTH);
        Vector<Integer> attrs = new Vector<Integer>();
        for(Integer attr : select){
            attrs.add(attr);
        }
        return attrs;
    }

    /**
     * Get the vector of user attributes assigned to an opset
     * @param sOpsetId the ID of the opset we want to get the assigned user attributes of
     * @return
     * @throws Exception
     */
    public ArrayList<Integer> getFromAttrs(String sOpsetId) throws Exception {
        return getFromAttrs(sOpsetId, PM_NODE.UATTR.value, DEPTH);
    }

    public HashSet<String> getFromAttrsSet(String baseId, String attrsType, int depth){
        HashSet<String> attrs = new HashSet<String>();
        List<Integer> iAttrs = getFromAttrs(baseId, attrsType, depth);
        if(iAttrs == null){
            return attrs;
        }
        for(Integer i : iAttrs){
            attrs.add(i.toString());
        }
        return attrs;
    }

    public HashSet<String> getToAttrsSet(String baseId, String attrsType, int depth){
        HashSet<String> attrs = new HashSet<String>();
        List<Integer> iAttrs = getToAttrs(baseId, attrsType, depth);
        if(iAttrs == null){
            return attrs;
        }
        for(Integer i : iAttrs){
            attrs.add(i.toString());
        }
        return attrs;
    }

    private ArrayList<Integer> setToList(HashSet<String> hs){
        ArrayList<Integer> list = new ArrayList<Integer>();
        Iterator<String> iter = hs.iterator();
        while(iter.hasNext()){
            list.add(Integer.valueOf(iter.next()));
        }
        return list;
    }

    /**
     * IMPORTANT METHOD
     * Get the attributes of a given type that are assigned to a node (given its ID)
     * @param baseId the ID of the base node to get the attrs
     * @param attrsType the type of attrs to get that are assigned to the base node
     * @return an ArrayList containing the IDs of the attributes that are assigned to a base node
     * @throws Exception
     */
    public ArrayList<Integer> getFromAttrs(String baseId, String attrsType, int depth){
        try {
            if (baseId == null || baseId.equals("")) {
                return null;
            }
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, baseId);
            if (attrsType == null) {
                if (depth == NO_DEPTH) {
                    //there is no depth or type - call GET_FROM_ATTRS
                    return extractIntegers(select(GET_FROM_ATTRS, params));
                } else {
                    //there is a depth - hardcoded 1
                    if(graphMgr.isBuilt()) {
                        PmGraphNode node = graphMgr.getNode(baseId);
                        if(node != null){
                            return setToList(node.getMembers());
                        }
                    }
                    return extractIntegers(select(GET_FROM_ATTRS_D, params));
                }

            } else {
                params.addParam(ParamType.STRING, attrsType);

                if (depth == NO_DEPTH) {
                    //no depth but there is type
                    return extractIntegers(select(GET_FROM_ATTRS_T, params));
                } else {
                    //depth and type - hardcoded as 1
                    //return extractIntegers(select(GET_FROM_ATTRS_T_D, params));
                    if(graphMgr.isBuilt()) {
                        PmGraphNode node = graphMgr.getNode(baseId);
                        if (node != null) {
                            ArrayList<Integer> attrs = new ArrayList<Integer>();
                            HashSet<String> members = node.getMembers();
                            for (String m : members) {
                                if (graphMgr.getNode(m).getType().equals(attrsType)) {
                                    attrs.add(Integer.valueOf(m));
                                }
                            }
                            return attrs;
                        }
                    }
                    return extractIntegers(select(GET_FROM_ATTRS_T_D, params));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * IMPORTANT METHOD
     * Get the attributes of a given type that a node (given it's ID) is assigned to
     * @param baseId the ID of the base node to get the attrs
     * @param attrsType the type of attrs to get that are assigned to the base node
     * @return an ArrayList containing the IDs of the attributes that are assigned to a base node
     * @throws Exception
     */
    public ArrayList<Integer> getToAttrs(String baseId, String attrsType, int depth){
        try {
            if (baseId == null || baseId.equals("")) {
                return null;
            }
            MySQL_Parameters params = new MySQL_Parameters();
            if (attrsType == null) {
                params.addParam(ParamType.INT, baseId);
                if (depth == NO_DEPTH) {
                    //there is no depth or type - call GET_TO_ATTRS
                    return extractIntegers(select(GET_TO_ATTRS, params));
                } else {
                    //there is a hardcoded depth 1 - call GET_TO_ATTRS_D
                    return extractIntegers(select(GET_TO_ATTRS_D, params));
                }
            } else {
                params.addParam(ParamType.INT, baseId);
                params.addParam(ParamType.STRING, attrsType);
                if (depth == NO_DEPTH) {
                    //no depth but there is type - call GET_TO_ATTRS_T
                    return extractIntegers(select(GET_TO_ATTRS_T, params));
                } else {
                    //depth and type - call GET_TO_ATTRS_T_D
                    return extractIntegers(select(GET_TO_ATTRS_T_D, params));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * For a given user attribute, get the operation sets this attribute is
     * assigned to.
     * @param qe QueueElement containing user attribute
     * @return a vector of operation sets
     * @throws Exception
     */
    public Vector<Integer> getToOpsets(QueueElement qe) throws Exception {
        return getToOpsets(qe.getId());
    }


    /**
     * For an object attribute, get the operation sets assigned to this oattr.
     * @param sAttrId Find the operation sets the object attribute with this ID is assigned to
     * @return a Vector<String> containing the IDs of the opsets TODO maybe return Vector<String>
     * @throws Exception
     */

    public ArrayList<Integer> getFromOpsets(String sAttrId) throws Exception {
        return getFromAttrs(sAttrId, PM_NODE.OPSET.value, DEPTH);
    }

    /**
     * For a user attribute, get the operation sets this attribute is assigned to, as a vector.
     * @param sAttrId user atribute ID to find operation sets its assigned to
     * @return a Vector<Integer> of the IDs of the operations sets the given user attribute is assigned to
     * @throws Exception
     */
    public Vector<Integer> getToOpsets(String sAttrId) throws Exception {
        Vector<Integer> attrs = new Vector<Integer>();

        ArrayList<Integer> select = getToAttrs(sAttrId, PM_NODE.OPSET.value, DEPTH);
        for(Integer attr : select){
            attrs.add((Integer) attr);
        }

        return attrs;
    }

    /**
     * For a given user attribute or object attribute, get the policy classes this attribute is assigned to (directly).
     * @param qe the QueueElement containing the attribute ID
     * @return
     * @throws Exception
     */
    public ArrayList<Integer> getToPolicies(QueueElement qe) throws Exception {
        String sBaseId = qe.getId();
        String sBaseType = qe.getType();
        return getToPolicies(sBaseId, sBaseType);
    }

    /**
     * For a given user or object attribute, get the policy classes the
     * attribute is assigned to.
     * Argument sType should bePM_NODE.UATTR.value orPM_NODE.OATTR.value orPM_NODE.ASSOC.value.
     * @param sId the ID of the given attribute
     * @param sType the type of the given attribute
     * @return an ArrayList of IDs of the policy classes it is assigned to
     * @throws Exception
     */
    public ArrayList<Integer> getToPolicies(String sId, String sType) throws Exception{
        return getToAttrs(sId, PM_NODE.POL.value, DEPTH);
    }

    /**
     *
     * @param sId
     * @param sType
     * @return
     * @throws Exception
     */
    public ArrayList<Integer> getToConnector(String sId, String sType) throws Exception {
        return getToAttrs(sId, PM_NODE.CONN.value, DEPTH);
    }

    /**
     * For the connector node, get the policy classes assigned to it (i.e.,
     * all).
     * NOTE THAT THE ATTRIBUTE SHOULD BE CALLED pmFromPolicy, BUT I WRONGLY
     * DEFINED IT AS BEING SINGLE VALUED, SO I HAD TO DEFINE A NEW ONE,
     * MULTI-VALUED.
     * @param qe
     * @return ArrayList<Integer> containing the policy classes assigned to the connector node
     * @throws Exception
     */
    public ArrayList<Integer> getFromPolicies(QueueElement qe) throws Exception {
        String sBaseId = qe.getId();
        String sBaseType = "c";
        return getFromPolicies(sBaseId, sBaseType);
    }

    /**
     * For the connector node, get the policy classes assigned to it (i.e.,
     * all).
     * @param sBaseId
     * @param sBaseType
     * @return ArrayList<Integer> containing the policy classes assigned to the connector node
     * @throws Exception
     */
    public ArrayList<Integer> getFromPolicies(String sBaseId, String sBaseType) throws Exception{
        return getFromAttrs(sBaseId, PM_NODE.POL.value, DEPTH);
    }

    // For the connector node or an object attribute, get the operation sets
    // assigned to it.

    public ArrayList<Integer> getFromOpsets(QueueElement qe) throws Exception {
        return getFromOpsets(qe.getId(), qe.getType());
    }


    public ArrayList<Integer> getFromOpsets(String sBaseId, String sBaseType) throws Exception{
        return getFromAttrs(sBaseId, PM_NODE.OPSET.value, DEPTH);
    }

    // For a policy class or connector node, get the user attributes
    // (directly) assigned to it. Params:
    // qe: a QueueElement that contains the id and type of the base node.

    public ArrayList<Integer> getFromUserAttrs(QueueElement qe) throws Exception {//TODO
        return getFromUserAttrs(qe.getId(), qe.getType());
    }

    // For a policy class or connector node, get the user attributes
    // (directly) assigned to it.

    public ArrayList<Integer> getFromUserAttrs(String sBaseId, String sBaseType) throws Exception {
        return getFromAttrs(sBaseId, PM_NODE.UATTR.value, DEPTH);
    }

    /**
     * For a policy class or the connector node, get the object attributes
     * (directly) assigned to it.
     * @param qe  a QueueElement that contains the id and type of the policy class or the connector node.
     * @return an ArrayList<Integer> containing the object attributes directly assigned to the base node
     * @throws Exception
     */
    public ArrayList<Integer> getFromObjAttrs(QueueElement qe) throws Exception {
        String sType = qe.getType();
        String sId = qe.getId();
        return getFromObjAttrs(sId, sType);
    }

    /**
     * For a policy class or the connector node, get the object attributes
     * (directly) assigned to it.
     * @param sBaseId ID of the policy class or the connector
     * @param sBaseType either a policy class or the connector
     * @return an ArrayList<Integer> containing the object attributes directly assigned to the base node
     * @throws Exception
     */
    public ArrayList<Integer> getFromObjAttrs(String sBaseId, String sBaseType) throws Exception{
        return getFromAttrs(sBaseId, PM_NODE.OATTR.value, DEPTH);
    }

    public boolean sessionExists(String sessionId) throws Exception {
        if (sessionId == null) {
            return false;
        }
        Integer userId = null;
        ArrayList<ArrayList<Object>> returned = null;
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, Integer.valueOf(sessionId));
        returned = select(GET_SESS_USER_ID, params);
        if(returned.size() > 0){
            userId = (Integer)returned.get(0).get(0);
        }
        System.out.println("userId is "+ userId);
        if (userId==null) {
            reqPermsMsg = "You're not in a Policy Machine session!";
            return false;
        }
        return true;
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

}

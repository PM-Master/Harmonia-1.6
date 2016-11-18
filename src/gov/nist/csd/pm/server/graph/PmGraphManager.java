package gov.nist.csd.pm.server.graph;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.PM_NODE;

import java.util.*;

public class PmGraphManager {

    public PmGraph graph;
    private boolean isBuilt;

    public PmGraphManager(PmGraph g){
        graph = g;
    }

    public void build(){
        isBuilt = false;
        graph.reset();

        HashSet<String> members = new HashSet<String>();
        HashSet<String> containers = new HashSet<String>();

        //c
        members = listToSet(ServerConfig.SQLDAO.getFromAttrs(PM_CONNECTOR_ID, null, 1));
        graph.addNode(PM_CONNECTOR_ID, new PmGraphNode(PM_CONNECTOR_NAME, PM_NODE.CONN.value, PM_CONNECTOR_ID, members, containers, null));

        //p
        try {
            List<Integer> pcs = ServerConfig.SQLDAO.getPolicies();
            for(Integer i : pcs){
                String name = ServerConfig.SQLDAO.getEntityName(i.toString(), PM_NODE.POL.value);
                members = listToSet(ServerConfig.SQLDAO.getFromAttrs(i.toString(), null, 1));
                containers = listToSet(ServerConfig.SQLDAO.getToAttrs(i.toString(), null, 1));
                graph.addNode(i.toString(), new PmGraphNode(name, PM_NODE.POL.value, i.toString(), members, containers, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //a
        try {
            List<Integer> uattrs = ServerConfig.SQLDAO.getUattrs();
            for(Integer i : uattrs){
                String name = ServerConfig.SQLDAO.getEntityName(i.toString(), PM_NODE.UATTR.value);
                members = listToSet(ServerConfig.SQLDAO.getFromAttrs(i.toString(), null, 1));
                containers = listToSet(ServerConfig.SQLDAO.getToAttrs(i.toString(), null, 1));
                graph.addNode(i.toString(), new PmGraphNode(name, PM_NODE.UATTR.value, i.toString(), members, containers, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //u
        try {
            List<Integer> users = ServerConfig.SQLDAO.getUsers();
            for(Integer i : users){
                String name = ServerConfig.SQLDAO.getEntityName(i.toString(), PM_NODE.USER.value);
                members = listToSet(ServerConfig.SQLDAO.getFromAttrs(i.toString(), null, 1));
                containers = listToSet(ServerConfig.SQLDAO.getToAttrs(i.toString(), null, 1));
                graph.addNode(i.toString(), new PmGraphNode(name, PM_NODE.USER.value, i.toString(), members, containers, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //b
        try {
            List<Integer> oattrs = ServerConfig.SQLDAO.getOattrs();
            for(Integer i : oattrs){
                String name = ServerConfig.SQLDAO.getEntityName(i.toString(), PM_NODE.OATTR.value);
                members = listToSet(ServerConfig.SQLDAO.getFromAttrs(i.toString(), null, 1));
                containers = listToSet(ServerConfig.SQLDAO.getToAttrs(i.toString(), null, 1));
                graph.addNode(i.toString(), new PmGraphNode(name, PM_NODE.OATTR.value, i.toString(), members, containers, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //o
        try {
            List<Integer> objs = ServerConfig.SQLDAO.getObjs();
            for(Integer i : objs){
                String name = ServerConfig.SQLDAO.getEntityName(i.toString(), PM_NODE.ASSOC.value);
                members = listToSet(ServerConfig.SQLDAO.getFromAttrs(i.toString(), null, 1));
                containers = listToSet(ServerConfig.SQLDAO.getToAttrs(i.toString(), null, 1));
                graph.addNode(i.toString(), new PmGraphNode(name, PM_NODE.ASSOC.value, i.toString(), members, containers, null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //s
        try {
            List<Integer> opsets = ServerConfig.SQLDAO.getOpsets();
            for(Integer i : opsets){
                String name = ServerConfig.SQLDAO.getEntityName(i.toString(), PM_NODE.OPSET.value);
                members = listToSet(ServerConfig.SQLDAO.getFromAttrs(i.toString(), null, 1));
                containers = listToSet(ServerConfig.SQLDAO.getToAttrs(i.toString(), null, 1));
                HashSet<String> ops = new HashSet<String>(ServerConfig.SQLDAO.getOpsetOperations(i.toString()));
                graph.addNode(i.toString(), new PmGraphNode(name, PM_NODE.OPSET.value, i.toString(), members, containers, ops));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //printGraph();
        isBuilt = true;
    }

    public boolean isBuilt(){
        return isBuilt;
    }

    public void addNode(String baseId, PmGraphNode node){
        graph.addNode(node.getId(), node);
        if(baseId != null)graph.getNode(baseId).addMember(node.getId());
    }

    public void deleteNode(String id){
        HashSet<String> containers = graph.getNode(id).getContainers();
        for(String i : containers){
            graph.getNode(i).deleteMember(id);
        }
        HashSet<String> members = graph.getNode(id).getMembers();
        for(String i : members){
            graph.getNode(i).deleteContainer(id);
        }
        graph.deleteNode(id);
    }

    public void updateNode(PmGraphNode node){
        graph.getGraph().put(node.getId(), node);
    }

    public PmGraph getGraph(){
        return graph;
    }

    private HashSet<String> listToSet(List<Integer> list){
        HashSet<String> sList = new HashSet<String>();
        for(Integer i : list){
            sList.add(i.toString());
        }
        return sList;
    }

    public PmGraph getTypeSubGraph(String type){
        PmGraph subGraph = new PmGraph();
        Iterator<String> iter = graph.getGraph().keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            PmGraphNode node = graph.getNode(key);
            if(node.getType().equals(type)){
                subGraph.addNode(key, node);
            }
        }
        return subGraph;
    }

    public void printGraph(){
        System.out.println("********** START GRAPH **********");
        Iterator<String> iter = graph.getGraph().keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            PmGraphNode node = graph.getNode(key);
            System.out.println(node);
        }
        System.out.println("********** END GRAPH **********");
    }

    public PmGraphNode getNode(String id){
        return graph.getNode(id);
    }

    public List<PmGraphNode> getAllNodes(){
        List<PmGraphNode> allNodes = new ArrayList<PmGraphNode>();
        Iterator<String> iter = graph.getGraph().keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            PmGraphNode node = graph.getNode(key);
            allNodes.add(node);
        }
        return allNodes;
    }
}

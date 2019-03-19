package gov.nist.csd.pm.server.graph;

import static gov.nist.csd.pm.common.constants.GlobalConstants.*;
import static gov.nist.csd.pm.common.constants.MySQL_Statements.GET_ALL_NODES;
import static gov.nist.csd.pm.common.constants.MySQL_Statements.select;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.PM_NODE;

import java.io.FileReader;
import java.util.*;

public class PmGraphManager {

    public PmGraph graph;
    private boolean isBuilt;

    public PmGraphManager(PmGraph g){
        graph = g;
    }

    public void build() throws Exception {
        isBuilt = false;
        graph.reset();

        ArrayList<ArrayList<Object>> results = select(GET_ALL_NODES);
        for (ArrayList<Object> r : results)
        {
            String name = (String) r.get(0);
            String id = String.valueOf(r.get(1));
            String type = (String) r.get(2);

            HashSet<String> children = listToSet(ServerConfig.SQLDAO.getFromAttrs(id, null, 1));
            HashSet<String> parents = listToSet(ServerConfig.SQLDAO.getToAttrs(id, null, 1));
            HashSet<String> operations = null;
            if (type.equals(PM_NODE.OPSET.value))
            {
                operations = listToSetString(ServerConfig.SQLDAO.getOpsetOperations(id));
            }
            graph.addNode(id, new PmGraphNode(name, type, id, children,
                    parents, operations));
        }

        isBuilt = true;
    }

    public static HashSet<Integer> listToSetInt(List<Integer> list)
    {
        HashSet<Integer> ret = new HashSet<Integer>();
        for (Integer i : list)
        {
            ret.add(i);
        }
        return ret;
    }

    public static HashSet<String> listToSetString(List<String> list)
    {
        HashSet<String> ret = new HashSet<String>();
        for (String s : list)
        {
            ret.add(s);
        }
        return ret;
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
        if(list == null){
            return null;
        }

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

    public PmGraphManager fromJson(String path){
        graph.getGraph().clear();
        try {
            JsonReader reader = new Gson().newJsonReader(new FileReader(path));
            reader.setLenient(true);
            reader.beginObject();

            while(reader.hasNext()){
                String j = reader.nextName();
                String name = reader.nextString();

                j = reader.nextName();
                String type = reader.nextString();

                j = reader.nextName();
                String id = reader.nextString();

                j = reader.nextName();
                reader.beginArray();
                HashSet<String> members = new HashSet<>();
                while(reader.hasNext()){
                    String m = reader.nextString();
                    members.add(m);
                }
                reader.endArray();

                j = reader.nextName();
                reader.beginArray();
                HashSet<String> containers = new HashSet<>();
                while(reader.hasNext()){
                    String c = reader.nextString();
                    containers.add(c);
                }
                reader.endArray();

                HashSet<String> operations = new HashSet<>();
                if(type.equals("s")){
                    j = reader.nextName();
                    reader.beginArray();
                    while(reader.hasNext()){
                        String o = reader.nextString();
                        operations.add(o);
                    }
                    reader.endArray();
                }

                PmGraphNode node = new PmGraphNode(name, type, id, members, containers, operations.isEmpty()?null:operations);
                graph.addNode(id, node);
                reader.endObject();
                reader.beginObject();
            }
        } catch (Exception e) {}
        return this;
    }

    public void clearGraph(){
        Iterator<String> iter = graph.getGraph().keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            if(Integer.valueOf(graph.getNode(key).getId()) > 7){
                graph.deleteNode(key);
            }
        }
        isBuilt = false;
    }
}

package gov.nist.csd.pm.server.graph;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class PmGraph {
    private ConcurrentHashMap<String, PmGraphNode> graph;
    //private Hashtable<Integer, PmGraphNode> graph;

    public PmGraph(){
        //graph = new Hashtable<Integer, PmGraphNode>();
        graph = new ConcurrentHashMap<String, PmGraphNode>();
    }

    public PmGraphNode getNode(String id){
        return graph.get(id);
    }

    public PmGraphNode addNode(String id, PmGraphNode node){
        return graph.put(id, node);
    }

    public void deleteNode(String id){
        graph.remove(id);
    }

    public ConcurrentHashMap<String, PmGraphNode> getGraph(){
        return graph;
    }

    public void reset(){
        graph.clear();
    }

    public int size(){
        return graph.size();
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof PmGraph)){
            return false;
        }

        PmGraph g = (PmGraph)o;

        if(this.size() != g.size()){
            return false;
        }

        Iterator<String> iter = graph.keySet().iterator();
        while(iter.hasNext()){
            String key = iter.next();
            if(!g.getGraph().keySet().contains(key)){
                return false;
            }
            PmGraphNode node = getNode(key);
            PmGraphNode gNode = g.getNode(key);
            if(!node.equals(gNode)){
                return false;
            }
        }

        return true;
    }
}

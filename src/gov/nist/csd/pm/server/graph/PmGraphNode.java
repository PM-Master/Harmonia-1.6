package gov.nist.csd.pm.server.graph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.HashSet;
import java.util.Iterator;

public class PmGraphNode {
    private String name;
    private String type;
    private String id;
    private HashSet<String> members;
    private HashSet<String> containers;
    private HashSet<String> operations;

    public PmGraphNode(String name, String type, String id,
                       HashSet<String> members, HashSet<String> containers, HashSet<String> ops) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.members = members;
        this.containers = containers;
        this.operations = ops;
    }

    public String getName() {
        return name;
    }

    public PmGraphNode setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public PmGraphNode setType(String type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public PmGraphNode setId(String id) {
        this.id = id;
        return this;
    }

    public HashSet<String> getMembers() {
        return members;
    }

    public PmGraphNode setMembers(HashSet<String> members) {
        this.members = members;
        return this;
    }

    public void addMember(String memberId){
        this.members.add(memberId);
    }

    public void deleteMember(String memberId){
        this.members.remove(memberId);
    }

    public HashSet<String> getContainers() {
        return containers;
    }

    public PmGraphNode setContainers(HashSet<String> containers) {
        this.containers = containers;
        return this;
    }

    public void addContainer(String containerId){
        this.containers.add(containerId);
    }

    public void deleteContainer(String containerId){
        this.containers.remove(containerId);
    }

    public HashSet<String> getOperations(){
        return operations;
    }

    public void setOperations(HashSet<String> ops){
        this.operations = ops;
    }

    public void addOperation(String op){
        operations.add(op);
    }

    public String toString(){
        String s = new Gson().toJson(this);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(s);
        String json = gson.toJson(je);
        return json;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof PmGraphNode)){
            return false;
        }

        PmGraphNode n = (PmGraphNode) o;
        if(!this.getName().equals(n.getName())){
            return false;
        }

        if(!this.getType().equals(n.getType())){
            return false;
        }

        if(this.getId() != n.getId()){
            return false;
        }

        if(!members.equals(n.getMembers())){
            return false;
        }

        if(!containers.equals(n.getContainers())){
            return false;
        }

        if(operations != null) {
            if (!operations.equals(n.getOperations())) {
                return false;
            }
        }

        return true;
    }
}

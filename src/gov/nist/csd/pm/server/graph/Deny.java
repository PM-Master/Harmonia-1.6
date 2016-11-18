package gov.nist.csd.pm.server.graph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.HashSet;

/**
 * Created by jnr6 on 8/10/2016.
 */
public class Deny {

    private String id;
    private String name;
    private String deny_type;
    private boolean intersection;
    private HashSet<DenyObject> objects;
    private HashSet<String> ops;

    public Deny(){}

    public Deny(String id, String name, String deny_type, boolean intersection,
                HashSet<DenyObject> objects, HashSet<String> ops) {
        this.id = id;
        this.name = name;
        this.deny_type = deny_type;
        this.intersection = intersection;
        this.objects = objects;
        this.ops = ops;
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

    public String getDenyType() {
        return deny_type;
    }

    public void setDenyType(String deny_type) {
        this.deny_type = deny_type;
    }

    public boolean isIntersection() {
        return intersection;
    }

    public void setIntersection(boolean intersection) {
        this.intersection = intersection;
    }

    public HashSet<DenyObject> getObjects() {
        return objects;
    }

    public void setObjects(HashSet<DenyObject> objects) {
        this.objects = objects;
    }

    public void addObject(DenyObject denyObj){
        objects.add(denyObj);
    }

    public void addObject(String id, boolean comp){
        objects.add(new DenyObject(id, comp));
    }

    public HashSet<String> getOps() {
        return ops;
    }

    public void setOps(HashSet<String> ops) {
        this.ops = ops;
    }

    public void addOp(String op){
        ops.add(op);
    }

    public String toString(){
        String s = new Gson().toJson(this);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(s);
        String json = gson.toJson(je);
        return json;
    }
}

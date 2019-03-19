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
    private String denyType;
    private int processId;
    private int userId;
    private int sessionId;
    private boolean intersection;
    private HashSet<DenyObject> objects;
    private HashSet<String> ops;

    public Deny(){}

    public Deny(String id, String name, String denyType, int processId, int userId,
                boolean intersection, HashSet<DenyObject> objects, HashSet<String> ops) {
        this.id = id;
        this.name = name;
        this.denyType = denyType;
        this.processId = processId;
        this.userId = userId;
        this.intersection = intersection;
        this.objects = objects;
        this.ops = ops;
    }

    public Deny (String name, String type, boolean intersection){
        this.name = name;
        this.denyType = type;
        this.intersection = intersection;
        this.sessionId = -1;
        this.processId = -1;
        this.userId = -1;
        this.objects = new HashSet<>();
        this.ops = new HashSet<>();
    }

    public Deny session(int sessId){
        this.sessionId = sessId;
        return this;
    }

    public Deny process(int procId){
        this.processId = procId;
        return this;
    }

    public Deny user(int userId){
        this.userId = userId;
        return this;
    }

    public Deny objects(HashSet<DenyObject> objects){
        this.objects = objects;
        return this;
    }

    public Deny operations(HashSet<String> ops){
        this.ops = ops;
        return this;
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
        return denyType;
    }

    public void setDenyType(String denyType) {
        this.denyType = denyType;
    }

    public int getSessionId(){
        return sessionId;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public static Deny fromJson(String deny){
        Gson gson = new Gson();
        return gson.fromJson(deny, Deny.class);
    }
}

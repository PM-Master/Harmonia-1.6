package gov.nist.csd.pm.server.graph;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.net.Packet;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DenyManager {

    private List<Deny> denies;

    public DenyManager(){
        denies = new ArrayList<Deny>();
    }

    public DenyManager(List<Deny> d){
        denies = d;
    }

    public List<Deny> getDenies() {
        return denies;
    }

    public void setDenies(List<Deny> denies) {
        this.denies = denies;
    }

    public Deny getDeny(String denyId){
        for(Deny d : denies){
            if(d.getId().equals(denyId)){
                return d;
            }
        }
        return null;
    }

    public void addDeny(Deny d){
        denies.add(d);
    }

    public void deleteDeny(Deny d){
        denies.remove(d);
    }

    public void updateDeny(Deny d){
        for(int i = 0; i < denies.size(); i++){
            if(denies.get(i).getId().equals(d.getId())){
                denies.set(i, d);
            }
        }
    }

    public void build(){
        try {
            Packet p = ServerConfig.SQLDAO.getDenies(null);
            for (int i = 0; i < p.size(); i++) {
                String name = p.getItemStringValue(i).split(GlobalConstants.PM_FIELD_DELIM)[0];
                String id = p.getItemStringValue(i).split(GlobalConstants.PM_FIELD_DELIM)[1];
                Packet denyPacket = ServerConfig.SQLDAO.getDenyInfo(null, id);

                /*for(int j = 0; j < denyPacket.size(); j++){
                    System.out.println(j + ": " + denyPacket.getStringValue(j));
                }*/
                String[] pieces = denyPacket.getItemStringValue(1).split(GlobalConstants.PM_FIELD_DELIM);
                String type = pieces[0];
                int processId = -1;
                if(type.equals("process")){
                    processId = Integer.valueOf(pieces[2]);
                }
                Integer attrId = Integer.valueOf(pieces[2]);//either a user id or set id or process id
                boolean inter = pieces[3].equalsIgnoreCase("yes");

                HashSet<String> ops = new HashSet<String>();
                int opCount = Integer.valueOf(denyPacket.getItemStringValue(2));
                for(int j = 3; j < 3 + opCount; j++){
                    String op = denyPacket.getItemStringValue(j);
                    ops.add(op);
                }

                HashSet<DenyObject> objects = new HashSet<DenyObject>();
                int contCount = Integer.valueOf(denyPacket.getItemStringValue(3+opCount));
                for(int j = 3 + opCount + 1; j < 3 + opCount + 1 + contCount; j++){
                    pieces = denyPacket.getItemStringValue(j).split(GlobalConstants.PM_FIELD_DELIM);
                    String objName = pieces[0];
                    String objId = pieces[1];
                    DenyObject dO = new DenyObject(objId, objName.startsWith("!"));
                    objects.add(dO);
                }

                Deny deny = new Deny(id, name, type, processId, attrId, inter, objects, ops);
                denies.add(deny);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        //printDenies();
    }

    public void printDenies(){
        for(Deny d : denies){
            System.out.println(d);
        }
    }

    public DenyManager fromJson(String path){
        denies.clear();
        try {
            JsonReader reader = new Gson().newJsonReader(new FileReader(path));
            reader.setLenient(true);
            reader.beginObject();

            while(reader.hasNext()){
                String j = reader.nextName();
                String id = reader.nextString();

                j = reader.nextName();
                String name = reader.nextString();

                j = reader.nextName();
                String denyType = reader.nextString();

                j = reader.nextName();
                String processid = reader.nextString();

                j = reader.nextName();
                String userId = reader.nextString();

                j = reader.nextName();
                boolean intersection = reader.nextBoolean();

                j = reader.nextName();
                reader.beginArray();
                HashSet<DenyObject> objects = new HashSet<>();
                while(reader.hasNext()){
                    reader.beginObject();

                    j = reader.nextName();
                    String oaId = reader.nextString();

                    j = reader.nextName();
                    boolean c = reader.nextBoolean();

                    objects.add(new DenyObject(oaId, c));

                    reader.endObject();
                }
                reader.endArray();

                j = reader.nextName();
                reader.beginArray();
                HashSet<String> operations = new HashSet<>();
                while(reader.hasNext()){
                    String o = reader.nextString();
                    operations.add(o);
                }
                reader.endArray();


                Deny d = new Deny(id, name, denyType,
                        Integer.valueOf(processid), Integer.valueOf(userId),
                        intersection, objects, operations);
                addDeny(d);
                reader.endObject();
                reader.beginObject();
            }
        } catch (Exception e) {}
        return this;
    }

    public void clearDenies(){
        denies.clear();
    }
}

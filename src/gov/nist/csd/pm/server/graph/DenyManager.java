package gov.nist.csd.pm.server.graph;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.net.Packet;

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
                Packet denyPacket = ServerConfig.SQLDAO.getDenyInfo(null, id.toString());

                for(int j = 0; j < denyPacket.size(); j++){
                    System.out.println(j + ": " + denyPacket.getStringValue(j));
                }
                String[] pieces = denyPacket.getItemStringValue(1).split(GlobalConstants.PM_FIELD_DELIM);
                String type = pieces[0];
                String attrId = pieces[2];
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

                Deny deny = new Deny(id, name, type, inter, objects, ops);
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
}

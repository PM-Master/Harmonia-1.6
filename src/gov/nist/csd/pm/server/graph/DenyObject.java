package gov.nist.csd.pm.server.graph;

public class DenyObject {

    private String id;
    private boolean compliment;

    public DenyObject(){}

    public DenyObject(String id, boolean compliment) {
        this.id = id;
        this.compliment = compliment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isCompliment() {
        return compliment;
    }

    public void setCompliment(boolean compliment) {
        this.compliment = compliment;
    }

    public String toString(){
        return (compliment) ? "!" : "" + id;
    }
}

package gov.nist.csd.pm.server.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author  gavrila@nist.gov
 * @version  $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since  1.5
 */
public class ActionSpec {

    /**
	 * @uml.property  name="type"
	 */
    String type;
    /**
	 * @uml.property  name="name"
	 */
    String name;
    /**
	 * @uml.property  name="id"
	 */
    String id;
    /**
	 * @uml.property  name="isIntrasession"
	 */
    boolean isIntrasession;
    /**
	 * @uml.property  name="isIntersection"
	 */
    boolean isIntersection;
    /**
	 * @uml.property  name="condSpec"
	 * @uml.associationEnd  
	 */
    CondSpec condSpec;
    /**
	 * @uml.property  name="opnds1"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.OpndSpec"
	 */
    private Set<List<OpndSpec>> opnds1;
    /**
	 * @uml.property  name="opnds2"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.OpndSpec"
	 */
    private Set<List<OpndSpec>> opnds2;
    /**
	 * @uml.property  name="opnds3"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.OpndSpec"
	 */
    private Set<List<OpndSpec>> opnds3;
    /**
	 * @uml.property  name="opnds4"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.OpndSpec"
	 */
    private Set<List<OpndSpec>> opnds4;

    public ActionSpec(String id) {
        this(id, null);
    }

    public ActionSpec(String id, String type) {
        this.id = id;
        this.type = type;
        condSpec = null;
        opnds1 = new HashSet<List<OpndSpec>>();
        opnds2 = new HashSet<List<OpndSpec>>();
        opnds3 = new HashSet<List<OpndSpec>>();
        opnds4 = new HashSet<List<OpndSpec>>();
    }

    /**
	 * @return
	 * @uml.property  name="type"
	 */
    public String getType() {
        return type;
    }

    /**
	 * @return
	 * @uml.property  name="isIntrasession"
	 */
    public boolean isIntrasession() {
        return isIntrasession;
    }

    /**
	 * @return
	 * @uml.property  name="isIntersection"
	 */
    public boolean isIntersection() {
        return isIntersection;
    }

    /**
	 * @return
	 * @uml.property  name="id"
	 */
    public String getId() {
        return id;
    }

    /**
	 * @param type
	 * @uml.property  name="type"
	 */
    public void setType(String type) {
        this.type = type;
    }

    /**
	 * @param isIntrasession
	 * @uml.property  name="isIntrasession"
	 */
    public void setIsIntrasession(boolean isIntrasession) {
        this.isIntrasession = isIntrasession;
    }

    /**
	 * @param isIntersection
	 * @uml.property  name="isIntersection"
	 */
    public void setIsIntersection(boolean isIntersection) {
        this.isIntersection = isIntersection;
    }

    /**
	 * @param condSpec
	 * @uml.property  name="condSpec"
	 */
    public void setCondSpec(CondSpec condSpec) {
        this.condSpec = condSpec;
    }

    /**
	 * @return
	 * @uml.property  name="condSpec"
	 */
    public CondSpec getCondSpec() {
        return condSpec;
    }

    /**
	 * @return
	 * @uml.property  name="opnds1"
	 */
    public Set<List<OpndSpec>> getOpnds1() {
        return opnds1;
    }

    /**
	 * @return
	 * @uml.property  name="opnds2"
	 */
    public Set<List<OpndSpec>> getOpnds2() {
        return opnds2;
    }

    /**
	 * @return
	 * @uml.property  name="opnds3"
	 */
    public Set<List<OpndSpec>> getOpnds3() {
        return opnds3;
    }

    /**
	 * @return
	 * @uml.property  name="opnds4"
	 */
    public Set<List<OpndSpec>> getOpnds4() {
        return opnds4;
    }

    /**
	 * @param opnds1
	 * @uml.property  name="opnds1"
	 */
    public void setOpnds1(Set<List<OpndSpec>> opnds1) {
        this.opnds1 = opnds1;
    }

    /**
	 * @param opnds2
	 * @uml.property  name="opnds2"
	 */
    public void setOpnds2(Set<List<OpndSpec>> opnds2) {
        this.opnds2 = opnds2;
    }

    /**
	 * @param opnds3
	 * @uml.property  name="opnds3"
	 */
    public void setOpnds3(Set<List<OpndSpec>> opnds3) {
        this.opnds3 = opnds3;
    }

    /**
	 * @param opnds4
	 * @uml.property  name="opnds4"
	 */
    public void setOpnds4(Set<List<OpndSpec>> opnds4) {
        this.opnds4 = opnds4;
    }
}

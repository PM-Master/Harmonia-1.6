package gov.nist.csd.pm.server.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author  gavrila@nist.gov
 * @version  $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since  1.5
 */
public class CondSpec {
  /**
 * @uml.property  name="negated"
 */
boolean negated;
  /**
 * @uml.property  name="type"
 */
String type;
  /**
 * @uml.property  name="id"
 */
String id;
  /**
 * @uml.property  name="opnds1"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.OpndSpec"
 */
Set<List<OpndSpec>> opnds1;
  /**
 * @uml.property  name="opnds2"
 */
HashSet opnds2;
  /**
 * @uml.property  name="opnds3"
 */
HashSet opnds3;
  /**
 * @uml.property  name="opnds4"
 */
HashSet opnds4;

  public CondSpec(String id) {
    this.id = id;
    this.negated = false;
    this.type = null;
    opnds1 = new HashSet<List<OpndSpec>>();
    opnds2 = new HashSet();
    opnds3 = new HashSet();
    opnds4 = new HashSet();
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
 * @uml.property  name="id"
 */
public String getId() {
    return id;
  }

  /**
 * @return
 * @uml.property  name="negated"
 */
public boolean isNegated() {
    return negated;
  }
  
  /**
 * @param type
 * @uml.property  name="type"
 */
public void setType(String type) {
    this.type = type;
  }
  
  /**
 * @param neg
 * @uml.property  name="negated"
 */
public void setNegated(boolean neg) {
    this.negated = neg;
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
public HashSet getOpnds2() {
    return opnds2;
  }

  /**
 * @return
 * @uml.property  name="opnds3"
 */
public HashSet getOpnds3() {
    return opnds3;
  }

  /**
 * @return
 * @uml.property  name="opnds4"
 */
public HashSet getOpnds4() {
    return opnds4;
  }
}

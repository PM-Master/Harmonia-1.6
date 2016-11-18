package gov.nist.csd.pm.server.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * @author  gavrila@nist.gov
 * @version  $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since  1.5
 */
public class PatternSpec {
  /**
 * @uml.property  name="id"
 */
String id;
  /**
 * @uml.property  name="isActive"
 */
boolean isActive;
  /**
 * @uml.property  name="isAny"
 */
boolean isAny;
  /**
 * @uml.property  name="userSpecs"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.UserSpec"
 */
Set<UserSpec> userSpecs;
  /**
 * @uml.property  name="pcSpecs"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.PcSpec"
 */
Set<PcSpec> pcSpecs;
  /**
 * @uml.property  name="opSpecs"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.OpSpec"
 */
HashSet<OpSpec> opSpecs;
  /**
 * @uml.property  name="objSpecs"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.ObjSpec"
 */
HashSet<ObjSpec> objSpecs;// Has at most one entry.
  /**
 * @uml.property  name="contSpecs"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.ContSpec"
 */
HashSet<ContSpec> contSpecs;

  public PatternSpec(String id) {
    this.id = id;
    userSpecs = new HashSet<UserSpec>();
    pcSpecs = new HashSet<PcSpec>();
    opSpecs = new HashSet<OpSpec>();
    objSpecs = new HashSet<ObjSpec>();
    contSpecs = new HashSet<ContSpec>();
  }

  /**
 * @param active
 * @uml.property  name="isActive"
 */
public void setActive(boolean active) {
    isActive = active;
  }

  /**
 * @param any
 * @uml.property  name="isAny"
 */
public void setAny(boolean any) {
    isAny = any;
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
 * @uml.property  name="isActive"
 */
public boolean isActive() {
    return isActive;
  }

  /**
 * @return
 * @uml.property  name="isAny"
 */
public boolean isAny() {
    return isAny;
  }

  /**
 * @return
 * @uml.property  name="userSpecs"
 */
public Set<UserSpec> getUserSpecs() {
    return userSpecs;
  }

  /**
 * @return
 * @uml.property  name="pcSpecs"
 */
public Set<PcSpec> getPcSpecs() {
    return pcSpecs;
  }

  /**
 * @return
 * @uml.property  name="opSpecs"
 */
public HashSet<OpSpec> getOpSpecs() {
    return opSpecs;
  }

  /**
 * @return
 * @uml.property  name="objSpecs"
 */
public HashSet<ObjSpec> getObjSpecs() {
    return objSpecs;
  }

  /**
 * @return
 * @uml.property  name="contSpecs"
 */
public HashSet<ContSpec> getContSpecs() {
    return contSpecs;
  }
}

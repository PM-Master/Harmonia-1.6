package gov.nist.csd.pm.server.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  gavrila@nist.gov
 * @version  $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since  1.5
 */
public class OpndSpec {
  /**
 * @uml.property  name="type"
 */
String type;  // can be u (user), a (user attribute), o (object),
                // b (object attribute), p (policy class), c (base),
                // op (operation), os (operation set).
  /**
 * @uml.property  name="isSubgraph"
 */
boolean isSubgraph; // ignored except for represented objects.
  /**
 * @uml.property  name="isFunction"
 */
boolean isFunction; // true = function, false = name.
  /**
 * @uml.property  name="isComplement"
 */
boolean isComplement; // true = complement of a container.
  /**
 * @uml.property  name="id"
 */
String id;
  /**
 * @uml.property  name="origId"
 */
String origId;
  /**
 * @uml.property  name="origName"
 */
String origName;
  /**
 * @uml.property  name="children"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
 */
List<Integer> children;
  /**
 * @uml.property  name="parent"
 */
int parent;

  public OpndSpec(String id, String type, String origName, String origId) {
    this.id = id;
    this.type = type;
    this.origName = origName;
    this.origId = origId;
    children = new ArrayList<Integer>();
  }

  public OpndSpec(String id, String origName) {
    this.id = id;
    this.origName = origName;
    children = new ArrayList<Integer>();
  }

  /**
 * @param origId
 * @uml.property  name="origId"
 */
public void setOrigId(String origId) {
    this.origId = origId;
  }

  /**
 * @param origName
 * @uml.property  name="origName"
 */
public void setOrigName(String origName) {
    this.origName = origName;
  }

  /**
 * @param type
 * @uml.property  name="type"
 */
public void setType(String type) {
    this.type = type;
  }

  /**
 * @param b
 * @uml.property  name="isFunction"
 */
public void setIsFunction(boolean b) {
    this.isFunction = b;
  }

  /**
 * @param b
 * @uml.property  name="isSubgraph"
 */
public void setIsSubgraph(boolean b) {
    this.isSubgraph = b;
  }

  /**
 * @param b
 * @uml.property  name="isComplement"
 */
public void setIsComplement(boolean b) {
    this.isComplement = b;
  }

  /**
 * @param parent
 * @uml.property  name="parent"
 */
public void setParent(int parent) {
    this.parent = parent;
  }

  public void addChild(int n) {
    children.add(Integer.valueOf(n));
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
 * @uml.property  name="isFunction"
 */
public boolean isFunction() {
    return isFunction;
  }

  /**
 * @return
 * @uml.property  name="isSubgraph"
 */
public boolean isSubgraph() {
    return isSubgraph;
  }

  /**
 * @return
 * @uml.property  name="isComplement"
 */
public boolean isComplement() {
    return isComplement;
  }

  /**
 * @return
 * @uml.property  name="origId"
 */
public String getOrigId() {
    return origId;
  }

  /**
 * @return
 * @uml.property  name="origName"
 */
public String getOrigName() {
    return origName;
  }

  /**
 * @return
 * @uml.property  name="parent"
 */
public int getParent() {
    return parent;
  }

  /**
 * @return
 * @uml.property  name="children"
 */
public List<Integer> getChildren() {
    return children;
  }

  @Override
  public String toString(){
      return new StringBuilder()
              .append("OpndSpec").append("\n")
              .append("\tid: ").append(this.id).append("\n")
              .append("\tType: ").append(this.type).append("\n")
              .append("\tOrig Name: ").append(origName).append("\n")
              .append("\tOrig Id: ").append(origId).append("\n")
              .append("\tParent: ").append(parent).append("\n")
              .append("\tChildren: ").append(children).append("\n")
              .toString();
  }

}

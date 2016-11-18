package gov.nist.csd.pm.server.parser;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class ObjSpec {
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

  public ObjSpec(String name, String type) {
    this.name = name;
    this.type = type;
  }

  /**
 * @param id
 * @uml.property  name="id"
 */
public void setId(String id) {
    this.id = id;
  }

  /**
 * @param type
 * @uml.property  name="type"
 */
public void setType(String type) {
    this.type = type;
  }
  
  /**
 * @return
 * @uml.property  name="name"
 */
public String getName() {
    return name;
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
}
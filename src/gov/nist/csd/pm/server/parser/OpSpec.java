package gov.nist.csd.pm.server.parser;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class OpSpec {
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

  public OpSpec(String name, String type) {
    this.name = name;
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
}

package gov.nist.csd.pm.user;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/10/12 14:00:00 $
 * @since 1.5
 */

public class PmProcess {
  /**
 * @uml.property  name="sProcessId"
 */
private String sProcessId = null;
  /**
 * @uml.property  name="sSessionId"
 */
private String sSessionId = null;

  public PmProcess(String sProcessId, String sSessId) {
    this.sSessionId = sSessId;
    this.sProcessId = sProcessId;
  }

}

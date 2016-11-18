package gov.nist.csd.pm.common.browser;

import gov.nist.csd.pm.common.application.PolicyMachineClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.util.Log;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/18/11 Time: 12:57 PM To
 * change this template use File | Settings | File Templates.
 */
public class PmNodeChildDelegate implements
		PmNode.ChildProvidingDelegate<PmNode> {
	Log log = new Log(Log.Level.INFO, true);

	/**
	 * @uml.property name="_client"
	 * @uml.associationEnd
	 */
	private PolicyMachineClient _client;
	/**
	 * @uml.property name="_sessionId"
	 */
	private String _sessionId;
	/**
	 * @uml.property name="_direction"
	 * @uml.associationEnd
	 */
	private PmGraphDirection _direction;
	/**
	 * @uml.property name="_type"
	 * @uml.associationEnd
	 */
	private PmGraphType _type;
	

	public PmNodeChildDelegate(SysCaller sysCaller, PmGraphDirection dir,
			PmGraphType type) {
		this(sysCaller.getSocketClient(), sysCaller.getSessionId(), dir, type);
        log.debug("TRACE 14 - In PmNodeChildDelegate constructor");

	}

	public PmNodeChildDelegate(PolicyMachineClient client, String sessionId,
			PmGraphDirection dir, PmGraphType type) {
        log.debug("TRACE 14* - In PmNodeChildDelegate constructor:\n"
        		+ "-dir: " + dir + "\n"
        		+ "-type: " + type);
		_client = checkNotNull(client);
		_direction = checkNotNull(dir);
		_type = checkNotNull(type);
		_sessionId = checkNotNull(sessionId);
	}

	public PmGraphDirection getDirection() {
		return _direction;
	}

	public void setDirection(PmGraphDirection direction) {
		_direction = direction;
	}

	public PmGraphType getType() {
		return _type;
	}

	public void setType(PmGraphType type) {
		_type = type;
	}

	@Override
	public List<PmNode> getChildrenOf(PmNode parent) {
        log.debug("TRACE 15 - In PmNodeChildDelegate.getChildrenOf()");
		return _type.getChildrenOf(parent, _direction, _client, _sessionId);
	}

}

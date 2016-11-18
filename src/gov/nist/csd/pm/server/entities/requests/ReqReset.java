package gov.nist.csd.pm.server.entities.requests;


public class ReqReset extends ReqBase {
	
	public ReqReset(String clientId, String sessId,
			String procId, String userId)
	{
		super(clientId, sessId, procId, userId);
	}
}

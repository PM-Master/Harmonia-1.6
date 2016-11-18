package gov.nist.csd.pm.server.entities.requests;

public class ReqGetOperationId extends ReqBase{
	private String opName;
	
	public ReqGetOperationId(String clientId, String sessId, 
			String procId, String userId, String opName) {
		super(clientId, sessId, procId, userId);
		setOpName(opName);
	}

	public String getOpName() {
		return opName;
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}
}

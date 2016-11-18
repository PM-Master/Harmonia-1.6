package gov.nist.csd.pm.server.entities.responses;

public class ResGetOperationId extends ResBase{
	private String opId;
	
	public ResGetOperationId(String clientId, String sessId, 
			String procId, String userId, String opId){
		super(clientId, sessId, procId, userId);
		setOpId(opId);
	}

	public String getOpId() {
		return opId;
	}

	public void setOpId(String opId) {
		this.opId = opId;
	}
}

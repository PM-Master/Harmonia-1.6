package gov.nist.csd.pm.server.entities.requests;


public class ReqBase {

	private String clientId;
	private String sessId;
	private String procId;
	private String userId;

	public String getSessId() {
		return sessId;
	}

	public void setSessId(String sessId) {
		this.sessId = sessId;
	}

	public String getProcId() {
		return procId;
	}

	public void setProcId(String procId) {
		this.procId = procId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public ReqBase(String clientId, String sessId,
			String procId, String userId){
		this.clientId = clientId;
		this.sessId = sessId;
		this.procId = procId;
		this.userId = userId;
	}
	
}

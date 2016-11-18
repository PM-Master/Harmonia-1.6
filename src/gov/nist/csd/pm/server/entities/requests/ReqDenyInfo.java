package gov.nist.csd.pm.server.entities.requests;

public class ReqDenyInfo extends ReqBase{
	private Integer denyId;
	public ReqDenyInfo(String clientId, String sessId, 
			String procId, String userId, Integer denyId) {
		super(clientId, sessId, procId, userId);
		setDenyId(denyId);
	}
	public Integer getDenyId() {
		return denyId;
	}
	public void setDenyId(Integer denyId) {
		this.denyId = denyId;
	}
}

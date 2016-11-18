package gov.nist.csd.pm.server.entities.responses;

public class ResGetEntityId extends ResBase{
	private Integer entityId;
	public ResGetEntityId(String clientId, String sessId, 
			String procId, String userId, Integer id) {
		super(clientId, sessId, procId, userId);
		setEntityId(id);
	}
	public Integer getEntityId() {
		return entityId;
	}
	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}
	public String toString(){
		return entityId + "";
	}
}

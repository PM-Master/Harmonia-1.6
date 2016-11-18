package gov.nist.csd.pm.server.entities.responses;

public class ResGetEntityName extends ResBase{
	private String entityName;
	public ResGetEntityName(String clientId, String sessId, 
			String procId, String userId, String name) {
		super(clientId, sessId, procId, userId);
		setEntityName(name);
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String toString(){
		return entityName;
	}
}

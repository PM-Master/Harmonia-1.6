package gov.nist.csd.pm.server.entities.requests;

public class ReqGetEntityId extends ReqBase{
	private String name;
	private String type;
	
	public ReqGetEntityId(String clientId, String sessId,
			String procId, String userId, String name, String type){
		super(clientId, sessId, procId, userId);
		setName(name);
		setType(type);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}

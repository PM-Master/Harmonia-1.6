package gov.nist.csd.pm.server.entities.requests;

public class ReqGetEntityName extends ReqBase{
	private Integer id;
	
	public ReqGetEntityName(String clientId, String sessId,
			String procId, String userId, Integer id){
		super(clientId, sessId, procId, userId);
		setId(id);
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
}

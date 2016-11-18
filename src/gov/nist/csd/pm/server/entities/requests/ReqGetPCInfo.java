package gov.nist.csd.pm.server.entities.requests;

public class ReqGetPCInfo extends ReqBase {
	private String PCId; 
	
	public String getPCId() {
		return PCId;
	}

	public void setPCId(String pCId) {
		PCId = pCId;
	}

	public String getIsVos() {
		return isVos;
	}

	public void setIsVos(String isVos) {
		this.isVos = isVos;
	}

	private String isVos;
	
	public ReqGetPCInfo(String clientId, String sessId,
			String procId, String userId, String PCId,
			String isVos)
	{
		super(clientId, sessId, procId, userId);
		setPCId(PCId);
		setIsVos(isVos);
	}
}

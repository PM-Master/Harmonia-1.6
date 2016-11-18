package gov.nist.csd.pm.server.entities.requests;

public class ReqGetAttrInfo extends ReqBase{
	private Integer attrId;
	private boolean isVos;
	
	public ReqGetAttrInfo(String clientId, String sessId, 
			String procId, String userId, Integer attrId, boolean isVos) {
		super(clientId, sessId, procId, userId);
		setAttrId(attrId);
		setIsVos(isVos);
	}

	public Integer getAttrId() {
		return attrId;
	}

	public void setAttrId(Integer attrId) {
		this.attrId = attrId;
	}

	public boolean getIsVos() {
		return isVos;
	}

	public void setIsVos(boolean isVos) {
		this.isVos = isVos;
	}

}

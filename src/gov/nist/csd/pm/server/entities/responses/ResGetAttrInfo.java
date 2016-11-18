package gov.nist.csd.pm.server.entities.responses;

import java.util.List;

public class ResGetAttrInfo extends ResBase{
	List<String> info;
	public ResGetAttrInfo(String clientId, String sessId, 
			String procId, String userId, List<String> info) {
		super(clientId, sessId, procId, userId);
		setInfo(info);
	}
	public List<String> getInfo() {
		return info;
	}
	public void setInfo(List<String> info) {
		this.info = info;
	}
}

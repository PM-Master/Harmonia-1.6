package gov.nist.csd.pm.server.entities.responses;

import java.util.List;

public class ResGetDenyInfo extends ResBase{
	private List<String> info;
	public ResGetDenyInfo(String clientId, String sessId, 
			String procId, String userId, List<String> info) {
		super(clientId, sessId, procId, userId);
	}
	public List<String> getInfo() {
		return info;
	}
	public void setInfo(List<String> info) {
		this.info = info;
	}
}

package gov.nist.csd.pm.server.entities.responses;

import java.util.List;

public class ResGetAllOps extends ResBase{
	private List<String> allOps;
	public ResGetAllOps(String clientId, String sessId, 
			String procId, String userId, List<String> allOps) {
		super(clientId, sessId, procId, userId);
	}
	public List<String> getAllOps() {
		return allOps;
	}
	public void setAllOps(List<String> allOps) {
		this.allOps = allOps;
	}
}

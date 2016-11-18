package gov.nist.csd.pm.server.entities.responses;

import java.util.List;

public class ResGetOattrs extends ResBase{
	private List<String> oattrs;
	public ResGetOattrs(String clientId, String sessId, 
			String procId, String userId, List<String> oattrs) {
		super(clientId, sessId, procId, userId);
	}
	public List<String> getOattrs() {
		return oattrs;
	}
	public void setOattrs(List<String> oattrs) {
		this.oattrs = oattrs;
	}
}

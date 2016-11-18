package gov.nist.csd.pm.server.entities.requests;

public class ReqDeleteAssignment extends ReqBase{
	private int start;
	private int end;
	
	public ReqDeleteAssignment(String clientId, String sessId,
			String procId, String userId, int aStart, int aEnd){
		super(clientId, sessId, procId, userId);
		start = aStart;
		end = aEnd;
	}
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}
}

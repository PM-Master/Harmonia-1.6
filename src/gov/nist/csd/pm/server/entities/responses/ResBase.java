package gov.nist.csd.pm.server.entities.responses;

import java.util.ArrayList;
import java.util.List;


public class ResBase {

	private String sessId;
	private String clientId;
	private String procId;
	private String userId;

	List<NameValuePair> response = new ArrayList<NameValuePair>();

	public List<NameValuePair> getResponse() {
		return response;
	}
	
	public void setResponse(List<NameValuePair> response) {
		this.response = response;
	}

	public class NameValuePair {
	    private String name;
	    private String value;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public String getprocId() {
		return procId;
	}

	public void setprocId(String procId) {
		this.procId = procId;
	}

	public String getsessId() {
		return sessId;
	}
	public void setsessId(String sessId) {
		this.sessId = sessId;
	}
	public String getclientId() {
		return clientId;
	}
	public void setclientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getuserId() {
		return userId;
	}

	public void setuserId(String userId) {
		this.userId = userId;
	}
	
	public ResBase(String clientId, String sessId, 
			String procId, String userId){//,List<NameValuePair> response) {
		setclientId(clientId);
		setsessId(sessId);
		setprocId(procId);
		setuserId(userId);
		//setResponse(response);
	}

	
}

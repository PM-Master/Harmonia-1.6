package gov.nist.csd.pm.server.entities.requests;

public class ReqAddHostApp extends ReqBase{
	private String sessId;
	private String host;
	private String appName;
	private String appPath;
	private String mainClassName;
	private String appPrefix;
	
	public ReqAddHostApp(String clientId, String sessId,
			String procId, String userId, String host, String appName,
			String appPath, String mainClassName, String appPrefix){
		super(clientId, sessId, procId, userId);
		setSessId(sessId);
		setHost(host);
		setAppName(appName);
		setAppPath(appPath);
		setMainClassName(mainClassName);
		setAppPrefix(appPrefix);
	}
	public String getSessId() {
		return sessId;
	}
	public void setSessId(String sessId) {
		this.sessId = sessId;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getAppPath() {
		return appPath;
	}
	public void setAppPath(String appPath) {
		this.appPath = appPath;
	}
	public String getMainClassName() {
		return mainClassName;
	}
	public void setMainClassName(String mainClassName) {
		this.mainClassName = mainClassName;
	}
	public String getAppPrefix() {
		return appPrefix;
	}
	public void setAppPrefix(String appPrefix) {
		this.appPrefix = appPrefix;
	}
}

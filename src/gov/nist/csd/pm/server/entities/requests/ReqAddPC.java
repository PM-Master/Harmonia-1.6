package gov.nist.csd.pm.server.entities.requests;

public class ReqAddPC extends ReqBase {
	private String name; 
	private String descr; 
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescr() {
		return descr;
	}
	public void setDescr(String descr) {
		this.descr = descr;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String[] getProps() {
		return props;
	}
	public void setProps(String[] props) {
		this.props = props;
	}
	private String info;
	private String[] props;
	public ReqAddPC(String clientId, String sessId,
			String procId, String userId, String name, 
			String descr, String info, String[] props)
	{
		super(clientId, sessId, procId, userId);
		setName(name);
		setDescr(descr);
		setInfo(info);
		setProps(props);
	}
}


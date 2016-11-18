package gov.nist.csd.pm.application.schema.tableeditor;

import java.util.List;

public class Template{
	private String tplName;
	private String tplId;
	private List<String> conts;
	private List<String> keys;

	public Template(String tplName, String tplId, List<String> conts, List<String> keys) {
		super();
		this.tplName = tplName;
		this.tplId = tplId;
		this.conts = conts;
		this.keys = keys;
	}
	public String getTplName() {
		return tplName;
	}
	public void setTplName(String tplName) {
		this.tplName = tplName;
	}
	public String getTplId() {
		return tplId;
	}
	public void setTplId(String tplId) {
		this.tplId = tplId;
	}
	public List<String> getConts() {
		return conts;
	}
	public void setConts(List<String> conts) {
		this.conts = conts;
	}
	public List<String> getKeys() {
		return keys;
	}
	public void setKeys(List<String> keys) {
		this.keys = keys;
	}
	public String toString(){
		return "name: " + tplName + "\nid: " + tplId + "\nconts: " + conts + "\nkeys: " + keys;
	}
}

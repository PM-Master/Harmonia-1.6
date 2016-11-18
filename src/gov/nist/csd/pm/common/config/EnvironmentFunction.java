package gov.nist.csd.pm.common.config;

public class EnvironmentFunction {
	
	public EnvironmentFunction(String sName, String sType, String sParamTypes) {
		this.sName = sName;
		this.sType = sType;
		this.sParamTypes = sParamTypes;
	}

	/**
	 * @uml.property  name="sName"
	 */
	private String sName;
	/**
	 * @uml.property  name="sType"
	 */
	private String sType;
	/**
	 * @uml.property  name="sParamTypes"
	 */
	private String sParamTypes;


	public String getName() {
		return sName;
	}

	public String getType() {
		return sType;
	}

	public String getParamTypes() {
		return sParamTypes;
	}
}


package gov.nist.csd.pm.common.constants;

public enum ParamType{
	INT("int"),
	STRING("string"),
	BOOLEAN("boolean");

	private String name;

	ParamType(String n){
		name = n;
	}

	public String getName(){
		return name;
	}
}
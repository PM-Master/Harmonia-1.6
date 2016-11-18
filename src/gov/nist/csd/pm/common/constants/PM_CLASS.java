package gov.nist.csd.pm.common.constants;

public enum PM_CLASS {
	
	 CLASS("class"				,8),
	 FILE("File"				,9),
	 DIR("Directory"			,10),
	 USER("User"				,11),
	 UATTR("User attribute"		,12),
	 OBJ("Object"				,13),
	 OATTR("Object attribute"	,14),
	 CONNECTOR("Connector"		,15),
	 POLICY("Policy class"		,16),
	 OPSET("Operation set"		,17),
	 ANY("*"					,18),
	 CLIPBOARD("Clipboard"		,19),
	 RECORD("Record"			,20),
	 SESSION("Session"			,21);
	
 public String name;
 public int id;
 private  PM_CLASS(int theId, String theName ){
	 id = theId;
	 name = theName;
 }
 private  PM_CLASS(String theName, int theId ){
	 id = theId;
	 name = theName;
 } 	
}

package gov.nist.csd.pm.common.constants;

import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;

public enum PM_NODE {
	USER		("u"),		
	USERA		( "U"),
	AUATTR		( "aa"),// active user attribute
	UATTR		( "a"),
	UATTRA		( "A"),
	POL			( "p"),
	POLA		( "P"), // Found 0 entries!!!
	OATTR		( "b"),
	OATTRA		( "B"),
	ASSOC		( "o"),
	ASSOCA		( "O"),
	OPSET		( "s"),
	OPSETA		( "S"), // 0 Entries found!!!
	CONN		( "c"),
	CONNA		( "C"), // 0 Entries Found!!!
	M_PREFIX	( "m"),
	// A user attribute used in INTRASESSION deny constraints.
	INTRA		( "ai");//,
	/*S_ASSOC		("so"),
	S_OATTR 	("sb"),
	S_POL 		("sp");*/

	
	public String value;
	PM_NODE(String theCode){
		value = theCode;
	}

}

package gov.nist.csd.pm.common.constants;

public enum PacketCommands4PM {
	setTableModifying(1),
	connect(0);
	
	int code;
	PacketCommands4PM(int theCode){
		code = theCode;
	}
}

package gov.nist.csd.pm.server.packet;

import gov.nist.csd.pm.common.net.Packet;

public class PacketParser {

	/// Definitely not void, it should be the base 
	/// of the command and parameters, all of which 
	/// should be the vocabulary objects the 
	/// PM Engine talks and operates on
	public static void PaseCommandPacket(Packet cmdPacket){
		String sCmdCode = cmdPacket.getStringValue(0);
		/// TODO: make it conditional on debugging flag
		System.out.println(sCmdCode);
		System.out.println("PM Engine Dispatch " + sCmdCode);
		
		
	}
	
}

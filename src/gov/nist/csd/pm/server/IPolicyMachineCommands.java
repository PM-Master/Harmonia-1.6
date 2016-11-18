package gov.nist.csd.pm.server;

public interface IPolicyMachineCommands {
	void Connect(String clientID);
	void Reset(String clientId, String sessionId);
	//void getGraph(String clientId, String sGraphType, String sAnchorId, String sAnchorLabel, String sAnchorType, String sLevel);
}

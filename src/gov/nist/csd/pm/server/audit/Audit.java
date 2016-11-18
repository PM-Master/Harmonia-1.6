package gov.nist.csd.pm.server.audit;

import gov.nist.csd.pm.common.constants.MySQL_Parameters;
import gov.nist.csd.pm.common.constants.MySQL_Statements;
import gov.nist.csd.pm.common.constants.ParamType;

import java.sql.Connection;
import java.util.ArrayList;

public class Audit {

    public static final String PM_NODE_USER = "u";
    static Connection connection = null;
	static String dbName = "PM_Audit";
	static ArrayList<Object> inputStmt = new ArrayList<Object>();

	public static void setAuditInfo(String sSessId, String sHost,
			String sUser, String sUserId, String sAction, boolean sResult, String sDesc, String sObjName, String sObjId) throws Exception {
			// Gopi need to revisit getConnection call
			
			java.sql.Timestamp timeDate = new java.sql.Timestamp(new java.util.Date().getTime());
		    
			MySQL_Parameters params = new MySQL_Parameters();
			params.addParam(ParamType.INT, Integer.valueOf(sSessId));
			params.addParam(ParamType.STRING, sUserId);
			params.addParam(ParamType.STRING, sUser);
			params.addParam(ParamType.STRING, sHost);
			params.addParam(ParamType.STRING, timeDate.toString());
			params.addParam(ParamType.STRING, sAction);
			params.addParam(ParamType.STRING, sResult);
			params.addParam(ParamType.STRING, sDesc);
			params.addParam(ParamType.STRING, sObjId);
			params.addParam(ParamType.STRING, sObjName);
			System.err.println(new Throwable().getStackTrace()[0].getLineNumber());
			MySQL_Statements.insert(MySQL_Statements.INSERT_AUDIT_RECORD, params);
	}
	public static ArrayList<ArrayList<Object>> getAuditInfo(String query) throws Exception {
			MySQL_Parameters params = new MySQL_Parameters();
			params.addParam(ParamType.STRING, query);
			ArrayList<ArrayList<Object>> auditInfo = MySQL_Statements.select(query, params);
			return auditInfo;
	}
	
}


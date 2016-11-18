package gov.nist.csd.pm.server.dao.MySQL;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.lang.reflect.Method;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.MySQL_Functions;
import gov.nist.csd.pm.common.constants.MySQL_Statements;
import gov.nist.csd.pm.common.constants.MySQL_StoredProcedures;
import gov.nist.csd.pm.server.entities.requests.ReqCreateAssignment;
import gov.nist.csd.pm.server.entities.requests.ReqDeleteAssignment;
import gov.nist.csd.pm.server.entities.requests.ReqGetEntityId;
import gov.nist.csd.pm.server.entities.requests.ReqGetEntityName;
import gov.nist.csd.pm.server.entities.requests.ReqReset;
import gov.nist.csd.pm.server.entities.responses.ResBase;
import gov.nist.csd.pm.server.entities.responses.ResGetEntityId;
import gov.nist.csd.pm.server.entities.responses.ResGetEntityName;
import gov.nist.csd.pm.sql.PmDatabase;

/**
 * TODO
 * reset
 * error handling
 * 
 * getIdWNameAndType response will use getEntityId response
 * @author Administrator
 *
 */
public class CommonDAO {
	
	public static void main(String[] args){
		ReqGetEntityId req = new ReqGetEntityId(null, null, null, null, "PM", null);
		CommonDAO dao = new CommonDAO();
		//System.out.println(dao.getEntityId(req));
	}

	// First empty almost all containers. Exceptions:
	// The HostContainer, the NameContainer.
	// Also, do not delete the current session in which the Admin Tool is
	// executing.
//	public void reset(ReqReset requestReset) throws Exception  {
//		try {
//			MySQL_StoredProcedures.reset_data();
//			return;
//		} catch (Exception e) {
//			if (ServerConfig.debugFlag) {
//				e.printStackTrace();
//			}
//			throw new Exception("Exception when emptying the database: ");
//		}
//	}
//
//	public ResBase getEntityId(ReqGetEntityId requestGetEntityId){
//		String name = requestGetEntityId.getName();
//		Integer id = MySQL_Functions.get_node_id(name);
//		return new ResGetEntityId(requestGetEntityId.getClientId(), requestGetEntityId.getSessId(), 
//				requestGetEntityId.getProcId(), requestGetEntityId.getUserId(), id);
//	}
//
//	public ResBase getEntityName(ReqGetEntityName nameRequest){
//		int id = nameRequest.getId();
//		String name = MySQL_Functions.get_node_name(id);
//		return new ResGetEntityName(nameRequest.getClientId(), nameRequest.getSessId(), 
//				nameRequest.getProcId(), nameRequest.getUserId(), name);
//	}
//	
}

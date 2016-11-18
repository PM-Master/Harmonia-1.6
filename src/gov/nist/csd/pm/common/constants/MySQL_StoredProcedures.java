package gov.nist.csd.pm.common.constants;

import gov.nist.csd.pm.common.config.ServerConfig;

import java.sql.*;
import java.util.ArrayList;

public class MySQL_StoredProcedures {

	public static final String CREATE_ASSIGNMENT 	= "{call create_assignment(?,?)}";
	public static final String DELETE_ASSIGNMENT 	= "{call delete_assignment(?,?)}";
	public static final String CREATE_ASSOCIATION	= "{call create_association(?,?,?,?)}";
	public static final String CREATE_HOST			= "{call create_host(?,?)}";
	public static final String CREATE_OBJECT_CLASS	= "{call create_object_class(?,?,?)}";
	public static final String CREATE_OBJECT_DETAIL = "{call create_object_detail(?,?,?,?,?,?,?)}";
	public static final String CREATE_OPERATION		= "{call create_operation(?,?)}";
	public static final String CREATE_OPSET			= "{call create_opset(?,?)}";
	public static final String CREATE_OPSET_DETAIL 	= "{call create_opset_detail(?,?)}";
	public static final String RESET_DATA			= "{call reset_data(?)}";
	public static final String SET_PROPERTY			= "{call set_property(?,?,?)}";
	public static final String DELETE_PROPERTY_SP	= "{call delete_property(?)}";
	public static final String GET_DENIED_OPS		= "{call get_denied_ops(?,?,?)}"; 
	public static final String DELETE_DENY_SP		= "{call delete_deny(?)}";
	public static final String CREATE_RECORD 		= "{call create_record(?,?,?,?)}";
	public static final String CREATE_RECORD_KEYS 	= "{call create_record_keys(?,?)}";
	public static final String CREATE_RECORD_COMPS	= "{call create_record_components(?,?)}";
	public static final String SET_APP_PATH 		= "{call set_app_path(?,?,?,?,?)}";

	private static Connection conn;

	public static ArrayList<ArrayList<Object>> executeStoredProcedure(String sql, Object ... args) throws Exception{
		return executeStoredProcedure(sql, params(args));
	}

	private static MySQL_Parameters params(Object ... args){
		MySQL_Parameters params = new MySQL_Parameters();
		for(int i = 0; i < args.length; i++){
			Object arg = args[i];
			if(arg instanceof String){
				params.addParam(ParamType.STRING, arg);
			}else if(arg instanceof Integer){
				params.addParam(ParamType.INT, arg);
			}else if(arg instanceof Boolean){
				params.addParam(ParamType.BOOLEAN, arg);
			}
		}
		return params;
	}

	/**
	 * Method to prepare a sql statement
	 * @param sql the sql statement to be executed
	 * @param params a 2D array of parameters and their data types (each row will be a value, type pair: [value, type]).
	 */
	public static ArrayList<ArrayList<Object>> executeStoredProcedure(
			String sql, MySQL_Parameters params) throws Exception {
		conn = ServerConfig.getPmDB().getConnection();
		//System.out.println("in executeStoredProcedure");
		ArrayList<ArrayList<Object>> returned = new ArrayList<ArrayList<Object>>();
		Statement stmt = conn.createStatement();
		stmt.executeUpdate("use " + GlobalConstants.PM_SCHEMA);
		CallableStatement cs = conn.prepareCall(sql);

		if (cs == null) {
			//System.out.println("CallableStatement is null");
			throw new SQLException("CallableStatement is null");
		}
		// Set parameters
		try {
			if (params != null) {
				for (int i = 0; i < params.getParams().size(); i++) {
					String type = params.getParams().get(i)[0];
					String value = params.getParams().get(i)[1];
					//System.out.println("Value is " + value);
					if (type.equals(ParamType.INT.getName())) {
						//System.out.println("Integer Value is " + value);
						if (value == null) {
							cs.setNull(i + 1, java.sql.Types.INTEGER);
						} else
							cs.setInt(i + 1, Integer.parseInt(value));
					} else if (type.equals(ParamType.STRING.getName())) {
						//System.out.println("String Value is " + value);
						if (value == null) {
							cs.setNull(i + 1, java.sql.Types.VARCHAR);
						} else {
							cs.setString(i + 1, value);
						}
					} else if (type.equals(ParamType.BOOLEAN.getName())) {
						//System.out.println("Boolean Value is " + value);
						if (value == null) {
							cs.setNull(i + 1, java.sql.Types.BOOLEAN);
						} else {
							cs.setBoolean(i + 1, Boolean.parseBoolean(value));
						}
					}
				}
			} else{}
				//System.out.println(" params null");

			boolean hasResults = false;
			//System.out.println("Executing Stored Proc");
			hasResults = cs.execute();
			//System.out.println("Out from Executing Stored Proc");

			if (hasResults) {
				//System.out.println("has Results!");
				ResultSet rs = cs.getResultSet();
				if (rs != null) {
					//System.out.println("Result set is not null");
					ResultSetMetaData meta = rs.getMetaData();
					int numCols = meta.getColumnCount();
					while (rs.next()) {
						ArrayList<Object> row = new ArrayList<Object>();
						for (int i = 0; i < numCols; i++) {
							row.add(rs.getObject(i + 1));
						}
						returned.add(row);
					}
				} else{}
					//System.out.println("Result set is null");
			}
			return returned;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Exception while executing Stored Procedure: " + cs.toString());
		}finally{
			conn.close();
		}
	}
}

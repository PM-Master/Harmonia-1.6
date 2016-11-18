package gov.nist.csd.pm.common.constants;

import gov.nist.csd.pm.common.config.ServerConfig;

import java.sql.*;

public class MySQL_Functions {
	public static final String GET_ID_OF_ENTITY_WITH_NAME_FUN 	= "{? = call get_node_id(?,?)}";
	public static final String GET_NAME_OF_ENTITY_WITH_ID 		= "{? = call get_node_name(?)}";
	public static final String GET_HOST_ID_FN 					= "{? = call get_host_id(?)}";
	public static final String GET_OPERATION_ID					= "{? = call get_operation_id(?)}";
	public static final String CREATE_NODE_FUN					= "{? = call create_node_fun(?,?,?,?)}";
	public static final String CREATE_USER_FUN					= "{? = call create_user_detail_fun(?,?,?,?,?,?,?)}";
	public static final String CREATE_DENY						= "{? = call create_deny(?,?,?,?,?,?,?)}";
	public static final String IS_ACCESSIBLE					= "{? = call is_accessible(?,?)}";
	public static final String ADD_PATH							= "{? = call add_path(?)}";
	public static final String CREATE_SCRIPT_RECORD_FUN			= "{? = call add_script(?)}";
	public static final String CREATE_OB_USER_SPEC				= "{? = call create_ob_user_spec(?,?,?)}";
    public static final String CREATE_OB_PC_SPEC				= "{? = call create_ob_pc_spec(?,?,?)}";
    public static final String CREATE_OB_OP_SPEC				= "{? = call create_ob_op_spec(?,?,?)}";
    public static final String CREATE_OB_OBJ_SPEC				= "{? = call create_ob_obj_spec(?,?,?)}";
    public static final String CREATE_OB_CONT_SPEC				= "{? = call create_ob_cont_spec(?,?,?)}";
	public static final String CREATE_OPERAND					= "{? = call create_operand(?,?,?,?,?,?,?,?,?)}";



	private static ParamType paramType;
	
	private static Connection conn;

	public static void registerOutParamType(ParamType p){
		paramType = p;
	}

	public static Object executeFunction(String sql, ParamType outParamType, Object ... args) throws Exception{
        return executeFunction(sql, params(outParamType, args));
    }

    private static MySQL_Parameters params(ParamType outParamType, Object ... args){
        MySQL_Parameters params = new MySQL_Parameters();
        params.setOutParamType(outParamType);
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
	 * Method to prepare a function
	 * @param sql the sql statement to be executed
	 * @param params a 2D array of parameters and their data types (each row will be a value, type pair: [value, type]).
	 */
	public static Object executeFunction(String sql, MySQL_Parameters params)
			throws Exception {
		conn = ServerConfig.getPmDB().getConnection();
		Statement stmt = null;
		CallableStatement cs = null;
		cs = conn.prepareCall(sql);
		stmt = conn.createStatement();
		stmt.executeUpdate("use " + GlobalConstants.PM_SCHEMA);
		stmt.executeUpdate("SET SQL_SAFE_UPDATES = 0;");
		try{
			if (params != null) {
				String type = params.getOutParamType().getName();
				if (type.equals(ParamType.INT.getName())) {
					cs.registerOutParameter(1, Types.INTEGER);
				} else if (type.equals(ParamType.STRING.getName())) {
					cs.registerOutParameter(1, Types.VARCHAR);
				} else if (type.equals(ParamType.BOOLEAN.getName())) {
					cs.registerOutParameter(1, Types.BOOLEAN);
				}

				for (int i = 0; i < params.getParams().size(); i++) {
					type = params.getParams().get(i)[0];
					String value = params.getParams().get(i)[1];
					if (type.equals(ParamType.INT.getName())) {
						if (value == null) {
							cs.setNull(i + 2, Types.INTEGER);
						} else {
							cs.setInt(i + 2, Integer.parseInt(value));
						}

					} else if (type.equals(ParamType.STRING.getName())) {
						cs.setString(i + 2, value);
					} else if (type.equals(ParamType.BOOLEAN.getName())) {
						cs.setBoolean(i + 2, Boolean.parseBoolean(value));
					}
				}
				cs.execute();
			}

			Object ret = cs.getObject(1);
			conn.close();
			//System.out.println("Returned object is " + ret);
			return ret;
        }catch(Exception e){
            System.out.println("exception in function: " + e.getMessage());
            throw new Exception("Exception in Function: " + e.getMessage());
		}finally{
			conn.close();
		}
	}
}

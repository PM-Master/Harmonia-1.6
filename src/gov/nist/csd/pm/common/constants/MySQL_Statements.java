package gov.nist.csd.pm.common.constants;

import gov.nist.csd.pm.common.config.ServerConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

public class MySQL_Statements {
	/**
	 * obligations
	 */
    public static final String DELETE_ASSIGNMENT_BY_PATH = "delete from assignment where start_node_id=?,end_node_id=?,assignment_path_id=?";
    public static final String UPDATE_ASSIGNMENT_PATH_ID = "update assignment set assignment_path_id=? where assignment_id=?";
    public static final String GET_ASSIGNMENT_PATH_ID = "select assignment_path_id from assignment where assignment_id=?";
    public static final String GET_CONDITION_INFO = "select get_cond_type_name(type), is_negated from ob_condition where condition_id=?";
    public static final String GET_CONDITION_OPERANDS = "select operand_id from ob_operand where condition_id=? order by sequence";
    public static final String DELETE_CONDITION = "delete from ob_condition where condition_id=?";
    public static final String WRITE_COND_OPERAND_RECORD = "insert into ob_operand (operand_id,operand_type,operand_num,sequence,is_function,is_subgraph,is_compliment,expression,expression_id,condition_id) values (?,get_operand_type_id(?),?,?,?,?,?,?,?,?)";
    public static final String UPDATE_COND_OPERAND = "update ob_operand set operand_type=get_operand_type_id(?), operand_num=?, sequence=?, is_function=?, is_subgraph=?, is_compliment=?, expression=?, expression_id=?, condition_id=? where operand_id=?";
	public static final String WRITE_ACTION_COND_RECORD = "insert into ob_condition (condition_id, action_id, cond_type, is_negated) values (?,?,get_cond_type_id(?),?)";
    public static final String DELETE_OPERAND = "delete from ob_operand where operand_id=?";
    public static final String DELETE_ACTION = "delete from ob_action where action_id=?";
    public static final String DELETE_EVENT_PATTERN = "delete from ob_event_pattern where event_pattern_id=?";
    public static final String DELETE_RULE = "delete from ob_rule where rule_id=?";
    public static final String DELETE_SCRIPT_SOURCE = "delete from ob_script_source where script_id=?";
    public static final String GET_SCRIPT_SOURCE = "select source from ob_script_source where script_id=? order by ob_script_source.order";
    public static final String ADD_LINE_TO_SOURCE = "insert into ob_script_source (script_id, source, ob_script_source.order) values (?,?,?)";
    public static final String GET_FUNCTION_ARGS = "select arg_operand_id from ob_operand_args where operand_id=? order by sequence";
    public static final String INIT_OPERAND_RECORD = "insert into ob_operand (operand_id) values (?)";
    public static final String OPERAND_EXISTS = "select count(*) from ob_operand where operand_id=?";
    public static final String UPDATE_OPERAND = "update ob_operand set operand_type=get_operand_type_id(?), operand_num=?, sequence=?, is_function=?, is_subgraph=?, is_compliment=?, expression=?, expression_id=?, action_id=? where operand_id=?";
    public static final String WRITE_FUNCTION_ARGS = "insert into ob_operand_args (operand_id, arg_operand_id, sequence) values (?,?,?)";
    public static final String GET_OPERAND_INFO = "select get_operand_type_name(operand_type), is_function, is_subgraph, is_compliment, expression, expression_id from ob_operand where operand_id = ?";
    public static final String GET_ACTION_OPERANDS = "select operand_id from ob_operand where action_id=? and operand_num=? order by sequence";
    //public static final String GET_ACTION_OPERANDS = "select operand_id from ob_action join ob_operand on ob_action.action_id=ob_operand.action_id where ob_action.action_id=? and ob_operand.operand_num=? order by ob_operand.sequence";
    public static final String GET_ACTION_INFO = "select get_action_type_name(action_type), is_intrasession, is_intersection, sequence from ob_action where action_id=?";
    public static final String GET_ACTION_TYPE = "select get_action_type_name(action_type) from ob_action where ob_action.action_id=?";
    public static final String GET_RULE_ACTIONS = "select action_id from ob_action where rule_id = ? order by sequence";
    public static final String GET_EVENT_PATTERN_INFO = "select is_active, is_any from ob_event_pattern where event_pattern_id = ?";
    public static final String GET_EVENT_PATTERN_CONT_SPEC = "select concat(ob_cont_spec_type.cont_spec_type, '|', cont_spec_value) from ob_cont_spec join ob_cont_spec_type on ob_cont_spec_type.cont_spec_type_id=ob_cont_spec.cont_spec_type where event_pattern_id=?";
    public static final String GET_EVENT_PATTERN_OBJ_SPEC = "select obj_spec_value from ob_obj_spec where event_pattern_id=?";
    public static final String GET_EVENT_PATTERN_PC_SPEC = "select policy_spec_value from ob_policy_spec where event_pattern_id=?";
	public static final String GET_EVENT_PATTERN_OP_SPEC = "select event_name from ob_op_spec join ob_op_spec_events on ob_op_spec.op_spec_event_id=ob_op_spec_events.event_id where event_pattern_id=?";
	public static final String GET_EVENT_PATTERN_USER_SPEC = "select concat(ob_user_spec_type.user_spec_type, '|', user_spec_value) from ob_user_spec join ob_user_spec_type on ob_user_spec_type.user_spec_type_id=ob_user_spec.user_spec_type where event_pattern_id=?";
    public static final String GET_RULE_EVENT_PATTERN = "select event_pattern_id from ob_event_pattern where rule_id = ?";
    public static final String GET_SCRIPT_RULES = "select rule_id from ob_rule where script_id = ? order by sequence";
    public static final String GET_SCRIPT_NAME = "select script_name from ob_script where script_id = ?";
    public static final String WRITE_OPERAND_RECORD = "insert into ob_operand (operand_id,operand_type,operand_num,sequence,is_function,is_subgraph,is_compliment,expression,expression_id,action_id) values (?,get_operand_type_id(?),?,?,?,?,?,?,?,?)";
    public static final String DELETE_SCRIPT_WITH_NAME = "delete from ob_script where script_name = ? and script_id <> ?";
    public static final String DELETE_SCRIPT = "delete from ob_script where script_id = ?";
    public static final String ADD_RULE_TO_SCRIPT = "";
	public static final String GET_SCRIPT_COUNT = "select count from ob_script where script_id=?";
	public static final String UPDATE_RULE_RECORD = "update ob_rule set script_id=?, sequence=?";
    public static final String INCREASE_SCRIPT_COUNT = "update ob_script set count = count+1 where script_id = ?";
    public static final String DECREASE_SCRIPT_COUNT = "update ob_script set count = count-1 where script_id = ?";
    public static final String DELETE_SCRIPT_RULE = "delete from ob_rule where script_id=? and rule_name=?";
	public static final String CREATE_ACTION_RECORD = "insert into ob_action(action_id, action_type, is_intrasession, is_intersection, sequence, rule_id) values (?,get_action_type_id(?),?,?,?,?)";
    public static final String CREATE_SCRIPT_RECORD = "insert into ob_script(script_name) values (?)";
	public static final String WRITE_RULE_RECORD = "insert into ob_rule (rule_id, rule_name, count, sequence, script_id) values (?,?,?,?,?)";
	public static final String SET_RULE_SCRIPT_ID = "update ob_rule set script_id=? where rule_id=?";
	public static final String WRITE_PATTERN_RECORD = "insert into ob_event_pattern (event_pattern_id,rule_id,is_any,is_active) values (?,?,?,?)";
    public static final String ASSOCIATION_EXISTS = "SELECT case when count(*) > 0 then true else false end FROM association where opset_id = ? and oa_id = ?";
	public static final String ADD_USER_SPEC = "insert into ob_user_spec (event_pattern_id, user_spec_type, user_spec_value) values (?,get_user_spec_type_id(?),?)";
    public static final String ADD_PC_SPEC = "insert into ob_policy_spec (event_pattern_id, policy_spec_type, policy_spec_value) values (?,get_node_type_id(?),?)";
    public static final String ADD_OP_SPEC = "insert into ob_op_spec (event_pattern_id, op_spec_event_id) values (?,get_op_spec_type_id(?))";
    public static final String ADD_OBJ_SPEC = "insert into ob_obj_spec (event_pattern_id, obj_spec_type, obj_spec_value) values (?,get_node_type_id(?),?)";
    public static final String ADD_CONT_SPEC = "insert into ob_cont_spec (event_pattern_id, cont_spec_type, cont_spec_value) values (?,get_cont_spec_type_id(?),?)";


    public static final String GET_ALL_ACC_NODES = "select node_id from node where is_accessible(?, node_id) = true";
	public static final String GET_ALL_NODES = "select node_id from node";

	/**
	 * Paths
	 */
	public static final String GET_PATHS = "SELECT path FROM assignment where end_node_id=? and path is not null";
	public static final String GET_NODE_PATHS = "SELECT path FROM path_view where node_id = ?";
	public static final String ADD_TO_PATH = "insert into path (path_id, node_id) values (?,?)";
    public static final String CHECK_NAME_EXISTS_IN_PATH = "select case when count(*) > 0 then true else false end from path_view_names where path like concat('%', get_node_name(?), '%') and node_name=?";
    public static final String GET_FROM_ATTRS_T_D_PATHS = "select path from path_view join node on path_view.node_id = node.node_id join node_type on node.node_type_id = node_type.node_type_id where path not like concat('%', ?, '>%>%') AND path like concat('%', ?, '%') AND node_type.name = ?";

	/**
	 * obligation
	 */
	public static final String GET_SCRIPTS = "select script_name, script_id from ob_script";
	public static final String GET_ENABLED_SCRIPT = "select script_id from ob_script where enabled = 1";
	public static final String DISABLE_ENABLED_SCRIPT = "update ob_script set enabled = 0 where script_id = ?";
	public static final String ENABLE_SCRIPT = "update ob_script set enabled = 1 where script_id = ?";
	public static final String IS_SUPER = "select case when node.name='super' then true else false end from session join node on session.user_node_id=node.node_id where session.session_id=?";


	/**
	 * application
	 */
	public static final String ADD_HOST_APP 		= "INSERT INTO application (host_id, application_name, application_main_class, application_path, application_prefix) values(?,?,?,?,?)";
	public static final String GET_HOST_APP_PATHS 	= "SELECT application_name, application_path FROM application, host where host.host_id = application.host_id and UPPER(host.host_name) = UPPER(?) order by application_id";
	public static final String GET_HOST_APP_PATH	= "select application_main_class, application_path, application_prefix from application join host on application.host_id=host.host_id where application.application_name = ? and host.host_name = ?";
	public static final String DELETE_APP			= "delete from application where upper(application_name) = upper(?) and host_id = ?";

	/**
	 * assignment
	 */
	public static final String GET_ASSIGNMENTS 		= "SELECT start_node_id FROM assignment WHERE end_node_id = ? AND get_node_type(start_node_id) <> 7 AND depth = 1";
	public static final String GET_MEMBERS 			= "SELECT end_node_id FROM assignment WHERE start_node_id = ? AND get_node_type(end_node_id) <> 7 AND depth = 1";
	public static final String IS_ASSIGNED 			= "select case when COUNT(*) > 0 then true else false end FROM assignment WHERE start_node_id = ? AND end_node_id = ?";
    public static final String IS_ASSIGNED_PATHS    = "select case when count(*) > 0 then true else false end from path_node join path on path_node.path_id = path.path_id where path_node.node_id = ? and path.node_id = ?";
	public static final String IS_NODE_ISOLATED		= "select case when COUNT(*) > 0 then false else true end FROM assignment WHERE (end_node_id = ? OR start_node_id = ? ) AND depth > 0";
	public static final String HAS_ASCENDENTS 		= "select COUNT(*) > 0 FROM assignment WHERE START_NODE_ID = ? AND get_node_type(end_node_id) <> 7 AND DEPTH > 0";
	public static final String HAS_DESCENDENTS 		= "select case when COUNT(*) > 0 then false else true end FROM assignment WHERE END_NODE_ID = ? AND get_node_type(end_node_id) <> 7 AND DEPTH > 0";
	public static final String ATTR_IN_ANY_POL 		= "select node.node_id from assignment join node on node.node_id = assignment.start_node_id where assignment.end_node_id = ? and (node.node_type_id = 2 or node.node_type_id = 8)";
	public static final String GET_REACHABLE_PC_NODES = "select distinct start_node_id from assignment where get_node_type(start_node_id) = 2 or get_node_type(start_node_id) = 8 and end_node_id = ?";
	public static final String GET_1ST_LEVEL_ASSIGNMENT_INFO = "SELECT start_node_id, get_node_type_name(start_node_id), get_node_name(start_node_id) FROM assignment WHERE end_node_id = ? AND get_node_type(start_node_id) <> 7 AND depth = 1";
    public static final String GET_MAX_ASSIGNMENT_ID_BTW = "select max(assignment_id) from assignment where start_node_id=? and end_node_id=?";
    public static final String GET_DEPTH_FROM       = "select depth from assignment join assignment_path on assignment_path.assignment_path_id=assignment.assignment_path_id where start_node_id=? and end_node_id=? and assignment_node_id=?";
    public static final String UPDATE_ASSIGNMENT_DEPTH = "update assignment set depth=? where assignment_id=?";
	public static final String GET_ASSIGNMENT_SUBGRAPH = "select end_node_id, node.name, depth from assignment join node on assignment.end_node_id=node.node_id where start_node_id=?";
	/**
	 * Audit
	 */
	public static final String INSERT_AUDIT_RECORD = "INSERT INTO audit_Information VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	/**
	 * deny
	 */
	public static final String GET_DENIES			= "SELECT deny_name, deny_id FROM deny";
	public static final String GET_DENY_NAME		= "SELECT DENY_NAME FROM DENY WHERE DENY_ID = ?";
	public static final String GET_DENY_ID			= "SELECT DENY_ID FROM DENY WHERE DENY_NAME = ?";
	public static final String DENY_HAS_OP			= "SELECT case when count(*) > 0 then true else false end FROM DENY_OPERATION WHERE DENY_ID = ? AND DENY_OPERATION_ID = ?";
	public static final String DENY_HAS_OATTR		= "select case when COUNT(*) > 0 then true else false end FROM DENY_OBJ_ATTRIBUTE WHERE DENY_ID = ? AND OBJECT_ATTRIBUTE_ID = ?";
	public static final String GET_DENY_SIMPLE_INFO = "select deny.deny_name, deny_type.name, node.name, deny.user_attribute_id, deny.process_id, deny.is_intersection from deny join deny_type on deny.deny_type_id = deny_type.deny_type_id left join node on node.node_id = deny.user_attribute_id where deny.deny_id = ?";
	public static final String GET_DENY_OPS			= "SELECT operation.name FROM deny_operation join operation on operation.operation_id = deny_operation.deny_operation_id where deny_operation.deny_id = ?";
	public static final String GET_DENY_OATTRS		= "select node.name, deny_obj_attribute.object_attribute_id, object_complement from deny_obj_attribute join node on node.node_id = deny_obj_attribute.object_attribute_id where deny_id = ?";
	public static final String ADD_DENY_OP			= "INSERT INTO DENY_OPERATION (deny_id, deny_operation_id) VALUES(?,get_operation_id(?))";
	public static final String ADD_DENY_OBJ_ID		= "INSERT INTO DENY_OBJ_ATTRIBUTE (deny_id, object_attribute_id, object_complement) VALUES(?,?,?)";
	public static final String DELETE_DENY			= "DELETE FROM DENY WHERE DENY_ID = ?";
	public static final String ADD_DENY				= "insert into deny (deny_name, deny_type_id, user_attribute_id, is_intersection) values (?,get_deny_type_id(?),?,?)";
	public static final String ADD_PROCESS_DENY		= "insert into deny (deny_name, deny_type_id, process_id, is_intersection) values (?,get_deny_type_id(?),?,?)";
	public static final String ADD_OATTR_TO_DENY 	= "insert into deny_obj_attribute values(?,?,?)";

	/**
	 * host
	 */
	public static final String DELETE_HOST			= "DELETE FROM host WHERE host_id = ?";
	public static final String GET_HOSTS			= "SELECT host_id, host_name FROM host";
	public static final String GET_HOST_INFO		= "SELECT host_name, workarea_path FROM host WHERE host_id = ?";
	public static final String GET_HOST_ID			= "SELECT host_id FROM host WHERE UPPER(host_name) = UPPER(?)";
	public static final String UPDATE_HOST			= "update host set host_name=?,workarea_path=? where host_id = ?";
	public static final String GET_HOST_REPO		= "select workarea_path from host where upper(host_name) = upper(?)";
	public static final String GET_HOST_NAME		= "SELECT HOST_NAME FROM HOST WHERE HOST_ID = ?";

	/**
	 * node
	 */
	public static final String ENTITY_NAME_EXISTS	= "SELECT node.name FROM node WHERE UPPER(node.NAME) = UPPER(?)";
	public static final String DELETE_NODE			= "DELETE FROM node WHERE node_id = ?";
	public static final String GET_PM_ENTITIES_OF_CLASS = "SELECT N.NAME, N.NODE_ID FROM NODE N WHERE N.NODE_TYPE_ID IN (SELECT NT.NODE_TYPE_ID FROM NODE_TYPE NT WHERE UPPER(NT.DESCRIPTION) = UPPER(?))";
	public static final String GET_NODE_TYPE 		= "select node_type.name from node join node_type on node.node_type_id = node_type.node_type_id where node.node_id = ?";
	public static final String GET_NODE_INFO		= "select node.name, node.node_id, node.description from node where node.node_id = ?";

	/**
	 * property
	 */
	public static final String GET_PROPERTY_VALUE 	= "SELECT PROPERTY_VALUE FROM node_property WHERE UPPER(PROPERTY_key) = UPPER(?)";
	public static final String INSERT_PROPERTY 		= "INSERT INTO NODE_PROPERTY (PROPERTY_KEY, PROPERTY_VALUE, PROPERTY_NODE_ID) VALUES (?,?,?)";
	public static final String GET_ENTITY_WITH_PROP = "SELECT PROPERTY_NODE_ID FROM NODE_PROPERTY WHERE UPPER(PROPERTY_KEY) = UPPER(?) AND UPPER(PROPERTY_VALUE) = UPPER(?)";
	public static final String GET_NODE_PROPERTIES 	= "SELECT concat(NODE_PROPERTY.property_key, '" + GlobalConstants.PM_PROP_DELIM + "', NODE_PROPERTY.property_value) FROM NODE_PROPERTY WHERE PROPERTY_NODE_ID = ?";
	public static final String GET_ALL_PROPERTIES 	= "SELECT NODE_PROPERTY.property_KEY, property.property_value FROM property";
	public static final String DELETE_PROPERTY 		= "delete from NODE_PROPERTY where property_node_id = ? and upper(concat(property_key, '" + GlobalConstants.PM_PROP_DELIM + "', property_value)) = upper(?)";
	public static final String REPLACE_PROP 		= "update NODE_PROPERTY set property_key=?, property_value=? where property_node_id = ? and upper(concat(property_KEY, '" + GlobalConstants.PM_PROP_DELIM + "', property_value)) = upper(?)";

	/**
	 * object_class
	 */
	public static final String GET_OBJ_CLASSES 		= "SELECT name FROM object_class";
	public static final String OBJ_CLASS_HAS_OP		= "SELECT COUNT(*) FROM OPERATION WHERE UPPER(NAME) = upper(?) AND OBJECT_CLASS_ID = ?";
	public static final String GET_CLASS_ID 		= "SELECT OBJECT_CLASS_ID FROM OBJECT_CLASS WHERE UPPER(NAME) = UPPER(?)";
	public static final String GET_OP_NAMES 		= "SELECT OP.NAME FROM OPERATION OP, OBJECT_CLASS OC WHERE OP.OBJECT_CLASS_ID = OC.OBJECT_CLASS_ID AND UPPER(OC.NAME) = UPPER(?)";
	public static final String GET_OBJ_CLASS_NAME 	= "SELECT OC.NAME FROM OBJECT_CLASS OC WHERE OC.OBJECT_CLASS_ID = ?";
	public static final String ADD_OBJ_CLASS		= "insert into object_class (name, description) values (?, ?)";
	public static final String DELETE_OBJ_CLASS		= "delete from object_class where upper(name) = upper(?)";

	/**
	 * object_detail
	 */
	public static final String GET_OBJ_IDS_OF_ORIG_ID	= "SELECT OBJECT_NODE_ID FROM OBJECT_DETAIL WHERE ORIGINAL_NODE_ID = ?";
	public static final String GET_ORIG_OBJ_ID		= "SELECT ORIGINAL_NODE_ID FROM OBJECT_DETAIL WHERE OBJECT_NODE_ID = ?";
	public static final String GET_NODE_TYPE_NAME	= "SELECT NT.DESCRIPTION FROM NODE N, NODE_TYPE NT WHERE NT.NODE_TYPE_ID = N.NODE_TYPE_ID AND N.NODE_ID =  ?";
	public static final String GET_OATTR_REPRESENTING_ENTITY = "SELECT OBJECT_NODE_ID FROM OBJECT_DETAIL WHERE ORIGINAL_NODE_ID = ? AND INCLUDE_ASCEDANTS = ?";
	public static final String OATTR_REPRESENTS_THIS_ENTITY = "SELECT OBJECT_NODE_ID FROM OBJECT_DETAIL WHERE OBJECT_NODE_ID = ? AND ORIGINAL_NODE_ID = ? AND INCLUDE_ASCEDANTS = ?";
	public static final String IS_RECORD 			= "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM  OBJECT_DETAIL WHERE OBJECT_NODE_ID = ? AND TEMPLATE_ID IS NOT NULL";
	public static final String GET_RECORD 			= "SELECT OBJECT_DETAIL.OBJECT_NODE_ID FROM OBJECT_DETAIL WHERE OBJECT_NODE_ID IN (SELECT START_NODE_ID FROM ASSIGNMENT WHERE END_NODE_ID = ?) AND TEMPLATE_ID IS NOT NULL";
	public static final String GET_OBJECT_INFO 		= "select object_detail.object_node_id, object_detail.original_node_id, object_detail.include_ascedants, get_object_class_name(object_detail.object_class_id) from object_detail where object_node_id = ?";
	public static final String UPDATE_RECORD_TPL 	= "update object_detail set template_id=? where object_node_id=?";
	public static final String GET_REC_TPL			= "select template_id from object_detail where object_node_id = ?";
	public static final String GET_REC_INFO			= "select node.name, node.node_id, template.template_name, template.template_id from object_detail join node on node.node_id = object_detail.object_node_id join template on template.template_id = object_detail.template_id where object_detail.object_node_id = ?";
	public static final String GET_REC_COMPS		= "select record_component_id from record_components where record_node_id = ? order by record_components.order";
	public static final String GET_RECORDS			= "select node.name, object_detail.object_node_id from object_detail join node on node.node_id = object_detail.object_node_id where object_detail.template_id is not null";
	public static final String GET_TPL_RECORDS  	= "select node.name, object_detail.object_node_id from object_detail join node on node.node_id = object_detail.object_node_id where object_detail.template_id = ?";
	public static final String GET_TPL_RECORDS_WITH_KEY = "select node.name, object_detail.object_node_id from object_detail join node on node.node_id = object_detail.object_node_id join record_key on record_key.record_node_id = node.node_id where object_detail.template_id = ? and record_key.record_key=?";
	public static final String GET_HOST_AND_PATH 	= "select host.host_name, object_detail.path from object_detail join host on object_detail.host_id = host.host_id where object_detail.object_node_id = ?";
	public static final String GET_ALL_OBJECTS 		= "SELECT NODE.NAME, NODE.NODE_ID, NODE_TYPE.DESCRIPTION, OBJECT_DETAIL.INCLUDE_ASCEDANTS, GET_NODE_NAME(OBJECT_DETAIL.ORIGINAL_NODE_ID), OBJECT_DETAIL.ORIGINAL_NODE_ID, GET_HOST_NAME(OBJECT_DETAIL.HOST_ID), GET_HOST_PATH(OBJECT_DETAIL.HOST_ID) FROM NODE JOIN NODE_TYPE ON NODE_TYPE.NODE_TYPE_ID = NODE.NODE_TYPE_ID LEFT JOIN OBJECT_DETAIL ON OBJECT_DETAIL.OBJECT_NODE_ID = NODE.NODE_ID LEFT JOIN HOST ON HOST.HOST_ID = OBJECT_DETAIL.HOST_ID WHERE NODE.NODE_TYPE_ID = 6";


    /**
	 * email
	 */
	public static final String ADD_EMAIL_DETAIL = "insert into email_detail (object_node_id, sender, recipient, timestamp, email_subject) values (?,?,?,now(),?)";
	public static final String ADD_EMAIL_ATTACHMENT = "insert into email_attachment (object_node_id, attachment_node_id) values (?,?)";
	public static final String GET_ATTACHMENTS = "select attachment_node_id from email_attachment where object_node_id = ?";
	public static final String GET_EMAIL_DETAIL = "select sender, recipient, timestamp, email_subject from email_detail where object_node_id = ?";

	/**
	 * record_key
	 */
	public static final String INSERT_RECORD_KEY	= "INSERT INTO RECORD_KEY (RECORD_NODE_ID, RECORD_KEY, RECORD_VALUE) VALUES (?,?,?)";
	public static final String GET_RECORD_KEY		= "SELECT RECORD_KEY FROM RECORD_KEY WHERE RECORD_NODE_ID = ?";

	/**
	 * operation
	 */
	public static final String GET_OP_ID			= "SELECT operation_id FROM operation where UPPER(name) = UPPER(?)";
	public static final String GET_ALL_OPS			= "SELECT name FROM operation";
	public static final String ADD_OPERATION_TO_OPSET = "INSERT INTO OPERATION_SET_DETAILS VALUES(?,?)";
	public static final String DELETE_OP			= "delete from operation where upper(name) = upper(?)";
	public static final String GET_OPERATION_CLASS 	= "SELECT object_class.name FROM operation join object_class on object_class.object_class_id = operation.object_class_id where operation.operation_id = ?";

	/**
	 * operation_set
	 */
	public static final String GET_OPSETS			= "SELECT node.name, node.node_id FROM node WHERE node.node_type_id = 7";
	public static final String UATTR_HAS_OPSET		= "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END  FROM ASSIGNMENT A WHERE A.START_NODE_ID = ? AND DEPTH = 1 AND  EXISTS (SELECT NODE_ID FROM NODE B WHERE B.NODE_ID = A.END_NODE_ID AND B.NODE_TYPE_ID = (SELECT NODE_TYPE_ID FROM NODE_TYPE WHERE UPPER(NODE_TYPE.NAME) = UPPER(?)))";
	public static final String OATTR_HAS_OPSET		= "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END  FROM ASSIGNMENT A WHERE A.END_NODE_ID = ? AND DEPTH = 1 AND  EXISTS (SELECT NODE_ID FROM NODE B WHERE B.NODE_ID = A.START_NODE_ID AND B.NODE_TYPE_ID = (SELECT NODE_TYPE_ID FROM NODE_TYPE WHERE UPPER(NODE_TYPE.NAME) = UPPER(?)))";

	/**
	 * operation_set_detail
	 */
	public static final String OPSET_CONTAINS_OP	= "SELECT DISTINCT operation_set_details_node_id FROM operation_set_details WHERE operation_set_details_node_id = ? AND operation_id = ?";
	public static final String DELETE_OPSET_OPS		= "delete from operation_set_details where operation_set_details_node_id = ?";
	public static final String ADD_OPSET_OP			= "insert into operation_set_details (operation_set_details_node_id, operation_id) values (?, (select operation.operation_id from operation where upper(operation.name) = upper(?)));";
	public static final String DELETE_OPSET_OP		= "delete from operation_set_details where operation_set_details_node_id = ? and operation_id = ?";

	/**
	 * open object
	 */
	public static final String ADD_OPEN_OBJ			= "insert into open_object (session_id, object_node_id, count) values (?,?,?) on duplicate key update count = count + 1";
	public static final String DELETE_OPEN_OBJ		= "delete from open_object where session_id = ? and object_node_id = ?";
    public static final String GET_OPEN_OBJ_COUNT 	= "select count from open_object where session_id = ? and object_node_id = ?";
    public static final String UPDATE_OPEN_OBJ_COUNT 	= "update open_object set count = ? where session_id = ? and object_node_id = ?";
	public static final String GET_SESSION_OPEN_OBJECTS = "select object_node_id from open_object where session_id = ?";

	/**
	 * user_detail
	 */
	public static final String GET_USER_PASS		= "SELECT password FROM user_detail WHERE user_node_id = ?";
	public static final String USER_DETAIL_EXISTS	= "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM user_detail WHERE user_node_id = ?";
	public static final String CHANGE_PASSWORD		= "UPDATE user_detail SET password = ? WHERE upper(user_name) = upper(?)";
	public static final String GET_USERS			= "SELECT user_name, user_node_id FROM user_detail";
	public static final String UPDATE_EMAIL_ACCT	= "UPDATE USER_DETAIL SET USER_NAME = IFNULL(?, USER_NAME), EMAIL_ADDRESS = IFNULL(?, EMAIL_ADDRESS),  POP_SERVER = IFNULL(?, POP_SERVER), SMTP_SERVER = IFNULL(?, SMTP_SERVER), ACCOUNT_NAME = IFNULL(?, ACCOUNT_NAME)" + /*, PASSWORD = ?*/ " WHERE USER_NODE_ID = ?";
	public static final String ADD_EMAIL_ACCT		= "INSERT INTO USER_DETAIL (USER_NODE_ID, USER_NAME, EMAIL_ADDRESS, POP_SERVER, SMTP_SERVER, ACCOUNT_NAME) values (?,?,?,?,?,?)";
	public static final String GET_EMAIL_ACCT		= "select user_name, email_address, pop_server, smtp_server, account_name from user_detail where user_node_id = ?";

	/**
	 * record_components
	 */
	public static final String ADD_COMP_TO_OATTR	= "insert into record_components (record_node_id, record_component_id, record_components.order) values (?, ?, ?)";

	/**
	 * session
	 */
    public static final String GET_SESSIONS         = "select session_name, session_id from session";
	public static final String GET_SESSION_INFO		= "select session_name, session_id, node_id, name, host.host_id, host_name from session, node, host where node.node_id = session.user_node_id and session.host_id = host.host_id and session.session_id = ?";
	public static final String GET_SESSION_NAME		= "SELECT SESSION_NAME FROM SESSION WHERE SESSION_ID = ?";
	public static final String GET_SESSION_ID		= "SELECT SESSION_ID FROM SESSION WHERE upper(SESSION_NAME) = upper(?)";
	public static final String GET_SESS_USER_ID		= "select user_node_id from session where session_id = ?";
	public static final String CREATE_SESSION 		= "insert into session (user_node_id, start_time, session_name, host_id) values (?, now(), ?, ?)";
	public static final String GET_SESSION_HOST_NAME= "select host.host_name from session join host on session.host_id = host.host_id where session.session_id = ?";
	public static final String GET_USER_SESSIONS 	= "select session_id from session where user_node_id = ?";
	public static final String GET_SESSION_HOST_ID  = "select host_id from session where session_id = ?";
	public static final String DELETE_SESSION		= "delete from session where session_id = ?";

	/**
	 * template
	 */
	public static final String INSERT_TEMPLATE      = "INSERT INTO TEMPLATE (TEMPLATE_NAME) VALUES (?)";
	public static final String GET_TEMPLATE_NAME	= "SELECT TEMPLATE_NAME FROM TEMPLATE WHERE TEMPLATE_ID = ?";
	public static final String GET_TEMPLATE_ID		= "SELECT TEMPLATE_ID FROM TEMPLATE WHERE upper(TEMPLATE_NAME) = upper(?)";
	public static final String DELETE_TEMPLATE		= "DELETE FROM TEMPLATE WHERE TEMPLATE_ID = ?";
	public static final String GET_TEMPLATES		= "SELECT TEMPLATE_NAME, TEMPLATE_ID FROM TEMPLATE";
	public static final String GET_TEMPLATE_INFO	= "SELECT TEMPLATE_ID, TEMPLATE_NAME FROM TEMPLATE where TEMPLATE_ID = ?";

	/**
	 * template_key
	 */
	public static final String GET_TEMPLATE_KEYS	= "select template_key from template_key where template_id = ?";
	public static final String INSERT_TPL_KEY		= "insert into template_key (template_id, template_key) values (?,?)";

	/**
	 * template_conts
	 */
	public static final String GET_TEMPLATE_CONTS	= "select template_component_id from template_component where template_id = ? order by template_component.order";
	public static final String ADD_TEMPLATE_CONT	= "insert into template_component (template_id, template_component_id, template_component.order) values (?,?,?)";

	/**
	 * POLICY
	 */
	public static final String GET_POLICY_CLASSES 	= "SELECT name, node_id  FROM node WHERE node_type_id = 2 or node_type_id = 8";

	/**
	 * getFromAttrs
	 */
	public static final String GET_FROM_ATTRS		= "select assignment.end_node_id from assignment where assignment.start_node_id = ?";
	public static final String GET_FROM_ATTRS_D		= "select assignment.end_node_id from assignment where assignment.start_node_id = ? and assignment.depth = 1";
	public static final String GET_FROM_ATTRS_T		= "select assignment.end_node_id from assignment join node on node.node_id = assignment.end_node_id join node_type on node_type.node_type_id = node.node_type_id where assignment.start_node_id = ? and node_type.name = ?";
	public static final String GET_FROM_ATTRS_T_D	= "select assignment.end_node_id from assignment join node on node.node_id = assignment.end_node_id join node_type on node_type.node_type_id = node.node_type_id where assignment.start_node_id = ? and node_type.name = ? and assignment.depth = 1";

	/**
	 * getToAttrs
	 */
	public static final String GET_TO_ATTRS		 	= "select distinct assignment.start_node_id from assignment where assignment.end_node_id = ?";
	public static final String GET_TO_ATTRS_T_D 	= "select distinct assignment.start_node_id from assignment join node on node.node_id = assignment.start_node_id join node_type on node_type.node_type_id = node.node_type_id where assignment.end_node_id = ? and node_type.name = ? and depth = 1";
	public static final String GET_TO_ATTRS_T 		= "select distinct assignment.start_node_id from assignment join node on node.node_id = assignment.start_node_id join node_type on node_type.node_type_id = node.node_type_id where assignment.end_node_id = ? and node_type.name = ?";
	public static final String GET_TO_ATTRS_D 		= "select distinct assignment.start_node_id from assignment where assignment.end_node_id = ? and depth = 1";
	public static final String GET_TO_ATTRS_WITH_TYPE 	= "select distinct node.name, node_type.name from assignment, node, node_type where node.node_id = assignment.start_node_id and node.node_type_id = node_type.node_type_id and assignment.end_node_id = ?";

	public static final String GET_INSTALLED_APPS 	= "select application_name from application join host on application.host_id = host.host_id where upper(host.host_name) = upper(?)";
	public static final String GET_USER_ATTRS 		= "select name, node_id from node where node_type_id = 3";
	public static final String GET_OPSET_OPS 		= "select operation.name from operation_set_details join operation on operation_set_details.operation_id = operation.operation_id where operation_set_details.operation_set_details_node_id = ?";
	public static final String GET_OPSETS_BETWEEN	= "select node.name, node.node_id from assignment join node on (node.node_id = assignment.start_node_id or node.node_id = assignment.end_node_id) and node.node_type_id = 7 where node.name in (select node.name from assignment join node on (node.node_id = assignment.start_node_id or node.node_id = assignment.end_node_id) and node.node_type_id = 7 where end_node_id = ?) and start_node_id = ? and depth = 1;";
	public static final String GET_OPSET_OATTRS		= "select node.name, assignment.start_node_id from assignment join node on node.node_id = assignment.start_node_id where end_node_id = ? and node_type_id = 5";
	public static final String GET_USER_FULL_NAME   = "select full_name from user_detail where user_node_id = ?";
	public static final String GET_USER_INFO		= "select user_name, user_node_id, full_name from user_detail where user_node_id = ?";
	public static final String GET_OBJ_PATH 		= "select path from object_detail where object_node_id = ?;";
	public static final String GET_ALL_KSTORE_PATHS = "select host_id, user_node_id, keystore_path, truststore_path from keystore";
	public static final String GET_KSTORE_PATHS		= "select keystore_path, truststore_path from keystore where host_id = ? and user_node_id = ?";
	public static final String GET_OATTRS 			= "select node.name, node.node_id from node where node_type_id = 5 or node_type_id = 6 or node_type_id = 9";
	public static final String GET_OATTRS_PROPER	= "select node.name, node.node_id from node where node_type_id = 5 or node_type_id = 9";
	public static final String SET_KSTORE_PATHS		= "insert into keystore (keystore_path, truststore_path, host_id, user_node_id) values (?, ?, ?, ?)";
    public static final String UPDATE_KSTORE_PATHS  = "update keystore set keystore_path=?, truststore_path=? where host_id=?and user_node_id=?";
	public static final String GET_ALL_POLICY_IDS 	= "select node_id from node where node_type_id = 2 or node_type_id = 8";
	public static final String GET_ALL_UATTR_IDS 	= "select node_id from node where node_type_id = 3";
	public static final String GET_ALL_USER_IDS		= "select node_id from node where node_type_id = 4";
	public static final String GET_ALL_OATTR_IDS 	= "select node_id from node where node_type_id = 5 or node_type_id = 9";
	public static final String GET_ALL_OBJ_IDS		= "select node_id from node where node_type_id = 6";
	public static final String 	GET_ALL_OPSET_IDS 	= "select node_id from node where node_type_id = 7";

	/**
	 * Method to prepare a sql statement
	 *
	 * @param sql
	 *            the sql statement to be executed
	 * @param params
	 *            a 2D array of parameters and their data types (each row will
	 *            be a value, type pair: [value, type]).
	 */
	public static PreparedStatement prepareSQLStatement(String sql,
			MySQL_Parameters params) throws Exception {
        if(ServerConfig.conn == null){
            throw new Exception("Error connecting to database");
        }
		Statement stmt = ServerConfig.conn.createStatement();
		stmt.executeUpdate("use " + GlobalConstants.PM_SCHEMA);
		PreparedStatement ps = ServerConfig.conn.prepareCall(sql);
		if(params != null){
			for(int i = 0; i < params.getParams().size(); i++){
				String type = params.getParams().get(i)[0];
				String value = params.getParams().get(i)[1];
				if(type.equals(ParamType.INT.getName())){
					if (value==null) {
						ps.setNull(i+1, Types.INTEGER);
					} else {
						ps.setInt(i+1, Integer.parseInt(value));
					}
				}else if(type.equals(ParamType.STRING.getName())){
					if (value==null) {
						ps.setNull(i+1, Types.VARCHAR);
					} else {
						ps.setString(i+1, value);
					}
				}else if(type.equals(ParamType.BOOLEAN.getName())){
					if (value==null) {
						ps.setNull(i+1, Types.BOOLEAN);
					} else {
						ps.setBoolean(i+1, Boolean.parseBoolean(value));
					}
				}
			}
		}
		return ps;
	}

	/**
	 * General delete method
	 *
	 * @param sql
	 *            the sql statement to be executed
	 * @param params
	 *            a 2D array of parameters and their data types (each row will
	 *            be a value, type pair: [type, value]).
	 */
	public static void delete(String sql, MySQL_Parameters params)
			throws Exception {
        try {
            ServerConfig.conn = ServerConfig.pmDB.getConnection();
            PreparedStatement ps = prepareSQLStatement(sql, params);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new Exception("Exception in delete: " + e.getMessage());
        } finally {
            ServerConfig.conn.close();
        }
    }

    public static void delete(String sql, Object ... args) throws Exception{
        delete(sql, params(args));
    }

	/**
	 * General update method
	 *
	 * @param sql
	 *            the sql statement to be executed
	 * @param params
	 *            a 2D array of parameters and their data types (each row will
	 *            be a value, type pair: [value, type]).
	 */
	public static void update(String sql, MySQL_Parameters params)
			throws Exception {
		try{
			ServerConfig.conn = ServerConfig.pmDB.getConnection();
			PreparedStatement ps = prepareSQLStatement(sql, params);
			ServerConfig.conn.setAutoCommit(true);
			ps.executeUpdate();
		}catch(Exception e){
			throw new Exception("Exception in update: " + e.getMessage());
		}finally{
			ServerConfig.conn.close();
		}
	}

    public static void update(String sql, Object ... args) throws Exception{
        update(sql, params(args));
    }

	/**
	 * General method to insert into a table
	 *
	 * @param sql
	 *            the sql statement to be executed
	 * @param params
	 *            a 2D array of parameters and their data types (each row will
	 *            be a value, type pair: [value, type]).
	 */
	public static void insert(String sql, MySQL_Parameters params)
			throws Exception /* , ClassNotFoundException */{
		PreparedStatement ps = null;
		try{
			ServerConfig.conn = ServerConfig.pmDB.getConnection();
			ps = prepareSQLStatement(sql, params);
			ps.executeUpdate();
		}catch(Exception e){
			throw new Exception("Exception in insert: " + e.getMessage());
		}finally{
			ServerConfig.conn.close();
		}
	}

    public static void insert(String sql, Object ... args) throws Exception{
        insert(sql, params(args));
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
	 * General select method
	 *
	 * @param sql
	 *            the sql statement to be executed
	 * @param params
	 *            a 2D array of parameters and their data types (each row will
	 *            be a value, type pair: [value, type]).
	 * @return an ArrayList containing the result of executing query
	 */
	public static ArrayList<ArrayList<Object>> select(String sql, MySQL_Parameters params) throws Exception {
		ServerConfig.conn = ServerConfig.getPmDB().getConnection();
		ArrayList<ArrayList<Object>> returned = new ArrayList<ArrayList<Object>>();
		try {
			PreparedStatement ps = prepareSQLStatement(sql, params);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			int numCols = meta.getColumnCount();
			if (rs!=null) {
				while(rs.next()){
					ArrayList<Object> row = new ArrayList<Object>();
					for(int i = 0; i < numCols; i++){
						row.add(rs.getObject(i+1));
					}
					returned.add(row);
				}
			}
			return returned;
		}catch(Exception e){
			throw new Exception("Exception in select: " + e.getMessage());
		}finally{
			ServerConfig.conn.close();
		}
	}

    public static ArrayList<ArrayList<Object>> select(String sql, Object ... args) throws Exception{
        return select(sql, params(args));
    }
}

package gov.nist.csd.pm.common.constants;

import gov.nist.csd.pm.common.config.EnvironmentFunction;


public class GlobalConstants {
	public static final int PM_DEFAULT_SERVER_PORT = 8080;
	public static final int PM_DEFAULT_SIMULATOR_PORT = 8085;
	public static final int PM_DEFAULT_EXPORTER_PORT = 8086;

	public static final int PM_MAX_BLOCK_SIZE = 1024;
	public static final int PM_MAX_DISPLAYED_ASCS = 30;
	public static final String PM_FAILURE = "err ";
	public static final String PM_SUCCESS = "ok  ";
	public static final String PM_CMD = "cmd ";
	public static final String PM_EOC = "eoc ";
	public static final String PM_ARG = "arg ";
	public static final String PM_SEP = "sep ";
	public static final String PM_DATA = "data";
	public static final String PM_EOD = "eod ";
	public static final String PM_BYE = "bye ";
	public static final String PM_PROP_DELIM = "=";
	public static final String PM_FIELD_DELIM = ":";
	public static final String PM_ALT_FIELD_DELIM = "|";
	public static final String PM_ALT_DELIM_PATTERN = "\\|";
	public static final String PM_TERMINATOR = ".";
	public static final String PM_LIST_MEMBER_SEP = ",";
	public static final String PM_IMPORT_COMMENT_START = "#";
	public static final String PM_CONNECTOR_NAME = "PM";
	public static final String PM_CONNECTOR_ID = "1";
	public static final String PM_ADMIN_NAME = "admin";
	public static final String PM_ADMIN_ID = "2";
	public static final String PM_SUPER_ADMIN_NAME = "superAdmin";
	public static final String PM_SUPER_ADMIN_ID = "3";
	public static final String PM_SUPER_NAME = "super";
	public static final String PM_SUPER_PASS = "super";
	public static final String PM_SUPER_ID = "4";
	public static final String PM_EVERYTHING_NAME = "everything";
	public static final String PM_EVERYTHING_ID = "5";
	public static final String PM_EVERYTHING_ASSOC_ID = "6";
	public static final String PM_ALL_OPS_NAME = "all ops";
	public static final String PM_ALL_OPS_ID = "7";
	// Classes of actual objects
	public static final String PM_CLASS_CLASS_NAME = "class";
	public static final String PM_CLASS_CLASS_ID = "1";
	public static final String PM_CLASS_FILE_NAME = "File";
	public static final String PM_CLASS_FILE_ID = "2";
	public static final String PM_CLASS_DIR_NAME = "Directory";
	public static final String PM_CLASS_DIR_ID = "3";
	public static final String PM_CLASS_USER_NAME = "User";
	public static final String PM_CLASS_USER_ID = "4";
	public static final String PM_CLASS_UATTR_NAME = "User attribute";
	public static final String PM_CLASS_UATTR_ID = "5";
	public static final String PM_CLASS_OBJ_NAME = "Object";
	public static final String PM_CLASS_OBJ_ID = "6";
	public static final String PM_CLASS_OATTR_NAME = "Object attribute";
	public static final String PM_CLASS_OATTR_ID = "7";
	public static final String PM_CLASS_CONN_NAME = "Connector";
	public static final String PM_CLASS_CONN_ID = "8";
	public static final String PM_CLASS_POL_NAME = "Policy class";
	public static final String PM_CLASS_POL_ID = "9";
	public static final String PM_CLASS_OPSET_NAME = "Operation set";
	public static final String PM_CLASS_OPSET_ID = "10";
	public static final String PM_CLASS_ANY_NAME = "*";
	public static final String PM_CLASS_ANY_ID = "11";
	public static final String PM_CLASS_CLIPBOARD_NAME = "Clipboard";
	public static final String PM_CLASS_CLIPBOARD_ID = "12";
	public static final String PM_CLASS_RECORD_NAME = "Record";
	public static final String PM_CLASS_RECORD_ID = "13";
	public static final String PM_CLASS_SESSION_NAME = "Session";
	public static final String PM_CLASS_SESSION_ID = "14";
	public static final String PM_CLASS_CREATE_CLASS = "Class create class";
	public static final String PM_CLASS_DELETE_CLASS = "Class delete class";
	/**
	 * @uml.property  name="sClassOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sClassOps[] = {PM_CLASS_CREATE_CLASS, PM_CLASS_DELETE_CLASS};
	public static final String PM_DIR_MODIFY = "Dir modify";
	public static final String PM_DIR_READEXEC = "Dir read and execute";
	public static final String PM_DIR_LIST = "Dir list contents";
	public static final String PM_DIR_READ = "Dir read";
	public static final String PM_DIR_WRITE = "Dir write";
	/**
	 * @uml.property  name="sDirOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sDirOps[] = {PM_DIR_MODIFY, PM_DIR_READEXEC, PM_DIR_LIST,
			PM_DIR_READ, PM_DIR_WRITE};
	public static final String PM_FILE_MODIFY = "File modify";
	public static final String PM_FILE_READEXEC = "File read and execute";
	public static final String PM_FILE_READ = "File read";
	public static final String PM_FILE_WRITE = "File write";
	/**
	 * @uml.property  name="sFileOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sFileOps[] = {PM_FILE_MODIFY, PM_FILE_READEXEC,
			PM_FILE_READ, PM_FILE_WRITE};
	public static final String PM_ENTITY_REPRESENT = "Entity represent";
	public static final String PM_USER_CREATE_UATTR = "User create user attribute";
	public static final String PM_USER_ASSIGN = "User assign";
	public static final String PM_USER_DELETE = "User delete";
	public static final String PM_USER_DELETE_ASSIGN = "User delete assign";
	/**
	 * @uml.property  name="sUserOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sUserOps[] = {PM_USER_CREATE_UATTR, PM_USER_ASSIGN,
			PM_USER_DELETE, PM_USER_DELETE_ASSIGN, PM_ENTITY_REPRESENT};
	public static final String PM_UATTR_CREATE_UATTR = "User attribute create user attribute";
	public static final String PM_UATTR_CREATE_USER = "User attribute create user";
	public static final String PM_UATTR_DELETE_USER = "User attribute delete user";
	public static final String PM_UATTR_CREATE_OPSET = "User attribute create operation set";
	public static final String PM_UATTR_ASSIGN_TO_OPSET = "User attribute assign to operation set";
	public static final String PM_UATTR_ASSIGN = "User attribute assign";
	public static final String PM_UATTR_ASSIGN_TO = "User attribute assign to";
	public static final String PM_UATTR_DELETE = "User attribute delete";
	public static final String PM_UATTR_DELETE_ASSIGN = "User attribute delete assign";
	public static final String PM_UATTR_DELETE_ASSIGN_TO = "User attribute delete assign to";
	/**
	 * @uml.property  name="sUattrOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sUattrOps[] = {PM_UATTR_CREATE_UATTR, PM_UATTR_CREATE_USER,
			PM_UATTR_DELETE_USER, PM_UATTR_CREATE_OPSET,
			PM_UATTR_ASSIGN_TO_OPSET, PM_UATTR_ASSIGN, PM_UATTR_ASSIGN_TO,
			PM_UATTR_DELETE, PM_UATTR_DELETE_ASSIGN, PM_UATTR_DELETE_ASSIGN_TO,
			PM_ENTITY_REPRESENT};
	public static final String PM_OBJ_DELETE = "Object delete";
	/**
	 * @uml.property  name="sObjOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sObjOps[] = {PM_OBJ_DELETE};
	public static final String PM_OATTR_CREATE_OBJ = "Object attribute create object";
	public static final String PM_OATTR_DELETE_OBJ = "Object attribute delete object";
	public static final String PM_OATTR_CREATE_OATTR = "Object attribute create object attribute";
	public static final String PM_OATTR_DELETE_OATTR = "Object attribute delete object attribute";
	public static final String PM_OATTR_CREATE_OPSET = "Object attribute create operation set";
	public static final String PM_OATTR_ASSIGN = "Object attribute assign";
	public static final String PM_OATTR_ASSIGN_TO = "Object attribute assign to";
	public static final String PM_OATTR_DELETE = "Object attribute delete";
	public static final String PM_OATTR_DELETE_ASSIGN = "Object attribute delete assign";
	public static final String PM_OATTR_DELETE_ASSIGN_TO = "Object attribute delete assign to";
	/**
	 * @uml.property  name="sOattrOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sOattrOps[] = {PM_OATTR_CREATE_OBJ, PM_OATTR_DELETE_OBJ,
			PM_OATTR_CREATE_OATTR, PM_OATTR_DELETE_OATTR,
			PM_OATTR_CREATE_OPSET, PM_OATTR_ASSIGN, PM_OATTR_ASSIGN_TO,
			PM_OATTR_DELETE, PM_OATTR_DELETE_ASSIGN, PM_OATTR_DELETE_ASSIGN_TO,
			PM_ENTITY_REPRESENT};
	public static final String PM_POL_CREATE_UATTR = "Policy class create user attribute";
	public static final String PM_POL_DELETE_UATTR = "Policy class delete user attribute";
	public static final String PM_POL_CREATE_OATTR = "Policy class create object attribute";
	public static final String PM_POL_DELETE_OATTR = "Policy class delete object attribute";
	public static final String PM_POL_CREATE_OBJ = "Policy class create object";
	public static final String PM_POL_ASSIGN = "Policy class assign";
	public static final String PM_POL_ASSIGN_TO = "Policy class assign to";
	public static final String PM_POL_DELETE = "Policy class delete";
	public static final String PM_POL_DELETE_ASSIGN = "Policy class delete assign";
	public static final String PM_POL_DELETE_ASSIGN_TO = "Policy class delete assign to";
	/**
	 * @uml.property  name="sPolOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sPolOps[] = {PM_POL_CREATE_UATTR, PM_POL_DELETE_UATTR,
			PM_POL_CREATE_OATTR, PM_POL_DELETE_OATTR, PM_POL_CREATE_OBJ,
			PM_POL_ASSIGN, PM_POL_ASSIGN_TO, PM_POL_DELETE,
			PM_POL_DELETE_ASSIGN, PM_POL_DELETE_ASSIGN_TO, PM_ENTITY_REPRESENT};
	public static final String PM_OPSET_ASSIGN = "Operation set assign";
	public static final String PM_OPSET_ASSIGN_TO = "Operation set assign to";
	public static final String PM_OPSET_DELETE = "Operation set delete";
	public static final String PM_OPSET_DELETE_ASSIGN = "Operation set delete assign";
	public static final String PM_OPSET_DELETE_ASSIGN_TO = "Operation set delete assign to";
	/**
	 * @uml.property  name="sOpsetOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sOpsetOps[] = {PM_OPSET_ASSIGN, PM_OPSET_ASSIGN_TO,
			PM_OPSET_DELETE, PM_OPSET_DELETE_ASSIGN, PM_OPSET_DELETE_ASSIGN_TO,
			PM_ENTITY_REPRESENT};
	public static final String PM_CONN_CREATE_POL = "Connector create policy class";
	public static final String PM_CONN_DELETE_POL = "Connector delete policy class";
	public static final String PM_CONN_CREATE_USER = "Connector create user";
	public static final String PM_CONN_DELETE_USER = "Connector delete user";
	public static final String PM_CONN_CREATE_UATTR = "Connector create user attribute";
	public static final String PM_CONN_DELETE_UATTR = "Connector delete user attribute";
	public static final String PM_CONN_CREATE_OATTR = "Connector create object attribute";
	public static final String PM_CONN_DELETE_OATTR = "Connector delete object attribute";
	public static final String PM_CONN_CREATE_OBJ = "Connector create object";
	public static final String PM_CONN_CREATE_OPSET = "Connector create operation set";
	public static final String PM_CONN_ASSIGN_TO = "Connector assign to";
	public static final String PM_CONN_DELETE_ASSIGN_TO = "Connector delete assign to";
	/**
	 * @uml.property  name="sConnOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sConnOps[] = {PM_CONN_CREATE_POL, PM_CONN_DELETE_POL,
			PM_CONN_CREATE_USER, PM_CONN_DELETE_USER, PM_CONN_CREATE_UATTR,
			PM_CONN_DELETE_UATTR, PM_CONN_CREATE_OATTR, PM_CONN_DELETE_OATTR,
			PM_CONN_CREATE_OBJ, PM_CONN_CREATE_OPSET, PM_CONN_ASSIGN_TO,
			PM_CONN_DELETE_ASSIGN_TO, PM_ENTITY_REPRESENT};
	public static final String PM_ANY_ANY = "*";
	/**
	 * @uml.property  name="sAnyOps" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sAnyOps[] = {PM_ANY_ANY};
	public static final String PM_GRAPH_UATTR = "ua";
	public static final String PM_GRAPH_CAPS = "ca";
	public static final String PM_GRAPH_OATTR = "oa";
    public static final String PM_GRAPH_OA_ONLY = "oa_only";
	public static final String PM_GRAPH_ACES = "ac";
	public static final String PM_GRAPH_PERMS = "pe";
	public static final String PM_ID = "i";
	public static final String PM_SELECTED = "s";
	public static final String PM_ARC = "r";
	public static final String PM_HOST = "h";
	public static final String PM_OBJ_CLASS = "oc";
	public static final String PM_OP = "op";
	public static final String PM_OBJ = "ob";
	public static final String PM_COMPL_OATTR = "cb";
	public static final String PM_ASET = "as";
	public static final String PM_SAC = "sac";
	public static final String PM_SESSION = "ses";
	public static final String PM_PROCESS = "proc";
	public static final String PM_RULE = "rule";
	public static final String PM_PERM = "perm";
	public static final String PM_APP_PATH = "app";
	public static final String PM_KS_PATH = "ks";
	public static final String PM_DENY = "deny";
	public static final String PM_EMAIL_ACCT = "eml";
	public static final String PM_TEMPLATE = "tpl";
	public static final String PM_KEY = "key";
	public static final String PM_CONTAINERS = "conts";
	public static final String PM_COMPONENTS = "comps";
	public static final String PM_FULL_NAME = "fn";
	public static final String PM_VALUE = "v";
	public static final String PM_RECORD = "rec";
	public static final String PM_DENY_USER_ID = "user id";
	public static final String PM_DENY_USER_SET = "user set";
	public static final String PM_DENY_SESSION = "session";
	public static final String PM_DENY_PROCESS = "process";
	public static final String PM_DENY_INTRA_SESSION = "intra session";
	public static final String PM_DENY_ACROSS_SESSIONS = "across sessions";
	public static final String PM_FUN = "f";
	public static final String PM_UNKNOWN = "k";
	public static final String PM_SCRIPT = "scr";
	public static final String PM_PROP = "prop";
	public static final String PM_TASK = "tk";
	public static final String PM_SCON = "sc";
	public static final String PM_SCONA = "sca";
	public static final String PM_CAP = "cap";
	public static final String PM_THRESHOLD = "th";
	public static final String PM_LABEL = "l";
	public static final String PM_EVENT_CREATE = "create";
	public static final String PM_EVENT_DELETE = "delete";
	// Events.
	public static final String PM_EVENT_OBJECT_CREATE = "Object create";
	public static final String PM_EVENT_OBJECT_DELETE = "Object delete";
	public static final String PM_EVENT_OBJECT_READ = "Object read";
	public static final String PM_EVENT_OBJECT_WRITE = "Object write";
	public static final String PM_EVENT_USER_CREATE = "User create";
	public static final String PM_EVENT_SESSION_CREATE = "Session create";
	public static final String PM_EVENT_SESSION_DELETE = "Session delete";
	public static final String PM_EVENT_OBJECT_SEND = "Object send";
	/**
	 * @uml.property  name="sEventNames" multiplicity="(0 -1)" dimension="1"
	 */
	public static final  String sEventNames[] = {PM_EVENT_OBJECT_CREATE,
			PM_EVENT_OBJECT_DELETE, PM_EVENT_OBJECT_READ,
			PM_EVENT_OBJECT_WRITE, PM_EVENT_USER_CREATE,
			PM_EVENT_SESSION_CREATE, PM_EVENT_SESSION_DELETE,
			PM_EVENT_OBJECT_SEND};
	// Requested information about events and their processing.
	public static final String PM_INFO_USER_SELECTS_CONTAINERS = "User selects containers";
	public static final String PM_SELECTION_MULTIPLE = "multiple";
	public static final String PM_SELECTION_SINGLE = "single";
	public static final String PM_VOS_PRES_ADMIN = "admin";
	public static final String PM_VOS_PRES_USER = "user";
	
	public static final EnvironmentFunction[] evrFunctions = {
			new EnvironmentFunction("object_new", "ob", null),
			new EnvironmentFunction("oattr_with_name_of_active_attr", "b", "p"),
			new EnvironmentFunction("oattr_corresponding_to_active_attr", "b", "p"),
			new EnvironmentFunction("oattr_of_user_choice", "b", "p"),
			new EnvironmentFunction("oattr_of_default_user", "b", null),
			new EnvironmentFunction("oattr_home_of_default_user", "b", null),
			new EnvironmentFunction("user_default", "u", null),
			new EnvironmentFunction("prop_home_of_new_user", "k", null),
			new EnvironmentFunction("uattr_name_of_new_user", "a", null),
			new EnvironmentFunction("prop_name_of_new_user", "k", null),
			new EnvironmentFunction("user_new", "u", null),
			new EnvironmentFunction("uattr_name_of_user", "a", "u"),
			new EnvironmentFunction("prop_name_of_user", "k", "u"),
			new EnvironmentFunction("pol_discr", "p", null),
			new EnvironmentFunction("pol_id", "p", null),
			new EnvironmentFunction("pol_with_prop", "p", "k"),
			new EnvironmentFunction("oattr_home_of_new_user", "b", null),
			new EnvironmentFunction("oattr_home_of_user", "b", "u"),
			new EnvironmentFunction("obj_rep_of_home_of_new_user", "ob", null),
			new EnvironmentFunction("obj_rep_of_home_of_user", "ob", "u"),
			new EnvironmentFunction("oattr_rep_of_home_of_new_user", "b", null),
			new EnvironmentFunction("oattr_rep_of_home_of_user", "b", "u"),
			new EnvironmentFunction("oattr_rep_of_discr_users", "b", null),
			new EnvironmentFunction("uattr_discr_users", "a", null),
			new EnvironmentFunction("oattr_of_default_obj", "b", null),
			new EnvironmentFunction("oattr_record_of_default_obj", "b", null),
			new EnvironmentFunction("oattr_record_of_oattr", "b", "b"),
			new EnvironmentFunction("uattr_lowest_level", "a", null),
			new EnvironmentFunction("oattr_direct_asc_of_and_containing", "b", "b,b"),
			new EnvironmentFunction("uattr_direct_ascs_of_uattr", "a", "a"),
			new EnvironmentFunction("uattr_direct_ascs_of_uattr_except", "a", "a,a"),
			new EnvironmentFunction("uattr_active_in_default_session_and_in_uattr",
					"a", "a"), new EnvironmentFunction("prop_discr_users", "k", null),
			new EnvironmentFunction("obj_rep_of_discr_users", "ob", null),
			new EnvironmentFunction("oattr_witems_of_new_user", "b", null),
			new EnvironmentFunction("prop_witems_of_new_user", "k", null),
			new EnvironmentFunction("oattr_inbox_of_new_user", "b", null),
			new EnvironmentFunction("oattr_winbox_of_new_user", "b", null),
			new EnvironmentFunction("oattr_inbox_of_user", "b", "u"),
			new EnvironmentFunction("oattr_winbox_of_user", "b", "u"),
			new EnvironmentFunction("oattr_outbox_of_new_user", "b", null),
			new EnvironmentFunction("oattr_outbox_of_user", "b", "u"),
			new EnvironmentFunction("prop_inbox_of_new_user", "k", null),
			new EnvironmentFunction("prop_inbox_of_user", "k", "u"),
			new EnvironmentFunction("prop_outbox_of_new_user", "k", null),
			new EnvironmentFunction("prop_outbox_of_user", "k", "u"),
			new EnvironmentFunction("obj_rep_of_inbox_of_new_user", "ob", null),
			new EnvironmentFunction("obj_rep_of_inbox_of_user", "ob", "u"),
			new EnvironmentFunction("oattr_rep_of_inbox_of_new_user", "b", null),
			new EnvironmentFunction("oattr_rep_of_inbox_of_user", "b", "u"),
			new EnvironmentFunction("obj_rep_of_outbox_of_new_user", "ob", null),
			new EnvironmentFunction("obj_rep_of_outbox_of_user", "ob", "u"),
			new EnvironmentFunction("oattr_rep_of_outbox_of_new_user", "b", null),
			new EnvironmentFunction("oattr_rep_of_outbox_of_user", "b", "u"),
			new EnvironmentFunction("user_recipient", "u", null),
			new EnvironmentFunction("oattr_inboxes", "b", null),
			new EnvironmentFunction("oattr_outboxes", "b", null),
			new EnvironmentFunction("oattr_witems", "b", null),
			new EnvironmentFunction("session_default", "ses", null),
			new EnvironmentFunction("rule_composed_of", "rule", "k"),
			new EnvironmentFunction("id_or_name_as_string", "k", "k"),
			new EnvironmentFunction("name_of_rep_of_oattr", "b", "k"),
			new EnvironmentFunction("oattr_rep_of_oattr", "b", "b"),
			new EnvironmentFunction("obj_rep_of_oattr", "ob", "b"),
			new EnvironmentFunction("process_default", "proc", null)
	};
	
	public static final String PM_SCHEMA = "policydb";

}

package gov.nist.csd.pm.server;

import java.util.HashSet;
import java.util.jar.Pack200.Packer;

import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketException;
import gov.nist.csd.pm.server.dao.MySQL.CommonSQLDAO;

/**
 * @author gopi.katwala@nist.gov
 * @since 2.0
 */

public class TestClient {

	
	public static void main(String[] args) {
		CommonSQLDAO dao = null;
		try {
			dao = new CommonSQLDAO();
		} catch (Exception e) {
			e.printStackTrace();
		}

// **********************************************************************		
//		Packet addUattrInternal(String sName, String sDescr, String sInfo,
//		String sBaseId, String sBaseType, String[] sProps) 
//		String props[] = new String[4];
//		props[0] = "user=katie";
//		props[1] = "user=tom";
//		props[2] = "user=bob";
//		props[3] = "user=marry";
//		System.out.println("Calling addUserInternal method from SQLDAO" );
//		dao.addUattrInternal("testUA", "testUA", "testUA","2", "p",props);
//		String newUserId = dao.getEntityId("testUA",null);
//		System.out.println("addUattrInternal is successful for id "+ newUserId);
		
// **********************************************************************		
//		boolean objClassHasOp(String sClassId, String sOp) 
//		System.out.println("Calling objClassHasOp method from SQLDAO" );
//		boolean hasOp = dao.objClassHasOp("9", "File read");
//		System.out.println("objClassHasOp is successful - result is " + hasOp);
		
		
//		public addOpsetAndOpInternal(String sOpsetName, String sOpsetId,
//			String sDescr, String sInfo, String sOp, String sAscId,
//			String sAscType, String sDescId, String sDescType)
		
//		System.out.println("Calling addOpsetAndOpInternal method from SQLDAO" );
//		dao.addOpsetAndOpInternal("tempOpset", null, "tempOpset","tempOpset", "File read","30","a","5","b");
//		System.out.println("addOpsetAndOpInternal is successful.");
//		public Packet addHost(String sSessId, String sHost, String sRepo,
//				String sReserved, String sIpa, String sDescr, String sPdc)
//		dao.addHost("temp", "Temp","Temp","Temp","Temp","Temp","true");
		
		//testGetDescOattrsInPc(dao);
		//testGetOpsetInfo(dao);
		//testGetOpsetsbetween(dao);
		//testAddEmailAcctInternal(dao);
		//testCreateOpsetBetween(dao);
		//testGetHostInfo(dao);
		//testOpsetIsAscendant(dao);
		//testAddTemplate(dao);
		//testGetMemberUsers(dao);
		//testGetAscAttrs(dao);
		//testAddObjClassAndOp(dao);
		//testDeleteOpsetAndOp(dao);
		//testDeleteUattr(dao);
		//testDeleteOattr(dao);
		//testGetPcInfo(dao);
		//testSetOpsetOps(dao);
		//testGetMembersOf(dao);
		testGetContainersOf(dao);
	}
	
	public static void testGetDescOattrsInPc(CommonSQLDAO dao){
		System.out.println(dao.getDascUattrs("all ops", "s"));
	}
	
	public static void testGetOpsetOps(CommonSQLDAO dao){
		System.out.println(dao.getOpsetOps(null, "all ops"));
	}
	
	public static void testGetOpsetInfo(CommonSQLDAO dao){
		Packet p = dao.getOpsetInfo(null, "7");
		for(int i = 0; i < p.size(); i++){
			System.out.println(p.getStringValue(i));
		}
	}
	
	public static void testGetOpsetsbetween(CommonSQLDAO dao){
		Packet p = new Packet();
		try {
			dao.getOpsetsBetween("3", "5", p);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i = 0; i < p.size(); i++){
			System.out.println(p.getStringValue(i));
		}
	}
	
	public static void testAddEmailAcctInternal(CommonSQLDAO dao){
		dao.addEmailAcctInternal("super", "SuperFirst", "super@mail", "", "", "", "password");
		
	}
	
	public static void testCreateOpsetBetween(CommonSQLDAO dao){
		HashSet<String> opSet = new HashSet<String>();
		opSet.add("*");
		Packet p = dao.createOpsetBetween(null, null, opSet, "9", "5");
		for(int i = 0; i < p.size(); i++){
			System.out.println(p.getStringValue(i));
		}
	}
	
	public static void testGetHostInfo(CommonSQLDAO dao){
		Packet p = dao.getHostInfo("", "3");
		for(int i = 0; i < p.size(); i++){
			System.out.println(p.getStringValue(i));
		}
	}
	
	public static void testOpsetIsAscendant(CommonSQLDAO dao){
		try {
			System.out.println(dao.opsetIsAscendant("7", "5", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testAddTemplate(CommonSQLDAO dao){
		//dao.addTemplate(null, "tpl1", "TableA", "TableA");
		try {
			System.out.println(dao.getEntityId("table1234", "b"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testGetMemberUsers(CommonSQLDAO dao){
		HashSet<Integer> users = new HashSet<Integer>();
		try {
			dao.getMemberUsers("1", users);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(users);
	}
	
	public static void testGetAscAttrs(CommonSQLDAO dao){
		HashSet<String> attrs = new HashSet<String>();
		try {
			//dao.getAscAttrs("2", "b", attrs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(attrs);
	}
	
	public static void testAddObjClassAndOp(CommonSQLDAO dao){
		Packet p = dao.addObjClassAndOp(null, "NEWCLASS", "123", "test", "testOp3");
	}
	
	public static void testDeleteOpsetAndOp(CommonSQLDAO dao){
		Packet p = dao.deleteOpsetAndOp(null, "37", "testOp2");
	}
	
	public static void testDeleteUattr(CommonSQLDAO dao){
		dao.deleteUattr(null, "3");
	}
	
	public static void testDeleteUser(CommonSQLDAO dao){
		dao.deleteUser(null, "39");
	}
	
	public static void testDeleteOattr(CommonSQLDAO dao){
//		dao.addUattr("12", null, null, "uatest", 
//				"", "", "132", "a", 
//				"no", new String[]{"prop"});
//		dao.addUattr("12", null, null, "uatest", 
//				"", "", "133", "a", 
//				"no", new String[]{"prop"});
		//dao.deleteUattr(null, "136");
		dao.assign("136", "a", "137", "a", null, null);
		
	}
	
	public static void testDeleteObject(CommonSQLDAO dao){
		try {
			dao.deleteAssignment("134", "a", "3", "a", null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testGetPcInfo(CommonSQLDAO dao){
		Packet p = dao.getPcInfo(null, "2", "no");
		for(int i = 0; i < p.size(); i++){
			try {
				System.out.println(p.getItemStringValue(i));
			} catch (PacketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void testSetOpsetOps(CommonSQLDAO dao){
		try {
			dao.setOpsetOps("7", "*");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testGetMembersOf(CommonSQLDAO dao){
		Packet p = dao.getMembersOf(null, "TableA", "60", "b", "ac");
		for(int i = 0; i < p.size(); i++){
			System.out.println(p.getStringValue(i));
		}
	}
	
	public static void testGetContainersOf(CommonSQLDAO dao) {
		Packet p = dao.getContainersOf(null, "Table123", "124", "b", "ac");
		for(int i = 0; i < p.size(); i++){
			System.out.println(p.getStringValue(i));
		}
	}
	
	
	
	
	//addOpsetToSet
}




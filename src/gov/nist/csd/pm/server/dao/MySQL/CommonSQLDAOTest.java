/*
package gov.nist.csd.pm.server.dao.MySQL;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketException;
import gov.nist.csd.pm.common.util.CommandUtil;
import gov.nist.csd.pm.server.PmEngine;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;

import static org.junit.Assert.*;

public class CommonSQLDAOTest {

    public static CommonSQLDAO dao;

    @BeforeClass
    public static void init() throws Exception{
        System.out.println(ServerConfig.SQLDAO);
        dao = ServerConfig.getSQLDAO();
        dao.reset(null);
    }

    @After
    public void cleanUp() throws Exception{
        //dao.reset(null);
    }

    private int createNode(String name){
        try {
            return dao.createNode(null, name, "b", "", 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    @Test
    public void testCreateNode(){
        String errorMessage = "";
        String actual = "";
        try {
            errorMessage = "createNode did not work";
            System.out.println("before create");
            Integer newNodeId = dao.createNode(null, "name", "b", "", 2);
            System.out.println("newId: " + newNodeId);
            System.out.println("after create");
            actual = dao.getEntityName(newNodeId.toString(), "b");
            System.out.println("after getEntityName");
        }catch(Exception e) {
            e.printStackTrace();
        }

        assertEquals(errorMessage, "name", actual);

    }

    @Test (expected = Exception.class)
    public void testCreateNodeException() throws Exception{
        dao.createNode(null, "newName", "b", "", 2);
        dao.createNode(null, "newName", "b", "", 2);
    }

    @Test
    public void testDeleteNode() throws Exception {
        String errorMessage = "";
        try {
            errorMessage = "deleteNode did not work";
            dao.deleteNode(Integer.valueOf(dao.getEntityId("name", "b")));
            assertNull("deleteNode did not work", dao.getEntityId("name", "b"));
        }catch(Exception e){
        }
    }

    @Test (expected = Exception.class)
    public void testDeleteNodeException() throws Exception{
        dao.deleteNode(Integer.valueOf(dao.getEntityId("name", "b")));
    }

    @Test (expected = Exception.class)
    public void testDeleteNodeWithNullId() throws Exception {
        dao.deleteNode(null);
    }

    @Test
    public void testIsAssigned(){
        String errorMessage = "";
        boolean actual = false;
        int id = 0;
        try {
            errorMessage = "createNode in testIsAssigned //failed";
            id = dao.createNode(null, "isAssigned", "b", "", 2);
            errorMessage = "isAssigned 1 //failed";
            actual = dao.isAssigned(id, 2);
        }catch(Exception e){
        }
        assertTrue(errorMessage, actual);

        try{
            errorMessage = "isAssigned did not work 2";
            actual = dao.isAssigned(id, 5);
        }catch(Exception e){
        }
        assertFalse(errorMessage, actual);
    }

    @Test (expected = Exception.class)
    public void testIsAssignedException() throws Exception{
        dao.isAssigned(null, 5);
    }

    @Test
    public void testHasAscendants() throws Exception {
        boolean expected = true;
        boolean actual = dao.hasAscendants("2");
        assertEquals("hasAscendants did not work 1", expected, actual);

        expected = false;
        actual = dao.hasAscendants(dao.getEntityId("name", "b"));
        assertEquals("hasAscendants did not work 2", expected, actual);

        actual = dao.hasAscendants(null);
        assertEquals("hasAscendants did not work 3", expected, actual);

        actual = dao.hasAscendants("9999");
        assertEquals("hasAscendants did not work 4", expected, actual);
    }

    @Test
    public void testHasDescendants() throws Exception {
        boolean expected = true;
        boolean actual = dao.hasDescendants("5");
        assertEquals("hasDescendants did not work 1", expected, actual);

        expected = false;
        actual = dao.hasDescendants(dao.getEntityId("PM", "c"));
        assertEquals("hasDescendants did not work 2", expected, actual);

        actual = dao.hasDescendants(null);
        assertEquals("hasDescendants did not work 3", expected, actual);

        actual = dao.hasDescendants("9999");
        assertEquals("hasDescendants did not work 4", expected, actual);
    }

    @Test
    public void testAssign(){
        int id = 0;
        boolean actual = false;
        try {
            id = dao.createNode(null, "testAssign", "b", "", 2);
            dao.assign(5, id);
            actual = dao.isAssigned(id, 5);
        } catch (Exception e) {}
        assertTrue("testAssign failed", actual);
    }

    @Test (expected = Exception.class)
    public void testAssignException() throws Exception {
        int id = dao.createNode(null, "testAssignExcep", "b", "", 2);
        dao.assign(5, id);
        dao.assign(5, id);
    }

    @Test (expected = Exception.class)
    public void testAssignExceptionWithNullParams() throws Exception {
        dao.assign(new Integer(null), null);
    }

    @Test
    public void testGetContainerList(){
        String actual = "";
        String expected = "";
        try {
            expected = "2";
            actual = dao.getContainerList("5", "b");
        }catch(Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getContainerList returned wrong list for OA", expected, actual);

        try {
            expected = "2";
            actual = dao.getContainerList("3", "a");
        }catch(Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getContainerList returned wrong list for UA", expected, actual);

        try {
            expected = "3";
            actual = dao.getContainerList("4", "u");
        }catch(Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getContainerList returned wrong list user", expected, actual);

        try {
            actual = dao.getContainerList("7", "s");
        }catch(Exception e) {
            fail(e.getMessage());
        }
        assertNull("getContainerList returned a list for an opset", actual);

        try {
            actual = dao.getContainerList("2", "p");
        }catch(Exception e) {
            fail(e.getMessage());
        }
        assertNull("getContainerList returned a list when there is nothing there", actual);
    }

    @Test
    public void testEntityExists(){
        boolean actual = false;
        try{
            actual = dao.entityExists("5", "b");
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertTrue("entity does exist", actual);

        try{
            actual = dao.entityExists("everything", "b");
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertTrue("entity does exist", actual);

        try{
            actual = dao.entityExists("500", "b");
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertFalse("entity does not exist", actual);

        try{
            actual = dao.entityExists(null, "b");
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertFalse("entity does not exist", actual);
    }

    @Test
    public void testAddPcInternal(){
        Packet p = dao.addPcInternal("testPc", null, "", "", new String[] {"prop=newPc"});
        boolean result = false;
        try{
            result = p.getItemType(0)== ItemType.RESPONSE_ERROR;
        }catch(Exception e){

        }
        assertNull("addPcInternal had an error", p.getErrorMessage());
        assertFalse("addPcInternal had error response", result);
    }

    @Test
    public void testAddDoubleLink(){
        int id = createNode("testAddDL");
        Packet p = dao.addDoubleLink(String.valueOf(id), null, String.valueOf(5), null);
        boolean result = false;
        try{
            result = p.getItemType(0)==ItemType.RESPONSE_SUCCESS;
        }catch(Exception e){
            fail(e.getMessage());
        }

        assertTrue(result);
    }

    @Test
    public void testAddUserInternal(){
        Packet p = dao.addUserInternal("bob", null, "Robert", "", "bob", "3", "a");
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertEquals("user was not created correctly", "bob", result);
    }

    //addObjectInternal tests
    @Test
    public void testAddOjectInternalPlainObject(){
        Packet p = dao.addObjectInternal("object1", null, null, "", "", "5", "b", "File",
                "o", "P645799", "C:\\PMWorkarea", null, null, false, null,
                null, null, null, null, null, null);
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertEquals("object was not created correctly", "object1", result);
    }

    @Test
    public void testAddObjectInternalEmailObject(){
        Packet p = dao.addObjectInternal("object1", null, null, "", "", "5", "b", "File",
                "o", "P645799", "C:\\PMWorkarea", null, null, false, "bob@nist.gov",
                "alice@nist.gov", "subject", "everything", null, null, null);
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertEquals("object was not created correctly", "object1", result);
    }

    @Test
    public void testAddObjectInternalTemplateObject(){
        Packet p = dao.addObjectInternal("tplObject", null, null, "", "", "5", "b", "File",
                "o", "P645799", "C:\\PMWorkarea", null, null, false, null,
                null, null, null, "138", null, null);
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertEquals("object was not created correctly", "tplObject", result);
    }
    //end addObjectInternal tests

    @Test
    public void testAddOpsetAndOpInternal(){
        Packet p = dao.addOpsetAndOpInternal("opset1", null, "", "", "File write",
                "3", "a", "5", "b");
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertEquals("opset1 was not created correctly", "opset1", result);

        p = dao.addOpsetAndOpInternal("opset2", null, "", "", null,
                "3", "a", null, null);
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertEquals("opset2 was not created correctly", "opset2", result);

        p = dao.addOpsetAndOpInternal("opset3", null, "", "", "File write",
                "3", "a", null, null);
        Integer opsetId = 0;
        try{
            result = p.getItemStringValue(0).split(":")[0];
            opsetId = Integer.valueOf(p.getItemStringValue(0).split(":")[1]);
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertEquals("opset3 was not created correctly", "opset3", result);

        p = dao.addOpsetAndOpInternal("opset3", opsetId.toString(), "", "", null,
                "3", "a", null, null);
        assertTrue("adding a null op to opset3 should have failed: " + p.getErrorMessage(), p.hasError());

        p = dao.addOpsetAndOpInternal("opset3", opsetId.toString(), "", "", "",
                "3", "a", null, null);
        assertTrue("adding an empty op to opset3 should have failed: " + p.getErrorMessage(), p.hasError());

        p = dao.addOpsetAndOpInternal("opset3", opsetId.toString(), "", "", "NOP",
                "3", "a", null, null);
        assertTrue("trying to add an invalid operation should have failed: " + p.getErrorMessage(), p.hasError());

        p = dao.addOpsetAndOpInternal("opset3", opsetId.toString(), "", "", "File write",
                null, null, null, null);
        assertTrue("opset should already contain this operation: " + p.getErrorMessage(), p.hasError());

    }

    @Test (expected = Exception.class)
    public void testGetOperationId() throws Exception {
        Integer actual = 0;
        try {
            actual = dao.getOperationId("File write");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("Expected id of 7 for File write", new Integer(7), actual);

        dao.getOperationId("NOP");
    }

    @Test (expected = Exception.class)
    public void testEntityNameExists()throws Exception{
        boolean actual = false;
        try{
            actual = dao.entityNameExists("everything", "b");
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertTrue("Entity name everything does exist", actual);

        try{
            actual = dao.entityNameExists("ENTITY", "b");
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertFalse("Entity name ENTITY does NOT exist", actual);

        dao.entityNameExists(null, "b");
    }

    @Test
    public void testAddHost(){
        Packet p = dao.addHost(null, "P645799", "REPO", "", "", "", "");
        assertTrue("Packet should have error because of duplicate host name", p.hasError());

        p = dao.addHost(null, "", "REPO", "", "", "", "");
        assertTrue("Packet should have error because of empty host name", p.hasError());

        p = dao.addHost(null, null, "REPO", "", "", "", "");
        assertTrue("Packet should have error because of null host name", p.hasError());

        p = dao.addHost(null, "HOST$#@!%^&*()<>/?{}|+=-_123", "REPO", "", "", "", "");
        assertTrue("Packet should have error because of invalid host name", p.hasError());

        p = dao.addHost(null, "HOST", null, "", "", "", "");
        assertTrue("Packet should have error because of null repo path", p.hasError());

        p = dao.addHost(null, "HOST", "", "", "", "", "");
        assertTrue("Packet should have error because of empty repo path", p.hasError());

        p = dao.addHost(null, "HOST", "REPO", "", "", "", "");
        assertFalse("Packet should not have an error", p.hasError());

    }

    private void loadConfig(){
        try {
            Packet cmd = CommandUtil.makeCmd("importConfiguration", null, new String[]{});
            BufferedReader in = new BufferedReader(new FileReader(new File("./conf/PMServerConfiguration-RBAC.pm")));
            String sLine;
            int nLineNo = 0;
            while ((sLine = in.readLine()) != null) {
                nLineNo++;
                sLine = sLine.trim();
                if (sLine.length() <= 0 || sLine.startsWith("#")) {
                    continue;
                }
                cmd.addItem(ItemType.CMD_ARG, sLine);
            }
            dao.importConfiguration(null, cmd);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testGetEntityId(){
        String actual = "";
        try{
            actual = dao.getEntityId("everything", "b");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("5", actual);

        try{
            actual = dao.getEntityId("*", "b");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("*", actual);

        try{
            actual = dao.getEntityId("File", "oc");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("2", actual);

        String expected = "";
        */
/*try{
            Packet p = dao.addDenyInternal("deny", "user set", "superAdmin",
                    "3", "File write", "everything", "5", false);
            expected = p.getItemStringValue(0).split(":")[1];
            actual = dao.getEntityId("deny", "deny");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getting deny id failed", expected, actual);*//*


        try{
            Packet p = dao.addTemplate(null, "tpl", "everything", "everything");
            actual = dao.getEntityId("tpl", "tpl");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull("getting template id failed", actual);
    }

    @Test
    public void testAddOattrInternal(){
        Packet p = new Packet();
        try{
            p = dao.addOattrInternal("testOa", null, "", "", "5", "b", null, new String[]{"prop=prop12"});
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertFalse("adding oattr failed", p.hasError());
    }

    @Test (expected = Exception.class)
    public void testIsOperation() throws Exception{
        boolean actual = false;
        try{
            actual = dao.isOperation("File write");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("File write is an operation", actual);

        try{
            actual = dao.isOperation("NOP");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertFalse("NOP is not an operation", actual);


        actual = dao.isOperation(null);
    }

    @Test (expected = Exception.class)
    public void testOpsetContainsOp() throws Exception{
        boolean actual = false;
        try{
            actual = dao.opsetContainsOp(7, "*");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("the opset does contain this op", actual);

        try{
            actual = dao.opsetContainsOp(7, "File write");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertFalse("the opset does not contain this op", actual);

        actual = dao.opsetContainsOp(7, "NOP");
    }

    @Test (expected = Exception.class)
    public void testAddOperationToOpset() throws Exception{
        boolean actual = false;
        try{
            dao.addOperationToOperationSet(7, 7);
            actual = dao.opsetContainsOp(7, "File write");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue(actual);

        dao.addOperationToOperationSet(7, 1000);
    }

    @Test (expected = Exception.class)
    public void testObjClassHasOp() throws Exception{
        boolean actual = false;
        try{
            actual = dao.objClassHasOp("2", "File write");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("the object class does have this op", actual);

        try{
            actual = dao.objClassHasOp("2", "Object delete");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertFalse("the object class does not have this op", actual);

        actual = dao.opsetContainsOp(7, "NOP");
    }

    @Test (expected = Exception.class)
    public void testUserIsAssigned() throws Exception{
        boolean actual = false;
        try{
            actual = dao.userIsAssigned("4", "3");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("user is assigned to this uattr", actual);

        Packet p = dao.addUattrInternal("ua", "", "", "2", "p", new String[]{"props=props1"});
        try{
            String id = p.getItemStringValue(0).split(":")[1];
            actual = dao.userIsAssigned("4", id);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertFalse("user is NOT assigned to this uattr", actual);

        actual = dao.userIsAssigned("4", null);
    }

    @Test
    public void testOpsetIsAssignedToConnector(){
        boolean actual = false;
        try{
            actual = dao.opsetIsAssignedToConnector("7");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertFalse("opset is not assigned to the connector", actual);

        Packet p = dao.addOpsetAndOpInternal("opset1", null, "", "", "File write",
                "3", "a", null, null);
        try{
            String id = p.getItemStringValue(0).split(":")[1];
            actual = dao.opsetIsAssignedToConnector(id);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("opset is assigned to connector", actual);
    }

    //TODO can we delete a property just by its name or should we have value too?
    */
/*@Test
    public void testInsertProperties() throws Exception{
        String[] props = new String[]{"prop=test", "prop1=test1"};
        int id = 5;
        dao.insertProperties(props, id);
        HashSet<String> set = dao.getProps(String.valueOf(id), "b");
        assertTrue(set.contains("[prop=test]"));
        assertTrue(set.contains("[prop1=test1]"));

        dao.deleteProperty("prop=test");
        dao.deleteProperty("prop1=test1");
        dao.insertProperties(new String[]{}, id);
        set = dao.getProps("2", "b");
        assertTrue(set.isEmpty());

        dao.insertProperties(props, null);
        set = dao.getProps("2", "b");
        assertTrue(set.isEmpty());
    }

    @Test (expected = Exception.class)
    public void testInsertOrModifyProperties() throws Exception{
        dao.insertOrModifyProperty("prop3", "test3", 5);
        HashSet<String> set = dao.getProps(String.valueOf(5), "b");
        assertTrue(set.contains("[prop3=test3]"));
    }

    @Test
    public void testGetEntityName() {
        String errorMessage = "";
        try {
            errorMessage = "getEntityName with oattr did not work";
            assertEquals(errorMessage, "everything", dao.getEntityName("5", "b"));

            errorMessage = "getEntityName with oattr class did not work";
            assertEquals(errorMessage, "Object attribute", dao.getEntityName("7", "oc"));

            //need deny TODO
            //script
            //template

            errorMessage = "getEntityName with session did not work";
            //assertEquals(errorMessage, "super@P645799-4", dao.getEntityName("52", "ses"));

            errorMessage = "getEntityName should have returned null 1";
            assertNull(errorMessage, dao.getEntityName(null, "b"));

            errorMessage = "getEntityName should have returned null 2";
            assertNull(errorMessage, dao.getEntityName("5", ""));
        } catch (Exception e) {
            ////failerrorMessage);
        }
    }*//*


}*/

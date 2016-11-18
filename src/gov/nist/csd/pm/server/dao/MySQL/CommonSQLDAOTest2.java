package gov.nist.csd.pm.server.dao.MySQL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class CommonSQLDAOTest2 {

    public static CommonSQLDAO dao = ServerConfig.getSQLDAO();

    @BeforeClass
    public static void init() throws Exception{
        System.out.println(ServerConfig.SQLDAO);
        dao = ServerConfig.getSQLDAO();
        //dao.reset(null);
        //loadConfig();
        System.out.println("===========================Beginning of test============================");

    }

    @After
    public void cleanUp() throws Exception{
        //dao.reset(null);
        System.out.println("===========================End of test============================");
    }

    @Test
    public void testAddUattr(){
    	System.out.println("Testing addUattr");
    	Packet p = new Packet();
        p = dao.addUattr(null, "2", null, "testUserAttr", "testUserAttr", "testUserAttr", "2", "p", null, null);
        String result = null;
        try{
        	result = p.getItemStringValue(0).split(":")[0];
        	System.out.println("result is .."+result);
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "testUserAttr", result);
    }

   
    @Test
    public void testGetContainers(){
    	System.out.println("Testing getContainers");
    	Packet p = new Packet();
        p = dao.getContainers("3","a");
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "2", result);
    }

   
    @Test
    public void testGetUsersOf(){
    	System.out.println("Testing getUsersOf");
    	Packet p = new Packet();
        p = dao.getUsersOf("superAdmin");
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "super", result);
    }

    @Test
    public void testGetToAttrsUser(){
    	System.out.println("Testing getToAttrsUser");
    	Packet p = new Packet();
        p = dao.getToAttrsUser("4", null);
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "3", result);
    }

    @Test
    public void testGetToAttrs1(){
    	System.out.println("Testing getToAttrs1");
    	Packet p = new Packet();
        p = dao.getToAttrs1(null, "7");
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "5", result);
    }

    @Test
    public void testGetFromAttrs1(){
    	System.out.println("Testing getFromAttrs1");
    	Packet p = new Packet();
        p = dao.getFromAttrs1(null, "7");
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "3", result);
    }

    @Test
    public void testGetFromOpsets1(){
    	System.out.println("Testing getFromOpsets1");
    	Packet p = new Packet();
        p = dao.getFromOpsets1(null, "5");
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "7", result);
    }

    @Test
    public void testGetFromUserAttrsPacket(){
    	System.out.println("Testing getFromUserAttrsPacket");
    	Packet p = new Packet();
        p = dao.getFromUserAttrsPacket("2", null);
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "3", result);
    }

    @Test
    public void testGetDascUattrs(){
    	System.out.println("Testing getDascUattrs");
    	Packet p = new Packet();
        p = dao.getDascUattrs("admin", null);
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "superAdmin", result);
    }

    @Test
    public void testGetDascUsers(){
    	System.out.println("Testing getDascUsers");
    	Packet p = new Packet();
        p = dao.getDascUsers("superAdmin", null);
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "super", result);
    }

    @Test
    public void testGetDascOattrs(){
    	System.out.println("Testing getDascOattrs");
    	Packet p = new Packet();
        p = dao.getDascOattrs("admin", null);
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "everything", result);
    }

    @Test
    public void testGetDascObjects(){
    	System.out.println("Testing getDascObjects");
    	Packet p = new Packet();
        p = dao.getDascObjects("PM", "p");
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "admin", result);
    }

    @Test
    public void testGetDascOpsets(){
    	System.out.println("Testing getDascOpsets");
    	Packet p = new Packet();
        p = dao.getDascOpsets("everything", null);
        String result = "";
        try{
            result = p.getItemStringValue(0).split(":")[0];
        }catch(Exception e){
        	System.out.println("in Catch..");
            fail(e.getMessage());
        }
        assertEquals("Failed", "all ops", result);
    }
    
    private static void loadConfig(){
        try {
            Packet cmd = CommandUtil.makeCmd("importConfiguration", null, new String[]{});
            BufferedReader in = new BufferedReader(new FileReader(new File("/PM/conf/PMServerConfiguration.pm")));
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

}
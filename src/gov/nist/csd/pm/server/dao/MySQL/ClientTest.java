package gov.nist.csd.pm.server.dao.MySQL;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketException;
import gov.nist.csd.pm.common.util.CommandUtil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ClientTest {

    /**
     * Testing parameters to set for specific machines
     */
    public static String sHost = "P859310";

    private static String sessId;
    public static CommonSQLDAO dao;
    private static boolean load = false;

    @BeforeClass
    public static void init() {
        InetAddress addr;
//        try {
//            addr = InetAddress.getLocalHost();
//            sHost = addr.getHostName();
//        } catch (UnknownHostException e) {
//            fail("failed getting host");
//        }

        dao = ServerConfig.getSQLDAO();
//        dao.reset(null);
//        load = true;
//        loadConfig();
        Packet p = new Packet();
        try {
            p = dao.createSession(null, null, sHost, "super", "super");
            sessId = p.getItemStringValue(1);
        } catch (Exception e) {
            fail("could not create session for tests: " + p.getErrorMessage());
        }
    }

    @AfterClass
    public static void cleanUp() {
//        dao.reset(null);
        dao.deleteSession(null, sessId);
    }

    private static void loadConfig() {
        if (load) {
            try {
                Packet cmd = CommandUtil.makeCmd("importConfiguration", null);
                BufferedReader in = new BufferedReader(new FileReader(new File("./conf/PMServerConfiguration.pm")));
                String sLine;
                while ((sLine = in.readLine()) != null) {
                    sLine = sLine.trim();
                    if (sLine.length() <= 0 || sLine.startsWith("#")) {
                        continue;
                    }
                    cmd.addItem(ItemType.CMD_ARG, sLine);
                }
                dao.importConfiguration(null, cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getItems(Packet p) {
        String ret = "";
        try {
            for (int i = 0; i < p.size() - 1; i++) {
                ret += p.getItemStringValue(i) + ",";
            }
            ret += p.getItemStringValue(p.size() - 1);
        } catch (Exception e) {
            return "";
        }
        return ret;
    }

    @Test
    public void testGetFromAttrs1() {
        Packet p = new Packet();
        String expected = "";
        String actual = "";
        try {
            String id = dao.getEntityId("F14BB514", "s");
            p = dao.getFromAttrs1(null, id);
            actual = getItems(p);
            expected = dao.getEntityId("Intern", "a");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getFromAttrs1 failed for opset f14bb514", expected, actual);

        try {
            p = dao.getFromAttrs1(null, null);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("getFromAttrs1 should have an error", p.hasError());
    }

    @Test
    public void testGetFromUserAttrsPacket() {
        Packet p = new Packet();
        String expected = "";
        String actual = "";
        try {
            p = dao.getFromUserAttrsPacket(dao.getEntityId("Intern", "a"), "a");
            actual = getItems(p);
            expected = dao.getEntityId("Doctor", "a");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("failed getting from uattrs for Intern", expected, actual);

        try {
            p = dao.getFromUserAttrsPacket(null, "a");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue("getting from uattr should have an error because null base parameter", p.hasError());

        try {
            p = dao.getFromUserAttrsPacket(dao.getEntityId("Acct Columns", "b"), "b");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("getting from uattr should have an error because wrong base parameter", p.isEmpty());
    }

    @Test
    public void testIsRecordPacket() {
        Packet p = new Packet();
        String expected = "";
        String actual = "";
        try {
            String id = dao.getEntityId("8E094FF2", "b");
            p = dao.isRecordPacket(id);
            actual = getItems(p);
            expected = "yes";
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("8E094FF2 is a record", expected, actual);

        try {
            String id = dao.getEntityId("CMR Columns", "b");
            p = dao.isRecordPacket(id);
            actual = getItems(p);
            expected = "no";
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("CMR Columns is not a record", expected, actual);

        try {
            p = dao.isRecordPacket(null);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("isRecordPacket shold have an error with null id", p.hasError());
    }

    @Test
    public void testCreateSchemaPC() {
        /*try {
            Packet p = dao.createSchemaPC(null, null, "POLICY", "POLICY_UA");
            assertFalse("creating POLICY should not have error: " + p.getErrorMessage(), p.hasError());

            p = dao.createSchemaPC(null, null, "POLICY", "POLICY_UA");
            assertTrue("creating POLICY should have error", p.hasError());

            p = dao.createSchemaPC(null, null, null, "POLICY_UA");
            assertTrue("creating POLICY should have error with null policy name", p.hasError());
        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY", "p")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY", "b")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY rep", "o")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY_UA", "a")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    @Test
    public void testAddSchemaOattr() {
        /*try {
            Packet p = dao.addSchemaOattr(null, "OATTR", "RBAC", "p");
            assertFalse("creating OATTR should not have error: " + p.getErrorMessage(), p.hasError());

            p = dao.addSchemaOattr(null, null, "OATTR", "RBAC");
            assertTrue("creating OATTR should have error", p.hasError());

            p = dao.addSchemaOattr(null, null, null, "RBAC");
            assertTrue("creating OATTR should have error with null oattr name", p.hasError());
        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OATTR", "b")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    @Test
    public void testGetContainers() {
        Packet p = new Packet();
        ArrayList<String> expectedList = new ArrayList<String>();
        String actual = "";
        try {
            p = dao.getContainers(dao.getEntityId("E294203A", "o"), "o");
            actual = getItems(p);
            expectedList.add(dao.getEntityId("PatTreatment", "b"));
            expectedList.add(dao.getEntityId("8E094FF2", "b"));
            String[] pieces = actual.split(",");
            int i = 0;
            for (String s : pieces) {
                assertTrue("retrieved an id not in expected list: " + s + ":" + dao.getEntityName(s, "a"), expectedList.contains(s));
                i++;
            }
            assertTrue("Returned wrong number of Containers", i == expectedList.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            p = dao.getContainers(null, "o");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("getting Containers for null should have error", p.hasError());
    }

    @Test
    public void testGetToAttrsUser() {
        Packet p = new Packet();
        String actual = "";
        try {
            p = dao.getToAttrsUser(dao.getEntityId("bob", "u"), "u");
            actual = getItems(p);
            ArrayList<String> expectedList = new ArrayList<String>();
            expectedList.add(dao.getEntityId("Schema Admins", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Contracting", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("OU users", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Top secret", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Robert", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Doctor", PM_NODE.UATTR.value));
            String[] pieces = actual.split(",");
            int i = 0;
            for (String s : pieces) {
                assertTrue("retrieved an id not in expected list: " + s + ":" + dao.getEntityName(s, "a"), expectedList.contains(s));
                i++;
            }
            assertEquals("Did not get right number of attrs", expectedList.size(), i);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            p = dao.getToAttrsUser(dao.getEntityId("CMR Columns", "b"), "b");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("getting the attributes user is assigned to failed", p.isEmpty());

        try {
            p = dao.getToAttrsUser(null, "b");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("getting the attributes user is assigned to failed", p.hasError());
    }

    @Test
    public void testGetPermittedOpsOnEntity() {
        Packet p = new Packet();
        String expected = "";
        String actual = "";
        String sessId = "";
        try {
            try {
                sessId = dao.createSession(null, null, sHost, "bob", "bob").getItemStringValue(1);
            } catch (Exception e) {
                fail("could not create session for tests");
            }
            try {
                p = dao.getPermittedOpsOnEntity(sessId, null, dao.getEntityId("bob home", "b"), "b");
                actual = getItems(p);
                expected = "Object attribute create object attribute,Operation set assign to,Operation set assign,Object attribute create object,Entity represent,Object attribute assign to,Object attribute create operation set,Object attribute assign";
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("bob should have permissions on his home", expected, actual);

            try {
                p = dao.getPermittedOpsOnEntity(sessId, null, dao.getEntityId("alice home", "b"), "b");
                actual = getItems(p);
                expected = "";
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("bob should not have permissions on alice home", expected, actual);

            try {
                p = dao.getPermittedOpsOnEntity(null, null, dao.getEntityId("alice home", "b"), "b");
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertTrue("empty packet is expected when sessId is null", p.isEmpty());

            try {
                p = dao.getPermittedOpsOnEntity(sessId, null, null, "b");
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertTrue("error is expected when target is null", p.hasError());

        }finally {
            dao.deleteSession(null, sessId);
        }
    }

    //TODO getAssocOattr1 test

    @Test
    public void testGetReps() {
        Packet p = new Packet();
        String expected = "";
        String actual = "";
        try {
            p = dao.getReps(dao.getEntityId("bob home", "b"), "b");
            actual = getItems(p);
            expected = dao.getEntityId("bob home rep", "b");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getreps failed for bob home", expected, actual);

        try {
            p = dao.getReps(null, "b");
            actual = getItems(p);
            expected = dao.getEntityId("bob home rep", "b");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("getreps should be empty for null id", p.hasError());
    }

    @Test
    public void testGetFromOpsets1() {
        Packet p = new Packet();
        String actual = "";

        try {
            p = dao.getFromOpsets1(null, dao.getEntityId("Med Records", "b"));
            actual = getItems(p);

            ArrayList<String> expectedList = new ArrayList<String>();
            expectedList.add(dao.getEntityId("945358F8", "s"));
            expectedList.add(dao.getEntityId("5C59BE03", "s"));
            String[] pieces = actual.split(",");
            int i = 0;
            for (String s : pieces) {
                assertTrue("retrieved an id not in expected list: " + s + ":" + dao.getEntityName(s, "a"), expectedList.contains(s));
                i++;
            }
            assertEquals("Did not get right number of attrs", expectedList.size(), i);

        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            p = dao.getFromOpsets1(null, dao.getEntityId("mrec4", "o"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("getting attributes assigned to object should have been empty", p.isEmpty());

        try {
            p = dao.getFromOpsets1(null, null);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("getting attributes assigned to null opset should have failed", p.hasError());
    }

    @Test
    public void testGetPermittedOps1() {
        Packet p = new Packet();
        String expected = "Object attribute create object attribute,Operation set assign to,Operation set assign,Object attribute create object,Entity represent,Object attribute assign to,Object attribute create operation set,Object attribute assign";
        String actual = "";
        String sessId = "";
        try {
            try {
                sessId = dao.createSession(null, null, sHost, "bob", "bob").getItemStringValue(1);
            } catch (Exception e) {
                fail("could not create session for tests");
            }

            try {
                p = dao.getPermittedOps1(sessId, null, dao.getEntityId("bob home", "b"), "b");
                actual = getItems(p);
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("failed getting permitted ops for bob on bob home", expected, actual);

            try {
                p = dao.getPermittedOps1(sessId, null, dao.getEntityId("alice home", "b"), "b");
                actual = getItems(p);
                expected = "";
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("failed getting permitted ops (should be none) for bob on alice home", expected, actual);

            try {
                p = dao.getPermittedOps1(null, null, dao.getEntityId("Alice home", "b"), "b");
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertTrue("error is expected when sessId is null", p.hasError());

            try {
                p = dao.getPermittedOps1(sessId, null, null, "b");
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertTrue("error is expected when target is null", p.hasError());

        }finally{
            dao.deleteSession(null, sessId);
        }
    }

    @Test
    public void testGetToAttrs1() {
        Packet p = new Packet();
        String expected = "";
        String actual = "";

        try {
            p = dao.getToAttrs1(null, dao.getEntityId("1DDA9933", "s"));
            actual = getItems(p);
            expected = dao.getEntityId("CMRecs", "b");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getting attributes the opset is assigned to failed", expected, actual);

        try {
            p = dao.getToAttrs1(null, null);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue("getting attributes null is assigned to should have error", p.hasError());
    }

    @Test
    public void testSetSchemaPerms() {
        /*try {
            String sessId = "";
            try{
                sessId = dao.createSession(null, null, sHost, "bob", "bob").getItemStringValue(1);
            } catch (PacketException e) {
                fail(e.getMessage());
            }
            Packet p = dao.createSchemaPC(sessId, null, "POLICY", "POLICY_UA");
            p = dao.addSchemaOattr(sessId, "OATTR", "POLICY", "p");
            p = dao.setSchemaPerms(sessId, null, "POLICY", "p", "OATTR", "POLICY_UA");
            assertFalse("Setting schema perms shouldn't have an error", p.hasError());

            p = dao.setSchemaPerms(sessId, null, "P", "p", "OATTR", "POLICY_UA");
            assertTrue("Setting schema perms should have an error", p.hasError());

        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY", "p")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY", "b")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY rep", "o")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY_UA", "a")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OATTR", "b")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    @Test
    public void testDeleteTemplate() {
        Packet p = dao.addTemplate(null, "tpl", "PatTreatment", "PatTreatment");
        if (p.hasError()) {
            fail(p.getErrorMessage());
        }

        String sId = "";
        try {
            sId = dao.getEntityId("tpl", "tpl");
        } catch (Exception e) {
            fail(e.getMessage());
        }

        p = dao.deleteTemplate(sessId, sId, "tpl");
        assertFalse("deleting template should not have an error: " + p.getErrorMessage(), p.hasError());


        p = dao.deleteTemplate(sessId, null, "tpl");
        assertTrue("deleting template with null id should fail", p.hasError());
    }

    @Test
    public void testGetPolicyClasses() {
        Packet p = dao.getPolicyClasses(null);
        String actual = getItems(p);
        String expected = "";
        try {
            expected =
                    "admin:" + dao.getEntityId("admin", "p")
                            + ",MLS:" + dao.getEntityId("MLS", "p")
                            + ",DAC:" + dao.getEntityId("DAC", "p")
                            + ",Schema Builder:" + dao.getEntityId("Schema Builder", "p")
                            + ",RBAC:" + dao.getEntityId("RBAC", "p")
                            + ",Confine:" + dao.getEntityId("Confine", "p");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getting policy classes failed", expected, actual);
    }

    @Test
    public void testAddPc() {
        String id = "";
        try {
            Packet p = dao.addPc(sessId, null, "POLICY", "", "", new String[]{"prop=1234"});
            assertFalse("addPc should not have an error", p.hasError());

            try {
                System.out.println(p.getStringValue(0));
                id = p.getItemStringValue(0).split(":")[1];
            } catch (PacketException e) {
                fail(e.getMessage());
            }

            assertFalse("id returned in packet was null", id.equals("null"));

            p = dao.addPc(sessId, null, "POLICY", "", "", new String[]{"prop=1234"});
            assertTrue("addPc should have an error because duplicate", p.hasError());
        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY", "p")));
                //dao.deleteSession(null, sessId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testGetPcInfo() {
        String id = "";
        String actual = "";
        String expected = "";
        try {
            Packet p = dao.addPc(sessId, null, "POLICY", "descr", "", new String[]{"prop=1234"});

            try {
                id = p.getItemStringValue(0).split(":")[1];
                p = dao.getPcInfo(sessId, id, "no");
                actual = getItems(p);
                expected = "POLICY:" + dao.getEntityId("POLICY", "p") + ",descr,,prop=1234";
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("getting PC info should work", expected, actual);

            p = dao.getPcInfo(null, null, "no");
            assertTrue("getting pc info of null should result in error", p.hasError());


        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY", "p")));
                //dao.deleteSession(null, sessId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testAddUattr() {
        Packet p = new Packet();
        String actual = "";
        try {
            try {
                p = dao.addUattr(null, sessId, null, "UATTR", "", "", dao.getEntityId("RBAC", "p"), "p", "no", new String[]{"prop=123123"});
                actual = p.getItemStringValue(0).split(":")[0];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals(p.getErrorMessage(), "UATTR", actual);

            try {
                p = dao.addUattr(null, sessId, null, null, "", "", dao.getEntityId("RBAC", "p"), "p", "no", new String[]{"prop=123123"});
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertTrue("adding Uattr should have error with null name", p.hasError());
        } finally {
            try {
            	String result = dao.getEntityId("UATTR", "a");
            	if (result!=null) {
            		dao.deleteNode(Integer.valueOf(dao.getEntityId("UATTR", "a")));
            	} else {
            		fail("getEntityId returned null");
            	}
                //dao.deleteSession(null, sessId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testAddOattr() {
        Packet p = new Packet();
        String actual = "";
        try {
            try {
                p = dao.addOattr(sessId, null, "OATTR", "", "", dao.getEntityId("RBAC", "p"), "p", "no", null, new String[]{"prop=123123"});
                actual = p.getItemStringValue(0).split(":")[0];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("adding oattr should return name:id", "OATTR", actual);

            try {
                p = dao.addOattr(null, sessId, null, null, "", "", dao.getEntityId("RBAC", "p"), "p", "no", new String[]{"prop=123123"});
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertTrue("adding oattr should have error with null name", p.hasError());
        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OATTR", "b")));
                //dao.deleteSession(null, sessId);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testAssign() {
        Packet p = new Packet();
        String start = "";
        String end = "";
        // user ---> user attribute
        try {
            try {
                p = dao.addUattr(null, sessId, null, "UATTR", "", "", dao.getEntityId("RBAC", "p"), "p", "no", new String[]{"prop=123123"});
                start = p.getItemStringValue(0).split(":")[1];
                end = dao.getEntityId("bob", "u");
                p = dao.assign(sessId, null, end, "u", start, "a");
                assertFalse("u -> ua error: " + p.getErrorMessage(), p.hasError());
                assertTrue("u -> ua failed", dao.isAssigned(Integer.valueOf(end), Integer.valueOf(start)));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } finally {
            try {
                dao.deleteAssignment(Integer.valueOf(end), Integer.valueOf(start));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("UATTR", "a")));
            } catch (Exception e) {
                //dao.deleteSession(null, sessId);
                fail(e.getMessage());
            }
        }

        // user attribute ---> user attribute
        try {
            try {
                p = dao.addUattr(null, sessId, null, "UATTR-START", "", "", dao.getEntityId("RBAC", "p"), "p", "no", new String[]{"prop=123123"});
                start = p.getItemStringValue(0).split(":")[1];
                p = dao.addUattr(null, sessId, null, "UATTR-END", "", "", dao.getEntityId("RBAC", "p"), "p", "no", new String[]{"prop=123123"});
                end = p.getItemStringValue(0).split(":")[1];
                p = dao.assign(sessId, null, end, "a", start, "a");
                assertFalse("ua -> ua error: " + p.getErrorMessage(), p.hasError());
                assertTrue("ua -> ua failed", dao.isAssigned(Integer.valueOf(end), Integer.valueOf(start)));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } finally {
            try {
                dao.deleteAssignment(Integer.valueOf(end), Integer.valueOf(start));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("UATTR-START", "a")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("UATTR-END", "a")));
            } catch (Exception e) {
                //dao.deleteSession(null, sessId);
                fail(e.getMessage());
            }
        }

        // user attribute ---> policy class
        try {
            try {
                p = dao.addPc(sessId, null, "POLICY", "", "", new String[]{"prop=1234"});
                start = p.getItemStringValue(0).split(":")[1];
                p = dao.addUattr(null, sessId, null, "UATTR-END", "", "", dao.getEntityId("RBAC", "p"), "p", "no", new String[]{"prop=123123"});
                end = p.getItemStringValue(0).split(":")[1];
                p = dao.assign(sessId, null, end, "a", start, "p");
                assertFalse("p -> ua error: " + p.getErrorMessage(), p.hasError());
                assertTrue("p -> ua failed", dao.isAssigned(Integer.valueOf(end), Integer.valueOf(start)));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } finally {
            try {
                dao.deleteAssignment(Integer.valueOf(end), Integer.valueOf(start));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY", "p")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("UATTR-END", "a")));
            } catch (Exception e) {
                //dao.deleteSession(null, sessId);
                fail(e.getMessage());
            }
        }

        // user attribute ---> operation set
        try {
            try {
                p = dao.addOpsetAndOp(sessId, "OPSET", "", "", "File write", dao.getEntityId("PatTreatment", "b"), "b");
                start = p.getItemStringValue(0).split(":")[1];
                p = dao.addUattr(null, sessId, null, "UATTR-END", "", "", dao.getEntityId("RBAC", "p"), "p", "no", new String[]{"prop=123123"});
                end = p.getItemStringValue(0).split(":")[1];
                p = dao.assign(sessId, null, end, "a", start, "s");
                assertFalse("s -> ua error: " + p.getErrorMessage(), p.hasError());
                assertTrue("s -> ua failed", dao.isAssigned(Integer.valueOf(end), Integer.valueOf(start)));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } finally {
            try {
                dao.deleteAssignment(Integer.valueOf(end), Integer.valueOf(start));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OPSET", "s")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("UATTR-END", "a")));
            } catch (Exception e) {
                //dao.deleteSession(null, sessId);
                fail(e.getMessage());
            }
        }

        // object attribute ---> object attribute (but not associated to object)
        try {
            try {
                p = dao.addOattr(sessId, null, "OATTR-S", "", "", dao.getEntityId("RBAC", "p"), "p", "no", null, new String[]{"prop=123123"});
                start = p.getItemStringValue(0).split(":")[1];
                p = dao.addOattr(sessId, null, "OATTR-E", "", "", dao.getEntityId("RBAC", "p"), "p", "no", null, new String[]{"prop=123123"});
                end = p.getItemStringValue(0).split(":")[1];
                p = dao.assign(sessId, null, end, "b", start, "b");
                assertFalse("oa -> ua error: " + p.getErrorMessage(), p.hasError());
                assertTrue("oa -> oa failed", dao.isAssigned(Integer.valueOf(end), Integer.valueOf(start)));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } finally {
            try {
                dao.deleteAssignment(Integer.valueOf(end), Integer.valueOf(start));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OATTR-S", "b")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OATTR-E", "b")));
            } catch (Exception e) {
                //dao.deleteSession(null, sessId);
                fail(e.getMessage());
            }
        }

        // object attribute ---> policy class
        try {
            try {
                p = dao.addPc(sessId, null, "POLICY", "", "", new String[]{"prop=1234"});
                start = p.getItemStringValue(0).split(":")[1];
                p = dao.addOattr(sessId, null, "OATTR", "", "", dao.getEntityId("RBAC", "p"), "p", "no", null, new String[]{"prop=123123"});
                end = p.getItemStringValue(0).split(":")[1];
                p = dao.assign(sessId, null, end, "b", start, "p");
                assertFalse("p -> oa error: " + p.getErrorMessage(), p.hasError());
                assertTrue("p -> oa failed", dao.isAssigned(Integer.valueOf(end), Integer.valueOf(start)));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } finally {
            try {
                dao.deleteAssignment(Integer.valueOf(end), Integer.valueOf(start));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("POLICY", "p")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OATTR", "b")));
            } catch (Exception e) {
                //dao.deleteSession(null, sessId);
                fail(e.getMessage());
            }
        }

        // operation set ---> object attribute.
        try {
            try {
                p = dao.addOpsetAndOp(sessId, "OPSET", "", "", "File write", dao.getEntityId("PatTreatment", "b"), "b");
                end = p.getItemStringValue(0).split(":")[1];
                p = dao.addOattr(sessId, null, "OATTR", "", "", dao.getEntityId("RBAC", "p"), "p", "no", null, new String[]{"prop=123123"});
                start = p.getItemStringValue(0).split(":")[1];
                p = dao.assign(sessId, null, end, "b", start, "s");
                assertFalse("s -> oa error: " + p.getErrorMessage(), p.hasError());
                assertTrue("s -> oa failed", dao.isAssigned(Integer.valueOf(end), Integer.valueOf(start)));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        } finally {
            try {
                dao.deleteAssignment(Integer.valueOf(end), Integer.valueOf(start));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OPSET", "s")));
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OATTR", "b")));
            } catch (Exception e) {
                //dao.deleteSession(null, sessId);
                fail(e.getMessage());
            }
        }

        try {
            p = dao.assign(sessId, null, null, null, null, null);
            assertTrue("should have error when assigning null attributes", p.hasError());
        } finally {
            //dao.deleteSession(null, sessId);
        }

    }

    @Test
    public void testDeleteAssignment() {
        //This may have been adequately tested in testAssign
    }

    @Test
    public void testGetUserAttributes() {
        String actual = "";
        try {
            actual = getItems(dao.getUserAttributes(null));
            ArrayList<String> expectedList = new ArrayList<String>();
            expectedList.add(dao.getEntityId("superAdmin", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Schema Admins", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Secret", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Acct Mgr", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Acct Repr", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Nurse", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Adm Clerk", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("DAC uattrs", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Accts Pbl", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Contracting", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Accts Rcv", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Acquisition", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Secretary", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Intern", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("OU users", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Top secret", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("David", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Katherine", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Charles", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Robert", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Alicia", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("ExporterUA", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Doctor", PM_NODE.UATTR.value));
            String[] pieces = actual.split(",");
            int i = 0;
            for (String s : pieces) {
                assertTrue("retrieved an id not in expected list: " + dao.getEntityName(s.split(":")[1], "a"), expectedList.contains(s.split(":")[1]));
                i++;
            }
            assertTrue("Returned wrong number of User Attributes", i == expectedList.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetUsers() {
        String actual = "";
        try {
            actual = getItems(dao.getUsers(null));
            ArrayList<String> expectedList = new ArrayList<String>();
            expectedList.add(dao.getEntityId("super", PM_NODE.USER.value));
            expectedList.add(dao.getEntityId("katie", PM_NODE.USER.value));
            expectedList.add(dao.getEntityId("alice", PM_NODE.USER.value));
            expectedList.add(dao.getEntityId("dave", PM_NODE.USER.value));
            expectedList.add(dao.getEntityId("charlie", PM_NODE.USER.value));
            expectedList.add(dao.getEntityId("exporter", PM_NODE.USER.value));
            expectedList.add(dao.getEntityId("bob", PM_NODE.USER.value));

            String[] pieces = actual.split(",");
            int i = 0;
            for (String s : pieces) {
                assertTrue("retrieved an id not in expected list: " + s + ":" + dao.getEntityName(s.split(":")[1], "a"), expectedList.contains(s.split(":")[1]));
                i++;
            }
            assertTrue("Returned wrong number of Users", i == expectedList.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetAsets() {// Need to confirm -  this method is not used anymore

    }

    @Test
    public void testAddUser() {
        Packet p = new Packet();
        String actual = "";
        try {
            try {
                p = dao.addUser(sessId, null, "USER", "USERNAME", "", "USER", dao.getEntityId("RBAC", "p"), "p", "no");
                actual = p.getItemStringValue(0).split(":")[0];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("adding user should return name:id", "USER", actual);

            try {
                p = dao.addUser(sessId, null, null, "USERNAME", "", "USER", dao.getEntityId("RBAC", "p"), "p", "no");
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertTrue("adding user should have error with null name", p.hasError());
        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("USER", "u")));
                //dao.deleteSession(null, sessId);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testDeleteNode() {
        //may have been tested in all tests so far
        Packet p = dao.deleteNode(sessId, null, "b", "no");
        assertTrue("Should have an error when deleting null", p.hasError());
    }

    @Test
    public void testAddObjClassAndOp() {
        // Add a new object class and/or a new operation.
        // If the object class does not exist, add a new one and the operation,
        // if present in the command.
        // If the object class already exists, add a new operation (which must be
        // present in the command.

        try {
            Packet p = dao.addObjClassAndOp(null, "TEST_CLASS", "", "", "OP");
            assertFalse("adding obj class CLASS and op OP should not have error", p.hasError());

            p = dao.addObjClassAndOp(null, "TEST_CLASS", "", "", "OP1");
            assertFalse("adding obj class CLASS and op OP1 should not have error", p.hasError());

            p = dao.addObjClassAndOp(null, "TEST_CLASS", "", "", "OP1");
            assertTrue("adding obj class CLASS and op OP1 again should have error", p.hasError());

            p = dao.addObjClassAndOp(null, "TEST_CLASS", "", "", "");
            assertTrue("adding obj class NEW CLASS and empty op should have error", p.hasError());
        } finally {
            dao.deleteObjClassAndOp(null, "TEST_CLASS", "OP");
            dao.deleteObjClassAndOp(null, "TEST_CLASS", "OP1");
            dao.deleteObjClassAndOp(null, "TEST_CLASS", null);
        }
    }

    @Test
    public void testDeleteObjClassAndOp() {
        // Add a new object class and/or a new operation.
        // If the object class does not exist, add a new one and the operation,
        // if present in the command.
        // If the object class already exists, add a new operation (which must be
        // present in the command.

        try {
            Packet p = dao.addObjClassAndOp(null, "TEST_CLASS", "", "", "OP");
            p = dao.deleteObjClassAndOp(null, "TEST_CLASS", "OP");
            assertFalse(p.getErrorMessage(), p.hasError());

            p = dao.addObjClassAndOp(null, "TEST_CLASS", "", "", "OP1");
            p = dao.deleteObjClassAndOp(null, "TEST_CLASS", "OP1");
            assertFalse(p.getErrorMessage(), p.hasError());

            p = dao.deleteObjClassAndOp(null, "TEST_CLASS", null);
            assertFalse(p.getErrorMessage(), p.hasError());

            p = dao.deleteObjClassAndOp(null, "", null);
            assertTrue(p.getErrorMessage(), p.hasError());
        } finally {
            dao.deleteObjClassAndOp(null, "TEST_CLASS", "OP");
            dao.deleteObjClassAndOp(null, "TEST_CLASS", "OP1");
            dao.deleteObjClassAndOp(null, "TEST_CLASS", null);
        }
    }

    @Test
    public void testGetObjClasses() {
        String actual = getItems(dao.getObjClasses(null));
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList(new String[]{
                "class",
                "File",
                "Directory",
                "User",
                "User attribute",
                "Object",
                "Object attribute",
                "Connector",
                "Policy class",
                "Operation set",
                "*"
        }));

        String[] pieces = actual.split(",");
        int i = 0;
        for (String s : pieces) {
            assertTrue("retrieved an id not in expected list: " + s, expected.contains(s));
            i++;
        }
        assertTrue("Returned wrong number of object classes", i == expected.size());
    }

    @Test
    public void testGetObjects() {
        Packet p = dao.getObjects(null);
        assertFalse(p.getErrorMessage(), p.hasError());
    }

    @Test
    public void testGetPmEntitiesOfClass() {
        Packet p = dao.getPmEntitiesOfClass(null, "Policy class");
        String actual = getItems(p);
        ArrayList<String> expected = null;
        try {
            expected = new ArrayList<String>(Arrays.asList(new String[]{
                    "admin:" + dao.getEntityId("admin", "p"),
                    "MLS:" + dao.getEntityId("MLS", "p"),
                    "DAC:" + dao.getEntityId("DAC", "p"),
                    "Schema Builder:" + dao.getEntityId("Schema Builder", "p"),
                    "RBAC:" + dao.getEntityId("RBAC", "p"),
                    "Confine:" + dao.getEntityId("Confine", "p")
            }));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        String[] pieces = actual.split(",");
        int i = 0;
        for (String s : pieces) {
            assertTrue("retrieved an entity not in expected list: " + s, expected.contains(s));
            i++;
        }
        assertTrue("Returned wrong number of pm entites with class Policy class", i == expected.size());
    }

    @Test
    public void testAddObject3() {
        try {
            Packet p = dao.addObject3(sessId, null, "object1", null, null, "b|everything", "File",
                    "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                    null, null, null, null, null, null);
            String result = "";
            try {
                result = p.getItemStringValue(0).split(":")[0];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("plain object was not created correctly", "object1", result);

            p = dao.addObject3(sessId, null, "emlObject", null, null, "b|everything", "File",
                    "o", sHost, "C:\\PMWorkarea123", null, null, "no", "bob@nist.gov",
                    "alice@nist.gov", "subject", "everything", null, null, null);
            result = "";
            try {
                result = p.getItemStringValue(0).split(":")[0];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("email object was not created correctly", "emlObject", result);

            /*p = dao.addObject3(sessId, null, "tplObject", null, null, "b|8E094FF2", "File",
                    "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                    null, null, null, "138", null, null);
            result = "";
            try {
                result = p.getItemStringValue(0).split(":")[0];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("template object was not created correctly", "tplObject", result);*/

            p = dao.addObject3(sessId, null, "object2", null, null, null, "File",
                    "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                    null, null, null, null, null, null);
            try {
                result = p.getItemStringValue(0).split(":")[0];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("object should be assigned to connector if container is null", "object2", result);
        } finally {
            try {
                dao.deleteNode(new Integer(dao.getEntityId("object1", "o")));
                dao.deleteNode(new Integer(dao.getEntityId("emlObject", "o")));
                //dao.deleteNode(new Integer(dao.getEntityId("tplObject", "o")));
                dao.deleteNode(new Integer(dao.getEntityId("object2", "o")));
            } catch (Exception e) {

            }
        }
    }

    @Test
    public void testDeleteObject() {
        try {
            String id = "";
            Packet p = dao.addObject3(sessId, null, "object1", null, null, "b|everything", "File",
                    "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                    null, null, null, null, null, null);
            try {
                id = p.getItemStringValue(0).split(":")[1];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            p = dao.deleteObject(sessId, id);
            assertFalse(p.hasError());

            p = dao.deleteObject(sessId, id);
            assertTrue(p.hasError());
        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("object1", "o")));
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testDeleteObjectStrong() {
    	Packet p;
    	String op1, ua, oa;
        try {
            p = dao.addOpsetAndOp(sessId, "OPSET1", "", "", "File write", dao.getEntityId("PatTreatment", "b"), "b");
				op1 = p.getItemStringValue(0).split(":")[1];
            p = dao.addUattr(null, sessId, null, "UA1", "", "", dao.getEntityId("RBAC", "p"), "p", "no", new String[]{"prop=123123"});
            	ua = p.getItemStringValue(0).split(":")[1];
            p = dao.assign(sessId, null, ua, "a", op1, "s");

			p = dao.addOattr(sessId, null, "OA", "", "", dao.getEntityId("RBAC", "p"), "p", "no", null, new String[]{"prop=123123"});
				 oa = p.getItemStringValue(0).split(":")[1];
            p = dao.assign(sessId, null, op1, "b", oa, "s");
            
            dao.deleteObjectStrong(sessId, oa);
            
            assertFalse(dao.entityExists(oa, PM_NODE.OATTR.value));
            assertFalse(dao.entityExists(op1, PM_NODE.OPSET.value));
		} catch (Exception e) {
            fail(e.getMessage());
        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("ua", "a")));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testDeleteContainerObjects() {
        //create oa
        //create 3 o
        //check oa doesnt have asc
        try {
            Packet p = new Packet();
            String id = "";
            try {
                p = dao.addOattr(sessId, null, "OATTR", "", "", dao.getEntityId("RBAC", "p"), "p", "no", null, new String[]{"prop=123123"});
                id = p.getItemStringValue(0).split(":")[1];
                p = dao.addObject3(sessId, null, "object1", null, null, "b|OATTR", "File",
                        "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                        null, null, null, null, null, null);
                p = dao.addObject3(sessId, null, "object2", null, null, "b|OATTR", "File",
                        "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                        null, null, null, null, null, null);
                p = dao.addObject3(sessId, null, "object3", null, null, "b|OATTR", "File",
                        "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                        null, null, null, null, null, null);
                p = dao.deleteContainerObjects(sessId, "OATTR", "b");
                assertFalse(p.getErrorMessage(), p.hasError());
                assertFalse("OATTR should not have ascendants", dao.hasAscendants(id));
            } catch (Exception e) {
                fail(p.getErrorMessage());
            }

            try {
                p = dao.addObject3(sessId, null, "object1", null, null, "b|OATTR", "File",
                        "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                        null, null, null, null, null, null);
                p = dao.addObject3(sessId, null, "object2", null, null, "b|OATTR", "File",
                        "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                        null, null, null, null, null, null);
                p = dao.addObject3(sessId, null, "object3", null, null, "b|OATTR", "File",
                        "o", sHost, "C:\\PMWorkarea", null, null, "no", null,
                        null, null, null, null, null, null);
                p = dao.deleteContainerObjects(sessId, null, "b");
                assertTrue(p.getErrorMessage(), p.hasError());
                assertTrue("OATTR should have ascendants", dao.hasAscendants(id));
            } catch (Exception e) {
                fail(p.getErrorMessage());
            }
        } finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("object1", "o")));
            } catch (Exception e) {
            }
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("object2", "o")));
            } catch (Exception e) {
            }
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("object3", "o")));
            } catch (Exception e) {
            }
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("OATTR", "b")));
            } catch (Exception e) {
            }
        }
    }

    @Test
    public void testGetAssocObj() {
        try {
            Packet p = dao.getAssocObj(null, dao.getEntityId("mrec1", "o"));
            String actual = p.getItemStringValue(0);
            String expected = dao.getEntityId("mrec1", "o");
            assertEquals("gettingAssocObj for mrec1 failed", expected, actual);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            Packet p = dao.getAssocObj(null, dao.getEntityId("PatTreatment", "b"));
            String actual = p.getItemStringValue(0);
            assertTrue("gettingAssocObj for PatTreatment should be null got: " + actual, p.hasError());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetObjNamePath() {
        String objName = "FC15B612";
        Packet p = dao.getObjNamePath(objName);
        String actual = getItems(p);
        String expected = "C:\\pmworkArea\\FC15B612.txt";
        assertEquals("retrieved the wrong path for object: " + objName, expected, actual);

        objName = "PatTreatment";
        p = dao.getObjNamePath(objName);
        assertTrue("shoyuld have error because PatTreatment is not an object", p.hasError());
    }

    @Test
    public void testGetObjClassOps() {
        Packet p = dao.getObjClassOps(null, "File");

        ArrayList<String> expected = new ArrayList<String>(Arrays.asList(
                new String[]{
                        "Class delete class",
                        "File modify",
                        "File read and execute",
                        "File read",
                        "File write"
                }));
        String actual = getItems(p);

        String[] pieces = actual.split(",");
        int i = 0;
        for (String s : pieces) {
            assertTrue("retrieved an op not in expected list: " + s, expected.contains(s));
            i++;
        }
        assertTrue("Returned wrong number of object class ops", i == expected.size());

        p = dao.getObjClassOps(null, "NEWCLASS");
        assertTrue("getting object class ops should have error for invalid class", p.isEmpty());

        p = dao.getObjClassOps(null, null);
        assertTrue("getting object class ops should have error for null class", p.hasError());
    }

    @Test
    public void testGetHosts() {
        Packet p = dao.getHosts(null);
        assertFalse("getting hosts should not fail", p.hasError());
        assertFalse("getting hosts should not be empty, should at least have a dummy", p.isEmpty());
    }

    @Test
    public void testAddHost() {
        Packet p = new Packet();
        try{
            p = dao.addHost(sessId, sHost, "REPO");
            assertTrue("Packet should have error because of duplicate host name", p.hasError());

            p = dao.addHost(sessId, "", "REPO");
            assertTrue("Packet should have error because of empty host name", p.hasError());

            p = dao.addHost(sessId, null, "REPO");
            assertTrue("Packet should have error because of null host name", p.hasError());

            p = dao.addHost(sessId, "HOST$#@!%^&*()<>/?{}|+=-_123", "REPO");
            assertTrue("Packet should have error because of invalid host name", p.hasError());

            p = dao.addHost(sessId, "HOST", null);
            assertTrue("Packet should have error because of null repo path", p.hasError());

            p = dao.addHost(sessId, "HOST", "");
            assertTrue("Packet should have error because of empty repo path", p.hasError());

            p = dao.addHost(sessId, "HOST", "REPO");
            assertFalse("Packet should not have an error", p.hasError());
        } finally {
            try {
                dao.deleteHost(null, p.getItemStringValue(0).split(":")[1]);
            } catch (PacketException e) {
            }
        }
    }

    @Test
    public void testUpdateHost() {
        String id = "";
        try {
            Packet p = dao.addHost(sessId, "HOST", "REPO");
            assertFalse("adding host failed: " + p.getErrorMessage(), p.hasError());

            id = getItems(p).split(":")[1];

            p = dao.updateHost(sessId, id, "HOST123", "REPO123");
            assertFalse("updating host should not fail: " + p.getErrorMessage(), p.hasError());
            String hostName = "";
            try {
                hostName = p.getItemStringValue(0).split(":")[0];
                id = p.getItemStringValue(0).split(":")[1];
            } catch (PacketException e) {
                fail("could not get updated host name or id");
            }
            assertEquals("host name did not update", "HOST123", hostName);

            p = dao.updateHost(sessId, "45", "HOST1234", "REPO1234");
            assertTrue("updating host that doesn't exist should return error", p.hasError());
        } finally {
            dao.deleteHost(null, id);
        }
    }

    @Test
    public void testDeleteHost() {
        Packet p = dao.addHost(sessId, "HOST", "REPO");
        String id = getItems(p).split(":")[1];
        p = dao.deleteHost(null, id);
        assertFalse("should not have an error when deleting host", p.hasError());

        p = dao.deleteHost(null, null);
        assertTrue("deleting host with null id should have error", p.hasError());
    }

    @Test
    public void testGetHostInfo() {
        String id = "";
        try {
            Packet p = dao.addHost(sessId, "HOST", "REPO");
            id = getItems(p).split(":")[1];
            p = dao.getHostInfo(sessId, id);
            String actual = getItems(p);
            String expected = "HOST,REPO";
            assertEquals("Did not get correct host info", expected, actual);

            p = dao.getHostInfo(sessId, "45");
            assertTrue("error should occur with unknown id", p.hasError());

            p = dao.getHostInfo(sessId, null);
            assertTrue("error should occur with null id", p.hasError());
        }finally{
            dao.deleteHost(null, id);
        }
    }

    @Test
    public void testGetOpsets() {
        Packet p = dao.getOpsets(null);
        String actual = getItems(p);
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList(new String[]{
                "all ops", "FAFC7F4A", "033A0D1C", "C2CF01CD", "20DE6FEB", "78E5ABCE",
                "5791E4F9", "0F404D67", "47CE9638", "8B31D32C", "3E5AF6F1", "F6CCB92B",
                "581A03EB", "20B9C61C", "CAF1CC9C", "0AA431B8", "EA3DBB84", "AD179DE1",
                "4EBE4892", "6BB7EBA7", "D7B13327", "4D72BCE2", "6523425D", "D36773F1",
                "7F91A035", "830C202D", "FEBB2C43", "6121EE87", "02CDCAC2", "46A18983",
                "56B1E529", "09C9B277", "1DDA9933", "C5F57D1C", "AD27716D", "D31DD406",
                "A1F3F938", "E963428D", "0B4407A1", "ACD130E8", "A0BD0694", "94C9DF0B",
                "73F33EBB", "663A07F7", "9F00F0A0", "B254FA19", "12345679", "15E69BE1",
                "EE568F88", "60507C01", "16A11DA2", "18E9B035", "B3FD2394", "D6F23181",
                "D7048A1C", "63C0F4E0", "17D90B49", "945358F8", "F14BB514", "011D8B80",
                "24214879", "690D3A30", "D1687D6A", "C74AAC05", "5C59BE03", "359268CA",
                "EBBE3FE1", "61FAF91D", "61538005", "7F85FF96", "11AE564E", "39D1BFC8",
                "708FA799", "D041A88C", "E2327C18", "B270223E", "BCDE77D3", "94E4DFD4",
                "BBD10825", "D827BFC6", "B12DFFDD", "FE4A7080", "504C2DFC", "19C91A87",
                "F7D14F3F", "9079F508", "79E12419", "BB2F4148", "F8891E54", "14D0E012",
                "971B7214", "15068202", "1FAC2FCC", "87499089", "1698910F", "BDB987A8",
                "46C6EC2B", "05792340", "08BAB037", "B219B313", "B4F11AFD", "8DE0EB23",
                "A4ADADDD", "4FA145EA", "E664D534", "6D223F10", "AF698BEF", "6DB75F58",
                "ADFA7E54", "53811547",
        }));

        String[] pieces = actual.split(",");
        int i = 0;
        for (String s : pieces) {
            assertTrue("retrieved an opset not in expected list: " + s, expected.contains(s.split(":")[0]));
            i++;
        }
        assertTrue("Returned wrong number of opsets", i == expected.size());
    }

    @Test
    public void testAddOpsetAndOp(){
        String opsetId1 = "";
        String opsetId2 = "";
        String opsetId3 = "";
        try {
            Packet p = dao.addOpsetAndOp(sessId, "opset1", "", "", "File write", "5", "b");
            String result = "";
            try {
                result = p.getItemStringValue(0).split(":")[0];
                opsetId1 = p.getItemStringValue(0).split(":")[1];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("opset1 was not created correctly", "opset1", result);

            p = dao.addOpsetAndOp(sessId, "opset2", "", "", null, "5", "b");
            try {
                result = p.getItemStringValue(0).split(":")[0];
                opsetId2 = p.getItemStringValue(0).split(":")[1];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("opset2 was not created correctly", "opset2", result);

            p = dao.addOpsetAndOp(sessId, "opset3", "", "", "File write", null, null);
            try {
                result = p.getItemStringValue(0).split(":")[0];
                opsetId3 = p.getItemStringValue(0).split(":")[1];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertEquals("opset3 was not created correctly", "opset3", result);

            p = dao.addOpsetAndOp(sessId, "opset3", "", "", null, null, null);
            assertTrue("adding a null op to opset3 should have failed: " + p.getErrorMessage(), p.hasError());

            p = dao.addOpsetAndOp(sessId, "opset3", "", "", "", null, null);
            assertTrue("adding an empty op to opset3 should have failed: " + p.getErrorMessage(), p.hasError());

            p = dao.addOpsetAndOp(sessId, "opset3", "", "", "NOP", null, null);
            assertTrue("trying to add an invalid operation should have failed: " + p.getErrorMessage(), p.hasError());

            p = dao.addOpsetAndOp(sessId, "opset3", "", "", "File write", null, null);
            assertTrue("opset should already contain this operation: " + p.getErrorMessage(), p.hasError());
        }finally{
            try {
                dao.deleteNode(Integer.valueOf(opsetId1));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                dao.deleteNode(Integer.valueOf(opsetId2));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                dao.deleteNode(Integer.valueOf(opsetId3));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testGetOpsetOps(){
        Packet p = dao.getOpsetOps(null, "033A0D1C");
        ArrayList<String> expected = new ArrayList<String>(Arrays.asList(new String[]{
                "Object attribute create object",
                "Object attribute assign to"
        }));

        String actual = getItems(p);
        String[] pieces = actual.split(",");
        int i = 0;
        for (String s : pieces) {
            assertTrue("retrieved an op not in expected list: " + s, expected.contains(s));
            i++;
        }
        assertTrue("Returned wrong number of opset ops", i == expected.size());

        p = dao.getOpsetOps(null, "OPSET");
        assertTrue("OPSET is not an opset.  Should have error", p.hasError());

        p = dao.getOpsetOps(null, null);
        assertTrue("Should have error with null opset", p.hasError());
    }

    @Test
    public void testGetOpsetInfo(){
        Packet p = new Packet();
        String actual = "";
        String id = "";
        try {
            id = dao.getEntityId("033A0D1C", "s");
            p = dao.getOpsetInfo(null, id);

            String[] expected = new String[]{"033A0D1C", id, "033A0D1C", "", "Operation set"};
            for(int i = 0; i < 5; i++){
                assertEquals("getOpsetInfo failed, expected: " + expected[i], expected[i], p.getItemStringValue(i));
            }

            ArrayList<String> expectedList = new ArrayList<String>(Arrays.asList(new String[]{
                    "Object attribute create object",
                    "Object attribute assign to"
            }));

            actual = getItems(p);
            String[] pieces = actual.split(",");
            int c = 0;
            for (int i = 5; i < p.size(); i++) {
                assertTrue("retrieved an op not in expected list: " + pieces[i], expectedList.contains(pieces[i]));
                c++;
            }
            assertTrue("Returned wrong number of opset ops", c == expectedList.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        p = dao.getOpsetInfo(null, null);
        assertTrue("getOpsetInfo should have error with null opset id", p.hasError());

        p = dao.getOpsetInfo(null, "");
        assertTrue("getOpsetInfo should have error with null opset id", p.hasError());
    }

    @Test
    public void testDeleteOpsetAndOp(){
        String result = "";
        String id = "";
        try {
            Packet p = dao.addOpsetAndOp(sessId, "opset3", "", "", "File write", null, null);
            try {
                id = p.getItemStringValue(0).split(":")[1];
            } catch (Exception e) {
                fail(e.getMessage());
            }
            p = dao.deleteOpsetAndOp(null, id, "File write");
            assertFalse("deleteing op from opset should not have an error", p.hasError());

            p = dao.deleteOpsetAndOp(null, id, null);
            assertFalse("deleting opset should not have error", p.hasError());

            p = dao.deleteOpsetAndOp(null, null, null);
            assertTrue("deleting null opset should have error", p.hasError());
        }finally{
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("opset3", "s")));
            } catch (Exception e) {}
        }
    }

    @Test
    public void testCreateSession(){
        Packet p = dao.createSession(null, null, "HOST", "bob", "bob");
        assertTrue("createSession should have error with unkown host name: " + p.getErrorMessage(), p.hasError());

        p = dao.createSession(null, null, sHost, "bob", "bob123");
        assertTrue("createSession should have error with wrong password: " + p.getErrorMessage(), p.hasError());

        p = dao.createSession(null, null, sHost, "bob123", "bob");
        assertTrue("createSession should have error with unkown host name: " + p.getErrorMessage(), p.hasError());
    }

    @Test
    public void testChangePassword(){
        try {
            Packet p = new Packet();
            String oldPass = "";
            String newPass = "";
            try {
                oldPass = dao.getUserPass(dao.getEntityId("bob", "u"));
                p = dao.changePassword(null, "bob", "bob", "bob123", "bob123");
                newPass = dao.getUserPass(dao.getEntityId("bob", "u"));
            } catch (Exception e) {
                fail(e.getMessage());
            }
            assertNotSame("changing password didn't work", oldPass, newPass);

            p = dao.changePassword(null, "bob", "bob123", "bob1234", "bob12345");
            assertTrue("changing password with confirm password mismatch should have error", p.hasError());

            p = dao.changePassword(null, "bob", "bob1234", "bob12345", "bob12345");
            assertTrue("changing password with wrong password should have error", p.hasError());

            p = dao.changePassword(null, "bob123", "bob123", "bob1234", "bob1234");
            assertTrue("changing password with wrong user name should have error", p.hasError());
        }finally{
            dao.changePassword(null, "bob", "bob123", "bob", "bob");
        }
    }

    @Test
    public void testDeleteSession(){
        Packet p = dao.createSession(null, null, sHost, "bob", "bob");
        String id = "";
        try{
            id = p.getItemStringValue(1);
        } catch (PacketException e) {
            fail(e.getMessage());
        }

        p = dao.deleteSession(null, id);
        assertFalse("deleting session should not have error", p.hasError());

        p = dao.deleteSession(null, null);
        assertTrue("deleting session eith null id should have error", p.hasError());
    }

    @Test
    public void testGetPermittedOps(){
    	String expected = "Object attribute create object attribute,Operation set assign to,Operation set assign,Object attribute create object,Entity represent,Object attribute assign to,Object attribute create operation set,Object attribute assign";
        String actual = "";
        String sessId = "";
        try {
            sessId = dao.createSession(null, null, sHost, "bob", "bob").getItemStringValue(1);
        } catch (Exception e) {
            fail("could not create session for tests");
        }

        try {
            Packet p = dao.getPermittedOps(sessId, null, "bob home");
            actual = getItems(p);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("failed getting permitted ops for bob on bob home", expected, actual);
    }

    @Ignore("revisit")
    @Test
    public void testGetObjEmailProps(){

    }

    @Ignore("revisit")
    @Test
    public void testGetPosNodeProperties(){

    }

    @Test
    public void testGetSessions(){
        Packet p = dao.getSessions(null);
        try {
            assertTrue("expecting only 1 session", p.size()==1);
            assertEquals("expected 1 session with id " + sessId, sessId, p.getItemStringValue(0).split(":")[1]);
        } catch (PacketException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetSessionInfo(){
        Packet p = dao.getSessionInfo(null, sessId);
        String actual = getItems(p);
        String expected = "";
        try{
            expected = dao.getEntityName(sessId, "ses") + ":" + sessId
                    + ",super:4"
                    + "," + sHost + ":" + dao.getEntityId(sHost, "h");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("did not get host info correctly", expected, actual);

        p = dao.getSessionInfo(null, null);
        assertTrue("getting session info should have error with null session id", p.hasError());
    }

    @Test
    public void testGetSessionUser(){
        Packet p = dao.getSessionUser(sessId);
        String actual = getItems(p);
        assertEquals("getting session user failed", "super", actual);

        p = dao.getSessionUser(null);
        assertTrue("expected error with null session id", p.hasError());

        p = dao.getSessionUser(String.valueOf(Integer.valueOf(sessId) + 1));
        assertTrue("expected error with invalid session id", p.hasError());
    }

    @Test
    public void testGetSessionName(){
        Packet p = dao.getSessionName(sessId);
        String actual = getItems(p);
        assertEquals("getting session name failed", "super@" + sHost, actual.split("-")[0]);

        p = dao.getSessionName(null);
        assertTrue("expected error with null session id", p.hasError());

        p = dao.getSessionName(String.valueOf(Integer.valueOf(sessId) + 1));
        assertTrue("expected error with invalid session id", p.hasError());
    }

    @Test
    public void testGetUserDescendants(){
        Packet p = new Packet();
        String actual = "";
        try {
            p = dao.getUserDescendants(null, dao.getEntityId("bob", "u"));
            assertFalse("packet should not have error for getUserDescendants", p.hasError());
            actual = getItems(p);
            ArrayList<String> expectedList = new ArrayList<String>();
            expectedList.add(dao.getEntityId("Schema Admins", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Contracting", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("OU users", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Top secret", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Secret", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Robert", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Doctor", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("DAC uattrs", PM_NODE.UATTR.value));
            expectedList.add(dao.getEntityId("Intern", PM_NODE.UATTR.value));
            String[] pieces = actual.split(",");
            int i = 0;
            for (String s : pieces) {
                assertTrue("retrieved an id not in expected list: " + s + ":" + dao.getEntityName(s, "a"), expectedList.contains(s));
                i++;
            }
            assertEquals("Did not get right number of descendants", expectedList.size(), i);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Ignore("revisit")
    @Test
    public void testExport(){
        //Packet p = dao.export(null, sessId);
        //assertFalse("export should not have an error", p.hasError());
    }

//    @Ignore("revisit")
    @Test
    public void testGetHostAppPaths(){
    	Packet p = new Packet();
        String actual = "";
        String expected = "";
        p = dao.getHostAppPaths(null, null,"P859310","Admin");
        actual = getItems(p);
        expected = "C:\\PM\\dist\\pm-admin-1.5.jar;C:\\PM\\dist\\pm-commons-1.5.jar;C:\\PM\\lib\\activation-1.1.jar;C:\\PM\\lib\\aopalliance-1.0.jar;C:\\PM\\lib\\asm-3.1.jar;C:\\PM\\lib\\bcmail-jdk15-1.44.jar;C:\\PM\\lib\\bcprov-jdk15-1.44.jar;C:\\PM\\lib\\cglib-2.2.1-v20090111.jar;C:\\PM\\lib\\colorchooser-1.0.jar;C:\\PM\\lib\\commons-logging-1.1.1.jar;C:\\PM\\lib\\fontbox-1.6.0.jar;C:\\PM\\lib\\guava-r09.jar;C:\\PM\\lib\\guice-3.0.jar;C:\\PM\\lib\\icu4j-3.8.jar;C:\\PM\\lib\\jarjar-1.0.jar;C:\\PM\\lib\\javax.inject-1.jar;C:\\PM\\lib\\javax.mail-1.4.4.jar;C:\\PM\\lib\\jempbox-1.6.0.jar;C:\\PM\\lib\\jfontchooser-1.0.5-pm.jar;C:\\PM\\lib\\jna-3.2.7-pm-platform.jar;C:\\PM\\lib\\jna-3.2.7-pm.jar;C:\\PM\\lib\\jsr305-1.3.7.jar;C:\\PM\\lib\\miglayout-3.7.3.1-swing.jar;C:\\PM\\lib\\pdfbox-1.6.0.jar;C:\\PM\\lib\\wrapper-3.2.3.jar;C:\\PM\\lib\\wrapper.jar,Admin >> ,gov.nist.csd.pm.admin.PmAdmin";
        System.out.println("expected is " +expected);
        System.out.println("actual is " +actual);
        assertEquals(expected,actual);
    }

    @Test
    public void testGetInstalledApps(){
        Packet p = dao.getInstalledApps(sHost);
        String actual = getItems(p);

        ArrayList<String> expectedList = new ArrayList<String>();
        expectedList.add("Admin");
        expectedList.add("Rich Text Editor");
        expectedList.add("Workflow Editor");
        expectedList.add("PDF Viewer");
        expectedList.add("e-grant");
        expectedList.add("Exporter");
        expectedList.add("Open Office");
        expectedList.add("Microsoft Office Launcher");
        expectedList.add("Med-Rec");
        expectedList.add("Acct-Rec");
        expectedList.add("Workflow Old");
        expectedList.add("Schema Builder");
        expectedList.add("Employee Record");
        expectedList.add("Table Editor");

        String[] pieces = actual.split(",");
        int i = 0;
        for (String s : pieces) {
            assertTrue("retrieved an app name not in expected list: " + s, expectedList.contains(s));
            i++;
        }
        assertEquals("Did not get right number of app names", expectedList.size(), i);

        p = dao.getInstalledApps(null);
        assertTrue("Error expected with null host name", p.hasError());

        p = dao.getInstalledApps("MyHost");
        assertTrue("Error expected with unknown host name", p.hasError());
    }

    @Test
    public void testGetHostRepository(){
        Packet p = dao.getHostRepository(sessId);
        assertFalse("getting host repository should not have error", p.hasError());

        p = dao.getHostRepository(null);
        assertTrue("getting host repository should have error with null session id", p.hasError());

        p = dao.getHostRepository("1234");
        assertTrue("getting host repository should have error with unknown session id", p.hasError());
    }

    @Test
    public void testAddHostApp(){
    	Packet p = new Packet();
        String actual = "";
        String expected = "";
        p = dao.addHostApp(null,"P859310","MSExcel","C:\\PM\\dist\\pm-admin-1.5.jar;C:\\PM\\dist\\pm-commons-1.5.jar;C:\\PM\\lib\\activation-1.1.jar;C:\\PM\\lib\\aopalliance-1.0.jar;C:\\PM\\lib\\asm-3.1.jar;C:\\PM\\lib\\bcmail-jdk15-1.44.jar;C:\\PM\\lib\\bcprov-jdk15-1.44.jar;C:\\PM\\lib\\cglib-2.2.1-v20090111.jar;C:\\PM\\lib\\colorchooser-1.0.jar;C:\\PM\\lib\\commons-logging-1.1.1.jar;C:\\PM\\lib\\fontbox-1.6.0.jar;C:\\PM\\lib\\guava-r09.jar;C:\\PM\\lib\\guice-3.0.jar;C:\\PM\\lib\\icu4j-3.8.jar;C:\\PM\\lib\\jarjar-1.0.jar;C:\\PM\\lib\\javax.inject-1.jar;C:\\PM\\lib\\javax.mail-1.4.4.jar;C:\\PM\\lib\\jempbox-1.6.0.jar;C:\\PM\\lib\\jfontchooser-1.0.5-pm.jar;C:\\PM\\lib\\jna-3.2.7-pm-platform.jar;C:\\PM\\lib\\jna-3.2.7-pm.jar;C:\\PM\\lib\\jsr305-1.3.7.jar;C:\\PM\\lib\\miglayout-3.7.3.1-swing.jar;C:\\PM\\lib\\pdfbox-1.6.0.jar;C:\\PM\\lib\\wrapper-3.2.3.jar;C:\\PM\\lib\\wrapper.jar", 
                "gov.nist.csd.pm.apps.PMExcel", "Excel >> ");
        p = dao.getHostAppPaths(null, null,"P859310","MSExcel");
        actual = getItems(p);
        expected = "C:\\PM\\dist\\pm-admin-1.5.jar;C:\\PM\\dist\\pm-commons-1.5.jar;C:\\PM\\lib\\activation-1.1.jar;C:\\PM\\lib\\aopalliance-1.0.jar;C:\\PM\\lib\\asm-3.1.jar;C:\\PM\\lib\\bcmail-jdk15-1.44.jar;C:\\PM\\lib\\bcprov-jdk15-1.44.jar;C:\\PM\\lib\\cglib-2.2.1-v20090111.jar;C:\\PM\\lib\\colorchooser-1.0.jar;C:\\PM\\lib\\commons-logging-1.1.1.jar;C:\\PM\\lib\\fontbox-1.6.0.jar;C:\\PM\\lib\\guava-r09.jar;C:\\PM\\lib\\guice-3.0.jar;C:\\PM\\lib\\icu4j-3.8.jar;C:\\PM\\lib\\jarjar-1.0.jar;C:\\PM\\lib\\javax.inject-1.jar;C:\\PM\\lib\\javax.mail-1.4.4.jar;C:\\PM\\lib\\jempbox-1.6.0.jar;C:\\PM\\lib\\jfontchooser-1.0.5-pm.jar;C:\\PM\\lib\\jna-3.2.7-pm-platform.jar;C:\\PM\\lib\\jna-3.2.7-pm.jar;C:\\PM\\lib\\jsr305-1.3.7.jar;C:\\PM\\lib\\miglayout-3.7.3.1-swing.jar;C:\\PM\\lib\\pdfbox-1.6.0.jar;C:\\PM\\lib\\wrapper-3.2.3.jar;C:\\PM\\lib\\wrapper.jar,Excel >> ,gov.nist.csd.pm.apps.PMExcel";
        System.out.println("expected is " +expected);
        System.out.println("actual is " +actual);
        assertEquals(expected,actual);
        dao.deleteHostApp("MSExcel","P859310");
    }

    @Ignore("revisit")
    @Test
    public void testSetHostAppPaths(){

    }

    @Test
    public void testGetKStorePaths(){
        Packet p = dao.getKStorePaths(null, sessId);
        String actual = getItems(p);
        String expected = "C:\\PM\\keystores\\superKeystore,C:\\PM\\keystores\\clientTruststore";
        assertFalse("getKStorePaths should not have error", p.hasError());
        assertEquals(expected, actual);

        p = dao.getKStorePaths(null, null);
        assertTrue("getKStorePaths should have error", p.hasError());
    }

    @Test
    public void testSetKStorePaths(){
        Packet p = dao.getKStorePaths(null, sessId);
        String ks = "";
        String ts = "";
        try{
            ks = p.getItemStringValue(0);
            ts = p.getItemStringValue(1);
        } catch (PacketException e) {
            fail(e.getMessage());
        }
        p = dao.setKStorePaths(sessId, "4", sHost, ks, ts);
        assertFalse("setting keystore paths should not have error", p.hasError());

        p = dao.getKStorePaths(null, sessId);
        String actual = getItems(p);
        String expected = "'C:\\PM\\keystores\\superKeystore,C:\\PM\\keystores\\clientTruststore";
        assertEquals(expected,actual);
    }

    @Ignore("revisit")
    @Test
    public void testGetDenies(){

    }

    @Ignore("revisit")
    @Test
    public void testGetDenyInfo(){

    }

    @Test
    public void testGetAllOps(){
        Packet p = dao.getAllOps(null);
        String actual = getItems(p);

        ArrayList<String> expected = new ArrayList<String>(Arrays.asList(new String[]{
                "Class create class",
                "Class delete class",
                "*",
                "File modify",
                "File read and execute",
                "File read",
                "File write",
                "Dir modify",
                "Dir read and execute",
                "Dir list contents",
                "Dir read",
                "Dir write",
                "User create user attribute",
                "User assign",
                "User delete",
                "User delete assign",
                "Entity represent",
                "User attribute create user attribute",
                "User attribute create user",
                "User attribute delete user",
                "User attribute create operation set",
                "User attribute assign to operation set",
                "User attribute assign",
                "User attribute assign to",
                "User attribute delete",
                "User attribute delete assign",
                "User attribute delete assign to",
                "Object delete",
                "Object attribute create object",
                "Object attribute delete object",
                "Object attribute create object attribute",
                "Object attribute delete object attribute",
                "Object attribute create operation set",
                "Object attribute assign",
                "Object attribute assign to",
                "Object attribute delete",
                "Object attribute delete assign",
                "Object attribute delete assign to",
                "Policy class create user attribute",
                "Policy class delete user attribute",
                "Policy class create object attribute",
                "Policy class delete object attribute",
                "Policy class create object",
                "Policy class assign",
                "Policy class assign to",
                "Policy class delete",
                "Policy class delete assign",
                "Policy class delete assign to",
                "Operation set assign",
                "Operation set assign to",
                "Operation set delete",
                "Operation set delete assign",
                "Operation set delete assign to",
                "Connector create policy class",
                "Connector delete policy class",
                "Connector create user",
                "Connector delete user",
                "Connector create user attribute",
                "Connector delete user attribute",
                "Connector create object attribute",
                "Connector delete object attribute",
                "Connector create object",
                "Connector create operation set",
                "Connector assign to",
                "Connector delete assign to"
        }));

        String[] pieces = actual.split(",");
        int i = 0;
        for (String s : pieces) {
            assertTrue("retrieved an op not in expected list: " + s, expected.contains(s));
            i++;
        }
        assertEquals("Did not get right number of ops", expected.size(), i);
    }

    @Test
    public void testGetObjAttrsProper(){
        Packet p = dao.getObjAttrsProper(null);
        String actual = getItems(p);
        ArrayList<String> expected = new ArrayList<String>();
        try{
            expected.add("everything:" + dao.getEntityId("everything","b"));
            expected.add("S_TS:" + dao.getEntityId("S_TS","b"));
            expected.add("witems:" + dao.getEntityId("witems","b"));
            expected.add("katie home:" + dao.getEntityId("katie home","b"));
            expected.add("inboxes:" + dao.getEntityId("inboxes","b"));
            expected.add("charlie home:" + dao.getEntityId("charlie home","b"));
            expected.add("bob home:" + dao.getEntityId("bob home","b"));
            expected.add("alice home:" + dao.getEntityId("alice home","b"));
            expected.add("Acct Recs:" + dao.getEntityId("Acct Recs","b"));
            expected.add("Today:" + dao.getEntityId("Today","b"));
            expected.add("Populated Forms:" + dao.getEntityId("Populated Forms","b"));
            expected.add("outboxes:" + dao.getEntityId("outboxes","b"));
            expected.add("CMRecs:" + dao.getEntityId("CMRecs","b"));
            expected.add("Records:" + dao.getEntityId("Records","b"));
            expected.add("Acct Columns:" + dao.getEntityId("Acct Columns","b"));
            expected.add("CMR Columns:" + dao.getEntityId("CMR Columns","b"));
            expected.add("Accts Pbl witems:" + dao.getEntityId("Accts Pbl witems","b"));
            expected.add("Contracting witems:" + dao.getEntityId("Contracting witems","b"));
            expected.add("Accts Rcv witems:" + dao.getEntityId("Accts Rcv witems","b"));
            expected.add("Approved Orders:" + dao.getEntityId("Approved Orders","b"));
            expected.add("Forms:" + dao.getEntityId("Forms","b"));
            expected.add("Med Records:" + dao.getEntityId("Med Records","b"));
            expected.add("OU messages:" + dao.getEntityId("OU messages","b"));
            expected.add("S:" + dao.getEntityId("S","b"));
            expected.add("TS:" + dao.getEntityId("TS","b"));
            expected.add("12345678:" + dao.getEntityId("12345678","b"));
            expected.add("katie witems:" + dao.getEntityId("katie witems","b"));
            expected.add("charlie witems:" + dao.getEntityId("charlie witems","b"));
            expected.add("bob witems:" + dao.getEntityId("bob witems","b"));
            expected.add("alice witems:" + dao.getEntityId("alice witems","b"));
            expected.add("katie INBOX:" + dao.getEntityId("katie INBOX","b"));
            expected.add("OU inboxes:" + dao.getEntityId("OU inboxes","b"));
            expected.add("SharedContainer:" + dao.getEntityId("SharedContainer","b"));
            expected.add("Bob Med Records:" + dao.getEntityId("Bob Med Records","b"));
            expected.add("Proposals:" + dao.getEntityId("Proposals","b"));
            expected.add("Alice Med Records:" + dao.getEntityId("Alice Med Records","b"));
            expected.add("OU outboxes:" + dao.getEntityId("OU outboxes","b"));
            expected.add("katie OUTBOX:" + dao.getEntityId("katie OUTBOX","b"));
            expected.add("AcctAddr:" + dao.getEntityId("AcctAddr","b"));
            expected.add("AcctSsn:" + dao.getEntityId("AcctSsn","b"));
            expected.add("AcctName:" + dao.getEntityId("AcctName","b"));
            expected.add("AcctNum:" + dao.getEntityId("AcctNum","b"));
            expected.add("PatDrafts:" + dao.getEntityId("PatDrafts","b"));
            expected.add("PatTreatment:" + dao.getEntityId("PatTreatment","b"));
            expected.add("PatDiag:" + dao.getEntityId("PatDiag","b"));
            expected.add("PatSymptoms:" + dao.getEntityId("PatSymptoms","b"));
            expected.add("PatAllergies:" + dao.getEntityId("PatAllergies","b"));
            expected.add("PatHistory:" + dao.getEntityId("PatHistory","b"));
            expected.add("PatBio:" + dao.getEntityId("PatBio","b"));
            expected.add("PatId:" + dao.getEntityId("PatId","b"));
            expected.add("katie wINBOX:" + dao.getEntityId("katie wINBOX","b"));
            expected.add("alice INBOX:" + dao.getEntityId("alice INBOX","b"));
            expected.add("charlie INBOX:" + dao.getEntityId("charlie INBOX","b"));
            expected.add("bob INBOX:" + dao.getEntityId("bob INBOX","b"));
            expected.add("Charlie recipes:" + dao.getEntityId("Charlie recipes","b"));
            expected.add("8E094FF2:" + dao.getEntityId("8E094FF2","b"));
            expected.add("8B54E24B:" + dao.getEntityId("8B54E24B","b"));
            expected.add("charlie OUTBOX:" + dao.getEntityId("charlie OUTBOX","b"));
            expected.add("bob OUTBOX:" + dao.getEntityId("bob OUTBOX","b"));
            expected.add("alice OUTBOX:" + dao.getEntityId("alice OUTBOX","b"));
            expected.add("PatHistoryDrafts:" + dao.getEntityId("PatHistoryDrafts","b"));
            expected.add("PatDiagDrafts:" + dao.getEntityId("PatDiagDrafts","b"));
            expected.add("PatTreatmentDrafts:" + dao.getEntityId("PatTreatmentDrafts","b"));
            expected.add("alice wINBOX:" + dao.getEntityId("alice wINBOX","b"));
            expected.add("charlie wINBOX:" + dao.getEntityId("charlie wINBOX","b"));
            expected.add("bob wINBOX:" + dao.getEntityId("bob wINBOX","b"));
            expected.add("8E094FF2-Drafts:" + dao.getEntityId("8E094FF2-Drafts","b"));
            expected.add("8E094FF2-Treatments:" + dao.getEntityId("8E094FF2-Treatments","b"));
            expected.add("8E094FF2-History:" + dao.getEntityId("8E094FF2-History","b"));
            expected.add("8E094FF2-Diagnoses:" + dao.getEntityId("8E094FF2-Diagnoses","b"));
            expected.add("8B54E24B-Treatments:" + dao.getEntityId("8B54E24B-Treatments","b"));
            expected.add("8B54E24B-History:" + dao.getEntityId("8B54E24B-History","b"));
            expected.add("8B54E24B-Diagnoses:" + dao.getEntityId("8B54E24B-Diagnoses","b"));
            expected.add("8B54E24B-Drafts:" + dao.getEntityId("8B54E24B-Drafts","b"));
            expected.add("8E094FF2-DraftHistory:" + dao.getEntityId("8E094FF2-DraftHistory","b"));
            expected.add("8E094FF2-DraftDiagnoses:" + dao.getEntityId("8E094FF2-DraftDiagnoses","b"));
            expected.add("8E094FF2-DraftTreatments:" + dao.getEntityId("8E094FF2-DraftTreatments","b"));
            expected.add("8B54E24B-DraftTreatments:" + dao.getEntityId("8B54E24B-DraftTreatments","b"));
            expected.add("8B54E24B-DraftHistory:" + dao.getEntityId("8B54E24B-DraftHistory","b"));
            expected.add("8B54E24B-DraftDiagnoses:" + dao.getEntityId("8B54E24B-DraftDiagnoses","b"));

            String[] pieces = actual.split(",");
            int i = 0;
            for (String s : pieces) {
                assertTrue("retrieved an oattr not in expected list: " + s, expected.contains(s));
                i++;
            }
            assertEquals("Did not get right number of oattrs", expected.size(), i);
        }catch(Exception e){
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetOattrs(){
        Packet p = dao.getOattrs(null);
        String actual = getItems(p);
        ArrayList<String> expected = new ArrayList<String>();
        try{
            expected.add("12345678:" + dao.getEntityId("12345678", "b"));
            expected.add("237D8FA7:" + dao.getEntityId("237D8FA7", "b"));
            expected.add("30A44CB5:" + dao.getEntityId("30A44CB5", "b"));
            expected.add("33EAA2DF:" + dao.getEntityId("33EAA2DF", "b"));
            expected.add("39FA5BA8:" + dao.getEntityId("39FA5BA8", "b"));
            expected.add("4902BD22:" + dao.getEntityId("4902BD22", "b"));
            expected.add("53C1525D:" + dao.getEntityId("53C1525D", "b"));
            expected.add("58423CA7:" + dao.getEntityId("58423CA7", "b"));
            expected.add("6411940B:" + dao.getEntityId("6411940B", "b"));
            expected.add("7002DA15:" + dao.getEntityId("7002DA15", "b"));
            expected.add("8B54E24B:" + dao.getEntityId("8B54E24B", "b"));
            expected.add("8B54E24B-Diagnoses:" + dao.getEntityId("8B54E24B-Diagnoses", "b"));
            expected.add("8B54E24B-Diagnoses rep:" + dao.getEntityId("8B54E24B-Diagnoses rep", "b"));
            expected.add("8B54E24B-DraftDiagnoses:" + dao.getEntityId("8B54E24B-DraftDiagnoses", "b"));
            expected.add("8B54E24B-DraftHistory:" + dao.getEntityId("8B54E24B-DraftHistory", "b"));
            expected.add("8B54E24B-Drafts:" + dao.getEntityId("8B54E24B-Drafts", "b"));
            expected.add("8B54E24B-Drafts rep:" + dao.getEntityId("8B54E24B-Drafts rep", "b"));
            expected.add("8B54E24B-DraftTreatments:" + dao.getEntityId("8B54E24B-DraftTreatments", "b"));
            expected.add("8B54E24B-History:" + dao.getEntityId("8B54E24B-History", "b"));
            expected.add("8B54E24B-History rep:" + dao.getEntityId("8B54E24B-History rep", "b"));
            expected.add("8B54E24B-Treatments:" + dao.getEntityId("8B54E24B-Treatments", "b"));
            expected.add("8B54E24B-Treatments rep:" + dao.getEntityId("8B54E24B-Treatments rep", "b"));
            expected.add("8E094FF2:" + dao.getEntityId("8E094FF2", "b"));
            expected.add("8E094FF2-Diagnoses:" + dao.getEntityId("8E094FF2-Diagnoses", "b"));
            expected.add("8E094FF2-Diagnoses rep:" + dao.getEntityId("8E094FF2-Diagnoses rep", "b"));
            expected.add("8E094FF2-DraftDiagnoses:" + dao.getEntityId("8E094FF2-DraftDiagnoses", "b"));
            expected.add("8E094FF2-DraftHistory:" + dao.getEntityId("8E094FF2-DraftHistory", "b"));
            expected.add("8E094FF2-Drafts:" + dao.getEntityId("8E094FF2-Drafts", "b"));
            expected.add("8E094FF2-Drafts rep:" + dao.getEntityId("8E094FF2-Drafts rep", "b"));
            expected.add("8E094FF2-DraftTreatments:" + dao.getEntityId("8E094FF2-DraftTreatments", "b"));
            expected.add("8E094FF2-History:" + dao.getEntityId("8E094FF2-History", "b"));
            expected.add("8E094FF2-History rep:" + dao.getEntityId("8E094FF2-History rep", "b"));
            expected.add("8E094FF2-Treatments:" + dao.getEntityId("8E094FF2-Treatments", "b"));
            expected.add("8E094FF2-Treatments rep:" + dao.getEntityId("8E094FF2-Treatments rep", "b"));
            expected.add("91c1aa7f:" + dao.getEntityId("91c1aa7f", "b"));
            expected.add("933161a3:" + dao.getEntityId("933161a3", "b"));
            expected.add("9435D63E:" + dao.getEntityId("9435D63E", "b"));
            expected.add("9ce0521d:" + dao.getEntityId("9ce0521d", "b"));
            expected.add("Acct Columns:" + dao.getEntityId("Acct Columns", "b"));
            expected.add("Acct Columns rep:" + dao.getEntityId("Acct Columns rep", "b"));
            expected.add("Acct Recs:" + dao.getEntityId("Acct Recs", "b"));
            expected.add("Acct Recs rep:" + dao.getEntityId("Acct Recs rep", "b"));
            expected.add("AcctAddr:" + dao.getEntityId("AcctAddr", "b"));
            expected.add("AcctName:" + dao.getEntityId("AcctName", "b"));
            expected.add("AcctNum:" + dao.getEntityId("AcctNum", "b"));
            expected.add("Accts Pbl witems:" + dao.getEntityId("Accts Pbl witems", "b"));
            expected.add("Accts Rcv witems:" + dao.getEntityId("Accts Rcv witems", "b"));
            expected.add("AcctSsn:" + dao.getEntityId("AcctSsn", "b"));
            expected.add("alice home:" + dao.getEntityId("alice home", "b"));
            expected.add("alice home rep:" + dao.getEntityId("alice home rep", "b"));
            expected.add("alice INBOX:" + dao.getEntityId("alice INBOX", "b"));
            expected.add("Alice Med Records:" + dao.getEntityId("Alice Med Records", "b"));
            expected.add("Alice Med Records rep:" + dao.getEntityId("Alice Med Records rep", "b"));
            expected.add("alice OUTBOX:" + dao.getEntityId("alice OUTBOX", "b"));
            expected.add("alice OUTBOX rep:" + dao.getEntityId("alice OUTBOX rep", "b"));
            expected.add("alice wINBOX:" + dao.getEntityId("alice wINBOX", "b"));
            expected.add("alice witems:" + dao.getEntityId("alice witems", "b"));
            expected.add("Approved Orders:" + dao.getEntityId("Approved Orders", "b"));
            expected.add("bob home:" + dao.getEntityId("bob home", "b"));
            expected.add("bob home rep:" + dao.getEntityId("bob home rep", "b"));
            expected.add("bob INBOX:" + dao.getEntityId("bob INBOX", "b"));
            expected.add("Bob Med Records:" + dao.getEntityId("Bob Med Records", "b"));
            expected.add("Bob Med Records rep:" + dao.getEntityId("Bob Med Records rep", "b"));
            expected.add("bob OUTBOX:" + dao.getEntityId("bob OUTBOX", "b"));
            expected.add("bob OUTBOX rep:" + dao.getEntityId("bob OUTBOX rep", "b"));
            expected.add("bob wINBOX:" + dao.getEntityId("bob wINBOX", "b"));
            expected.add("bob witems:" + dao.getEntityId("bob witems", "b"));
            expected.add("C9CFE6DE:" + dao.getEntityId("C9CFE6DE", "b"));
            expected.add("charlie home:" + dao.getEntityId("charlie home", "b"));
            expected.add("charlie home rep:" + dao.getEntityId("charlie home rep", "b"));
            expected.add("charlie INBOX:" + dao.getEntityId("charlie INBOX", "b"));
            expected.add("charlie OUTBOX:" + dao.getEntityId("charlie OUTBOX", "b"));
            expected.add("charlie OUTBOX rep:" + dao.getEntityId("charlie OUTBOX rep", "b"));
            expected.add("Charlie recipes:" + dao.getEntityId("Charlie recipes", "b"));
            expected.add("charlie wINBOX:" + dao.getEntityId("charlie wINBOX", "b"));
            expected.add("charlie witems:" + dao.getEntityId("charlie witems", "b"));
            expected.add("Chili recipes:" + dao.getEntityId("Chili recipes", "b"));
            expected.add("CMR Columns:" + dao.getEntityId("CMR Columns", "b"));
            expected.add("CMR Columns rep:" + dao.getEntityId("CMR Columns rep", "b"));
            expected.add("CMRecs:" + dao.getEntityId("CMRecs", "b"));
            expected.add("CMRecs rep:" + dao.getEntityId("CMRecs rep", "b"));
            expected.add("Contracting witems:" + dao.getEntityId("Contracting witems", "b"));
            expected.add("d4cb3401:" + dao.getEntityId("d4cb3401", "b"));
            expected.add("D9971E4A:" + dao.getEntityId("D9971E4A", "b"));
            expected.add("DAC uattrs rep:" + dao.getEntityId("DAC uattrs rep", "b"));
            expected.add("E294203A:" + dao.getEntityId("E294203A", "b"));
            expected.add("E4B48FB1:" + dao.getEntityId("E4B48FB1", "b"));
            expected.add("E9663596:" + dao.getEntityId("E9663596", "b"));
            expected.add("everything:" + dao.getEntityId("everything", "b"));
            expected.add("FB40D908:" + dao.getEntityId("FB40D908", "b"));
            expected.add("FC15B612:" + dao.getEntityId("FC15B612", "b"));
            expected.add("FE2CA75B:" + dao.getEntityId("FE2CA75B", "b"));
            expected.add("Forms:" + dao.getEntityId("Forms", "b"));
            expected.add("inboxes:" + dao.getEntityId("inboxes", "b"));
            expected.add("inboxes rep:" + dao.getEntityId("inboxes rep", "b"));
            expected.add("Italian recipes:" + dao.getEntityId("Italian recipes", "b"));
            expected.add("katie home:" + dao.getEntityId("katie home", "b"));
            expected.add("katie home rep:" + dao.getEntityId("katie home rep", "b"));
            expected.add("katie INBOX:" + dao.getEntityId("katie INBOX", "b"));
            expected.add("katie OUTBOX:" + dao.getEntityId("katie OUTBOX", "b"));
            expected.add("katie OUTBOX rep:" + dao.getEntityId("katie OUTBOX rep", "b"));
            expected.add("katie wINBOX:" + dao.getEntityId("katie wINBOX", "b"));
            expected.add("katie witems:" + dao.getEntityId("katie witems", "b"));
            expected.add("Med Records:" + dao.getEntityId("Med Records", "b"));
            expected.add("mrec1:" + dao.getEntityId("mrec1", "b"));
            expected.add("mrec11:" + dao.getEntityId("mrec11", "b"));
            expected.add("mrec2:" + dao.getEntityId("mrec2", "b"));
            expected.add("mrec22:" + dao.getEntityId("mrec22", "b"));
            expected.add("mrec3:" + dao.getEntityId("mrec3", "b"));
            expected.add("mrec33:" + dao.getEntityId("mrec33", "b"));
            expected.add("mrec4:" + dao.getEntityId("mrec4", "b"));
            expected.add("mrec5:" + dao.getEntityId("mrec5", "b"));
            expected.add("OU inboxes:" + dao.getEntityId("OU inboxes", "b"));
            expected.add("OU messages:" + dao.getEntityId("OU messages", "b"));
            expected.add("OU messages rep:" + dao.getEntityId("OU messages rep", "b"));
            expected.add("OU outboxes:" + dao.getEntityId("OU outboxes", "b"));
            expected.add("outboxes:" + dao.getEntityId("outboxes", "b"));
            expected.add("PatAllergies:" + dao.getEntityId("PatAllergies", "b"));
            expected.add("PatAllergies rep:" + dao.getEntityId("PatAllergies rep", "b"));
            expected.add("PatBio:" + dao.getEntityId("PatBio", "b"));
            expected.add("PatBio rep:" + dao.getEntityId("PatBio rep", "b"));
            expected.add("PatDiag:" + dao.getEntityId("PatDiag", "b"));
            expected.add("PatDiag rep:" + dao.getEntityId("PatDiag rep", "b"));
            expected.add("PatDiagDrafts:" + dao.getEntityId("PatDiagDrafts", "b"));
            expected.add("PatDrafts:" + dao.getEntityId("PatDrafts", "b"));
            expected.add("PatDrafts rep:" + dao.getEntityId("PatDrafts rep", "b"));
            expected.add("PatHistory:" + dao.getEntityId("PatHistory", "b"));
            expected.add("PatHistory rep:" + dao.getEntityId("PatHistory rep", "b"));
            expected.add("PatHistoryDrafts:" + dao.getEntityId("PatHistoryDrafts", "b"));
            expected.add("PatId:" + dao.getEntityId("PatId", "b"));
            expected.add("PatId rep:" + dao.getEntityId("PatId rep", "b"));
            expected.add("PatSymptoms:" + dao.getEntityId("PatSymptoms", "b"));
            expected.add("PatSymptoms rep:" + dao.getEntityId("PatSymptoms rep", "b"));
            expected.add("PatTreatment:" + dao.getEntityId("PatTreatment", "b"));
            expected.add("PatTreatment rep:" + dao.getEntityId("PatTreatment rep", "b"));
            expected.add("PatTreatmentDrafts:" + dao.getEntityId("PatTreatmentDrafts", "b"));
            expected.add("poForm:" + dao.getEntityId("poForm", "b"));
            expected.add("Populated Forms:" + dao.getEntityId("Populated Forms", "b"));
            expected.add("Populated Forms rep:" + dao.getEntityId("Populated Forms rep", "b"));
            expected.add("prop1:" + dao.getEntityId("prop1", "b"));
            expected.add("Proposals:" + dao.getEntityId("Proposals", "b"));
            expected.add("Records:" + dao.getEntityId("Records", "b"));
            expected.add("S:" + dao.getEntityId("S", "b"));
            expected.add("S rep:" + dao.getEntityId("S rep", "b"));
            expected.add("Schema Builder rep:" + dao.getEntityId("Schema Builder rep", "b"));
            expected.add("SharedContainer:" + dao.getEntityId("SharedContainer", "b"));
            expected.add("S_TS:" + dao.getEntityId("S_TS", "b"));
            expected.add("Today:" + dao.getEntityId("Today", "b"));
            expected.add("TS:" + dao.getEntityId("TS", "b"));
            expected.add("TS rep:" + dao.getEntityId("TS rep", "b"));
            expected.add("witems:" + dao.getEntityId("witems","b"));

            String[] pieces = actual.split(",");
            int i = 0;
            for (String s : pieces) {
                assertTrue("retrieved an oattr not in expected list: " + s, expected.contains(s));
                i++;
            }
            assertEquals("Did not get right number of oattrs", expected.size(), i);
        }catch(Exception e){
            fail(e.getMessage());
        }
    }

    @Ignore("revisit")
    @Test
    public void testAddDeny(){

    }

    @Ignore("revisit")
    @Test
    public void testDeleteDeny(){

    }

    @Test
    public void testGetIdOfEntityWithNameAndType(){
        try {
            String actual = "";
            try {
                actual = dao.getEntityId(sessId, "everything", "b").getItemStringValue(0);
            } catch (PacketException e) {
                fail(e.getMessage());
            }
            assertEquals("5", actual);

            try {
                actual = dao.getEntityId(sessId, "*", "b").getItemStringValue(0);
            } catch (PacketException e) {
                fail(e.getMessage());
            }
            assertEquals("*", actual);

            try {
                actual = dao.getEntityId(sessId, "File", "oc").getItemStringValue(0);
            } catch (PacketException e) {
                fail(e.getMessage());
            }
            assertEquals("2", actual);

            String expected = "";
        /*try{
            Packet p = dao.addDenyInternal("deny", "user set", "superAdmin",
                    "3", "File write", "everything", "5", false);
            expected = p.getItemStringValue(0).split(":")[1];
            actual = dao.getEntityId("deny", "deny");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getting deny id failed", expected, actual);*/

            Packet p = dao.addTemplate(null, "tpl", "everything", "everything");
            try {
                actual = dao.getEntityId(sessId, "tpl", "tpl").getItemStringValue(0);
            } catch (PacketException e) {
                fail(e.getMessage());
            }
            assertNotNull("getting template id failed", actual);

            try {
                actual = dao.getEntityId(sessId, sHost, "h").getItemStringValue(0);
            } catch (PacketException e) {
                fail(e.getMessage());
            }
            assertEquals("failed getting id for host", "100", actual);
        }finally{
            try {
                dao.deleteTemplate(sessId, dao.getEntityId("tpl", "tpl"), "tpl");
            } catch (Exception e) {
                fail("could not delete template");
            }
        }
    }

    @Test
    public void testGetAttrInfo(){
        Packet p = dao.getAttrInfo(sessId, "5", "b", "no");
        assertFalse("getting attribute info for everything should not have an error", p.hasError());

        p = dao.getAttrInfo(sessId, "4", "u", "no");
        assertTrue("getting attribute info for user should have error", p.hasError());

        p = dao.getAttrInfo(sessId, null, "b", "no");
        assertTrue("getting attribute info for null id should have error", p.hasError());

        p = dao.getAttrInfo(sessId, "", "b", "no");
        assertTrue("getting attribute info for empty id should have error", p.hasError());
    }

    @Test
    public void testAddProp(){
        try{
            Packet p = dao.addProp(sessId, "5", "b", "no", "prop=1234");
            assertFalse("adding prop to everything should not have error: " + p.getErrorMessage(), p.hasError());

            p = dao.addProp(sessId, "5", "b", "no", "prop=1234");
            assertTrue("adding duplicate prop shoudl have error: " + p.getErrorMessage(), p.hasError());

            p = dao.addProp(sessId, null, "b", "no", "prop=prop");
            assertTrue("adding prop with null id should have error: " + p.getErrorMessage(), p.hasError());

            p = dao.addProp(sessId, "", "b", "no", "prop=prop");
            assertTrue("adding prop with empty id should have error: " + p.getErrorMessage(), p.hasError());

            p = dao.addProp(sessId, "4", "u", "no", "prop=prop");
            assertTrue("adding prop to user should have error: " + p.getErrorMessage(), p.hasError());
        }finally{
            dao.removeProp(sessId, "5", "b", "no", "prop=1234");
        }
    }

    @Test
    public void testReplaceProp(){
        try{
            Packet p = dao.addProp(sessId, "5", "b", "no", "prop=1234");
            assertFalse("adding prop to everything should not have error", p.hasError());

            p = dao.replaceProp(sessId, "5", "b", "no", "prop=1234", "prop=12345");
            assertFalse("replacing prop for everything should not have error", p.hasError());

            p = dao.replaceProp(sessId, "5", "b", "no", "", "prop=12345");
            assertTrue("replacing prop for everything with empty old prop should have error", p.hasError());

            p = dao.replaceProp(sessId, "5", "b", "no", "prop=12345", "");
            assertTrue("replacing prop for everything with empty old prop should have error", p.hasError());

            p = dao.replaceProp(sessId, null, "b", "no", "prop=prop", "prop=prop");
            assertTrue("replacing prop with null id should have error", p.hasError());

            p = dao.replaceProp(sessId, "", "b", "no", "prop=prop", "prop=prop");
            assertTrue("replacing prop with empty id should have error", p.hasError());

            p = dao.replaceProp(sessId, "4", "u", "no", "prop=prop", "prop=prop");
            assertTrue("replacing prop to user should have error", p.hasError());
        }finally{
            dao.removeProp(sessId, "5", "b", "no", "prop=12345");
        }
    }

    @Test
    public void testRemoveProp(){
        Packet p = dao.addProp(sessId, "5", "b", "no", "prop=1234");

        p = dao.removeProp(sessId, "5", "b", "no", "prop=1234");
        assertFalse("removing prop should not have an error", p.hasError());

        p = dao.removeProp(sessId, null, "b", "no", "prop=prop");
        assertTrue("removing prop with null id should have error", p.hasError());

        p = dao.removeProp(sessId, "", "b", "no", "prop=prop");
        assertTrue("removing prop with empty id should have error", p.hasError());

        p = dao.removeProp(sessId, "4", "u", "no", "prop=prop");
        assertTrue("removing prop to user should have error", p.hasError());
    }

    @Ignore("revisit")
    @Test
    public void testCreateObject3(){

    }

    @Test
    public void testGetUserInfo(){
        Packet p = dao.getUserInfo(sessId, "4");
        String actual = getItems(p);
        String expected = "super:4,,,SuperFirst";
        assertEquals("getting user info for super failed", expected, actual);

        p = dao.getUserInfo(sessId, "5");
        assertTrue("getting user info for an oa should fail", p.hasError());

        p = dao.getUserInfo(sessId, null);
        assertTrue("getting user info for null should fail", p.hasError());
    }

    @Test
    public void testGetOpsetsBetween(){
        Packet p = dao.getOpsetsBetween(sessId, "superAdmin", "everything", "b");
        String actual = getItems(p);
        assertEquals("getopsetsbetween failed. Expected an opset", "all ops:7", actual);

        p = dao.getOpsetsBetween(sessId, "Doctor", "everything", "b");
        assertEquals("getopsetsbetween failed. Expected no opsets", 0, p.size());

        p = dao.getOpsetsBetween(sessId, null, "everything", "b");
        assertTrue("getopsetsbetween failed. Expected error with null ua", p.hasError());

        p = dao.getOpsetsBetween(sessId, "Doctor", null, "b");
        assertTrue("getopsetsbetween failed. Expected error with null oa", p.hasError());
    }

    @Ignore("revisit")
    @Test
    public void testDeleteOpsetsBetween(){

    }

    @Test
    public void testGetOpsetOattrs(){
        Packet p = dao.getOpsetOattrs(sessId, "all ops");
        String actual = getItems(p);
        assertEquals("getting opsets between all ops and everything failed", "everything:5", actual);

        p = dao.getOpsetOattrs(sessId, "OPSET");
        assertTrue("getting opset oattrs with unknown opset should have error", p.hasError());

        p = dao.getOpsetOattrs(sessId, null);
        assertTrue("getting opset oattrs with null opset should have error", p.hasError());

        p = dao.getOpsetOattrs(sessId, "");
        assertTrue("getting opset oattrs with empty opset name should have error", p.hasError());
    }

    @Test
    public void testIsolateOattr(){
        Packet p = new Packet();
        String actual = "";
        String id = "";
        try {
            try {
                p = dao.addOattr(sessId, null, "OATTR", "", "", dao.getEntityId("RBAC", "p"), "p", "no", null, new String[]{"prop=123123"});
                actual = p.getItemStringValue(0).split(":")[0];
                id = p.getItemStringValue(0).split(":")[1];
            } catch (Exception e) {
                fail(e.getMessage());
            }

            p = dao.isolateOattr("OATTR", "b");
            assertFalse("isolating OATTR failed", p.hasError());

            p = dao.isolateOattr(null, "");
            assertTrue("isolating null oattr should fail", p.hasError());

            p = dao.isolateOattr("", "");
            assertTrue("isolating null oattr should fail", p.hasError());
        }finally{
            try {
                dao.deleteNode(new Integer(id));
            } catch (Exception e) {}
        }
    }

    @Ignore("revisit")
    @Test
    public void testSetPerms(){

    }

    @Test
    public void testGetEntityWithProp(){
        Packet p = dao.getEntityWithProp(sessId, "p", "type=rbac");
        String actual = getItems(p);
        String expected = "";
        try {
            expected = "RBAC:" + dao.getEntityId("RBAC", "p");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals("getting entity with prop failed", expected, actual);

        p = dao.getEntityWithProp(sessId, "p", "prop");
        assertTrue("getting entity with unused prop should fail", p.hasError());

        p = dao.getEntityWithProp(sessId, "p", null);
        assertTrue("getting entity with null prop should fail", p.hasError());
    }

    @Ignore("revisit")
    @Test
    public void testDoDacConfinement(){

    }

    @Test
    public void testIsAssigned(){
        Packet p = new Packet();
        String actual = "";
        try {
            p = dao.isAssigned(dao.getEntityId("PatTreatment", "b"), "b", dao.getEntityId("RBAC", "p"), "p");
            actual = p.getItemStringValue(0);
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertEquals("isAssigned failed for assigned entities", "yes", actual);

        try {
            p = dao.isAssigned(dao.getEntityId("PatTreatment", "b"), "b", dao.getEntityId("DAC", "p"), "p");
            actual = p.getItemStringValue(0);
        }catch(Exception e){
            fail(e.getMessage());
        }
        assertEquals("isAssigned failed for not assigned entities", "no", actual);

        p = dao.isAssigned(null, null, null, null);
        assertTrue("isAssigned should have error with null ids", p.hasError());
    }

    @Test
    public void testGetEntityName() {
        Packet p = dao.getEntityName(sessId, "5", "b");
        String actual = getItems(p);
        assertEquals("getEntityName with oattr did not work", "everything", actual);

        p = dao.getEntityName(sessId, "7", "oc");
        actual = getItems(p);
        assertEquals("getEntityName with oattr class did not work", "Object attribute", actual);

        //need deny TODO
        //script
        //template

        p = dao.getEntityName(sessId, sessId, "ses");
        assertFalse("getEntityName with session did not work", p.hasError());

        p = dao.getEntityName(sessId, null, "b");
        assertTrue("getEntityName should have returned error with null id", p.hasError());
    }

    @Ignore("revisit")
    @Test
    public void testGenConfForDacUser(){

    }

    @Test
    public void testAddEmailAcct(){
        Packet p = dao.addEmailAcct(sessId, "USER", null, null, null, null, null, null);
        assertTrue("adding email acct for invalid user should fail", p.hasError());

        p = dao.addEmailAcct(sessId, "bob", "Robert", null, null, null, null, null);
        assertTrue("updating with null email should fail", p.hasError());

        p = dao.addEmailAcct(sessId, null, null, null, null, null, null, null);
        assertTrue("adding email acct for null user should fail", p.hasError());

        p = dao.addEmailAcct(sessId, "dave", "David", null, null, null, null, null);
        assertTrue("adding new null email should fail", p.hasError());
    }

    @Test
    public void testGetEmailAcct(){
        Packet p = dao.getEmailAcct(sessId, "bob");
        String actual = getItems(p);
        String expected = "";
        try {
            expected = "bob:" + dao.getEntityId("bob", "u")
                    + ",bob,bob@nist.gov,email.nist.gov,email.nist.gov,nist";
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals("getting email acct failed", expected, actual);

        p = dao.getEmailAcct(sessId, "USER");
        assertTrue("getting email acct should have failed for unknown user", p.hasError());

        p = dao.getEmailAcct(sessId, "USER");
        assertTrue("getting email acct should have failed for unknown user", p.hasError());
    }

    @Ignore("revisit")
    @Test
    public void testSendSimpleMessage(){

    }

    @Test
    public void testCopyObject(){
        try {
            Packet p = dao.copyObject(sessId, null, "poForm");
            System.out.println(getItems(p));
            assertFalse("copying object should not have error", p.hasError());

            p = dao.copyObject(sessId, null, "OBJECT");
            assertTrue("copying unknown object should have failed", p.hasError());

            p = dao.copyObject(sessId, null, null);
            assertTrue("copying null object should have error", p.hasError());
        }finally {
            try {
                dao.deleteNode(Integer.valueOf(dao.getEntityId("copyOfpoForm", PM_NODE.ASSOC.value)));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Ignore("revisit")
    @Test
    public void testGetEmailRecipients(){
        Packet p = dao.getEmailRecipients(sessId, "Doctor");
        String actual = getItems(p);
        String expected = "bob,alice";
        assertEquals("getting Email recipients for Doctor failed", expected, actual);

        p = dao.getEmailRecipients(sessId, "bob");
        actual = getItems(p);
        expected = "bob";
        assertEquals("getting Email recipients for bob failed", expected, actual);
    }

    @Test
    public void testGetUsersAndAttrs(){
        Packet p = dao.getUsersAndAttrs(sessId);
        String actual = getItems(p);
        ArrayList<String> expected = new ArrayList<String>();
        try {
            expected.add("superAdmin:" + dao.getEntityId("superAdmin", "a"));
            expected.add("Schema Admins:" + dao.getEntityId("Schema Admins", "a"));
            expected.add("Secret:" + dao.getEntityId("Secret", "a"));
            expected.add("Acct Mgr:" + dao.getEntityId("Acct Mgr", "a"));
            expected.add("Acct Repr:" + dao.getEntityId("Acct Repr", "a"));
            expected.add("Nurse:" + dao.getEntityId("Nurse", "a"));
            expected.add("Adm Clerk:" + dao.getEntityId("Adm Clerk", "a"));
            expected.add("DAC uattrs:" + dao.getEntityId("DAC uattrs", "a"));
            expected.add("Accts Pbl:" + dao.getEntityId("Accts Pbl", "a"));
            expected.add("Contracting:" + dao.getEntityId("Contracting", "a"));
            expected.add("Accts Rcv:" + dao.getEntityId("Accts Rcv", "a"));
            expected.add("Acquisition:" + dao.getEntityId("Acquisition", "a"));
            expected.add("Secretary:" + dao.getEntityId("Secretary", "a"));
            expected.add("Intern:" + dao.getEntityId("Intern", "a"));
            expected.add("OU users:" + dao.getEntityId("OU users", "a"));
            expected.add("Top secret:" + dao.getEntityId("Top secret", "a"));
            expected.add("David:" + dao.getEntityId("David", "a"));
            expected.add("Katherine:" + dao.getEntityId("Katherine", "a"));
            expected.add("Charles:" + dao.getEntityId("Charles", "a"));
            expected.add("Robert:" + dao.getEntityId("Robert", "a"));
            expected.add("Alicia:" + dao.getEntityId("Alicia", "a"));
            expected.add("ExporterUA:" + dao.getEntityId("ExporterUA", "a"));
            expected.add("Doctor:" + dao.getEntityId("Doctor", "a"));

            expected.add("super:" + dao.getEntityId("super", "u"));
            expected.add("katie:" + dao.getEntityId("katie", "u"));
            expected.add("alice:" + dao.getEntityId("alice", "u"));
            expected.add("dave:" + dao.getEntityId("dave", "u"));
            expected.add("charlie:" + dao.getEntityId("charlie", "u"));
            expected.add("exporter:" + dao.getEntityId("exporter", "u"));
            expected.add("bob:" + dao.getEntityId("bob", "u"));
        }catch(Exception e){
            fail(e.getMessage());
        }

        String[] pieces = actual.split(",");
        int i = 0;
        for (String s : pieces) {
            assertTrue("retrieved an u or ua not in expected list: " + s, expected.contains(s));
            i++;
        }
        assertEquals("Did not get right number of u and uas", expected.size(), i);
    }

    @Test
    public void testAddOpenObj() {
        try {
            Packet p = dao.addOpenObj(sessId, "poForm");
            assertFalse("addOpenObj should not have failed: " + p.getErrorMessage(), p.hasError());

            p = dao.addOpenObj(sessId, "OBJECT");
            assertTrue("addOpenObj should have failed with unknown object name", p.hasError());

            p = dao.addOpenObj(sessId, null);
            assertTrue("addOpenObj should have failed with null object name", p.hasError());
        }finally{
            dao.deleteOpenObj(sessId, "poForm");
        }
    }

    @Test
    public void testDeleteOpenObj() {
        Packet p = dao.deleteOpenObj(sessId, "OBJECT");
        assertTrue("addOpenObj should have failed with unknown object name", p.hasError());

        p = dao.deleteOpenObj(sessId, null);
        assertTrue("addOpenObj should have failed with null object name", p.hasError());
    }

    @Test
    public void testAssignObjToOattrWithProp(){
        try {
            Packet p = dao.assignObjToOattrWithProp(sessId, null, "poForm", "inboxof=bob");
            assertFalse("assigning obj to oattr with prop inboxof=bob failed: " + p.getErrorMessage(), p.hasError());

            p = dao.assignObjToOattrWithProp(sessId, null, null, "inboxof=bob");
            assertTrue("expected failure with null obj", p.hasError());

            p = dao.assignObjToOattrWithProp(sessId, null, "poForm", null);
            assertTrue("expected failure with null property", p.hasError());
        }finally{
            try {
                dao.deleteAssignment(sessId, null,
                        dao.getEntityId("poForm", "o"), "o",
                        dao.getEntityId("bob INBOX", "b"), "b", "no");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testAssignObjToOattr(){
        try {
            Packet p = dao.assignObjToOattr(sessId, null, "poForm", "bob INBOX");
            assertFalse("assigning obj to oattr failed: " + p.getErrorMessage(), p.hasError());

            p = dao.assignObjToOattr(sessId, null, null, "bob INBOX");
            assertTrue("expected failure with null obj", p.hasError());

            p = dao.assignObjToOattr(sessId, null, "poForm", null);
            assertTrue("expected failure with null oattr", p.hasError());
        }finally{
            try {
                dao.deleteAssignment(sessId, null,
                        dao.getEntityId("poForm", "o"), "o",
                        dao.getEntityId("bob INBOX", "b"), "b", "no");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDeassignObjToOattrWithProp(){
        try {
            Packet p = dao.assignObjToOattr(sessId, null, "poForm", "bob INBOX");

            p = dao.deassignObjFromOattrWithProp(sessId, null, "poForm", "inboxof=bob");
            assertFalse("assigning obj to oattr with prop inboxof=bob failed: " + p.getErrorMessage(), p.hasError());

            p = dao.deassignObjFromOattrWithProp(sessId, null, null, "inboxof=bob");
            assertTrue("expected failure with null obj", p.hasError());

            p = dao.deassignObjFromOattrWithProp(sessId, null, "poForm", null);
            assertTrue("expected failure with null property", p.hasError());
        }finally{
            try {
                dao.deleteAssignment(sessId, null,
                        dao.getEntityId("poForm", "o"), "o",
                        dao.getEntityId("bob INBOX", "b"), "b", "no");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testIsObjInOattrWithProp(){
        try {
            Packet p = dao.assignObjToOattr(sessId, null, "poForm", "bob INBOX");

            p = dao.isObjInOattrWithProp(sessId, "poForm", "inboxof=bob");
            String actual = getItems(p);
            assertEquals("isObjInIattrWithProp failed when should be YES", "yes", actual);

            p = dao.isObjInOattrWithProp(sessId, "poForm", "inboxof=alice");
            actual = getItems(p);
            assertEquals("isObjInIattrWithProp failed when should be NO", "no", actual);

            p = dao.isObjInOattrWithProp(sessId, null, "inboxof=bob");
            actual = getItems(p);
            assertTrue("isObjInIattrWithProp should fail with null obj name", p.hasError());

            p = dao.isObjInOattrWithProp(sessId, "poForm", null);
            actual = getItems(p);
            assertTrue("isObjInIattrWithProp should fail with null property", p.hasError());
        }finally{
            try {
                dao.deleteAssignment(sessId, null,
                        dao.getEntityId("poForm", "o"), "o",
                        dao.getEntityId("bob INBOX", "b"), "b", "no");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Ignore("revisit")
    @Test
    public void testGetInboxMessages(){

    }

    @Ignore("revisit")
    @Test
    public void testGetOutboxMessages(){

    }

    @Ignore("revisit")
    @Test
    public void testGetFileContents(){

    }

    @Ignore("revisit")
    @Test
    public void testCreateObjForFile(){

    }

    @Ignore("revisit")
    @Test
    public void testCreateContForFolder(){

    }
}
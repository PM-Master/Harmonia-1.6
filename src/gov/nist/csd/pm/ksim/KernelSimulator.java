/*
 * KernelSimulator.java
 *
 * Created by Serban Gavrila
 * for KT Consulting, Inc.
 */
package gov.nist.csd.pm.ksim;

import com.google.common.base.Joiner;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.application.SSLSocketServer;
import static gov.nist.csd.pm.common.constants.GlobalConstants.*;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.net.PacketManager;
import gov.nist.csd.pm.common.pattern.cmd.Command;
import gov.nist.csd.pm.common.pattern.cmd.CommandLookup;
import gov.nist.csd.pm.common.pattern.cmd.Dispatcher;
import gov.nist.csd.pm.common.util.RandomGUID;
import gov.nist.csd.pm.common.util.swing.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.*;
import java.util.List;

import static gov.nist.csd.pm.common.info.Args.*;
import static gov.nist.csd.pm.common.info.CommunicationCodes.CODE_DATA;
import static gov.nist.csd.pm.common.info.CommunicationCodes.CODE_EOD;
import static gov.nist.csd.pm.common.info.PMCommand.*;
import static gov.nist.csd.pm.common.info.Permissions.*;
import static gov.nist.csd.pm.common.info.PmClasses.CLASS_FILE_NAME;
import static gov.nist.csd.pm.common.net.LocalHostInfo.getLocalHost;
import static gov.nist.csd.pm.common.net.LocalHostInfo.isLocalHost;
import static gov.nist.csd.pm.common.util.CommandUtil.makeCmd;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:01 $
 * @since 1.5
 */
@SuppressWarnings("CallToThreadDumpStack")
public class KernelSimulator {

    /*
     * Change made by REM
     * Put strings into constants for reuse.
     */
    public static final String CANNOT_READ_THE_FILE_S_ON_HOST_S_ERROR_FMTMSG = "Cannot read the file %s on host %s.";
    public static final String CLIPBOARD_PASTE_PERMISSION_VIOLATION_MSG = "You are not authorized to paste from the clipboard!";
    public static final String CLIPBOARD_SUFFIX = " clipboard";
    public static final String COMMAND_FORWARDING_ERROR_MSG = "Exception when forwarding the command to the engine!";

    public static final String DISCARD_OBJECT_DATA_ERROR_MSG = "Exception when discarding the object data!";
    public static final String FILE_STREAM_CREATION_ERROR_MSG = "Exception when creating streams for the requested file!";
    public static final String FILE_S_CANNOT_BE_READ_ERROR_MSGFMT = "File %s cannot be read!";
    public static final String FILE_S_DOES_NOT_EXIST_ERR_MSGFMT = "File %s does not exist!";
    public static final String FILE_S_NOT_ON_HOST_S_ERR_FMTMSG = "File %s does not exist on host %s!";
    public static final String EXCEPTION_IN_S_S_ERROR_FMTMSG = "Exception in %s: %s";
    public static final String EXCEPTION_IN_DO_CREATE_PROCESS_MSG = "Exception in doCreateProcess(): ";
    public static final String INVALID_OBJECT_HANDLE_ERROR_MSG = "Invalid object handle!";
    public static final String LOCALHOST_ADDY_NOT_FOUND_ERROR_MSG = "Internal error: couldn't get local host address or name";
    public static final String LOCALHOST_OBJECT_WRITE_ERROR_MSG = "Exception when writing the object to the local host!";
    public static final String NOT_IMPLEMENTED_ERROR_MSG = "Not implemented!";
    public static final String NOT_THIS_OBJECTS_HOST_ERROR_MSG = "This is not the object's host!";
    public static final String NULL_OR_EMPTY_FILE_PATH_ERROR_MSG = "Null or empty file path!";
    public static final String NULL_OR_INCOMPLETE_COMMAND_RECEIVED_ERROR_MSG = "Null or incomplete command received!";
    public static final String READ_PERMISSION_VIOLATION_ERROR_MSG = "You don't have permission to read this object!";
    public static final String PROCESS_S_IS_NOT_MAPPED_TO_A_SESSION_FMTMSG = "Process %s is not mapped to a session!";
    public static final String SESSION_S_IS_NOT_MAPPED_FMTMSG = "Session %s is not mapped!";
    public static final String SESSION_S_IS_NOT_MAPPED_TO_PROCESS_S_FMTMSG = "Session %s is not mapped to process %s";
    public static final String SIMULATOR_CHECK_S_S_D_ERROR_FMTMSG = "Please check whether the simulator has been started on %s. (Unable to create SSL socket for %s:%i)";
    public static final String SOURCE_FILE_S_ERROR_MSG = "Something's wrong with the source file %s !";
    public static final String S_IS_NOT_A_FILE_ERROR_FMTMSG = "%s is not a file!";
    public static final String S_IS_NOT_A_FILE_ON_HOST_S_ERROR_FMTMSG = "%s is not a file on host %s";
    public static final String S_S_IS_NOT_THE_LOCAL_HOST_ERROR_FMTMSG = "(%s) %s is not the local host!";
    public static final String UNKNOWN_COMMAND_ERROR_MSG = "Unknown command";
    public static final String UNSPECIFIED_ERROR_MSG = "Unspecified error";
    public static final String WRITE_PERMISSION_VIOLATION_ERROR_MSG = "You don't have permission to write this object!";
    public static final String TOO_FEW_ARGUMENTS_ERROR_MSG = "Too few arguments";

    public static final String DNR_EMPTY_PAYLOAD = "";
    public static final String FAILURE_PACKET_PAYLOAD = "no";
    public static final String OK_PACKET_PAYLOAD = "Ok";
    public static final String SUCCESS_MSG = "Success";
    public static final String KERNEL_TO_KERNEL_LOG_PREFIX = "c,K-K";

    public static final String PM_DEFAULT_ENGINE_HOST = "localhost";
    public static final int PM_MAX_BLOCK_SIZE = 16384;


    public static final String PM_FIELD_DELIM = ":";
    public static final String PM_ALT_FIELD_DELIM = "|";
    public static final String PM_ALT_DELIM_PATTERN = "\\|";
    public static final String PM_TERMINATOR = ".";


    /**
	 * @uml.property  name="currentHost"
	 */
    private String currentHost;
    /**
	 * @uml.property  name="engineHost"
	 */
    private String engineHost;
    /**
	 * @uml.property  name="enginePort"
	 */
    private int enginePort;
    /**
	 * @uml.property  name="simulatorPort"
	 */
    private int simulatorPort;
    /**
	 * @uml.property  name="engineClient"
	 * @uml.associationEnd  
	 */
    private SSLSocketClient engineClient;
    /**
	 * @uml.property  name="simServer"
	 * @uml.associationEnd  
	 */
    private SSLSocketServer simServer;
    /**
	 * @uml.property  name="openHandles"
	 * @uml.associationEnd  inverse="this$0:gov.nist.csd.pm.ksim.KernelSimulator$OpenObjectRecord" qualifier="sHandle:java.lang.String gov.nist.csd.pm.ksim.KernelSimulator$OpenObjectRecord"
	 */
    private Map<String, OpenObjectRecord> openHandles;
    /**
	 * @uml.property  name="processToSession"
	 * @uml.associationEnd  qualifier="sPid:java.lang.String java.lang.String"
	 */
    private Map<String, String> processToSession;
    /**
	 * @uml.property  name="sessionToProcesses"
	 * @uml.associationEnd  multiplicity="(0 -1)" ordering="true" elementType="java.lang.String" qualifier="sSessId:java.lang.String java.util.HashSet"
	 */
    private Map<String, Set<String>> sessionToProcesses;
    /**
	 * @uml.property  name="debugFlag"
	 */
    private boolean debugFlag;
    /**
	 * @uml.property  name="sClipboard"
	 */
    private String sClipboard;
    // KernelSimulator constructor.
    // Builds the GUI - actually done by the SessionManager contructor.
    // Sets up the SSL client for engine communication.
    // Sets up its own SSL server for communication with the other hosts.

    public KernelSimulator(String engineHost, int enginePort,
                           int simulatorPort, boolean debugFlag) {

        this.engineHost = engineHost;
        this.enginePort = enginePort;
        this.simulatorPort = simulatorPort;
        this.debugFlag = debugFlag;

        currentHost = getLocalHost();

        // Set up the hash map for open object handles. An entry looks like:
        // <handle, openObjectRecord>. The handle is the key. The record
        // contains the permissions received from the engine at open, the
        // host and the path of the actual object.
        openHandles = new HashMap();

        // Set up the two mappings between process and session ids.
        processToSession = new HashMap();
        sessionToProcesses = new HashMap();

        // Set up the K-sim's SSL client for communications with the PM Server
        // (the engine) and check the connection.
        try {
            engineClient = new SSLSocketClient(engineHost, enginePort,
                    debugFlag, "c,K-E");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Please check whether the PM engine has been started"
                            + " (unable to create SSL socket for " + engineHost
                            + ":" + enginePort + ")");
            e.printStackTrace();
            System.exit(-1);
        }

        if (!connectedToEngine()) {
            JOptionPane.showMessageDialog(null,
                    "Unable to connect to the PM engine.");
            System.exit(-2);
        }
        /**
         * Consolidated SSLSocketServer as an abstract class.
         */
        simServer = new SSLSocketServer(simulatorPort, debugFlag, "s,K") {

            @Override
            public Packet executeCommand(String clientName, Packet command, InputStream fromClient, OutputStream toClient) {
                return KernelSimulator.this.executeCommand(clientName, command, fromClient, toClient);
            }
        };

        try {
            simServer.service();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to create the simulator's server socket.");
            System.exit(1);
        }
    }

    /**
	 * @return
	 * @uml.property  name="engineHost"
	 */
    public String getEngineHost() {
        return engineHost;
    }

    /**
	 * @return
	 * @uml.property  name="enginePort"
	 */
    public int getEnginePort() {
        return enginePort;
    }

    private boolean connectedToEngine() {
        try {
            Packet cmd = makeCmd("connect", null);
            Packet res = engineClient.sendReceive(cmd, null);
            if (res.hasError()) {
                System.out.println("Connect: " + res.getErrorMessage());
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connect: " + e.getMessage());
            return false;
        }
    }
    /*
     * Command dispatch in the KernelSimulator has been reimplemented in the style of the Command pattern
     */
    /**
	 * @uml.property  name="commandList"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.ksim.KernelSimulator$14"
	 */
    List<Command<PacketCommandArgs, Packet>> commandList = new ArrayList();
    /**
	 * Could use set logic instead of enumeration of all commands passed to the PM engine. All Commands - KSim Commands = PM Engine Commands The set of KSim Commands is much smaller.
	 * @uml.property  name="passToEngineCommands"
	 */
    Set<PMCommand> passToEngineCommands = EnumSet.of(ADD_HOST_APP,ADD_PROP, GET_ENTITY_WITH_PROP, GET_POLICY_CLASSES, GET_SIMPLE_VOS_GRAPH,
            GET_SEL_VOS_GRAPH, GET_SEL_VOS_GRAPH_2, COMPILE_SCRIPT_AND_ENABLE, COMPILE_SCRIPT_AND_ADD_TO_ENABLED,
            GET_SESSION_USER, SEND_SIMPLE_MSG, SEND_OBJECT, CREATE_SESSION, COMPUTE_VOS, GET_VOS_GRAPH,
            COMPUTE_FAST_VOS,
            GET_VOS_ID_PROPERTIES,
            GET_POS_NODE_PROPERTIES,
            GET_K_STORE_PATHS, SET_K_STORE_PATHS, DELETE_SESSION, GET_HOST_APP_PATHS,
            SET_HOST_APP_PATHS, GET_HOST_REPOSITORY, REQUEST_PERMS, 
            IS_TIME_TO_REFRESH, DELETE_NODE, DELETE_ASSIGNMENT, DELETE_CONTAINER_OBJECTS, ASSIGN, SPAWN_SESSION, CHANGE_PASSWORD,
            GET_USER_ATTRIBUTES, GET_OATTRS, GET_OBJ_NAME_PATH, GET_OPSETS_BETWEEN, GET_ALL_OPS, GET_OPSET_OATTRS,
            GET_OPSET_OPS,GET_OBJ_PROPERTIES, SET_PERMS, GET_EMAIL_ACCT, GET_OBJ_EMAIL_PROPS, GET_EMAIL_RECIPIENTS, DELETE_OPSETS_BETWEEN,
            GET_ENTITY_ID, ASSIGN_OBJ_TO_OATTR_WITH_PROP, ASSIGN_OBJ_TO_OATTR, DEASSIGN_OBJ_FROM_OATTR_WITH_PROP,
            GET_INBOX_MESSAGES, GET_OUTBOX_MESSAGES, 
            SET_STARTUPS,
            IS_OBJ_IN_OATTR_WITH_PROP, GET_OBJ_ATTRS_PROPER, ADD_TEMPLATE, GET_TEMPLATES, GET_TEMPLATE_INFO,
            GET_ENTITY_NAME, GET_DASC_OBJECTS, CREATE_RECORD, CREATE_RECORD_IN_ENTITY_WITH_PROP, GET_RECORDS,
            GET_RECORD_INFO, SET_RECORD_KEYS, ADD_RECORD_KEYS, GET_OBJ_INFO, GET_OBJ_PROPERTIES, IS_IN_POS, GET_USERS, GET_USERS_AND_ATTRS,
            GET_USERS_OF, GET_MEMBERS_OF, GET_MELL_MEMBERS_OF, GET_CONNECTOR, ADD_OATTR, GET_INSTALLED_APP_NAMES, CREATE_LINKED_OBJECTS,
            GET_CONTAINERS_OF, GET_MELL_CONTAINERS_OF
            );

    private Dispatcher<PacketCommandArgs, Packet> getKSimCommandDispatcher() {
        if (ksimCommandDispatcher == null) {
            //These are the commands that ksim is unconcerned with.
            for (PMCommand command : passToEngineCommands) {
                commandList.add(new PassToEngineCommand(command));
            }
            //These are the ones we care about.
            commandList.add(new PacketCommand(CONNECT) {
                @Override
                public Packet execute(Packet input) {
                    return successPacket(getClientName());
                }
            });
            commandList.add(new PacketCommand(CREATE_OBJECT_3, 8) {
                @Override
                public Packet execute(Packet input) {
                    return doCreateObject3(sessionId(), procId(), objectName(), objectClass(), objectType(), containers(), permissions(), input);
                }
            });
            commandList.add(new PacketCommand(GET_FILE_CONTENT) {
                @Override
                public Packet execute(Packet input) {
                    return doGetFileContent(input, getClientOutputStream());
                }
            });
            commandList.add(new PacketCommand(OPEN_OBJECT_3, 5) {
                @Override
                public Packet execute(Packet input) {
                    return doOpenObject3(sessionId(), procId(), objectName(), permissions());
                }
            });
            commandList.add(new PacketCommand(READ_OBJECT_3, 3) {
                @Override
                public Packet execute(Packet input) {
                    return doReadObject3(input, getClientOutputStream());
                }
            });
            commandList.add(new PacketCommand(WRITE_OBJECT_3) {
                @Override
                public Packet execute(Packet input) {
                    return doWriteObject3(input, getClientInputStream());
                }
            });
            commandList.add(new PacketCommand(CLOSE_OBJECT, 3) {
                @Override
                public Packet execute(Packet input) {
                    return doCloseObject(sessionId(), handle());
                }
            });
            commandList.add(new PacketCommand(NEW_READ_FILE, 4) {
                @Override
                public Packet execute(Packet input) {
                    return doNewReadFile(sessionId(), host(), path(), getClientOutputStream());
                }
            });
            commandList.add(new PacketCommand(NEW_WRITE_FILE, 4) {
                @Override
                public Packet execute(Packet input) {
                    return doLocalWriteFile(sessionId(), host(), path(), getClientInputStream());
                }
            });

            commandList.add(new PacketCommand(PMCommand.COPY_TO_CLIPBOARD, 4) {
                @Override
                public Packet execute(Packet input) {
                    return doCopyToClipboard(sessionId(), handle(), selectedText());
                }
            });

            commandList.add(new PacketCommand(IS_PASTING_ALLOWED, 3) {
                @Override
                public Packet execute(Packet input) {
                    return isPastingAllowed(sessionId(), procId());
                }
            });

            commandList.add(new PacketCommand(COPY_OBJECT, 4) {
                @Override
                public Packet execute(Packet input) {
                    return doCopyObject(sessionId(), procId(), objectName(), input);
                }
            });
            commandList.add(new PacketCommand(GET_DEVICES, 2) {
                @Override
                public Packet execute(Packet input) {
                    return doGetDevices(sessionId());
                }
            });
            commandList.add(new PacketCommand(CREATE_PROCESS, 2) {
                @Override
                public Packet execute(Packet input) {
                    return doCreateProcess(sessionId());
                }
            });
            commandList.add(new PacketCommand(MAY_SESSION_CLOSE, 2) {
                @Override
                public Packet execute(Packet input) {
                    return doMaySessionClose(sessionId());
                }
            });
            commandList.add(new PacketCommand(EXIT_PROCESS, 3) {
                @Override
                public Packet execute(Packet input) {
                    return doExitProcess(sessionId(), procId());
                }
            });
            commandList.add(new PacketCommand(COPY_OBJECT, 4) {
                @Override
                public Packet execute(Packet input) {
                    return doCopyObject(sessionId(), procId(), objectName(), input);
                }
            });


            Command<PacketCommandArgs, Packet> unknownCommand = new Command<PacketCommandArgs, Packet>() {

                @Override
                public Object commandKey() {
                    return "";
                }


                /**
                 * Implementation of a command
                 *
                 * @param input
                 * @return
                 */
                @Override
                public Packet execute(PacketCommandArgs input) {
                    return failurePacket(UNKNOWN_COMMAND_ERROR_MSG + ": " + input.getPacket().getStringValue(0));
                }
            };
            ksimCommandDispatcher = Dispatcher.create(ksimCommandLookup, unknownCommand, commandList);

        }
        return ksimCommandDispatcher;
    }

    //@AP - Should use the command pattern, which would enable the
    //      deconstruction of this 400 line method into reasonable
    //      chunks.
    //@Perf - This is a rather large synchronization block.
    //        Could newer multi-threading techniques help make this faster?
    //@Perf - equalsIngoreCase is inefficient.  Homogenize the input (upcase, downcase)
    //        then do the comparison on that input using equals (much faster).
    //@Perf,AP - Unnecessary casts from Object to Packet, this method can only return a packet.
    //           There are no checks before the casts either.
    //@Refactor -

    public synchronized Packet executeCommand(String sClientName, Packet cmd,
                                              InputStream bisFromClient, OutputStream bosToClient) {

        if (cmd == null || cmd.size() < 1) {
            return failurePacket(NULL_OR_INCOMPLETE_COMMAND_RECEIVED_ERROR_MSG);
        }
        // Dispatch the command
        PacketCommandArgs args = new PacketCommandArgs(cmd, sClientName, bisFromClient, bosToClient);
        try {
            return getKSimCommandDispatcher().dispatch(args);
        } catch (Exception e) {
            return failurePacket(e.getMessage());
        }
    }


    private Packet doExitProcess(String sSessId, String sPid) {
        // Delete the process from the mappings pid <-> sessid.
        if (!processToSession.containsKey(sPid)) {
            return failurePacket(String.format(PROCESS_S_IS_NOT_MAPPED_TO_A_SESSION_FMTMSG, sPid));
        }

        if (!sessionToProcesses.containsKey(sSessId)) {
            return failurePacket(String.format(SESSION_S_IS_NOT_MAPPED_FMTMSG, sSessId));
        }
        HashSet hs = (HashSet) sessionToProcesses.get(sSessId);
        if (!hs.contains(sPid)) {
            return failurePacket(String.format(SESSION_S_IS_NOT_MAPPED_TO_PROCESS_S_FMTMSG, sSessId, sPid));
        }

        processToSession.remove(sPid);
        hs.remove(sPid);
        return successPacket(null);
    }

    private Packet doCreateProcess(String sSessId) {
        /*RandomGUID myGUID = new RandomGUID();
        String sProcessId = myGUID.toStringNoDashes();
        */
        String sProcessId = String.valueOf((int)(100000000 + Math.random() * 900000000));

        // Store the mappings: pid -> sessid and sessid -> pid.
        mapPidToSessid(sProcessId, sSessId);
        mapSessidToPid(sSessId, sProcessId);

        try {
            Packet result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, sProcessId);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, DO_CREATE_PROCESS, e.getMessage()));
        }
    }

    private Packet doMaySessionClose(String sSessId) {
        // If the session has any process running respond "failed".
        if (!sessionToProcesses.containsKey(sSessId)) {
            return successPacket(null);
        }
       
        HashSet hs = (HashSet) sessionToProcesses.get(sSessId);
       if(hs.isEmpty())
       {
    	   return successPacket(null);
       }
       
        /*if (hs.isEmpty()) {
            Map<File, InputStream> openFileStreams = _sessionFileStreams.get(sSessId);
            if (openFileStreams != null) {
                for (InputStream is : openFileStreams.values()) {
                    if (is == null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                }
            }
            return successPacket(null);
        } else {
            if (this.debugFlag) {
                StringBuilder sb = new StringBuilder(String.format("Closing session %s not allowed, processes still running: ", sSessId));
                Joiner.on("\n").appendTo(sb, hs);
                JOptionPane.showMessageDialog(null, sb.toString(), "Error in doMaySessionClose", JOptionPane.ERROR_MESSAGE);
                System.out.println(sb.toString());
            }
       // }*/
        return failurePacket(FAILURE_PACKET_PAYLOAD);
    }

    private boolean mapPidToSessid(String sPid, String sSessid) {
        // pid cannot be in processToSession
        if (processToSession.containsKey(sPid)) {
            return false;
        }
        processToSession.put(sPid, sSessid);
        return true;
    }

    private boolean mapSessidToPid(String sSessid, String sPid) {
        // Two cases: the session is already in, or the session is not.
        if (sessionToProcesses.containsKey(sSessid)) {
            HashSet hs = (HashSet) sessionToProcesses.get(sSessid);
            // The process must not be in the hashset.
            if (hs.contains(sPid)) {
                return false;
            }
            // Add the process to the session's list of processes.
            hs.add(sPid);
            return true;
        } else {
            // Add a new entry for the session.
            Set<String> hs = new HashSet();
            sessionToProcesses.put(sSessid, hs);
            hs.add(sPid);
            return true;
        }
    }

    /*
     * private Object doGetFileBytes(String sSessId, String sFileProp,
     * BufferedInputStream bisFromClient, BufferedOutputStream bosToClient) {
     * String sPath = "E:\\PmNew\\PmKsim\\alice.jpg"; if (sPath == null) return
     * failurePacket("No file with property: " + sFileProp + "!");
     *
     * File f = new File(sPath); if (!f.exists() || !f.isFile() || !f.canRead())
     * { return failurePacket("Something wrong with file " + sPath + "!"); } try
     * { FileInputStream fis = new FileInputStream(f); BufferedInputStream bis =
     * new BufferedInputStream(fis); PacketManager.sendPacket(bis,
     * (int)f.length(), bosToClient); } catch (Exception e) {
     * e.printStackTrace(); return
     * failurePacket("Exception when creating streams for the requested file!");
     * } return dnrPacket(); }
     */
    private Packet doGetFileContent(Packet cmd, OutputStream bosToClient) {

        try {
            engineClient.download(cmd, bosToClient);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(COMMAND_FORWARDING_ERROR_MSG);
        }
        return dnrPacket();
    }


    //
    // Open an object.
    // Issue an engine command getPermittedOps with the session id,
    // process id, and the
    // object name as arguments. If the engine returns an error,
    // return it to the caller (probably the SysCaller). Otherwise,
    // store the result (the object properties and the permitted ops)
    // in the kernel's OpenObjectRecord, generate a handle as the
    // key to this record, and return the handle in a packet.
    //
    // openObject3() currently doesn't use the sReqOps argument,
    // which contains the requested operations.
    //
    private Packet doOpenObject3(String sSessId, String sProcId,
                                 String sObjName, String sReqOps) {
        // Ask the engine for permissions on the specified object.
        // The result will contain:
        // Item 0: <name>|<id>|<class>|<inh>|<host or orig name>|<path or orig
        // id>
        // Item 1: <permission>
        // Item 2: <permission>
        // ...
        Packet result = (Packet) getPermittedOps(sSessId, sProcId, sObjName);
        if (result.hasError()) {
            return result;
        }

        // Generate a handle and create an entry in the openHandles.
        RandomGUID myGUID = new RandomGUID();
        String sHandle = myGUID.toStringNoDashes();
        OpenObjectRecord oor = new OpenObjectRecord(sObjName);

        String s = result.getStringValue(0);
        String[] pieces = s.split(PM_ALT_DELIM_PATTERN);
        String sClass = pieces[2];
        String sNameOrHost = pieces[4];
        String sIdOrPath = pieces[5];
        oor.setHost(sNameOrHost);
        oor.setPath(sIdOrPath);
        oor.setPerms(packetToSet(result, 1));
        openHandles.put(sHandle, oor);

        // Add the object to the engine's list of open objects for the current
        // session.
        try {
            Packet cmd = makeCmd(ADD_OPEN_OBJ, sSessId, sObjName);
            result = engineClient.sendReceive(cmd, null);
            if (result.hasError()) {
                return result;
            }

            // Return the handle.
            result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, sHandle);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, ADD_OPEN_OBJ, e.getMessage()));
        }
    }


    private Packet doReadObject3(Packet cmd, OutputStream bosToClient) {
        if (cmd.size() <= 3) {
            return failurePacket(TOO_FEW_ARGUMENTS_ERROR_MSG);
        }
        String sSessId = getArgument(cmd, 1);
        String sProcId = getArgument(cmd, 2);
        String sHandle = getArgument(cmd, 3);


        OpenObjectRecord oor = openHandles.get(sHandle);
        if (oor == null) {
            return failurePacket(INVALID_OBJECT_HANDLE_ERROR_MSG);
        }

        // Get the permitted operations for the specified session and object.
        String sObjName = oor.getName();
        Packet res = getPermittedOps(sSessId, sProcId, sObjName);
        if (res.hasError()) {
            return res;
        }

        HashSet retOps = packetToSet(res, 1);
        if (!retOps.contains(PERMISSION_FILE_READ)) {
            return failurePacket(READ_PERMISSION_VIOLATION_ERROR_MSG);
        }

        String sHost = oor.getHost();
        String sPath = oor.getPath();

        // Local file?
        if (oor.isLocal()) {
             //return doLocalReadFile(sSessId, sHost, sPath, bosToClient);
        	if(sPath == null || sPath.length() == 0)
        		return failurePacket(NULL_OR_EMPTY_FILE_PATH_ERROR_MSG);
        	File file = new File(sPath);
        	if(!file.exists()) 
        		return failurePacket(String.format(FILE_S_NOT_ON_HOST_S_ERR_FMTMSG, 
        			sPath, sHost));
        	if(!file.isFile()) 
        		return failurePacket(String.format(S_IS_NOT_A_FILE_ON_HOST_S_ERROR_FMTMSG,
        			sPath, sHost));
        	if(!file.canRead()) 
        		return failurePacket(String.format(CANNOT_READ_THE_FILE_S_ON_HOST_S_ERROR_FMTMSG, 
        			sPath, sHost));
        	try{
        		FileInputStream fis = new FileInputStream(file);
        		BufferedInputStream bis = new BufferedInputStream(fis);
        		PacketManager.sendPacket(bis, (int)file.length(), bosToClient);
        	}
        	catch(Exception e){
        		e.printStackTrace();
        		return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, READ_NEW_FILE ,e.getMessage()));
        	}

        } else {
            // A file on a remote host.
            // First establish a communication channel to the remote host.
            SSLSocketClient mySslClient = null;
            int port = PM_DEFAULT_SIMULATOR_PORT;
            try {
                mySslClient = new SSLSocketClient(sHost,
                        port, debugFlag, KERNEL_TO_KERNEL_LOG_PREFIX);

                // Make a command to extract the remote file content.
                cmd = makeCmd(NEW_READ_FILE, sSessId, sHost, sPath);
                mySslClient.download(cmd, bosToClient);
            } catch (Exception e) {
                e.printStackTrace();
                return failurePacket(String.format(SIMULATOR_CHECK_S_S_D_ERROR_FMTMSG, sHost, sHost, port));
            }
            
        }

        // Generate the event "Object read"...
        generateEvent(sSessId, sProcId, PERMISSION_OBJECT_READ, oor.getName(),
                oor.getId(), CLASS_FILE_NAME);

        // Return a "do not respond" packet to the server thread, so that no
        // response
        // is sent to the client - we already sent the bytes.
        return dnrPacket();
    }

    private Packet doGetDevices(String sSessId) {
        File[] roots = File.listRoots();
        try {
            Packet result = new Packet();
            if (roots == null) {
                return result;
            }
            for (int i = 0; i < roots.length; i++) {
                File[] cont = roots[i].listFiles();
                if (cont == null) {
                    continue;
                }
                result.addItem(ItemType.RESPONSE_TEXT,
                        roots[i].getAbsolutePath());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, GET_DEVICES, e.getMessage()));
        }
    }

    private Packet passCmdToEngine(Packet cmd) {
        try {
            return engineClient.sendReceive(cmd, null);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, PASS_CMD_TO_ENGINE, cmd.getStringValue(0)));
        }
    }

    private Packet doCopyObject(String sSessId, String sProcId,
                                String sObjName, Packet cmd) {
        // Ask the engine to make a copy of the object. The result contains the
        // properties of the copy:
        // <name>|<id>|<class>|<inh>|<host or orig name>|<path or orig id>
        Packet dstProps = passCmdToEngine(cmd);
        if (dstProps.hasError()) {
            return dstProps;
        }

        // Now get the properties of the source object.
        Packet srcProps = null;
        try {
            Packet newcmd = makeCmd(GET_OBJ_INFO, sSessId, sObjName);
            srcProps = engineClient.sendReceive(newcmd, null);
            if (srcProps.hasError()) {
                return srcProps;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, GET_OBJ_INFO, e.getMessage()));
        }

        String s = srcProps.getStringValue(0);
        String[] pieces = s.split(PM_ALT_DELIM_PATTERN);
        String sSrcHost = pieces[4];
        String sSrcPath = pieces[5];

        s = dstProps.getStringValue(0);
        pieces = s.split(PM_ALT_DELIM_PATTERN);
        String sDstName = pieces[0];
        String sDstId = pieces[1];
        String sDstHost = pieces[4];
        String sDstPath = pieces[5];

        try {
            if (isLocalHost(sSrcHost)) {
                if (isLocalHost(sDstHost)) {
                    // Case 1. Both source and dest files are local.
                    File fSrc = new File(sSrcPath);
                    if (!fSrc.exists() || !fSrc.isFile() || !fSrc.canRead()) {
                        return failurePacket(String.format(SOURCE_FILE_S_ERROR_MSG, sSrcPath));
                    }

                    FileInputStream fis = new FileInputStream(fSrc);
                    BufferedInputStream bis = new BufferedInputStream(fis);

                    FileOutputStream fos = new FileOutputStream(sDstPath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    PacketManager.copyBytes(bis, (int) fSrc.length(), bos);
                } else {
                    // Case 2. Local source, remote destination.
                    return failurePacket(NOT_IMPLEMENTED_ERROR_MSG);


                }
            } else {
                // The source is remote. Establish a communication channel to
                // it.
                SSLSocketClient mySrcClient = null;
                mySrcClient = new SSLSocketClient(sSrcHost,
                        PM_DEFAULT_SIMULATOR_PORT, debugFlag, KERNEL_TO_KERNEL_LOG_PREFIX);
                if (isLocalHost(sDstHost)) {
                    // Case 3. Source is remote, dest is local.
                    FileOutputStream fos = new FileOutputStream(sDstPath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    // Make a command to extract the remote file content.
                    cmd = makeCmd(NEW_READ_FILE, sSessId, sSrcHost, sSrcPath);
                    Packet res = mySrcClient.sendReceive(cmd, bos);
                    if (res.hasError()) {
                        return res;
                    }
                } else {
                    return failurePacket(NOT_IMPLEMENTED_ERROR_MSG);
                }
            }

            // OK. Return the name of the copy.
            Packet res = new Packet();
            res.addItem(ItemType.RESPONSE_TEXT, sDstName);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, COPY_OBJECT, e.getMessage()));
        }
    }

    private Packet doCreateObject3(String sSessId, String sProcId,
                                   String sObjName, String sObjClass, String sObjType,
                                   String sContainers, String sReqPerms, Packet cmd) {
        System.out.printf("Kernel's doCreateObject3{%s, %s, %s, %s, %s, %s, \"%s\", %s)\n", sSessId, sProcId, sObjName, sObjClass, sObjType, sContainers, sReqPerms, cmd);


        // Ask the engine to create the new object. If creation is successful,
        // the engine will generate an event and process it.
        Packet result = passCmdToEngine(cmd);
        if (result.hasError()) {
            return result;
        }

        // Ask the engine for permissions on and
        // properties of the specified object. The result will contain:
        // Item 0: <name>|<id>|<class>|<inh>|<host or orig name>|<path or orig
        // id>
        // Item 1: <permission>
        // Item 2: <permission>
        // ...
        result = (Packet) getPermittedOps(sSessId, sProcId, sObjName);
        if (result.hasError()) {
            return result;
        }

        // Generate a handle and create an entry in the openHandles.
        RandomGUID myGUID = new RandomGUID();
        String sHandle = myGUID.toStringNoDashes();
        OpenObjectRecord oor = new OpenObjectRecord(sObjName);

        String s = result.getStringValue(0);
        String[] pieces = s.split(PM_ALT_DELIM_PATTERN);
        String sClass = pieces[2];
        String sNameOrHost = pieces[4];
        String sIdOrPath = pieces[5];
        oor.setHost(sNameOrHost);
        oor.setPath(sIdOrPath);
        oor.setPerms(packetToSet(result, 1));
        openHandles.put(sHandle, oor);

        // Add the object to the engine's list of open objects for the current
        // session.
        try {
            cmd = makeCmd(ADD_OPEN_OBJ, sSessId, sObjName);
            result = engineClient.sendReceive(cmd, null);
            if (result.hasError()) {
                return result;
            }

            // Return the handle.
            result = new Packet();
            result.addItem(ItemType.RESPONSE_TEXT, sHandle);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, ADD_OPEN_OBJ, e.getMessage()));
        }
    }


    // Sends the command with the current session, a null user id, the current process id,
    // and the object name. The engine will retrieve the user id from the current session.
    private Packet getPermittedOps(String sSessId, String sProcId,
                                   String sObjName) {
        try {
            Packet cmd = makeCmd(GET_PERMITTED_OPS, sSessId, null, sProcId, sObjName);
            Packet res = engineClient.sendReceive(cmd, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, GET_PERMITTED_OPS, e.getMessage()));
        }
    }


    // The "paste from clipboard" action is seen as a read from
    // the clipboard object, and is preceded by a call to the
    // engine to get the permitted operations on the clipboard
    // object. If the returned operations do not contain "File read",
    // the process cannot read the clipboard.
    //
    // Note that each PM host has its own clipboard object, whose
    // name is "<host name> clipboard", e.g., "pmengine clipboard".
    //
    private Packet isPastingAllowed(String sSessId, String sProcId) {
        String sClipName = currentHost + CLIPBOARD_SUFFIX;

        // Ask the engine for permitted operations on the clipboard object.
        Packet res = (Packet) getPermittedOps(sSessId, sProcId, sClipName);
        if (res.hasError()) {
            return res;
        }

        HashSet retPerms = packetToSet(res, 1);
        if (!retPerms.contains(PERMISSION_FILE_READ)) {
            return failurePacket(CLIPBOARD_PASTE_PERMISSION_VIOLATION_MSG);
        }

        generateEvent(sSessId, sProcId, PERMISSION_OBJECT_READ, sClipName, null,
                CLASS_FILE_NAME);
        return successPacket(null);
    }

    // If the handle is null, we copy from something which is not yet an object.
    private Packet doCopyToClipboard(String sSessId, String sHandle,
                                     String sSelText) {
        String sObjName = null;
        if (sHandle != null) {
            OpenObjectRecord oor = (OpenObjectRecord) openHandles.get(sHandle);
            if (oor == null) {
                return failurePacket(INVALID_OBJECT_HANDLE_ERROR_MSG);
            }
            Set<String> setPerms = oor.getPerms();
            sObjName = oor.getName();

            // if (!setPerms.contains("File read")) return
            // failure("You don't have permission to read from this object!");
            System.out.printf("CopyToClipboard from %s to %s\n", sObjName, sSelText);

        }

        // Tell the engine to build a temporary object for the
        // clipboard of the current host, with all the original object's
        // assigned
        // attributes and operation sets, except the MLS attribute, if any,
        // which is to be replaced by the crt session's MLS attribute.
        Packet res = null;
        try {
            Packet cmd = makeCmd(BUILD_CLIPBOARD, sSessId,
                    (sObjName == null) ? "" : sObjName);
            res = engineClient.sendReceive(cmd, null);
            if (res.hasError()) {
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, BUILD_CLIPBOARD, e.getMessage()));
        }
        // If the selected text is not null, insert it into the system
        // clipboard.
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection content = new StringSelection(sSelText);
        clipboard.setContents(content, null);
        return res;
    }

    // Close an open object handle. Returns error or success message.
    private Packet doCloseObject(String sSessId, String sHandle) {
        if (!openHandles.containsKey(sHandle)) {
            return failurePacket(INVALID_OBJECT_HANDLE_ERROR_MSG);
        }

        OpenObjectRecord oor = (OpenObjectRecord) openHandles.get(sHandle);
        if (oor == null) {
            return failurePacket(INVALID_OBJECT_HANDLE_ERROR_MSG);
        }

        String sObjName = oor.getName();
        openHandles.remove(sHandle);
        
       Packet res = null;
        try {
            Packet cmd = makeCmd(DELETE_OPEN_OBJ, sSessId, sObjName);
            res = engineClient.sendReceive(cmd, null);
            if (res.hasError()) {
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, DELETE_OPEN_OBJ, e.getMessage()));
        }

        return successPacket(null);
    }

    /**
	 * @uml.property  name="_sessionFileStreams"
	 * @uml.associationEnd  multiplicity="(0 -1)" ordering="true" elementType="java.io.FileInputStream" qualifier="sSessId:java.lang.String java.io.InputStream"
	 */
   // private Map<String, Map<File, InputStream>> _sessionFileStreams = Collections.synchronizedMap(new HashMap<String, Map<File, InputStream>>());
    // Usually this command is issued by another KSim server.

    private Packet doNewReadFile(String sSessId, String sHost,
                                   String sFilePath, OutputStream bosToClient) {
        if (!isLocalHost(sHost)) {
            return failurePacket(NOT_THIS_OBJECTS_HOST_ERROR_MSG);
        }
        if (sFilePath == null || sFilePath.length() == 0) {
            return failurePacket(NULL_OR_EMPTY_FILE_PATH_ERROR_MSG);
        }

        File f = new File(sFilePath);
        if (!f.exists()) {
            return failurePacket(String.format(FILE_S_NOT_ON_HOST_S_ERR_FMTMSG, sFilePath, sHost));
        }
        if (!f.isFile()) {
            return failurePacket(String.format(S_IS_NOT_A_FILE_ON_HOST_S_ERROR_FMTMSG, sFilePath, sHost));
        }
        if (!f.canRead()) {
            return failurePacket(String.format(CANNOT_READ_THE_FILE_S_ON_HOST_S_ERROR_FMTMSG, sFilePath, sHost));
        }

        try {
        	FileInputStream fis = new FileInputStream(f);
        	BufferedInputStream bis = new BufferedInputStream(fis);
        	PacketManager.sendPacket(bis, (int)f.length(), bosToClient);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, READ_NEW_FILE, e.getMessage()));
        }
        return (Packet) dnrPacket();
    }


    // The communication protocol here is strange. The client sends
    // two commands. The first one
    // "newWriteobject" is a warning. The second one carries the binary data.
    // The server must read the second command itself.
    private Packet doWriteObject3(Packet cmd, InputStream bisFromClient) {
        if (cmd.size() <= 3) {
            return failurePacket(TOO_FEW_ARGUMENTS_ERROR_MSG);
        }
        System.out.println(" ******************************* in doWriteObject3 ********************************* ");
        String sSessId = getArgument(cmd, 1);
        String sProcId = getArgument(cmd, 2);
        String sHandle = getArgument(cmd, 3);

        OpenObjectRecord oor = (OpenObjectRecord) openHandles.get(sHandle);
        if (oor == null) {
            return failurePacket(INVALID_OBJECT_HANDLE_ERROR_MSG);
        }

        // Get the permitted operations for the specified session and object.
        String sObjName = oor.getName();
        Packet res = getPermittedOps(sSessId, sProcId, sObjName);
        if (res.hasError()) {
            return res;
        }

        HashSet retOps = packetToSet(res, 1);
        if (!retOps.contains(PERMISSION_FILE_WRITE)) {
            // Consume the second packet containing the object data and return
            // failure.
            try {
                PacketManager.discardReceivedPacket(bisFromClient);
            } catch (Exception e) {
                e.printStackTrace();
                return failurePacket(DISCARD_OBJECT_DATA_ERROR_MSG);
            }
            return failurePacket(WRITE_PERMISSION_VIOLATION_ERROR_MSG);
        }

        String sHost = oor.getHost();
        String sPath = oor.getPath();

        // Local file?
        if (isLocalHost(sHost)) {
            try {
            	FileOutputStream fos = new FileOutputStream(sPath);
            	Packet secondCmd = PacketManager.receivePacket(bisFromClient, fos);
            	fos.close();
            } catch (Exception e) {
            	e.printStackTrace();
            	return failurePacket("Exception when writing the object to the local host!");
            }
        } else {
            // A file on a remote host.
            // First establish a communication channel to the remote host.
            SSLSocketClient mySslClient = null;
            try {
                mySslClient = new SSLSocketClient(sHost,
                        PM_DEFAULT_SIMULATOR_PORT, debugFlag, KERNEL_TO_KERNEL_LOG_PREFIX);

                // Make a command to write the remote file content.
                cmd = makeCmd(NEW_WRITE_FILE, sSessId, sHost, sPath);
                mySslClient.upload(cmd, bisFromClient);
            } catch (Exception e) {
                e.printStackTrace();
                return failurePacket(String.format(SIMULATOR_CHECK_S_S_D_ERROR_FMTMSG, sHost, sHost, PM_DEFAULT_SIMULATOR_PORT));
            }
        }

        // Generate the event "Object write".
        generateEvent(sSessId, sProcId, PERMISSION_OBJECT_WRITE, oor.getName(),
                oor.getId(), CLASS_FILE_NAME);

        // Return a "do not respond" packet to the server thread, so that no
        // response
        // is sent to the client.
        return (Packet) successPacket(OK_PACKET_PAYLOAD);
    }

    /**
	 * @uml.property  name="retainedFileOutputStreams"
	 */
    Map<File, OutputStream> retainedFileOutputStreams = Collections.synchronizedMap(new HashMap<File, OutputStream>());

    // Usually this command is issued by another KSim server.
    private Packet doLocalWriteFile(String sSessId, String sHost, String sPath,
                                    InputStream bisFromClient) {
        if (!isLocalHost(sHost)) {
            return failurePacket(String.format(S_S_IS_NOT_THE_LOCAL_HOST_ERROR_FMTMSG, "doLocalWriteFile", sHost));
        }
        if (sPath == null || sPath.length() == 0) {
            return failurePacket(NULL_OR_EMPTY_FILE_PATH_ERROR_MSG);
        }
        try {
            FileOutputStream fos = new FileOutputStream(sPath);
            System.out.println("We're in doLocalWriteFile before receivePacket");
            Packet secondCmd = PacketManager.receivePacket(bisFromClient, fos);
            System.out.println("We're in doLocalWriteFile after receivePacket");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, "doLocalWriteFile", e.getMessage()));
        }
        return successPacket(OK_PACKET_PAYLOAD);
    }

    private Packet generateEvent(String sSessId, String sProcId,
                                 String sEventName, String sObjName, String sObjId, String sObjClass) {
        System.out.println(" *********************************** in generateEvent() sEventName - " + sEventName + " sObjName -" + sObjName);
    	try {
            Packet cmd = makeCmd(PROCESS_EVENT, sSessId,
                    (sProcId == null) ? "" : sProcId, sEventName, sObjName,
                    (sObjId == null) ? "" : sObjId, sObjClass);
            Packet res = engineClient.sendReceive(cmd, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(String.format(EXCEPTION_IN_S_S_ERROR_FMTMSG, PROCESS_EVENT, e.getMessage()));
        }
    }


    // Parameter n is a byte with a value between 0 and 15 - representing the
    // decimal value of a hex digit.
    // The method returns a byte that is the ascii character representing
    // the hex digit. For example, if n = 7, the method returns 0x37, i.e. '7'.
    // If n = 12, the method returns 'C'.
    byte byte2HexDigit(byte n) {
        if (n < 10) {
            return (byte) ('0' + n);
        } else {
            return (byte) ('A' + n - 10);
        }
    }

    String byteArray2HexString(byte[] inp, int length) {
        byte[] buf = new byte[2 * length];
        int inpix, outix;
        int n;
        byte q, r;

        for (inpix = outix = 0; inpix < length; inpix++) {
            n = inp[inpix] & 0x000000FF;
            q = (byte) (n / 16);
            r = (byte) (n % 16);
            buf[outix++] = byte2HexDigit(q);
            buf[outix++] = byte2HexDigit(r);
        }
        return new String(buf);
    }

    private static byte hexDigit2Byte(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        } else if (c >= 'a' && c <= 'f') {
            return (byte) (c - 'a' + 10);
        } else if (c >= 'A' && c <= 'F') {
            return (byte) (c - 'A' + 10);
        } else {
            return (byte) 0;
        }
    }

    // Return value: negative = error; positive = number of output bytes in the
    // out buffer.
    public static int hexString2ByteArray(String inp, byte[] out, int maxlen) {
        int len;
        int inpix, outix;

        if (inp == null || inp.length() == 0 || inp.length() % 2 != 0) {
            return -1;
        }
        if (inp.length() / 2 > maxlen) {
            return -2;
        }

        for (inpix = 0, outix = 0; inpix < inp.length(); ) {
            int msn = hexDigit2Byte(inp.charAt(inpix++));
            int lsn = hexDigit2Byte(inp.charAt(inpix++));

            out[outix++] = (byte) ((msn << 4) | lsn);
        }
        return (inp.length() / 2);
    }



    private Packet failurePacket(String s) {
        try {
            Packet res = new Packet();
            if (s == null || s.length() == 0) {
                res.addItem(ItemType.RESPONSE_ERROR, UNSPECIFIED_ERROR_MSG);
            } else {
                res.addItem(ItemType.RESPONSE_ERROR, s);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Packet successPacket(String s) {
        try {
            Packet res = new Packet();
            if (s == null || s.length() == 0) {
                res.addItem(ItemType.RESPONSE_SUCCESS, SUCCESS_MSG);
            } else {
                res.addItem(ItemType.RESPONSE_SUCCESS, s);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Packet dnrPacket() {
        Packet p = new Packet();
        try {
            p.addItem(ItemType.RESPONSE_DNR, DNR_EMPTY_PAYLOAD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return p;
    }

    public static boolean isEod(String sLine) {
        if (sLine == null) {
            return false;
        }
        return sLine.startsWith(CODE_EOD);
    }

    // Find and return where to insert a new string in an alphabetically ordered
    // list.
    public static int getIndex(DefaultListModel model, String target) {
        int high = model.size(), low = -1, probe;
        while (high - low > 1) {
            probe = (high + low) / 2;
            if (target.compareToIgnoreCase((String) model.get(probe)) < 0) {
                high = probe;
            } else {
                low = probe;
            }
        }
        return (low + 1);
    }

    public String getArgument(Packet p, int n) {
        if (p == null) {
            return null;
        }
        if (n < 0 || n >= p.size()) {
            return null;
        }
        String s = p.getStringValue(n);
        if (s == null || s.length() == 0) {
            return null;
        }
        return s;
    }
    public static final String PM_APPLICATION_NAME = "Kernel Simulator";
    public static void main(String[] args) {
        String enghost = null;
        int engport = 0;
        int simport = 0;
        boolean debug = false;

        // Process possible arguments.
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(DEBUG_ARG)) {
                debug = true;
            } else if (args[i].equals(ENGINE_HOST_ARG)) {
                enghost = args[++i];
            } else if (args[i].equals(ENGINE_PORT_ARG)) {
                engport = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals(SIM_PORT_ARG)) {
                simport = Integer.valueOf(args[++i]).intValue();
            }
        }

        if (enghost == null) {
            enghost = PM_DEFAULT_ENGINE_HOST;
        }
        if (engport < 1024) {
            engport = PM_DEFAULT_SERVER_PORT;
        }
        if (simport < 1024) {
            simport = PM_DEFAULT_SIMULATOR_PORT;
        }

        DialogUtils.getAllSystemProperties(PM_APPLICATION_NAME);
        KernelSimulator simulator = new KernelSimulator(enghost, engport,
                simport, debug);
        System.out.println("KernelSimulator for the Policy Machine started...");
        System.out.println("...with Engine host " + enghost + "...");
        System.out.println("...Engine port " + engport + "...");
        System.out.println("...and K-sim port " + simport + "...");
        System.out.printf("...Debugging is %s.\n", debug ? "enabled" : "disabled");
    }

    private static HashSet packetToSet(Packet p) {
        return packetToSet(p, 0);
    }

    private static HashSet packetToSet(Packet p, int offset) {
        HashSet set = new HashSet();
        if (p == null) {
            return set;
        }
        for (int i = offset; i < p.size(); i++) {
            set.add(p.getStringValue(i));
        }
        return set;
    }

    // The method assumes there is no EOD item at the end of the array.
    private static HashSet arrayListToSet(ArrayList al, int off) {
        HashSet set = new HashSet();
        if (al == null) {
            return set;
        }
        for (int i = off; i < al.size(); i++) {
            set.add(((String) al.get(i)).substring(4));
        }
        return set;
    }

    private static HashSet stringToSet(String sArg) {
        HashSet set = new HashSet();
        if (sArg != null) {
            String[] pieces = sArg.split(",");
            for (int i = 0; i < pieces.length; i++) {
                String t = pieces[i].trim();
                if (t.length() > 0) {
                    set.add(t);
                }
            }
        }
        return set;
    }

    // Returns an ArrayList:
    // DATA<set member>
    // DATA<set member>
    // ...
    // Note that the method does not add the EOD (End-Of-Data) item.
    private static ArrayList setToArrayList(HashSet set) {
        ArrayList al = new ArrayList();
        if (set != null && !set.isEmpty()) {
            Iterator hsiter = set.iterator();
            while (hsiter.hasNext()) {
                al.add(CODE_DATA + (String) hsiter.next());
            }
        }
        return al;
    }

    private static String setToString(HashSet set) {
        if (set == null || set.isEmpty()) {
            return "";
        }

        Iterator hsiter = set.iterator();
        boolean firstTime = true;
        StringBuilder sb = new StringBuilder();

        while (hsiter.hasNext()) {
            String sId = (String) hsiter.next();
            if (firstTime) {
                sb.append(sId);
                firstTime = false;
            } else {
                sb.append(",").append(sId);
            }
        }
        return sb.toString();
    }

    private void printSet(HashSet hs, String caption) {
        if (caption != null && caption.length() > 0) {
            System.out.println(caption);
        }
        Iterator hsiter = hs.iterator();

        System.out.print("{");
        boolean firstTime = true;
        while (hsiter.hasNext()) {
            String sId = (String) hsiter.next();
            if (firstTime) {
                System.out.print(sId);
                firstTime = false;
            } else {
                System.out.print(", " + sId);
            }
        }
        System.out.println("}");
    }


    /**
	 * @author  Administrator
	 */
    private abstract class PacketCommand implements Command<PacketCommandArgs, Packet> {
        /**
		 * @uml.property  name="_command"
		 * @uml.associationEnd  
		 */
        private final PMCommand _command;
        private final int _expectedArguments;
        private String _clientName;
        private InputStream _clientInputStream;
        private OutputStream _clientOutputStream;
        private List<String> args = new ArrayList<String>();

        public PacketCommand(PMCommand command, int expectedArguments) {
            _command = command;
            _expectedArguments = expectedArguments;
        }

        public PacketCommand(PMCommand command) {
            this(command, -1);
        }

        public InputStream getClientInputStream() {
            return _clientInputStream;
        }

        public OutputStream getClientOutputStream() {
            return _clientOutputStream;
        }

        @Override
        public String commandKey() {
            return _command.commandCode();
        }

        public String getClientName() {
            return _clientName;
        }

        private void fillCommonArgumentPositions(Packet p) {
            args.clear();
            for (int i = 0; i < p.size(); ++i) {
                args.add(getArgument(p, i));
            }
        }


        protected String commandName() {
            return elementOrNull(args, 0);
        }

        protected String sessionId() {
            return elementOrNull(args, 1);
        }

        protected String handle() {
            return elementOrNull(args, 2);
        }

        protected String host() {
            return elementOrNull(args, 2);
        }

        protected String procId() {
            return elementOrNull(args, 2);
        }

        protected String path() {
            return elementOrNull(args, 3);
        }

        protected int offset() {
            return Integer.parseInt(elementOr(args, 4, "0"));
        }

        protected int limit() {
            return Integer.parseInt(elementOr(args, 5, "-1"));
        }


        protected String selectedText() {
            return elementOrNull(args, 3);
        }

        protected String objectName() {
            return elementOrNull(args, 3);
        }

        protected String objectClass() {
            return elementOrNull(args, 4);
        }

        protected String objectType() {
            return elementOrNull(args, 5);
        }

        protected String containers() {
            return elementOrNull(args, 6);
        }

        protected String permissions() {
            return elementOrNull(args, 7);
        }

        private <T> T elementOr(List<T> list, int pos, T alternate) {
            if (list == null || pos >= list.size()) {
                return alternate;
            } else {
                return list.get(pos);
            }
        }

        private <T> T elementOrNull(List<T> list, int pos) {
            return elementOr(list, pos, null);
        }


        @Override
        public Packet execute(PacketCommandArgs input) {
            if (input.getPacket().size() < _expectedArguments) {
                return failurePacket(TOO_FEW_ARGUMENTS_ERROR_MSG);
            }
            _clientInputStream = input.getInputStreamFromClient();
            _clientOutputStream = input.getOutputStreamToClient();
            _clientName = input.getClientName();
            fillCommonArgumentPositions(input.getPacket());
            return execute(input.getPacket());
        }

        public abstract Packet execute(Packet input);


    }

    /**
	 * @author  Administrator
	 */
    static class PacketCommandArgs {

        /**
		 * @uml.property  name="_packet"
		 * @uml.associationEnd  
		 */
        private final Packet _packet;
        private final InputStream _inputStream;
        private final OutputStream _outputStream;
        private final String _clientName;

        PacketCommandArgs(Packet p, String clientName) {
            this(p, clientName, null, null);
        }

        PacketCommandArgs(Packet p, String clientName, OutputStream os) {
            this(p, clientName, null, os);
        }

        PacketCommandArgs(Packet p, String clientName, InputStream is) {
            this(p, clientName, is, null);
        }

        PacketCommandArgs(Packet p, String clientName, InputStream is, OutputStream os) {
            _packet = p;
            _clientName = clientName;
            _inputStream = is;
            _outputStream = os;
        }

        String getClientName() {
            return _clientName;
        }

        Packet getPacket() {
            return _packet;
        }

        InputStream getInputStreamFromClient() {
            return _inputStream;
        }

        OutputStream getOutputStreamToClient() {
            return _outputStream;
        }
    }

    /**
	 * @uml.property  name="readObject"
	 * @uml.associationEnd  
	 */
    Command<PacketCommandArgs, Packet> readObject = new PacketCommand(READ_OBJECT_3) {
        @Override
        public Packet execute(Packet input) {
            return (Packet) doReadObject3(input, getClientOutputStream());
        }
    };


    class PassToEngineCommand extends PacketCommand {
        public PassToEngineCommand(PMCommand cmdName) {
            super(cmdName);
        }

        public Packet execute(Packet input) {
            return passCmdToEngine(input);
        }
    }


    /**
	 * @uml.property  name="ksimCommandLookup"
	 * @uml.associationEnd  
	 */
    CommandLookup<PacketCommandArgs> ksimCommandLookup = new CommandLookup<PacketCommandArgs>() {

        @Override
        public Object commandKey(Command<PacketCommandArgs, ?> cmd) {
            return cmd.commandKey();
        }

        @Override
        public Object commandKey(PacketCommandArgs obj) {
            Packet p = obj.getPacket();
            String cmd = p != null ? p.getStringValue(0) : null;
            return cmd;
        }
    };

    /**
	 * @uml.property  name="ksimCommandDispatcher"
	 * @uml.associationEnd  
	 */
    Dispatcher<PacketCommandArgs, Packet> ksimCommandDispatcher = null;


    // To do: ??? Use the original id of the virtual object as key.
    /**
	 * @author  Administrator
	 */
    class OpenObjectRecord {

        /**
		 * @uml.property  name="perms"
		 */
        Set<String> perms; // The permissions returned by the engine.
        String sId; // The virtual object original id.
        String sName; // The virtual object name/label.
        String sHost;
        String sPath;

        public OpenObjectRecord(String sName) {
            this.sName = sName;
        }

        public void setId(String sId) {
            this.sId = sId;
        }

        public void setName(String sName) {
            this.sName = sName;
        }

        public void setPerms(String sPerms) {
            perms = stringToSet(sPerms);
        }

        /**
		 * @param perms
		 * @uml.property  name="perms"
		 */
        public void setPerms(Set<String> perms) {
            this.perms = perms;
        }

        public void setHost(String sHost) {
            this.sHost = sHost;
        }

        public void setPath(String sPath) {
            this.sPath = sPath;
        }


        public boolean isLocal() {
            return isLocalHost(sHost);
        }

        public String getId() {
            return sId;
        }

        public String getName() {
            return sName;
        }

        /**
		 * @return
		 * @uml.property  name="perms"
		 */
        public Set<String> getPerms() {
            return perms;
        }

        public String getHost() {
            return sHost;
        }

        public String getPath() {
            return sPath;
        }
    }
}

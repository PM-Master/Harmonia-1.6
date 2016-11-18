package gov.nist.csd.pm.common.util;

import com.google.common.base.Throwables;

import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;

import javax.swing.*;

import static com.google.common.base.Strings.nullToEmpty;
import static gov.nist.csd.pm.common.net.LocalHostInfo.getLocalHost;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;

public class CommandUtil {

	static Log log = new Log(Log.Level.INFO, true);

	/**
	 *
	 * @param sessionId The session id of the session creating the process
	 * @param simClient The client to be used for communication with the PM server 
	 * @return The id of the created process
	 * 
	 * This is essentially a shortcut for calling make command with the createProcess command name
	 */
	public static String createProcess(String sessionId, SSLSocketClient simClient) {
		Packet res = null;
		try {
			Packet cmd = makeCmd("createProcess", sessionId);
			res = simClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(null,
						"createProcess() returned null!");
				return null;
			}
			if (res.size() < 1) {
				JOptionPane.showMessageDialog(null,
						"no process id returned in createProcess()!");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(null,
						"Error in createProcess(): " + res.getErrorMessage());
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Exception in createProcess(): " + e.getMessage());
			return null;
		}
		return res.getStringValue(0);
	}

    public static boolean computeVos(String sSessionId, String sSessionUserId,
                                    String sVosPresType, SSLSocketClient simClient){
        try {
                Packet cmd = CommandUtil.makeCmd("computeVos", sSessionId,
                        sVosPresType, sSessionUserId, sSessionId);
                Packet res = simClient.sendReceive(cmd, null);
                if (res.hasError()) {
                    JOptionPane.showMessageDialog(null, "Error in computeVos: "
                            + res.getErrorMessage());
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Exception in computeVos: " + e.getMessage());
                return false;
            }
        return true;
    }

    public static String exitProcess(String sessionId, String processId, SSLSocketClient simClient) {
		Packet res = null;
		try {
			Packet cmd = makeCmd("exitProcess", sessionId, processId);
			res = simClient.sendReceive(cmd, null);
			if (res == null) {
				JOptionPane.showMessageDialog(null,
						"createProcess() returned null!");
				return null;
			}
			if (res.hasError()) {
				JOptionPane.showMessageDialog(null,
						"Error in createProcess(): " + res.getErrorMessage());
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Exception in createProcess(): " + e.getMessage());
			return null;
		}
		return res.getStringValue(0);
	}

    /**
     * Overloaded version of makeCmd adding type-safe command checking
     * @param cmd
     * @param sSessionId
     * @param args
     * @return
     */
    public static Packet makeCmd(PMCommand cmd, String sSessionId, String... args){
        log.debug("TRACE 18 - In CommandUtil.makeCmd() 1: cmd = " + cmd.name() + " = PmCommand CODE: " + cmd.commandCode());

        try {
            return makeCmd(cmd.commandCode(), sSessionId, args);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return failurePacket("Failure in makeCmd");
    }


	/**
	 * 
	 * @param sCode The name of the command
	 * @param sSessionId  The sessionid of the session creating the command
	 * @param sArgs  The arguments for the command
	 * @return The packet for the command
	 * @throws Exception if there is an error reading the response
	 */
	public static Packet makeCmd(String sCode, String sSessionId, String... sArgs)
			throws Exception {
        log.debug("TRACE 19 - In CommandUtil.makeCmd() 2 - Creating Packet here");

		Packet cmd = new Packet();

		cmd.addItem(ItemType.CMD_CODE, sCode);
	    cmd.addItem(ItemType.CMD_ARG, nullToEmpty(sSessionId));
		if (sArgs == null){
			return cmd;
        }
		for (String arg : sArgs) {
			cmd.addItem(ItemType.CMD_ARG, nullToEmpty(arg));
		}
		return cmd;
	}

    public static SessionInfo createSession(SSLSocketClient simClient, String username, char[] pass){
        String sHostName = getLocalHost();
        // Send the session name, host name, user name, and password to the
        // server.
        Packet res = null;
        try {
            Packet cmd = makeCmd("createSession", null, "My session",
                    sHostName, username, new String(pass));
            res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                throw new RuntimeException("Error in createSession: "
                        + res.getErrorMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception in createSession: "
                    + e.getMessage());
        }

        // The engine answer contains:
        // item 0: <sess name>
        // item 1: <sess id>
        // item 2: <user id>
        // Build the new session object.
        String sSessName = res.getStringValue(0);
        String sSessId = res.getStringValue(1);
        String sUserId = res.getStringValue(2);
        return new SessionInfo(sSessName, sSessId, sUserId);
    }

     // Just delete the session from the server.
    public static boolean exitSession(String sId, SSLSocketClient simClient) {
        Packet res = null;
        try {
            Packet cmd = makeCmd("deleteSession", null, sId);
            res = simClient.sendReceive(cmd, null);
            if (res.hasError()) {
                JOptionPane.showMessageDialog(null, "Error in deleteSession: "
                        + res.getErrorMessage());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Exception in deleteSession: "
                    + e.getMessage());
            return false;
        }
        return true;
    }

    public static class SessionInfo{
        private final String _sessionName;
        private final String _sessionId;
        private final String _userId;
        public SessionInfo(String sessionName, String sessionId, String userId){
            _sessionName= sessionName;
            _sessionId = sessionId;
            _userId = userId;
        }

        public String getUserId(){return _userId;}
        public String getSessionId(){return _sessionId;}
        public String getSessionName(){return _sessionName;}
    }

}

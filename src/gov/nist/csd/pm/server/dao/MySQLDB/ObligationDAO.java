package gov.nist.csd.pm.server.dao.MySQLDB;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.MySQL_Parameters;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.constants.ParamType;
import gov.nist.csd.pm.common.net.ItemType;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.RandomGUID;
import gov.nist.csd.pm.common.util.UtilMethods;
import gov.nist.csd.pm.server.audit.Audit;
import gov.nist.csd.pm.server.packet.SQLPacketHandler;
import gov.nist.csd.pm.server.parser.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

import static gov.nist.csd.pm.common.constants.MySQL_Functions.CREATE_SCRIPT_RECORD_FUN;
import static gov.nist.csd.pm.common.constants.MySQL_Functions.executeFunction;
import static gov.nist.csd.pm.common.constants.MySQL_Statements.*;
import static gov.nist.csd.pm.common.net.Packet.failurePacket;
import static gov.nist.csd.pm.common.net.Packet.getSuccessPacket;


public class ObligationDAO extends CommonSQLDAO{

    // A global where processEvent stores the enabled script id.
    private String sEnabledScriptId;

    public final int DEPTH = 1;
    public final int NO_DEPTH = -1;

    public final String NO = "no";
    public final String YES = "yes";

    public ObligationDAO() {
    }

    public Packet getScripts(){
        Packet packet = new Packet();
        try {
            ArrayList<ArrayList<Object>> scripts = select(GET_SCRIPTS);
            for (ArrayList<Object> script : scripts) {
                packet.addItem(ItemType.RESPONSE_TEXT, script.get(0) + GlobalConstants.PM_FIELD_DELIM + script.get(1));// id
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        return packet;

        // Packet result = new Packet();
        // NamingEnumeration<?> scripts;
        // try {
        // SearchControls constraints = new SearchControls();
        // constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        // constraints.setReturningAttributes(new String[]{"pmId", "pmName"});
        // scripts = ServerConfig.ctx.search(sRuleContainerDN, "(objectClass="
        // + sScriptClass + ")", constraints);
        // } catch (CommunicationException e) {
        // if (ServerConfig.debugFlag) {
        // e.printStackTrace();
        // }
        // return failurePacket("AD connection error");
        // } catch (Exception e) {
        // if (ServerConfig.debugFlag) {
        // e.printStackTrace();
        // }
        // return failurePacket("Exception: " + e.getMessage());
        // }
        //
        // while (scripts != null && scripts.hasMore()) {
        // SearchResult sr = (SearchResult) scripts.next();
        // String sName = (String) sr.getAttributes().get("pmName").get();
        // String sId = (String) sr.getAttributes().get("pmId").get();
        // result.addItem(ItemType.RESPONSE_TEXT, sName +
        // GlobalConstants.PM_FIELD_DELIM + sId);
        // }
        // return result;
    }

    // Enable the script specified by its id. Returns the enabled script name
    // and id,
    // or failure.

    public Packet enableScript(String sSessId, String sScriptId) {
        // Make sure the script exists.
        try {
            String sName = ServerConfig.SQLDAO.getEntityName(sScriptId,
                    GlobalConstants.PM_SCRIPT);
            if (sName == null) {
                return failurePacket("Script does not exist!");
            }

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sScriptId);

            update(ENABLE_SCRIPT, params);
            Packet res = new Packet();
            res.addItem(ItemType.RESPONSE_TEXT, sName + GlobalConstants.PM_FIELD_DELIM + sScriptId);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet disableEnabledScript(String sSessId) {
        String sScriptId = getEnabledScriptId();
        if (sScriptId == null) {
            return failurePacket("No script is enabled!");
        }

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sScriptId);

        try {
            update(DISABLE_ENABLED_SCRIPT, params);
            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }

    public Packet getEnabledScript() {
        Packet res = new Packet();

        String sId = getEnabledScriptId();
        if (sId == null) {
            return res;
        }
        try {

            String sName = ServerConfig.SQLDAO.getEntityName(sId,
                    GlobalConstants.PM_SCRIPT);
            if (sName == null) {
                return failurePacket("Inconsistency: Enabled script does not exist!");
            }
            res.addItem(ItemType.RESPONSE_TEXT, sName
                    + GlobalConstants.PM_FIELD_DELIM + sId);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    public String getEnabledScriptId() {
        try {
            ArrayList<Integer> script = extractIntegers(select(GET_ENABLED_SCRIPT));
            if(script == null || script.size() == 0){
                return null;
            }else{
                return String.valueOf(script.get(0));
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public Packet deleteScriptRule(String sScriptId, String sLabel) {
        if (sScriptId == null) {
            return failurePacket("The script id is null!");
        }
        try {
            String sName = ServerConfig.SQLDAO.getEntityName(sScriptId,
                    GlobalConstants.PM_SCRIPT);
            if (sName == null) {
                return failurePacket("No such script!");
            }
            if (sLabel == null || sLabel.length() == 0) {
                return failurePacket("Null or empty rule label!");
            }

            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sScriptId);
            params.addParam(ParamType.STRING, sLabel);
            delete(DELETE_SCRIPT_RULE, params);

            // Update the rule count in the destination script.
            params.clearParams();
            params.addParam(ParamType.INT, sScriptId);
            update(DECREASE_SCRIPT_COUNT, params);

            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return failurePacket("Exception when deleting a rule from a script: "
                    + e.getMessage());
        }
    }

    // Compile and add to the enabled script, if one exists.
    // Otherwise, enable the new script. Returns the name and id of the
    // enabled script.

    public Packet compileScriptAndAddToEnabled(String sSessId, Packet cmdPacket) {
        // Create a temporary file.
        File ftemp = null;
        String sResult;
        RuleParser ruleParser;
        // The rules start at item 3 (0 = cmd code, 1 = sess id, 2 = filler).
        try {
            ftemp = File.createTempFile("evr", ".evr", null);
            FileOutputStream fos = new FileOutputStream(ftemp);
            PrintWriter pw = new PrintWriter(fos);
            for (int i = 3; i < cmdPacket.size(); i++) {
                String sLine = cmdPacket.getStringValue(i);
                if (sLine == null) {
                    continue;
                }
                pw.println(sLine);
            }
            pw.close();

            // Create a new parser object and parse the script. Note that if
            // parsing
            // is
            // successful, the script code is written in the active directory.
            // An
            // object
            // of class pmClassScript with the script's id and name is created.
            // Other
            // scripts stored in the AD might have the same name.
            ruleParser = new RuleParser(ftemp);
            sResult = ruleParser.parse();
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return failurePacket("Exception while writing the rules to a temporary file: "
                    + e.getMessage());
        }
        if (sResult != null) {
            return failurePacket(sResult);
        }

        // Compilation is OK.
        // If no script is enabled, delete the scripts with the same
        // name as the new script and enable the new script.
        Packet res;

        String sEnScriptId = getEnabledScriptId();
        String sEnScriptName = null;
        try {

            if (sEnScriptId != null) {
                sEnScriptName = ServerConfig.SQLDAO.getEntityName(sEnScriptId,
                        GlobalConstants.PM_SCRIPT);
                if (sEnScriptName == null) {
                    sEnScriptId = null;
                }
            }

            if (sEnScriptId == null) {
                res = deleteScriptsWithNameExcept(sSessId,
                        ruleParser.getScriptName(), ruleParser.getScriptId());
                if (res.hasError()) {
                    return res;
                }
                sEnScriptId = ruleParser.getScriptId();

                res = enableScript(sSessId, sEnScriptId);
                if (res.hasError()) {
                    return res;
                }

                saveScriptSource(ruleParser.getScriptId(),
                        ruleParser.getScriptName(), cmdPacket);

                return res;
            }

            // An enabled script exists. Add the new script to it.
            res = addScript(sSessId, ruleParser.getScriptId(), sEnScriptId);
            if (res.hasError()) {
                return res;
            }

            // Now add the source for the compiled script to the end of the old
            // source.
            addScriptSource(sEnScriptId, sEnScriptName, cmdPacket);

            res = new Packet();
            res.addItem(
                    ItemType.RESPONSE_TEXT,
                    ServerConfig.SQLDAO.getEntityName(sEnScriptId,
                            GlobalConstants.PM_SCRIPT)
                            + GlobalConstants.PM_FIELD_DELIM + sEnScriptId);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
        return res;
    }

    // Compile and enable the script (deleting all other scripts with the same
    // name).
    // The script starts at item 3 in the packet.

    public Packet compileScriptAndEnable(String sSessId, Packet cmdPacket) {
        // Create a temporary file.
        File ftemp = null;
        String sResult;
        RuleParser ruleParser;
        // The rules start at item 3 (0 = cmd code, 1 = sess id, 2 = filler).
        try {
            ftemp = File.createTempFile("evr", ".evr", null);
            FileOutputStream fos = new FileOutputStream(ftemp);
            PrintWriter pw = new PrintWriter(fos);
            for (int i = 3; i < cmdPacket.size(); i++) {
                String sLine = cmdPacket.getStringValue(i);
                if (sLine == null) {
                    continue;
                }
                pw.println(sLine);
            }
            pw.close();

            // Create a new parser object and parse the script. Note that if
            // parsing
            // is
            // successful, the script code is written in the active directory.
            // An
            // object
            // of class pmClassScript with the script's id and name is created.
            // Other
            // scripts stored in the AD might have the same name.
            ruleParser = new RuleParser(ftemp);
            sResult = ruleParser.parse();
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return failurePacket("Exception while writing the rules to a temporary file: "
                    + e.getMessage());
        }
        // Delete the other scripts with the same name.
        if (sResult == null) {
            Packet res = deleteScriptsWithNameExcept(sSessId,
                    ruleParser.getScriptName(), ruleParser.getScriptId());
            if (res.hasError()) {
                return res;
            }

            // Enable the new script.
            String sEnScriptId = ruleParser.getScriptId();
            res = enableScript(sSessId, sEnScriptId);
            if (res.hasError()) {
                return res;
            }
            saveScriptSource(ruleParser.getScriptId(),
                    ruleParser.getScriptName(), cmdPacket);
            return res;
        }
        return failurePacket(sResult);
    }

    // Let scr be the script contained in the command.
    //
    // compile scr;
    // if (compilation fails) return failurePacket("Compilation failed!");
    //
    // if (! synonymExists(scr)) {
    // save scr;
    // return scr's name and id;
    // }
    //
    // s = synonym(scr);
    //
    // if (sDelOthers != YES) {
    // delete scr;
    // return failurePacket("Synonym script exists!");
    // }
    //
    // if (s is enabled) {
    // delete scr;
    // return
    // failurePacket("another script with the same name exists and is enabled");
    // }
    //
    // delete s;
    // save scr;
    // return scr's name and id;

    public Packet compileScript(String sSessId, String sDeleteOthers, Packet cmd) {
        // Create a temporary file.
        File ftemp = null;
        RuleParser ruleParser;
        String sResult;
        // The rules start at item 3 (0 = cmd code, 1 = sess id, 2 =
        // sDeleteOthers).
        // Some items could be empty, the String used to extract them would be
        // null,
        // skip them.
        try {
            ftemp = File.createTempFile("evr", ".evr", null);
            FileOutputStream fos = new FileOutputStream(ftemp);
            PrintWriter pw = new PrintWriter(fos);

            // Copy the lines to the temp file.
            for (int i = 3; i < cmd.size(); i++) {
                String sLine = cmd.getStringValue(i);
                if (sLine == null) {
                    continue;
                }
                pw.println(sLine);
            }
            pw.close();

            // Create a new parser object and parse the script. Note that if
            // parsing
            // is
            // successful, the script code is written in the active directory.
            // An
            // object
            // of class pmClassScript with the script's id and name is created.
            // Other
            // scripts stored in the AD might have the same name.
            ruleParser = new RuleParser(ftemp);
            sResult = ruleParser.parse();
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return failurePacket("Exception while writing the rules to a temporary file: "
                    + e.getMessage());
        }
        // If the compilation failed, return failure.
        if (sResult != null) {
            System.out.println("Error during compilation:" + sResult);
            return failurePacket(sResult);
        } else {
            System.out.println("Successful compilation of script "
                    + ruleParser.getScriptName());
        }

        // Compilation was successful.
        // If there is no other script with the same name, return the new
        // script's name and id.
        Packet res = new Packet();
        if (!synonymScriptsExist(ruleParser.getScriptName())) {
            System.out.println("No other scripts with the same name exist!");
            try {
                res.addItem(
                        ItemType.RESPONSE_TEXT,
                        ruleParser.getScriptName()
                                + GlobalConstants.PM_FIELD_DELIM
                                + ruleParser.getScriptId());
            } catch (Exception e) {
                e.printStackTrace();
                return failurePacket("Exception when building the result packet");
            }

            System.out.println("Trying to save script source!");
            saveScriptSource(ruleParser.getScriptId(),
                    ruleParser.getScriptName(), cmd);
            System.out.println("Saved script source!");

            return res;
        }

        // There is a script with the same name.
        // If sDeleteOthers is not YES, delete the new script and return
        // failure.
        if (!sDeleteOthers.equalsIgnoreCase(YES)) {
            System.out.println("I am at 13145 pmEngin");
            sResult = deleteScriptInternal(ruleParser.getScriptId());
            if (sResult == null) {
                return failurePacket("A script with the same name exists. The new script was deleted!");
            } else {
                return failurePacket("A script with the same name exists. Error while deleting the new script!");
            }
        }

        // There is a script with the same name, and sDeleteOthers is YES.
        // If the synonym script is enabled, we delete the new script.
        String sEnScriptId = getEnabledScriptId();
        if (sEnScriptId != null) {
            String sEnName;
            try {
                sEnName = ServerConfig.SQLDAO.getEntityName(sEnScriptId,
                        GlobalConstants.PM_SCRIPT);
            } catch (Exception e) {
                e.printStackTrace();
                return failurePacket(e.getMessage());
            }
            if (sEnName == null) {
                return failurePacket("Inconsistency: the enabled script "
                        + sEnScriptId + " does not exist!");
            }
            if (sEnName.equalsIgnoreCase(ruleParser.getScriptName())) {
                // Delete the script just compiled and return failure.
                System.out.println("13164 in PmEngin");
                sResult = deleteScriptInternal(ruleParser.getScriptId());
                if (sResult == null) {
                    return failurePacket("A script with the same name exists and is enabled. The new script was deleted!");
                }
                return failurePacket("A script with the same name exists and is enabled. Error while deleting the new script!");
            }
        }

        // The synonyn is not enabled. Delete it.
        res = deleteScriptsWithNameExcept(sSessId, ruleParser.getScriptName(),
                ruleParser.getScriptId());
        if (res.hasError()) {
            return failurePacket("A script with the same name exists. Error while deleting it!");
        }
        res = new Packet();

        try {
            res.addItem(ItemType.RESPONSE_TEXT, ruleParser.getScriptName()
                    + GlobalConstants.PM_FIELD_DELIM + ruleParser.getScriptId());
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet");
        }

        saveScriptSource(ruleParser.getScriptId(), ruleParser.getScriptName(),
                cmd);

        return res;
    }

    // Add a source script to the end of the source of a compiled script.
    // Parameters:
    // sScriptId and sScriptName: the id and name of the compiled script.
    // cmd: a Packet that contains the source script starting at item 3.

    public Packet addScriptSource(String sScriptId, String sScriptName,
                                  Packet cmd) {
        // For each line of the source to be added, create a source line object
        // and link it to the source script.
        for (int i = 3, n = 0; i < cmd.size(); i++) {
            String sLine = cmd.getStringValue(i);
            addLineToSourceScript(++n, sLine, sScriptId);
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    // Save a source script in the AD and set it as the source script of a
    // compiled
    // script.
    // Parameters:
    // sScriptId and sScriptName: the id and name of the compiled script.
    // cmd: a Packet that contains the source script starting at item 3.

    public Packet saveScriptSource(String sScriptId, String sScriptName,
                                   Packet cmd) {
        for (int i = 3, n = 0; i < cmd.size(); i++) {
            String sLine = cmd.getStringValue(i);
            addLineToSourceScript(++n, sLine != null ? sLine : "", sScriptId);

        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet addLineToSourceScript(int n, String sLine,
                                        String sSourceScriptId) {
        try {
            System.out.println("Adding line " + n + ":<" + sLine + ">");
            insert(ADD_LINE_TO_SOURCE, sSourceScriptId, sLine, n);
            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Error while saving source line " + n);
        }
    }

    public boolean synonymScriptsExist(String sScriptName) {
        int howmany = 0;
        try {
            ArrayList<ArrayList<Object>> scripts = select(GET_SCRIPTS);
            for(ArrayList<Object> s : scripts){
                String name = (String) s.get(0);
                if (name.equals(sScriptName)) {
                    howmany++;
                }
                if (howmany >= 2) {
                    return true;
                }
            }
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Add the rules of the source script to the end of the destination script.
    // Both scripts must exist. Take into account the case when the destination
    // script exists but is empty (pmFirst and pmLast are not set).
    // The source script is destroyed after the operation.
    // Return success or failure.

    public Packet addScript(String sSessId, String sSrcScriptId,
                            String sDstScriptId) {
        // If the source script does not exist, return failure.
        if (sSrcScriptId == null) {
            return failurePacket("Source script id is null!");
        }
        try {
            String sName = ServerConfig.SQLDAO.getEntityName(sSrcScriptId,
                    GlobalConstants.PM_SCRIPT);
            if (sName == null) {
                return failurePacket("Source script does not exist!");
            }

            // If the destination script does not exist, return failure.
            if (sDstScriptId == null) {
                return failurePacket("Destination script id is null!");
            }
            sName = ServerConfig.SQLDAO.getEntityName(sDstScriptId,
                    GlobalConstants.PM_SCRIPT);
            if (sName == null) {
                return failurePacket("Destination script does not exist!");
            }

            //get the rules for the src script (not destination)
            ArrayList<String> ruleIds = extractStrings(select(GET_SCRIPT_RULES, sSrcScriptId));
            int maxSeq = extractIntegers(select(GET_SCRIPT_COUNT, sDstScriptId)).get(0);

            for(String ruleId : ruleIds){
                update(UPDATE_RULE_RECORD, sDstScriptId, maxSeq);
                update(INCREASE_SCRIPT_COUNT, sDstScriptId);
                maxSeq++;
            }

            delete(DELETE_SCRIPT, sSrcScriptId);

            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return failurePacket("Exception when adding scripts: "
                    + e.getMessage());
        }
    }

    // Add the rules of script sScriptId to the end of the enabled script.
    // If no enabled script exists, the script sScriptId is enabled.
    // Return success or failure.

    public Packet addScriptToEnabled(String sSessId, String sScriptId) {
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet getSourceScript(String sSessId, String sScriptId) {
        Packet script = new Packet();

        try {
            String sName = ServerConfig.SQLDAO.getEntityName(sScriptId,
                    GlobalConstants.PM_SCRIPT);
            if (sName == null) {
                return failurePacket("Script does not exist!");
            }

            List<String> source = extractStrings(select(GET_SCRIPT_SOURCE, sScriptId));
            if (source == null) {
                return failurePacket("No source for script " + sName
                        + " with id " + sScriptId);
            }
            for(String line : source){
                script.addItem(ItemType.RESPONSE_TEXT, line);
            }
            return script;
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Error while extracting source script!");
        }
    }

    // Delete the script specified by its id. The enabled script cannot be
    // deleted.

    public Packet deleteScript(String sSessId, String sScriptId) {
        // If the script to be deleted is the enabled script, return failure.
        String sEnScriptId = getEnabledScriptId();
        if (sEnScriptId != null && sEnScriptId.equalsIgnoreCase(sScriptId)) {
            return failurePacket("The enabled script cannot be deleted!");
        }
        System.out.println("i am at 13582 pmEngin");
        String res = deleteScriptInternal(sScriptId);
        if (res == null) {
            return SQLPacketHandler.getSuccessPacket();
        } else {
            return failurePacket(res);
        }
    }

    public String deleteScriptInternal(String sScriptId) {
        System.out.println(">>>>>>>>>>>>Delete Script " + sScriptId);
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.INT, sScriptId);
            delete(DELETE_SCRIPT, params);
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception while deleting the EVER script " + sScriptId;
        }
        return null;
    }

    public String deleteSourceScript(String sSourceScriptId) {
        try {
            delete(DELETE_SCRIPT_SOURCE, sSourceScriptId);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception while deleting the source script "
                    + sSourceScriptId;
        }
    }

    public String deleteRule(String sRuleId) {
        System.out.println(">>>>>>>>>>>>Delete Rule " + sRuleId);
        String result = null;
        try {
            delete(DELETE_RULE, sRuleId);
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return "Exception while deleting the rule " + sRuleId;
        }
        return null;
    }

    public String deletePattern(String sPatternId) {
        System.out.println(">>>>>>>>>>>>Delete Pattern " + sPatternId);
        System.out.println("^^^^^^^Deleting pattern " + sPatternId);
        try {
            delete(DELETE_EVENT_PATTERN, sPatternId);
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception while deleting the pattern " + sPatternId;
        }
        return null;
    }

    public String deleteAction(String sActionId) {
        System.out.println(">>>>>>>>>>>>Delete Action " + sActionId);
        String result = null;
        try {
            delete(DELETE_ACTION, sActionId);
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception while deleting the action " + sActionId;
        }
        return null;
    }

    public String deleteCondition(String sCondId) {
        System.out.println(">>>>>>>>>>>>Delete Condition " + sCondId);
        String result = null;
        try {
            delete(DELETE_CONDITION, sCondId);
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception while deleting the condition " + sCondId;
        }
        return null;
    }

    public String deleteOperand(String sOpndId) {
        System.out.println(">>>>>>>>>>>>Delete Operand " + sOpndId);
        String result = null;
        try {
            delete(DELETE_OPERAND, sOpndId);
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return "Exception while deleting the operand " + sOpndId;
        }
        return null;
    }

    // Delete all scripts of the specified name, except the one with the
    // specified id.

    public Packet deleteScriptsWithNameExcept(String sSessId,
                                              String sScriptName, String sScriptId) {
        try{
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sScriptName);
            params.addParam(ParamType.STRING, sScriptId);
            delete(DELETE_SCRIPT_WITH_NAME, params);
        }catch(Exception e){
            e.printStackTrace();
            errorMessage = "exception deleting scripts with name = " + sScriptName;
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    // This is called from the parser at the start of a new compilation.
    // It creates a script record with the specified name and id.

    public String createScriptRecord(String scriptId, String scriptName) {
        MySQL_Parameters params = new MySQL_Parameters();
        params.setOutParamType(ParamType.INT);
        params.addParam(ParamType.STRING, scriptName);

        try {
            return executeFunction(CREATE_SCRIPT_RECORD_FUN, params).toString();
        }catch(Exception e){
            e.printStackTrace();
            return "Unable to create script record";
        }
    }

    // Called from the parser for one rule if parsing is OK for that rule.
    // Generate the code for the rule.

    public String generateRuleCode(RuleSpec ruleSpec, String scriptId,
                                   String prevRuleId) throws Exception {
        String result = checkRuleSemantics(ruleSpec);
        if (result != null) {
            return result;
        }

        return writeRuleCode(ruleSpec, scriptId);
    }

    // If this is the first rule (prevRuleId = null), add the rule to the script
    // record as the first rule.
    // Otherwise, set the double link between previous rule and this rule.
    // Increase the rule count in the script record.

    public String addRuleToScript(RuleSpec ruleSpec, String scriptId,
                                  String prevRuleId) {
        if(writeRuleRecord(ruleSpec, scriptId) != null){
            return "could not add rule to script";
        }
        return null;
    }

    public String writeRuleCode(RuleSpec ruleSpec, String scriptId) {
        String result = null;

        // Get the pattern spec and the action specs.
        Vector<ActionSpec> actSpecs = ruleSpec.getActions();

        result = writeRuleRecord(ruleSpec, scriptId);
        if (result != null) {
            return result;
        }

        result = writePatternRecord(ruleSpec);
        if (result != null) {
            return result;
        }

        // Write the actions to AD.
        traceTitle("RESPONSE ACTIONS");
        if (actSpecs.isEmpty()) {
            return null;
        }

        String sPrevActId = null;

        for (int nActRank = 0; nActRank < actSpecs.size(); nActRank++) {
            ActionSpec actSpec = actSpecs.get(nActRank);

            // First create the AD record, links, etc.
            result = createActionRecord(ruleSpec, actSpec, sPrevActId, nActRank);
            if(result != null){
                return result;
            }

            // Write the condition record.
            result = writeActionCondRecord(actSpec);
            if (result != null) {
                return result;
            }

            // Then fill in the record.
            String type = actSpec.getType();
            if (type.startsWith(RuleScanner.PM_VALUE_ASSIGN)) {
                result = writeAssignActionRecord(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_GRANT)) {
                result = writeGrantActionRecord(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_CREATE)) {
                result = writeCreateActionRecord(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_DENY)) {
                result = writeDenyActionRecord(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
                    + RuleScanner.PM_VALUE_ASSIGNMENT)) {
                result = writeDeassignActionRecord(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
                    + RuleScanner.PM_VALUE_DENY)) {
                result = writeDeleteDenyActionRecord(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
                    + RuleScanner.PM_VALUE_RULE)) {
                result = writeDeleteRuleActionRecord(actSpec);
                if (result != null) {
                    return result;
                }
            }
            sPrevActId = actSpec.getId();
        }

        return null;
    }

    public String writePatternRecord(RuleSpec ruleSpec) {
        PatternSpec patSpec = ruleSpec.getPattern();

        String sPatternId = patSpec.getId();

        boolean isAny = patSpec.isAny();
        boolean isActive = patSpec.isActive();

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sPatternId);
        params.addParam(ParamType.STRING, ruleSpec.getId());
        params.addParam(ParamType.INT, isAny ? 1 : 0);
        params.addParam(ParamType.INT, isActive ? 1 : 0);

        try{
            insert(WRITE_PATTERN_RECORD, params);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to write pattern record to database";
        }

        traceTitle("EVENT PATTERN id = " + patSpec.getId());
        traceLine("  isAny = " + patSpec.isAny());
        traceLine("  isActive = " + patSpec.isActive());

        // Write the user specs:
        traceTitle("Event users");
        Set<UserSpec> UserSet = patSpec.getUserSpecs();
        if (UserSet.isEmpty()) {
            params.clearParams();
            params.addParam(ParamType.STRING, sPatternId);
            params.addParam(ParamType.STRING, "u");
            params.addParam(ParamType.STRING, "*");
            try{
                insert(ADD_USER_SPEC, params);
            } catch (Exception e) {
                e.printStackTrace();
                return "Unable to add a user specification to the event pattern object!";
            }
            traceLine("  userSpec(\"*\", \"" + PM_NODE.USER.value + "\")");
        } else {
            Iterator<UserSpec> hsiter;
            hsiter = UserSet.iterator();
            while (hsiter.hasNext()) {
                UserSpec spec = hsiter.next();
                params.clearParams();
                params.addParam(ParamType.STRING, sPatternId);
                params.addParam(ParamType.STRING, spec.getType());
                params.addParam(ParamType.STRING, spec.getName());
                try{
                    insert(ADD_USER_SPEC, params);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Unable to add a user specification to the event pattern object!";
                }
                traceLine("  userSpec(name = \"" + spec.getName() + "\", "
                        + "id = \"" + spec.getId() + "\", " + "type = \""
                        + spec.getType() + "\")");
            }
        }
        // Write the policy specs.
        traceTitle("Event policy classes");
        Set<PcSpec> pcSet = patSpec.getPcSpecs();
        if (pcSet.isEmpty()) {
            params.clearParams();
            params.addParam(ParamType.STRING, sPatternId);
            params.addParam(ParamType.STRING, "p");
            params.addParam(ParamType.STRING, "*");
            try{
                insert(ADD_PC_SPEC, params);
            } catch (Exception e) {
                e.printStackTrace();
                return "Unable to add a policy specification to the event pattern object!";
            }
            traceLine("  policySpec(\"*\", \"" + PM_NODE.POL.value + "\")");
        } else {
            Iterator<PcSpec> hsiter = pcSet.iterator();
            while (hsiter.hasNext()) {
                PcSpec spec = hsiter.next();
                params.clearParams();
                params.addParam(ParamType.STRING, sPatternId);
                params.addParam(ParamType.STRING, spec.getType());
                params.addParam(ParamType.STRING, spec.getName());
                try{
                    insert(ADD_PC_SPEC, params);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Unable to add a policy specification to the event pattern object!";
                }
                traceLine("  pcSpec(name = \"" + spec.getName() + "\", "
                        + "id = \"" + spec.getId() + "\", " + "type = \""
                        + spec.getType() + "\")");
            }
        }

        // Write the op specs.
        traceTitle("Event operations");
        Set<OpSpec> opSet = patSpec.getOpSpecs();
        if (opSet.isEmpty()) {
            params.clearParams();
            params.addParam(ParamType.STRING, sPatternId);
            //params.addParam(ParamType.STRING, "op");
            params.addParam(ParamType.STRING, "*");
            try{
                insert(ADD_OP_SPEC, params);
            } catch (Exception e) {
                e.printStackTrace();
                return "Unable to add an operation specification to the event pattern object!";
            }
            traceLine("  opSpec(\"*\", \"" + GlobalConstants.PM_OP + "\")");
        } else {
            Iterator<OpSpec> hsiter = opSet.iterator();
            while (hsiter.hasNext()) {
                OpSpec spec = hsiter.next();
                params.clearParams();
                params.addParam(ParamType.STRING, sPatternId);
                //params.addParam(ParamType.STRING, "op");
                params.addParam(ParamType.STRING, spec.getName());
                try{
                    insert(ADD_OP_SPEC, params);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Unable to add an operation specification to the event pattern object!";
                }
                traceLine("  opSpec(\"" + spec.getName() + "\", \""
                        + spec.getType() + "\")");
            }
        }

        // Write the obj specs. There is only one. If multiple objects are
        // desired,
        // use their associated object attributes as containers.
        traceTitle("Event objects");
        Set<ObjSpec> hs = patSpec.getObjSpecs();
        if (hs.isEmpty()) {
            params.clearParams();
            params.addParam(ParamType.STRING, sPatternId);
            params.addParam(ParamType.STRING, "o");
            params.addParam(ParamType.STRING, "*");
            try{
                insert(ADD_OBJ_SPEC, params);
            } catch (Exception e) {
                e.printStackTrace();
                return "Unable to add an object specification to the event pattern object!";
            }
            traceLine("  objSpec(\"*\", \"" + GlobalConstants.PM_OBJ + "\")");
        } else {
            Iterator<ObjSpec> hsiter = hs.iterator();
            while (hsiter.hasNext()) {
                ObjSpec spec = hsiter.next();
                params.clearParams();
                params.addParam(ParamType.STRING, sPatternId);
                params.addParam(ParamType.STRING, spec.getType());
                params.addParam(ParamType.STRING, spec.getName());
                try{
                    insert(ADD_OBJ_SPEC, params);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Unable to add a obj specification to the event pattern object!";
                }
                traceLine("  objSpec(name = \"" + spec.getName() + "\", "
                        + "id = \"" + spec.getId() + "\", " + "type = \""
                        + spec.getType() + "\")");
            }
        }

        // Write the container specs.
        traceTitle("Event object containers");
        Set<ContSpec> contSet = patSpec.getContSpecs();
        if (contSet.isEmpty()) {
            params.clearParams();
            params.addParam(ParamType.STRING, sPatternId);
            params.addParam(ParamType.STRING, "b");
            params.addParam(ParamType.STRING, "*");
            try{
                insert(ADD_CONT_SPEC, params);
            } catch (Exception e) {
                e.printStackTrace();
                return "Unable to add an object container specification to the event pattern object!";
            }
            traceLine("  contSpec(\"*\", \"" + GlobalConstants.PM_OBJ + "\")");
        } else {
            Iterator<ContSpec> hsiter = contSet.iterator();
            while (hsiter.hasNext()) {
                ContSpec spec = hsiter.next();
                params.clearParams();
                params.addParam(ParamType.STRING, sPatternId);
                params.addParam(ParamType.STRING, spec.getType());
                params.addParam(ParamType.STRING, spec.getName());
                try{
                    insert(ADD_CONT_SPEC, params);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Unable to add an object container specification to the event pattern object!";
                }
                traceLine("  contSpec(name = \"" + spec.getName() + "\", "
                        + "id = \"" + spec.getId() + "\", " + "type = \""
                        + spec.getType() + "\")");
            }
        }
        return null;
    }

    // Generate an event.
    public Packet sendObject(String sSessId, String sObjName, String sReceiver) {
        String sObjId = "";
        try {
            sObjId = ServerConfig.SQLDAO.getEntityId(sObjName, GlobalConstants.PM_OBJ);
            return  processEvent(sSessId, null, GlobalConstants.PM_EVENT_OBJECT_SEND,
                    sObjName, sObjId, null, null, sReceiver, null);
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }
    }


    public String writeRuleRecord(RuleSpec ruleSpec, String scriptId) {
        String sLabel = ruleSpec.getLabel();
        PatternSpec patSpec = ruleSpec.getPattern();
        List<ActionSpec> actSpecs = ruleSpec.getActions();
        int nActs = actSpecs.size();
        try{
            insert(WRITE_RULE_RECORD, ruleSpec.getId(), sLabel == null ? "" : sLabel,
                    nActs, ruleSpec.getRank(), scriptId);
            update(INCREASE_SCRIPT_COUNT, scriptId);
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to write rule record";
        }

        traceTitle("RULE id = " + ruleSpec.getId());
        traceLine("  event id = " + patSpec.getId());

        return null;

    }

    public boolean isEvent(String sName) {
        for (int i = 0; i < GlobalConstants.sEventNames.length; i++) {
            if (GlobalConstants.sEventNames[i].equalsIgnoreCase(sName)) {
                return true;
            }
        }
        return false;
    }

    public String checkRuleSemantics(RuleSpec ruleSpec) throws Exception {
        PatternSpec patSpec = ruleSpec.getPattern();
        List<ActionSpec> actSpecs = ruleSpec.getActions();
        String result = null;

        // Check user specs in the event pattern. Note that the user spec
        // could in fact be a session spec or a process spec.
        Set<UserSpec> hs = patSpec.getUserSpecs();
        Iterator<UserSpec> hsiter;
        hsiter = hs.iterator();
        while (hsiter.hasNext()) {
            UserSpec spec = hsiter.next();
            String sType = spec.getType();
            if (sType.equalsIgnoreCase(PM_NODE.AUATTR.value)) {
                sType = PM_NODE.UATTR.value;
            }
            String sName = spec.getName();
            String sId = spec.getId();
            if (sId == null) {
                if (sName == null) {
                    return "The user spec has both name and id null!";
                }
                sId = ServerConfig.SQLDAO.getEntityId(sName, sType);
                if (sId == null) {
                    return "No entity " + sName + " of type " + sType;
                }
                spec.setId(sId);
            } else {
                if (sName == null) {
                    sName = ServerConfig.SQLDAO.getEntityName(sId, sType);
                    if (sName == null) {
                        return "No entity with id " + sId + " of type " + sType;
                    }
                }
                spec.setName(sName);
            }
        }

        // Check policy class specs in the event pattern:
        Set<PcSpec> pcSpec = patSpec.getPcSpecs();
        Iterator<PcSpec> pcIterator = pcSpec.iterator();
        while (pcIterator.hasNext()) {
            PcSpec spec = pcIterator.next();
            String sType = spec.getType();
            String sName = spec.getName();
            String sId = ServerConfig.SQLDAO.getEntityId(sName, sType);
            if (sId == null) {
                return ("No PM policy class \"" + sName + "\"!");
            }
            spec.setId(sId);
        }

        // Check event/op specs in the event pattern:
        Set<OpSpec> opSet = patSpec.getOpSpecs();
        Iterator<OpSpec> opIterator = opSet.iterator();
        while (opIterator.hasNext()) {
            OpSpec spec = opIterator.next();
            String sName = spec.getName();
            System.out.println("Op name = " + sName);
            if (!isEvent(sName)) {
                return ("No PM event \"" + sName + "\"!");
            }
        }

        // Check object specs in the event pattern:
        Set<ObjSpec> objSet = patSpec.getObjSpecs();
        Iterator<ObjSpec> objIterator = objSet.iterator();
        while (opIterator.hasNext()) {
            ObjSpec spec = objIterator.next();
            String sName = spec.getName();
            String sId = null;

            // sType can be: GlobalConstants.PM_OBJ. Change the type to object
            // attribute
            // and check that the object really exists.
            spec.setType(PM_NODE.OATTR.value);
            sId = ServerConfig.SQLDAO.getEntityId(sName, PM_NODE.OATTR.value);

            // When the target object is the object just created and written,
            // the object name is unknown at compile time, so we don't signal
            // an error now. We just let the id be null.

            // if (sId == null || !hasAssocObj(sId)) return "No object \"" +
            // sName + "\"!";

            spec.setId(sId);
        }

        // Check container specs in the event pattern:
        Set<ContSpec> contSpecs = patSpec.getContSpecs();
        Iterator<ContSpec> contIterator = contSpecs.iterator();
        while (contIterator.hasNext()) {
            ContSpec spec = contIterator.next();
            System.out.println("Container before: \n" + spec);
            String sName = spec.getName();
            String sType = spec.getType();
            String sId = null;

            // sType can be: GlobalConstants.PM_OBJ,PM_NODE.OATTR.value,
            // GlobalConstants.PM_RECORD, or GlobalConstants.PM_OBJ_CLASS.
            // Instead of
            // objects use the associated object attributes.
            // For records (containers with a small addition - a template plus
            // other
            // few things), the name can be * (for "any record").
            if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
                sType = PM_NODE.OATTR.value;
                spec.setType(PM_NODE.OATTR.value);
            } else if (sType.equalsIgnoreCase(GlobalConstants.PM_RECORD)) {
                sType = PM_NODE.OATTR.value;
            }
            // getIdOfEntityWithNameAndType(*, t) should return * (any).
            sId = ServerConfig.SQLDAO.getEntityId(sName, sType);
            if (sId == null) {
                return "No object or object attribute or class \"" + sName
                        + "\"!";
            }
            spec.setId(sId);
            System.out.println("Container After: \n" + spec);
        }

        // Check actions in the rule.
        for (int vindex = 0; vindex < actSpecs.size(); vindex++) {
            ActionSpec actSpec = actSpecs.get(vindex);

            // Check the action's condition.
            result = checkCondSemantics(actSpec);
            if (result != null) {
                return result;
            }

            String type = actSpec.getType();
            if (type.startsWith(RuleScanner.PM_VALUE_ASSIGN)) {
                result = checkAssignSemantics(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_GRANT)) {
                result = checkGrantSemantics(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_CREATE)) {
                result = checkCreateSemantics(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_DENY)) {
                result = checkDenySemantics(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
                    + RuleScanner.PM_VALUE_ASSIGNMENT)) {
                result = checkDeassignSemantics(actSpec);
                if (result != null) {
                    return result;
                }
            } else if (type.startsWith(RuleScanner.PM_VALUE_DELETE + " "
                    + RuleScanner.PM_VALUE_DENY)) {
                result = checkDeleteDenySemantics(actSpec);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public String checkCondSemantics(ActionSpec actSpec) throws Exception {
        CondSpec condSpec = actSpec.getCondSpec();
        System.out.println("Checking condition's semantics");
        if (condSpec == null) {
            return null;
        }
        String sType = condSpec.getType();
        System.out.println("Condition of type " + sType);
        if (!sType.equalsIgnoreCase(RuleScanner.PM_VALUE_EXISTS)) {
            return "Condition type is not \"" + RuleScanner.PM_VALUE_EXISTS
                    + "\"!";
        }
        Set<List<OpndSpec>> opnds = condSpec.getOpnds1();
        String result = null;
        Iterator<List<OpndSpec>> hsiter = opnds.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, false);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public String checkDeassignSemantics(ActionSpec actSpec) throws Exception {
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        String result = null;

        Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        hsiter = opnds2.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public String checkAssignSemantics(ActionSpec actSpec) throws Exception {
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        String result = null;

        Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        Iterator<List<OpndSpec>> opnds2Iterator = opnds2.iterator();
        while (opnds2Iterator.hasNext()) {
            List<OpndSpec> opndVec = opnds2Iterator.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    // Check the semantics of the operand contained in a vector.
    // The operand is a PM entity name or function call and is stored as a tree
    // in a vector. First item (0) is the root of the tree. Each item points
    // to its children and to its parent. A simple name does not have children.
    // A function call has its arguments as children. Each item is of class
    // OpndSpec. A side effect of the semantic checks is setting the ids of
    // existing PM entities.
    // The arguments are: the vector containing the operand, the index
    // of the component to be first checked, and a boolean that indicates
    // whether to check the existence of a PM entity or not.

    public String checkOpndSemantics(List<OpndSpec> opndVec, int index,
                                     boolean checkExist) throws Exception {
        String result = null;
        OpndSpec os = opndVec.get(index);

        // Operand is a PM entity (process, session, user, uattr, object, oattr,
        // policy, base, opset, class).
        if (!os.isFunction()) {
            String sName = os.getOrigName();
            String sType = os.getType();
            String sOrigId = null;
            if (!checkExist) {
                RandomGUID myGUID = new RandomGUID();
                os.setOrigId(myGUID.toStringNoDashes());
                return null;
            }
            if (sType.equalsIgnoreCase(GlobalConstants.PM_SESSION)) {
                // Look for the operand as a session.
                sOrigId = ServerConfig.SQLDAO.getEntityId(sName,
                        GlobalConstants.PM_SESSION);
                if (sOrigId == null) {
                    return "No PM entity \"" + sName + "\" of type \"" + sType
                            + "\"";
                }
            } else if (sType.equalsIgnoreCase(GlobalConstants.PM_PROCESS)) {
                // Look for the operand as a process.
                // The process name and id are identical.
                sOrigId = sName;
            } else if (sType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
                // Look for the operand as an object attribute but associated to
                // an object.
                sOrigId = ServerConfig.SQLDAO.getEntityId(sName, PM_NODE.OATTR.value);
                if (sOrigId == null) {
                    return "No PM entity \"" + sName + "\" of type \"" + sType
                            + "\"";
                }
                if (!ServerConfig.SQLDAO.hasAssocObj(sOrigId)) {
                    return "No PM entity \"" + sName + "\" of type \"" + sType
                            + "\"";
                }
            } else if (sType.equalsIgnoreCase(GlobalConstants.PM_UNKNOWN)) {
                RandomGUID myGUID = new RandomGUID();
                sOrigId = myGUID.toStringNoDashes();
            } else if (sType.equalsIgnoreCase(PM_NODE.CONN.value)) {
                sOrigId = ServerConfig.SQLDAO.getEntityId(
                        GlobalConstants.PM_CONNECTOR_NAME, sType);
                if (sOrigId == null) {
                    return "No PM entity \""
                            + GlobalConstants.PM_CONNECTOR_NAME
                            + "\" of type \"" + sType + "\"";
                }
            } else {
                sOrigId = ServerConfig.SQLDAO.getEntityId(sName, sType);
                if (sOrigId == null) {
                    return "No PM entity \"" + sName + "\" of type \"" + sType
                            + "\"";
                }
            }
            os.setOrigId(sOrigId);
            return null;
        }

        // Operand is a function.
        String sFunName = os.getOrigName();
        // Find out whether the function exists.
        if (!evrFunctionExists(sFunName)) {
            return "No EVER function \"" + sFunName + "\"";
        }
        os.setOrigId("F");

        // Check the number of arguments against the number of parameters. Note
        // that
        // the number of arguments may be larger than the number of parameters.
        List<Integer> children = os.getChildren();
        result = checkNumberOfArgs(sFunName, children.size());
        if (result != null) {
            return result;
        }

        // For each function parameter, check the corresponding argument type
        // against the
        // parameter type. Some arguments may not have corresponding parameters.
        // We don't
        // check those arguments.
        for (int j = 0; j < children.size(); j++) {
            int child = children.get(j).intValue();
            result = checkOpndSemantics(opndVec, child, checkExist);
            if (result != null) {
                return result;
            }

            // Check whether the argument type matches the function parameter
            // type.
            result = checkFunctionArg(sFunName, j, opndVec.get(child));
            if (result != null) {
                return result;
            }
        }

        // Check the function type.
        String sFunType = os.getType();
        result = checkFunctionType(sFunName, sFunType);
        if (result != null) {
            return result;
        }

        // Anything else???

        return result;
    }

    // First argument is the function name.
    // Second argument is the number of actual arguments.
    // One can find the number of formal parameters using the function name.
    // Originally, we wanted the number of formal parameters to be the same
    // as the number of actual arguments, but later we thought about allowing
    // functions with an unknown number of arguments, so we allow
    // the number of actual arguments to be larger than the number
    // of formal parameters, and we check the types only for the common ones.
    // Returns null, or err message: number of arguments mismatch.

    public String checkNumberOfArgs(String sFunName, int nArgCount) {
        int nParamCount = 0;

        try {
            String sTypes = getEvrFunctionParamTypes(sFunName);
            if (sTypes == null) {
                nParamCount = 0;
            } else {
                String[] pieces = sTypes.split(",");
                nParamCount = pieces.length;
            }
            // if (nArgCount != nParamCount) return
            // "Argument number mismatch for function " + sFunName;
            if (nArgCount < nParamCount) {
                return "Too few arguments for function " + sFunName;
            }
            return null;
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return "Exception when matching the number of arguments for function "
                    + sFunName;
        }
    }

    // Tries to match the nth argument and parameter types. The argument type
    // probably is GlobalConstants.PM_UNKNOWN.
    // If the parameter type is GlobalConstants.PM_UNKNOWN, do not check
    // further.
    // Otherwise, check whether an entity of the param type with the name of
    // the argument exists.
    // Returns null if OK, or err message if argument type mismatch.

    public String checkFunctionArg(String sFunName, int argIndex, OpndSpec arg) {
        try {
            String sTypes = getEvrFunctionParamTypes(sFunName);
            // If the function has no parameters, we still allow arguments.
            if (sTypes == null) {
                return null;
            }

            // Get the parameter count.
            String[] pieces = sTypes.split(",");
            int nParamCount = pieces.length;

            // We don't check arguments that do not have corresponding
            // parameters.
            if (argIndex >= nParamCount) {
                return null;
            }

            // Get the param type.
            String sParamType = pieces[argIndex];

            // If the param type is unknown, ok.
            if (sParamType.equalsIgnoreCase(GlobalConstants.PM_UNKNOWN)) {
                return null;
            }

            // The param type is known. First get the argument name.
            String sArgName = arg.getOrigName();

            // If the argument is a function call, check this function type
            // against
            // the parameter type.
            if (arg.isFunction()) {
                if (!evrFunctionExists(sArgName)) {
                    return "No EVER function \"" + sArgName + "\"";
                }
                return checkFunctionType(sArgName, sParamType);
            }

            // Otherwise, check that an entity of the parameter type with the
            // name
            // of the argument exists.
            String sOrigId = ServerConfig.SQLDAO.getEntityId(sArgName, sParamType);
            if (sOrigId == null) {
                return "No entity " + sArgName + " of type " + sParamType
                        + " (argument " + argIndex + " of function " + sFunName
                        + ")";
            }
            arg.setOrigId(sOrigId);
            arg.setType(sParamType);
            return null;
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return "Exception when matching the number of arguments of function "
                    + sFunName;
        }
    }

    // Tries to match the declared and required function type.
    // The required type may be unknown (no check), or specified explicitly
    // in the EVER script, or is the type of another function's parameter,
    // like in f1(f2(...)), where sFunId identifies f2, and sActType
    // is the type of the first argument of f1.
    // Returns null, or err message: function type mismatch.

    public String checkFunctionType(String sFunName, String sReqType) {
        if (sReqType.equalsIgnoreCase(GlobalConstants.PM_UNKNOWN)) {
            return null;
        }
        try {
            String sDclType = getEvrFunctionType(sFunName);
            if (sDclType == null) {
                return "No function or no type for function " + sFunName;
            }

            if (sDclType.equalsIgnoreCase(sReqType)) {
                return null;
            }
            if (sDclType.equalsIgnoreCase(PM_NODE.OATTR.value)
                    && sReqType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
                return null;
            }
            return "Type mismatch for function " + sFunName;
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return "Exception when matching the type of function " + sFunName;
        }
    }

    public String getEvrFunctionType(String sName) {
        for (int i = 0; i < GlobalConstants.evrFunctions.length; i++) {
            if (sName.equalsIgnoreCase(GlobalConstants.evrFunctions[i]
                    .getName())) {
                return GlobalConstants.evrFunctions[i].getType();
            }
        }
        return null;
    }

    public String getEvrFunctionParamTypes(String sName) {
        for (int i = 0; i < GlobalConstants.evrFunctions.length; i++) {
            if (sName.equalsIgnoreCase(GlobalConstants.evrFunctions[i]
                    .getName())) {
                return GlobalConstants.evrFunctions[i].getParamTypes();
            }
        }
        return null;
    }

    public boolean evrFunctionExists(String sName) {
        for (int i = 0; i < GlobalConstants.evrFunctions.length; i++) {
            if (sName.equalsIgnoreCase(GlobalConstants.evrFunctions[i]
                    .getName())) {
                return true;
            }
        }
        return false;
    }

    public String checkGrantSemantics(ActionSpec actSpec) throws Exception {
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
        String result = null;

        // Opnds1 is a HashSet; each item is a vector of OpndsSpecs
        // that describes a user attribute.
        Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        hsiter = opnds2.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            Iterator<OpndSpec> hsiter2 = opndVec.iterator();
            while (hsiter2.hasNext()) {
                OpndSpec os = hsiter2.next();
                if (!ServerConfig.SQLDAO.isOperation(os.getOrigName())) {
                    return "\"" + os.getOrigName() + "\" is not an operation";
                }
            }
        }

        // Opnds3 is a HashSet; each item is a vector of OpndsSpecs
        // that describes an object (attribute).
        hsiter = opnds3.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    // Opnds1 = what to create, opnds2 = representing what,
    // opnds3 = what properties, opnds4 = where to create.

    public String checkCreateSemantics(ActionSpec actSpec) throws Exception {
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
        Set<List<OpndSpec>> opnds4 = actSpec.getOpnds4();
        String result = null;

        // Opnds1 is a HashSet; each item (there should be exactly one item)
        // is a vector of OpndsSpec that describes the entity to be created.
        Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, false);
                if (result != null) {
                    return result;
                }
            }
        }

        // Opnds2 is a HashSet; each item (there should be exactly one item)
        // is a vector of OpndsSpec that describes the entity represented
        // by the entity to be created. In Addition, the entity to be created
        // must
        // be an object.
        hsiter = opnds2.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        // Opnds3 is a HashSet; each item (there may be more than one or none)
        // is a vector of OpndsSpec that defines a property of the entity to be
        // created.
        hsiter = opnds3.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, false);
                if (result != null) {
                    return result;
                }
            }
        }

        // Opnds4 is a HashSet; each item (there should be exactly one item)
        // is a vector of OpndsSpec that defines the container where the new
        // entity is to be created.
        hsiter = opnds4.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public String checkDenySemantics(ActionSpec actSpec) throws Exception {
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
        String result = null;

        // Opnds1 is a HashSet; each item is a vector of OpndsSpecs
        // that describes a user, user attribute, session, or process.
        Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        // Opnds2 is a HashSet; each item is a OpndsSpec containing
        // exactly one granted operation.
        hsiter = opnds2.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndSpecList = hsiter.next();

            Iterator<OpndSpec> hister2 = opndSpecList.iterator();
            while (hister2.hasNext()) {
                OpndSpec os = hister2.next();

                if (!ServerConfig.SQLDAO.isOperation(os.getOrigName())) {
                    return "\"" + os.getOrigName() + "\" is not an operation";
                }
            }

        }

        // Opnds3 is a HashSet; each item is a vector of OpndsSpecs
        // that describes an object attribute.
        hsiter = opnds3.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public String checkDeleteDenySemantics(ActionSpec actSpec) throws Exception {
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
        String result = null;

        // Opnds1 is a HashSet; each item is a vector of OpndsSpecs
        // that describes a user or user attribute.
        Iterator<List<OpndSpec>> hsiter = opnds1.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }

        hsiter = opnds2.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndSpecList = hsiter.next();
            Iterator<OpndSpec> hister2 = opndSpecList.iterator();
            while (hister2.hasNext()) {
                OpndSpec os = hister2.next();
                if (!ServerConfig.SQLDAO.isOperation(os.getOrigName())) {
                    return "\"" + os.getOrigName() + "\" is not an operation";
                }
            }
        }

        // Opnds3 is a HashSet; each item is a vector of OpndsSpecs
        // that describes an object attribute.
        hsiter = opnds3.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            if (!opndVec.isEmpty()) {
                result = checkOpndSemantics(opndVec, 0, true);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public String createActionRecord(RuleSpec ruleSpec, ActionSpec actSpec, String sPrevActId,
                                     int nActRank) {
        traceTitle("Action " + actSpec.getType() + ", id = " + actSpec.getId());
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, actSpec.getId());
        params.addParam(ParamType.STRING, actSpec.getType());
        params.addParam(ParamType.BOOLEAN, actSpec.isIntrasession());
        params.addParam(ParamType.BOOLEAN, actSpec.isIntersection());
        params.addParam(ParamType.INT, nActRank);
        params.addParam(ParamType.STRING, ruleSpec.getId());
        try {
            insert(CREATE_ACTION_RECORD, params);
        } catch (Exception e) {
            return "failed creating action record: " + e.getMessage();
        }
        return null;
    }

    public String writeActionCondRecord(ActionSpec actSpec) {
        Iterator<List<OpndSpec>> hsiter;
        List<OpndSpec> opndVec;

        CondSpec condSpec = actSpec.getCondSpec();
        if (condSpec == null) {
            return null;
        }

        String sCondId = condSpec.getId();
        traceTitle("Condition " + condSpec.getType() + ", id = " + sCondId);

        try {
            insert(WRITE_ACTION_COND_RECORD, sCondId, actSpec.getId(), condSpec.getType(), condSpec.isNegated());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Set<List<OpndSpec>> opnds1 = condSpec.getOpnds1();

        // Write the operand AD objects.
        traceTitle("  First Operands:");
        String result = writeCondOpndRecords(opnds1, actSpec.getId(), 1);
        if (result != null) {
            return result;
        }

        return null;
    }

    public String writeDeleteRuleActionRecord(ActionSpec actSpec) {
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();

        traceTitle("  First Operands:");
        String result = writeOpndRecords(opnds1, actSpec.getId(), 1);
        if (result != null) {
            return result;
        }
        return null;
    }

    public String writeDeleteDenyActionRecord(ActionSpec actSpec) {
        List<OpndSpec> opndVec;
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();

        traceTitle("  First Operands:");
        String result = writeOpndRecords(opnds1, actSpec.getId(), 1);
        if (result != null) {
            return result;
        }

        traceTitle("  Second Operands:");
        result = writeOpndRecords(opnds2, actSpec.getId(), 2);
        if (result != null) {
            return result;
        }

        traceTitle("  Third Operands:");
        result = writeOpndRecords(opnds3, actSpec.getId(), 3);
        if (result != null) {
            return result;
        }
        return null;
    }

    public String writeDeassignActionRecord(ActionSpec actSpec) {
        Iterator<List<OpndSpec>> hsiter;
        List<OpndSpec> opndVec;

        // Write the operands to the active directory.
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();

        traceTitle("  First Operands:");
        String result = writeOpndRecords(opnds1, actSpec.getId(), 1);
        if (result != null) {
            return result;
        }

        traceTitle("  Second Operands:");
        result = writeOpndRecords(opnds2, actSpec.getId(), 2);
        if (result != null) {
            return result;
        }
        return null;
    }

    public String writeAssignActionRecord(ActionSpec actSpec) {
        Iterator<List<OpndSpec>> hsiter;
        List<OpndSpec> opndVec;

        // Write the operands to the active directory.
        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();

        traceTitle("  First Operands:");
        String result = writeOpndRecords(opnds1, actSpec.getId(), 1);
        if (result != null) {
            return result;
        }

        traceTitle("  Second Operands:");
        result = writeOpndRecords(opnds2, actSpec.getId(), 2);
        if (result != null) {
            return result;
        }
        return null;
    }

    public String writeCreateActionRecord(ActionSpec actSpec) {
        Iterator<List<OpndSpec>> hsiter;
        List<OpndSpec> opndVec;

        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();
        Set<List<OpndSpec>> opnds4 = actSpec.getOpnds4();

        traceTitle("  First Operands:");
        String result = writeOpndRecords(opnds1, actSpec.getId(), 1);
        if (result != null) {
            return result;
        }

        traceTitle("  Second Operands:");
        result = writeOpndRecords(opnds2, actSpec.getId(), 2);
        if (result != null) {
            return result;
        }

        traceTitle("  Third Operands:");
        result = writeOpndRecords(opnds3, actSpec.getId(), 3);
        if (result != null) {
            return result;
        }

        traceTitle("  Fourth Operands:");
        result = writeOpndRecords(opnds4, actSpec.getId(), 4);
        if (result != null) {
            return result;
        }

        return null;
    }

    public String writeGrantActionRecord(ActionSpec actSpec) {
        Iterator<List<OpndSpec>> hsiter;
        List<OpndSpec> opndVec;

        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();

        traceTitle("  First Operands:");
        String result = writeOpndRecords(opnds1, actSpec.getId(), 1);
        if (result != null) {
            return result;
        }

        traceTitle("  Second Operands:");
        result = writeOpndRecords(opnds2, actSpec.getId(), 2);
        if (result != null) {
            return result;
        }

        traceTitle("  Third Operands:");
        result = writeOpndRecords(opnds3, actSpec.getId(), 3);
        if (result != null) {
            return result;
        }
        return null;
    }

    public String writeDenyActionRecord(ActionSpec actSpec) {
        Iterator<List<OpndSpec>> hsiter;
        List<OpndSpec> opndVec;

        Set<List<OpndSpec>> opnds1 = actSpec.getOpnds1();
        Set<List<OpndSpec>> opnds2 = actSpec.getOpnds2();
        Set<List<OpndSpec>> opnds3 = actSpec.getOpnds3();

        traceTitle("  First Operands:");
        String result = writeOpndRecords(opnds1, actSpec.getId(), 1);
        if (result != null) {
            return result;
        }

        traceTitle("  Second Operands:");
        result = writeOpndRecords(opnds2, actSpec.getId(), 2);
        if (result != null) {
            return result;
        }

        traceTitle("  Third Operands:");
        result = writeOpndRecords(opnds3, actSpec.getId(), 3);
        if (result != null) {
            return result;
        }
        return null;
    }

    public String writeCondOpndRecords(Set<List<OpndSpec>> opnds, String actionId, int opNum) {
        return writeCondOpndRecords(opnds, Boolean.FALSE, actionId, opNum);
    }

    public String writeCondOpndRecords(Set<List<OpndSpec>> opnds, Boolean simple, String conditionId, int opNum) {
        Iterator<List<OpndSpec>> hsiter = opnds.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            traceTitle("    Operand with id = " + opndVec.get(0).getId());
            for (int i = 0; i < opndVec.size(); i++) {
                OpndSpec os = opndVec.get(i);
                String sOpndId = os.getId();
                traceTitle("      Item " + i);
                traceLine("      id       = " + sOpndId);
                traceLine("      type     = " + os.getType());
                traceLine("      origName = " + os.getOrigName());
                traceLine("      function = " + os.isFunction());
                traceLine("      subgraph = " + os.isSubgraph());
                traceLine("      compl    = " + os.isComplement());
                traceLine("      parent   = " + os.getParent());
                List<Integer> children = os.getChildren();
                assert (!simple || (children.isEmpty()));
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < children.size(); j++) {
                    int child = children.get(j);
                    if (j > 0) {
                        sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
                    }
                    sb.append(opndVec.get(child).getId());
                }
                String sArgs = sb.toString();
                if (!simple) {
                    traceLine("      origId   = " + os.getOrigId());
                    traceLine("      args     = " + sArgs);
                }

                try {
                    if(!operandExists(sOpndId)){
                        insert(WRITE_COND_OPERAND_RECORD, sOpndId, os.getType(), opNum, i, os.isFunction() ? 1 : 0,
                                os.isSubgraph() ? 1 : 0, os.isComplement() ? 1 : 0, os.getOrigName(),
                                os.getOrigId(), conditionId);
                    }else{
                        //operand is already in there, now update all fields
                        update(UPDATE_COND_OPERAND, os.getType(), opNum, i, os.isFunction() ? 1 : 0,
                                os.isSubgraph() ? 1 : 0, os.isComplement() ? 1 : 0, os.getOrigName(),
                                os.getOrigId(), conditionId, sOpndId);
                    }

                    String[] pieces = sb.toString().split(GlobalConstants.PM_ALT_DELIM_PATTERN);
                    for (int j = 0; j < pieces.length; j++) {
                        String argId = pieces[j];
                        if(argId != null && argId.length() > 0) {
                            if (!operandExists(argId)) {
                                //write a new operand, just the operand_id
                                insert(INIT_OPERAND_RECORD, argId);
                            }
                            if (argId != null && argId.length() > 0) {
                                insert(WRITE_FUNCTION_ARGS, sOpndId, argId, j);
                            }
                        }
                    }
                    //traceLine("      args     = " + args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    // The argument is a HashSet. Each item is an OpndSpec containing
    // exactly one simple operand (e.g., an operation for a grant action).

    public String writeSimpleOpndRecords(Set<List<OpndSpec>> opndsSet, String actionId, int opNum) {
        return writeOpndRecords(opndsSet, Boolean.TRUE, actionId, opNum);
    }

    public String writeOpndRecords(Set<List<OpndSpec>> opnds, String actionId, int opNum) {
        return writeOpndRecords(opnds, Boolean.FALSE, actionId, opNum);
    }

    private boolean operandExists(String sOpndId) {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sOpndId);
        long ret = -1;
        try {
            ret = (Long)select(OPERAND_EXISTS, params).get(0).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return !(ret < 1);
    }

    public String writeOpndRecords(Set<List<OpndSpec>> opnds, Boolean simple, String actionId, int opNum) {
        Iterator<List<OpndSpec>> hsiter = opnds.iterator();
        while (hsiter.hasNext()) {
            List<OpndSpec> opndVec = hsiter.next();
            traceTitle("    Operand with id = " + opndVec.get(0).getId());
            for (int i = 0; i < opndVec.size(); i++) {
                OpndSpec os = opndVec.get(i);
                String sOpndId = os.getId();
                traceTitle("      Item " + i);
                traceLine("      id       = " + sOpndId);
                traceLine("      type     = " + os.getType());
                traceLine("      origName = " + os.getOrigName());
                traceLine("      function = " + os.isFunction());
                traceLine("      subgraph = " + os.isSubgraph());
                traceLine("      compl    = " + os.isComplement());
                traceLine("      parent   = " + os.getParent());
                List<Integer> children = os.getChildren();
                assert (!simple || (children.isEmpty()));
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < children.size(); j++) {
                    int child = children.get(j);
                    if (j > 0) {
                        sb.append(GlobalConstants.PM_ALT_FIELD_DELIM);
                    }
                    sb.append(opndVec.get(child).getId());
                }
                String sArgs = sb.toString();
                if (!simple) {
                    traceLine("      origId   = " + os.getOrigId());
                    traceLine("      args     = " + sArgs);
                }

                try {
                    if(!operandExists(sOpndId)){
                        insert(WRITE_OPERAND_RECORD, sOpndId, os.getType(), opNum, i, os.isFunction() ? 1 : 0,
                                os.isSubgraph() ? 1 : 0, os.isComplement() ? 1 : 0, os.getOrigName(),
                                os.getOrigId(), actionId);
                    }else{
                        //operand is already in there, now update all fields
                        update(UPDATE_OPERAND, os.getType(), opNum, i, os.isFunction() ? 1 : 0,
                                os.isSubgraph() ? 1 : 0, os.isComplement() ? 1 : 0, os.getOrigName(),
                                os.getOrigId(), actionId, sOpndId);
                    }

                    String[] pieces = sb.toString().split(GlobalConstants.PM_ALT_DELIM_PATTERN);
                    for (int j = 0; j < pieces.length; j++) {
                        String argId = pieces[j];
                        if(argId != null && argId.length() > 0) {
                            if (!operandExists(argId)) {
                                //write a new operand, just the operand_id
                                insert(INIT_OPERAND_RECORD, argId);
                            }
                            if (argId != null && argId.length() > 0) {
                                insert(WRITE_FUNCTION_ARGS, sOpndId, argId, j);
                            }
                        }
                    }
                    //traceLine("      args     = " + args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void traceTitle(String title) {
        System.out.println();
        System.out.println(title);
    }

    public void traceLine(String line) {
        System.out.println("  " + line);
    }

    public String selectBest(HashMap<String, Set<String>> selectable,
                             HashSet<String> remaining, HashSet<String> sm) throws Exception {
        // Walk through selectable's entries whose keys ARE ALSO IN sm.
        // Select that key (i.e., ua) which contributes the maximum number of
        // perms.
        int maxContrib = 0;
        String sSelectedUa = null;
        System.out.println("*    *Begin selectBest");
        ServerConfig.SQLDAO.printSet(remaining, GlobalConstants.PM_PERM,
                "*    *with remaining set:");
        Iterator<String> mapiter = selectable.keySet().iterator();
        while (mapiter.hasNext()) {
            String sUa = mapiter.next();
            if (!sm.contains(sUa)) {
                continue;
            }
            HashSet<String> set = new HashSet<String>(selectable.get(sUa));
            ServerConfig.SQLDAO.printSet(
                    set,
                    GlobalConstants.PM_PERM,
                    "*    *selectBest: attribute "
                            + ServerConfig.SQLDAO.getEntityName(sUa,
                            PM_NODE.UATTR.value) + " has contrib:");
            if (set.contains(GlobalConstants.PM_ANY_ANY)) {
                set = new HashSet<String>(remaining);
            } else if (remaining.contains(GlobalConstants.PM_ANY_ANY)) {
            } else {
                set.retainAll(remaining);
            }
            if (set.size() > maxContrib) {
                maxContrib = set.size();
                sSelectedUa = sUa;
            }
        }
        System.out.println("*    *End selectBest");
        return sSelectedUa;
    }

    private List<String> getRuleActions(String sRuleId){
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sRuleId);
        try{
            return extractStrings(select(GET_RULE_ACTIONS, params));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Packet applyRule(EventContext eventctx, String sRuleId) {
        //get all actions for rule sRuleId
        List<String> actions = getRuleActions(sRuleId);
        for(String sActionId : actions){
            System.out.println("......Applying action " + sActionId + "!");
            Packet res = applyAction(eventctx, sActionId);

            // Even when applying the action results in a failure, we may
            // want
            // to continue.
            if (res.hasError()) {
                System.out.println("ERROR: " + res.getErrorMessage());
            }
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    private String getActionType(String sActionId){
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sActionId);
        try{
            return extractStrings(select(GET_ACTION_TYPE, params)).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Packet applyAction(EventContext eventctx, String sActionId) {
        try {
            //TODO check condition
            String sAct = getActionType(sActionId);//(String) actAttrs.get("pmType").get();
            if(sAct == null){
                return failurePacket("Unknown action type " + sAct);
            }else if (sAct.equals("assign")) {
                return applyActionAssign(eventctx, sActionId);
            } else if (sAct.equals("assign like")) {
                return applyActionAssignLike(eventctx, sActionId);
            } else if (sAct.equals("grant")) {
                return applyActionGrant(eventctx, sActionId);
            } else if (sAct.equals("create")) {
                return applyActionCreate(eventctx, sActionId);
            } else if (sAct.equals("deny")) {
                return applyActionDeny(eventctx, sActionId);
            } else if (sAct.equals("delete assignment")) {
                return applyActionDeassign(eventctx, sActionId);
            } else if (sAct.equals("delete deny")) {
                return applyActionDeleteDeny(eventctx, sActionId);
            } else if (sAct.equals("delete rule")) {
                return applyActionDeleteRule(eventctx, sActionId);
            } else {
                return failurePacket("Unknown action type " + sAct);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while dispatching event action");
        }
    }

    private List<String> getConditionOperands(String sContId){
        try{
            return extractStrings(select(GET_CONDITION_OPERANDS, sContId));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public CondSpec getConditionInfo(String sCondId){
        CondSpec condSpec = new CondSpec(sCondId);
        try{
            ArrayList<Object> condInfo = select(GET_CONDITION_INFO, sCondId).get(0);
            condSpec.setType((String) condInfo.get(0));
            condSpec.setNegated((Boolean) condInfo.get(1));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return condSpec;
    }

    // When checking conditions, retain all operands, even those that have
    // null name and/or id - this means that the operand looked for does not
    // exist.

    public Packet checkCondition(EventContext eventctx, String sCondId) {
        try {
            List<String> condOperands = getConditionOperands(sCondId);
            HashSet<ActOpnd> hsOpnd1 = new HashSet<ActOpnd>();
            if (condOperands != null) {
                for (String sOpndId : condOperands) {
                    // Get the runtime action operand and insert it into the HashSet
                    // of
                    // first operands. Most often, the run-time operand is the same
                    // as the
                    // compile-time operand. A function operand at run-time is
                    // different.
                    ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                    for (int i = 0; i < actOpnds.length; i++) {
                        hsOpnd1.add(actOpnds[i]);
                    }
                }
            }
            printOpndSet(hsOpnd1, "Set of first operands in condition");
            if (hsOpnd1.isEmpty()) {
                return failurePacket("No first operands in condition!?");
            }

            // To date, the only check implemented is whether the operand exists
            // (or
            // does not exist if the condition is negated).
            Iterator<ActOpnd> iter1 = hsOpnd1.iterator();
            CondSpec condSpec = getConditionInfo(sCondId);
            boolean isNegated = condSpec.isNegated();
            while (iter1.hasNext()) {
                if (!isNegated && !condOpndExists(iter1.next())) {
                    return failurePacket("Operand does not exist");
                } else if (isNegated && condOpndExists(iter1.next())) {
                    return failurePacket("Operand exists");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while dispatching event action");
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public boolean condOpndExists(ActOpnd opnd) throws Exception {
        String sName = opnd.getName();
        String sId = opnd.getId();
        String sType = opnd.getType();
        System.out.println("(((((((((((((Operand " + sName + ", " + sId + ", "
                + sType);
        if (sName == null) {
            return false;
        }
        if (sType == null) {
            return false;
        }
        if (ServerConfig.SQLDAO.getEntityId(sName, sType) == null) {
            return false;
        }
        return true;
    }

    public Packet applyActionDeleteRule(EventContext eventctx, String sActionId) {
        try {
            String sLastError = null;
            ActOpnd[] actOpnds = null;

            List<String> operandIds = getActionOperandsByNum(sActionId, 1);
            if (operandIds == null) {
                return failurePacket("No labels in \"Delete rule(s)\" action "
                        + sActionId);
            }
            List<ActOpnd> hsOpnd1 = new ArrayList<ActOpnd>();
            Collections.reverse(operandIds);
            for (String sOpndId : operandIds) {
                actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error when evaluating operand 1: "
                            + sLastError);
                    continue;
                }
                for (int i = 0; i < actOpnds.length; i++) {
                    hsOpnd1.add(actOpnds[i]);
                }
            }
            //printOpndSet(hsOpnd1, "Set of first operands in \"Delete rule(s)\"");
            if (hsOpnd1.isEmpty()) {
                return failurePacket("No first operands in \"Delete rule(s)\". Last error was: "
                        + sLastError);
            }
            for(ActOpnd a : hsOpnd1){
                System.out.println(a.getName() + ":" + a.getType());
            }

            // Delete the rules with the labels specified in hsOpnd1.

            for (ActOpnd opnd1 : hsOpnd1) {
                Packet res = deleteScriptRule(sEnabledScriptId, opnd1.getName());
                if (res.hasError()) {
                    sLastError = res.getErrorMessage();
                }
            }
            if (sLastError != null) {
                return failurePacket(sLastError);
            }

            return SQLPacketHandler.getSuccessPacket();
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while dispatching event action");
        }
    }

    public Packet applyActionDeassign(EventContext eventctx, String sActionId) {
        try {
            List<String> operandIds = getActionOperandsByNum(sActionId, 1);
            if (operandIds == null) {
                return failurePacket("No operand 1 in \"Delete assignment\" action");
            }
            List<ActOpnd> hsOpnd1 = new ArrayList<ActOpnd>();
            String sLastError = null;
            Collections.reverse(operandIds);
            for (String sOpndId : operandIds) {
                // Get the runtime action operand and insert it into the HashSet
                // of
                // first operands. Most often, the run-time operand is the same
                // as the
                // compile-time operand. A function operand at run-time is
                // different.
                ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error in evalOpnd was: "
                            + sLastError);
                    continue;
                }
                for (int i = 0; i < actOpnds.length; i++) {
                    hsOpnd1.add(actOpnds[i]);
                }
            }
            //printOpndSet(hsOpnd1,"Set of first operands in \"Delete assignment\"");
            if (hsOpnd1.isEmpty()) {
                System.out
                        .println("No first operands in deassign. Last error was: "
                                + sLastError);
                return failurePacket("No first operands in deassign. Last error was: "
                        + sLastError);
            }
            for(ActOpnd a : hsOpnd1){
                System.out.println(a.getName() + ":" + a.getType());
            }

            operandIds = getActionOperandsByNum(sActionId, 2);
            if (operandIds == null) {
                return failurePacket("No operand 2 in \"Delete assignment\" action");
            }
            List<ActOpnd> hsOpnd2 = new ArrayList<ActOpnd>();
            for (String sOpndId : operandIds) {
                ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error in evalOpnd was: "
                            + sLastError);
                    continue;
                }
                for (int i = 0; i < actOpnds.length; i++) {
                    hsOpnd2.add(actOpnds[i]);
                }
            }
            //printOpndSet(hsOpnd2, "Set of second operands in \"Delete assignment\"");
            if (hsOpnd2.isEmpty()) {
                System.out.println("No second operands in \"Delete assignment\". Last error was: "
                        + sLastError);
                return failurePacket("No second operands in \"Delete assignment\". Last error was: "
                        + sLastError);
            }
            for(ActOpnd a : hsOpnd2){
                System.out.println(a.getName() + ":" + a.getType());
            }

            // Deassign each operand1 from each operand2. Even if there is an
            // error,
            // try to perform as much as possible and then report.
            Iterator<ActOpnd> iter1 = hsOpnd1.iterator();
            Iterator<ActOpnd> iter2 = hsOpnd2.iterator();

            while (iter1.hasNext()) {
                ActOpnd opnd1 = iter1.next();
                while (iter2.hasNext()) {
                    ActOpnd opnd2 = iter2.next();
                    boolean result = ServerConfig.SQLDAO.deleteAssignment(Integer.valueOf(
                            opnd1.getId()), Integer.valueOf(opnd2.getId()));
                    if (!result) {
                        sLastError = ServerConfig.SQLDAO.getErrorMessage();
                    }
                }
            }
            if (sLastError != null) {
                return failurePacket(sLastError);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while dispatching event action");
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet applyActionDeleteDeny(EventContext eventctx, String sActionId) {
        try {
            ActionSpec action = getActionInfo(sActionId);
            boolean bIntrasession = action.isIntrasession();
            boolean bIntersection = action.isIntersection();

            List<String> operandIds = getActionOperandsByNum(sActionId, 1);

            String sLastError = null;
            ActOpnd actOpnd1 = null;
            ActOpnd[] actOpnds = null;
            String sOpndId = null;

            // "Delete deny" could have an empty set of first operands,
            // meaning any user.
            if (operandIds == null) {
                actOpnd1 = new ActOpnd("*", PM_NODE.USER.value, "*", false,
                        false, null);
            } else {
                // There should be only one first operand - a user or a user
                // attribute.
                sOpndId = operandIds.get(0);
                // Get the runtime action operand
                actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error when evaluating operand 1: "
                            + sLastError);
                    return failurePacket("No first operands in \"Delete deny\". Last error was: "
                            + sLastError);
                }
                actOpnd1 = actOpnds[0];
            }
            printOpnd(actOpnd1, "First operand in \"Delete deny\"");

            operandIds = getActionOperandsByNum(sActionId, 2);
            if (operandIds == null) {
                return failurePacket("No operand 2 in \"Delete deny\" action "
                        + sActionId);
            }
            List<ActOpnd> hsOpnd2 = new ArrayList<ActOpnd>();
            Collections.reverse(operandIds);
            for (String id : operandIds) {
                sOpndId = id;
                actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error when evaluating operand 2: "
                            + sLastError);
                    continue;
                }
                for (int i = 0; i < actOpnds.length; i++) {
                    hsOpnd2.add(actOpnds[i]);
                }
            }

            //printOpndSet(hsOpnd2, "Set of second operands in \"Delete deny\"");
            if (hsOpnd2.isEmpty()) {
                return failurePacket("No second operands in \"Deny\". Last error was: "
                        + sLastError);
            }
            for(ActOpnd a : hsOpnd2){
                System.out.println(a.getName() + ":" + a.getType());
            }

            operandIds = getActionOperandsByNum(sActionId, 3);
            if (operandIds == null) {
                return failurePacket("No operand 3 in \"Delete deny\" action "
                        + sActionId);
            }
            List<ActOpnd> hsOpnd3 = new ArrayList<ActOpnd>();
            Collections.reverse(operandIds);
            for (String id : operandIds) {
                sOpndId = id;
                actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error when evaluating operand 3: "
                            + sLastError);
                    continue;
                }
                for (int i = 0; i < actOpnds.length; i++) {
                    hsOpnd3.add(actOpnds[i]);
                }
            }

            //printOpndSet(hsOpnd3, "Set of third operands in \"Delete deny\"");
            if (hsOpnd3.isEmpty()) {
                return failurePacket("No third operands in \"Delete deny\". Last error was: "
                        + sLastError);
            }
            for(ActOpnd a : hsOpnd3){
                System.out.println(a.getName() + ":" + a.getType());
            }

            // Generate a name for the deny constraint.
            Random random = new Random();
            byte[] bytes = new byte[4];
            random.nextBytes(bytes);
            String sDenyName = UtilMethods.byteArray2HexString(bytes);
            System.out.println("============deny name is " + sDenyName);
            String sType = actOpnd1.getType();
            String sDenyType = null;
            if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
                sDenyType = GlobalConstants.PM_DENY_USER_ID;
            } else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                sDenyType = GlobalConstants.PM_DENY_ACROSS_SESSIONS;
                if (bIntrasession) {
                    sDenyType = GlobalConstants.PM_DENY_INTRA_SESSION;
                }
            } else {
                return failurePacket("Incorrect type for first \"Delete deny\" operand");
            }

            String sExistingDeny = getSimilarDeny(sDenyType,
                    actOpnd1.getName(), actOpnd1.getId(), bIntersection,
                    hsOpnd2, hsOpnd3);
            if (sExistingDeny == null) {
                return failurePacket("No such deny exists");
            }
            boolean deleteDeny = ServerConfig.SQLDAO.deleteDenyInternal(sExistingDeny, null, null,
                    null);
            if(deleteDeny){
                return getSuccessPacket();
            }else{
                return failurePacket("could not delete deny in applyActionDeleteDeny");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while dispatching event action");
        }
    }

    public Packet applyActionAssign(EventContext eventctx, String sActionId) {
        try {
            ActionSpec action = getActionInfo(sActionId);
            boolean bIntrasession = action.isIntrasession();
            boolean bIntersection = action.isIntersection();

            List<String> operandIds = getActionOperandsByNum(sActionId, 1);
            if (operandIds == null) {
                return failurePacket("No operand 1 in \"Assign\" action");
            }
            HashSet<ActOpnd> hsOpnd1 = new HashSet<ActOpnd>();
            String sLastError = null;
            for (String sOpndId : operandIds) {
                // Get the runtime action operand and insert it into the HashSet
                // of
                // first operands. Most often, the run-time operand is the same
                // as the
                // compile-time operand. A function operand at run-time is
                // different.
                ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error in evalOpnd was: "
                            + sLastError);
                    continue;
                }
                for (int i = 0; i < actOpnds.length; i++) {
                    hsOpnd1.add(actOpnds[i]);
                }
            }
            printOpndSet(hsOpnd1, "Set of first operands in \"Assign\"");
            if (hsOpnd1.isEmpty()) {
                System.out.println("No first operands in assign. Last error was: "
                        + sLastError);
                return failurePacket("No first operands in assign. Last error was: "
                        + sLastError);
            }

            operandIds = getActionOperandsByNum(sActionId, 2);
            if (operandIds == null) {
                return failurePacket("No operand 2 in \"Assign\" action");
            }
            HashSet<ActOpnd> hsOpnd2 = new HashSet<ActOpnd>();
            for (String sOpndId : operandIds) {
                ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error in evalOpnd was: "
                            + sLastError);
                    continue;
                }
                for (int i = 0; i < actOpnds.length; i++) {
                    hsOpnd2.add(actOpnds[i]);
                }
            }
            printOpndSet(hsOpnd2, "Set of second operands in \"Assign\"");
            if (hsOpnd2.isEmpty()) {
                System.out.println("No second operands in \"Assign\". Last error was: "
                        + sLastError);
                return failurePacket("No second operands in \"Assign\". Last error was: "
                        + sLastError);
            }

            // Assign each operand1 to each operand2. Even if there is an error,
            // try to perform as much as possible and then report.
            Iterator<ActOpnd> iter1 = hsOpnd1.iterator();
            Iterator<ActOpnd> iter2 = hsOpnd2.iterator();

            while (iter1.hasNext()) {
                ActOpnd opnd1 = iter1.next();
                while (iter2.hasNext()) {
                    ActOpnd opnd2 = iter2.next();
                    boolean res = ServerConfig.SQLDAO.assignInternal(opnd1.getId(),
                            opnd1.getType(), opnd2.getId(), opnd2.getType());
                    if (!res) {
                        sLastError = "could not complete assignment";
                    }
                }
            }
            if (sLastError != null) {
                return failurePacket(sLastError);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while dispatching event action");
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public void printOpndSet(HashSet<ActOpnd> opndSet, String caption) {
        Iterator<ActOpnd> iter = opndSet.iterator();

        System.out.println(caption);
        while (iter.hasNext()) {
            System.out.print("  Operand (");
            ActOpnd actOpnd = iter.next();
            System.out.print("name=" + actOpnd.getName());
            System.out.print(", type=" + actOpnd.getType());
            System.out.print(", id=" + actOpnd.getId());
            System.out.print(", err=" + actOpnd.getError());
            System.out.println(")");
        }
    }

    public void printOpnd(ActOpnd actOpnd, String caption) {
        System.out.println(caption);
        System.out.print("  Operand (");
        System.out.print("name=" + actOpnd.getName());
        System.out.print(", type=" + actOpnd.getType());
        System.out.print(", id=" + actOpnd.getId());
        System.out.print(", err=" + actOpnd.getError());
        System.out.println(")");
    }

    // Evaluate an action operand (an object of AD class pmClassOperand),
    // pointed to by sOpndId. It has the following attributes:
    // pmId, pmType, pmIsFunction, pmIsSubgraph, pmIsComplement,
    // pmOriginalName, pmOriginalId, pmArgs.
    // If the operand is function, pmArgs is a list of pointers to its arguments
    // (which are operands); pmOriginalName and pmOriginalId are the function's
    // name and id.
    // If it's not a function, the operand could be a PM entity, and then
    // the pmOriginalName and pmOriginalId are the name and id of that entity.
    // If it's not a PM entity, the operand could be a word, and then the
    // pmOriginalName is that word, and pmOriginalId should be ignored.
    // For example, the operand could be the name of an operation,
    // like "File write", or a property like "homeof=gigi".
    // The return value is an object of Java class ActOpnd, which contains
    // the name, type, and id of a PM entity, whether it represents a subgraph,
    // or if it's to be interpreted as the complement of a container.
    // It also contains an error message, which, if null, indicates successful
    // evaluation.
    // NOTE THAT THE RESULT OF AN EVALUATION IS ALWAYS AN ARRAY OF ActOpnd
    // objects.
    // From an operand which is argument of a function, we retain only the
    // first array component.

    private OpndSpec getOperandInfo(String sOpndId){
        try{
            ArrayList<Object> operandInfo = select(GET_OPERAND_INFO, sOpndId).get(0);
            //get_operand_type_name(operand_type), is_function, is_subgraph, is_compliment, expression, expression_id
            OpndSpec opSpec = new OpndSpec(sOpndId, null);
            opSpec.setType((String) operandInfo.get(0));
            opSpec.setIsFunction((Boolean) operandInfo.get(1));
            opSpec.setIsSubgraph((Boolean) operandInfo.get(2));
            opSpec.setIsComplement((Boolean) operandInfo.get(3));
            opSpec.setOrigName((String) operandInfo.get(4));
            opSpec.setOrigId((String) operandInfo.get(5));
            return opSpec;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> getFunctionArgs(String sFuncId){
        try {
            return extractStrings(select(GET_FUNCTION_ARGS, sFuncId));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ActOpnd[] evalOpnd(EventContext eventctx, String sOpndId) {
        System.out.println("EvalOpnd " + sOpndId);
        ActOpnd[] res = new ActOpnd[1];
        try {
            OpndSpec opSpec = getOperandInfo(sOpndId);
            boolean isFun = opSpec.isFunction();
            String sType = opSpec.getType();
            String sExpressionId = opSpec.getOrigId();
            if (sExpressionId == null && !sType.equalsIgnoreCase(GlobalConstants.PM_OP)
                    && !sType.equalsIgnoreCase(GlobalConstants.PM_LABEL)) {
                res[0] = new ActOpnd(null, null, null, false, false,
                        "Missing id of operand " + sOpndId);
                return res;
            }
            String sOrigId = sExpressionId;

            String sExpression = opSpec.getOrigName();
            if (sExpression == null) {
                res[0] = new ActOpnd(null, null, null, false, false,
                        "Missing name of operand " + sOpndId);
                return res;
            }
            String sOrigName = sExpression;

            boolean isSubgraph = opSpec.isSubgraph();
            boolean isComplement = opSpec.isComplement();

            // If the operand is not a function, return a record containing its
            // name,
            // type, id, isSubgraph, and a null error string.
            if (!isFun) {
                res[0] = new ActOpnd(sOrigName, sType, sOrigId, isSubgraph,
                        isComplement, null);
                return res;
            }

            // If the operand is a function, first evaluate its arguments, put
            // them
            // into a vector to preserve the order, then evaluate the function.
            // Be careful to put in the vector only the first component of the
            // array resulted from the evaluation of an argument.
            //in this case sOpnId is the id of a function and we want to get the operands
            //which are also in the operand table
            List<String> funArgIds = getFunctionArgs(sOpndId);
            Vector<ActOpnd> funArgs = new Vector<ActOpnd>();

            // Function without parameters.
            if (funArgIds == null || funArgIds.size() == 0) {
                res = evalFun(eventctx, sOrigName, sOrigId, sType, funArgs);
                for (int i = 0; i < res.length; i++) {
                    res[i].setSubgraph(isSubgraph);
                    res[i].setComplement(isComplement);
                }
                return res;
            }

            // Function with parameters. Its parameters' ids are
            // separated by "|".

            // Prepare the vector containing the arguments evaluated.
            // Be careful to insert into the vector only the first component of
            // the
            // array resulted from the evaluation of an argument.
            for (int i = 0; i < funArgIds.size(); i++) {
                String sArgId = funArgIds.get(i);
                res = evalOpnd(eventctx, sArgId);
                if (res[0].getError() != null) {
                    return res;
                }
                funArgs.addElement(res[0]);// !!!!!!!!!!!!!!!!!!!!!!!!??????????
            }

            res = evalFun(eventctx, sOrigName, sOrigId, sType, funArgs);
            for (int i = 0; i < res.length; i++) {
                res[i].setSubgraph(isSubgraph);
                res[i].setComplement(isComplement);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Exception during evaluation of operand " + sOpndId);
            return res;
        }
    }

    // Evaluate a function. funArgs is a vector containing the runtime function
    // arguments.

    public ActOpnd[] evalFun(EventContext eventctx, String sFunName,
                             String sFunId, String sFunType, Vector<ActOpnd> funArgs)
            throws Exception {
        System.out.println("Evaluating function " + sFunName);
        if(funArgs != null) {
            for (int i = 0; i < funArgs.size(); i++) {
                System.out.println("Argument id = " + funArgs.get(i).getId());
            }
        }

        // Dispatch the function evaluation to the correct method.
        if (sFunName.equalsIgnoreCase("object_new")) {
            return evalFun_object_new(eventctx, sFunType, funArgs);// 1116
        } else if (sFunName.equalsIgnoreCase("oattr_of_user_choice")) {
            return evalFun_oattr_of_user_choice(eventctx, sFunType, funArgs);// 1119
        } else if (sFunName.equalsIgnoreCase("oattr_of_default_user")) {
            return evalFun_oattr_of_default_user(eventctx, sFunType, funArgs);// 1120
        } else if (sFunName.equalsIgnoreCase("oattr_home_of_default_user")) {
            return evalFun_oattr_home_of_default_user(eventctx, sFunType,
                    funArgs);// 1121
        } else if (sFunName.equalsIgnoreCase("user_default")) {
            return evalFun_user_default(eventctx, sFunType, funArgs);// 1122
        } else if (sFunName.equalsIgnoreCase("prop_home_of_new_user")) {
            return evalFun_prop_home_of_new_user(eventctx, sFunType, funArgs);// 1123
        } else if (sFunName.equalsIgnoreCase("uattr_name_of_new_user")) {
            return evalFun_uattr_name_of_new_user(eventctx, sFunType, funArgs);// 1124
        } else if (sFunName.equalsIgnoreCase("prop_name_of_new_user")) {
            return evalFun_prop_name_of_new_user(eventctx, sFunType, funArgs);// 1125
        } else if (sFunName.equalsIgnoreCase("user_new")) {
            return evalFun_user_new(eventctx, sFunType, funArgs);// 1126
        } else if (sFunName.equalsIgnoreCase("uattr_name_of_user")) {
            return evalFun_uattr_name_of_user(eventctx, sFunType, funArgs);// 1127
        } else if (sFunName.equalsIgnoreCase("prop_name_of_user")) {
            return evalFun_prop_name_of_user(eventctx, sFunType, funArgs);// 1128
        } else if (sFunName.equalsIgnoreCase("pol_discr")) {
            return evalFun_pol_discr(eventctx, sFunType, funArgs);// 1129
        } else if (sFunName.equalsIgnoreCase("pol_id")) {
            return evalFun_pol_id(eventctx, sFunType, funArgs);// 1130
        } else if (sFunName.equalsIgnoreCase("pol_with_prop")) {
            return evalFun_pol_with_prop(eventctx, sFunType, funArgs);// 1131
        } else if (sFunName.equalsIgnoreCase("oattr_home_of_new_user")) {
            return evalFun_oattr_home_of_new_user(eventctx, sFunType, funArgs);// 1132
        } else if (sFunName.equalsIgnoreCase("oattr_home_of_user")) {
            return evalFun_oattr_home_of_user(eventctx, sFunType, funArgs);// 1133
        } else if (sFunName.equalsIgnoreCase("obj_rep_of_home_of_new_user")) {
            return evalFun_obj_rep_of_home_of_new_user(eventctx, sFunType,
                    funArgs);// 1134
        } else if (sFunName.equalsIgnoreCase("obj_rep_of_home_of_user")) {
            return evalFun_obj_rep_of_home_of_user(eventctx, sFunType, funArgs);// 1135
        } else if (sFunName.equalsIgnoreCase("oattr_rep_of_home_of_new_user")) {
            return evalFun_oattr_rep_of_home_of_new_user(eventctx, sFunType,
                    funArgs);// 1136
        } else if (sFunName.equalsIgnoreCase("oattr_rep_of_home_of_user")) {
            return evalFun_oattr_rep_of_home_of_user(eventctx, sFunType,
                    funArgs);// 1137
        } else if (sFunName.equalsIgnoreCase("oattr_rep_of_discr_users")) {
            return evalFun_oattr_rep_of_discr_users(eventctx, sFunType, funArgs);// 1138
        } else if (sFunName.equalsIgnoreCase("uattr_discr_users")) {
            return evalFun_uattr_discr_users(eventctx, sFunType, funArgs);// 1139
        } else if (sFunName.equalsIgnoreCase("oattr_of_default_obj")) {
            return evalFun_oattr_of_default_obj(eventctx, sFunType, funArgs);// 1140
        } else if (sFunName.equalsIgnoreCase("uattr_lowest_level")) {
            return evalFun_uattr_lowest_level(eventctx, sFunType, funArgs);// 1141
        } else if (sFunName
                .equalsIgnoreCase("oattr_direct_asc_of_and_containing")) {
            return evalFun_oattr_direct_asc_of_and_containing(eventctx,
                    sFunType, funArgs);// 1142
        } else if (sFunName.equalsIgnoreCase("uattr_direct_ascs_of_uattr")) {
            return evalFun_uattr_direct_ascs_of_uattr(eventctx, sFunType,
                    funArgs);// 1143
        } else if (sFunName
                .equalsIgnoreCase("uattr_direct_ascs_of_uattr_except")) {
            return evalFun_uattr_direct_ascs_of_uattr_except(eventctx,
                    sFunType, funArgs);// 1144
        } else if (sFunName.equalsIgnoreCase("prop_discr_users")) {
            return evalFun_prop_discr_users(eventctx, sFunType, funArgs);// 1146
        } else if (sFunName.equalsIgnoreCase("obj_rep_of_discr_users")) {
            return evalFun_obj_rep_of_discr_users(eventctx, sFunType, funArgs);// 1147
        } else if (sFunName.equalsIgnoreCase("oattr_witems_of_new_user")) {
            return evalFun_oattr_witems_of_new_user(eventctx, sFunType, funArgs);// 1148
        } else if (sFunName.equalsIgnoreCase("oattr_inbox_of_new_user")) {
            return evalFun_oattr_inbox_of_new_user(eventctx, sFunType, funArgs);// 1148
        } else if (sFunName.equalsIgnoreCase("oattr_winbox_of_new_user")) {
            return evalFun_oattr_winbox_of_new_user(eventctx, sFunType, funArgs);// 1148
        } else if (sFunName.equalsIgnoreCase("oattr_inbox_of_user")) {
            return evalFun_oattr_inbox_of_user(eventctx, sFunType, funArgs);// 1149
        } else if (sFunName.equalsIgnoreCase("oattr_winbox_of_user")) {
            return evalFun_oattr_winbox_of_user(eventctx, sFunType, funArgs);// 1149
        } else if (sFunName.equalsIgnoreCase("oattr_outbox_of_new_user")) {
            return evalFun_oattr_outbox_of_new_user(eventctx, sFunType, funArgs);// 1150
        } else if (sFunName.equalsIgnoreCase("oattr_outbox_of_user")) {
            return evalFun_oattr_outbox_of_user(eventctx, sFunType, funArgs);// 1151
        } else if (sFunName.equalsIgnoreCase("prop_witems_of_new_user")) {
            return evalFun_prop_witems_of_new_user(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("prop_inbox_of_new_user")) {
            return evalFun_prop_inbox_of_new_user(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("prop_inbox_of_user")) {
            return evalFun_prop_inbox_of_user(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("prop_outbox_of_new_user")) {
            return evalFun_prop_outbox_of_new_user(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("prop_outbox_of_user")) {
            return evalFun_prop_outbox_of_user(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("obj_rep_of_inbox_of_new_user")) {
            return evalFun_obj_rep_of_inbox_of_new_user(eventctx, sFunType,
                    funArgs);
        } else if (sFunName.equalsIgnoreCase("obj_rep_of_inbox_of_user")) {
            return evalFun_obj_rep_of_inbox_of_user(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_rep_of_inbox_of_new_user")) {
            return evalFun_oattr_rep_of_inbox_of_new_user(eventctx, sFunType,
                    funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_rep_of_inbox_of_user")) {
            return evalFun_oattr_rep_of_inbox_of_user(eventctx, sFunType,
                    funArgs);
        } else if (sFunName.equalsIgnoreCase("obj_rep_of_outbox_of_new_user")) {
            return evalFun_obj_rep_of_outbox_of_new_user(eventctx, sFunType,
                    funArgs);
        } else if (sFunName.equalsIgnoreCase("obj_rep_of_outbox_of_user")) {
            return evalFun_obj_rep_of_outbox_of_user(eventctx, sFunType,
                    funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_rep_of_outbox_of_new_user")) {
            return evalFun_oattr_rep_of_outbox_of_new_user(eventctx, sFunType,
                    funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_rep_of_outbox_of_user")) {
            return evalFun_oattr_rep_of_outbox_of_user(eventctx, sFunType,
                    funArgs);
        } else if (sFunName.equalsIgnoreCase("user_recipient")) {
            return evalFun_user_recipient(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_witems")) {
            return evalFun_oattr_witems(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_inboxes")) {
            return evalFun_oattr_inboxes(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_outboxes")) {
            return evalFun_oattr_outboxes(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("session_default")) {
            return evalFun_session_default(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("rule_composed_of")) {
            return evalFun_rule_composed_of(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("id_or_name_as_string")) {
            return evalFun_id_or_name_as_string(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("name_of_rep_of_oattr")) {
            return evalFun_name_of_rep_of_oattr(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_rep_of_oattr")) {
            return evalFun_oattr_rep_of_oattr(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("obj_rep_of_oattr")) {
            return evalFun_obj_rep_of_oattr(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_record_of_default_obj")) {
            return evalFun_oattr_record_of_default_obj(eventctx, sFunType,
                    funArgs);
        } else if (sFunName.equalsIgnoreCase("oattr_record_of_oattr")) {
            return evalFun_oattr_record_of_oattr(eventctx, sFunType, funArgs);
        } else if (sFunName.equalsIgnoreCase("process_default")) {
            return evalFun_process_default(eventctx, sFunType, funArgs);
        } else {
            ActOpnd[] err = new ActOpnd[1];
            err[0] = new ActOpnd(null, null, null, false, false, "Function "
                    + sFunName + " not implemented!");
            return err;
        }
    }

    // Returns the object attribute associated to an object that represents
    // another object attribute.

    public ActOpnd[] evalFun_oattr_rep_of_oattr(EventContext eventctx,
                                                String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_rep_of_oattr");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_rep_of_oattr is not an object attribute");
            return res;
        }
        String sOattrName = arg.getName();
        String sRepName = sOattrName + " rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        if (sRepId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_rep_of_oattr has no representative");
            return res;
        }
        res[0] = new ActOpnd(sRepName, PM_NODE.OATTR.value, sRepId, false,
                false, null);
        return res;
    }

    // Returns the object attribute associated to an object that represents
    // another object attribute.

    public ActOpnd[] evalFun_obj_rep_of_oattr(EventContext eventctx,
                                              String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_rep_of_oattr");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_rep_of_oattr is not an object attribute");
            return res;
        }
        String sOattrName = arg.getName();
        String sRepName = sOattrName + " rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        // No check
        res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false,
                false, null);
        return res;
    }

    // Returns the name of the object attribute associated to the object
    // that represents a specified object attribute.
    // Parameters: an object attribute.

    public ActOpnd[] evalFun_name_of_rep_of_oattr(EventContext eventctx,
                                                  String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function name_of_rep_of_oattr");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function name_of_rep_of_oattr is not an object attribute");
            return res;
        }
        String sOattrName = arg.getName();
        String sRepName = sOattrName + " rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName,
                GlobalConstants.PM_OBJ);
        // I didn't check existence because it ccould just being created.
        res[0] = new ActOpnd(sRepName, GlobalConstants.PM_UNKNOWN, sRepId,
                false, false, null);
        return res;
    }

    // If the argument is of any of the types a, u, p, c, b, ob, ses,
    // then return its id as string (i.e., return it in the name field and
    // with type k.
    // If the argument is of type k or rule, then return its name in the
    // name field and with type k.

    public ActOpnd[] evalFun_id_or_name_as_string(EventContext eventctx,
                                                  String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Wrong argument count in id_or_name_as_string()");
            return res;
        }

        ActOpnd arg0 = funArgs.get(0);
        String sT = arg0.getType();

        RandomGUID myGUID = new RandomGUID();
        String sId = myGUID.toStringNoDashes();

        if (sT.equals(GlobalConstants.PM_UNKNOWN)
                || sT.equals(GlobalConstants.PM_RULE)) {
            res[0] = new ActOpnd(arg0.getName(), GlobalConstants.PM_UNKNOWN,
                    sId, false, false, null);
        } else {
            res[0] = new ActOpnd(arg0.getId(), GlobalConstants.PM_UNKNOWN, sId,
                    false, false, null);
        }
        return res;
    }

    // This function has a variable number of arguments, but at least one.
    // This first argument (argument number 0) is the string representation
    // of an EVER rule, that could contain up to 9 macros #1,...,#n (n <= 9),
    // which will be replaced by the values of its subsequent arguments:
    // #1 by argument number 1, #2 by argument number 2, etc. If naa is the
    // number of actual arguments including the first (argument number 0),
    // then naa must be > n. After substitution, the first argument must be
    // compiled and added to the currently enabled script.

    public ActOpnd[] evalFun_rule_composed_of(EventContext eventctx,
                                              String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty()) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No arguments in rule_composed_of()");
            return res;
        }

        int nActualArgs = funArgs.size();
        ActOpnd arg0 = funArgs.get(0);
        System.out.println("rule_composed_of's arg 0 type is " + arg0.getType());
        System.out.println("rule_composed_of's arg 0 id is " + arg0.getId());
        System.out.println("rule_composed_of's arg 0 name is " + arg0.getName());

        String sOrigString = arg0.getName();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < sOrigString.length();) {
            char c = sOrigString.charAt(i++);
            if (c == '#') {
                if (i >= sOrigString.length()) {
                    res[0] = new ActOpnd(null, null, null, false, false,
                            "Incorrect argument reference in rule_composed_of()");
                    return res;
                }
                c = sOrigString.charAt(i++);
                if (c < '1' || c > '9') {
                    res[0] = new ActOpnd(null, null, null, false, false,
                            "Argument reference not in range 1..9 in rule_composed_of()");
                    return res;
                }
                int iArg = c - '0';
                if (iArg >= nActualArgs) {
                    res[0] = new ActOpnd(null, null, null, false, false,
                            "Argument reference out of the arguments range in rule_composed_of()");
                    return res;
                }

                String sRepl = funArgs.get(iArg).getName();
                sb.append(sRepl);
            } else {
                sb.append(c);
            }
        }

        RandomGUID myGUID = new RandomGUID();
        String sId = myGUID.toStringNoDashes();
        res[0] = new ActOpnd(sb.toString(), GlobalConstants.PM_RULE, sId,
                false, false, null);
        System.out.println("result type is " + res[0].getType());
        System.out.println("result id is " + res[0].getId());
        System.out.println("result name is " + res[0].getName());
        return res;
    }

    // The object_new() function returns an ActOpnd containing the object
    // attribute associated with the newly created object, if the event
    // who triggered its evaluation was "Object create". Otherwise, it
    // returns an error ActOpnd.
    // Parameters: none. It uses the event context to extract info.

    public ActOpnd[] evalFun_object_new(EventContext eventctx, String sFunType,
                                        Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        // The event context should contain the virtual object name and id,
        // and the function type is GlobalConstants.PM_OBJ. This function needs
        // to return the
        // object attribute associated to the virtual object.
        if (!eventctx.getEventName().equalsIgnoreCase(
                GlobalConstants.PM_EVENT_OBJECT_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_OBJECT_CREATE
                            + "\"!");
            return res;
        }
        String sObjId = eventctx.getObjId();
        if (sObjId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Null object id in the context");
            return res;
        }
        String sId = null;
        sId = ServerConfig.SQLDAO.getAssocOattr(sObjId);
        if (sId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute associated to object "
                            + eventctx.getObjId());
            return res;
        }
        res[0] = new ActOpnd(eventctx.getObjName(), PM_NODE.OATTR.value, sId,
                false, false, null);
        return res;
    }

    // The user_recipient() function returns an ActOpnd
    // containing the user to be recipient of an email message.
    // It is extracted from ctx1.
    // Parameters: None.

    public ActOpnd[] evalFun_user_recipient(EventContext eventctx,
                                            String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];
        String sUserName = eventctx.getctx1();
        String sUserId = ServerConfig.SQLDAO.getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user of name " + sUserName);
            return res;
        }

        res[0] = new ActOpnd(sUserName, PM_NODE.USER.value, sUserId, false,
                false, null);
        return res;
    }

    // The oattr_of_user_choice() function returns an ActOpnd
    // containing an object attribute selected by the user app, which is
    // contained in a given policy class.
    // Parameters: the policy class.

    public ActOpnd[] evalFun_oattr_of_user_choice(EventContext eventctx,
                                                  String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_of_user_choice");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.POL.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_of_user_choice is not a policy class");
            return res;
        }
        HashSet<String> selConts = UtilMethods.stringToSet(eventctx.getctx1());
        if (selConts.isEmpty()) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user-selected (oattr) containers!");
            return res;
        }
        Iterator<String> iter = selConts.iterator();
        while (iter.hasNext()) {
            String sOattrName = iter.next();
            String sOattrId = ServerConfig.SQLDAO.getEntityId(sOattrName,
                    PM_NODE.OATTR.value);
            if (sOattrId == null) {
                res[0] = new ActOpnd(null, null, null, false, false,
                        "No object attribute of name " + sOattrName);
                return res;
            }
            try {
                if (ServerConfig.SQLDAO.attrIsAscendantToPolicy(sOattrId, arg.getId())) {
                    res[0] = new ActOpnd(sOattrName, PM_NODE.OATTR.value,
                            sOattrId, false, false, null);
                    return res;
                }
            } catch (Exception e) {
                e.printStackTrace();
                res[0] = new ActOpnd(null, null, null, false, false,
                        "Exception in attrIsAscendantToPolicy() " + sOattrName);
            }
        }
        res[0] = new ActOpnd(null, null, null, false, false,
                "No user-selected container in policy class " + arg.getName());
        return res;
    }

    // Returns the object attribute with the property:
    // owner=<user>
    // where <user> is the user of the process that triggered the event.
    // Parameters: None.

    public ActOpnd[] evalFun_oattr_of_default_user(EventContext eventctx,
                                                   String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sSessId = eventctx.getSessId();
        String sUserId = ServerConfig.SQLDAO.getSessionUserId(sSessId);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user for session " + sSessId);
            return res;
        }
        String sUser = ServerConfig.SQLDAO.getEntityName(sUserId, PM_NODE.USER.value);
        if (sUser == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user with id " + sUserId);
            return res;
        }

        // Let's find the oattr with the property:
        String sProp = "owner=" + sUser;
        String sOattrId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.OATTR.value, sProp);
        System.out.println("=============oattr with property " + sProp
                + " found " + sOattrId);

        if (sOattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with property " + sProp);
            return res;
        }
        String sOattr = ServerConfig.SQLDAO.getEntityName(sOattrId,
                PM_NODE.OATTR.value);
        if (sOattr == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with id " + sOattrId);
            return res;
        }
        res[0] = new ActOpnd(sOattr, PM_NODE.OATTR.value, sOattrId, false,
                false, null);
        return res;
    }

    // Returns all direct ascendants of first argument excepting the second
    // argument. Both arguments are user attributes.

    public ActOpnd[] evalFun_uattr_direct_ascs_of_uattr_except(
            EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] err = new ActOpnd[1];
        if (funArgs.isEmpty() || funArgs.size() != 2) {
            err[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function uattr_direct_ascs_of_uattr_except");
            return err;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
            err[0] = new ActOpnd(
                    null,
                    null,
                    null,
                    false,
                    false,
                    "Argument 1 of function uattr_direct_ascs_of_uattr_except is not a user attribute");
            return err;
        }
        String sUattrId1 = arg.getId();
        arg = funArgs.get(1);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
            err[0] = new ActOpnd(
                    null,
                    null,
                    null,
                    false,
                    false,
                    "Argument 2 of function uattr_direct_ascs_of_uattr_except is not a user attribute");
            return err;
        }
        String sUattrId2 = arg.getId();

        // Get all direct ascendants of the first argument.
        try {
            ArrayList<Integer> fromAttrs = getFromAttrs(sUattrId1, null, DEPTH);
            if (fromAttrs == null) {
                err[0] = new ActOpnd(null, null, null, false, false,
                        "No direct ascendants found");
                return err;
            }
            // Let's see if the second argument is one of these ascendants.
            boolean isOne = false;
            for (Integer id : fromAttrs) {
                String sId = id.toString();
                if (sId.equalsIgnoreCase(sUattrId2)) {
                    isOne = true;
                    break;
                }
            }
            int n = fromAttrs.size();
            if (isOne) {
                n--;
            }
            if (n == 0) {
                err[0] = new ActOpnd(null, null, null, false, false,
                        "No ascendants as requested found");
                return err;
            }
            ActOpnd[] result = new ActOpnd[n];
            int i = 0;
            for (Integer id : fromAttrs) {
                String sId = id.toString();
                if (sId.equalsIgnoreCase(sUattrId2)) {
                    continue;
                }
                result[i++] = new ActOpnd(ServerConfig.SQLDAO.getEntityName(sId,
                        PM_NODE.UATTR.value), PM_NODE.UATTR.value, sId, false,
                        false, null);
            }
            return result;
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            err[0] = new ActOpnd(null, null, null, false, false, "Exception: "
                    + e.getMessage());
            return err;
        }
    }

    public ActOpnd[] evalFun_uattr_direct_ascs_of_uattr(EventContext eventctx,
                                                        String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] err = new ActOpnd[1];
        if (funArgs.isEmpty() || funArgs.size() != 1) {
            err[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function uattr_direct_ascs_of_uattr");
            return err;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.UATTR.value)) {
            err[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function uattr_direct_ascs_of_uattr is not a user attribute");
            return err;
        }
        String sUattrId = arg.getId();
        // Get all direct ascendants of the first argument.
        try {
            ArrayList<Integer> fromAttrs = getFromAttrs(sUattrId, null, DEPTH);
            if (fromAttrs == null) {
                err[0] = new ActOpnd(null, null, null, false, false,
                        "No direct ascendants found");
                return err;
            }
            int n = fromAttrs.size();
            ActOpnd[] result = new ActOpnd[n];
            int i = 0;
            for (Integer id : fromAttrs) {
                String sId = id.toString();
                result[i++] = new ActOpnd(ServerConfig.SQLDAO.getEntityId(sId,
                        PM_NODE.UATTR.value), PM_NODE.UATTR.value, sId, false,
                        false, null);
            }
            return result;
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            err[0] = new ActOpnd(null, null, null, false, false, "Exception: "
                    + e.getMessage());
            return err;
        }
    }

    // Returns an object attribute oa with the property:
    // oa -> oattr1 and oattr2 ->* oa, where oattr1 and oattr2 are its two
    // parameters. It assumes that oa is unique.
    // Parameters: two object attributes oattr1 and oattr2 such that
    // there is a unique oa such that oattr2 ->* oa -> oattr1.

    public ActOpnd[] evalFun_oattr_direct_asc_of_and_containing(
            EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 2) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_direct_asc_of_and_containing");
            return res;
        }
        ActOpnd arg1 = funArgs.get(0);
        if (!arg1.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
            res[0] = new ActOpnd(
                    null,
                    null,
                    null,
                    false,
                    false,
                    "First argument of function oattr_direct_asc_of_and_containing is not an object attribute");
            return res;
        }
        String sOattrId1 = arg1.getId();
        System.out.println("First operand is " + arg1.getName());

        ActOpnd arg2 = funArgs.get(1);
        if (!arg2.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
            res[0] = new ActOpnd(
                    null,
                    null,
                    null,
                    false,
                    false,
                    "Second argument of function oattr_direct_asc_containing is not an object attribute");
            return res;
        }
        String sOattrId2 = arg2.getId();
        System.out.println("Second operand is " + arg2.getName());

        // Get all direct ascendants of the first argument.
        System.out.println("Walking thru all ascendants of the first argument");
        try {
            ArrayList<Integer> fromAttrs = getFromAttrs(sOattrId1, null, DEPTH);
            if (fromAttrs != null) {
                for (Integer id : fromAttrs) {
                    String sId = id.toString();
                    System.out.println("For direct ascendant " + sId);
                    if (ServerConfig.SQLDAO.attrIsAscendant(sOattrId2, sId)) {
                        res[0] = new ActOpnd(ServerConfig.SQLDAO.getEntityId(sId,
                                PM_NODE.OATTR.value), PM_NODE.OATTR.value, sId,
                                false, false, null);
                        return res;
                    }
                }
            }
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            res[0] = new ActOpnd(null, null, null, false, false, "Exception: "
                    + e.getMessage());
            return res;
        }
        res[0] = new ActOpnd(null, null, null, false, false,
                "No container satisfying the requirement was found");
        return res;
    }

    // Returns the object attribute associated with the event's object,
    // if the event is Object create, Object write, etc., but not User create,
    // etc.
    //
    // Note that when the event is "Object delete", the id of the associated
    // object attribute cannot be obtained using getAssocId(), because the
    // object no longer exists. Instead, the assoc id is passed to
    // processEvent() and all subsequent functions in ctx2. All containers
    // the deleted object is assigned to are passed in ctx1.
    // Parameters: None.

    public ActOpnd[] evalFun_oattr_of_default_obj(EventContext eventctx,
                                                  String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.startsWith("Object ")) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "The event is not \"Object ...\"!");
            return res;
        }
        String sObjName = eventctx.getObjName();
        String sObjId = eventctx.getObjId();

        // If the event is "Object delete", the assoc id is in ctx2.
        if (sEventName.equalsIgnoreCase("Object delete")) {
            res[0] = new ActOpnd(sObjName, PM_NODE.OATTR.value,
                    eventctx.getctx2(), false, false, null);
            return res;
        }
        String sActObjId = ServerConfig.SQLDAO.getEntityId(sObjName,
                GlobalConstants.PM_OBJ);
        if (sActObjId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No such object \"" + sObjName + "\"!");
            return res;
        }
        if (!sActObjId.equalsIgnoreCase(sObjId)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Context's object id is not the actual id!");
            return res;
        }
        String sAssocId = ServerConfig.SQLDAO.getAssocOattr(sObjId);
        if (sAssocId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Context's object has no associated object attribute!");
            return res;
        }
        res[0] = new ActOpnd(sObjName, PM_NODE.OATTR.value, sAssocId, false,
                false, null);
        return res;
    }

    // Returns the record (an object container) the default object is a member
    // of.
    // The event must be Object create, Object write, etc., but not User create,
    // etc.
    //
    // Note that when the event is "Object delete", the id of the associated
    // object attribute cannot be obtained using getAssocId(), because the
    // object no longer exists. Instead, the assoc id is passed to
    // processEvent() and all subsequent functions in ctx2. All containers
    // the deleted object is assigned to are passed in ctx1.
    // Parameters: None.

    public ActOpnd[] evalFun_oattr_record_of_default_obj(EventContext eventctx,
                                                         String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.startsWith("Object ")) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "The event is not \"Object ...\"!");
            return res;
        }
        String sObjName = eventctx.getObjName();

        String sAssocId = null;

        // If the event is "Object delete", the assoc id is in ctx2.
        if (sEventName.equalsIgnoreCase("Object delete")) {
            sAssocId = eventctx.getctx2();
        } else {
            sAssocId = ServerConfig.SQLDAO.getEntityId(sObjName, PM_NODE.OATTR.value);
        }
        if (sAssocId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Could not find an object attribute associated to object \""
                            + sObjName + "\"!");
            return res;
        }
        String sRecId = ServerConfig.SQLDAO.getRecordOf(sAssocId, PM_NODE.OATTR.value);
        if (sRecId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "Object "
                    + sObjName + " is not a field of a record!");
            return res;
        }

        String sRecName = ServerConfig.SQLDAO.getEntityName(sRecId,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sRecName, PM_NODE.OATTR.value, sRecId, false,
                false, null);
        return res;
    }

    public ActOpnd[] evalFun_oattr_record_of_oattr(EventContext eventctx,
                                                   String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];
        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_record_of_oattr");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.OATTR.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_record_of_oattr is not an object attribute");
            return res;
        }
        String sOattrId = arg.getId();
        if (sOattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_record_of_oattr has null id!");
            return res;
        }

        String sRecId = ServerConfig.SQLDAO.getRecordOf(sOattrId, PM_NODE.OATTR.value);
        if (sRecId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No record containing the argument " + sOattrId);
            return res;
        }

        String sRecName = ServerConfig.SQLDAO.getEntityName(sRecId,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sRecName, PM_NODE.OATTR.value, sRecId, false,
                false, null);
        return res;
    }

    // This function returns the property "homeof=<user>", where <user>
    // is the name of the user just created. The event
    // must be "Create user", the context object must be a user, and the
    // event object class must be "User".
    // Parameters: None.

    public ActOpnd[] evalFun_prop_home_of_new_user(EventContext eventctx,
                                                   String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO.getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        res[0] = new ActOpnd("homeof=" + sUserName, GlobalConstants.PM_UNKNOWN,
                null, false, false, null);
        return res;
    }

    public ActOpnd[] evalFun_prop_witems_of_new_user(EventContext eventctx,
                                                     String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO.getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        res[0] = new ActOpnd("witemsof=" + sUserName,
                GlobalConstants.PM_UNKNOWN, null, false, false, null);
        return res;
    }

    public ActOpnd[] evalFun_prop_inbox_of_new_user(EventContext eventctx,
                                                    String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        res[0] = new ActOpnd("inboxof=" + sUserName,
                GlobalConstants.PM_UNKNOWN, null, false, false, null);
        return res;
    }

    public ActOpnd[] evalFun_prop_inbox_of_user(EventContext eventctx,
                                                String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function prop_inbox_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function prop_inbox_of_user is not a user");
            return res;
        }
        String sProp = "inboxof=" + arg.getName();
        res[0] = new ActOpnd(sProp, GlobalConstants.PM_UNKNOWN, null, false,
                false, null);
        return res;
    }

    public ActOpnd[] evalFun_prop_outbox_of_new_user(EventContext eventctx,
                                                     String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        res[0] = new ActOpnd("outboxof=" + sUserName,
                GlobalConstants.PM_UNKNOWN, null, false, false, null);
        return res;
    }

    public ActOpnd[] evalFun_prop_outbox_of_user(EventContext eventctx,
                                                 String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function prop_outbox_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function prop_outbox_of_user is not a user");
            return res;
        }
        String sProp = "outboxof=" + arg.getName();
        res[0] = new ActOpnd(sProp, GlobalConstants.PM_UNKNOWN, null, false,
                false, null);
        return res;
    }

    // This function returns the property "nameof=<user>", where <user>
    // is the name of the user just created. The event
    // must be "Create user", the context object must be a user, and the
    // event object class must be "User".
    // Parameters: None.

    public ActOpnd[] evalFun_prop_name_of_new_user(EventContext eventctx,
                                                   String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        res[0] = new ActOpnd("nameof=" + sUserName, GlobalConstants.PM_UNKNOWN,
                null, false, false, null);
        return res;
    }

    // Return the new user when the event is "User create".
    // Parameters: None.

    public ActOpnd[] evalFun_user_new(EventContext eventctx, String sFunType,
                                      Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        res[0] = new ActOpnd(sUserName, PM_NODE.USER.value, sUserId, false,
                false, null);
        return res;
    }

    // Returns the object attribute with the property homeof=<user>,
    // where <user> is the user of the current session.
    // Parameters: None.

    public ActOpnd[] evalFun_oattr_home_of_default_user(EventContext eventctx,
                                                        String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sSessId = eventctx.getSessId();
        String sUserId = ServerConfig.SQLDAO.getSessionUserId(sSessId);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user for session " + sSessId);
            return res;
        }
        String sUser = ServerConfig.SQLDAO.getEntityName(sUserId, PM_NODE.USER.value);
        if (sUser == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user with id " + sUserId);
            return res;
        }
        // Let's find the oattr with the property:
        String sProp = "homeof=" + sUser;
        String sOattrId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.OATTR.value, sProp);
        System.out.println("=============oattr with property " + sProp
                + " found " + sOattrId);

        if (sOattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with property " + sProp);
            return res;
        }
        String sOattr = ServerConfig.SQLDAO.getEntityName(sOattrId,
                PM_NODE.OATTR.value);
        if (sOattr == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with id " + sOattrId);
            return res;
        }
        res[0] = new ActOpnd(sOattr, PM_NODE.OATTR.value, sOattrId, false,
                false, null);
        return res;
    }

    public ActOpnd[] evalFun_oattr_outboxes(EventContext eventctx,
                                            String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        // Let's find the oattr with the property:
        String sProp = "containerof=outboxes";
        String sOattrId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.OATTR.value, sProp);
        if (sOattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with property " + sProp);
            return res;
        }
        String sOattr = ServerConfig.SQLDAO.getEntityName(sOattrId,
                PM_NODE.OATTR.value);
        if (sOattr == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with id " + sOattrId);
            return res;
        }
        res[0] = new ActOpnd(sOattr, PM_NODE.OATTR.value, sOattrId, false,
                false, null);
        return res;
    }

    public ActOpnd[] evalFun_oattr_inboxes(EventContext eventctx,
                                           String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        // Let's find the oattr with the property:
        String sProp = "containerof=inboxes";
        String sOattrId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.OATTR.value, sProp);
        if (sOattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with property " + sProp);
            return res;
        }
        String sOattr = ServerConfig.SQLDAO.getEntityName(sOattrId,
                PM_NODE.OATTR.value);
        if (sOattr == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with id " + sOattrId);
            return res;
        }
        res[0] = new ActOpnd(sOattr, PM_NODE.OATTR.value, sOattrId, false,
                false, null);
        return res;
    }

    public ActOpnd[] evalFun_oattr_witems(EventContext eventctx,
                                          String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        // Let's find the oattr with the property:
        String sProp = "containerof=witems";
        String sOattrId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.OATTR.value, sProp);
        if (sOattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with property " + sProp);
            return res;
        }
        String sOattr = ServerConfig.SQLDAO.getEntityName(sOattrId,
                PM_NODE.OATTR.value);
        if (sOattr == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute with id " + sOattrId);
            return res;
        }
        res[0] = new ActOpnd(sOattr, PM_NODE.OATTR.value, sOattrId, false,
                false, null);
        return res;
    }

    public ActOpnd[] evalFun_oattr_inbox_of_user(EventContext eventctx,
                                                 String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_inbox_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_inbox_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sInboxName = sUserName + " INBOX";
        String sInboxId = ServerConfig.SQLDAO.getEntityId(sInboxName,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sInboxName, PM_NODE.OATTR.value, sInboxId, false,
                false, null);
        return res;
    }

    public ActOpnd[] evalFun_oattr_winbox_of_user(EventContext eventctx,
                                                  String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_winbox_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_winbox_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sInboxName = sUserName + " wINBOX";
        String sInboxId = ServerConfig.SQLDAO.getEntityId(sInboxName,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sInboxName, PM_NODE.OATTR.value, sInboxId, false,
                false, null);
        return res;
    }

    public ActOpnd[] evalFun_oattr_outbox_of_new_user(EventContext eventctx,
                                                      String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sOutboxName = sUserName + " OUTBOX";
        String sOutboxId = ServerConfig.SQLDAO.getEntityId(sOutboxName,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sOutboxName, PM_NODE.OATTR.value, sOutboxId,
                false, false, null);
        return res;
    }

    public ActOpnd[] evalFun_oattr_outbox_of_user(EventContext eventctx,
                                                  String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_outbox_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_outbox_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sOutboxName = sUserName + " OUTBOX";
        String sOutboxId = ServerConfig.SQLDAO.getEntityId(sOutboxName,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sOutboxName, PM_NODE.OATTR.value, sOutboxId,
                false, false, null);
        return res;
    }

    public ActOpnd[] evalFun_oattr_witems_of_new_user(EventContext eventctx,
                                                      String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sInboxName = sUserName + " witems";
        String sInboxId = ServerConfig.SQLDAO.getEntityId(sInboxName,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sInboxName, PM_NODE.OATTR.value, sInboxId, false,
                false, null);
        return res;
    }

    // Returns the object attribute which will be the INBOX container of the
    // new user. Its name is "<user> INBOX",
    // where <user> is the new user. The event name must be "User create",
    // the object name must be the new user name, and the object class
    // must be "User".

    public ActOpnd[] evalFun_oattr_inbox_of_new_user(EventContext eventctx,
                                                     String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sInboxName = sUserName + " INBOX";
        String sInboxId = ServerConfig.SQLDAO.getEntityId(sInboxName,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sInboxName, PM_NODE.OATTR.value, sInboxId, false,
                false, null);
        return res;
    }

    // Returns the object attribute which will be the wINBOX container of the
    // new user. Its name is "<user> wINBOX",
    // where <user> is the new user. The event name must be "User create",
    // the object name must be the new user name, and the object class
    // must be "User".

    public ActOpnd[] evalFun_oattr_winbox_of_new_user(EventContext eventctx,
                                                      String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sInboxName = sUserName + " wINBOX";
        String sInboxId = ServerConfig.SQLDAO.getEntityId(sInboxName,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sInboxName, PM_NODE.OATTR.value, sInboxId, false,
                false, null);
        return res;
    }

    // Returns the object attribute which will be the home container of the
    // new user. Its name is "<user> home",
    // where <user> is the new user. The event name must be "User create",
    // the object name must be the new user name, and the object class
    // must be "User".
    // Parameters: None.

    public ActOpnd[] evalFun_oattr_home_of_new_user(EventContext eventctx,
                                                    String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sHomeName = sUserName + " home";
        String sHomeId = ServerConfig.SQLDAO.getEntityId(sHomeName,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sHomeName, PM_NODE.OATTR.value, sHomeId, false,
                false, null);
        return res;
    }

    // Returns the object attribute which is the home container of the
    // user passed as argument. Its name is "<user> home".
    // Parameters: A user.

    public ActOpnd[] evalFun_oattr_home_of_user(EventContext eventctx,
                                                String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_home_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_home_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sHomeName = sUserName + " home";
        String sHomeId = ServerConfig.SQLDAO.getEntityId(sHomeName,
                PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sHomeName, PM_NODE.OATTR.value, sHomeId, false,
                false, null);
        return res;
    }

    // Returns the object that represents the outbox container of the
    // new user. Its name is "<user> outbox rep",
    // where <user> is the new user. The event name must be "User create",
    // the object name must be the new user name, and the object class
    // must be "User".
    // Parameters: None.

    public ActOpnd[] evalFun_obj_rep_of_outbox_of_new_user(
            EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs)
            throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sRepName = sUserName + " outbox rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName,
                GlobalConstants.PM_OBJ);
        res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false,
                false, null);
        return res;
    }

    // Returns the object that represents the inbox container of the
    // new user. Its name is "<user> inbox rep",
    // where <user> is the new user. The event name must be "User create",
    // the object name must be the new user name, and the object class
    // must be "User".
    // Parameters: None.

    public ActOpnd[] evalFun_obj_rep_of_inbox_of_new_user(
            EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs)
            throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sRepName = sUserName + " inbox rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName,
                GlobalConstants.PM_OBJ);
        res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false,
                false, null);
        return res;
    }

    // Returns the object that represents the home container of the
    // new user. Its name is "<user> home rep",
    // where <user> is the new user. The event name must be "User create",
    // the object name must be the new user name, and the object class
    // must be "User".
    // Parameters: None.

    public ActOpnd[] evalFun_obj_rep_of_home_of_new_user(EventContext eventctx,
                                                         String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sRepName = sUserName + " home rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName,
                GlobalConstants.PM_OBJ);
        res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false,
                false, null);
        return res;
    }

    // Returns the object which represents the user attribute container
    // of the discretionary users, i.e., the user attribute with the
    // property "usersof=dicretionary". The name of this object is
    // <name of user attr container> + "rep".
    // Parameters: None.

    public ActOpnd[] evalFun_obj_rep_of_discr_users(EventContext eventctx,
                                                    String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        // Let's find the uattr with the property:
        String sProp = "usersof=discretionary";
        String sUattrId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.UATTR.value, sProp);
        System.out.println("=============uattr with property " + sProp
                + " found " + sUattrId);

        if (sUattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user attribute with property " + sProp);
            return res;
        }
        String sUattrName = ServerConfig.SQLDAO.getEntityName(sUattrId,
                PM_NODE.UATTR.value);
        if (sUattrName == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user attribute with id " + sUattrId);
            return res;
        }
        String sRepName = sUattrName + " rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        // We don't test sRepId, because it may not exist when we call this
        // function just to get the object created.
        res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false,
                false, null);
        return res;
    }

    // Returns the object which represents the outbox container of the
    // user passed as argument. Its name is "<user> outbox rep".
    // Parameters: a user.

    public ActOpnd[] evalFun_obj_rep_of_outbox_of_user(EventContext eventctx,
                                                       String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function obj_rep_of_outbox_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function obj_rep_of_outbox_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sRepName = sUserName + " outbox rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName,
                GlobalConstants.PM_OBJ);
        res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false,
                false, null);
        return res;
    }

    // Returns the object which represents the inbox container of the
    // user passed as argument. Its name is "<user> inbox rep".
    // Parameters: a user.

    public ActOpnd[] evalFun_obj_rep_of_inbox_of_user(EventContext eventctx,
                                                      String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function obj_rep_of_inbox_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function obj_rep_of_inbox_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sRepName = sUserName + " inbox rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName,
                GlobalConstants.PM_OBJ);
        res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false,
                false, null);
        return res;
    }

    // Returns the object which represents the home container of the
    // user passed as argument. Its name is "<user> home rep".
    // Parameters: a user.

    public ActOpnd[] evalFun_obj_rep_of_home_of_user(EventContext eventctx,
                                                     String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function obj_rep_of_home_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function obj_rep_of_home_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sRepName = sUserName + " home rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName,
                GlobalConstants.PM_OBJ);
        res[0] = new ActOpnd(sRepName, GlobalConstants.PM_OBJ, sRepId, false,
                false, null);
        return res;
    }

    // Like evalFun_obj_rep_of_outbox_of_new_user, but returns an object
    // attribute.
    // Parameters: None.

    public ActOpnd[] evalFun_oattr_rep_of_outbox_of_new_user(
            EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs)
            throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sRepName = sUserName + " outbox rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sRepName, PM_NODE.OATTR.value, sRepId, false,
                false, null);
        return res;
    }

    // Like evalFun_obj_rep_of_inbox_of_new_user, but returns an object
    // attribute.
    // Parameters: None.

    public ActOpnd[] evalFun_oattr_rep_of_inbox_of_new_user(
            EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs)
            throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sRepName = sUserName + " inbox rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sRepName, PM_NODE.OATTR.value, sRepId, false,
                false, null);
        return res;
    }

    // Like evalFun_obj_rep_of_home_of_new_user, but returns an object
    // attribute.
    // Parameters: None.

    public ActOpnd[] evalFun_oattr_rep_of_home_of_new_user(
            EventContext eventctx, String sFunType, Vector<ActOpnd> funArgs)
            throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "No user \""
                    + sUserName + "\"");
            return res;
        }
        String sRepName = sUserName + " home rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sRepName, PM_NODE.OATTR.value, sRepId, false,
                false, null);
        return res;
    }

    // Like evalFun_obj_rep_of_outbox_of_user, but returns an object attribute.
    // Parameters: a user.

    public ActOpnd[] evalFun_oattr_rep_of_outbox_of_user(EventContext eventctx,
                                                         String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_rep_of_outbox_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_rep_of_outbox_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sRepName = sUserName + " outbox rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sRepName, PM_NODE.OATTR.value, sRepId, false,
                false, null);
        return res;
    }

    // Like evalFun_obj_rep_of_inbox_of_user, but returns an object attribute.
    // Parameters: a user.

    public ActOpnd[] evalFun_oattr_rep_of_inbox_of_user(EventContext eventctx,
                                                        String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_rep_of_inbox_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_rep_of_inbox_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sRepName = sUserName + " inbox rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sRepName, PM_NODE.OATTR.value, sRepId, false,
                false, null);
        return res;
    }

    // Like evalFun_obj_rep_of_home_of_user, but returns an object attribute.
    // Parameters: a user.

    public ActOpnd[] evalFun_oattr_rep_of_home_of_user(EventContext eventctx,
                                                       String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function oattr_rep_of_home_of_user");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function oattr_rep_of_home_of_user is not a user");
            return res;
        }
        String sUserName = arg.getName();
        String sRepName = sUserName + " home rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        res[0] = new ActOpnd(sRepName, PM_NODE.OATTR.value, sRepId, false,
                false, null);
        return res;
    }

    // Returns the object that represents all discretionary users and
    // attributes.
    // First find the uattr with the property
    // "usersof=discretionary", then add " rep" to its name, and look for an
    // object attribute with this name associated to an object.
    // Parameters: None.

    public ActOpnd[] evalFun_oattr_rep_of_discr_users(EventContext eventctx,
                                                      String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        // Let's find the uattr with the property:
        String sProp = "usersof=discretionary";
        String sUattrId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.UATTR.value, sProp);
        System.out.println("=============uattr with property " + sProp
                + " found " + sUattrId);

        if (sUattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user attribute with property " + sProp);
            return res;
        }
        String sUattrName = ServerConfig.SQLDAO.getEntityName(sUattrId,
                PM_NODE.UATTR.value);
        if (sUattrName == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user attribute with id " + sUattrId);
            return res;
        }
        String sRepName = sUattrName + " rep";
        String sRepId = ServerConfig.SQLDAO.getEntityId(sRepName, PM_NODE.OATTR.value);
        if (sRepId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No object attribute " + sRepName);
            return res;
        }
        res[0] = new ActOpnd(sRepName, PM_NODE.OATTR.value, sRepId, false,
                false, null);
        return res;
    }

    // Returns the user attribute "DAC users".
    // Parameters: None.

    public ActOpnd[] evalFun_uattr_discr_users(EventContext eventctx,
                                               String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        // Let's find the uattr with the property:
        String sProp = "usersof=discretionary";
        String sUattrId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.UATTR.value, sProp);
        System.out.println("=============uattr with property " + sProp
                + " found " + sUattrId);
        if (sUattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user attribute with property " + sProp);
            return res;
        }
        String sUattrName = ServerConfig.SQLDAO.getEntityName(sUattrId,
                PM_NODE.UATTR.value);
        if (sUattrName == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user attribute with id " + sUattrId);
            return res;
        }
        res[0] = new ActOpnd(sUattrName, PM_NODE.UATTR.value, sUattrId, false,
                false, null);
        return res;
    }

    // Returns the default process, i.e., the one that triggered the event.
    // Parameters: None.

    public ActOpnd[] evalFun_process_default(EventContext eventctx,
                                             String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] res = new ActOpnd[1];

        String sProcId = eventctx.getProcId();
        if (sProcId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No process in the event context");
            return res;
        }
        // The process name (first arg in ActOpnd) is the same as the process
        // id.
        res[0] = new ActOpnd(sProcId, GlobalConstants.PM_PROCESS, sProcId,
                false, false, null);
        return res;
    }

    // Returns the default session, i.e., the one that triggered the event.
    // Parameters: None.

    public ActOpnd[] evalFun_session_default(EventContext eventctx,
                                             String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sSessId = eventctx.getSessId();
        if (sSessId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No session in the event context");
            return res;
        }
        String sSessName = ServerConfig.SQLDAO.getEntityName(sSessId,
                GlobalConstants.PM_SESSION);
        if (sSessName == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No session with id " + sSessId);
            return res;
        }
        res[0] = new ActOpnd(sSessName, GlobalConstants.PM_SESSION, sSessId,
                false, false, null);
        return res;
    }

    // Returns the default user, i.e., the session user.
    // Parameters: None.

    public ActOpnd[] evalFun_user_default(EventContext eventctx,
                                          String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sSessId = eventctx.getSessId();
        String sUserId = ServerConfig.SQLDAO.getSessionUserId(sSessId);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user for session " + sSessId);
            return res;
        }
        String sUserName = ServerConfig.SQLDAO
                .getEntityName(sUserId, PM_NODE.USER.value);
        if (sUserName == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user with id " + sUserId);
            return res;
        }
        res[0] = new ActOpnd(sUserName, PM_NODE.USER.value, sUserId, false,
                false, null);
        return res;
    }

    // Returns the name attribute of the new user. Its name is "<user> name",
    // where <user> is the new user. The event name must be "User create",
    // the object name must be the new user name, and the object class
    // must be "User".
    // Parameters: None.

    public ActOpnd[] evalFun_uattr_name_of_new_user(EventContext eventctx,
                                                    String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sEventName = eventctx.getEventName();
        if (!sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Event is not \"" + GlobalConstants.PM_EVENT_USER_CREATE
                            + "\"");
            return res;
        }
        String sUserName = eventctx.getObjName();
        String sClass = eventctx.getObjClass();
        if (!sClass.equalsIgnoreCase(GlobalConstants.PM_CLASS_USER_NAME)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Object class is not \"User\"");
            return res;
        }
        String sUserId = ServerConfig.SQLDAO
                .getEntityId(sUserName, PM_NODE.USER.value);
        if (sUserId == null) {
            res[0] = new ActOpnd(null, null, null, false, false, "User \""
                    + sUserName + "\" not yet created");
            return res;
        }
        String sAttrName = ServerConfig.SQLDAO.getUserFullName(sUserId);
        String sAttrId = ServerConfig.SQLDAO.getEntityId(sAttrName,
                PM_NODE.UATTR.value);
        // It's not an error to get a null id for the user name attribute.
        // It might not be created yet. But if it is already created, get the
        // id.
        res[0] = new ActOpnd(sAttrName, PM_NODE.UATTR.value, sAttrId, false,
                false, null);

        return res;
    }

    // Returns the name attribute of the user passed as argument,
    // i.e., the user attribute that has the full name of the user as name.
    // Parameters: a user.

    public ActOpnd[] evalFun_uattr_name_of_user(EventContext eventctx,
                                                String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function uattr_user_name");
            return res;
        }
        ActOpnd arg = funArgs.get(0);

        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "The argument of function uattr_user_name is not a user");
            return res;
        }
        String sAttrName = ServerConfig.SQLDAO.getUserFullName(arg.getId());
        String sAttrId = ServerConfig.SQLDAO.getEntityId(sAttrName,
                PM_NODE.UATTR.value);
        // We may get a null id for the attribute. This is not an error,
        // it might not be created yet. But if it's there, we need the id.
        res[0] = new ActOpnd(sAttrName, PM_NODE.UATTR.value, sAttrId, false,
                false, null);
        return res;
    }

    // Returns the property "nameof=<user>", where <user> is a user passed as
    // argument.
    // Parameters: a user.

    public ActOpnd[] evalFun_prop_name_of_user(EventContext eventctx,
                                               String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function prop_user_name");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        if (!arg.getType().equalsIgnoreCase(PM_NODE.USER.value)) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Argument of function uattr_user_name is not a user");
            return res;
        }
        String sProp = "nameof=" + arg.getName();
        res[0] = new ActOpnd(sProp, GlobalConstants.PM_UNKNOWN, null, false,
                false, null);
        return res;
    }

    // Returns the property "usersof=discretionary".
    // Parameters: None.

    public ActOpnd[] evalFun_prop_discr_users(EventContext eventctx,
                                              String sFunType, Vector<ActOpnd> funArgs) {
        ActOpnd[] res = new ActOpnd[1];
        res[0] = new ActOpnd("usersof=discretionary",
                GlobalConstants.PM_UNKNOWN, null, false, false, null);
        return res;
    }

    // Returns a policy with the property "type=discretionary", if one exists.
    // Parameters: None.

    public ActOpnd[] evalFun_pol_discr(EventContext eventctx, String sFunType,
                                       Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sProp = "type=discretionary";
        String sPolId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.POL.value, sProp);
        System.out.println("=============policy with property " + sProp
                + " found " + sPolId);
        if (sPolId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No policy with property " + sProp);
            return res;
        }
        String sPol = ServerConfig.SQLDAO.getEntityName(sPolId, PM_NODE.POL.value);
        if (sPol == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No policy with id " + sPolId);
            return res;
        }
        res[0] = new ActOpnd(sPol, PM_NODE.POL.value, sPolId, false, false,
                null);
        return res;
    }

    // Returns the user attribute representing the lowest clearance level of the
    // mls policy, if one exists.
    // Parameters: None.

    public ActOpnd[] evalFun_uattr_lowest_level(EventContext eventctx,
                                                String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sProp = "type=mls";
        String sPolId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.POL.value, sProp);
        System.out.println("=============policy with property " + sProp
                + " found " + sPolId);
        if (sPolId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No policy with property " + sProp);
            return res;
        }
        String sPolName = ServerConfig.SQLDAO.getEntityName(sPolId, PM_NODE.POL.value);
        if (sPolName == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No policy with id " + sPolId);
            return res;
        }
        String sPrefix = "levels=";
        String sLevelsProp = ServerConfig.SQLDAO.getPropertyWithPrefix(sPrefix,
                sPolId, PM_NODE.POL.value);
        if (sLevelsProp == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No levels specified in policy " + sPolName);
            return res;
        }
        String sLevels = sLevelsProp.substring(sPrefix.length());
        String[] pieces = sLevels.split(GlobalConstants.PM_LIST_MEMBER_SEP);
        if (pieces.length < 1 || pieces[0] == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No lowest level specified in policy " + sPolName);
            return res;
        }
        String sUattrId = ServerConfig.SQLDAO.getEntityId(pieces[0],
                PM_NODE.UATTR.value);
        if (sUattrId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No user attribute at lowest level " + pieces[0]);
            return res;
        }
        res[0] = new ActOpnd(pieces[0], PM_NODE.UATTR.value, sUattrId, false,
                false, null);
        return res;
    }

    // Returns a policy with the property "type=identity", if one exists.
    // Parameters: None.

    public ActOpnd[] evalFun_pol_id(EventContext eventctx, String sFunType,
                                    Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        String sProp = "type=identity";
        String sPolId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.POL.value, sProp);
        System.out.println("=============policy with property " + sProp
                + " found " + sPolId);
        if (sPolId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No policy with property " + sProp);
            return res;
        }
        String sPol = ServerConfig.SQLDAO.getEntityName(sPolId, PM_NODE.POL.value);
        if (sPol == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No policy with id " + sPolId);
            return res;
        }
        res[0] = new ActOpnd(sPol, PM_NODE.POL.value, sPolId, false, false,
                null);
        return res;
    }

    // Returns a policy with the property passed as the sole argument.
    // Parameters: A property.

    public ActOpnd[] evalFun_pol_with_prop(EventContext eventctx,
                                           String sFunType, Vector<ActOpnd> funArgs) throws Exception {
        ActOpnd[] res = new ActOpnd[1];

        if (funArgs.isEmpty() || funArgs.size() != 1) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "Incorrect number of arguments for function pol_with_prop");
            return res;
        }
        ActOpnd arg = funArgs.get(0);
        String sProp = arg.getName();
        String sPolId = ServerConfig.SQLDAO.getEntityWithPropInternal(
                PM_NODE.POL.value, sProp);
        System.out.println("=============policy with property " + sProp
                + " found " + sPolId);
        if (sPolId == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No policy with property " + sProp);
            return res;
        }
        String sPol = ServerConfig.SQLDAO.getEntityName(sPolId, PM_NODE.POL.value);
        if (sPol == null) {
            res[0] = new ActOpnd(null, null, null, false, false,
                    "No policy with id " + sPolId);
            return res;
        }
        res[0] = new ActOpnd(sPol, PM_NODE.POL.value, sPolId, false, false,
                null);
        return res;
    }

    public Packet applyActionAssignLike(EventContext eventctx, String sActionId) {
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet applyActionGrant(EventContext eventctx, String sActionId) {
        try {
            ActionSpec action = getActionInfo(sActionId);
            List<String> operandIds = getActionOperandsByNum(sActionId, 1);
            if (operandIds == null) {
                return failurePacket("No operand 1 in \"Grant\" action "
                        + sActionId);
            }
            List<ActOpnd> hsOpnd1 = new ArrayList<ActOpnd>();
            String sLastError = null;
            Collections.reverse(operandIds);
            for (String sOpndId : operandIds) {
                // Get the runtime action operand and insert it into the HashSet
                // of
                // first operands. Most of the times, the run-time operand is
                // the same as the
                // compile-time operand. A function operand at run-time is
                // different.
                ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error when evaluating operand 1: "
                            + sLastError);
                    continue;
                }
                for (int i = 0; i < actOpnds.length; i++) {
                    hsOpnd1.add(actOpnds[i]);
                }
            }
            //printOpndSet(hsOpnd1, "Set of first operands in \"Grant\"");
            for(ActOpnd a : hsOpnd1){
                System.out.println(a.getName() + ":" + a.getType());
            }
            if (hsOpnd1.isEmpty()) {
                return failurePacket("No first operands in grant. Last error was: "
                        + sLastError);
            }

            operandIds = getActionOperandsByNum(sActionId, 2);
            List<ActOpnd> hsOpnd2 = new ArrayList<ActOpnd>();
            if (operandIds != null) {
                Collections.reverse(operandIds);

                for (String sOpndId : operandIds) {
                    ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                    sLastError = actOpnds[0].getError();
                    if (sLastError != null) {
                        System.out.println("Last error when evaluating operand 2: "
                                + sLastError);
                        continue;
                    }
                    for (int i = 0; i < actOpnds.length; i++) {
                        hsOpnd2.add(actOpnds[i]);
                    }
                }
            }
            //printOpndSet(hsOpnd2, "Set of second operands in \"Grant\"");
            for(ActOpnd a : hsOpnd2){
                System.out.println(a.getName() + ":" + a.getType());
            }

            if (hsOpnd2.isEmpty()) {
                return failurePacket("No second operands in \"Grant\". Last error was: "
                        + sLastError);
            }

            operandIds = getActionOperandsByNum(sActionId, 3);
            List<ActOpnd> hsOpnd3 = new ArrayList<ActOpnd>();
            if (operandIds != null) {
                Collections.reverse(operandIds);

                for (String sOpndId : operandIds) {
                    ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                    sLastError = actOpnds[0].getError();
                    if (sLastError != null) {
                        System.out.println("Last error when evaluating operand 3: "
                                + sLastError);
                        continue;
                    }
                    for (int i = 0; i < actOpnds.length; i++) {
                        hsOpnd3.add(actOpnds[i]);
                    }
                }
            }
            //printOpndSet(hsOpnd3, "Set of third operands in \"Grant\"");
            if (hsOpnd3.isEmpty()) {
                return failurePacket("No third operands in \"Grant\". Last error was: "
                        + sLastError);
            }

            for(ActOpnd a : hsOpnd3){
                System.out.println(a.getName() + ":" + a.getType());
            }

            // Generate a name for the operation set.
            Random random = new Random();
            byte[] bytes = new byte[4];
            random.nextBytes(bytes);
            String sOpset = UtilMethods.byteArray2HexString(bytes);
            System.out.println("============opset name is " + sOpset);
            // Create an empty opset and assign it to connector.
            Integer res = ServerConfig.SQLDAO.addOpsetAndOpInternal(sOpset, null,
                    sOpset, sOpset, null, null, null, null, null);
            if (res== null) {
                return fail();
            }
            // Get the opset name and id from the result.
            //String sLine = res.getStringValue(0);
            //String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
            String sOpsetId = String.valueOf(res);
            // Add all the ops in operands 2 to the new opset.
            Iterator<ActOpnd> iter2 = hsOpnd2.iterator();
            while (iter2.hasNext()) {
                ActOpnd opnd2 = iter2.next();
                res = ServerConfig.SQLDAO.addOpsetAndOpInternal(sOpset, null, null,
                        null, opnd2.getName(), null, null, null, null);
                if (res== null) {
                    return fail();
                }
            }

            // Assign each operand1 to the opset.
            Iterator<ActOpnd> iter1 = hsOpnd1.iterator();
            while (iter1.hasNext()) {
                ActOpnd opnd1 = iter1.next();
                boolean bRes = ServerConfig.SQLDAO.assignInternal(opnd1.getId(),
                        opnd1.getType(), sOpsetId, PM_NODE.OPSET.value);
                if (!bRes) {
                    return fail();
                }
            }

            // Assign the opset to each operand3.
            Iterator<ActOpnd> iter3 = hsOpnd3.iterator();
            while (iter3.hasNext()) {
                ActOpnd opnd3 = iter3.next();
                boolean bRes = ServerConfig.SQLDAO.assignInternal(sOpsetId,
                        PM_NODE.OPSET.value, opnd3.getId(), opnd3.getType());
                if (!bRes) {
                    return fail();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while dispatching event action");
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    private ActionSpec getActionInfo(String sActionId){
        ActionSpec actSpec = new ActionSpec(sActionId);

        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.STRING, sActionId);
        try{
            ArrayList<Object> actionInfo = select(GET_ACTION_INFO, params).get(0);
            actSpec.setType((String) actionInfo.get(0));
            actSpec.setIsIntrasession((Boolean) actionInfo.get(1));
            actSpec.setIsIntersection((Boolean) actionInfo.get(2));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return actSpec;
    }

    private List<String> getActionOperandsByNum(String sActionId, int opNum){
        try{
            return extractStrings(select(GET_ACTION_OPERANDS, sActionId, opNum));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Packet applyActionDeny(EventContext eventctx, String sActionId) {
        try {
            ActionSpec action = getActionInfo(sActionId);
            boolean bIntrasession = action.isIntrasession();
            boolean bIntersection = action.isIntersection();

            List<String> operandIds = getActionOperandsByNum(sActionId, 1);
            if (operandIds == null) {
                return failurePacket("No operand 1 in \"Deny\" action "
                        + sActionId);
            }
            String sOpndId = operandIds.get(0);

            String sLastError = null;

            // There should be only one first operand - a session, a user or a
            // user attribute.
            // Get the runtime action operand
            ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
            sLastError = actOpnds[0].getError();
            if (sLastError != null) {
                System.out.println("Last error when evaluating operand 1: "
                        + sLastError);
                return failurePacket("No first operands in \"Deny\". Last error was: "
                        + sLastError);
            }
            ActOpnd actOpnd1 = actOpnds[0];
            printOpnd(actOpnd1, "First operand in \"Deny\"");

            operandIds = getActionOperandsByNum(sActionId, 2);
            if (operandIds == null) {
                return failurePacket("No operand 2 in \"Deny\" action "
                        + sActionId);
            }
            List<ActOpnd> hsOpnd2 = new ArrayList<ActOpnd>();
            Collections.reverse(operandIds);
            for (int i = 0; i < operandIds.size(); i++) {
                sOpndId = operandIds.get(i);
                actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error when evaluating operand 2: "
                            + sLastError);
                    continue;
                }
                for (int j = 0; j < actOpnds.length; j++) {
                    hsOpnd2.add(actOpnds[j]);
                }
            }
            //printOpndSet(hsOpnd2, "Set of second operands in \"Deny\"");
            if (hsOpnd2.isEmpty()) {
                return failurePacket("No second operands in \"Deny\". Last error was: "
                        + sLastError);
            }
            for(ActOpnd a : hsOpnd2){
                System.out.println(a.getName() + ":" + a.getType());
            }

            operandIds = getActionOperandsByNum(sActionId, 3);
            if (operandIds == null) {
                return failurePacket("No operand 3 in \"Deny\" action "
                        + sActionId);
            }
            List<ActOpnd> hsOpnd3 = new ArrayList<ActOpnd>();
            Collections.reverse(operandIds);
            for (int i = 0; i < operandIds.size(); i++) {
                sOpndId = operandIds.get(i);
                actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error when evaluating operand 3: "
                            + sLastError);
                    continue;
                }
                for (int j = 0; j < actOpnds.length; j++) {
                    hsOpnd3.add(actOpnds[j]);
                }
            }
            //printOpndSet(hsOpnd3, "Set of third operands in \"Deny\"");
            if (hsOpnd3.isEmpty()) {
                return failurePacket("No third operands in \"Deny\". Last error was: "
                        + sLastError);
            }
            for(ActOpnd a : hsOpnd2){
                System.out.println(a.getName() + ":" + a.getType());
            }

            // Generate a name for the deny constraint.
            Random random = new Random();
            byte[] bytes = new byte[4];
            random.nextBytes(bytes);
            String sDenyName = UtilMethods.byteArray2HexString(bytes);
            System.out.println("============deny name is " + sDenyName);
            String sType = actOpnd1.getType();
            String sDenyType = null;
            if (sType.equalsIgnoreCase(GlobalConstants.PM_SESSION)) {
                sDenyType = GlobalConstants.PM_DENY_SESSION;
            } else if (sType.equalsIgnoreCase(GlobalConstants.PM_PROCESS)) {
                sDenyType = GlobalConstants.PM_DENY_PROCESS;
            } else if (sType.equalsIgnoreCase(PM_NODE.USER.value)) {
                sDenyType = GlobalConstants.PM_DENY_USER_ID;
            } else if (sType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                sDenyType = GlobalConstants.PM_DENY_ACROSS_SESSIONS;
                if (bIntrasession) {
                    sDenyType = GlobalConstants.PM_DENY_INTRA_SESSION;
                }
            } else {
                return failurePacket("Incorrect type for first \"Deny\" operand");
            }

            String sSimDeny = getSimilarDeny(sDenyType, actOpnd1.getName(),
                    actOpnd1.getId(), bIntersection, hsOpnd2, hsOpnd3);
            if (sSimDeny != null) {
                System.out.println("A similar deny constraint exists.");
                return SQLPacketHandler.getSuccessPacket();
            }

            // Create a deny constraint of the correct class/type.
            System.out.println("Creating the deny relation");
            Integer res = ServerConfig.SQLDAO.addDenyInternal(sDenyName, sDenyType,
                    actOpnd1.getName(), actOpnd1.getId(), null, null, null,
                    bIntersection);
            if (res == null) {
                return fail();
            }

            // Get the deny name and id from the result.
            //String sLine = res.getStringValue(0);
            //String[] pieces = sLine.split(GlobalConstants.PM_FIELD_DELIM);
            //sDenyName = pieces[0];
            sDenyName = getEntityName(res.toString(), GlobalConstants.PM_DENY);

            // Add all the ops in operands 2 to the new deny constraint.
            System.out.println("Adding the operations to the deny relation");
            Iterator<ActOpnd> iter2 = hsOpnd2.iterator();
            while (iter2.hasNext()) {
                ActOpnd opnd2 = iter2.next();
                res = ServerConfig.SQLDAO.addDenyInternal(sDenyName, null, null, null,
                        opnd2.getName(), null, null, bIntersection);
                if (res == null) {
                    fail();
                }
            }

            // Add all the containers (object attributes) to the new deny
            // constraint.
            System.out.println("Adding the containers to the deny relation");
            Iterator<ActOpnd> iter3 = hsOpnd3.iterator();
            while (iter3.hasNext()) {
                ActOpnd opnd3 = iter3.next();
                String sContName;
                if (opnd3.isComplement()) {
                    sContName = "!" + opnd3.getName();
                } else {
                    sContName = opnd3.getName();
                }
                res = ServerConfig.SQLDAO.addDenyInternal(sDenyName, null, null, null,
                        null, sContName, opnd3.getId(), bIntersection);
                if (res == null) {
                    fail();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while dispatching event action");
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    // Returns the id of a similar deny.
    // called only from Obligation
    public String getSimilarDeny(String sDenyType, String sOrigName,
                                 String sOrigId, boolean bInters, List<ActOpnd> hsOpnd2,
                                 List<ActOpnd> hsOpnd3) {

        // The op set of an existing deny.
        HashSet<String> hsCrtOps = new HashSet<String>();

        // The op set of the deny we want to add.
        HashSet<String> hsOps = new HashSet<String>();
        Iterator<ActOpnd> iter = hsOpnd2.iterator();
        while (iter.hasNext()) {
            String sOp = iter.next().getName();
            hsOps.add(sOp);
        }

        // The container set of an existing deny.
        HashSet<String> hsCrtConts = new HashSet<String>();

        // The container set of the deny we want to add.
        HashSet<String> hsConts = new HashSet<String>();
        iter = hsOpnd3.iterator();
        while (iter.hasNext()) {
            ActOpnd opnd = iter.next();
            String sContId;
            if (opnd.isComplement()) {
                sContId = "!" + opnd.getId();
            } else {
                sContId = opnd.getId();
            }
            hsConts.add(sContId);
        }
        try{
            Packet p = ServerConfig.SQLDAO.getDenies(null);
            ArrayList<Integer> denyIds = new ArrayList<Integer>();
            if (p != null) {
                for (int i = 0; i < p.size(); i++) {
                    denyIds.add(Integer.valueOf(p.getItemStringValue(i).split(
                            GlobalConstants.PM_FIELD_DELIM)[1]));
                }
            }
            for (Integer id : denyIds) {
                // Get the id of the current deny constraint.
                String sCrtDenyId = id.toString();

                Packet denyInfo = ServerConfig.SQLDAO.getDenyInfo(null, sCrtDenyId);

                String[] basicInfo = denyInfo.getItemStringValue(1).split(
                        GlobalConstants.PM_FIELD_DELIM);

                String sCrtDenyType = basicInfo[0];
                // Check the type
                System.out.println("Compare " + sDenyType + " to "
                        + sCrtDenyType);
                if (!sCrtDenyType.equalsIgnoreCase(sDenyType)) {
                    continue;
                }
                // Check if both are intersections or both unions.
                boolean bCrtInters = basicInfo[3].equalsIgnoreCase("TRUE");
                System.out.println("Compare " + bInters + " to " + bCrtInters);
                if (bCrtInters != bInters) {
                    continue;
                }
                // Check the user or attribute id.
                String sCrtId = basicInfo[2];
                System.out.println("Compare " + sOrigId + " to " + sCrtId);
                if (!sOrigId.equalsIgnoreCase(sCrtId)) {
                    continue;
                }
                // Check the operations.
                hsCrtOps.clear();
                int opCount = Integer.valueOf(denyInfo.getItemStringValue(2));
                System.out.println("PACKET denyInfo:");
                for(int i = 0; i < denyInfo.size(); i++){
                    System.out.println(i + ": " + denyInfo.getItemStringValue(i));
                }
                if (opCount > 0) {
                    for (int i = 3; i < 3+opCount; i++) {
                        hsCrtOps.add(denyInfo.getItemStringValue(i));
                    }
                }
                ServerConfig.SQLDAO.printSet(hsOps, GlobalConstants.PM_PERM,
                        "Compare operations");
                ServerConfig.SQLDAO.printSet(hsCrtOps, GlobalConstants.PM_PERM,
                        "with crt. operations");
                if (!hsOps.containsAll(hsCrtOps)) {
                    continue;
                }
                if (!hsCrtOps.containsAll(hsOps)) {
                    continue;
                }

                // Check the containers.
                hsCrtConts.clear();
                int contCount = Integer.valueOf(denyInfo
                        .getItemStringValue(3 + opCount));
                if (contCount > 0) {
                    for (int i = 3 + opCount + 1; i < 3 + opCount + 1
                            + contCount; i++) {
                        hsCrtConts.add(denyInfo.getItemStringValue(i));
                    }

                }
                ServerConfig.SQLDAO.printSet(hsConts, GlobalConstants.PM_PERM,
                        "Compare containers");
                ServerConfig.SQLDAO.printSet(hsCrtConts, GlobalConstants.PM_PERM,
                        "with crt. containers");
                if (!hsConts.containsAll(hsCrtConts)) {
                    continue;
                }
                if (!hsCrtConts.containsAll(hsConts)) {
                    continue;
                }
                return sCrtDenyId;
            }
            return null;
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Apply a "create" action. First evaluate the operands, because the
    // run-time value of an operand could be different from the compile-time
    // value (when the operand is a function).
    // The first operand specifies the entity to be created (usually name and
    // type).
    // The second operand specifies the PM entity represented by the entity to
    // be
    // created if that entity is an object, or is empty.
    // The third operand specifies the properties of the entity to be created,
    // or is empty.
    // The fourth operand specifies the container where the entity to be created
    // should reside.

    public Packet applyActionCreate(EventContext eventctx, String sActionId) {
        try {
            ActionSpec action = getActionInfo(sActionId);
            boolean bIntrasession = action.isIntrasession();
            boolean bIntersection = action.isIntersection();
            List<String> operandIds = getActionOperandsByNum(sActionId, 1);
            if (operandIds == null) {
                return failurePacket("No operand 1 in \"Create\" action");
            }
            List<ActOpnd> hsOpnd1 = new ArrayList<ActOpnd>();
            String sLastError = null;
            Collections.reverse(operandIds);
            for (String sOpndId : operandIds) {
                System.out.println("Evaluating first operand " + sOpndId);
                // Get the runtime action operand and insert it into the HashSet
                // of
                // first operands. Most of the times, the run-time operand is
                // the same as the
                // compile-time operand. A function operand at run-time is
                // different.
                ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                sLastError = actOpnds[0].getError();
                if (sLastError != null) {
                    System.out.println("Last error when evaluating operand 1: "
                            + sLastError);
                    continue;
                }
                for (int i = 0; i < actOpnds.length; i++) {
                    hsOpnd1.add(actOpnds[i]);
                }
            }
            //printOpndSet(hsOpnd1, "Set of first operands in \"Create\"");
            for(ActOpnd a : hsOpnd1){
                System.out.println(a.getName() + ":" + a.getType());
            }
            if (hsOpnd1.isEmpty()) {
                return failurePacket("No first operands in \"Create\". Last error was: "
                        + sLastError);
            }

            operandIds = getActionOperandsByNum(sActionId, 2);
            List<ActOpnd> hsOpnd2 = new ArrayList<ActOpnd>();
            if (operandIds != null) {
                Collections.reverse(operandIds);

                for (String sOpndId : operandIds) {
                    System.out.println("Evaluating second operand " + sOpndId);
                    ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                    sLastError = actOpnds[0].getError();
                    if (sLastError != null) {
                        System.out
                                .println("Last error when evaluating operand 2: "
                                        + sLastError);
                        continue;
                    }
                    for (int i = 0; i < actOpnds.length; i++) {
                        hsOpnd2.add(actOpnds[i]);
                    }
                }
            }
            //printOpndSet(hsOpnd2, "Set of second operands in \"Create\"");
            for(ActOpnd a : hsOpnd2){
                System.out.println(a.getName() + ":" + a.getType());
            }

            operandIds = getActionOperandsByNum(sActionId, 3);
            List<ActOpnd> hsOpnd3 = new ArrayList<ActOpnd>();
            if (operandIds != null) {
                Collections.reverse(operandIds);

                for (String sOpndId : operandIds) {
                    System.out.println("Evaluating third operand " + sOpndId);
                    ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                    sLastError = actOpnds[0].getError();
                    if (sLastError != null) {
                        System.out
                                .println("Last error when evaluating operand 3: "
                                        + sLastError);
                        continue;
                    }
                    for (int i = 0; i < actOpnds.length; i++) {
                        hsOpnd3.add(actOpnds[i]);
                    }
                }
            }
            //printOpndSet(hsOpnd3, "Set of third operands in \"Create\"");
            for(ActOpnd a : hsOpnd3){
                System.out.println(a.getName() + ":" + a.getType());
            }

            operandIds = getActionOperandsByNum(sActionId, 4);
            List<ActOpnd> hsOpnd4 = new ArrayList<ActOpnd>();
            if (operandIds != null) {
                Collections.reverse(operandIds);

                for (String sOpndId : operandIds) {
                    System.out.println("Evaluating fourth operand " + sOpndId);
                    ActOpnd[] actOpnds = evalOpnd(eventctx, sOpndId);
                    sLastError = actOpnds[0].getError();
                    if (sLastError != null) {
                        System.out.println("Last error when evaluating operand 4: "
                                + sLastError);
                        continue;
                    }
                    for (int i = 0; i < actOpnds.length; i++) {
                        hsOpnd4.add(actOpnds[i]);
                    }
                }
            }
            //printOpndSet(hsOpnd4, "Set of fourth operands in \"Create\"");
            for(ActOpnd a : hsOpnd4){
                System.out.println("4 >> " + a.getName() + ":" + a.getType());
            }

            // Create entity specified by operand 1...
            ActOpnd opnd1 = null;
            Iterator<ActOpnd> iter = hsOpnd1.iterator();
            while (iter.hasNext()) {
                opnd1 = iter.next();
                System.out.println(opnd1.getName() + ":" + opnd1.getType());
            }
            // opnd1 cannot be null.
            String sEntType = opnd1.getType();
            String sEntName = opnd1.getName();

            // ...within the container specified in operand 4...
            ActOpnd opnd4 = null;
            iter = hsOpnd4.iterator();
            while (iter.hasNext()) {
                opnd4 = iter.next();
            }
            String sContType = null;
            String sContId = null;
            if (opnd4 != null) {
                sContType = opnd4.getType();
                sContId = opnd4.getId();
            }

            // ...with the properties specified in operands 3...
            ActOpnd opnd3 = null;
            int n = hsOpnd3.size();
            String[] sProps = null;
            if (n > 0) {
                sProps = new String[n];
                iter = hsOpnd3.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    opnd3 = iter.next();
                    sProps[i++] = opnd3.getName();
                }
            }

            // ...and (if the new entity is an object) representing the entity
            // specified by operand 2 (ignored if the entity to be created
            // is NOT an object).
            ActOpnd opnd2 = null;
            iter = hsOpnd2.iterator();
            while (iter.hasNext()) {
                opnd2 = iter.next();
            }

            String sRepEntName = null;
            String sRepEntId = null;
            String sRepEntType = null;
            boolean bInh = false;
            if (opnd2 != null) {
                sRepEntName = opnd2.getName();
                sRepEntId = opnd2.getId();
                sRepEntType = opnd2.getType();
                bInh = opnd2.isSubgraph();
            }

            System.out.println(".........Represented entity name "
                    + sRepEntName);
            System.out.println(".........Represented entity id " + sRepEntId);
            System.out.println(".........Represented entity type "
                    + sRepEntType);
            System.out.println(".........Represented entity subgraph " + bInh);

            Packet result = null;

            System.out.println(!sEntType.equalsIgnoreCase(GlobalConstants.PM_RULE));
            System.out.println((sContType == null || sContId == null));

            if (!sEntType.equalsIgnoreCase(GlobalConstants.PM_RULE)
                    && (sContType == null || sContId == null)) {
                return failurePacket("Container unspecified in operand 4!");
            }

            Integer id = 0;
            if (sEntType.equalsIgnoreCase(GlobalConstants.PM_RULE)) {
                result = SQLPacketHandler.getSuccessPacket("Now compile rule: "
                        + sEntName);
                System.out.println(result.getStringValue(0));

                // Compile the rule and insert it into the enabled script.
                Packet script = new Packet();
                // The function that processes the script expects it to start at
                // item 3.
                try {
                    script.addItem(ItemType.RESPONSE_TEXT, "Filler");
                    script.addItem(ItemType.RESPONSE_TEXT, "Filler");
                    script.addItem(ItemType.RESPONSE_TEXT, "Filler");
                    script.addItem(ItemType.RESPONSE_TEXT, "script genrule");
                    script.addItem(ItemType.RESPONSE_TEXT, sEntName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return failurePacket("Exception when building the result packet");
                }
                Packet res = compileScriptAndAddToEnabled(null, script);
                if (res == null) {
                    return failurePacket("compileScript... returned null!");
                }
                return res; // failure or success, possibly name:id of the
                // compiled script.
            } else if (sEntType.equalsIgnoreCase(PM_NODE.POL.value)) {
                id = ServerConfig.SQLDAO.addPcInternal(sEntName, null, sEntName,
                        sEntName, sProps);
            } else if (sEntType.equalsIgnoreCase(PM_NODE.USER.value)) {
                id = ServerConfig.SQLDAO.addUserInternal(sEntName, null, sEntName,
                        sEntName, sEntName, sContId, sContType);
            } else if (sEntType.equalsIgnoreCase(PM_NODE.UATTR.value)) {
                id = ServerConfig.SQLDAO.addUattrInternal(sEntName, sEntName,
                        sEntName, sContId, sContType, sProps);
            } else if (sEntType.equalsIgnoreCase(PM_NODE.OATTR.value)) {
                id = ServerConfig.SQLDAO.addOattrInternal(sEntName, null,
                        sEntName, sEntName, sContId, sContType, null, sProps);
            } else if (sEntType.equalsIgnoreCase(GlobalConstants.PM_OBJ)) {
                // Only an object representing a PM entity/subgraph can be
                // created
                // as a response to an event.
                if (opnd2 == null) {
                    return failurePacket("The object to create as a response must represent a PM entity!");
                }
                // Find the object class based on the represented entity type.
                String sObjClass = ServerConfig.SQLDAO.typeToClass(sRepEntType);
                id = ServerConfig.SQLDAO.addObjectInternal(sEntName, null, null,
                        sEntName, sEntName, sContId, sContType, sObjClass,
                        null, null, null, sRepEntName, sRepEntId, bInh, null,
                        null, null, null, null, null, null);
            } else {
                return failurePacket("Creation of this type of entity not implemented");
            }
            if (id == null) {
                return fail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while dispatching event action");
        }

        return SQLPacketHandler.getSuccessPacket();
    }

    private String getRuleEventPattern(String sRuleId){
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sRuleId);
            return extractStrings(select(GET_RULE_EVENT_PATTERN, params)).get(0);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Packet matchEvent(EventContext eventctx, String sRuleId) {
        System.out.println("Matching event against rule " + sRuleId + "...");
        // Get rule's event pattern.
        try {
            String sPatternId = getRuleEventPattern(sRuleId);

            Packet res = eventUserMatchesPattern(eventctx.getSessId(),
                    eventctx.getProcId(), sPatternId);
            if (res.hasError()) {
                return res;
            }

            res = eventNameMatchesPattern(eventctx.getEventName(), sPatternId);
            if (res.hasError()) {
                return res;
            }

            res = eventObjMatchesPattern(eventctx, sPatternId);
            return res;
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return failurePacket("Exception while matching event to pattern in rule "
                    + sRuleId + ": " + e.getMessage());
        }
    }

    private String getEventPatternObjSpec(String sPatternId){
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sPatternId);
            return extractStrings(select(GET_EVENT_PATTERN_OBJ_SPEC, params)).get(0);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private List<String> getEventPatternContSpecs(String sPatternId){
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sPatternId);
            return extractStrings(select(GET_EVENT_PATTERN_CONT_SPEC, params));
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    // Matching the event's object spec against the pattern's object spec and
    // container specs.
    //
    // The event's object spec should contain: the object name, the object id,
    // the obj class name, the list of first level containers of the object
    // in the format t1|name1,t2|name2,..., where ti is the container
    // type, and namei is the container name, and finally the id of the
    // object's associated attribute.
    //
    // The pattern's object spec may be missing, or is '*', or is 'b'|associd.
    // The pattern's container spec may be missing, or is a list
    // t1|id1,t2|id2,..., where ti is 'b' or 'oc', and idi is the id of an
    // object attribute or object class.
    //
    // The matching algorithm.
    // If the pattern's object spec is not missing and is not '*',
    // check whether the event's object is the one specified in the
    // pattern's object spec. If not, return false.
    // If the pattern's container spec is missing, return true.
    // If the event's object is contained in one of the pattern's object
    // attributes, or the event's object class is the same as one of the
    // pattern's classes, return true.
    // Otherwise return false.

    public Packet eventObjMatchesPattern(EventContext eventctx,
                                         String sPatternId) throws Exception {
        // The event name, object name, id, class, containers, assoc oattr.
        String sEventName = eventctx.getEventName();
        String sEventObjName = eventctx.getObjName();
        String sEventObjId = eventctx.getObjId();
        String sEventObjClass = eventctx.getObjClass();
        String sEventContainers = eventctx.getctx1();
        String sEventAssocId = eventctx.getctx2();

        if (sEventObjClass != null
                && sEventObjClass
                .equalsIgnoreCase(GlobalConstants.PM_CLASS_FILE_NAME)
                && sEventObjId != null) {
            sEventAssocId = ServerConfig.SQLDAO.getAssocOattr(sEventObjId);
        }
        String sEventObjClassId = null;
        if (sEventObjClass != null) {
            try {
                sEventObjClassId = ServerConfig.SQLDAO.getEntityId(sEventObjClass,
                        GlobalConstants.PM_OBJ_CLASS);
            } catch (Exception e) {
                e.printStackTrace();
                return failurePacket(e.getMessage());
            }
        }

        System.out.println("===========Event's name is " + sEventName);
        System.out
                .println("===========Event's object name is " + sEventObjName);
        System.out.println("===========Event's object id is " + sEventObjId);
        System.out.println("===========Event's object class " + sEventObjClass);
        System.out.println("===========Event's object class id "
                + sEventObjClassId);
        System.out.println("===========Event's object containers are "
                + sEventContainers);
        System.out.println("===========Event's object assoc id is "
                + sEventAssocId);
        System.out.println("===========");

        try {
            String sPatObjSpec = getEventPatternObjSpec(sPatternId);

            System.out.println("===========Pattern's object spec is "
                    + sPatObjSpec);

            List<String> sPatContSpecs = getEventPatternContSpecs(sPatternId);
            if (sPatContSpecs != null) {
                for (String sPatContSpec : sPatContSpecs) {
                    System.out.println("===========Pattern's container spec is "
                            + sPatContSpec);
                }
            }

            // Let's match the event's object against the pattern's objspec.

            // If the pattern's objspec is not null and is not '*', check
            // the objname/objid of the event against the pattern's.
            if (sPatObjSpec != null && !sPatObjSpec.equals("*")) {
                // The pattern's objspec has the format type|name, where the
                // type should be 'b'.
                String[] pieces = sPatObjSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
                // Pieces[0] is the type (oc or b). Pieces[1] is the id.
                String sPatObjName = pieces[1];

                if (!sEventObjName.equalsIgnoreCase(sPatObjName)) {
                    return failurePacket("Event's object does not match pattern object spec!");
                }
            }

            // Now let's match the event's object against the pattern's
            // container specs.
            // If the pattern has no container specs, the event matches the
            // pattern.
            if (sPatContSpecs == null || sPatContSpecs.contains("b|*")) {
                return SQLPacketHandler.getSuccessPacket();
            }

            // VERY IMPORTANT NOTE: Probably we need to check that the event's
            // object is still included in the original containers as specified
            // in the event. This would prevent applying rules in cascade if the
            // object location is changed by the first (and subsequent) rule.
            if (sEventContainers != null) {
                String[] pieces = sEventContainers.split(GlobalConstants.PM_LIST_MEMBER_SEP);
                for (int i = 0; i < pieces.length; i++) {
                    String[] sTypeAndName = pieces[i].split(GlobalConstants.PM_ALT_DELIM_PATTERN);
                    System.out.println("Container no. " + i + ": "
                            + sTypeAndName[0] + "|" + sTypeAndName[1]);
                    String sEventContId = ServerConfig.SQLDAO.getEntityId(
                            sTypeAndName[1], sTypeAndName[0]);
                    if (!ServerConfig.SQLDAO.attrIsAscendant(sEventAssocId,
                            sEventContId)) {
                        return failurePacket("Event's object is no longer in the original containers!");
                    }
                }
            }

            // To match, the event's object must be included in at least one of
            // the pattern's containers.
            for (String sPatContSpec : sPatContSpecs) {
                System.out.println("For pattern's container spec "
                        + sPatContSpec);

                String[] pieces = sPatContSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
                // Pieces[0] is the type (oc or b or rec). Pieces[1] is the id
                // or *
                // (in the case "any record").
                if (pieces[0].equalsIgnoreCase(GlobalConstants.PM_OBJ_CLASS)) {
                    if (sEventObjClassId != null
                            && sEventObjClassId.equalsIgnoreCase(getEntityId(pieces[1], GlobalConstants.PM_OBJ_CLASS))) {
                        System.out.println("Event obj class matches pattern cont spec");
                        return SQLPacketHandler.getSuccessPacket();
                    }
                } else if (pieces[0].equalsIgnoreCase(PM_NODE.OATTR.value)) {
                    if (sEventAssocId != null
                            && ServerConfig.SQLDAO.attrIsAscendant(sEventAssocId,
                            getEntityId(pieces[1], PM_NODE.OATTR.value))) {
                        System.out.println("Event obj is contained in pattern cont spec");
                        return SQLPacketHandler.getSuccessPacket();
                    }
                } else if (pieces[0].equalsIgnoreCase(GlobalConstants.PM_RECORD)) {
                    String sRecordId = getEntityId(pieces[1], PM_NODE.OATTR.value);
                    if (sEventAssocId != null) {
                        if (sRecordId.equals("*")
                                && ServerConfig.SQLDAO.isInARecord(sEventAssocId)) {
                            return SQLPacketHandler.getSuccessPacket();
                        } else if (!sRecordId.equals("*")
                                && ServerConfig.SQLDAO.isInRecord(sEventAssocId,
                                sRecordId)) {
                            return SQLPacketHandler.getSuccessPacket();
                        }
                    }
                }
                System.out.println("Event obj does not match this pattern cont spec. Continuing...");
            }
            return failurePacket("Event's object does not match pattern container specs!");
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception during event object matching!");
        }
    }

    private List<String> getEventPatternOpSpec(String sPatternId){
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sPatternId);
            return extractStrings(select(GET_EVENT_PATTERN_OP_SPEC, params));
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Packet eventNameMatchesPattern(String sEventOp, String sPatternId) {
        // System.out.println("<<<eventNameMatchesPattern " + sPatternId);
        // Get the op specs from the pattern.
        try {
            List<String> sOpSpecs = getEventPatternOpSpec(sPatternId);
            if (sOpSpecs.contains("*")) {
                return SQLPacketHandler.getSuccessPacket();
            }

            for (String sOpSpec : sOpSpecs) {
                if (sEventOp.equalsIgnoreCase(sOpSpec)) {
                    return SQLPacketHandler.getSuccessPacket();
                }
            }
            return failurePacket("Event operation doesn't match specs!");
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception during event operation matching!");
        }
    }

    private List<String> getEventPatternUserSpec(String sPatternId){
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sPatternId);
            return extractStrings(select(GET_EVENT_PATTERN_USER_SPEC, params));
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Packet eventUserMatchesPattern(String sSessId, String sProcId,
                                          String sPatternId) {
        // System.out.println("<<<userMatchesPattern " + sPatternId);
        // Get the user specs from the pattern.
        try {
            List<String> sUserSpecs = getEventPatternUserSpec(sPatternId);

            // If user spec is missing from the pattern, or is empty, the user
            // can be
            // anything. We still need to check the pc specs.
            if (sUserSpecs == null || sUserSpecs.contains("u|*")) {
                // System.out.println("No user specs in pattern");
                if (userMatchesPatternPc(sSessId, sPatternId)) {
                    return SQLPacketHandler.getSuccessPacket();
                } else {
                    return failurePacket("Event user does not match the pattern!");
                }
            }

            // If user spec contains '*', the user can be anything. We still
            // need
            // to check the pc specs.

            // At least a user, a user attribute, a session, or a process
            // is specified in the pattern.
            // Check whether the event's user/session/process matches any of
            // those specified.
            String sSessUserId = ServerConfig.SQLDAO.getSessionUserId(sSessId);
            // System.out.println("Event user is " + sSessUserId);
            for (String sUserSpec : sUserSpecs) {
                System.out.println("Found user spec " + sUserSpec);

                String[] pieces = sUserSpec.split(GlobalConstants.PM_ALT_DELIM_PATTERN);
                if (pieces[0].equalsIgnoreCase(PM_NODE.USER.value)) {
                    // The spec is a user.
                    if (!sSessUserId.equalsIgnoreCase(pieces[1])) {
                        continue;
                    }
                    if (userMatchesPatternPc(sSessId, sPatternId)) {
                        return SQLPacketHandler.getSuccessPacket();
                    }
                } else if (pieces[0].equalsIgnoreCase(PM_NODE.UATTR.value)) {
                    // The spec is a user attribute, not necessarily active for
                    // the user.
                    // We check only that the user has that attribute.
                    if (!ServerConfig.SQLDAO.userIsAscendant(Integer.valueOf(sSessUserId),
                            Integer.valueOf(getEntityId(pieces[1], PM_NODE.UATTR.value)))) {
                        continue;
                    }
                    if (userAttrMatchesPatternPc(sSessId, pieces[1], sPatternId)) {
                        return SQLPacketHandler.getSuccessPacket();
                    }
                } else if (pieces[0].equalsIgnoreCase(GlobalConstants.PM_SESSION)) {
                    // The spec is a session.
                    // We check only that the current session is the same as the
                    // one in spec.
                    if (!sSessId.equalsIgnoreCase(pieces[1])) {
                        continue;
                    }
                    if (userAttrMatchesPatternPc(sSessId, pieces[1], sPatternId)) {
                        return SQLPacketHandler.getSuccessPacket();
                    }
                } else if (pieces[0].equalsIgnoreCase(GlobalConstants.PM_PROCESS)) {
                    // The spec is a process.
                    // We check only that the current process is the same as the
                    // one in spec.
                    if (!sProcId.equalsIgnoreCase(pieces[1])) {
                        continue;
                    }
                    if (userAttrMatchesPatternPc(sSessId, pieces[1], sPatternId)) {
                        return SQLPacketHandler.getSuccessPacket();
                    }
                    // } else {
                    // // The spec is an active user attribute. Check whether
                    // the
                    // // user
                    // // is active in that attribute in the current session.
                    // if (userIsActiveInAttr(sSessId, pieces[1])) {
                    // System.out.println("Session user found active in attribute "
                    // + ServerConfig.SQLDAO.getEntityId(pieces[1],
                    // PM_NODE.UATTR.value));
                    // return SQLPacketHandler.getSuccessPacket();
                    // }
                    // System.out.println("Session user NOT active in attribute "
                    // +
                    // ServerConfig.SQLDAO.getEntityId(pieces[1],PM_NODE.UATTR.value));
                }
            }
            return failurePacket("Event user does not match pattern!");
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception while matching event user to pattern "
                    + sPatternId + ": " + e.getMessage());
        }
    }

    private List<String> getEventPatternPcSpec(String sPatternId){
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sPatternId);
            return extractStrings(select(GET_EVENT_PATTERN_PC_SPEC, params));
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean userMatchesPatternPc(String sSessId, String sPatternId) {
        // System.out.println("<<<userMatchesPatternPc " + sPatternId);
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sPatternId);
            List<Object> patternInfo = select(GET_EVENT_PATTERN_INFO, params).get(0);
            boolean isActive = (Boolean)patternInfo.get(0);
            boolean isAny = (Boolean)patternInfo.get(1);
            List<String> sPcSpec = getEventPatternPcSpec(sPatternId);

            if (isAny) {
                // If no pc specs or *:
                // System.out.println("Pc is any of...");
                if (sPcSpec == null || sPcSpec.contains("*")) {
                    return true;
                }

                // Some pc specs:
                return userIsInAnyPolicy(sSessId, sPcSpec);
            } else {
                // It's each policy.
                return userIsInEachPolicy(sSessId, sPcSpec);
            }
            // }
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return false;
        }
    }


    public boolean userIsInAnyPolicy(String sSessId, List<String> pcNames) {
        if (pcNames == null || pcNames.size() == 0) {
            return true;
        }
        try {
            for (String pc : pcNames) {
                if (userIsInPolicy(sSessId, pc)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean userIsInPolicy(String sSessId, String sPolId) throws Exception {
        String sUserId = ServerConfig.SQLDAO.getSessionUserId(sSessId);
        return ServerConfig.SQLDAO.userIsAscendantToPolicy(sUserId, sPolId);
    }

    public boolean userAttrIsInAnyPolicy(String sAttrId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sAttrId);
        Vector<Integer> pcs = ServerConfig.SQLDAO.getPolicyClasses();

        // list of policy classes that contain this attr
        ArrayList<Integer> results = ServerConfig.SQLDAO.extractIntegers(select(ATTR_IN_ANY_POL,params));
        for (Integer pc : results) {
            if (pcs.contains(pc)) {
                return true;
            }
        }

        return false;
    }


    public boolean userAttrIsInEachPolicy(String sAttrId) throws Exception {
        MySQL_Parameters params = new MySQL_Parameters();
        params.addParam(ParamType.INT, sAttrId);
        Vector<Integer> pcs = ServerConfig.SQLDAO.getPolicyClasses();

        // list of policy classes that contain this attr
        ArrayList<Integer> results = ServerConfig.SQLDAO.extractIntegers(select(ATTR_IN_ANY_POL,params));
        for (Integer pc : results) {
            if (!pcs.contains(pc)) {
                return false;
            }
        }

        return false;
    }
    public boolean userIsInEachPolicy(String sSessId, List<String> pcNames) {
        if (pcNames == null || pcNames.size() == 0) {
            return true;
        }
        try {
            for (String pc : pcNames) {
                if (!userIsInPolicy(sSessId, pc)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Packet genIbac(String sSessId, String sPolName) {
        // Check permissions...

        // If a policy with the property "type=identity" already exists, return.
        Packet res;
        Packet script = new Packet();
        try {
            if (ServerConfig.SQLDAO.getEntityWithPropInternal(PM_NODE.POL.value,
                    "type=identity") != null) {
                return failurePacket("An identity-based policy already exists!");
            }

            if (ServerConfig.SQLDAO.getEntityId(sPolName, PM_NODE.POL.value) != null) {
                return failurePacket("A policy with the same name already exists!");
            }

            String sCmd;

            // Create the policy class.
            sCmd = "add|p|" + sPolName + "|c|PM";
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|prop|type=identity|p|" + sPolName;
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            // Generate the EVER script for "User create".
            // The function that processes the script expects it to start at
            // item 3.
            script.addItem(ItemType.RESPONSE_TEXT, "Filler");
            script.addItem(ItemType.RESPONSE_TEXT, "Filler");
            script.addItem(ItemType.RESPONSE_TEXT, "Filler");
            script.addItem(ItemType.RESPONSE_TEXT, "script ibac");
            script.addItem(ItemType.RESPONSE_TEXT, "");
            script.addItem(ItemType.RESPONSE_TEXT, "when any user performs \""
                    + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
            script.addItem(ItemType.RESPONSE_TEXT, "do");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "  create user attribute uattr_name_of_user(user_new())");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    with property prop_name_of_user(user_new())");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    in policy pol_with_prop(\"type=identity\")");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "  assign user user_new() to user attribute uattr_name_of_new_user()");
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.toString());
        }

        // Compile and submit the script.
        // boolean bScriptCompiled = sysCaller.addScript(script);
        // System.out.println("Result from compile script: " + bScriptCompiled);

        res = compileScriptAndAddToEnabled(sSessId, script);
        if (res == null) {
            return failurePacket("Null result from addScript call");
        }
        if (res.hasError()) {
            return res;
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    public Packet genMls(String sSessId, String sPolName, String sLevels) {
        // Check permissions...

        // If a policy with the property "type=mls" already exists, return.
        try {
            if (ServerConfig.SQLDAO.getEntityWithPropInternal(PM_NODE.POL.value,
                    "type=mls") != null) {
                return failurePacket("A multi-level security policy already exists!");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        // Generate commands for creating the policy class.
        String sCmd;
        Packet res;

        sCmd = "add|p|" + sPolName + "|c|PM";
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }
        sCmd = "add|prop|type=mls|p|" + sPolName;
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }
        sCmd = "add|prop|levels=" + sLevels + "|p|" + sPolName;
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }

        // Get the levels in an array.
        String[] levels = sLevels.split(GlobalConstants.PM_LIST_MEMBER_SEP);
        int n = levels.length;
        if (n < 2) {
            return failurePacket("Too few security levels!");
        }

        // Generate the user attributes.
        for (int i = 1; i <= n; i++) {
            if (i == 1) {
                sCmd = "add|a|" + levels[i - 1] + "|p|" + sPolName;
            } else {
                sCmd = "add|a|" + levels[i - 1] + "|a|" + levels[i - 2];
            }
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
        }

        // Generate the object attribute x1...xn.
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i <= n; i++) {
            sb.append(levels[i - 1]);
        }
        String sAll = sb.toString();
        sCmd = "add|b|" + sAll + "|p|" + sPolName;
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }

        // Prepare the array sRight used to store the object attributes
        // x1...xn, x2...xn, x3...xn, ..., xn, and the array sLeft used to store
        // the object attributes x1...xn, x1...xn-1, ..., x1.
        String[] sRight = new String[n];
        sRight[0] = sAll;
        String[] sLeft = new String[n];
        sLeft[0] = sAll;
        // Generate the other names in the arrays.
        for (int i = 2; i <= n; i++) {
            sb = new StringBuffer();
            for (int j = i; j <= n; j++) {
                sb.append(levels[j - 1]);
            }
            sRight[i - 1] = sb.toString();
            System.out.println("Generated right name " + sRight[i - 1]);
        }

        for (int i = 2; i <= n; i++) {
            sb = new StringBuffer();
            for (int j = 1; j <= n - i + 1; j++) {
                sb.append(levels[j - 1]);
            }
            sLeft[i - 1] = sb.toString();
            System.out.println("Generated left name " + sLeft[i - 1]);
        }

        // Generate the object attributes.
        for (int i = 2; i <= n; i++) {
            sCmd = "add|b|" + sRight[i - 1] + "|b|" + sRight[i - 2];
            System.out.println("Trying to generate container " + sRight[i - 1]
                    + " in " + sRight[i - 2]);
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
        }

        for (int i = 2; i <= n; i++) {
            sCmd = "add|b|" + sLeft[i - 1] + "|b|" + sLeft[i - 2];
            System.out.println("Trying to generate container " + sLeft[i - 1]
                    + " in " + sLeft[i - 2]);
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
        }

        // Generate the object attributes x2,...,xn-1 and assign them as
        // follows (the indexes start with 1; in the code they start with 0):
        // x2 -> leftn-1, x2 -> right2
        // x3 -> leftn-2, x3 -> right3
        // ...
        // xn-1 -> left2, xn-1 -> rightn-1
        for (int i = 2; i <= n - 1; i++) {
            sCmd = "add|b|" + levels[i - 1] + "|b|" + sLeft[n - i];
            System.out.println("Trying to generate container " + levels[i - 1]
                    + " in " + sLeft[n - i]);
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            sCmd = "asg|b|" + levels[i - 1] + "|b|" + sRight[i - 1];
            System.out.println("Trying to assign container " + levels[i - 1]
                    + " in " + sRight[i - 1]);
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
        }

        // Generate and assign the operation sets:
        // xi -> {read} -> x1...xi (i.e., left[n-i+1]),
        // xi -> {write} -> xi...xn (i.e., right[i]).
        for (int i = 1; i <= n; i++) {
            String sOpsName = UtilMethods.generateRandomName();
            sCmd = "add|s|" + sOpsName + "|oc|ignored|a|" + levels[i - 1];
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "asg|s|" + sOpsName + "|b|" + sLeft[n - i];
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|File read|s|" + sOpsName;
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            sOpsName = UtilMethods.generateRandomName();
            sCmd = "add|s|" + sOpsName + "|oc|ignored|a|" + levels[i - 1];
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "asg|s|" + sOpsName + "|b|" + sRight[i - 1];
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|File write|s|" + sOpsName;
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
        }

        // Generate the objects that represent containers x1,...,xn:
        for (int i = 1; i <= n; i++) {
            sCmd = "add|ob|" + levels[i - 1] + " rep|Object attribute|no|"
                    + levels[i - 1] + "|ignored|p|" + sPolName;
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            String sOpsName = UtilMethods.generateRandomName();
            sCmd = "add|s|" + sOpsName + "|oc|ignored|a|" + levels[i - 1];
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "asg|s|" + sOpsName + "|b|" + levels[i - 1] + " rep";
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|Object attribute assign to|s|" + sOpsName;
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
            sCmd = "add|op|Object attribute create object|s|" + sOpsName;
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
        }

        // Add the subject attribute constraints.
        // first the attribute sets.
        sCmd = "add|sac|mls|intra session";
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }

        for (int i = 1; i <= n; i++) {
            sCmd = "add|as|" + levels[i - 1];
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            sCmd = "add|a|" + levels[i - 1] + "|as|" + levels[i - 1];
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }

            sCmd = "add|as|" + levels[i - 1] + "|sa|mls";
            res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
            if (res.hasError()) {
                return res;
            }
        }

        // Generate the EVER script for MLS.
        Packet script = new Packet();
        // The function that processes the script expects it to start at item 3.
        try {
            script.addItem(ItemType.RESPONSE_TEXT, "Filler");
            script.addItem(ItemType.RESPONSE_TEXT, "Filler");
            script.addItem(ItemType.RESPONSE_TEXT, "Filler");
            script.addItem(ItemType.RESPONSE_TEXT, "script mls");
            script.addItem(ItemType.RESPONSE_TEXT, "");
            script.addItem(ItemType.RESPONSE_TEXT, "when any user performs \""
                    + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
            script.addItem(ItemType.RESPONSE_TEXT, "do");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "  assign user user_new() to");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    user attribute uattr_lowest_level()");

            script.addItem(ItemType.RESPONSE_TEXT, "when any user performs \""
                    + GlobalConstants.PM_EVENT_OBJECT_CREATE + "\"");
            script.addItem(ItemType.RESPONSE_TEXT, "do");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "  assign object object_new() to");
            script.addItem(
                    ItemType.RESPONSE_TEXT,
                    "    object attribute oattr_with_name_of_active_attr(pol_with_prop(\"type=mls\"))");
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the script packet!");
        }

        // Compile and submit the script.
        res = compileScriptAndAddToEnabled(sSessId, script);
        if (res == null) {
            return failurePacket("Null result from addScript call");
        }
        return res;
    }

    // Configure a DAC policy. Parameters:
    // sPolName: the DAC policy name.
    // sContName: the name of a user attribute that will contain all users'
    // name attributes.

    public Packet genDac(String sSessId, String sPolName, String sContName) {
        // Check permissions...

        // If a policy with the property "type=discretionary" already exists,
        // return.
        try {
            if (ServerConfig.SQLDAO.getEntityWithPropInternal(PM_NODE.POL.value,
                    "type=discretionary") != null) {
                return failurePacket("A discretionary policy already exists!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket(e.getMessage());
        }

        String sCmd;
        Packet res;

        // Create the policy class.
        sCmd = "add|p|" + sPolName + "|c|PM";
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }
        sCmd = "add|prop|type=discretionary|p|" + sPolName;
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }

        // Create the attribute container and its representative object.
        sCmd = "add|a|" + sContName + "|p|" + sPolName;
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }
        sCmd = "add|prop|usersof=discretionary|a|" + sContName;
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }
        sCmd = "add|ob|" + sContName + " rep|User attribute|yes|" + sContName
                + "|ignored|p|" + sPolName;
        res = ServerConfig.SQLDAO.interpretCmd(sSessId, sCmd);
        if (res.hasError()) {
            return res;
        }

        // Generate the EVER script for "User create".
        Packet script = new Packet();
        // The function that processes the script expects it to start at item 3.
        try {
            script.addItem(ItemType.RESPONSE_TEXT, "Filler");
            script.addItem(ItemType.RESPONSE_TEXT, "Filler");
            script.addItem(ItemType.RESPONSE_TEXT, "Filler");
            script.addItem(ItemType.RESPONSE_TEXT, "script dac");
            script.addItem(ItemType.RESPONSE_TEXT, "");
            script.addItem(ItemType.RESPONSE_TEXT, "when any user performs \""
                    + GlobalConstants.PM_EVENT_USER_CREATE + "\"");
            script.addItem(ItemType.RESPONSE_TEXT, "do");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "  create user attribute uattr_name_of_user(user_new())");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    with property prop_name_of_user(user_new())");
            script.addItem(ItemType.RESPONSE_TEXT, "    in policy pol_discr()");

            script.addItem(ItemType.RESPONSE_TEXT,
                    "  assign user user_new() to user attribute uattr_name_of_new_user()");

            script.addItem(ItemType.RESPONSE_TEXT,
                    "  assign user attribute uattr_name_of_new_user() to");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    user attribute uattr_discr_users()");

            script.addItem(ItemType.RESPONSE_TEXT,
                    "  create object attribute oattr_home_of_new_user()");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    with property prop_home_of_new_user()");
            script.addItem(ItemType.RESPONSE_TEXT, "    in policy pol_discr()");

            script.addItem(ItemType.RESPONSE_TEXT,
                    "  create object obj_rep_of_home_of_new_user()");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    representing object attribute oattr_home_of_new_user() and ascendants");
            script.addItem(ItemType.RESPONSE_TEXT, "    in policy pol_discr()");

            script.addItem(ItemType.RESPONSE_TEXT,
                    "  grant uattr_name_of_new_user() operations");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    \"File read\", \"File write\"");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    on object attribute oattr_home_of_new_user()");

            script.addItem(ItemType.RESPONSE_TEXT,
                    "  grant uattr_name_of_new_user() operations");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    \"Object attribute create object\",");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    \"Object attribute create object attribute\",");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    \"Object attribute create operation set\",");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    \"Object attribute assign\",");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    \"Object attribute assign to\",");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    \"Operation set assign to\",");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    \"Operation set assign\",");
            script.addItem(ItemType.RESPONSE_TEXT, "    \"Entity represent\"");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    on object attribute oattr_rep_of_home_of_new_user()");

            script.addItem(ItemType.RESPONSE_TEXT,
                    "  grant uattr_name_of_new_user() operations");
            script.addItem(ItemType.RESPONSE_TEXT, "    \"User assign\",");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    \"User attribute assign to operation set\"");
            script.addItem(ItemType.RESPONSE_TEXT,
                    "    on object attribute oattr_rep_of_discr_users()");
        } catch (Exception e) {
            e.printStackTrace();
            return failurePacket("Exception when building the result packet");
        }

        // Compile and submit the script.
        // boolean bScriptCompiled = sysCaller.addScript(script);
        // System.out.println("Result from compile script: " + bScriptCompiled);

        res = compileScriptAndAddToEnabled(sSessId, script);
        if (res == null) {
            return failurePacket("Null result from addScript call");
        }
        if (res.hasError()) {
            return res;
        }
        return SQLPacketHandler.getSuccessPacket();
    }

    // Event Obj id Obj class ctx1 ctx2
    // ---------------------------------------------------------------------------
    // Object create obj id File Containers Permissions
    // Object read ? File - -
    // Object write - File Containers -
    // Object delete obj id - Containers assoc id
    // User create user id User - -
    // Object send obj id File Recipient -
    // Session delete sess id Session - -
    //
    // / For "Object delete", ctx1 contains the containers the deleted object is
    // directly assigned to. ctx2 contains the id of the associated object
    // attribute.
    // For Object write/read, first get the obj id, ctx1 and ctx2.

    public Packet processEvent(String sSessId, String sProcId,
                               String sEventName, String sObjName, String sObjId,
                               String sObjClass, String sObjType, String ctx1, String ctx2){
        System.out.println("processEvent with initial arguments");
        System.out.println("    sessId          = " + sSessId);
        System.out.println("    procId          = " + sProcId);
        System.out.println("    eventName       = " + sEventName);
        System.out.println("    objName         = " + sObjName);
        System.out.println("    objId           = " + sObjId);
        System.out.println("    objClass        = " + sObjClass);
        System.out.println("    objType         = " + sObjType);
        System.out.println("    ctx1            = " + ctx1);
        System.out.println("    ctx2            = " + ctx2);

        // NDK Added stuff here

        String sHost = null;
        String sUserId = null;
        String sUser = null;
        String sAction = null;
        try {
            sHost = ServerConfig.SQLDAO.getSessionHostName(sSessId);
            sUserId = ServerConfig.SQLDAO.getSessionUserId(sSessId);
            sUser = ServerConfig.SQLDAO.getEntityName(sUserId, PM_NODE.USER.value);
            sAction = sEventName;
            // Addition Finished - NDK

            if (sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_OBJECT_WRITE)
                    || sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_OBJECT_READ)) {
                sObjId = ServerConfig.SQLDAO.getEntityId(sObjName, GlobalConstants.PM_OBJ);
            } else if (sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_USER_CREATE)) {
                sObjId = ServerConfig.SQLDAO.getEntityId(sObjName, PM_NODE.USER.value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (sEventName.equalsIgnoreCase(GlobalConstants.PM_EVENT_OBJECT_WRITE)
                    || sEventName
                    .equalsIgnoreCase(GlobalConstants.PM_EVENT_OBJECT_READ)) {
                String sAssocId = ServerConfig.SQLDAO.getAssocOattr(sObjId);
                // Get the first level containers where this object is in this
                // moment.
                ArrayList<Integer> attr = ServerConfig.SQLDAO.getToAttrs(sAssocId,
                        PM_NODE.OATTR.value, DEPTH);
                StringBuffer sb = new StringBuffer();
                boolean firstTime = true;
                if (attr != null) {
                    for (Integer i : attr) {
                        String sId = String.valueOf(i);
                        if (firstTime) {
                            firstTime = false;
                        } else {
                            sb.append(GlobalConstants.PM_LIST_MEMBER_SEP);
                        }
                        sb.append(PM_NODE.OATTR.value
                                + GlobalConstants.PM_ALT_FIELD_DELIM
                                + ServerConfig.SQLDAO.getEntityName(sId,
                                PM_NODE.OATTR.value));
                    }
                }
                if (sb.length() > 0) {
                    ctx1 = sb.toString();
                }
                ctx2 = sAssocId;
            }
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            // NDK - Added
            if (ServerConfig.auditDebug) {
                try {
                    Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction,
                            false, "This is the description", sObjName, sObjId);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            // end = NDK
            return failurePacket("Exception processing event arguments");
        }

        System.out.println("processEvent with processed arguments");
        System.out.println("    sessId          = " + sSessId);
        System.out.println("    procId          = " + sProcId);
        System.out.println("    eventName       = " + sEventName);
        System.out.println("    objName         = " + sObjName);
        System.out.println("    objId           = " + sObjId);
        System.out.println("    objClass        = " + sObjClass);
        System.out.println("    objType         = " + sObjType);
        System.out.println("    ctx1            = " + ctx1);
        System.out.println("    ctx2            = " + ctx2);

        // Get the enabled script. Set the global var sEnabledScriptId
        // because applyActionDeleteRule will us it.
        // The call chain is:
        // processEvent->applyRule->applyAction->applyActionDeleteRule.
        sEnabledScriptId = getEnabledScriptId();
        if (sEnabledScriptId == null) {
            ServerConfig.SQLDAO.nextSessionNumber++;
            if (ServerConfig.auditDebug) {
                try {
                    Audit.setAuditInfo(sSessId, sHost, sUser, sUserId, sAction,
                            true, "This is the description", sObjName, sObjId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // end of addition- NDK
            return SQLPacketHandler.getSuccessPacket();
        }

        Packet res = null;
        try {
            ArrayList<String> ruleIds = extractStrings(select(GET_SCRIPT_RULES, sEnabledScriptId));

            EventContext eventctx = new EventContext(sSessId, sProcId,
                    sEventName, sObjName, sObjClass, sObjType, sObjId, ctx1,
                    ctx2);
            for(String sRuleId : ruleIds){
                res = matchEvent(eventctx, sRuleId);
                if (res.hasError()) {
                    System.out.println(res.getErrorMessage());
                } else {
                    System.out.println("...Applying rule " + sRuleId + "!");
                    res = applyRule(eventctx, sRuleId);
                    if (res.hasError()) {
                        System.out.println("Error in rule " + sRuleId + ": "
                                + res.getErrorMessage());
                    }
                }
            }
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return failurePacket("Exception looping through rules during event match: "
                    + e.getMessage());
        }

        return SQLPacketHandler.getSuccessPacket();
    }

    public boolean userAttrMatchesPatternPc(String sSessId, String sAttrId,
                                            String sPatternId) {
        System.out.println("[userAttrMatchesPatternPc " + sAttrId
                + ", pattern = " + sPatternId);
        try {
            MySQL_Parameters params = new MySQL_Parameters();
            params.addParam(ParamType.STRING, sPatternId);
            List<Object> patternInfo = select(GET_EVENT_PATTERN_INFO, params).get(0);
            boolean isActive = (Boolean)patternInfo.get(0);
            boolean isAny = (Boolean)patternInfo.get(1);
            List<String> sPcSpec = getEventPatternPcSpec(sPatternId);
            if (isAny) {
                // If no pc specs:
                if (sPcSpec== null || sPcSpec.contains("*")) {
                    return true;
                }

                // Some pc specs:
                return userAttrIsInAnyPolicy(sAttrId);
            } else {
                // It's each policy.
                return userAttrIsInEachPolicy(sAttrId);
            }
        } catch (Exception e) {
            if (ServerConfig.debugFlag) {
                e.printStackTrace();
            }
            return false;
        }
    }

}



// Class Action's Operand
class ActOpnd {

    private String sName;
    private String sType;
    private String sId;
    private boolean bSubgraph;
    private boolean bComplement;
    private String sError;

    public ActOpnd(String sName, String sType, String sId, boolean bSubgraph,
                   boolean bComplement, String sError) {
        this.sName = sName;
        this.sType = sType;
        this.sId = sId;
        this.bSubgraph = bSubgraph;
        this.bComplement = bComplement;
        this.sError = sError;
    }

    public String getName() {
        return sName;
    }

    public String getId() {
        return sId;
    }

    public String getType() {
        return sType;
    }

    public boolean isSubgraph() {
        return bSubgraph;
    }

    public boolean isComplement() {
        return bComplement;
    }

    public String getError() {
        return sError;
    }

    public void setName(String sName) {
        this.sName = sName;
    }

    public void setId(String sId) {
        this.sId = sId;
    }

    public void setType(String sType) {
        this.sType = sType;
    }

    public void setSubgraph(boolean bSubgraph) {
        this.bSubgraph = bSubgraph;
    }

    public void setComplement(boolean bComplement) {
        this.bComplement = bComplement;
    }

    public void setError(String sError) {
        this.sError = sError;
    }
}

class EventContext {

    private String sSessId;
    private String sProcId;
    private String sEventName;
    private String sObjName;
    private String sObjClass;
    private String sObjType;
    private String sObjId;
    private String ctx1;
    private String sctx2;

    public EventContext(String sSessId, String sProcId, String sEventName,
                        String sObjName, String sObjClass, String sObjType, String sObjId,
                        String ctx1, String sctx2) {
        this.sSessId = sSessId;
        this.sProcId = sProcId;
        this.sEventName = sEventName;
        this.sObjName = sObjName;
        this.sObjClass = sObjClass;
        this.sObjType = sObjType;
        this.sObjId = sObjId;
        this.ctx1 = ctx1;
        this.sctx2 = sctx2;
    }

    public String getSessId() {
        return sSessId;
    }

    public String getProcId() {
        return sProcId;
    }

    public String getEventName() {
        return sEventName;
    }

    public String getObjName() {
        return sObjName;
    }

    public String getObjId() {
        return sObjId;
    }

    public String getObjClass() {
        return sObjClass;
    }

    public String getObjType() {
        return sObjType;
    }

    public String getctx1() {
        return ctx1;
    }

    public String getctx2() {
        return sctx2;
    }
}


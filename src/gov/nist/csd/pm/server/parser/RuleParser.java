package gov.nist.csd.pm.server.parser;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.constants.PM_NODE;
import gov.nist.csd.pm.common.util.RandomGUID;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class RuleParser {

    /**
	 * @uml.property  name="myEngine"
	 * @uml.associationEnd  
	 */
    /**
	 * @uml.property  name="crtToken"
	 * @uml.associationEnd  
	 */
    Token crtToken;
    /**
	 * @uml.property  name="myScanner"
	 * @uml.associationEnd  
	 */
    RuleScanner myScanner;
    /**
	 * @uml.property  name="ruleLineNo"
	 */
    int ruleLineNo;
    /**
	 * @uml.property  name="ruleSpec"
	 * @uml.associationEnd  
	 */
    RuleSpec ruleSpec;
    /**
	 * @uml.property  name="patternSpec"
	 * @uml.associationEnd  
	 */
    PatternSpec patternSpec;
    /**
	 * @uml.property  name="actionSpec"
	 * @uml.associationEnd  
	 */
    ActionSpec actionSpec;
    /**
	 * @uml.property  name="condSpec"
	 * @uml.associationEnd  
	 */
    CondSpec condSpec;
    /**
	 * @uml.property  name="actionSpecs"
	 */
    List<ActionSpec> actionSpecs;
    /**
	 * @uml.property  name="vOpnds"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.server.parser.OpndSpec"
	 */
    List<OpndSpec> vOpnds;
    /**
	 * @uml.property  name="globalFree"
	 */
    int globalFree;
    /**
	 * @uml.property  name="globalCrt"
	 */
    int globalCrt;
    /**
	 * @uml.property  name="parentStack"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.Integer"
	 */
    List<Integer> parentStack;
    /**
	 * @uml.property  name="defaultType"
	 */
    String defaultType;
    /**
	 * @uml.property  name="isComplement"
	 */
    boolean isComplement;
    /**
	 * @uml.property  name="scriptName"
	 */
    String scriptName;
    /**
	 * @uml.property  name="scriptId"
	 */
    String scriptId;
    /**
	 * @uml.property  name="lastRuleId"
	 */
    String lastRuleId;
    /**
	 * @uml.property  name="crtRuleRank"
	 */
    int crtRuleRank = 0;

    public static void main(String args[]) {
    	if (ServerConfig.datastore.equalsIgnoreCase("SQL")) {
	    	System.out.println("Obligation is not yet implemented in SQL");
	        System.exit(-1);
    	}
        if (args.length < 1) {
            System.out.println("Missing argument: input file!");
            System.exit(-1);
        }
        try {
            RuleParser ruleParser = new RuleParser(args[0]); // removed extra null argument 
            String result = ruleParser.parse();
            if (result == null) {
                System.out.println("The program is correct!");
            } else {
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RuleParser(String path) {
        myScanner = new RuleScanner(path);
        
    }

    public RuleParser(File inputFile) {
        myScanner = new RuleScanner(inputFile);
    }

    /**
	 * @return
	 * @uml.property  name="scriptName"
	 */
    public String getScriptName() {
        return scriptName;
    }

    /**
	 * @return
	 * @uml.property  name="scriptId"
	 */
    public String getScriptId() {
        return scriptId;
    }

    // Invoke the function corresponding to the start nonterminal, rules.
    public String parse() throws Exception {
        String result = null;
        crtToken = myScanner.nextToken();
        result = script();
        if (result != null) {
            if (ServerConfig.myEngine != null) {
            	System.out.println("OK I'm AT 0 in RuleParser.java");
               // ServerConfig.myEngine.deleteScriptInternal(scriptId);
            }
        }
        return result;
    }

    // <script> ::= <script header> <rules>
    private String script() throws Exception {
        String result = null;

        traceEntry("script");

        result = scriptHeader();
        if (result != null) {
            traceExit("script");
            return result;
        }

        result = rules();

        traceExit("script");
        return result;
    }

    // <script header> ::= script script_name
    private String scriptHeader() {
        traceEntry("scriptHeader");

        if (crtToken.tokenId != RuleScanner.PM_SCRIPT) {
            traceExit("scriptHeader");
            System.out.println("HEADER");
            return signalError(crtToken.tokenValue, RuleScanner.PM_SCRIPT);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("scriptHeader");
            System.out.println("HEADER 2");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }
        traceConsume();
        semopScript();
        crtToken = myScanner.nextToken();

        traceExit("scriptHeader");
        return null;
    }

    // <rules> ::= { <rule> }
    private String rules() throws Exception {
        String result = null;

        traceEntry("rules");
        while (crtToken.tokenId != RuleScanner.PM_EOF) {
            result = rule();
            if (result != null) {
                traceExit("rules");
                return result;
            }
        }
        traceExit("rules");
        return null;
    }

    // Note that we allow "script name" and rules to be interspersed. This allows
    // concatenation of multiple scripts without having to deal with header
    // deletion. All interior script headers are ignored.
    //
    // <rule> ::= [label :] when <event pattern> do <response> | script script_name
    private String rule() throws Exception {
        String result;

        traceEntry("rule");

        if (crtToken.tokenId == RuleScanner.PM_SCRIPT) {
            traceConsume();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_WORD) {
                traceExit("rule");
                System.out.println("Rule");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
            traceExit("rule");
            return null;
        }

        // Must be a proper rule.
        semopRuleInit();

        // Check for label.
        if (crtToken.tokenId == RuleScanner.PM_WORD) {
            traceConsume();
            semopSetLabel();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_COLON) {
                traceExit("rule");
                return signalError(crtToken.tokenValue, RuleScanner.PM_COLON);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        if (crtToken.tokenId != RuleScanner.PM_WHEN) {
            traceExit("rule");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WHEN);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        result = eventPattern();
        if (result != null) {
            traceExit("rule");
            return result;
        }
        if (crtToken.tokenId != RuleScanner.PM_DO) {
            traceExit("rule");
            return signalError(crtToken.tokenValue, RuleScanner.PM_DO);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        result = response();
        if (result != null) {
            traceExit("rule");
            return result;
        }
        result = semopRuleFinal();
        traceExit("rule");
        return result;
    }

    // <event pattern> ::= <user spec> <pc spec> <op spec> <obj spec>
    private String eventPattern() {
        String result;

        traceEntry("eventPattern");
        result = userSpec();
        if (result != null) {
            traceExit("eventPattern");
            return result;
        }
        result = pcSpec();
        if (result != null) {
            traceExit("eventPattern");
            return result;
        }
        result = opSpec();
        if (result != null) {
            traceExit("eventPattern");
            return result;
        }
        result = objSpec();
        traceExit("eventPattern");
        return result;
    }

    // <user spec> ::= [<user> | <any user> | <session>]
    private String userSpec() {
        String result = null;

        traceEntry("userSpec");
        if (crtToken.tokenId == RuleScanner.PM_ANY) {
            result = anyUser();
        } else if (crtToken.tokenId == RuleScanner.PM_USER || crtToken.tokenId == RuleScanner.PM_WORD) {
            result = user();
        } else if (crtToken.tokenId == RuleScanner.PM_SESSION) {
            result = session();
        } else if (crtToken.tokenId == RuleScanner.PM_PROCESS) {
            result = process();
        }

        if (result != null) {
            traceExit("userSpec");
            return result;
        }

        traceExit("userSpec");
        return result;
    }

    // <user> ::= [user] user_name
    private String user() {
        traceEntry("user");
        if (crtToken.tokenId != RuleScanner.PM_WORD && crtToken.tokenId != RuleScanner.PM_USER) {
            traceExit("user");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_USER);
        }
        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("user");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        traceConsume();
        semopAUser();
        crtToken = myScanner.nextToken();

        traceExit("user");
        return null;
    }

    // <any user> ::= any [user] [of <user or attr set>]
    private String anyUser() {
        String result = null;

        traceEntry("anyUser");
        if (crtToken.tokenId != RuleScanner.PM_ANY) {
            traceExit("anyUser");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ANY);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId == RuleScanner.PM_OF) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = userOrAttrSet();
        }

        traceExit("anyUser");
        return result;
    }

    // <user or attr set> ::= <user or attr> {, <user or attr>}
    private String userOrAttrSet() {
        String result = null;

        traceEntry("userOrAttrSet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD && crtToken.tokenId != RuleScanner.PM_USER
                    && crtToken.tokenId != RuleScanner.PM_ACTIVE && crtToken.tokenId != RuleScanner.PM_ATTR) {
                traceExit("userOrAttrSet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_USER, RuleScanner.PM_ATTR);
            }

            result = userOrAttr();
            if (result != null) {
                traceExit("userOrAttrSet");
                return result;
            }

            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("userOrAttrSet");
        return null;
    }

    // <user or attr> ::= <user> | <uattr>
    private String userOrAttr() {
        String result = null;

        traceEntry("userOrAttr");
        if (crtToken.tokenId == RuleScanner.PM_USER || crtToken.tokenId == RuleScanner.PM_WORD) {
            result = user();
        } else if (crtToken.tokenId == RuleScanner.PM_ACTIVE || crtToken.tokenId == RuleScanner.PM_ATTR) {
            result = uattr();
        } else {
            traceExit("userOrAttr");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_USER,
                    RuleScanner.PM_ACTIVE, RuleScanner.PM_ATTR);
        }
        traceExit("userOrAttr");
        return result;
    }

    // <uattr> ::= [active] attribute attribute_name
    private String uattr() {
        traceEntry("uattr");

        if (crtToken.tokenId == RuleScanner.PM_ACTIVE) {
            traceConsume();
            crtToken = myScanner.nextToken();

            if (crtToken.tokenId != RuleScanner.PM_ATTR) {
                traceExit("uattr");
                return signalError(crtToken.tokenValue, RuleScanner.PM_ATTR);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_WORD) {
                traceExit("uattr");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
            }
            traceConsume();
            semopAnActiveUattr();
            crtToken = myScanner.nextToken();
        } else {
            if (crtToken.tokenId != RuleScanner.PM_ATTR) {
                traceExit("uattr");
                return signalError(crtToken.tokenValue, RuleScanner.PM_ATTR);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_WORD) {
                traceExit("uattr");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
            }

            traceConsume();
            semopAUattr();
            crtToken = myScanner.nextToken();
        }
        traceExit("uattr");
        return null;
    }

    // <session> ::= session session_id
    private String session() {
        traceEntry("session");
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("session");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }
        traceConsume();
        semopASession();
        crtToken = myScanner.nextToken();
        traceExit("session");
        return null;
    }

    // <process> ::= process process_id
    private String process() {
        traceEntry("process");
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("process");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }
        traceConsume();
        semopAProcess();
        crtToken = myScanner.nextToken();
        traceExit("process");
        return null;
    }

    // <pc spec> ::= [active] [in <pc subspec>]
    private String pcSpec() {
        String result = null;

        traceEntry("pcSpec");

        semopPcSpecInit();
        if (crtToken.tokenId == RuleScanner.PM_ACTIVE) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        if (crtToken.tokenId == RuleScanner.PM_IN) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = pcSubspec();
        }
        if (result != null) {
            traceExit("pcSpec");
            return result;
        }

        traceExit("pcSpec");
        return result;
    }

    // <pc subspec> ::= <pc> | <any pc> | <each pc>
    private String pcSubspec() {
        String result = null;

        traceEntry("pcSubspec");
        if (crtToken.tokenId == RuleScanner.PM_POLICY || crtToken.tokenId == RuleScanner.PM_WORD) {
            result = pc();
        } else if (crtToken.tokenId == RuleScanner.PM_ANY) {
            result = anyPc();
        } else if (crtToken.tokenId == RuleScanner.PM_EACH) {
            result = eachPc();
        } else {
            traceExit("pcSubspec");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_POLICY,
                    RuleScanner.PM_ANY, RuleScanner.PM_EACH);
        }
        traceExit("pcSubspec");
        return result;
    }

    // <pc> ::= [policy] pc_name
    private String pc() {
        traceEntry("pc");
        if (crtToken.tokenId != RuleScanner.PM_WORD && crtToken.tokenId != RuleScanner.PM_POLICY) {
            traceExit("pc");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_POLICY);
        }
        if (crtToken.tokenId == RuleScanner.PM_POLICY) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("pc");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        traceConsume();
        semopAPc();
        crtToken = myScanner.nextToken();

        traceExit("pc");
        return null;
    }

    // <any pc> ::= any [policy] [of <pc set>]
    private String anyPc() {
        String result = null;

        traceEntry("anyPc");
        if (crtToken.tokenId != RuleScanner.PM_ANY) {
            traceExit("anyPc");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ANY);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId == RuleScanner.PM_POLICY) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        if (crtToken.tokenId == RuleScanner.PM_OF) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = pcSet();
        }
        traceExit("anyPc");
        return result;
    }

    // <pc set> ::= <pc> {, <pc>}
    private String pcSet() {
        String result = null;

        traceEntry("pcSet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD && crtToken.tokenId != RuleScanner.PM_POLICY) {
                traceExit("pcSet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_POLICY);
            }
            result = pc();
            if (result != null) {
                traceExit("pcSet");
                return result;
            }
            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("pcSet");
        return null;
    }

    // <each pc> ::= each [policy] [of <pc set>]
    private String eachPc() {
        String result = null;

        traceEntry("eachPc");
        if (crtToken.tokenId != RuleScanner.PM_EACH) {
            traceExit("eachPc");
            return signalError(crtToken.tokenValue, RuleScanner.PM_EACH);
        }

        traceConsume();
        semopEachPc();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId == RuleScanner.PM_POLICY) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId == RuleScanner.PM_OF) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = pcSet();
        }
        traceExit("eachPc");
        return result;
    }

    // <op spec> ::= performs <op subspec>
    private String opSpec() {
        String result = null;

        traceEntry("opSpec");
        if (crtToken.tokenId != RuleScanner.PM_PERFORMS) {
            traceExit("opSpec");
            return signalError(crtToken.tokenValue, RuleScanner.PM_PERFORMS);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        result = opSubspec();

        traceExit("opSpec");
        return result;
    }

    // <op subspec> ::= <op> | <any op>
    private String opSubspec() {
        String result = null;

        traceEntry("opSubspec");
        if (crtToken.tokenId == RuleScanner.PM_OP || crtToken.tokenId == RuleScanner.PM_WORD) {
            result = op();
        } else if (crtToken.tokenId == RuleScanner.PM_ANY) {
            result = anyOp();
        } else {
            traceExit("opSubspec");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_OP,
                    RuleScanner.PM_ANY);
        }
        traceExit("opSubspec");
        return result;
    }

    // <op> ::= [operation] op_name
    private String op() {

        traceEntry("op");
        if (crtToken.tokenId != RuleScanner.PM_WORD && crtToken.tokenId != RuleScanner.PM_OP) {
            traceExit("op");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_OP);
        }
        if (crtToken.tokenId == RuleScanner.PM_OP) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("op");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        traceConsume();
        semopAnOp();
        crtToken = myScanner.nextToken();

        traceExit("op");
        return null;
    }

    // <any op> ::= any [operation] [of <op set>]
    private String anyOp() {
        String result = null;

        traceEntry("anyOp");
        if (crtToken.tokenId != RuleScanner.PM_ANY) {
            traceExit("anyOp");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ANY);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId == RuleScanner.PM_OP) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        if (crtToken.tokenId == RuleScanner.PM_OF) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = opSet();
        }
        traceExit("anyOp");
        return result;
    }

    // <op set> ::= <op> {, <op>}
    private String opSet() {
        String result = null;

        traceEntry("opSet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD && crtToken.tokenId != RuleScanner.PM_OP) {
                traceExit("opSet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_OP);
            }

            result = op();
            if (result != null) {
                traceExit("opSet");
                return result;
            }

            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("opSet");
        return null;
    }

    // <obj spec> ::= [on <obj subspec> <container subspec>]
    private String objSpec() {
        String result = null;

        traceEntry("objSpec");
        if (crtToken.tokenId != RuleScanner.PM_ON) {
            traceExit("objSpec");
            return null;
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        result = objSubspec();
        if (result != null) {
            traceExit("objSpec");
            return result;
        }

        result = containerSubspec();

        traceExit("objSpec");
        return result;
    }

    // <obj subspec> ::= <obj> | <any obj>
    private String objSubspec() {
        String result = null;

        traceEntry("objSubspec");
        if (crtToken.tokenId == RuleScanner.PM_OBJECT || crtToken.tokenId == RuleScanner.PM_WORD) {
            result = obj();
        } else if (crtToken.tokenId == RuleScanner.PM_ANY) {
            result = anyObj();
        } else {
            traceExit("objSubspec");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_OBJECT,
                    RuleScanner.PM_ANY);
        }
        traceExit("objSubspec");
        return result;
    }

    // <obj> ::= [object] object_name
    private String obj() {
        traceEntry("obj");

        if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("obj");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        traceConsume();
        semopObj();
        crtToken = myScanner.nextToken();

        traceExit("obj");
        return null;
    }

    // <any obj> ::= any [object]
    private String anyObj() {
        String result = null;

        traceEntry("anyObj");
        if (crtToken.tokenId != RuleScanner.PM_ANY) {
            traceExit("anyObj");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ANY);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("anyObj");
        return result;
    }

    // <container subspec> ::= [of <obj or attr or record or class set>]
    private String containerSubspec() {
        String result = null;

        traceEntry("containerSubspec");
        if (crtToken.tokenId == RuleScanner.PM_OF) {
            traceConsume();
            crtToken = myScanner.nextToken();
            result = objOrAttrOrRecordOrClassSet();
        }
        traceExit("containerSubspec");
        return result;
    }

    // <obj or attr or record or class set> ::= <obj or attr or record or class>
    //                                        {, <obj or attr or record or class>}
    private String objOrAttrOrRecordOrClassSet() {
        String result = null;

        traceEntry("objOrAttrOrRecordOrClassSet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD
                    && crtToken.tokenId != RuleScanner.PM_OBJECT
                    && crtToken.tokenId != RuleScanner.PM_ATTR
                    && crtToken.tokenId != RuleScanner.PM_RECORD
                    && crtToken.tokenId != RuleScanner.PM_ANY
                    && crtToken.tokenId != RuleScanner.PM_CLASS) {
                traceExit("objOrAttrOrRecordOrClassSet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_OBJECT,
                        RuleScanner.PM_ATTR, RuleScanner.PM_RECORD, RuleScanner.PM_ANY, RuleScanner.PM_CLASS);
            }
            result = objOrAttrOrRecordOrClass();
            if (result != null) {
                traceExit("objOrAttrOrRecordOrClassSet");
                return result;
            }

            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("objOrAttrOrRecordOrClassSet");
        return result;
    }

    // <obj or attr or record or class> ::= <obj as cont> | <oattr> | <record spec> | <a class>
    private String objOrAttrOrRecordOrClass() {
        String result = null;

        traceEntry("objOrAttrOrRecordOrClass");
        if (crtToken.tokenId == RuleScanner.PM_OBJECT || crtToken.tokenId == RuleScanner.PM_WORD) {
            result = objAsCont();
        } else if (crtToken.tokenId == RuleScanner.PM_ATTR) {
            result = oattr();
        } else if (crtToken.tokenId == RuleScanner.PM_CLASS) {
            result = aClass();
        } else if (crtToken.tokenId == RuleScanner.PM_RECORD || crtToken.tokenId == RuleScanner.PM_ANY) {
            result = recordSpec();
        } else {
            traceExit("objOrAttrOrRecordOrClass");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_OBJECT,
                    RuleScanner.PM_ATTR, RuleScanner.PM_RECORD, RuleScanner.PM_ANY, RuleScanner.PM_CLASS);
        }
        traceExit("objOrAttrOrRecordOrClass");
        return result;
    }

    // <obj as cont> ::= [object] object_name
    private String objAsCont() {
        traceEntry("objAsCont");

        if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("objAsCont");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        traceConsume();
        semopObjAsCont();
        crtToken = myScanner.nextToken();

        traceExit("objAsCont");
        return null;
    }

    // <oattr> ::= attribute attribute_name
    private String oattr() {
        String result = null;

        traceEntry("oattr");
        if (crtToken.tokenId != RuleScanner.PM_ATTR) {
            traceExit("oattr");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ATTR);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("oattr");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        traceConsume();
        semopAnOattr();
        crtToken = myScanner.nextToken();

        traceExit("oattr");
        return result;
    }

    // <record spec> ::= <record> | <any record>
    private String recordSpec() {
        String result = null;

        traceEntry("recordSpec");
        if (crtToken.tokenId == RuleScanner.PM_RECORD) {
            result = record();
        } else if (crtToken.tokenId == RuleScanner.PM_ANY) {
            result = anyRecord();
        } else {
            traceExit("recordSpec");
            return signalError(crtToken.tokenValue, RuleScanner.PM_RECORD, RuleScanner.PM_ANY);
        }
        traceExit("recordSpec");
        return result;
    }

    // <record> ::= record record_name
    private String record() {
        String result = null;

        traceEntry("record");
        if (crtToken.tokenId != RuleScanner.PM_RECORD) {
            traceExit("record");
            return signalError(crtToken.tokenValue, RuleScanner.PM_RECORD);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("record");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        traceConsume();
        semopARecord();
        crtToken = myScanner.nextToken();

        traceExit("record");
        return result;
    }

    // <any record> ::= any record
    private String anyRecord() {
        String result = null;

        traceEntry("anyRecord");
        if (crtToken.tokenId != RuleScanner.PM_ANY) {
            traceExit("anyRecord");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ANY);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != RuleScanner.PM_RECORD) {
            traceExit("anyRecord");
            return signalError(crtToken.tokenValue, RuleScanner.PM_RECORD);
        }
        traceConsume();
        semopAnyRecord();
        crtToken = myScanner.nextToken();
        traceExit("anyObj");
        return result;
    }

    // <a class> ::= class class_name
    private String aClass() {
        String result = null;

        traceEntry("aClass");
        if (crtToken.tokenId != RuleScanner.PM_CLASS) {
            traceExit("aClass");
            return signalError(crtToken.tokenValue, RuleScanner.PM_CLASS);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("aClass");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        traceConsume();
        semopAClass();
        crtToken = myScanner.nextToken();

        traceExit("aClass");
        return result;
    }

    // <response> ::= {<conditional action>}
    private String response() {
        String result = null;

        traceEntry("response");

        while (crtToken.tokenId == RuleScanner.PM_ASSIGN
                || crtToken.tokenId == RuleScanner.PM_GRANT
                || crtToken.tokenId == RuleScanner.PM_CREATE
                || crtToken.tokenId == RuleScanner.PM_DENY
                || crtToken.tokenId == RuleScanner.PM_DELETE
                || crtToken.tokenId == RuleScanner.PM_IF) {
            result = condAction();
            if (result != null) {
                traceExit("response");
                return result;
            }
        }
        traceExit("response");
        return result;
    }

    // <conditional action> ::= [if <condition> then] <action>
    private String condAction() {
        String result = null;

        traceEntry("condAction");

        semopCondActionInit();

        if (crtToken.tokenId == RuleScanner.PM_IF) {
            traceConsume();
            crtToken = myScanner.nextToken();

            semopCondInit();

            result = condition();
            if (result != null) {
                traceExit("condAction");
                return result;
            }
            if (crtToken.tokenId != RuleScanner.PM_THEN) {
                traceExit("condAction");
                return signalError(crtToken.tokenValue, RuleScanner.PM_THEN);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        result = action();
        if (result != null) {
            traceExit("condAction");
            return result;
        }

        semopCondActionFinal();
        traceExit("condAction");
        return result;
    }

    // <condition> ::= [not] <cond entity> exists
    private String condition() {
        String result = null;

        traceEntry("condition");
        if (crtToken.tokenId == RuleScanner.PM_NOT) {
            traceConsume();
            semopSetCondNegated();
            crtToken = myScanner.nextToken();
        }
        result = condEntity();
        if (result != null) {
            traceExit("condition");
            return result;
        }
        if (crtToken.tokenId == RuleScanner.PM_EXISTS) {
            semopSetCondExist();
        } else {
            traceExit("condition");
            return signalError(crtToken.tokenValue, RuleScanner.PM_EXISTS);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        return null;
    }

    // <cond entity> ::= user [attribute] <name or function call> |
    //                   object [attribute] <name or function call> |
    //                   policy <name or function call>
    private String condEntity() {
        String result = null;

        traceEntry("condEntity");

        if (crtToken.tokenId != RuleScanner.PM_USER
                && crtToken.tokenId != RuleScanner.PM_OBJECT
                && crtToken.tokenId != RuleScanner.PM_POLICY) {
            traceExit("condEntity");
            return signalError(crtToken.tokenValue, RuleScanner.PM_USER,
                    RuleScanner.PM_OBJECT, RuleScanner.PM_POLICY);
        }

        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            semopSetTypeUser();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId == RuleScanner.PM_ATTR) {
                traceConsume();
                semopUpdateType();
                crtToken = myScanner.nextToken();
            }
        } else if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            semopSetTypeObject();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId == RuleScanner.PM_ATTR) {
                traceConsume();
                semopUpdateType();
                crtToken = myScanner.nextToken();
            }
        } else if (crtToken.tokenId == RuleScanner.PM_POLICY) {
            traceConsume();
            semopSetTypePolicy();
            crtToken = myScanner.nextToken();
        }
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("condEntity");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        semopCondEntityInit();

        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("condEntity");
            return result;
        }
        semopCondEntityFinal();

        traceExit("condEntity");
        return result;
    }

    // <action> ::= <assign action> |
    //              <grant action> |
    //              <create action> |
    //              <deny action> |
    //              <delete action>
    private String action() {
        String result = null;

        traceEntry("action");

        if (crtToken.tokenId == RuleScanner.PM_ASSIGN) {
            result = assignAction();
        } else if (crtToken.tokenId == RuleScanner.PM_GRANT) {
            result = grantAction();
        } else if (crtToken.tokenId == RuleScanner.PM_CREATE) {
            result = createAction();
        } else if (crtToken.tokenId == RuleScanner.PM_DENY) {
            result = denyAction();
        } else if (crtToken.tokenId == RuleScanner.PM_DELETE) {
            result = deleteAction();
        } else {
            traceExit("action");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ASSIGN,
                    RuleScanner.PM_GRANT, RuleScanner.PM_CREATE);
        }
        traceExit("action");
        return result;
    }

    // <delete action> ::= delete <delete subaction>
    private String deleteAction() {
        String result;

        traceEntry("deleteAction");
        if (crtToken.tokenId != RuleScanner.PM_DELETE) {
            traceExit("deleteAction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_DELETE);
        }

        traceConsume();
        crtToken = myScanner.nextToken();
        result = deleteSubaction();
        traceExit("assignAction");
        return result;
    }

    // <delete subaction> ::= <delete assignment subaction> |
    //                        <delete deny subaction> |
    //                        <delete rules subaction>
    private String deleteSubaction() {
        String result = null;

        traceEntry("deleteSubaction");

        if (crtToken.tokenId == RuleScanner.PM_ASSIGNMENT) {
            result = deleteAssignmentSubaction();
        } else if (crtToken.tokenId == RuleScanner.PM_DENY) {
            result = deleteDenySubaction();
        } else if (crtToken.tokenId == RuleScanner.PM_RULE
                || crtToken.tokenId == RuleScanner.PM_RULES) {
            result = deleteRuleSubaction();
        } else {
            traceExit("deleteSubaction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ASSIGNMENT);
        }
        traceExit("deleteSubaction");
        return result;
    }

    // <delete rule subaction> ::= <rule prefix> <label set>
    // <rule prefix> ::= rule | rules
    private String deleteRuleSubaction() {
        String result = null;

        traceEntry("deleteRuleSubaction");

        if (crtToken.tokenId != RuleScanner.PM_RULES
                && crtToken.tokenId != RuleScanner.PM_RULE) {
            traceExit("deleteRuleSubaction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_RULE, RuleScanner.PM_RULES);
        }

        traceConsume();
        semopSetActionDeleteRule();
        crtToken = myScanner.nextToken();

        result = labelSet();

        traceExit("deleteRuleSubaction");
        return result;
    }

    // <label set> ::= label {, label}
    private String labelSet() {

        traceEntry("labelSet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD) {
                traceExit("labelSet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
            }

            traceConsume();
            semopALabel();
            crtToken = myScanner.nextToken();

            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("labelSet");
        return null;
    }

    // <delete assignment subaction> ::= assignment of <assign what> <assign to containers>
    private String deleteAssignmentSubaction() {
        String result;

        traceEntry("deleteAssignmentSubaction");
        if (crtToken.tokenId != RuleScanner.PM_ASSIGNMENT) {
            traceExit("deleteAssignmentSubaction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ASSIGNMENT);
        }
        traceConsume();
        semopSetActionDeassign();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != RuleScanner.PM_OF) {
            traceExit("deleteAssignmentSubaction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_OF);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        semopSetTypeUattr();
        result = assignWhat();
        if (result != null) {
            traceExit("deleteAssignmentSubaction");
            return result;
        }
        result = assignToContainers();
        traceExit("deleteAssignmentSubaction");
        return result;
    }

    // <delete deny subaction> ::= deny <deny to> <deny what> <deny on>
    private String deleteDenySubaction() {
        String result = null;

        traceEntry("deleteDenySubaction");
        if (crtToken.tokenId != RuleScanner.PM_DENY) {
            traceExit("deleteDenySubaction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_DENY);
        }

        traceConsume();
        semopSetActionDeleteDeny();
        crtToken = myScanner.nextToken();

        result = denyTo();
        if (result != null) {
            traceExit("deleteDenySubaction");
            return result;
        }

        result = denyWhat();
        if (result != null) {
            traceExit("deleteDenySubaction");
            return result;
        }

        result = denyOn();
        if (result != null) {
            traceExit("deleteDenySubaction");
            return result;
        }

        traceExit("deleteDenySubaction");
        return result;
    }

    // <assign action> ::= assign <assign what> <assign to>
    private String assignAction() {
        String result;

        traceEntry("assignAction");
        if (crtToken.tokenId != RuleScanner.PM_ASSIGN) {
            traceExit("assignAction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ASSIGN);
        }

        traceConsume();
        semopSetActionAssign();
        crtToken = myScanner.nextToken();

        semopSetTypeUattr();
        result = assignWhat();
        if (result != null) {
            traceExit("assignAction");
            return result;
        }

        result = assignTo();
        if (result != null) {
            traceExit("assignAction");
            return result;
        }

        traceExit("assignAction");
        return result;
    }

    // <assign what> ::= <user or obj prefix> [attribute] <name or function call>
    // <user or obj prefix> ::= user | object
    private String assignWhat() {
        String result = null;

        traceEntry("assignWhat");

        if (crtToken.tokenId != RuleScanner.PM_USER
                && crtToken.tokenId != RuleScanner.PM_OBJECT) {
            traceExit("assignWhat");
            return signalError(crtToken.tokenValue, RuleScanner.PM_USER,
                    RuleScanner.PM_OBJECT);
        }

        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            semopSetTypeUser();
            crtToken = myScanner.nextToken();
        } else if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            semopSetTypeObject();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId == RuleScanner.PM_ATTR) {
            traceConsume();
            semopUpdateType();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("assignWhat");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        semopAssignWhatInit();

        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("assignWhat");
            return result;
        }

        semopAssignWhatFinal();
        traceExit("assignWhat");
        return result;
    }

    // <assign to> ::= [<assign like> | <assign as> | <assign to containers>]
    private String assignTo() {
        String result = null;

        traceEntry("assignTo");
        if (crtToken.tokenId == RuleScanner.PM_LIKE) {
            result = assignLike();
        } else if (crtToken.tokenId == RuleScanner.PM_AS) {
            result = assignAs();
        } else if (crtToken.tokenId == RuleScanner.PM_TO) {
            result = assignToContainers();
        }
        traceExit("assignTo");
        return result;
    }

    // <assign like> ::= like <model entity>
    private String assignLike() {
        String result = null;

        traceEntry("assignLike");
        if (crtToken.tokenId != RuleScanner.PM_LIKE) {
            traceExit("assignLike");
            return signalError(crtToken.tokenValue, RuleScanner.PM_LIKE);
        }
        traceConsume();
        crtToken = myScanner.nextToken();
        result = modelEntity();
        traceExit("assignLike");
        return result;
    }

    // <assign as> ::= as <model entity>
    private String assignAs() {
        String result = null;

        traceEntry("assignAs");
        if (crtToken.tokenId != RuleScanner.PM_AS) {
            traceExit("assignAs");
            return signalError(crtToken.tokenValue, RuleScanner.PM_AS);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        result = modelEntity();

        traceExit("assignAs");
        return result;
    }

    // <model entity> ::= <user or obj prefix> [attribute] <name or function call>
    // <user or obj prefix> ::= user | object
    private String modelEntity() {
        String result = null;

        traceEntry("modelEntity");

        if (crtToken.tokenId != RuleScanner.PM_USER
                && crtToken.tokenId != RuleScanner.PM_OBJECT) {
            traceExit("modelEntity");
            return signalError(crtToken.tokenValue, RuleScanner.PM_USER,
                    RuleScanner.PM_OBJECT);
        }

        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            semopSetTypeUser();
            crtToken = myScanner.nextToken();
        } else if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            semopSetTypeObject();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId == RuleScanner.PM_ATTR) {
            traceConsume();
            semopUpdateType();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("modelEntity");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        semopModelEntityInit();

        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("modelEntity");
            return result;
        }

        semopModelEntityFinal();
        traceExit("modelEntity");
        return result;
    }

    // <assign to containers> ::= to <container set>
    private String assignToContainers() {
        String result = null;

        traceEntry("assignToContainers");
        if (crtToken.tokenId != RuleScanner.PM_TO) {
            traceExit("assignToContainers");
            return signalError(crtToken.tokenValue, RuleScanner.PM_TO);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        semopAdjustTypeForContainer();
        result = containerSet();

        traceExit("assignToContainers");
        return result;
    }

    // <container set> ::= <container> {, <container>}
    private String containerSet() {
        String result = null;

        traceEntry("containerSet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD
                    && crtToken.tokenId != RuleScanner.PM_USER
                    && crtToken.tokenId != RuleScanner.PM_OBJECT
                    && crtToken.tokenId != RuleScanner.PM_ATTR
                    && crtToken.tokenId != RuleScanner.PM_POLICY
                    && crtToken.tokenId != RuleScanner.PM_BASE) {
                traceExit("containerSet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD,
                        RuleScanner.PM_USER, RuleScanner.PM_OBJECT, RuleScanner.PM_ATTR,
                        RuleScanner.PM_POLICY, RuleScanner.PM_BASE);
            }
            result = container();
            if (result != null) {
                traceExit("containerSet");
                return result;
            }
            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("containerSet");
        return result;
    }

    // <container> ::= <base cont> | <policy cont> | <attr cont>
    private String container() {
        String result = null;

        traceEntry("container");
        if (crtToken.tokenId == RuleScanner.PM_BASE) {
            result = baseCont();
        } else if (crtToken.tokenId == RuleScanner.PM_POLICY) {
            result = policyCont();
        } else if (crtToken.tokenId == RuleScanner.PM_WORD
                || crtToken.tokenId == RuleScanner.PM_ATTR
                || crtToken.tokenId == RuleScanner.PM_OBJECT
                || crtToken.tokenId == RuleScanner.PM_USER) {
            result = attrCont();
        } else {
            traceExit("container");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD, RuleScanner.PM_ATTR,
                    RuleScanner.PM_USER);
        }
        traceExit("container");
        return result;
    }

    // <base cont> ::= base
    private String baseCont() {
        String result = null;

        traceEntry("baseCont");
        if (crtToken.tokenId != RuleScanner.PM_BASE) {
            traceExit("baseCont");
            return signalError(crtToken.tokenValue, RuleScanner.PM_BASE);
        }

        traceConsume();
        semopSetTypeBase();
        semopBaseCont();
        crtToken = myScanner.nextToken();

        traceExit("baseCont");
        return result;
    }

    // <policy cont> ::= policy <name or function call>
    private String policyCont() {
        String result = null;

        traceEntry("policyCont");
        if (crtToken.tokenId != RuleScanner.PM_POLICY) {
            traceExit("policyCont");
            return signalError(crtToken.tokenValue, RuleScanner.PM_POLICY);
        }
        traceConsume();
        semopSetTypePolicy();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("policyCont");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        semopPolicyContInit();

        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("policyCont");
            return result;
        }

        semopPolicyContFinal();
        traceExit("policyCont");
        return result;
    }

    // <attr cont> ::= <attr prefix> <name or function call>
    // <attr prefix> ::= user attribute | object attribute
    private String attrCont() {
        String result = null;

        traceEntry("attrCont");

        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            semopSetTypeUser();
            crtToken = myScanner.nextToken();
        } else if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            semopSetTypeObject();
            crtToken = myScanner.nextToken();
        } else {
            traceExit("attrCont");
            return signalError(crtToken.tokenValue, RuleScanner.PM_USER,
                    RuleScanner.PM_OBJECT);
        }

        if (crtToken.tokenId != RuleScanner.PM_ATTR) {
            traceExit("attrCont");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ATTR);
        }
        traceConsume();
        semopUpdateType();
        crtToken = myScanner.nextToken();

        semopAttrContInit();

        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("attrCont");
            return result;
        }

        semopAttrContFinal();
        traceExit("attrCont");
        return result;
    }

    // <name or function call> ::= name <arg part>
    private String nameOrFunctionCall() {
        String result = null;

        traceEntry("nameOrFunctionCall");
        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("nameOrFunctionCall");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        traceConsume();
        semopNameOrFunctionCallInit();
        crtToken = myScanner.nextToken();
        if (crtToken.tokenId == RuleScanner.PM_LPAR) {
            result = argPart();
        }
        traceExit("nameOrFunctionCall");
        return result;
    }

    // <arg part> ::= [ ( [ <arg list> ] ) ]
    private String argPart() {
        String result = null;

        traceEntry("argPart");
        if (crtToken.tokenId != RuleScanner.PM_LPAR) {
            traceExit("argPart");
            return signalError(crtToken.tokenValue, RuleScanner.PM_LPAR);
        }

        traceConsume();
        semopArgPartInit();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId != RuleScanner.PM_RPAR) {
            result = argList();
            if (result != null) {
                traceExit("argPart");
                return result;
            }
        }
        if (crtToken.tokenId != RuleScanner.PM_RPAR) {
            traceExit("argPart");
            return signalError(crtToken.tokenValue, RuleScanner.PM_RPAR);
        }

        traceConsume();
        semopArgPartFinal();
        crtToken = myScanner.nextToken();

        traceExit("argPart");
        return null;
    }

    // <arg list> ::= <name or function call> {, <name or function call> }
    private String argList() {
        String result = null;

        traceEntry("argList");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD) {
                traceExit("argList");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
            }
            // In a function argument list, set the type of the argument to unknown.
            // We'll check the argument type against the param type later, BUT
            // ONLY IF the parameter type is not unknown. The check goes as follows:
            // If the arg. is a function call, check this function type against the
            // param type.
            // Else check whether the argument is a PM entity of the type of the
            // parameter.
            semopSetTypeUnknown();
            result = nameOrFunctionCall();
            if (result != null) {
                traceExit("argList");
                return result;
            }

            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("argList");
        return result;
    }

    /*
    // <arg list> ::= <arg> {, <arg> }
    private String argList() {
    String result = null;
    
    traceEntry("argList");
    while (true) {
    if (crtToken.tokenId != RuleScanner.PM_WORD &&
    crtToken.tokenId != RuleScanner.PM_USER &&
    crtToken.tokenId != RuleScanner.PM_OBJECT &&
    crtToken.tokenId != RuleScanner.PM_ATTR &&
    crtToken.tokenId != RuleScanner.PM_POLICY &&
    crtToken.tokenId != RuleScanner.PM_BASE) {
    traceExit("argList");
    return signalError(crtToken.tokenValue, RuleScanner.PM_WORD,
    RuleScanner.PM_USER, RuleScanner.PM_OBJECT, RuleScanner.PM_ATTR,
    RuleScanner.PM_POLICY, RuleScanner.PM_BASE);
    }

    result = arg();
    if (result != null) {
    traceExit("argList");
    return result;
    }

    if (crtToken.tokenId != RuleScanner.PM_COMMA) {
    break;
    }
    traceConsume();
    crtToken = myScanner.nextToken();
    }
    traceExit("argList");
    return result;
    }

    // <arg> ::= [<user or obj prefix> [attribute]] <name or function call> |
    //            policy <name or function call> |
    //            base
    // <user or obj prefix> ::= user | object
    private String arg() {
    String result = null;
    traceEntry("arg");

    switch (crtToken.tokenId) {

    case RuleScanner.PM_USER:
    case RuleScanner.PM_OBJECT:
    traceConsume();
    semopUpdateType();
    crtToken = myScanner.nextToken();
    if (crtToken.tokenId == RuleScanner.PM_ATTR) {
    traceConsume();
    semopUpdateType();
    crtToken = myScanner.nextToken();
    }
    if (crtToken.tokenId != RuleScanner.PM_WORD) {
    traceExit("arg");
    return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
    }
    break;

    case RuleScanner.PM_POLICY:
    traceConsume();
    semopUpdateType();
    crtToken = myScanner.nextToken();
    if (crtToken.tokenId != RuleScanner.PM_WORD) {
    traceExit("arg");
    return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
    }
    break;

    case RuleScanner.PM_BASE:
    traceExit("arg");
    semopUpdateType();
    return null;

    case RuleScanner.PM_WORD:
    break;

    default:
    traceExit("arg");
    return signalError(crtToken.tokenValue, RuleScanner.PM_WORD,
    RuleScanner.PM_USER, RuleScanner.PM_OBJECT,
    RuleScanner.PM_POLICY, RuleScanner.PM_BASE);
    }

    result = nameOrFunctionCall();
    traceExit("arg");
    return result;
    }
     */
    // <grant action> ::= grant <grant to> <grant what> <grant on>
    private String grantAction() {
        String result = null;

        traceEntry("grantAction");
        if (crtToken.tokenId != RuleScanner.PM_GRANT) {
            traceExit("grantAction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_GRANT);
        }

        traceConsume();
        semopSetActionGrant();
        crtToken = myScanner.nextToken();

        result = grantTo();
        if (result != null) {
            traceExit("grantAction");
            return result;
        }

        result = grantWhat();
        if (result != null) {
            traceExit("grantAction");
            return result;
        }

        result = grantOn();
        if (result != null) {
            traceExit("grantAction");
            return result;
        }

        traceExit("grantAction");
        return result;
    }

    // <grant to> ::= <uattr spec> {, <uattr spec> }
    private String grantTo() {
        String result = null;

        traceEntry("grantTo");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD
                    && crtToken.tokenId != RuleScanner.PM_ATTR
                    && crtToken.tokenId != RuleScanner.PM_USER) {
                traceExit("grantTo");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD,
                        RuleScanner.PM_ATTR, RuleScanner.PM_USER);
            }
            result = uattrSpec();
            if (result != null) {
                traceExit("grantTo");
                return result;
            }
            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("grantTo");
        return result;
    }

    // <uattr spec> ::= [[user] attribute] <name or function call>
    private String uattrSpec() {
        String result = null;

        traceEntry("uattrSpec");
        if (crtToken.tokenId != RuleScanner.PM_USER
                && crtToken.tokenId != RuleScanner.PM_ATTR
                && crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("uattrSpec");
            return signalError(crtToken.tokenValue, RuleScanner.PM_USER,
                    RuleScanner.PM_ATTR, RuleScanner.PM_WORD);
        }

        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            crtToken = myScanner.nextToken();

            if (crtToken.tokenId != RuleScanner.PM_ATTR) {
                traceExit("uattrSpec");
                return signalError(crtToken.tokenValue, RuleScanner.PM_ATTR);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        } else if (crtToken.tokenId == RuleScanner.PM_ATTR) {
            traceConsume();
            crtToken = myScanner.nextToken();
        }

        semopSetTypeUattr();
        semopUattrSpecInit();

        result = nameOrFunctionCall();

        if (result != null) {
            traceExit("uattrSpec");
            return result;
        }

        semopUattrSpecFinal();
        traceExit("uattrSpec");
        return result;
    }

    // <grant what> ::= <op prefix> <granted op set>
    // <op prefix> ::= operation | operations
    private String grantWhat() {
        String result = null;

        traceEntry("grantWhat");

        if (crtToken.tokenId != RuleScanner.PM_OPS
                && crtToken.tokenId != RuleScanner.PM_OP) {
            traceExit("grantWhat");
            return signalError(crtToken.tokenValue, RuleScanner.PM_OP, RuleScanner.PM_OPS);
        }

        traceConsume();
        crtToken = myScanner.nextToken();

        result = grantedOpSet();

        traceExit("grantWhat");
        return result;
    }

    // <granted op set> ::= op_name {, op_name}
    private String grantedOpSet() {

        traceEntry("grantedOpSet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD) {
                traceExit("grantedOpSet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
            }

            traceConsume();
            semopGrantedOp();
            crtToken = myScanner.nextToken();

            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("grantedOpSet");
        return null;
    }

    // <grant on> ::= [on [object] [attribute] <name or function call>]
    private String grantOn() {
        String result = null;

        traceEntry("grantOn");
        if (crtToken.tokenId != RuleScanner.PM_ON) {
            traceExit("grantOn");
            return null;
        }

        traceConsume();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId != RuleScanner.PM_OBJECT
                && crtToken.tokenId != RuleScanner.PM_ATTR
                && crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("grantOn");
            return signalError(crtToken.tokenValue, RuleScanner.PM_OBJECT, RuleScanner.PM_ATTR, RuleScanner.PM_WORD);
        }

        semopSetTypeOattr();

        if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            semopUpdateType();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId == RuleScanner.PM_ATTR) {
            traceConsume();
            semopUpdateType();
            crtToken = myScanner.nextToken();
        }

        semopGrantOnInit();

        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("grantOn");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("grantOn");
            return result;
        }

        semopGrantOnFinal();

        traceExit("grantOn");
        return result;
    }

    // <create action> ::= create <create what> <representing what> <with properties> <create where>
    private String createAction() {
        String result = null;

        traceEntry("createAction");
        if (crtToken.tokenId != RuleScanner.PM_CREATE) {
            traceExit("createAction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_CREATE);
        }

        traceConsume();
        semopSetActionCreate();
        crtToken = myScanner.nextToken();

        result = createWhat();
        if (result != null) {
            traceExit("createAction");
            return result;
        }

        result = representingWhat();
        if (result != null) {
            traceExit("createAction");
            return result;
        }

        result = withProperties();
        if (result != null) {
            traceExit("createAction");
            return result;
        }

        result = createWhere();
        if (result != null) {
            traceExit("createAction");
            return result;
        }

        traceExit("createAction");
        return result;
    }

    // <create what> ::= user [attribute] <name or function call> |
    //                   object [attribute] <name or function call> |
    //                   policy <name or function call> |
    //                   rule <name or function call>
    private String createWhat() {
        String result = null;

        traceEntry("createWhat");

        if (crtToken.tokenId != RuleScanner.PM_USER
                && crtToken.tokenId != RuleScanner.PM_OBJECT
                && crtToken.tokenId != RuleScanner.PM_POLICY
                && crtToken.tokenId != RuleScanner.PM_RULE) {
            traceExit("createWhat");
            return signalError(crtToken.tokenValue, RuleScanner.PM_USER,
                    RuleScanner.PM_OBJECT, RuleScanner.PM_POLICY, RuleScanner.PM_RULE);
        }

        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            semopSetTypeUser();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId == RuleScanner.PM_ATTR) {
                traceConsume();
                semopUpdateType();
                crtToken = myScanner.nextToken();
            }
        } else if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            semopSetTypeObject();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId == RuleScanner.PM_ATTR) {
                traceConsume();
                semopUpdateType();
                crtToken = myScanner.nextToken();
            }
        } else if (crtToken.tokenId == RuleScanner.PM_POLICY) {
            traceConsume();
            semopSetTypePolicy();
            crtToken = myScanner.nextToken();
        } else if (crtToken.tokenId == RuleScanner.PM_RULE) {
            traceConsume();
            semopSetTypeRule();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("createWhat");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        semopCreateWhatInit();
        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("createWhat");
            return result;
        }
        semopCreateWhatFinal();
        traceExit("createWhat");
        return result;
    }

    // <representing what> ::= [representing <represented entity> [and ascendants]]
    private String representingWhat() {
        String result = null;

        traceEntry("representingWhat");
        if (crtToken.tokenId != RuleScanner.PM_REPR) {
            traceExit("representingWhat");
            return null;
        }
        // Consume 'representing'.
        traceConsume();
        crtToken = myScanner.nextToken();

        result = representedEntity();
        if (result != null) {
            traceExit("representingWhat");
            return result;
        }

        if (crtToken.tokenId == RuleScanner.PM_AND) {
            traceConsume();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_ASCS) {
                traceExit("representingWhat");
                return signalError(crtToken.tokenValue, RuleScanner.PM_ASCS);
            }
            traceConsume();
            semopSetAndAscs();
            crtToken = myScanner.nextToken();
        }
        return null;
    }

    // <represented entity> ::= base |
    //                          policy <name or function call> |
    //                          user [attribute] <name or function call> |
    //                          object [attribute] <name or function call>
    private String representedEntity() {
        String result = null;

        traceEntry("representedEntity");

        if (crtToken.tokenId == RuleScanner.PM_BASE) {
            traceConsume();
            semopSetTypeBase();
            semopReprBase();
            crtToken = myScanner.nextToken();
            traceExit("representedEntity");
            return null;
        }

        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            semopSetTypeUser();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId == RuleScanner.PM_ATTR) {
                traceConsume();
                semopUpdateType();
                crtToken = myScanner.nextToken();
            }
        } else if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            semopSetTypeObject();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId == RuleScanner.PM_ATTR) {
                traceConsume();
                semopUpdateType();
                crtToken = myScanner.nextToken();
            }
        } else if (crtToken.tokenId == RuleScanner.PM_POLICY) {
            traceConsume();
            semopSetTypePolicy();
            crtToken = myScanner.nextToken();
        } else {
            traceExit("representedEntity");
            return signalError(crtToken.tokenValue, RuleScanner.PM_USER,
                    RuleScanner.PM_OBJECT, RuleScanner.PM_POLICY, RuleScanner.PM_BASE);
        }

        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("representedEntity");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }
        semopReprEntityInit();
        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("representedEntity");
            return result;
        }
        semopReprEntityFinal();
        return result;
    }

    // <with properties> ::= [with property <property set>]
    private String withProperties() {
        String result = null;

        traceEntry("withProperties");
        if (crtToken.tokenId != RuleScanner.PM_WITH) {
            traceExit("withProperty");
            return null;
        }

        // Consume 'with'.
        traceConsume();
        crtToken = myScanner.nextToken();

        // Check for and consume 'property'
        if (crtToken.tokenId != RuleScanner.PM_PROPERTY) {
            traceExit("withProperty");
            return signalError(crtToken.tokenValue, RuleScanner.PM_PROPERTY);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        result = propertySet();
        traceExit("withProperty");
        return result;
    }

    // <property set> ::= <name of function call> {, <name or function call>}
    private String propertySet() {
        String result = null;

        traceEntry("propertySet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD) {
                traceExit("propertySet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
            }

            semopSetTypeUnknown();
            semopPropSetInit();
            result = nameOrFunctionCall();
            if (result != null) {
                traceExit("propertySet");
                return result;
            }
            semopPropSetFinal();

            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("propertySet");
        return null;
    }

    // <create where> ::= [in <create container>]
    private String createWhere() {
        String result = null;

        traceEntry("createWhere");
        if (crtToken.tokenId != RuleScanner.PM_IN) {
            traceExit("createWhere");
            return null;
        }

        // Consume 'in'.
        traceConsume();
        crtToken = myScanner.nextToken();

        result = createContainer();
        return result;
    }

    // <create container> ::= base |
    //                        policy <name or function call> |
    //                        user attribute <name or function call> |
    //                        object attribute <name or function call>
    private String createContainer() {
        String result = null;

        traceEntry("createContainer");

        if (crtToken.tokenId == RuleScanner.PM_BASE) {
            traceConsume();
            semopSetTypeBase();
            semopCreateContBase();
            crtToken = myScanner.nextToken();
            traceExit("createContainer");
            return null;
        }

        if (crtToken.tokenId == RuleScanner.PM_USER) {
            traceConsume();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_ATTR) {
                traceExit("createContainer");
                return signalError(crtToken.tokenValue, RuleScanner.PM_ATTR);
            }
            traceConsume();
            semopSetTypeUattr();
            crtToken = myScanner.nextToken();
        } else if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_ATTR) {
                traceExit("createContainer");
                return signalError(crtToken.tokenValue, RuleScanner.PM_ATTR);
            }
            traceConsume();
            semopSetTypeOattr();
            crtToken = myScanner.nextToken();
        } else if (crtToken.tokenId == RuleScanner.PM_POLICY) {
            traceConsume();
            semopSetTypePolicy();
            crtToken = myScanner.nextToken();
        } else {
            traceExit("createContainer");
            return signalError(crtToken.tokenValue, RuleScanner.PM_USER,
                    RuleScanner.PM_OBJECT, RuleScanner.PM_POLICY, RuleScanner.PM_BASE);
        }

        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("createContainer");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }
        semopCreateContInit();
        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("createContainer");
            return result;
        }
        semopCreateContFinal();
        return result;
    }

    // <deny action> ::= deny <deny to> <deny what> <deny on>
    private String denyAction() {
        String result = null;

        traceEntry("denyAction");
        if (crtToken.tokenId != RuleScanner.PM_DENY) {
            traceExit("denyAction");
            return signalError(crtToken.tokenValue, RuleScanner.PM_DENY);
        }

        traceConsume();
        semopSetActionDeny();
        crtToken = myScanner.nextToken();

        result = denyTo();
        if (result != null) {
            traceExit("denyAction");
            return result;
        }

        result = denyWhat();
        if (result != null) {
            traceExit("denyAction");
            return result;
        }

        result = denyOn();
        if (result != null) {
            traceExit("denyAction");
            return result;
        }

        traceExit("denyAction");
        return result;
    }

    // <deny to> ::= user [attribute] <name or function call> [intrasession] |
    //               any user |
    //               session <name or function call> |
    //               process <name or function call>
    private String denyTo() {
        String result = null;

        traceEntry("denyTo");

        if (crtToken.tokenId == RuleScanner.PM_ANY) {
            // any user
            traceConsume();
            semopSetTypeUser();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_USER) {
                traceExit("denyTo");
                return signalError(crtToken.tokenValue, RuleScanner.PM_USER);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
            traceExit("denyTo");
            return result;
        } else if (crtToken.tokenId == RuleScanner.PM_USER) {
            // user [attribute]...
            traceConsume();
            semopSetTypeUser();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId == RuleScanner.PM_ATTR) {
                traceConsume();
                semopUpdateType();
                crtToken = myScanner.nextToken();
            }

            semopDenyToInit();
            result = nameOrFunctionCall();
            if (result != null) {
                traceExit("denyTo");
                return result;
            }

            if (crtToken.tokenId == RuleScanner.PM_INTRA) {
                semopSetIntraAttrDeny();
                traceConsume();
                crtToken = myScanner.nextToken();
            }
            semopDenyToFinal();
            traceExit("denyTo");
            return result;

        } else if (crtToken.tokenId == RuleScanner.PM_SESSION) {
            // session...
            traceConsume();
            semopSetTypeSession();
            crtToken = myScanner.nextToken();
            semopDenyToInit();

            result = nameOrFunctionCall();
            if (result != null) {
                traceExit("denyTo");
                return result;
            }
            semopDenyToFinal();
            traceExit("denyTo");
            return result;

        } else if (crtToken.tokenId == RuleScanner.PM_PROCESS) {
            // process...
            traceConsume();
            semopSetTypeProcess();
            crtToken = myScanner.nextToken();
            semopDenyToInit();

            result = nameOrFunctionCall();
            if (result != null) {
                traceExit("denyTo");
                return result;
            }
            semopDenyToFinal();
            traceExit("denyTo");
            return result;
        }

        traceExit("denyTo");
        return signalError(crtToken.tokenValue, RuleScanner.PM_SESSION,
                RuleScanner.PM_PROCESS, RuleScanner.PM_USER, RuleScanner.PM_ANY);
    }

    // <deny what> ::= <op prefix> <denied op set>
    // <op prefix> ::= operation | operations
    private String denyWhat() {
        String result = null;

        traceEntry("denyWhat");

        if (crtToken.tokenId != RuleScanner.PM_OPS
                && crtToken.tokenId != RuleScanner.PM_OP) {
            traceExit("denyWhat");
            return signalError(crtToken.tokenValue, RuleScanner.PM_OP, RuleScanner.PM_OPS);
        }

        traceConsume();
        crtToken = myScanner.nextToken();

        result = deniedOpSet();

        traceExit("denyWhat");
        return result;
    }

    // <denied op set> ::= op_name {, op_name}
    private String deniedOpSet() {

        traceEntry("deniedOpSet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD) {
                traceExit("deniedOpSet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
            }

            traceConsume();
            semopDeniedOp();
            crtToken = myScanner.nextToken();

            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("deniedOpSet");
        return null;
    }

    // <deny on> ::= on [intersection of] <obj container set>
    private String denyOn() {
        String result = null;

        traceEntry("denyOn");
        if (crtToken.tokenId != RuleScanner.PM_ON) {
            traceExit("denyOn");
            return signalError(crtToken.tokenValue, RuleScanner.PM_ON);
        }
        traceConsume();
        crtToken = myScanner.nextToken();

        if (crtToken.tokenId == RuleScanner.PM_INTERSECTION) {
            traceConsume();
            semopSetIntersectionDeny();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_OF) {
                traceExit("denyOn");
                return signalError(crtToken.tokenValue, RuleScanner.PM_OF);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }

        result = objContainerSet();

        traceExit("denyOn");
        return result;
    }

    // <obj container set> ::= <obj container> {, <obj container>}
    private String objContainerSet() {
        String result = null;

        traceEntry("objContainerSet");
        while (true) {
            if (crtToken.tokenId != RuleScanner.PM_WORD
                    && crtToken.tokenId != RuleScanner.PM_OBJECT
                    && crtToken.tokenId != RuleScanner.PM_ATTR
                    && crtToken.tokenId != RuleScanner.PM_COMPLEMENT) {
                traceExit("objContainerSet");
                return signalError(crtToken.tokenValue, RuleScanner.PM_WORD,
                        RuleScanner.PM_ATTR, RuleScanner.PM_OBJECT, RuleScanner.PM_COMPLEMENT);
            }
            result = objContainer();
            if (result != null) {
                traceExit("objContainerSet");
                return result;
            }
            if (crtToken.tokenId != RuleScanner.PM_COMMA) {
                break;
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }
        traceExit("objContainerSet");
        return result;
    }

    // <obj container> ::= [complement of] [object] [attribute] <name or function call>
    private String objContainer() {
        String result = null;

        traceEntry("objContainer");
        if (crtToken.tokenId != RuleScanner.PM_OBJECT
                && crtToken.tokenId != RuleScanner.PM_ATTR
                && crtToken.tokenId != RuleScanner.PM_COMPLEMENT
                && crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("denyOn");
            return signalError(crtToken.tokenValue, RuleScanner.PM_OBJECT,
                    RuleScanner.PM_ATTR, RuleScanner.PM_WORD, RuleScanner.PM_COMPLEMENT);
        }
        semopSetTypeOattr();
        if (crtToken.tokenId == RuleScanner.PM_COMPLEMENT) {
            traceConsume();
            semopSetIsComplement();
            crtToken = myScanner.nextToken();
            if (crtToken.tokenId != RuleScanner.PM_OF) {
                traceExit("denyOn");
                return signalError(crtToken.tokenValue, RuleScanner.PM_OF);
            }
            traceConsume();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
            traceConsume();
            semopUpdateType();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId == RuleScanner.PM_ATTR) {
            traceConsume();
            semopUpdateType();
            crtToken = myScanner.nextToken();
        }

        if (crtToken.tokenId != RuleScanner.PM_WORD) {
            traceExit("denyOn");
            return signalError(crtToken.tokenValue, RuleScanner.PM_WORD);
        }

        semopObjContainerInit();

        result = nameOrFunctionCall();
        if (result != null) {
            traceExit("denyOn");
            return result;
        }

        semopObjContainerFinal();
        semopSetIsNotComplement();

        traceExit("denyOn");
        return result;
    }

    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Semantic Operators ///////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    private void semopScript() {
        traceSemop("semopScript");
        scriptName = crtToken.tokenValue;
        //scriptId = generateId();
        if (ServerConfig.myEngine != null) {
            scriptId = ServerConfig.obligationDAO.createScriptRecord(scriptId, scriptName);
            traceSemact("save script " + scriptName + ", " + scriptId);
        }
    }

    private void semopRuleInit() {
        traceSemop("semopRuleInit");

        traceSemact("create the ruleSpec with an empty set of actions");
        ruleSpec = new RuleSpec(generateId());
        ruleSpec.setRank(crtRuleRank++);
        actionSpecs = ruleSpec.getActions();

        traceSemact("set ruleSpec's pattern to a new pattern");
        ruleSpec.setPattern(new PatternSpec(generateId()));
        patternSpec = ruleSpec.getPattern();

        ruleLineNo = myScanner.lineno();
    }

    private void semopSetLabel() {
        traceSemop("semopSetLabel");
        // Set the ruleSpec label.
        traceSemact("set ruleSpec.label = " + crtToken.tokenValue);
        ruleSpec.setLabel(crtToken.tokenValue);
    }

    private void semopSetTypeUnknown() {
        traceSemop("semopSetTypeUnknown");
        defaultType = GlobalConstants.PM_UNKNOWN;
    }

    private void semopSetTypeBase() {
        traceSemop("semopSetTypeBase");
        defaultType = PM_NODE.CONN.value;
    }

    private void semopSetTypePolicy() {
        traceSemop("semopSetTypePolicy");
        defaultType = PM_NODE.POL.value;
    }

    private void semopSetTypeRule() {
        traceSemop("semopSetTypeRule");
        defaultType = GlobalConstants.PM_RULE;
    }

    private void semopSetTypeUser() {
        traceSemop("semopSetTypeUser");
        defaultType = PM_NODE.USER.value;
    }

    private void semopSetTypeObject() {
        traceSemop("semopSetTypeObject");
        defaultType = GlobalConstants.PM_OBJ;
    }

    private void semopSetTypeUattr() {
        traceSemop("semopSetTypeUattr");
        defaultType = PM_NODE.UATTR.value;
    }

    private void semopSetTypeOattr() {
        traceSemop("semopSetTypeOattr");
        defaultType = PM_NODE.OATTR.value;
    }

    private void semopSetTypeSession() {
        traceSemop("semopSetTypeSession");
        defaultType = GlobalConstants.PM_SESSION;
    }

    private void semopSetTypeProcess() {
        traceSemop("semopSetTypeProcess");
        defaultType = GlobalConstants.PM_PROCESS;
    }

    private void semopAdjustTypeForContainer() {
        traceSemop("semopAdjustTypeForContainer");
        if (defaultType.equals(PM_NODE.USER.value)) {
            defaultType = PM_NODE.UATTR.value;
        } else if (defaultType.equals(GlobalConstants.PM_OBJ)) {
            defaultType = PM_NODE.OATTR.value;
        } else if (defaultType.equals(PM_NODE.POL.value)); else if (defaultType.equals(PM_NODE.OATTR.value)); else if (defaultType.equals(PM_NODE.UATTR.value)); else {
            defaultType = PM_NODE.UATTR.value;
        }
        traceSemact("adjusted type to " + defaultType);
    }

    private void semopUpdateType() {
        traceSemop("semopUpdateType");
        if (defaultType.equals(PM_NODE.POL.value)
                || defaultType.equals(PM_NODE.CONN.value)
                || defaultType.equals(PM_NODE.USER.value)
                || defaultType.equals(PM_NODE.UATTR.value)) {
            if (crtToken.tokenId == RuleScanner.PM_USER) {
                defaultType = PM_NODE.USER.value;
            } else if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
                defaultType = GlobalConstants.PM_OBJ;
            } else if (crtToken.tokenId == RuleScanner.PM_ATTR) {
                defaultType = PM_NODE.UATTR.value;
            } else if (crtToken.tokenId == RuleScanner.PM_POLICY) {
                defaultType = PM_NODE.POL.value;
            } else if (crtToken.tokenId == RuleScanner.PM_BASE) {
                defaultType = PM_NODE.CONN.value;
            } else {
                defaultType = GlobalConstants.PM_UNKNOWN;
            }
        } else if (defaultType.equals(GlobalConstants.PM_OBJ)
                || defaultType.equals(PM_NODE.OATTR.value)) {
            if (crtToken.tokenId == RuleScanner.PM_USER) {
                defaultType = PM_NODE.USER.value;
            } else if (crtToken.tokenId == RuleScanner.PM_OBJECT) {
                defaultType = GlobalConstants.PM_OBJ;
            } else if (crtToken.tokenId == RuleScanner.PM_ATTR) {
                defaultType = PM_NODE.OATTR.value;
            } else if (crtToken.tokenId == RuleScanner.PM_POLICY) {
                defaultType = PM_NODE.POL.value;
            } else if (crtToken.tokenId == RuleScanner.PM_BASE) {
                defaultType = PM_NODE.CONN.value;
            } else {
                defaultType = GlobalConstants.PM_UNKNOWN;
            }
        } else {
            defaultType = GlobalConstants.PM_UNKNOWN;
        }
        traceSemact("updated type to " + defaultType);
    }

    private void semopAUser() {
        //traceSemop("semopAUser");
        // Create a user spec with name = crt. token and type = user.
        //traceSemact("create userSpec(\"" + crtToken.tokenValue + "\", \"" +
        //  PM_NODE.USER + "\")");
        //traceSemact("store userSpec into patternSpec.userSpecs");
        UserSpec userSpec = new UserSpec(crtToken.tokenValue, PM_NODE.USER.value);
        patternSpec.getUserSpecs().add(userSpec);
    }

    private void semopAnActiveUattr() {
        //traceSemop("semopAnActiveUattr");
        // Create a user spec with name = crt. token and type = active user attribute.
        //traceSemact("create userSpec(\"" + crtToken.tokenValue + "\", \"" +
        //  PM_NODE.AUATTR + "\")");
        //traceSemact("store userSpec into hsUserSpecs");
        UserSpec userSpec = new UserSpec(crtToken.tokenValue, PM_NODE.AUATTR.value);
        patternSpec.getUserSpecs().add(userSpec);
    }

    private void semopAUattr() {
        //traceSemop("semopAUattr");
        // Create a user spec with name = crt. token and type = user attribute.
        //traceSemact("create userSpec(\"" + crtToken.tokenValue + "\", \"" +
        //  PM_NODE.UATTR + "\")");
        //traceSemact("store userSpec into hsUserSpecs");
        UserSpec userSpec = new UserSpec(crtToken.tokenValue, PM_NODE.UATTR.value);
        patternSpec.getUserSpecs().add(userSpec);
    }

    // Create a UserSpec with type=PM_SESSION and id=the session id.
    // NOte that is different from the other semantic operators dealing
    // with UserSpecs by the fact that the spec will contain THE SESSION ID,
    // not its name. Later on, the engine will add the session name to the
    // spec.
    private void semopASession() {
        //traceSemop("semopASession");
        // Create a user spec with id = crt. token and type = session.
        //traceSemact("create userSpec(\"" + crtToken.tokenValue + "\", \"" +
        //  GlobalConstants.PM_SESSION + "\")");
        //traceSemact("store userSpec into hsUserSpecs");
        UserSpec userSpec = new UserSpec(GlobalConstants.PM_SESSION);
        userSpec.setId(crtToken.tokenValue);
        patternSpec.getUserSpecs().add(userSpec);
    }

    // Create a UserSpec with type=PM_PROCESS and id=the process id.
    // Note that is different from the other semantic operators dealing
    // with UserSpecs by the fact that the pid and name are idientical
    private void semopAProcess() {
        //traceSemop("semopAProcess");
        // Create a user spec with id = name = crt. token and type = process.
        //traceSemact("create userSpec(\"" + crtToken.tokenValue + "\", \"" +
        //  GlobalConstants.PM_PROCESS + "\")");
        //traceSemact("store userSpec into hsUserSpecs");
        UserSpec userSpec = new UserSpec(crtToken.tokenValue, GlobalConstants.PM_PROCESS);
        userSpec.setId(crtToken.tokenValue);
        patternSpec.getUserSpecs().add(userSpec);
    }

    private void semopPcSpecInit() {
        //traceSemop("semopPcSpecInit");
        // Set bPcIsAny to true and bUserIsActive according to the crt token.
        patternSpec.setAny(true);
        patternSpec.setActive(crtToken.tokenId == RuleScanner.PM_ACTIVE);

        //traceSemact("set isAny = " + patternSpec.isAny());
        //traceSemact("set isActive = " + patternSpec.isActive());
    }

    private void semopAPc() {
        //traceSemop("semopAPc");
        // Create a pc spec with name = crt. token and type = policy class.
        //traceSemact("create pcSpec(\"" + crtToken.tokenValue + "\", \"" +
        //  PM_NODE.POL.value + "\")");
        //traceSemact("store pcSpec into hsPcSpecs");
        PcSpec pcSpec = new PcSpec(crtToken.tokenValue, PM_NODE.POL.value);
        patternSpec.getPcSpecs().add(pcSpec);
    }

    private void semopEachPc() {
        //traceSemop("semopEachPc");
        // Set isAny to false.
        patternSpec.setAny(false);
        //traceSemact("set isAny to " + patternSpec.getAny());
    }

    private void semopAnOp() {
        //traceSemop("semopAnOp");
        // Create an op spec with name = crt. token.
        //traceSemact("create opSpec(\"" + crtToken.tokenValue + "\", \"" +
        //  GlobalConstants.PM_OP + "\")");
        //traceSemact("store opSpec into hsOpSpecs");
        OpSpec opSpec = new OpSpec(crtToken.tokenValue, GlobalConstants.PM_OP);
        patternSpec.getOpSpecs().add(opSpec);
    }

    private void semopObj() {
        traceSemop("semopObj");
        traceSemact("create objSpec(\"" + crtToken.tokenValue + "\", \""
                + GlobalConstants.PM_OBJ + "\")");
        traceSemact("save objSpec to pattern's objSpecs");
        ObjSpec objSpec = new ObjSpec(crtToken.tokenValue, GlobalConstants.PM_OBJ);
        patternSpec.getObjSpecs().add(objSpec);
    }

    private void semopObjAsCont() {
        traceSemop("semopObjAsCont");
        traceSemact("create contSpec(\"" + crtToken.tokenValue + "\", \""
                + GlobalConstants.PM_OBJ + "\")");
        traceSemact("save contSpec to pattern's contSpecs");
        ContSpec contSpec = new ContSpec(crtToken.tokenValue, GlobalConstants.PM_OBJ);
        patternSpec.getContSpecs().add(contSpec);
    }

    private void semopAnOattr() {
        traceSemop("semopAnOattr");
        traceSemact("create contSpec(\"" + crtToken.tokenValue + "\", \""
                + PM_NODE.OATTR + "\")");
        traceSemact("save contSpec to pattern's contSpecs");
        ContSpec contSpec = new ContSpec(crtToken.tokenValue, PM_NODE.OATTR.value);
        patternSpec.getContSpecs().add(contSpec);
    }

    private void semopARecord() {
        traceSemop("semopARecord");
        traceSemact("create contSpec(\"" + crtToken.tokenValue + "\", \""
                + GlobalConstants.PM_RECORD + "\")");
        traceSemact("save contSpec to pattern's contSpecs");
        ContSpec contSpec = new ContSpec(crtToken.tokenValue, GlobalConstants.PM_RECORD);
        patternSpec.getContSpecs().add(contSpec);
    }

    private void semopAnyRecord() {
        traceSemop("semopAnyRecord");
        traceSemact("create contSpec(\"" + "*" + "\", \""
                + GlobalConstants.PM_RECORD + "\")");
        traceSemact("save contSpec to pattern's contSpecs");
        ContSpec contSpec = new ContSpec("*", GlobalConstants.PM_RECORD);
        patternSpec.getContSpecs().add(contSpec);
    }

    private void semopAClass() {
        traceSemop("semopAClass");
        traceSemact("create contSpec(\"" + crtToken.tokenValue + "\", \""
                + GlobalConstants.PM_OBJ_CLASS + "\")");
        traceSemact("save contSpec to pattern's contSpecs");
        ContSpec contSpec = new ContSpec(crtToken.tokenValue, GlobalConstants.PM_OBJ_CLASS);
        patternSpec.getContSpecs().add(contSpec);
    }

    private void semopSetActionGrant() {
        traceSemop("semopSetActionGrant");
        // Set the action spec type "grant".
        traceSemact("set actionSpec.type(\"" + RuleScanner.PM_VALUE_GRANT + "\")");
        actionSpec.setType(RuleScanner.PM_VALUE_GRANT);
    }

    //FIXME Verify - The var opndSpec is created but never added to the opnds2List, is this a problem?
    @SuppressWarnings("serial")
    private void semopGrantedOp() {
        traceSemop("semopGrantedOp");
        // Create an OpndSpec with type = "op", origName = crtToken.tokenValue,
        // origId = "". Store the opndSpec in the actionSpec's opnds2 set.
        traceSemact("create opndSpec(\"" + GlobalConstants.PM_OP + "\", \""
                + crtToken.tokenValue + "\", \"\")");
        traceSemact("store opndSpec into actionSpec's opnds2 hash set.");
        //Had to change the the way OpndSpec was created to account for the fact that origId,
        //  the fourth parameter, cannot be blank when inserted into LDAP.
        //  Since it doesn't appear to be used we input a placeholder.
        //  FIXME Event Breaking candidate.
        final OpndSpec opndSpec = new OpndSpec(generateId(), GlobalConstants.PM_OP, crtToken.tokenValue, "NULL");
        List<OpndSpec> opnds2List = new ArrayList<OpndSpec>(1);
        //  FIXME Event Breaking candidate.  This was not being added to the opnds2List before I inserted this line.
        opnds2List.add(opndSpec);
        //  FIXME We are now adding an empty list to the second operand set of the actionSpec
        actionSpec.getOpnds2().add(opnds2List);
    }

    private void semopUattrSpecInit() {
        traceSemop("semopUattrSpecInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to a user attribute specified by name,
        // or a tree of OpndSpec corresponding to a function call.
        traceSemact("create vector vOpnds = empty");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }
        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopUattrSpecFinal() {
        traceSemop("semopUattrSpecFinal");

        // The vector vOpnds contains a description of a first operand of the
        // the grant action (only one user attribute or a function call).
        // Store vOpnds into the HashSet opnds1 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds1 hash set.");
        actionSpec.getOpnds1().add(vOpnds);
    }

    private void semopNameOrFunctionCallInit() {
        traceSemop("semopNameOrFunctionCallInit");

        // Create an OpndSpec with name = crt. token value, type = default type,
        // parent = top of parent stack, children = empty.
        OpndSpec opndSpec = new OpndSpec(generateId(), crtToken.tokenValue);
        opndSpec.setType(defaultType);
        opndSpec.setIsComplement(isComplement);
        int parent = parentStack.get(parentStack.size() - 1).intValue();
        opndSpec.setParent(parent);

        traceSemact("create opndSpec name = " + opndSpec.getOrigName());
        traceSemact("                type = " + opndSpec.getType());
        traceSemact("                parent = " + opndSpec.getParent());

        // Insert it into vOpnds at index globalFree.
        traceSemact("insert opndSpec into vOpnds at " + globalFree);
        vOpnds.add(globalFree, opndSpec);

        // Set globalCrt = globalFree.
        traceSemact("set globalCrt = " + globalFree);
        globalCrt = globalFree;

        // If (parent >= 0) then add globalCrt as a child of the item at index = parent.
        // Note that the children order is important.
        if (parent >= 0) {
            traceSemact("add " + globalCrt + " as child of item at " + parent);
            OpndSpec os = vOpnds.get(parent);
            os.addChild(globalCrt);
        }

        // Increment globalFree.
        traceSemact("increment globalFree to " + globalFree);
        globalFree++;
    }

    private void semopArgPartInit() {
        traceSemop("semopArgPartInit");

        // Set type = function call in the item at globalCrt
        traceSemact("set isFunction = true in item at " + globalCrt);
        OpndSpec os = vOpnds.get(globalCrt);
        os.setIsFunction(true);

        // Make the item at index = globalCrt parent for the arguments in the arg list
        traceSemact("set parent = " + globalCrt);
        parentStack.add(Integer.valueOf(globalCrt));
        printParentStack();
    }

    private void semopArgPartFinal() {
        traceSemop("semopArgPartFinal");

        // Discard the current parent from stack
        parentStack.remove(parentStack.size() - 1);
        printParentStack();

        int parent = parentStack.get(parentStack.size() - 1).intValue();
        traceSemact("set parent = " + parent);
    }

    private void semopGrantOnInit() {
        traceSemop("semopGrantOnInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to an object or object attribute
        // specified by its name, or a tree of OpndSpec corresponding to a function call.
        traceSemact("create empty vector vOpnds");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }

        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopGrantOnFinal() {
        traceSemop("semopGrantOnFinal");

        // The vector vOpnds contains a description of a third operand of the
        // the grant action (only one user attribute or a function call).
        // Store vOpnds into the HashSet opnds3 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds3 hash set.");
        actionSpec.getOpnds3().add(vOpnds);
    }

    private void semopSetActionCreate() {
        traceSemop("semopSetActionCreate");
        // Set action spec type "create".
        traceSemact("set actionSpec.type(\"" + RuleScanner.PM_VALUE_CREATE + "\")");
        actionSpec.setType(RuleScanner.PM_VALUE_CREATE);
    }

    private void semopCreateWhatInit() {
        traceSemop("semopCreateWhatInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to a user attribute specified by name,
        // or a tree of OpndSpec corresponding to a function call.
        traceSemact("create vector vOpnds = empty");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }
        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopCreateWhatFinal() {
        traceSemop("semopCreateWhatFinal");

        // The vector vOpnds contains a description of a first operand of the
        // the create action (either a single entity or a function call).
        // Store vOpnds into the HashSet opnds1 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds1 hash set.");
        actionSpec.getOpnds1().add(vOpnds);
    }

    private void semopReprBase() {
        traceSemop("semopReprBase");

        // Create an empty vector of OpndSpecs that will contain
        // a single OpndSpec corresponding to the base node.
        traceSemact("create empty vector vOpnds");

        // Create an OpndSpec with name = "base" (can be extracted from crt. token value),
        // type = PM_NODE_CONN.value, parent = -1.
        OpndSpec opndSpec = new OpndSpec(generateId(), crtToken.tokenValue);
        //FIXME - should this be set to the type and parents above
        opndSpec.setType(defaultType);
        opndSpec.setParent(-1);

        traceSemact("create opndSpec name = " + opndSpec.getOrigName());
        traceSemact("                type = " + opndSpec.getType());
        traceSemact("                parent = " + opndSpec.getParent());

        // Insert it into vOpnds.
        traceSemact("insert opndSpec into vOpnds at 0");
        vOpnds.add(0, opndSpec);

        // Store vOpnds into the HashSet opnds2 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds2 hash set.");
        actionSpec.getOpnds2().add(vOpnds);
    }

    private void semopReprEntityInit() {
        traceSemop("semopReprEntityInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to an entity specified by name,
        // or a tree of OpndSpecs corresponding to a function call.
        traceSemact("create vector vOpnds = empty");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }
        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopReprEntityFinal() {
        traceSemop("semopCreateWhatFinal");

        // The vector vOpnds contains a description of a second operand of the
        // the create action (either a single entity or a function call).
        // Store vOpnds into the HashSet opnds2 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds2 hash set.");
        actionSpec.getOpnds2().add(vOpnds);
    }

    private void semopSetAndAscs() {
        traceSemop("semopSetAndAscs");

        // Set isSubgraph = true in the item at globalCrt
        traceSemact("set isSubgraph = true in item at " + globalCrt);
        OpndSpec os = vOpnds.get(globalCrt);
        os.setIsSubgraph(true);
    }

    private void semopPropSetInit() {
        traceSemop("semopPropSetInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to an entity specified by name,
        // or a tree of OpndSpecs corresponding to a function call.
        traceSemact("create vector vOpnds = empty");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }
        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopPropSetFinal() {
        traceSemop("semopPropSetFinal");
        // The vector vOpnds contains a description of a third operand of the
        // the create action (either a single entity or a function call).
        // Store vOpnds into the HashSet opnds3 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds3 hash set.");
        actionSpec.getOpnds3().add(vOpnds);
    }

    private void semopCreateContBase() {
        traceSemop("semopCreateContBase");

        // Create an empty vector of OpndSpecs that will contain
        // a single OpndSpec corresponding to the base node.
        traceSemact("create empty vector vOpnds");
        vOpnds = new ArrayList<OpndSpec>();

        // Create an OpndSpec with name = "base" (can be extracted from crt. token value),
        // type = PM_NODE_CONN.value, parent = -1.
        OpndSpec opndSpec = new OpndSpec(generateId(), crtToken.tokenValue);
        opndSpec.setType(defaultType);
        opndSpec.setParent(-1);

        traceSemact("create opndSpec name = " + opndSpec.getOrigName());
        traceSemact("                type = " + opndSpec.getType());
        traceSemact("                parent = " + opndSpec.getParent());

        // Insert it into vOpnds.
        traceSemact("insert opndSpec into vOpnds at 0");
        vOpnds.add(0, opndSpec);

        // Store vOpnds into the HashSet opnds4 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds4 hash set.");
        actionSpec.getOpnds4().add(vOpnds);
    }

    private void semopCreateContInit() {
        traceSemop("semopCreateContInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to a user attribute specified by name,
        // or a tree of OpndSpec corresponding to a function call.
        traceSemact("create vector vOpnds = empty");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }
        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopCreateContFinal() {
        traceSemop("semopCreateContFinal");

        // The vector vOpnds contains a description of a first operand of the
        // the create action (either a single entity or a function call).
        // Store vOpnds into the HashSet opnds1 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds4 hash set.");
        actionSpec.getOpnds4().add(vOpnds);
    }
    /*
    private void semopActionFinal() {
    traceSemop("semopActionFinal");
    
    // Add the actionSpec to the HashSet of action specs.
    traceSemact("add actionSpec to the vector of ActionSpecs");
    actionSpecs.add(actionSpec);
    }
     */

    private void semopCondActionInit() {
        traceSemop("semopCondActionInit");
        // Open an action spec with empty type and empty sets of operands.
        traceSemact("create actionSpec");
        actionSpec = new ActionSpec(generateId());
    }

    private void semopCondInit() {
        traceSemop("semopCondInit");
        // Open an empty condition spec in the current action spec.
        traceSemact("create condSpec");
        condSpec = new CondSpec(generateId());
        actionSpec.setCondSpec(condSpec);
    }

    private void semopSetCondNegated() {
        traceSemop("semopSetCondNegated");
        // Set the condition spec negated.
        traceSemact("set condSpec.negated(\"true\")");
        condSpec.setNegated(true);
    }

    private void semopSetCondExist() {
        traceSemop("semopSetCondExist");
        // Set the condition spec type = exist.
        traceSemact("set condSpec.type(\"exists\")");
        condSpec.setType(RuleScanner.PM_VALUE_EXISTS);
    }

    private void semopCondEntityInit() {
        traceSemop("semopCondEntityInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to a user or uattr or object or oattr or
        // policy specified by its name, or a tree of OpndSpec corresponding to
        // a function call.
        traceSemact("create empty vector vOpnds");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }

        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopCondEntityFinal() {
        traceSemop("semopCondEntityFinal");

        // The vector vOpnds contains a description of a first operand of an
        // action condition.
        // Store vOpnds into the HashSet opnds1 of condSpec.
        traceSemact("store vector vOpnds into condSpec's opnds1 hash set.");
        condSpec.opnds1.add(vOpnds);
    }

    private void semopSetActionAssign() {
        traceSemop("semopSetActionAssign");
        // Set the action type to "assign".
        traceSemact("set actionSpec.type(\"" + RuleScanner.PM_VALUE_ASSIGN + "\")");
        actionSpec.setType(RuleScanner.PM_VALUE_ASSIGN);
    }

    private void semopAssignWhatInit() {
        traceSemop("semopAssignWhatInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to a user or uattr or object or oattr
        // specified by its name, or a tree of OpndSpec corresponding to a function call.
        traceSemact("create empty vector vOpnds");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }

        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopAssignWhatFinal() {
        traceSemop("semopAssignWhatFinal");

        // The vector vOpnds contains a description of a first operand of the
        // the assign action (only one user attribute or a function call).
        // Store vOpnds into the HashSet opnds1 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds1 hash set.");
        actionSpec.getOpnds1().add(vOpnds);
    }

    private void semopModelEntityInit() {
        traceSemop("semopModelEntityInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to a user or uattr or object or oattr
        // specified by its name, or a tree of OpndSpec corresponding to a function call.
        traceSemact("create empty vector vOpnds");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }

        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopModelEntityFinal() {
        traceSemop("semopModelEntityFinal");

        // The vector vOpnds contains a description of a first operand of the
        // the assign action (only one user attribute or a function call).
        // Store vOpnds into the HashSet opnds2 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds2 hash set.");
        actionSpec.getOpnds2().add(vOpnds);

        // Change actionSpec's type to "assign like".
        traceSemact("change actionSpec type to " + RuleScanner.PM_VALUE_ASSIGN + " "
                + RuleScanner.PM_VALUE_LIKE);
        actionSpec.setType(RuleScanner.PM_VALUE_ASSIGN + " " + RuleScanner.PM_VALUE_LIKE);
    }

    private void semopPolicyContInit() {
        traceSemop("semopPolicyContInit");
        // Create an empty vector of OpndSpecs that will contain either
        // a single OpndSpec corresponding to a policy specified by its name,
        // or a tree of OpndSpec corresponding to a function call.
        traceSemact("create empty vector vOpnds");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }

        parentStack.add(Integer.valueOf(-1));
        printParentStack();

        traceSemact("set defaultType = " + PM_NODE.POL.value);
        defaultType = PM_NODE.POL.value;
    }

    private void semopPolicyContFinal() {
        traceSemop("semopPolicyContFinal");

        // The vector vOpnds contains a description of a first operand of the
        // the assign action (only one user attribute or a function call).
        // Store vOpnds into the HashSet opnds2 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds2 hash set.");
        actionSpec.getOpnds2().add(vOpnds);
    }

    private void semopBaseCont() {
        traceSemop("semopBaseCont");

        // Create an empty vector of OpndSpecs that will contain
        // a single OpndSpec corresponding to the base node.
        traceSemact("create empty vector vOpnds");
        vOpnds = new ArrayList<OpndSpec>();

        // Create an OpndSpec with name = "base" (can be extracted from crt. token value),
        // type = PM_NODE_CONN.value, parent = -1.
        OpndSpec opndSpec = new OpndSpec(generateId(), crtToken.tokenValue);
        opndSpec.setType(defaultType);
        opndSpec.setParent(-1);

        traceSemact("create opndSpec name = " + opndSpec.getOrigName());
        traceSemact("                type = " + opndSpec.getType());
        traceSemact("                parent = " + opndSpec.getParent());

        // Insert it into vOpnds.
        traceSemact("insert opndSpec into vOpnds at 0");
        vOpnds.add(0, opndSpec);

        // Store vOpnds into the HashSet opnds2 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds2 hash set.");
        actionSpec.getOpnds2().add(vOpnds);
    }

    private void semopAttrContInit() {
        traceSemop("semopAttrContInit");
        // Create an empty vector of OpndSpecs that will contain either
        // a single OpndSpec corresponding to a policy specified by its name,
        // or a tree of OpndSpec corresponding to a function call.
        traceSemact("create empty vector vOpnds");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }

        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopAttrContFinal() {
        traceSemop("semopAttrContFinal");

        // The vector vOpnds contains a description of a first operand of the
        // the assign action (only one user attribute or a function call).
        // Store vOpnds into the HashSet opnds2 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds2 hash set.");
        actionSpec.getOpnds2().add(vOpnds);
    }

    private void semopCondActionFinal() {
        traceSemop("semopCondActionFinal");

        // Add the actionSpec to the HashSet of action specs.
        traceSemact("add actionSpec to actionSpecs");
        actionSpecs.add(actionSpec);
    }

    private String semopRuleFinal() throws Exception{
        traceSemop("semopRuleFinal");
        traceSemact("write rule spec to the active directory");
        String result = null;
        if (ServerConfig.myEngine != null) {
            result = ServerConfig.obligationDAO.generateRuleCode(ruleSpec, scriptId, lastRuleId);
        }
        if (result != null) {
            return signalSemError(result);
        }
        lastRuleId = ruleSpec.getId();
        return null;
    }

    private void semopSetActionDeny() {
        traceSemop("semopSetActionDeny");
        // Set the action spec type "deny".
        traceSemact("set actionSpec.type(\"" + RuleScanner.PM_VALUE_DENY + "\")");
        actionSpec.setType(RuleScanner.PM_VALUE_DENY);
    }

    private void semopDenyToInit() {
        traceSemop("semopDenyToInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to a user, user attribute, or session
        // specified by name,
        // or a tree of OpndSpec corresponding to a function call.
        traceSemact("create vector vOpnds = empty");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }
        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopDenyToFinal() {
        traceSemop("semopDenyToFinal");

        // The vector vOpnds contains a description of a first operand of the
        // the deny action (either a single entity or a function call).
        // Store vOpnds into the HashSet opnds1 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds1 hash set.");
        actionSpec.getOpnds1().add(vOpnds);
    }

    private void semopSetIntraAttrDeny() {
        actionSpec.setIsIntrasession(true);
    }

    private void semopDeniedOp() {
        traceSemop("semopDeniedOp");
        // Create an OpndSpec with type = "op", origName = crtToken.tokenValue,
        // origId = "". Store the opndSpec in the actionSpec's opnds2 set.
        traceSemact("create opndSpec(\"" + GlobalConstants.PM_OP + "\", \""
                + crtToken.tokenValue + "\", \"\")");
        traceSemact("store opndSpec into actionSpec's opnds2 hash set.");
        //Had to change the the way OpndSpec was created to account for the fact that origId,
        //  the fourth parameter, cannot be blank when inserted into LDAP.
        //  Since it doesn't appear to be used we input a placeholder
        // FIXME - Event Breaking Candidate
        OpndSpec opndSpec = new OpndSpec(generateId(), GlobalConstants.PM_OP, crtToken.tokenValue, "NULL");
        List<OpndSpec> opndSpecList = new ArrayList<OpndSpec>(1);
        opndSpecList.add(opndSpec);
        actionSpec.getOpnds2().add(opndSpecList);
    }

    private void semopSetIntersectionDeny() {
        actionSpec.setIsIntersection(true);
    }

    private void semopObjContainerInit() {
        traceSemop("semopObjContainerInit");
        // Create an empty vector of OpndSpecs. Such a vector will contain either
        // a single OpndSpec corresponding to an object or object attribute
        // specified by its name, or a tree of OpndSpec corresponding to a function call.
        traceSemact("create empty vector vOpnds");
        vOpnds = new ArrayList<OpndSpec>();

        traceSemact("init globalFree = 0, globalCrt = -1");
        globalFree = 0;
        globalCrt = -1;

        traceSemact("init parent stack");
        if (parentStack == null) {
            parentStack = new ArrayList<Integer>();
        } else {
            parentStack.clear();
        }

        parentStack.add(Integer.valueOf(-1));
        printParentStack();
    }

    private void semopObjContainerFinal() {
        traceSemop("semopObjContainerFinal");

        // The vector vOpnds contains a description of a third operand of the
        // the grant action (only one user attribute or a function call).
        // Store vOpnds into the HashSet opnds3 of actionSpec.
        traceSemact("store vector vOpnds into actionSpec's opnds3 hash set.");
        actionSpec.getOpnds3().add(vOpnds);
    }

    private void semopSetIsComplement() {
        traceSemop("semopSetIsComplement");
        isComplement = true;
    }

    private void semopSetIsNotComplement() {
        traceSemop("semopSetIsNotComplement");
        isComplement = false;
    }

    private void semopSetActionDeassign() {
        traceSemop("semopSetActionDeassign");
        // Set the action type to "delete assignment".
        traceSemact("set actionSpec.type(\"" + RuleScanner.PM_VALUE_DELETE + " "
                + RuleScanner.PM_VALUE_ASSIGNMENT + "\"");
        actionSpec.setType(RuleScanner.PM_VALUE_DELETE + " " + RuleScanner.PM_VALUE_ASSIGNMENT);
    }

    private void semopSetActionDeleteDeny() {
        traceSemop("semopSetActionDeleteDeny");
        // Set the action type to "delete deny".
        traceSemact("set actionSpec.type(\"" + RuleScanner.PM_VALUE_DELETE + " "
                + RuleScanner.PM_VALUE_DENY + "\"");
        actionSpec.setType(RuleScanner.PM_VALUE_DELETE + " " + RuleScanner.PM_VALUE_DENY);
    }

    private void semopSetActionDeleteRule() {
        traceSemop("semopSetActionDeleteRule");
        // Set the action type to "delete rule".
        traceSemact("set actionSpec.type(\"" + RuleScanner.PM_VALUE_DELETE + " "
                + RuleScanner.PM_VALUE_RULE + "\"");
        actionSpec.setType(RuleScanner.PM_VALUE_DELETE + " " + RuleScanner.PM_VALUE_RULE);
    }

    private void semopALabel() {
        traceSemop("semopALabel");
        // Create an OpndSpec with type = "l", origName = crtToken.tokenValue,
        // origId = "". Store the opndSpec in the actionSpec's opnds1 set.
        traceSemact("create opndSpec(\"" + GlobalConstants.PM_LABEL + "\", \""
                + crtToken.tokenValue + "\", \"\")");
        traceSemact("store opndSpec into actionSpec's opnds1 hash set.");
        //Had to change the the way OpndSpec was created to account for the fact that origId,
        //  the fourth parameter, cannot be blank when inserted into LDAP.
        //  Since it doesn't appear to be used we input a placeholder.
        //  Fixme event breaking candidate.
        OpndSpec opndSpec = new OpndSpec(generateId(), GlobalConstants.PM_LABEL, crtToken.tokenValue, "NULL");
        List<OpndSpec> opndSpecList = new ArrayList<OpndSpec>(1);
        opndSpecList.add(opndSpec);
        actionSpec.getOpnds1().add(opndSpecList);
    }

    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Utility Methods //////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    private String generateId() {
        /*RandomGUID myGUID = new RandomGUID();
        return myGUID.toStringNoDashes();*/
        return String.valueOf((int)(100000000 + Math.random() * 900000000));
    }

    private void printParentStack() {
        System.out.println("Parent stack is (base first): {");
        for (int i = 0; i < parentStack.size(); i++) {
            int parent = parentStack.get(i).intValue();
            System.out.println("                              " + parent);
        }
        System.out.println("                              }");
    }

    private String signalSemError(String err) {
        return ("1 Error around line " + ruleLineNo + ": " + err);
    }

    private String signalError(String found, int expected) {
        return "2 Error around line " + myScanner.lineno() + ": token \""
                + RuleScanner.getTokenValue(expected) + "\" expected. Found \"" + found + "\"!";
    }

    private String signalError(String found, int expected, int expected2) {
        return "3 Error around line " + myScanner.lineno() + ": token \""
                + RuleScanner.getTokenValue(expected) + "\" or \""
                + RuleScanner.getTokenValue(expected2) + "\" expected. Found \"" + found + "\"!";
    }

    private String signalError(String found, int expected, int expected2, int expected3) {
        return "4 Error around line " + myScanner.lineno() + ": token \""
                + RuleScanner.getTokenValue(expected) + "\" or \""
                + RuleScanner.getTokenValue(expected2) + "\" or \""
                + RuleScanner.getTokenValue(expected3) + "\" expected. Found \"" + found + "\"!";
    }

    private String signalError(String found, int expected, int expected2, int expected3,
            int expected4) {
        return "5 Error around line " + myScanner.lineno() + ": token \""
                + RuleScanner.getTokenValue(expected) + "\" or \""
                + RuleScanner.getTokenValue(expected2) + "\" or \""
                + RuleScanner.getTokenValue(expected3) + "\" or \""
                + RuleScanner.getTokenValue(expected4) + "\" expected. Found \"" + found + "\"!";
    }
    /*
    private String signalError(String found, int expected, int expected2, int expected3,
    int expected4, int expected5) {
    return "Error around line " + myScanner.lineno() + ": token \"" +
    RuleScanner.getTokenValue(expected) + "\" or \"" +
    RuleScanner.getTokenValue(expected2) + "\" or \"" +
    RuleScanner.getTokenValue(expected3) + "\" or \"" +
    RuleScanner.getTokenValue(expected4) + "\" or \"" +
    RuleScanner.getTokenValue(expected5) + "\" expected. Found \"" + found + "\"!";
    }
     */

    private String signalError(String found, int expected, int expected2, int expected3,
            int expected4, int expected5, int expected6) {
        return "Error around line " + myScanner.lineno() + ": token \""
                + RuleScanner.getTokenValue(expected) + "\" or \""
                + RuleScanner.getTokenValue(expected2) + "\" or \""
                + RuleScanner.getTokenValue(expected3) + "\" or \""
                + RuleScanner.getTokenValue(expected4) + "\" or \""
                + RuleScanner.getTokenValue(expected5) + "\" or \""
                + RuleScanner.getTokenValue(expected6) + "\" expected. Found \"" + found + "\"!";
    }
    static int depth = 0;

    private void traceConsume() {
        for (int i = 0; i < depth + depth + 2; i++) {
            System.out.print(' ');
        }
        System.out.println(crtToken.tokenValue + ", " + crtToken.tokenId);
    }

    private void traceEntry(String nonterm) {
        for (int i = 0; i < depth + depth; i++) {
            System.out.print(' ');
        }
        System.out.println("< " + nonterm);
        depth++;
    }

    private void traceExit(String nonterm) {
        depth--;
        for (int i = 0; i < depth + depth; i++) {
            System.out.print(' ');
        }
        System.out.println(">");
    }

    private void traceSemop(String semOp) {
        System.out.println("                                    Semantic operator: " + semOp);
    }

    private void traceSemact(String semAct) {
        System.out.println("                                    - Semantic action: " + semAct);
    }

    /*
    private void traceTitle(String title) {
    System.out.println();
    System.out.println(title);
    }

    private void traceLine(String line) {
    System.out.println("  " + line);
    }
     */
}

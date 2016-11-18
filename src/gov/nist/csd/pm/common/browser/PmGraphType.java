package gov.nist.csd.pm.common.browser;

import com.google.common.base.Throwables;

import gov.nist.csd.pm.common.application.PolicyMachineClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.info.PMCommand;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.Log;

import java.util.*;

import static gov.nist.csd.pm.common.util.CommandUtil.makeCmd;

/**
 * Created by IntelliJ IDEA. User: R McHugh Date: 7/18/11 Time: 1:13 PM To change this template use File | Settings | File Templates.
 */
public enum PmGraphType {


    /**
	 * @uml.property  name="uSER"
	 * @uml.associationEnd  
	 */
    USER(SysCaller.PM_VOS_PRES_USER,"User Mode"),
    // Steve - Added (4/16/16): Same as above?
    USER_MELL(SysCaller.PM_VOS_PRES_USER,"User Mode"),
    
    /**
	 * @uml.property  name="aDMIN"
	 * @uml.associationEnd  
	 */
    ADMIN(SysCaller.PM_VOS_PRES_ADMIN, "Administrator Mode"),
    /**
	 * @uml.property  name="uSER_ATTRIBUTES"
	 * @uml.associationEnd  
	 */
    USER_ATTRIBUTES(PmGraphTypes.PM_GRAPH_UATTR, "User attributes"),
    
    // Steve - Added (4/16/16): Same as above?
    USER_MELL_ATTRIBUTES(PmGraphTypes.PM_GRAPH_UATTR, "User attributes"),
    /**
	 * @uml.property  name="aCCESS_CONTROL_ENTRIES"
	 * @uml.associationEnd  
	 */
    ACCESS_CONTROL_ENTRIES(PmGraphTypes.PM_GRAPH_ACES,"Objects/Attributes with ACE's"),
    
    MELL_ACCESS_CONTROL_ENTRIES(PmGraphTypes.PM_GRAPH_ACES,"Objects/Attributes with ACE's"),

    /**
	 * @uml.property  name="cAPABILITIES"
	 * @uml.associationEnd  
	 */
    CAPABILITIES(PmGraphTypes.PM_GRAPH_CAPS,  "Users/Attributes with Capabilities"),
    /**
	 * @uml.property  name="oBJECT_ATTRIBUTES"
	 * @uml.associationEnd  
	 */
    OBJECT_ATTRIBUTES(PmGraphTypes.PM_GRAPH_OATTR, "Object attributes"),
    /**
	 * @uml.property  name="pERMISSIONS"
	 * @uml.associationEnd  
	 */
    PERMISSIONS(PmGraphTypes.PM_GRAPH_PERMS, "Permissions");

	Log log = new Log(Log.Level.INFO, true);


    PmGraphType(String typeCode, String name){
        _typeCode = typeCode;
        _readableName = name;
    }

    public static Map<PmGraphType, EnumSet<PmGraphDirection>> VALID_DIRECTIONS_FOR_TYPE = new HashMap(){{
        put(USER, EnumSet.of(PmGraphDirection.USER));
        put(USER_MELL, EnumSet.of(PmGraphDirection.USER_MELL));
        put(ADMIN, EnumSet.of(PmGraphDirection.USER));
        put(USER_ATTRIBUTES, EnumSet.of(PmGraphDirection.UP, PmGraphDirection.DOWN));
        put(USER_MELL_ATTRIBUTES, EnumSet.of(PmGraphDirection.UP_MELL, PmGraphDirection.DOWN_MELL));
        put(ACCESS_CONTROL_ENTRIES, EnumSet.of(PmGraphDirection.UP, PmGraphDirection.DOWN));
        put(MELL_ACCESS_CONTROL_ENTRIES, EnumSet.of(PmGraphDirection.UP_MELL, PmGraphDirection.DOWN_MELL));
        put(CAPABILITIES, EnumSet.of(PmGraphDirection.UP, PmGraphDirection.DOWN));
        put(OBJECT_ATTRIBUTES, EnumSet.of(PmGraphDirection.UP, PmGraphDirection.DOWN));
        put(PERMISSIONS, EnumSet.of(PmGraphDirection.UP, PmGraphDirection.DOWN));

    }};
    /**
	 * @uml.property  name="_typeCode"
	 */
    private String _typeCode;
    /**
	 * @uml.property  name="_readableName"
	 */
    private String _readableName;
    public String typeCode(){
        return _typeCode;
    }

    public String readableName(){
        return _readableName;
    }
    public String toString(){
        return _typeCode;
    }

    public List<PmNode> getChildrenOf(PmNode node, PmGraphDirection dir, PolicyMachineClient client, String sessionId){
        log.debug("TRACE 16* - In PmGraphType.getChildrenOf() node " + 
        		node.sLabel + " with direction " + dir.name() + " = " + dir.getSysCallerCommand());

        if(VALID_DIRECTIONS_FOR_TYPE.get(this).contains(dir)){
        	log.debug("TRACE 17* - " + VALID_DIRECTIONS_FOR_TYPE.get(this));
            return PmNode.createAll(
                    getResultsOfNodeCommand(dir.getSysCallerCommand(), node, client, sessionId),
                    node.getChildProvidingDelegate());
        }
        else{
        	log.warn("TRACE 17* - " + VALID_DIRECTIONS_FOR_TYPE.get(this) + " NOT VALID DIRECTION TYPE");
            return null;
        }
    }

    public List<String[]> getResultsOfNodeCommand(PMCommand command, PmNode node, PolicyMachineClient client, String sessionId){
        log.debug("TRACE 17 - In PmNode.getResultsOfNodeCommand() START");

        try{
            Packet res = null;
            Packet cmd = makeCmd(command, sessionId, node.getName(), node.getId(), node.getType(), typeCode());
            res = client.sendReceive(cmd, null);
            List<String[]> results = new ArrayList();
            log.debug("TRACE 23 - In PmNode.getResultsOfNodeCommand() payload length: " + res.getPayloadLength());
            int i = 0;
            for(String part : res.toStringIterable()){
                log.debug("TRACE 24 - res [" + i++ + "]: " + part);
                String[] nodeValues = part.split(SysCaller.PM_FIELD_DELIM);
                if (nodeValues.length == 3) {
                    results.add(Arrays.copyOf(part.split(SysCaller.PM_FIELD_DELIM), 3));
                } else if (nodeValues.length == 4) {
                    results.add(Arrays.copyOf(part.split(SysCaller.PM_FIELD_DELIM), 4));
                } else {
                	log.warn("Invalid number of node values");
                }
            }
            log.debug("TRACE 25 - In PmNode.getResultsOfNodeCommand() -- PACKET RESULT RECEIVED");

            return results;
        }catch(Exception e){
            Throwables.propagate(e);
        }
        return null;
    }
}


interface PmGraphTypes{
    public static final String PM_GRAPH_UATTR = "ua";
    public static final String PM_GRAPH_CAPS = "ca";
    public static final String PM_GRAPH_OATTR = "oa";
    public static final String PM_GRAPH_ACES = "ac";
    public static final String PM_GRAPH_PERMS = "pe";
}

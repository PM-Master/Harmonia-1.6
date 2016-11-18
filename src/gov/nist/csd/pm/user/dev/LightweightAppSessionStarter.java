package gov.nist.csd.pm.user.dev;

import com.google.common.base.Throwables;
import gov.nist.csd.pm.common.application.ArgumentProcessor;
import gov.nist.csd.pm.common.application.ArgumentProcessors;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.info.Args;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static gov.nist.csd.pm.common.application.ArgumentProcessors.forArgumentInPosition;
import static gov.nist.csd.pm.common.application.ArgumentProcessors.forDirective;
import static gov.nist.csd.pm.common.util.CommandUtil.*;
import static java.util.Arrays.copyOfRange;

/**
 * Created by IntelliJ IDEA.
 * User: R McHugh
 * Date: 7/6/11
 * Time: 2:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class LightweightAppSessionStarter {
    //logs in, starts a session, creates a process, invokes an application

    public static SSLSocketClient createSocketClient(int simulatorPort, boolean debugFlag, String prefix){
        try{
        return new SSLSocketClient("localhost", simulatorPort,
                    debugFlag, prefix);
        }catch(Exception e){
            Throwables.propagate(e);
        }
        return null;
    }

    /**
	 * @uml.property  name="_args" multiplicity="(0 -1)" dimension="1"
	 */
    private final String[] _args;

    public LightweightAppSessionStarter(String[] args){
         _debug =     forDirective(Args.DEBUG_ARG);
        _simport =   forDirective(Args.SIM_PORT_ARG, 1);
        _username =  forDirective("-username", 1);
        _password =  forDirective("-password", 1);
        _mainClass = forArgumentInPosition(0);
        List<ArgumentProcessor> argProcessors = newArrayList(_debug, _simport, _username, _password, _mainClass);
        ArgumentProcessors.processArguments(args, argProcessors);
        _args = args;
    }

    public static void main(String[] args){
        LightweightAppSessionStarter lsas = new LightweightAppSessionStarter(args);

        lsas.startApplication();

    }

    public static void invokeStatic(String mainClassName, String methodName, Object... args){
        try {
            Class<?> mainClass = Class.forName(mainClassName);
            for(Method method : mainClass.getMethods()){
                if(method.getName().equals(methodName)){
                    method.invoke(null, (Object)args);
                }
            }
        } catch (ClassNotFoundException e) {
            Logger.getLogger(LightweightAppSessionStarter.class.getName()).log(Level.SEVERE, e.getLocalizedMessage());
        } catch (InvocationTargetException e) {
            Throwables.propagate(e);
        } catch (IllegalAccessException e) {
            Logger.getLogger(LightweightAppSessionStarter.class.getName()).log(Level.SEVERE, e.getLocalizedMessage());
        }

    }

    /**
	 * @uml.property  name="_sockClient"
	 * @uml.associationEnd  
	 */
    private SSLSocketClient _sockClient;

    public SSLSocketClient getSocketClient(){
        if(_sockClient == null){

            _sockClient = createSocketClient(getSimulatorPort(), isDebugEnabled(), "lwi");
        }
        return _sockClient;
    }
    private static final boolean DEFAULT_DEBUG_ENABLED = false;
    public boolean isDebugEnabled(){
        return _debug == null ? DEFAULT_DEBUG_ENABLED : _debug.value().toBoolean();
    }

    private static final int DEFAULT_SIMPORT = 8081;
    public int getSimulatorPort(){
        return _simport == null ? DEFAULT_SIMPORT : _simport.value().toInt();
    }
    /**
	 * @uml.property  name="_debug"
	 * @uml.associationEnd  
	 */
    private final ArgumentProcessor _debug;

	/**
	 * @uml.property  name="_simport"
	 * @uml.associationEnd  
	 */
	private final ArgumentProcessor _simport;

    /**
	 * @uml.property  name="_sessionInfo"
	 * @uml.associationEnd  
	 */
    private SessionInfo _sessionInfo;
    public SessionInfo getSession(){
        if(_sessionInfo == null){
            _sessionInfo = createSession(getSocketClient(), getUsername(), getPassword());
        }
        return _sessionInfo;
    }

    public String getUsername(){
        return _username == null ? "" : _username.value().toString();
    }

    /**
	 * @uml.property  name="_username"
	 * @uml.associationEnd  
	 */
    private final ArgumentProcessor _username;

	/**
	 * @uml.property  name="_password"
	 * @uml.associationEnd  
	 */
	private final ArgumentProcessor _password;

	/**
	 * @uml.property  name="_mainClass"
	 * @uml.associationEnd  
	 */
	private final ArgumentProcessor _mainClass;

    public char[] getPassword(){
        return _password == null ? new char[0] : _password.value().toString().toCharArray();
    }

    public String getMainClass(){
        return _mainClass == null ? "" : _mainClass.value().toString();
    }
    /**
	 * @uml.property  name="_processId"
	 */
    private String _processId;
    public String getProcessId(){
        if(_processId == null){
            _processId = createProcess(getSession().getSessionId(), getSocketClient());
            if(_processId != null){
                boolean result = computeVos(
                        getSession().getSessionId(),
                        getSession().getUserId(),
                        SysCaller.PM_VOS_PRES_USER,
                        getSocketClient());
                if(!result){
                    _processId = null;
                }
                else{
                    addAutomaticShutdownHook(getSocketClient(), getSession(), _processId);
                }
            }
        }
        return _processId;
    }

    private static void addAutomaticShutdownHook(final SSLSocketClient sockClient, final SessionInfo sessionInfo, final String procId) {
        checkNotNull(sockClient); checkNotNull(sessionInfo); checkNotNull(procId);
        Runtime.getRuntime().addShutdownHook(new Thread(){

            @Override
            public void run() {
                exitProcess(sessionInfo.getSessionId(), procId, sockClient);
                exitSession(sessionInfo.getSessionId(), sockClient);
                try {
                    sockClient.close();
                } catch (Exception e) {
                    Throwables.propagate(e);

                }

            }
        });
    }

    public void quitProcess(){
        if(_processId != null){
            exitProcess(getSession().getSessionId(), getProcessId(), getSocketClient());
            _processId = null;
        }
    }

    private void closeSocketClient(){
        if(_sockClient != null){
            try {
                _sockClient.close();
            } catch (Exception e) {
                Throwables.propagate(e);
            }
            _sockClient = null;
        }
    }

    public void quitSession(){
        if(_sessionInfo != null){
            quitProcess();
            exitSession(getSession().getSessionId(), getSocketClient());
            _sessionInfo = null;
            closeSocketClient();
        }
    }



    public void startApplication(){

        String pid = getProcessId();
        String sid = getSession().getSessionId();

        List<String> newArgs = newArrayList(copyOfRange(_args, 5, _args.length));
        newArgs.addAll(0, newArrayList(Args.SESSION_ARG, sid, Args.PROCESS_ARG, pid));

        invokeStatic(getMainClass(), "main", (Object[]) newArgs.toArray(new String[0]));

    }




}

package gov.nist.csd.pm.user;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import gov.nist.csd.pm.common.application.SSLSocketClient;
import gov.nist.csd.pm.common.net.Item;
import gov.nist.csd.pm.common.net.Packet;
import gov.nist.csd.pm.common.util.CommandUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Utility class used for managing installed applications on the policy machine
 * 
 */
public class ApplicationManager implements LauncherThreadDelegate {

	public static final String EGRANT_APP_NAME = "e-grant";
	public static final String OPEN_OFFICE_APP_NAME = "Open Office";
	public static final String MS_OFFICE_APP_NAME = "Microsoft Office Launcher";
	public static final String RTF_EDITOR_APP_NAME = "Rich Text Editor";
	public static final String TH_EDITOR_APP_NAME = "TH EDITOR NOT FOUND";
	public static final String EXPORTER_APP_NAME = "Exporter";
	public static final String ACCOUNT_EDITOR_APP_NAME = "Acct-Rec";
	public static final String MEDICAL_RECORD_EDITOR_APP_NAME = "Med-Rec";
	public static final String SCHEMA_BUILDER_APP_NAME = "Schema Builder";
	public static final String TABLE_EDITOR_APP_NAME = "Table Editor";
	public static final String ADMIN_TOOL_APP_NAME = "Admin";
	public static final String WORKFLOW_EDITOR_APP_NAME = "Workflow Editor";
	public static final String PDF_VIEWER_APP_NAME = "PDF Viewer" ;
	public static final String WORKFLOW_OLD = "Workflow Old";
	public static final String DUMMY_APP = "Dummy";
	//TODO: add variable for old workflow 
	//TODO: generate old work flow in template to interpert the conifugration file 


	private static final int APP_PATH_INDEX = 0;
	private static final int APP_PREFIX_INDEX = 1;
	private static final int APP_CLASS_INDEX = 2;
	/**
	 * @uml.property  name="socketClient"
	 * @uml.associationEnd  
	 */
	private SSLSocketClient socketClient;
	/**
	 * @uml.property  name="host"
	 */
	private String host;
	/**
	 * @uml.property  name="applicationManagerListeners"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="gov.nist.csd.pm.user.ApplicationManagerListener"
	 */
	private List<ApplicationManagerListener> applicationManagerListeners;

	private ApplicationManager(String host) {
		this.host = checkNotNull(host);
		applicationManagerListeners = new ArrayList<ApplicationManagerListener>();
	}

	public void addApplicationManagerListener(ApplicationManagerListener listener) {
		if (!applicationManagerListeners.contains(listener)) {
			applicationManagerListeners.add(listener);
		}
	}

	public void removeApplicationManagerListener(ApplicationManagerListener listener) {
		if (applicationManagerListeners.contains(listener)) {
			applicationManagerListeners.remove(listener);
		}
	}

	public ApplicationManager(SSLSocketClient socketClient, String host) {
		this(host);
		this.socketClient = socketClient;
	}

	/**
	 * Gets a list of applications installed on this host.
	 * @return
	 */
	public List<String> getInstalledApplications() {
		List<String> apps = new ArrayList<String>();
		Packet resp = null;
		try {
			Packet cmd = CommandUtil.makeCmd("getInstalledAppNames", null, host);
			resp = socketClient.sendReceive(cmd, null);
			if (resp == null) {
				return apps;
			}
			if (resp.hasError()) {
				return apps;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList<Item> items = resp.getItems();
		for (Item item : items) {
			try{
				apps.add(item.getValueString());
			}catch(NullPointerException npe){
				//Ignore this, it just means that item was null
			}
		}
		return apps;
	}

	public void launchClientApp(String applicationName, String sessionId, String... arguments){
		System.out.println("Application openning is: " + applicationName);
		launchApp(applicationName, sessionId, false, arguments);
	}

	public void launchPeerApp(String applicationName, String sessionId, String... arguments){
		launchApp(applicationName, sessionId, true, arguments);
	}


	/**
	 * @uml.property  name="wrapInQuotationMarks"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private Function<String, String> wrapInQuotationMarks = new Function<String, String>() {
		@Override
		public String apply(String input) {
			return String.format("\"%s\"", input);
		}
	};

	/**
	 * This method will launch a the specified app as a child process, meaning 
	 * that it will be passed -session and -process arguments by default
	 *
	 * @param applicationName
	 *            The name of the application being launched
	 * @param sessionId
	 *            The id of the session launching the application
	 * @param asPeer
	 *            If false will pass the parameters "-session XXX" and "-process YYY"
	 *            If true it will only pass default parameters.
	 * @param arguments
	 *            Additional arguments to pass to the child application.
	 */
	public void launchApp(String applicationName, String sessionId, boolean asPeer, String... arguments) {
		String[] pathInfo = getApplicationPath(applicationName);
		for(int i = 0; i < pathInfo.length; i++){
			System.out.println(i + " " + pathInfo[i]);
		}

		String procId = "";
		if(asPeer){
			System.out.println("TRUE LINE 137 App Manager");
			procId = UUID.randomUUID().toString().replaceAll("-", "");
		}
		else{
			System.out.println("FALSE LINE 141 APP Manager");
			System.out.println("sessid: " + sessionId);
			procId = CommandUtil.createProcess(sessionId, this.socketClient);
			//System.out.println("cUtilCreateProcess: " + CommandUtil.createProcess(sessionId, this.socketClient));
			System.out.println("procid" + procId);
		}
		if (procId != null) {
			System.out.println("procId is not null");
			String a = String.format("javaw -cp \"%s\"", pathInfo[APP_PATH_INDEX]);
			System.out.println(a);
			String b = String.format("-Djavax.net.ssl.keyStore=\"%s\"",System.getProperty("javax.net.ssl.keyStore"));
			System.out.println(b);
			String c = String.format("-Djavax.net.ssl.keyStorePassword=\"%s\"",
					System.getProperty("javax.net.ssl.keyStorePassword"));
			System.out.println(c);
			String d = String.format("-Djavax.net.ssl.trustStore=\"%s\"",
					System.getProperty("javax.net.ssl.trustStore"));
			System.out.println(d);
			String[] commandSegments = new String[]{
					/*String.format("javaw -cp \"%s\"", pathInfo[APP_PATH_INDEX]),
					String.format("-Djavax.net.ssl.keyStore=\"%s\"",
							System.getProperty("javax.net.ssl.keyStore")),
							String.format("-Djavax.net.ssl.keyStorePassword=\"%s\"",
									System.getProperty("javax.net.ssl.keyStorePassword")),
									String.format("-Djavax.net.ssl.trustStore=\"%s\"",
											System.getProperty("javax.net.ssl.trustStore")),*/
					a, b, c, d,
											pathInfo[APP_CLASS_INDEX],
											"-session",
											sessionId,
			};
			StringBuilder sb = new StringBuilder();
			Joiner.on(" ").appendTo(sb, commandSegments);
			List<String> argList = newArrayList(" ");

			if (!asPeer) {
				argList.addAll(newArrayList("-process", procId));
			}

			if (arguments != null && arguments.length > 0) {
				argList.addAll(Lists.transform(Arrays.asList(arguments), wrapInQuotationMarks));
			}


			Joiner.on(" ").skipNulls().appendTo(sb, argList);

			String execString = sb.toString();
			System.out.println("Executing process in another thread: "
					+ execString);
			System.out.println("    with pathInfo: " + pathInfo[1]);
			LauncherThread lt = new LauncherThread(execString, pathInfo[APP_PREFIX_INDEX], procId, this);
			lt.start();

			Process proc = lt.getProcess();
			NativeProcessWrapper wrappedProcess = NativeProcessWrapperManager.getManager().getNativeProcessWrapperForCurrentPlatform(proc);
			for (ApplicationManagerListener delegate : applicationManagerListeners) {
				delegate.applicationStarted(
						applicationName,
						procId,
						wrappedProcess);
			}
		}
	}

	public String getApplicationPathString(String appName) {
		String[] appPath = getApplicationPath(appName);
		if (appPath != null && appPath.length > APP_PATH_INDEX) {
			return appPath[APP_PATH_INDEX];
		}
		return null;
	}

	/**
	 * TODO:This method has a duplicate in Syscaller
	 *
	 * @param appName
	 *            The name of the application whose path you need
	 * @return At index zero the applications path, at index 1 the applications
	 *         prefix, at index 2 the applications main class
	 */
	private String[] getApplicationPath(String appName) {
		Packet resp = null;
		String[] returnValue = new String[3];


		try {
			Packet cmd = CommandUtil.makeCmd("getHostAppPaths", null,
					host, appName);
			resp = socketClient.sendReceive(cmd, null);


			if (resp == null || resp.hasError()) {
				return null;
			}
			returnValue[0] = resp.getItemStringValue(0);
			returnValue[1] = resp.getItemStringValue(1);
			returnValue[2] = resp.getItemStringValue(2);

			return returnValue;


		} catch (Exception e) {
			System.out.println("There was a problem getting the application path information for " + appName);


		}
		return null;


	}

	/**
	 * sets a the path of a given application on the current host.
	 * @param appName the well-known name of the application
	 * @param appPath the path to the application on this machine.
	 * @return true of the path was set in the policy machine, false otherwise.
	 */
	public boolean setApplicationPath(String appName, String appPath) throws ApplicationUpdateException {
		Packet resp = null;
		//Get the original app information so that we can feed it back to the PolicyMachine
		String[] appInfo = getApplicationPath(appName);

		try {
			Packet cmd = CommandUtil.makeCmd("addHostApp", null, host, appName, appPath, appInfo[APP_CLASS_INDEX], appInfo[APP_PREFIX_INDEX]);
			resp = socketClient.sendReceive(cmd, null);


			if (resp == null || resp.hasError()) {
				return false;


			}
		} catch (Exception e) {
			throw new ApplicationUpdateException(appName, e);
		}
		return true;


	}

	/**
	 *
	 * @param appName
	 *            The name of the application which might be installed
	 * @return A boolean indicating if the application is installed
	 */
	public boolean checkIfInstalled(String appName) {
		String[] pathInfo = getApplicationPath(appName);


		if (pathInfo == null) {
			return false;


		}
		return true;


	}

	public void terminateApplication(String procid) {
		System.out.println("******* TERM****** " + procid);
		for (ApplicationManagerListener delegate : applicationManagerListeners) {
			delegate.applicationTerminated(procid);


		}
	}

	@Override
	public void launcherThreadTerminated(LauncherThread thread) {
		System.out.println("******* Laun - TERM****** " + thread.toString());
		terminateApplication(thread.getProcessId());

	}
}

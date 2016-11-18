/*
 * WriterLauncher.java
 *
 * Created on June 8, 2007, 10:30 AM
 */
package gov.nist.csd.pm.application.openoffice;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.*;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.CloseVetoException;
import com.sun.star.util.URL;
import com.sun.star.util.XCloseable;

import gov.nist.csd.pm.common.application.PMIOObject;
import gov.nist.csd.pm.common.application.SysCaller;
import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.ObjectBrowser;
import static gov.nist.csd.pm.common.constants.GlobalConstants.*;


import javax.swing.*;

//import sun.jdbc.odbc.OdbcDef;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;

/**
 * @author steveq@nist.gov
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class OfficeLauncher extends JFrame implements XDispatchProviderInterceptor, XDispatch {
    public static final String OPENOFFICE_LAUNCHER_APP_TITLE = "OpenOffice Launcher";
    public static final String OPEN_OFFICE_PREFIX = "OFL";

    /**
	 * @uml.property  name="pM_DEFAULT_SIMULATOR_PORT"
	 */
    public static final String PM_FAILURE = "err ";
    public static final String PM_SUCCESS = "ok  ";
    public static final String PM_CMD = "cmd ";
    public static final String PM_EOC = "eoc ";
    public static final String PM_ARG = "arg ";
    public static final String PM_SEP = "sep ";
    public static final String PM_DATA = "data";
    public static final String PM_EOD = "eod ";
    public static final String PM_BYE = "bye ";
    public static final String PM_OBJTYPE_DOC = "doc";
    public static final String PM_OBJTYPE_PPT = "ppt";
    public static final String PM_OBJTYPE_XLS = "xls";
    /**
	 * @uml.property  name="objectBrowser"
	 * @uml.associationEnd  
	 */
    private ObjectBrowser objectBrowser;
    // Name and handle for the virtual object currently open.
    /**
	 * @uml.property  name="currentObject"
	 * @uml.associationEnd  
	 */
    private PMIOObject currentObject;
    /**
	 * @uml.property  name="sObjType"
	 */
    private String sObjType;
    /**
	 * @uml.property  name="sProcessId"
	 */
    private String sProcessId;
    /**
	 * @uml.property  name="sysCaller"
	 * @uml.associationEnd  
	 */
    private SysCaller sysCaller;
    /**
	 * @uml.property  name="simPort"
	 */
    private int simPort;
    /**
	 * @uml.property  name="bDebug"
	 */
    private boolean bDebug;
    /**
	 * Used to register interceptor handler class.
	 * @uml.property  name="xDispatchInterception"
	 * @uml.associationEnd  
	 */
    public XDispatchProviderInterception xDispatchInterception = null;
    /**
	 * The master dispatch provider for the intercepted event.
	 * @uml.property  name="xDispatchProviderMaster"
	 * @uml.associationEnd  
	 */
    private XDispatchProvider xDispatchProviderMaster;
    /**
	 * The slave dispatch provider for the intercepted event.
	 * @uml.property  name="xDispatchProviderSlave"
	 * @uml.associationEnd  
	 */
    private XDispatchProvider xDispatchProviderSlave;
    /**
	 * The URL of the current menu item action.
	 * @uml.property  name="currentActionURL"
	 * @uml.associationEnd  
	 */
    protected URL currentActionURL;
    /**
	 * @uml.property  name="xComp"
	 * @uml.associationEnd  
	 */
    private XComponent xComp;
    /**
	 * @uml.property  name="xContext"
	 * @uml.associationEnd  
	 */
    private XComponentContext xContext;
    /**
	 * @uml.property  name="xFrame"
	 * @uml.associationEnd  
	 */
    private XFrame xFrame;
    /**
	 * @uml.property  name="xMCF"
	 * @uml.associationEnd  
	 */
    private XMultiComponentFactory xMCF;
    /**
	 * @uml.property  name="createNew"
	 */
    private boolean createNew = false;

    private static final int ERROR_DOCUMENT_OPEN = -1;
    private static final int ERROR_OOO_FRAME_NOT_ACQUIRED = -2;
    private static final int ERROR_DISPATCH_INTERCEPTOR_NOT_ACQUIRED = -3;
    
    private String sCrtObjHandle;

    /** Creates a new instance of WriterLauncher */
    public OfficeLauncher(int simPort, String sSessId, String sProcId,
            String sObjType, boolean bDebug) {
        this.simPort = (simPort < 1024) ? PM_DEFAULT_SIMULATOR_PORT : simPort;
        this.sObjType = sObjType;
        this.bDebug = bDebug;
        this.sProcessId = sProcId;
        

        // An instance of SysCaller to call Kernel's APIs.
        sysCaller = new SysCallerImpl(simPort, sSessId, sProcId, bDebug, OPEN_OFFICE_PREFIX);

        setTitle(OPENOFFICE_LAUNCHER_APP_TITLE);
        
        //This is a new variable to indicate that we are creating a new file
        // from blank
        this.createNew =false;
        
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                terminate(0);

            }
        });

    }

    private void terminate(int nExitCode) 
    {
    	if (sCrtObjHandle != null) {
    	      sysCaller.closeObject(sCrtObjHandle);
    	    }
    	    
        XCloseable xcloseable = (XCloseable) UnoRuntime.queryInterface(XCloseable.class, xComp);
        try {
        	if(xcloseable != null)
        	{
        		xcloseable.close(true);
        	}
		} catch (CloseVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        sysCaller.exitProcess(sProcessId);
    	
        System.exit(nExitCode);
    }

    @SuppressWarnings("CallToThreadDumpStack")
    private void openInitialObj(String sObjName) {
        // Open the object.
        try {

            xContext = com.sun.star.comp.helper.Bootstrap.bootstrap();
            System.out.println("Connected to a running office ...");
            // Get the remote office service manager.
            xMCF = xContext.getServiceManager();
            // Get the root frame (i.e. desktop) of openoffice framework.
            Object oDesktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
           // XDesktop oDesktop = (XDesktop) xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
            
        	
            // Desktop has 3 interfaces. The XComponentLoader interface
            // provides ability to load components.
           if(sObjName != null){
        	   //TODO: Removed this section and replaced it with the sCrtObjHanle to 
        	   // eliminate the use of PMIOOBJECT and use JUST the straight streaming 
        	   
        	   /*   currentObject = sysCaller.openObject4(sObjName, "File read, File write");
                if (currentObject == null) {
                    JOptionPane.showMessageDialog(null, sysCaller.getLastError());
                    return;
                }*/
        	   //The next 4 lines replaces the above commented block!!!!
            sCrtObjHandle = sysCaller.openObject3(sObjName, "File read,File write");
            if (sCrtObjHandle == null) {
              JOptionPane.showMessageDialog(null, sysCaller.getLastError());
              return;
            }
            // Get the object content.
            byte[] buf = sysCaller.readObject3(sCrtObjHandle);
            if (buf == null) {
              JOptionPane.showMessageDialog(null, sysCaller.getLastError());
              String sErr = sysCaller.getLastError();
              if (sErr != null && sErr.indexOf("exist") > 0)
            	  buf = new byte[0];
              else
            	  return;
            }
                XComponentLoader xCompLoader = UnoRuntime.queryInterface(
                        com.sun.star.frame.XComponentLoader.class, oDesktop);

                PropertyValue[] loadProps = new PropertyValue[2];// empty

                // Set filter type
                loadProps[0] = new PropertyValue();
                loadProps[0].Name = "FilterName";

                if (sObjType.equalsIgnoreCase(PM_OBJTYPE_DOC)) {
                    loadProps[0].Value = "MS Word 97";
                } else if (sObjType.equalsIgnoreCase(PM_OBJTYPE_PPT)) {
                    loadProps[0].Value = "MS PowerPoint 97";
                } else if (sObjType.equalsIgnoreCase(PM_OBJTYPE_XLS)) {
                    loadProps[0].Value = "MS Excel 97";
                } else {
                    JOptionPane.showMessageDialog(null, "Unknown type of Office object: " + sObjType);
                    return;
                }


                // Set the input stream.
                OOInputStream oois = new OOInputStream(buf);
                loadProps[1] = new PropertyValue();
                loadProps[1].Name = "InputStream";
                //loadProps[1].Value = new OOInputStream(currentObject.getInputStream());
                loadProps[1].Value = oois; 
                
                xComp = xCompLoader.loadComponentFromURL("private:stream",
                        "_blank", 0, loadProps);
                if(xComp == null){
                    String couldNotOpenDocumentError = String.format("The document \"%s\" couldn't be opened.", sObjName);
                    quitWithError(couldNotOpenDocumentError, ERROR_DOCUMENT_OPEN);
                }
            }
            else{
            	this.createNew=true;
            	sObjType = PM_OBJTYPE_XLS;
                currentObject = PMIOObject.EMPTY_OBJECT;
                System.out.print("LOOODING ****");
                sObjType= PM_OBJTYPE_DOC;//TODO uncomment
                //sObjType = PM_OBJTYPE_XLS;
                System.out.print("\nTYPE is : ***** : " + sObjType + " \n");
                PropertyValue[] loadProps = new PropertyValue[2];// empty
                // Set filter type
                loadProps[0] = new PropertyValue();
                loadProps[0].Name = "FilterName";
                loadProps[0].Value = "MS Word 97";//TODO; uncomment
                //loadProps[0].Value = "MS Excel 97";
                // Set the input stream.
                loadProps[1] = new PropertyValue();
                loadProps[1].Name = "InputStream";
                loadProps[1].Value = new OOInputStream(currentObject.getInputStream());
                System.out.println("We here *************");
                //Set handle new
                /*loadProps[2] = new PropertyValue();
                loadProps[2].Name = "OpenNewView";
                loadProps[2].Value= Boolean.FALSE;*/
                
                XComponentLoader xCompLoader = UnoRuntime.queryInterface(
                        com.sun.star.frame.XComponentLoader.class, oDesktop);
                //"private:factory/swriter", "private:stream"//
                xComp = xCompLoader.loadComponentFromURL("private:stream", "_blank", 0, loadProps);
            }
            com.sun.star.frame.XDesktop xDesktop = UnoRuntime.queryInterface(com.sun.star.frame.XDesktop.class, oDesktop);

            xFrame = xDesktop.getCurrentFrame();
            if(xFrame == null){
                quitWithError("Could not find the OpenOffice frame", ERROR_OOO_FRAME_NOT_ACQUIRED);
            }
            xDispatchInterception =  UnoRuntime.queryInterface(com.sun.star.frame.XDispatchProviderInterception.class,
                    xFrame);
            if(xDispatchInterception == null){
                quitWithError("Could not tie into the OpenOffice dispatch interception", ERROR_DISPATCH_INTERCEPTOR_NOT_ACQUIRED);
            }
            xDispatchInterception.registerDispatchProviderInterceptor(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void quitWithError(String message, int error){
        JOptionPane.showMessageDialog(null, message, "PM OpenOffice.org Invoker", JOptionPane.ERROR_MESSAGE);
        System.exit(error);
    }

    /**
     * Dispatch provider.
     * @param url The URL of the dispatched menu item.
     * @param target
     * @param searchFlags
     */
    @Override
    public XDispatch queryDispatch(URL url, String target, int searchFlags) {
        if ((url.Complete.compareTo(".uno:SaveAs") == 0)
                || (url.Complete.compareTo(".uno:Save") == 0)
                || (url.Complete.compareTo(".uno:Open") == 0)
                || (url.Complete.compareTo(".uno:CloseDoc") == 0)
                || (url.Complete.compareTo(".uno:CloseWin") == 0)
                || (url.Complete.compareTo(".uno:CloseFrame") == 0)
                || (url.Complete.compareTo(".uno:Quit") == 0)
                || (url.Complete.compareTo(".uno:Copy") == 0)
                || (url.Complete.compareTo(".uno:Paste") == 0)) {
            return this;
        }

        if (xDispatchProviderSlave != null) {
            return xDispatchProviderSlave.queryDispatch(url, target, searchFlags);
        }

        return null;
    }

    @Override
    public XDispatch[] queryDispatches(DispatchDescriptor[] descriptor) {
        int count = descriptor.length;
        XDispatch[] xDispatch = new XDispatch[count];

        for (int i = 0; i < count; i++) {
            xDispatch[i] = queryDispatch(descriptor[i].FeatureURL,
                    descriptor[i].FrameName, descriptor[i].SearchFlags);
        }
        return xDispatch;
    }

    @Override
    public XDispatchProvider getSlaveDispatchProvider() {
        //System.out.println("Get Slave");
        return xDispatchProviderSlave;
    }

    @Override
    public void setSlaveDispatchProvider(XDispatchProvider xDispatchProviderSlave) {
        //System.out.println("Set Slave");
        this.xDispatchProviderSlave = xDispatchProviderSlave;
    }

    @Override
    public XDispatchProvider getMasterDispatchProvider() {
        //System.out.println("Get Master");
        return xDispatchProviderMaster;
    }

    @Override
    public void setMasterDispatchProvider(XDispatchProvider xDispatchProviderMaster) {
        //System.out.println("Set Master");
        this.xDispatchProviderMaster = xDispatchProviderMaster;
    }

    @Override
    public void dispatch(URL url, PropertyValue[] propertyValueArray) {
        //System.out.println("Dispatch URL: " + url.Complete.toString());
        currentActionURL = url;

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            @SuppressWarnings("CallToThreadDumpStack")
            public void run() {
                if (currentActionURL.Complete.compareTo(".uno:Save") == 0) {
                    if(currentObject == PMIOObject.EMPTY_OBJECT){
                        return;
                    }
                    try {            
                        XStorable xStorable =
                          (XStorable) UnoRuntime.queryInterface(XStorable.class, xComp);
                        if (xStorable == null) System.out.println("xStorable is null...");
                        
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        OOOutputStream ooos = new OOOutputStream(byteArrayOutputStream);
                        
                        PropertyValue[] storeProps = new PropertyValue[3]; 

                        storeProps[0] = new PropertyValue();
                        storeProps[0].Name = "FilterName";
                        
                        if (sObjType.equalsIgnoreCase(PM_OBJTYPE_DOC))
                          storeProps[0].Value = "MS Word 97";
                        else if (sObjType.equalsIgnoreCase(PM_OBJTYPE_PPT))
                          storeProps[0].Value = "MS PowerPoint 97";
                        else if (sObjType.equalsIgnoreCase(PM_OBJTYPE_XLS))
                          storeProps[0].Value = "MS Excel 97";
                        else {
                          JOptionPane.showMessageDialog(null, "Unknown type of Office object: " + sObjType);
                          return;
                        }

                        storeProps[1] = new PropertyValue();
                        storeProps[1].Name = "Overwrite";
                        storeProps[1].Value = Boolean.TRUE;

                        storeProps[2] = new PropertyValue();
                        storeProps[2].Name = "OutputStream";
                        storeProps[2].Value = ooos;
                        
                        xStorable.storeToURL("private:stream", storeProps);
                        //UNCOMMENTED::: TODO
                       // XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComp);
                       // XCloseable xCloseable = (XCloseable) UnoRuntime.queryInterface(XCloseable.class, xModel);
                       // xCloseable.close(true);//this was false::TODO;;

                        byte[] buf = byteArrayOutputStream.toByteArray();
                        System.out.println("Size of output file is: " + buf.length);
                        int ret = sysCaller.writeObject3(sCrtObjHandle, buf);
                        if (ret < 0) {
                          JOptionPane.showMessageDialog(null, sysCaller.getLastError());
                          return;
                        }
                      } catch (Exception e) {
                        e.printStackTrace();
                      }
/*                    try {
                        XStorable xStorable =
                                (XStorable) UnoRuntime.queryInterface(XStorable.class, xComp);
                        if (xStorable == null) {
                            System.out.println("xStorable is null...");
                        }


                        PropertyValue[] storeProps = new PropertyValue[3];

                        storeProps[0] = new PropertyValue();
                        storeProps[0].Name = "FilterName";

                        if (sObjType.equalsIgnoreCase(PM_OBJTYPE_DOC)) {
                            storeProps[0].Value = "MS Word 97";
                        } else if (sObjType.equalsIgnoreCase(PM_OBJTYPE_PPT)) {
                            storeProps[0].Value = "MS PowerPoint 97";
                        } else if (sObjType.equalsIgnoreCase(PM_OBJTYPE_XLS)) {
                            storeProps[0].Value = "MS Excel 97";
                        } else {
                            JOptionPane.showMessageDialog(null, "Unknown type of Office object: " + sObjType);
                            return;
                        }


                        storeProps[1] = new PropertyValue();
                        storeProps[1].Name = "Overwrite";
                        storeProps[1].Value = Boolean.TRUE;

                        storeProps[2] = new PropertyValue();
                        storeProps[2].Name = "OutputStream";
                        storeProps[2].Value = new OOOutputStream(currentObject.getOutputStream());

                        xStorable.storeToURL("private:stream", storeProps);

                        currentObject.reset();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
*/
                } else if (currentActionURL.Complete.compareTo(".uno:SaveAs") == 0) {
                    if(currentObject == PMIOObject.EMPTY_OBJECT && !createNew){
                        return;
                    }
                    try {
                        int ret = getObjectBrowser().showSaveAsDialog();
                        if (ret != ObjectBrowser.PM_OK) {
                            return;
                        }

                        // Prepare for object creation.
                        String sObjName = getObjectBrowser().getObjName();
                        String sContainers = getObjectBrowser().getContainers();

                        System.out.println("Containers: " + sContainers);
                        System.out.println("Object nam: " + sObjName);

                        String sObjClass = "File";
                        String sPerms = "File write";

                        // Create the object.
                        String sHandle = sysCaller.createObject3(sObjName, sObjClass, sObjType,
                                sContainers, sPerms, null, null, null, null);
                        if (sHandle == null) {
                            JOptionPane.showMessageDialog(null, sysCaller.getLastError());
                            return;
                        }
                        sysCaller.closeObject(sHandle);

                        PMIOObject pmo = sysCaller.openObject4(sObjName, "File read, File write");

                        // Save the document to a byte array.
                        XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, xComp);
                        if (xStorable == null) {
                            System.out.println("xStorable is null...");
                        }



                        PropertyValue[] storeProps = new PropertyValue[3];

                        storeProps[0] = new PropertyValue();
                        storeProps[0].Name = "FilterName";

                        if (sObjType.equalsIgnoreCase(PM_OBJTYPE_DOC)) {
                            storeProps[0].Value = "MS Word 97";
                        } else if (sObjType.equalsIgnoreCase(PM_OBJTYPE_PPT)) {
                            storeProps[0].Value = "MS PowerPoint 97";
                        } else if (sObjType.equalsIgnoreCase(PM_OBJTYPE_XLS)) {
                            storeProps[0].Value = "MS Excel 97";
                        } else {
                            JOptionPane.showMessageDialog(null, "Unknown type of Office object: " + sObjType);
                            return;
                        }

                        storeProps[1] = new PropertyValue();
                        storeProps[1].Name = "Overwrite";
                        storeProps[1].Value = Boolean.TRUE;

                        storeProps[2] = new PropertyValue();
                        storeProps[2].Name = "OutputStream";
                        storeProps[2].Value = new OOOutputStream(pmo.getOutputStream());

                        xStorable.storeToURL("private:stream", storeProps);

                        //XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComp);
                        //XCloseable xCloseable = (XCloseable) UnoRuntime.queryInterface(XCloseable.class, xModel);
                        //xCloseable.close(false);

                        currentObject.close();
                        if (ret < 0) {
                            JOptionPane.showMessageDialog(OfficeLauncher.this, sysCaller.getLastError());
                            return;
                        }
                        currentObject = pmo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (currentActionURL.Complete.compareTo(".uno:Open") == 0) {

                    try {
                    	//TODO: uncomment 3 lines below.
                    	
                       // XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, xComp);
                       // XCloseable xCloseable = (XCloseable) UnoRuntime.queryInterface(XCloseable.class, xModel);
                       // xCloseable.close(false);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    int ret = getObjectBrowser().showOpenDialog();
                    if (ret != ObjectBrowser.PM_OK) {
                        return;
                    }

                    String sObjName = getObjectBrowser().getObjName();
                    System.out.println("Object nam: " + sObjName);

                    openInitialObj(sObjName);

                    // User clicks File/Close.
                } else if (currentActionURL.Complete.compareTo(".uno:CloseDoc") == 0) {
                    //JOptionPane.showMessageDialog(null, "Close Document Intercepted");
                    releaseInterceptor();
                    System.out.println("Close Doc: Releasing the interceptor");

                    if(currentObject!=null)
                    	currentObject.close();
                    System.out.println("Close Doc: Closing the opened object");

                    try {
                    	
                        final Object helper = xMCF.createInstanceWithContext(
                                "com.sun.star.frame.DispatchHelper",
                                xContext);
                        XDispatchHelper xdh = (XDispatchHelper) UnoRuntime.queryInterface(
                                XDispatchHelper.class, helper);
                        XDispatchProvider xdp = (XDispatchProvider) UnoRuntime.queryInterface(
                                XDispatchProvider.class, xFrame);
                        xdh.executeDispatch(xdp, ".uno:CloseDoc", "", 0, new PropertyValue[0]);
                        System.out.println("Closing the document");
                        XCloseable xc = (XCloseable) UnoRuntime.queryInterface(XCloseable.class, xComp);
                        xc.close(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Re-register the interceptor.
                    registerInterceptor();
                    System.out.println("Re-registering the interceptor");

                    /*
                    } else if (currentActionURL.Complete.compareTo(".uno:CloseWin") == 0) {
                    JOptionPane.showMessageDialog(null, "Close Window Intercepted");

                     */
                    // The user closes the application's main window.
                } else if (currentActionURL.Complete.compareTo(".uno:CloseFrame") == 0) {
                    //JOptionPane.showMessageDialog(null, "Close Frame Intercepted");
                    releaseInterceptor();
                    System.out.println("Close Frame: Releasing the interceptor");
                    
                    if(currentObject !=null)
                    	currentObject.close();
                    System.out.println("Close Frame: Closing the opened object");

                    try {
                        final Object helper = xMCF.createInstanceWithContext(
                                "com.sun.star.frame.DispatchHelper",
                                xContext);
                        XDispatchHelper xdh = (XDispatchHelper) UnoRuntime.queryInterface(
                                XDispatchHelper.class, helper);
                        XDispatchProvider xdp = (XDispatchProvider) UnoRuntime.queryInterface(
                                XDispatchProvider.class, xFrame);
//            xdh.executeDispatch(xdp, ".uno:Quit", "", 0, new PropertyValue[0]);
//            xdh.executeDispatch(xdp, ".uno:CloseFrame", "", 0, new PropertyValue[0]);
                        xdh.executeDispatch(xdp, ".uno:CloseWindow", "", 0, new PropertyValue[0]);
                        System.out.println("Close Frame: Closing the application");                      
                        terminate(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // The user clicks File/Exit.
                } else if (currentActionURL.Complete.compareTo(".uno:Quit") == 0) {
                    //JOptionPane.showMessageDialog(null, "Exit intercepted");
                    releaseInterceptor();
                    System.out.println("File/Exit: Releasing the interceptor");

                    currentObject.close();
                    System.out.println("File/Exit: Closing the opened object");

                    try {

                        final Object helper = xMCF.createInstanceWithContext(
                                "com.sun.star.frame.DispatchHelper",
                                xContext);
                        XDispatchHelper xdh = (XDispatchHelper) UnoRuntime.queryInterface(
                                XDispatchHelper.class, helper);
                        XDispatchProvider xdp = (XDispatchProvider) UnoRuntime.queryInterface(
                                XDispatchProvider.class, xFrame);
                        
                        xdh.executeDispatch(xdp, ".uno:Quit", "", 0, new PropertyValue[0]);
                        System.out.println("File/Exit: Closing the application");
                        terminate(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else if (currentActionURL.Complete.compareTo(".uno:Copy") == 0) {
                    System.out.println("Executing Copy handler...");

                    try {
                        if (!sysCaller.copyToClipboard(currentObject.getHandle(), null)) {
                            JOptionPane.showMessageDialog(null, sysCaller.getLastError());
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Exception while copying selected text: " + e.getMessage());
                        return;
                    }

                    // Release interceptor.
                    releaseInterceptor();
                    System.out.println("Releasing the interceptor");
                    try {
                        final Object helper = xMCF.createInstanceWithContext(
                                "com.sun.star.frame.DispatchHelper",
                                xContext);
                        XDispatchHelper xdh =  UnoRuntime.queryInterface(
                                XDispatchHelper.class, helper);
                        XDispatchProvider xdp = UnoRuntime.queryInterface(
                                XDispatchProvider.class, xFrame);
                        xdh.executeDispatch(xdp, ".uno:Copy", "", 0, new PropertyValue[0]);
                        System.out.println("Copying to clipboard");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Re-register the interceptor.
                    registerInterceptor();
                    System.out.println("Re-registering the interceptor");

                } else if (currentActionURL.Complete.compareTo(".uno:Paste") == 0) {
                    System.out.println("Executing Paste handler...");

                    // Check with the policy machine.
                    try {
                        boolean b = sysCaller.isPastingAllowed();
                        System.out.println("Pasting allowed: " + b);

                        if (!b) {
                            JOptionPane.showMessageDialog(null, sysCaller.getLastError());
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Exception while pasting selected text: " + e.getMessage());
                        return;
                    }

                    // Release interceptor
                    System.out.println("Releasing interceptor...");
                    releaseInterceptor();
                    // Do the paste
                    try {
                        final Object helper = xMCF.createInstanceWithContext(
                                "com.sun.star.frame.DispatchHelper", xContext);
                        XDispatchHelper xdh =
                                (XDispatchHelper) UnoRuntime.queryInterface(XDispatchHelper.class, helper);
                        XDispatchProvider xdp = (XDispatchProvider) UnoRuntime.queryInterface(
                                XDispatchProvider.class, xFrame);
                        System.out.println("Pasting from clipboard...");
                        xdh.executeDispatch(xdp, ".uno:Paste", "", 0, new PropertyValue[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Re-register interceptor
                    System.out.println("Re-registering interceptor...");
                    registerInterceptor();
                }
            }

            private ObjectBrowser getObjectBrowser() {
                if (objectBrowser == null) {
                    objectBrowser = new ObjectBrowser(OfficeLauncher.this, sysCaller, "OpenOffice Launcher");
                    objectBrowser.pack();
                }
                return objectBrowser;
            }
        });
    }

    @Override
    public void addStatusListener(XStatusListener xStatusListener, URL url) {
    }

    @Override
    public void removeStatusListener(XStatusListener xStatusListener, URL url) {
    }

    public void registerInterceptor() {
        xDispatchInterception.registerDispatchProviderInterceptor(this);
    }

    public void releaseInterceptor() {
        xDispatchInterception.releaseDispatchProviderInterceptor(this);
    }

    // Arguments on the command line:
    // -session <sessionId> -process <pId> -simport <simulator port> -objtype <object type> <virtual object name>
    // The session id, process id, and the object type are mandatory.
    // The object type can be: doc, ppt, xsl.
    public static void main(String[] args) {
        String sessid = null;
        String pid = null;
        int simport = 0;
        String objname = null;
        String objtype = null;
        boolean debug = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-session")) {
                sessid = args[++i];
            } else if (args[i].equals("-process")) {
                pid = args[++i];
            } else if (args[i].equals("-simport")) {
                simport = Integer.valueOf(args[++i]).intValue();
            } else if (args[i].equals("-objtype")) {
                objtype = args[++i];
            } else if (args[i].equals("-debug")) {
                debug = true;
            } else {
                objname = args[i];
            }
        }
        if (sessid == null || sessid.length() == 0) {
            System.out.println("The WriterLauncher must run in a PM session!");
            System.exit(-1);
        }
        if (pid == null || pid.length() == 0) {
            System.out.println("The WriterLauncher must run in a PM process!");
            System.exit(-1);
        }  
		
		System.out.println("sessid =" + sessid);
		System.out.println("pid = " + pid);
		System.out.println("simport = "+ simport);
		System.out.println("objtype = "+ objtype);
		System.out.println("objname = "+ objname);
		
        OfficeLauncher launcher = new OfficeLauncher(simport, sessid, pid, objtype, debug);
        launcher.pack();
        launcher.setVisible(true);
        launcher.openInitialObj(objname);
    }
}

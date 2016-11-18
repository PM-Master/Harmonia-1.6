package gov.nist.csd.pm.user;

import com.sun.jna.Platform;
import gov.nist.csd.pm.user.platform.win32.Win32NativeProcessWrapper;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author  Administrator
 */
public class NativeProcessWrapperManager {

    /**
	 * @uml.property  name="iNSTANCE"
	 * @uml.associationEnd  
	 */
    private final static NativeProcessWrapperManager INSTANCE = new NativeProcessWrapperManager();
    /**
	 * @uml.property  name="platformWrapperMap"
	 * @uml.associationEnd  qualifier="valueOf:java.lang.Integer java.lang.String"
	 */
    private final Map<Integer, Class<? extends NativeProcessWrapper>> platformWrapperMap =
            new HashMap<Integer, Class<? extends NativeProcessWrapper>>();

    private NativeProcessWrapperManager() {
        //We only support windows for the time being, this architecture should allow us
        //to add new platforms simply by adding to this Map.
        platformWrapperMap.put(Platform.WINDOWS, Win32NativeProcessWrapper.class);
    }

    ;

    public static NativeProcessWrapperManager getManager() {
        return INSTANCE;
    }

    public NativeProcessWrapper getNativeProcessWrapperForCurrentPlatform(Process processToWrap) {
        //System.get
        Class<? extends NativeProcessWrapper> clazz = platformWrapperMap.get(Platform.getOSType());
        if (clazz == null) {
            return null;
        }
        try {
            Constructor<? extends NativeProcessWrapper> constructor = clazz.getConstructor(Process.class);
            return constructor.newInstance(processToWrap);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    NativeProcessWrapper getEmptyNativeProcessWrapper() {
        return new NativeProcessWrapper() {

            @Override
            public Process getProcess() {
                throw new UnsupportedOperationException("This is an empty process wrapper.  There is no process.");
            }

            @Override
            public boolean bringApplicationToFront() {
                return true;
            }

            @Override
            public String[] getWindowNames() {
                return new String[0];
            }
            
            @Override
            public Icon getApplicationIcon(){
            	return new ImageIcon();
            }
        };
    }
}

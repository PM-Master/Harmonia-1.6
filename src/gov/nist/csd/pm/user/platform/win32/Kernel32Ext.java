/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.user.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.win32.W32APIOptions;

/**
 * JNA extensions for the Kernel32 interface. The Kernel32 is missing several WinAPI functions required for the policy manager.  Ideally these would be added to the JNA mainline at some point Some care was taken to ensure that the WinAPI to JNA naming conventions were appeared to.  This should enable a smoother transition to later versions of JNA that include these methods.
 * @author  Administrator
 */
public interface Kernel32Ext extends Kernel32 {
    /**
	 * Entry point to the Kernel32 WinAPI methods When interfacing with Win32 apis, it helps to add the W32APIOptions.DEFAULT_OPTIONS Some functions have ASCII and UNICODE variants.  This option will provide the appropriate mapping without having to implement ASCII and UNICODE methods in your interfaces.
	 * @uml.property  name="iNSTANCE"
	 * @uml.associationEnd  
	 */
    public static Kernel32Ext INSTANCE = (Kernel32Ext)Native.loadLibrary("Kernel32", Kernel32Ext.class, W32APIOptions.DEFAULT_OPTIONS);
    public boolean EnumResourceNames(HMODULE hModule, String lpszType, ENUMRESNAMEPROC lpEnumFunc, Pointer lParam);
    
    public static interface ENUMRESNAMEPROC extends StdCallCallback{
       public boolean callback(HMODULE hModule, String lpszType, String lpszName, Pointer lParam); 
    }
}

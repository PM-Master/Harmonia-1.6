package gov.nist.csd.pm.user.platform.win32;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser.WINDOWINFO;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;
import com.sun.jna.ptr.IntByReference;
import gov.nist.csd.pm.common.graphics.GraphicsUtil;
import gov.nist.csd.pm.user.NativeProcessWrapper;
import gov.nist.csd.pm.user.platform.win32.GDI32Ext.BITMAPINFOHEADER_MANUAL;
import gov.nist.csd.pm.user.platform.win32.WinUserExt.ICONINFO;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author  Administrator
 */
public class Win32NativeProcessWrapper implements NativeProcessWrapper {

    /**
	 * @uml.property  name="process"
	 */
    private Process process;
    /**
	 * @uml.property  name="appIcon"
	 * @uml.associationEnd  
	 */
    private Icon appIcon;
    private static Map<Integer, List<HWND>> windowProcessMap;
    private static int NUM_PIXEL_COMPONENTS = 4;

    public Win32NativeProcessWrapper(Process process) {
        super();
        this.process = process;
        this.appIcon = null;
    }

    private static long extractProcessHandle(Process proc) {
        long retVal = -1;
        if (proc != null) {
            try {
                Field handleField = proc.getClass().getDeclaredField("handle");
                handleField.setAccessible(true);
                if (handleField.getType().equals(int.class)) {
                    retVal = handleField.getInt(proc);
                } else if (handleField.getType().equals(long.class)) {
                    retVal = handleField.getLong(proc);
                }
            } catch (SecurityException e) {
                Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.INFO, e.getStackTrace().toString());
            } catch (NoSuchFieldException e) {
                Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.INFO, e.getStackTrace().toString());
            } catch (IllegalArgumentException e) {
                Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.INFO, e.getStackTrace().toString());
            } catch (IllegalAccessException e) {
                Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.INFO, e.getStackTrace().toString());
            }
        }
        return retVal;
    }

    /**
     * Gets the platform process id given a java.lang.Process
     * @param proc
     * @return
     */
    private static Integer getProcessId(Process proc) {
        long handlePeer = extractProcessHandle(proc);
        HANDLE handle = new HANDLE(Pointer.createConstant(handlePeer));
        return Kernel32.INSTANCE.GetProcessId(handle);
    }
    /**
	 * Getter for the jvm process wrapper
	 * @return
	 * @uml.property  name="process"
	 */
    @Override
    public Process getProcess() {
        return process;
    }
    /**
	 * Gets a mapping from windows process id's to a list of windows associated with those process id's.  All windows are returned, even windows that are usually hidden all the time.
	 * @return
	 * @uml.property  name="windowProcess"
	 */
    private static Map<Integer, List<HWND>> getWindowProcessMap() {
        windowProcessMap = null; //Reset windowProcessMap every time.
        if (windowProcessMap == null) {
            User32 user32 = User32.INSTANCE;
            User32Ext myuser32 = User32Ext.INSTANCE;
            windowProcessMap = new HashMap<Integer, List<HWND>>();
            final List<HWND> windows = new ArrayList<HWND>();
            user32.EnumWindows(new WNDENUMPROC() {

                @Override
                public boolean callback(HWND hwnd, Pointer pntr) {
                    windows.add(hwnd);
                    return true;
                }
            }, Pointer.NULL);

            for (HWND window : windows) {
                IntByReference ibr = new IntByReference();
                myuser32.GetWindowThreadProcessId(window, ibr);

                // System.out.printf("Putting window named %s in for process %d\n",
                // windowName, ibr.getValue());
                List<HWND> winList = windowProcessMap.get(ibr.getValue());
                if (winList == null) {
                    winList = new ArrayList<HWND>();
                    windowProcessMap.put(ibr.getValue(), winList);
                }
                winList.add(window);
            }
        }
        return windowProcessMap;
    }

    /*
     * ShowWindow
     *
     * FUNCTION ForceForegroundWindow(lnHWND)
     *
     * LOCAL nForeThread, nAppThread
     *
     * =decl()
     *
     * nForeThread = GetWindowThreadProcessId(GetForegroundWindow(), 0)
     * nAppThread = GetCurrentThreadId()
     *
     * IF nForeThread != nAppThread AttachThreadInput(nForeThread, nAppThread,
     * .T.) BringWindowToTop(lnHWND) ShowWindow(lnHWND,3)
     * AttachThreadInput(nForeThread, nAppThread, .F.) ELSE
     * BringWindowToTop(lnHWND) ShowWindow(lnHWND,3) ENDIF
     *
     * ENDFUNC
     *
     * FUNCTION Decl!* DECLARE INTEGER SetForegroundWindow IN user32 INTEGER
     * hwnd DECLARE Long BringWindowToTop In Win32API Long
     *
     * DECLARE Long ShowWindow In Win32API Long, Long
     *
     * DECLARE INTEGER GetCurrentThreadId; IN kernel32
     *
     * DECLARE INTEGER GetWindowThreadProcessId IN user32; INTEGER hWnd,;
     * INTEGER @ lpdwProcId
     *
     * DECLARE INTEGER GetCurrentThreadId; IN kernel32
     *
     * DECLARE INTEGER AttachThreadInput IN user32 ; INTEGER idAttach, ; INTEGER
     * idAttachTo, ; INTEGER fAttach
     *
     * DECLARE INTEGER GetForegroundWindow IN user32
     *
     * ENDFUNC
     */
    // http://www.tek-tips.com/faqs.cfm?fid=4262
    @Override
    public boolean bringApplicationToFront() {
        boolean success = false;
        User32Ext myuser32 = User32Ext.INSTANCE;

        Map<Integer, List<HWND>> windowProcessMap = getWindowProcessMap();
        Integer processId = getProcessId(getProcess());
        System.out.println("Retrieved procid " + processId);
        List<HWND> selectedWindows = windowProcessMap.get(processId);

        if (selectedWindows != null) {
            for (HWND selectedWindow : selectedWindows) {
                System.out.println("Selected window name "
                        + User32ExtUtil.getWindowText(selectedWindow));
                Kernel32 k32 = Kernel32.INSTANCE;
                System.out.println("Found a window and bringing it to the front.");

                boolean result = true;
                int foreThread = myuser32.GetWindowThreadProcessId(
                        myuser32.GetForegroundWindow(), null);
                int targetThread = myuser32.GetWindowThreadProcessId(
                        selectedWindow, null);
                int appThread = k32.GetCurrentThreadId();
                System.out.printf("Button clicker: Fore %d App %d target %d\n",
                        foreThread, appThread, targetThread);
                if (SwingUtilities.isEventDispatchThread()) {
                    System.out.println("is the event dispatch thread.");
                }
                if (foreThread != appThread) {
                    // If the foreground thread is not the same as the
                    // application thread
                    // we cascade the thread input so we can call the window's
                    // in another
                    // thread to the foreground.
                    result = myuser32.AttachThreadInput(appThread, foreThread,
                            true);
                    result = myuser32.AttachThreadInput(targetThread,
                            appThread, true);
                    bringWindowToFront(selectedWindow);
                    result = myuser32.AttachThreadInput(appThread, foreThread,
                            false);
                    result = myuser32.AttachThreadInput(targetThread,
                            appThread, false);
                    processResult(result, "DetachThreadInput");
                } else {
                    System.out.println("Foreground and app thread alike.");
                    bringWindowToFront(selectedWindow);
                    processResult(result, "SetForegroundWindow");

                }
            }
        } else {
            System.out.println("Could not find the window in our window list.");
        }

        return success;
    }
/**
 * Brings a selected window to the front.  If the window is a hidden or non-visible
 * nothing will happen.  This function checks the style of the window to ensure that
 * non-visible windows remain hidden.
 * @param selectedWindow
 */
    private void bringWindowToFront(HWND selectedWindow) {
        User32 user32 = User32.INSTANCE;
        User32Ext myuser32 = User32Ext.INSTANCE;
        boolean result;
        WINDOWINFO winfo = new WINDOWINFO();
        result = user32.GetWindowInfo(selectedWindow, winfo);
        // If the windows are not titled then we don't even want to TRY to bring
        // them to the front. There are more windows than just what you can see
        // in every application.
        if (result
                && ((winfo.dwStyle & WinUserExt.WS_TILEDWINDOW) == WinUserExt.WS_TILEDWINDOW)) {
            if ((winfo.dwStyle & WinUserExt.WS_MINIMIZE) == WinUserExt.WS_MINIMIZE) {
                result = myuser32.ShowWindow(selectedWindow,
                        WinUserExt.SW_RESTORE);
                processResult(result, "Show Window");
            }
            result = myuser32.BringWindowToTop(selectedWindow);
            processResult(result, "Bring to top");
        }
    }
/**
 * Convenience method for getting all the handles from a java.util.Process
 * @param process - the process we want the windows from.
 * @return
 */
    private static List<HWND> getWindowHandlesFromProcess(Process process) {
        final User32 user32 = User32.INSTANCE;
        Collection<HWND> windows = getWindowProcessMap().get(getProcessId(process));
        if (windows == null) {
            System.out.println("There was a problem with the window getting");
            windows = new ArrayList<HWND>();
        }
        windows = Collections2.filter(windows, new Predicate<HWND>(){
            @Override
            public boolean apply(HWND window) {
                boolean result;
                WINDOWINFO winfo = new WINDOWINFO();
                result = user32.GetWindowInfo(window, winfo);
                // If the windows are not titled then we don't even want to TRY to bring
                // them to the front. There are more windows than just what you can see
                // in every application.
                return (result
                        && ((winfo.dwStyle & WinUserExt.WS_TILEDWINDOW) == WinUserExt.WS_TILEDWINDOW));
            }
        });
        return new ArrayList<HWND>(windows);
    }
    /**
     * Gets the Windows instance handle given a java.util.Process
     * @param process - the process we want the instance handle of
     * @return - the instance handle if found, null otherwise.
     */
    private static HINSTANCE getInstanceFromProcess(Process process) {
        // throw new
        // UnsupportedOperationException("getInstanceFromProcess is not yet implemented.");
        HINSTANCE hinst = null;
        User32 u32 = User32.INSTANCE;
        List<HWND> windows = getWindowHandlesFromProcess(process);
        if (windows.size() > 0) {
            hinst = new HINSTANCE();
            HWND window = windows.get(0);
            hinst.setPointer(Pointer.createConstant(u32.GetWindowLong(window,
                    WinUser.GWL_HINSTANCE)));
        }
        return hinst;
    }
    /**
     * Convenience method for writing native memory into java.awt.Icon
     *
     * This method is NOT optimized and will be slow for large images.  System icons
     * should be fine, but do not use for large images.
     *
     * Once in a java.awt.Icon the native resources are no longer needed.
     * @param memory - JNA native memory wrapper
     * @param bitmapInfo - Custom BITMAPINFOHEADER jna struct wrapper. See windows struct BITMAPINFOHEADER for usages and details.
     * @return - a java.awt.Icon representing the memory.
     */
    private static Icon writeMemoryToIcon(Memory memory, BITMAPINFOHEADER_MANUAL bitmapInfo) {
        int width = bitmapInfo.biWidth;
        int height = bitmapInfo.biHeight;
        int[] src = memory.getIntArray(0, (int) memory.size() / 4);

        Image image = GraphicsUtil.imageFromRawData(src, width, height, true);
        return new ImageIcon(image);
    }
    /**
     * Gets a java.awt.Icon from a Windows icon handle.
     * @param hIcon
     * @return
     */
    private Icon getIconFromHICON(HICON hIcon) {
        User32 u32 = User32.INSTANCE;
        User32Ext u32Ext = User32Ext.INSTANCE;
        GDI32 gdi = GDI32.INSTANCE;
        GDI32Ext gdie = GDI32Ext.INSTANCE;
        Icon icon;
        ICONINFO iconInfo = new ICONINFO();
        u32Ext.GetIconInfo(hIcon, iconInfo);
        HDC screenDC = u32.GetDC(null);

        BITMAPINFOHEADER_MANUAL bitmapInfo = new BITMAPINFOHEADER_MANUAL(new Memory(100));
        gdie.GetDIBits(screenDC, iconInfo.hbmColor, 0, 0, null, bitmapInfo, WinGDI.DIB_RGB_COLORS);

        int height = bitmapInfo.biHeight;
        int imageSize = bitmapInfo.biSizeImage;

        if (imageSize == 0) {
            return new ImageIcon();
        }
        Memory pMem = new Memory(imageSize);
        gdie.GetDIBits(screenDC, iconInfo.hbmColor, 0, height, pMem, bitmapInfo, WinGDI.DIB_RGB_COLORS);
        icon = writeMemoryToIcon(pMem, bitmapInfo);
        u32.ReleaseDC(null, screenDC);
        gdi.DeleteObject(iconInfo.hbmColor);
        gdi.DeleteObject(iconInfo.hbmMask);
        return icon;
    }


    /**
     * Gets the first window visible in the given process.  It will try this every retryPeriod milliseconds until
     * timeoutMillis milliseconds has elapsed.
     * @param process - The process we care about getting windows from
     * @param timeoutMillis - How long to try getting these windows
     * @param retryPeriod - How long to wait before trying again.
     * @return - the first window visible in the application or null if no windows have been found.
     */
    private static HWND getFirstVisibleWindow(Process process, long timeoutMillis, long retryPeriod) {
        Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.FINE, "Getting the first visible window");
        User32 user32 = User32.INSTANCE;

        long endTime = System.currentTimeMillis() + timeoutMillis;
        HWND visibleWindow = null;
        boolean success = false;
        Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.FINER, "Waiting for the window to become visible.");
        Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.FINEST, "{1} < {2} endTime; VISABL {3}", new Object[]{System.currentTimeMillis(), endTime, visibleWindow});
        while (System.currentTimeMillis() < endTime && visibleWindow == null) {
            List<HWND> windows = getWindowHandlesFromProcess(process);
            for (HWND window : windows) {
                Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.FINE, "Getting window info.");
                WINDOWINFO winfo = new WINDOWINFO();
                success = user32.GetWindowInfo(window, winfo);
                if (success) {
                    if ((winfo.dwWindowStatus & WinUserExt.WS_ACTIVECAPTION) == WinUserExt.WS_ACTIVECAPTION) {
                        Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.FINE, "Window is active");
                    }
                    if ((winfo.dwStyle & WinUserExt.WS_VISIBLE) == WinUserExt.WS_VISIBLE) {
                        Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.FINE, "Window is visible");
                        visibleWindow = window;
                    }
                }
            }
            try {
                Thread.sleep(retryPeriod);
            } catch (InterruptedException ex) {
                Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.FINE, "{1} < {2} endTime; VISABL {3}", new Object[]{System.currentTimeMillis(), endTime, visibleWindow});

        }
        return visibleWindow;
    }

    /**
     * Returns the application icon for this process.
     * We assume that the application icon is the window icon for the first window visible in the application.
     * @return - the application icon for this process.
     */
    @Override
    public Icon getApplicationIcon() {
        if (appIcon == null) {
            User32Ext u32e = User32Ext.INSTANCE;
            HWND selectedWindow = getFirstVisibleWindow(process, 10000, 500);
            DWORD value = u32e.GetClassLong(selectedWindow, WinUserExt.GCL_HICONSM);
            if (value == null) {
                Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.FINE, "There was no icon handle");
                return new ImageIcon();
            }
            HICON hIcon = new HICON();
            hIcon.setPointer(Pointer.createConstant(value.intValue()));
            appIcon = getIconFromHICON(hIcon);
        } else {
            Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.FINE, "Couldn't find any window handles.");

        }
        return appIcon;
    }

    /**
     * Process the result of a function call.  This is primarily used for dealing
     * with the results returned from the WinAPI.  As an improvement, we could get the
     * LastErrorMessage from the WinAPI and log it here.
     * @param result - the exit code of the function called.
     * @param context - the context of the process, for disambiguation.
     */
    private void processResult(boolean result, String context) {
        if (!result) {
            String errorMessage = Kernel32Util.formatMessageFromLastErrorCode(Kernel32.INSTANCE.GetLastError());
            Logger.getLogger(Win32NativeProcessWrapper.class.getName()).log(Level.INFO, "Call Failed: {1} - {2}", new Object[]{context, errorMessage});
        }
    }

    /**
     * Utility for getting a window's title using the WinAPI.
     * @param window - a window handle.
     * @return - the title of the window specified by the handle as a String
     */
    private static String getNameForWindow(HWND window){
        int length = User32.INSTANCE.GetWindowTextLength(window);
        char[] nameChars = new char[length];
        User32.INSTANCE.GetWindowText(window, nameChars, length);
        return new String(nameChars, 0, length - 2);
    }
    /**
     * Returns the names of all appplication windows.
     * @return - a string array containing the names of all the windows in this application.
     */
    @Override
    public String[] getWindowNames() {
        List<String> windowNames = new ArrayList<String>();
        List<HWND> handles = getWindowHandlesFromProcess(process);
        for(HWND handle : handles){
            windowNames.add(getNameForWindow(handle));
        }
        return windowNames.toArray(new String[0]);
    }


}

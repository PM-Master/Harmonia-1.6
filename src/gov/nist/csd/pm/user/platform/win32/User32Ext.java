/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.user.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.W32APIOptions;
import gov.nist.csd.pm.user.platform.win32.WinUserExt.ICONINFO;

/**
 * JNA Extension of the JNA User32 WinAPI interface.
 * @author  Administrator
 */
public interface User32Ext extends User32 {

    /**
	 * Instance and entry point into the User32 Windows library.
	 * @uml.property  name="iNSTANCE"
	 * @uml.associationEnd  
	 */
    public static User32Ext INSTANCE = (User32Ext) Native.loadLibrary("User32", User32Ext.class, W32APIOptions.DEFAULT_OPTIONS);

    /**
     *  Parameters
     *  @param - hWnd [in]
     *  Type: HWND
     *  A handle to the window.
     *  @param - fAltTab [in]
     *  Type: BOOL
     *  A TRUE for this parameter indicates that the window is being switched to using the Alt/Ctl+Tab key sequence. This parameter should be FALSE otherwise.
     *  @return
     *  This function does not return a value.
     *  Remarks
     *  This function is typically called to maintain window z-ordering.
     *  This function was not included in the SDK headers and libraries until Windows XP with Service Pack 1 (SP1) and Windows Server 2003. If you do not have a header file and import library for this function, you can call the function using LoadLibrary and GetProcAddress.
     */
    void SwitchToThisWindow(HWND hwnd, boolean fAltTab);

    /**
     *  Parameters
     *  @param - hWnd [in]
     *  Type: HWND
     *  A handle to the window.
     *  lpdwProcessId [out, optional]
     *  Type: LPDWORD
     *  A pointer to a variable that receives the process identifier. If this parameter is not NULL, GetWindowThreadProcessId copies the identifier of the process to the variable; otherwise, it does not.
     *  @return
     *  Type: DWORD
     *  The return value is the identifier of the thread that created the window.
     */
    int GetWindowThreadProcessId(HWND hWnd, PointerType lpdwProcessId);

    /**
     *  Parameters
     *  This function has no parameters.
     *  @return
     *  Type: HWND
     *  The return value is a handle to the foreground window. The foreground window can be NULL in certain circumstances, such as when a window is losing activation.
     *  Requirements
     */
    HWND GetForegroundWindow();

    /**
     *  Parameters
     *  @param - hWnd [in]
     *  Type: HWND
     *  A handle to the window to bring to the top of the Z order.
     *  @return
     *  Type: BOOL
     *  If the function succeeds, the return value is nonzero.
     *  If the function fails, the return value is zero. To get extended error information, call GetLastError.
     *  Remarks
     *  Use the BringWindowToTop function to uncover any window that is partially or completely obscured by other windows.
     *  Calling this function is similar to calling the SetWindowPos function to change a window's position in the Z order. BringWindowToTop does not make a window a top-level window.
     */
    boolean BringWindowToTop(HWND hWnd);

    /**
     *  Parameters
     *  @param - hWnd [in]
     *  Type: HWND
     *  A handle to the window that should be activated and brought to the foreground.
     *  @return
     *  Type: BOOL
     *  If the window was brought to the foreground, the return value is nonzero.
     *  If the window was not brought to the foreground, the return value is zero.
     *  Remarks
     *  The system restricts which processes can set the foreground window. A process can set the foreground window only if one of the following conditions is true:
     * <ul>
     *  <li>The process is the foreground process.</li>
     *  <li>The process was started by the foreground process.</li>
     *  <li>The process received the last input event.</li>
     *  <li>There is no foreground process.</li>
     *  <li>The foreground process is being debugged.</li>
     *  <li>The foreground is not locked (see LockSetForegroundWindow).</li>
     *  <li>The foreground lock time-out has expired (see SPI_GETFOREGROUNDLOCKTIMEOUT in SystemParametersInfo).</li>
     *  <li>No menus are active.</li>
     * </ul>
     *  An application cannot force a window to the foreground while the user is working with another window. Instead, Foreground and Background Windows will activate the window (see SetActiveWindow) and call the function to notify the user.
     *  A process that can set the foreground window can enable another process to set the foreground window by calling the AllowSetForegroundWindow function. The process specified by dwProcessId loses the ability to set the foreground window the next time the user generates input, unless the input is directed at that process, or the next time a process calls AllowSetForegroundWindow, unless that process is specified.
     *  The foreground process can disable calls to SetForegroundWindow by calling the LockSetForegroundWindow function.
     */
    boolean SetForegroundWindow(HWND hWnd);

    /**
     *  @param - hWnd [in]
     *  Type: HWND
     *  A handle to the window.
     *  @param - nCmdShow [in]
     *  Type: int
     *  Controls how the window is to be shown. This parameter is ignored the first time an application calls ShowWindow, if the program that launched the application provides a STARTUPINFO structure. Otherwise, the first time ShowWindow is called, the value should be the value obtained by the WinMain function in its nCmdShow parameter. In subsequent calls, this parameter can be one of the following values.
     *  table>
     *  <tr><th>Value</th><th>Meaning</th></tr>
     *  <tr><td><a id="SW_FORCEMINIMIZE"></a><dl>
     *  <dt>SW_FORCEMINIMIZE</dt>
     *  <dt>11</dt>
     *  </dl>
     *  </td><td>
     *  <p>
     *  						 Minimizes a window, even if the thread that owns the window is not responding. This flag should only be used when minimizing windows from a different thread.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_HIDE"></a><dl>
     *  <dt>SW_HIDE</dt>
     *  <dt>0</dt>
     *  </dl>
     *  </td><td>
     *  <p>Hides the window and activates another window.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_MAXIMIZE"></a><dl>
     *  <dt>SW_MAXIMIZE</dt>
     *  <dt>3</dt>
     *  </dl>
     *  </td><td>
     *  <p>Maximizes the specified window.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_MINIMIZE"></a><dl>
     *  <dt>SW_MINIMIZE</dt>
     *  <dt>6</dt>
     *  </dl>
     *  </td><td>
     *  <p>Minimizes the specified window and activates the next top-level window in the Z order.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_RESTORE"></a><dl>
     *  <dt>SW_RESTORE</dt>
     *  <dt>9</dt>
     *  </dl>
     *  </td><td>
     *  <p>Activates and displays the window. If the window is minimized or maximized, the system restores it to its original size and position. An application should specify this flag when restoring a minimized window.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_SHOW"></a><dl>
     *  <dt>SW_SHOW</dt>
     *  <dt>5</dt>
     *  </dl>
     *  </td><td>
     *  <p>Activates the window and displays it in its current size and position. </p>
     *  </td></tr>
     *  <tr><td><a id="SW_SHOWDEFAULT"></a><dl>
     *  <dt>SW_SHOWDEFAULT</dt>
     *  <dt>10</dt>
     *  </dl>
     *  </td><td>
     *  <p>Sets the show state based on the SW_ value specified in the <a href="http://msdn.microsoft.com/en-us/library/ms686331(v=VS.85).aspx"><strong xmlns="http://www.w3.org/1999/xhtml">STARTUPINFO</strong></a> structure passed to the <a href="http://msdn.microsoft.com/en-us/library/ms682425(v=VS.85).aspx"><strong xmlns="http://www.w3.org/1999/xhtml">CreateProcess</strong></a> function by the program that started the application. </p>
     *  </td></tr>
     *  <tr><td><a id="SW_SHOWMAXIMIZED"></a><dl>
     *  <dt>SW_SHOWMAXIMIZED</dt>
     *  <dt>3</dt>
     *  </dl>
     *  </td><td>
     *  <p>Activates the window and displays it as a maximized window.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_SHOWMINIMIZED"></a><dl>
     *  <dt>SW_SHOWMINIMIZED</dt>
     *  <dt>2</dt>
     *  </dl>
     *  </td><td>
     *  <p>Activates the window and displays it as a minimized window.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_SHOWMINNOACTIVE"></a><dl>
     *  <dt>SW_SHOWMINNOACTIVE</dt>
     *  <dt>7</dt>
     *  </dl>
     *  </td><td>
     *  <p>Displays the window as a minimized window. This value is similar to SW_SHOWMINIMIZED, except the window is not activated.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_SHOWNA"></a><dl>
     *  <dt>SW_SHOWNA</dt>
     *  <dt>8</dt>
     *  </dl>
     *  </td><td>
     *  <p>Displays the window in its current size and position. This value is similar to SW_SHOW, except that the window is not activated.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_SHOWNOACTIVATE"></a><dl>
     *  <dt>SW_SHOWNOACTIVATE</dt>
     *  <dt>4</dt>
     *  </dl>
     *  </td><td>
     *  <p>Displays a window in its most recent size and position. This value is similar to SW_SHOWNORMAL, except that the window is not activated.</p>
     *  </td></tr>
     *  <tr><td><a id="SW_SHOWNORMAL"></a><dl>
     *  <dt>SW_SHOWNORMAL</dt>
     *  <dt>1</dt>
     *  </dl>
     *  </td><td>
     *  <p>Activates and displays a window. If the window is minimized or maximized, the system restores it to its original size and position. An application should specify this flag when displaying the window for the first time.</p>
     *  </td></tr>
     *  </table>
     *  @return
     *  Type: BOOL
     *  If the window was previously visible, the return value is nonzero.
     *  If the window was previously hidden, the return value is zero.
     *  Remarks
     *  To perform certain special effects when showing or hiding a window, use AnimateWindow.
     *  The first time an application calls ShowWindow, it should use the WinMain function's nCmdShow parameter as its nCmdShow parameter. Subsequent calls to ShowWindow must use one of the values in the given list, instead of the one specified by the WinMain function's nCmdShow parameter.
     *  As noted in the discussion of the nCmdShow parameter, the nCmdShow value is ignored in the first call to ShowWindow if the program that launched the application specifies startup information in the structure. In this case, ShowWindow uses the information specified in the STARTUPINFO structure to show the window. On subsequent calls, the application must call ShowWindow with nCmdShow set to SW_SHOWDEFAULT to use the startup information provided by the program that launched the application. This behavior is designed for the following situations:
     *  Applications create their main window by calling CreateWindow with the WS_VISIBLE flag set.
     *  Applications create their main window by calling CreateWindow with the WS_VISIBLE flag cleared, and later call ShowWindow with the SW_SHOW flag set to make it visible.
     */
    boolean ShowWindow(HWND hWnd, int nCmdShow);

    /**
     *  @param - idAttach [in]
     *  The identifier of the thread to be attached to another thread. The thread to be attached cannot be a system thread.
     *  @param - idAttachTo [in]
     *  The identifier of the thread to which idAttach will be attached. This thread cannot be a system thread.
     *  A thread cannot attach to itself. Therefore, idAttachTo cannot equal idAttach.
     *  @param - fAttach [in]
     *  If this parameter is TRUE, the two threads are attached. If the parameter is FALSE, the threads are detached.
     *  @return
     *  If the function succeeds, the return value is nonzero.
     *  If the function fails, the return value is zero. To get extended error information, call GetLastError.
     *  Windows Server 2003 and Windows XP/2000:  There is no extended error information; do not call GetLastError. This behavior changed as of Windows Vista.
     *  Remarks
     *  Windows created in different threads typically process input independently of each other. That is, they have their own input states (focus, active, capture windows, key state, queue status, and so on), and their input processing is not synchronized with the input processing of other threads. By using the AttachThreadInput function, a thread can attach its input processing mechanism to another thread. Keyboard and mouse events received by both threads are processed by the thread specified by the idAttachTo parameter until the threads are detached by calling AttachThreadInput a second time and specifying FALSE for the fAttach parameter. This also allows threads to share their input states, so they can call the SetFocus function to set the keyboard focus to a window of a different thread. This also allows threads to get key-state information.
     *  The AttachThreadInput function fails if either of the specified threads does not have a message queue. The system creates a thread's message queue when the thread makes its first call to one of the USER or GDI functions. The AttachThreadInput function also fails if a journal record hook is installed. Journal record hooks attach all input queues together.
     *  Note that key state, which can be ascertained by calls to the GetKeyState or GetKeyboardState function, is reset after a call to AttachThreadInput. You cannot attach a thread to a thread in another desktop.
     */
    boolean AttachThreadInput(int idAttach, int idAttachTo, boolean fAttach);

    /**
     *  Parameters
     *  @param - hWnd [in]
     *  HWND
     *  A handle to the top-level window to be activated.
     *  @return
     *  HWND
     *  If the function succeeds, the return value is the handle to the window that was previously active.
     *  If the function fails, the return value is NULL. To get extended error information, call GetLastError.
     *  Remarks
     *  The SetActiveWindow function activates a window, but not if the application is in the background. The window will be brought into the foreground (top of Z-Order) if its application is in the foreground when the system activates the window.
     *  If the window identified by the hWnd parameter was created by the calling thread, the active window status of the calling thread is set to hWnd. Otherwise, the active window status of the calling thread is set to NULL.
     *  By using the AttachThreadInput function, a thread can attach its input processing to another thread. This allows a thread to call SetActiveWindow to activate a window attached to another thread's message queue.
     */
    HWND SetActiveWindow(HWND hWnd);

    //Icon stuff
    /**
     *  Parameters
     *  @param - hIcon [in]
     *  HICON
     *  A handle to the icon or cursor. To retrieve information about a standard icon or cursor, specify one of the following values.
     *
     *  <table>
     *  <tr><th>Value</th><th>Meaning</th></tr>
     *  <tr><td><a id="IDC_APPSTARTING"></a><dl>
     *  <dt>IDC_APPSTARTING</dt>
     *  <dt>MAKEINTRESOURCE(32650)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Standard arrow and small hourglass cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_ARROW"></a><dl>
     *  <dt>IDC_ARROW</dt>
     *  <dt>MAKEINTRESOURCE(32512)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Standard arrow cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_CROSS"></a><dl>
     *  <dt>IDC_CROSS</dt>
     *  <dt>MAKEINTRESOURCE(32515)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Crosshair cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_HAND"></a><dl>
     *  <dt>IDC_HAND</dt>
     *  <dt>MAKEINTRESOURCE(32649)</dt>
     *  </dl>
     *  </td><td>
     *  <p>
     *  						 Hand cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_HELP"></a><dl>
     *  <dt>IDC_HELP</dt>
     *  <dt>MAKEINTRESOURCE(32651)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Arrow and question mark cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_IBEAM"></a><dl>
     *  <dt>IDC_IBEAM</dt>
     *  <dt>MAKEINTRESOURCE(32513)</dt>
     *  </dl>
     *  </td><td>
     *  <p>I-beam cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_NO"></a><dl>
     *  <dt>IDC_NO</dt>
     *  <dt>MAKEINTRESOURCE(32648)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Slashed circle cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_SIZEALL"></a><dl>
     *  <dt>IDC_SIZEALL</dt>
     *  <dt>MAKEINTRESOURCE(32646)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Four-pointed arrow cursor pointing north, south, east, and west.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_SIZENESW"></a><dl>
     *  <dt>IDC_SIZENESW</dt>
     *  <dt>MAKEINTRESOURCE(32643)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Double-pointed arrow cursor pointing northeast and southwest.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_SIZENS"></a><dl>
     *  <dt>IDC_SIZENS</dt>
     *  <dt>MAKEINTRESOURCE(32645)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Double-pointed arrow cursor pointing north and south.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_SIZENWSE"></a><dl>
     *  <dt>IDC_SIZENWSE</dt>
     *  <dt>MAKEINTRESOURCE(32642)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Double-pointed arrow cursor pointing northwest and southeast.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_SIZEWE"></a><dl>
     *  <dt>IDC_SIZEWE</dt>
     *  <dt>MAKEINTRESOURCE(32644)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Double-pointed arrow cursor pointing west and east.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_UPARROW"></a><dl>
     *  <dt>IDC_UPARROW</dt>
     *  <dt>MAKEINTRESOURCE(32516)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Vertical arrow cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IDC_WAIT"></a><dl>
     *  <dt>IDC_WAIT</dt>
     *  <dt>MAKEINTRESOURCE(32514)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Hourglass cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_APPLICATION"></a><dl>
     *  <dt>IDI_APPLICATION</dt>
     *  <dt>MAKEINTRESOURCE(32512)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Application icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_ASTERISK"></a><dl>
     *  <dt>IDI_ASTERISK</dt>
     *  <dt>MAKEINTRESOURCE(32516)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Asterisk icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_EXCLAMATION"></a><dl>
     *  <dt>IDI_EXCLAMATION</dt>
     *  <dt>MAKEINTRESOURCE(32515)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Exclamation point icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_HAND"></a><dl>
     *  <dt>IDI_HAND</dt>
     *  <dt>MAKEINTRESOURCE(32513)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Stop sign icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_QUESTION"></a><dl>
     *  <dt>IDI_QUESTION</dt>
     *  <dt>MAKEINTRESOURCE(32514)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Question-mark icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_WINLOGO"></a><dl>
     *  <dt>IDI_WINLOGO</dt>
     *  <dt>MAKEINTRESOURCE(32517)</dt>
     *  </dl>
     *  </td><td>
     *  <p> Application icon.</p>
     *  <blockquote><div><strong>Windows 2000:  </strong>Windows logo icon. </div></blockquote>
     *  </td></tr>
     *  </table>
     *  @param - piconinfo [out]
     *  PICONINFO
     *  A pointer to an ICONINFO structure. The function fills in the structure's members.
     *  @return
     *  BOOL
     *  If the function succeeds, the return value is nonzero and the function fills in the members of the specified ICONINFO structure.
     *  If the function fails, the return value is zero. To get extended error information, call GetLastError.
     *  Remarks
     *  GetIconInfo creates bitmaps for the hbmMask and hbmColor members of ICONINFO. The calling application must manage these bitmaps and delete them when they are no longer necessary.
     */
    boolean GetIconInfo(HICON icon, ICONINFO iconInfo);

    /**
     *  Parameters
     *  @param - hDC [in]
     *  HDC
     *  A handle to the device context into which the icon or cursor will be drawn.
     *  @param - X [in]
     *  int
     *  The logical x-coordinate of the upper-left corner of the icon.
     *  @param - Y [in]
     *  int
     *  The logical y-coordinate of the upper-left corner of the icon.
     *  @param - hIcon [in]
     *  HICON
     *  A handle to the icon to be drawn.
     *  @return
     *  BOOL
     *  If the function succeeds, the return value is nonzero.
     *  If the function fails, the return value is zero. To get extended error information, call GetLastError.
     *  Remarks
     *  DrawIcon places the icon's upper-left corner at the location specified by the X and Y parameters. The location is subject to the current mapping mode of the device context.
     *  DrawIcon draws the icon or cursor using the width and height specified by the system metric values for icons; for more information, see GetSystemMetrics.
     */
    boolean DrawIcon(HDC hDC, int x, int y, HICON icon);

    /**
     *  Parameters
     *  hInstance [in, optional]
     *  HINSTANCE
     *  A handle to an instance of the module whose executable file contains the icon to be loaded. This parameter must be NULL when a standard icon is being loaded.
     *  @param - lpIconName [in]
     *  LPCTSTR
     *  The name of the icon resource to be loaded. Alternatively, this parameter can contain the resource identifier in the low-order word and zero in the high-order word. Use the MAKEINTRESOURCE macro to create this value.
     *  To use one of the predefined icons, set the hInstance parameter to NULL and the lpIconName parameter to one of the following values.
     *  <table>
     *  <tr><th>Value</th><th>Meaning</th></tr>
     *  <tr><td><a id="IDI_APPLICATION"></a><dl>
     *  <dt>IDI_APPLICATION</dt>
     *  <dt>MAKEINTRESOURCE(32512)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Default application icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_ASTERISK"></a><dl>
     *  <dt>IDI_ASTERISK</dt>
     *  <dt>MAKEINTRESOURCE(32516)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Asterisk icon. Same as IDI_INFORMATION.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_ERROR"></a><dl>
     *  <dt>IDI_ERROR</dt>
     *  <dt>MAKEINTRESOURCE(32513)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Hand-shaped icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_EXCLAMATION"></a><dl>
     *  <dt>IDI_EXCLAMATION</dt>
     *  <dt>MAKEINTRESOURCE(32515)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Exclamation point icon. Same as IDI_WARNING.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_HAND"></a><dl>
     *  <dt>IDI_HAND</dt>
     *  <dt>MAKEINTRESOURCE(32513)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Hand-shaped icon. Same as IDI_ERROR. </p>
     *  </td></tr>
     *  <tr><td><a id="IDI_INFORMATION"></a><dl>
     *  <dt>IDI_INFORMATION</dt>
     *  <dt>MAKEINTRESOURCE(32516)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Asterisk icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_QUESTION"></a><dl>
     *  <dt>IDI_QUESTION</dt>
     *  <dt>MAKEINTRESOURCE(32514)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Question mark icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_SHIELD"></a><dl>
     *  <dt>IDI_SHIELD</dt>
     *  <dt>MAKEINTRESOURCE(32518)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Security Shield icon. </p>
     *  </td></tr>
     *  <tr><td><a id="IDI_WARNING"></a><dl>
     *  <dt>IDI_WARNING</dt>
     *  <dt>MAKEINTRESOURCE(32515)</dt>
     *  </dl>
     *  </td><td>
     *  <p>Exclamation point icon.</p>
     *  </td></tr>
     *  <tr><td><a id="IDI_WINLOGO"></a><dl>
     *  <dt>IDI_WINLOGO</dt>
     *  <dt>MAKEINTRESOURCE(32517)</dt>
     *  </dl>
     *  </td><td>
     *  <p>
     *  						Default application icon.</p>
     *  <blockquote><div><strong>Windows 2000:  </strong>Windows logo icon.</div></blockquote>
     *  </td></tr>
     *  </table>
     *
     *  @return
     *  HICON
     *  If the function succeeds, the return value is a handle to the newly loaded icon.
     *  If the function fails, the return value is NULL. To get extended error information, call GetLastError.
     *  Remarks
     *  LoadIcon loads the icon resource only if it has not been loaded; otherwise, it retrieves a handle to the existing resource. The function searches the icon resource for the icon most appropriate for the current display. The icon resource can be a color or monochrome bitmap.
     *  LoadIcon can only load an icon whose size conforms to the SM_CXICON and SM_CYICON system metric values. Use the LoadImage function to load icons of other sizes.
     */
    HICON LoadIcon(HINSTANCE hModule, Pointer lpSzName);

    /**
     *  Parameters
     *  hinst [in, optional]
     *  HINSTANCE
     *  A handle to the module of either a DLL or executable (.exe) that contains the image to be loaded. For more information, see GetModuleHandle. Note that as of 32-bit Windows, an instance handle (HINSTANCE), such as the application instance handle exposed by system function call of WinMain, and a module handle (HMODULE) are the same thing.
     *  To load an OEM image, set this parameter to NULL.
     *  To load a stand-alone resource (icon, cursor, or bitmap file)for example, c:\myimage.bmp; set this parameter to NULL.
     *  @param - lpszName [in]
     *  LPCTSTR
     *  The image to be loaded. If the hinst parameter is non-NULL and the fuLoad parameter omits LR_LOADFROMFILE, lpszName specifies the image resource in the hinst module. If the image resource is to be loaded by name from the module, the lpszName parameter is a pointer to a null-terminated string that contains the name of the image resource. If the image resource is to be loaded by ordinal from the module, use the MAKEINTRESOURCE macro to convert the image ordinal into a form that can be passed to the LoadImage function. For more information, see the Remarks section below.
     *  If the hinst parameter is NULL and the fuLoad parameter omits the LR_LOADFROMFILE value, the lpszName specifies the OEM image to load. The OEM image identifiers are defined in Winuser.h and have the following prefixes.
     *  <table>
     *  <tr><th>Prefix</th><th>Meaning</th></tr>
     *  <tr><td>OBM_</td><td>OEM bitmaps</td></tr>
     *  <tr><td>OIC_</td><td>OEM icons</td></tr>
     *  <tr><td>OCR_</td><td>OEM cursors</td></tr>
     *  </table>
     *
     *  To pass these constants to the LoadImage function, use the MAKEINTRESOURCE macro. For example, to load the OCR_NORMAL cursor, pass MAKEINTRESOURCE(OCR_NORMAL) as the lpszName parameter, NULL as the hinst parameter, and LR_SHARED as one of the flags to the fuLoad parameter.
     *  If the fuLoad parameter includes the LR_LOADFROMFILE value, lpszName is the name of the file that contains the stand-alone resource (icon, cursor, or bitmap file). Therefore, set hinst to NULL.
     *  @param - uType [in]
     *  UINT
     *  The type of image to be loaded. This parameter can be one of the following values.
     *  <table>
     *  <tr><th>Value</th><th>Meaning</th></tr>
     *  <tr><td><a id="IMAGE_BITMAP"></a><dl>
     *  <dt>IMAGE_BITMAP</dt>
     *  <dt>0</dt>
     *  </dl>
     *  </td><td>
     *  <p>Loads a bitmap.</p>
     *  </td></tr>
     *  <tr><td><a id="IMAGE_CURSOR"></a><dl>
     *  <dt>IMAGE_CURSOR</dt>
     *  <dt>2</dt>
     *  </dl>
     *  </td><td>
     *  <p>Loads a cursor.</p>
     *  </td></tr>
     *  <tr><td><a id="IMAGE_ICON"></a><dl>
     *  <dt>IMAGE_ICON</dt>
     *  <dt>1</dt>
     *  </dl>
     *  </td><td>
     *  <p>Loads an icon.</p>
     *  </td></tr>
     *  </table>
     *
     *  @param - cxDesired [in]
     *  int
     *  The width, in pixels, of the icon or cursor. If this parameter is zero and the fuLoad parameter is LR_DEFAULTSIZE, the function uses the SM_CXICON or SM_CXCURSOR system metric value to set the width. If this parameter is zero and LR_DEFAULTSIZE is not used, the function uses the actual resource width.
     *  @param - cyDesired [in]
     *  int
     *  The height, in pixels, of the icon or cursor. If this parameter is zero and the fuLoad parameter is LR_DEFAULTSIZE, the function uses the SM_CYICON or SM_CYCURSOR system metric value to set the height. If this parameter is zero and LR_DEFAULTSIZE is not used, the function uses the actual resource height.
     *  @param - fuLoad [in]
     *  UINT
     *  This parameter can be one or more of the following values.
     *  <table>
     *  <tr><th>Value</th><th>Meaning</th></tr>
     *  <tr><td><a id="LR_CREATEDIBSECTION"></a><dl>
     *  <dt>LR_CREATEDIBSECTION</dt>
     *  <dt>0x00002000</dt>
     *  </dl>
     *  </td><td>
     *  <p>When the <em>uType</em> parameter specifies IMAGE_BITMAP, causes the function to return a DIB section bitmap rather than a compatible bitmap. This flag is useful for loading a bitmap without mapping it to the colors of the display device.</p>
     *  </td></tr>
     *  <tr><td><a id="LR_DEFAULTCOLOR"></a><dl>
     *  <dt>LR_DEFAULTCOLOR</dt>
     *  <dt>0x00000000</dt>
     *  </dl>
     *  </td><td>
     *  <p>The default flag; it does nothing. All it means is "not LR_MONOCHROME".</p>
     *  </td></tr>
     *  <tr><td><a id="LR_DEFAULTSIZE"></a><dl>
     *  <dt>LR_DEFAULTSIZE</dt>
     *  <dt>0x00000040</dt>
     *  </dl>
     *  </td><td>
     *  <p>Uses the width or height specified by the system metric values for cursors or icons, if the <em>cxDesired</em> or <em>cyDesired</em> values are set to zero. If this flag is not specified and <em>cxDesired</em> and <em>cyDesired</em> are set to zero, the function uses the actual resource size. If the resource contains multiple images, the function uses the size of the first image.</p>
     *  </td></tr>
     *  <tr><td><a id="LR_LOADFROMFILE"></a><dl>
     *  <dt>LR_LOADFROMFILE</dt>
     *  <dt>0x00000010</dt>
     *  </dl>
     *  </td><td>
     *  <p>Loads the stand-alone image from the file specified by  <em>lpszName</em> (icon, cursor, or bitmap file).</p>
     *  </td></tr>
     *  <tr><td><a id="LR_LOADMAP3DCOLORS"></a><dl>
     *  <dt>LR_LOADMAP3DCOLORS</dt>
     *  <dt>0x00001000</dt>
     *  </dl>
     *  </td><td>
     *  <p>Searches the color table for the image and replaces the following shades of gray with the corresponding 3-D color.
     *
     *                          </p>
     *  <ul>
     *  <li>Dk Gray, RGB(128,128,128) with COLOR_3DSHADOW</li>
     *  <li>Gray, RGB(192,192,192) with COLOR_3DFACE</li>
     *  <li>Lt Gray, RGB(223,223,223) with COLOR_3DLIGHT</li>
     *  </ul>
     *  <p>Do not use this option if you are loading a bitmap with a color depth greater than 8bpp.</p>
     *  </td></tr>
     *  <tr><td><a id="LR_LOADTRANSPARENT"></a><dl>
     *  <dt>LR_LOADTRANSPARENT</dt>
     *  <dt>0x00000020</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves the color value of the first pixel in the image and replaces the corresponding entry in the color table with the default window color (COLOR_WINDOW). All pixels in the image that use that entry become the default window color. This value applies only to images that have corresponding color tables.</p>
     *  <p>Do not use this option if you are loading a bitmap with a color depth greater than 8bpp.</p>
     *  <p>If <em>fuLoad</em> includes both the LR_LOADTRANSPARENT and LR_LOADMAP3DCOLORS values, LR_LOADTRANSPARENT takes precedence. However, the color table entry is replaced with COLOR_3DFACE rather than COLOR_WINDOW.</p>
     *  </td></tr>
     *  <tr><td><a id="LR_MONOCHROME"></a><dl>
     *  <dt>LR_MONOCHROME</dt>
     *  <dt>0x00000001</dt>
     *  </dl>
     *  </td><td>
     *  <p>Loads the image in black and white.</p>
     *  </td></tr>
     *  <tr><td><a id="LR_SHARED"></a><dl>
     *  <dt>LR_SHARED</dt>
     *  <dt>0x00008000</dt>
     *  </dl>
     *  </td><td>
     *  <p>Shares the image handle if the image is loaded multiple times. If LR_SHARED is not set, a second call to <strong>LoadImage</strong> for the same resource will load the image again and return a different handle. </p>
     *  <p>When you use this flag, the system will destroy the resource when it is no longer needed.</p>
     *  <p>Do not use LR_SHARED for images that have non-standard sizes, that may change after loading, or that are loaded from a file.</p>
     *  <p>When loading a system icon or cursor, you must use LR_SHARED or the function will fail to load the resource.</p>
     *  <p>This function finds the first image in the cache with the requested resource name, regardless of the size requested.</p>
     *  </td></tr>
     *  <tr><td><a id="LR_VGACOLOR"></a><dl>
     *  <dt>LR_VGACOLOR</dt>
     *  <dt>0x00000080</dt>
     *  </dl>
     *  </td><td>
     *  <p>Uses true VGA colors.</p>
     *  </td></tr>
     *  </table>
     *
     *  @return
     *  HANDLE
     *  If the function succeeds, the return value is the handle of the newly loaded image.
     *  If the function fails, the return value is NULL. To get extended error information, call GetLastError.
     *  Remarks
     *  If IS_INTRESOURCE(lpszName) is TRUE, then lpszName specifies the integer identifier of the given resource. Otherwise, it is a pointer to a null- terminated string. If the first character of the string is a pound sign (#), then the remaining characters represent a decimal number that specifies the integer identifier of the resource. For example, the string "#258" represents the identifier 258.
     *  When you are finished using a bitmap, cursor, or icon you loaded without specifying the LR_SHARED flag, you can release its associated memory by calling one of the functions in the following table.
     */
    HANDLE LoadImage(HINSTANCE hinst, Pointer name, int type, int xDesired,
            int yDesired, int load);

    /**
     *  Parameters
     *  @param - hWnd [in]
     *  Type: HWND
     *  A handle to the window and, indirectly, the class to which the window belongs.
     *  @param - nIndex [in]
     *  Type: int
     *  The value to be retrieved. To retrieve a value from the extra class memory, specify the positive, zero-based byte offset of the value to be retrieved. Valid values are in the range zero through the number of bytes of extra class memory, minus four; for example, if you specified 12 or more bytes of extra class memory, a value of 8 would be an index to the third integer. To retrieve any other value from the WNDCLASSEX structure, specify one of the following values.
     *  <table>
     *  <tr><th>Value</th><th>Meaning</th></tr>
     *  <tr><td><a id="GCW_ATOM"></a><dl>
     *  <dt>GCW_ATOM</dt>
     *  <dt>-32</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves an
     *  						<strong>ATOM</strong> value that uniquely identifies the window class. This is the same atom that the <a href="http://msdn.microsoft.com/en-us/library/ms633587(v=VS.85).aspx"><strong xmlns="http://www.w3.org/1999/xhtml">RegisterClassEx</strong></a> function returns.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_CBCLSEXTRA"></a><dl>
     *  <dt>GCL_CBCLSEXTRA</dt>
     *  <dt>-20</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves the size, in bytes, of the extra memory associated with the class.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_CBWNDEXTRA"></a><dl>
     *  <dt>GCL_CBWNDEXTRA</dt>
     *  <dt>-18</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves the size, in bytes, of the extra window memory associated with each window in the class. For information on how to access this memory, see <a href="http://msdn.microsoft.com/en-us/library/ms633584(v=VS.85).aspx"><strong xmlns="http://www.w3.org/1999/xhtml">GetWindowLong</strong></a>.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_HBRBACKGROUND"></a><dl>
     *  <dt>GCL_HBRBACKGROUND</dt>
     *  <dt>-10</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves a handle to the background brush associated with the class.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_HCURSOR"></a><dl>
     *  <dt>GCL_HCURSOR</dt>
     *  <dt>-12</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves a handle to the cursor associated with the class.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_HICON"></a><dl>
     *  <dt>GCL_HICON</dt>
     *  <dt>-14</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves a handle to the icon associated with the class.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_HICONSM"></a><dl>
     *  <dt>GCL_HICONSM</dt>
     *  <dt>-34</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves a handle to the small icon associated with the class.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_HMODULE"></a><dl>
     *  <dt>GCL_HMODULE</dt>
     *  <dt>-16</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves a handle to the module that registered the class.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_MENUNAME"></a><dl>
     *  <dt>GCL_MENUNAME</dt>
     *  <dt>-8</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves the address of the menu name string. The string identifies the menu resource associated with the class.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_STYLE"></a><dl>
     *  <dt>GCL_STYLE</dt>
     *  <dt>-26</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves the window-class style bits.</p>
     *  </td></tr>
     *  <tr><td><a id="GCL_WNDPROC"></a><dl>
     *  <dt>GCL_WNDPROC</dt>
     *  <dt>-24</dt>
     *  </dl>
     *  </td><td>
     *  <p>Retrieves the address of the window procedure, or a handle representing the address of the window procedure. You must use the <a href="http://msdn.microsoft.com/en-us/library/ms633571(v=VS.85).aspx"><strong xmlns="http://www.w3.org/1999/xhtml">CallWindowProc</strong></a> function to call the window procedure. </p>
     *  </td></tr>
     *  </table> 
     *  @return
     *  Type: DWORD
     *  If the function succeeds, the return value is the requested value.
     *  If the function fails, the return value is zero. To get extended error information, call GetLastError.
     *  Remarks
     *  Reserve extra class memory by specifying a nonzero value in the cbClsExtra member of the WNDCLASSEX structure used with the RegisterClassEx function.
     */
    DWORD GetClassLong(HWND window, int nIndex);
}

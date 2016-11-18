/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.user.platform.win32;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.win32.StdCallLibrary;

/**
 *
 * @author Administrator
 */
public interface WinUserExt extends StdCallLibrary {

    //Structures
    /**
     *  Members
     *  fIcon
     *  BOOL
     *  Specifies whether this structure defines an icon or a cursor. A value of TRUE specifies an icon; FALSE specifies a cursor.
     *  xHotspot
     *  DWORD
     *  The x-coordinate of a cursor's hot spot. If this structure defines an icon, the hot spot is always in the center of the icon, and this member is ignored.
     *  yHotspot
     *  DWORD
     *  The y-coordinate of the cursor's hot spot. If this structure defines an icon, the hot spot is always in the center of the icon, and this member is ignored.
     *  hbmMask
     *  HBITMAP
     *  The icon bitmask bitmap. If this structure defines a black and white icon, this bitmask is formatted so that the upper half is the icon AND bitmask and the lower half is the icon XOR bitmask. Under this condition, the height should be an even multiple of two. If this structure defines a color icon, this mask only defines the AND bitmask of the icon.
     *  hbmColor
     *  HBITMAP
     *  A handle to the icon color bitmap. This member can be optional if this structure defines a black and white icon. The AND bitmask of hbmMask is applied with the SRCAND flag to the destination; subsequently, the color bitmap is applied (using XOR) to the destination by using the SRCINVERT flag.
     */
    public class ICONINFO extends Structure {

        public boolean fIcon;
        public int xHotspot;
        public int yHotspot;
        public HBITMAP hbmMask;
        public HBITMAP hbmColor;
    }
    public static final int WS_ACTIVECAPTION = 0x0001;
    //static variables for nCmdShow
    public static final int SW_FORCEMINIMIZE = 11;
    public static final int SW_HIDE = 0;
    public static final int SW_MAXIMIZE = 3;
    public static final int SW_RESTORE = 9;
    public static final int SW_SHOW = 5;
    public static final int SW_SHOWDEFUALT = 10;
    public static final int SW_SHOWMAXIMIZED = 3;
    public static final int SW_SHOWMINIMIZED = 2;
    public static final int SW_SHOWMINNOACTIVE = 7;
    public static final int SW_SHOWNA = 8;
    public static final int SW_SHOWNOACTIVATE = 4;
    public static final int SW_SHOWNORMAL = 1;
    //static variables for WINDOWINFO.Style
    public static final int WS_BORDER = 0x00800000;
    public static final int WS_CAPTION = 0x00C00000;
    public static final int WS_CHILD = 0x40000000;
    public static final int WS_CHILDWINDOW = 0x40000000;
    public static final int WS_CLIPCHILDREN = 0x02000000;
    public static final int WS_CLIPSIBLINGS = 0x04000000;
    public static final int WS_DISABLED = 0x08000000;
    public static final int WS_DLGFRAME = 0x00400000;
    public static final int WS_GROUP = 0x00020000;
    public static final int WS_HSCROLL = 0x00100000;
    public static final int WS_ICONIC = 0x20000000;
    public static final int WS_MAXIMIZE = 0x01000000;
    public static final int WS_MAXIMIZEBOX = 0x00010000;
    public static final int WS_MINIMIZE = 0x20000000;
    public static final int WS_MINIMIZEBOX = 0x00020000;
    public static final int WS_OVERLAPPED = 0x00000000;
    public static final int WS_POPUP = 0x80000000;
    public static final int WS_SIZEBOX = 0x00040000;
    public static final int WS_SYSMENU = 0x00080000;
    public static final int WS_TABSTOP = 0x00010000;
    public static final int WS_THICKFRAME = 0x00040000;
    public static final int WS_TILED = 0x00000000;
    public static final int WS_VISIBLE = 0x10000000;
    public static final int WS_VSCROLL = 0x00200000;
    public static final int WS_OVERLAPPEDWINDOW = (WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX);
    public static final int WS_POPUPWINDOW = (WS_POPUP | WS_BORDER | WS_SYSMENU);
    public static final int WS_TILEDWINDOW = (WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX);
    public static final int RT_ACCELERATOR = 9; //Accelerator table.
    public static final int RT_ANICURSOR = 21;//Animated cursor.
    public static final int RT_ANIICON = 22;//Animated icon.
    public static final int RT_BITMAP = 2;//Bitmap resource.
    public static final int RT_CURSOR = 1; //Hardware-dependent cursor resource.
    public static final int RT_DIALOG = 5;//Dialog box.
    public static final int RT_DLGINCLUDE = 17; //Allows a resource editing tool to associate a string with an .rc file. Typically, the string is the name of the header file that provides symbolic names. The resource compiler parses the string but otherwise ignores the value. For example,
    //1 DLGINCLUDE "MyFile.h"
    public static final int RT_FONT = 8; //Font resource.
    public static final int RT_FONTDIR = 7; //Font directory resource.
    public static final int DIFFERENCE = 11;
    public static final int RT_GROUP_CURSOR = RT_CURSOR + DIFFERENCE;  //Hardware-independent cursor resource.
    public static final int RT_ICON = 3;  //Hardware-dependent icon resource.
    public static final int RT_GROUP_ICON = RT_ICON + DIFFERENCE;  //Hardware-independent icon resource.
    public static final int RT_HTML = 23; //HTML resource.
    public static final int RT_MANIFEST = 24; //Side-by-Side Assembly Manifest.
    public static final int RT_MENU = 4;  //Menu resource.
    public static final int RT_MESSAGETABLE = 11;  //Message-table entry.
    public static final int RT_PLUGPLAY = 19;  //Plug and Play resource.
    public static final int RT_RCDATA = 10;  //Application-defined resource (raw data).
    public static final int RT_STRING = 6; //String-table entry.
    public static final int RT_VERSION = 16;  //Version resource.
    public static final int RT_VXD = 20;  //VXD.
    //LoadImage Types
    public static final int IMAGE_BITMAP = 0;
    public static final int IMAGE_ICON = 1;
    public static final int IMAGE_CURSOR = 2;
    //GetClassLong index types
    public static final int GCW_ATOM = -32;
    //Retrieves an ATOM value that uniquely identifies the window class. This is the same atom that the RegisterClassEx function returns.
    public static final int GCL_CBCLSEXTRA = -20;
    //Retrieves the size, in bytes, of the extra memory associated with the class.
    public static final int GCL_CBWNDEXTRA = -18;
    //Retrieves the size, in bytes, of the extra window memory associated with each window in the class. For information on how to access this memory, see GetWindowLong.
    public static final int GCL_HBRBACKGROUND = -10;
    //Retrieves a handle to the background brush associated with the class.
    public static final int GCL_HCURSOR = -12;
    //Retrieves a handle to the cursor associated with the class.
    public static final int GCL_HICON = -14;
    //Retrieves a handle to the icon associated with the class.
    public static final int GCL_HICONSM = -34;
    //Retrieves a handle to the small icon associated with the class.
    public static final int GCL_HMODULE = -16;
    //Retrieves a handle to the module that registered the class.
    public static final int GCL_MENUNAME = -8;
    //Retrieves the address of the menu name string. The string identifies the menu resource associated with the class.
    public static final int GCL_STYLE = -26;
    //Retrieves the window-class style bits.
    public static final int GCL_WNDPROC = -24;
    //Retrieves the address of the window procedure, or a handle representing the address of the window procedure. You must use the CallWindowProc function to call the window procedure.
}

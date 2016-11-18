

package gov.nist.csd.pm.user.platform.win32;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;

/**
 * Utilities for working with User32 libraries
 * @author Administrator
 */
public class User32ExtUtil {
    /**
     * Gets the title of a window in a String
     * @param window - a HWND reference of a window
     * @return - the title of the window as a String
     */
    public static String getWindowText(HWND window) {
        User32 user32 = User32.INSTANCE;
        char[] chars = new char[user32.GetWindowTextLength(window) + 1];
        user32.GetWindowText(window, chars, chars.length);
        String windowName = new String(chars, 0, chars.length - 1);
        return windowName;
    }
}

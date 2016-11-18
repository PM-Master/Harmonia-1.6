package gov.nist.csd.pm.user.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinUser.SIZE;
import com.sun.jna.win32.W32APIOptions;

/**
 * Extensions for the JNA GDI32 library interface. The current JNA GDI interface was missing several functions needed to provide policy machine's functionality. Ideally, these functions would be added into the GDI32 JNA interface.
 * @author  Administrator
 */
public interface GDI32Ext extends GDI32 {

    /**
	 * Returns an instance of the GDI32Ext interface.  This is your entry point into the Windows API.
	 * @uml.property  name="iNSTANCE"
	 * @uml.associationEnd  
	 */
    public static GDI32Ext INSTANCE = (GDI32Ext) Native.loadLibrary("GDI32", GDI32Ext.class, W32APIOptions.DEFAULT_OPTIONS);


    /*
     * Created a new BITMAPINFOHEADER structure to allow
     * manual memory setting.  There was an issue with the auto-allocated
     * BITMAPINFOHEADER in the JNA libraries that I was not able to resolve
     * without this workaround.  I currently pass in a Memory with 100 bytes
     * allocated, which seems to work.  Ideally we should only allocate the
     * exact amount of memory needed.
     *
     */
    public class BITMAPINFOHEADER_MANUAL extends Structure {

        public int biSize = size();
        public int biWidth;
        public int biHeight;
        public short biPlanes;
        public short biBitCount;
        public int biCompression;
        public int biSizeImage;
        public int biXPelsPerMeter;
        public int biYPelsPerMeter;
        public int biClrUsed;
        public int biClrImportant;
        /**
         * Default constructor for this structure
         * Requires a com.sun.jna.Pointer to com.sun.jna.Memory.
         * This structure has only been tested with a Memory of at least 100 bytes.
         * Using less memory than that may cause an invalid memory access.  Caviat Emptor
         * @param mem
         */
        public BITMAPINFOHEADER_MANUAL(Pointer mem) {
            super(mem);
        }
    }

    /**
     * Retrieves the dimension of the bitmap passed into it.
     *
     * @param hBitmap [in] A handle to a compatible bitmap (DDB).
     * @param lpDimension [out]* A pointer to a SIZE structure to receive the bitmap dimensions. For more information, see Remarks.
     * @return <b>true</b> if the function succeeds, <b>false</b> if the function fails.
     * Remarks
     * The function returns a data structure that contains fields for the height and width of the bitmap, in .01-mm units. If those dimensions have not yet been set, the structure that is returned will have zeroes in those fields
     */
    public boolean GetBitmapDimensionEx(HBITMAP hBitmap, SIZE size);

    /** The GetDIBits function retrieves the bits fo the specified compatible
     * bitmap and copies them into a buffer as a DIB using the specified
     * format.
     * @param hdc A handle to the device context.
     * @param hbmp A handle to the bitmap.  This must be a compatible bitmap
     * (DDB).
     * @param uStartScan The first scan line to retrieve
     * @param cScanLines The number of scan lines to retrieve.
     * @param lpvBits A pointer to a buffer to receive the bitmap data.  If
     * this parameter is <code>null</code>, the function passes the dimensions
     * and format of the bitmap to the {@link BITMAPINFOHEADER_MANUAL} structure pointed to
     * by the <i>lpbi</i> parameter.
     * @param lpbi A pointer to a {@link BITMAPINFOHEADER_MANUAL} structure that specifies
     * the desired format for the DIB data.
     * @param uUsage The format of the bmiColors member of the {@link
     * BITMAPINFO} structure. This method will not return those as they don't exist in the BITMAPINFOHEADER
     * structure.
     */
    int GetDIBits(HDC hdc, HBITMAP hbmp, int uStartScan, int cScanLines, Pointer lpvBits, BITMAPINFOHEADER_MANUAL lpbi, int uUsage);
}

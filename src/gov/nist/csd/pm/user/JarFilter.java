package gov.nist.csd.pm.user;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:03:00 $
 * @since 1.5
 */
public class JarFilter extends FileFilter {

  //accept all directories and the jar files
  public boolean accept(File f) {
    if (f.isDirectory()) {
      return true;
    }

    String ext = getExtension(f);
    if (ext != null) {
      if (ext.equals("jar")) {
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  public String getDescription() {
    return "Jar Files";
  }
  
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf(".");

    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i+1).toLowerCase();
    }
    return ext;
  }
}

package gov.nist.csd.pm.admin;


import java.io.File;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class SimpleFileFilter extends javax.swing.filechooser.FileFilter {

    /**
	 * @uml.property  name="extensions" multiplicity="(0 -1)" dimension="1"
	 */
    String[] extensions;
    /**
	 * @uml.property  name="description"
	 */
    String description;

    public SimpleFileFilter(String ext) {
      this (new String[] {ext}, null);
    }

    public SimpleFileFilter(String[] exts, String descr) {
      // clone and lowercase the extensions
      extensions = new String[exts.length];
      for (int i = exts.length - 1; i >= 0; i--) {
        extensions[i] = exts[i].toLowerCase();
      }
      // make sure we have a valid (if simplistic) description
      description = (descr == null ? exts[0] + " files" : descr);
    }

    public boolean accept(File f) {
      // we always allow directories, regardless of their extension
      if (f.isDirectory()) { return true; }

      // ok, it's a regular file so check the extension
      String name = f.getName().toLowerCase();
      for (int i = extensions.length - 1; i >= 0; i--) {
        if (name.endsWith(extensions[i])) {
          return true;
        }
      }
      return false;
    }

    /**
	 * @return
	 * @uml.property  name="description"
	 */
    public String getDescription() { return description; }
  }

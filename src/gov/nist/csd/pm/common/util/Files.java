package gov.nist.csd.pm.common.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Robert McHugh
 * Date: 2/9/11
 * Time: 2:16 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Files {
    private Files(){}
    /*
     *    File Extension Constants
     */
    public static final String PDF_EXT = "pdf";

    /*
     *    Pre-defined file filters.
     */
    public static final FileFilter PDF_FILTER = new FileFilter(){

        @Override
        public boolean accept(File file) {

            return file.isDirectory() || isPDF(file);
        }

        @Override
        public String getDescription() {
            return "PDF Files";
        }
    };

    /**
     * Convenience method for turning any of the above file javax.swing.filechooser.FileFilter into java.io.FileFilter
     * @param fileFilter
     * @return
     */
    public static java.io.FileFilter asIOFilter(FileFilter fileFilter){
        return new IOFileFilterWrappingFileFilter(fileFilter);
    }


    /**
     * Internal implementation of a java.io.FileFilter wrapping a javax.swing.filechooser.FileFilter
     */
    private static class IOFileFilterWrappingFileFilter implements java.io.FileFilter{

        private final FileFilter _fileFilter;

        public IOFileFilterWrappingFileFilter(FileFilter fileFilter){
            _fileFilter = checkNotNull(fileFilter);
        }

        @Override
        public boolean accept(File file) {
            return _fileFilter.accept(file);
        }
    }

    public static boolean isPDF(File file){
        return file.exists() && file.getName().toLowerCase().endsWith(PDF_EXT);
    }

}

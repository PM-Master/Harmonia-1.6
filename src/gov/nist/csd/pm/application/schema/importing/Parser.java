package gov.nist.csd.pm.application.schema.importing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface Parser {
	
	/**
	 * Keywords to look for in the file
	 */
    String CREATE_KEYWORD = "CREATE";
	String TABLE_KEYWORD = "TABLE";
	
	/**
	 * Reads data from a File object
	 * @param file The file to read from
	 */
	String read(File file);
	
	/**
	 * Reads data from the file specified in the path
	 * @param path The path of the file to read from
	 */
	String read(String path);

	ArrayList<String> readLines(String path);

    List<Table> parse(ArrayList<String> contents);
}

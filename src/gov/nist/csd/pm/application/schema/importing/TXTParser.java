package gov.nist.csd.pm.application.schema.importing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TXTParser implements Parser {

	@Override
	public String read(File file) {
		try{
			Scanner sc = new Scanner(file);
			String contents = "";
			while(sc.hasNext()){
				contents += sc.next() + " ";
			}
			sc.close();
			return contents;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String read(String path) {
		try{
			Scanner sc = new Scanner(new File(path));
			String contents = "";
			while(sc.hasNext()){
				contents += sc.next() + " ";
			}
			sc.close();
			return contents;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ArrayList<String> readLines(String path) {
		return null;
	}

	@Override
	public List<Table> parse(ArrayList<String> contents) {
		return null;
	}

}

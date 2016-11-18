package gov.nist.csd.pm.application.schema.importing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

public class RTFParser implements Parser {

	/**
	 * RTFEditorKit to read the rtf file
	 */
	private RTFEditorKit kit;

	public RTFParser(){
		kit = new RTFEditorKit();
	}

	public String read(String path){
		try {
			FileInputStream inputStream = new FileInputStream(path);
			DefaultStyledDocument styledDoc = new DefaultStyledDocument();
			kit.read(inputStream,styledDoc,0);

			return parseAll(styledDoc.getText(0,styledDoc.getLength()));
		}
		catch (IOException io){
			io.printStackTrace();
		}
		catch (BadLocationException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public ArrayList<String> readLines(String path) {
		return null;
	}

	@Override
	public List<Table> parse(ArrayList<String> contents) {
		return null;
	}

	public String read(File f) {
		try {
			FileInputStream inputStream = new FileInputStream(f);
			DefaultStyledDocument styledDoc = new DefaultStyledDocument();
			kit.read(inputStream,styledDoc,0);

			return parseAll(styledDoc.getText(0,styledDoc.getLength()));
		}
		catch (IOException io){
			io.printStackTrace();
		}
		catch (BadLocationException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	private String parseAll(String input){
		Stack<Integer> parenStack = new Stack<Integer>();
		ArrayList<String> returnList = new ArrayList<String>();
		boolean match = false;
		String[] tokens = input.split("");
		String block = "";
		String all = "";
		for(int i = 0; i < tokens.length; i++){
			if(tokens[i].equals("(")){
				parenStack.push(i);
			}else if(tokens[i].equals(")")){
				parenStack.pop();
				match = true;
			}

			block += tokens[i];
			if(parenStack.empty() && match){
				all += block.trim() + " ";
				match = false;
				block = "";
			}
		}
		return all;
	}
	
}

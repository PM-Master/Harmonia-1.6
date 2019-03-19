package gov.nist.csd.pm.application.schema.importing;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class SQLParser implements Parser {

    public SQLParser(){
    }

    public String read(String path){
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String text = "";
            String line = "";
            while ((line=br.readLine()) != null){
                text += line;
            }
            System.out.println(text);
            return parseAll(text);
        }
        catch (IOException io){
            io.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> readLines(String path){
        ArrayList<String> contents = new ArrayList<String>();
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String text = "";
            String line = "";
            while ((line=br.readLine()) != null){
                if(!line.startsWith("--"))contents.add(line);
            }
            System.out.println(text);
            return contents;
        }
        catch (IOException io){
            io.printStackTrace();
        }
        return null;
    }

    public List<Table> parse(ArrayList<String> contents) {
        List<Table> tables = new ArrayList<Table>();
        for(int i = 0; i < contents.size(); i++){
            String line = contents.get(i);
            if(line.contains("CREATE TABLE") || line.contains("create table")){
                int j = i;
                List<String> tableContents = new ArrayList<String>();
                while(!contents.get(j).contains(";")){
                    tableContents.add(contents.get(j).trim());
                    j++;
                }
                String table = tableContents.get(0).split("`")[1];
                List<String> cols = new ArrayList<String>();
                for(String s : tableContents){
                    if(s.startsWith("`")){
                        cols.add(s.split("`")[1]);
                    }
                }
                Table t = new Table(table, null, cols, null);
                tables.add(t);
            }
        }
        return tables;
    }

    public String read(File f) {
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

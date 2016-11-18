package gov.nist.csd.pm.admin;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import gov.nist.csd.pm.server.audit.Audit;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

/**
 *
 * @author nkeller
 */
public class AuditResultsHTML {
    static Connection connection = null;
    static String dbName = "PM_Audit";
    static String state  = "";
    static String restOfState = "";
    static String sessionId = "";
    static String userId = "";
    static String userName = "";
    static String hostName = "";
    static String timeStamp = null;
    static String action = "";
    static String result = "";
    static String description = "";
    static String objectId = "";
    static String objectName = "";
    static String query = "";

    //HTML Additional Variables
    static StringBuffer table = new StringBuffer();
    static String beginning;
    static String end;
    static String queryHTML;
    static StringBuffer body  = new StringBuffer();
    static File htmlFile;
    static boolean flag;
    static String url;

    //The result set is received here
    public static StringBuffer results() throws Exception{
        state = AuditGUI.stmt.toString();
        state = state.replace("[", "");
        state = state.replace("]", "");
        state = state.toLowerCase();
        restOfState = AuditGUI.restOfStmt.toString();
        restOfState = restOfState.replace("[", "");
        restOfState = restOfState.replace("]", "");
        restOfState = restOfState.replace(",", " ");

        if((restOfState.isEmpty() == true)||(restOfState.equals(""))){
            query = "select "+state+ " from "+dbName+".Audit_Information";
        }else{
            query = "select "+state+ " from "+dbName+".Audit_Information where "+restOfState;
        }
        System.out.println("The query is: "+query);
        ArrayList<ArrayList<Object>> auditRecords = Audit.getAuditInfo(query);
        int i=0;
        for(ArrayList<Object> auditRecord : auditRecords){
        	i = 0;
                if(AuditGUI.stmt.contains("SESS_ID")){
                    sessionId = (String)auditRecord.get(i++);
                }else{
                    sessionId = "";
                }
                if(AuditGUI.stmt.contains("USER_ID")){
                        userId = (String)auditRecord.get(i++);
                }

                if(AuditGUI.stmt.contains("USER_NAME")){
                        userName = (String)auditRecord.get(i++);
                }

                if(AuditGUI.stmt.contains("HOST_NAME")){
                        hostName = (String)auditRecord.get(i++);
                }
                if(AuditGUI.stmt.contains("TS")){
                        timeStamp = (String)auditRecord.get(i++);
                }else{
                	timeStamp = "";
                }
                

                if(AuditGUI.stmt.contains("ACTION")){
                        action = (String)auditRecord.get(i++);
                }
                if(AuditGUI.stmt.contains("RESULT_SUCCESS")){
                        result = (String)auditRecord.get(i++);
                        flag = true;
                }else{
                	result = "";
                	flag = false;
                }

                if(AuditGUI.stmt.contains("DESCRIPTION")){
                        description = (String)auditRecord.get(i++);
                }

                if(AuditGUI.stmt.contains("OBJ_ID")){
                        objectId = (String)auditRecord.get(i++);
                }

                if(AuditGUI.stmt.contains("OBJ_NAME")){
                        objectName = (String)auditRecord.get(i++);
                }
                //This is where the table tags and the information will go into. After this the String will be concatnated and 
                //added to the beggining of the html code which is held in another string in another method. 
                String bPart = "<tr><td>"+sessionId+"</td><td>"+userId+"</td><td>"+userName+"</td><td>"+hostName+"</td>";
                String bPart2 = "";
                if(timeStamp == null){
                	bPart2 = "<td></td>";
                	//return bPart2;
                }else{
                	bPart2 = "<td>"+timeStamp+"</td>";
                	//return bPart2;
                }
                
                String bPart3 = "<td>"+action+"</td>";
                String bPart4 = "";
                if(flag == false){
                	bPart4 = "<td></td>";
                	//return bPart4;
                }else{
                	bPart4 = "<td>"+result+"</td>";
                	//return bPart4;
                }
                String bPart5 = "<td>"+description+"</td><td>"+objectId+"</td><td>"+objectName+"</td></tr>";
                body.append(bPart +bPart2 + bPart3 + bPart4 + bPart5);        
            }
        table.append("<table border=\"2\" style=\"width:100%\"><tr><th>Session ID</th><th>User ID</th><th>User Name</th><th>Host Name</th>"
                        + "<th>Timestamp</th><th>Action</th><th>Result Success</th><th>Description</th><th>Object ID</th><th>Object Name</th></tr>"+body+"</table>");
        return table;
        
    }
    
    public static String beginning(String query, StringBuffer table){
        String ending = "</body></html>";
        beginning = "<!doctype html>"+
                    "<html lang=\"en\">" +
                    "<head>" +
                    "  <meta charset=\"utf-8\">"
                + "<title>Audit Results</title>" 
                + "</head>"
                + "<body>"
                + "<h1>Audit Results</h1>"
                + "<p>"+query+"</p>"
                + table.toString() 
                + ending;
        System.out.println("this is the beginning statement: "+beginning);
        
        return beginning;
    }
    
    public static void createFile() throws IOException{
    	beginning(query, table);
        // TODO code application logic here
        if(Desktop.isDesktopSupported()){
            String origName = "results";
            String ext = ".html";
            int num = 1;
            htmlFile = new File(origName + ext);
            while(htmlFile.exists()){
            	num++;
            	htmlFile = new File(origName + "(" + num + ")" + ext);
            }
            
            FileWriter fw = new FileWriter(htmlFile, false);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(beginning);
            bw.close();
            fw.close();
            Desktop.getDesktop().browse(htmlFile.toURI());
            htmlFile.deleteOnExit();
        }
    }
    /**
     * @param args the command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
			results();
	        createFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        //What if I created a function that created different file names 
        reset();
        state = "";
		restOfState = "";
		AuditGUI.restOfStmt.clear();
		AuditGUI.unf = false;
		AuditGUI.stmt.clear();
		AuditGUI.reset();
		htmlFile.deleteOnExit();
    }
    
    public static String fileNames(){
    	//What if every file was named by what the query was?
    	//Replace the spaces with "" and limit it to be only 7 characters long and delete the rest
    	//Then add the .html to the end and return it to the results() function.
    	
    	String fileName = query.replace(" ", "");
    	fileName = fileName.replace("select", "");
    	if(fileName.length() >= 10){
    		url = fileName.substring(0, 10);
    		return url;
    	}
    	url = fileName;
    	return url;
    }
    
    public static void reset(){
        sessionId = "";
        userId = "";
        userName = "";
        hostName = "";
        timeStamp = null;
        action = "";
        result = "";
        description = "";
        objectName = "";
        objectId = "";
        query = "";
        body = new StringBuffer();
        System.out.println("This is the table before reset: "+table);
        System.out.println("This is the beginning before reset:"+beginning);
        table = new StringBuffer();
        beginning = "";        
        System.out.println("This is the value of table and beginning: "+table+" "+beginning);
    }
}


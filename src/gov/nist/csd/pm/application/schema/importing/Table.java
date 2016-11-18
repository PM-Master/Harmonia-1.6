package gov.nist.csd.pm.application.schema.importing;

import java.util.ArrayList;
import java.util.List;

public class Table{
    private String table;
    private String[] columns;
    private List<List<Object>> data;

    public Table(String table, String[] columns, List<List<Object>> d) {
        this.table = table;
        this.columns = columns;
        this.data = d;
    }
    public String getTable() {
        return table;
    }
    public void setTable(String table) {
        this.table = table;
    }
    public String[] getColumns() {
        return columns;
    }
    public void setColumns(String[] columns) {
        this.columns = columns;
    }
    public List<List<Object>> getData(){
        return data;
    }
    public List<Object> getRow(int i){
        return data.get(i);
    }
    public List<Object> getColumn(int i){
        List<Object> col = new ArrayList<Object>();
        for(int j = 0; j < data.size(); j++){
            col.add(data.get(j).get(i));
        }
        return col;
    }
    public String toString(){
        String sCols = "";
        for(String s : columns){
            sCols += s + " ";
        }
        return table + ": " + sCols;
    }
}

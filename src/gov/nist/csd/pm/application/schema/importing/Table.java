package gov.nist.csd.pm.application.schema.importing;

import java.util.ArrayList;
import java.util.List;

public class Table{
    private String table;
    private List<String> keys;
    private List<String> columns;
    private List<Row> data;

    public Table(String table, List<String> key, List<String> columns, List<Row> d) {
        this.table = table;
        this.keys = key;
        this.columns = columns;
        this.data = d;
    }

    public Table() {
        table = null;
        keys = null;
        columns = null;
        data = new ArrayList<>();
    }

    public String getTable() {
        return table;
    }
    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public List<String> getColumns() {
        return columns;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    public List<Row> getData(){
        return data;
    }
    public void addRow(Row r){
        data.add(r);
    }
    public Row getRow(String rowId){
        for(Row r : data){
            if(r.getRowId().equals(rowId)){
                return r;
            }
        }
        return null;
    }
    public void setData(List<Row> d){
        data = d;
    }
    public Row getRow(int i){
        return data.get(i);
    }
    public List<Object> getColumn(int i){
        List<Object> col = new ArrayList<Object>();
        for(int j = 0; j < data.size(); j++){
            col.add(data.get(j).getCell(i));
        }
        return col;
    }
    public List<String> getColumnObjects(int i){
        List<String> col = new ArrayList<String>();
        for(int j = 0; j < data.size(); j++){
            col.add(data.get(j).getObjects().get(i));
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

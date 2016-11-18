package gov.nist.csd.pm.admin;


import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:57 $
 * @since 1.5
 */
public class ObjTableModel extends AbstractTableModel {
    protected static int NUM_COLUMNS = 5;
    protected static int START_NUM_ROWS = 8;
    /**
	 * @uml.property  name="nextEmptyRow"
	 */
    protected int nextEmptyRow = 0;
    /**
	 * @uml.property  name="numRows"
	 */
    protected int numRows = 0;

    static final public String PM_ALT_DELIM_PATTERN = "\\|";

    static final public String objName = "Object name";
    static final public String objClass = "Class";
    static final public String actHostOrName = "Host/orig. name";
    static final public String actPathOrId = "Path/orig. id";
    static final public String subgraph = "Subgraph?";

    /**
	 * @uml.property  name="data"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
	 */
    protected Vector data = null;

    public ObjTableModel() {
        data = new Vector();
    }

    public String getColumnName(int column) {
	switch (column) {
	  case 0:
	    return objName;
	  case 1:
	    return objClass;
	  case 2:
	    return actHostOrName;
	  case 3:
	    return actPathOrId;
	  case 4:
	    return subgraph;
	}
	return "";
    }

    // Should this really be synchronized?
    public synchronized int getColumnCount() {
        return NUM_COLUMNS;
    }

    public synchronized int getRowCount() {
        if (numRows < START_NUM_ROWS) {
            return START_NUM_ROWS;
        } else {
            return numRows;
        }
    }

    public synchronized Object getValueAt(int row, int column) {
      String sName, sId, sClass, sInh, sNameOrHost, sIdOrPath;
      System.out.println("Calling getValueAt(" + row + "," + column + ")");
      
      // GetValueAt() could be called for all table rows (the table starts
      // with 8 rows) in order to be painted. The vector data could have
      // fewer than 8 items.
      if (row >= data.size()) return "";
      try {
        String sObj = (String)data.elementAt(row);
        String[] pieces = sObj.split(PM_ALT_DELIM_PATTERN);
        int n = pieces.length;
        System.out.println("getValueAt(" + row + "," + column + ") has " + n + " pieces.");
        sName = pieces[0];
        sId = pieces[1];
        sClass = pieces[2];
        sInh = pieces[3];
        if (n > 4) sNameOrHost = pieces[4];
        else sNameOrHost = "";
        if (n > 5) sIdOrPath = pieces[5];
        else sIdOrPath = "";
        switch (column) {
          case 0:
            return sName;
          case 1:
            return sClass;
          case 2:
            return sNameOrHost;
          case 3:
            return sIdOrPath;
          case 4:
            return sInh;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return "";
    }

  public synchronized void addObject(String sObj) {
    if (numRows <= nextEmptyRow) {
      numRows++;
    }
    int index = nextEmptyRow;
    data.addElement(sObj);
    nextEmptyRow++;

    fireTableRowsInserted(index, index);
  }

  public synchronized void delObject(int delIndex) {
    if (delIndex < 0) return;
    if (delIndex >= nextEmptyRow) return;
    numRows--;
    if (numRows < START_NUM_ROWS) numRows = START_NUM_ROWS;
    nextEmptyRow--;
    data.removeElementAt(delIndex);
    fireTableRowsDeleted(delIndex, delIndex);
  }
  
  public synchronized void clear() {
    int oldNumRows = numRows;

    numRows = START_NUM_ROWS;
    data.removeAllElements();
    nextEmptyRow = 0;
    if (oldNumRows > START_NUM_ROWS) {
      fireTableRowsDeleted(START_NUM_ROWS, oldNumRows - 1);
    }
    fireTableRowsUpdated(0, START_NUM_ROWS - 1);
  }
  
  public synchronized Object get(int index) {
    if (index < 0 || index > data.size()) return null;
    return data.get(index);
  }
}
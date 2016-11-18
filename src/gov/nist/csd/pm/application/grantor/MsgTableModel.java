package gov.nist.csd.pm.application.grantor;


import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class MsgTableModel extends AbstractTableModel {
  protected static int NUM_COLUMNS = 6;
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

  static final public String msgLabel = "Label";
  static final public String msgAttach = "Attachment";
  static final public String msgFrom = "From";
  static final public String msgTo = "To";
  static final public String msgDate = "Date";
  static final public String msgSubject = "Subject";

  /**
 * @uml.property  name="msgData"
 * @uml.associationEnd  multiplicity="(0 -1)" elementType="java.lang.String"
 */
protected Vector msgData = null;

  public MsgTableModel() {
      msgData = new Vector();
  }

  public String getColumnName(int column) {
      switch (column) {
        case 0:
          return msgLabel;
        case 1:
          return msgAttach;
        case 2:
          return msgFrom;
        case 3:
          return msgTo;
        case 4:
          return msgDate;
        case 5:
          return msgSubject;
      }
      return "";
  }

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
    System.out.println("Calling getValueAt(" + row + "," + column + ")");

    // GetValueAt() could be called for all table rows (the table starts
    // with 8 rows) in order to be painted. The vector data could have
    // fewer than 8 items.
    if (row >= msgData.size()) return "";
    String[] sMsgFields = (String[])msgData.elementAt(row);
    return sMsgFields[column];
  }

  public synchronized void addObject(String[] sMsg) {
    if (numRows <= nextEmptyRow) {
      numRows++;
    }
    int index = nextEmptyRow;
    msgData.addElement(sMsg);
    nextEmptyRow++;

    fireTableRowsInserted(index, index);
  }

  public synchronized void delObject(int delIndex) {
    if (delIndex < 0) return;
    if (delIndex >= nextEmptyRow) return;
    numRows--;
    if (numRows < START_NUM_ROWS) numRows = START_NUM_ROWS;
    nextEmptyRow--;
    msgData.removeElementAt(delIndex);
    fireTableRowsDeleted(delIndex, delIndex);
  }
  
  public synchronized void clear() {
    int oldNumRows = numRows;

    numRows = START_NUM_ROWS;
    msgData.removeAllElements();
    nextEmptyRow = 0;
    if (oldNumRows > START_NUM_ROWS) {
      fireTableRowsDeleted(START_NUM_ROWS, oldNumRows - 1);
    }
    fireTableRowsUpdated(0, START_NUM_ROWS - 1);
  }
  
  public synchronized Object get(int index) {
    if (index < 0 || index >= msgData.size()) return null;
    return msgData.get(index);
  }
  
  public synchronized int nextEmptyRow() {
    return nextEmptyRow;
  }
}
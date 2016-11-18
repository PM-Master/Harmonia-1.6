package gov.nist.csd.pm.application.rtf;

import gov.nist.csd.pm.common.application.SysCallerImpl;
import gov.nist.csd.pm.common.browser.ObjectBrowser;
import gov.nist.csd.pm.common.net.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;
import javax.swing.text.html.*;
import javax.swing.filechooser.*;
import javax.swing.undo.*;
import java.util.*;

/**
 * @author gavrila@nist.gov
 * @version $Revision: 1.1 $, $Date: 2008/07/16 17:02:58 $
 * @since 1.5
 */
public class RTFEditor extends JFrame {

  protected JEditorPane myEditorPane;
  HashMap actions;
  
  // Name and handle for the virtual object currently open.
  String sCrtObjName = null;
  String sCrtObjHandle = null;

  // The SysCaller instance.
  SysCallerImpl sysCaller = null;

  private String sProcessId;
  
  // The object browser used to select the container(s) and the name of the
  // object to save.
  private ObjectBrowser objectBrowser;

  // Undo helpers
  protected UndoAction undoAction;
  protected RedoAction redoAction;
  protected UndoManager undo = new UndoManager();

  public RTFEditor(int nSimPort, String sSessId, String sProcId, boolean bDebug) {
    super("RTF Editor v1.0");
    
    this.sProcessId = sProcId;

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    setIconImage(new ImageIcon("images/nist.gif").getImage());
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        terminate(0);
      }
    });

    sysCaller = new SysCallerImpl(nSimPort, sSessId, sProcId, bDebug, "RTF");
    createUI();
    
    System.out.println("Content type is " + myEditorPane.getContentType());
    printEditorKit();
  }

  private void terminate(int exitCode) {
    
    if (sCrtObjHandle != null) {
      sysCaller.closeObject(sCrtObjHandle);
    }
    sysCaller.exitProcess(sProcessId);
    System.exit(exitCode);
  }
  
  protected void createUI() {
    setSize(500, 600);
    center();
    Container content = getContentPane();
    content.setLayout(new BorderLayout());
    
    // Add the editor pane
    myEditorPane = new JEditorPane();
    myEditorPane.setContentType("text/rtf");
    myEditorPane.setCaretPosition(0);

    JScrollPane scrollPane = new JScrollPane(myEditorPane);
    scrollPane.setPreferredSize(new Dimension(500, 600));
    content.add(scrollPane, BorderLayout.CENTER);

    // Build the action HashMap.
    createActionTable(myEditorPane);
    
    // Add key mappings.
    updateKeymap();
    
    // Build the menu.
    JMenu fileMenu = createFileMenu();
    JMenu editMenu = createEditMenu();
    JMenu fontMenu = createFontMenu();
    JMenuBar mb = new JMenuBar();
    mb.add(fileMenu);
    mb.add(editMenu);
    mb.add(fontMenu);
    setJMenuBar(mb);
  }

  protected void center() {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension us = getSize();
    int x = (screen.width - us.width) / 2;
    int y = (screen.height - us.height) / 2;
    setLocation(x, y);
  }
  
  /*
  protected void openInitialDoc(String sObjId) {
    System.out.println("Content type is now " + myEditorPane.getContentType());
    printEditorKit();
    
    File f = new File(sObjId);
    if (!f.exists() || !f.isFile() || !f.canRead()) {
      JOptionPane.showMessageDialog(RTFEditor.this, "Something is wrong with the input file!"); 
      return;
    }
    try {
      FileInputStream fis = new FileInputStream(f);
      EditorKit myEdKit = myEditorPane.getEditorKit();
      Document doc = myEditorPane.getDocument();
      myEdKit.read(fis, doc, 0);
      fis.close();
      sCrtDocPath = sObjId;
      RTFEditor.this.setTitle("RTFEditor v1.0 - " + sCrtDocPath);
    } catch (Exception e) {
      e.printStackTrace();
      sCrtDocPath = null;
      JOptionPane.showMessageDialog(RTFEditor.this, "Couldn't read the input file!"); 
    }
    System.out.println("Content type is now " + myEditorPane.getContentType());
    printEditorKit();
    myEditorPane.setCaretPosition(0);
  }
  */

  // Open the object whose name was passed on the command line.
  protected void openInitialObj(String sObjName) {
    // Open the object.
    sCrtObjHandle = sysCaller.openObject3(sObjName, "File read,File write");
    if (sCrtObjHandle == null) {
      JOptionPane.showMessageDialog(RTFEditor.this, sysCaller.getLastError());
      return;
    }
    
    // Insert the object content into the document.
    final RTFEditorKit myEdKit = (RTFEditorKit)myEditorPane.getEditorKit();
    final Document doc = myEditorPane.getDocument();
    System.out.println("Document class is " + doc.getClass().getName());

    try {
        final PipedOutputStream pos = new PipedOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(pos);
        final PipedInputStream pis = new PipedInputStream(pos);
        final BufferedInputStream bis = new BufferedInputStream(pis);


        new Thread(new Runnable() {
            public void run() {
              try {
                doc.remove(0, doc.getLength());
                myEdKit.read(bis, doc, 0);
              } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(RTFEditor.this,
                  "Exception while the editor kit reads object content!");
              }
            }
          }).start();
   
      

      Packet res = sysCaller.readObject3(sCrtObjHandle, bos);
      if (res == null) {
        JOptionPane.showMessageDialog(RTFEditor.this,
          "Null packet received from readObject()!");
        return;
      } else if (res.isEmpty()) {
        JOptionPane.showMessageDialog(RTFEditor.this,
          "Empty packet received from readObject()!");
        return;
      } else if (res.hasError()) {
        JOptionPane.showMessageDialog(RTFEditor.this,
          res.getErrorMessage());
        return;
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      JOptionPane.showMessageDialog(RTFEditor.this,
        "Exception in readObject: " + exc.getMessage());
      return;
    }

    myEditorPane.setCaretPosition(doc.getLength());
    RTFEditor.this.setTitle("RTFEditor v1.0 - " + sObjName);
    sCrtObjName = sObjName;
    myEditorPane.setCaretPosition(0);
  }
  
  private void printEditorKit() {
    EditorKit ek = myEditorPane.getEditorKit();
    if (ek instanceof RTFEditorKit) System.out.println("RTFEditorKit");
    else if (ek instanceof HTMLEditorKit) System.out.println("HTMLEditorKit");
    else if (ek instanceof StyledEditorKit) System.out.println("StyledEditorKit");
    else if (ek instanceof DefaultEditorKit) System.out.println("DefaultEditorKit");
    else System.out.println("None");
  }

  // The following two methods allow us to find an
  // action provided by the editor kit by its name.
  private void createActionTable(JTextComponent textComponent) {
    actions = new HashMap();
    Action[] actionsArray = textComponent.getActions();
    for (int i = 0; i < actionsArray.length; i++) {
      Action a = actionsArray[i];
      actions.put(a.getValue(Action.NAME), a);
//      System.out.println(a.getValue(Action.NAME));
    }
    
    Action a = (Action)actions.get(DefaultEditorKit.cutAction);
    a.putValue(Action.NAME, "Cut");
    a = (Action)actions.get(DefaultEditorKit.copyAction);
    a.putValue(Action.NAME, "Copy");
    a = (Action)actions.get(DefaultEditorKit.pasteAction);
    a.putValue(Action.NAME, "Paste");
    a = (Action)actions.get(DefaultEditorKit.selectAllAction);
    a.putValue(Action.NAME, "Select All");
    
    a = (Action)actions.get("font-bold");
    a.putValue(Action.NAME, "Bold");
    a = (Action)actions.get("font-italic");
    a.putValue(Action.NAME, "Italic");
    a = (Action)actions.get("font-underline");
    a.putValue(Action.NAME, "Underline");
    a = (Action)actions.get("font-family-SansSerif");
    a.putValue(Action.NAME, "SansSerif");
    a = (Action)actions.get("font-family-Monospaced");
    a.putValue(Action.NAME, "Monospaced");
    a = (Action)actions.get("font-family-Serif");
    a.putValue(Action.NAME, "Serif");
    a = (Action)actions.get("font-size-10");
    a.putValue(Action.NAME, "10");
    a = (Action)actions.get("font-size-12");
    a.putValue(Action.NAME, "12");
    a = (Action)actions.get("font-size-16");
    a.putValue(Action.NAME, "16");
    a = (Action)actions.get("font-size-24");
    a.putValue(Action.NAME, "24");
  }

  private Action getActionByName(String name) {
      return (Action)(actions.get(name));
  }

  private void updateKeymap() {
    FocusManager.setCurrentManager(new CtrlFocusManager());
    
    Keymap map = JTextComponent.addKeymap("StyleMap", myEditorPane.getKeymap());
    
    KeyStroke bold = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK, false);
    KeyStroke italic = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK, false);
    KeyStroke underline = KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK, false);
    
    map.addActionForKeyStroke(bold, getActionByName("font-bold"));
    map.addActionForKeyStroke(italic, getActionByName("font-italic"));
    map.addActionForKeyStroke(underline, getActionByName("font-underline"));
    
    myEditorPane.setKeymap(map);
  }
  
  class UndoAction extends AbstractAction {
    public UndoAction() {
      super("Undo");
      setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      try {
        undo.undo();
      } catch (CannotUndoException ex) {
        System.out.println("Unable to undo: " + ex);
        ex.printStackTrace();
      }
      updateUndoState();
      redoAction.updateRedoState();
    }

    protected void updateUndoState() {
      if (undo.canUndo()) {
        setEnabled(true);
        putValue(Action.NAME, undo.getUndoPresentationName());
      } else {
         setEnabled(false);
         putValue(Action.NAME, "Undo");
      }
    }
  }

  //////////////////////////Class RedoAction///////////////////////////////
  class RedoAction extends AbstractAction {
    public RedoAction() {
      super("Redo");
      setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      try {
        undo.redo();
      } catch (CannotRedoException ex) {
        System.out.println("Unable to redo: " + ex);
        ex.printStackTrace();
      }
      updateRedoState();
      undoAction.updateUndoState();
    }

    protected void updateRedoState() {
      if (undo.canRedo()) {
        setEnabled(true);
        putValue(Action.NAME, undo.getRedoPresentationName());
      } else {
        setEnabled(false);
        putValue(Action.NAME, "Redo");
      }
    }
  }
  //////////////////////////End RedoAction///////////////////////////////

  // Create the File menu.
  protected JMenu createFileMenu() {
    JMenu menu = new JMenu("File");
    menu.setMnemonic('F');

    JMenuItem item = new JMenuItem(getOpenAction());
    item.setMnemonic('O');
    item.setAccelerator(KeyStroke.getKeyStroke('O', Event.CTRL_MASK, false));
    menu.add(item);

    menu.addSeparator();
    
    item = new JMenuItem(getSaveAction());
    item.setMnemonic('S');
    item.setAccelerator(KeyStroke.getKeyStroke('S', Event.CTRL_MASK, false));
    menu.add(item);

    item = new JMenuItem(getSaveAsAction());
    item.setMnemonic('A');
    menu.add(item);
    
    menu.addSeparator();

    item = new JMenuItem(getPrintAction());
    item.setMnemonic('P');
    menu.add(item);
    
    menu.addSeparator();

    item = new JMenuItem(new ExitAction());
    item.setMnemonic('x');
    menu.add(item);

    return menu;
  }

  // Open Action
  protected Action getOpenAction() {
    return openAction;
  }
  
  private Action openAction = new OpenAction();
  
  //////////////////////////Class OpenAction///////////////////////////////
  class OpenAction extends AbstractAction {

    public OpenAction() {
      super("Open");
    }
    
    public void actionPerformed(ActionEvent event) {
      if (objectBrowser == null) {
        objectBrowser = new ObjectBrowser(RTFEditor.this, sysCaller, "RTF Editor");
        objectBrowser.pack();
      }
      int ret = objectBrowser.showOpenDialog();
      if (ret != ObjectBrowser.PM_OK) return;
      
      String sObjName = objectBrowser.getObjName();
      System.out.println("Object nam: " + sObjName);      
      openInitialObj(sObjName);
    }
  }
  //////////////////////////End OpenAction///////////////////////////////
  
  // Save Action
  protected Action getSaveAction() {
    return saveAction;
  }
  
  private Action saveAction = new SaveAction();
  
  //////////////////////////Class SaveAction///////////////////////////////
  class SaveAction extends AbstractAction {
    public SaveAction() {
      super("Save");
    }
    
    public void actionPerformed(ActionEvent event) {
      System.out.println("Trying to save the object with handle " + sCrtObjHandle);
      
      if (sCrtObjHandle == null) {
        // To do: ???Ask for a new object name and location, like in Save As case.
        return;
      }

      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EditorKit myEdKit = myEditorPane.getEditorKit();
        Document doc = myEditorPane.getDocument(); 
        myEdKit.write(baos, doc, 0, doc.getLength());
        byte[] buf = baos.toByteArray();
        int res = sysCaller.writeObject3(sCrtObjHandle, buf);
        if (res < 0) {
          JOptionPane.showMessageDialog(RTFEditor.this, sysCaller.getLastError());
          return;
        }
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(RTFEditor.this, "Exception while saving object: " + e.getMessage());
      }
    }
  }
  //////////////////////////End SaveAction///////////////////////////////
  
  // SaveAs Action
  protected Action getSaveAsAction() {
    return saveAsAction;
  }
  
  private Action saveAsAction = new SaveAsAction();
  
  //////////////////////////Class SaveAsAction///////////////////////////////
  class SaveAsAction extends AbstractAction {
    public SaveAsAction() {
      super("Save As");
    }
    
    public void actionPerformed(ActionEvent event) {
      if (objectBrowser == null) {
        objectBrowser = new ObjectBrowser(RTFEditor.this, sysCaller, "RTF Editor");;
        objectBrowser.pack();
      }
      int ret = objectBrowser.showSaveAsDialog();
      if (ret != ObjectBrowser.PM_OK) return;
      
      // Prepare for object creation.
      String sObjName = objectBrowser.getObjName();
      String sContainers = objectBrowser.getContainers();
      
      System.out.println("Containers: " + sContainers);
      System.out.println("Object nam: " + sObjName);
      
      String sObjClass = "File";
      String sObjType = "rtf";
      String sPerms = "File write";
      
      // Create the object.
      String sHandle = sysCaller.createObject3(sObjName, sObjClass, sObjType,
        sContainers, sPerms, null, null, null, null);
      if (sHandle == null) {
        JOptionPane.showMessageDialog(RTFEditor.this, sysCaller.getLastError());
        return;
      }

      // Write object contents.
      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EditorKit myEdKit = myEditorPane.getEditorKit();
        Document doc = myEditorPane.getDocument(); 
        myEdKit.write(baos, doc, 0, doc.getLength());
        byte[] buf = baos.toByteArray();
        int len = sysCaller.writeObject3(sHandle, buf);
        if (len < 0) {
          JOptionPane.showMessageDialog(RTFEditor.this, sysCaller.getLastError());
          return;
        }
        sCrtObjHandle = sHandle;
        RTFEditor.this.setTitle("RTFEditor v1.0 - " + sObjName);
        sCrtObjName = sObjName;
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(RTFEditor.this, "Exception while saving object: " + e.getMessage());
      }
    }
  }
  //////////////////////////End SaveAsAction///////////////////////////////

  //////////////////////////End SaveAction///////////////////////////////
  
  // Print Action
  protected Action getPrintAction() {
    return printAction;
  }
  
  private Action printAction = new PrintAction();

  //////////////////////////Class PrintAction///////////////////////////////
  class PrintAction extends AbstractAction {
    public PrintAction() {
      super("Print");
    }
    
    public void actionPerformed(ActionEvent event) {
      
      try {
        boolean printComplete = myEditorPane.print();
        if (printComplete) {
        } else {
        }
      } catch (PrinterException pe) {
      }
      
    }
  }
  //////////////////////////End PrintAction///////////////////////////////

  // Exit Action
  public class ExitAction extends AbstractAction {
    public ExitAction() {
      super("Exit");
    }
    
    public void actionPerformed(ActionEvent event) {
      terminate(0);
    }
  }

  // Copy Action
  protected Action getCopyAction() {
    return copyAction;
  }
  
  private Action copyAction = new CopyAction();

  //////////////////////////Class CopyAction///////////////////////////////

/*
  class CopyAction extends AbstractAction {
    public CopyAction() {
      super("Copy");
    }
    
    public void actionPerformed(ActionEvent event) {
      try {
        //Document doc = myEditorPane.getDocument();
        String sSelText = myEditorPane.getSelectedText();
        if (!sysCaller.copyToClipboard(sCrtObjHandle, sSelText)) {
          JOptionPane.showMessageDialog(RTFEditor.this, sysCaller.getLastError());
          return;
        }
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(RTFEditor.this, "Exception while copying selected text: " + e.getMessage());
      }
    }
  }
 */

  class CopyAction extends AbstractAction {
    public CopyAction() {
      super("Copy");
    }
    
    public void actionPerformed(ActionEvent event) {
      try {
        if (!sysCaller.copyToClipboard(sCrtObjHandle, null)) {
          JOptionPane.showMessageDialog(RTFEditor.this, sysCaller.getLastError());
          return;
        }
        
        RTFEditorKit myEditorKit = (RTFEditorKit)myEditorPane.getEditorKit();
        Action edKitCopyAct = getActionByName(myEditorKit.copyAction);
        edKitCopyAct.actionPerformed(event);

      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(RTFEditor.this, "Exception while copying selected text: " + e.getMessage());
      }
    }
  }
  
  //////////////////////////End CopyAction///////////////////////////////
  
  // Paste Action
  protected Action getPasteAction() {
    return pasteAction;
  }
  
  private Action pasteAction = new PasteAction();

  //////////////////////////Class PasteAction///////////////////////////////
  class PasteAction extends AbstractAction {
    public PasteAction() {
      super("Paste");
    }
    
    public void actionPerformed(ActionEvent event) {
      try {
        boolean b = sysCaller.isPastingAllowed();
        System.out.println("Pasting allowed: " + b);
        if (!b) {
          JOptionPane.showMessageDialog(null, sysCaller.getLastError());
          return;
        }

        RTFEditorKit myEditorKit = (RTFEditorKit)myEditorPane.getEditorKit();
        Action edKitPasteAct = getActionByName(myEditorKit.pasteAction);
        edKitPasteAct.actionPerformed(event);
      } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(RTFEditor.this, "Exception while copying selected text: " + e.getMessage());
      }
    }
  }

  //////////////////////////End PasteAction///////////////////////////////

  //Create the edit menu.
  protected JMenu createEditMenu() {
    JMenu menu = new JMenu("Edit");
    menu.setMnemonic('E');

    //Undo and redo are actions of our own creation.
    undoAction = new UndoAction();
    menu.add(undoAction);

    redoAction = new RedoAction();
    menu.add(redoAction);

    menu.addSeparator();

    // These actions come from the default editor kit.
    // Get the ones we want and stick them in the menu.
    JMenuItem item = null;
    /*
    item = new JMenuItem(getActionByName(DefaultEditorKit.cutAction));
    item.setMnemonic('t');
    item.setAccelerator(KeyStroke.getKeyStroke('X', Event.CTRL_MASK, false));
    menu.add(item);

    item = new JMenuItem(getActionByName(DefaultEditorKit.copyAction));
    item.setMnemonic('C');
    item.setAccelerator(KeyStroke.getKeyStroke('C', Event.CTRL_MASK, false));
    menu.add(item);
    
    item = new JMenuItem(getActionByName(DefaultEditorKit.pasteAction));
    item.setMnemonic('P');
    item.setAccelerator(KeyStroke.getKeyStroke('V', Event.CTRL_MASK, false));
    menu.add(item);

    menu.addSeparator();
     */

    item = new JMenuItem(getCopyAction());
    item.setMnemonic('D');
    item.setAccelerator(KeyStroke.getKeyStroke('D', Event.CTRL_MASK, false));
    menu.add(item);

    item = new JMenuItem(getPasteAction());
    item.setMnemonic('E');
    item.setAccelerator(KeyStroke.getKeyStroke('E', Event.CTRL_MASK, false));
    menu.add(item);
    
    menu.addSeparator();

    item = new JMenuItem(getActionByName(DefaultEditorKit.selectAllAction));
    item.setMnemonic('l');
    item.setAccelerator(KeyStroke.getKeyStroke('A', Event.CTRL_MASK, false));
    menu.add(item);

    return menu;
  }

  // Create the style menu.
  protected JMenu createFontMenu() {
    JMenu fontMenu = new JMenu("Font");
    fontMenu.setMnemonic('o');
    
    JMenu styleMenu = new JMenu("Style");
    styleMenu.setMnemonic('y');
    
    JMenu familyMenu = new JMenu("Family");
    familyMenu.setMnemonic('F');
    
    JMenu sizeMenu = new JMenu("Size");
    sizeMenu.setMnemonic('S');
    
    JMenu colorMenu = new JMenu("Color");
    colorMenu.setMnemonic('C');

    fontMenu.add(styleMenu);
    fontMenu.add(familyMenu);
    fontMenu.add(sizeMenu);
    fontMenu.add(colorMenu);
    
    styleMenu.add(getActionByName("font-bold"));
    styleMenu.add(getActionByName("font-italic"));
    styleMenu.add(getActionByName("font-underline"));

    familyMenu.add(getActionByName("font-family-Monospaced"));
    familyMenu.add(getActionByName("font-family-SansSerif"));
    familyMenu.add(getActionByName("font-family-Serif"));

    sizeMenu.add(getActionByName("font-size-10"));
    sizeMenu.add(getActionByName("font-size-12"));
    sizeMenu.add(getActionByName("font-size-16"));
    sizeMenu.add(getActionByName("font-size-24"));

    colorMenu.add(new StyledEditorKit.ForegroundAction("Red", Color.red));
    colorMenu.add(new StyledEditorKit.ForegroundAction("Green", Color.green));
    colorMenu.add(new StyledEditorKit.ForegroundAction("Blue", Color.blue));
    colorMenu.add(new StyledEditorKit.ForegroundAction("Black", Color.black));

    return fontMenu;
  }

  public class CtrlFocusManager extends DefaultFocusManager {
    public void processKeyEvent(Component focusedComp, KeyEvent evt) {
      if (evt.getKeyCode() == KeyEvent.VK_C) {
        if ((evt.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
          return;
        }
      }
      super.processKeyEvent(focusedComp, evt);
    }
  }
  
  public class SimpleFileFilter extends javax.swing.filechooser.FileFilter {

    String[] extensions;
    String description;

    public SimpleFileFilter(String ext) {
      this (new String[] {ext}, null);
    }

    public SimpleFileFilter(String[] exts, String descr) {
      // clone and lowercase the extensions
      extensions = new String[exts.length];
      for (int i = exts.length - 1; i >= 0; i--) {
        extensions[i] = exts[i].toLowerCase();
      }
      // make sure we have a valid (if simplistic) description
      description = (descr == null ? exts[0] + " files" : descr);
    }

    public boolean accept(File f) {
      // we always allow directories, regardless of their extension
      if (f.isDirectory()) { return true; }

      // ok, it's a regular file so check the extension
      String name = f.getName().toLowerCase();
      for (int i = extensions.length - 1; i >= 0; i--) {
        if (name.endsWith(extensions[i])) {
          return true;
        }
      }
      return false;
    }

    public String getDescription() { return description; }
  }
  
  // sObjName is the label of the OATTR associated with the virtual object.
  private static void createAndShowGUI(int nSimPort, String sSessId,
      String sProcId, String sObjName, boolean bDebug) {
    //Make sure we have nice window decorations.
    //JFrame.setDefaultLookAndFeelDecorated(true);

    //Create and set up the window.
    RTFEditor myEditor = new RTFEditor(nSimPort, sSessId, sProcId, bDebug);
    //myEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Display the window.
    myEditor.pack();
    myEditor.setVisible(true);
    
    if (sObjName == null) return;
    myEditor.openInitialObj(sObjName);
  }
    
  private static String sesid;
  private static String pid;
  private static String objname;
  private static int simport;
  private static boolean debug;
  
  public static void main(String[] args) {
    // Process command line arguments.
    // -session <sessionId> -simport <simulator port> -debug <virtual object name>
    // The session id is mandatory.
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-session")) {
        sesid = args[++i];
      } else if (args[i].equals("-process")) {
          pid = args[++i];
      } else if (args[i].equals("-simport")) {
        simport = Integer.valueOf(args[++i]).intValue();
      } else if (args[i].equals("-debug")) {
        debug = true;
      } else {
        objname = args[i];
      }
    }
    
    if (sesid == null) {
      System.out.println("RTFEditor must run in a Policy Machine session!");
      System.exit(-1);
    }
    if (pid == null) {
      System.out.println("RTFEditor must run in a Policy Machine process!");
      System.exit(-1);
    }
    
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI(simport, sesid, pid, objname, debug);
      }
    });
  }
}

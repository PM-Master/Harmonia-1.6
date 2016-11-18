package gov.nist.csd.pm.user;


import com.google.common.base.Predicate;
import gov.nist.csd.pm.common.graphics.GraphicsUtil;
import gov.nist.csd.pm.common.graphics.MenuToggleButton;
import gov.nist.csd.pm.common.util.swing.SwingShortcuts;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.find;
import static java.util.Collections.synchronizedList;

/**
 * Implementation of the Session Manager's task bar.  the bar that currently runs along the bottom of the
 * Session Manager window.  Its function is to allow quick access to the Session Manager's installed applications.
 */
public class OsTaskBar extends JPanel implements ActionListener, ApplicationManagerListener {

    /**
	 * @uml.property  name="applicationManager"
	 * @uml.associationEnd  
	 */
    private ApplicationManager applicationManager;
    /**
	 * @uml.property  name="sessionid"
	 */
    private String sessionid;
    /**
	 * @uml.property  name="clock"
	 * @uml.associationEnd  
	 */
    private JTextField clock;
    /**
	 * @uml.property  name="clockTimer"
	 * @uml.associationEnd  
	 */
    private Timer clockTimer;
    /**
	 * @uml.property  name="tasks"
	 * @uml.associationEnd  multiplicity="(0 -1)" inverse="this$0:gov.nist.csd.pm.user.OsTaskBar$TaskItem"
	 */
    private Collection<TaskItem> tasks = synchronizedList(new ArrayList<TaskItem>());
    /**
	 * @uml.property  name="goButton"
	 * @uml.associationEnd  
	 */
    private MenuToggleButton goButton;
    /**
	 * @uml.property  name="taskPanel"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
    private JPanel taskPanel = new JPanel();

    /**
     * @param sessionid  The id of the session for the user
     * @param appManager The manager for invoking applications
     * @param debug      if the application should print debug statements
     */
    public OsTaskBar(String sessionid, ApplicationManager appManager, boolean debug) {
        super();
        this.sessionid = sessionid;
        applicationManager = appManager;
        applicationManager.addApplicationManagerListener(this);
        setupTaskbar();
    }

    /**
     * Sets up the taskbar, including "Go" button, clock and layout manager.
     */
    private final void setupTaskbar() {
        setLayout(new BorderLayout());
        setupClock();
        List<String> apps = applicationManager.getInstalledApplications();
        JPopupMenu goButtonPopupMenu = new JPopupMenu("Go");
        JMenu applicationsMenu = new JMenu("Applications");
        JMenu configuration = new JMenu("Configuration");
        
        JMenu openOffice = new JMenu("OpenOffice");
        String word  ="OpenOffice-Word";
        String power ="OpenOffice-PowerPoint";
        String excel ="OpenOffice-ExcelSpreadSheet";
        JMenuItem itemo = new JMenuItem(word);
        openOffice.add(power);
        openOffice.add(excel);
        openOffice.add(itemo);
        for (String appName : apps) {
            JMenuItem item = new JMenuItem(appName);
            item.addActionListener(this);
            applicationsMenu.add(item);
        }
        applicationsMenu.add(openOffice);
        setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createBevelBorder(BevelBorder.RAISED),
                        BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        Icon icon = GraphicsUtil.getImageIcon("/images/ApplicationIcon16x16.png", this.getClass());
        goButton = new MenuToggleButton("Go", icon);
        goButton.setFont(goButton.getFont().deriveFont(Font.BOLD));
        goButton.setBorder(new BevelBorder(BevelBorder.RAISED));
        Dimension actualSize = goButton.getSize();
        goButton.setComponentPopupMenu(goButtonPopupMenu);
        goButton.setPopupMenu(goButtonPopupMenu);
        goButton.setMinimumSize(new Dimension(70, 22));
        goButton.setMaximumSize(new Dimension(70, 22));
        taskPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.add(taskPanel, BorderLayout.CENTER);
        goButtonPopupMenu.add(applicationsMenu);
        goButtonPopupMenu.add(configuration);
        this.add(goButton, BorderLayout.WEST);
        this.setMinimumSize(new Dimension(0, 26));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));


    }

    /**
     * Sets up the task bar's clock.
     */
    private final void setupClock() {
        Calendar now = Calendar.getInstance();
        String time = String.format("%1$tH:%1$tM hours", now);
        clock = new JTextField(time);
        clock.setBorder(
                new CompoundBorder(
                        new BevelBorder(BevelBorder.LOWERED),
                        new EmptyBorder(1, 4, 1, 4)));
        clock.setHorizontalAlignment(JTextField.RIGHT);
        clock.setEditable(false);
        clock.setFont(new Font("sansserif", Font.PLAIN, 12));
        if (clockTimer == null) {
            clockTimer = new Timer(1000, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    Calendar now = Calendar.getInstance();
                    String time = String.format("%1$tH:%1$tM hours", now);
                    clock.setText(time);
                }
            });
            clockTimer.start();
        }
        this.add(clock, BorderLayout.EAST);

    }


    /**
     * Adds a task item to the task bar, used internally to simplify the implementation of adding a task bar item.
     *
     * @param task
     */
    private void addTaskBarComponent(final TaskItem task) {
        //this.add(Box.createHorizontalStrut(2));
        JButton taskButton = new JButton(task.getText());
        taskButton.setIcon(task.getNativeProcessWrapper().getApplicationIcon());
        taskButton.setBackground(goButton.getBackground());
        taskButton.setHorizontalAlignment(SwingConstants.LEFT);
        taskButton.setName(task.getId());
        //taskButton.setEnabled(false);

        //taskButton.setEditable(false);
        //taskButton.setBorder(new CompoundBorder(new BevelBorder(BevelBorder.RAISED), new EmptyBorder(0, 5, 0, 5)));
        taskButton.setBorder(new BevelBorder(BevelBorder.RAISED));
        taskButton.setPreferredSize(new Dimension(160, 22));
        taskButton.setMaximumSize(taskButton.getPreferredSize());
        taskPanel.add(taskButton);

        taskButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                task.getNativeProcessWrapper().bringApplicationToFront();
            }
        });

    }


    /**
     * Performs the application launch function.
     * The actionCommand property of the incoming ActionEvent is assumed to be
     * set to the name of the application we are trying to invoke.
     *
     * @param arg0 ActionEvent with actionCommand set to the application we want to invoke
     */
    @Override
    public void actionPerformed(ActionEvent arg0) {
        String applicationName = arg0.getActionCommand();
        System.out.println("application is launching - " + applicationName ); // Added by Gopi for testing
        applicationManager.launchClientApp(applicationName, sessionid);
    }

    /**
     * Adds a task to the taskbar
     *
     * @param taskName the name of the task as it appears on the task bar.
     * @param id       a unique identifier by which to refer this task.
     * @param wrapper  a wrapper for the process. Permits access to native process resources.
     */
    public void addTask(String taskName, String id, NativeProcessWrapper wrapper) {
        TaskItem ti = new TaskItem(taskName, id, wrapper);
        tasks.add(ti);
        addTaskBarComponent(ti);
    }

    private void removeTaskBarComponentWithName(String name) {
        Component found = SwingShortcuts.getComponentNamed(taskPanel, name, Component.class);
        if (found != null) {
            taskPanel.remove(found);
        }
    }

    private static final Predicate<TaskItem> withMatchingId(final String id) {

        return new Predicate<TaskItem>() {

            @Override
            public boolean apply(@Nullable TaskItem input) {
                checkNotNull(input);
                return  (id == null && id == input.getId())||
                        (id != null && id.equals(input.getId()));
            }
        };
    }

    /**
     * Removes a task from the taskbar
     *
     * @param id the unique identifier used when this task was added to the taskbar.
     */
    public void removeTask(String id) {
        try{
            TaskItem found = find(tasks, withMatchingId(id));
            if(found != null){
                removeTaskBarComponentWithName(id);
                tasks.remove(found);
            }
        }catch(NoSuchElementException nsee){
            //Dont care
        }

    }

    //ApplicationManagerDelegate methods
    @Override
    public void applicationStarted(String applicationName, String processId, NativeProcessWrapper procWrapper) {
        addTask(applicationName, processId, procWrapper);
    }

    @Override
    public void applicationTerminated(String processId) {
        removeTask(processId);
    }

    /**
	 * Data object for TaskItem information
	 */
    private class TaskItem {

        private String name;
        /**
		 * @uml.property  name="id"
		 */
        private String id;
        /**
		 * @uml.property  name="procWrapper"
		 * @uml.associationEnd  
		 */
        private NativeProcessWrapper procWrapper;

        public TaskItem(String name, String id, NativeProcessWrapper procWrapper) {
            this.name = name;
            this.id = id;
            this.procWrapper = procWrapper;
        }

        public String getText() {
            return name;
        }

        public void setText(String text) {
            this.name = text;
        }

        /**
		 * @return
		 * @uml.property  name="id"
		 */
        public String getId() {
            return id;
        }

        /**
		 * @param id
		 * @uml.property  name="id"
		 */
        public void setId(String id) {
            this.id = id;
        }

        public void setNativeProcessWrapper(NativeProcessWrapper procWrapper) {
            this.procWrapper = procWrapper;
        }

        public NativeProcessWrapper getNativeProcessWrapper() {
            return procWrapper;
        }
    }
}

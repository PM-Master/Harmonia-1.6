/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nist.csd.pm.common.util.swing;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Note:  This class makes extensive use of the Google Guava library
 *
 * @author Robert McHugh
 */
public class SwingShortcuts {

    //Button Construction helpers

    /**
     * Creates a jbutton using the given action.
     * @param action
     * @return
     */
    public static JButton jButton(Action action){
        return new JButton(action);
    }



    /**
     * Creates a JButton with the given name
     * @param name
     * @return
     */
    public static JButton jButton(String name) {
        return jButtonFunction().apply(name);
    }

    /**
     * returns a Function that takes in a String and returns a JButton using that string as its text.
     * Good for transforming a list of strings, into buttons.
     * @return
     */
    public static Function<String, JButton> jButtonFunction() {
        return new JButtonFunction();
    }

    /**
     * Implementation of the JButton function
     */
    private static class JButtonFunction implements Function<String, JButton> {

        @Override
        public JButton apply(String f) {
            return new JButton(f);
        }
    }

    //Label Construction helpers
    /**
     * Convenience function for creating a jLabel without a constructor
     * @param name - the text you wish the label to show.
     * @return
     */
    public static JLabel jLabel(String name) {
        return jLabelFunction().apply(name);
    }

    /**
     * Returns a function for generating a label from a string.
     * This function returns a JLabel with text set to the provided input string
     * @return
     */
    public static Function<String, JLabel> jLabelFunction() {
        return new JLabelFunction();
    }

    public static Function<JLabel, JLabel> withLabelText(final String text){
        return new Function<JLabel, JLabel>(){

            @Override
            public JLabel apply(@Nullable JLabel jLabel) {
                jLabel.setText(text);
                return jLabel;
            }
        };
    }

    public static Function<JLabel, JLabel> withLabelIcon(final Icon icon){
        return new Function<JLabel, JLabel>(){

            @Override
            public JLabel apply(@Nullable JLabel jLabel) {
                jLabel.setIcon(icon);
                return jLabel;
            }
        };
    }

    /**
     * Internal implementation of the JLabel generation function.
     */
    private static class JLabelFunction implements Function<String, JLabel> {

        @Override
        public JLabel apply(String f) {
            return new JLabel(f);
        }
    }

    //Separator Construction helpers
    /**
     * Convenience creation function for a JSeparator
     * @param orientation
     * @return
     */
    public static JSeparator jSeparator(int orientation) {
        return JSeparatorFunction().apply(orientation);
    }

    /**
     * Returns a Function that takes in an Integer and returns a JSeparator
     * The integer should specify the aligment (horizontal or vertical)
     * of the separator.
     * @return
     */
    public static Function<Integer, JSeparator> JSeparatorFunction() {
        return new JSeparatorFunction();
    }

    /**
     * Internal implementation of the JSeparator generation function.
     */
    private static class JSeparatorFunction implements Function<Integer, JSeparator> {

        @Override
        public JSeparator apply(Integer f) {
            return new JSeparator(f);
        }
    }

    //JPassword Construction helpers
    /**
     * Convenience generator for creating a JPasswordField
     * @param columns - the number of columns in the JPasswordField
     * @return
     */
    public static JPasswordField jPasswordField(int columns) {
        return JPasswordFieldFunction().apply(columns);
    }

    /**
     * method for generating a Function that takes in an Integer and returns a JPasswordField
     * @return
     */
    public static Function<Integer, JPasswordField> JPasswordFieldFunction() {
        return new JPassFieldFunction();
    }

    /**
     * Internal implementation of the JSeparator generation function.
     */
    public static class JPassFieldFunction implements Function<Integer, JPasswordField> {

        @Override
        public JPasswordField apply(Integer f) {
            return new JPasswordField(f);
        }
    }

    //TextField Construction helpers
    /**
     * Convenience method that returns a JTextField with the number of columns specified.
     *
     * @param columns
     * @return
     */
    public static JTextField jTextField(int columns) {
        return jTextFieldFunction().apply(columns);
    }

    /**
     * Method that returns a function that takes and integer and returns a JTextField with a number of columns
     * specified for that Integer.
     * @return
     */
    public static Function<Integer, JTextField> jTextFieldFunction() {
        return new JTextFieldFunction();
    }

    /**
     * Internal implementation of the JTextField generation function.
     */
    private static class JTextFieldFunction implements Function<Integer, JTextField> {

        @Override
        public JTextField apply(Integer f) {
            return new JTextField(f);
        }
    }

    //TextArea construction helpers
    public static JTextArea jTextArea() {
        return new JTextArea();
    }

    public static JTextArea jTextArea(int rows, int columns){
        return new JTextArea(rows, columns);
    }

    /**
     * Convenience method for creating a JTextArea with the number of columns
     * @param columns
     * @return
     */
    public static JTextArea jTextArea(int columns) {
        return JTextAreaFunction().apply(columns);
    }

    /**
     * Method returns a function that takes in an Integer and returns a JTextArea with the
     * number of columns specified by that integer.
     * @return
     */
    public static Function<Integer, JTextArea> JTextAreaFunction() {
        return new JTextAreaFunction();
    }

    /**
     * Internal implementation of the JTextArea generation function.
     */
    private static class JTextAreaFunction implements Function<Integer, JTextArea> {

        @Override
        public JTextArea apply(Integer f) {
            JTextArea jta = new JTextArea();
            jta.setColumns(f);
            return jta;
        }
    }

    /**
     * Returns a function that sets the number of rows of a JTextArea
     * @param rows
     * @return
     */
    public static Function<JTextArea, JTextArea> withRows(Integer rows) {
        return new WithRowCountFunction(rows);
    }

    /**
     * Internal implementation of the withRows function
     */
    private static class WithRowCountFunction implements Function<JTextArea, JTextArea> {

        private final Integer _rows;

        public WithRowCountFunction(Integer rows) {
            _rows = rows;
        }

        @Override
        public JTextArea apply(JTextArea f) {
            f.setRows(_rows);
            return f;
        }
    }

    //Text Component helpers
    /**
     * returns a function that sets the text of a text component to the string specified
     * @param text - a string that will be used to set the value of the text component
     * @param <T> a type that extends JTextComponent
     * @return
     */
    public static <T extends JTextComponent> Function<T, T> withText(String text) {
        return new WithTextFunction<T>(text);
    }

    /**
     * Internal implementation of the withText function
     * @param <T>
     */
    private static class WithTextFunction<T extends JTextComponent> implements Function<T, T> {

        private final String _text;

        public WithTextFunction(String text) {
            _text = text;
        }

        @Override
        public T apply(T f) {
            f.setText(_text);
            return f;
        }
    }

    public static <T extends JTextComponent> T focusOnShown(final T comp){
        comp.addComponentListener(new ComponentAdapter(){

                 @Override
                 public void componentShown(ComponentEvent componentEvent) {
                     super.componentShown(componentEvent);
                     comp.requestFocusInWindow();
                 }
             });
        return comp;
    }

     public static class EnteredPasswordSupplier implements Supplier<char[]>{
            final JPasswordField jpf;
            public EnteredPasswordSupplier(JPasswordField jpf){

                this.jpf = jpf;
            }

            @Override
            public char[] get() {
                return jpf != null ? jpf.getPassword() : null;
            }
        }

    public static class EnteredTextSupplier implements Supplier<String>{
        final JTextComponent jtc;
        public EnteredTextSupplier(JTextComponent jtc){
            this.jtc = jtc;

        }

        @Override
        public String get() {
            return jtc != null ? jtc.getText() : null;
        }
    }

    public static Function<JTextComponent, JTextComponent> withDocument(Document document){
        return new WithDocumentFunction(document);
    }

    private static class WithDocumentFunction implements Function<JTextComponent, JTextComponent>{

        private final Document document;

        public WithDocumentFunction(Document document){
            this.document = document;
        }

        @Override
        public JTextComponent apply(@Nullable JTextComponent jTextComponent) {
            if(jTextComponent != null){
                jTextComponent.setDocument(document);
            }
            return jTextComponent;
        }
    }

    //jcombobox helpers
    /**
     * Convenience method for creating a JComboBox
     * @return
     */
    public static JComboBox jComboBox() {
        return new JComboBox();
    }

    /**
     * Returns a function that updates a JComboBox's options to the
     * objects specified.
     * @param ops
     * @return
     */
    public static <T> Function<JComboBox, JComboBox> withOptions(java.util.List<T> ops) {
        return new WithOptionsFunction(ops.toArray());
    }

    public static <T> Function<JComboBox, JComboBox> withOptions(T first, T... rest) {
        return withOptions(Lists.asList(first, rest));
    }

    /**
     * Internal implementation of the withOptions function
     */
    private static class WithOptionsFunction implements Function<JComboBox, JComboBox> {

        private final Object[] _options;

        public WithOptionsFunction(Object[] options) {
            _options = options;
        }

        @Override
        public JComboBox apply(JComboBox jComboBox) {
            jComboBox.setModel(new DefaultComboBoxModel(_options));
            return jComboBox;
        }
    }

    /**
     * @param defaultOption - the item you would like selected in the combo box.
     * @return Returns a function that sets the selected item of a combo box to the object specified.
     */
    public static Function<JComboBox, JComboBox> withDefaultOption(Object defaultOption) {
        return new WithDefaultOptionFunction(defaultOption);
    }

    /**
     * Internal implementation for the withDefaultOption function.
     */
    private static class WithDefaultOptionFunction implements Function<JComboBox, JComboBox> {

        private final Object _defaultOption;

        public WithDefaultOptionFunction(Object defaultOption) {
            _defaultOption = defaultOption;
        }

        @Override
        public JComboBox apply(JComboBox comboBox) {
            checkNotNull(comboBox);
            comboBox.setSelectedItem(_defaultOption);
            return comboBox;
        }
    }

    /**
     *
     * @param defaultOptionIndex - the index value you would like selected in a combo box
     * @return a Function that will set the selected index of a JComboBox to defaultOptionIndex
     */
    public static Function<JComboBox, JComboBox> withDefaultOptionIndex(int defaultOptionIndex) {
        return new WithDefaultOptionIndexFunction(defaultOptionIndex);
    }

    /**
     * Internal implementation of the withDefaultOptionIndex function.
     */
    private static class WithDefaultOptionIndexFunction implements Function<JComboBox, JComboBox> {

        private final int _defaultOptionIndex;

        public WithDefaultOptionIndexFunction(int defaultOptionIndex) {
            _defaultOptionIndex = defaultOptionIndex;
        }

        @Override
        public JComboBox apply(JComboBox comboBox) {
            checkNotNull(comboBox);
            comboBox.setSelectedIndex(_defaultOptionIndex);
            return comboBox;
        }
    }

    /**
     * Returns a function that adds the provided ItemListener to an ItemSelectable (e.g. a JComboBox)
     * @param listener
     * @return
     */
    public static Function<ItemSelectable, ItemSelectable> withItemListener(ItemListener listener) {
        return new WithItemListenerFunction(listener);
    }

    /**
     * Internal implementation of the withItemListener function
     */
    private static class WithItemListenerFunction implements Function<ItemSelectable, ItemSelectable> {

        private final ItemListener _listener;

        public WithItemListenerFunction(ItemListener listener) {
            _listener = listener;
        }

        @Override
        public ItemSelectable apply(ItemSelectable itemSelectable) {
            itemSelectable.addItemListener(_listener);
            return itemSelectable;
        }
    }

    //JRadioButton
    public static JRadioButton jRadioButton() {
        return jRadioButton("");
    }

    public static JRadioButton jRadioButton(String title) {
        return new JRadioButton(title);
    }

    //JCheckBox helpers
    public static JCheckBox jCheckBox() {
        return jCheckBox("");
    }

    public static JCheckBox jCheckBox(String title) {
        return new JCheckBox(title);
    }

    public static Function<JToggleButton, JToggleButton> withCheckValue(boolean checked) {
        return new WithCheckValueFunction(checked);
    }

    private static class WithCheckValueFunction implements Function<JToggleButton, JToggleButton> {

        private final boolean _checked;

        public WithCheckValueFunction(boolean checked) {
            _checked = checked;
        }

        @Override
        public JToggleButton apply(JToggleButton f) {
            f.setSelected(_checked);
            return f;
        }
    }



    public static Function<AbstractButton, AbstractButton> withButtonText(final String text) {
        return new Function<AbstractButton, AbstractButton>() {

            @Override
            public AbstractButton apply(AbstractButton f) {
                f.setText(text);
                return f;
            }
        };
    }

    //Component helpers
    /**
     * returns a function that sets a component to disabled
     * @param <T>
     * @return
     */
    public static <T extends Component> Function<T, T> makeDisabled() {
        return makeEnabled(Boolean.FALSE);
    }

    /**
     * returns a function that sets a component to enabled.
     * @param <T>
     * @return
     */
    public static <T extends Component> Function<T, T> makeEnabled() {
        return makeEnabled(Boolean.TRUE);
    }

    /**
     * returns a function that sets a component's enabled value to the specified boolean
     * @param enabled
     * @param <T>
     * @return
     */
    public static <T extends Component> Function<T, T> makeEnabled(boolean enabled) {
        return new EnabledFunction<T>(enabled);
    }

    /**
     * Internal implementation of the makeEnabled/makeDisabled functions
     * @param <T>
     */
    private static class EnabledFunction<T extends Component> implements Function<T, T> {

        private final Boolean _enabled;

        public EnabledFunction(Boolean enabled) {
            _enabled = enabled;
        }

        @Override
        public T apply(T f) {
            f.setEnabled(_enabled);
            return f;
        }
    }

    /**
     * returns a function that makes a JTextComponent uneditable
     * @param <T>
     * @return
     */
    public static <T extends JTextComponent> Function<T, T> makeNotEditable() {
        return makeEditable(Boolean.FALSE);
    }

    /**
     * returns a function that makes a JTextComponent editable
     * @param <T>
     * @return
     */
    public static <T extends JTextComponent> Function<T, T> makeEditable() {
        return makeEditable(Boolean.TRUE);
    }

    /**
     * returns a function that sets the editable value of a JTextComponent to the boolean specified.
     * @param editable
     * @param <T>
     * @return
     */
    public static <T extends JTextComponent> Function<T, T> makeEditable(boolean editable) {
        return new EditableFunction(editable);
    }

    /**
     * Internal implementation of the makeEditable/makeNotEditable functions
     * @param <T>
     */
    private static class EditableFunction<T extends JTextComponent> implements Function<T, T> {

        private final Boolean _editable;

        public EditableFunction(Boolean editable) {
            _editable = editable;
        }

        @Override
        public T apply(T f) {
            f.setEditable(_editable);
            return f;
        }
    }

    public static <C extends Component> Function<Container, Container> addBuiltComponent(C initial, final Object constraints, Function<? super C, ? super C>... instructions){
        final Supplier<C> comp = new BuildSupplier(initial, instructions);
        return new Function<Container,Container>(){

            @Override
            public Container apply(@Nullable Container container) {
                if(container == null) return null;
                if(constraints == null){
                    container.add(comp.get());
                }
                else{
                    container.add(comp.get(), constraints);
                }
                return container;
            }
        };
    }

    /**
     * Returns a function that adds a component to a container
     * @param comp
     * @param constraints
     * @return
     */
    public static Function<Container, Container> addComponent(Component comp, Object constraints) {
        return new AddComponentFunction(comp, constraints);

    }

    public static Function<Container, Container> addComponent(Component comp) {
        return new AddComponentFunction(comp, null);

    }

    /**
     * Returns a function that adds an ActionListener to an AbstractButton
     * @param action
     * @return
     */
    public static Function<AbstractButton, AbstractButton> withAction(ActionListener action) {
        return new WithButtonActionFunction(action);
    }

    /**
     * Internal implementation of the withAction function
     */
    private static class WithButtonActionFunction implements Function<AbstractButton, AbstractButton> {

        private final ActionListener _action;

        public WithButtonActionFunction(ActionListener action) {
            _action = action;
        }

        @Override
        public AbstractButton apply(AbstractButton abstractButton) {
            if (abstractButton != null) {
                abstractButton.addActionListener(_action);
            }
            return abstractButton;
        }
    }

    /**
     *
     * @param icon the icon to add to an AbstractButton
     * @return a Function that takes in an AbstractButton, sets the Icon property, and returns it.
     */
    public static Function<AbstractButton, AbstractButton> withIcon(Icon icon) {
        return new WithIconFunction(icon);
    }

    /**
     * Internal implementation of the withIcon function
     */
    private static class WithIconFunction implements Function<AbstractButton, AbstractButton> {

        private final Icon _icon;

        public WithIconFunction(Icon icon) {
            _icon = icon;
        }

        @Override
        public AbstractButton apply(AbstractButton f) {
            f.setIcon(_icon);
            return f;
        }
    }

    /**
     * Internal implementation of the addComponent function
     */
    private static class AddComponentFunction implements Function<Container, Container> {

        private final Component _component;
        private final Object _constraints;

        public AddComponentFunction(Component component, Object cons) {
            _component = component;
            _constraints = cons;
        }

        @Override
        public Container apply(Container container) {
            System.out.println("Adding " + _component + " to " + container + " constraints " + _constraints);
            if (_component != null && container != null) {
                if(_constraints == null){
                    container.add(_component);
                }
                else{
                    container.add(_component, _constraints);
                }
            }
            return container;
        }
    }

    /**
     * returns a Function that sets the layoutManager of the container to the layoutManager provided
     * @param layoutMgr
     * @return
     */
    public static Function<Container, Container> withLayout(LayoutManager layoutMgr) {
        return new WithLayoutManagerFunction(layoutMgr);
    }

    /**
     * Internal implementation of the withLayout function
     */
    private static class WithLayoutManagerFunction implements Function<Container, Container> {

        private final LayoutManager _layoutManager;

        public WithLayoutManagerFunction(LayoutManager layoutManager) {
            _layoutManager = layoutManager;
        }

        @Override
        public Container apply(Container container) {
            container.setLayout(_layoutManager);
            return container;
        }
    }

    /**
     * returns a method that sets the name of a component
     * @param name
     * @return
     */
    public static Function<Component, Component> withName(String name) {
        return new SetNameFunction(name);
    }

    /**
     * Internal implementation of the withName function
     */
    private static class SetNameFunction implements Function<Component, Component> {

        private final String _name;

        public SetNameFunction(String name) {
            _name = name;
        }

        @Override
        public Component apply(Component component) {
            component.setName(_name);
            return component;
        }
    }

    /**
     * Returns a function that sets the size of a JComponent to the width and height specified.
     * @param width
     * @param height
     * @return
     */
    public static Function<JComponent, JComponent> withSize(int width, int height) {
        return withSize(new Dimension(width, height));
    }

    /**
     * Returns a function that sets the size of a JComponent to the dimension specified
     * @param dim
     * @return
     */
    public static Function<JComponent, JComponent> withSize(Dimension dim) {
        return new SetSizeFunction(dim);
    }

    /**
     * Internal implementation of the withSize function.
     */
    private static class SetSizeFunction implements Function<JComponent, JComponent> {

        private final Dimension _dim;

        public SetSizeFunction(Dimension dim) {
            _dim = dim;
        }

        @Override
        public JComponent apply(JComponent f) {
            f.setSize(_dim);
            return f;
        }
    }

    //JPanel helpers
    /**
     * convenience method for creating a jpanel.
     * @return
     */
    public static JPanel jpanel() {
        return jPanelSupplier().get();
    }

    /**
     * Supplier for JPanel's.  Returns JPanel's in a lazy fashion.
     * @return
     */
    public static Supplier<JPanel> jPanelSupplier() {
        return new JPanelSupplier();
    }

    /**
     * Internal implementation of the jPanelSupplier supplier
     */
    private static class JPanelSupplier implements Supplier<JPanel> {

        @Override
        public JPanel get() {
            return new JPanel();
        }
    }

    //JFrame helpers
    /**
     * Returns a function that sets the title of a JFrame to the specified title.
     * @param title
     * @return
     */
    public static Function<JFrame, JFrame> withTitle(final String title) {
        return new Function<JFrame, JFrame>() {

            @Override
            public JFrame apply(JFrame jFrame) {
                jFrame.setTitle(title);
                return jFrame;
            }
        };
    }

    /**
     * returns a JFrame with the title specified.
     * @param title
     * @return
     */
    public static JFrame jFrame(String title) {
        return jFrameFunction().apply(title);
    }

    /**
     * returns a Function that takes in a string and returns a JFrame with a title set to the value of that string.
     * @return
     */
    public static Function<String, JFrame> jFrameFunction() {
        return new JFrameFunction();
    }

    /**
     * Internal implementation of a jFrameFunction
     */
    private static class JFrameFunction implements Function<String, JFrame> {

        @Override
        public JFrame apply(String f) {
            return new JFrame(f);
        }
    }

    //JMenubar helpers
    /**
     * returns a function that adds a JMenuBar containing the menus provided to a JFrame
     * @param menus
     * @return
     */
    public static Function<JFrame, JFrame> withJMenuBar(JMenu... menus) {
        return new JMenuBarFunction(jMenuBar(menus));
    }

    /**
     * Internal implementation of the withJMenuBar function
     */
    private static class JMenuBarFunction implements Function<JFrame, JFrame> {

        private final JMenuBar _menuBar;

        public JMenuBarFunction(JMenuBar menuBar) {
            _menuBar = menuBar;
        }

        @Override
        public JFrame apply(JFrame jFrame) {
            jFrame.setJMenuBar(_menuBar);
            return jFrame;
        }
    }

    /**
     * Convenience method for creating a JMenuBar
     * @return
     */
    public static JMenuBar jMenuBar() {
        return jMenuBar(new JMenu[0]);
    }

    /**
     * Convenience method for creating a JMenuBar with the menus specified.
     * @param menus
     * @return
     */
    public static JMenuBar jMenuBar(JMenu... menus) {
        JMenuBar menuBar = new JMenuBar();
        for (JMenu menu : menus) {
            menuBar.add(menu);
        }
        return menuBar;
    }

    /**
     * returns a Function that adds a JMenu to a JMenuBar
     * @param menu
     * @return
     */
    public static Function<JMenuBar, JMenuBar> withJMenu(JMenu menu) {
        return withJMenus(menu);
    }

    /**
     * returns a Function that adds a set of menus to a JMenuBar
     * @param menus
     * @return
     */
    public static Function<JMenuBar, JMenuBar> withJMenus(final JMenu... menus) {
        return new WithJMenuFunction(menus);
    }

    /**
     * Internal implementation of the withJMenus function
     */
    private static class WithJMenuFunction implements Function<JMenuBar, JMenuBar> {

        final JMenu[] _menus;

        public WithJMenuFunction(JMenu[] menus) {
            _menus = menus;
        }

        @Override
        public JMenuBar apply(JMenuBar jMenuBar) {
            for (JMenu menu : _menus) {
                jMenuBar.add(menu);
            }
            return jMenuBar;
        }
    }

    //JMenu helpers
    /**
     * Convenience method for creating a JMenu
     * @param name
     * @return
     */
    public static JMenu jMenu(String name) {
        return new JMenu(name);
    }

    /**
     * Returns a Function that adds a horizonatal separator to a JMenu
     * @return
     */
    public static Function<JMenu, JMenu> withSeparator() {
        return new JMenuWithSeparatorFunction();
    }

    /**
     * Internal implementation ofthe withSeparator function
     */
    private static class JMenuWithSeparatorFunction implements Function<JMenu, JMenu> {

        @Override
        public JMenu apply(JMenu jMenu) {
            jMenu.addSeparator();
            return jMenu;
        }
    }




    //JMenuItem helpers
    /**
     * Convenience method for creating a JMenuItem with the specified name.
     * @param name
     * @return
     */
    public static JMenuItem jmenuitem(String name) {
        return new JMenuItem(name);
    }

    //JMenuItem helpers
    /**
     * Convenience method for creating a JMenuItem with the specified name.
     * @param act
     * @return
     */
    public static JMenuItem jmenuitem(Action act) {
        return new JMenuItem(act);
    }

    /**
     * Returns a Function that add a specified submenu to a JMenuItem
     * @param menuItem
     * @return
     */
    public static Function<JMenuItem, JMenuItem> withJMenuItem(JMenuItem menuItem) {
        return withJMenuItems(menuItem);
    }

    /**
     * Returns a function that adds a specified set of submenus to a JMenuItem
     * @param menuItems
     * @return
     */
    public static Function<JMenuItem, JMenuItem> withJMenuItems(final JMenuItem... menuItems) {
        return new WithJMenuItemsFunction(menuItems);
    }

    /**
     * Private implementation of the withJMenuItems function
     */
    private static class WithJMenuItemsFunction implements Function<JMenuItem, JMenuItem> {

        private final JMenuItem[] _menuItems;

        public WithJMenuItemsFunction(JMenuItem[] menuItems) {
            _menuItems = menuItems;
        }

        @Override
        public JMenuItem apply(JMenuItem jMenu) {
            for (JMenuItem item : _menuItems) {
                System.out.println("Adding menu item " + item + " to " + jMenu);
                jMenu.add(item);
            }
            return jMenu;
        }
    }

    /**
     * returns a function that adds an accelerator to a JMenuItem
     * @param keyStroke
     * @return
     */
    public static Function<JMenuItem, JMenuItem> withAccelerator(KeyStroke keyStroke) {
        return new WithAcceleratorFunction(keyStroke);
    }

    /**
     * Internal implementation of the withAccelerator function
     */
    private static class WithAcceleratorFunction implements Function<JMenuItem, JMenuItem> {

        private final KeyStroke _keyStroke;

        public WithAcceleratorFunction(KeyStroke keyStroke) {
            _keyStroke = keyStroke;
        }

        @Override
        public JMenuItem apply(JMenuItem jMenuItem) {
            jMenuItem.setAccelerator(_keyStroke);
            return jMenuItem;
        }
    }

    /**
     * returns a Function that adds a mnemonic to a JMenuItem
     * @param keyEvent
     * @return
     */
    public static Function<JMenuItem, JMenuItem> withMnemonic(int keyEvent) {
        return new WithMnemonicFunction(keyEvent);
    }

    /**
     * Internal implementation of the withMnemonic function
     */
    private static class WithMnemonicFunction implements Function<JMenuItem, JMenuItem> {

        private final int _keyEvent;

        public WithMnemonicFunction(int keyEvent) {
            _keyEvent = keyEvent;
        }

        @Override
        public JMenuItem apply(JMenuItem jMenuItem) {
            jMenuItem.setMnemonic(_keyEvent);
            return jMenuItem;
        }
    }

    //JComponent helpers
    /**
     * Returns a Function that sets a JComponents toolTip to the tool tip specified.
     * @param toolTipText
     * @return
     */
    public static Function<JComponent, JComponent> withToolTip(final String toolTipText) {
        return new WithToolTipFunction(toolTipText);
    }

    /**
     * Internal implementation of the withToolTip Fucntion
     */
    private static class WithToolTipFunction implements Function<JComponent, JComponent> {

        private final String _toolTipText;

        public WithToolTipFunction(String toolTipText) {
            _toolTipText = toolTipText;
        }

        @Override
        public JComponent apply(JComponent jComponent) {
            jComponent.setToolTipText(_toolTipText);
            return jComponent;
        }
    }

    //Builder pattern helpers
    /**
     * This methods enables the chaining of a set of functions in order to transform an object
     * The input of the first function is set to the value of the first parameter.
     * The result of each function call us used as the input of the following function.
     * The result of the last function is returned by this method.
     * after each function called the resulting type is cast back into the input type C.
     * It is assumed that the type of the output will match the type of the input.
     * Functions accepting and returning superclasses of C are allowed as a convenience.
     * @param comp
     * @param functions
     * @param <C>
     * @return
     */
    public static <C> C build(C comp, Function<? super C, ? super C>... functions) {
        return new BuildSupplier<C>(comp, functions).get();
    }

    /**
     * A lazy implementation of the build command
     *
     * Note:  While this function is generic, it's implementation is not type safe.
     * Internally it uses casting in order to keep the output type the same as the input type.
     *
     * @param comp - the component you wish to mutate
     * @param functions - the functions that perform the mutation
     * @param <C> - the type of the component
     * @return
     */
    public static <C> Supplier<C> builder(C comp, Function<? super C, ? super C>... functions) {
        return new BuildSupplier<C>(comp, functions);
    }

    /**
     * Convenience method for adding all components to a specified container.
     * @param cont
     * @param components
     */
    public static void addAllComponentsTo(Container cont, Component... components) {
        addAllComponentsTo(cont, Arrays.asList(components));
    }

    /**
     * Convenience method for adding all components to a specified container.
     * @param cont
     * @param compCollection
     */
    public static void addAllComponentsTo(Container cont, Collection<? extends Component> compCollection) {
        for (Component component : compCollection) {
            cont.add(component);
        }
    }


    /**
     * Overload of the three parameter version of getComponentWithBaseClass.
     * This call will NOT recurse down any Container objects it encounters.
     * @param <T> any type of Container or its subclasses.
     * @param container - the Container to search in.  Can not be null.
     * @param compClass - the type of Component to search for.  Can not be null.
     * @return - a list of components matching the type specified found in the container provided.
     */
    public static <T extends Component> java.util.List<T> getComponentsWithBaseClass(Container container, Class<T> compClass){
        return getComponentsWithBaseClass(container, compClass, false);
    }

    /**
     * Returns all components in a container that have a superclass matching a specified class.  Will recursively search
     * sub-Container objects if specified.
     * @param <T> and type of Container or its subclasses.
     * @param container - the Container to search in.
     * @param compClass - the type of component to search for.
     * @param recurseThroughContainers - if true then the search algorithm with recursively search for components of this class in any Container it encounters.
     * @return
     */
    public static <T extends Component> java.util.List<T> getComponentsWithBaseClass(Container container, Class<T> compClass, boolean recurseThroughContainers){
        checkNotNull(container);
        checkNotNull(compClass);
        java.util.List<T> components = newArrayList();
        for(Component comp : container.getComponents()){
            if(compClass.isAssignableFrom(comp.getClass())){
                components.add(compClass.cast(comp));
            }
            if (recurseThroughContainers == true && comp instanceof Container){
                Container subContainer = (Container) comp;
                java.util.List<T> subComponents = getComponentsWithBaseClass(subContainer, compClass, recurseThroughContainers);
                components.addAll(subComponents);
            }
        }
        return components;
    }




    /**
     * Convenience method to wrap a view in a JFrame and show it.
     * @param view - the view to show
     * @param name - the title you want the frame to have.
     * @return a JFrame containing the view specified with the title specified.
     */
    public static JFrame displayInJFrame(JPanel view, String name) {
        return displayInJFrame(view, name, null);
    }

    /**
     * Convenience method to wrap a view in a JFrame and show it.
     *
     * This method also sets the following JFrame properties
     * name - name parameter
     * title - name parameter
     * location - 200x 200y
     * visible - true
     * minimumSize - the packed size of the JFrame.
     *
     * @param view - the view to show
     * @param name - the title you want the frame to have.
     * @param menu - the menu you want the JFrame to have.
     * @return a JFrame containing the view specified with the title specified.
     */
    public static JFrame displayInJFrame(JPanel view, String name, JMenuBar menu) {
        JFrame window = new JFrame();
        window.getContentPane().add(view);
        if (menu != null) {
            window.setJMenuBar(menu);
        }
        window.setName(name);
        window.setTitle(name);
        window.setLocation(200, 200);
        window.pack();
        window.setVisible(true);
        window.setMinimumSize(window.getSize());
        return window;
    }

    /**
     * Convenience method for putting a JComponent in a JScrollPane
     * @param comp
     * @return
     */
    public static JScrollPane embedInJScrollPane(Component comp) {
        JScrollPane scrollView = new JScrollPane(comp);
        scrollView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollView.setPreferredSize(new Dimension(600, 400));
        scrollView.getVerticalScrollBar().setUnitIncrement(10);
        scrollView.getVerticalScrollBar().setBlockIncrement(10);
        scrollView.getHorizontalScrollBar().setUnitIncrement(10);
        scrollView.getHorizontalScrollBar().setBlockIncrement(10);
        return scrollView;
    }

    /**
	 * Typesafe enum for specifying a JSplitPane orientation
	 */
    public static enum Split {

        /**
		 * @uml.property  name="vERTICAL"
		 * @uml.associationEnd  
		 */
        VERTICAL(JSplitPane.VERTICAL_SPLIT), /**
		 * @uml.property  name="hORIZONTAL"
		 * @uml.associationEnd  
		 */
        HORIZONTAL(JSplitPane.HORIZONTAL_SPLIT);
        private final int _orientation;

        private Split(int orientation) {
            _orientation = orientation;
        }

        public int orientation() {
            return _orientation;
        }
    }

    /**
     * Convenience method for putting two JComponent's in a JSplitPane
     * @param first - the left component in a horizontal orientation, top in a vertical one.
     * @param second - the right component in a horizontal orientation, bottom in a vertical one.
     * @param splitType - Split.VERTICAL or Split.HORIZONTAL
     * @return the JSplitPane described above
     */
    public static JSplitPane embedInJSplitPane(JComponent first, JComponent second, Split splitType) {
        return new JSplitPane(splitType.orientation(), first, second);
    }

    /**
     * Retrieves a component by name inside a container.  Containers in this container will be traversed recursively.
     * @param container
     * @param componentName
     * @param compClass
     * @param <T>
     * @return
     */
    public static <T extends Component> T getComponentNamed(Container container, String componentName, Class<T> compClass) {
        checkNotNull(componentName);
        if (componentName.equals(container.getName())) {
            return (T) container;
        } else {
            Component[] components = container.getComponents();
            for (Component component : components) {
                if (component instanceof Container) {
                    T result = getComponentNamed((Container) component, componentName, compClass);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public static void resizeComponentsToDimension(Dimension dim, Component... containers){
        for (Component cont : containers) {
            cont.setMinimumSize(dim);
            cont.setPreferredSize(cont.getMinimumSize());
            cont.setBounds(new Rectangle(0, 0, (int)dim.getWidth(), (int)dim.getHeight()));
            cont.doLayout();
            cont.repaint();
        }


    }

    public static MenuElement getMenuItemByName(JFrame frame, String name) {
        return getMenuItemByName(frame.getJMenuBar(), name);
    }

    public static MenuElement getMenuItemByName(JMenuBar menuBar, String name) {
        for (int i = 0; i < menuBar.getMenuCount(); ++i) {
            MenuElement result = getMenuItemByName(menuBar.getMenu(i), name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static MenuElement getMenuItemByName(MenuElement menuItem, String name) {
        if (menuItem.getComponent().getName().equals(name)) {
            return menuItem;
        }
        for (MenuElement element : menuItem.getSubElements()) {
            MenuElement result = getMenuItemByName(element, name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static Window getActiveWindow() {
        Window currentWindow = null;
        for (Window window : JFrame.getWindows()) {
            if (window.isActive() && window.isFocused()) {
                currentWindow = window;
            }
        }
        return currentWindow;
    }

    public static Window locateSuitableWindowForActionEvent(ActionEvent ae){
        Object source = ae.getSource();
        Window top = null;
        if(source != null && source instanceof Component){
            Component comp = (Component)source;
            top  = SwingUtilities.getWindowAncestor(comp);
        }
        if(top == null){
            top = getActiveWindow();
        }
        return top;

    }

    public static Container replaceComponent(Component old, Component replacement){
        Container cont = old.getParent();
        if(cont != null){
            Component[] components = cont.getComponents();
            int compIndex = -1;
            for(Component c : components){
                if(c == old){
                    break;
                }
                compIndex++;
            }
            if(compIndex >= 0){
                replacement.setBounds(old.getBounds());
                cont.add(replacement, compIndex);
                cont.remove(old);
            }
        }
        return cont;
    }

    /**
     * Convenience method for setting a look and feel.  Any encountered exceptions will be ignored
     * @param lookAndFeelClassName
     * @return
     */
    public static boolean setApplicationLookAndFeel(String lookAndFeelClassName) {
        boolean result = false;
        try {
            UIManager.setLookAndFeel(lookAndFeelClassName);
            result = true;
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (UnsupportedLookAndFeelException e) {
        }
        return result;
    }

    /**
     * Internal implementation of the build supplier using the Supplier interface provided by
     * Google Guava
     * @param <T> Whatever type you would like to build off of.  For this class we only use it for AWT and Swing components.
     */
    private static class BuildSupplier<T> implements Supplier<T> {

        final T _comp;
        final Function<? super T, ? super T>[] _functions;

        public BuildSupplier(T comp, Function<? super T, ? super T>[] functions) {
            _comp = comp;
            _functions = functions;
        }

        @Override
        public T get() {
            T comp = _comp;
            for (Function<? super T, ? super T> function : _functions) {
                comp = (T) function.apply(comp);
            }
            return comp;
        }
    }
}

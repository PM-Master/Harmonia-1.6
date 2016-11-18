package gov.nist.csd.pm.common.util.swing;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import gov.nist.csd.pm.common.util.Delegate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import static gov.nist.csd.pm.common.util.collect.Arrays.isNullOrEmpty;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.focusOnShown;
import static gov.nist.csd.pm.common.util.swing.SwingShortcuts.getActiveWindow;

/**
 * @author  Administrator
 */
public class DialogUtils {


    private static JFileChooser fileChooser;

    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static DialogBuilder buildDialog() {
        return new DialogBuilder();
    }

    public static class DialogBuilder {
        private String DEFAULT_TITLE = "Dialog";
        private String DEFAULT_DESCRIPTION = "Your message here.";
        Predicate<String> _validationPredicate = Predicates.alwaysTrue();

        private DialogBuilder() {

        }

        int dialogType;

        String title = DEFAULT_TITLE;
        Object[] message = new Object[]{DEFAULT_DESCRIPTION};
        Window parent = getActiveWindow();
        Dialog.ModalityType modalityType = Dialog.ModalityType.DOCUMENT_MODAL;
        Object[] options;
        Object initialOption;
        Icon icon;
        int optionType;


        public DialogBuilder withIcon(Icon icon) {
            this.icon = icon;
            return this;
        }

        public DialogBuilder withParent(Window comp) {
            this.parent = comp;
            return this;
        }

        public DialogBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public DialogBuilder withMessage(String message) {
            return withMessage(new Object[]{message});
        }

        public DialogBuilder withMessage(Object[] message) {
            this.message = message;
            return this;
        }

        public DialogBuilder withModalityType(Dialog.ModalityType type) {
            modalityType = type;
            return this;
        }

        public DialogBuilder error() {
            dialogType = JOptionPane.ERROR_MESSAGE;
            return this;
        }

        public DialogBuilder info() {
            dialogType = JOptionPane.INFORMATION_MESSAGE;
            return this;
        }

        public DialogBuilder warn() {
            dialogType = JOptionPane.WARNING_MESSAGE;
            return this;
        }

        public DialogBuilder question() {
            dialogType = JOptionPane.QUESTION_MESSAGE;
            return this;
        }


        public DialogBuilder plain() {
            dialogType = JOptionPane.PLAIN_MESSAGE;
            return this;
        }


        class SelectedOptionSupplier extends ValidatingDialogResultSupplier<Integer> {

            public SelectedOptionSupplier(JOptionPane validatingInputDialog) {
                super(validatingInputDialog);
            }

            @Override
            public Integer get() {
                return (Integer) dialog.getValue();
            }
        }

        private JOptionPane getOptionPane() {
            JOptionPane op = new JOptionPane();
            op.setMessage(message);
            op.setMessageType(dialogType);
            if (!isNullOrEmpty(options)) {
                op.setOptions(options);
                op.setInitialValue(initialOption);

            }
            return op;
        }

        private <T> void runWithCallback(final Predicate<T> validator, final Delegate<T> callback) {
            JOptionPane optionPane = getOptionPane();
            runWithCallback(optionPane, new SelectedValueSupplier<T>(optionPane), validator, callback);
        }

        private <T> void runWithCallback(final Supplier<T> supplier,
                                         final Predicate<? super T> validator,
                                         final Delegate<? super T> callback) {
            runWithCallback(getOptionPane(), supplier, validator, callback);
        }

        private <T> void runWithCallback(final JOptionPane optionPane,
                                         final Supplier<T> supplier,
                                         final Predicate<? super T> validator,
                                         final Delegate<? super T> callback) {
            final JDialog dialog = optionPane.createDialog(parent, title);
            dialog.setModalityType(JDialog.ModalityType.APPLICATION_MODAL);
            dialog.setLocationByPlatform(true);
            dialog.setLocationRelativeTo(parent);
            boolean test = false;
            T result = null;
            do{
                dialog.setVisible(true);
                Object selection = optionPane.getValue();
                if (selection == null) {
                    return;
                }

                result = supplier.get();
                test = validator.apply(result);
            }while(!test);
            callback.delegate(result);


        }

        public void presentWithOptions(int options, Predicate<Integer> validator, Delegate<Integer> callback) {
            optionType = options;
            runWithCallback(validator, callback);

        }

        public abstract class ValidatingDialogResultSupplier<T> implements Supplier<T> {
            protected final JOptionPane dialog;

            public ValidatingDialogResultSupplier(JOptionPane validatingInputDialog) {
                dialog = validatingInputDialog;
            }

            @Override
            public abstract T get();
        }

        private class SelectedValueSupplier<T> extends ValidatingDialogResultSupplier<T> {

            private SelectedValueSupplier(JOptionPane dialog) {
                super(dialog);
            }


            @Override
            public T get() {
                return (T) dialog.getValue();
            }
        }

        public <T> void presentWithSelections(T[] values, T initial, Predicate<T> validator, Delegate<T> callback) {
            this.options = values;
            this.initialOption = initial;
            runWithCallback(validator, callback);
        }


        public void presentForPassword(Predicate<? super char[]> validator, Delegate<? super char[]> callback) {
            JPasswordField passwordField = focusOnShown(new JPasswordField());

            withMessage(new Object[]{message, passwordField});
            runWithCallback(new SwingShortcuts.EnteredPasswordSupplier(passwordField), validator, callback);
        }

        public void presentForInput(Predicate<? super String> validator, Delegate<? super String> callback) {
            JTextField jTextField = focusOnShown(new JTextField());
            if (initialOption != null) {
                jTextField.setText(initialOption.toString());
            }
            withMessage(new Object[]{message, jTextField});
            runWithCallback(new SwingShortcuts.EnteredTextSupplier(jTextField), validator, callback);
        }


    }

    /**
     * @param message    the message to display in the dialog
     * @param isPassword indicates if the dialog should have a password field of text
     *                   field
     * @return what the user entered into the dialog box
     */
    public static String showQuestionDisplay(String message, boolean isPassword) {
        final JTextField ksPassField;
        if (isPassword) {
            ksPassField = new JPasswordField();
        } else {
            ksPassField = new JTextField();
        }
        String ksPass = null;
        while (true) {
            JOptionPane optionPane = new JOptionPane(new Object[]{message, ksPassField}, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = optionPane.createDialog("Password");
            dialog.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent ce) {
                    ksPassField.requestFocusInWindow();
                }
            });
            dialog.setVisible(true);
            int result = ((Integer) optionPane.getValue()).intValue();
            if (result == JOptionPane.CANCEL_OPTION) {
                System.exit(1);
            }
            if (isPassword) {
                ksPass = new String(
                        ((JPasswordField) ksPassField).getPassword());
            } else {
                ksPass = ksPassField.getText();
            }
            if (ksPass != null && ksPass.length() > 0) {
                break;
            }
        }
        return ksPass;
    }

    private static File lastSelectedFile = null;
    private static final String WORKING_DIRECTORY_KEY = "user.dir";

    static {
        String workingDirectoryPath = System.getProperty(WORKING_DIRECTORY_KEY);
        lastSelectedFile = new File(workingDirectoryPath);
    }


    /**
     * @param title the title for the file chooser
     * @return the path to the file that was chosen
     */
    public static String openFileChooser(String title) {

        JFileChooser fileChooser = getFileChooser();
        fileChooser.setDialogTitle(title);
        fileChooser.setCurrentDirectory(lastSelectedFile);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.CANCEL_OPTION) {
            return "";
        }
        lastSelectedFile = fileChooser.getSelectedFile();
        return lastSelectedFile.getAbsolutePath();
    }

    private static String dataEntryFormat(String what, String whom){
        return String.format("Enter the %s for %s", what, whom);
    }

    public static void getAllSystemProperties(String systemContext) {
        String keyStorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
        if (keyStorePassword == null) {
            String keyPass = DialogUtils.showQuestionDisplay(dataEntryFormat("Keystore Password", systemContext), true);
            System.setProperty("javax.net.ssl.keyStorePassword", keyPass);
        }
        if (System.getProperty("javax.net.ssl.keyStore") == null) {
            //JOptionPane.showMessageDialog(null, "Please push OK and select the keystore location");
            String keystoreLocation = DialogUtils.openFileChooser(dataEntryFormat("Keystore Location", systemContext));
            System.setProperty("javax.net.ssl.keyStore", keystoreLocation);
        }
        if (System.getProperty("javax.net.ssl.trustStore") == null) {
            //JOptionPane.showMessageDialog(null, "Please push OK and select the trustore location");
            String trustStoreLocation = DialogUtils.openFileChooser(dataEntryFormat("Truststore Location", systemContext));
            System.setProperty("javax.net.ssl.trustStore", trustStoreLocation);
        }
    }

    /**
	 * @return
	 * @uml.property  name="fileChooser"
	 */
    private static JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        return fileChooser;
    }
}

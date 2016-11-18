package gov.nist.csd.pm.application.schema.builder;

import edu.cnu.cs.gooey.Gooey;
import edu.cnu.cs.gooey.GooeyDialog;
import edu.cnu.cs.gooey.GooeyFrame;
import gov.nist.csd.pm.common.constants.GlobalConstants;
import gov.nist.csd.pm.common.graphics.MenuToggleButton;
import gov.nist.csd.pm.user.SessionManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchemaBuilder3Test {

    private String name = "";
    private static SchemaBuilder3 schema;
    public static String sessId;

    public static void setParams(String s){
        l("setParams");
        sessId = s;
    }

    public static void setSchemaBuilder(SchemaBuilder3 s){
        l("setSchemaBuilder");
        schema = s;
    }

    private static void l(Object o){
        System.out.println(o);
    }

    public void createSchema(){
        Gooey.capture(new GooeyFrame() {

            @Override
            public void invoke() {
                schema = new SchemaBuilder3(sessId, GlobalConstants.PM_DEFAULT_SIMULATOR_PORT, "", false);
            }

            @Override
            public void test(JFrame f) {
                List<JTextField> fields = Gooey.getComponents(schema, JTextField.class);
                JTextField nameField = fields.get(0);
                JTextField descrField = fields.get(1);
                JTextField propsField = fields.get(3);
                JTextField infoField = fields.get(2);

                name = UUID.randomUUID().toString();
                nameField.setText(name);
                descrField.setText(name);
                propsField.setText("prop=" + name);
                infoField.setText(name);

                JButton btnCreateSchema = Gooey.getButton(schema, "Create Schema");
                btnCreateSchema.doClick();
            }
        });
    }

    public void createTables(final int numTables){
        final JButton btnCreateTable = Gooey.getButton(schema, "Create Table");
        assertTrue(btnCreateTable.isEnabled());

        Gooey.capture(new GooeyFrame() {
            @Override
            public void invoke() {
                btnCreateTable.doClick();
            }

            @Override
            public void test(JFrame jf) {
                List<JTextField> colFields = Gooey.getComponents(jf, JTextField.class);
                int x = (int) (Math.random() * 10);
                colFields.get(0).setText(name + "-" + x);
                if (numTables > 0) {
                    for (int i = 0; i < numTables; i++) {
                        JButton btnAddColumn = Gooey.getButton(jf, "Add Column");
                        btnAddColumn.doClick();
                    }

                    colFields = Gooey.getComponents(jf, JTextField.class);
                    for (int i = 1; i < colFields.size(); i++) {
                        colFields.get(i).setText(name + "-" + x + "." + i);
                    }

                    JButton btnCreate = Gooey.getButton(jf, "Create");
                    btnCreate.doClick();
                } else {
                    JButton btnClose = Gooey.getButton(jf, "Close");
                    btnClose.doClick();
                }
            }
        });
    }

    public void createTables(final int numTables, final int numCols){
        final JButton btnCreateTable = Gooey.getButton(schema, "Create Table");
        assertTrue(btnCreateTable.isEnabled());

        Gooey.capture(new GooeyFrame() {
            @Override
            public void invoke() {
                btnCreateTable.doClick();
            }

            @Override
            public void test(JFrame jf) {
                List<JTextField> colFields = Gooey.getComponents(jf, JTextField.class);
                colFields.get(0).setText(name + "-" + numTables);
                if (numTables > 0) {
                    for (int i = 0; i < numTables; i++) {
                        for (int j = 0; j < numCols; j++) {
                            JButton btnAddColumn = Gooey.getButton(jf, "Add Column");
                            btnAddColumn.doClick();
                        }

                        colFields = Gooey.getComponents(jf, JTextField.class);
                        for (int j = 1; j < colFields.size(); j++) {
                            colFields.get(i).setText(name + "." + i);
                        }

                        JButton btnCreate = Gooey.getButton(jf, "Create");
                        btnCreate.doClick();
                    }
                } else {
                    JButton btnClose = Gooey.getButton(jf, "Close");
                    btnClose.doClick();
                }
            }
        });
    }

    //have to call doClick on combocbox item
    public void addKeys(final int numTables, final int numKeys){
        final JButton mngKeys = Gooey.getButton(schema, "Manage Keys");
        Gooey.capture(new GooeyFrame() {
            @Override
            public void invoke() {
                mngKeys.doClick();
            }

            @Override
            public void test(JFrame jf) {
                List<JComboBox> comboBoxes = Gooey.getComponents(jf, JComboBox.class);
                JComboBox tableBox = comboBoxes.get(1);
                JComboBox colBox = comboBoxes.get(0);

                for (int i = 0; i < numTables; i++) {
                    for (int j = 0; j < numKeys; j++) {
                        tableBox.setSelectedIndex(i);
                        System.out.println(colBox.getModel().getSize());
                        colBox.setSelectedIndex(j);
                        l(tableBox.getSelectedItem());
                        l(colBox.getSelectedItem());

                        JButton add = Gooey.getComponent(jf, JButton.class);
                        add.doClick();
                    }
                }
                System.out.println("Closing key manager..." + jf);
                Gooey.getButton(jf, "Close").doClick();
            }
        });
    }

    public void submitSchema() {
        Gooey.capture(new GooeyDialog() {
            @Override
            public void invoke() {
                JButton btnSubmit = Gooey.getButton(schema, "Submit Schema");
                btnSubmit.doClick();
            }

            @Override
            public void test(final JDialog j) {
                Gooey.getButton(j, "Yes").doClick();
            }
        });
        schema.dispose();
    }

    public void assertErrorOnSubmit(){
        Gooey.capture(new GooeyDialog() {
            @Override
            public void invoke() {
                JButton btnSubmit = Gooey.getButton(schema, "Submit Schema");
                btnSubmit.doClick();
            }

            @Override
            public void test(final JDialog j) {
                Gooey.capture(new GooeyDialog() {
                    @Override
                    public void invoke() {
                        Gooey.getButton(j, "Yes").doClick();
                    }

                    @Override
                    public void test(JDialog jDialog) {
                        assertTrue(jDialog.isVisible());
                        Gooey.getButton(jDialog, "OK").doClick();
                    }
                });

            }
        });
        schema.dispose();
    }

    @Test
    public void testXBeforeSubmitting(){
        createSchema();
        createTables(2);
        schema.dispatchEvent(new WindowEvent(schema, WindowEvent.WINDOW_CLOSING));
        List<String> pcs = schema.getUtilities().getPolicyClasses();
        for(String pc : pcs){
            assertFalse("schema was not deleted", pc.split(":")[0].equals(name));
        }
    }

    @Test
    public void testDoneBeforeSubmitting(){
        createSchema();
        createTables(2);
        JButton btnDone = Gooey.getButton(schema, "Done");
        btnDone.doClick();
        List<String> pcs = schema.getUtilities().getPolicyClasses();
        for(String pc : pcs){
            assertFalse("schema was not deleted", pc.split(":")[0].equals(name));
        }
    }

    @Test
    public void test2Table2Col1Key(){
        createSchema();
        createTables(2);
        createTables(2);
        addKeys(2, 1);
        submitSchema();
    }

    @Test
    public void test1Table2Col2Key(){
        createSchema();
        createTables(2);
        addKeys(1, 2);
        submitSchema();
    }

    @Test
    public void test1Table1Col1Key(){
        createSchema();
        createTables(1);
        addKeys(1, 1);
        submitSchema();
    }

    @Test
    public void testNoTablesNoKeysError(){
        createSchema();
        createTables(0);
        assertErrorOnSubmit();
    }

    @Test
    public void test1TableNoKeysError(){
        createSchema();
        createTables(1);
        assertErrorOnSubmit();
    }
}
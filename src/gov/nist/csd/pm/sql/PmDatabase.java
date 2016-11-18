package gov.nist.csd.pm.sql;

import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;
import javax.swing.*;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PmDatabase {
    /** The MySQL database connector. */
    public ComboPooledDataSource ds;

    public PmDatabase() throws PropertyVetoException, IOException, Exception {
        System.out.println(System.getProperty("user.dir"));
        ds = new ComboPooledDataSource();
        Properties props = new Properties();
        String file = "../conf/DB_properties/db_props.properties";
        InputStream inputStream = new FileInputStream(new File(file));
        if (inputStream != null) {
            props.load(inputStream);

            String driver = props.getProperty("driver");
            String url = props.getProperty("url");
            String user = props.getProperty("username");
            String pass = props.getProperty("password");

            if((pass == null || pass.isEmpty())) {
                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout(1, 2));
                JTextField passField = new JTextField(15);
                panel.add(new JLabel("Password: "));
                panel.add(passField);
                int ret = JOptionPane.showConfirmDialog(null,
                        panel,
                        "Root user password",
                        JOptionPane.OK_CANCEL_OPTION);
                if (ret == JOptionPane.OK_OPTION) {
                    pass = passField.getText();

                    props.setProperty("password", pass);
                    props.store(new FileOutputStream(file), null);
                } else {
                    throw new Exception("Cannot connect to database");
                }
            }


            ds.setDriverClass(driver);
            ds.setJdbcUrl(url);
            ds.setUser(user);
            ds.setPassword(pass);

            ds.setMinPoolSize(3);
            ds.setAcquireIncrement(1);
            ds.setMaxPoolSize(30);
            ds.setMaxStatements(20);

            ds.setCheckoutTimeout(2000);
        }
    }

    public Connection getConnection() throws Exception /*, ClassNotFoundException */ {
		/*System.out.println("Busy connections: " + ds.getNumBusyConnections() +
				", Total connections: " + ds.getNumConnections() +
				", Unclosed connections: " + ds.getNumUnclosedOrphanedConnections() +
				", Idle connections: " + ds.getNumIdleConnections());*/
        return ds.getConnection();
    }

    public void closeConnection(){

    }

    public static void main(String[] args) {
        //String input = JOptionPane.showInputDialog(null, "Enter jdbc url:");

        Connection conn = null;

        try {
            Class.forName("com.mysql.jdbc.Driver");
            //System.out.println("Connecting to database...");
            //connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/","root", "root");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "root");
            if (conn != null) {
                //System.out.println("connected to db");
                conn.setAutoCommit(true); // No need to manually commit

            } else {
                JOptionPane.showMessageDialog(null, "Could not establish database connection...");
            }

            long s = System.currentTimeMillis();
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("use policydb");

            PreparedStatement ps = conn.prepareCall("show tables");
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int numCols = meta.getColumnCount();
            if (rs!=null) {
                while(rs.next()){
                    String tableName = (String) rs.getObject(1);
                    System.out.println(tableName);
                    System.out.print("Checking consistency...");

                    ps = conn.prepareCall("checksum table " + tableName);
                    ResultSet rs1 = ps.executeQuery();
                    rs1.next();
                    System.out.println(rs1.getObject(2));

                    String sql = "select * from " + tableName;

                    ps = conn.prepareCall(sql);
                    rs1 = ps.executeQuery();
                    ResultSetMetaData meta1 = rs1.getMetaData();
                    int numCols1 = meta1.getColumnCount();
                    if (rs1!=null) {
                        while(rs1.next()){
                            ArrayList<Object> row = new ArrayList<Object>();
                            for(int i = 0; i < numCols1; i++){
                                row.add(rs1.getObject(i+1));
                            }
                            System.out.println("\t"+row);
                        }
                    }
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("TIME: " + (double)(end-s)/1000);

            ps = conn.prepareCall("checksum table node");
            rs = ps.executeQuery();
            rs.next();
            System.out.println(rs.getObject(2));

            ps = conn.prepareCall("insert into node (node_type_id, name, description) values (5, '"
                    + UUID.randomUUID().toString() + "', 'd')");
            ps.executeUpdate();
            System.out.println("INSERTED NODE");

            ps = conn.prepareCall("checksum table node");
            rs = ps.executeQuery();
            rs.next();
            System.out.println(rs.getObject(2));

            ps = conn.prepareCall("insert into node (node_type_id, name, description) values (5, '"
                    + UUID.randomUUID().toString() + "', 'd')");
            ps.executeUpdate();
            System.out.println("INSERTED NODE");

            ps = conn.prepareCall("checksum table node");
            rs = ps.executeQuery();
            rs.next();
            System.out.println(rs.getObject(2));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("CONNECTION{");
    }
}

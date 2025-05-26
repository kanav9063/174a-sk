package org.ivc.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DButil {

    // This should match your Oracle ATP Wallet alias (HIGH, LOW, etc.)
    private static final String URL = "jdbc:oracle:thin:@cs174adb_tp";
    private static final String USER = "ADMIN";        // Oracle ATP username
    private static final String PASSWORD = "Helloworld@1234";  // Oracle ATP password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

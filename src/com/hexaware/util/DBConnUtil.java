package com.hexaware.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnUtil {

    public static Connection getConnection(String filename) {
        Properties props = DBPropertyUtil.loadProperties(filename);
        String url = props.getProperty("url");
        String user = props.getProperty("username");
        String pass = props.getProperty("password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, pass);
            
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed:");
            e.printStackTrace();
            return null;
        }
    }
}

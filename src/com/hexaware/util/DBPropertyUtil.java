package com.hexaware.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DBPropertyUtil {

    public static Properties loadProperties(String filename) {
        Properties props = new Properties();
        try (FileInputStream input = new FileInputStream(filename)) {
            props.load(input);
            
        } catch (IOException e) {
            System.out.println("Failed to load properties file: " + filename);
            e.printStackTrace();
        }
        return props;
    }
}

package com.sms.util;

import com.sms.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central place that knows how to open a JDBC connection.
 * Settings come from {@code db.properties}; the environment variables
 * {@code DB_URL}, {@code DB_USER} and {@code DB_PASSWORD} take precedence so that
 * credentials never have to be committed to source control.
 */
public final class DBConnection {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Unable to read db.properties: " + e.getMessage());
        }
    }

    private DBConnection() {
    }

    private static String setting(String envKey, String propKey) {
        String fromEnv = System.getenv(envKey);
        return (fromEnv != null && !fromEnv.isBlank()) ? fromEnv : PROPS.getProperty(propKey);
    }

    /**
     * Opens a new connection. Callers own the connection and must close it
     * (use try-with-resources).
     */
    public static Connection getConnection() {
        String url = setting("DB_URL", "db.url");
        String user = setting("DB_USER", "db.user");
        String password = setting("DB_PASSWORD", "db.password");

        if (url == null || user == null) {
            throw new DatabaseException("Database settings are missing. Check db.properties or DB_* environment variables.");
        }
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException("Could not connect to the database: " + e.getMessage(), e);
        }
    }
}

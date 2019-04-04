package org.kuplkris.accounts.main;

import org.kuplkris.accounts.exception.LoadPropertyFileException;
import org.kuplkris.accounts.exception.PropertyNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SuppressWarnings({"StaticVariableUsedBeforeInitialization", "WeakerAccess", "UtilityClassCanBeEnum"})
public final class PropertiesHandler {

    private static final Properties PROPERTIES = new Properties();

    private static String requestPostParameter;
    private static int applicationPort;
    private static String jdbcDriver;
    private static String databaseUrl;
    private static String databaseUser;
    private static String databasePassword;
    private static int maxPoolResources;
    private static int maxWaitMillis;
    private static int maxAttempts;

    private PropertiesHandler() {
    }

    public static void loadProperties(final InputStream fileInputStream) {
        loadPropertyFile(fileInputStream);

        requestPostParameter = checkAndGetProperty("request.post.parameter");
        applicationPort = Integer.parseInt(checkAndGetProperty("application.port"));
        jdbcDriver = checkAndGetProperty("database.jdbc.driver");
        databaseUrl = checkAndGetProperty("database.url");
        databaseUser = checkAndGetProperty("database.user");
        databasePassword = checkAndGetProperty("database.password");
        maxPoolResources = Integer.parseInt(checkAndGetProperty("database.pool.max.resources"));
        maxWaitMillis = Integer.parseInt(checkAndGetProperty("database.connection.max.wait.millis"));
        maxAttempts = Integer.parseInt(checkAndGetProperty("database.connection.max.attempts"));
    }

    public static String getRequestPostParameter() {
        return requestPostParameter;
    }

    public static int getApplicationPort() {
        return applicationPort;
    }

    public static String getJdbcDriver() {
        return jdbcDriver;
    }

    public static String getDatabaseUrl() {
        return databaseUrl;
    }

    public static String getDatabaseUser() {
        return databaseUser;
    }

    public static String getDatabasePassword() {
        return databasePassword;
    }

    public static int getMaxPoolResources() {
        return maxPoolResources;
    }

    public static int getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public static int getMaxAttempts() {
        return maxAttempts;
    }

    private static void loadPropertyFile(final InputStream fileInputStream) {
        try {
            PROPERTIES.load(fileInputStream);
        } catch (IOException e) {
            throw new LoadPropertyFileException("Can not find PROPERTIES file!", e);
        }
    }


    private static String checkAndGetProperty(final String propertyName) {
        final String property = PROPERTIES.getProperty(propertyName);
        if (property == null) {
            throw new PropertyNotFoundException("Property " + propertyName + " is absent");
        }

        return property;
    }
}

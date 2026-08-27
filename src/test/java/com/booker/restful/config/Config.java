package com.booker.restful.config;

import java.util.Properties;

public final class Config {

    private static final Properties PROPERTIES = load();
    
    public static String baseUrl() { return get("BASE_URL"); }
    public static String auth_login()   { return get("AUTH_LOGIN"); }
    public static String auth_password() { return get("AUTH_PASSWORD"); }

    private static String get(String key) {
        String value = System.getProperty(key);
        if (value == null) value = System.getenv(key);
        if (value == null) value = PROPERTIES.getProperty(key);
        if (value == null) throw new IllegalStateException("Missing configuration variables: " + key);
        return value;
    }

    private static Properties load() {
        var env = System.getProperty("env", "local");
        var properties = new Properties();
        try (var in = Config.class.getResourceAsStream("/config/" + env + ".properties")) {
            if (in != null) properties.load(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
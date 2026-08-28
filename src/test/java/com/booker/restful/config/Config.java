package com.booker.restful.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

public final class Config {

    private static final Properties PROPERTIES = load();
    
    public static String baseUrl() { return get("BASE_URL"); }
    public static String authLogin()   { return get("AUTH_LOGIN"); }
    public static String authPassword() { return get("AUTH_PASSWORD"); }
    public static boolean httpLog() { return Boolean.parseBoolean(get("HTTP_LOG", "false")); }
    public static long maxResponseTimeMs() { return Long.parseLong(get("MAX_RESPONSE_TIME_MS", "5000")); }

    private static String get(String key) {
        String value = get(key, null);
        if (value == null) throw new IllegalStateException("Missing configuration variables: " + key);
        return value;
    }

    private static String get(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) value = System.getenv(key);
        if (value == null) value = PROPERTIES.getProperty(key);
        return value != null ? value : defaultValue;
    }

    private static Properties load() {
        var path = "/config/" + System.getProperty("env", "local") + ".properties";
        var properties = new Properties();

        try (var in = Config.class.getResourceAsStream(path)) {
            if (in != null) properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }
}
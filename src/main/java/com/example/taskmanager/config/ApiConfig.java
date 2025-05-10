package com.example.taskmanager.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApiConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static ApiConfig instance;
    private Properties properties;

    private ApiConfig() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Cannot find " + CONFIG_FILE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ApiConfig getInstance() {
        if (instance == null) {
            instance = new ApiConfig();
        }
        return instance;
    }

    public String getApiBaseUrl() {
        String url = properties.getProperty("api.base.url", "http://localhost/API_Secu");
        System.out.println("API Base URL: " + url);
        return url;
    }

    public String getClientId() {
        return properties.getProperty("oauth.client.id", "task_manager_client");
    }

    public String getClientSecret() {
        return properties.getProperty("oauth.client.secret", "secret");
    }

    public int getConnectTimeout() {
        return Integer.parseInt(properties.getProperty("api.timeout.connect", "5000"));
    }

    public int getReadTimeout() {
        return Integer.parseInt(properties.getProperty("api.timeout.read", "5000"));
    }

    public boolean isDebugMode() {
        return Boolean.parseBoolean(properties.getProperty("debug.mode", "false"));
    }
}
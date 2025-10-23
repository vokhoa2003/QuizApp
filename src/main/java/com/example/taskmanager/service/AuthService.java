package com.example.taskmanager.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import com.example.taskmanager.auth.GoogleLoginHelper;
import com.example.taskmanager.config.ApiConfig;
import com.example.taskmanager.security.EncryptionUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.oauth2.model.Userinfo;

public class AuthService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ApiConfig apiConfig;
    private final Preferences preferences;
    private String userEmail; // Lưu email người dùng sau khi đăng nhập
    
    private String accessToken;
    private String refreshToken;
    private String lastLoginRole; // Thêm để lưu role từ response
    private LocalDateTime expiryTime;
    private String lastLoginResponse; // Thêm để lưu response từ /app_login
    
    private static final String PREF_ACCESS_TOKEN = "access_token";
    private static final String PREF_REFRESH_TOKEN = "refresh_token";
    private static final String PREF_EXPIRY_TIME = "expiry_time";

    public AuthService() {
        this.apiConfig = ApiConfig.getInstance();
        this.objectMapper = new ObjectMapper();
        this.preferences = Preferences.userNodeForPackage(AuthService.class);
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(apiConfig.getConnectTimeout()))
                .build();
                
        loadTokenFromPreferences();
    }
    
    private void loadTokenFromPreferences() {
        String encryptedAccessToken = preferences.get(PREF_ACCESS_TOKEN, null);
        String encryptedRefreshToken = preferences.get(PREF_REFRESH_TOKEN, null);
        String expiryTimeStr = preferences.get(PREF_EXPIRY_TIME, null);

        if (encryptedAccessToken != null && encryptedRefreshToken != null && expiryTimeStr != null) {
            this.accessToken = EncryptionUtil.decrypt(encryptedAccessToken, userEmail);
            this.refreshToken = EncryptionUtil.decrypt(encryptedRefreshToken, userEmail);
            this.expiryTime = LocalDateTime.parse(expiryTimeStr);
        }
    }

    private void saveTokenToPreferences() {
        if (accessToken != null && refreshToken != null && expiryTime != null && userEmail != null) {
            preferences.put(PREF_ACCESS_TOKEN, EncryptionUtil.encrypt(accessToken, userEmail));
            preferences.put(PREF_REFRESH_TOKEN, EncryptionUtil.encrypt(refreshToken, userEmail));
            preferences.put(PREF_EXPIRY_TIME, expiryTime.toString());
        }
    }

    public String getAccessToken() {
        if (accessToken == null || expiryTime == null || LocalDateTime.now().isAfter(expiryTime)) {
            if (refreshToken != null) {
                refreshAccessToken();
            }
        }
        return accessToken;
    }
    
    
    private boolean refreshAccessToken() {
        try {
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                    (apiConfig.getClientId() + ":" + apiConfig.getClientSecret()).getBytes()
            );
            
            Map<String, String> params = new HashMap<>();
            params.put("grant_type", "refresh_token");
            params.put("refresh_token", refreshToken);
            
            String formData = params.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/refresh_token"))
                    .header("Authorization", authHeader)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                processTokenResponse(jsonNode);
                return true;
            } else {
                System.err.println("Token refresh failed: " + response.statusCode() + " - " + response.body());
                this.accessToken = null;
                this.refreshToken = null;
                this.expiryTime = null;
                preferences.remove(PREF_ACCESS_TOKEN);
                preferences.remove(PREF_REFRESH_TOKEN);
                preferences.remove(PREF_EXPIRY_TIME);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void processTokenResponse(JsonNode jsonNode) {
        this.accessToken = jsonNode.get("refresh_token").asText();
        this.refreshToken = jsonNode.get("refresh_token").asText();
        int expiresIn = jsonNode.get("expires_in").asInt(3600);
        this.expiryTime = LocalDateTime.now().plusSeconds(expiresIn - 300);
        saveTokenToPreferences();
    }
    
    public void logout() {
        this.accessToken = null;
        this.refreshToken = null;
        this.expiryTime = null;
        
        preferences.remove(PREF_ACCESS_TOKEN);
        preferences.remove(PREF_REFRESH_TOKEN);
        preferences.remove(PREF_EXPIRY_TIME);
        
        try {
            GoogleLoginHelper.clearStoredTokens();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public boolean isLoggedIn() {
        return (accessToken != null && expiryTime != null && LocalDateTime.now().isBefore(expiryTime));
    }
    public boolean loginWithGoogle(Userinfo userInfo) {
        try {
            if (userInfo == null || userInfo.getEmail() == null || userInfo.getId() == null) {
                System.out.println("Invalid Userinfo: " + (userInfo == null ? "null" : "email or ID missing"));
                return false;
            }

            // Lưu email người dùng để sử dụng cho mã hóa
            this.userEmail = userInfo.getEmail();
            //System.out.println("User email set for encryption: " + userEmail);

            String csrfToken = generateCsrfToken();
            String formData = "GoogleID=" + userInfo.getId() +
                    "&email=" + userEmail +
                    "&FullName=" + (userInfo.getName() != null ? userInfo.getName() :
                    (userInfo.getGivenName() + " " + userInfo.getFamilyName())) +
                    "&access_token=google_" + System.currentTimeMillis() +
                    "&expires_at=" + LocalDateTime.now().plusHours(1).toString() +
                    "&csrf_token=" + csrfToken;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/app_login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", "csrf_token=" + csrfToken)
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();
                    
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            this.lastLoginResponse = response.body();

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                if (jsonNode.has("token") || jsonNode.has("status")) {
                    this.accessToken = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
                    this.refreshToken = this.accessToken;
                    this.expiryTime = LocalDateTime.now().plusHours(1);
                    this.lastLoginRole = extractRoleFromToken(this.accessToken);
                    saveTokenToPreferences();
                    //System.out.println("Google login successful for user: " + userEmail);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Google login failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public String generateCsrfToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    private boolean saveGoogleUserToDatabase(Userinfo userInfo) {
        return true;
    }
    public int getUserIdFromToken(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return -1;
            }
            String payload = new String(Base64.getDecoder().decode(tokenParts[1]));
            JsonNode payloadNode = objectMapper.readTree(payload);
            JsonNode dataNode = payloadNode.get("data");
            return dataNode != null ? dataNode.get("id").asInt(-1) : -1;
        } catch (Exception e) {
            System.err.println("Error extracting user ID from token: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    private String extractRoleFromToken(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return null;
            }
            String payload = new String(Base64.getDecoder().decode(tokenParts[1]));
            JsonNode payloadNode = objectMapper.readTree(payload);
            JsonNode dataNode = payloadNode.get("data");
            return dataNode != null ? dataNode.get("role").asText() : null;
        } catch (Exception e) {
            System.err.println("Error extracting role from token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public String extractEmailFromToken(String token) {
        try {
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return null;
            }
            String payload = new String(Base64.getDecoder().decode(tokenParts[1]));
            JsonNode payloadNode = objectMapper.readTree(payload);
            JsonNode dataNode = payloadNode.get("data");
            return dataNode != null ? dataNode.get("email").asText() : null;
        } catch (Exception e) {
            System.err.println("Error extracting email from token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public String getLastLoginRole() {
        return lastLoginRole;
    }
    public String getLastLoginResponse() {
        return lastLoginResponse;
    }
    public String getUserEmail() {
        // Lấy từ token đã decode hoặc session hiện tại
        // Ví dụ:
        return userEmail; // hoặc decode từ token
    }
}
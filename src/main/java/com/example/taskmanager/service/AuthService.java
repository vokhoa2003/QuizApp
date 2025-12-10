package com.example.taskmanager.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.example.taskmanager.auth.GoogleLoginHelper;
import com.example.taskmanager.config.ApiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.oauth2.model.Userinfo;

public class AuthService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ApiConfig apiConfig;
    private String userEmail;

    private String accessToken;
    private String refreshToken;
    private String lastLoginRole;
    private LocalDateTime expiryTime;
    private String lastLoginResponse;
    private String csrfToken;

    public AuthService() {
        this.apiConfig = ApiConfig.getInstance();
        this.objectMapper = new ObjectMapper();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(apiConfig.getConnectTimeout()))
                .build();
    }

    public String getAccessToken() {
        if (accessToken == null) {
            if (refreshToken != null) {
                refreshAccessToken();
            }
        }
        return accessToken;
    }

    public boolean refreshAccessToken() {
        try {
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                    (apiConfig.getClientId() + ":" + apiConfig.getClientSecret()).getBytes());

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
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void processTokenResponse(JsonNode json) throws IOException {
        if (json == null) {
            throw new IOException("Empty token response from auth server");
        }

        String accessToken = json.path("access_token").asText(null);
        String refreshToken = json.path("refresh_token").asText(null);
        Integer expiresIn = json.path("expires_in").isNumber() ? json.path("expires_in").intValue() : null;

        if (accessToken == null || accessToken.isEmpty()) {
            String err = json.path("error").asText(null);
            String errDesc = json.path("error_description").asText(null);
            throw new IOException("Invalid token response: access_token missing. error=" + err
                    + ", description=" + errDesc);
        }

        this.accessToken = accessToken;
        if (refreshToken != null && !refreshToken.isEmpty()) {
            this.refreshToken = refreshToken;
        }
        if (expiresIn != null) {
            this.expiryTime = LocalDateTime.now().plusSeconds(expiresIn);
        }
    }

    public void logout() {
        this.accessToken = null;
        this.refreshToken = null;
        this.expiryTime = null;
        this.userEmail = null;
        this.lastLoginRole = null;
        this.lastLoginResponse = null;
        this.csrfToken = null;

        try {
            GoogleLoginHelper.clearStoredTokens();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLoggedIn() {
        return (accessToken != null);
    }

    /**
     * ✅ SỬA: Thêm parameter realGoogleAccessToken
     * Login với Google bằng cách gửi REAL access token đến backend
     */
    public boolean loginWithGoogle(Userinfo userInfo, String realGoogleAccessToken) {
        try {
            if (userInfo == null || userInfo.getEmail() == null || userInfo.getId() == null) {
                System.out.println("Invalid Userinfo: " + (userInfo == null ? "null" : "email or ID missing"));
                return false;
            }

            if (realGoogleAccessToken == null || realGoogleAccessToken.isEmpty()) {
                System.err.println("❌ Real Google access token is null or empty!");
                return false;
            }

            this.userEmail = userInfo.getEmail();
            this.csrfToken = generateCsrfToken();

            String googleId = URLEncoder.encode(userInfo.getId(), StandardCharsets.UTF_8);
            String emailEnc = URLEncoder.encode(userEmail, StandardCharsets.UTF_8);
            String fullName = URLEncoder.encode(
                    (userInfo.getName() != null ? userInfo.getName()
                            : (userInfo.getGivenName() + " " + userInfo.getFamilyName())),
                    StandardCharsets.UTF_8);
            
            //GỬI TOKEN THẬT TỪ GOOGLE
            String accessTokenEnc = URLEncoder.encode(realGoogleAccessToken, StandardCharsets.UTF_8);
            
            String expiresAt = URLEncoder.encode(LocalDateTime.now().plusHours(1).toString(), StandardCharsets.UTF_8);
            String csrfEnc = URLEncoder.encode(this.csrfToken, StandardCharsets.UTF_8);

            String formData = "GoogleID=" + googleId +
                    "&email=" + emailEnc +
                    "&FullName=" + fullName +
                    "&access_token=" + accessTokenEnc +  // ← TOKEN THẬT
                    "&expires_at=" + expiresAt +
                    "&csrf_token=" + csrfEnc;

            System.out.println("Sending REAL Google token to backend");
            //System.out.println("Token (first 30 chars): " + realGoogleAccessToken.substring(0, Math.min(30, realGoogleAccessToken.length())));
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/app_login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Cookie", "csrf_token=" + this.csrfToken)
                    .header("X-CSRF-Token", this.csrfToken)
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            this.lastLoginResponse = response.body();
            //System.out.println("Login response: " + response.body());
            
            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                if (jsonNode.has("token") || jsonNode.has("status")) {
                    this.accessToken = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
                    // this.refreshToken = this.accessToken;
                    // this.expiryTime = LocalDateTime.now().plusHours(1);
                    this.lastLoginRole = extractRoleFromToken(this.accessToken);
                    System.out.println("Login thành công với backend JWT");
                    return true;
                }
            } else {
                System.err.println("Backend returned: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Google login failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public String getCsrfToken() {
        return this.csrfToken;
    }

    public String generateCsrfToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String t = Base64.getEncoder().encodeToString(randomBytes);
        this.csrfToken = t;
        return t;
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
        return userEmail;
    }
}
package com.example.taskmanager.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.example.taskmanager.config.ApiConfig;

/**
 * Helper class để xử lý Google OAuth 2.0 login
 */
public class GoogleLoginHelper {
    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");
    private static final String TOKENS_DIRECTORY_PATH = System.getProperty("user.home") + 
            File.separator + ".quiz-app" + File.separator + "tokens";
    private static final ApiConfig apiConfig = ApiConfig.getInstance();

    /**
     * Wrapper class để return cả Userinfo và access token
     */
    public static class LoginResult {
        private final Userinfo userinfo;
        private final String accessToken;
        private final String refreshToken;

        public LoginResult(Userinfo userinfo, String accessToken, String refreshToken) {
            this.userinfo = userinfo;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public Userinfo getUserinfo() {
            return userinfo;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }

    /**
     * Method CŨ - Giữ lại để backward compatible
     * @deprecated Sử dụng loginWithToken() để lấy cả access token
     * @return Chỉ trả về Userinfo (không có token)
     */
    @Deprecated
    public static Userinfo login() throws Exception {
        System.out.println("⚠️  Calling deprecated login() method. Consider using loginWithToken() instead.");
        LoginResult result = loginWithToken();
        return result.getUserinfo();
    }

    /**
     * Method MỚI - Thực hiện Google OAuth 2.0 login flow
     * @return LoginResult chứa userinfo và access token THẬT (không mã hóa)
     */
    public static LoginResult loginWithToken() throws Exception {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        File tokensDirectory = new File(TOKENS_DIRECTORY_PATH);
        if (!tokensDirectory.exists()) {
            tokensDirectory.mkdirs();
        }

        String credentialsJson = String.format("""
                {
                  "installed": {
                    "client_id": "%s",
                    "client_secret": "%s",
                    "redirect_uris": ["http://localhost"],
                    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                    "token_uri": "https://oauth2.googleapis.com/token"
                  }
                }
                """, apiConfig.getClientId(), apiConfig.getClientSecret());

        ByteArrayInputStream in = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, 
                new InputStreamReader(in, StandardCharsets.UTF_8));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(0).build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver) {
            @Override
            protected void onAuthorization(com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl authorizationUrl) 
                    throws IOException {
                authorizationUrl.set("prompt", "select_account");
                customBrowse(authorizationUrl.build());
            }

            protected boolean customBrowse(String url) throws IOException {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    return true;
                } catch (Exception e) {
                    throw new IOException("Failed to open browser: " + e.getMessage(), e);
                }
            }
        }.authorize("user");

        if (credential == null) {
            throw new Exception("Credential is null after authorization");
        }

        Oauth2 oauth2 = new Oauth2.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Task Manager App")
                .build();

        Userinfo userinfo = oauth2.userinfo().get().execute();
        
        // ✅ LẤY TOKEN THẬT - KHÔNG MÃ HÓA
        String realAccessToken = credential.getAccessToken();
        String realRefreshToken = credential.getRefreshToken();
        
        System.out.println("✅ Lấy token thật từ Google thành công");
        // System.out.println("Access Token (first 20 chars): " + 
        //         (realAccessToken != null ? realAccessToken.substring(0, Math.min(20, realAccessToken.length())) : "null"));
        
        // ✅ RETURN CẢ USERINFO VÀ TOKEN THẬT
        return new LoginResult(userinfo, realAccessToken, realRefreshToken);
    }

    /**
     * Xóa stored tokens (vẫn giữ để logout)
     */
    public static void clearStoredTokens() throws Exception {
        File tokensDirectory = new File(TOKENS_DIRECTORY_PATH);
        if (tokensDirectory.exists() && tokensDirectory.isDirectory()) {
            File[] tokenFiles = tokensDirectory.listFiles();
            if (tokenFiles != null) {
                for (File tokenFile : tokenFiles) {
                    Files.deleteIfExists(tokenFile.toPath());
                }
            }
            Files.deleteIfExists(tokensDirectory.toPath());
        }
        System.out.println("Stored tokens cleared.");
    }
}
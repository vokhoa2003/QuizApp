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
import com.example.taskmanager.security.EncryptionUtil;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;

public class GoogleLoginHelper {
    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final ApiConfig apiConfig = ApiConfig.getInstance();

    public static Userinfo login() throws Exception {
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
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in, StandardCharsets.UTF_8));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(0).build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver) {
            @Override
            protected void onAuthorization(com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
                authorizationUrl.set("prompt", "select_account");
                // Gọi phương thức browse tùy chỉnh thay vì super.onAuthorization
                customBrowse(authorizationUrl.build());
            }

            // Không gắn @Override vì có thể không tồn tại trong phiên bản cũ
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
        // Mã hóa access token với email người dùng
        String encryptedAccessToken = EncryptionUtil.encrypt(credential.getAccessToken(), userinfo.getEmail());
        credential.setAccessToken(encryptedAccessToken);
        
        String encryptedRefreshToken = credential.getRefreshToken() != null 
        ? EncryptionUtil.encrypt(credential.getRefreshToken(), userinfo.getEmail()) 
        : null;
        if (encryptedRefreshToken != null) {
            credential.setRefreshToken(encryptedRefreshToken);
        }
        // Lưu credential vào file sau khi mã hóa
//        try {
//            DataStore<com.google.api.client.auth.oauth2.StoredCredential> dataStore = 
//                flow.getDataStoreFactory().getDataStore("StoredCredential");
//            dataStore.set("user", new com.google.api.client.auth.oauth2.StoredCredential(credential));
//            System.out.println("Credential saved to tokens directory");
//            
//            // Kiểm tra lại nội dung file
//            com.google.api.client.auth.oauth2.StoredCredential storedCredential = dataStore.get("user");
//            if (storedCredential != null) {
//                System.out.println("Stored Access Token: " + storedCredential.getAccessToken());
//                System.out.println("Stored Refresh Token: " + storedCredential.getRefreshToken());
//                String decryptedAccessToken = EncryptionUtil.decrypt(storedCredential.getAccessToken(), userinfo.getEmail());
//                System.out.println("Decrypted Stored Access Token: " + decryptedAccessToken);
//            } else {
//                System.out.println("No credential found in tokens directory");
//            }
//        } catch (Exception e) {
//            System.out.println("Error saving credential: " + e.getMessage());
//            e.printStackTrace();
//        }
try {
    FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH));
    DataStore<StoredCredential> dataStore = dataStoreFactory.getDataStore("StoredCredential");
    if (dataStore == null) {
        System.out.println("DataStore is null. Check if tokens directory is accessible.");
        throw new Exception("Failed to initialize DataStore");
    }
    StoredCredential storedCredential = new StoredCredential(credential);
    dataStore.set("user", storedCredential);
    //System.out.println("Credential saved successfully to tokens directory");

    // Kiểm tra lại nội dung
    StoredCredential retrievedCredential = dataStore.get("user");
    if (retrievedCredential != null) {
        //System.out.println("Stored Access Token: " + retrievedCredential.getAccessToken());
        //System.out.println("Stored Refresh Token: " + retrievedCredential.getRefreshToken());
        String decryptedAccessToken = EncryptionUtil.decrypt(retrievedCredential.getAccessToken(), userinfo.getEmail());
        //System.out.println("Decrypted Stored Access Token: " + decryptedAccessToken);
    } else {
        //System.out.println("No credential found in DataStore");
    }
} catch (Exception e) {
    System.out.println("Error saving or retrieving credential: " + e.getMessage());
    e.printStackTrace();
    throw e;
}
        return userinfo;
    }

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
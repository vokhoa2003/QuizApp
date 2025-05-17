package com.example.taskmanager.service;

import com.example.taskmanager.config.ApiConfig;
import com.example.taskmanager.model.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ApiService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ApiConfig apiConfig;
    private final AuthService authService;

    public ApiService(AuthService authService) {
        this.authService = authService;
        this.apiConfig = ApiConfig.getInstance();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, 
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, 
            new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        this.objectMapper.registerModule(javaTimeModule);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(apiConfig.getConnectTimeout()))
                .build();
    }

//    public List<Task> getUsers() {
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/get"))
//                    .header("Authorization", "Bearer " + authService.getAccessToken())
//                    .header("Content-Type", "application/json")
//                    .GET()
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//            if (response.statusCode() == 200) {
//                return objectMapper.readValue(
//                        response.body(),
//                        new TypeReference<List<Task>>() {});
//            } else {
//                System.err.println("Error fetching users: " + response.statusCode() + " - " + response.body());
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        return Collections.emptyList();
//    }

//    public List<Task> getUsers() {
//    try {
//        String uri = apiConfig.getApiBaseUrl() + "/get";
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(uri))
//                .header("Authorization", "Bearer " + authService.getAccessToken())
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .GET()
//                .build();
//
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//        if (response.statusCode() == 200) {
//            return objectMapper.readValue(
//                    response.body(),
//                    new TypeReference<List<Task>>() {});
//        } else {
//            System.err.println("Error fetching users: " + response.statusCode() + " - " + response.body());
//        }
//    } catch (IOException | InterruptedException e) {
//        e.printStackTrace();
//    }
//    return Collections.emptyList();
//}
//    public Task getUsers() {
//    try {
//        String uri = apiConfig.getApiBaseUrl() + "/get";
//        System.out.println("Calling API: " + uri);
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(uri))
//                .header("Authorization", "Bearer " + authService.getAccessToken())
//                .GET()
//                .build();
//
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//        System.out.println("API Response: " + response.statusCode() + " - " + response.body());
//
//        if (response.statusCode() == 200) {
//            JsonNode rootNode = objectMapper.readTree(response.body());
//            if (rootNode.has("error")) {
//                System.err.println("API Error: " + rootNode.get("error").asText());
//                return null;
//            }
//            try {
//                Task task = objectMapper.readValue(response.body(), Task.class);
//                System.out.println("Task object: " + (task != null ? task.getEmail() : "null"));
//                return task;
//            } catch (JsonProcessingException e) {
//                System.err.println("Error parsing JSON: " + e.getMessage());
//                e.printStackTrace();
//                return null;
//            }
//        } else {
//            System.err.println("Error fetching users: " + response.statusCode() + " - " + response.body());
//        }
//    } catch (IOException | InterruptedException e) {
//        e.printStackTrace();
//    }
//    return null;
//}
    public List<Task> getUsers() {
        try {
            String uri = apiConfig.getApiBaseUrl() + "/get";
            System.out.println("Calling API: " + uri);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    
                    .GET()
                    .build();
            //System.out.println("bla bla bla:"+authService.getAccessToken());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("API Response: " + response.statusCode() + " - " + response.body());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                if (rootNode.has("error")) {
                    System.err.println("API Error: " + rootNode.get("error").asText());
                    return null;
                }
                try {
                    // Parse JSON array into List<Task>
                    List<Task> users = objectMapper.readValue(response.body(), 
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
                    System.out.println("Users fetched: " + (users != null ? users.size() : 0) + " users");
                    return users;
                } catch (JsonProcessingException e) {
                    System.err.println("Error parsing JSON: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            } else {
                System.err.println("Error fetching users: " + response.statusCode() + " - " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Task createUser(Task user) {
        try {
            String requestBody = objectMapper.writeValueAsString(user);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/add"))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Task.class);
            } else {
                System.err.println("Error creating user: " + response.statusCode() + " - " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public Task updateUser(Task user) {
//        try {
//            String requestBody = objectMapper.writeValueAsString(user);
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/AdminUpdate"))
//                    .header("Authorization", "Bearer " + authService.getAccessToken())
//                    .header("Content-Type", "application/json")
//                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            System.out.println("API Response: " + response.statusCode() + " - " + response.body());
//
//            if (response.statusCode() == 200) {
//                return objectMapper.readValue(response.body(), Task.class);
//            } else {
//                System.err.println("Error updating user: " + response.statusCode() + " - " + response.body());
//            }
//            System.out.println("bai bai bai: "+requestBody);
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    public List<Task> updateUser(Task user) {
        try {
            String requestBody = objectMapper.writeValueAsString(user);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/AdminUpdate"))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("API Response: " + response.statusCode() + " - " + response.body());
            System.out.println("Request body: " + requestBody);

            if (response.statusCode() == 200) {
                try {
                    List<Task> updatedUsers = objectMapper.readValue(response.body(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
                    System.out.println("Users updated: " + (updatedUsers != null ? updatedUsers.size() : 0) + " users");
                    return updatedUsers;
                } catch (JsonProcessingException e) {
                    System.err.println("Error parsing JSON to List<Task>: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            } else {
                System.err.println("Error updating user: " + response.statusCode() + " - " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
//    public boolean UpdateUser (Task user) {
//        try {
//            System.out.println("Attempting Google login for user: " + (user != null ? userInfo.getEmail() : "null"));
//            if (user == null || user.getEmail() == null || user.getId() == null) {
//                System.out.println("Invalid Userinfo: " + (user == null ? "null" : "email or ID missing"));
//                return false;
//            }
//
//            // Tạo CSRF token
//            String csrfToken = authService.generateCsrfToken();
//            System.out.println("Generated CSRF Token: " + csrfToken);
//
//            String formData = "GoogleID=" + user.getId() +
//                    "&email=" + user.getEmail() +
//                    "&FullName=" + (user.getName() != null ? user.getName() : 
//                        (user.getGivenName() + " " + user.getFamilyName())) +
//                    "&access_token=google_" + System.currentTimeMillis() +
//                    "&expires_at=" + LocalDateTime.now().plusHours(1).toString() +
//                    "&csrf_token=" + csrfToken;
//
//            String fullUrl = apiConfig.getApiBaseUrl() + "/login";
//            System.out.println("Sending POST request to: " + fullUrl);
//            System.out.println("Request body: " + formData);
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(fullUrl))
//                    .header("Content-Type", "application/x-www-form-urlencoded")
//                    .header("Cookie", "csrf_token=" + csrfToken) // Gửi CSRF token trong cookie
//                    .POST(HttpRequest.BodyPublishers.ofString(formData))
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            System.out.println("Backend login response: " + response.body());
//
//            if (response.statusCode() == 200) {
//                JsonNode jsonNode = objectMapper.readTree(response.body());
//                String data=jsonNode.get("status").asText();
//                if (jsonNode.has("status") && "success".equals(data)) {
////                    this.accessToken = jsonNode.has("token") ? jsonNode.get("token").asText() : null;
////                    this.refreshToken = this.accessToken;
////                    this.expiryTime = LocalDateTime.now().plusHours(1);
//                    // Giải mã token để lấy role
////                    this.lastLoginRole = extractRoleFromToken(this.accessToken);
////                    saveTokenToPreferences();
//                    System.out.println("Google login successful for user: " + user.getEmail());
//                    return true;
//                } else {
//                    System.err.println("No token in response: " + response.body());
//                }
//            } else {
//                System.err.println("Backend login failed: " + response.statusCode() + " - " + response.body());
//            }
//        } catch (Exception e) {
//            System.err.println("Google login failed: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return false;
//    }
    public boolean deleteUser(Long userId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/delete"))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
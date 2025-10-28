package com.example.taskmanager.service;

import com.example.taskmanager.config.ApiConfig;
import com.example.taskmanager.model.ClassRoom;
import com.example.taskmanager.model.Student;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Teacher;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ApiConfig apiConfig;
    private final AuthService authService;
    private String csrfToken; // Lưu CSRF token

    public ApiService(AuthService authService) {
        this.authService = authService;
        this.apiConfig = ApiConfig.getInstance();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // ✅ Thêm nhiều pattern để xử lý date không đầy đủ
        javaTimeModule.addDeserializer(LocalDateTime.class, 
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, 
            new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        
        this.objectMapper.registerModule(javaTimeModule);
        // ✅ Cho phép parse date rỗng/null thành null thay vì lỗi
        this.objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(apiConfig.getConnectTimeout()))
                .build();
        this.csrfToken = authService.getCsrfToken();
        if (this.csrfToken == null) {
            this.csrfToken = authService.generateCsrfToken();
        }
    }    

    public List<Task> getUsers() {
        try {
            String uri = apiConfig.getApiBaseUrl() + "/get";
            Map<String, Object> data = new HashMap<>();
            data.put("csrf_token", csrfToken);
            data.put("scope", "all");
            String requestBody = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("Content-Type", "application/json")
                    .header("Cookie", "csrf_token=" + csrfToken)
                    .header("X-CSRF-Token", csrfToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            System.out.println("API Base URL: " + apiConfig.getApiBaseUrl());
            System.out.println("Request body: " + requestBody);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("API Response: " + response.statusCode());
            System.out.println("Response body RAW: " + response.body()); // ✅ THÊM dòng này

            if (response.statusCode() == 200) {
                String responseBody = response.body().trim();
                
                // ✅ Kiểm tra response có phải JSON hợp lệ không
                if (!responseBody.startsWith("[") && !responseBody.startsWith("{")) {
                    System.err.println("⚠️ Invalid JSON - response starts with: " + responseBody.substring(0, Math.min(200, responseBody.length())));
                    return Collections.emptyList();
                }

                JsonNode rootNode = objectMapper.readTree(responseBody);
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

    public boolean createUser(Task user) {
        try {
            String uri = apiConfig.getApiBaseUrl() + "/add";
            
            // ✅ Gửi FLAT JSON (không wrap trong structure phức tạp)
            Map<String, Object> data = new HashMap<>();
            data.put("email", user.getEmail());
            data.put("FullName", user.getFullName());
            data.put("role", user.getRole());
            data.put("Status", user.getStatus());
            data.put("Phone", user.getPhone());
            data.put("Address", user.getAddress());
            
            // ✅ Format BirthDate đúng định dạng yyyy-MM-dd
            if (user.getBirthDate() != null) {
                data.put("BirthDate", user.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            
            data.put("IdentityNumber", user.getIdentityNumber());
            data.put("CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            data.put("UpdateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            data.put("csrf_token", csrfToken);

            // ✅ KHÔNG wrap trong {action, method, table, data}
            String requestBody = objectMapper.writeValueAsString(data);
            System.out.println("📤 Create user request: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("Content-Type", "application/json")
                    .header("Cookie", "csrf_token=" + csrfToken)
                    .header("X-CSRF-Token", csrfToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("API Response: " + response.statusCode());
            System.out.println("📥 Create user response: " + response.body());
            
            String responseBody = response.body().trim();
            if (!responseBody.startsWith("{") && !responseBody.startsWith("[")) {
                System.err.println("⚠️ Invalid JSON response: " + responseBody.substring(0, Math.min(500, responseBody.length())));
                return false;
            }

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(responseBody);
                if (rootNode.has("status") && "success".equals(rootNode.get("status").asText())) {
                    System.out.println("✅ THÊM THÀNH CÔNG");
                    return true;
                } else {
                    System.err.println("❌ Error: " + (rootNode.has("message") ? rootNode.get("message").asText() : "Unknown error"));
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Exception in createUser: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUser(Task user) {
        try {
            // Chuyển đổi Task thành Map và thêm csrf_token
            Map<String, Object> data = objectMapper.convertValue(user, new TypeReference<Map<String, Object>>() {});
            data.put("csrf_token", csrfToken);
            // Đảm bảo CreateDate và updateDate là chuỗi
            if (user.getCreateDate() != null) {
                data.put("createDate", user.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if (user.getUpdateDate() != null) {
                data.put("updateDate", user.getUpdateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if (user.getBirthDate() != null) {
                data.put("birthDate", user.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            String requestBody = objectMapper.writeValueAsString(data);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/AdminUpdate"))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("Content-Type", "application/json")
                    .header("Cookie", "csrf_token=" + csrfToken)
                    .header("X-CSRF-Token", csrfToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("API Response: " + response.statusCode());
            //System.out.println("Request body: " + requestBody);

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                if (jsonNode.has("status") && "success".equals(jsonNode.get("status").asText())) {
                    System.out.println("CẬP NHẬT THÀNH CÔNG");
                    return true;
                } else {
                    System.err.println("Error: " + (jsonNode.has("message") ? jsonNode.get("message").asText() : "Unknown error"));
                    return false;
                }
            } else {
                System.err.println("Error creating user: " + response.statusCode() + " - " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean deleteUser(Long userId) {
        try {
            // Tạo Map chứa dữ liệu, bao gồm id và csrf_token
            Map<String, Object> data = new HashMap<>();
            data.put("id", userId); // Gửi id qua body JSON
            data.put("csrf_token", csrfToken);
            String requestBody = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + "/delete"))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("Content-Type", "application/json")
                    .header("Cookie", "csrf_token=" + csrfToken)
                    .header("X-CSRF-Token", csrfToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("API Response: " + response.statusCode());
            //System.out.println("Request body: " + requestBody);

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                if (jsonNode.has("message") && "Xóa thành công".equals(jsonNode.get("message").asText())) {
                    System.out.println("XÓA THÀNH CÔNG");
                    return true;
                } else {
                    System.err.println("Error: " + (jsonNode.has("message") ? jsonNode.get("message").asText() : "Unknown error"));
                    return false;
                }
            } else {
                System.err.println("Error deleting user: " + response.statusCode() + " - " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
    //-------------------------------------------------------------------------------------------------------
    public <T> List<T> postDataAndGetList(String endpoint, Map<String, Object> data, Class<T> clazz) {

        try {
            // Thêm CSRF token nếu có
            data.put("csrf_token", csrfToken);
            String requestBody = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + endpoint))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("Content-Type", "application/json")
                    .header("Cookie", "csrf_token=" + csrfToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("API Response: " + response.statusCode());

            if (response.statusCode() == 200) {
                // Parse JSON array into List<T>
                return objectMapper.readValue(response.body(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            } else {
                System.err.println("Error: " + response.statusCode() + " - " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    // Thêm hàm này:
    public List<Map<String, Object>> postApiGetList(
            String endpoint,
            Map<String, Object> data
    ) {
        try {
            // Thêm CSRF token vào data nếu chưa có
            data.put("csrf_token", csrfToken);
            String requestBody = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiConfig.getApiBaseUrl() + endpoint))
                    .header("Authorization", "Bearer " + authService.getAccessToken())
                    .header("Content-Type", "application/json")
                    .header("Cookie", "csrf_token=" + csrfToken)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("API Response: " + response.statusCode());
            System.out.println("Request body: " + requestBody);

            // if (response.statusCode() == 200) {
            //     // Parse JSON array to List<Map<String, Object>>
            //     return objectMapper.readValue(response.body(),
            //             new TypeReference<List<Map<String, Object>>>() {});
            // } else {
            //     System.err.println("API error: " + response.statusCode() + " - " + response.body());
            // }
            if (response.statusCode() == 200) {
            // Parse response to Map, then get "data" field
                Map<String, Object> respMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                Object dataObj = respMap.get("data");
            if (dataObj instanceof List) {
                return objectMapper.convertValue(dataObj, new TypeReference<List<Map<String, Object>>>() {});
            }
            } else {
                System.err.println("API error: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    // ========================================
// 1. LẤY DANH SÁCH
// ========================================

public List<ClassRoom> getClasses() {
    Map<String, Object> data = new HashMap<>();
    data.put("table", "classes");
    return postDataAndGetList("/get", data, ClassRoom.class);
}

public List<Teacher> getTeachers() {
    Map<String, Object> data = new HashMap<>();
    data.put("table", "teacher");
    return postDataAndGetList("/get", data, Teacher.class);
}

public List<Student> getStudents() {
    Map<String, Object> data = new HashMap<>();
    data.put("table", "student");
    return postDataAndGetList("/get", data, Student.class);
}

// ========================================
// 2. THÊM MỚI
// ========================================

public boolean createClass(ClassRoom classRoom) {
    Map<String, Object> data = objectMapper.convertValue(classRoom, Map.class);
    data.put("table", "classes");
    data.put("action", "insert");
    return postAndCheckSuccess("/add", data);
}

public boolean createTeacher(Teacher teacher) {
    Map<String, Object> data = objectMapper.convertValue(teacher, Map.class);
    data.put("table", "teacher");
    data.put("action", "insert");
    return postAndCheckSuccess("/add", data);
}

public boolean createStudent(Student student) {
    Map<String, Object> data = objectMapper.convertValue(student, Map.class);
    data.put("table", "student");
    data.put("action", "insert");
    return postAndCheckSuccess("/add", data);
}

// ========================================
// 3. CẬP NHẬT
// ========================================

public boolean updateClass(ClassRoom classRoom) {
    Map<String, Object> data = objectMapper.convertValue(classRoom, Map.class);
    data.put("table", "classes");
    data.put("action", "update");
    return postAndCheckSuccess("/AdminUpdate", data);
}

public boolean updateTeacher(Teacher teacher) {
    Map<String, Object> data = objectMapper.convertValue(teacher, Map.class);
    data.put("table", "teacher");
    data.put("action", "update");
    return postAndCheckSuccess("/AdminUpdate", data);
}

public boolean updateStudent(Student student) {
    Map<String, Object> data = objectMapper.convertValue(student, Map.class);
    data.put("table", "student");
    data.put("action", "update");
    return postAndCheckSuccess("/AdminUpdate", data);
}

// ========================================
// 4. XÓA
// ========================================

public boolean deleteClass(Long id) {
    Map<String, Object> data = new HashMap<>();
    data.put("table", "classes");
    data.put("id", id);
    return postAndCheckSuccess("/delete", data);
}

public boolean deleteTeacher(Long id) {
    Map<String, Object> data = new HashMap<>();
    data.put("table", "teacher");
    data.put("id", id);
    return postAndCheckSuccess("/delete", data);
}

public boolean deleteStudent(Long id) {
    Map<String, Object> data = new HashMap<>();
    data.put("table", "student");
    data.put("id", id);
    return postAndCheckSuccess("/delete", data);
}

// ========================================
// 5. HÀM HỖ TRỢ: GỬI POST + KIỂM TRA SUCCESS
// ========================================

private boolean postAndCheckSuccess(String endpoint, Map<String, Object> data) {
    try {
        data.put("csrf_token", csrfToken);
        String requestBody = objectMapper.writeValueAsString(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiConfig.getApiBaseUrl() + endpoint))
                .header("Authorization", "Bearer " + authService.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Cookie", "csrf_token=" + csrfToken)
                .header("X-CSRF-Token", csrfToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("API [" + endpoint + "] Response: " + response.statusCode());
        System.out.println("Request: " + requestBody);
        System.out.println("Response: " + response.body());

        if (response.statusCode() == 200) {
            JsonNode json = objectMapper.readTree(response.body());
            return json.has("status") && "success".equals(json.get("status").asText());
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}
}
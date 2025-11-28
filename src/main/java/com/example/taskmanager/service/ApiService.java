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
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import com.google.api.services.oauth2.model.Userinfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiConfig apiConfig;
    private final AuthService authService;
    private String csrfToken; // Lưu CSRF token

    public ApiService(AuthService authService) {
        this.authService = authService;
        this.apiConfig = ApiConfig.getInstance();
        
        // Allow case-insensitive mapping so JSON keys like "Id" / "Name" map to id/name
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // ✅ Register JavaTimeModule với cả Serializer và Deserializer
    JavaTimeModule javaTimeModule = new JavaTimeModule();
    
    // Deserializer (JSON → Java) - đã có sẵn
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dtf));
    
    // ✅ Serializer (Java → JSON) - THÊM DÒNG NÀY
    javaTimeModule.addSerializer(LocalDateTime.class, 
        new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer(dtf));

    objectMapper.registerModule(javaTimeModule);

    objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    

        // keep existing date acceptance setting
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
        Map<String, Object> data = new HashMap<>();
        data.remove("updateDate");
        data.remove("UpdateDate");
        data.remove("createDate");
        data.remove("CreateDate");
        
        // ✅ QUAN TRỌNG: KHÔNG gửi "id" trong body
        // Backend dùng id làm WHERE condition, không phải data update
        
        // ✅ CHỈ GỬI CÁC FIELD CẦN UPDATE (KHÔNG BAO GỒM id)
        data.put("emailUpdate", user.getEmail());
        data.put("FullName", user.getFullName());
        data.put("roleUpdate", user.getRole());
        data.put("Status", user.getStatus());
        data.put("Phone", user.getPhone());
        data.put("Address", user.getAddress());
        data.put("IdentityNumber", user.getIdentityNumber());
        
        // ✅ Format BirthDate
        if (user.getBirthDate() != null) {
            data.put("BirthDate", user.getBirthDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        
        // // ✅ UpdateDate
        // data.put("UpdateDate", LocalDateTime.now()
        //     .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // ✅ Metadata cho backend
        data.put("table", "account");  // Backend cần biết update table nào
        data.put("action", "AdminUpdate");
        data.put("csrf_token", csrfToken);
        
        // ✅ GỬI Id RIÊNG BIỆT - Backend dùng làm WHERE condition
        data.put("id", user.getId());  // Backend sẽ filter ra khỏi $data, dùng cho WHERE
        
        String requestBody = objectMapper.writeValueAsString(data);
        
        System.out.println("📤 UPDATE User Request: " + requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiConfig.getApiBaseUrl() + "/AdminUpdate"))
                .header("Authorization", "Bearer " + authService.getAccessToken())
                .header("Content-Type", "application/json")
                .header("Cookie", "csrf_token=" + csrfToken)
                .header("X-CSRF-Token", csrfToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, 
                    java.nio.charset.StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        
        System.out.println("📥 UPDATE User Response (" + response.statusCode() + "): " 
            + response.body());

        if (response.statusCode() == 200) {
            JsonNode jsonNode = objectMapper.readTree(response.body());
            if (jsonNode.has("status") && "success".equals(jsonNode.get("status").asText())) {
                System.out.println("✅ CẬP NHẬT USER THÀNH CÔNG");
                return true;
            } else {
                System.err.println("❌ Error: " + (jsonNode.has("message") 
                    ? jsonNode.get("message").asText() : "Unknown error"));
                return false;
            }
        } else {
            System.err.println("❌ HTTP Error: " + response.statusCode() 
                + " - " + response.body());
        }
    } catch (IOException | InterruptedException e) {
        System.err.println("❌ Exception in updateUser: " + e.getMessage());
        e.printStackTrace();
    }
    return false;
}
    public boolean deleteUser(Long userId) {
        try {
            // Tạo Map chứa dữ liệu, bao gồm id và csrf_token
            Map<String, Object> data = new HashMap<>();
            data.put("table", "account");
            data.put("Id", userId); // Gửi id qua body JSON
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
            System.out.println("Request body: " + requestBody);
            System.out.println("Response body: " + response.body());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                if (jsonNode.has("message") && "Xóa thành công".equals(jsonNode.get("message").asText())) {
                    System.out.println("XÓA THÀNH CÔNG");
                    return true;
                } else if (jsonNode.has("message") && "Xóa thất bại".equals(jsonNode.get("message").asText())) {
                    //.out.println("requestBody: " + requestBody);
                    System.out.println("XÓA THẤT BẠI");
                    return false;
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
            System.out.println("📤 Request to " + endpoint + ": " + requestBody);
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
    try {
        // ✅ KHÔNG dùng objectMapper.convertValue vì nó convert tất cả thành String
        Map<String, Object> data = new HashMap<>();
        
        // ✅ GỬI ĐÚNG TYPE: Long cho Id, String cho Name/Description
        data.put("Id", classRoom.getId());  // Long → JSON number
        data.put("Name", classRoom.getName());
        data.put("Description", classRoom.getDescription());
        
        // ✅ Format UpdateDate
        if (classRoom.getUpdateDate() != null) {
            data.put("UpdateDate", classRoom.getUpdateDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            data.put("UpdateDate", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        data.put("table", "classes");
        data.put("action", "AdminUpdate");
        data.put("csrf_token", csrfToken);
        
        String requestBody = objectMapper.writeValueAsString(data);
        
        System.out.println("📤 UPDATE Request: " + requestBody);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiConfig.getApiBaseUrl() + "/AdminUpdate"))
                .header("Authorization", "Bearer " + authService.getAccessToken())
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Cookie", "csrf_token=" + csrfToken)
                .header("X-CSRF-Token", csrfToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, java.nio.charset.StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        
        System.out.println("📥 UPDATE Response (" + response.statusCode() + "): " + response.body());

        if (response.statusCode() == 200) {
            JsonNode json = objectMapper.readTree(response.body());
            
            if (json.has("status") && "success".equals(json.get("status").asText())) {
                System.out.println("✅ CẬP NHẬT THÀNH CÔNG");
                return true;
            } else if (json.has("message")) {
                System.err.println("❌ Lỗi từ API: " + json.get("message").asText());
            }
        }
    } catch (Exception e) {
        System.err.println("❌ Exception: " + e.getMessage());
        e.printStackTrace();
    }
    return false;
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
    data.put("Id", id);
    return postAndCheckSuccess("/delete", data);
}

public boolean deleteTeacher(Long id) {
    Map<String, Object> data = new HashMap<>();
    data.put("table", "teacher");
    data.put("Id", id);
    return postAndCheckSuccess("/delete", data);
}

public boolean deleteStudent(Long id) {
    Map<String, Object> data = new HashMap<>();
    data.put("table", "student");
    data.put("Id", id);
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
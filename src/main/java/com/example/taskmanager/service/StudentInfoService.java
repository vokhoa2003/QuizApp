package com.example.taskmanager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentInfoService {
    private final ApiService apiService;

    public StudentInfoService(ApiService apiService) {
        this.apiService = apiService;
    }

    // Lấy profile (account + student + class) theo email
    public List<Map<String, Object>> fetchProfileByEmail(String email) {
        if (email == null || email.isEmpty()) return Collections.emptyList();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            params.put("table", List.of("account", "student", "classes"));

            List<Map<String, Object>> joinList = new ArrayList<>();
            Map<String, Object> j1 = new HashMap<>();
            j1.put("type", "inner");
            j1.put("on", List.of("account.id = student.IdAccount"));
            joinList.add(j1);

            Map<String, Object> j2 = new HashMap<>();
            j2.put("type", "inner");
            j2.put("on", List.of("student.ClassId = classes.Id"));
            joinList.add(j2);

            params.put("join", joinList);
            params.put("columns", List.of(
                "account.id", "account.FullName", "account.email", "account.GoogleID",
                "student.Id as StudentId", "student.Name as StudentName", "classes.Id as ClassId", "classes.Name as ClassName"
            ));

            Map<String, Object> where = new HashMap<>();
            where.put("account.email", email);
            params.put("where", where);

            List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
            return result != null ? result : Collections.emptyList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    // (renamed) Lấy profile (account + student + class) theo account id (từ token)
    public List<Map<String, Object>> fetchProfileById(int accountId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            params.put("table", List.of("account", "student", "classes"));

            List<Map<String, Object>> joinList = new ArrayList<>();
            Map<String, Object> j1 = new HashMap<>();
            j1.put("type", "inner");
            j1.put("on", List.of("account.id = student.IdAccount"));
            joinList.add(j1);

            Map<String, Object> j2 = new HashMap<>();
            j2.put("type", "inner");
            j2.put("on", List.of("student.ClassId = classes.Id"));
            joinList.add(j2);

            params.put("join", joinList);
            params.put("columns", List.of(
                "account.id", "account.FullName", "account.email",
                "student.Id as StudentId", "student.Name as StudentName", "classes.Id as ClassId", "classes.Name as ClassName"
            ));

            Map<String, Object> where = new HashMap<>();
            where.put("account.id", accountId);
            params.put("where", where);

            List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
            return result != null ? result : Collections.emptyList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Lấy danh sách lớp học của học sinh theo email (kèm giáo viên)
    public List<Map<String, Object>> fetchStudentClassesByEmail(String email) {
        if (email == null || email.isEmpty()) return Collections.emptyList();
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            // account -> student -> classes -> teacher
            params.put("table", List.of("account", "student", "classes", "teacher"));

            List<Map<String, Object>> join = new ArrayList<>();
            Map<String, Object> j1 = new HashMap<>();
            j1.put("type", "inner");
            j1.put("on", List.of("account.id = student.IdAccount"));
            join.add(j1);

            Map<String, Object> j2 = new HashMap<>();
            j2.put("type", "inner");
            j2.put("on", List.of("student.ClassId = classes.Id"));
            join.add(j2);

            Map<String, Object> j3 = new HashMap<>();
            j3.put("type", "left"); // dùng left để không loại lớp nếu thiếu giáo viên
            // Nếu schema khác, đổi điều kiện dưới đây:
            j3.put("on", List.of("classes.id = teacher.ClassId"));
            join.add(j3);

            params.put("join", join);

            params.put("columns", List.of(
                "classes.Id as ClassId",
                "classes.Name as ClassName",
                "COALESCE(teacher.Name, 'Đang cập nhật') as TeacherName"
            ));

            Map<String, Object> where = new HashMap<>();
            where.put("account.email", email);
            params.put("where", where);

            List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
            return result != null ? result : Collections.emptyList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Lấy danh sách lớp học theo accountId (kèm giáo viên)
    public List<Map<String, Object>> fetchStudentClassesByAccountId(int accountId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("table", List.of("account", "student", "classes", "teacher"));

            List<Map<String, Object>> join = new ArrayList<>();
            Map<String, Object> j1 = new HashMap<>();
            j1.put("type", "inner");
            j1.put("on", List.of("account.id = student.IdAccount"));
            join.add(j1);

            Map<String, Object> j2 = new HashMap<>();
            j2.put("type", "inner");
            j2.put("on", List.of("student.ClassId = classes.Id"));
            join.add(j2);

            Map<String, Object> j3 = new HashMap<>();
            j3.put("type", "left");
            j3.put("on", List.of("classes.id = teacher.ClassId"));
            join.add(j3);

            params.put("join", join);

            params.put("columns", List.of(
                "classes.Id as ClassId",
                "classes.Name as ClassName",
                "COALESCE(teacher.Name, 'Đang cập nhật') as TeacherName"
            ));

            Map<String, Object> where = new HashMap<>();
            where.put("account.id", accountId);
            params.put("where", where);

            List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
            return result != null ? result : Collections.emptyList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }
}
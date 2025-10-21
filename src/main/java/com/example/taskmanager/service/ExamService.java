package com.example.taskmanager.service;

import java.util.*;

public class ExamService {
    private ApiService apiService;

    public ExamService(ApiService apiService) {
        this.apiService = apiService;
    }

    public List<Map<String, Object>> fetchExamsByClass(String className) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            
            // Join exams với classes
            params.put("table", List.of("exams", "classes"));
            
            params.put("columns", List.of(
                "exams.id as ExamId",
                "exams.ExamName",
                "exams.NumberQuestion",
                "exams.Description",
                "exams.PublishDate",
                "exams.ExpireDate",
                "exams.ClassId",
                "classes.Name as ClassName"
            ));
            
            // Join condition
            Map<String, Object> join = new HashMap<>();
            join.put("type", "inner");
            join.put("on", List.of("exams.ClassId = classes.Id"));
            params.put("join", List.of(join));
            
            // WHERE: Lọc theo tên lớp
            Map<String, Object> where = new HashMap<>();
            where.put("classes.Name", className);
            params.put("where", where);
            
            System.out.println("📡 Fetching exams with params: " + params);
            
            List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
            
            System.out.println("✅ Fetched " + (result != null ? result.size() : 0) + " exams for class: " + className);
            
            return result != null ? result : Collections.emptyList();
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching exams by class: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> fetchExamResults(String studentEmail) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            
            // Join exam_results với student và account
            params.put("table", List.of("exam_results", "student", "account"));
            
            params.put("columns", List.of(
                "exam_results.ExamId",
                "exam_results.StudentId",
                "exam_results.Score",
                "exam_results.SubmittedDate",
                "account.email as StudentEmail"
            ));
            
            // Join conditions
            List<Map<String, Object>> joins = new ArrayList<>();
            
            Map<String, Object> join1 = new HashMap<>();
            join1.put("type", "inner");
            join1.put("on", List.of("exam_results.StudentId = student.Id"));
            joins.add(join1);
            
            Map<String, Object> join2 = new HashMap<>();
            join2.put("type", "inner");
            join2.put("on", List.of("student.IdAccount = account.id"));
            joins.add(join2);
            
            params.put("join", joins);
            
            // WHERE: Lọc theo email
            Map<String, Object> where = new HashMap<>();
            where.put("account.email", studentEmail);
            params.put("where", where);
            
            System.out.println("📡 Fetching exam results with params: " + params);
            
            List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
            
            System.out.println("✅ Fetched " + (result != null ? result.size() : 0) + " exam results for: " + studentEmail);
            
            return result != null ? result : Collections.emptyList();
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching exam results: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
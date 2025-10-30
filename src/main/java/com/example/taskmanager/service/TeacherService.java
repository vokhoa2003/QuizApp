package com.example.taskmanager.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.example.taskmanager.model.Teacher;

public class TeacherService {
    private final ApiService apiService;

    public TeacherService(ApiService apiService) {
        this.apiService = apiService;
    }

    // Trả về danh sách lớp của giáo viên kèm studentCount (keys: ClassId, ClassName, StudentCount)
    public List<Map<String, Object>> getClassesForTeacher(int teacherId) {
    if (teacherId <= 0) return Collections.emptyList();

    Set<Integer> classIds = new HashSet<>();
    Map<Integer, String> classNames = new HashMap<>();

    // === LẤY TỪ exams ===
    try {
        Map<String, Object> p = new HashMap<>();
        p.put("action", "get"); p.put("method", "SELECT");
        p.put("table", List.of("exams", "classes"));
        p.put("columns", List.of("DISTINCT classes.Id as ClassId", "classes.Name as ClassName"));
        p.put("join", List.of(Map.of("type", "inner", "on", List.of("exams.ClassId = classes.Id"))));
        p.put("where", Map.of("exams.TeacherId", teacherId));

        List<Map<String, Object>> rows = normalize(apiService.postApiGetList("/autoGet", p));
        for (Map<String, Object> r : rows) {
            Object cidObj = firstNonNull(r, "ClassId", "id", "Id");
            Integer cid = null;
            if (cidObj instanceof Number) {
                cid = ((Number) cidObj).intValue();
            } else if (cidObj != null) {
                try { cid = Integer.parseInt(cidObj.toString().trim()); } catch (Exception ignored) {}
            }
            String name = String.valueOf(firstNonNull(r, "ClassName", "Name", "Lớp"));
            if (cid != null) {
                classIds.add(cid);
                classNames.put(cid, name);
            }
        }
    } catch (Exception ignored) {}

    // === LẤY TỪ teacher.ClassId ===
    try {
        Map<String, Object> p = new HashMap<>();
        p.put("action", "get"); p.put("method", "SELECT");
        p.put("table", List.of("teacher", "classes"));
        p.put("columns", List.of("teacher.ClassId", "classes.Name as ClassName"));
        p.put("join", List.of(Map.of("type", "left", "on", List.of("teacher.ClassId = classes.Id"))));
        p.put("where", Map.of("teacher.Id", teacherId));

        List<Map<String, Object>> rows = normalize(apiService.postApiGetList("/autoGet", p));
        if (!rows.isEmpty()) {
            Map<String, Object> r = rows.get(0);
            Object cidObj = r.get("ClassId");
            Integer cid = null;
            if (cidObj instanceof Number) {
                cid = ((Number) cidObj).intValue();
            } else if (cidObj != null) {
                try { cid = Integer.parseInt(cidObj.toString().trim()); } catch (Exception ignored) {}
            }
            String name = String.valueOf(firstNonNull(r, "ClassName", "Name", "Lớp chính"));
            if (cid != null) {
                classIds.add(cid);
                classNames.put(cid, name);
            }
        }
    } catch (Exception ignored) {}

    // Tính số học sinh
    List<Integer> listIds = new ArrayList<>(classIds);
    Map<Integer, Integer> counts = getCountsForClasses(listIds);

    // Tạo kết quả
    List<Map<String, Object>> result = new ArrayList<>();
    for (Integer cid : listIds) {
        Map<String, Object> item = new HashMap<>();
        item.put("ClassId", cid);
        item.put("ClassName", classNames.getOrDefault(cid, "Lớp " + cid));
        item.put("StudentCount", counts.getOrDefault(cid, 0));
        result.add(item);
    }
    return result;
}
    // Trả về danh sách exams do teacher tạo (id, ExamName, NumberQuestion, PublishDate, ExpireDate)
    public List<Map<String,Object>> getExamsForTeacher(int teacherId) {
        if (teacherId <= 0) return Collections.emptyList();
        Map<String,Object> p = new HashMap<>();
        p.put("action", "get");
        p.put("method", "SELECT");
        p.put("table", "exams");
        p.put("columns", List.of("id as ExamId", "ExamName", "NumberQuestion", "PublishDate", "ExpireDate"));
        p.put("where", Map.of("TeacherId", teacherId));
        try {
            Object resp = apiService.postApiGetList("/autoGet", p);
            return normalize(resp);
        } catch (Exception ex) {
            System.err.println("WARN: getExamsForTeacher error: " + ex.getMessage());
            return Collections.emptyList();
        }
    }

    // Lấy số học sinh theo nhiều classId bằng 1 truy vấn GROUP BY (nếu API hỗ trợ IN + GROUP)
    private Map<Integer,Integer> getCountsForClasses(List<Integer> classIds) {
        Map<Integer,Integer> out = new HashMap<>();
        if (classIds == null || classIds.isEmpty()) return out;

        List<Map<String,Object>> rows = Collections.emptyList();

        // 1) Try fetch students with WHERE ClassId IN (...)
        try {
            Map<String,Object> q = new HashMap<>();
            q.put("action", "get");
            q.put("method", "SELECT");
            q.put("table", "student");
            q.put("columns", List.of("Id", "ClassId"));
            q.put("where", Map.of("ClassId", classIds));
            System.out.println("DEBUG: trying student fetch by ClassId IN " + classIds);
            Object resp = apiService.postApiGetList("/autoGet", q);
            System.out.println("DEBUG: student-by-classIds raw resp = " + Objects.toString(resp));
            rows = normalize(resp);
        } catch (Exception ex) {
            System.err.println("WARN: student-by-classIds failed: " + ex.getMessage());
        }

        // 2) Fallback: if rows empty or looks invalid, fetch all students and group locally
        if (rows == null || rows.isEmpty()) {
            try {
                Map<String,Object> q2 = new HashMap<>();
                q2.put("action", "get");
                q2.put("method", "SELECT");
                q2.put("table", "student");
                q2.put("columns", List.of("Id", "ClassId"));
                System.out.println("DEBUG: fallback fetch all students");
                Object resp2 = apiService.postApiGetList("/autoGet", q2);
                System.out.println("DEBUG: all-students raw resp = " + Objects.toString(resp2));
                rows = normalize(resp2);
            } catch (Exception ex) {
                System.err.println("ERROR: fallback fetch all students failed: " + ex.getMessage());
                rows = Collections.emptyList();
            }
        }

        // 3) Count locally for requested classIds
        // initialize zeros
        for (Integer id : classIds) out.put(id, 0);

        if (rows != null && !rows.isEmpty()) {
            for (Map<String,Object> r : rows) {
                Object cidObj = firstNonNull(r, "ClassId", "classid", "classId", "Class_Id", "ClassID", "ClassId", "classes.Id");
                Integer cid = null;
                if (cidObj instanceof Number) cid = ((Number) cidObj).intValue();
                else if (cidObj != null) {
                    try { cid = Integer.parseInt(cidObj.toString()); } catch (Exception ignored) {}
                }
                if (cid != null && out.containsKey(cid)) {
                    out.put(cid, out.getOrDefault(cid, 0) + 1);
                }
            }
        }

        return out;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String,Object>> normalize(Object resp) {
        if (resp == null) return Collections.emptyList();
        if (resp instanceof List) return (List<Map<String,Object>>) resp;
        if (resp instanceof Map) {
            Object d = ((Map) resp).get("data");
            if (d instanceof List) return (List<Map<String,Object>>) d;
            List<Map<String,Object>> list = new ArrayList<>();
            for (Object k : ((Map)resp).keySet()) {
                String ks = k == null ? "" : k.toString();
                if (ks.matches("\\d+")) {
                    Object val = ((Map)resp).get(k);
                    if (val instanceof Map) list.add((Map<String,Object>) val);
                }
            }
            if (!list.isEmpty()) return list;
        }
        return Collections.emptyList();
    }

    public Teacher getTeacherById(int teacherId) {
    if (teacherId <= 0) return null;

    Map<String, Object> p = new HashMap<>();
    p.put("action", "get");
    p.put("method", "SELECT");
    p.put("table", "teacher");
    p.put("columns", List.of("Id", "Name", "ClassId"));
    p.put("where", Map.of("Id", teacherId));

    try {
        Object resp = apiService.postApiGetList("/autoGet", p);
        List<Map<String, Object>> rows = normalize(resp);
        if (rows != null && !rows.isEmpty()) {
            Map<String, Object> d = rows.get(0);
            Teacher t = new Teacher();
            t.setId(toLong(d.get("id")));
            t.setName((String) firstNonNull(d, "name", "Name")); // ← AN TOÀN VỚI CASE
            t.setClassId(toLong(d.get("classId")));
            System.out.println("DEBUG: Teacher Name loaded = " + t.getName());
            return t;
        }
    } catch (Exception e) {
        System.err.println("ERROR: getTeacherById: " + e.getMessage());
        e.printStackTrace();
    }
    return null;
}

// Helper methods
private Long toLong(Object obj) {
    if (obj instanceof Number) return ((Number) obj).longValue();
    if (obj != null) {
        try { return Long.parseLong(obj.toString().trim()); } catch (Exception ignored) {}
    }
    return null;
}

private LocalDateTime toLocalDateTime(Object obj) {
    if (obj == null) return null;
    try { return LocalDateTime.parse(obj.toString()); } catch (Exception e) { return null; }
}

    private Object firstNonNull(Map<String,Object> m, String... keys) {
        if (m == null) return null;
        for (String k : keys) {
            if (k == null) continue;
            Object v = m.get(k);
            if (v == null) v = m.get(k.toLowerCase());
            if (v != null) return v;
        }
        return null;
    }
}
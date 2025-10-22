package com.example.taskmanager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TeacherService {
    private final ApiService apiService;

    public TeacherService(ApiService apiService) {
        this.apiService = apiService;
    }

    // Trả về danh sách lớp của giáo viên kèm studentCount (keys: ClassId, ClassName, StudentCount)
    public List<Map<String, Object>> getClassesForTeacher(int teacherId) {
        if (teacherId <= 0) return Collections.emptyList();

        List<Map<String,Object>> result = Collections.emptyList();

        // Thử một số biến thể query theo thứ tự
        List<Map<String,Object>> attempts = new ArrayList<>();

        Map<String,Object> a = new HashMap<>();
        a.put("action", "get");
        a.put("method", "SELECT");
        a.put("table", "classes");
        a.put("columns", List.of("id as ClassId", "Name as ClassName"));
        a.put("where", Map.of("TeacherId", teacherId));
        attempts.add(a);

        Map<String,Object> b = new HashMap<>();
        b.put("action", "get");
        b.put("method", "SELECT");
        b.put("table", List.of("teacher", "classes"));
        b.put("columns", List.of("classes.id as ClassId", "classes.Name as ClassName"));
        b.put("where", Map.of("teacher.Id", teacherId));
        attempts.add(b);

        Map<String,Object> c = new HashMap<>();
        c.put("action", "get");
        c.put("method", "SELECT");
        c.put("table", "classes");
        c.put("columns", List.of("Id as ClassId", "Name as ClassName"));
        c.put("where", Map.of("TeacherId", teacherId));
        attempts.add(c);

        for (Map<String,Object> params : attempts) {
            try {
                Object resp = apiService.postApiGetList("/autoGet", params);
                List<Map<String,Object>> rows = normalize(resp);
                if (rows != null && !rows.isEmpty()) {
                    result = rows;
                    break;
                }
            } catch (Exception ex) {
                System.err.println("WARN: getClassesForTeacher attempt failed: " + ex.getMessage());
            }
        }

        // Nếu không có lớp thì trả rỗng
        if (result == null || result.isEmpty()) return Collections.emptyList();

        // Thu thập classIds để lấy counts bằng 1 truy vấn GROUP BY
        List<Integer> classIds = new ArrayList<>();
        Map<Integer, Map<String,Object>> tmpById = new HashMap<>();
        for (Map<String,Object> r : result) {
            Object cidObj = firstNonNull(r, "ClassId", "id", "Id", "classes.Id");
            Integer cid = null;
            if (cidObj instanceof Number) cid = ((Number) cidObj).intValue();
            else if (cidObj != null) {
                try { cid = Integer.parseInt(cidObj.toString()); } catch (Exception ignored) {}
            }
            String cname = String.valueOf(firstNonNull(r, "ClassName", "Name", "classes.Name", ""));
            Map<String,Object> item = new HashMap<>();
            item.put("ClassId", cid);
            item.put("ClassName", cname);
            tmpById.put(cid != null ? cid : -1, item);
            if (cid != null) classIds.add(cid);
        }

        // Lấy counts cho tất cả classIds bằng 1 request (nếu có IDs)
        Map<Integer,Integer> counts = Collections.emptyMap();
        if (!classIds.isEmpty()) {
            counts = getCountsForClasses(classIds);
        }

        // Ghép kết quả
        List<Map<String,Object>> out = new ArrayList<>();
        for (Integer key : tmpById.keySet()) {
            Map<String,Object> base = tmpById.get(key);
            Integer cid = (Integer) base.get("ClassId");
            int cnt = 0;
            if (cid != null && counts.containsKey(cid)) cnt = counts.get(cid);
            base.put("StudentCount", cnt);
            out.add(base);
        }

        return out;
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
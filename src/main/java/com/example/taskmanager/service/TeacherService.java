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
            p.put("action", "get"); 
            p.put("method", "SELECT");
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

        // === LẤY TỪ teacher_class (QUAN HỆ NHIỀU-NHIỀU) ===
        try {
            Map<String, Object> p = new HashMap<>();
            p.put("action", "get"); 
            p.put("method", "SELECT");
            p.put("table", List.of("teacher_class", "classes"));
            p.put("columns", List.of("classes.Id as ClassId", "classes.Name as ClassName"));
            p.put("join", List.of(Map.of("type", "inner", "on", List.of("teacher_class.ClassId = classes.Id"))));
            p.put("where", Map.of("teacher_class.TeacherId", teacherId));

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

    // Lấy số học sinh theo nhiều classId qua student_class
    private Map<Integer,Integer> getCountsForClasses(List<Integer> classIds) {
        Map<Integer,Integer> out = new HashMap<>();
        if (classIds == null || classIds.isEmpty()) return out;

        // Initialize zeros
        for (Integer id : classIds) out.put(id, 0);

        // Đếm học sinh qua bảng student_class
        try {
            Map<String,Object> q = new HashMap<>();
            q.put("action", "get");
            q.put("method", "SELECT");
            q.put("table", "student_class");
            q.put("columns", List.of("ClassId", "COUNT(*) as StudentCount"));
            q.put("where", Map.of("ClassId", classIds));
            q.put("groupBy", List.of("ClassId"));
            
            System.out.println("DEBUG: counting students via student_class for classIds: " + classIds);
            Object resp = apiService.postApiGetList("/autoGet", q);
            System.out.println("DEBUG: student count response = " + Objects.toString(resp));
            
            List<Map<String,Object>> rows = normalize(resp);
            for (Map<String,Object> r : rows) {
                Object cidObj = firstNonNull(r, "ClassId", "classid");
                Object countObj = firstNonNull(r, "StudentCount", "count", "cnt");
                
                Integer cid = null;
                if (cidObj instanceof Number) cid = ((Number) cidObj).intValue();
                else if (cidObj != null) {
                    try { cid = Integer.parseInt(cidObj.toString()); } catch (Exception ignored) {}
                }
                
                Integer count = 0;
                if (countObj instanceof Number) count = ((Number) countObj).intValue();
                else if (countObj != null) {
                    try { count = Integer.parseInt(countObj.toString()); } catch (Exception ignored) {}
                }
                
                if (cid != null && out.containsKey(cid)) {
                    out.put(cid, count);
                }
            }
        } catch (Exception ex) {
            System.err.println("ERROR: counting students failed: " + ex.getMessage());
        }

        return out;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String,Object>> normalize(Object resp) {
        if (resp == null) {
            System.out.println("normalize: resp is null → return emptyList");
            return Collections.emptyList();
        }
        if (resp instanceof List) {
            return (List<Map<String,Object>>) resp;
        }
        if (resp instanceof Map) {
            Object d = ((Map<?, ?>) resp).get("data");
            if (d instanceof List) {
                return (List<Map<String,Object>>) d;
            }
            List<Map<String,Object>> list = new ArrayList<>();
            for (Object k : ((Map<?, ?>) resp).keySet()) {
                String ks = k == null ? "" : k.toString();
                if (ks.matches("\\d+")) {
                    Object val = ((Map<?, ?>) resp).get(k);
                    if (val instanceof Map) {
                        list.add((Map<String,Object>) val);
                    }
                }
            }
            if (!list.isEmpty()) {
                return list;
            }
        }
        System.out.println("normalize: cannot parse resp → return emptyList | resp=" + resp);
        return Collections.emptyList();
    }

    public Teacher getTeacherById(int teacherId) {
        System.out.println("getTeacherById(" + teacherId + ") STARTED");
        if (teacherId <= 0) {
            System.out.println("getTeacherById: teacherId <= 0 → return null");
            return null;
        }

        Map<String, Object> p = new HashMap<>();
        p.put("action", "get");
        p.put("method", "SELECT");
        p.put("table", "teacher");
        p.put("columns", List.of("Id", "IdAccount", "Name", "CreateDate", "UpdateDate"));
        p.put("where", Map.of("IdAccount", teacherId));

        try {
            Object resp = apiService.postApiGetList("/autoGet", p);
            System.out.println("DEBUG RAW RESPONSE: " + resp);

            List<Map<String, Object>> rows = normalize(resp);
            if (rows != null && !rows.isEmpty()) {
                Map<String, Object> d = rows.get(0);
                Teacher t = new Teacher();
                t.setId(toLong(firstNonNull(d, "Id", "id")));
                t.setIdAccount(toLong(firstNonNull(d, "IdAccount", "idaccount")));
                String name = String.valueOf(firstNonNull(d, "Name", "name", "FullName"));
                t.setName(name != null && !name.trim().isEmpty() ? name.trim() : "Giáo viên");
                t.setCreateDate(toLocalDateTime(firstNonNull(d, "CreateDate", "createdate")));
                t.setUpdateDate(toLocalDateTime(firstNonNull(d, "UpdateDate", "updatedate")));
                
                System.out.println("getTeacherById() SUCCESS → Name = " + t.getName());
                return t;
            } else {
                System.out.println("getTeacherById() → No data in response");
            }
        } catch (Exception e) {
            System.err.println("ERROR in getTeacherById(): " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("getTeacherById() → return null");
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
            // Thử exact match trước
            Object v = m.get(k);
            if (v != null) return v;
            
            // Thử case-insensitive
            for (Map.Entry<String, Object> entry : m.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(k)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
}
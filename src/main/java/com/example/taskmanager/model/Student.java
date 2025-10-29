package com.example.taskmanager.model;

import java.time.LocalDateTime;

public class Student {
    private Long id;
    private Long idAccount;
    private String name;
    private Long classId;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public Student() {}

    public Student(Long id, String name, Long classId) {
        this.id = id;
        this.name = name;
        this.classId = classId;
    }

    public Long getId() { return id; }
    public Long getIdAccount() { return idAccount; }
    public String getName() { return name; }
    public Long getClassId() { return classId; }
    public LocalDateTime getCreateDate() { return createDate; }
    public LocalDateTime getUpdateDate() { return updateDate; }

    public void setId(Long id) { this.id = id; }
    public void setIdAccount(Long idAccount) { this.idAccount = idAccount; }
    public void setName(String name) { this.name = name; }
    public void setClassId(Long classId) { this.classId = classId; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
}
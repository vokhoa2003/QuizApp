package com.example.taskmanager.model;

import java.time.LocalDateTime;

public class Teacher {
    private Long id;
    private Long idAccount;
    private String name;
    private Long classId;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    // Constructors
    public Teacher() {}
    public Teacher(Long idAccount, String name, Long classId) {
        this.idAccount = idAccount;
        this.name = name;
        this.classId = classId;
        this.createDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdAccount() { return idAccount; }
    public void setIdAccount(Long idAccount) { this.idAccount = idAccount; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
}
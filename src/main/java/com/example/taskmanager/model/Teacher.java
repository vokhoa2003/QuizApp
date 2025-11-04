package com.example.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Teacher {
    @JsonProperty("Id")
    private Long id;

    @JsonProperty("IdAccount")
    private Long idAccount;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("ClassId")
    private Long classId;

    @JsonProperty("CreateDate")
    private LocalDateTime createDate;

    @JsonProperty("UpdateDate")
    private LocalDateTime updateDate;

    // Constructors
    public Teacher() {}

    // DÙNG KHI THÊM MỚI (cần idAccount)
    public Teacher(Long idAccount, String name, Long classId) {
        this.idAccount = idAccount;
        this.name = name;
        this.classId = classId;
        this.createDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getIdAccount() { return idAccount; }
    public String getName() { return name; }
    public Long getClassId() { return classId; }
    public LocalDateTime getCreateDate() { return createDate; }
    public LocalDateTime getUpdateDate() { return updateDate; }

    public void setFullName(String fullName) { this.name = fullName; }
    @JsonProperty("status")
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setIdAccount(Long idAccount) { this.idAccount = idAccount; }
    public void setName(String name) { this.name = name; }
    public void setClassId(Long classId) { this.classId = classId; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
}
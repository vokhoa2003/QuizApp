package com.example.taskmanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Student {
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
    public Student() {}

    public Student(Long idAccount, String name, Long classId) {
        //this.id = id;
        this.idAccount = idAccount;
        this.name = name;
        this.classId = classId;
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
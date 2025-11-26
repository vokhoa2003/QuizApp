package com.example.taskmanager.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Teacher {
    @JsonProperty("Id")
    private Long id;

    @JsonProperty("IdAccount")
    private Long idAccount;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("CreateDate")
    private LocalDateTime createDate;

    @JsonProperty("UpdateDate")
    private LocalDateTime updateDate;

    // Constructors
    public Teacher() {}

    // Constructor cho thêm mới (không cần classId nữa)
    public Teacher(Long idAccount, String name) {
        this.idAccount = idAccount;
        this.name = name;
        this.createDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getIdAccount() { return idAccount; }
    public String getName() { return name; }
    public LocalDateTime getCreateDate() { return createDate; }
    public LocalDateTime getUpdateDate() { return updateDate; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setIdAccount(Long idAccount) { this.idAccount = idAccount; }
    public void setName(String name) { this.name = name; }
    public void setFullName(String fullName) { this.name = fullName; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
}
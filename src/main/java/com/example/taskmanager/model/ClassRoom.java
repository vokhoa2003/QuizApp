package com.example.taskmanager.model;

import java.time.LocalDateTime;

public class ClassRoom {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    // Constructors
    public ClassRoom() {}
    public ClassRoom(String name, String description) {
        this.name = name;
        this.description = description;
        this.createDate = LocalDateTime.now();
        this.updateDate = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
}
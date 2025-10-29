package com.example.taskmanager.model;

import java.time.LocalDateTime;

public class ClassRoom {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public ClassRoom() {}

    // getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getCreateDate() { return createDate; }
    public LocalDateTime getUpdateDate() { return updateDate; }

    // setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
}
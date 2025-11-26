package com.example.taskmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task {
    private Long id;
    private String email;
    private String role;
    private String googleId;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private String fullName;
    private String status;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String identityNumber;
    private String question; 
    private String answer;
    private String accountStatus;



 

    // Getters và setters cho các trường chính
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    @JsonProperty("GoogleID")
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    @JsonProperty("createDate")
    public LocalDateTime getCreateDate() { return createDate; }
    public void setCreateDate(LocalDateTime createDate) { this.createDate = createDate; }
    @JsonProperty("updateDate")
    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }
    @JsonProperty("fullName")
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    @JsonProperty("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @JsonProperty("phone")
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    @JsonProperty("address")
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    @JsonProperty("birthDate")
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    @JsonProperty("identityNumber")
    public String getIdentityNumber() { return identityNumber; }
    public void setIdentityNumber(String identityNumber) { this.identityNumber = identityNumber; }

    // Khóa lại những trường không dùng (comment lại)
//    private String accountStatus;
//    public String getAccountStatus() { return accountStatus; }
//    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
}
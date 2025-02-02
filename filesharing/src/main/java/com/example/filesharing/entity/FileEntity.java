package com.example.filesharing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    @Column(nullable = false)
    private LocalDateTime deleteTime;

    // Default constructor (required by JPA)
    public FileEntity() {}

    // Parameterized constructor (optional, for convenience)
    public FileEntity(String fileName, String filePath, LocalDateTime uploadTime, LocalDateTime deleteTime) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadTime = uploadTime;
        this.deleteTime = deleteTime;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }

    // toString method (optional, for debugging)
    @Override
    public String toString() {
        return "FileEntity{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", uploadTime=" + uploadTime +
                ", deleteTime=" + deleteTime +
                '}';
    }
}
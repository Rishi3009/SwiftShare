package com.example.filesharing.service;

import com.example.filesharing.entity.FileEntity;
import com.example.filesharing.exception.FileStorageException;
import com.example.filesharing.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Value("${file.upload-dir:uploads}") // Default to "uploads" if not set
    private String uploadDir;

    private Path rootLocation;
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir);
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
                logger.info("✅ Uploads directory initialized: {}", rootLocation.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new FileStorageException("❌ Could not initialize storage directory!", e);
        }
    }

    @Transactional
    public String saveFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("❌ Cannot upload an empty file.");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            throw new FileStorageException("❌ File name cannot be null or empty.");
        }

        String fileName = UUID.randomUUID() + "_" + originalFileName;
        Path filePath = rootLocation.resolve(fileName);

        logger.info("📂 Attempting to save file: {} (Size: {} bytes)", originalFileName, file.getSize());

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("✅ File saved successfully: {}", filePath.toAbsolutePath());

            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setFilePath(filePath.toString());
            fileEntity.setUploadTime(LocalDateTime.now());
            fileEntity.setDeleteTime(LocalDateTime.now().plusHours(24)); // Auto-delete after 24 hours

            logger.info("🗂 Saving file entity to database: {}", fileEntity);
            FileEntity savedFile = fileRepository.save(fileEntity);
            logger.info("✅ File successfully stored in DB with ID: {}", savedFile.getId());

            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("❌ Failed to store file: " + originalFileName, e);
        }
    }

    public Optional<FileEntity> getFile(String fileName) {
        logger.info("🔍 Retrieving file: {}", fileName);
        return fileRepository.findByFileName(fileName);
    }

    public List<FileEntity> getAllFiles() {
        logger.info("🔍 Retrieving all files...");
        return fileRepository.findAll();
    }

    @Transactional
    public void deleteExpiredFiles() {
        logger.info("⏳ Checking for expired files...");
        List<FileEntity> expiredFiles = fileRepository.findByDeleteTimeBefore(LocalDateTime.now());

        if (!expiredFiles.isEmpty()) {
            for (FileEntity file : expiredFiles) {
                if (file.getFilePath() != null && !file.getFilePath().isBlank()) {
                    try {
                        Path filePath = Paths.get(file.getFilePath());
                        Files.deleteIfExists(filePath);
                        logger.info("🗑 Deleted expired file: {}", file.getFileName());
                    } catch (IOException e) {
                        logger.error("❌ Failed to delete file: {}", file.getFileName(), e);
                    }
                }
            }
            fileRepository.deleteAll(expiredFiles);
            logger.info("✅ Deleted {} expired files from database.", expiredFiles.size());
        } else {
            logger.info("📭 No expired files found.");
        }
    }

    @Async
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredFiles() {
        logger.info("🧹 Running scheduled cleanup...");
        deleteExpiredFiles();
    }

    public void deleteFile(String fileName) {
        Optional<FileEntity> fileEntity = fileRepository.findByFileName(fileName);
        if (fileEntity.isPresent()) {
            try {
                Path filePath = Paths.get(fileEntity.get().getFilePath());
                Files.deleteIfExists(filePath);
                fileRepository.delete(fileEntity.get());
                logger.info("🗑 Deleted file: {}", fileName);
            } catch (IOException e) {
                throw new FileStorageException("❌ Failed to delete file: " + fileName, e);
            }
        } else {
            throw new FileStorageException("❌ File not found: " + fileName);
        }
    }

    public boolean fileExists(String fileName) {
        return fileRepository.findByFileName(fileName).isPresent();
    }
}

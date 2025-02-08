package com.example.filesharing.controller;

import com.example.filesharing.entity.FileEntity;
import com.example.filesharing.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
@CrossOrigin(origins = "*") // Allow cross-origin requests
public class FileController {

    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("files", fileService.getAllFiles());
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/";
        }

        try {
            String fileName = fileService.saveFile(file);
            redirectAttributes.addFlashAttribute("message", "File uploaded successfully: " + fileName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to upload file: " + e.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Optional<FileEntity> fileEntityOptional = fileService.getFile(fileName);
            if (fileEntityOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            FileEntity fileEntity = fileEntityOptional.get();
            Path filePath = Paths.get(fileEntity.getFilePath());

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            Resource resource = new UrlResource(filePath.toUri());

            // Detect the content type dynamically
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream"; // Default fallback
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION) // Fix for CORS downloads
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

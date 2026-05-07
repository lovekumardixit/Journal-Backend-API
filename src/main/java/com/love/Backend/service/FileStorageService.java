package com.love.Backend.service;

import com.love.Backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service 
public class FileStorageService {

    @Value("${app.upload.dir:uploads}") 
    private String uploadDir; 

    public String store(MultipartFile file) { 
        if (file == null || file.isEmpty()) { 
            throw new BadRequestException("Attachment file is required"); 
        }
        try { 
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize(); 
            Files.createDirectories(uploadPath); 
            String originalName = file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename(); 
            String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_"); 
            String storedName = UUID.randomUUID() + "_" + safeName; 
            Path target = uploadPath.resolve(storedName).normalize(); 
            file.transferTo(target); 
            return "/uploads/" + storedName; 
        } catch (IOException ex) { 
            throw new BadRequestException("Failed to store attachment"); 
        }
    }
}

package com.love.Backend.service;

import com.love.Backend.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ProfilePhotoService {

    private final S3Client s3Client;

    public ProfilePhotoService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public String uploadProfilePhoto(MultipartFile file, String userId) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Profile photo file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Profile photo size must not exceed 5 MB");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "Only JPEG, PNG, GIF, and WebP images are allowed"
            );
        }

        try {

            String originalName = file.getOriginalFilename();

            String extension = getFileExtension(originalName);

            String fileName =
                    "profile-photos/" +
                            userId + "_" +
                            UUID.randomUUID() +
                            "." + extension;

            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(contentType)
                            .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(file.getBytes())
            );

            return "https://" +
                    bucketName +
                    ".s3.amazonaws.com/" +
                    fileName;

        } catch (IOException ex) {
            throw new BadRequestException(
                    "Failed to upload profile photo: " + ex.getMessage()
            );
        }
    }

    public void deleteProfilePhoto(String photoUrl) {

        if (photoUrl == null || photoUrl.isBlank()) {
            return;
        }

        try {

            String key =
                    photoUrl.substring(
                            photoUrl.indexOf(".com/") + 5
                    );

            DeleteObjectRequest deleteObjectRequest =
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception ex) {

            System.err.println(
                    "Failed to delete old profile photo: "
                            + ex.getMessage()
            );
        }
    }

    private String getFileExtension(String filename) {

        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }

        return filename.substring(
                filename.lastIndexOf(".") + 1
        ).toLowerCase();
    }
}
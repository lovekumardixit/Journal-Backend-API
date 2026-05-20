package com.love.Backend.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "users")
@Schema(description = "User entity representing a system user")
public class User {
    @Id
    @Schema(description = "Unique identifier for the user", example = "507f1f77bcf86cd799439011")
    private ObjectId id;
    @Indexed(unique = true, sparse = true)
    @Schema(description = "Unique username (lowercase, no spaces)", example = "john_doe")
    private String userName;
    @Schema(description = "Password for the user account (hashed)", example = "hashed_password")
    private String password;

    @Indexed(unique = true, sparse = true)
    @Schema(description = "Unique valid email address", example = "john.doe@example.com")
    private String email;
    @Schema(description = "Flag to enable sentiment analysis", example = "true")
    private Boolean sentimentAnalysis;
    @Schema(description = "List of entries associated with the user")
    @DBRef(lazy = true)
    @JsonIgnore
    private List<entry> entries = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of roles assigned to the user", example = "[\"USER\", \"ADMIN\"]")
    private List<String> roles = new ArrayList<>();

    @Schema(description = "URL to user's profile photo", example = "https://bucket.s3.amazonaws.com/profile-photos/photo.jpg")
    private String profilePhotoUrl;

    @Schema(description = "First name of the user", example = "John")
    private String firstName;

    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;

    @Schema(description = "Age of the user", example = "30")
    private Integer age;

    @Schema(description = "City of the user", example = "Mumbai")
    private String city;

}

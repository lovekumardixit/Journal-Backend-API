package com.love.Backend.entity;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

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
    @Indexed(unique = true)
    @NonNull
    @Schema(description = "Username of the user", example = "john_doe")
    private String userName;
    @NonNull
    @Schema(description = "Password for the user account", example = "password123")
    private String password;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;
    @Schema(description = "Flag to enable sentiment analysis", example = "true")
    private Boolean sentimentAnalysis;
    @DBRef
    @Schema(description = "List of entries associated with the user")
    private List<entry> entries = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of roles assigned to the user", example = "[\"USER\", \"ADMIN\"]")
    private List<String> roles = new ArrayList<>();



}

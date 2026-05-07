package com.love.Backend.dto.request;

import com.love.Backend.entity.entry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "Request DTO for user operations")
public class UserRequestDTO {

    
    @Schema(description = "Unique identifier for the user", example = "507f1f77bcf86cd799439011")
    private ObjectId id;
    @NotNull(message = "Username cannot be null")
    @NotBlank
    @Size(min = 3, message = "Username should have atleast 3 characters")
    @Schema(description = "Username of the user", example = "lav_dixit", required = true)
    private String userName;
    @NotBlank
    @Size(min = 8, message = "Password must be minimum 8 characters")
    @Schema(description = "Password for the user account", example = "password123", required = true)
    private String password;
    @Email
    @Schema(description = "Email address of the user", example = "lav.dixit@example.com", required = true)
    private String email;
    @Schema(description = "Flag to enable sentiment analysis", example = "true")
    private Boolean sentimentAnalysis;
    @Schema(description = "List of roles assigned to the user", example = "[\"USER\", \"ADMIN\"]")
    private List<String> roles = new ArrayList<>();
    @DBRef
    @Schema(description = "List of entries associated with the user")
    private List<entry> entries = new ArrayList<>();
}

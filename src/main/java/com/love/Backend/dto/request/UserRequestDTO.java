package com.love.Backend.dto.request;

import com.love.Backend.entity.entry;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "Username cannot be empty or blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-z0-9_.-]+$", message = "Username can only contain lowercase letters, numbers, dots, hyphens, and underscores. No spaces allowed.")
    @Schema(description = "Unique username (lowercase, alphanumeric with underscore/dot/hyphen)", example = "lav_dixit", required = true)
    private String userName;
    @NotBlank(message = "Password cannot be empty or blank")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&)")
    @Schema(description = "Password with uppercase, lowercase, number, and special character", example = "Password@123", required = true)
    private String password;
    @NotBlank(message = "Email cannot be empty or blank")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(description = "Unique valid email address", example = "lav.dixit@example.com", required = true)
    private String email;
    @Schema(description = "Flag to enable sentiment analysis", example = "true")
    private Boolean sentimentAnalysis;
    @Schema(description = "List of roles assigned to the user", example = "[\"USER\", \"ADMIN\"]")
    private List<String> roles = new ArrayList<>();
    @DBRef
    @Schema(description = "List of entries associated with the user")
    private List<entry> entries = new ArrayList<>();

    @Schema(description = "First name of the user", example = "John")
    private String firstName;

    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;

    @Schema(description = "Age of the user", example = "30")
    private Integer age;

    @Schema(description = "City of the user (India based)", example = "Mumbai")
    private String city;

    @Schema(description = "URL to user's profile photo", example = "/uploads/profile-photos/507f1f77bcf86cd799439011_photo.jpg")
    private String profilePhotoUrl;
}

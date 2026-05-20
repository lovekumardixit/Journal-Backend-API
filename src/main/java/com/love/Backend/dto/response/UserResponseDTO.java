package com.love.Backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "Response DTO for user profile information")
public class UserResponseDTO {

    @Schema(description = "Username of the user", example = "john_doe")
    private String userName;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Roles assigned to the user", example = "[\"USER\"]")
    private List<String> roles = new ArrayList<>();

    @Schema(description = "URL to user's profile photo (if set)", example = "/uploads/profile-photos/507f1f77bcf86cd799439011_photo.jpg")
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
package com.love.Backend.controller;

import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.dto.response.UserResponseDTO;
import com.love.Backend.entity.User;
import com.love.Backend.entity.WeatherResponse;
import com.love.Backend.service.ProfilePhotoService;
import com.love.Backend.service.UserEntryService;
import com.love.Backend.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "User Management APIs", description = "APIs for managing user profiles and weather information")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserEntryService userService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private ProfilePhotoService profilePhotoService;

    @Operation(
            summary = "Get weather greeting",
            description = "Retrieve a personalized greeting with weather information for a city"
    )
    @ApiResponse(responseCode = "200", description = "Greeting retrieved successfully")
    @Parameter(name = "city", description = "City name", required = true)
    @GetMapping("/get/{city}")
    public ResponseEntity<?> greeting(@PathVariable String city) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        WeatherResponse weatherResponse =
                weatherService.getWeather(city);

        String greeting = "";

        if (weatherResponse != null) {
            greeting =
                    "Today feels like sunny : "
                            + weatherResponse.getCurrent().getFeelsLikeC();
        }

        return ResponseEntity.ok(
                "Hii " + authentication.getName() + " " + greeting
        );
    }

    @Operation(
            summary = "Find users above age",
            description = "Retrieve users who are above a specified age"
    )
    @ApiResponse(responseCode = "200", description = "Users found")
    @Parameter(name = "age", description = "Minimum age", required = true)
    @GetMapping("/age")
    public ResponseEntity<?> findByAge(@RequestParam int age) {

        List<User> users = userService.findUsersAboveAge(age);

        if (!users.isEmpty()) {
            return ResponseEntity.ok(users);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No users found above age " + age);
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieve all users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "404", description = "No users found")
    })
    @GetMapping
    public ResponseEntity<?> getAllEntries() {

        List<User> entries = userService.getAll();

        if (entries.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Not found");
        }

        return ResponseEntity.ok(entries);
    }

    @Operation(
            summary = "Get current user",
            description = "Retrieve details of the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "User found")
    @GetMapping("/me")
    public ResponseEntity<?> getByUserName() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String userName = authentication.getName();

        return ResponseEntity.ok(
                userService.findByUserName(userName)
        );
    }

    @Operation(
            summary = "Update user profile fully",
            description = "Perform a full update of the user profile"
    )
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @PutMapping("/update")
    public ResponseEntity<UserResponseDTO> updateUser(
            @RequestBody UserRequestDTO dto) {

        UserResponseDTO updatedUser =
                userService.fullUserUpdate(dto);

        return ResponseEntity.ok(updatedUser);
    }

    @Operation(
            summary = "Partial update user profile",
            description = "Perform a partial update of the user profile"
    )
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @PatchMapping("/update")
    public ResponseEntity<UserResponseDTO> partialUpdate(
            @RequestBody UserRequestDTO dto) {

        UserResponseDTO updated =
                userService.partialUserUpdate(dto);

        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Update authenticated user",
            description = "Update the authenticated user's details"
    )
    @ApiResponse(responseCode = "204", description = "Updated successfully")
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        User old = userService.findByName(username);

        old.setUserName(user.getUserName());
        old.setPassword(user.getPassword());

        userService.saveNewUser(old);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update user username by ID",
            description = "Update the username of a user by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @Parameter(name = "id", description = "User ID", required = true)
    @PatchMapping("/id/{id}")
    public ResponseEntity<?> updateUserName(
            @PathVariable ObjectId id,
            @RequestBody User user) {

        Optional<User> existing = userService.findById(id);

        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        User u = existing.get();

        u.setUserName(user.getUserName());

        userService.saveNewUser(u);

        return ResponseEntity.ok(u);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<?> deleteById(@PathVariable ObjectId id) {

        Optional<User> old = userService.findById(id);

        if (old.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Upload user profile photo",
            description = "Upload or update the authenticated user's profile photo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile photo uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping(
            value = "/profile-photo/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadProfilePhoto(
            @RequestPart("file")
            @Parameter(
                    description = "Profile photo file",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            MultipartFile file) {

        try {

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String userName = authentication.getName();

            User user = userService.findByName(userName);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please upload a valid file");
            }

            // Delete old photo if exists
            if (user.getProfilePhotoUrl() != null
                    && !user.getProfilePhotoUrl().isBlank()) {

                profilePhotoService.deleteProfilePhoto(
                        user.getProfilePhotoUrl()
                );
            }

            // Upload new photo
            String photoUrl =
                    profilePhotoService.uploadProfilePhoto(
                            file,
                            user.getId().toString()
                    );

            // Save URL in DB
            user.setProfilePhotoUrl(photoUrl);

            userService.saveNewUser(user);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Profile photo uploaded successfully",
                            "photoUrl", photoUrl
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "error", e.getMessage()
                            )
                    );
        }
    }

    @Operation(
            summary = "Get user profile with photo",
            description = "Retrieve authenticated user's profile including profile photo URL"
    )
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String userName = authentication.getName();

        User user = userService.findByName(userName);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        UserResponseDTO response = new UserResponseDTO();

        response.setUserName(user.getUserName());
        response.setEmail(user.getEmail());
        response.setRoles(user.getRoles());
        response.setProfilePhotoUrl(user.getProfilePhotoUrl());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete user profile photo",
            description = "Delete the authenticated user's profile photo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Profile photo deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/profile-photo")
    public ResponseEntity<?> deleteProfilePhoto() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String userName = authentication.getName();

        User user = userService.findByName(userName);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        if (user.getProfilePhotoUrl() != null
                && !user.getProfilePhotoUrl().isBlank()) {

            profilePhotoService.deleteProfilePhoto(
                    user.getProfilePhotoUrl()
            );
        }

        user.setProfilePhotoUrl(null);

        userService.saveNewUser(user);

        return ResponseEntity.ok(
                "Profile photo deleted successfully"
        );
    }
}

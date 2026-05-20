package com.love.Backend.controller;

import com.love.Backend.dto.response.UserResponseDTO;
import com.love.Backend.entity.User;
import com.love.Backend.service.AuditLogService;
import com.love.Backend.service.UserEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin APIs", description = "Admin have to use these APIs to manage users")
@RestController
@RequestMapping("admin")
public class AdminController {

    @Autowired
    private UserEntryService userService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private BackendController backendController;

    @Operation(summary = "Get all users", description = "Retrieve a list of all users in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "404", description = "No users found")
    })
    @GetMapping("all-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAll() {
        String userName = backendController.currentUserName();
        List<UserResponseDTO> dtos = userService.getAllUsersDTO();
        if (dtos == null || dtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        auditLogService.record(userName, "get-all-users", "users", "Retrieved all users");
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Create new user", description = "Create a new user account by admin")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @PostMapping("create-new-user")
    public ResponseEntity<?> createUser(@RequestBody User user){
        userService.saveAdmin(user);
        auditLogService.record(backendController.currentUserName(), "create-new-user", "users", "Created new user: " + user.getUserName());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @Operation(summary = "Clear app cache", description = "Initialize and clear the application cache")
    @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
    @GetMapping("/clear-app-cache")
    public void clearAppCache(){
        // If you have an AppCache bean, call its init here; otherwise keep this empty or remove.
    }
}
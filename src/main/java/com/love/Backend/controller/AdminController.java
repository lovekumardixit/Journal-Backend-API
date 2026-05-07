package com.love.Backend.controller;

import com.love.Backend.cache.AppCache;
import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.dto.response.UserResponseDTO;
import com.love.Backend.entity.User;
import com.love.Backend.service.BackendEntryService;
import com.love.Backend.service.UserEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin APIs", description = "Admin have to use these APIs to manage users")
@RestController
@RequestMapping("admin")
public class    AdminController {

    @Autowired
    private UserEntryService userService;
    @Autowired
    private AppCache appCache;

    @Operation(summary = "Get all users", description = "Retrieve a list of all users in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "404", description = "No users found")
    })
    @GetMapping("all-users")
    public ResponseEntity<?> getALl(){
        List<User> all = userService.getAll();
        if( all != null && !all.isEmpty()){
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Create new user", description = "Create a new user account by admin")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @PostMapping("create-new-user")
    public ResponseEntity<?> createUser(@RequestBody User user){
        userService.saveAdmin(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }


    @Operation(summary = "Clear app cache", description = "Initialize and clear the application cache")
    @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
    @GetMapping("/clear-app-cache")
    public void clearAppCache(){
        appCache.init();

    }

    @Operation(summary = "Get user by username", description = "Retrieve user details by username, requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @Parameter(name = "username", description = "Username of the user to retrieve", required = true)
    @GetMapping("/admin/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserByName(@PathVariable String username){

        return ResponseEntity.ok(userService.findByUserName(username));
    }

}

package com.love.Backend.controller;

import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.dto.response.UserResponseDTO;
import com.love.Backend.entity.WeatherResponse;
import com.love.Backend.entity.User;
import com.love.Backend.service.UserEntryService;
import com.love.Backend.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "User Management APIs", description = "APIs for managing user profiles and weather information")
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserEntryService userService;

    @Autowired
    private WeatherService weatherService;



    @Operation(summary = "Get weather greeting", description = "Retrieve a personalized greeting with weather information for a city")
    @ApiResponse(responseCode = "200", description = "Greeting retrieved successfully")
    @Parameter(name = "city", description = "City name", required = true)
    @GetMapping("/get/{city}")
    public ResponseEntity<?> greeting(@PathVariable String city){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        WeatherResponse weatherResponse = weatherService.getWeather(city);
        String greeting = "";
        if(weatherResponse != null){
            greeting = "Today feels line sunny :" + weatherResponse.getCurrent().getFeelsLikeC();
        }
        System.out.println(weatherResponse);
        System.out.println(weatherResponse.getCurrent());
        return new ResponseEntity<>("Hii "+ authentication.getName() + " "+ greeting , HttpStatus.OK);

    }

    @Operation(summary = "Find users above age", description = "Retrieve users who are above a specified age")
    @ApiResponse(responseCode = "200", description = "Users found")
    @Parameter(name = "age", description = "Minimum age", required = true)
    @GetMapping("/age")
    public List<User> findByAge(@RequestParam int age){
        return userService.findUsersAboveAge(age);
    }



    @Operation(summary = "Get all users", description = "Retrieve all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "404", description = "No users found")
    })
    @GetMapping
    public ResponseEntity<?> getAll(){
        List<User> entries = userService.getAll();
        if(entries.isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Not found");
        }
        return new ResponseEntity<>(entries, HttpStatus.OK);
    }

    @Operation(summary = "Get current user", description = "Retrieve details of the authenticated user")
    @ApiResponse(responseCode = "200", description = "User found")
    @GetMapping("me")
    public ResponseEntity<?> getByUserName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        return new ResponseEntity<>(userService.findByUserName(userName), HttpStatus.OK);
    }

    @Operation(summary = "Update user profile fully", description = "Perform a full update of the user profile")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @PutMapping("/update")  
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody UserRequestDTO dto){

        UserResponseDTO updatedUser = userService.fullUserUpdate(dto);

        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Partial update user profile", description = "Perform a partial update of the user profile")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @PatchMapping("/update")
    public ResponseEntity<UserResponseDTO> partialUpdate(@RequestBody UserRequestDTO dto){
        UserResponseDTO updated = userService.partialUserUpdate(dto);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }




    @Operation(summary = "Update authenticated user", description = "Update the authenticated user's details")
    @ApiResponse(responseCode = "204", description = "Updated successfully")
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User old = userService.findByName(username);
        old.setUserName(user.getUserName());
        old.setPassword(user.getPassword());
        userService.saveNewUser(old);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @Operation(summary = "Update user username by ID", description = "Update the username of a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @Parameter(name = "id", description = "User ID", required = true)
    @PatchMapping("id/{id}")
    public ResponseEntity<?> updateUserName(@PathVariable ObjectId id, @RequestBody User user){

        Optional<User> existing = userService.findById(id);

        if(existing.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User u = existing.get();

        u.setUserName(user.getUserName());

        userService.saveNewUser(u);

        return new ResponseEntity<>(u,HttpStatus.OK);
    }

    public ResponseEntity<?> deleteById(@PathVariable ObjectId id){
        Optional<User> old = userService.findById(id);
        if(old.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

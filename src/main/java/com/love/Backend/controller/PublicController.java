package com.love.Backend.controller;


import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.exception.UserAlreadyExistsException;
import com.love.Backend.kafka.event.UserEvent;
import com.love.Backend.kafka.producer.RegisterProducer;
import com.love.Backend.service.UserEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public APIs", description = "Publicly accessible APIs for user registration")
@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private UserEntryService userService;

    @Autowired
    private RegisterProducer kafkaProducerService;


    @Operation(summary = "Create new user", description = "Register a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "User already exists")
    })
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequestDTO dto) {

        UserRequestDTO user = userService.saveNewUser(dto);

        if(user == null){
            throw new UserAlreadyExistsException("User exists");
        }

        UserEvent event = new UserEvent();
        event.setId(user.getId());
        event.setUserName(user.getUserName());
        event.setEmail(user.getEmail());

        kafkaProducerService.sendUserEvent(event);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}

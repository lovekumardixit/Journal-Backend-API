package com.love.Backend.controller;


import com.love.Backend.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Email APIs", description = "APIs for sending emails")
@RestController
@RequestMapping("/mail")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Operation(summary = "Send test email", description = "Send a test email to a predefined address")
    @ApiResponse(responseCode = "200", description = "Email sent successfully")
    @GetMapping("/send")
    public String sendEmail(@RequestBody String to, String subject, String body){
        emailService.sendEmail(
                to, subject, body
        );
        return "Email sent";

    }
}

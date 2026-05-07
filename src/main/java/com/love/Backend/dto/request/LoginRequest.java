package com.love.Backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class LoginRequest {

    @NotBlank(message = "Username is required") 
    private String userName; 

    @NotBlank(message = "Password is required") 
    private String password; 
}

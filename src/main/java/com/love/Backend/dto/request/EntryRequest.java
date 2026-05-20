package com.love.Backend.dto.request;

import com.love.Backend.enums.Sentiment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
public class EntryRequest {

    @NotBlank(message = "Title is required") 
    @Size(max = 120, message = "Title must be less than 120 characters") 
    private String title; 

    @NotBlank(message = "Content is required") 
    @Size(max = 5000, message = "Content must be less than 5000 characters") 
    private String content; 

    private Sentiment sentiment; 
}

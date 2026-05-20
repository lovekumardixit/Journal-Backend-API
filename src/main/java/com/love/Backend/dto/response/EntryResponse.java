package com.love.Backend.dto.response;

import com.love.Backend.enums.Sentiment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter 
@Builder 
public class EntryResponse {

    private String id; 

    private String title; 

    private String content; 

    private LocalDateTime date; 

    private Sentiment sentiment; 

    private String attachmentUrl; 
}

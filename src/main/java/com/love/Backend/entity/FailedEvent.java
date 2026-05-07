package com.love.Backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "failed_events")
public class FailedEvent {
    @Id
    private  String id;

    private String topic;
    private String payload;
    private boolean sent;

    private LocalDateTime createdAt;



}

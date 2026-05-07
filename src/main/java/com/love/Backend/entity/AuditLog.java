package com.love.Backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter 
@Setter 
@Document(collection = "audit_logs") 
public class AuditLog {

    @Id 
    private ObjectId id; 

    private String username; 

    private String action; 

    private String resourceType; 

    private String resourceId; 

    private LocalDateTime createdAt; 
}

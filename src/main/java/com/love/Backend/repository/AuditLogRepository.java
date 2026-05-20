package com.love.Backend.repository;

import com.love.Backend.entity.AuditLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, ObjectId> {

    List<AuditLog> findByUsernameOrderByCreatedAtDesc(String username); 
}

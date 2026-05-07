package com.love.Backend.service;

import com.love.Backend.entity.AuditLog;
import com.love.Backend.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service 
public class AuditLogService {

    @Autowired 
    private AuditLogRepository auditLogRepository; 

    public void record(String username, String action, String resourceType, String resourceId) { 
        AuditLog log = new AuditLog(); 
        log.setUsername(username); 
        log.setAction(action); 
        log.setResourceType(resourceType); 
        log.setResourceId(resourceId); 
        log.setCreatedAt(LocalDateTime.now()); 
        auditLogRepository.save(log); 
    }

    public List<AuditLog> getLogsForUser(String username) { 
        return auditLogRepository.findByUsernameOrderByCreatedAtDesc(username); 
    }
}

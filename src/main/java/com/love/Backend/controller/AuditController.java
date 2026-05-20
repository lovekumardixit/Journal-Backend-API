package com.love.Backend.controller;

import com.love.Backend.entity.AuditLog;
import com.love.Backend.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Audit APIs", description = "APIs for user action audit logs") 
@RestController 
@RequestMapping("/audit") 
public class AuditController {

    @Autowired 
    private AuditLogService auditLogService; 

    @Operation(summary = "Get my audit logs", description = "Returns latest audit logs for authenticated user") 
    @GetMapping("/me") 
    public List<AuditLog> getMyAuditLogs() { 
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); 
        return auditLogService.getLogsForUser(authentication.getName()); 
    }
}

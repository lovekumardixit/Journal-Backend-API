package com.love.Backend.controller;


import com.love.Backend.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Configuration APIs", description = "APIs for retrieving configuration values")
@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private ConfigService service;

    @Operation(summary = "Get configuration value", description = "Retrieve the value of a configuration key")
    @ApiResponse(responseCode = "200", description = "Value retrieved successfully")
    @Parameter(name = "key", description = "Configuration key", required = true)
    @GetMapping
    public String getConfig(@RequestParam String key) {
        return service.getValue(key);
    }
}

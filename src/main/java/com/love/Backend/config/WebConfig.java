package com.love.Backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration 
public class WebConfig implements WebMvcConfigurer {

    @Autowired 
    private RateLimitInterceptor rateLimitInterceptor; 

    @Value("${app.upload.dir:uploads}") 
    private String uploadDir; 

    @Override 
    public void addInterceptors(InterceptorRegistry registry) { 
        registry.addInterceptor(rateLimitInterceptor) 
                .addPathPatterns("/**"); 
    }

    @Override 
    public void addResourceHandlers(ResourceHandlerRegistry registry) { 
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString(); 
        registry.addResourceHandler("/uploads/**").addResourceLocations(uploadPath); 
    }
}

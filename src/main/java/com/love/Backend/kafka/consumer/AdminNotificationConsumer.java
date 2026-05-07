package com.love.Backend.kafka.consumer;

import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.kafka.event.UserEvent;
import com.love.Backend.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AdminNotificationConsumer {

    @Autowired
    private IdempotencyService service;

    @KafkaListener(topics = "user_register", groupId = "admin.notification")
    public void adminNotification(UserEvent event){





        System.out.println("ALERT: New user" + event.getUserName()+" registerd");
    }
}
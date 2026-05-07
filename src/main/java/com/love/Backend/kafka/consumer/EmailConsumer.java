package com.love.Backend.kafka.consumer;


import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.kafka.event.UserEvent;
import com.love.Backend.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    @Autowired
    private IdempotencyService service;

    @KafkaListener(topics = "user_register", groupId = "email-group")
    public void emailSend(UserEvent event){




        System.out.println("Mail successfully sent to " + event.getEmail());
    }
}

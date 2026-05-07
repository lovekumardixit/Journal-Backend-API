package com.love.Backend.kafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.love.Backend.entity.FailedEvent;
import com.love.Backend.kafka.event.UserEvent;
import com.love.Backend.repository.FailedEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class KafkaProducerService {

    @Autowired
    private FailedEventRepository failedEventRepository;

    @Autowired
    private ObjectMapper objectMapper;


    public void saveEventToDB(UserEvent event){
        try{
            FailedEvent failed = new FailedEvent();
            failed.setTopic("user_register");
            failed.setPayload(objectMapper.writeValueAsString(event));
            failed.setSent(false);
            failed.setCreatedAt(LocalDateTime.now());
            failedEventRepository.save(failed);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

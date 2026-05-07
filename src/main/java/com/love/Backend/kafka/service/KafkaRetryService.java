package com.love.Backend.kafka.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.love.Backend.entity.FailedEvent;
import com.love.Backend.kafka.event.UserEvent;
import com.love.Backend.repository.FailedEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableScheduling
public class KafkaRetryService {

    @Autowired
    private FailedEventRepository repo;

    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    public void retryFailedEvents() {

        System.out.println("Retry job running...");

        
        if (!isKafkaAvailable()) {
            System.out.println("Kafka down, skipping...");
            return;
        }

        List<FailedEvent> list = repo.findBySentFalse();

        for (FailedEvent fe : list) {
            try {
                UserEvent event = objectMapper.readValue(fe.getPayload(), UserEvent.class);

                kafkaTemplate.send("user_register",event.getUserName(), event);

                fe.setSent(true);
                repo.save(fe);

                System.out.println("Retried success " + fe.getId());

            } catch (Exception e) {
                System.out.println("Retry failed again " + fe.getId());
            }
        }
    }

    
    private boolean isKafkaAvailable() {
        try {
            UserEvent dummy = new UserEvent();
            dummy.setUserName("Health-check");
            kafkaTemplate.send("health_check", dummy);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
package com.love.Backend.kafka.producer;

import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.kafka.event.UserEvent;
import com.love.Backend.kafka.service.KafkaProducerService;
import com.love.Backend.kafka.service.KafkaRetryService;
import com.love.Backend.repository.ProcessedEventRepo;
import com.love.Backend.repository.UserEntryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RegisterProducer {
    @Autowired
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Autowired
    private ProcessedEventRepo eventRepo;
    @Autowired
    private UserEntryRepo entryRepo;

    @Autowired
    private KafkaRetryService kafkaRetryService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    public void sendUserEvent(UserEvent event) {
        kafkaTemplate.send("user_register", event.getUserName(),  event)
                .whenComplete(
                        (result, ex) -> {
                            if(ex == null){
                                System.out.println("Event sent successfully");
                            }else{
                                System.out.println("Error event sending");
                                kafkaProducerService.saveEventToDB(event);
                            }
                        });

    }
}
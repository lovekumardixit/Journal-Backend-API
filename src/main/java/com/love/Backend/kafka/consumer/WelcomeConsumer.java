package com.love.Backend.kafka.consumer;

import com.love.Backend.dto.request.UserRequestDTO;
import com.love.Backend.exception.ConsumerException;
import com.love.Backend.kafka.event.UserEvent;
import com.love.Backend.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Constants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class WelcomeConsumer {
    @Autowired
    private IdempotencyService service;

    @KafkaListener(
            topics = "user_register",
            groupId = "welcome.msg") 
    public void welcome(@Payload UserEvent event,
                        Acknowledgment ack,
                        @Header(KafkaHeaders.RECEIVED_KEY) String key) {





        try {
            System.out.println("Thank you! "+event.getUserName()+ " for registering our service!" );
            System.out.println("Your key is : "+key); 
            ack.acknowledge();
        }catch (Exception e){
            throw new ConsumerException("Something bad wrong in consumer");
        }
    }
}

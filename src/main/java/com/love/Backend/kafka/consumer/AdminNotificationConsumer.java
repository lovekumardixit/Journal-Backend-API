package com.love.Backend.kafka.consumer;

import com.love.Backend.entity.ProcessedEvent;
import com.love.Backend.kafka.event.UserEvent;
import com.love.Backend.repository.ProcessedEventRepo;
import com.love.Backend.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class AdminNotificationConsumer {

    private final EmailService emailService;
    private final ProcessedEventRepo processedEventRepo;

    public AdminNotificationConsumer(EmailService emailService, ProcessedEventRepo processedEventRepo) {
        this.emailService = emailService;
        this.processedEventRepo = processedEventRepo;
    }

    @KafkaListener(topics = "user_register", groupId = "admin.notification")
    public void adminNotification(UserEvent event, Acknowledgment ack){
        if (event == null) {
            if (ack != null) ack.acknowledge();
            return;
        }

        String processedId;
        if (event.getId() != null) {
            processedId = event.getId().toHexString();
        } else {
            processedId = event.getUserName() + ":" + event.getEmail();
        }

        try {
            if (processedEventRepo.existsById(processedId)) {
                System.out.println("Skipping already processed admin notification: " + processedId);
                if (ack != null) ack.acknowledge();
                return;
            }

            emailService.sendAdminNotification(event);
            System.out.println("Admin notification triggered for " + event.getUserName());

            ProcessedEvent p = new ProcessedEvent();
            p.setEventId(processedId);
            processedEventRepo.save(p);

            if (ack != null) ack.acknowledge();
        } catch (Exception e) {
            System.err.println("Failed to process admin notification for " + processedId + " : " + e.getMessage());
            // Do not acknowledge so message can be retried
        }
    }
}
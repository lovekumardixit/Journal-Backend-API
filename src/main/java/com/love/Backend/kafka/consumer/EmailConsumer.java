package com.love.Backend.kafka.consumer;

import com.love.Backend.entity.ProcessedEvent;
import com.love.Backend.kafka.event.UserEvent;
import com.love.Backend.repository.ProcessedEventRepo;
import com.love.Backend.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    private final EmailService emailService;
    private final ProcessedEventRepo processedEventRepo;

    public EmailConsumer(EmailService emailService, ProcessedEventRepo processedEventRepo) {
        this.emailService = emailService;
        this.processedEventRepo = processedEventRepo;
    }

    @KafkaListener(topics = "user_register", groupId = "email-group")
    public void emailSend(UserEvent event, Acknowledgment ack){
        if (event == null) {
            if (ack != null) ack.acknowledge();
            return;
        }

        // Build a stable processed id (prefer event.id if present)
        String processedId = null;
        if (event.getId() != null) {
            processedId = event.getId().toHexString();
        } else {
            // fallback - combine username + email
            processedId = event.getUserName() + ":" + event.getEmail();
        }

        try {
            if (processedEventRepo.existsById(processedId)) {
                System.out.println("Skipping already processed email event: " + processedId);
                if (ack != null) ack.acknowledge();
                return;
            }

            if (event.getEmail() != null && !event.getEmail().isBlank()) {
                emailService.sendWelcomeEmail(event.getEmail(), event.getUserName());
                System.out.println("Triggered welcome mail to " + event.getEmail());
            } else {
                System.out.println("No email present for user " + event.getUserName());
            }

            // mark processed
            ProcessedEvent p = new ProcessedEvent();
            p.setEventId(processedId);
            processedEventRepo.save(p);

            if (ack != null) ack.acknowledge();
        } catch (Exception e) {
            System.err.println("Failed to process/wrap email event " + processedId + " : " + e.getMessage());
            // Do not acknowledge - will allow retry
        }
    }
}
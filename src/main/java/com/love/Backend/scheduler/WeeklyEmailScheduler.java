package com.love.Backend.scheduler;

import com.love.Backend.entity.User;
import com.love.Backend.repository.UserEntryRepo;
import com.love.Backend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Slf4j
public class WeeklyEmailScheduler {

    @Autowired
    private UserEntryRepo userRepo;

    @Autowired
    private EmailService emailService;


    @Scheduled(cron = "0 2 15 ? * SUN")
    public void sendWeeklyEmails() {

        try {
            List<User> users = userRepo.findAll();

            for (User user : users) {

                emailService.sendEmail(
                        user.getEmail(),
                        "Weekly Update 🚀",
                        "Hello " + user.getUserName() + ", this is your Sunday email!"
                );
            }

            System.out.println("Emails sent to all users!");
        } catch (Exception e){
            log.error("Error While sending mails");
        }
    }
}

package com.love.Backend.service;

import com.love.Backend.kafka.event.UserEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String adminEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(adminEmail, "Journal Service Team");
            helper.setSubject(subject);

            String htmlContent = buildProfessionalTemplate(
                    "Hello,",
                    body,
                    "Thank you for choosing Journal Service."
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Email sent successfully to " + to);

        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + " : " + e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String userName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(adminEmail, "Journal Service Team");
            helper.setSubject("🎉 Welcome to Journal Service - Your Account is Ready!");

            String body = String.format("""
                    Dear %s,
                    
                    We are delighted to welcome you to Journal Service!
                    
                    Your account has been successfully created, and you are now part of our growing community.
                    
                    With Journal Service, you can:
                    • Securely manage your journal entries
                    • Organize your thoughts anytime
                    • Enjoy a reliable and personalized experience
                    
                    If you have any questions or need assistance, our support team is always here to help.
                    """, userName);

            String htmlContent = buildProfessionalTemplate(
                    "Welcome Aboard!",
                    body,
                    "Best Regards,<br>Journal Service Team"
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Welcome mail sent to " + to);

        } catch (Exception e) {
            System.err.println("Failed to send welcome email to " + to + " : " + e.getMessage());
        }
    }

    @Async
    public void sendAdminNotification(UserEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(adminEmail);
            helper.setFrom(adminEmail, "Journal Service System");
            helper.setSubject("🚀 New User Registration Alert");

            String body = String.format("""
                    A new user has successfully registered on Journal Service.
                    
                    User Details:
                    • Username: %s
                    • Email: %s
                    • User ID: %s
                    
                    Password details are securely omitted.
                    """,
                    event.getUserName(),
                    event.getEmail(),
                    event.getId()
            );

            String htmlContent = buildProfessionalTemplate(
                    "New Registration Notification",
                    body,
                    "System Generated Alert"
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Admin notification sent to " + adminEmail);

        } catch (Exception e) {
            System.err.println("Failed to send admin notification : " + e.getMessage());
        }
    }

    private String buildProfessionalTemplate(String heading, String body, String footer) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background-color: #f4f4f4;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 30px auto;
                            background-color: #ffffff;
                            border-radius: 10px;
                            overflow: hidden;
                            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
                        }
                        .header {
                            background-color: #4CAF50;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            font-size: 24px;
                            font-weight: bold;
                        }
                        .content {
                            padding: 30px;
                            color: #333333;
                            line-height: 1.8;
                            white-space: pre-line;
                        }
                        .footer {
                            background-color: #f1f1f1;
                            padding: 20px;
                            text-align: center;
                            font-size: 14px;
                            color: #666666;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">%s</div>
                        <div class="content">%s</div>
                        <div class="footer">%s</div>
                    </div>
                </body>
                </html>
                """.formatted(heading, body, footer);
    }
}
package org.example.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.example.config.EmailConfig;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EmailService {

    private static final Properties props = new Properties();
    private static final ExecutorService emailExecutor = Executors.newFixedThreadPool(3);

    static {
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
        props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", EmailConfig.SMTP_HOST);

        // Additional debugging properties
        props.put("mail.debug", "false");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
    }

    // Send password email (existing method)
    public static void sendPasswordEmail(String recipientEmail, String generatedPassword) {
        String subject = "Your Account Password";
        String htmlContent = String.format(
                "<html>" +
                        "<body>" +
                        "<h2>Welcome to Event Token System</h2>" +
                        "<p>Your account has been created successfully.</p>" +
                        "<p><strong>Login credentials:</strong></p>" +
                        "<ul>" +
                        "<li>Email: %s</li>" +
                        "<li>Password: %s</li>" +
                        "</ul>" +
                        "<p style='color: red;'>Please change your password after first login.</p>" +
                        "<p>Best regards,<br>Event Token System Team</p>" +
                        "</body>" +
                        "</html>",
                recipientEmail, generatedPassword
        );

        sendHtmlEmail(recipientEmail, subject, htmlContent);
    }

    // IMPROVED: Send plain text email with better error handling
    public static void send(String to, String subject, String content) {
        emailExecutor.submit(() -> {
            try {
                System.out.println("📧 Attempting to send email to: " + to);

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                EmailConfig.USERNAME,
                                EmailConfig.PASSWORD
                        );
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EmailConfig.FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(content);

                // Add sent timestamp
                message.setSentDate(new java.util.Date());

                Transport.send(message);
                System.out.println("✅ Email sent successfully to: " + to);
            } catch (MessagingException e) {
                System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
                e.printStackTrace();

                // Log specific error types
                if (e instanceof AuthenticationFailedException) {
                    System.err.println("🔐 Authentication failed - check email credentials");
                } else if (e instanceof SendFailedException) {
                    System.err.println("📤 Send failed - check recipient address");
                } else if (e instanceof MessagingException) {
                    System.err.println("📧 Messaging error - check SMTP configuration");
                }
            } catch (Exception e) {
                System.err.println("💥 Unexpected error sending email: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Send HTML email (improved method)
    public static void sendHtmlEmail(String recipientEmail, String subject, String htmlContent) {
        emailExecutor.submit(() -> {
            try {
                System.out.println("📧 Attempting to send HTML email to: " + recipientEmail);

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                EmailConfig.USERNAME,
                                EmailConfig.PASSWORD
                        );
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EmailConfig.FROM));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setContent(htmlContent, "text/html; charset=utf-8");
                message.setSentDate(new java.util.Date());

                Transport.send(message);
                System.out.println("✅ HTML email sent successfully to: " + recipientEmail);
            } catch (MessagingException e) {
                System.err.println("❌ Failed to send HTML email to " + recipientEmail + ": " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("💥 Unexpected error sending HTML email: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Send reset code email (specific method for password reset)
    public static void sendResetCode(String email, String code) {
        String subject = "Password Reset Code - Event Token System";
        String htmlContent = String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                        "<h2 style='color: #f39c12;'>Password Reset Request</h2>" +
                        "<p>You have requested to reset your password. Please use the verification code below:</p>" +
                        "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 5px; text-align: center; margin: 20px 0;'>" +
                        "<h1 style='color: #f39c12; font-size: 32px; margin: 0; letter-spacing: 5px;'>%s</h1>" +
                        "</div>" +
                        "<p><strong>This code will expire in 10 minutes.</strong></p>" +
                        "<p>If you did not request this password reset, please ignore this email.</p>" +
                        "<p>Best regards,<br>Event Token System Team</p>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                code
        );

        sendHtmlEmail(email, subject, htmlContent);
    }

    // IMPROVED: Test email connectivity with detailed logging
    public static boolean testEmailConnection() {
        try {
            System.out.println("🔍 Testing email connection...");
            System.out.println("SMTP Host: " + EmailConfig.SMTP_HOST);
            System.out.println("SMTP Port: " + EmailConfig.SMTP_PORT);
            System.out.println("Username: " + EmailConfig.USERNAME);
            System.out.println("From: " + EmailConfig.FROM);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            EmailConfig.USERNAME,
                            EmailConfig.PASSWORD
                    );
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();

            System.out.println("✅ Email connection test successful");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Email connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // NEW: Send a test email
    public static void sendTestEmail(String recipientEmail) {
        String subject = "Test Email - Event Token System";
        String content = "This is a test email to verify the email configuration is working properly.\n\n" +
                "If you received this email, the system is configured correctly.\n\n" +
                "Best regards,\nEvent Token System Team";

        send(recipientEmail, subject, content);
    }

    // Shutdown the executor properly
    public static void shutdown() {
        emailExecutor.shutdown();
    }
}
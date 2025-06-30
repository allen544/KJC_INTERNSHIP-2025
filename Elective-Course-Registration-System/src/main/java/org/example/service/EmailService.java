package org.example.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "24mcab06@kristujayanti.com"; // your Gmail address
    private static final String APP_PASSWORD = "wded pgel mpfx hmue"; // Gmail App Password

    public static void sendPasswordEmail(String toEmail, String studentName, String password) {
        // SMTP Properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Auth session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject("Welcome to Elective Course System");
            message.setText("Hi " + studentName + ",\n\nYour registration was successful.\n" +
                    "Your password is: " + password + "\n\nRegards,\nElective System Team");

            Transport.send(message);
            System.out.println("📧 Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.out.println("❌ Email sending failed: " + e.getMessage());
        }
    }
}

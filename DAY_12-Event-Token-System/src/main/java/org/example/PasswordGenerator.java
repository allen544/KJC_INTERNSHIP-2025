package org.example;

import org.example.security.PasswordUtil;

public class PasswordGenerator {
    public static void main(String[] args) {
        String password = "admin123";
        String hash = PasswordUtil.hashPassword(password);
        System.out.println("Password: " + password);
        System.out.println("New Hash: " + hash);
    }
}
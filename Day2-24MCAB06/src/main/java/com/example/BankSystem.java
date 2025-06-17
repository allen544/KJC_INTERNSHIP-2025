package com.example;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.Scanner;

public class BankSystem {

    static Scanner scanner = new Scanner(System.in);
    static MongoClient client = MongoClients.create("mongodb://localhost:27017");
    static MongoDatabase database = client.getDatabase("bankdbs");
    static MongoCollection<Document> collection = database.getCollection("accounts");

    public static void main(String[] args) {

        while (true) {
            System.out.println("\n--- Banking Menu ---");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Check Balance");
            System.out.println("5. Exit");
            System.out.print("Choose: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> createAccount();
                case 2 -> deposit();
                case 3 -> withdraw();
                case 4 -> checkBalance();
                case 5 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void createAccount() {
        System.out.print("Enter account number: ");
        String accNo = scanner.next();
        System.out.print("Enter name: ");
        String name = scanner.next();

        if (collection.find(new Document("accNo", accNo)).first() != null) {
            System.out.println("Account already exists.");
        } else {
            Document doc = new Document("accNo", accNo)
                    .append("name", name)
                    .append("balance", 0.0);
            collection.insertOne(doc);
            System.out.println("Account created and stored in DB.");
        }
    }

    static void deposit() {
        System.out.print("Enter account number: ");
        String accNo = scanner.next();

        Document acc = collection.find(new Document("accNo", accNo)).first();
        if (acc == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Enter deposit amount: ");
        double amount = scanner.nextDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount.");
            return;
        }

        double newBalance = acc.getDouble("balance") + amount;
        collection.updateOne(new Document("accNo", accNo),
                new Document("$set", new Document("balance", newBalance)));

        System.out.println("Deposit successful. New Balance: " + newBalance);
    }

    static void withdraw() {
        System.out.print("Enter account number: ");
        String accNo = scanner.next();

        Document acc = collection.find(new Document("accNo", accNo)).first();
        if (acc == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Enter withdrawal amount: ");
        double amount = scanner.nextDouble();

        double currentBalance = acc.getDouble("balance");

        if (amount <= 0 || amount > currentBalance) {
            System.out.println("Invalid or insufficient funds.");
            return;
        }

        double newBalance = currentBalance - amount;
        collection.updateOne(new Document("accNo", accNo),
                new Document("$set", new Document("balance", newBalance)));

        System.out.println("Withdrawal successful. New Balance: " + newBalance);
    }

    static void checkBalance() {
        System.out.print("Enter account number: ");
        String accNo = scanner.next();

        Document acc = collection.find(new Document("accNo", accNo)).first();
        if (acc == null) {
            System.out.println("Account not found.");
        } else {
            System.out.println("Account Holder: " + acc.getString("name"));
            System.out.println("Balance: " + acc.getDouble("balance"));
        }
    }
}
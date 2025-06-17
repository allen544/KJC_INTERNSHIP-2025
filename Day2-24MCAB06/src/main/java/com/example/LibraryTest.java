package com.example;

public class LibraryTest {

    // Base class
    static class Book {
        String title;
        String author;

        Book(String title, String author) {
            this.title = title;
            this.author = author;
        }

        void showDetails() {
            System.out.println("Title: " + title);
            System.out.println("Author: " + author);
        }
    }

    // Subclass for Fiction
    static class FictionBook extends Book {
        FictionBook(String title, String author) {
            super(title, author);
        }

        @Override
        void showDetails() {
            System.out.println("[Fiction Book]");
            super.showDetails();
        }
    }

    // Subclass for Non-Fiction
    static class NonFictionBook extends Book {
        NonFictionBook(String title, String author) {
            super(title, author);
        }

        @Override
        void showDetails() {
            System.out.println("[Non-Fiction Book]");
            super.showDetails();
        }
    }

    // Main method
    public static void main(String[] args) {
        FictionBook fb = new FictionBook("Harry Potter", "J.K. Rowling");
        NonFictionBook nfb = new NonFictionBook("Wings of Fire", "A.P.J Abdul Kalam");

        fb.showDetails();
        System.out.println();
        nfb.showDetails();
    }
}
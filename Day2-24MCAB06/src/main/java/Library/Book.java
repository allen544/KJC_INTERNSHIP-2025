package Library;

public class Book {
    protected String title;
    protected String author;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;

    }

    public void displayInfo() {
        System.out.println("Title: " + title + ", Author: " + author);
    }
}

package Library;

public class LibraryMain {
    public static void main(String[] args) {
        Book book1 = new FictionBook("The Hobbit", "J.R.R. Tolkien", "Fantasy");
        Book book2 = new NonFictionBook("Brief History of Time", "Stephen Hawking", "Science");

        book1.displayInfo();
        book2.displayInfo();
    }
}
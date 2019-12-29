package com.periphery.littlefreelibrary;

import java.io.Serializable;

public class Book implements Serializable {
    String title;
    String author;
    String genre;
    String image;
    String book_id;
    String description;
    int numCopies;

    public Book(String book_id, String image, String title, String author, String genre, String description, int numCopies) {
        this.book_id = book_id;
        this.title = title;
        this.author = author;
        this.image = image;
        this.genre = genre;
        this.description = description;
        this.numCopies = numCopies;
    }

    public int getNumCopies() {
        return numCopies;
    }

    public void setNumCopies(int numCopies) {
        this.numCopies = numCopies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", image='" + image.substring(0, 10) + "..." + '\'' +
                ", book_id='" + book_id + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

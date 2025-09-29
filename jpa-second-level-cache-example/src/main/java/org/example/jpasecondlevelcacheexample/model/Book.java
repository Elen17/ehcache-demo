package org.example.jpasecondlevelcacheexample.model;

import jakarta.persistence.*;

@Entity
@Cacheable  // Enable 2nd level caching
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    public Book() {}
    public Book(String title) { this.title = title; }

    @Override
    public String toString() {
        return "Book{id=" + id + ", title='" + title + "'}";
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}

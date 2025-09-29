package com.example.jcache.writer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Book {
    private final int id;
    @Setter
    private String title;
    @Setter
    private String author;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

}

package org.example.jpasecondlevelcacheexample;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.jpasecondlevelcacheexample.model.Book;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JpaSecondLevelCacheExample {
    private static final Logger LOGGER = Logger.getLogger(JpaSecondLevelCacheExample.class.toString());

    public static void main(String[] args) {

//        // Setup JPA EntityManagerFactory (Hibernate 6)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("demoPU");

        // Persist a book
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Book book = new Book("Hibernate Programmatic Cache");
        em.persist(book);
        em.getTransaction().commit();
        em.close();

        Long bookId = book.getId();

        // First fetch -> hits DB
        EntityManager em1 = emf.createEntityManager();
        Book b1 = em1.find(Book.class, bookId);
        LOGGER.log(Level.INFO, "First fetch: {0}", b1);
        em1.close();

        // Second fetch -> should hit 2nd-level cache
        EntityManager em2 = emf.createEntityManager();
        Book b2 = em2.find(Book.class, bookId);
        LOGGER.log(Level.INFO, "Second fetch (from cache): {0}", b2);
        em2.close();

        emf.close();
    }
}

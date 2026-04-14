package com.example.bookstore.ejb;

import com.example.bookstore.entity.Book;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class BookService {

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    public List<Book> findAllBooks() {
        return em.createQuery("SELECT b FROM Book b ORDER BY b.title", Book.class)
                .getResultList();
    }

    public Book findBookById(Long id) {
        return em.find(Book.class, id);
    }

    public void saveBook(Book book) {
        if (book.getId() == null) {
            em.persist(book);
        } else {
            em.merge(book);
        }
    }

    public void deleteBook(Long id) {
        Book book = em.find(Book.class, id);
        if (book != null) {
            em.remove(book);
        }
    }
}

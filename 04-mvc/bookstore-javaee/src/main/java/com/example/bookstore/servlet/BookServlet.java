package com.example.bookstore.servlet;

import com.example.bookstore.ejb.BookService;
import com.example.bookstore.entity.Book;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/books")
public class BookServlet extends HttpServlet {

    @EJB
    private BookService bookService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Book> books = bookService.findAllBooks();
        req.setAttribute("books", books);
        req.getRequestDispatcher("/books.jsp").forward(req, resp);
    }
}

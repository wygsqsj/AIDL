package com.crfchina.server;
import com.crfchina.server.Book;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
}

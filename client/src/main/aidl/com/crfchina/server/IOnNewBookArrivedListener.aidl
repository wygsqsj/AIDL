package com.crfchina.server;
import com.crfchina.server.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}

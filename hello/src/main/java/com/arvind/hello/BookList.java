package com.arvind.hello;

import java.util.ArrayList;
import java.util.List;

public class BookList {

	public BookList() {
		super();
		this.books = new ArrayList<>();
	}

	private List<Book> books;
 
    public void addBook(Book book) {
        this.books.add(book);
    }

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}
}
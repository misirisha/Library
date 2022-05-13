package com.example.library.repos;

import com.example.library.accessingdatah2.Book;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookRepository extends CrudRepository<Book, Integer> {
    List<Book> findByTitleContaining (String text);
    List<Book> findByAuthorContaining (String text);
}

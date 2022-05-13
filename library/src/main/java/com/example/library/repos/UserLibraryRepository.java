package com.example.library.repos;


import com.example.library.accessingdatah2.UserLibrary;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserLibraryRepository extends CrudRepository<UserLibrary, Integer> {
    List<UserLibrary> findByUserID(Integer id);
    List<UserLibrary> findByBookID(Integer id);
}

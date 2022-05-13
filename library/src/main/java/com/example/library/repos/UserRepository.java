package com.example.library.repos;

import com.example.library.accessingdatah2.AppUser;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<AppUser, Integer> {
    List<AppUser> findByLoginContaining (String text);
    List<AppUser> findByLogin(String text);
    List<AppUser> findByPassword(String text);
}

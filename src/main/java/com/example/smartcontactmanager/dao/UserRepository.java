package com.example.smartcontactmanager.dao;

import com.example.smartcontactmanager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Integer> {

    @Query(value = "select * from user where email =:email", nativeQuery = true)
    public User getUserByUserName(@Param("email") String email);

}

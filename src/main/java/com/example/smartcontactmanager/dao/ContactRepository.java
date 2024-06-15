package com.example.smartcontactmanager.dao;

import com.example.smartcontactmanager.entities.Contact;
import com.example.smartcontactmanager.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact,Integer> {

//    Pagination

    /*
        Pageable take two thing when we create object of it.
        currentPage
        number of page we want to show
     */
    @Query(value = "select * from contact where user_id =:userId order by name", nativeQuery = true)
    public Page<Contact> findAllContactsByUserId(@Param("userId") int userId, Pageable pageable);

    // search
    public List<Contact> findByNameContainingAndUser(String name, User user);

}

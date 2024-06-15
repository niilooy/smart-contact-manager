package com.example.smartcontactmanager.dao;

import com.example.smartcontactmanager.entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders,Integer> {

    public Orders findByOrderId(String order_id);

}

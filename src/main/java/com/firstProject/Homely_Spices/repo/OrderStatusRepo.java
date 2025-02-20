package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusRepo extends JpaRepository<OrderStatus,Integer> {
    OrderStatus findByStatus(String status);
}

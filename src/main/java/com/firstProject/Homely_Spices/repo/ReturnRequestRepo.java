package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRequestRepo extends JpaRepository<ReturnRequest,Integer> {
    List<ReturnRequest> findByOrderId(int orderId);
}

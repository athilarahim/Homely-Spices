package com.firstProject.Homely_Spices.service;

import com.firstProject.Homely_Spices.DTO.ReturnRequestDTO;
import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.OrderItemsRepo;
import com.firstProject.Homely_Spices.repo.OrderRepo;
import com.firstProject.Homely_Spices.repo.OrderStatusRepo;
import com.firstProject.Homely_Spices.repo.ReturnRequestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReturnRequestService {

    @Autowired
    private ReturnRequestRepo returnRequestRepository;

    @Autowired
    private OrderRepo orderRepository;

    @Autowired
    private OrderItemsRepo orderItemRepository;

    @Autowired
    private OrderStatusRepo orderStatusRepo;


    public void createReturnRequests(List<ReturnRequestDTO> requestDTOs) {
        for (ReturnRequestDTO requestDTO : requestDTOs) {
            Orders order = orderRepository.findById(requestDTO.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            OrderItems orderItem = orderItemRepository.findById(requestDTO.getOrderItemId())
                    .orElseThrow(() -> new RuntimeException("Order item not found"));

            ReturnRequest returnRequest = new ReturnRequest();
            returnRequest.setOrder(order);
            returnRequest.setOrderItem(orderItem);
            returnRequest.setReturnDate(LocalDate.now());
            returnRequest.setStatus(ReturnStatus.PENDING);
            returnRequest.setReason(requestDTO.getReason());
            returnRequest.setComment(requestDTO.getComment());

            OrderStatus returnStatus = new OrderStatus();
            returnStatus.setStatus("Return Pending");
            returnStatus.setStatusChangeDate(LocalDateTime.now());
            returnStatus.setOrder(order);
            order.setLatestStatus(returnStatus);

            List<OrderStatus> orderStatuses = new ArrayList<>();
            orderStatuses.add(returnStatus);
            order.setOrderStatuses(orderStatuses);

            orderStatusRepo.save(returnStatus);
            orderRepository.save(order);
            returnRequestRepository.save(returnRequest);
        }
    }
}


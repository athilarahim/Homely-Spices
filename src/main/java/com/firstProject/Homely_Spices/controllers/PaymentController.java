package com.firstProject.Homely_Spices.controllers;
import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.OrderRepo;
import com.firstProject.Homely_Spices.repo.UserRepo;
import com.firstProject.Homely_Spices.service.PaymentService;
import com.firstProject.Homely_Spices.service.UserProfileService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    OrderRepo orderRepo;

    @Autowired
    UserProfileService userProfileService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/razorpay/order")
    public Map<String, Object> createRazorpayOrder(@RequestParam double amount) {
        try {
            String orderDetails = paymentService.createOrder(amount);
            return Map.of("success", true, "order", orderDetails);
        } catch (RazorpayException e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }


}

package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.DTO.ReturnRequestDTO;
import com.firstProject.Homely_Spices.service.ReturnRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/returns")
public class ReturnRequestController {

    @Autowired
    private ReturnRequestService returnRequestService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitReturnRequest(@RequestBody List<ReturnRequestDTO> returnRequestDTOs) {
        returnRequestService.createReturnRequests(returnRequestDTOs);
        return ResponseEntity.ok("Return request submitted successfully.");
    }
}


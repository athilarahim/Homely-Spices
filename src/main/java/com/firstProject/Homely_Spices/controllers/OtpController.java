package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.service.MailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/resendOtp")
public class OtpController {

    @Autowired
    private MailService mailService;

    @PostMapping
    public ResponseEntity<String> resendOtp(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        mailService.resendOtp(email, session);
        return ResponseEntity.ok("OTP has been resent successfully.");
    }
}

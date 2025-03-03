package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.model.Coupon;
import com.firstProject.Homely_Spices.repo.CouponRepo;
import com.firstProject.Homely_Spices.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepo couponRepo;

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @RequestParam String code,
            @RequestParam double cartTotal) {

        String validationMessage = couponService.validateCoupon(code, cartTotal);
        Map<String, Object> response = new HashMap<>();

        if (validationMessage.equals("VALID")) {
            Optional<Coupon> coupon = couponRepo.findByCode(code);
            double updatedCartTotal = cartTotal - ((cartTotal * coupon.get().getDiscount()) / 100);
            double couponDiscount = (cartTotal * coupon.get().getDiscount()) / 100;
            response.put("message", "Coupon applied successfully!");
            response.put("updatedCartTotal", updatedCartTotal);
            response.put("couponDiscount",couponDiscount);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", validationMessage);
            response.put("updatedCartTotal", cartTotal);
            response.put("couponDiscount", 0.0);
            return ResponseEntity.badRequest().body(response);
        }
    }

}

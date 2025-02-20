package com.firstProject.Homely_Spices.service;
import com.firstProject.Homely_Spices.model.Coupon;
import com.firstProject.Homely_Spices.model.CouponStatus;
import com.firstProject.Homely_Spices.repo.CouponRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepo couponRepository;


    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public void createCoupon(Coupon coupon) {
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new RuntimeException("Coupon code already exists!");
        }
        couponRepository.save(coupon);
    }

    public void deleteCoupon(int id) {
        couponRepository.deleteById(id);
    }

    public String validateCoupon(String code, double cartTotal) {
        Optional<Coupon> optionalCoupon = couponRepository.findByCode(code);

        if (optionalCoupon.isEmpty()) {
            return "Invalid coupon code!";
        }

        Coupon coupon = optionalCoupon.get();
        LocalDate today = LocalDate.now();

        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            return "This coupon is inactive!";
        }
        if (today.isBefore(coupon.getStartDate())) {
            return "This coupon is not active yet!";
        }
        if (today.isAfter(coupon.getEndDate())) {
            return "This coupon has expired!";
        }
        if (cartTotal < coupon.getMinPrice()) {
            return "Your order does not meet the minimum price for this coupon!";
        }

        return "VALID";
    }
}
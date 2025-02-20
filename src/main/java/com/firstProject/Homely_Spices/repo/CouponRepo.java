package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepo extends JpaRepository<Coupon, Integer> {
    boolean existsByCode(String code);

    Optional<Coupon> findByCode(String code);
}

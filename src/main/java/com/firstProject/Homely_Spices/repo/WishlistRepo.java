package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepo extends JpaRepository<Wishlist, Integer> {

    List<Wishlist> findByUserId(int userId);

    void deleteByUserIdAndProductId(int userId, int productId);

    boolean existsByUserIdAndProductId(int userId, int productId);
}
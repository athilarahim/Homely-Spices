package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Cart;
import com.firstProject.Homely_Spices.model.Product;
import com.firstProject.Homely_Spices.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart,Integer> {

    @Query("SELECT c FROM Cart c WHERE c.user = :user AND c.product = :product AND c.productPrice = :productPrice")
    Optional<Cart> findByUserProductAndPrice(@Param("user") Users user,
                                             @Param("product") Product product,
                                             @Param("productPrice") Double productPrice);

    List<Cart> findByUser_Id(int userId);

    List<Cart> findAllByIdInAndUserUsername(List<Integer> itemIds, String username);

}

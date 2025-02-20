package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Quantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuantityRepo extends JpaRepository<Quantity,Integer> {
    @Query("SELECT q FROM Quantity q WHERE q.product.id = :productId AND q.quantity = :quantity")
    Quantity findByProductIdAndQuantity(@Param("productId") int productId, @Param("quantity") String quantity);

    List<Quantity> findAllByOrderByStockAsc();
}

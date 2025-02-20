package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.OrderItems;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderItemsRepo extends JpaRepository<OrderItems, Integer> {

    @Query("SELECT SUM(oi.itemcount) FROM OrderItems oi WHERE oi.order.orderDate BETWEEN :startDate AND :endDate")
    Integer getTotalProductsSold(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT oi.product, SUM(oi.itemcount) AS totalSold " +
            "FROM OrderItems oi " +
            "GROUP BY oi.product " +
            "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);

    @Query("SELECT oi.product.category.categoryname, SUM(oi.itemcount) as totalSold " +
            "FROM OrderItems oi " +
            "GROUP BY oi.product.category.categoryname " +
            "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingCategories(Pageable pageable);
}

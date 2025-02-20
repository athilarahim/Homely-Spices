package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Category;
import com.firstProject.Homely_Spices.model.Product;
import com.firstProject.Homely_Spices.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product,Integer> {
    @Query
            ("SELECT p FROM Product p WHERE p.category.id=1 and p.deletedAt IS NULL and p.category.deletedAt IS NULL")
    List<Product> getSpices();

    @Query
            ("SELECT p FROM Product p WHERE p.category.id=2 and p.deletedAt IS NULL and p.category.deletedAt IS NULL")
    List<Product> getFlours();

    @Query
            ("SELECT p FROM Product p WHERE p.category.id=3 and p.deletedAt IS NULL and p.category.deletedAt IS NULL")
    List<Product> getCombos();

    @Query
            ("SELECT p FROM Product p WHERE p.category.id=(SELECT p2.category.id FROM Product p2 WHERE p2.id=:id) AND p.id != :id AND p.deletedAt IS NULL AND p.category.deletedAt IS NULL")
    List<Product> similarProducts(@Param("id") int id);

    @Query
            ("SELECT u FROM Product u WHERE u.name LIKE %:keyword%")
    List<Product> searchProduct(String keyword);

    @Query("SELECT c FROM Product c WHERE c.deletedAt IS NULL AND c.category.deletedAt IS NULL")
    List<Product> findActiveProducts();

    @Modifying
    @Transactional
    @Query
            ("UPDATE Product c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void softDeleteProduct(@Param("id") int id);

    @Query("SELECT p FROM Product p JOIN p.category c JOIN p.quantities q " +
            "WHERE (:categories IS NULL OR c.categoryname IN :categories) " +
            "AND (:minPrice IS NULL OR :maxPrice IS NULL OR q.price BETWEEN :minPrice AND :maxPrice) " +
            "AND (:quantities IS NULL OR q.quantity IN :quantities)" +
            "AND (p.deletedAt IS NULL)")
    List<Product> filterProducts(
            @Param("categories") List<String> categories,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("quantities") List<String> quantities
    );

    List<Product> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String productName);
}

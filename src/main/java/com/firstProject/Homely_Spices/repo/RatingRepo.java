package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepo extends JpaRepository<Rating, Integer> {
    @Query
            ("SELECT COALESCE(AVG(r.rating), 0) FROM Rating r WHERE r.product_id = :productId")
    Double findAverageRatingByProductId(@Param("productId") int productId);

    @Query
            ("SELECT COALESCE(COUNT(r), 0) FROM Rating r WHERE r.product_id = :productId")
    int countRatingsByProductId(@Param("productId") int productId);
}

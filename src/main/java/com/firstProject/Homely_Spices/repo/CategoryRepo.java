package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Category;
import com.firstProject.Homely_Spices.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CategoryRepo extends JpaRepository<Category,Integer> {

    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
    List<Category> findActiveCategories();

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void softDeleteCategory(@Param("id") int id);

    @Query
            ("SELECT u FROM Category u WHERE u.categoryname LIKE %:keyword%")
    List<Category> searchCategory(String keyword);

    boolean existsByCategoryname(String categoryname);
}

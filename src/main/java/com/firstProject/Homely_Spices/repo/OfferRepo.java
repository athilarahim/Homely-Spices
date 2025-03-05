package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Category;
import com.firstProject.Homely_Spices.model.Offer;
import com.firstProject.Homely_Spices.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepo extends JpaRepository<Offer, Integer> {
    List<Offer> findByProductAndActive(Product product, Boolean active);
    List<Offer> findByCategoryAndActive(Category category, Boolean active);
}

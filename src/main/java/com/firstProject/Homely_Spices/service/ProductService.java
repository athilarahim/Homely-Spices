package com.firstProject.Homely_Spices.service;

import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private QuantityRepo quantityRepo;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private OfferRepo offerRepository;

    public List<Product> getAllProducts() {
        return productRepo.findActiveProducts();
    }

    public List<Product> getSpices() {
        return productRepo.getSpices();
    }

    public List<Product> getFlours() {
        return productRepo.getFlours();
    }

    public List<Product> getCombos() {
        return productRepo.getCombos();
    }

    public List<Quantity> getAllQuantity() {
        return quantityRepo.findAll();
    }

    public List<Category> getAllCategory() {
        return categoryRepo.findActiveCategories();
    }

    public Product getProductById(int id) {
        return productRepo.findById(id).orElseThrow(() -> new RuntimeException("No product found"));
    }

    public List<Product> getSimilarProducts(int id) {
        return productRepo.similarProducts(id);
    }

    @Autowired
    private RatingRepo ratingRepo;

    public void saveRating(int productId, int userId, int rating) {
        Rating productRating = new Rating();
        productRating.setProduct_id(productId);
        productRating.setUser_id(userId);
        productRating.setRating(rating);
        ratingRepo.save(productRating);
    }

    public Double getAverageRating(int productId) {
        return ratingRepo.findAverageRatingByProductId(productId);
    }

    public int getRatingCount(int productId) {
        return ratingRepo.countRatingsByProductId(productId);
    }

    //filter the products
    public List<Product> filterProducts(List<String> categories, Double minPrice, Double maxPrice, List<String> quantities) {
        return productRepo.filterProducts(categories, minPrice, maxPrice, quantities);
    }

    public List<Product> searchWithinFilteredProducts(List<Product> filteredProducts, String query) {
        return filteredProducts.stream()
                .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()) ||
                        product.getDescription().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Quantity> getDiscountedQuantities(Product product) {
        List<Quantity> updatedQuantities = new ArrayList<>();

        // Fetch product & category offers
        List<Offer> productOffers = offerRepository.findByProductAndActive(product, true);
        List<Offer> categoryOffers = offerRepository.findByCategoryAndActive(product.getCategory(), true);

        double discountPercentage = 0.0;

        // Get the highest discount available
        for (Offer offer : productOffers) {
            if (isOfferValid(offer)) {
                discountPercentage = Math.max(discountPercentage, offer.getDiscountPercentage());
            }
        }
        for (Offer offer : categoryOffers) {
            if (isOfferValid(offer)) {
                discountPercentage = Math.max(discountPercentage, offer.getDiscountPercentage());
            }
        }

        // Apply the discount to each quantity
        for (Quantity quantity : product.getQuantities()) {
            double originalPrice = quantity.getPrice();
            double discountedPrice = originalPrice - (originalPrice * discountPercentage / 100);
            discountedPrice = Math.round(discountedPrice * 10.0) / 10.0;

            Quantity updatedQuantity = new Quantity();
            updatedQuantity.setId(quantity.getId());
            updatedQuantity.setProduct(quantity.getProduct());
            updatedQuantity.setQuantity(quantity.getQuantity());
            updatedQuantity.setPrice(discountedPrice); // Apply new price
            updatedQuantity.setStock(quantity.getStock());

            updatedQuantities.add(updatedQuantity);
        }

        return updatedQuantities;
    }

    private boolean isOfferValid(Offer offer) {
        return offer.getStartDate().isBefore(LocalDate.now()) && offer.getEndDate().isAfter(LocalDate.now());
    }
}


package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.model.Cart;
import com.firstProject.Homely_Spices.model.Product;
import com.firstProject.Homely_Spices.model.Quantity;
import com.firstProject.Homely_Spices.repo.ProductRepo;
import com.firstProject.Homely_Spices.repo.QuantityRepo;
import com.firstProject.Homely_Spices.repo.RatingRepo;
import com.firstProject.Homely_Spices.service.CartService;
import com.firstProject.Homely_Spices.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController// Use RestController for JSON responses
public class ProductApiController {

    @Autowired
    QuantityRepo quantityRepo;

    @Autowired
    ProductService productService;

    @Autowired
    CartService cartService;

    @GetMapping("/api/productDetails/{id}")
    public ResponseEntity<Map<String, Object>> getProductDetails(@PathVariable int id, @RequestParam String quantity) {


        Product product = productService.getProductById(id);

        List<Quantity> discountedQuantities = productService.getDiscountedQuantities(product);

        Map<String, Object> response = new HashMap<>();

        for(Quantity qty: discountedQuantities){
            if(Objects.equals(qty.getQuantity(), quantity)){
                response.put("price", qty.getPrice());
                response.put("stock", qty.getStock());
            }
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ratings/{productId}")
    public ResponseEntity<String> rateProduct(@PathVariable int productId, @RequestParam int userId, @RequestParam int rating) {
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5.");
        }
        productService.saveRating(productId, userId, rating);
        return ResponseEntity.ok("Rating submitted successfully.");
    }

    @GetMapping("/ratings/{productId}")
    public ResponseEntity<Map<String, Object>> getProductRating(@PathVariable int productId) {
        Double averageRating = productService.getAverageRating(productId);
        int ratingCount = productService.getRatingCount(productId);

        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", averageRating != null ? averageRating : 0);
        response.put("ratingCount", ratingCount);
        return ResponseEntity.ok(response);
    }

}

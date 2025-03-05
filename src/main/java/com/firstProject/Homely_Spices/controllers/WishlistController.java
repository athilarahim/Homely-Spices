package com.firstProject.Homely_Spices.controllers;


import com.firstProject.Homely_Spices.model.Product;
import com.firstProject.Homely_Spices.model.Users;
import com.firstProject.Homely_Spices.model.Wishlist;
import com.firstProject.Homely_Spices.repo.ProductRepo;
import com.firstProject.Homely_Spices.repo.UserRepo;
import com.firstProject.Homely_Spices.repo.WishlistRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistRepo wishlistRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private ProductRepo productRepository;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToWishlist(@RequestParam int productId, Principal principal) {
        Map<String, Object> response = new HashMap<>();

        if (principal == null) {
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Users user = userRepository.findByUsername(principal.getName()).orElseThrow(()->new RuntimeException("You have to login first to add to wishlist"));
        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if(!wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)){
            Wishlist wishlist = new Wishlist(user, product);
            wishlistRepository.save(wishlist);

        }
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @Transactional
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromWishlist(@RequestParam int productId, Principal principal) {

        Users user = userRepository.findByUsername(principal.getName()).orElseThrow(()->new RuntimeException("You have to login first to add to wishlist"));
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
        return ResponseEntity.ok("Product removed from wishlist!");
    }
}

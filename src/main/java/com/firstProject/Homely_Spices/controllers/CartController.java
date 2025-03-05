package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.DTO.CartRequestDTO;
import com.firstProject.Homely_Spices.DTO.CartUpdateRequestDTO;
import com.firstProject.Homely_Spices.DTO.OrderItemRequestDTO;
import com.firstProject.Homely_Spices.DTO.OrderRequestDTO;
import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.UserRepo;
import com.firstProject.Homely_Spices.service.*;
import jakarta.validation.Valid;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserProfileService userProfileService;

    public OrderStatus orderStatus;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductService productService;


    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartRequestDTO cartRequest, Principal principal) {
        try {
            cartService.addProductToCart(cartRequest,principal.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "Product added to cart"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to add product to cart"));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateCartQuantity(@RequestBody CartUpdateRequestDTO request, Principal principal) {
        try {
            cartService.updateProductCount(request,request.getProductprice(),request.getNewQuantity(), principal.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "Cart updated successfully"));
        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to update cart"));
        }
    }

    @DeleteMapping("/remove/{productId}/{price}")
    public ResponseEntity<?> removeFromCart(@PathVariable Double price,@PathVariable int productId, Principal principal) {
        try {
            cartService.removeProductFromCart(price,productId, principal.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "Product removed from cart"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to remove product from cart"));
        }
    }


}


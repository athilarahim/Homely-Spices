package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.DTO.OrderRequestDTO;
import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.*;
import com.firstProject.Homely_Spices.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CheckoutApiController {


    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CartService cartService;

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private QuantityRepo quantityRepo;

    @Autowired
    private LedgerService ledgerService;

    @PostMapping("/checkout/select-address")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> selectAddress(@RequestBody Map<String, Integer> payload, Principal principal) {
        int addressId = payload.get("addressId");
        boolean success = userProfileService.setSelectedAddress(principal.getName(), addressId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkout/placeOrder")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody OrderRequestDTO orderRequest, Principal principal) {

        double finalPrice = orderRequest.getCartTotal() - orderRequest.getCouponDiscount() + orderRequest.getDeliveryFee();
        Map<String, Object> response = new HashMap<>();
        try {

            String username = principal.getName();
            Users user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));

            if(orderRequest.getPaymentId()==3){
                double walletBalance = walletRepo.findLatestWalletBalanceByUserId(user.getId());
                walletBalance-= finalPrice;
                Wallet wallet = new Wallet();
                wallet.setWalletBalance(walletBalance);
                wallet.setTransactionAmount(finalPrice);
                wallet.setTransactionType("DEBIT");
                wallet.setTransactionDate(LocalDateTime.now());
                wallet.setDescription("Order placed");
                wallet.setUser(user);

                walletRepo.save(wallet);
            }

            LocalDate orderDate = LocalDate.now();

            Orders order = new Orders();
            order.setOrderDate(orderDate);
            order.setDeliveryFee(orderRequest.getDeliveryFee());
            order.setTotalAmount(orderRequest.getCartTotal());
            order.setProductCount(orderRequest.getProductCount());
            order.setCouponDeduction(orderRequest.getCouponDiscount());

            Address address = userProfileService.getAddressById(orderRequest.getAddressId());
            Payment payment = orderService.getPaymentMethod(orderRequest.getPaymentId()).orElseThrow(() -> new RuntimeException("Payment method not found"));

            order.setAddress(address);
            order.setUser(user);
            order.setPayment(payment);

            OrderStatus initialStatus = new OrderStatus();


                initialStatus.setStatus("Order placed");  // Mark as placed if payment was successful

            initialStatus.setStatusChangeDate(LocalDateTime.now());
            initialStatus.setOrder(order);
            order.setLatestStatus(initialStatus);

            List<OrderStatus> orderStatuses = new ArrayList<>();
            orderStatuses.add(initialStatus);
            order.setOrderStatuses(orderStatuses);

            List<Cart> cartItems = cartService.getCartItems(principal.getName());
            List<OrderItems> orderItems = new ArrayList<>();
            for (Cart item : cartItems) {
                if (orderRequest.getSelectedItemIds().contains(item.getId())) {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setProduct(item.getProduct());
                    orderItem.setQuantity(item.getWeight());
                    orderItem.setPrice(item.getProductPrice());
                    orderItem.setItemcount(item.getProductCount());
                    orderItem.setOrder(order);

                    //reduce the stock from Quantities table
                    Quantity qty = quantityRepo.findByProductIdAndQuantity(item.getProduct().getId(), item.getWeight());
                    qty.setStock(qty.getStock()-item.getProductCount());
                    quantityRepo.save(qty);

                    orderItems.add(orderItem);
                }
            }

            order.setOrderItems(orderItems);
            orderService.saveOrder(order);
            ledgerService.recordOrderTransaction(order);

            // Clear the cart after the order is placed
            cartService.removeSelectedItemsFromCart(orderRequest.getSelectedItemIds());

            response.put("success", true);
            response.put("message", "Order placed successfully!");
            response.put("orderId", order.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to place order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/checkout/markPaymentPending")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markPaymentPending(@RequestBody OrderRequestDTO orderRequest, Principal principal) {
        double finalPrice = orderRequest.getCartTotal() - orderRequest.getCouponDiscount() + orderRequest.getDeliveryFee();
        Map<String, Object> response = new HashMap<>();
        try {

            String username = principal.getName();
            Users user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));

            LocalDate orderDate = LocalDate.now();

            Orders order = new Orders();
            order.setOrderDate(orderDate);
            order.setDeliveryFee(orderRequest.getDeliveryFee());
            order.setTotalAmount(orderRequest.getCartTotal());
            order.setProductCount(orderRequest.getProductCount());
            order.setCouponDeduction(orderRequest.getCouponDiscount());

            Address address = userProfileService.getAddressById(orderRequest.getAddressId());
            Payment payment = orderService.getPaymentMethod(orderRequest.getPaymentId()).orElseThrow(() -> new RuntimeException("Payment method not found"));

            order.setAddress(address);
            order.setUser(user);
            order.setPayment(payment);

            OrderStatus initialStatus = new OrderStatus();

            initialStatus.setStatus("Payment Pending");  // Mark as pending if payment failed

            initialStatus.setStatusChangeDate(LocalDateTime.now());
            initialStatus.setOrder(order);
            order.setLatestStatus(initialStatus);

            List<OrderStatus> orderStatuses = new ArrayList<>();
            orderStatuses.add(initialStatus);
            order.setOrderStatuses(orderStatuses);

            List<Cart> cartItems = cartService.getCartItems(principal.getName());
            List<OrderItems> orderItems = new ArrayList<>();
            for (Cart item : cartItems) {
                if (orderRequest.getSelectedItemIds().contains(item.getId())) {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setProduct(item.getProduct());
                    orderItem.setQuantity(item.getWeight());
                    orderItem.setPrice(item.getProductPrice());
                    orderItem.setItemcount(item.getProductCount());
                    orderItem.setOrder(order);

                    //reduce the stock from Quantities table
                    Quantity qty = quantityRepo.findByProductIdAndQuantity(item.getProduct().getId(), item.getWeight());
                    qty.setStock(qty.getStock()-item.getProductCount());
                    quantityRepo.save(qty);
                    orderItems.add(orderItem);
                }
            }

            order.setOrderItems(orderItems);
            orderService.saveOrder(order);


            // Clear the cart after the order is placed
            cartService.removeSelectedItemsFromCart(orderRequest.getSelectedItemIds());

            response.put("success", true);
            response.put("message", "Order has been set to Payment Pending!");
            response.put("orderId", order.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to place order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/orders/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable int orderId, Principal principal) {
        try {
            // Verify if the order belongs to the logged-in user
            boolean isCancelled = orderService.cancelOrder(orderId, principal.getName());
            if (isCancelled) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Order cancelled successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Order cancellation failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred while cancelling the order"));
        }
    }

}

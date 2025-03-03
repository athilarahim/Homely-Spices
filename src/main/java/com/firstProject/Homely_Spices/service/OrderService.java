package com.firstProject.Homely_Spices.service;

import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.*;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private OrderStatusRepo orderStatusRepo;

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private WalletRepo walletRepository;

    @Autowired
    private LedgerService ledgerService;

    public Optional<Payment> getPaymentMethod(int paymentId){
        return paymentRepo.findById(paymentId);
    }

    public OrderStatus getOrderStatus(String status){
        return orderStatusRepo.findByStatus(status);
    }

    public void saveOrder(Orders order) {
        orderRepo.save(order);
    }

    public List<Orders> getOrders(String username) {
        Optional<Users> user = userRepo.findByUsername(username);
        int userId = user.get().getId();
        return orderRepo.findByUserId(userId);
    }

    public Orders getOrderSummary(int orderId, String username) {
        Optional<Users> user = userRepo.findByUsername(username);
        int userId = user.get().getId();
        return orderRepo.findByIdAndUserId(orderId,userId);
    }

    public boolean cancelOrder(int orderId, String username) {
        Optional<Users> user = userRepo.findByUsername(username);
        int userId = user.get().getId();
        Optional<Orders> orderOpt = Optional.ofNullable(orderRepo.findByIdAndUserId(orderId, userId));

        if (orderOpt.isPresent()) {
            Orders order = orderOpt.get();
            // Check if the latest status is already "Cancelled"
            List<OrderStatus> orderStatuses = order.getOrderStatuses();
            OrderStatus latestStatus = orderStatuses.get(orderStatuses.size() - 1); // Get the most recent status

            if (!latestStatus.getStatus().equals("Cancelled") && !latestStatus.getStatus().equals("Delivered")){
                // Create the new "Cancelled" status
                OrderStatus cancelledStatus = new OrderStatus();
                cancelledStatus.setStatus("Cancelled");
                cancelledStatus.setStatusChangeDate(LocalDateTime.now());
                cancelledStatus.setOrder(order);

                // Add the new status to the list
                orderStatuses.add(cancelledStatus);
                order.setOrderStatuses(orderStatuses);
                order.setLatestStatus(cancelledStatus);

                orderRepo.save(order); // Save the changes

                if(order.getPayment().getId()==2 || order.getPayment().getId()==3){
                    double finalPrice = order.getTotalAmount()-order.getCouponDeduction()+order.getDeliveryFee();

                    Wallet wallet = new Wallet();
                    wallet.setUser(order.getUser());
                    wallet.setTransactionAmount(finalPrice);
                    wallet.setTransactionType("CREDIT");
                    wallet.setTransactionDate(LocalDateTime.now());
                    wallet.setDescription("Cancelled Order: #"+orderId);

                    // Get existing wallet balance
                    Double currentBalance = walletRepository.findLatestWalletBalanceByUserId(order.getUser().getId());
                    wallet.setWalletBalance((currentBalance != null ? currentBalance : 0.0) + finalPrice);

                    walletRepository.save(wallet);
                    ledgerService.recordRefundTransaction(order);
                }
                return true;
            }
        }
        return false;
    }

    public List<Orders> getAllOrders() {
       return orderRepo.findAll();
    }
}

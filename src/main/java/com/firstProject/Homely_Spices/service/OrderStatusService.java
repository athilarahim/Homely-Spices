package com.firstProject.Homely_Spices.service;
import com.firstProject.Homely_Spices.model.OrderStatus;
import com.firstProject.Homely_Spices.model.Orders;
import com.firstProject.Homely_Spices.model.Wallet;
import com.firstProject.Homely_Spices.repo.OrderRepo;
import com.firstProject.Homely_Spices.repo.OrderStatusRepo;
import com.firstProject.Homely_Spices.repo.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class OrderStatusService {

    @Autowired
    private OrderStatusRepo orderStatusRepository;

    @Autowired
    private OrderRepo orderRepository;

    @Autowired
    private WalletRepo walletRepository;


    @Transactional
    public void updateOrderStatus(int orderId, String newStatus) {
        // Fetch the order
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if((Objects.equals(newStatus, "Cancelled")) &&(order.getPayment().getId()==2 || order.getPayment().getId()==3)){
            double finalPrice = order.getTotalAmount()+order.getDeliveryFee()-order.getCouponDeduction();
            Wallet wallet = new Wallet();
            wallet.setUser(order.getUser());
            wallet.setTransactionAmount(finalPrice);
            wallet.setTransactionType("CREDIT");
            wallet.setTransactionDate(LocalDateTime.now());
            wallet.setDescription("Cancelled Order: #"+orderId);

            Double currentBalance = walletRepository.findLatestWalletBalanceByUserId(order.getUser().getId());
            wallet.setWalletBalance((currentBalance != null ? currentBalance : 0.0) + finalPrice);

            walletRepository.save(wallet);
        }
        // Create new OrderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrder(order);
        orderStatus.setStatus(newStatus);
        orderStatus.setStatusChangeDate(LocalDateTime.now());

        // Save the new status
        orderStatus = orderStatusRepository.save(orderStatus);


        // Update the latestStatus field in the Orders table
        order.setLatestStatus(orderStatus);
        orderRepository.save(order);
    }
}

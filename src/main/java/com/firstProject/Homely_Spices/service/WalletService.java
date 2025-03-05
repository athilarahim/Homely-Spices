package com.firstProject.Homely_Spices.service;

import com.firstProject.Homely_Spices.model.Orders;
import com.firstProject.Homely_Spices.model.Wallet;
import com.firstProject.Homely_Spices.repo.OrderRepo;
import com.firstProject.Homely_Spices.repo.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WalletService {

    @Autowired
    private WalletRepo walletRepository;

    @Autowired
    private OrderRepo ordersRepository;

    @Autowired
    private LedgerService ledgerService;

    @Transactional
    public void addRefundToWallet(int orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));

        double finalPrice = order.getTotalAmount()-order.getCouponDeduction()+order.getDeliveryFee();

        if (order.getLatestStatus() != null && "Return Received".equals(order.getLatestStatus().getStatus())) {
            Wallet wallet = new Wallet();
            wallet.setUser(order.getUser());
            wallet.setTransactionAmount(finalPrice);
            wallet.setTransactionType("CREDIT");
            wallet.setTransactionDate(LocalDateTime.now());
            wallet.setDescription("Return Order: #"+orderId);

            // Get existing wallet balance
            Double currentBalance = walletRepository.findLatestWalletBalanceByUserId(order.getUser().getId());
            wallet.setWalletBalance((currentBalance != null ? currentBalance : 0.0) + finalPrice);

            walletRepository.save(wallet);
            ledgerService.recordRefundTransaction(order);
        }
    }
}

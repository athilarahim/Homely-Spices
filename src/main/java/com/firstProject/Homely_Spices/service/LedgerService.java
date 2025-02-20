package com.firstProject.Homely_Spices.service;

import com.firstProject.Homely_Spices.model.LedgerEntry;
import com.firstProject.Homely_Spices.model.Orders;
import com.firstProject.Homely_Spices.model.Wallet;
import com.firstProject.Homely_Spices.repo.LedgerRepo;
import com.firstProject.Homely_Spices.repo.OrderRepo;
import com.firstProject.Homely_Spices.repo.WalletRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LedgerService {

    @Autowired
    private LedgerRepo ledgerRepository;

    @Autowired
    private OrderRepo orderRepository;

    @Autowired
    private WalletRepo walletRepository;

    @Transactional
    public void recordOrderTransaction(Orders order) {
        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setEntryDate(LocalDateTime.now());
        ledgerEntry.setUser(order.getUser());
        ledgerEntry.setTransactionType("SALE");
        ledgerEntry.setPayment(order.getPayment());
        ledgerEntry.setAmount(order.getTotalAmount()-order.getCouponDeduction()+order.getDeliveryFee());
        ledgerEntry.setOrder(order);

        List<Orders> orders = orderRepository.findAll();

        double totalRevenue = orders.stream().mapToDouble(Orders::getTotalAmount).sum();

        double couponDeductions = orders.stream()
                .mapToDouble(orders1 -> orders1.getCouponDeduction() != null ? orders1.getCouponDeduction() : 0.0)
                .sum();

        double deliveryFee = orders.stream().mapToDouble(Orders::getDeliveryFee).sum();

        double walletCredits = walletRepository.findTotalCredits();

        ledgerEntry.setBalanceAfterTransaction(totalRevenue-couponDeductions+deliveryFee-walletCredits);

        ledgerRepository.save(ledgerEntry);
    }

    @Transactional
    public void recordRefundTransaction(Orders refund) {
        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setEntryDate(LocalDateTime.now());
        ledgerEntry.setUser(refund.getUser());
        ledgerEntry.setTransactionType("REFUND");
        ledgerEntry.setAmount(refund.getTotalAmount()-refund.getCouponDeduction()+refund.getDeliveryFee());
        ledgerEntry.setOrder(refund);

        List<Orders> orders = orderRepository.findAll();

        double totalRevenue = orders.stream().mapToDouble(Orders::getTotalAmount).sum();

        double couponDeductions = orders.stream()
                .mapToDouble(orders1 -> orders1.getCouponDeduction() != null ? orders1.getCouponDeduction() : 0.0)
                .sum();

        double deliveryFee = orders.stream().mapToDouble(Orders::getDeliveryFee).sum();

        double walletCredits = walletRepository.findTotalCredits();

        ledgerEntry.setBalanceAfterTransaction(totalRevenue-couponDeductions+deliveryFee-walletCredits);


        ledgerRepository.save(ledgerEntry);
    }

    @Transactional
    public void recordWalletTransaction(Wallet walletTransaction) {
        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setEntryDate(LocalDateTime.now());
        ledgerEntry.setUser(walletTransaction.getUser());
        ledgerEntry.setTransactionType(walletTransaction.getTransactionType()); // "CREDIT" or "DEBIT"
        ledgerEntry.setAmount(walletTransaction.getTransactionAmount());

//        // Get the latest balance from the last ledger entry
//        Double lastBalance = ledgerRepository.findLastBalanceByUserId(walletTransaction.getUser().getId());
//        double newBalance = walletTransaction.getTransactionType().equals("CREDIT")
//                ? (lastBalance != null ? lastBalance : 0) + walletTransaction.getTransactionAmount()
//                : (lastBalance != null ? lastBalance : 0) - walletTransaction.getTransactionAmount();
//        ledgerEntry.setBalanceAfterTransaction(newBalance);


        ledgerRepository.save(ledgerEntry);
    }


    public List<LedgerEntry> getAllLedgerEntries() {
        return ledgerRepository.findAllByOrderByEntryDateDesc();
    }

    public List<LedgerEntry> findLedgerEntriesByUsername(String username) {
        return ledgerRepository.findByUserUsernameContainingIgnoreCase(username);
    }

}

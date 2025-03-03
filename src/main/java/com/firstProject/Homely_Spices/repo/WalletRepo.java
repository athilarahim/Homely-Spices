package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WalletRepo extends JpaRepository<Wallet, Integer> {

    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId ORDER BY w.transactionDate DESC")
    List<Wallet> findWalletTransactionsByUserId(@Param("userId") int userId);

    @Query(value = "SELECT COALESCE(wallet_balance, 0.0) FROM wallet " +
            "WHERE user_id = :userId " +
            "ORDER BY transaction_date DESC, id DESC " +
            "LIMIT 1",
            nativeQuery = true)
    Double findLatestWalletBalanceByUserId(@Param("userId") int userId);


    @Query("SELECT COALESCE(SUM(w.transactionAmount), 0) FROM Wallet w WHERE w.transactionType = 'CREDIT'")
    Double findTotalCredits();

    @Query(value = "SELECT DATE(transaction_date), COALESCE(SUM(transaction_amount), 0) " +
            "FROM wallet WHERE transaction_type = 'CREDIT' " +
            "AND DATE(transaction_date) BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(transaction_date) " +
            "ORDER BY DATE(transaction_date) ASC",
            nativeQuery = true)
    List<Object[]> findDailyCreditsWithinRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);


}

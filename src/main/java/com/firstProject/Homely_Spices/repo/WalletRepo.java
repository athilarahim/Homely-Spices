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

    @Query(value = "SELECT COALESCE(MAX(wallet_balance), 0.0) FROM wallet WHERE user_id = :userId ORDER BY transaction_date DESC, id DESC LIMIT 1", nativeQuery = true)
    Double findLatestWalletBalanceByUserId(@Param("userId") int userId);

    @Query("SELECT COALESCE(SUM(w.transactionAmount), 0) FROM Wallet w WHERE w.transactionType = 'CREDIT'")
    Double findTotalCredits();

    @Query("SELECT DATE(w.transactionDate), COALESCE(SUM(w.transactionAmount), 0) " +
            "FROM Wallet w WHERE w.transactionType = 'CREDIT' " +
            "AND DATE(w.transactionDate) BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(w.transactionDate) " +
            "ORDER BY DATE(w.transactionDate) ASC")
    List<Object[]> findDailyCreditsWithinRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);


}

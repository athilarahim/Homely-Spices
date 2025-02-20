package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepo extends JpaRepository<LedgerEntry, Integer> {

//    @Query("SELECT l.balanceAfterTransaction FROM LedgerEntry l WHERE l.user.id = :userId ORDER BY l.entryDate DESC LIMIT 1")
//    Double findLastBalanceByUserId(@Param("userId") int userId);

    List<LedgerEntry> findAllByOrderByEntryDateDesc();

    List<LedgerEntry> findByUserUsernameContainingIgnoreCase(String username);
}

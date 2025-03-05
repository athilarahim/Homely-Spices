package com.firstProject.Homely_Spices.repo;

import com.firstProject.Homely_Spices.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users,Integer> {
    Optional<Users> findByUsername(String username);

    Users findByEmail(String email);
    Boolean existsByEmail(String email);


    @Query
            ("SELECT u FROM Users u WHERE " +
                    "u.username LIKE %:keyword% OR " +
                    "u.name LIKE %:keyword% OR " +
                    "u.phone LIKE %:keyword% OR " +
                    "u.email LIKE %:keyword%")
    List<Users> searchUsers(String keyword);

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.password = :newPass WHERE u.email = :email")
    void changePassword(@Param("email") String email, @Param("newPass") String newPass);

}

package com.firstProject.Homely_Spices.service;
import com.firstProject.Homely_Spices.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.firstProject.Homely_Spices.repo.UserRepo;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepo userRepo;

    public Users saveUser(Users user) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getIs_blocked()) {
            String blockReason = user.getBlockReason();
            System.out.println("User is blocked. Block reason: " + blockReason); // Debug log
            throw new LockedException("Your account has been blocked. Reason: " + blockReason);
        }

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

    public boolean userExisting(String email) {
        return userRepo.existsByEmail(email);
    }


    public void toggleBlockUser(int userId, String blockReason) {
            Users user = userRepo.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid User ID: " + userId));
        System.out.println("Toggling block status for user: " + user.getUsername());
        System.out.println("Current blocked status: " + user.getIs_blocked());
        System.out.println("Block reason provided: " + blockReason);
            user.setIs_blocked(!user.getIs_blocked());
        if (user.getIs_blocked()) {
            user.setBlockReason(blockReason);
        } else {
            user.setBlockReason(null); // Clear the block reason if unblocking
        }
            userRepo.save(user);
        System.out.println("Updated blocked status: " + user.getIs_blocked());
        System.out.println("Updated block reason: " + user.getBlockReason());
    }
}

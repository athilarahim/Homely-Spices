package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.model.Users;
import com.firstProject.Homely_Spices.model.Wallet;
import com.firstProject.Homely_Spices.repo.UserRepo;
import com.firstProject.Homely_Spices.repo.WalletRepo;
import com.firstProject.Homely_Spices.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepo walletRepo;

    @Autowired
    UserRepo userRepo;

    @GetMapping("/wallet")
    public String getWalletPage(Model model, Principal principal){

        Users user = userRepo.findByUsername(principal.getName()).orElseThrow(()->new RuntimeException("No user found"));

        List<Wallet> walletTransactions = walletRepo.findWalletTransactionsByUserId(user.getId());

        Double walletBalance = walletRepo.findLatestWalletBalanceByUserId(user.getId());

        model.addAttribute("transactions",walletTransactions);
        model.addAttribute("walletBalance", walletBalance != null ? walletBalance : 0.0);

        return "wallet";
    }
}

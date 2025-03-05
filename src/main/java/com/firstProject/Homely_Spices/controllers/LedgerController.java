package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.model.LedgerEntry;
import com.firstProject.Homely_Spices.model.Orders;
import com.firstProject.Homely_Spices.repo.OrderRepo;
import com.firstProject.Homely_Spices.repo.WalletRepo;
import com.firstProject.Homely_Spices.service.LedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/ledger")
public class LedgerController {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private WalletRepo walletRepo;

    @GetMapping
    public String viewAllLedgerEntries(Model model) {

        List<LedgerEntry> ledgerEntries = ledgerService.getAllLedgerEntries();

        List<Orders> orders = orderRepo.findAll();

        model.addAttribute("ledgerEntries", ledgerEntries);
        return "ledgerRecords";
    }

    @GetMapping("/search")
    @ResponseBody
    public List<LedgerEntry> searchLedgerByUsername(@RequestParam String username) {
        return ledgerService.findLedgerEntriesByUsername(username);
    }


}

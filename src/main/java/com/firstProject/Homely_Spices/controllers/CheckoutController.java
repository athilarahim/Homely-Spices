package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.AddressRepo;
import com.firstProject.Homely_Spices.repo.QuantityRepo;
import com.firstProject.Homely_Spices.repo.UserRepo;
import com.firstProject.Homely_Spices.repo.WalletRepo;
import com.firstProject.Homely_Spices.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CheckoutController {

    @Autowired
    UserProfileService userProfileService;

    @Autowired
    UserService userService;

    @Autowired
    CartService cartService;

    @Autowired
    OrderService orderService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    AddressRepo addressRepo;

    @Autowired
    CouponService couponService;

    @Autowired
    WalletRepo walletRepo;

    @Autowired
    QuantityRepo quantityRepo;

    @Autowired
    ProductService productService;

    @GetMapping("/checkout")
    public String getCheckoutPage(Model model, Principal principal, HttpSession session){

        Users user = userRepo.findByUsername(principal.getName()).orElseThrow(()->new RuntimeException("No user found!"));
        List<Address> addresses = userProfileService.getUserAddress(principal.getName());

        if (addresses == null || addresses.isEmpty()) {
            // Handle case where no addresses are found
            model.addAttribute("addresses", new ArrayList<>());
        } else {
            model.addAttribute("addresses", addresses);
        }

        double cartSubtotal = (double) session.getAttribute("cartSubtotal");
        cartSubtotal = Math.round(cartSubtotal * 10.0) / 10.0;
        double deliveryFee = (double) session.getAttribute("deliveryFee");

        double walletBalance = walletRepo.findLatestWalletBalanceByUserId(user.getId());
        model.addAttribute("walletBalance",walletBalance);

        List<Cart> cartItems = cartService.getCartItems(principal.getName());

        model.addAttribute("cartSubtotal", cartSubtotal);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("cartItems", cartItems);



        return "checkoutPage";
    }

    @GetMapping("/cart/selected")
    public String getCheckoutPageForSelectedItems(
            @RequestParam(value = "selectedItems", required = false) List<Integer> selectedItemIds,
            Model model,
            Principal principal,
            HttpSession session)
    {

        Users user = userRepo.findByUsername(principal.getName()).orElseThrow(()->new RuntimeException("No user found!"));

        List<Cart> cartItems = cartService.getCartItems(principal.getName());

        // If no items are selected, use all cart items
        List<Cart> selectedCartItems = (selectedItemIds == null || selectedItemIds.isEmpty())
                ? cartItems
                : cartItems.stream()
                .filter(item -> selectedItemIds.contains(item.getId()))
                .collect(Collectors.toList());

        double cartSubtotal = selectedCartItems.stream()
                .mapToDouble(item -> item.getProductPrice() * item.getProductCount())
                .sum();
        cartSubtotal = Math.round(cartSubtotal * 10.0) / 10.0;


        double deliveryFee = (cartSubtotal<140)? 70 : 0.0;

        double walletBalance = walletRepo.findLatestWalletBalanceByUserId(user.getId());

        model.addAttribute("walletBalance",walletBalance);
        model.addAttribute("cartSubtotal", cartSubtotal);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("cartItems", selectedCartItems);

        List<Address> addresses = userProfileService.getUserAddress(principal.getName());
        model.addAttribute("addresses", addresses);

        return "checkoutPage";
    }

    @PostMapping("/checkout/address/add")
    public String addAddress(
            @RequestParam String housename,
            @RequestParam String street,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String country,
            @RequestParam String zipcode,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {

            String username = principal.getName();
            Users user = userRepo.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));


            // Create a new Address entity and set its fields
            Address address = new Address();
            address.setHousename(housename);
            address.setStreet(street);
            address.setCity(city);
            address.setState(state);
            address.setCountry(country);
            address.setZipcode(zipcode);
            address.setUser(user); // Set the user for this address

            // Save the address to the database
            addressRepo.save(address);

            redirectAttributes.addFlashAttribute("success", "Address added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add address. Please try again.");
        }

        // Redirect back to the address list page
        return "redirect:/cart/selected";
    }



    @GetMapping("/checkout/address/edit/{id}")
    public String editAddress(@PathVariable("id") int id, Model model) {
        // Fetch the address using the id from the database or service
        Address address = userProfileService.getAddressById(id);
        model.addAttribute("address", address);

        return "edit-address-checkout";
    }

    @PostMapping("/checkout/address/update")
    public String updateAddress(@ModelAttribute("address") Address address) {
        userProfileService.updateAddress(address);
        return "redirect:/checkout";
    }

    @GetMapping("/checkout/coupons")
    public String showCoupons(Model model){

        List<Coupon> coupons = couponService.getAllCoupons();

        model.addAttribute("coupons",coupons);
        return "coupons";
    }


}

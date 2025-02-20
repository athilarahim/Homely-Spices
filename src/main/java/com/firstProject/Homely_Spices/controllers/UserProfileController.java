package com.firstProject.Homely_Spices.controllers;

import com.firstProject.Homely_Spices.model.Address;
import com.firstProject.Homely_Spices.model.Orders;
import com.firstProject.Homely_Spices.model.Product;
import com.firstProject.Homely_Spices.model.Users;
import com.firstProject.Homely_Spices.repo.AddressRepo;
import com.firstProject.Homely_Spices.repo.UserRepo;
import com.firstProject.Homely_Spices.service.OrderService;
import com.firstProject.Homely_Spices.service.UserProfileService;
import com.firstProject.Homely_Spices.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class UserProfileController {

    @Autowired
    UserProfileService userProfileService;

    @Autowired
    UserService userService;

    @Autowired
    AddressRepo addressRepo;

    @Autowired
    UserRepo userRepo;


    @GetMapping("/view-profile")
    public String getUserAddress(Model model, Principal principal){
        String username = principal.getName();
        UserDetails user = userService.loadUserByUsername(username);
        List<Address> addresses = userProfileService.getUserAddress(user.getUsername());
        Users usr = addresses.getFirst().getUser();
        model.addAttribute("addresses",addresses);
        model.addAttribute("user",usr);

        return "userProfile";
    }

    @PostMapping("/view-profile/save")
    public String savePersonalInfo(@ModelAttribute("user") Users updatedUser) {
        userProfileService.updateUser(updatedUser);
        return "redirect:/view-profile?success"; // Redirect back to the profile page with a success message
    }

    @GetMapping("/address/edit/{id}")
    public String editAddress(@PathVariable("id") int id, Model model) {
        // Fetch the address using the id from the database or service
        Address address = userProfileService.getAddressById(id);
        model.addAttribute("address", address);

        return "edit-address";
    }

    @PostMapping("/view-profile/address/update")
    public String updateAddress(@ModelAttribute("address") Address address) {
        userProfileService.updateAddress(address);
        return "redirect:/view-profile";
    }

    @PostMapping("/address/add")
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
        return "redirect:/view-profile";
    }


    @PostMapping("/address/delete/{id}")
    public String deleteAddress(
            @PathVariable("id") int id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {

            String username = principal.getName();
            Users user = userRepo.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));


            if (user == null) {
                throw new RuntimeException("User not found.");
            }

            // Fetch the address to ensure it belongs to the logged-in user
            Address address = addressRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Address not found."));

            addressRepo.delete(address);

            redirectAttributes.addFlashAttribute("success", "Address deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete address. Please try again.");
        }

        // Redirect back to the address list page
        return "redirect:/view-profile";
    }
}


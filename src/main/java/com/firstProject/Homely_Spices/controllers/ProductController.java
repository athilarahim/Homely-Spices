package com.firstProject.Homely_Spices.controllers;
import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.QuantityRepo;
import com.firstProject.Homely_Spices.repo.UserRepo;
import com.firstProject.Homely_Spices.repo.WishlistRepo;
import com.firstProject.Homely_Spices.service.CartService;
import com.firstProject.Homely_Spices.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    QuantityRepo quantityRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    WishlistRepo wishlistRepo;

    @Autowired
    private CartService cartService;

    @GetMapping("/home")
    public String mainPage(Model model){
        List<Product> spices = productService.getSpices();
        List<Product> flours = productService.getFlours();
        List<Product> combos = productService.getCombos();
        model.addAttribute("spices", spices);
        model.addAttribute("flours", flours);
        model.addAttribute("combos", combos);
        return "mainPage";
    }
    @GetMapping("/products")
    public String productsPage(Model model,
                               @RequestParam(name = "priceSort", required = false) Boolean priceSort,
                               @RequestParam(name = "letterSort", required = false) Boolean letterSort,
                               @RequestParam(name = "ratingSort", required = false) Boolean ratingSort)
    {
        List<Product> products = productService.getAllProducts();
        List<Quantity> quantities = productService.getAllQuantity();
        List<Category> categories = productService.getAllCategory();
        model.addAttribute("products", products);
        model.addAttribute("quantities", quantities);
        model.addAttribute("categories", categories);

        if (priceSort != null) {
            if (priceSort) {
                products.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            } else {
                products.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
            }
        }

        if (letterSort != null) {
            if (letterSort) {
                products.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
            } else {
                products.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
            }
        }

        if (ratingSort != null) {
            if (ratingSort) {
                products.sort((p1, p2) -> Double.compare(productService.getAverageRating(p2.getId()),productService.getAverageRating(p1.getId())));
            } else {
                products.sort((p1, p2) -> Double.compare(productService.getAverageRating(p1.getId()),productService.getAverageRating(p2.getId())));
            }
        }

        model.addAttribute("priceSort", priceSort);
        model.addAttribute("letterSort", letterSort);
        model.addAttribute("ratingSort", ratingSort);

        return "productPage";
    }

    @GetMapping("/productDetails/{id}")
    public String productDetails(Model model, @PathVariable int id){
        Product product = productService.getProductById(id);
        List<Product> similarProducts = productService.getSimilarProducts(id);

        // Retrieve logged-in user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
            Users user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            int userId = user.getId();
            System.out.println(userId);
            model.addAttribute("userId", userId);
        }

        List<Quantity> discountedQuantities = productService.getDiscountedQuantities(product);

            model.addAttribute("product", product);
            model.addAttribute("similarProducts",similarProducts);
            model.addAttribute("quantities", discountedQuantities);

        return "productDetails";
    }

    @GetMapping("/filter")
    public String filterProducts(
            @RequestParam(value = "categories", required = false) List<String> categories,
            @RequestParam(value = "priceRange",required = false) String priceRange,
            @RequestParam(value = "quantities", required = false) List<String> quantities,
            Model model
    ) {
        Double minPrice = null;
        Double maxPrice = null;

        if (priceRange != null) {
            String[] prices = priceRange.split("-");
            minPrice = Double.parseDouble(prices[0]);
            maxPrice = Double.parseDouble(prices[1]);
        }
        List<Product> products = productService.filterProducts(categories, minPrice, maxPrice, quantities);
        List<Category> allCategory = productService.getAllCategory();
        model.addAttribute("products",products);
        model.addAttribute("selectedCategories", categories);
        model.addAttribute("selectedPriceRange", priceRange);
        model.addAttribute("selectedQuantities", quantities);
        model.addAttribute("categories", allCategory);
        return "productPage";
    }

    @GetMapping("/products/search")
    public String searchProducts(@RequestParam("query") String query,
                                 @RequestParam(value = "categories", required = false) List<String> categories,
                                 @RequestParam(value = "priceRange", required = false) String priceRange,
                                 @RequestParam(value = "quantities", required = false) List<String> quantities,
                                 Model model) {
        Double minPrice = null;
        Double maxPrice = null;

        if (priceRange != null && !priceRange.isEmpty()) { // Check if priceRange is not empty
            String[] prices = priceRange.split("-");
            if (prices.length == 2) { // Ensure we have exactly two values
                try {
                    minPrice = Double.parseDouble(prices[0].trim());
                    maxPrice = Double.parseDouble(prices[1].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid price range format: " + priceRange);
                }
            }
        }
        // Apply filtering first
        List<Product> filteredProducts = productService.filterProducts(categories, minPrice, maxPrice, quantities);

        // Apply search within filtered results
        List<Product> searchResults = productService.searchWithinFilteredProducts(filteredProducts, query);

        List<Category> allCategory = productService.getAllCategory();
        model.addAttribute("products", searchResults);
        model.addAttribute("selectedCategories", categories);
        model.addAttribute("selectedPriceRange", priceRange);
        model.addAttribute("selectedQuantities", quantities);
        model.addAttribute("categories", allCategory);
        return "productPage";
    }

    @GetMapping("/cart/view-cart")
    public String viewCart(Model model, Principal principal, HttpSession session) {
        String username = principal.getName();
        List<Cart> cartItems = cartService.getCartItems(username);
        double cartSubtotal = cartItems.stream()
                .mapToDouble(cart -> cart.getProductPrice() * cart.getProductCount())
                .sum();
        cartSubtotal = Math.round(cartSubtotal * 10.0) / 10.0;

        double deliveryFee = 0;

        if(cartSubtotal<140 && !cartItems.isEmpty()){
             deliveryFee=70.0;
        }
        session.setAttribute("cartSubtotal", cartSubtotal);
        session.setAttribute("deliveryFee", deliveryFee);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartSubtotal", cartSubtotal);
        model.addAttribute("deliveryFee", deliveryFee);

        return "cartPage";
    }

    @GetMapping("/wishlist")
    public String showWishlist(Model model, Principal principal) {
        Users user = userRepo.findByUsername(principal.getName()).orElseThrow(()->new RuntimeException("You have to login to view your wishlist"));
        List<Wishlist> wishlistItems = wishlistRepo.findByUserId(user.getId());
        model.addAttribute("wishlist", wishlistItems);
        return "wishlist";
    }

}

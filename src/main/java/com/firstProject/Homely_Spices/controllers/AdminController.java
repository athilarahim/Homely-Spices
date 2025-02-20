package com.firstProject.Homely_Spices.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import com.firstProject.Homely_Spices.DTO.ChartDataDTO;
import com.firstProject.Homely_Spices.DTO.TopSellingCategoryDTO;
import com.firstProject.Homely_Spices.DTO.TopSellingProductDTO;
import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.*;
import com.firstProject.Homely_Spices.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;


@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService service;

    @Autowired
    UserService userService;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderStatusRepo orderStatusRepo;

    @Autowired
    QuantityRepo quantityRepo;

    @Autowired
    StatusRepo statusRepo;

    @Autowired
    OfferRepo offerRepo;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ReturnRequestRepo returnRequestRepository;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private CouponService couponService;

    @Autowired
    private WalletService walletService;


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("currentPage", "dashboard");
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("adminName", "Admin Name");

        List<TopSellingProductDTO> topProducts = service.getTopSellingProducts();
        List<TopSellingCategoryDTO> topCategories = service.getTopSellingCategories();

        model.addAttribute("topProducts", topProducts);
        model.addAttribute("topCategories", topCategories);

        return "admindashboard";
    }

    @GetMapping("/sales-overview-data")
    public ResponseEntity<ChartDataDTO> getSalesOverviewPageData(@RequestParam("type") String type) {
        ChartDataDTO chartData = service.getChartData(type);
        return ResponseEntity.ok(chartData);
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("currentPage", "users");
        model.addAttribute("pageTitle", "User Management");
        model.addAttribute("adminName", "Admin Name");
        List<Users> users = service.getAllUsers();
        model.addAttribute("users", users);
        return "manageUser";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("currentPage", "categories");
        model.addAttribute("pageTitle", "Category Management");
        model.addAttribute("adminName", "Admin Name");
        List<Category> categories = service.getCategories();
        model.addAttribute("categories", categories);
        return "manageCategory";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("currentPage", "products");
        model.addAttribute("pageTitle", "Product Management");
        model.addAttribute("adminName", "Admin Name");
        List<Product> products = service.getAllProducts();
        List<Category> categories = service.getCategories();
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "manageProduct";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("currentPage", "orders");
        model.addAttribute("pageTitle", "Order Management");
        model.addAttribute("adminName", "Admin Name");
        List<Orders> orders = orderService.getAllOrders();
        List<Status> statusList = statusRepo.findAll();
        model.addAttribute("orders", orders);
        model.addAttribute("statusList", statusList);

        return "manageOrders";
    }

    @GetMapping("/inventory")
    public String stocks(Model model) {
        model.addAttribute("currentPage", "inventory");
        model.addAttribute("pageTitle", "Inventory Management");
        model.addAttribute("adminName", "Admin Name");
        List<Quantity> stocks = quantityRepo.findAllByOrderByStockAsc();
        model.addAttribute("stocks", stocks);
        return "manageStock";
    }

    @GetMapping("/searchUser")
    public String searchUsers(@RequestParam String keyword, Model model){
        List<Users> users = service.searchUsers(keyword);
        model.addAttribute("users", users);
        return "manageUser";
    }

    @PostMapping("/categories")
    public String addCategory(@ModelAttribute("category") Category category,RedirectAttributes redirectAttributes){

        try {
            service.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category added successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/searchCategory")
    public String searchCategory(@RequestParam String keyword, Model model){
        List<Category> categories = service.searchCategory(keyword);
        model.addAttribute("categories", categories);
        return "manageCategory";
    }

    @GetMapping("/editCategory/{id}")
    public String getEditCategory(@PathVariable("id") int id, Model model){
        Category category = service.getCategoryById(id);
        model.addAttribute("category", category);
        return "editCategory";
    }

    @PostMapping("/editCategory")
    public String editCategoryDetails(@ModelAttribute Category category){
        Category cate = service.getCategoryById(category.getId());
        if(cate!=null){
            service.editCategory(category.getId(),category);
        }
        else{
            System.out.println("Category not found");
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/deleteCategory/{id}")
    public String softDeleteCategory(@PathVariable int id) {
        service.softDeleteCategory(id);
        return "redirect:/admin/categories";
    }

    @GetMapping("/add-product")
    public String addProductPage(Model model){
        List<Category> categories = service.getCategories();
        model.addAttribute("categories", categories);
        return "addProduct";
    }

    @PostMapping("/products")
    public String addProduct(@ModelAttribute("product") Product product,
                             @RequestParam("quantity") List<String> quantityList,
                             @RequestParam("qprice") List<Double> priceList,
                             @RequestParam("qstock") List<Integer> stockList,
                             @RequestParam("file1") MultipartFile file1,
                             @RequestParam("file2") MultipartFile file2,
                             @RequestParam("file3") MultipartFile file3,
                             HttpSession session,
                             RedirectAttributes redirectAttributes){



        List<Quantity> quantities = new ArrayList<>();
        for (int i = 0; i < quantityList.size(); i++) {
            Quantity q = new Quantity();
            q.setQuantity(quantityList.get(i));
            q.setPrice(priceList.get(i));
            q.setStock(stockList.get(i));
            quantities.add(q);
        }

        if (file1.isEmpty() || file2.isEmpty() || file3.isEmpty()) {
            session.setAttribute("errorMsg", "Please upload exactly 3 images.");
            return "redirect:/admin/products";
        }


        List<String> imageList = new ArrayList<>();

        MultipartFile[] files = {file1, file2, file3};

        for (MultipartFile file : files) {
            try {
                String imageUrl = cloudinaryService.uploadAndGetCloudinaryUrl(file);
                System.out.println("Images added");
                imageList.add(imageUrl);
            } catch (Exception e) {
                session.setAttribute("errorMsg", "Failed to upload images. Please try again.");
                return "redirect:/admin/products";
            }
        }

        product.setImages(imageList);

        session.setAttribute("successMsg", "Product and images saved successfully.");

        boolean isSaved = service.saveProduct(product, quantities);

        if (!isSaved) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product already exists!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Product added successfully!");
        }

        return "redirect:/admin/products";
    }

    @GetMapping("/searchProduct")
    public String searchProduct(@RequestParam String keyword, Model model){
        List<Product> products = service.searchProduct(keyword);
        model.addAttribute("products", products);
        return "manageProduct";
    }

    @GetMapping("/editProduct/{id}")
    public String getEditProduct(@PathVariable("id") int id, Model model){
        Product product = service.getProductById(id);
        List<Category> categories = service.getCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("product", product);

        return "editProduct";
    }

    @PostMapping("/editProduct")
    public String editProductDetails(@ModelAttribute Product product,
                                     @RequestParam(value = "imagesToDelete", required = false) List<String> imagesToDelete,
                                     @RequestParam(value = "file1", required = false) MultipartFile file1,
                                     HttpSession session, Model model) {

        Product existingProduct = service.getProductById(product.getId());
        if (existingProduct == null) {
            session.setAttribute("errorMsg", "Product not found.");
            return "redirect:/admin/editProduct/" + product.getId();
        }

        // Update the basic fields
        existingProduct.setName(product.getName());
        existingProduct.setSku(product.getSku());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setDiscountprice(product.getDiscountprice());

        // Handle images
        if (imagesToDelete != null) {
            for (String deleteUrl : imagesToDelete) {
                String publicId = deleteUrl.substring(deleteUrl.lastIndexOf("/") + 1, deleteUrl.lastIndexOf("."));
                cloudinaryService.deleteFromCloudinary(publicId);
                existingProduct.getImages().remove(deleteUrl);
            }
        }

        if (!file1.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadAndGetCloudinaryUrl(file1);
                existingProduct.getImages().add(imageUrl);
            } catch (Exception e) {
                session.setAttribute("errorMsg", "Failed to upload images. Please try again.");
                return "redirect:/admin/editProduct/" + product.getId();
            }
        }

        existingProduct.setImages(existingProduct.getImages());

        // Handle quantity updates
        List<Quantity> updatedQuantities = new ArrayList<>();

        // Update existing quantities or add new ones
        for (Quantity newQuantity : product.getQuantities()) {
            if (newQuantity.getId() > 0) {  // Existing quantity
                for (Quantity existingQuantity : existingProduct.getQuantities()) {
                    if (existingQuantity.getId() == newQuantity.getId()) {
                        existingQuantity.setQuantity(newQuantity.getQuantity());
                        existingQuantity.setPrice(newQuantity.getPrice());
                        existingQuantity.setStock(newQuantity.getStock());
                        updatedQuantities.add(existingQuantity);
                        break;
                    }
                }
            } else {  // New quantity
                newQuantity.setProduct(existingProduct);
                updatedQuantities.add(newQuantity);
            }
        }

        // Set the updated quantities on the product
        existingProduct.setQuantities(updatedQuantities);

        // Save the updated product
        Product updatedProduct = service.updateProduct(existingProduct, updatedQuantities);

        if (updatedProduct != null) {
            session.setAttribute("successMsg", "Product updated successfully.");
            model.addAttribute("product", updatedProduct);
        } else {
            session.setAttribute("errorMsg", "Failed to update the product.");
        }

        return "redirect:/admin/products";
    }


    @PostMapping("/deleteProduct/{id}")
    public String softDeleteProduct(@PathVariable int id) {
        service.softDeleteProduct(id);
        return "redirect:/admin/products";
    }

    //block user
    @PostMapping("users/{id}/toggle-block")
    public String toggleBlockUser(@PathVariable("id") int userId,@RequestParam(required = false) String blockReason) {

        userService.toggleBlockUser(userId,blockReason);
        return "redirect:/admin/users";
    }

    //update inventory
    @PostMapping("/updateStock")
    public String updateStock(@RequestParam int productId,@RequestParam String quantity, @RequestParam int stock) {
            // Find the quantity entity for the given product
            Quantity qty = quantityRepo.findByProductIdAndQuantity(productId,quantity);

            // Update the stock
            qty.setStock(stock);
            quantityRepo.save(qty);

            return "redirect:/admin/inventory";
    }

    //return orders
    @GetMapping("/returns")
    public String viewReturns(Model model) {
        model.addAttribute("currentPage", "returns");
        model.addAttribute("pageTitle", "Returns Management");
        model.addAttribute("adminName", "Admin Name");
        List<ReturnRequest> returnRequests = returnRequestRepository.findAll();
        model.addAttribute("returnRequests", returnRequests);
        return "manageReturns"; // Thymeleaf view
    }

    @PostMapping("/returns/update")
    public String updateReturnStatus(@RequestParam int returnId, @RequestParam String status) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Return request not found"));

        returnRequest.setStatus(ReturnStatus.valueOf(status.toUpperCase()));
        returnRequestRepository.save(returnRequest);

        if(status.equalsIgnoreCase("RECEIVED")){

            OrderStatus returnStatus = new OrderStatus();
            returnStatus.setStatus("Return Received");
            returnStatus.setStatusChangeDate(LocalDateTime.now());
            returnStatus.setOrder(returnRequest.getOrder());
            returnRequest.getOrder().setLatestStatus(returnStatus);

            List<OrderStatus> orderStatuses = new ArrayList<>();
            orderStatuses.add(returnStatus);
            returnRequest.getOrder().setOrderStatuses(orderStatuses);

            orderStatusRepo.save(returnStatus);
            orderRepo.save(returnRequest.getOrder());

            //for saving the balance to wallet
            walletService.addRefundToWallet(returnRequest.getOrder().getId());

        }
        else if (status.equalsIgnoreCase("REJECTED")) {
            OrderStatus returnStatus = new OrderStatus();
            returnStatus.setStatus("Return Rejected");
            returnStatus.setStatusChangeDate(LocalDateTime.now());
            returnStatus.setOrder(returnRequest.getOrder());
            returnRequest.getOrder().setLatestStatus(returnStatus);

            List<OrderStatus> orderStatuses = new ArrayList<>();
            orderStatuses.add(returnStatus);
            returnRequest.getOrder().setOrderStatuses(orderStatuses);

            orderStatusRepo.save(returnStatus);
            orderRepo.save(returnRequest.getOrder());
        }


        return "redirect:/admin/returns";
    }

    @GetMapping("/coupons")
    public String couponPage(Model model) {
        model.addAttribute("currentPage", "coupons");
        model.addAttribute("pageTitle", "Coupon Management");
        model.addAttribute("adminName", "Admin Name");

        List<Coupon> coupons = couponService.getAllCoupons();
        model.addAttribute("coupons",coupons);

        return "manageCoupon";
    }

    @PostMapping("/coupons")
    public String createCoupon(@RequestBody Coupon coupon) {
        couponService.createCoupon(coupon);
        return "redirect:/admin/coupons";
    }

    @DeleteMapping("/coupons/{id}")
    public String deleteCoupon(@PathVariable int id) {
        couponService.deleteCoupon(id);
        return "redirect:/admin/coupons";
    }

    @GetMapping("/offers")
    public String getOffers(Model model) {
        model.addAttribute("offers", offerRepo.findAll());
        model.addAttribute("products", service.getAllProducts());
        model.addAttribute("categories", service.getCategories());

        model.addAttribute("currentPage", "offers");
        model.addAttribute("pageTitle", "Offer Management");
        model.addAttribute("adminName", "Admin Name");
        return "manageOffer";
    }

    @PostMapping("/offers/add")
    public String addOffer(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) Integer categoryId,
            @ModelAttribute Offer offerReq,
            RedirectAttributes redirectAttributes) {

        Offer offer = new Offer();
        offer.setDiscountPercentage(offerReq.getDiscountPercentage());
        offer.setStartDate(offerReq.getStartDate());
        offer.setEndDate(offerReq.getEndDate());
        offer.setOfferType(offerReq.getOfferType());
        offer.setActive(offerReq.getActive());

        if (productId != null) {
            Product product = productRepo.findById(productId).orElse(null);
            if (product != null) {
                offer.setProduct(product);
                System.out.println("Product set: " + product.getName());
            }
        } else if (categoryId != null) {
            Category category = categoryRepo.findById(categoryId).orElse(null);
            if (category != null) {
                offer.setCategory(category);
                System.out.println("Category set: " + category.getCategoryname());
            }
        }

        offerRepo.save(offer);
        redirectAttributes.addFlashAttribute("success", "Offer added successfully!");
        return "redirect:/admin/offers";
    }


    @DeleteMapping("/offers/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteOffer(@PathVariable int id) {
        offerRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }


}


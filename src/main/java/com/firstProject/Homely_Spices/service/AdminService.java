package com.firstProject.Homely_Spices.service;

import com.firstProject.Homely_Spices.DTO.ChartDataDTO;
import com.firstProject.Homely_Spices.DTO.TopSellingCategoryDTO;
import com.firstProject.Homely_Spices.DTO.TopSellingProductDTO;
import com.firstProject.Homely_Spices.model.*;
import com.firstProject.Homely_Spices.repo.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {
    @Autowired
    UserRepo userRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    ProductRepo productRepo;

    @Autowired
    private QuantityRepo quantityRepo;

    @Autowired
    private OrderItemsRepo orderItemsRepo;

    @Autowired
    private OrderRepo orderRepo;

    public List<Users> getAllUsers(){
        return userRepo.findAll();
    }

    public List<Users> searchUsers(String keyword) {
        return userRepo.searchUsers(keyword);
    }

    public Users getUserById(int id){
        return userRepo.findById(id).get();
    }

    public void editUserDetails(int id, Users user) {
        Users usr = userRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        //orElseThrow is required to unwrap the Optional<User> type to User type
        usr.setUsername(user.getUsername());
        usr.setPassword(user.getPassword());
        usr.setRole(user.getRole());
        userRepo.save(usr);
    }

    public List<Category> getCategories(){
        return categoryRepo.findActiveCategories();
    }

    public void saveCategory(Category category){
        if (categoryRepo.existsByCategoryname(category.getCategoryname())) {
            throw new IllegalArgumentException("Category already exists!");
        }
        categoryRepo.save(category);
    }


    public void editCategory(int id, Category category) {
        Category cate = categoryRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        //orElseThrow is required to unwrap the Optional<User> type to User type
        cate.setCategoryname(category.getCategoryname());
        cate.setDescription(category.getDescription());
        categoryRepo.save(cate);
    }

    public List<Category> searchCategory(String keyword) {
        return categoryRepo.searchCategory(keyword);
    }

    public Category getCategoryById(int id){
        return categoryRepo.findById(id).get();
    }

    public void softDeleteCategory(int categoryId) {
        categoryRepo.softDeleteCategory(categoryId);
    }


    //add product and quantity together
    public boolean saveProduct(Product product, List<Quantity> quantities) {
        if (productRepo.existsByName(product.getName())) {
            return false; // Product already exists
        }
        Product addedProduct = productRepo.save(product);
        // Save associated quantities
        for (Quantity quantity : quantities) {
            quantity.setProduct(addedProduct);
            quantityRepo.save(quantity);
        }
        return true; // Successfully saved
    }

    public Product updateProduct(Product product, List<Quantity> quantities) {

        Product editedProduct = productRepo.save(product);
            // Save associated quantities
            for (Quantity quantity : quantities) {
                quantity.setProduct(editedProduct);
                quantityRepo.save(quantity);
            }
        return editedProduct;
    }


    public List<Product> searchProduct(String keyword) {
        return productRepo.searchProduct(keyword);
    }

    public Product getProductById(int id) {
        return productRepo.findById(id).get();
    }

    public void editProduct(int id, Product product) {
        productRepo.save(product);
    }

    public List<Product> getAllProducts() {
        return productRepo.findActiveProducts();
    }

    public void softDeleteProduct(int id) {
        productRepo.softDeleteProduct(id);
    }

    public List<TopSellingProductDTO> getTopSellingProducts() {
        List<Object[]> result = orderItemsRepo.findTopSellingProducts(PageRequest.of(0, 10));
        return result.stream()
                .map(obj -> new TopSellingProductDTO((Product) obj[0], (long) obj[1]))
                .collect(Collectors.toList());
    }

    public List<TopSellingCategoryDTO> getTopSellingCategories() {
        List<Object[]> result = orderItemsRepo.findTopSellingCategories(PageRequest.of(0, 10));
        return result.stream()
                .map(obj -> new TopSellingCategoryDTO((String) obj[0], (long) obj[1]))
                .collect(Collectors.toList());
    }

    public ChartDataDTO getChartData(String type) {
        ChartDataDTO chartData = new ChartDataDTO();
        chartData.setChartType(ChartType.valueOf(type));

        Map<String, Integer> values = new HashMap<>();

        List<Orders> orders = orderRepo.findAll();
        Set<String> dateKeys = new HashSet<>();

        for (Orders order : orders) {
            if (order.getOrderDate() != null) {
                String dateKey;
                switch (type) {
                    case "YEARLY":
                        dateKey = String.valueOf(order.getOrderDate().getYear());
                        break;
                    case "MONTHLY":
                        dateKey = order.getOrderDate().getMonth().toString();
                        break;
                    case "WEEKLY":
                        dateKey = "W-" + order.getOrderDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR);
                        break;
                    default:
                        continue;
                }

                values.put(dateKey, values.getOrDefault(dateKey, 0) + 1);
                dateKeys.add(dateKey);
            }
        }


        LocalDate now = LocalDate.now();
        switch (type) {
            case "YEARLY":
                for (int i = -1; i <= 0; i++) {
                    int year = now.getYear() + i;
                    String yearKey = String.valueOf(year);
                    values.putIfAbsent(yearKey, 0);
                }
                break;
            case "MONTHLY":
                for (int i = -4; i <= 0; i++) {
                    LocalDate monthKey = now.minusMonths(i);
                    values.putIfAbsent(monthKey.getMonth().toString(), 0);
                }
                break;
            case "WEEKLY":
                for (int i = -1; i <= 0; i++) {
                    LocalDate weekKey = now.minusWeeks(i);
                    values.putIfAbsent("W-" + weekKey.get(ChronoField.ALIGNED_WEEK_OF_YEAR), 0);
                }
                break;
        }

        chartData.setValues(values);
        return chartData;
    }

}

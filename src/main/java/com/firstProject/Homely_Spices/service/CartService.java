package com.firstProject.Homely_Spices.service;

import com.firstProject.Homely_Spices.DTO.CartRequestDTO;
import com.firstProject.Homely_Spices.DTO.CartUpdateRequestDTO;
import com.firstProject.Homely_Spices.model.Cart;
import com.firstProject.Homely_Spices.model.Product;
import com.firstProject.Homely_Spices.model.Quantity;
import com.firstProject.Homely_Spices.model.Users;
import com.firstProject.Homely_Spices.repo.CartRepo;
import com.firstProject.Homely_Spices.repo.ProductRepo;
import com.firstProject.Homely_Spices.repo.QuantityRepo;
import com.firstProject.Homely_Spices.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepo cartRepository;

    @Autowired
    private ProductRepo productRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private QuantityRepo quantityRepository;

    public void addProductToCart(CartRequestDTO cartRequest, String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        Product product = productRepository.findById(cartRequest.getProductid())
                .orElseThrow(()->new RuntimeException("Product not found"));

       Cart cart = cartRepository.findByUserProductAndPrice(user, product, cartRequest.getProductprice()).orElse(new Cart());
       System.out.println("Cart amount "+cartRequest.getProductprice());

        cart.setUser(user);
        cart.setProduct(product);
        if(cart.getProductCount()!=null) {
            cart.setProductCount(cartRequest.getProductcount() + cart.getProductCount());
        }
        else{
            cart.setProductCount(cartRequest.getProductcount());
        }

        cart.setProductPrice(cartRequest.getProductprice());
        cart.setWeight(cartRequest.getWeight());
        cart.setStock(cartRequest.getStock());

        cartRepository.save(cart);
    }

    public void updateProductCount(CartUpdateRequestDTO request, Double productPrice, int newQuantity, String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(()->new RuntimeException("Product not found"));

        System.out.println("Username: " + username);
        System.out.println("Product ID: " + request.getProductId());
        System.out.println("Product Price: " + productPrice);
        System.out.println("New Quantity: " + newQuantity);

        Cart cart = cartRepository.findByUserProductAndPrice(user, product, productPrice)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));



        cart.setProductCount(newQuantity);

        cartRepository.save(cart);
    }

    public List<Cart> getCartItems(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cartRepository.findByUser_Id(user.getId());
    }

    public void removeProductFromCart(Double price, int productId, String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(()->new RuntimeException("Product not found"));

        Cart cartItem = cartRepository.findByUserProductAndPrice(user, product, price)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartRepository.delete(cartItem);
    }

    public void clearCart(String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        List<Cart> cartItems = cartRepository.findByUser_Id(user.getId());
        cartRepository.deleteAll(cartItems);
    }


    public void removeSelectedItemsFromCart(List<Integer> itemIds) {
        cartRepository.deleteAllById(itemIds);
    }


}


package com.firstProject.Homely_Spices.DTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrderRequestDTO {
    private double deliveryFee;
    private double cartTotal;
    private int productCount;
    private int addressId;
    private int paymentId;
    private List<Integer> selectedItemIds;
    private double couponDiscount;

    //for razorpay orders
    private String razorpayPaymentId;
    private String razorpayOrderId;
    private String razorpaySignature;

    // Getters and Setters
    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }


    public double getCartTotal() {
        return cartTotal;
    }

    public void setCartTotal(double cartTotal) {
        this.cartTotal = cartTotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public List<Integer> getSelectedItemIds() {
        return selectedItemIds;
    }

    public double getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(double couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public void setSelectedItemIds(List<Integer> selectedItemIds) {
        this.selectedItemIds = selectedItemIds;
    }
    // Add a method to handle comma-separated string input
    public void setSelectedItemIds(String selectedItemIds) {
        if (selectedItemIds != null && !selectedItemIds.isEmpty()) {
            this.selectedItemIds = Arrays.stream(selectedItemIds.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } else {
            this.selectedItemIds = new ArrayList<>();
        }
    }

}


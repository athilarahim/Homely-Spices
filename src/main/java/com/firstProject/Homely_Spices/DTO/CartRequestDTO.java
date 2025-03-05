package com.firstProject.Homely_Spices.DTO;

import com.firstProject.Homely_Spices.model.Quantity;
import jakarta.validation.constraints.*;

import java.util.List;

public class CartRequestDTO {
    @NotNull(message = "Product ID is required")
    private int productid;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Quantity cannot exceed 10")
    private int productcount;

    private Double productprice;

    private String weight;

    private int stock;

    public int getProductid() {
        return productid;
    }

    public void setProductid(int productid) {
        this.productid = productid;
    }

    public int getProductcount() {
        return productcount;
    }

    public void setProductcount(int productcount) {
        this.productcount = productcount;
    }

    public Double getProductprice() {
        return productprice;
    }

    public void setProductprice(Double productprice) {
        this.productprice = productprice;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}

package com.firstProject.Homely_Spices.DTO;

public class CartUpdateRequestDTO {
    private int productId;
    private int newQuantity;

    private Double productprice;



    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(int newQuantity) {
        this.newQuantity = newQuantity;
    }

    public Double getProductprice() {
        return productprice;
    }

    public void setProductPrice(Double productprice) {
        this.productprice = productprice;
    }


}


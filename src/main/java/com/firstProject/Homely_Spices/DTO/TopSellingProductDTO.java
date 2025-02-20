package com.firstProject.Homely_Spices.DTO;


import com.firstProject.Homely_Spices.model.Product;

public class TopSellingProductDTO {
    private int productId;
    private String productName;
    private String productImage;
    private String categoryName;
    private double price;
    private long totalSold;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(long totalSold) {
        this.totalSold = totalSold;
    }

    public TopSellingProductDTO(Product product, long totalSold) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.productImage = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0)
                : "/images/imageplaceholder.jpg";
        this.categoryName = product.getCategory().getCategoryname();
        this.price = product.getPrice();
        this.totalSold = totalSold;
    }
}
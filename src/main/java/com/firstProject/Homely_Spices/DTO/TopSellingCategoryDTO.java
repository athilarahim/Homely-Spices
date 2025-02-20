package com.firstProject.Homely_Spices.DTO;

public class TopSellingCategoryDTO {
    private String categoryName;
    private long totalSold;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public long getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(long totalSold) {
        this.totalSold = totalSold;
    }

    public TopSellingCategoryDTO(String categoryName, long totalSold) {
        this.categoryName = categoryName;
        this.totalSold = totalSold;
    }
}

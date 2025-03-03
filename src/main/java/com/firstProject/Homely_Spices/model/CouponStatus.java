package com.firstProject.Homely_Spices.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CouponStatus {
    ACTIVE,
    INACTIVE;

    @JsonCreator
    public static CouponStatus fromString(String value) {
        return CouponStatus.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
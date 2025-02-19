package com.example.cafe.domain.item.entity;

import lombok.Getter;

@Getter
public enum ItemStatus {

    ON_SALE("판매중"),
    SOLD_OUT("품절");

    private final String status;

    private ItemStatus(String status) {
        this.status = status;
    }
}

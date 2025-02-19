package com.example.cafe.domain.item.entity;

import lombok.Getter;

@Getter
public enum ItemCategory {

    ARABICA("아라비카"),
    ROBUSTA("로부스타"),
    LIBERICA("리베리카"),
    DECAF("디카페인");

    private final String category;

    ItemCategory(String category) {
        this.category = category;
    }
}

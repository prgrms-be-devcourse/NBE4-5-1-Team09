package com.example.cafe.domain.item.dto;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemCategory;
import com.example.cafe.domain.item.entity.ItemStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemResponseDto {

    private Long id;
    private String itemName;
    private int price;
    private int stock;
    private String imagePath;
    private String content;
    private ItemCategory category;
    private Double avgRating;
    private ItemStatus itemStatus;

    public ItemResponseDto(Item item) {

        this.id = item.getId();
        this.itemName = item.getItemName();
        this.price = item.getPrice();
        this.stock = item.getStock();
        this.imagePath = item.getImagePath();
        this.content = item.getContent();
        this.category = item.getCategory();
        this.avgRating = item.getAvgRating();
        this.itemStatus = item.getItemStatus();
    }
}

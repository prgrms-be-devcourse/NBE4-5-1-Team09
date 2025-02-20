package com.example.cafe.domain.item.dto;

import com.example.cafe.domain.item.entity.ItemCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequestDto {

    @NotBlank(message = "상품명은 필수입니다")
    private String itemName;

    @Min(value = 0, message = "가격은 0원 이상이어야 합니다")
    private int price;

    @Min(value = 0, message = "재고는 0개 이상이어야 합니다")
    private int stock;

    private String imagePath;
    private String content;
    private ItemCategory category;
}

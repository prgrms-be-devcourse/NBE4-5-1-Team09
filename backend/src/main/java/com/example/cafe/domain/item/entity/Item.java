package com.example.cafe.domain.item.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "content")
    @Lob
    private String content;

    @Column(name = "category")
    private String category;

    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "item_status", nullable = false)
    private String itemStatus;
}

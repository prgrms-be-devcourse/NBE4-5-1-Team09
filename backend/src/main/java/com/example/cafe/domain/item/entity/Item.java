package com.example.cafe.domain.item.entity;

import com.example.cafe.domain.review.entity.Review;
import com.example.cafe.domain.trade.domain.entity.CartItem;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
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
    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "item_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemStatus itemStatus;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;
    
    public void autoCheckQuantityForSetStatus() {
        if (this.getStock() <= 0) {
            this.itemStatus = ItemStatus.SOLD_OUT;
        } else {
            this.itemStatus = ItemStatus.ON_SALE;
        }
    }
}

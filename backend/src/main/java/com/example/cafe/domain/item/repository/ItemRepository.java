package com.example.cafe.domain.item.repository;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.entity.ItemCategory;
import com.example.cafe.domain.item.entity.ItemStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id = :id")
    Optional<Item> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("update Item i set i.stock = i.stock - :quantity where i.id = :itemId and i.stock >= :quantity")
    int decreaseStock(@Param("itemId") Long itemId, @Param("quantity") int quantity);

    @Query("SELECT i FROM Item i " +
            "WHERE (:keyword IS NULL OR i.itemName LIKE %:keyword% OR i.content LIKE %:keyword%) " +
            "AND (:category IS NULL OR i.category = :category) " +
            "AND (:minPrice IS NULL OR i.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR i.price <= :maxPrice) " +
            "AND i.itemStatus = :status")
    Page<Item> searchItems(String keyword, ItemCategory category, Integer minPrice, Integer maxPrice, @Param("status") ItemStatus status, Pageable pageable);

    Page<Item> findByCategory(ItemCategory category, Pageable pageable);

    Page<Item> findByItemStatus(ItemStatus status, Pageable pageable);
}

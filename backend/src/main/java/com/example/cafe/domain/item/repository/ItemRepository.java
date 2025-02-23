package com.example.cafe.domain.item.repository;

import com.example.cafe.domain.item.entity.Item;
import jakarta.persistence.LockModeType;
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

}

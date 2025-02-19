package com.example.cafe.domain.item.controller;

import com.example.cafe.domain.item.entity.Item;
import com.example.cafe.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItem(id));
    }

}
